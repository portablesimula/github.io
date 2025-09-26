/// (CC) This work is licensed under a Creative Commons
/// Attribution 4.0 International License.
/// 
/// You find a copy of the License on the following
/// page: https://creativecommons.org/licenses/by/4.0/
package simula.compiler.syntaxClass;

/// Utility class OverLoad.
/// 
/// Link to GitHub: <a href=
/// "https://github.com/portablesimula/EclipseWorkSpaces/blob/main/SimulaCompiler2/Simula/src/simula/compiler/syntaxClass/OverLoad.java"><b>Source File</b></a>.
/// 
/// @author Øystein Myhre Andersen
public final class OverLoad extends Type {
	
	/// The overloaded types.
	public Type[] type;
	
	/// Create a new OverLoad type list.
	/// @param type the types
	public OverLoad(final Type... type)	{
		super("OverLoad");
		this.type=type;
	}
	
	/// Check if this Overload type contains the given type.
	/// @param type a type
	/// @return true: if this Overload type contains the given type.
	public boolean contains(Type type) {
		for(Type tp:this.type) {
			if(tp.keyWord == type.keyWord) return(true);
		}
		return(false);
	}
	
	@Override
	public String toString()
	{ StringBuilder s=new StringBuilder();
	  s.append("OverLoad(");
	  boolean first=true;
	  if(type!=null)
		  for(Type t:type) { if(!first) s.append(','); first=false; s.append(t); }
	  s.append(')');
	  return(s.toString());
	}

}
