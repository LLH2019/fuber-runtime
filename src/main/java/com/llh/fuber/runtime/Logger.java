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

public class Logger {

    public static final int FATAL = -3;
    public static final int ERROR = -2;
    public static final int WARN = -1;
    public static final int QUIET = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2; // runtime info
    public static final int DEBUG1 = 3; // blocks
    public static final int DEBUG2 = 4; // events
    public static final int DEBUG3 = 5; // queues
    public static final int DEBUG4 = 6; // algorithms
    public static final int DEBUG5 = 7; // variable values
    private static int verboseLevel = INFO;

    public static void setVerboseLevel(int level) {
        verboseLevel = level;
    }

    public static int getVerboseLevel() {
        return verboseLevel;
    }

    public static void hLine(int level) {
        output(level,
               "========================================" +
               "========================================");
    }

    public static void output() {
        output(INFO, "", 0);
    }

    public static void output(int verboseLevel) {
        output(verboseLevel, "", 0);
    }

    public static void output(String text) {
        output(INFO, text, 0);
    }

    public static void output(int verboseLevel, String text) {
        output(verboseLevel, text, 0);
    }

    public static void output(String text, int indentLevel) {
        output(INFO, text, indentLevel);
    }

    public static void output(int verboseLevel, String text, int indentLevel) {
        if (verboseLevel <= Logger.verboseLevel) {
            System.out.print(Thread.currentThread().getName() + ": ");
            for (int i = 1; i <= indentLevel; i++) {
                System.out.print("\t");
            }
            System.out.println(text);
        }
    }
}
