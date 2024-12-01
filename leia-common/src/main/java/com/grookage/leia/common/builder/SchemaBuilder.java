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

package com.grookage.leia.common.builder;

import com.grookage.leia.common.utils.SchemaAttributeUtils;
import com.grookage.leia.models.annotations.SchemaDefinition;
import com.grookage.leia.models.schema.ingestion.CreateSchemaRequest;
import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.Optional;

@UtilityClass
public class SchemaBuilder {
    public Optional<CreateSchemaRequest> buildSchemaRequest(final Class<?> klass) {
        if (Objects.isNull(klass) || !klass.isAnnotationPresent(SchemaDefinition.class)) {
            return Optional.empty();
        }
        final var schemaDefinition = klass.getAnnotation(SchemaDefinition.class);
        return Optional.of(CreateSchemaRequest.builder()
                .schemaName(schemaDefinition.name())
                .namespace(schemaDefinition.namespace())
                .description(schemaDefinition.description())
                .schemaType(schemaDefinition.type())
                .validationType(schemaDefinition.validation())
                .attributes(SchemaAttributeUtils.getSchemaAttributes(klass))
                .build()
        );
    }
}
