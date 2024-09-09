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

package com.grookage.leia.provider.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grookage.leia.provider.endpoint.EndPointScheme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeiaHttpConfiguration {

    @NotEmpty
    private String host;

    @Min(1)
    private int port;

    @Builder.Default
    private final EndPointScheme scheme = EndPointScheme.HTTPS;

    @Builder.Default
    private final int refreshTimeSeconds = 30;

    private String rootPathPrefix;

    @Min(10)
    @Max(1024)
    @Builder.Default
    private final int connections = 10;

    @Min(0)
    @Max(1024)
    private int maxConcurrentRequests;

    @Min(0)
    @Max(1024)
    private int maxConcurrentRequestsPerHost;

    @Max(86400)
    @Builder.Default
    private final int idleTimeOutSeconds = 30;

    @Max(86400000)
    @Builder.Default
    private final int connectTimeoutMs = 10000;

    @Max(86400000)
    @Builder.Default
    private final int opTimeoutMs = 10000;

    @Min(0L)
    private int retryInterval;
}
