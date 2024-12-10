package com.grookage.leia.common.violation;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
public class ViolationContext {
    @Getter
    private final Set<LeiaSchemaViolation> violations = new HashSet<>();
    private final LinkedList<String> path = new LinkedList<>();

    public void addViolation(final String message) {
        String fullPath = String.join(".", path);
        violations.add(new LeiaSchemaViolationImpl(message, fullPath));
    }

    public void pushPath(final String element) {
        path.addLast(element);
    }

    public void popPath() {
        if (!path.isEmpty()) {
            path.removeLast();
        }
    }
}
