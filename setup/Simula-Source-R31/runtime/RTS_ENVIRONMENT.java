/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.runtime;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/// System class ENVIRONMENT.
/// 
/// The purpose of the environmental class is to encapsulate all constants,
/// procedures and classes which are accessible to all source modules. It
/// contains procedures for mathematical functions, text generation, random
/// drawing, etc.
/// 
/// Link to GitHub: <a href="https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/runtime/RTS_ENVIRONMENT.java"><b>Source File</b></a>.
/// 
/// @author SIMULA Standards Group
/// @author Øystein Myhre Andersen
public class RTS_ENVIRONMENT extends RTS_RTObject {
	/// The Simula release identification.
	/// 
	/// NOTE: When updating release id, change version in setup.SimulaExtractor and simula.Global
	static final String simulaReleaseID = "Simula-2.0";

	/// The start time from System.currentTimeMillis
	static long _STARTTIME = System.currentTimeMillis();
	
	/// The current lowten character.
	static char CURRENTLOWTEN = '&';
	
	/// The current decimal mark character.
	static char CURRENTDECIMALMARK = '.';
	
	/// A constant holding the maximum value a long real can have.
	public final static double maxlongreal = Double.MAX_VALUE;

	/// A constant holding the minimum value a long real can have.
	public final static double minlongreal = -maxlongreal;// Double.MIN_VALUE;
	
	/// A constant holding the maximum value a real can have.
	public final static float maxreal = Float.MAX_VALUE;
	
	/// A constant holding the minimum value a real can have.
	public final static float minreal = -maxreal;// Float.MIN_VALUE;
	
	/// A constant holding the maximum value an integer can have.
	public final static int maxint = Integer.MAX_VALUE;
	
	/// A constant holding the minimum value an integer can have.
	public final static int minint = Integer.MIN_VALUE;

	/// Normal Constructor
	/// @param staticLink static link
	public RTS_ENVIRONMENT(final RTS_RTObject staticLink) {
		super(staticLink);
	}

	/// Simula identification String.
	/// 
	/// The value of "simulaid" is an implementation defined string of the following
	/// general format:
	/// 
	/// <pre>
	///     <simid>!!!<siteid>!!!<OS>!!!<CPU>!!!<user>!!!<job>!!!<acc>!!!<prog>
	/// 
	///         <simid>:   Identification of the SIMULA system (name, version etc.)
	///         <siteid>:  Identification of the installation (e.g. organisation name)
	///         <OS>:      Operating system identification (name, version, etc.)
	///         <CPU>:     Host system identification (manufacturer, name, number, etc.)
	///         <user>:    User identification
	///         <job>:     Job identification (session number)
	///         <acc>:     Account identification
	///         <prog>:    Identification of the executing task or program
	/// </pre>
	/// 
	/// @return Simula identification String.
	public static RTS_TXT simulaid() {
		String simid = "Portable " + simulaReleaseID;
		String OS = System.getProperty("os.name");
		String CPU = System.getProperty("os.arch");
		String user = System.getProperty("user.name");
		user=new String(user.getBytes(), StandardCharsets.US_ASCII);
		String job = RTS_UTIL.progamIdent;
		String acc = user;
		String prog = RTS_UTIL.progamIdent;
		String siteid=OS+'.'+user;
		
		String simulaIdent = simid + "!!!" + siteid + "!!!" + OS + "!!!" + CPU + "!!!" + user + "!!!" + job + "!!!" + acc + "!!!" + prog;
		return (new RTS_TXT(simulaIdent));
	}

	///// ****************************************
	///// ** Basic operations/// **
	///// ****************************************

	/// Standard Procedure mod.
	/// <pre>
	/// integer procedure mod(i,j);   integer i,j;
	/// begin integer res;
	///    res := i - (i//j)*j;
	///    mod := if res = 0 then 0
	///      else if sign(res) ne sign(j) then res+j
	///      else res
	/// end mod;
	/// </pre>
	/// 
	/// The result is the mathematical modulo value of the parameters.
	/// 
	/// @param i argument i
	/// @param j argument j
	/// @return the resulting mod
	public static int mod(final int i, final int j) {
		int res = i % j;
		if (res == 0)
			return (0);
		if (res *  j < 0)
			return (res + j);
		return (res);
	}

	/// Standard Procedure rem.
	/// <pre>
	/// integer procedure rem(i,j); integer i,j;
	///                   rem := i - (i//j)*j;
	/// </pre>
	/// 
	/// The result is the remainder of an integer division.
	/// 
	/// 
	/// @param i argument i
	/// @param j argument j
	/// @return the resulting rem
	public static int rem(final int i, final int j) {
		return (i % j);
	}

	/// Standard Procedure abs.
	/// <pre>
	/// <type of e> procedure abs(e); <type> e;
	///       abs := if e >= 0 then e else -e;
	/// </pre>
	/// 
	/// The result is the absolute value of the parameter.
	/// @param e the argument
	/// @return the resulting abs value
	public static int abs(final int e) {
		return (Math.abs(e));
	}

	/// Standard Procedure abs.
	/// See {@link RTS_ENVIRONMENT#abs(int)}
	/// @param e the argument
	/// @return the resulting abs value
	public static float abs(final float e) {
		return (Math.abs(e));
	}

	/// Standard Procedure abs.
	/// See {@link RTS_ENVIRONMENT#abs(int)}
	/// @param e the argument
	/// @return the resulting abs value
	public static double abs(final double e) {
		return (Math.abs(e));
	}
	
	/// Standard Procedure sign.
	/// @param e the argument e
	/// @return resulting sign code
	public static int sign(final double e) {
		return ((e > 0) ? (1) : ((e < 0) ? -1 : 0));
	}

	/// Standard Procedure entier.
	/// <pre>
	/// integer procedure entier(r); <real-type> r;
	/// begin integer j;
	///       j := r;             ! implied conversion of "r" to integer ;
	///       entier:= if j > r   ! implied conversion of "j" to real ;
	///                then j-1 else j
	/// end entier;
	/// </pre>
	/// 
	/// The result is the integer "floor" of a real type item, the value always being
	/// less than or equal to the parameter. Thus, entier(1.8) returns the value 1,
	/// while entier(-1.8) returns -2.
	/// 
	/// @param d argument d
	/// @return the resulting entier
	public static int entier(final double d) {
		int j = (int) d;
		return ((((float) j) > d) ? (j - 1) : (j));
	}

