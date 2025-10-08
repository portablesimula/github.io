/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

// ************************************************************
// *** CHARACTER ARRAY
// ************************************************************
/// This class represent a Simula character array. 
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_CHARACTER_ARRAY.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class RTS_CHARACTER_ARRAY extends RTS_ARRAY {
	/// The elements in this RTS_CHARACTER_ARRAY
	final private char[] ELTS;

	/// Create a character array with the given bounds.
	/// @param BOUNDS the array bounds
	public RTS_CHARACTER_ARRAY(final RTS_BOUNDS... BOUNDS) {
		super(BOUNDS);
		ELTS = new char[SIZE];
	}

	/// This method will put a value into ELTS(ix)
	/// @param ix the index of ELTS
	/// @param val the value to put
	/// @return the value stored
	public char putELEMENT(int ix, char val) {
		ELTS[ix] = val;
		return (val);
	}

	/// This method will return a value from ELTS(x)
	/// @param x the index of ELTS
	/// @return the value loaded
	public char getELEMENT(int... x) {
		return (ELTS[index(x)]);
	}

	/// Abstract method redefined for all subclass <type>_ARRAY
	/// @return a copy of this RTS_CHARACTER_ARRAY
	@Override
	public RTS_CHARACTER_ARRAY COPY() {
		RTS_CHARACTER_ARRAY copy = new RTS_CHARACTER_ARRAY(BOUNDS);
		System.arraycopy(ELTS, 0, copy.ELTS, 0, SIZE);
		return (copy);
	}
}
