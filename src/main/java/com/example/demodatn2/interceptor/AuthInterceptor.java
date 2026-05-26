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

        AccountStatusState accountStatusState = getLoggedInAccountStatusState(user);
        if (accountStatusState != AccountStatusState.ACTIVE) {
            session.invalidate();
            if (isAjaxOrApiRequest(request, uri)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(accountStatusState == AccountStatusState.LOCKED
                        ? "{\"message\": \"Tài khoản đã bị khóa.\"}"
                        : "{\"message\": \"Tài khoản đã ngừng hoạt động.\"}");
                return false;
            }
            response.sendRedirect(accountStatusState == AccountStatusState.LOCKED
                    ? "/login?locked=1"
                    : "/login?inactive=1");
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
            response.sendRedirect("/login?next=" + URLEncoder.encode(getFullRequestPath(request, uri), StandardCharsets.UTF_8));
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
                if (isStaffAllowed(request, uri)) {
                    return true;
                }
                response.sendRedirect("/403");
                return false;
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

    private AccountStatusState getLoggedInAccountStatusState(Object user) {
        if (!(user instanceof TaiKhoanDTO loginUser) || loginUser.getId() == null) {
            return AccountStatusState.ACTIVE;
        }
        return taiKhoanRepository.findById(loginUser.getId())
                .map(taiKhoan -> {
                    String status = taiKhoan.getTrangThai();
                    if ("ACTIVE".equals(status)) {
                        return AccountStatusState.ACTIVE;
                    }
                    if ("LOCKED".equals(status)) {
                        return AccountStatusState.LOCKED;
                    }
                    return AccountStatusState.INACTIVE;
                })
                .orElse(AccountStatusState.INACTIVE);
    }

    private boolean isAjaxOrApiRequest(HttpServletRequest request, String uri) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) || uri.startsWith("/api/");
    }

    private String getFullRequestPath(HttpServletRequest request, String uri) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return uri;
        }
        return uri + "?" + queryString;
    }

    private boolean isStaffAllowed(HttpServletRequest request, String uri) {
        String method = request.getMethod();

        if ("GET".equals(method) && uri.equals("/admin/dashboard")) {
            return true;
        }
        if (uri.startsWith("/admin/inventory")) {
            return true;
        }
        if ("GET".equals(method) && (uri.equals("/admin/pos") || uri.equals("/admin/ban-hang-tai-quay"))) {
            return true;
        }
        if (uri.startsWith("/admin/pos/api/")) {
            return true;
        }
        if (uri.startsWith("/admin/orders")) {
            return true;
        }
        if ("GET".equals(method) && uri.startsWith("/admin/returns")) {
            return true;
        }
        if ("GET".equals(method) && uri.equals("/san-pham")) {
            return true;
        }
        if ("GET".equals(method) && uri.startsWith("/admin/san-pham/view/")) {
            return true;
        }
        if ("GET".equals(method) && uri.startsWith("/admin/san-pham/nhap-kho/")) {
            return true;
        }
        return "POST".equals(method) && uri.matches("^/admin/san-pham/\\d+/them-so-luong$");
    }

    private enum AccountStatusState {
        ACTIVE,
        INACTIVE,
        LOCKED
    }
}
