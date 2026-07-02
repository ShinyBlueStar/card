package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.valueObject.CardTypeId;
import lombok.*;

import java.io.Serializable;

public class CardType extends AggregateRoot<CardTypeId> implements Serializable {

    private Integer code;
    private String name;
    private String description;
    private Boolean isActive;

    public CardType(CardTypeId id,
                        Integer code,
                        String name,
                        String description,
                        Boolean isActive) {
        this.setId(id);
        this.code = code;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
    }

    /** No‑args constructor (required for JPA / manual creation) */
    public CardType() {
    }

    // ---------- getters & setters ----------

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    // ---------- business helpers ----------
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Activate the card type
     */
    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}