package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.FileDownload;

import java.util.Optional;

public interface FileDownloadRepository {

    /**
     * Save a file download record
     */
    FileDownload save(FileDownload fileDownload);

    /**
     * Find file download by hash ID
     */
    Optional<FileDownload> findByHashId(String hashId);

    /**
     * Find file download by hash ID that is not used
     */
    Optional<FileDownload> findByHashIdAndNotUsed(String hashId);

    void delete(FileDownload fileDownload);
}

