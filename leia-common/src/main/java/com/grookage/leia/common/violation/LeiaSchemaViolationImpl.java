package com.grookage.leia.common.violation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeiaSchemaViolationImpl implements LeiaSchemaViolation {
    private String message;
    private String fieldPath;

    @Override
    public String message() {
        return message;
    }

    @Override
    public String fieldPath() {
        return fieldPath;
    }

    @Override
    public String toString() {
        return String.format("[Violation] %s: %s", fieldPath, message);
    }
}
