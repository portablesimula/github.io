package simula.compiler.utilities;

import java.io.IOException;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import simula.compiler.AttributeInputStream;
import simula.compiler.AttributeOutputStream;
import simula.compiler.syntaxClass.SyntaxClass;

import java.util.Vector;

/// ClasFile coding utility: ClassHierarchy.
///
/// Mail from: Adam Sotona <adam.sotona@oracle.com>  on Apr 28, 2024.
/// 
/// Your compiler reached the complexity where you reference generated classes
/// from other generated classes. Class-File API in certain circumstances needs
/// to know some information about the classes referenced from the generated
/// bytecode. Such information is provided in ClassHierarchyInfo using functional
/// interface ClassHierarchyResolver. By default is the information obtained from
/// system class loader. However, the classes you generate are probably not yet
/// known to the system class loader.
/// 
/// You should specify a custom ClassHierarchyResolver for your compiler as a
/// Class-File API option
/// `ClassFile.of(ClassFile.ClassHierarchyResolverOption.of(...))`.
/// 
/// Here you have multiple options how to provide the missing information using
/// combinations of ClassHierarchyResolver factory methods and custom code:
/// 
/// For example, if the required classes have been already generated and you can
/// provide a physical access to them, you can compose the ClassHierarchyResolver
/// this way:
/// 
/// `ClassHierarchyResolver.defaultResolver().orElse(ClassHierarchyResolver.
/// ofResourceParsing(Function<ClassDesc, InputStream>).cached())`
/// 
/// Or if you know all the generated classes in advance, you can provide the
/// missing info about the generated classes in a set and map form:
/// 
/// `ClassHierarchyResolver.defaultResolver().orElse(ClassHierarchyResolver.
/// of(Collection<ClassDesc> interfaces, Map<ClassDesc, ClassDesc> classToSuperClass))`
/// 
/// Or in a form of dynamic direct implementation of the ClassHierarchyResolver:
/// 
/// `ClassHierarchyResolver.defaultResolver().orElse(classDesc -> isInterface ? :
/// ClassHierarchyInfo.ofInterface() : ClassHierarchyInfo.ofClass(ClassDesc superClass))`
///
/// ----------------------------------------------------------------------------------
///
/// This class 'ClassHierarchy' is used to establish a complete map of all generated classes.
/// It is used by the class file builders of Simula classes, procedures, blocks, prefixed blocks and thunks.
///
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/utilities/ClassHierarchy.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public abstract class ClassHierarchy {
	/// The classToSuperClass Map.
	private static Map<ClassDesc, ClassDesc> classToSuperClass;
	
	/// The interfaces.
	private static Collection<ClassDesc> interfaces;
	
	/** Default Constructor: NOT USED */ private ClassHierarchy() {}

	/// Initiale local variables.
	public static void init() {
		classToSuperClass = new HashMap<ClassDesc, ClassDesc>();
		interfaces = new Vector<ClassDesc>();
		
		classToSuperClass.put(RTS.CD.RTS_CLASS, ConstantDescs.CD_Object);
		classToSuperClass.put(RTS.CD.RTS_PROCEDURE, ConstantDescs.CD_Object);

	}
	
	/// get the ClassHierarchyResolver.
	/// @return the ClassHierarchyResolver.
	public static ClassHierarchyResolver getResolver() {
		ClassHierarchyResolver res = ClassHierarchyResolver.defaultResolver()
				.orElse(ClassHierarchyResolver.of(interfaces, classToSuperClass));
		return res;
	}

	/// Add a class to the classToSuperClass map.
	/// @param cld a class
	/// @param sup a super class
	public static void addClassToSuperClass(ClassDesc cld, ClassDesc sup) {
		classToSuperClass.put(cld, sup);
	}
	
	/// Get real super class. Ignore RTS_CLASS and RTS_PROCEDURE-
	/// @param sub a sub class.
	/// @return real super class.
	public static ClassDesc getRealSuper(ClassDesc sub) {
		ClassDesc sup = classToSuperClass.get(sub);
		if(sup == null) return null;
		if(sup.equals(RTS.CD.RTS_CLASS) || sup.equals(RTS.CD.RTS_PROCEDURE)) return null;
		return sup;
	}
	
	/// Get real prefix.
	/// @param sub a sub class.
	/// @return real prefix.
	public static String getRealPrefix(String sub) {
		ClassDesc sup = classToSuperClass.get(ClassDesc.of(sub));
		if(sup == null) return null;
		String packageName = sup.packageName();
		if(packageName.equals("simula.runtime")) return null;
		return packageName+'.'+sup.displayName();
	}
	
	/// Returns the resulting ClassDesc.
	/// @return the resulting ClassDesc.
	/// @param classID class ident.
	public static ClassDesc getClassDesc(String classID) {
		Set<ClassDesc> keys = classToSuperClass.keySet();
		String ID = "ClassDesc[" + classID + ']';
		for(ClassDesc key:keys) {
			if(key.toString().equals(ID))
				return key;
		}
		return null;
	}

	// ***********************************************************************************************
	// *** Debug utilities: ClassHierarchy list and print tree
	// ***********************************************************************************************
	/// list of all Nodes.
	private static Vector<Node> allNodes;
	
	/// Debug utility: list classToSuperClass map.
	public static void list() {
		for (Map.Entry<ClassDesc, ClassDesc> entry : classToSuperClass.entrySet()) {
			String key = entry.getKey().descriptorString();
			String val = entry.getValue().descriptorString();
		     IO.println(key + " extends " +val);
		}
	}
	
	/// Debug utility: print classToSuperClass map etc.
	public static void print() {
		allNodes = new Vector<Node>();
		Node top = lookup("_TOP");
		top.children.add(lookup("simula.runtime.RTS_CLASS"));
		top.children.add(lookup("simula.runtime.RTS_PROCEDURE"));
        for (Entry<ClassDesc, ClassDesc> entry : classToSuperClass.entrySet()) {
            ClassDesc sub = entry.getKey();
            ClassDesc sup = entry.getValue();
    		Node supNode = lookup(sup.packageName() + '.' + sup.displayName());
    		Node subNode = lookup(sub.packageName() + '.' + sub.displayName());
    		supNode.children.add(subNode);
        }
		IO.println("\n================= ClassHierarchy.print =================");
		lookup("simula.runtime.RTS_CLASS").print(1);
		IO.println("\n================= Procedure List =================");
		lookup("simula.runtime.RTS_PROCEDURE").print(1);
	}
	
	/// Utility class Node.
	private static class Node implements Comparable<Node> {
		/// Node's name
		String name;
		
		/// Node's children.
		TreeSet<Node> children = new TreeSet<Node>();
		
		/** Default Constructor: NOT USED */ private Node() {}
		/// Utility method: print.
		/// @param indent the indentation.
		void print(int indent) {
			IO.println(SyntaxClass.edIndent(indent) + this.name);
			for(Node child:children) child.print(indent + 1);
		}

		@Override
		public int compareTo(Node node) {
			return name.compareToIgnoreCase(node.name);
		}
	}
	
	/// Debug utility: Lookup a node.
	/// @param name node's name.
	/// @return the node.
	private static Node lookup(String name) {
		for(Node node:allNodes) if(node.name.equals(name)) return(node);
		Node n = new Node(); n.name = name;
		allNodes.add(n);
		return n;
	}
	

	// ***********************************************************************************************
	// *** Attribute File I/O
	// ***********************************************************************************************
	/// Marker in the AttributeOutputStream.
	private static final String mark = "]END ClassHierarchy";
	
	/// Write a ClassHierarchy object to a AttributeOutputStream.
	/// @param oupt the AttributeOutputStream to write to.
	/// @throws IOException if something went wrong.
	public static void writeObject(AttributeOutputStream oupt) throws IOException {
		Util.TRACE_OUTPUT("BEGIN Write ClassHierarchy: ");
        for (Entry<ClassDesc, ClassDesc> entry : classToSuperClass.entrySet()) {
            ClassDesc sub = entry.getKey();
            ClassDesc sup = entry.getValue();
    		oupt.writeString(sub.packageName());
    		oupt.writeString(sub.displayName());
    		oupt.writeString(sup.packageName());
    		oupt.writeString(sup.displayName());
        }
		oupt.writeString(mark);
	}

	/// Read a ClassHierarchy object.
	/// @param inpt the AttributeInputStream to read from
	/// @throws IOException if something went wrong.
	public static void readObject(AttributeInputStream inpt) throws IOException {
		String next = inpt.readString();
		while(! next.equals(mark)) {
			ClassDesc sub = ClassDesc.of(next+'.'+inpt.readString());
			ClassDesc sup = ClassDesc.of(inpt.readString()+'.'+inpt.readString());
			addClassToSuperClass(sub, sup);
			next = inpt.readString();
		}
		Util.TRACE_INPUT("END Read ClassHierarchy: ");
	}
	
	
}
