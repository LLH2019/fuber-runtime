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

import java.io.File;

public class ServiceFbType extends FbType {

private File serviceScript;

public ServiceFbType(String n, Resource r, File s) {
    Logger.output(Logger.DEBUG, "ServiceFbType(" + n + "," + r.getName() + ")");
    setName(n);
    resource = r;
    serviceScript = s;
}
//====================================================================================

public FbInstance createInstance(String name) {
    Logger.output(Logger.DEBUG, "ServiceFbType.createInstace(" + name + ")");
    ServiceFbInstance newInstance = new ServiceFbInstance(name, resource, this, serviceScript);

    newInstance.setVariables(variables.clone());
    newInstance.initializeService();

    addInstance(newInstance);

    return newInstance;
}
}
