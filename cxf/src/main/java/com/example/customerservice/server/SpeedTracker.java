package com.example.customerservice.server;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class SpeedTracker {
    private long blockSize = 500;
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
        if (curCount % blockSize == 0) {
            long timediff = System.currentTimeMillis() - oldTime;
            long messagespersec = blockSize * 1000L / timediff;
            oldTime = System.currentTimeMillis();
            Date date = new Date();
            String dateSt = DateFormat.getTimeInstance().format(date);
            if (messagespersec < 1000) {
                System.out.println(dateSt + " - " + curCount + " messages total, "+ messagespersec + " msg/s: " + timediff + " time for " +blockSize+" msgs");
            }
        }
    }
    
    public void showStats() {
        int curCount = count.get();
        long timeDiff = System.currentTimeMillis() - startTime;
        long messagespersec = new Long(curCount) * 1000 / timeDiff;
        System.out.println("Overall measurement: " + curCount + " messages total, "+ messagespersec + " msg/s: ");
    }
}
