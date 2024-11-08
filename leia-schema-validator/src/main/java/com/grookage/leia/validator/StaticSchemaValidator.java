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

import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaKey;
import com.grookage.leia.validator.annotations.SchemaValidatable;
import com.grookage.leia.validator.exception.SchemaValidationException;
import com.grookage.leia.validator.exception.ValidationErrorCode;
import com.grookage.leia.validator.utils.SchemaValidationUtils;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class StaticSchemaValidator implements LeiaSchemaValidator {

    private final ConcurrentHashMap<SchemaKey, Boolean> validationRegistry = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<SchemaKey, Class<?>> klassRegistry = new ConcurrentHashMap<>();
    private final Supplier<List<SchemaDetails>> supplier;
    private final Set<String> packageRoots;

    @Builder
    public StaticSchemaValidator(Supplier<List<SchemaDetails>> supplier,
                                 Set<String> packageRoots) {
        this.supplier = supplier;
        this.packageRoots = packageRoots;
    }

    @SneakyThrows
    private boolean validate(final SchemaKey schemaKey, Class<?> klass) {
        final var details = supplier.get().stream()
                .filter(each -> each.match(schemaKey)).findFirst().orElse(null);
        if (null == details) {
            throw SchemaValidationException.error(ValidationErrorCode.NO_SCHEMA_FOUND);
        }
        return SchemaValidationUtils.valid(details, klass);
    }

    @Override
    public void start() {
        log.info("Starting the schema validator");
        packageRoots.forEach(handlerPackage -> {
            final var reflections = new Reflections(handlerPackage);
            final var annotatedClasses = reflections.getTypesAnnotatedWith(SchemaValidatable.class);
            annotatedClasses.forEach(annotatedClass -> {
                final var annotation = annotatedClass.getAnnotation(SchemaValidatable.class);
                final var schemaKey = SchemaKey.builder()
                        .schemaName(annotation.schemaName())
                        .version(annotation.versionId())
                        .namespace(annotation.namespace())
                        .build();
                klassRegistry.putIfAbsent(schemaKey, annotatedClass);
                validationRegistry.putIfAbsent(schemaKey, validate(schemaKey, annotatedClass));
            });
        });
        final var invalidSchemas = validationRegistry.keySet().stream()
                .filter(key -> !validationRegistry.get(key)).collect(Collectors.toSet());
        if (!invalidSchemas.isEmpty()) {
            log.error("Found invalid schemas. Please fix the following schemas to start the bundle {}", invalidSchemas);
            throw SchemaValidationException.error(ValidationErrorCode.INVALID_SCHEMAS);
        }
    }

    @Override
    public void stop() {
        log.info("Stopping the schema validator");
    }

    @Override
    public boolean valid(SchemaKey schemaKey) {
        return validationRegistry.computeIfAbsent(schemaKey, key -> {
            final var klass = getKlass(key).orElse(null);
            return null == klass ? Boolean.FALSE : validate(key, klass);
        });
    }

    @Override
    public Optional<Class<?>> getKlass(SchemaKey schemaKey) {
        return Optional.ofNullable(klassRegistry.get(schemaKey));
    }

}
