/* Graph.java
 * Created on 01.08.2005 by snej
 */
package anja.graph;


import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import org.jdom.*;
import org.jdom.output.*;

import anja.graph.dcel.DCEL;
import anja.geom.Polygon2;
import anja.geom.Point2;
import anja.util.SimpleList;
import anja.util.ListItem;

import static anja.graph.Constants.*;


/**
* Implementation of a Graph based on some data-structures like 
* DCEL(default), Adjacencylist, ...
* 
* The Graph can be directed or undirected, edge-weighted, vertex-weighted, 
* (edge-sited), multi-edged and embedded
* 
* Some standard methods are given for adding/deleting a vertex or an edge, 
* drawing and embedding with an embedder(@see anja.newgraph.embedder), ...
*
* @author Jens Behley
* @author Florian Kunze
* @author Jörg Wegener
* 
* @see Edge
* @see Vertex
*/

public abstract class Graph<V extends Vertex, E extends Edge<V>> implements Cloneable, java.io.Serializable
{
    
    //*************************************************************************
    //                             Public constants
    //*************************************************************************
    
    // Data structure types
	
	public enum GraphType {DCEL, ADJACENCY_LIST, SIMPLE}
	
	
    
    //*************************************************************************
    //                           Private constants
    //*************************************************************************
    private static final String _XML_GRAPH = "Graph";
    
    // ------------------------ Attribute names -------------------------------
    private static final String _XML_VERTICES = "Vertices";
	private static final String _XML_EDGES = "Edges";
    private static final String _XML_EDGE_WEIGHTED  = "edge_weighted";
	private static final String _XML_VERTEX_WEIGHTED = "vertex_weighted";
    private static final String _XML_DIRECTED       = "directed"; 
	private static final String _XML_EMBEDDED = "embedded";
	private static final String _XML_TYPE = "type";
	
    //*************************************************************************
    //                            Class variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Public instance variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Package-visible variables
    //*************************************************************************
    
        
    //*************************************************************************
    //                       Protected instance variables
    //*************************************************************************
          
    // tags and flags
    protected boolean _directed           = false;
    protected boolean _edge_weighted      = false; // edges weighted?
    protected boolean _vertex_weighted    = false; // vertices weighted?   
    protected boolean _embedded           = false; // embedded in R^d?
    protected GraphType _type             = GraphType.DCEL;
   
          
    // associated space partition data structure
    protected SpacePartition _spacePartition = null;
    
    protected boolean _drawDebugInfo = false;
        
    //*************************************************************************
    //                        Private instance variables
    //*************************************************************************
    /**
     * IDs are assigned to vertices and edgesstarting from 0 and counting upwards.
     * _nextID indicates the ID which will be assigned to the next added vertex.
     */
    private int _nextVertexID = 0;
    private int _nextEdgeID = 0;
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    /**
     * Creates a new empty graph with the following default parameters:
     * <ul>
     *  <li>The graph is undirected</li><br>
     *  <li>The graph is not weighted</li><br>
     *  <li>The graph IS embedded (and thus drawable)</li><br>
     *  <li>The graph uses a DCEL data structure for connectivity
     *      representation</li><br>
     * </ul> 
     * 
     */
    
    public final static Graph CreateGraph() {
    	Graph g = new DCEL();
    	return g;
    }
    
    /**
     * Default constructor disabled. Graphs are created
     * via the CreateGraph()-methods.
     */
    protected Graph() {
        _directed = false;
        _edge_weighted = false;
        _vertex_weighted = false;
        _embedded = true;
        _drawDebugInfo = false;        
    }
    
    //*************************************************************************
    
    /**
    * Creates a new graph with specified parameters.
    * 
    * @param graph_type     Internal data structure type,
    *                           see descriptions of data structure types 
    *                           above.<p> 
    *                           
    * @param directed           Set this to <code><b>true</b></code>
    *                           to create a directed graph, or to
    *                           <code><b>false</b></code> for an 
    *                           undirected one.<p>
    * 
    * @param edge_weighted      Set to <code><b>true</b></code> if
    *                           edges should be weighted.<p>
    * 
    * @param vertex_weighted    Set to <code><b>true</b></code> if
    *                           vertices should be weighted.<p>
    * 
    * @param embedded           Set to <code><b>true</b></code> if
    *                           the graph is to be embedded in
    *                           &#x211D<sup>d</sup>.<p>
    *                           
    *                           <br>TODO: can I safely use
    *                           the special symbol for double-strike
    *                           R letter here ?
    */
    
    public static final Graph CreateGraph(GraphType graph_type,
    		boolean directed,
    		boolean edge_weighted,
    		boolean vertex_weighted,
    		boolean embedded)
    {
        Graph g;
        switch (graph_type) {
        case DCEL:
            g = new DCEL();
            break;
        default:
            g = new DCEL();
        }
        g._directed = directed;
        g._edge_weighted = edge_weighted;
        g._vertex_weighted = vertex_weighted;
        g._embedded = embedded;
        g._drawDebugInfo = false;
        return g;
    }
    
    
    //*************************************************************************

