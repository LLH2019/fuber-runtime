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
import com.llh.fuber.runtime.interpreters.Evaluator;
import com.llh.fuber.runtime.interpreters.abstractsyntax.StatementList;
import com.llh.fuber.runtime.interpreters.st.Lexer;
import com.llh.fuber.runtime.interpreters.st.Parser;
import java_cup.runtime.ComplexSymbolFactory;

import java.io.StringReader;

public class StructuredTextAlgorithm extends Algorithm {

    private String algorithmText;
    private StatementList abstractSyntax = null;
    private Evaluator evaluator = null;

    public StructuredTextAlgorithm(String n, String algText) {
        setName(n);
        algorithmText = algText;

        StringReader stringReader = new StringReader(algorithmText);
        ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();
        Lexer lexer = new Lexer(stringReader, symbolFactory);
        Parser parser = new Parser(lexer, symbolFactory);

        try {
            abstractSyntax = (StatementList) parser.parse().value;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage()
                    + ": cannot parse algorithm as statement list: "
                    + algorithmText);
        }

        algorithmText = algText;
    }

    public void execute(Variables vars) throws Exception {
        if (evaluator == null) {
            evaluator = new Evaluator(vars);
        } else {
            evaluator.setVariables(vars);
        }

        evaluator.evaluate(abstractSyntax);
    }
}