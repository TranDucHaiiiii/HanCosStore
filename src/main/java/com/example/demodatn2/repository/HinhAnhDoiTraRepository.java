package com.example.demodatn2.repository;

import com.example.demodatn2.entity.HinhAnhDoiTra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HinhAnhDoiTraRepository extends JpaRepository<HinhAnhDoiTra, Integer> {
    List<HinhAnhDoiTra> findByYeuCauDoiTraIdOrderByIdAsc(Integer yeuCauDoiTraId);
}
