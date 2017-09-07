package com.netflix.turbine.discovery;
import com.netflix.turbine.init.TurbineInit;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MyRestart extends Thread {
    @Override
    public void run() {
        Date time1=new Date();
        System.out.println("turbine.stop() start["+time1+"]");
        TurbineInit.stop();
        Date time2=new Date();
        System.out.println("turbine.stop() finished-_-["+time2+"]");
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Date time3=new Date();
        System.out.println("turbine.init() start["+time3+"]");
        TurbineInit.init();
        Date time4=new Date();
        System.out.println("turbine.init() finished["+time4+"]");
    }
}
