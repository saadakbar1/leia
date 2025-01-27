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

package com.grookage.leia.models.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grookage.leia.models.schema.engine.SchemaEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemaHistoryItem {

    @NotNull SchemaEvent schemaEvent;
    @NotNull long timestamp;
    @NotBlank String configUpdaterName;
    String configUpdaterId;
    String configUpdaterEmail;

    @Override
    public int hashCode() {
        return this.getSchemaEvent().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final var thatKey = (SchemaHistoryItem) obj;
        return (thatKey.getSchemaEvent().equals(this.schemaEvent));
    }
}
