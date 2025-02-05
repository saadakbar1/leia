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

package com.grookage.leia.http.processor;

import com.google.common.base.Preconditions;
import com.grookage.leia.http.processor.config.HttpClientConfig;
import com.grookage.leia.http.processor.exception.LeiaHttpErrorCode;
import com.grookage.leia.http.processor.executor.HttpExecutor;
import com.grookage.leia.http.processor.executor.HttpExecutorFactory;
import com.grookage.leia.http.processor.resolver.BackendNameResolver;
import com.grookage.leia.http.processor.utils.HttpClientUtils;
import com.grookage.leia.models.exception.LeiaException;
import com.grookage.leia.models.mux.LeiaMessage;
import com.grookage.leia.mux.processors.hub.MessageProcessor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class HttpMessageProcessor extends MessageProcessor {
    private final HttpClientConfig clientConfig;
    private final BackendNameResolver backendNameResolver;
    private final HttpExecutorFactory executorFactory;

    @Builder
    public HttpMessageProcessor(final HttpClientConfig clientConfig,
                                final BackendNameResolver backendNameResolver,
                                final HttpExecutorFactory executorFactory) {
        super("http_processor");
        Preconditions.checkNotNull(clientConfig, "Http Client Config can't be null");
        Preconditions.checkNotNull(backendNameResolver, "Backend Resolver can't be null");
        HttpClientUtils.initialize(clientConfig);
        this.clientConfig = clientConfig;
        this.backendNameResolver = backendNameResolver;
        this.executorFactory = executorFactory;
    }

    private Map<HttpExecutor, List<LeiaMessage>> getExecutorMapping(List<LeiaMessage> messages) {
        final var executorMapping = new HashMap<HttpExecutor, List<LeiaMessage>>();
        messages.forEach(message -> {
            final var backends = backendNameResolver.getEligibleBackends(message);
            if (backends.isEmpty() && clientConfig.isFailOnUnavailableBackend()) {
                log.error("No backends found for message with schemaKey {} and tags {}", message.getSchemaKey(), message.getTags());
                throw LeiaException.error(LeiaHttpErrorCode.BACKENDS_NOT_FOUND);
            }
            backends.forEach(backend -> {
                final var executor = executorFactory.getExecutor(backend).orElse(null);
                if (null == executor && clientConfig.isFailOnUnavailableBackend()) {
                    log.error("No executor found for backend name {}", backend);
                    throw LeiaException.error(LeiaHttpErrorCode.EXECUTOR_NOT_FOUND);
                }
                executorMapping.computeIfAbsent(executor, k -> new ArrayList<>()).add(message);
            });
        });
        return executorMapping;
    }

    @Override
    public void processMessages(List<LeiaMessage> messages) {
        final var executorMapping = getExecutorMapping(messages);
        if (executorMapping.isEmpty()) {
            log.debug("Haven't found any eligible executors with the set of messages {}", messages);
            return;
        }
        final var futures = CompletableFuture.allOf(
                executorMapping.entrySet().stream()
                        .map(each -> CompletableFuture.runAsync(
                                () -> each.getKey().send(each.getValue())))
                        .toArray(CompletableFuture[]::new));
        try {
            futures.get();
        } catch (InterruptedException e) {
            log.error("Couldn't perform the message processor execution. It exceeded the process duration", e);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Couldn't perform the message processor execution. It exceeded the process duration");
        } catch (Exception e) {
            log.error("There is an exception while trying to process messages", e);
            throw new IllegalStateException("There is an exception while trying to process messages", e);
        }
    }
}
