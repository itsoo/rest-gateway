package com.cupshe.gateway.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * HostStatus
 *
 * @author zxy
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class HostStatus {

    /**
     * service name(remote host)
     */
    private final String host;

    /**
     * support
     */
    private Boolean status;
}
