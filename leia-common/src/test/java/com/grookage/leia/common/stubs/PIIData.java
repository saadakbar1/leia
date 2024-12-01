package com.grookage.leia.common.stubs;

import com.grookage.leia.models.annotations.attribute.qualifiers.Encrypted;
import com.grookage.leia.models.annotations.attribute.qualifiers.PII;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@PII
@Data
@Builder
@Jacksonized
public class PIIData {
    String name;
    @Encrypted
    String accountNumber;
}
