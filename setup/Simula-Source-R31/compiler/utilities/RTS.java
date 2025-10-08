/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.utilities;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import simula.compiler.syntaxClass.Type;

/// Predefined values for common constants, including descriptors for class types etc. 
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/RTS.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class RTS {
	
	/** Default Constructor. NOT USED */ private RTS() {} 
	/// Predefined values for descriptors. 
	public class CD {
		/** Default Constructor. NOT USED */ private CD() {} 
		/** ClassDesc */ public static final ClassDesc RTS_UTIL			  = ClassDesc.of("simula.runtime.RTS_UTIL");
		/** ClassDesc */ public static final ClassDesc RTS_RTObject       = ClassDesc.of("simula.runtime.RTS_RTObject");
		/** ClassDesc */ public static final ClassDesc RTS_ENVIRONMENT    = ClassDesc.of("simula.runtime.RTS_ENVIRONMENT"); 
		/** ClassDesc */ public static final ClassDesc RTS_BASICIO        = ClassDesc.of("simula.runtime.RTS_BASICIO");
		/** ClassDesc */ public static final ClassDesc RTS_CLASS          = ClassDesc.of("simula.runtime.RTS_CLASS");  
		/** ClassDesc */ public static final ClassDesc RTS_PROCEDURE      = ClassDesc.of("simula.runtime.RTS_PROCEDURE");
		/** ClassDesc */ public static final ClassDesc RTS_Simulation     = ClassDesc.of("simula.runtime.RTS_Simulation");
		/** ClassDesc */ public static final ClassDesc RTS_Process        = ClassDesc.of("simula.runtime.RTS_Process");
		/** ClassDesc */ public static final ClassDesc RTS_CatchingErrors = ClassDesc.of("simula.runtime.RTS_CatchingErrors");
		
		/** ClassDesc */ public static final ClassDesc RTS_TXT            = ClassDesc.of("simula.runtime.RTS_TXT");
		/** ClassDesc */ public static final ClassDesc RTS_ARRAY          = ClassDesc.of("simula.runtime.RTS_ARRAY");
		/** ClassDesc */ public static final ClassDesc RTS_BOUNDS         = ClassDesc.of("simula.runtime.RTS_BOUNDS");
		/** ClassDesc */ public static final ClassDesc RTS_LABEL          = ClassDesc.of("simula.runtime.RTS_LABEL");
		/** ClassDesc */ public static final ClassDesc RTS_NAME           = ClassDesc.of("simula.runtime.RTS_NAME"); 
		/** ClassDesc */ public static final ClassDesc RTS_PRCQNT         = ClassDesc.of("simula.runtime.RTS_PRCQNT");
		
		/** ClassDesc */ public static final ClassDesc FOR_List			  = ClassDesc.of("simula.runtime.FOR_List");
		/** ClassDesc */ public static final ClassDesc FOR_Element		  = ClassDesc.of("simula.runtime.FOR_Element");
		/** ClassDesc */ public static final ClassDesc FOR_SingleElt	  = ClassDesc.of("simula.runtime.FOR_SingleElt");
		/** ClassDesc */ public static final ClassDesc FOR_WhileElt		  = ClassDesc.of("simula.runtime.FOR_WhileElt");
		/** ClassDesc */ public static final ClassDesc FOR_StepUntil	  = ClassDesc.of("simula.runtime.FOR_StepUntil");
		
		/** ClassDesc */ public static final ClassDesc JAVA_LANG_MATH                = ClassDesc.of("java.lang.Math");  
		/** ClassDesc */ public static final ClassDesc JAVA_LANG_THROWABLE           = ClassDesc.of("java.lang.Throwable");  
		/** ClassDesc */ public static final ClassDesc JAVA_LANG_RUNTIME_EXCEPTION   = ClassDesc.of("java.lang.RuntimeException");
		/** ClassDesc */ public static final ClassDesc JAVA_LANG_CLASSCAST_EXCEPTION = ClassDesc.of("java.lang.ClassCastException");
		/** ClassDesc */ public static final ClassDesc JAVA_UTIL_ITERATOR            = ClassDesc.of("java.util.Iterator");
		
		/// Return ClassDesc for RTS_ARRAY
		/// @param type the Array Type
		/// @return ClassDesc for RTS_ARRAY
		public static ClassDesc RTS_ARRAY(Type type) {
			return(ClassDesc.of("simula.runtime."+type.getArrayType()));
		}
		
		/// Return ClassDesc for 'name'.
		/// @param name class simple name
		/// @return ClassDesc
		public static ClassDesc classDesc(String name) {
			return(ClassDesc.of(Global.packetName,name));
		}
	}
	
	/// Predefined values for Field Ref Entries. 
	public class FRE {
		/** Default Constructor. NOT USED */ private FRE() {} 
		// Static
		/// FieldRefEntry
		/// @param pool the ConstantPoolBuilder to use
		/// @return FieldRefEntry
		public static FieldRefEntry RTObject_CTX(ConstantPoolBuilder pool) { return pool.fieldRefEntry(RTS.CD.RTS_RTObject,"_CTX",RTS.CD.RTS_BASICIO); }
		/// FieldRefEntry
		/// @param pool the ConstantPoolBuilder to use
		/// @return FieldRefEntry
		public static FieldRefEntry RTObject_USR(ConstantPoolBuilder pool) { return pool.fieldRefEntry(RTS.CD.RTS_RTObject,"_USR",RTS.CD.RTS_BASICIO); }
		/// FieldRefEntry
		/// @param pool the ConstantPoolBuilder to use
		/// @return FieldRefEntry
		public static FieldRefEntry RTObject_CUR(ConstantPoolBuilder pool) { return pool.fieldRefEntry(RTS.CD.RTS_RTObject,"_CUR",RTS.CD.RTS_RTObject); }
		/// FieldRefEntry
		/// @param pool the ConstantPoolBuilder to use
		/// @return FieldRefEntry
		public static FieldRefEntry NAME_CUR(ConstantPoolBuilder pool)     { return pool.fieldRefEntry(RTS.CD.RTS_NAME,"_CUR",RTS.CD.RTS_RTObject); }
		/// FieldRefEntry
		/// @param pool the ConstantPoolBuilder to use
		/// @return FieldRefEntry
		public static FieldRefEntry RTObject_JTX(ConstantPoolBuilder pool) { return pool.fieldRefEntry(RTS.CD.RTS_RTObject,"_JTX", ConstantDescs.CD_int); }

		// Dynamic
		/// FieldRefEntry
		/// @param pool the ConstantPoolBuilder to use
		/// @return FieldRefEntry
		public static FieldRefEntry RTObject_SL(ConstantPoolBuilder pool) {	return pool.fieldRefEntry(RTS.CD.RTS_RTObject,"_SL",RTS.CD.RTS_RTObject); }
		/// FieldRefEntry
		/// @param pool the ConstantPoolBuilder to use
		/// @return FieldRefEntry
		public static FieldRefEntry PROCEDURE_nParLeft(ConstantPoolBuilder pool) { return pool.fieldRefEntry(RTS.CD.RTS_PROCEDURE,"_nParLeft",ConstantDescs.CD_int); }
		/// FieldRefEntry
		/// @param pool the ConstantPoolBuilder to use
		/// @return FieldRefEntry
		public static FieldRefEntry LABEL_index(ConstantPoolBuilder pool) { return pool.fieldRefEntry(RTS.CD.RTS_LABEL,"index", ConstantDescs.CD_int); }

	}

	// ********************************************************************************************
	// *** OWNER: RTS_NAME
	// ********************************************************************************************
	
	/// Builds: invoke NAME put code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_NAME_put(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_NAME;
		codeBuilder.invokevirtual(owner, "put", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Ljava/lang/Object;"));
	}
	
	/// Builds: invoke NAME get code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_NAME_get(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_NAME;
		codeBuilder.invokevirtual(owner, "get", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"));
	}

	
	// ********************************************************************************************
	// *** OWNER: RTS_Simulation
	// ********************************************************************************************
	
	/// Builds: invoke Simulation ActivateDirect code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_Simulation_ActivateDirect(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_Simulation;
		codeBuilder.invokevirtual(owner, "ActivateDirect", MethodTypeDesc.ofDescriptor("(ZLsimula/runtime/RTS_Process;)V"));
	}

	/// Builds: invoke Simulation ActivateAt code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_Simulation_ActivateAt(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_Simulation;
		codeBuilder.invokevirtual(owner, "ActivateAt", MethodTypeDesc.ofDescriptor("(ZLsimula/runtime/RTS_Process;DZ)V"));
	}

	/// Builds: invoke Simulation ActivateDelay code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_Simulation_ActivateDelay(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_Simulation;
		codeBuilder.invokevirtual(owner, "ActivateDelay", MethodTypeDesc.ofDescriptor("(ZLsimula/runtime/RTS_Process;DZ)V"));
	}

	/// Builds: invoke Simulation ActivateBefore code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_Simulation_ActivateBefore(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_Simulation;
		codeBuilder.invokevirtual(owner, "ActivateBefore", MethodTypeDesc.ofDescriptor("(ZLsimula/runtime/RTS_Process;Lsimula/runtime/RTS_Process;)V"));
	}

	/// Builds: invoke Simulation ActivateAfter code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_Simulation_ActivateAfter(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_Simulation;
		codeBuilder.invokevirtual(owner, "ActivateAfter", MethodTypeDesc.ofDescriptor("(ZLsimula/runtime/RTS_Process;Lsimula/runtime/RTS_Process;)V"));
	}

	
	// ********************************************************************************************
	// *** OWNER: RTS_Process
	// ********************************************************************************************
	
	/// Builds: invoke Process terminate code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_Process_terminate(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_Process;
		codeBuilder.invokevirtual(owner,"terminate", MethodTypeDesc.ofDescriptor("()V"));
	}

	
	// ********************************************************************************************
	// *** OWNER: FOR_List
	// ********************************************************************************************
	
	/// Builds: invoke FOR_List iterator code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_FOR_List_iterator(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.FOR_List;
		codeBuilder.invokevirtual(owner, "iterator", MethodTypeDesc.ofDescriptor("()Ljava/util/Iterator;"));
	}

	
	// ********************************************************************************************
	// *** OWNER: RTS_CLASS
	// ********************************************************************************************
	
	/// Builds: invoke CLASS START code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_CLASS_START(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_CLASS;
		codeBuilder.invokevirtual(owner, "_START", MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_RTObject;"));
	}

	
	// ********************************************************************************************
	// *** OWNER: RTS_PROCEDURE
	// ********************************************************************************************
	
	/// Builds: invoke PROCEDURE setpar code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_PROCEDURE_setpar(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_PROCEDURE;
		codeBuilder.invokevirtual(owner, "setPar", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Lsimula/runtime/RTS_PROCEDURE;"));
	}
	
	/// Builds: invoke PROCEDURE ENT code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_PROCEDURE_ENT(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_PROCEDURE;
		codeBuilder.invokevirtual(owner, "_ENT", MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_PROCEDURE;"));
	}

	/// Builds: invoke PROCEDURE RESULT code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_PROCEDURE_RESULT(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_PROCEDURE;
		codeBuilder.invokevirtual(owner, "_RESULT", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"));
	}

	
	// ********************************************************************************************
	// *** OWNER: RTS_PRCQNT
	// ********************************************************************************************
	
	/// Builds: invoke PRCQNT CPF code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_PRCQNT_CPF(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_PRCQNT;
		codeBuilder.invokevirtual(owner, "CPF", MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_PROCEDURE;"));
	}

	
	// ********************************************************************************************
	// *** OWNER: RTS_ARRAY
	// ********************************************************************************************
	
	/// Builds: invoke ARRAY copy code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_ARRAY_copy(CodeBuilder codeBuilder) {
		codeBuilder.invokevirtual(RTS.CD.RTS_ARRAY, "COPY", MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_ARRAY;"));
	}
	
	/// Builds: invoke ARRAY index code.
	/// @param type the type of the Array.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_ARRAY_index(Type type, CodeBuilder codeBuilder) {
		ClassDesc CD_ArrayType=RTS.CD.RTS_ARRAY(type);
		codeBuilder.invokevirtual(CD_ArrayType, "index", MethodTypeDesc.ofDescriptor("([I)I"));
	}
	
	/// Builds: invoke ARRAY putELEMENT code.
	/// @param type the type of the Array.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_ARRAY_putELEMENT(Type type, CodeBuilder codeBuilder) {
		ClassDesc CD_ArrayType=RTS.CD.RTS_ARRAY(type);
		String eltType = (type.isRefClassType())?"Ljava/lang/Object;":type.toJVMType();
		codeBuilder.invokevirtual(CD_ArrayType, "putELEMENT", MethodTypeDesc.ofDescriptor("(I"+eltType+")"+eltType));
	}
	
	/// Builds: invoke ARRAY getELEMENT code.
	/// @param type the type of the Array.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_ARRAY_getELEMENT(Type type, CodeBuilder codeBuilder) {
		String eltType = type.toJVMType();
		MethodTypeDesc MTD=(type.isRefClassType())?MethodTypeDesc.ofDescriptor("([I)Ljava/lang/Object;")
		                                          :MethodTypeDesc.ofDescriptor("([I)"+eltType);
		codeBuilder.invokevirtual(RTS.CD.RTS_ARRAY(type), "getELEMENT", MTD);
		if(type.isReferenceType())
			codeBuilder.checkcast(type.toClassDesc());
	}


	// ********************************************************************************************
	// *** OWNER: RTS_ENVIRONMENT
	// ********************************************************************************************

	/// Builds: invoke ENVIRONMENT copy code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_ENVIRONMENT_copy(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_ENVIRONMENT;
		codeBuilder.invokestatic(owner, "copy", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_TXT;)Lsimula/runtime/RTS_TXT;"));
	}


	// ********************************************************************************************
	// *** OWNER: RTS_UTIL
	// ********************************************************************************************
	
	/// Builds: invoke UTIL copy code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_BPRG(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		codeBuilder.invokestatic(owner, "BPRG", MethodTypeDesc.ofDescriptor("(Ljava/lang/String;[Ljava/lang/String;)V"));
	}
	
	/// Builds: invoke UTIL copy code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_RUN_STM(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		codeBuilder.invokestatic(owner, "RUN_STM", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_RTObject;)V"));
	}

	/// Builds: invoke UTIL ASGTXT code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_ASGTXT(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		MethodTypeDesc MTD=MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_TXT;Lsimula/runtime/RTS_TXT;)Lsimula/runtime/RTS_TXT;");
		codeBuilder.invokestatic(owner, "_ASGTXT", MTD);
	}

	/// Builds: invoke UTIL ASGSTR code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_ASGSTR(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		MethodTypeDesc MTD=MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_TXT;Ljava/lang/String;)Lsimula/runtime/RTS_TXT;");
		codeBuilder.invokestatic(owner, "_ASGSTR", MTD);
	}
	
	/// Builds: invoke UTIL Text relation code.
	/// @param rel the relation code
	/// @param codeBuilder the codeBuilder to use.
	public static void buildInvokeTextRel(int rel, CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		String textRelMethod=null;
		switch (rel) {
			case KeyWord.GE -> textRelMethod="_TXTREL_GE";
			case KeyWord.NE -> textRelMethod="_TXTREL_NE";
			case KeyWord.GT -> textRelMethod="_TXTREL_GT";
			case KeyWord.LE -> textRelMethod="_TXTREL_LE";
			case KeyWord.EQ -> textRelMethod="_TXTREL_EQ";
			case KeyWord.LT -> textRelMethod="_TXTREL_LT";
			case KeyWord.EQR -> textRelMethod="TRF_EQ";
			case KeyWord.NER -> textRelMethod="TRF_NE";
			default -> Util.IERR();
		}
		MethodTypeDesc MTD=MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_TXT;Lsimula/runtime/RTS_TXT;)Z");
		codeBuilder.invokestatic(owner, textRelMethod, MTD);
	}


	/// Builds: invoke UTIL treatException code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_treatException(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		codeBuilder.invokestatic(owner, "treatException",
				MethodTypeDesc.ofDescriptor("(Ljava/lang/Throwable;Lsimula/runtime/RTS_RTObject;)V"));
	}

	/// Builds: invoke UTIL IADD code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_IADD(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		codeBuilder.invokestatic(owner, "_IADD", MethodTypeDesc.ofDescriptor("(II)I"));
	}

	/// Builds: invoke UTIL ISUB code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_ISUB(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		codeBuilder.invokestatic(owner, "_ISUB", MethodTypeDesc.ofDescriptor("(II)I"));
	}

	/// Builds: invoke UTIL IMUL code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_IMUL(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		codeBuilder.invokestatic(owner, "_IMUL", MethodTypeDesc.ofDescriptor("(II)I"));
	}

	/// Builds: invoke UTIL IPOW code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_IPOW(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		codeBuilder.invokestatic(owner, "_IPOW", MethodTypeDesc.ofDescriptor("(II)I"));
	}
	
	/// Builds: invoke UTIL IS code.
	/// @param classDesc the class descriptor.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_IS(ClassDesc classDesc, CodeBuilder codeBuilder) {
		codeBuilder
			.ldc(classDesc)
			.invokestatic(CD.RTS_UTIL, "_IS", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;Ljava/lang/Class;)Z"));
	}
	
	/// Builds: invoke UTIL sign(DELTA) code.
	/// @param type the type
	/// @param DELTA the argument
	/// @param codeBuilder the codeBuilder to use.
	public static void invokestatic_UTIL_sign(Type type, int DELTA, CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_UTIL;
		switch(type.keyWord) {
			case Type.T_INTEGER   -> { codeBuilder.iload(DELTA).invokestatic(owner, "isign", MethodTypeDesc.ofDescriptor("(I)I")); }
			case Type.T_REAL      -> { codeBuilder.fload(DELTA).invokestatic(owner, "fsign", MethodTypeDesc.ofDescriptor("(F)F")); }
			case Type.T_LONG_REAL -> { codeBuilder.dload(DELTA).invokestatic(owner, "dsign", MethodTypeDesc.ofDescriptor("(D)D")); }
			default -> Util.IERR();
		}
	}

	/// Debugging utility - Builds: invoke UTIL _SNAPSHOT code* 
	/// @param codeBuilder the codeBuilder to use.
	/// @param stx the string to output
	public static void buildSNAPSHOT(CodeBuilder codeBuilder, String stx) {
		// SnapShot
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		codeBuilder
			.sipush(0)
			.ldc(pool.stringEntry(stx.toString()))
			.invokestatic(ClassDesc.of("simula.runtime.RTS_UTIL"), "_SNAPSHOT", MethodTypeDesc.ofDescriptor("(ILjava/lang/String;)V"));
	}

	/// Debugging utility - Builds: invoke UTIL _SNAPSHOT code* 
	/// @param codeBuilder the codeBuilder to use.
	/// @param stx the string to output
	public static void buildSNAPSHOT2(CodeBuilder codeBuilder, String stx) {
		// SnapShot
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		codeBuilder
			.dup()
			.sipush(0)
			.ldc(pool.stringEntry(stx.toString()))
			.invokestatic(ClassDesc.of("simula.runtime.RTS_UTIL"), "_SNAPSHOT", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;ILjava/lang/String;)V"));
	}


	/// Debugging utility - Builds: invoke UTIL _SNAPSHOT code* 
	/// @param codeBuilder the codeBuilder to use.
	/// @param stx the string to output
	public static void buildSNAPSHOT2F(CodeBuilder codeBuilder, String stx) {
		// SnapShot
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		codeBuilder
			.dup()
			.sipush(0)
			.ldc(pool.stringEntry(stx.toString()))
			.invokestatic(ClassDesc.of("simula.runtime.RTS_UTIL"), "_SNAPSHOT", MethodTypeDesc.ofDescriptor("(FILjava/lang/String;)V"));
	}
	
	
	
	// ********************************************************************************************
	// *** OWNER: RTS_CatchingErrors
	// ********************************************************************************************
	
	/// Builds: invoke CatchingErrors onError code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_CatchingErrors_onError(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_CatchingErrors;
		codeBuilder.invokevirtual(owner, "_onError", MethodTypeDesc.ofDescriptor("(Ljava/lang/RuntimeException;Lsimula/runtime/RTS_PRCQNT;)V"));
	}	

	
	// ********************************************************************************************
	// *** OWNER: RTS_RTOBJECT
	// ********************************************************************************************
	
	/// Convert a Runtime Object to a primitive type value.
	/// 
	/// If the input Object is a name parameter or a parameter procedure it evaluated before the conversion.
	/// @param type the type
	/// @param codeBuilder the CodeBuilder to use
	/// @return true if the value is converted; otherwise false
	public static boolean objectToPrimitiveType(Type type, CodeBuilder codeBuilder) {
		// Object TOS value ==> possibleEvaluation ==> Primitive type
		ClassDesc owner = RTS.CD.RTS_RTObject;
		switch(type.keyWord) {
			case Type.T_INTEGER   -> codeBuilder.invokevirtual(owner,"intValue", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)I"));
			case Type.T_REAL      -> codeBuilder.invokevirtual(owner,"floatValue", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)F"));
			case Type.T_LONG_REAL -> codeBuilder.invokevirtual(owner,"doubleValue", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)D"));
			case Type.T_BOOLEAN   -> codeBuilder.invokevirtual(owner,"booleanValue", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Z"));
			case Type.T_CHARACTER -> codeBuilder.invokevirtual(owner,"charValue", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)C"));
			default               -> { return false; }
		}
		return true;
	}
	

	/// Builds: invoke RTObject BBLK code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_RTObject_BBLK(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_RTObject;
		codeBuilder.invokevirtual(owner, "BBLK", MethodTypeDesc.ofDescriptor("()V"));
	}
	
	/// Builds: invoke RTObject EBLK code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_RTObject_EBLK(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_RTObject;
		codeBuilder.invokevirtual(owner, "EBLK", MethodTypeDesc.ofDescriptor("()V"));
	}
	
	/// Builds: invoke RTS arrayValue code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_RTS_arrayValue(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_RTObject;
		codeBuilder.invokevirtual(owner, "arrayValue", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Lsimula/runtime/RTS_ARRAY;"));
	}
	
	/// Builds: invoke RTS procValue code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_RTS_procValue(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_RTObject;
		codeBuilder.invokevirtual(owner, "procValue", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Lsimula/runtime/RTS_PRCQNT;"));
	}
	
	/// Builds: invoke RTS objectValue code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_RTS_objectValue(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_RTObject;
		codeBuilder.invokevirtual(owner, "objectValue", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Ljava/lang/Object;"));
	}
	
	/// Builds: invoke RTS CONC code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_RTS_CONC(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_RTObject;
		codeBuilder.invokevirtual(owner, "CONC", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_TXT;Lsimula/runtime/RTS_TXT;)Lsimula/runtime/RTS_TXT;"));
	}
	
	/// Builds: invoke RTS GOTO code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_RTS_GOTO(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_RTObject;
		codeBuilder.invokevirtual(owner,"_GOTO", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_LABEL;)V"));
	}

	/// Builds: invoke RTS detach code.
	/// @param codeBuilder the codeBuilder to use.
	public static void invokevirtual_RTS_detach(CodeBuilder codeBuilder) {
		ClassDesc owner = CD.RTS_RTObject;
		codeBuilder.invokevirtual(owner,"detach", MethodTypeDesc.ofDescriptor("()V"));
	}

	
}
