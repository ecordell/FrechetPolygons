/*
 * Created on Feb 24, 2005 
 * 
 * EditorActionListener.java
 *  
 */
package anja.SwingFramework.event;


/**
 * This interface should be implemented by classes that want to be notified of
 * various editor operations.
 * 
 * @author Ibragim Kuliyev
 */

public interface EditorListener
{

	//*************************************************************************
	// 				  Public constants
	//*************************************************************************

	//*************************************************************************
	// 				  Abstract methods
	//*************************************************************************

	/**
	 * Fired by an editor when any editor action that modifies a scene is
	 * peformed.
	 * 
	 * @param event
	 *            An event to be processed.
	 */
	public void editorActionPerformed(
			EditorEvent event);


	//*************************************************************************

	/**
	 * Fired by an editor when an object context menu action occurs.
	 * 
	 * @param event
	 *            An event to be processed.
	 */
	public void contextActionPerformed(
			EditorEvent event);
}
