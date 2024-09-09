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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grookage.leia.provider.config.LeiaHttpConfiguration;
import com.grookage.leia.provider.endpoint.LeiaEndPoint;
import com.grookage.leia.provider.endpoint.LeiaEndPointProvider;
import com.grookage.leia.provider.endpoint.SimpleEndPointProvider;
import com.grookage.leia.provider.exceptions.RefresherErrorCode;
import com.grookage.leia.provider.exceptions.RefresherException;
import com.grookage.leia.provider.utils.OkHttpUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.Objects;

@Slf4j
public abstract class LeiaHttpSupplier<T> implements LeiaSupplier<T> {

    private final ObjectMapper mapper;
    private final Class<T> configKlass;
    private final LeiaEndPointProvider endpointProvider;
    private final OkHttpClient okHttpClient;

    @SneakyThrows
    protected LeiaHttpSupplier(
            LeiaHttpConfiguration httpConfiguration,
            ObjectMapper mapper,
            Class<T> klass,
            String name
    ) {
        this.configKlass = klass;
        this.mapper = mapper;
        this.endpointProvider = SimpleEndPointProvider.builder()
                .endPoint(
                        LeiaEndPoint.builder()
                                .host(httpConfiguration.getHost())
                                .port(httpConfiguration.getPort())
                                .rootPathPrefix(httpConfiguration.getRootPathPrefix())
                                .scheme(httpConfiguration.getScheme())
                                .build()
                )
                .build();
        this.okHttpClient = OkHttpUtils.okHttpClient(httpConfiguration, name);
    }


    protected HttpUrl endPoint(final String path) {
        return endpointProvider
                .endPoint()
                .orElseThrow(IllegalArgumentException::new)
                .url(path);
    }

    @Override
    public T get() {
        final var url = url();
        try {
            final var request = getRequest(url);
            if (null == request) {
                return null; //Gracefully ignore here, the configSupplier will not override, on the null value
            }
            final var response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("Request Failed for uri {} with response:{}", url,
                        Objects.requireNonNull(response.body()).string());
                throw RefresherException.error(RefresherErrorCode.BAD_REQUEST);
            }
            final var body = OkHttpUtils.bodyAsBytes(response);
            return mapper.readValue(body, configKlass);
        } catch (Exception e) {
            log.error("Error while executing API with uri: {}", url, e);
            throw RefresherException.error(RefresherErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    protected abstract String url();

    protected abstract Request getRequest(String url);

    public void start() {
        endpointProvider.start();
    }

    public void stop() {
        endpointProvider.stop();
    }
}
