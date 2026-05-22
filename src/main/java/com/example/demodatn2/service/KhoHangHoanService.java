package com.example.demodatn2.service;

import com.example.demodatn2.dto.ReturnWarehouseItemDTO;
import com.example.demodatn2.entity.*;
import com.example.demodatn2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhoHangHoanService {
    public static final String CONDITION_NEW = "NEW";
    public static final String CONDITION_OPEN_BOX = "OPEN_BOX";
    public static final String CONDITION_BOX_DAMAGED = "BOX_DAMAGED";

    public static final String HANDLING_RESELL = "BAN_LAI";
    public static final String HANDLING_LIQUIDATE = "THANH_LY";
    public static final String HANDLING_IMPORT_MAIN = "NHAP_KHO_CHINH";

    public static final String STATUS_WAITING = "CHO_XU_LY";
    public static final String STATUS_IMPORTED = "DA_NHAP_KHO";
    public static final String STATUS_LIQUIDATED = "DA_THANH_LY";

    private final KhoHangHoanRepository khoHangHoanRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final GiaoDichTonKhoRepository giaoDichTonKhoRepository;
    private final HinhAnhSanPhamRepository hinhAnhSanPhamRepository;

    @Transactional
    public void createFromPassedInspection(ChiTietDoiTra detail, String note) {
        if (detail == null || detail.getId() == null || khoHangHoanRepository.existsByChiTietDoiTra_Id(detail.getId())) {
            return;
        }

        KhoHangHoan item = new KhoHangHoan();
        item.setBienTheSanPham(detail.getChiTietDonHang().getBienTheSanPham());
        item.setYeuCauDoiTra(detail.getYeuCauDoiTra());
        item.setChiTietDoiTra(detail);
        item.setSoLuong(detail.getSoLuong());
        item.setTinhTrang(CONDITION_OPEN_BOX);
        item.setHuongXuLy(HANDLING_RESELL);
        item.setTrangThai(STATUS_WAITING);
        item.setGhiChu(clean(note));
        item.setNgayNhap(Instant.now());
        khoHangHoanRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<ReturnWarehouseItemDTO> getReturnWarehouseItems() {
        return khoHangHoanRepository.findTop50ByTrangThaiInOrderByNgayNhapDesc(List.of(STATUS_WAITING))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void importToMainStock(Long id, Integer actorId, String note) {
        KhoHangHoan item = getWaitingItem(id);
        BienTheSanPham variant = item.getBienTheSanPham();
        int currentStock = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
        int quantity = item.getSoLuong() != null ? item.getSoLuong() : 0;

        variant.setSoLuongTon(currentStock + quantity);
        bienTheSanPhamRepository.save(variant);

        GiaoDichTonKho tx = new GiaoDichTonKho();
        tx.setBienTheSanPham(variant);
        tx.setLoai("NHAP");
        tx.setSoLuong(quantity);
        tx.setThamChieuLoai("KHO_HANG_HOAN");
        tx.setThamChieuId(item.getId().intValue());
        tx.setGhiChu(clean(note) != null ? clean(note) : "Nhap kho chinh tu hang hoan dat QC");
        tx.setNgayTao(Instant.now());
        giaoDichTonKhoRepository.save(tx);

        item.setHuongXuLy(HANDLING_IMPORT_MAIN);
        item.setTrangThai(STATUS_IMPORTED);
        item.setGhiChu(clean(note));
        khoHangHoanRepository.save(item);
    }

    @Transactional
    public void markForLiquidation(Long id, String note) {
        KhoHangHoan item = getWaitingItem(id);
        item.setHuongXuLy(HANDLING_LIQUIDATE);
        item.setTrangThai(STATUS_LIQUIDATED);
        item.setGhiChu(clean(note));
        khoHangHoanRepository.save(item);
    }

    @Transactional
    public void markForResell(Long id, String note) {
        KhoHangHoan item = getWaitingItem(id);
        item.setHuongXuLy(HANDLING_RESELL);
        item.setTrangThai(STATUS_WAITING);
        item.setGhiChu(clean(note));
        khoHangHoanRepository.save(item);
    }

    private KhoHangHoan getWaitingItem(Long id) {
        KhoHangHoan item = khoHangHoanRepository.findWithGraphById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay hang hoan."));
        if (!STATUS_WAITING.equals(item.getTrangThai())) {
            throw new RuntimeException("Hang hoan nay da duoc xu ly.");
        }
        return item;
    }

    private ReturnWarehouseItemDTO toDto(KhoHangHoan item) {
        BienTheSanPham variant = item.getBienTheSanPham();
        SanPham product = variant != null ? variant.getSanPham() : null;
        String image = product != null && product.getId() != null
                ? hinhAnhSanPhamRepository.findFirstBySanPham_IdOrderByLaAnhChinhDescThuTuAscIdAsc(product.getId())
                        .map(HinhAnhSanPham::getDuongDanAnh)
                        .orElse("/images/no-image.png")
                : "/images/no-image.png";

        return ReturnWarehouseItemDTO.builder()
                .id(item.getId())
                .sanPhamId(product != null ? product.getId() : null)
                .bienTheId(variant != null ? variant.getId() : null)
                .tenSanPham(product != null ? product.getTen() : "-")
                .hinhAnh(image)
                .maSKU(variant != null ? variant.getMaSKU() : "-")
                .mauSac(variant != null ? variant.getMauSac() : "-")
                .kichCo(variant != null ? variant.getKichCo() : "-")
                .soLuong(item.getSoLuong())
                .tinhTrang(item.getTinhTrang())
                .huongXuLy(item.getHuongXuLy())
                .trangThai(item.getTrangThai())
                .maDonHoan(item.getYeuCauDoiTra() != null && item.getYeuCauDoiTra().getDonHang() != null
                        ? item.getYeuCauDoiTra().getDonHang().getMaDonHang()
                        : "-")
                .ngayNhap(item.getNgayNhap())
                .ghiChu(item.getGhiChu())
                .build();
    }

    private String clean(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
