package com.example.demodatn2.controller;

import com.example.demodatn2.dto.*;
import com.example.demodatn2.service.ReturnRequestService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnRequestApiController {
    private static final Path RETURN_IMAGE_UPLOAD_DIR = Paths.get("src/main/resources/static/images/returns");

    private final ReturnRequestService returnRequestService;
    private final ObjectMapper objectMapper;

    @GetMapping("/orders/{orderId}/items")
    public ResponseEntity<ApiResponse<List<ReturnOrderItemDTO>>> getReturnableItems(@PathVariable Integer orderId,
                                                                                    HttpSession session) {
        var loginUser = (com.example.demodatn2.dto.TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Bạn cần đăng nhập."));
        }
        return ResponseEntity.ok(ApiResponse.ok(returnRequestService.getReturnableItems(orderId, loginUser.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnRequestResponseDTO>> createReturnRequest(@RequestParam Integer orderId,
                                                                                    @RequestParam String reason,
                                                                                    @RequestParam(required = false) String description,
                                                                                    @RequestParam String refundMethod,
                                                                                    @RequestParam String itemsJson,
                                                                                    @RequestParam("images") List<MultipartFile> images,
                                                                                    HttpSession session) {
        var loginUser = (com.example.demodatn2.dto.TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Bạn cần đăng nhập."));
        }

        List<String> imageUrls = List.of();
        try {
            List<ReturnRequestItemDTO> items = objectMapper.readValue(itemsJson, new TypeReference<>() {});
            imageUrls = saveReturnImages(images);
            var request = returnRequestService.createRequest(orderId, loginUser.getId(), reason, description, refundMethod, items, imageUrls);
            return ResponseEntity.ok(ApiResponse.ok("Yêu cầu trả hàng đã được gửi.", returnRequestService.toResponse(request)));
        } catch (Exception e) {
            deleteReturnImages(imageUrls);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(@PathVariable Integer id,
                                                            @RequestBody ReturnStatusUpdateDTO dto,
                                                            HttpSession session) {
        if (!isAdminOrStaff(session)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Bạn không có quyền xử lý yêu cầu trả hàng."));
        }
        try {
            Integer actorId = ((com.example.demodatn2.dto.TaiKhoanDTO) session.getAttribute("LOGIN_USER")).getId();
            returnRequestService.updateStatus(
                    id,
                    dto.getStatus(),
                    dto.getNote(),
                    actorId,
                    dto.getRefundAmount(),
                    dto.getRefundMethod(),
                    dto.getRefundTransactionCode()
            );
            return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật trạng thái."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/items/{detailId}/inspection")
    public ResponseEntity<ApiResponse<String>> updateInspection(@PathVariable Integer detailId,
                                                                @RequestBody ReturnInspectionUpdateDTO dto,
                                                                HttpSession session) {
        if (!isAdminOrStaff(session)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Bạn không có quyền kiểm định hàng trả."));
        }
        try {
            Integer actorId = ((com.example.demodatn2.dto.TaiKhoanDTO) session.getAttribute("LOGIN_USER")).getId();
            returnRequestService.updateInspection(detailId, dto.getInspectionStatus(), dto.getNote(), actorId);
            return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật kiểm định."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private List<String> saveReturnImages(List<MultipartFile> images) throws Exception {
        if (images == null || images.isEmpty()) {
            throw new RuntimeException("Vui lòng upload ảnh minh chứng.");
        }

        Files.createDirectories(RETURN_IMAGE_UPLOAD_DIR);
        List<String> urls = new ArrayList<>();
        try {
            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) {
                    continue;
                }
                String contentType = image.getContentType();
                if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                    throw new RuntimeException("File minh chứng phải là ảnh.");
                }
                String originalName = StringUtils.cleanPath(image.getOriginalFilename() != null ? image.getOriginalFilename() : "");
                String ext = "";
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
                    ext = originalName.substring(dotIndex).toLowerCase();
                }
                if (!Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(ext)) {
                    throw new RuntimeException("Ảnh minh chứng chỉ hỗ trợ JPG, PNG, WEBP hoặc GIF.");
                }
                String fileName = UUID.randomUUID() + ext;
                Path target = RETURN_IMAGE_UPLOAD_DIR.resolve(fileName).normalize();
                if (!target.startsWith(RETURN_IMAGE_UPLOAD_DIR.normalize())) {
                    throw new RuntimeException("Tên file ảnh không hợp lệ.");
                }
                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }
                urls.add("/images/returns/" + fileName);
            }
        } catch (Exception e) {
            deleteReturnImages(urls);
            throw e;
        }
        if (urls.isEmpty()) {
            throw new RuntimeException("Vui lòng upload ảnh minh chứng.");
        }
        return urls;
    }

    private void deleteReturnImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        for (String imageUrl : imageUrls) {
            if (imageUrl == null || !imageUrl.startsWith("/images/returns/")) {
                continue;
            }
            Path target = RETURN_IMAGE_UPLOAD_DIR.resolve(imageUrl.substring("/images/returns/".length())).normalize();
            if (!target.startsWith(RETURN_IMAGE_UPLOAD_DIR.normalize())) {
                continue;
            }
            try {
                Files.deleteIfExists(target);
            } catch (Exception ignored) {
                // Best effort cleanup for failed return requests.
            }
        }
    }

    private boolean isAdminOrStaff(HttpSession session) {
        Object rolesObj = session.getAttribute("ROLES");
        if (!(rolesObj instanceof List<?> roles)) {
            return false;
        }
        return roles.contains("ADMIN") || roles.contains("STAFF");
    }
}
