package com.example.demodatn2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final String UPLOAD_DIR = "src/main/resources/static/images/products/";

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return badRequest("File is empty");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                return badRequest("Invalid filename");
            }
            originalFilename = originalFilename.replaceAll("\\s+", "_");

            Path filePath = uploadPath.resolve(originalFilename);
            if (Files.exists(filePath)) {
                if (isSameContent(file, filePath)) {
                    return success("/images/products/" + originalFilename);
                }
                String newName = renameFile(originalFilename);
                filePath = uploadPath.resolve(newName);
                originalFilename = newName;
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return success("/images/products/" + originalFilename);
        } catch (IOException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", false);
            body.put("message", "Failed to upload: " + e.getMessage());
            return ResponseEntity.internalServerError().body(body);
        }
    }

    private ResponseEntity<Map<String, Object>> success(String url) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("url", url);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        return ResponseEntity.badRequest().body(body);
    }

    private boolean isSameContent(MultipartFile file, Path existingFilePath) throws IOException {
        if (file.getSize() != Files.size(existingFilePath)) {
            return false;
        }

        String newFileHash = DigestUtils.md5DigestAsHex(file.getInputStream());
        String existingFileHash;
        try (InputStream is = Files.newInputStream(existingFilePath)) {
            existingFileHash = DigestUtils.md5DigestAsHex(is);
        }

        return newFileHash.equals(existingFileHash);
    }

    private String renameFile(String originalName) {
        String name = originalName;
        String ext = "";
        int dotIndex = originalName.lastIndexOf(".");
        if (dotIndex != -1) {
            name = originalName.substring(0, dotIndex);
            ext = originalName.substring(dotIndex);
        }
        return name + "_" + System.currentTimeMillis() + ext;
    }
}
