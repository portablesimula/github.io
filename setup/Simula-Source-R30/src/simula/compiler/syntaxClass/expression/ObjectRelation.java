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
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Object relations IS and IN.
/// 
/// <pre>
/// Simula Standard: 3.3.4. Object relations
/// 
///   object-relation
///        =  simple-object-expression  IS  class-identifier
///        |  simple-object-expression  IN  class-identifier
/// </pre>
/// 
/// The operators IS and IN may be used to test the class membership of an
/// object.
/// 
/// The relation "X IS C" has the value true if X refers to an object belonging
/// to the class C, otherwise the value is false.
/// 
/// The relation "X IN C" has the value true if X refers to an object belonging
/// to a class C or a class inner to C, otherwise the value is false.
/// 
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
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/ObjectRelation.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class ObjectRelation extends Expression {
	
	/// The left hand side.
	private Expression lhs;
	
	/// The operation: IN or IS
	private int opr; 
	
	/// The right hand class identifier.
	private String classIdentifier;
	
	/// The class declaration.
	/// Set by doChecking.
	ClassDeclaration classDeclaration;

	/// Create a new ObjectRelation
	/// @param lhs left hand side
	/// @param opr the operation: IN or IS
	/// @param classIdentifier the right hand class identifier
	ObjectRelation(final Expression lhs, final int opr, final String classIdentifier) {
		this.lhs = lhs;
		this.opr = opr;
		this.classIdentifier = classIdentifier;
		lhs.backLink = this;
	}
  
	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())
			return;
		Global.sourceLineNumber = lineNumber;
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("BEGIN ObjectRelation" + toString() + ".doChecking - Current Scope Chain: " + Global.getCurrentScope().edScopeChain());
		classDeclaration = getQualification(classIdentifier);
		// Object IS ClassIdentifier | Object IN ClassIdentifier
		lhs.doChecking();
		Type type1 = lhs.type;
		String refIdent = type1.getRefIdent();
		if (refIdent == null)
			Util.warning("NONE IS/IN " + classIdentifier + " -- Rewrite program");
		this.type = Type.Boolean;
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("END ObjectRelation" + toString() + ".doChecking - Result type=" + this.type);
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
		String refIdent = lhs.type.getRefIdent();
		if (refIdent == null) return ("false"); // NONE IS/IN Any is always FALSE
		if (opr == KeyWord.IN) {
			if (!checkCompatibility(lhs, classIdentifier)) return ("false"); // warning("IN is always FALSE
			return (lhs.get() + KeyWord.toJavaCode(opr) + classDeclaration.getJavaIdentifier());
		} else if (opr == KeyWord.IS) {
			if (!checkCompatibility(lhs, classIdentifier)) return ("false"); // warning("IS is always FALSE
			return ("RTS_UTIL._IS(" + lhs.get() + "," + classDeclaration.getJavaIdentifier() + ".class)");
		} else {
			Util.IERR();
			return ("");
		}
	}

	@Override
	public void buildEvaluation(Expression rightPart,CodeBuilder codeBuilder) {
		setLineNumber();
		ASSERT_SEMANTICS_CHECKED();
		if(opr == KeyWord.IS) {
			lhs.buildEvaluation(null,codeBuilder);
			RTS.invokestatic_UTIL_IS(classDeclaration.getClassDesc(), codeBuilder);
		} else if(opr == KeyWord.IN) {
			lhs.buildEvaluation(null,codeBuilder);
			codeBuilder.instanceOf(classDeclaration.getClassDesc());
		} else Util.IERR();
	}

	@Override
	public String toString() {
		return ("(" + lhs + ' ' + KeyWord.edit(opr) + ' ' + classIdentifier + ")");
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private ObjectRelation() {}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeObjectRelation: " + this);
		oupt.writeKind(ObjectKind.ObjectRelation);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** Expression
		oupt.writeType(type);
		oupt.writeObj(backLink);
		// *** ArithmeticExpression
		oupt.writeObj(lhs);
		oupt.writeShort(opr);
		oupt.writeString(classIdentifier);
	}
	
	/// Read and return a ObjectRelation object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the ObjectRelation object read from the stream.
	/// @throws IOException if something went wrong.
	public static ObjectRelation readObject(AttributeInputStream inpt) throws IOException {
		ObjectRelation expr = new ObjectRelation();
		expr.OBJECT_SEQU = inpt.readSEQU(expr);
		expr.lineNumber = inpt.readShort();
		expr.type = inpt.readType();
		expr.backLink = (SyntaxClass) inpt.readObj();
		expr.lhs = (Expression) inpt.readObj();
		expr.opr = inpt.readShort();
		expr.classIdentifier = inpt.readString();
		Util.TRACE_INPUT("readObjectRelation: " + expr);
		return(expr);
	}

}
