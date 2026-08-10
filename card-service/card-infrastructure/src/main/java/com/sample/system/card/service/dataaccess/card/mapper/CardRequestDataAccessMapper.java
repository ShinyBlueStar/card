package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.CardCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardRequestCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.requests.ChangePinRequestEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.requests.IssueCardRequestEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.requests.ReissuanceCardRequestEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.requests.RenewalCardRequestEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.requests.ReplacementCardRequestEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.enums.CardRequestType;
import com.sample.system.card.service.domain.valueObject.CardRequestId;
import org.hibernate.Hibernate;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Mapper(config = DataAccessMapperConfig.class,
        uses = {ReasonDataAccessMapper.class, CardProfileDataAccessMapper.class, CardDataAccessMapper.class},
        imports = Hibernate.class)
public interface CardRequestDataAccessMapper {

    // ==================== Entity -> Domain ====================

    // فیلدهای مخصوص هر نوع درخواست بر اساس کلاس فرزند مپ میشوند
    // CardRequest فقط builder دارد، پس id بعد از ساخت آبجکت ست میشود
    default CardRequest toDomain(CardRequestCommandEntity entity) {
        if (entity == null) {
            return null;
        }
        CardRequest cardRequest;
        if (entity instanceof IssueCardRequestEntity issueEntity) {
            cardRequest = issueRequestToDomain(issueEntity);
        } else if (entity instanceof ReplacementCardRequestEntity replacementEntity) {
            cardRequest = replacementRequestToDomain(replacementEntity);
        } else if (entity instanceof RenewalCardRequestEntity renewalEntity) {
            cardRequest = renewalRequestToDomain(renewalEntity);
        } else {
            cardRequest = baseRequestToDomain(entity);
        }
        if (entity.getId() != null) {
            cardRequest.setId(new CardRequestId(entity.getId()));
        }
        return cardRequest;
    }

    // نوع درخواست از روی کلاس واقعی انتیتی (ستون discriminator) تعیین میشود
    @Named("baseRequestToDomain")
    @Mapping(target = "requestType",
            expression = "java(mapRequestType(Hibernate.getClass(entity).getSimpleName()))")
    @Mapping(target = "cardId", source = "card.id")
    CardRequest baseRequestToDomain(CardRequestCommandEntity entity);

    @Named("issueRequestToDomain")
    @InheritConfiguration(name = "baseRequestToDomain")
    @Mapping(target = "cardProfileId", source = "cardProfile.id")
    CardRequest issueRequestToDomain(IssueCardRequestEntity entity);

    @Named("replacementRequestToDomain")
    @InheritConfiguration(name = "baseRequestToDomain")
    @Mapping(target = "reasonId", source = "reason.id")
    CardRequest replacementRequestToDomain(ReplacementCardRequestEntity entity);

    @Named("renewalRequestToDomain")
    @InheritConfiguration(name = "baseRequestToDomain")
    CardRequest renewalRequestToDomain(RenewalCardRequestEntity entity);

    // ==================== Domain -> Entity ====================

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "requestStatus", source = "requestStatus")
    CardRequestCommandEntity toEntity(CardRequest domain);

    @CommonRequestFields
    @BeanMapping(builder = @Builder(disableBuilder = true))
    IssueCardRequestEntity toIssueCardRequestEntity(CardRequest domain);

    @CommonRequestFields
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardNumber", ignore = true)
    ReissuanceCardRequestEntity toDuplicateCardIssuanceRequestEntity(CardRequest domain);

    @CommonRequestFields
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "oldCardId", ignore = true)
    RenewalCardRequestEntity toCardRenewalRequestEntity(CardRequest domain);

    @CommonRequestFields
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    ChangePinRequestEntity toChangePinRequestEntity(CardRequest domain);

    @CommonRequestFields
    @BeanMapping(builder = @Builder(disableBuilder = true))
    ReplacementCardRequestEntity toReplacementRequestEntity(CardRequest domain);

    // اگر cardId موجود باشد اولویت دارد، وگرنه از خود کارت استفاده میشود
    default CardCommandEntity toCardReference(CardRequest domain) {
        Long cardId = null;
        if (domain.getCardId() != null && domain.getCardId().getValue() != null) {
            cardId = domain.getCardId().getValue();
        } else if (domain.getCard() != null) {
            cardId = domain.getCard().getId().getValue();
        }
        if (cardId == null) {
            return null;
        }
        CardCommandEntity card = new CardCommandEntity();
        card.setId(cardId);
        return card;
    }

    /**
     * Map request type string from discriminator column to CardRequestType enum
     */
    default CardRequestType mapRequestType(String requestTypeValue) {
        if (requestTypeValue == null || requestTypeValue.isBlank()) {
            return null;
        }
        try {
            return CardRequestType.valueOf(requestTypeValue.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            // Not a direct enum name match
        }
        String upperValue = requestTypeValue.toUpperCase();
        if (upperValue.contains("ISSUE") || upperValue.contains("LOGICAL")) {
            return CardRequestType.CARD_ISSUANCE;
        } else if (upperValue.contains("REPLACEMENT") || upperValue.contains("REPLACE")) {
            return CardRequestType.CARD_REPLACEMENT;
        } else if (upperValue.contains("RENEWAL") || upperValue.contains("RENEW")) {
            return CardRequestType.CARD_RENEWAL;
        } else if (upperValue.contains("PRINT")) {
            return CardRequestType.CARD_PRINT;
        } else if (upperValue.contains("CHANGE_PIN") || upperValue.contains("PIN")) {
            return CardRequestType.CHANGE_PIN;
        } else if (upperValue.contains("CARD_REISSUANCE") || upperValue.contains("REISSUANCE")) {
            return CardRequestType.CARD_REISSUANCE;
        } else if (upperValue.contains("CORRECTION")) {
            return CardRequestType.CARD_CORRECTION;
        }
        for (CardRequestType type : CardRequestType.values()) {
            if (type.getDescription().equalsIgnoreCase(requestTypeValue)) {
                return type;
            }
        }
        return null;
    }

    // فیلدهای مشترک همه انواع درخواست هنگام تبدیل دامین به انتیتی
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.METHOD)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "requestType", ignore = true)
    @Mapping(target = "card", expression = "java(toCardReference(domain))")
    @interface CommonRequestFields {
    }
}
