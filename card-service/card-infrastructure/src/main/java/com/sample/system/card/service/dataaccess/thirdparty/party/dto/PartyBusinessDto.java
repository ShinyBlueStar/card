package com.sample.system.card.service.dataaccess.thirdparty.party.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartyBusinessDto {

    @JsonProperty("id")
    private PartyIndividualDto.IdWrapper id;

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
    private String type;  // "individual" or "business"

    @JsonProperty("email")
    private String email;

    @JsonProperty("birthDate")
    private String birthDate;

    @JsonProperty("isActive")
    private Boolean isActive;

}
