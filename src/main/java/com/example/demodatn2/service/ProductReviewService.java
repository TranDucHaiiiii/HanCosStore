package com.example.demodatn2.service;

import com.example.demodatn2.dto.ProductReviewVM;
import com.example.demodatn2.entity.ChiTietDonHang;
import com.example.demodatn2.entity.DanhGiaSanPham;
import com.example.demodatn2.entity.SanPham;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.repository.ChiTietDonHangRepository;
import com.example.demodatn2.repository.DanhGiaSanPhamRepository;
import com.example.demodatn2.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReviewService {
    private static final Set<String> COMPLETED_STATUSES = Set.of("HOAN_THANH", "COMPLETED", "DELIVERED");

    private final DanhGiaSanPhamRepository danhGiaSanPhamRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;

    @Transactional(readOnly = true)
    public List<ProductReviewVM> getReviews(Integer sanPhamId) {
        return danhGiaSanPhamRepository.findBySanPham_IdOrderByNgayTaoDesc(sanPhamId)
                .stream()
                .map(this::toVm)
                .toList();
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Integer sanPhamId) {
        Double average = danhGiaSanPhamRepository.findAverageRatingBySanPhamId(sanPhamId);
        return average != null ? average : 0;
    }

    @Transactional(readOnly = true)
    public long getReviewCount(Integer sanPhamId) {
        return danhGiaSanPhamRepository.countBySanPham_Id(sanPhamId);
    }

    @Transactional(readOnly = true)
    public Map<Integer, Boolean> getReviewedProductMap(List<ChiTietDonHang> orderItems, Integer taiKhoanId) {
        if (taiKhoanId == null || orderItems == null || orderItems.isEmpty()) {
            return Map.of();
        }

        return orderItems.stream()
                .filter(item -> item.getBienTheSanPham() != null && item.getBienTheSanPham().getSanPham() != null)
                .map(item -> item.getBienTheSanPham().getSanPham().getId())
                .distinct()
                .collect(Collectors.toMap(
                        productId -> productId,
                        productId -> danhGiaSanPhamRepository.existsBySanPham_IdAndTaiKhoan_Id(productId, taiKhoanId)
                ));
    }

    @Transactional
    public void submitReview(Integer orderItemId, Integer taiKhoanId, Integer soSao, String noiDung) {
        if (taiKhoanId == null) {
            throw new RuntimeException("Ban can dang nhap de danh gia san pham.");
        }
        if (soSao == null || soSao < 1 || soSao > 5) {
            throw new RuntimeException("So sao danh gia phai tu 1 den 5.");
        }

        ChiTietDonHang orderItem = chiTietDonHangRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay san pham trong don hang."));

        if (orderItem.getDonHang() == null
                || orderItem.getDonHang().getTaiKhoan() == null
                || !taiKhoanId.equals(orderItem.getDonHang().getTaiKhoan().getId())) {
            throw new RuntimeException("Ban khong co quyen danh gia san pham nay.");
        }

        String status = normalizeStatus(orderItem.getDonHang().getTrangThai());
        if (!COMPLETED_STATUSES.contains(status)) {
            throw new RuntimeException("Chi co the danh gia san pham trong don hang da hoan thanh.");
        }

        SanPham sanPham = orderItem.getBienTheSanPham().getSanPham();
        if (danhGiaSanPhamRepository.existsBySanPham_IdAndTaiKhoan_Id(sanPham.getId(), taiKhoanId)) {
            throw new RuntimeException("Ban da danh gia san pham nay roi.");
        }

        TaiKhoan taiKhoan = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan."));

        DanhGiaSanPham review = new DanhGiaSanPham();
        review.setSanPham(sanPham);
        review.setTaiKhoan(taiKhoan);
        review.setSoSao(soSao);
        review.setNoiDung(cleanContent(noiDung));
        review.setNgayTao(Instant.now());
        danhGiaSanPhamRepository.save(review);
    }

    private ProductReviewVM toVm(DanhGiaSanPham review) {
        TaiKhoan taiKhoan = review.getTaiKhoan();
        String name = taiKhoan != null && taiKhoan.getHoTen() != null && !taiKhoan.getHoTen().isBlank()
                ? taiKhoan.getHoTen()
                : "Khach hang";

        return ProductReviewVM.builder()
                .id(review.getId())
                .soSao(review.getSoSao())
                .noiDung(review.getNoiDung())
                .ngayTao(review.getNgayTao())
                .tenKhachHang(name)
                .build();
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        return switch (status.trim().toUpperCase()) {
            case "DELIVERED", "COMPLETED" -> "HOAN_THANH";
            default -> status.trim().toUpperCase();
        };
    }

    private String cleanContent(String content) {
        if (content == null) {
            return null;
        }
        String cleaned = content.trim();
        return cleaned.length() > 1000 ? cleaned.substring(0, 1000) : cleaned;
    }
}
