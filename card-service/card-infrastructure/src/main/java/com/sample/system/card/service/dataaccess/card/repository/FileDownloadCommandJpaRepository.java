package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.FileDownloadCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileDownloadCommandJpaRepository extends JpaRepository<FileDownloadCommandEntity, Long> {

    Optional<FileDownloadCommandEntity> findByHashId(String hashId);

    Optional<FileDownloadCommandEntity> findByHashIdAndIsUsedFalse(String hashId);
}