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

public class BasicFbType extends FbType {

// the Ecc of the type
private Ecc theEcc = new Ecc();
// Alogrithms
// map algorithm name to algorithm text
private Map<String, Algorithm> algorithms =
                               new HashMap<String, Algorithm>();
// map algorithm name to algorithm variables map;
//private Map algorithmVariablesMap = new HashMap();

//==========================================================================
// instantiate BasicFbType of name n in resource r
public BasicFbType(String n, Resource r) {
    Logger.output(Logger.DEBUG1, "BasicFbType(" + n +
                                 "," + r.getName() + ")");
    setName(n);
    resource = r;
}

//==========================================================================
public FbInstance createInstance(String name) {
    Logger.output(Logger.DEBUG1, "BasicFbType.createInstance(" + name + ")");

    String execModel = Runtime.getProperties().getProperty("execModel");
    BasicFbInstance newInstance = null;
    if (execModel.toLowerCase().equals("cycl")) {
        newInstance =
        new BasicFbInstanceCyclic(name, resource, this);
    } else if (execModel.toLowerCase().equals("dual")) {
        newInstance =
        new BasicFbInstanceDual(name, resource, this);
    } else if (execModel.toLowerCase().equals("npmtr")) {
        newInstance =
        new BasicFbInstanceNpmtr(name, resource, this);
    } else if (execModel.toLowerCase().equals("seqb")) {
        newInstance =
        new BasicFbInstanceSeqBlock(name, resource, this);
    } else if (execModel.toLowerCase().equals("seqe")) {
        newInstance =
        new BasicFbInstanceSeqEvent(name, resource, this);
    }
    addInstance(newInstance);
    return newInstance;
}

public Ecc getEcc() {
    return theEcc;
}

public void addAlgorithm(Algorithm alg) {
    algorithms.put(alg.getName(), alg);
}

public Algorithm getAlgorithm(String name) {
    return algorithms.get(name);
}
}
