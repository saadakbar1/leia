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

package com.grookage.leia.client;

import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.SchemaKey;
import com.jayway.jsonpath.JsonPath;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@UtilityClass
@Slf4j
public class MessageTransformerUtils {

    public static Map<SchemaKey, Map<String, JsonPath>> getCompiledPaths(List<SchemaDetails> schemas,
                                                                         Predicate<SchemaKey> schemaPredicate) {
        final var compiledPaths = new HashMap<SchemaKey, Map<String, JsonPath>>();
        schemas.forEach(schemaDetails -> {
            if (!schemaPredicate.test(schemaDetails.getSchemaKey())) {
                return;
            }
            final var transformationTargets = schemaDetails.getTransformationTargets();
            transformationTargets.forEach(transformationTarget -> {
                final var valid = schemaPredicate.test(transformationTarget.getSchemaKey());
                if (!valid) {
                    log.error("The transformationSchema schema doesn't seem to be valid for schemaKey {}. Please check the schema bindings provided",
                            transformationTarget.getSchemaKey());
                    throw new IllegalStateException("Invalid transformation schema");
                }
                final var paths = new HashMap<String, JsonPath>();
                transformationTarget.getTransformers().forEach(transformer -> paths.put(transformer.getAttributeName(),
                        JsonPath.compile(transformer.getTransformationPath())));
                compiledPaths.put(transformationTarget.getSchemaKey(), paths);
            });
        });
        return compiledPaths;
    }
}
