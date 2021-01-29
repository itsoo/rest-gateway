package com.cupshe.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * TimeoutException
 *
 * @author zxy
 */
@Component
public class TimeoutException extends RuntimeException implements GatewayException {

    /**
     * 504
     */
    public static final HttpStatus HS = HttpStatus.GATEWAY_TIMEOUT;

    public static final String MESSAGE = HS.value() + " " + HS.getReasonPhrase();

    public TimeoutException() {
        super(MESSAGE);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HS;
    }
}
