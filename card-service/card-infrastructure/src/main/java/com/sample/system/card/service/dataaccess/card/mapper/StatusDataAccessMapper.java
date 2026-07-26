package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.StatusCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.Status;
import org.mapstruct.Mapper;

@Mapper(config = DataAccessMapperConfig.class)
public interface StatusDataAccessMapper {

    Status toDomain(StatusCommandEntity entity);

    StatusCommandEntity toEntity(Status domain);
}
