package com.example.demodatn2.repository;

import com.example.demodatn2.entity.ChiTietDoiTra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChiTietDoiTraRepository extends JpaRepository<ChiTietDoiTra, Integer> {
    @EntityGraph(attributePaths = {"chiTietDonHang", "chiTietDonHang.bienTheSanPham", "chiTietDonHang.bienTheSanPham.sanPham"})
    List<ChiTietDoiTra> findByYeuCauDoiTraId(Integer yeuCauDoiTraId);

    @EntityGraph(attributePaths = {
            "yeuCauDoiTra",
            "yeuCauDoiTra.donHang",
            "chiTietDonHang",
            "chiTietDonHang.bienTheSanPham",
            "chiTietDonHang.bienTheSanPham.sanPham"
    })
    @Query("select c from ChiTietDoiTra c where c.id = :id")
    Optional<ChiTietDoiTra> findWithReturnRequestById(@Param("id") Integer id);

    boolean existsByYeuCauDoiTraIdAndInspectionStatus(Integer yeuCauDoiTraId, String inspectionStatus);
}