    /**
     * Creates a new graph from a polygon2 with the following parameters:
     * <ul>
     *  <li>The graph is undirected</li><br>
     *  <li>The graph is not weighted</li><br>
     *  <li>The graph IS embedded (and thus drawable)</li><br>
     *  <li>The graph uses a DCEL data structure for connectivity
     *      representation</li><br>
     * </ul> 
     * 
     * @param poly    Polygon with source vertices
     */
    public final static Graph CreateGraph(Polygon2 poly) {
        Graph g = CreateGraph(GraphType.DCEL, false, false, false, true);
        
        SimpleList pointList = poly.points();
        ListItem point = null;
        Vertex prevVertex = null;
        Vertex firstVertex = null;
        
        for (int i=0; i<pointList.length();i++) {
            point = pointList.next(point);
            Point2 p = (Point2)point.value();
            Vertex newVertex = g.addVertex(p);
            newVertex.setReferenceObject(p);
            if (prevVertex == null)
                firstVertex = newVertex;
            else g.addEdge(prevVertex, newVertex);
            prevVertex = newVertex;
        }
        if (poly.isClosed()) g.addEdge(prevVertex, firstVertex);       
        return g;
    }
   

    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
      
    //------------------ Addition and removal of graph elements ---------------
     
    /**
     * Adds a copy of a vertex to the graph.
     * 
     *  @param v new vertex
     */
    public V addVertex(Vertex v) {
    	V newV = this.addVertex(v.getX(), v.getY(), v.getZ());
    	newV._dimension = v._dimension;
    	newV.setLabel(v.getLabel());
    	newV.setColor(v.getColor());
    	newV.setWeight(v.getWeight());
    	newV.setCounter(v.getCounter());
    	newV.setOutlineColor(v.getOutlineColor());
    	newV.setProxyRadius(v.getProxyRadius());
    	newV.setRadius(v.getRadius());
    	newV.setReferenceObject(v.getReferenceObject());
    	newV.setTextColor(v.getTextColor());
    	return newV;
    }
    
    /**
     * Adds a vertex at 0/0/0
     */
    
   public V addVertex() {
	   V v = this.createVertex();
	   v._graph = this;
	   v.setID(_nextVertexID);
	   _nextVertexID++;
	   return v;
   }
   
   /**
    * Adds a vertex at x
    */
   public V addVertex(double x) {
	   V v = this.addVertex();
	   v.setX(x);
	   v._dimension = 1;
	   return v;
   }
   
   public V addVertex(double x, double y){
	   V v = this.addVertex();
	   v.setX(x);
	   v.setY(y);
	   v._dimension = 2;
	   return v;
   }
   
   public V addVertex(double x, double y, double z) {
	   V v = this.addVertex();
	   v.setX(x);
	   v.setY(y);
	   v.setZ(z);
	   v._dimension = 3;
	   return v;
   }
   
   public V addVertex(double x, double y, String label) {
	   V v = this.addVertex(x,y);
	   v.setLabel(label);
	   return v;
   }
   
   public V addVertex(double x, double  y, Color color)
   {
       V v = this.addVertex(x,y);
       v.setColor(color);
       return v;
   }
   
   public V addVertex(double x, double y, String label, Color color)
   {
       V v = this.addVertex(x,y, label);
       v.setColor(color);
       return v;
   }
   
   public V addVertex(Point2D p) {
	   return this.addVertex(p.getX(), p.getY());
	   
   }
   
   
   /**
    * Has to create a new vertex which is added and returned by the graph via addVertex 
    * @return a new Vertex with default parameters
    */
   abstract protected V createVertex();
   
   
   
    //*************************************************************************
   
    /**
     * Adds an edge from vertex v1 to vertex v2 (v1->v2).
     * 
     * <p>
     * This is a convenience version of addEdge(Edge e) which
     * automatically creates a new edge from v1 to v2 and then adds it
     * to the graph.
     * </p>
     * 
     * @param v1 origin (start) vertex
     * @param v2 target (end) vertex
     * 
     */
    public E addEdge(V v1, V v2) {
    	E e = this.createEdge(v1, v2);
    	e.setID(_nextEdgeID);
    	_nextEdgeID++;
    	return e;
    }
    
    public E addEdge(V v1, V v2, boolean isDirected) {
    	E e = this.addEdge(v1, v2);
    	e.setDirected(isDirected);
    	return e;
    }
    
    /**
     * Adds a copy of an edge to the graph. The edge will be a new object with a new id, but the same attributes
     * 
     * @param e The new edge
     * 
     * @return A reference to the new edge
     */
    public E addEdge(E e) {
    	E newE = this.addEdge(e.getSource(), e.getTarget());
    	newE.setColor(e.getColor());
    	newE.setDirected(e.isDirected());
    	newE.setLabel(e.getLabel());
    	newE.setReferenceObject(e.getReferenceObject());
    	newE.setTextColor(e._textColor);
    	newE.setWeight(e.getWeight());
    	
    	return newE;
    	
    }
    
    abstract protected E createEdge(V v1, V v2);
   
  
    //*************************************************************************
   
    /**
     * Deletes a vertex. <br>
     * All adjacent edges will be removed too, and partial
     * restructuring of the graph's faces may occur as well.
     * 
     * @param v vertex to be removed
     */
    public void deleteVertex(V v) {
    	v._graph = null;
    }
   
    //*************************************************************************
   
    /**
     * Deletes an edge. <br>
     * This command may cause partial restructuring of the graph's
     * faces as well. The vertices belonging to the edge are, of
     * course, not deleted!
     * 
     * @param e The edge
     */
    abstract public void deleteEdge(E e);
    
    //*************************************************************************
    
    /**
     * Clears the entire graph. Graph parameters are left unchanged.
     * 
     */
    abstract public void clear();
        
    //-------------------------- Data modification ----------------------------
         
    //------------------------- Getters / Setters -----------------------------
    
    /**
     * Specifies whether a graph is directed or not.
     * 
     * @param mode <code>true</code> for directed.
     */
    public void setDirected(boolean mode)
    {
        _directed = mode;
    }
   
    //*************************************************************************
      
