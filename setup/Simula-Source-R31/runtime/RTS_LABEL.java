/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

// ************************************************************
// *** FRAMEWORK for Label-Variable in Java Coding
// ************************************************************
/// This class represent a Simula Label quantity
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_LABEL.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
@SuppressWarnings("serial")
public final class RTS_LABEL extends RuntimeException {
	/// Static link, i.e. the block in which the label is defined.
	public final RTS_RTObject _SL;
	
	/// Prefix level.
	public final int _PRFX; // Prefix level.
	
	/// Index, I.e. ordinal number of the Label within its Scope(staticLink).
	public final int index;
	
	/// Label identifier. To improve error and trace messages.
	public final String identifier; // To improve error and trace messages.

	/// Create a label quantity
	/// @param _SL static link
	/// @param _PRFX prefix level
	/// @param index label index
	/// @param identifier label identifier
	public RTS_LABEL(final RTS_RTObject _SL, final int _PRFX, final int index, final String identifier) {
		this._SL = _SL;
		this._PRFX = _PRFX;
		this.index = index;
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return ("RTS_LABEL(" + _SL + ", PRFX=" + _PRFX + ", LABEL#" + index + ", identifier=" + identifier + ')');
	}
}
