package com.cupshe.gateway.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * InetIpUtilsTests
 *
 * @author zxy
 */
public class InetIpUtilsTests {

    private final Funnel<CharSequence> f = Funnels.stringFunnel(StandardCharsets.UTF_8);

    private final BloomFilter<String> filter = BloomFilter.create(f, 1 << 27, 0.01);

    @Test
    public void test() {
        List<String> ips = InetIpUtils.rangeIps("127.0.0.0/99");
        ips.forEach(filter::put);

        System.out.println(filter.mightContain("127.0.0.0"));
        System.out.println(filter.mightContain("127.0.0.1"));
        System.out.println(filter.mightContain("127.0.0.99"));
        System.out.println(filter.mightContain("127.0.1.0"));
    }
}
