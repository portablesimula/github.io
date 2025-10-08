/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.expression;

import java.io.IOException;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.constant.MethodTypeDesc;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.syntaxClass.SyntaxClass;
import simula.compiler.syntaxClass.Type;
import simula.compiler.utilities.Global;
import simula.compiler.utilities.KeyWord;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.RTS;
import simula.compiler.utilities.Util;

/// Constant.
/// 
/// All constants are treated by the Lexicographical Scanner.
/// 
/// <pre>
/// 
/// Syntax:
/// 
///   Constant = unsigned-number | string | character-constant | NONE | NOTEXT
///   
/// </pre>
/// Link to GitHub: <a href="https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/expression/Constant.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
/// @see simula.compiler.parsing.SimulaScanner
public final class Constant extends Expression {
	
	/// The constant's value
	public Object value;

	/// Create a new Constant.
	/// @param type the constant's type
	/// @param value the constant's value
	public Constant(final Type type,final Object value) {
		this.type=type;
		this.value = value;
	}
	
	/// Create a real type Constant.
	/// @param value a real type value
	/// @return the resulting Constant
    static Constant createRealType(final Object value)
    { Type type=Type.Real;
      if(value instanceof Double) type=Type.LongReal;
      return(new Constant(type,value));
    }
    
    /// Returns the type of this number.
    /// @param n the number
    /// @return the type of this number.
    private static Type getType(final Number n) {
    	if(n instanceof Integer) return(Type.Integer);
    	if(n instanceof Long) return(Type.Integer);
    	if(n instanceof Float) return(Type.Real);
    	return(Type.LongReal);
    }
    
     /// Simplify this Constant.
    /// @param opr an unary operation
    /// @param rhn a right hand Number
    /// @return the resulting Constant
    static Constant evaluate(final int opr,final Number rhn) { 
    	Type type=getType(rhn);
		Number result=null;
		switch(type.keyWord) {
			case Type.T_INTEGER -> {
				switch(opr) {
		        	case KeyWord.PLUS: result=rhn.intValue(); break;
		        	case KeyWord.MINUS: result= - rhn.intValue(); break;
		        	default:
				} }
			case Type.T_REAL -> {
			switch(opr) {
        		case KeyWord.PLUS: result=rhn.floatValue(); break;
        		case KeyWord.MINUS: result= - rhn.floatValue(); break;
        		default:
			} }
			case Type.T_LONG_REAL -> {
			switch(opr) {
				case KeyWord.PLUS: result=rhn.doubleValue(); break;
				case KeyWord.MINUS: result= - rhn.doubleValue(); break;
				default:
			} }
		}
		if(result==null) Util.IERR();
		return(new Constant(type,result));
    }
  
    /// Simplify this Constant.
    /// @param lhn a left hand Number
    /// @param opr an binary operation
    /// @param rhn a right hand Number
    /// @return the resulting Constant
    static Constant evaluate(final Number lhn,final int opr,final Number rhn) { 
    	Type type=Type.arithmeticTypeConversion(getType(lhn),getType(rhn));
		if(opr==KeyWord.DIV && type.keyWord == Type.T_INTEGER) type=Type.Real;
		Number result=null;
		switch(type.keyWord) {
			case Type.T_INTEGER -> {
				switch(opr) {
	        		case KeyWord.PLUS   -> result=lhn.longValue() + rhn.longValue();
	        		case KeyWord.MINUS  -> result=lhn.longValue() - rhn.longValue();
	        		case KeyWord.MUL    -> result=lhn.longValue() * rhn.longValue();
	        		case KeyWord.INTDIV -> result=lhn.longValue() / rhn.longValue();
	        		case KeyWord.EXP    -> result=Util.IPOW(lhn.longValue(),rhn.longValue());
	        		default     -> Util.IERR();
				}
				if(result.longValue() > Integer.MAX_VALUE || result.longValue() < Integer.MIN_VALUE)
					Util.error("Arithmetic overflow: "+lhn+' '+KeyWord.edit(opr)+' '+rhn);
				result=(int) result.longValue();
			}
			case Type.T_REAL -> {
			switch(opr) {
        		case KeyWord.PLUS  -> result=lhn.floatValue() + rhn.floatValue();
        		case KeyWord.MINUS -> result=lhn.floatValue() - rhn.floatValue();
        		case KeyWord.MUL   -> result=lhn.floatValue() * rhn.floatValue();
        		case KeyWord.DIV   -> result=lhn.floatValue() / rhn.floatValue();
        		case KeyWord.EXP   -> result=Math.pow(lhn.floatValue(),rhn.floatValue());
        		default    -> Util.IERR();
			} }
			case Type.T_LONG_REAL -> {
			switch(opr) {
				case KeyWord.PLUS  -> result=lhn.doubleValue() + rhn.doubleValue();
				case KeyWord.MINUS -> result=lhn.doubleValue() - rhn.doubleValue();
				case KeyWord.MUL   -> result=lhn.doubleValue() * rhn.doubleValue();
				case KeyWord.DIV   -> result=lhn.doubleValue() / rhn.doubleValue();
				case KeyWord.EXP   -> result=Math.pow(lhn.doubleValue(),rhn.doubleValue());
        		default    -> Util.IERR();
			} }
		}
		if(result==null) Util.IERR();
		return(new Constant(type,result));
    }
    
