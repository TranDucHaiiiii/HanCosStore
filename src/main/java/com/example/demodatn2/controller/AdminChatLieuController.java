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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/chat-lieu")
@RequiredArgsConstructor
public class AdminChatLieuController {

    private final ChatLieuRepository chatLieuRepository;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping
        public String list(@RequestParam(required = false) String q,
                   @RequestParam(defaultValue = "1") int page,
                   @RequestParam(defaultValue = "10") int size,
                       Model model) {
        int safePage = Math.max(page, 1);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        String keyword = q == null ? "" : q.trim().toLowerCase();

        List<ChatLieu> allChatLieus = chatLieuRepository.findAll().stream()
            .filter(cl -> keyword.isEmpty()
                || (cl.getTenChatLieu() != null && cl.getTenChatLieu().toLowerCase().contains(keyword)))
                .sorted(Comparator.comparing(ChatLieu::getTenChatLieu, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int totalElements = allChatLieus.size();
        int totalPages = Math.max((int) Math.ceil((double) totalElements / safeSize), 1);
        if (safePage > totalPages) {
            safePage = totalPages;
        }

        int start = Math.min((safePage - 1) * safeSize, totalElements);
        int end = Math.min(start + safeSize, totalElements);
        long fromItem = totalElements == 0 ? 0 : (long) start + 1;
        long toItem = totalElements == 0 ? 0 : end;

        model.addAttribute("chatLieus", allChatLieus.subList(start, end));
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("fromItem", fromItem);
        model.addAttribute("toItem", toItem);
        model.addAttribute("q", q);
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
