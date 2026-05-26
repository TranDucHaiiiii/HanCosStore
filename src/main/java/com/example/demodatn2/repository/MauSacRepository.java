package com.example.demodatn2.repository;

import com.example.demodatn2.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    Optional<MauSac> findByTenMauIgnoreCase(String tenMau);
}
