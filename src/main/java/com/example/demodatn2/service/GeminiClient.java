package com.example.demodatn2.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generate(String systemInstruction, String userMessage) {
        String resolvedApiKey = apiKey != null ? apiKey.trim() : "";
        System.out.println("GEMINI_API_KEY loaded: " + maskApiKey(System.getenv("GEMINI_API_KEY")));
        System.out.println("gemini.api.key resolved: " + maskApiKey(resolvedApiKey));
        if (resolvedApiKey.isBlank() || resolvedApiKey.startsWith("${")) {
            return "Chatbot chua duoc cau hinh API Key.";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", systemInstruction != null ? systemInstruction : ""))
                ),
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", userMessage != null ? userMessage : ""))
                        )
                )
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", resolvedApiKey);

            Map<?, ?> response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), Map.class);
            return extractText(response);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return "Chatbot dang qua tai, ban vui long cho 1-2 phut roi thu lai nhe.";
            }
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "Model Gemini khong ton tai hoac chua duoc ho tro: " + model;
            }
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST
                    && e.getResponseBodyAsString() != null
                    && e.getResponseBodyAsString().contains("API Key not found")) {
                return "Gemini API key khong hop le hoac chua duoc Google kich hoat. Hay tao key moi trong Google AI Studio, set GEMINI_API_KEY, roi restart lai ung dung.";
            }
            return "Loi goi Gemini API: " + e.getStatusCode() + formatErrorDetail(e);
        } catch (Exception e) {
            return "Khong the ket noi toi chatbot AI.";
        }
    }

    private String extractText(Map<?, ?> response) {
        if (response == null || !response.containsKey("candidates")) {
            return "Chatbot khong nhan duoc phan hoi tu AI.";
        }

        List<?> candidates = (List<?>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return "Chatbot chua co cau tra loi phu hop.";
        }

        Object candidateObject = candidates.get(0);
        if (!(candidateObject instanceof Map<?, ?> candidate)) {
            return "Phan hoi Gemini khong dung dinh dang.";
        }

        Object contentObject = candidate.get("content");
        if (!(contentObject instanceof Map<?, ?> content)) {
            return "Phan hoi Gemini khong co noi dung.";
        }

        Object partsObject = content.get("parts");
        if (!(partsObject instanceof List<?> parts) || parts.isEmpty()) {
            return "Phan hoi Gemini khong co doan tra loi.";
        }

        Object firstPartObject = parts.get(0);
        if (!(firstPartObject instanceof Map<?, ?> firstPart)) {
            return "Phan hoi Gemini khong dung dinh dang.";
        }

        Object text = firstPart.get("text");
        return text != null ? text.toString() : "Phan hoi Gemini khong co text.";
    }

    private String formatErrorDetail(HttpClientErrorException e) {
        String detail = e.getResponseBodyAsString();
        if (detail == null || detail.isBlank()) {
            return "";
        }
        if (detail.length() > 240) {
            detail = detail.substring(0, 240) + "...";
        }
        return " - " + detail;
    }

    private String maskApiKey(String value) {
        if (value == null || value.isBlank()) {
            return "(empty)";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 10) {
            return "***";
        }
        return trimmed.substring(0, 6) + "..." + trimmed.substring(trimmed.length() - 4)
                + " (length=" + trimmed.length() + ")";
    }
}
