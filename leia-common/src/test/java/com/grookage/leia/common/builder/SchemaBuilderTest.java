package com.grookage.leia.common.builder;

import com.grookage.leia.common.utils.SchemaAttributeUtils;
import com.grookage.leia.models.annotations.SchemaDefinition;
import com.grookage.leia.models.annotations.attribute.Optional;
import com.grookage.leia.models.annotations.attribute.qualifiers.Encrypted;
import com.grookage.leia.models.annotations.attribute.qualifiers.PII;
import com.grookage.leia.models.schema.SchemaType;
import com.grookage.leia.models.schema.SchemaValidationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaBuilderTest {
    @Test
    void testSchemaRequest() {
        final var schemaCreateRequest = SchemaBuilder.buildSchemaRequest(TestRecord.class)
                .orElse(null);
        Assertions.assertNotNull(schemaCreateRequest);
        final var schemaAttributes = SchemaAttributeUtils.getSchemaAttributes(TestRecord.class);
        Assertions.assertEquals(TestRecord.NAME, schemaCreateRequest.getSchemaName());
        Assertions.assertEquals(TestRecord.NAMESPACE, schemaCreateRequest.getNamespace());
        Assertions.assertEquals(TestRecord.DESCRIPTION, schemaCreateRequest.getDescription());
        Assertions.assertEquals(SchemaType.JSON, schemaCreateRequest.getSchemaType());
        Assertions.assertEquals(SchemaValidationType.MATCHING, schemaCreateRequest.getValidationType());
        Assertions.assertEquals(schemaAttributes.size(), schemaCreateRequest.getAttributes().size());
    }

    @Test
    void testSchemaRequest_WithInvalidClass() {
        Assertions.assertTrue(SchemaBuilder.buildSchemaRequest(null).isEmpty());
        Assertions.assertTrue(SchemaBuilder.buildSchemaRequest(TestObject.class).isEmpty());
    }

    @SchemaDefinition(
            name = TestRecord.NAME,
            namespace = TestRecord.NAMESPACE,
            version = TestRecord.VERSION,
            description = TestRecord.DESCRIPTION,
            type = SchemaType.JSON,
            validation = SchemaValidationType.MATCHING
    )
    static class TestRecord {
        static final String NAME = "TEST_RECORD";
        static final String NAMESPACE = "test";
        static final String VERSION = "v1";
        static final String DESCRIPTION = "Test Record";

        int id;
        String name;
        @PII
        @Encrypted
        String accountNumber;
        long ttl;
        @Optional
        String accountId;
    }

    static class TestObject {
        String name;
    }
}