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
@Table(name = "BIEN_THE_SAN_PHAM")
public class BienTheSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "MaSKU", nullable = false, length = 80)
    private String maSKU;

    @Nationalized
    @Column(name = "MauSac", nullable = false, length = 50)
    private String mauSac;

    @Nationalized
    @Column(name = "KichCo", nullable = false, length = 20)
    private String kichCo;

    @Column(name = "Gia", nullable = false, precision = 18, scale = 2)
    private BigDecimal gia;

    @Column(name = "GiaGoc", precision = 18, scale = 2)
    private BigDecimal giaGoc;

    @Column(name = "SoLuongTon", nullable = false)
    private Integer soLuongTon;

    @ColumnDefault("0")
    @Column(name = "SoLuongLoi", nullable = false)
    private Integer soLuongLoi = 0;

    @Column(name = "KhoiLuongGram")
    private Integer khoiLuongGram;

    @Nationalized
    @ColumnDefault("N'ACTIVE'")
    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = Instant.now();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SanPhamId", nullable = false)
    private SanPham sanPham;
}
