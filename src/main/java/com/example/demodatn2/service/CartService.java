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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// Service giỏ hàng: tạo/lấy giỏ theo session hoặc tài khoản, cập nhật item và tính tiền.
public class CartService {

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
    public void addToCart(Integer bienTheId, Integer soLuong, HttpSession session) {
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
            chiTietGioHangRepository.save(item);
        } else {
            ChiTietGioHang newItem = new ChiTietGioHang();
            newItem.setGioHang(gioHang);
            newItem.setBienTheSanPham(bienThe);
            newItem.setSoLuong(soLuong);
            newItem.setDonGia(bienThe.getGia());
            chiTietGioHangRepository.save(newItem);
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

    // Quy tắc phí vận chuyển theo tổng tiền hàng (chưa trừ voucher)
    // > 500.000đ: miễn phí, từ 200.000đ - 500.000đ: 20.000đ, còn lại: 30.000đ
    public static BigDecimal calculateShippingFee(BigDecimal subtotal) {
        if (subtotal.compareTo(new BigDecimal("500000")) > 0) {
            return BigDecimal.ZERO;
        } else if (subtotal.compareTo(new BigDecimal("200000")) >= 0) {
            return new BigDecimal("20000");
        } else {
            return new BigDecimal("30000");
        }
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
    public int getItemCount(HttpSession session) {
        GioHang gioHang = getOrCreateCart(session);
        return gioHang.getChiTiets().stream()
                .mapToInt(ChiTietGioHang::getSoLuong)
                .sum();
    }
}
