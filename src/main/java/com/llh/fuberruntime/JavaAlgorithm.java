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

import bsh.Interpreter;

import java.io.Reader;
import java.io.StringReader;

public class JavaAlgorithm extends Algorithm {

private String algorithmText;

public JavaAlgorithm(String n, String algText) {
    setName(n);
    algorithmText = algText;
}

public void execute(Variables vars) throws Exception {
    Interpreter interpreter = new Interpreter();

    // set variables
    for (var name : vars.getNames()) {
        Variable var = vars.getVariable(name);
        if (var.getRole().equals(VariableRole.LOCAL)
                || var.getRole().equals(VariableRole.DATA_INPUT)
                || var.getRole().equals(VariableRole.DATA_OUTPUT)) {
            if (var instanceof StringVariable) {
                String value = ((StringVariable) var).getValue();
                interpreter.set(name, value);
            } else if (var instanceof IntegerVariable) {
                int value = ((IntegerVariable) var).getValue().intValue();
                interpreter.set(name, value);
            } else if (var instanceof DoubleVariable) {
                double value = ((DoubleVariable) var).getValue().doubleValue();
                interpreter.set(name, value);
            } else if (var instanceof BooleanVariable) {
                boolean value = ((BooleanVariable) var).getValue().booleanValue();
                interpreter.set(name, value);
            }
        }
    }
    // execute algorithm
    Reader algTextReader = new StringReader(algorithmText);
    interpreter.eval(algTextReader);
    // get variables
    for (var name : vars.getNames()) {
        Variable var = vars.getVariable(name);
        if (var.getRole().equals(VariableRole.LOCAL)
                || var.getRole().equals(VariableRole.DATA_OUTPUT)) {
            if (var instanceof StringVariable) {
                ((StringVariable) var).setValue((String) interpreter.get(name));
            } else if (var instanceof IntegerVariable) {
                ((IntegerVariable) var)
                        .setValue(((Integer) interpreter.get(name)).intValue());
            } else if (var instanceof DoubleVariable) {
                ((DoubleVariable) var)
                        .setValue(((Double) interpreter.get(name)).doubleValue());
            } else if (var instanceof BooleanVariable) {
                ((BooleanVariable) var)
                        .setValue(((Boolean) interpreter.get(name)).booleanValue());
            }
        }
    }
}
}
