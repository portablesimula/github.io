/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.statement;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.constant.MethodTypeDesc;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.Parameter;
import simula.compiler.syntaxClass.declaration.SimpleVariableDeclaration;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.syntaxClass.expression.TypeConversion;
import simula.compiler.syntaxClass.expression.VariableExpression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

// ************************************************************************************
// *** ForListElement -- While Element
// ************************************************************************************
/// Utility class: For-list While element.
///
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/ForWhileElement.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class ForWhileElement extends ForListElement {
	
	/// The second expression.
	Expression expr2;

	/// Create a new WhileElement.
	/// @param forStatement the ForStatement
	/// @param expr1 first expression 
	/// @param expr2 second expression
	public ForWhileElement(final ForStatement forStatement, final Expression expr1, final Expression expr2) {
		super(forStatement, expr1);
		this.expr2 = expr2;
	}

	@Override
	public void doChecking() {
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("BEGIN WhileElement(" + this + ").doChecking - Current Scope Chain: " + Global.getCurrentScope().edScopeChain());
		expr1.doChecking();
		expr2.doChecking();
		expr2.backLink = forStatement; // To ensure _RESULT from functions
		if (expr2.type == null || expr2.type.keyWord != Type.T_BOOLEAN)
			Util.error("While condition is not Boolean but " + expr2.type);				
		expr1 = TypeConversion.testAndCreate(forStatement.controlVariable.type, expr1);
		expr1.backLink = forStatement; // To ensure _RESULT from functions
//		expr2.backLink = forStatement; // To ensure _RESULT from functions
	}

	@Override
	public String edCode(final String classIdent, Type xType) {
		String forElt = (forStatement.controlVariable.type.keyWord == Type.T_TEXT
				&& forStatement.assignmentOperator == KeyWord.ASSIGNVALUE) ? "TValElt" : "Elt<" + classIdent + ">";
		return ("new FOR_While" + forElt + "(" + forStatement.edControlVariableByName(classIdent, xType) + ",new RTS_NAME<"
				+ classIdent + ">() { public " + classIdent + " get(){return(" + expr1.toJavaCode() + "); }}"
				+ ",new RTS_NAME<Boolean>() { public Boolean get(){return(" + expr2.toJavaCode() + "); }})");
	}

	@Override
	public ForListElement isOptimisable() {
		return (this);
	}

	@Override
	public void doSimplifiedJavaCoding() {
		String cv = forStatement.controlVariable.toJavaCode();
		String cond = this.expr2.toJavaCode();
		// ------------------------------------------------------------
		// cv=expr1; while(cond) { Statements ... cv=expr1; }
		// ------------------------------------------------------------
		JavaSourceFileCoder.code(cv + "=" + this.expr1.toJavaCode() + ";");
		JavaSourceFileCoder.code("while(" + cond + ") {");
		forStatement.doStatement.doJavaCoding();
		JavaSourceFileCoder.code(cv + "=" + this.expr1.toJavaCode() + ";");
		JavaSourceFileCoder.code("}");
	}

	@Override
	public void doSingleElementByteCoding(CodeBuilder codeBuilder) {
		
		// V while B REPEAT: C:= V;
		//                   if not B then goto END;
		//                   S;
		//                   goto REPEAT; 
		//           END:
		//                 ... next statement
		Label repeatLabel = codeBuilder.newLabel();
		Label endLabel = codeBuilder.newLabel();
		SimpleVariableDeclaration decl = (SimpleVariableDeclaration)forStatement.controlVariable.meaning.declaredAs;
		FieldRefEntry FRE=decl.getFieldRefEntry(codeBuilder.constantPool());

		codeBuilder.labelBinding(repeatLabel);
		// controlVariable := expr1
		forStatement.controlVariable.buildIdentifierAccess(true, codeBuilder);
		this.expr1.buildEvaluation(null,codeBuilder); // evaluate expr1
		codeBuilder.putfield(FRE);
		
		this.expr2.buildEvaluation(null,codeBuilder); // evaluate condition
		codeBuilder
			.iconst_1()
			.if_icmpne(endLabel);
		forStatement.doStatement.buildByteCode(codeBuilder);
		codeBuilder
			.goto_(repeatLabel)
			.labelBinding(endLabel);
	}

	@Override
	public void buildByteCode(CodeBuilder codeBuilder,VariableExpression controlVariable) {
		codeBuilder
			.new_(RTS.CD.FOR_WhileElt)
			.dup();
		Parameter.buildNameParam(codeBuilder,controlVariable);
		Parameter.buildNameParam(codeBuilder,expr1); // PARAMETER: RTS_NAME<T> expr
		Parameter.buildNameParam(codeBuilder,expr2); // PARAMETER: RTS_NAME<T> cond

		MethodTypeDesc MTD=MethodTypeDesc.ofDescriptor(
				"(Lsimula/runtime/RTS_NAME;Lsimula/runtime/RTS_NAME;Lsimula/runtime/RTS_NAME;)V");
		codeBuilder.invokespecial(RTS.CD.FOR_WhileElt, "<init>", MTD); // Invoke Constructor
	}
	
	public String toString() {
		return ("" + expr1 + " while " + expr2);
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private ForWhileElement() {}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("ForWhileElement: " + this);
		oupt.writeKind(ObjectKind.ForWhileElement);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** ForListElement
		oupt.writeObj(forStatement);
		oupt.writeObj(expr1);
		oupt.writeObj(expr2);
	}
	
	/// Read and return a ForWhileElement object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the ForWhileElement object read from the stream.
	/// @throws IOException if something went wrong.
	public static ForWhileElement readObject(AttributeInputStream inpt) throws IOException {
		ForWhileElement elt = new ForWhileElement();
		elt.OBJECT_SEQU = inpt.readSEQU(elt);
		// *** SyntaxClass
		elt.lineNumber = inpt.readShort();
		// *** ForListElement
		elt.forStatement = (ForStatement) inpt.readObj();
		elt.expr1 = (Expression) inpt.readObj();
		elt.expr2 = (Expression) inpt.readObj();
		Util.TRACE_INPUT("ForWhileElement: " + elt);
		return(elt);
	}

}
