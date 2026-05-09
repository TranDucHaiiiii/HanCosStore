package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "CHI_TIET_DOI_TRA")
public class ChiTietDoiTra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "YeuCauDoiTraId", nullable = false)
    private YeuCauDoiTra yeuCauDoiTra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ChiTietDonHangId", nullable = false)
    private ChiTietDonHang chiTietDonHang;

    @Nationalized
    @Column(name = "InspectionStatus", nullable = false, length = 30)
    private String inspectionStatus = "PENDING_INSPECTION";

    @Nationalized
    @Column(name = "GhiChu", length = 300)
    private String ghiChu;


}
