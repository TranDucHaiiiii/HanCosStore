package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "DANH_GIA_SAN_PHAM")
public class DanhGiaSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SanPhamId", nullable = false)
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TaiKhoanId", nullable = false)
    private TaiKhoan taiKhoan;

    @Column(name = "SoSao", nullable = false)
    private Integer soSao;

    @Nationalized
    @Column(name = "NoiDung", length = 1000)
    private String noiDung;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = Instant.now();
        }
    }
}
