/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import javax.swing.JOptionPane;

/// Utility class containing a lot of common stuff.
/// 
/// Link to GitHub: <a href="https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_UTIL.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class RTS_UTIL {
	/// Default constructor.
	private RTS_UTIL(){}

	/// The runtime console. May be null
	static RTS_ConsolePanel console;

	/// The program ident.
	static String progamIdent;
	
	/// The current module ident.
	static String currentModid;
	
	/// The current simula source line number.
	static int currentSimLine;
	
	/// Number of edit overflows.
	static int numberOfEditOverflows;



	// *****************************************
	// *** Text utilities ***
	// *****************************************
	/// Utility constant NOTEXT
	public final static RTS_TXT NOTEXT = new RTS_TXT();

	/// Text value assignment.
	/// @param T the from text
	/// @param U the target text
	/// @return the resulting text
	public static RTS_TXT _ASGTXT(RTS_TXT T, RTS_TXT U) {
		if (T == null)
			T = NOTEXT;
		if (U == null)
			U = NOTEXT;
		int fromLength = U.LENGTH;
		if (fromLength > T.LENGTH)
			throw (new RTS_SimulaRuntimeError(
					"RHS too long in text value assignment: LHS.length=" + T.LENGTH + ", RHS.length=" + fromLength));
		for (int i = 0; i < fromLength; i++)
			T.OBJ.MAIN[T.START + i] = U.OBJ.MAIN[U.START + i];
		for (int i = fromLength; i < T.LENGTH; i++)
			T.OBJ.MAIN[T.START + i] = ' ';
		return (T);
	}

	/// Text value assignment from String.
	/// @param T the from text
	/// @param s the target text
	/// @return the resulting text
	public static RTS_TXT _ASGSTR(RTS_TXT T, final String s) {
		if (T == null)
			T = NOTEXT;
		int fromLength = 0;
		if (s != null)
			fromLength = s.length();
		if (fromLength > T.LENGTH)
			throw (new RTS_SimulaRuntimeError(
					"RHS too long in text value assignment: LHS.length=" + T.LENGTH + ", RHS.length=" + fromLength));
		for (int i = 0; i < fromLength; i++)
			T.OBJ.MAIN[T.START + i] = s.charAt(i);
		for (int i = fromLength; i < T.LENGTH; i++)
			T.OBJ.MAIN[T.START + i] = ' ';
		return (T);
	}

	// **************************************************************
	// *** TXTREL - Text value relations
	// **************************************************************
	/// Text value relation - LT
	/// @param left left hand side
	/// @param right right hand side
	/// @return true if relation holds
	public static boolean _TXTREL_LT(final RTS_TXT left, final RTS_TXT right) {
		return (TXTREL(left, right, 1));
	}

	/// Text value relation - EQ
	/// @param left left hand side
	/// @param right right hand side
	/// @return true if relation holds
	public static boolean _TXTREL_EQ(final RTS_TXT left, final RTS_TXT right) {
		return (TXTREL(left, right, 2));
	}

	/// Text value relation - LE
	/// @param left left hand side
	/// @param right right hand side
	/// @return true if relation holds
	public static boolean _TXTREL_LE(final RTS_TXT left, final RTS_TXT right) {
		return (TXTREL(left, right, 3));
	}

	/// Text value relation - GT
	/// @param left left hand side
	/// @param right right hand side
	/// @return true if relation holds
	public static boolean _TXTREL_GT(final RTS_TXT left, final RTS_TXT right) {
		return (TXTREL(left, right, 4));
	}

	/// Text value relation - NE
	/// @param left left hand side
	/// @param right right hand side
	/// @return true if relation holds
	public static boolean _TXTREL_NE(final RTS_TXT left, final RTS_TXT right) {
		return (TXTREL(left, right, 5));
	}

	/// Text value relation - GE
	/// @param left left hand side
	/// @param right right hand side
	/// @return true if relation holds
	public static boolean _TXTREL_GE(final RTS_TXT left, final RTS_TXT right) {
		return (TXTREL(left, right, 6));
	}

	/// Text value relation
	/// @param left left hand side
	/// @param right right hand side
	/// @param code relational code
	/// @return true if relation holds
	private static boolean TXTREL(RTS_TXT left, RTS_TXT right, int code) {
		int i; // Loop index.
		int dif; // Difference between lengths.
		int lng; // Length of common parts.
		if (left == null)  left = RTS_UTIL.NOTEXT;
		if (right == null) right = RTS_UTIL.NOTEXT;
		lng = right.LENGTH;
		dif = lng - left.LENGTH;
		if (dif != 0) {
			if (code == 2) return (false);
			if (code == 5) return (true);
			if (dif > 0)   lng = left.LENGTH;
		}
		i = 0;
		while (i < lng) {
			int rightChar = right.OBJ.MAIN[right.START + i];
			int leftChar = left.OBJ.MAIN[left.START + i];
			if (rightChar != leftChar) {
				dif = rightChar - leftChar;
				break;
			}
			i = i + 1;
		}
		switch (code) {
			case 1:	return (0 < dif);
			case 2:	return (0 == dif);
			case 3:	return (0 <= dif);
			case 4:	return (0 > dif);
			case 5:	return (0 != dif);
			case 6:	return (0 >= dif);
			default:
				throw new RTS_SimulaRuntimeError("Internal Error");
		}
	}

	// **************************************************************
	// *** TXTREL - Text reference relations. == =/=
	// **************************************************************
	/// Text reference equal relation
	/// @param left left hand side
	/// @param right right hand side
	/// @return true if relation holds
	public static boolean TRF_EQ(RTS_TXT left, RTS_TXT right) {
		if (left == null)
			left = RTS_UTIL.NOTEXT;
		if (right == null)
			right = RTS_UTIL.NOTEXT;
		if (left.LENGTH != right.LENGTH)
			return (false);
		if (left.START != right.START)
			return (false);
		if (left.OBJ != right.OBJ)
			return (false);
		return (true);
	}

	/// Text reference not equal relation
	/// @param left left hand side
	/// @param right right hand side
	/// @return true if relation holds
	public static boolean TRF_NE(final RTS_TXT left, final RTS_TXT right) {
		return (!TRF_EQ(left, right));
	}

	/// Integer Addition: x + y
	/// @param x argument x
	/// @param y argument y
	/// @return x + y
	public static int _IADD(final int x, int y) {
		long z = (long)x + (long)y;
		if(z > Integer.MAX_VALUE)
			throw new RTS_SimulaRuntimeError("Integer Overflow: " + x + " + " + y + " = " + z + " > Integer.MAX_VALUE(" + Integer.MAX_VALUE + ')');
		if(z < Integer.MIN_VALUE)
			throw new RTS_SimulaRuntimeError("Integer Overflow: " + x + " + " + y + " = " + z + " < Integer.MIN_VALUE(" + Integer.MIN_VALUE + ')');
		return (int) z;
	}

	/// Integer Subtraction: x - y
	/// @param x argument x
	/// @param y argument y
	/// @return x - y
	public static int _ISUB(final int x, int y) {
		long z = (long)x - (long)y;
		if(z > Integer.MAX_VALUE)
			throw new RTS_SimulaRuntimeError("Integer Overflow: " + x + " - " + y + " = " + z + " > Integer.MAX_VALUE(" + Integer.MAX_VALUE + ')');
		if(z < Integer.MIN_VALUE)
			throw new RTS_SimulaRuntimeError("Integer Overflow: " + x + " - " + y + " = " + z + " < Integer.MIN_VALUE(" + Integer.MIN_VALUE + ')');
		return (int) z;
	}

	/// Integer Multiplication: x * y
	/// @param x argument x
	/// @param y argument y
	/// @return x * y
	public static int _IMUL(final int x, int y) {
		long z = (long)x * (long)y;
		if(z > Integer.MAX_VALUE)
			throw new RTS_SimulaRuntimeError("Multiplication Overflow: " + x + " * " + y + " = " + z + " > Integer.MAX_VALUE(" + Integer.MAX_VALUE + ')');
		if(z < Integer.MIN_VALUE)
			throw new RTS_SimulaRuntimeError("Multiplication Overflow: " + x + " * " + y + " = " + z + " < Integer.MIN_VALUE(" + Integer.MIN_VALUE + ')');
		return (int) z;
	}
	
	/// Integer Power: b ** x
	/// @param b argument b
	/// @param x argument x
	/// @return b ** x
	public static int _IPOW(final int b, int x) {
		if (x == 0) {
			if (b == 0)
				throw new RTS_SimulaRuntimeError("Exponentiation: " + b + " ** " + x + "  Result is undefined.");
			return (1); // any ** 0 ==> 1
		} else if (x < 0)
			throw new RTS_SimulaRuntimeError("Exponentiation: " + b + " ** " + x + "  Result is undefined.");
		else if (b == 0)
			return (0); // 0 ** non_zero ==> 0

		long res = (long) Math.pow((double) b, (double) x);
		if (res > Integer.MAX_VALUE || res < Integer.MIN_VALUE)
			throw new RTS_SimulaRuntimeError("Arithmetic overflow: " + b + " ** " + x + " ==> " + res
					+ " which is outside integer value range[" + Integer.MIN_VALUE + ':' + Integer.MAX_VALUE + ']');
		return ((int) res);
	}

	// ************************************************************
	// *** object IS classIdentifier
	// ************************************************************
	
	/// Object relation: IS.
	/// <pre>
	///       simple-object-expression is class-identifier
	/// </pre>
	/// 
	/// @param obj object reference
	/// @param cls class reference
	/// @return true: relation holds
	public static boolean _IS(final Object obj, final Class<?> cls) {
		return ((obj == null) ? false : (obj.getClass() == cls));
	}

	/// Standard Procedure sign.
	/// <pre>
	/// integer procedure sign(e);    e;
	///     sign := if e > 0 then  1
	///        else if e < 0 then -1 else 0;
	/// </pre>
	/// 
	/// The result is zero if the parameter is zero, one if the parameter is
	/// positive, and minus one otherwise.
	/// 
	/// @param e the argument e
	/// @return resulting sign code
	public static int isign(final int e) {
		return ((e > 0) ? (1) : ((e < 0) ? -1 : 0));
	}
	
	/// Standard Procedure sign.
	/// @param e the argument e
	/// @return resulting sign code
	public static float fsign(final float e) {
		return ((e > 0) ? (1) : ((e < 0) ? -1 : 0));
	}
	
	/// Standard Procedure sign.
	/// @param e the argument e
	/// @return resulting sign code
	public static double dsign(final double e) {
		return ((e > 0) ? (1) : ((e < 0) ? -1 : 0));
	}
	
	/// Treat Exception
	/// @param e the Throwable Object
	/// @param obj the RTObject which object that received the exception
	public static void treatException(final Throwable e, final RTS_RTObject obj) {
		String threadID = (RTS_Option.VERBOSE) ? ("Thread:" + Thread.currentThread().getName() + '[' + obj + "]: ") : "";
		if (RTS_Option.GOTO_TRACING) {
			RTS_UTIL.println("\nRTS_RTObject.treatException: In "+ threadID + e);
			e.printStackTrace(System.out);
		}
		
		if (e instanceof RTS_LABEL) {
			if (RTS_Option.GOTO_TRACING) {
				RTS_UTIL.println("POSSIBLE GOTO OUT OF COMPONENT " + obj.edObjectAttributes());
			}
			RTS_RTObject DL = obj._DL;
			if (DL != null && DL != RTS_RTObject._CTX) {
				if (RTS_Option.GOTO_TRACING) {
					System.err.println("DL=" + DL.edObjectAttributes());
					RTS_UTIL.println("DL=" + DL.edObjectAttributes());
				}
				RTS_Coroutine._PENDING_EXCEPTION = (RuntimeException) e;
				DL._CORUT.run();
			} else {
				String msg = "Illegal GOTO " + ((RTS_LABEL) e).identifier;
				if (RTS_ENVIRONMENT.EXCEPTION_HANDLER != null) {
					callExceptionHandler(msg);
				} else {
					RTS_UTIL.println(threadID + "SIMULA RUNTIME(1) ERROR: " + msg);
					if (RTS_Option.VERBOSE)
						e.printStackTrace();
					
					if (RTS_Option.GOTO_TRACING) {
						IO.println("RTS_UTIL.treatException: Return after 'Illege GOTO' message");
					}
					RTS_ENVIRONMENT.exit(-1);
				}
			}
		} else if (e instanceof RTS_EndProgram) {
			if (RTS_Option.GOTO_TRACING) {
				IO.println("RTS_UTIL.treatException: RTS_EndProgram EXIT");
			}
			// NOTHING
		} else if (e instanceof RuntimeException) {
			String msg = getErrorMessage(e);
			msg = msg.replace("RTS_SimulaRuntimeError: ", "");
			if (RTS_ENVIRONMENT.EXCEPTION_HANDLER != null) {
				callExceptionHandler(msg);
			} else {
				RTS_UTIL.printError(threadID + "SIMULA RUNTIME(2) ERROR: " + msg);
				if (RTS_Option.VERBOSE)
					Thread.dumpStack();
				RTS_UTIL.printSimulaStackTrace(e, 0);
				if(RTS_UTIL.console != null) {
					while(true) Thread.yield();
				}
				RTS_ENVIRONMENT.exit(-1);
			}
			
		} else if (e instanceof Error) {
			String msg = e.getClass().getSimpleName();
			RTS_UTIL.printError(threadID + "SIMULA RUNTIME(3) ERROR: " + msg);
			RTS_UTIL.printSimulaStackTrace(e, 0);
			if (RTS_Option.VERBOSE)
				e.printStackTrace();
			RTS_ENVIRONMENT.exit(-1);
		} else {
			RTS_UTIL.printError(threadID + "UNCAUGHT EXCEPTION: " + e.getMessage());
			e.printStackTrace();
			RTS_ENVIRONMENT.exit(-1);
		}
		if (RTS_Option.GOTO_TRACING)
			RTS_UTIL.printThreadList();
		
	}
	
	/// Utility: Treat Runtime error
	/// @param msg the message
	private static void callExceptionHandler(String msg) {
		RTS_PRCQNT erh = RTS_ENVIRONMENT.EXCEPTION_HANDLER;
		try {
			RTS_ENVIRONMENT.EXCEPTION_HANDLER = null;
			erh.CPF().setPar(new RTS_TXT(msg))._ENT();
		} catch (RTS_EndProgram e) {
			if(RTS_Option.GOTO_TRACING) {
				IO.println("RTS_UTIL.callExceptionHandler: callExceptionHandler returned with exception: "+e);
			}
			// NOTHING
		} catch (Throwable t) {
			RTS_UTIL.printError("EXCEPTION IN SIMULA EXCEPTION_HANDLER: " + t);
			RTS_UTIL.printError("EXCEPTION_HANDLER: " + erh);
			t.printStackTrace();
		}
	}

	/// Utility to convert a Throwable message to Simula message.
	/// @param e a Throwable
	/// @return converted message
	public static String getErrorMessage(Throwable e) {
		String msg = e.getMessage();
		if (e instanceof NullPointerException)
			msg = "NONE-CHECK Failed";
		else if (e instanceof ArrayIndexOutOfBoundsException)
			msg = "ArrayIndexOutOfBounds";
		return (e.getClass().getSimpleName() + ": " + msg);
	}

	// *********************************************************************
	// *** endProgram
	// *********************************************************************
	/// End of Simula program execution.
	/// @param exitValue the exit value
	static void endProgram(final int exitValue) {
		// _SYSIN.close();
		// _SYSOUT.close();
		
		// RTS_BASICIO._SYSOUT.outimage();
		String img = RTS_BASICIO._SYSOUT.image.edStripedText();
		if(img.length() > 0) RTS_BASICIO._SYSOUT.outimage();
		
		long timeUsed = System.currentTimeMillis() - RTS_RTObject.startTimeMs;
		if (RTS_Option.VERBOSE) {
			RTS_UTIL.println("\nEnd program: " + RTS_UTIL.progamIdent);
			if (RTS_UTIL.numberOfEditOverflows > 0)
				RTS_UTIL.println(" -  WARNING " + RTS_UTIL.numberOfEditOverflows + " EditOverflows");
			Runtime runtime = Runtime.getRuntime();
			RTS_UTIL.println(" -  Memory(used=" + runtime.totalMemory() + ",free=" + runtime.freeMemory() + ')');
			RTS_UTIL.println(" -  nProcessors=" + runtime.availableProcessors());
			RTS_UTIL.println(" -  Elapsed Time Approximately " + timeUsed / 1000 + " sec.");
		} else if (RTS_UTIL.numberOfEditOverflows > 0)
			RTS_UTIL.println("End program: WARNING " + RTS_UTIL.numberOfEditOverflows + " EditOverflows");
		if (RTS_UTIL.console == null) {
			if(RTS_Option.GOTO_TRACING) {
				IO.println("RTS_UTIL.endProgram: "+exitValue);
			}

			throw new RTS_EndProgram("Simula - endProgram");				
		}
	}


	// ************************************************************
	// *** BPRG -- Begin Program
	// ************************************************************
	/// The begin program routine (BPRG) is the runtime system initialization
	/// routine. It will initiate the global data in the runtime system.
	/// @param ident the program identifier
	/// @param args the arguments
	public static void BPRG(final String ident, final String[] args) {
		setRuntimeOptions(args);
		RTS_Coroutine.INIT();
		RTS_UTIL.numberOfEditOverflows = 0;
		RTS_RTObject.startTimeMs = System.currentTimeMillis();
		RTS_UTIL.progamIdent = ident;
		if (RTS_RTObject._SYSIN == null) {
			if (RTS_Option.BLOCK_TRACING) {
				RTS_UTIL.TRACE("Begin Execution of Simula Program: " + ident);
			}
			RTS_RTObject._SYSIN = new RTS_Infile(RTS_RTObject._CTX, new RTS_TXT("#sysin"));
			RTS_RTObject._SYSOUT = new RTS_Printfile(RTS_RTObject._CTX, new RTS_TXT("#sysout"));
			RTS_RTObject._SYSIN.open(RTS_ENVIRONMENT.blanks(RTS_RTObject._INPUT_LINELENGTH));
			RTS_RTObject._SYSOUT.open(RTS_ENVIRONMENT.blanks(RTS_RTObject._OUTPUT_LINELENGTH));
		}
		RTS_RTObject._CUR = RTS_RTObject._CTX;
	}

	// ************************************************************
	// *** RUN_STM -- Run users statements
	// ************************************************************
	/// Run users statements
	/// @param usr user program
	public static void RUN_STM(final RTS_RTObject usr) {
		RTS_RTObject._USR = (RTS_BASICIO) usr;
		try {
			RTS_RTObject._USR._STM();
		} catch (Throwable e) {
			RTS_UTIL.treatException(e, RTS_RTObject._USR);
		}
	}

	/// Print synopsis of standard options
	private static void help() {
		println(RTS_ENVIRONMENT.simulaReleaseID + " See: https://github.com/portablesimula\n");
		println("Usage: java -jar jarFile.jar  [options]  sourceFile\n\n"
				+ "jarFile			Any output jar file from the simula compiler\n\n" + "possible options include:\n"
				+ "  -help                 Print this synopsis of standard options\n"
				+ "  -verbose              Output messages about what the RTS is doing\n"
				+ "  -noPopup              Don't create popups at runtime\n"
				+ "  -blockTracing         Debug: Trace enter and exit of blocks, classes and procedures\n"
				+ "  -gotoTracing          Debug: Trace goto statements\n"
				+ "  -qpsTracing           Debug: Trace detach, resume and call\n"
				+ "  -smlTracing           Debug: Trace Simulation events\n"
				+ "  -sysout <file name>   Specify where a copy of Sysout is written\n"
				+ "  -userDir <directory>  Specify where Simula files (Outfile, Infile, ...) are written and read\n"
				+ "                        Default: User working directory. System.property(\"user.dir\")\n"
		);
//		System.exit(-2);
		RTS_ENVIRONMENT.exit(-2);
	}

	/// Set runtime options.
	/// @param args argument array
	public static void setRuntimeOptions(final String[] args) {
		File sysoutFile = null;
		RTS_Option.argv = args;
		// Parse command line arguments.
		RTS_Option.RUNTIME_USER_DIR = System.getProperty("user.dir", null);
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if(arg == null) ; // Nothing
			else if(arg.length()==0) ; // Nothing
			else if (arg.charAt(0) == '-') { // command line option
				// General RTS Options
				if (arg.equalsIgnoreCase("-help"))					help();
				else if (arg.equalsIgnoreCase("-verbose"))			RTS_Option.VERBOSE = true;
				else if (arg.equalsIgnoreCase("-noPopup"))			RTS_Option.noPopup = true;
				else if (arg.equalsIgnoreCase("-blockTracing"))		RTS_Option.BLOCK_TRACING = true;
				else if (arg.equalsIgnoreCase("-gotoTracing"))		RTS_Option.GOTO_TRACING = true;
				else if (arg.equalsIgnoreCase("-qpsTracing"))		RTS_Option.QPS_TRACING = true;
				else if (arg.equalsIgnoreCase("-smlTracing"))		RTS_Option.SML_TRACING = true;
				else if (arg.equalsIgnoreCase("-userDir"))			RTS_Option.RUNTIME_USER_DIR = args[++i];
			}
		}
		if (RTS_Option.VERBOSE) {
			RTS_UTIL.println("Begin Execution of Simula Program using " + getJavaID());
			listRuntimeOptions();
			IO.println("sysout Copy=" + sysoutFile);
		}
	}

	/// Popup an error message box.
	/// @param msg the error message
	static void popUpError(final String msg) {
		int res = optionDialog(msg + "\nDo you want to continue ?", "Error", JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE, "Yes", "No");
		if (res != JOptionPane.YES_OPTION)
			throw new RTS_EndProgram("Simula - endProgram");
	}

	/// Brings up an option dialog.
	/// @param msg the message to display
	/// @param title the title string for the dialog
	/// @param optionType an integer designating the options available on the dialog
	/// @param messageType an integer designating the kind of message this is
	/// @param option an array of objects indicating the possible choices the user can make
	/// @return an integer indicating the option chosen by the user, or CLOSED_OPTION if the user closed the dialog
	static int optionDialog(final Object msg, final String title, final int optionType, final int messageType, final String... option) {
		int answer = JOptionPane.showOptionDialog(null, msg, title, optionType, messageType, null, option, option[0]);
		return (answer);
	}

	/// List runtime options.
	static void listRuntimeOptions() {
		IO.println("file.encoding=" + System.getProperty("file.encoding"));
		IO.println("defaultCharset=" + Charset.defaultCharset());
		IO.println("verbose=" + RTS_Option.VERBOSE);
		IO.println("blockTracing=" + RTS_Option.BLOCK_TRACING);
		IO.println("gotoTracing=" + RTS_Option.GOTO_TRACING);
		IO.println("qpsTracing=" + RTS_Option.QPS_TRACING);
		IO.println("smlTracing=" + RTS_Option.SML_TRACING);
		IO.println("userDir=" + RTS_Option.RUNTIME_USER_DIR);
	}

	/// Print a line on the runtime console if present, otherwise on System.out
	/// @param msg the message to print
	static void println(final String msg) {
		if(RTS_Option.noPopup) {
			IO.println(msg);
		} else {
			ensureOpenRuntimeConsole();
			console.write(msg + '\n');
		}
	}

	/// Print an error on the runtime console if present, otherwise on System.out
	/// @param msg the message to print
	static void printError(final String msg) {
		if(RTS_Option.noPopup) {
			IO.println(msg);
		} else {
			ensureOpenRuntimeConsole();
			console.writeError(msg + '\n');
		}
	}

	/// Print a warning message on the runtime console if present, otherwise on System.out
	/// @param msg the message to print
	static void printWarning(final String msg) {
		if(RTS_Option.noPopup) {
			IO.println(msg);
		} else {
			ensureOpenRuntimeConsole();
			console.writeWarning(msg + '\n');
		}
	}
	
	/// Open Simula Runtime Console.
	static void ensureOpenRuntimeConsole() {
		if (console == null) {
			console = new RTS_ConsolePanel();
			console.popup("Simula Runtime Console");
		}		
	}

	/// TRACE Utility.
	/// @param msg a trace message
	static void TRACE(final String msg) {
		println(Thread.currentThread().toString() + ": " + msg);
	}

	/// Utility: Internal error.
	/// @param msg an error message
	static void IERR(final String msg) {
		printError(msg);
		Thread.dumpStack();
		RTS_ENVIRONMENT.exit(-1);
	}

	// *********************************************************************
	// *** GET JAVA VERSION
	// *********************************************************************
	/// Returns System property "java.vm.specification.version"
	/// @return System property "java.vm.specification.version"
	static String getJavaID() {
		return ("JDK version " + System.getProperty("java.version"));
	}

	// *********************************************************************
	// *** TRACING AND DEBUGGING UTILITIES
	// *********************************************************************

	/// Debug utility method to print a snapshot in the generated code.
	/// @param sequ a sequence number
	/// @param msg a message
	public static void _SNAPSHOT(int sequ,String msg) {
		String id=RTS_UTIL.progamIdent;
		println("*** SNAPSHOT-"+id+"["+sequ+"]: "+msg);
		StackTraceElement stackTraceElement[] = Thread.currentThread().getStackTrace();
		int n = stackTraceElement.length;
		for (int i = 0; i < n; i++) {
			println("*** stackTraceElement["+i+"] = "+stackTraceElement[i]);
		}
	}

	/// Debug utility method to print a snapshot in the generated code.
	/// @param TOS top of operand stack
	/// @param sequ a sequence number
	/// @param msg a message
	public static void _SNAPSHOT(Object TOS,int sequ,String msg) {
		String id=RTS_UTIL.progamIdent;
		println("*** SNAPSHOT-"+id+"["+sequ+"]: "+msg+", TOS="+TOS.getClass().getSimpleName()+"  "+TOS);
		StackTraceElement stackTraceElement[] = Thread.currentThread().getStackTrace();
		int n = stackTraceElement.length;
		for (int i = 0; i < n; i++) {
			println("*** stackTraceElement["+i+"] = "+stackTraceElement[i]);
		}
	}

	/// Print static chain starting with the current instance.
	static void printStaticChain() {
		RTS_UTIL.printStaticChain(RTS_RTObject._CUR);
	}

	/// Print static chain starting with 'ins'
	/// @param ins argument
	static void printStaticChain(final RTS_RTObject ins) {
		RTS_RTObject x = ins;
		println("RTS_UTIL: printStaticChain: *** STATIC CHAIN:");
		while (x != null) {
			boolean qps = x.isQPSystemBlock();
			boolean dau = x.isDetachUsed();
			println(" - " + x.edObjectIdent() + "[QPSystemBlock=" + qps + ",detachUsed=" + dau + ",state=" + x._STATE
					+ ']');
			x = x._SL;
		}
		Thread.dumpStack();
	}

	/// Utility: Print Simula stack trace.
	/// @param start start index in Java stackTrace
	static void printSimulaStackTrace(final int start) {
		printSimulaStackTrace(Thread.currentThread().getStackTrace(), start);
	}

	/// Utility: Print Simula stack trace.
	/// @param e a Throwable
	/// @param start start index in Java stackTrace
	static void printSimulaStackTrace(final Throwable e, final int start) {
		printSimulaStackTrace(e.getStackTrace(), start);
	}

	/// Utility: Print Simula StackTrace.
	/// @param stackTraceElement Java stackTrace
	/// @param start start index in Java stackTrace
	private static void printSimulaStackTrace(final StackTraceElement stackTraceElement[], final int start) {
		if (currentModid != null) {
			RTS_UTIL.println("In " + currentModid + " at line " + currentSimLine);
		} else {
			int n = stackTraceElement.length;
			LOOP: for (int i = start; i < (n - 1); i++) {
				if (printSimulaLineInfo(stackTraceElement[i], " In "))
					break LOOP;
			}
		}
		if (RTS_Option.VERBOSE) {
			RTS_UTIL.println("*** DYNAMIC CHAIN:");
			int n = stackTraceElement.length;
			for (int i = start; i < (n - 1); i++) {
				printSimulaLineInfo(stackTraceElement[i], " - ");
				if (i > 30) {
					println("... SimulaStackTrace " + (n - 30) + " lines Truncated");
					return;
				}
			}
			printSimulaLineInfo(stackTraceElement[start], " - ");
			printStaticChain();
		} else {
			RTS_UTIL.println("(For more info: rerun with runtime option 'verbose')\n");
		}
	}

	/// Utility: Print SimulaLineInfo
	/// @param elt a StackTraceElement
	/// @param lead the keading string
	/// @return the resulting line info
	private static boolean printSimulaLineInfo(final StackTraceElement elt, final String lead) {
		try {
			Class<?> cls = Class.forName(elt.getClassName());
			Field field = cls.getField("_INFO");
			RTS_PROGINFO info = (RTS_PROGINFO) field.get(null);
			int[] lineMap = info.LINEMAP_;
			int x = 0;
			int javaLineNumber = elt.getLineNumber();
			try {
				while (lineMap[x] < javaLineNumber)
					x = x + 2;
				StringBuilder sb = new StringBuilder();
				sb.append(lead + info.ident);
				if (RTS_Option.VERBOSE)
					sb.append("(" + elt.getFileName() + ':' + elt.getLineNumber() + " " + elt.getMethodName() + ")");
				sb.append(" at Simula Source Line " + lineMap[x - 1] + "[" + info.file + "]");
				RTS_UTIL.println(sb.toString());
				return (true);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} catch (Exception e) {
//			if (Option.VERBOSE)   // TODO:
//				e.printStackTrace();
		}
		return (false);
	}

	// **********************************************************************
	// *** Debugging utility: Procedure printThreadList
	// **********************************************************************
	/// Print Thread list.
	static void printThreadList() {
		printThreadList(false);
	}

	/// Print Thread list.
	/// @param withStackTrace argument
	static synchronized void printThreadList(boolean withStackTrace) {
		Thread[] t = new Thread[50];
		int i = Thread.enumerate(t);
		RTS_UTIL.println("ACTIVE THREAD LIST:");
		IO.println("ACTIVE THREAD LIST:");
		for (int j = 0; j < i; j++) {
			Thread T = t[j];
			String msg = "  - " + T;
			if (T == Thread.currentThread())
				msg = msg + " = CurrentThread";
			RTS_UTIL.println(msg + "   STATE=" + T.getState());
			if (withStackTrace) {
				RTS_UTIL.printSimulaStackTrace(T.getStackTrace(), 0);
				RTS_UTIL.println("");
			}
		}
		RTS_UTIL.println("-----------------------------------------------------------------------------------------------");
	}

	/// Utility: Set current modid and Simula source line number.
	/// @param modid new current modid
	/// @param simLine new Simula source line number
	public static void _LINE(String modid, int simLine) {
		currentModid = modid;
		currentSimLine = simLine;
	}

}
