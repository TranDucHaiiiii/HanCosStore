package com.example.demodatn2.repository;

import com.example.demodatn2.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    Optional<ThuongHieu> findByMaIgnoreCase(String ma);

    Optional<ThuongHieu> findByTenIgnoreCase(String ten);
}
