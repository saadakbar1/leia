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

package com.grookage.leia.core.ingestion.hub;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.grookage.leia.core.ingestion.VersionIDGenerator;
import com.grookage.leia.core.ingestion.processors.*;
import com.grookage.leia.models.schema.engine.SchemaEvent;
import com.grookage.leia.models.schema.engine.SchemaEventVisitor;
import com.grookage.leia.repository.SchemaRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SchemaProcessorHub {

    private final Map<SchemaEvent, SchemaProcessor> processors = Maps.newHashMap();
    private SchemaRepository schemaRepository;
    private VersionIDGenerator versionIDGenerator;

    private SchemaProcessorHub() {

    }

    public static SchemaProcessorHub of() {
        return new SchemaProcessorHub();
    }

    public SchemaProcessorHub withSchemaRepository(SchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
        return this;
    }

    public SchemaProcessorHub withVersionIDGenerator(VersionIDGenerator versionIDGenerator) {
        this.versionIDGenerator = versionIDGenerator;
        return this;
    }

    public SchemaProcessorHub build() {
        Preconditions.checkNotNull(schemaRepository, "Schema Repository can't be null");
        Preconditions.checkNotNull(versionIDGenerator, "Version ID Generator can't be null");
        Arrays.stream(SchemaEvent.values()).forEach(this::buildProcessor);
        return this;
    }

    public void buildProcessor(final SchemaEvent event) {
        processors.putIfAbsent(event, event.accept(new SchemaEventVisitor<>() {
            @Override
            public SchemaProcessor schemaCreate() {
                return CreateSchemaProcessor.builder()
                        .schemaRepository(schemaRepository)
                        .versionIDGenerator(versionIDGenerator)
                        .build();
            }

            @Override
            public SchemaProcessor schemaUpdate() {
                return UpdateSchemaProcessor.builder()
                        .schemaRepository(schemaRepository)
                        .versionIDGenerator(versionIDGenerator)
                        .build();
            }

            @Override
            public SchemaProcessor schemaApprove() {
                return ApproveSchemaProcessor.builder()
                        .schemaRepository(schemaRepository)
                        .versionIDGenerator(versionIDGenerator)
                        .build();
            }

            @Override
            public SchemaProcessor schemaReject() {
                return RejectSchemaProcessor.builder()
                        .schemaRepository(schemaRepository)
                        .versionIDGenerator(versionIDGenerator)
                        .build();
            }
        }));
    }


    public Optional<SchemaProcessor> getProcessor(final SchemaEvent event) {
        return Optional.ofNullable(processors.get(event));
    }

}
