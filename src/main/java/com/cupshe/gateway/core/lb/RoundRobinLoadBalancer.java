package com.cupshe.gateway.core.lb;

import com.cupshe.gateway.core.HostStatus;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round-Robin (RR)
 *
 * @author zxy
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private final AtomicInteger i = new AtomicInteger(-1);

    private final List<HostStatus> services;

    public RoundRobinLoadBalancer(@NonNull List<String> services) {
        this.services = initial(services);
    }

    @Override
    public HostStatus next() {
        int curr, next, size = services.size();

        for (int j = 0; j < size; j++) {
            do {
                next = curr = i.get();
                next = ++next >= size ? 0 : next;
            } while (!i.compareAndSet(curr, next));

            HostStatus result = services.get(next);
            if (result.isStatus()) {
                return result;
            }
        }

        return HostStatus.NON_SUPPORT;
    }

    @Override
    public List<HostStatus> getAll() {
        return services;
    }
}
