package com.example.demodatn2.controller;

import com.example.demodatn2.entity.ChatLieu;
import com.example.demodatn2.repository.ChatLieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/chat-lieu")
@RequiredArgsConstructor
public class AdminChatLieuController {

    private final ChatLieuRepository chatLieuRepository;

    @GetMapping
    public String list(Model model) {
        List<ChatLieu> chatLieus = chatLieuRepository.findAll().stream()
                .sorted(Comparator.comparing(ChatLieu::getTenChatLieu, String.CASE_INSENSITIVE_ORDER))
                .toList();
        model.addAttribute("chatLieus", chatLieus);
        return "admin/chat-lieu";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        ChatLieu chatLieu = new ChatLieu();
        chatLieu.setTrangThai("ACTIVE");
        model.addAttribute("chatLieu", chatLieu);
        return "admin/chat-lieu-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        ChatLieu chatLieu = chatLieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chất liệu không tồn tại: " + id));
        model.addAttribute("chatLieu", chatLieu);
        return "admin/chat-lieu-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ChatLieu chatLieu, RedirectAttributes redirectAttributes) {
        try {
            String tenChatLieu = chatLieu.getTenChatLieu() != null ? chatLieu.getTenChatLieu().trim() : "";
            if (tenChatLieu.isEmpty()) {
                throw new RuntimeException("Ten chat lieu khong duoc de trong.");
            }
            if (tenChatLieu.length() < 2 || tenChatLieu.length() > 100) {
                throw new RuntimeException("Ten chat lieu phai dai tu 2 den 100 ky tu.");
            }

            chatLieuRepository.findByTenChatLieuIgnoreCase(tenChatLieu)
                    .filter(existing -> !existing.getId().equals(chatLieu.getId()))
                    .ifPresent(existing -> {
                        throw new RuntimeException("Ten chat lieu da ton tai.");
                    });

            ChatLieu target = chatLieu;
            if (chatLieu.getId() != null) {
                target = chatLieuRepository.findById(chatLieu.getId())
                        .orElseThrow(() -> new RuntimeException("Chất liệu không tồn tại: " + chatLieu.getId()));
            }

            target.setTenChatLieu(tenChatLieu);
            String trangThai = chatLieu.getTrangThai() != null ? chatLieu.getTrangThai().trim().toUpperCase() : "";
            if (trangThai.isEmpty()) {
                target.setTrangThai("ACTIVE");
            } else if ("ACTIVE".equals(trangThai) || "INACTIVE".equals(trangThai)) {
                target.setTrangThai(trangThai);
            } else {
                throw new RuntimeException("Trang thai chat lieu khong hop le.");
            }

            chatLieuRepository.save(target);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu chất liệu thành công!");
            return "redirect:/admin/chat-lieu";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return chatLieu.getId() == null
                    ? "redirect:/admin/chat-lieu/add"
                    : "redirect:/admin/chat-lieu/edit/" + chatLieu.getId();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            chatLieuRepository.deleteById(id);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
