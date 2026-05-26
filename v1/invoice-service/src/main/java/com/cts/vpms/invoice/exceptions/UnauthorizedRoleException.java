package com.cts.vpms.invoice.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedRoleException extends BillingException {
    public static UnauthorizedRoleException forRole(String role, String endpoint) {
        return new UnauthorizedRoleException(
                "Role '" + role + "' is not permitted to access: " + endpoint
        );
    }
    public UnauthorizedRoleException(String message) {
        // 403 Forbidden — authenticated but not authorised
        super(message, HttpStatus.FORBIDDEN, "UNAUTHORIZED_ROLE");
    }
}