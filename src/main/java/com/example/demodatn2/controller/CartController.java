package com.example.demodatn2.controller;

import com.example.demodatn2.dto.CartItemDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.service.CartService;
import com.example.demodatn2.service.DanhMucService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final DanhMucService danhMucService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        String sessionId = session.getId(); // Force session creation
        List<CartItemDTO> items = cartService.getCartItems(session);
        BigDecimal total = cartService.getTotalAmount(items);
        clearAppliedVoucher(session);
        
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("categories", danhMucService.getActive());
        return "cart";
    }

    @GetMapping("/pending-add")
    public String handlePendingAdd(HttpSession session) {
        Integer bienTheId = (Integer) session.getAttribute("PENDING_BIEN_THE_ID");
        Integer soLuong   = (Integer) session.getAttribute("PENDING_SO_LUONG");
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (bienTheId != null && soLuong != null && loginUser != null) {
            session.removeAttribute("PENDING_BIEN_THE_ID");
            session.removeAttribute("PENDING_SO_LUONG");
            try {
                cartService.addToCart(bienTheId, soLuong, session);
                int count = cartService.getItemCount(session);
                session.setAttribute("CART_COUNT", count);
            } catch (Exception ignored) {}
        }
        return "redirect:/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam Integer bienTheId, 
                                        @RequestParam Integer soLuong, 
                                        HttpSession session) {
        String sessionId = session.getId(); // Force session creation
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            session.setAttribute("PENDING_BIEN_THE_ID", bienTheId);
            session.setAttribute("PENDING_SO_LUONG", soLuong);
            return Map.of(
                    "success", false,
                    "requireLogin", true,
                    "message", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng",
                    "loginUrl", "/login?next=/cart/pending-add"
            );
        }
        try {
            cartService.addToCart(bienTheId, soLuong, session);
            int count = cartService.getItemCount(session);
            session.setAttribute("CART_COUNT", count);
            return Map.of("success", true, "message", "Đã thêm vào giỏ hàng", "count", count);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
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
            BigDecimal total = cartService.getTotalAmount(items);
            int count = cartService.getItemCount(session);
            session.setAttribute("CART_COUNT", count);
            clearAppliedVoucher(session);

            return Map.of(
                "success", true, 
                "total", total,
                "totalAfterDiscount", total,
                "count", count
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeItem(@RequestParam Integer itemId, HttpSession session) {
        try {
            cartService.removeItem(itemId);
            List<CartItemDTO> items = cartService.getCartItems(session);
            BigDecimal total = cartService.getTotalAmount(items);
            int count = cartService.getItemCount(session);
            session.setAttribute("CART_COUNT", count);
            clearAppliedVoucher(session);

            return Map.of(
                "success", true, 
                "total", total,
                "totalAfterDiscount", total,
                "count", count
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    private void clearAppliedVoucher(HttpSession session) {
        session.removeAttribute("APPLIED_VOUCHER_CODE");
        session.removeAttribute("DISCOUNT_AMOUNT");
    }
}
