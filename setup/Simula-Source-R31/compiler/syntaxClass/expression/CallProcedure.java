/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.expression;

import java.util.Iterator;

import simula.compiler.syntaxClass.ProcedureSpecification;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.ArrayDeclaration;
import simula.compiler.syntaxClass.declaration.BlockDeclaration;
import simula.compiler.syntaxClass.declaration.Declaration;
import simula.compiler.syntaxClass.declaration.DeclarationScope;
import simula.compiler.syntaxClass.declaration.Parameter;
import simula.compiler.syntaxClass.declaration.ProcedureDeclaration;
import simula.compiler.syntaxClass.declaration.StandardProcedure;
import simula.compiler.syntaxClass.declaration.VirtualSpecification;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Util;

/// Java Coding Utilities: Call Procedure
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/CallProcedure.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class CallProcedure {
	/// Default constructor.
	CallProcedure(){}
	
	// ********************************************************************
	// *** CallProcedure.normal
	// ********************************************************************
	/// Java Coding: Edit Call Procedure Normal.
	/// @param variable the procedure variable
	/// @return piece of Java source code
	static String normal(final VariableExpression variable) {
		StringBuilder s=new StringBuilder();
		Meaning meaning=variable.meaning;
		Declaration decl=meaning.declaredAs;
		ProcedureDeclaration procedure = (ProcedureDeclaration) decl;
		s.append("new ").append(decl.getJavaIdentifier());
		String staticLink=meaning.edUnqualifiedStaticLink();
		// Generate Parameter Transmission
		s.append(edProcedureParameters(variable,staticLink,procedure)); 
		// Check if part of expression
		if(decl.type!=null && variable.backLink!=null) {
			s.append("._RESULT");
		}
		return(s.toString());
	}
	
	// ********************************************************************
	// *** CallProcedure.remote
	// ********************************************************************
	/// Java Coding: Edit Call Procedure Remote.
	/// @param obj Object Expression before DOT
	/// @param procedure Procedure Declaration
	/// @param func Function Designator, may be subscripted
	/// @param backLink if not null, this procedure call is part of the backLink Expression/Statement.
	/// @return piece of Java source code
	static String remote(final Expression obj,final ProcedureDeclaration procedure,final VariableExpression func,final SyntaxClass backLink) {
		if(procedure.myVirtual!=null) {
			// Call Remote Virtual Procedure
			return(remoteVirtual(obj,func,procedure.myVirtual.virtualSpec));
		} else if(procedure.declarationKind==ObjectKind.ContextFreeMethod) {
			// Call Remote Method
			return(asRemoteMethod(obj,procedure,func));
		} else if(procedure.declarationKind==ObjectKind.MemberMethod) {
			// Call Remote Method
			return(asRemoteMethod(obj,procedure,func));
		}
		String call="new "+procedure.getJavaIdentifier();
		String staticLink=obj.get();	  
		call=call+edProcedureParameters(func,staticLink,procedure);
		if(procedure.type!=null && backLink!=null) {
			call=call+"._RESULT";
		}
		return(call);
	}

	
	// ********************************************************************
	// *** CallProcedure.asRemoteMethod
	// ********************************************************************
	/// Java Coding: Edit Call Procedure as remote Method.
	/// @param obj Object Expression before DOT
	/// @param procedure Procedure Declaration
	/// @param func Function Designator, may be subscripted
	/// @return piece of Java source code
	private static String asRemoteMethod(final Expression obj,final ProcedureDeclaration procedure,final VariableExpression func) {
		BlockDeclaration declaredIn=(BlockDeclaration)procedure.declaredIn;
		if(declaredIn.isContextFree) {
			// Call Static Member Method
			String cast=declaredIn.getJavaIdentifier();
			String params=edProcedureParameters(func,obj.toJavaCode(),procedure);
			String methodCall=cast+'.'+procedure.getJavaIdentifier()+params;
			return(methodCall);
		}
		// Call Ordinary Member Method
		String params=edProcedureParameters(func,null,procedure);
		String methodCall=obj.toJavaCode()+'.'+procedure.getJavaIdentifier()+params;
		return(methodCall);
	}

	
	// ********************************************************************
	// *** CallProcedure.asNormalMethod
	// ********************************************************************
	/// Java Coding: Edit Call Procedure as normal Method.
	/// @param variable the procedure variable
	/// @return piece of Java source code
	static String asNormalMethod(final VariableExpression variable) { 
		Meaning meaning=variable.meaning;
		ProcedureDeclaration procedure = (ProcedureDeclaration) meaning.declaredAs;
		String params=edProcedureParameters(variable,null,procedure);
		if(meaning.declaredAs instanceof StandardProcedure)	{
		    if(Util.equals(variable.identifier, "detach")) {
		    	params="("+Global.sourceLineNumber+')';
		    }
		    else if(Util.equals(variable.identifier, "call")  
		          | Util.equals(variable.identifier, "resume") 	) {
		    	params=params.substring(0,params.length()-1);
		    	params=params+","+Global.sourceLineNumber+')';
		    }
		}
		String methodCall=meaning.declaredAs.getJavaIdentifier()+params;
		if(meaning.isConnected()) {
			DeclarationScope declaredIn = meaning.declaredIn;
			String connID=declaredIn.toJavaCode();
			return(connID+'.'+methodCall);
		}
		BlockDeclaration staticLink=(BlockDeclaration)meaning.declaredAs.declaredIn;
		if(!staticLink.isContextFree) {
			BlockDeclaration currentModule=Global.currentJavaFileCoder.blockDeclaration; // Class, Procedure, ...
			String castIdent=meaning.declaredIn.getJavaIdentifier();
			int n=meaning.declaredIn.getRTBlockLevel();
			if(n!=currentModule.getRTBlockLevel())
				methodCall="(("+castIdent+")"+meaning.declaredIn.edCTX()+")."+methodCall;
		}
		return(methodCall);
	}
	
	// ********************************************************************
	// *** CallProcedure.asStaticMethod
	// ********************************************************************
	/// Java Coding: Edit Call Procedure as static Method.
	/// @param variable the procedure variable
	/// @param isContextFree true if the procedure is independent of context
	/// @return piece of Java source code
	static String asStaticMethod(final VariableExpression variable,final boolean isContextFree) { 
		Meaning meaning=variable.meaning;
		ProcedureDeclaration procedure = (ProcedureDeclaration) meaning.declaredAs;
		String staticLinkString=null;
		if(!isContextFree) {
			DeclarationScope staticLink=procedure.declaredIn;
			staticLinkString=staticLink.edCTX();
		}
		String params=edProcedureParameters(variable,staticLinkString,procedure);

		String methodCall=meaning.declaredAs.getJavaIdentifier()+params;
		if(meaning.isConnected()) {
			String connID=meaning.declaredIn.toJavaCode();
			return(connID+'.'+methodCall);
		}
		if(!isContextFree) {
			BlockDeclaration currentModule=Global.currentJavaFileCoder.blockDeclaration; // Class, Procedure, ...
			String castIdent=meaning.declaredIn.getJavaIdentifier();
			int n=meaning.declaredIn.getRTBlockLevel();
			if(n!=currentModule.getRTBlockLevel())
				methodCall="(("+castIdent+")"+meaning.declaredIn.edCTX()+")."+methodCall;
		} else {
			String contextIdent=meaning.declaredIn.getJavaIdentifier();
			methodCall=contextIdent+"."+methodCall;
		}
		return(methodCall);
	}

	// ********************************************************************
	// *** CallProcedure.formal
	// ********************************************************************
	/// Java Coding: Edit Call Procedure Formal.
	/// @param variable the procedure variable
	/// @param par declared as parameter 'par'
	/// @return piece of Java source code
	static String formal(final VariableExpression variable,final Parameter par) {
		//return("<IDENT>.CPF().setPar(4).setpar(3.14)._ENT()");
		String ident=variable.edIdentifierAccess(false);
		if(par.mode==Parameter.Mode.name) ident=ident+".get()";
		return(codeCPF(ident,variable,null));
	}

	// ********************************************************************
	// *** CallProcedure.virtual
	// ********************************************************************
	/// Java Coding: Edit Call Procedure Virtual.
	/// @param variable the procedure variable
	/// @param virtual the virtual specification
	/// @param remotelyAccessed true if remotely accessed.
	/// @return piece of Java source code
	static String virtual(final VariableExpression variable,final VirtualSpecification virtual,final boolean remotelyAccessed) {
		//return("<IDENT>.CPF().setPar(4).setpar(3.14)._ENT()");
	    String ident=virtual.getVirtualIdentifier();
	    if(virtual.kind==VirtualSpecification.Kind.Label) return(ident);
	    if(variable.meaning.isConnected()) {
	    	String conn=variable.meaning.declaredIn.toJavaCode();
	        ident=conn+"."+ident;
	    } else if(!remotelyAccessed) {
		    String staticLink=variable.meaning.edQualifiedStaticLink();
	        ident=staticLink+"."+ident;
	    }
	    return(codeCPF(ident,variable,virtual.procedureSpec));
	}

	// ********************************************************************
	// *** CallProcedure.remoteVirtual
	// ********************************************************************
	/// Java Coding: Edit Call Procedure Remote Virtual.
	/// @param obj Object Expression before DOT
	/// @param variable the procedure variable
	/// @param virtual Virtual Specification
	/// @return piece of Java source code
	static String remoteVirtual(final Expression obj,final VariableExpression variable,final VirtualSpecification virtual) {
		//return("<Object>.<IDENT>.CPF().setPar(4).setpar(3.14)._ENT()");
		String ident=obj.get()+'.'+virtual.getVirtualIdentifier();
		return(codeCPF(ident,variable,virtual.procedureSpec));
	}
	
	// ********************************************************************
	// *** codeCPF
	// ********************************************************************
	/// Java Coding Utility: Edit Call Procedure Formal.
	/// @param ident the procedure identifier
	/// @param variable the procedure variable
	/// @param procedureSpec the procedure spec
	/// @return the resulting Java source code
	private static String codeCPF(final String ident,final VariableExpression variable,final ProcedureSpecification procedureSpec) {
		if(! variable.hasArguments()) {
			if(procedureSpec != null && procedureSpec.parameterList.size() > 0)
				Util.error("Missing parameter(s) to " + variable.identifier);
		}
		StringBuilder s=new StringBuilder();
		if(procedureSpec!=null) s.append(codeCSVP(ident,variable,procedureSpec));
		else {
			s.append(ident).append(".CPF()");
			if(variable.hasArguments()) {
				for(Expression actualParameter:variable.checkedParams) {
					actualParameter.backLink=actualParameter;  // To ensure _RESULT from functions
					s.append(".setPar(");
					Type formalType=actualParameter.type;
					int kind=Parameter.Kind.Simple;  // Default, see below
					if((actualParameter instanceof VariableExpression var) && !var.hasArguments()) {
						Declaration decl=var.meaning.declaredAs;
						if(decl instanceof StandardProcedure) {
							if(Util.equals(decl.identifier, "sourceline")) {
								actualParameter=new Constant(Type.Integer,Global.sourceLineNumber);
								actualParameter.doChecking();
							}
						}
						switch(decl.declarationKind) {
							case ObjectKind.SimpleVariableDeclaration -> kind=Parameter.Kind.Simple;
							case ObjectKind.Parameter -> kind=((Parameter)decl).kind;
							case ObjectKind.Procedure -> kind=Parameter.Kind.Procedure;
						//	case ObjectKind.Switch -> kind=Parameter.Kind.Procedure;
							case ObjectKind.ContextFreeMethod -> kind=Parameter.Kind.Simple;
							case ObjectKind.ArrayDeclaration -> kind=Parameter.Kind.Array;
							case ObjectKind.LabelDeclaration -> kind=Parameter.Kind.Label;
							case ObjectKind.Class -> kind=Parameter.Kind.Simple; // Error Recovery
							default -> Util.IERR("Flere sånne tilfeller ??? " + ObjectKind.edit(decl.declarationKind));
						}
					}
					int mode=Parameter.Mode.name; // NOTE: ALL PARAMETERS BY'NAME !!!
					s.append(doParameterTransmition(formalType,kind,mode,actualParameter));
					s.append(')');
				}
				s.append("._ENT()"); // Only when any parameter
			}
		}
		Type resultType=variable.type;
		if(procedureSpec!=null) resultType=procedureSpec.type;
		if(resultType!=null && variable.backLink!=null) {
			boolean partOfExpression=true;
			if(variable.backLink instanceof RemoteVariable binOper) {
				// NOTE: Standalone <expression>.<function> should not be casted
				if(binOper.backLink==null) partOfExpression=false;
			}
			if(partOfExpression) {
				s.append("._RESULT()");
				String callVirtual=s.toString();
				String cast=resultType.toJavaType();
				if(resultType.isArithmeticType())
					return(cast+"Value("+callVirtual+")");
				else return("(("+cast+")("+callVirtual+"))");
			}
		}
		return(s.toString());
	}
	
	// ********************************************************************
	// *** codeCSVP  -- Call Specified Virtual Procedure
	// ********************************************************************
	/// Java Coding Utility: Edit Call Specified Virtual Procedure.
	/// @param ident the procedure identifier
	/// @param variable the procedure variable
	/// @param procedureSpec the procedure spec
	/// @return the resulting Java source code
	private static String codeCSVP(final String ident, final VariableExpression variable,	final ProcedureSpecification procedureSpec) {
		StringBuilder s = new StringBuilder();
		s.append(ident).append(".CPF()");
		if (variable.hasArguments()) {
			Iterator<Parameter> formalIterator = procedureSpec.parameterList.iterator();
			Iterator<Expression> actualIterator = variable.checkedParams.iterator();
			while (actualIterator.hasNext()) {
				Expression actualParameter = actualIterator.next();
				Parameter formalParameter = (Parameter) formalIterator.next();
				s.append(".setPar(");
				Type formalType = formalParameter.type;
				int kind = formalParameter.kind;
				int mode = formalParameter.mode;
				s.append(doParameterTransmition(formalType, kind, mode, actualParameter));
				s.append(')');
			}
			s.append("._ENT()"); // Only when any parameter
		}
		return (s.toString());
	}
	
	// ********************************************************************
	// *** edProcedureParameters
	// ********************************************************************
	/// Java Coding Utility: Edit procedure parameters.
	/// @param variable a variable
	/// @param SL static link
	/// @param procedure the procedure
	/// @return the resulting Java source code
	private static String edProcedureParameters(final VariableExpression variable, final String SL, final ProcedureDeclaration procedure) {
		StringBuilder s = new StringBuilder();
		boolean prevPar = false;
		s.append('(');
		if (SL != null) {
			s.append(SL);
			prevPar = true;
		}
		if (variable.hasArguments()) {
			Iterator<Parameter> formalIterator = procedure.parameterList.iterator();
			Iterator<Expression> actualIterator = variable.checkedParams.iterator();
			while (actualIterator.hasNext()) {
				Expression actualParameter = actualIterator.next();
				Parameter formalParameter = (Parameter) formalIterator.next();
				if (formalParameter.nDim > 0) {
					int aDim = getNdim(actualParameter);
					if (aDim < 1)
						Util.warning("Parameter Array " + actualParameter
								+ " remains unchecked. Java or Runtime errors may occur");
					else if (aDim != formalParameter.nDim)
						Util.error("Parameter Array " + actualParameter + " has wrong number of dimensions");
				}
				if (prevPar)
					s.append(',');
				prevPar = true;
				Type formalType = formalParameter.type;
				int kind = formalParameter.kind;
				int mode = formalParameter.mode;
				s.append(doParameterTransmition(formalType, kind, mode, actualParameter));
			}
		}
		s.append(')');
		return (s.toString());
	}
	
	/// Returns the array's number of dimensions.
	/// @param actualParameter the array parameter
	/// @return the array's number of dimensions.
	private static int getNdim(final Expression actualParameter) {
    	VariableExpression aVar=null;
    	if(actualParameter instanceof RemoteVariable rem) aVar=rem.var;
    	else if(actualParameter instanceof VariableExpression var) aVar=var;
    	else return(-1); // Unchecked
    	Meaning meaning=aVar.meaning;
    	if(meaning.declaredAs instanceof Parameter par) return(par.nDim);    		
    	if(meaning.declaredAs instanceof ArrayDeclaration aArray) return(aArray.nDim);
    	return(-1);
    }
	
	// ********************************************************************
	// *** doParameterTransmition
	// ********************************************************************
    /// Java Coding Utility: Edit parameter transmission,
    /// @param formalType parameter's formal type
    /// @param kind parameter's kind
    /// @param mode parameter's transmission mode
    /// @param apar the actual parameter
    /// @return the resulting Java source code
 	private static String doParameterTransmition(final Type formalType,final int kind,final int mode,final Expression apar) {
		StringBuilder s = new StringBuilder();
		switch(kind) {
		    case Parameter.Kind.Simple -> doSimpleParameter(s,formalType,mode,apar);
		    case Parameter.Kind.Procedure -> doProcedureParameter(s,formalType,mode,apar);
		    case Parameter.Kind.Array -> doArrayParameter(s,formalType,mode,apar);
		    case Parameter.Kind.Label -> {
		    		String labQuant=apar.toJavaCode();
		    		if(mode==Parameter.Mode.name) {
		    			s.append("new RTS_NAME<RTS_LABEL>()");
		    			s.append("{ public RTS_LABEL get() { return("+labQuant+"); }");
		    			s.append(" }");
		    		}
		    		else s.append(labQuant);
		    	}
		}
		return(s.toString());
	}

	
	// ********************************************************************
	// *** doSimpleParameter -- Simple Variable as Actual Parameter
	// ********************************************************************
	/// Java Coding Utility: Edit simple parameter into the given StringBuilder. 
	/// @param s the StringBuilder
	/// @param formalType the formal type
	/// @param mode the parameter's mode
	/// @param apar actual parameter
	private static void doSimpleParameter(final StringBuilder s,final Type formalType,final int mode,final Expression apar) {
		if(mode==0) // Simple Type/Ref/Text by Default
		  	s.append(apar.toJavaCode());
		else if(mode==Parameter.Mode.value) { // Simple Type/Ref/Text by Value
		        if(formalType.keyWord == Type.T_TEXT)
		    	     s.append("copy(").append(apar.toJavaCode()).append(')');
		        else s.append(apar.toJavaCode());
		} else if(formalType.keyWord == Type.T_LABEL) {
		    	String labQuant=apar.toJavaCode();
		    	if(mode==Parameter.Mode.name) {
			    	  s.append("new RTS_NAME<RTS_LABEL>()");
				      s.append("{ public RTS_LABEL get() { return("+labQuant+"); }");
				      s.append(" }");
			    }
			    else s.append(labQuant);
		} else { // Simple Type/Ref/Text by Name
		    String javaTypeClass=formalType.toJavaTypeClass();
		    VariableExpression writeableVariable=apar.getWriteableVariable();
		    if(writeableVariable!=null) {
		    	s.append("new RTS_NAME<"+javaTypeClass+">()");
		    	s.append("{ public "+javaTypeClass+" get() { return("+apar.get()+"); }");
		    	if(!(writeableVariable.meaning.declaredAs instanceof BlockDeclaration)) {
		    		Type actualType=apar.type;
		    		String rhs="("+actualType.toJavaType()+")x_";
		    		if(apar instanceof TypeConversion) {
		    			// --------------------------------------------------
		    			// Generate something like:
		    			//  
		    			//  public Float put(Float x_) {
		    			//     float y=x_; 
		    			//	   n=(int) ( (float) y+0.5);
		    			//	   return(y);
		    			//  }
		    			// --------------------------------------------------
		    			String putValue=TypeConversion.mayBeConvert(actualType,writeableVariable.type,"y");
		    			s.append(" public "+javaTypeClass+" put("+javaTypeClass+" x_)");
		    			s.append("{ "+formalType.toJavaType()+" y=x_; ");
		    			s.append(writeableVariable.toJavaCode()).append(putValue);
		    			s.append("return(y); }");
		    		} else {
		    			// --------------------------------------------------
		    			// Generate something like:
		    			//  
		    			//  public Double put(Double x_) {
		    			//	   return (r = (double) x_);
		    			//  }
		    			// --------------------------------------------------
		    			s.append(" public "+javaTypeClass+" put("+javaTypeClass+" x_)"
		    						+" { return("+apar.put(rhs)+"); }");
		    			
		    		}
		    	}
		    	s.append(" }");
		    } else {
		    	s.append("new RTS_NAME<"+javaTypeClass+">()");
		    	s.append("{ public "+javaTypeClass+" get() { return("+apar.get()+"); }");
		    	s.append(" }");
		    }
		}
	}

	
	// ********************************************************************
	// *** doArrayParameter -- Array as Actual Parameter
	// ********************************************************************
	/// Java Coding Utility: Edit Array as Actual Parameter into the given StringBuilder.
	/// @param s the StringBuilder
	/// @param formalType the formal type
	/// @param mode the parameter mode
	/// @param apar actual parameter
	private static void doArrayParameter(final StringBuilder s,final Type formalType,final int mode,final Expression apar) {
		if(mode==Parameter.Mode.value) {
			s.append(apar.toJavaCode()).append(".COPY()");
		}
		else if(mode==Parameter.Mode.name) {
			s.append("new RTS_NAME<RTS_ARRAY>()");
			s.append("{ public RTS_ARRAY get() { return("+apar.toJavaCode()+"); }");
			s.append(" }");	
		} else s.append(apar.toJavaCode());
	}
	
	
	// ********************************************************************
	// *** doProcedureParameter -- Procedure as Actual Parameter
	// ********************************************************************
	/// Java Coding Utility: Edit Procedure as Actual Parameter into the given StringBuilder.
	/// @param s the StringBuilder
	/// @param formalType the formal type
	/// @param mode the parameter mode
	/// @param apar actual parameter
	private static void doProcedureParameter(final StringBuilder s, final Type formalType, final int mode, final Expression apar) {
		String procQuant = edProcedureQuant(apar);
		if (mode == Parameter.Mode.name) {
			// --- EXAMPLE -------------------------------------------------------------------------
			// r = new ParamSample_Q(this, new RTS_NAME<RTS_PRCQNT>() {
			//     public RTS_PRCQNT get() {
			//         return (new RTS_PRCQNT(ParamSample.this, ParamSample_P.class));
			//     }
			// })._RESULT;
			// -------------------------------------------------------------------------------------
			s.append("new RTS_NAME<RTS_PRCQNT>()");
			s.append("{ public RTS_PRCQNT get() { return(" + procQuant + "); }");
			s.append(" }");
		} else s.append(procQuant);
	}
	
	// ********************************************************************
	// *** edProcedureQuant
	// ********************************************************************
	/// Java Coding Utility: Edit new procedure quant.
	/// @param apar the actual parameter
	/// @return the resulting Java source code
	private static String edProcedureQuant(final Expression apar) {
	    if (apar instanceof VariableExpression var) {
			Declaration decl=var.meaning.declaredAs;
	    	String staticLink=var.meaning.edQualifiedStaticLink();
	    	String procIdent = decl.getJavaIdentifier();
			String procQuant = "new RTS_PRCQNT(" + staticLink + "," + procIdent + ".class)";
			if (decl instanceof Parameter par) {
				procQuant = ((VariableExpression) apar).getJavaIdentifier();
				if (par.mode == Parameter.Mode.name)
					procQuant = procQuant + ".get()";
			} else if (decl instanceof ProcedureDeclaration procedure) {
    	    	if(procedure.myVirtual!=null) {
    	    		VirtualSpecification vir = procedure.myVirtual.virtualSpec;
    				procQuant=staticLink+'.'+vir.getVirtualIdentifier();
    	    	}
			} else if (decl instanceof VirtualSpecification vir) {
				procQuant=staticLink+'.'+vir.getVirtualIdentifier();
			} else Util.IERR("Flere sånne(1) tilfeller ???  QUAL="+decl.getClass().getSimpleName());
			return(procQuant);
	    } else if (apar instanceof RemoteVariable rem) {
			// Check for <ObjectExpression> DOT <Variable>
			String staticLink = rem.obj.toJavaCode();
			VariableExpression var = rem.var;
			Declaration decl=var.meaning.declaredAs;
			if (decl instanceof VirtualSpecification vir) {
				return(staticLink+'.'+vir.getVirtualIdentifier());
			} else if (decl instanceof ProcedureDeclaration procedure) {
    	    	if(procedure.myVirtual!=null) {
    	    		VirtualSpecification vir = procedure.myVirtual.virtualSpec;
    				return(staticLink+'.'+vir.getVirtualIdentifier());
    	    	}
			} else Util.IERR("Flere sånne(2) tilfeller ???  QUAL="+decl.getClass().getSimpleName());
			String procIdent = var.meaning.declaredAs.getJavaIdentifier();
			return("new RTS_PRCQNT(" + staticLink + "," + procIdent + ".class)");
		} else Util.error("Illegal Procedure Expression as Actual Parameter: " + apar);
	    return("UNKNOWN"); // Error recovery
	}

}
