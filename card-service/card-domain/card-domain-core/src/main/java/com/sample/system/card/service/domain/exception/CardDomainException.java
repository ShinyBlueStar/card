package com.sample.system.card.service.domain.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
public class CardDomainException extends DomainException {

    private int status;
    private HttpStatus httpStatus;
    private Map<String, String> parameters;
    /** When set (e.g. from SSM), returned to client as-is instead of statusService lookup. */
    private String errorCode;
    /** When set (e.g. from SSM), returned to client as-is instead of statusService lookup. */
    private String errorMessage;

    public CardDomainException(String message) {
        super(message);
    }

    public CardDomainException(int status) {
        super("");
        this.status = status;
    }

    public CardDomainException(String message, int status, HttpStatus httpStatus) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
    }

    /** For passing through external error (e.g. SSM) to client: code and message shown as-is. */
    public CardDomainException(String message, String errorCode, String errorMessage, HttpStatus httpStatus) {
        super(message != null ? message : errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage != null ? errorMessage : message;
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.BAD_REQUEST;
        this.status = httpStatus != null ? httpStatus.value() : 0;
    }

    public CardDomainException(String message, int status, HttpStatus httpStatus, Map<String, String> parameters) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
        this.parameters = parameters;
    }

    public CardDomainException(String message, Throwable cause, int status){
        super(message, cause);
        this.status = status;
    }

}
