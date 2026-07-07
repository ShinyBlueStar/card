package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.CardProfileCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(config = DataAccessMapperConfig.class,
        uses = {CardTypeCategoryDataAccessMapper.class, CardTypeDataAccessMapper.class, CardCategoryDataAccessMapper.class,
                FeeProfileDataAccessMapper.class, BankDataAccessMapper.class, CardNumberPatternDataAccessMapper.class})
public interface CardProfileDataAccessMapper {

    // با builder ساخته میشود تا setter های اعتبارسنج دامین (مثل setValidityPeriod) اجرا نشوند
    // و id بعد از ساخت آبجکت ست میشود
    default CardProfile toDomain(CardProfileCommandEntity entity) {
        CardProfile cardProfile = mapFields(entity);
        if (cardProfile != null) {
            cardProfile.setId(new CardProfileId(entity.getId()));
        }
        return cardProfile;
    }

    @Named("mapFields")
    @Mapping(target = "bank", source = "issuingBank")
    @Mapping(target = "bankId", source = "issuingBank.id")
    @Mapping(target = "cardTypeCategoryId", source = "cardTypeCategory.id")
    @Mapping(target = "cardType", source = "cardTypeCategory.cardType")
    @Mapping(target = "cardTypeId", source = "cardTypeCategory.cardType.id")
    @Mapping(target = "cardCategory", source = "cardTypeCategory.cardCategory")
    @Mapping(target = "cardCategoryId", source = "cardTypeCategory.cardCategory.id")
    @Mapping(target = "feeProfileId", source = "feeProfile.id")
    @Mapping(target = "feeProfileIdLong", source = "feeProfile.id")
    @Mapping(target = "cardNumberRangeList", source = "numberPatterns")
    CardProfile mapFields(CardProfileCommandEntity entity);

    @Mapping(target = "issuingBank", source = "bank")
    @Mapping(target = "numberPatterns", source = "cardNumberRangeList")
    @Mapping(target = "productCodeList", ignore = true)
    CardProfileCommandEntity toEntity(CardProfile domain);

    // لیست کد محصول ها بدون کپی پاس داده میشود (ElementCollection لیزی است)
    default List<String> productCodes(List<String> productCodeList) {
        return productCodeList;
    }
}
