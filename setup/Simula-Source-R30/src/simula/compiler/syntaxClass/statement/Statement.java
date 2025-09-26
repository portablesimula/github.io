/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.statement;

import java.lang.classfile.CodeBuilder;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.declaration.DeclarationScope;
import simula.compiler.syntaxClass.declaration.LabelDeclaration;
import simula.compiler.syntaxClass.declaration.MaybeBlockDeclaration;
import simula.compiler.syntaxClass.declaration.PrefixedBlockDeclaration;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.syntaxClass.expression.VariableExpression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.LabelList;
import simula.compiler.utilities.ObjectList;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Statement.
/// 
/// <pre>
/// 
/// Simula Standard: Chapter 4: Statements
/// 
///  Statement
///         =  { label : }  unconditional-statement
///         |  { label : }  conditional-statement
///         |  { label : }  for-statement
/// 
///     Unconditional-statement
///         =  assignment-statement  NOTE: Treated as a binary operation
///         |  while-statement
///         |  goto-statement
///         |  procedure-statement
///         |  object-generator
///         |  connection-statement
///         |  compound-statement
///         |  block
///         |  dummy-statement
///         |  activation-statement
/// 
/// </pre>
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/Statement.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public abstract class Statement extends SyntaxClass {
	
	/// Create a new Statement.
	/// @param line the source line number
	protected Statement(int line) {
		lineNumber=line;
	}

	/// Parse a statement.
	/// @return the statement
	public static Statement expectStatement() {
		ObjectList<LabelDeclaration> labels = null;
		int lineNumber=Parse.currentToken.lineNumber;
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("Statement.doParse: LabeledStatement: lineNumber="+lineNumber+", current=" + Parse.currentToken	+ ", prev=" + Parse.prevToken);
		String ident = Parse.acceptIdentifier();
		while (Parse.accept(KeyWord.COLON)) {
			if (ident != null) {
				if (labels == null)	labels = new ObjectList<LabelDeclaration>();
				LabelDeclaration label = new LabelDeclaration(ident);
				labels.add(label);
				DeclarationScope scope = Global.getCurrentScope();
				if(scope.labelList == null) scope.labelList = new LabelList(scope); 
				scope.labelList.add(label);
			} else Util.error("Missplaced ':'");
			ident = Parse.acceptIdentifier();
		}
		if(ident!=null) Parse.saveCurrentToken(); // Not Label: Pushback
		Statement statement = expectUnlabeledStatement();
		if (labels != null && statement != null)
			statement = new LabeledStatement(lineNumber,labels, statement);
		return (statement);
	}

	/// Parse Utility: Expect an unlabeled statement.
	/// @return the resulting statement
	private static Statement expectUnlabeledStatement() {
		int lineNumber=Parse.currentToken.lineNumber;
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("Statement.doUnlabeledStatement: lineNumber="+lineNumber+", current=" + Parse.currentToken	+ ", prev=" + Parse.prevToken);
		switch(Parse.currentToken.getKeyWord()) {
		    case KeyWord.BEGIN: Parse.nextToken(); return (new MaybeBlockDeclaration(null).expectMaybeBlock(lineNumber));
		    case KeyWord.IF:    Parse.nextToken(); return (new ConditionalStatement(lineNumber));
		    case KeyWord.GOTO:  Parse.nextToken(); return (new GotoStatement(lineNumber));
		    case KeyWord.GO:    Parse.nextToken(); 
				        if (!Parse.accept(KeyWord.TO))	Util.error("Missing 'TO' after 'GO'");
				        return (new GotoStatement(lineNumber));
		    case KeyWord.FOR:        Parse.nextToken(); return (new ForStatement(lineNumber));
		    case KeyWord.WHILE:      Parse.nextToken(); return (new WhileStatement(lineNumber));
		    case KeyWord.INSPECT:    Parse.nextToken(); return (new ConnectionStatement(lineNumber));
		    case KeyWord.SWITCH:	 if(Option.EXTENSIONS) {
		    							 Parse.nextToken(); return (new SwitchStatement(lineNumber));
		    						 } break;
		    case KeyWord.ACTIVATE:   Parse.nextToken(); return (new ActivationStatement(lineNumber));
		    case KeyWord.REACTIVATE: Parse.nextToken(); return (new ActivationStatement(lineNumber));
		    case KeyWord.INNER:      Parse.nextToken(); return (new InnerStatement(lineNumber));
		    case KeyWord.SEMICOLON:  Parse.nextToken(); return (new DummyStatement(lineNumber)); // Dummy Statement
		    case KeyWord.END:        return (new DummyStatement(lineNumber)); // Dummy Statement, keep END
		
		    case KeyWord.IDENTIFIER: case KeyWord.NEW: case KeyWord.THIS: case KeyWord.BEGPAR:
		         Expression expr = Expression.acceptExpression();
		         if(expr!=null) {
		        	 if(expr instanceof VariableExpression var) {
		        		 if (Parse.accept(KeyWord.BEGIN))
		        			 return new BlockStatement(PrefixedBlockDeclaration.expectPrefixedBlock(var,false));
		        	 }
		        	 return (new StandaloneExpression(lineNumber,expr));
		         }
		}
    	Parse.skipMisplacedCurrentSymbol();
    	return(new DummyStatement(lineNumber));
	}

	@Override
	public void doJavaCoding() {
		Global.sourceLineNumber=lineNumber;
		ASSERT_SEMANTICS_CHECKED();
		JavaSourceFileCoder.code(toJavaCode() + ';');
	}

	/// Build Java ByteCode.
	@Override
	public void buildByteCode(CodeBuilder codeBuilder) {
		Util.IERR("Method buildByteCode need a redefinition in "+this.getClass().getSimpleName());
	}

}
