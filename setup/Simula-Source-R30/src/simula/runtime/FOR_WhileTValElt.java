/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

/// For-statement While-element with Text Value Assignment.
/// 
/// <pre>
/// 		For t:= <TextExpr> while <Cond> // Text Value Assignment
/// </pre>
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/FOR_WhileTValElt.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class FOR_WhileTValElt extends FOR_Element {
	// For t:= <TextExpr> while <Cond> // Text Value Assignment
	/// The for-statement control variable.
	final RTS_NAME<RTS_TXT> cvar;
	/// A Text Value expression.
	final RTS_NAME<RTS_TXT> expr;
	/// The condition that determines whether to continue.
	RTS_NAME<Boolean> cond;

	/// Create Text Value While-element. 
	/// @param cvar the for-statement control variable
	/// @param expr a Text Value expression
	/// @param cond a Boolean expression
	public FOR_WhileTValElt(final RTS_NAME<RTS_TXT> cvar, final RTS_NAME<RTS_TXT> expr, final RTS_NAME<Boolean> cond) {
		this.cvar = cvar;
		this.expr = expr;
		this.cond = cond;
	}

    /// Update the control variable with the next value if any.
    /// @return {@code true} if the control variable was updated; otherwise {@code false}.
	@Override
	public Boolean next() {
		RTS_TXT val = expr.get();
		RTS_UTIL._ASGTXT(cvar.get(), val);
		more = cond.get(); // IF not more return null - test i loopen: if(_CB==null) continue;
		return (more);
	}
}
