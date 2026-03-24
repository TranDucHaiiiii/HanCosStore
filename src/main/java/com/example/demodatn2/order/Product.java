package com.example.demodatn2.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "PRODUCTS")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Ten", nullable = false, length = 200)
    private String ten;

    @Column(name = "SoLuongTon", nullable = false)
    private Integer soLuongTon;
}
