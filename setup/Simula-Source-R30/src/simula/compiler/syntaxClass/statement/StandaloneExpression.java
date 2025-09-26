/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.statement;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.expression.AssignmentOperation;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Standalone Expression Statement.
/// 
/// <pre>
/// 
/// Syntax:
/// 
///   standalone-expression = expression | assignment-statement
/// 
///      assignment-statement
///           = expression { assignment-operator expression }
/// 
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/StandaloneExpression.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class StandaloneExpression extends Statement {
	
	/// The expression.
	private Expression expression;

	/// Create a new StandaloneExpression.
	/// @param line the source line number
	/// @param expression the expression
	StandaloneExpression(final int line,final Expression expression) {
		super(line);
		this.expression = expression;
		if (Option.internal.TRACE_PARSE) Util.TRACE("Line "+lineNumber+": StandaloneExpression: "+this);
		while (Parse.accept(KeyWord.ASSIGNVALUE,KeyWord.ASSIGNREF)) { 
			this.expression = new AssignmentOperation(this.expression, Parse.prevToken.getKeyWord(),expectStandaloneExpression());
		}		
	}

	/// Parse a standalone expression.
	/// <pre>
	/// Syntax:
	/// 
	///    standalone-expression  =  expression  { assignment-operator  expression }
	/// </pre>
	/// Pre-Condition: First expression is already read.
	/// @return the resulting StandaloneExpression
	private static Expression expectStandaloneExpression() { 
		Expression retExpr=Expression.expectExpression();
		while (Parse.accept(KeyWord.ASSIGNVALUE,KeyWord.ASSIGNREF)) {
			int opr=Parse.prevToken.getKeyWord();
			retExpr=new AssignmentOperation(retExpr,opr,expectStandaloneExpression());
		}
		return retExpr;
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		Global.sourceLineNumber=lineNumber;
		if (Option.internal.TRACE_CHECKER) Util.TRACE("StandaloneExpression("+expression+").doChecking - Current Scope Chain: "+Global.getCurrentScope().edScopeChain());
		expression.doChecking();
		if(!expression.maybeStatement()) Util.error("Illegal/Missplaced Expression: "+expression);
		if (Option.internal.TRACE_CHECKER) Util.TRACE("END StandaloneExpression(" + expression+ ").doChecking:");
		SET_SEMANTICS_CHECKED();
	}
	
	@Override
	public void doJavaCoding() {
		Global.sourceLineNumber=lineNumber;
		JavaSourceFileCoder.code(toJavaCode() + ';');
	}

	@Override
	public String toJavaCode() {
		ASSERT_SEMANTICS_CHECKED();
		String result=expression.toJavaCode();
		return (result);
	}

	@Override
	public void buildByteCode(CodeBuilder codeBuilder) {
		expression.buildEvaluation(null,codeBuilder);
	}

	@Override
	public void print(final int indent) {
		expression.print(indent);
	}
	
	@Override
	public void printTree(final int indent, final Object head) {
		expression.printTree(indent,this);
	}

	@Override
	public String toString() {
		return ("STANDALONE " + expression);
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private StandaloneExpression() {
		super(0);
	}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeStandaloneExpression: " + this);
		oupt.writeKind(ObjectKind.StandaloneExpression);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** StandaloneExpression
		oupt.writeObj(expression);
	}

	/// Read and return a StandaloneExpression object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the StandaloneExpression object read from the stream.
	/// @throws IOException if something went wrong.
	public static StandaloneExpression readObject(AttributeInputStream inpt) throws IOException {
		StandaloneExpression stm = new StandaloneExpression();
		stm.OBJECT_SEQU = inpt.readSEQU(stm);
		// *** SyntaxClass
		stm.lineNumber = inpt.readShort();
		// *** StandaloneExpression
		stm.expression = (Expression) inpt.readObj();
		Util.TRACE_INPUT("StandaloneExpression: " + stm);
		return(stm);
	}

}
