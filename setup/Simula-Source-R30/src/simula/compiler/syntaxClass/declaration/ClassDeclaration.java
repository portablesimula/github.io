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
import java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Iterator;
import java.util.Vector;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.HiddenSpecification;
import simula.compiler.syntaxClass.ProtectedSpecification;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.statement.DummyStatement;
import simula.compiler.syntaxClass.statement.InnerStatement;
import simula.compiler.syntaxClass.statement.Statement;
import simula.compiler.utilities.ClassHierarchy;
import simula.compiler.utilities.DeclarationList;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.LabelList;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.ObjectList;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Simula Class Declaration.
/// 
/// <pre>
/// 
/// Simula Standard: 5.5 Class declaration
/// 
/// class-declaration = [ prefix ] main-part
/// 
///   prefix = class-identifier
/// 
///   main-part = CLASS class-identifier
///               [ formal-parameter-part ; [ value-part ] specification-part ] ;
///               [ protection-part ; ]
///               [ virtual-part ; ]
///               class-body
///   
///      class-identifier = identifier
/// 
///      formal-parameter-part = "(" FormalParameter { , FormalParameter } ")"
///            FormalParameter = identifier
/// 
///      value-part = VALUE identifier-list
/// 
///      specification-part = class-parameter-specifier  identifier-list ; { class-parameter-specifier  identifier-list ; }
///               class-parameter-specifier = Type | [Type] ARRAY 
/// 
///      protection-part = protection-specification { ; protection-specification }
///               protection-specification = HIDDEN identifier-list | HIDDEN PROTECTED identifier-list
///                                        | PROTECTED identifier-list | PROTECTED HIDDEN identifier-list
/// 
///      virtual-part = VIRTUAL: virtual-spec ; { virtual-spec ; }
///         virtual-spec
///             = virtual-specifier identifier-list
///             | PROCEDURE procedure-identifier  procedure-specification
///             
///                virtual-Specifier = [ type ] PROCEDURE | LABEL | SWITCH
///                
///                procedure-specification = IS procedure-declaration
/// 
///      
///      class-body = statement | split-body
///      
///         split-body = initial-operations inner-part final-operations
///         
///            initial-operations = ( BEGIN | block-head ; ) { statement ; }
///         
///            inner-part = [ label : ] INNER ;
/// 
///            final-operations
///               = END
///               | ; statement { ; statement } END
/// 
/// </pre>
/// 
/// 
/// This class is prefix to StandardClass and PrefixedBlockDeclaration.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/ClassDeclaration.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public class ClassDeclaration extends BlockDeclaration {

	/// The parameter list.
	ObjectList<Parameter> parameterList = new ObjectList<Parameter>();

	/// The virtual spec list.
	protected ObjectList<VirtualSpecification> virtualSpecList = new ObjectList<VirtualSpecification>();

	/// The virtual match list.
	protected Vector<VirtualMatch> virtualMatchList = new Vector<VirtualMatch>();

	/// The protected list.
	ObjectList<ProtectedSpecification> protectedList = new ObjectList<ProtectedSpecification>();

	/// The hidden list.
	public ObjectList<HiddenSpecification> hiddenList = new ObjectList<HiddenSpecification>();

	/// Possible statements before inner.
	/// If this is non-null then 'statements' contains the statements after inner
	public ObjectList<Statement> statements1; // Statement code before inner

	/// Class Prefix in case of a SubClass or Prefixed Block.
	public String prefix;

	/// Class Prefix in case of a SubClass or Prefixed Block.
	/// Set by coChecking
	public ClassDeclaration prefixClass;

	/// Set true when attribute procedure 'detach' is used in/on this class.
	public boolean detachUsed = false;

	// ***********************************************************************************************
	// *** CONSTRUCTOR
	// ***********************************************************************************************
	/// Create a new ClassDeclaration.
	/// @param identifier the given identifier
	protected ClassDeclaration(String identifier) {
		super(identifier);
		this.declarationKind = ObjectKind.Class;
	}

	// ***********************************************************************************************
	// *** Parsing: expectClassDeclaration
	// ***********************************************************************************************
	/// Parse Class Declaration.
	/// <pre>
	/// 
	/// Syntax:
	/// 
	/// class-declaration = [ prefix ] main-part
	/// 
	///   prefix = class-identifier
	/// 
	///   main-part = CLASS class-identifier
	///               [ formal-parameter-part ; [ value-part ] specification-part ] ;
	///               [ protection-part ; ]
	///               [ virtual-part ; ]
	///               class-body
	/// 
	/// </pre>
	/// @param prefix class identifier
	/// @return the resulting ClassDeclaration
	public static ClassDeclaration expectClassDeclaration(final String prefix) {
		ClassDeclaration cls = new ClassDeclaration(null);
		cls.sourceFileName = Global.sourceFileName;
		cls.lineNumber = Parse.prevToken.lineNumber;
		cls.prefix = prefix;
		cls.declaredIn.hasLocalClasses = true;
		if (cls.prefix == null)
			cls.prefix = StandardClass.CLASS.identifier;
		cls.modifyIdentifier(Parse.expectIdentifier());
		if (Parse.accept(KeyWord.BEGPAR)) {
			expectFormalParameterPart(cls.parameterList);
			Parse.expect(KeyWord.SEMICOLON);
			acceptValuePart(cls.parameterList);
			acceptParameterSpecificationPart(cls.parameterList);
		} else
			Parse.expect(KeyWord.SEMICOLON);

		acceptProtectionPart(cls);
		if (Parse.accept(KeyWord.VIRTUAL))
			VirtualSpecification.expectVirtualPart(cls);
		expectClassBody(cls);
		
		cls.lastLineNumber = Global.sourceLineNumber;
		cls.type = Type.Ref(cls.identifier);
		if (Option.internal.TRACE_PARSE)
			Parse.TRACE("Line " + cls.lineNumber + ": ClassDeclaration: " + cls);
		Global.setScope(cls.declaredIn);
		return (cls);
	}
	
	// ***********************************************************************************************
	// *** PARSING: acceptValuePart
	// ***********************************************************************************************
	/// Parse utility: Accept value part and set matching parameter's mode.
	/// <pre>
	/// Syntax:
	///              VALUE identifier-list ;
	/// </pre>
	/// @param pList Parameter list
	private static void acceptValuePart(final Vector<Parameter> pList) {
		if (Parse.accept(KeyWord.VALUE)) {
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
				parameter.setMode(Parameter.Mode.value);
			} while (Parse.accept(KeyWord.COMMA));
			Parse.expect(KeyWord.SEMICOLON);
		}
	}

	// ***********************************************************************************************
	// *** PARSING: acceptParameterSpecificationPart
	// ***********************************************************************************************
	/// Parse Utility: Accept Class Parameter specification-part updating Parameter's type and kind.
	/// <pre>
	/// Syntax:
	/// 
	///     specification-part
    ///           = class-parameter-specifier identifier-list { ; class-parameter-specifier identifier-list }
	///     
	///        class-parameter-specifier = Type | [Type] ARRAY
	/// </pre>
	/// @param pList the parameter list
	private static void acceptParameterSpecificationPart(final Vector<Parameter> pList) {
		if (Option.internal.TRACE_PARSE)
			Parse.TRACE("Parse ParameterSpecifications");
		while (true) {
			Type type;
			int kind = Parameter.Kind.Simple;
			type = Parse.acceptType();
			if (Parse.accept(KeyWord.ARRAY)) {
				if (type == null) {
					// See Simula Standard 5.2 -
					// If no type is given the type real is understood.
					type = Type.Real;
				}
				kind = Parameter.Kind.Array;
			}
			if (type == null)
				return;
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
				parameter.setTypeAndKind(type, kind);
			} while (Parse.accept(KeyWord.COMMA));

			Parse.expect(KeyWord.SEMICOLON);
		}
	}


	// ***********************************************************************************************
	// *** PARSING: acceptProtectionPart
	// ***********************************************************************************************
	/// Parse Utility: Accept protection-part updating Hidden and Protected lists.
	/// <pre>
	/// Syntax:
	/// 
	///      protection-part = protection-specification { ; protection-specification }
	///      
	///           protection-specification
	///                = HIDDEN identifier-list
	///                | HIDDEN PROTECTED identifier-list
	///                | PROTECTED identifier-list
	///                | PROTECTED HIDDEN identifier-list
	/// </pre>
	/// @param cls the ClassDeclaration
	private static void acceptProtectionPart(ClassDeclaration cls) {
		while (true) {
			if (Parse.accept(KeyWord.HIDDEN)) {
				if (Parse.accept(KeyWord.PROTECTED))
					expectHiddenProtectedList(cls, true, true);
				else
					expectHiddenProtectedList(cls, true, false);
			} else if (Parse.accept(KeyWord.PROTECTED)) {
				if (Parse.accept(KeyWord.HIDDEN))
					expectHiddenProtectedList(cls, true, true);
				else
					expectHiddenProtectedList(cls, false, true);
			} else
				break;
		}	
	}
	
	/// Parse Utility: Expect Hidden Protected list.
	/// <pre>
	/// Syntax:
	/// 
	///      identifier-list
	/// </pre>
	/// @param cls the ClassDeclaration
	/// @param hidden if true, update the hidden list
	/// @param prtected if true, update the protected list
	private static void expectHiddenProtectedList(final ClassDeclaration cls, final boolean hidden, final boolean prtected) {
		do {
			String identifier = Parse.expectIdentifier();
			if (hidden)
				cls.hiddenList.add(new HiddenSpecification(cls, identifier));
			if (prtected)
				cls.protectedList.add(new ProtectedSpecification(cls, identifier));
		} while (Parse.accept(KeyWord.COMMA));
		Parse.expect(KeyWord.SEMICOLON);
	}

	// ***********************************************************************************************
	// *** PARSING: expectClassBody
	// ***********************************************************************************************
	/// Parse Utility: Expect class-body.
	/// In case of a split-body, updating the class's declaration and statement lists.
	/// <pre>
	/// Syntax:
	///                
	///      class-body = statement | split-body
	///      
	///         split-body = initial-operations inner-part final-operations
	///         
	///            initial-operations = ( BEGIN | block-head ; ) { statement ; }
	///         
	///            inner-part = [ label : ] INNER ;
	/// 
	///            final-operations
	///               = END
	///               | ; statement { ; statement } END
	/// </pre>
	/// @param cls the ClassDeclaration
	private static void expectClassBody(ClassDeclaration cls) {
		if (Parse.accept(KeyWord.BEGIN)) {
			Statement stm;
			if (Option.internal.TRACE_PARSE)
				Parse.TRACE("Parse Block");
			while (Declaration.acceptDeclaration(cls)) {
				Parse.accept(KeyWord.SEMICOLON);
			}
			boolean seen = false;
			while (!Parse.accept(KeyWord.END)) {
				stm = Statement.expectStatement();
				if (stm != null)
					cls.statements.add(stm);
				if (Parse.accept(KeyWord.INNER)) {
					if (seen)
						Util.error("Max one INNER per Block");
					else
						cls.statements.add(new InnerStatement(Parse.currentToken.lineNumber));
					seen = true;
				}
			}
			if (!seen)
				cls.statements.add(new InnerStatement(Parse.currentToken.lineNumber)); // Implicit INNER
		}
		else {
			if(Parse.currentToken.keyWord != KeyWord.SEMICOLON)
				cls.statements.add(Statement.expectStatement());
			cls.statements.add(new InnerStatement(Parse.currentToken.lineNumber)); // Implicit INNER
		}
	}

	// ***********************************************************************************************
	// *** Utility: isSubClassOf
	// ***********************************************************************************************
	/// Checks if this class is a subclass of the 'other' class.
	/// 
	/// Consider the class definitions:
	/// 
	/// <pre>
	///  
	///      Class A ......;
	///    A Class B ......;
	///    B Class C ......;
	/// </pre>
	/// 
	/// Then Class B is a subclass of Class A, While Class C is subclass of both B and A.
	/// @param other the other ClassDeclaration
	/// @return Boolean true iff this class is a subclass of the 'other' class.
	public boolean isSubClassOf(final ClassDeclaration other) {
		ClassDeclaration prefixClass = getPrefixClass();
		if (prefixClass != null)
			do {
				if (other == prefixClass)
					return (true);
			} while ((prefixClass = prefixClass.getPrefixClass()) != null);
		return (false);
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
		if(Option.internal.TRACE_CHECKER)
			Util.TRACE("BEGIN ClassDeclaration("+this.identifier+").doChecking");
		
		if (hasRealPrefix()) {
			prefixClass = getPrefixClass();
			prefixClass.doChecking();
			if (prefixClass.declarationKind != ObjectKind.StandardClass) {
				if (sourceBlockLevel != prefixClass.sourceBlockLevel)
					Util.warning("Subclass on a deeper block level not allowed.");
			}
		}
		LabelList.accumLabelList(this);
		
		if(type != null) type.doChecking(declaredIn);
		int prfx = prefixLevel();
		for (Parameter par : this.parameterList)
			par.setExternalIdentifier(prfx);
		for (Declaration par : new ClassParameterIterator())
			par.doChecking();
		for (VirtualSpecification vrt : virtualSpecList)
			vrt.doChecking();
		for (Declaration dcl : declarationList)
			dcl.doChecking();
		if(statements1 != null) 
			for (Statement stm : statements1) 
				stm.doChecking();  		
		for (Statement stm : statements)
			stm.doChecking();
		checkProtectedList();
		checkHiddenList();
		Global.exitScope();
		if(Option.internal.TRACE_CHECKER)
			Util.TRACE("END ClassDeclaration("+this.identifier+").doChecking");
		SET_SEMANTICS_CHECKED();
	}

	// ***********************************************************************************************
	// *** Utility: checkHiddenList
	// ***********************************************************************************************
	/// Perform sematic checking of the Hidden list.
	private void checkHiddenList() {
		for (HiddenSpecification hdn : hiddenList)
			hdn.doChecking();
	}

	// ***********************************************************************************************
	// *** Utility: checkProtectedList
	// ***********************************************************************************************
	/// Perform sematic checking of the Protected list.
	private void checkProtectedList() {
		for (ProtectedSpecification pct : protectedList) {
			pct.doChecking();
		}
	}

	// ***********************************************************************************************
	// *** Utility: searchVirtualSpecList -- - Search VirtualSpec-list for 'ident'
	// ***********************************************************************************************
	/// Utility: Search VirtualSpec-list for 'ident'
	/// @param ident argument
	/// @return a VirtualSpecification when it was found, otherwise null
	public VirtualSpecification searchVirtualSpecList(final String ident) {
		for (VirtualSpecification virtual : virtualSpecList) {
			if (Util.equals(ident, virtual.identifier))
				return (virtual);
		}
		return (null);
	}

	// ***********************************************************************************************
	// *** Utility: prefixLevel
	// ***********************************************************************************************
	/// Returns the prefix level.
	/// @return the prefix level
	@Override
	public int prefixLevel() {
		if (!hasRealPrefix())
			return (0);
		ClassDeclaration prfx = getPrefixClass();
		if (prfx != null)
			return (prfx.prefixLevel() + 1);
		return (-1);
	}

	// ***********************************************************************************************
	// *** Utility: findLocalAttribute
	// ***********************************************************************************************
	/// Utility: Search for an attribute named 'ident'
	/// @param ident argument
	/// @return a ProcedureDeclaration when it was found, otherwise null
	public Declaration findLocalAttribute(final String ident) {
		if (Option.internal.TRACE_FIND_MEANING > 0)
			Util.println("BEGIN Checking Class for " + ident + " ================================== " + identifier + " ==================================");
		for (Parameter parameter : parameterList) {
			if (Option.internal.TRACE_FIND_MEANING > 1)
				Util.println("Checking Parameter " + parameter);
			if (Util.equals(ident, parameter.identifier))
				return (parameter);
		}
		for (Declaration declaration : declarationList) {
			if (Option.internal.TRACE_FIND_MEANING > 1)
				Util.println("Checking Local " + declaration);
			if (Util.equals(ident, declaration.identifier))
				return (declaration);
		}
		if(labelList != null) for (LabelDeclaration label : labelList.getDeclaredLabels()) {
			if (Option.internal.TRACE_FIND_MEANING > 1)
				Util.println("Checking Label " + label);
			if (Util.equals(ident, label.identifier))
				return (label);
		}
		for (VirtualMatch match : virtualMatchList) {
			if (Option.internal.TRACE_FIND_MEANING > 1)
				Util.println("Checking Match " + match);
			if (Util.equals(ident, match.identifier))
				return (match);
		}
		for (VirtualSpecification virtual : virtualSpecList) {
			if (Option.internal.TRACE_FIND_MEANING > 1)
				Util.println("Checking Virtual " + virtual);
			if (Util.equals(ident, virtual.identifier))
				return (virtual);
		}
		if (Option.internal.TRACE_FIND_MEANING > 0)
			Util.println("ENDOF Checking Class for " + ident + " ================================== " + identifier + " ==================================");
		return (null);
	}

	// ***********************************************************************************************
	// *** Utility: findLocalProcedure
	// ***********************************************************************************************
	/// Utility: Search Declaration-list for a procedure named 'ident'
	/// @param ident argument
	/// @return a ProcedureDeclaration when it was found, otherwise null
	ProcedureDeclaration findLocalProcedure(final String ident) {
		for (Declaration decl : declarationList)
			if (Util.equals(ident, decl.identifier)) {
				if (decl instanceof ProcedureDeclaration proc)
					 return (proc);
				else return (null);
			}
		return (null);
	}

	// ***********************************************************************************************
	// *** Utility: findRemoteAttributeMeaning
	// ***********************************************************************************************
	/// Find Remote Attribute's Meaning.
	/// @param ident attribute identifier
	/// @return the resulting Meaning
	public Meaning findRemoteAttributeMeaning(final String ident) {
		boolean behindProtected = false;
		ClassDeclaration scope = this;
		Declaration decl = scope.findLocalAttribute(ident);
		if (decl != null) {
			boolean prtected = decl.isProtected != null;
			VirtualSpecification virtSpec = VirtualSpecification.getVirtualSpecification(decl);
			if (virtSpec != null && virtSpec.isProtected != null)
				prtected = true;
			if (!prtected)
				return (new Meaning(decl, this, scope, behindProtected));
		}
		SEARCH: while (scope != null) {
			HiddenSpecification hdn = scope.searchHiddenList(ident);
			if (hdn != null) {
				scope = hdn.getScopeBehindHidden();
				behindProtected = true;
				continue SEARCH;
			}
			Declaration decl2 = scope.findLocalAttribute(ident);
			if (decl2 != null) {
				boolean prtected = decl2.isProtected != null;
				if (withinScope(scope))
					prtected = false;
				if (!prtected)
					return (new Meaning(decl2, this, scope, behindProtected));
				behindProtected = true;
			}
			scope = scope.getPrefixClass();
		}
		return (null);
	}

	// ***********************************************************************************************
	// *** Utility: searchProtectedList - Search Protected-list for 'ident'
	// ***********************************************************************************************
	/// Utility: Search Protected-list for 'ident'
	/// @param ident argument
	/// @return a ProtectedSpecification when it was found, otherwise null
	public ProtectedSpecification searchProtectedList(final String ident) {
		for (ProtectedSpecification pct : protectedList)
			if (Util.equals(ident, pct.identifier))
				return (pct);
		return (null);
	}

	// ***********************************************************************************************
	// *** Utility: withinScope -- Used by findRemoteAttributeMeaning
	// ***********************************************************************************************
	/// Checks if the other scope is this scope or any of the prefixes.
	/// @param otherScope the other scope
	/// @return true if the other scope is this scope or any of the prefixes
	private static boolean withinScope(final DeclarationScope otherScope) {
		DeclarationScope scope = Global.getCurrentScope();
		while (scope != null) {
			if (scope == otherScope)
				return (true);
			if (scope instanceof ClassDeclaration cls) {
				ClassDeclaration prfx = cls.getPrefixClass();
				while (prfx != null) {
					if (prfx == otherScope)
						return (true);
					prfx = prfx.getPrefixClass();
				}
			}
			scope = scope.declaredIn;
		}
		return (false);
	}

	// ***********************************************************************************************
	// *** Utility: findVisibleAttributeMeaning
	// ***********************************************************************************************
	@Override
	public Meaning findVisibleAttributeMeaning(final String ident) {
		if (Option.internal.TRACE_FIND_MEANING > 0)
			Util.println("BEGIN Checking Class for " + ident + " ================================== " + identifier + " ==================================");
		boolean searchBehindHidden = false;
		ClassDeclaration scope = this;
		Declaration decl = scope.findLocalAttribute(ident);
		if (decl != null) {
			Meaning meaning = new Meaning(decl, this, scope, searchBehindHidden);
			return (meaning);
		}
		scope = scope.getPrefixClass();
		SEARCH: while (scope != null) {
			HiddenSpecification hdn = scope.searchHiddenList(ident);
			if (hdn != null) {
				scope = hdn.getScopeBehindHidden();
				searchBehindHidden = true;
				continue SEARCH;
			}
			decl = scope.findLocalAttribute(ident);
			if (decl != null) {
				Meaning meaning = new Meaning(decl, this, scope, searchBehindHidden);
				return (meaning);
			}
			scope = scope.getPrefixClass();
		}
		if (Option.internal.TRACE_FIND_MEANING > 0)
			Util.println("ENDOF Checking Class for " + ident + " ================================== " + identifier + " ==================================");
		return (null);
	}

	// ***********************************************************************************************
	// *** Utility: searchHiddenList -- Search Hidden-list for 'ident'
	// ***********************************************************************************************
	/// Utility: Search Hidden-list for 'ident'
	/// @param ident argument
	/// @return a HiddenSpecification when it was found, otherwise null
	HiddenSpecification searchHiddenList(final String ident) {
		for (HiddenSpecification hdn : hiddenList)
			if (Util.equals(ident, hdn.identifier))
				return (hdn);
		return (null);
	}

	// ***********************************************************************************************
	// *** Utility: getPrefixClass
	// ***********************************************************************************************
	/// Returns the prefix ClassDeclaration or null.
	/// @return the prefix ClassDeclaration or null
	public ClassDeclaration getPrefixClass() {
		if (prefix == null)
			return (null);
		if(prefixClass != null) return(prefixClass);
		
		Meaning meaning = declaredIn.findMeaning(prefix);
		if (meaning == null)
			Util.error("Undefined prefix: " + prefix);
		Declaration decl = meaning.declaredAs;
		if (decl == this) {
			Util.error("Class prefix chain loops: " + identifier);
		}
		if (decl instanceof ClassDeclaration cls) {
			prefixClass = cls;
			return (cls);
		}
		Util.error("Prefix " + prefix + " is not a Class but " + decl.getClass().getSimpleName()
				+ " Declared in " + this.sourceFileName + " at line " + decl.lineNumber);
		printStaticChain("",0);
		return (null);
	}

	// ***********************************************************************************************
	// *** Coding Utility: hasRealPrefix
	// ***********************************************************************************************
	/// Check if this class has a real prefix.
	/// @return true if this class has a real prefix, otherwise false.
	boolean hasRealPrefix() {
		String prfx = prefix;
		boolean res = false;
		if (prfx != null) {
			res = true;
			if (Util.equals(prfx, "CLASS"))
				res = false;
		}
		return res;
	}

	// ***********************************************************************************************
	// *** Coding: isDetachUsed -- If the 'detach' attribute is used
	// ***********************************************************************************************
	//
	// COMMENT FROM Stein: Ta utgangspunkt i hvilke klasser man har kalt "detach" i, altså
	// kvalifikasjonen av de X som er brukt i "X.detach". Men da må man jo også holde greie på
	// hvilke slike som har forekommet i eksterne "moduler" (som f.eks. SIMULATION), uten at det
	// burde være problematisk.
	// ***********************************************************************************************
	/// Returns true if detach is called in/on this class.
	/// @return true if detach is called in/on this class
	public boolean isDetachUsed() {
		// TRAVERSER PREFIX LOOKING FOR (detachUsed==true)
		if (this.detachUsed)
			return (true);
		if (this instanceof ClassDeclaration) {
			ClassDeclaration prfx = ((ClassDeclaration) this).getPrefixClass();
			if (prfx != null)
				return (prfx.isDetachUsed());
		}
		return (false);
	}

	// ***********************************************************************************************
	// *** Utility: ClassParameterIterator - Iterates through prefix-chain
	// ***********************************************************************************************
	/// Utility: ClassParameterIterator - Iterates through prefix-chain.
	public final class ClassParameterIterator implements Iterator<Parameter>, Iterable<Parameter> {
		/// The prefix Iterator
		private Iterator<Parameter> prefixIterator;
		/// The local Iterator
		private Iterator<Parameter> localIterator;

		/// Constructor
		public ClassParameterIterator() {
			ClassDeclaration prefix = getPrefixClass();
			if (prefix != null)
				prefixIterator = prefix.parameterIterator();
			localIterator = parameterList.iterator();
		}

		@Override
		public boolean hasNext() {
			if (prefixIterator != null) {
				if (prefixIterator.hasNext())
					return (true);
				prefixIterator = null;
			}
			return (localIterator.hasNext());
		}

		@Override
		public Parameter next() {
			if (!hasNext())
				return (null);
			if (prefixIterator != null)
				return (prefixIterator.next());
			return (localIterator.next());
		}

		@Override
		public Iterator<Parameter> iterator() {
			return (new ClassParameterIterator());
		}
	}

	/// Iterates through all class parameters.
	/// @return a ClassParameterIterator
	public Iterator<Parameter> parameterIterator() {
		return (new ClassParameterIterator());
	}

	// ***********************************************************************************************
	// *** Coding: doJavaCoding
	// ***********************************************************************************************
	@Override
	public void doJavaCoding() {
		ASSERT_SEMANTICS_CHECKED();
		if (this.isPreCompiledFromFile != null) {
			if(Option.verbose) IO.println("Skip  doJavaCoding: "+this.identifier+" -- It is read from "+isPreCompiledFromFile);	
			return;
		}
		Global.sourceLineNumber = lineNumber;
		JavaSourceFileCoder javaCoder = new JavaSourceFileCoder(this);
		Global.enterScope(this);
			labelList.setLabelIdexes();
			JavaSourceFileCoder.code("@SuppressWarnings(\"unchecked\")");
			String line = "public class " + getJavaIdentifier();
			line = line + " extends " + getPrefixClass().getJavaIdentifier();
			JavaSourceFileCoder.code(line + " {");
			JavaSourceFileCoder.debug("// ClassDeclaration: Kind=" + declarationKind + ", BlockLevel=" + getRTBlockLevel()
					+ ", PrefixLevel=" + prefixLevel() + ", firstLine=" + lineNumber + ", lastLine=" + lastLineNumber
					+ ", hasLocalClasses=" + ((hasLocalClasses) ? "true" : "false") + ", System="
					+ ((isQPSystemBlock()) ? "true" : "false") + ", detachUsed=" + ((detachUsed) ? "true" : "false"));
			if (isQPSystemBlock())
				JavaSourceFileCoder.code("public boolean isQPSystemBlock() { return(true); }");
			if (isDetachUsed())
				JavaSourceFileCoder.code("public boolean isDetachUsed() { return(true); }");
			JavaSourceFileCoder.debug("// Declare parameters as attributes");
			for (Parameter par : parameterList) {
				String tp = par.toJavaType();
				JavaSourceFileCoder.code("public " + tp + ' ' + par.externalIdent + ';');
			}
			if(this.hasAccumLabel()) {
				JavaSourceFileCoder.debug("// Declare local labels");
				for (LabelDeclaration lab : labelList.getAccumLabels())
					lab.declareLocalLabel(this);
			}
			JavaSourceFileCoder.debug("// Declare locals as attributes");
			for (Declaration decl : declarationList)
				decl.doJavaCoding();
	
			for (VirtualSpecification virtual : virtualSpecList) {
				if (!virtual.hasDefaultMatch)
					virtual.doJavaCoding();
			}
			for (VirtualMatch match : virtualMatchList)
				match.doJavaCoding();
			doCodeConstructor();
			codeClassStatements();
			javaCoder.codeProgramInfo();
			JavaSourceFileCoder.code("}", "End of Class");
		Global.exitScope();
		javaCoder.closeJavaOutput();
	}

	// ***********************************************************************************************
	// *** Coding Utility: doCodeConstructor
	// ***********************************************************************************************
	/// Java Coding Utility: Code the constructor.
	private void doCodeConstructor() {
		JavaSourceFileCoder.debug("// Normal Constructor");
		JavaSourceFileCoder.code("public " + getJavaIdentifier() + edFormalParameterList());
		if (prefix != null) {
			ClassDeclaration prefixClass = this.getPrefixClass();
			JavaSourceFileCoder.code("super" + prefixClass.edCompleteParameterList());
		} else
			JavaSourceFileCoder.code("super(staticLink);");
		JavaSourceFileCoder.debug("// Parameter assignment to locals");
		for (Parameter par : parameterList)
			JavaSourceFileCoder.code("this." + par.externalIdent + " = s" + par.externalIdent + ';');

		if (!hasRealPrefix())
			JavaSourceFileCoder.code("BBLK(); // Iff no prefix");

		JavaSourceFileCoder.debug("// Declaration Code");
		for (Declaration decl : declarationList)
			decl.doDeclarationCoding();
		JavaSourceFileCoder.code("}");
	}

	// ***********************************************************************************************
	// *** Coding Utility: edFormalParameterList
	// ***********************************************************************************************
	/// Edit the formal parameter list
	/// 
	/// Also used by subclass PrefixedBlockDeclaration.
	/// @return the resulting Java code
	protected String edFormalParameterList() {
		// Accumulates through prefix-chain when class
		StringBuilder s = new StringBuilder();
		s.append('(');
		s.append("RTS_RTObject staticLink");
		for (Declaration par : new ClassParameterIterator()) {
			// Iterates through prefix-chain
			s.append(',');
			s.append(((Parameter) par).toJavaType());
			s.append(' ');
			s.append('s').append(par.externalIdent); // s to indicate Specified Parameter
		}
		s.append(") {");
		return (s.toString());
	}

	// ***********************************************************************************************
	// *** Java Coding Utility: codeStatements
	// ***********************************************************************************************
	@Override
	public void codeStatements() {
		codeStatementsBeforeInner();
		codeStatementsAfterInner();
	}

	// ***********************************************************************************************
	// *** Coding Utility: codeStatementsBeforeInner
	// ***********************************************************************************************
	/// Java coding utility: codeStatementsBeforeInner
	private void codeStatementsBeforeInner() {
		if (hasRealPrefix()) {
			ClassDeclaration prfx = this.getPrefixClass();
			if (prfx != null) prfx.codeStatementsBeforeInner();
		}
		if(statements1 != null) for (Statement stm : statements1) stm.doJavaCoding();
		JavaSourceFileCoder.code("// BEGIN "+identifier+" INNER PART");
	}

	// ***********************************************************************************************
	// *** Coding Utility: codeStatementsAfterInner
	// ***********************************************************************************************
	/// Java coding utility: codeStatementsAfterInner
	private void codeStatementsAfterInner() {
		JavaSourceFileCoder.code("// ENDOF "+identifier+" INNER PART");
		for (Statement stm : statements) stm.doJavaCoding();
		if (hasRealPrefix()) {
			ClassDeclaration prfx = this.getPrefixClass();
			if (prfx != null) prfx.codeStatementsAfterInner();
		}
	}

	// ***********************************************************************************************
	// *** Coding Utility: codeClassStatements
	// ***********************************************************************************************
	/// Java coding utility: Code class statements.
	protected void codeClassStatements() {
		boolean duringSTM_Coding = Global.duringSTM_Coding;
		Global.duringSTM_Coding = false;
		JavaSourceFileCoder.debug("// Class Statements");
		JavaSourceFileCoder.code("@Override");
		JavaSourceFileCoder.code("public " + getJavaIdentifier() + " _STM() {");
		Global.duringSTM_Coding = true;
		codeSTMBody();
		JavaSourceFileCoder.code("EBLK();");
		JavaSourceFileCoder.code("return(this);");
		JavaSourceFileCoder.code("}", "End of Class Statements");
		Global.duringSTM_Coding = duringSTM_Coding;
	}

	// ***********************************************************************************************
	// *** Coding Utility: edCompleteParameterList
	// ***********************************************************************************************
	/// Coding Utility: Edit the complete parameter list including all prefixes.
	/// @return the resulting Java code
	protected String edCompleteParameterList() {
		StringBuilder s = new StringBuilder();
		s.append("(staticLink");
		for (Parameter par : new ClassParameterIterator()) // Iterates through prefix-chain
			s.append(",s").append(par.externalIdent); // s to indicate Specified Parameter
		s.append(");");
		return (s.toString());
	}



	// ***********************************************************************************************
	// *** ClassFile coding Utility: superClassDesc
	// ***********************************************************************************************
	/// Get super class ClassDesc.
	/// @return super class ClassDesc.
	public ClassDesc superClassDesc() {
		if(hasRealPrefix())
			return getPrefixClass().getClassDesc();
		return RTS.CD.RTS_CLASS;
	}
	
	/// Indicates if this class is loaded.
	private boolean isLoaded;

	/// Defined in DeclarationScope - Redefined in ClassDeclaration
    /// @throws IOException if something went wrong.
	@Override
    protected void buildAndLoadOrAddClassFile() throws IOException {
		if(this.isLoaded) return;
		if(this instanceof StandardClass) return;
		if(hasRealPrefix()) {
			ClassDeclaration prefix = this.getPrefixClass();
			if(!prefix.isLoaded) {
				prefix.buildAndLoadOrAddClassFile();
			}
		}
		
		if(isPreCompiledFromFile != null) return;
			
    	byte[] bytes = doBuildClassFile();
    	loadOrAddClassFile(bytes);
    	this.isLoaded = true;
    }

	
	// ***********************************************************************************************
	// *** ByteCoding: buildClassFile
	// ***********************************************************************************************
	@Override
	public byte[] buildClassFile() {
		labelList.setLabelIdexes();
		ClassDesc CD_ThisClass = currentClassDesc();
		ClassDesc CD_SuperClass = superClassDesc();
		if(isPreCompiledFromFile != null) return getBytesFromFile();
		ClassHierarchy.addClassToSuperClass(CD_ThisClass, CD_SuperClass);
		
		// Mail from: Chen Liang <chen.l.liang@oracle.com> on October 18, 2024.
		// Thanks for sharing Oystein. The problem is that ClassHierarchyResolver.of(Collection, Map) takes the immediate
		// values in the argument and ignores subsequent updates, which is how most ClassFile APIs work, unfortunately.
		//
		// My solution: It was possible to implement a re-try loop solution.
		//              But it did not turn out very pretty.
		int count = 5;
		while((count--) > 0) {
			try {
				if(Option.verbose)
					Util.println("ClassDeclaration.buildClassFile: TRY: "+CD_ThisClass+" extends "+CD_SuperClass);
				return tryBuildClassFile(CD_ThisClass, CD_SuperClass);
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
				Util.println("ClassDeclaration.buildClassFile: FATAL ERROR CAUSED BY "+e+" FeasibleToReTry="+feasibleToReTry);
				if(count <= 0 || !feasibleToReTry) throw e;
			}
		}
		return null;
	}

	/**
	 * Try to build classFile for this ClassDeclaration
	 * @param CD_ThisClass this class descriptor
	 * @param CD_SuperClass super class descriprot
	 * @return class file bytes
	 */
	private byte[] tryBuildClassFile(ClassDesc CD_ThisClass, ClassDesc CD_SuperClass) {
		byte[] bytes = ClassFile.of(ClassFile.ClassHierarchyResolverOption.of(ClassHierarchy.getResolver())).build(CD_ThisClass,
				classBuilder -> {
					classBuilder
						.with(SourceFileAttribute.of(Global.sourceFileName))
						.withFlags(ClassFile.ACC_PUBLIC + ClassFile.ACC_SUPER)
						.withSuperclass(CD_SuperClass);
	
					if(this.hasAccumLabel())
						for (LabelDeclaration lab : labelList.getAccumLabels())
							lab.buildDeclaration(classBuilder,this);
	
					for (Declaration decl : declarationList)
						decl.buildDeclaration(classBuilder,this);
					
					for (Parameter par : parameterList)
						par.buildDeclaration(classBuilder,this);
					
					for (VirtualSpecification virtual : virtualSpecList) 
						if (!virtual.hasDefaultMatch) 
							virtual.buildMethod(classBuilder);
					
					for (VirtualMatch match : virtualMatchList)
						match.buildMethod(classBuilder);
	
					classBuilder
						.withMethodBody("<init>", MethodTypeDesc.ofDescriptor(edConstructorSignature()), ClassFile.ACC_PUBLIC,
							codeBuilder -> buildConstructor(codeBuilder))
						.withMethodBody("_STM", MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_RTObject;"), ClassFile.ACC_PUBLIC,
							codeBuilder -> buildMethod_STM(codeBuilder));
					
					if (isDetachUsed()) 
						classBuilder
							.withMethodBody("isDetachUsed", MethodTypeDesc.ofDescriptor("()Z"), ClassFile.ACC_PUBLIC,
								codeBuilder -> buildIsMethodDetachUsed(codeBuilder));
				}
		);
		if(Option.verbose)
			Util.println("ClassDeclaration.buildClassFile: DONE: "+CD_ThisClass+" extends "+CD_SuperClass);
		return(bytes);
	}

	// ***********************************************************************************************
	// *** ByteCoding: edConstructorSignature
	// ***********************************************************************************************
	/// Edit the constructor signature.
	/// 
	/// Example: (Lsimula/runtime/RTS_RTObject;IID)V
	/// 
	/// Also used by PrefixedBlockDeclaration.
	/// @return the MethodTypeDesc for the constructor
	@Override
	public String edConstructorSignature() {
		StringBuilder sb = new StringBuilder("(Lsimula/runtime/RTS_RTObject;");
		Iterator<Parameter> parameterIterator = parameterIterator();
		while(parameterIterator.hasNext()) {
			Parameter par = parameterIterator.next();
			sb.append(par.type.toJVMType(par.kind,par.mode));
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
	/// Also used by PrefixedBlockDeclaration
	/// @param codeBuilder the CodeBuilder
	protected void buildConstructor(CodeBuilder codeBuilder) {
		ASSERT_SEMANTICS_CHECKED();
		Global.enterScope(this);
			ConstantPoolBuilder pool=codeBuilder.constantPool();
		
			Label begScope = codeBuilder.newLabel();
			Label endScope = codeBuilder.newLabel();
			MethodTypeDesc MTD_Super;
			if(hasRealPrefix()) {
				ClassDeclaration prefixClass = this.getPrefixClass();
				String pfxSignatur=prefixClass.edConstructorSignature();
				MTD_Super = MethodTypeDesc.ofDescriptor(pfxSignatur);
			} else {
				MTD_Super = MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_RTObject;)V");
			}
			codeBuilder
				.labelBinding(begScope)
				.localVariable(0,"this",currentClassDesc(),begScope,endScope)
				.localVariable(1,"staticLink",RTS.CD.RTS_RTObject,begScope,endScope);
			Iterator<Parameter> parameterIterator = this.parameterIterator();
			int npar=2;
			while(parameterIterator.hasNext()) {
				Parameter par = parameterIterator.next();
				codeBuilder
					.localVariable(npar++,par.externalIdent,par.type.toClassDesc(par.declaredIn),begScope,endScope);
			}

			// super(staticLink);
			codeBuilder
				.aload(0)
				.aload(1);
			// Load parameters
			int parOfst=2;
			if(hasRealPrefix()) {
				ClassDeclaration prefixClass = this.getPrefixClass();
				Iterator<Parameter> parIterator = prefixClass.parameterIterator();
				while(parIterator.hasNext()) {
					Parameter par = parIterator.next();
					par.loadParameter(codeBuilder, parOfst++);
					if(par.type.keyWord == Type.T_LONG_REAL) parOfst++;
				}
			}
			codeBuilder
				.invokespecial(this.superClassDesc(),"<init>", MTD_Super);

			if(hasDeclaredLabel()) // Declare local labels
				for (LabelDeclaration lab : labelList.getDeclaredLabels())
					lab.buildInitAttribute(codeBuilder);
			
			// Add and Initialize attributes
			for (Declaration decl : declarationList)
				decl.buildInitAttribute(codeBuilder);

			// Parameter assignment to locals
			for(Parameter par:parameterList) {
				codeBuilder.aload(0);
				par.loadParameter(codeBuilder, parOfst++);
				if(par.type.keyWord == Type.T_LONG_REAL) parOfst++;
				codeBuilder.putfield(par.getFieldRefEntry(pool));
			}

			// BBLK(); // Iff no prefix
			if (!hasRealPrefix()) {
				codeBuilder.aload(0);
				RTS.invokevirtual_RTObject_BBLK(codeBuilder);
			}
			
			// Add Declaration Code to Constructor
			for (Declaration decl : declarationList)
				decl.buildDeclarationCode(codeBuilder);

			codeBuilder
				.return_()
				.labelBinding(endScope);
		Global.exitScope();
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
	}

	// ***********************************************************************************************
	// *** ByteCoding: buildMethod_STM
	// ***********************************************************************************************
	/// Generate byteCode for the '_STM' method.
	/// @param codeBuilder the CodeBuilder
	@Override
	protected void build_STM_BODY(CodeBuilder codeBuilder, Label begScope, Label endScope) {
		int nStat = this.statements.size();
		if(statements1 != null) nStat = nStat + this.statements1.size();
		ClassDeclaration prefix = this.getPrefixClass();
		while(prefix != null) {
			nStat = prefix.statements.size();
			if(prefix.statements1 != null) nStat = nStat + prefix.statements1.size();
			prefix = prefix.getPrefixClass();
		}
		clearLabelList();
		labelContextStack.push(labelContext);
		labelContext = this;
		if(this.getPrefixClass() == StandardClass.CatchingErrors) {	
			if(this instanceof PrefixedBlockDeclaration)
				buildMethod_CatchingErrors_TRY_CATCH(codeBuilder, begScope, endScope);
			else {
				Util.error("It is not allowed to declare a subclass of StandardClass CatchingErrors");
				buildStatementsBeforeInner(codeBuilder);
				buildStatementsAfterInner(codeBuilder);
			}
		} else {
			buildStatementsBeforeInner(codeBuilder);
			buildStatementsAfterInner(codeBuilder);
		}
		labelContext = labelContextStack.pop();

		codeBuilder.aload(0);
		RTS.invokevirtual_RTObject_EBLK(codeBuilder);
		codeBuilder.aload(0).areturn();
	}
	
	/// Clear the LabelList.
	private void clearLabelList() {
		if(labelList != null) {
			if(labelList.accumLabelSize() > 0)
			for(LabelDeclaration lab:labelList.getAccumLabels()) lab.isBinded = false;
		}
		if(prefixClass != null) prefixClass.clearLabelList();
	}


	// ***********************************************************************************************
	// *** ByteCoding: buildMethod_CatchingErrors_TRY_CATCH
	// ***********************************************************************************************
	/// Generate byteCode for the 'CatchingErrors_TRY_CATCH' method.
	/// @param codeBuilder the CodeBuilder
	/// @param begScope label
	/// @param endScope label
	@SuppressWarnings("unused")
	private void buildMethod_CatchingErrors_TRY_CATCH(CodeBuilder codeBuilder, Label begScope, Label endScope) {
		codeBuilder.trying(
			tryCodeBuilder -> {
				buildStatementsBeforeInner(tryCodeBuilder);
				buildStatementsAfterInner(tryCodeBuilder);
			},
			catchBuilder -> catchBuilder.catching(RTS.CD.JAVA_LANG_RUNTIME_EXCEPTION,
				catchCodeBuilder -> {
					ConstantPoolBuilder pool = codeBuilder.constantPool();
					int local_EXEPTN = BlockDeclaration.currentBlock.allocateLocalVariable(Type.Ref);
					codeBuilder
						.localVariable(local_EXEPTN,"exception",RTS.CD.JAVA_LANG_RUNTIME_EXCEPTION,begScope,endScope)
						.astore(local_EXEPTN)  // The caught exception will be on top of the operand stack when the catch block is entered.
						.aload(0)
						.putstatic(RTS.FRE.RTObject_CUR(pool))
						.aload(0)
						.aload(local_EXEPTN)
						.aload(0)
						.invokevirtual(currentClassDesc(), "onError_0", MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_PRCQNT;"));
					RTS.invokevirtual_CatchingErrors_onError(codeBuilder);
				}));
	}

	// ***********************************************************************************************
	// *** Coding Utility: buildStatementsBeforeInner
	// ***********************************************************************************************
	/// ClassFile coding utility: buildStatementsBeforeInner
	/// @param codeBuilder the codeBuilder to use.
	private void buildStatementsBeforeInner(CodeBuilder codeBuilder) {
		if (hasRealPrefix()) {
			ClassDeclaration prfx = this.getPrefixClass();
			if (prfx != null) prfx.buildStatementsBeforeInner(codeBuilder);
		}
		if(statements1 != null) for (Statement stm : statements1) {
			if(!(stm instanceof DummyStatement)) Util.buildLineNumber(codeBuilder,stm.lineNumber);
			stm.buildByteCode(codeBuilder);
		}
	}

	// ***********************************************************************************************
	// *** Coding Utility: buildStatementsAfterInner
	// ***********************************************************************************************
	/// ClassFile coding utility: buildStatementsAfterInner
	/// @param codeBuilder the codeBuilder to use.
	private void buildStatementsAfterInner(CodeBuilder codeBuilder) {
		for (Statement stm : statements){
			if(!(stm instanceof DummyStatement)) Util.buildLineNumber(codeBuilder,stm.lineNumber);
			stm.buildByteCode(codeBuilder);
		}
		if (hasRealPrefix()) {
			ClassDeclaration prfx = this.getPrefixClass();
			if (prfx != null) prfx.buildStatementsAfterInner(codeBuilder);
		}
	}


	// ***********************************************************************************************
	// *** Printing Utility: print
	// ***********************************************************************************************
	@Override
	public void print(final int indent) {
		String spc = edIndent(indent);
		StringBuilder s = new StringBuilder(spc);
		s.append('[').append(sourceBlockLevel).append(':').append(getRTBlockLevel()).append("] ");
		if (prefix != null)
			s.append(prefix).append(' ');
		s.append(declarationKind).append(' ').append(identifier);
		s.append('[').append(externalIdent).append("] ");
		s.append(Parameter.editParameterList(parameterList));
		Util.println(s.toString());
		if (!virtualSpecList.isEmpty())
			Util.println(spc + "    VIRTUAL-SPEC" + virtualSpecList);
		if (!virtualMatchList.isEmpty())
			Util.println(spc + "    VIRTUAL-MATCH" + virtualMatchList);
		if (!hiddenList.isEmpty())
			Util.println(spc + "    HIDDEN" + hiddenList);
		if (!protectedList.isEmpty())
			Util.println(spc + "    PROTECTED" + protectedList);
		String beg = "begin[" + edScopeChain() + ']';
		Util.println(spc + beg);
		for (Declaration decl : declarationList)
			decl.print(indent + 1);
		for (Statement stm : statements)
			stm.print(indent + 1);
		Util.println(spc + "end[" + edScopeChain() + ']');
	}
	
	@Override
	public void printTree(final int indent, final Object head) {
		verifyTree(head);
		String tail = (IS_SEMANTICS_CHECKED()) ? "  BL=" + getRTBlockLevel() : "";
		if(isPreCompiledFromFile != null) tail = tail + " From: " + isPreCompiledFromFile;
		String prfx = (prefix==null) ? "" : "  extends " + prefix;
		String declIn = " declaredIn " + this.declaredIn.identifier;
		IO.println(edTreeIndent(indent) + "CLASS " + identifier + tail + "  PrefixLevel=" + prefixLevel() + prfx + declIn);
		if(labelList != null) labelList.printTree(indent+1,this);
		for(Parameter p:parameterList) p.printTree(indent+1,this);
		if (!virtualSpecList.isEmpty())
			for( VirtualSpecification v:virtualSpecList) v.printTree(indent+1,this);
		if (!virtualMatchList.isEmpty())
			for( VirtualMatch m:virtualMatchList) m.printTree(indent+1,this);
		if (!hiddenList.isEmpty())
			for( HiddenSpecification h:hiddenList) h.printTree(indent+1,this);
		if (!protectedList.isEmpty())
			for( ProtectedSpecification p:protectedList) p.printTree(indent+1,this);
		printDeclarationList(indent+1);
		if(Option.internal.PRINT_SYNTAX_TREE > 2)
			for(Statement s:statements1) s.printTree(indent+1,this);
		printStatementList(indent+1);
	}

	@Override
	public String toString() {
		return ("" + identifier + '[' + externalIdent + "] DeclarationKind=" + ObjectKind.edit(declarationKind));
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************

	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("BEGIN Write ClassDeclaration: " + identifier + ", Declared in: " + declaredIn);
		oupt.writeKind(declarationKind); // Mark: This is a ClassDeclaration
		oupt.writeString(identifier);
		oupt.writeShort(OBJECT_SEQU);
		
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		
		// *** Declaration
		//oupt.writeString(identifier);
		oupt.writeString(externalIdent);
		oupt.writeType(type);// Declaration
		
		// *** DeclarationScope
		oupt.writeString(sourceFileName);
		oupt.writeBoolean(hasLocalClasses);
		LabelList.writeLabelList(labelList, oupt);
		DeclarationList decls = prep(declarationList);
		decls.writeObject(oupt);

		// *** BlockDeclaration
		oupt.writeBoolean(isMainModule);
		oupt.writeObjectList(statements);
		
		// *** ClassDeclaration
		oupt.writeString(prefix);
		oupt.writeBoolean(detachUsed);
		oupt.writeObjectList(parameterList);
		oupt.writeObjectList(virtualSpecList);
		oupt.writeObjectList(hiddenList);
		oupt.writeObjectList(protectedList);
		oupt.writeObjectList(statements1);
		Util.TRACE_OUTPUT("END Write ClassDeclaration: " + identifier);
	}

	/// Read and return a ClassDeclaration object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the object read from the stream.
	/// @throws IOException if something went wrong.
	@SuppressWarnings("unchecked")
	public static ClassDeclaration readObject(AttributeInputStream inpt) throws IOException {
		String identifier = (String) inpt.readString();
		ClassDeclaration cls = new ClassDeclaration(identifier);
		Util.TRACE_INPUT("BEGIN Read ClassDeclaration: " + identifier + ", Declared in: " + cls.declaredIn);
		cls.declarationKind = ObjectKind.Class;
		cls.OBJECT_SEQU = inpt.readSEQU(cls);
		
		// *** SyntaxClass
		cls.lineNumber = inpt.readShort();

		// *** Declaration
		//identifier = inpt.readString();
		cls.externalIdent = inpt.readString();
		cls.type = inpt.readType();

		// *** DeclarationScope
		cls.sourceFileName = inpt.readString();
		cls.hasLocalClasses = inpt.readBoolean();
		cls.labelList = LabelList.readLabelList(inpt);
		cls.declarationList = DeclarationList.readObject(inpt);

		// *** BlockDeclaration
		cls.isMainModule = inpt.readBoolean();
		cls.statements = (ObjectList<Statement>) inpt.readObjectList();
		
		// *** ClassDeclaration
		cls.prefix = inpt.readString();
		cls.detachUsed = inpt.readBoolean();
		cls.parameterList = (ObjectList<Parameter>) inpt.readObjectList();
		cls.virtualSpecList = (ObjectList<VirtualSpecification>) inpt.readObjectList();
		cls.hiddenList = (ObjectList<HiddenSpecification>) inpt.readObjectList();
		cls.protectedList = (ObjectList<ProtectedSpecification>) inpt.readObjectList();
		cls.statements1 = (ObjectList<Statement>) inpt.readObjectList();

		cls.isPreCompiledFromFile = inpt.jarFileName;
		Util.TRACE_INPUT("END Read ClassDeclaration: " + identifier + ", Declared in: " + cls.declaredIn);
		Global.setScope(cls.declaredIn);
		return(cls);
	}
	
}
