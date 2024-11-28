package com.grookage.leia.client.utils;

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
import com.grookage.leia.models.qualifiers.EncryptedQualifier;
import com.grookage.leia.models.qualifiers.PIIQualifier;
import com.grookage.leia.models.qualifiers.QualifierInfo;
import com.grookage.leia.models.qualifiers.QualifierType;
import com.grookage.leia.models.qualifiers.ShortLivedQualifier;
import com.grookage.leia.models.qualifiers.StandardQualifier;
import com.grookage.leia.models.qualifiers.annotations.Qualifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

class SchemaUtilTest {

    @Test
    void testSchemaAttributes_WithPrimitiveClass() {
        final var schemaAttributeSet = SchemaUtil.buildSchemaAttributes(TestWithPrimitive.class);
        Assertions.assertNotNull(schemaAttributeSet);
        Assertions.assertEquals(2, schemaAttributeSet.size());
        final var nameAttribute = new StringAttribute("name", false, new StandardQualifier());
        equals(nameAttribute, filter(schemaAttributeSet, "name").orElse(null));
        final var idAttribute = new IntegerAttribute("id", true, new StandardQualifier());
        equals(idAttribute, filter(schemaAttributeSet, "id").orElse(null));
    }

    @Test
    void testSchemaAttributes_WithRecordClass() {
        final var schemaAttributeSet = SchemaUtil.buildSchemaAttributes(TestRecord.class);
        Assertions.assertNotNull(schemaAttributeSet);
        Assertions.assertEquals(2, schemaAttributeSet.size());
        final var nameAttribute = new StringAttribute("name", false, new PIIQualifier());
        equals(nameAttribute, filter(schemaAttributeSet, "name").orElse(null));
        final var idAttribute = new IntegerAttribute("id", true, new StandardQualifier());
        equals(idAttribute, filter(schemaAttributeSet, "id").orElse(null));
    }

    @Test
    void testSchemaAttributes_WithNestedObject() {
        final var schemaAttributes = SchemaUtil.buildSchemaAttributes(TestWithNested.class);
        Assertions.assertFalse(schemaAttributes.isEmpty());
        Assertions.assertEquals(6, schemaAttributes.size());
        final var nameAttribute = new StringAttribute("name", true, new StandardQualifier());
        equals(nameAttribute, filter(schemaAttributes, "name").orElse(null));

        final var idAttribute = new IntegerAttribute("id", true, new StandardQualifier());
        equals(idAttribute, filter(schemaAttributes, "id").orElse(null));

        final var testPIIDataAttributes = new HashSet<SchemaAttribute>();
        final var piiNameAttribute = new StringAttribute("name", true, new StandardQualifier());
        final var accountNumberAttribute = new StringAttribute("accountNumber", true, new EncryptedQualifier());
        testPIIDataAttributes.add(piiNameAttribute);
        testPIIDataAttributes.add(accountNumberAttribute);
        final var piiDataAttribute = new ObjectAttribute("piiData", true, new PIIQualifier(), testPIIDataAttributes);
        equals(piiDataAttribute, filter(schemaAttributes, "piiData").orElse(null));

        final var testRecordAttributes = new HashSet<SchemaAttribute>();
        final var recordNameAttribute = new StringAttribute("name", false, new PIIQualifier());
        final var recordIdAttribute = new IntegerAttribute("id", true, new StandardQualifier());
        testRecordAttributes.add(recordNameAttribute);
        testRecordAttributes.add(recordIdAttribute);
        final var testRecordAttribute = new ObjectAttribute("testRecord", true, new EncryptedQualifier(),
                testRecordAttributes);
        equals(testRecordAttribute, filter(schemaAttributes, "testRecord").orElse(null));

        final var enumClassAttribute = new EnumAttribute("enumClass", true, new StandardQualifier(), Set.of(EnumClass.ONE.name(),
                EnumClass.TWO.name()));
        equals(enumClassAttribute, filter(schemaAttributes, "enumClass").orElse(null));

        final var phoneNoAttribute = new StringAttribute("phoneNumber", true, new PIIQualifier());
        equals(phoneNoAttribute, filter(schemaAttributes, "phoneNumber").orElse(null));
    }

