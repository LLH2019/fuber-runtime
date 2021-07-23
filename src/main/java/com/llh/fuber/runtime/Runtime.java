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

import java.io.File;
import java.util.Calendar;
import java.util.Properties;

public class Runtime {

    private static Properties properties;
    private static final String helpString = "Arguments: \n" +
                                             "\t[-d [<level>]]    - debug info\n" +
                                             "\t[-q]              - quiet\n" +
                                             "\t[-t <nr>]         - number of threads for dual exec model\n" +
                                             "\t[-m <exec model>] - exec model is one of:\n" +
                                             "\t                      seqe  - sequential event buffered exec model (default)\n" +
                                             "\t                      seqb  - sequential block buffered exec model\n" +
                                             "\t                      cycl  - cyclic buffered exec model\n" +
                                             "\t                      npmtr - npmtr exec model\n" +
                                             "\t                      dual  - dual buffered exec model\n" +
                                             "\t[-lb <path>]      - library path base\n" +
                                             "\t[-lp <path>]...   - library directories (relative to the library path base if given)\n" +
                                             "\t <file>           - system file";

    public static void main(String[] args) {
        properties = new Properties();
        new Runtime(args);
        new Loader().getDevice().run();
        Logger.output(Logger.DEBUG, "Runtime.main(): returning");
    }

    public static Properties getProperties() {
        return properties;
    }

    private Runtime(String[] args) {

        properties.setProperty("execModel", "seqe"); // default exec model
        properties.setProperty("threads", "1");

        // argument parsing ===================================================
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                if (i + 1 < args.length) {
                    switch (args[i + 1].toCharArray()[0]) {
                        case '1':
                            Logger.setVerboseLevel(Logger.DEBUG1);
                            break;
                        case '2':
                            Logger.setVerboseLevel(Logger.DEBUG2);
                            break;
                        case '3':
                            Logger.setVerboseLevel(Logger.DEBUG3);
                            break;
                        case '4':
                            Logger.setVerboseLevel(Logger.DEBUG4);
                            break;
                        case '5':
                            Logger.setVerboseLevel(Logger.DEBUG5);
                            break;
                        default:
                            Logger.setVerboseLevel(Logger.DEBUG);
                    }
                }
            }
            if (args[i].equals("-q")) {
                Logger.setVerboseLevel(Logger.WARN);
            }
            if (args[i].equals("-t")) {
                if (i + 1 < args.length) {
                    properties.setProperty("threads", args[i + 1]);
                }
            }
            if (args[i].equals("-m")) {
                if (i + 1 < args.length) {
                    properties.setProperty("execModel", args[i + 1]);
                }
            }
            if (args[i].equals("-lb")) {
                if (i + 1 < args.length) {
                    properties.setProperty("libraryPathBase", args[i + 1]);
                }
            }

            if (args[i].equals("-lp")) {
                if (i + 1 < args.length) {
                    if (properties.getProperty("libraryPath") == null) {
                        properties.setProperty("libraryPath", args[i + 1]);
                    } else {
                        properties.setProperty("libraryPath",
                                               properties.getProperty("libraryPath") + File.pathSeparator + args[i + 1]);
                    }
                }

            }

            if (i == args.length - 1) {
                properties.setProperty("systemFileName", args[i]);
                if (properties.getProperty("systemFileName").charAt(0) == '-') {
                    properties.setProperty("systemFileName", null);
                }
            }
        }

        // startup ============================================================
        String systemFileName = properties.getProperty("systemFileName");
        String libraryPathBase = properties.getProperty("libraryPathBase");
        String libraryPath = properties.getProperty("libraryPath");
        String execModel = properties.getProperty("execModel");
        Integer threads = Integer.valueOf(properties.getProperty("threads"));


        Logger.output("Fuber");
        Logger.output("Copyright (C) 2006-" + Calendar.getInstance().get(Calendar.YEAR) + " Goran Cengic");

        if (args.length == 0 || systemFileName == null) {
            Logger.output(Logger.ERROR, helpString);
            System.exit(1);
        }

        Logger.output(Logger.DEBUG1, "Arguments: " + args.length);
        for (int i = 0; i < args.length; i++) {
            Logger.output(Logger.DEBUG1, "  arg[" + i + "]: " + args[i], 1);
        }

        Logger.output(Logger.DEBUG1, "Input arguments: \n" +
                                     "\t system file name: " + systemFileName + "\n" +
                                     "\t library path base: " + libraryPathBase + "\n" +
                                     "\t library path: " + libraryPath + "\n" +
                                     "\t execution model: " + execModel + "\n" +
                                     "\t nr of threads: " + threads);
    }
}
