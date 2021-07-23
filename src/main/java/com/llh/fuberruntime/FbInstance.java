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

public abstract class FbInstance extends NamedObject {

// resource to which this instance belongs
public Resource resource;
// type of this instance
public FbType fbType;
public Map<String, Connection> eventOutputConnections =
                               new HashMap<String, Connection>();
public Map<String, Connection> dataInputConnections =
                               new HashMap<String, Connection>();
// instance's variables
public Variables variables;
public Event currentEvent;

public void setVariables(Variables vars) {
    variables = vars;
}

public void addVariable(String name, Variable var) {
    variables.addVariable(name, var);
}

public void addEventOutputConnection(String output, Connection cnt) {
    if (eventOutputConnections.get(output) == null) {
        eventOutputConnections.put(output, cnt);
    } else {
        Logger.output(Logger.DEBUG, "FBInstance(" + getName() +
                                    ").addEventOutputConnection(): Warning: Event output " +
                                    output + " is already connected!");
    }
}

public void addDataInputConnection(String input, Connection cnt) {
    if (dataInputConnections.get(input) == null) {
        dataInputConnections.put(input, cnt);
    } else {
        Logger.output(Logger.DEBUG, "FBInstance(" + getName() +
                                    ").addDataInputConnection(): Warning: Data input " +
                                    input + " is already connected!");
    }
}

// This method provides output data to the calling BasicFbInstance
public Variable getDataOutput(String dataOutput) {
    if (!variables.getVariable(dataOutput).getRole().equals(VariableRole.DATA_OUTPUT)) {
        throw new IllegalArgumentException("No such DataOutput: " + dataOutput);
    }
    return variables.getVariable(dataOutput);
}

// Get the data variables associated with this event and put them in
// variables attribute
public void getDataInputs(Event event) {
    for (var name : event.getWithDataNames()) {
        Connection connection = dataInputConnections.get(name);
        if (connection != null) {
            Variable var = connection.getFbInstance().getDataOutput(connection.getSignalName())
                    .clone();
            var.setRole(VariableRole.DATA_INPUT);
            event.addWithDataVariable(name, var);
        }
    }
}

public void setVariableValue(String name, String value) {
    Variable var = variables.getVariable(name);

    if (var instanceof StringVariable) {
        ((StringVariable) var).setValue(value);
    } else if (var instanceof IntegerVariable) {
        ((IntegerVariable) var).setValue(Integer.valueOf(value));
    } else if (var instanceof DoubleVariable) {
        ((DoubleVariable) var).setValue(Double.valueOf(value));
    } else if (var instanceof BooleanVariable) {
        ((BooleanVariable) var).setValue(Boolean.valueOf(value));
    }
}

public abstract void receiveEvent(String eventInput);

public void updateVarsForEvent(Event event) {
    // set all InputEvents to false
    for (var variable : variables.getVars()) {
        if (variable.getRole().equals(VariableRole.EVENT_INPUT)) {
            ((BooleanVariable) variable).setValue(false);
        }
    }

    // set the corresponding event var of the input event to TRUE
    ((BooleanVariable) variables.getVariable(event.getName())).setValue(true);

    // copy data inputs from event buffer to variables
    for (var name : event.getWithDataNames()) {
        Variable var = event.getWithDataVariable(name);
        if (var != null) {
            addVariable(name, var);
        }
    }
}

public abstract void handleEvent();

public synchronized void sendEvent(String eventOutput) {
    Logger.output(Logger.DEBUG, "FbInstance(" + getName() + ").sendEvent(" +
                                eventOutput + ")");

    Connection outputConnection =
               eventOutputConnections.get(eventOutput);

    if (outputConnection != null) {
        FbInstance toInstance = outputConnection.getFbInstance();
        toInstance.receiveEvent(outputConnection.getSignalName());
    }
}
}
