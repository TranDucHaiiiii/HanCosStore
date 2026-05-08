package com.example.demodatn2.service;

import com.example.demodatn2.dto.SepayWebhookRequest;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.GiaoDichThanhToan;
import com.example.demodatn2.repository.DonHangRepository;
import com.example.demodatn2.repository.GiaoDichThanhToanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SepayServiceTest {

    @Mock
    private DonHangRepository donHangRepository;

    @Mock
    private GiaoDichThanhToanRepository giaoDichThanhToanRepository;

    @Mock
    private com.example.demodatn2.repository.PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Test
    void handleWebhook_updatesOrderAndTransactionWhenPending() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SepayService sepayService = new SepayService(donHangRepository, giaoDichThanhToanRepository, phuongThucThanhToanRepository, objectMapper);

        SepayWebhookRequest payload = new SepayWebhookRequest();
        payload.setContent("DH-TEST01");
        payload.setAmount(new BigDecimal("199000"));
        payload.setTransactionId("SEPAY-001");

        DonHang order = new DonHang();
        order.setMaDonHang("DH-TEST01");
        order.setTongTien(new BigDecimal("199000"));
        order.setTrangThai("PENDING");

        when(donHangRepository.findByMaDonHangIgnoreCase("DH-TEST01")).thenReturn(Optional.of(order));
        when(giaoDichThanhToanRepository.findFirstByDonHangAndNhaCungCapOrderByNgayTaoDesc(order, "SEPAY"))
                .thenReturn(Optional.empty());
        when(phuongThucThanhToanRepository.findByMa("SEPAY"))
                .thenReturn(Optional.of(new com.example.demodatn2.entity.PhuongThucThanhToan()));

        sepayService.handleWebhook(payload);

        ArgumentCaptor<GiaoDichThanhToan> txCaptor = ArgumentCaptor.forClass(GiaoDichThanhToan.class);
        verify(giaoDichThanhToanRepository).save(txCaptor.capture());
        verify(donHangRepository).save(order);

        GiaoDichThanhToan tx = txCaptor.getValue();
        assertThat(tx.getDonHang()).isEqualTo(order);
        assertThat(tx.getSoTien()).isEqualByComparingTo("199000");
        assertThat(tx.getTrangThai()).isEqualTo("PAID");
        assertThat(order.getTrangThai()).isEqualTo("PAID");
    }

    @Test
    void handleWebhook_acceptsRealSepayPayloadWithTransferAmountAndCodeWithoutDash() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SepayService sepayService = new SepayService(donHangRepository, giaoDichThanhToanRepository, phuongThucThanhToanRepository, objectMapper);

        String json = """
                {
                  "gateway": "MSB",
                  "transactionDate": "2026-05-06 23:04:59",
                  "accountNumber": "80003084517",
                  "subAccount": "96886693011616",
                  "code": "DHFDE07CB8",
                  "content": "ZP7CTN3US6UO DHFDE07CB8 ",
                  "transferType": "in",
                  "description": "BankAPINotify ZP7CTN3US6UO DHFDE07CB8 _96886693011616",
                  "transferAmount": 40000,
                  "referenceCode": "FT261278J12P",
                  "accumulated": 0,
                  "id": 56039435
                }
                """;
        SepayWebhookRequest payload = objectMapper.readValue(json, SepayWebhookRequest.class);

        DonHang order = new DonHang();
        order.setMaDonHang("DH-FDE07CB8");
        order.setTongTien(new BigDecimal("40000"));
        order.setTrangThai("PENDING");

        when(donHangRepository.findByMaDonHangIgnoreCase("DHFDE07CB8")).thenReturn(Optional.empty());
        when(donHangRepository.findByMaDonHangIgnoreCase("DH-FDE07CB8")).thenReturn(Optional.of(order));
        when(giaoDichThanhToanRepository.findFirstByDonHangAndNhaCungCapOrderByNgayTaoDesc(order, "SEPAY"))
                .thenReturn(Optional.empty());
        when(phuongThucThanhToanRepository.findByMa("SEPAY"))
                .thenReturn(Optional.of(new com.example.demodatn2.entity.PhuongThucThanhToan()));

        sepayService.handleWebhook(payload);

        ArgumentCaptor<GiaoDichThanhToan> txCaptor = ArgumentCaptor.forClass(GiaoDichThanhToan.class);
        verify(giaoDichThanhToanRepository).save(txCaptor.capture());
        verify(donHangRepository).save(order);

        GiaoDichThanhToan tx = txCaptor.getValue();
        assertThat(tx.getSoTien()).isEqualByComparingTo("40000");
        assertThat(tx.getMaGiaoDich()).isEqualTo("FT261278J12P");
        assertThat(order.getTrangThai()).isEqualTo("PAID");
    }
}
