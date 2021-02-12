package com.cupshe.gateway.listener;

import com.cupshe.ak.net.UuidUtils;
import com.cupshe.gateway.constant.Symbols;
import com.cupshe.gateway.core.HostStatus;
import com.cupshe.gateway.core.RequestCaller;
import com.cupshe.gateway.core.lb.LoadBalancer;
import com.cupshe.gateway.exception.TimeoutException;
import com.cupshe.gateway.filter.FilterContext;
import com.cupshe.gateway.filter.Filters;
import com.cupshe.gateway.util.Attributes;
import com.cupshe.gateway.util.RequestProcessor;
import com.cupshe.gateway.util.ResponseProcessor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.util.Map;
import java.util.Objects;

/**
 * InitLearnServiceListener
 *
 * @author zxy
 */
//@Component
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

        setFilterContext();

        for (LoadBalancer lb : routerMap.values()) {
            for (HostStatus hostStatus : lb.getAll()) {
                resetHostStatus(hostStatus);
            }
        }
    }

    private void setFilterContext() {
        FilterContext.setAttributes(Attributes.attributesBuilder()
                .setId(UuidUtils.createUuid())
                .setMethod(HttpMethod.GET)
                .setContentType(MediaType.ALL)
                .build());
    }

    private void resetHostStatus(HostStatus hostStatus) {
        try {
            String url = RequestProcessor.getRequestUrlOf(hostStatus, Symbols.EMPTY);
            ClientResponse remoteResp = filters.requestAndResponse(url).block();
            ResponseProcessor.cleanup(remoteResp);
            hostStatus.setStatus(true);
        } catch (TimeoutException e) {
            hostStatus.setStatus(false);
        } catch (Exception ignore) {}
    }
}
