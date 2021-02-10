package com.cupshe.gateway.constant;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Cors
 *
 * @author zxy
 */
public interface Cors {

    Set<String> HEADERS = ImmutableSet.<String>builder()
            .add("access-control-allow-origin")
            .add("access-control-allow-credentials")
            .add("access-control-allow-headers")
            .add("access-control-max-age")
            .build();

    /**
     * contains key
     *
     * @param key String
     * @return boolean
     */
    static boolean contains(String key) {
        return HEADERS.contains(key.toLowerCase());
    }

    /**
     * not contains key
     *
     * @param key String
     * @return boolean
     */
    static boolean nonContains(String key) {
        return !contains(key);
    }
}
