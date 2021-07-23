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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class Event extends NamedObject {

// maps with data name to buffered variable
private Map<String, Variable> withData = new HashMap<String, Variable>();

public Event(String n) {
    setName(n);
}

public void addWithData(String dataVarName) {
    withData.put(dataVarName, null);
}

public void addWithDataVariable(String name, Variable dataVar) {
    withData.put(name, dataVar);
}

public Variable getWithDataVariable(String name) {
    return withData.get(name);
}

public Collection<String> getWithDataNames() {
    return withData.keySet();
}

public Event clone() {
    Event newEvent = new Event(getName());
    for (var name : getWithDataNames()) {
        newEvent.addWithData(name);
    }
    return newEvent;
}

@Override
public String toString() {
    return getName();
}
}
