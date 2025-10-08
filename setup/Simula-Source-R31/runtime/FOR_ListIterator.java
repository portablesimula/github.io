/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

import java.util.Iterator;

// *******************************************************
// *** FRAMEWORK for for-list iteration
// *******************************************************
/// The Implementation of the for-statement is a bit tricky.
/// The basic idea is to create a ForList iterator that iterates over a set of ForElt iterators.
/// The following subclasses of ForElt are defined:
///
/// - [FOR_SingleElt]		with basic types T control variable
/// - [FOR_SingleTValElt]	with Text type control variable
/// - [FOR_StepUntil]		with numeric types
/// - [FOR_WhileElt]		with basic types T control variable
/// - [FOR_WhileTValElt]	representing For t:= <TextExpr> while <Cond> with text value assignment
/// 
/// Each of which has a boolean method 'hasNext' indicating whether this for-element is exhausted.
/// All parameters to these classes are transferred 'by name'.
/// This is done to ensure that all expressions are evaluated in the right order.
/// The assignment to the 'control variable' is done within the various for-elements when the 'next' method is invoked.
///
///
/// Simula for-statement for-list iterator
/// 
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/FOR_ListIterator.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class FOR_ListIterator implements Iterator<Boolean> {
	/// The ForElt array.
	final FOR_Element[] forElt;
	/// Current index into the ForElt array.
	int index;

	/// Create a ForListIterator consisting of an array of ForElt Iterators.
	/// @param forElt an array of ForElt Iterators.
	public FOR_ListIterator(final FOR_Element... forElt) {
		this.forElt = forElt;
	}

    /// Returns {@code true} if the iteration has more elements.
 	/// @return {@code true} if the iteration has more elements
 	@Override
	public boolean hasNext() {
		return (index < forElt.length && forElt[index].hasNext());
	}

    /// Update the control variable with the next value if any.
    /// @return {@code true} if the control variable was updated; otherwise {@code false}.
 	@Override
	public Boolean next() {
		Boolean val = forElt[index].next();
		if (!forElt[index].hasNext())
			index++;
		return (val);
	}

}
