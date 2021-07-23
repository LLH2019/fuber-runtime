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
package com.llh.fuberruntime;

public abstract class Scheduler extends NamedObject {

public Resource resource;
public boolean doStop = false;

public void send_restart_event() {
    // find all E_RESTART COLD connections and queue events on them
    for (var restartInstance : resource.getFbType("E_RESTART").getInstances()) {
        restartInstance.sendEvent("COLD");
    }
}

public abstract void run();

public synchronized void stop() {
    Logger.output(Logger.DEBUG, getLogTag() + ".stop()");
    doStop = true;
    notifyAll();
}

public abstract FbInstance getNextScheduledFbInstance();

public abstract void scheduleFbInstance(FbInstance fbInst);

public void scheduleJob(Job job) {
    executeJob(job);
}

public void executeJob(Job job) {
    try {
        job.getAlgorithm().execute(job.getVariables());
    } catch (Exception e) {
        Logger.output(Logger.FATAL, job.getInstance().getName() + ": cannot evaluate algorithm: "
                + job.getAlgorithm().getName() + System.getProperty("line.separator") + "\t"
                + e.getMessage());
        stop();
    }
    ((BasicFbInstance) job.getInstance()).finishedJob(job);
}
}
