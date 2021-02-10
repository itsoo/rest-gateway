package com.cupshe.gateway.listener;

import com.cupshe.gateway.constant.Symbols;
import com.cupshe.gateway.core.HostStatus;
import com.cupshe.gateway.core.RequestCaller;
import com.cupshe.gateway.core.lb.LoadBalancer;
import com.cupshe.gateway.exception.TimeoutException;
import com.cupshe.gateway.filter.Filters;
import com.cupshe.gateway.util.RequestProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * InitLearnServiceListener
 *
 * @author zxy
 */
@Component
public class InitLearnServiceListener {

    private final RequestCaller caller;

    private final Filters filters;

    public InitLearnServiceListener(RequestCaller caller, Filters filters) {
        this.caller = caller;
        this.filters = filters;
    }

    @Scheduled(fixedDelay = 2_000L)
    public void schedule() {
        Map<String, LoadBalancer> routerMap = caller.getRouterMap();
        if (Objects.isNull(routerMap)) {
            return;
        }

        for (LoadBalancer lb : routerMap.values()) {
            for (HostStatus hostStatus : lb.getAll()) {
                resetHostStatus(hostStatus);
            }
        }
    }

    private void resetHostStatus(HostStatus hostStatus) {
        try {
            String url = RequestProcessor.getRequestUrl(hostStatus, Symbols.EMPTY);
            filters.requestAndResponse(null, url);
            hostStatus.setStatus(true);
        } catch (TimeoutException e) {
            hostStatus.setStatus(false);
        }
    }
}
