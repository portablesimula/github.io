/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

/// For-statement single text value element.
/// 
/// <pre>
/// 		For t:= <TextExpr> // Text Value Assignment
/// </pre>
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/FOR_SingleTValElt.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class FOR_SingleTValElt extends FOR_Element {
	/// The for-statement control variable.
	final RTS_NAME<RTS_TXT> cvar;

	/// Next value.
	RTS_NAME<RTS_TXT> nextValue;

	/// Create single text value element.
	/// 
	/// @param cvar the for-statement control variable
	/// @param tval the text value expression
	/// 
	public FOR_SingleTValElt(final RTS_NAME<RTS_TXT> cvar, final RTS_NAME<RTS_TXT> tval) {
		this.cvar = cvar;
		this.nextValue = tval;
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
		RTS_TXT val = nextValue.get();
		RTS_UTIL._ASGTXT(cvar.get(), val);
		nextValue = null;
		return (true);
	}
}
