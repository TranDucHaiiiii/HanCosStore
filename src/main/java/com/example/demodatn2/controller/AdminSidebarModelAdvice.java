package com.example.demodatn2.controller;

import com.example.demodatn2.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class AdminSidebarModelAdvice {

    private final OrderService orderService;

    @ModelAttribute("adminPendingOrderCount")
    public long adminPendingOrderCount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return 0L;
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) session.getAttribute("ROLES");
        if (roles == null || (!roles.contains("ADMIN") && !roles.contains("STAFF"))) {
            return 0L;
        }

        return orderService.getPendingConfirmationCount();
    }
}
