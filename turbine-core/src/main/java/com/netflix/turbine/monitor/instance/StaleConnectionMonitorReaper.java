/**
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.turbine.monitor.instance;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.turbine.data.DataFromSingleInstance;
import com.netflix.turbine.monitor.MonitorConsole;
import com.netflix.turbine.monitor.TurbineDataMonitor;

public class StaleConnectionMonitorReaper {

    private final static Logger logger = LoggerFactory.getLogger(StaleConnectionMonitorReaper.class);

    private final DynamicIntProperty StalenessThreshold = DynamicPropertyFactory.getInstance().getIntProperty("turbine.StaleConnectionMonitorReaper.StalenessThreshold", 30000);
    private final DynamicIntProperty RunFrequencyMillis = DynamicPropertyFactory.getInstance().getIntProperty("turbine.StaleConnectionMonitorReaper.RunFrequencyMillis", 20000);
    private final DynamicBooleanProperty ReaperEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("turbine.StaleConnectionMonitorReaper.Enabled", false);

    private final AtomicReference<List<MonitorConsole<DataFromSingleInstance>>> monitorConsoles 
        = new AtomicReference<List<MonitorConsole<DataFromSingleInstance>>>(new ArrayList<MonitorConsole<DataFromSingleInstance>>());
    
    private final Timer timer = new Timer();
           
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    
    public static StaleConnectionMonitorReaper Instance = new StaleConnectionMonitorReaper();
    
    private StaleConnectionMonitorReaper() {
    }
    
    public void addMonitorConsole(MonitorConsole<DataFromSingleInstance> console) {
        List<MonitorConsole<DataFromSingleInstance>> newList = 
                new ArrayList<MonitorConsole<DataFromSingleInstance>>(monitorConsoles.get());
        newList.add(console);
        monitorConsoles.set(newList);
    }

    public void removeMonitorConsole(MonitorConsole<DataFromSingleInstance> consoleToBeRemoved) {
        List<MonitorConsole<DataFromSingleInstance>> newList = 
                new ArrayList<MonitorConsole<DataFromSingleInstance>>(monitorConsoles.get());
        newList.remove(consoleToBeRemoved);
        Date  dateTmp = new Date();
        System.out.print("lianjie-"+dateTmp+"-"+newList.toString());
        monitorConsoles.set(newList);
    }
    
    public void start() {
        if (started.get()) {
            return; // already started 
        }
        if (stopped.get()) {
            return; // already stopped
        }
        boolean success = started.compareAndSet(false, true);
        if (!success) {
            return; // someone else beat me to starting this. 
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkStaleMonitors();
            }
        }, 60000, RunFrequencyMillis.get());
    }
    
    public void stop() {
        stopped.set(true);
        timer.cancel();
    }
    
    private void checkStaleMonitors() {
        
        if (stopped.get()) {
            return;
        }
        
        if (!ReaperEnabled.get()) {
            return;
        }
        
        logger.info("Checking for stale connections");
        
        for (MonitorConsole<DataFromSingleInstance> console : monitorConsoles.get()) {
            
            Collection<TurbineDataMonitor<DataFromSingleInstance>> monitors = console.getAllMonitors();
            for (TurbineDataMonitor<DataFromSingleInstance> instanceMonitor : monitors) {
                
                long lastEventUpdate = instanceMonitor.getLastEventUpdateTime();
                if (lastEventUpdate == -1) {
                    continue;  // this instance monitor has opted out, move onto the next one
                }
                
                // check for statelness
                long staleness = System.currentTimeMillis() - lastEventUpdate;
                if (staleness > StalenessThreshold.get()) {
                    logger.info("Terminating InstanceMonitor due to staleness threshold breach, " + instanceMonitor.getName());
                    try {
                        instanceMonitor.stopMonitor();
                    } catch (Exception e) {
                        logger.error("Could not terminate InstanceMonitor due to staleness threshold breach, " + instanceMonitor.getName(), e);
                    }
                }
            }
        }
    }
}
