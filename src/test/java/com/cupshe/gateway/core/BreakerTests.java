package com.cupshe.gateway.core;

import org.junit.Test;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

        System.out.println("pull 0 :" + task.poll());
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 1 :" + task.poll());
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 2 :" + task.poll());
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 3 :" + task.poll());
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("pull 4 :" + task.poll());
    }

    @Test
    public void test03() {
        Queue<Object> queue = new ConcurrentLinkedQueue<>();
        queue.add("1");
        queue.add(10L);
        queue.add("A");

        for (Object o : queue) {}

        System.out.println(queue.size());

        Iterator<Object> it2 = queue.iterator();
        while (it2.hasNext()) {
            it2.next();
            it2.remove();
        }

        System.out.println(queue.size());
    }
}
