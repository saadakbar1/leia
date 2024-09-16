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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import lombok.*;
import okhttp3.HttpUrl;

import java.net.URI;
import java.util.Locale;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeiaEndPoint {

    @Builder.Default
    private EndPointScheme scheme = EndPointScheme.HTTPS;
    private String host;
    private int port;
    private String rootPathPrefix;

    public HttpUrl url(final String path) {
        final var obtainedScheme = null == scheme ? EndPointScheme.HTTP : scheme;
        final var completePath =
                Strings.isNullOrEmpty(rootPathPrefix) ? path : String.format("/%s%s", rootPathPrefix,
                        path);
        return HttpUrl.get(URI.create(
                String.format("%s://%s:%d%s", obtainedScheme.name().toLowerCase(Locale.ROOT), host, port,
                        completePath)));
    }
}
