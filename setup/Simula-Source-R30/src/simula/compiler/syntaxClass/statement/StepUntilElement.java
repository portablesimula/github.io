/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.statement;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.constant.MethodTypeDesc;
import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.BlockDeclaration;
import simula.compiler.syntaxClass.declaration.Parameter;
import simula.compiler.syntaxClass.declaration.SimpleVariableDeclaration;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.syntaxClass.expression.TypeConversion;
import simula.compiler.syntaxClass.expression.VariableExpression;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

// ************************************************************************************
// *** ForListElement -- Step Until Element
// ************************************************************************************
/// Utility class: For-list Step until element.
///
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/StepUntilElement.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class StepUntilElement extends ForListElement {
	/// The second expression.
	Expression expr2;
	
	/// The third expression.
	Expression expr3;

	/// Create a new StepUntilElement.
	/// @param forStatement the ForStatement
	/// @param expr1 The first expression
	/// @param expr2 The second expression
	/// @param expr3 The third expression
	public StepUntilElement(final ForStatement forStatement, final Expression expr1, final Expression expr2, final Expression expr3) {
		super(forStatement, expr1);
		this.expr2 = expr2;
		this.expr3 = expr3;
		if (expr1 == null)
			Util.error("Missing expression before 'step' in ForStatement");
		if (expr2 == null)
			Util.error("Missing expression after 'step' in ForStatement");
		if (expr3 == null)
			Util.error("Missing expression after 'until' in ForStatement");
	}

	@Override
	public void doChecking() {
		expr1.doChecking();
		expr1 = TypeConversion.testAndCreate(forStatement.controlVariable.type, expr1);
		expr2.doChecking();
		expr2 = TypeConversion.testAndCreate(forStatement.controlVariable.type, expr2);
		expr3.doChecking();
		expr3 = TypeConversion.testAndCreate(forStatement.controlVariable.type, expr3);
		expr1.backLink = forStatement; // To ensure _RESULT from functions
		expr2.backLink = forStatement; // To ensure _RESULT from functions
		expr3.backLink = forStatement; // To ensure _RESULT from functions
	}

	@Override
	public String edCode(final String classIdent, Type xType) {
		return ("new FOR_StepUntil(" + forStatement.edControlVariableByName(classIdent, xType)
				+ ",new RTS_NAME<Number>() { public Number get(){return(" + expr1.toJavaCode() + "); }}"
				+ ",new RTS_NAME<Number>() { public Number get(){return(" + expr2.toJavaCode() + "); }}"
				+ ",new RTS_NAME<Number>() { public Number get(){return(" + expr3.toJavaCode() + "); }})");

	}

	@Override
	public ForListElement isOptimisable() {
		return this; // All for-step-until elements are optimisable.
	}

	@Override
	public void doSimplifiedJavaCoding() {
		Number num2 = expr2.getNumber();
		if(num2 == null) {
			generalCase();
			return;
		}
		int step = num2.intValue();
		String cv = forStatement.controlVariable.toJavaCode();
		String stepClause, incrClause;
		if (step >= 0) {
			// ------------------------------------------------------------
			// for(cv=expr1; cv<=expr3; cv=cv+step) { Statements ... }
			// ------------------------------------------------------------
			stepClause = cv + "<=" + this.expr3.toJavaCode() + ";";
			if (step == 1)
				incrClause = cv + "++";
			else
				incrClause = cv + "=" + cv + "+" + step;
		} else {
			// ------------------------------------------------------------
			// for(cv=expr1; cv>=expr3; cv=cv+step) { Statements ... }
			// ------------------------------------------------------------
			stepClause = cv + ">=" + this.expr3.toJavaCode() + ";";
			if (step == -1)
				incrClause = cv + "--";
			else
				incrClause = cv + "=" + cv + step;
		}
		JavaSourceFileCoder
				.code("for(" + cv + "=" + this.expr1.toJavaCode() + ";" + stepClause + incrClause + ") {");
		forStatement.doStatement.doJavaCoding();
		JavaSourceFileCoder.code("}");
	}
	
	/// Used to make deltaID unique.
	private static int DELTA_SEQU=0;
	/// ClassFile coding utility: generalCase.
	/// <pre>
	/// FOR controlVariable := expr1 STEP expr2 UNTIL expr3 DO statement;
	/// 
	/// According to Simula Standard:
	/// 
	/// 		int/float/double DELTA; // Local
	/// 
	/// 		controlVariable = expr1();
	/// 		DELTA = expr2();
	/// 		while( sign(DELTA)*(controlVariable-expr3()) <= 0) {
	/// 			STATEMENT();
	/// 			DELTA = expr2();
	/// 			controlVariable = controlVariable + DELTA;
	/// 		}  // end while;
	/// </pre>
	private void generalCase() {
		String cv = forStatement.controlVariable.toJavaCode();
		String deltaType=expr2.type.toJavaType();
		String deltaID = "DELTA_" + (DELTA_SEQU++);
		JavaSourceFileCoder.debug("// ForStatement:");
		
		JavaSourceFileCoder.code(deltaType + " " + deltaID + ';');
		JavaSourceFileCoder.code(cv + " = " + this.expr1.toJavaCode() + ";");
		JavaSourceFileCoder.code(deltaID + " = " + this.expr2.toJavaCode() + ";");
		
		String deltaSign = null;
		switch(expr2.type.keyWord) {
			case Type.T_INTEGER:   deltaSign = "RTS_UTIL.isign("+deltaID+")"; break;
			case Type.T_REAL:      deltaSign = "RTS_UTIL.fsign("+deltaID+")"; break;
			case Type.T_LONG_REAL: deltaSign = "RTS_UTIL.dsign("+deltaID+")"; break;
			default: Util.IERR();
		}
		JavaSourceFileCoder.code("while( "+ deltaSign + " * ( " + cv + " - (" + this.expr3.toJavaCode() + ") ) <= 0 ) {");
		
		forStatement.doStatement.doJavaCoding();
		
		JavaSourceFileCoder.code(deltaID + " = " + this.expr2.toJavaCode() + ";");
		JavaSourceFileCoder.code(cv + " = " + cv + " + " + deltaID + ';');
		JavaSourceFileCoder.code("}","end while");
	}

	
	/// ClassFile coding utility: doSingleElementByteCoding.
	/// <pre>
	/// FOR controlVariable := expr1 STEP expr2 UNTIL expr3 DO statement;
	/// 
	/// 
	/// int/float/double DELTA; // Local
	/// 
	/// controlVariable = expr1();
	/// DELTA = expr2();
	/// while( sign(DELTA)/// (controlVariable - expr3() ) <= 0) {
	/// 		STATEMENT();
	/// 		DELTA = expr2();
	/// 		controlVariable = controlVariable + DELTA;
	/// }  // end while;
	/// 
	/// 
	///   // controlVariable = expr1();
    ///   aload_0
    ///   aload_0
    ///   invokevirtual #24                 // Method expr1:()I
    ///   putfield      #12                 // Field controlVariable:I
    /// 
    /// // DELTA = expr2();
    ///   aload_0
    ///   invokevirtual #26                 // Method expr2:()I
    ///   istore_1	   // Local DELTA
	/// 
    /// TST:
    /// // if(DELTA*(controlVariable-expr3()) > 0) goto END
    ///   iload_1       // Local DELTA
    ///   aload_0
    ///   getfield      #12                 // Field controlVariable:I
    ///   aload_0
    ///   invokevirtual #30                 // Method expr3:()I
    ///   isub
    ///   imul
    ///   ifle          END
	/// 
    /// // STATEMENT
    ///   aload_0
    ///   invokevirtual #28                 // Method STATEMENT:()V
    ///   
    /// // DELTA = expr2();
    ///   aload_0
    ///   invokevirtual #26                 // Method expr2:()I
    ///   istore_1      // Local DELTA
    ///   
    /// // controlVariable = controlVariable + DELTA;  
    ///   aload_0
    ///   dup
    ///   getfield      #12                 // Field controlVariable:I
    ///   iload_1       // Local DELTA
    ///   iadd
    ///   putfield      #12                 // Field controlVariable:I
    /// 
    ///   goto          TST
    ///   
    /// END:  
    /// </pre>
	@Override
	public void doSingleElementByteCoding(CodeBuilder codeBuilder) {
		Label tstLabel = codeBuilder.newLabel();
		Label endLabel = codeBuilder.newLabel();
		
		FieldRefEntry CTRL = null;
		if(forStatement.controlVariable.meaning.declaredAs instanceof SimpleVariableDeclaration var) {
			CTRL=var.getFieldRefEntry(codeBuilder.constantPool());
		} else if(forStatement.controlVariable.meaning.declaredAs instanceof Parameter par) {
			CTRL=par.getFieldRefEntry(codeBuilder.constantPool());
		} else Util.IERR();
		
		int DELTA = BlockDeclaration.currentBlock.allocateLocalVariable(expr2.type); // Local Slot 1, 2 ...

    	//      // controlVariable = expr1();
        //      aload_0
        //      aload_0
        //      invokevirtual #24                 // Method expr1:()I
        //      putfield      #12                 // Field controlVariable:I
		forStatement.controlVariable.buildIdentifierAccess(true, codeBuilder);
		this.expr1.buildEvaluation(null,codeBuilder);
		TypeConversion.buildMayBeConvert(expr1.type, forStatement.controlVariable.type, codeBuilder);
		codeBuilder.putfield(CTRL);

		// LOOP:
	    //      // DELTA = expr2();
        //      aload_0
        //      invokevirtual #26                 // Method expr2:()I
        //      istore_1
		this.expr2.buildEvaluation(null,codeBuilder); // init
		switch(expr2.type.keyWord) {
			case Type.T_INTEGER ->   codeBuilder.istore(DELTA);
			case Type.T_REAL ->      codeBuilder.fstore(DELTA);
			case Type.T_LONG_REAL -> codeBuilder.dstore(DELTA);
			default -> Util.IERR();
		}

		// TST:
		//      // if(DELTA*(controlVariable-expr3()) > 0) goto END
		//      iload_1                           // Local DELTA
		//      aload_0
		//      getfield      #12                 // Field controlVariable:I
		//      aload_0
		//      invokevirtual #30                 // Method expr3:()I
		//      isub
		//      imul
		//      ifle          16  // STM
		codeBuilder.labelBinding(tstLabel);
		RTS.invokestatic_UTIL_sign(forStatement.controlVariable.type, DELTA, codeBuilder);
		forStatement.controlVariable.buildIdentifierAccess(true, codeBuilder);
		codeBuilder.getfield(CTRL);

		this.expr3.buildEvaluation(null,codeBuilder);
		TypeConversion.buildMayBeConvert(forStatement.controlVariable.type,this.expr3.type, codeBuilder);
		
		switch(forStatement.controlVariable.type.keyWord) {
			case Type.T_INTEGER ->   codeBuilder.isub().imul();
			case Type.T_REAL ->      codeBuilder.fsub().fmul().fconst_0().fcmpg();
			case Type.T_LONG_REAL -> codeBuilder.dsub().dmul().dconst_0().dcmpg();
			default -> Util.IERR();
		}
		
		codeBuilder.ifgt(endLabel);
		
		
        // STM: STATEMENT
        //      aload_0
        //      invokevirtual #28                 // Method STATEMENT:()V
		forStatement.doStatement.buildByteCode(codeBuilder);

		// DELTA = expr2();
		//      aload_0
		//      invokevirtual #26                 // Method expr2:()I
		//      istore_1
		this.expr2.buildEvaluation(null,codeBuilder);
		switch(expr2.type.keyWord) {
			case Type.T_INTEGER ->   codeBuilder.istore(DELTA);
			case Type.T_REAL ->      codeBuilder.fstore(DELTA);
			case Type.T_LONG_REAL -> codeBuilder.dstore(DELTA);
			default -> Util.IERR();
		}
		
        // controlVariable = controlVariable + DELTA;  
        // 25: aload_0
        // 26: dup
        // 27: getfield      #12                 // Field controlVariable:I
        // 30: iload_1
        // 31: iadd
        // 32: putfield      #12                 // Field controlVariable:I
		forStatement.controlVariable.buildIdentifierAccess(true, codeBuilder);
		codeBuilder
			.dup()
			.getfield(CTRL);
		switch(expr2.type.keyWord) {
			case Type.T_INTEGER ->   codeBuilder.iload(DELTA).iadd();
			case Type.T_REAL ->      codeBuilder.fload(DELTA).fadd();
			case Type.T_LONG_REAL -> codeBuilder.dload(DELTA).dadd();
			default -> Util.IERR();
		}
		
		// MAY BE CONVERT   TOS:DELTA to controlVariable.type
		TypeConversion.buildMayBeConvert(expr2.type, forStatement.controlVariable.type, codeBuilder);
		
		codeBuilder.putfield(CTRL);

        // 13: goto          35  // TST
		codeBuilder.goto_(tstLabel);
		
		codeBuilder.labelBinding(endLabel);
	}


	@Override
	public void buildByteCode(CodeBuilder codeBuilder,VariableExpression controlVariable) {
		codeBuilder
			.new_(RTS.CD.FOR_StepUntil)
			.dup();
		Parameter.buildNameParam(codeBuilder,controlVariable);
		Parameter.buildNameParam(codeBuilder,expr1); // PARAMETER: RTS_NAME<T> init
		Parameter.buildNameParam(codeBuilder,expr2); // PARAMETER: RTS_NAME<T> step
		Parameter.buildNameParam(codeBuilder,expr3); // PARAMETER: RTS_NAME<T> until

		MethodTypeDesc MTD=MethodTypeDesc.ofDescriptor(
				"(Lsimula/runtime/RTS_NAME;Lsimula/runtime/RTS_NAME;Lsimula/runtime/RTS_NAME;Lsimula/runtime/RTS_NAME;)V");
		codeBuilder.invokespecial(RTS.CD.FOR_StepUntil, "<init>", MTD); // Invoke Constructor
	}

	@Override
	public String toString() {
		return ("" + expr1 + " step " + expr2 + " until " + expr3);
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private StepUntilElement() {}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("StepUntilElement: " + this);
		oupt.writeKind(ObjectKind.StepUntilElement);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** ForListElement
		oupt.writeObj(forStatement);
		oupt.writeObj(expr1);
		oupt.writeObj(expr2);
		oupt.writeObj(expr3);
	}
	
	/// Read and return an object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the object read from the stream.
	/// @throws IOException if something went wrong.
	public static StepUntilElement readObject(AttributeInputStream inpt) throws IOException {
		StepUntilElement elt = new StepUntilElement();
		elt.OBJECT_SEQU = inpt.readSEQU(elt);
		// *** SyntaxClass
		elt.lineNumber = inpt.readShort();
		// *** ForListElement
		elt.forStatement = (ForStatement) inpt.readObj();
		elt.expr1 = (Expression) inpt.readObj();
		elt.expr2 = (Expression) inpt.readObj();
		elt.expr3 = (Expression) inpt.readObj();
		Util.TRACE_INPUT("StepUntilElement: " + elt);
		return(elt);
	}
	
}
