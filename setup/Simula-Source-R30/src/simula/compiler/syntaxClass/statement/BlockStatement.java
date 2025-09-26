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
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.syntaxClass.declaration.BlockDeclaration;
import simula.compiler.syntaxClass.declaration.ClassDeclaration;
import simula.compiler.syntaxClass.declaration.PrefixedBlockDeclaration;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.syntaxClass.expression.VariableExpression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// BlockStatement.
/// <pre>
/// Simula Standard: 4.10 Blocks
/// 
///   block
///      = subblock
///      | prefixed-block
///      
///         subblock = BEGIN declaration { ; declaration } ; statement { ; statement } END
///         
///         prefixed-block
///            = block-prefix main-block
///            
///            block-prefix
///               = class-identifier [ actual-parameter-part ]
///               
///            main-block
///            
///               = block
///               | BEGIN statement { ; statement } END
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/BlockStatement.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class BlockStatement extends Statement {
	
	/// The associated block declaration.
	private BlockDeclaration blockDeclaration;

	/// Create a new BlockStatement.
	/// @param blockDeclaration the BlockDeclaration
	public BlockStatement(final BlockDeclaration blockDeclaration) {
		super(blockDeclaration.lineNumber);
		this.blockDeclaration = blockDeclaration;
		if (Option.internal.TRACE_PARSE) Util.TRACE("Line "+lineNumber+": BlockStatement: "+this);
	}
	
	/// Check if this BlockStatement is a CompoundStatement.
	/// @return true if this BlockStatement is a CompoundStatement
	boolean isCompoundStatement() {
		return(blockDeclaration.declarationKind == ObjectKind.CompoundStatement);
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		blockDeclaration.doChecking();
		SET_SEMANTICS_CHECKED();
	}

	/// Add a leading label to this BlockStatement.
	/// @param labelcode the label code
	void addLeadingLabel(String labelcode) {
		blockDeclaration.addLeadingLabel(labelcode);
	}
	
	@Override
	public void doJavaCoding() {
		Global.sourceLineNumber=lineNumber;
		ASSERT_SEMANTICS_CHECKED();
		if(blockDeclaration.declarationKind!=ObjectKind.CompoundStatement) {
			String staticLink=blockDeclaration.declaredIn.edCTX();
			StringBuilder s = new StringBuilder();
			s.append("new ").append(blockDeclaration.getJavaIdentifier()).append('(');
			s.append(staticLink);
			if(blockDeclaration instanceof PrefixedBlockDeclaration pref) {
				VariableExpression blockPrefix=pref.blockPrefix;
				if(blockPrefix.hasArguments())
					for (Expression par:blockPrefix.checkedParams) {
						s.append(',').append(par.toJavaCode());
					}
			}
			s.append(')');
			if(blockDeclaration.declarationKind==ObjectKind.PrefixedBlock && ((ClassDeclaration)blockDeclaration).isDetachUsed())
				s.append("._START();");
			else s.append("._STM();");
			JavaSourceFileCoder.code(s.toString());
		}
		boolean duringSTM_Coding=Global.duringSTM_Coding;
		Global.duringSTM_Coding=false;
		blockDeclaration.doJavaCoding();
		Global.duringSTM_Coding=duringSTM_Coding;
	}

	@Override
	public void buildByteCode(CodeBuilder codeBuilder) {
		Global.sourceLineNumber=lineNumber;
		ASSERT_SEMANTICS_CHECKED();
		blockDeclaration.buildByteCode(codeBuilder);
	}

	@Override
	public void print(final int indent) {
		blockDeclaration.print(indent);
	}
	
	@Override
	public void printTree(final int indent, final Object head) {
		blockDeclaration.printTree(indent,head);
	}

	@Override
	public String toString() {
		return ("BLOCK " + blockDeclaration);
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************

	/// Default constructor used by Attribute File I/O
	private BlockStatement() { super(0); }

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeBlockStatement: " + this);
		oupt.writeKind(ObjectKind.BlockStatement);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** BlockStatement
		oupt.writeObj(blockDeclaration);
	}

	/// Read and return a BlockStatement object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the BlockStatement object read from the stream.
	/// @throws IOException if something went wrong.
	public static BlockStatement readObject(AttributeInputStream inpt) throws IOException {
		BlockStatement stm = new BlockStatement();
		stm.OBJECT_SEQU = inpt.readSEQU(stm);
		// *** SyntaxClass
		stm.lineNumber = inpt.readShort();
		// *** BlockStatement
		stm.blockDeclaration = (BlockDeclaration) inpt.readObj();

		Util.TRACE_INPUT("BlockStatement: " + stm);
		return(stm);
	}

}