    /**
     * Specifies whether graph's edges are weighted.
     * 
     * @param mode <code>true</code> for weighted
     */
    public void setEdgeWeighted(boolean mode)
    {
        _edge_weighted = mode;
    }
   
    //*************************************************************************
    
    /**
     * Specifies whether a graph's vertices are weighted.
     * 
     * @param mode <code>true</code> for weighted
     */
    public void setVertexWeighted(boolean mode)
    {
        _vertex_weighted = mode;
    }
   
    //*************************************************************************
   
    /**
     * Specifies whether a graph is embedded.
     * 
     * @param mode <code>true</code> for embedded.
     */
    public void setEmbedded(boolean mode)
    {
        _embedded = mode;
    }
   
    //*************************************************************************
  
    //---------------------------- Query methods ------------------------------
    
    /**
     * Checks whether the graph is completely empty (i.e. no vertices
     * or edges).
     * 
     * @return <code>true</code> if the graph is empty, otherwise
     *         <code>false</code>.
     */
    abstract public boolean isEmpty();
    
    //------------------------------ Embedding --------------------------------
    
    /**
     * Embeds the graph using the given embedder. This method is
     * essentially a callback, i.e. it just does <br>
     * 
     * <pre>
     * embedder.embedGraph(this);
     * </pre>
     * 
     * <br>
     * 
     * @param embedder The embedder
     * 
     * @see Embedder
     * 
     */
    public void embedGraph(Embedder embedder)
    {
        embedder.embedGraph(this);
    }
    
    //*************************************************************************
    
    /**
     * Checks the graph for cycles/loops and returns true if the graph
     * is acyclic.
     * 
     * @return true if the graph is acyclic
     * 
     * TODO: implement this method
     */
    public boolean isAcyclic()
    {
        System.out.println("Graph:isAcyclic() is not implemented yet!\n");
        return true;
    }
   
    //*************************************************************************
   
    /**
     * Returns if the graph is directed or not
     * 
     * @return true if directed, false else
     * 
     */
    public boolean isDirected()
    {
        return _directed;
    }
   
    //*************************************************************************
   
    /**
     * Returns if the graph is weighted or not
     * 
     * @return true if weighted, false else
     * 
     */
    public boolean isEdgeWeighted()
    {
        return _edge_weighted;
    }
   
    //*************************************************************************
    
    /**
     * Returns if the graph is embedded or not
     * 
     * @return true if embedded, false else
     */
    public boolean isEmbedded()
    {
        return _embedded;
    }
   
    //*************************************************************************
   
    /**
     * Returns if the vertex is weighted or not
     * 
     * @return true if weighted, false else
     */
    public boolean isVertexWeighted()
    {
        return _vertex_weighted;
    }
    
    //*************************************************************************
    
    /**
     * Returns the total number of vertices in the graph.
     * 
     * @return The number of vertices
     */
    abstract public int getNumVertices();
       
    //*************************************************************************
    
    /**
     * Returns the total number of edges in the graph.
     * 
     * @return The number of edges
     */
    abstract public int getNumEdges();
    
    //*************************************************************************
    
    /*
     * Returns the total number of faces in the graph.
     * 
     * 
     */
    /*public int getNumFaces()
    {
        return _graphHandler.getNumFaces();
    }*/
    
    /**
     * Returns the ID the next added vertex will get.
     * 
     * @return The ID of the vertex
     */
    public int getNextVertexID() {
    	return _nextVertexID;
    }
    
    /**
     * Returns the ID the next added vertex will get.
     * 
     * @return The ID of the edge
     */
    public int getNextEdgeID() {
    	return _nextEdgeID;
    }    
    //*************************************************************************
    
    /**
     * Returns a Vertex. Which Vertex depends on the actual graph implementation.
     * It will most likely be the first vertex added to the graph.
     * This method is there to get a starting point of the graph without the need
     * to create a vertex iterator.
     */
    abstract public V getAnyVertex();

    /**
     * Returns an Edge. Which Edge depends on the actual graph implementation.
     * It will most likely be the first edge added to the graph.
     * This method is there to get a starting edge of the graph without the need
     * to create an edge iterator.
     */
    abstract public E getAnyEdge();
    
    /**
     * @return BDFSVertexIterator with a starting vertex by getAnyVertex(), outputs all vertices in the graph either by DFS or BFS.
     * @param dfs true->DFS, false->BFS
     */    
    public Iterator<Vertex> getBDFSVertexIterator(boolean dfs) {
        return new BDFSVertexIterator(this, dfs);
    }
 
    /**
     * @return BDFSVertexIterator with source as starting vertex, outputs all vertices in the graph either by DFS or BFS.
     * @param dfs true->DFS, false->BFS
     * @param source The vertex
     */
    public Iterator<Vertex> getBDFSVertexIterator(Vertex source, boolean dfs) {
    	if (source == null) return null;
        if (this.contains(source))
            return new BDFSVertexIterator(source, dfs);
        else return source.getGraph().getBDFSVertexIterator(source, dfs);
    }
    
    /**
     * @return Iterator<Vertex> with source as starting vertex, edges are
     * searched in mode order around a vertex, outputs all vertices in the graph either by DFS or BFS.
     * @param source The vertex
     * @param mode The mode
     */   
    public Iterator<Vertex> getBDFSVertexIterator(Vertex source, int mode, boolean dfs) {
    	if (source == null) return null;
        if (this.contains(source))
            return new BDFSVertexIterator(source, mode, dfs);
        else return source.getGraph().getBDFSVertexIterator(source, mode, dfs);
    }
    
