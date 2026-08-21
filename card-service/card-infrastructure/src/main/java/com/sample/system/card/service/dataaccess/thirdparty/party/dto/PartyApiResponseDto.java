package com.sample.system.card.service.dataaccess.thirdparty.party.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO برای پاسخ کامل API Party
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartyApiResponseDto {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("data")
    private PartyDataDto data;

    @JsonProperty("message")
    private String message;
}