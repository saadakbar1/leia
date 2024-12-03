package com.grookage.leia.common.stubs;

import com.grookage.leia.models.annotations.attribute.Optional;
import com.grookage.leia.models.annotations.attribute.qualifiers.PII;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record RecordStub(@PII String name,
                         @Optional int id) {
}
