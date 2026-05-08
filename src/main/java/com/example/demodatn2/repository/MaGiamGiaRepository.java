package com.example.demodatn2.repository;

import com.example.demodatn2.entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Integer> {
    Optional<MaGiamGia> findByMa(String ma);

    @Query("SELECT m FROM MaGiamGia m WHERE UPPER(m.ma) = UPPER(:ma) AND m.trangThai = 'ACTIVE' AND m.batDauLuc <= CURRENT_TIMESTAMP AND m.ketThucLuc >= CURRENT_TIMESTAMP")
    Optional<MaGiamGia> findValidVoucher(String ma);

    @Query("SELECT m FROM MaGiamGia m WHERE m.trangThai = 'ACTIVE' AND m.batDauLuc <= CURRENT_TIMESTAMP AND m.ketThucLuc >= CURRENT_TIMESTAMP AND (m.soLuongToiDa IS NULL OR m.soLuongDaDung < m.soLuongToiDa)")
    java.util.List<MaGiamGia> findAvailableVouchers();

    @Modifying
    @Query("""
        update MaGiamGia m
        set m.soLuongDaDung = coalesce(m.soLuongDaDung, 0) + 1
        where m.id = :voucherId
          and (m.soLuongToiDa is null or coalesce(m.soLuongDaDung, 0) < m.soLuongToiDa)
    """)
    int incrementUsageIfAvailable(@Param("voucherId") Integer voucherId);
}
