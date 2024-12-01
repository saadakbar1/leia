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

package com.grookage.leia.common.utils;

import com.grookage.leia.models.annotations.attribute.qualifiers.Encrypted;
import com.grookage.leia.models.annotations.attribute.qualifiers.PII;
import com.grookage.leia.models.annotations.attribute.qualifiers.ShortLived;
import com.grookage.leia.models.qualifiers.EncryptedQualifier;
import com.grookage.leia.models.qualifiers.PIIQualifier;
import com.grookage.leia.models.qualifiers.QualifierInfo;
import com.grookage.leia.models.qualifiers.QualifierType;
import com.grookage.leia.models.qualifiers.ShortLivedQualifier;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@UtilityClass
public class QualifierUtils {
    public Optional<QualifierInfo> filter(final Set<QualifierInfo> qualifiers,
                                          final QualifierType type) {
        if (Objects.isNull(qualifiers)) {
            return Optional.empty();
        }
        return qualifiers.stream()
                .filter(qualifierInfo -> qualifierInfo.getType().equals(type))
                .findFirst();
    }

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
