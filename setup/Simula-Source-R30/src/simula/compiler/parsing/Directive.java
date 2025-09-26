/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.parsing;

import java.io.File;
import simula.compiler.utilities.Util;

/// Utility class Directive.
/// 
/// Link to GitHub: <a href="https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/parsing/Directive.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class Directive {
	/// Default constructor.
	Directive() {}

	/// Treat a directive line.
	/// @param scanner the scanner
	/// @param id      the directive identifier
	/// @param arg     the directive argument
	static void treatDirectiveLine(final SimulaScanner scanner, final String id, final String arg) {
		if (id.equalsIgnoreCase("OPTION"))			; // Ignored in this implementation
		else if (id.equalsIgnoreCase("INSERT"))		Directive.insert(scanner, arg);
		else if (id.equalsIgnoreCase("SPORT"))		; // Ignored in this implementation
		else if (id.equalsIgnoreCase("TITLE"))		; // Ignored in this implementation
		else if (id.equalsIgnoreCase("PAGE"))		; // Ignored in this implementation
		else if (id.equalsIgnoreCase("KEEP_JAVA"))	; // Ignored in this implementation
		else if (id.equalsIgnoreCase("EOF"))		scanner.sourceFileReader.forceEOF();
		else Util.warning("Unknown Compiler Directive: " + id + ' ' + arg);
	}

	/// %INSERT file-name
	/// 
	/// Will cause the compiler to include the indicated file at this place in the
	/// source input stream. INSERT may occur in the included file.
	/// @param scanner the SimulaScanner
	/// @param fileName the file to insert
	private static void insert(final SimulaScanner scanner, final String fileName) {
		Util.warning("%INSERT " + fileName);
		File file = new File(fileName);
		if (file.exists() && file.canRead()) {
			scanner.insert(file);
		} else
			Util.error("Can't open " + fileName + " for reading");
	}

}
