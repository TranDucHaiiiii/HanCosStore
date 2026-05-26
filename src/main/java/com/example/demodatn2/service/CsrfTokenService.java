package com.example.demodatn2.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CsrfTokenService {

    public static final String SESSION_ATTRIBUTE = "CSRF_TOKEN";
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    // Tạo token an toàn cho URL theo session và tái sử dụng đến khi bị thay đổi.
    public String getOrCreateToken(HttpSession session) {
        Object existingToken = session.getAttribute(SESSION_ATTRIBUTE);
        if (existingToken instanceof String token && !token.isBlank()) {
            return token;
        }

        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_ATTRIBUTE, token);
        return token;
    }

    // Kiểm tra token gửi lên có khớp với token đang lưu trong session.
    public boolean isValidToken(HttpSession session, String token) {
        if (session == null || token == null || token.isBlank()) {
            return false;
        }
        Object expectedToken = session.getAttribute(SESSION_ATTRIBUTE);
        return expectedToken instanceof String expected && expected.equals(token);
    }
}
