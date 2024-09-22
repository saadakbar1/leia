package com.grookage.leia.models.attributes;

import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public abstract class SchemaAttributeAdapter<T> implements SchemaAttributeAcceptor<T> {
    private final Function<SchemaAttribute,T> defaultValueFunction;

    @Override
    public T accept(BooleanAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }

    @Override
    public T accept(ByteAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }

    @Override
    public T accept(DoubleAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }

    @Override
    public T accept(EnumAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }

    @Override
    public T accept(FloatAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }

    @Override
    public T accept(IntegerAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }

    @Override
    public T accept(LongAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }

    @Override
    public T accept(final StringAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }

    @Override
    public T accept(final ArrayAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }

    @Override
    public T accept(final MapAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }

    @Override
    public T accept(final ObjectAttribute attribute) {
        return defaultValueFunction.apply(attribute);
    }
}
