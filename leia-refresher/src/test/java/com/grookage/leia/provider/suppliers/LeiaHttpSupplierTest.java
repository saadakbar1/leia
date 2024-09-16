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

package com.grookage.leia.provider.suppliers;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.grookage.leia.models.ResourceHelper;
import com.grookage.leia.provider.config.LeiaHttpConfiguration;
import com.grookage.leia.provider.endpoint.EndPointScheme;
import com.grookage.leia.provider.stubs.TestDetails;
import com.grookage.leia.provider.stubs.TestMarshaller;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SuppressWarnings("KotlinInternalInJava")
@WireMockTest
class LeiaHttpSupplierTest {

    @Test
    @SneakyThrows
    void testHttpSupplier(WireMockRuntimeInfo wireMockRuntimeInfo) {
        stubFor(get(urlEqualTo("/v1/test"))
                .willReturn(aResponse()
                        .withBody(ResourceHelper.getObjectMapper().writeValueAsBytes(TestDetails.getTestableDetails()))
                        .withStatus(200)));
        val clientConfig = LeiaHttpConfiguration.builder()
                .host("127.0.0.1")
                .port(wireMockRuntimeInfo.getHttpPort())
                .scheme(EndPointScheme.HTTP)
                .build();
        final var testableDetails = getSupplier(clientConfig).get();
        Assertions.assertNotNull(testableDetails);
        Assertions.assertEquals("attribute1", testableDetails.getAttribute1());
        Assertions.assertEquals("attribute2", testableDetails.getAttribute2());
        Assertions.assertEquals("attribute3", testableDetails.getAttribute3());
    }

    private LeiaHttpSupplier<TestDetails> getSupplier(LeiaHttpConfiguration clientConfig) {
        return new LeiaHttpSupplier<>(clientConfig, new TestMarshaller(), "testSupplier") {
            @Override
            protected String url() {
                return "/v1/test";
            }

            @Override
            protected Request getRequest(String url) {
                return new Request.Builder()
                        .url(endPoint(url))
                        .get()
                        .build();
            }
        };
    }
}
