/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.declaration;

import java.io.IOException;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.HiddenSpecification;
import simula.compiler.syntaxClass.ProcedureSpecification;
import simula.compiler.syntaxClass.ProtectedSpecification;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Util;

/// Virtual Quantities.
/// <pre>
/// Simula Standard: 5.5.3 Virtual quantities
/// 
///    virtual-part  =  VIRTUAL  :  virtual-spec  ;  {  virtual-spec  ;  }
///    
///    virtual-spec  =  virtual-specifier  identifier-list
///                  |  PROCEDURE  procedure-identifier  procedure-specification
///                  
///      procedure-specification =  IS  procedure-declaration
///        
///   	virtual-specifier = LABEL | SWITCH |  [ type ] PROCEDURE
///   
///    	identifier-list  =  identifier  { , identifier }
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/VirtualSpecification.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class VirtualSpecification extends Declaration {
	// String identifier; // Inherited
	// String externalIdent; // Inherited
	// Type type; // Inherited: Procedure's type if any

	/// Virtual Kind.
	public class Kind {
		/** Virtual procedure */ public static final int Procedure = 1;
		/** Virtual label */	 public static final int Label     = 2;
		/** Virtual switch */	 public static final int Switch    = 3;
		/** Default Constructor. NOT USED */ private Kind() {} 
	}

	/// Virtual kind.
	public int kind;

	/// The prefix level of the class with this virtual specification.
	public int prefixLevel;

	/// The procedure specification if present.
	public ProcedureSpecification procedureSpec; // From: IS ProcedureSpecification
	
	/// Indicates if this virtual has a default match.
	/// 
	/// Set during doChecking.
	boolean hasDefaultMatch;

	/// VirtualSpecification.
	/// @param identifier virtual identifier
	/// @param type the virtual's type
	/// @param kind the vitual Kind
	/// @param prefixLevel the prefix level of the class with this virtual specification
	/// @param procedureSpec the ProcedureSpecification or null if not present
	VirtualSpecification(final String identifier, final Type type, final int kind, final int prefixLevel, final ProcedureSpecification procedureSpec) {
		super(identifier);
		this.declarationKind = ObjectKind.VirtualSpecification;
		this.externalIdent = identifier;
		this.type = type;
		this.kind = kind;
		this.prefixLevel = prefixLevel;
		this.procedureSpec = procedureSpec;
	}

	/// Parse a virtual-part and put it into the given class.
	/// <pre>
	/// Syntax:
	/// 
	///      virtual-part = VIRTUAL: virtual-spec ; { virtual-spec ; }
	///         virtual-spec
	///             = virtual-specifier identifier-list
	///             | PROCEDURE procedure-identifier  procedure-specification
	///             
	///                virtual-Specifier = [ type ] PROCEDURE | LABEL | SWITCH
	///                
	///                identifier-list = identifier { , identifier }
	///                
	///                procedure-specification = IS procedure-declaration
	/// </pre>
	/// Precondition: VIRTUAL  is already read.
	/// @param cls the ClassDeclaration
	static void expectVirtualPart(final ClassDeclaration cls) {
		Parse.expect(KeyWord.COLON);
		LOOP: while (true) {
			Type type;
			if (Parse.accept(KeyWord.SWITCH)) {
				expectIdentifierList(cls, Type.Label, Kind.Switch);
			} else if (Parse.accept(KeyWord.LABEL)) {
				expectIdentifierList(cls, Type.Label, Kind.Label);
			} else {
				type = Parse.acceptType();
				if (!Parse.accept(KeyWord.PROCEDURE))
					break LOOP;

				String identifier = Parse.expectIdentifier();
				ProcedureSpecification procedureSpec = null;
				if (Parse.accept(KeyWord.IS)) {
					if(type != null) Util.error("An IS-specified virtual procedure can have its type only after IS.");
					type = Parse.acceptType();
					Parse.expect(KeyWord.PROCEDURE);
					procedureSpec = ProcedureSpecification.expectProcedureSpecification(type);						
					cls.virtualSpecList
							.add(new VirtualSpecification(identifier, type, Kind.Procedure, cls.prefixLevel(), procedureSpec));
				} else {
					cls.virtualSpecList.add(new VirtualSpecification(identifier, type, Kind.Procedure, cls.prefixLevel(), null));
					if (Parse.accept(KeyWord.COMMA))
						expectIdentifierList(cls, type, Kind.Procedure);
					else
						Parse.expect(KeyWord.SEMICOLON);
				}
			}
		}
		if(cls.virtualSpecList.size()==0) Util.error("Missing virtual specifier after VIRTUAL:");
	}

	/// Parse a virtual identifier list.
	/// <pre>
	/// Syntax:
	/// 
	///        identifier-list = identifier { , identifier
	/// </pre>
	/// @param cls the ClassDeclaration
	/// @param type the specifiers type
	/// @param kind the specifiers kind
	private static void expectIdentifierList(final ClassDeclaration cls, final Type type, final int kind) {
		do {
			String identifier = Parse.expectIdentifier();
			cls.virtualSpecList.add(new VirtualSpecification(identifier, type, kind, cls.prefixLevel(), null));
		} while (Parse.accept(KeyWord.COMMA));
		Parse.expect(KeyWord.SEMICOLON);
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())
			return;
		Global.sourceLineNumber = lineNumber;
		if (procedureSpec != null)
			procedureSpec.doChecking(this.declaredIn);
		
		// Label and switch attributes are implicit specified 'protected'
		if (kind == Kind.Label || kind == Kind.Switch)
			((ClassDeclaration) declaredIn).protectedList
					.add(new ProtectedSpecification((ClassDeclaration) declaredIn, identifier));
		SET_SEMANTICS_CHECKED();
	}

	/// Returns the virtual identifier used i Java code.
	/// @return the virtual identifier used i Java code
	public String getVirtualIdentifier() {
		return(getSimpleVirtualIdentifier() + "()");
	}

	/// Returns the virtual identifier used in JVM code.
	/// @return the virtual identifier used in JVM code
	public String getSimpleVirtualIdentifier() {
		return (getJavaIdentifier() + '_' + prefixLevel);
	}

	// ***********************************************************************************************
	// *** Utility: getVirtualSpecification
	// ***********************************************************************************************
	/// Get virtual specification.
	/// @param decl the declaration to search for
	/// @return a VirtualSpecification or null
	public static VirtualSpecification getVirtualSpecification(final Declaration decl) {
		if (!(decl.declaredIn instanceof ClassDeclaration))
			return (null);
		ClassDeclaration scope = (ClassDeclaration) decl.declaredIn;
		VirtualSpecification virtSpec = scope.searchVirtualSpecList(decl.identifier);
		
		
		if(scope.prefix.equals("ERRMSG")) {
			IO.println("VirtualSpecification.getVirtualSpecification: decl="+decl);
			IO.println("VirtualSpecification.getVirtualSpecification: scope="+scope+"   PREFIX="+scope.prefix);
			
		}
		
		if (virtSpec != null)
			return (virtSpec);
		scope = scope.getPrefixClass();

		SEARCH: while (scope != null) {
			HiddenSpecification hdn = scope.searchHiddenList(decl.identifier);
			if (hdn != null) {
				scope = hdn.getScopeBehindHidden();
				continue SEARCH;
			}
			virtSpec = scope.searchVirtualSpecList(decl.identifier);
			if (virtSpec != null)
				return (virtSpec);
			scope = scope.getPrefixClass();
		}
		return (null);
	}

	@Override
	public void doJavaCoding() {
		ASSERT_SEMANTICS_CHECKED();
		String matchCode = "{ throw new RTS_SimulaRuntimeError(\"No Virtual Match: " + identifier + "\"); }";
		String qnt = (kind == Kind.Label) ? "RTS_LABEL " : "RTS_PRCQNT ";
		JavaSourceFileCoder.code("public " + qnt + getVirtualIdentifier() + matchCode);
	}

	/// Build the default virtual match method RTS_LABEL or RTS_PRCQNT.
	/// @param classBuilder the classBuilder to use.
	public void buildMethod(ClassBuilder classBuilder) {
	    String ident=getSimpleVirtualIdentifier();
		String qnt = (kind == Kind.Label) ? "RTS_LABEL;" : "RTS_PRCQNT;";
		classBuilder
			.withMethodBody(ident, MethodTypeDesc.ofDescriptor("()Lsimula/runtime/"+qnt), ClassFile.ACC_PUBLIC,
				codeBuilder -> Util.buildSimulaRuntimeError("No Virtual Match: " + identifier, codeBuilder));
	}

	/// Build call virtual method.
	/// @param owner the class owning the virtual.
	/// @param codeBuilder the codeBuilder to use.
	public void buildCallMethod(ClassDesc owner, CodeBuilder codeBuilder) {
	    String name=getSimpleVirtualIdentifier();
		String qnt = (kind == Kind.Label) ? "RTS_LABEL;" : "RTS_PRCQNT;";
		
        // 4: getfield      #13  // Field simulaTestPrograms/adHoc000.x:LsimulaTestPrograms/adHoc000_A;
        // 7: invokevirtual #19  // Method simulaTestPrograms/adHoc000_A.vP_0:()Lsimula/runtime/RTS_PRCQNT;
		codeBuilder.invokevirtual(owner, name, MethodTypeDesc.ofDescriptor("()Lsimula/runtime/" + qnt));
	}

	@Override
	public void printTree(int indent, final Object head) {
		IO.println(SyntaxClass.edIndent(indent)+this.getClass().getSimpleName()+"    "+this);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (type != null)
			s.append(type).append(' ');
		if (kind == Kind.Procedure)
			s.append("PROCEDURE ");
		s.append(identifier);
		s.append("[Specified in ").append(declaredIn.identifier).append(']');
		if (procedureSpec != null)
			s.append('=').append(procedureSpec);
		return (s.toString());
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private VirtualSpecification() {
		super(null);
		this.declarationKind = ObjectKind.VirtualSpecification;
	}
	
	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("VirtualSpec: " + type + ' ' + identifier + ' ' + kind);
		oupt.writeKind(declarationKind);
		oupt.writeShort(OBJECT_SEQU);
		// *** VirtualSpecification
		oupt.writeString(identifier);
		oupt.writeString(externalIdent);
		oupt.writeType(type);
		oupt.writeShort(kind);
		oupt.writeShort(prefixLevel);
		ProcedureSpecification.writeProcedureSpec(procedureSpec,oupt);
	}

	/// Read and return an object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the object read from the stream.
	/// @throws IOException if something went wrong.
	public static SyntaxClass readObject(AttributeInputStream inpt) throws IOException {
		VirtualSpecification virt = new VirtualSpecification();
		virt.OBJECT_SEQU = inpt.readSEQU(virt);
		// *** VirtualSpecification
		virt.identifier = inpt.readString();
		virt.externalIdent = inpt.readString();
		virt.type = inpt.readType();
		virt.kind = inpt.readShort();
		virt.prefixLevel = inpt.readShort();
		virt.procedureSpec = ProcedureSpecification.readProcedureSpec(inpt);
		Util.TRACE_INPUT("VirtualSpec: " + virt);
		return(virt);
	}

}
