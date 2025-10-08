/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.declaration;

import java.io.IOException;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Vector;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.syntaxClass.expression.RemoteVariable;
import simula.compiler.syntaxClass.expression.VariableExpression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;
	
/// Parameter Declaration.
/// 
/// A parameter models class and procedure parameters.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/Parameter.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class Parameter extends Declaration {
	/// Parameter transfer Mode.
	public int mode;
	
	/// Parameter Kind.
	public int kind;
	
	/// Parameter's number of dimension in case of array kind.
	/// Set during doChecking
	public int nDim = -1;

	/// Procedure parameter transfer Mode.
	public class Mode {
		/** Parameter transfered by value */ public static final int value = 1;
		/** Parameter transfered by name */  public static final int name = 2;

		/** Default constructor: Not used */ public Mode() {}
	}
	
	/// Utility: edMode
	/// @param mode a mode code
	/// @return the resulting String
	public static String edMode(int mode) {
		switch(mode) {
			case 1: return("value");
			case 2: return("name");
		}
		return("default");
	}
	
	/// Procedure parameter Kind.
	public class Kind {
		/** Simple parameter */    public static final int Simple = 1;
		/** Procedure parameter */ public static final int Procedure = 2;
		/** Array parameter */     public static final int Array = 3;
		/** Label parameter */     public static final int Label = 4;
		
		/** Default constructor: Not used */ public Kind() {}
	}
	
	/// Utility: edKind
	/// @param kind a kind code
	/// @return the resulting String
	public static String edKind(int kind) {
		switch(kind) {
			case 1: return("Simple");
			case 2: return("Procedure");
			case 3: return("Array");
			case 4: return("Label");
		}
		return("Default");
	}

	/// Create a new Parameter.
	/// @param identifier parameter identifier
	Parameter(final String identifier) {
		super(identifier);
		this.declarationKind = ObjectKind.Parameter;
	}

	/// Create a new Parameter.
	/// @param identifier parameter identifier
	/// @param type parameter type
	/// @param kind parameter kind
	Parameter(final String identifier, final Type type, final int kind) {
		this(identifier);
		this.type = type;
		this.kind = kind;
	}

	/// Create a new Parameter.
	/// @param identifier parameter identifier
	/// @param type parameter type
	/// @param kind parameter kind
	/// @param nDim parameter's number of dimension in case of array kind.
	public Parameter(final String identifier, final Type type, final int kind, final int nDim) {
		this(identifier, type, kind);
		this.nDim = nDim;
	}

	// ***********************************************************************************************
	// *** Utility: into
	// ***********************************************************************************************
	/// Add this parameter to the given parameter list.
	/// @param parameterList the given parameter list
	void into(final Vector<Parameter> parameterList) {
		for (Parameter par : parameterList)
			if (Util.equals(par.identifier, this.identifier)) {
				Util.error("Parameter already defined: " + identifier);
				return;
			}
		parameterList.add(this);
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Parameter))
			return (false);
		Parameter otherPar = (Parameter) other;
		if (!type.equals(otherPar.type))
			return (false);
		if (kind != otherPar.kind)
			return (false);
		if (mode != otherPar.mode)
			return (false);
		return (true);
	}

	/// Utility: Set new parameter mode.
	/// @param mode the new mode
	void setMode(final int mode) {
		if (this.mode != 0)
			Util.error("Parameter " + identifier + " is already specified by " + this.mode);
		this.mode = mode;
	}

	/// Utility: Set new type and kind.
	/// @param type the new type
	/// @param kind the new kind
	void setTypeAndKind(final Type type, final int kind) {
		this.type = type;
		this.kind = kind;
	}

	/// Utility: Set new external identifier based on the given prefix level.
	/// @param prefixLevel the given prefix level
	void setExternalIdentifier(final int prefixLevel) {
		if (prefixLevel > 0)
			 externalIdent = "p" + prefixLevel + '_' + identifier;
		else externalIdent = "p_" + identifier;
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())
			return;
		Global.sourceLineNumber = lineNumber;
		if (kind == 0) {
			Util.error("Parameter " + identifier + " is not specified -- assumed Simple Integer");
			kind = Kind.Simple;
			type = Type.Integer;
		}
		if (type != null)
			type.doChecking(Global.getCurrentScope().declaredIn);
		if (!legalTransmitionMode())
			Util.error("Illegal transmission mode: " + mode + ' ' + kind + ' ' + identifier + " by " + edMode(mode) + " is not allowed");
		SET_SEMANTICS_CHECKED();
	}

	/// Check if the parameter has a legal transmission mode.
	/// 
	/// The available transmission modes for the different kinds of parameters to
	/// procedures.
	/// 
	/// <pre>
	///     --------------------------------------------------------------
	///    |                       |         Transmission modes           |
	///    |   Parameter           | - - - - - - - - - - - - - - - - - - -|
	///    |                       |  by value | by reference |  by name  |
	///    |--------------------------------------------------------------|
	///    |   value type          |     D     |       I      |     O     |
	///    |   object ref. type    |     I     |       D      |     O     |
	///    |   text                |     O     |       D      |     O     |
	///    |   value type array    |     O     |       D      |     O     |
	///    |   reference type array|     I     |       D      |     O     |
	///    |   procedure           |     I     |       D      |     O     |
	///    |   type procedure      |     I     |       D      |     O     |
	///    |   label               |     I     |       D      |     O     |
	///    |   switch              |     I     |       D      |     O     |
	///     --------------------------------------------------------------
	/// 
	///        D:  default mode       O:  optional mode       I:  illegal
	/// </pre>
	/// @return true if the parameter has a legal transmission mode
	private boolean legalTransmitionMode() {
		boolean illegal = false;
		switch (kind) {
			case Kind.Simple -> {
				if (type.keyWord == Type.T_TEXT)
					break; // Simple Text
				else if (type.isReferenceType()) {
					if (mode == Parameter.Mode.value)
						illegal = true;
				} // Simple ref(ClassIdentifier)
				else if (mode == 0)
					mode = Parameter.Mode.value; // Simple Type Integer, Real, Character
			}
			case Kind.Array -> {
				if (type.isReferenceType() && mode == Parameter.Mode.value)
					illegal = true;
			}
			case Kind.Procedure, Kind.Label -> {
				if (mode == Parameter.Mode.value)
					illegal = true;
			}
		}
		return (!illegal);
	}

	/// Java coding utility: edit Java code version of this parameter's type.
	/// @return the resulting Java code
	String toJavaType() {
		ASSERT_SEMANTICS_CHECKED();
		if (mode == Parameter.Mode.name) {
			switch (kind) {
			case Kind.Simple: if (type.keyWord == Type.T_LABEL)
								 return ("RTS_NAME<RTS_LABEL>");
								 return ("RTS_NAME<" + type.toJavaTypeClass() + ">");
			case Kind.Procedure: return ("RTS_NAME<RTS_PRCQNT>");
			case Kind.Label:	 return ("RTS_NAME<RTS_LABEL>");
			case Kind.Array:	 return ("RTS_NAME<RTS_ARRAY>");
			}
		}
		switch (kind) {
			case Kind.Array:	 return ("RTS_ARRAY");
			case Kind.Label:	 return ("RTS_LABEL");
			case Kind.Procedure: return ("RTS_PRCQNT");
			case Kind.Simple: // Fall through
		}
		return (type.toJavaType());
	}

	@Override
	public String toJavaCode() {
		return (toJavaType() + ' ' + externalIdent);
	}
	
	/// ClassFile coding utility: buildParamCode
	/// @param codeBuilder the codeBuilder to use
	/// @param expr parameter value expression
	public void buildParamCode(CodeBuilder codeBuilder,Expression expr) {
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		if (mode == Parameter.Mode.name) {
			buildNameParam(codeBuilder,this,expr);
		} else switch (kind) {
			case Kind.Array -> {
				expr.buildEvaluation(null,codeBuilder);
				if(mode == Parameter.Mode.value)
					RTS.invokevirtual_ARRAY_copy(codeBuilder);
				}
			case Kind.Label -> Util.IERR();
				
			case Kind.Procedure -> {
//				return ("RTS_PRCQNT");
//				new RTS_PRCQNT(((adHoc000)(_CUR))
//		         4: getstatic     #21                 // Field _CUR:Lsimula/runtime/RTS_RTObject;
//		         7: new           #25                 // class simula/runtime/RTS_PRCQNT
//		        10: dup
//		        11: getstatic     #21                 // Field _CUR:Lsimula/runtime/RTS_RTObject;
//		        14: checkcast     #8                  // class simulaTestPrograms/adHoc000
//		        17: ldc           #27                 // class simulaTestPrograms/adHoc000_PPP
//		        19: invokespecial #29                 // Method simula/runtime/RTS_PRCQNT."<init>":(Lsimula/runtime/RTS_RTObject;Ljava/lang/Class;)V
				Expression beforeDot = null;
				if(expr instanceof RemoteVariable rem) {
					beforeDot = rem.obj;
					expr = rem.var;
				}
				VariableExpression var=(VariableExpression)expr;
				Declaration decl = var.meaning.declaredAs;
				
				if(decl instanceof ProcedureDeclaration proc) {
					// Declared Procedure: new RTS_PRCQNT
					codeBuilder
						.new_(RTS.CD.RTS_PRCQNT)
						.dup();
					if(beforeDot == null) {
						codeBuilder
							.aload(0)
							.checkcast(BlockDeclaration.currentClassDesc());
					} else beforeDot.buildEvaluation(null, codeBuilder);
					codeBuilder
						.ldc(pool.loadableConstantEntry(proc.getClassDesc()))
						.invokespecial(RTS.CD.RTS_PRCQNT, "<init>", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_RTObject;Ljava/lang/Class;)V"));
					
				} else if(decl instanceof Parameter par) {
					// Parameter Procedure: 
//				        10: aload_0
//				        11: getfield      #9                  // Field p_FFF:Lsimula/runtime/RTS_NAME;
//				        14: invokevirtual #63                 // Method simula/runtime/RTS_NAME.get:()Ljava/lang/Object;
//				        17: checkcast     #67                 // class simula/runtime/RTS_PRCQNT
					codeBuilder
						.aload(0)
						.getfield(par.getFieldRefEntry(pool));
					if(par.mode == Parameter.Mode.name) {
						RTS.invokevirtual_NAME_get(codeBuilder);
						codeBuilder.checkcast(RTS.CD.RTS_PRCQNT);
					}
					
				} else Util.IERR();
			}
				
			case Kind.Simple ->  {
				expr.buildEvaluation(null,codeBuilder);
				if(mode == Parameter.Mode.value && type.keyWord == Type.T_TEXT) {
					RTS.invokestatic_ENVIRONMENT_copy(codeBuilder);
				}
			}
		}
	}
	
	/// ClassFile coding utility: buildNameParam
	/// @param codeBuilder to use
	/// @param expr the Thunk expression to be evaluated.
	public static void buildNameParam(CodeBuilder codeBuilder,Expression expr) {
		Thunk.buildInvoke(0, expr, codeBuilder);
	}	
	
	/// ClassFile coding utility: buildNameParam
	/// @param codeBuilder to use
	/// @param par the parameter used decide parameter kind
	/// @param expr the Thunk expression to be evaluated.
	private static void buildNameParam(CodeBuilder codeBuilder,Parameter par,Expression expr) {
		Thunk.buildInvoke((par==null)?0:par.kind, expr, codeBuilder);
	}
	

	/// ClassFile coding utility: get FieldRefEntry of this Parameter.
	/// @param pool the ConstantPoolBuilder to use.
	/// @return the FieldRefEntry of this Parameter.
	public FieldRefEntry getFieldRefEntry(ConstantPoolBuilder pool) {
		ClassDesc owner=declaredIn.getClassDesc();
		ClassDesc CD_type=null; //type.toClassDesc(kind,mode);
		if(kind==Kind.Procedure)
			 CD_type=Type.Procedure.toClassDesc(kind,mode);
		else CD_type=type.toClassDesc(kind,mode);
		return(pool.fieldRefEntry(owner, getFieldIdentifier(), CD_type));
	}
	
	/// ClassFile coding utility: get getFieldIdentifier of this Parameter.
	/// @return the resulting String.
	@Override
	public String getFieldIdentifier() {
		if(declaredIn instanceof ClassDeclaration cls)
			 return("p"+cls.prefixLevel()+'_'+identifier);
		else return("p_"+identifier);
	}

	/// ClassFile coding utility: buildDeclaration of this Parameter.
	@Override
	public void buildDeclaration(ClassBuilder classBuilder,BlockDeclaration encloser) {
		String ident = getFieldIdentifier();
		if (mode == Parameter.Mode.name) {
			if (kind == Parameter.Kind.Procedure) {
				classBuilder.withField(ident, RTS.CD.RTS_NAME, ClassFile.ACC_PUBLIC);
			} else {			
				classBuilder
					.withField(ident, RTS.CD.RTS_NAME, fieldBuilder -> {
						fieldBuilder
							.withFlags(ClassFile.ACC_PUBLIC)
							.with(SignatureAttribute.of(type.toNameClassSignature()));
					});
			}
		} else if (kind == Parameter.Kind.Array) {
			classBuilder.withField(ident, RTS.CD.RTS_ARRAY, ClassFile.ACC_PUBLIC);
		} else if (kind == Parameter.Kind.Procedure) {
			classBuilder.withField(ident, RTS.CD.RTS_PRCQNT, ClassFile.ACC_PUBLIC);
		} else {
			ClassDesc CD=type.toClassDesc(kind,mode);
			classBuilder.withField(ident, CD, ClassFile.ACC_PUBLIC);
		}
	}
	
	/// ClassFile coding utility: Parameter type toClassDesc.
	/// @return the resulting CalssDesc
	public ClassDesc type_toClassDesc() {
		switch(this.kind) {
		case Kind.Array: return(this.type.toClassDesc(this.kind,this.mode));
		case Kind.Label:
			Util.IERR();
			return(null);
		case Kind.Procedure:		  return(Type.Procedure.toClassDesc(this.kind,this.mode));
		case Kind.Simple: default: return(this.type.toClassDesc(this.kind,this.mode));
		}
	}

	/// ClassFile coding utility: generate load instruction dependent of type.
	/// @param codeBuilder the codeBuilder to use
	/// @param ofst the parameter offset.
	public void loadParameter(CodeBuilder codeBuilder,int ofst) {
		if (mode == Parameter.Mode.name) codeBuilder.aload(ofst);
		else if (kind == Parameter.Kind.Array) codeBuilder.aload(ofst);
		else if (kind == Parameter.Kind.Procedure) codeBuilder.aload(ofst);
		else {
			switch(type.keyWord) {
				case Type.T_BOOLEAN:
				case Type.T_CHARACTER:
				case Type.T_INTEGER:	codeBuilder.iload(ofst); break;
				case Type.T_REAL:		codeBuilder.fload(ofst); break;
				case Type.T_LONG_REAL:	codeBuilder.dload(ofst); break;
				case Type.T_TEXT:
				case Type.T_REF:
				case Type.T_PROCEDURE:
				case Type.T_LABEL:		codeBuilder.aload(ofst); break;
				default: Util.IERR();
			}
		}
	}
	

	// ***********************************************************************************************
	// *** Printing Utility: editParameterList
	// ***********************************************************************************************
	/// Printing Utility: edit parameter list.
	/// @param parameterList the given parameter list
	/// @return the resulting string
	public static String editParameterList(Vector<Parameter> parameterList) {
		StringBuilder s = new StringBuilder();
		s.append('(');
		boolean first = true;
		if (parameterList != null) {
			for (Parameter par : parameterList) {
				if (!first)
					s.append(',');
				s.append(par);
				first = false;
			}
		}
		s.append(");");
		return (s.toString());
	}
	
	@Override
	public void printTree(final int indent, final Object head) {
		IO.println(edTreeIndent(indent)+this);
	}
	
	@Override
	public String toString() {
		String s = "";
		if (type != null)
			s = s + type;
		else
			s = "NOTYPE";
		if (mode != 0)
			s = "" + edMode(mode) + " " + type;
		if (kind == 0)
			s = s + " NOKIND";
		if (nDim > 0)
			s = s + " " + nDim + "-Dimentional";
		else if (kind != Parameter.Kind.Simple)
			s = s + " " + kind;
		return (s + ' ' + identifier + "(" + externalIdent + ')');
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private Parameter() {
		super(null);
		this.declarationKind = ObjectKind.Parameter;
	}
	
	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("Parameter: " + type + ' ' + identifier + ' ' + kind + ' ' + mode);
		oupt.writeKind(declarationKind);
		oupt.writeShort(OBJECT_SEQU);
		// *** Parameter
		oupt.writeString(identifier);
		oupt.writeString(externalIdent);
		oupt.writeType(type);
		oupt.writeShort(kind);
		oupt.writeShort(mode);
	}

	/// Read and return an object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the object read from the stream.
	/// @throws IOException if something went wrong.
	public static SyntaxClass readObject(AttributeInputStream inpt) throws IOException {
		Parameter par = new Parameter();
		par.OBJECT_SEQU = inpt.readSEQU(par);
		// *** Parameter
		par.identifier = inpt.readString();
		par.externalIdent = inpt.readString();
		par.type = inpt.readType();
		par.kind = inpt.readShort();
		par.mode = inpt.readShort();
		Util.TRACE_INPUT("Parameter: " + par.type + ' ' + par.identifier + ' ' + par.kind + ' ' + par.mode);
		return(par);
	}

}
