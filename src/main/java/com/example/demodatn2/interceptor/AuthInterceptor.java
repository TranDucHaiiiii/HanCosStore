package com.example.demodatn2.interceptor;

import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.repository.TaiKhoanRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TaiKhoanRepository taiKhoanRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);
        Object user = session != null ? session.getAttribute("LOGIN_USER") : null;
        List<String> roles = session != null ? (List<String>) session.getAttribute("ROLES") : null;

        if (isLoggedInAccountLocked(user)) {
            session.invalidate();
            if (isAjaxOrApiRequest(request, uri)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"Tài khoản đã bị khóa.\"}");
                return false;
            }
            response.sendRedirect("/login?locked=1");
            return false;
        }

        // Các request public: /login, /register, /logout, /403, static resources, trang chủ /
        if (uri.equals("/login") || uri.equals("/register") || uri.equals("/logout") || uri.equals("/forgot-password") || uri.equals("/reset-password")
            || uri.equals("/") || uri.equals("/index") || uri.equals("/403") || uri.equals("/error") || uri.equals("/chinh-sach-doi-tra")
                || uri.equals("/api/chatbot")
                || uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") || uri.startsWith("/data/")
                || (uri.startsWith("/api/danh-muc/") && request.getMethod().equals("GET")) // Chỉ cho GET danh mục là public
                || uri.equals("/api/sepay/webhook")
                || uri.startsWith("/api/order/status/")
                || uri.startsWith("/products/")
                || uri.startsWith("/cart/") || uri.startsWith("/order/checkout") || uri.startsWith("/order/success")
                || uri.startsWith("/payment/")) {
            return true;
        }

        // Kiểm tra login
        if (user == null) {
            if (isAjaxOrApiRequest(request, uri)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"Session expired. Please login again.\"}");
                return false;
            }
            response.sendRedirect("/login?next=" + URLEncoder.encode(uri, StandardCharsets.UTF_8));
            return false;
        }

        // Phân quyền chi tiết
        if (uri.startsWith("/admin/") || uri.equals("/them-san-pham") || uri.equals("/san-pham") || uri.startsWith("/api/san-pham") 
                || uri.startsWith("/api/upload") || uri.startsWith("/api/danh-muc/")) {
            if (roles == null) {
                response.sendRedirect("/403");
                return false;
            }

            boolean isAdmin = roles.contains("ADMIN");
            boolean isStaff = roles.contains("STAFF");
            boolean isCustomer = roles.contains("CUSTOMER");

            // ADMIN có toàn quyền trong các path này
            if (isAdmin) {
                return true;
            }

            // STAFF bị hạn chế
            if (isStaff) {
                // Cho phép xem chi tiết sản phẩm
                if (uri.startsWith("/admin/san-pham/view/")) {
                    return true;
                }
                
                // 1. Không được quản lý tài khoản
                if (uri.startsWith("/admin/users")) {
                    response.sendRedirect("/403");
                    return false;
                }
                // 2. Không được quản lý danh mục (Controller + API)
                if (uri.startsWith("/admin/categories") || uri.startsWith("/api/danh-muc/")) {
                    response.sendRedirect("/403");
                    return false;
                }
                // 3. Không được thêm/sửa sản phẩm mới
                if (uri.equals("/them-san-pham") || uri.startsWith("/admin/san-pham/edit/")
                        || (uri.startsWith("/api/san-pham") && (request.getMethod().equals("POST") || request.getMethod().equals("PUT")))) {
                    response.sendRedirect("/403");
                    return false;
                }
                // 4. Không được xóa sản phẩm
                if (uri.startsWith("/api/san-pham") && request.getMethod().equals("DELETE")) {
                    response.sendRedirect("/403");
                    return false;
                }
                // 5. Không được xem báo cáo chi tiết (nếu có trang riêng)
                if (uri.equals("/admin/thong-ke")) {
                    response.sendRedirect("/403");
                    return false;
                }
                
                // 6. Không được quản lý Voucher
                if (uri.startsWith("/admin/vouchers")) {
                    response.sendRedirect("/403");
                    return false;
                }

                return true;
            }

            if (isCustomer) {
                response.sendRedirect("/403");
                return false;
            }

            response.sendRedirect("/403");
            return false;
        }

        // /account/** yêu cầu login (đã check user != null ở trên)
        if (uri.startsWith("/account/")) {
            return true;
        }

        return true;
    }

    private boolean isLoggedInAccountLocked(Object user) {
        if (!(user instanceof TaiKhoanDTO loginUser) || loginUser.getId() == null) {
            return false;
        }
        return taiKhoanRepository.findById(loginUser.getId())
                .map(taiKhoan -> "LOCKED".equals(taiKhoan.getTrangThai()))
                .orElse(true);
    }

    private boolean isAjaxOrApiRequest(HttpServletRequest request, String uri) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) || uri.startsWith("/api/");
    }
}