    @Test
    void testSchemaAttributes_WithParameterizedType() {
        final var schemaAttributes = SchemaUtil.buildSchemaAttributes(TestWithParameterized.class);
        Assertions.assertNotNull(schemaAttributes);
        Assertions.assertEquals(3, schemaAttributes.size());

        final var valuesAttributes = new ArrayAttribute("values", true, new StandardQualifier(),
                new StringAttribute("element", true, new StandardQualifier()));
        equals(valuesAttributes, filter(schemaAttributes, "values").orElse(null));

        final var testPIIDataAttributes = new HashSet<SchemaAttribute>();
        final var piiNameAttribute = new StringAttribute("name", true, new StandardQualifier());
        final var accountNumberAttribute = new StringAttribute("accountNumber", true, new EncryptedQualifier());
        testPIIDataAttributes.add(piiNameAttribute);
        testPIIDataAttributes.add(accountNumberAttribute);
        final var piiDataListAttribute = new ArrayAttribute("piiDataList", true, new PIIQualifier(),
                new ObjectAttribute("element", true, new PIIQualifier(), testPIIDataAttributes));
        equals(piiDataListAttribute, filter(schemaAttributes, "piiDataList").orElse(null));

        final var mapAttribute = new MapAttribute("map", true, new EncryptedQualifier(),
                new EnumAttribute("key", true, new StandardQualifier(), Set.of(EnumClass.ONE.name(), EnumClass.TWO.name())),
                new StringAttribute("value", true, new StandardQualifier()));
        equals(mapAttribute, filter(schemaAttributes, "map").orElse(null));
    }

    enum EnumClass {
        ONE,
        TWO
    }

    @Qualifier(type = QualifierType.PII)
    static class TestPIIData {
        String name;
        @Qualifier(type = QualifierType.ENCRYPTED)
        String accountNumber;
    }

    static record TestRecord(@Qualifier(type = QualifierType.PII) @NotEmpty String name,
                             int id) {

    }

    static class TestWithPrimitive {
        @NotEmpty
        String name;
        int id;
    }

    static class TestWithNested {
        String name;
        int id;
        @Qualifier(type = QualifierType.PII)
        TestPIIData piiData;
        @Qualifier(type = QualifierType.ENCRYPTED)
        TestRecord testRecord;
        EnumClass enumClass;
        @Qualifier(type = QualifierType.PII)
        String phoneNumber;
    }

    static class TestWithParameterized {
        String[] values;
        @Qualifier(type = QualifierType.PII)
        List<TestPIIData> piiDataList;
        @Qualifier(type = QualifierType.ENCRYPTED)
        Map<EnumClass, String> map;
    }

    private Optional<SchemaAttribute> filter(Set<SchemaAttribute> schemaAttributes,
                                             String name) {
        return schemaAttributes.stream()
                .filter(schemaAttribute -> schemaAttribute.getName().equals(name))
                .findFirst();
    }

