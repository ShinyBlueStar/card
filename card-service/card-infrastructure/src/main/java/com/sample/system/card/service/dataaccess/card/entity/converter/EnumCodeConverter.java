package com.sample.system.card.service.dataaccess.card.entity.converter;

import com.sample.system.card.service.domain.enums.CodedEnum;
import jakarta.persistence.AttributeConverter;

import java.util.Arrays;
import java.util.Optional;

/**
 * اینام را در انتیتی نگه میدارد ولی در دیتابیس همان کد عددی قبلی ذخیره میشود
 */
public abstract class EnumCodeConverter<E extends Enum<E> & CodedEnum> implements AttributeConverter<E, Integer> {

    private final Class<E> type;

    protected EnumCodeConverter(Class<E> type) {
        this.type = type;
    }

    @Override
    public Integer convertToDatabaseColumn(E value) {
        return value == null ? null : value.getCode();
    }

    @Override
    public E convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return find(type, code).orElseThrow(() -> new IllegalArgumentException(
                "Unknown " + type.getSimpleName() + " code: " + code));
    }

    public static <E extends Enum<E> & CodedEnum> Optional<E> find(Class<E> type, Integer code) {
        return Arrays.stream(type.getEnumConstants())
                .filter(value -> value.getCode().equals(code))
                .findFirst();
    }
}
