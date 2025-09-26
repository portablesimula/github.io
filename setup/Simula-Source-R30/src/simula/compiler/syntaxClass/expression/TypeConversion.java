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
import simula.compiler.syntaxClass.OverLoad;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.Type.ConversionKind;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Type Conversion.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/TypeConversion.java">
/// <b>Source File</b></a>.
public final class TypeConversion extends Expression {
	
	/// The expression.
	public Expression expression;

	/// Create a new TypeConversion.
	/// @param type the new type
	/// @param expression the expression
	public TypeConversion(final Type type,final Expression expression) {
		this.type=type;
		this.expression = expression; expression.backLink=this;
	    this.doChecking();
	}

	/// Java Coding utility: Test if a TypeConversion is necessary and then create it.
	/// @param fromType convert from this type
	/// @param toType convert to this type
	/// @param expr the expression
	/// @return piece of Java source code
	public static String mayBeConvert(final Type fromType, final Type toType, final String expr) {
		if(fromType.keyWord == Type.T_REAL || fromType.keyWord == Type.T_LONG_REAL) {
			if(toType.keyWord == Type.T_INTEGER)
              return("=(int)Math.round("+expr+");");
		}
        return("=("+toType.toJavaType()+")("+expr+");");
	}
	
	/// ClassFile coding utility: Test if a TypeConversion is necessary and then create it.
	/// @param fromType convert from this type
	/// @param toType convert to this type
	/// @param codeBuilder the codeBuilder to use.
	public static void buildMayBeConvert(final Type fromType, final Type toType, CodeBuilder codeBuilder) {
		// NOTE: 'expr' is top of operand stack
		switch(fromType.keyWord) {
			case Type.T_INTEGER:
				switch(toType.keyWord) {
					case Type.T_INTEGER:	break; // Nothing
					case Type.T_REAL:		codeBuilder.i2f(); break;
					case Type.T_LONG_REAL:	codeBuilder.i2d(); break;
					default: Util.IERR();
				} break;
			case Type.T_REAL:
				switch(toType.keyWord) {
					case Type.T_INTEGER:	codeBuilder.invokestatic(RTS.CD.JAVA_LANG_MATH, "round", MethodTypeDesc.ofDescriptor("(F)I")); break;

					case Type.T_REAL:		break; // Nothing
					case Type.T_LONG_REAL:	codeBuilder.f2d(); break;
					default: Util.IERR();
				} break;
			case Type.T_LONG_REAL:
				switch(toType.keyWord) {
					case Type.T_INTEGER:	codeBuilder
												.invokestatic(RTS.CD.JAVA_LANG_MATH, "round", MethodTypeDesc.ofDescriptor("(D)J"))
												.l2i();
											break;
					case Type.T_REAL:		codeBuilder.d2f(); break;
					case Type.T_LONG_REAL:	break; // Nothing
					default: Util.IERR();
				} break;
			default: Util.IERR();
		}
	}

	/// Java Coding utility: Test if a TypeConversion is necessary and then create it.
	/// @param toType convert to toType
	/// @param expression the expression
	/// @return the resulting expression
	public static Expression testAndCreate(final Type toType,final Expression expression) {
		Type fromType=expression.type;
		String qual=(fromType==null)?null:fromType.getRefIdent();
		if(qual != null) {
			int rhsBL=(fromType!=null && fromType.declaredIn!=null)?fromType.declaredIn.getRTBlockLevel() : 0;
			int lhsBL=(toType!=null && toType.declaredIn!=null)?toType.declaredIn.getRTBlockLevel() : 0;
			if(rhsBL != 0 && lhsBL != 0 && rhsBL != lhsBL) {
				Util.error("Incompatible types: "+expression+" of type "+expression.type+" can't be converted to "+toType);
			}
		}
		if (testCastNeccessary(toType, expression)) {
			if(expression instanceof Constant constant) {
				Number val=(Number)constant.value;
				switch(toType.keyWord) {
					case Type.T_INTEGER: {
						if(fromType.keyWord == Type.T_REAL) val=(int)Math.round(val.floatValue());
						else if(fromType.keyWord == Type.T_LONG_REAL) val=(int)Math.round(val.doubleValue());
					} break;
					case Type.T_REAL: val=val.floatValue(); break;
					case Type.T_LONG_REAL: val=val.doubleValue(); break;
					default: Util.IERR();
				}
				Constant c=new Constant(toType,val); c.doChecking();
				return(c);
			}
			if(Option.compilerMode != Option.CompilerMode.viaJavaSource) {
				if(toType instanceof OverLoad otp) {
					return (new TypeConversion(otp.type[0], expression));
				}				
			}
			return (new TypeConversion(toType, expression));
		}
		return (expression);
	}

