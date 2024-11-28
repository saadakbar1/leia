package com.grookage.leia.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.grookage.leia.models.ResourceHelper;
import com.grookage.leia.models.attributes.ArrayAttribute;
import com.grookage.leia.models.attributes.BooleanAttribute;
import com.grookage.leia.models.attributes.IntegerAttribute;
import com.grookage.leia.models.attributes.MapAttribute;
import com.grookage.leia.models.attributes.ObjectAttribute;
import com.grookage.leia.models.attributes.SchemaAttribute;
import com.grookage.leia.models.attributes.StringAttribute;
import com.grookage.leia.models.schema.SchemaValidationType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationUtilsTest {
    @Test
    void testValidJsonAgainstSchema() throws Exception {
        JsonNode jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "name": "John Doe",
                    "age": 30,
                    "isActive": true
                }
                """);

        Set<SchemaAttribute> schemaAttributes = Set.of(
                new StringAttribute("name", false, null),
                new IntegerAttribute("age", false, null),
                new BooleanAttribute("isActive", false, null)
        );

        List<String> errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testUnexpectedFieldInJson() throws Exception {
        JsonNode jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "name": "John Doe",
                    "age": 30,
                    "isActive": true,
                    "unexpectedField": "extra"
                }
                """);

        Set<SchemaAttribute> schemaAttributes = Set.of(
                new StringAttribute("name", false, null),
                new IntegerAttribute("age", false, null),
                new BooleanAttribute("isActive", false, null)
        );

        List<String> errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals("Unexpected field: unexpectedField", errors.get(0));
    }

    @Test
    void testMissingRequiredField() throws Exception {
        JsonNode jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "name": "John Doe"
                }
                """);

        Set<SchemaAttribute> schemaAttributes = Set.of(
                new StringAttribute("name", false, null),
                new IntegerAttribute("age", false, null)
        );

        List<String> errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals("Missing required field: age", errors.get(0));
    }

    @Test
    void testTypeMismatch() throws Exception {
        JsonNode jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "name": "John Doe",
                    "age": "thirty"
                }
                """);

        Set<SchemaAttribute> schemaAttributes = Set.of(
                new StringAttribute("name", false, null),
                new IntegerAttribute("age", false, null)
        );

        List<String> errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals("Type mismatch for field: age. Expected: INTEGER, Found: STRING", errors.get(0));
    }

    @Test
    void testNestedObjectValidation() throws Exception {
        JsonNode jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "user": {
                        "id": 1,
                        "username": "johndoe"
                    }
                }
                """);

        Set<SchemaAttribute> nestedAttributes = Set.of(
                new IntegerAttribute("id", false, null),
                new StringAttribute("username", false, null)
        );

        Set<SchemaAttribute> schemaAttributes = Set.of(
                new ObjectAttribute("user", false, null, nestedAttributes)
        );

        List<String> errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testArrayValidation() throws Exception {
        JsonNode jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "numbers": [1, 2, 3, 4]
                }
                """);

        Set<SchemaAttribute> schemaAttributes = Set.of(
                new ArrayAttribute("numbers", false, null, new IntegerAttribute("element", false, null))
        );

        List<String> errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testMapValidation() throws Exception {
        JsonNode jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "attributes": {
                        "key1": "value1",
                        "key2": "value2"
                    }
                }
                """);

        Set<SchemaAttribute> schemaAttributes = Set.of(
                new MapAttribute(
                        "attributes",
                        false,
                        null,
                        new StringAttribute("key", false, null),
                        new StringAttribute("value", false, null)
                )
        );

        List<String> errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testInvalidMapValueType() throws Exception {
        JsonNode jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "attributes": {
                        "key1": 100
                    }
                }
                """);

        Set<SchemaAttribute> schemaAttributes = Set.of(
                new MapAttribute(
                        "attributes",
                        false,
                        null,
                        new StringAttribute("key", false, null),
                        new StringAttribute("value", false, null)
                )
        );

        List<String> errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
    }
}