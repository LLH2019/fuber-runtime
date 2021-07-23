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
package com.llh.fuber.runtime.variable;

import com.llh.fuber.runtime.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Variables {

private Map<String, Variable> variables = Collections.synchronizedMap(new HashMap<String, Variable>());

public void addVariable(String name, Variable var) {
    Logger.output(Logger.DEBUG1, "Variables.addVariable(" + name + ", " + var.toString() + ")");
    Variable existingVar = variables.get(name);
    if (existingVar != null && !var.getClass().equals(existingVar.getClass())) {
        throw new IllegalArgumentException(
                "New variable is not of the same value type as existing variable. "
                        + "Not replacing existing variable.");
    }
    variables.put(name, var);
}

public void setVariableValue(String name, String value) {
    Variable var = getVariable(name);

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

public Variable getVariable(String name) {
    if (!variables.keySet().contains(name)) {
        throw new IllegalArgumentException("No variable named: " + name);
    }
    return variables.get(name);
}

public boolean contains(String name) {
    if (variables.keySet().contains(name)) {
        return true;
    }
    return false;
}

public Collection<String> getNames() {
    return variables.keySet();
}

public Collection<Variable> getVars() {
    return variables.values();
}

public Variables clone() {
    Logger.output(Logger.DEBUG5, "Variables.clone(" + this.toString() + ")");
    Variables newVars = new Variables();
    for (var name : variables.keySet()) {
        Variable var = getVariable(name);
        newVars.addVariable(name, (Variable) var.clone());
    }
    return newVars;
}

@Override
public String toString() {
    return variables.toString();
}
}
