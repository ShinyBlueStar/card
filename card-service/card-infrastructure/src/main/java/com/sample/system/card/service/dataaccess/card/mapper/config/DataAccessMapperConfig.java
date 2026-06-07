package com.sample.system.card.service.dataaccess.card.mapper.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

// تنظیمات مشترک همه مپرهای لایه دیتا اکسس
@MapperConfig(
        componentModel = "spring",
        uses = IdMappings.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DataAccessMapperConfig {
}
