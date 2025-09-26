/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

/// The RTS End Program Exception
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_EndProgram.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
@SuppressWarnings("serial")
public final class RTS_EndProgram extends RuntimeException {

	/// Constructs a new RTS_EndProgram exception with the specified detail message. 
	/// @param message the detail message.
	public RTS_EndProgram(final String message) {
		super(message);
	}
}
