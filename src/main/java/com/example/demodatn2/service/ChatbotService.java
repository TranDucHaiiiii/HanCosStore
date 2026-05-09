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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("(?i)\\bDH-[A-Z0-9]{4,12}\\b");
    private static final Pattern HEIGHT_METER_PATTERN = Pattern.compile("\\b([12])m\\s*(\\d{1,2})\\b");
    private static final Pattern HEIGHT_CM_PATTERN = Pattern.compile("\\b(1[4-9]\\d|2[0-1]\\d)\\s*cm\\b");
    private static final Pattern WEIGHT_PATTERN = Pattern.compile("\\b(3\\d|4\\d|5\\d|6\\d|7\\d|8\\d|9\\d|1[0-5]\\d)\\s*kg\\b");
    private static final int PRODUCT_LIMIT = 6;

    private final SanPhamRepository sanPhamRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final DonHangRepository donHangRepository;
    private final GeminiClient geminiClient;

    public ChatbotResponse chat(String message, TaiKhoanDTO loginUser) {
        String input = message == null ? "" : message.trim();
        if (input.isEmpty()) {
            return reply("Bạn cần Hancos tư vấn gì? Bạn có thể hỏi về sản phẩm, size, đơn hàng hoặc chính sách đổi trả.");
        }

        Optional<String> fixedReply = answerByRule(input, loginUser);
        if (fixedReply.isPresent()) {
            return reply(fixedReply.get());
        }

        Optional<String> orderCode = extractOrderCode(input);
        if (orderCode.isPresent()) {
            return replyOrderStatus(orderCode.get(), loginUser);
        }

        String productContext = buildProductContext(input);
        String systemInstruction = buildSystemInstruction(loginUser);
        String contextBlock = buildContextBlock(productContext);
        String modelReply = geminiClient.generate(systemInstruction, input + "\n\n" + contextBlock);

        if (modelReply == null || modelReply.isBlank()) {
            return reply("Hiện chatbot chưa phản hồi được. Bạn thử lại sau hoặc nhắn rõ nhu cầu: sản phẩm, size, màu, ngân sách.");
        }

        return reply(cleanReply(modelReply));
    }

    private Optional<String> answerByRule(String input, TaiKhoanDTO loginUser) {
        String text = normalize(input);

        if (containsAny(text, "doi tra", "hoan tra", "tra hang", "doi hang", "bao hanh")) {
            return Optional.of("Hancos hỗ trợ đổi trả theo chính sách của cửa hàng. Bạn xem chi tiết tại /chinh-sach-doi-tra. Nếu cần đổi trả đơn đã mua, hãy chuẩn bị mã đơn hàng và tình trạng sản phẩm.");
        }

        if (containsAny(text, "don hang", "ma don", "trang thai don", "kiem tra don", "tra cuu don")) {
            if (loginUser == null) {
                return Optional.of("Bạn vui lòng đăng nhập để tra cứu đơn hàng: /login. Sau đó vào /order/my-orders hoặc gửi mã đơn dạng DH-XXXX để mình kiểm tra nhanh.");
            }
            return Optional.of("Bạn gửi mã đơn hàng dạng DH-XXXX để mình kiểm tra trạng thái. Bạn cũng có thể xem danh sách đơn tại /order/my-orders.");
        }

        if (containsAny(text, "size", "kich co", "mac vua", "chon co")) {
            Optional<String> sizeReply = answerSizeQuestion(text);
            if (sizeReply.isPresent()) {
                return sizeReply;
            }
            return Optional.of("Bạn cho mình biết chiều cao, cân nặng và kiểu mặc mong muốn (vừa người hay rộng). Mình sẽ gợi ý size phù hợp theo sản phẩm bạn đang quan tâm.");
        }

        if (containsAny(text, "ship", "giao hang", "van chuyen", "phi giao")) {
            return Optional.of("Phí giao hàng được tính ở bước thanh toán theo địa chỉ nhận hàng. Bạn thêm sản phẩm vào giỏ, nhập địa chỉ, hệ thống sẽ hiển thị phí trước khi đặt.");
        }

        if (containsAny(text, "thanh toan", "tra tien", "cod", "chuyen khoan")) {
            return Optional.of("Hancos hỗ trợ thanh toán theo các phương thức đang hiển thị ở trang thanh toán. Bạn kiểm tra lại tổng tiền, phí giao hàng và mã giảm giá trước khi xác nhận đơn.");
        }

        return Optional.empty();
    }

    private Optional<String> answerSizeQuestion(String normalizedInput) {
        Optional<Integer> heightCm = extractHeightCm(normalizedInput);
        Optional<Integer> weightKg = extractWeightKg(normalizedInput);
        if (heightCm.isEmpty() || weightKg.isEmpty()) {
            return Optional.empty();
        }

        String baseSize = recommendBaseSize(heightCm.get(), weightKg.get());
        boolean wantsLooseFit = containsAny(normalizedInput, "rong", "oversize", "thoai mai", "form rong");
        boolean wantsSlimFit = containsAny(normalizedInput, "om", "vua nguoi", "gon nguoi", "body");
        String finalSize = wantsLooseFit ? nextSize(baseSize) : baseSize;
        String fitNote = wantsLooseFit
                ? "Nếu bạn thích mặc rộng/thoải mái, nên nhích lên " + finalSize + "."
                : wantsSlimFit
                ? "Nếu bạn thích mặc gọn người, chọn " + baseSize + " là hợp lý."
                : "Nếu thích mặc rộng hơn, bạn có thể cân nhắc lên " + nextSize(baseSize) + ".";

        String heightText = formatHeight(heightCm.get());
        return Optional.of("Với chiều cao " + heightText + " và cân nặng " + weightKg.get()
                + "kg, mình gợi ý bạn chọn size " + finalSize + ". " + fitNote
                + " Size có thể lệch nhẹ tùy form từng sản phẩm, nên bạn nên xem thêm size đang có ở trang chi tiết sản phẩm.");
    }

    private Optional<Integer> extractHeightCm(String normalizedInput) {
        Matcher meterMatcher = HEIGHT_METER_PATTERN.matcher(normalizedInput);
        if (meterMatcher.find()) {
            int meters = Integer.parseInt(meterMatcher.group(1));
            int centimeters = Integer.parseInt(meterMatcher.group(2));
            if (centimeters < 10) {
                centimeters *= 10;
            }
            int height = meters * 100 + centimeters;
            if (height >= 140 && height <= 220) {
                return Optional.of(height);
            }
        }

        Matcher cmMatcher = HEIGHT_CM_PATTERN.matcher(normalizedInput);
        if (cmMatcher.find()) {
            return Optional.of(Integer.parseInt(cmMatcher.group(1)));
        }

        return Optional.empty();
    }

    private Optional<Integer> extractWeightKg(String normalizedInput) {
        Matcher matcher = WEIGHT_PATTERN.matcher(normalizedInput);
        if (matcher.find()) {
            return Optional.of(Integer.parseInt(matcher.group(1)));
        }
        return Optional.empty();
    }

    private String recommendBaseSize(int heightCm, int weightKg) {
        if (weightKg <= 45 && heightCm <= 160) {
            return "XS";
        }
        if (weightKg <= 52 && heightCm <= 166) {
            return "S";
        }
        if (weightKg <= 60 && heightCm <= 172) {
            return "M";
        }
        if (weightKg <= 70 && heightCm <= 178) {
            return "L";
        }
        if (weightKg <= 82 && heightCm <= 185) {
            return "XL";
        }
        return "XXL";
    }

    private String nextSize(String size) {
        return switch (size) {
            case "XS" -> "S";
            case "S" -> "M";
            case "M" -> "L";
            case "L" -> "XL";
            case "XL" -> "XXL";
            default -> size;
        };
    }

    private String formatHeight(int heightCm) {
        return (heightCm / 100) + "m" + String.format("%02d", heightCm % 100);
    }

    private ChatbotResponse replyOrderStatus(String orderCode, TaiKhoanDTO loginUser) {
        if (loginUser == null) {
            return reply("Bạn vui lòng đăng nhập để tra cứu đơn hàng. Link: /login");
        }

        Optional<DonHang> orderOpt = donHangRepository.findByMaDonHangIgnoreCase(orderCode);
        if (orderOpt.isEmpty()) {
            return reply("Không tìm thấy đơn hàng với mã " + orderCode.toUpperCase() + ". Bạn kiểm tra lại mã đơn giúp mình nhé.");
        }

        DonHang order = orderOpt.get();
        if (order.getTaiKhoan() == null || !order.getTaiKhoan().getId().equals(loginUser.getId())) {
            return reply("Mã đơn này không thuộc tài khoản của bạn. Nếu cần hỗ trợ, bạn hãy gửi đúng mã đơn trong tài khoản đang đăng nhập.");
        }

        String statusLabel = mapStatus(order.getTrangThai());
        String dateText = order.getNgayDat() != null
                ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(order.getNgayDat())
                : "";

        StringBuilder result = new StringBuilder("Đơn ")
                .append(order.getMaDonHang())
                .append(" đang ở trạng thái: ")
                .append(statusLabel)
                .append(".");
        if (!dateText.isEmpty()) {
            result.append(" Đặt lúc ").append(dateText).append(".");
        }
        result.append(" Xem chi tiết: /order/my-orders/").append(order.getId());

        return reply(result.toString());
    }

    private String buildSystemInstruction(TaiKhoanDTO loginUser) {
        return """
                Bạn là trợ lý bán hàng của Hancos, một cửa hàng thời trang tối giản.
                Trả lời bằng tiếng Việt tự nhiên, thân thiện vừa phải, rõ ý và ngắn gọn.
                Không dùng markdown, không dùng dấu **, không bịa thông tin ngoài dữ liệu được cung cấp.
                Khi tư vấn sản phẩm, ưu tiên nêu tên sản phẩm, giá, màu, size và link.
                Khi khách hỏi phối đồ/outfit, hãy tạo 1 đến 3 combo từ sản phẩm Hancos đang được cung cấp.
                Mỗi combo cần có mục đích mặc, các món phối với nhau, lý do hợp phong cách và link sản phẩm nếu có.
                Ưu tiên phối đồ tối giản, dễ mặc, màu trung tính; nếu thiếu áo/quần/khoác thì nói rõ và đề xuất món gần nhất đang có.
                Nếu thiếu dữ liệu để tư vấn, hỏi thêm tối đa 2 thông tin quan trọng như size, màu, ngân sách hoặc phong cách.
                Nếu khách hỏi chính sách, điều hướng tới /chinh-sach-doi-tra.
                Nếu khách hỏi đơn hàng, yêu cầu mã đơn dạng DH-XXXX và đăng nhập nếu cần.
                Trạng thái đăng nhập của khách: %s.
                """.formatted(loginUser == null ? "chưa đăng nhập" : "đã đăng nhập");
    }

    private String buildContextBlock(String productContext) {
        return "DỮ LIỆU HANCOS:\n"
                + "Sản phẩm gợi ý:\n" + productContext + "\n\n"
                + "Gợi ý phối đồ:\n"
                + "- Casual tối giản: áo thun/áo len màu trung tính + quần jeans/quần short + áo khoác nhẹ nếu có.\n"
                + "- Đi học/đi làm nhẹ nhàng: áo form gọn + quần tối màu + màu trắng/đen/xám/be để dễ phối.\n"
                + "- Đi chơi: chọn một món nổi bật, các món còn lại giữ màu trung tính để outfit sạch và dễ mặc.\n"
                + "- Nếu khách đưa chiều cao/cân nặng, kết hợp gợi ý size với combo phối đồ.\n\n"
                + "Link chính sách đổi trả: /chinh-sach-doi-tra\n"
                + "Link đăng nhập: /login\n"
                + "Link đơn hàng của khách: /order/my-orders\n"
                + "Quy tắc trả lời: chỉ tư vấn theo dữ liệu trên; không tự tạo giá, màu, size hoặc tồn kho.";
    }

    private Optional<String> extractOrderCode(String input) {
        Matcher matcher = ORDER_CODE_PATTERN.matcher(input);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }

    private String buildProductContext(String input) {
        List<SanPham> products = input.length() >= 2
                ? sanPhamRepository.searchActive(input)
                : sanPhamRepository.findActiveForListing();

        if (products.isEmpty()) {
            products = sanPhamRepository.findActiveForListing();
        }

        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        return products.stream()
                .limit(PRODUCT_LIMIT)
                .map(sp -> describeProduct(sp, currency))
                .collect(Collectors.joining("\n"));
    }

    private String describeProduct(SanPham sp, NumberFormat currency) {
        BienTheSanPhamRepository.PriceRange range = bienTheSanPhamRepository.findPriceRange(sp.getId());
        String colors = joinValues(bienTheSanPhamRepository.findDistinctMauSac(sp.getId()));
        String sizes = joinValues(bienTheSanPhamRepository.findDistinctKichCo(sp.getId()));

        return "- " + sp.getTen()
                + " | Giá: " + formatPriceRange(range, currency)
                + " | Màu: " + colors
                + " | Size: " + sizes
                + " | Chất liệu: " + valueOrDefault(sp.getChatLieu(), "chưa cập nhật")
                + " | Dành cho: " + valueOrDefault(sp.getGioiTinh(), "unisex")
                + " | Link: /products/" + sp.getId();
    }

    private String joinValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "chưa cập nhật";
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .limit(8)
                .collect(Collectors.joining(", "));
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
            return currency.format(min) + " - " + currency.format(max);
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

    private ChatbotResponse reply(String text) {
        return ChatbotResponse.builder().reply(text).build();
    }

    private String cleanReply(String reply) {
        return reply
                .replace("**", "")
                .replace("__", "")
                .replace("```", "")
                .trim();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        normalized = normalized.replace('đ', 'd');
        normalized = java.text.Normalizer.normalize(normalized, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
