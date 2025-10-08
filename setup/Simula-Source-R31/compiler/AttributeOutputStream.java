/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.ObjectList;
import simula.compiler.utilities.Util;

/// Attribute output stream.
/// 
/// Link to GitHub: <a href="https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/AttributeOutputStream.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class AttributeOutputStream {
	/// The underlying DataOutputStream.
	DataOutputStream oupt;

	/// Debug utility.
	private boolean TRACE = false; //true;

	/// Creates a new AttributeOutputStream to write data to the specified OutputStream.
	/// @param oupt the underlying OutputStream.
	/// @throws IOException if an I/O error occurs.
    public AttributeOutputStream(OutputStream oupt) throws IOException {
    	this.oupt = new DataOutputStream(oupt);
    }

    /// Closes this AttributeOutputStream.
    /// @throws IOException if an I/O error occurs.
	public void close() throws IOException { oupt.flush(); oupt.close(); }

	/// Writes a kind code to the underlying DataOutputStream.
	/// @param i a kind code to be written.
	/// @throws IOException if an I/O error occurs.
    public void writeKind(int i) throws IOException {
		if(TRACE) IO.println("AttributeOutputStream.writeKind: "+i+':'+ObjectKind.edit(i));
		if(i > ObjectKind.MAX_VALUE || i < 0) throw new IllegalArgumentException("Argument = "+i);
		oupt.writeByte(i);
	}

    /// Writes a type to the underlying DataOutputStream.
    /// @param type a type to be written.
    /// @throws IOException if an I/O error occurs.
    public void writeType(Type type) throws IOException {
		if(TRACE) IO.println("AttributeOutputStream.writeType: "+type);
		if(type == null)
			oupt.writeByte(0);
		else {
			oupt.writeByte(type.keyWord);
			writeString(type.classIdent);
		}
	}
	
    /// Writes a boolean to the underlying DataOutputStream.
    /// @param b a boolean to be written.
    /// @throws IOException if an I/O error occurs.
    public void writeBoolean(boolean b) throws IOException {
		if(TRACE) IO.println("AttributeOutputStream.writeBoolean: "+b);
		oupt.writeBoolean(b);
	}

    /// Writes a short to the underlying DataOutputStream.
    /// @param i a short to be written.
    /// @throws IOException if an I/O error occurs.
    public void writeShort(int i) throws IOException {
		if(TRACE) IO.println("AttributeOutputStream.writeInt: "+i);
		if(i > Short.MAX_VALUE || i < Short.MIN_VALUE) throw new IllegalArgumentException("Argument = "+i);
		oupt.writeShort(i);			
	}

    /// Writes a typed constant to the underlying DataOutputStream.
    /// @param c a typed constant to be written.
    /// @throws IOException if an I/O error occurs.
    public void writeConstant(Object c) throws IOException {
		if(TRACE) IO.println("AttributeOutputStream.writeConstant: "+c);
		if(c == null)						{ oupt.writeByte(Type.T_UNDEF); }
		else if(c instanceof Boolean b)		{ oupt.writeByte(Type.T_BOOLEAN);   oupt.writeBoolean(b);	}
		else if(c instanceof Integer i)		{ oupt.writeByte(Type.T_INTEGER);   oupt.writeShort(i);	}
		else if(c instanceof Long li)		{ oupt.writeByte(Type.T_INTEGER);   oupt.writeShort(li.intValue()); }
		else if(c instanceof Character k)	{ oupt.writeByte(Type.T_CHARACTER); oupt.writeChar(k); }
		else if(c instanceof Float f)		{ oupt.writeByte(Type.T_REAL);      oupt.writeFloat(f); }
		else if(c instanceof Double f)		{ oupt.writeByte(Type.T_LONG_REAL); oupt.writeDouble(f); }
		else if(c instanceof String s)		{ oupt.writeByte(Type.T_TEXT);      writeString(s); }
		else Util.IERR(""+c.getClass().getSimpleName());
	}

    /// Writes a String to the underlying DataOutputStream.
    /// @param s a String to be written.
    /// @throws IOException if an I/O error occurs.
    public void writeString(String s) throws IOException {
		if(TRACE) IO.println("AttributeOutputStream.writeString: "+s);
		if(s == null) oupt.writeShort(0);
		else {
			int lng = s.length();
			oupt.writeShort(lng+1);
			for(int i=0;i<lng;i++) oupt.writeChar(s.charAt(i));
		}
	}

    /// Writes a Object list to the underlying DataOutputStream.
    /// @param list a Object list to be written.
    /// @throws IOException if an I/O error occurs.
	public void writeObjectList(ObjectList<?> list) throws IOException {
		ObjectList.write(list, this);
	}

	/// Writes a Object to the underlying DataOutputStream.
	/// @param obj a Object to be written.
	/// @throws IOException if an I/O error occurs.
    public void writeObj(SyntaxClass obj) throws IOException {
		if(obj == null) {
			if(TRACE) IO.println("AttributeOutputStream.writeObj: null");
			writeKind(ObjectKind.NULL);
		} else if(obj.OBJECT_SEQU != 0) {
			if(TRACE) IO.println("AttributeOutputStream.writeObj: ObjectReference "+(obj.OBJECT_SEQU));
			writeKind(ObjectKind.ObjectReference);
			oupt.writeShort(obj.OBJECT_SEQU);
		} else {
			obj.OBJECT_SEQU = Global.Object_SEQU++;
			if(TRACE) IO.println("AttributeOutputStream.writeObj: OBJECT_SEQU="+obj.OBJECT_SEQU+": "+obj.getClass().getSimpleName()+"  "+obj);
			obj.writeObject(this);
		}
    }

}