/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.expression;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Vector;
import java.lang.classfile.CodeBuilder;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.*;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Util;

/// Coding Utilities: Build Call Procedure (CP)
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/BuildCP.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class BuildCP {
	/** Default Constructor: NOT USED */ private BuildCP() {}

	// ********************************************************************
	// *** BuildCP.normal
	// ********************************************************************
	/// BuildCP.normal
	/// @param variable the procedure variable
	/// @param prc Procedure Declaration
	/// @param codeBuilder the CodeBuilder
	static void normal(final VariableExpression variable,final ProcedureDeclaration prc,final CodeBuilder codeBuilder) {
		//  	kkk := P(444);
		// ==>  kkk=new adHoc00_P((_CUR),444)._RESULT;
		//
		//    1: new           #46                 // class simulaTestPrograms/adHoc00_P
		/////  4: dup
		//    5: getstatic     #48                 // Field _CUR:Lsimula/runtime/RTS_RTObject;
		//    8: sipush        444
		//   11: invokespecial #51                 // Method simulaTestPrograms/adHoc00_P."<init>":(Lsimula/runtime/RTS_RTObject;I)V
		//
		///// 14: pop
		// or
		///// 14: getfield      #54                 // Field simulaTestPrograms/adHoc00_P._RESULT:I
		ClassDesc CD_prc=prc.getClassDesc();
		codeBuilder
			.new_(CD_prc)
			.dup();
		variable.meaning.buildQualifiedStaticLink(codeBuilder); // SL
		// Push parameters
		if(variable.checkedParams != null) {
			int n=variable.checkedParams.size();
			for(int i=0;i<n;i++) {
				Parameter p=prc.parameterList.get(i);
				p.buildParamCode(codeBuilder, variable.checkedParams.get(i));
			}
		}
		codeBuilder.invokespecial(CD_prc, "<init>", prc.getConstructorMethodTypeDesc());
		// Note: TOS points to the Procedure object.
		SyntaxClass backLink = variable.backLink;
		if(prc.type != null && backLink != null) {
			// Using TOS to access TOS._RESULT
			codeBuilder.getfield(prc.getResultFieldRefEntry(codeBuilder.constantPool()));
		} else {
			codeBuilder.pop(); // Pop off TOS
		}
	}

	// ********************************************************************
	// *** BuildCP.remote
	// ********************************************************************
	/// BuildCP.remote
	/// @param obj Object Expression before DOT
	/// @param procedure Procedure Declaration
	/// @param func Function Designator, may be subscripted
	/// @param backLink if not null, this procedure call is part of the backLink Expression/Statement.
	/// @param codeBuilder the CodeBuilder
	static void remote(final Expression obj,final ProcedureDeclaration procedure,final VariableExpression func,final SyntaxClass backLink,CodeBuilder codeBuilder) {
		if(procedure.myVirtual!=null) {
			// Call Remote Virtual Procedure
			BuildCPV.remoteVirtual(obj,func,procedure.myVirtual.virtualSpec,backLink,codeBuilder);
		} else if(procedure.declarationKind==ObjectKind.ContextFreeMethod) {
			// Call Remote Method
			Util.IERR();
		} else if(procedure.declarationKind==ObjectKind.MemberMethod) {
			// Call Remote Standard Member Method
			callRemoteStandardProcedure(obj,(StandardProcedure) procedure,func,codeBuilder);
			if(procedure.type != null) {
				SyntaxClass backLnk = obj.backLink;
				if(backLnk instanceof QualifiedObject qua)	backLnk = qua.backLink;
				if(backLnk instanceof RemoteVariable rem)	backLnk = rem.backLink;
				if(backLnk == null) codeBuilder.pop();
			}
		} else {
			ClassDesc CD_prc=procedure.getClassDesc();
			codeBuilder
				.new_(CD_prc)
				.dup();
			obj.buildEvaluation(null,codeBuilder);
			// Push parameters
			if(func.checkedParams != null) {
				int n=func.checkedParams.size();
				for(int i=0;i<n;i++) {
					Parameter p=procedure.parameterList.get(i);
					p.buildParamCode(codeBuilder, func.checkedParams.get(i));
				}
			}
			RemoteVariable remVar=(RemoteVariable)func.backLink;
			codeBuilder.invokespecial(CD_prc, "<init>", procedure.getConstructorMethodTypeDesc());
			if(remVar.backLink != null && procedure.type != null)
				 codeBuilder.getfield(procedure.getResultFieldRefEntry(codeBuilder.constantPool()));
			else codeBuilder.pop();
		}
	}


	// ********************************************************************
	// *** callRemoteStandardProcedure
	// ********************************************************************
	/// callRemoteStandardProcedure.
	/// @param beforeDot expression.
	/// @param pro StandardProcedure.
	/// @param variable the variable.
	/// @param codeBuilder the codeBuilder to use.
	private static void callRemoteStandardProcedure(Expression beforeDot,StandardProcedure pro,final VariableExpression variable,CodeBuilder codeBuilder) {
		if (beforeDot instanceof VariableExpression var) {
			Meaning meaning = var.meaning;
			ClassDesc owner = meaning.declaredIn.getClassDesc();
			Declaration remoteQual = meaning.declaredAs;
			if (remoteQual instanceof StandardProcedure prx) {
				switch(remoteQual.declarationKind) {
					case ObjectKind.MemberMethod -> {
						var.buildIdentifierAccess(false, codeBuilder);
						codeBuilder
							.invokevirtual(owner, prx.identifier, prx.getMethodTypeDesc(null,variable.checkedParams));
					}
					case ObjectKind.ContextFreeMethod -> {
						codeBuilder
							.invokestatic(owner, prx.identifier, prx.getMethodTypeDesc(null,variable.checkedParams));
					}
					default -> Util.IERR();
				}
			} else beforeDot.buildEvaluation(null, codeBuilder);
		} else beforeDot.buildEvaluation(null, codeBuilder);
		// PUSH Parameter values onto the stack
		checkForExtraParameter(variable);
		if(variable.checkedParams != null) for(Expression par:variable.checkedParams) {
			par.buildEvaluation(null,codeBuilder);
		}
		ClassDesc owner=ClassDesc.of("simula.runtime."+pro.declaredIn.externalIdent);
		codeBuilder.invokevirtual(owner, pro.identifier, pro.getMethodTypeDesc(null,variable.checkedParams));
		if(pro.type != null && variable.backLink == null)
			pro.type.pop(codeBuilder);
	}


	// ********************************************************************
	// *** BuildCP.normalStandardProcedure
	// ********************************************************************
	/// ClassFile coding utility: BuildCP.normalStandardProcedure
	/// @param variable the variable.
	/// @param codeBuilder the codeBuilder to use.
	static void normalStandardProcedure(final VariableExpression variable,CodeBuilder codeBuilder) {
		Meaning meaning=variable.meaning;
		StandardProcedure pro = (StandardProcedure) meaning.declaredAs;
		if (meaning.isConnected()) {
			Expression inspectedVariable = ((ConnectionBlock) meaning.declaredIn).getTypedInspectedVariable();
			callRemoteStandardProcedure(inspectedVariable, pro, variable, codeBuilder);
		} else {
			// PUSH Parameter values onto the stack
			boolean xpar = checkForExtraParameter(variable);
			boolean withFollowSL = meaning.declaredIn.buildCTX(codeBuilder);
			if(withFollowSL) codeBuilder.checkcast(meaning.declaredIn.getClassDesc());
			if (variable.checkedParams != null) {
				if(xpar) {
					for (Expression par : variable.checkedParams)
						par.buildEvaluation(null,codeBuilder);
				} else {
					int n=variable.checkedParams.size();
					for(int i=0;i<n;i++) {
						Parameter p=pro.parameterList.get(i);
						p.buildParamCode(codeBuilder, variable.checkedParams.get(i));
					}
				}
			}
			ClassDesc owner = meaning.declaredIn.getClassDesc();
			codeBuilder
				.invokevirtual(owner, pro.identifier, pro.getMethodTypeDesc(null,variable.checkedParams));
			if(pro.type != null && variable.backLink == null)
				pro.type.pop(codeBuilder);
		}
	}

	// ********************************************************************
	// *** BuildCP.staticStandardProcedure
	// ********************************************************************
	/// ClassFile coding utility: BuildCP.staticStandardProcedure
	/// @param variable the variable.
	/// @param codeBuilder the codeBuilder to use.
	static void staticStandardProcedure(final VariableExpression variable,CodeBuilder codeBuilder) {
		Meaning meaning=variable.meaning;
		StandardProcedure pro = (StandardProcedure) meaning.declaredAs;
		// PUSH Parameter values onto the stack
		if (variable.checkedParams != null) {
			int n=variable.checkedParams.size();
			for(int i=0;i<n;i++) {
				Parameter par = pro.parameterList.get(i);
				Expression expr = variable.checkedParams.get(i);
				par.buildParamCode(codeBuilder,expr);
			}
		}
		DeclarationScope declaredIn = variable.meaning.declaredIn;
		ClassDesc owner = declaredIn.getClassDesc();
		MethodTypeDesc MTD = null;
		if(pro.identifier.equalsIgnoreCase("histd")) {
			MTD = MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_REALTYPE_ARRAY;Lsimula/runtime/RTS_NAME;)I");
		} else if(pro.identifier.equalsIgnoreCase("discrete")) {
			MTD = MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_REALTYPE_ARRAY;Lsimula/runtime/RTS_NAME;)I");
		} else if(pro.identifier.equalsIgnoreCase("linear")) {
			MTD = MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_REALTYPE_ARRAY;Lsimula/runtime/RTS_REALTYPE_ARRAY;Lsimula/runtime/RTS_NAME;)D");
		} else MTD = pro.getMethodTypeDesc(null,variable.checkedParams);
		codeBuilder
			.invokestatic(owner, pro.identifier, MTD);
		if(pro.type != null && variable.backLink == null) {
			pro.type.pop(codeBuilder);
		}
	}
	
	// ********************************************************************
	// *** checkForExtraParameter
	// ********************************************************************
	/// Check for Extra Parameter sourceLineNumber
	/// @param variable the variable
	/// @return true: if extra parameter 'sourceLineNumber' is pushed.
	private static boolean checkForExtraParameter(VariableExpression variable) {
		String id = variable.identifier;
		if (id.equalsIgnoreCase("detach")) {
			variable.checkedParams = new Vector<Expression>();
			// Push extra parameter 'sourceLineNumber'
			Constant c = new Constant(Type.Integer, variable.lineNumber);
			variable.checkedParams.add(c);
			return true;
		} else if( id.equalsIgnoreCase("call") | id.equalsIgnoreCase("resume")) {
			if(variable.checkedParams.size() == 1) {
				// Push extra parameter 'sourceLineNumber'
				Constant c = new Constant(Type.Integer, variable.lineNumber);
				variable.checkedParams.add(c);
				return true;
			}
			else if(variable.checkedParams.size() == 2) return true; // Precompiled
		}
		return false;
	}


}
