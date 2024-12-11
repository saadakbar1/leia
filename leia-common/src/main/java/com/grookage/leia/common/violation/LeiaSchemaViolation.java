package com.grookage.leia.common.violation;

public interface LeiaSchemaViolation {

    /**
     * @return Error message for the violation
     */
    String message();

    /**
     * @return Relative path of the field being validated from the {@code rootKlass}
     */
    String fieldPath();

    /**
     * @return Class of the field being validated
     */
    Class<?> rootKlass();
}
