package com.sample.system.card.service.dataaccess.thirdparty.party.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartyIndividualDto {
    @JsonProperty("id")
    private IdWrapper id; // {"value": 44}

    @JsonProperty("nationalCode")
    private String nationalCode;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("latinFirstName")
    private String latinFirstName;

    @JsonProperty("latinLastName")
    private String latinLastName;

    @JsonProperty("fatherName")
    private String fatherName;

    @JsonProperty("type")
    private String type;

    @JsonProperty("email")
    private String email;

    @JsonProperty("birthDate")
    private String birthDate;

    @JsonProperty("isActive")
    private Boolean isActive;

    private List<String> streetAddresses;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdWrapper {
        @JsonProperty("value")
        public Long value;
    }

}
