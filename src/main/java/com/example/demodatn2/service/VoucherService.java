package com.example.demodatn2.service;

import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.repository.MaGiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
// Service quản lý voucher: chuẩn hóa dữ liệu, kiểm tra hợp lệ và tính tiền giảm.
public class VoucherService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final MaGiamGiaRepository voucherRepository;

    // Lấy toàn bộ voucher và cập nhật trạng thái hết hạn.
    @Transactional
    public List<MaGiamGia> getAll() {
        deactivateExpiredVouchers();
        return voucherRepository.findAll();
    }

    // Tìm kiếm voucher theo bộ lọc, trả về danh sách.
    @Transactional
    public List<MaGiamGia> search(String keyword, String status, String type, String validity) {
        deactivateExpiredVouchers();
        Instant now = Instant.now();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toUpperCase();
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        String normalizedType = type == null ? "" : type.trim().toUpperCase();
        String normalizedValidity = validity == null ? "" : validity.trim().toUpperCase();

        return voucherRepository.findAll().stream()
                .filter(v -> normalizedKeyword.isEmpty()
                        || (v.getMa() != null && v.getMa().toUpperCase().contains(normalizedKeyword)))
                .filter(v -> normalizedStatus.isEmpty()
                        || (v.getTrangThai() != null && v.getTrangThai().equalsIgnoreCase(normalizedStatus)))
                .filter(v -> normalizedType.isEmpty()
                        || (v.getLoai() != null && v.getLoai().equalsIgnoreCase(normalizedType)))
                .filter(v -> matchesValidity(v, normalizedValidity, now))
                .sorted(Comparator
                        .comparing((MaGiamGia v) -> v.getId() == null ? 0 : v.getId()).reversed()
                        .thenComparing(v -> v.getMa() == null ? "" : v.getMa()))
                .toList();
    }

    // Tìm kiếm voucher theo bộ lọc, trả về phân trang.
    @Transactional
    public Page<MaGiamGia> search(String keyword, String status, String type, String validity, int page, int size) {
        deactivateExpiredVouchers();
        Instant now = Instant.now();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toUpperCase();
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        String normalizedType = type == null ? "" : type.trim().toUpperCase();
        String normalizedValidity = validity == null ? "" : validity.trim().toUpperCase();

        List<MaGiamGia> filtered = voucherRepository.findAll().stream()
                .filter(v -> normalizedKeyword.isEmpty()
                        || (v.getMa() != null && v.getMa().toUpperCase().contains(normalizedKeyword)))
                .filter(v -> normalizedStatus.isEmpty()
                        || (v.getTrangThai() != null && v.getTrangThai().equalsIgnoreCase(normalizedStatus)))
                .filter(v -> normalizedType.isEmpty()
                        || (v.getLoai() != null && v.getLoai().equalsIgnoreCase(normalizedType)))
                .filter(v -> matchesValidity(v, normalizedValidity, now))
                .sorted(Comparator
                        .comparing((MaGiamGia v) -> v.getId() == null ? 0 : v.getId()).reversed()
                        .thenComparing(v -> v.getMa() == null ? "" : v.getMa()))
                .toList();

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        // Cắt danh sách theo trang hiện tại.
        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    // Lấy voucher theo id.
    @Transactional
    public Optional<MaGiamGia> getById(Integer id) {
        deactivateExpiredVouchers();
        return voucherRepository.findById(id);
    }

    // Tạo/cập nhật voucher sau khi chuẩn hóa và kiểm tra dữ liệu.
    @Transactional
    public MaGiamGia save(MaGiamGia voucher) {
        normalizeVoucher(voucher);

        if (voucher.getId() != null) {
            MaGiamGia existing = voucherRepository.findById(voucher.getId())
                    .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + voucher.getId()));
            voucher.setSoLuongDaDung(existing.getSoLuongDaDung() == null ? 0 : existing.getSoLuongDaDung());
        } else if (voucher.getSoLuongDaDung() == null) {
            voucher.setSoLuongDaDung(0);
        }

        validateVoucherData(voucher);
        return voucherRepository.save(voucher);
    }

    // Vô hiệu hóa voucher bằng cách chuyển trạng thái.
    @Transactional
    public void delete(Integer id) {
        MaGiamGia voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + id));
        voucher.setTrangThai("INACTIVE");
        voucherRepository.save(voucher);
    }

    // Lấy voucher còn hiệu lực cho người dùng.
    @Transactional
    public List<MaGiamGia> getAvailableVouchers() {
        deactivateExpiredVouchers();
        return voucherRepository.findAvailableVouchers();
    }

    // Lấy danh sách voucher đủ điều kiện theo giá trị đơn hàng.
    @Transactional
    public List<MaGiamGia> getEligibleVouchers(BigDecimal orderAmount) {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        deactivateExpiredVouchers();
        return voucherRepository.findAvailableVouchers().stream()
                .filter(v -> v.getDonToiThieu() == null || orderAmount.compareTo(v.getDonToiThieu()) >= 0)
                .filter(v -> !"FIXED".equals(v.getLoai())
                        || v.getGiaTri() == null
                        || v.getGiaTri().compareTo(orderAmount) <= 0)
                .sorted(Comparator
                        .comparing((MaGiamGia v) -> calculateDiscount(v, orderAmount)).reversed()
                        .thenComparing(v -> v.getDonToiThieu() == null ? BigDecimal.ZERO : v.getDonToiThieu())
                        .thenComparing(v -> v.getMa() == null ? "" : v.getMa()))
                .toList();
    }

    // Kiểm tra voucher hợp lệ với đơn hàng.
    @Transactional
    public Optional<MaGiamGia> validateVoucher(String code, BigDecimal orderAmount) {
        if (code == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        String normalizedCode = code.trim().toUpperCase();
        if (normalizedCode.isEmpty()) {
            return Optional.empty();
        }

        deactivateExpiredVouchers();
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

    // Tính giá trị giảm áp dụng cho đơn hàng.
    public BigDecimal calculateDiscount(MaGiamGia voucher, BigDecimal orderAmount) {
        if (voucher == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;

        // Tính giảm theo % và chặn trần tối đa nếu có.
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
        // FIXED: lấy trực tiếp giá trị giảm.
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

    // Chuẩn hóa dữ liệu đầu vào của voucher.
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

    // Kiểm tra dữ liệu voucher hợp lệ trước khi lưu.
    private void validateVoucherData(MaGiamGia voucher) {
        // 1. Mã voucher
        if (voucher.getMa() == null || voucher.getMa().isEmpty()) {
            throw new IllegalArgumentException("Mã voucher không được để trống.");
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

        if ("FIXED".equals(voucher.getLoai())) {
            BigDecimal minOrderRequired = voucher.getGiaTri().multiply(new BigDecimal("5"));
            if (voucher.getDonToiThieu().compareTo(minOrderRequired) < 0) {
                throw new IllegalArgumentException(
                        "Đơn tối thiểu phải ít nhất gấp 5 lần giá trị giảm. " +
                                "Với voucher giảm "
                                + voucher.getGiaTri().setScale(0, java.math.RoundingMode.DOWN).toPlainString() +
                                "₫, đơn tối thiểu phải từ " +
                                minOrderRequired.setScale(0, java.math.RoundingMode.DOWN).toPlainString() + "₫.");
            }
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

        if ("ACTIVE".equals(voucher.getTrangThai()) && voucher.getKetThucLuc().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Voucher đã quá hạn. Vui lòng gia hạn thời gian kết thúc trước khi kích hoạt lại.");
        }
    }

    // Đưa voucher hết hạn về trạng thái INACTIVE.
    private int deactivateExpiredVouchers() {
        return voucherRepository.deactivateExpiredActiveVouchers();
    }

    // Kiểm tra điều kiện hiệu lực (UPCOMING/VALID/EXPIRED).
    private boolean matchesValidity(MaGiamGia voucher, String validity, Instant now) {
        if (validity == null || validity.isBlank()) {
            return true;
        }

        Instant startsAt = voucher.getBatDauLuc();
        Instant endsAt = voucher.getKetThucLuc();
        return switch (validity) {
            case "UPCOMING" -> startsAt != null && startsAt.isAfter(now);
            case "VALID" -> (startsAt == null || !startsAt.isAfter(now))
                    && (endsAt == null || !endsAt.isBefore(now));
            case "EXPIRED" -> endsAt != null && endsAt.isBefore(now);
            default -> true;
        };
    }
}
