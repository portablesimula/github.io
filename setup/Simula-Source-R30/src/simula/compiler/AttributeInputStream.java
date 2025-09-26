
/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import simula.compiler.syntaxClass.HiddenSpecification;
import simula.compiler.syntaxClass.ProtectedSpecification;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.ArrayDeclaration;
import simula.compiler.syntaxClass.declaration.ClassDeclaration;
import simula.compiler.syntaxClass.declaration.ConnectionBlock;
import simula.compiler.syntaxClass.declaration.ExternalDeclaration;
import simula.compiler.syntaxClass.declaration.InspectVariableDeclaration;
import simula.compiler.syntaxClass.declaration.LabelDeclaration;
import simula.compiler.syntaxClass.declaration.MaybeBlockDeclaration;
import simula.compiler.syntaxClass.declaration.Parameter;
import simula.compiler.syntaxClass.declaration.PrefixedBlockDeclaration;
import simula.compiler.syntaxClass.declaration.ProcedureDeclaration;
import simula.compiler.syntaxClass.declaration.SimpleVariableDeclaration;
import simula.compiler.syntaxClass.declaration.StandardClass;
import simula.compiler.syntaxClass.declaration.UndefinedDeclaration;
import simula.compiler.syntaxClass.declaration.VirtualSpecification;
import simula.compiler.syntaxClass.expression.ArithmeticExpression;
import simula.compiler.syntaxClass.expression.AssignmentOperation;
import simula.compiler.syntaxClass.expression.BooleanExpression;
import simula.compiler.syntaxClass.expression.ConditionalExpression;
import simula.compiler.syntaxClass.expression.Constant;
import simula.compiler.syntaxClass.expression.LocalObject;
import simula.compiler.syntaxClass.expression.ObjectGenerator;
import simula.compiler.syntaxClass.expression.ObjectRelation;
import simula.compiler.syntaxClass.expression.QualifiedObject;
import simula.compiler.syntaxClass.expression.RelationalOperation;
import simula.compiler.syntaxClass.expression.RemoteVariable;
import simula.compiler.syntaxClass.expression.TextExpression;
import simula.compiler.syntaxClass.expression.TypeConversion;
import simula.compiler.syntaxClass.expression.UnaryOperation;
import simula.compiler.syntaxClass.expression.VariableExpression;
import simula.compiler.syntaxClass.statement.ActivationStatement;
import simula.compiler.syntaxClass.statement.BlockStatement;
import simula.compiler.syntaxClass.statement.ConditionalStatement;
import simula.compiler.syntaxClass.statement.ConnectionDoPart;
import simula.compiler.syntaxClass.statement.ConnectionStatement;
import simula.compiler.syntaxClass.statement.ConnectionWhenPart;
import simula.compiler.syntaxClass.statement.DummyStatement;
import simula.compiler.syntaxClass.statement.ForListElement;
import simula.compiler.syntaxClass.statement.ForStatement;
import simula.compiler.syntaxClass.statement.ForWhileElement;
import simula.compiler.syntaxClass.statement.GotoStatement;
import simula.compiler.syntaxClass.statement.InnerStatement;
import simula.compiler.syntaxClass.statement.LabeledStatement;
import simula.compiler.syntaxClass.statement.StandaloneExpression;
import simula.compiler.syntaxClass.statement.StepUntilElement;
import simula.compiler.syntaxClass.statement.SwitchStatement;
import simula.compiler.syntaxClass.statement.WhileStatement;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.ObjectList;
import simula.compiler.utilities.ObjectReferenceMap;
import simula.compiler.utilities.Util;

