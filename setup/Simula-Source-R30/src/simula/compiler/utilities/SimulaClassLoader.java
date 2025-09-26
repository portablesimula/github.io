/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.utilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import simula.runtime.RTS_EndProgram;

/// SimulaClassLoader.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/SimulaClassLoader.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class SimulaClassLoader extends ClassLoader {
	/// Debug utility.
	private final static boolean TESTING = false;
	
	/// Default Constructor.
	public SimulaClassLoader() {}
	
	/// Load a class
	/// @param name class name
	/// @param bytes classFile bytes
	public void loadClass(String name, byte[] bytes) {
		Class<?> clazz = Global.simulaClassLoader.findLoadedClass(name);
		if(clazz != null) {
			if(TESTING) IO.println("SimulaClassLoader.loadClass: " + name + " FAILED: " + clazz);
		} else {
			if(TESTING) IO.println("SimulaClassLoader.loadClass: TRY " + name);
			clazz = Global.simulaClassLoader.defineClass(name, bytes, 0, bytes.length);
			Global.simulaClassLoader.resolveClass(clazz);
			if(TESTING) IO.println("SimulaClassLoader.loadClass: " + name + " SUCCESS");
		}
	}
	
	/// Check if a named class is loaded
	/// @param name class name
	/// @return true: if a named class is loaded
	public boolean isClassLoaded(String name) {
		Class<?> clazz = Global.simulaClassLoader.findLoadedClass(name);
		return clazz != null;
	}
	
	/// Run loaded class 'name'
	/// @param name class name
	/// @param cmd command vector
	public void runClass(String name, final Vector<String> cmd) {
		String[] cmds = new String[cmd.size()];
		cmd.copyInto(cmds);
		Class<?> clazz = Global.simulaClassLoader.findLoadedClass(name);
		Class<?> argTypes[] = { (new String[0]).getClass() };
		Method main = null;
		try {
			main = clazz.getMethod("main", argTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		Object[] arg = { cmds };
			
		if(TESTING) IO.println("SimulaClassLoader.runClass: BEFORE INVOKE clazz = " + clazz);
		try {
			main.invoke(null, arg);
		} catch (IllegalAccessException e) {
			if(TESTING)	IO.println("SimulaClassLoader.runClass: EXCEPTION-1 AFTER INVOKE clazz = " + clazz);
			Util.IERR();
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if(TESTING)	IO.println("SimulaClassLoader.runClass: EXCEPTION-2 AFTER INVOKE cause = " + cause + ", clazz = " + clazz);
			if(cause instanceof RTS_EndProgram) ; // OK
			else if(cause instanceof RuntimeException err) throw err;
			else Util.IERR("SimulaClassLoader.runClass: EXCEPTION AFTER INVOKE cause = "+cause);
		}
		if(TESTING) IO.println("SimulaClassLoader.runClass: AFTER INVOKE clazz = " + clazz);
	}

}

