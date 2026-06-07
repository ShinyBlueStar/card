package com.sample.system.card.service.dataaccess.thirdparty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThirdPartyException extends Exception{

    private final int resultCode;
    private final int httpStatusCode;
    private final String channelMessage;
    private final String completeResponse;

    public ThirdPartyException(String message, int status, String channelMessage, int httpStatusCode, String completeResponse){
        super(message);
        this.resultCode = status;
        this.channelMessage = channelMessage;
        this.httpStatusCode = httpStatusCode;
        this.completeResponse = completeResponse;
    }
}
