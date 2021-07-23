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

public class Device extends NamedObject {

private Map<String, Resource> resources = new HashMap<String, Resource>();

public Device(String name) {
    setName(name);
    setLogTag("Device(" + getName() + ")");
    Logger.output(Logger.DEBUG, getLogTag());
}

public void run() {
    Logger.output(Logger.DEBUG, getLogTag() + ".run()");
    for (var resource : resources.values()) {
        resource.run();
    }
}

public void addResource(String name, Resource res) {
    resources.put(name, res);
}

public Resource getResource(String name) {
    return resources.get(name);
}
}
