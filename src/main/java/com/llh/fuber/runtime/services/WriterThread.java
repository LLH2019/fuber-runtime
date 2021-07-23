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
package com.llh.fuber.runtime.services;

import com.llh.fuber.runtime.Logger;
import com.llh.fuber.runtime.instance.ServiceFbInstance;

public class WriterThread extends Thread {

    private int outputSignal;
    private boolean outputValue;
    private boolean serviceActive = true;
    private ServiceFbInstance serviceFb;

    //==========================================================================
    public WriterThread(ServiceFbInstance fb) {
        setName("WriterThread");
        serviceFb = fb;
    }
    //==========================================================================

    public synchronized void writeOutput(int output, boolean value) {
        outputSignal = output;
        outputValue = value;
        notifyAll();
    }

    public synchronized void deactivateService() {
        Logger.output(Logger.DEBUG, "WriterThread: deactivateService()");
        serviceActive = false;
        notifyAll();
    }

    @Override
    public synchronized void run() {
        while (serviceActive) {
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.output(Logger.ERROR,
                              "WriterThread: Interrupted Exception");
                e.printStackTrace(System.err);
            }

            // write value to the signal of the Digital I/O card.
            Logger.output(Logger.DEBUG,
                    "WriterThread(" + serviceFb.getName() + ").run(): " +
                    "writing value " + outputValue + " to " + outputSignal);

            // send output event
            Logger.output(Logger.DEBUG,
                          "WriterThread(" + serviceFb.getName() + ").run(): " +
                          "sending CNF event");
            serviceFb.sendEvent("CNF");
        }
    }
}
