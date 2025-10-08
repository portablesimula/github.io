/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.expression;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.ClassDeclaration;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Qualified Object
/// 
/// <pre>
/// Simula Standard: 3.8 Object expressions
/// 
///   qualified-object
///        =  simple-object-expression  QUA  class-identifier
/// </pre>
/// 
/// The qualification of an object expression is defined by the following rules:
/// <ul>
/// <li>The expression none is qualified by a fictitious class which is inner to
/// all declared classes.
/// 
/// <li>A variable or function designator is qualified as stated in the
/// declaration (or specification, see below) of the variable or array or
/// procedure in question.
/// 
/// <li>An object generator, local object or qualified object is qualified by the
/// class of the identifier following the symbol new, this or qua respectively.
/// 
/// <li>A conditional object expression is qualified by the innermost class which
/// includes the qualifications of both alternatives. If there is no such class,
/// the expression is illegal.
/// 
/// <li>Any formal parameter of object reference type is qualified according to
/// its specification regardless of the qualification of the corresponding actual
/// parameter.
/// 
/// <li>The qualification of a function designator whose procedure identifier is
/// that of a virtual quantity depends on the access level (see 5.5.5). The
/// qualification is that of the matching declaration, if any, occurring at the
/// innermost prefix level equal or outer to the access level, or, if no such
/// match exists, it is that of the virtual specification.
/// </ul>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/QualifiedObject.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class QualifiedObject extends Expression {
	
	/// The left hand simple-object-expression 
	private Expression lhs;
	
	/// The right hand class identifier.
	private String classIdentifier;
	
	/// The right hand class declaration. Set by doChecking.
	ClassDeclaration classDeclaration; // Set by doChecking

	/// Create a new QualifiedObject
	/// @param lhs left hand side
	/// @param classIdentifier class identifier
	QualifiedObject(final Expression lhs, final String classIdentifier) {
		this.lhs = lhs;
		this.classIdentifier = classIdentifier;
		lhs.backLink = this;
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		Global.sourceLineNumber = lineNumber;
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("BEGIN QualifiedObject" + toString() + ".doChecking - Current Scope Chain: "	+ Global.getCurrentScope().edScopeChain());
		classDeclaration = getQualification(classIdentifier);
		lhs.doChecking();
		if (!checkCompatibility(lhs, classIdentifier))
			Util.error("Illegal Object Expression: " + lhs + " is not compatible with " + classIdentifier);
		this.type = new Type(classIdentifier);
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("END QualifiedObject" + toString() + ".doChecking - Result type=" + this.type);
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
		return("((" + classDeclaration.getJavaIdentifier() + ")(" + lhs.get() + "))");
	}

	@Override
	public void buildEvaluation(Expression rightPart,CodeBuilder codeBuilder) {	setLineNumber();
		ASSERT_SEMANTICS_CHECKED();
		lhs.buildEvaluation(null,codeBuilder);
		codeBuilder.checkcast(classDeclaration.getClassDesc());
	}

	@Override
	public String toString() {
		return ("(" + lhs + " QUA " + classIdentifier + ")");
	}

	// ***********************************************************************************************
	// *** Externalization
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private QualifiedObject() {}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeQualifiedObject: " + this);
		oupt.writeKind(ObjectKind.QualifiedObject);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** Expression
		oupt.writeType(type);
		oupt.writeObj(backLink);
		// *** QualifiedObject
		oupt.writeObj(lhs);
		oupt.writeString(classIdentifier);
	}
	
	/// Read and return a QualifiedObject object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the QualifiedObject object read from the stream.
	/// @throws IOException if something went wrong.
	public static QualifiedObject readObject(AttributeInputStream inpt) throws IOException {
		QualifiedObject expr = new QualifiedObject();
		expr.OBJECT_SEQU = inpt.readSEQU(expr);
		// *** SyntaxClass
		expr.lineNumber = inpt.readShort();
		// *** Expression
		expr.type = inpt.readType();
		expr.backLink = (SyntaxClass) inpt.readObj();
		// *** QualifiedObject
		expr.lhs = (Expression) inpt.readObj();
		expr.classIdentifier = inpt.readString();
		Util.TRACE_INPUT("readQualifiedObject: " + expr);
		return(expr);
	}

}
