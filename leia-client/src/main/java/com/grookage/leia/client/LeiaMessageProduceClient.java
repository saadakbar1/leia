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

package com.grookage.leia.client;

import com.grookage.leia.models.mux.LeiaMessage;
import com.grookage.leia.models.mux.LeiaMessages;
import com.grookage.leia.models.schema.SchemaKey;
import com.grookage.leia.models.schema.transformer.TransformationTarget;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@Slf4j
public class LeiaMessageProduceClient extends AbstractSchemaClient {

    private final Map<SchemaKey, Map<String, JsonPath>> compiledPaths = new HashMap<>();

    /*
        Multiplexes from source and generates the list of messages as applicable
        a) Fetches the schemaDetails from schemaKey of source
        b) Checks if there is a transformationTarget, if none, returns source as is.
     */
    @SneakyThrows
    private Optional<LeiaMessage> createMessage(DocumentContext sourceContext,
                                                TransformationTarget transformationTarget) {
        final var registeredKlass = getSchemaValidator()
                .getKlass(transformationTarget.getSchemaKey()).orElse(null);
        if (null == registeredKlass) {
            return Optional.empty();
        }
        final var responseObject = new LinkedHashMap<String, Object>();
        transformationTarget.getTransformers().forEach(transformer -> {
            final var jsonPath = getJsonPath(transformationTarget.getSchemaKey(), transformer.getTransformationPath())
                    .orElse(null);
            if (null != jsonPath) {
                final var attribute = sourceContext.read(jsonPath);
                responseObject.put(transformer.getAttributeName(), attribute);
            }
        });
        getMapper().convertValue(responseObject, registeredKlass); //Do this to do the schema validation of if the conversion is right or not.
        return Optional.of(
                LeiaMessage.builder()
                        .schemaKey(transformationTarget.getSchemaKey())
                        .tags(transformationTarget.getTags())
                        .message(responseObject.toString().getBytes(StandardCharsets.UTF_8))
                        .build()
        );
    }

    private Optional<JsonPath> getJsonPath(SchemaKey schemaKey, String attributeName) {
        return compiledPaths.containsKey(schemaKey) ?
                Optional.ofNullable(compiledPaths.get(schemaKey).get(attributeName)) : Optional.empty();
    }

    @SneakyThrows
    public LeiaMessages getMessages(SchemaKey schemaKey, byte[] sourceMessage) {
        final var sourceSchemaDetails = super.getSchemaDetails()
                .stream().filter(each -> each.getSchemaKey().equals(schemaKey))
                .findFirst().orElse(null);
        final var messages = new LeiaMessages();
        messages.add(
                LeiaMessage.builder()
                        .schemaKey(schemaKey)
                        .message(sourceMessage)
                        .build()
        );
        final var transformationTargets = null == sourceSchemaDetails ? null :
                sourceSchemaDetails.getTransformationTargets();
        if (null == transformationTargets) {
            return messages;
        }
        final var documentContext = JsonPath.parse(new String(sourceMessage));
        transformationTargets.forEach(transformationTarget ->
                createMessage(documentContext, transformationTarget).ifPresent(messages::add));
        return messages;
    }

    @Override
    public void start() {
        super.getSchemaDetails().forEach(schemaDetails -> {
            final var validSchema = super.valid(schemaDetails.getSchemaKey());
            if (!validSchema) {
                log.error("The source schema doesn't seem to be valid for schemaKey {}. Please check the schema bindings provided",
                        schemaDetails.getSchemaKey());
                throw new IllegalStateException("Invalid source schema");
            }
            final var transformationTargets = schemaDetails.getTransformationTargets();
            transformationTargets.forEach(transformationTarget -> {
                final var valid = super.valid(transformationTarget.getSchemaKey());
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
    }
}
