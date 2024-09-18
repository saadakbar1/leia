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
import com.grookage.leia.models.attributes.BooleanAttribute;
import com.grookage.leia.models.attributes.ByteAttribute;
import com.grookage.leia.models.attributes.DoubleAttribute;
import com.grookage.leia.models.attributes.EnumAttribute;
import com.grookage.leia.models.attributes.FloatAttribute;
import com.grookage.leia.models.attributes.IntegerAttribute;
import com.grookage.leia.models.attributes.LongAttribute;
import com.grookage.leia.models.attributes.MapAttribute;
import com.grookage.leia.models.attributes.ObjectAttribute;
import com.grookage.leia.models.attributes.SchemaAttribute;
import com.grookage.leia.models.attributes.SchemaAttributeAcceptor;
import com.grookage.leia.models.attributes.StringAttribute;
import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaValidationType;
import com.grookage.leia.models.schema.SchemaValidationVisitor;
import com.grookage.leia.validator.exception.SchemaValidationException;
import com.grookage.leia.validator.exception.ValidationErrorCode;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class SchemaValidationUtils {

    public static boolean valid(final SchemaDetails schemaDetails,
                                final Class<?> klass) {
        return valid(schemaDetails.getValidationType(), schemaDetails.getAttributes(), klass);
    }

    private static boolean valid(final SchemaValidationType validationType,
                                 Set<SchemaAttribute> attributes, final Class<?> klass) {
        final var fields = getAllFields(klass);
        if (!validSchema(validationType, attributes, fields)) {
            return false;
        }
        return attributes.stream().allMatch(each -> validAttribute(each, fields, validationType));
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
                                    + "schema. [Validation Failed : MODE STRICT]. The attributes are {}", mismatchedAttributes);
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
                                          final List<Field> fields, SchemaValidationType validationType) {
        final var field = fields.stream()
                .filter(each -> each.getName().equals(attribute.getName()))
                .findFirst().orElse(null);
        if (null == field) {
            return false;
        }
        return attribute.accept(new AbstractSchemaAttributeValidator(field.getType()) {
            @Override
            public Boolean accept(ArrayAttribute attribute) {
                return valid(attribute, field, validationType);
            }

            @Override
            public Boolean accept(MapAttribute attribute) {
                return valid(attribute, field, validationType);
            }

            @Override
            public Boolean accept(ObjectAttribute attribute) {
                return valid(validationType, attribute.getNestedAttributes(), field.getType());
            }
        });
    }

    private static Boolean valid(final SchemaAttribute attribute,
                                 final Class<?> klass,
                                 final SchemaValidationType validationType) {
        return attribute.accept(new AbstractSchemaAttributeValidator(klass) {

            @Override
            public Boolean accept(final ArrayAttribute attribute) {
                throw SchemaValidationException.error(ValidationErrorCode.NOT_SUPPORTED,
                                                      "Nested collection not supported");
            }

            @Override
            public Boolean accept(final MapAttribute attribute) {
                throw SchemaValidationException.error(ValidationErrorCode.NOT_SUPPORTED,
                                                      "Nested collection not supported");
            }

            @Override
            public Boolean accept(final ObjectAttribute attribute) {
                return valid(validationType, attribute.getNestedAttributes(), klass);
            }
        });
    }

    private static boolean valid(ArrayAttribute attribute, Field field,
                                 SchemaValidationType validationType) {
        if (!Collection.class.isAssignableFrom(field.getType())) {
            return false;
        }
        final var typeArguments = getTypeArguments(field);
        Type type = typeArguments[0];
        if (type instanceof Class<?> klass) {
            return valid(attribute.getItemAttribute(), klass, validationType);
        }
        throw SchemaValidationException.error(ValidationErrorCode.NOT_SUPPORTED,
                                              "Other types not supported");
    }

    private static boolean valid(MapAttribute attribute, Field field, SchemaValidationType validationType) {
        if (!Map.class.isAssignableFrom(field.getType())) {
            return false;
        }
        final var typeArguments = getTypeArguments(field);
        Type keyType = typeArguments[0];
        Type valueType = typeArguments[1];
        if (keyType instanceof Class<?> keyKlass && valueType instanceof Class<?> valueKlass) {
            return valid(attribute.getKeyAttribute(), keyKlass, validationType) &&
                    valid(attribute.getValueAttribute(), valueKlass, validationType);
        }
        throw SchemaValidationException.error(ValidationErrorCode.NOT_SUPPORTED,
                                              "Other types not supported");
    }

    private static Type[] getTypeArguments(Field field) {
        Type fieldType = field.getGenericType();

        if (fieldType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length == 0) {
                throw SchemaValidationException.error(ValidationErrorCode.INVALID_SCHEMAS,
                                                      String.format("No type arguments found for %s", field.getName()));
            }
            return typeArguments;
        } else {
            throw SchemaValidationException.error(ValidationErrorCode.INVALID_SCHEMAS,
                                                  "The field is not a parameterized type.");
        }
    }

    public static boolean valid(Class<?> klass, SchemaAttribute schemaAttribute) {
        return schemaAttribute.accept(new AbstractSchemaAttributeValidator(klass) {});
    }

    @AllArgsConstructor
    public abstract static class AbstractSchemaAttributeValidator implements SchemaAttributeAcceptor<Boolean> {
        private Class<?> klass;

        @Override
        public Boolean accept(BooleanAttribute attribute) {
            return Boolean.class.isAssignableFrom(klass);
        }

        @Override
        public Boolean accept(ByteAttribute attribute) {
            return Byte.class.isAssignableFrom(klass);
        }

        @Override
        public Boolean accept(DoubleAttribute attribute) {
            return Double.class.isAssignableFrom(klass);
        }

        @Override
        public Boolean accept(EnumAttribute attribute) {
            return Enum.class.isAssignableFrom(klass);
        }

        @Override
        public Boolean accept(FloatAttribute attribute) {
            return Float.class.isAssignableFrom(klass);
        }

        @Override
        public Boolean accept(IntegerAttribute attribute) {
            return Integer.class.isAssignableFrom(klass);
        }

        @Override
        public Boolean accept(LongAttribute attribute) {
            return Long.class.isAssignableFrom(klass);
        }

        @Override
        public Boolean accept(final StringAttribute attribute) {
            return String.class.isAssignableFrom(klass);
        }

        @Override
        public Boolean accept(final ArrayAttribute attribute) {
            return Collection.class.isAssignableFrom(klass);
        }

        @Override
        public Boolean accept(final MapAttribute attribute) {
            return Map.class.isAssignableFrom(klass);
        }

        @Override
        public Boolean accept(final ObjectAttribute attribute) {
            return Object.class.isAssignableFrom(klass);
        }
    }
}
