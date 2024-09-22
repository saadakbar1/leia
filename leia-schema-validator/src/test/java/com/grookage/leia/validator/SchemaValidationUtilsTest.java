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
import com.grookage.leia.models.attributes.StringAttribute;
import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaValidationType;
import com.grookage.leia.validator.exception.ValidationErrorCode;
import com.grookage.leia.validator.utils.SchemaValidationUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

class SchemaValidationUtilsTest {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    static class ValidTestClass {
        private Set<String> testAttribute;
        private TestEnum testAttribute2;
        private String testAttribute3;
    }

    enum TestEnum {
        TEST_ENUM
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    static class InvalidTestClass {
        private Set<String> testAttribute;
    }

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
    void testNestedAttribute() {
        final var stringAttribute = new StringAttribute("testAttribute", true, null);
        final var arrayAttribute = new ArrayAttribute("testAttribute", true, null, stringAttribute);
        Assertions.assertTrue(SchemaValidationUtils.valid(SchemaValidationType.MATCHING, Set.of(arrayAttribute),
                                                          ValidTestClass.class));
    }
}
