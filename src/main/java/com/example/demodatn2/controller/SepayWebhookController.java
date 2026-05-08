package com.example.demodatn2.controller;

import com.example.demodatn2.dto.SepayWebhookRequest;
import com.example.demodatn2.service.SepayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sepay")
@RequiredArgsConstructor
@Slf4j
public class SepayWebhookController {

    @Value("${sepay.api.key}")
    private String apiKey;

    private final SepayService sepayService;

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(
            @RequestBody SepayWebhookRequest payload,
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        if (!isValidApiKey(auth)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        try {
            sepayService.handleWebhook(payload);
            return ResponseEntity.ok("OK");
        } catch (IllegalArgumentException ex) {
            log.warn("Sepay webhook rejected: {} - payload={}", ex.getMessage(), payload);
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            // Log full stacktrace for debugging and return a helpful message in dev
            log.error("Error processing SePay webhook: payload={}", payload, ex);
            String msg = ex.getMessage() != null ? ex.getMessage() : "Webhook xử lý thất bại";
            return ResponseEntity.internalServerError().body(msg);
        }
    }

    private boolean isValidApiKey(String auth) {
        if (auth == null || auth.trim().isEmpty()) {
            return false;
        }
        String expected = "Apikey " + apiKey;
        return expected.equals(auth.trim());
    }
}
