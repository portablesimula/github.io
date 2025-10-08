/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

// ************************************************************
// *** BOOLEAN ARRAY
// ************************************************************
/// This class represent a Simula boolean array. 
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_BOOLEAN_ARRAY.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class RTS_BOOLEAN_ARRAY extends RTS_ARRAY {
	/// The elements in this RTS_BOOLEAN_ARRAY
	final private boolean[] ELTS;

	/// Create a boolean array with the given bounds.
	/// @param BOUNDS the array bounds
	public RTS_BOOLEAN_ARRAY(final RTS_BOUNDS... BOUNDS) {
		super(BOUNDS);
		ELTS = new boolean[SIZE];
	}

	/// This method will put a value into ELTS(ix)
	/// @param ix the index of ELTS
	/// @param val the value to put
	/// @return the value stored
	public boolean putELEMENT(int ix, boolean val) {
		ELTS[ix] = val;
		return (val);
	}

	/// This method will return a value from ELTS(x)
	/// @param x the index of ELTS
	/// @return the value loaded
	public boolean getELEMENT(int... x) {
		return (ELTS[index(x)]);
	}

	/// Abstract method redefined for all subclass <type>_ARRAY
	/// @return a copy of this RTS_BOOLEAN_ARRAY
	@Override
	public RTS_BOOLEAN_ARRAY COPY() {
		RTS_BOOLEAN_ARRAY copy = new RTS_BOOLEAN_ARRAY(BOUNDS);
		System.arraycopy(ELTS, 0, copy.ELTS, 0, SIZE);
		return (copy);
	}
}
