package com.cupshe.gateway.util;

import com.cupshe.ak.text.StringUtils;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 * InetIpUtils
 *
 * @author zxy
 */
public class InetIpUtils {

    private static final int DOT_COUNT = 3;

    private static final int FOR_SP_LEN = 2;

    public static List<String> filters(List<String> ipTables) {
        return ipTables
                .parallelStream()
                .filter(InetIpUtils::isValid)
                .collect(Collectors.toList());
    }

    public static boolean isValid(String ip) {
        return StringUtils.isNotBlank(ip)
                && StringUtils.findSubstringCountOf(ip, ".") == DOT_COUNT;
    }

    public static List<String> rangeIps(String ip) {
        List<String> result = Lists.newLinkedList();
        String[] s = StringUtils.split(ip, "/");
        if (s.length == FOR_SP_LEN) {
            int i = s[0].lastIndexOf('.');
            String ipPrefix = s[0].substring(0, i);
            String r1 = StringUtils.trimLeadingCharacter(s[0].substring(i), '.');
            String r2 = s[1];
            for (int j = Integer.parseInt(r1), k = Integer.parseInt(r2); j <= k; j++) {
                result.add(ipPrefix + '.' + j);
            }
        } else {
            result.add(ip);
        }

        return result;
    }

    public static long toLong(String ip) {
        String[] s = ip.split("\\.");
        return ((Long.parseLong(s[0]) << 24) |
                (Long.parseLong(s[1]) << 16) |
                (Long.parseLong(s[2]) << 8) |
                (Long.parseLong(s[3]))) & 0x0ffffffffL;
    }
}