    /**
     * @return BDFSEdgeIterator with a starting vertex by getAnyVertex(), outputs all edges in the graph either by DFS or BFS.
     * @param dfs true->DFS, false->BFS
     */    
    public Iterator<Edge> getBDFSEdgeIterator(boolean dfs) {
        return new BDFSEdgeIterator(this, dfs);
    }
 
    /**
     * @return Iterator<Edge> with source as starting vertex, outputs all edges in the graph either by DFS or BFS.
     * @param dfs true->DFS, false->BFS
     * @param source The vertex
     */
    public Iterator<Edge> getBDFSEdgeIterator(Vertex source, boolean dfs) {
    	if (source == null) return null;
        if (this.contains(source))
            return new BDFSEdgeIterator(source, dfs);
        else return source.getGraph().getBDFSEdgeIterator(source, dfs);
    }
    
    /**
     * @return BDFSEdgeIterator with source as starting vertex, edges are
     * searched in mode order around a vertex, outputs all edges in the graph either by DFS or BFS.
     * @param source The source vertex
     * @param mode The mode
     */   
    public Iterator<Edge> getBDFSEdgeIterator(Vertex source, int mode, boolean dfs) {
    	if (source == null) return null;
        if (this.contains(source))
            return new BDFSEdgeIterator(source, mode, dfs);
        else return source.getGraph().getBDFSEdgeIterator(source, mode, dfs);
    }
    
    /**
     * Checks whether a given vertex is contained in the graph.
     * 
     * @param vertex Vertex to be checked
     * @return <b>true</b> if the vertex in question is in the graph,
     *         otherwise <b>false</b>
     */
    public boolean contains(Vertex vertex) {
        if(_spacePartition != null)
        {
            return _spacePartition.contains(vertex);
        }
        else
        {
            // Fall-back section

            /* Perform 'naive' linear search for 
             * a vertex that's equal to the source.
             * O(n)
             */

            Vertex vx;
            Iterator<Vertex> itr = getAllVertices();
            while(itr.hasNext())
            {
                vx = itr.next();                
                if(vx == vertex)
                {
                    return true;
                }         
            }

            return false; 
        }
    }
    
    //*************************************************************************
    
    /**
     * Checks whether a given edge is contained in the graph.
     * 
     * @param edge Edge to be checked
     * @return <b>true</b> if the edge in question is in the graph,
     *         otherwise <b>false</b>
     */
    public boolean contains(Edge edge){
    if (_spacePartition != null) {
            return _spacePartition.contains(edge);
        } else {
            // Fall-back section

            /*
             * Perform 'naive' linear search for an edge that's equal to the
             * source. O(n)
             */

            Edge compedge;
            Iterator<Edge> itr = getAllEdges();
            while (itr.hasNext()) {
                compedge = itr.next();
                if (compedge == edge) {
                    return true;
                }
            }
            return false;
        }
    }
      
    //------------------------ Graph element access ---------------------------
      
    /**
     * Returns the adjacency list for a vertex v
     * 
     *  @param v Vertex-object
     *  @return Edge list
     */
    /*public LinkedList getAdjList(Vertex v)
    {
        return _graphHandler.getAdjList(v);  
    }*/
   
    //*************************************************************************
   
    /**
     * getting the Edges that have v as destination-vertex (incidence-list)
     * 
     * <br>TODO: this method and getAdjList() have not been properly
     * implemented yet, and they should be superceded by the functionality
     * of Adjacency List container implementation once it's completed!<br>
     * 
     * @param v   vertex
     * @return incidence-list as a LinkedList instance
     */
   
    /*public LinkedList getIncList(Vertex v)
    {
        return _graphHandler.getIncidenceList(v);
    }*/
   
    //*************************************************************************
   
    //---------------------------- Iterators ----------------------------------
    
    /**
     * Returns an iterator that sequentially accesses <u>all</u>
     * vertices in a graph data structure.
     * 
     * @return a vertex iterator
     */
    abstract public Iterator<Vertex> getAllVertices();
    
    //*************************************************************************
    
    /**
     * Returns an iterator that sequentially accesses <u>all</u>
     * edges in a graph data structure.
     * 
     * @return an edge iterator
     */
    abstract public Iterator<Edge> getAllEdges();
    
    //*************************************************************************
    
    /**
     * Returns an iterator that sequentially accesses <u>all</u>
     * faces in a graph data structure.
     * 
     * @return a face iterator
     */
    /*public Iterator<Face> getAllFaces()
    {
        return _graphHandler.getAllFaces();
    }*/
   
    //*************************************************************************
    /**
     * Called internally by a vertex container to update the graph
     * connectivity and point location data (if necessary) after a
     * vertex has been moved.
     * 
     * @param vertex The vertex that has just been modified.
     */
    public abstract void vertexMoved(Vertex vertex);
    // -------------- Data retrieval at specific coordinates ------------------
   
