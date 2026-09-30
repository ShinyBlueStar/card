package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.cardCategory.CreateCardCategoryCommand;
import com.sample.system.card.service.domain.command.cardCategory.UpdateCardCategoryCommand;
import com.sample.system.card.service.domain.entity.CardCategory;
import com.sample.system.card.service.domain.response.cardCategory.*;
import com.sample.system.card.service.domain.valueObject.CardCategoryId;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardCategoryDataMapperTest {

    private final CardCategoryDataMapper mapper = new CardCategoryDataMapper();

    private static CardCategory category(long id, Integer code) {
        return new CardCategory(new CardCategoryId(id), code, "Gold", "gold cards", true);
    }

    @Test
    void createCommandIsMappedWithNumericCode() {
        CardCategory category = mapper.createCommandToCardCategory(
                new CreateCardCategoryCommand(1L, "12", "Gold", "desc", true));

        assertThat(category.getId()).isNull();
        assertThat(category.getCode()).isEqualTo(12);
        assertThat(category.getName()).isEqualTo("Gold");
        assertThat(category.getDescription()).isEqualTo("desc");
        assertThat(category.getIsActive()).isTrue();
    }

    @Test
    void createCommandWithoutCodeGivesNullCode() {
        assertThat(mapper.createCommandToCardCategory(
                new CreateCardCategoryCommand(1L, null, "Gold", null, null)).getCode()).isNull();
    }

    @Test
    void createCommandWithNonNumericCodeIsRejected() {
        assertThatThrownBy(() -> mapper.createCommandToCardCategory(
                new CreateCardCategoryCommand(1L, "abc", "Gold", null, true)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("abc");
    }

    @Test
    void categoryIsMappedToCreateResponse() {
        CreateCardCategoryResponse response = mapper.cardCategoryToCreateResponse(category(5L, 12), "ok");

        assertThat(response.getCardCategoryId()).isEqualTo(5L);
        assertThat(response.getCardTypeId()).isNull();
        assertThat(response.getCode()).isEqualTo("12");
        assertThat(response.getName()).isEqualTo("Gold");
        assertThat(response.getDescription()).isEqualTo("gold cards");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getMessage()).isEqualTo("ok");
    }

    @Test
    void updateChangesProvidedFieldsOnly() {
        CardCategory category = category(1L, 12);

        mapper.updateCardCategoryFromCommand(category,
                new UpdateCardCategoryCommand(1L, 1L, "12", "Platinum", "new", false));

        assertThat(category.getName()).isEqualTo("Platinum");
        assertThat(category.getDescription()).isEqualTo("new");
        assertThat(category.getIsActive()).isFalse();
        assertThat(category.getCode()).isEqualTo(12);
    }

    @Test
    void updateIgnoresNullFields() {
        CardCategory category = category(1L, 12);

        mapper.updateCardCategoryFromCommand(category,
                new UpdateCardCategoryCommand(1L, 1L, "12", null, null, null));

        assertThat(category.getName()).isEqualTo("Gold");
        assertThat(category.getDescription()).isEqualTo("gold cards");
        assertThat(category.getIsActive()).isTrue();
    }

    @Test
    void categoryIsMappedToUpdateResponse() {
        UpdateCardCategoryResponse response = mapper.cardCategoryToUpdateResponse(category(7L, 1), "msg");

        assertThat(response.getCardCategoryId()).isEqualTo(7L);
        assertThat(response.getMessage()).isEqualTo("msg");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void categoryIsMappedToGetResponseWithAndWithoutCode() {
        GetCardCategoryResponse withCode = mapper.cardCategoryToGetResponse(category(1L, 42));
        GetCardCategoryResponse withoutCode = mapper.cardCategoryToGetResponse(category(2L, null));

        assertThat(withCode.code()).isEqualTo("42");
        assertThat(withCode.id()).isEqualTo(1L);
        assertThat(withoutCode.code()).isNull();
    }

    @Test
    void listOfCategoriesIsMapped() {
        assertThat(mapper.cardCategoriesToGetResponses(List.of(category(1L, 1), category(2L, 2))))
                .extracting(GetCardCategoryResponse::id).containsExactly(1L, 2L);
    }

    @Test
    void pageIsMappedWithPagingInfo() {
        PageImpl<CardCategory> page = new PageImpl<>(List.of(category(1L, 1)), PageRequest.of(2, 1), 5);

        CardCategoryListResponse response = mapper.cardCategoriesToGetListResponse(page);

        assertThat(response.getList()).hasSize(1);
        assertThat(response.getNumber()).isEqualTo(2);
        assertThat(response.getSize()).isEqualTo(1);
        assertThat(response.getTotalElements()).isEqualTo(5);
        assertThat(response.getTotalPages()).isEqualTo(5);
    }

    @Test
    void nullPageGivesNull() {
        assertThat(mapper.cardCategoriesToGetListResponse(null)).isNull();
    }

    @Test
    void activeFlagFollowsActivateAndDeactivate() {
        CardCategory category = new CardCategory();
        assertThat(category.isActive()).isFalse();

        category.activate();
        assertThat(category.isActive()).isTrue();

        category.deactivate();
        assertThat(category.isActive()).isFalse();

        category.setCode(3);
        assertThat(category.getCode()).isEqualTo(3);
    }
}
