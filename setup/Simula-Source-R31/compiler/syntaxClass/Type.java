/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass;

import java.lang.classfile.ClassSignature;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;

import simula.compiler.syntaxClass.declaration.ClassDeclaration;
import simula.compiler.syntaxClass.declaration.ConnectionBlock;
import simula.compiler.syntaxClass.declaration.Declaration;
import simula.compiler.syntaxClass.declaration.DeclarationScope;
import simula.compiler.syntaxClass.declaration.Parameter;
import simula.compiler.syntaxClass.declaration.StandardClass;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Utility class Type.
/// <pre>
/// Syntax: 
///     type = value-type
///          | reference-type
///          
///        value-type = arithmetic-type
///                   | boolean
///                   | character
///                   
///           arithmetic-type = integer-type
///                           | real-type
///                           
///              integer-type = [ short ] integer
///              
///              real-type = [ long ] real
///              
///              reference-type = object-reference-type
///                             | text
///                             
///                 object-reference-type = ref "(" qualification ")"
///                 
///                    qualification = class-identifier
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/Type.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class Type extends SyntaxClass {

	/// The keyWord attribute
	public int keyWord;
	
	/// The Class Identifier in case of ref(classIdent)
	public String classIdent;
	
	/// Qual in case of ref(Qual) type. Set by doChecking
	protected ClassDeclaration qual;   // Qual in case of ref(Qual) type; Set by doChecking below
	
	/// Qual's scope in case of ref(Qual) type. Set by doChecking
	public ConnectionBlock declaredIn; // Qual'scope in case of ref(Qual) type; Set by special constructor

	// **************************************************************************************************
	// *** BASIC TYPES
	// **************************************************************************************************
	/** Simula's No type */			public static final int T_UNDEF = 0;
	/** Simula's Integer type */	public static final int T_INTEGER = 1;
	/** Simula's Real type */		public static final int T_REAL = 2;
	/** Simula's Long Real type */	public static final int T_LONG_REAL = 3;
	/** Simula's Boolean type */	public static final int T_BOOLEAN = 4;
	/** Simula's Character type */	public static final int T_CHARACTER = 5;
	/** Simula's Text type */		public static final int T_TEXT = 6;
	/** Simula's Ref() type */		public static final int T_REF = 7;
	/** Simula's Procedure type */	public static final int T_PROCEDURE = 8;
	/** Simula's Label type */		public static final int T_LABEL = 9;
	

	/** Simula's Undefined type */	public static final Type Undef = new Type(Type.T_UNDEF);
	/** Simula's Integer type */	public static final Type Integer = new Type(Type.T_INTEGER);
	/** Simula's Real type */		public static final Type Real = new Type(Type.T_REAL);
	/** Simula's Long Real type */	public static final Type LongReal = new Type(Type.T_LONG_REAL);
	/** Simula's Boolean type */	public static final Type Boolean = new Type(Type.T_BOOLEAN);
	/** Simula's Character type */	public static final Type Character = new Type(Type.T_CHARACTER);
	/** Simula's Text type */		public static final Type Text = new Type(Type.T_TEXT);
	/** Simula's Ref() type */		public static final Type Ref = new Type(Type.T_REF);
	/** Simula's Procedure type */	public static final Type Procedure = new Type(Type.T_PROCEDURE);
	/** Simula's Label type */		public static final Type Label = new Type(Type.T_LABEL);
	
	/// Simula's Ref(classIdent) type
	/// @param classIdent the class name
	/// @return a new ref(classIdent) type.
	public static final Type Ref(String classIdent) { return (new Type(Type.T_REF,classIdent)); }
	

	// **************************************************************************************************
	// *** TYPE CREATION - CONSTRUCTORES 
	// **************************************************************************************************
	  
	/// Create a new simple Type with the given keyWord
	/// @param keyWord the given keyWord
	private Type(int keyWord) {
		this.keyWord = keyWord;
	}
	
	/// Create a new simple Type with the given keyWord and classIdent
	/// @param keyWord the given keyWord
	/// @param classIdent the given class identifier
	public Type(int keyWord, String classIdent) {
		if(classIdent != null && !Option.CaseSensitive) classIdent = classIdent.toUpperCase();
		this.keyWord = keyWord;
		this.classIdent = classIdent;
	}

	/// Create a new ref(classIdent) type.
	/// @param classIdent the class name
	public Type(String classIdent) {
		this(T_REF,classIdent);
	}
	
	/// Create a new Type based on the given Type and ConnectionBlock.
	/// @param tp the given Type
	/// @param declaredIn the ConnectionBlock
	public Type(Type tp,ConnectionBlock declaredIn) {
		this.keyWord = tp.keyWord;
		this.classIdent = tp.classIdent;
		
		this.qual = tp.qual;
		this.declaredIn = declaredIn;
	}
	
	
	// **************************************************************************************************
	// *** UTILITIES
	// **************************************************************************************************

	/// Returns the qualifying ClassDeclaration or null.
	/// @return the qualifying ClassDeclaration or null
	public ClassDeclaration getQual() {
		ASSERT_SEMANTICS_CHECKED();
		return (qual);
	}

	/// Check if types are equals or subordinate.
	/// @param t1 a type
	/// @param t2 a type
	/// @return true: if types are equals or subordinate.
	public static boolean equalsOrSubordinate(Type t1, Type t2) {
		if(t1 == null) return(t2 == null);
		if(t2 == null) return(t1 == null);
		if(t2.keyWord == Type.T_REF) {
			if(t2.isSubReferenceOf(t1)) return true;
		}
		return t1.equals(t2);
	}
	
	@Override
	public boolean equals(Object obj) {
		Type other=(Type) obj;
		if(this.keyWord != other.keyWord) return(false);
		if (this.classIdent == other.classIdent) return (true);
		if (this.classIdent == null) return (false);
		if (other.classIdent == null) return (false);
//		if (!this.classIdent.equals(other.classIdent)) return (false);
		if (!this.classIdent.equalsIgnoreCase(other.classIdent)) return (false);
		return (true);
	}
	
	/// Returns the ref-identifier or null.
	/// @return the ref-identifier or null
	public String getRefIdent() {
		if(keyWord == Type.T_REF) return(classIdent);
		return(null); 
	}
  
	/// Returns the Java ref-identifier or null.
	/// @return the Java ref-identifier or null
	public String getJavaRefIdent() {
		if(keyWord == Type.T_REF) {
			if(classIdent == null) return("RTS_RTObject");
			if(!IS_SEMANTICS_CHECKED()) this.doChecking(Global.getCurrentScope());
			if(qual==null) return("UNKNOWN");
			return(qual.getJavaIdentifier());
		}
		return(null); 
	}
	
	/// Perform semantic checking in the given scope.
	/// @param scope the given scope
	public void doChecking(final DeclarationScope scope) {
		if(qual!=null) SET_SEMANTICS_CHECKED(); // This Ref-Type is read from attribute file
		if(IS_SEMANTICS_CHECKED()) return;
		Global.enterScope(scope);
		String refIdent=getRefIdent();
		if(refIdent!=null) {
			if(!refIdent.equals("RTS_LABEL") && !refIdent.equals("_UNKNOWN")) {
				Declaration decl=scope.findMeaning(refIdent).declaredAs;
			    if(decl instanceof ClassDeclaration cdecl) qual=cdecl;
			    else Util.error("Illegal Type: "+this.toString()+" - "+refIdent+" is not a Class");
			}
		}
		Global.exitScope();
		SET_SEMANTICS_CHECKED();
	}

	/// Returns true if this type is an arithmetic type.
	/// Integer, real or long real.
	/// @return true if this type is an arithmetic type.
	public boolean isArithmeticType() {
		switch(keyWord) {
			case T_INTEGER,T_REAL,T_LONG_REAL: return true;
			default: return false;
		}
	}
		
	/// Returns true if this type is a value type.
	/// Integer, real or long real.
	/// @return true if this type is a value type.
	public boolean isValueType() {
		switch(keyWord) {
			case T_INTEGER,T_REAL,T_LONG_REAL,T_BOOLEAN,T_CHARACTER: return true;
			default: return false;
		}
	}
  
	/// Returns true if this type is ref or text type.
	/// @return true if this type is ref or text type
	public boolean isReferenceType() {
		if(keyWord == Type.T_REF) return(true);
		if(keyWord == Type.T_TEXT) return(true);
		return false;
	}
	  
	/// Returns true if this type is ref type.
	/// @return true if this type is ref type
	public boolean isRefClassType() {
		if(keyWord == Type.T_REF) return(true);
		return false;
	}
  
	/// Utility enum ConversionKind.
    public enum ConversionKind { 
    	/// Type conversion is illegal.
    	Illegal,
    	/// No type-conversion is necessary. E.g. integer to integer.
    	DirectAssignable,
    	/// Type-conversion with possible Runtime check is necessary. E.g. real to integer.
    	ConvertValue,
    	/// Type-conversion with Runtime check is necessary. E.g. ref(A) to ref(B) where B is a subclass of A.
    	ConvertRef
    }

    /// Checks if a type-conversion is legal.
    /// 
    /// The possible return values are:
    /// 
    ///  - DirectAssignable - No type-conversion is necessary. E.g. integer to integer
    ///  - ConvertValue - Type-conversion with possible Runtime check is necessary. E.g. real to integer.
    ///  - ConvertRef - Type-conversion with Runtime check is necessary. E.g. ref(A) to ref(B) where B is a subclass of A.
    ///  - Illegal - Conversion is illegal
    /// @param to the to type-
    /// @return the resulting ConversionKind
    public ConversionKind isConvertableTo(final Type to) {
	    ConversionKind result;
	    if(to==null) result=ConversionKind.Illegal;
	    else if(to.keyWord == T_UNDEF) result=ConversionKind.DirectAssignable;
	    else if(this.equals(to)) result=ConversionKind.DirectAssignable;
	    else if(this.isArithmeticType()&&to.isArithmeticType()) result=ConversionKind.ConvertValue;
	    else if(this.isSubReferenceOf(to)) result=ConversionKind.DirectAssignable;  
	    else if(to.isSubReferenceOf(this)) result=ConversionKind.ConvertRef; // Needs Runtime-Check
	    else result=ConversionKind.Illegal;
	    return(result); 
    }
  
    /// Utility method isSubReferenceOf.
    /// 
    /// ref(B) is a sub-reference of ref(A) iff B is a subclass of A.
    /// 
    /// Any ref is a sub-reference of NONE
    /// 
    /// @param other the other type
    /// @return true if the condition holds
	public boolean isSubReferenceOf(final Type other) {
		String thisRef=this.getRefIdent();   // May be null for NONE
		String otherRef=other.getRefIdent(); // May be null for NONE
		boolean result;		
		if(otherRef==null) result=false;     // No ref is a super-reference of NONE
		else if(thisRef==null) result=true;  // Any ref is a sub-reference of NONE
		else {
			ClassDeclaration thisDecl=(ClassDeclaration)Global.getCurrentScope().findMeaning(thisRef).declaredAs;
			ClassDeclaration otherDecl=(ClassDeclaration)Global.getCurrentScope().findMeaning(otherRef).declaredAs;
			if(thisDecl==null) result=false; // Error Recovery
			else result=thisDecl.isSubClassOf(otherDecl);
		}
		return(result); 
	}
  
	/// Returns the type to which both types can be converted.
	/// @param type1 argument
	/// @param type2 argument
	/// @return the resulting Type
	public static Type commonRefType(final Type type1,final Type type2) {
		if(type1.isSubReferenceOf(type2)) return(type2);
	    if(type2.isSubReferenceOf(type1)) return(type1);
		return(type1);
	}
  
	/// Returns the type to which both types can be converted.
	/// @param type1 argument
	/// @param type2 argument
	/// @return the resulting Type
	public static Type commonTypeConversion(final Type type1,final Type type2) {
		if(type1.equals(type2)) return(type1);
		Type atype=arithmeticTypeConversion(type1,type2);
		if(atype!=null) return(atype);
		if(type1.isReferenceType() && type2.isReferenceType()) {
			if(type1.isSubReferenceOf(type2)) return(type2);
		    if(type2.isSubReferenceOf(type1)) return(type1);
		    Util.error("Incompatible types: "+type1+", "+type2);
		    return(type1);
		}
		Util.error("Incompatible types: "+type1+", "+type2);
		return(null);
	}
	
	/// Returns the most dominant type.
	/// @param type1 argument
	/// @param type2 argument
	/// @return the most dominant type
	public static Type arithmeticTypeConversion(final Type type1,final Type type2) {
		switch(type1.keyWord) {
			case T_INTEGER:
				switch(type2.keyWord) {
					case T_INTEGER:		return(Type.Integer); 
					case T_REAL:		return(Type.Real);
					case T_LONG_REAL:	return(Type.LongReal);
				}
			case T_REAL:
				switch(type2.keyWord) {
					case T_INTEGER:		return(Type.Real); 
					case T_REAL:		return(Type.Real);
					case T_LONG_REAL:	return(Type.LongReal);
				}
			case T_LONG_REAL:
				switch(type2.keyWord) {
					case T_INTEGER:		return(Type.LongReal); 
					case T_REAL:		return(Type.LongReal);
					case T_LONG_REAL:	return(Type.LongReal);
				}
		}
		return null;
	}
  
	/// Returns an edited default value of this Type.
	/// @return an edited default value of this Type
	public String edDefaultValue() {
		switch(keyWord) {
			case T_UNDEF:		return("void");
			case T_BOOLEAN:		return("false");
			case T_CHARACTER:	return("0");
			case T_INTEGER:		return("0");
			case T_REAL:		return("0.0f");
			case T_LONG_REAL:	return("0.0d");
			case T_TEXT:		return("null");
			case T_LABEL:		return("null");
			case T_REF:			return("null");
			default: Util.IERR(); return null;
		}
	}
  
	/// ClassFile coding utility: toJavaType.
	/// @return the resulting code string.
	public String toJavaType() {
		switch(keyWord) {
			case T_UNDEF:		return("void");
			case T_BOOLEAN:		return("boolean");
			case T_CHARACTER:	return("char");
			case T_INTEGER:		return("int");
			case T_REAL:		return("float");
			case T_LONG_REAL:	return("double");
			case T_TEXT:		return("RTS_TXT");
			case T_LABEL:		return("RTS_LABEL");
			case T_REF:			return(getJavaRefIdent());
			default: Util.IERR(); return null;
		}
	}
	 
	/// ClassFile coding utility: toJavaTypeClass.
	/// @return the resulting code string.
	public String toJavaTypeClass() {
		switch(keyWord) {
			case T_UNDEF:		return("void");
			case T_BOOLEAN:		return("Boolean");
			case T_CHARACTER:	return("Character");
			case T_INTEGER:		return("Integer");
			case T_REAL:		return("Float");
			case T_LONG_REAL:	return("Double");
			case T_TEXT:		return("RTS_TXT");
			case T_LABEL:		return("RTS_LABEL");
			case T_REF:			return(getJavaRefIdent());
			default: Util.IERR(); return null;
		}
	}
	
	/// ClassFile coding utility: toJavaArrayType.
	/// @return the resulting code string.
	public String toJavaArrayType() {
		switch(keyWord) {
			case T_BOOLEAN:		return("RTS_BOOLEAN_ARRAY");
			case T_CHARACTER:	return("RTS_CHARACTER_ARRAY");
			case T_INTEGER:		return("RTS_INTEGER_ARRAY");
			case T_REAL:		return("RTS_REAL_ARRAY");
			case T_LONG_REAL:	return("RTS_LONG_REAL_ARRAY");
			case T_TEXT:		return("RTS_TEXT_ARRAY");
			case T_REF:			return("RTS_REF_ARRAY<"+getJavaRefIdent()+">");
			default: Util.IERR(); return null;
		}

	}
	
	/// Return RTS Array type String.
	/// @return RTS Array type String.
	public String getArrayType() {
		switch(keyWord) {
			case T_BOOLEAN:		return("RTS_BOOLEAN_ARRAY");
			case T_CHARACTER:	return("RTS_CHARACTER_ARRAY");
			case T_INTEGER:		return("RTS_INTEGER_ARRAY");
			case T_REAL:		return("RTS_REAL_ARRAY");
			case T_LONG_REAL:	return("RTS_LONG_REAL_ARRAY");
			case T_TEXT:		return("RTS_TEXT_ARRAY");
			case T_REF:			return("RTS_REF_ARRAY");
			default: Util.IERR(); return null;
		}
	}

	/// ClassFile coding utility: toJVMType.
	/// @param kind kind code.
	/// @param mode mode code.
	/// @return the resulting code string.
	public String toJVMType(int kind,int mode) {
		String jvmType=toClassDesc(kind,mode).descriptorString();
		return(jvmType);
	}
	
	/// ClassFile coding utility: toJVMType.
	/// @return the resulting code string.
	public String toJVMType() {
		switch(keyWord) {
			case T_UNDEF:		return("V");
			case T_BOOLEAN:		return("Z");
			case T_CHARACTER:	return("C");
			case T_INTEGER:		return("I");
			case T_REAL:		return("F");
			case T_LONG_REAL:	return("D");
			case T_TEXT:		return("Lsimula/runtime/RTS_TXT;");
			case T_PROCEDURE:	return("Lsimula/runtime/RTS_PRCQNT;");
			case T_LABEL:		return("Lsimula/runtime/RTS_LABEL;");
			case T_REF: {
				String refIdent=this.getJavaRefIdent();
				if(this.getQual() instanceof StandardClass)
					return("Lsimula/runtime/"+refIdent+";");
				else if(refIdent.startsWith("RTS_"))
					return("Lsimula/runtime/"+refIdent+";");
				else return("L"+Global.packetName+"/"+refIdent+";");
			}
			default: Util.IERR(); return null;
		}
	}

	/// ClassFile coding utility: toJVMType. Used by: Thunk.buildClassFile.
	/// @param type a type
	/// @param kind a kind code
	/// @return the resulting code string.
	public static String toJVMClassType(Type type,int kind) {
		if(kind == Parameter.Kind.Procedure) return("Lsimula/runtime/RTS_PRCQNT;");
		switch(type.keyWord) {
			case T_UNDEF:		return("V");
			case T_BOOLEAN:		return("Ljava/lang/Boolean;");
			case T_CHARACTER:	return("Ljava/lang/Character;");
			case T_INTEGER:		return("Ljava/lang/Integer;");
			case T_REAL:		return("Ljava/lang/Float;");
			case T_LONG_REAL:	return("Ljava/lang/Double;");
			case T_TEXT:		return("Lsimula/runtime/RTS_TXT;");
			case T_PROCEDURE:	return("Lsimula/runtime/RTS_PRCQNT;");
			case T_LABEL:		return("Lsimula/runtime/RTS_LABEL;");
			case T_REF: {
				String refIdent=type.getJavaRefIdent();
				if(type.getQual() instanceof StandardClass)
					return("Lsimula/runtime/"+refIdent+";");
				else if(refIdent.startsWith("RTS_"))
					return("Lsimula/runtime/"+refIdent+";");
				else return("L"+Global.packetName+"/"+refIdent+";");
			}
			default: Util.IERR(); return null;
		}
	}

	/// ClassFile coding utility: toClassDesc.
	/// @param declaredIn the owner.
	/// @return the resulting Class Descriptor.
	public ClassDesc toClassDesc(Declaration declaredIn) {
		if(keyWord == Type.T_REF) {
			String refID=getJavaRefIdent();
			if(declaredIn != null) {
				if(declaredIn.declarationKind == ObjectKind.StandardClass) {
					ClassDesc classDesc = ClassDesc.of("simula.runtime."+refID);
					return(classDesc);
				}
				if(declaredIn.declarationKind == ObjectKind.MemberMethod) {
					ClassDesc classDesc = ClassDesc.of("simula.runtime."+refID);
					return(classDesc);
				}
			}
		}
		return(toClassDesc());
	}

	/// ClassFile coding utility: toClassDesc.
	/// @return the resulting Class Descriptor.
	public ClassDesc toClassDesc() {
		switch(keyWord) {
			case T_UNDEF:		return(ConstantDescs.CD_void);
			case T_BOOLEAN:		return(ConstantDescs.CD_boolean);
			case T_CHARACTER:	return(ConstantDescs.CD_char);
			case T_INTEGER:		return(ConstantDescs.CD_int);
			case T_REAL:		return(ConstantDescs.CD_float);
			case T_LONG_REAL:	return(ConstantDescs.CD_double);
			case T_TEXT:		return(RTS.CD.RTS_TXT);
			case T_REF:			return(this.getQual().getClassDesc());
			case T_PROCEDURE:	return(RTS.CD.RTS_PRCQNT);
			case T_LABEL:		return(RTS.CD.RTS_LABEL);
			default: Util.IERR(); return null;
		}
	}

	/// ClassFile coding utility: toClassDesc.
	/// @param kind a kind code
	/// @param mode a mode code
	/// @return the resulting Class Descriptor.
	public ClassDesc toClassDesc(int kind,int mode) {
		if (mode == Parameter.Mode.name) return(RTS.CD.RTS_NAME);
		else switch(kind) { // Parameter.Kind
			case Parameter.Kind.Array:			 return(RTS.CD.RTS_ARRAY);
			case Parameter.Kind.Label:           return(RTS.CD.RTS_LABEL);
			case Parameter.Kind.Procedure:       return(RTS.CD.RTS_PRCQNT);
			case Parameter.Kind.Simple: default: return(this.toClassDesc(declaredIn));
		}
	}

	/// ClassFile coding utility: toClassDesc.
	/// @return the resulting Class Descriptor.
	public ClassDesc toObjectClassDesc() {
		switch(keyWord) {
			case T_UNDEF:		return(ConstantDescs.CD_void);
			case T_BOOLEAN:		return(ConstantDescs.CD_Boolean);
			case T_CHARACTER:	return(ConstantDescs.CD_Character);
			case T_INTEGER:		return(ConstantDescs.CD_Integer);
			case T_REAL:		return(ConstantDescs.CD_Float);
			case T_LONG_REAL:	return(ConstantDescs.CD_Double);
			case T_TEXT:		return(RTS.CD.RTS_TXT);
			case T_REF:			return(this.getQual().getClassDesc());
			case T_PROCEDURE:	return(RTS.CD.RTS_PRCQNT);
			case T_LABEL:		return(RTS.CD.RTS_LABEL);
			default: Util.IERR(); return null;
		}
	}

	/// ClassFile coding utility: toNameClassSignature.
	/// @return the resulting Class Signature.
	public ClassSignature toNameClassSignature() {
		String CSS=null;
		switch(keyWord) {
			case T_BOOLEAN ->	CSS = "Lsimula/runtime/RTS_NAME<Ljava/lang/Boolean;>;";
			case T_CHARACTER ->	CSS = "Lsimula/runtime/RTS_NAME<Ljava/lang/Character;>;";
			case T_INTEGER ->	CSS = "Lsimula/runtime/RTS_NAME<Ljava/lang/Integer;>;";
			case T_REAL ->		CSS = "Lsimula/runtime/RTS_NAME<Ljava/lang/Float;>;";
			case T_LONG_REAL ->	CSS = "Lsimula/runtime/RTS_NAME<Ljava/lang/Double;>;";
			case T_TEXT ->		CSS = "Lsimula/runtime/RTS_NAME<Lsimula/runtime/TXT;>;";
			case T_PROCEDURE ->	CSS = "Lsimula/runtime/RTS_NAME<Lsimula/runtime/RTS_PRCQNT;>;";
			case T_LABEL ->		CSS = "Lsimula/runtime/RTS_NAME<Lsimula/runtime/RTS_LABEL;>;";
			case T_REF ->			CSS = "Lsimula/runtime/RTS_NAME<"+this.toJVMType()+">;";
			default -> { Util.IERR(); return null; }
		}
		return ClassSignature.parseFrom(CSS);
	}

	/// ClassFile coding utility: toArrayClassSignature.
	/// @return the resulting Array Class Signature.
	public ClassSignature toArrayClassSignature() {
		String CSS=null;
		switch(keyWord) {
			case T_BOOLEAN ->	CSS = "Lsimula.runtime.RTS_BOOLEAN_ARRAY;";
			case T_CHARACTER ->	CSS = "Lsimula.runtime.RTS_CHARACTER_ARRAY;";
			case T_INTEGER ->	CSS = "Lsimula.runtime.RTS_INTEGER_ARRAY;";
			case T_REAL ->		CSS = "Lsimula.runtime.RTS_REAL_ARRAY;";
			case T_LONG_REAL ->	CSS = "Lsimula.runtime.RTS_LONG_REAL_ARRAY;";
			case T_TEXT ->		CSS = "Lsimula.runtime.RTS_TEXT_ARRAY;";
			case T_REF ->		CSS = "Lsimula/runtime/RTS_REF_ARRAY<"+this.toJVMType()+">;";
			default -> Util.IERR();
		}
		return ClassSignature.parseFrom(CSS);
	}
	
	/// ClassFile coding utility: code instruction dup acording to type.
	/// @param codeBuilder the codeBuilder
	public void dup(CodeBuilder codeBuilder) {
		if(keyWord == Type.T_LONG_REAL)
			 codeBuilder.dup2();
		else codeBuilder.dup();
	}		
	
	/// ClassFile coding utility: code instruction dup_x1 acording to type.
	/// @param codeBuilder the codeBuilder
	public void dup_x1(CodeBuilder codeBuilder) {
		if(keyWord == Type.T_LONG_REAL)
			 codeBuilder.dup2_x1();
		else codeBuilder.dup_x1();
	}		
	
	/// ClassFile coding utility: code instruction pop acording to type.
	/// @param codeBuilder the codeBuilder
	public void pop(CodeBuilder codeBuilder) {
		if(keyWord == Type.T_LONG_REAL)
			 codeBuilder.pop2();
		else codeBuilder.pop();
	}		

	/// ClassFile coding utility: code instruction checkcast acording to type.
	/// @param codeBuilder the codeBuilder
	public void checkCast(CodeBuilder codeBuilder) {
		switch(keyWord) {
		case Type.T_INTEGER   -> codeBuilder.checkcast(ConstantDescs.CD_Integer);
		case Type.T_REAL      -> codeBuilder.checkcast(ConstantDescs.CD_Float);
		case Type.T_LONG_REAL -> codeBuilder.checkcast(ConstantDescs.CD_Double);
		case Type.T_BOOLEAN   -> codeBuilder.checkcast(ConstantDescs.CD_Boolean);
		case Type.T_CHARACTER -> codeBuilder.checkcast(ConstantDescs.CD_Character);
		case Type.T_TEXT      -> codeBuilder.checkcast(RTS.CD.RTS_TXT);
		case Type.T_LABEL     -> codeBuilder.checkcast(RTS.CD.RTS_LABEL);
		case Type.T_REF       -> codeBuilder.checkcast(this.toClassDesc());
		default -> Util.IERR();
	}

	}
	
	/// Convert JVM Object value to primitive type value.
	/// @param codeBuilder the CodeBuilder to use
	public void valueToPrimitiveType(CodeBuilder codeBuilder) {
		// Object TOS value ==> Primitive type
		switch(keyWord) {
			case Type.T_BOOLEAN   -> codeBuilder.invokevirtual(ConstantDescs.CD_Boolean, "booleanValue", MethodTypeDesc.ofDescriptor("()Z"));
			case Type.T_CHARACTER -> codeBuilder.invokevirtual(ConstantDescs.CD_Character, "charValue", MethodTypeDesc.ofDescriptor("()C"));
			case Type.T_INTEGER   -> codeBuilder.invokevirtual(ConstantDescs.CD_Integer, "intValue", MethodTypeDesc.ofDescriptor("()I"));
			case Type.T_REAL      -> codeBuilder.invokevirtual(ConstantDescs.CD_Float, "floatValue", MethodTypeDesc.ofDescriptor("()F"));
			case Type.T_LONG_REAL -> codeBuilder.invokevirtual(ConstantDescs.CD_Double, "doubleValue", MethodTypeDesc.ofDescriptor("()D"));
		}
	}
	
	/// ClassFile coding utility: buildObjectValueOf
	/// @param codeBuilder the CodeBuilder to use
	public void buildObjectValueOf(CodeBuilder codeBuilder) {
		switch(keyWord) {
		case T_BOOLEAN ->	codeBuilder.invokestatic(ConstantDescs.CD_Boolean,   "valueOf", MethodTypeDesc.ofDescriptor("(Z)Ljava/lang/Boolean;"));
		case T_CHARACTER -> codeBuilder.invokestatic(ConstantDescs.CD_Character, "valueOf", MethodTypeDesc.ofDescriptor("(C)Ljava/lang/Character;"));
		case T_INTEGER ->	codeBuilder.invokestatic(ConstantDescs.CD_Integer,   "valueOf", MethodTypeDesc.ofDescriptor("(I)Ljava/lang/Integer;"));
		case T_REAL ->	  	codeBuilder.invokestatic(ConstantDescs.CD_Float,     "valueOf", MethodTypeDesc.ofDescriptor("(F)Ljava/lang/Float;"));
		case T_LONG_REAL -> codeBuilder.invokestatic(ConstantDescs.CD_Double,    "valueOf", MethodTypeDesc.ofDescriptor("(D)Ljava/lang/Double;"));
		default -> {} // Nothing
		}
	}

	@Override
	public String toString() {
		switch(keyWord) {
			case T_INTEGER:		return "Integer";
			case T_REAL:		return "Real";
			case T_LONG_REAL:	return "Long Real";
			case T_BOOLEAN:		return "Boolean";
			case T_CHARACTER:	return "Character";
			case T_TEXT:		return "Text";
			case T_PROCEDURE:	return "Procedure";
			case T_LABEL:		return "Label";
			case T_REF:
				if(declaredIn==null) {
					return("ref("+classIdent+')');
				}
				return("ref("+classIdent+") declared in "+declaredIn.identifier);
		}
		return "UNKNOWN";
	}
	
}
