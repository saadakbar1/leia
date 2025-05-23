package com.grookage.leia.common.stubs.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userId;
    private String userType;
    private String userEmail;
    private String organisationId;
    private String clientId;
}
