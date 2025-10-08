/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.statement;

import java.lang.classfile.CodeBuilder;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Inline Statement.
/// 
/// <pre>
/// - detach
/// - terminate
/// - try
/// - catch
/// </pre>
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/InlineStatement.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class InlineStatement extends Statement {
	
	/// The kind String: detach | terminate | try | catch
	String kind;
	
	/// Create a new InlineStatement.
	/// @param kind the kind code string.
	public InlineStatement(String kind) {
		super(1);
		this.kind = kind;
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		SET_SEMANTICS_CHECKED();
	}

	@Override
	public void doJavaCoding() {
		Global.sourceLineNumber = lineNumber;
		ASSERT_SEMANTICS_CHECKED();
		if(kind.equalsIgnoreCase("detach")) JavaSourceFileCoder.code("detach();","Process'detach");
		else if(kind.equalsIgnoreCase("terminate")) JavaSourceFileCoder.code("terminate();","Process'terminate");
		else if(kind.equals("try")) JavaSourceFileCoder.code("try {");
		else if(kind.equals("catch")) JavaSourceFileCoder.code("} catch(RuntimeException e) { _CUR=this; _onError(e,onError_0()); }");
		else Util.IERR();
	}

	@Override
	public void buildByteCode(CodeBuilder codeBuilder) {
		Global.sourceLineNumber=lineNumber;
		ASSERT_SEMANTICS_CHECKED();
		if(kind.equalsIgnoreCase("detach")) {
			codeBuilder.aload(0);
			RTS.invokevirtual_RTS_detach(codeBuilder);
		}
		else if(kind.equalsIgnoreCase("terminate")) {
			codeBuilder.aload(0);
			RTS.invokevirtual_Process_terminate(codeBuilder);
		}
		else if(kind.equals("try")) ;   // Nothing. Treated by ClassDeclaration.buildMethod_CatchingErrors_TRY_CATCH
		else if(kind.equals("catch")) ; // Nothing. Treated by ClassDeclaration.buildMethod_CatchingErrors_TRY_CATCH
		else Util.IERR();
	}

	@Override
	public void printTree(final int indent, final Object head) {
		IO.println(edTreeIndent(indent)+"INLINE "+kind);
	}

	@Override
	public String toString() {
		return ("INLINE " + kind);
	}

}
