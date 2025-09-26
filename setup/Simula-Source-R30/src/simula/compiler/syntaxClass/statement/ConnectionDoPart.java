/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.statement;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.ConnectionBlock;
import simula.compiler.syntaxClass.expression.AssignmentOperation;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Utility class to hold the single Connection do-part.
///
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/ConnectionDoPart.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class ConnectionDoPart extends SyntaxClass {
	
	/// The associated connection statement.
	ConnectionStatement connectionStatement;
	
	/// The associated connection block.
	public ConnectionBlock connectionBlock;

	/// Create a new do-part.
	/// @param connectionStatement The owner.
	/// @param connectionBlock The associated connection block
	/// @param statement the statement after DO
	ConnectionDoPart(final ConnectionStatement connectionStatement, final ConnectionBlock connectionBlock,final Statement statement) {
		this.connectionStatement = connectionStatement;
		this.connectionBlock = connectionBlock; // this.statement=statement;
		connectionBlock.setStatement(statement);
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("NEW ConnectionDoPart: " + toString());
	}

	/// Perform semantic checking.
	public void doChecking() {
		Type type = connectionStatement.inspectVariableDeclaration.type;
		String refIdentifier = type.getRefIdent();
		if (refIdentifier == null)
			Util.error("The Variable " + connectionStatement.inspectedVariable + " is not ref() type");
		connectionBlock.setClassDeclaration(AssignmentOperation.getQualification(refIdentifier));
		connectionBlock.doChecking();
		SET_SEMANTICS_CHECKED();
	}

	/// Perform Java coding.
	/// @param first true if coding the first when-part
	public void doCoding(final boolean first) {
		ASSERT_SEMANTICS_CHECKED();
		connectionBlock.doJavaCoding();
	}

	@Override
	public void buildByteCode(CodeBuilder codeBuilder) {
		ASSERT_SEMANTICS_CHECKED();
		connectionBlock.buildByteCode(codeBuilder);
		codeBuilder.goto_(connectionStatement.endLabel);
	}

	/// Utility print method.
	/// @param indent the indent
	@Override
	public void printTree(final int indent, final Object head) {
    	String spc=edTreeIndent(indent);
		Util.println(spc + "DO " + connectionBlock.statement);
		connectionBlock.printTree(indent, head);
	}

	@Override
	public String toString() {
		return (connectionBlock.toString());
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	protected ConnectionDoPart() {}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeDoPart: " + this);
		oupt.writeKind(ObjectKind.ConnectionDoPart);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** ConnectionDoPart
		oupt.writeObj(connectionStatement);
		oupt.writeObj(connectionBlock);
	}
	
	/// Read and return a ConnectionDoPart object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the ConnectionDoPart object read from the stream.
	/// @throws IOException if something went wrong.
	public static ConnectionDoPart readObject(AttributeInputStream inpt) throws IOException {
		ConnectionDoPart dop = new ConnectionDoPart();
		dop.OBJECT_SEQU = inpt.readSEQU(dop);
		// *** SyntaxClass
		dop.lineNumber = inpt.readShort();
		// *** ConnectionDoPart
		dop.connectionStatement = (ConnectionStatement) inpt.readObj();
		dop.connectionBlock = (ConnectionBlock) inpt.readObj();
		Util.TRACE_INPUT("ConnectionDoPart: " + dop);
		return(dop);
	}
	
}

