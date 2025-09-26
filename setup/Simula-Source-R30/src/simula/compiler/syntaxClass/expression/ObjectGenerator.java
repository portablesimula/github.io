/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.expression;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Iterator;
import java.util.Vector;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.parsing.Parse;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.declaration.ClassDeclaration;
import simula.compiler.syntaxClass.declaration.Declaration;
import simula.compiler.syntaxClass.declaration.Parameter;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// ObjectGenerator i.e. new Object expression.
/// 
/// <pre>
/// 
/// Simula Standard: 3.8 Object expressions
/// 
/// object-generator = NEW class-identifier [ ( actual-parameter-part ) ]
/// 
///    actual-parameter-part
///         =  "("  actual-parameter  {  ,  actual-parameter  }  ")"
/// 
///       actual-parameter
///           =  expression
///           |  array-identifier-1
///           |  switch-identifier
///           |  procedure-identifier-1
/// 
/// </pre>
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/ObjectGenerator.java"><b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public final class ObjectGenerator extends Expression {
	
	/// The class-identifier
	private String classIdentifier;
	
	/// The semantic meaning 
	Meaning meaning;
	
	/// The actual parameters before checking
	private Vector<Expression> params;
	
	/// The actual parameters after checking
	private Vector<Expression> checkedParams = new Vector<Expression>();

	/// Create a new ObjectGenerator.
	/// @param ident class-identifier
	/// @param params the actual parameters
	private ObjectGenerator(final String ident,final Vector<Expression> params) {
		this.classIdentifier = ident;
		this.type = Type.Ref(classIdentifier);
		this.params = params;
		if (Option.internal.TRACE_PARSE) Util.TRACE("NEW ObjectGenerator: " + toString());
	}

	/// Parse an object generator.
	/// <pre>
	/// object-generator = NEW class-identifier [ ( actual-parameter-part ) ]
	/// 
	///    actual-parameter-part
	///         =  "("  actual-parameter  {  ,  actual-parameter  }  ")"
	/// </pre>
	/// @return the newly created ObjectGenerator.
	static Expression expectNew() {
		if (Option.internal.TRACE_PARSE)
			Util.TRACE("Parse ObjectGenerator, current=" + Parse.currentToken);
		String classIdentifier = Parse.expectIdentifier();
		Vector<Expression> params = new Vector<Expression>();
		if (Parse.accept(KeyWord.BEGPAR)) {
			do {
				Expression par=acceptExpression();
				if(par==null) Util.error("Missing class parameter");
				else params.add(par);
			} while (Parse.accept(KeyWord.COMMA));
			Parse.expect(KeyWord.ENDPAR);
		}

		Expression expr = new ObjectGenerator(classIdentifier, params);
		return (expr);
	}

	@Override
	public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		Global.sourceLineNumber = lineNumber;
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("BEGIN ObjectGenerator(" + classIdentifier + ").doChecking - Current Scope Chain: " + Global.getCurrentScope().edScopeChain());
		meaning = Global.getCurrentScope().findMeaning(classIdentifier);
		if (meaning == null) {
			Util.error("Undefined class identifier: " + classIdentifier);
			meaning = new Meaning(null, null); // Error Recovery: No Meaning
		}
		if (!(meaning.declaredAs instanceof ClassDeclaration)) {
			Util.error("NEW " + classIdentifier + ": Not a Class");
			return;
		}
		ClassDeclaration cls = (ClassDeclaration) meaning.declaredAs;
		// Check parameters
		Iterator<Parameter> formalIterator = cls.parameterIterator();
		Iterator<Expression> actualIterator = params.iterator();
		while (actualIterator.hasNext()) {
			if (!formalIterator.hasNext())
				Util.error("Wrong number of parameters to " + cls);
			Declaration formalParameter = formalIterator.next();
			Type formalType = formalParameter.type;
			if (Option.internal.TRACE_CHECKER)
				Util.TRACE("Formal Parameter: " + formalParameter + ", Formal Type=" + formalType);
			Expression actualParameter = actualIterator.next();
			actualParameter.doChecking();

			Type actualType = actualParameter.type;
			if (Option.internal.TRACE_CHECKER)
				Util.TRACE("Actual Parameter: " + actualType + " " + actualParameter + ", Actual Type=" + actualType);
			Expression checkedParameter = TypeConversion.testAndCreate(formalType, actualParameter);
			checkedParameter.backLink = this;
			checkedParams.add(checkedParameter);

		}
		if (formalIterator.hasNext())
			Util.error("Missing parameter("+formalIterator.next()+") to " + cls);
		if (Option.internal.TRACE_CHECKER)
			Util.TRACE("END ObjectGenerator(" + classIdentifier + ").doChecking: type=" + type);
		SET_SEMANTICS_CHECKED();
	}

	// Returns true if this expression may be used as a statement.
	@Override
	public boolean maybeStatement() {
		ASSERT_SEMANTICS_CHECKED();
		return (true);
	}

	@Override
	public String toJavaCode() {
		ASSERT_SEMANTICS_CHECKED();
		StringBuilder s = new StringBuilder();
		String classIdent = meaning.declaredAs.getJavaIdentifier();
		s.append("new ").append(classIdent).append('(');
		s.append(meaning.edUnqualifiedStaticLink());

		ClassDeclaration cls = (ClassDeclaration) meaning.declaredAs;
		Iterator<Parameter> formalIterator = cls.parameterIterator();
		for (Expression par : checkedParams) {
			Parameter formalParameter = formalIterator.next();
			if (formalParameter.mode == Parameter.Mode.value) {
				if (par.type.keyWord == Type.T_TEXT)
					s.append(",copy(").append(par.toJavaCode()).append(')');
				else if (formalParameter.kind == Parameter.Kind.Array) {
					String cast=par.type.toJavaArrayType();
					s.append(",((").append(cast).append(')').append(par.toJavaCode()).append(").COPY()");
				}
				else
					s.append(',').append(par.toJavaCode());
			} else
				s.append(',').append(par.toJavaCode());
		}

		s.append(')');
		if (cls.isDetachUsed()) {
			s.append("._START()");
			String start=s.toString();
			if(backLink!=null) start="(("+classIdent+')'+start+')';
			return(start);
		} else
			s.append("._STM()");
		return (s.toString());
	}

	@Override
	public void buildEvaluation(Expression rightPart,CodeBuilder codeBuilder) {	setLineNumber();
		ASSERT_SEMANTICS_CHECKED();
		//  new adHoc03_A((_CUR))._STM();
		//
		//   0: new           #44                 // class simulaTestPrograms/adHoc03_A
		//   3: dup
		//   4: getstatic     #46                 // Field _CUR:Lsimula/runtime/RTS_RTObject;
		//   7: invokespecial #49                 // Method simulaTestPrograms/adHoc03_A."<init>":(Lsimula/runtime/RTS_RTObject;)V
		//  10: invokevirtual #50                 // Method simulaTestPrograms/adHoc03_A._STM:()LsimulaTestPrograms/adHoc03_A;
		//  13: pop
		//
		// 	x:-new A;
		//   1: new           #19                 // class simulaTestPrograms/adHoc07_A
		//   4: dup
		//   5: getstatic     #21                 // Field _CUR:Lsimula/runtime/RTS_RTObject;
		//   8: invokespecial #25                 // Method simulaTestPrograms/adHoc07_A."<init>":(Lsimula/runtime/RTS_RTObject;)V
		//  11: invokevirtual #26                 // Method simulaTestPrograms/adHoc07_A._START:()Lsimula/runtime/RTS_RTObject;
		//  14: checkcast     #19                 // class simulaTestPrograms/adHoc07_A
		//  17: putfield      #7                  // Field x_2:LsimulaTestPrograms/adHoc07_A;

		ClassDeclaration cls = (ClassDeclaration) meaning.declaredAs;
		ClassDesc CD_cls=cls.getClassDesc();
		codeBuilder
			.new_(CD_cls)
			.dup();
		meaning.buildQualifiedStaticLink(codeBuilder);

		// Push parameters
		Iterator<Parameter> formalIterator = cls.parameterIterator();
		for (Expression par : checkedParams) {
			par.buildEvaluation(null,codeBuilder);
			Parameter formalParameter = formalIterator.next();
			if (formalParameter.mode == Parameter.Mode.value) {
				if (par.type.keyWord == Type.T_TEXT) {
					RTS.invokestatic_ENVIRONMENT_copy(codeBuilder);
				}
				else if (formalParameter.kind == Parameter.Kind.Array) {
					RTS.invokevirtual_ARRAY_copy(codeBuilder);
				}
			}
		}

		codeBuilder.invokespecial(CD_cls, "<init>", cls.getConstructorMethodTypeDesc());

		// _STM(); or _START
		if(cls.isDetachUsed()) {
			String resultType="Lsimula/runtime/RTS_RTObject;";
			codeBuilder.invokevirtual(CD_cls, "_START", MethodTypeDesc.ofDescriptor("()" + resultType));
		} else {
			String resultType="Lsimula/runtime/RTS_RTObject;";
			codeBuilder.invokevirtual(CD_cls, "_STM", MethodTypeDesc.ofDescriptor("()" + resultType));
		}
		if(backLink == null) codeBuilder.pop();
		else codeBuilder.checkcast(CD_cls);
	}


	@Override
	public String toString() {
		return (("NEW " + classIdentifier + params).replace('[', '(').replace(']', ')'));
	}
	
	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private ObjectGenerator() {	}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("ObjectGenerator: "+this);
		oupt.writeKind(ObjectKind.ObjectGenerator);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** Expression
		oupt.writeType(type);
		oupt.writeObj(backLink);
		// *** ObjectGenerator
		oupt.writeString(classIdentifier);
		if(params == null) {
			oupt.writeShort(-1);			
		} else {
			oupt.writeShort(params.size());
			for(Expression par:params) oupt.writeObj(par);
		}
	}
	
	/// Read and return an ObjectGenerator object.
	/// @param inpt the AttributeInputStream to read from
	/// @return the ObjectGenerator object read from the stream.
	/// @throws IOException if something went wrong.
	public static ObjectGenerator readObject(AttributeInputStream inpt) throws IOException {
		ObjectGenerator gen = new ObjectGenerator();
		gen.OBJECT_SEQU = inpt.readSEQU(gen);
		// *** SyntaxClass
		gen.lineNumber = inpt.readShort();
		// *** Expression
		gen.type = inpt.readType();
		gen.backLink = (SyntaxClass) inpt.readObj();
		// *** ObjectGenerator
		gen.classIdentifier = inpt.readString();
		int n = inpt.readShort();
		if(n >= 0) {
			gen.params = new Vector<Expression>();
			for(int i=0;i<n;i++)
				gen.params.add((Expression) inpt.readObj());
		}
		Util.TRACE_INPUT("ObjectGenerator: "+gen);
		return(gen);
	}

}
