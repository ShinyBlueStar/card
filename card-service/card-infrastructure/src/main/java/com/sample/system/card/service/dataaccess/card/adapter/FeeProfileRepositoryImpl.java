package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.FeeProfileCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.FeeProfileDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.FeeProfileCommandJpaRepository;
import com.sample.system.card.service.dataaccess.utility.CardSearchUtility;
import com.sample.system.card.service.domain.entity.FeeProfile;
import com.sample.system.card.service.domain.ports.output.repository.FeeProfileRepository;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class FeeProfileRepositoryImpl implements FeeProfileRepository {
    private final FeeProfileCommandJpaRepository feeProfileCommandJpaRepository;
    private final FeeProfileDataAccessMapper feeProfileDataAccessMapper;
    @Override
    public FeeProfile save(FeeProfile feeProfile) {
        return null;
    }

    @Override
    public Optional<FeeProfile> findById(Long id) {
        return feeProfileCommandJpaRepository.findById(id).map(feeProfileDataAccessMapper::toDomain);
    }

    @Override
    public Optional<FeeProfile> findByName(String name) {
        return Optional.empty();
    }

    @Override
    public List<FeeProfile> findAll() {
        return List.of();
    }

    @Override
    public List<FeeProfile> findActiveFeeProfiles() {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public boolean existsByName(String name) {
        return feeProfileCommandJpaRepository.findAll().stream()
                .anyMatch(fp -> fp.getName() != null && fp.getName().equals(name));
    }

    @Override
    public Page<FeeProfile> findAllProfiles(Map<String, String> searchCriteria) {
        if (searchCriteria == null) {
            searchCriteria = new java.util.HashMap<>();
        }

        Pageable pageable = PageRequest.of(
            Integer.parseInt(searchCriteria.getOrDefault("page", "0")),
            Integer.parseInt(searchCriteria.getOrDefault("size", "10"))
        );

        Page<FeeProfileCommandEntity> entityPage = feeProfileCommandJpaRepository
                .findAll(getFeeProfileSpecification(searchCriteria), pageable);

        List<FeeProfile> profiles = entityPage.getContent().stream()
            .map(feeProfileDataAccessMapper::toDomain)
            .toList();
        log.info("fee profile list size is: {}", profiles.size());

        return new PageImpl<>(profiles, entityPage.getPageable(), entityPage.getTotalElements());
    }

    private Specification<FeeProfileCommandEntity> getFeeProfileSpecification(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildFeeProfilePredicatesFromCriteria(searchCriteria, root, criteriaBuilder);

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

    private List<Predicate> buildFeeProfilePredicatesFromCriteria(Map<String, String> searchCriteria,
                                                                   Root<FeeProfileCommandEntity> root,
                                                                   CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotEmpty(searchCriteria.get("id"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "id", searchCriteria.get("id"));
            if (p != null) predicates.add(p);
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("name"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "name", searchCriteria.get("name"));
            if (p != null) predicates.add(p);
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("isActive"))) {
            Boolean isActive = CardSearchUtility.parseBooleanValue(searchCriteria.get("isActive"));
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }
        }

        return predicates;
    }
}
