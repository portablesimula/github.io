/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.utilities;

import java.io.IOException;
import java.util.Vector;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.syntaxClass.SyntaxClass;

/// Utility class to hold a list of objects.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/ObjectList.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
/// @param <E> the element type.
@SuppressWarnings("serial")
public class ObjectList<E> extends Vector<E> {
	
	/// Default constructor.
	public ObjectList() {}
	
	@SuppressWarnings("unchecked")
	public boolean add(Object obj) {
		return super.add((E) obj);
	}
	
	/// Write an ObjectList to a AttributeOutputStream.
	/// @param list the list to be written.
	/// @param oupt the AttributeOutputStream to write to.
	/// @throws IOException if something went wrong.
	public static void write(ObjectList<?> list, AttributeOutputStream oupt) throws IOException {
		if(list != null) {
			oupt.writeShort(list.size());
			for(Object stm:list) oupt.writeObj((SyntaxClass) stm);
		} else oupt.writeShort(-1);
	}

	/// Read and return an ObjectList.
	/// @param inpt the AttributeInputStream to read from
	/// @return the ObjectList object read from the stream.
	/// @throws IOException if something went wrong.
	public static ObjectList<?> read(AttributeInputStream inpt) throws IOException {
		ObjectList<?> list = null;
		int n = inpt.readShort();
		if (n >= 0)
			list = new ObjectList<Object>();
		if(n > 0) {
			for (int i = 0; i < n; i++)
				list.add(inpt.readObj());
		}
		return list;
	}
	
}
