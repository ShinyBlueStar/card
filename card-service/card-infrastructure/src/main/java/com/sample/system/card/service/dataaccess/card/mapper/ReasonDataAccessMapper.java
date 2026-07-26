package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.ReasonCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.Reason;
import com.sample.system.card.service.domain.valueObject.ReasonId;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(config = DataAccessMapperConfig.class)
public interface ReasonDataAccessMapper {

    ReasonCommandEntity toEntity(Reason domain);

    // Reason فقط builder دارد، پس id بعد از ساخت آبجکت ست میشود
    default Reason toDomain(ReasonCommandEntity entity) {
        Reason reason = mapFields(entity);
        if (reason != null) {
            reason.setId(new ReasonId(entity.getId()));
        }
        return reason;
    }

    @Named("mapFields")
    Reason mapFields(ReasonCommandEntity entity);
}
