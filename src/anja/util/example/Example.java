package anja.util.example;


import java.io.Serializable;


/**
 * Basic class for all examples. Each example should be saved in a single object
 * which implements this interface.
 * 
 * <br>
 * 
 * Each example should have a unique and meaningful name that can be used to
 * identify the example. Each object must therefore override the
 * <code>toString()</code> method of <code>Object</code>
 * 
 * @author Andreas Lenerz, 27.09.2011
 * @version 1.0
 * 
 * @see java.io.Serializable
 * @see java.lang.Object
 */
public interface Example
		extends Serializable
{

	/**
	 * Returns the name of the example.
	 * 
	 * @return The name of this example
	 */
	@Override
	public String toString();

}
