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

public class BooleanVariable extends Variable {

private Boolean value = Boolean.valueOf(false);

public BooleanVariable(VariableRole role, boolean b) {
    super(role);
    value = Boolean.valueOf(b);
}

public synchronized void setValue(boolean v) {
    value = Boolean.valueOf(v);
}

public synchronized Boolean getValue() {
    return value;

}

public Variable clone() {
    return new BooleanVariable(getRole(), getValue().booleanValue());
}

@Override
public String toString() {
    return "{" + getRole() + ", " + value + "}";
}
}
