package com.example.demodatn2.controller;

import com.example.demodatn2.dto.CartItemDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.ChiTietDonHang;
import com.example.demodatn2.entity.DiaChiGiaoHang;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.HinhAnhSanPham;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.repository.DiaChiGiaoHangRepository;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.service.CartService;
import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.service.OrderService;
import com.example.demodatn2.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final DanhMucService danhMucService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final VoucherService voucherService;
    private final DiaChiGiaoHangRepository diaChiGiaoHangRepository;

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        List<CartItemDTO> items = cartService.getCartItems(session);
        if (items.isEmpty()) {
            return "redirect:/cart";
        }
        
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để tiếp tục mua hàng.");
            return "redirect:/login?next=/order/checkout";
        }

        BigDecimal total = cartService.getTotalAmount(items);
        
        // Luôn tính toán lại voucher khi vào trang thanh toán để đảm bảo chính xác nhất
        String voucherCode = (String) session.getAttribute("APPLIED_VOUCHER_CODE");
        BigDecimal discount = BigDecimal.ZERO;
        
        if (voucherCode != null) {
            var voucherOpt = orderService.getVoucherService().validateVoucher(voucherCode, total);
            if (voucherOpt.isPresent()) {
                discount = orderService.getVoucherService().calculateDiscount(voucherOpt.get(), total);
                session.setAttribute("DISCOUNT_AMOUNT", discount);
            } else {
                session.removeAttribute("APPLIED_VOUCHER_CODE");
                session.removeAttribute("DISCOUNT_AMOUNT");
                model.addAttribute("voucherWarning", "Mã giảm giá đã bị gỡ do không còn đủ điều kiện.");
            }
        }
        
        BigDecimal shippingFee = CartService.calculateShippingFee(total);
        
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("discount", discount);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("finalTotal", total.subtract(discount).add(shippingFee));
        model.addAttribute("user", loginUser);
        model.addAttribute("categories", danhMucService.getActive());
        model.addAttribute("eligibleVouchers", voucherService.getEligibleVouchers(total));
        model.addAttribute("appliedVoucherCode", voucherCode);

        if (loginUser != null) {
            model.addAttribute("savedAddresses",
                    diaChiGiaoHangRepository.findByTaiKhoanIdOrderByLaMacDinhDescNgayTaoDesc(loginUser.getId()));
        }
        
        return "checkout";
    }

    @PostMapping("/checkout/apply-voucher")
    @ResponseBody
    public Map<String, Object> applyVoucherAtCheckout(@RequestParam String code, HttpSession session) {
        List<CartItemDTO> items = cartService.getCartItems(session);
        BigDecimal total = cartService.getTotalAmount(items);

        var voucherOpt = voucherService.validateVoucher(code, total);
        if (voucherOpt.isPresent()) {
            var voucher = voucherOpt.get();
            BigDecimal discount = voucherService.calculateDiscount(voucher, total);
            session.setAttribute("APPLIED_VOUCHER_CODE", voucher.getMa());
            session.setAttribute("DISCOUNT_AMOUNT", discount);
            BigDecimal shippingFee = CartService.calculateShippingFee(total);
            return Map.of("success", true, "discount", discount, "shippingFee", shippingFee, "finalTotal", total.subtract(discount).add(shippingFee), "code", voucher.getMa());
        }
        return Map.of("success", false, "message", "Mã giảm giá không hợp lệ hoặc không đủ điều kiện");
    }

    @PostMapping("/checkout/remove-voucher")
    @ResponseBody
    public Map<String, Object> removeVoucherAtCheckout(HttpSession session) {
        session.removeAttribute("APPLIED_VOUCHER_CODE");
        session.removeAttribute("DISCOUNT_AMOUNT");
        List<CartItemDTO> items = cartService.getCartItems(session);
        BigDecimal total = cartService.getTotalAmount(items);
        BigDecimal shippingFee = CartService.calculateShippingFee(total);
        return Map.of("success", true, "shippingFee", shippingFee, "finalTotal", total.add(shippingFee));
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
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để tiếp tục mua hàng.");
            return "redirect:/login?next=/order/checkout";
        }

        try {
            BigDecimal resolvedShippingFee = resolveShippingFee(session, shippingFee);
            session.setAttribute("CURRENT_SHIPPING_FEE", resolvedShippingFee);

            DonHang donHang = orderService.createOrder(hoTen, soDienThoai, email, diaChi, ghiChu, payment, resolvedShippingFee, session);
            session.setAttribute("CART_COUNT", 0);
            return "redirect:/order/success?id=" + donHang.getId();
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return checkout(session, model, redirectAttributes);
        }
    }




