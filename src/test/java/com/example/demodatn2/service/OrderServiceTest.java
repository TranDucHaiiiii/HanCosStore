package com.example.demodatn2.service;

import com.example.demodatn2.entity.BienTheSanPham;
import com.example.demodatn2.entity.ChiTietGioHang;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.GiaoDichThanhToan;
import com.example.demodatn2.entity.GioHang;
import com.example.demodatn2.entity.KichCo;
import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.entity.MauSac;
import com.example.demodatn2.entity.SanPham;
import com.example.demodatn2.repository.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private DonHangRepository donHangRepository;
    @Mock private ChiTietDonHangRepository chiTietDonHangRepository;
    @Mock private ChiTietGioHangRepository chiTietGioHangRepository;
    @Mock private GioHangRepository gioHangRepository;
    @Mock private TaiKhoanRepository taiKhoanRepository;
    @Mock private BienTheSanPhamRepository bienTheSanPhamRepository;
    @Mock private MaGiamGiaRepository maGiamGiaRepository;
    @Mock private LichSuSuDungMaGiamGiaRepository lichSuSuDungMaGiamGiaRepository;
    @Mock private YeuCauDoiTraRepository yeuCauDoiTraRepository;
    @Mock private GiaoDichTonKhoRepository giaoDichTonKhoRepository;
    @Mock private GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    @Mock private JavaMailSender mailSender;

    private OrderService newOrderService() {
        VoucherService voucherService = new VoucherService(maGiamGiaRepository);
        OrderConfirmationEmailService orderConfirmationEmailService = new OrderConfirmationEmailService(mailSender);
        return new OrderService(
                donHangRepository,
                chiTietDonHangRepository,
                chiTietGioHangRepository,
                gioHangRepository,
                taiKhoanRepository,
                bienTheSanPhamRepository,
                maGiamGiaRepository,
                lichSuSuDungMaGiamGiaRepository,
                yeuCauDoiTraRepository,
                giaoDichTonKhoRepository,
                giaoDichThanhToanRepository,
                orderConfirmationEmailService,
                voucherService
        );
    }

    @Test
    void createOrder_includesGhtkShippingFeeInSepayTotal() {
        OrderService orderService = newOrderService();

        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn("session-1");
        when(session.getAttribute("LOGIN_USER")).thenReturn(null);

        SanPham sanPham = new SanPham();
        sanPham.setTen("Ao thun");

        BienTheSanPham bienThe = new BienTheSanPham();
        bienThe.setId(11);
        MauSac mauSac = new MauSac();
        mauSac.setId(1);
        mauSac.setTenMau("Den");
        mauSac.setMaMau("#000000");

        KichCo kichCo = new KichCo();
        kichCo.setId(1);
        kichCo.setTenKichCo("L");
        kichCo.setLoai("AO");

        bienThe.setMauSac(mauSac);
        bienThe.setKichCo(kichCo);
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
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void cancelOrder_allowsConfirmedSepayOrder() {
        OrderService orderService = newOrderService();

        DonHang order = new DonHang();
        order.setId(1);
        order.setMaDonHang("DH-SEPAY01");
        order.setTrangThai("DA_XAC_NHAN");
        order.setPhuongThucThanhToan("SEPAY");
        MaGiamGia voucher = new MaGiamGia();
        voucher.setId(9);
        order.setMaGiamGia(voucher);

        when(donHangRepository.findById(1)).thenReturn(Optional.of(order));
        when(chiTietDonHangRepository.findByDonHang(order)).thenReturn(List.of());

        orderService.cancelOrder(1, "Khach huy", false);

        assertThat(order.getTrangThai()).isEqualTo("DA_HUY");
        assertThat(order.getLyDoHuy()).isEqualTo("Khach huy");
        verify(maGiamGiaRepository).decrementUsageAfterOrderCancel(9);
        verify(donHangRepository).save(order);
    }

    @Test
    void cancelOrder_rejectsShippingOrderEvenForAdmin() {
        OrderService orderService = newOrderService();

        DonHang order = new DonHang();
        order.setId(1);
        order.setMaDonHang("DH-SHIP01");
        order.setTrangThai("DANG_GIAO");

        when(donHangRepository.findById(1)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1, "Admin huy", true))
                .hasMessageContaining("Không thể hủy đơn hàng ở trạng thái: DANG_GIAO");

        verify(donHangRepository, never()).save(any());
        verify(chiTietDonHangRepository, never()).findByDonHang(any());
        verify(maGiamGiaRepository, never()).decrementUsageAfterOrderCancel(any());
    }

    @Test
    void updateOrderStatus_toCanceledRestoresVoucherUsage() {
        OrderService orderService = newOrderService();

        DonHang order = new DonHang();
        order.setId(1);
        order.setMaDonHang("DH-VOUCHER01");
        order.setTrangThai("CHO_XAC_NHAN");
        MaGiamGia voucher = new MaGiamGia();
        voucher.setId(15);
        order.setMaGiamGia(voucher);

        when(donHangRepository.findById(1)).thenReturn(Optional.of(order));
        when(chiTietDonHangRepository.findByDonHang(order)).thenReturn(List.of());

        orderService.updateOrderStatus(1, "DA_HUY");

        assertThat(order.getTrangThai()).isEqualTo("DA_HUY");
        verify(maGiamGiaRepository).decrementUsageAfterOrderCancel(15);
        verify(donHangRepository).save(order);
    }

    @Test
    void updateOrderStatus_rejectsUnpaidSepayConfirmation() {
        OrderService orderService = newOrderService();

        DonHang order = new DonHang();
        order.setId(1);
        order.setMaDonHang("DH-SEPAY02");
        order.setTrangThai("PENDING");
        order.setPhuongThucThanhToan("SEPAY");

        when(donHangRepository.findById(1)).thenReturn(Optional.of(order));
        when(giaoDichThanhToanRepository.findFirstByDonHangAndNhaCungCapOrderByNgayTaoDesc(order, "SEPAY"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(1, "DA_XAC_NHAN"))
                .hasMessageContaining("chưa được SePay xác nhận");

        verify(donHangRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_allowsPaidSepayConfirmation() {
        OrderService orderService = newOrderService();

        DonHang order = new DonHang();
        order.setId(1);
        order.setMaDonHang("DH-SEPAY03");
        order.setTrangThai("PENDING");
        order.setPhuongThucThanhToan("SEPAY");

        GiaoDichThanhToan tx = new GiaoDichThanhToan();
        tx.setTrangThai("PAID");

        when(donHangRepository.findById(1)).thenReturn(Optional.of(order));
        when(giaoDichThanhToanRepository.findFirstByDonHangAndNhaCungCapOrderByNgayTaoDesc(order, "SEPAY"))
                .thenReturn(Optional.of(tx));

        orderService.updateOrderStatus(1, "DA_XAC_NHAN");

        assertThat(order.getTrangThai()).isEqualTo("DA_XAC_NHAN");
        verify(donHangRepository).save(order);
    }

    @Test
    void updateOrderAddress_rejectsConfirmedSepayOrder() {
        OrderService orderService = newOrderService();

        DonHang order = new DonHang();
        order.setId(1);
        order.setTrangThai("DA_XAC_NHAN");
        order.setPhuongThucThanhToan("SEPAY");

        when(donHangRepository.findById(1)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderAddress(
                1,
                "Nguyen Van A",
                "0900000000",
                "Dia chi moi",
                new BigDecimal("30000")
        )).hasMessageContaining("Không thể thay đổi địa chỉ");

        verify(donHangRepository, never()).save(any());
    }

    @Test
    void cleanupExpiredPendingTransferOrders_cancelsOnlyBankTransferOrders() {
        OrderService orderService = newOrderService();

        DonHang sepayOrder = new DonHang();
        sepayOrder.setId(1);
        sepayOrder.setMaDonHang("DH-SEPAY01");
        sepayOrder.setTrangThai("PENDING");
        sepayOrder.setPhuongThucThanhToan("SEPAY");
        sepayOrder.setNgayDat(Instant.now().minus(45, ChronoUnit.MINUTES));

        DonHang codOrder = new DonHang();
        codOrder.setId(2);
        codOrder.setMaDonHang("DH-COD01");
        codOrder.setTrangThai("CHO_XAC_NHAN");
        codOrder.setPhuongThucThanhToan("COD");
        codOrder.setNgayDat(Instant.now().minus(45, ChronoUnit.MINUTES));

        when(donHangRepository.timTheoTrangThaiVaCapNhatTruoc(eq("PENDING"), any(Instant.class)))
                .thenReturn(List.of(sepayOrder));
        when(donHangRepository.timTheoTrangThaiVaCapNhatTruoc(eq("CHO_XAC_NHAN"), any(Instant.class)))
                .thenReturn(List.of(codOrder));
        when(chiTietDonHangRepository.findByDonHang(sepayOrder)).thenReturn(List.of());

        int cleaned = orderService.cleanupExpiredPendingTransferOrders(30);

        assertThat(cleaned).isEqualTo(1);
        assertThat(sepayOrder.getTrangThai()).isEqualTo("DA_HUY");
        assertThat(sepayOrder.getLyDoHuy()).isEqualTo("Quá hạn chờ thanh toán chuyển khoản");
        assertThat(codOrder.getTrangThai()).isEqualTo("CHO_XAC_NHAN");
        verify(donHangRepository).save(sepayOrder);
        verify(donHangRepository, never()).save(codOrder);
    }
}



