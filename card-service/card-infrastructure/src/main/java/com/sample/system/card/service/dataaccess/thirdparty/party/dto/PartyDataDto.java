package com.sample.system.card.service.dataaccess.thirdparty.party.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartyDataDto {
    @JsonProperty("partyIndividual") private PartyIndividualDto partyIndividual;
    @JsonProperty("partyBusiness")   private PartyBusinessDto partyBusiness;

    // getters/setters
}