	/// Standard Procedure addepsilon.
	/// 
	/// <pre>
	/// <type of e> procedure addepsilon(e);   <real-type> e;
	///     addepsilon := e + ... ; ! see below;
	/// </pre>
	/// 
	/// The result type is that of the parameter. The result is the value of the
	/// parameter incremented by the
	/// smallest positive value, such that the result is not equal to the parameter
	/// within the precision of the implementation.
	/// 
	/// Thus, for all positive values of "eps",
	/// <pre>
	///       E - eps <= subepsilon(E) < E < addepsilon(E) <= E + eps
	/// </pre>
	/// 
	/// @param x float argument
	/// @return the argument incremented by the smallest possible value
	public static float addepsilon(final float x) {
		return (Math.nextUp(x));
	}

	/// Standard Procedure addepsilon.
	/// See {@link RTS_ENVIRONMENT#addepsilon(float)}
	/// @param x double argument
	/// @return the argument incremented by the smallest possible value
	public static double addepsilon(final double x) {
		return (Math.nextUp(x));
	}

	/// Standard Procedure subepsilon.
	/// 
	/// <pre>
	/// <type of e> procedure subepsilon(e);   <real-type> e;
	///     subepsilon := e - ... ; ! see below;
	/// </pre>
	/// 
	/// The result type is that of the parameter. The result is the value of the
	/// parameter decremented by the
	/// smallest positive value, such that the result is not equal to the parameter
	/// within the precision of the implementation.
	/// 
	/// Thus, for all positive values of "eps",
	/// <pre>
	///       E - eps <= subepsilon(E) < E < addepsilon(E) <= E + eps
	/// </pre>
	/// 
	/// @param x float argument
	/// @return the argument decremented by the smallest possible value
	public static float subepsilon(final float x) {
		return (Math.nextDown(x));
	}

	/// Standard Procedure subepsilon.
	/// See {@link RTS_ENVIRONMENT#subepsilon(float)}
	/// @param x double argument
	/// @return the argument decremented by the smallest possible value
	public static double subepsilon(final double x) {
		return (Math.nextDown(x));
	}

	/// Standard Procedure char.
	/// <pre>
	/// character procedure char(i);  integer i;
	///    char := ... ;
	/// </pre>
	/// 
	/// The result is the character obtained by converting the parameter according to
	/// the implementation-defined coding of characters. The parameter must be in the
	/// range 0..maxrank.
	/// 
	/// @param i an integer agrgument
	/// @return the casted value: (char) i
	public static char Char(final int i) {
		return ((char) i);
	}

	/// Standard Procedure char.
	/// See {@link RTS_ENVIRONMENT#Char(int)}
	/// 
	/// @param i an integer agrgument
	/// @return the casted value: (char) i
	public static char _char(final int i) {
		return ((char) i);
	}

	/// Standard Procedure isochar.
	/// <pre>
	/// character procedure isochar(i);  integer i;
	///    isochar := ... ;
	/// </pre>
	/// 
	/// The result is the character obtained by converting the parameter according to
	/// the ISO 2022 standard character code. The parameter must be in the range
	/// 0..255.
	/// 
	/// @param i an integer agrgument
	/// @return the casted value: (char) i
	public static char isochar(final int i) {
		return ((char) i);
	}

	/// Standard Procedure rank.
	/// <pre>
	/// integer procedure rank(c);  character c;
	///    rank := ... ;
	/// </pre>
	/// 
	/// The result is the integer obtained by converting the parameter according to
	/// the implementation-defined character code.
	/// @param c the argument
	/// @return resulting rank
	public static int rank(final char c) {
		return ((int) c);
	}

	/// Standard Procedure isorank.
	/// <pre>
	/// integer procedure isorank(c);  character c;
	///    isorank := ... ;
	/// </pre>
	/// 
	/// The result is the integer obtained by converting the parameter according to
	/// the ISO 2022 standard character code.
	/// @param c the argument
	/// @return resulting isorank
	public static int isorank(final char c) {
		return ((int) c);
	}

	/// Standard Procedure digit.
	/// <pre>
	/// Boolean procedure digit(c);  character c;
	///    digit := ... ;
	/// </pre>
	/// 
	/// The result is true if the parameter is a decimal digit.
	/// @param c the argument
	/// @return true: c is a digit
	public static boolean digit(final char c) {
		return (Character.isDigit(c));
	}

	/// Standard Procedure letter.
	/// <pre>
	/// Boolean procedure letter(c);  character c;
	///    letter := ... ;
	/// </pre>
	/// 
	/// The result is true if the parameter is a letter of the English alphabet ('a' ... 'z', 'A' ... 'Z').
	/// @param c the argument
	/// @return true: c is a letter
	public static boolean letter(final char c) {
		return (Character.isLetter(c));
	}

	/// Standard Procedure lowten.
	/// <pre>
	/// character procedure lowten(c);  character c;
	///                   if ... ! c is illegal as lowten;
	///                   then  error("..." ! Lowten error ;)
	///                   else begin
	///                      lowten:= CURRENTLOWTEN; CURRENTLOWTEN:= c
	///                    end lowten;
	/// </pre>
	/// 
	/// Changes the value of the current lowten character to that of the parameter.
	/// The previous value is returned. Illegal parameters are
	/// 
	/// digits, plus ("+"), minus ("-"), dot ("."), comma (","), control characters
	/// (i.e. ISO code<32), DEL (ISO code 127), and all characters with ISO code
	/// greater than 127.
	/// @param c the new lowten character
	/// @return the previous lowten character
	public static char lowten(final char c) {
		if (illegalLowten(c))
			throw new RTS_SimulaRuntimeError("Illegal LOWTEN Character: " + c + "  Code=" + (int) c);
		char lowten = CURRENTLOWTEN;
		CURRENTLOWTEN = Character.toUpperCase(c);
		return (lowten);
	}

	/// Check if the given character is illegal as lowten.
	/// @param c the given character
	/// @return true if the given character is illegal as lowten.
	private static boolean illegalLowten(final char c) {
		if (c <= 32)
			return (true); // SPACE is also Illegal in this implementation
		if (c >= 127)
			return (true);
		switch (c) {
			case '0', '1', '2', '3', '4', '5', '6','7','8','9', '+', '-', '.', ',':	return (true);
		}
		return (false);
	}

