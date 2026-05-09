package com.example.demodatn2.service;

import com.example.demodatn2.dto.RegisterRequestDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.entity.VaiTro;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.repository.VaiTroRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;

    @Transactional
    public void register(RegisterRequestDTO request) {
        String tenDangNhap = request.getTenDangNhap() != null ? request.getTenDangNhap().trim() : "";
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";

        if (tenDangNhap.isEmpty()) {
            throw new RuntimeException("Ten dang nhap khong duoc de trong!");
        }
        if (email.isEmpty()) {
            throw new RuntimeException("Email khong duoc de trong!");
        }
        if (request.getMatKhau() == null || request.getMatKhau().isEmpty()) {
            throw new RuntimeException("Mat khau khong duoc de trong!");
        }
        if (taiKhoanRepository.findByTenDangNhap(tenDangNhap).isPresent()) {
            throw new RuntimeException("Ten dang nhap da ton tai!");
        }
        if (taiKhoanRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new RuntimeException("Email da duoc su dung!");
        }

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(tenDangNhap);
        taiKhoan.setMatKhau(BCrypt.hashpw(request.getMatKhau(), BCrypt.gensalt()));
        taiKhoan.setHoTen(request.getHoTen() != null ? request.getHoTen().trim() : null);
        taiKhoan.setEmail(email);
        taiKhoan.setSoDienThoai(request.getSoDienThoai() != null ? request.getSoDienThoai().trim() : null);
        taiKhoan.setTrangThai("ACTIVE");

        VaiTro customerRole = vaiTroRepository.findByMa("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Vai tro CUSTOMER khong ton tai!"));
        taiKhoan.setVaiTros(new HashSet<>(Collections.singletonList(customerRole)));

        try {
            taiKhoanRepository.saveAndFlush(taiKhoan);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Email hoac ten dang nhap da ton tai!");
        }
    }

    public boolean login(String tenDangNhap, String matKhau, HttpSession session) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return false;
        }

        String loginValue = tenDangNhap.trim();
        Optional<TaiKhoan> optUser = taiKhoanRepository.findByTenDangNhap(loginValue);
        if (optUser.isEmpty()) {
            optUser = taiKhoanRepository.findByEmailIgnoreCase(loginValue);
        }

        if (optUser.isPresent()) {
            TaiKhoan user = optUser.get();

            if (!"ACTIVE".equals(user.getTrangThai())) {
                return false;
            }

            boolean isMatch;
            String hashed = user.getMatKhau();

            if (hashed != null && (hashed.startsWith("$2a$") || hashed.startsWith("$2b$"))) {
                try {
                    isMatch = BCrypt.checkpw(matKhau, hashed);
                } catch (Exception e) {
                    isMatch = false;
                }
            } else {
                isMatch = matKhau.equals(hashed);
            }

            if (isMatch) {
                TaiKhoanDTO dto = TaiKhoanDTO.builder()
                        .id(user.getId())
                        .tenDangNhap(user.getTenDangNhap())
                        .hoTen(user.getHoTen())
                        .email(user.getEmail())
                        .soDienThoai(user.getSoDienThoai())
                        .build();

                List<String> roles = user.getVaiTros().stream()
                        .map(VaiTro::getMa)
                        .filter(ma -> ma.equals("ADMIN") || ma.equals("STAFF") || ma.equals("CUSTOMER"))
                        .collect(Collectors.toList());

                session.setAttribute("LOGIN_USER", dto);
                session.setAttribute("ROLES", roles);
                return true;
            }
        }
        return false;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}
