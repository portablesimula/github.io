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
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Relational Operation.
/// 
/// <pre>
/// 
/// Simula Standard: 3.3 Relations
/// 
/// </pre>
/// 
/// <h2>Syntax used during Parsing:</h2>
/// 
/// <pre>
/// 
///   relational-operation = expression  relational-operator  expression
///   
///      relational-operator = value-relational-operator | reference-comparator
///      
///         value-relational-operator =  < | <= | = | >= | > | <>
///         
///         reference-comparator =  == | =/= 
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/RelationalOperation.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class RelationalOperation extends Expression {

	/// The left hand side
	private Expression lhs;

	/// The relational operation
	private int opr;

	/// The right hand side
	private Expression rhs;

	/// Create a new RelationalOperation.
	/// @param lhs the left hand side
	/// @param opr the relation
	/// @param rhs the right hand side
	RelationalOperation(final Expression lhs,final int opr,final Expression rhs) {
		this.type = Type.Boolean;
		this.lhs = lhs;
		this.opr = opr;
		this.rhs = rhs;
		if (this.lhs == null) {
			Util.error("Missing operand before " + KeyWord.edit(opr));
			this.lhs = new VariableExpression("UNKNOWN_");
		}
		if (this.rhs == null) {
			Util.error("Missing operand after " + KeyWord.edit(opr));
			this.rhs = new VariableExpression("UNKNOWN_");
		}
		this.lhs.backLink = this.rhs.backLink = this;
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		Global.sourceLineNumber = lineNumber;
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("BEGIN RelationalOperation" + toString() + ".doChecking - Current Scope Chain: "	+ Global.getCurrentScope().edScopeChain());
		lhs.doChecking();
		rhs.doChecking();
		Type type1 = lhs.type;
		Type type2 = rhs.type;
		this.type = Type.Boolean;
		switch (opr) {
		case KeyWord.LT, KeyWord.LE, KeyWord.EQ, KeyWord.NE, KeyWord.GE, KeyWord.GT: {
			if (type1.keyWord == Type.T_TEXT      && type2.keyWord == Type.T_TEXT) break;
			if (type1.keyWord == Type.T_CHARACTER && type2.keyWord == Type.T_CHARACTER) break;
			if (type1.keyWord == Type.T_BOOLEAN   && type2.keyWord == Type.T_BOOLEAN) break;
			// Arithmetic Relation
			Type atype = Type.arithmeticTypeConversion(type1, type2);
			if (atype == null)
				Util.error("Incompatible types in binary operation: " + toString());
			lhs = (Expression) TypeConversion.testAndCreate(atype, lhs);
			rhs = (Expression) TypeConversion.testAndCreate(atype, rhs);
			break;
		}
		case KeyWord.EQR: case KeyWord.NER: {
			// Object =/= Object or Object == Object
			if ((!type1.isReferenceType()) || (!type2.isReferenceType()))
				Util.error("RelationalOperation: Illegal types: " + type1 + " " + opr + " " + type2);
			break;
		}
		default:
			Util.IERR();
			this.type = rhs.type;
		}
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("END RelationalOperation" + toString() + ".doChecking - Result type=" + this.type);
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
		if (lhs.type.keyWord == Type.T_TEXT && rhs.type.keyWord == Type.T_TEXT) {
			switch (opr) {
				case KeyWord.LT, KeyWord.LE, KeyWord.EQ, KeyWord.NE, KeyWord.GE, KeyWord.GT:
						return (doCodeTextValueRelation());
				case KeyWord.EQR, KeyWord.NER: 
						return (doCodeTextRefRelation());
				default: Util.IERR();
			}
		}
		if (this.backLink == null)
			 return (lhs.get() + KeyWord.toJavaCode(opr) + '(' + rhs.get() + ')');
		else return ("(" + lhs.get() + KeyWord.toJavaCode(opr) + '(' + rhs.get() + "))");
	}

	// ***********************************************************************
	// *** CODE: doCodeTextValueRelation
	// ***********************************************************************
	/// Coding Utility: Code text value relation
	/// @return the resulting Java source code
	private String doCodeTextValueRelation() {
		StringBuilder s = new StringBuilder();
		String fnc = "??";
		switch (opr) {
		    case KeyWord.LT -> fnc = "LT(";
		    case KeyWord.EQ -> fnc = "EQ(";
		    case KeyWord.LE -> fnc = "LE(";
		    case KeyWord.GT -> fnc = "GT(";
		    case KeyWord.NE -> fnc = "NE(";
		    case KeyWord.GE -> fnc = "GE(";
		    default -> Util.IERR();
		}
		s.append("RTS_UTIL._TXTREL_").append(fnc);
		s.append(lhs.get()).append(',');
		s.append(rhs.get()).append(')');
		return (s.toString());
	}

	// ***********************************************************************
	// *** CODE: doCodeTextRefRelation
	// ***********************************************************************
	/// Coding Utility: Code text reference relation
	/// @return the resulting Java source code
	private String doCodeTextRefRelation() {
		String fnc = "TRF_EQ(";
		StringBuilder s = new StringBuilder();
		if (opr == KeyWord.NER)	fnc = "TRF_NE(";
		s.append("RTS_UTIL.");
		s.append(fnc);
		s.append(lhs.get()).append(',');
		s.append(rhs.get()).append(')');
		return (s.toString());
	}

	@Override
	public void buildEvaluation(Expression rightPart,CodeBuilder codeBuilder) {	setLineNumber();
		ASSERT_SEMANTICS_CHECKED();
		if(lhs.type.keyWord == Type.T_TEXT) {
			buildTextRelation(codeBuilder);
			return;
		}
		lhs.buildEvaluation(null,codeBuilder);
		rhs.buildEvaluation(null,codeBuilder);
		Label target = codeBuilder.newLabel();
		Label lab2 = codeBuilder.newLabel();
		switch(lhs.type.keyWord) {
			case Type.T_INTEGER, Type.T_CHARACTER, Type.T_BOOLEAN -> {
				switch (opr) {
					case KeyWord.GE -> codeBuilder.if_icmplt(target);
					case KeyWord.NE -> codeBuilder.if_icmpeq(target);
					case KeyWord.GT -> codeBuilder.if_icmple(target);
					case KeyWord.LE -> codeBuilder.if_icmpgt(target);
					case KeyWord.EQ -> codeBuilder.if_icmpne(target);
					case KeyWord.LT -> codeBuilder.if_icmpge(target);
					default -> Util.IERR();
				}
			}
			case Type.T_REAL -> {
				codeBuilder.fcmpl();
					switch (opr) {
						case KeyWord.GE -> codeBuilder.iflt(target);
						case KeyWord.NE -> codeBuilder.ifeq(target);
						case KeyWord.GT -> codeBuilder.ifle(target);
						case KeyWord.LE -> codeBuilder.ifgt(target);
						case KeyWord.EQ -> codeBuilder.ifne(target);
						case KeyWord.LT -> codeBuilder.ifge(target);
						default -> Util.IERR();
					}
			}
			case Type.T_LONG_REAL -> {
				codeBuilder.dcmpl();
				switch (opr) {
					case KeyWord.GE -> codeBuilder.iflt(target);
					case KeyWord.NE -> codeBuilder.ifeq(target);
					case KeyWord.GT -> codeBuilder.ifle(target);
					case KeyWord.LE -> codeBuilder.ifgt(target);
					case KeyWord.EQ -> codeBuilder.ifne(target);
					case KeyWord.LT -> codeBuilder.ifge(target);
					default -> Util.IERR();
				}
			}
			case Type.T_REF, Type.T_TEXT -> {
				switch (opr) {
					case KeyWord.EQR -> codeBuilder.if_acmpne(target);
					case KeyWord.NER -> codeBuilder.if_acmpeq(target);
					default -> Util.IERR();
				}
			}
			default -> Util.IERR();
		}
		codeBuilder
			.iconst_1()
			.goto_(lab2)
			.labelBinding(target)
			.iconst_0()
			.labelBinding(lab2);
	}

	/// ClassFile coding utility: Build Text Relation.
	/// @param codeBuilder the codeBuiløder to use.
	private void buildTextRelation(CodeBuilder codeBuilder) {
		lhs.buildEvaluation(null,codeBuilder);
		rhs.buildEvaluation(null,codeBuilder);
		RTS.buildInvokeTextRel(opr, codeBuilder);
	}


	@Override
	public String toString() {
		return ("(" + lhs + ' ' + KeyWord.edit(opr) + ' ' + rhs + ")");
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private RelationalOperation() {}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeRelationalOperation: " + this);
		oupt.writeKind(ObjectKind.RelationalOperation);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** Expression
		oupt.writeType(type);
		oupt.writeObj(backLink);
		// *** RelationalOperation
		oupt.writeObj(lhs);
		oupt.writeShort(opr);
		oupt.writeObj(rhs);
	}
	
	/// Read and return an object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the object read from the stream.
	/// @throws IOException if something went wrong.
	public static RelationalOperation readObject(AttributeInputStream inpt) throws IOException {
		RelationalOperation expr = new RelationalOperation();
		expr.OBJECT_SEQU = inpt.readSEQU(expr);
		// *** SyntaxClass
		expr.lineNumber = inpt.readShort();
		// *** Expression
		expr.type = inpt.readType();
		expr.backLink = (SyntaxClass) inpt.readObj();
		// *** RelationalOperation
		expr.lhs = (Expression) inpt.readObj();
		expr.opr = inpt.readShort();
		expr.rhs = (Expression) inpt.readObj();
		Util.TRACE_INPUT("readRelationalOperation: " + expr);
		return(expr);
	}

}