	@Override
    public void doChecking() {
		if (IS_SEMANTICS_CHECKED())	return;
		Global.sourceLineNumber=lineNumber;
		this.type.doChecking(Global.getCurrentScope());
		SET_SEMANTICS_CHECKED();
	}

    // Returns true if this expression may be used as a statement.
	@Override
    public boolean maybeStatement() {
    	ASSERT_SEMANTICS_CHECKED();
		return(false);  
    }

	@Override
	public String toJavaCode() {
		//ASSERT_SEMANTICS_CHECKED(); // ØM: Ad'Hoc
		switch(type.keyWord) {
			case Type.T_TEXT -> {
				if(value==null) return("null");
				String val=value.toString();
				val=encode(val);
				return "new RTS_TXT(\""+val+"\")";
			}
			case Type.T_CHARACTER -> {
				char charValue=((Character)value).charValue();
				if(charValue=='\\') return("'\\\\'");
				int intValue=(int)charValue;
				if(intValue!='\'' && intValue>32 && intValue<127) return("'"+value+"'");
				return "((char)"+intValue+')';
			}
			case Type.T_INTEGER -> { return ""+((Number)value).intValue(); }
			case Type.T_REAL    -> { return ""+((Number)value).floatValue()+'f'; }
			case Type.T_LONG_REAL -> { return ""+((Number)value).doubleValue()+'d'; }
			default -> { return ""+value; }
		}
	}
	
	/// Encode a string with escape sequences.
	/// @param s the string
	/// @return a string with escape sequences.
	private String encode(final String s) {
		StringBuilder b = new StringBuilder();
		for (char c : s.toCharArray()) {
			if(c < 32) {
				if (c == '\"') b.append("\\\"");
				else if (c == '\\') b.append("\\\\");
				else if (c == '\n') b.append("\\n");
				else if (c == '\r') b.append("\\r");
				else if (c == '\t') b.append("\\t");
				else if (c == '\b') b.append("\\b");
				else if (c == '\f') b.append("\\f");
				else if (c == '\'') b.append("\\'");
				else appendHex(b,c);
			} else if(c == 92) b.append("\\\\");
			  else if(c > 90 & c < 97) appendHex(b,c);
			  else if(c == 92) b.append("\\");
			  else if(c > 123) appendHex(b,c);
			  else if (c == '\"') b.append("\\\"");
			  else b.append(c);
		}
		return (b.toString());
	}
	
	/// Java Encoding Utility: Edit hex(c) and append it to the given StringBuilder.
	/// @param sb the StringBuilder
	/// @param c the input character
	private void appendHex(StringBuilder sb,char c) {
		String hex=Integer.toHexString(c);
		switch(hex.length()) {
		case 1-> hex="000"+hex;
		case 2-> hex="00"+hex;
		case 3-> hex="0"+hex;
		}
		sb.append("\\u"+hex);	
	}

