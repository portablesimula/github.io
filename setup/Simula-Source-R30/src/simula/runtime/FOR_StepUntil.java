package simula.runtime;

/// For-statement step until element.
/// 
/// <pre>
///  A1 step A2 until A3
///
///          C := A1;
///          DELTA := A2;
///          while DELTA*(C-A3) <= 0
///          do begin
///              S;
///              DELTA := A2;
///              C := C + DELTA;
///          end while;
///          ... next for list element
/// </pre>
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/FOR_StepUntil.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class FOR_StepUntil extends FOR_Element {
	/// The for-statement control variable.
	final RTS_NAME<Number> cvar;

	/// The initial value.
	final RTS_NAME<Number> init;

	/// The step value.
	final RTS_NAME<Number> step;

	/// The until value.
	final RTS_NAME<Number> until;

	/// Next value.
	Number nextValue;

	/// Create step until element.
	/// 
	/// @param cvar the for-statement control variable
	/// @param init the initial value expression
	/// @param step the step value expression
	/// @param until the until value expression
	/// 
	public FOR_StepUntil(final RTS_NAME<Number> cvar, final RTS_NAME<Number> init, final RTS_NAME<Number> step,
			final RTS_NAME<Number> until) {
		this.cvar = cvar;
		this.init = init;
		this.step = step;
		this.until = until;
	}

    /// Update the control variable with the next value if any.
    /// @return {@code true} if the control variable was updated; otherwise {@code false}.
	@Override
	public Boolean next() {
		try {
			Number stp;
			int sign;
			if (nextValue == null) {
				nextValue = init.get();
				stp = 0;// new Integer(0);
				sign = (int) Math.signum(step.get().longValue());
			} // First value
			else {
				stp = step.get();
				sign = (int) Math.signum(stp.longValue());
			}
			Number val = nextValue;
			Number utl = until.get();
			if (val instanceof Double || stp instanceof Double) {
				nextValue = val.doubleValue() + stp.doubleValue();
				more = (sign * (nextValue.doubleValue() - utl.doubleValue()) <= 0);
			} else if (val instanceof Float || stp instanceof Float) {
				nextValue = val.floatValue() + stp.floatValue();
				more = (sign * (nextValue.floatValue() - utl.floatValue()) <= 0);
			} else if (val instanceof Long || stp instanceof Long) {
				nextValue = val.longValue() + stp.longValue();
				more = (sign * (nextValue.longValue() - utl.longValue()) <= 0);
			} else {
				nextValue = val.intValue() + stp.intValue();
				more = (sign * (nextValue.intValue() - utl.intValue()) <= 0);
			}
			cvar.put(nextValue);
			return (more);
		} catch (Throwable e) {
			e.printStackTrace();
			return (null);
		}
	}
}
