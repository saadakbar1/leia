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

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.grookage.leia.http.processor.config.HttpClientConfig;
import com.grookage.leia.http.processor.endpoint.DefaultEndPointResolver;
import com.grookage.leia.http.processor.utils.HttpClientUtils;
import com.grookage.leia.http.processor.utils.HttpRequestUtils;
import com.grookage.leia.models.ResourceHelper;
import com.grookage.leia.models.mux.LeiaMessage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest
class HttpExecutorTest {

    @Test
    @SneakyThrows
    void testSyncMessageSending(WireMockRuntimeInfo wireMockRuntimeInfo) {
        final var clientConfig = ResourceHelper.getResource("httpClientConfig.json", HttpClientConfig.class);
        HttpClientUtils.initialize(clientConfig);
        Assertions.assertNotNull(clientConfig);
        final var backend = clientConfig.getBackendConfigs().stream().findFirst().orElse(null);
        Assertions.assertNotNull(backend);
        backend.setPort(wireMockRuntimeInfo.getHttpPort());
        backend.setUri("/ingest");
        final var endPointResolver = new DefaultEndPointResolver();
        final var messages = ResourceHelper.getResource("mux/leiaMessages.json", new TypeReference<List<LeiaMessage>>() {
        });
        Assertions.assertNotNull(messages);
        Assertions.assertFalse(messages.isEmpty());
        final var entityMessages = HttpRequestUtils.toHttpEntity(messages, backend);
        stubFor(post(urlEqualTo("/ingest"))
                .withRequestBody(binaryEqualTo(ResourceHelper.getObjectMapper().writeValueAsBytes(entityMessages)))
                .willReturn(aResponse()
                        .withStatus(200)));
        final var testableExecutor = new HttpMessageExecutor(backend, () -> "Bearer 1234", ResourceHelper.getObjectMapper(), endPointResolver);
        testableExecutor.send(messages);
    }
}
