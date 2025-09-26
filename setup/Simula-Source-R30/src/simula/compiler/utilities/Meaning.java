/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.utilities;

import java.lang.classfile.CodeBuilder;
import simula.compiler.syntaxClass.declaration.ConnectionBlock;
import simula.compiler.syntaxClass.declaration.Declaration;
import simula.compiler.syntaxClass.declaration.DeclarationScope;
import simula.compiler.syntaxClass.declaration.ProcedureDeclaration;
import simula.compiler.syntaxClass.declaration.SimpleVariableDeclaration;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.syntaxClass.expression.VariableExpression;

/// Utility class Meaning.
/// 
/// Holding the semantic meaning of an identifier.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/Meaning.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class Meaning {
	/// True if it was found behind invisible
	public boolean foundBehindInvisible; // Behind hidden/protected
	
	/// The corresponding declaration
	public Declaration declaredAs;
	
	/// Where it was declared
	public DeclarationScope declaredIn; // Search started here (modified in ConnectionBlock)
	
	/// Where it was found
	public DeclarationScope foundIn; // Search ended here

	/// Create a new Meaning.
	/// @param declaredAs the corresponding declaration
	/// @param declaredIn where it was declared
	/// @param foundIn where it was found
	/// @param foundBehindInvisible true if it was found behind invisible
	public Meaning(final Declaration declaredAs,final DeclarationScope declaredIn,final DeclarationScope foundIn,final boolean foundBehindInvisible) {
		this.declaredAs = declaredAs;
		this.declaredIn = declaredIn;
		this.foundIn = foundIn;
		this.foundBehindInvisible = foundBehindInvisible;
	}

	/// Create a new Meaning.
	/// @param declaredAs the corresponding declaration
	/// @param declaredIn where it was declared
	public Meaning(final Declaration declaredAs,final DeclarationScope declaredIn) {
		this(declaredAs, declaredIn,null,false);
	}

	/// Returns the constant element or null.
	/// @return the constant element or null
	public Expression getConstant() {
		if(declaredAs instanceof SimpleVariableDeclaration simple) {
			if(simple.isConstant()) return(simple.constantElement);
		}
		return(null);
	}

	/// Returns true if it was declared in a ConnectionBlock.
	/// @return true if it was declared in a ConnectionBlock
	public boolean isConnected() {
		return (declaredIn instanceof ConnectionBlock);
	}

	/// Returns the inspected expression or null.
	/// @return the inspected expression or null
	public Expression getTypedInspectedVariable() {
		if (declaredIn instanceof ConnectionBlock conn)
			 return (conn.getTypedInspectedVariable());
		else return (null);
	}

	/// Get the inspectedVariable of the enclosing ConnectionBlock or null.
	/// @return the inspectedVariable of the enclosing ConnectionBlock or null.
	public VariableExpression getInspectedVariable() {
		if (declaredIn instanceof ConnectionBlock conn)
			 return (conn.inspectedVariable);
		else return (null);
	}

	// ***************************************************************************************
	// *** CODING: edUnqualifiedStaticLink
	// ***************************************************************************************
	/// Java coding utility: Edit unqualified static link chain.
	/// @return the resulting string
	public String edUnqualifiedStaticLink() {
		String staticLink;
		Expression connectedObject = getTypedInspectedVariable();
		if (connectedObject != null)
			staticLink = connectedObject.toJavaCode();
		else {
			staticLink = declaredIn.edCTX();
		}
		return (staticLink);
	}

	// ***************************************************************************************
	// *** CODING: edQualifiedStaticLink
	// ***************************************************************************************
	/// Java coding utility: Edit qualified static link chain.
	/// @return the resulting string
	public String edQualifiedStaticLink() {
		String staticLink;
		Expression connectedObject = getTypedInspectedVariable();
		if (connectedObject != null)
			staticLink = connectedObject.toJavaCode();
		else {
			staticLink = declaredIn.edCTX();
			String cast = declaredIn.getJavaIdentifier();
			staticLink = "((" + cast + ")" + staticLink + ')';
		}
		return (staticLink);
	}

	// ***************************************************************************************
	// *** JVM CODING: buildQualifiedStaticLink
	// ***************************************************************************************
	/// ClassFile coding utility: Build qualified static link chain.
	/// @param codeBuilder the codeBuilder to use.
	public void buildQualifiedStaticLink(CodeBuilder codeBuilder) {
		// Edit staticLink reference
		if(this.isConnected()) {
	    	VariableExpression inspectedVariable = getInspectedVariable();
			inspectedVariable.buildEvaluation(null, codeBuilder);
		}
		else {
			boolean withFollowSL = declaredIn.buildCTX(codeBuilder);
			if(withFollowSL) codeBuilder.checkcast(declaredIn.getClassDesc());
		}
	}

	// ***************************************************************************************
	// *** JVM CODING: buildIdentifierAccess
	// ***************************************************************************************
	/// ClassFile Coding Utility: Build identifier access.
	/// @param destination true if destination
	/// @param codeBuilder the CodeBuilder
	public void buildIdentifierAccess(boolean destination,CodeBuilder codeBuilder) {
		Meaning meaning=this;
		if (meaning.isConnected()) {
			Expression inspectedExpression = ((ConnectionBlock) meaning.declaredIn).getTypedInspectedVariable();
				inspectedExpression.buildEvaluation(null, codeBuilder);
		} else if(declaredAs instanceof ProcedureDeclaration) {			
	        // 0: getstatic     #17                 // Field _CUR:Lsimula/runtime/RTS_RTObject;
	        // 3: getfield      #21                 // Field simula/runtime/RTS_RTObject._SL:Lsimula/runtime/RTS_RTObject;
	        // 6: checkcast     #26                 // class simulaTestPrograms/adHoc000_PPP
	 		int corr = 1;
			boolean withFollowSL = meaning.declaredIn.buildCTX(corr,codeBuilder);
			if(withFollowSL) codeBuilder.checkcast(meaning.declaredIn.getClassDesc());
			
		} else if (!(meaning.declaredIn.declarationKind == ObjectKind.ContextFreeMethod
				|| meaning.declaredIn.declarationKind == ObjectKind.MemberMethod)) {

			// id = "((" + cast + ")" + meaning.declaredIn.edCTX() + ")." + id;
			if (meaning.foundBehindInvisible) {
				meaning.declaredIn.buildCTX(codeBuilder);
				codeBuilder.checkcast(meaning.foundIn.getClassDesc());
			} else {
				boolean withFollowSL = meaning.declaredIn.buildCTX(codeBuilder);
				if(withFollowSL) {
					codeBuilder.checkcast(meaning.declaredIn.getClassDesc());
				}
			}
		}
	}

	@Override
	public String toString() {
		if (declaredAs == null)	return ("NO MEANING");
		String BL = (declaredIn.IS_SEMANTICS_CHECKED()) ? ", rtBlockLevel=" + declaredIn.getRTBlockLevel() : "";
		return ("DeclaredAs " + declaredAs + ", foundBehindInvisible=" + foundBehindInvisible
				+ "  (" + BL + ",declaredIn=" + declaredIn + ",foundIn=" + foundIn + ')');
	}

}
