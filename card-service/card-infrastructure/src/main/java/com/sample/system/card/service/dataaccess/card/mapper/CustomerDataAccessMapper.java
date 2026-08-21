package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.CustomerCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.Customer;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DataAccessMapperConfig.class)
public interface CustomerDataAccessMapper {

    @Mapping(target = "address", ignore = true)
    CustomerCommandEntity toEntity(Customer domain);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    Customer toDomain(CustomerCommandEntity entity);
}
