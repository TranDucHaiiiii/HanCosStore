package com.example.demodatn2.service;

import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.repository.MaGiamGiaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private MaGiamGiaRepository voucherRepository;

    @Test
    void getEligibleVouchers_sortsBestDiscountFirst() {
        VoucherService voucherService = new VoucherService(voucherRepository);
        BigDecimal orderAmount = new BigDecimal("500000");

        MaGiamGia fixed30k = voucher("FIX30K", "FIXED", "30000", null, "100000");
        MaGiamGia percent10Max70k = voucher("P10MAX70", "PERCENT", "10", "70000", "200000");
        MaGiamGia fixed50k = voucher("FIX50K", "FIXED", "50000", null, "250000");

        when(voucherRepository.findAvailableVouchers()).thenReturn(List.of(fixed30k, percent10Max70k, fixed50k));

        List<MaGiamGia> vouchers = voucherService.getEligibleVouchers(orderAmount);

        assertThat(vouchers).extracting(MaGiamGia::getMa)
                .containsExactly("P10MAX70", "FIX50K", "FIX30K");
    }

    @Test
    void getAll_deactivatesExpiredActiveVouchersBeforeListing() {
        VoucherService voucherService = new VoucherService(voucherRepository);
        when(voucherRepository.findAll()).thenReturn(List.of());

        voucherService.getAll();

        verify(voucherRepository).deactivateExpiredActiveVouchers();
    }

    @Test
    void save_rejectsActiveExpiredVoucherUntilExtended() {
        VoucherService voucherService = new VoucherService(voucherRepository);
        MaGiamGia voucher = voucher("EXPIRED", "FIXED", "30000", null, "150000");
        voucher.setTrangThai("ACTIVE");
        voucher.setSoLuongToiDa(10);
        voucher.setSoLuongDaDung(0);
        voucher.setBatDauLuc(Instant.now().minus(10, ChronoUnit.DAYS));
        voucher.setKetThucLuc(Instant.now().minus(1, ChronoUnit.DAYS));
        when(voucherRepository.findByMa("EXPIRED")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voucherService.save(voucher))
                .hasMessageContaining("gia hạn thời gian kết thúc");
    }

    @Test
    void save_preservesUsedQuantityWhenEditingVoucher() {
        VoucherService voucherService = new VoucherService(voucherRepository);
        MaGiamGia existing = voucher("SAVE10", "PERCENT", "10", "30000", "100000");
        existing.setId(5);
        existing.setSoLuongToiDa(100);
        existing.setSoLuongDaDung(12);

        MaGiamGia edited = voucher("SAVE10", "PERCENT", "15", "30000", "100000");
        edited.setId(5);
        edited.setSoLuongToiDa(100);
        edited.setTrangThai("ACTIVE");
        edited.setBatDauLuc(Instant.now().minus(1, ChronoUnit.DAYS));
        edited.setKetThucLuc(Instant.now().plus(1, ChronoUnit.DAYS));

        when(voucherRepository.findById(5)).thenReturn(Optional.of(existing));
        when(voucherRepository.findByMa("SAVE10")).thenReturn(Optional.of(existing));
        when(voucherRepository.save(edited)).thenReturn(edited);

        MaGiamGia saved = voucherService.save(edited);

        assertThat(saved.getSoLuongDaDung()).isEqualTo(12);
        verify(voucherRepository).save(edited);
    }

    @Test
    void search_filtersByKeywordStatusTypeAndValidity() {
        VoucherService voucherService = new VoucherService(voucherRepository);
        MaGiamGia validPercent = voucher("SUMMER10", "PERCENT", "10", "30000", "100000");
        validPercent.setTrangThai("ACTIVE");
        validPercent.setBatDauLuc(Instant.now().minus(1, ChronoUnit.DAYS));
        validPercent.setKetThucLuc(Instant.now().plus(1, ChronoUnit.DAYS));

        MaGiamGia expiredPercent = voucher("SUMMER20", "PERCENT", "20", "50000", "200000");
        expiredPercent.setTrangThai("INACTIVE");
        expiredPercent.setBatDauLuc(Instant.now().minus(10, ChronoUnit.DAYS));
        expiredPercent.setKetThucLuc(Instant.now().minus(1, ChronoUnit.DAYS));

        MaGiamGia validFixed = voucher("FIX30K", "FIXED", "30000", null, "150000");
        validFixed.setTrangThai("ACTIVE");
        validFixed.setBatDauLuc(Instant.now().minus(1, ChronoUnit.DAYS));
        validFixed.setKetThucLuc(Instant.now().plus(1, ChronoUnit.DAYS));

        when(voucherRepository.findAll()).thenReturn(List.of(validPercent, expiredPercent, validFixed));

        List<MaGiamGia> vouchers = voucherService.search("summer", "ACTIVE", "PERCENT", "VALID");

        assertThat(vouchers).extracting(MaGiamGia::getMa).containsExactly("SUMMER10");
    }

    private MaGiamGia voucher(String code, String type, String value, String maxValue, String minOrder) {
        MaGiamGia voucher = new MaGiamGia();
        voucher.setMa(code);
        voucher.setLoai(type);
        voucher.setGiaTri(new BigDecimal(value));
        voucher.setGiaTriToiDa(maxValue == null ? null : new BigDecimal(maxValue));
        voucher.setDonToiThieu(new BigDecimal(minOrder));
        return voucher;
    }
}
