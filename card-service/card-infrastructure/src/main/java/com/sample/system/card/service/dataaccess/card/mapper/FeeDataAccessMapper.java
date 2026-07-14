package com.sample.system.card.service.dataaccess.card.mapper;

import com.sample.system.card.service.dataaccess.card.entity.command.FeeCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.config.DataAccessMapperConfig;
import com.sample.system.card.service.dataaccess.card.mapper.config.IgnoreAuditFields;
import com.sample.system.card.service.domain.entity.Fee;
import com.sample.system.card.service.domain.enums.FeePeriod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DataAccessMapperConfig.class)
public interface FeeDataAccessMapper {

    // feeProfile برای جلوگیری از رفرنس چرخشی مپ نمیشود
    @Mapping(target = "feeProfile", ignore = true)
    Fee toDomain(FeeCommandEntity entity);

    // feeProfile در لایه ریپازیتوری ست میشود
    @IgnoreAuditFields
    @Mapping(target = "feeProfile", ignore = true)
    FeeCommandEntity toEntity(Fee domain);

    // در دامین feePeriod هنوز به صورت کد نگه داشته میشود
    default Integer toFeePeriodCode(FeePeriod feePeriod) {
        return feePeriod == null ? null : feePeriod.getCode();
    }

    default FeePeriod toFeePeriod(Integer code) {
        return code == null ? null : FeePeriod.fromCode(code);
    }
}
