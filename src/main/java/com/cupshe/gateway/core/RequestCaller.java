package com.cupshe.gateway.core;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.exception.NotFoundException;
import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * RequestCaller
 * <p>Maintain service list and provide load balancing service
 *
 * @author zxy
 */
@Data
@Component
public class RequestCaller {

    private Map<String, Boolean> callStatus;

    private Map<String, AbstractLoadBalancer> callRouters;

    public RequestCaller(RestGatewayProperties properties) {
        initial(properties.getRouters());
    }

    public void initial(@NonNull List<Router> routers) {
        final int capacity = routers.size() << 1;
        callStatus = Maps.newHashMapWithExpectedSize(capacity);
        callRouters = Maps.newHashMapWithExpectedSize(capacity);
        routers.forEach(t -> {
            callStatus.put(t.getPrefix(), Boolean.TRUE);
            callRouters.put(t.getPrefix(), Router.LoadBalance.R.equals(t.getLbType())
                    ? new RandomLoadBalancer(t.getServices())
                    : new RoundRobinLoadBalancer(t.getServices()));
        });
    }

    public HostStatus next(@NonNull String prefix) {
        Assert.notNull(prefix, "'prefix' cannot be null.");
        for (Map.Entry<String, AbstractLoadBalancer> me : callRouters.entrySet()) {
            if (prefix.startsWith(me.getKey())) {
                return me.getValue().next();
            }
        }

        throw new NotFoundException();
    }

    private static abstract class AbstractLoadBalancer {

        /**
         * init data-list
         *
         * @param services List of String
         * @return List of HostStatus
         */
        protected List<HostStatus> initial(List<String> services) {
            return services
                    .parallelStream()
                    .map(t -> new HostStatus(t, true))
                    .collect(Collectors.toList());
        }

        /**
         * Get next service of service-list
         *
         * @return next remote host
         */
        @NonNull
        abstract HostStatus next();
    }

    /**
     * round-robin (RR)
     */
    private static class RoundRobinLoadBalancer extends AbstractLoadBalancer {

        private final AtomicInteger i = new AtomicInteger(-1);

        private final List<HostStatus> services;

        private RoundRobinLoadBalancer(List<String> services) {
            this.services = initial(services);
        }

        @Override
        public HostStatus next() {
            if (services.isEmpty()) {
                return HostStatus.NON_SUPPORT;
            }

            int curr, next, size = services.size();

            while (true) {
                do {
                    next = curr = i.get();
                    next = ++next >= size ? 0 : next;
                } while (!i.compareAndSet(curr, next));

                HostStatus result = services.get(next);
                if (result.isStatus()) {
                    return result;
                }
            }
        }
    }

    /**
     * random (R)
     */
    private static class RandomLoadBalancer extends AbstractLoadBalancer {

        private final List<HostStatus> services;

        private RandomLoadBalancer(List<String> services) {
            this.services = initial(services);
        }

        @Override
        public HostStatus next() {
            if (services.isEmpty()) {
                return HostStatus.NON_SUPPORT;
            }

            int next, size = services.size();

            while (true) {
                next = ThreadLocalRandom.current().nextInt(0, size);
                HostStatus result = services.get(next);
                if (result.isStatus()) {
                    return result;
                }
            }
        }
    }
}
