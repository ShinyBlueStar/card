package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.CardTypeCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.CardTypeDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.CardTypeJpaRepository;
import com.sample.system.card.service.dataaccess.utility.CardSearchUtility;
import com.sample.system.card.service.domain.entity.CardType;
import com.sample.system.card.service.domain.ports.output.repository.CardTypeRepository;
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
 * CardType Repository Implementation
 * Following Adapter pattern for infrastructure layer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardTypeRepositoryImpl implements CardTypeRepository {

    private final CardTypeJpaRepository cardTypeJpaRepository;
    private final CardTypeDataAccessMapper cardTypeDataAccessMapper;

    @Override
    public CardType save(CardType cardType) {
        CardTypeCommandEntity entity = cardTypeDataAccessMapper.toEntity(cardType);
        CardTypeCommandEntity savedEntity = cardTypeJpaRepository.save(entity);
        return cardTypeDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CardType> findById(Long id) {
        return cardTypeJpaRepository.findById(id)
                .map(cardTypeDataAccessMapper::toDomain);
    }

    @Override
    public Optional<CardType> findByCode(Integer code) {
        return cardTypeJpaRepository.findByCode(code)
                .map(cardTypeDataAccessMapper::toDomain);
    }

    @Override
    public List<CardType> findActiveCardTypes() {
        return cardTypeJpaRepository.findActiveCardTypes()
                .stream()
                .map(cardTypeDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public List<CardType> findAll() {
        return cardTypeJpaRepository.findAll()
                .stream()
                .map(cardTypeDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCode(Integer code) {
        return cardTypeJpaRepository.existsByCode(code);
    }

    @Override
    public long count() {
        return cardTypeJpaRepository.count();
    }

    @Override
    public void deleteById(Long id) {
        cardTypeJpaRepository.deleteById(id);
    }

    @Override
    public Page<CardType> findAllTypes(Map<String, String> searchCriteria) {
        if (searchCriteria == null) {
            searchCriteria = new java.util.HashMap<>();
        }

        Pageable pageable = PageRequest.of(
            Integer.parseInt(searchCriteria.getOrDefault("page", "0")),
            Integer.parseInt(searchCriteria.getOrDefault("size", "10"))
        );

        Page<CardTypeCommandEntity> entityPage = cardTypeJpaRepository
                .findAll(getCardTypeSpecification(searchCriteria), pageable);

        List<CardType> types = entityPage.getContent().stream()
            .map(cardTypeDataAccessMapper::toDomain)
            .toList();
        log.info("card type list size is: {}", types.size());

        return new PageImpl<>(types, entityPage.getPageable(), entityPage.getTotalElements());
    }

    private Specification<CardTypeCommandEntity> getCardTypeSpecification(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildCardTypePredicatesFromCriteria(searchCriteria, root, criteriaBuilder);

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

    private List<Predicate> buildCardTypePredicatesFromCriteria(Map<String, String> searchCriteria,
                                                                 Root<CardTypeCommandEntity> root,
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
