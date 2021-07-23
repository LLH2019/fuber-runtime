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

import java.util.LinkedList;
import java.util.List;

public final class SchedulerCyclic extends Scheduler {

private List<FbInstance> instances = new LinkedList<FbInstance>();

public SchedulerCyclic(Resource resource) {
    this.resource= resource;

    setName(SchedulerCyclic.class.getSimpleName() + "(" + resource.getName() + ")");
    setLogTag(getName());

    Logger.output(Logger.DEBUG, getLogTag());
}

public synchronized void run() {
    Logger.output(Logger.INFO, getLogTag() + ".run()");
    Logger.hLine(Logger.INFO);

    for (var type : resource.getFbTypes()) {
        if (!(type instanceof CompositeFbType)) {
            for (var instance : type.getInstances()) {
                instances.add(instance);    
            }
        }
    }

    send_restart_event();

    while (true) {
        for (var instance : instances) {
            instance.handleEvent();
            if (doStop) {
                Logger.output(Logger.INFO, getLogTag() + ": STOP event");
                return;
            }
        }
    }
}

public FbInstance getNextScheduledFbInstance() {
    return null;
}

public void scheduleFbInstance(FbInstance fbInst) {
}
}
