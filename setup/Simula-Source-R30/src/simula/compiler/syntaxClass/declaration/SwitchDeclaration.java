/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.declaration;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ConstantDescs;
import java.util.List;
import java.util.Vector;

import simula.compiler.JavaSourceFileCoder;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.ProtectedSpecification;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Switch Declaration.
/// 
/// <pre>
/// 
/// Simula Standard: 5.3 Switch declaration
/// 
///  switch-declaration
///     = SWITCH switch-identifier := switch-list
///     
///     switch-list = designational-expression { , designational-expression }
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/SwitchDeclaration.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class SwitchDeclaration extends ProcedureDeclaration {
	
	/// The switch list of label expressions.
	Vector<Expression> switchList = new Vector<Expression>();

	/// Create a new SwitchDeclaration.
	/// @param ident switch identifier
	public SwitchDeclaration(final String ident) {
		super(ident,ObjectKind.Procedure);
		if (Option.internal.TRACE_PARSE)	Parse.TRACE("Parse SwitchDeclaration");
		this.type = Type.Label;
		Parse.expect(KeyWord.ASSIGNVALUE);
		do { switchList.add(Expression.expectExpression());
		} while (Parse.accept(KeyWord.COMMA));
		if (Option.internal.TRACE_PARSE)	Parse.TRACE("Parse SwitchDeclaration(3), switchList=" + switchList);
		new Parameter("_SW", Type.Integer, Parameter.Kind.Simple).into(parameterList);
		Global.setScope(declaredIn);
	}

	@Override
	public void doChecking() {
		super.doChecking();
		for (Expression expr : switchList) {
			expr.doChecking();
			if(expr.type.keyWord != Type.T_LABEL) Util.error("Switch element "+expr+" is not a Label");
			expr.backLink = this; // To ensure _RESULT from functions
		}
		VirtualSpecification virtSpec=VirtualSpecification.getVirtualSpecification(this);
		if(virtSpec==null) {
			// Switch attributes are implicit specified 'protected'
			if(declaredIn.declarationKind==ObjectKind.Class)
				((ClassDeclaration)declaredIn).protectedList.add(new ProtectedSpecification((ClassDeclaration)Global.getCurrentScope(),identifier));
		}
	}

	// ***********************************************************************************************
	// *** Coding Utility: doCodeSwitchBody
	// ***********************************************************************************************
	@Override
	protected void codeProcedureBody() {
		boolean duringSTM_Coding=Global.duringSTM_Coding;
		Global.duringSTM_Coding=false;
		JavaSourceFileCoder.debug("// Switch Body");
		JavaSourceFileCoder.code("@Override");
		JavaSourceFileCoder.code("public " + getJavaIdentifier() + " _STM() {");
		Global.duringSTM_Coding=true;
		JavaSourceFileCoder.code("switch(p__SW-1) {");
		int n = 0;
		for (Expression expr : ((SwitchDeclaration) this).switchList) {
			JavaSourceFileCoder.code("case " + (n++) + ": _RESULT=" + expr.toJavaCode() + "; break;");				
		}
		JavaSourceFileCoder.code("default: throw new RTS_SimulaRuntimeError(\"Illegal switch index: \"+p__SW);");
		JavaSourceFileCoder.code("}");
		JavaSourceFileCoder.code("EBLK();");
		JavaSourceFileCoder.code("return(this);");
		JavaSourceFileCoder.code("}","End of Switch BODY");
		Global.duringSTM_Coding=duringSTM_Coding;
	}

	// ***********************************************************************************************
	// *** ByteCoding: buildMethod_STM_BODY
	// ***********************************************************************************************
	/// Generate byteCode for the '_STM' method.
	/// @param codeBuilder the CodeBuilder
	@Override
	protected void build_STM_BODY(CodeBuilder codeBuilder, Label begScope, Label endScope) {
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		List<SwitchCase> tableSwitchCases;
		int tableSize = switchList.size();
		tableSwitchCases = new Vector<SwitchCase>();
		for (int i = 1; i <= tableSize; i++) {
			tableSwitchCases.add(SwitchCase.of(i, codeBuilder.newLabel()));
		}
		// Build the TableSwitch Instruction
		Label defaultTarget = codeBuilder.newLabel(); // beginning of the default handler block.
		Label endLabel = codeBuilder.newLabel();
		int lowValue = 1;          // the minimum key value.
		int highValue = tableSize; // the maximum key value.
		codeBuilder
			.aload(0)
			.getfield(currentClassDesc(), "p__SW", ConstantDescs.CD_int)
			.tableswitch(lowValue, highValue, defaultTarget, tableSwitchCases);
		
		int n = 0;
		for (Expression expr : ((SwitchDeclaration) this).switchList) {
			//JavaSourceFileCoder.code("case " + (n++) + ": _RESULT=" + expr.toJavaCode() + "; break;");
			Label lab = tableSwitchCases.get(n++).target();
			codeBuilder
				.labelBinding(lab)
				.aload(0);
			
			expr.buildEvaluation(null,codeBuilder);
			
			codeBuilder
				.putfield(pool.fieldRefEntry(currentClassDesc(), "_RESULT", RTS.CD.RTS_LABEL)) // _RESULT
				.goto_(endLabel);
			
		}
		codeBuilder.labelBinding(defaultTarget);
		Util.buildSimulaRuntimeError("Illegal switch index: ", codeBuilder);

		codeBuilder.labelBinding(endLabel);

	}

	@Override
	public String toString() {
		return ("SWITCH " + identifier + " := " + switchList);
	}

}
