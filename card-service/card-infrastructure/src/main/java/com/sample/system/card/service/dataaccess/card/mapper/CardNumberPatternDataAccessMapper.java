package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.CardNumberPatternCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardProfileCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.valueObject.CardNumberPatternId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = DataAccessMapperConfig.class)
public interface CardNumberPatternDataAccessMapper {

    // CardNumberPattern فقط builder دارد، پس id بعد از ساخت آبجکت ست میشود
    default CardNumberPattern toDomain(CardNumberPatternCommandEntity entity) {
        CardNumberPattern pattern = mapFields(entity);
        if (pattern != null && entity.getId() != null) {
            pattern.setId(new CardNumberPatternId(entity.getId()));
        }
        return pattern;
    }

    @Named("mapFields")
    @Mapping(target = "cardProfile", ignore = true)
    @Mapping(target = "cardProfileId", source = "cardProfile.id")
    CardNumberPattern mapFields(CardNumberPatternCommandEntity entity);

    @Mapping(target = "cardProfile", expression = "java(toCardProfileReference(domain))")
    CardNumberPatternCommandEntity toEntity(CardNumberPattern domain);

    // فقط شناسه پروفایل برای رابطه ManyToOne کافی است
    default CardProfileCommandEntity toCardProfileReference(CardNumberPattern domain) {
        Long profileId = null;
        if (domain.getCardProfileId() != null) {
            profileId = domain.getCardProfileId().getValue();
        } else if (domain.getCardProfile() != null && domain.getCardProfile().getId() != null) {
            profileId = domain.getCardProfile().getId().getValue();
        }
        if (profileId == null) {
            return null;
        }
        CardProfileCommandEntity profile = new CardProfileCommandEntity();
        profile.setId(profileId);
        return profile;
    }
}
