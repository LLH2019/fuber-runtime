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
package com.llh.fuber.runtime.fbtype;

import com.llh.fuber.runtime.variable.Variables;

import java.util.*;

public class Ecc {

    // Map: String name -> EcState state
    private Map<String, EcState> ecStates = new HashMap<String, EcState>();
    // List: EcTransition
    private List<EcTransition> ecTransitions = new LinkedList<EcTransition>();
    private EcState initialState = null;

    public Ecc() {
        //Logger.output(Logger.DEBUG,"Ecc(): Creating empty Ecc");
    }

    public void addInitialState(String initial) {
        addState(initial);
        initialState = getState(initial);
    }

    public EcState getInitialState() {
        return initialState;
    }

    public void addState(String state) {
        ecStates.put(state, new EcState(state));
    }

    public EcState getState(String state) {
        return ecStates.get(state);
    }

    public void addTransition(String source, String dest, String cond) {
        ecTransitions.add(new EcTransition(getState(source), getState(dest), new EcCondition(cond)));
    }

    // returns new state if transition clears, otherwise null
    public synchronized EcState execute(EcState currentEcState, Variables vars) {

        // find all transitions that have currentEcState in their source
        List<EcTransition> ecTransitionsWithSameSource = new LinkedList<EcTransition>();
        for (var transition : ecTransitions) {
            if (transition.getSource() == currentEcState) {
                ecTransitionsWithSameSource.add(transition);
            }
        }


        // evaluate all of their conditions and remove the ones that can not be taken
        if (ecTransitionsWithSameSource.size() > 0) {
            for (Iterator<EcTransition> iter = ecTransitionsWithSameSource.iterator(); iter.hasNext();) {
                EcTransition tempTransition = iter.next();
                Boolean evaluationResult;
                try {
                    evaluationResult = tempTransition.getCondition().evaluate(vars);
                } catch (Exception e) {
                    throw new IllegalArgumentException("at state: " + currentEcState.getName() + ": "
                    + e.getMessage());
                }
                if (!evaluationResult.booleanValue()) {
                    iter.remove();
                }
            }
        } else {
            throw new IllegalArgumentException("no possible transitions from state: "
                    + currentEcState.getName());
        }

        // if more than one condition is true throw exception, otherwise take the transition
        if (ecTransitionsWithSameSource.size() > 0) {
            if (ecTransitionsWithSameSource.size() == 1) {
                return ecTransitionsWithSameSource.get(0).
                        getDestination();
            } else {
                throw new IllegalArgumentException("more than one possible transitions from state: "
                        + currentEcState.getName());
            }
        }

        // didn't find any transitions that could be taken
        return null;
    }
}