    /**
     * Returns the vertex at the specified coordinates. If such a
     * vertex can't be found, this method returns
     * <code><b>null</b></code>. <br>
     * Thus, it can be used as a query method as well, i.e. just to
     * check for the presence of a vertex with specific coordinates.
     * <p>
     * <table width = 100% bgcolor="lavender" cellpading = 20>
     * <tr>
     * <td> Note on implementation: <br>
     * <p>
     * This and other vertex/edge location methods first check whether
     * a spatial partition data structure, derived from
     * {@link SpacePartition}, has been specified. If yes,
     * the method calls are passed on to the corresponding methods of
     * the spatial partition class. Otherwise, these methods fall back
     * to default implementations which perform simple O(n) linear
     * searching for an element.</td>
     * </tr>
     * </table>
     * 
     * <p>In the simplest case, these methods don't need to be 
     * reimplemented by a derived class. <b>However, please note 
     * that the correct operation of their default implementations
     * is CONTINGENT upon the derived class' ability to provide
     * the iterators that access all vertices/edges via getAllVertices()
     * and getAllEdges() methods.</b> Thus, every derived class must 
     * provide appropriate iterators to ensure that the default
     * vertex/edge location works.
     * 
     * @param x x-coordinate of the target position
     * @param y y-coordinate of the target position
     * @return Reference to a <code><b>Vertex</b></code> or
     *         <code><b>null</b></code>.
     */
    public Vertex getVertexAt(double x, double y)
    {
        if(_spacePartition != null)
        {
            // 'normal' mode
            return _spacePartition.getVertexAt(x,y);
        }
        else
        {
            // Fall-back section
            
            /* Perform 'naive' linear search for 
             * a vertex containing the specified position.
             * O(n)
             */
            
            Vertex vx;
            Iterator<Vertex> itr = getAllVertices();
            while(itr.hasNext())
            {
                vx = itr.next();                
                if(vx.containsPoint(x,y))
                {
                    return vx;
                }         
            }        
            return null;            
        }
    }
   
    //*************************************************************************
    
    /**
     * Wrapper for {@link Graph#getVertexAt(double, double)}, takes a
     * Point2D argument instead of individual coordinates.
     * 
     * @param position a point to be checked
     * @see Graph#getVertexAt(double, double)
     */
    public Vertex getVertexAt(Point2D position)
    {
        return getVertexAt(position.getX(), position.getY());
    }
    
    //*************************************************************************
   

    /**
     * Same as above, but in 3-space. <br>
     * 
     * <p>
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
     * @see Graph#getVertexAt(double, double)
     */
    public Vertex getVertexAt(double x, double y, double z)
    {
        if(_spacePartition != null)
        {
            // normal mode
            return _spacePartition.getVertexAt(x,y,z);
        }
        else
        {
            // fallback mode
            Vertex vx;
            Iterator<Vertex> itr = getAllVertices();
            while(itr.hasNext())
            {
                vx = itr.next();        
                if(vx.containsPoint(x,y,z))
                {
                    return vx;
                }         
            }        
            return null;
        }      
    }
    
    //*************************************************************************
    
    /**
     * Wrapper for {@link Graph#getNearestVertex(double, double)},
     * takes a Point2D argument instead of individual coordinates.
     * 
     * @param position a point to be checked
     * @see Graph#getNearestVertex(double, double)
     */
    
    public Vertex getNearestVertex(Point2D position)
    {
        return getNearestVertex(position.getX(), position.getY());
    }
      
    //*************************************************************************
   
    /**
     * Retrieves the vertex which is closest to the specified
     * position, in terms of Euclidian distance.
     * 
     * @param x x-coordinate of the target position
     * @param y y-coordinate of the target position
     * @return Reference to a <code><b>Vertex</b></code> or
     *         <code><b>null</b></code> if the data structure is
     *         empty.
     */
    public Vertex getNearestVertex(double x, double y)
    {
        if(_spacePartition != null)
        {
            return _spacePartition.getNearestVertex(x,y);
        }
        else
        {            
            /* fallback mode
             * Perform simple O(n) minimum-distance search
             */
            
            Vertex vx, nearest_vertex;
            double distance, nearest_distance;
            
            Iterator<Vertex> itr = getAllVertices();
            
            nearest_vertex   = itr.next();
            nearest_distance = nearest_vertex.distance(x,y);
                        
            while(itr.hasNext())
            {
                vx = itr.next();
                distance = vx.distance(x,y);
                
                //TODO: does this need an epsilon guard?
                if(distance < nearest_distance)
                {
                    nearest_distance = distance; 
                    nearest_vertex = vx;
                }
            }
            return nearest_vertex;
        }       
    }
      
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
    public Vertex getNearestVertex(double x, double y, double z)
    {
        if(_spacePartition != null)
        {
            return _spacePartition.getNearestVertex(x,y,z);
        }
        else
        {
            /* fallback mode
             * Perform simple O(n) minimum-distance search
             */
            
            Vertex vx, nearest_vertex;
            double distance, nearest_distance;
            
            Iterator<Vertex> itr = getAllVertices();
            
            nearest_vertex   = itr.next();
            nearest_distance = nearest_vertex.distance(x,y,z);
                        
            while(itr.hasNext())
            {
                vx = itr.next();
                distance = vx.distance(x,y,z);
                
                //TODO: does this need an epsilon guard?
                if(distance < nearest_distance)
                {
                    nearest_distance = distance; 
                    nearest_vertex = vx;
                }
            }
            return nearest_vertex;
        }      
    }
   
    //*************************************************************************
   
    /**
     * Retrieves the edge whose line segment 'contains' the point with
     * the specified coordinates. If no such edge can be found, this
     * method returns <code><b>null</b></code>.
     * 
     * @param x x-coordinate of the target position
     * @param y y-coordinate of the target position
     * @return Reference to an <code><b>Edge</b></code> or
     *         <code><b>null</b></code>.
     */
    public Edge getEdgeAt(double x, double y)
    {
        if(_spacePartition != null)
        {
            // normal mode
            return _spacePartition.getEdgeAt(x,y);
        }
        else
        {
            // fallback mode
            Edge edge;
            Iterator<Edge> it = this.getAllEdges();
            while(it.hasNext())
            {
                edge = it.next();
                if(edge.containsPoint(x,y))
                {
                    return edge;
                }
            }       
            return null;
        } 
    }
   
