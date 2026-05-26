package com.example.demodatn2.controller;

import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.service.TaiKhoanService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminTaiKhoanController {

    private final TaiKhoanService taiKhoanService;

    @GetMapping
    public String listUsers(@RequestParam(required = false) String keyword, 
                           @RequestParam(required = false) String trangThai,
                           @RequestParam(required = false) String vaiTro,
                           Model model) {
        model.addAttribute("users", taiKhoanService.searchTaiKhoans(keyword, trangThai, vaiTro));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedTrangThai", trangThai);
        model.addAttribute("selectedVaiTro", vaiTro);
        model.addAttribute("allRoles", taiKhoanService.getStandardRoles());
        return "admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Integer id, Model model) {
        model.addAttribute("user", taiKhoanService.getTaiKhoanById(id));
        model.addAttribute("allRoles", taiKhoanService.getStandardRoles());
        return "admin/user-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Integer id,
                             @ModelAttribute TaiKhoanDTO userDTO,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.updateTaiKhoan(id, userDTO, getCurrentUserId(session));
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteUser(@PathVariable Integer id, HttpSession session) {
        try {
            taiKhoanService.deleteTaiKhoan(id, getCurrentUserId(session));
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private Integer getCurrentUserId(HttpSession session) {
        Object loginUser = session != null ? session.getAttribute("LOGIN_USER") : null;
        return loginUser instanceof TaiKhoanDTO dto ? dto.getId() : null;
    }
}
