package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.entity.FileDownload;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.FileDownloadService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.FileDownloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * FileDownload Service Implementation
 * Handles secure file download operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
class FileDownloadServiceImpl implements FileDownloadService {

    private final FileDownloadRepository fileDownloadRepository;

    @Override
    public Resource downloadFile(String hashId, String token, String accessLevel) throws CardDomainException {
        log.info("Attempting to download file with hashId: {}", hashId);

        // Find file download record
        FileDownload fileDownload = fileDownloadRepository.findByHashIdAndNotUsed(hashId)
                .orElseThrow(() -> {
                    log.warn("File download record not found or already used - HashId: {}", hashId);
                    return new CardDomainException(
                            "cannot find file with hashId: "+ hashId,
                            StatusService.FILE_NOT_FOUND,
                            HttpStatus.NOT_FOUND);
                });

        // Check expiration
        if (fileDownload.getExpiresAt() != null &&
            fileDownload.getExpiresAt().before(Timestamp.valueOf(LocalDateTime.now()))) {
            log.warn("File download expired - HashId: {}, ExpiresAt: {}",
                    hashId, fileDownload.getExpiresAt());
            throw new CardDomainException("Download link is expired",
                    StatusService.FILE_EXPIRED,
                    HttpStatus.GONE);
        }

        Path filePath = Paths.get(fileDownload.getFilePath());
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            log.error("File not found on disk - Path: {}", fileDownload.getFilePath());
            throw new CardDomainException(
                    "Cannot find file",
                    StatusService.FILE_NOT_FOUND,
                    HttpStatus.NOT_FOUND);
        }

        log.info("File download successful - HashId: {}, FileName: {}",
                hashId, fileDownload.getFileName());

        return new FileSystemResource(filePath.toFile());
    }

    @Async
    @Override
    public void deleteFile(String hashId) throws CardDomainException {
        log.info("Attempting to delete file with hashId: {}", hashId);

        // 1. Find file download record
        FileDownload fileDownload = fileDownloadRepository.findByHashId(hashId)
                .orElseThrow(() -> {
                    log.warn("File download record not found - HashId: {}", hashId);
                    return new CardDomainException(
                            "Cannot find file record.",
                            StatusService.FILE_NOT_FOUND,
                            HttpStatus.NOT_FOUND
                    );
                });

        // 2. Resolve file path
        Path filePath = Paths.get(fileDownload.getFilePath());

        // 3. Delete physical file if exists
        try {
            if (Files.exists(filePath)) {
                if (Files.isRegularFile(filePath)) {
                    Files.delete(filePath);
                    log.info("Physical file deleted successfully - Path: {}", filePath);
                } else {
                    log.warn("Path exists but is not a regular file - Path: {}", filePath);
                }
            } else {
                log.warn("Physical file does not exist on disk - Path: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Failed to delete physical file - Path: {}", filePath, e);
            throw new CardDomainException(
                    "error in delete file in filePath: "+ filePath,
                    StatusService.FILE_DELETE_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        // 4. Delete database record
        try {
            fileDownloadRepository.delete(fileDownload);
            log.info("File download record deleted successfully - HashId: {}", hashId);
        } catch (Exception e) {
            log.error("Failed to delete file download record from database - HashId: {}", hashId, e);
            throw new CardDomainException(
                    "Error in delete file from DataBase",
                    StatusService.FILE_DELETE_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

}

