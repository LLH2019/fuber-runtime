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

public final class BasicFbInstanceNpmtr extends BasicFbInstance {

private boolean handlingEvent = false;

public BasicFbInstanceNpmtr(String name, Resource resource, BasicFbType type) {
    super(name, resource, type);
    setLogTag(BasicFbInstanceNpmtr.class.getSimpleName() + "(" + name + ")");
}

public synchronized void receiveEvent(String eventInput) {
    if (fbType.getName().equals("E_STOP")) {
        Logger.output(Logger.INFO, getLogTag() + ": STOP event");
        System.exit(0);
    }

    queueEvent(eventInput);

    if (!handlingEvent) {
        handlingEvent = true;

        Logger.output(Logger.DEBUG2, getLogTag() + ": receive event: " + eventInput);

        handleEvent();

        handlingEvent = false;
    } else {
        Logger.output(Logger.DEBUG1, getLogTag() + ": scheduling this FB instance");
        resource.getScheduler().scheduleFbInstance(this);
    }
}

public void handleEvent() {
    currentEvent = getNextEvent();

    Logger.output(Logger.DEBUG2, getLogTag() + ": handling event: " + currentEvent.getName()
            + ": from Ecc state: " + currentEcState.getName());

    updateVarsForEvent(currentEvent);

    EcState newEcState = updateEcc();

    if (newEcState != null) {
        Logger.output(Logger.DEBUG2, getLogTag() + ": handling new state: " + newEcState.getName());
        handleNewState(newEcState);
    }
}
}