	/// Standard Procedure decimalmark.
	/// <pre>
	/// character procedure decimalmark(c);   character c;
	///    if c ne '.' and then c ne ','
	///    then error("..." ! Decimalmark error ;)
	///    else begin
	///            decimalmark:= CURRENTDECIMALMARK;
	///            CURRENTDECIMALMARK:= c
	/// end decimalmark;
	/// </pre>
	/// 
	/// Changes the value of the decimal point character used by the text (de)editing
	/// procedures (cf. 8.7 and 8.8). See the _TXT methods: getreal, getfrac, putfix,
	/// putreal and putfrac. The previous value is returned. The only legal parameter
	/// values are dot and comma.
	/// @param c the new decimalmark character
	/// @return the previous decimalmark character
	public static char decimalmark(final char c) {
		char decimalmark = 0;
		if (c != '.' && c != ',') {
			throw new RTS_SimulaRuntimeError("Decimalmark error: " + c);
		} else {
			decimalmark = CURRENTDECIMALMARK;
			CURRENTDECIMALMARK = c;
		}
		return (decimalmark);
	}

	/// Standard Procedure copy.
	/// <pre>
	/// text procedure copy(T); text T;
	///            if T =/= notext
	///            then begin text U;
	///               U.OBJ    :- new TEXTOBJ(T.LENGTH,false);
	///               U.START  := 1;
	///               U.LENGTH := T.LENGTH;
	///               U.POS    := 1;
	///               U        := T;
	///               copy     :- U
	///            end copy;
	/// </pre>
	/// 
	/// "copy(T)", with T =/= notext, references a new alterable main frame which
	/// contains a text value identical to that of T.
	/// @param T the text object to be copied
	/// @return a copy of T
	public static RTS_TXT copy(final RTS_TXT T) {
		if (T == null)
			return (null);
		RTS_TXT U = blanks(T.LENGTH);
		RTS_UTIL._ASGTXT(U, T);
		return (U);
	}

	/// Standard Procedure blanks.
	/// <pre>
	/// text procedure blanks(n); integer n;
	///            if        n < 0 then error("..." ! Parm. to blanks < 0;)
	///            else if   n > 0
	///            then begin text T;
	///               T.OBJ    :- new TEXTOBJ(n,false);
	///               T.START  := 1;
	///               T.LENGTH := n;
	///               T.POS    := 1;
	///               T        := notext;    ! blank-fill, see 4.1.2;
	///               blanks   :- T
	///            end blanks;
	/// </pre>
	/// 
	/// "blanks(n)", with n > 0, references a new alterable main frame of length n,
	/// containing only blank characters. "blanks(0)" references notext.
	/// @param n the number of space characters
	/// @return a text object conraining n space characters
	public static RTS_TXT blanks(final int n) {
		if (n < 0)
			throw new RTS_SimulaRuntimeError("Parmameter to blanks < 0");
		if (n == 0)
			return (RTS_UTIL.NOTEXT);
		RTS_TXT textRef = new RTS_TXT();
		RTS_TEXTOBJ textObj = new RTS_TEXTOBJ(n, false);
		textObj.fill(' ');
		textRef.START = 0; // Note: Counting from zero in this implementation
		textRef.LENGTH = n;
		textRef.POS = 0; // Note: Counting from zero in this implementation
		textRef.OBJ = textObj;
		return (textRef);
	}

	/// Standard Procedure uppercase.
	/// <pre>
	/// text procedure upcase(t);   text t;
	///    begin  t.setpos(1); upcase:- t;  ... end;
	/// </pre>
	/// 
	/// Convert the letters in the text parameter to their upper case representation.
	/// Only letters of the English alphabet are converted.
	/// 
	/// The result is a reference to the parameter.
	/// @param t the argument text
	/// @return same text with upper case letters
	public static RTS_TXT upcase(RTS_TXT t) {
		if (t == null)
			t = RTS_UTIL.NOTEXT;
		String s = t.edText().toUpperCase();
		return (RTS_UTIL._ASGTXT(t, new RTS_TXT(s)));
	}

	/// Standard Procedure lowcase.
	/// <pre>
	/// text procedure lowcase(t); text t;
	///                begin  t.setpos(1); lowcase:- t; ... end;
	/// </pre>
	/// 
	/// Convert the letters in the text parameter to their lower case representation.
	/// Only letters of the English alphabet are converted.
	/// 
	/// The result is a reference to the parameter.
	/// @param t the argument text
	/// @return same text with lower case letters
	public static RTS_TXT lowcase(RTS_TXT t) {
		if (t == null)
			t = RTS_UTIL.NOTEXT;
		String s = t.edText().toLowerCase();
		return (RTS_UTIL._ASGTXT(t, new RTS_TXT(s)));
	}

	///// ****************************************
	///// ** Mathematical functions/// **
	///// ****************************************

	/// Standard Procedure sqrt.
	/// @param r the parameter r
	/// @return Math.sqrt(r)
	public static double sqrt(final double r) {
		return (Math.sqrt(r));
	}

	/// Standard Procedure sin.
	/// @param r the parameter r
	/// @return Math.sin(r)
	public static double sin(final double r) {
		return (Math.sin(r));
	}

	/// Standard Procedure cos.
	/// @param r the parameter r
	/// @return Math.cos(r)
	public static double cos(final double r) {
		return (Math.cos(r));
	}

	/// Standard Procedure tan.
	/// @param r the parameter r
	/// @return Math.tan(r)
	public static double tan(final double r) {
		return (Math.tan(r));
	}

	/// Standard Procedure cotan.
	/// @param r the parameter r
	/// @return 1.0 / Math.tan(r)
	public static double cotan(final double r) {
		return (1.0 / Math.tan(r));
	}

	/// Standard Procedure arcsin.
	/// @param r the parameter r
	/// @return Math.asin(r)
	public static double arcsin(final double r) {
		return (Math.asin(r));
	}

	/// Standard Procedure arccos.
	/// @param r the parameter r
	/// @return Math.acos(r)
	public static double arccos(final double r) {
		return (Math.acos(r));
	}

	/// Standard Procedure arctan.
	/// @param r the parameter r
	/// @return Math.atan(r)
	public static double arctan(final double r) {
		return (Math.atan(r));
	}


	/// Standard Procedure arctan2.
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return Math.atan2(x,y)

	public static double arctan2(final double y, final double x) {
		return (Math.atan2(y, x));
	}


	/// Standard Procedure sinh.
	/// @param r the parameter r
	/// @return Math.sinh(r)

	public static double sinh(final double r) {
		return (Math.sinh(r));
	}


	/// Standard Procedure cosh.
	/// @param r the parameter r
	/// @return Math.cosh(r)

	public static double cosh(final double r) {
		return (Math.cosh(r));
	}


