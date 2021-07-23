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
package com.llh.fuber.runtime;

import com.llh.fuber.runtime.fbtype.*;
import com.llh.fuber.runtime.instance.ServiceFbInstance;
import com.llh.fuber.runtime.scheduler.Scheduler;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Resource extends NamedObject {

    private Scheduler scheduler;
    private Map<String, FbType> fbTypes = new HashMap<String, FbType>();
    private Map<String, FbNetwork> fbNetworks = new HashMap<String, FbNetwork>();

    public Resource(String name) {
        Logger.output(Logger.DEBUG, "Resource(" + name + ")");
        setName(name);
    }

    public void run() {
        Logger.output(Logger.DEBUG, "Resource(" + getName() + ").run()");
        scheduler.run();
        // close down service FBs
        for (var fbType : fbTypes.values()) {
            if (fbType instanceof ServiceFbType) {
                for (var fbInstance : fbType.getInstances()) {
                    ((ServiceFbInstance) fbInstance).finalizeService();
                }
            }
        }
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void addFbNetwork(String name) {
        fbNetworks.put(name, new FbNetwork(this));
    }

    public FbNetwork getFbNetwork(String name) {
        return fbNetworks.get(name);
    }

    public void addBasicFbType(String name) {
        fbTypes.put(name, new BasicFbType(name, this));
    }

    public void addCompositeFbType(String name) {
        fbTypes.put(name, new CompositeFbType(name, this));
    }

    public void addServiceFbType(String name, File serviceScript) {
        fbTypes.put(name, new ServiceFbType(name, this, serviceScript));
    }

    public FbType getFbType(String name) {
        return fbTypes.get(name);
    }

    public Collection<FbType> getFbTypes() {
        return fbTypes.values();
    }
}
