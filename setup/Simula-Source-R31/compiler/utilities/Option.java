/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.utilities;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/// Compile Time Options.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/Option.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class Option {

	/// The Compiler Modes.
	public enum CompilerMode { 
    	/** Generate Java source and use Java compiler to generate JavaClass files. */					viaJavaSource,
    	/** Generate JavaClass files directly. No Java source files are generated. */ 					directClassFiles,
    	/** Generate ClassFile byte array and load it directly. No intermediate files are created. */	simulaClassLoader
    }

	/// The Compiler mode.
	public static CompilerMode compilerMode;
	
	/// Source file is case sensitive.
	public static boolean CaseSensitive=false;
	
	/// Output messages about what the compiler is doing.
	public static boolean verbose = false; 
	
	/// Generate warning messages
	public static boolean WARNINGS=true;

	/// TRUE:Do not create popUps at runtime
	public static boolean noPopup = false; 
	
	/// true: Don't execute generated .jar file
	public static boolean noExecution = false;
	
	/// false: Disable all language extensions. In other words,
	/// follow the Simula Standard literally
	public static boolean EXTENSIONS=true;

	/// Testing and debugging options
	public static class internal {
		/** Default Constructor: NOT USED */ public internal() { Util.IERR(); }

		/// Used to insert code to enforce 'stack size mismatch'
		public static boolean TESTING_STACK_SIZE = false;

		/// List generated .class files
		public static boolean LIST_GENERATED_CLASS_FILES = false;

		/// INLINE_TESTING on/off
		public static boolean INLINE_TESTING = false; 
		
		/// Used by Java-Coding to save the generated .java files.
		/// If not set, a temp directory is used/created.
		public static File keepJava = null;


		// Overall TRACING Options
		/** Debug option */	public static boolean TRACING=false;
		/** Debug option */	public static boolean DEBUGGING=false;		// Set by EditorMenues - doDebugAction

		// Scanner Trace Options
		/** Debug option */	public static boolean TRACE_SCAN=false;
		/** Debug option */	public static boolean TRACE_COMMENTS=false;

		// Parser Trace Options
		/** Debug option */	public static boolean TRACE_PARSE=false;
		/** Debug option */	public static int     PRINT_SYNTAX_TREE=0;
		/** Debug option */	public static boolean TRACE_ATTRIBUTE_OUTPUT=false;
		/** Debug option */	public static boolean TRACE_ATTRIBUTE_INPUT=false;

		// Checker Trace Options
		/** Debug option */	public static boolean TRACE_CHECKER=false;
		/** Debug option */	public static boolean TRACE_CHECKER_OUTPUT=false;
		/** Debug option */	public static int     TRACE_FIND_MEANING=0;

		// Java Coder Options
		/** Debug option */	public static boolean TRACE_CODING=false;         // Only when .java output
		/** Debug option */	public static boolean GNERATE_LINE_CALLS=false;   // Only when .java output

		// Byte code engineering Options
		/** Debug option */	public static boolean TRACE_BYTECODE_OUTPUT=false;
		/** Debug option */	public static boolean LIST_REPAIRED_INSTRUCTION_LIST=false;
		/** Debug option */	public static boolean TRACE_REPAIRING=false;
		/** Debug option */	public static boolean LIST_INPUT_INSTRUCTION_LIST=false;
		/** Debug option */	public static boolean TRACE_REPAIRING_INPUT=false;
		/** Debug option */	public static boolean TRACE_REPAIRING_OUTPUT=false;

		/** Runtime Options */ public static String SOURCE_FILE="";
		/** Runtime Options */ public static String RUNTIME_USER_DIR="";
		
		/// Initiate Compiler options
		public static void InitCompilerOptions() {

			Option.internal.TRACING=false;
			Option.internal.DEBUGGING=false;

			// Scanner Trace Options
			Option.internal.TRACE_SCAN=false;
			Option.internal.TRACE_COMMENTS=false;

			// Parser Trace Options
			Option.internal.TRACE_PARSE=false;

			// Checker Trace Options
			Option.internal.TRACE_CHECKER=false;
			Option.internal.TRACE_CHECKER_OUTPUT=false;

			// Coder Trace Options
			Option.internal.TRACE_CODING=false;
		}

	}
	
	/// The default constructor
	private Option() {}
	
	/// Initiate Compiler options.
	public static void InitCompilerOptions() {
//		CompilerMode compilerMode=CompilerMode.viaJavaSource;
		compilerMode=CompilerMode.directClassFiles;
//		compilerMode=CompilerMode.simulaClassLoader;
		Option.CaseSensitive=false;
		Option.verbose = false;
		Option.noExecution = false;
		Option.WARNINGS=true;
		Option.EXTENSIONS=true;
		
		Option.internal.InitCompilerOptions();
	}
	
	/// Get Compiler options from property file.
	/// @param properties the properties used.
	public static void getCompilerOptions(Properties properties) {
		setCompilerMode(properties.getProperty("simula.compiler.option.mode", "directClassFiles"));
		Option.CaseSensitive = properties.getProperty("simula.compiler.option.CaseSensitive", "false").equalsIgnoreCase("true");
		Option.verbose = properties.getProperty("simula.compiler.option.verbose", "false").equalsIgnoreCase("true");
		Option.noExecution = properties.getProperty("simula.compiler.option.noExecution", "false").equalsIgnoreCase("true");
		Option.WARNINGS = properties.getProperty("simula.compiler.option.WARNINGS", "true").equalsIgnoreCase("true");
		Option.EXTENSIONS = properties.getProperty("simula.compiler.option.EXTENSIONS", "true").equalsIgnoreCase("true");
	}
	
	/// Set Compiler options in property file.
	/// @param properties the properties used.
	public static void setCompilerOptions(Properties properties) {
		properties.setProperty("simula.compiler.option.mode", ""+Option.compilerMode);
		properties.setProperty("simula.compiler.option.CaseSensitive", ""+Option.CaseSensitive);
		properties.setProperty("simula.compiler.option.verbose", ""+Option.verbose);
		properties.setProperty("simula.compiler.option.noExecution", ""+Option.noExecution);
		properties.setProperty("simula.compiler.option.WARNINGS", ""+Option.WARNINGS);
		properties.setProperty("simula.compiler.option.EXTENSIONS", ""+Option.EXTENSIONS);
	}

	/// Editor Utility: Set Compiler Mode.
	public static void setCompilerMode() {
		JPanel panel=new JPanel();
		panel.setBackground(Color.white);
		JCheckBox but1 = checkBox("viaJavaSource","Generate Java source and use Java compiler to generate JavaClass files.");
		JCheckBox but2 = checkBox("directClassFiles","Generate JavaClass files directly. No Java source files are generated.");
		JCheckBox but3 = checkBox("simulaClassLoader","Generate ClassFile byte array and load it directly. No intermediate files are created.");

		if(Option.compilerMode == CompilerMode.viaJavaSource) but1.setSelected(true);
		else if(Option.compilerMode == CompilerMode.directClassFiles) but2.setSelected(true);
		else if(Option.compilerMode == CompilerMode.simulaClassLoader) but3.setSelected(true);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		panel.add(but1); buttonGroup.add(but1);
		panel.add(new JLabel("   The Simula Compiler will generate Java source files and use"));
		panel.add(new JLabel("   the Java compiler to generate JavaClass files which in turn"));
		panel.add(new JLabel("   are collected together with the Runtime System into the"));
		panel.add(new JLabel("   resulting executable jar-file."));
		panel.add(new JLabel(" "));
		panel.add(but2); buttonGroup.add(but2);
		panel.add(new JLabel("   The Simula Compiler will generate JavaClass files directly"));
		panel.add(new JLabel("   which in turn are collected together with the Runtime System"));
		panel.add(new JLabel("   into the resulting executable jar-file."));
		panel.add(new JLabel("   No Java source files are generated."));
		panel.add(new JLabel(" "));
		panel.add(but3); buttonGroup.add(but3);
		panel.add(new JLabel("   The Simula Compiler will generate ClassFile byte array and"));
		panel.add(new JLabel("   load it directly. No intermediate files are created."));
		panel.add(new JLabel(" "));
		panel.add(new JLabel("   NOTE:   In this mode, the editor will terminate after the first"));
		panel.add(new JLabel("                  program execution"));
		panel.add(new JLabel(" "));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		Util.optionDialog(panel,"Select Compiler Mode",JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,"Ok");
    	Global.storeWorkspaceProperties();
	}

	/// Editor Utility: Set Compiler Mode.
	/// @param id the mode String.
	public static void setCompilerMode(String id) {
		if(id.equals("viaJavaSource")) {
			Option.compilerMode = CompilerMode.viaJavaSource;
		} else if(id.equals("directClassFiles")) {
			Option.compilerMode = CompilerMode.directClassFiles;
		} else if(id.equals("simulaClassLoader")) {
			Option.compilerMode = CompilerMode.simulaClassLoader;
		}
	}
	
	/// Utility to get SelectedButtonText.
	/// @param buttonGroup the button group to inspect.
	/// @return the selected String.
	public String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }

	/// Returns the option name 'id'
	/// @param id option id
	/// @return the option name 'id'
	public static boolean getOption(String id) {
		if(id.equalsIgnoreCase("CaseSensitive")) return(CaseSensitive); 
		if(id.equalsIgnoreCase("VERBOSE")) return(verbose); 
		if(id.equalsIgnoreCase("noExecution")) return(noExecution); 
		if(id.equalsIgnoreCase("WARNINGS")) return(WARNINGS); 
		if(id.equalsIgnoreCase("EXTENSIONS")) return(EXTENSIONS); 
		if(id.equalsIgnoreCase("TRACING")) return(internal.TRACING); 
		if(id.equalsIgnoreCase("TRACE_SCAN")) return(internal.TRACE_SCAN); 
		if(id.equalsIgnoreCase("TRACE_COMMENTS")) return(internal.TRACE_COMMENTS); 
		if(id.equalsIgnoreCase("TRACE_PARSE")) return(internal.TRACE_PARSE); 
		if(id.equalsIgnoreCase("TRACE_ATTRIBUTE_OUTPUT")) return(internal.TRACE_ATTRIBUTE_OUTPUT); 
		if(id.equalsIgnoreCase("TRACE_ATTRIBUTE_INPUT")) return(internal.TRACE_ATTRIBUTE_INPUT); 
		if(id.equalsIgnoreCase("TRACE_CHECKER")) return(internal.TRACE_CHECKER); 
		if(id.equalsIgnoreCase("TRACE_CHECKER_OUTPUT")) return(internal.TRACE_CHECKER_OUTPUT); 
		if(id.equalsIgnoreCase("TRACE_CODING")) return(internal.TRACE_CODING); 
		if(id.equalsIgnoreCase("TRACE_BYTECODE_OUTPUT")) return(internal.TRACE_BYTECODE_OUTPUT); 
		return(false);
	}

	/// Set the option named 'id' to the given value
	/// @param id option id
	/// @param val new option value
	public static void setOption(String id,boolean val) {
		if(id.equalsIgnoreCase("CaseSensitive")) CaseSensitive=val; 
		if(id.equalsIgnoreCase("VERBOSE")) verbose=val; 
		if(id.equalsIgnoreCase("noExecution")) noExecution=val; 
		if(id.equalsIgnoreCase("WARNINGS")) WARNINGS=val; 
		if(id.equalsIgnoreCase("EXTENSIONS")) EXTENSIONS=val; 
		if(id.equalsIgnoreCase("TRACING")) internal.TRACING=val; 
		if(id.equalsIgnoreCase("TRACE_SCAN")) internal.TRACE_SCAN=val; 
		if(id.equalsIgnoreCase("TRACE_COMMENTS")) internal.TRACE_COMMENTS=val; 
		if(id.equalsIgnoreCase("TRACE_PARSE")) internal.TRACE_PARSE=val; 
		if(id.equalsIgnoreCase("TRACE_ATTRIBUTE_OUTPUT")) internal.TRACE_ATTRIBUTE_OUTPUT=val; 
		if(id.equalsIgnoreCase("TRACE_ATTRIBUTE_INPUT")) internal.TRACE_ATTRIBUTE_INPUT=val; 
		if(id.equalsIgnoreCase("TRACE_CHECKER")) internal.TRACE_CHECKER=val; 
		if(id.equalsIgnoreCase("TRACE_CHECKER_OUTPUT")) internal.TRACE_CHECKER_OUTPUT=val; 
		if(id.equalsIgnoreCase("TRACE_CODING")) internal.TRACE_CODING=val; 
		if(id.equalsIgnoreCase("TRACE_BYTECODE_OUTPUT")) internal.TRACE_BYTECODE_OUTPUT=val; 
	}

	/// Editor Utility: Select Compiler Options.
	public static void selectCompilerOptions() {
		JPanel panel=new JPanel();
		panel.setBackground(Color.white);
		panel.add(checkBox("CaseSensitive","Source file is case sensitive."));
		panel.add(checkBox("Verbose","Output messages about what the compiler is doing"));
		panel.add(checkBox("Warnings","Generate warning messages"));
		panel.add(checkBox("Extensions","Disable all language extensions. In other words, follow the Simula Standard literally"));
		panel.add(checkBox("noExecution","Don't execute generated .jar file"));
		if(Option.internal.DEBUGGING) {
			panel.add(checkBox("TRACING","Debug option"));
			panel.add(checkBox("TRACE_SCAN","Debug option"));
			panel.add(checkBox("TRACE_COMMENTS","Debug option"));
			panel.add(checkBox("TRACE_PARSE","Debug option"));
			panel.add(checkBox("TRACE_ATTRIBUTE_OUTPUT","Debug option"));
			panel.add(checkBox("TRACE_ATTRIBUTE_INPUT","Debug option"));
			panel.add(checkBox("TRACE_CHECKER","Debug option"));
			panel.add(checkBox("TRACE_CHECKER_OUTPUT","Debug option"));
			panel.add(checkBox("TRACE_CODING","Debug option"));
			panel.add(checkBox("TRACE_BYTECODE_OUTPUT","Debug option"));
		}
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		Util.optionDialog(panel,"Select Compiler Options",JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,"Ok");
    	Global.storeWorkspaceProperties();
	}

	/// Editor Utility: Create a checkBox with tooltips.
	/// @param id option id
	/// @param tooltip option's tooltip or null
	/// @return the resulting check box
	private static JCheckBox checkBox(String id,String tooltip) {
		return checkBox(id, tooltip,Option.getOption(id));
	}

	/// Editor Utility: Create a checkBox with tooltips.
	/// @param id option id.
	/// @param tooltip option's tooltip or null.
	/// @param selected true: this checkBox is selected.
	/// @return the resulting check box.
	private static JCheckBox checkBox(String id,String tooltip,boolean selected) {
		JCheckBox item = new JCheckBox(id);
		item.setBackground(Color.white);
        item.setSelected(selected);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if(id.equals("viaJavaSource") || id.equals("directClassFiles") || id.equals("simulaClassLoader")) {
            		if(Option.verbose) Util.println("Compiler Mode: "+id);
        			Option.setCompilerMode(id);
        		} else {
        		Option.setOption(id,item.isSelected());
        		}
		}});
        if(tooltip != null) item.setToolTipText(tooltip);
        item.addMouseListener(new MouseAdapter() {
            Color color = item.getBackground();
            @Override
            public void mouseEntered(MouseEvent me) {
               color = item.getBackground();
               item.setBackground(Color.lightGray); // change the color to lightGray when mouse over a button
            }
            @Override
            public void mouseExited(MouseEvent me) {
            	item.setBackground(color);
            }
         });
        return(item);
	}

}