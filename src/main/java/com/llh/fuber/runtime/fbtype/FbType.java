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
package com.llh.fuber.runtime.fbtype;

import com.llh.fuber.runtime.*;
import com.llh.fuber.runtime.instance.FbInstance;
import com.llh.fuber.runtime.variable.Variable;
import com.llh.fuber.runtime.variable.VariableRole;
import com.llh.fuber.runtime.variable.Variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class FbType extends NamedObject {

// resource to which this type belongs
public Resource resource;
// representing the events and their with data
private Map<String, Event> events = new HashMap<String, Event>();
// all instances of this type
private Map<String, FbInstance> instances = new HashMap<String, FbInstance>();
// variables in an FB type
public Variables variables = new Variables();

// add a variable of the name to the type
public void addVariable(String name, Variable var) {
    variables.addVariable(name, var);

    // if event var add it to events
    if (var.getRole().equals(VariableRole.EVENT_INPUT)
            || var.getRole().equals(VariableRole.EVENT_OUTPUT)) {
        events.put(name, new Event(name));
    }

    // update instances
    for (var instance : instances.values()) {
        instance.addVariable(name, var);
    }
}

public void addDataAssociation(String event, String dataVar) {
    events.get(event).addWithData(dataVar);
}

public void addInstance(FbInstance fbInst) {
    Logger.output(Logger.DEBUG1,
                  "FbType(" + getName() + ").addInstance(" +
                  fbInst.getName() + ")");

    instances.put(fbInst.getName(), fbInst);
}

public abstract FbInstance createInstance(String name);

public Event getEvent(String name) {
    return events.get(name);
}

public Collection<FbInstance> getInstances() {
    return instances.values();
}
}
