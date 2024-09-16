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

package com.grookage.leia.provider.endpoint;

import com.grookage.leia.models.ResourceHelper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LeiaEndpointTest {

    @Test
    @SneakyThrows
    void testLeiaEndPoint() {
        final var endPoint = ResourceHelper.getResource(
                "endPoint.json",
                LeiaEndPoint.class
        );
        Assertions.assertNotNull(endPoint);
        Assertions.assertEquals("testHost", endPoint.getHost());
        Assertions.assertEquals(8080, endPoint.getPort());
        final var url = endPoint.url("/v1/test");
        Assertions.assertNotNull(url);
        Assertions.assertTrue("https://testHost:8080/v1/test".equalsIgnoreCase(url.toString()));
    }
}
