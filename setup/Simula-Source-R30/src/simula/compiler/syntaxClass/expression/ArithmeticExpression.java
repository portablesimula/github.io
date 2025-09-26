/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.expression;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Arithmetic expressions
/// 
/// <pre>
/// Simula Standard:  3.5 Arithmetic expressions.
///
///    arithmetic-expression
///        =  simple-arithmetic-expression
///        |  if-clause  simple-arithmetic-expression
///           else  arithmetic-expression
/// 
///    simple-arithmetic-expression
///        =  [ + | - ]  term  {  ( + | - )  term }
/// 
///    term
///        =  factor  {  ( * | / | // )  factor }
/// 
///    factor
///        =  primary  { **  primary }
/// 
///    primary
///        =  unsigned-number
///        |  variable
///        |  function-designator
///        |  "("  arithmetic-expression  ")"
/// </pre>
/// 
/// An arithmetic expression is a rule for computing a numerical value. In the
/// case of simple arithmetic expressions this value is obtained by executing the
/// indicated arithmetic operations on the actual numerical values of the
/// primaries of the expression, as explained in detail in 3.5.1 below. The value
/// of a primary is obvious in the case of numbers. For variables it is the
/// current value (assigned last in the dynamic sense), and for function
/// designators it is the value arising from the computing rules defining the
/// procedure when applied to the current values of the procedure parameters
/// given in the expression. Finally, for arithmetic expressions enclosed by
/// parentheses the value must through a recursive analysis be expressed in terms
/// of the values of primaries of the other three kinds.
/// 
/// In the more general arithmetic expressions, which include if-clauses, one out
/// of several simple arithmetic expressions is selected on the basis of the
/// actual values of the Boolean expressions (see 3.2). This selection is made as
/// follows: The Boolean expressions of the if-clauses are evaluated one by one
/// in sequence from left to right until one having the value true is found. The
/// value of the arithmetic expression is then the value of the first arithmetic
/// expression following this Boolean (the longest arithmetic expression found in
/// this position is understood). If none of the Boolean expressions has the
/// value true, then the value of the arithmetic expression is the value of the
/// expression following the final else.
/// 
/// In evaluating an arithmetic expression, all primaries within that expression
/// are evaluated with the following exceptions:
/// <pre>
///  - Primaries that occur within any expression governed by an if-clause but not selected by it.
/// 
///  - Primaries that occur within a Boolean expression.
///    1) after the operator or else when the evaluation of a preceding Boolean tertiary results in false, or
///    2) after the operator and then when the evaluation of a preceding equivalence results in false.
///  
///  - Primaries that occur after a function designator, and the evaluation of
///    the function terminates with a goto-statement. In this case the evaluation of
///    the arithmetic expression is abandoned.
/// </pre>   
/// Primaries are always evaluated in strict lexical order.
/// 
/// NOTE: The implementation of EXP '**' deviates from the definition in Simula Standard.
///   It is always evaluated in long real and the result is converted to the appropriate type. 
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/ArithmeticExpression.java">
/// <b>Source File</b></a>.
/// 
/// @author Simula Standard
/// @author Øystein Myhre Andersen
public final class ArithmeticExpression extends Expression {

	/// The left hand side
	private Expression lhs;

	/// The arithmetic operation
	private int opr;

	/// The right hand side
	private Expression rhs;

	/// Create a new ArithmeticExpression.
	/// @param lhs left hand side
	/// @param opr arithmetic operation
	/// @param rhs right hand side
	private ArithmeticExpression(final Expression lhs, final int opr, final Expression rhs) {
		this.opr = opr;
		if (lhs == null) {
			Util.error("Missing operand before " + KeyWord.edit(opr));
			this.lhs = new VariableExpression("UNKNOWN_");
		} else
			this.lhs = lhs;
		if (rhs == null) {
			Util.error("Missing operand after " + KeyWord.edit(opr));
			this.rhs = new VariableExpression("UNKNOWN_");
		} else
			this.rhs = rhs;
		this.lhs.backLink = this.rhs.backLink = this;
	}

	/// Create a new ArithmeticExpression.
	/// @param lhs the left hand side
	/// @param opr the arithmetic operation
	/// @param rhs the right hand side
	/// @return the newly created ArithmeticExpression
	static Expression create(final Expression lhs, final int opr, final Expression rhs) {
		try { // Try to Compile-time Evaluate this expression
			Number lhn = lhs.getNumber();
			if (lhn != null) {
				Number rhn = rhs.getNumber();
				if (rhn != null)
					return (Constant.evaluate(lhn, opr, rhn));
			}
		} catch (Exception e) {
			Util.error("Arithmetic overflow: " + lhs + ' ' + KeyWord.edit(opr) + ' ' + rhs + "   " + e);
			e.printStackTrace();
		}
		return (new ArithmeticExpression(lhs, opr, rhs));
	}

