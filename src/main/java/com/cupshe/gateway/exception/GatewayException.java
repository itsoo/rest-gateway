package com.cupshe.gateway.exception;

import org.springframework.http.HttpStatus;

/**
 * GatewayException
 *
 * @author zxy
 */
public interface GatewayException {

    /**
     * HttpStatus
     *
     * @return HttpStatus
     * @see HttpStatus
     */
    HttpStatus getHttpStatus();

    /**
     * Error message
     *
     * @return String
     */
    String getMessage();
}
