package com.example.demodatn2.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SAN_PHAM")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "MaSanPham", nullable = false, length = 50)
    private String maSanPham;

    @Nationalized
    @Column(name = "Ten", nullable = false, length = 200)
    private String ten;

    @Nationalized
    @Column(name = "MoTaNgan", length = 500)
    private String moTaNgan;

    @Nationalized
    @Lob
    @Column(name = "MoTa")
    private String moTa;

    @ManyToOne
    @JoinColumn(name = "ChatLieuId")
    private ChatLieu chatLieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ThuongHieuId")
    private ThuongHieu thuongHieu;

    @Nationalized
    @Column(name = "GioiTinh", length = 30)
    private String gioiTinh;

    @Nationalized
    @ColumnDefault("N'ACTIVE'")
    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @ColumnDefault("0")
    @Column(name = "DaXoa", nullable = false)
    private Boolean daXoa;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;

    @Column(name = "NgayCapNhat")
    private Instant ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = Instant.now();
        }
        ngayCapNhat = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = Instant.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DanhMucId")
    private DanhMuc danhMuc;
    @JsonIgnore
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BienTheSanPham> bienThes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HinhAnhMauSac> hinhAnhMauSacs = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HinhAnhSanPham> hinhAnhSanPhams = new ArrayList<>();
}