	@Override
	public Expression evaluate() {
		// Try to Compile-time Evaluate this expression
		Number lhn = lhs.getNumber();
		if (lhn != null) {
			Number rhn = rhs.getNumber();
			if (rhn != null)
				return (Constant.evaluate(lhn, opr, rhn));
		}
		return (this);
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())
			return;
		Global.sourceLineNumber = lineNumber;
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("BEGIN ArithmeticOperation" + toString() + ".doChecking - Current Scope Chain: "
					+ Global.getCurrentScope().edScopeChain());
		switch (opr) {
			case KeyWord.PLUS, KeyWord.MINUS, KeyWord.MUL -> {
				// ArithmeticExpression
				lhs.doChecking();
				rhs.doChecking();
				Type type1 = lhs.type;
				Type type2 = rhs.type;
				this.type = Type.arithmeticTypeConversion(type1, type2);
				lhs = (Expression) TypeConversion.testAndCreate(this.type, lhs);
				rhs = (Expression) TypeConversion.testAndCreate(this.type, rhs);
				if (this.type == null)
					Util.error("Incompatible types in binary operation: " + toString());
			}
			case KeyWord.DIV -> { // Real Division
				// The operator / denotes real division.
				// Any operand of integer type is converted before the operation.
				// Division by zero constitutes an error.
				lhs.doChecking();
				rhs.doChecking();
				Type type1 = lhs.type;
				Type type2 = rhs.type;
				this.type = Type.arithmeticTypeConversion(type1, type2);
				if (this.type.keyWord == Type.T_INTEGER)
					this.type = Type.Real;
				lhs = (Expression) TypeConversion.testAndCreate(this.type, lhs);
				rhs = (Expression) TypeConversion.testAndCreate(this.type, rhs);
				if (this.type == null)
					Util.error("Incompatible types in binary operation: " + toString());
			}
			case KeyWord.INTDIV -> { // Integer Division
				lhs.doChecking();
				rhs.doChecking();
				if ((lhs.type.keyWord != Type.T_INTEGER) || (rhs.type.keyWord != Type.T_INTEGER))
					Util.error("Incompatible types in binary operation: " + toString());
				this.type = Type.Integer;
				lhs = (Expression) TypeConversion.testAndCreate(this.type, lhs);
				rhs = (Expression) TypeConversion.testAndCreate(this.type, rhs);
			}
			case KeyWord.EXP -> {
				lhs.doChecking();
				rhs.doChecking();
				if ((lhs.type.keyWord != Type.T_INTEGER) || (rhs.type.keyWord != Type.T_INTEGER)) {
					this.type = Type.LongReal; // Deviation from Simula Standard
					lhs = (Expression) TypeConversion.testAndCreate(this.type, lhs);
					rhs = (Expression) TypeConversion.testAndCreate(this.type, rhs);
				} else
					this.type = Type.Integer;
			}
			default -> Util.IERR();
		}
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("END ArithmeticOperation" + toString() + ".doChecking - Result type=" + this.type);
		SET_SEMANTICS_CHECKED();
	}

	// Returns true if this expression may be used as a statement.
	@Override
	public boolean maybeStatement() {
		ASSERT_SEMANTICS_CHECKED();
		return (false);
	}

	@Override
	public void buildEvaluation(Expression rightPart,CodeBuilder codeBuilder) {	setLineNumber();
		ASSERT_SEMANTICS_CHECKED();
		if(opr == KeyWord.EXP) {
			// Real:     r2=((float)(Math.pow(((double)(r1)),((double)(e)))));
			// LongReal: r2=Math.pow(r1,e);
			// Integer:  k=_IPOW(i,j);
			lhs.buildEvaluation(null,codeBuilder);
			if(type.keyWord == Type.T_INTEGER) {
				rhs.buildEvaluation(null,codeBuilder);
				RTS.invokestatic_UTIL_IPOW(codeBuilder);
			} else {
				if(type.keyWord == Type.T_REAL) codeBuilder.f2d();
				rhs.buildEvaluation(null,codeBuilder);
				if(type.keyWord == Type.T_REAL) codeBuilder.f2d();
				codeBuilder.invokestatic(RTS.CD.JAVA_LANG_MATH, "pow", MethodTypeDesc.ofDescriptor("(DD)D"));
				if(type.keyWord == Type.T_REAL) codeBuilder.d2f();
			}
			return;
		}
		lhs.buildEvaluation(null,codeBuilder);
		rhs.buildEvaluation(null,codeBuilder);
		switch(type.keyWord) {
			case Type.T_INTEGER -> {
				switch (opr) {
					case KeyWord.PLUS -> {
						// codeBuilder.iadd();
						RTS.invokestatic_UTIL_IADD(codeBuilder);
					}
					case KeyWord.MINUS -> {
						// codeBuilder.isub();
						RTS.invokestatic_UTIL_ISUB(codeBuilder);
					}
					case KeyWord.MUL -> {
						// codeBuilder.imul();
						RTS.invokestatic_UTIL_IMUL(codeBuilder);
					}
					case KeyWord.DIV ->    codeBuilder.idiv();
					case KeyWord.INTDIV -> codeBuilder.idiv();
					default -> Util.IERR();
				} }
			case Type.T_REAL -> {
				switch(opr) {
					case KeyWord.PLUS ->  codeBuilder.fadd();
					case KeyWord.MINUS -> codeBuilder.fsub();
					case KeyWord.MUL ->   codeBuilder.fmul();
					case KeyWord.DIV ->   codeBuilder.fdiv();
					default -> Util.IERR();
				} }
			case Type.T_LONG_REAL -> {
				switch(opr) {
					case KeyWord.PLUS ->  codeBuilder.dadd();
					case KeyWord.MINUS -> codeBuilder.dsub();
					case KeyWord.MUL ->   codeBuilder.dmul();
					case KeyWord.DIV ->   codeBuilder.ddiv();
					default -> Util.IERR();
				} }
			default -> Util.IERR();
		}
	}

	@Override
	public String toJavaCode() {
		ASSERT_SEMANTICS_CHECKED();
		if (this.type.keyWord == Type.T_INTEGER) {
			switch (opr) {
			case KeyWord.EXP:	 return ("RTS_UTIL._IPOW(" + lhs.get() + ',' + rhs.get() + ')');
			case KeyWord.PLUS:	 return ("RTS_UTIL._IADD(" + lhs.get() + ',' + rhs.get() + ')');					
			case KeyWord.MINUS:	 return ("RTS_UTIL._ISUB(" + lhs.get() + ',' + rhs.get() + ')');					
			case KeyWord.MUL:	 return ("RTS_UTIL._IMUL(" + lhs.get() + ',' + rhs.get() + ')');					
			default:
				if (this.backLink == null)
					 return (lhs.get() + KeyWord.toJavaCode(opr) + '(' + rhs.get() + ')');
				else return ("(" + lhs.get() + KeyWord.toJavaCode(opr) + '(' + rhs.get() + "))");
		}
			
		}
		switch (opr) {
			case KeyWord.EXP:
//				if (this.type.keyWord == Type.T_INTEGER)
//					 return ("RTS_UTIL._IPOW(" + lhs.get() + ',' + rhs.get() + ')');
//				else
					return ("Math.pow(" + lhs.get() + ',' + rhs.get() + ')');
			
//			case KeyWord.MUL:
//				if (this.type.keyWord == Type.T_INTEGER) {
//					 return ("RTS_UTIL._IMUL(" + lhs.get() + ',' + rhs.get() + ')');					
//				} // else fall thru to default handling.
			default:
				if (this.backLink == null)
					 return (lhs.get() + KeyWord.toJavaCode(opr) + '(' + rhs.get() + ')');
				else return ("(" + lhs.get() + KeyWord.toJavaCode(opr) + '(' + rhs.get() + "))");
		}
	}

	@Override
	public String toString() {
		return ("(" + lhs + ' ' + KeyWord.edit(opr) + ' ' + rhs + ")");
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private ArithmeticExpression() {}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeArithmeticExpression: " + this);
		oupt.writeKind(ObjectKind.ArithmeticExpression);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** Expression
		oupt.writeType(type);
		oupt.writeObj(backLink);
		// *** ArithmeticExpression
		oupt.writeObj(lhs);
		oupt.writeShort(opr);
		oupt.writeObj(rhs);
	}
	
	/// Read and return an ArithmeticExpression.
	/// @param inpt the AttributeInputStream to read from
	/// @return the ArithmeticExpression read from the stream.
	/// @throws IOException if something went wrong.
	public static ArithmeticExpression readObject(AttributeInputStream inpt) throws IOException {
		ArithmeticExpression expr = new ArithmeticExpression();
		expr.OBJECT_SEQU = inpt.readSEQU(expr);
		// *** SyntaxClass
		expr.lineNumber = inpt.readShort();
		// *** Expression
		expr.type = inpt.readType();
		expr.backLink = (SyntaxClass) inpt.readObj();
		// *** ArithmeticExpression
		expr.lhs = (Expression) inpt.readObj();
		expr.opr = inpt.readShort();
		expr.rhs = (Expression) inpt.readObj();
		Util.TRACE_INPUT("readArithmeticExpression: " + expr);
		return(expr);
	}

}
