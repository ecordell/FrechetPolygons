package anja.util.example;


import java.util.EventListener;


/**
 * The listener is called after an <code>Example</code> changes or needs to be
 * saved. <br> It has to be registered to the object that handles the examples
 * (like the <code>ExamplePanel</code>.
 * 
 * @author Andreas Lenerz, 27.09.2011
 * @version 1.0
 * 
 * @see anja.util.example.Example
 * @see anja.util.example.ExamplePanel
 */
public interface ExampleListener<E extends Example>
		extends EventListener
{

	/**
	 * Invoked when an example change occurs.
	 * 
	 * @param e
	 *            The change event
	 */
	public void exampleChanged(
			ExampleChangeEvent<E> e);


	/**
	 * Invoked when an example save is requested
	 * 
	 * @param e
	 *            The save event
	 */
	public void exampleSave(
			ExampleSaveEvent e);

}
