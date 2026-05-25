package com.example.demodatn2.controller;

import com.example.demodatn2.dto.CartItemDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.service.CartService;
import com.example.demodatn2.service.DanhMucService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final DanhMucService danhMucService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        session.getId(); // Force session creation
        List<CartItemDTO> items = cartService.getCartItems(session);
        Set<Integer> selectedIds = cartService.syncSelectedIds(session, items);
        BigDecimal total = cartService.getTotalAmount(
                items.stream().filter(item -> selectedIds.contains(item.getId())).toList()
        );
        clearAppliedVoucher(session);

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("selectedIds", selectedIds);
        model.addAttribute("categories", danhMucService.getActive());
        return "cart";
    }

    @GetMapping("/pending-add")
    public String handlePendingAdd(HttpSession session) {
        Integer bienTheId = (Integer) session.getAttribute("PENDING_BIEN_THE_ID");
        Integer soLuong = (Integer) session.getAttribute("PENDING_SO_LUONG");
        Boolean checkoutNow = (Boolean) session.getAttribute("PENDING_CHECKOUT_NOW");
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (bienTheId != null && soLuong != null && loginUser != null) {
            session.removeAttribute("PENDING_BIEN_THE_ID");
            session.removeAttribute("PENDING_SO_LUONG");
            session.removeAttribute("PENDING_CHECKOUT_NOW");
            try {
                Integer itemId = cartService.addToCart(bienTheId, soLuong, session);
                if (Boolean.TRUE.equals(checkoutNow) && itemId != null) {
                    cartService.setSelectedIds(session, Set.of(itemId));
                }
                int count = cartService.getItemCount(session);
                session.setAttribute("CART_COUNT", count);
                if (Boolean.TRUE.equals(checkoutNow)) {
                    return "redirect:/order/checkout";
                }
            } catch (Exception ignored) {
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam Integer bienTheId,
                                         @RequestParam Integer soLuong,
                                         @RequestParam(required = false, defaultValue = "false") boolean checkoutNow,
                                         HttpSession session) {
        session.getId(); // Force session creation
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            session.setAttribute("PENDING_BIEN_THE_ID", bienTheId);
            session.setAttribute("PENDING_SO_LUONG", soLuong);
            session.setAttribute("PENDING_CHECKOUT_NOW", checkoutNow);
            return Map.of(
                    "success", false,
                    "requireLogin", true,
                    "message", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng",
                    "loginUrl", "/login?next=/cart/pending-add"
            );
        }
        try {
            Integer itemId = cartService.addToCart(bienTheId, soLuong, session);
            if (checkoutNow && itemId != null) {
                cartService.setSelectedIds(session, Set.of(itemId));
            }
            int count = cartService.getItemCount(session);
            session.setAttribute("CART_COUNT", count);
            clearAppliedVoucher(session);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "Đã thêm vào giỏ hàng");
            response.put("count", count);
            if (checkoutNow) {
                response.put("checkoutUrl", "/order/checkout");
            }
            return response;
        } catch (Exception e) {
            return Map.of("success", false, "message", safeMessage(e));
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateQuantity(@RequestParam Integer itemId,
                                              @RequestParam Integer soLuong,
                                              HttpSession session) {
        try {
            cartService.updateQuantity(itemId, soLuong);
            List<CartItemDTO> items = cartService.getCartItems(session);
            Set<Integer> selectedIds = cartService.syncSelectedIds(session, items);
            BigDecimal total = getSelectedTotal(items, selectedIds);
            int count = cartService.getItemCount(session);
            session.setAttribute("CART_COUNT", count);
            clearAppliedVoucher(session);

            return Map.of(
                    "success", true,
                    "total", total,
                    "totalAfterDiscount", total,
                    "count", count,
                    "selectedCount", selectedIds.size()
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", safeMessage(e));
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeItem(@RequestParam Integer itemId, HttpSession session) {
        try {
            cartService.removeItem(itemId);
            cartService.removeSelectedId(session, itemId);
            List<CartItemDTO> items = cartService.getCartItems(session);
            Set<Integer> selectedIds = cartService.syncSelectedIds(session, items);
            BigDecimal total = getSelectedTotal(items, selectedIds);
            int count = cartService.getItemCount(session);
            session.setAttribute("CART_COUNT", count);
            clearAppliedVoucher(session);

            return Map.of(
                    "success", true,
                    "total", total,
                    "totalAfterDiscount", total,
                    "count", count,
                    "selectedCount", selectedIds.size()
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", safeMessage(e));
        }
    }

    @PostMapping("/remove-selected")
    @ResponseBody
    public Map<String, Object> removeSelectedItems(HttpSession session) {
        try {
            List<CartItemDTO> items = cartService.getCartItems(session);
            Set<Integer> selectedIds = cartService.syncSelectedIds(session, items);
            if (selectedIds.isEmpty()) {
                return Map.of("success", false, "message", "Vui lòng chọn sản phẩm cần xóa.");
            }

            for (Integer itemId : selectedIds) {
                cartService.removeItem(itemId);
            }
            cartService.setSelectedIds(session, Set.of());

            List<CartItemDTO> remainingItems = cartService.getCartItems(session);
            BigDecimal total = BigDecimal.ZERO;
            int count = cartService.getItemCount(session);
            session.setAttribute("CART_COUNT", count);
            clearAppliedVoucher(session);

            return Map.of(
                    "success", true,
                    "total", total,
                    "totalAfterDiscount", total,
                    "count", count,
                    "selectedCount", 0,
                    "removedIds", selectedIds,
                    "remainingCount", remainingItems.size()
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", safeMessage(e));
        }
    }

    @PostMapping("/select")
    @ResponseBody
    public Map<String, Object> selectItem(@RequestParam Integer itemId,
                                          @RequestParam boolean selected,
                                          HttpSession session) {
        List<CartItemDTO> items = cartService.getCartItems(session);
        Set<Integer> currentItemIds = items.stream()
                .map(CartItemDTO::getId)
                .collect(Collectors.toSet());
        Set<Integer> selectedIds = cartService.syncSelectedIds(session, items);

        if (!currentItemIds.contains(itemId)) {
            return Map.of("success", false, "message", "Sản phẩm không còn trong giỏ hàng.");
        }

        if (selected) {
            selectedIds.add(itemId);
        } else {
            selectedIds.remove(itemId);
        }
        cartService.setSelectedIds(session, selectedIds);
        clearAppliedVoucher(session);

        BigDecimal total = getSelectedTotal(items, selectedIds);
        return Map.of(
                "success", true,
                "total", total,
                "selectedCount", selectedIds.size(),
                "allSelected", !items.isEmpty() && selectedIds.containsAll(currentItemIds)
        );
    }

    @PostMapping("/select-all")
    @ResponseBody
    public Map<String, Object> selectAll(@RequestParam boolean selected, HttpSession session) {
        List<CartItemDTO> items = cartService.getCartItems(session);
        Set<Integer> selectedIds = selected
                ? items.stream().map(CartItemDTO::getId).collect(Collectors.toCollection(HashSet::new))
                : new HashSet<>();
        cartService.setSelectedIds(session, selectedIds);
        clearAppliedVoucher(session);

        BigDecimal total = getSelectedTotal(items, selectedIds);
        return Map.of(
                "success", true,
                "total", total,
                "selectedCount", selectedIds.size(),
                "allSelected", selected && !items.isEmpty()
        );
    }

    private void clearAppliedVoucher(HttpSession session) {
        session.removeAttribute("APPLIED_VOUCHER_CODE");
        session.removeAttribute("DISCOUNT_AMOUNT");
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null || e.getMessage().isBlank()
                ? "Không thể xử lý giỏ hàng"
                : e.getMessage();
    }

    private BigDecimal getSelectedTotal(List<CartItemDTO> items, Set<Integer> selectedIds) {
        return cartService.getTotalAmount(items.stream()
                .filter(item -> selectedIds.contains(item.getId()))
                .toList());
    }
}
