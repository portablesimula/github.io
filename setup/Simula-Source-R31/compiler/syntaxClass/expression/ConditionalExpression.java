/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.expression;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Conditional Expression.
/// 
/// <pre>
/// 
/// Syntax: 
/// 
///   conditional-expression
///       = IF Boolean-expression THEN simple-expression ELSE expression
/// 
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/ConditionalExpression.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class ConditionalExpression extends Expression {
	
	/// The condition.
	Expression condition;
	
	/// The then branch expression
	Expression thenExpression;
	
	/// The else branch expression
	Expression elseExpression;

	/// Create a new ConditionalExpression.
	/// @param type expression's type
	/// @param condition the condition
	/// @param thenExpression then branch expression
	/// @param elseExpression else branch expression
	ConditionalExpression(final Type type, final Expression condition, final Expression thenExpression, final Expression elseExpression) {
		this.condition = condition;
		this.thenExpression = thenExpression; thenExpression.backLink=this;
		this.elseExpression = elseExpression; elseExpression.backLink=this;
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("NEW ConditionalExpression: " + toString());
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		Global.sourceLineNumber=lineNumber;
		condition.doChecking();
		condition.backLink=this; // To ensure _RESULT from functions
		Type cType = condition.type;
		if (cType.keyWord != Type.T_BOOLEAN)
			Util.error("ConditionalExpression: Condition is not a boolean (rather " + cType + ")");
		thenExpression.doChecking();
		elseExpression.doChecking();
		Type expectedType=Type.commonTypeConversion(thenExpression.type,elseExpression.type);
		thenExpression = TypeConversion.testAndCreate(expectedType, thenExpression);
		elseExpression = TypeConversion.testAndCreate(expectedType, elseExpression);
		thenExpression.doChecking(); // In case TypeConversion was added
		elseExpression.doChecking(); // In case TypeConversion was added
		this.type=expectedType;
		SET_SEMANTICS_CHECKED();
	}

	// Returns true if this expression may be used as a statement.
	@Override
	public boolean maybeStatement() {
		ASSERT_SEMANTICS_CHECKED();
		return (false);
	}

	@Override
	public String toJavaCode() {
		ASSERT_SEMANTICS_CHECKED();
		return ("((" + condition.get() + ")?("
				+ thenExpression.get() + "):("
				+ elseExpression.get() + "))");
	}

	@Override
	public void buildEvaluation(Expression rightPart,CodeBuilder codeBuilder) {	setLineNumber();
		ASSERT_SEMANTICS_CHECKED();
		condition.buildEvaluation(null,codeBuilder);
		Label elseLabel = codeBuilder.newLabel();
		codeBuilder.ifeq(elseLabel);
		thenExpression.buildEvaluation(null,codeBuilder);
		if(elseExpression != null) {
			Label endLabel = codeBuilder.newLabel();
			codeBuilder.goto_(endLabel);
			codeBuilder.labelBinding(elseLabel);
			elseExpression.buildEvaluation(null,codeBuilder);
			codeBuilder.labelBinding(endLabel);
		} else codeBuilder.labelBinding(elseLabel);
	}

	@Override
	public String toString() {
		return ("(IF " + condition + " THEN " + thenExpression + " ELSE "
				+ elseExpression + ')');
	}


	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private ConditionalExpression() {}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeConditionalExpression: " + this);
		oupt.writeKind(ObjectKind.ConditionalExpression);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** Expression
		oupt.writeType(type);
		oupt.writeObj(backLink);
		// *** ConditionalExpression
		oupt.writeObj(condition);
		oupt.writeObj(thenExpression);
		oupt.writeObj(elseExpression);
	}
	
	/// Read and return a ConditionalExpression.
	/// @param inpt the AttributeInputStream to read from
	/// @return the ConditionalExpression read from the stream.
	/// @throws IOException if something went wrong.
	public static ConditionalExpression readObject(AttributeInputStream inpt) throws IOException {
		ConditionalExpression expr = new ConditionalExpression();
		expr.OBJECT_SEQU = inpt.readSEQU(expr);
		// *** SyntaxClass
		expr.lineNumber = inpt.readShort();
		// *** Expression
		expr.type = inpt.readType();
		expr.backLink = (SyntaxClass) inpt.readObj();
		// *** ConditionalExpression
		expr.condition = (Expression) inpt.readObj();
		expr.thenExpression = (Expression) inpt.readObj();
		expr.elseExpression = (Expression) inpt.readObj();
		Util.TRACE_INPUT("readConditionalExpression: " + expr);
		return(expr);
	}

}
