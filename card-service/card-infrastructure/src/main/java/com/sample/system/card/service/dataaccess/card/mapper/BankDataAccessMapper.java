package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.BankCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.Bank;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(config = DataAccessMapperConfig.class)
public interface BankDataAccessMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    Bank toDomain(BankCommandEntity entity);

    BankCommandEntity toEntity(Bank domain);
}
