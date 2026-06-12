package org.controllers.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.controllers.config.StorageConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path path;
    private final StorageConfig storageConfig;

    public FileStorageService(StorageConfig storageConfig) throws IOException {
        this.storageConfig = storageConfig;
        this.path = Paths.get(storageConfig.getUploadDir());

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException();
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null) {
            throw new IllegalArgumentException();
        }

        String fileExtension = getFileExtension(originalFileName);

        if (!isValidExtension(fileExtension)) {
            throw new IllegalArgumentException();
        }

        String storedFileName = generateUniqueId(fileExtension);

        Path fileDistinationPath = path.resolve(storedFileName);

        try(InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, fileDistinationPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return fileDistinationPath.toString();
    }

    private String generateUniqueId(String extension) {
        return UUID.randomUUID().toString() + '.' + extension;
    }

    public String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    private boolean isValidExtension(String extension) {
        return storageConfig.getAllowedExtensions() != null &&
                storageConfig.getAllowedExtensions().contains(extension.toLowerCase());
    }

    public InputStream getFileContent(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException();
        }

        return Files.newInputStream(path);
    }

    public boolean deleteFile(String filename) {
        try {
            Path path = Paths.get(filename);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
