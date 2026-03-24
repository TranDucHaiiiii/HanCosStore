package com.example.demodatn2.util;

import java.util.regex.Pattern;
import java.text.Normalizer;

public final class AddressNormalizer {
    private static final Pattern PREFIX_PATTERN = Pattern.compile(
            "^(?i)(Thành\\s*phố|Tỉnh|TP\\.?|Quận|Huyện|Thị\\s*xã|Phường|Xã|Thị\\s*trấn)\\s*",
            Pattern.UNICODE_CASE
    );

    private AddressNormalizer() {
    }

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }

        String trimmed = input.trim();
        String withoutPrefix = PREFIX_PATTERN.matcher(trimmed).replaceFirst("");
        return withoutPrefix.replaceAll("\\s+", " ").trim();
    }

    public static String normalizeForCompare(String input) {
        String normalized = normalize(input);
        if (normalized.isEmpty()) {
            return "";
        }
        String noDiacritics = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noDiacritics.toLowerCase().replaceAll("\\s+", " ").trim();
    }
}
