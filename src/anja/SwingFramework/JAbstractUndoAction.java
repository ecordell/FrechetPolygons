/*
 * Created on Mar 2, 2005
 *
 * JAbstractUndoAction.java
 */

package anja.SwingFramework;

import anja.SwingFramework.event.EventType;

/**
 * This is the base class for all editor undo action classes. Undo
 * actions can be stored from any editor via the
 * {@link JAbstractEditor#putUndoAction(JAbstractUndoAction)}method.
 * The corresponding changes can then be undone later by pressing
 * CTRL+Z. The undo system is stack based, i.e. the latest undo action
 * is executed first.
 * 
 * <p>The minimal requirement for a derived class is to implement the
 * undo() method which defines the actual undo operation(s). 
 * 
 * @author Ibragim Kouliev
 */

public abstract class JAbstractUndoAction
{

	public abstract void undo();
    
    //*************************************************************************
    // 		              Protected instance methods
    //*************************************************************************
    
    //disabled default constructor
    protected JAbstractUndoAction()
    {}
    
    //*************************************************************************
    // 			       Private instance methods
    //*************************************************************************
}




