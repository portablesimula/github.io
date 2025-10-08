package simula.runtime;

/// For-statement While element, not text
/// 
/// <pre>
/// 		For i:= <Expr> while <Cond>
/// </pre>
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/FOR_WhileElt.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
/// @param <T> the type of this element
public final class FOR_WhileElt<T> extends FOR_Element {
	/// The for-statement control variable.
	final RTS_NAME<T> cvar;
	
	/// A Value expression.
	final RTS_NAME<T> expr;
	
	/// The condition that determines whether to continue.
	RTS_NAME<Boolean> cond;

	/// Create While element. 
	/// @param cvar the for-statement control variable
	/// @param expr a Value expression
	/// @param cond a Boolean exppression
	public FOR_WhileElt(final RTS_NAME<T> cvar, final RTS_NAME<T> expr, final RTS_NAME<Boolean> cond) {
		this.cvar = cvar;
		this.expr = expr;
		this.cond = cond;
	}

    /// Update the control variable with the next value if any.
    /// @return {@code true} if the control variable was updated; otherwise {@code false}.
	@Override
	public Boolean next() {
		T val = expr.get();
		cvar.put(val);
		more = cond.get(); // IF not more return null - test i loopen: if(_CB==null) continue;
		return (more);
	}
}
