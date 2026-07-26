package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.StatusCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusCommandJpaRepository extends JpaRepository<StatusCommandEntity, Long>, JpaSpecificationExecutor<StatusCommandEntity> {

    StatusCommandEntity findByCode(String code);

}
