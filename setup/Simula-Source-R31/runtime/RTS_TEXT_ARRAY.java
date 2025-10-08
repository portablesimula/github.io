/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

// ************************************************************
// *** TEXT ARRAY
// ************************************************************
/// This class represent a Simula text array. 
/// 
/// Link to GitHub: <a href="https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_TEXT_ARRAY.java"><b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class RTS_TEXT_ARRAY extends RTS_ARRAY {
	/// The elements in this RTS_TEXT_ARRAY
	final private RTS_TXT[] ELTS;


	/// Create a text array with the given bounds.
	/// @param BOUNDS the array bounds
	public RTS_TEXT_ARRAY(final RTS_BOUNDS... BOUNDS) {
		super(BOUNDS);
		ELTS = new RTS_TXT[SIZE];
	}

	/// This method will put a text reference into ELTS(ix)
	/// @param ix the index of ELTS
	/// @param val the value to put
	/// @return the value stored
	public RTS_TXT putELEMENT(int ix, RTS_TXT val) {
		ELTS[ix] = val;
		return (val);
	}

	/// This method will return a text reference from ELTS(x...)
	/// @param x the indexes of ELTS
	/// @return the value loaded
	public RTS_TXT getELEMENT(int... x) {
		return (ELTS[index(x)]);
	}

	/// Abstract method redefined for all subclass <type>_ARRAY
	/// @return a copy of this TEXT_ARRAY
	@Override
	public RTS_TEXT_ARRAY COPY() {
		RTS_TEXT_ARRAY copy = new RTS_TEXT_ARRAY(BOUNDS);
		System.arraycopy(ELTS, 0, copy.ELTS, 0, SIZE);
		return (copy);
	}
}
