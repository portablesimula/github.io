/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

/// For-statement single value element, not text.
/// 
/// <pre>
/// 		For i:= <Expr>
/// </pre>
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/FOR_SingleElt.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
/// @param <T> the type of this element
public final class FOR_SingleElt<T> extends FOR_Element {
	/// The for-statement control variable.
	final RTS_NAME<T> cvar;

	/// Next value.
	RTS_NAME<T> nextValue;

	/// Create single value element, not text.
	/// 
	/// @param cvar the for-statement control variable
	/// @param val the text value expression
	/// 
	public FOR_SingleElt(final RTS_NAME<T> cvar, final RTS_NAME<T> val) {
		this.cvar = cvar;
		this.nextValue = val;
		more = true;
	}

    /// Returns {@code true} if the iteration has more elements.
 	/// @return {@code true} if the iteration has more elements
	@Override
	public boolean hasNext() {
		return (nextValue != null);
	}

    /// Update the control variable with the next value if any.
    /// @return {@code true} if the control variable was updated; otherwise {@code false}.
	@Override
	public Boolean next() {
		if (nextValue == null)
			return (false);
		T val = nextValue.get();
		cvar.put(val);
		nextValue = null;
		return (true);
	}
}
