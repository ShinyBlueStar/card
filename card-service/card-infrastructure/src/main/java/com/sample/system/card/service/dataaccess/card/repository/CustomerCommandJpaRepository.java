package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.dataaccess.card.entity.command.CustomerCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerCommandJpaRepository extends JpaRepository<CustomerCommandEntity, Long> {

    Optional<CustomerCommandEntity> findByNationalId(String nationalId);
}

