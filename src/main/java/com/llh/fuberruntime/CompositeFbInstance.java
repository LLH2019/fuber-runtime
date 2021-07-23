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

import java.util.HashMap;
import java.util.Map;

public class CompositeFbInstance extends FbInstance {

// internal instances
private Map<String, FbInstance> fbInstances = new HashMap<String, FbInstance>();
// map type event input to connection containing internal instance and its event input
private Map<String, Connection> internalEventInputConnections = new HashMap<String, Connection>();
// map type event output to connection containing internal instance and its event output
private Map<String, Connection> internalEventOutputConnections = new HashMap<String, Connection>();
// the same for internal data connection
private Map<String, Connection> internalDataInputConnections = new HashMap<String, Connection>();
private Map<String, Connection> internalDataOutputConnections = new HashMap<String, Connection>();

public CompositeFbInstance(String n, Resource r, CompositeFbType t) {
    setName(n);
    resource = r;
    fbType = t;
}
//================================================================

//=================================================================================================
// following methods are used during the creation of the instance
public void addFbInstance(String instName, String typeName) {
    fbInstances.put(instName, resource.getFbType(typeName).createInstance(getName() + "." + instName));
}

public FbInstance getFbInstance(String instName) {
    return fbInstances.get(instName);
}

public void addInternalEventInputConnection(String fromEvent, String toInstance, String toEvent) {
    Connection newConnection = new Connection(getFbInstance(toInstance), toEvent);
    internalEventInputConnections.put(fromEvent, newConnection);
}

public void addInternalEventOutputConnection(String fromInstance, String fromEvent, String toEvent) {
    Connection newConnection = new Connection(getFbInstance(fromInstance), fromEvent);
    internalEventOutputConnections.put(toEvent, newConnection);
}

public void addInternalDataInputConnection(String fromData, String toInstance, String toData) {
    Connection newConnection = new Connection(getFbInstance(toInstance), toData);
    internalDataInputConnections.put(fromData, newConnection);
}

public void addInternalDataOutputConnection(String fromInstance, String fromData, String toData) {
    Connection newConnection = new Connection(getFbInstance(fromInstance), fromData);
    internalDataOutputConnections.put(toData, newConnection);
}

public void addEventConnection(String fromInstance, String fromEvent, String toInstance, String toEvent) {
    Connection newConnection = new Connection(getFbInstance(toInstance), toEvent);
    getFbInstance(fromInstance).addEventOutputConnection(fromEvent, newConnection);
}

public void addDataConnection(String fromInstance, String fromData, String toInstance, String toData) {
    //Logger.output(Logger.DEBUG,fromInstance+"."+fromData+":"+toInstance+"."+toData);
    Connection newConnection = new Connection(getFbInstance(fromInstance), fromData);
    getFbInstance(toInstance).addDataInputConnection(toData, newConnection);
}

//==================================================================================================
// following methods are used after the instance has been created
@Override
public void addEventOutputConnection(String output, Connection cnt) {
    Connection internalCnt = internalEventOutputConnections.get(output);
    internalCnt.getFbInstance().addEventOutputConnection(internalCnt.
            getSignalName(), cnt);
}

@Override
public void addDataInputConnection(String input, Connection cnt) {
    Connection internalCnt = internalDataInputConnections.get(input);
    internalCnt.getFbInstance().addDataInputConnection(internalCnt.
            getSignalName(), cnt);
}

public void receiveEvent(String eventInput) {
    Connection internalCnt = internalEventInputConnections.get(eventInput);
    internalCnt.getFbInstance().receiveEvent(internalCnt.getSignalName());
}

@Override
public Variable getDataOutput(String dataOutput) {
    Connection internalCnt = internalDataOutputConnections.get(dataOutput);
    return internalCnt.getFbInstance().getDataOutput(internalCnt.
            getSignalName());
}

@Override
public void setVariableValue(String name, String value) {
    if (!variables.contains(name)) {
        throw new IllegalArgumentException("No variable name: " + name);
    }

    Connection internalCnt = internalDataInputConnections.get(name);
    internalCnt.getFbInstance().setVariableValue(internalCnt.getSignalName(), value);
}

public void handleEvent() {
    // Doesn't do anything. Just here to satisfy the abstraction of FBInstance.
}
}
