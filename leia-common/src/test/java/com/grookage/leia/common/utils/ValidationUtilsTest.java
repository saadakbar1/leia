package com.grookage.leia.common.utils;

import com.grookage.leia.models.ResourceHelper;
import com.grookage.leia.models.attributes.ArrayAttribute;
import com.grookage.leia.models.attributes.BooleanAttribute;
import com.grookage.leia.models.attributes.IntegerAttribute;
import com.grookage.leia.models.attributes.MapAttribute;
import com.grookage.leia.models.attributes.ObjectAttribute;
import com.grookage.leia.models.attributes.SchemaAttribute;
import com.grookage.leia.models.attributes.StringAttribute;
import com.grookage.leia.models.schema.SchemaValidationType;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationUtilsTest {
    @Test
    void testValidJsonAgainstSchema() throws Exception {
        final var jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "name": "John Doe",
                    "age": 30,
                    "isActive": true
                }
                """);

        final var schemaAttributes = Set.of(
                new StringAttribute("name", false, null),
                new IntegerAttribute("age", false, null),
                new BooleanAttribute("isActive", false, null)
        );

        final var errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testUnexpectedFieldInJson() throws Exception {
        final var jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "name": "John Doe",
                    "age": 30,
                    "isActive": true,
                    "unexpectedField": "extra"
                }
                """);

        final var schemaAttributes = Set.of(
                new StringAttribute("name", false, null),
                new IntegerAttribute("age", false, null),
                new BooleanAttribute("isActive", false, null)
        );

        final var errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals("Unexpected field: unexpectedField", errors.get(0));
    }

    @Test
    void testMissingRequiredField() throws Exception {
        final var jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "name": "John Doe"
                }
                """);

        final var schemaAttributes = Set.of(
                new StringAttribute("name", false, null),
                new IntegerAttribute("age", false, null)
        );

        final var errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals("Missing required field: age", errors.get(0));
    }

    @Test
    void testTypeMismatch() throws Exception {
        final var jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "name": "John Doe",
                    "age": "thirty"
                }
                """);

        final var schemaAttributes = Set.of(
                new StringAttribute("name", false, null),
                new IntegerAttribute("age", false, null)
        );

        final var errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals("Type mismatch for field: age. Expected: INTEGER, Found: STRING", errors.get(0));
    }

    @Test
    void testNestedObjectValidation() throws Exception {
        final var jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "user": {
                        "id": 1,
                        "username": "johndoe"
                    }
                }
                """);

        final Set<SchemaAttribute> nestedAttributes = Set.of(
                new IntegerAttribute("id", false, null),
                new StringAttribute("username", false, null)
        );

        final Set<SchemaAttribute> schemaAttributes = Set.of(
                new ObjectAttribute("user", false, null, nestedAttributes)
        );

        final var errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testArrayValidation() throws Exception {
        final var jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "numbers": [1, 2, 3, 4]
                }
                """);

        final Set<SchemaAttribute> schemaAttributes = Set.of(
                new ArrayAttribute("numbers", false, null, new IntegerAttribute("element", false, null))
        );

        final var errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testMapValidation() throws Exception {
        final var jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "attributes": {
                        "key1": "value1",
                        "key2": "value2"
                    }
                }
                """);

        final Set<SchemaAttribute> schemaAttributes = Set.of(
                new MapAttribute(
                        "attributes",
                        false,
                        null,
                        new StringAttribute("key", false, null),
                        new StringAttribute("value", false, null)
                )
        );

        final var errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testInvalidMapValueType() throws Exception {
        final var jsonNode = ResourceHelper.getObjectMapper().readTree("""
                {
                    "attributes": {
                        "key1": 100
                    }
                }
                """);

        final Set<SchemaAttribute> schemaAttributes = Set.of(
                new MapAttribute(
                        "attributes",
                        false,
                        null,
                        new StringAttribute("key", false, null),
                        new StringAttribute("value", false, null)
                )
        );

        final var errors = ValidationUtils.validate(jsonNode, SchemaValidationType.STRICT, schemaAttributes);

        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
    }

    @Test
    void testValidateNested() {
        final var testRecord = TestRecord.builder()
                .id(100)
                .name("name")
                .nestedObjectsList(List.of(NestedObject.builder()
                                .key("key")
                                .version(5l)
                                .enumclass(Enumclass.ONE)
                        .build(), NestedObject.builder()
                                .key("key")
                                .version(6l)
                                .enumclass(Enumclass.TWO)
                        .build()))
                .nestedObjectMap(Map.of(Enumclass.ONE, NestedObject.builder()
                                .key("key")
                                .version(7l)
                                .enumclass(Enumclass.ONE)
                        .build()))
                .build();
        // TODO::Abhishek Finish this
        final var jsonNode = ResourceHelper.getObjectMapper().valueToTree(testRecord);
    }

    static enum Enumclass {
        ONE,
        TWO
    }
    @Data
    @Builder
    static class NestedObject{
        String key;
        long version;
        Enumclass enumclass;
    }

    @Data
    @Builder
    static class TestRecord {
        String name;
        int id;
        List<NestedObject> nestedObjectsList;
        Map<Enumclass, NestedObject> nestedObjectMap;
    }
}