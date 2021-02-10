package com.cupshe.gateway.core.lb;

import com.cupshe.gateway.core.HostStatus;

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

    public RoundRobinLoadBalancer(List<String> services) {
        this.services = initial(services);
    }

    @Override
    public HostStatus next() {
        final List<HostStatus> list = aliveList(services);
        if (list.isEmpty()) {
            return HostStatus.NON_SUPPORT;
        }

        int curr, next, size = list.size();

        do {
            next = curr = i.get();
            next = ++next >= size ? 0 : next;
        } while (!i.compareAndSet(curr, next));

        return list.get(next);
    }

    @Override
    public List<HostStatus> getAll() {
        return services;
    }
}