    private void equals(SchemaAttribute expected,
                        SchemaAttribute original) {
        if (Objects.isNull(expected) && Objects.isNull(original)) {
            return;
        }
        Assertions.assertEquals(expected.getType(), original.getType(), "Type mismatch");
        Assertions.assertEquals(expected.getName(), original.getName(), "Name mismatch");
        Assertions.assertEquals(expected.isOptional(), original.isOptional(), "Optionality mismatch");

        // Compare QualifierInfo
        equals(expected.getQualifierInfo(), original.getQualifierInfo());

        // Accept the expected attribute type and perform specific validations
        expected.accept(new SchemaAttributeAcceptor<Void>() {
            @Override
            public Void accept(BooleanAttribute attribute) {
                Assertions.assertInstanceOf(BooleanAttribute.class, original, "Original is not BooleanAttribute");
                return null;
            }

            @Override
            public Void accept(ByteAttribute attribute) {
                Assertions.assertInstanceOf(ByteAttribute.class, original, "Original is not ByteAttribute");
                return null;
            }

            @Override
            public Void accept(DoubleAttribute attribute) {
                Assertions.assertInstanceOf(DoubleAttribute.class, original, "Original is not DoubleAttribute");
                return null;
            }

            @Override
            public Void accept(EnumAttribute attribute) {
                Assertions.assertInstanceOf(EnumAttribute.class, original, "Original is not EnumAttribute");
                EnumAttribute originalEnum = (EnumAttribute) original;
                Assertions.assertEquals(attribute.getValues(), originalEnum.getValues(), "Enum values mismatch");
                return null;
            }

            @Override
            public Void accept(FloatAttribute attribute) {
                Assertions.assertInstanceOf(FloatAttribute.class, original, "Original is not FloatAttribute");
                return null;
            }

            @Override
            public Void accept(IntegerAttribute attribute) {
                Assertions.assertInstanceOf(IntegerAttribute.class, original, "Original is not IntegerAttribute");
                return null;
            }

            @Override
            public Void accept(LongAttribute attribute) {
                Assertions.assertInstanceOf(LongAttribute.class, original, "Original is not LongAttribute");
                return null;
            }

            @Override
            public Void accept(StringAttribute attribute) {
                Assertions.assertInstanceOf(StringAttribute.class, original, "Original is not StringAttribute");
                return null;
            }

            @Override
            public Void accept(ArrayAttribute attribute) {
                Assertions.assertInstanceOf(ArrayAttribute.class, original, "Original is not ArrayAttribute");
                ArrayAttribute originalArray = (ArrayAttribute) original;
                SchemaUtilTest.this.equals(attribute.getElementAttribute(), originalArray.getElementAttribute()); // Recursive comparison for elementAttribute
                return null;
            }

            @Override
            public Void accept(MapAttribute attribute) {
                Assertions.assertInstanceOf(MapAttribute.class, original, "Original is not MapAttribute");
                MapAttribute originalMap = (MapAttribute) original;
                SchemaUtilTest.this.equals(attribute.getKeyAttribute(), originalMap.getKeyAttribute()); // Recursive comparison for key
                SchemaUtilTest.this.equals(attribute.getValueAttribute(), originalMap.getValueAttribute()); // Recursive comparison for value
                return null;
            }

            @Override
            public Void accept(ObjectAttribute attribute) {
                Assertions.assertInstanceOf(ObjectAttribute.class, original, "Original is not ObjectAttribute");
                ObjectAttribute originalObject = (ObjectAttribute) original;

                // Recursive comparison of nested attributes
                Assertions.assertEquals(attribute.getNestedAttributes().size(), originalObject.getNestedAttributes().size(),
                        "Nested attributes size mismatch");
                Iterator<SchemaAttribute> expectedIterator = attribute.getNestedAttributes().iterator();
                Iterator<SchemaAttribute> originalIterator = originalObject.getNestedAttributes().iterator();

                while (expectedIterator.hasNext() && originalIterator.hasNext()) {
                    SchemaUtilTest.this.equals(expectedIterator.next(), originalIterator.next());
                }
                return null;
            }
        });
    }

    private void equals(QualifierInfo expected,
                        QualifierInfo original) {
        if (Objects.isNull(expected) && Objects.isNull(original)) {
            return;
        }
        Assertions.assertEquals(expected.getType(), original.getType());
        if (expected instanceof ShortLivedQualifier expectedQualifier
                && original instanceof ShortLivedQualifier originalQualifier) {
            Assertions.assertEquals(expectedQualifier.getTtlSeconds(), originalQualifier.getTtlSeconds());
        }
    }
}