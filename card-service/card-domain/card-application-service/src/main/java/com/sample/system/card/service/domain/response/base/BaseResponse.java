package com.sample.system.card.service.domain.response.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.MDC;

@ToString
@Getter
@Setter
public class BaseResponse<T> {

    private Boolean success;
    private T data;
    private String trackingId;
    private Long doTimeStamp;
    private ErrorDetail errorDetail;

    public BaseResponse() {
    }

    public BaseResponse(Boolean success) {
        this.success = success;
        this.trackingId = MDC.get("uuid");
        this.doTimeStamp = System.currentTimeMillis();
    }

    public BaseResponse(Boolean success, T data) {
        this(success);
        this.data = data;
    }

    public BaseResponse(Boolean success, ErrorDetail errorDetail) {
        this(success);
        this.errorDetail = errorDetail;
    }
}
