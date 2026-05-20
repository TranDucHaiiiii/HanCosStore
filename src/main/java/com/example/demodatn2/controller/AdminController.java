package com.example.demodatn2.controller;

import com.example.demodatn2.dto.DanhMucDTO;
import com.example.demodatn2.dto.InventoryLogDTO;
import com.example.demodatn2.dto.InventoryVariantDTO;
import com.example.demodatn2.dto.PosCartItemDTO;
import com.example.demodatn2.dto.PosCartItemRequestDTO;
import com.example.demodatn2.dto.PosOrderRequestDTO;
import com.example.demodatn2.dto.ReturnWarehouseItemDTO;
import com.example.demodatn2.dto.SanPhamResponseDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.service.KhoHangHoanService;
import com.example.demodatn2.service.OrderService;
import com.example.demodatn2.service.PosCartService;
import com.example.demodatn2.service.SanPhamService;
import com.example.demodatn2.service.TaiKhoanService;
import com.example.demodatn2.service.ThongKeService;
import com.example.demodatn2.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final int DEFAULT_INVENTORY_PAGE_SIZE = 12;
    private static final int MAX_INVENTORY_PAGE_SIZE = 100;
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String LOGIN_USER = "LOGIN_USER";
    private static final String REDIRECT_ADMIN_INVENTORY = "redirect:/admin/inventory";
    private static final String CUSTOMER_MODE_GUEST = "guest";
    private static final String CUSTOMER_MODE_EXISTING = "existing";
    private static final String CUSTOMER_MODE_NEW = "new";
    private static final String PAYMENT_CASH = "cash";
    private static final String PAYMENT_TRANSFER = "transfer";

    private final ThongKeService thongKeService;
    private final SanPhamService sanPhamService;
    private final DanhMucService danhMucService;
    private final OrderService orderService;
    private final TaiKhoanService taiKhoanService;
    private final VoucherService voucherService;
    private final PosCartService posCartService;
    private final KhoHangHoanService khoHangHoanService;
    private final TaiKhoanRepository taiKhoanRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", thongKeService.getDoanhThuTongHop());
        return "admin/dashboard";
    }

    @GetMapping("/inventory")
    public String inventoryPage(@RequestParam(required = false) String q,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "12") int size,
                                Model model) {
        int safePage = Math.max(page, 1);
        int safeSize = size <= 0 ? DEFAULT_INVENTORY_PAGE_SIZE : Math.min(size, MAX_INVENTORY_PAGE_SIZE);

        Page<InventoryVariantDTO> variantPage = sanPhamService.getInventoryVariants(q, safePage - 1, safeSize);
        List<InventoryLogDTO> logs = sanPhamService.getRecentInventoryLogs();
        List<ReturnWarehouseItemDTO> returnWarehouseItems = khoHangHoanService.getReturnWarehouseItems();

        model.addAttribute("q", q);
        model.addAttribute("inventoryVariants", variantPage.getContent());
        model.addAttribute("returnWarehouseItems", returnWarehouseItems);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", Math.max(variantPage.getTotalPages(), 1));
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("totalElements", variantPage.getTotalElements());
        model.addAttribute("inventoryLogs", logs);
        return "admin/inventory";
    }

    @PostMapping("/inventory/adjust")
    public String adjustInventory(@RequestParam Integer sanPhamId,
                                  @RequestParam Integer bienTheId,
                                  @RequestParam Integer soLuong,
                                  @RequestParam String actionType,
                                  @RequestParam(required = false) String ghiChu,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            TaiKhoanDTO loginUser = getLoginUser(session);
            sanPhamService.adjustStockForVariant(
                    sanPhamId,
                    bienTheId,
                    soLuong,
                    actionType,
                    loginUser != null ? loginUser.getId() : null,
                    ghiChu
            );
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tồn kho thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_ADMIN_INVENTORY;
    }

    @PostMapping("/inventory/returns/{id}/import-main")
    public String importReturnItemToMainStock(@PathVariable Long id,
                                              @RequestParam(required = false) String ghiChu,
                                              HttpSession session,
                                              RedirectAttributes redirectAttributes) {
        try {
            TaiKhoanDTO loginUser = getLoginUser(session);
            khoHangHoanService.importToMainStock(id, loginUser != null ? loginUser.getId() : null, ghiChu);
            redirectAttributes.addFlashAttribute("successMessage", "Da nhap hang hoan vao kho chinh.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_ADMIN_INVENTORY;
    }

    @PostMapping("/inventory/returns/{id}/liquidate")
    public String liquidateReturnItem(@PathVariable Long id,
                                      @RequestParam(required = false) String ghiChu,
                                      RedirectAttributes redirectAttributes) {
        try {
            khoHangHoanService.markForLiquidation(id, ghiChu);
            redirectAttributes.addFlashAttribute("successMessage", "Da dua hang hoan vao thanh ly.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_ADMIN_INVENTORY;
    }

    @PostMapping("/inventory/returns/{id}/resell")
    public String resellReturnItem(@PathVariable Long id,
                                   @RequestParam(required = false) String ghiChu,
                                   RedirectAttributes redirectAttributes) {
        try {
            khoHangHoanService.markForResell(id, ghiChu);
            redirectAttributes.addFlashAttribute("successMessage", "Da danh dau hang hoan de ban lai.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_ADMIN_INVENTORY;
    }

    @GetMapping("/pos")
    public String pos() {
        return "admin/pos";
    }

    @GetMapping("/ban-hang-tai-quay")
    public String banHangTaiQuay(Model model, HttpSession session) {
        model.addAttribute("customers", taiKhoanService.searchTaiKhoans(null, ACTIVE_STATUS));
        model.addAttribute("products", sanPhamService.searchSanPham(null, null, ACTIVE_STATUS));
        model.addAttribute("categories", danhMucService.getAllDTOs());
        model.addAttribute("posInvoices", posCartService.listInvoices(session));
        model.addAttribute("posActiveInvoice", posCartService.getActiveInvoiceId(session));
        model.addAttribute("posSessionCart", posCartService.getCart(session));
        return "admin/banhangtaiquay";
    }

    @GetMapping("/pos/api/cart")
    @ResponseBody
    public Map<String, Object> posCart(HttpSession session) {
        return buildCartResponse(posCartService.getCart(session));
    }

    @GetMapping("/pos/api/cart/summary")
    @ResponseBody
    public Map<String, Object> posCartSummary(@RequestParam(required = false) String voucherCode,
                                              @RequestParam(required = false) BigDecimal cashGiven,
                                              HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("summary", buildPosSummary(posCartService.getCart(session), voucherCode, cashGiven));
        return response;
    }

    @PostMapping("/pos/api/cart/items")
    @ResponseBody
    public Map<String, Object> addPosCartItem(@RequestBody PosCartItemRequestDTO req, HttpSession session) {
        try {
            return buildCartResponse(posCartService.addItem(session, req.getVariantId(), req.getQty()));
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/pos/api/cart/items/{variantId}")
    @ResponseBody
    public Map<String, Object> updatePosCartItem(@PathVariable Integer variantId,
                                                 @RequestBody PosCartItemRequestDTO req,
                                                 HttpSession session) {
        try {
            return buildCartResponse(posCartService.updateQty(session, variantId, req.getQty()));
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/pos/api/cart/items/{variantId}")
    @ResponseBody
    public Map<String, Object> removePosCartItem(@PathVariable Integer variantId, HttpSession session) {
        return buildCartResponse(posCartService.removeItem(session, variantId));
    }

    @DeleteMapping("/pos/api/cart")
    @ResponseBody
    public Map<String, Object> clearPosCart(HttpSession session) {
        posCartService.clear(session);
        return buildCartResponse(List.of());
    }

    @GetMapping("/orders/pending-count")
    @ResponseBody
    public Map<String, Long> getPendingOrderCount() {
        return Map.of("count", orderService.getPendingConfirmationCount());
    }

    @GetMapping("/pos/api/products")
    @ResponseBody
    public List<SanPhamResponseDTO> posProducts() {
        return sanPhamService.searchSanPham(null, null, ACTIVE_STATUS);
    }

    @GetMapping("/pos/api/categories")
    @ResponseBody
    public List<DanhMucDTO> posCategories() {
        return danhMucService.getAllDTOs();
    }

    @GetMapping("/pos/api/customers")
    @ResponseBody
    public List<TaiKhoanDTO> searchCustomers(@RequestParam(required = false) String q) {
        return taiKhoanService.searchTaiKhoans(q, ACTIVE_STATUS);
    }

    @PostMapping("/pos/api/voucher/validate")
    @ResponseBody
    public Map<String, Object> validatePosVoucher(@RequestBody Map<String, Object> body, HttpSession session) {
        String code = (String) body.get("code");
        if (isBlank(code)) {
            return Map.of("success", false, "message", "Vui lòng nhập mã giảm giá");
        }
        Object amountObj = body.get("amount");
        BigDecimal amount = amountObj != null
                ? new BigDecimal(amountObj.toString())
                : calculatePosSubtotal(posCartService.getCart(session));

        Optional<MaGiamGia> voucherOpt = voucherService.validateVoucher(code, amount);
        if (voucherOpt.isPresent()) {
            MaGiamGia v = voucherOpt.get();
            BigDecimal discount = voucherService.calculateDiscount(v, amount);
            return Map.of("success", true, "discount", discount, "code", v.getMa(),
                    "label", v.getLoai() + " - " + v.getGiaTri());
        }
        return Map.of("success", false, "message", "Mã giảm giá không hợp lệ hoặc không đủ điều kiện");
    }

    @GetMapping("/pos/api/vouchers/eligible")
    @ResponseBody
    public Map<String, Object> getEligiblePosVouchers(@RequestParam(name = "amount", required = false) BigDecimal amount,
                                                      HttpSession session) {
        BigDecimal safeAmount = amount != null ? amount : calculatePosSubtotal(posCartService.getCart(session));
        List<Map<String, Object>> vouchers = voucherService.getEligibleVouchers(safeAmount).stream()
                .map(v -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("code", v.getMa());
                    item.put("label", v.getLoai() + " - " + v.getGiaTri());
                    item.put("type", v.getLoai());
                    item.put("value", v.getGiaTri());
                    item.put("minAmount", v.getDonToiThieu());
                    item.put("discount", voucherService.calculateDiscount(v, safeAmount));
                    item.put("usageLeft", v.getSoLuongToiDa() == null ? null : Math.max(0, v.getSoLuongToiDa() - (v.getSoLuongDaDung() == null ? 0 : v.getSoLuongDaDung())));
                    return item;
                })
                .toList();

        return Map.of("success", true, "vouchers", vouchers);
    }

    @PostMapping("/pos/api/order-code")
    @ResponseBody
    public Map<String, Object> generatePosOrderCode(@RequestBody PosOrderRequestDTO req, HttpSession session) {
        try {
            List<PosCartItemDTO> cart = posCartService.getCart(session);
            if (cart.isEmpty()) {
                return Map.of("success", false, "message", "Gio hang POS trong");
            }

            req.setItems(toPosOrderItems(cart));
            if (req.getOrderCode() == null || req.getOrderCode().trim().isEmpty()) {
                req.setOrderCode(posCartService.ensureTransferReference(session));
            }
            req.setPaymentMethod(PAYMENT_TRANSFER);
            TaiKhoanDTO staff = getLoginUser(session);
            DonHang donHang = orderService.createPendingPosTransferOrder(req, staff);
            return Map.of("success", true, "orderCode", donHang.getMaDonHang(), "total", donHang.getTongTien());
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/pos/api/invoices")
    @ResponseBody
    public Map<String, Object> getInvoices(HttpSession session) {
        return Map.of("success", true,
                "invoices", posCartService.listInvoices(session),
                "activeInvoiceId", posCartService.getActiveInvoiceId(session));
    }

    @PostMapping("/pos/api/invoices")
    @ResponseBody
    public Map<String, Object> createInvoice(HttpSession session) {
        try {
            String newId = posCartService.createInvoice(session);
            return Map.of("success", true,
                    "invoiceId", newId,
                    "invoices", posCartService.listInvoices(session),
                    "activeInvoiceId", newId,
                    "cart", posCartService.getCart(session));
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/pos/api/invoices/{invoiceId}/activate")
    @ResponseBody
    public Map<String, Object> activateInvoice(@PathVariable String invoiceId, HttpSession session) {
        try {
            posCartService.switchInvoice(session, invoiceId);
            return Map.of("success", true,
                    "invoices", posCartService.listInvoices(session),
                    "activeInvoiceId", invoiceId,
                    "cart", posCartService.getCart(session));
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/pos/api/invoices/{invoiceId}")
    @ResponseBody
    public Map<String, Object> removeInvoice(@PathVariable String invoiceId, HttpSession session) {
        String newActiveId = posCartService.deleteInvoice(session, invoiceId);
        return Map.of("success", true,
                "invoices", posCartService.listInvoices(session),
                "activeInvoiceId", newActiveId,
                "cart", posCartService.getCart(session));
    }

    @PostMapping("/pos/api/checkout")
    @ResponseBody
    public Map<String, Object> posCheckout(@RequestBody PosOrderRequestDTO req, HttpSession session) {
        try {
            List<PosCartItemDTO> cart = posCartService.getCart(session);
            if (cart.isEmpty()) {
                return Map.of("success", false, "message", "Giỏ hàng POS trống");
            }

            req.setItems(toPosOrderItems(cart));

            validateAndNormalizePosCustomer(req);
            validatePosPayment(req, cart);

            TaiKhoanDTO staff = getLoginUser(session);
            DonHang donHang = orderService.createPosOrder(req, staff);
            posCartService.clear(session);
            return Map.of("success", true, "orderId", donHang.getId(), "orderCode", donHang.getMaDonHang(),
                    "total", donHang.getTongTien(), "invoices", posCartService.listInvoices(session));
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    private TaiKhoanDTO getLoginUser(HttpSession session) {
        return (TaiKhoanDTO) session.getAttribute(LOGIN_USER);
    }

    private Map<String, Object> buildCartResponse(List<PosCartItemDTO> cart) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("cart", cart);
        response.put("summary", buildPosSummary(cart, null, null));
        return response;
    }

    private List<PosOrderRequestDTO.PosItemDTO> toPosOrderItems(List<PosCartItemDTO> cart) {
        return cart.stream()
                .map(this::toPosOrderItem)
                .toList();
    }

    private PosOrderRequestDTO.PosItemDTO toPosOrderItem(PosCartItemDTO cartItem) {
        PosOrderRequestDTO.PosItemDTO item = new PosOrderRequestDTO.PosItemDTO();
        item.setVariantId(cartItem.getVariantId());
        item.setQty(cartItem.getQty());
        item.setPrice(cartItem.getPrice());
        return item;
    }

    private void validateAndNormalizePosCustomer(PosOrderRequestDTO req) {
        String mode = req.getCustomerMode() != null ? req.getCustomerMode().trim().toLowerCase() : "guest";
        req.setCustomerMode(mode);

        if ("guest".equals(mode)) {
            req.setCustomerId(null);
            req.setCustomerName("Khách lẻ");
            req.setCustomerPhone("N/A");
            return;
        }

        if ("existing".equals(mode)) {
            if (req.getCustomerId() == null) {
                throw new RuntimeException("Vui lòng chọn khách hàng");
            }
            TaiKhoan customer = taiKhoanRepository.findById(req.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
            if (!"ACTIVE".equalsIgnoreCase(customer.getTrangThai())) {
                throw new RuntimeException("Khách hàng không hoạt động");
            }
            String customerName = firstNonBlank(customer.getHoTen(), customer.getTenDangNhap());
            req.setCustomerName(customerName);
            req.setCustomerPhone(firstNonBlank(customer.getSoDienThoai(), "N/A"));
            return;
        }

        if ("new".equals(mode)) {
            if (isBlank(req.getCustomerName())) {
                throw new RuntimeException("Vui lòng nhập tên khách hàng");
            }
            if (isBlank(req.getCustomerPhone())) {
                throw new RuntimeException("Vui lòng nhập số điện thoại khách hàng");
            }
            req.setCustomerId(null);
            req.setCustomerName(req.getCustomerName().trim());
            req.setCustomerPhone(req.getCustomerPhone().trim());
            return;
        }

        throw new RuntimeException("Chế độ khách hàng không hợp lệ");
    }

    private void validatePosPayment(PosOrderRequestDTO req, List<PosCartItemDTO> cart) {
        String paymentMethod = req.getPaymentMethod() != null ? req.getPaymentMethod().trim().toLowerCase() : "cash";
        if (!paymentMethod.equals("cash") && !paymentMethod.equals("transfer")) {
            throw new RuntimeException("Phương thức thanh toán không hợp lệ");
        }
        req.setPaymentMethod(paymentMethod);

        Map<String, Object> summary = buildPosSummary(cart, req.getVoucherCode(), req.getCashGiven());
        if (!isBlank(req.getVoucherCode()) && summary.get("voucherCode") == null) {
            throw new RuntimeException("Mã giảm giá không hợp lệ hoặc không đủ điều kiện");
        }
        BigDecimal total = (BigDecimal) summary.get("total");
        if ("cash".equals(paymentMethod)) {
            BigDecimal cashGiven = req.getCashGiven() != null ? req.getCashGiven() : BigDecimal.ZERO;
            if (cashGiven.compareTo(total) < 0) {
                throw new RuntimeException("Tiền khách đưa chưa đủ!");
            }
        } else if (!Boolean.TRUE.equals(req.getTransferConfirmed())) {
            throw new RuntimeException("Vui lòng xác nhận đã nhận được tiền chuyển khoản!");
        }
    }

    private BigDecimal calculatePosSubtotal(List<PosCartItemDTO> cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (cart == null) {
            return subtotal;
        }
        for (PosCartItemDTO item : cart) {
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            BigDecimal qty = BigDecimal.valueOf(item.getQty() != null ? item.getQty() : 0);
            subtotal = subtotal.add(price.multiply(qty));
        }
        return subtotal;
    }

    private Map<String, Object> buildPosSummary(List<PosCartItemDTO> cart, String voucherCode, BigDecimal cashGiven) {
        BigDecimal subtotal = calculatePosSubtotal(cart);
        BigDecimal discount = BigDecimal.ZERO;
        String appliedCode = null;

        if (voucherCode != null && !voucherCode.trim().isEmpty() && subtotal.compareTo(BigDecimal.ZERO) > 0) {
            Optional<MaGiamGia> voucherOpt = voucherService.validateVoucher(voucherCode.trim(), subtotal);
            if (voucherOpt.isPresent()) {
                MaGiamGia voucher = voucherOpt.get();
                discount = voucherService.calculateDiscount(voucher, subtotal);
                appliedCode = voucher.getMa();
            }
        }

        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal given = cashGiven != null ? cashGiven : BigDecimal.ZERO;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("subtotal", subtotal);
        summary.put("discount", discount);
        summary.put("total", total);
        summary.put("cashGiven", given);
        summary.put("change", given.subtract(total));
        summary.put("voucherCode", appliedCode);
        return summary;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String firstNonBlank(String first, String fallback) {
        return !isBlank(first) ? first.trim() : fallback;
    }
}
