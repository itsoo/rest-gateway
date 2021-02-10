package com.cupshe.gateway.constant;

import com.cupshe.ak.text.StringUtils;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Headers
 *
 * @author zxy
 */
public interface Headers {

    String ORIGIN_IP = "originIp";

    String X_ORIGIN_IP = "X-Origin-IP";

    String X_FORWARDED_FOR = "X-Forwarded-For";

    String X_CALL_SOURCE = "Call-Source";

    /**
     * values
     */
    interface Values {

        String X_CALL_SOURCE = "REST-GATEWAY";

        String BINARY_ADDRESS = "0:0:0:0:0:0:0:1";

        String LOOP_BACK = "127.0.0.1";

        String UNKNOWN = "unknown";
    }

    /**
     * ignore headers
     */
    interface Ignores {

        Set<String> HEADERS = Sets.newLinkedHashSet();

        static void addAll(Set<String> filterHeaders) {
            HEADERS.addAll(filterHeaders);
        }

        /**
         * contains key
         *
         * @param key String
         * @return boolean
         */
        static boolean contains(String key) {
            return StringUtils.isBlank(key) || HEADERS.contains(key.toLowerCase());
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
}