	/// Standard Procedure tanh.
	/// @param r the parameter r
	/// @return Math.tanh(r)

	public static double tanh(final double r) {
		return (Math.tanh(r));
	}


	/// Standard Procedure ln.
	/// @param r the parameter r
	/// @return Math.log(r)

	public static double ln(final double r) {
		return (Math.log(r));
	}


	/// Standard Procedure log10.
	/// @param r the parameter r
	/// @return Math.log10(r)

	public static double log10(final double r) {
		return (Math.log10(r));
	}


	/// Standard Procedure exp.
	/// @param r the parameter r
	/// @return Math.exp(r)

	public static double exp(final double r) {
		return (Math.exp(r));
	}

	///// ****************************************
	///// ** Extremum functions/// **
	///// ****************************************


	/// Standard Procedure max.
	/// <pre>
	///  <type> procedure max(i1,i2); <type> i1; <type> i2;
	/// </pre>
	/// 
	/// The value is the greater of the two parameter values. Legal parameter types
	/// are text, character, real type and integer type.
	/// 
	/// The type of the result conforms to the rules of 3.3.1. in Simula Standard.
	/// 
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static double max(final double x, final double y) {
		return (Math.max(x, y));
	}


	/// Standard Procedure max.
	/// See {@link RTS_ENVIRONMENT#max(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static double max(final double x, final float y) {
		return (Math.max(x, y));
	}


	/// Standard Procedure max.
	/// See {@link RTS_ENVIRONMENT#max(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static double max(final double x, final int y) {
		return (Math.max(x, y));
	}


	/// Standard Procedure max.
	/// See {@link RTS_ENVIRONMENT#max(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static float max(final float x, final float y) {
		return (Math.max(x, y));
	}


	/// Standard Procedure max.
	/// See {@link RTS_ENVIRONMENT#max(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static float max(final float x, final int y) {
		return (Math.max(x, y));
	}


	/// Standard Procedure max.
	/// See {@link RTS_ENVIRONMENT#max(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static double max(final float x, final double y) {
		return (Math.max(x, y));
	}


	/// Standard Procedure max.
	/// See {@link RTS_ENVIRONMENT#max(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static int max(final int x, final int y) {
		return (Math.max(x, y));
	}


	/// Standard Procedure max.
	/// See {@link RTS_ENVIRONMENT#max(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static float max(final int x, final float y) {
		return (Math.max(x, y));
	}


	/// Standard Procedure max.
	/// See {@link RTS_ENVIRONMENT#max(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static double max(final int x, final double y) {
		return (Math.max(x, y));
	}


	/// Standard Procedure max.
	/// See {@link RTS_ENVIRONMENT#max(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static char max(final char x, final char y) {
		return ((char) Math.max((int) x, (int) y));
	}


	/// Standard Procedure max.
	/// See {@link RTS_ENVIRONMENT#max(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the greater of the two parameter values

	public static RTS_TXT max(final RTS_TXT x, final RTS_TXT y) {
		return (RTS_UTIL._TXTREL_LT(x, y) ? y : x);
	}


	/// Standard Procedure min.
	/// <pre>
	/// <type> procedure min(i1,i2); <type> i1;  <type> i2;
	/// </pre>
	/// 
	/// The value is the lesser of the two parameter values. Legal parameter types
	/// are text, character, real type and integer type.
	/// 
	/// The type of the result conforms to the rules of 3.3.1. in Simula Standard.
	/// 
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static double min(final double x, final double y) {
		return (Math.min(x, y));
	}


	/// Standard Procedure min.
	/// See {@link RTS_ENVIRONMENT#min(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static double min(final double x, final float y) {
		return (Math.min(x, y));
	}


	/// Standard Procedure min.
	/// See {@link RTS_ENVIRONMENT#min(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static double min(final double x, final int y) {
		return (Math.min(x, y));
	}


	/// Standard Procedure min.
	/// See {@link RTS_ENVIRONMENT#min(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static float min(final float x, final float y) {
		return (Math.min(x, y));
	}


	/// Standard Procedure min.
	/// See {@link RTS_ENVIRONMENT#min(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static double min(final float x, final double y) {
		return (Math.min(x, y));
	}


	/// Standard Procedure min.
	/// See {@link RTS_ENVIRONMENT#min(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static float min(final float x, final int y) {
		return (Math.min(x, y));
	}


	/// Standard Procedure min.
	/// See {@link RTS_ENVIRONMENT#min(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static int min(final int x, final int y) {
		return (Math.min(x, y));
	}


	/// Standard Procedure min.
	/// See {@link RTS_ENVIRONMENT#min(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static float min(final int x, final float y) {
		return (Math.min(x, y));
	}


	/// Standard Procedure min.
	/// See {@link RTS_ENVIRONMENT#min(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static double min(final int x, final double y) {
		return (Math.min(x, y));
	}


	/// Standard Procedure min.
	/// See {@link RTS_ENVIRONMENT#min(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static char min(final char x, final char y) {
		return ((char) Math.min((int) x, (int) y));
	}


	/// Standard Procedure min.
	/// See {@link RTS_ENVIRONMENT#min(double,double)}
	/// @param x the parameter x
	/// @param y the parameter y
	/// @return the lesser of the two parameter values

	public static RTS_TXT min(final RTS_TXT x, final RTS_TXT y) {
		return (RTS_UTIL._TXTREL_LT(x, y) ? x : y);
	}

	///// ****************************************
	///// ** Error control/// **
	///// ****************************************


	/// Standard Procedure error.
	/// <pre>
	/// procedure error(t);   text t;
	///    begin ... display text "t" and stop program...
	///    end error;
	/// </pre>
	/// 
	/// The procedure "error" stops the execution of the program as if a runtime
	/// error has occurred and presents the contents of the text parameter on the
	/// diagnostic channel (normally the controlling terminal).
	/// 
	/// @param msg error message text

	public static void error(final RTS_TXT msg) {
		throw new RTS_SimulaRuntimeError(msg.edText());
	}

	///// ****************************************
	///// ** Array quantities/// **
	///// ****************************************


	/// Standard Procedure lowerbound.
	/// <pre>
	///  integer procedure lowerbound(a,i);
	///          <type> array a; integer i;
	/// </pre>
	/// 
	/// The procedure "lowerbound" returns the lower bound of the dimension of the
	/// given array corresponding to the given index. The first dimension has index
	/// one, the next two, etc. An index less than one or greater than the number of
	/// dimensions of the given array constitutes a run time error.
	/// 
	/// @param a _ARRAY reference
	/// @param i bounds index
	/// @return the lower bound

	public static int lowerbound(final RTS_ARRAY a, final int i) {
		try {
			return (a.lowerBound(i - 1));
		} catch (RTS_SimulaRuntimeError e) {
			throw new RTS_SimulaRuntimeError("Wrong number of dimensions", e);
		}
	}


	/// Standard Procedure upperbound.
	/// <pre>
	///  integer procedure upperbound(a,i);
	///          <type> array a; integer i;
	/// </pre>
	/// 
	/// The procedure "upperbound" returns the upper bound of the dimension of the
	/// given array corresponding to the given index. The first dimension has index
	/// one, the next two, etc. An index less than one or greater than the number of
	/// dimensions of the given array constitutes a run time error.
	/// 
	/// @param a _ARRAY reference
	/// @param i bounds index
	/// @return the upper bound

	public static int upperbound(final RTS_ARRAY a, final int i) {
		try {
			return (a.upperBound(i - 1));
		} catch (RTS_SimulaRuntimeError e) {
			throw new RTS_SimulaRuntimeError("Wrong number of dimensions", e);
		}
	}

	///// ********************************************************************
	///// ** Random drawing/// **
	///// ********************************************************************

	///// *********************************************************************
	///// ** Random drawing: Procedure draw
	///// *********************************************************************

	/// Random drawing: Procedure draw.
	/// <pre>
	///  Boolean procedure draw (a,U);
	///                name U; long real a; integer U;
	/// 
	/// The value is true with the probability a, false with the probability 1 - a.
	/// It is always true if a >= 1 and always false if a <= 0.
	/// </pre>
	/// 
	/// @param a The long real parameter a
	/// @param U The pseudo random number (seed) by name.
	/// @return Returns the next pseudorandom with the probability a

	public static boolean draw(final double a, final RTS_NAME<Integer> U) {
		boolean res;
		if (a >= 1.0)
			res = true;
		else if (a <= 0.0)
			res = false;
		else
			res = a >= RTS_RandomDrawing.basicDRAW(U);
		return (res);
	}

	///// *********************************************************************
	///// ** Random drawing: Procedure randint
	///// *********************************************************************

	/// Random drawing: randint distribution.
	/// <pre>
	///  integer procedure randint (a,b,U);
	/// 		          name U; integer a,b,U;
	/// </pre>
	/// 
	/// The value is one of the integers a, a+1, ..., b-1, b with equal probability.
	/// If b < a, the call constitutes an error.
	/// 
	/// @param a The integer parameter a
	/// @param b The integer parameter b
	/// @param U The pseudo random number (seed) by name.
	/// @return Returns the next pseudorandom, according to the randint distribution

	public static int randint(final int a, final int b, final RTS_NAME<Integer> U) {
		if (b < a)
			throw new RTS_SimulaRuntimeError("Randint(a,b,u):  b < a");
		return (entier(RTS_RandomDrawing.basicDRAW(U) *  ((b - a + 1))) + a);
	}

	///// *********************************************************************
	///// ** Random drawing: Procedure uniform
	///// *********************************************************************

	/// Random drawing: uniform distribution.
	/// <pre>
	/// long real procedure uniform (a,b,U);
	///          name U; long real a,b; integer U;
	/// </pre>
	/// 
	/// The value is uniformly distributed in the interval a <= u < b. If b < a, the
	/// call constitutes an error.
	/// 
	/// @param a The long real parameter a
	/// @param b The long real parameter b
	/// @param U The pseudo random number (seed) by name.
	/// @return Returns the next pseudorandom, according to the uniform distribution

	public static double uniform(final double a, final double b, final RTS_NAME<Integer> U) {
		if (b < a)
			throw new RTS_SimulaRuntimeError("Uniform(a,b,u): b < a");
		return (a + ((b - a)  *  RTS_RandomDrawing.basicDRAW(U)));
	}

	///// *********************************************************************
	///// ** Random drawing: Procedure normal
	///// *********************************************************************

	/// Random drawing: normal distribution.
	/// <pre>
	/// long real procedure normal (a,b,U);
	///         name U; long real a,b; integer U;
	/// </pre>
	/// 
	/// The value is normally distributed with mean a and standard deviation b. An
	/// approximation formula may be used for the normal distribution function.
	/// 
	/// @param a The long real parameter a
	/// @param b The long real parameter b
	/// @param U The pseudo random number (seed) by name.
	/// @return Returns the next pseudorandom, according to the negexp distribution

	public static double normal(final double a, final double b, final RTS_NAME<Integer> U) {
		double t, p, q, v, x;
		boolean z;
		if (b < 0.0)
			throw new RTS_SimulaRuntimeError("Normal(a,b,u):  b <= 0.");
		v = RTS_RandomDrawing.basicDRAW(U);
		if (v > 0.5) {
			z = true;
			v = 1.0f - v;
		} else
			z = false;
		t = Math.log(v); // log is natural logarithm (base e) in Java
		t = Math.sqrt(-t - t);
		p = 2.515517f + (t *  (0.802853f + (t *  0.010328f)));
		q = 1.0f + (t *  (1.432788f + (t *  (0.189269f + (t *  0.001308f)))));
		x = b *  (t - (p / q));
		double res = a + ((z) ? x : -x);
		return (res);
	}

	///// *********************************************************************
	///// ** Random drawing: Procedure negexp
	///// *********************************************************************

	/// Random drawing: negexp distribution.
	/// <pre>
	/// long real procedure negexp (a,U);
	///          name U; long real a; integer U;
	/// </pre>
	/// 
	/// The value is a drawing from the negative exponential distribution with mean
	/// 1/a, defined by -ln(u)/a, where u is a basic drawing. This is the same as a
	/// random "waiting time" in a Poisson distributed arrival pattern with expected
	/// number of arrivals per time unit equal to a.
	/// 
	/// If a is non-positive, a runtime error occurs.
	/// 
	/// @param a The long real parameter a
	/// @param U The pseudo random number (seed) by name.
	/// @return Returns the next pseudorandom, according to the negexp distribution

	public static double negexp(final double a, final RTS_NAME<Integer> U) {
		if (a <= 0.0)
			throw new RTS_SimulaRuntimeError("Negexp(a,u): a <= 0");
		return (-Math.log(RTS_RandomDrawing.basicDRAW(U)) / a);
	}

	///// *********************************************************************
	///// ** Random drawing: Procedure Poisson
	///// *********************************************************************

	/// Random drawing: Poisson distribution.
	/// <pre>
	/// integer procedure Poisson (a,U);
	///          name U; long real a; integer U;
	/// </pre>
	/// 
	/// The value is a drawing from the Poisson distribution with parameter a. It is
	/// obtained by n+1 basic drawings, u(i), where n is the function value. n is
	/// defined as the smallest non-negative integer for which
	/// 
	/// u(0)/// u(1)/// .../// u(n) < e**(-a)
	/// 
	/// The validity of the formula follows from the equivalent condition
	/// 
	/// -ln(u(0)) - ln(u(1)) - ... - ln(u(n)) > 1
	/// 
	/// where the left hand side is seen to be a sum of "waiting times" drawn from
	/// the corresponding negative exponential distribution.
	/// 
	/// When the parameter a is greater than some implementation-defined value, for
	/// instance 20.0, the value may be approximated by entier(normal(a,sqrt(a),U) +
	/// 0.5) or, when this is negative, by zero.
	/// 
	/// @param a The long real parameter a
	/// @param U The pseudo random number (seed) by name.
	/// @return Returns the next pseudorandom, according to the Poisson distribution

	public static int Poisson(double a, final RTS_NAME<Integer> U) {
		int res;
		if (a <= 0.0)
			res = 0;
		else if (a > 20.0) {
			// entier(normal(a,sqrt(a),U) + 0.5)
			double sqa = Math.sqrt(a);
			res = entier(normal(a, sqa, U) + 0.5);
		} else {
			double acc = 1.0;
			res = 0;
			double xpa = Math.exp(-a);
			do {
				acc = acc *  RTS_RandomDrawing.basicDRAW(U);
				res = res + 1;
			} while (acc >= xpa);
			res = res - 1;
		}
		return (res);
	}

	///// *********************************************************************
	///// ** Random drawing: Procedure Erlang
	///// *********************************************************************

	/// Random drawing: Erlang distribution.
	/// <pre>
	/// long real procedure Erlang (a,b,U);
	///          name U; long real a,b; integer U;
	/// </pre>
	/// 
	/// The value is a drawing from the Erlang distribution with mean 1/a and
	/// standard deviation 1/(a*sqrt(b)). It is defined by b basic drawings u(i), if
	/// b is an integer value,
	/// 
	/// - ( ln(u(1)) + ln(u(2)) + ... + ln(u(b)) ) / (a*b)
	/// 
	/// and by c+1 basic drawings u(i) otherwise, where c is equal to entier(b),
	/// 
	/// - ( ln(u(1)) + ... + ln(u(c)) + (b-c)*ln(u(c+1)) ) / (a*b)
	/// 
	/// Both a and b must be greater than zero.
	/// 
	/// The last formula represents an approximation.
	/// 
	/// @param a The long real parameter a
	/// @param b The long real parameter b
	/// @param U The pseudo random number (seed) by name.
	/// @return Returns the next pseudorandom, according to the Erlang distribution

	public static double Erlang(final double a, final double b, final RTS_NAME<Integer> U) {
		double res;
		if (a <= 0.0 || b <= 0.0)
			throw new RTS_SimulaRuntimeError("Erlang(a,b,u):  a <= 0  or  b <= 0");
		res = 0;
		int c = entier(b);
		double bc = b - c;
		double ab = a *  b;
		while ((c--) > 0) {
			double v = RTS_RandomDrawing.basicDRAW(U);
			double z = Math.log(v);
			res = res - (z / ab);
		}
		if (bc > 0.0) {
			double v = RTS_RandomDrawing.basicDRAW(U);
			double z = Math.log(v);
			res = res - ((bc *  z) / ab);
		}
		return (res);
	}

	///// *********************************************************************
	///// ** Random drawing: Procedure discrete
	///// *********************************************************************

	/// Random drawing: discrete distribution.
	/// <pre>
	///  integer procedure discrete (A,U);
	///           name U; <real-type> array A; integer U;
	/// </pre>
	/// 
	/// The one-dimensional array A, augmented by the element 1 to the right, is
	/// interpreted as a step function of the subscript, defining a discrete
	/// (cumulative) distribution function.
	/// 
	/// The function value satisfies
	/// 
	/// lowerbound(A,1) <= discrete(A,U) <= upperbound(A,1)+1.
	/// 
	/// It is defined as the smallest i such that A(i) > u, where u is a basic
	/// drawing and A(upperbound(A,1)+1) = 1.
	/// 
	/// @param A a real-type array
	/// @param U The pseudo random number (seed) by name.
	/// @return Returns the next pseudorandom, discrete distributed according to the array A

	public static int discrete(final RTS_REALTYPE_ARRAY A, final RTS_NAME<Integer> U) {
		int result;
		int lb = A.lowerBound(0);
		int ub = A.upperBound(0);
		double v = RTS_RandomDrawing.basicDRAW(U);
		int nelt = ub - lb + 1;
		result = ub + 1;
		int j = 0;
		do {
			if (A.getRealTypeELEMENT(j) > v) {
				result = lb + j;
				nelt = 0;
			}
			j = j + 1;
		} while (j < nelt);
		return (result);
	}

	///// *********************************************************************
	///// ** Random drawing: Procedure linear
	///// *********************************************************************

	/// Random drawing: linear distribution.
	/// <pre>
	///  long real procedure linear (A,B,U);
	///       name U; <real-type> array A,B; integer U;
	/// </pre>
	/// 
	/// The value is a drawing from a (cumulative) distribution function F, which is
	/// obtained by linear interpolation in a non-equidistant table defined by A and
	/// B, such that A(i) = F(B(i)).
	/// 
	/// It is assumed that A and B are one-dimensional arrays of the same length,
	/// that the first and last elements of A are equal to 0 and 1 respectively and
	/// that A(i) >= A(j) and B(i) > B(j) for i>j. If any of these conditions are not
	/// satisfied, the effect is implementation-defined.
	/// 
	/// The steps in the function evaluation are:
	/// 
	/// l. draw a uniform <0,1> random number, u.
	/// 
	/// 2. determine the lowest value of i, for which
	/// 
	/// A(i-1) <= u <= A(i)
	/// 
	/// 3. compute D = A(i) - A(i-1)
	/// 
	/// 4. if D = 0: linear = B(i-1) if D ne 0: linear = B(i-1) + (B(i) -
	/// B(i-1))*(u-A(i-1))/D
	/// 
	/// @param A a real-type array
	/// @param B a real-type array
	/// @param U The pseudo random number (seed) by name.
	/// @return Returns the next pseudorandom, lineary distributed according to the arrays A and B

	public static double linear(final RTS_REALTYPE_ARRAY A, final RTS_REALTYPE_ARRAY B, final RTS_NAME<Integer> U) {
		double res;
		int nelt = A.SIZE;
		if (nelt != B.SIZE)
			throw new RTS_SimulaRuntimeError("Linear(A,B,U): The number of elements in A and B are different.");
		double v = RTS_RandomDrawing.basicDRAW(U);
		int i = 0;
		while (A.getRealTypeELEMENT(i) < v)
			i = i + 1;
		if (i == 0) {
			if (v == 0.0 && A.getRealTypeELEMENT(i) == 0.0)
				i = 1;
			else
				throw new RTS_SimulaRuntimeError("Linear(A,B,U): The array a does not satisfy the stated assumptions.");
		} else if (i >= nelt)
			throw new RTS_SimulaRuntimeError("Linear(A,B,U): The array a does not satisfy the stated assumptions.");

		double a_val = A.getRealTypeELEMENT(i);
		double a_lag = A.getRealTypeELEMENT(i - 1);
		double a_dif = a_val - a_lag;
		if (a_dif == 0.0)
			res = B.getRealTypeELEMENT(i - 1);
		else {
			double b_val = B.getRealTypeELEMENT(i);
			double b_lag = B.getRealTypeELEMENT(i - 1);
			res = (((b_val - b_lag) / a_dif) *  (v - a_lag)) + b_lag;
		}
		return (res);
	}

	///// *********************************************************************
	///// ** Utility: Procedure histd
	///// *********************************************************************

	/// Utility: Procedure histd.
	/// <pre>
	/// integer procedure histd (A,U);
	///          name U; <real-type> array A; integer U;
	/// </pre>
	/// 
	/// The value is an integer in the range (lsb,usb), where lsb and usb are the
	/// lower and upper subscript bounds of the one-dimensional array A. The latter
	/// is interpreted as a histogram defining the relative frequencies of the
	/// values.
	/// 
	/// @param A a real-type array
	/// @param U The pseudo random number (seed) by name.
	/// @return Returns the next pseudorandom, distributed according to the array A

	public static int histd(final RTS_REALTYPE_ARRAY A, final RTS_NAME<Integer> U) {
		int result = 0;
		int j; // Array index.
		int nelt; // Number of array elements.
		double sum; // Sum of all array element values.
		double wsum; // Weighted sum of all array element values.
		double tmp; // Temporary variabel.

		int lb = A.lowerBound(0);
		nelt = A.SIZE;
		j = 0;
		sum = 0.0;
		do {
			tmp = A.getRealTypeELEMENT(j);
			if (tmp < 0.0)
				throw new RTS_SimulaRuntimeError("Histd(a,u):  An element of the array a is negative");
			sum = sum + tmp;
			j = j + 1;
		} while (j < nelt);
		wsum = sum *  RTS_RandomDrawing.basicDRAW(U); // Make 0 <= wsum < sum
		j = 0;
		sum = 0.0;
		do {
			sum = sum + A.getRealTypeELEMENT(j);
			if (sum >= wsum) {
				result = lb + j;
				nelt = 0;
			} // We will do this once and only once.
			j = j + 1;
		} while (j < nelt);
		return (result);
	}

	///// ****************************************
	///// ** Calendar and timing utilities/// **
	///// ****************************************

	/// Standard Procedure datetime.
	/// <pre>
	/// text procedure datetime;   datetime :- Copy("...");
	/// </pre>
	/// 
	/// The value is a text frame containing the current date and time in the form
	/// YYYY-MM-DD HH:MM:SS.sss.... The number of decimals in the field for seconds
	/// is implementation-defined.
	/// 
	/// @return a formated text
	public static RTS_TXT datetime() {
		DateTimeFormatter form = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		String datim = LocalDateTime.now().format(form);
		return (new RTS_TXT(datim));
	}


	/// Standard Procedure cputime.
	/// <pre>
	/// long real procedure cputime;
	/// </pre>
	/// 
	/// The value is the number of processor seconds spent by the calling program.
	/// 
	/// @return seconds since start of program
	public static double cputime() {
		double cputime = System.currentTimeMillis() - _STARTTIME;
		return (cputime / 1000);
	}


	/// Standard Procedure clocktime.
	/// <pre>
	/// long real procedure clocktime;
	/// </pre>
	/// 
	/// @return The value is the number of seconds since midnight.
	public static double clocktime() {
		LocalTime localTime = LocalTime.now();
		int hour = localTime.getHour();
		int minute = localTime.getMinute();
		int second = localTime.getSecond();
		double time = ((hour *  60) + minute) *  60 + second;
		return (time);
	}

	///// ****************************************
	///// ** Miscellaneous utilities/// **
	///// ****************************************


	/// Standard Procedure histo.
	/// <pre>
	/// procedure histo(A,B,c,d);
	///           real array A,B; real c,d;
	/// </pre>
	/// 
	/// Procedure statement "histo(A,B,c,d)" updates a histogram defined by the
	/// one-dimensional arrays A and B according to the observation c with the weight
	/// d. A(lba+i) is increased by d, where i is the smallest integer such that c <=
	/// B(lbb+i) and lba and lbb are the lower bounds of A and B respectively. If the
	/// length of A is not one greater than that of B the effect is
	/// implementation-defined. The last element of A corresponds to those
	/// observations which are greater than all elements of B.
	/// 
	/// @param A real array a
	/// @param B real array b
	/// @param c real value c
	/// @param d real value d
	public static void histo(final RTS_REAL_ARRAY A, final RTS_REAL_ARRAY B, final float c, final float d) {
		if (A.nDim() != 1)
			throw new RTS_SimulaRuntimeError("histo(A,B,c,d) - A is not one-dimensional");
		if (B.nDim() != 1)
			throw new RTS_SimulaRuntimeError("histo(A,B,c,d) - B is not one-dimensional");
		int nelt = B.SIZE;
		if (nelt >= A.SIZE)
			throw new RTS_SimulaRuntimeError("histo(A,B,c,d) - A'length <= B'length");
		int i = 0;
		EX: do {
			if (B.ELTS[i] >= c)
				break EX;
			i = i + 1;
		} while (i < nelt);
		A.ELTS[i] = A.ELTS[i] + d;
	}

	///// *********************************************************************
	///// ** Additional Standard Procedures
	///// *********************************************************************

	/// Extended Standard procedure argv.
	/// @param index case index
	/// @return requested text info
	public static RTS_TXT argv(final int index) {
		RTS_TXT arg = null;
		try {
			arg = new RTS_TXT(RTS_Option.argv[index]);
		} catch(Exception e) {}
		return(arg);
	}

	/// Extended Standard procedure exit.
	/// @param status exit status
	public static void exit(final int status) {
//		if(console != null) {
//			console.write("EXIT: ");  // TODO:
//			console.read();
//		}
//		IO.println("RTS_ENVIRONMENT.exit: Just before 'System.exit(status)'  status="+status);
		System.exit(status);
	}


	///// *********************************************************************
	///// ** Editing: Text Procedure edit, edfix, edtime
	///// *********************************************************************


	/// Extended Standard procedure edit.
	/// @param n the argument
	/// @return the edited text
	public static RTS_TXT edit(final boolean n) {
		return (new RTS_TXT("" + n));
	}


	/// Extended Standard procedure edit.
	/// @param n the argument
	/// @return the edited text
	public static RTS_TXT edit(final char n) {
		return (new RTS_TXT("" + n));
	}


	/// Extended Standard procedure edit.
	/// @param n the argument
	/// @return the edited text
	public static RTS_TXT edit(final long n) {
		return (new RTS_TXT(("" + n)));
	}


	/// Extended Standard procedure edit.
	/// @param n the argument
	/// @return the edited text
	public static RTS_TXT edit(final int n) {
		return (new RTS_TXT(("" + n)));
	}


	/// Extended Standard procedure edit.
	/// @param n the argument
	/// @return the edited text
	public static RTS_TXT edit(final float n) {
		return (new RTS_TXT(("" + n).replace('E', '&')));
	}


	/// Extended Standard procedure edit.
	/// @param n the argument
	/// @return the edited text
	public static RTS_TXT edit(final double n) {
		return (new RTS_TXT(("" + n).replace("E", "&&")));
	}


	/// Extended Standard procedure edit.
	/// @param r the real argument
	/// @param n number of digits after decimal sign
	/// @return the edited text
	public static RTS_TXT edfix(final double r, final int n) {
		RTS_TXT T = blanks(n + 10);
		RTS_TXT.putfix(T, r, n);
		String S = T.edText().trim();
		return (new RTS_TXT(S));
	}


	/// Extended Standard procedure edit.
	/// @param r the real argument
	/// @param n number of digits after decimal sign
	/// @return the edited text
	public static RTS_TXT edfix(final float r, final int n) {
		RTS_TXT T = blanks(n + 10);
		RTS_TXT.putfix(T, r, n);
		String S = T.edText().trim();
		return (new RTS_TXT(S));
	}
	

	/// Edit simulated time.
	/// @param hours simulated time 
	/// @return the edited text
	public static RTS_TXT edtime(float hours) {
		return(new RTS_TXT(LocalTime.ofSecondOfDay((long) (hours*60*60)).toString()));
	}
	

	/// Edit simulated time.
	/// @param hours simulated time 
	/// @return the edited text
	public static RTS_TXT edtime(double hours) {
		return(new RTS_TXT(LocalTime.ofSecondOfDay((long) (hours*60*60)).toString()));
	}


	///// *********************************************************************
	///// ** Utility: Procedure waitSomeTime
	///// *********************************************************************
	

	/// Extended Standard procedure waitSomeTime.
	/// @param millies wait time in millisecods
	public static void waitSomeTime(final int millies) {
		try {
			Thread.sleep(millies);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	///// *********************************************************************
	///// ** Utility: Procedure printThreadList
	///// *********************************************************************

	/// Extended Standard procedure printThreadList.
	/// @param withStackTrace true: if stacktrace is wanted
	public static void printThreadList(final boolean withStackTrace) {
		RTS_UTIL.printThreadList(withStackTrace);
	}

	///// *********************************************************************
	///// ** Utility: Procedure printStaticChain
	///// *********************************************************************
	

	/// Extended Standard procedure printStaticChain.
	/// 
	/// See {@link RTS_ENVIRONMENT#DEFEXCEPTION(RTS_PRCQNT)}
	public static void printStaticChain() {
		RTS_UTIL.printStaticChain(RTS_RTObject._CUR);
	}

	///// *********************************************************************
	///// ** Additional S-Port'like Procedures
	///// *********************************************************************


	/// The registered EXCEPTION_HANDLER or null.
	public static RTS_PRCQNT EXCEPTION_HANDLER = null;

	/// S-PORT Extension Procedure DEFEXCEPTION.
	///
	/// Register an EXCEPTION_HANDLER to be used by the runtime system when a runtime error occur.
	/// @param EXCEPTION_HANDLER the argument
	public static void DEFEXCEPTION(final RTS_PRCQNT EXCEPTION_HANDLER) {
		RTS_ENVIRONMENT.EXCEPTION_HANDLER = EXCEPTION_HANDLER;
	}


	/// S-PORT Extension Procedure hash.
	/// @param txt the argument
	/// @return resulting hash value
	public static int hash(final RTS_TXT txt) {
		if (txt == null || RTS_TXT.length(txt) == 0)
			return (0);
		String s = txt.edText();
		String tstrip = s.trim();
		if (tstrip.length() == 0)
			return (0);
		int a = tstrip.charAt(0);
		if (tstrip.length() > 3) {
			a = a + 8 *  tstrip.charAt(1);
			a = a + 64 *  rank(tstrip.charAt(2));
		}
		a = a + 512 *  tstrip.charAt(tstrip.length() - 1) + s.length();
		return (a & 255);
	}


	/// Text Extension Procedure loadChar.
	/// 
	/// Load a character from a text.
	/// 
	/// @param t a text reference
	/// @param p the text position in which the character is stored 
	/// @return the character at position p
	public static char loadChar(final RTS_TXT t, final int p) {
		// Returns the character at position p+1 (NB).
		// I.e. characters are counted from zero
		// c=t.char[p];
		RTS_TEXTOBJ obj = t.OBJ;
		char c = obj.MAIN[t.START + p];
		return (c);
	}


	/// Text Extension Procedure storeChar.
	/// 
	/// Store character into a text.
	/// 
	/// @param c a character
	/// @param t a text reference
	/// @param p the text position in which the character is stored 
	public static void storeChar(final char c, final RTS_TXT t, final int p) {
		// Deposit c at position p+1 (NB).
		// I.e. characters are counted from zero
		// t.char[p]=c;
		RTS_TEXTOBJ obj = t.OBJ;
		obj.MAIN[t.START + p] = c;
	}
	
	
}
