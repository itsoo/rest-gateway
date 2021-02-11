package com.cupshe.gateway.core.lb;

import com.cupshe.gateway.core.HostStatus;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random (R)
 *
 * @author zxy
 */
public class RandomLoadBalancer implements LoadBalancer {

    private final List<HostStatus> services;

    public RandomLoadBalancer(@NonNull List<String> services) {
        this.services = initial(services);
    }

    @Override
    public HostStatus next() {
        int next, size = services.size();

        for (int j = 0; j < size; j++) {
            next = ThreadLocalRandom.current().nextInt(0, size);
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
