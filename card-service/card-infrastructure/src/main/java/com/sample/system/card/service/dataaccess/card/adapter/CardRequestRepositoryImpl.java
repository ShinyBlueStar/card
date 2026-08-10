package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.*;
import com.sample.system.card.service.dataaccess.card.entity.command.requests.*;
import com.sample.system.card.service.dataaccess.card.mapper.CardRequestDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.*;
import com.sample.system.card.service.domain.enums.CardRequestType;
import com.sample.system.card.service.dataaccess.utility.CardSearchUtility;
import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.enums.CardRequestStatus;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.CardRequestRepository;
import com.sample.system.card.service.domain.valueObject.CardRequestId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
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

import java.util.*;
import static java.util.stream.Collectors.toList;

/**
 * CardRequest Repository Implementation
 * Following DDD infrastructure patterns with adapter pattern
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardRequestRepositoryImpl implements CardRequestRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final CardRequestCommandJpaRepository repository;
    private final CardRequestDataAccessMapper cardRequestDataAccessMapper;
    private final IssueCardRequestJpaRepository issueCardRequestJpaRepository;
    private final ReplacementCardRequestJpaRepository replacementCardRequestJpaRepository;
    private final ReissuanceCardRequestJpaRepository reissuanceCardRequestJpaRepository;
    private final CardRenewalRequestJpaRepository cardRenewalRequestJpaRepository;
    private final FirstPinIssuanceRequestJpaRepository firstPinIssuanceRequestJpaRepository;
    private final FixedSecondPinIssuanceRequestJpaRepository fixedSecondPinIssuanceRequestJpaRepository;
    private final ChangePinRequestJpaRepository changePinRequestJpaRepository;
    private final PinRequestsRequestJpaRepository pinRequestsRequestJpaRepository;

    @Override
    public CardRequest save(CardRequest cardRequest) {
        log.debug("Saving card request with ID: {}", cardRequest.getId() != null ? cardRequest.getId().getValue() : null);

        if (cardRequest.getRequestType() == null) {
            throw new IllegalArgumentException("CardRequest requestType cannot be null");
        }

        CardRequestCommandEntity savedEntity = switch (cardRequest.getRequestType()) {
            case CARD_ISSUANCE -> issueCardRequestJpaRepository.save(
                    cardRequestDataAccessMapper.toIssueCardRequestEntity(cardRequest));
            case CARD_REPLACEMENT -> replacementCardRequestJpaRepository.save(
                    cardRequestDataAccessMapper.toReplacementRequestEntity(cardRequest));
            case CARD_RENEWAL -> cardRenewalRequestJpaRepository.save(
                    cardRequestDataAccessMapper.toCardRenewalRequestEntity(cardRequest));
            case CARD_REISSUANCE -> reissuanceCardRequestJpaRepository.save(
                    cardRequestDataAccessMapper.toDuplicateCardIssuanceRequestEntity(cardRequest));
            case CHANGE_PIN -> changePinRequestJpaRepository.save(
                    cardRequestDataAccessMapper.toChangePinRequestEntity(cardRequest));
            default -> repository.save(cardRequestDataAccessMapper.toEntity(cardRequest));
        };

        log.info("Request was Saved Successfully with id: {}", savedEntity.getId());
        return cardRequestDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public CardRequest updateStatus(CardRequest cardRequest) throws CardDomainException {
        Long id = cardRequest.getId().getValue();
        log.debug("Updating card request with ID: {}", id);

        CardRequestCommandEntity existingEntity = repository.findById(id)
                .orElseThrow(() -> new CardDomainException(
                        "CardRequest not found: " + id,
                        StatusService.REQUEST_NOT_FOUND,
                        HttpStatus.BAD_REQUEST));

        CardRequestCommandEntity savedEntity;

        if (existingEntity instanceof IssueCardRequestEntity issueEntity) {
            issueEntity.setRequestStatus(cardRequest.getRequestStatus());
            savedEntity = issueCardRequestJpaRepository.save(issueEntity);
        } else if (existingEntity instanceof ReplacementCardRequestEntity replacementEntity) {
            replacementEntity.setRequestStatus(cardRequest.getRequestStatus());
            savedEntity = replacementCardRequestJpaRepository.save(replacementEntity);
        } else if (existingEntity instanceof RenewalCardRequestEntity renewalEntity) {
            renewalEntity.setRequestStatus(cardRequest.getRequestStatus());
            savedEntity = cardRenewalRequestJpaRepository.save(renewalEntity);
        } else if (existingEntity instanceof ReissuanceCardRequestEntity duplicateEntity) {
            duplicateEntity.setRequestStatus(cardRequest.getRequestStatus());
            savedEntity = reissuanceCardRequestJpaRepository.save(duplicateEntity);
        } else if (existingEntity instanceof ChangePinRequestEntity changePinEntity) {
            changePinEntity.setRequestStatus(cardRequest.getRequestStatus());
            savedEntity = changePinRequestJpaRepository.save(changePinEntity);
        } else if (existingEntity instanceof FirstPinIssuanceRequestEntity firstPinEntity) {
            firstPinEntity.setRequestStatus(cardRequest.getRequestStatus());
            savedEntity = firstPinIssuanceRequestJpaRepository.save(firstPinEntity);
        } else if (existingEntity instanceof FixedSecondPinIssuanceRequestEntity fixedSecondPinEntity) {
            fixedSecondPinEntity.setRequestStatus(cardRequest.getRequestStatus());
            savedEntity = fixedSecondPinIssuanceRequestJpaRepository.save(fixedSecondPinEntity);
        } else if (existingEntity instanceof PrintPinRequestEntity printPinEntity) {
            printPinEntity.setRequestStatus(cardRequest.getRequestStatus());
            savedEntity = repository.save(printPinEntity);
        } else if (existingEntity instanceof PinRequestsRequestEntity pinRequestsEntity) {
            pinRequestsEntity.setRequestStatus(cardRequest.getRequestStatus());
            savedEntity = pinRequestsRequestJpaRepository.save(pinRequestsEntity);
        } else {
            existingEntity.setRequestStatus(cardRequest.getRequestStatus());
            savedEntity = repository.save(existingEntity);
        }

        log.info("Request was Updated Successfully with id: {}", savedEntity.getId());
        return cardRequestDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public CardRequest saveInitiateReq(CardRequest cardRequest) throws CardDomainException {
        log.debug("Saving new card issue request with national ID: {}", cardRequest.getNationalId());
        // Convert to specific child entity based on requestType
        var entity = cardRequestDataAccessMapper.toIssueCardRequestEntity(cardRequest);
        var savedEntity = issueCardRequestJpaRepository.save(entity);
        return cardRequestDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public CardRequest updateIssueReq(CardRequest cardRequest) throws CardDomainException {
        log.debug("Updating request:{}, for Issue card , with Card Number: {}", cardRequest.getId().getValue(),
                cardRequest.getCard().getPan());
        // Convert to specific child entity based on requestType
        var entity = cardRequestDataAccessMapper.toIssueCardRequestEntity(cardRequest);
        var savedEntity = issueCardRequestJpaRepository.save(entity);
        return cardRequestDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public CardRequest saveReplacementReq(CardRequest cardRequest) throws CardDomainException{
        log.debug("Saving Replacement card request with Card ID: {}",
                cardRequest.getCardId().getValue());
        var entity = cardRequestDataAccessMapper.toReplacementRequestEntity(cardRequest);
        var savedEntity = replacementCardRequestJpaRepository.save(entity);
        return cardRequestDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CardRequest> findById(Long cardRequestId) {
        log.debug("Finding card request by ID: {}", cardRequestId);
        return repository.findById(cardRequestId)
                .map(cardRequestDataAccessMapper::toDomain);
    }

    @Override
    public Optional<CardRequest> findByIdAndType(Long cardRequestId, CardRequestType requestType) {
        log.debug("Finding card request by ID: {} and Type: {}", cardRequestId, requestType);

        if (cardRequestId == null || requestType == null) {
            return Optional.empty();
        }

        Optional<CardRequestCommandEntity> entityOpt = repository.findById(cardRequestId);
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        CardRequestCommandEntity entity = entityOpt.get();
        CardRequest cardRequest = cardRequestDataAccessMapper.toDomain(entity);

        // Check if the entity type matches the requested type
        if (cardRequest == null || cardRequest.getRequestType() != requestType) {
            log.debug("CardRequest type mismatch - Requested: {}, Found: {}",
                    requestType, cardRequest != null ? cardRequest.getRequestType() : null);
            return Optional.empty();
        }

        return Optional.of(cardRequest);
    }

    @Override
    public List<CardRequest> findAll() {
        log.debug("Finding all card requests");
        return repository.findAll()
                .stream()
                .map(cardRequestDataAccessMapper::toDomain)
                .collect(toList());
    }

    @Override
    public Page<CardRequest> findAllCardRequests(Map<String, String> searchCriteria) {
            if (searchCriteria == null) {
                searchCriteria = new java.util.HashMap<>();
            }

            Pageable pageable = PageRequest.of(
                    Integer.parseInt(searchCriteria.getOrDefault("page", "0")),
                    Integer.parseInt(searchCriteria.getOrDefault("size", "10"))
            );

            Page<CardRequestCommandEntity> entityPage = repository
                    .findAll(getCardRequestsSpecification(searchCriteria), pageable);

            List<CardRequest> requests = entityPage.getContent().stream()
                    .map(cardRequestDataAccessMapper::toDomain)
                    .collect(toList());
            log.info("card profile list size is: {}" ,  requests.size());

            return new PageImpl<>(requests, entityPage.getPageable(), entityPage.getTotalElements());
    }

    private Specification<CardRequestCommandEntity> getCardRequestsSpecification(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
            } else {
                query.distinct(true);
            }

            joinChildEntities(root, query, criteriaBuilder);

            List<Predicate> predicates = buildCardRequestPredicatesFromCriteria(
                    searchCriteria, root, query, criteriaBuilder);

            // Set ordering
            String orderBy = searchCriteria.getOrDefault("orderBy", "id");
            String sortDirection = searchCriteria.getOrDefault("sort", "desc");
            setCardRequestOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void joinChildEntities(Root<CardRequestCommandEntity> root,
                                   CriteriaQuery<?> query,
                                   CriteriaBuilder criteriaBuilder) {
        try {
            root.join("card", JoinType.LEFT); // Join to Card if needed
        } catch (Exception e) {
            log.trace("Optional join failed (this is normal for base queries): {}", e.getMessage());
        }
    }

    private void setCardRequestOrder(CriteriaQuery<?> query, String orderBy, String sortDirection,
                                     CriteriaBuilder criteriaBuilder, Root<CardRequestCommandEntity> root) {
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

    private List<Predicate> buildCardRequestPredicatesFromCriteria(
            Map<String, String> criteria,
            Root<CardRequestCommandEntity> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        addPredicate(predicates,
                CardSearchUtility.buildExactMatchPredicate(root, cb, "id", criteria.get("id")));

        addPredicate(predicates, parseInteger(criteria.get("requestStatus"))
                .map(v -> CardSearchUtility.buildEnumCodePredicate(root, cb, "requestStatus",
                        CardRequestStatus.class, v)).orElse(null));

        addPredicate(predicates,
                CardSearchUtility.buildDateRangePredicate(
                        root, cb, "createdDate",
                        criteria.get("createdDateFrom"),
                        criteria.get("createdDateTo"))
        );

        // Search by CardRequestType code
        if (StringUtils.isNotEmpty(criteria.get("requestType"))) {
            String requestTypeValue = criteria.get("requestType").trim();
            String discriminatorValue = resolveRequestTypeFromCode(requestTypeValue);
            if (discriminatorValue != null) {
                // Access discriminator column using type() and compare with entity classes
                // Map discriminator value to entity class
                Class<?> entityClass = mapDiscriminatorToEntityClass(discriminatorValue);
                if (entityClass != null) {
                    predicates.add(cb.equal(root.type(), cb.literal(entityClass)));
                }
            }
        }

        addPredicate(predicates,
                CardSearchUtility.buildExactMatchPredicate(root, cb, "unitCode",
                        criteria.get("unitCode")));

        addPredicate(predicates,
                CardSearchUtility.buildFarsiLikePredicate(root, cb, "unitName",
                        criteria.get("unitName")));

        addPredicate(predicates,
                parseLong(criteria.get("profileId"))
                        .map(v -> cb.equal(root.get("profileId"), v))
                        .orElse(null)
        );

        addPredicate(predicates,
                CardSearchUtility.buildFarsiLikePredicate(root, cb, "nationalId",
                        criteria.get("nationalId")));

        // caseNumber is in IssueCardRequestEntity table (CARD_REQUEST_LOGICAL_ISSUANCE), need to use treat() to access it
        if (StringUtils.isNotBlank(criteria.get("caseNumber"))) {
            try {
                Path<IssueCardRequestEntity> issuePath = cb.treat(root, IssueCardRequestEntity.class);
                String caseNumberValue = criteria.get("caseNumber").trim();
                String pattern = "%" + caseNumberValue + "%";
                Predicate caseNumberPredicate = cb.like(
                        issuePath.get("caseNumber").as(String.class), pattern);
                predicates.add(caseNumberPredicate);
            } catch (Exception e) {
                log.trace("caseNumber filter skipped (entity might not be IssueCardRequestEntity): {}", e.getMessage());
            }
        }

        // cardNumber (PAN) is in Card table, need to join to Card entity to access it
        if (StringUtils.isNotBlank(criteria.get("cardNumber"))) {
            try {
                Join<CardRequestCommandEntity, CardCommandEntity> cardJoin = root.join("card", JoinType.LEFT);
                String cardNumberValue = criteria.get("cardNumber").trim();
                Predicate cardNumberPredicate = cb.equal(
                        cardJoin.get("pan").as(String.class), cardNumberValue);
                predicates.add(cardNumberPredicate);
            } catch (Exception e) {
                log.trace("cardNumber filter skipped (card join failed): {}", e.getMessage());
            }
        }

        addPredicate(predicates,
                CardSearchUtility.buildFarsiLikePredicate(root, cb, "firstName",
                        criteria.get("firstName")));

        addPredicate(predicates,
                CardSearchUtility.buildFarsiLikePredicate(root, cb, "lastName",
                        criteria.get("lastName")));

        return predicates;
    }

    private void addPredicate(List<Predicate> list, Predicate p) {
        if (p != null) list.add(p);
    }

    private Optional<Integer> parseInteger(String v) {
        try {
            return (StringUtils.isNotBlank(v))
                    ? Optional.of(Integer.parseInt(v.trim()))
                    : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Long> parseLong(String v) {
        try {
            return (StringUtils.isNotBlank(v))
                    ? Optional.of(Long.parseLong(v.trim()))
                    : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Map discriminator value to entity class
     */
    private Class<?> mapDiscriminatorToEntityClass(String discriminatorValue) {
        if (StringUtils.isBlank(discriminatorValue)) {
            return null;
        }

        return switch (discriminatorValue.toUpperCase()) {
            case "CARD_ISSUANCE" -> IssueCardRequestEntity.class;
            case "CARD_RENEWAL" -> RenewalCardRequestEntity.class;
            case "CARD_REPLACEMENT" -> ReplacementCardRequestEntity.class;
            case "CARD_REISSUANCE" -> ReissuanceCardRequestEntity.class;
            case "CHANGE_PIN" -> ChangePinRequestEntity.class;
            case "CARD_CORRECTION" -> CardRequestCommandEntity.class; // Base entity for correction
            default -> null;
        };
    }

    /**
     * Resolve CardRequestType code to discriminator value
     * Accepts code (1-7) and maps to discriminator column value
     */
    private String resolveRequestTypeFromCode(String requestTypeValue) {
        if (StringUtils.isBlank(requestTypeValue)) {
            return null;
        }

        String trimmed = requestTypeValue.trim();

        // Try to parse as integer code first
        try {
            Integer code = Integer.parseInt(trimmed);
            CardRequestType requestType = CardRequestType.fromCode(code);

            // Map CardRequestType enum to discriminator value
            return switch (requestType) {
                case CARD_ISSUANCE -> "CARD_ISSUANCE";
                case CARD_PRINT -> "CARD_ISSUANCE"; // CARD_PRINT might use same discriminator
                case CARD_RENEWAL -> "CARD_RENEWAL";
                case CHANGE_PIN -> "CHANGE_PIN";
                case CARD_REISSUANCE -> "CARD_REISSUANCE";
                case CARD_REPLACEMENT -> "CARD_REPLACEMENT";
                case CARD_CORRECTION -> "CARD_CORRECTION";
            };
        } catch (NumberFormatException e) {
            // If not a number, try to match by enum name or description
            log.debug("RequestType value is not a number, trying enum name/description match: {}", trimmed);
        } catch (Exception e) {
            log.warn("Invalid CardRequestType code: {}", trimmed, e);
            return null;
        }

        // Try to match by enum name
        try {
            CardRequestType requestType = CardRequestType.valueOf(trimmed.toUpperCase());
            return switch (requestType) {
                case CARD_ISSUANCE -> "CARD_ISSUANCE";
                case CARD_PRINT -> "CARD_ISSUANCE";
                case CARD_RENEWAL -> "CARD_RENEWAL";
                case CHANGE_PIN -> "CHANGE_PIN";
                case CARD_REISSUANCE -> "CARD_REISSUANCE";
                case CARD_REPLACEMENT -> "CARD_REPLACEMENT";
                case CARD_CORRECTION -> "CARD_CORRECTION";
            };
        } catch (IllegalArgumentException e) {
            log.debug("RequestType value does not match enum name: {}", trimmed);
        }

        // Try to match by description (Persian)
        for (CardRequestType type : CardRequestType.values()) {
            if (type.getDescription().equals(trimmed)) {
                return switch (type) {
                    case CARD_ISSUANCE -> "CARD_ISSUANCE";
                    case CARD_PRINT -> "CARD_ISSUANCE";
                    case CARD_RENEWAL -> "CARD_RENEWAL";
                    case CHANGE_PIN -> "CHANGE_PIN";
                    case CARD_REISSUANCE -> "CARD_REISSUANCE";
                    case CARD_REPLACEMENT -> "CARD_REPLACEMENT";
                    case CARD_CORRECTION -> "CARD_CORRECTION";
                };
            }
        }

        return null;
    }

    @Override
    public long count() {
        log.debug("Counting total card requests");
        return repository.count();
    }

    @Override
    public void deleteById(CardRequestId cardRequestId) {
        log.debug("Deleting card request by ID: {}", cardRequestId.getValue());
        repository.deleteById(cardRequestId.getValue());
    }

    @Override
    public Boolean existByIdAndStatus(CardRequest request, CardRequestStatus status) throws CardDomainException {
        if (request == null || request.getId() == null || request.getId().getValue() == null) {
            throw new CardDomainException("CardRequest id is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        if (request.getRequestStatus() == null) {
            throw new CardDomainException("CardRequest status is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        Long requestId = request.getId().getValue();

        log.debug("Checking existence for CardRequest id: {}, status: {}", requestId, request.getRequestStatus());
        boolean exists = repository.existsByIdAndRequestStatus(requestId, status);

        if (!exists) {
            throw new CardDomainException(String.format("CardRequest not found with id %s and status %s",
                    requestId, request.getRequestStatus()),
                    StatusService.REQUEST_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean existByTypeAndStatusAndPan(CardRequestType type, String pan, List<Integer> statusList)
            throws CardDomainException {
        return false;
    }

    @Override
    public Boolean existByTypeAndStatusAndCardId(CardRequestType type, Long cardId, List<Integer> statusList)
            throws CardDomainException {
        log.info("Checking existence for CardRequest type: {}, cardId: {}, statusList: {}", type, cardId, statusList);

        // Build native query to check existence with discriminator column
        String discriminatorValue = null;
        if (type != null) {
            if (CardRequestType.CARD_REPLACEMENT.equals(type)) {
                discriminatorValue = "CARD_REPLACEMENT";
            } else if (CardRequestType.CARD_RENEWAL.equals(type)) {
                discriminatorValue = "CARD_RENEWAL";
            } else if (CardRequestType.CARD_REISSUANCE.equals(type)) {
                discriminatorValue = "CARD_REISSUANCE";
            } else if (CardRequestType.CARD_ISSUANCE.equals(type)) {
                discriminatorValue = "CARD_ISSUANCE";
            }
        }

        if (discriminatorValue == null) {
            log.warn("Unknown CardRequestType: {}", type);
            return false;
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT COUNT(*) FROM CARD_REQUEST cr ");
        queryBuilder.append("INNER JOIN CARDS c ON cr.CARD_ID = c.ID ");
        queryBuilder.append("WHERE c.ID = :cardId ");
        queryBuilder.append("AND cr.REQUEST_TYPE = :requestType ");

        if (statusList != null && !statusList.isEmpty()) {
            queryBuilder.append("AND cr.REQUEST_STATUS IN (:statusList) ");
        }

        jakarta.persistence.Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter("cardId", cardId);
        query.setParameter("requestType", discriminatorValue);
        if (statusList != null && !statusList.isEmpty()) {
            query.setParameter("statusList", statusList);
        }

        Object result = query.getSingleResult();
        Number count = (Number) result;
        boolean exists = count != null && count.intValue() > 0;
        log.debug("Existence check result: {} (count: {})", exists, count);
        return exists;
    }

    @Override
    public Boolean findByNationalIdAndCaseNumberAndTypeAndStatus(
            String nationalId, String caseNumber, CardRequestType requestType) {
        log.debug("Finding card request by NationalId: {}, CaseNumber: {}, Type: {} when status is not Cancel",
                nationalId, caseNumber, requestType);

        List<IssueCardRequestEntity> entity = issueCardRequestJpaRepository
                .findByNationalIdAndCaseNumberAndRequestStatusIn(
                        nationalId,
                        caseNumber,
                        CardRequestStatus.busyStatuses());

        if (entity.isEmpty()) {
            log.debug("No card request found matching criteria");
            return false;
        }
        return true;
    }

}
