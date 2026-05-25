package com.example.demodatn2.service;

import com.example.demodatn2.dto.CartItemDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.*;
import com.example.demodatn2.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// Service giỏ hàng: tạo/lấy giỏ theo session hoặc tài khoản, cập nhật item và tính tiền.
public class CartService {

    public static final String SELECTED_CART_ITEM_IDS = "SELECTED_CART_ITEM_IDS";

    private final GioHangRepository gioHangRepository;
    private final ChiTietGioHangRepository chiTietGioHangRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    // Lấy giỏ hàng hiện tại theo user đăng nhập, nếu là khách thì theo sessionId; chưa có thì tạo mới
    public GioHang getOrCreateCart(HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser != null) {
            TaiKhoan taiKhoan = taiKhoanRepository.findById(loginUser.getId()).orElse(null);
            if (taiKhoan != null) {
                log.info("Lấy giỏ hàng cho user đã đăng nhập: {}", loginUser.getTenDangNhap());
                return gioHangRepository.findByTaiKhoan(taiKhoan)
                        .orElseGet(() -> {
                            GioHang newCart = new GioHang();
                            newCart.setTaiKhoan(taiKhoan);
                            return gioHangRepository.save(newCart);
                        });
            }
        }

        String sessionId = session.getId();
        log.info("Lấy giỏ hàng cho khách (Guest) - SessionID: {}", sessionId);
        return gioHangRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    log.info("Tạo mới giỏ hàng cho khách với SessionID: {}", sessionId);
                    GioHang newCart = new GioHang();
                    newCart.setSessionId(sessionId);
                    return gioHangRepository.save(newCart);
                });
    }

    // Thêm sản phẩm vào giỏ, kiểm tra tồn kho và cộng dồn số lượng nếu biến thể đã tồn tại trong giỏ
    @Transactional
    public Integer addToCart(Integer bienTheId, Integer soLuong, HttpSession session) {
        log.info("Thêm sản phẩm vào giỏ hàng - SessionID: {}", session.getId());
        GioHang gioHang = getOrCreateCart(session);
        BienTheSanPham bienThe = bienTheSanPhamRepository.findById(bienTheId)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));

        if (bienThe.getSoLuongTon() < soLuong) {
            throw new RuntimeException("Số lượng tồn kho không đủ");
        }

        Optional<ChiTietGioHang> existingItem = chiTietGioHangRepository.findByGioHangAndBienTheSanPham(gioHang, bienThe);

        if (existingItem.isPresent()) {
            ChiTietGioHang item = existingItem.get();
            item.setSoLuong(item.getSoLuong() + soLuong);
            if (item.getSoLuong() > bienThe.getSoLuongTon()) {
                throw new RuntimeException("Tổng số lượng vượt quá tồn kho");
            }
            return chiTietGioHangRepository.save(item).getId();
        } else {
            ChiTietGioHang newItem = new ChiTietGioHang();
            newItem.setGioHang(gioHang);
            newItem.setBienTheSanPham(bienThe);
            newItem.setSoLuong(soLuong);
            newItem.setDonGia(bienThe.getGia());
            return chiTietGioHangRepository.save(newItem).getId();
        }
    }

    // Cập nhật số lượng 1 dòng sản phẩm trong giỏ; nếu số lượng <= 0 thì xóa dòng đó
    @Transactional
    public void updateQuantity(Integer itemId, Integer soLuong) {
        ChiTietGioHang item = chiTietGioHangRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm trong giỏ hàng không tồn tại"));
        
        if (soLuong <= 0) {
            chiTietGioHangRepository.delete(item);
            return;
        }

        if (item.getBienTheSanPham().getSoLuongTon() < soLuong) {
            throw new RuntimeException("Số lượng tồn kho không đủ");
        }

        item.setSoLuong(soLuong);
        chiTietGioHangRepository.save(item);
    }

    // Xóa một dòng sản phẩm khỏi giỏ theo id chi tiết giỏ hàng
    @Transactional
    public void removeItem(Integer itemId) {
        chiTietGioHangRepository.deleteById(itemId);
    }

    // Chuyển dữ liệu entity giỏ hàng sang DTO để render giao diện và tính tiền
    @Transactional(readOnly = true)
    public List<CartItemDTO> getCartItems(HttpSession session) {
        GioHang gioHang = getOrCreateCart(session);
        return gioHang.getChiTiets().stream()
                .map(item -> {
                    BienTheSanPham bt = item.getBienTheSanPham();
                    SanPham sp = bt.getSanPham();
                    
                    // Lấy ảnh chính của sản phẩm
                    String anh = sp.getHinhAnhSanPhams().stream()
                            .filter(HinhAnhSanPham::getLaAnhChinh)
                            .map(HinhAnhSanPham::getDuongDanAnh)
                            .findFirst()
                            .orElse(sp.getHinhAnhSanPhams().isEmpty() ? "/images/no-image.png" : sp.getHinhAnhSanPhams().get(0).getDuongDanAnh());

                    return CartItemDTO.builder()
                            .id(item.getId())
                            .bienTheId(bt.getId())
                            .sanPhamId(sp.getId())
                            .tenSanPham(sp.getTen())
                            .mauSac(bt.getMauSac())
                            .kichCo(bt.getKichCo())
                            .anh(anh)
                            .soLuong(item.getSoLuong())
                            .donGia(item.getDonGia())
                            .thanhTien(item.getDonGia().multiply(new BigDecimal(item.getSoLuong())))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // Tính tổng tiền tạm tính của toàn bộ sản phẩm trong giỏ
    public BigDecimal getTotalAmount(List<CartItemDTO> items) {
        return items.stream()
                .map(CartItemDTO::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<CartItemDTO> getSelectedCartItems(HttpSession session) {
        List<CartItemDTO> items = getCartItems(session);
        Set<Integer> selectedIds = syncSelectedIds(session, items);
        return items.stream()
                .filter(item -> selectedIds.contains(item.getId()))
                .toList();
    }

    public Set<Integer> getSelectedIds(HttpSession session) {
        Object raw = session.getAttribute(SELECTED_CART_ITEM_IDS);
        if (raw instanceof Set<?> rawSet) {
            return rawSet.stream()
                    .filter(Integer.class::isInstance)
                    .map(Integer.class::cast)
                    .collect(Collectors.toCollection(HashSet::new));
        }
        return new HashSet<>();
    }

    public void setSelectedIds(HttpSession session, Set<Integer> selectedIds) {
        session.setAttribute(SELECTED_CART_ITEM_IDS, new HashSet<>(selectedIds));
    }

    public Set<Integer> syncSelectedIds(HttpSession session, List<CartItemDTO> items) {
        Set<Integer> currentItemIds = items.stream()
                .map(CartItemDTO::getId)
                .collect(Collectors.toCollection(HashSet::new));
        Set<Integer> selectedIds = getSelectedIds(session);

        if (session.getAttribute(SELECTED_CART_ITEM_IDS) == null) {
            selectedIds = new HashSet<>(currentItemIds);
        } else {
            selectedIds.retainAll(currentItemIds);
        }

        setSelectedIds(session, selectedIds);
        return selectedIds;
    }

    public void removeSelectedId(HttpSession session, Integer itemId) {
        Set<Integer> selectedIds = getSelectedIds(session);
        selectedIds.remove(itemId);
        setSelectedIds(session, selectedIds);
    }

    // Tổng số lượng sản phẩm (theo từng đơn vị) đang có trong giỏ
    @Transactional(readOnly = true)
    public int getTotalWeightGram(HttpSession session) {
        GioHang gioHang = getOrCreateCart(session);
        int total = gioHang.getChiTiets().stream()
                .mapToInt(item -> {
                    Integer gram = item.getBienTheSanPham().getKhoiLuongGram();
                    int safeGram = (gram == null || gram <= 0) ? 100 : gram;
                    return safeGram * item.getSoLuong();
                })
                .sum();
        return total > 0 ? total : 100;
    }

    @Transactional(readOnly = true)
    public int getSelectedTotalWeightGram(HttpSession session) {
        Set<Integer> selectedIds = getSelectedIds(session);
        if (selectedIds.isEmpty()) {
            return 0;
        }
        GioHang gioHang = getOrCreateCart(session);
        int total = gioHang.getChiTiets().stream()
                .filter(item -> selectedIds.contains(item.getId()))
                .mapToInt(item -> {
                    Integer gram = item.getBienTheSanPham().getKhoiLuongGram();
                    int safeGram = (gram == null || gram <= 0) ? 100 : gram;
                    return safeGram * item.getSoLuong();
                })
                .sum();
        return total > 0 ? total : 0;
    }

    @Transactional(readOnly = true)
    public int getItemCount(HttpSession session) {
        GioHang gioHang = getOrCreateCart(session);
        return gioHang.getChiTiets().stream()
                .mapToInt(ChiTietGioHang::getSoLuong)
                .sum();
    }
}
