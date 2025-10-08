/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass.declaration;

import simula.compiler.utilities.ClassHierarchy;
import simula.compiler.utilities.Meaning;
import simula.compiler.utilities.ObjectKind;
import simula.compiler.utilities.ObjectList;
import simula.compiler.utilities.Option;
import simula.compiler.utilities.Util;

import java.lang.constant.ClassDesc;
import simula.compiler.syntaxClass.OverLoad;
import simula.compiler.syntaxClass.Type;
import simula.compiler.syntaxClass.expression.Constant;
import simula.compiler.syntaxClass.statement.InlineStatement;
import simula.compiler.syntaxClass.statement.Statement;

/// Standard Class.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/declaration/StandardClass.java">
/// <b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class StandardClass extends ClassDeclaration {
	public String edJavaClassName() {
		return (identifier);
	}

	/// The type text.
	public static StandardClass typeText;

	/// The Standard Class ENVIRONMENT.
	public static StandardClass ENVIRONMENT;

	/// The Standard Class BASICIO.
	public static StandardClass BASICIO;

	/// The Standard Class CLASS.
	static StandardClass CLASS;

	/// The Standard Class Infile.
	public static StandardClass Infile;

	/// The Standard Class Printfile.
	public static StandardClass Printfile;

	/// The Standard Class CatchingErrors.
	public static StandardClass CatchingErrors;

	/// Method to initiate all standard classes.
	public static void INITIATE() {
		initTypeText();
		initUNIVERSE();
		initRTObject();
		initENVIRONMENT();
		initBASICIO();
		initCLASS();
		initFile();
			initImagefile();
				initInfile();
				initOutfile();
				initDirectfile();
				initPrintfile();
			initBytefile();
				initInbytefile();
				initOutbytefile();
				initDirectbytefile();
		initSimset();
			initLinkage();
			initHead();
			initLink();
		initSimulation();
			initEVENT_NOTICE();
			initProcess();
			initMAIN_PROGRAM();
			
		if(Option.EXTENSIONS) {
			initCatchingErrors();
			initDEC_Lib();
			initDrawing();
				initShapeElement();
				initTextElement();
		}
	}


	// ******************************************************************
	// *** The Type TXT
	// ******************************************************************
	/// Initiate the The Type Text.
	private static void initTypeText() {
		String[] mtd = { "(Lsimula/runtime/RTS_TXT;FI)V", "(Lsimula/runtime/RTS_TXT;DI)V" };
		typeText=new StandardClass("TXT");
		typeText.isContextFree=true;
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"constant");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"start");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"length");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"main");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"pos");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,null,"setpos",parameter("i",Type.Integer));  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"more");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Character,"getchar");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,null,"putchar",parameter("c",Type.Character));  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"sub",parameter("i",Type.Integer),parameter("n",Type.Integer));  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"strip");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"getint");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.LongReal,"getreal");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"getfrac");  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,null,"putint",parameter("i",Type.Integer));  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,null,"putfrac",parameter("i",Type.Integer),parameter("n",Type.Integer));  
		typeText.addStandardProcedure(ObjectKind.MemberMethod,mtd,null,"putfix", parameter("r",new OverLoad(Type.Real,Type.LongReal)),parameter("n",Type.Integer)); 
		typeText.addStandardProcedure(ObjectKind.MemberMethod,mtd,null,"putreal",parameter("r",new OverLoad(Type.Real,Type.LongReal)),parameter("n",Type.Integer)); 
		// **************************************
		// *** Additional Text Procedures ***
		// **************************************
		if(Option.EXTENSIONS) {
			typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"trim"); 
			typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Character,"loadChar",parameter("i",Type.Integer)); 
			typeText.addStandardProcedure(ObjectKind.MemberMethod,null,"storeChar",parameter("c",Type.Character),parameter("i",Type.Integer)); 
			typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"startsWith",parameter("t",Type.Text)); 
			typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"endsWith",parameter("t",Type.Text)); 
			typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"indexOf",parameter("c",Type.Character)); 
			typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"lastIndexOf",parameter("c",Type.Character)); 
			typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"replace",parameter("old",Type.Character),parameter("new",Type.Character)); 
			typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"replaceText",parameter("old",Type.Text),parameter("new",Type.Text)); 
			typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"toLowerCase"); 
			typeText.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"toUpperCase"); 
		}
	}

	// ******************************************************************
	// *** The Standard Class UNIVERSE
	// ******************************************************************
	/// The Standard Class UNIVERSE.
	private static StandardClass UNIVERSE;
	
	/// Initiate the Standard Class UNIVERSE
	private static void initUNIVERSE() {
		UNIVERSE = new StandardClass("UNIVERSE");
		UNIVERSE.isContextFree = true;
		UNIVERSE.declaredIn = null;
	}
	  
	// ******************************************************************
	// *** The Standard Class RTObject - Prefix to all classes
	// ******************************************************************
	/// Initiate the Standard Class RTObject.
	private static void initRTObject() {
		StandardClass RTObject = new StandardClass("RTObject");
		UNIVERSE.addStandardClass(RTObject); // Declared in UNIVERSE
		RTObject.isContextFree = true;
		RTObject.addStandardProcedure(ObjectKind.MemberMethod, Type.Text, "objectTraceIdentifier");
		RTObject.addStandardProcedure(ObjectKind.MemberMethod, null, "detach"); // Nødvendig for å kompilere Simuletta
	}
	  
	// ******************************************************************
	// *** The Standard Class ENVIRONMENT
	// ******************************************************************
	/// Initiate the Standard Class ENVIRONMENT.
	private static void initENVIRONMENT() {
		ENVIRONMENT=new StandardClass("RTObject","ENVIRONMENT");
		UNIVERSE.addStandardClass(ENVIRONMENT); // Declared in UNIVERSE
		ENVIRONMENT.isContextFree=true; // This class is a Context i.e. all members are static

		//	    Environmental enquiries ................................. 9.6
		//	    Procedure sourceline.
		//	    Constants  maxrank, maxint, minint, maxreal, minreal,
		//	      maxlongreal, minlongreal, simulaid.

		ENVIRONMENT.addStandardAttribute(Type.LongReal,"maxlongreal",Double.MAX_VALUE);  
		ENVIRONMENT.addStandardAttribute(Type.LongReal,"minlongreal",-Double.MAX_VALUE);  
//		ENVIRONMENT.addStandardAttribute(Type.LongReal,"minlongreal",Double.MIN_VALUE);  
		ENVIRONMENT.addStandardAttribute(Type.Real,"maxreal",Float.MAX_VALUE);  
		ENVIRONMENT.addStandardAttribute(Type.Real,"minreal",-Float.MAX_VALUE);  
//		ENVIRONMENT.addStandardAttribute(Type.Real,"minreal",Float.MIN_VALUE);  
		ENVIRONMENT.addStandardAttribute(Type.Integer,"maxrank",255);  
		ENVIRONMENT.addStandardAttribute(Type.Integer,"maxint",Integer.MAX_VALUE);  
		ENVIRONMENT.addStandardAttribute(Type.Integer,"minint",Integer.MIN_VALUE);  
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"simulaid");
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"sourceline");

		//	    Basic operations ........................................ 9.1
		//	    Procedures mod, rem, abs, sign, entier,
		//	      addepsilon, subepsilon.

		String[] mtd = { "(F)F", "(D)D" };

		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"mod",parameter("i",Type.Integer),parameter("j",Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"rem",parameter("i",Type.Integer),parameter("j",Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"abs",parameter("e",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"sign",parameter("e",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"entier",parameter("e",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,mtd,new OverLoad(Type.Real,Type.LongReal),"addepsilon",parameter("e",new OverLoad(Type.Real,Type.LongReal)));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,mtd,new OverLoad(Type.Real,Type.LongReal),"subepsilon",parameter("e",new OverLoad(Type.Real,Type.LongReal)));

		//	    Text utilities .......................................... 9.2
		//	    Procedures copy, blanks, char, isochar, rank, isorank,
		//	      digit, letter, lowten, decimalmark, upcase, lowcase.

		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"copy",parameter("T",Parameter.Mode.value,Type.Text));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"blanks",parameter("n",Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"Char",parameter("n",Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"_char",parameter("n",Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"isochar",parameter("n",Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"rank",parameter("c",Type.Character));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"isorank",parameter("c",Type.Character));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Boolean,"digit",parameter("c",Type.Character));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Boolean,"letter",parameter("c",Type.Character));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"lowten",parameter("c",Type.Character));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"decimalmark",parameter("c",Type.Character));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"upcase",parameter("t",Type.Text));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"lowcase",parameter("t",Type.Text));

		//	    Scheduling .............................................. 9.3
		//	    Procedures call (7.3.2), resume (7.3.3).

		//	    Mathematical functions .................................. 9.4
		//	    Procedures sqrt, sin, cos, tan, cotan, arcsin, arccos,
		//	      arctan, arctan2, sinh, cosh, tanh, ln, log10, exp.

		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"sqrt",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"sin",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"cos",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"tan",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"cotan",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"arcsin",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"arccos",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"arctan",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"arctan2",parameter("x",Type.LongReal),parameter("y",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"sinh",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"cosh",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"tanh",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"ln",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"log10",parameter("x",Type.LongReal));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"exp",parameter("x",Type.LongReal));

		//	    Extremum functions ...................................... 9.5
		//	    Procedures max, min.

		String[] mtd2 = { "(II)I", "(IF)F", "(ID)D", "(FI)F", "(FF)F", "(FD)D", "(DI)D", "(DF)D", "(DD)D",
				          "(CC)C", "(Lsimula/runtime/RTS_TXT;Lsimula/runtime/RTS_TXT;)Lsimula/runtime/RTS_TXT;" };

		OverLoad types = new OverLoad(Type.Integer,Type.Real,Type.LongReal,Type.Character,Type.Text);
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,mtd2,types,"min",parameter("x",types),parameter("y",types));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,mtd2,types,"max",parameter("x",types),parameter("y",types));

		//	    Error control ........................................... 9.7
		//	    Procedure error.

		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"error",parameter("msg",Type.Text));

		// Array quantities ........................................ 9.8
		//	    Procedures upperbound, lowerbound.

		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"upperbound",parameter("a",null,Parameter.Kind.Array),parameter("i",Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"lowerbound",parameter("a",null,Parameter.Kind.Array),parameter("i",Type.Integer));

		// Random drawing .......................................... 9.9
		//	    Procedures draw, randint, uniform, normal, negexp,
		//	      Poisson, Erlang, discrete, linear, histd.

		String[] mtdx = { "SPECIAL", "SPECIAL" };

		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Boolean,"draw",parameter("a",Type.LongReal),parameter("U",Parameter.Mode.name,Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"randint",parameter("a",Type.Integer),parameter("b",Type.Integer),parameter("U",Parameter.Mode.name,Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"uniform",parameter("a",Type.LongReal),parameter("b",Type.LongReal),parameter("U",Parameter.Mode.name,Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"normal",parameter("a",Type.LongReal),parameter("b",Type.LongReal),parameter("U",Parameter.Mode.name,Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"negexp",parameter("a",Type.LongReal),parameter("U",Parameter.Mode.name,Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"Poisson",parameter("a",Type.LongReal),parameter("U",Parameter.Mode.name,Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"Erlang",parameter("a",Type.LongReal),parameter("b",Type.LongReal),parameter("U",Parameter.Mode.name,Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,mtdx,Type.Integer,"discrete",parameter("A",new OverLoad(Type.Real,Type.LongReal),Parameter.Kind.Array,1),parameter("U",Parameter.Mode.name,Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,mtdx,Type.LongReal,"linear",parameter("A",new OverLoad(Type.Real,Type.LongReal),Parameter.Kind.Array,1),parameter("B",new OverLoad(Type.Real,Type.LongReal),Parameter.Kind.Array,1),parameter("U",Parameter.Mode.name,Type.Integer));
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,mtdx,Type.Integer,"histd",parameter("A",new OverLoad(Type.Real,Type.LongReal),Parameter.Kind.Array,1),parameter("U",Parameter.Mode.name,Type.Integer));

		//	    Calendar and timing utilities ........................... 9.10
		//	    Procedures datetime, cputime, clocktime.

		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"datetime");
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"cputime");
		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"clocktime");

		//	    Miscellaneous utilities ................................. 9.11
		//	    Procedure histo.

		ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"histo",parameter("A",Type.Real,Parameter.Kind.Array,1),parameter("B",Type.Real,Parameter.Kind.Array,1)
				,parameter("c",Type.Real),parameter("d",Type.Real));
		//	    ENVIRONMENT.addStandardProcedure(BlockKind.ContextFreeMethod,Type.Text,"objectTraceIdentifier");

		// **************************************
		// *** Additional Standard Procedures ***
		// **************************************
		if(Option.EXTENSIONS) {
			
			String[] mtd4 = { "(I)Lsimula/runtime/RTS_TXT;", "(F)Lsimula/runtime/RTS_TXT;","(D)Lsimula/runtime/RTS_TXT;","(Z)Lsimula/runtime/RTS_TXT;","(C)Lsimula/runtime/RTS_TXT;" };
			String[] mtd5 = { "(FI)Lsimula/runtime/RTS_TXT;", "(DI)Lsimula/runtime/RTS_TXT;" };
			String[] mtd6 = { "(F)Lsimula/runtime/RTS_TXT;", "(D)Lsimula/runtime/RTS_TXT;" };

			ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"waitSomeTime"
				,parameter("millies",Type.Integer)); 
			ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"printThreadList"
				,parameter("withStackTrace",Type.Boolean));
			ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"printStaticChain");
			ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,mtd4,Type.Text,"edit"
				,parameter("x",new OverLoad(Type.Integer,Type.Real,Type.LongReal,Type.Boolean,Type.Character)));
			ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,mtd5,Type.Text,"edfix"
				,parameter("x",new OverLoad(Type.Real,Type.LongReal)),parameter("n",Type.Integer));
			ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,mtd6,Type.Text,"edtime"
				,parameter("x",new OverLoad(Type.Real,Type.LongReal)));
			ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"argv",parameter("index",Type.Integer)); 
			ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"exit",parameter("status",Type.Integer)); 
			ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"hash",parameter("t",Type.Text));
			ENVIRONMENT.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"DEFEXCEPTION",parameter("erh",Parameter.Kind.Procedure,Parameter.Mode.value,null));
		}
	}

	// ******************************************************************
	// *** The Standard Class BASICIO
	// ******************************************************************
	/// Initiate the Standard Class BASICIO.
	/// <pre>
	///  ENVIRONMENT class BASICIO (INPUT_LINELENGTH, OUTPUT_LINELENGTH);
	///  integer INPUT_LINELENGTH, OUTPUT_LINELENGTH;
	///  begin ref (Infile) SYSIN; ref (Printfile) SYSOUT;
	///        ref (Infile)    procedure sysin;   sysin  :- SYSIN;
	///        ref (Printfile) procedure sysout;  sysout :- SYSOUT;
	/// 
	///        procedure terminate_program;
	///        begin ... ;  goto STOP  end terminate_program;
	/// 
	///            class File 
	///       File class Imagefile
	///       File class Bytefile
	///  Imagefile class Infile
	///  Imagefile class Outfile
	///  Imagefile class Directfile
	///    Outfile class Printfile
	///   Bytefile class Inbytefile
	///   Bytefile class Outbytefile 
	///   Bytefile class Directbytefile
	/// 
	///        SYSIN  :- new Infile("...");    ! Implementation-defined
	///        SYSOUT :- new Printfile("..."); ! files names;
	///        SYSIN.open(blanks(INPUT_LINELENGTH));
	///        SYSOUT.open(blanks(OUTPUT_LINELENGTH));
	///        inner;
	///  STOP: SYSIN.close;
	///        SYSOUT.close
	///  end BASICIO;
	/// </pre>
	private static void initBASICIO() {
		BASICIO=new StandardClass("RTObject","BASICIO");
		ENVIRONMENT.addStandardClass(BASICIO); // Declared in ENVIRONMENT
		BASICIO.isContextFree=true;
		BASICIO.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Ref("Infile"),"sysin");  
		BASICIO.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Ref("Printfile"),"sysout");  
		BASICIO.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"terminate_program");  
		BASICIO.addStandardProcedure(ObjectKind.MemberMethod,null,"call",parameter("obj",Type.Ref("RTObject")));
		BASICIO.addStandardProcedure(ObjectKind.MemberMethod,null,"resume",parameter("obj",Type.Ref("RTObject")));
	}


	// ******************************************************************
	// *** The Standard Class CLASS
	// ******************************************************************
	/// Initiate the Standard Class CLASS.
	/// 
	/// Simula Stadard States: Fictituous outermost prefix
	/// Any class that has no (textually given) prefix is by definition
	/// prefixed by a fictitious class whose only attribute is:
	/// <pre>
	/// 	          procedure detach; ... ;  (see 7.3.1)
	/// </pre>
	/// Thus every class object or instance of a prefixed block has this attribute.

	private static void initCLASS() {
		CLASS=new StandardClass("RTObject","CLASS");
		ENVIRONMENT.addStandardClass(CLASS);  // Declared in ENVIRONMENT
		CLASS.addStandardProcedure(ObjectKind.MemberMethod,null,"detach");
	}

	// ******************************************************************
	// *** The Standard Class File
	// ******************************************************************
	/// The Standard Class File.
	private static StandardClass File;
	
	/// Initiate the Standard Class File.
	/// <pre>
	///  class File(FILENAME); value FILENAME; text FILENAME;
	///  begin
	///     Boolean OPEN_;
	///     text procedure filename; filename:=copy(FILENAME);
	///     Boolean procedure isopen; isopen:=OPEN_;
	///     Boolean procedure setaccess(mode);  text mode; ... ;
	///  
	///     if FILENAME = notext then error("Illegal File Name");
	///  end File;      
	/// </pre>
	private static void initFile() {
		File=new StandardClass("CLASS","File",parameter("FILENAME_",Type.Text));
		BASICIO.addStandardClass(File);  // Declared in BASICIO
		File.addStandardAttribute(Type.Boolean,"OPEN_");  
		File.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"filename");
		File.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"isopen");
		File.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"setaccess",parameter("mode",Type.Text));  
	}  

	// ******************************************************************
	// *** The Standard File Class Imagefile
	// ******************************************************************
	/// The Standard Class Imagefile.
	private static StandardClass Imagefile;
	
	/// Initiate the Standard Class Imagefile.
	/// <pre>
	///  File class Imagefile;
	///  begin text image;
	///     procedure setpos(i);  integer i;  image.setpos(i);
	///     integer procedure pos;     pos    := image.pos;
	///     Boolean procedure more;    more   := image.more;
	///     integer procedure length;  length := image.length;
	///  end Imagefile;
	/// </pre>
	private static void initImagefile() {
		Imagefile=new StandardClass("File","Imagefile");
		BASICIO.addStandardClass(Imagefile);  // Declared in BASICIO
		Imagefile.addStandardAttribute(Type.Text,"image");  
		Imagefile.addStandardProcedure(ObjectKind.MemberMethod,null,"setpos",parameter("i",Type.Integer));  
		Imagefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"pos");  
		Imagefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"more");  
		Imagefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"length");   
	}

	// ******************************************************************
	// *** The Standard Imagefile Class Infile
	// ******************************************************************
	/// Initiate the Standard Class Infile.
	/// <pre>
	///  Imagefile class Infile;
	///  begin Boolean ENDFILE;
	///     Boolean procedure endfile;  endfile:= ENDFILE;
	///     Boolean procedure open(fileimage); text fileimage;
	///     Boolean procedure close;
	///     procedure inimage;
	///     Boolean procedure inrecord;
	///     character procedure inchar;
	///     Boolean procedure lastitem;
	///     text procedure intext(w); integer w;
	///     integer procedure inint;
	///     long real procedure inreal;
	///     integer procedure infrac;
	/// 
	///     ENDFILE:= true
	///     ...
	///  end Infile;
	/// </pre>
	private static void initInfile() {
		Infile=new StandardClass("Imagefile","Infile");
		BASICIO.addStandardClass(Infile);  // Declared in BASICIO
		Infile.addStandardAttribute(Type.Boolean,"ENDFILE_");  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"endfile");  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"open",parameter("fileimage",Type.Text));  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"close");  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,null,"inimage");  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"inrecord");  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,Type.Character,"inchar");  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"lastitem");  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"intext",parameter("w",Type.Integer));  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"inint");  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,Type.LongReal,"inreal");  
		Infile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"infrac");  
	}  

	// ******************************************************************
	// *** The Standard Imagefile Class Outfile
	// ******************************************************************
	/// Initiate the Standard Class Outfile.
	/// <pre>
	///  Imagefile class Outfile;
	///  begin
	///     Boolean procedure open(fileimage);  text fileimage;
	///     Boolean procedure close;
	///     procedure outimage;
	///     procedure outrecord;
	///     procedure breakoutimage;
	///     Boolean procedure checkpoint;
	///     procedure outchar(c); character c;
	///     procedure outtext(t); text t;
	///     text procedure FIELD(w); integer w;
	///     procedure outint(i,w); integer i,w;
	///     procedure outfix(r,n,w); long real r; integer n,w;
	///     procedure outreal(r,n,w); long real r; integer n,w;
	///     procedure outfrac(i,n,w); integer i,n,w;
	/// 
	///    ... ;
	/// end Outfile;
	/// </pre>
	private static void initOutfile() { 
		String[] mtd = { "(FII)V", "(DII)V" };

		StandardClass Outfile=new StandardClass("Imagefile","Outfile");
		BASICIO.addStandardClass(Outfile);  // Declared in BASICIO
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"open",parameter("fileimage",Type.Text));  
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"close");  
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outimage");  
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outrecord");  
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,null,"breakoutimage");  
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"checkpoint");  
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outchar",parameter("c",Type.Character));  
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outtext",parameter("t",Type.Text));  
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"FIELD_",parameter("w",Type.Integer));  
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outint",parameter("i",Type.Integer),parameter("w",Type.Integer));  
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outfrac",parameter("i",Type.Integer),parameter("n",Type.Integer),parameter("w",Type.Integer)); 
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,mtd,null,"outfix", parameter("r",new OverLoad(Type.Real,Type.LongReal)),parameter("n",Type.Integer),parameter("w",Type.Integer)); 
		Outfile.addStandardProcedure(ObjectKind.MemberMethod,mtd,null,"outreal",parameter("r",new OverLoad(Type.Real,Type.LongReal)),parameter("n",Type.Integer),parameter("w",Type.Integer)); 
	}  

	// ******************************************************************
	// *** The Standard Imagefile Class Directfile
	// ******************************************************************
	/// Initiate the Standard Class Directfile.
	/// <pre>
	///  Imagefile class Directfile;
	///  begin   integer LOC, MAXLOC;  Boolean ENDFILE, LOCKED;
	///     integer procedure location;  location:= LOC;
	///     Boolean procedure endfile;   endfile := ENDFILE;
	///     Boolean procedure locked;    locked  := LOCKED;
	///     Boolean procedure open(fileimage); text fileimage; 
	///     Boolean procedure close;
	///     integer procedure lastloc;
	///     integer procedure maxloc;
	///     procedure locate(i); integer i;
	///     procedure inimage;
	///     procedure outimage;
	///     Boolean procedure deleteimage;
	///     character procedure inchar;
	///     integer procedure lock(t,i,j); real t; integer i,j;
	///     Boolean procedure unlock; 
	///     Boolean procedure checkpoint;
	///     Boolean procedure lastitem;
	///     text procedure intext;
	///     integer procedure inint;
	///     long real procedure inreal;
	///     integer procedure infrac;
	///     procedure outchar(c); character c;
	///     procedure outtext(t); text t;
	///     text procedure FIELD(w); integer w;
	///     procedure outint(i,w); integer i,w;
	///     procedure outfix(r,n,w);  long real r; integer n,w;
	///     procedure outreal(r,n,w); long real r; integer n,w;
	///     procedure outfrac(i,n,w); integer i,n,w;
	/// 
	///     ENDFILE:= true
	///     ...
	///  end Directfile;
	/// </pre>
	private static void initDirectfile() {
		String[] mtd = { "(FII)V", "(DII)V" };

		StandardClass Directfile=new StandardClass("Imagefile","Directfile");
		BASICIO.addStandardClass(Directfile);  // Declared in BASICIO
		Directfile.addStandardAttribute(Type.Integer,"LOC_");  
		Directfile.addStandardAttribute(Type.Integer,"MAXLOC_");  
		Directfile.addStandardAttribute(Type.Boolean,"ENDFILE_");  
		Directfile.addStandardAttribute(Type.Boolean,"LOCKED_");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"location");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"endfile");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"locked");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"open",parameter("fileimage",Type.Text));  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"close");      
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"lastloc");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"maxloc");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,null,"locate",parameter("i",Type.Integer));  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,null,"inimage");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outimage");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"deleteimage");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Character,"inchar");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"lock",parameter("t",Type.Real),parameter("i",Type.Integer),parameter("j",Type.Integer));  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"unlock");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"checkpoint");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"lastitem");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"intext",parameter("w",Type.Integer));  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"inint");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.LongReal,"inreal");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"infrac");  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outchar",parameter("c",Type.Character));  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outtext",parameter("t",Type.Text));   
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"FIELD_",parameter("w",Type.Integer));  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outint",parameter("i",Type.Integer),parameter("w",Type.Integer));  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outfrac",parameter("i",Type.Integer),parameter("n",Type.Integer),parameter("w",Type.Integer));  
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,mtd,null,"outfix", parameter("r",new OverLoad(Type.Real,Type.LongReal)),parameter("n",Type.Integer),parameter("w",Type.Integer)); 
		Directfile.addStandardProcedure(ObjectKind.MemberMethod,mtd,null,"outreal",parameter("r",new OverLoad(Type.Real,Type.LongReal)),parameter("n",Type.Integer),parameter("w",Type.Integer)); 
	}  

	// ******************************************************************
	// *** The Standard Outfile Class Printfile
	// ******************************************************************
	/// Initiate the Standard Class Printfile.
	/// <pre>
	///  Outfile class Printfile;
	///  begin integer LINE, LINES_PER_PAGE, SPACING, PAGE;
	///    integer procedure line; line := LINE;
	///    integer procedure page; page := PAGE;
	///    Boolean procedure open(fileimage); text fileimage; 
	///    Boolean procedure close; 
	///    integer procedure linesperpage(n); integer n; 
	///    procedure spacing(n); integer n; 
	///    procedure eject(n);  integer n; 
	///    procedure outimage;
	///    procedure outrecord;
	/// 
	///    SPACING := 1;
	///    LINES_PER_PAGE := ... ;
	///    ...
	///  end Printfile;
	/// </pre>
	private static void initPrintfile() {
		Printfile=new StandardClass("Outfile","Printfile");
		BASICIO.addStandardClass(Printfile);  // Declared in BASICIO
		Printfile.addStandardAttribute(Type.Integer,"LINE_");  
		Printfile.addStandardAttribute(Type.Integer,"LINES_PER_PAGE_");  
		Printfile.addStandardAttribute(Type.Integer,"SPACING_");  
		Printfile.addStandardAttribute(Type.Integer,"PAGE_");  
		Printfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"line"); 
		Printfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"page");  
		Printfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"open",parameter("fileimage",Type.Text));  
		Printfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"close");  
		Printfile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"linesperpage",parameter("n",Type.Integer));  
		Printfile.addStandardProcedure(ObjectKind.MemberMethod,null,"spacing",parameter("n",Type.Integer));  
		Printfile.addStandardProcedure(ObjectKind.MemberMethod,null,"eject",parameter("n",Type.Integer));  
		Printfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outimage");  
		Printfile.addStandardProcedure(ObjectKind.MemberMethod,null,"outrecord");  
	}  

	// ******************************************************************
	// *** The Standard file Class Bytefile
	// ******************************************************************
	/// The Standard Class Bytefile.
	private static StandardClass Bytefile;

	/// Initiate the Standard Class Bytefile.
	/// <pre>
	///  file class Bytefile;
	///  begin short integer BYTESIZE;
	///     short integer procedure bytesize; bytesize := BYTESIZE;
	/// 
	///  end Bytefile;
	/// </pre>
	private static void initBytefile() { 
		Bytefile=new StandardClass("File","Bytefile");
		BASICIO.addStandardClass(Bytefile);  // Declared in BASICIO
		Bytefile.addStandardAttribute(Type.Integer,"BYTESIZE_");  
		Bytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"bytesize");  
	}  

	// ******************************************************************
	// *** The Standard Bytefile Class Inbytefile
	// ******************************************************************
	/// Initiate the Standard Class Inbytefile.
	/// <pre>
	///  Bytefile class Inbytefile;
	///  begin Boolean ENDFILE;
	///    Boolean procedure endfile; endfile:= ENDFILE;
	///    Boolean procedure open; 
	///    Boolean procedure close;
	///    short integer procedure inbyte;
	///    text procedure intext(t); text t; 
	/// 
	///    ENDFILE:= true;
	///    ...
	///  end Inbytefile;
	/// </pre>
	private static void initInbytefile() { 
		StandardClass Inbytefile=new StandardClass("Bytefile","Inbytefile");
		BASICIO.addStandardClass(Inbytefile);  // Declared in BASICIO
		Inbytefile.addStandardAttribute(Type.Boolean,"ENDFILE_");  
		Inbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"endfile");  
		Inbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"open");
		Inbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"close");  
		Inbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"inbyte");  
		Inbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"intext",parameter("t",Type.Text));
		if(Option.EXTENSIONS) {
			Inbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"in2byte");  // Extension to Simula Standard
		}
	}  

	// ******************************************************************
	// *** The Standard Bytefile Class Outbytefile
	// ******************************************************************
	/// Initiate the Standard Class Outbytefile.
	/// <pre>
	///  Bytefile class Outbytefile;
	///  begin
	///    Boolean procedure open; 
	///    Boolean procedure close; 
	///    procedure outbyte(x); short integer x; 
	///    procedure outtext(t); text t; 
	///    Boolean procedure checkpoint; 
	/// 
	///  end Outbytefile;
	/// </pre>
	private static void initOutbytefile() { 
		StandardClass Outbytefile=new StandardClass("Bytefile","Outbytefile");
		BASICIO.addStandardClass(Outbytefile);
		Outbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"open");
		Outbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"close");  
		Outbytefile.addStandardProcedure(ObjectKind.MemberMethod,null,"outbyte",parameter("x",Type.Integer));   
		Outbytefile.addStandardProcedure(ObjectKind.MemberMethod,null,"outtext",parameter("t",Type.Text));  
		Outbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"checkpoint");  
		if(Option.EXTENSIONS) {
			Outbytefile.addStandardProcedure(ObjectKind.MemberMethod,null,"out2byte",parameter("x",Type.Integer));   			
		}
	}  

	// ******************************************************************
	// *** The Standard Bytefile Class Directbytefile
	// ******************************************************************
	/// Initiate the Standard Class Directbytefile.
	/// <pre>
	///  Bytefile class Directbytefile;
	///  begin integer LOC, MAXLOC;  Boolean LOCKED;
	///    Boolean procedure endfile; endfile:=OPEN and then LOC>lastloc;
	///    integer procedure location; location := LOC;
	///    integer procedure maxloc; maxloc := MAXLOC;
	///    Boolean procedure locked; locked := LOCKED;
	///    Boolean procedure open; 
	///    Boolean procedure close; 
	///    integer procedure lastloc; 
	///    procedure locate(i); integer i; 
	///    short integer procedure inbyte; 
	///    procedure outbyte(x); short integer x; 
	///    Boolean procedure checkpoint; 
	///    integer procedure lock(t,i,j); real t; integer i,j; 
	///    Boolean procedure unlock; 
	///    procedure intext(t); text t; 
	///    procedure outtext(t); text t;
	///     ...
	///  end Directbytefile;
	/// </pre>
	private static void initDirectbytefile() { 
		StandardClass Directbytefile=new StandardClass("Bytefile","Directbytefile");
		BASICIO.addStandardClass(Directbytefile);  // Declared in BASICIO
		Directbytefile.addStandardAttribute(Type.Integer,"LOC_");  
		Directbytefile.addStandardAttribute(Type.Integer,"MAXLOC_");  
		Directbytefile.addStandardAttribute(Type.Boolean,"LOCKED_");  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"endfile");  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"location");  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"maxloc");  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"locked");  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"open");  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"close");      
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"lastloc");  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,null,"locate",parameter("i",Type.Integer));  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"inbyte");  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,null,"outbyte",parameter("x",Type.Integer));   
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,null,"out2byte",parameter("x",Type.Integer));   
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"checkpoint");  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"lock",parameter("t",Type.Real),parameter("i",Type.Integer),parameter("j",Type.Integer));  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"unlock");  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,Type.Text,"intext",parameter("t",Type.Text));  
		Directbytefile.addStandardProcedure(ObjectKind.MemberMethod,null,"outtext",parameter("t",Type.Text));  
	}  

	// ******************************************************************
	// *** The Standard Class Simset
	// ******************************************************************
	/// The Standard Class Simset.
	private static StandardClass Simset;

	/// Initiate the Standard Class Simset.
	private static void initSimset() { 
		Simset=new StandardClass("CLASS","Simset");
		ENVIRONMENT.addStandardClass(Simset);  // Declared in ENVIRONMENT
	}  

	// ******************************************************************
	// *** The Standard Class Linkage
	// ******************************************************************
	/// The Standard Class Linkage.
	private static StandardClass Linkage;

	/// Initiate the Standard Class Linkage.
	private static void initLinkage() { 
		Linkage=new StandardClass("CLASS","Linkage");
		Simset.addStandardClass(Linkage);  // Declared in Simset
		Linkage.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("Link"),"suc");  
		Linkage.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("Link"),"pred");  
		Linkage.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("Linkage"),"prev");  
	}  

	// ******************************************************************
	// *** The Standard Linkage Class Head
	// ******************************************************************
	/// Initiate the Standard Class Head.
	private static void initHead() {
		StandardClass Head=new StandardClass("Linkage","Head");
		Simset.addStandardClass(Head);  // Declared in Simset
		Head.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("Link"),"first");  
		Head.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("Link"),"last");  
		Head.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"empty");  
		Head.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"cardinal");  
		Head.addStandardProcedure(ObjectKind.MemberMethod,null,"clear");  
	}  

	// ******************************************************************
	// *** The Standard Linkage Class Link
	// ******************************************************************
	/// Initiate the Standard Class Link.
	private static void initLink() { 
		StandardClass Link=new StandardClass("Linkage","Link");
		Simset.addStandardClass(Link);  // Declared in Simset
		Link.addStandardProcedure(ObjectKind.MemberMethod,null,"out");  
		Link.addStandardProcedure(ObjectKind.MemberMethod,null,"follow",parameter("X",Type.Ref("Linkage")));  
		Link.addStandardProcedure(ObjectKind.MemberMethod,null,"precede",parameter("X",Type.Ref("Linkage")));  
		Link.addStandardProcedure(ObjectKind.MemberMethod,null,"into",parameter("S",Type.Ref("Head")));  
	}  

	// ******************************************************************
	// *** The Standard Class Simulation
	// ******************************************************************
	/// The Standard Class Simulation.
	private static StandardClass Simulation;	

	/// Initiate the Standard Class Simulation.
	private static void initSimulation() { 
		Simulation=new StandardClass("Simset","Simulation");
		ENVIRONMENT.addStandardClass(Simulation);  // Declared in ENVIRONMENT
		Simulation.detachUsed=true;
		Simulation.addStandardAttribute(Type.Ref("Head"),"SQS");  
		Simulation.addStandardAttribute(Type.Ref("MAIN_PROGRAM"),"main");  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,Type.LongReal,"time");  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("EVENT_NOTICE"),"FIRSTEV");  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("Process"),"current");  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,null,"hold",parameter("T",Type.LongReal));  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,null,"passivate");  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,null,"wait",parameter("S",Type.Ref("Head")));  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,null,"cancel",parameter("x",Type.Ref("Process")));  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,null,"accum",parameter("a",Parameter.Mode.name,Type.LongReal),parameter("b",Parameter.Mode.name,Type.LongReal)
				,parameter("c",Parameter.Mode.name,Type.LongReal),parameter("d",Type.LongReal));    
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,null,"ActivateDirect"
				,parameter("REAC",Type.Boolean)
				,parameter("X",Type.Ref("Process"))
				);  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,null,"ActivateAt"
				,parameter("REAC",Type.Boolean)
				,parameter("X",Type.Ref("Process"))
				,parameter("T",Type.LongReal)
				,parameter("PRIO",Type.Boolean)
				);  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,null,"ActivateDelay"
				,parameter("REAC",Type.Boolean)
				,parameter("X",Type.Ref("Process"))
				,parameter("T",Type.LongReal)
				,parameter("PRIO",Type.Boolean)
				);  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,null,"ActivateBefore"
				,parameter("REAC",Type.Boolean)
				,parameter("X",Type.Ref("Process"))
				,parameter("Y",Type.Ref("Process"))
				);  
		Simulation.addStandardProcedure(ObjectKind.MemberMethod,null,"ActivateAfter"
				,parameter("REAC",Type.Boolean)
				,parameter("X",Type.Ref("Process"))
				,parameter("Y",Type.Ref("Process"))
				);  
	}  

	// ******************************************************************
	// *** The Standard Link Class EVENT_NOTICE
	// ******************************************************************
	/// Initiate the Standard Class EVENT_NOTICE.
	private static void initEVENT_NOTICE() { 
		StandardClass EVENT_NOTICE=new StandardClass("Link","EVENT_NOTICE");
		Simulation.addStandardClass(EVENT_NOTICE);  // Declared in Simulation
		//	    ref(EVENT_NOTICE) procedure suc;
		//	    ref(EVENT_NOTICE) procedure pred;
		//	    procedure RANK(BEFORE_); Boolean BEFORE_;
		EVENT_NOTICE.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("EVENT_NOTICE"),"suc");  
		EVENT_NOTICE.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("EVENT_NOTICE"),"pred");  
		EVENT_NOTICE.addStandardProcedure(ObjectKind.MemberMethod,null,"RANK",parameter("BEFORET",Type.Boolean));  
	}  

	// ******************************************************************
	// *** The Standard Link Class Process
	// ******************************************************************
	/// The Standard Class Process.
	private static StandardClass Process;

	/// Initiate the Standard Class Process.
	private static void initProcess() { 
		Process=new StandardClass("Link","Process");
		Simulation.addStandardClass(Process);  // Declared in Simulation
		Process.detachUsed=true;
		Process.statements1=new ObjectList<Statement>();
		Process.statements1.add(new InlineStatement("detach")); // Statements before inner 
		Process.statements.add(new InlineStatement("terminate")); // Statements after inner 				
		//	    ref(EVENT_NOTICE) EVENT;
		//	    Boolean TERMINATED_;
		//	    Boolean procedure idle;
		//	    Boolean procedure terminated;
		//	    real procedure evtime;
		//	    ref(Process) procedure nextev;
		Process.addStandardAttribute(Type.Ref("EVENT_NOTICE"),"EVENT");  
		Process.addStandardAttribute(Type.Boolean,"TERMINATED_");  
		Process.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"idle");  
		Process.addStandardProcedure(ObjectKind.MemberMethod,Type.Boolean,"terminated");  
		Process.addStandardProcedure(ObjectKind.MemberMethod,Type.LongReal,"evtime");  
		Process.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("Process"),"nextev");  
	}  


	// ******************************************************************
	// *** The Standard Process Class MAIN_PROGRAM
	// ******************************************************************
	/// Initiate the Standard Class MAIN_PROGRAM.
	private static void initMAIN_PROGRAM() { 
		StandardClass MAIN_PROGRAM=new StandardClass("Process","MAIN_PROGRAM");
		Simulation.addStandardClass(MAIN_PROGRAM);   // Declared in Simulation
		//	    Process class MAIN_PROGRAM;
		//	    begin
		//	       L: detach; goto L
		//	    end MAIN_PROGRAM;
	}  


	// ******************************************************************
	// *** The Standard Class CatchingErrors  NOTE: if(Option.EXTENSIONS)
	// ******************************************************************
	/// Initiate the Standard Class CatchingErrors.
	private static void initCatchingErrors() { 
		CatchingErrors=new StandardClass("CLASS","CatchingErrors");
		ENVIRONMENT.addStandardClass(CatchingErrors);  // Declared in ENVIRONMENT
		CatchingErrors.virtualSpecList.add(new VirtualSpecification("onError",null,VirtualSpecification.Kind.Procedure,CatchingErrors.prefixLevel(),null));
		CatchingErrors.statements1=new ObjectList<Statement>();
		CatchingErrors.statements1.add(new InlineStatement("try")); // Statements before inner 
		CatchingErrors.statements.add(new InlineStatement("catch")); // Statements after inner 				
	}  

	
	// ******************************************************************
	// *** The Standard Class DEC_Lib   - as defined in DEC handbook III    NOTE: if(Option.EXTENSIONS)
	// ******************************************************************
	/// Initiate the Standard Class DEC_Lib.
	private static void initDEC_Lib() { 
		StandardClass DEC_Lib=new StandardClass("CLASS","DEC_Lib");
		ENVIRONMENT.addStandardClass(DEC_Lib);  // Declared in ENVIRONMENT.
		DEC_Lib.isContextFree=true; // This class is a Context i.e. all members are static
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"abort",parameter("mess",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Boolean,"change",parameter("m",Parameter.Mode.name,Type.Text),parameter("o",Type.Text),parameter("n",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"checkextension",parameter("fileName",Type.Text),parameter("defaultextension",Parameter.Mode.value,Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"checkfrac",parameter("t",Parameter.Mode.name,Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"checkint",parameter("t",Parameter.Mode.name,Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"checkreal",parameter("t",Parameter.Mode.name,Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"compress",parameter("t",Type.Text),parameter("c",Type.Character));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"conc",parameter("t1",Type.Text),parameter("t2",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"conc2",parameter("t1",Type.Text),parameter("t2",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"conc3",parameter("t1",Type.Text),parameter("t2",Type.Text),parameter("t3",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"conc4",parameter("t1",Type.Text),parameter("t2",Type.Text),parameter("t3",Type.Text),parameter("t4",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"conc5",parameter("t1",Type.Text),parameter("t2",Type.Text),parameter("t3",Type.Text),parameter("t4",Type.Text),parameter("t5",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"cptime");  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"dayno");  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"daytime");  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"depchar",parameter("t",Type.Text),parameter("p",Type.Integer),parameter("c",Type.Character));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"enterdebug",parameter("maycontinue",Type.Boolean));
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,null,"exit",parameter("code",Type.Integer));
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"fetchar",parameter("t",Type.Text),parameter("p",Type.Integer));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"findtrigger",parameter("master",Parameter.Mode.name,Type.Text),parameter("triggers",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"from",parameter("t",Type.Text),parameter("p",Type.Integer));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"front",parameter("t",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Boolean,"frontcompare",parameter("string",Type.Text),parameter("config",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"frontstrip",parameter("t",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"getitem",parameter("tt",Parameter.Mode.name,Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"hash",parameter("t",Type.Text),parameter("n",Type.Integer));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"initem",parameter("f",Type.Ref("File")));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"linecount",parameter("pf",Type.Ref("Printfile")));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"insinglechar");  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"lowc",parameter("c",Type.Character));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"maketext",parameter("c",Type.Character),parameter("n",Type.Integer));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Boolean,"puttext",parameter("ot",Parameter.Mode.name,Type.Text),parameter("nt",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"rest",parameter("t",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"scanchar",parameter("t",Parameter.Mode.name,Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"scanfrac",parameter("tt",Parameter.Mode.name,Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"scanint",parameter("tt",Parameter.Mode.name,Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.LongReal,"scanreal",parameter("tt",Parameter.Mode.name,Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"scanto",parameter("t",Parameter.Mode.name,Type.Text),parameter("c",Type.Character));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"search",parameter("t1",Type.Text),parameter("t2",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"skip",parameter("t",Parameter.Mode.name,Type.Text),parameter("c",Type.Character));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Integer,"startpos",parameter("t",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"today");  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"tsub",parameter("t",Type.Text),parameter("p",Type.Integer),parameter("l",Type.Integer));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Character,"upc",parameter("c",Type.Character));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Boolean,"upcompare",parameter("master",Type.Text),parameter("test",Type.Text));  
		DEC_Lib.addStandardProcedure(ObjectKind.ContextFreeMethod,Type.Text,"upto",parameter("t",Type.Text),parameter("p",Type.Integer));  
	}  

	// ******************************************************************
	// *** The Standard Class Drawing    NOTE: if(Option.EXTENSIONS)
	// ******************************************************************
	/// The Standard Class Drawing.
	private static StandardClass Drawing;

	/// Initiate the Standard Class Drawing.
	private static void initDrawing() {
		Drawing=new StandardClass("Simset","Drawing",parameter("Title",Type.Text),parameter("width",Type.Integer),parameter("height",Type.Integer)); 
		ENVIRONMENT.addStandardClass(Drawing);  // Declared in ENVIRONMENT
		Drawing.addStandardAttribute(Type.Integer,"white",    0xffffff); // Color white:      R=255, G=255, B=255.
		Drawing.addStandardAttribute(Type.Integer,"lightGray",0xc0c0c0); // Color light gray: R=192, G=192, B=192.  
		Drawing.addStandardAttribute(Type.Integer,"gray",     0x808080); // Color gray:       R=128, G=128, B=128. 
		Drawing.addStandardAttribute(Type.Integer,"darkGray", 0x404040); // Color dark gray:  R=64,  G=64,  B=64.
		Drawing.addStandardAttribute(Type.Integer,"black",    0x000000); // Color black:      R=0,   G=0,   B=0.  
		Drawing.addStandardAttribute(Type.Integer,"red",      0xff0000); // Color red:        R=255, G=0,   B=0. 
		Drawing.addStandardAttribute(Type.Integer,"pink",     0xffafaf); // Color pink:       R=255, G=175, B=175.
		Drawing.addStandardAttribute(Type.Integer,"orange",   0xffc800); // Color orange:     R=255, G=200, B=0. 
		Drawing.addStandardAttribute(Type.Integer,"yellow",   0xffff00); // Color yellow:     R=255, G=255, B=0. 
		Drawing.addStandardAttribute(Type.Integer,"green",    0x00ff00); // Color green:      R=0,   G=255, B=0.
		Drawing.addStandardAttribute(Type.Integer,"magenta",  0xff00ff); // Color magenta:    R=255, G=0,   B=255.
		Drawing.addStandardAttribute(Type.Integer,"cyan",     0x00ffff); // Color cyan:       R=0,   G=255, B=255.
		Drawing.addStandardAttribute(Type.Integer,"blue",     0x0000ff); // Color blue:       R=0,   G=0,   B=255.
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Integer,"color",parameter("r",Type.Integer),parameter("g",Type.Integer),parameter("b",Type.Integer));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,null,"setBackgroundColor",parameter("color",Type.Integer));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,null,"setDrawColor",parameter("color",Type.Integer));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,null,"setFillColor",parameter("color",Type.Integer));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,null,"setStroke",parameter("width",Type.Real));  

		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("Head"),"renderingSet");  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,null,"setFontStylePlain");  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,null,"setFontStyleBold");  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,null,"setFontStyleItalic");  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,null,"setFontStyleBoldItalic");  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,null,"setFontSize",parameter("size",Type.Real));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Real,"getFontSize");  

		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("TextElement"),"drawText",parameter("t",Type.Text),parameter("x",Type.LongReal),parameter("y",Type.LongReal));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("ShapeElement"),"drawLine",parameter("x1",Type.LongReal),parameter("y1",Type.LongReal),parameter("x2",Type.LongReal),parameter("y2",Type.LongReal));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("ShapeElement"),"drawEllipse",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal),parameter("height",Type.LongReal));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("ShapeElement"),"drawRectangle",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal),parameter("height",Type.LongReal));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("ShapeElement"),"drawRoundRectangle",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal)
				,parameter("height",Type.LongReal),parameter("arcw",Type.LongReal),parameter("arch",Type.LongReal));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("ShapeElement"),"fillEllipse",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal),parameter("height",Type.LongReal));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("ShapeElement"),"fillRectangle",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal),parameter("height",Type.LongReal));  
		Drawing.addStandardProcedure(ObjectKind.MemberMethod,Type.Ref("ShapeElement"),"fillRoundRectangle",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal)
				,parameter("height",Type.LongReal),parameter("arcw",Type.LongReal),parameter("arch",Type.LongReal));  
	}

	// ******************************************************************
	// *** The Standard Link Class ShapeElement    NOTE: if(Option.EXTENSIONS)
	// ******************************************************************
	/// Initiate the Standard Class ShapeElement.
	private static void initShapeElement() {
		StandardClass ShapeElement=new StandardClass("Link","ShapeElement");
		Drawing.addStandardClass(ShapeElement);  // Declared in Drawing
		ShapeElement.addStandardProcedure(ObjectKind.MemberMethod,null,"setColor",parameter("color",Type.Integer));  
		ShapeElement.addStandardProcedure(ObjectKind.MemberMethod,null,"drawLine",parameter("x1",Type.LongReal),parameter("y1",Type.LongReal),parameter("x2",Type.LongReal),parameter("y2",Type.LongReal));  
		ShapeElement.addStandardProcedure(ObjectKind.MemberMethod,null,"drawEllipse",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal),parameter("height",Type.LongReal));  
		ShapeElement.addStandardProcedure(ObjectKind.MemberMethod,null,"drawRectangle",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal),parameter("height",Type.LongReal));  
		ShapeElement.addStandardProcedure(ObjectKind.MemberMethod,null,"drawRoundRectangle",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal)
				,parameter("height",Type.LongReal),parameter("arcw",Type.LongReal),parameter("arch",Type.LongReal));  
		ShapeElement.addStandardProcedure(ObjectKind.MemberMethod,null,"fillEllipse",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal),parameter("height",Type.LongReal));  
		ShapeElement.addStandardProcedure(ObjectKind.MemberMethod,null,"fillRectangle",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal),parameter("height",Type.LongReal));  
		ShapeElement.addStandardProcedure(ObjectKind.MemberMethod,null,"fillRoundRectangle",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("width",Type.LongReal)
				,parameter("height",Type.LongReal),parameter("arcw",Type.LongReal),parameter("arch",Type.LongReal));  
		ShapeElement.addStandardProcedure(ObjectKind.MemberMethod,null,"instantMoveTo",parameter("x",Type.LongReal),parameter("y",Type.LongReal));  
		ShapeElement.addStandardProcedure(ObjectKind.MemberMethod,null,"moveTo",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("speed",Type.LongReal));  
	}

	// ******************************************************************
	// *** The Standard Link Class TextElement    NOTE: if(Option.EXTENSIONS)
	// ******************************************************************
	/// Initiate the Standard Class TextElement.
	private static void initTextElement() {
		StandardClass TextElement=new StandardClass("Link","TextElement",parameter("txt",Type.Text),parameter("x",Type.LongReal),parameter("y",Type.LongReal));  
		Drawing.addStandardClass(TextElement);  // Declared in Drawing
		TextElement.addStandardProcedure(ObjectKind.MemberMethod,null,"setColor",parameter("color",Type.Integer));  
		TextElement.addStandardProcedure(ObjectKind.MemberMethod,null,"setStroke",parameter("width",Type.Real));  
		TextElement.addStandardProcedure(ObjectKind.MemberMethod,null,"setFontStylePlain");  
		TextElement.addStandardProcedure(ObjectKind.MemberMethod,null,"setFontStyleBold");  
		TextElement.addStandardProcedure(ObjectKind.MemberMethod,null,"setFontStyleItalic");  
		TextElement.addStandardProcedure(ObjectKind.MemberMethod,null,"setFontStyleBoldItalic");  
		TextElement.addStandardProcedure(ObjectKind.MemberMethod,null,"setFontSize",parameter("size",Type.Real));  
		TextElement.addStandardProcedure(ObjectKind.MemberMethod,Type.Real,"getFontSize");  

		TextElement.addStandardProcedure(ObjectKind.MemberMethod,null,"setText",parameter("t",Type.Text));  
		TextElement.addStandardProcedure(ObjectKind.MemberMethod,null,"instantMoveTo",parameter("x",Type.LongReal),parameter("y",Type.LongReal));  
		TextElement.addStandardProcedure(ObjectKind.MemberMethod,null,"moveTo",parameter("x",Type.LongReal),parameter("y",Type.LongReal),parameter("speed",Type.LongReal));  
	}





	// ******************************************************************
	// *** Constructors
	// ******************************************************************

	/// Create a new StandardClass.
	/// @param className the class's name
	private StandardClass(String className) {
		super(className);
		this.externalIdent = "RTS_"+className;
		this.declarationKind=ObjectKind.StandardClass;
		this.type=Type.Ref(className);
	}

	/// Create a new StandardClass.
	/// @param prefix prefix class-identifier
	/// @param className the class's name
	private StandardClass(String prefix,String className) {
		this(className);
		this.prefix=prefix;
		if(Option.compilerMode == Option.CompilerMode.simulaClassLoader) {
			ClassDesc CD_ThisClass = ClassDesc.of("simula.runtime.RTS_" + className); 
			ClassDesc CD_SuperClass = ClassDesc.of("simula.runtime.RTS_" + prefix); 
			ClassHierarchy.addClassToSuperClass(CD_ThisClass, CD_SuperClass);
		}
	}

	/// Create a new StandardClass.
	/// @param prefix prefix class-identifier
	/// @param className the class's name
	/// @param param the parameters
	private StandardClass(String prefix,String className,Parameter... param) {
		this(prefix,className);
		for(int i=0;i<param.length;i++) param[i].into(parameterList);
	}
	
	@Override
	public int getRTBlockLevel() {
		return 0;
	}

	// ******************************************************************
	// *** Lookup Meaning
	// ******************************************************************

	@Override
	public Meaning findVisibleAttributeMeaning(String ident) {
		if(Option.internal.TRACE_FIND_MEANING>0) Util.println("BEGIN Checking Standard Class "+identifier+" for "+ident+" ================================== "+identifier+" ==================================");
		for(Declaration declaration:declarationList) {
			if(Option.internal.TRACE_FIND_MEANING>1) Util.println("Checking Local "+declaration.identifier);
			if(Util.equals(ident, declaration.identifier)) {
				return(new Meaning(declaration,this));
			}
		}
		if(Option.internal.TRACE_FIND_MEANING>0) Util.println("ENDOF Checking Standard Class "+identifier+" for "+ident+" ================================== "+identifier+" ==================================");
		if(prefix != null) {
			ClassDeclaration prfx=getPrefixClass();
			if(prfx!=null) return(prfx.findVisibleAttributeMeaning(ident));
		}
		return(null);
	}

	@Override
	public Meaning findRemoteAttributeMeaning(String ident) {
		for(Declaration declaration:declarationList)
			if(Util.equals(ident, declaration.identifier))
				return(new Meaning(declaration,this));
		ClassDeclaration prfx=getPrefixClass();
		if(prfx!=null) return(prfx.findRemoteAttributeMeaning(ident));
		return(null);
	}

	// ******************************************************************
	// *** Parameter Specifications
	// ******************************************************************

	/// Create a new Parameter.
	/// @param ident the identifier
	/// @param type  the type
	/// @return the newly created Parameter
	private static Parameter parameter(String ident,Type type)	{
		return(new Parameter(ident,type,Parameter.Kind.Simple)); }

	/// Create a new Parameter.
	/// @param ident the identifier
	/// @param type  the type
	/// @param kind  the parameter kind
	/// @return the newly created Parameter
	private static Parameter parameter(String ident,Type type,int kind)	{
		return(new Parameter(ident,type,kind)); }

	/// Create a new Parameter.
	/// @param ident the identifier
	/// @param type  the type
	/// @param kind  the parameter kind
	/// @param nDim  number of dimensions for arrays
	/// @return the newly created Parameter
	private static Parameter parameter(String ident,Type type,int kind,int nDim)	{
		return(new Parameter(ident,type,kind,nDim)); }

	/// Create a new Parameter.
	/// @param ident the identifier
	/// @param mode  the mode
	/// @param type  the type
	/// @return the newly created Parameter
	private static Parameter parameter(String ident,int mode,Type type) {
		Parameter spec=new Parameter(ident,type,Parameter.Kind.Simple);
		spec.setMode(mode); return(spec);
	}

	/// Create a new Parameter.
	/// @param ident the identifier
	/// @param kind  the parameter kind
	/// @param mode  the mode
	/// @param type  the type
	/// @return the newly created Parameter
	private static Parameter parameter(String ident,int kind, int mode,Type type) {
		Parameter spec=new Parameter(ident,type,kind);
		spec.setMode(mode); return(spec);
	}


	// ******************************************************************
	// *** Add Class / Attribute / Procedure
	// ******************************************************************

	/// Add a StandardClass.
	/// @param standardClass the StandardClass
	private void addStandardClass(StandardClass standardClass) {
		standardClass.declaredIn = this;
		((ClassDeclaration)standardClass.declaredIn).hasLocalClasses=true;
		declarationList.add(standardClass);
	}

	/// Create and add a new standard attribute.
	/// @param type the attribute type
	/// @param ident the attribute identifier
	private void addStandardAttribute(Type type,String ident) {
		declarationList.add(new SimpleVariableDeclaration(type,ident)); }

	/// Create and add a new constant standard attribute.
	/// @param type the attribute type
	/// @param ident the attribute identifier
	/// @param value the constant integer value
	private void addStandardAttribute(Type type,String ident,Number value) {
		declarationList.add(new SimpleVariableDeclaration(type,ident,true,new Constant(type,value))); }

	/// Create and add a new StandardProcedure.
	/// @param kind the declaration kind
	/// @param type the procedure's type
	/// @param ident the procedure identifier
	private void addStandardProcedure(int kind,Type type,String ident) {
		declarationList.add(new StandardProcedure(this,kind,type,ident)); }

	/// Create and add a new StandardProcedure.
	/// @param kind the declaration kind
	/// @param type the procedure's type
	/// @param ident the procedure identifier
	/// @param param the parameters
	private void addStandardProcedure(int kind,Type type,String ident,Parameter... param) {
		declarationList.add(new StandardProcedure(this,kind,type,ident,param)); }

	/// Create and add a new StandardProcedure.
	/// @param kind the declaration kind
	/// @param mtdSet the set of Method Type Descriptors
	/// @param type the procedure's type
	/// @param ident the procedure identifier
	/// @param param the parameters
	private void addStandardProcedure(int kind,String[] mtdSet,Type type,String ident,Parameter... param) {
		declarationList.add(new StandardProcedure(this,kind,mtdSet,type,ident,param)); }

	// ***********************************************************************************************
	// *** ClassFile coding Utility: getClassDesc   -- Defined in DeclarationScope
	// ***********************************************************************************************
	@Override
	public ClassDesc getClassDesc() {
		return(ClassDesc.of("simula.runtime." + this.externalIdent));
	}

	// ***********************************************************************************************
	// *** Externalization
	// ***********************************************************************************************
	/// Default constructor used by Attribute File I/O
	public StandardClass() {
		super(null);
	}


}


