/*
 * This file is part of Fuber (Function Block Execution Runtime) library.
 * Copyright (C) 2006-2021 Goran Cengic
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 * To contact author please refer to contact information in the README file.
 */
package com.llh.fuberruntime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class SchedulerSeq extends Scheduler {

    private List<FbInstance> scheduledFbInstances =
            Collections.synchronizedList(new LinkedList<FbInstance>());
    private List<EventExecutingThread> eventThreads = new LinkedList<EventExecutingThread>();

    private int maxScheduledInstances;
    static long startTime;

    public SchedulerSeq(Resource resource, int numberOfEventThreads) {
        this.resource = resource;
        for (int i = 1; i <= numberOfEventThreads; i++) {
            eventThreads.add(new EventExecutingThread("EventThread" + i, this));
        }
    }

    public synchronized void run() {
        Logger.output(Logger.INFO, getLogTag() + ".run()");
        Logger.hLine(Logger.INFO);

        for (EventExecutingThread thread : eventThreads) {
            thread.start();
        }

        startTime = System.nanoTime();

        send_restart_event();


        while (!doStop) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // wait for all event threads to finish before exiting
        boolean terminated = true;
        do {
            stop();
            try {
                wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            terminated = true;
            for (EventExecutingThread thread : eventThreads) {
                terminated = terminated && thread.getState().equals(Thread.State.TERMINATED);
            }
        } while (!terminated);

        // Logger.output(Logger.DEBUG, "Average time per FB = "
        // + (BasicFbInstance.allTime / BasicFbInstance.count) + " ms");
        // Logger.output(Logger.DEBUG, "Average event time per FB = "
        // + (BasicFbInstance.allEventTime / BasicFbInstance.count) + " ms");

    }

    public synchronized FbInstance getNextScheduledFbInstance() {
        while (scheduledFbInstances.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        FbInstance curFbInstance = scheduledFbInstances.remove(0);

        if (curFbInstance.fbType.getName().equals("E_STOP")) {
            Logger.output(Logger.INFO, getLogTag() + ": STOP event");
            stop();
            return null;
        }

        return curFbInstance;
    }

    public synchronized void scheduleFbInstance(FbInstance fbInst) {
        if (fbInst instanceof ServiceFbInstance) {
            scheduledFbInstances.add(0, fbInst);
        } else {
            scheduledFbInstances.add(fbInst);
        }

        if (maxScheduledInstances < scheduledFbInstances.size()) {
            maxScheduledInstances = scheduledFbInstances.size();
        }

        notifyAll();
    }
}
