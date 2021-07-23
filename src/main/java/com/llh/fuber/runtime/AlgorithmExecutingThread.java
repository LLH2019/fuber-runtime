/*
 *   This file is part of Fuber (Function Block Execution Runtime) library.
 *   Copyright (C) 2006-2021 Goran Cengic
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *   To contact author please refer to contact information in the README file.
 */
package com.llh.fuber.runtime;

import com.llh.fuber.runtime.instance.BasicFbInstanceDual;
import com.llh.fuber.runtime.scheduler.SchedulerDual;

public class AlgorithmExecutingThread extends Thread {

private SchedulerDual scheduler = null;

public AlgorithmExecutingThread(String name, SchedulerDual s) {
    scheduler = s;
    setName(name);
}

@Override
public void run() {
    while (true) {
        Job job = scheduler.getNextScheduledJob();
        if (job == null) {
            return;
        }

        Logger.output(Logger.DEBUG, "Executing " + job.getAlgorithm().getName());

        BasicFbInstanceDual fbInstance = (BasicFbInstanceDual) job.getInstance();
        fbInstance.eventTime = (System.nanoTime() - fbInstance.eventStartTime) / 1000000;
        fbInstance.algorithmTime = System.nanoTime();

        scheduler.executeJob(job);

        fbInstance.algorithmTime = (System.nanoTime() - fbInstance.algorithmTime) / 1000000;
        fbInstance.finishTime = (System.nanoTime() - SchedulerDual.startTime) / 1000000;
        fbInstance.totalTime = fbInstance.eventTime + fbInstance.algorithmTime;

        BasicFbInstanceDual.allTime = BasicFbInstanceDual.allTime + fbInstance.totalTime;
        BasicFbInstanceDual.allEventTime = BasicFbInstanceDual.allEventTime + fbInstance.eventTime;
        BasicFbInstanceDual.allAlgorithmTime = BasicFbInstanceDual.allAlgorithmTime + fbInstance.algorithmTime;
        BasicFbInstanceDual.count++;

        Logger.output(Logger.DEBUG5, "Block times for instance " + fbInstance.getName() + " :");
        Logger.output(Logger.DEBUG5, "\t t_CON = " + fbInstance.eventTime + " ms");
        Logger.output(Logger.DEBUG5, "\t t_ALG = " + fbInstance.algorithmTime + " ms");
        Logger.output(Logger.DEBUG5, "\t t_TOT = " + fbInstance.totalTime + " ms");
        Logger.output(Logger.DEBUG5, "\t t_FIN = " + fbInstance.finishTime + " ms");
    }
}
}
