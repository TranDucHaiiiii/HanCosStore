package com.example.demodatn2.repository;

import com.example.demodatn2.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
    Optional<TaiKhoan> findByEmail(String email);
    Optional<TaiKhoan> findByEmailIgnoreCase(String email);

    @Query("SELECT COUNT(DISTINCT t.id) FROM TaiKhoan t JOIN t.vaiTros v WHERE v.ma = 'CUSTOMER'")
    Long countCustomers();

    @Query("SELECT DISTINCT t FROM TaiKhoan t LEFT JOIN t.vaiTros v WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(t.tenDangNhap) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(t.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(t.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR t.trangThai = :trangThai) " +
           "AND (:vaiTro IS NULL OR :vaiTro = '' OR v.ma = :vaiTro)")
    List<TaiKhoan> search(@Param("keyword") String keyword,
                          @Param("trangThai") String trangThai,
                          @Param("vaiTro") String vaiTro);
}
