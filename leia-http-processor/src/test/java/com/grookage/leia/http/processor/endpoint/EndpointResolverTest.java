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

package com.grookage.leia.http.processor.endpoint;

import com.grookage.leia.http.processor.config.HttpClientConfig;
import com.grookage.leia.models.ResourceHelper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EndpointResolverTest {

    @Test
    @SneakyThrows
    void testDefaultResolver() {
        final var clientConfig = ResourceHelper.getResource("httpClientConfig.json", HttpClientConfig.class);
        final var defaultResolver = new DefaultEndPointResolver();
        final var backend = clientConfig.getBackendConfigs().stream().findFirst().orElse(null);
        Assertions.assertNotNull(backend);
        final var endPoint = defaultResolver.getEndPoint(backend, null).orElse(null);
        Assertions.assertNotNull(endPoint);
        Assertions.assertEquals("127.0.0.1", endPoint.getHost());
        Assertions.assertEquals(8080, endPoint.getPort());
    }
}
