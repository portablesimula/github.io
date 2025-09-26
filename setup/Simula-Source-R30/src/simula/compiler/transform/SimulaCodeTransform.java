/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.transform;

import java.util.List;
import java.util.Vector;

import java.lang.constant.ConstantDesc;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Label;

import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LabelTarget;
import java.lang.classfile.instruction.SwitchCase;

import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Simula .class file transformer.
///
/// To easily modify the code, the Simula Compiler generates certain method call in the .java file:
/// <br>Process the stream of [CodeElement]s looking for two particular code sequences:
/// 
/// CASE 1: Repair the JUMPTABLE(_JTX, n).
/// 
/// This method-call is a placeholder for where to put in a Jump-Table.
/// <br>The parameter 'n' is the number of cases.
///
/// JUMPTABLE will always occur before any any labels.
///
/// Locate the instruction sequence:
/// <pre>
///    ... any instruction(s)
///    GETFIELD _JTX
///    ICONST n   or  BIPUSH  or SIPUSH
///    INVOKESTATIC _JUMPTABLE
/// </pre>
/// Replace it by:
/// <pre>
///    ... any instruction(s)
///    GETFIELD _JTX
///    TABLESWITCH ... uses 'n' labels which is binded later.
/// </pre>
///
///
/// CASE 2: Repair a LABEL(n).
/// 
/// This method-call is used to signal the occurrence of a Simula Label.
/// <br>The label 'n' in the TABLESWITCH is binded.
/// 
/// Locate the instruction sequence:
/// <pre>
///    ICONST n   or  BIPUSH  or SIPUSH
///    INVOKESTATIC _SIM_LABEL
///    NEXT-INSTRUCTION
/// </pre>
/// Replace it by:
/// <pre>
///    PREV-INSTRUCTION
///    LABELBINDING( case n )
///    NEXT-INSTRUCTION
/// </pre>
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/transform/SimulaCodeTransform.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
final class SimulaCodeTransform implements CodeTransform {
	/// The switch cases.
	private List<SwitchCase> cases;
	
	/// The previous CodeElement
	private CodeElement prevElement;
	
	/** Default Constructor. */ SimulaCodeTransform() {}

	@Override
	public void atEnd(CodeBuilder builder) {
		if (prevElement != null) {
			if (Option.internal.TRACE_REPAIRING_OUTPUT)
				Util.TRACE("SimulaCodeTransform.atEnd: Output " + prevElement);
			builder.with(prevElement);
			prevElement = null;
		}
	}

	@Override
	public void accept(CodeBuilder builder, CodeElement element) {
		if (Option.internal.TRACE_REPAIRING_INPUT)
			Util.TRACE("SimulaCodeTransform.accept: Input " + element);
		if (element instanceof InvokeInstruction instr) {
			if (instr.name().equalsString("_JUMPTABLE")) {
				// *******************************************************************************
				// *** JUMPTABLE Case
				// *******************************************************************************
				// iconst_n // Number of cases (tableSize)
				// invokestatic _JUMPTABLE
				//
				// Output:
				//
				// tableswitch ...
				//
				int tableSize = getConst(prevElement);
				prevElement = null;
				if (Option.internal.TRACE_REPAIRING)
					Util.TRACE("SimulaCodeTransform.accept: Define TableSwitch " + tableSize);
				cases = new Vector<SwitchCase>();
				for (int i = 1; i <= tableSize; i++) {
					cases.add(SwitchCase.of(i, builder.newLabel()));
				}
				// Build the TableSwitch Instruction
				Label defaultTarget = builder.newLabel(); // beginning of the default handler block.
				int lowValue = 1; // the minimum key value.
				int highValue = cases.size(); // the maximum key value.
				if (Option.internal.TRACE_REPAIRING_OUTPUT)
					Util.TRACE("SimulaCodeTransform.accept: Output TableSwitch");
				if (Option.internal.TRACE_REPAIRING_OUTPUT)
					Util.TRACE("SimulaCodeTransform.accept: Output defaultTarget=" + defaultTarget);
				builder.tableswitch(lowValue, highValue, defaultTarget, cases).labelBinding(defaultTarget);
				return;
			}
			if (instr.name().equalsString("_SIM_LABEL")) {
				// *******************************************************************************
				// *** LABEL Cases
				// *******************************************************************************
				// iconst_n // Case number
				// invokestatic _SIM_LABEL
				//
				// Output:
				//
				// label (pseudo instruction)
				//
				int caseValue = getConst(prevElement);
				prevElement = null;
				if (Option.internal.TRACE_REPAIRING)
					Util.TRACE("SimulaCodeTransform.accept: Define Label  " + caseValue);
				LabelTarget target = (LabelTarget) cases.get(caseValue - 1).target();
				builder.labelBinding(target.label());
				if (Option.internal.TRACE_REPAIRING_OUTPUT)
					Util.TRACE("SimulaCodeTransform.accept: Output " + target);
				return;
			}
		}
		if (Option.internal.TRACE_REPAIRING_OUTPUT)
			Util.TRACE("SimulaCodeTransform.accept: Output " + prevElement);
		if (prevElement != null)
			builder.with(prevElement);
		prevElement = element;
	}

	/// ConstantInstruction: ICONST n or BIPUSH or SIPUSH
	/// 
	/// @param element a CodeElement: ICONST n or BIPUSH or SIPUSH
	/// @return the integer constant value
	/// @throws RuntimeException if something went wrong
	private int getConst(CodeElement element) {
		if (element instanceof ConstantInstruction instr) {
			ConstantDesc val = instr.constantValue();
			Integer ival = (Integer) val;
			return (ival.intValue());
		}
		throw new RuntimeException("Expected ConstantInstruction, GOT " + element);
	}

	@Override
	public String toString() {
		return ("SimulaCodeTransform");
	}
}
