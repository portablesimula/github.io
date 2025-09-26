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
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.Parameter;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.syntaxClass.expression.VariableExpression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Goto Statement.
/// 
/// <pre>
/// 
/// Simula Standard: 4.5 Goto-statement
/// 
///  goto-statement = GOTO designational-expression
///                 | GO TO designational-expression
/// 
/// </pre>
/// Java does not support labels like Simula. The Java Virtual Machine (JVM), however, has labels.
/// A JVM-label is simply a relative byte-address within the byte-code of a method. We will use Java's
/// exception handling together with byte code engineering to re-introduce goto in the Java Language.
/// This is done by generating Java-code which is prepared for Byte Code Engineering.
/// 
/// See <a href="https://portablesimula.github.io/github.io/doc/SimulaRTS.pdf">Mapping Simula to Java (runtime design)</a> 
/// Sect. 6.1 Goto Statement
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/GotoStatement.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class GotoStatement extends Statement {
	/// The target label.
	private Expression label;

	/// Create a new GotoStatement.
	/// @param line source line
	GotoStatement(final int line) {
		super(line);
		label = Expression.expectExpression();
		if (Option.internal.TRACE_PARSE) Util.TRACE("Line "+this.lineNumber+": GotoStatement: "+this);
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		label.doChecking();
		if (label.type == null || label.type.keyWord != Type.T_LABEL)
			Util.error("Goto " + label + ", " + label + " is not a Label");
		label.backLink = this; // To ensure _RESULT from functions
		SET_SEMANTICS_CHECKED();
	}

	@Override
	public void doJavaCoding() {
		Global.sourceLineNumber = lineNumber;
		ASSERT_SEMANTICS_CHECKED();
  		Type type = label.type;
		Util.ASSERT(type.keyWord == Type.T_LABEL, "Invariant");
		JavaSourceFileCoder.code("_GOTO(" + label.toJavaCode() + ");","GOTO EVALUATED LABEL");
	}
	
	@Override
	public void buildByteCode(CodeBuilder codeBuilder) {
		if(! labelIsParameterProcedure()) codeBuilder.aload(0);
		label.buildEvaluation(null,codeBuilder);
		RTS.invokevirtual_RTS_GOTO(codeBuilder);
	}
	
	/// Check if label is a parameter procedure.
	/// @return true: if label is a parameter procedure.
	private boolean labelIsParameterProcedure() {
		if(label instanceof VariableExpression var) {
			Meaning meaning = var.meaning;
			if(meaning.declaredAs instanceof Parameter par) {
				if(par.kind == Parameter.Kind.Procedure)
					return true;
			}
		}
		return false;
	}

	@Override
	public void printTree(final int indent, final Object head) {
		IO.println(edTreeIndent(indent)+"GOTO "+label);
		label.printTree(indent+1,this);
	}

	@Override
	public String toString() {
		return ("GOTO " + label);
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	public GotoStatement() {
		super(0);
	}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeGotoStatement: " + this);
		oupt.writeKind(ObjectKind.GotoStatement);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** GotoStatement
		oupt.writeObj(label);
	}

	/// Read and return a GotoStatement object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the GotoStatement object read from the stream.
	/// @throws IOException if something went wrong.
	public static GotoStatement readObject(AttributeInputStream inpt) throws IOException {
		GotoStatement stm = new GotoStatement();
		stm.OBJECT_SEQU = inpt.readSEQU(stm);
		// *** SyntaxClass
		stm.lineNumber = inpt.readShort();
		// *** GotoStatement
		stm.label = (Expression) inpt.readObj();
		Util.TRACE_INPUT("GotoStatement: " + stm);
		return(stm);
	}

}
