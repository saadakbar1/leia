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

package com.grookage.leia.core.retrieval;

import com.google.common.base.Preconditions;
import com.grookage.leia.core.ingestion.utils.SchemaUtils;
import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaKey;
import com.grookage.leia.models.schema.engine.SchemaState;
import com.grookage.leia.repository.SchemaRepository;
import com.grookage.leia.repository.config.CacheConfig;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class SchemaRetriever {

    private final SchemaRepository schemaRepository;
    private final CacheConfig cacheConfig;
    private RepositoryRefresher refresher;

    @Builder
    public SchemaRetriever(final SchemaRepository schemaRepository,
                           final CacheConfig cacheConfig) {
        Preconditions.checkNotNull(schemaRepository, "Schema Repository can't be null");
        this.schemaRepository = schemaRepository;
        this.cacheConfig = cacheConfig;

        if (null != cacheConfig && cacheConfig.isEnabled()) {
            final var supplier = new RepositorySupplier(schemaRepository);
            supplier.start();
            this.refresher = RepositoryRefresher.builder()
                    .supplier(supplier)
                    .configRefreshTimeSeconds(cacheConfig.getRefreshCacheSeconds())
                    .build();
        }
    }

    public Optional<SchemaDetails> getSchemaDetails(final SchemaKey schemaKey) {
        if (null != cacheConfig && cacheConfig.isEnabled()) {
            return this.refresher.getConfiguration().getSchemaDetails(schemaKey);
        } else {
            final var storedSchema = schemaRepository.get(schemaKey);
            return storedSchema.map(SchemaUtils::toSchemaDetails);
        }
    }

    public List<SchemaDetails> getCurrentSchemaDetails(final Set<String> namespaces) {
        if (null != cacheConfig && cacheConfig.isEnabled()) {
            return this.refresher.getConfiguration().getSchemaDetails(namespaces);
        } else {
            return schemaRepository.getSchemas(namespaces, Set.of(SchemaState.APPROVED),
                    SchemaUtils::toSchemaDetails);
        }
    }

    public List<SchemaDetails> getAllSchemaDetails(final Set<String> namespaces) {
        if (null != cacheConfig && cacheConfig.isEnabled()) {
            return this.refresher.getConfiguration().getAllSchemaDetails(namespaces);
        } else {
            return schemaRepository.getSchemas(namespaces, Arrays.stream(SchemaState.values()).collect(Collectors.toSet()),
                    SchemaUtils::toSchemaDetails);
        }
    }
}
