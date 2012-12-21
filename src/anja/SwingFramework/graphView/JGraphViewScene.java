/*
 * File: JGraphViewScene.java
 * Created on May 24, 2006 by ibr
 *
 * TODO Write documentation
 */
package anja.SwingFramework.graphView;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import anja.graph.*;
import anja.SwingFramework.*;

/**
 * This class implements a static graph scene solely for viewing
 * purposes, i.e. no modifications are possible here (or in
 * JGraphViewEditor, for that matter). The only useful method here
 * is setGraph(), which is used after initialization to specify 
 * an instance of Graph to be viewed, which is then automatically
 * rendered.
 *  
 * @author Ibragim Kouliev
 *
 */

class JGraphViewScene extends JAbstractScene
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
    
    private Graph _graph; // graph object reference
    
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    /**
     * 
     * 
     */
    public JGraphViewScene(JSystemHub hub)
    {
        super(hub);
        _graph = null;
    }
    
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
    
    /**
     * Sets the graph object to be rendered in this view.
     * 
     * @param graphObject reference to a Graph object.
     * 
     */
    public void setGraph(Graph graphObject)
    {
        if(graphObject == null)
        {
            System.err.
             println("JGraphView: supplied graph object is NULL!\n");
        }
        else
        {
            _graph = graphObject;
        }
    }
    
    //*************************************************************************
    
    /**
     * Reimplemented from JAbstractScene.
     * 
     */
    public void draw(Graphics2D g)
    {
        //TODO: anything else here?
        
        if(_graph != null)
        {
            _graph.draw(g, 1.0 / _pixelSize, _invertedFont);
        }
    }
    
    //*************************************************************************

    /**
     * 
     * 
     */
    public Rectangle2D getBoundingRectangle()
    {
       return _graph.getBoundingRectangle();
    }
    
    //*************************************************************************

    /**
     * Reimplemented from JAbstractScene<br>
     * <b>NO-OP!</b>
     */
    public void clear()
    {
       
    }
    
    // ------------------------------- STUBS ----------------------------------
    
    /**
     * Reimplemented from JAbstractScene<br>
     * <b>NO-OP!</b>
     */
    public boolean isEmpty()
    {return false;}

   
    // ---------------------------- STUBS END ---------------------------------
    
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************

    //*************************************************************************

}

		
   
    
    
   