/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

// ************************************************************
// *** REAL TYPE ARRAY
// ************************************************************
/// This class is the common superclass for real type arrays. 
/// It is introduced to implement overloading of real type parameter arrays.
/// 
/// Link to GitHub: <a href="https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_REALTYPE_ARRAY.java"><b>Source File</b></a>.
/// @author Øystein Myhre Andersen
public abstract class RTS_REALTYPE_ARRAY extends RTS_ARRAY {

	/// Create a real-type array with the given bounds.
	/// @param BOUNDS the array bounds
	public RTS_REALTYPE_ARRAY(final RTS_BOUNDS... BOUNDS) {
		super(BOUNDS);
	}

	/// Utility for fetching value of a real type array.
	/// 
	/// Used by: Reandom drawing discrete and linear procedures.
	/// @param i index
	/// @return value of ELTS(i)
	public abstract double getRealTypeELEMENT(int i);
}
