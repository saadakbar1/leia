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

package com.grookage.leia.core.ingestion.utils;

import com.grookage.leia.core.ingestion.VersionIDGenerator;
import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaKey;
import com.grookage.leia.models.schema.engine.SchemaState;
import com.grookage.leia.models.schema.ingestion.CreateSchemaRequest;
import com.grookage.leia.models.storage.StoredSchema;
import com.grookage.leia.models.storage.StoredSchemaMeta;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SchemaUtils {

    public StoredSchema toStoredSchema(final CreateSchemaRequest createSchemaRequest,
                                       final String userName,
                                       final String email,
                                       final VersionIDGenerator versionIDGenerator) {
        return StoredSchema.builder()
                .schemaName(createSchemaRequest.getSchemaName())
                .namespace(createSchemaRequest.getNamespace())
                .versionId(versionIDGenerator.generateVersionId("V"))
                .schemaState(SchemaState.CREATED)
                .schemaType(createSchemaRequest.getSchemaType())
                .description(createSchemaRequest.getDescription())
                .attributes(createSchemaRequest.getAttributes())
                .schemaMeta(StoredSchemaMeta.builder()
                        .createdBy(userName)
                        .createdByEmail(email)
                        .createdAt(System.currentTimeMillis())
                        .build())
                .validationType(createSchemaRequest.getValidationType())
                .build();
    }

    public SchemaDetails toSchemaDetails(final StoredSchema storedSchema) {
        return SchemaDetails.builder()
                .schemaState(storedSchema.getSchemaState())
                .description(storedSchema.getDescription())
                .attributes(storedSchema.getAttributes())
                .schemaKey(SchemaKey.builder()
                        .namespace(storedSchema.getNamespace())
                        .schemaName(storedSchema.getSchemaName())
                        .version(storedSchema.getVersionId())
                        .build())
                .schemaType(storedSchema.getSchemaType())
                .validationType(storedSchema.getValidationType())
                .build();
    }
}
