package anja.util.example;


import java.io.File;
import java.util.EventObject;


/**
 * This class is used to handle events on example save requests. <br> In case
 * the user has created an example and wishes to save it the class informs all
 * registered <code>ExampleListener</code>. Every event has a file associated in
 * which the user wishes to save the example.
 * 
 * @author Andreas Lenerz, 27.09.2011
 * @version 1.0
 * 
 * @see java.io.File
 * @see anja.util.example.ExampleListener
 */
public class ExampleSaveEvent
		extends EventObject
{

	/**
	 * The file, initialized <code>null</code>
	 */
	protected File	file	= null;


	/**
	 * Main constructor. <code>o</code> specifies the object the event initially
	 * occured in.
	 * 
	 * @param o
	 *            The object the event occured in.
	 */
	public ExampleSaveEvent(
			Object o)
	{
		super(o);
	}


	/**
	 * Constructor. <code>o</code> specifies the object the event initially
	 * occured in and receives the <code>File</code> object, the example should
	 * be saved in. The name of the file could be the name of the example.
	 * 
	 * @param o
	 *            The object the event occured in.
	 * @param f
	 *            The file object
	 * 
	 * @see java.io.File
	 */
	public ExampleSaveEvent(
			Object o,
			File f)
	{
		super(o);
		this.file = f;
	}


	/**
	 * Returns the <code>File</code> object.
	 * 
	 * @return The file
	 * 
	 * @see java.io.File
	 */
	public File getFile()
	{
		return this.file;
	}

}
