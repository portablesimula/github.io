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
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.JavaSourceFileCoder;
import simula.compiler.syntaxClass.ProtectedSpecification;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.expression.Constant;
import simula.compiler.syntaxClass.expression.Expression;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Label Declaration.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/LabelDeclaration.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class LabelDeclaration extends SimpleVariableDeclaration {
	/// The label index. Set by BlockDeclaration.doAccumLabels.
	public int index;

	/// Special case: Labels in a CompoundStatement or ConnectionBlock are moved to
	/// nearest enclosing Block which is not a CompoundStatement or ConnectionBlock.
	public DeclarationScope movedTo;
	
	/// Indicates that codeBuilder.labelBinding is called.
	public boolean isBinded;
	

	/// Create a new Label Declaration.
	/// 
	/// @param identifier label identifier
	public LabelDeclaration(final String identifier) {
		super(Type.Label, identifier);
		this.externalIdent = "_LABEL_" + identifier;
		this.declarationKind = ObjectKind.LabelDeclaration;
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())
			return;
		Global.sourceLineNumber = lineNumber;
		DeclarationScope declaredIn = Global.getCurrentScope();
		this.externalIdent = "_LABEL_" + declaredIn.externalIdent + '_' + identifier + '_' + declaredIn.prefixLevel();
		type.doChecking(declaredIn);
		VirtualSpecification virtSpec = VirtualSpecification.getVirtualSpecification(this);
		if (virtSpec == null) {
			// Label attributes are implicit specified 'protected'
			if (declaredIn.declarationKind == ObjectKind.Class)
				((ClassDeclaration) declaredIn).protectedList
						.add(new ProtectedSpecification((ClassDeclaration) declaredIn, identifier));
		} else {
			// This Label is a Virtual Match
			ClassDeclaration decl = (ClassDeclaration) declaredIn;
			if (decl == virtSpec.declaredIn)
				virtSpec.hasDefaultMatch = true;
		}
		SET_SEMANTICS_CHECKED();
	}
	
	/// Declare a local Label.
	/// @param encloser the BlockDeclaration to update.
	public void declareLocalLabel(BlockDeclaration encloser) {
		Global.sourceLineNumber = lineNumber;
		String ident = getJavaIdentifier();
		int prefixLevel=0;
		if(movedTo != null) {
			if(movedTo instanceof ClassDeclaration cls) prefixLevel=cls.prefixLevel();
		} else {
			if(declaredIn instanceof ClassDeclaration cls) prefixLevel=cls.prefixLevel();			
		}
		VirtualSpecification virtSpec = VirtualSpecification.getVirtualSpecification(this);
		if (virtSpec != null) {
			if(this.isLatestVirtualLabel(encloser)) {
				JavaSourceFileCoder.code("    public RTS_LABEL " + virtSpec.getVirtualIdentifier()
					+ " { return(new RTS_LABEL(this," + prefixLevel + ',' + index + ",\"" + identifier + "\")); }",
					" // Virtual Label #" + index + '=' + identifier + " At PrefixLevel " + prefixLevel);
			}
		} else {
			JavaSourceFileCoder.code(
					"final RTS_LABEL " + ident + "=new RTS_LABEL(this," +prefixLevel + ',' + index + ",\"" + identifier + "\");",
					"Local Label #" + index + '=' + identifier + " At PrefixLevel " + prefixLevel);
		}
	}

	@Override
	public void buildDeclaration(ClassBuilder classBuilder,BlockDeclaration encloser) {
		String ident = getFieldIdentifier();
		int prefixLevel = getPrefixLevel();
		
		VirtualSpecification virtSpec = VirtualSpecification.getVirtualSpecification(this);
		if (virtSpec != null) {
			if(this.isLatestVirtualLabel(encloser)) {
				MethodTypeDesc MTD_STM=MethodTypeDesc.ofDescriptor("()Lsimula/runtime/RTS_LABEL;");
				classBuilder
					.withMethodBody(virtSpec.getSimpleVirtualIdentifier(), MTD_STM, ClassFile.ACC_PUBLIC,
						codeBuilder -> buildVirtualMatchMethodBody(prefixLevel,codeBuilder));
			}
		} else {
			classBuilder.withField(ident, RTS.CD.RTS_LABEL, ClassFile.ACC_PUBLIC);
		}
	}
	
	/// Check if this label is the last label in the owner's label list.
	/// @param encloser the owner.
	/// @return true: if this label is the last label in the owner's label list.
	private boolean isLatestVirtualLabel(DeclarationScope encloser) {
		LabelDeclaration last = encloser.labelList.getLastDeclaredLabel(this.identifier);
		if(this.index == last.index) {
			return true;
		}
		return false;
	}
	
	/// Returns the prefix level.
	/// @return the prefix level.
	private int getPrefixLevel() {
		int prefixLevel=0;
		if(movedTo != null) {
			if(movedTo instanceof ClassDeclaration cls) prefixLevel=cls.prefixLevel();
		} else {
			if(declaredIn instanceof ClassDeclaration cls) prefixLevel=cls.prefixLevel();			
		}
		return prefixLevel;
	}
	
	/// ClassFile coding utility: Build Virtual Match Method Body.
	/// @param prefixLevel the prefix level.
	/// @param codeBuilder the codeBuilder to use.
	private void buildVirtualMatchMethodBody(int prefixLevel,CodeBuilder codeBuilder) {
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		// Build virtual match method:
		// public RTS_LABEL " + virtSpec.getVirtualIdentifier()
		// { return(new RTS_LABEL(this, prefixLevel, index, "identifier")); }
		codeBuilder
			.new_(RTS.CD.RTS_LABEL)
			.dup()
			.aload(0); // this
		Constant.buildIntConst(codeBuilder, prefixLevel);
		Constant.buildIntConst(codeBuilder, index);
		codeBuilder.ldc(pool.stringEntry(this.identifier));
		codeBuilder
			.invokespecial(RTS.CD.RTS_LABEL, "<init>", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_RTObject;IILjava/lang/String;)V"))
			.areturn();
	}


	@Override
	public FieldRefEntry getFieldRefEntry(ConstantPoolBuilder pool) {
		DeclarationScope declaredIn = (movedTo != null)? movedTo : this.declaredIn;
		ClassDesc owner=declaredIn.getClassDesc();
		return(pool.fieldRefEntry(owner, getFieldIdentifier(), RTS.CD.RTS_LABEL));
	}
	
	@Override
	public String getFieldIdentifier() {
		return(this.externalIdent);
	}

	public void buildInitAttribute(CodeBuilder codeBuilder) {
		VirtualSpecification virtSpec = VirtualSpecification.getVirtualSpecification(this);
		if (virtSpec == null) {
			ConstantPoolBuilder pool=codeBuilder.constantPool();
			buildLabelQuant(codeBuilder);
			codeBuilder.putfield(getFieldRefEntry(pool));
		}
	}
	
	/// Build binding for this Label.
	/// @param codeBuilder the codeBuilder to use.
	public void doBind(CodeBuilder codeBuilder) {
		if(isBinded) Util.IERR();
		BlockDeclaration labelContext = BlockDeclaration.labelContext;
		labelContext.labelList.labelBinding(this,codeBuilder);
		isBinded = true;
	}
	
	/// Build Label Quantity
	/// @param codeBuilder the codeBuilder to use
	public void buildLabelQuant(CodeBuilder codeBuilder) {
		int prefixLevel=0;
		if(movedTo != null) {
			if(movedTo instanceof ClassDeclaration cls) prefixLevel=cls.prefixLevel();
		} else {
			if(declaredIn instanceof ClassDeclaration cls) prefixLevel=cls.prefixLevel();			
		}

		// new RTS_LABEL(this,0,1,"L1"); // Local Label #1=L1 At PrefixLevel 0
		codeBuilder
			.aload(0)
			.new_(RTS.CD.RTS_LABEL)
			.dup()
			.aload(0); // this
		Constant.buildIntConst(codeBuilder, prefixLevel);
		Constant.buildIntConst(codeBuilder, index);
		codeBuilder
			.ldc(codeBuilder.constantPool().stringEntry(identifier))
			.invokespecial(RTS.CD.RTS_LABEL, "<init>", MethodTypeDesc.ofDescriptor("(Lsimula/runtime/RTS_RTObject;IILjava/lang/String;)V"));
	}
	
	@Override
	public String toString() {
		String comment = "DeclaredIn: "+declaredIn.identifier;
		if(movedTo != null) comment = comment+" -> "+movedTo;
		return ("LABEL " + identifier + '[' + externalIdent + ']' + ", index=" + index + " IN " + comment);
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	
	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("writeLabelDeclaration: " + identifier);
		oupt.writeKind(declarationKind);
		oupt.writeString(identifier);
		oupt.writeShort(OBJECT_SEQU);

		// *** SyntaxClass
		oupt.writeShort(lineNumber);

		// *** Declaration
		oupt.writeString(identifier);
		oupt.writeString(externalIdent);
		oupt.writeType(type);// Declaration
		
		// *** SimpleVariableDeclaration
		oupt.writeBoolean(constant);
		oupt.writeObj(constantElement);

		// *** LabelDeclaration
		oupt.writeShort(index);
		oupt.writeObj(movedTo);
	}
	
	/// Read and return an object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the object read from the stream.
	/// @throws IOException if something went wrong.
	public static LabelDeclaration readObject(AttributeInputStream inpt) throws IOException {
		String identifier = inpt.readString();
		LabelDeclaration lab = new LabelDeclaration(identifier);
		lab.OBJECT_SEQU = inpt.readSEQU(lab);

		// *** SyntaxClass
		lab.lineNumber = inpt.readShort();

		// *** Declaration
		lab.identifier = inpt.readString();
		lab.externalIdent = inpt.readString();
		lab.type = inpt.readType();
		
		// *** SimpleVariableDeclaration
		lab.constant = inpt.readBoolean();
		lab.constantElement = (Expression) inpt.readObj();

		// *** LabelDeclaration
		lab.index = inpt.readShort();
		lab.movedTo = (DeclarationScope) inpt.readObj();
		Util.TRACE_INPUT("readLabelDeclaration: " + lab);
		return(lab);
	}

}
