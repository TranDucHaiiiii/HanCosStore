package com.example.demodatn2.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "ORDERS")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MaDon", nullable = false, length = 50, unique = true)
    private String maDon;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false, length = 30)
    private OrderStatus trangThai = OrderStatus.CHO_XAC_NHAN;

    @Column(name = "LyDoLoiVanChuyen", length = 500)
    private String lyDoLoiVanChuyen;

    @Column(name = "LyDoHuy", length = 500)
    private String lyDoHuy;

    @Column(name = "DaDieuChinhTonKho", nullable = false)
    private boolean daDieuChinhTonKho = false;

    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;

    @Column(name = "NgayCapNhat", nullable = false)
    private Instant ngayCapNhat;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (ngayTao == null) {
            ngayTao = now;
        }
        ngayCapNhat = now;
    }

    @PreUpdate
    public void preUpdate() {
        ngayCapNhat = Instant.now();
    }
}
