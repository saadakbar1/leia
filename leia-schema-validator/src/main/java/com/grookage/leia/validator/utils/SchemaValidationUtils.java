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

@UtilityClass
@Slf4j
public class SchemaValidationUtils {

    public static boolean valid(final SchemaDetails schemaDetails,
                                final Class<?> klass) {
        final var fields = getAllFields(klass);
        final var validationType = schemaDetails.getValidationType();
        final var allAttributesListed = fields.stream().allMatch(each -> schemaDetails.hasAttribute(each.getName()));
        if (!allAttributesListed && validationType == SchemaValidationType.STRICT) {
            log.debug("There seems to attributes present in the class definition that are not in the schema. [Validation Failed]");
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
        return attribute.accept(new SchemaAttributeAcceptor<>() {
            @Override
            public Boolean accept(ArrayAttribute attribute) {
                return field.getType().isInstance(Collection.class);
            }

            @Override
            public Boolean accept(BooleanAttribute attribute) {
                return field.getType().isInstance(Boolean.class);
            }

            @Override
            public Boolean accept(ByteAttribute attribute) {
                return field.getType().isInstance(Byte.class);
            }

            @Override
            public Boolean accept(DoubleAttribute attribute) {
                return field.getType().isInstance(Double.class);
            }

            @Override
            public Boolean accept(EnumAttribute attribute) {
                return field.getType().isInstance(Enum.class);
            }

            @Override
            public Boolean accept(FloatAttribute attribute) {
                return field.getType().isInstance(Float.class);
            }

            @Override
            public Boolean accept(IntegerAttribute attribute) {
                return field.getType().isInstance(Integer.class);
            }

            @Override
            public Boolean accept(LongAttribute attribute) {
                return field.getType().isInstance(Long.class);
            }

            @Override
            public Boolean accept(MapAttribute attribute) {
                return field.getType().isInstance(Map.class);
            }

            @Override
            public Boolean accept(ObjectAttribute attribute) {
                return field.getType().isInstance(Object.class);
            }

            @Override
            public Boolean accept(StringAttribute attribute) {
                return field.getType().isInstance(String.class);
            }
        });
    }
}
