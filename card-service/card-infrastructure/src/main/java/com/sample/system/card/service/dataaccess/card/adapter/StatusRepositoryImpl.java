package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.StatusCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.StatusDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.StatusCommandJpaRepository;
import com.sample.system.card.service.dataaccess.utility.CardSearchUtility;
import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.StatusRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Status Repository Implementation
 * Following DDD infrastructure patterns with adapter pattern
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatusRepositoryImpl implements StatusRepository {

    private final StatusCommandJpaRepository statusJpaRepository;
    private final StatusDataAccessMapper statusDataAccessMapper;

    @Override
    public Status save(Status status) {
        log.debug("Saving status with ID: {}", status.getId());
        var entity = statusDataAccessMapper.toEntity(status);
        var savedEntity = statusJpaRepository.save(entity);
        return statusDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Status> findById(Long id) {
        log.debug("Finding status by ID: {}", id);
        return statusJpaRepository.findById(id)
                .map(statusDataAccessMapper::toDomain);
    }

    @Override
    public Status findByCode(String code) throws CardDomainException {
        StatusCommandEntity statusCommandEntity = statusJpaRepository.findByCode(code);
        if (statusCommandEntity == null) {
            log.warn("Status not found for code: {}, returning default status", code);
            throw new CardDomainException("General Error",
                    StatusService.GENERAL_ERROR, HttpStatus.BAD_REQUEST);
        }
        return statusDataAccessMapper.toDomain(statusCommandEntity);
    }

    @Override
    public List<Status> findAll() {
        log.debug("Finding all statuses");
        return statusJpaRepository.findAll()
                .stream()
                .map(statusDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        log.debug("Counting total statuses");
        return statusJpaRepository.count();
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting status by ID: {}", id);
        statusJpaRepository.deleteById(id);
    }

    @Override
    public Page<Status> findAllStatuses(Map<String, String> searchCriteria) {
        if (searchCriteria == null) {
            searchCriteria = new java.util.HashMap<>();
        }

        Pageable pageable = PageRequest.of(
            Integer.parseInt(searchCriteria.getOrDefault("page", "0")),
            Integer.parseInt(searchCriteria.getOrDefault("size", "10"))
        );

        Page<StatusCommandEntity> entityPage = statusJpaRepository
                .findAll(getStatusSpecification(searchCriteria), pageable);

        List<Status> statuses = entityPage.getContent().stream()
            .map(statusDataAccessMapper::toDomain)
            .toList();
        log.info("status list size is: {}", statuses.size());

        return new PageImpl<>(statuses, entityPage.getPageable(), entityPage.getTotalElements());
    }

    private Specification<StatusCommandEntity> getStatusSpecification(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildStatusPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);

            String orderBy = searchCriteria.getOrDefault("orderBy", "id");
            String sortDirection = searchCriteria.getOrDefault("sort", "desc");
            if ("asc".equalsIgnoreCase(sortDirection)) {
                query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
            } else {
                query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildStatusPredicatesFromCriteria(Map<String, String> searchCriteria,
                                                               Root<StatusCommandEntity> root,
                                                               CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        // ID filter
        if (StringUtils.isNotEmpty(searchCriteria.get("id"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "id", searchCriteria.get("id"));
            if (p != null) predicates.add(p);
        }

        // Code filter
        if (StringUtils.isNotEmpty(searchCriteria.get("code"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "code", searchCriteria.get("code"));
            if (p != null) predicates.add(p);
        }

        // Description filter (LIKE search)
        if (StringUtils.isNotEmpty(searchCriteria.get("description"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "description", searchCriteria.get("description"));
            if (p != null) predicates.add(p);
        }

        // Persian description filter (LIKE search)
        if (StringUtils.isNotEmpty(searchCriteria.get("persianDescription"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "persianDescription", searchCriteria.get("persianDescription"));
            if (p != null) predicates.add(p);
        }

        return predicates;
    }
}
