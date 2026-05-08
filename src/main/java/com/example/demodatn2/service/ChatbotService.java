package com.example.demodatn2.service;

import com.example.demodatn2.dto.ChatbotResponse;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.SanPham;
import com.example.demodatn2.repository.BienTheSanPhamRepository;
import com.example.demodatn2.repository.DonHangRepository;
import com.example.demodatn2.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
// Service chatbot: xử lý hỏi đáp sản phẩm và tra cứu trạng thái đơn hàng cho người dùng.
public class ChatbotService {

    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("(?i)\\bDH-[A-Z0-9]{4,12}\\b");

    private final SanPhamRepository sanPhamRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final DonHangRepository donHangRepository;
    private final GeminiClient geminiClient;

    public ChatbotResponse chat(String message, TaiKhoanDTO loginUser) {
        String input = message == null ? "" : message.trim();
        if (input.isEmpty()) {
            return ChatbotResponse.builder()
                    .reply("Bạn cần Hancos tư vấn gì? (sản phẩm, đơn hàng, chính sách)")
                    .build();
        }

        Optional<String> orderCode = extractOrderCode(input);
        if (orderCode.isPresent()) {
            if (loginUser == null) {
                return ChatbotResponse.builder()
                        .reply("Bạn vui lòng đăng nhập để tra cứu đơn hàng. Link: /login")
                        .build();
            }

            Optional<DonHang> orderOpt = donHangRepository.findByMaDonHangIgnoreCase(orderCode.get());
            if (orderOpt.isEmpty()) {
                return ChatbotResponse.builder()
                        .reply("Không tìm thấy đơn hàng với mã " + orderCode.get().toUpperCase() + ". Bạn kiểm tra lại giúp mình nhé.")
                        .build();
            }

            DonHang order = orderOpt.get();
            if (order.getTaiKhoan() == null || !order.getTaiKhoan().getId().equals(loginUser.getId())) {
                return ChatbotResponse.builder()
                        .reply("Mã đơn này không thuộc tài khoản của bạn. Nếu cần hỗ trợ, bạn có thể gửi mã đơn chính xác.")
                        .build();
            }

            String statusLabel = mapStatus(order.getTrangThai());
            String dateText = order.getNgayDat() != null
                    ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(order.getNgayDat())
                    : "";

            String reply = "Đơn " + order.getMaDonHang() + " đang ở trạng thái: " + statusLabel + ".";
            if (!dateText.isEmpty()) {
                reply += " Đặt lúc " + dateText + ".";
            }
            reply += " Xem chi tiết: /order/my-orders/" + order.getId();

            return ChatbotResponse.builder().reply(reply).build();
        }

        String productContext = buildProductContext(input);
        String systemInstruction = "Bạn là trợ lý bán hàng Hancos. Trả lời ngắn gọn, rõ ràng, tiếng Việt. " +
                "Nếu hỏi về chính sách, hướng dẫn link /chinh-sach-doi-tra. " +
                "Nếu hỏi đơn hàng mà chưa đăng nhập, hướng dẫn /login và /order/my-orders. " +
                "Chỉ dùng dữ liệu sản phẩm được cung cấp; nếu không đủ dữ liệu, hãy hỏi thêm nhu cầu (size, màu, ngân sách, giới tính).";

        String contextBlock = "DANH SÁCH SẢN PHẨM (gợi ý):\n" + productContext +
                "\nHỖ TRỢ: /chinh-sach-doi-tra (chính sách đổi trả)\n" +
                "TRANG ĐƠN HÀNG: /order/my-orders (cần đăng nhập)";

        String modelReply = geminiClient.generate(systemInstruction, input + "\n\n" + contextBlock);

        if (modelReply == null || modelReply.isBlank()) {
            return ChatbotResponse.builder()
                    .reply("Hiện chưa cấu hình Gemini hoặc kết nối lỗi. Bạn thử lại sau nhé.")
                    .build();
        }

        return ChatbotResponse.builder().reply(modelReply.trim()).build();
    }

    private Optional<String> extractOrderCode(String input) {
        Matcher matcher = ORDER_CODE_PATTERN.matcher(input);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }

    private String buildProductContext(String input) {
        List<SanPham> products;
        if (input.length() >= 2) {
            products = sanPhamRepository.searchActive(input);
        } else {
            products = sanPhamRepository.findActiveForListing();
        }

        if (products.isEmpty()) {
            products = sanPhamRepository.findActiveForListing();
        }

        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        return products.stream()
                .limit(6)
                .map(sp -> {
                    BienTheSanPhamRepository.PriceRange range = bienTheSanPhamRepository.findPriceRange(sp.getId());
                    String priceText = formatPriceRange(range, currency);
                    return "- " + sp.getTen() + " | Giá: " + priceText + " | Link: /products/" + sp.getId();
                })
                .reduce((a, b) -> a + "\n" + b)
                .orElse("(Chưa có dữ liệu sản phẩm phù hợp)");
    }

    private String formatPriceRange(BienTheSanPhamRepository.PriceRange range, NumberFormat currency) {
        if (range == null) {
            return "Liên hệ";
        }
        BigDecimal min = range.getMinGia();
        BigDecimal max = range.getMaxGia();
        if (min == null && max == null) {
            return "Liên hệ";
        }
        if (min != null && max != null && min.compareTo(max) != 0) {
            return currency.format(min) + " – " + currency.format(max);
        }
        BigDecimal value = min != null ? min : max;
        return currency.format(value);
    }

    private String mapStatus(String status) {
        if (status == null) {
            return "Không xác định";
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "CHO_XAC_NHAN", "PENDING" -> "Chờ xác nhận";
            case "DA_XAC_NHAN", "CONFIRMED", "PROCESSING" -> "Đã xác nhận";
            case "DANG_GIAO", "SHIPPING", "SHIPPED" -> "Đang giao";
            case "HOAN_THANH", "DELIVERED", "COMPLETED" -> "Hoàn thành";
            case "TRA_HANG", "RETURN_REQUESTED", "RETURNED" -> "Trả hàng";
            case "DA_HUY", "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }
}
