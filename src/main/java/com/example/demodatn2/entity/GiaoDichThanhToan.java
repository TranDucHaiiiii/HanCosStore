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
@Table(name = "GIAO_DICH_THANH_TOAN")
public class GiaoDichThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DonHangId", nullable = false)
    private DonHang donHang;

    @Column(name = "SoTien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @Nationalized
    @Column(name = "NhaCungCap", length = 50)
    private String nhaCungCap;

    @Nationalized
    @Column(name = "MaGiaoDich", length = 100)
    private String maGiaoDich;

    @Nationalized
    @ColumnDefault("N'PENDING'")
    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @Column(name = "ThoiGianThanhToan")
    private Instant thoiGianThanhToan;

    @Nationalized
    @Lob
    @Column(name = "DuLieuRaw")
    private String duLieuRaw;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = Instant.now();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PhuongThucThanhToanId", nullable = false)
    private PhuongThucThanhToan phuongThucThanhToan;


}