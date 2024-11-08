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

package com.grookage.leia.validator;

import com.grookage.leia.models.ResourceHelper;
import com.grookage.leia.models.attributes.*;
import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaValidationType;
import com.grookage.leia.validator.exception.ValidationErrorCode;
import com.grookage.leia.validator.utils.SchemaValidationUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class SchemaValidationUtilsTest {

    @Test
    @SneakyThrows
    void testSchemaValidator() {
        final var schemaDetails = ResourceHelper
                .getResource("validSchema.json", SchemaDetails.class);
        Assertions.assertNotNull(schemaDetails);
        Assertions.assertTrue(SchemaValidationUtils.valid(schemaDetails, ValidTestClass.class));
        schemaDetails.setValidationType(SchemaValidationType.STRICT);
        Assertions.assertFalse(SchemaValidationUtils.valid(schemaDetails, ValidTestClass.class));
    }

    @Test
    @SneakyThrows
    void testInvalidMatchingSchema() {
        final var schemaDetails = ResourceHelper
                .getResource("validSchema.json", SchemaDetails.class);
        schemaDetails.setValidationType(SchemaValidationType.MATCHING);
        Assertions.assertNotNull(schemaDetails);
        Assertions.assertFalse(SchemaValidationUtils.valid(schemaDetails, InvalidTestClass.class));
    }

    @Test
    void testAllFields() {
        final var booleanAttribute = new BooleanAttribute("testAttribute", true, null);
        Assertions.assertTrue(SchemaValidationUtils.valid(Boolean.class, booleanAttribute));

        final var byteAttribute = new ByteAttribute("testAttribute", true, null);
        Assertions.assertTrue(SchemaValidationUtils.valid(Byte.class, byteAttribute));

        final var doubleAttribute = new DoubleAttribute("testAttribute", true, null);
        Assertions.assertTrue(SchemaValidationUtils.valid(Double.class, doubleAttribute));

        final var enumAttribute = new EnumAttribute("testAttribute", true, null, Set.of());
        Assertions.assertTrue(SchemaValidationUtils.valid(ValidationErrorCode.class, enumAttribute));

        final var floatAttribute = new FloatAttribute("testAttribute", true, null);
        Assertions.assertTrue(SchemaValidationUtils.valid(Float.class, floatAttribute));

        final var integerAttribute = new IntegerAttribute("testAttribute", true, null);
        Assertions.assertTrue(SchemaValidationUtils.valid(Integer.class, integerAttribute));

        final var longAttribute = new LongAttribute("testAttribute", true, null);
        Assertions.assertTrue(SchemaValidationUtils.valid(Long.class, longAttribute));

        final var stringAttribute = new StringAttribute("testAttribute", true, null);
        Assertions.assertTrue(SchemaValidationUtils.valid(String.class, stringAttribute));

        final var arrayAttribute = new ArrayAttribute("testAttribute", true, null, stringAttribute);
        Assertions.assertTrue(SchemaValidationUtils.valid(Set.class, arrayAttribute));

        final var mapAttribute = new MapAttribute("testAttribute", true, null, stringAttribute, stringAttribute);
        Assertions.assertTrue(SchemaValidationUtils.valid(Map.class, mapAttribute));

        final var objectAttribute = new ObjectAttribute("testAttribute", true, null, Set.of(stringAttribute));
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaDetails.class, objectAttribute));

        Assertions.assertFalse(SchemaValidationUtils.valid(Long.class, integerAttribute));
        Assertions.assertTrue(SchemaValidationUtils.valid(Long.class, objectAttribute));


    }

    @Test
    void testParametrizedArray() {
        final var stringAttribute = new StringAttribute("stringAttribute", true, null);
        final var arrayAttribute = new ArrayAttribute("arrayAttribute", true, null, stringAttribute);
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(arrayAttribute),
                SetTestClass.class));
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(arrayAttribute),
                ListTestClass.class));
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(arrayAttribute),
                ArrayTestClass.class));
        Assertions.assertFalse(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(arrayAttribute),
                RawSetTestClass.class));
    }

    @Test
    void testRawArray() {
        final var arrayAttribute = new ArrayAttribute("arrayAttribute", true, null, null);
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING,
                Set.of(arrayAttribute), RawSetTestClass.class));
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING,
                Set.of(arrayAttribute), SetTestClass.class));
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING,
                Set.of(arrayAttribute), ListTestClass.class));
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING,
                Set.of(arrayAttribute), ArrayTestClass.class));
    }

    @Test
    void testParametrizedMap() {
        final var keyAttribute = new StringAttribute("keyAttribute", true, null);
        final var valueAttribute = new StringAttribute("valueAttribute", true, null);
        final var mapAttribute = new MapAttribute("mapAttribute", true, null, keyAttribute, valueAttribute);
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(mapAttribute),
                MapTestClass.class));
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(mapAttribute),
                ConcurrentMapTestClass.class));
        Assertions.assertFalse(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(mapAttribute),
                RawMapTestClass.class));
    }

    @Test
    void testRawMap() {
        final var mapAttribute = new MapAttribute("mapAttribute", true, null, null, null);
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(mapAttribute),
                RawMapTestClass.class));
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(mapAttribute),
                MapTestClass.class));
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(mapAttribute),
                ConcurrentMapTestClass.class));
    }

    @Test
    @SneakyThrows
    void testNestedObject() {
        final var schemaDetails = ResourceHelper
                .getResource("validNestedSchema.json", SchemaDetails.class);
        schemaDetails.setValidationType(SchemaValidationType.MATCHING);
        Assertions.assertTrue(SchemaValidationUtils.valid(schemaDetails, ValidObjectTestClass.class));
        Assertions.assertFalse(SchemaValidationUtils.valid(schemaDetails, InvalidObjectTestClass.class));
    }

    @Test
    void testGenericArrayType() {
        final var stringAttribute = new StringAttribute("stringAttribute", true, null);
        final var listAttribute = new ArrayAttribute("listAttribute", true, null, stringAttribute);
        final var arrayAttribute = new ArrayAttribute("arrayAttribute", true, null, listAttribute);
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(arrayAttribute),
                GenericArrayTestClass.class));
    }

    enum TestEnum {
        TEST_ENUM
    }

    static class ValidTestClass {
        Set<String> testAttribute;
        TestEnum testAttribute2;
        String testAttribute3;
    }

    static class InvalidTestClass {
        Set<String> testAttribute;
    }

    static class SetTestClass {
        Set<String> arrayAttribute;
    }

    static class ListTestClass {
        List<String> arrayAttribute;
    }

    static class ArrayTestClass {
        String[] arrayAttribute;
    }

    static class RawSetTestClass {
        Set arrayAttribute;
    }

    static class MapTestClass {
        Map<String, String> mapAttribute;
    }

    static class ConcurrentMapTestClass {
        ConcurrentHashMap<String, String> mapAttribute;
    }

    static class RawMapTestClass {
        Map mapAttribute;
    }

    static class ValidObjectTestClass {
        String stringAttribute;
        ValidNestedObjectTestClass nestedObjectAttribute;
    }

    static class ValidNestedObjectTestClass {
        Integer integerAttribute;
    }

    static class InvalidObjectTestClass {
        String stringAttribute;
        InvalidNestedObjectTestClass nestedObjectAttribute;
    }

    static class InvalidNestedObjectTestClass {
        String integerAttribute;
    }

    static class GenericArrayTestClass {
        List<String>[] arrayAttribute;
    }
}
