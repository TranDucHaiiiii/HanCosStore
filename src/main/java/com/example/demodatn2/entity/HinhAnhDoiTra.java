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
@Table(name = "HINH_ANH_DOI_TRA")
public class HinhAnhDoiTra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "YeuCauDoiTraId", nullable = false)
    private YeuCauDoiTra yeuCauDoiTra;

    @Nationalized
    @Column(name = "DuongDanAnh", nullable = false, length = 500)
    private String duongDanAnh;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;
}
