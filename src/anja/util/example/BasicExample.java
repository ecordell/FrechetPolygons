package anja.util.example;


/**
 * An implementation of the <code>Example</code> interface.
 * 
 * <br>
 * 
 * It adds a name variable to the Example and overwrites the
 * <code>toString()</code> method.
 * 
 * @author Andreas Lenerz, 27.09.2011
 * @version 1.0
 * 
 * @see anja.util.example.Example
 * @see anja.util.example.ExampleListener
 * @see anja.util.example.ExampleChangeEvent
 */
public class BasicExample
		implements Example
{

	/**
	 * The name of the example
	 */
	protected String	name	= null;


	/**
	 * Basic constructor. Initializes the object with an empty name string
	 */
	public BasicExample()
	{
		this(new String());
	}


	/**
	 * The constructor sets the name of the example
	 * 
	 * @param name
	 *            The name of the example
	 */
	public BasicExample(
			String name)
	{
		this.name = name;
	}


	@Override
	public String toString()
	{
		return name;
	}

}
