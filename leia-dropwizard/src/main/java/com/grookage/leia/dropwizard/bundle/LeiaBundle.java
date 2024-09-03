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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.inject.Injector;
import com.grookage.leia.core.engine.SchemaProcessorHub;
import com.grookage.leia.core.ingestion.IngestionService;
import com.grookage.leia.dropwizard.bundle.mapper.LeiaExceptionMapper;
import com.grookage.leia.models.user.SchemaUpdater;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@NoArgsConstructor
@Getter
public abstract class LeiaBundle<T extends Configuration, U extends SchemaUpdater> implements ConfiguredBundle<T> {

    private IngestionService<U> ingestionService;

    protected abstract Supplier<Injector> getInjector();

    @Override
    public void run(T configuration, Environment environment) {
        environment.getObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        environment.getObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        environment.getObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        environment.getObjectMapper()
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        environment.getObjectMapper()
                .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        this.ingestionService = new IngestionService<U>()
                .withMapper(environment.getObjectMapper())
                .withProcessorHub(new SchemaProcessorHub(getInjector().get()))
                .build();
        environment.jersey().register(new LeiaExceptionMapper());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }


}
