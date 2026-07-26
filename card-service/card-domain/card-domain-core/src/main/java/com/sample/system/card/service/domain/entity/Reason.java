package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.enums.ReasonGroup;
import com.sample.system.card.service.domain.valueObject.ReasonId;
import lombok.*;

import java.io.Serializable;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reason extends BaseEntity<ReasonId> implements Serializable {

    private final Integer code;
    private final String title;
    private final String reason;
    private final ReasonGroup groupId;
}

