package com.grookage.leia.common.stubs.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String fabricatorEndPoint;
    private String backendServiceUrl;
    private String userAgent;
    private String referrer;
    private String method;
}
