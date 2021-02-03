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

    private Map<String, LoadBalancer> callRouters;

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
        for (Map.Entry<String, LoadBalancer> me : callRouters.entrySet()) {
            if (prefix.startsWith(me.getKey())) {
                return me.getValue().next();
            }
        }

        throw new NotFoundException();
    }

    private interface LoadBalancer {

        /**
         * init data-list
         *
         * @param services List of String
         * @return List of HostStatus
         */
        default List<HostStatus> init(List<String> services) {
            return services
                    .stream()
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
                    .stream()
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
    }

    /**
     * round-robin (RR)
     */
    private static class RoundRobinLoadBalancer implements LoadBalancer {

        private final AtomicInteger i = new AtomicInteger(0);

        private final List<HostStatus> services;

        private RoundRobinLoadBalancer(List<String> services) {
            this.services = init(services);
        }

        @Override
        public HostStatus next() {
            List<HostStatus> list = aliveList(services);
            if (list.isEmpty()) {
                return HostStatus.NON_SUPPORT;
            }

            int curr, next, size = list.size() - 1;

            do {
                curr = i.get();
                next = curr >= size ? 0 : curr + 1;
            } while (!i.compareAndSet(curr, next));

            return list.get(curr);
        }
    }

    /**
     * random (R)
     */
    private static class RandomLoadBalancer implements LoadBalancer {

        private final List<HostStatus> services;

        private RandomLoadBalancer(List<String> services) {
            this.services = init(services);
        }

        @Override
        public HostStatus next() {
            List<HostStatus> list = aliveList(services);
            if (list.isEmpty()) {
                return HostStatus.NON_SUPPORT;
            }

            int curr = ThreadLocalRandom.current().nextInt(0, list.size());
            return list.get(curr);
        }
    }
}
