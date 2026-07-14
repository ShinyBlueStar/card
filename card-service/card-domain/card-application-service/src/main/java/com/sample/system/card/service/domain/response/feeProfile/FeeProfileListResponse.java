package com.sample.system.card.service.domain.response.feeProfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sample.system.card.service.domain.response.base.PageResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@ToString
public class FeeProfileListResponse extends PageResponse {

    @JsonProperty("list")
    private List<GetFeeProfileResponse> list;
}

