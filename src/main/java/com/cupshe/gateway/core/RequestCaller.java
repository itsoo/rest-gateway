package com.cupshe.gateway.core;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import com.cupshe.gateway.exception.NotFoundException;
import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;
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
                    .parallelStream()
                    .map(t -> new HostStatus(t, Boolean.TRUE))
                    .collect(Collectors.toList());
        }

        /**
         * list of support services
         *
         * @param services List of HostStatus
         * @return List of HostStatus
         */
        default List<HostStatus> list(List<HostStatus> services) {
            return services
                    .parallelStream()
                    .filter(t -> Boolean.TRUE.equals(t.getStatus()))
                    .collect(Collectors.toList());
        }

        /**
         * Get next service of service-list
         *
         * @return next remote host
         */
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
            List<HostStatus> list = list(services);
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }

            int curr, next;

            do {
                curr = i.get();
                next = curr >= list.size() - 1 ? 0 : curr + 1;
            } while (!i.compareAndSet(curr, next));

            return list.get(curr);
        }
    }

    /**
     * random (R)
     */
    private static class RandomLoadBalancer implements LoadBalancer {

        private final Random i = new Random();

        private final List<HostStatus> services;

        private RandomLoadBalancer(List<String> services) {
            this.services = init(services);
        }

        @Override
        public HostStatus next() {
            List<HostStatus> list = list(services);
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }

            int curr = this.i.nextInt(list.size());
            return list.get(curr);
        }
    }
}
