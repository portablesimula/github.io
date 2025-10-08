/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import java.util.jar.JarFile;

import javax.swing.ImageIcon;

import simula.compiler.JavaSourceFileCoder;
import simula.compiler.JarFileBuilder;
import simula.compiler.syntaxClass.declaration.DeclarationScope;
import simula.compiler.syntaxClass.declaration.StandardClass;
import simula.compiler.syntaxClass.statement.ProgramModule;
import simula.editor.RTOption;

/// Global Variables.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/Global.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class Global {
	/// The Simula release identification.
	/// 
	/// NOTE: When updating release id, change version in SimulaExtractor and RuntimeSystem
	public static final String simulaReleaseID = "Simula-2.0";
	
	/// A Simula icon
	public static ImageIcon simIcon;
	/// A small Simula icon
	static ImageIcon sIcon;

	/// The Simula Home directory.
	public static File simulaHome;

	/// The Simula Release Home directory.
	public static File releaseHome;
	
	/// The Simula Version
	public static String simulaVersion;
	
	/// The set of external .jar files.
	public static Vector<File> externalJarFiles;
	
	/// The current Charset.
	public static Charset _CHARSET = Charset.defaultCharset();

	/// The current source line number.
	public static int sourceLineNumber;
	
	/// The source file's directory.
	public static File sourceFileDir;
	
	/// The source file name.
	public static String sourceFileName;
	
	/// The source file name without .sim
	public static String sourceName;
	
	/// The insert name.
	public static String insertName;
	
	/// The programModule.
	public static ProgramModule programModule;
	
	/// Where to find the Simula Runtime System.
	public static File simulaRtsLib; // The simula runtime system

	/// The Simula properties file
	public static File simulaPropertiesFile;
	
	/// The Simula properties
	private static Properties simulaProperties;
	
	/// The sample source directory. Where to find sample Simula files
	public static File sampleSourceDir;
	
	/// Current workspace. Where to find .sim source files
	public static File currentWorkspace;

	/// The set of workspaces
	public static ArrayDeque<File> workspaces;
	
	/// The output directory. Used by Java-Coding to save the generated .jar files.
	public static File outputDir;
	
	/// The external library. Used by ExternalDeclaration.readAttributeFile
	public static File extLib;

	/// Compiler state: True while Parsing
	public static boolean duringParsing;

	/// Compiler state: True while Checking
	public static boolean duringChecking;

	/// Compiler state: True while generating STM code
	public static boolean duringSTM_Coding;

	/// The .jar File Builder
	public static JarFileBuilder jarFileBuilder;

	/// The Simula temp directory
	public static File simulaTempDir;
	
	/// Temp directory for generated .java files
	public static File tempJavaFileDir;
	
	/// Temp directory for generated .class files
	public static File tempClassFileDir;
	
	/// Next available Object Sequence Number.
	public static int Object_SEQU;

	/// The Simula ClassLoader.
	public static SimulaClassLoader simulaClassLoader;

	/// Packet name used in generated .java files.
	/// NOTE: Must be a single identifier.
	public static String packetName = "simprog";
	
	/// Current Java output Module. Maintained by JavaModule during Java Coding
	public static JavaSourceFileCoder currentJavaFileCoder;
	
	/// The set of Java SourceFile Coders.
	public static Vector<JavaSourceFileCoder> javaSourceFileCoders;

	/// The console
	public static ConsolePanel console;

	/// The Jar files queued for later inclusion.
	/// See: JarFileBuilder for details.
	public static LinkedList<JarFile> includeQueue;

	/// Default constructor.
	Global() {}

	/// Initiate Global variables.
	public static void initiate() {
		Object_SEQU = 8001;
		jarFileBuilder = null;
		simulaClassLoader = null;
		includeQueue = null;
		ClassHierarchy.init();
		duringParsing = true;
		duringChecking = false;
		duringSTM_Coding = false;
		externalJarFiles = new Vector<File>();
		StandardClass.INITIATE();
		String SIMULA_HOME = getSimulaProperty("simula.home", null);
		if (SIMULA_HOME != null) {
			String SIMULA_VERSION = getSimulaProperty("simula.version", null);
			if (SIMULA_VERSION != null) {
				File simdir = new File(SIMULA_HOME, SIMULA_VERSION);
				simIcon = new ImageIcon(new File(simdir, "icons/sim2.png").toString());
				sIcon = new ImageIcon(new File(simdir, "icons/sim.png").toString());
			}
		}
	}

	/// The declaration scope stack.
	private static Stack<DeclarationScope> scopeStack = new Stack<DeclarationScope>();
	
	/// Current declaration scope.
	/// Maintained during Checking and Coding
	private static DeclarationScope currentScope = null; // Current Scope. Maintained during Checking and Coding

	/// Returns the current scope.
	/// @return the current scope
	public static DeclarationScope getCurrentScope() {
		return (currentScope);
	}

	/// During Parsing: Set current scope.
	/// @param scope the new scope
	public static void setScope(DeclarationScope scope) {
		currentScope = scope;
	}

	/// During Checking and Coding: Enter declaration scope.
	/// @param scope the new current scope
	public static void enterScope(DeclarationScope scope) {
		scopeStack.push(currentScope);
		currentScope = scope;
	}

	/// During Checking and Coding: Exit declaration scope.
	public static void exitScope() {
		currentScope = scopeStack.pop();
	}

	/// Returns a temp file directory.
	/// @param subDir the wanted sub-directory name
	/// @return a temp file directory
	public static File getTempFileDir(String subDir) {
		String tmp = System.getProperty("java.io.tmpdir");
		File tempFileDir = new File(tmp, subDir);
		tempFileDir.mkdirs();
		setAccessRWX(tempFileDir);
		return (tempFileDir);
	}

	/// Utility: Set read-write-execute access on a directory
	/// @param dir the directory
	private static void setAccessRWX(File dir) {
		dir.setReadable(true, false); // Readable for all users
		dir.setWritable(true, false); // Writable for all users
		dir.setExecutable(true, false); // Executable for all users
	}

	/// Try set Global.outputDir
	/// @param dir a directory
	public static void trySetOutputDir(File dir) {
		dir.mkdirs();
		if (dir.canWrite())
			Global.outputDir = dir;
		else {
			Global.outputDir = getTempFileDir("simulaEditor/bin");
		}
	}

	/// Initiate Simula properties.
	public static void initSimulaProperties() {
		if (simulaProperties == null)
			loadProperties();
	}

	/// Returns a Simula property.
	/// @param key          property key
	/// @param defaultValue default value
	/// @return a Simula property
	public static String getSimulaProperty(String key, String defaultValue) {
		if (simulaProperties == null)
			loadProperties();
		return (simulaProperties.getProperty(key, defaultValue));
	}

	/// Load Simula properties.
	private static void loadProperties() {
		String USER_HOME=System.getProperty("user.home");
		simulaPropertiesFile=new File(USER_HOME,".simula/simulaProperties.xml");			
		simulaProperties = new Properties();
		try {
			simulaProperties.loadFromXML(new FileInputStream(simulaPropertiesFile));
		} catch (Exception e) {
			Util.popUpError("Can't load: " + simulaPropertiesFile + "\nGot error: " + e);
//			Thread.dumpStack();
		}
		simulaHome = new File(simulaProperties.getProperty("simula.home"));
		String version = simulaProperties.getProperty("simula.version");
		releaseHome = new File(simulaHome, "/"+version);
//		IO.println("Global.loadProperties: simulaHome="+simulaHome);
//		IO.println("Global.loadProperties: Version="+version);
//		IO.println("Global.loadProperties: releaseHome="+releaseHome);
	}

	// **********************************************************
	// *** WORKSPACES
	// **********************************************************
	/// The Simula workspace .xml file.
	private static File simulaWorkspacesFile;
	
	/// The Simula workspace properties.
	private static Properties simulaWorkspaces;

	/// Load Workspace properties.
	public static void loadWorkspaceProperties() {
		simulaWorkspaces = new Properties();
		String USER_HOME = System.getProperty("user.home");
		File simulaPropertiesDir = new File(USER_HOME, ".simula");
		simulaWorkspacesFile = new File(simulaPropertiesDir, "workspaces.xml");
		workspaces = new ArrayDeque<File>();
		if (simulaWorkspacesFile.exists()) {
			try {
				Option.getCompilerOptions(simulaProperties);
				simulaWorkspaces.loadFromXML(new FileInputStream(simulaWorkspacesFile));
				
				Option.getCompilerOptions(simulaWorkspaces);
				RTOption.getRuntimeOptions(simulaWorkspaces);
				
				String ext = simulaWorkspaces.getProperty("simula.extLib", null);
				// Util.println("Global.loadWorkspaceProperties: extLib="+ext);
				if (ext != null)
					Global.extLib = new File(ext);
				
//				for (int i = 1; i <= MAX_WORKSPACE; i++) {
//					String ws = simulaWorkspaces.getProperty("simula.workspace." + i, null);
//					if (ws != null) {
//						if (ws.contains("Simula-Beta-0.3"))
//							ws = ws.replace("Simula-Beta-0.3", "Simula-1.0");
//						// Util.println("Global.loadWorkspaceProperties: ADD "+ws);
//						workspaces.add(new File(ws));
//					}
//				}
				
				String count = simulaWorkspaces.getProperty("simula.workspace.count","0");
//				IO.println("Global.loadSPortEditorProperties: count="+count);
				int n =  Integer.decode(count).intValue();
				for(int i=0;i<n;i++) {
					String ws = simulaWorkspaces.getProperty("simula.workspace." + (i+1));
//					IO.println("Global.loadSPortEditorProperties: workspace="+ws);
					if(ws != null) {
						File workspace = new File(ws);
						if(workspace.exists()) workspaces.add(new File(ws));
					}
				}

			} catch (Exception e) {
				Util.popUpError("Can't load: " + simulaWorkspacesFile + "\nGot error: " + e);
			}
		}
		if (workspaces.isEmpty()) {
			workspaces.add(Global.sampleSourceDir);
		}
		currentWorkspace = workspaces.getFirst();
	}

	/// Set current Workspace.
	/// @param workspace the workspace
	public static void setCurrentWorkspace(File workspace) {
		if (!workspace.equals(Global.currentWorkspace)) {
			workspaces.remove(workspace);
			workspaces.addFirst(workspace);
			storeWorkspaceProperties();
		}
	}

	/// Store Workspace properties.
	public static void storeWorkspaceProperties() {
		simulaWorkspaces = new Properties();
		Option.setCompilerOptions(simulaWorkspaces);
		RTOption.setRuntimeOptions(simulaWorkspaces);
		if (Global.extLib != null)
			simulaWorkspaces.setProperty("simula.extLib", Global.extLib.toString());
		simulaWorkspaces.setProperty("simula.workspace.count", ""+workspaces.size());
		int i = 1;
		for (File ws : workspaces) {
			simulaWorkspaces.setProperty("simula.workspace." + (i++), ws.toString());
		}
		Global.currentWorkspace = workspaces.getFirst();
		simulaWorkspacesFile.getParentFile().mkdirs();
		try {
			simulaWorkspaces.storeToXML(new FileOutputStream(simulaWorkspacesFile), "Simula Editor Properties");
		} catch (Exception e) {
			Util.IERR();
		}
	}

}