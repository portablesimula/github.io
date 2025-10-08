/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.declaration;

import java.io.IOException;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeBuilder.BlockCodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodSignature;
import java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Vector;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.expression.Constant;
import simula.compiler.syntaxClass.statement.DummyStatement;
import simula.compiler.syntaxClass.statement.Statement;
import simula.compiler.utilities.ClassHierarchy;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.LabelList;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.ObjectList;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Procedure Declaration.
/// <pre>
/// Simula Standard: 5.4 Procedure declaration
/// 
///      procedure-declaration
///          = [ type ] PROCEDURE procedure-heading ; procedure-body
///      
///            procedure-heading
///                = procedure-identifier [ formal-parameter-part ; [ mode-part ] specification-part ] 
///                
///               procedure-identifier = identifier
///                
///               formal-parameter-part = "(" formal-parameter { , formal-parameter } ")"
///            
///                  formal-parameter = identifier
///            
///               specification-part = specifier identifier-list { ; specifier identifier-list }
///            
///                  specifier
///                      = type [ ARRAY | PROCEDURE ]
///                      | LABEL
///                      | SWITCH 
///                
///               mode-part
///                   = name-part [ value-part ]
///                   | value-part [ name-part ]
///            
///                  name-part = NAME identifier-list ;
///            
///                  value-part = VALUE identifier-list ;
///            
///               identifier-list = identifier { , identifier }
///                
///            procedure-body = statement
/// </pre>
/// This class is prefix to StandardProcedure and SwitchDeclaration.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/ProcedureDeclaration.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public class ProcedureDeclaration extends BlockDeclaration {

	/// Result in case of Type Procedure 
	public SimpleVariableDeclaration result;

	/// Parameter list.
	public ObjectList<Parameter> parameterList = new ObjectList<Parameter>();
	
	/// Virtual Match indicator. 
	/// 
	/// If myVirtual != null, this Procedure is a Virtual Match.
	/// 
	/// Set during doChecking.
	public VirtualMatch myVirtual; // Set during doChecking

	// ***********************************************************************************************
	// *** CONSTRUCTORS
	// ***********************************************************************************************
	/// Create a new ProcedureDeclaration.
	/// @param identifier procedure identifier
	/// @param declarationKind procedure or switch
	protected ProcedureDeclaration(final String identifier,final int declarationKind) {
		super(identifier);
		this.declarationKind = declarationKind;
	}

	// ***********************************************************************************************
	// *** Parsing: expectProcedureDeclaration
	// ***********************************************************************************************
	/// Parse and build a ProcedureDeclaration.
	/// 
	/// <pre>
	/// Syntax:
	/// 
	///      procedure-declaration
	///          = [ type ] PROCEDURE procedure-heading ; procedure-body
	///      
	///            procedure-heading
	///                = procedure-identifier [ formal-parameter-part ; [ mode-part ] specification-part ] 
	///                
	///            procedure-identifier = identifier
	/// </pre>
	/// 
	/// Precondition: [ type ] PROCEDURE is already read.
	/// 
	/// @param type procedure's type
	/// @return a newly created ProcedureDeclaration
	public static ProcedureDeclaration expectProcedureDeclaration(final Type type) {
		ProcedureDeclaration proc = new ProcedureDeclaration(null, ObjectKind.Procedure);
		proc.sourceFileName = Global.sourceFileName;
		proc.lineNumber=Parse.prevToken.lineNumber;
		proc.type = type;
		if (Option.internal.TRACE_PARSE)
			Parse.TRACE("Parse ProcedureDeclaration, type=" + type);
		proc.modifyIdentifier(Parse.expectIdentifier());
		if (Parse.accept(KeyWord.BEGPAR)) {
			expectFormalParameterPart(proc.parameterList);
			Parse.expect(KeyWord.SEMICOLON);
			while(acceptModePart(proc.parameterList));
			expectSpecificationPart(proc);
		} else Parse.expect(KeyWord.SEMICOLON);
		expectProcedureBody(proc);

		proc.lastLineNumber = Global.sourceLineNumber;
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("Line "+proc.lineNumber+": ProcedureDeclaration: "+proc);
		Global.setScope(proc.declaredIn);
		return (proc);
	}
	
	/// Parse Utility: Accept mode-part and set matching parameter's mode.
	/// <pre>
	///   mode-part
	///      = name-part [ value-part ]
	///      | value-part [ name-part ]
	///            
	///   name-part = NAME identifier-list ;
	///            
	///   value-part = VALUE identifier-list ;
	///            
	///   identifier-list = identifier { , identifier }
	/// </pre>
	/// @param pList the parameter list
	/// @return true: if mode-part was present.
	private static boolean acceptModePart(Vector<Parameter> pList) {
		if (Parse.accept(KeyWord.VALUE, KeyWord.NAME)) {
			int mode = (Parse.prevToken.getKeyWord() == KeyWord.VALUE)
					? Parameter.Mode.value
					: Parameter.Mode.name;
			do {
				String identifier = Parse.expectIdentifier();
				Parameter parameter = null;
				for (Parameter par : pList)
					if (Util.equals(identifier, par.identifier)) {
						parameter = par;
						break;
					}
				if (parameter == null) {
					Util.error("Identifier " + identifier + " is not defined in this scope");
					parameter = new Parameter(identifier);
				}
				parameter.setMode(mode);
			} while (Parse.accept(KeyWord.COMMA));
			Parse.expect(KeyWord.SEMICOLON);
			return(true);
		}
		return(false);
	}
	
	/// Parse Utility: Accept Procedure Parameter specification-part updating Parameter's type and kind.
	/// <pre>
	/// Syntax:
	/// 
	///     specification-part
    ///           = specifier identifier-list { ; specifier identifier-list }
	///     
	///        specifier = Type | [Type] ARRAY | [Type] PROCEDURE ] | LABEL | SWITCH
	/// </pre>
	/// @param proc the procedure declaration
	private static void expectSpecificationPart(ProcedureDeclaration proc) {
		if (Option.internal.TRACE_PARSE)	Parse.TRACE("Parse ParameterSpecifications");
		LOOP: while(true) {
			Type type;
			int kind = Parameter.Kind.Simple;
			if (Parse.accept(KeyWord.SWITCH)) {
				type = Type.Label;
				kind = Parameter.Kind.Procedure;
			} else if (Parse.accept(KeyWord.LABEL))
				type = Type.Label;
			else {
				type = Parse.acceptType();
				//if (type == null) return (false);
				if (Parse.accept(KeyWord.ARRAY)) {
					if (type == null) {
						// See Simula Standard 5.2 -
						// If no type is given the type real is understood.
						type=Type.Real;
					}
					kind = Parameter.Kind.Array;
				}
				else if (Parse.accept(KeyWord.PROCEDURE)) kind = Parameter.Kind.Procedure;
				else if(type == null) break LOOP;
			}
			do {
				String identifier = Parse.expectIdentifier();
				Parameter parameter = null;
				for (Parameter par : proc.parameterList)
					if (Util.equals(identifier,par.identifier)) { parameter = par; break; }
				if (parameter == null) {
					Util.error("Identifier " + identifier + " is not defined in this scope");
					parameter = new Parameter(identifier);
				}
				parameter.setTypeAndKind(type, kind);
			} while (Parse.accept(KeyWord.COMMA));
			Parse.expect(KeyWord.SEMICOLON);
			continue LOOP;
		}
		for (Parameter par : proc.parameterList) {
			if (par.kind != 0)
				switch (par.kind) {
				case Parameter.Kind.Array:
				case Parameter.Kind.Label:
				case Parameter.Kind.Procedure:
					break; // OK
				case Parameter.Kind.Simple:
				default:
					if (par.type == null)
						Util.error("Missing specification of parameter: " + par.identifier);
				}
		}
	}

	/// Parse Utility: Expect procedure-body.
	/// In case of a compound-statement, updating the procedure's declaration and statement lists.
	/// <pre>
	/// Syntax:
	///                
	///        procedure-body = statement
	/// </pre>
	/// 
	/// @param proc the procedure
	private static void expectProcedureBody(ProcedureDeclaration proc) {
		if (Parse.accept(KeyWord.BEGIN)) {
			Statement stm;
			if (Option.internal.TRACE_PARSE)	Parse.TRACE("Parse Procedure Block");
			while (Declaration.acceptDeclaration(proc)) {
				Parse.accept(KeyWord.SEMICOLON);
			}
//			Vector<Statement> stmList = proc.statements;
			ObjectList<Statement> stmList = proc.statements;
			while (!Parse.accept(KeyWord.END)) {
				stm = Statement.expectStatement();
				if (stm != null) stmList.add(stm);
			}
		}
		else proc.statements.add(Statement.expectStatement());
	}

	// ***********************************************************************************************
	// *** Checking
	// ***********************************************************************************************
	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())
			return;
		Global.sourceLineNumber = lineNumber;
		Global.enterScope(this);
			LabelList.accumLabelList(this);
			if(type != null) {
				this.result = new SimpleVariableDeclaration(type, "_RESULT");
				declarationList.add(result);
			}
			int prfx = 0;// prefixLevel();
			if (declarationKind == ObjectKind.Procedure)
				for (Parameter par : this.parameterList) par.setExternalIdentifier(prfx);
			for (Declaration par : this.parameterList) par.doChecking();
			for (Declaration dcl : declarationList) dcl.doChecking();
			for (Statement stm : statements) stm.doChecking();
			VirtualSpecification virtualSpec = VirtualSpecification.getVirtualSpecification(this);
			if (virtualSpec != null) {
				// This Procedure is a Virtual Match
				if(! Type.equalsOrSubordinate(virtualSpec.type, this.type))
					Util.error("Virtual match has wrong type " + type + ", specified as " + virtualSpec.type);
				if(virtualSpec.procedureSpec != null) {
					ObjectList<Parameter> list1 = this.parameterList;
					ObjectList<Parameter> list2 = virtualSpec.procedureSpec.parameterList;
					if(list1.size() != list2.size()) {
						Util.error("Virtual match has wrong number of parameters " + list1.size() + ". Specified with " + list2.size());	
					} else {
						for(int i=0;i<list1.size();i++)
							if(! list1.get(i).equals(list2.get(i)))
								Util.error("Virtual match has wrong heading. Parameter " + (i+1) + " does not match the specification");	
					}
				} 
				myVirtual = new VirtualMatch(virtualSpec, this);
				ClassDeclaration decl = (ClassDeclaration) declaredIn;
				decl.virtualMatchList.add(myVirtual);
				if (decl == virtualSpec.declaredIn) virtualSpec.hasDefaultMatch = true;
			}
		Global.exitScope();
		SET_SEMANTICS_CHECKED();
	}

	// ***********************************************************************************************
	// *** Utility: findVisibleAttributeMeaning
	// ***********************************************************************************************
	@Override
	public Meaning findVisibleAttributeMeaning(final String ident) {
		if(Option.internal.TRACE_FIND_MEANING>0) Util.println("BEGIN Checking Procedure for "+ident+" ================================== "+identifier+" ==================================");
		for (Declaration declaration : declarationList) {
			if(Option.internal.TRACE_FIND_MEANING>1) Util.println("Checking Local "+declaration);
			if (Util.equals(ident, declaration.identifier))
				return (new Meaning(declaration, this, this, false));
		}
		for (Parameter parameter : parameterList) {
			if(Option.internal.TRACE_FIND_MEANING>1) Util.println("Checking Parameter "+parameter);
			if (Util.equals(ident, parameter.identifier))
				return (new Meaning(parameter, this, this, false));
		}
		if(labelList != null) for (LabelDeclaration label : labelList.getDeclaredLabels()) {
			if(Option.internal.TRACE_FIND_MEANING>1) Util.println("Checking Label "+label);
			if (Util.equals(ident, label.identifier))
				return (new Meaning(label, this, this, false));
		}
		if(Option.internal.TRACE_FIND_MEANING>0) Util.println("ENDOF Checking Procedure for "+ident+" ================================== "+identifier+" ==================================");
		return (null);
	}

	// ***********************************************************************************************
	// *** Coding: doJavaCoding
	// ***********************************************************************************************
	@Override
	public void doJavaCoding() {
		ASSERT_SEMANTICS_CHECKED();
		if (this.isPreCompiledFromFile != null) {
			if(Option.verbose) IO.println("Skip  doJavaCoding: "+this.identifier+" -- It is read from "+isPreCompiledFromFile);		
		} else {
			switch (declarationKind) {
				case ObjectKind.Procedure -> doProcedureCoding();
				default -> Util.IERR();
			}
		}
	}

	// ***********************************************************************************************
	// *** Coding Utility: edFormalParameterList
	// ***********************************************************************************************
	/// Java Coding Utility: Edit the formal parameter list
	/// @param isInlineMethod true if generating an inline method, otherwise false
	/// @param addStaticLink add static link as 0'th parameter
	/// @return the resulting Java code
	private String edFormalParameterList(final boolean isInlineMethod,final boolean addStaticLink) {
		// Accumulates through prefix-chain when class
		StringBuilder s = new StringBuilder();
		s.append('(');
		boolean withparams = false;
		if (addStaticLink) {
			s.append("RTS_RTObject _SL");
			withparams = true;
		}
		for (Declaration par : this.parameterList) {
			if (withparams)	s.append(',');
			withparams = true;
			s.append(((Parameter) par).toJavaType()).append(' ');
			if (isInlineMethod)
				 s.append(par.identifier);
			else s.append('s').append(par.externalIdent); // s to indicate Specified Parameter
		}
		s.append(") {");
		return (s.toString());
	}

	// ***********************************************************************************************
	// *** Coding: PROCEDURE
	// ***********************************************************************************************
	/// Generate java source code for this Procedure.
	private void doProcedureCoding() {
		Global.sourceLineNumber = lineNumber;
		ASSERT_SEMANTICS_CHECKED();
		if (this.isPreCompiledFromFile != null) {
			if(Option.verbose) IO.println("Skip  doProcedureCoding: "+this.identifier+" -- It is read from "+isPreCompiledFromFile);		
			return;
		}
		JavaSourceFileCoder javaCoder = new JavaSourceFileCoder(this);
		Global.enterScope(this);
			labelList.setLabelIdexes();
			JavaSourceFileCoder.code("@SuppressWarnings(\"unchecked\")");
			JavaSourceFileCoder.code("public final class " + getJavaIdentifier() + " extends RTS_PROCEDURE {");
			JavaSourceFileCoder.debug("// ProcedureDeclaration: Kind=" + declarationKind + ", BlockLevel=" + getRTBlockLevel()
						+ ", firstLine=" + lineNumber + ", lastLine=" + lastLineNumber + ", hasLocalClasses="
						+ ((hasLocalClasses) ? "true" : "false") + ", System=" + ((isQPSystemBlock()) ? "true" : "false"));
			if (isQPSystemBlock())
				JavaSourceFileCoder.code("public boolean isQPSystemBlock() { return(true); }");
			if ( declarationKind == ObjectKind.Procedure && type != null) {
				JavaSourceFileCoder.code("@Override");
				JavaSourceFileCoder.code("public Object _RESULT() { return("+this.result.identifier+"); }");
			}
			JavaSourceFileCoder.debug("// Declare parameters as attributes");
			boolean hasParameter = false;
			for (Parameter par : parameterList) {
				String tp = par.toJavaType();
				hasParameter = true;
				JavaSourceFileCoder.code("public " + tp + ' ' + par.externalIdent + ';');
			}
			if(this.hasAccumLabel()) {
				JavaSourceFileCoder.debug("// Declare local labels");
				for (LabelDeclaration lab : labelList.getAccumLabels())
					lab.declareLocalLabel(this);
			}
			JavaSourceFileCoder.debug("// Declare locals as attributes");
			for (Declaration decl : declarationList) decl.doJavaCoding();
			if (declarationKind == ObjectKind.Procedure && hasParameter) doCodePrepareFormal();
			doCodeConstructor();
			codeProcedureBody();
			javaCoder.codeProgramInfo();
			JavaSourceFileCoder.code("}", "End of Procedure");
		Global.exitScope();
		javaCoder.closeJavaOutput();
	}

	// ***********************************************************************************************
	// *** Coding Utility: doCodeConstructor
	// ***********************************************************************************************
	/// Generate Java source code for the constructor.
	private void doCodeConstructor() {
		JavaSourceFileCoder.debug("// Normal Constructor");
		JavaSourceFileCoder.code("public " + getJavaIdentifier() + edFormalParameterList(false, true));
		JavaSourceFileCoder.code("super(_SL);");
		JavaSourceFileCoder.debug("// Parameter assignment to locals");
		for (Parameter par : parameterList)
			JavaSourceFileCoder.code("this." + par.externalIdent + " = s" + par.externalIdent + ';');
		JavaSourceFileCoder.code("BBLK();");
		JavaSourceFileCoder.debug("// Declaration Code");
		for (Declaration decl : declarationList) decl.doDeclarationCoding();
		JavaSourceFileCoder.code("_STM();");
		JavaSourceFileCoder.code("}");
	}

	// ***********************************************************************************************
	// *** Coding Utility: doCodePrepareFormal
	// ***********************************************************************************************
	/// Generate Java source code prepared for 'call formal procedure'.
	private void doCodePrepareFormal() {
		JavaSourceFileCoder.debug("// Parameter Transmission in case of Formal/Virtual Procedure Call");
		JavaSourceFileCoder.code("@Override");
		JavaSourceFileCoder.code("public " + getJavaIdentifier() + " setPar(Object param) {");
		JavaSourceFileCoder.code("try {");
		JavaSourceFileCoder.code("switch(_nParLeft--) {");
		int nPar = 0;
		for (Parameter par : parameterList) {
			String tp = par.toJavaType();
			String typeValue;
			if (par.mode == Parameter.Mode.name) typeValue = ("(" + tp + ")param");
			else {
				switch(par.kind) {
					case Parameter.Kind.Simple -> {
						if(par.type.keyWord == Type.T_TEXT && par.mode == Parameter.Mode.value) {
							// Edit TEXT.COPY
							typeValue = ("RTS_ENVIRONMENT.copy((RTS_TXT)objectValue(param))");
						}
						else
							if (par.type.isArithmeticType()) typeValue = (tp + "Value(param)");
						else typeValue = ("(" + tp + ")objectValue(param)");
					}
					case Parameter.Kind.Array -> {
						typeValue = ("arrayValue(param)");
						if (par.mode == Parameter.Mode.value) typeValue = typeValue + ".COPY()";
					}
					case Parameter.Kind.Procedure -> typeValue = ("procValue(param)");
					default -> typeValue = ("(" + tp + ")param");
				}
			}
			JavaSourceFileCoder.code("case " + ( parameterList.size() - (nPar++)) + ": " + par.externalIdent + "=" + typeValue + "; break;");
		}
		JavaSourceFileCoder.code("default: throw new RTS_SimulaRuntimeError(\"Too many parameters\");");
		JavaSourceFileCoder.code("}");
		JavaSourceFileCoder.code("}");
		JavaSourceFileCoder.code("catch(ClassCastException e) { throw new RTS_SimulaRuntimeError(\"Wrong type of parameter: \"+param,e);}");
		JavaSourceFileCoder.code("return(this);");
		JavaSourceFileCoder.code("}");
		JavaSourceFileCoder.debug("// Constructor in case of Formal/Virtual Procedure Call");
		JavaSourceFileCoder.code("public " + getJavaIdentifier() + "(RTS_RTObject _SL) {");
		JavaSourceFileCoder.code("super(_SL,"+parameterList.size()+");","Expecting "+parameterList.size()+" parameters");
		JavaSourceFileCoder.code("}");
	}

	// ***********************************************************************************************
	// *** Coding Utility: codeProcedureBody -- Redefined in SwitchDeclaration
	// ***********************************************************************************************
	/// Coding Utility: codeProcedureBody. Redefined in SwitchDeclaration.
	protected void codeProcedureBody() {
		boolean duringSTM_Coding=Global.duringSTM_Coding;
		Global.duringSTM_Coding=false;
		JavaSourceFileCoder.debug("// Procedure Statements");
		JavaSourceFileCoder.code("@Override");
		JavaSourceFileCoder.code("public " + getJavaIdentifier() + " _STM() {");
		Global.duringSTM_Coding=true;
		codeSTMBody();
		JavaSourceFileCoder.code("EBLK();");
		JavaSourceFileCoder.code("return(this);");
		JavaSourceFileCoder.code("}", "End of Procedure BODY");
		Global.duringSTM_Coding=duringSTM_Coding;
	}



	// ***********************************************************************************************
	// *** ByteCoding: buildClassFile
	// ***********************************************************************************************
	@Override
	public byte[] buildClassFile() {
		labelList.setLabelIdexes();
		ClassDesc CD_ThisClass = currentClassDesc();
		if(Option.verbose) Util.println("ProcedureDeclaration.buildClassFile: "+CD_ThisClass);
		if(isPreCompiledFromFile != null) return getBytesFromFile();
		ClassHierarchy.addClassToSuperClass(CD_ThisClass, RTS.CD.RTS_PROCEDURE);
		
		int count = 5;
		while((count--) > 0) {
			try {
				if(Option.verbose)
					Util.println("ProcedureDeclaration.buildClassFile: TRY: "+CD_ThisClass);
				return tryBuildClassFile(CD_ThisClass);
			} catch(IllegalArgumentException e) {
				boolean feasibleToReTry = false;
				String msg = e.getMessage();
				if(msg.startsWith("Could not resolve class")) {
					String classID = msg.substring(24);
					ClassDesc CD = ClassHierarchy.getClassDesc(classID);
					if(CD != null) {
						ClassHierarchyResolver resolver = ClassHierarchy.getResolver();
						ClassHierarchyInfo classInfo = resolver.getClassInfo(CD);
						feasibleToReTry = classInfo != null;
					}
				}
				Util.println("ProcedureDeclaration.buildClassFile: FATAL ERROR CAUSED BY "+e+" FeasibleToReTry="+feasibleToReTry);
				if(count <= 0 || !feasibleToReTry) throw e;
			}
		}
		return null;
	}
	
	/**
	 * Try to build classFile for this ProcedureDeclaration
	 * @param CD_ThisClass this class descriptor
	 * @return class file bytes
	 */
	private byte[] tryBuildClassFile(ClassDesc CD_ThisClass) {
		byte[] bytes = ClassFile.of(ClassFile.ClassHierarchyResolverOption.of(ClassHierarchy.getResolver())).build(CD_ThisClass,
				classBuilder -> {
					classBuilder
						.with(SourceFileAttribute.of(Global.sourceFileName))
						.withFlags(ClassFile.ACC_PUBLIC + ClassFile.ACC_FINAL + ClassFile.ACC_SUPER)
						.withSuperclass(RTS.CD.RTS_PROCEDURE);
					
					// Add Fields (Return, attributes and parameters)
					
					if(type != null)
						classBuilder
							.withMethodBody("_RESULT", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"), ClassFile.ACC_PUBLIC,
								codeBuilder -> buildMethod_RESULT(codeBuilder));
					
					for (Parameter par:parameterList)
						par.buildDeclaration(classBuilder,this);
					
					if(this.hasAccumLabel())
						for (LabelDeclaration lab : labelList.getAccumLabels())
							lab.buildDeclaration(classBuilder,this);
					
					for (Declaration decl : declarationList)
						decl.buildDeclaration(classBuilder,this);
					
					if(parameterList.size() > 0)
						classBuilder
							.withMethod("setPar", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Lsimula/runtime/RTS_PROCEDURE;"), ClassFile.ACC_PUBLIC,
								codeBuilder -> buildSetPar(codeBuilder))
							.withMethod("<init>", MTD_Constructor(false), ClassFile.ACC_PUBLIC,
								codeBuilder -> buildConstructor2(codeBuilder));

					classBuilder
						.withMethod("<init>", MTD_Constructor(true), ClassFile.ACC_PUBLIC,
							codeBuilder -> buildConstructor(codeBuilder))
						.withMethodBody("_STM", MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_RTObject;"), ClassFile.ACC_PUBLIC,
							codeBuilder -> buildMethod_STM(codeBuilder));
				}
		);
		return(bytes);
	}

	// ***********************************************************************************************
	// *** ByteCoding: MTD_Constructor
	// ***********************************************************************************************
	/// Create the MethodTypeDesc for the constructor.
	/// 
	/// Example: (Lsimula/runtime/RTS_RTObject;IID)V
	/// 
	/// Also used by PrefixedBlockDeclaration.
	/// @param withParams true: create MethodTypeDesc with parameters.
	/// @return the MethodTypeDesc for the constructor
	private MethodTypeDesc MTD_Constructor(boolean withParams) {
		StringBuilder sb=new StringBuilder("(Lsimula/runtime/RTS_RTObject;");
		if(withParams) for(Parameter par:parameterList) {
			sb.append(par.type_toClassDesc().descriptorString());
		}
		sb.append(")V");
		return(MethodTypeDesc.ofDescriptor(sb.toString()));
	}

	/// ClassFile coding utility: getResultFieldRefEntry
	/// @param pool the ConstantPoolBuilder to use
	/// @return a FieldRefEntry
	public FieldRefEntry getResultFieldRefEntry(ConstantPoolBuilder pool) {
		return(pool.fieldRefEntry(RTS.CD.classDesc(this.getJavaIdentifier()), "_RESULT", type.toClassDesc()));
	}
	
	// ***********************************************************************************************
	// *** ByteCoding: edConstructorSignature
	// ***********************************************************************************************
	@Override
	public String edConstructorSignature() {
		// MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_RTObject;I)V");
		StringBuilder sb=new StringBuilder("(Lsimula/runtime/RTS_RTObject;");
		for(Parameter par:parameterList) {
			if(par.mode==Parameter.Mode.name) sb.append("Lsimula/runtime/RTS_NAME;");
			else switch(par.kind) {
				case Parameter.Kind.Array:			 sb.append("Lsimula/runtime/RTS_ARRAY;");  break;
				case Parameter.Kind.Label:           sb.append("Lsimula/runtime/RTS_LABEL;");  break;
				case Parameter.Kind.Procedure:       sb.append("Lsimula/runtime/RTS_PRCQNT;"); break;
				case Parameter.Kind.Simple: default: sb.append(par.type.toJVMType(par.kind, par.mode));
			}
		}
		sb.append(")V");
		return(sb.toString());
	}

	
	// ***********************************************************************************************
	// *** ByteCoding: buildConstructor
	// ***********************************************************************************************
	/// Generate byteCode for the Constructor.
	/// <pre>
	///     public Program'name(RTS_RTObject staticLink, par, par ...) {
	///         super(staticLink);
	/// 		   // Parameter assignment to locals
	/// 		   BBLK();
	/// 		   // Declaration Code
	/// 		   _STM();
	/// 	   }
	/// </pre>
	/// @param methodBuilder the MethodBuilder to use.
	private void buildConstructor(MethodBuilder methodBuilder) {
		methodBuilder
			.withFlags(ClassFile.ACC_PUBLIC)
			.with(SignatureAttribute.of(MethodSignature.parseFrom(edConstructorSignature())))
			.withCode(codeBuilder -> {
				ASSERT_SEMANTICS_CHECKED();
				Global.enterScope(this);
					ConstantPoolBuilder pool=codeBuilder.constantPool();
					Label begScope = codeBuilder.newLabel();
					Label endScope = codeBuilder.newLabel();
					codeBuilder
						.labelBinding(begScope)
						.localVariable(0,"this",currentClassDesc(),begScope,endScope)
						.localVariable(1,"staticLink",RTS.CD.RTS_RTObject,begScope,endScope);
		
					// Declare local parameters
					int nPar = 2;
					for(Parameter par:parameterList) {
						ClassDesc TD=par.type_toClassDesc();
						codeBuilder.localVariable(nPar++,par.getJavaIdentifier(),TD,begScope,endScope);
					}
		
					// super(staticLink);
					codeBuilder
						.aload(0)
						.aload(1)
						.invokespecial(RTS.CD.RTS_PROCEDURE,"<init>", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_RTObject;)V"));
		
					if (this.hasDeclaredLabel()) {
						// Declare local labels
						for (LabelDeclaration lab : labelList.getDeclaredLabels())
							lab.buildInitAttribute(codeBuilder);
					}
					// Add and Initialize attributes
					for (Declaration decl : declarationList) {
						decl.buildInitAttribute(codeBuilder);
					}
		
		
					// Parameter assignment to locals
					int parOfst=2;
					for(Parameter par:parameterList) {
						codeBuilder.aload(0);
						par.loadParameter(codeBuilder, parOfst++);
						if(par.type!=null && par.type.keyWord == Type.T_LONG_REAL && (par.mode != Parameter.Mode.name)) parOfst++;
						codeBuilder.putfield(par.getFieldRefEntry(pool));
					}
		
					// BBLK();
					codeBuilder.aload(0);
					RTS.invokevirtual_RTObject_BBLK(codeBuilder);
						
					// Add Declaration Code to Constructor
					for (Declaration decl : declarationList) {
						decl.buildDeclarationCode(codeBuilder);
					}
		
					// _STM();
					codeBuilder
						.aload(0)
						.invokevirtual(currentClassDesc(),"_STM", MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_RTObject;"))
						.pop();
		
					codeBuilder
						.return_()
						.labelBinding(endScope);
				Global.exitScope();
		}	);
	}

	
	// ***********************************************************************************************
	// *** ByteCoding: buildConstructor
	// ***********************************************************************************************
	/// Generate byteCode for the Constructor.
	/// <pre>
	///     public Program'name(RTS_RTObject staticLink, par, par ...) {
	///         super(staticLink);
	/// 		   // Parameter assignment to locals
	/// 		   BBLK();
	/// 		   // Declaration Code
	/// 		   _STM();
	/// 	   }
	/// 
	///    // Constructor in case of Formal/Virtual Procedure Call
	///    public adHoc000_PPP(RTS_RTObject _SL) {
	///        super(_SL,n); // Expecting n parameters
	///    }
	/// </pre>
	/// @param methodBuilder the MethodBuilder to use.
	private void buildConstructor2(MethodBuilder methodBuilder) {
		methodBuilder
			.withFlags(ClassFile.ACC_PUBLIC)
			.with(SignatureAttribute.of(MethodSignature.parseFrom("(Lsimula/runtime/RTS_RTObject;)V")))
			.withCode(codeBuilder -> {
				ASSERT_SEMANTICS_CHECKED();
				Global.enterScope(this);
					Label begScope = codeBuilder.newLabel();
					Label endScope = codeBuilder.newLabel();
					codeBuilder
						.labelBinding(begScope)
						.localVariable(0,"this",currentClassDesc(),begScope,endScope)
						.localVariable(1,"staticLink",RTS.CD.RTS_RTObject,begScope,endScope);
		
					// super(staticLink);
					codeBuilder
						.aload(0)
						.aload(1);
						
					Constant.buildIntConst(codeBuilder,parameterList.size());
					
					codeBuilder
						.invokespecial(RTS.CD.RTS_PROCEDURE,"<init>", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_RTObject;I)V"));
		
					codeBuilder
						.return_()
						.labelBinding(endScope);
				Global.exitScope();
			}	);
		}
	
	// ***********************************************************************************************
	// *** ByteCoding: buildSetPar
	// ***********************************************************************************************
	/// Generate byteCode for the Constructor.
	/// <pre>
	///     public adHoc000_R setPar(Object param) {
	///        try {
	///           switch(_nParLeft--) {
	///              case 1: p_SFD=procValue(param); break;
	///              case 2: ... ...
	///              default: throw new RTS_SimulaRuntimeError("Too many parameters");
	///           }
	///        } catch(ClassCastException e) {
	///            throw new RTS_SimulaRuntimeError("Wrong type of parameter: "+param,e);
	///        }
	///        return(this);
	///     }
	/// </pre>
	/// @param methodBuilder the MethodBuilder to use.
	@SuppressWarnings("unused")
	private void buildSetPar(MethodBuilder methodBuilder) {
		methodBuilder
			.withFlags(ClassFile.ACC_PUBLIC)
			.with(SignatureAttribute.of(MethodSignature.parseFrom("(Lsimula/runtime/RTS_RTObject;)V")))
			.withCode(codeBuilder -> {
				ASSERT_SEMANTICS_CHECKED();
				Global.enterScope(this);
					Label begScope = codeBuilder.newLabel();
					Label endScope = codeBuilder.newLabel();
					codeBuilder
						.labelBinding(begScope)
						.localVariable(0,"this",currentClassDesc(),begScope,endScope)
						.localVariable(1,"param",RTS.CD.RTS_RTObject,begScope,endScope);
		
					codeBuilder.trying(
							blockCodeBuilder -> {
								build_SWITCH(blockCodeBuilder);
							},
							catchBuilder -> catchBuilder.catching(RTS.CD.JAVA_LANG_CLASSCAST_EXCEPTION,
									blockCodeBuilder -> {
										// throw new RTS_SimulaRuntimeError("Wrong type of parameter: "+param,e);
										codeBuilder.astore(2);
										Util.buildSimulaRuntimeError("Wrong type of parameter: ", codeBuilder);
									}));
					
					codeBuilder
						.aload(0)
						.areturn()
						.labelBinding(endScope);
				Global.exitScope();
		}	);
	}
	
	/// ClassFile coding utility: Build switch
	/// @param codeBuilder the codeBuilder to use.
	private void build_SWITCH(BlockCodeBuilder codeBuilder) {
		//  switch(_nParLeft--) {
		//     case 1: p_SFD=procValue(param); break;
		//     case 2: ... ...
		//     default: throw new RTS_SimulaRuntimeError("Too many parameters");
		//  }
		int tableSize = parameterList.size();
		List<SwitchCase> tableSwitchCases = new Vector<SwitchCase>();
		for (int i = 1; i <= tableSize; i++) {
			tableSwitchCases.add(SwitchCase.of(i, codeBuilder.newLabel()));
		}
		Label defaultTarget = codeBuilder.newLabel(); // beginning of the default handler block.
		Label breakLabel = codeBuilder.newLabel(); // beginning of the default handler block.
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		
		FieldRefEntry FRE_nParLeft=RTS.FRE.PROCEDURE_nParLeft(pool);
		codeBuilder
			.aload(0)
			.dup()
			.getfield(FRE_nParLeft)
			.dup_x1()
			.iconst_1()
			.isub()
			.putfield(FRE_nParLeft)
			
			.lookupswitch(defaultTarget, tableSwitchCases);
		
		codeBuilder.labelBinding(defaultTarget);
		Util.buildSimulaRuntimeError("Too many parameters", codeBuilder);
		
		for(int i=0;i<tableSize;i++) {
			Label lab = tableSwitchCases.get(i).target();
			codeBuilder
				.labelBinding(lab)
				.aload(0);
			Parameter par = parameterList.get(tableSize-i-1);
			if (par.mode == Parameter.Mode.name) {
				codeBuilder
					.aload(1) // param
					.checkcast(RTS.CD.RTS_NAME)
					.putfield(par.getFieldRefEntry(pool))
					.goto_(breakLabel);
			} else {
				switch(par.kind) {
					case Parameter.Kind.Array -> {
						codeBuilder
							.aload(0)
							.aload(1); // param
						RTS.invokevirtual_RTS_arrayValue(codeBuilder);
						if(par.mode == Parameter.Mode.value) {
							RTS.invokevirtual_ARRAY_copy(codeBuilder);
						}
						codeBuilder
							.putfield(par.getFieldRefEntry(pool))
							.goto_(breakLabel);
					}
					
					case Parameter.Kind.Procedure -> {
						codeBuilder
							.aload(0)
							.aload(1); // param
						RTS.invokevirtual_RTS_procValue(codeBuilder);
						codeBuilder
							.putfield(par.getFieldRefEntry(pool))
							.goto_(breakLabel);
					}
					
					case Parameter.Kind.Simple -> {
	
						if (par.type.isArithmeticType()) {
							codeBuilder
								.aload(0)
								.aload(1); // param
							RTS.objectToPrimitiveType(par.type, codeBuilder);
							codeBuilder
								.putfield(par.getFieldRefEntry(pool))
								.goto_(breakLabel);
						}
						else {
							// typeValue = ("(" + tp + ")objectValue(param)");
							codeBuilder
								.aload(0)
								.aload(1); // param
							if(! RTS.objectToPrimitiveType(par.type, codeBuilder)) {
								RTS.invokevirtual_RTS_objectValue(codeBuilder);
								codeBuilder.checkcast(par.type_toClassDesc());
							}
							
							if(par.type.keyWord == Type.T_TEXT && par.mode == Parameter.Mode.value) {
								// Build TEXT.COPY
								RTS.invokestatic_ENVIRONMENT_copy(codeBuilder);
							}
							
							codeBuilder
								.putfield(par.getFieldRefEntry(pool))
								.goto_(breakLabel);
						}
					}
					default -> Util.IERR();
				}
			}
		}
		codeBuilder.labelBinding(breakLabel);
	}
		
	@Override
	public void buildDeclaration(ClassBuilder classBuilder, BlockDeclaration encloser) {
		Global.sourceLineNumber = lineNumber;
		try {
			this.createJavaClassFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void buildInitAttribute(CodeBuilder codeBuilder) {
		Global.sourceLineNumber = lineNumber;
		// NOTHING
	}

	// ***********************************************************************************************
	// *** ByteCoding: buildMethod_STM_BODY
	// ***********************************************************************************************
	/// Generate byteCode for the '_STM' method.
	/// @param codeBuilder the CodeBuilder
	@Override
	protected void build_STM_BODY(CodeBuilder codeBuilder, Label begScope, Label endScope) {
		labelContextStack.push(labelContext);
		labelContext = this;
		for (Statement stm : statements) {
			if(!(stm instanceof DummyStatement)) Util.buildLineNumber(codeBuilder,stm.lineNumber);
			stm.buildByteCode(codeBuilder);
		}
		labelContext = labelContextStack.pop();
	}

	// ***********************************************************************************************
	// *** ByteCoding: buildMethod_STM_BODY
	// ***********************************************************************************************
	/// Generate byteCode for the '_RESULT' method.
	/// @param codeBuilder the CodeBuilder
	private void buildMethod_RESULT(CodeBuilder codeBuilder) {
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		ClassDesc DC_RESULT = type.toClassDesc(declaredIn);
		FieldRefEntry FRE_RESULT=pool.fieldRefEntry(BlockDeclaration.currentClassDesc(),"_RESULT", DC_RESULT);
		codeBuilder
			.aload(0)
			.getfield(FRE_RESULT);
    	type.buildObjectValueOf(codeBuilder);
		codeBuilder.areturn();
	}


	// ***********************************************************************************************
	// *** Printing Utility: print
	// ***********************************************************************************************
	@Override
	public void print(final int indent) {
    	String spc=edIndent(indent);
		StringBuilder s = new StringBuilder(spc);
		s.append('[').append(sourceBlockLevel).append(':').append(getRTBlockLevel()).append("] ");
		s.append(declarationKind).append(' ').append(identifier);
		s.append('[').append(externalIdent).append("] ");
		s.append(Parameter.editParameterList(parameterList));
		s.append("  isProtected=").append(isProtected);
		Util.println(s.toString());
		String beg = "begin[" + edScopeChain() + ']';
		Util.println(spc + beg);
		for (Declaration decl : declarationList) decl.print(indent + 1);
		for (Statement stm : statements) stm.print(indent + 1);
		Util.println(spc + "end[" + edScopeChain() + ']');
	}
	
	@Override
	public void printTree(final int indent, final Object head) {
		verifyTree(head);
		String typeID = (type == null) ? "" : type.toString() + " ";
		String tail = (IS_SEMANTICS_CHECKED()) ? "  BL=" + getRTBlockLevel() : "";
		if(isPreCompiledFromFile != null) tail = tail + " From: " + isPreCompiledFromFile;
		IO.println(edTreeIndent(indent) + typeID + "PROCEDURE " + identifier + '[' + externalIdent + "]" + tail);
		if (labelList != null) labelList.printTree(indent + 1, this);
		for (Parameter p : parameterList) p.printTree(indent + 1, this);
		printDeclarationList(indent + 1);
		printStatementList(indent + 1);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(identifier).append("[externalIdent=").append(externalIdent).append("] Kind=")
		.append(declarationKind).append(", QUAL=").append(this.getClass().getSimpleName()).append(", HashCode=").append(hashCode());
		if (isProtected != null) {
			s.append(", Protected by ").append(isProtected.identifier);
			s.append(" defined in ");
			s.append((isProtected.definedIn != null) ? isProtected.definedIn.identifier : "MISSING");
		}
		return (s.toString());
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	public ProcedureDeclaration() {	super(null); }

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("BEGIN Write ProcedureDeclaration: "+identifier);
		oupt.writeKind(declarationKind); // Mark: This is a ProcedureDeclaration
		oupt.writeString(identifier);
		oupt.writeShort(OBJECT_SEQU);
		
		// *** SyntaxClass
		oupt.writeShort(lineNumber);

		// *** Declaration
		oupt.writeString(externalIdent);
		oupt.writeType(type);

		// *** DeclarationScope
		oupt.writeString(sourceFileName);
		oupt.writeBoolean(hasLocalClasses);
		
		// *** ProcedurekDeclaration
		oupt.writeObjectList(parameterList);

		Util.TRACE_OUTPUT("END Write ProcedureDeclaration: "+identifier);
	}

	/// Read and return an object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the object read from the stream.
	/// @throws IOException if something went wrong.
	@SuppressWarnings("unchecked")
	public static ProcedureDeclaration readObject(AttributeInputStream inpt) throws IOException {
		String identifier = inpt.readString();
		ProcedureDeclaration pro = new ProcedureDeclaration(identifier, ObjectKind.Procedure);
		pro.OBJECT_SEQU = inpt.readSEQU(pro);

		// *** SyntaxClass
		pro.lineNumber = inpt.readShort();

		// *** Declaration
		pro.externalIdent = inpt.readString();
		pro.type = inpt.readType();

		// *** DeclarationScope
		pro.sourceFileName = inpt.readString();
		pro.hasLocalClasses = inpt.readBoolean();
		
		// *** ProcedurekDeclaration
		pro.parameterList = (ObjectList<Parameter>) inpt.readObjectList();

		pro.isPreCompiledFromFile = inpt.jarFileName;
		Util.TRACE_INPUT("END Read ProcedureDeclaration: Procedure "+identifier+", Declared in: "+pro.declaredIn);
		Global.setScope(pro.declaredIn);
		return(pro);
	}

}
