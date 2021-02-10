package com.cupshe.gateway.constant;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Authorization
 *
 * @author zxy
 */
public interface Auth {

    Set<String> HEADERS = ImmutableSet.of("token"); // add your headers

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
