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
import simula.compiler.syntaxClass.declaration.Declaration;

/// Declaration List.
/// 
/// This is a utility class to hold local declaration lists in declaration scopes.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/DeclarationList.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
@SuppressWarnings("serial")
public final class DeclarationList extends Vector<Declaration> {
	
	/// Identifier.
	public final String identifier;

	/// Create a new DeclarationList.
	/// @param identifier the given identifier
	public DeclarationList(String identifier) {
		this.identifier=identifier;
	}
	
	/// Find a declaration in this DeclarationList
	/// @param identifier declaration identifier
	/// @return the resulting Declaration
	public Declaration find(String identifier) {
		for(Declaration d:this)
			if(d.identifier.equals(identifier)) return(d);
		return(null);
	}
	
	/// Add a declaration to this list.
	@Override
	public boolean add(Declaration dcl) {
		Declaration d=find(dcl.identifier);
		if(d!=null) {
			Util.warning("Multiple declarations with the same name: "+dcl.identifier);
			return(false);			
		}
		super.addElement(dcl);
		return(true);
	}

	/// Utility print method.
	/// @param title the title
	public void print(String title) {
		Util.println("DeclarationList: "+identifier+" -- "+title);
		for(Declaration decl:this) Util.println(decl.toString());
	}
	
	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	
	/// Write a DeclarationList object to a AttributeOutputStream.
	/// @param oupt the AttributeOutputStream to write to.
	/// @throws IOException if something went wrong.
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		oupt.writeString(identifier);
		oupt.writeShort(this.size());
		for(Declaration dcl:this) oupt.writeObj(dcl);
	}

	/// Read and return a DeclarationList object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the DeclarationList object read from the stream.
	/// @throws IOException if something went wrong.
	public static DeclarationList readObject(AttributeInputStream inpt) throws IOException {
		String identifier = inpt.readString();
		DeclarationList list = new DeclarationList(identifier);
		int n = inpt.readShort();
		if(n > 0) {
			for (int i = 0; i < n; i++) {
				Declaration dcl = (Declaration) inpt.readObj();
				list.add(dcl);
			}
		}
		return list;
	}

}
