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

package com.grookage.leia.http.processor.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotEmpty;
import java.util.Optional;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpBackendConfig {
    @NotEmpty
    String backendName;
    String backendId;
    String host;
    int port;
    boolean secure = true;
    String uri;
    @NotEmpty
    String hasher;
    @Builder.Default
    BackendType backendType = BackendType.SYNC;
    @Builder.Default
    int retryCount = 3;
    @Builder.Default
    String queuePath = "leia-messages";
    @Builder.Default
    int queueThreshold = 5;

    public Optional<LeiaHttpEndPoint> getEndPoint() {
        return Optional.of(
                LeiaHttpEndPoint.builder()
                        .uri(this.getUri())
                        .host(this.getHost())
                        .port(this.getPort())
                        .secure(this.isSecure())
                        .build()
        );
    }
}
