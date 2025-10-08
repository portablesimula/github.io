/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.transform;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Predicate;

import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.ClassFile;
import java.lang.classfile.MethodModel;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// ClassFileTransform.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/transform/ClassFileTransform.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public abstract class ClassFileTransform {
	/** Default Constructor: NOT USED */ private ClassFileTransform() {}

	/// Repair a single .class file.
	/// 
	/// A [ClassFile] is read and parsed into a [ClassModel].
	/// 
	/// The method '_STM' is filtered from this model and transformed using the [SimulaCodeTransform].
	/// @param inputFileName  the input .class file name
	/// @param outputFileName the output .class file name
	/// @throws IOException if an I/O error occurs
	public static void doRepairSingleByteCode(final String inputFileName, final String outputFileName) throws IOException {
		if (Option.internal.TRACE_REPAIRING)
			Util.TRACE("ClassFileTransform.doRepairSingleByteCode: Input = " + inputFileName);
		if (Option.internal.LIST_INPUT_INSTRUCTION_LIST)
			Util.doListClassFile(inputFileName);
		FileInputStream inpt = new FileInputStream(inputFileName);
		byte[] bytes = inpt.readAllBytes();
		inpt.close();
		if (Option.internal.TRACE_REPAIRING_INPUT)
			Util.TRACE("ClassFileTransform.doRepairSingleByteCode: Input=" + inputFileName);
		ClassFile cf = ClassFile.of();
		ClassModel classModel = cf.parse(bytes);
		
		Predicate<MethodModel> filter = model -> (model.methodName().equalsString("_STM"));
		ClassTransform transform = ClassTransform.transformingMethodBodies(filter, new SimulaCodeTransform());
		if (transform != null) {
			byte[] bytes2 = cf.transformClass(classModel, transform);
			if (Option.internal.TRACE_REPAIRING_OUTPUT)
				Util.TRACE("ClassFileTransform.doRepairSingleByteCode: Output=" + outputFileName);
			FileOutputStream oupt = new FileOutputStream(outputFileName);
			oupt.write(bytes2);
			oupt.flush();
			oupt.close();
		} else
			Util.IERR("ClassFileTransform.doRepairSingleByteCode: _STM Method not found");
		if (Option.internal.LIST_REPAIRED_INSTRUCTION_LIST)
			Util.doListClassFile(outputFileName);
	}

}
