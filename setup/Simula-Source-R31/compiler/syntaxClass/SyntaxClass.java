/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.syntaxClass.declaration.Declaration;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.Util;

/// The class SyntaxClass.
/// 
/// The Simula syntax is formally defined in the Simula Standard.
/// Some non-terminal symbols give rise to a Java class with almost the same name.
/// They are all subclasses of the class SyntaxClass.
/// The subclass hierarchy of the Syntax class is described below
/// 
/// <pre>
///            SyntaxClass
///               HiddenSpecification 
///               ProtectedSpecification 
///               Type 
///                  OverLoad
///               Declaration
///                  ArrayDeclaration 
///                  DeclarationScope
///                     BlockDeclaration
///                        ClassDeclaration 
///                           PrefixedBlockDeclaration
///                           StandardClass
///                        MaybeBlockDeclaration
///                        ProcedureDeclaration 
///                           StandardProcedure
///                           SwitchDeclaration
///                     ConnectionBlock 
///                  ExternalDeclaration
///                  Parameter 
///                  SimpleVariableDeclaration 
///                     LabelDeclaration 
///                  UndefinedDeclaration 
///                  VirtualMatch 
///                  VirtualSpecification 
///               Statement
///                  ActivationStatement
///                  BlockStatement
///                  ConditionalStatement
///                  ConnectionStatement
///                  DummyStatement
///                  ForStatement
///                  GotoStatement
///                  InnerStatement
///                  LabeledStatement
///                  ProgramModule
///                  StandaloneExpression
///                  SwitchStatement
///                  WhileStatement
///               Expression
///                  ArithmeticExpression
///                  AssignmentOperation
///                  BooleanExpression
///                  ConditionalExpression
///                  Constant 
///                  LocalObject
///                  ObjectGenerator
///                  ObjectRelation
///                  QualifiedObject
///                  RelationalOperation
///                  RemoteVariable
///                  TextExpression
///                  TypeConversion
///                  UnaryOperation
///                  VariableExpression          
/// </pre>
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/SyntaxClass.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public abstract class SyntaxClass {
	/// Controls semantic checking.
	/// 
	/// Set true when the method doChecking() has been completed.
	protected boolean CHECKED = false;

	/// Object sequence number used by Attribute File I/O to fixup object references.
	/// 
	/// During Attribute File Input it is index to the Object Reference Table.
	/// See: Global.Object_SEQU
	public int OBJECT_SEQU;
	
	/// The source line number
	public int lineNumber;
	
	/// Set source line number.
	protected void setLineNumber() {
		Global.sourceLineNumber = lineNumber;
	}

	/// Create a new SyntaxClass.
	protected SyntaxClass() {
		lineNumber = Global.sourceLineNumber;
	}

	/// Perform semantic checking.
	/// 
	/// This must be redefined in every subclass.
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())
			return;
		Global.sourceLineNumber = lineNumber;
		Util.IERR("The method 'doChecking' needs a redefinition in "+this.getClass().getSimpleName());
	}

	/// Set semantic checked.
	/// 
	/// Should be called from all doChecking methods to signal that semantic checking is done.
	protected void SET_SEMANTICS_CHECKED() {
		CHECKED = true;
	}

	/// Returns true if semantic checking is done.
	/// 
	/// @return true if semantic checking is done
	public boolean IS_SEMANTICS_CHECKED() {
		return (CHECKED);
	}

	/// Assert that semantic checking done.
	protected void ASSERT_SEMANTICS_CHECKED() {
		if (!CHECKED) {
			IO.println("FATAL error - THE Semantic checker not called: " + this.getClass().getName() + ", " + this);
			System.exit(-1);
		}
		if (this instanceof Declaration decl) {
			if (decl.externalIdent == null) {
				Thread.dumpStack();
				Util.error("External Identifier is not set -- "+this);
			}
		}
	}

	/// Output possible declaration Java code.
	public void doDeclarationCoding() {}

	/// Output Java code.
	public void doJavaCoding() {
		Global.sourceLineNumber = lineNumber;
		JavaSourceFileCoder.code(toJavaCode());
	}

	/// Generate Java code for this Syntax Class.
	/// 
	/// @return Java code
	public String toJavaCode() {
		return (toString());
	}

	/// Build Java ClassFile ByteCode.
	/// @param codeBuilder the codeBuilder to use.
	public void buildByteCode(CodeBuilder codeBuilder) {
		Util.IERR("Method buildByteCode need a redefinition in "+this.getClass().getSimpleName()
				+"\n\n            MAYBE: Use buildEvaluation(boolean destination,CodeBuilder codeBuilder)\n");
	}

	/// Utility print method.
	/// 
	/// @param indent number of spaces leading the line
	public void print(final int indent) {
		Util.println(edIndent(indent) + this);
	}

	/// Utility print syntax tree method.
	/// 
	/// @param indent number of spaces leading the lines
	/// @param head the head of the tree.
	public void printTree(final int indent, final Object head) {
		Util.IERR("Method printTree need a redefinition in "+this.getClass().getSimpleName());
	}

	/// Utility: Returns a number of blanks followed by qualification of this
	/// 
	/// @param indent the number of blanks requested
	/// @return the resulting string
	public String edTreeIndent(final int indent) {
		int i = indent;
		String s = "";
		while ((i--) > 0)
			s = s + "    ";
		return (s+"Line "+this.lineNumber+": "+this.getClass().getSimpleName()+"    ");
	}

	/// Utility: Returns a number of blanks.
	/// 
	/// @param indent the number of blanks requested
	/// @return a number of blanks.
	public static String edIndent(final int indent) {
		int i = indent;
		String s = "";
		while ((i--) > 0)
			s = s + "    ";
		return (s);
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************

	/// Write a SyntaxClass object to a AttributeOutputStream.
	/// @param oupt the AttributeOutputStream to write to.
	/// @throws IOException if something went wrong.
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.IERR("Method 'writeObject' needs a redefinition in "+this.getClass().getSimpleName());
	}

	/// Read and return a SyntaxClass object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the object read from the stream.
	/// @throws IOException if something went wrong.
	public static SyntaxClass readObject(AttributeInputStream inpt) throws IOException {
		Util.IERR("Method 'readObject' needs a redefiniton");
		return(null);
	}

}
