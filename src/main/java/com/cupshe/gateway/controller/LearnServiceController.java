package com.cupshe.gateway.controller;

import com.cupshe.gateway.core.RequestCaller;
import com.cupshe.gateway.core.Router;
import com.cupshe.gateway.filter.Filters;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * LearnServiceController
 *
 * @author zxy
 */
@RestController
@RequestMapping("/rest-gateway/v1")
public class LearnServiceController {

    private final RequestCaller caller;

    public LearnServiceController(RequestCaller caller) {
        this.caller = caller;
    }

    @GetMapping
    public Flux<Router> list() {
        return Flux.fromIterable(caller.getRouters());
    }

    @PostMapping("/on/{name}")
    public Mono<String> online(@PathVariable String name) {
        return options(name, true);
    }

    @PostMapping("/off/{name}")
    public Mono<String> offline(@PathVariable String name) {
        return options(name, false);
    }

    private Mono<String> options(String name, boolean status) {
        Router router = Filters.findRouterByName(caller.getRouters(), name);
        if (Objects.nonNull(router)) {
            router.setStatus(status);
            return Mono.just("success");
        }

        return Mono.just("false");
    }
}
