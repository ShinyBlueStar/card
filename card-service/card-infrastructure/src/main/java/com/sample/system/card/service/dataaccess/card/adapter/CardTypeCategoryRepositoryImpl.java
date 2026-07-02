package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.CardTypeCategoryCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.CardTypeCategoryDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.CardTypeCategoryJpaRepository;
import com.sample.system.card.service.domain.entity.CardTypeCategory;
import com.sample.system.card.service.domain.ports.output.repository.CardTypeCategoryRepository;
import com.sample.system.card.service.domain.valueObject.CardTypeCategoryId;
import com.sample.system.card.service.domain.valueObject.CardTypeId;
import com.sample.system.card.service.domain.valueObject.CardCategoryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * CardTypeCategory Repository Implementation
 * Following Adapter pattern for infrastructure layer
 */
@Component
@RequiredArgsConstructor
public class CardTypeCategoryRepositoryImpl implements CardTypeCategoryRepository {

    private final CardTypeCategoryJpaRepository cardTypeCategoryJpaRepository;
    private final CardTypeCategoryDataAccessMapper cardTypeCategoryDataAccessMapper;

    @Override
    public CardTypeCategory save(CardTypeCategory cardTypeCategory) {
        CardTypeCategoryCommandEntity entity = cardTypeCategoryDataAccessMapper.toEntity(cardTypeCategory);
        CardTypeCategoryCommandEntity savedEntity = cardTypeCategoryJpaRepository.save(entity);
        return cardTypeCategoryDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CardTypeCategory> findById(CardTypeCategoryId cardTypeCategoryId) {
        return cardTypeCategoryJpaRepository.findById(cardTypeCategoryId.getValue())
                .map(cardTypeCategoryDataAccessMapper::toDomain);
    }

    @Override
    public Optional<CardTypeCategory> findByCardTypeAndCardCategory(Long cardTypeId, Long cardCategoryId) {
        return cardTypeCategoryJpaRepository.findByCardTypeIdAndCardCategoryId(cardTypeId, cardCategoryId)
                .map(cardTypeCategoryDataAccessMapper::toDomain);
    }

    @Override
    public List<CardTypeCategory> findActiveCardTypeCategories() {
        return cardTypeCategoryJpaRepository.findActiveCardTypeCategories()
                .stream()
                .map(cardTypeCategoryDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public List<CardTypeCategory> findByCardType(CardTypeId cardTypeId) {
        return cardTypeCategoryJpaRepository.findByCardTypeId(cardTypeId.getValue())
                .stream()
                .map(cardTypeCategoryDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public List<CardTypeCategory> findByCardCategory(CardCategoryId cardCategoryId) {
        return cardTypeCategoryJpaRepository.findByCardCategoryId(cardCategoryId.getValue())
                .stream()
                .map(cardTypeCategoryDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public List<CardTypeCategory> findAll() {
        return cardTypeCategoryJpaRepository.findAll()
                .stream()
                .map(cardTypeCategoryDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCardTypeAndCardCategory(CardTypeId cardTypeId, CardCategoryId cardCategoryId) {
        return cardTypeCategoryJpaRepository.existsByCardTypeIdAndCardCategoryId(cardTypeId.getValue(), cardCategoryId.getValue());
    }

    @Override
    public long count() {
        return cardTypeCategoryJpaRepository.count();
    }

    @Override
    public void deleteById(CardTypeCategoryId cardTypeCategoryId) {
        cardTypeCategoryJpaRepository.deleteById(cardTypeCategoryId.getValue());
    }
}
