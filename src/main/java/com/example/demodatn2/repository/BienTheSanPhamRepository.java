package com.example.demodatn2.repository;

import com.example.demodatn2.entity.BienTheSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Integer> {
    List<BienTheSanPham> findBySanPham_Id(Integer SanPhamId);

    @Query("""
        select v
        from BienTheSanPham v
        where v.sanPham.id = :idSanPham
          and lower(v.mauSac.tenMau) = lower(:mauSac)
    """)
    List<BienTheSanPham> findBySanPham_IdAndMauSac(@Param("idSanPham") Integer idSanPham, @Param("mauSac") String mauSac);

    Optional<BienTheSanPham> findByMaSKU(String maSKU);

    @Query("""
        select v
        from BienTheSanPham v
        where v.sanPham.id = :idSanPham
          and lower(v.mauSac.tenMau) = lower(:mauSac)
          and lower(v.kichCo.tenKichCo) = lower(:kichCo)
    """)
    Optional<BienTheSanPham> findBySanPham_IdAndMauSacAndKichCo(@Param("idSanPham") Integer idSanPham,
                                                                @Param("mauSac") String mauSac,
                                                                @Param("kichCo") String kichCo);

    interface PriceRange{
        BigDecimal getMinGia();
        BigDecimal getMaxGia();
    }

    @Query("""
        select min(v.gia) as minGia, max(v.gia) as maxGia
        from BienTheSanPham v
        where v.sanPham.id = ?1
          and (v.trangThai is null or lower(v.trangThai) = 'active')
    """)
    PriceRange findPriceRange(Integer sanPhamId);
    @Query(value = """
        select distinct ms.TenMau
        from BIEN_THE_SAN_PHAM bt
        join MAU_SAC ms on ms.Id = bt.MauSacId
        where bt.SanPhamId = :sanPhamId
          and (bt.TrangThai is null or lower(bt.TrangThai) = 'active')
        order by ms.TenMau
    """, nativeQuery = true)
    List<String> findDistinctMauSac(Integer sanPhamId);

        @Query(value = """
                select distinct kc.TenKichCo
                from BIEN_THE_SAN_PHAM bt
                join KICH_CO kc on kc.Id = bt.KichCoId
                where bt.SanPhamId = :sanPhamId
                    and (bt.TrangThai is null or lower(bt.TrangThai) = 'active')
                order by kc.TenKichCo
        """, nativeQuery = true)
        List<String> findDistinctKichCo(Integer sanPhamId);

    @Query("""
        select count(v)
        from BienTheSanPham v
        where (v.trangThai is null or lower(v.trangThai) = 'active')
          and v.soLuongTon >= 0
          and v.soLuongTon <= ?1
    """)
    Long countActiveLowStock(Integer threshold);

        @Query("""
                select v
                from BienTheSanPham v
                join fetch v.sanPham sp
                join fetch v.mauSac ms
                join fetch v.kichCo kc
                where (v.trangThai is null or lower(v.trangThai) = 'active')
                    and v.soLuongTon >= 0
                    and v.soLuongTon <= ?1
                order by v.soLuongTon asc, v.id asc
        """)
        List<BienTheSanPham> findLowStockActiveVariants(Integer threshold, Pageable pageable);

        @Query(value = """
                select v
                from BienTheSanPham v
                join v.sanPham sp
                where (sp.daXoa is null or sp.daXoa = false)
                    and (v.trangThai is null or lower(v.trangThai) = 'active')
                    and (
                                :keyword is null
                                or lower(sp.ten) like lower(concat('%', :keyword, '%'))
                                or lower(v.maSKU) like lower(concat('%', :keyword, '%'))
                                or lower(v.mauSac.tenMau) like lower(concat('%', :keyword, '%'))
                                or lower(v.kichCo.tenKichCo) like lower(concat('%', :keyword, '%'))
                    )
                """,
                countQuery = """
                select count(v)
                from BienTheSanPham v
                join v.sanPham sp
                where (sp.daXoa is null or sp.daXoa = false)
                    and (v.trangThai is null or lower(v.trangThai) = 'active')
                    and (
                                :keyword is null
                                or lower(sp.ten) like lower(concat('%', :keyword, '%'))
                                or lower(v.maSKU) like lower(concat('%', :keyword, '%'))
                                or lower(v.mauSac.tenMau) like lower(concat('%', :keyword, '%'))
                                or lower(v.kichCo.tenKichCo) like lower(concat('%', :keyword, '%'))
                    )
                """)
        Page<BienTheSanPham> searchInventoryVariants(String keyword, Pageable pageable);

    @Modifying
    @Query("""
        update BienTheSanPham v
        set v.soLuongTon = v.soLuongTon - :qty
        where v.id = :variantId
          and v.soLuongTon >= :qty
    """)
    int decrementStockIfEnough(@Param("variantId") Integer variantId, @Param("qty") Integer qty);

    @Modifying
    @Query("""
        update BienTheSanPham v
        set v.soLuongTon = v.soLuongTon + :qty
        where v.id = :variantId
    """)
    int incrementStock(@Param("variantId") Integer variantId, @Param("qty") Integer qty);

}
