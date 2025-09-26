/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

import java.util.Iterator;

/// This class holds a for-list for a FOR-Statement.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/FOR_List.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class FOR_List implements Iterable<Boolean> {
	/// The ForList Iterator.
	FOR_ListIterator forListIterator;

	/// Create a ForList Iterator.
	/// @param forElt an array of ForElt Iterators.
	public FOR_List(final FOR_Element... forElt) {
		forListIterator = new FOR_ListIterator(forElt);
	}

	@Override
	public Iterator<Boolean> iterator() {
		return (forListIterator);
	}
}
