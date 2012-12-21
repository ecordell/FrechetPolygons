/**
 * 
 */
package anja.util.example;


import java.util.EventObject;


/**
 * This class is used to handle events on example changes. <br> In case the user
 * has chosen an example an instance of this class is send to all registered
 * <code>ExampleListener</code>.
 * 
 * @author Andreas Lenerz, 27.09.2011
 * @version 1.0
 * 
 * @see anja.util.example.Example
 * @see anja.util.example.ExampleListener
 */
public class ExampleChangeEvent<E extends Example>
		extends EventObject
{

	/**
	 * The example, initialized <code>null</code>
	 */
	protected E	example	= null;


	/**
	 * Main constructor. <code>o</code> specifies the object the event initially
	 * occured in.
	 * 
	 * @param o
	 *            The object the event occured in.
	 */
	public ExampleChangeEvent(
			Object o)
	{
		super(o);
	}


	/**
	 * Constructor. <code>o</code> specifies the object the event initially
	 * occured in, <code>example</code> receives the
	 * <code>Example</code> object, this event is based on.
	 * 
	 * @param o
	 *            The object the event occured in.
	 * @param example
	 *            The example object
	 * 
	 * @see anja.util.example.Example
	 */
	public ExampleChangeEvent(
			Object o,
			E example)
	{
		super(o);
		this.example = example;
	}


	/**
	 * Returns the <code>Example</code> object.
	 * 
	 * @return The example
	 * 
	 * @see anja.util.example.Example
	 */
	public E getExample()
	{
		return this.example;
	}

}
