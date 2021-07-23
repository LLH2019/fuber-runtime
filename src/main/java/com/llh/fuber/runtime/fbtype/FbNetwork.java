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

import java.util.HashMap;
import java.util.Map;

public class FbNetwork extends NamedObject {

    private Resource resource;
    private Map<String, FbInstance> fbInstances = new HashMap<String, FbInstance>();

    public FbNetwork(Resource res) {
        resource = res;
    }

    public void addFbInstance(String instName, String typeName) {
        if (resource.getFbType(typeName) != null) {
            fbInstances.put(instName, resource.getFbType(typeName).
                    createInstance(instName));
        } else {
            throw new IllegalArgumentException(instName + ": no FB type: " + typeName + " in resource");
        }
    }

    public FbInstance getFbInstance(String name) {
        if (fbInstances.get(name) == null) {
            throw new IllegalArgumentException("No FB instance: " + name);
        }
        return fbInstances.get(name);
    }

    public void addEventConnection(String source, String dest) {

        String fromInstance = getInstanceName(source);
        String fromSignal = getSignalName(source);
        String toInstance = getInstanceName(dest);
        String toSignal = getSignalName(dest);

        if (fromInstance.equals("")) {
            Logger.output(Logger.ERROR, "FbNetwork.addEventConnection(): from instance is empty in connection from " + source + " to " + dest);
        } else if (toInstance.equals("")) {
            Logger.output(Logger.ERROR, "FbNetwork.addEventConnection(): to instance is empty in connection from " + source + " to " + dest);
        } else {
            Logger.output(Logger.DEBUG1, "FbNetwork.addEventConnection: From " + source + " to " + dest);
            Connection newConn = new Connection(getFbInstance(toInstance), toSignal);
            getFbInstance(fromInstance).addEventOutputConnection(fromSignal, newConn);

        }

    }

    public void addDataConnection(String source, String dest) {

        String fromInstance = getInstanceName(source);
        String fromSignal = getSignalName(source);
        String toInstance = getInstanceName(dest);
        String toSignal = getSignalName(dest);

        if (fromInstance.equals("")) {
            // Constant specification
            Logger.output(Logger.DEBUG1, "FbNetwork.addDataConnection(): setting data input " + dest + " to " + source);
            getFbInstance(toInstance).setVariableValue(toSignal, fromSignal);
        } else if (toInstance.equals("")) {
            Logger.output(Logger.ERROR, "FbNetwork.addDataConnection(): to instance is empty in connection from " + source + " to " + dest);
        } else {
            Logger.output(Logger.DEBUG, "FbNetwork.addDataConnection: From " + source + " to " + dest);
            Connection newConn = new Connection(getFbInstance(fromInstance), fromSignal);
            getFbInstance(toInstance).addDataInputConnection(toSignal, newConn);
        }

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
