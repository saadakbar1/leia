package com.grookage.leia.common.utils;

import com.grookage.leia.common.exception.SchemaValidationException;
import com.grookage.leia.common.exception.ValidationErrorCode;
import com.grookage.leia.models.annotations.Optional;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class Utils {
    public List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    public Type[] getTypeArguments(ParameterizedType parameterizedType) {
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length == 0) {
            throw SchemaValidationException.error(ValidationErrorCode.INVALID_SCHEMAS,
                    String.format("No type arguments found for %s", parameterizedType));
        }
        return typeArguments;
    }

    public Set<String> getEnumValues(Class<?> klass) {
        return Arrays.stream(klass.getEnumConstants())
                .map(enumConstant -> ((Enum<?>) enumConstant).name())
                .collect(Collectors.toSet());
    }

    private boolean isOptional(Type type) {
        if (type instanceof Class<?> klass) {
            return isOptional(klass);
        }
        return false;
    }

    private boolean isOptional(Class<?> klass) {
        return klass.isAnnotationPresent(Optional.class);
    }

    private boolean isOptional(Field field) {
        return field.isAnnotationPresent(Optional.class);
    }
}
