package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadDirectory =
            Paths.get("uploads/pod").toAbsolutePath().normalize();

    public FileStorageServiceImpl() {
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create upload directory", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String folder) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {

            Path folderPath = uploadDirectory
                    .resolve(folder)
                    .normalize();

            Files.createDirectories(folderPath);

            String originalFileName = file.getOriginalFilename();

            String extension = "";

            if (originalFileName != null &&
                    originalFileName.contains(".")) {

                extension = originalFileName.substring(
                        originalFileName.lastIndexOf("."));
            }

            String fileName =
                    UUID.randomUUID() + extension;

            Path targetPath =
                    folderPath.resolve(fileName).normalize();

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            log.info("POD file stored: {}", targetPath);

            return "/uploads/pod/"
                    + folder
                    + "/"
                    + fileName;

        } catch (IOException e) {

            log.error("Failed to store POD file", e);

            throw new RuntimeException(
                    "Could not store file", e);
        }
    }
}