	@Override
	public void buildEvaluation(Expression rightPart,CodeBuilder codeBuilder) {	setLineNumber();
		//ASSERT_SEMANTICS_CHECKED(); // ØM: Ad'Hoc
		ConstantPoolBuilder pool=codeBuilder.constantPool();
		if(this.value==null)
			codeBuilder.aconst_null();
		else switch(type.keyWord) {
			case Type.T_BOOLEAN -> buildIntConst(codeBuilder, (boolean) value);
			
			case Type.T_CHARACTER -> buildIntConst(codeBuilder, (int)((char)value));
					
			case Type.T_INTEGER -> {
				Number i = (Number) value;
				buildIntConst(codeBuilder, i.intValue());
			}
					
			case Type.T_REAL -> {
				float f = (float) value;
				if (f == 0)		 codeBuilder.fconst_0();
				else if (f == 1) codeBuilder.fconst_1();
				else if (f == 2) codeBuilder.fconst_2();
				else codeBuilder.ldc(pool.floatEntry(f));
			}
					
			case Type.T_LONG_REAL -> {
				double d=(double) value;
				if(d == 0) codeBuilder.dconst_0();
				else if(d == 1) codeBuilder.dconst_1();
				else codeBuilder.ldc(pool.doubleEntry(d));
			}
					
			case Type.T_TEXT -> {
				codeBuilder
					.new_(RTS.CD.RTS_TXT)
					.dup()
					.ldc(pool.stringEntry((String) value))
					.invokespecial(RTS.CD.RTS_TXT, "<init>", MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)V"));
			}
				
			default -> Util.IERR();
		}
	}

	/// ClassFile coding: Build boolean const as an integer 1:true or 0:false.
	/// @param codeBuilder the codeBuilder to use.
	/// @param b the actual boolean value.
	public static void buildIntConst(CodeBuilder codeBuilder, boolean b) {
		if(b) codeBuilder.iconst_1(); else codeBuilder.iconst_0();
	}
	
	/// ClassFile coding: Build integer const.
	/// @param codeBuilder the codeBuilder to use.
	/// @param i the actual integer value.
	public static void buildIntConst(CodeBuilder codeBuilder, int i) {
		switch(i) {
			case 0 -> codeBuilder.iconst_0();
			case 1 -> codeBuilder.iconst_1();
			case 2 -> codeBuilder.iconst_2();
			case 3 -> codeBuilder.iconst_3();
			case 4 -> codeBuilder.iconst_4();
			case 5 -> codeBuilder.iconst_5();
			default -> { // bipush, sipush or ldc
				if (i < Byte.MAX_VALUE && i > Byte.MIN_VALUE)
					codeBuilder.bipush(i);
				else if (i < Short.MAX_VALUE && i > Short.MIN_VALUE)
					codeBuilder.sipush(i);
				else
					codeBuilder.ldc(codeBuilder.constantPool().intEntry(i));
			}
		}
	}

	@Override
	public String toString() {
		if(type != null && type.keyWord == Type.T_TEXT) return("\""+value+'"');
		return("Constant type="+type+", value="+value);
	}

	
	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	private Constant() {}

	@Override
	public void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("Constant: "+type+' '+value);
		oupt.writeKind(ObjectKind.Constant);
		oupt.writeShort(OBJECT_SEQU);
		// *** SyntaxClass
		oupt.writeShort(lineNumber);
		// *** Expression
		oupt.writeType(type);
		oupt.writeObj(backLink);
		// *** Constant
		oupt.writeConstant(value);
	}
	
	/// Read and return a Constant.
	/// @param inpt the AttributeInputStream to read from
	/// @return the Constant read from the stream.
	/// @throws IOException if something went wrong.
	public static Constant readObject(AttributeInputStream inpt) throws IOException {
		Constant cnst = new Constant();
		cnst.OBJECT_SEQU = inpt.readSEQU(cnst);
		// *** SyntaxClass
		cnst.lineNumber = inpt.readShort();
		// *** Expression
		cnst.type = inpt.readType();
		cnst.backLink = (SyntaxClass) inpt.readObj();
		// *** Constant
		cnst.value=inpt.readConstant();
		Util.TRACE_INPUT("Constant: "+cnst);
		return(cnst);
	}

}
