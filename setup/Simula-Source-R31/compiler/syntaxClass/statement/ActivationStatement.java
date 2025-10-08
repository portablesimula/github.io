/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.statement;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.expression.Constant;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.syntaxClass.expression.TypeConversion;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Token;
import simula.compiler.utilities.Util;

/// Activation Statement.
/// 
/// <pre>
/// 
/// Simula Standard: 12.2 Activation statement
/// 
///   activation-statement = activator  object-expression [ scheduling-part ]
/// 
///      activator = ACTIVATE | REACTIVATE
/// 
///      scheduling-part = AT arithmetic-expression [ PRIOR ]
///                      | DELAY arithmetic-expression [ PRIOR ]
///                      | BEFORE object-expression
///                      | AFTER object-expression
///                      
///                      
///                      
/// The activation statement is defined by the procedure ACTIVAT in Simula Standard.
/// In this implementation we use a set of methods for the same purpose:
/// 
///   <b>activate</b> x;                      ==> ActivateDirect(false,x);
///   <b>activate</b> x <b>delay</b> 1.34;           ==> ActivateDelay(false,x,1.34f,false);
///   <b>activate</b> x <b>delay</b> 1.34 <b>prior</b>;     ==> ActivateDelay(false,x,1.34f,true);
///   <b>activate</b> x <b>at</b> 13.7;              ==> ActivateAt(false,x,13.7f,false);
///   <b>activate</b> x <b>at</b> 13.7 <b>prior</b>;        ==> ActivateAt(false,x,13.7f,true);
///   <b>activate</b> x <b>before</b> y;             ==> ActivateBefore(false,x,y);
///   <b>activate</b> x <b>after</b> y;              ==> ActivateAfter(false,x,y);
///   
///   <b>reactivate</b> x;                    ==> ActivateDirect(true,x);
///   <b>reactivate</b> x <b>delay</b> 1.34;         ==> ActivateDelay(true,x,1.34f,false);
///   <b>reactivate</b> x <b>delay</b> 1.34 <b>prior</b>;   ==> ActivateDelay(true,x,1.34f,true);
///   <b>reactivate</b> x <b>at</b> 13.7;            ==> ActivateAt(true,x,13.7f,false);
///   <b>reactivate</b> x <b>at</b> 13.7 <b>prior</b>;      ==> ActivateAt(true,x,13.7f,true);
///   <b>reactivate</b> x <b>before</b> y;           ==> ActivateBefore(true,x,y);
///   <b>reactivate</b> x <b>after</b> y;            ==> ActivateAfter(true,x,y);
///   
/// See runtime module RTS_Simulation for details.  
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/ActivationStatement.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class ActivationStatement extends Statement {
	
	/// Indicates reactivation when true, otherwise activation.
	private boolean REAC;
	
	/// First object-expression in activation statement.
	private Expression object1;
	
	/// Second object-expression in activation statement.
	private Expression object2;
	
	/// The AT time expression.
	private Expression time = null;
	
	/// Indicates that PRIOR is present in the activation statement.
	private Boolean prior = false;
	
	/// The activation code
	private ActivationCode code;
	
	/// The activation code
	private enum ActivationCode {
		/** Direct activation */				direct,
		/** (Re)Activate Process AT ... */		at,
		/** (Re)Activate Process DELAY ... */	delay,
		/** (Re)Activate Process BEFORE ... */	before,
		/** (Re)Activate Process AFTER ... */	after
	}

	/// Create a new ActivationStatement.
	/// @param line the source line number
	ActivationStatement(final int line) {
		super(line);
		Token activator = Parse.prevToken;
		REAC = activator.getKeyWord() == KeyWord.REACTIVATE;
		if (Option.internal.TRACE_PARSE) Parse.TRACE("Parse ActivationStatement");
		object1 = Expression.expectExpression();
		code = ActivationCode.direct;
		if (Parse.accept(KeyWord.AT) || Parse.accept(KeyWord.DELAY)) {
			code = (Parse.prevToken.getKeyWord() == KeyWord.AT) ? ActivationCode.at : ActivationCode.delay;
			time = Expression.expectExpression();
			if (Parse.accept(KeyWord.PRIOR)) prior = true;
		} else if (Parse.accept(KeyWord.BEFORE) || Parse.accept(KeyWord.AFTER)) {
			code = (Parse.prevToken.getKeyWord() == KeyWord.BEFORE) ? ActivationCode.before : ActivationCode.after;
			object2 = Expression.expectExpression();
		}
		if (Option.internal.TRACE_PARSE) Util.TRACE("Line "+lineNumber+": ActivationStatement: "+this);
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		if (object1 != null) object1.doChecking();
		if (time != null) time.doChecking();
		if (object2 != null) object2.doChecking();
		SET_SEMANTICS_CHECKED();
	}

	@Override
	public String toJavaCode() {
		Type refProcess = Type.Ref("Process");
		if (object1 != null)
			object1 = TypeConversion.testAndCreate(refProcess, object1);
		if (object2 != null)
			object2 = TypeConversion.testAndCreate(refProcess, object2);
		switch (code) {
		    case at:     return (edActivateAt());
		    case delay:	 return (edActivateDelay());
		    case before: return (edActivateBefore());
		    case after:  return (edActivateAfter());
		    case direct:
			default: return (edActivateDirect());
		}
	}

	/// Java coding utility: Edit direct (re)activation
	/// @return the resulting Java source code
	private String edActivateDirect() {
		String obj1 = (object1 == null) ? "null" : "(RTS_Process)"+object1.toJavaCode();
		Meaning activate1 = Global.getCurrentScope().findMeaning("ActivateDirect");
		String staticLink = activate1.edQualifiedStaticLink();
		return (staticLink + ".ActivateDirect(" + REAC + ',' + obj1 + ')');
	}

	/// Java coding utility: Edit (Re)Activate Process AT ...
	/// @return the resulting Java source code
	private String edActivateAt() {
		String obj1 = (object1 == null) ? "null" : "(RTS_Process)"+object1.toJavaCode();
		String staticLink = Global.getCurrentScope().findMeaning("ActivateAt").edQualifiedStaticLink();
		return (staticLink + ".ActivateAt(" + REAC + ',' + obj1 + ',' + time.toJavaCode() + ',' + prior + ')');
	}

	/// Java coding utility: Edit (Re)Activate Process DELAY ...
	/// @return the resulting Java source code
	private String edActivateDelay() {
		String obj1 = (object1 == null) ? "null" : "(RTS_Process)"+object1.toJavaCode();
		String staticLink = Global.getCurrentScope().findMeaning("ActivateDelay").edQualifiedStaticLink();
		return (staticLink + ".ActivateDelay(" + REAC + ',' + obj1 + ',' + time.toJavaCode() + ',' + prior + ')');
	}

	/// Java coding utility: Edit (Re)Activate Process BEFORE ...
	/// @return the resulting Java source code
	private String edActivateBefore() {
		String obj1 = (object1 == null) ? "null" : "(RTS_Process)"+object1.toJavaCode();
		String obj2 = (object2 == null) ? "null" : "(RTS_Process)"+object2.toJavaCode();
		Meaning activate3 = Global.getCurrentScope().findMeaning("ActivateBefore");
		String staticLink = activate3.edQualifiedStaticLink();
		return (staticLink + ".ActivateBefore(" + REAC + ',' + obj1 + ',' + obj2 + ')');
	}

	/// Java coding utility: Edit (Re)Activate Process AFTER ...
	/// @return the resulting Java source code
	private String edActivateAfter() {
		String obj1 = (object1 == null) ? "null" : "(RTS_Process)"+object1.toJavaCode();
		String obj2 = (object2 == null) ? "null" : "(RTS_Process)"+object2.toJavaCode();
		Meaning activate3 = Global.getCurrentScope().findMeaning("ActivateAfter");
		String staticLink = activate3.edQualifiedStaticLink();
		return (staticLink + ".ActivateAfter(" + REAC + ',' + obj1 + ',' + obj2 + ')');
	}
	
	@Override
	public void buildByteCode(CodeBuilder codeBuilder) {
		Type refProcess = Type.Ref("Process");
		if (object1 != null) {
			object1 = TypeConversion.testAndCreate(refProcess, object1);
			object1.backLink = this;
		}
		if (object2 != null) {
			object2 = TypeConversion.testAndCreate(refProcess, object2);
			object2.backLink = this;
		}
		if(time != null) 	 time = TypeConversion.testAndCreate(Type.LongReal, time);

		switch (code) {
		    case at:     buildActivateAt(codeBuilder); break;
		    case delay:	 buildActivateDelay(codeBuilder); break;
		    case before: buildActivateBefore(codeBuilder); break;
		    case after:  buildActivateAfter(codeBuilder); break;
		    case direct:
			default: buildActivateDirect(codeBuilder);
		}
	}

	/// ClassFile coding utility: Build direct (re)activation
	/// @param codeBuilder the codeBuilder to use.
	private void buildActivateDirect(CodeBuilder codeBuilder) {
//        0: getstatic     #47                 // Field _CUR:Lsimula/runtime/RTS_RTObject;
//        3: checkcast     #8                  // class simulaTestPrograms/adHoc000
//        6: iconst_0  or  iconst_1   Avhengig av  REAC
//        7: aload_0
//        8: getfield      #7                  // Field bil1_2:LsimulaTestPrograms/adHoc000_Car;
//       11: invokevirtual #51                 // Method ActivateDirect:(ZLsimula/runtime/RTS_Process;)V
		Meaning activate1 = Global.getCurrentScope().findMeaning("ActivateDirect");
		activate1.buildQualifiedStaticLink(codeBuilder);
		Constant.buildIntConst(codeBuilder, REAC);
		object1.buildEvaluation(null, codeBuilder);
		RTS.invokevirtual_Simulation_ActivateDirect(codeBuilder);
	}

	/// ClassFile coding utility: Build (Re)Activate Process AT ...
	/// @param codeBuilder the codeBuilder to use.
	private void buildActivateAt(CodeBuilder codeBuilder) {
//        29: getstatic     #49                 // Field _CUR:Lsimula/runtime/RTS_RTObject;
//        32: checkcast     #8                  // class simulaTestPrograms/adHoc000
//        35: iconst_0
//        36: aload_0
//        37: getfield      #13                 // Field bil2_2:LsimulaTestPrograms/adHoc000_Car;
//        40: ldc2_w        #66                 // double 7.449999809265137d
//        43: iconst_0
//        44: invokevirtual #68                 // Method ActivateAt:(ZLsimula/runtime/RTS_Process;DZ)V
		Meaning activate1 = Global.getCurrentScope().findMeaning("ActivateAt");
		activate1.buildQualifiedStaticLink(codeBuilder);
		Constant.buildIntConst(codeBuilder, REAC);
		object1.buildEvaluation(null, codeBuilder);
		time.buildEvaluation(null, codeBuilder);
		Constant.buildIntConst(codeBuilder, prior);
		RTS.invokevirtual_Simulation_ActivateAt(codeBuilder);
	}

	/// ClassFile coding utility: Build (Re)Activate Process DELAY ...
	/// @param codeBuilder the codeBuilder to use.
	private void buildActivateDelay(CodeBuilder codeBuilder) {
		Meaning activate1 = Global.getCurrentScope().findMeaning("ActivateDelay");
		activate1.buildQualifiedStaticLink(codeBuilder);
		Constant.buildIntConst(codeBuilder, REAC);
		object1.buildEvaluation(null, codeBuilder);
		time.buildEvaluation(null, codeBuilder);
		Constant.buildIntConst(codeBuilder, prior);
		RTS.invokevirtual_Simulation_ActivateDelay(codeBuilder);
	}

	/// ClassFile coding utility: Build (Re)Activate Process BEFORE ...
	/// @param codeBuilder the codeBuilder to use.
	private void buildActivateBefore(CodeBuilder codeBuilder) {
		Meaning activate1 = Global.getCurrentScope().findMeaning("ActivateBefore");
		activate1.buildQualifiedStaticLink(codeBuilder);
		Constant.buildIntConst(codeBuilder, REAC);
		object1.buildEvaluation(null, codeBuilder);
		object2.buildEvaluation(null, codeBuilder);
		RTS.invokevirtual_Simulation_ActivateBefore(codeBuilder);
	}

	/// ClassFile coding utility: Build (Re)Activate Process AFTER ...
	/// @param codeBuilder the codeBuilder to use.
	private void buildActivateAfter(CodeBuilder codeBuilder) {
		Meaning activate1 = Global.getCurrentScope().findMeaning("ActivateAfter");
		activate1.buildQualifiedStaticLink(codeBuilder);
		Constant.buildIntConst(codeBuilder, REAC);
		object1.buildEvaluation(null, codeBuilder);
		object2.buildEvaluation(null, codeBuilder);
		RTS.invokevirtual_Simulation_ActivateAfter(codeBuilder);
	}


	@Override
	public void printTree(final int indent, final Object head) {
		IO.println(edTreeIndent(indent)+this);
		object1.printTree(indent+1,this);
		if(object2 != null) object2.printTree(indent+1,this);
	}

	@Override
	public String toString() {
		String pri = "";
		if (prior) pri = " PRIOR";
		String activator = ((REAC) ? "REACTIVATE " : "ACTIVATE ") + object1;
		switch (code) {
		    case at, delay:     return (activator + ' ' + code + ' ' + time + pri);
		    case before, after: return (activator + ' ' + code + ' ' + object2);
		    case direct:
		    default: return (activator);
		}
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private ActivationStatement() { super(0); }

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeActivationStatement: " + this);
		oupt.writeKind(ObjectKind.ActivationStatement);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** ActivationStatement
		oupt.writeBoolean(REAC);
		oupt.writeObj(object1);
		oupt.writeObj(object2);
		oupt.writeObj(time);
		oupt.writeBoolean(prior);
	}

	/// Read and return an ActivationStatement object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the ActivationStatement object read from the stream.
	/// @throws IOException if something went wrong.
	public static ActivationStatement readObject(AttributeInputStream inpt) throws IOException {
		ActivationStatement stm = new ActivationStatement();
		stm.OBJECT_SEQU = inpt.readSEQU(stm);
		// *** SyntaxClass
		stm.lineNumber = inpt.readShort();
		// *** ActivationStatement
		stm.REAC = inpt.readBoolean();
		stm.object1 = (Expression) inpt.readObj();
		stm.object2 = (Expression) inpt.readObj();
		stm.time = (Expression) inpt.readObj();
		stm.prior = inpt.readBoolean();
		Util.TRACE_INPUT("ActivationStatement: " + stm);
		return(stm);
	}

}
