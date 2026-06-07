package com.sample.system.card.service.dataaccess.card.mapper.config;

import com.sample.system.card.service.domain.valueObject.BankId;
import com.sample.system.card.service.domain.valueObject.BaseId;
import com.sample.system.card.service.domain.valueObject.CardCategoryId;
import com.sample.system.card.service.domain.valueObject.CardId;
import com.sample.system.card.service.domain.valueObject.CardNumberPatternId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import com.sample.system.card.service.domain.valueObject.CardRequestId;
import com.sample.system.card.service.domain.valueObject.CardTypeCategoryId;
import com.sample.system.card.service.domain.valueObject.CardTypeId;
import com.sample.system.card.service.domain.valueObject.FeeProfileId;
import com.sample.system.card.service.domain.valueObject.ReasonId;
import org.mapstruct.Mapper;

// تبدیل شناسه های Value Object دامین به Long انتیتی و برعکس
@Mapper(componentModel = "spring")
public interface IdMappings {

    default Long fromId(BaseId<Long> id) {
        return id == null ? null : id.getValue();
    }

    default BankId toBankId(Long id) {
        return id == null ? null : new BankId(id);
    }

    default CardCategoryId toCardCategoryId(Long id) {
        return id == null ? null : new CardCategoryId(id);
    }

    default CardId toCardId(Long id) {
        return id == null ? null : new CardId(id);
    }

    default CardNumberPatternId toCardNumberPatternId(Long id) {
        return id == null ? null : new CardNumberPatternId(id);
    }

    default CardProfileId toCardProfileId(Long id) {
        return id == null ? null : new CardProfileId(id);
    }

    default CardRequestId toCardRequestId(Long id) {
        return id == null ? null : new CardRequestId(id);
    }

    default CardTypeCategoryId toCardTypeCategoryId(Long id) {
        return id == null ? null : new CardTypeCategoryId(id);
    }

    default CardTypeId toCardTypeId(Long id) {
        return id == null ? null : new CardTypeId(id);
    }

    default FeeProfileId toFeeProfileId(Long id) {
        return id == null ? null : new FeeProfileId(id);
    }

    default ReasonId toReasonId(Long id) {
        return id == null ? null : new ReasonId(id);
    }
}
