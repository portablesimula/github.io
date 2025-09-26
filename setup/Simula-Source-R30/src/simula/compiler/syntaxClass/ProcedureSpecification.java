/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass;

import java.io.IOException;
import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.syntaxClass.declaration.DeclarationScope;
import simula.compiler.syntaxClass.declaration.Parameter;
import simula.compiler.syntaxClass.declaration.ProcedureDeclaration;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.ObjectList;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

/// Procedure Specification.
/// <pre>
/// Simula Standard: 5.5.3 Virtual quantities
/// Simula Standard: 6.3 External procedure declaration
/// 
/// procedure-specification
///     = [ type ] PROCEDURE procedure-identifier procedure-head empty-body
///     
///    procedure-head
///        = [ formal-parameter-part ; [ mode-part ] specification-part  ] ;
///         
///    empty-body = dummy-statement
/// 
///    procedure-identifier = identifier
/// 
///       formal-parameter-part = "(" formal-parameter { , formal-parameter } ")"
///       
///          formal-parameter = identifier
///          
///       specification-part = specifier identifier-list ; { specifier identifier-list ; }
///       
///          specifier
///             = type [ array | procedure ]
///             | label
///             | switch
///             
///       mode-part 
///          = name-part [ value-part ]
///          | value-part [ name-part ]
///          
///          name-part = name identifier-list ;
///          value-part = value identifier-list ;
///          
///             identifier-list = identifier { , identifier }
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/ProcedureSpecification.java">
/// <b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class ProcedureSpecification extends SyntaxClass {
	
	/// The procedure identifier.
	private String identifier;

	/// The procedure's type.
	public Type type;
	
	/// The parameter list.
	public ObjectList<Parameter> parameterList;

	// ***********************************************************************************************
	// *** CONSTRUCTORS
	// ***********************************************************************************************
	/// Create a new ProcedureSpecification
	/// @param identifier procedure-identifier
	/// @param type procedure's type or null
	/// @param pList the parameter lList
	public ProcedureSpecification(final String identifier, final Type type, final ObjectList<Parameter> pList) {
		this.identifier = identifier;
		this.type = type;
		this.parameterList = pList;
	}

	// ***********************************************************************************************
	// *** Parsing: expectProcedureSpecification
	// ***********************************************************************************************
	/// Procedure Specification.
	/// 
	/// <pre>
	/// Syntax:
	/// 
	/// procedure-specification
	///     = [ type ] PROCEDURE procedure-identifier procedure-head empty-body
	///     
	///    procedure-head
	///        = [ formal-parameter-part ; [ mode-part ] procedure-specification-part  ] ;
	///         
	///    empty-body = dummy-statement
	/// 
	///    procedure-identifier = identifier
	/// 
	/// </pre>
	/// Precondition:  [ type ] PROCEDURE  is already read.
	/// @param type procedure's type
	/// @return a newly created ProcedureSpecification
	public static ProcedureSpecification expectProcedureSpecification(final Type type) {
		ProcedureDeclaration block = ProcedureDeclaration.expectProcedureDeclaration(type);
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("END ProcedureSpecification: " + block);
		Global.setScope(block.declaredIn);
		ProcedureSpecification procedureSpecification = new ProcedureSpecification(block.identifier, type, block.parameterList);
		return (procedureSpecification);
	}

	// ***********************************************************************************************
	// *** Utility: doChecking
	// ***********************************************************************************************
	/// Perform semantic checking.
	/// 
	/// @param scope the DeclarationScope
	public void doChecking(final DeclarationScope scope) {
		if (type != null)
			type.doChecking(scope);
		// Check parameters
		if (parameterList != null) {
			for (Parameter par : parameterList)
				par.doChecking();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (type != null)
			s.append(type).append(' ');
		s.append("PROCEDURE ").append(identifier).append(Parameter.editParameterList(parameterList));
		return (s.toString());
	}

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	public ProcedureSpecification() {
	}

	/// Write a ProcedureSpecification.
	/// @param spec the ProcedureSpecification.
	/// @param oupt the AttributeOutputStream.
	/// @throws IOException if something went wrong.
	public static void writeProcedureSpec(ProcedureSpecification spec,AttributeOutputStream oupt) throws IOException {
		if(spec == null) {
			oupt.writeBoolean(false);
		} else {
			Util.TRACE_OUTPUT("BEGIN Write ProcedureSpecification: " + spec.identifier);
			oupt.writeBoolean(true);
			oupt.writeString(spec.identifier);
			oupt.writeType(spec.type);
			oupt.writeObjectList(spec.parameterList);
		}
	}
	
	/// Read and return a ProcedureSpecification.
	/// @param inpt the AttributeInputStream to read from
	/// @return the ProcedureSpecification read from the stream.
	/// @throws IOException if something went wrong.
	@SuppressWarnings("unchecked")
	public static ProcedureSpecification readProcedureSpec(AttributeInputStream inpt) throws IOException {
		boolean present = inpt.readBoolean();
		if(!present) return(null);
		ProcedureSpecification spec = new ProcedureSpecification();
		spec.identifier = inpt.readString();
		spec.type = inpt.readType();
		spec.parameterList = (ObjectList<Parameter>) inpt.readObjectList();
		
		Util.TRACE_INPUT("END Read ProcedureSpecification: " + spec.identifier);
		return(spec);
	}

}
