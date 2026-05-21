package com.example.demodatn2.repository;

import com.example.demodatn2.entity.SanPham;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    Optional<SanPham> findByMaSanPham(String maSanPham);
    List<SanPham> findByTrangThai(String trangThai);
    // Load sản phẩm kèm biến thể + ảnh theo màu + ảnh gallery (tránh N+1)
    // Chia nhỏ để tránh MultipleBagFetchException
    @EntityGraph(attributePaths = {"bienThes"})
    @Query("select sp from SanPham sp where sp.id=?1")
    Optional<SanPham> findDetailWithBienTheById(Integer id);

    @EntityGraph(attributePaths = {"hinhAnhMauSacs"})
    @Query("select sp from SanPham sp where sp.id=?1")
    Optional<SanPham> findDetailWithHinhAnhMauSacById(Integer id);

    @EntityGraph(attributePaths = {"hinhAnhSanPhams"})
    @Query("select sp from SanPham sp where sp.id=?1")
    Optional<SanPham> findDetailWithHinhAnhSanPhamById(Integer id);
    //load list sanpham kem anh
    @EntityGraph(attributePaths = {"hinhAnhSanPhams", "chatLieu"})
    @Query("SELECT sp FROM SanPham sp where sp.trangThai='ACTIVE' AND (sp.daXoa= false or sp.daXoa is null)")
    List<SanPham> findActiveForListing();

    @EntityGraph(attributePaths = {"hinhAnhSanPhams"})
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThai='ACTIVE' AND (sp.daXoa= false or sp.daXoa is null) AND sp.danhMuc.id = :danhMucId")
    List<SanPham> findActiveByDanhMucId(@Param("danhMucId") Integer danhMucId);

    @EntityGraph(attributePaths = {"hinhAnhSanPhams"})
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThai='ACTIVE' AND (sp.daXoa= false or sp.daXoa is null) AND sp.danhMuc.id IN :danhMucIds")
    List<SanPham> findActiveByDanhMucIds(@Param("danhMucIds") List<Integer> danhMucIds);

    // Tìm kiếm và lọc sản phẩm cho admin
    @Query("SELECT sp FROM SanPham sp LEFT JOIN sp.chatLieu cl WHERE (sp.daXoa = false OR sp.daXoa IS NULL) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(sp.ten) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(sp.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(cl.tenChatLieu) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:danhMucId IS NULL OR sp.danhMuc.id = :danhMucId OR sp.danhMuc.danhMucCha.id = :danhMucId) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR sp.trangThai = :trangThai)")
    List<SanPham> searchAdmin(@Param("keyword") String keyword, 
                              @Param("danhMucId") Integer danhMucId, 
                              @Param("trangThai") String trangThai);

    @Query("SELECT sp FROM SanPham sp LEFT JOIN sp.chatLieu cl WHERE (sp.daXoa = false OR sp.daXoa IS NULL) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(sp.ten) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(sp.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(cl.tenChatLieu) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:danhMucId IS NULL OR sp.danhMuc.id = :danhMucId OR sp.danhMuc.danhMucCha.id = :danhMucId) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR sp.trangThai = :trangThai)")
    Page<SanPham> searchAdminPage(@Param("keyword") String keyword,
                                  @Param("danhMucId") Integer danhMucId,
                                  @Param("trangThai") String trangThai,
                                  Pageable pageable);

    // Tìm kiếm sản phẩm cho khách hàng (chỉ lấy ACTIVE và chưa xóa)
    @EntityGraph(attributePaths = {"hinhAnhSanPhams", "chatLieu"})
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThai='ACTIVE' AND (sp.daXoa = false OR sp.daXoa IS NULL) " +
           "AND (LOWER(sp.ten) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(sp.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<SanPham> searchActive(@Param("keyword") String keyword);

    // === Paginated versions ===
    @EntityGraph(attributePaths = {"hinhAnhSanPhams"})
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThai='ACTIVE' AND (sp.daXoa=false OR sp.daXoa IS NULL)")
    Page<SanPham> findActiveForListingPage(Pageable pageable);

    @EntityGraph(attributePaths = {"hinhAnhSanPhams"})
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThai='ACTIVE' AND (sp.daXoa=false OR sp.daXoa IS NULL) AND sp.danhMuc.id = :danhMucId")
    Page<SanPham> findActiveByDanhMucIdPage(@Param("danhMucId") Integer danhMucId, Pageable pageable);

    @EntityGraph(attributePaths = {"hinhAnhSanPhams"})
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThai='ACTIVE' AND (sp.daXoa=false OR sp.daXoa IS NULL) AND sp.danhMuc.id IN :danhMucIds")
    Page<SanPham> findActiveByDanhMucIdsPage(@Param("danhMucIds") List<Integer> danhMucIds, Pageable pageable);

    @EntityGraph(attributePaths = {"hinhAnhSanPhams"})
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThai='ACTIVE' AND (sp.daXoa=false OR sp.daXoa IS NULL) " +
           "AND (LOWER(sp.ten) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(sp.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SanPham> searchActivePage(@Param("keyword") String keyword, Pageable pageable);
}
