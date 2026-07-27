package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.CardCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CustomerCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardProfileCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardTypeCategoryCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.CardDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.CardCommandJpaRepository;
import com.sample.system.card.service.dataaccess.utility.CardSearchUtility;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.enums.CardStatus;
import com.sample.system.card.service.domain.ports.output.repository.CardRepository;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
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

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepository {

    private final CardCommandJpaRepository cardJpaRepository;
    private final CardDataAccessMapper cardDataAccessMapper;

    @Override
    public Card save(Card card) {
        log.debug("Saving card with Card Number(Pan): {}", card.getMaskedPan() != null ? card.getMaskedPan() : PanMaskingUtil.maskPan(card.getPan()));
        var entity = cardDataAccessMapper.toEntity(card);
        var savedEntity = cardJpaRepository.save(entity);
        if (savedEntity.getTrack() != null) {
            log.debug("TrackCommandEntity associated with Card - Track ID: {}",
                    savedEntity.getTrack().getId());
        } else {
            log.debug("No TrackCommandEntity associated with Card");
        }
        return cardDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Card> findById(Long cardId) {
        log.debug("Finding card by ID: {}", cardId);
        return cardJpaRepository.findById(cardId)
                .map(cardDataAccessMapper::toDomain);
    }

    @Override
    public List<Card> findAll() {
        log.debug("Finding all cards");
        return cardJpaRepository.findAll()
                .stream()
                .map(cardDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public List<Card> findPrintableCards() {
        log.debug("Finding printable cards (cards with tracks and customer data)");
        // Fetch cards with eager loading of tracks and customer for export
        // This is a CQRS-optimized query for read model
        List<CardCommandEntity> entities = cardJpaRepository.findAll();
        return entities.stream()
                .filter(entity -> entity.getTrack() != null) // Must have track data
                .filter(entity -> entity.getCustomer() != null) // Must have customer data
                .map(cardDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Card> findAllCards(Map<String, String> mapParameter) {
        if (mapParameter == null) {
            mapParameter = new java.util.HashMap<>();
        }

        Pageable pageable = PageRequest.of(
            Integer.parseInt(mapParameter.getOrDefault("page", "0")),
            Integer.parseInt(mapParameter.getOrDefault("size", "10"))
        );

        Page<CardCommandEntity> entityPage = cardJpaRepository
                .findAll(getCardSpecification(mapParameter), pageable);
        List<Card> cards = entityPage.getContent().stream()
            .map(cardDataAccessMapper::toDomain)
            .toList();

        return new PageImpl<>(cards, pageable, entityPage.getTotalElements());
    }

    /**
     * Build Card specification from search criteria
     */
    private Specification<CardCommandEntity> getCardSpecification(Map<String, String> searchCriteria) {
        return (root, query, cb) -> {
            // Use distinct to avoid duplicate rows when using joins
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                // Don't use distinct for count queries
            } else {
                query.distinct(true);
            }

            List<Predicate> predicates = buildCardPredicatesFromCriteria(searchCriteria, root, cb);
            // Ordering
            String orderBy = searchCriteria.getOrDefault("orderBy", "id");
            String sort = searchCriteria.getOrDefault("sort", "desc");

            if ("asc".equalsIgnoreCase(sort)) {
                query.orderBy(cb.asc(root.get(orderBy)));
            } else {
                query.orderBy(cb.desc(root.get(orderBy)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildCardPredicatesFromCriteria(Map<String, String> searchCriteria,
                                                           Root<CardCommandEntity> root,
                                                           CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        // Joins for related entities
        Join<CardCommandEntity, CustomerCommandEntity> customerJoin = root.join("customer", JoinType.LEFT);
        Join<CardCommandEntity, CardProfileCommandEntity> profileJoin = root.join("cardProfile", JoinType.LEFT);

        // ID filter
        if (StringUtils.isNotEmpty(searchCriteria.get("id"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "id", searchCriteria.get("id"));
            if (p != null) predicates.add(p);
        }

        // Card number filter (LIKE search)
        if (StringUtils.isNotEmpty(searchCriteria.get("pan"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "pan", searchCriteria.get("pan"));
            if (p != null) predicates.add(p);
        }

        // Holder name filter (from Customer.firstName)
        if (StringUtils.isNotEmpty(searchCriteria.get("holderName"))) {
            String holderName = searchCriteria.get("holderName").trim();
            String pattern = "%" + holderName + "%";
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(customerJoin.get("firstName").as(String.class)),
                    criteriaBuilder.lower(criteriaBuilder.literal(pattern))
            ));
        }

        // Holder family filter (from Customer.lastName)
        if (StringUtils.isNotEmpty(searchCriteria.get("holderFamily"))) {
            String holderFamily = searchCriteria.get("holderFamily").trim();
            String pattern = "%" + holderFamily + "%";
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(customerJoin.get("lastName").as(String.class)),
                    criteriaBuilder.lower(criteriaBuilder.literal(pattern))
            ));
        }

        // National ID filter (from Customer.nationalId)
        if (StringUtils.isNotEmpty(searchCriteria.get("nationalId"))) {
            predicates.add(criteriaBuilder.equal(
                    customerJoin.get("nationalId"),
                    searchCriteria.get("nationalId").trim()
            ));
        }

        // Issuing bank filter (by profile.issuingBank.id)
        if (StringUtils.isNotEmpty(searchCriteria.get("issuingBank"))) {
            try {
                Long bankId = Long.parseLong(searchCriteria.get("issuingBank").trim());
                predicates.add(criteriaBuilder.equal(profileJoin.get("issuingBank").get("id"), bankId));
            } catch (NumberFormatException e) {
                log.warn("Invalid issuingBank format (expected numeric id): {}", searchCriteria.get("issuingBank"));
            }
        }

        // Card status filter (supports code, enum name, or Persian description)
        if (StringUtils.isNotEmpty(searchCriteria.get("cardStatus"))) {
            Integer statusCode = parseCardStatus(searchCriteria.get("cardStatus"));
            if (statusCode != null) {
                predicates.add(CardSearchUtility.buildEnumCodePredicate(root, criteriaBuilder, "cardStatus",
                        CardStatus.class, statusCode));
            }
        }

        // Card type filter by profile.cardTypeCategory.cardType.id
        if (StringUtils.isNotEmpty(searchCriteria.get("cardTypeId"))) {
            try {
                Long cardTypeId = Long.parseLong(searchCriteria.get("cardTypeId").trim());
                Join<CardProfileCommandEntity, CardTypeCategoryCommandEntity> typeCategoryJoin =
                        profileJoin.join("cardTypeCategory", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(typeCategoryJoin.get("cardType").get("id"), cardTypeId));
            } catch (NumberFormatException e) {
                log.warn("Invalid cardTypeId format (expected numeric id): {}", searchCriteria.get("cardTypeId"));
            }
        }

        // Issuance type filter (physical/logical)
        if (StringUtils.isNotEmpty(searchCriteria.get("issuanceType"))) {
            Boolean issuanceType = resolveIssuanceType(searchCriteria.get("issuanceType"));
            if (issuanceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("isVirtualCard"), issuanceType));
            }
        }

        // Card profile ID filter (through relationship)
        if (StringUtils.isNotEmpty(searchCriteria.get("cardProfileId"))) {
            try {
                Long profileId = Long.parseLong(searchCriteria.get("cardProfileId"));
                predicates.add(criteriaBuilder.equal(profileJoin.get("id"), profileId));
            } catch (NumberFormatException e) {
                log.warn("Invalid cardProfileId format: {}", searchCriteria.get("cardProfileId"));
            }
        }

        // Virtual card filter (direct boolean flag)
        if (StringUtils.isNotEmpty(searchCriteria.get("isVirtualCard"))) {
            Boolean parsed = CardSearchUtility.parseBooleanValue(searchCriteria.get("isVirtualCard"));
            if (parsed != null) {
                predicates.add(criteriaBuilder.equal(root.get("isVirtualCard"), parsed));
            }
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("issueDateFrom")) || StringUtils.isNotEmpty(searchCriteria.get("issueDateTo"))) {
            Predicate p = CardSearchUtility.buildDateRangePredicate(root, criteriaBuilder, "issueDate",
                    searchCriteria.get("issueDateFrom"), searchCriteria.get("issueDateTo"));
            if (p != null) predicates.add(p);
        }

        // Created date range filter
        if (StringUtils.isNotEmpty(searchCriteria.get("createdDateFrom")) || StringUtils.isNotEmpty(searchCriteria.get("createdDateTo"))) {
            Predicate p = CardSearchUtility.buildDateRangePredicate(root, criteriaBuilder, "createdDate",
                    searchCriteria.get("createdDateFrom"), searchCriteria.get("createdDateTo"));
            if (p != null) predicates.add(p);
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("caseNumber"))) {
            try {
                predicates.add(criteriaBuilder.equal(root.get("caseNumber").as(String.class),
                        searchCriteria.get("caseNumber").trim()));
            } catch (Exception e) {
                log.warn("Error filtering by caseNumber", e);
            }
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("unitName"))) {
            try {
                predicates.add(criteriaBuilder.equal(root.get("unitName").as(String.class),
                        searchCriteria.get("unitName").trim()));
            } catch (Exception e) {
                log.warn("Error filtering by unitName", e);
            }
        }

        return predicates;
    }

    private Integer parseCardStatus(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
        }
        try {
            return CardStatus.valueOf(trimmed.toUpperCase(Locale.ROOT)).getCode();
        } catch (IllegalArgumentException ignored) {
        }
        for (CardStatus status : CardStatus.values()) {
            if (status.getDescription().equals(trimmed)) {
                return status.getCode();
            }
        }
        return null;
    }

    private Boolean resolveIssuanceType(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Boolean parsed = CardSearchUtility.parseBooleanValue(value);
        if (parsed != null) {
            return parsed;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("فیز") || normalized.contains("physical")) {
            return Boolean.FALSE;
        }
        if (normalized.contains("منطق") || normalized.contains("logical")) {
            return Boolean.TRUE;
        }
        return null;
    }

    @Override
    public Optional<Card> findByPan(String pan) {
        log.debug("Finding card by PAN: {}", PanMaskingUtil.maskPan(pan));
        return cardJpaRepository.findByPan(pan)
                .map(cardDataAccessMapper::toDomain);
    }

    @Override
    public boolean existsByCardNumber(String cardNumber) {
        log.debug("Checking if card exists by card number: {}", PanMaskingUtil.maskPan(cardNumber));
        return cardJpaRepository.existsByPan(cardNumber);
    }

    @Override
    public long count() {
        log.debug("Counting total cards");
        return cardJpaRepository.count();
    }

    @Override
    public boolean existsByProfileId(Long cardProfileId) {
        log.debug("Checking if any card exists for profile ID: {}", cardProfileId);
        return cardJpaRepository.existsByProfileId(cardProfileId);
    }

}

