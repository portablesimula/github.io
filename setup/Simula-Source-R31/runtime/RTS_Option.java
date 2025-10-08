package simula.runtime;

/// Utility class Runtime Options.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_Option.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class RTS_Option {
	
	/// Default constructor.
	RTS_Option() {}
	
	/// Command line arguments
	public static String[] argv;

	/// Output messages about what the RTS is doing.
	/// Default: false.
	public static boolean VERBOSE = false;

	/// TRUE:Don't create popUps at runtime
	static boolean noPopup = false;
	
	/// Debug: Trace enter and exit of blocks, classes and procedures.
	/// Default: false.
	public static boolean BLOCK_TRACING = false;
	
	/// Debug: Trace goto statements.
	/// Default: false.
	public static boolean GOTO_TRACING = false;
	
	///  Debug: Trace detach, resume and call.
	/// Default: false.
	public static boolean QPS_TRACING = false;
	
	/// Debug: Trace Simulation events.
	/// Default: false.
	public static boolean SML_TRACING = false;
	
	/// Specify where Simula files (Outfile, Infile, ...) are written and read.
	/// Default: User working directory. System.property("user.dir")
	public static String RUNTIME_USER_DIR = "";
	
	/// Selectors for conditional compilation.
	static String Selectors = "AZ";

}
