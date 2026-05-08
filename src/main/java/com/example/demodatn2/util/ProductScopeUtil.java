package com.example.demodatn2.util;

import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.entity.SanPham;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ProductScopeUtil {

    private static final Pattern EXCLUDED_CATEGORY_PATTERN =
            Pattern.compile("\\b(giay|dep|sneaker|boot|sandal)\\b");

    private ProductScopeUtil() {
    }

    public static boolean isAllowedCategory(DanhMuc danhMuc) {
        return !isExcludedCategory(danhMuc);
    }

    public static boolean isExcludedCategory(DanhMuc danhMuc) {
        if (danhMuc == null) {
            return false;
        }

        if (isExcludedCategoryName(danhMuc.getTen()) || isExcludedCategoryName(danhMuc.getMa())) {
            return true;
        }

        DanhMuc parent = danhMuc.getDanhMucCha();
        return parent != null
                && (isExcludedCategoryName(parent.getTen()) || isExcludedCategoryName(parent.getMa()));
    }

    public static boolean isAllowedProduct(SanPham sanPham) {
        return sanPham != null && !isExcludedCategory(sanPham.getDanhMuc());
    }

    public static boolean isExcludedCategoryName(String value) {
        String normalized = normalizeText(value);
        return !normalized.isEmpty() && EXCLUDED_CATEGORY_PATTERN.matcher(normalized).find();
    }

    public static String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();

        return normalized.replaceAll("\\s+", " ");
    }
}
