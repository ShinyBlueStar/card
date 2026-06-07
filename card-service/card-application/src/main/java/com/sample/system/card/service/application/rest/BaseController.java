package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.response.base.BaseResponse;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

// همه کنترلرها از این کلاس ارث بری میکنند
public abstract class BaseController {

    protected <T> ResponseEntity<BaseResponse<T>> ok(T data) {
        return ResponseEntity.ok(new BaseResponse<>(true, data));
    }

    protected Map<String, String> searchParams(BaseSearchQuery baseSearchQuery) {
        return baseSearchQuery != null ? baseSearchQuery.getMap() : new HashMap<>();
    }
}
