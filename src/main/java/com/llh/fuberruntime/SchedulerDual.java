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

public final class SchedulerDual extends SchedulerSeq {

    private List<Job> scheduledJobs = Collections.synchronizedList(new LinkedList<Job>());
    private List<AlgorithmExecutingThread> algorithmThreads =
            new LinkedList<AlgorithmExecutingThread>();

    public SchedulerDual(Resource resource, int numEventThreads, int numAlgorithmThreads) {
        super(resource, numEventThreads);

        for (int i = 1; i <= numAlgorithmThreads; i++) {
            algorithmThreads.add(new AlgorithmExecutingThread("algorithm" + i, this));
        }

        setName(SchedulerDual.class.getSimpleName() + "(" + resource.getName() + ")");
        setLogTag(getName());

        Logger.output(Logger.DEBUG, getLogTag() + ": " + "Event Threads: " + numEventThreads
                + ": Algorithm Threads: " + numAlgorithmThreads);
    }

    public synchronized void run() {
        for (AlgorithmExecutingThread thread : algorithmThreads) {
            thread.start();
        }

        super.run();

        // wait for all algorithm threads to finish before exiting
        boolean terminated = true;
        do {
            stop();
            try {
                wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            terminated = true;
            for (AlgorithmExecutingThread thread : algorithmThreads) {
                terminated = terminated && thread.getState().equals(Thread.State.TERMINATED);
            }
        } while (!terminated);
    }


    public synchronized Job getNextScheduledJob() {
        while (scheduledJobs.isEmpty()) {
            try {
                wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (doStop) {
                return null;
            }
        }
        return scheduledJobs.remove(0);
    }

    @Override
    public synchronized void scheduleJob(Job j) {
        scheduledJobs.add(j);
        notifyAll();
    }
}
