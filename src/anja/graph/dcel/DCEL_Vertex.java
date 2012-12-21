/*
 * File: DCEL_Vertex.java
 * Created on Oct 26, 2005 by ibr
 *
 */
package anja.graph.dcel;

import anja.graph.*;
import java.util.Vector;
import java.util.Iterator;

/**
 * DCEL Vertex container class.
 * 
 * @author ibr
 *
 * TODO Write documentation
 */

/*
 * Package visibility here, since these classes are not meant
 * to be instantiated from outside the graph package
 */

class DCEL_Vertex extends Vertex
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
    //this is public so the class is not cluttered with access methods
    public Vector<DCEL_Edge> _slings;
    
    //*************************************************************************
    //                       Protected instance variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance variables
    //*************************************************************************
    
    private DCEL_Edge _incidentEdge;
    
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    /**
     * Creates a new DCEL_Vertex object and initializes it
     * with the supplied vertex.
     */
    public DCEL_Vertex()
    {
        super();
        _incidentEdge = null;
        _slings = new Vector<DCEL_Edge>();
    }
    
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
    
    //-------------------------- Getters / setters ----------------------------
   
    /**
     * For description, 
     * see {@link anja.graph.Vertex#getIncidentEdge()}
     *
     */
    public DCEL_Edge getIncidentEdge()
    {
        return _incidentEdge;
        
    }
    
    //*************************************************************************
    
    /**
     * 
     */
    public Iterator<Vertex> getVertexIterator(int mode)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    //*************************************************************************

    /**
     * 
     * TODO: write doc
     */
    public Iterator<Edge> getEdgeIterator(int mode)
    {
        return new VertexEdgeIterator(this, mode);
    }
    
    //*************************************************************************

    /**
     * 
     */
    public Iterator<Face> getFaceIterator(int mode)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    //*************************************************************************
    
    /*
    public void vertexMoved()
    {
        
    }*/
    
    
    //------------------ Package-internal getters / setters -------------------
    
    /**
     * Sets the incident DCEL edge.
     * 
     * @param edge
     */
    void setDcelEdge(DCEL_Edge edge)
    {
        _incidentEdge = edge;
    }
    
    //*************************************************************************

    /**
     * Gets the incident DCEL edge.
     * @return  The DCEL edge
     */
    DCEL_Edge getDcelEdge()
    {
        return _incidentEdge;
    }
    
    
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
  
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************
	
    //*************************************************************************
    //                              Class methods
    //*************************************************************************
     
}

		
   
