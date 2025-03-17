package com.grookage.leia.models.annotations;

import com.grookage.leia.models.SchemaConstants;
import com.grookage.leia.models.schema.SchemaType;
import com.grookage.leia.models.schema.SchemaValidationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaDefinition {
    String name();

    String namespace();

    String version() default SchemaConstants.LATEST_VERSION;

    String description() default "";

    SchemaType type() default SchemaType.JSON;

    SchemaValidationType validation() default SchemaValidationType.MATCHING;

    Class<?>[] transformationTargets() default {};
}
