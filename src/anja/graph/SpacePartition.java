/*
 * File: AbstractPointLocator.java
 * Created on Dec 6, 2005 by ibr
 *
 * 
 */
package anja.graph;

import java.awt.geom.Rectangle2D;

/**
 * AbstractSpacePartition is the base class for various 
 * spacial partition structures. In its present form the API provides
 * methods for addition and removal of vertices and edges, as well as 
 * vertex/edge location and nearest-neighbor queries. 
 * 
 * <p>Data structures derived from this class can be used in conjunction
 * with graph connectivity classes or separately as well.
 * 
 * @author Ibragim Kouliev
 * 
 */
public abstract class SpacePartition
		implements java.io.Serializable
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
    
    //*************************************************************************
    //                     Abstract public API declarations
    //*************************************************************************
    
    //------------------ Addition and removal of graph elements ---------------
    
    /**
     * Adds a new vertex. All addition/removal methods 
     * must automatically perform any necesssary restructuring of 
     * the space partition.
     * 
     */
    public abstract void addVertex(Vertex v);
    
    //*************************************************************************
    
    /**
     * Removes a vertex.
     * 
     */
    public abstract void removeVertex(Vertex v);
    
    //*************************************************************************
    
    /**
     * Adds a new edge.
     * 
     */
    public abstract void addEdge(Edge e);
    
    //*************************************************************************
    
    /**
     * Removes an edge.
     * 
     */
    public abstract void removeEdge(Edge e);
    
    //*************************************************************************
    
    /**
     * Clears the entire contents of the space partition
     *
     */
    public abstract void clear();
    
    //------------------------- General queries -------------------------------
    
    /**
     * Returns <code><b>true</b></code> if the partition is empty.
     * 
     * @return true, if the partition is empty, false else
     */
    public abstract boolean isEmpty();
    
    //*************************************************************************
    
    /**
     * Returns the total number of vertices inside a partition.
     * 
     * @return The number of vertices
     */
    public abstract int getNumVertices();
    
    //*************************************************************************
    
    /**
     * Returns the total number of edges inside a partition.
     * 
     * @return The number of edges
     */
    public abstract int getNumEdges();
    
    
    /**
     * Returns the bounding rectangle of the vertices contained
     * inside a partition.
     * 
     * @return The bounding rectangle
     */
    public abstract Rectangle2D getBoundingRectangle();
    
    //TODO: Bounding Box (i.e. also for 3-D) method
    
    
    //-------- Point location / Data retrieval at specific coordinates --------
    
    /**
     * Returns the vertex at the specified coordinates. If such a
     * vertex can't be found, this method returns
     * <code><b>null</b></code>. <br>
     * Thus, it can be used as a query method as well, i.e. just to
     * check for the presence of a vertex with specific coordinates.
     * 
     * @param x x-coordinate of the target position
     * @param y y-coordinate of the target position
     * @return Reference to a <code><b>Vertex</b></code> or
     *         <code><b>null</b></code>.
     */
    public abstract Vertex getVertexAt(double x, double y);
      
    //*************************************************************************
   
    /**
     * Same as above, but in 3-space. <br>
     * 
     * <br>
     * <a name="twoDeeGetVertexAt"></a> <b>Note:</b> in purely
     * two-dimensional data structures this method should always
     * return <code><b>null</b></code> to avoid confusion.
     * 
     * @param x x-coordinate of the target position
     * @param y y-coordinate of the target position
     * @param z z-coordinate of the target position
     * @return Reference to a <code><b>Vertex</b></code> or
     *         <code><b>null</b></code>.
     * 
     */
    public abstract Vertex getVertexAt(double x, double y, double z);
    
    //*************************************************************************
      
    /**
     * Retrieves the vertex which is closest to the specified
     * position, in terms of Euclidian distance.
     * 
     * @param x x-coordinate of the target position
     * @param y y-coordinate of the target position
     * @return Reference to a <code><b>Vertex</b></code> or
     *         <code><b>null</b></code> if the data structure
     *         is empty.
     */
    public abstract Vertex getNearestVertex(double x, double y);
      
    //*************************************************************************
   
    /**
     * Same as above, but in 3-space. The above 
     * <a href="#twoDeeGetVertexAt">comment
     * about pure 2-d data structures </a> applies here as well.
     * 
     * @param x x-coordinate of the target position
     * @param y y-coordinate of the target position
     * @param z z-coordinate of the target position
     * @return Reference to a <code><b>Vertex</b></code> or
     *         <code><b>null</b></code> if the data structure
     *         is empty.
     */
    public abstract Vertex getNearestVertex(double x, double y, double z);
   
    //*************************************************************************
   
    /**
     * Retrieves the edge whose line segment 'contains' the point with
     * the specified coordinates. If no such edge can be found,
     * this method returns <code><b>null</b></code>. 
     *     
     * @param x x-coordinate of the target position
     * @param y y-coordinate of the target position
     * @return Reference to an <code><b>Edge</b></code> or
     *         <code><b>null</b></code>.
     */
    public abstract Edge getEdgeAt(double x, double y);
  
    //*************************************************************************
   
    /**
     * Same as above, but in 3-space. The above 
     * <a href="#twoDeeGetVertexAt">comment
     * about pure 2-d data structures </a> applies here as well.
     * 
     * @param x x-coordinate of the target position
     * @param y y-coordinate of the target position
     * @param z z-coordinate of the target position
     * 
     * @return Reference to an <code><b>Edge</b></code> or
     *         <code><b>null</b></code>.
     */
    public abstract Edge getEdgeAt(double x, double y, double z);
   
    //*************************************************************************
       
    /**
     * Find the nearest edge to the point given by the two coordinates
     * 
     * @param x First coordinate
     * @param y Second coordinate
     * @return The nearest edge
     */
    public abstract Edge getNearestEdge(double x, double y);
    
    //*************************************************************************
       
    /**
     * Find the nearest edge to the point given by the three coordinates
     * 
     * @param x First coordinate
     * @param y Second coordinate
     * @param z Third coordinate
     * @return The nearest edge
     */
    public abstract Edge getNearestEdge(double x, double y, double z);
   
    //TODO: what about faces? are such methods even necesssary?
    
    
    //----------------------------- Queries -----------------------------------
    
    /**
     * 
     * @param vertex The vertex
     */
    public abstract boolean contains(Vertex vertex);
    
    //*************************************************************************
    
    /**
     * 
     * @param edge The edge
     */
    public abstract boolean contains(Edge edge);
    
    //*************************************************************************
    
    /**
     * 
     */
    public abstract void vertexMoved(Vertex vertex);
        
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************

}

		
    
