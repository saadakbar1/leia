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

package com.grookage.leia.repository;

import com.grookage.leia.models.schema.SchemaKey;
import com.grookage.leia.models.storage.StoredSchema;

import java.util.List;
import java.util.Set;

public interface SchemaRepository {

    List<StoredSchema> getAllSchemas();

    StoredSchema add(final StoredSchema schema);

    StoredSchema update(final StoredSchema schema);

    StoredSchema remove(final StoredSchema schema);

    List<StoredSchema> get(final Set<SchemaKey> schemaKeys);

    List<StoredSchema> get(final String namespace, final String schemaName);

    StoredSchema get(final SchemaKey schemaKey);
}
