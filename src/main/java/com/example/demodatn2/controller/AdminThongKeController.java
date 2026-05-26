package com.example.demodatn2.controller;

import com.example.demodatn2.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/thong-ke")
@RequiredArgsConstructor
public class AdminThongKeController {

    private final ThongKeService thongKeService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("stats", thongKeService.getDoanhThuTongHop());

        LocalDate today = LocalDate.now();
        Instant tu = today.minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant den = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        Map<String, Object> tongHop = thongKeService.getDoanhThuTrongKhoang(tu, den);
        model.addAttribute("doanhThuThucTe", tongHop.get("doanhThuThucTe"));
        model.addAttribute("soDonTrongKy", tongHop.get("soDon"));
        model.addAttribute("thanhToanTheoLoai", tongHop.get("thanhToanTheoLoai"));
        model.addAttribute("donHangGanDay", thongKeService.getDonHangGanDay());

        return "admin/thong-ke";
    }

    @GetMapping("/data/doanh-thu-ngay")
    @ResponseBody
    public List<Map<String, Object>> getDoanhThuNgay(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        
        Instant tu = tuNgay != null ? tuNgay.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant den = denNgay != null ? denNgay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant() : null;
        
        return thongKeService.getDoanhThuTheoNgay(tu, den);
    }

    @GetMapping("/data/tong-hop")
    @ResponseBody
    public Map<String, Object> getTongHop(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        
        Instant tu = tuNgay != null ? tuNgay.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant den = denNgay != null ? denNgay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant() : null;
        
        return thongKeService.getDoanhThuTrongKhoang(tu, den);
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) throws IOException {
        
        Instant tu = tuNgay != null ? tuNgay.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant den = denNgay != null ? denNgay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant() : null;
        
        byte[] excelContent = thongKeService.exportDoanhThuToExcel(tu, den);
        
        String filename = "doanh-thu-" + (tuNgay != null ? tuNgay : "all") + "-to-" + (denNgay != null ? denNgay : "now") + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }
}
