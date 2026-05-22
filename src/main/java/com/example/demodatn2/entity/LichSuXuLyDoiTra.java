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
@Table(name = "LICH_SU_XU_LY_DOI_TRA")
public class LichSuXuLyDoiTra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "YeuCauDoiTraId", nullable = false)
    private YeuCauDoiTra yeuCauDoiTra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiXuLyId")
    private TaiKhoan nguoiXuLy;

    @Nationalized
    @Column(name = "HanhDong", nullable = false, length = 80)
    private String hanhDong;

    @Nationalized
    @Column(name = "GhiChu", length = 1000)
    private String ghiChu;

    @ColumnDefault("sysdatetime()")
    @Column(name = "ThoiGian", nullable = false)
    private Instant thoiGian;
}
