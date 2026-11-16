package com.darmoz.tramiteaadmin.exception;

import org.springframework.http.HttpStatus;

/** Error con status y un codigo estable en `error` (`category-in-use`, `not-admin`...). */
public class ApiException extends RuntimeException {

    public static final String INVALID_CREDENTIALS = "invalid-credentials";
    public static final String NOT_ADMIN = "not-admin";
    public static final String EMAIL_NOT_VERIFIED = "email-not-verified";
    public static final String AUTH_UNAVAILABLE = "auth-unavailable";
    public static final String INVALID_SESSION = "invalid-session";
    public static final String INVALID_PAYLOAD = "invalid-payload";
    public static final String DUPLICATED = "duplicated";
    public static final String IN_USE = "in-use";
    public static final String NO_LOCATION = "no-location";
    public static final String LOCATION_NOT_ALLOWED = "location-not-allowed";
    public static final String ALREADY_REVIEWED = "already-reviewed";

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, INVALID_PAYLOAD, message);
    }

    public static ApiException conflict(String code, String message) {
        return new ApiException(HttpStatus.CONFLICT, code, message);
    }

    public static ApiException forbidden(String code, String message) {
        return new ApiException(HttpStatus.FORBIDDEN, code, message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
