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

package com.grookage.leia.common.utils;

import com.google.common.collect.Sets;
import com.grookage.leia.common.exception.SchemaValidationException;
import com.grookage.leia.common.exception.ValidationErrorCode;
import com.grookage.leia.models.attributes.ArrayAttribute;
import com.grookage.leia.models.attributes.MapAttribute;
import com.grookage.leia.models.attributes.ObjectAttribute;
import com.grookage.leia.models.attributes.SchemaAttribute;
import com.grookage.leia.models.attributes.SchemaAttributeHandler;
import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaValidationType;
import com.grookage.leia.models.schema.SchemaValidationVisitor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
            klass -> attribute -> ClassUtils.isAssignable(klass, attribute.getType().getAssignableClass());

    static Function<SchemaAttribute, Boolean> throwException = attribute -> {
        log.error("Attribute {} of type {} not compatible with the type provided",
                attribute.getName(), attribute.getType());
        throw SchemaValidationException.error(ValidationErrorCode.INVALID_SCHEMAS);
    };

    public boolean valid(final SchemaDetails schemaDetails,
                         final Class<?> klass) {
        return valid(schemaDetails.getValidationType(), schemaDetails.getAttributes(), klass);
    }

    public boolean valid(final SchemaValidationType validationType,
                         final Set<SchemaAttribute> attributes,
                         final Class<?> klass) {

        final var fields = Utils.getAllFields(klass);
        if (!validSchema(validationType, attributes, fields)) {
            return false;
        }
        return attributes.stream().allMatch(
                each -> validAttribute(each, fields, validationType));
    }

    private boolean validSchema(final SchemaValidationType validationType,
                                final Set<SchemaAttribute> attributes,
                                final List<Field> fields) {
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

    private boolean validAttribute(final SchemaAttribute attribute,
                                   final List<Field> fields,
                                   final SchemaValidationType validationType) {
        final var field = fields.stream()
                .filter(each -> each.getName().equals(attribute.getName()))
                .findFirst().orElse(null);
        return null != field && valid(validationType, attribute, field.getGenericType());
    }

    public boolean valid(final SchemaValidationType validationType,
                         final SchemaAttribute attribute,
                         final Type type) {
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

    private boolean valid(final SchemaValidationType validationType,
                          final SchemaAttribute attribute,
                          final Class<?> klass) {
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
                return ClassUtils.isAssignable(klass, Collection.class) && attribute.getElementAttribute() == null;
            }

            @Override
            public Boolean accept(MapAttribute attribute) {
                return ClassUtils.isAssignable(klass, Map.class) && attribute.getKeyAttribute() == null;
            }

            @Override
            public Boolean accept(ObjectAttribute attribute) {
                // Handling plain Object.class
                if (klass.equals(Object.class) && attribute.getNestedAttributes() == null) {
                    return true;
                }
                return valid(validationType, attribute.getNestedAttributes(), klass);
            }
        });
    }

    private boolean valid(final SchemaValidationType validationType,
                          final SchemaAttribute attribute,
                          final ParameterizedType parameterizedType) {
        return attribute.accept(new SchemaAttributeHandler<>(throwException) {
            @Override
            public Boolean accept(ArrayAttribute attribute) {
                if (attribute.getElementAttribute() == null) {
                    return true;
                }
                final var rawType = (Class<?>) parameterizedType.getRawType();
                if (!ClassUtils.isAssignable(rawType, attribute.getType().getAssignableClass())) {
                    return false;
                }
                final var typeArguments = Utils.getTypeArguments(parameterizedType);
                return valid(validationType, attribute.getElementAttribute(), typeArguments[0]);
            }

            @Override
            public Boolean accept(MapAttribute attribute) {
                if (attribute.getKeyAttribute() == null) {
                    return true;
                }
                final var rawType = (Class<?>) parameterizedType.getRawType();
                if (!ClassUtils.isAssignable(rawType, attribute.getType().getAssignableClass())) {
                    return false;
                }
                final var typeArguments = Utils.getTypeArguments(parameterizedType);
                return valid(validationType, attribute.getKeyAttribute(), typeArguments[0]) &&
                        valid(validationType, attribute.getValueAttribute(), typeArguments[1]);
            }
        });
    }

    private boolean valid(final SchemaValidationType validationType,
                          final SchemaAttribute attribute,
                          final GenericArrayType arrayType) {
        return attribute.accept(new SchemaAttributeHandler<>(throwException) {
            @Override
            public Boolean accept(final ArrayAttribute attribute) {
                return valid(validationType, attribute.getElementAttribute(), arrayType.getGenericComponentType());
            }
        });
    }

    public boolean valid(final Class<?> klass,
                         final SchemaAttribute schemaAttribute) {
        return schemaAttribute.accept(new SchemaAttributeHandler<>(assignableCheckFunction.apply(klass)) {
        });
    }
}
