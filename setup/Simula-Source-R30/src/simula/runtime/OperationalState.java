/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

/// Simula object Operational States
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/OperationalState.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public enum OperationalState {
	/// The object is attached
	attached,
	/// The object is detached
	detached,
	/// The object is resumed
	resumed,
	/// The object is terminated
	terminated,
	/// The Process object is shuting down
	terminatingProcess
}
