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
package com.llh.fuberruntime.services;

import com.llh.fuberruntime.Logger;
import com.llh.fuberruntime.NamedObject;
import com.llh.fuberruntime.ServiceFbInstance;

public class DelayService extends NamedObject {

    private boolean sendOutput = true;
    private ServiceFbInstance serviceFb;

    public DelayService(ServiceFbInstance fb) {
        setName(DelayService.class.getSimpleName() + "(" + fb.getName() + ")");
        setLogTag(getName());
        serviceFb = fb;
    }

    public synchronized void startDelay(int delay) {
        Logger.output(Logger.DEBUG, getLogTag() + ": waiting" + delay +" ms");
        sendOutput = true;

        try {
            wait(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // send output only if the delay wasn't stopped
        if (sendOutput) {
            serviceFb.sendEvent("EO");
        }
    }

    public synchronized void stopDelay() {
        sendOutput = false;
        notifyAll();
    }
}

