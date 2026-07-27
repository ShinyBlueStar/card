package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.CardCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardProfileCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CustomerCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.TrackCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.dataaccess.card.mapper.config.IgnoreAuditFields;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.entity.Customer;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = DataAccessMapperConfig.class,
        uses = {CustomerDataAccessMapper.class, CardProfileDataAccessMapper.class, TrackDataAccessMapper.class},
        imports = {PanMaskingUtil.class, java.math.BigDecimal.class})
public interface CardDataAccessMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @IgnoreAuditFields
    @Mapping(target = "cardId", source = "cardUuid")
    @Mapping(target = "cardProfile", source = "cardProfileId")
    @Mapping(target = "physicalStatus", ignore = true)
    @Mapping(target = "maskedPan",
            expression = "java(domain.getMaskedPan() != null ? domain.getMaskedPan() : PanMaskingUtil.maskPan(domain.getPan()))")
    CardCommandEntity toEntity(Card domain);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "cardUuid", source = "cardId")
    @Mapping(target = "cardProfileId", source = "cardProfile.id")
    @Mapping(target = "cardPhysicalStatus", source = "physicalStatus")
    @Mapping(target = "maskedPan",
            expression = "java(entity.getMaskedPan() != null ? entity.getMaskedPan() : PanMaskingUtil.maskPan(entity.getPan()))")
    @Mapping(target = "creditLimit", defaultExpression = "java(BigDecimal.ZERO)")
    Card toDomain(CardCommandEntity entity);

    // فقط شناسه پروفایل برای رابطه ManyToOne کافی است
    default CardProfileCommandEntity toCardProfileReference(CardProfileId cardProfileId) {
        if (cardProfileId == null) {
            return null;
        }
        CardProfileCommandEntity profile = new CardProfileCommandEntity();
        profile.setId(cardProfileId.getValue());
        return profile;
    }

    @Condition
    default boolean hasNationalId(Customer customer) {
        return customer != null && customer.getNationalId() != null;
    }

    @Condition
    default boolean isPersisted(CustomerCommandEntity customer) {
        return customer != null && customer.getId() != null;
    }

    @Condition
    default boolean isPersisted(TrackCommandEntity track) {
        return track != null && track.getId() != null;
    }

    @AfterMapping
    default void linkTrack(@MappingTarget CardCommandEntity entity) {
        if (entity.getTrack() != null) {
            entity.getTrack().setCard(entity);
        }
    }
}
