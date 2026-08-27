package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.FileDownloadCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.domain.entity.FileDownload;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DataAccessMapperConfig.class)
public interface FileDownloadDataAccessMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    FileDownload toDomain(FileDownloadCommandEntity entity);

    @Mapping(target = "isUsed", defaultValue = "false")
    FileDownloadCommandEntity toEntity(FileDownload domain);
}
