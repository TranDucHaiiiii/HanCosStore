package com.example.demodatn2.service;

import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.repository.MaGiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
// Service quản lý voucher: chuẩn hóa dữ liệu, kiểm tra hợp lệ và tính tiền giảm.
public class VoucherService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final MaGiamGiaRepository voucherRepository;

    public List<MaGiamGia> getAll() {
        return voucherRepository.findAll();
    }

    public Optional<MaGiamGia> getById(Integer id) {
        return voucherRepository.findById(id);
    }

    @Transactional
    public MaGiamGia save(MaGiamGia voucher) {
        normalizeVoucher(voucher);
        validateVoucherData(voucher);

        if (voucher.getSoLuongDaDung() == null) {
            voucher.setSoLuongDaDung(0);
        }
        return voucherRepository.save(voucher);
    }

    @Transactional
    public void delete(Integer id) {
        MaGiamGia voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + id));
        voucher.setTrangThai("INACTIVE");
        voucherRepository.save(voucher);
    }

    public List<MaGiamGia> getAvailableVouchers() {
        return voucherRepository.findAvailableVouchers();
    }

    public List<MaGiamGia> getEligibleVouchers(BigDecimal orderAmount) {
        return voucherRepository.findAvailableVouchers().stream()
                .filter(v -> v.getDonToiThieu() == null || orderAmount.compareTo(v.getDonToiThieu()) >= 0)
                .filter(v -> !"FIXED".equals(v.getLoai())
                        || v.getGiaTri() == null
                        || v.getGiaTri().compareTo(orderAmount) <= 0)
                .toList();
    }

    public Optional<MaGiamGia> validateVoucher(String code, BigDecimal orderAmount) {
        if (code == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        String normalizedCode = code.trim().toUpperCase();
        if (normalizedCode.isEmpty()) {
            return Optional.empty();
        }

        Optional<MaGiamGia> voucherOpt = voucherRepository.findValidVoucher(normalizedCode);
        if (voucherOpt.isEmpty()) {
            return Optional.empty();
        }

        MaGiamGia voucher = voucherOpt.get();

        if (voucher.getSoLuongToiDa() != null) {
            int daDung = voucher.getSoLuongDaDung() == null ? 0 : voucher.getSoLuongDaDung();
            if (daDung >= voucher.getSoLuongToiDa()) {
                return Optional.empty();
            }
        }

        if (voucher.getDonToiThieu() != null && orderAmount.compareTo(voucher.getDonToiThieu()) < 0) {
            return Optional.empty();
        }

        if (voucher.getGiaTri() == null || voucher.getGiaTri().compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        // Không cho giảm vượt quá tổng tiền đơn.
        if ("FIXED".equals(voucher.getLoai()) && voucher.getGiaTri().compareTo(orderAmount) > 0) {
            return Optional.empty();
        }

        return Optional.of(voucher);
    }

    public BigDecimal calculateDiscount(MaGiamGia voucher, BigDecimal orderAmount) {
        if (voucher == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;

        if ("PERCENT".equals(voucher.getLoai())) {
            BigDecimal percent = voucher.getGiaTri();
            if (percent == null || percent.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO;
            }
            if (percent.compareTo(ONE_HUNDRED) > 0) {
                percent = ONE_HUNDRED;
            }
            discount = orderAmount.multiply(percent.movePointLeft(2));
            if (voucher.getGiaTriToiDa() != null && discount.compareTo(voucher.getGiaTriToiDa()) > 0) {
                discount = voucher.getGiaTriToiDa();
            }
        } else if ("FIXED".equals(voucher.getLoai())) {
            discount = voucher.getGiaTri() != null ? voucher.getGiaTri() : BigDecimal.ZERO;
        }

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }

        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    private void normalizeVoucher(MaGiamGia voucher) {
        if (voucher.getMa() != null) {
            voucher.setMa(voucher.getMa().trim().toUpperCase());
        }
        if (voucher.getLoai() != null) {
            voucher.setLoai(voucher.getLoai().trim().toUpperCase());
        }
        if (voucher.getTrangThai() == null || voucher.getTrangThai().trim().isEmpty()) {
            voucher.setTrangThai("ACTIVE");
        } else {
            voucher.setTrangThai(voucher.getTrangThai().trim().toUpperCase());
        }
    }

    private void validateVoucherData(MaGiamGia voucher) {
        // 1. Mã voucher
        if (voucher.getMa() == null || voucher.getMa().isEmpty()) {
            throw new IllegalArgumentException("Mã voucher không được để trống.");
        }
        if (voucher.getMa().length() < 5 || voucher.getMa().length() > 20) {
            throw new IllegalArgumentException("Mã voucher phải có độ dài từ 5 đến 20 ký tự.");
        }
        if (!voucher.getMa().matches("^[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException("Mã voucher không được chứa khoảng trắng hoặc ký tự đặc biệt.");
        }
        // Kiểm tra trùng mã
        Optional<MaGiamGia> existing = voucherRepository.findByMa(voucher.getMa());
        if (existing.isPresent() && !existing.get().getId().equals(voucher.getId())) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại trong hệ thống.");
        }

        // Loại voucher
        if (voucher.getLoai() == null || (!"PERCENT".equals(voucher.getLoai()) && !"FIXED".equals(voucher.getLoai()))) {
            throw new IllegalArgumentException("Loại voucher phải là PERCENT hoặc FIXED.");
        }

        // 3. Giá trị giảm
        if (voucher.getGiaTri() == null || voucher.getGiaTri().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0.");
        }
        if ("PERCENT".equals(voucher.getLoai())) {
            if (voucher.getGiaTri().compareTo(ONE_HUNDRED) > 0) {
                throw new IllegalArgumentException("Voucher phần trăm không được vượt quá 100%.");
            }
            if (voucher.getGiaTriToiDa() == null || voucher.getGiaTriToiDa().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Voucher giảm theo phần trăm phải có giá trị giảm tối đa lớn hơn 0.");
            }
        } else if (voucher.getGiaTriToiDa() != null) {
            voucher.setGiaTriToiDa(null);
        }
        if (voucher.getGiaTriToiDa() != null && voucher.getGiaTriToiDa().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị giảm tối đa không hợp lệ.");
        }

        // 4. Đơn tối thiểu — bắt buộc
        if (voucher.getDonToiThieu() == null || voucher.getDonToiThieu().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Đơn tối thiểu phải lớn hơn 0.");
        }

        if ("FIXED".equals(voucher.getLoai()) && voucher.getGiaTri().compareTo(voucher.getDonToiThieu()) > 0) {
            throw new IllegalArgumentException("Voucher giảm tiền mặt không được lớn hơn đơn tối thiểu.");
        }

        // Giá trị giảm tối đa ≤ 30% đơn tối thiểu
        if (voucher.getGiaTriToiDa() != null && voucher.getDonToiThieu() != null) {
            BigDecimal maxAllowed = voucher.getDonToiThieu().multiply(new BigDecimal("0.3"));
            if (voucher.getGiaTriToiDa().compareTo(maxAllowed) > 0) {
                throw new IllegalArgumentException("Giá trị giảm tối đa không được vượt quá 30% đơn tối thiểu ("
                        + maxAllowed.setScale(0, java.math.RoundingMode.DOWN).toPlainString() + "₫).");
            }
        }

        // 5. Số lượng sử dụng — bắt buộc
        if (voucher.getSoLuongToiDa() == null || voucher.getSoLuongToiDa() <= 0) {
            throw new IllegalArgumentException("Số lượng mã phải lớn hơn 0.");
        }
        if (voucher.getSoLuongDaDung() != null && voucher.getSoLuongDaDung() < 0) {
            throw new IllegalArgumentException("Số lượng đã dùng không hợp lệ.");
        }
        if (voucher.getSoLuongToiDa() != null && voucher.getSoLuongDaDung() != null
                && voucher.getSoLuongDaDung() > voucher.getSoLuongToiDa()) {
            throw new IllegalArgumentException("Số lượng đã dùng không được lớn hơn số lượng tối đa.");
        }

        // 2. Thời gian áp dụng
        if (voucher.getBatDauLuc() == null || voucher.getKetThucLuc() == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống.");
        }
        if (!voucher.getKetThucLuc().isAfter(voucher.getBatDauLuc())) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
    }
}
