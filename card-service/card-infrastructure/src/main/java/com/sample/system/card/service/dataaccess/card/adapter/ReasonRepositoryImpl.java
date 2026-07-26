package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.*;
import com.sample.system.card.service.dataaccess.card.mapper.ReasonDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.ReasonJpaRepository;
import com.sample.system.card.service.dataaccess.utility.CardSearchUtility;
import com.sample.system.card.service.domain.entity.Reason;
import com.sample.system.card.service.domain.enums.ReasonGroup;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.exception.InvalidInputParameterException;
import com.sample.system.card.service.domain.ports.output.repository.ReasonRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReasonRepositoryImpl implements ReasonRepository {

    private final ReasonJpaRepository reasonJpaRepository;
    private final ReasonDataAccessMapper mapper;
    private final ReasonDataAccessMapper reasonDataAccessMapper;

    @Override
    public Reason save(Reason reason) {
        log.info("Saving reason with code: {}", reason.getCode());
        ReasonCommandEntity entity = mapper.toEntity(reason);
        ReasonCommandEntity saved = reasonJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Reason> findById(Long id) {
        return reasonJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Reason> findByCode(Integer code) {
        return reasonJpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public Page<Reason> findAll(Map<String, String> searchCriteria) throws CardDomainException {
        if (searchCriteria == null) {
            searchCriteria = new java.util.HashMap<>();
        }

        Pageable pageable = PageRequest.of(
                Integer.parseInt(searchCriteria.getOrDefault("page", "0")),
                Integer.parseInt(searchCriteria.getOrDefault("size", "10"))
        );

        Page<ReasonCommandEntity> entityPage = reasonJpaRepository.findAll(
                getReasonSpecification(searchCriteria), pageable);

        List<Reason> reasons = entityPage.getContent().stream()
                .map(reasonDataAccessMapper::toDomain)
                .toList();
        log.info("card profile list size is: {}" ,  reasons.size());

        return new PageImpl<>(reasons, entityPage.getPageable(), entityPage.getTotalElements());
    }

    private Specification<ReasonCommandEntity> getReasonSpecification(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
            } else {
                query.distinct(true);
            }
            List<Predicate> predicates = buildReasonPredicatesFromCriteria(searchCriteria, root, query, criteriaBuilder);
            // Set ordering
            String orderBy = searchCriteria.getOrDefault("orderBy", "id");
            String sortDirection = searchCriteria.getOrDefault("sort", "desc");
            setReasonOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void setReasonOrder(CriteriaQuery<?> query, String orderBy, String sortDirection,
                                     CriteriaBuilder criteriaBuilder, Root<ReasonCommandEntity> root) {
        try {
            if ("asc".equalsIgnoreCase(sortDirection)) {
                query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
            } else {
                query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
            }
        } catch (Exception e) {
            log.warn("Invalid orderBy field: {}, using default ordering", orderBy);
            query.orderBy(criteriaBuilder.desc(root.get("id")));
        }
    }

    private List<Predicate> buildReasonPredicatesFromCriteria(Map<String, String> searchCriteria,
                                                                   Root<ReasonCommandEntity> root,
                                                                   CriteriaQuery<?> query,
                                                                   CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        // ID filter
        if (StringUtils.isNotEmpty(searchCriteria.get("id"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "id", searchCriteria.get("id"));
            if (p != null) predicates.add(p);
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("profileName"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "profileName", searchCriteria.get("profileName"));
            if (p != null) predicates.add(p);
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("productCode"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "productCode", searchCriteria.get("productCode"));
            if (p != null) predicates.add(p);
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("groupId"))) {
            try {
                ReasonGroup group = ReasonGroup.fromCode(Integer.parseInt(searchCriteria.get("groupId")));
                predicates.add(criteriaBuilder.equal(root.get("groupId"), group));
            } catch (NumberFormatException | InvalidInputParameterException e) {
                log.debug("Invalid groupId in search criteria: {}", searchCriteria.get("groupId"));
            }
        }
        return predicates;
    }

    @Override
    public List<Reason> findByGroupId(ReasonGroup groupId) {
        if (groupId == null) return List.of();
        return reasonJpaRepository.findByGroupId(groupId).stream()
                .map(reasonDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCode(Integer code) {
        return reasonJpaRepository.existsByCode(code);
    }
}

