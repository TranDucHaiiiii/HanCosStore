package com.example.demodatn2.controller;

import com.example.demodatn2.service.CsrfTokenService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class CsrfTokenModelAdvice {

    private final CsrfTokenService csrfTokenService;

    @ModelAttribute("csrfToken")
    public String csrfToken(HttpSession session) {
        return csrfTokenService.getOrCreateToken(session);
    }
}
