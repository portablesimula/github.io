/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.statement;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Conditional Statement.
/// 
/// <pre>
/// 
/// Simula Standard: 4.2 Conditional statement
/// 
///   conditional-statement = if-clause { Label : } for-statement
///                         | if-clause { Label : } unconditional-statement  [ ELSE statement ]
///                         
///     if-clause = IF boolean-expression THEN
/// 
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/ConditionalStatement.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class ConditionalStatement extends Statement {
	
	/// The if-clause condition
	private Expression condition;
	
	/// The then-statement
	private Statement thenStatement;
	
	/// The else-statement
	private Statement elseStatement;

	/// Create a new ConditionalStatement.
	/// @param line the source line number
	ConditionalStatement(final int line) {
		super(line);
		condition = Expression.expectExpression();
		Parse.expect(KeyWord.THEN);
		Statement elseStatement = null;
		if (Parse.accept(KeyWord.ELSE)) {
			thenStatement = new DummyStatement(Parse.currentToken.lineNumber);
			elseStatement = Statement.expectStatement();
		} else {
		    thenStatement = Statement.expectStatement();
		    if (Parse.accept(KeyWord.ELSE)) {
			    elseStatement = Statement.expectStatement();
		    }
		}
		this.elseStatement=elseStatement;
		if (Option.internal.TRACE_PARSE) Util.TRACE("Line "+lineNumber+": IfStatement: "+this);
	}

	@Override
	public void print(final int indent) {
    	String spc=edIndent(indent);
		StringBuilder s = new StringBuilder(spc);
		s.append("IF ").append(condition);
		Util.println(s.toString());
		Util.println(spc + "THEN ");
		thenStatement.print(indent + 1);
		if (elseStatement != null) {
			Util.println(spc + "ELSE ");
			elseStatement.print(indent + 1);
		}
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
//		IO.println("ConditionalStatement.doChecking: " + condition.getClass().getSimpleName() + "  " + this);
		condition.doChecking();
		condition.backLink=this; // To ensure _RESULT from functions
		if (condition.type == null || condition.type.keyWord != Type.T_BOOLEAN)
			Util.error("ConditionalStatement.doChecking: Condition is not of Type Boolean, but: " + condition.type);
		thenStatement.doChecking();
		if (elseStatement != null) {
			elseStatement.doChecking();
		}
		SET_SEMANTICS_CHECKED();
	}
	
	@Override
	public void doJavaCoding() {
		Global.sourceLineNumber=lineNumber;
		ASSERT_SEMANTICS_CHECKED();
		JavaSourceFileCoder.code("if(_VALUE(" + condition.toJavaCode() + ")) {");
		thenStatement.doJavaCoding();
		if (elseStatement != null) {
			JavaSourceFileCoder.code("} else {");
			elseStatement.doJavaCoding();
			JavaSourceFileCoder.code("}");
		} else
			JavaSourceFileCoder.code("}");
	}

	@Override
	public void buildByteCode(CodeBuilder codeBuilder) {
		ASSERT_SEMANTICS_CHECKED();
		condition.buildEvaluation(null,codeBuilder);
		Label elseLabel = codeBuilder.newLabel();
		codeBuilder.ifeq(elseLabel);
		thenStatement.buildByteCode(codeBuilder);
		if(elseStatement != null) {
			Label endLabel = codeBuilder.newLabel();
			codeBuilder.goto_(endLabel);
			codeBuilder.labelBinding(elseLabel);
			elseStatement.buildByteCode(codeBuilder);
			codeBuilder.labelBinding(endLabel);
		} else codeBuilder.labelBinding(elseLabel);
	}
	
	@Override
	public void printTree(final int indent, final Object head) {
		IO.println(edTreeIndent(indent)+"IF " + condition + " THEN");
		thenStatement.printTree(indent+1,this);
		if(elseStatement != null) {
			IO.println(edTreeIndent(indent)+"ELSE");
			elseStatement.printTree(indent+1,this);
		}
	}

	@Override
	public String toString() {
		return ("IF " + condition + " THEN " + thenStatement + " ELSE "
				+ elseStatement + ';');
	}
	

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private ConditionalStatement() {
		super(0);
	}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeConditionalStatement: " + this);
		oupt.writeKind(ObjectKind.ConditionalStatement);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** ConditionalStatement
		oupt.writeObj(condition);
		oupt.writeObj(thenStatement);
		oupt.writeObj(elseStatement);
	}

	/// Read and return a ConditionalStatement object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the ConditionalStatement object read from the stream.
	/// @throws IOException if something went wrong.
	public static ConditionalStatement readObject(AttributeInputStream inpt) throws IOException {
		ConditionalStatement stm = new ConditionalStatement();
		stm.OBJECT_SEQU = inpt.readSEQU(stm);
		// *** SyntaxClass
		stm.lineNumber = inpt.readShort();
		// *** ConditionalStatement
		stm.condition = (Expression) inpt.readObj();
		stm.thenStatement = (Statement) inpt.readObj();
		stm.elseStatement = (Statement) inpt.readObj();

		Util.TRACE_INPUT("ConditionalStatement: " + stm);
		return(stm);
	}

}