    //*************************************************************************
   
    
    /**
     * Wrapper for {@link Graph#getEdgeAt(double, double)}, takes a
     * Point2D argument instead of individual coordinates.
     * 
     * @param position a point to be checked
     * @see Graph#getEdgeAt(double, double)
     */
    
    public Edge getEdgeAt(Point2D position)
    {
        return getEdgeAt(position.getX(), position.getY());
    }
    
    //*************************************************************************
    
    /**
     * Same as above, but in 3-space.
     * 
     * @param x x-coordinate of the target position
     * @param y y-coordinate of the target position
     * @param z z-coordinate of the target position
     * @return Reference to an <code><b>Edge</b></code> or
     *         <code><b>null</b></code>.
     */
    public Edge getEdgeAt(double x, double y, double z)
    {
        if(_spacePartition != null)
        {
            // normal mode
            return _spacePartition.getEdgeAt(x,y,z);
        }
        else
        {
            // fallback mode            
            Edge edge;
            Iterator<Edge> it = getAllEdges();
            while(it.hasNext())
            {
                edge = it.next();
                if(edge.containsPoint(x,y,z))
                {
                    return edge;
                }
            }       
            return null;       
        }  
    }
   
    //*************************************************************************
       
    /**
     * Calcualte the nearest edge to (x,y)
     * 
     * @param x First coordinate
     * @param y Second coordinate
     * @return The nearest edge
     */
    public Edge getNearestEdge(double x, double y)
    {
        if(_spacePartition != null)
        {
            return _spacePartition.getNearestEdge(x,y);
        }
        else
        {
            // TODO: Nearest edge fallback 
            System.err.println("Nearest edge query fallback " +
                               "not implemented yet!");
            
            return null;
        }
    }
        
    //*************************************************************************
    
    /**
     * Calculate the nearest edge to position
     * 
     * @param position The point
     * 
     * @return The nearest edge
     */
    public Edge getNearestEdge(Point2D position)
    {
        return getNearestEdge(position.getX(), position.getY());
    }
    
    //*************************************************************************
    
    /**
     * Calculate the nearest edge to (x,y,z)
     * 
     * @param x First coordinate
     * @param y Second coordinate
     * @param z Third coordinate
     * @return The nearest edge
     */
    public Edge getNearestEdge(double x, double y, double z)
    {
        if(_spacePartition != null)
        {
            return _spacePartition.getNearestEdge(x,y,z);
        }
        else
        {
            // TODO: 3-D Nearest edge fallback 
            System.err.println("Nearest edge query fallback " +
                               "not implemented yet!");
            
            return null;
        }
    }

    //TODO: Convenience wrappers for get*() methods
    //TODO: what about faces? are such methods even necesssary?
   
    /**
     * Returns the bounding rectangle of the graph scene.
     * See the above comment on space partitions in
     * {@link Graph#getVertexAt(double, double)}
     * <br>
     * This function is kind of a 'lazy-update' thing, in the
     * sense that the bounding rectangle is not updated during
     * e.g vertex/edge modification, but only recalculated
     * when it's requested from outside. For this reason,
     * any rectangle returned by this function should be
     * viewed as temporary, i.e. don't try to 'cache' it
     * anywhere - discard it as soon as you're done with it!
     * 
     * @return  The bounding rectangle
     */
    public Rectangle2D getBoundingRectangle()
    {
        if(_spacePartition != null)
        {
            return _spacePartition.getBoundingRectangle();
        }
        else
        {
            // Linear bbox recalculation 
            Rectangle2D bbox = new Rectangle2D.Double();
            
            // Search for min/max coordinates of vertex b'boxes
            Iterator<Vertex> it = getAllVertices();
            Vertex v;
            
            double min_x = 0, min_y = 0, max_x = 0, max_y = 0;
            Rectangle2D temp_box;
            
            if(it.hasNext())
            {
                v = it.next();
                bbox.setRect(v.getBoundingBox());
                
                min_x = bbox.getMinX();
                min_y = bbox.getMinY();
                max_x = bbox.getMaxX();
                max_y = bbox.getMaxY();
            }
            
            while(it.hasNext())
            {
               v = it.next();
               temp_box = v.getBoundingBox();
               
               if(min_x > temp_box.getMinX())
               {
                   min_x = temp_box.getMinX();
               }
               
               if(min_y > temp_box.getMinY())
               {
                   min_y = temp_box.getMinY();
               }
               
               if(max_x < temp_box.getMaxX())
               {
                   max_x = temp_box.getMaxX();
               }
               
               if(max_y < temp_box.getMaxY())
               {
                   max_y = temp_box.getMaxY();
               }
            }
            
            // setup the bounding rectangle
            bbox.setRect(min_x, min_y, max_x - min_x, max_y - min_y);
            
            return bbox;
        }
    }
   
    //--------------------------- Visualization -------------------------------
   
