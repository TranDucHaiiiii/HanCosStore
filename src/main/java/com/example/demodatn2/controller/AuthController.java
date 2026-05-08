package com.example.demodatn2.controller;

import com.example.demodatn2.dto.RegisterRequestDTO;
import com.example.demodatn2.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Hiển thị trang đăng nhập.
     * @param next Tham số URL lưu trang đích người dùng muốn truy cập trước khi đăng nhập.
     * @param model Đối tượng truyền dữ liệu sang giao diện Thymeleaf.
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String next, Model model) {
        model.addAttribute("next", next);
        return "login";
    }

    /**
     * Xử lý quá trình đăng nhập khi người dùng nhấn nút Login.
     * @param tenDangNhap Tên tài khoản lấy từ Form.
     * @param matKhau Mật khẩu lấy từ Form.
     * @param next Trang đích cần quay lại sau khi đăng nhập thành công.
     * @param session Đối tượng lưu trữ phiên làm việc của người dùng.
     * @param redirectAttributes Dùng để truyền thông báo (Flash Message) khi chuyển hướng.
     */
    @PostMapping("/login")
    public String login(@RequestParam String tenDangNhap,
                        @RequestParam String matKhau,
                        @RequestParam(required = false) String next,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        // Gọi service kiểm tra thông tin đăng nhập
        if (authService.login(tenDangNhap, matKhau, session)) {
            redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công!");
            // Nếu có trang đích (next) thì chuyển hướng đến đó, ngược lại về trang chủ
            if (next != null && !next.isEmpty()) {
                return "redirect:" + next;
            }
            return "redirect:/";
        } else {
            // Nếu đăng nhập thất bại, thêm thông báo lỗi và quay lại trang đăng nhập (giữ nguyên tham số next nếu có)
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
            return "redirect:/login" + (next != null ? "?next=" + next : "");
        }
    }
// trang dang ki
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        return "register";
    }
// trang quen mat khau
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }
// trang reset mat khau
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    /**
     * Hiển thị trang đặt lại mật khẩu mới thông qua mã token từ email.
     * @param token Mã xác thực được gửi qua link email.
     */
    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequestDTO registerRequest,
                           RedirectAttributes redirectAttributes) {
        try {
            authService.register(registerRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * Xử lý đăng xuất.
     * Hủy bỏ session hiện tại và chuyển hướng về trang đăng nhập.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        authService.logout(session);
        return "redirect:/login";
    }

    /**
     * Hiển thị trang thông báo lỗi 403 (Không có quyền truy cập).
     */
    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }
}
