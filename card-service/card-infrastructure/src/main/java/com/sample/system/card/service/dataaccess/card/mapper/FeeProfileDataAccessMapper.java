package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.FeeProfileCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.FeeProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DataAccessMapperConfig.class, uses = FeeDataAccessMapper.class)
public interface FeeProfileDataAccessMapper {

    @Mapping(target = "isActive", defaultValue = "true")
    @Mapping(target = "cardProfiles", ignore = true)
    FeeProfile toDomain(FeeProfileCommandEntity entity);

    @Mapping(target = "isActive", defaultValue = "true")
    FeeProfileCommandEntity toEntity(FeeProfile domain);
}
