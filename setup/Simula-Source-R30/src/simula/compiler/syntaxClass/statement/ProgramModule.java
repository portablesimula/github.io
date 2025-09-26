/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.statement;

import java.io.IOException;
import java.util.Vector;

import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.ClassDeclaration;
import simula.compiler.syntaxClass.declaration.ConnectionBlock;
import simula.compiler.syntaxClass.declaration.Declaration;
import simula.compiler.syntaxClass.declaration.DeclarationScope;
import simula.compiler.syntaxClass.declaration.ExternalDeclaration;
import simula.compiler.syntaxClass.declaration.MaybeBlockDeclaration;
import simula.compiler.syntaxClass.declaration.PrefixedBlockDeclaration;
import simula.compiler.syntaxClass.declaration.ProcedureDeclaration;
import simula.compiler.syntaxClass.declaration.StandardClass;
import simula.compiler.syntaxClass.declaration.StandardProcedure;
import simula.compiler.syntaxClass.expression.VariableExpression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Simula Program Module.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/statement/ProgramModule.java">
/// <b>Source File</b></a>.
/// 
/// <pre>
/// 
/// Simula Standard: Chapter 6 Program Modules
/// 
///      SIMULA-source-module
///         = [ external-head ] ( program | procedure-declaration | class-declaration )
///         
///         external-head = external-declaration ; { external-declaration ; }
///         
///            external-declaration = external-procedure-declaration | external-class-declaration
///            
/// 			program = [ block-prefix ] block | [ block-prefix ] compound-statement
/// 
/// 				block-prefix = class-identifier [ ( actual-parameter-part ) ]
/// 
/// 			procedure-declaration
/// 			     = [ type ] PROCEDURE procedure-identifier procedure-head procedure-body
/// 
/// </pre>
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class ProgramModule extends Statement {
	
	/// The Variable SYSIN.
	final private VariableExpression sysin;
	
	/// The Variable SYSOUT.
	final private VariableExpression sysout;
	
	/// The mainModule declaration.
	public final DeclarationScope mainModule;

	/// The external head
	public Vector<ExternalDeclaration> externalHead;

	/// Returns the mainModule identifier.
	/// @return the mainModule identifier
	public String getIdentifier() { return(mainModule.identifier); }

	/// Returns the relative file name.
	/// @return the relative file name
	public String getRelativeAttributeFileName() {
		if(mainModule.declarationKind==ObjectKind.Class) return(Global.packetName+"/CLASS.AF");
		if(mainModule.declarationKind==ObjectKind.Procedure) return(Global.packetName+"/PROCEDURE.AF");
		else return(null);
	}
	  
	/// Returns true if this program mainModule is executable.
	/// @return true if this program mainModule is executable
	public boolean isExecutable() {
		if(mainModule.declarationKind==ObjectKind.SimulaProgram) return(true);
		if(mainModule.declarationKind==ObjectKind.PrefixedBlock) return(true);
		else return(false);
	}

	/// Create a new ProgramModule.
	public ProgramModule() {
		super(0);
		DeclarationScope mainModule=null;
		sysin=new VariableExpression("sysin");
		sysout=new VariableExpression("sysout");
		try	{
			if(Option.internal.TRACE_PARSE) Parse.TRACE("Parse Program");
			Global.setScope(StandardClass.BASICIO);		    	// BASICIO Begin
			new ConnectionBlock(sysin, null)                     	//    Inspect sysin do
			     .setClassDeclaration(StandardClass.Infile);
			new ConnectionBlock(sysout, null)                    	//    Inspect sysout do
			     .setClassDeclaration(StandardClass.Printfile);
			Global.getCurrentScope().sourceBlockLevel=0;
			while(Parse.accept(KeyWord.EXTERNAL)) {
				externalHead = ExternalDeclaration.expectExternalHead(StandardClass.BASICIO);					
				Parse.expect(KeyWord.SEMICOLON);
			}
			String ident=Parse.acceptIdentifier();
			if(ident!=null) {
				if(Parse.accept(KeyWord.CLASS)) mainModule=ClassDeclaration.expectClassDeclaration(ident);
			    else {
			    	VariableExpression blockPrefix=VariableExpression.expectVariable(ident);	
			    	
			  	    Parse.expect(KeyWord.BEGIN);
		        	mainModule=PrefixedBlockDeclaration.expectPrefixedBlock(blockPrefix,true);
			    }
			}
			else if(Parse.accept(KeyWord.BEGIN)) mainModule=MaybeBlockDeclaration.createMainProgramBlock(); 
			else if(Parse.accept(KeyWord.CLASS)) mainModule=ClassDeclaration.expectClassDeclaration(null);
			else {
				Type type=Parse.acceptType();
			    if(Parse.expect(KeyWord.PROCEDURE)) mainModule=ProcedureDeclaration.expectProcedureDeclaration(type);
			}
			StandardClass.BASICIO.declarationList.add(mainModule);
		
			if(Option.verbose) Util.TRACE("ProgramModule: END NEW SimulaProgram: "+toString());
		} catch(Throwable e) {
			e.printStackTrace();
			Util.IERR();
			}
		this.mainModule=mainModule;
	}	

	@Override
	public void doChecking() {
		if(IS_SEMANTICS_CHECKED()) return;
		sysin.doChecking();
		sysout.doChecking();
		mainModule.doChecking();
		SET_SEMANTICS_CHECKED();
	}
  
	@Override
	public void doJavaCoding() { mainModule.doJavaCoding(); }

	/// Create Java ClassFile.
	/// @throws IOException if something went wrong
	public void createJavaClassFile() throws IOException {
		Global.sourceLineNumber = lineNumber;
		mainModule.createJavaClassFile();
	}

	@Override
	public void print(final int indent) { mainModule.print(0); }

	@Override
	public void printTree(final int indent, final Object head) {
		IO.println("BASICIO");
		IO.println("    ... Standard Classes and Procedures");
		for(Declaration decl:StandardClass.BASICIO.declarationList) {
			if(decl instanceof StandardProcedure) ; // Nothing
			else if(decl instanceof StandardClass) ; // Nothing
			else decl.printTree(1,this);
		}
		IO.println("=================================================================");
	}
	
	@Override
	public String toString() { return((mainModule==null)?"":""+mainModule.identifier); }
	
}
