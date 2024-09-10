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

package com.grookage.leia.models.schema;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grookage.leia.models.attributes.SchemaAttribute;
import com.grookage.leia.models.schema.engine.SchemaState;
import com.grookage.leia.models.storage.StoredSchemaMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@AllArgsConstructor
@Data
@Builder
public class SchemaDetails {
    @NotNull SchemaKey schemaKey;
    String description;
    @NotNull SchemaState schemaState;
    SchemaType schemaType;
    SchemaValidationType validationType;
    @NotNull StoredSchemaMeta schemaMeta;
    @NotEmpty Set<SchemaAttribute> attributes;

    @JsonIgnore
    public boolean match(final SchemaKey thatKey) {
        return schemaKey.equals(thatKey);
    }

    public boolean hasAttribute(final String name) {
        return attributes != null && attributes.stream().anyMatch(each -> each.getName().equalsIgnoreCase(name));
    }
}
