package com.example.demodatn2.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "CHAT_LIEU")
public class ChatLieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "TenChatLieu", nullable = false, length = 100, unique = true)
    private String tenChatLieu;

    @Nationalized
    @Column(name = "TrangThai", length = 20)
    private String trangThai = "ACTIVE";

    @JsonIgnore
    @OneToMany(mappedBy = "chatLieu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SanPham> sanPhams = new ArrayList<>();
}
