/*
 * Copyright (c) 2012, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 * 
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 * 
 * (a) the Software, and
 * 
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 * 
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 * 
 * This license is subject to the following condition:
 * 
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

// The content of this file is automatically generated. DO NOT EDIT.

package com.oracle.truffle.sl.parser;

import java.util.*;

import com.oracle.truffle.api.source.*;
import com.oracle.truffle.sl.*;
import com.oracle.truffle.sl.nodes.*;
import com.oracle.truffle.sl.runtime.*;

// Checkstyle: stop
// @formatter:off
public class Parser {
	public static final int _EOF = 0;
	public static final int _identifier = 1;
	public static final int _stringLiteral = 2;
	public static final int _numericLiteral = 3;
	public static final int maxT = 33;

    static final boolean _T = true;
    static final boolean _x = false;
    static final int minErrDist = 2;

    public Token t; // last recognized token
    public Token la; // lookahead token
    int errDist = minErrDist;

    public final Scanner scanner;
    public final Errors errors;
    private final SLNodeFactory factory;
    
    public Parser(SLContext context, Source source) {
        this.scanner = new Scanner(source.getInputStream());
        this.factory = new SLNodeFactory(context, source);
        errors = new Errors();
    }

    void SynErr(int n) {
        if (errDist >= minErrDist)
            errors.SynErr(la.line, la.col, n);
        errDist = 0;
    }

    public void SemErr(String msg) {
        if (errDist >= minErrDist)
            errors.SemErr(t.line, t.col, msg);
        errDist = 0;
    }

    void Get() {
        for (;;) {
            t = la;
            la = scanner.Scan();
            if (la.kind <= maxT) {
                ++errDist;
                break;
            }

            la = t;
        }
    }

    void Expect(int n) {
        if (la.kind == n)
            Get();
        else {
            SynErr(n);
        }
    }

    boolean StartOf(int s) {
        return set[s][la.kind];
    }

    void ExpectWeak(int n, int follow) {
        if (la.kind == n)
            Get();
        else {
            SynErr(n);
            while (!StartOf(follow))
                Get();
        }
    }

    boolean WeakSeparator(int n, int syFol, int repFol) {
        int kind = la.kind;
        if (kind == n) {
            Get();
            return true;
        } else if (StartOf(repFol))
            return false;
        else {
            SynErr(n);
            while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
                Get();
                kind = la.kind;
            }
            return StartOf(syFol);
        }
    }

	void SimpleLanguage() {
		Function();
		while (la.kind == 4) {
			Function();
		}
	}

	void Function() {
		Expect(4);
		Expect(1);
		Token identifierToken = t; 
		Expect(5);
		int bodyStartPos = t.charPos; 
		factory.startFunction(identifierToken, bodyStartPos); 
		if (la.kind == 1) {
			Get();
			factory.addFormalParameter(t); 
			while (la.kind == 6) {
				Get();
				Expect(1);
				factory.addFormalParameter(t); 
			}
		}
		Expect(7);
		SLStatementNode body = Block(false);
		factory.finishFunction(body); 
	}

	SLStatementNode  Block(boolean inLoop) {
		SLStatementNode  result;
		factory.startBlock();
		List<SLStatementNode> body = new ArrayList<>(); 
		Expect(8);
		int start = t.charPos; 
		while (StartOf(1)) {
			SLStatementNode s = Statement(inLoop);
			body.add(s); 
		}
		Expect(9);
		int length = (t.charPos + t.val.length()) - start; 
		result = factory.finishBlock(body, start, length); 
		return result;
	}

	SLStatementNode  Statement(boolean inLoop) {
		SLStatementNode  result;
		result = null; 
		switch (la.kind) {
		case 13: {
			result = WhileStatement();
			break;
		}
		case 10: {
			Get();
			if (inLoop) { result = factory.createBreak(t); } else { SemErr("break used outside of loop"); } 
			Expect(11);
			break;
		}
		case 12: {
			Get();
			if (inLoop) { result = factory.createContinue(t); } else { SemErr("continue used outside of loop"); } 
			Expect(11);
			break;
		}
		case 14: {
			result = IfStatement(inLoop);
			break;
		}
		case 16: {
			result = ReturnStatement();
			break;
		}
		case 1: case 2: case 3: case 5: case 29: {
			result = Expression();
			Expect(11);
			break;
		}
		default: SynErr(34); break;
		}
		return result;
	}

	SLStatementNode  WhileStatement() {
		SLStatementNode  result;
		Expect(13);
		Token whileToken = t; 
		Expect(5);
		SLExpressionNode condition = Expression();
		Expect(7);
		SLStatementNode body = Block(true);
		result = factory.createWhile(whileToken, condition, body); 
		return result;
	}

	SLStatementNode  IfStatement(boolean inLoop) {
		SLStatementNode  result;
		Expect(14);
		Token ifToken = t; 
		Expect(5);
		SLExpressionNode condition = Expression();
		Expect(7);
		SLStatementNode thenPart = Block(inLoop);
		SLStatementNode elsePart = null; 
		if (la.kind == 15) {
			Get();
			elsePart = Block(inLoop);
		}
		result = factory.createIf(ifToken, condition, thenPart, elsePart); 
		return result;
	}

	SLStatementNode  ReturnStatement() {
		SLStatementNode  result;
		Expect(16);
		Token returnToken = t;
		SLExpressionNode value = null; 
		if (StartOf(2)) {
			value = Expression();
		}
		result = factory.createReturn(returnToken, value); 
		Expect(11);
		return result;
	}

	SLExpressionNode  Expression() {
		SLExpressionNode  result;
		result = LogicTerm();
		while (la.kind == 17) {
			Get();
			Token op = t; 
			SLExpressionNode right = LogicTerm();
			result = factory.createBinary(op, result, right); 
		}
		return result;
	}

	SLExpressionNode  LogicTerm() {
		SLExpressionNode  result;
		result = LogicFactor();
		while (la.kind == 18) {
			Get();
			Token op = t; 
			SLExpressionNode right = LogicFactor();
			result = factory.createBinary(op, result, right); 
		}
		return result;
	}

	SLExpressionNode  LogicFactor() {
		SLExpressionNode  result;
		result = Arithmetic();
		if (StartOf(3)) {
			switch (la.kind) {
			case 19: {
				Get();
				break;
			}
			case 20: {
				Get();
				break;
			}
			case 21: {
				Get();
				break;
			}
			case 22: {
				Get();
				break;
			}
			case 23: {
				Get();
				break;
			}
			case 24: {
				Get();
				break;
			}
			}
			Token op = t; 
			SLExpressionNode right = Arithmetic();
			result = factory.createBinary(op, result, right); 
		}
		return result;
	}

	SLExpressionNode  Arithmetic() {
		SLExpressionNode  result;
		result = Term();
		while (la.kind == 25 || la.kind == 26) {
			if (la.kind == 25) {
				Get();
			} else {
				Get();
			}
			Token op = t; 
			SLExpressionNode right = Term();
			result = factory.createBinary(op, result, right); 
		}
		return result;
	}

	SLExpressionNode  Term() {
		SLExpressionNode  result;
		result = Factor();
		while (la.kind == 27 || la.kind == 28) {
			if (la.kind == 27) {
				Get();
			} else {
				Get();
			}
			Token op = t; 
			SLExpressionNode right = Factor();
			result = factory.createBinary(op, result, right); 
		}
		return result;
	}

	SLExpressionNode  Factor() {
		SLExpressionNode  result;
		result = null; 
		if (la.kind == 1) {
			Get();
			if (la.kind == 5 || la.kind == 31 || la.kind == 32) {
				result = MemberExpression(null, null, t);
			} else if (StartOf(4)) {
				result = factory.createRead(t); 
			} else SynErr(35);
		} else if (la.kind == 2) {
			Get();
			result = factory.createStringLiteral(t); 
		} else if (la.kind == 3) {
			Get();
			result = factory.createNumericLiteral(t); 
		} else if (la.kind == 5) {
			Get();
			int start = t.charPos; 
			result = Expression();
			SLExpressionNode expr = result; 
			Expect(7);
			int length = (t.charPos + t.val.length()) - start; 
			result = factory.createParenExpression(expr, start, length); 
		} else if (la.kind == 29) {
			Get();
			List<SLExpressionNode> parameters = new ArrayList<>(); 
			Token startToken = t; 
			SLExpressionNode parameter; 
			if (StartOf(2)) {
				parameter = Expression();
				parameters.add(parameter); 
				while (la.kind == 6) {
					Get();
					parameter = Expression();
					parameters.add(parameter); 
				}
			}
			Expect(30);
			result = factory.createArray(startToken, t, parameters); 
		} else SynErr(36);
		return result;
	}

	SLExpressionNode  MemberExpression(SLExpressionNode r, SLExpressionNode assignmentReceiver, Token assignmentName) {
		SLExpressionNode  result;
		result = null;
		SLExpressionNode receiver = r;
		Token nestedAssignmentName = null; 
		if (la.kind == 5) {
			Get();
			List<SLExpressionNode> parameters = new ArrayList<>();
			SLExpressionNode parameter;
			if (receiver == null) {
			   receiver = factory.createRead(assignmentName); 
			} 
			if (StartOf(2)) {
				parameter = Expression();
				parameters.add(parameter); 
				while (la.kind == 6) {
					Get();
					parameter = Expression();
					parameters.add(parameter); 
				}
			}
			Expect(7);
			Token finalToken = t; 
			result = factory.createCall(receiver, parameters, finalToken); 
		} else if (la.kind == 31) {
			Get();
			SLExpressionNode value = Expression();
			if (assignmentName == null) {
			   SemErr("invalid assignment target");
			} else if (assignmentReceiver == null) {
			   result = factory.createAssignment(assignmentName, value);
			} else {
			   result = factory.createWriteProperty(assignmentReceiver, assignmentName, value);
			} 
		} else if (la.kind == 32) {
			Get();
			if (receiver == null) {
			   receiver = factory.createRead(assignmentName); 
			} 
			Expect(1);
			result = factory.createReadProperty(receiver, t); 
			nestedAssignmentName = t; 
		} else SynErr(37);
		if (la.kind == 5 || la.kind == 31 || la.kind == 32) {
			result = MemberExpression(result, receiver, nestedAssignmentName);
		}
		return result;
	}



    public void Parse() {
        la = new Token();
        la.val = "";
        Get();
		SimpleLanguage();
		Expect(0);

    }

    private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x},
		{_x,_T,_T,_T, _x,_T,_x,_x, _x,_x,_T,_x, _T,_T,_T,_x, _T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_T,_x,_x, _x,_x,_x},
		{_x,_T,_T,_T, _x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_T,_x,_x, _x,_x,_x},
		{_x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_T, _T,_T,_T,_T, _T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x},
		{_x,_x,_x,_x, _x,_T,_T,_T, _x,_x,_x,_T, _x,_x,_x,_x, _x,_T,_T,_T, _T,_T,_T,_T, _T,_T,_T,_T, _T,_x,_T,_T, _T,_x,_x}

    };

    public static void parseSL(SLContext context, Source source) {
        Parser parser = new Parser(context, source);
        parser.Parse();
        if (parser.errors.errors.size() > 0) {
            StringBuilder msg = new StringBuilder("Error(s) parsing script:\n");
            for (String error : parser.errors.errors) {
                msg.append(error).append("\n");
            }
            throw new SLException(msg.toString());
        }
    }
} // end Parser

class Errors {

    protected final List<String> errors = new ArrayList<>();
    public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text

    protected void printMsg(int line, int column, String msg) {
        StringBuffer b = new StringBuffer(errMsgFormat);
        int pos = b.indexOf("{0}");
        if (pos >= 0) {
            b.delete(pos, pos + 3);
            b.insert(pos, line);
        }
        pos = b.indexOf("{1}");
        if (pos >= 0) {
            b.delete(pos, pos + 3);
            b.insert(pos, column);
        }
        pos = b.indexOf("{2}");
        if (pos >= 0)
            b.replace(pos, pos + 3, msg);
        errors.add(b.toString());
    }

    public void SynErr(int line, int col, int n) {
        String s;
        switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "identifier expected"; break;
			case 2: s = "stringLiteral expected"; break;
			case 3: s = "numericLiteral expected"; break;
			case 4: s = "\"function\" expected"; break;
			case 5: s = "\"(\" expected"; break;
			case 6: s = "\",\" expected"; break;
			case 7: s = "\")\" expected"; break;
			case 8: s = "\"{\" expected"; break;
			case 9: s = "\"}\" expected"; break;
			case 10: s = "\"break\" expected"; break;
			case 11: s = "\";\" expected"; break;
			case 12: s = "\"continue\" expected"; break;
			case 13: s = "\"while\" expected"; break;
			case 14: s = "\"if\" expected"; break;
			case 15: s = "\"else\" expected"; break;
			case 16: s = "\"return\" expected"; break;
			case 17: s = "\"||\" expected"; break;
			case 18: s = "\"&&\" expected"; break;
			case 19: s = "\"<\" expected"; break;
			case 20: s = "\"<=\" expected"; break;
			case 21: s = "\">\" expected"; break;
			case 22: s = "\">=\" expected"; break;
			case 23: s = "\"==\" expected"; break;
			case 24: s = "\"!=\" expected"; break;
			case 25: s = "\"+\" expected"; break;
			case 26: s = "\"-\" expected"; break;
			case 27: s = "\"*\" expected"; break;
			case 28: s = "\"/\" expected"; break;
			case 29: s = "\"[\" expected"; break;
			case 30: s = "\"]\" expected"; break;
			case 31: s = "\"=\" expected"; break;
			case 32: s = "\".\" expected"; break;
			case 33: s = "??? expected"; break;
			case 34: s = "invalid Statement"; break;
			case 35: s = "invalid Factor"; break;
			case 36: s = "invalid Factor"; break;
			case 37: s = "invalid MemberExpression"; break;
            default:
                s = "error " + n;
                break;
        }
        printMsg(line, col, s);
    }

    public void SemErr(int line, int col, String s) {
        printMsg(line, col, s);
    }

    public void SemErr(String s) {
        errors.add(s);
    }

    public void Warning(int line, int col, String s) {
        printMsg(line, col, s);
    }

    public void Warning(String s) {
        errors.add(s);
    }
} // Errors

class FatalError extends RuntimeException {

    public static final long serialVersionUID = 1L;

    public FatalError(String s) {
        super(s);
    }
}
