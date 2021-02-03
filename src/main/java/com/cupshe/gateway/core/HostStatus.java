package com.cupshe.gateway.core;

import com.cupshe.gateway.constant.Headers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

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
     * unsupported
     */
    public static final HostStatus NON_SUPPORT = new HostStatus(Headers.Values.UNKNOWN, false);

    /**
     * service name(remote host)
     */
    private final String host;

    /**
     * support
     */
    private boolean status;

    /***
     * is unsupported host-status
     *
     * @param hostStatus HostStatus
     * @return boolean
     */
    public static boolean isNotSupport(HostStatus hostStatus) {
        return Objects.isNull(hostStatus) || hostStatus == NON_SUPPORT || !hostStatus.isStatus();
    }
}
