package com.grookage.leia.models.qualifiers.annotations;

import com.grookage.leia.models.qualifiers.QualifierType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {
    QualifierType type();

    int ttlSeconds() default -1; // Applicable for ShortLivedQualifier
}
