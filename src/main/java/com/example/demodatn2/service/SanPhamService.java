package com.example.demodatn2.service;



import com.example.demodatn2.dto.*;
import com.example.demodatn2.entity.*;
import com.example.demodatn2.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
// Service nghiệp vụ sản phẩm: tạo/sửa/xóa mềm, quản lý biến thể, ảnh và nhập kho.
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final BienTheSanPhamRepository bienTheRepository;
    private final GiaoDichTonKhoRepository giaoDichTonKhoRepository;
    private final HinhAnhSanPhamRepository hinhAnhSanPhamRepository;
    private final HinhAnhMauSacRepository hinhAnhMauSacRepository;
    private final DanhMucRepository danhMucRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    /**
     * Tạo sản phẩm mới kèm biến thể và hình ảnh
     */
    @Transactional
    public SanPhamResponseDTO createSanPham(SanPhamRequestDTO requestDTO) {
        log.info("Creating new product: {}", requestDTO.getTen());

        // 1. Validate
        validateSanPhamRequest(requestDTO);

        // 2. Tạo SanPham entity
        SanPham sanPham = new SanPham();
        sanPham.setMaSanPham(requestDTO.getMaSanPham());
        sanPham.setTen(requestDTO.getTen());
        sanPham.setMoTaNgan(requestDTO.getMoTaNgan());
        sanPham.setMoTa(requestDTO.getMoTa());
        sanPham.setChatLieu(requestDTO.getChatLieu());
        sanPham.setGioiTinh(requestDTO.getGioiTinh());
        sanPham.setTrangThai("ACTIVE");
        sanPham.setDaXoa(false);
        sanPham.setNgayTao(Instant.now());

        // Set danh mục nếu có
        if (requestDTO.getDanhMucId() != null) {
            DanhMuc danhMuc = danhMucRepository.findById(requestDTO.getDanhMucId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại: " + requestDTO.getDanhMucId()));
            sanPham.setDanhMuc(danhMuc);
        }

        // 3. Lưu sản phẩm trước
        sanPham = sanPhamRepository.save(sanPham);
        log.info("Product saved with ID: {}", sanPham.getId());

        // 4. Tạo biến thể
        if (requestDTO.getBienThes() != null && !requestDTO.getBienThes().isEmpty()) {
            List<BienTheSanPham> bienThes = createBienThes(sanPham, requestDTO.getBienThes());
            sanPham.getBienThes().addAll(bienThes);
            log.info("Created {} variants", bienThes.size());
        }

        // 5. Tạo hình ảnh sản phẩm (gallery)
        if (requestDTO.getHinhAnhSanPhams() != null && !requestDTO.getHinhAnhSanPhams().isEmpty()) {
            List<HinhAnhSanPham> hinhAnhs = createHinhAnhSanPhams(sanPham, requestDTO.getHinhAnhSanPhams());
            sanPham.getHinhAnhSanPhams().addAll(hinhAnhs);
            log.info("Created {} product images", hinhAnhs.size());
        }

        // 6. Tạo hình ảnh theo màu sắc
        if (requestDTO.getHinhAnhMauSacs() != null && !requestDTO.getHinhAnhMauSacs().isEmpty()) {
            List<HinhAnhMauSac> hinhAnhMauSacs = createHinhAnhMauSacs(sanPham, requestDTO.getHinhAnhMauSacs());
            sanPham.getHinhAnhMauSacs().addAll(hinhAnhMauSacs);
            log.info("Created {} color images", hinhAnhMauSacs.size());
        }

        sanPham = sanPhamRepository.save(sanPham);

        // 7. Convert to response DTO
        return convertToResponseDTO(sanPham);
    }

    /**
     * Validate request
     */
    private void validateSanPhamRequest(SanPhamRequestDTO requestDTO) {
        // Check mã sản phẩm đã tồn tại
        if (sanPhamRepository.findByMaSanPham(requestDTO.getMaSanPham()).isPresent()) {
            throw new RuntimeException("Mã sản phẩm đã tồn tại: " + requestDTO.getMaSanPham());
        }

        // Check tên sản phẩm
        if (requestDTO.getTen() == null || requestDTO.getTen().trim().isEmpty()) {
            throw new RuntimeException("Tên sản phẩm không được để trống");
        }

        // Check biến thể
        if (requestDTO.getBienThes() == null || requestDTO.getBienThes().isEmpty()) {
            throw new RuntimeException("Sản phẩm phải có ít nhất 1 biến thể");
        }

        // Check mã SKU trùng
        List<String> maSKUs = requestDTO.getBienThes().stream()
                .map(BienTheRequestDTO::getMaSKU)
                .collect(Collectors.toList());

        long distinctCount = maSKUs.stream().distinct().count();
        if (distinctCount != maSKUs.size()) {
            throw new RuntimeException("Có mã SKU bị trùng lặp trong danh sách biến thể");
        }

        // Check SKU đã tồn tại trong DB
        for (String maSKU : maSKUs) {
            if (bienTheRepository.findByMaSKU(maSKU).isPresent()) {
                throw new RuntimeException("Mã SKU đã tồn tại: " + maSKU);
            }
        }
    }

    /**
     * Tạo danh sách biến thể
     */
    private List<BienTheSanPham> createBienThes(SanPham sanPham, List<BienTheRequestDTO> bienTheDTOs) {
        List<BienTheSanPham> bienThes = new ArrayList<>();

        for (BienTheRequestDTO dto : bienTheDTOs) {
            BienTheSanPham bienThe = new BienTheSanPham();
            bienThe.setSanPham(sanPham);
            bienThe.setMaSKU(dto.getMaSKU());
            bienThe.setMauSac(dto.getMauSac());
            bienThe.setKichCo(dto.getKichCo());
            bienThe.setGia(dto.getGia());
            bienThe.setGiaGoc(dto.getGiaGoc());
            bienThe.setSoLuongTon(dto.getSoLuongTon());
            bienThe.setKhoiLuongGram(dto.getKhoiLuongGram());
            bienThe.setTrangThai("ACTIVE");
            bienThe.setNgayTao(Instant.now());

            bienThe = bienTheRepository.save(bienThe);
            bienThes.add(bienThe);
        }

        return bienThes;
    }

    /**
     * Tạo danh sách hình ảnh sản phẩm
     */
    private List<HinhAnhSanPham> createHinhAnhSanPhams(SanPham sanPham, List<HinhAnhSanPhamDTO> hinhAnhDTOs) {
        if (hinhAnhDTOs == null || hinhAnhDTOs.isEmpty()) {
            return Collections.emptyList();
        }

        List<HinhAnhSanPhamDTO> validDTOs = hinhAnhDTOs.stream()
                .filter(dto -> dto != null && dto.getDuongDanAnh() != null && !dto.getDuongDanAnh().trim().isEmpty())
                .collect(Collectors.toList());

        if (validDTOs.isEmpty()) {
            return Collections.emptyList();
        }

        List<HinhAnhSanPham> hinhAnhs = new ArrayList<>();

        // Đảm bảo chỉ có 1 ảnh chính
        long countAnhChinh = validDTOs.stream()
                .filter(dto -> dto.getLaAnhChinh() != null && dto.getLaAnhChinh())
                .count();

        if (countAnhChinh == 0) {
            // Nếu không có ảnh chính, set ảnh đầu tiên làm ảnh chính
            validDTOs.get(0).setLaAnhChinh(true);
        } else if (countAnhChinh > 1) {
            throw new RuntimeException("Chỉ được có 1 ảnh chính");
        }

        for (HinhAnhSanPhamDTO dto : validDTOs) {
            HinhAnhSanPham hinhAnh = new HinhAnhSanPham();
            hinhAnh.setSanPham(sanPham);
            hinhAnh.setDuongDanAnh(dto.getDuongDanAnh().trim());
            hinhAnh.setLaAnhChinh(dto.getLaAnhChinh() != null ? dto.getLaAnhChinh() : false);
            hinhAnh.setThuTu(dto.getThuTu() != null ? dto.getThuTu() : 0);
            hinhAnh.setNgayTao(Instant.now());

            // hinhAnh = hinhAnhSanPhamRepository.save(hinhAnh);
            hinhAnhs.add(hinhAnh);
        }

        return hinhAnhs;
    }

    /**
     * Tạo danh sách hình ảnh theo màu sắc
     */
    private List<HinhAnhMauSac> createHinhAnhMauSacs(SanPham sanPham, List<HinhAnhMauSacDTO> hinhAnhMauSacDTOs) {
        if (hinhAnhMauSacDTOs == null || hinhAnhMauSacDTOs.isEmpty()) {
            return Collections.emptyList();
        }

        List<HinhAnhMauSac> hinhAnhMauSacs = new ArrayList<>();

        for (HinhAnhMauSacDTO dto : hinhAnhMauSacDTOs) {
            if (dto == null
                    || dto.getMauSac() == null || dto.getMauSac().trim().isEmpty()
                    || dto.getDuongDanAnh() == null || dto.getDuongDanAnh().trim().isEmpty()) {
                continue;
            }

            HinhAnhMauSac hinhAnhMauSac = new HinhAnhMauSac();
            hinhAnhMauSac.setSanPham(sanPham);
            hinhAnhMauSac.setMauSac(dto.getMauSac().trim());
            hinhAnhMauSac.setDuongDanAnh(dto.getDuongDanAnh().trim());
            hinhAnhMauSac.setNgayTao(Instant.now());

            // hinhAnhMauSac = hinhAnhMauSacRepository.save(hinhAnhMauSac);
            hinhAnhMauSacs.add(hinhAnhMauSac);
        }

        return hinhAnhMauSacs;
    }

    /**
     * Convert entity sang response DTO
     */
    private SanPhamResponseDTO convertToResponseDTO(SanPham sanPham) {
        SanPhamResponseDTO responseDTO = new SanPhamResponseDTO();
        responseDTO.setId(sanPham.getId());
        responseDTO.setMaSanPham(sanPham.getMaSanPham());
        responseDTO.setTen(sanPham.getTen());
        responseDTO.setMoTaNgan(sanPham.getMoTaNgan());
        responseDTO.setMoTa(sanPham.getMoTa());
        responseDTO.setChatLieu(sanPham.getChatLieu());
        responseDTO.setGioiTinh(sanPham.getGioiTinh());
        responseDTO.setTrangThai(sanPham.getTrangThai());
        responseDTO.setNgayTao(sanPham.getNgayTao());
        responseDTO.setNgayCapNhat(sanPham.getNgayCapNhat());

        // Danh mục
        if (sanPham.getDanhMuc() != null) {
            responseDTO.setDanhMucId(sanPham.getDanhMuc().getId());
            responseDTO.setTenDanhMuc(sanPham.getDanhMuc().getTen());
            if (sanPham.getDanhMuc().getDanhMucCha() != null) {
                responseDTO.setParentDanhMucId(sanPham.getDanhMuc().getDanhMucCha().getId());
                responseDTO.setTenParentDanhMuc(sanPham.getDanhMuc().getDanhMucCha().getTen());
            }
        }

        // Biến thể
        List<BienTheResponseDTO> bienTheDTOs = sanPham.getBienThes().stream()
                .map(this::convertBienTheToDTO)
                .collect(Collectors.toList());
        responseDTO.setBienThes(bienTheDTOs);

        // Tính tổng số lượng tồn
        int tongTon = bienTheDTOs.stream()
                .mapToInt(bt -> bt.getSoLuongTon() != null ? bt.getSoLuongTon() : 0)
                .sum();
        responseDTO.setTongSoLuongTon(tongTon);

        // Hình ảnh sản phẩm
        List<HinhAnhSanPhamDTO> hinhAnhDTOs = sanPham.getHinhAnhSanPhams().stream()
                .map(this::convertHinhAnhSanPhamToDTO)
                .collect(Collectors.toList());
        responseDTO.setHinhAnhSanPhams(hinhAnhDTOs);

        // Hình ảnh màu sắc
        List<HinhAnhMauSacDTO> hinhAnhMauSacDTOs = sanPham.getHinhAnhMauSacs().stream()
                .map(this::convertHinhAnhMauSacToDTO)
                .collect(Collectors.toList());
        responseDTO.setHinhAnhMauSacs(hinhAnhMauSacDTOs);

        return responseDTO;
    }

    private BienTheResponseDTO convertBienTheToDTO(BienTheSanPham bienThe) {
        BienTheResponseDTO dto = new BienTheResponseDTO();
        dto.setId(bienThe.getId());
        dto.setMaSKU(bienThe.getMaSKU());
        dto.setMauSac(bienThe.getMauSac());
        dto.setKichCo(bienThe.getKichCo());
        dto.setGia(bienThe.getGia());
        dto.setGiaGoc(bienThe.getGiaGoc());
        dto.setSoLuongTon(bienThe.getSoLuongTon());
        dto.setKhoiLuongGram(bienThe.getKhoiLuongGram());
        dto.setTrangThai(bienThe.getTrangThai());
        return dto;
    }

    private HinhAnhSanPhamDTO convertHinhAnhSanPhamToDTO(HinhAnhSanPham hinhAnh) {
        HinhAnhSanPhamDTO dto = new HinhAnhSanPhamDTO();
        dto.setDuongDanAnh(hinhAnh.getDuongDanAnh());
        dto.setLaAnhChinh(hinhAnh.getLaAnhChinh());
        dto.setThuTu(hinhAnh.getThuTu());
        return dto;
    }

    private HinhAnhMauSacDTO convertHinhAnhMauSacToDTO(HinhAnhMauSac hinhAnhMauSac) {
        HinhAnhMauSacDTO dto = new HinhAnhMauSacDTO();
        dto.setMauSac(hinhAnhMauSac.getMauSac());
        dto.setDuongDanAnh(hinhAnhMauSac.getDuongDanAnh());
        return dto;
    }

    /**
     * Lấy chi tiết sản phẩm theo ID
     */
    @Transactional(readOnly = true)
    public SanPhamResponseDTO getSanPhamById(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // Load lazy collections
        sanPham.getBienThes().size();
        sanPham.getHinhAnhSanPhams().size();
        sanPham.getHinhAnhMauSacs().size();

        return convertToResponseDTO(sanPham);
    }

    /**
     * Lấy danh sách tất cả sản phẩm
     */
    @Transactional(readOnly = true)
    public List<SanPhamResponseDTO> getAllSanPham() {
        return searchSanPham(null, null, null);
    }

    /**
     * Tìm kiếm và lọc sản phẩm (Admin)
     */
    @Transactional(readOnly = true)
    public List<SanPhamResponseDTO> searchSanPham(String keyword, Integer danhMucId, String trangThai) {
        List<SanPham> sanPhams = sanPhamRepository.searchAdmin(
                (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null,
                danhMucId,
                (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai.trim() : null
        );

        return sanPhams.stream()
                .filter(sp -> sp.getDaXoa() == null || !sp.getDaXoa())
                .map(sp -> {
                    // Load lazy collections
                    sp.getBienThes().size();
                    sp.getHinhAnhSanPhams().size();
                    sp.getHinhAnhMauSacs().size();
                    return convertToResponseDTO(sp);
                })
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm và lọc sản phẩm có phân trang (Admin)
     */
    @Transactional(readOnly = true)
    public Page<SanPhamResponseDTO> searchSanPhamPaged(String keyword, Integer danhMucId, String trangThai, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SanPham> sanPhamPage = sanPhamRepository.searchAdminPage(
                (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null,
                danhMucId,
                (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai.trim() : null,
                pageable
        );

        return sanPhamPage.map(sp -> {
            sp.getBienThes().size();
            sp.getHinhAnhSanPhams().size();
            sp.getHinhAnhMauSacs().size();
            return convertToResponseDTO(sp);
        });
    }

    /**
     * Cập nhật sản phẩm
     */
    @Transactional
    public SanPhamResponseDTO updateSanPham(SanPhamRequestDTO requestDTO) {
        log.info("Updating product ID: {}", requestDTO.getId());
        
        SanPham sanPham = sanPhamRepository.findById(requestDTO.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + requestDTO.getId()));

        // 1. Cập nhật thông tin cơ bản
        sanPham.setTen(requestDTO.getTen());
        sanPham.setMoTaNgan(requestDTO.getMoTaNgan());
        sanPham.setMoTa(requestDTO.getMoTa());
        sanPham.setChatLieu(requestDTO.getChatLieu());
        sanPham.setGioiTinh(requestDTO.getGioiTinh());

        if (requestDTO.getDanhMucId() != null) {
            DanhMuc danhMuc = danhMucRepository.findById(requestDTO.getDanhMucId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại: " + requestDTO.getDanhMucId()));
            sanPham.setDanhMuc(danhMuc);
        }

        // 2. Cập nhật biến thể
        // Đối với sự đơn giản, tôi sẽ xóa các biến thể cũ không có trong request và cập nhật/thêm mới
        // Tuy nhiên, để tránh làm hỏng dữ liệu đơn hàng cũ, tốt hơn là chỉ INACTIVE biến thể cũ hoặc update
        
        // Ở đây tôi sẽ dùng cách đơn giản: Xóa hết và thêm lại (chỉ khi chưa có đơn hàng liên quan)
        // Nhưng vì bài toán yêu cầu chỉnh sửa, tôi sẽ làm cách an toàn hơn: 
        // - Duyệt qua list bienThes trong request:
        //   - Nếu có ID -> update
        //   - Nếu không có ID -> thêm mới
        // - Những biến thể nào có trong DB mà không có trong request -> Xóa (hoặc set daXoa)

        List<BienTheSanPham> currentBienThes = sanPham.getBienThes();
        List<Integer> requestIds = requestDTO.getBienThes().stream()
                .map(BienTheRequestDTO::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        // Xóa những biến thể không còn trong request
        currentBienThes.removeIf(bt -> !requestIds.contains(bt.getId()));

        for (BienTheRequestDTO btDto : requestDTO.getBienThes()) {
            if (btDto.getId() != null) {
                // Update
                BienTheSanPham bt = currentBienThes.stream()
                        .filter(b -> b.getId().equals(btDto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại: " + btDto.getId()));
                bt.setMaSKU(btDto.getMaSKU());
                bt.setMauSac(btDto.getMauSac());
                bt.setKichCo(btDto.getKichCo());
                bt.setGia(btDto.getGia());
                bt.setGiaGoc(btDto.getGiaGoc());
                bt.setSoLuongTon(btDto.getSoLuongTon());
                bt.setKhoiLuongGram(btDto.getKhoiLuongGram());
            } else {
                // Add new
                BienTheSanPham newBt = new BienTheSanPham();
                newBt.setSanPham(sanPham);
                newBt.setMaSKU(btDto.getMaSKU());
                newBt.setMauSac(btDto.getMauSac());
                newBt.setKichCo(btDto.getKichCo());
                newBt.setGia(btDto.getGia());
                newBt.setGiaGoc(btDto.getGiaGoc());
                newBt.setSoLuongTon(btDto.getSoLuongTon());
                newBt.setKhoiLuongGram(btDto.getKhoiLuongGram());
                newBt.setTrangThai("ACTIVE");
                currentBienThes.add(newBt);
            }
        }

        // 3. Cập nhật hình ảnh (gallery)
        // Xóa cũ thêm mới cho nhanh (vì là danh sách ảnh)
        hinhAnhSanPhamRepository.deleteBySanPham(sanPham);
        hinhAnhSanPhamRepository.flush(); // Đảm bảo xóa xong trước khi thêm mới
        sanPham.getHinhAnhSanPhams().clear();
        if (requestDTO.getHinhAnhSanPhams() != null) {
            List<HinhAnhSanPham> newHinhAnhs = createHinhAnhSanPhams(sanPham, requestDTO.getHinhAnhSanPhams());
            sanPham.getHinhAnhSanPhams().addAll(newHinhAnhs);
        }

        // 4. Cập nhật hình ảnh theo màu
        hinhAnhMauSacRepository.deleteBySanPham(sanPham);
        hinhAnhMauSacRepository.flush(); // Đảm bảo xóa xong trước khi thêm mới
        sanPham.getHinhAnhMauSacs().clear();
        if (requestDTO.getHinhAnhMauSacs() != null) {
            List<HinhAnhMauSac> newHinhAnhMauSacs = createHinhAnhMauSacs(sanPham, requestDTO.getHinhAnhMauSacs());
            sanPham.getHinhAnhMauSacs().addAll(newHinhAnhMauSacs);
        }

        sanPham.setNgayCapNhat(Instant.now());
        sanPham = sanPhamRepository.save(sanPham);
        
        return convertToResponseDTO(sanPham);
    }

    /**
     * Xóa mềm sản phẩm
     */
    @Transactional
    public void deleteSanPham(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        sanPham.setDaXoa(true);
        sanPham.setTrangThai("INACTIVE");
        sanPhamRepository.save(sanPham);
    }
    @Transactional
    public void addStockForVariant(Integer sanPhamId, Integer bienTheId, Integer soLuongThem) {
        addStockForVariant(sanPhamId, bienTheId, soLuongThem, null, null);
    }

    @Transactional
    public void addStockForVariant(Integer sanPhamId,
                                   Integer bienTheId,
                                   Integer soLuongThem,
                                   Integer nguoiNhapId,
                                   String ghiChu) {
        adjustStockForVariant(sanPhamId, bienTheId, soLuongThem, "NHAP", nguoiNhapId, ghiChu);
    }

    @Transactional
    public void adjustStockForVariant(Integer sanPhamId,
                                      Integer bienTheId,
                                      Integer soLuong,
                                      String actionType,
                                      Integer nguoiThucHienId,
                                      String ghiChu) {
        if (soLuong == null || soLuong <= 0) {
            throw new RuntimeException("So luong phai lon hon 0.");
        }

        if (actionType == null || (!"NHAP".equals(actionType) && !"TRU_LOI_VAN_CHUYEN".equals(actionType) && !"XUAT".equals(actionType))) {
            throw new RuntimeException("Loai giao dich khong hop le.");
        }

        BienTheSanPham bienThe = bienTheRepository.findById(bienTheId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay bien the san pham."));

        if (bienThe.getSanPham() == null
                || bienThe.getSanPham().getId() == null
                || !bienThe.getSanPham().getId().equals(sanPhamId)) {
            throw new RuntimeException("Bien the khong thuoc san pham nay.");
        }

        int current = bienThe.getSoLuongTon() != null ? bienThe.getSoLuongTon() : 0;
        int newStock;

        boolean isImport = "NHAP".equals(actionType);
        if (isImport) {
            newStock = current + soLuong;
        } else {
            if (current < soLuong) {
                throw new RuntimeException("Ton kho khong du de tru. Hien tai chi con " + current + ".");
            }
            newStock = current - soLuong;
            if (ghiChu == null || ghiChu.isBlank()) {
                ghiChu = "Tru ton kho do loi van chuyen";
            }
        }

        bienThe.setSoLuongTon(newStock);
        bienTheRepository.save(bienThe);

        GiaoDichTonKho giaoDich = new GiaoDichTonKho();
        giaoDich.setBienTheSanPham(bienThe);
        giaoDich.setLoai(isImport ? "NHAP" : "XUAT");
        giaoDich.setSoLuong(soLuong);
        giaoDich.setThamChieuLoai("TAI_KHOAN");
        giaoDich.setThamChieuId(nguoiThucHienId);
        giaoDich.setGhiChu(ghiChu);
        giaoDich.setNgayTao(Instant.now());
        giaoDichTonKhoRepository.save(giaoDich);
    }

    @Transactional(readOnly = true)
        public Page<InventoryVariantDTO> getInventoryVariants(String keyword, int page, int size) {
        String normalizedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("sanPham.ten").ascending().and(Sort.by("mauSac").ascending()).and(Sort.by("kichCo").ascending()));
        Page<BienTheSanPham> variants = bienTheRepository.searchInventoryVariants(normalizedKeyword, pageable);
        java.util.Map<Integer, String> imageCache = new java.util.HashMap<>();

        return variants.map(v -> {
                Integer productId = v.getSanPham().getId();
                String image = imageCache.computeIfAbsent(productId,
                    id -> hinhAnhSanPhamRepository.findFirstBySanPham_IdOrderByLaAnhChinhDescThuTuAscIdAsc(id)
                        .map(HinhAnhSanPham::getDuongDanAnh)
                        .orElse("/images/no-image.png"));

                return InventoryVariantDTO.builder()
                    .sanPhamId(productId)
                    .bienTheId(v.getId())
                    .tenSanPham(v.getSanPham().getTen())
                    .maSKU(v.getMaSKU())
                    .mauSac(v.getMauSac())
                    .kichCo(v.getKichCo())
                    .soLuongTon(v.getSoLuongTon() != null ? v.getSoLuongTon() : 0)
                    .hinhAnh(image)
                    .build();
            });
    }

    @Transactional(readOnly = true)
    public List<InventoryLogDTO> getRecentInventoryLogs() {
        return giaoDichTonKhoRepository.findTop50ByOrderByNgayTaoDesc().stream()
                .map(gd -> {
                    BienTheSanPham bt = gd.getBienTheSanPham();
                    String nguoiThucHien;
                    if (gd.getThamChieuId() != null) {
                        nguoiThucHien = taiKhoanRepository.findById(gd.getThamChieuId())
                                .map(tk -> tk.getHoTen() != null && !tk.getHoTen().isBlank()
                                        ? tk.getHoTen() + " (@" + tk.getTenDangNhap() + ")"
                                        : tk.getTenDangNhap())
                                .orElse("Unknown");
                    } else {
                        nguoiThucHien = "Unknown";
                    }

                    return InventoryLogDTO.builder()
                            .tenSanPham(bt != null && bt.getSanPham() != null ? bt.getSanPham().getTen() : "-")
                            .maSKU(bt != null ? bt.getMaSKU() : "-")
                            .mauSac(bt != null ? bt.getMauSac() : "-")
                            .kichCo(bt != null ? bt.getKichCo() : "-")
                            .loai(gd.getLoai())
                            .soLuong(gd.getSoLuong())
                            .nguoiThucHien(nguoiThucHien)
                            .ghiChu(gd.getGhiChu())
                            .ngayTao(gd.getNgayTao())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NhapKhoHistoryDTO> getNhapKhoHistoryByProduct(Integer sanPhamId) {
        return giaoDichTonKhoRepository
                .findByBienTheSanPham_SanPham_IdOrderByNgayTaoDesc(sanPhamId)
                .stream()
                .map(gd -> {
                    BienTheSanPham bt = gd.getBienTheSanPham();
                    String nguoiNhap;
                    if (gd.getThamChieuId() != null) {
                        nguoiNhap = taiKhoanRepository.findById(gd.getThamChieuId())
                                .map(tk -> tk.getHoTen() != null && !tk.getHoTen().isBlank()
                                        ? tk.getHoTen() + " (@" + tk.getTenDangNhap() + ")"
                                        : tk.getTenDangNhap())
                                .orElse("Unknown");
                    } else {
                        nguoiNhap = "Unknown";
                    }

                    return NhapKhoHistoryDTO.builder()
                            .giaoDichId(gd.getId())
                            .bienTheId(bt != null ? bt.getId() : null)
                            .maSKU(bt != null ? bt.getMaSKU() : null)
                            .mauSac(bt != null ? bt.getMauSac() : null)
                            .kichCo(bt != null ? bt.getKichCo() : null)
                            .soLuongNhap(gd.getSoLuong())
                            .loaiGiaoDich(gd.getLoai())
                            .nguoiNhap(nguoiNhap)
                            .ghiChu(gd.getGhiChu())
                            .ngayTao(gd.getNgayTao())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BienTheSanPham getBienTheByIdAndSanPhamId(Integer sanPhamId, Integer bienTheId) {
        BienTheSanPham bienThe = bienTheRepository.findById(bienTheId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay bien the san pham."));

        if (bienThe.getSanPham() == null
                || bienThe.getSanPham().getId() == null
                || !bienThe.getSanPham().getId().equals(sanPhamId)) {
            throw new RuntimeException("Bien the khong thuoc san pham nay.");
        }
        return bienThe;
    }

    @Transactional(readOnly = true)
    public Integer getFirstBienTheIdBySanPham(Integer sanPhamId) {
        return bienTheRepository.findBySanPham_Id(sanPhamId).stream()
                .findFirst()
                .map(BienTheSanPham::getId)
                .orElseThrow(() -> new RuntimeException("San pham chua co bien the."));
    }

    @Transactional(readOnly = true)
    public List<NhapKhoHistoryDTO> getNhapKhoHistoryByVariant(Integer bienTheId) {
        return giaoDichTonKhoRepository
                .findByBienTheSanPham_IdOrderByNgayTaoDesc(bienTheId)
                .stream()
                .map(gd -> {
                    BienTheSanPham bt = gd.getBienTheSanPham();
                    String nguoiNhap;
                    if (gd.getThamChieuId() != null) {
                        nguoiNhap = taiKhoanRepository.findById(gd.getThamChieuId())
                                .map(tk -> tk.getHoTen() != null && !tk.getHoTen().isBlank()
                                        ? tk.getHoTen() + " (@" + tk.getTenDangNhap() + ")"
                                        : tk.getTenDangNhap())
                                .orElse("Unknown");
                    } else {
                        nguoiNhap = "Unknown";
                    }

                    return NhapKhoHistoryDTO.builder()
                            .giaoDichId(gd.getId())
                            .bienTheId(bt != null ? bt.getId() : null)
                            .maSKU(bt != null ? bt.getMaSKU() : null)
                            .mauSac(bt != null ? bt.getMauSac() : null)
                            .kichCo(bt != null ? bt.getKichCo() : null)
                            .soLuongNhap(gd.getSoLuong())
                            .loaiGiaoDich(gd.getLoai())
                            .nguoiNhap(nguoiNhap)
                            .ghiChu(gd.getGhiChu())
                            .ngayTao(gd.getNgayTao())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void addGalleryImage(Integer sanPhamId, String duongDanAnh) {
        if (duongDanAnh == null || duongDanAnh.trim().isEmpty()) {
            throw new RuntimeException("Duong dan anh khong hop le.");
        }

        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay san pham voi ID: " + sanPhamId));

        List<HinhAnhSanPham> currentImages = hinhAnhSanPhamRepository.findBySanPham_IdOrderByLaAnhChinhDescThuTuAscIdAsc(sanPhamId);

        boolean duplicated = currentImages.stream()
                .anyMatch(img -> duongDanAnh.equals(img.getDuongDanAnh()));
        if (duplicated) {
            return;
        }

        int nextOrder = currentImages.stream()
                .map(HinhAnhSanPham::getThuTu)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        HinhAnhSanPham image = new HinhAnhSanPham();
        image.setSanPham(sanPham);
        image.setDuongDanAnh(duongDanAnh);
        image.setLaAnhChinh(currentImages.isEmpty());
        image.setThuTu(nextOrder);
        image.setNgayTao(Instant.now());
        hinhAnhSanPhamRepository.save(image);
    }
}
