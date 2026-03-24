package com.example.demodatn2.controller;

import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.DiaChiGiaoHang;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.repository.DiaChiGiaoHangRepository;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.util.AddressNormalizer;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final TaiKhoanRepository taiKhoanRepository;
    private final DiaChiGiaoHangRepository diaChiGiaoHangRepository;
    private final DanhMucService danhMucService;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        TaiKhoan user = taiKhoanRepository.findById(loginUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        TaiKhoanDTO dto = TaiKhoanDTO.builder()
                .id(user.getId())
                .tenDangNhap(user.getTenDangNhap())
                .hoTen(user.getHoTen())
                .email(user.getEmail())
                .soDienThoai(user.getSoDienThoai())
                .ngayTao(user.getNgayTao())
                .build();

        model.addAttribute("user", dto);
        model.addAttribute("addresses", diaChiGiaoHangRepository.findByTaiKhoanIdOrderByLaMacDinhDescNgayTaoDesc(loginUser.getId()));
        model.addAttribute("categories", danhMucService.getActive());
        
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfilePage(HttpSession session, Model model) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        TaiKhoan user = taiKhoanRepository.findById(loginUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        model.addAttribute("user", user);
        model.addAttribute("addresses", diaChiGiaoHangRepository.findByTaiKhoanIdOrderByLaMacDinhDescNgayTaoDesc(loginUser.getId()));
        model.addAttribute("categories", danhMucService.getActive());
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@RequestParam String hoTen,
                                @RequestParam String email,
                                @RequestParam String soDienThoai,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        TaiKhoan user = taiKhoanRepository.findById(loginUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        user.setHoTen(hoTen);
        user.setEmail(email);
        user.setSoDienThoai(soDienThoai);

        taiKhoanRepository.save(user);

        // Cập nhật lại session
        loginUser.setHoTen(user.getHoTen());
        loginUser.setEmail(user.getEmail());
        loginUser.setSoDienThoai(user.getSoDienThoai());
        session.setAttribute("LOGIN_USER", loginUser);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/account/profile";
    }

    // === Địa chỉ giao hàng CRUD (AJAX) ===

    @PostMapping("/address/save")
    @ResponseBody
    public Map<String, Object> saveAddress(@RequestParam(required = false) Integer id,
                                           @RequestParam String hoTenNhan,
                                           @RequestParam String soDienThoaiNhan,
                                           @RequestParam String tinhThanh,
                                           @RequestParam String quanHuyen,
                                           @RequestParam String phuongXa,
                                           @RequestParam String diaChiChiTiet,
                                           @RequestParam(defaultValue = "false") Boolean laMacDinh,
                                           HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) return Map.of("success", false, "message", "Chưa đăng nhập");

        TaiKhoan tk = taiKhoanRepository.findById(loginUser.getId()).orElse(null);
        if (tk == null) return Map.of("success", false, "message", "Không tìm thấy tài khoản");

        DiaChiGiaoHang addr;
        if (id != null) {
            addr = diaChiGiaoHangRepository.findById(id).orElse(null);
            if (addr == null || !addr.getTaiKhoan().getId().equals(loginUser.getId()))
                return Map.of("success", false, "message", "Không tìm thấy địa chỉ");
        } else {
            addr = new DiaChiGiaoHang();
            addr.setTaiKhoan(tk);
            addr.setNgayTao(Instant.now());
        }

        addr.setHoTenNhan(hoTenNhan);
        addr.setSoDienThoaiNhan(soDienThoaiNhan);
        addr.setTinhThanh(AddressNormalizer.normalize(tinhThanh));
        addr.setQuanHuyen(AddressNormalizer.normalize(quanHuyen));
        addr.setPhuongXa(AddressNormalizer.normalize(phuongXa));
        addr.setDiaChiChiTiet(diaChiChiTiet);
        addr.setLaMacDinh(laMacDinh);

        if (Boolean.TRUE.equals(laMacDinh)) {
            List<DiaChiGiaoHang> all = diaChiGiaoHangRepository.findByTaiKhoanIdOrderByLaMacDinhDescNgayTaoDesc(loginUser.getId());
            all.forEach(a -> { if (Boolean.TRUE.equals(a.getLaMacDinh())) { a.setLaMacDinh(false); diaChiGiaoHangRepository.save(a); } });
        }

        diaChiGiaoHangRepository.save(addr);
        return Map.of("success", true);
    }

    @PostMapping("/address/delete")
    @ResponseBody
    public Map<String, Object> deleteAddress(@RequestParam Integer id, HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) return Map.of("success", false, "message", "Chưa đăng nhập");

        DiaChiGiaoHang addr = diaChiGiaoHangRepository.findById(id).orElse(null);
        if (addr == null || !addr.getTaiKhoan().getId().equals(loginUser.getId()))
            return Map.of("success", false, "message", "Không tìm thấy địa chỉ");

        diaChiGiaoHangRepository.delete(addr);
        return Map.of("success", true);
    }
}
