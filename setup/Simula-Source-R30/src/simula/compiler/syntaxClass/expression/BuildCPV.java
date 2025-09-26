/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.expression;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Iterator;

import simula.compiler.syntaxClass.ProcedureSpecification;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.BlockDeclaration;
import simula.compiler.syntaxClass.declaration.Declaration;
import simula.compiler.syntaxClass.declaration.Parameter;
import simula.compiler.syntaxClass.declaration.ProcedureDeclaration;
import simula.compiler.syntaxClass.declaration.Thunk;
import simula.compiler.syntaxClass.declaration.VirtualSpecification;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.Meaning;

/// Coding Utilities: Build Call Procedure Virtual (CPV).
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/BuildCPV.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public abstract class BuildCPV {
	/** Default Constructor: NOT USED */ private BuildCPV() {}

	// ********************************************************************
	// *** Build ProcedureCall virtual
	// ********************************************************************
	/// Build Call Procedure Virtual
	/// @param variable the procedure variable
	/// @param virtual the virtual specification
	/// @param remotelyAccessed true if remotely accessed.
	/// @param codeBuilder the CodeBuilder
	static void virtual(final VariableExpression variable,final VirtualSpecification virtual,final boolean remotelyAccessed,CodeBuilder codeBuilder) {
		if(! variable.hasArguments()) {
			if(virtual.procedureSpec != null && virtual.procedureSpec.parameterList.size() > 0) {
				Util.error("Missing parameter(s) to " + variable.identifier);
			}
		}
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		//return("<IDENT>.CPF().setPar(4).setpar(3.14)._ENT()");
	    String ident=virtual.getSimpleVirtualIdentifier();
	    
	    if(virtual.kind==VirtualSpecification.Kind.Label) {
	    	// 	return(ident);
	        // 1: aload_0
	        // 2: invokevirtual #20                 // Method L_0:()Lsimula/runtime/RTS_LABEL;
			ClassDesc owner = BlockDeclaration.currentClassDesc();
			variable.buildIdentifierAccess(false, codeBuilder);
			codeBuilder
				.invokevirtual(owner, ident, MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_LABEL;"));

	    } else {
		    Meaning meaning = variable.meaning;
			prepareForValueType(variable, codeBuilder);
	    	if(meaning.isConnected()) {
	    		// String conn=variable.meaning.declaredIn.toJavaCode();
	    		// ident=conn+"."+ident;
	    		// 32: aload_0
	    		// 33: getfield      #17                 // Field _inspect_21_0:LsimulaTestPrograms/adHoc00_A;
	    		// 36: invokevirtual #37                 // Method simulaTestPrograms/adHoc00_A.P1_0:()Lsimula/runtime/RTS_PRCQNT;
	    		// 39: invokevirtual #43                 // Method simula/runtime/RTS_PRCQNT.CPF:()Lsimula/runtime/RTS_PROCEDURE;
	    		// 42: pop
		    	VariableExpression inspectedVariable = meaning.getInspectedVariable();
		    	ClassDesc owner = meaning.declaredIn.getClassDesc();
		    	inspectedVariable.buildIdentifierAccess(false, codeBuilder);
				codeBuilder
					.getfield(pool.fieldRefEntry(BlockDeclaration.currentClassDesc(), inspectedVariable.getJavaIdentifier(), inspectedVariable.type.toClassDesc()))
					.invokevirtual(owner, ident, MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_PRCQNT;"));
				if(virtual.procedureSpec != null) {
					BuildCPV.buildCSVP(variable, virtual.procedureSpec, codeBuilder);					
				} else {
					BuildCPF.buildCPF(variable, codeBuilder);
				}
			    
			    ProcedureDeclaration prc=(ProcedureDeclaration)meaning.declaredAs;
				if(prc.type == null || variable.backLink == null)
					codeBuilder.pop();
				
		    } else {
		        //  0: getstatic     #7                  // Field _CUR:Lsimula/runtime/RTS_RTObject;
		        //  3: checkcast     #8                  // class simulaTestPrograms/adHoc000_PBLK20
		        //  6: invokevirtual #13                 // Method P1_0:()Lsimula/runtime/RTS_PRCQNT;
		        //  9: invokevirtual #17                 // Method simula/runtime/RTS_PRCQNT.CPF:()Lsimula/runtime/RTS_PROCEDURE;
		        // 12: pop
				boolean withFollowSL = meaning.declaredIn.buildCTX(codeBuilder);
				if(withFollowSL) codeBuilder.checkcast(meaning.declaredIn.getClassDesc());
		    	ClassDesc owner = meaning.declaredIn.getClassDesc();
				codeBuilder
					.invokevirtual(owner, ident, MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_PRCQNT;"));
				
				if(virtual.procedureSpec != null)
					 buildCSVP(variable, virtual.procedureSpec, codeBuilder);					
				else BuildCPF.buildCPF(variable, codeBuilder);
				
			    if(variable.backLink == null) codeBuilder.pop();
		    }
	    }
	}

	// ********************************************************************
	// *** BuildCPV.remoteVirtual
	// ********************************************************************
	/// BuildCPV.remoteVirtual
	/// @param obj Object Expression before DOT
	/// @param variable the procedure variable
	/// @param virtual Virtual Specification
	/// @param backLink if not null, this procedure call is part of the backLink Expression/Statement.
	/// @param codeBuilder the CodeBuilder to use
	static void remoteVirtual(final Expression obj,final VariableExpression variable,final VirtualSpecification virtual,final SyntaxClass backLink,CodeBuilder codeBuilder) {
		if(! variable.hasArguments()) {
			if(virtual.procedureSpec != null && virtual.procedureSpec.parameterList.size() > 0)
				Util.error("Missing parameter(s) to " + variable.identifier);
		}
		//return("<Object>.<IDENT>.CPF().setPar(4).setpar(3.14)._ENT()");
	    String ident=virtual.getSimpleVirtualIdentifier();
		prepareForValueType(variable, codeBuilder);
		obj.buildEvaluation(null, codeBuilder);
		codeBuilder.invokevirtual(obj.type.toClassDesc(),ident, MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_PRCQNT;"));
		if(virtual.procedureSpec != null)
			 buildCSVP(variable, virtual.procedureSpec, codeBuilder);					
		else BuildCPF.buildCPF(variable, codeBuilder);
		
	    if(backLink == null) codeBuilder.pop();
	}
	
	// ********************************************************************
	// *** codeCSVP  -- Call Specified Virtual Procedure
	// ********************************************************************
	/// Coding Utility: Build Call Specified Virtual Procedure.
	/// @param variable the procedure variable
	/// @param procedureSpec the procedure spec
	/// @param codeBuilder the CodeBuilder
	private static void buildCSVP(final VariableExpression variable,final ProcedureSpecification procedureSpec,CodeBuilder codeBuilder) {
//        25: invokevirtual #46                 // Method simula/runtime/RTS_PRCQNT.CPF:()Lsimula/runtime/RTS_PROCEDURE;
		
//        28: bipush        7
//        30: invokestatic  #52                 // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
//        33: invokevirtual #58                 // Method simula/runtime/RTS_PROCEDURE.setPar:(Ljava/lang/Object;)Lsimula/runtime/RTS_PROCEDURE;
		
//        36: bipush        21
//        38: invokestatic  #52                 // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
//        41: invokevirtual #58                 // Method simula/runtime/RTS_PROCEDURE.setPar:(Ljava/lang/Object;)Lsimula/runtime/RTS_PROCEDURE;
		
//        44: invokevirtual #64                 // Method simula/runtime/RTS_PROCEDURE._ENT:()Lsimula/runtime/RTS_PROCEDURE;
//        47: invokevirtual #67                 // Method simula/runtime/RTS_PROCEDURE._RESULT:()Ljava/lang/Object;
//        50: checkcast     #71                 // class simula/runtime/RTS_File

		
		RTS.invokevirtual_PRCQNT_CPF(codeBuilder);
		if (variable.hasArguments()) {
			Iterator<Parameter> formalIterator = procedureSpec.parameterList.iterator();
			Iterator<Expression> actualIterator = variable.checkedParams.iterator();
			while (actualIterator.hasNext()) {
				Expression actualParameter = actualIterator.next();
				Parameter formalParameter = (Parameter) formalIterator.next();
				Type formalType = formalParameter.type;
				actualParameter = TypeConversion.testAndCreate(formalType, actualParameter);
				actualParameter.buildEvaluation(null, codeBuilder);
				formalType.buildObjectValueOf(codeBuilder);
				// s.append(".setPar(");
				RTS.invokevirtual_PROCEDURE_setpar(codeBuilder);
			}
			// s.append("._ENT()"); // Only when any parameter
			RTS.invokevirtual_PROCEDURE_ENT(codeBuilder);
		}
		BuildLoad_RESULT(variable, codeBuilder);			
	}
	
	// ********************************************************************
	// *** prepareForValueType
	// ********************************************************************
	/// ClassFile coding utility: Prepare for ValueType.
	/// @param variable the variable
	/// @param codeBuilder the codeBuilder to use.
	private static void prepareForValueType(final VariableExpression variable, CodeBuilder codeBuilder) {
	    // Prepare for possible _RESULT to Object conversion in buildCPF.
		Type resultType = functionResultType(variable);
		if(resultType != null && resultType.isValueType() && variable.backLink != null) {
			codeBuilder.aload(0);
			if(Global.getCurrentScope() instanceof Thunk thunk) {
				ConstantPoolBuilder pool=codeBuilder.constantPool();
				codeBuilder
					.getfield(RTS.FRE.NAME_CUR(pool))
					.checkcast(thunk.declaredIn.getClassDesc());
			}
		}
	}
	
	/// ClassFile coding utility: Get function ResultType.
	/// @param variable the variable.
	/// @return the function ResultType.
	private static Type functionResultType(VariableExpression variable) {
		Declaration proc = variable.meaning.declaredAs;
		Type resultType = proc.type;
		if(proc instanceof VirtualSpecification virt) {
			if(virt.procedureSpec != null) {
				resultType = virt.procedureSpec.type;
			}
		}
		return resultType;
	}
	
	/// ClassFile coding utility: Build Load_RESULT.
	/// @param variable the variable
	/// @param codeBuilder the codeBuilder to use.
	private static void BuildLoad_RESULT(VariableExpression variable, CodeBuilder codeBuilder) {
		SyntaxClass backLink = variable.backLink;
		if(backLink instanceof RemoteVariable rem) backLink = rem.backLink;
		Type resultType = functionResultType(variable);
		if(resultType!=null && variable.backLink!=null) {
			boolean partOfExpression=true;
			if(variable.backLink instanceof RemoteVariable binOper) {
				// NOTE: Standalone <expression>.<function> should not be casted
				if(binOper.backLink==null) partOfExpression=false;
			}
			if(partOfExpression) {
				RTS.invokevirtual_PROCEDURE_RESULT(codeBuilder);
				if(resultType.isValueType()) {
					RTS.objectToPrimitiveType(resultType, codeBuilder);
				} else {
					resultType.checkCast(codeBuilder);				
				}
			}
		}
	}

}
