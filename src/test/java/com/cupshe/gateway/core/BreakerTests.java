package com.cupshe.gateway.core;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * BreakerTests
 *
 * @author zxy
 */
public class BreakerTests {

    @Test
    public synchronized void test01() throws InterruptedException {
        TimerTask<HostStatus> task = new TimerTask<>(60, t -> t.setStatus(true));
        System.out.println(task);
        wait();
    }

    @Test
    public synchronized void test02() throws InterruptedException {
        TimerTask<HostStatus> task = new TimerTask<>(60, t -> {
//            System.out.println("start: " + t);
            t.setStatus(false);
//            System.out.println("  end: " + t);
        });

        TimeUnit.SECONDS.sleep(3L);

        task.push(new HostStatus("127.0.0.1", true), 3);

        System.out.println("pull 0 :" + task);
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 1 :" + task);
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 2 :" + task);
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 3 :" + task);
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 4 :" + task);
    }
}
