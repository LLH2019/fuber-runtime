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
package com.llh.fuberruntime.interpreters;

import com.llh.fuberruntime.interpreters.abstractsyntax.*;

import java.io.PrintStream;

public class Printer {

PrintStream out;
String indentChar;

public Printer(PrintStream o, String indentChar) {
    out = o;
    this.indentChar = indentChar;
}

public void print(Goal g, int d) {
    prGoal(g, d);
}

public void prGoal(Goal g, int d) {
    indent(d);
    sayLine("Goal");
    if (g instanceof StatementList) {
        prStatementList(((StatementList) g).a, d + 2);
    } else if (g instanceof Expression) {
        prExpression(((Expression) g).a, d + 2);
    }
}

public void prStatementList(StatementList s, int d) {
    indent(d);
    sayLine("StatementList");

    if (s.a != null) {
        prStatementList(s.a, d + 2);
        prStatement(s.b, d + 2);
    } else {
        prStatement(s.b, d + 2);
    }
}

public void prStatement(Statement s, int d) {
    if (s instanceof AssignmentStatement) {
        indent(d);
        sayLine("AssignmentStatement");
        prIdentifier(((AssignmentStatement) s).a, d + 2);
        prExpression(((AssignmentStatement) s).b, d + 2);
    }
}

public void prExpression(Expression e, int d) {
    indent(d);
    sayLine("Expression");
    if (e.a != null) {
        prExpression(e.a, d + 2);
        prXorExpression(e.b, d + 2);
    } else {
        prXorExpression(e.b, d + 2);
    }
}

public void prXorExpression(XorExpression e, int d) {
    indent(d);
    sayLine("XorExpression");
    if (e.a != null) {
        prXorExpression(e.a, d + 2);
        prAndExpression(e.b, d + 2);
    } else {
        prAndExpression(e.b, d + 2);
    }
}

public void prAndExpression(AndExpression e, int d) {
    indent(d);
    sayLine("AndExpression");
    if (e.a != null) {
        prAndExpression(e.a, d + 2);
        prComparison(e.b, d + 2);
    } else {
        prComparison(e.b, d + 2);
    }
}

public void prComparison(Comparison e, int d) {
    if (e instanceof Eq) {
        indent(d);
        sayLine("Eq");
        prAddExpression(((Eq) e).a, d + 2);
        prAddExpression(((Eq) e).b, d + 2);
    } else if (e instanceof Neq) {
        indent(d);
        sayLine("Neq");
        prAddExpression(((Neq) e).a, d + 2);
        prAddExpression(((Neq) e).b, d + 2);
    } else if (e instanceof Less) {
        indent(d);
        sayLine("Less");
        prAddExpression(((Less) e).a, d + 2);
        prAddExpression(((Less) e).b, d + 2);
    } else if (e instanceof LessEq) {
        indent(d);
        sayLine("LessEq");
        prAddExpression(((LessEq) e).a, d + 2);
        prAddExpression(((LessEq) e).b, d + 2);
    } else if (e instanceof More) {
        indent(d);
        sayLine("More");
        prAddExpression(((More) e).a, d + 2);
        prAddExpression(((More) e).b, d + 2);
    } else if (e instanceof MoreEq) {
        indent(d);
        sayLine("MoreEq");
        prAddExpression(((MoreEq) e).a, d + 2);
        prAddExpression(((MoreEq) e).b, d + 2);
    } else if (e instanceof NoComparison) {
        indent(d);
        sayLine("NoComparison");
        prAddExpression(((NoComparison) e).a, d + 2);
    }
}

public void prAddExpression(AddExpression e, int d) {
    if (e instanceof Plus) {
        indent(d);
        sayLine("Plus");
        prAddExpression(((Plus) e).a, d + 2);
        prAddExpression(((Plus) e).b, d + 2);
    } else if (e instanceof Minus) {
        indent(d);
        sayLine("Minus");
        prAddExpression(((Minus) e).a, d + 2);
        prAddExpression(((Minus) e).b, d + 2);
    } else if (e instanceof NoAddExpression) {
        indent(d);
        sayLine("NoAddExpression");
        prTerm(((NoAddExpression) e).a, d + 2);
    }
}

public void prTerm(Term e, int d) {
    if (e instanceof Times) {
        indent(d);
        sayLine("Times");
        prTerm(((Times) e).a, d + 2);
        prTerm(((Times) e).b, d + 2);
    } else if (e instanceof Div) {
        indent(d);
        sayLine("Div");
        prTerm(((Div) e).a, d + 2);
        prTerm(((Div) e).b, d + 2);
    } else if (e instanceof Mod) {
        indent(d);
        sayLine("Mod");
        prTerm(((Mod) e).a, d + 2);
        prTerm(((Mod) e).b, d + 2);
    } else if (e instanceof NoTerm) {
        indent(d);
        sayLine("NoTerm");
        prPowerExpression(((NoTerm) e).a, d + 2);
    }
}

public void prPowerExpression(PowerExpression e, int d) {
    indent(d);
    sayLine("PowerExpression");
    if (e.a != null) {
        prPowerExpression(e.a, d + 2);
        prUnaryExpression(e.b, d + 2);
    } else {
        prUnaryExpression(e.b, d + 2);
    }
}

public void prUnaryExpression(UnaryExpression e, int d) {
    if (e instanceof NoUnaryExpression) {
        indent(d);
        sayLine("NoUnaryExpression");
        prPrimaryExpression(((NoUnaryExpression) e).a, d + 2);
    } else if (e instanceof UnaryExpressionExpression) {
        indent(d);
        sayLine("UnaryExpressionExpression");
        prExpression(((UnaryExpressionExpression) e).a, d + 2);
    } else if (e instanceof UnaryMinus) {
        indent(d);
        sayLine("UnaryMinus");
        prUnaryExpression(((UnaryMinus) e).a, d + 2);
    } else if (e instanceof UnaryNot) {
        indent(d);
        sayLine("UnaryNot");
        prUnaryExpression(((UnaryNot) e).a, d + 2);
    }
}

public void prPrimaryExpression(PrimaryExpression e, int d) {
    if (e instanceof Identifier) {
        prIdentifier((Identifier) e, d);
    } else if (e instanceof PrimaryString) {
        indent(d);
        sayLine("PrimaryString");
        indent(d + 2);
        sayLine(((PrimaryString) e).a);
    } else if (e instanceof PrimaryInt) {
        indent(d);
        sayLine("PrimaryInt");
        indent(d + 2);
        say(((PrimaryInt) e).a);
        sayLine("");
    } else if (e instanceof PrimaryDouble) {
        indent(d);
        sayLine("PrimaryDouble");
        indent(d + 2);
        say(((PrimaryDouble) e).a);
        sayLine("");
    } else if (e instanceof PrimaryBool) {
        indent(d);
        sayLine("PrimaryBool");
        indent(d + 2);
        say(((PrimaryBool) e).a);
        sayLine("");
    }
}

public void prIdentifier(Identifier e, int d) {
    indent(d);
    sayLine("Identifier");
    indent(d + 2);
    sayLine(e.a);
}

private void indent(int d) {
    for (int i = 0; i < d; i++) {
        out.print(indentChar);
    }
}

private void say(Integer i) {
    out.print(i.toString());
}

private void say(Boolean b) {
    out.print(b.toString());
}

private void say(Double d) {
    out.print(d.toString());
}

private void sayLine(String s) {
    out.println(s);
}
}