    /**
     * Draws the entire Graph.
     * 
     * <p>
     * A short, but very important rationale for requiring inverted
     * pixel size and inverted font:
     * 
     * <p>
     * <b>Concerning inverted pixel size units:</b>
     * <p>
     * When rendering a graph, is it usually preferrable to hold the
     * vertex radii, as well as other graphic element proportions,
     * constant. However, a Graphics2D rendering context transforms
     * <u>all</u> graphics primitives by the current world-to-screen
     * affine transformation. Thus, if one were to render e.g.
     * vertices as circles with constant radii in world coordinates,
     * their resulting image-space radii would depend on the current
     * "zoom" factor (i.e. the scaling part of the affine transform
     * matrix), i.e the vertices would "grow" or "shrink" depending on
     * zoom.
     * 
     * <p>
     * So, in order to keep their radii <b>constant in image space</b>,
     * one has to "counteract" the affine transform by multiplying
     * their world-coordinate radii by the inverted world-to-screen
     * scaling factor - hence the inverted pixel size parameter. A
     * similar argument applies to font sizes, rectangle sizes, line
     * width/dash parameters and all other geometric elements that can
     * be drawn by a Graphics2D context.
     * 
     * <p>
     * Additionally, some containment test routines, e.g.
     * containsPoint() in Vertex, which is used frequently e.g. for
     * mouse cursor-on-vertex tests, cannot function properly without
     * correct world-coordinate sizes, either!
     * 
     * <p>
     * <b>Concering inverted fonts:</b>
     * <p>
     * Because of the aforementioned behavior of a Graphics2D context,
     * all text being rendered is actually first converted into
     * polygons using the current font attributes, and then
     * transformed by the current world-to-screen affine transform. As
     * the result, if the said affine transform mirrors the
     * Y-coordinate,
     * <p>
     * (as is the case within e.g. SwingFramework package, in order to
     * provide a more familiar coordinate system with the origin at
     * the center of the screen, and positive Y-axis looking "up",
     * instead of "down", as in "normal" Java),
     * </p>
     * it will also draw all the text strings upside down. One very
     * simple way to fix this is create an additional font object
     * (instead of the default one), which is derived from the current
     * system font and is itself mirrored on the Y-axis. All strings
     * drawn using this font will then be mirrored <b>again</b> by
     * the Graphics2D pipeline, and will appear correctly on screen!
     * 
     * For an example on how all of this comes together, look at the
     * <b>JSystemHub class</b>, and the graph scene and editor
     * classes inside the <b>SwingFramework</b> package.
     * <p>
     * Alternatively, if you <b>don't</b> use any of our GUI packages
     * in your application, and also don't have a mirroring
     * world-to-screen transform or zooming, use can just call:<br>
     * 
     * <pre>
     * yourGraphObject.draw(g2d, 1.0, g2d.getFont());
     * </pre>
     * 
     * <br>
     * 
     * @param g2d Java2D graphics object for rendering.
     * @param invertedPixelSize inverted pixel size in world coordinates.
     * @param invertedFont inverted font object.
     */ 
    
    /*
     *  Sync is necessary for now for multiple views of the same
     *  graph :-]
     * 
     */
    
    public synchronized void draw(Graphics2D g2d, 
                                  double invertedPixelSize, 
                                  Font invertedFont)
    {
        //-------------- preliminary rendering routine --------------------
        
        // save original font
        Font last_font = g2d.getFont();
        
        // use 'inverted' font supplied by the rendering system.
        g2d.setFont(invertedFont);
        
        // render edges
        Iterator<Edge> edge_it = this.getAllEdges();
        
        while(edge_it.hasNext())
        {
            edge_it.next().draw(g2d, 
                                    invertedPixelSize, 
                                    invertedFont);
        }
        
        // render vertices
        Iterator<Vertex> vertex_it = this.getAllVertices();
        
        while(vertex_it.hasNext())
        {
            vertex_it.next().draw(g2d, 
                                        invertedPixelSize, 
                                        invertedFont);
        }
        
        // restore original font
        g2d.setFont(last_font);
        
        //------------------------------------------------------------------
        
        //------------------------ Debugging info --------------------------
        
        if(_drawDebugInfo)
        {
            // draw vertex bounding boxes
            
            g2d.setColor(Color.gray);
            
            vertex_it = this.getAllVertices();

            while(vertex_it.hasNext())
            {
                g2d.draw(vertex_it.next().getBoundingBox());
            }
            
            // draw edge bounding boxes
            
            edge_it = this.getAllEdges();
            
            while(edge_it.hasNext())
            {
                g2d.draw(edge_it.next().getBoundingBox());
            }
        }
        
        
    }
    
    //*************************************************************************
    
    /**
     * Draws the scematic of a graph container if the container
     * implements the drawSchematic() method.
     * 
     * @param g Java2D graphics context to be used for rendering.
     * 
     * @param pixelSize current pixel size in world coordinate units.
     * 
     * @param font Y-axis-mirrored font object for rendering labels.
     * 
     * @param viewport current viewport extents in world coordinates.
     */
    public void drawDebug(Graphics2D g, double pixelSize, 
                          Font font, Rectangle2D viewport)
    {
        this.drawSchematic(g, pixelSize, font, viewport);
    }
    
    // --------------------------- Visualization ------------------------------
    
    /**
     * This method is optional and can be used to draw a data
     * structure's 'internal' schematic representation, e.g. edge
     * pointers in a DCEL, for debugging or similar purposes. It is
     * <u>not required</u> for correct operation of a data structure,
     * as it's a purely informational method, so you may leave the
     * implementation empty if you don't intend to visualize the
     * internals of your data structure.
     * 
     * 
     * @param graphics Reference to a <code><b>Graphics2D</b></code>
     *            rendering context.
     * @param pixelSize TODO
     * @param font TODO
     * @param viewport TODO
     */    
    public abstract void drawSchematic(Graphics2D graphics, 
                                       double pixelSize, 
                                       Font font, 
                                       Rectangle2D viewport);
    
    //*************************************************************************
    
    // -------------- temporary debugging methods -----------------------------
    
    /*
    public void listFubarStuff()
    {
        DCEL dd = (DCEL)_graphHandler;
        dd.listFubarStuff();
    }*/
    
