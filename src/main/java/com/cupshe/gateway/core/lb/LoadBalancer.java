package com.cupshe.gateway.core.lb;

import com.cupshe.gateway.core.HostStatus;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LoadBalancer
 *
 * @author zxy
 */
public interface LoadBalancer {

    /**
     * init data-list
     *
     * @param services List of String
     * @return List of HostStatus
     */
    default List<HostStatus> initial(List<String> services) {
        return services
                .parallelStream()
                .map(t -> new HostStatus(t, true))
                .collect(Collectors.toList());
    }

    /**
     * list of support services
     *
     * @param services List of HostStatus
     * @return List of HostStatus
     */
    default List<HostStatus> aliveList(List<HostStatus> services) {
        return services
                .parallelStream()
                .filter(HostStatus::isStatus)
                .collect(Collectors.toList());
    }

    /**
     * Get next service of service-list
     *
     * @return next remote host
     */
    @NonNull
    HostStatus next();

    /**
     * all services
     *
     * @return List of HostStatus
     */
    @NonNull
    List<HostStatus> getAll();
}
