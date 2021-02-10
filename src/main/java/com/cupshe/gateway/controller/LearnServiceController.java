package com.cupshe.gateway.controller;

import com.cupshe.gateway.core.RequestCaller;
import com.cupshe.gateway.core.Router;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * LearnServiceController
 *
 * @author zxy
 */
@RestController
@RequestMapping("/rest-gateway/v1")
public class LearnServiceController {

    private final RequestCaller requestCaller;

    public LearnServiceController(RequestCaller requestCaller) {
        this.requestCaller = requestCaller;
    }

    @GetMapping
    public Flux<Router> findServices() {
        System.out.println("进入了 LearnServerController");
        return Flux.fromIterable(requestCaller.getRouters());
    }
}
