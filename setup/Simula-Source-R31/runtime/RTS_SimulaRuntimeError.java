/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

/// Simula Runtime Error
/// 
/// Link to GitHub: <a href="https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_SimulaRuntimeError.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
@SuppressWarnings("serial")
public class RTS_SimulaRuntimeError extends RuntimeException {

	/// Constructs a new RTS_SimulaRuntimeError with the specified detail message.
	/// @param message the detail message
	public RTS_SimulaRuntimeError(String message) {
		super(message);
	}

	/// Constructs a new exception with the specified detail message and cause.
	/// @param message the detail message
	/// @param cause the cause
	public RTS_SimulaRuntimeError(String message, Throwable cause) {
		super(message, cause);
	}

	/// Constructs a new exception with the specified detail cause.
	/// @param cause the cause
	public RTS_SimulaRuntimeError(Throwable cause) {
		super(cause.getMessage(), cause);
	}
}
