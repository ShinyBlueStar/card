package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.CardCategoryCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.CardCategory;
import org.mapstruct.Mapper;

@Mapper(config = DataAccessMapperConfig.class)
public interface CardCategoryDataAccessMapper {

    CardCategory toDomain(CardCategoryCommandEntity entity);

    CardCategoryCommandEntity toEntity(CardCategory domain);
}
