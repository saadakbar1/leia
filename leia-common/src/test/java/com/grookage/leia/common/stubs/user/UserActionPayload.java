package com.grookage.leia.common.stubs.user;

import com.grookage.leia.models.annotations.SchemaDefinition;
import com.grookage.leia.models.schema.SchemaValidationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SchemaDefinition(
        orgId = "lending",
        namespace = "test",
        tenantId = "default",
        type = "default",
        version = "v1",
        validation = SchemaValidationType.STRICT,
        name = "UserActionPayload"
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserActionPayload {
    private User user;
    private Request request;
    private Response response;
    private String actionMessage;
}
