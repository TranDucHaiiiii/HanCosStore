package com.example.demodatn2.controller;

import com.example.demodatn2.dto.CartItemDTO;
import com.example.demodatn2.dto.GhtkFeeRequest;
import com.example.demodatn2.dto.GhtkFeeResponse;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.ChiTietDonHang;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.HinhAnhSanPham;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.repository.DiaChiGiaoHangRepository;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.service.CartService;
import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.service.GhtkService;
import com.example.demodatn2.service.OrderService;
import com.example.demodatn2.service.ProductReviewService;
import com.example.demodatn2.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private static final String LOGIN_USER = "LOGIN_USER";
    private static final String APPLIED_VOUCHER_CODE = "APPLIED_VOUCHER_CODE";
    private static final String DISCOUNT_AMOUNT = "DISCOUNT_AMOUNT";
    private static final String CURRENT_SHIPPING_FEE = "CURRENT_SHIPPING_FEE";

    private final OrderService orderService;
    private final CartService cartService;
    private final DanhMucService danhMucService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final VoucherService voucherService;
    private final DiaChiGiaoHangRepository diaChiGiaoHangRepository;
    private final ProductReviewService productReviewService;
    private final GhtkService ghtkService;

    @Value("${app.order.pending-transfer-timeout-minutes:30}")
    private int pendingTransferTimeoutMinutes;

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        List<CartItemDTO> items = cartService.getSelectedCartItems(session);
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một sản phẩm để thanh toán.");
            return "redirect:/cart";
        }

        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để tiếp tục mua hàng.");
            return "redirect:/login?next=/order/checkout";
        }

        BigDecimal total = cartService.getTotalAmount(items);
        session.setAttribute(CURRENT_SHIPPING_FEE, BigDecimal.ZERO);

        // Luôn tính toán lại voucher khi vào trang thanh toán để đảm bảo chính xác nhất
        String voucherCode = (String) session.getAttribute(APPLIED_VOUCHER_CODE);
        BigDecimal discount = BigDecimal.ZERO;

        if (voucherCode != null) {
            var voucherOpt = voucherService.validateVoucher(voucherCode, total);
            if (voucherOpt.isPresent()) {
                discount = voucherService.calculateDiscount(voucherOpt.get(), total);
                session.setAttribute(DISCOUNT_AMOUNT, discount);
            } else {
                clearVoucher(session);
                model.addAttribute("voucherWarning", "Mã giảm giá đã bị gỡ do không còn đủ điều kiện.");
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("discount", discount);
        model.addAttribute("shippingFee", BigDecimal.ZERO);
        model.addAttribute("finalTotal", total.subtract(discount));
        model.addAttribute("user", loginUser);
        model.addAttribute("categories", danhMucService.getActive());
        model.addAttribute("eligibleVouchers", voucherService.getEligibleVouchers(total));
        model.addAttribute("appliedVoucherCode", voucherCode);

        model.addAttribute("savedAddresses",
                diaChiGiaoHangRepository.findByTaiKhoanIdOrderByLaMacDinhDescNgayTaoDesc(loginUser.getId()));

        return "checkout";
    }

    @PostMapping("/checkout/apply-voucher")
    @ResponseBody
    public Map<String, Object> applyVoucherAtCheckout(@RequestParam String code, HttpSession session) {
        List<CartItemDTO> items = cartService.getSelectedCartItems(session);
        BigDecimal total = cartService.getTotalAmount(items);

        var voucherOpt = voucherService.validateVoucher(code, total);
        if (voucherOpt.isPresent()) {
            var voucher = voucherOpt.get();
            BigDecimal discount = voucherService.calculateDiscount(voucher, total);
            session.setAttribute(APPLIED_VOUCHER_CODE, voucher.getMa());
            session.setAttribute(DISCOUNT_AMOUNT, discount);
            return Map.of(
                    "success", true,
                    "discount", discount,
                    "finalTotal", total.subtract(discount),
                    "code", voucher.getMa()
            );
        }
        return Map.of("success", false, "message", "Mã giảm giá không hợp lệ hoặc không đủ điều kiện");
    }

    @PostMapping("/checkout/remove-voucher")
    @ResponseBody
    public Map<String, Object> removeVoucherAtCheckout(HttpSession session) {
        clearVoucher(session);
        List<CartItemDTO> items = cartService.getSelectedCartItems(session);
        BigDecimal total = cartService.getTotalAmount(items);
        return Map.of(
                "success", true,
                "finalTotal", total
        );
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String hoTen,
                             @RequestParam String soDienThoai,
                             @RequestParam String email,
                             @RequestParam String diaChi,
                             @RequestParam(required = false) String ghiChu,
                             @RequestParam(defaultValue = "COD") String payment,
                             @RequestParam(required = false, defaultValue = "0") BigDecimal shippingFee,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để tiếp tục mua hàng.");
            return "redirect:/login?next=/order/checkout";
        }

        try {
            BigDecimal resolvedShippingFee = resolveShippingFee(session, shippingFee);
            session.setAttribute(CURRENT_SHIPPING_FEE, resolvedShippingFee);

            DonHang donHang = orderService.createOrder(hoTen, soDienThoai, email, diaChi, ghiChu, payment, resolvedShippingFee, session);
            session.setAttribute("CART_COUNT", cartService.getItemCount(session));
            return "redirect:/order/success?id=" + donHang.getId();
        } catch (Exception e) {
            log.warn("Failed to place order", e);
            model.addAttribute("errorMessage", e.getMessage());
            return checkout(session, model, redirectAttributes);
        }
    }

    @GetMapping("/success")
    public String success(@RequestParam Integer id, Model model) {
        DonHang order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("categories", danhMucService.getActive());
        model.addAttribute("paymentDeadlineMillis", getPaymentDeadlineMillis(order));
        return "order-success";
    }

    @GetMapping("/my-orders")
    public String myOrders(@RequestParam(defaultValue = "ALL") String status, HttpSession session, Model model) {
        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        TaiKhoan taiKhoan = taiKhoanRepository.findById(loginUser.getId()).orElseThrow();
        List<DonHang> orders = orderService.getOrdersByAccount(taiKhoan);
        String normalizedStatus = normalizeOrderStatus(status);

        List<DonHang> filteredOrders = switch (normalizedStatus) {
            case "CHO_XAC_NHAN" -> orders.stream()
                    .filter(o -> "CHO_XAC_NHAN".equals(normalizeOrderStatus(o.getTrangThai())))
                    .toList();
            case "DA_XAC_NHAN" -> orders.stream()
                    .filter(o -> "DA_XAC_NHAN".equals(normalizeOrderStatus(o.getTrangThai())))
                    .toList();
            case "DANG_GIAO" -> orders.stream()
                    .filter(o -> "DANG_GIAO".equals(normalizeOrderStatus(o.getTrangThai())))
                    .toList();
            case "HOAN_THANH" -> orders.stream()
                    .filter(o -> "HOAN_THANH".equals(normalizeOrderStatus(o.getTrangThai())))
                    .toList();
            case "DA_HUY" -> orders.stream()
                    .filter(o -> "DA_HUY".equals(normalizeOrderStatus(o.getTrangThai())))
                    .toList();
            case "TRA_HANG" -> orders.stream()
                    .filter(o -> "TRA_HANG".equals(normalizeOrderStatus(o.getTrangThai())))
                    .toList();
            default -> orders;
        };

        Instant now = Instant.now();
        Set<Integer> returnableOrderIds = orders.stream()
                .filter(o -> canRequestReturn(o, now))
                .filter(o -> orderService.getReturnRequest(o.getId()).isEmpty())
                .map(DonHang::getId)
                .collect(Collectors.toSet());

        model.addAttribute("orders", orders);
        model.addAttribute("filteredOrders", filteredOrders);
        model.addAttribute("returnableOrderIds", returnableOrderIds);
        model.addAttribute("paymentDeadlines", buildPaymentDeadlineMap(orders));
        model.addAttribute("activeFilter", normalizedStatus);
        model.addAttribute("categories", danhMucService.getActive());
        return "my-orders";
    }

    @GetMapping("/my-orders/{id}")
    public String myOrderDetail(@PathVariable Integer id, HttpSession session, Model model) {
        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        DonHang order = orderService.getOrderById(id);
        // Bảo mật: chỉ cho xem đơn hàng của chính mình
        if (!isOwner(order, loginUser)) {
            return "redirect:/order/my-orders";
        }

        var returnRequest = orderService.getReturnRequest(id);
        boolean canRequestReturn = canRequestReturn(order, Instant.now()) && returnRequest.isEmpty();
        List<ChiTietDonHang> orderItems = orderService.getOrderItems(id);
        Map<Integer, String> itemImages = new HashMap<>();
        Map<Integer, Boolean> reviewedProductMap = productReviewService.getReviewedProductMap(orderItems, loginUser.getId());

        for (ChiTietDonHang item : orderItems) {
            itemImages.put(item.getId(), getItemImagePath(item));
        }

        model.addAttribute("order", order);
        model.addAttribute("items", orderItems);
        model.addAttribute("itemImages", itemImages);
        model.addAttribute("reviewedProductMap", reviewedProductMap);
        model.addAttribute("canRequestReturn", canRequestReturn);
        model.addAttribute("categories", danhMucService.getActive());
        model.addAttribute("paymentDeadlineMillis", getPaymentDeadlineMillis(order));
        returnRequest.ifPresent(r -> model.addAttribute("returnRequest", r));
        return "my-order-detail";
    }

    private TaiKhoanDTO getLoginUser(HttpSession session) {
        return (TaiKhoanDTO) session.getAttribute(LOGIN_USER);
    }

    private void clearVoucher(HttpSession session) {
        session.removeAttribute(APPLIED_VOUCHER_CODE);
        session.removeAttribute(DISCOUNT_AMOUNT);
    }

    private boolean isOwner(DonHang order, TaiKhoanDTO loginUser) {
        return order != null
                && order.getTaiKhoan() != null
                && loginUser != null
                && order.getTaiKhoan().getId().equals(loginUser.getId());
    }

    private String getItemImagePath(ChiTietDonHang item) {
        if (item == null || item.getBienTheSanPham() == null) {
            return "/images/no-image.png";
        }

        var product = item.getBienTheSanPham().getSanPham();
        if (product == null || product.getHinhAnhSanPhams() == null || product.getHinhAnhSanPhams().isEmpty()) {
            return "/images/no-image.png";
        }

        List<HinhAnhSanPham> images = product.getHinhAnhSanPhams();
        return images.stream()
                .filter(image -> Boolean.TRUE.equals(image.getLaAnhChinh()))
                .map(HinhAnhSanPham::getDuongDanAnh)
                .findFirst()
                .orElse(images.get(0).getDuongDanAnh());
    }

    private boolean canRequestReturn(DonHang order, Instant now) {
        if (order == null || order.getTrangThai() == null || order.getNgayDat() == null) {
            return false;
        }

        boolean rightStatus = "HOAN_THANH".equals(normalizeOrderStatus(order.getTrangThai()));
        if (!rightStatus) {
            return false;
        }

        return !order.getNgayDat().plus(7, ChronoUnit.DAYS).isBefore(now);
    }

    private Map<Integer, Long> buildPaymentDeadlineMap(List<DonHang> orders) {
        if (orders == null || orders.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Long> deadlines = new HashMap<>();
        for (DonHang order : orders) {
            if (order == null || order.getId() == null) {
                continue;
            }
            Long deadline = getPaymentDeadlineMillis(order);
            if (deadline != null) {
                deadlines.put(order.getId(), deadline);
            }
        }
        return deadlines;
    }

    private Long getPaymentDeadlineMillis(DonHang order) {
        if (order == null || order.getNgayDat() == null || pendingTransferTimeoutMinutes <= 0) {
            return null;
        }
        String status = normalizeOrderStatus(order.getTrangThai());
        if (!"CHO_XAC_NHAN".equals(status) || !isBankTransferPayment(order.getPhuongThucThanhToan())) {
            return null;
        }
        return order.getNgayDat()
                .plus(pendingTransferTimeoutMinutes, ChronoUnit.MINUTES)
                .toEpochMilli();
    }

    private boolean isBankTransferPayment(String paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        String normalized = paymentMethod.trim().toUpperCase();
        return normalized.contains("SEPAY")
                || normalized.contains("TRANSFER")
                || normalized.contains("CHUYEN_KHOAN")
                || normalized.contains("CHUYENKHOAN")
                || normalized.contains("CHUYEN KHOAN");
    }

    private String normalizeOrderStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "ALL";
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "PENDING" -> "CHO_XAC_NHAN";
            case "PAID" -> "DA_XAC_NHAN";
            case "CONFIRMED" -> "DA_XAC_NHAN";
            case "SHIPPING" -> "DANG_GIAO";
            case "DELIVERED", "COMPLETED" -> "HOAN_THANH";
            case "CANCELLED" -> "DA_HUY";
            case "RETURN_REQUESTED", "RETURNED" -> "TRA_HANG";
            default -> normalized;
        };
    }

    private BigDecimal resolveShippingFee(HttpSession session, BigDecimal shippingFeeFromForm) {
        Object sessionShippingFee = session.getAttribute(CURRENT_SHIPPING_FEE);
        if (sessionShippingFee instanceof BigDecimal fee && fee.compareTo(BigDecimal.ZERO) > 0) {
            return fee;
        }
        if (sessionShippingFee instanceof Number number) {
            BigDecimal fee = BigDecimal.valueOf(number.doubleValue());
            if (fee.compareTo(BigDecimal.ZERO) > 0) {
                return fee;
            }
        }

        return shippingFeeFromForm != null && shippingFeeFromForm.compareTo(BigDecimal.ZERO) > 0
                ? shippingFeeFromForm
                : BigDecimal.ZERO;
    }

    @PostMapping("/return/{id}")
    @ResponseBody
    public String requestReturn(@PathVariable Integer id,
                                @RequestParam String reason,
                                @RequestParam(value = "image", required = false) MultipartFile image,
                                HttpSession session) {
        return "Vui lòng gửi yêu cầu trả hàng bằng form chọn sản phẩm trả hàng mới.";
    }

    @PostMapping("/cancel/{id}")
    @ResponseBody
    public String cancelOrder(@PathVariable Integer id, @RequestParam String reason, HttpSession session) {
        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "Bạn cần đăng nhập để thực hiện thao tác này.";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            // Bảo mật: chỉ cho hủy đơn hàng của chính mình
            if (!isOwner(order, loginUser)) {
                return "Bạn không có quyền hủy đơn hàng này.";
            }

            orderService.cancelOrder(id, reason, false);
            return "SUCCESS";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/update-address/{id}")
    @ResponseBody
    public String updateAddress(@PathVariable Integer id,
                                @RequestParam String hoTen,
                                @RequestParam String soDienThoai,
                                @RequestParam String diaChi,
                                @RequestParam(required = false) String province,
                                @RequestParam(required = false) String district,
                                @RequestParam(required = false) String ward,
                                @RequestParam(required = false) String detailAddress,
                                HttpSession session) {
        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "Bạn cần đăng nhập để thực hiện thao tác này.";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            // Bảo mật: chỉ cho sửa đơn hàng của chính mình
            if (!isOwner(order, loginUser)) {
                return "Bạn không có quyền chỉnh sửa đơn hàng này.";
            }

            BigDecimal shippingFee = calculateShippingFeeForOrder(order, province, district, ward, detailAddress);
            orderService.updateOrderAddress(id, hoTen, soDienThoai, diaChi, shippingFee);
            return "SUCCESS";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/my-orders/{id}/cancel")
    public String cancelOrderFromDetail(@PathVariable Integer id,
                                        @RequestParam String reason,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            if (!isOwner(order, loginUser)) {
                redirectAttributes.addFlashAttribute("actionError", "Bạn không có quyền hủy đơn hàng này.");
                return "redirect:/order/my-orders";
            }

            orderService.cancelOrder(id, reason, false);
            redirectAttributes.addFlashAttribute("actionSuccess", "Đơn hàng của bạn đã được hủy.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("actionError", e.getMessage());
        }

        return "redirect:/order/my-orders/" + id;
    }

    @PostMapping("/my-orders/{id}/return-request")
    public String requestReturnFromDetail(@PathVariable Integer id,
                                          @RequestParam String reason,
                                          @RequestParam(value = "image", required = false) MultipartFile image,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            redirectAttributes.addFlashAttribute("actionError", "Vui lòng gửi yêu cầu trả hàng bằng form chọn sản phẩm trả hàng mới.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("actionError", e.getMessage());
        }

        return "redirect:/order/my-orders/" + id;
    }

    @PostMapping("/my-orders/{id}/update-address")
    public String updateAddressFromDetail(@PathVariable Integer id,
                                          @RequestParam String hoTen,
                                          @RequestParam String soDienThoai,
                                          @RequestParam String diaChi,
                                          @RequestParam(required = false) String province,
                                          @RequestParam(required = false) String district,
                                          @RequestParam(required = false) String ward,
                                          @RequestParam(required = false) String detailAddress,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            if (!isOwner(order, loginUser)) {
                redirectAttributes.addFlashAttribute("actionError", "Bạn không có quyền chỉnh sửa đơn hàng này.");
                return "redirect:/order/my-orders";
            }

            BigDecimal shippingFee = calculateShippingFeeForOrder(order, province, district, ward, detailAddress);
            orderService.updateOrderAddress(id, hoTen, soDienThoai, diaChi, shippingFee);
            redirectAttributes.addFlashAttribute("actionSuccess", "Địa chỉ giao hàng đã được cập nhật.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("actionError", e.getMessage());
        }

        return "redirect:/order/my-orders/" + id;
    }

    @PostMapping("/my-orders/{id}/update-payment-method")
    public String updatePaymentMethodFromDetail(@PathVariable Integer id,
                                                @RequestParam String paymentMethod,
                                                HttpSession session,
                                                RedirectAttributes redirectAttributes) {
        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            if (!isOwner(order, loginUser)) {
                redirectAttributes.addFlashAttribute("actionError", "Bạn không có quyền chỉnh sửa đơn hàng này.");
                return "redirect:/order/my-orders";
            }

            orderService.updateOrderPaymentMethod(id, paymentMethod);
            redirectAttributes.addFlashAttribute("actionSuccess", "Phương thức thanh toán đã được cập nhật.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("actionError", e.getMessage());
        }

        return "redirect:/order/my-orders/" + id;
    }

    private BigDecimal calculateShippingFeeForOrder(DonHang order,
                                                    String province,
                                                    String district,
                                                    String ward,
                                                    String detailAddress) {
        if (isBlank(province) || isBlank(district) || isBlank(ward) || isBlank(detailAddress)) {
            throw new RuntimeException("Vui lòng chọn đầy đủ địa chỉ để tính lại phí ship.");
        }

        GhtkFeeRequest request = new GhtkFeeRequest();
        request.setProvince(province);
        request.setDistrict(district);
        request.setWard(ward);
        request.setAddress(detailAddress);
        request.setWeight(calculateOrderWeightGram(order));
        request.setValue(toIntegerValue(order.getTamTinh()));

        GhtkFeeResponse response = ghtkService.calculateFeeWithDefaultPick(request);
        Integer fee = response != null && response.getFee() != null ? response.getFee().getFee() : null;
        if (fee == null) {
            throw new RuntimeException("Không thể tính lại phí ship cho địa chỉ mới.");
        }
        return BigDecimal.valueOf(fee.longValue());
    }

    private int calculateOrderWeightGram(DonHang order) {
        int total = orderService.getOrderItems(order.getId()).stream()
                .mapToInt(item -> {
                    Integer gram = item.getBienTheSanPham() != null
                            ? item.getBienTheSanPham().getKhoiLuongGram()
                            : null;
                    int safeGram = gram == null || gram <= 0 ? 100 : gram;
                    int quantity = item.getSoLuong() == null || item.getSoLuong() <= 0 ? 1 : item.getSoLuong();
                    return safeGram * quantity;
                })
                .sum();
        return total > 0 ? total : 100;
    }

    private Integer toIntegerValue(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return 0;
        }
        try {
            return totalAmount.setScale(0, RoundingMode.HALF_UP).intValueExact();
        } catch (ArithmeticException ex) {
            return totalAmount.intValue();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @PostMapping("/my-orders/{id}/review")
    public String reviewProductFromOrder(@PathVariable Integer id,
                                         @RequestParam Integer orderItemId,
                                         @RequestParam Integer soSao,
                                         @RequestParam(required = false) String noiDung,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        TaiKhoanDTO loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            if (!isOwner(order, loginUser)) {
                redirectAttributes.addFlashAttribute("actionError", "Ban khong co quyen danh gia don hang nay.");
                return "redirect:/order/my-orders";
            }

            productReviewService.submitReview(orderItemId, loginUser.getId(), soSao, noiDung);
            redirectAttributes.addFlashAttribute("actionSuccess", "Cam on ban da danh gia san pham.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("actionError", e.getMessage());
        }

        return "redirect:/order/my-orders/" + id;
    }
}
