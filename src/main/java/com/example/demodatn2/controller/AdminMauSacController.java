package com.example.demodatn2.controller;

import com.example.demodatn2.entity.MauSac;
import com.example.demodatn2.repository.MauSacRepository;
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
@RequestMapping("/admin/mau-sac")
@RequiredArgsConstructor
public class AdminMauSacController {

    private final MauSacRepository mauSacRepository;

    @GetMapping
    public String list(Model model) {
        List<MauSac> mauSacs = mauSacRepository.findAll().stream()
                .sorted(Comparator.comparing(MauSac::getTenMau, String.CASE_INSENSITIVE_ORDER))
                .toList();
        model.addAttribute("mauSacs", mauSacs);
        return "admin/mau-sac";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        MauSac mauSac = new MauSac();
        mauSac.setTrangThai("ACTIVE");
        model.addAttribute("mauSac", mauSac);
        return "admin/mau-sac-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        MauSac mauSac = mauSacRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại: " + id));
        model.addAttribute("mauSac", mauSac);
        return "admin/mau-sac-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute MauSac mauSac, RedirectAttributes redirectAttributes) {
        try {
            String tenMau = mauSac.getTenMau() != null ? mauSac.getTenMau().trim() : "";
            String maMau = mauSac.getMaMau() != null ? mauSac.getMaMau().trim() : null;
            String trangThai = mauSac.getTrangThai() != null ? mauSac.getTrangThai().trim().toUpperCase() : "";

            validateMauSac(mauSac.getId(), tenMau, maMau, trangThai);

            MauSac target = mauSac;
            if (mauSac.getId() != null) {
                target = mauSacRepository.findById(mauSac.getId())
                        .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại: " + mauSac.getId()));
            }

            target.setTenMau(tenMau);
            target.setMaMau(maMau == null || maMau.isBlank() ? null : maMau);
            target.setTrangThai(trangThai.isEmpty() ? "ACTIVE" : trangThai);

            mauSacRepository.save(target);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu màu sắc thành công!");
            return "redirect:/admin/mau-sac";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return mauSac.getId() == null
                    ? "redirect:/admin/mau-sac/add"
                    : "redirect:/admin/mau-sac/edit/" + mauSac.getId();
        }
    }

    private void validateMauSac(Integer id, String tenMau, String maMau, String trangThai) {
        if (tenMau.isEmpty()) {
            throw new RuntimeException("Tên màu không được để trống.");
        }
        if (tenMau.length() < 2 || tenMau.length() > 50) {
            throw new RuntimeException("Tên màu phải dài từ 2 đến 50 ký tự.");
        }
        if (maMau != null && !maMau.isBlank() && maMau.length() > 20) {
            throw new RuntimeException("Mã màu không được vượt quá 20 ký tự.");
        }
        if (!trangThai.isEmpty() && !"ACTIVE".equals(trangThai) && !"INACTIVE".equals(trangThai)) {
            throw new RuntimeException("Trạng thái màu không hợp lệ.");
        }

        mauSacRepository.findByTenMauIgnoreCase(tenMau)
                .filter(existing -> !Objects.equals(existing.getId(), id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Tên màu đã tồn tại.");
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            MauSac mauSac = mauSacRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại: " + id));
            mauSac.setTrangThai("INACTIVE");
            mauSacRepository.save(mauSac);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
