package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "REFUND_TRANSACTION")
public class RefundTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "YeuCauDoiTraId", nullable = false)
    private YeuCauDoiTra yeuCauDoiTra;

    @Column(name = "SoTienHoan", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienHoan;

    @Nationalized
    @Column(name = "PhuongThucHoanTien", nullable = false, length = 50)
    private String phuongThucHoanTien;

    @Nationalized
    @Column(name = "MaGiaoDich", length = 100)
    private String maGiaoDich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiXuLyId")
    private TaiKhoan nguoiXuLy;

    @ColumnDefault("sysdatetime()")
    @Column(name = "ThoiGianHoan", nullable = false)
    private Instant thoiGianHoan;

    @Nationalized
    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    @PrePersist
    protected void onCreate() {
        if (thoiGianHoan == null) {
            thoiGianHoan = Instant.now();
        }
    }
}
