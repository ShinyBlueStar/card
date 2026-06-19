package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.CardCategoryCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for CardCategory operations
 * Following Repository pattern
 */
@Repository
public interface CardCategoryJpaRepository extends JpaRepository<CardCategoryCommandEntity, Long>, JpaSpecificationExecutor<CardCategoryCommandEntity> {

    /**
     * Find card category by code
     */
    Optional<CardCategoryCommandEntity> findByCode(Integer code);

    /**
     * Find all active card categories
     */
    @Query("SELECT c FROM CardCategoryCommandEntity c WHERE c.isActive = true")
    List<CardCategoryCommandEntity> findActiveCardCategories();

    boolean existsByCode(Integer code);

    /**
     * Find card categories by name containing
     */
    @Query("SELECT c FROM CardCategoryCommandEntity c WHERE c.name LIKE %:name%")
    List<CardCategoryCommandEntity> findByNameContaining(@Param("name") String name);
}
