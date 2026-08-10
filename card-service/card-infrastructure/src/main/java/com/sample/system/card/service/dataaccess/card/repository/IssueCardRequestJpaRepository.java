package com.sample.system.card.service.dataaccess.card.repository;

import com.sample.system.card.service.domain.enums.CardRequestStatus;
import com.sample.system.card.service.dataaccess.card.entity.command.requests.IssueCardRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for IssueCardRequestEntity (صدور کارت منطقی)
 */
@Repository
public interface IssueCardRequestJpaRepository extends JpaRepository<IssueCardRequestEntity, Long> {

    List<IssueCardRequestEntity> findByNationalIdAndCaseNumberAndRequestStatusIn(
            String nationalId, String caseNumber, List<CardRequestStatus> requestStatus);
}
