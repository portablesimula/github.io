/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Vector;

import simula.compiler.syntaxClass.declaration.BlockDeclaration;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Java source-file coder.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/JavaSourceFileCoder.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen 
public final class JavaSourceFileCoder {
	
	/// The enclosing JavaSourceFileCoder
	private JavaSourceFileCoder enclosingJavaCoder;
	
	/// The underlying Java output writer.
	private final Writer writer;
	
	/// The line number map
	private final Vector<Integer> lineMap = new Vector<Integer>();
	
	/// The output file for generated Java code.
	final File javaOutputFile;

	/// Given as argument. Class, Procedure, Prefixed Block or Sub-Block.
	public final BlockDeclaration blockDeclaration;
	
	/// Signals that ByteCodeEngineering is necessary.
	public boolean mustDoByteCodeEngineering;

	/// Create a new JavaSourceFileCoder.
	/// @param blockDeclaration argument
	public JavaSourceFileCoder(final BlockDeclaration blockDeclaration) {
		this.blockDeclaration = blockDeclaration;
		Global.javaSourceFileCoders.add(this);
		enclosingJavaCoder = Global.currentJavaFileCoder;
		Global.currentJavaFileCoder = this;
		javaOutputFile = new File(Global.tempJavaFileDir, blockDeclaration.getJavaIdentifier() + ".java");
		try {
			javaOutputFile.getParentFile().mkdirs();
			if (Option.verbose)
				Util.TRACE("Output: " + javaOutputFile.getCanonicalPath());
			writer = new OutputStreamWriter(new FileOutputStream(javaOutputFile), Global._CHARSET);
			JavaSourceFileCoder.code("package " + Global.packetName + ";");
			JavaSourceFileCoder.code("// " + Global.simulaReleaseID + " Compiled at " + new Date());
			JavaSourceFileCoder.code("import simula.runtime.*;");
		} catch (IOException e) {
			throw new RuntimeException("Writing .java output failed", e);
		}
	}

	/// Edit the current module's identification.
	/// @return the current module's identification
	private String modid() {
		BlockDeclaration blk = Global.currentJavaFileCoder.blockDeclaration;
		return (blk.declarationKind + " " + blk.scopeID());
	}

	/// Returns the output file for generated Java code.
	/// @return the output file for generated Java code
	String getClassOutputFileName() {
		return (Global.tempClassFileDir + "/" + Global.packetName + '/' + blockDeclaration.getJavaIdentifier() + ".class");
	}

	/// Close Java output file.
	/// @throws RuntimeException if writing .java output failed
	public void closeJavaOutput() {
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Writing .java output failed", e);
		}
		Global.currentJavaFileCoder = enclosingJavaCoder;
		enclosingJavaCoder = null;
	}

	/// Output a debug code line.
	/// 
	/// To optimize, it is safe to drop such lines
	/// 
	/// @param line a debug code line
	public static void debug(final String line) {
		code(line);
	}

	/// Output a code line without comment.
	/// 
	/// @param line a code line
	public static void code(final String line) {
		Global.currentJavaFileCoder.write(Global.sourceLineNumber, line, Global.currentJavaFileCoder.modid());
	}

	/// Output a code line with comment.
	/// 
	/// @param line a code line
	/// @param comment a comment string
	public static void code(final String line, final String comment) {
		code(line + " // " + comment);
	}

	/// Current Java line number
	private int currentJavaLineNumber = 0;

	/// Previous source line number.
	private static int prevLineNumber = 0;
	
	/// Current indentation
	private int indent;

	/// Write a code line to the Java output writer.
	/// @param sourceLineNumber the source line number
	/// @param line the code line string
	/// @param modid module identifier
	private void write(final int sourceLineNumber, final String line, final String modid) {
		Util.ASSERT(sourceLineNumber > 0, "Invariant");
		try {
			currentJavaLineNumber++;
			if (prevLineNumber != sourceLineNumber) {
				String s0 = edIndent() + edLineNumberLine(sourceLineNumber, modid);
				appendLine(currentJavaLineNumber, sourceLineNumber);
				if (Option.internal.TRACE_CODING)
					Util.println("CODE " + sourceLineNumber + ": " + s0);
				currentJavaLineNumber++;
				writer.write(s0 + '\n');
			}
			if (line.contains("}")) {
				indent--;
				if (indent < 0)
					indent = 0;
			}
			String s = edIndent() + line;
			if (line.contains("{"))
				indent++;
			if (Option.internal.TRACE_CODING)
				Util.println("CODE " + sourceLineNumber + ": " + s);
			Util.ASSERT(writer != null, "Can't Output Code - writer==null");
			writer.write(s + '\n');
		} catch (IOException e) {
			Util.IERR("Error Writing File: " + javaOutputFile, e);
		}
		prevLineNumber = sourceLineNumber;
	}

	/// Code Utility: Edit line number comment line.
	///
	/// On the form:
	///
	/// 		// JavaLine currentJavaLineNumber <== SourceLine simulaLine
	/// @param simulaLine Simula line number
	/// @param modid the module identifier
	/// @return the resulting Java source line
	private String edLineNumberLine(final int simulaLine, final String modid) {
		StringBuilder sb = new StringBuilder();
		if (Global.duringSTM_Coding && Option.internal.GNERATE_LINE_CALLS) {
			sb.append("RTS_UTIL._LINE(\"").append(modid).append("\",").append(simulaLine).append("); ");
		}
		sb.append("// JavaLine ").append(currentJavaLineNumber).append(" <== SourceLine ").append(simulaLine);
		return (sb.toString());
	}

	/// Utility: Edit indent string
	/// @return the indent string
	private String edIndent() {
		int i = indent;
		String s = "";
		while ((i--) > 0)
			s = s + "    ";
		return (s);
	}

	/// Append an entry to the line map.
	/// @param javaLine the Java line number
	/// @param simulaLine the simula line number
	private void appendLine(final int javaLine, final int simulaLine) {
		lineMap.add(javaLine);
		lineMap.addElement(simulaLine);
	}

	/// Output program info. I.e. identifier and lineMap.
	public void codeProgramInfo() {
		appendLine(currentJavaLineNumber, blockDeclaration.lastLineNumber);
		// public static RTS_PROGINFO _INFO=new
		// RTS_PROGINFO("file.sim","MainProgram",1,4,12,5,14,12,32,14,37,16);
		StringBuilder s = new StringBuilder();
		s.append(edIndent() + "public static RTS_PROGINFO _INFO=new RTS_PROGINFO(\"");
		s.append(Global.sourceFileName);
		s.append("\",\"");
		s.append(blockDeclaration.declarationKind + " " + blockDeclaration.identifier);
		s.append('"');
		for (Integer i : lineMap) {
			s.append(',');
			s.append("" + i);
		}
		s.append(");");
		writeCode(s.toString());
	}

	/// Write a code line to the Java output writer.
	/// @param s the code line string
	private void writeCode(String s) {
		if (Option.internal.TRACE_CODING)
			Util.println("CODE " + Global.sourceLineNumber + ": " + s);
		Util.ASSERT(writer != null, "Can't Output Code - writer==null");
		try {
			writer.write(s.toString() + '\n');
		} catch (IOException e) {
			Util.IERR("Error Writing File: " + javaOutputFile, e);
		}

	}

	@Override
	public String toString() {
		return ("JavaModule " + blockDeclaration + ", javaOutputFile=" + javaOutputFile);
	}

}
