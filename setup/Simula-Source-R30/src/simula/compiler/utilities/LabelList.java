/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.utilities;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.CodeBuilder.BlockCodeBuilder;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.instruction.SwitchCase;
import java.util.List;
import java.util.Vector;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.declaration.BlockDeclaration;
import simula.compiler.syntaxClass.declaration.ClassDeclaration;
import simula.compiler.syntaxClass.declaration.DeclarationScope;
import simula.compiler.syntaxClass.declaration.LabelDeclaration;

/// A list of LabelDeclarations.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/LabelList.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class LabelList {
	/// Debug utility.
	private static boolean TRACING = false;
	
	/// Utility to help make unique sequence numbers.
	private static int LABEL_SEQU = 0;
	
	/// The sequence number of this label list.
	private int sequ;
	
	/// The owner of this label list.
	private DeclarationScope declaredIn;
	
	/// The labels in this list.
	private Vector<LabelDeclaration> declaredLabels;
	
	/// true: this LabelList is ready for coding.
	private boolean READY_FOR_CODING;
	
	/// Set by MAKE_READY_FOR_CODING  or  accumLabelList
	private Vector<LabelDeclaration> accumLabels;
	
	/// beginning of the default handler block. Set by MAKE_READY_FOR_CODING
	private Label defaultTarget;
	
	/// Set by MAKE_READY_FOR_CODING
	private Vector<SwitchCase> tableSwitchCases;
	
	/// A list of LabelDeclarations.
	/// @param declaredIn the DeclarationScope owning this list.
	public LabelList(DeclarationScope declaredIn) {
		if(declaredIn == null) Util.IERR();
		this.sequ = (LABEL_SEQU++);
		this.declaredIn = declaredIn;
		this.declaredLabels = new Vector<LabelDeclaration>();
		if(TRACING) IO.println("NEW " + this);
	}
	
	/// Clears this list.
	public void clear() {
		tableSwitchCases = null;
		READY_FOR_CODING = false;
	}
	
	/// Debug utility: Return identification String.
	/// @return identification String.
	private String ident() {
		return "LabelList["+sequ+':'+declaredIn.identifier+"]";
	}
	
	/// Get DeclaredLabels
	/// @return DeclaredLabels
	public Vector<LabelDeclaration> getDeclaredLabels(){
		return declaredLabels;
	}
	
	/// Get all labels. Accumulated through the prefix chain for classes and prefixed blocks.
	/// @return a Vector of LabelDeclarations
	public Vector<LabelDeclaration> getAccumLabels(){
		return accumLabels;
	}
	
	/// Get number of accumulated labels. Accumulated through the prefix chain for classes and prefixed blocks.
	/// @return number of accumulated labels.
	public int accumLabelSize() {
		return (accumLabels == null)?0:accumLabels.size();
	}
	
	/// Get number of local labels.
	/// @return number of local labels.
	public int declaredLabelSize() {
		return (declaredLabels == null)?0:declaredLabels.size();
	}
	
	/// Get last declared local label with the given ident.
	/// @param ident label identifier
	/// @return number of accumulated labels.
	public LabelDeclaration getLastDeclaredLabel(String ident) {
		int n = declaredLabelSize();
		if(n > 0) {
			for(int i=n-1;i>=0;i--) {
				LabelDeclaration lab = declaredLabels.get(i);
				if(lab.identifier.equalsIgnoreCase(ident)) return lab;
			}
		}
		return null;
	}
	
	/// Add a label to the local label list. 
	/// @param lab the label to add.
	public void add(LabelDeclaration lab) {
		if(TRACING) IO.println(ident()+".add: "+lab.identifier+'['+lab.externalIdent+']');
		if(READY_FOR_CODING) Util.IERR("Can't add a new Label when LabelLisit is marked READY_FOR_CODING");
		declaredLabels.add(lab);
		if(TRACING) IO.println(ident()+".add: DONE: LabelList = "+this);
	}
	
	/// Accumulate the label list for the given block. 
	/// accumulated through the prefix chain for classes and prefixed blocks.
	/// 
	/// This method is used when generating .java source.
	/// @param blk the given BlockDeclaration.
	public static void accumLabelList(BlockDeclaration blk) {
		Vector<LabelDeclaration> accumLabels = new Vector<LabelDeclaration>();
		if(blk instanceof ClassDeclaration cls) {
			ClassDeclaration prefix = cls.prefixClass;
			while(prefix != null) {
				if(prefix.labelList != null) { 
					for(LabelDeclaration lab:prefix.labelList.declaredLabels)
						accumLabels.add(lab);
				}
				prefix = prefix.prefixClass;
			}
		}
		if(blk.labelList != null) 
			for(LabelDeclaration lab:blk.labelList.declaredLabels)
				accumLabels.add(lab);
		if(blk.labelList == null) blk.labelList = new LabelList(blk);
		blk.labelList.accumLabels = accumLabels;
	}
	
	/// Get a List of SwitchCase suitable for the 'tableswitch' instruction.
	/// 
	/// This method is used when generating classFile using the codeBuilder.
	/// @param codeBuilder the codeBuilder
	/// @return a List of SwitchCase
	public List<SwitchCase> getTableSwitchCases(CodeBuilder codeBuilder) {
		if(!READY_FOR_CODING) MAKE_READY_FOR_CODING(codeBuilder);
		if(TRACING) IO.println(ident()+".getTableSwitchCases: "+this);
		if(TRACING) IO.println("LabelList.getTableSwitchCases: "+this);
		if(tableSwitchCases == null) Util.IERR();
		return tableSwitchCases;
	}

	/// Sets the label indexes suitable for the 'tableswitch' instruction.
	/// 
	/// This method is used when generating classFile using the codeBuilder.
	public void setLabelIdexes() {
		if(accumLabels != null) {
			int nextIndex = 1;
			for (LabelDeclaration label : accumLabels) label.index = nextIndex++;
		}
	}
	
	/// ClassFile coding utility: Make this LabelList ready for coding.
	/// @param codeBuilder the codeBuilder to use
	private void MAKE_READY_FOR_CODING(CodeBuilder codeBuilder) {
		if(READY_FOR_CODING) return;
		if(TRACING) IO.println("\n" + ident() +".MAKE_READY_FOR_CODING: "+this);
		if(accumLabelSize() > 0) {
			defaultTarget = codeBuilder.newLabel();
			tableSwitchCases = new Vector<SwitchCase>();
			if(TRACING) IO.println(ident()+".MAKE_READY_FOR_CODING: nLabels="+accumLabels.size());
			for (int i = 1; i <= accumLabels.size(); i++) {
				Label lab = codeBuilder.newLabel();
				tableSwitchCases.add(SwitchCase.of(i, lab));
			}
		}			
		READY_FOR_CODING = true;
	}
	
	/// ClassFile coding utility: Build the TableSwitch Instruction.
	/// @param codeBuilder the codeBuilder to use
	public void build_JUMPTABLE(BlockCodeBuilder codeBuilder) {
		if(!READY_FOR_CODING) MAKE_READY_FOR_CODING(codeBuilder);
		if(TRACING) IO.println(ident()+".build_JUMPTABLE: "+this);
		// *******************************************************************************
		// *** JUMPTABLE Case
		// *******************************************************************************
		// iconst_n // Number of cases (tableSize)
		// invokestatic _JUMPTABLE
		//
		// Output:
		//
		// tableswitch ...
		//
		// *******************************************************************************
		// Build the TableSwitch Instruction
		int lowValue = 1;            // the minimum key value.
		int highValue = accumLabelSize(); // the maximum key value.
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		codeBuilder
			.aload(0)
			.getfield(RTS.FRE.RTObject_JTX(pool))
			.tableswitch(lowValue, highValue, defaultTarget, this.getTableSwitchCases(codeBuilder))
			.labelBinding(defaultTarget);
	}
	
	/// ClassFile coding utility: Build a labelBinding.
	/// @param label the label to bind
	/// @param codeBuilder the codeBuilder to use
	public void labelBinding(LabelDeclaration label,CodeBuilder codeBuilder) {
		if(!READY_FOR_CODING) MAKE_READY_FOR_CODING(codeBuilder);
		BlockDeclaration labelContext = BlockDeclaration.labelContext;
		LabelList currentList = labelContext.labelList;
		if(TRACING) IO.println(ident()+".labelBinding: labelContext="+labelContext+", codeBuilder="+codeBuilder);
		if(TRACING) IO.println(ident()+".labelBinding: currentList="+currentList);
		SwitchCase switchCase=currentList.tableSwitchCases.get(label.index-1);
		if(TRACING) IO.println(ident()+".labelBinding: "+label+"   SwitchCase="+switchCase);
		codeBuilder.labelBinding(switchCase.target());
	}

	/// Debug utility: Print Syntax tree
	/// @param indent indentation
	/// @param owner the BlockDeclaration owning this LabelList
	public void printTree(final int indent, final BlockDeclaration owner) {
		if(Option.internal.PRINT_SYNTAX_TREE > 2) {
			IO.println(SyntaxClass.edIndent(indent)+this);
		} else {
			IO.println(SyntaxClass.edIndent(indent) + "LabelList with " + (declaredLabels.size()) + " DeclaredLabels ...");
		}
	}
	
	/// Debug utility: print the LabelList
	/// @param title title String
	public void print(String title) {
		IO.println("\n************ BEGIN LabelList[" +sequ + "]: "+title+" ************");
		IO.println("*** DeclaredIn: "+declaredIn.identifier+"  READY_FOR_CODING="+READY_FOR_CODING);
		System.out.print("*** DeclaredLabels:");
		if(declaredLabelSize() > 0) {
			for(LabelDeclaration lab:getDeclaredLabels()) {
				System.out.print(" " + lab.identifier + '[' + lab.declaredIn.externalIdent + ':' + lab.index + ']' + "atLine:" + lab.lineNumber);
			}
			IO.println("");
		} else IO.println(" NONE");
		System.out.print("*** AccumLabels:   ");
		if(accumLabelSize() > 0) {
			for(LabelDeclaration lab:getAccumLabels()) {
				System.out.print(" " + lab.identifier + '[' + lab.declaredIn.externalIdent + ':' + lab.index + ']' + "atLine:" + lab.lineNumber);
			}
			IO.println("");
		} else IO.println(" NONE");
		IO.println("*** DefaultTarget:  "+defaultTarget);
		if(tableSwitchCases != null) {
			for(SwitchCase swc:tableSwitchCases) {
				IO.println("*** SwitchCase:     "+swc);
			}
		}
		IO.println("************ ENDOF LabelList[" +sequ + "]: "+title+" ************\n");
	}
	
	public String toString() {
		String s = "LabelList[" +sequ + "]:";
		String sep = " ";
		if(declaredLabelSize() > 0) {
			for(LabelDeclaration lab:declaredLabels) {
				s = s + sep + lab.identifier + '[' + lab.declaredIn.externalIdent + ':' + lab.index + ']';				
				s = s + "atLine:" + lab.lineNumber;
				sep = " ";
			}
		} else s = s + " With no labels";
		return(s);
	}
	

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************

	/// Write a LabelList to a AttributeOutputStream.
	/// @param labelList the LabelList to write.
	/// @param oupt the AttributeOutputStream to write to.
	/// @throws IOException if something went wrong.
	public static void writeLabelList(LabelList labelList,AttributeOutputStream oupt) throws IOException {
		if(labelList == null) {
			oupt.writeBoolean(false);
		} else {
			oupt.writeBoolean(true);
			Util.TRACE_OUTPUT(""+labelList);
			oupt.writeObj(labelList.declaredIn);
			oupt.writeShort(labelList.declaredLabelSize());			
			for(LabelDeclaration lab:labelList.declaredLabels) {
				oupt.writeObj(lab);
			}
		}
	}

	/// Read and return a LabelList.
	/// @param inpt the AttributeInputStream to read from
	/// @return the LabelList object read from the stream.
	/// @throws IOException if something went wrong.
	public static LabelList readLabelList(AttributeInputStream inpt) throws IOException {
		boolean present = inpt.readBoolean();
		LabelList labelList = null;
		if(present) {
			DeclarationScope declaredIn = (DeclarationScope) inpt.readObj();
			labelList = new LabelList(declaredIn);
			int n = inpt.readShort();
			if(TRACING)
				IO.println("LabelList.readLabelList: Read Label List: "+n);
			for(int i=0;i<n;i++) {
				LabelDeclaration lab = (LabelDeclaration) inpt.readObj();
				labelList.add(lab);
			}
		}
		Util.TRACE_INPUT("LabelList: " + labelList);
		return(labelList);
	}

}
