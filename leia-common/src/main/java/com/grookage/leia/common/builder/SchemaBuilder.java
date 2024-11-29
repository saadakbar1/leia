package com.grookage.leia.common.builder;

import com.grookage.leia.common.utils.SchemaAttributeUtils;
import com.grookage.leia.models.annotations.SchemaDefinition;
import com.grookage.leia.models.schema.ingestion.CreateSchemaRequest;
import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.Optional;

@UtilityClass
public class SchemaBuilder {
    public Optional<CreateSchemaRequest> buildSchemaDetails(final Class<?> klass) {
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
