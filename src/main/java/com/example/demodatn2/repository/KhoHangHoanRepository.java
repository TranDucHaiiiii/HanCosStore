package com.example.demodatn2.repository;

import com.example.demodatn2.entity.KhoHangHoan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KhoHangHoanRepository extends JpaRepository<KhoHangHoan, Long> {
    boolean existsByChiTietDoiTra_Id(Integer chiTietDoiTraId);

    @EntityGraph(attributePaths = {"bienTheSanPham", "bienTheSanPham.sanPham", "yeuCauDoiTra", "yeuCauDoiTra.donHang"})
    List<KhoHangHoan> findTop50ByTrangThaiInOrderByNgayNhapDesc(Collection<String> trangThai);

    @EntityGraph(attributePaths = {"bienTheSanPham", "bienTheSanPham.sanPham", "yeuCauDoiTra", "yeuCauDoiTra.donHang", "chiTietDoiTra"})
    @Query("select k from KhoHangHoan k where k.id = :id")
    Optional<KhoHangHoan> findWithGraphById(@Param("id") Long id);
}
