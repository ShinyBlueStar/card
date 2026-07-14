package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.valueObject.FeeProfileId;
import lombok.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeProfile extends AggregateRoot<FeeProfileId> implements Serializable {

    private String name;

    private List<Fee> fees = new ArrayList<>();

    private List<CardProfile> cardProfiles = new ArrayList<>();

    private Boolean isActive;

}