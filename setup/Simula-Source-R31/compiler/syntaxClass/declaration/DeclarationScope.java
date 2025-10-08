/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.declaration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.constant.ClassDesc;

import simula.compiler.utilities.DeclarationList;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.LabelList;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Declaration Scope.
/// 
/// This class is prefix to BlockDeclaration and ConnectionBlock, and superclass
/// of ClassDeclaration, ProcedureDeclaration and MaybeBlockDeclaration.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/DeclarationScope.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public abstract class DeclarationScope extends Declaration  {

	/// The source file name.
	public String sourceFileName;

	/// The source block level. Set during Parsing.
	public int sourceBlockLevel;

	/// Indicate if this scope has local classes.
	public boolean hasLocalClasses = false;
	
	/// If not null; this Class/Procedure is Pre-Compiled from a .jar file
	public String isPreCompiledFromFile;

	/// The declaration list.
	public DeclarationList declarationList;

	/// The label list.
	public LabelList labelList; // = new LabelList();

	// ***********************************************************************************************
	// *** Constructor
	// ***********************************************************************************************
	/// Create a new DeclarationScope.
	/// 
	/// @param ident scope identifier
	protected DeclarationScope(final String ident) {
		super(ident);
		declarationList = new DeclarationList(getClass().getSimpleName() + ':' + ident + ":Line=" + Global.sourceLineNumber);
		declaredIn = Global.getCurrentScope();
		Global.setScope(this);
		if (declaredIn != null)
			sourceBlockLevel = declaredIn.sourceBlockLevel + 1;
	}
	
	/// Modify the identifier of this class, procedure, ...
	/// @param newIdentifier the new identifier
	protected void modifyIdentifier(final String newIdentifier) {
		this.identifier = newIdentifier;
		checkAlreadyDefined();
		if (declarationKind == ObjectKind.ContextFreeMethod) externalIdent = this.identifier;
		else if (declarationKind == ObjectKind.MemberMethod) externalIdent = this.identifier;
		else if (externalIdent == null)	externalIdent = edJavaClassName();
	}

	// ***********************************************************************************************
	// *** Utility: edJavaClassName
	// ***********************************************************************************************
	/// Utility to edit JavaClass'Name
	/// @return the edited JavaClass'Name
	protected String edJavaClassName() {
		DeclarationScope scope = this;
		String id = null;
		while (scope != null) {
			if ((scope instanceof BlockDeclaration) && !(scope instanceof StandardClass)
					&& !(scope instanceof StandardProcedure)) {
				if (id == null)
					id = scope.identifier;
				else
					id = scope.identifier + '_' + id;
			}
			scope = scope.declaredIn;
		}
		return (id);
	}

	// ***********************************************************************************************
	// *** After Checking: getRTBlockLevel
	// ***********************************************************************************************
	/// Utility: Get Runtime BlockLevel.
	/// @return true: the Runtime BlockLevel.
	public int getRTBlockLevel() {
		int rtBlockLevel = declaredIn.getRTBlockLevel() + 1;
		return rtBlockLevel;
	}

	// ***********************************************************************************************
	// *** Utility: scopeID
	// ***********************************************************************************************
	/// Returns a printable scope ID.
	/// @return a printable scope ID
	public String scopeID() {
		if (getRTBlockLevel() > 1)
			return (declaredIn.scopeID() + '.' + identifier);
		return identifier;
	}

	// ***********************************************************************************************
	// *** Utility: prefixLevel
	// ***********************************************************************************************
	/// Returns the prefix level.
	/// 
	/// Redefined in ClassDeclaration
	/// @return the prefix level
	public int prefixLevel() {
		return 0;
	}

	// ***********************************************************************************************
	// *** Utility: findVisibleAttributeMeaning
	// ***********************************************************************************************
	/// Find visible attribute's Meaning
	/// 
	/// @param ident attribute identifier
	/// @return the resulting Meaning
	public Meaning findVisibleAttributeMeaning(final String ident) {
		Util.IERR("DeclarationScope.findVisibleAttributeMeaning: SHOULD BEEN REDEFINED: " + identifier + " IN " + this.getClass().getSimpleName());
		return null;
	}

	// ***********************************************************************************************
	// *** Utility: findMeaning
	// ***********************************************************************************************
	/// Find Meaning
	/// 
	/// @param identifier declared identifier
	/// @return the resulting Meaning
	public Meaning findMeaning(final String identifier) {
		Meaning meaning = findVisibleAttributeMeaning(identifier);
		if (meaning == null && declaredIn != null)
			meaning = declaredIn.findMeaning(identifier);
		
		if (meaning == null) {
			if (!Global.duringParsing) {
				Util.error("Undefined variable: " + identifier);
			}
			UndefinedDeclaration undef = new UndefinedDeclaration(identifier);
			meaning = new Meaning(undef, this); // Error Recovery
		}
		return (meaning);
	}

	// ***********************************************************************************************
	// *** Utility: findLabelMeaning
	// ***********************************************************************************************
	/// Find Label's Meaning
	/// 
	/// @param identifier declared label identifier
	/// @return the resulting Meaning
	public Meaning findLabelMeaning(final String identifier) {
		for (LabelDeclaration dcl : labelList.getDeclaredLabels())
			if (Util.equals(dcl.identifier, identifier))
				return (new Meaning(dcl, this, this, false));
		
		if(this instanceof ClassDeclaration cls && cls.hasRealPrefix())
			return(cls.getPrefixClass().findLabelMeaning(identifier));

		if (declaredIn != null)
			return (declaredIn.findLabelMeaning(identifier));
		return (null);
	}

	// ***********************************************************************************************
	// *** Utility: findProcedure -- Follow Static Chain Looking for a Procedure named 'identifier'
	// ***********************************************************************************************
	/// Follow Static Chain Looking for a Procedure named 'identifier'
	/// @param identifier the procedure identifier
	/// @return the resulting ProcedureDeclaration
	public ProcedureDeclaration findProcedure(final String identifier) {
		DeclarationScope scope = this;
		while (scope != null) {
			if (Util.equals(identifier, scope.identifier)) {
				if (scope instanceof ProcedureDeclaration proc)
					return (proc);
				return (null);
			}
			scope = scope.declaredIn;
		}
		return (null);
	}

	// ***********************************************************************************************
	// *** Coding Utility: edCTX
	// ***********************************************************************************************
	/// ClassFile coding utility: Edit current context chain.
	/// @return edited context chain
	public String edCTX() {
		if (getRTBlockLevel() == 0)	return ("_USR");			
		int curLevel = Global.getCurrentScope().getRTBlockLevel();
		int ctxDiff = curLevel - getRTBlockLevel();
		return (edCTX(ctxDiff));

	}

	// ***********************************************************************************************
	// *** Coding Utility: edCTX
	// ***********************************************************************************************
	/// ClassFile coding utility: Edit context chain.
	/// @param ctxDiff block level difference.
	/// @return edited context chain
	public static String edCTX(int ctxDiff) {
		String ret = "_CUR";
		while ((ctxDiff--) > 0)
			ret = ret + "._SL";
		return ("(" + ret + ')');
	}

	// ***********************************************************************************************
	// *** Byte Coding Utility: buildCTX
	// ***********************************************************************************************
	/// ClassFile coding utility: Build current context chain.
	/// @param codeBuilder the codeBuilder to use.
	/// @return true: if resulting field need a cast.
	public boolean buildCTX(CodeBuilder codeBuilder) {
		return(buildCTX(0, codeBuilder));
	}
	
	/// ClassFile coding utility: Build current context chain.
	/// @param corr correction .
	/// @param codeBuilder the codeBuilder to use.
	/// @return true: if resulting field need a cast.
	public boolean buildCTX(int corr,CodeBuilder codeBuilder) {
		ConstantPoolBuilder pool = codeBuilder.constantPool();
		
		DeclarationScope endScope=this;                     // The scope of the attribute to access.
		int endLevel = endScope.getRTBlockLevel();
		
		DeclarationScope curScope=Global.getCurrentScope(); // The current scope. In case of Thunk one level up to Thunk.ENV
		int curLevel = curScope.getRTBlockLevel();
		int ctxDiff = curLevel - endLevel - corr;

		if(endLevel == 0 && ctxDiff > 3) {
			// Access outmost block directly
			codeBuilder.getstatic(RTS.FRE.RTObject_USR(pool));
			return(true);
		}

		codeBuilder.aload(0); // Current Object
		
		boolean withFollowSL = false;
		if(Global.getCurrentScope() instanceof Thunk thunk) {
			curScope=thunk.declaredIn;
			DeclarationScope encl = curScope;
			while(encl instanceof ConnectionBlock) encl = encl.declaredIn;
			codeBuilder
				.getfield(RTS.FRE.NAME_CUR(pool))
				.checkcast(encl.getClassDesc());
			ctxDiff = curScope.getRTBlockLevel() - getRTBlockLevel();
		}
		while ((ctxDiff--) > 0) {
			curScope=curScope.declaredIn;
			codeBuilder.getfield(RTS.FRE.RTObject_SL(pool));
			withFollowSL = true;			
		}
		return(withFollowSL);
	}
	

	// ***********************************************************************************************
	// *** Coding Utility: buildCTX
	// ***********************************************************************************************
	/// ClassFile coding utility: Build context chain.
	/// @param ctxDiff block level difference.
	/// @param codeBuilder the codeBuilder to use.
	/// @return  true: if resulting field need a cast.
	public static boolean buildCTX2(int ctxDiff,CodeBuilder codeBuilder) {
		ConstantPoolBuilder pool = codeBuilder.constantPool();
		DeclarationScope curScope=Global.getCurrentScope();
		boolean withFollowSL = false;
		codeBuilder.aload(0);
		while ((ctxDiff--) > 0) {
			curScope=curScope.declaredIn;
			withFollowSL = true;			
			codeBuilder.getfield(RTS.FRE.RTObject_SL(pool));
		}
		return(withFollowSL);
	}


	// ***********************************************************************************************
	// *** Print Utility: edScopeChain
	// ***********************************************************************************************
	/// Edit scope chain.
	/// @return edited scope chain
	public String edScopeChain() {
		if (declaredIn == null)
			return (identifier);
		String encName = declaredIn.edScopeChain();
		return (identifier + '.' + encName);
	}

	// ***********************************************************************************************
	// *** ClassFile coding Utility: getClassDesc -- Redefined in StandardClass, SubBlock and ConnectionBlock
	// ***********************************************************************************************
	/// Return the ClassDesc
	/// @return the ClassDesc
	public ClassDesc getClassDesc() {
		return(RTS.CD.classDesc(externalIdent));
	}
	
	/// Debug utility: printStaticChain
	/// @param title title String
	/// @param details level of details
	public void printStaticChain(String title,int details) {
		IO.println("\nDeclarationScope.printStaticChain: **************** "+title+" ****************");
		DeclarationScope scope=this;//.declaredIn;
		int lim = 5;//7;
		for(int i=1;i<lim;i++) {
			IO.println("DeclarationScope.printStaticChain: " + scope.edScope());
			if(details > 0) {
				for(Declaration decl:scope.declarationList) {
					IO.println("DeclarationScope.printStaticChain:                  "+decl);			
				}
			}
			scope=scope.declaredIn;
		}
	}
	
	/// Debug utility: print DeclarationList.
	/// @param indent the indentation.
	protected void printDeclarationList(int indent) {
		for(Declaration d:declarationList) d.printTree(indent,this);
		if(labelList != null) for(LabelDeclaration d:labelList.getDeclaredLabels()) d.printTree(indent,this);
	}
	
	/// Debug utility: edScope
	/// @return edited scope String
	public String edScope() {
		return "DeclarationScope: BL=" + getRTBlockLevel() + "  "
				+ getClass().getSimpleName() + ' ' + identifier + '[' + externalIdent + "] declaredIn="+declaredIn;
	}

	// ***********************************************************************************************
    // *** ByteCoding: buildClassFile
    // ***********************************************************************************************
	/// Build Class File
	/// @return Class File bytes
    public abstract byte[] buildClassFile();

	// ***********************************************************************************************
	// *** createJavaClassFile
	// ***********************************************************************************************
    /// Indicator used to prevent multiple ClassFile generation.
    /// This situation may occur during the class body concatenation process.
    protected boolean CLASSFILE_ALREADY_GENERATED;

	/// Create Java ClassFile.
	/// @throws IOException  if something went wrong.
    public void createJavaClassFile() throws IOException {
    	if (this.isPreCompiledFromFile != null) {
			if(Option.verbose) IO.println("Skip  buildClassFile: "+this.identifier+" -- It is read from "+isPreCompiledFromFile);			
    	} else if (CLASSFILE_ALREADY_GENERATED) {
			if(Option.verbose) IO.println("Skip  buildClassFile: "+this.identifier+" -- It is already generated");			
    	} else {
    		CLASSFILE_ALREADY_GENERATED = true;
    		buildAndLoadOrAddClassFile();
//    		Util.dumpStack();
    	}
    }
	
    /// Redefined in ClassDeclaration
    /// @throws IOException if something went wrong.
    protected void buildAndLoadOrAddClassFile() throws IOException {
		if (this.isPreCompiledFromFile != null) {
			if(Option.verbose) IO.println("Skip  buildClassFile: "+this.identifier);
		} else {
	    	byte[] bytes = doBuildClassFile();
	    	loadOrAddClassFile(bytes);
    	}
    }
    
    /// Build ClassFile.
    /// @return ClassFile bytes.
    protected byte[] doBuildClassFile() {
    	byte[] bytes;
    	if(this instanceof BlockDeclaration blk) {
    		blk.prevBlock = BlockDeclaration.currentBlock;
    		BlockDeclaration.currentBlock = blk;
    			bytes = buildClassFile();
    		BlockDeclaration.currentBlock = blk.prevBlock;
    	} else {
    		bytes = buildClassFile();
    	}
    	return bytes;
    }
    
    /// Get ClassFile bytes from file.
    /// @return ClassFile bytes from file.
    protected byte[] getBytesFromFile() {
    	IO.println("DeclarationScope.getBytesFromFile: ");
    	Util.IERR("NOT IMPLEMENTED");
    	return null;
    }
    
    /// Load or add a ClassFile depending on the Option.compilerMode
    /// @param bytes the ClassFile bytes
    /// @throws IOException if something went wrong
    protected void loadOrAddClassFile(byte[] bytes) throws IOException {
    	if(bytes != null) {
    		if(Option.compilerMode == Option.CompilerMode.simulaClassLoader) {
    			if(Global.simulaClassLoader != null) {
    				String name = Global.packetName + "." + externalIdent;
    				if(Option.verbose) Util.println(Global.sourceName + ": Begin Load ClassFile: " + name);
    				Global.simulaClassLoader.loadClass(name, bytes);
    			} else {
        			String entryName = Global.packetName + "/" + externalIdent + ".class";
        			if(Option.verbose) Util.println(Global.sourceName + ": Begin Write .jar Entry: " + entryName);
        			Global.jarFileBuilder.writeJarEntry(entryName, bytes);
    			}
    		} else {
    			String entryName = Global.packetName + "/" + externalIdent + ".class";
    			Global.jarFileBuilder.putMapEntry(entryName, bytes);    				
    		}
 			if(Option.internal.LIST_GENERATED_CLASS_FILES)
   				listGeneratedClassFile(bytes);
    	}
    	
    }

    /// Debug utility: listGeneratedClassFile.
    /// @param bytes the classFile bytes.
    /// @throws IOException if something went wrong.
	private void listGeneratedClassFile(byte[] bytes) throws IOException {
        File outputFile = new File(Global.tempClassFileDir + "\\" + Global.packetName + "\\" + externalIdent + ".class");
        outputFile.getParentFile().mkdirs();
        FileOutputStream oupt = new FileOutputStream(outputFile);
        oupt.write(bytes); oupt.flush(); oupt.close();
        if(Option.verbose) IO.println("ClassFile written to: " + outputFile + "  nBytes="+bytes.length);

        Util.doListClassFile("" + outputFile); // List generated .class file
        outputFile.delete();
	}
	
	/// Prepare the declaration list for attribute output.
	/// @param declarationList the input declarationList.
	/// @return a new prepped declarationList.
	protected DeclarationList prep(DeclarationList declarationList) {
		DeclarationList res = new DeclarationList("");
		for(Declaration decl:declarationList) {
			switch(decl.declarationKind) {
				case ObjectKind.ArrayDeclaration -> res.add(decl);
				case ObjectKind.Class -> res.add(decl);
//				case ObjectKind.PrefixedBlock -> res.add(decl);
				case ObjectKind.ExternalDeclaration -> res.add(decl);
				case ObjectKind.LabelDeclaration -> res.add(decl);
				case ObjectKind.Procedure -> res.add(decl);
//				case ObjectKind.Switch -> res.add(decl);
				case ObjectKind.ConnectionBlock -> res.add(decl);
//				case ObjectKind.CompoundStatement -> res.add(decl);
//				case ObjectKind.SubBlock -> res.add(decl);
				case ObjectKind.SimpleVariableDeclaration -> res.add(decl);
				case ObjectKind.InspectVariableDeclaration -> res.add(decl);
			}
		}
		return(res);
	}

}
