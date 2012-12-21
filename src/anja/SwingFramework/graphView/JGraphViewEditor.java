/*
 * File: JGraphViewEditor.java
 * Created on May 24, 2006 by ibr
 *
 * TODO Write documentation
 */
package anja.SwingFramework.graphView;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import anja.SwingFramework.JAbstractEditor;
import javax.swing.filechooser.FileNameExtensionFilter;
import anja.SwingFramework.JSystemHub;

/**
 * Part of the static graph viewer system. 
 * Cannot perform any modifications to a graph!<br>
 * <b>(i.e. the scene/editor components are minimally implemented to
 * provide graph rendering and nothing else!)</b>
 * 
 * @author Ibragim Kouliev
 *
 */

class JGraphViewEditor extends JAbstractEditor
{

    //*************************************************************************
    //                             Public constants
    //*************************************************************************
    
    //*************************************************************************
    //                             Private constants
    //*************************************************************************
    
    //*************************************************************************
    //                             Class variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Public instance variables
    //*************************************************************************
    
    //*************************************************************************
    //                       Protected instance variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance variables
    //*************************************************************************
        
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    public JGraphViewEditor(JSystemHub hub)
    {
        super(hub);
    }
 
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
    
    // ------------------------------ STUBS -----------------------------------
    
    /**
     * Reimplemented from JAbstractEditor<br>
     * <b>NO-OP!</b>
     */
    public void mouseClicked(MouseEvent e)
    {
    }
    
    /**
     * Reimplemented from JAbstractEditor<br>
     * <b>NO-OP!</b>
     */
    public void mouseEntered(MouseEvent e)
    {
    }
    
    /**
     * Reimplemented from JAbstractEditor<br>
     * <b>NO-OP!</b>
     */
    public void mouseExited(MouseEvent e)
    {
    }
   
    /**
     * Reimplemented from JAbstractEditor<br>
     * <b>NO-OP!</b>
     */
    public void mousePressed(MouseEvent e)
    {
    }
   
    /**
     * Reimplemented from JAbstractEditor<br>
     * <b>NO-OP!</b>
     */
    public void mouseReleased(MouseEvent e)
    {
    }
   
    /**
     * Reimplemented from JAbstractEditor<br>
     * <b>NO-OP!</b>
     */
    public void mouseDragged(MouseEvent e)
    {
    }
   
    /**
     * Reimplemented from JAbstractEditor<br>
     * <b>NO-OP!</b>
     */
    public void mouseMoved(MouseEvent e)
    {
    }
   
    /**
     * Reimplemented from JAbstractEditor<br>
     * <b>NO-OP!</b>
     */
    public void draw(Graphics2D g)
    {
    }
   
    /**
     * Reimplemented from JAbstractEditor<br>
     * <b>NO-OP!</b>
     */
    protected void createUI()
    { 
    }

    /**
     * Reimplemented from JAbstractEditor<br>
     * <b>NO-OP!</b>
     */
    protected FileNameExtensionFilter getFileFilter()
    {return null;}
    
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************

    
    
    
}

		
    
   