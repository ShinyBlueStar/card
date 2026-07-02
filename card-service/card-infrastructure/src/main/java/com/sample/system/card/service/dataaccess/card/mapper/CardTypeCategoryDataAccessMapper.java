package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.CardTypeCategoryCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.CardTypeCategory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DataAccessMapperConfig.class,
        uses = {CardTypeDataAccessMapper.class, CardCategoryDataAccessMapper.class})
public interface CardTypeCategoryDataAccessMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "cardTypeId", source = "cardType.id")
    @Mapping(target = "cardCategoryId", source = "cardCategory.id")
    CardTypeCategory toDomain(CardTypeCategoryCommandEntity entity);

    // روابط CardType و CardCategory جداگانه در لایه ریپازیتوری ست میشوند
    @Mapping(target = "cardType", ignore = true)
    @Mapping(target = "cardCategory", ignore = true)
    CardTypeCategoryCommandEntity toEntity(CardTypeCategory domain);
}
