package com.grookage.leia.common.stubs;

import com.grookage.leia.models.GenericResponse;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.Range;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Jacksonized
public class BiGenericStub<R, U> {
    GenericResponse<R> tGenericResponse;
    U data;
    R key;
    Range<R> tRange;
}
