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
import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaKey;
import com.grookage.leia.models.schema.engine.SchemaState;
import com.grookage.leia.repository.SchemaRepository;
import com.grookage.leia.repository.config.CacheConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class SchemaRetriever {

    private final Supplier<SchemaRepository> repositorySupplier;
    private final CacheConfig cacheConfig;
    private RepositoryRefresher refresher;

    @Builder
    public SchemaRetriever(final Supplier<SchemaRepository> repositorySupplier,
                           final CacheConfig cacheConfig) {
        Preconditions.checkNotNull(repositorySupplier, "Schema Repository can't be null");
        this.repositorySupplier = repositorySupplier;
        this.cacheConfig = cacheConfig;

        if (null != cacheConfig && cacheConfig.isEnabled()) {
            final var supplier = new RepositorySupplier(repositorySupplier);
            supplier.start();
            this.refresher = RepositoryRefresher.builder()
                    .supplier(supplier)
                    .refreshTimeInSeconds(cacheConfig.getRefreshCacheSeconds())
                    .periodicRefresh(true)
                    .build();
            refresher.start();
        }
    }

    public Optional<SchemaDetails> getSchemaDetails(final SchemaKey schemaKey) {
        if (null != cacheConfig && cacheConfig.isEnabled()) {
            return this.refresher.getData().getSchemaDetails(schemaKey);
        } else {
            return repositorySupplier.get().get(schemaKey);
        }
    }

    public List<SchemaDetails> getCurrentSchemaDetails(final Set<String> namespaces) {
        if (null != cacheConfig && cacheConfig.isEnabled()) {
            return this.refresher.getData().getSchemaDetails(namespaces);
        } else {
            return repositorySupplier.get().getSchemas(namespaces, Set.of(SchemaState.APPROVED));
        }
    }

    public List<SchemaDetails> getAllSchemaDetails(final Set<String> namespaces) {
        if (null != cacheConfig && cacheConfig.isEnabled()) {
            return this.refresher.getData().getAllSchemaDetails(namespaces);
        } else {
            return repositorySupplier.get().getSchemas(namespaces, Arrays.stream(SchemaState.values()).collect(Collectors.toSet()));
        }
    }
}
