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

public class CompositeFbType extends FbType {

// map instance names to type names
private Map<String, String> fbInstanceNames = new HashMap<String, String>();
// map from event cnt spec to to event cnt spec
private Map<String, String> eventConnectionSpecs = new HashMap<String, String>();
private Map<String, String> dataConnectionSpecs = new HashMap<String, String>();

public CompositeFbType(String n, Resource r) {
    Logger.output(Logger.DEBUG, "CompositeFbType(" + n + "," + r.getName() + ")");
    setName(n);
    resource = r;
}
//====================================================================================

public FbInstance createInstance(String name) {
    Logger.output(Logger.DEBUG, "CompositeFbType.createInstance(" + name + ")");
    CompositeFbInstance newInstance = new CompositeFbInstance(name, resource, this);

    // first instantiate all internal instances
    for (var instance : fbInstanceNames.keySet()) {
        String type = fbInstanceNames.get(instance);
        Logger.output(Logger.DEBUG, "\tInternal FB: " + type + " " + instance);
        newInstance.addFbInstance(instance, type);
    }
    // then make event connections
    for (var from : eventConnectionSpecs.keySet()) {
        String to = eventConnectionSpecs.get(from);

        String fromInstance = getInstanceName(from);
        String fromSignal = getSignalName(from);
        String toInstance = getInstanceName(to);
        String toSignal = getSignalName(to);
        Logger.output(Logger.DEBUG, "\tEvent connection: "
                + fromInstance + "." + fromSignal + "-->"
                + toInstance + "." + toSignal);

        if (fromInstance.equals("")) {
            // internal event input connection
            newInstance.addInternalEventInputConnection(fromSignal, toInstance, toSignal);
        } else if (toInstance.equals("")) {
            // internal event output connection
            newInstance.addInternalEventOutputConnection(fromInstance, fromSignal, toSignal);
        } else {
            // internal instance event connection
            newInstance.addEventConnection(fromInstance, fromSignal, toInstance, toSignal);
        }

    }
    // finally make data connections
    for (var from : dataConnectionSpecs.keySet()) {
        String to = dataConnectionSpecs.get(from);

        //Turn hash key from:to to only from
        from = from.substring(0, from.indexOf(':'));

        String fromInstance = getInstanceName(from);
        String fromSignal = getSignalName(from);
        String toInstance = getInstanceName(to);
        String toSignal = getSignalName(to);
        Logger.output(Logger.DEBUG, "\tData connection: "
                + fromInstance + "." + fromSignal + "-->"
                + toInstance + "." + toSignal);

        if (fromInstance.equals("")) {
            // constant data value may be specified for an internal fb data input
            // try to set the internal fb instance variable to that value
            try {
                Logger.output(Logger.DEBUG,"\t\tTrying to set variable "
                        + to + " to " + fromSignal);
                newInstance.getFbInstance(toInstance).setVariableValue(toSignal, fromSignal);
            } catch (Exception e) {
                // if the variable setting fails then this data connection is internal data input
                // connection
                Logger.output(Logger.DEBUG,"\t\tFailed to set variable");
                Logger.output(Logger.DEBUG,"\t\tSetting internal data input connection from "
                        + fromSignal + " to " + to);
                newInstance.addInternalDataInputConnection(fromSignal, toInstance, toSignal);
            }
        } else if (toInstance.equals("")) {
            // internal data output connection
            newInstance.addInternalDataOutputConnection(fromInstance, fromSignal, toSignal);
        } else {
            // internal instance data connection
            newInstance.addDataConnection(fromInstance, fromSignal, toInstance, toSignal);
        }

    }

    newInstance.setVariables(variables.clone());

    addInstance(newInstance);

    return newInstance;
}

public void addFbInstance(String instName, String typeName) {
    fbInstanceNames.put(instName, typeName);
}

public void addEventConnection(String from, String to) {
    eventConnectionSpecs.put(from, to);
}

public void addDataConnection(String from, String to) {
    // Hash key "from:to" needed for; one data out to multiple data in
    dataConnectionSpecs.put(from + ":" + to, to);
}

private String getInstanceName(String cntSpec) {
    if (cntSpec.indexOf(".") < 0) {
        return "";
    }
    return cntSpec.substring(0, cntSpec.indexOf("."));
}

private String getSignalName(String cntSpec) {
    if (cntSpec.indexOf(".") < 0) {
        return cntSpec;
    }

    return cntSpec.substring(cntSpec.indexOf(".") + 1, cntSpec.length());
}
}
