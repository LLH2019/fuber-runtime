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

import com.holobloc.xml.libraryelement.*;
import com.llh.fuber.runtime.fbtype.*;
import com.llh.fuber.runtime.scheduler.*;
import com.llh.fuber.runtime.variable.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public final class Loader {
    private Properties properties;
    private Device device;
    private Resource resource;
    private FbNetwork fbNetwork;
    private List<File> libraryPathList = new LinkedList<>();
    private JAXBContext context;
    private Unmarshaller unmarshaller;

    public Loader() {

        properties = Runtime.getProperties();

        String systemFileName = properties.getProperty("systemFileName");
        String libraryPathBase = properties.getProperty("libraryPathBase");
        String libraryPath = properties.getProperty("libraryPath");

        // convert libraryPath string into list of Files
        if (libraryPath == null) {
            if (libraryPathBase == null) {
                libraryPathList = null;
            } else {
                File libraryPathBaseFile = new File(libraryPathBase);

                if (!libraryPathBaseFile.isDirectory()) {
                    Logger.output(Logger.ERROR, "Loader(): Specified library base is not a directory!: " + libraryPathBaseFile.
                            getAbsolutePath());
                } else if (!libraryPathBaseFile.exists()) {
                    Logger.output(Logger.ERROR, "Loader(): Specified library base does not exist!: " + libraryPathBaseFile.
                            getAbsolutePath());
                } else {
                    libraryPathList.add(libraryPathBaseFile);
                }
            }
        } else // libraryPath is specified by the user
        {

            while (true) {
                File curLibraryDir;

                if (libraryPath.indexOf(File.pathSeparatorChar) == -1) {
                    curLibraryDir = new File(libraryPathBase, libraryPath);
                } else {
                    curLibraryDir = new File(libraryPathBase, libraryPath.
                            substring(0, libraryPath.indexOf(File.pathSeparatorChar)));
                }

                if (!curLibraryDir.isDirectory()) {
                    Logger.output(Logger.ERROR, "Loader(): Specified library path element is not a directory!: " + curLibraryDir.
                            getAbsolutePath());
                } else if (!curLibraryDir.exists()) {
                    Logger.output(Logger.ERROR, "Loader(): Specified library path element does not exist!: " + curLibraryDir.
                            getAbsolutePath());
                } else {
                    libraryPathList.add(curLibraryDir);
                }

                if (libraryPath.indexOf(File.pathSeparatorChar) == -1) {
                    break;
                }

                libraryPath = libraryPath.substring(libraryPath.indexOf(File.pathSeparatorChar) + 1);
            }

        }

        // create unmarshaller
        try {
            context = JAXBContext.newInstance("com.holobloc.xml.libraryelement");
            unmarshaller = context.createUnmarshaller();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        load(systemFileName);

    }

    public void load(String fileName) {

        File file = getFile(fileName);

        try {
            Object unmarshalledXmlObject = unmarshaller.unmarshal(file);
            if (unmarshalledXmlObject instanceof JaxbFBType) {
                loadFbType((JaxbFBType) unmarshalledXmlObject);
            } else if (unmarshalledXmlObject instanceof JaxbSystem) {
                Logger.output(Logger.DEBUG, "Loader.load(): Loading system from " + fileName + " file.");
                loadSystem((JaxbSystem) unmarshalledXmlObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Device getDevice() {
        return device;
    }

    // find the fileName in the libraries and return the corresponding File
    private File getFile(String fileName) {
        File file = new File(fileName);

        if (libraryPathList != null) {
            for (var libraryDir : libraryPathList) {
                file = new File(libraryDir, fileName);
                Logger.output(Logger.DEBUG, "Loader.getFile(" + fileName + "): Looking for file in "
                        + file.toString());
                if (file.exists()) {
                    Logger.output(Logger.DEBUG1, "Loader.getFile(" + fileName + "): Got file in "
                            + file.toString());
                    break;
                }
            }
        }

        if (!file.exists()) {
            Logger.output(Logger.FATAL, "Loader.getFile(" + fileName + "): The file \"" + fileName
                    + "\" does not exist in the following specified libraries!:");
            if (libraryPathList != null) {
                for (var libraryDir : libraryPathList) {
                    Logger.output(Logger.FATAL, libraryDir.getAbsolutePath() + File.separator, 1);
                }
            } else {
                Logger.output(Logger.FATAL, ".  (current directory)", 1);
            }
            Logger.output(Logger.FATAL, "Exiting");
            System.exit(1);
        }

        return file;
    }

    private void loadSystem(JaxbSystem xmlSystemData) {

        // Loading with Devices and Resources
        if (xmlSystemData.isSetDevice()) {
            JaxbDevice theDevice = xmlSystemData.getDevice().get(0);
            device = new Device(theDevice.getName());
            if (theDevice.isSetResource()) {
                for (var xmlResource : theDevice.getResource()) {
                    constructResource(xmlResource.getName());
                    if (xmlResource.isSetFBNetwork()) {
                        resource.addFbNetwork("FbNetwork");
                        fbNetwork = resource.getFbNetwork("FbNetwork");
                        constructFbNetwork(xmlResource.getFBNetwork());
                    }
                }
            } else if (xmlSystemData.isSetApplication()) {
                // Loading of Application element
                // as for now only the first application of the system is loaded
                Application theApplication = xmlSystemData.
                        getApplication().get(0);
                String applicationName = (theApplication.isSetName() ? theApplication.
                                          getName() : "Unnamed");
                if (theApplication.isSetFBNetwork()) {

                    constructResource("Resource 1");
                    resource = device.getResource("Resource 1");
                    resource.addFbNetwork(applicationName);
                    fbNetwork = resource.getFbNetwork(applicationName);
                    constructFbNetwork(theApplication.getFBNetwork());

                }
            }
        }
    }

    private void constructResource(String name) {

        device.addResource(name, new Resource(name));

        resource = device.getResource(name);

        // instantiate appropriate scheduler for the resource
        String execModel = properties.getProperty("execModel");
        Integer threads = Integer.valueOf(properties.getProperty("threads"));

        if (execModel.toLowerCase().equals("seqe")) {
            // Sequential event exec model (default):
            // one place in scheduler per fb event received
            Logger.output(Logger.DEBUG, "Running SEQUENTIAL EVENT execution model.");
            resource.setScheduler(new SchedulerSeqEvent(resource, threads));
        } else if (execModel.toLowerCase().equals("seqb")) {
            // Sequential block exec model:
            // one place in scheduler per fb, handle all events per run
            Logger.output(Logger.DEBUG, "Running SEQUENTIAL BLOCK execution model.");
            resource.setScheduler(new SchedulerSeqBlock(resource, threads));
        } else if (execModel.toLowerCase().equals("cycl")) {
            // Cyclic exec model:
            // fb handles all fb events each run
            Logger.output(Logger.DEBUG, "Running CYCLIC execution model.");
            resource.setScheduler(new SchedulerCyclic(resource));
        } else if (execModel.toLowerCase().equals("npmtr")) {
            // NPMTR exec model
            Logger.output(Logger.DEBUG, "Running NPMTR execution model.");
            resource.setScheduler(new SchedulerNpmtr(resource, threads));
        } else if (execModel.toLowerCase().equals("dual")) {
            // Dual exec model
            Logger.output(Logger.DEBUG, "Running DUAL execution model.");
            resource.setScheduler(new SchedulerDual(resource, threads, threads));
        } else {
            Logger.output(Logger.FATAL, "Unknown execution model specified. Exiting.");
            System.exit(1);
        }
    }

    private void loadFbType(JaxbFBType xmlFbTypeData) {
        if (xmlFbTypeData.isSetFBNetwork()) {
            // load composite FB

            resource.addCompositeFbType(xmlFbTypeData.getName());

            CompositeFbType newCompositeFbType =
                (CompositeFbType) resource.getFbType(xmlFbTypeData.getName());

            constructFbInterface(xmlFbTypeData, newCompositeFbType);

            constructCompositeFbType(xmlFbTypeData.getFBNetwork(), newCompositeFbType);
        } else if (xmlFbTypeData.isSetBasicFB()) {
            // load  BasicFbType

            resource.addBasicFbType(xmlFbTypeData.getName());

            BasicFbType newBasicFbType = (BasicFbType) resource.getFbType(xmlFbTypeData.
                                                                            getName());

            constructFbInterface(xmlFbTypeData, newBasicFbType);

            constructBasicFbType(xmlFbTypeData, newBasicFbType);
        } // load service FBs
        else if (xmlFbTypeData.getName().equals("E_RESTART")) {
            resource.addBasicFbType("E_RESTART");

            BasicFbType newBasicFbType = (BasicFbType) resource.getFbType("E_RESTART");

            constructFbInterface(xmlFbTypeData, newBasicFbType);
        } else if (xmlFbTypeData.getName().equals("E_STOP")) {
            resource.addBasicFbType("E_STOP");

            BasicFbType newBasicFbType = (BasicFbType) resource.getFbType("E_STOP");

            constructFbInterface(xmlFbTypeData, newBasicFbType);
        } else {
            String sfbName = xmlFbTypeData.getName();

            resource.addServiceFbType(sfbName, getFile(sfbName + ".bsh"));

            ServiceFbType newServiceFbType = (ServiceFbType) resource.
                getFbType(sfbName);

            constructFbInterface(xmlFbTypeData, newServiceFbType);
        }
    }

    private void constructFbNetwork(JaxbFBNetwork xmlFbNetworkData) {
        if (xmlFbNetworkData.isSetFB()) {
            Logger.output(Logger.DEBUG, "Loader.constructFBNetwork(): Function Blocks");
            for (var fb : xmlFbNetworkData.getFB()) {
                // get and load the FB type
                if (resource.getFbType(fb.getType()) == null) {
                    if (fb.getType().startsWith("E_SPLIT")) {
                        constructSplitType(Integer.valueOf(fb.getType().substring(7)));
                    } else if (fb.getType().startsWith("E_MERGE")) {
                        constructMergeType(Integer.valueOf(fb.getType().substring(7)));
                    } else {
                        load(fb.getType() + ".fbt");
                    }
                }
                fbNetwork.addFbInstance(fb.getName(), fb.getType());
            }
        }


        if (xmlFbNetworkData.isSetEventConnections()) {
            Logger.output(Logger.DEBUG, "Loader.constructFBNetwork(): Event Connections");
            for (var connection : xmlFbNetworkData.getEventConnections().getConnection()) {
                fbNetwork.addEventConnection(connection.getSource(), connection.getDestination());
            }
        }


        if (xmlFbNetworkData.isSetDataConnections()) {
            Logger.output(Logger.DEBUG, "Loader.constructFBNetwork(): Data Connections");
            for (var connection : xmlFbNetworkData.getDataConnections().getConnection()) {
                fbNetwork.addDataConnection(connection.getSource(), connection.getDestination());
            }
        }
    }

    private Variable declarationToVar(VarDeclaration declaration, VariableRole role) {
        if (declaration.getType().toLowerCase().equals("int")) {
            if (declaration.isSetInitialValue()) {
                return new IntegerVariable(role,
                        Integer.valueOf(declaration.getInitialValue()).intValue());
            } else {
                return new IntegerVariable(role, 0);
            }
        } else if (declaration.getType().toLowerCase().equals("bool")) {
            if (declaration.isSetInitialValue()) {
                return new BooleanVariable(role,
                        Boolean.valueOf(declaration.getInitialValue()).booleanValue());
            } else {
                return new BooleanVariable(role, false);
            }
        } else if (declaration.getType().toLowerCase().equals("real")) {
            if (declaration.isSetInitialValue()) {
                return new DoubleVariable(role,
                        Double.valueOf(declaration.getInitialValue()).doubleValue());
            } else {
                return new DoubleVariable(role, 0.0);
            }
        } else if (declaration.getType().toLowerCase().equals("string")) {
            if (declaration.isSetInitialValue()) {
                return new StringVariable(role, declaration.getInitialValue());
            } else {
                return new StringVariable(role, "");
            }
        } else {
            throw new IllegalArgumentException("Unknown type of var: " + declaration.getName());
        }
    }

    private void constructFbInterface(JaxbFBType xmlFbTypeData, FbType newFbType) {

        // Build the interface
        if (xmlFbTypeData.isSetInterfaceList()) {

            // event inputs
            if (xmlFbTypeData.getInterfaceList().isSetEventInputs()) {
                for (var event : xmlFbTypeData.getInterfaceList().getEventInputs().getEvent()) {
                    newFbType.addVariable(event.getName(),
                            new BooleanVariable(VariableRole.EVENT_INPUT, false));
                    // data associations
                    for (var with : event.getWith()) {
                        newFbType.addDataAssociation(event.getName(), with.getVar());
                    }
                }
            }

            // event outputs
            if (xmlFbTypeData.getInterfaceList().isSetEventOutputs()) {
                for (var event : xmlFbTypeData.getInterfaceList().getEventOutputs().getEvent()) {
                    newFbType.addVariable(event.getName(),
                            new BooleanVariable(VariableRole.EVENT_OUTPUT, false));
                    // data associations
                    for (var with : event.getWith()) {
                        newFbType.addDataAssociation(event.getName(), with.getVar());
                    }
                }
            }

            // input data variables
            if (xmlFbTypeData.getInterfaceList().isSetInputVars()) {
                for (var declaration : xmlFbTypeData.getInterfaceList().getInputVars()
                        .getVarDeclaration()) {
                    newFbType.addVariable(declaration.getName(),
                            declarationToVar(declaration, VariableRole.DATA_INPUT));
                }
            }

            // output data variables
            if (xmlFbTypeData.getInterfaceList().isSetOutputVars()) {
                for (var declaration : xmlFbTypeData.getInterfaceList().getOutputVars()
                        .getVarDeclaration()) {
                    newFbType.addVariable(declaration.getName(),
                            declarationToVar(declaration, VariableRole.DATA_OUTPUT));
                }
            }
        }
        // End Build the interface
    }

    private void constructBasicFbType(JaxbFBType xmlFbTypeData, BasicFbType newBasicFbType) {

        // Build internal variables
        if (xmlFbTypeData.getBasicFB().isSetInternalVars()) {
            for (var declaration : xmlFbTypeData.getBasicFB().getInternalVars().getVarDeclaration()) {
                newBasicFbType.addVariable(declaration.getName(),
                        declarationToVar(declaration, VariableRole.LOCAL));
            }
        }
        // End Build internal variables


        // Build Ecc
        // Build States
        int stateNum = 0;
        for (var state : xmlFbTypeData.getBasicFB().getECC().getECState()) {
            if (stateNum == 0) {
                newBasicFbType.getEcc().addInitialState(state.getName());
                stateNum++;
            } else {
                newBasicFbType.getEcc().addState(state.getName());
            }
            for (var action : state.getECAction()) {
                newBasicFbType.getEcc().getState(state.getName()).addAction(action.getAlgorithm(),
                        action.getOutput());
            }
        }
        // Build transitions
        for (var transition : xmlFbTypeData.getBasicFB().getECC().getECTransition()) {
            if (transition.getCondition().equals("1")) {
                newBasicFbType.getEcc().addTransition(transition.getSource(),
                        transition.getDestination(), "TRUE");
            } else {
                newBasicFbType.getEcc().addTransition(transition.getSource(),
                        transition.getDestination(), transition.getCondition());
            }
        }
        // End Build Ecc


        //Build Algorithms
        for (var algorithm : xmlFbTypeData.getBasicFB().getAlgorithm()) {
            if (algorithm.isSetOther()) {
                if (algorithm.getOther().getLanguage().toLowerCase().equals("java")) {
                    newBasicFbType.addAlgorithm(
                            new JavaAlgorithm(algorithm.getName(), algorithm.getOther().getText()));
                }
            }
        }
        // End Build Algorithms

    }

    private void constructCompositeFbType(JaxbFBNetwork xmlFbNetworkData, CompositeFbType newCompositeFbType) {
        if (xmlFbNetworkData.isSetFB()) {
            for (var fb : xmlFbNetworkData.getFB()) {
                // get and load the FB type
                if (resource.getFbType(fb.getType()) == null) {
                    if (fb.getType().startsWith("E_SPLIT")) {
                        constructSplitType(Integer.valueOf(fb.getType().substring(7)));
                    } else if (fb.getType().startsWith("E_MERGE")) {
                        constructMergeType(Integer.valueOf(fb.getType().substring(7)));
                    } else {
                        load(fb.getType() + ".fbt");
                    }
                }
                newCompositeFbType.addFbInstance(fb.getName(), fb.getType());
            }
        }


        if (xmlFbNetworkData.isSetEventConnections()) {
            Logger.output(Logger.DEBUG, "Event Connections:");
            for (var connection : xmlFbNetworkData.getEventConnections().getConnection()) {
                String source = connection.getSource();
                String dest = connection.getDestination();
                newCompositeFbType.addEventConnection(source, dest);
            }
        }


        if (xmlFbNetworkData.isSetDataConnections()) {
            Logger.output(Logger.DEBUG, "Data Connections:");
            for (var connection : xmlFbNetworkData.getDataConnections().getConnection()) {
                String source = connection.getSource();
                String dest = connection.getDestination();
                newCompositeFbType.addDataConnection(source, dest);
            }
        }
    }

    private void constructMergeType(Integer size) {
        resource.addBasicFbType("E_MERGE" + size);

        BasicFbType newBasicFbType = (BasicFbType) resource.getFbType("E_MERGE" + size);

        for (int i = 1; i <= size; i++) {
            newBasicFbType.addVariable("EI" + i, new BooleanVariable(VariableRole.EVENT_INPUT, false));
        }
        newBasicFbType.addVariable("EO", new BooleanVariable(VariableRole.EVENT_OUTPUT, false));

        newBasicFbType.getEcc().addInitialState("S0");
        newBasicFbType.getEcc().addState("S1");
        newBasicFbType.getEcc().getState("S1").addAction(null, "EO");
        String condition = "";
        for (int i = 1; i <= size; i++) {
            if (i == size) {
                condition = condition + "EI" + i;
            } else {
                condition = condition + "EI" + i + " OR ";
            }
        }
        newBasicFbType.getEcc().addTransition("S0", "S1", condition);
        newBasicFbType.getEcc().addTransition("S1", "S0", "TRUE");
    }

    private void constructSplitType(Integer size) {
        resource.addBasicFbType("E_SPLIT" + size);

        BasicFbType newBasicFbType = (BasicFbType) resource.getFbType("E_SPLIT" + size);

        newBasicFbType.addVariable("EI", new BooleanVariable(VariableRole.EVENT_INPUT, false));

        for (int i = 1; i <= size; i++) {
            newBasicFbType.addVariable("EO" + i, new BooleanVariable(VariableRole.EVENT_INPUT, false));
        }

        newBasicFbType.getEcc().addInitialState("S0");
        newBasicFbType.getEcc().addState("S1");
        for (int i = 1; i <= size; i++) {
            newBasicFbType.getEcc().getState("S0").addAction(null, "EO" + i);
        }
        newBasicFbType.getEcc().addTransition("S0", "S1", "EI");
        newBasicFbType.getEcc().addTransition("S1", "S0", "TRUE");
    }
}
