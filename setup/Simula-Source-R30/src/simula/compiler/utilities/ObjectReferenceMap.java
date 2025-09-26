/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.utilities;

import java.util.HashMap;
import java.util.Map;

import simula.compiler.syntaxClass.SyntaxClass;

/// This ObjectReferenceMap is used to support the Attribute input stream.
///
/// Efforts are made to ensure that only one copy of each object read, is created.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/ObjectReferenceMap.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class ObjectReferenceMap {
	
	/// The underlying ObjectReference Map
	Map<Integer,SyntaxClass> objectReference;
	
	/// Construct a ObjectReferenceMap with an underlying HashMap.
	public ObjectReferenceMap() {
		objectReference = new HashMap<Integer,SyntaxClass>();
	}
	
	/// Associates the specified value with the specified key in this map using the underlying HashMap.
	/// @param key with which the specified value is to be associated
	/// @param value to be associated with the specified key
	public void put(Integer key, SyntaxClass value) {
		if(key == null) Util.IERR();
		if(value == null) Util.IERR();
		objectReference.put(key, value);
	}
	
	/// Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
	/// @param key the key whose associated value is to be returned
	/// @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
	public SyntaxClass get(int key) {
		return(objectReference.get(key));
	}
	
	/// Utility method: print
	public void print() {
		IO.println("ObjectReferenceMap.print: BEGIN Print OBJECT REFERENCE TABLE:");
		Map<Integer, SyntaxClass> map = objectReference;
		map.forEach((key, value) -> IO.println("   " + key + "  <==>  " + value));
		IO.println("ObjectReferenceMap.print: ENDOF Print OBJECT REFERENCE TABLE:");
	}

}
