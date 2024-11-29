package com.grookage.leia.client.utils;

import com.grookage.leia.models.annotations.Optional;
import com.grookage.leia.models.annotations.qualifiers.Encrypted;
import com.grookage.leia.models.annotations.qualifiers.PII;
import com.grookage.leia.models.annotations.qualifiers.ShortLived;
import com.grookage.leia.models.annotations.qualifiers.Standard;
import com.grookage.leia.models.attributes.ArrayAttribute;
import com.grookage.leia.models.attributes.BooleanAttribute;
import com.grookage.leia.models.attributes.DoubleAttribute;
import com.grookage.leia.models.attributes.EnumAttribute;
import com.grookage.leia.models.attributes.FloatAttribute;
import com.grookage.leia.models.attributes.IntegerAttribute;
import com.grookage.leia.models.attributes.LongAttribute;
import com.grookage.leia.models.attributes.MapAttribute;
import com.grookage.leia.models.attributes.ObjectAttribute;
import com.grookage.leia.models.attributes.SchemaAttribute;
import com.grookage.leia.models.attributes.StringAttribute;
import com.grookage.leia.models.qualifiers.EncryptedQualifier;
import com.grookage.leia.models.qualifiers.PIIQualifier;
import com.grookage.leia.models.qualifiers.QualifierInfo;
import com.grookage.leia.models.qualifiers.ShortLivedQualifier;
import com.grookage.leia.models.qualifiers.StandardQualifier;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SchemaUtil {
    public Set<SchemaAttribute> buildSchemaAttributes(final Class<?> klass) {
        return getAllFields(klass)
                .stream().map(SchemaUtil::schemaAttribute)
                .collect(Collectors.toSet());
    }

    private SchemaAttribute schemaAttribute(final Field field) {
        return schemaAttribute(
                field.getGenericType(),
                field.getName(),
                getQualifierInfo(field),
                isOptional(field)
        );
    }

    private SchemaAttribute schemaAttribute(final Type type,
                                            final String name,
                                            final Set<QualifierInfo> qualifiers,
                                            final boolean optional) {
        // Handle Class instances (eg. String, Enum classes, Complex POJO Objects etc.)
        if (type instanceof Class<?> klass) {
            return schemaAttribute(klass, name, qualifiers, optional);
        }

        // Handle ParameterizedType (e.g., List<String>, Map<String, Integer>)
        if (type instanceof ParameterizedType parameterizedType) {
            return schemaAttribute(parameterizedType, name, qualifiers, optional);
        }

        // Handle GenericArrayType (e.g., T[], List<T[]>)
        if (type instanceof GenericArrayType genericArrayType) {
            return schemaAttribute(genericArrayType, name, qualifiers, optional);
        }

        throw new UnsupportedOperationException("Unsupported field type: " + type.getTypeName());
    }

    private SchemaAttribute schemaAttribute(final ParameterizedType parameterizedType,
                                            final String name,
                                            final Set<QualifierInfo> qualifiers,
                                            final boolean optional) {
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        // Handle List<T> or Set<T>
        if (ClassUtils.isAssignable(rawType, Collection.class)) {
            return handleCollection(parameterizedType, name, qualifiers, optional);
        }

        // Handle Map<T,R>
        if (ClassUtils.isAssignable(rawType, Map.class)) {
            return handleMap(parameterizedType, name, qualifiers, optional);
        }
        throw new UnsupportedOperationException("Unsupported field type: " + parameterizedType.getTypeName());
    }

    private SchemaAttribute handleMap(ParameterizedType parameterizedType,
                                      String name,
                                      Set<QualifierInfo> qualifiers,
                                      boolean optional) {
        final var keyType = parameterizedType.getActualTypeArguments()[0];
        final var valueType = parameterizedType.getActualTypeArguments()[1];
        return new MapAttribute(
                name,
                optional,
                qualifiers,
                schemaAttribute(keyType, "key", getQualifierInfo(keyType), isOptional(keyType)),
                schemaAttribute(valueType, "value", getQualifierInfo(valueType), isOptional(valueType))
        );
    }

    private SchemaAttribute handleCollection(ParameterizedType parameterizedType,
                                             String name,
                                             Set<QualifierInfo> qualifiers, boolean optional) {
        final var elementType = parameterizedType.getActualTypeArguments()[0];
        return new ArrayAttribute(
                name,
                optional,
                qualifiers,
                schemaAttribute(elementType, "element", getQualifierInfo(elementType), isOptional(elementType))
        );
    }

    private SchemaAttribute schemaAttribute(final GenericArrayType genericArrayType,
                                            final String name,
                                            final Set<QualifierInfo> qualifiers,
                                            final boolean optional) {
        final var componentType = genericArrayType.getGenericComponentType();
        return new ArrayAttribute(
                name,
                optional,
                qualifiers,
                schemaAttribute(componentType, "element", getQualifierInfo(componentType), isOptional(componentType))
        );
    }


    private SchemaAttribute schemaAttribute(final Class<?> klass,
                                            final String name,
                                            Set<QualifierInfo> qualifiers,
                                            final boolean optional) {
        if (klass == String.class) {
            return new StringAttribute(name, optional, qualifiers);
        }

        if (klass.isEnum()) {
            return new EnumAttribute(name, optional, qualifiers, getEnumValues(klass));
        }

        if (klass.isPrimitive()) {
            return handlePrimitive(klass, name, qualifiers, optional);
        }

        // Handle String[], Object[] etc.
        if (klass.isArray()) {
            final var componentType = klass.getComponentType();
            return new ArrayAttribute(
                    name,
                    optional,
                    qualifiers,
                    schemaAttribute(componentType, "element", getQualifierInfo(componentType), isOptional(componentType))
            );
        }

        // Handling custom defined POJO's
        final var schemaAttributes = buildSchemaAttributes(klass);
        return new ObjectAttribute(name, optional, qualifiers, schemaAttributes);
    }

    private SchemaAttribute handlePrimitive(final Class<?> klass,
                                            final String name,
                                            final Set<QualifierInfo> qualifiers,
                                            final boolean optional) {
        if (klass == Integer.class || klass == int.class) {
            return new IntegerAttribute(name, optional, qualifiers);
        }
        if (klass == Boolean.class || klass == boolean.class) {
            return new BooleanAttribute(name, optional, qualifiers);
        }
        if (klass == Double.class || klass == double.class) {
            return new DoubleAttribute(name, optional, qualifiers);
        }
        if (klass == Long.class || klass == long.class) {
            return new LongAttribute(name, optional, qualifiers);
        }
        if (klass == Float.class || klass == float.class) {
            return new FloatAttribute(name, optional, qualifiers);
        }

        throw new UnsupportedOperationException("Unsupported primitive class type: " + klass.getName());

    }

    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    private Set<String> getEnumValues(Class<?> klass) {
        return Arrays.stream(klass.getEnumConstants())
                .map(enumConstant -> ((Enum<?>) enumConstant).name())
                .collect(Collectors.toSet());
    }

    private Set<QualifierInfo> getQualifierInfo(Field field) {
        Set<QualifierInfo> qualifierInfos = new HashSet<>();
        if (field.isAnnotationPresent(Encrypted.class)) {
            qualifierInfos.add(new EncryptedQualifier());
        }
        if (field.isAnnotationPresent(Standard.class)) {
            qualifierInfos.add(new StandardQualifier());
        }
        if (field.isAnnotationPresent(PII.class)) {
            qualifierInfos.add(new PIIQualifier());
        }
        if (field.isAnnotationPresent(ShortLived.class)) {
            final var shortLived = field.getAnnotation(ShortLived.class);
            qualifierInfos.add(new ShortLivedQualifier(shortLived.ttlSeconds()));
        }
        return qualifierInfos;
    }

    private Set<QualifierInfo> getQualifierInfo(Type type) {
        if (type instanceof Class<?> klass) {
            return getQualifierInfo(klass);
        }
        return new HashSet<>();
    }

    private Set<QualifierInfo> getQualifierInfo(Class<?> klass) {
        Set<QualifierInfo> qualifierInfos = new HashSet<>();
        if (klass.isAnnotationPresent(Encrypted.class)) {
            qualifierInfos.add(new EncryptedQualifier());
        }
        if (klass.isAnnotationPresent(Standard.class)) {
            qualifierInfos.add(new StandardQualifier());
        }
        if (klass.isAnnotationPresent(PII.class)) {
            qualifierInfos.add(new PIIQualifier());
        }
        if (klass.isAnnotationPresent(ShortLived.class)) {
            final var shortLived = klass.getAnnotation(ShortLived.class);
            qualifierInfos.add(new ShortLivedQualifier(shortLived.ttlSeconds()));
        }
        return qualifierInfos;
    }

    private boolean isOptional(Type type) {
        if (type instanceof Class<?> klass) {
            return isOptional(klass);
        }
        return false;
    }

    private boolean isOptional(Class<?> klass) {
        return klass.isAnnotationPresent(Optional.class);
    }

    private boolean isOptional(Field field) {
        return field.isAnnotationPresent(Optional.class);
    }
}
