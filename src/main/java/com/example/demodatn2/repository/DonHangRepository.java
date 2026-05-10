package com.example.demodatn2.repository;

import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.TaiKhoan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {

    // Lay danh sach don theo tai khoan, sap xep moi nhat truoc.
    List<DonHang> findByTaiKhoanOrderByNgayDatDesc(TaiKhoan taiKhoan);

    // Kiem tra tai khoan da co don hang hay chua.
    boolean existsByTaiKhoan_Id(Integer taiKhoanId);

    // Lay toan bo don hang, sap xep moi nhat truoc.
    List<DonHang> findAllByOrderByNgayDatDesc();

    // Lay don theo trang thai, sap xep moi nhat truoc.
    List<DonHang> findByTrangThaiOrderByNgayDatDesc(String trangThai);

    // Dem so don theo tap trang thai.
    long countByTrangThaiIn(List<String> trangThais);

    // Tim don theo ma don, khong phan biet hoa thuong.
    Optional<DonHang> findByMaDonHangIgnoreCase(String maDonHang);


    // Tinh tong doanh thu cua cac don da hoan thanh.
    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED')")
        BigDecimal tinhTongDoanhThu();

    // Dem so don da hoan thanh.
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED')")
        Long demDonHangThanhCong();

    // Tinh doanh thu tu ngay chi dinh den hien tai.
    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') AND d.ngayDat >= ?1")
        BigDecimal tinhDoanhThuTuNgay(Instant tuNgay);

    // Dem so don thanh cong tu ngay chi dinh den hien tai.
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') AND d.ngayDat >= ?1")
        Long demDonHangTuNgay(Instant tuNgay);

    // Tong hop doanh thu theo ngay trong khoang loc.
    @org.springframework.data.jpa.repository.Query("SELECT CAST(d.ngayDat AS LocalDate) as date, SUM(d.tongTien) as amount " +
            "FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay) " +
            "GROUP BY CAST(d.ngayDat AS LocalDate) " +
            "ORDER BY CAST(d.ngayDat AS LocalDate) ASC")
        List<Object[]> layDoanhThuTheoNgay(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    // Tinh tong doanh thu trong khoang thoi gian loc.
    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay)")
        BigDecimal tinhDoanhThuTrongKhoang(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    // Tinh tong tien theo danh sach trang thai trong khoang loc.
    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai IN :statuses " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay)")
        BigDecimal tinhTongTienTheoTrangThaiTrongKhoang(@Param("statuses") List<String> statuses,
                                                                                                        @Param("tuNgay") Instant tuNgay,
                                                                                                        @Param("denNgay") Instant denNgay);

    // Dem don thanh cong trong khoang thoi gian loc.
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay)")
        Long demDonHangTrongKhoang(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    // Lay danh sach don da hoan thanh trong khoang loc.
    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE d.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED') " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay) " +
            "ORDER BY d.ngayDat DESC")
        List<DonHang> timDonHoanThanhTrongKhoang(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    // Tim don theo tu khoa tren ma don, ten nhan, hoac so dien thoai.
    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE " +
            "LOWER(d.maDonHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.hoTenNhan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.soDienThoaiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<DonHang> timKiemTheoTuKhoa(@Param("keyword") String keyword);

    // Tim don theo tu khoa va trang thai cu the.
    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE d.trangThai = :status AND (" +
            "LOWER(d.maDonHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.hoTenNhan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.soDienThoaiNhan) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        List<DonHang> timKiemTheoTuKhoaVaTrangThai(@Param("keyword") String keyword, @Param("status") String status);

    // Lay don theo trang thai va moc thoi gian cap nhat/dat de xu ly tu dong.
    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE d.trangThai = :status AND (" +
            "(d.ngayCapNhat IS NOT NULL AND d.ngayCapNhat <= :before) OR " +
            "(d.ngayCapNhat IS NULL AND d.ngayDat <= :before))")
        List<DonHang> timTheoTrangThaiVaCapNhatTruoc(@Param("status") String status,
                                                                                                 @Param("before") Instant before);

    // Dem so don theo nhieu trang thai tinh tu ngay from.
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai IN :trangThais AND d.ngayDat >= :from")
        Long demTheoTrangThaiVaNgayDatTu(@Param("trangThais") List<String> trangThais, @Param("from") Instant from);

    // Tim don theo tu khoa + trang thai va sap xep theo thu tu uu tien trang thai.
    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE d.trangThai = :status AND (" +
            "LOWER(d.maDonHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.hoTenNhan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.soDienThoaiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY CASE UPPER(d.trangThai) " +
            "WHEN 'CHO_XAC_NHAN' THEN 1 " +
            "WHEN 'PENDING' THEN 1 " +
            "WHEN 'DA_XAC_NHAN' THEN 2 " +
            "WHEN 'CONFIRMED' THEN 2 " +
            "WHEN 'DANG_GIAO' THEN 3 " +
            "WHEN 'SHIPPING' THEN 3 " +
            "WHEN 'LOI_VAN_CHUYEN' THEN 4 " +
            "WHEN 'LOST' THEN 4 " +
            "WHEN 'HOAN_THANH' THEN 5 " +
            "WHEN 'COMPLETED' THEN 5 " +
            "WHEN 'DELIVERED' THEN 5 " +
            "WHEN 'TRA_HANG' THEN 6 " +
            "WHEN 'RETURN_REQUESTED' THEN 6 " +
            "WHEN 'RETURNED' THEN 6 " +
            "WHEN 'DA_HUY' THEN 7 " +
            "WHEN 'CANCELLED' THEN 7 " +
            "ELSE 99 END, d.ngayDat DESC")
        List<DonHang> timKiemTheoTrangThaiUuTien(@Param("keyword") String keyword,
                                                                                         @Param("status") String status);

    // Tong hop khach tiem nang theo tong chi tieu, so don, va lan mua gan nhat.
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
        List<Object[]> timKhachHangTiemNang(@Param("fromDate") Instant fromDate, Pageable pageable);

    // Tim don theo tong tien va thoi gian tao (su dung khi webhook khong co ma don)
    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE d.tongTien = :amount AND d.ngayDat >= :fromTime ORDER BY d.ngayDat DESC")
    List<DonHang> findByTongTienAndNgayDatGreaterThan(@Param("amount") BigDecimal amount, @Param("fromTime") Instant fromTime);

    @org.springframework.data.jpa.repository.Query("""
            select d
            from DonHang d
            join fetch d.taiKhoan tk
            where d.id = :orderId
              and tk.id = :customerId
              and d.trangThai in ('HOAN_THANH', 'DELIVERED', 'COMPLETED')
            """)
    Optional<DonHang> findOwnedCompletedForReturn(@Param("orderId") Integer orderId,
                                                  @Param("customerId") Integer customerId);
}
