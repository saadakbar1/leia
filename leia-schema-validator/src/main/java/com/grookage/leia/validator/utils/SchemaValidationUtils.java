/*
 * Copyright (c) 2024. Koushik R <rkoushik.14@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grookage.leia.validator.utils;

import com.grookage.leia.models.attributes.*;
import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaValidationType;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class SchemaValidationUtils {

    public static boolean valid(final SchemaDetails schemaDetails,
                                final Class<?> klass) {
        final var fields = getAllFields(klass);
        final var validationType = schemaDetails.getValidationType();
        final var attributedNotListed = fields.stream().filter(each -> !schemaDetails.hasAttribute(each.getName()))
                .collect(Collectors.toSet());
        if (!attributedNotListed.isEmpty() &&
                validationType == SchemaValidationType.STRICT) {
            log.error("There seems to be attributes present in the class definition that are not in the schema. " +
                            "[Validation Failed]. The extra attributes are {}",
                    attributedNotListed);
            return false;
        }
        return schemaDetails.getAttributes().stream().allMatch(each ->
                valid(each, fields));
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    private static Optional<Field> getField(final List<Field> fields,
                                            final String attributeName) {
        return fields.stream()
                .filter(each -> each.getName().equals(attributeName)).findFirst();
    }

    private static boolean valid(final SchemaAttribute attribute,
                                 final List<Field> fields) {
        final var field = getField(fields, attribute.getName()).orElse(null);
        if (null == field) {
            return attribute.isOptional();
        }
        return valid(field.getType(), attribute);
    }

    public static boolean valid(Class<?> fieldType, SchemaAttribute attribute) {
        final Class<?> assignableKlass = attribute.accept(new SchemaAttributeAcceptor<>() {
            @Override
            public Class<?> accept(ArrayAttribute attribute) {
                return Collection.class;
            }

            @Override
            public Class<?> accept(BooleanAttribute attribute) {
                return Boolean.class;
            }

            @Override
            public Class<?> accept(ByteAttribute attribute) {
                return Byte.class;
            }

            @Override
            public Class<?> accept(DoubleAttribute attribute) {
                return Double.class;
            }

            @Override
            public Class<?> accept(EnumAttribute attribute) {
                return Enum.class;
            }

            @Override
            public Class<?> accept(FloatAttribute attribute) {
                return Float.class;
            }

            @Override
            public Class<?> accept(IntegerAttribute attribute) {
                return Integer.class;
            }

            @Override
            public Class<?> accept(LongAttribute attribute) {
                return Long.class;
            }

            @Override
            public Class<?> accept(MapAttribute attribute) {
                return Map.class;
            }

            @Override
            public Class<?> accept(ObjectAttribute attribute) {
                return Object.class;
            }

            @Override
            public Class<?> accept(StringAttribute attribute) {
                return String.class;
            }
        });
        return assignableKlass.isAssignableFrom(fieldType);
    }
}
