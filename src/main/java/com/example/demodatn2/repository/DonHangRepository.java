package com.example.demodatn2.repository;

import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    List<DonHang> findByTaiKhoanOrderByNgayDatDesc(TaiKhoan taiKhoan);
    boolean existsByTaiKhoan_Id(Integer taiKhoanId);
    List<DonHang> findAllByOrderByNgayDatDesc();
    List<DonHang> findByTrangThaiOrderByNgayDatDesc(String trangThai);
        long countByTrangThaiIn(List<String> trangThais);
        java.util.Optional<DonHang> findByMaDonHangIgnoreCase(String maDonHang);

        @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED')")
    BigDecimal sumTongDoanhThu();

        @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED')")
    Long countDonHangThanhCong();

        @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') AND d.ngayDat >= ?1")
    BigDecimal sumDoanhThuTuNgay(Instant tuNgay);

        @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') AND d.ngayDat >= ?1")
    Long countDonHangTuNgay(Instant tuNgay);

    @org.springframework.data.jpa.repository.Query("SELECT CAST(d.ngayDat AS LocalDate) as date, SUM(d.tongTien) as amount " +
            "FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay) " +
            "GROUP BY CAST(d.ngayDat AS LocalDate) " +
            "ORDER BY CAST(d.ngayDat AS LocalDate) ASC")
    List<Object[]> getDoanhThuTheoNgay(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay)")
    BigDecimal sumDoanhThuTrongKhoang(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai IN :statuses " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay)")
    BigDecimal sumTongTienByTrangThaiInRange(@Param("statuses") List<String> statuses,
                                             @Param("tuNgay") Instant tuNgay,
                                             @Param("denNgay") Instant denNgay);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay)")
    Long countDonHangTrongKhoang(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay) " +
            "ORDER BY d.ngayDat DESC")
    List<DonHang> findAllDeliveredInRange(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE " +
            "LOWER(d.maDonHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.hoTenNhan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.soDienThoaiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DonHang> search(@Param("keyword") String keyword);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE d.trangThai = :status AND (" +
            "LOWER(d.maDonHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.hoTenNhan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.soDienThoaiNhan) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<DonHang> searchWithStatus(@Param("keyword") String keyword, @Param("status") String status);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE d.trangThai = :status AND (" +
            "(d.ngayCapNhat IS NOT NULL AND d.ngayCapNhat <= :before) OR " +
            "(d.ngayCapNhat IS NULL AND d.ngayDat <= :before))")
    List<DonHang> findByTrangThaiAndLastUpdateBefore(@Param("status") String status, @Param("before") Instant before);

        Long countByTrangThaiInAndNgayDatGreaterThanEqual(List<String> trangThais, Instant from);

        Long countByNgayDatGreaterThanEqual(Instant from);

    @org.springframework.data.jpa.repository.Query("""
            select d.taiKhoan.id,
                   coalesce(d.taiKhoan.hoTen, d.hoTenNhan),
                   coalesce(d.taiKhoan.soDienThoai, d.soDienThoaiNhan),
                   count(d.id),
                   sum(d.tongTien),
                   max(d.ngayDat)
            from DonHang d
            where d.taiKhoan is not null
              and d.trangThai in ('HOAN_THANH', 'COMPLETED', 'DELIVERED')
              and (:fromDate is null or d.ngayDat >= :fromDate)
            group by d.taiKhoan.id, d.taiKhoan.hoTen, d.hoTenNhan, d.taiKhoan.soDienThoai, d.soDienThoaiNhan
            order by sum(d.tongTien) desc, count(d.id) desc, max(d.ngayDat) desc
            """)
    List<Object[]> findPotentialCustomers(@Param("fromDate") Instant fromDate, Pageable pageable);
}
