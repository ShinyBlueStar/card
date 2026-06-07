package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.enums.*;
import com.sample.system.card.service.domain.response.base.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping(value = "/api/v1/card/enums", produces = "application/vnd.api.v1+json")
public class EnumController extends BaseController {

    private final Map<String, Supplier<Object[]>> enumRegistry = new LinkedHashMap<>();

    public EnumController() {
        register("ACTIONTYPE", ActionType::values);
        register("CARDCHANGEREASON", CardChangeReason::values);
        register("CARDISSUEMETHOD", CardIssueMethod::values);
        register("CARDNUMGENERATIONMETHOD", CardNumGenerationMethod::values);
        register("CARDNUMBERPATTERNSTATUS", CardNumberPatternStatus::values);
        register("CARDPHYSICALSTATUS", CardPhysicalStatus::values);
        register("CARDPROFILESTATUS", CardProfileStatus::values);
        register("CARDRENEWALTYPE", CardRenewalType::values);
        register("CARDREQUESTSTATUS", CardRequestStatus::values);
        register("CARDREQUESTTYPE", CardRequestType::values);
        register("CARDSTATUS", CardStatus::values);
        register("FEEPERIOD", FeePeriod::values);
        register("FEETYPE", FeeType::values);
        register("INITIALLOADTIME", InitialLoadTime::values);
        register("PIN2GENMETHOD", Pin2GenMethod::values);
        register("RECHARGEABLE", Rechargeable::values);
        register("RELOADABLE", Reloadable::values);
        register("REQUESTREASON", RequestReason::values);
        register("SALESMETHOD", SalesMethod::values);
        register("GENERALSTATUSENUM", GeneralStatusEnum::values);
        register("ACTIONREQUESTNEXTSTATUS", ActionRequestNextStatus::values);
        register("REASONGROUP", ReasonGroup::values);
    }

    private void register(String key, Supplier<Object[]> supplier) {
        enumRegistry.put(key.toUpperCase(Locale.ROOT), supplier);
    }

    @GetMapping("/names")
    public ResponseEntity<BaseResponse<Set<String>>> getAllEnumNames() {
        Set<String> names = enumRegistry.keySet();
        return ok(names);
    }

    @GetMapping("/{enumName}")
    public ResponseEntity<?> getEnumValues(
            @Parameter(description = "نام enum مورد نظر", required = true, example = "CARDSTATUS")
            @PathVariable String enumName
    )
    {
        Supplier<Object[]> supplier = enumRegistry.get(enumName.toUpperCase(Locale.ROOT));
        BaseResponse<String> errorResponse;
        if (supplier == null) {
            errorResponse = new BaseResponse<>(false, "Enum not found: " + enumName);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }

        Object converted = convertEnumValues(supplier.get());
        return ok(converted);
    }

    private List<Map<String, Object>> convertEnumValues(Object[] enumValues) {
        return Arrays.stream(enumValues)
                .map(this::convertSingleEnum)
                .toList();
    }

    private Map<String, Object> convertSingleEnum(Object enumConstant) {
        Map<String, Object> map = new LinkedHashMap<>();
        Field[] fields = enumConstant.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isSynthetic() || field.isEnumConstant()) {
                continue;
            }
            try {
                field.setAccessible(true);
                map.put(field.getName(), field.get(enumConstant));
            } catch (IllegalAccessException ignored) {
            }
        }
        map.put("name", enumConstant.toString());
        return map;
    }
}
