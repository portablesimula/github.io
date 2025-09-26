/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

/// The Abstract Class RTS_NAME<T> are supporting Simula's Name-Parameters.
/// 
/// The basic principle is to establish an
/// object within the calling scope. This object will have two attribute methods;
/// 'get' og 'put' to evaluate and read the value of the actual parameter, or, if legal, to
/// write into it. The following Java-class is used to perform such parameter
/// transmissions:
/// 
/// <pre>
/// 
/// 	 abstract class RTS_NAME<T> {
/// 	 	abstract T get();
/// 
/// 	 	void put(T x) {
/// 	 		error("Illegal ...");
/// 	 	}
/// 	 }
/// 
/// </pre>
/// 
/// Note that we both use abstract Java classes and 'generics' i.e. the actual
/// type is a parameter. Also note that the 'put' method has a default definition
/// producing an error. This enables redefinition of the 'put' method to be
/// dropped for expression as actual parameters.
/// 
/// Suppose the Simula Procedure:
/// 
/// <pre>
/// 		procedure P(k); name k; integer k; k:=k+1;
/// </pre>
/// 
/// It will be translated to something like this Java method:
/// 
/// <pre>
/// 	 void P(RTS_NAME<Integer> k) {
/// 	 	k.put(k.get() + 1); // E.g: k=k+1
/// 	 }
/// </pre>
/// 
/// In the calling place, in practice in the actual parameter list, we create an
/// object of a specific subclass of RTS_NAME<T> by specifying the Integer type
/// and defining the get and put methods. Eg. If the current parameter is a
/// variable 'q', then the actual parameter will be coded as follows:
/// 
/// <pre>
/// 	 new RTS_NAME<Integer>() {
/// 	 	Integer get() {
/// 	 		return (q);
/// 	 	}
/// 
/// 	 	void put(Integer x) {
/// 	 		q = (int) x;
/// 	 	}
/// 	 }
/// </pre>
/// 
/// However, if the actual parameter is an expression like (j + m * n) then it
/// will be coded as follows:
/// 
/// <pre>
/// 	 new RTS_NAME<Integer>() {
/// 	 	Integer get() {
/// 	 		return (j + m * n);
/// 	 	}
/// 	 }
/// </pre>
/// 
/// Here we see that the 'put' method is not redefined so that any attempt to
/// assign a new value to this name parameter will result in an error message.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_NAME.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
///
/// @param <T> the type of the parameter
public abstract class RTS_NAME<T> {
	/// The environment in which evaluations of get'parameters will take place.
	public RTS_RTObject _CUR; // Thunk Environment

	/// Construct a RTS_NAME object
	public RTS_NAME() {
		_CUR = RTS_RTObject._CUR;
	}

	/// Evaluate and get the value of a name parameter
	/// @return the value 
	public abstract T get();

	/// Write back into a name parameter
	/// @param x the value to be written
	/// @return the value written
	public T put(final T x) {
		throw new RTS_SimulaRuntimeError("Illegal assignment. Name parameter is not a variable");
	}

	@Override
	public String toString() {
		return "RTS_NAME " + this.getClass();
	}
}

