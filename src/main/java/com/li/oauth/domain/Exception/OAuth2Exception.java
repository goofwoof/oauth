package com.li.oauth.domain.Exception;

import org.springframework.http.HttpStatus;

public class OAuth2Exception extends RuntimeException {
    private HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    private int errorCode = 200;

    public OAuth2Exception(String msg) {
        super(msg);
    }

    public OAuth2Exception(String msg, Throwable t) {
        super(msg, t);
    }

    public OAuth2Exception(String msg, HttpStatus httpStatus, int errorCode) {
        super(msg);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public OAuth2Exception(String msg, Throwable t, HttpStatus httpStatus, int errorCode) {
        super(msg, t);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
