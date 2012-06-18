package org.scratch.microwebserver.properties;


import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.runtime.ZemBoolean;
import net.zeminvaders.lang.runtime.ZemNumber;
import net.zeminvaders.lang.runtime.ZemObject;
import net.zeminvaders.lang.runtime.ZemString;

public class PropertyDependency
{
	private PropertyNames p;
	private String compareScript;
	//private String hint;
	
	public PropertyDependency(PropertyNames p,String compareScript/*,String hint*/)
	{
		this.p=p;
		this.compareScript=compareScript;
	}
	
	public boolean dependencyMet(Object input)
	{
		System.err.println("CHECKING DEPENDS ("+compareScript+") FOR "+p.toString());
		
		if(input==null)
			return false;
		
		Interpreter ip = new Interpreter();
		
		if(input instanceof Boolean)
		{
			System.err.println("DATATYPE: boolean");
			ip.setVariable("input",new ZemBoolean( ((Boolean)input).booleanValue()) );
		}
		else if(input instanceof Number)
		{
			System.err.println("DATATYPE: number");
			ip.setVariable("input",new ZemNumber( ((Number)input).doubleValue()) );
		}
		else
		{
			System.err.println("DATATYPE: string ("+input.getClass().getName()+") "+input.toString());
			ip.setVariable("input",new ZemString(input.toString()));
		}
		
		try
		{
			//System.err.println("if ( "+compareScript+" )\n{\n\toutput=true;\n}\nelse\n{\n\toutput=false;\n}");
			ip.eval("if ( "+compareScript+" )\n{\n\toutput=true;\n}\nelse\n{\n\toutput=false;\n}");
			
			ZemObject zo = ip.getVariable("output",null);
			
			if(zo instanceof ZemBoolean)
				return ((ZemBoolean)zo).booleanValue();
			else
				return false;
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		
	}
	
	public PropertyNames getPropertyName()
	{
		return p;
	}
}
