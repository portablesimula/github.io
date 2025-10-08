/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.utilities;

/// The Simula Keywords and some additional symbols.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/KeyWord.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public class KeyWord {
	/** Default Constructor: Not used */ private KeyWord() {}
	// Simula Reserved Words
	/** Simula Keyword */ public final static int ACTIVATE     = 1;
	/** Simula Keyword */ public final static int AND          = 2;
	/** Simula Keyword */ public final static int AFTER        = 3;
	/** Simula Keyword */ public final static int ARRAY        = 4;
	/** Simula Keyword */ public final static int AT           = 5;
	/** Simula Keyword */ public final static int BEFORE       = 6;
	/** Simula Keyword */ public final static int BEGIN        = 7;
	/** Simula Keyword */ public final static int BOOLEAN      = 8;
	/** Simula Keyword */ public final static int CHARACTER    = 9;
	/** Simula Keyword */ public final static int CLASS        = 10;
	/** Simula Keyword */ public final static int COMMENT      = 11;
	/** Simula Keyword */ public final static int CONC         = 12;
	/** Simula Keyword */ public final static int DELAY        = 13;
	/** Simula Keyword */ public final static int DO           = 14;
	/** Simula Keyword */ public final static int ELSE         = 15;
	/** Simula Keyword */ public final static int END          = 16;
	/** Simula Keyword */ public final static int EQ           = 17;
	/** Simula Keyword */ public final static int EQV          = 18;
	/** Simula Keyword */ public final static int EXTERNAL     = 19;
	/** Simula Keyword */ public final static int FALSE        = 20;
	/** Simula Keyword */ public final static int FOR          = 21;
	/** Simula Keyword */ public final static int GE           = 22;
	/** Simula Keyword */ public final static int GO           = 23;
	/** Simula Keyword */ public final static int GOTO         = 24;
	/** Simula Keyword */ public final static int GT           = 25;
	/** Simula Keyword */ public final static int HIDDEN       = 26;
	/** Simula Keyword */ public final static int IF           = 27;
	/** Simula Keyword */ public final static int IMP          = 28;
	/** Simula Keyword */ public final static int IN           = 29;
	/** Simula Keyword */ public final static int INNER        = 30;
	/** Simula Keyword */ public final static int INSPECT      = 31;
	/** Simula Keyword */ public final static int INTEGER      = 32;
	/** Simula Keyword */ public final static int IS           = 33;
	/** Simula Keyword */ public final static int LABEL        = 34;
	/** Simula Keyword */ public final static int LE           = 35;
	/** Simula Keyword */ public final static int LONG         = 36;
	/** Simula Keyword */ public final static int LT           = 37;
	/** Simula Keyword */ public final static int NAME         = 38;
	/** Simula Keyword */ public final static int NE           = 39;
	/** Simula Keyword */ public final static int NEW          = 40;
	/** Simula Keyword */ public final static int NONE         = 41;
	/** Simula Keyword */ public final static int NOT          = 42;
	/** Simula Keyword */ public final static int NOTEXT       = 43;
	/** Simula Keyword */ public final static int OR           = 44;
	/** Simula Keyword */ public final static int OTHERWISE    = 45;
	/** Simula Keyword */ public final static int PRIOR        = 46;
	/** Simula Keyword */ public final static int PROCEDURE    = 47;
	/** Simula Keyword */ public final static int PROTECTED    = 48;
	/** Simula Keyword */ public final static int QUA          = 49;
	/** Simula Keyword */ public final static int REACTIVATE   = 50;
	/** Simula Keyword */ public final static int REAL         = 51;
	/** Simula Keyword */ public final static int REF          = 52;
	/** Simula Keyword */ public final static int SHORT        = 53;
	/** Simula Keyword */ public final static int STEP         = 54;
	/** Simula Keyword */ public final static int SWITCH       = 55;
	/** Simula Keyword */ public final static int TEXT         = 56;
	/** Simula Keyword */ public final static int THEN         = 57;
	/** Simula Keyword */ public final static int THIS         = 58;
	/** Simula Keyword */ public final static int TO           = 59;
	/** Simula Keyword */ public final static int TRUE         = 60;
	/** Simula Keyword */ public final static int UNTIL        = 61;
	/** Simula Keyword */ public final static int VALUE        = 62;
	/** Simula Keyword */ public final static int VIRTUAL      = 63;
	/** Simula Keyword */ public final static int WHEN         = 64;
	/** Simula Keyword */ public final static int WHILE        = 65;
	// Other Symbols
	/** Other Symbol */ public final static int ASSIGNVALUE    = 66;
	/** Other Symbol */ public final static int ASSIGNREF      = 67;
	/** Other Symbol */ public final static int COMMA          = 68;
	/** Other Symbol */ public final static int COLON          = 69;
	/** Other Symbol */ public final static int SEMICOLON      = 70;
	/** Other Symbol */ public final static int BEGPAR         = 71;
	/** Other Symbol */ public final static int ENDPAR         = 72;
	/** Other Symbol */ public final static int BEGBRACKET     = 73;
	/** Other Symbol */ public final static int ENDBRACKET     = 74;
	/** Other Symbol */ public final static int EQR            = 75;
	/** Other Symbol */ public final static int NER            = 76;
	/** Other Symbol */ public final static int PLUS           = 77;
	/** Other Symbol */ public final static int MINUS          = 78;
	/** Other Symbol */ public final static int MUL            = 79;
	/** Other Symbol */ public final static int DIV            = 80;
	/** Other Symbol */ public final static int INTDIV         = 81;
	/** Other Symbol */ public final static int EXP            = 82;
	/** Other Symbol */ public final static int IDENTIFIER     = 83;
	/** Other Symbol */ public final static int BOOLEANKONST   = 84;
	/** Other Symbol */ public final static int INTEGERKONST   = 85;
	/** Other Symbol */ public final static int CHARACTERKONST = 86;
	/** Other Symbol */ public final static int REALKONST      = 87;
	/** Other Symbol */ public final static int TEXTKONST      = 88;
	/** Other Symbol */ public final static int OR_ELSE        = 89;
	/** Other Symbol */ public final static int AND_THEN       = 90;
	/** Other Symbol */ public final static int DOT            = 91;
	/** Other Symbol */ public final static int NEWLINE        = 92;
	/** Other Symbol */ public final static int STRING         = 93;

	/// Returns the corresponding String.
	/// @param key the argument key.
	/// @return the corresponding String.
	public static String edit(int key) {
		switch (key) {
		case ACTIVATE   : return("ACTIVATE");
		case AND        : return("AND");
		case AFTER      : return("AFTER");
		case ARRAY      : return("ARRAY");
		case AT         : return("at");
		case BEFORE     : return("BEFORE");
		case BEGIN      : return("BEGIN");
		case BOOLEAN    : return("BOOLEAN");
		case CHARACTER  : return("CHARACTER");
		case CLASS      : return("CLASS");
		case COMMENT    : return("COMMENT");
		case CONC       : return("CONC");
		case DELAY      : return("DELAY");
		case DO         : return("DO");
		case ELSE       : return("ELSE");
		case END        : return("END");
		case EQ         : return("EQ");
		case EQV        : return("EQV");
		case EXTERNAL   : return("EXTERNAL");
		case FALSE      : return("FALSE");
		case FOR        : return("FOR");
		case GE         : return("GE");
		case GO         : return("GO");
		case GOTO       : return("GOTO");
		case GT         : return("GT");
		case HIDDEN     : return("HIDDEN");
		case IF         : return("IF");
		case IMP        : return("IMP");
		case IN         : return("IN");
		case INNER      : return("INNER");
		case INSPECT    : return("INSPECT");
		case INTEGER    : return("INTEGER");
		case IS         : return("IS");
		case LABEL      : return("LABEL");
		case LE         : return("LE");
		case LONG       : return("LONG");
		case LT         : return("LT");
		case NAME       : return("NAME");
		case NE         : return("NE");
		case NEW        : return("NEW");
		case NONE       : return("NONE");
		case NOT        : return("NOT");
		case NOTEXT     : return("NOTEXT");
		case OR         : return("OR");
		case OTHERWISE  : return("OTHERWISE");
		case PRIOR      : return("PRIOR");
		case PROCEDURE  : return("PROCEDURE");
		case PROTECTED  : return("PROTECTED");
		case QUA        : return("QUA");
		case REACTIVATE : return("REACTIVATE");
		case REAL       : return("REAL");
		case REF        : return("REF");
		case SHORT      : return("SHORT");
		case STEP       : return("STEP");
		case SWITCH     : return("SWITCH");
		case TEXT       : return("TEXT");
		case THEN       : return("THEN");
		case THIS       : return("THIS");
		case TO         : return("TO");
		case TRUE       : return("TRUE");
		case UNTIL      : return("UNTIL");
		case VALUE      : return("VALUE");
		case VIRTUAL    : return("VIRTUAL");
		case WHEN       : return("WHEN");
		case WHILE      : return("WHILE");
		// Other Symbols
		case ASSIGNVALUE:    return("ASSIGNVALUE");
		case ASSIGNREF:      return("ASSIGNREF");
		case COMMA:          return("COMMA");
		case COLON:          return("COLON");
		case SEMICOLON:      return("SEMICOLON");
		case BEGPAR:         return("BEGPAR");
		case ENDPAR:         return("ENDPAR");
		case BEGBRACKET:     return("BEGBRACKET");
		case ENDBRACKET:     return("ENDBRACKET");
		case EQR:            return("EQR");
		case NER:            return("NER");
		case PLUS:           return("PLUS");
		case MINUS:          return("MINUS");
		case MUL:            return("MUL");
		case DIV:            return("DIV");
		case INTDIV:         return("INTDIV");
		case EXP:            return("EXP");
		case IDENTIFIER:     return("IDENTIFIER");
		case BOOLEANKONST:   return("BOOLEANKONST");
		case INTEGERKONST:   return("INTEGERKONST");
		case CHARACTERKONST: return("CHARACTERKONST");
		case REALKONST:      return("REALKONST");
		case TEXTKONST:      return("TEXTKONST");
		case OR_ELSE:        return("OR_ELSE");
		case AND_THEN:       return("AND_THEN");
		case DOT:            return("DOT");
		case NEWLINE:        return("NEWLINE");
		case STRING:         return("STRING");
		}
		return("Unknown:"+key);
	}
	
	/// Returns the corresponding Java code.
	/// @param key the argument key.
	/// @return the corresponding Java code
	public static String toJavaCode(int key) {
		switch (key) {
		case NONE: return("null");
		case NOTEXT: return("null");
		case ASSIGNVALUE: return(":=");
		case ASSIGNREF: return(":-");
		case EQ: return ("==");
		case GE: return (">=");
		case GT: return (">");
		case LE: return ("<=");
		case LT: return ("<");
		case NE: return ("!=");
		case EQR: return ("==");
		case NER: return ("!=");
		case IN: return (" instanceof ");
		case PLUS: return ("+");
		case MINUS: return ("-");
		case MUL: return ("*");
		case DIV: return ("/");
		case INTDIV: return ("/");  
		case EXP: return ("^"); 
		case NOT: return ("!");
		case AND: return ("&");
		case OR: return ("|");
		case IMP: return (" imp "); //  x -> y   ==>   !x | y
		case EQV: return (" == ");
		case AND_THEN: return ("&&");
		case OR_ELSE: return ("||");
		case DOT: return (".");
		default: return (edit(key));
		}
 }

}
