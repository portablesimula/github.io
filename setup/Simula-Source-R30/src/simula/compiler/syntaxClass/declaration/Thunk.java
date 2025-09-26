/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.declaration;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassSignature;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.syntaxClass.expression.RemoteVariable;
import simula.compiler.syntaxClass.expression.TypeConversion;
import simula.compiler.syntaxClass.expression.VariableExpression;
import simula.compiler.utilities.ClassHierarchy;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Thunk Declaration.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/Thunk.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class Thunk extends DeclarationScope {

	/** Local variable */ private static int OBJECT_SEQU = 0;
	/** Local variable */ private ClassDesc CD_ThisClass;
	/** Local variable */ private int kind; //Parameter.Kind kind;
	/** Local variable */ private Expression expr;
	
	/// Create a new Thunk object.
	/// @param kind the kind code
	/// @param expr the Thunk expression.
	private Thunk(int kind,Expression expr) {
		super(Global.sourceName + "$THUNK$" + (++OBJECT_SEQU));
		this.declarationKind = ObjectKind.Thunk;
		this.kind = kind;
		this.expr = expr;
		this.CD_ThisClass = this.getClassDesc();
		if(this.declaredIn instanceof Thunk) Util.IERR();
		Global.setScope(this.declaredIn);
		this.CHECKED = true;
	}

	// ***********************************************************************************************
	// *** Utility: findVisibleAttributeMeaning
	// ***********************************************************************************************
	@Override
	public Meaning findVisibleAttributeMeaning(final String ident) {
		if(Option.internal.TRACE_FIND_MEANING>0) Util.println("BEGIN Checking Thunk for "+ident+" ================================== "+identifier+" ==================================");

		Meaning meaning = declaredIn.findVisibleAttributeMeaning(ident);
		
		if(Option.internal.TRACE_FIND_MEANING>0) Util.println("ENDOF Checking Thunk for "+ident+" ================================== "+identifier+" ==================================");
		return (meaning);
	}

	// ***********************************************************************************************
	// *** ClassFile Coding Utility: buildInvoke
	// ***********************************************************************************************
	/// ClassFile Coding Utility: Build invoke Thunk
	/// @param kind a kind code
	/// @param expr the Thunk expression
	/// @param codeBuilder the codeBuilder to use.
	public static void buildInvoke(int kind,Expression expr,CodeBuilder codeBuilder) {
		//  new RTS_NAME< TYPE >() {
		//     public TYPE get() {
		//        return("+apar.toJavaCode()+");
		//     }
		//  }	
		Thunk thunk = new Thunk(kind,expr);
		try { thunk.createJavaClassFile(); } catch (IOException e) { e.printStackTrace(); }
		ClassDesc CD_THUNK=thunk.getClassDesc();
		codeBuilder
			.new_(CD_THUNK)
			.dup()
			.aload(0)
			.invokespecial(CD_THUNK, "<init>", MethodTypeDesc.ofDescriptor("("+BlockDeclaration.currentClassDesc().descriptorString()+")V"));
	}

	// ***********************************************************************************************
	// *** ByteCoding: buildClassFile
	// ***********************************************************************************************
	@Override
	public byte[] buildClassFile() {
		if(Option.verbose) Util.println("Begin buildClassFile: "+CD_ThisClass);
		ClassHierarchy.addClassToSuperClass(CD_ThisClass, RTS.CD.RTS_NAME);
		
		byte[] bytes = ClassFile.of(ClassFile.ClassHierarchyResolverOption.of(ClassHierarchy.getResolver())).build(CD_ThisClass,
				classBuilder -> {
					classBuilder
						.with(SourceFileAttribute.of(Global.sourceFileName))
						.withFlags(ClassFile.ACC_PUBLIC + ClassFile.ACC_SUPER + ClassFile.ACC_FINAL)
						.withSuperclass(RTS.CD.RTS_NAME)
						.with(SignatureAttribute.of(ClassSignature.parseFrom("Lsimula/runtime/RTS_NAME<"+Type.toJVMClassType(expr.type,kind)+">;")))
						.withMethodBody("<init>", MethodTypeDesc.ofDescriptor("("+BlockDeclaration.currentClassDesc().descriptorString()+")V"), 0,
							codeBuilder -> buildConstructor(codeBuilder))
						.withMethodBody("get", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"), ClassFile.ACC_PUBLIC,
							codeBuilder -> buildMethod_get(codeBuilder));
				    VariableExpression writeableVariable=expr.getWriteableVariable();
				    if(writeableVariable!=null) {
				    	Declaration declaredAs = writeableVariable.meaning.declaredAs;
				    	switch(declaredAs.declarationKind) {
		    				case ObjectKind.ArrayDeclaration -> writeableVariable = null; // OK 
			    			case ObjectKind.Parameter ->  {} // OK
				    		case ObjectKind.SimpleVariableDeclaration -> {} // OK
				    		case ObjectKind.Procedure,
//				    			 ObjectKind.Switch,
			    		     	 ObjectKind.MemberMethod,
				    		     ObjectKind.LabelDeclaration,
				    		     ObjectKind.ContextFreeMethod -> writeableVariable = null;
				    		default -> Util.IERR(ObjectKind.edit(declaredAs.declarationKind));
				    	}
				    }
				    if(writeableVariable!=null) {
				    	String MTD_put=null;
				    	if(expr.type != null) {
				    		switch(expr.type.keyWord) {
								case Type.T_INTEGER   -> MTD_put = "(Ljava/lang/Integer;)Ljava/lang/Integer;";
					    		case Type.T_REAL      -> MTD_put = "(Ljava/lang/Float;)Ljava/lang/Float;";
					    		case Type.T_LONG_REAL -> MTD_put = "(Ljava/lang/Double;)Ljava/lang/Double;";
					    		case Type.T_BOOLEAN   -> MTD_put = "(Ljava/lang/Boolean;)Ljava/lang/Boolean;";
					    		case Type.T_CHARACTER -> MTD_put = "(Ljava/lang/Character;)Ljava/lang/Character;";
					    		case Type.T_LABEL     -> MTD_put = "(Lsimula/runtime/RTS_LABEL;)Lsimula/runtime/RTS_LABEL;";
					    		case Type.T_TEXT, Type.T_REF -> {
					    			String CDS=expr.type.toClassDesc().descriptorString();
					    			MTD_put = "("+CDS+')'+CDS;
					    		}
					    		default -> Util.IERR();
				    		}
				    	
				    		Expression beforeDot=(expr instanceof RemoteVariable rem)?rem.obj:null;
				    		classBuilder
				    			.withMethodBody("put", MethodTypeDesc.ofDescriptor(MTD_put), ClassFile.ACC_PUBLIC,
				    				codeBuilder -> buildMethod_put(codeBuilder,beforeDot,expr))
				    			.withMethodBody("put", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Ljava/lang/Object;"),
				    				ClassFile.ACC_PUBLIC + ClassFile.ACC_BRIDGE + ClassFile.ACC_SYNTHETIC,
				    				codeBuilder -> buildMethod_put2(codeBuilder));
				    	}
				    }
				}
		);
		return(bytes);
	}


	// ***********************************************************************************************
	// *** ByteCoding: buildConstructor
	// ***********************************************************************************************
	/// Generate byteCode for the Constructor.
	/// <pre>
	///     public ClassIdent(RTS_RTObject staticLink) {
	///         super(staticLink);
	///    }
	/// </pre>
	/// @param codeBuilder the CodeBuilder
	private void buildConstructor(CodeBuilder codeBuilder) {
		Label begScope = codeBuilder.newLabel();
		Label endScope = codeBuilder.newLabel();
		codeBuilder
			.labelBinding(begScope)
			.localVariable(0,"this",CD_ThisClass,begScope,endScope)
			.localVariable(1,"staticLink",RTS.CD.RTS_RTObject,begScope,endScope);

		// super(staticLink);
		codeBuilder
			.aload(0)
			.invokespecial(RTS.CD.RTS_NAME,"<init>", MethodTypeDesc.ofDescriptor("()V"));

		codeBuilder
			.return_()
			.labelBinding(endScope);
	}


	// ***********************************************************************************************
	// *** ClassFile Coding Utility: buildMethod_get
	// ***********************************************************************************************
	/// Generate byteCode for the 'get' method.
	/// 
	/// 	public Integer get() { return(((adHoc13)(_ENV._SL)).n);
	/// @param codeBuilder the CodeBuilder
	void buildMethod_get(CodeBuilder codeBuilder) {
		Global.enterScope(this);
			Label begScope = codeBuilder.newLabel();
			Label endScope = codeBuilder.newLabel();
			codeBuilder
				.labelBinding(begScope)
				.localVariable(0,"this",CD_ThisClass,begScope,endScope);

			if(kind==0) {
				expr.buildEvaluation(null,codeBuilder);
				expr.type.buildObjectValueOf(codeBuilder);
			} else {
				switch(kind) { // Parameter.Kind
					case Parameter.Kind.Array ->		expr.buildEvaluation(null,codeBuilder);
					case Parameter.Kind.Label ->		Util.IERR();
					case Parameter.Kind.Procedure ->	buildProcedureQuant(expr,codeBuilder);
					case Parameter.Kind.Simple ->     { expr.buildEvaluation(null,codeBuilder);
														expr.type.buildObjectValueOf(codeBuilder); }
					default -> {
						expr.buildEvaluation(null,codeBuilder);
						expr.type.buildObjectValueOf(codeBuilder);
					}
				}
			}
			codeBuilder
				.areturn()
				.labelBinding(endScope);
		Global.exitScope();
	}

	// ***********************************************************************************************
	// *** ByteCoding: buildMethod_put
	// ***********************************************************************************************
	/// Generate byteCode for the 'put' method.
	/// 
	/// 	public Integer put(Integer x_) {
	/// 		return(((adHoc13)(_ENV._SL)).n=(int)x_);
	///  }
	/// @param codeBuilder the CodeBuilder
	/// @param beforeDot expression.
	/// @param expr the Thunk expression.
	void buildMethod_put(CodeBuilder codeBuilder, Expression beforeDot, Expression expr) {
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		VariableExpression writeableVariable=expr.getWriteableVariable();
		Meaning meaning=writeableVariable.meaning;
		Declaration declaredAs = meaning.declaredAs;
		Global.enterScope(this);
			Label begScope = codeBuilder.newLabel();
			Label endScope = codeBuilder.newLabel();
			Label checkStackSize = null; // TESTING_STACK_SIZE
			codeBuilder
				.labelBinding(begScope)
				.localVariable(0,"this",CD_ThisClass,begScope,endScope)
				.localVariable(1,"parameter_x",RTS.CD.RTS_RTObject,begScope,endScope);
			
			if(Option.internal.TESTING_STACK_SIZE) {
				checkStackSize = codeBuilder.newLabel();
				codeBuilder
					.aconst_null()                // TESTING_STACK_SIZE
					.ifnonnull(checkStackSize);  // TESTING_STACK_SIZE				
			}

			writeableVariable.buildIdentifierAccess(true,codeBuilder);
				
			if(beforeDot != null) beforeDot.buildEvaluation(null,codeBuilder);
				
			Parameter nameParameter = null;
			if(declaredAs instanceof Parameter par) {
				if(par.mode == Parameter.Mode.name) {
					nameParameter = par;
					codeBuilder.getfield(par.getFieldRefEntry(pool));
				}
			}
					
			codeBuilder.aload(1); // Parameter x			
			expr.type.valueToPrimitiveType(codeBuilder);

			if(expr instanceof TypeConversion) {
				Type fromType = expr.type;
				Type toType = writeableVariable.type;
				// NOTE: 'expr' is top of operand stack
				if(fromType.isArithmeticType()) {
					TypeConversion.buildMayBeConvert(fromType,toType, codeBuilder);
				} else if(toType.isRefClassType()) {
				    // return("=("+toType.toJavaType()+")("+expr+");");
					expr.buildEvaluation(null, codeBuilder);
					codeBuilder.checkcast(toType.toClassDesc());
				}
				else Util.IERR();
			}
				
			String ident=null;
			if(declaredAs instanceof SimpleVariableDeclaration var) ident=var.getFieldIdentifier();
			else if(declaredAs instanceof Parameter par)            ident=par.getFieldIdentifier();
			else if(declaredAs instanceof ArrayDeclaration arr)     ident=arr.identifier;
			else if(declaredAs instanceof ProcedureDeclaration pro) ident=pro.identifier;
			else Util.IERR();

			if(nameParameter != null) {
				expr.type.buildObjectValueOf(codeBuilder);
				RTS.invokevirtual_NAME_put(codeBuilder);
				codeBuilder.pop();
			} else {
				DeclarationScope declaredIn = meaning.declaredIn;
				ClassDesc owner = declaredIn.getClassDesc();
				if(declaredIn instanceof ConnectionBlock conn) {
					ClassDeclaration whenClass = conn.classDeclaration;
					if(whenClass != null) owner = whenClass.getClassDesc();
				}
				codeBuilder.putfield(pool.fieldRefEntry(owner, ident, writeableVariable.type.toClassDesc()));
			}
			
			if(Option.internal.TESTING_STACK_SIZE) {
				codeBuilder.labelBinding(checkStackSize);  // TESTING_STACK_SIZE
			}

			codeBuilder
				.aload(1) // Parameter x
				.areturn()
				.labelBinding(endScope);
					
		Global.exitScope();
	}

	// ***********************************************************************************************
	// *** ByteCoding: buildMethod_put2    Build syntetic bridge to the 'put' method
	// ***********************************************************************************************
	/// ClassFile Coding Utility: Build synthetic bridge to the 'put' method.
	/// @param codeBuilder the codeBuilder to use.
	private void buildMethod_put2(CodeBuilder codeBuilder) {
		Label begScope = codeBuilder.newLabel();
		Label endScope = codeBuilder.newLabel();
		codeBuilder
			.labelBinding(begScope)
			.localVariable(0,"this",CD_ThisClass,begScope,endScope)
			.localVariable(1,"parameter_x",RTS.CD.RTS_RTObject,begScope,endScope)
			.aload(0)
			.aload(1); // Parameter x			
		ClassDesc owner = CD_ThisClass;
		switch(expr.type.keyWord) {
			case Type.T_INTEGER -> 
				codeBuilder
					.checkcast(ConstantDescs.CD_Integer)
					.invokevirtual(owner, "put", MethodTypeDesc.ofDescriptor("(Ljava/lang/Integer;)Ljava/lang/Integer;"));
			case Type.T_REAL ->
				codeBuilder
					.checkcast(ConstantDescs.CD_Float)
					.invokevirtual(owner, "put", MethodTypeDesc.ofDescriptor("(Ljava/lang/Float;)Ljava/lang/Float;"));
			case Type.T_LONG_REAL ->
				codeBuilder
					.checkcast(ConstantDescs.CD_Double)
					.invokevirtual(owner, "put", MethodTypeDesc.ofDescriptor("(Ljava/lang/Double;)Ljava/lang/Double;"));
			case Type.T_BOOLEAN ->
				codeBuilder
					.checkcast(ConstantDescs.CD_Boolean)
					.invokevirtual(owner, "put", MethodTypeDesc.ofDescriptor("(Ljava/lang/Boolean;)Ljava/lang/Boolean;"));
			case Type.T_CHARACTER ->
				codeBuilder
					.checkcast(ConstantDescs.CD_Character)
					.invokevirtual(owner, "put", MethodTypeDesc.ofDescriptor("(Ljava/lang/Character;)Ljava/lang/Character;"));
			case Type.T_LABEL ->
				codeBuilder
					.checkcast(RTS.CD.RTS_LABEL)
					.invokevirtual(owner, "put", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_LABEL;)Lsimula/runtime/RTS_LABEL;"));
			case Type.T_TEXT, Type.T_REF -> {
				ClassDesc CD=expr.type.toClassDesc();
				String CDS=CD.descriptorString();
				MethodTypeDesc MTD_put=MethodTypeDesc.ofDescriptor("("+CDS+')'+CDS);
				codeBuilder
					.checkcast(CD)
					.invokevirtual(owner, "put", MTD_put);
			}
			default -> Util.IERR();
		}
		
		codeBuilder
			.areturn()
			.labelBinding(endScope);
	}
	
	// ********************************************************************
	// *** buildProcedureQuant
	// ********************************************************************
	/// ClassFile Coding Utility: Edit new procedure quant.
	/// @param apar the actual parameter
	/// @param codeBuilder the CodeBuilder
	private static void buildProcedureQuant(final Expression apar,CodeBuilder codeBuilder) {
//        0: new           #7                  // class simula/runtime/RTS_PRCQNT
//        3: dup
//        4: getstatic     #9                  // Field simula/runtime/RTS_RTObject._CUR:Lsimula/runtime/RTS_RTObject;
//        7: checkcast     #15                 // class simulaTestPrograms/adHoc000
//       10: ldc           #17                 // class simulaTestPrograms/adHoc000_SWITCH1
//       12: invokespecial #19                 // Method simula/runtime/RTS_PRCQNT."<init>":(Lsimula/runtime/RTS_RTObject;Ljava/lang/Class;)V
		ConstantPoolBuilder pool=codeBuilder.constantPool();
	    if (apar instanceof VariableExpression var) {
    		Meaning meaning = var.meaning;
			Declaration decl=meaning.declaredAs;
	    	String procIdent = decl.getJavaIdentifier();
			if (decl instanceof Parameter par) {
				DeclarationScope curScope=Global.getCurrentScope();
				// The current scope. In case of Thunk one level up to Thunk.ENV
				if(curScope instanceof Thunk) curScope = curScope.declaredIn;
				ClassDesc CD_ENV = RTS.CD.classDesc(curScope.externalIdent);
				codeBuilder
					.aload(0)
					.getfield(RTS.FRE.NAME_CUR(pool))
					.checkcast(CD_ENV)
					.getfield(par.getFieldRefEntry(pool));
				if (par.mode == Parameter.Mode.name) {
					RTS.invokevirtual_NAME_get(codeBuilder);
					codeBuilder.checkcast(RTS.CD.RTS_PRCQNT);
				}
			} else if (decl instanceof ProcedureDeclaration procedure) {
				if(procedure.myVirtual!=null) {
    	    		boolean withFollowSL = meaning.declaredIn.buildCTX(codeBuilder);
    	    		if(withFollowSL) codeBuilder.checkcast(meaning.declaredIn.getClassDesc());

    	    		ClassDesc owner = meaning.declaredIn.getClassDesc();
    	    		VirtualSpecification vir = procedure.myVirtual.virtualSpec;
    	    		vir.buildCallMethod(owner, codeBuilder);

				} else {
					codeBuilder
						.new_(RTS.CD.RTS_PRCQNT)
						.dup();
					var.buildIdentifierAccess(false, codeBuilder);
					codeBuilder
						.ldc(RTS.CD.classDesc(procIdent))
						.invokespecial(RTS.CD.RTS_PRCQNT, "<init>", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_RTObject;Ljava/lang/Class;)V"));
				}
			} else Util.IERR();
			
	    } else if (apar instanceof RemoteVariable rem) {
			Meaning meaning = rem.var.meaning;
			if (meaning.declaredAs instanceof ProcedureDeclaration procedure) {
    	    	if(procedure.myVirtual!=null) {
					rem.obj.buildEvaluation(null, codeBuilder);
					
			    	ClassDesc owner = meaning.declaredIn.getClassDesc();
    	    		VirtualSpecification vir = procedure.myVirtual.virtualSpec;
					vir.buildCallMethod(owner, codeBuilder);
    	    	} else {
    				codeBuilder
    					.new_(RTS.CD.RTS_PRCQNT)
    					.dup();
    				// Check for <ObjectExpression> DOT <Variable>
    				rem.obj.buildEvaluation(null,codeBuilder);
    				codeBuilder.ldc(procedure.getClassDesc());
    				codeBuilder.invokespecial(RTS.CD.RTS_PRCQNT
    						, "<init>", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_RTObject;Ljava/lang/Class;)V"));
    	    	}
			} else Util.IERR();
			
			
		} else Util.error("Illegal Procedure Expression as Actual Parameter: " + apar);
	}


	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(identifier).append("[externalIdent=").append(externalIdent).append("] Kind=").append(declarationKind);
		s.append(", declaredIn="+declaredIn.externalIdent);//.append(", rtBlockLevel="+getRTBlockLevel());
		s.append(", QUAL=").append(this.getClass().getSimpleName()).append(", HashCode=").append(hashCode());
		return (s.toString());
	}

}
