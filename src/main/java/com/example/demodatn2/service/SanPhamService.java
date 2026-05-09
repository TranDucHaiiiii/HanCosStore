package com.example.demodatn2.service;

import com.example.demodatn2.dto.BienTheRequestDTO;
import com.example.demodatn2.dto.BienTheResponseDTO;
import com.example.demodatn2.dto.GenerateQuickVariantsRequest;
import com.example.demodatn2.dto.HinhAnhMauSacDTO;
import com.example.demodatn2.dto.HinhAnhSanPhamDTO;
import com.example.demodatn2.dto.InventoryLogDTO;
import com.example.demodatn2.dto.InventoryVariantDTO;
import com.example.demodatn2.dto.NhapKhoHistoryDTO;
import com.example.demodatn2.dto.SanPhamRequestDTO;
import com.example.demodatn2.dto.SanPhamResponseDTO;
import com.example.demodatn2.entity.BienTheSanPham;
import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.entity.GiaoDichTonKho;
import com.example.demodatn2.entity.HinhAnhMauSac;
import com.example.demodatn2.entity.HinhAnhSanPham;
import com.example.demodatn2.entity.SanPham;
import com.example.demodatn2.repository.BienTheSanPhamRepository;
import com.example.demodatn2.repository.DanhMucRepository;
import com.example.demodatn2.repository.GiaoDichTonKhoRepository;
import com.example.demodatn2.repository.HinhAnhMauSacRepository;
import com.example.demodatn2.repository.HinhAnhSanPhamRepository;
import com.example.demodatn2.repository.SanPhamRepository;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.util.ProductScopeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final BienTheSanPhamRepository bienTheRepository;
    private final GiaoDichTonKhoRepository giaoDichTonKhoRepository;
    private final HinhAnhSanPhamRepository hinhAnhSanPhamRepository;
    private final HinhAnhMauSacRepository hinhAnhMauSacRepository;
    private final DanhMucRepository danhMucRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    @Transactional
    public SanPhamResponseDTO createSanPham(SanPhamRequestDTO requestDTO) {
        log.info("Creating new product: {}", requestDTO.getTen());

        // Auto-generate product code if not provided
        if (requestDTO.getMaSanPham() == null || requestDTO.getMaSanPham().trim().isEmpty()) {
            requestDTO.setMaSanPham(generateProductCode(requestDTO.getTen()));
        }

        validateSanPhamRequest(requestDTO);

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

        if (requestDTO.getDanhMucId() != null) {
            DanhMuc danhMuc = danhMucRepository.findById(requestDTO.getDanhMucId())
                    .orElseThrow(() -> new RuntimeException("Danh muc khong ton tai: " + requestDTO.getDanhMucId()));
            validateCategoryScope(danhMuc);
            sanPham.setDanhMuc(danhMuc);
        }

        sanPham = sanPhamRepository.save(sanPham);
        log.info("Product saved with ID: {}", sanPham.getId());

        if (requestDTO.getBienThes() != null && !requestDTO.getBienThes().isEmpty()) {
            List<BienTheSanPham> bienThes = createBienThes(sanPham, requestDTO.getBienThes());
            sanPham.getBienThes().addAll(bienThes);
            log.info("Created {} variants", bienThes.size());
        }

        if (requestDTO.getHinhAnhSanPhams() != null && !requestDTO.getHinhAnhSanPhams().isEmpty()) {
            List<HinhAnhSanPham> hinhAnhs = createHinhAnhSanPhams(sanPham, requestDTO.getHinhAnhSanPhams());
            sanPham.getHinhAnhSanPhams().addAll(hinhAnhs);
            log.info("Created {} product images", hinhAnhs.size());
        }

        if (requestDTO.getHinhAnhMauSacs() != null && !requestDTO.getHinhAnhMauSacs().isEmpty()) {
            List<HinhAnhMauSac> hinhAnhMauSacs = createHinhAnhMauSacs(sanPham, requestDTO.getHinhAnhMauSacs());
            sanPham.getHinhAnhMauSacs().addAll(hinhAnhMauSacs);
            log.info("Created {} color images", hinhAnhMauSacs.size());
        }

        sanPham = sanPhamRepository.save(sanPham);
        return convertToResponseDTO(sanPham);
    }

    /**
     * Auto-generate unique product code with format SP-<timestamp>
     */
    public String generateProductCode(String productName) {
        String normalized = normalizeVietnameseText(productName == null ? "" : productName)
                .toUpperCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^A-Z0-9]", "");
        String prefix = normalized.isEmpty() ? "SP" : normalized.substring(0, Math.min(6, normalized.length()));
        String baseCode = prefix + "_" + String.valueOf(System.currentTimeMillis()).substring(7);
        String code = baseCode;
        int counter = 0;
        
        // Ensure uniqueness
        while (sanPhamRepository.findByMaSanPham(code).isPresent()) {
            counter++;
            code = baseCode + "-" + counter;
        }
        
        log.info("Generated product code: {}", code);
        return code;
    }

    private void validateSanPhamRequest(SanPhamRequestDTO requestDTO) {
        if (sanPhamRepository.findByMaSanPham(requestDTO.getMaSanPham()).isPresent()) {
            throw new RuntimeException("Ma san pham da ton tai: " + requestDTO.getMaSanPham());
        }

        if (requestDTO.getTen() == null || requestDTO.getTen().trim().isEmpty()) {
            throw new RuntimeException("Ten san pham khong duoc de trong");
        }

        if (requestDTO.getBienThes() == null || requestDTO.getBienThes().isEmpty()) {
            throw new RuntimeException("San pham phai co it nhat 1 bien the");
        }

        List<String> maSKUs = requestDTO.getBienThes().stream()
                .map(BienTheRequestDTO::getMaSKU)
                .collect(Collectors.toList());

        long distinctCount = maSKUs.stream().distinct().count();
        if (distinctCount != maSKUs.size()) {
            throw new RuntimeException("Co ma SKU bi trung lap trong danh sach bien the");
        }

        for (String maSKU : maSKUs) {
            if (bienTheRepository.findByMaSKU(maSKU).isPresent()) {
                throw new RuntimeException("Ma SKU da ton tai: " + maSKU);
            }
        }
    }

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

        long countAnhChinh = validDTOs.stream()
                .filter(dto -> dto.getLaAnhChinh() != null && dto.getLaAnhChinh())
                .count();

        if (countAnhChinh == 0) {
            validDTOs.get(0).setLaAnhChinh(true);
        } else if (countAnhChinh > 1) {
            throw new RuntimeException("Chi duoc co 1 anh chinh");
        }

        List<HinhAnhSanPham> hinhAnhs = new ArrayList<>();
        for (HinhAnhSanPhamDTO dto : validDTOs) {
            HinhAnhSanPham hinhAnh = new HinhAnhSanPham();
            hinhAnh.setSanPham(sanPham);
            hinhAnh.setDuongDanAnh(dto.getDuongDanAnh().trim());
            hinhAnh.setLaAnhChinh(dto.getLaAnhChinh() != null ? dto.getLaAnhChinh() : false);
            hinhAnh.setThuTu(dto.getThuTu() != null ? dto.getThuTu() : 0);
            hinhAnh.setNgayTao(Instant.now());
            hinhAnhs.add(hinhAnh);
        }

        return hinhAnhs;
    }

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
            hinhAnhMauSacs.add(hinhAnhMauSac);
        }

        return hinhAnhMauSacs;
    }

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

        if (sanPham.getDanhMuc() != null) {
            responseDTO.setDanhMucId(sanPham.getDanhMuc().getId());
            responseDTO.setTenDanhMuc(sanPham.getDanhMuc().getTen());
            if (sanPham.getDanhMuc().getDanhMucCha() != null) {
                responseDTO.setParentDanhMucId(sanPham.getDanhMuc().getDanhMucCha().getId());
                responseDTO.setTenParentDanhMuc(sanPham.getDanhMuc().getDanhMucCha().getTen());
            }
        }

        List<BienTheResponseDTO> bienTheDTOs = sanPham.getBienThes().stream()
                .map(this::convertBienTheToDTO)
                .collect(Collectors.toList());
        responseDTO.setBienThes(bienTheDTOs);

        int tongTon = bienTheDTOs.stream()
                .mapToInt(bt -> bt.getSoLuongTon() != null ? bt.getSoLuongTon() : 0)
                .sum();
        responseDTO.setTongSoLuongTon(tongTon);

        List<HinhAnhSanPhamDTO> hinhAnhDTOs = sanPham.getHinhAnhSanPhams().stream()
                .map(this::convertHinhAnhSanPhamToDTO)
                .collect(Collectors.toList());
        responseDTO.setHinhAnhSanPhams(hinhAnhDTOs);

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

    @Transactional(readOnly = true)
    public SanPhamResponseDTO getSanPhamById(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay san pham voi ID: " + id));

        if (!isAllowedProduct(sanPham)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "San pham khong ton tai");
        }

        sanPham.getBienThes().size();
        sanPham.getHinhAnhSanPhams().size();
        sanPham.getHinhAnhMauSacs().size();

        return convertToResponseDTO(sanPham);
    }

    @Transactional(readOnly = true)
    public List<SanPhamResponseDTO> getAllSanPham() {
        return searchSanPham(null, null, null);
    }

    @Transactional(readOnly = true)
    public List<SanPhamResponseDTO> searchSanPham(String keyword, Integer danhMucId, String trangThai) {
        List<SanPham> sanPhams = sanPhamRepository.searchAdmin(
                (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null,
                danhMucId,
                (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai.trim() : null
        );

        return sanPhams.stream()
                .filter(sp -> sp.getDaXoa() == null || !sp.getDaXoa())
                .filter(this::isAllowedProduct)
                .map(sp -> {
                    sp.getBienThes().size();
                    sp.getHinhAnhSanPhams().size();
                    sp.getHinhAnhMauSacs().size();
                    return convertToResponseDTO(sp);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SanPhamResponseDTO> searchSanPhamPaged(String keyword, Integer danhMucId, String trangThai, String sortBy, int page, int size) {
        List<SanPhamResponseDTO> allResults = searchSanPham(keyword, danhMucId, trangThai);
        
        // Sort theo ngÃ y táº¡o
        if ("oldest".equalsIgnoreCase(sortBy)) {
            allResults.sort((a, b) -> {
                Instant timeA = a.getNgayTao() != null ? a.getNgayTao() : Instant.EPOCH;
                Instant timeB = b.getNgayTao() != null ? b.getNgayTao() : Instant.EPOCH;
                return timeA.compareTo(timeB);
            });
        } else {
            // Default: newest first
            allResults.sort((a, b) -> {
                Instant timeA = a.getNgayTao() != null ? a.getNgayTao() : Instant.EPOCH;
                Instant timeB = b.getNgayTao() != null ? b.getNgayTao() : Instant.EPOCH;
                return timeB.compareTo(timeA);
            });
        }
        
        return paginate(allResults, page, size);
    }

    @Transactional
    public SanPhamResponseDTO updateSanPham(SanPhamRequestDTO requestDTO) {
        log.info("Updating product ID: {}", requestDTO.getId());

        SanPham sanPham = sanPhamRepository.findById(requestDTO.getId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay san pham voi ID: " + requestDTO.getId()));

        sanPham.setTen(requestDTO.getTen());
        sanPham.setMoTaNgan(requestDTO.getMoTaNgan());
        sanPham.setMoTa(requestDTO.getMoTa());
        sanPham.setChatLieu(requestDTO.getChatLieu());
        sanPham.setGioiTinh(requestDTO.getGioiTinh());

        if (requestDTO.getDanhMucId() != null) {
            DanhMuc danhMuc = danhMucRepository.findById(requestDTO.getDanhMucId())
                    .orElseThrow(() -> new RuntimeException("Danh muc khong ton tai: " + requestDTO.getDanhMucId()));
            validateCategoryScope(danhMuc);
            sanPham.setDanhMuc(danhMuc);
        }

        List<BienTheSanPham> currentBienThes = sanPham.getBienThes();
        List<Integer> requestIds = requestDTO.getBienThes().stream()
                .map(BienTheRequestDTO::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        currentBienThes.removeIf(bt -> !requestIds.contains(bt.getId()));

        for (BienTheRequestDTO btDto : requestDTO.getBienThes()) {
            if (btDto.getId() != null) {
                BienTheSanPham bt = currentBienThes.stream()
                        .filter(b -> b.getId().equals(btDto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Bien the khong ton tai: " + btDto.getId()));
                bt.setMaSKU(btDto.getMaSKU());
                bt.setMauSac(btDto.getMauSac());
                bt.setKichCo(btDto.getKichCo());
                bt.setGia(btDto.getGia());
                bt.setGiaGoc(btDto.getGiaGoc());
                bt.setSoLuongTon(btDto.getSoLuongTon());
                bt.setKhoiLuongGram(btDto.getKhoiLuongGram());
            } else {
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

        hinhAnhSanPhamRepository.deleteBySanPham(sanPham);
        hinhAnhSanPhamRepository.flush();
        sanPham.getHinhAnhSanPhams().clear();
        if (requestDTO.getHinhAnhSanPhams() != null) {
            List<HinhAnhSanPham> newHinhAnhs = createHinhAnhSanPhams(sanPham, requestDTO.getHinhAnhSanPhams());
            sanPham.getHinhAnhSanPhams().addAll(newHinhAnhs);
        }

        hinhAnhMauSacRepository.deleteBySanPham(sanPham);
        hinhAnhMauSacRepository.flush();
        sanPham.getHinhAnhMauSacs().clear();
        if (requestDTO.getHinhAnhMauSacs() != null) {
            List<HinhAnhMauSac> newHinhAnhMauSacs = createHinhAnhMauSacs(sanPham, requestDTO.getHinhAnhMauSacs());
            sanPham.getHinhAnhMauSacs().addAll(newHinhAnhMauSacs);
        }

        sanPham.setNgayCapNhat(Instant.now());
        sanPham = sanPhamRepository.save(sanPham);
        return convertToResponseDTO(sanPham);
    }

    @Transactional
    public void deleteSanPham(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay san pham voi ID: " + id));
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
        List<BienTheSanPham> variants = bienTheRepository.searchInventoryVariants(normalizedKeyword, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(v -> isAllowedProduct(v.getSanPham()))
                .sorted(Comparator
                        .comparing((BienTheSanPham v) -> v.getSanPham().getTen(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(v -> v.getMauSac() != null ? v.getMauSac() : "", String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(v -> v.getKichCo() != null ? v.getKichCo() : "", String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        java.util.Map<Integer, String> imageCache = new java.util.HashMap<>();
        List<InventoryVariantDTO> mapped = variants.stream().map(v -> {
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
        }).collect(Collectors.toList());

        return paginate(mapped, page, size);
    }

    @Transactional(readOnly = true)
    public List<InventoryLogDTO> getRecentInventoryLogs() {
        return giaoDichTonKhoRepository.findTop50ByOrderByNgayTaoDesc().stream()
                .filter(gd -> gd.getBienTheSanPham() != null && isAllowedProduct(gd.getBienTheSanPham().getSanPham()))
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
                            .tenSanPham(bt.getSanPham() != null ? bt.getSanPham().getTen() : "-")
                            .maSKU(bt.getMaSKU() != null ? bt.getMaSKU() : "-")
                            .mauSac(bt.getMauSac() != null ? bt.getMauSac() : "-")
                            .kichCo(bt.getKichCo() != null ? bt.getKichCo() : "-")
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
                .filter(gd -> gd.getBienTheSanPham() != null && isAllowedProduct(gd.getBienTheSanPham().getSanPham()))
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

        if (!isAllowedProduct(bienThe.getSanPham())) {
            throw new RuntimeException("San pham ngoai pham vi website.");
        }

        return bienThe;
    }

    @Transactional(readOnly = true)
    public Integer getFirstBienTheIdBySanPham(Integer sanPhamId) {
        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay san pham."));
        if (!isAllowedProduct(sanPham)) {
            throw new RuntimeException("San pham ngoai pham vi website.");
        }

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
                .filter(gd -> gd.getBienTheSanPham() != null && isAllowedProduct(gd.getBienTheSanPham().getSanPham()))
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

        if (!isAllowedProduct(sanPham)) {
            throw new RuntimeException("San pham ngoai pham vi website.");
        }

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

    /**
     * Generate list of BienTheRequestDTO from colors, sizes, and default values
     * Used by API to create multiple variants quickly
     */
    public List<BienTheRequestDTO> generateQuickVariants(GenerateQuickVariantsRequest request) {
        if (request.getColors() == null || request.getColors().isEmpty()) {
            throw new RuntimeException("Vui lÃ²ng chá»n Ã­t nháº¥t 1 mÃ u sáº¯c");
        }
        if (request.getSizes() == null || request.getSizes().isEmpty()) {
            throw new RuntimeException("Vui lÃ²ng chá»n Ã­t nháº¥t 1 kÃ­ch cá»¡");
        }

        List<BienTheRequestDTO> variants = new ArrayList<>();
        Set<String> skuSet = new HashSet<>();
        String maSP = request.getMaSanPham() != null ? request.getMaSanPham().trim() : "";

        BigDecimal price = request.getPrice() != null ? BigDecimal.valueOf(request.getPrice()) : BigDecimal.ZERO;
        Integer stock = request.getStock() != null ? request.getStock() : 0;
        Integer weight = request.getWeight() != null && request.getWeight() > 0 ? request.getWeight() : 300;

        for (String color : request.getColors()) {
            for (String size : request.getSizes()) {
                BienTheRequestDTO variant = new BienTheRequestDTO();
                
                // Generate SKU
                String sku;
                if (!maSP.isEmpty()) {
                    sku = String.format("%s_%s_%s",
                            toSkuToken(maSP),
                            toSkuToken(color),
                            toSkuToken(size));
                } else {
                    sku = String.format("%s_%s",
                            toSkuToken(color),
                            toSkuToken(size));
                }

                // Check uniqueness
                if (skuSet.contains(sku)) {
                    continue;
                }
                skuSet.add(sku);

                variant.setMaSKU(sku);
                variant.setMauSac(color);
                variant.setKichCo(size);
                variant.setGia(price);
                variant.setSoLuongTon(stock);
                variant.setKhoiLuongGram(weight);

                variants.add(variant);
            }
        }

        if (variants.isEmpty()) {
            throw new RuntimeException("KhÃ´ng thá»ƒ sinh biáº¿n thá»ƒ vá»›i cÃ¡c tham sá»‘ Ä‘Æ°á»£c cung cáº¥p");
        }

        log.info("Generated {} variants", variants.size());
        return variants;
    }

    /**
     * Validate base product fields from FE before submit
     */
    public void validateBaseInfoForCreate(String maSanPham, String ten, Integer danhMucId) {
        String code = maSanPham != null ? maSanPham.trim() : "";
        String productName = ten != null ? ten.trim() : "";

        if (productName.isEmpty()) {
            throw new RuntimeException("Ten san pham khong duoc de trong");
        }
        if (danhMucId == null) {
            throw new RuntimeException("Vui long chon danh muc.");
        }
        if (!code.isEmpty() && sanPhamRepository.findByMaSanPham(code).isPresent()) {
            throw new RuntimeException("Ma san pham da ton tai: " + code);
        }
    }

    /**
     * Generate SKU preview from base fields
     */
    public String generateSkuPreview(String maSanPham, String mauSac, String kichCo) {
        String color = mauSac != null ? mauSac.trim() : "";
        String size = kichCo != null ? kichCo.trim() : "";
        String productCode = maSanPham != null ? maSanPham.trim() : "";

        if (color.isEmpty() || size.isEmpty()) {
            throw new RuntimeException("Mau sac va kich co khong duoc de trong.");
        }

        if (!productCode.isEmpty()) {
            return String.format("%s_%s_%s", toSkuToken(productCode), toSkuToken(color), toSkuToken(size));
        }
        return String.format("%s_%s", toSkuToken(color), toSkuToken(size));
    }

    /**
     * Validate variant payload from FE before submit
     */
    public void validateVariantRequests(List<BienTheRequestDTO> variants) {
        if (variants == null || variants.isEmpty()) {
            throw new RuntimeException("Vui long them it nhat 1 bien the.");
        }

        Set<String> comboSet = new HashSet<>();
        for (BienTheRequestDTO variant : variants) {
            String color = variant.getMauSac() != null ? variant.getMauSac().trim() : "";
            String size = variant.getKichCo() != null ? variant.getKichCo().trim() : "";
            String sku = variant.getMaSKU() != null ? variant.getMaSKU().trim() : "";

            if (color.isEmpty() || size.isEmpty()) {
                throw new RuntimeException("Vui long chon day du mau sac va kich co cho bien the.");
            }
            if (sku.isEmpty()) {
                throw new RuntimeException("Vui long dam bao ma SKU duoc tao cho bien the.");
            }
            if (variant.getSoLuongTon() == null || variant.getSoLuongTon() < 0) {
                throw new RuntimeException("So luong ton phai la so >= 0.");
            }
            if (variant.getGia() == null || variant.getGia().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Gia ban phai la so >= 0.");
            }
            if (variant.getKhoiLuongGram() == null || variant.getKhoiLuongGram() <= 0) {
                throw new RuntimeException("Khoi luong phai la so > 0.");
            }

            String comboKey = (color + "__" + size).toLowerCase();
            if (!comboSet.add(comboKey)) {
                throw new RuntimeException("Khong duoc trung mau sac + kich co.");
            }
        }
    }

    /**
     * Convert string to SKU token (remove Vietnamese tones, uppercase, replace spaces with underscore)
     */
    private String toSkuToken(String s) {
        if (s == null || s.isEmpty()) return "";
        return normalizeVietnameseText(s)
                .trim()
                .toUpperCase()
                .replaceAll("\\s+", "_");
    }

    private String normalizeVietnameseText(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.replace('\u0111', 'd').replace('\u0110', 'D');
    }

    private boolean isAllowedProduct(SanPham sanPham) {
        return ProductScopeUtil.isAllowedProduct(sanPham);
    }

    private void validateCategoryScope(DanhMuc danhMuc) {
        if (ProductScopeUtil.isExcludedCategory(danhMuc)) {
            throw new RuntimeException("Website hien chi ho tro ban quan ao, khong ho tro danh muc giay/dep.");
        }
    }

    private <T> Page<T> paginate(List<T> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int start = Math.min(safePage * safeSize, items.size());
        int end = Math.min(start + safeSize, items.size());
        return new PageImpl<>(items.subList(start, end), PageRequest.of(safePage, safeSize), items.size());
    }
}

