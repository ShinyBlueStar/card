package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.Reason;
import com.sample.system.card.service.domain.enums.ReasonGroup;
import com.sample.system.card.service.domain.exception.CardDomainException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReasonRepository {
    Reason save(Reason reason);
    Optional<Reason> findById(Long id);
    Optional<Reason> findByCode(Integer code);
    Page<Reason> findAll(Map<String, String> mapParameter) throws CardDomainException;
    List<Reason> findByGroupId(ReasonGroup groupId);
    boolean existsByCode(Integer code);
}

