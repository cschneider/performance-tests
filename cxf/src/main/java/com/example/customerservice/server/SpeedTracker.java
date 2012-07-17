package com.example.customerservice.server;

import java.util.concurrent.atomic.AtomicInteger;

public class SpeedTracker {
    private long startTime;
    private long oldTime;
    private AtomicInteger count = new AtomicInteger();
    
    public SpeedTracker() {
        reset();
    }
    
    public void reset() {
        oldTime = System.currentTimeMillis();
        startTime = oldTime;
        count.set(0);
    }
    
    public void count() {
        int curCount = count.addAndGet(1);
        if (curCount % 100 == 0) {
            long messagespersec = 100 * 1000 / (System.currentTimeMillis() - oldTime);
            oldTime = System.currentTimeMillis();
            System.out.println(curCount + " messages total, "+ messagespersec + " msg/s: ");
        }
    }
    
    public void showStats() {
        int curCount = count.get();
        long messagespersec = curCount * 1000 / (System.currentTimeMillis() - startTime);
        System.out.println("Overall measurement: " + curCount + " messages total, "+ messagespersec + " msg/s: ");
    }
}
