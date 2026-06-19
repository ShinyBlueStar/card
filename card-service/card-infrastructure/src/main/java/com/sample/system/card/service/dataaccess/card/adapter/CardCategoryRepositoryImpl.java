package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.CardCategoryCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.CardCategoryDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.CardCategoryJpaRepository;
import com.sample.system.card.service.dataaccess.utility.CardSearchUtility;
import com.sample.system.card.service.domain.entity.CardCategory;
import com.sample.system.card.service.domain.ports.output.repository.CardCategoryRepository;
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

/**
 * CardCategory Repository Implementation
 * Following Adapter pattern for infrastructure layer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardCategoryRepositoryImpl implements CardCategoryRepository {

    private final CardCategoryJpaRepository cardCategoryJpaRepository;
    private final CardCategoryDataAccessMapper cardCategoryDataAccessMapper;

    @Override
    public CardCategory save(CardCategory cardCategory) {
        CardCategoryCommandEntity entity = cardCategoryDataAccessMapper.toEntity(cardCategory);
        CardCategoryCommandEntity savedEntity = cardCategoryJpaRepository.save(entity);
        return cardCategoryDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CardCategory> findById(Long id) {
        return cardCategoryJpaRepository.findById(id)
                .map(cardCategoryDataAccessMapper::toDomain);
    }

    @Override
    public Optional<CardCategory> findByCode(Integer code) {
        return cardCategoryJpaRepository.findByCode(code)
                .map(cardCategoryDataAccessMapper::toDomain);
    }

    @Override
    public List<CardCategory> findActiveCardCategories() {
        return cardCategoryJpaRepository.findActiveCardCategories()
                .stream()
                .map(cardCategoryDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public List<CardCategory> findAll() {
        return cardCategoryJpaRepository.findAll()
                .stream()
                .map(cardCategoryDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCode(Integer code) {
        return cardCategoryJpaRepository.existsByCode(code);
    }

    @Override
    public long count() {
        return cardCategoryJpaRepository.count();
    }

    @Override
    public void deleteById(Long id) {
        cardCategoryJpaRepository.deleteById(id);
    }

    @Override
    public Page<CardCategory> findAllCategories(Map<String, String> searchCriteria) {
        if (searchCriteria == null) {
            searchCriteria = new java.util.HashMap<>();
        }

        Pageable pageable = PageRequest.of(
            Integer.parseInt(searchCriteria.getOrDefault("page", "0")),
            Integer.parseInt(searchCriteria.getOrDefault("size", "10"))
        );

        Page<CardCategoryCommandEntity> entityPage = cardCategoryJpaRepository
                .findAll(getCardCategorySpecification(searchCriteria), pageable);

        List<CardCategory> categories = entityPage.getContent().stream()
            .map(cardCategoryDataAccessMapper::toDomain)
            .toList();
        log.info("card category list size is: {}", categories.size());

        return new PageImpl<>(categories, entityPage.getPageable(), entityPage.getTotalElements());
    }

    private Specification<CardCategoryCommandEntity> getCardCategorySpecification(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildCardCategoryPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);

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

    private List<Predicate> buildCardCategoryPredicatesFromCriteria(Map<String, String> searchCriteria,
                                                                     Root<CardCategoryCommandEntity> root,
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

        if (StringUtils.isNotEmpty(searchCriteria.get("code"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "code", searchCriteria.get("code"));
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
