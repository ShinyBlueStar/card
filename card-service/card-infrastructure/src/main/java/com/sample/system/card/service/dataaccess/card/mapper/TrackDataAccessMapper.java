package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.TrackCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.Track;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DataAccessMapperConfig.class)
public interface TrackDataAccessMapper {

    @Mapping(target = "card", ignore = true)
    Track toDomain(TrackCommandEntity entity);

    // ارتباط با کارت در CardDataAccessMapper ست میشود
    @Mapping(target = "card", ignore = true)
    TrackCommandEntity toEntity(Track domain);
}
