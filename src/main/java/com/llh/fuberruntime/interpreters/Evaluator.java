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

import com.llh.fuberruntime.*;
import com.llh.fuberruntime.interpreters.abstractsyntax.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Evaluator {

private Variables vars;
private ByteArrayOutputStream errorMsg;
private PrintStream errorMsgStream;
private Printer printer;

public Evaluator(Variables v) {
    vars = v;
    errorMsg = new ByteArrayOutputStream();
    errorMsgStream = new PrintStream(errorMsg, true);
    printer = new Printer(errorMsgStream, " ");
}

public void setVariables(Variables v) {
    vars = v;
}

public Object evaluate(Goal g) {
    if (g instanceof StatementList) {
        evalStatementList(((StatementList) g).a);
        return vars;
    }
    return evalExpression(((Expression) g).a);
}

private void evalStatementList(StatementList s) {
    if (s.a != null) {
        evalStatementList(s.a);
        evalStatement(s.b);
    }
    evalStatement(s.b);
}

private void evalStatement(Statement s) {
    String ident = ((AssignmentStatement) s).a.a;
    Variable a = vars.getVariable(ident);
    Object b = evalExpression(((AssignmentStatement) s).b);
    if (a instanceof BooleanVariable && b instanceof Boolean) {
        ((BooleanVariable) a).setValue(((Boolean) b).booleanValue());
    } else if (a instanceof IntegerVariable && b instanceof Integer) {
        ((IntegerVariable) a).setValue(((Integer) b).intValue());
    } else if (a instanceof DoubleVariable && b instanceof Double) {
        ((DoubleVariable) a).setValue(((Double) b).doubleValue());
    } else if (a instanceof StringVariable && b instanceof String) {
        ((StringVariable) a).setValue((String) b);
    } else {
        errorMsgStream.println("Incompatible types in:");
        printer.prStatement(s, 4);
        throw new IllegalArgumentException(errorMsg.toString());
    }
}

private Object evalExpression(Expression e) {
    if (e.a != null) {
        Object a = evalExpression(e.a);
        Object b = evalXorExpression(e.b);
        if (a instanceof Boolean && b instanceof Boolean) {
            return Boolean.valueOf(((Boolean) a).booleanValue() || ((Boolean) b).booleanValue());
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prExpression(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    }
    return evalXorExpression(e.b);
}

private Object evalXorExpression(XorExpression e) {
    if (e.a != null) {
        Object a = evalXorExpression(e.a);
        Object b = evalAndExpression(e.b);
        if (a instanceof Boolean && b instanceof Boolean) {
            return Boolean.valueOf((!((Boolean) a).booleanValue() && ((Boolean) b).booleanValue()) || (((Boolean) a).booleanValue() && !((Boolean) b).booleanValue()));
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prXorExpression(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else {
        return evalAndExpression(e.b);
    }
}

private Object evalAndExpression(AndExpression e) {
    if (e.a != null) {
        Object a = evalAndExpression(e.a);
        Object b = evalComparison(e.b);
        if (a instanceof Boolean && b instanceof Boolean) {
            return Boolean.valueOf(((Boolean) a).booleanValue() && ((Boolean) b).booleanValue());
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prAndExpression(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else {
        return evalComparison(e.b);
    }
}

private Object evalComparison(Comparison e) {
    if (e instanceof Eq) {
        Object a = evalAddExpression(((Eq) e).a);
        if (a instanceof Integer) {
            Object b = evalAddExpression(((Eq) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Integer) a).intValue() == ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Integer) a).intValue() == ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalAddExpression(((Eq) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Double) a).doubleValue() == ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Double) a).doubleValue() == ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof String) {
            Object b = evalAddExpression(((Eq) e).b);
            if (b instanceof String) {
                return Boolean.valueOf(((String) a).equals(((String) b).toString()));
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prComparison(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else if (e instanceof Neq) {
        Object a = evalAddExpression(((Neq) e).a);
        if (a instanceof Integer) {
            Object b = evalAddExpression(((Neq) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Integer) a).intValue() != ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Integer) a).intValue() != ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalAddExpression(((Neq) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Double) a).doubleValue() != ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Double) a).doubleValue() != ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof String) {
            Object b = evalAddExpression(((Neq) e).b);
            if (b instanceof String) {
                return Boolean.valueOf(!((String) a).equals(((String) b).toString()));
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prComparison(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else if (e instanceof Less) {
        Object a = evalAddExpression(((Less) e).a);
        if (a instanceof Integer) {
            Object b = evalAddExpression(((Less) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Integer) a).intValue() < ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Integer) a).intValue() < ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalAddExpression(((Less) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Double) a).doubleValue() < ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Double) a).doubleValue() < ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prComparison(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else if (e instanceof LessEq) {
        Object a = evalAddExpression(((LessEq) e).a);
        if (a instanceof Integer) {
            Object b = evalAddExpression(((LessEq) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Integer) a).intValue() <= ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Integer) a).intValue() <= ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalAddExpression(((LessEq) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Double) a).doubleValue() <= ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Double) a).doubleValue() <= ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prComparison(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else if (e instanceof More) {
        Object a = evalAddExpression(((More) e).a);
        if (a instanceof Integer) {
            Object b = evalAddExpression(((More) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Integer) a).intValue() > ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Integer) a).intValue() > ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalAddExpression(((More) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Double) a).doubleValue() > ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Double) a).doubleValue() > ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prComparison(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else if (e instanceof MoreEq) {
        Object a = evalAddExpression(((MoreEq) e).a);
        if (a instanceof Integer) {
            Object b = evalAddExpression(((MoreEq) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Integer) a).intValue() >= ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Integer) a).intValue() >= ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalAddExpression(((MoreEq) e).b);
            if (b instanceof Integer) {
                return Boolean.valueOf(((Double) a).doubleValue() >= ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Boolean.valueOf(((Double) a).doubleValue() >= ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prComparison(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prComparison(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    }
    return evalAddExpression(((NoComparison) e).a);
}

private Object evalAddExpression(AddExpression e) {
    if (e instanceof Plus) {
        Object a = evalAddExpression(((Plus) e).a);
        if (a instanceof Integer) {
            Object b = evalAddExpression(((Plus) e).b);
            if (b instanceof Integer) {
                return Integer.valueOf(((Integer) a).intValue() + ((Integer) b).intValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prAddExpression(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalAddExpression(((Plus) e).b);
            if (b instanceof Double) {
                return Double.valueOf(((Double) a).doubleValue() + ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prAddExpression(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prAddExpression(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else if (e instanceof Minus) {
        Object a = evalAddExpression(((Minus) e).a);
        if (a instanceof Integer) {
            Object b = evalAddExpression(((Minus) e).b);
            if (b instanceof Integer) {
                return Integer.valueOf(((Integer) a).intValue() - ((Integer) b).intValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prAddExpression(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalAddExpression(((Minus) e).b);
            if (b instanceof Double) {
                return Double.valueOf(((Double) a).doubleValue() - ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prAddExpression(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prAddExpression(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } 
    return evalTerm(((NoAddExpression) e).a);
}

private Object evalTerm(Term e) {
    if (e instanceof Times) {
        Object a = evalTerm(((Times) e).a);
        if (a instanceof Integer) {
            Object b = evalTerm(((Times) e).b);
            if (b instanceof Integer) {
                return Integer.valueOf(((Integer) a).intValue() * ((Integer) b).intValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prTerm(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalTerm(((Times) e).b);
            if (b instanceof Double) {
                return Double.valueOf(((Double) a).doubleValue() * ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prTerm(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prTerm(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else if (e instanceof Div) {
        Object a = evalTerm(((Div) e).a);
        if (a instanceof Integer) {
            Object b = evalTerm(((Div) e).b);
            if (b instanceof Integer) {
                return Integer.valueOf(((Integer) a).intValue() / ((Integer) b).intValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prTerm(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalTerm(((Div) e).b);
            if (b instanceof Double) {
                return Double.valueOf(((Double) a).doubleValue() / ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prTerm(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prTerm(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else if (e instanceof Mod) {
        Object a = evalTerm(((Mod) e).a);
        if (a instanceof Integer) {
            Object b = evalTerm(((Mod) e).b);
            if (b instanceof Integer) {
                return Integer.valueOf(((Integer) a).intValue() % ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Double.valueOf(((Integer) a).intValue() % ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prTerm(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else if (a instanceof Double) {
            Object b = evalTerm(((Mod) e).b);
            if (b instanceof Integer) {
                return Double.valueOf(((Double) a).doubleValue() % ((Integer) b).intValue());
            } else if (b instanceof Double) {
                return Double.valueOf(((Double) a).doubleValue() % ((Double) b).doubleValue());
            } else {
                errorMsgStream.println("Incompatible types in:");
                printer.prTerm(e, 4);
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prTerm(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    }
    return evalPowerExpression(((NoTerm) e).a);
}

private Object evalPowerExpression(PowerExpression e) {
    if (e.a != null) {
        Object a = evalPowerExpression(e.a);
        Object b = evalUnaryExpression(e.b);
        if (a instanceof Double && b instanceof Double) {
            return Double.valueOf(Math.pow(((Double) a).doubleValue(), ((Double) b).doubleValue()));
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prPowerExpression(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    }
    return evalUnaryExpression(e.b);
}

private Object evalUnaryExpression(UnaryExpression e) {
    if (e instanceof UnaryExpressionExpression) {
        return evalExpression(((UnaryExpressionExpression) e).a);
    } else if (e instanceof UnaryMinus) {
        Object a = evalUnaryExpression(((UnaryMinus) e).a);
        if (a instanceof Integer) {
            return Integer.valueOf(-((Integer) a).intValue());
        } else if (a instanceof Double) {
            return Double.valueOf(-((Double) a).doubleValue());
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prUnaryExpression(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    } else if (e instanceof UnaryNot) {
        Object a = evalUnaryExpression(((UnaryNot) e).a);
        if (a instanceof Boolean) {
            return Boolean.valueOf(!((Boolean) a).booleanValue());
        } else {
            errorMsgStream.println("Wrong type in:");
            printer.prUnaryExpression(e, 4);
            throw new IllegalArgumentException(errorMsg.toString());
        }
    }
    return evalPrimaryExpression(((NoUnaryExpression) e).a);
}

private Object evalPrimaryExpression(PrimaryExpression e) {
    if (e instanceof PrimaryBool) {
        return ((PrimaryBool) e).a;
    } else if (e instanceof PrimaryInt) {
        return ((PrimaryInt) e).a;
    } else if (e instanceof PrimaryDouble) {
        return ((PrimaryDouble) e).a;
    } else if (e instanceof PrimaryString) {
        return ((PrimaryString) e).a;
    }
    Variable var = vars.getVariable(((Identifier) e).a);
    if (var instanceof BooleanVariable) {
        return ((BooleanVariable) var).getValue();
    } else if (var instanceof IntegerVariable) {
        return ((IntegerVariable) var).getValue();
    } else if (var instanceof DoubleVariable) {
        return ((DoubleVariable) var).getValue();
    }
    return ((StringVariable) var).getValue();
}
}

