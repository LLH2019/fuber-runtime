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

import com.llh.fuber.runtime.*;
import com.llh.fuber.runtime.fbtype.*;
import com.llh.fuber.runtime.variable.BooleanVariable;
import com.llh.fuber.runtime.variable.VariableRole;

import java.util.Iterator;

public abstract class BasicFbInstance extends FbInstance {

    public EcState currentEcState;
    public EcAction currentEcAction;
    public Iterator<EcAction> actionsIterator;
    public EventQueue eventInputQueue = new EventQueue();

    int maxEventsInQueue;

    public BasicFbInstance(String name, Resource res, BasicFbType type) {
        setName(name);
        setLogTag("BasicFbInstance(" + name + ")");
        resource = res;
        fbType = type;
        currentEcState = type.getEcc().getInitialState();
        setVariables(type.variables.clone());
    }

    public synchronized void finishedJob(Job job) {
        Logger.output(Logger.DEBUG4, getLogTag() + ": finished jb for algorithm: "
                + job.getAlgorithm().getName());
        setVariables(job.getVariables());
        sendEvent();
    }

    public synchronized void queueEvent(String eventInput) {
        if (!variables.getVariable(eventInput).getRole().equals(VariableRole.EVENT_INPUT)) {
            throw new IllegalArgumentException("No event input: " + eventInput);
        }

        Event newEvent = fbType.getEvent(eventInput).clone();
        getDataInputs(newEvent);
        eventInputQueue.add(newEvent);

        Logger.output(Logger.DEBUG3, getLogTag() + ": event input queue size: "
                + eventInputQueue.size());
        if (maxEventsInQueue < eventInputQueue.size()) {
            maxEventsInQueue = eventInputQueue.size();
            Logger.output(Logger.DEBUG3, getLogTag() + ": max event input queue size: "
                    + maxEventsInQueue);
        }
    }

    public Event getNextEvent() {
        Logger.output(Logger.DEBUG3, getLogTag() + ": get next event from: ");
        Logger.output(Logger.DEBUG3, eventInputQueue.toString(), 1);
        return eventInputQueue.remove();
    }

    // initializes the handling of new state
    public void handleNewState(EcState state) {
        currentEcState = state;

        Logger.output(Logger.DEBUG2, getLogTag() + ": handling new state: " + currentEcState.getName()
                + ": number of actions: " + currentEcState.getNumberOfActions());

        // set event var to false
        ((BooleanVariable) variables.getVariable(currentEvent.getName())).setValue(false);

        // and get the iterator for the actions
        actionsIterator = currentEcState.actionsIterator();

        handleState();
    }

    // handles currentEcState between actions
    public void handleState() {
        if (actionsIterator.hasNext()) {

            handleAction(actionsIterator.next());

        } else {

            // repeat the handling of the state if state is changed
            Logger.output(Logger.DEBUG2, getLogTag() + ":no more actions in state: "
                    + currentEcState.getName());

            EcState newEcState = updateEcc();

            if (newEcState != null) {
                handleNewState(newEcState);
            }
        }
    }

    public void handleAction(EcAction action) {

        currentEcAction = action;

        if (currentEcAction.getAlgorithm() != null) {

            Logger.output(Logger.DEBUG3, getLogTag() + ": scheduling algorithm: "
                    + currentEcAction.getAlgorithm());

            resource.getScheduler()
                    .scheduleJob(new Job(this,
                            ((BasicFbType) fbType).getAlgorithm(currentEcAction.getAlgorithm()),
                            variables.clone()));

        } else if (currentEcAction.getAlgorithm() == null &&
                   currentEcAction.getOutput() != null) {

            sendEvent();

        }
    }

    public synchronized void sendEvent() {

        if (currentEcAction.getOutput() != null) {

            String output = currentEcAction.getOutput();

            Logger.output(Logger.DEBUG2, getLogTag() + ": sending event: " + output);

            Connection outputConnection = eventOutputConnections.get(output);

            if (outputConnection != null) {
                outputConnection.getFbInstance().receiveEvent(outputConnection.getSignalName());
            }
        }
        handleState();
    }

    public EcState updateEcc() {
        EcState newState = null;
        try {
            newState = ((BasicFbType) fbType).getEcc().execute(currentEcState, variables);
        } catch (Exception e) {
            throw new IllegalArgumentException(getName() + ": cannot execute ECC: " + e.getMessage());
        }
        return newState;
    }
}