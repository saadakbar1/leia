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

import com.google.common.collect.Sets;
import com.grookage.leia.models.attributes.ArrayAttribute;
import com.grookage.leia.models.attributes.MapAttribute;
import com.grookage.leia.models.attributes.ObjectAttribute;
import com.grookage.leia.models.attributes.SchemaAttribute;
import com.grookage.leia.models.attributes.SchemaAttributeHandler;
import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaValidationType;
import com.grookage.leia.models.schema.SchemaValidationVisitor;
import com.grookage.leia.validator.exception.SchemaValidationException;
import com.grookage.leia.validator.exception.ValidationErrorCode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class SchemaValidationUtils {
    static Function<Class<?>, Function<SchemaAttribute, Boolean>> assignableCheckFunction =
            klass -> attribute -> attribute.getType().getAssignableClass().isAssignableFrom(klass);

    static Function<SchemaAttribute, Boolean> throwException = attribute -> {
        log.error("Attribute {} of type {} not compatible with the type provided",
                  attribute.getName(), attribute.getType());
        throw SchemaValidationException.error(ValidationErrorCode.INVALID_SCHEMAS);
    };

    public static boolean valid(final SchemaDetails schemaDetails,
                                final Class<?> klass) {
        return valid(schemaDetails.getValidationType(), schemaDetails.getAttributes(), klass);
    }

    public static boolean valid(final SchemaValidationType validationType,
                                Set<SchemaAttribute> attributes, final Class<?> klass) {

        final var fields = getAllFields(klass);
        if (!validSchema(validationType, attributes, fields)) {
            return false;
        }
        return attributes.stream().allMatch(
                each -> validAttribute(each, fields, validationType));
    }

    private static boolean validSchema(SchemaValidationType validationType, Set<SchemaAttribute> attributes,
                                       List<Field> fields) {
        final var fieldNames = fields.stream()
                .map(Field::getName)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        final var attributesListed = attributes.stream()
                .map(SchemaAttribute::getName)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        return validationType.accept(new SchemaValidationVisitor<>() {
            @Override
            public Boolean strict() {
                final var mismatchedAttributes = Sets.symmetricDifference(fieldNames, attributesListed);
                if (!mismatchedAttributes.isEmpty()) {
                    log.error(
                            "There seems to be a mismatch in the attributes present in the class definition and "
                                    + "schema. [Validation Failed : MODE STRICT]. The attributes are {}",
                            mismatchedAttributes);
                }
                return mismatchedAttributes.isEmpty();
            }

            @Override
            public Boolean matching() {
                final var attributesMissing = Sets.difference(attributesListed, fieldNames);
                if (!attributesMissing.isEmpty()) {
                    log.error("Some attributes are missing in the class definition" +
                                      "[Validation Failed : MODE MATCHING]. The attributes are {}", attributesMissing);
                }
                return attributesMissing.isEmpty();
            }
        });
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    private static boolean validAttribute(final SchemaAttribute attribute,
                                          List<Field> fields, SchemaValidationType validationType) {
        final var field = fields.stream()
                .filter(each -> each.getName().equals(attribute.getName()))
                .findFirst().orElse(null);
        return null != field && valid(validationType, attribute, field.getGenericType());
    }

    public static boolean valid(final SchemaValidationType validationType,
                                SchemaAttribute attribute, final Type type) {
        if (type instanceof Class<?> klass) {
            return valid(validationType, attribute, klass);
        } else if (type instanceof ParameterizedType parameterizedType) {
            return valid(validationType, attribute, parameterizedType);
        } else if (type instanceof GenericArrayType arrayType) {
            return valid(validationType, attribute, arrayType);
        } else {
            throw SchemaValidationException.error(ValidationErrorCode.NOT_SUPPORTED);
        }
    }

    private static boolean valid(final SchemaValidationType validationType,
                                 SchemaAttribute attribute, final Class<?> klass) {
        return attribute.accept(new SchemaAttributeHandler<>(
                assignableCheckFunction.apply(klass)) {
            @Override
            public Boolean accept(ArrayAttribute attribute) {
                if (klass.isArray()) {
                    if (attribute.getElementAttribute() == null) {
                        return true;
                    }
                    return valid(validationType, attribute.getElementAttribute(), klass.getComponentType());
                }
                return Collection.class.isAssignableFrom(klass) && attribute.getElementAttribute() == null;
            }

            @Override
            public Boolean accept(MapAttribute attribute) {
                return Map.class.isAssignableFrom(klass) && attribute.getKeyAttribute() == null;
            }

            @Override
            public Boolean accept(ObjectAttribute attribute) {
                return valid(validationType, attribute.getNestedAttributes(), klass);
            }
        });
    }

    private static boolean valid(final SchemaValidationType validationType,
                                 SchemaAttribute attribute, final ParameterizedType parameterizedType) {
        return attribute.accept(new SchemaAttributeHandler<>(throwException) {
            @Override
            public Boolean accept(ArrayAttribute attribute) {
                if (attribute.getElementAttribute() == null) {
                    return true;
                }
                final var rawType = (Class<?>) parameterizedType.getRawType();
                if (!attribute.getType().getAssignableClass().isAssignableFrom(rawType)) {
                    return false;
                }
                final var typeArguments = getTypeArguments(parameterizedType);
                return valid(validationType, attribute.getElementAttribute(), typeArguments[0]);
            }

            @Override
            public Boolean accept(MapAttribute attribute) {
                if (attribute.getKeyAttribute() == null) {
                    return true;
                }
                final var rawType = (Class<?>) parameterizedType.getRawType();
                if (!attribute.getType().getAssignableClass().isAssignableFrom(rawType)) {
                    return false;
                }
                final var typeArguments = getTypeArguments(parameterizedType);
                return valid(validationType, attribute.getKeyAttribute(), typeArguments[0]) &&
                        valid(validationType, attribute.getValueAttribute(), typeArguments[1]);
            }
        });
    }

    private static boolean valid(final SchemaValidationType validationType,
                                 SchemaAttribute attribute, final GenericArrayType arrayType) {
        return attribute.accept(new SchemaAttributeHandler<>(throwException) {
            @Override
            public Boolean accept(final ArrayAttribute attribute) {
                return valid(validationType, attribute.getElementAttribute(), arrayType.getGenericComponentType());
            }
        });
    }

    private static Type[] getTypeArguments(ParameterizedType parameterizedType) {
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length == 0) {
            throw SchemaValidationException.error(ValidationErrorCode.INVALID_SCHEMAS,
                                                  String.format("No type arguments found for %s", parameterizedType));
        }
        return typeArguments;
    }

    public static boolean valid(Class<?> klass, SchemaAttribute schemaAttribute) {
        return schemaAttribute.accept(new SchemaAttributeHandler<>(assignableCheckFunction.apply(klass)) {});
    }
}
