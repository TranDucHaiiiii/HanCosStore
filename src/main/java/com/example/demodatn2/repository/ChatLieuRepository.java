package com.example.demodatn2.repository;

import com.example.demodatn2.entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {
    Optional<ChatLieu> findByTenChatLieuIgnoreCase(String tenChatLieu);
}
