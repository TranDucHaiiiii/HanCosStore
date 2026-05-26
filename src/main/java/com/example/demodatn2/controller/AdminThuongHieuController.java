package com.example.demodatn2.controller;

import com.example.demodatn2.entity.ThuongHieu;
import com.example.demodatn2.repository.ThuongHieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/thuong-hieu")
@RequiredArgsConstructor
public class AdminThuongHieuController {

    private final ThuongHieuRepository thuongHieuRepository;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping
        public String list(@RequestParam(required = false) String q,
                   @RequestParam(defaultValue = "1") int page,
                   @RequestParam(defaultValue = "10") int size,
                       Model model) {
        int safePage = Math.max(page, 1);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        String keyword = q == null ? "" : q.trim().toLowerCase();

        List<ThuongHieu> allThuongHieus = thuongHieuRepository.findAll().stream()
            .filter(th -> keyword.isEmpty()
                || (th.getTen() != null && th.getTen().toLowerCase().contains(keyword))
                || (th.getMa() != null && th.getMa().toLowerCase().contains(keyword)))
                .sorted(Comparator.comparing(ThuongHieu::getTen, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int totalElements = allThuongHieus.size();
        int totalPages = Math.max((int) Math.ceil((double) totalElements / safeSize), 1);
        if (safePage > totalPages) {
            safePage = totalPages;
        }

        int start = Math.min((safePage - 1) * safeSize, totalElements);
        int end = Math.min(start + safeSize, totalElements);
        long fromItem = totalElements == 0 ? 0 : (long) start + 1;
        long toItem = totalElements == 0 ? 0 : end;

        model.addAttribute("thuongHieus", allThuongHieus.subList(start, end));
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("fromItem", fromItem);
        model.addAttribute("toItem", toItem);
        model.addAttribute("q", q);
        return "admin/thuong-hieu";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("thuongHieu", new ThuongHieu());
        return "admin/thuong-hieu-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại: " + id));
        model.addAttribute("thuongHieu", thuongHieu);
        return "admin/thuong-hieu-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ThuongHieu thuongHieu, RedirectAttributes redirectAttributes) {
        try {
            String ma = thuongHieu.getMa() != null ? thuongHieu.getMa().trim().toUpperCase() : "";
            String ten = thuongHieu.getTen() != null ? thuongHieu.getTen().trim() : "";
            String quocGia = thuongHieu.getQuocGia() != null ? thuongHieu.getQuocGia().trim() : null;

            validateThuongHieu(thuongHieu.getId(), ma, ten, quocGia);

            ThuongHieu target = thuongHieu;
            if (thuongHieu.getId() != null) {
                target = thuongHieuRepository.findById(thuongHieu.getId())
                        .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại: " + thuongHieu.getId()));
            } else {
                target.setNgayTao(Instant.now());
            }

            target.setMa(ma);
            target.setTen(ten);
            target.setQuocGia(quocGia);
            if (target.getNgayTao() == null) {
                target.setNgayTao(Instant.now());
            }

            thuongHieuRepository.save(target);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thương hiệu thành công!");
            return "redirect:/admin/thuong-hieu";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return thuongHieu.getId() == null
                    ? "redirect:/admin/thuong-hieu/add"
                    : "redirect:/admin/thuong-hieu/edit/" + thuongHieu.getId();
        }
    }

    private void validateThuongHieu(Integer id, String ma, String ten, String quocGia) {
        if (ma.isEmpty()) {
            throw new RuntimeException("Mã thương hiệu không được để trống.");
        }
        if (!ma.matches("^[A-Z0-9_-]{2,50}$")) {
            throw new RuntimeException("Mã thương hiệu chỉ gồm chữ, số, dấu gạch dưới hoặc gạch ngang, dài 2-50 ký tự.");
        }
        if (ten.isEmpty()) {
            throw new RuntimeException("Tên thương hiệu không được để trống.");
        }
        if (ten.length() < 2 || ten.length() > 150) {
            throw new RuntimeException("Tên thương hiệu phải dài từ 2 đến 150 ký tự.");
        }
        if (quocGia != null && quocGia.length() > 100) {
            throw new RuntimeException("Quốc gia không được vượt quá 100 ký tự.");
        }

        thuongHieuRepository.findByMaIgnoreCase(ma)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Mã thương hiệu đã tồn tại.");
                });
        thuongHieuRepository.findByTenIgnoreCase(ten)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Tên thương hiệu đã tồn tại.");
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            thuongHieuRepository.deleteById(id);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
