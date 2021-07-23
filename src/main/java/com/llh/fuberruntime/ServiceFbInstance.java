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

import bsh.Interpreter;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class ServiceFbInstance extends FbInstance {

private File serviceScript;
private Object serviceState;
private Interpreter interpreter = new Interpreter();
private EventQueue eventInputQueue = new EventQueue();

public ServiceFbInstance(String name, Resource resource, ServiceFbType fbType, File script) {
    setName(name);
    setLogTag(ServiceFbInstance.class.getSimpleName() + "(" + name + ")");
    this.resource = resource;
    this.fbType = fbType;
    serviceScript = script;
}

public void initializeService() {
    try {
        interpreter.set("serviceInitialize", true);
        interpreter.set("serviceFinalize", false);
        interpreter.set("serviceFb", this);
        interpreter.set("serviceState", null);
        interpreter.set("serviceEvent", null);
        interpreter.set("serviceVariables", variables);

        // evaluate the serviceScript
        Reader serviceScriptReader = new FileReader(serviceScript);
        interpreter.eval(serviceScriptReader);

        serviceState = interpreter.get("serviceState");
        interpreter.set("serviceInitialize", false);
    } catch (Exception e) {
        throw new IllegalArgumentException("Unable to evaluate service bsh script: "
                + e.getMessage());
    }
}

public void finalizeService() {
    try {
        interpreter.set("serviceFinalize", true);
        interpreter.set("serviceState", serviceState);

        // evaluate the serviceScript
        Reader serviceScriptReader = new FileReader(serviceScript);
        interpreter.eval(serviceScriptReader);
    } catch (Exception e) {
        throw new IllegalArgumentException("Unable to evaluate service bsh script: "
                + e.getMessage());
    }
}

public synchronized void receiveEvent(String eventInput) {
    if (!variables.getVariable(eventInput).getRole().equals(VariableRole.EVENT_INPUT)) {
        throw new IllegalArgumentException("No event input " + eventInput);
    }

    Logger.output(Logger.DEBUG, getLogTag() + ".receiveEvent(" + eventInput + ")");

    Event newEvent = fbType.getEvent(eventInput).clone();
    getDataInputs(newEvent);
    eventInputQueue.add(newEvent);

    resource.getScheduler().scheduleFbInstance(this);
}

public synchronized void handleEvent() {
    Logger.output(Logger.DEBUG, getLogTag() + ".handleEvent()");

    if (eventInputQueue.size() > 0) {
        currentEvent = getNextEvent();

        Logger.output(Logger.DEBUG, getLogTag() + ".handleEvent(): handling event "
                + currentEvent.getName());

        updateVarsForEvent(currentEvent);

        try {
            interpreter.set("serviceFb", this);
            interpreter.set("serviceState", serviceState);
            interpreter.set("serviceEvent", currentEvent);
            interpreter.set("serviceVariables", variables);

            // evaluate the serviceScript
            Reader serviceScriptReader = new FileReader(serviceScript);
            interpreter.eval(serviceScriptReader);

            serviceState = interpreter.get("serviceState");
            variables = (Variables) interpreter.get("serviceVariables");
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to evaluate service bsh script: "
                    + e.getMessage());
    }
    }
}

public Object getServiceState() {
    return serviceState;
}

private Event getNextEvent() {
    return eventInputQueue.remove();
}
}
