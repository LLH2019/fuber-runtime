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

import com.llh.fuber.runtime.Logger;
import com.llh.fuber.runtime.variable.Variables;
import com.llh.fuber.runtime.interpreters.Evaluator;
import com.llh.fuber.runtime.interpreters.abstractsyntax.Expression;
import com.llh.fuber.runtime.interpreters.st.Parser;
import java_cup.runtime.ComplexSymbolFactory;
import com.llh.fuber.runtime.interpreters.st.Lexer;

import java.io.StringReader;

public final class EcCondition {

    private String condition = "";
    private Expression abstractSyntax = null;
    private Evaluator evaluator = null;

    public EcCondition(String condition) {
        set(condition);
    }

    public void set(String cond) {

        Logger.output(Logger.DEBUG4, "EcCondition.set(" + cond + ")");

        condition = cond;

        StringReader stringReader = new StringReader(condition);
        ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();
        Lexer lexer = new Lexer(stringReader, symbolFactory);
        Parser parser = new Parser(lexer, symbolFactory);

        try {
            abstractSyntax = (Expression) parser.parse().value;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage()
                + ": cannot parse condition as expression: " + cond);
        }
    }

    public Boolean evaluate(Variables vars) {
        Logger.output(Logger.DEBUG2, "EcCondition.evaluate(): Evaluating \"" +
                                     condition + "\" with vars...");
        Logger.output(Logger.DEBUG2, "\t" + vars.toString());
        if (evaluator == null) {
            evaluator = new Evaluator(vars);
        } else {
            evaluator.setVariables(vars);
        }

        Boolean result;
        try {
            result = (Boolean) evaluator.evaluate(abstractSyntax);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("non Boolean returned from evaluation of: "
            + condition);
        }
        return result;
    }

}
