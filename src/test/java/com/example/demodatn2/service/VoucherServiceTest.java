package com.example.demodatn2.service;

import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.repository.MaGiamGiaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
