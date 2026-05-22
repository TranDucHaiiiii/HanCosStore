package com.example.demodatn2.repository;

import com.example.demodatn2.entity.LichSuXuLyDoiTra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LichSuXuLyDoiTraRepository extends JpaRepository<LichSuXuLyDoiTra, Integer> {
    List<LichSuXuLyDoiTra> findByYeuCauDoiTraIdOrderByThoiGianAsc(Integer yeuCauDoiTraId);
}
