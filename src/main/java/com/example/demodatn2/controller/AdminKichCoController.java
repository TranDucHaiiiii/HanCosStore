package com.example.demodatn2.controller;

import com.example.demodatn2.entity.KichCo;
import com.example.demodatn2.repository.KichCoRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/admin/kich-co")
@RequiredArgsConstructor
public class AdminKichCoController {

    private final KichCoRepository kichCoRepository;

    @GetMapping
    public String list(Model model) {
        List<KichCo> kichCos = kichCoRepository.findAll().stream()
                .sorted(this::compareSize)
                .toList();
        model.addAttribute("kichCos", kichCos);
        return "admin/kich-co";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        KichCo kichCo = new KichCo();
        kichCo.setTrangThai("ACTIVE");
        model.addAttribute("kichCo", kichCo);
        return "admin/kich-co-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        KichCo kichCo = kichCoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kích cỡ không tồn tại: " + id));
        model.addAttribute("kichCo", kichCo);
        return "admin/kich-co-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute KichCo kichCo, RedirectAttributes redirectAttributes) {
        try {
            String tenKichCo = kichCo.getTenKichCo() != null ? kichCo.getTenKichCo().trim() : "";
            String loai = kichCo.getLoai() != null ? kichCo.getLoai().trim().toUpperCase() : "";
            String trangThai = kichCo.getTrangThai() != null ? kichCo.getTrangThai().trim().toUpperCase() : "";

            validateKichCo(kichCo.getId(), tenKichCo, loai, trangThai);

            KichCo target = kichCo;
            if (kichCo.getId() != null) {
                target = kichCoRepository.findById(kichCo.getId())
                        .orElseThrow(() -> new RuntimeException("Kích cỡ không tồn tại: " + kichCo.getId()));
            }

            target.setTenKichCo(tenKichCo);
            target.setLoai(loai.isEmpty() ? "CHUNG" : loai);
            target.setTrangThai(trangThai.isEmpty() ? "ACTIVE" : trangThai);

            kichCoRepository.save(target);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu kích cỡ thành công!");
            return "redirect:/admin/kich-co";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return kichCo.getId() == null
                    ? "redirect:/admin/kich-co/add"
                    : "redirect:/admin/kich-co/edit/" + kichCo.getId();
        }
    }

    private void validateKichCo(Integer id, String tenKichCo, String loai, String trangThai) {
        if (tenKichCo.isEmpty()) {
            throw new RuntimeException("Tên kích cỡ không được để trống.");
        }
        if (tenKichCo.length() > 20) {
            throw new RuntimeException("Tên kích cỡ không được vượt quá 20 ký tự.");
        }
        if (!loai.isEmpty() && !"AO".equals(loai) && !"QUAN".equals(loai) && !"CHUNG".equals(loai)) {
            throw new RuntimeException("Loại kích cỡ không hợp lệ.");
        }
        if (!trangThai.isEmpty() && !"ACTIVE".equals(trangThai) && !"INACTIVE".equals(trangThai)) {
            throw new RuntimeException("Trạng thái kích cỡ không hợp lệ.");
        }

        kichCoRepository.findByTenKichCoIgnoreCase(tenKichCo)
                .filter(existing -> !Objects.equals(existing.getId(), id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Tên kích cỡ đã tồn tại.");
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            KichCo kichCo = kichCoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kích cỡ không tồn tại: " + id));
            kichCo.setTrangThai("INACTIVE");
            kichCoRepository.save(kichCo);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private int compareSize(KichCo left, KichCo right) {
        int typeCompare = Integer.compare(sizeTypeRank(left != null ? left.getLoai() : null),
                sizeTypeRank(right != null ? right.getLoai() : null));
        if (typeCompare != 0) {
            return typeCompare;
        }

        String a = left != null ? left.getTenKichCo() : null;
        String b = right != null ? right.getTenKichCo() : null;
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
        int rankCompare = Integer.compare(sizeRank(a), sizeRank(b));
        return rankCompare != 0 ? rankCompare : Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER).compare(a, b);
    }

    private int sizeTypeRank(String loai) {
        if (loai == null) {
            return 9;
        }
        return switch (loai.trim().toUpperCase()) {
            case "AO" -> 1;
            case "QUAN" -> 2;
            case "CHUNG" -> 3;
            default -> 9;
        };
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
}
