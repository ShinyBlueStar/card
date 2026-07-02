package com.sample.system.card.service.domain.response.cardType;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sample.system.card.service.domain.response.base.PageResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CardTypeListResponse extends PageResponse {

    @JsonProperty("list")
    private List<GetCardTypeResponse> list;
}

