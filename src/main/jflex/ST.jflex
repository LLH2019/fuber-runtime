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
/* The scanner definition for the IEC 61131-3 ST language */

package com.llh.fuberruntime.interpreters.st;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;
import java.io.StringReader;

%%

%class Lexer
%public
%implements Symbols
%line
%column
%full
%cupsym Symbols
%cup
%{
    private ComplexSymbolFactory symbolFactory;
    public Lexer(StringReader sr, ComplexSymbolFactory sf){
        this(sr);
        symbolFactory = sf;
    }
    public Symbol symbol(String name, int code) {
        return symbolFactory.newSymbol(name, code,
            new Location(yyline + 1, yycolumn + 1 - yylength()),
            new Location(yyline + 1, yycolumn + 1));
    }
    public Symbol symbol(String name, int code, Object value) {
        return symbolFactory.newSymbol(name, code,
            new Location(yyline + 1, yycolumn + 1),
            new Location(yyline + 1, yycolumn + yylength()), value);
    }
%}

ALPHA = [A-Za-z]
DIGIT = [0-9]
NONNEWLINE_WHITE_SPACE_CHAR = [\ \t\b\012]
NEWLINE = \r | \n | \r\n
WHITE_SPACE_CHAR = [\n\r\ \t\b\012]
STRING_TEXT = ( \\\" | [^\n\r\"] | \\{WHITE_SPACE_CHAR}+\\ )*
COMMENT = "(*" [^*] ~"*)" | "(*" "*"+ ")"
IDENT = {ALPHA} ( {ALPHA} | {DIGIT} | _ )*

%% 

<YYINITIAL> {
    ";"                             {return symbol("STATE", STATE);}
    ":="                            {return symbol("ASSIGN", ASSIGN);}

    "OR"                            {return symbol("OR", OR);}
    "XOR"                           {return symbol("XOR", XOR);}
    "&"                             {return symbol("AND", AND);}
    "AND"                           {return symbol("AND", AND);}

    "="                             {return symbol("EQ", EQ);}
    "<>"                            {return symbol("NEQ", NEQ);}

    "<"                             {return symbol("LESS", LESS);}
    "<="                            {return symbol("LESSEQ", LESSEQ);}
    ">"                             {return symbol("MORE", MORE);}
    ">="                            {return symbol("MOREEQ", MOREEQ);}

    "+"                             {return symbol("PLUS", PLUS);}
    "-"                             {return symbol("MINUS", MINUS);}

    "*"                             {return symbol("TIMES", TIMES);}
    "/"                             {return symbol("DIV", DIV);}
    "MOD"                           {return symbol("MOD", MOD);}

    "**"                            {return symbol("POWER", POWER);}

    "NOT"                           {return symbol("NOT", NOT);}

    "("                             {return symbol("LPAREN", LPAREN);}
    ")"                             {return symbol("RPAREN", RPAREN);}

    \"{STRING_TEXT}\"               {return symbol("STRING", STRING, yytext().substring(1, yylength() - 1));}

    {DIGIT}+                        {return symbol("INT", INT, Integer.valueOf(yytext()));}

    {DIGIT}+"."{DIGIT}+             {return symbol("DOUBLE", DOUBLE, Double.valueOf(yytext()));}
    {DIGIT}+"."{DIGIT}+"e"{DIGIT}+  {return symbol("DOUBLE", DOUBLE, Double.valueOf(yytext()));}

    "TRUE"                          {return symbol("BOOL", BOOL, Boolean.valueOf(true));}
    "FALSE"                         {return symbol("BOOL", BOOL, Boolean.valueOf(false));}

    {IDENT}                         {return symbol("IDENT", IDENT, yytext());}

    {NEWLINE}                       {}
    {NONNEWLINE_WHITE_SPACE_CHAR}+  {}
    {COMMENT}                       {}

    .                               {System.out.println("Illegal character: <" + yytext() + ">");}

    <<EOF>>                         {return symbol("EOF", EOF);}
}
