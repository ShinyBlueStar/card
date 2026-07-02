package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.CardTypeCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.CardType;
import org.mapstruct.Mapper;

@Mapper(config = DataAccessMapperConfig.class)
public interface CardTypeDataAccessMapper {

    CardType toDomain(CardTypeCommandEntity entity);

    CardTypeCommandEntity toEntity(CardType domain);
}
