package com.cupshe.gateway.constant;

import com.cupshe.ak.text.StringUtils;
import com.google.common.collect.ImmutableSet;
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

        /**
         * contains key
         *
         * @param key String
         * @return boolean
         */
        static boolean contains(String key) {
            return StringUtils.isBlank(key) || HEADERS.contains(key.toLowerCase());
        }
    }

    /**
     * CORS
     */
    interface Cors {

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
    }

    /**
     * Authorization
     */
    interface Auth {

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
    }
}
