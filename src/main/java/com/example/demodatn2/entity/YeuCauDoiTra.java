package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "YEU_CAU_DOI_TRA")
public class YeuCauDoiTra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DonHangId", nullable = false)
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TaiKhoanId", nullable = false)
    private TaiKhoan taiKhoan;

    @Nationalized
    @Column(name = "LyDo", nullable = false, length = 500)
    private String lyDo;

    @Nationalized
    @Column(name = "MoTaChiTiet", length = 1000)
    private String moTaChiTiet;

    @Nationalized
    @Column(name = "PhuongThucHoanTien", nullable = false, length = 50)
    private String phuongThucHoanTien;

    @Nationalized
    @Column(name = "GhiChuXuLy", length = 1000)
    private String ghiChuXuLy;

    @Nationalized
    @Column(name = "AnhMinhChung", length = 255)
    private String anhMinhChung;

    @Nationalized
    @ColumnDefault("N'PENDING'")
    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;

    @Column(name = "NgayCapNhat")
    private Instant ngayCapNhat;

    @OneToMany(mappedBy = "yeuCauDoiTra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietDoiTra> chiTiets = new ArrayList<>();

    @OneToMany(mappedBy = "yeuCauDoiTra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HinhAnhDoiTra> hinhAnhs = new ArrayList<>();

    @OneToMany(mappedBy = "yeuCauDoiTra", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("thoiGian ASC")
    private List<LichSuXuLyDoiTra> lichSuXuLy = new ArrayList<>();


}
