/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.expression;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Iterator;
import java.util.Vector;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.OverLoad;
import simula.compiler.syntaxClass.ProcedureSpecification;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.ArrayDeclaration;
import simula.compiler.syntaxClass.declaration.BlockDeclaration;
import simula.compiler.syntaxClass.declaration.ClassDeclaration;
import simula.compiler.syntaxClass.declaration.ConnectionBlock;
import simula.compiler.syntaxClass.declaration.Declaration;
import simula.compiler.syntaxClass.declaration.DeclarationScope;
import simula.compiler.syntaxClass.declaration.InspectVariableDeclaration;
import simula.compiler.syntaxClass.declaration.LabelDeclaration;
import simula.compiler.syntaxClass.declaration.Parameter;
import simula.compiler.syntaxClass.declaration.ProcedureDeclaration;
import simula.compiler.syntaxClass.declaration.SimpleVariableDeclaration;
import simula.compiler.syntaxClass.declaration.StandardProcedure;
import simula.compiler.syntaxClass.declaration.SwitchDeclaration;
import simula.compiler.syntaxClass.declaration.VirtualSpecification;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Variable.
/// 
/// <pre>
/// 
/// Simula Standard: 3.1 Variables
/// Simula Standard: 3.1.3. Array elements
/// Simula Standard: 3.1.4. Function designators
/// 
/// variable = simple-variable | subscripted-variable | simple-object-expression . variable
/// 
///    simple-object-expression = NONE | variable | function-designator | object-generator
///                             | local-object | qualified-object | ( object-expression )
///                             
///    simple-variable = identifier
/// 
///    subscripted-variable = function-designator | array-element
/// 
///       function-designator = procedure-identifier ( [ actual-parameter-part ] )
///  
///          actual-parameter-part = actual-parameter { , actual-parameter }
///          
///             actual-parameter = expression | array-identifier1 
///                              | switch-identifier1 | procedure-identifier1
///                              
///                identifier1 = identifier | remote-identifier
///                
///                   remote-identifier = simple-object-expression . attribute-identifier
///                                     | text-primary . attribute-identifier
/// 
///       array-element = array-identifier [ subscript-list ]
///       
///          subscript-list = arithmetic-expression { , arithmetic-expression }
/// 
/// </pre>
/// <b>Function designators:</b>
/// 
/// A function designator defines a value which results through the application
/// of a given set of rules defined by a procedure declaration (see 5.4) to a
/// fixed set of actual parameters. The rules governing specification of actual
/// parameters are given in 4.6.
/// 
/// Note: Not every procedure declaration defines rules for determining the value
/// of a function designator (cf. 5.4.1).
/// 
/// <b>Array elements:</b>
/// 
/// Subscripted variables designate values which are components of multi-
/// dimensional arrays. Each arithmetic expression of the subscript list occupies
/// one subscript position of the subscripted variable and is called a subscript.
/// The complete list of subscripts is enclosed by the subscript parentheses ( ).
/// The array component referred to by a subscripted variable is specified by
/// the actual value of its subscripts.
/// 
/// Each subscript position acts like a variable of type integer and the
/// evaluation of the subscript is understood to be equivalent to an assignment
/// to this fictitious variable. The value of the subscripted variable is defined
/// only if the actual integer value of each subscript expression is within the
/// associated subscript bounds of the array. A subscript expression value
/// outside its associated bounds causes a run time error.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/VariableExpression.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class VariableExpression extends Expression {

	/// The variable's identifier.
	public String identifier;

	/// The meaning of this variable.
	public Meaning meaning;

	/// Indicates that this variable is remotely accessed.
	private boolean remotelyAccessed;

	/// The parsed parameters.
	Vector<Expression> params;

	/// The checked parameters set by doChecking.
	public Vector<Expression> checkedParams; // Set by doChecking

	/// Create a new Variable.
	/// @param identifier the variable's identifier
	public VariableExpression(final String identifier) {
		this.identifier = identifier;
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("NEW Variable: " + identifier);
	}

	/// Returns true if this variable has arguments.
	/// @return true if this variable has arguments
	public boolean hasArguments() {
		return (params != null);
	}

	/// Returns a suitable java identifier for this variable.
	/// 
	/// @return a suitable java identifier
	public String getJavaIdentifier() {
		return (meaning.declaredAs.getJavaIdentifier());
	}

	/// This variable is remotely accessed through 'meaning'.
	/// @param meaning before dot
	void setRemotelyAccessed(final Meaning meaning) {
		this.meaning = meaning;
		remotelyAccessed = true;
		this.doChecking();
		SET_SEMANTICS_CHECKED(); // Checked as remote attribute
	}

	/// Returns the meaning.
	/// 
	/// If meaning is not set by setRemotelyAccessed then meaning by identifier will
	/// be set.
	/// @return the meaning
	public Meaning getMeaning() {
		if (meaning == null) {
			meaning = Global.getCurrentScope().findMeaning(identifier);
		}
		return (meaning);
	}

	/// Parse Utility: Expect Variable.
	/// <pre>
	/// Variable  =  Identifier  |  SubscriptedVariable
	/// 	SubscriptedVariable  =  Identifier  "("  Expression  {  ,  Expression  }  ")"
	/// </pre>
	/// Precondition: Identifier  is already read.
	/// @param ident the variable identifier
	/// @return the created Variable
	public static VariableExpression expectVariable(final String ident) {
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("Parse Variable, current=" + Parse.currentToken + ", prev=" + Parse.prevToken);
		VariableExpression variable = new VariableExpression(ident);
		if (Parse.accept(KeyWord.BEGPAR)) {
			variable.params = new Vector<Expression>();
			do {
				Expression par = acceptExpression();
				if (par == null)
					Util.error("Missing procedure parameter");
				else{
					variable.params.add(par);
					par.backLink = variable;
				}
			} while (Parse.accept(KeyWord.COMMA));
			Parse.expect(KeyWord.ENDPAR);
		}
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("NEW Variable: " + variable);
		return (variable);
	}

	@Override
	// Is redefined in Variable, RemoteVariable and TypeConversion
	public VariableExpression getWriteableVariable() {
		return (this);
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())
			return;
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("BEGIN Variable(" + identifier + ").doChecking: type=" + type);
		Global.sourceLineNumber = lineNumber;
		Declaration declaredAs = getMeaning().declaredAs;
		if (declaredAs != null)
			this.type = declaredAs.type;

		if (type != null && this.type.getRefIdent() != null && meaning.declaredIn instanceof ConnectionBlock conn) {
			if (type != null)
				type = new Type(type, conn);
		}

		if (meaning.declaredAs instanceof StandardProcedure) {
			if (Util.equals(identifier, "detach")) {
				if (meaning.declaredIn instanceof ConnectionBlock conn)
					conn.classDeclaration.detachUsed = true;
				else if (meaning.declaredIn instanceof ClassDeclaration cdecl)
					cdecl.detachUsed = true;
				else
					Util.error("Variable(" + identifier + ").doChecking:INTERNAL ERROR, "
							+ meaning.declaredIn.getClass().getSimpleName());
			}
		}
		Declaration decl = meaning.declaredAs;
		if (decl != null)
			switch (decl.declarationKind) {
			case ObjectKind.ArrayDeclaration:
				ArrayDeclaration array = (ArrayDeclaration) decl;
				this.type = array.type;
				if(params != null) {
					// Check parameters
					if (params.size() != array.nDim)
						Util.error("Wrong number of indices to " + array);
					checkedParams = new Vector<Expression>();
					for (Expression actualParameter : params) {
						actualParameter.doChecking();
						Expression checkedParameter = TypeConversion.testAndCreate(Type.Integer, actualParameter);
						checkedParameter.backLink = this;
						checkedParams.add(checkedParameter);
					}
				}
				break;

			case ObjectKind.Class:
//			case ObjectKind.PrefixedBlock:
			case ObjectKind.StandardClass:
			case ObjectKind.Procedure:
			case ObjectKind.ContextFreeMethod:
			case ObjectKind.MemberMethod:
				this.type = decl.type;
				Type overloadedType = this.type;
				Iterator<Parameter> paramIterator = null;
				if (decl instanceof ClassDeclaration cdecl)
					paramIterator = cdecl.new ClassParameterIterator();
				else if (decl instanceof ProcedureDeclaration) {
					paramIterator = ((ProcedureDeclaration) decl).parameterList.iterator();
					if(Option.compilerMode != Option.CompilerMode.viaJavaSource) {
						if(decl instanceof StandardProcedure prc) {
							if(prc.identifier.equalsIgnoreCase("histd")) ; // NOTHING
							else if(prc.identifier.equalsIgnoreCase("discrete")) ; // NOTHING
							else if(prc.identifier.equalsIgnoreCase("linear")) ; // NOTHING
							else {
								ProcedureSpecification overLoadMatch = prc.getOverLoadMatch(params);
								if(overLoadMatch != null)
									overloadedType = overLoadMatch.type;
							}
						}
					}
				} else Util.IERR();
				if (params == null) {
					if(decl.declarationKind != ObjectKind.Procedure) {
						if (paramIterator.hasNext())
							Util.error("Missing parameter(s) to " + decl.identifier);
					} else {
						if(!(decl instanceof SwitchDeclaration)) {
							if(backLink == null && paramIterator.hasNext())
								Util.error("Missing parameter(s) to " + decl.identifier);
						}
					}
				} else {
					// Check parameters
					checkedParams = new Vector<Expression>();
					Iterator<Expression> actualIterator = params.iterator();
					LOOP: while (actualIterator.hasNext()) {
						if (!paramIterator.hasNext()) {
							Util.error("Too many parameters to " + decl.identifier);
							break LOOP;
						}
						Parameter formalParameter = (Parameter) paramIterator.next();
						Type formalType = formalParameter.type;
						Expression actualParameter = actualIterator.next();
						actualParameter.doChecking();
						if (formalType instanceof OverLoad) {
							if(identifier.equalsIgnoreCase("addepsilon") || identifier.equalsIgnoreCase("subepsilon")) {
								formalType = actualParameter.type; // AD'HOC for add/subepsilon
								overloadedType = formalType;
							}
						}
						
						if (formalParameter.kind == Parameter.Kind.Array) {
							if (formalType != null && (!formalType.equals(actualParameter.type))
									&& formalType.isArithmeticType())
								Util.error("Parameter Array " + actualParameter + " must be of Type " + formalType);
						}
						Expression checkedParameter = TypeConversion.testAndCreate(formalType, actualParameter);
						checkedParameter.backLink = this;
						checkedParams.add(checkedParameter);
					}
					if (paramIterator.hasNext())
						Util.error("Missing parameter(s) to " + decl.identifier);
				}
				if (type instanceof OverLoad)
					this.type = overloadedType;
				break;

			case ObjectKind.Parameter:
				Parameter spec = (Parameter) decl;
				int kind = spec.kind;
				this.type = spec.type;
				if(params != null) {
					if (kind == Parameter.Kind.Array)
						spec.nDim = params.size();
					Iterator<Expression> actualIterator = params.iterator();
					checkedParams = new Vector<Expression>();
					while (actualIterator.hasNext()) {
						Expression actualParameter = actualIterator.next();
						actualParameter.doChecking();
						if (kind == Parameter.Kind.Array) {
							if (!actualParameter.type.isArithmeticType())
								Util.error("Illegal index-type");
							Expression checkedParameter = TypeConversion.testAndCreate(Type.Integer, actualParameter);
							checkedParameter.backLink = this;
							checkedParams.add(checkedParameter);
						} else
							checkedParams.add(actualParameter);
					}
				}
				break;
			case ObjectKind.VirtualSpecification:
				VirtualSpecification vspec = (VirtualSpecification) decl;
				this.type = vspec.type;
				if(params != null) {
					Iterator<Expression> pactualIterator = params.iterator();
					checkedParams = new Vector<Expression>();
					while (pactualIterator.hasNext()) {
						Expression actualParameter = pactualIterator.next();
						actualParameter.doChecking();
						if(checkedParams == null) checkedParams = new Vector<Expression>();
						checkedParams.add(actualParameter);
					}
				}
				break;

			case ObjectKind.SimpleVariableDeclaration:
			case ObjectKind.UndefinedDeclaration:
				if(params != null) Util.error("Illegal subscription of variable " + this.identifier);
				break;
				
			case ObjectKind.InspectVariableDeclaration:
			case ObjectKind.LabelDeclaration:
				break;
				
			default:
				Util.IERR();
			}

		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("END Variable(" + identifier + ").doChecking: type=" + type);
		SET_SEMANTICS_CHECKED();
	}

	// Returns true if this variable may be used as a statement.
	@Override
	public boolean maybeStatement() {
		ASSERT_SEMANTICS_CHECKED();
		if (meaning == null)
			return (false); // Error Recovery
		Declaration declaredAs = meaning.declaredAs;
		if (declaredAs == null)
			return (false); // Error Recovery
		switch (declaredAs.declarationKind) {
			case ObjectKind.Procedure:
//			case ObjectKind.Switch:
			case ObjectKind.ContextFreeMethod:
			case ObjectKind.MemberMethod:
				return (true);
				
			case ObjectKind.Parameter:
				Parameter par = (Parameter) declaredAs;
				return (par.kind == Parameter.Kind.Procedure);
			case ObjectKind.VirtualSpecification:
				VirtualSpecification vir = (VirtualSpecification) declaredAs;
				return (vir.kind == VirtualSpecification.Kind.Procedure);
			default:
		}
		return (false); // Variable, Parameter, Array, Class, Switch
	}

	// ******************************************************************
	// *** Coding: toJavaCode
	// ******************************************************************
	@Override
	public String toJavaCode() {
		ASSERT_SEMANTICS_CHECKED();
		return (get());
	}

	// ******************************************************************
	// *** Coding: doGetELEMENT
	// ******************************************************************
	/// Java coding utility: doGetELEMENT.
	/// @param var the variable
	/// @return a suitable java code
	public String doGetELEMENT(final String var) {
		StringBuilder s = new StringBuilder();
		s.append(var);
		char sep = '(';
		// A.getELEMENT(i);
		s.append(".getELEMENT");
		for (Expression ix : checkedParams) {
			String index = ix.toJavaCode();
			s.append(sep).append(index);
			sep = ',';
		}
		s.append(")");
		return (s.toString());
	}

	// ******************************************************************
	// *** Coding: doPutELEMENT
	// ******************************************************************
	/// Java coding utility: doPutELEMENT.
	/// @param var       the variable
	/// @param rightPart the right hand side
	/// @return a suitable java code
	public String doPutELEMENT(final String var, final String rightPart) {
		StringBuilder s = new StringBuilder();
		s.append(var);
		char sep = '(';
		// A.putELEMENT(rightPart,i);
		s.append(".putELEMENT(").append(var).append(".index");
		for (Expression ix : checkedParams) {
			String index = ix.toJavaCode();
			s.append(sep).append(index);
			sep = ',';
		}
		s.append("),").append(rightPart).append(")");
		return (s.toString());
	}

	// ******************************************************************
	// *** Coding: put
	// ******************************************************************
	// Generate code for putting an value(expression) into this Variable
	@Override
	public String put(final String rightPart) {
		ASSERT_SEMANTICS_CHECKED();
		String edited = this.editVariable(rightPart); // Is a Destination
		return (edited);
	}

	// ******************************************************************
	// *** Coding: get
	// ******************************************************************
	// Generate code for getting the value of this Variable
	@Override
	public String get() {
		ASSERT_SEMANTICS_CHECKED();
		String rightPart = null;
		String result = this.editVariable(rightPart); // Not a destination
		return (result);
	}

	// ******************************************************************
	// *** Coding: editVariable
	// ******************************************************************
	/// Java Coding Utility: Edit this Variable.
	/// @param rightPart When destination, this is the right part of the assignment
	/// @return the resulting Java source code
	private String editVariable(final String rightPart) {
		boolean destination = (rightPart != null);
		Declaration decl = meaning.declaredAs;
		ASSERT_SEMANTICS_CHECKED();
		StringBuilder s;
		switch (decl.declarationKind) {
			case ObjectKind.ArrayDeclaration:
				s = new StringBuilder();
				if (this.hasArguments()) { // Array Element Access
					String var = edIdentifierAccess(false);
					if (rightPart != null)
						return (doPutELEMENT(var, rightPart));
					else
						return (doGetELEMENT(var));
				} else {
					if (rightPart != null) {
						s.append(edIdentifierAccess(false)).append('=').append(rightPart);
					} else
						s.append(edIdentifierAccess(false));
				}
				return (s.toString());
	
			case ObjectKind.Class:
			case ObjectKind.StandardClass:
				Util.error("Illegal use of class identifier: " + decl.identifier);
				return (edIdentifierAccess(destination));
	
			case ObjectKind.LabelDeclaration:
				if (rightPart != null)
					Util.IERR();
				VirtualSpecification virtSpec = VirtualSpecification.getVirtualSpecification(decl);
				if (virtSpec != null)
					return (edIdentifierAccess(virtSpec.getVirtualIdentifier(), destination));
				return (edIdentifierAccess(destination));
	
			case ObjectKind.Parameter:
				s = new StringBuilder();
				Parameter par = (Parameter) decl;
				switch (par.kind) {
				case Parameter.Kind.Array: // Parameter Array
					String var = edIdentifierAccess(false);
					if (par.mode == Parameter.Mode.name)
						var = var + ".get()";
					if (this.hasArguments()) {
						String arrType = type.toJavaArrayType();
						String castedVar = "((" + arrType + ")" + var + ")";
						if (rightPart != null)
							return (doPutELEMENT(castedVar, rightPart));
						else
							return (doGetELEMENT(castedVar));
					} else {
						if (rightPart != null) {
							s.append(var).append('=').append(rightPart);
						} else {
							s.append(var);
						}
					}
					break;
				case Parameter.Kind.Procedure: // Parameter Procedure
					if (destination)
						Util.IERR();
					Expression inspectedVariable = meaning.getTypedInspectedVariable();
					if (inspectedVariable != null)
						s.append(inspectedVariable.toJavaCode()).append('.');
					if (par.mode == Parameter.Mode.value)
						Util.error("Parameter " + this + " by Value is not allowed - Rewrite Program");
					else // Procedure By Reference or Name.
						s.append(CallProcedure.formal(this, par));
					if (rightPart != null) {
						s.append('=').append(rightPart);
					}
					break;
				case Parameter.Kind.Simple:
				case Parameter.Kind.Label:
					var = edIdentifierAccess(destination); // Kind: Simple/Label
					if (!destination && par.mode == Parameter.Mode.name) {
						s.append(var).append(".get()");
					} else if (rightPart != null) {
						if (par.mode == Parameter.Mode.name) {
							s.append(var + ".put(" + rightPart + ')');
						} else
							s.append(var).append('=').append(rightPart);
					} else {
						s.append(edIdentifierAccess(destination)); // Kind: Simple/Label
					}
				}
				return (s.toString());
	
			case ObjectKind.ContextFreeMethod:
				// Standard Library Procedure
				if (Util.equals(identifier, "sourceline"))
					return ("" + Global.sourceLineNumber);
				if (destination) {
					Util.IERR();
					return ("_RESULT=" + rightPart);
				}
				return (CallProcedure.asStaticMethod(this, true));
	
			case ObjectKind.MemberMethod:
				if (destination) {
					Util.IERR();
					return ("_RESULT=" + rightPart);
				}
				return (CallProcedure.asNormalMethod(this));
	
			case ObjectKind.Procedure:
				// This Variable is a Procedure-Identifier.
				// When 'destination' it is a variable used to carry the resulting value until the final return.
				// otherwise; it is a ordinary procedure-call.
				if (destination) { // return("_RESULT");
					ProcedureDeclaration proc = (ProcedureDeclaration) meaning.declaredAs;
					if (proc.getRTBlockLevel() == Global.getCurrentScope().getRTBlockLevel()) {
						return "_RESULT" + "=" + rightPart;
					} else {
						String cast = proc.getJavaIdentifier();
						return "((" + cast + ")" + proc.edCTX() + ")._RESULT" + "=" + rightPart;
					}
				} else {
					ProcedureDeclaration procedure = (ProcedureDeclaration) decl;
					if (procedure.myVirtual != null)
						return CallProcedure.virtual(this, procedure.myVirtual.virtualSpec, remotelyAccessed);
					else
						return CallProcedure.normal(this);
				}
	
			case ObjectKind.SimpleVariableDeclaration:
			case ObjectKind.InspectVariableDeclaration:
				if (rightPart != null)
					return edIdentifierAccess(destination) + '=' + rightPart;
				else
					return edIdentifierAccess(destination);
	
			case ObjectKind.VirtualSpecification:
				if (rightPart != null)
					Util.IERR();
				VirtualSpecification virtual = (VirtualSpecification) decl;
				return CallProcedure.virtual(this, virtual, remotelyAccessed);

			case ObjectKind.UndefinedDeclaration:
				Util.IERR("");
	
			default:
				Util.IERR(""+ObjectKind.edit(decl.declarationKind));
		}
		return null;

	}

	// ***********************************************************************
	// *** Coding: edIdentifierAccess
	// ***********************************************************************
	/// Java coding utility: Edit identifier access.
	/// @param destination true if this variable is a destination
	/// @return a suitable java code
	public String edIdentifierAccess(boolean destination) {
		Declaration decl = meaning.declaredAs;
		String id = decl.getJavaIdentifier();
		String res = edIdentifierAccess(id, destination);
		return (res);
	}

	/// Java Coding Utility: Edit identifier access.
	/// @param id the identifier
	/// @param destination true if destination
	/// @return a suitable java code
	private String edIdentifierAccess(String id, boolean destination) {
		Expression constantElement = meaning.getConstant();
		if (constantElement != null) {
			if (constantElement instanceof Constant constant) {
				return (constant.toJavaCode());
			}
		}
		if (remotelyAccessed) {
			return (id);
		}
		if (meaning.isConnected()) {
			Expression inspectedVariable = ((ConnectionBlock) meaning.declaredIn).getTypedInspectedVariable();
			if (meaning.foundBehindInvisible) {
				String remoteCast = meaning.foundIn.getJavaIdentifier();
				id = "((" + remoteCast + ")(" + inspectedVariable.toJavaCode() + "))." + id;
			} else {
				id = inspectedVariable.toJavaCode() + "." + id;
			}
		} else if (!(meaning.declaredIn.declarationKind == ObjectKind.ContextFreeMethod
				|| meaning.declaredIn.declarationKind == ObjectKind.MemberMethod)) {
			
			String cast = meaning.declaredIn.getJavaIdentifier();
			int n = meaning.declaredIn.getRTBlockLevel();
			if (meaning.foundBehindInvisible)
				cast = meaning.foundIn.getJavaIdentifier();
			else if (n == Global.getCurrentScope().getRTBlockLevel())
				return (id); // currentScope may be a sub-block
			id = "((" + cast + ")" + meaning.declaredIn.edCTX() + ")." + id;
		}
		return (id);
	}


	/// ClassFile Coding Utility: Edit identifier access.
	/// @param destination true if destination
	/// @param codeBuilder the CodeBuilder
	public void buildIdentifierAccess(boolean destination,CodeBuilder codeBuilder) {
		if (remotelyAccessed) return;
		meaning.buildIdentifierAccess(destination, codeBuilder);
	}

	// ******************************************************************
	// *** Coding: buildEvaluation
	// ******************************************************************
	/// ClassFile Coding Utility: Build this Variable.
	/// @param rightPart When destination, this is the right part of the assignment
	/// @param codeBuilder the CodeBuilder
	@Override
	public void buildEvaluation(Expression rightPart,CodeBuilder codeBuilder) {	setLineNumber();
		ASSERT_SEMANTICS_CHECKED();
		Declaration decl=meaning.declaredAs;
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		boolean destination = (rightPart != null);
		VariableExpression inspectedVariable = meaning.getInspectedVariable();
		switch (decl.declarationKind) {
			case ObjectKind.ArrayDeclaration:
				ArrayDeclaration arr=(ArrayDeclaration)decl;
				buildIdentifierAccess(false,codeBuilder);
				if (this.hasArguments())
					 arr.arrayGetElement(this, false, codeBuilder);
				else codeBuilder.getfield(pool.fieldRefEntry(arr.declaredIn.getClassDesc(), arr.identifier, ArrayDeclaration.getClassDesc(type)));
				break;

			case ObjectKind.Class:
			case ObjectKind.StandardClass:
				Util.error("Illegal use of class identifier: " + decl.identifier);
				break;

			case ObjectKind.LabelDeclaration:
				if (destination) Util.IERR();
				buildIdentifierAccess(false,codeBuilder);
				LabelDeclaration lab=(LabelDeclaration)decl;
				VirtualSpecification virtSpec = VirtualSpecification.getVirtualSpecification(decl);
				if (virtSpec == null) {
					codeBuilder.getfield(lab.getFieldRefEntry(pool));
				} else {
					String ident = virtSpec.getSimpleVirtualIdentifier();
					ClassDesc owner = virtSpec.declaredIn.getClassDesc();
					codeBuilder.invokevirtual(owner, ident, MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_LABEL;"));
				}
				break;

			case ObjectKind.Parameter:
				buildEvaluateParameter((Parameter) decl,inspectedVariable,rightPart,codeBuilder);
				break;

			case ObjectKind.ContextFreeMethod:
				if (Util.equals(identifier, "sourceline"))
					 Constant.buildIntConst(codeBuilder, this.lineNumber);
				else BuildCP.staticStandardProcedure(this,codeBuilder);
				break;

			case ObjectKind.MemberMethod:
				BuildCP.normalStandardProcedure(this,codeBuilder);
				break;

			case ObjectKind.Procedure:
//			case ObjectKind.Switch:
				ProcedureDeclaration procedure = (ProcedureDeclaration) decl;
				if (destination) {
					codeBuilder
						.aload(0)
						.getfield(procedure.result.getFieldRefEntry(pool));
					Util.IERR();
				} //else
				if (procedure.myVirtual != null)
					 BuildCPV.virtual(this, procedure.myVirtual.virtualSpec, remotelyAccessed, codeBuilder);
				else {
					BuildCP.normal(this, procedure, codeBuilder);
				}
				break;

			case ObjectKind.SimpleVariableDeclaration:
				SimpleVariableDeclaration var=(SimpleVariableDeclaration)decl;
				if(var.constantElement != null) {
					var.constantElement.buildEvaluation(null,codeBuilder);
					break;
				}
				if(inspectedVariable != null) {
					ConnectionBlock cblk=(ConnectionBlock)meaning.declaredIn;
					boolean withFollowSL = meaning.declaredIn.buildCTX(codeBuilder);
					if(withFollowSL) {
						DeclarationScope declaredIn = cblk.declaredIn;
						int bl = declaredIn.getRTBlockLevel();
						if(bl == 0) { // Accessing _USR
							ClassDesc main = Global.programModule.mainModule.getClassDesc();
							codeBuilder.checkcast(main);
						} else {
							while(declaredIn.declaredIn.getRTBlockLevel() == bl) declaredIn = declaredIn.declaredIn;
							codeBuilder.checkcast(declaredIn.getClassDesc());
						}
					}
					codeBuilder
						.getfield(inspectedVariable.getFieldRefEntry(pool))
						.checkcast( ((DeclarationScope)var.declaredIn).getClassDesc())
						.getfield(var.getFieldRefEntry(pool));
				} else {
					buildIdentifierAccess(destination,codeBuilder);
					codeBuilder.getfield(var.getFieldRefEntry(pool));
				}
				break;

			case ObjectKind.InspectVariableDeclaration:
				InspectVariableDeclaration ivar=(InspectVariableDeclaration)decl;
				if(inspectedVariable != null) {
//					ConnectionBlock cblk=(ConnectionBlock)meaning.declaredIn;
//					boolean withFollowSL = meaning.declaredIn.buildCTX(codeBuilder);
//					if(withFollowSL) {
//						codeBuilder.checkcast(cblk.declaredIn.getClassDesc());
//					}
//					
//					codeBuilder
//						.getfield(inspectedVariable.getFieldRefEntry(pool))
//						.checkcast( ((DeclarationScope)ivar.declaredIn).getClassDesc())
//						.getfield(ivar.getFieldRefEntry(pool));
					Util.IERR("DON'T BELIEVE THIS WILL EVER HAPPEN !");
				} else {
					buildIdentifierAccess(destination,codeBuilder);
					codeBuilder.getfield(ivar.getFieldRefEntry(pool));
				}
				break;

			case ObjectKind.VirtualSpecification:
				VirtualSpecification virtual = (VirtualSpecification) decl;
				BuildCPV.virtual(this, virtual, remotelyAccessed,codeBuilder);
				break;

			default:
				Util.IERR(""+ObjectKind.edit(decl.declarationKind));
		}
	}


	// ***************************************************************************************
	// *** JVM CODING: getFieldRefEntry
	// ***************************************************************************************
	/// ClassFile Coding Utility: Return the field ref entry for this variable.
	/// @param pool the ConstantPoolBuilder to use.
	/// @return the field ref entry for this variable.
	public FieldRefEntry getFieldRefEntry(ConstantPoolBuilder pool) {
		ClassDesc owner=meaning.declaredIn.getClassDesc();
		Declaration declaredAs = meaning.declaredAs;
		return(pool.fieldRefEntry(owner, declaredAs.getJavaIdentifier(), declaredAs.type.toClassDesc()));
	}
	
	
	// ******************************************************************
	// *** Coding: buildEvaluateParameter
	// ******************************************************************
	/// ClassFile Coding Utility: Build this Parameter.
	/// @param par the Parameter
	/// @param inspectedVariable then inspected variable or null
	/// @param rightPart When destination, this is the right part of the assignment
	/// @param codeBuilder the CodeBuilder
	private void buildEvaluateParameter(Parameter par,Expression inspectedVariable,Expression rightPart,CodeBuilder codeBuilder) {
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		boolean destination = (rightPart != null);
		switch (par.kind) {
		case Parameter.Kind.Array: // Parameter Array
			buildIdentifierAccess(destination,codeBuilder);
			if (par.mode == Parameter.Mode.name) {
				codeBuilder.getfield(par.getFieldRefEntry(pool));
				RTS.invokevirtual_NAME_get(codeBuilder);
				codeBuilder.checkcast(RTS.CD.RTS_ARRAY(type));
				if(checkedParams != null)
					ArrayDeclaration.arrayGetElement2(type,par.getFieldIdentifier(),checkedParams,codeBuilder);
			} else {
				if (this.hasArguments()) {
					if (destination)
						 ArrayDeclaration.arrayPutElement(meaning,par.getFieldIdentifier(),true,this.checkedParams,rightPart,codeBuilder);
					else ArrayDeclaration.arrayGetElement(type,par.getFieldIdentifier(),true,this.checkedParams,null,par.declaredIn,codeBuilder);
				} else {
					if (destination) Util.IERR();
					ClassDesc owner = (inspectedVariable == null)
							? BlockDeclaration.currentClassDesc()
									: inspectedVariable.type.getQual().getClassDesc();
					codeBuilder.getfield(owner, par.getFieldIdentifier(), RTS.CD.RTS_ARRAY);
				}
			}
			break;

		case Parameter.Kind.Procedure: // Parameter Procedure
			if (destination)               Util.IERR();
			if (inspectedVariable != null) Util.IERR();
			if (par.mode == Parameter.Mode.value)
				Util.error("Parameter " + this + " by Value is not allowed - Rewrite Program");
			else { // Procedure By Reference or Name.
				BuildCPF.formal(this, par, codeBuilder);
				if(par.type == null) codeBuilder.pop();
			}
			break;

		case Parameter.Kind.Simple, Parameter.Kind.Label:
			buildIdentifierAccess(destination,codeBuilder); // Kind: Simple/Label
			codeBuilder.getfield(par.getFieldRefEntry(pool));
			if (!destination && par.mode == Parameter.Mode.name) {
				RTS.invokevirtual_NAME_get(codeBuilder);
				par.type.checkCast(codeBuilder);
				par.type.valueToPrimitiveType(codeBuilder);

			}
			break;
		}
	}

	@Override
	public void printTree(final int indent, final Object head) {
		IO.println(edTreeIndent(indent)+this);
	}

	// ***********************************************************************
	// *** Utility: toString
	// ***********************************************************************
	@Override
	public String toString() {
		if (params == null)
			return ("" + identifier + "  type=" + this.type);
		else
			return (("" + identifier + params).replace('[', '(').replace(']', ')') );// + "  backLink=" + this.backLink);
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O.
	public VariableExpression() {
	}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("BEGIN Write VariableExpression: "+this);
		oupt.writeKind(ObjectKind.VariableExpression);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** Expression
		oupt.writeType(type);
		oupt.writeObj(backLink);
		// *** VariableExpression
		oupt.writeString(identifier);
		oupt.writeBoolean(remotelyAccessed);
		
		if(params == null) {
			oupt.writeShort(0);			
		} else {
			oupt.writeShort(params.size());
			for(Expression par:params) oupt.writeObj(par);
		}
	}
	
	/// Read and return a VariableExpression object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the VariableExpression object read from the stream.
	/// @throws IOException if something went wrong.
	public static VariableExpression readObject(AttributeInputStream inpt) throws IOException {
		VariableExpression var = new VariableExpression();
		var.OBJECT_SEQU = inpt.readSEQU(var);
		// *** SyntaxClass
		var.lineNumber = inpt.readShort();
		// *** Expression
		var.type = inpt.readType();
		var.backLink = (SyntaxClass) inpt.readObj();
		// *** VariableExpression
		var.identifier = inpt.readString();
		var.remotelyAccessed = inpt.readBoolean();
		
		int n = inpt.readShort();
		if(n > 0) {
			var.params = new Vector<Expression>();
			for(int i=0;i<n;i++)
				var.params.add((Expression) inpt.readObj());
		}
		
		Util.TRACE_INPUT("readVariableExpression: " + var);
		return(var);
	}

}
