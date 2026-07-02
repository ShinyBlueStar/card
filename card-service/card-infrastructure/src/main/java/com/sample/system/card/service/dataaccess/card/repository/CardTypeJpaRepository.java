package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.CardTypeCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for CardType operations
 * Following Repository pattern
 */
@Repository
public interface CardTypeJpaRepository extends JpaRepository<CardTypeCommandEntity, Long>, JpaSpecificationExecutor<CardTypeCommandEntity> {

    /**
     * Find card type by code
     */
    Optional<CardTypeCommandEntity> findByCode(Integer code);

    /**
     * Find all active card types
     */
    @Query("SELECT c FROM CardTypeCommandEntity c WHERE c.isActive = true")
    List<CardTypeCommandEntity> findActiveCardTypes();

    /**
     * Check if card type exists by code
     */
    boolean existsByCode(Integer code);

    /**
     * Find card types by name containing
     */
    @Query("SELECT c FROM CardTypeCommandEntity c WHERE c.name LIKE %:name%")
    List<CardTypeCommandEntity> findByNameContaining(@Param("name") String name);
}