//        try {
//            DonHang donHang = orderService.createOrder(hoTen, soDienThoai, email, diaChi, ghiChu, payment, session);
//            session.setAttribute("CART_COUNT", 0);
//            return "redirect:/order/success?id=" + donHang.getId();
//        } catch (Exception e) {
//            model.addAttribute("errorMessage", e.getMessage());
//            return checkout(session, model, redirectAttributes);
//        }


    @GetMapping("/success")
    public String success(@RequestParam Integer id, Model model) {
        DonHang order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("categories", danhMucService.getActive());
        return "order-success";
    }

    @GetMapping("/my-orders")
    public String myOrders(@RequestParam(defaultValue = "ALL") String status, HttpSession session, Model model) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }
        
        TaiKhoan taiKhoan = taiKhoanRepository.findById(loginUser.getId()).orElseThrow();
        List<DonHang> orders = orderService.getOrdersByAccount(taiKhoan);
        String normalizedStatus = normalizeOrderStatus(status);

        List<DonHang> filteredOrders = switch (normalizedStatus) {
            case "CHO_XAC_NHAN" -> orders.stream()
                .filter(o -> "CHO_XAC_NHAN".equals(normalizeOrderStatus(o.getTrangThai()))
                    || "DA_XAC_NHAN".equals(normalizeOrderStatus(o.getTrangThai())))
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
                .map(DonHang::getId)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        
        model.addAttribute("orders", orders);
        model.addAttribute("filteredOrders", filteredOrders);
            model.addAttribute("returnableOrderIds", returnableOrderIds);
        model.addAttribute("activeFilter", normalizedStatus);
        model.addAttribute("categories", danhMucService.getActive());
        return "my-orders";
    }

    @GetMapping("/my-orders/{id}")
    public String myOrderDetail(@PathVariable Integer id, HttpSession session, Model model) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        DonHang order = orderService.getOrderById(id);
        // Bảo mật: chỉ cho xem đơn hàng của chính mình
        if (!order.getTaiKhoan().getId().equals(loginUser.getId())) {
            return "redirect:/order/my-orders";
        }

        boolean canRequestReturn = canRequestReturn(order, Instant.now());
        List<ChiTietDonHang> orderItems = orderService.getOrderItems(id);
        Map<Integer, String> itemImages = new HashMap<>();

        for (ChiTietDonHang item : orderItems) {
            String imagePath = "/images/no-image.png";
            if (item.getBienTheSanPham() != null && item.getBienTheSanPham().getSanPham() != null
                    && item.getBienTheSanPham().getSanPham().getHinhAnhSanPhams() != null
                    && !item.getBienTheSanPham().getSanPham().getHinhAnhSanPhams().isEmpty()) {
                imagePath = item.getBienTheSanPham().getSanPham().getHinhAnhSanPhams().stream()
                        .filter(HinhAnhSanPham::getLaAnhChinh)
                        .map(HinhAnhSanPham::getDuongDanAnh)
                        .findFirst()
                        .orElse(item.getBienTheSanPham().getSanPham().getHinhAnhSanPhams().get(0).getDuongDanAnh());
            }
            itemImages.put(item.getId(), imagePath);
        }

        model.addAttribute("order", order);
        model.addAttribute("items", orderItems);
        model.addAttribute("itemImages", itemImages);
        model.addAttribute("canRequestReturn", canRequestReturn);
        model.addAttribute("categories", danhMucService.getActive());
        orderService.getReturnRequest(id).ifPresent(r -> model.addAttribute("returnRequest", r));
        return "my-order-detail";
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
        Object sessionShippingFee = session.getAttribute("CURRENT_SHIPPING_FEE");
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
    public String requestReturn(@PathVariable Integer id, @RequestParam String reason, HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "Bạn cần đăng nhập để thực hiện thao tác này.";
        }

        try {
            orderService.createReturnRequest(id, loginUser.getId(), reason);
            return "SUCCESS";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/cancel/{id}")
    @ResponseBody
    public String cancelOrder(@PathVariable Integer id, @RequestParam String reason, HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "Bạn cần đăng nhập để thực hiện thao tác này.";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            // Bảo mật: chỉ cho hủy đơn hàng của chính mình
            if (!order.getTaiKhoan().getId().equals(loginUser.getId())) {
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
                                HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "Bạn cần đăng nhập để thực hiện thao tác này.";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            // Bảo mật: chỉ cho sửa đơn hàng của chính mình
            if (order.getTaiKhoan() == null || !order.getTaiKhoan().getId().equals(loginUser.getId())) {
                return "Bạn không có quyền chỉnh sửa đơn hàng này.";
            }

            orderService.updateOrderAddress(id, hoTen, soDienThoai, diaChi);
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
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            if (order.getTaiKhoan() == null || !order.getTaiKhoan().getId().equals(loginUser.getId())) {
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
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            orderService.createReturnRequest(id, loginUser.getId(), reason);
            redirectAttributes.addFlashAttribute("actionSuccess", "Yêu cầu trả hàng đã được gửi. Vui lòng chờ admin xử lý.");
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
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            if (order.getTaiKhoan() == null || !order.getTaiKhoan().getId().equals(loginUser.getId())) {
                redirectAttributes.addFlashAttribute("actionError", "Bạn không có quyền chỉnh sửa đơn hàng này.");
                return "redirect:/order/my-orders";
            }

            orderService.updateOrderAddress(id, hoTen, soDienThoai, diaChi);
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
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            if (order.getTaiKhoan() == null || !order.getTaiKhoan().getId().equals(loginUser.getId())) {
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
}