	/// Java coding utility: Test if a Type Cast is necessary.
	/// @param toType the desired type
	/// @param expression the expression
	/// @return piece of Java source code
	private static boolean testCastNeccessary(Type toType,final Expression expression) {
		if (toType == null)	return (false);
		if(Option.compilerMode != Option.CompilerMode.viaJavaSource) {
			if(toType instanceof OverLoad otp) {
				if(!otp.contains(expression.type)) {
					return(true); // Ad'Hoc
				}
			}
		}
		Type fromType = expression.type;
		if(fromType==null) {
			Util.error("Expression "+expression+" has no type - can't be converted to "+toType);
//			Thread.dumpStack();
			return(false);
		}
		ConversionKind conversionKind = fromType.isConvertableTo(toType);
		switch (conversionKind) {
			case DirectAssignable:	return (false);
			case ConvertValue:
			case ConvertRef:		return (true);
			case Illegal:
				Util.error("TypeConversion: Illegal cast: (" + toType + ") " + expression);
			default: return (false);
		}
	}
	
	@Override
	// Is redefined in Variable, RemoteVariable and TypeConversion
	public VariableExpression getWriteableVariable() {
	    VariableExpression writeableVariable=expression.getWriteableVariable();
		return(writeableVariable);
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		type.doChecking(Global.getCurrentScope());
		expression.doChecking();
		Type type = expression.type;
		if (type.isConvertableTo(this.type).equals(Type.ConversionKind.Illegal))
			Util.error("Illegal Type Conversion " + type + " ==> " + this.type);
		SET_SEMANTICS_CHECKED();
	}

	// Returns true if this expression may be used as a statement.
	@Override
	public boolean maybeStatement() {
		ASSERT_SEMANTICS_CHECKED();
		return(false);  
	}

	@Override
	public String toJavaCode() {
		ASSERT_SEMANTICS_CHECKED();
		String evaluated = expression.toJavaCode();
		if (type.keyWord == Type.T_INTEGER) {
			Type fromType = expression.type;
			if (fromType.keyWord == Type.T_REAL || fromType.keyWord == Type.T_LONG_REAL)
				return("(int)Math.round(" + evaluated + ")");
		}
		return ("((" + type.toJavaType() + ")(" + evaluated + "))");			
	}

	@Override
	public void buildEvaluation(Expression rightPart,CodeBuilder codeBuilder) {	setLineNumber();
		ASSERT_SEMANTICS_CHECKED();
		expression.buildEvaluation(null,codeBuilder);
		Type fromType = expression.type;
		switch(type.keyWord) {
			case Type.T_INTEGER: 
				switch(fromType.keyWord) {
					case Type.T_REAL:
						codeBuilder.invokestatic(RTS.CD.JAVA_LANG_MATH, "round", MethodTypeDesc.ofDescriptor("(F)I"));
						break;
					case Type.T_LONG_REAL:
						codeBuilder
							.invokestatic(RTS.CD.JAVA_LANG_MATH, "round", MethodTypeDesc.ofDescriptor("(D)J"))
							.l2i();
						break;
				} break;
			
			case Type.T_REAL:
				switch(fromType.keyWord) {
					case Type.T_INTEGER: codeBuilder.i2f(); break;
					case Type.T_LONG_REAL: codeBuilder.d2f(); break;
					default:
						if(Option.compilerMode != Option.CompilerMode.viaJavaSource && fromType instanceof OverLoad otp) {
							if(!otp.contains(Type.Real)) Util.IERR();
						} else if (fromType.keyWord != Type.T_REAL) Util.IERR();
				} break;
			
			case Type.T_LONG_REAL: {
				switch(fromType.keyWord) {
					case Type.T_INTEGER: codeBuilder.i2d(); break;
					case Type.T_REAL: codeBuilder.f2d(); break;
					default:
						if(Option.compilerMode != Option.CompilerMode.viaJavaSource && fromType instanceof OverLoad otp) {
							if(!otp.contains(Type.LongReal)) Util.IERR();
						} else if (fromType.keyWord != Type.T_LONG_REAL) Util.IERR();
				}
			} break;
			
			default:
				codeBuilder.checkcast(type.toObjectClassDesc());
		}
	}


	@Override
	public String toString() {
		return ("((" + type + ")(" + expression + "))");
	}


	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private TypeConversion() {}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeTypeConversion: " + this);
		oupt.writeKind(ObjectKind.TypeConversion);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** Expression
		oupt.writeType(type);
		oupt.writeObj(backLink);
		// *** TypeConversion
		oupt.writeObj(expression);
	}
	
	/// Read and return a TypeConversion object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the TypeConversion object read from the stream.
	/// @throws IOException if something went wrong.
	public static TypeConversion readObject(AttributeInputStream inpt) throws IOException {
		TypeConversion expr = new TypeConversion();
		expr.OBJECT_SEQU = inpt.readSEQU(expr);
		// *** SyntaxClass
		expr.lineNumber = inpt.readShort();
		// *** Expression
		expr.type = inpt.readType();
		expr.backLink = (SyntaxClass) inpt.readObj();
		// *** TypeConversion
		expr.expression = (Expression) inpt.readObj();
		Util.TRACE_INPUT("readTypeConversion: " + expr);
		return(expr);
	}

}
