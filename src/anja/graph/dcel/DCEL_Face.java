/*
 * File: DCEL_Face.java
 * Created on Nov 2, 2005 by ibr
 * 
 */
package anja.graph.dcel;

import java.util.Iterator;
import anja.graph.*;

/*
 * Package visibility here, since these classes are not meant
 * to be instantiated from outside the graph package
 */

/**
 * 
 * @author ibr
 *
 * TODO Write documentation
 */

class DCEL_Face extends Face
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
    
    /** An edge on the boundary of this face */
    private DCEL_Edge _boundaryEdge;
    
    //TODO: inner components vector + access
    
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    public DCEL_Face()
    {
        _boundaryEdge = null;
    }
     
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
    
    public Edge getBoundaryEdge()
    {
        return _boundaryEdge;
    }
    
    //*************************************************************************

    public Iterator getInnerFaces()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    //*************************************************************************

    public Iterator<Vertex> getVertexIterator(int mode)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    //*************************************************************************

    public Iterator<Edge> getEdgeIterator(int mode)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    //*************************************************************************

    public Iterator<Face> getFaceIterator(int mode)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    //------------------ Package-internal getters / setters -------------------
        
    DCEL_Edge getDcelBoundaryEdge()
    {
        return _boundaryEdge;
    }
    
    //*************************************************************************
    
    void setDcelBoundaryEdge(DCEL_Edge edge)
    {
        _boundaryEdge = edge;
    }
       
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************


}
 
   
