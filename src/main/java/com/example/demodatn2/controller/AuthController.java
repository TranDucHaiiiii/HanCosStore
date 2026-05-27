package com.example.demodatn2.controller;

import com.example.demodatn2.dto.RegisterRequestDTO;
import com.example.demodatn2.service.AuthService;
import com.example.demodatn2.service.CsrfTokenService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final CsrfTokenService csrfTokenService;

    /**
     * Hiển thị trang đăng nhập.
     * @param next Tham số URL lưu trang đích người dùng muốn truy cập trước khi đăng nhập.
     * @param model Đối tượng truyền dữ liệu sang giao diện Thymeleaf.
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String next,
                            @RequestParam(required = false) String locked,
                            @RequestParam(required = false) String inactive,
                            @RequestParam(required = false) String mode,
                            Model model) {
        model.addAttribute("next", next);
        model.addAttribute("authMode", mode);
        if ("1".equals(locked)) {
            model.addAttribute("errorMessage", "Tài khoản đã bị khóa!");
        } else if ("1".equals(inactive)) {
            model.addAttribute("errorMessage", "Tài khoản đã ngừng hoạt động!");
        }
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
                        @RequestParam(name = "_csrf", required = false) String csrfToken,
                        HttpServletRequest request,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        if (!csrfTokenService.isValidToken(session, csrfToken)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên bảo mật không hợp lệ. Vui lòng thử lại.");
            return redirectToLogin(next, redirectAttributes);
        }

        // Gọi service kiểm tra thông tin đăng nhập
        try {
            if (authService.login(tenDangNhap, matKhau, session)) {
                Object loginUser = session.getAttribute("LOGIN_USER");
                Object roles = session.getAttribute("ROLES");
                request.changeSessionId();
                if (loginUser != null) {
                    session.setAttribute("LOGIN_USER", loginUser);
                }
                if (roles != null) {
                    session.setAttribute("ROLES", roles);
                }
                redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công!");
                // Nếu có trang đích (next) thì chuyển hướng đến đó, ngược lại về trang chủ
                if (isSafeLocalRedirect(next)) {
                    return "redirect:" + next;
                }
                return "redirect:/";
            }
        } catch (AuthService.AccountLockedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản đã bị khóa!");
            return redirectToLogin(next, redirectAttributes);
        } catch (AuthService.AccountInactiveException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản đã ngừng hoạt động!");
            return redirectToLogin(next, redirectAttributes);
        }

        // Nếu đăng nhập thất bại, thêm thông báo lỗi và quay lại trang đăng nhập.
        redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        return redirectToLogin(next, redirectAttributes);
    }
// trang dang ki
    @GetMapping("/register")
    public String registerPage() {
        return "redirect:/login?mode=register";
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
                           @RequestParam(name = "_csrf", required = false) String csrfToken,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!csrfTokenService.isValidToken(session, csrfToken)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên bảo mật không hợp lệ. Vui lòng thử lại.");
            return "redirect:/login?mode=register";
        }

        try {
            authService.register(registerRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/login?mode=register";
        }
    }

    /**
     * Xử lý đăng xuất.
     * Hủy bỏ session hiện tại và chuyển hướng về trang đăng nhập.
     */
    @PostMapping("/logout")
    public String logout(@RequestParam(name = "_csrf", required = false) String csrfToken,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!csrfTokenService.isValidToken(session, csrfToken)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên bảo mật không hợp lệ. Vui lòng thử lại.");
            return "redirect:/";
        }

        authService.logout(session);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String legacyLogout(HttpSession session) {
        return "redirect:/";
    }

    /**
     * Hiển thị trang thông báo lỗi 403 (Không có quyền truy cập).
     */
    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }

    private String redirectToLogin(String next, RedirectAttributes redirectAttributes) {
        if (isSafeLocalRedirect(next)) {
            redirectAttributes.addAttribute("next", next);
        }
        return "redirect:/login";
    }

    private boolean isSafeLocalRedirect(String next) {
        return next != null && !next.isBlank() && next.startsWith("/") && !next.startsWith("//");
    }
}
