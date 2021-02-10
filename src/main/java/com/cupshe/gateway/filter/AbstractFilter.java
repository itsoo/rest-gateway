package com.cupshe.gateway.filter;

import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;

/**
 * AbstractFilter
 *
 * @author zxy
 */
public abstract class AbstractFilter {

    /**
     * has next
     *
     * @return boolean
     */
    public boolean hasNext() {
        return Objects.nonNull(next());
    }

    /**
     * next filter
     *
     * @return AbstractFilter
     */
    public abstract AbstractFilter next();

    /**
     * filter (main)
     *
     * @param exchange ServerWebExchange
     */
    public abstract void filter(ServerWebExchange exchange);

    /**
     * filter (overload)
     *
     * @param exchange ServerWebExchange
     * @return true:continue / false:end
     */
    public boolean doFilter(ServerWebExchange exchange) {
        return true;
    }

    /**
     * ignore
     *
     * @return boolean
     */
    public boolean ignore() {
        return false;
    }
}
