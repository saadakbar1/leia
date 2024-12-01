package com.grookage.leia.common.utils;

import com.grookage.leia.common.TestUtils;
import com.grookage.leia.common.stubs.NestedStub;
import com.grookage.leia.common.stubs.RecordStub;
import com.grookage.leia.common.stubs.TestEnum;
import com.grookage.leia.common.stubs.TestObjectStub;
import com.grookage.leia.common.stubs.TestParameterizedStub;
import com.grookage.leia.common.stubs.TestRawCollectionStub;
import com.grookage.leia.models.annotations.attribute.Optional;
import com.grookage.leia.models.attributes.ArrayAttribute;
import com.grookage.leia.models.attributes.EnumAttribute;
import com.grookage.leia.models.attributes.IntegerAttribute;
import com.grookage.leia.models.attributes.MapAttribute;
import com.grookage.leia.models.attributes.ObjectAttribute;
import com.grookage.leia.models.attributes.SchemaAttribute;
import com.grookage.leia.models.attributes.StringAttribute;
import com.grookage.leia.models.qualifiers.EncryptedQualifier;
import com.grookage.leia.models.qualifiers.PIIQualifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class SchemaAttributeUtilsTest {

    @Test
    void testSchemaAttributes_WithPrimitiveClass() {
        final var schemaAttributeSet = SchemaAttributeUtils.getSchemaAttributes(PrimitiveTestClass.class);
        Assertions.assertNotNull(schemaAttributeSet);
        Assertions.assertEquals(2, schemaAttributeSet.size());
        final var nameAttribute = new StringAttribute("name", true, new HashSet<>());
        TestUtils.assertEquals(nameAttribute, TestUtils.filter(schemaAttributeSet, "name").orElse(null));
        final var idAttribute = new IntegerAttribute("id", false, new HashSet<>());
        TestUtils.assertEquals(idAttribute, TestUtils.filter(schemaAttributeSet, "id").orElse(null));
    }

    @Test
    void testSchemaAttributes_WithRecordClass() {
        final var schemaAttributeSet = SchemaAttributeUtils.getSchemaAttributes(RecordStub.class);
        Assertions.assertNotNull(schemaAttributeSet);
        Assertions.assertEquals(2, schemaAttributeSet.size());
        final var nameAttribute = new StringAttribute("name", false, Set.of(new PIIQualifier()));
        TestUtils.assertEquals(nameAttribute, TestUtils.filter(schemaAttributeSet, "name").orElse(null));
        final var idAttribute = new IntegerAttribute("id", true, new HashSet<>());
        TestUtils.assertEquals(idAttribute, TestUtils.filter(schemaAttributeSet, "id").orElse(null));
    }

    @Test
    void testSchemaAttributes_WithNestedObject() {
        final var schemaAttributes = SchemaAttributeUtils.getSchemaAttributes(NestedStub.class);
        Assertions.assertFalse(schemaAttributes.isEmpty());
        Assertions.assertEquals(6, schemaAttributes.size());
        final var nameAttribute = new StringAttribute("name", false, new HashSet<>());
        TestUtils.assertEquals(nameAttribute, TestUtils.filter(schemaAttributes, "name").orElse(null));

        final var idAttribute = new IntegerAttribute("id", false, new HashSet<>());
        TestUtils.assertEquals(idAttribute, TestUtils.filter(schemaAttributes, "id").orElse(null));

        final var testPIIDataAttributes = new HashSet<SchemaAttribute>();
        final var piiNameAttribute = new StringAttribute("name", false, new HashSet<>());
        final var accountNumberAttribute = new StringAttribute("accountNumber", false, Set.of(new EncryptedQualifier()));
        testPIIDataAttributes.add(piiNameAttribute);
        testPIIDataAttributes.add(accountNumberAttribute);
        final var piiDataAttribute = new ObjectAttribute("piiData", false, Set.of(new PIIQualifier(), new EncryptedQualifier()), testPIIDataAttributes);
        TestUtils.assertEquals(piiDataAttribute, TestUtils.filter(schemaAttributes, "piiData").orElse(null));

        final var testRecordAttributes = new HashSet<SchemaAttribute>();
        final var recordNameAttribute = new StringAttribute("name", false, Set.of(new PIIQualifier()));
        final var recordIdAttribute = new IntegerAttribute("id", true, new HashSet<>());
        testRecordAttributes.add(recordNameAttribute);
        testRecordAttributes.add(recordIdAttribute);
        final var testRecordAttribute = new ObjectAttribute("recordStub", false, Set.of(new EncryptedQualifier()),
                testRecordAttributes);
        TestUtils.assertEquals(testRecordAttribute, TestUtils.filter(schemaAttributes, "recordStub").orElse(null));

        final var enumClassAttribute = new EnumAttribute("enumClass", false, new HashSet<>(), Set.of(TestEnum.ONE.name(),
                TestEnum.TWO.name()));
        TestUtils.assertEquals(enumClassAttribute, TestUtils.filter(schemaAttributes, "enumClass").orElse(null));

        final var phoneNoAttribute = new StringAttribute("phoneNumber", false, Set.of(new PIIQualifier()));
        TestUtils.assertEquals(phoneNoAttribute, TestUtils.filter(schemaAttributes, "phoneNumber").orElse(null));
    }

    @Test
    void testSchemaAttributes_WithParameterizedType() {
        final var schemaAttributes = SchemaAttributeUtils.getSchemaAttributes(TestParameterizedStub.class);
        Assertions.assertNotNull(schemaAttributes);
        Assertions.assertEquals(3, schemaAttributes.size());

        final var valuesAttributes = new ArrayAttribute("values", false, new HashSet<>(),
                new StringAttribute("element", false, new HashSet<>()));
        TestUtils.assertEquals(valuesAttributes, TestUtils.filter(schemaAttributes, "values").orElse(null));

        final var testPIIDataAttributes = new HashSet<SchemaAttribute>();
        final var piiNameAttribute = new StringAttribute("name", false, new HashSet<>());
        final var accountNumberAttribute = new StringAttribute("accountNumber", false, Set.of(new EncryptedQualifier()));
        testPIIDataAttributes.add(piiNameAttribute);
        testPIIDataAttributes.add(accountNumberAttribute);
        final var piiDataListAttribute = new ArrayAttribute("piiDataList", false, Set.of(new PIIQualifier()),
                new ObjectAttribute("element", false, Set.of(new PIIQualifier()), testPIIDataAttributes));
        TestUtils.assertEquals(piiDataListAttribute, TestUtils.filter(schemaAttributes, "piiDataList").orElse(null));

        final var mapAttribute = new MapAttribute("map", false, Set.of(new EncryptedQualifier()),
                new EnumAttribute("key", false, new HashSet<>(), Set.of(TestEnum.ONE.name(), TestEnum.TWO.name())),
                new StringAttribute("value", false, new HashSet<>()));
        TestUtils.assertEquals(mapAttribute, TestUtils.filter(schemaAttributes, "map").orElse(null));
    }

    @Test
    void testSchemaAttributes_WithRawCollections() {
        final var schemaAttributes = SchemaAttributeUtils.getSchemaAttributes(TestRawCollectionStub.class);
        Assertions.assertNotNull(schemaAttributes);
        Assertions.assertEquals(6, schemaAttributes.size());

        final var rawListAttribute = new ArrayAttribute("rawList", false, Set.of(), null);
        TestUtils.assertEquals(rawListAttribute, TestUtils.filter(schemaAttributes, "rawList").orElse(null));

        final var rawLinkedListAttribute = new ArrayAttribute("rawLinkedList", false, Set.of(), null);
        TestUtils.assertEquals(rawLinkedListAttribute, TestUtils.filter(schemaAttributes, "rawLinkedList").orElse(null));

        final var rawSetAttribute = new ArrayAttribute("rawSet", false, Set.of(), null);
        TestUtils.assertEquals(rawSetAttribute, TestUtils.filter(schemaAttributes, "rawSet").orElse(null));

        final var rawHashSetAttribute = new ArrayAttribute("rawHashSet", false, Set.of(), null);
        TestUtils.assertEquals(rawHashSetAttribute, TestUtils.filter(schemaAttributes, "rawHashSet").orElse(null));

        final var rawMapAttribute = new MapAttribute("rawMap", false, Set.of(), null, null);
        TestUtils.assertEquals(rawMapAttribute, TestUtils.filter(schemaAttributes, "rawMap").orElse(null));

        final var rawSortedMapAttribute = new MapAttribute("rawSortedMap", false, Set.of(), null, null);
        TestUtils.assertEquals(rawSortedMapAttribute, TestUtils.filter(schemaAttributes, "rawSortedMap").orElse(null));
    }

    @Test
    void testSchemaAttributes_WithObjects() {
        final var schemaAttributes = SchemaAttributeUtils.getSchemaAttributes(TestObjectStub.class);
        Assertions.assertNotNull(schemaAttributes);
        Assertions.assertEquals(5, schemaAttributes.size());

        final var objectAttribute = new ObjectAttribute("object", false, Set.of(), null);
        TestUtils.assertEquals(objectAttribute, TestUtils.filter(schemaAttributes, "object").orElse(null));

        final var objectsAttribute = new ArrayAttribute("objects", false, Set.of(),
                new ObjectAttribute("element", false, Set.of(), null));
        TestUtils.assertEquals(objectsAttribute, TestUtils.filter(schemaAttributes, "objects").orElse(null));

        final var objectListAttribute = new ArrayAttribute("objectList", false, Set.of(),
                new ObjectAttribute("element", false, Set.of(), null));
        TestUtils.assertEquals(objectListAttribute, TestUtils.filter(schemaAttributes, "objectList").orElse(null));

        final var objectSetAttribute = new ArrayAttribute("objectSet", false, Set.of(),
                new ObjectAttribute("element", false, Set.of(), null));
        TestUtils.assertEquals(objectSetAttribute, TestUtils.filter(schemaAttributes, "objectSet").orElse(null));

        final var objectMapAttribute = new MapAttribute("objectMap", false, Set.of(),
                new StringAttribute("key", false, Set.of()),
                new ObjectAttribute("value", false, Set.of(), null));
        TestUtils.assertEquals(objectMapAttribute, TestUtils.filter(schemaAttributes, "objectMap").orElse(null));

    }

    static class PrimitiveTestClass {
        @Optional
        String name;
        int id;
    }

}