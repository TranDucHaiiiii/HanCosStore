package com.example.demodatn2.controller;

import com.example.demodatn2.dto.BienTheRequestDTO;
import com.example.demodatn2.dto.GenerateQuickVariantsRequest;
import com.example.demodatn2.dto.SanPhamRequestDTO;
import com.example.demodatn2.dto.SanPhamResponseDTO;
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
