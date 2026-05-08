package com.example.demodatn2.controller;

import com.example.demodatn2.dto.BienTheRequestDTO;
import com.example.demodatn2.dto.GenerateQuickVariantsRequest;
import com.example.demodatn2.dto.SanPhamRequestDTO;
import com.example.demodatn2.dto.SanPhamResponseDTO;
import com.example.demodatn2.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamRestController {

    private final SanPhamService sanPhamService;

    @PutMapping("/{id}")
    public ResponseEntity<SanPhamResponseDTO> updateSanPham(@PathVariable Integer id, @RequestBody SanPhamRequestDTO requestDTO) {
        requestDTO.setId(id);
        return ResponseEntity.ok(sanPhamService.updateSanPham(requestDTO));
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
}
