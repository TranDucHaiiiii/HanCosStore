package com.example.demodatn2.service;

import com.example.demodatn2.entity.BienTheSanPham;
import com.example.demodatn2.entity.ChiTietGioHang;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.GioHang;
import com.example.demodatn2.entity.SanPham;
import com.example.demodatn2.repository.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private DonHangRepository donHangRepository;
    @Mock private ChiTietDonHangRepository chiTietDonHangRepository;
    @Mock private GioHangRepository gioHangRepository;
    @Mock private TaiKhoanRepository taiKhoanRepository;
    @Mock private BienTheSanPhamRepository bienTheSanPhamRepository;
    @Mock private MaGiamGiaRepository maGiamGiaRepository;
    @Mock private LichSuSuDungMaGiamGiaRepository lichSuSuDungMaGiamGiaRepository;
    @Mock private YeuCauDoiTraRepository yeuCauDoiTraRepository;
    @Mock private GiaoDichTonKhoRepository giaoDichTonKhoRepository;

    @Test
    void createOrder_includesGhtkShippingFeeInSepayTotal() {
        VoucherService voucherService = new VoucherService(maGiamGiaRepository);
        OrderService orderService = new OrderService(
                donHangRepository,
                chiTietDonHangRepository,
                gioHangRepository,
                taiKhoanRepository,
                bienTheSanPhamRepository,
                maGiamGiaRepository,
                lichSuSuDungMaGiamGiaRepository,
                yeuCauDoiTraRepository,
                giaoDichTonKhoRepository,
                voucherService
        );

        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn("session-1");
        when(session.getAttribute("LOGIN_USER")).thenReturn(null);

        SanPham sanPham = new SanPham();
        sanPham.setTen("Ao thun");

        BienTheSanPham bienThe = new BienTheSanPham();
        bienThe.setId(11);
        bienThe.setMauSac("Den");
        bienThe.setKichCo("L");
        bienThe.setGia(new BigDecimal("40000"));
        bienThe.setSoLuongTon(10);
        bienThe.setSanPham(sanPham);

        ChiTietGioHang cartItem = new ChiTietGioHang();
        cartItem.setBienTheSanPham(bienThe);
        cartItem.setSoLuong(1);
        cartItem.setDonGia(new BigDecimal("40000"));

        GioHang gioHang = new GioHang();
        gioHang.setChiTiets(List.of(cartItem));
        cartItem.setGioHang(gioHang);

        when(gioHangRepository.findBySessionId("session-1")).thenReturn(Optional.of(gioHang));
        when(donHangRepository.save(any(DonHang.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chiTietDonHangRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bienTheSanPhamRepository.decrementStockIfEnough(11, 1)).thenReturn(1);

        DonHang order = orderService.createOrder(
                "Nguyen Van A",
                "0900000000",
                "a@example.com",
                "123 Duong ABC",
                "",
                "SEPAY",
                new BigDecimal("30000"),
                session
        );

        ArgumentCaptor<DonHang> orderCaptor = ArgumentCaptor.forClass(DonHang.class);
        verify(donHangRepository).save(orderCaptor.capture());
        DonHang savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getPhuongThucThanhToan()).isEqualTo("SEPAY");
        assertThat(savedOrder.getTrangThai()).isEqualTo("PENDING");
        assertThat(savedOrder.getTamTinh()).isEqualByComparingTo("40000");
        assertThat(savedOrder.getPhiVanChuyen()).isEqualByComparingTo("30000");
        assertThat(savedOrder.getTongTien()).isEqualByComparingTo("70000");
        assertThat(order.getTongTien()).isEqualByComparingTo("70000");
        verify(giaoDichTonKhoRepository).save(any());
        verify(gioHangRepository).delete(gioHang);
    }
}



