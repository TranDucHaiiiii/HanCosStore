package com.example.demodatn2.repository;

import com.example.demodatn2.entity.RefundTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, Long> {
    boolean existsByYeuCauDoiTra_Id(Integer yeuCauDoiTraId);

    @EntityGraph(attributePaths = {"nguoiXuLy"})
    List<RefundTransaction> findByYeuCauDoiTraIdOrderByThoiGianHoanDesc(Integer yeuCauDoiTraId);
}
