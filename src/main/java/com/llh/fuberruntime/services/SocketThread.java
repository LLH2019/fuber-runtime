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
import com.llh.fuberruntime.ServiceFbInstance;
import com.llh.fuberruntime.StringVariable;
import com.llh.fuberruntime.Variables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketThread extends Thread {

    // serviceFb interaction attributes
    private boolean serviceActive = true;
    private ServiceFbInstance serviceFb;
    private Variables serviceFbVariables;
    // service thread specific attributes
    private Socket socket;
    private PrintWriter socketOut = null;
    private BufferedReader socketIn = null;
    private boolean receive = true;

    public SocketThread(ServiceFbInstance fb, Variables vars) {
        setName("SocketThread");
        serviceFb = fb;
        serviceFbVariables = vars;

        // open the socket
        try {
            socket = new Socket("localhost", 7);
            socketOut = new PrintWriter(socket.getOutputStream(), true);
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown host: localhost");
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public synchronized void receive() {
        receive = true;
        notifyAll();
    }

    public synchronized void send() {
        receive = false;
        notifyAll();

    }

    public synchronized void deactivateService() {
        Logger.output(Logger.DEBUG, "SocketThread: deactivateService()");
        serviceActive = false;
        notifyAll();
    }

    @Override
    public synchronized void run() {
        while (serviceActive) {
            try {
                wait();
            } catch (InterruptedException e) {
                Logger.output(Logger.ERROR, "SocketThread: Interrupted Exception");
                e.printStackTrace(System.err);
            }

            if (receive) // read socket
            {
                String in = "";

                // read socket
                try {
                    in = socketIn.readLine();
                    Logger.output(Logger.DEBUG, "SocketThread.run(): Reading socket: " + in);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }

                // set data output var to read value
                Logger.output(Logger.DEBUG, "SocketThread.run(): Setting var IN to " + in);
                ((StringVariable) serviceFbVariables.getVariable("IN")).setValue(in);

                // send output event
                serviceFb.sendEvent("RECEIVED");
            } else // write to socket
            {

                String out = ((StringVariable) serviceFbVariables.getVariable("OUT")).getValue();

                // write to the socket
                Logger.output(Logger.DEBUG, "SocketThread.run(): Writing " + out + " to the socket");
                socketOut.println(out);

                // send output event
                serviceFb.sendEvent("SENT");

            }
        }
    }
}


