package com.example.demodatn2.repository;

import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.GiaoDichThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GiaoDichThanhToanRepository extends JpaRepository<GiaoDichThanhToan, Integer> {
    Optional<GiaoDichThanhToan> findFirstByDonHangAndNhaCungCapOrderByNgayTaoDesc(DonHang donHang, String nhaCungCap);
}
