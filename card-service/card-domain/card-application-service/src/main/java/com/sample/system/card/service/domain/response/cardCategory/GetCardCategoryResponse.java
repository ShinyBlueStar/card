package com.sample.system.card.service.domain.response.cardCategory;

public record GetCardCategoryResponse(
        Long id,
        String code,
        String name,
        String description,
        Boolean isActive) {
}