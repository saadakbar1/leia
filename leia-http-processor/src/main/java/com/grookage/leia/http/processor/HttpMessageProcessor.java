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

import com.grookage.leia.http.processor.config.HttpClientConfig;
import com.grookage.leia.http.processor.utils.HttpClientUtils;
import com.grookage.leia.mux.AbstractMessageProcessor;
import com.grookage.leia.mux.executor.MessageExecutor;
import com.grookage.leia.mux.executor.MessageExecutorFactory;
import com.grookage.leia.mux.resolver.BackendNameResolver;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@EqualsAndHashCode(callSuper = true)
public class HttpMessageProcessor extends AbstractMessageProcessor {

    private final HttpClientConfig clientConfig;

    @Builder
    public HttpMessageProcessor(final HttpClientConfig clientConfig,
                                final BackendNameResolver backendNameResolver,
                                final MessageExecutorFactory executorFactory) {
        super(clientConfig.getClientId(), clientConfig.getMessageProcessingThresholdMs(),
                backendNameResolver, executorFactory);
        HttpClientUtils.initialize(clientConfig);
        this.clientConfig = clientConfig;
    }

    @Override
    protected boolean validBackends(List<String> backends) {
        return !(backends.isEmpty() && clientConfig.isFailOnUnavailableBackend());
    }

    @Override
    protected boolean validExecutor(MessageExecutor executor) {
        return !(null == executor && clientConfig.isFailOnUnavailableBackend());
    }
}
