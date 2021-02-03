package com.cupshe.gateway.core;

import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * BreakerTests
 *
 * @author zxy
 */
public class BreakerTests {

    @Test
    public synchronized void test01() throws InterruptedException {
        Breaker.TimerTask<HostStatus> task = new Breaker.TimerTask<>(t -> t.setStatus(true));
        System.out.println(task);
        wait();
    }

    @Test
    public synchronized void test02() throws InterruptedException {
        Breaker.TimerTask<HostStatus> task = new Breaker.TimerTask<>(t -> {
//            System.out.println("start: " + t);
            t.setStatus(false);
//            System.out.println("  end: " + t);
        });

        TimeUnit.SECONDS.sleep(3L);

        task.push(new HostStatus("127.0.0.1", true), 3);

        System.out.println("pull 0 :" + getNext(task.pull()));
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 1 :" + getNext(task.pull()));
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 2 :" + getNext(task.pull()));
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 3 :" + getNext(task.pull()));
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 4 :" + getNext(task.pull()));
    }

    private String getNext(Iterator<?> it) {
        return it.hasNext() ? it.next().toString() : null;
    }
}
