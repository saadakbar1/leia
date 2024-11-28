package com.grookage.leia.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.grookage.leia.models.ResourceHelper;
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
import com.grookage.leia.models.schema.SchemaValidationType;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@UtilityClass
public class ValidationUtils {
    public static List<String> validate(JsonNode jsonNode,
                                        SchemaValidationType validationType,
                                        Set<SchemaAttribute> schemaAttributes) {
        List<String> validationErrors = new ArrayList<>();

        Map<String, SchemaAttribute> schemaMap = new HashMap<>();
        for (SchemaAttribute attribute : schemaAttributes) {
            schemaMap.put(attribute.getName(), attribute);
        }

        // Validate extra fields in case of Strict Validation
        if (validationType == SchemaValidationType.STRICT) {
            jsonNode.fieldNames().forEachRemaining(fieldName -> {
                if (!schemaMap.containsKey(fieldName)) {
                    validationErrors.add("Unexpected field: " + fieldName);
                }
            });
        }

        // Validate missing and type mismatched fields
        for (SchemaAttribute attribute : schemaAttributes) {
            final var fieldName = attribute.getName();
            // Check the attribute only if the jsonNode is an object
            if (jsonNode.isObject() && !jsonNode.has(fieldName)) {
                if (!attribute.isOptional()) {
                    validationErrors.add("Missing required field: " + fieldName);
                }
                continue;
            }

            if (jsonNode.isValueNode()) {
                validateField(jsonNode, attribute, validationType, validationErrors);
                continue;
            }
            JsonNode fieldNode = jsonNode.get(fieldName);
            validateField(fieldNode, attribute, validationType, validationErrors);
        }

        return validationErrors;
    }

    private void validateField(JsonNode fieldNode,
                               SchemaAttribute attribute,
                               SchemaValidationType validationType,
                               List<String> validationErrors) {
        final var fieldName = attribute.getName();
        if (!isMatchingType(fieldNode, attribute)) {
            validationErrors.add("Type mismatch for field: " + fieldName +
                    ". Expected: " + attribute.getType() +
                    ", Found: " + fieldNode.getNodeType());
            return;
        }

        // Recursively validate nested objects
        if (attribute instanceof ObjectAttribute objectAttribute) {
            validationErrors.addAll(validate(fieldNode, validationType, objectAttribute.getNestedAttributes()));
            return;
        }
        if (attribute instanceof ArrayAttribute arrayAttribute) {
            for (JsonNode arrayElement : fieldNode) {
                validationErrors.addAll(validate(arrayElement, validationType, Set.of(arrayAttribute.getElementAttribute())));
            }
            return;
        }
        if (attribute instanceof MapAttribute mapAttribute) {
            validateMapAttribute(fieldNode, mapAttribute, validationType, validationErrors);
        }
    }

    private void validateMapAttribute(JsonNode fieldNode,
                                      MapAttribute mapAttribute,
                                      SchemaValidationType schemaValidationType,
                                      List<String> validationErrors) {
        fieldNode.fields().forEachRemaining(entry -> {
            // Validate key
            JsonNode keyNode = entry.getKey() != null
                    ? ResourceHelper.getObjectMapper().convertValue(entry.getKey(), JsonNode.class)
                    : null;
            if (!Objects.isNull(keyNode)) {
                // validate Key
                validateField(keyNode, mapAttribute.getKeyAttribute(), schemaValidationType, validationErrors);
                // Validate value
                validateField(entry.getValue(), mapAttribute.getValueAttribute(), schemaValidationType, validationErrors);
            } else {
                validationErrors.add("Key Not present for map attribute field:" +
                        mapAttribute.getName());
            }
        });

    }

    private boolean isMatchingType(JsonNode fieldNode, SchemaAttribute attribute) {
        return attribute.accept(new SchemaAttributeAcceptor<>() {
            @Override
            public Boolean accept(BooleanAttribute attribute) {
                return fieldNode.isBoolean();
            }

            @Override
            public Boolean accept(ByteAttribute attribute) {
                return fieldNode.isArray();
            }

            @Override
            public Boolean accept(DoubleAttribute attribute) {
                return fieldNode.isDouble() || fieldNode.isFloat() || fieldNode.isInt();
            }

            @Override
            public Boolean accept(EnumAttribute attribute) {
                return fieldNode.isTextual() && attribute.getValues().contains(fieldNode.asText());
            }

            @Override
            public Boolean accept(FloatAttribute attribute) {
                return fieldNode.isFloat();
            }

            @Override
            public Boolean accept(IntegerAttribute attribute) {
                return fieldNode.isInt();
            }

            @Override
            public Boolean accept(LongAttribute attribute) {
                return fieldNode.isLong() || fieldNode.isInt();
            }

            @Override
            public Boolean accept(StringAttribute attribute) {
                return fieldNode.isTextual();
            }

            @Override
            public Boolean accept(ArrayAttribute attribute) {
                return fieldNode.isArray();
            }

            @Override
            public Boolean accept(MapAttribute attribute) {
                return fieldNode.isObject();
            }

            @Override
            public Boolean accept(ObjectAttribute attribute) {
                return fieldNode.isObject();
            }
        });
    }
}
