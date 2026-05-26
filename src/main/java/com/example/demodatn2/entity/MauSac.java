package com.example.demodatn2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "MAU_SAC")
public class MauSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "TenMau", nullable = false, length = 50, unique = true)
    private String tenMau;

    @Column(name = "MaMau", length = 20)
    private String maMau;

    @Nationalized
    @Column(name = "TrangThai", length = 30)
    private String trangThai = "ACTIVE";

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao")
    private Instant ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = Instant.now();
        }
        if (trangThai == null || trangThai.isBlank()) {
            trangThai = "ACTIVE";
        }
    }

    public String getTen() {
        return tenMau;
    }
}
