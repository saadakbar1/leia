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

package com.grookage.leia.dropwizard.bundle;

import com.grookage.leia.core.ingestion.SchemaIngestor;
import com.grookage.leia.core.ingestion.VersionIDGenerator;
import com.grookage.leia.core.ingestion.hub.SchemaProcessorHub;
import com.grookage.leia.dropwizard.bundle.mapper.LeiaExceptionMapper;
import com.grookage.leia.models.user.SchemaUpdater;
import com.grookage.leia.repository.SchemaRepository;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public abstract class LeiaBundle<T extends Configuration, U extends SchemaUpdater> implements ConfiguredBundle<T> {

    private SchemaIngestor<U> schemaIngestor;
    private SchemaRepository schemaRepository;

    protected abstract SchemaRepository getSchemaRepository(T configuration);

    protected abstract VersionIDGenerator getVersionIDGenerator();

    @Override
    public void run(T configuration, Environment environment) {
        final var schemaProcessorHub = new SchemaProcessorHub()
                .withSchemaRepository(getSchemaRepository(configuration))
                .withVersionIDGenerator(getVersionIDGenerator())
                .build();
        this.schemaIngestor = new SchemaIngestor<U>()
                .withProcessorHub(schemaProcessorHub)
                .build();
        this.schemaRepository = getSchemaRepository(configuration);
        environment.jersey().register(new LeiaExceptionMapper());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }
}
