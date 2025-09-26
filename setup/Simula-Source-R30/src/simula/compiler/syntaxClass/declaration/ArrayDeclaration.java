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
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Enumeration;
import java.util.Vector;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.expression.Constant;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.syntaxClass.expression.TypeConversion;
import simula.compiler.syntaxClass.expression.VariableExpression;
import simula.compiler.utilities.DeclarationList;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Array Declaration.
/// 
/// <pre>
/// 
/// Simula Standard: 5.2 Array declaration
/// 
///   array-declaration = [ type ] ARRAY array-segment { , array-segment }
///   
///      array-segment = array-identifier { , array-identifier } "(" bound-pair-list ")"
///      
///         array-identifier = identifier
///         
///         bound-pair-list = bound-pair { , bound-pair }
///         
///            bound-pair = arithmetic-expression : arithmetic-expression 
/// </pre>
/// 
/// An array declaration declares one or several identifiers to represent
/// multi-dimensional arrays of subscripted variables and gives the dimensions of
/// the arrays, the bounds of the subscripts, and the type of the variables.
/// 
/// The subscript bounds for any array are given in the first subscript brackets
/// following the identifier of this array in the form of a bound pair list. Each
/// bound pair gives the lower bound of a subscript followed by : followed by the
/// upper bound. The bound pair list gives the bounds of all subscripts taken in
/// order from left to right.
/// 
/// NOTE: An initial "-" in upper bound may follow : directly (cf. 1.3).
/// The scanner will treat ":-" within parentheses (round brackets) as two
/// separate symbols ":" and "-" thus solving this ambiguity in the syntax. 
/// 
/// The dimension is given as the number of entries in the bound pair lists.
/// 
/// All arrays declared in one declaration are of the same quoted type. If no
/// type declarator is given the type real is understood.
/// 
/// The expressions are evaluated in the same way as subscript expressions. This
/// evaluation takes place once at each entrance into the block through the block
/// head. The expressions cannot include any identifier that is declared, either
/// explicitly or implicitly, in the same block head as the array in question.
/// 
/// An array has elements only when the values of all upper bounds are not
/// smaller than those of the corresponding lower bounds. If any lower bound
/// value is greater than the corresponding upper bound value, the array has no
/// elements. An attempt to access an element of an empty array leads to a
/// run-time error. The array may, however, be created at block entry and it may
/// be passed as a parameter.
/// 
/// The value of an array identifier is the ordered set of values of the
/// corresponding array of subscripted variables.
/// 
/// <pre>
/// Examples
/// 
///           integer array a(2:20)                      ! 19 elements;
///           real array  q(-7:if c < 0 then 2 else 1)   ! 10 or 9 elements;
///           array  a,b,c(7:n,2:m), s(-2:10)            ! any value of n or m legal;
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/ArrayDeclaration.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class ArrayDeclaration extends Declaration {
	// Type type; inherited

	/// Number of dimensions.
	public int nDim;
	
	/// The list of BoundPair.
	private Vector<BoundPair> boundPairList;

	/// Create a new ArrayDeclaration
	/// @param identifier the array identifier
	/// @param type the array type
	/// @param boundPairList The list of BoundPair
	private ArrayDeclaration(final String identifier, final Type type, final Vector<BoundPair> boundPairList) {
		super(identifier);
		this.declarationKind = ObjectKind.ArrayDeclaration;
		this.type = type;
		this.boundPairList = boundPairList;
		this.nDim = boundPairList.size();
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("END NEW ArrayDeclaration: " + toString());
	}

	/// Parse an array declaration and add it to the given declaration list.
	/// <pre>
	/// 
	/// Syntax:
	/// 
	/// ArrayDeclaration = [ Type ] ARRAY ArraySegment { , ArraySegment }
	///   ArraySegment = IdentifierList "(" BoundPairList ")"
	/// 
	/// 	IdentifierList = Identifier { , Identifier }
	/// 
	/// 	BoundPairList = BoundPair { , BoundPair }
	/// 	   BoundPair = ArithmeticExpression : ArithmeticExpression
	/// 
	/// </pre>
	/// Precondition:  [ Type ] ARRAY  is already read.
	/// 
	/// @param type            the array's type
	/// @param declarationList the given declaration list
	static void expectArrayDeclaration(final Type type, final DeclarationList declarationList) {
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("Parse ArrayDeclaration, type=" + type + ", current=" + Parse.currentToken);
		do {
			if (Option.internal.TRACE_PARSE)
				Parse.TRACE("Parse ArraySegment");
			// IdentifierList = Identifier { , Identifier }
			Vector<String> identList = new Vector<String>();
			do {
				identList.add(Parse.expectIdentifier());
			} while (Parse.accept(KeyWord.COMMA));
			Parse.expect(KeyWord.BEGPAR);
			// BoundPairList = BoundPair { , BoundPair }
			if (Option.internal.TRACE_PARSE)
				Parse.TRACE("Parse BoundPairList");
			Vector<BoundPair> boundPairList = new Vector<BoundPair>();
			do {
				Expression LB = Expression.expectExpression();
				Parse.expect(KeyWord.COLON);
				Expression UB = Expression.expectExpression();
				boundPairList.add(new BoundPair(LB, UB));
			} while (Parse.accept(KeyWord.COMMA));
			Parse.expect(KeyWord.ENDPAR);
			for (Enumeration<String> e = identList.elements(); e.hasMoreElements();) {
				String identifier = e.nextElement();
				declarationList.add(new ArrayDeclaration(identifier.toString(), type, boundPairList));
			}
			
		} while (Parse.accept(KeyWord.COMMA));
	}

	/// Utility Class to hold a BoundPair.
	/// <pre>
	/// Syntax:
	/// 
	///    BoundPair = ArithmeticExpression : ArithmeticExpression
	/// </pre>
	private static class BoundPair {
		/// The lower bound expression.
		Expression LB;
		/// The upper bound expression.
		Expression UB;

		/// Create a new BoundPair.
		/// @param LB The lower bound expression
		/// @param UB The upper bound expression
		BoundPair(final Expression LB, final Expression UB) {
			this.LB = LB;
			this.UB = UB;
		}

		/// Perform semantic checking.
		private void doChecking() {
			LB.doChecking();
			UB.doChecking();
			LB = (Expression) TypeConversion.testAndCreate(Type.Integer, LB);
			UB = (Expression) TypeConversion.testAndCreate(Type.Integer, UB);
		}

		@Override
		public String toString() {
			return ("" + LB + ':' + UB);
		}
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())
			return;
		Global.sourceLineNumber = lineNumber;
		if (type == null)
			type = Type.Real;
		type.doChecking(declaredIn);
		if (boundPairList != null)
			for (BoundPair it : boundPairList)
				it.doChecking();
		SET_SEMANTICS_CHECKED();
	}

	@Override
	public void doJavaCoding() {
		Global.sourceLineNumber = lineNumber;
		ASSERT_SEMANTICS_CHECKED();
		// --------------------------------------------------------------------
		// public RTS_REAL_ARRAY rTab=null;
		// --------------------------------------------------------------------
		String arrType = this.type.toJavaArrayType();
		String arrayIdent = this.getJavaIdentifier();
		JavaSourceFileCoder.code("public " + arrType + " " + arrayIdent + "=null;");
	}

	@Override
	public void doDeclarationCoding() {
		Global.sourceLineNumber = lineNumber;
		ASSERT_SEMANTICS_CHECKED();
		// --------------------------------------------------------------------
		// integer array A(1:4,4:6,6:12);
		// --------------------------------------------------------------------
		// A = new RTS_INTEGER_ARRAY(new _BOUNDS(1,4),new _BOUNDS(4,6),new _BOUNDS(6,7));
		// --------------------------------------------------------------------
		String arrayIdent = this.getJavaIdentifier();
		String arrType = this.type.toJavaArrayType();
		StringBuilder sb = new StringBuilder();
		sb.append(arrayIdent).append("=new " + arrType);
		char sep = '(';
		for (BoundPair boundPair : boundPairList) {
			sb.append(sep); sep = ',';
			sb.append("new RTS_BOUNDS(").append(boundPair.LB.toJavaCode()).append(',').append(boundPair.UB.toJavaCode()).append(')');
		}
		sb.append(");");
		JavaSourceFileCoder.code(sb.toString());
	}


	
	@Override
	public void buildDeclaration(ClassBuilder classBuilder, BlockDeclaration encloser) {
		Global.sourceLineNumber = lineNumber;
		ASSERT_SEMANTICS_CHECKED();
		classBuilder.withField(identifier, RTS.CD.RTS_ARRAY(type), fieldBuilder -> {
			fieldBuilder
				.withFlags(ClassFile.ACC_PUBLIC)
				.with(SignatureAttribute.of(type.toArrayClassSignature()));
		});

	}

	@Override
	public void buildInitAttribute(CodeBuilder codeBuilder) {
		String arrayIdent = this.getJavaIdentifier();
		ClassDesc CD_ArrayType=RTS.CD.RTS_ARRAY(type);
		codeBuilder
			.aload(0)
			.aconst_null()
			.putfield(codeBuilder.constantPool().fieldRefEntry(BlockDeclaration.currentClassDesc(),arrayIdent, CD_ArrayType));
	}

	@Override
	public String getFieldIdentifier() {
		return(this.getJavaIdentifier());
	}

	/// Get the ClassDescr.
	/// @param type array's type.
	/// @return the ClassDescr.
	public static ClassDesc getClassDesc(Type type) {
		return(RTS.CD.RTS_ARRAY(type));
	}

	@Override
	public void buildDeclarationCode(CodeBuilder codeBuilder) {
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		// --------------------------------------------------------------------
		// integer array A(1:4,4:6,6:12);
		// --------------------------------------------------------------------
		// A = new RTS_INTEGER_ARRAY(new _BOUNDS(1,4),new _BOUNDS(4,6),new _BOUNDS(6,7));
		// --------------------------------------------------------------------

		// 20: aload_0

		// 21: new           #23                 // class simula/runtime/RTS_INTEGER_ARRAY
		// 24: dup
		// 25: aload_0
		// 26: iconst_3
		// 27: anewarray     #25                 // class simula/runtime/RTS_BOUNDS

		// 30: dup
		// 31: iconst_0
		// 32: new           #25                 // class simula/runtime/RTS_BOUNDS
		// 35: dup
		// 36: aload_0
		// 37: iconst_1
		// 38: iconst_4
		// 39: invokespecial #27                 // Method simula/runtime/RTS_BOUNDS."<init>":(Lsimula/runtime/RTS_RTObject;II)V
		// 42: aastore

		// 43: dup
		// 44: iconst_1
		// 45: new           #25                 // class simula/runtime/RTS_BOUNDS
		// 48: dup
		// 49: aload_0
		// 50: iconst_4
		// 51: bipush        6
		// 53: invokespecial #27                 // Method simula/runtime/RTS_BOUNDS."<init>":(Lsimula/runtime/RTS_RTObject;II)V
		// 56: aastore

		// 57: dup
		// 58: iconst_2
		// 59: new           #25                 // class simula/runtime/RTS_BOUNDS
		// 62: dup
		// 63: aload_0
		// 64: bipush        6
		// 66: bipush        12
		// 68: invokespecial #27                 // Method simula/runtime/RTS_BOUNDS."<init>":(Lsimula/runtime/RTS_RTObject;II)V
		// 71: aastore

		// 72: invokespecial #30                 // Method simula/runtime/RTS_INTEGER_ARRAY."<init>":(Lsimula/runtime/RTS_RTObject;[Lsimula/runtime/RTS_BOUNDS;)V
		// 75: putfield      #7                  // Field A:Lsimula/runtime/RTS_INTEGER_ARRAY;

		ClassDesc CD_ArrayType=RTS.CD.RTS_ARRAY(type);

		codeBuilder
				.aload(0)
				.new_(CD_ArrayType)
				.dup();
		Constant.buildIntConst(codeBuilder, boundPairList.size());
		codeBuilder.anewarray(RTS.CD.RTS_BOUNDS);

		for(int i=0;i<boundPairList.size();i++) {
			BoundPair bp = boundPairList.get(i);
			codeBuilder.dup();
			Constant.buildIntConst(codeBuilder, i);
			codeBuilder
				.new_(RTS.CD.RTS_BOUNDS)
				.dup();
			bp.LB.buildEvaluation(null,codeBuilder);
			bp.UB.buildEvaluation(null,codeBuilder);
			codeBuilder
				.invokespecial(RTS.CD.RTS_BOUNDS, "<init>", MethodTypeDesc.ofDescriptor("(II)V"))
				.aastore();
		}
		codeBuilder
			.invokespecial(CD_ArrayType, "<init>", MethodTypeDesc.ofDescriptor("([Lsimula/runtime/RTS_BOUNDS;)V"))
			.putfield(pool.fieldRefEntry(BlockDeclaration.currentClassDesc(),identifier, CD_ArrayType));
	}

	// ********************************************************************************************
	// **************************************** buildGetArrayField ****************************************
	// ********************************************************************************************
	/// ClassFile coding utility: Build 'Get Array Field'
	/// @param type the array type.
	/// @param meaning the meaning.
	/// @param declaredIn the owner.
	/// @param arrayIdent the array identifier.
	/// @param isParameter true: array is a parameter.
	/// @param codeBuilder the codeBuilder to use.
	private static void buildGetArrayField(Type type,Meaning meaning,DeclarationScope declaredIn,String arrayIdent,boolean isParameter,CodeBuilder codeBuilder) {
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		ClassDesc CD_Type=RTS.CD.RTS_ARRAY(type);
		if(isParameter) {
			FieldRefEntry FRE_Arr1=pool.fieldRefEntry(declaredIn.getClassDesc(), arrayIdent, RTS.CD.RTS_ARRAY);
			codeBuilder
				.getfield(FRE_Arr1)
				.checkcast(CD_Type);					
		} else {
			ClassDesc owner = declaredIn.getClassDesc();
			if(meaning != null && meaning.foundBehindInvisible) {
				owner = meaning.foundIn.getClassDesc();
			}
			FieldRefEntry FRE_Arr=pool.fieldRefEntry(owner, arrayIdent, CD_Type);
			codeBuilder.getfield(FRE_Arr);	
		}
	}
	
	/// ClassFile coding utility: Prepare for indexing.
	/// @param checkedParams the checked parameters
	/// @param codeBuilder the codeBuilder to use.
	private static void prepIndexing(Vector<Expression> checkedParams, CodeBuilder codeBuilder) {
		Constant.buildIntConst(codeBuilder, checkedParams.size());
		codeBuilder.newarray(TypeKind.INT);
		for(int i=0;i<checkedParams.size();i++) {
			codeBuilder.dup();
			Constant.buildIntConst(codeBuilder, i);
			checkedParams.get(i).buildEvaluation(null,codeBuilder);
			codeBuilder.iastore();
		}
	}


	// ********************************************************************************************
	// **************************************** putELEMENT ****************************************
	// ********************************************************************************************
	/// ClassFile coding utility: Build invoke ARRAY_putELEMENT.
	/// <pre>
	/// Example: A.putELEMENT(A.index(2,5,9),666);
	/// 
	///  aload_0
	///  getfield      #7   // Field A:Lsimula/runtime/RTS_INTEGER_ARRAY;
	/// 
	///  dup
	///
	///  *** prepIndexing ***
	///  iconst_3			// boundPairList.size()
	///  newarray       int
	///  dup
	///  iconst_0
	///  iconst_2
	///  iastore
	///  dup
	///  iconst_1
	///  iconst_5
	///  iastore
	///  dup
	///  iconst_2
	///  bipush        9
	///  iastore
	/// 
	///  invokevirtual #33  // Method simula/runtime/RTS_INTEGER_ARRAY.index:([I)I
	/// 
	///  sipush        666
	/// 
	///  invokevirtual #37  // Method simula/runtime/RTS_INTEGER_ARRAY.putELEMENT:(II)I
	/// @param var the variable
	/// @param isParameter true: variable is a parameter
	/// @param rhs expression.
	/// @param codeBuilder the codeBuilder to use.
	public void arrayPutElement(VariableExpression var, boolean isParameter, Expression rhs, CodeBuilder codeBuilder) {
		String arrayIdent = this.getJavaIdentifier();
		arrayPutElement(var.meaning,arrayIdent,isParameter,var.checkedParams,rhs,codeBuilder);
	}
	
	/// ClassFile coding utility: Build invoke ARRAY_putELEMENT.
	/// @param meaning variable's meaning.
	/// @param arrayIdent array's identifier
	/// @param isParameter true: variable is a parameter
	/// @param checkedParams checked parameters.
	/// @param rhs expression.
	/// @param codeBuilder the codeBuilder to use.
	public static void arrayPutElement(Meaning meaning,String arrayIdent,boolean isParameter,Vector<Expression> checkedParams, Expression rhs, CodeBuilder codeBuilder) {
		Type type=meaning.declaredAs.type;
		buildGetArrayField(type,meaning,meaning.declaredIn,arrayIdent,isParameter,codeBuilder);
		codeBuilder.dup();
		arrayPutElement2(meaning,checkedParams,rhs,codeBuilder);
	}

	/// ClassFile coding utility: Build invoke ARRAY_putELEMENT.
	/// @param meaning variable's meaning.
	/// @param checkedParams checked parameters.
	/// @param rhs expression.
	/// @param codeBuilder the codeBuilder to use.
	public static void arrayPutElement2(Meaning meaning,Vector<Expression> checkedParams, Expression rhs, CodeBuilder codeBuilder) {
		
		prepIndexing(checkedParams,codeBuilder);
		
		Type type=meaning.declaredAs.type;
		RTS.invokevirtual_ARRAY_index(type, codeBuilder);
		
		rhs.buildEvaluation(null,codeBuilder);
		RTS.invokevirtual_ARRAY_putELEMENT(type, codeBuilder);
	}
	
	
	// ********************************************************************************************
	// **************************************** getELEMENT ****************************************
	// ********************************************************************************************
	/// ClassFile coding utility: Build invoke ARRAY_getELEMENT.
	/// @param var the variable
	/// @param isParameter true: variable is a parameter
	/// @param codeBuilder the codeBuilder to use.
	public void arrayGetElement(VariableExpression var, boolean isParameter, CodeBuilder codeBuilder) {
		String arrayIdent = this.getJavaIdentifier();
		arrayGetElement(type,arrayIdent,isParameter,var.checkedParams,var.meaning,var.meaning.declaredIn,codeBuilder);
	}

	/// ClassFile coding utility: Build invoke ARRAY_getELEMENT.
	/// @param type array's type
	/// @param arrayIdent array's identifier
	/// @param isParameter true: variable is a parameter
	/// @param checkedParams checked parameters.
	/// @param meaning variable's meaning.
	/// @param declaredIn array's owner
	/// @param codeBuilder the codeBuilder to use.
	public static void arrayGetElement(Type type,String arrayIdent,boolean isParameter,Vector<Expression> checkedParams,
			Meaning meaning,DeclarationScope declaredIn, CodeBuilder codeBuilder) {
		buildGetArrayField(type,meaning,declaredIn,arrayIdent,isParameter,codeBuilder);
		arrayGetElement2(type,arrayIdent,checkedParams,codeBuilder);
	}
	
	/// ClassFile coding utility: Build invoke ARRAY_getELEMENT.
	/// @param type array's type
	/// @param arrayIdent array's identifier
	/// @param checkedParams checked parameters.
	/// @param codeBuilder the codeBuilder to use.
	public static void arrayGetElement2(Type type,String arrayIdent,Vector<Expression> checkedParams, CodeBuilder codeBuilder) {
		prepIndexing(checkedParams,codeBuilder);
		RTS.invokevirtual_ARRAY_getELEMENT(type, codeBuilder);
	}



	@Override
	public void printTree(final int indent, final Object head) {
		IO.println(SyntaxClass.edIndent(indent)+this.getClass().getSimpleName()+"    "+this);
	}

	@Override
	public String toString() {
		String s = "ARRAY " + identifier;
		if (type != null)
			s = type.toString() + " " + s;
		return (s);
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	public ArrayDeclaration() {
		super(null);
		this.declarationKind = ObjectKind.ArrayDeclaration;
	}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("Array: " + type + ' ' + identifier + ", nDim=" + nDim);
		oupt.writeKind(declarationKind);
		oupt.writeShort(OBJECT_SEQU);

		// *** SyntaxClass
		oupt.writeShort(lineNumber);

		// *** Declaration
		oupt.writeString(identifier);
		oupt.writeString(externalIdent);
		oupt.writeType(type);// Declaration

		// *** ArrayDeclaration
		oupt.writeShort(nDim);
		for(BoundPair boundPair:boundPairList) {
			oupt.writeObj(boundPair.LB);
			oupt.writeObj(boundPair.UB);
		}
	}
	
	/// Read and return an ArrayDeclaration object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the object read from the stream.
	/// @throws IOException if something went wrong.
	public static ArrayDeclaration readObject(AttributeInputStream inpt) throws IOException {
		ArrayDeclaration arr = new ArrayDeclaration();
		arr.OBJECT_SEQU = inpt.readSEQU(arr);

		// *** SyntaxClass
		arr.lineNumber = inpt.readShort();

		// *** Declaration
		arr.identifier = inpt.readString();
		arr.externalIdent = inpt.readString();
		arr.type = inpt.readType();

		// *** ArrayDeclaration
		arr.nDim = inpt.readShort();
		arr.boundPairList = new Vector<BoundPair>();
		for(int i=0;i<arr.nDim;i++) {
			Expression LB = (Expression) inpt.readObj();
			Expression UB = (Expression) inpt.readObj();
			arr.boundPairList.add(new BoundPair(LB,UB));
		}
		Util.TRACE_INPUT("Array: " + arr);
		return(arr);
	}
	
}
