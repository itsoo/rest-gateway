package com.cupshe.gateway.handler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * InitLearnServers
 *
 * @author zxy
 */
@Component
public class InitLearnServers {

    @Scheduled(fixedDelay = 1_000L)
    public void schedule() {

    }
}
