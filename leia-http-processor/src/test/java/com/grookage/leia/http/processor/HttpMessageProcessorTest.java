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

import com.fasterxml.jackson.core.type.TypeReference;
import com.grookage.leia.http.processor.config.HttpClientConfig;
import com.grookage.leia.http.processor.executor.HttpExecutor;
import com.grookage.leia.http.processor.executor.HttpExecutorFactory;
import com.grookage.leia.http.processor.resolver.TagBasedBackedNameResolver;
import com.grookage.leia.models.ResourceHelper;
import com.grookage.leia.models.exception.LeiaException;
import com.grookage.leia.models.mux.LeiaMessage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

class HttpMessageProcessorTest {

    @Test
    @SneakyThrows
    void testHttpMessageProcessor() {
        final var clientConfig = ResourceHelper.getResource("httpClientConfig.json", HttpClientConfig.class);
        Assertions.assertNotNull(clientConfig);
        final var resolver = new TagBasedBackedNameResolver(clientConfig);
        final var httpExecutor = Mockito.mock(HttpExecutor.class);
        final var executorFactory = new HttpExecutorFactory() {
            @Override
            public Optional<HttpExecutor> getExecutor(String backendName) {
                return backendName.equalsIgnoreCase("BACKEND1") ? Optional.of(httpExecutor) : Optional.empty();
            }
        };
        final var leiaMessages = ResourceHelper.getResource("mux/leiaMessages.json", new TypeReference<List<LeiaMessage>>() {
        });
        final var httpMessageProcessor = new HttpMessageProcessor(clientConfig, resolver, executorFactory);
        Assertions.assertThrows(LeiaException.class, () -> httpMessageProcessor.processMessages(leiaMessages));
        leiaMessages.forEach(leiaMessage -> leiaMessage.setTags(List.of("backend-backend1::backend2",
                "importance-mild::extreme")));
        httpMessageProcessor.processMessages(leiaMessages);
        Mockito.verify(httpExecutor, Mockito.times(1)).send(leiaMessages);
    }
}
