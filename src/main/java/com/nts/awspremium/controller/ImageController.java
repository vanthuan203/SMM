package com.nts.awspremium.controller;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/image")
public class ImageController {


    @GetMapping(value = "random")
    public ResponseEntity<org.springframework.core.io.Resource> random(@RequestHeader(defaultValue = "Vn") String geo) {
        try {
            Path imageDir = Paths.get("/Data/Image/"+geo).toAbsolutePath().normalize();
            // Lấy danh sách file trong thư mục
            List<Path> files = Files.list(imageDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
                    })
                    .collect(Collectors.toList());

            if (files.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Chọn ngẫu nhiên
            Path randomFile = files.get(new Random().nextInt(files.size()));
            org.springframework.core.io.Resource resource = new UrlResource(randomFile.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file ảnh.", e);
        }
    }
}
