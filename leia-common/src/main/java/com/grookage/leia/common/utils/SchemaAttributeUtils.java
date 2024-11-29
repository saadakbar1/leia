package com.grookage.leia.common.utils;

import com.grookage.leia.models.annotations.attribute.Optional;
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
import com.grookage.leia.models.qualifiers.QualifierInfo;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SchemaAttributeUtils {
    public Set<SchemaAttribute> getSchemaAttributes(final Class<?> klass) {
        return Utils.getAllFields(klass)
                .stream()
                .map(SchemaAttributeUtils::schemaAttribute)
                .collect(Collectors.toSet());
    }

    private SchemaAttribute schemaAttribute(final Field field) {
        return schemaAttribute(
                field.getGenericType(),
                field.getName(),
                QualifierUtils.getQualifierInfo(field),
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
        final var rawType = (Class<?>) parameterizedType.getRawType();
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
                schemaAttribute(keyType, "key", QualifierUtils.getQualifierInfo(keyType), isOptional(keyType)),
                schemaAttribute(valueType, "value", QualifierUtils.getQualifierInfo(valueType), isOptional(valueType))
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
                schemaAttribute(elementType, "element", QualifierUtils.getQualifierInfo(elementType),
                        isOptional(elementType))
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
                schemaAttribute(componentType, "element", QualifierUtils.getQualifierInfo(componentType),
                        isOptional(componentType))
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
            return new EnumAttribute(name, optional, qualifiers, Utils.getEnumValues(klass));
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
                    schemaAttribute(componentType, "element", QualifierUtils.getQualifierInfo(componentType),
                            isOptional(componentType))
            );
        }

        // Handling custom defined POJO's
        final var schemaAttributes = getSchemaAttributes(klass);
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
