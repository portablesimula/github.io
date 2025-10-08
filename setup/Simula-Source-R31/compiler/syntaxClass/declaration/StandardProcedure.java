/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.declaration;

import java.lang.constant.MethodTypeDesc;
import java.util.Vector;

import simula.compiler.syntaxClass.OverLoad;
import simula.compiler.syntaxClass.ProcedureSpecification;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.ObjectList;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Standard Procedure.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/StandardProcedure.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class StandardProcedure extends ProcedureDeclaration {
	/// Set of method type descriptors.
	private String[] mtdSet;
	
	/// The overload match
	private ProcedureSpecification overLoadMatch;
	
	/// The mtd picked by 'getLegalOverLoadMatch'
	private String mtdPicked;
	
	/// Create a new StandardProcedure without parameters.
	/// @param declaredIn the enclosing scope
	/// @param kind the declaration kind
	/// @param type the procedure's type
	/// @param ident the procedure identifier
	StandardProcedure(DeclarationScope declaredIn,int kind,Type type, String ident) {
		super(ident,kind); this.declaredIn = declaredIn; this.type = type; }

	/// Create a new StandardProcedure with parameters.
	/// @param declaredIn the enclosing scope
	/// @param kind the declaration kind
	/// @param type the procuedre's type
	/// @param ident the procedure identifier
	/// @param param the parameters
	StandardProcedure(DeclarationScope declaredIn,int kind,Type type, String ident,Parameter... param) {
		this(declaredIn,kind,type,ident);
		for(int i=0;i<param.length;i++) param[i].into(parameterList); }

	/// Create a new StandardProcedure with parameters.
	/// @param declaredIn the enclosing scope
	/// @param kind the declaration kind
	/// @param mtdSet the set of Method Type Descriptors
	/// @param type the procuedre's type
	/// @param ident the procedure identifier
	/// @param param the parameters
	StandardProcedure(DeclarationScope declaredIn,int kind,String[] mtdSet,Type type, String ident,Parameter... param) {
		this(declaredIn,kind,type,ident);
		this.mtdSet = mtdSet;
		for(int i=0;i<param.length;i++) param[i].into(parameterList); 
	}

	@Override
	public void doChecking() {
		if(IS_SEMANTICS_CHECKED()) return;
		Global.enterScope(this);
		Global.sourceLineNumber=lineNumber;
		Global.exitScope();
		if(Option.internal.TRACE_CHECKER) Util.TRACE("END StandardProcedure("+toString()+").doChecking - Result type="+this.type);
		SET_SEMANTICS_CHECKED();
	}
	
	/// Get OverLoadMatch.
	/// @param params the actual parameters
	/// @return OverLoadMatch
	public ProcedureSpecification getOverLoadMatch(Vector<Expression> params) {
		if(mtdSet != null) for(String mtd:mtdSet) {
			ProcedureSpecification legal = getLegalOverLoadMatch(mtd,params);
			if(legal != null) {
				this.overLoadMatch = legal;
				return(legal);
			}
		}
		return null;
	}
	
	/// Get LegalOverLoadMatch.
	/// @param mtd a method type descriptor
	/// @param params the actual parameters.
	/// @return a legal OverLoadMatch or null.
	private ProcedureSpecification getLegalOverLoadMatch(String mtd,Vector<Expression> params) {
		ProcedureSpecification spec = getProcedureSpecification(mtd);
		int n = params.size();
		int m = spec.parameterList.size();
		if(spec.parameterList.size() != params.size())
			n = Math.min(n, m);
		for(int i=0;i<n;i++) {
			Expression expr = params.get(i);
			expr.doChecking();
			Parameter par = spec.parameterList.get(i);
			if(!expr.type.equals(par.type)) return(null);
		}
		this.mtdPicked = mtd;
		return(spec);
	}
	
	/// Return the getProcedureSpecification obtained from the given MethodTypeDesc.
	/// @param mtd the MethodTypeDesc
	/// @return the getProcedureSpecification obtained from the given MethodTypeDesc.
	public ProcedureSpecification getProcedureSpecification(String mtd) {
		Type type = null;
		ObjectList<Parameter> pList = new ObjectList<Parameter>();
		
		boolean more = true;
		int pos=0;
		Type pType=null;
		
		if(mtd.charAt(pos++) != '(') Util.IERR();
		LOOP:while(more) {
			char c = mtd.charAt(pos++);
			switch(c) {
				case ')' -> { break LOOP; }
				case 'Z' -> pType = Type.Boolean;
				case 'C' -> pType = Type.Character;
				case 'I' -> pType = Type.Integer;
				case 'F' -> pType = Type.Real;
				case 'D' -> pType = Type.LongReal;
				case 'L' -> {
					StringBuilder sb = new StringBuilder();
					while(c != ';') { c = mtd.charAt(pos++); sb.append(c); }
					if(sb.toString().equals("simula/runtime/RTS_TXT;")) pType = Type.Text;
					else Util.IERR(""+sb);
				}
				default -> Util.IERR(""+c);
			}
			Parameter par = new Parameter("_p"+(pos-1), pType, Parameter.Kind.Simple);
			pList.add(par);
		}
		char c = mtd.charAt(pos++);
		switch(c) {
			case 'V' -> type = null;
			case 'Z' -> type = Type.Boolean;
			case 'C' -> type = Type.Character;
			case 'I' -> type = Type.Integer;
			case 'F' -> type = Type.Real;
			case 'D' -> type = Type.LongReal;
			case 'L' -> {
				StringBuilder sb = new StringBuilder();
				while(c != ';') { c = mtd.charAt(pos++); sb.append(c); }
				if(sb.toString().equals("simula/runtime/RTS_TXT;")) type = Type.Text;
				else Util.IERR(""+sb);
			}
			default -> Util.IERR(""+c);
		}
		
		return(new ProcedureSpecification(identifier, type, pList)); 
	}
	
	/// Get MethodTypeDesc
	/// @param beforeDot the Expression beforeDot
	/// @param params the actual parameters
	/// @return MethodTypeDesc
	public MethodTypeDesc getMethodTypeDesc(Expression beforeDot,Vector<Expression> params) {
		if(overLoadMatch !=null) {
			getOverLoadMatch(params);
			return(MethodTypeDesc.ofDescriptor(mtdPicked));
		} else return(MethodTypeDesc.ofDescriptor(this.edMethodTypeDesc(beforeDot,params)));
	}
	
	/// Edit MethodTypeDesc
	/// @param beforeDot the Expression beforeDot
	/// @param params the actual parameters
	/// @return MethodTypeDesc String
	private String edMethodTypeDesc(Expression beforeDot,Vector<Expression> params) {
		// MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_Printfile;");
		StringBuilder sb=new StringBuilder("(");
		if(beforeDot != null) {
			if(beforeDot.type instanceof OverLoad ovl) {
				boolean found=false;
				for(Type alt:ovl.type) {
					if(beforeDot.type.equals(alt)) {
						found=true;
						sb.append(beforeDot.type.toJVMType());
					}
				} if(!found) Util.IERR();
			} else sb.append(beforeDot.type.toJVMType());
		}
		if(parameterList!=null) for(int i=0;i<parameterList.size();i++) {
			Parameter par = parameterList.get(i);
			if(par.mode == Parameter.Mode.name) {
				sb.append("Lsimula/runtime/RTS_NAME;");
			} else if(par.kind == Parameter.Kind.Array) {
				sb.append("Lsimula/runtime/RTS_ARRAY;");				
			} else if(par.kind == Parameter.Kind.Procedure) {
				sb.append("Lsimula/runtime/RTS_PRCQNT;");				
			} else if(par.type instanceof OverLoad ovl) {
				boolean found=false;
				for(Type alt:ovl.type) {
					if(params.get(i).type.equals(alt)) {
						found=true;
						sb.append(params.get(i).type.toJVMType());
					}
				} if(!found) Util.IERR();
			} else sb.append(par.type.toJVMType());
		}
		String id=identifier;
		if(id.equalsIgnoreCase("detach") | id.equalsIgnoreCase("call") | id.equalsIgnoreCase("resume")) {
			// Push extra parameter 'sourceLineNumber'
			Parameter lno=new Parameter(id,Type.Integer,Parameter.Kind.Simple);
			sb.append(lno.type.toJVMType());
		}
		sb.append(')');
		if(this.type != null) {
			if(this.type instanceof OverLoad ovl) {
				boolean found=false;
				Type params1Type = params.get(0).type;
				for(Type alt:ovl.type) {
					if(params1Type.equals(alt)) {
						found=true;
						sb.append(params1Type.toJVMType());
					}
				} if(!found) Util.IERR();
			} else sb.append(this.type.toJVMType());
		} else sb.append('V');
		return(sb.toString());
	}

	@Override
	public String toString() {
		String pfx=""; if(type!=null) pfx=type.toString()+" ";
		return(pfx+"PROCEDURE "+identifier);
	}

}
