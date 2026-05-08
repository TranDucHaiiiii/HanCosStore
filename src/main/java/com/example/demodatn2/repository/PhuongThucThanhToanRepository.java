package com.example.demodatn2.repository;

import com.example.demodatn2.entity.PhuongThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhuongThucThanhToanRepository extends JpaRepository<PhuongThucThanhToan, Integer> {
    Optional<PhuongThucThanhToan> findByMa(String ma);
}

