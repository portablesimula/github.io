/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

// ************************************************************
// *** ARRAY OBJECTS
// ************************************************************

/// This class is used to hold bound pairs in RTS_ARRAY objects.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_BOUNDS.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class RTS_BOUNDS {
	/// An array upper bound
	final public int LB;
	/// An array ELEMENTS size
	final public int SIZE;

	/// Create an array bound pair object
	/// @param LB Lower bound
	/// @param UB Upper bound
	public RTS_BOUNDS(final int LB, final int UB) {
		if (LB > UB)
			throw new RTS_SimulaRuntimeError("Lower bound(" + LB + ") > upper bound(" + UB + ")");
		this.LB = LB;
		SIZE = UB - LB + 1;
	}

	@Override
	public String toString() {
		return ("" + LB + ':' + (LB + SIZE - 1));
	}
}
