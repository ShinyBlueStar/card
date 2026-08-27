package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.FileDownloadCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.FileDownloadDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.FileDownloadCommandJpaRepository;
import com.sample.system.card.service.domain.entity.FileDownload;
import com.sample.system.card.service.domain.ports.output.repository.FileDownloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * FileDownload Repository Implementation
 * Following DDD infrastructure patterns with adapter pattern
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileDownloadRepositoryImpl implements FileDownloadRepository {

    private final FileDownloadCommandJpaRepository jpaRepository;
    private final FileDownloadDataAccessMapper mapper;

    @Override
    public FileDownload save(FileDownload fileDownload) {
        log.debug("Saving file download record with hashId: {}", fileDownload.getHashId());
        FileDownloadCommandEntity entity = mapper.toEntity(fileDownload);
        FileDownloadCommandEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<FileDownload> findByHashId(String hashId) {
        log.debug("Finding file download by hashId: {}", hashId);
        return jpaRepository.findByHashId(hashId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<FileDownload> findByHashIdAndNotUsed(String hashId) {
        log.debug("Finding unused file download by hashId: {}", hashId);
        return jpaRepository.findByHashIdAndIsUsedFalse(hashId)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(FileDownload fileDownload) {
        jpaRepository.deleteById(fileDownload.getId());
    }
}

