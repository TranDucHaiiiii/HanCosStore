package com.example.demodatn2.repository;

import com.example.demodatn2.entity.YeuCauDoiTra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface YeuCauDoiTraRepository extends JpaRepository<YeuCauDoiTra, Integer> {
    boolean existsByDonHangId(Integer donHangId);

    @EntityGraph(attributePaths = {"donHang", "taiKhoan"})
    Optional<YeuCauDoiTra> findByDonHangId(Integer donHangId);

    @EntityGraph(attributePaths = {"donHang", "taiKhoan"})
    List<YeuCauDoiTra> findAllByOrderByNgayTaoDesc();

    @EntityGraph(attributePaths = {"donHang", "taiKhoan"})
    List<YeuCauDoiTra> findByTrangThaiOrderByNgayTaoDesc(String trangThai);

    Long countByTrangThaiIn(List<String> trangThai);

    @Override
    @EntityGraph(attributePaths = {"donHang", "taiKhoan"})
    Optional<YeuCauDoiTra> findById(Integer id);
}
