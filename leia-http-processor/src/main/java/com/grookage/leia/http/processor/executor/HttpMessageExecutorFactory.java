/*
 * Copyright (c) 2025. Koushik R <rkoushik.14@gmail.com>.
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

package com.grookage.leia.http.processor.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.grookage.leia.http.processor.config.HttpClientConfig;
import com.grookage.leia.http.processor.endpoint.EndPointResolver;
import com.grookage.leia.mux.executor.MessageExecutor;
import com.grookage.leia.mux.executor.MessageExecutorFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class HttpMessageExecutorFactory implements MessageExecutorFactory {
    private final Map<String, MessageExecutor> executors = new ConcurrentHashMap<>();

    public HttpMessageExecutorFactory(final HttpClientConfig clientConfig,
                                      final Supplier<String> authSupplier,
                                      final EndPointResolver endPointResolver,
                                      final ObjectMapper mapper) {
        Preconditions.checkNotNull(clientConfig, "Client Config can't be null");
        Preconditions.checkNotNull(authSupplier, "Auth Supplier can't be null");
        Preconditions.checkNotNull(mapper, "Object mapper can't be null");
        Preconditions.checkNotNull(endPointResolver, "Endpoint Resolver can't be null");
        clientConfig.getBackendConfigs()
                .forEach(backendConfig -> executors.put(backendConfig.getBackendName().toUpperCase(Locale.ROOT), new HttpMessageExecutor(
                        backendConfig,
                        authSupplier,
                        mapper,
                        endPointResolver
                )));
    }

    @Override
    public Optional<MessageExecutor> getExecutor(String backendName) {
        return Optional.ofNullable(executors.get(backendName.toUpperCase(Locale.ROOT)));
    }
}
