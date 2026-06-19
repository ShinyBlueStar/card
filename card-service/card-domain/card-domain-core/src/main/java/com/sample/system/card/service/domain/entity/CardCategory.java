package com.sample.system.card.service.domain.entity;

import com.sample.system.card.service.domain.valueObject.CardCategoryId;

import java.io.Serializable;

public class CardCategory extends AggregateRoot<CardCategoryId> implements Serializable {

    private Integer code;
    private String name;
    private String description;
    private Boolean isActive;

    /** All‑args constructor */
    public CardCategory(CardCategoryId id,
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
    public CardCategory() {
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

    public void activate()   { this.isActive = true; }
    public void deactivate() { this.isActive = false; }
}