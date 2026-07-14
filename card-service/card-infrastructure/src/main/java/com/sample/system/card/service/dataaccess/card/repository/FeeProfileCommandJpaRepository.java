package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.FeeProfileCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeProfileCommandJpaRepository extends JpaRepository<FeeProfileCommandEntity, Long>, JpaSpecificationExecutor<FeeProfileCommandEntity> {
}
