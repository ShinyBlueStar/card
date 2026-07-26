package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.ReasonCommandEntity;
import com.sample.system.card.service.domain.enums.ReasonGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReasonJpaRepository extends
        JpaRepository<ReasonCommandEntity, Long>, JpaSpecificationExecutor<ReasonCommandEntity> {
    Optional<ReasonCommandEntity> findByCode(Integer code);
    List<ReasonCommandEntity> findByGroupId(ReasonGroup groupId);
    boolean existsByCode(Integer code);
}