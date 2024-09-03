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

package com.grookage.leia.core.engine;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.grookage.leia.core.LeiaExecutor;
import com.grookage.leia.models.schema.engine.SchemaEvent;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class SchemaProcessorHub {

    private static final String HANDLER_PACKAGE = "com.grookage.leia";

    private final Map<SchemaEvent, SchemaProcessor> processors = Maps.newHashMap();

    public SchemaProcessorHub(Injector injector) {
        Preconditions.checkNotNull(injector, "Injector cannot be null");
        final var reflections = new Reflections(HANDLER_PACKAGE);
        final var annotatedClasses = reflections.getTypesAnnotatedWith(LeiaExecutor.class);
        annotatedClasses.forEach(annotatedType -> {
            if (SchemaProcessor.class.isAssignableFrom(annotatedType)) {
                final var instance = (SchemaProcessor) injector.getInstance(annotatedType);
                processors.putIfAbsent(instance.name(), instance);
            }
        });
    }

    public Optional<SchemaProcessor> getProcessor(final SchemaEvent event) {
        return Optional.ofNullable(processors.get(event));
    }

}
