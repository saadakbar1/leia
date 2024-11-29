package com.grookage.leia.common.utils;

import com.grookage.leia.models.annotations.qualifiers.Encrypted;
import com.grookage.leia.models.annotations.qualifiers.PII;
import com.grookage.leia.models.annotations.qualifiers.ShortLived;
import com.grookage.leia.models.qualifiers.EncryptedQualifier;
import com.grookage.leia.models.qualifiers.PIIQualifier;
import com.grookage.leia.models.qualifiers.QualifierInfo;
import com.grookage.leia.models.qualifiers.ShortLivedQualifier;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class QualifierUtils {
    public Set<QualifierInfo> getQualifierInfo(Type type) {
        if (type instanceof Class<?> klass) {
            return getQualifierInfo(klass);
        }
        return new HashSet<>();
    }

    public Set<QualifierInfo> getQualifierInfo(Field field) {
        Set<QualifierInfo> qualifierInfos = new HashSet<>();
        if (field.isAnnotationPresent(Encrypted.class)) {
            qualifierInfos.add(new EncryptedQualifier());
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

    public Set<QualifierInfo> getQualifierInfo(Class<?> klass) {
        Set<QualifierInfo> qualifierInfos = new HashSet<>();
        if (klass.isAnnotationPresent(Encrypted.class)) {
            qualifierInfos.add(new EncryptedQualifier());
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
}
