package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.CardCommandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardCommandJpaRepository extends JpaRepository<CardCommandEntity, Long>, JpaSpecificationExecutor<CardCommandEntity> {

    @Query("SELECT COUNT(c) > 0 FROM CardCommandEntity c WHERE c.cardProfile.id = :profileId")
    boolean existsByProfileId(@Param("profileId") Long profileId);

    boolean existsByPan(String pan);

    Optional<CardCommandEntity> findByPan(String pan);

    @Override
    @EntityGraph(value = "Card.withProfileAndCustomer", type = EntityGraph.EntityGraphType.LOAD)
    Page<CardCommandEntity> findAll(Specification<CardCommandEntity> spec, Pageable pageable);

}