/// Attribute input stream.
/// 
/// Link to GitHub: <a href="https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/AttributeInputStream.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class AttributeInputStream {
	/// The module ident.
	String moduleID;
	
	/// The underlying DataInputStream.
	DataInputStream inpt;
	
	/// The jarFile being read.
	public String jarFileName;
	
	/// The Object Reference Table.
	/// Used during Attribute File Input to fixup Object References.
	public ObjectReferenceMap objectReference;
	
	/// Debug utility.
	private boolean TRACE = false; //true;

	/// Creates a new AttributeInputStream to read data from the specified InputStream.
	/// @param inpt the underlying InputStream.
	/// @param jarFileName the name of the jarFile containing this Attribute file.
	/// @throws IOException if an I/O error occurs.
    public AttributeInputStream(InputStream inpt, String jarFileName) throws IOException {
    	this.inpt = new DataInputStream(inpt);
    	this.jarFileName = jarFileName;
		objectReference = new ObjectReferenceMap();
		
		File file = new File(jarFileName);
		String name = file.getName();
		this.moduleID = name.substring(0, name.indexOf('.'));
    }

    /// Closes this AttributeInputStream.
    /// @throws IOException if an I/O error occurs.
	public void close() throws IOException { inpt.close(); }
    
	/// Reads and returns a kind code from the underlying DataInputStream.
	/// @return the kind code read.
	/// @throws IOException if an I/O error occurs.
    public int readKind() throws IOException {
    	int kind = inpt.readByte();
    	if(TRACE) IO.println("AttributeInputStream.readKind: "+kind+':'+ObjectKind.edit(kind));
    	return kind;
	}
	
    /// Reads and returns a type from the underlying DataInputStream.
    /// @return the type read.
    /// @throws IOException if an I/O error occurs.
    public Type readType() throws IOException {
		int keyWord = inpt.readUnsignedByte();
		if(keyWord == 0) {
	    	if(TRACE) IO.println("AttributeInputStream.readType: null");
			return null;
		}
		String classIdent = readString();
		Type type = new Type(keyWord,classIdent);			
    	if(TRACE) IO.println("AttributeInputStream.readType: "+type);
    	return type;
    }
	
    /// Reads and returns a boolean from the underlying DataInputStream.
    /// @return the boolean read.
    /// @throws IOException if an I/O error occurs.
    public boolean readBoolean() throws IOException {
    	boolean b = inpt.readBoolean();
    	if(TRACE) IO.println("AttributeInputStream.readBoolean: "+b);
    	return b;
    }
	
    /// Reads and returns a short from the underlying DataInputStream.
    /// @return the short read.
    /// @throws IOException if an I/O error occurs.
    public int readShort() throws IOException {
    	short i = inpt.readShort();
    	if(TRACE) IO.println("AttributeInputStream.readInt: "+i);
    	return i;
	}

    /// Reads and returns a typed constant from the underlying DataInputStream.
    /// @return the typed constant read.
    /// @throws IOException if an I/O error occurs.
    public Object readConstant() throws IOException {
    	int key = inpt.readByte();
    	if(TRACE) IO.println("AttributeInputStream.readConstant: key=" + (int)key + " \"" + key +"\"");
    	Object res = null;
		switch(key) {
			case Type.T_UNDEF:		res = null; break;
			case Type.T_BOOLEAN:	res = inpt.readBoolean(); break;
			case Type.T_CHARACTER:	res = inpt.readChar(); break;
			case Type.T_INTEGER:	res = inpt.readShort(); break;
			case Type.T_REAL:		res = inpt.readFloat(); break;
			case Type.T_LONG_REAL:	res = inpt.readDouble(); break;
			case Type.T_TEXT:		res = readString(); break;
			default: throw new RuntimeException("key = "+key);
		}
    	if(TRACE) IO.println("AttributeInputStream.readDouble: "+res);
    	return res;
	}

    /// Reads and returns a String from the underlying DataInputStream.
    /// @return the String read.
    /// @throws IOException if an I/O error occurs.
    public String readString() throws IOException {
    	int lng = inpt.readShort()-1;
    	if(lng < 0) {
        	if(TRACE) IO.println("AttributeInputStream.readString: null");
    		return null;
    	}
    	StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lng; i++) sb.append(inpt.readChar());
    	String s = sb.toString();
    	if(TRACE) IO.println("AttributeInputStream.readString: \""+s+'"');
    	return s;
    }

    /// Reads and returns an Object list from the underlying DataInputStream.
    /// @return the Object list read.
    /// @throws IOException if an I/O error occurs.
	public ObjectList<?> readObjectList() throws IOException {
		return ObjectList.read(this);
	}

	/// Reads and returns an Object sequence number from the underlying DataInputStream.
	/// @param obj the corresponding SyntaxClass object.
	/// @return the Object sequence number read.
	/// @throws IOException if an I/O error occurs.
    public int readSEQU(SyntaxClass obj) throws IOException {
    	int OBJECT_SEQU = inpt.readShort();
    	if(TRACE) IO.println("AttributeInputStream.readSEQU: " + OBJECT_SEQU + "  ====>  " + obj.getClass().getSimpleName());
		objectReference.put(OBJECT_SEQU, obj);
    	return OBJECT_SEQU;
	}
    
    /// Reads and returns an Object from the underlying DataInputStream.
    /// @return the Object read.
    /// @throws IOException if an I/O error occurs.
	public SyntaxClass readObj() throws IOException {
		int kind = readKind();
		switch(kind) {
		case ObjectKind.NULL:
			if(TRACE) IO.println("AttributeInputStream.readObj: null");
			return null;
		case ObjectKind.ObjectReference:
			int OBJECT_SEQU = inpt.readShort();
			if(TRACE) IO.println("AttributeInputStream.readObj: OBJECT_SEQU="+OBJECT_SEQU);
			SyntaxClass obj = objectReference.get(OBJECT_SEQU);
			Util.ASSERT(obj != null, "Invariant: OBJECT_SEQU="+moduleID+"#"+OBJECT_SEQU);
			if(TRACE) IO.println("AttributeInputStream.readObj: "+obj);
			return(obj);
		default:
			obj = readObj(kind,this);
			Util.ASSERT(obj.OBJECT_SEQU != 0, "Invariant: OBJECT_SEQU="+obj.OBJECT_SEQU);
			objectReference.put(obj.OBJECT_SEQU, obj);
			obj.OBJECT_SEQU = 0;
			if(TRACE)
				IO.println("AttributeInputStream.readObj: "+moduleID+"#"+obj.OBJECT_SEQU+" obj="+obj);
			return(obj);
		}	
	}

	/// Read and return an object.
	/// @param kind the object kind code.
	/// @param inpt the AttributeInputStream to read from.
	/// @return the object read from the stream.
	/// @throws IOException if something went wrong.
	private SyntaxClass readObj(int kind,AttributeInputStream inpt) throws IOException {
		switch(kind) {
			case ObjectKind.NULL:						return null;
			
			case ObjectKind.StandardClass:				return StandardClass.readObject(inpt);
			case ObjectKind.ConnectionBlock:			return ConnectionBlock.readObject(inpt);
			case ObjectKind.CompoundStatement:			return MaybeBlockDeclaration.readObject(inpt,ObjectKind.CompoundStatement);
			case ObjectKind.SubBlock:					return MaybeBlockDeclaration.readObject(inpt,ObjectKind.SubBlock);
			case ObjectKind.Procedure:					return ProcedureDeclaration.readObject(inpt);
//			case ObjectKind.Switch:						return SwitchDeclaration.readObject(inpt);
//			case ObjectKind.MemberMethod:				return MemberMethod.readObject(inpt);
//			case ObjectKind.ContextFreeMethod:			return ContextFreeMethod.readObject(inpt);
			case ObjectKind.Class:						return ClassDeclaration.readObject(inpt);
			case ObjectKind.PrefixedBlock:				return PrefixedBlockDeclaration.readObject(inpt);
//			case ObjectKind.SimulaProgram:				return SimulaProgram.readObject(inpt);
			case ObjectKind.ArrayDeclaration:			return ArrayDeclaration.readObject(inpt);
			case ObjectKind.VirtualSpecification:		return VirtualSpecification.readObject(inpt);
//			case ObjectKind.VirtualMatch:				return VirtualMatch.readObject(inpt);
			case ObjectKind.Parameter:					return Parameter.readObject(inpt);
//			case ObjectKind.Thunk:						return Thunk.readObject(inpt);
			case ObjectKind.LabelDeclaration:			return LabelDeclaration.readObject(inpt);
			case ObjectKind.SimpleVariableDeclaration:	return SimpleVariableDeclaration.readObject(inpt);
			case ObjectKind.InspectVariableDeclaration:	return InspectVariableDeclaration.readObject(inpt);
			case ObjectKind.ExternalDeclaration:		return ExternalDeclaration.readObject(inpt);
			case ObjectKind.HiddenSpecification:		return HiddenSpecification.readObject(inpt);
			case ObjectKind.ProtectedSpecification:		return ProtectedSpecification.readObject(inpt);
			case ObjectKind.UndefinedDeclaration:		return UndefinedDeclaration.readObject(inpt);

			case ObjectKind.ActivationStatement:		return ActivationStatement.readObject(inpt);
			case ObjectKind.BlockStatement:				return BlockStatement.readObject(inpt);
			case ObjectKind.ConditionalStatement:		return ConditionalStatement.readObject(inpt);
			case ObjectKind.ConnectionStatement:		return ConnectionStatement.readObject(inpt);
			case ObjectKind.ConnectionDoPart:			return ConnectionDoPart.readObject(inpt);
			case ObjectKind.ConnectionWhenPart:			return ConnectionWhenPart.readObject(inpt);
			case ObjectKind.DummyStatement:				return DummyStatement.readObject(inpt);
			case ObjectKind.ForStatement:				return ForStatement.readObject(inpt);
			case ObjectKind.ForListElement:				return ForListElement.readObject(inpt);
			case ObjectKind.ForWhileElement:			return ForWhileElement.readObject(inpt);
			case ObjectKind.StepUntilElement:			return StepUntilElement.readObject(inpt);
			case ObjectKind.GotoStatement:				return GotoStatement.readObject(inpt);
//			case ObjectKind.InlineStatement:			return InlineStatement.readObject(inpt);
			case ObjectKind.InnerStatement:				return InnerStatement.readObject(inpt);
			case ObjectKind.LabeledStatement:			return LabeledStatement.readObject(inpt);
//			case ObjectKind.ProgramModule:				return ProgramModule.readObject(inpt);
			case ObjectKind.StandaloneExpression:		return StandaloneExpression.readObject(inpt);
			case ObjectKind.SwitchStatement:			return SwitchStatement.readObject(inpt);
			case ObjectKind.WhileStatement:				return WhileStatement.readObject(inpt);

			case ObjectKind.ArithmeticExpression:		return ArithmeticExpression.readObject(inpt);
			case ObjectKind.AssignmentOperation:		return AssignmentOperation.readObject(inpt);
			case ObjectKind.BooleanExpression:			return BooleanExpression.readObject(inpt);
			case ObjectKind.ConditionalExpression:		return ConditionalExpression.readObject(inpt);
			case ObjectKind.Constant:					return Constant.readObject(inpt);
			case ObjectKind.LocalObject:				return LocalObject.readObject(inpt);
			case ObjectKind.ObjectGenerator:			return ObjectGenerator.readObject(inpt);
			case ObjectKind.ObjectRelation:				return ObjectRelation.readObject(inpt);
			case ObjectKind.QualifiedObject:			return QualifiedObject.readObject(inpt);
			case ObjectKind.RelationalOperation:		return RelationalOperation.readObject(inpt);
			case ObjectKind.RemoteVariable:				return RemoteVariable.readObject(inpt);
			case ObjectKind.TextExpression:				return TextExpression.readObject(inpt);
			case ObjectKind.TypeConversion:				return TypeConversion.readObject(inpt);
			case ObjectKind.UnaryOperation:				return UnaryOperation.readObject(inpt);
			case ObjectKind.VariableExpression:			return VariableExpression.readObject(inpt);

		}	
		Util.IERR("IMPOSSIBLE "+ObjectKind.edit(kind));
		return(null);
	}


}
