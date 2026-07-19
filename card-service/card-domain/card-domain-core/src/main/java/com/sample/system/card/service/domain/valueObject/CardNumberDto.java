package com.sample.system.card.service.domain.valueObject;

import com.sample.system.card.service.domain.entity.CardProfile;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class CardNumberDto {
    private final CardProfile cardProfile;

    public CardNumberDto(CardProfile profile) {
        this.cardProfile = profile;
    }
}
