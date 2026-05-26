package com.example.demodatn2.controller;

import com.example.demodatn2.dto.BienTheRequestDTO;
import com.example.demodatn2.dto.GenerateQuickVariantsRequest;
import com.example.demodatn2.dto.SanPhamRequestDTO;
import com.example.demodatn2.dto.SanPhamResponseDTO;
import com.example.demodatn2.repository.KichCoRepository;
import com.example.demodatn2.repository.MauSacRepository;
import com.example.demodatn2.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamRestController {

    private final SanPhamService sanPhamService;
    private final MauSacRepository mauSacRepository;
    private final KichCoRepository kichCoRepository;

    @GetMapping("/options/colors")
    public List<String> getColorOptions() {
        return mauSacRepository.findAll().stream()
                .filter(mau -> mau.getTrangThai() == null || "ACTIVE".equalsIgnoreCase(mau.getTrangThai()))
                .map(mau -> mau.getTenMau())
                .filter(ten -> ten != null && !ten.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @GetMapping("/options/color-details")
    public List<Map<String, String>> getColorDetails() {
        return mauSacRepository.findAll().stream()
                .filter(mau -> mau.getTrangThai() == null || "ACTIVE".equalsIgnoreCase(mau.getTrangThai()))
                .filter(mau -> mau.getTenMau() != null && !mau.getTenMau().isBlank())
                .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getTenMau(), b.getTenMau()))
                .map(mau -> {
                    Map<String, String> option = new LinkedHashMap<>();
                    option.put("tenMau", mau.getTenMau());
                    option.put("maMau", mau.getMaMau());
                    return option;
                })
                .toList();
    }

    @GetMapping("/options/sizes")
    public List<String> getSizeOptions(@RequestParam(required = false) String loai) {
        String normalizedType = normalizeSizeType(loai);
        return kichCoRepository.findAll().stream()
                .filter(size -> size.getTrangThai() == null || "ACTIVE".equalsIgnoreCase(size.getTrangThai()))
                .filter(size -> normalizedType == null || normalizedType.equals(normalizeSizeType(size.getLoai())))
                .map(size -> size.getTenKichCo())
                .filter(ten -> ten != null && !ten.isBlank())
                .sorted((a, b) -> compareSize(a, b))
                .toList();
    }

    @GetMapping("/options/sizes-by-type")
    public Map<String, List<String>> getSizeOptionsByType() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put("AO", getSizeOptions("AO"));
        result.put("QUAN", getSizeOptions("QUAN"));
        result.put("CHUNG", getSizeOptions("CHUNG"));
        return result;
    }

    private String normalizeSizeType(String loai) {
        if (loai == null || loai.isBlank()) {
            return null;
        }
        String normalized = loai.trim().toUpperCase();
        if ("AO".equals(normalized) || "QUAN".equals(normalized) || "CHUNG".equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private int compareSize(String a, String b) {
        Integer na = parseIntOrNull(a);
        Integer nb = parseIntOrNull(b);
        if (na != null && nb != null) {
            return na.compareTo(nb);
        }
        if (na != null) {
            return -1;
        }
        if (nb != null) {
            return 1;
        }
        return Integer.compare(sizeRank(a), sizeRank(b));
    }

    private Integer parseIntOrNull(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private int sizeRank(String value) {
        if (value == null) {
            return 999;
        }
        return switch (value.trim().toUpperCase()) {
            case "XS" -> 100;
            case "S" -> 101;
            case "M" -> 102;
            case "L" -> 103;
            case "XL" -> 104;
            case "XXL" -> 105;
            default -> 500;
        };
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSanPham(@PathVariable Integer id, @RequestBody SanPhamRequestDTO requestDTO) {
        try {
            requestDTO.setId(id);
            return ResponseEntity.ok(sanPhamService.updateSanPham(requestDTO));
        } catch (Exception e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", false);
            body.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSanPham(@PathVariable Integer id) {
        sanPhamService.deleteSanPham(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate-variants")
    public ResponseEntity<List<BienTheRequestDTO>> generateQuickVariants(@RequestBody GenerateQuickVariantsRequest request) {
        List<BienTheRequestDTO> variants = sanPhamService.generateQuickVariants(request);
        return ResponseEntity.ok(variants);
    }

    @PostMapping("/validate-variants")
    public ResponseEntity<Map<String, Object>> validateVariants(@RequestBody List<BienTheRequestDTO> variants) {
        try {
            sanPhamService.validateVariantRequests(variants);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", false);
            body.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    @PostMapping("/validate-base")
    public ResponseEntity<Map<String, Object>> validateBase(@RequestBody Map<String, Object> body) {
        try {
            String maSanPham = body.get("maSanPham") != null ? body.get("maSanPham").toString() : null;
            String ten = body.get("ten") != null ? body.get("ten").toString() : null;
            Integer danhMucId = null;
            Object rawDanhMuc = body.get("danhMucId");
            if (rawDanhMuc != null && !rawDanhMuc.toString().isBlank()) {
                danhMucId = Integer.valueOf(rawDanhMuc.toString());
            }

            sanPhamService.validateBaseInfoForCreate(maSanPham, ten, danhMucId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }

    @PostMapping("/preview-sku")
    public ResponseEntity<Map<String, Object>> previewSku(@RequestBody Map<String, Object> body) {
        try {
            String maSanPham = body.get("maSanPham") != null ? body.get("maSanPham").toString() : null;
            String mauSac = body.get("mauSac") != null ? body.get("mauSac").toString() : null;
            String kichCo = body.get("kichCo") != null ? body.get("kichCo").toString() : null;

            String sku = sanPhamService.generateSkuPreview(maSanPham, mauSac, kichCo);
            return ResponseEntity.ok(Map.of("success", true, "sku", sku));
        } catch (Exception e) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }

    @PostMapping("/generate-code")
    public ResponseEntity<Map<String, Object>> generateProductCode(@RequestBody(required = false) Map<String, Object> body) {
        try {
            String ten = body != null && body.get("ten") != null ? body.get("ten").toString() : "";
            String code = sanPhamService.generateProductCode(ten);
            return ResponseEntity.ok(Map.of("success", true, "code", code));
        } catch (Exception e) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }
}
