package com.example.demodatn2.repository;

import com.example.demodatn2.entity.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KichCoRepository extends JpaRepository<KichCo, Integer> {
    Optional<KichCo> findByTenKichCoIgnoreCase(String tenKichCo);
}
