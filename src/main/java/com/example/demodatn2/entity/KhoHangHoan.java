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
@Table(name = "KHO_HANG_HOAN")
public class KhoHangHoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SanPhamChiTietId", nullable = false)
    private BienTheSanPham bienTheSanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "YeuCauDoiTraId")
    private YeuCauDoiTra yeuCauDoiTra;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ChiTietDoiTraId")
    private ChiTietDoiTra chiTietDoiTra;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Nationalized
    @Column(name = "TinhTrang", length = 50)
    private String tinhTrang;

    @Nationalized
    @Column(name = "HuongXuLy", length = 50)
    private String huongXuLy;

    @Nationalized
    @Column(name = "TrangThai", length = 50)
    private String trangThai;

    @Nationalized
    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayNhap", nullable = false)
    private Instant ngayNhap;

    @PrePersist
    protected void onCreate() {
        if (ngayNhap == null) {
            ngayNhap = Instant.now();
        }
    }
}
