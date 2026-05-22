package com.example.demodatn2.repository;

import com.example.demodatn2.entity.DanhGiaSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DanhGiaSanPhamRepository extends JpaRepository<DanhGiaSanPham, Integer> {
    List<DanhGiaSanPham> findBySanPham_IdOrderByNgayTaoDesc(Integer sanPhamId);

    Optional<DanhGiaSanPham> findBySanPham_IdAndTaiKhoan_Id(Integer sanPhamId, Integer taiKhoanId);

    boolean existsBySanPham_IdAndTaiKhoan_Id(Integer sanPhamId, Integer taiKhoanId);

    @Query("select coalesce(avg(d.soSao), 0) from DanhGiaSanPham d where d.sanPham.id = :sanPhamId")
    Double findAverageRatingBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    long countBySanPham_Id(Integer sanPhamId);
}
