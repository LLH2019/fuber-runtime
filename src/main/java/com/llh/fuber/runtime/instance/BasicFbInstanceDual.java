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
package com.llh.fuber.runtime.instance;

import com.llh.fuber.runtime.Logger;
import com.llh.fuber.runtime.Resource;
import com.llh.fuber.runtime.fbtype.BasicFbType;
import com.llh.fuber.runtime.fbtype.EcState;

public final class BasicFbInstanceDual extends BasicFbInstance {

// profiling fields
public long eventStartTime;
public long eventTime;
public long algorithmTime;
public long totalTime;
public long finishTime;
public static long allTime;
public static long allEventTime;
public static long allAlgorithmTime;
public static int count;

private boolean queuedInScheduler = false;

public BasicFbInstanceDual(String n, Resource r, BasicFbType t) {
    super(n, r, t);
    setLogTag(BasicFbInstanceDual.class.getSimpleName() + "(" + n + ")");
}

public synchronized void receiveEvent(String eventInput) {
    eventStartTime = System.nanoTime();

    queueEvent(eventInput);

    Logger.output(Logger.DEBUG2, getLogTag() + ": receive event: " + eventInput);

    if (!queuedInScheduler) {
        Logger.output(Logger.DEBUG1, getLogTag() + ": scheduling this FB instance");
        resource.getScheduler().scheduleFbInstance(this);
        queuedInScheduler = true;
    }
}

public synchronized void handleEvent() {
    queuedInScheduler = false;

    // step through the event queue until there's change in Ecc state
    EcState newEcState = null;
    do {
        currentEvent = getNextEvent();

        Logger.output(Logger.DEBUG2, getLogTag() + "handling event: " + currentEvent.getName()
                + ": from Ecc state: " + currentEcState.getName());

        updateVarsForEvent(currentEvent);

        newEcState = updateEcc();

    } while (newEcState == null && eventInputQueue.size() > 0);

    // new Ecc state is entered
    Logger.output(Logger.DEBUG2, getLogTag() + ": handling new state: " + newEcState.getName());
    handleNewState(newEcState);
}

// handles currentEcState between actions
@Override
public void handleState() {
    if (actionsIterator.hasNext()) {
        handleAction(actionsIterator.next());
    } else {
        // repeat the handling of the state if state is changed
        Logger.output(Logger.DEBUG2, getLogTag() + ": no more actions in state: "
                + currentEcState.getName());
        EcState newEcState = updateEcc();
        if (newEcState != null) {
            handleNewState(newEcState);
        } else {
            Logger.output(Logger.DEBUG2, getLogTag() + ": done with event :"
                    + currentEvent.getName() + ": and in EcState: " + currentEcState.getName());
            // if there are more events left schedule the block again
            if (!queuedInScheduler && eventInputQueue.size() > 0) {
                Logger.output(Logger.DEBUG1, getLogTag() + ": scheduling this FB instance.");
                resource.getScheduler().scheduleFbInstance(this);
                queuedInScheduler = true;
            }
        }
    }
}
}