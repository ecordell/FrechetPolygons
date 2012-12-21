/*
 * File: AbstractEmbedder.java
 * Created on Jun 18, 2006 by ibr
 *
 * TODO Write documentation
 */

package anja.graph;


/**
 * Base abstract class for various graph embedders.
 * 
 * 
 * @author ibr
 *
 * TODO Write documentation
 */
public abstract class Embedder
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
    
    /**
     * TODO: Better docs...
     * 
     * Embed a graph in some way.
     * 
     * <p>Suggested usage of this system is as follows:
     * <p>
     * <ol>
     * <li>Derive a class from AbstractEmbedder</li>
     * <li>Implement the abstract method embedGraph()</li>
     * <li>Inside this method access graph elements using whichever 
     * methods seem appropriate. For example, the simplest possibility
     * would be to use getAllVertices() to retrieve all graph
     * vertices and store them into a temporary java.util.Vector. 
     * <p>You can then use the various move() methods provided
     * by the Vertex class to displace the vertices.</li>
     * 
     * </ol>
     * 
     * 
     * @param graphInstance The graph to be embedded.
     */
    public abstract void embedGraph(Graph graphInstance);
    
    //*************************************************************************
    //                       Protected instance variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance variables
    //*************************************************************************
        
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
        
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************
    
}

		
    
    
    