package com.example.demodatn2.controller;

import com.example.demodatn2.dto.DanhMucDTO;
import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.service.DanhMucService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminDanhMucController {

    private final DanhMucService danhMucService;

    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "2") int size,
            @RequestParam(required = false) String keyword,
            Model model) {
        int safePage = Math.max(page, 1);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);

        Page<DanhMuc> categoryPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            categoryPage = danhMucService.searchParents(keyword.trim(), safePage - 1, safeSize);
        } else {
            categoryPage = danhMucService.findByDanhMucChaIsNull(safePage - 1, safeSize);
        }
        int totalPages = Math.max(categoryPage.getTotalPages(), 1);

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("totalElements", categoryPage.getTotalElements());
        model.addAttribute("keyword", keyword != null ? keyword : "");

        // Stats
        model.addAttribute("totalAll", danhMucService.countAll());
        model.addAttribute("parentCount", danhMucService.countParents());
        model.addAttribute("childCount", danhMucService.countChildren());
        model.addAttribute("activeCount", danhMucService.countActive());

        return "admin/categories";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new DanhMucDTO());
        model.addAttribute("parents", danhMucService.getParents());
        return "admin/category-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        DanhMuc dm = danhMucService.getById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại: " + id));
        
        DanhMucDTO dto = DanhMucDTO.builder()
                .id(dm.getId())
                .ma(dm.getMa())
                .ten(dm.getTen())
                .trangThai(dm.getTrangThai())
                .danhMucChaId(dm.getDanhMucCha() != null ? dm.getDanhMucCha().getId() : null)
                .build();
                
        model.addAttribute("category", dto);
        model.addAttribute("parents", danhMucService.getParents());
        return "admin/category-form";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute DanhMucDTO categoryDTO, RedirectAttributes redirectAttributes) {
        try {
            danhMucService.saveDTO(categoryDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return categoryDTO.getId() == null
                    ? "redirect:/admin/categories/add"
                    : "redirect:/admin/categories/edit/" + categoryDTO.getId();
        }
        return "redirect:/admin/categories";
    }
}
