package com.example.demodatn2.controller;

import com.example.demodatn2.dto.SanPhamRequestDTO;
import com.example.demodatn2.dto.SanPhamResponseDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.service.SanPhamService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;
    private final DanhMucService danhMucService;

    @GetMapping("/them-san-pham")
    public String showAddProductPage(Model model) {
        model.addAttribute("parentDanhMuc", danhMucService.getParents());
        model.addAttribute("parentDanhMucTree", danhMucService.getAllDTOs());
        return "addsanpham";
    }

    @PostMapping("/them-san-pham")
    public String createProductFromForm(@ModelAttribute SanPhamRequestDTO requestDTO,
                                        RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.createSanPham(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo sản phẩm thành công.");
            return "redirect:/them-san-pham";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/them-san-pham";
        }
    }

    /**
     * Hiển thị trang danh sách sản phẩm dành cho Admin
     */
    @GetMapping("/san-pham")
    public String showProductListPage(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Integer danhMucId,
                                     @RequestParam(required = false) String trangThai,
                                     @RequestParam(required = false, defaultValue = "newest") String sortBy,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     Model model) {
        int safePage = Math.max(page, 1);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);

        Page<SanPhamResponseDTO> productPage = sanPhamService.searchSanPhamPaged(
            keyword,
            danhMucId,
            trangThai,
            sortBy,
            safePage - 1,
            safeSize
        );
        int totalPages = Math.max(productPage.getTotalPages(), 1);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("categories", danhMucService.getActive());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedDanhMucId", danhMucId);
        model.addAttribute("selectedTrangThai", trangThai);
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("totalElements", productPage.getTotalElements());
        return "admin/products"; 
    }

    /**
     * Hiển thị trang chi tiết sản phẩm dành cho Admin/Nhân viên (Read-only)
     */
    @GetMapping("/admin/san-pham/view/{id}")
    public String viewProductPage(@PathVariable Integer id, Model model) {
        model.addAttribute("product", sanPhamService.getSanPhamById(id));
        return "admin/product-detail";
    }

    /**
     * Hiển thị trang chỉnh sửa sản phẩm
     */
    @GetMapping("/admin/san-pham/edit/{id}")
    public String showEditProductPage(@PathVariable Integer id, Model model) {
        model.addAttribute("product", sanPhamService.getSanPhamById(id));
        model.addAttribute("parentDanhMuc", danhMucService.getParents());
        return "editsanpham";
    }

    @PostMapping("/admin/san-pham/edit/{id}")
    public String updateProductFromForm(@PathVariable Integer id,
                                        @ModelAttribute SanPhamRequestDTO requestDTO,
                                        RedirectAttributes redirectAttributes) {
        try {
            requestDTO.setId(id);
            sanPhamService.updateSanPham(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/san-pham/edit/" + id;
        }
        return "redirect:/san-pham";
    }

    @PostMapping("/admin/san-pham/{id}/them-so-luong")
    public String addStock(@PathVariable Integer id,
                           @RequestParam Integer bienTheId,
                           @RequestParam Integer soLuongThem,
                           @RequestParam(required = false) String ghiChu,
                           @RequestParam(required = false) String redirectTo,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
            sanPhamService.addStockForVariant(
                    id,
                    bienTheId,
                    soLuongThem,
                    loginUser != null ? loginUser.getId() : null,
                    ghiChu
            );
            redirectAttributes.addFlashAttribute("successMessage", "Them so luong thanh cong.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        if ("dashboard".equalsIgnoreCase(redirectTo)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/admin/san-pham/nhap-kho/" + id + "?bienTheId=" + bienTheId;
    }

    @GetMapping("/admin/san-pham/nhap-kho/{id}")
    public String showNhapKhoPage(@PathVariable Integer id,
                                  @RequestParam(required = false) Integer bienTheId,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        try {
            Integer selectedBienTheId = bienTheId != null ? bienTheId : sanPhamService.getFirstBienTheIdBySanPham(id);
            model.addAttribute("bienThe", sanPhamService.getBienTheByIdAndSanPhamId(id, selectedBienTheId));
            model.addAttribute("lichSuNhapKho", sanPhamService.getNhapKhoHistoryByVariant(selectedBienTheId));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/san-pham/view/" + id;
        }
        return "admin/stock-in";
    }

    @PostMapping("/admin/san-pham/{id}/gallery/add")
    @ResponseBody
    public String addGalleryImage(@PathVariable Integer id, @RequestParam String duongDanAnh) {
        sanPhamService.addGalleryImage(id, duongDanAnh);
        return "OK";
    }
}
