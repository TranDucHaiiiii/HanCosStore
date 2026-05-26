package com.example.demodatn2.service;

import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.entity.VaiTro;
import com.example.demodatn2.repository.DonHangRepository;
import com.example.demodatn2.repository.GioHangRepository;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// Service quản trị tài khoản: tìm kiếm, cập nhật role và xử lý xóa/khóa tài khoản.
public class TaiKhoanService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final DonHangRepository donHangRepository;
    private final GioHangRepository gioHangRepository;

    @Transactional(readOnly = true)
    public List<TaiKhoanDTO> getAllTaiKhoans() {
        return searchTaiKhoans(null, null, null);
    }

    @Transactional(readOnly = true)
    public List<TaiKhoanDTO> searchTaiKhoans(String keyword, String trangThai) {
        return searchTaiKhoans(keyword, trangThai, null);
    }

    @Transactional(readOnly = true)
    public List<TaiKhoanDTO> searchTaiKhoans(String keyword, String trangThai, String vaiTro) {
        String normalizedRole = normalizeRoleMa(vaiTro != null ? vaiTro.trim().toUpperCase() : null);
        if (normalizedRole != null && normalizedRole.isBlank()) {
            normalizedRole = null;
        }
        if (normalizedRole != null && !isStandardRole(normalizedRole)) {
            normalizedRole = null;
        }

        List<TaiKhoan> users = taiKhoanRepository.search(
                (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null,
                (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai.trim() : null,
                normalizedRole
        );
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaiKhoanDTO getTaiKhoanById(Integer id) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + id));
        return convertToDTO(taiKhoan);
    }

    @Transactional
    public void updateTaiKhoan(Integer id, TaiKhoanDTO dto) {
        updateTaiKhoan(id, dto, null);
    }

    @Transactional
    public void updateTaiKhoan(Integer id, TaiKhoanDTO dto, Integer currentUserId) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + id));

        boolean editingSelf = currentUserId != null && currentUserId.equals(id);
        if (editingSelf && dto.getTrangThai() != null && !"ACTIVE".equalsIgnoreCase(dto.getTrangThai())) {
            throw new RuntimeException("Admin không được tự khóa hoặc ngừng hoạt động tài khoản của mình.");
        }

        taiKhoan.setHoTen(dto.getHoTen());
        taiKhoan.setEmail(dto.getEmail());
        taiKhoan.setSoDienThoai(dto.getSoDienThoai());
        taiKhoan.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai().trim().toUpperCase() : "ACTIVE");

        if (dto.getVaiTroIds() == null || dto.getVaiTroIds().isEmpty()) {
            throw new RuntimeException("Mỗi tài khoản phải có đúng 1 vai trò.");
        }
        if (dto.getVaiTroIds().size() != 1) {
            throw new RuntimeException("Mỗi tài khoản chỉ được chọn 1 vai trò.");
        }

        Integer roleId = dto.getVaiTroIds().get(0);
        VaiTro role = vaiTroRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại."));
        String normalizedRole = normalizeRoleMa(role.getMa());
        if (!isStandardRole(normalizedRole)) {
            throw new RuntimeException("Không cho phép gán role ngoài danh sách chuẩn (ADMIN/STAFF/CUSTOMER).");
        }

        if (!role.getMa().equals(normalizedRole)) {
            role = vaiTroRepository.findByMa(normalizedRole)
                    .orElseThrow(() -> new RuntimeException("Vai trò chuẩn không tồn tại: " + normalizedRole));
        }

        boolean currentlyAdmin = taiKhoan.getVaiTros().stream()
                .map(VaiTro::getMa)
                .map(this::normalizeRoleMa)
                .anyMatch("ADMIN"::equals);
        if (editingSelf && currentlyAdmin && !"ADMIN".equals(normalizedRole)) {
            throw new RuntimeException("Admin không được tự hạ quyền chính mình. Cần admin khác thực hiện xác nhận.");
        }

        taiKhoan.setVaiTros(new HashSet<>(Set.of(role)));
        
        taiKhoanRepository.save(taiKhoan);
    }

    @Transactional
    public void deleteTaiKhoan(Integer id) {
        deleteTaiKhoan(id, null);
    }

    @Transactional
    public void deleteTaiKhoan(Integer id, Integer currentUserId) {
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new RuntimeException("Admin không được tự xóa tài khoản của mình.");
        }

        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + id));

        // Xóa giỏ hàng trước
        if (donHangRepository.existsByTaiKhoan_Id(id)) {
            taiKhoan.setTrangThai("INACTIVE");
            taiKhoanRepository.save(taiKhoan);
            return;
        }
        gioHangRepository.deleteByTaiKhoan_Id(id);

        // Sau đó mới xóa tài khoản
        taiKhoanRepository.delete(taiKhoan);
    }

    @Transactional(readOnly = true)
    public List<VaiTro> getStandardRoles() {
        return List.of(
                vaiTroRepository.findByMa("ADMIN").orElse(null),
                vaiTroRepository.findByMa("STAFF").orElse(null),
                vaiTroRepository.findByMa("CUSTOMER").orElse(null)
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private TaiKhoanDTO convertToDTO(TaiKhoan taiKhoan) {
        List<String> roleMas = taiKhoan.getVaiTros().stream()
                .map(VaiTro::getMa)
                .map(this::normalizeRoleMa)
                .filter(this::isStandardRole)
                .distinct()
                .collect(Collectors.toList());

        return TaiKhoanDTO.builder()
                .id(taiKhoan.getId())
                .tenDangNhap(taiKhoan.getTenDangNhap())
                .hoTen(taiKhoan.getHoTen())
                .email(taiKhoan.getEmail())
                .soDienThoai(taiKhoan.getSoDienThoai())
                .trangThai(taiKhoan.getTrangThai())
                .ngayTao(taiKhoan.getNgayTao())
                .vaiTroIds(taiKhoan.getVaiTros().stream()
                        .map(this::normalizeRoleId)
                        .distinct()
                        .collect(Collectors.toList()))
                .vaiTroMas(roleMas)
                .build();
    }

    private boolean isStandardRole(String ma) {
        return ma != null && (ma.equals("ADMIN") || ma.equals("STAFF") || ma.equals("CUSTOMER"));
    }

    private String normalizeRoleMa(String ma) {
        if (ma == null) {
            return null;
        }
        return switch (ma) {
            case "USER", "KHACH_HANG" -> "CUSTOMER";
            case "NHAN_VIEN" -> "STAFF";
            default -> ma;
        };
    }

    private Integer normalizeRoleId(VaiTro role) {
        if (role == null) {
            return null;
        }
        String normalizedMa = normalizeRoleMa(role.getMa());
        if (normalizedMa == null || normalizedMa.equals(role.getMa())) {
            return role.getId();
        }
        return vaiTroRepository.findByMa(normalizedMa)
                .map(VaiTro::getId)
                .orElse(role.getId());
    }
}
