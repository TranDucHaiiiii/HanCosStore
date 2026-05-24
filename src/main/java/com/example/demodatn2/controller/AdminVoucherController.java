package com.example.demodatn2.controller;

import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.service.VoucherService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String validity,
                       Model model) {
        model.addAttribute("vouchers", voucherService.search(q, status, type, validity));
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("validity", validity);
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
