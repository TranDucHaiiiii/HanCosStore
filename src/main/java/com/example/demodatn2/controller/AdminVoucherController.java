package com.example.demodatn2.controller;

import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String validity,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        int safePage = Math.max(page, 1);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Page<MaGiamGia> voucherPage = voucherService.search(q, status, type, validity, safePage - 1, safeSize);
        int totalPages = Math.max(voucherPage.getTotalPages(), 1);
        if (safePage > totalPages) {
            safePage = totalPages;
            voucherPage = voucherService.search(q, status, type, validity, safePage - 1, safeSize);
        }

        model.addAttribute("voucherPage", voucherPage);
        model.addAttribute("vouchers", voucherPage.getContent());
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("validity", validity);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", safeSize);
        long totalElements = voucherPage.getTotalElements();
        long fromItem = totalElements == 0 ? 0 : ((long) (safePage - 1) * safeSize + 1);
        long toItem = Math.min((long) safePage * safeSize, totalElements);

        model.addAttribute("totalElements", totalElements);
        model.addAttribute("fromItem", fromItem);
        model.addAttribute("toItem", toItem);
        return "admin/vouchers";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("voucher", new MaGiamGia());
        return "admin/voucher-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        MaGiamGia voucher = voucherService.getById(id)
                .orElseThrow(() -> new RuntimeException("Voucher khong ton tai: " + id));
        model.addAttribute("voucher", voucher);
        return "admin/voucher-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute MaGiamGia voucher,
                       @RequestParam("batDauStr") String batDauStr,
                       @RequestParam("ketThucStr") String ketThucStr,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            if (batDauStr == null || batDauStr.trim().isEmpty() || ketThucStr == null || ketThucStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui long nhap day du thoi gian bat dau va ket thuc.");
            }

            voucher.setBatDauLuc(LocalDateTime.parse(batDauStr).atZone(ZoneId.systemDefault()).toInstant());
            voucher.setKetThucLuc(LocalDateTime.parse(ketThucStr).atZone(ZoneId.systemDefault()).toInstant());

            voucherService.save(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Luu voucher thanh cong!");
            return "redirect:/admin/vouchers";
        } catch (DateTimeParseException e) {
            model.addAttribute("voucher", voucher);
            model.addAttribute("batDauStr", batDauStr);
            model.addAttribute("ketThucStr", ketThucStr);
            model.addAttribute("errorMessage", "Dinh dang ngay gio khong hop le. Vui long chon lai.");
            return "admin/voucher-form";
        } catch (Exception e) {
            model.addAttribute("voucher", voucher);
            model.addAttribute("batDauStr", batDauStr);
            model.addAttribute("ketThucStr", ketThucStr);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/voucher-form";
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String delete(@PathVariable Integer id) {
        try {
            voucherService.delete(id);
            return "SUCCESS";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
