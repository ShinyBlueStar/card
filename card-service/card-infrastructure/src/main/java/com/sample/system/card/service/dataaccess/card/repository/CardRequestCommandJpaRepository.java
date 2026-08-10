package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.domain.enums.CardRequestStatus;
import com.sample.system.card.service.dataaccess.card.entity.command.CardRequestCommandEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRequestCommandJpaRepository extends JpaRepository<CardRequestCommandEntity, Long>, JpaSpecificationExecutor<CardRequestCommandEntity> {

    boolean existsByIdAndRequestStatus(Long id, CardRequestStatus requestStatus);

    @EntityGraph(attributePaths = {"card"})
    Optional<CardRequestCommandEntity> findById(Long id);
}
