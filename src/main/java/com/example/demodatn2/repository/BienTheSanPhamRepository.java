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

    List<BienTheSanPham> findBySanPham_IdAndMauSac(Integer idSanPham, String mauSac);

    Optional<BienTheSanPham> findByMaSKU(String maSKU);

    Optional<BienTheSanPham> findBySanPham_IdAndMauSacAndKichCo(Integer idSanPham, String mauSac, String kichCo);

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
    @Query("""
        select distinct v.mauSac
        from BienTheSanPham v
        where v.sanPham.id = ?1
          and (v.trangThai is null or lower(v.trangThai) = 'active')
        order by v.mauSac
    """)
    List<String> findDistinctMauSac(Integer sanPhamId);

        @Query("""
                select distinct v.kichCo
                from BienTheSanPham v
                where v.sanPham.id = ?1
                    and (v.trangThai is null or lower(v.trangThai) = 'active')
                order by v.kichCo
        """)
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
                                or lower(v.mauSac) like lower(concat('%', :keyword, '%'))
                                or lower(v.kichCo) like lower(concat('%', :keyword, '%'))
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
                                or lower(v.mauSac) like lower(concat('%', :keyword, '%'))
                                or lower(v.kichCo) like lower(concat('%', :keyword, '%'))
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
