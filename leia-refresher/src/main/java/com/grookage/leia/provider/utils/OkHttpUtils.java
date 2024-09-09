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

package com.grookage.leia.provider.utils;

import com.grookage.leia.provider.config.LeiaHttpConfiguration;
import lombok.experimental.UtilityClass;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class OkHttpUtils {

    private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 10;
    private static final int DEFAULT_IDLE_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10000;
    private static final int DEFAULT_OP_TIMEOUT_MILLIS = 10000;

    public static OkHttpClient okHttpClient(final LeiaHttpConfiguration configuration,
                                            final String name) {
        var maxIdleConnections = configuration.getConnections();
        maxIdleConnections =
                maxIdleConnections == 0 ? DEFAULT_MAX_IDLE_CONNECTIONS : maxIdleConnections;

        var idleTimeOutSeconds = configuration.getIdleTimeOutSeconds();
        idleTimeOutSeconds =
                idleTimeOutSeconds == 0 ? DEFAULT_IDLE_TIMEOUT_SECONDS : idleTimeOutSeconds;

        var connTimeout = configuration.getConnectTimeoutMs();
        connTimeout = connTimeout == 0 ? DEFAULT_CONNECT_TIMEOUT_MILLIS : connTimeout;

        var opTimeout = configuration.getOpTimeoutMs();
        opTimeout = opTimeout == 0 ? DEFAULT_OP_TIMEOUT_MILLIS : opTimeout;

        var maxConcurrentRequests = configuration.getMaxConcurrentRequests() <= 0
                ? maxIdleConnections
                : configuration.getMaxConcurrentRequests();

        var maxConcurrentRequestsPerHost = configuration.getMaxConcurrentRequestsPerHost() <= 0
                ? maxIdleConnections
                : configuration.getMaxConcurrentRequestsPerHost();

        var dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(maxConcurrentRequests);
        dispatcher.setMaxRequestsPerHost(maxConcurrentRequestsPerHost);

        return new OkHttpClient.Builder()
                .connectionPool(
                        new ConnectionPool(maxIdleConnections, idleTimeOutSeconds, TimeUnit.SECONDS))
                .connectTimeout(connTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(opTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(opTimeout, TimeUnit.MILLISECONDS)
                .dispatcher(dispatcher)
                .build();
    }

    public static byte[] bodyAsBytes(Response response) throws IOException {
        try (final ResponseBody body = response.body()) {
            return null == body ? null : body.bytes();
        }
    }
}
