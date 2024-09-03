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

package com.grookage.leia.models.schema.engine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.function.Function;

@NoArgsConstructor
public class SchemaContext {

    private final ContextData data = new ContextData();

    @JsonIgnore
    public <V> void addContext(String key, V value) {
        if (Strings.isNullOrEmpty(key.toUpperCase())) {
            throw new IllegalArgumentException("Invalid key for context data. Key cannot be null/empty");
        }
        this.data.put(key.toUpperCase(), value);
    }

    @JsonIgnore
    public <T> Optional<T> getContext(String key, Function<Object, Optional<T>> converter) {
        var value = this.data.get(key.toUpperCase());
        return converter.apply(value);
    }
}