    private void dumpVertexXML(Vertex v)
    {
        Format ff = Format.getPrettyFormat();
        ff.setExpandEmptyElements(true);
        
        XMLOutputter out = new XMLOutputter(ff);

        System.out.println(out.outputString(v.convertToXML()) + "\n");
        
        /*
        AbstractIterator<Vertex> it = getAllVertices();
        while(it.hasNext())
        {
            System.out.println(
             out.outputString(it.next().convertToXML()));
        }*/
    }
    
    private void dumpEdgeXML(Edge e)
    {
        Format ff = Format.getPrettyFormat();
        ff.setExpandEmptyElements(true);
        
        XMLOutputter out = new XMLOutputter(ff);

        System.out.println(out.outputString(e.convertToXML()) + "\n");
    }
	
	/*
		Returns a JDOM element with the xml representation of the graph
	*/
	
	public Element convertToXML() {
		Element g = new Element(_XML_GRAPH);
		
		Element directed = new Element(_XML_DIRECTED);
		directed.addContent(String.valueOf(_directed));
		g.addContent(directed);
		
		Element edgeWeighted = new Element(_XML_EDGE_WEIGHTED);
		edgeWeighted.addContent(String.valueOf(_edge_weighted));
		g.addContent(edgeWeighted);
		
		Element vertexWeighted = new Element(_XML_VERTEX_WEIGHTED);
		vertexWeighted.addContent(String.valueOf(_vertex_weighted));
		g.addContent(vertexWeighted);
		
		Element embedded = new Element(_XML_EMBEDDED);
		embedded.addContent(String.valueOf(_embedded));
		g.addContent(embedded);
		
		Element type = new Element(_XML_TYPE);
		type.addContent(_type.toString());
		g.addContent(type);

		
		Element vertices = new Element(_XML_VERTICES);
		Iterator<Vertex> vi = this.getAllVertices();
		while (vi.hasNext())
			vertices.addContent(vi.next().convertToXML());			
		g.addContent(vertices);
		
		Element edges = new Element(_XML_EDGES);
		Iterator<Edge> ei = this.getAllEdges();
		while (ei.hasNext())
			edges.addContent(ei.next().convertToXML());			
		g.addContent(edges);		
		return g;
	}
	
	public static Graph createFromXML(Element xml) {
		boolean directed = false;
		String s = xml.getChildText(_XML_DIRECTED);
		if (s != null) directed = Boolean.valueOf(s);
		
		boolean edgeWeighted = false;
		s = xml.getChildText(_XML_EDGE_WEIGHTED);
		if (s != null) edgeWeighted = Boolean.valueOf(s);
		
		boolean vertexWeighted = false;
		s = xml.getChildText(_XML_VERTEX_WEIGHTED);
		if (s != null) vertexWeighted = Boolean.valueOf(s);

		boolean embedded = false;
		s = xml.getChildText(_XML_EMBEDDED);
		if (s != null) embedded = Boolean.valueOf(s);
		
		GraphType type = GraphType.DCEL;
		s = xml.getChildText(_XML_TYPE);
		if (s != null) type = GraphType.valueOf(s);
		
		Graph graph = CreateGraph(type, directed, edgeWeighted, vertexWeighted, embedded);
		Element vertices = xml.getChild(_XML_VERTICES);
		if (vertices != null) {
			java.util.List vertexList = vertices.getChildren(Vertex._XML_VERTEX_NAME);
			ListIterator li = vertexList.listIterator();
			HashMap<Integer, Vertex> hm = new HashMap<Integer, Vertex>(vertexList.size()+10,1);			
			while (li.hasNext()) {
				Element vxml = (Element)li.next();
				Vertex v = graph.addVertex();
				v.setFromXML(vxml);
				int hash = Integer.valueOf(vxml.getAttributeValue(Vertex._XML_VERTEX_ID));
				hm.put(hash,v);
			}
			Element edges = xml.getChild(_XML_EDGES);
			if (edges != null) {
				java.util.List edgeList = edges.getChildren(Edge._XML_EDGE_NAME);
				li = edgeList.listIterator();			
				while (li.hasNext()) {
					Element exml = (Element)li.next();
					Vertex sv = null;
					Vertex tv = null;
					s = exml.getAttributeValue(Edge._XML_SOURCE_VERTEX);
					if (s != null) {
						int start = Integer.valueOf(s);
						sv = (Vertex)hm.get(start);						
					}
					s = exml.getAttributeValue(Edge._XML_TARGET_VERTEX);
					if (s != null) {
						int target = Integer.valueOf(s);
						tv = (Vertex)hm.get(target);						
					}
					if (sv != null && tv != null) {
						Edge e = graph.addEdge(sv,tv);
						e.setFromXML(exml);
					}
				}					
			}
		}
		return graph;
	}
    
    /**
     * Clones the graph by cloning its vertices and edges and then building a new graph
     */
    public Object clone() {
		Graph newGraph = Graph.CreateGraph(_type, _directed, _edge_weighted, _vertex_weighted, _embedded);
		Vertex orgVertices[] = new Vertex[_nextVertexID];
		Vertex clonedVertices[] = new Vertex[_nextVertexID];
		Iterator<Vertex> av = getAllVertices();
		while (av.hasNext()) {
			Vertex v = av.next();
			orgVertices[v.getID()] = v;
			clonedVertices[v.getID()] = newGraph.addVertex(v);
		}
		Iterator<Edge> ev = this.getAllEdges();
		while (ev.hasNext()) {
			Edge e = ev.next();
			newGraph.addEdge(clonedVertices[e.getSource().getID()], clonedVertices[e.getTarget().getID()]);
		}
		return newGraph;
    }
    
    
    //*************************************************************************
    //                        Protected instance methods
    //*************************************************************************
   
    //*************************************************************************
    //                         Private instance methods
    //*************************************************************************
   
}
