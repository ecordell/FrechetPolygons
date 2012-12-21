/*
 * Created on Jan 14, 2005
 * 
 * JGenericGraphScene.java
 */
package anja.SwingFramework.graph;


import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import anja.geom.*;
import anja.SwingFramework.*;

import anja.graph.*;

import org.jdom.Element;


/**
 * 
 * Code 50% ported to newgraph
 * 
 * TODO: missing methods
 * 
 */
public class JGraphScene
		extends JAbstractScene
{

	//*************************************************************************
	// 				Public constants
	//*************************************************************************

	//*************************************************************************
	// 				Class variables
	//*************************************************************************

	//*************************************************************************
	// 			    Protected instance variables
	//*************************************************************************

	protected Graph	m_Graph;


	//*************************************************************************
	// 			     Private instance variables
	//*************************************************************************

	//*************************************************************************
	// 				  Constructors
	//*************************************************************************

	/**
	 * Creates an empty generic graph scene. The graph is per default undirected
	 * (can be changed later using JGenericGraphEditor) and uses default colors,
	 * i.e. medium-gray vertices with black outlines and medium-gray edges.
	 * 
	 * @param hub
	 *            reference to the system hub object
	 */
	public JGraphScene(
			JSystemHub hub)
	{
		super(hub);

		// temporarily switching to Graph.SIMPLE...
		//m_Graph = new Graph(Graph.SIMPLE, true, false, false, true);

		// structure dir    e.w.   v.w    embedded
		//            |      |      |      |   
		//m_Graph = new Graph(   Graph.GraphType.DCEL, true, false, false, true);
		m_Graph = Graph.CreateGraph(Graph.GraphType.DCEL, true, false, false,
				true);
	}


	//*************************************************************************
	// 			     Public instance methods
	//*************************************************************************

	//------------------- Object manipulation methods -------------------------

	//--------------- Scene query & manipulation methods ----------------------

	/**
	 * Sets the graph mode to directed or undirected.
	 * 
	 * @param directed
	 *            <code>true</code> for directed, <code>false</code> for
	 *            undirected.
	 */
	public void setDirectedGraphMode(
			boolean directed)
	{
		m_Graph.setDirected(directed);
	}


	//*************************************************************************

	/**
	 * Clears the entire graph and sets the "scene changed" flag.
	 * 
	 * @see Graph#clear()
	 */
	public void clear()
	{
		m_Graph.clear();

		_sceneWasChanged = true;
		_hub.getDisplayPanel().repaint();
	}


	//*************************************************************************

	/**
	 * 
	 * @return <code>true</code> if the scene is empty, otherwise
	 *         <code>false</code>
	 * @see Graph#isEmpty()
	 */
	public boolean isEmpty()
	{
		return m_Graph.isEmpty();
	}


	//*************************************************************************

	/**
	 * Renders the graph scene.
	 * 
	 * @param g2d
	 *            An instance of Graphics2D rendering context, supplied
	 *            internally by JSimpleDisplayPanel
	 * 
	 * @see Graph#draw(Graphics2D, double, java.awt.Font)
	 */
	public void draw(
			Graphics2D g2d)
	{

		m_Graph.draw(g2d, 1.0 / _pixelSize, _invertedFont);

		//TODO: any additional drawing code

	}


	//*************************************************************************

	/**
	 * Returns the bounding rectangle of the graph.
	 * 
	 * @return Reference to Rectangle2D
	 */
	public Rectangle2D getBoundingRectangle()
	{
		return null; // stub
		//return m_Graph.getBoundingRectangle();
	}


	//*************************************************************************

	/**
	 * Reimplemented from {@link JAbstractScene#readFromXML(Element)}
	 * 
	 * <p> Reconstructs a graph from a JDOM Element node. <br> Most of the work
	 * is done in {@link Graph#createFromXML(Element)}
	 * 
	 * @param root
	 *            the root node of the JDOM subtree representing the scene
	 */
	public void readFromXML(
			Element root)
	{
		Element graph = root.getChild("Graph");

		m_Graph = Graph.createFromXML(graph);
	}


	//*************************************************************************

	/**
	 * Reimplemented from {@link JAbstractScene#convertToXML()}
	 * 
	 * <br>Converts the scene to JDOM representation. Most of the work is done
	 * in {@link Graph#convertToXML()}
	 * 
	 * @return The root node of the resulting JDOM tree
	 */
	public Element convertToXML()
	{
		Element root = super.convertToXML();
		root.addContent(m_Graph.convertToXML());

		return root;
	}


	//*************************************************************************

	/**
	 * This method is used by
	 * {@link JSystemHub#updateTransform(AffineTransform)}to <br>synchronize the
	 * changes to the view's coordinate system.
	 * 
	 * @param tx
	 *            new affine transform
	 */
	public void updateAffineTransform(
			AffineTransform tx,
			double pixelSize,
			Font invertedFont)
	{
		super.updateAffineTransform(tx, pixelSize, invertedFont);
	}


	//------------ Direct vertex/edge manipulation methods --------------------

	/**
	 * Tests whether the graph contains the specified VisualVertex
	 * 
	 * @param vertex
	 *            vertex to be tested
	 * @return <code>true</code> if the specified vertex is contained in the
	 *         graph, otherwise <code>false</code>
	 * 
	 * @see Graph#contains(Vertex)
	 */
	public boolean contains(
			Vertex vertex)
	{
		return m_Graph.contains(vertex);
	}


	//*************************************************************************

	/**
	 * Tests whether the graph contains the specified VisualEdge
	 * 
	 * @param edge
	 *            edge to be tested
	 * @return <code>true</code> if the specified edge is contained in the
	 *         graph, otherwise <code>false</code>
	 * 
	 * @see Graph#contains(Edge)
	 */
	public boolean contains(
			Edge edge)
	{
		return m_Graph.contains(edge);
	}


	//*************************************************************************

	/**
	 * Adds a vertex to the graph.
	 * 
	 * @param p
	 *            New vertex to be added
	 * 
	 * @see Graph#addVertex(Vertex)
	 */
	public Vertex addVertex(
			Point2D p)
	{
		_sceneWasChanged = true;
		return m_Graph.addVertex(p);

	}


	public Vertex addVertex(
			double x,
			double y)
	{
		_sceneWasChanged = true;
		return m_Graph.addVertex(x, y);
	}


	//*************************************************************************

	/**
	 * Adds an edge to the graph. If a similar edge (i.e. one with same
	 * starting/ending vertices) already exists in the graph, nothing will be
	 * done.
	 * 
	 * @param v1
	 *            First vertex of the new edge
	 * @param v2
	 *            Second vertex of the new edge
	 * 
	 * @see Graph#addEdge(Edge)
	 */
	public Edge addEdge(
			Vertex v1,
			Vertex v2)
	{

		return m_Graph.addEdge(v1, v2);
	}


	//*************************************************************************

	/*
	 * Splits the specified edge e(v1,v2) by replacing e with two
	 * consequtive edges e1(v1,vx) and e2(vx,v2). The direction of the
	 * original edge is preserved.
	 * 
	 * @param ve Edge that is to be splitted.
	 * @param vx A vertex to be inserted into the edge.
	 * 
	 * @see VisualGraph#insertVertexIntoEdge(VisualVertex, VisualEdge)
	 */
	/*
	public void splitEdge(VisualEdge ve, VisualVertex vx)
	{
		m_Graph.insertVertexIntoEdge(vx, ve);
		m_bSceneChanged = true;
	}*/

	//*************************************************************************

	/**
	 * Removes a vertex from the graph.
	 * 
	 * @param vx
	 *            The vertex to be removed.
	 * 
	 * @see Graph#deleteVertex(Vertex)
	 */
	public void removeVertex(
			Vertex vx)
	{
		m_Graph.deleteVertex(vx);
		_sceneWasChanged = true;

		//return false; // stub
		//return m_Graph.remove(vx);				
	}


	//*************************************************************************

	/**
	 * Removes an edge from the graph.
	 * 
	 * @param edge
	 *            The edge to be removed.
	 * 
	 * @see Graph#deleteEdge(Edge)
	 */
	public void removeEdge(
			Edge edge)
	{
		m_Graph.deleteEdge(edge);
		_sceneWasChanged = true;

		//return m_Graph.remove(e);
	}


	//*************************************************************************

	/**
	 * Retrieves a vertex at the specified world-coordinate position.
	 * 
	 * @param point
	 *            The point to be tested
	 * @return An instance of VisualVertex, or <code>null</code> if no vertex
	 *         was found at the specified position.
	 * 
	 * @see Graph#getVertexAt(Point2D)
	 */
	public Vertex getVertexAt(
			Point2D point)
	{
		return m_Graph.getVertexAt(point);
	}


	//*************************************************************************

	/**
	 * Checks whether the specified point falls into the proxy zone of any
	 * vertex in the graph and if true, returns that vertex.
	 * 
	 * @param point
	 *            a point to be inspected
	 * @return a vertex whose proxy zone the point falls into. (is this correct
	 *         English? hmm...). <code>null</code> if no proxy zone has been
	 *         found.
	 * 
	 */
	public Vertex getHotVertex(
			Point2D point)
	{
		if (m_Graph.isEmpty())
		{
			return null;
		}
		else
		{
			// experimental implementation via nearest-neighbour query
			Vertex vx = m_Graph.getNearestVertex(point);

			if (vx.pointInProxyZone(point))
			{
				return vx;
			}

			return null; // no proxy zone tagged...
		}
	}


	//*************************************************************************

	/**
	 * Retrieves an edge that is incident on the specified world-coordinate
	 * position.
	 * 
	 * @param point
	 *            The point to be tested.
	 * @return An instance of VisualEdge, or <code>null</code> if no edge was
	 *         found at the specified position.
	 * 
	 * @see Graph#getEdgeAt(Point2D)
	 */
	public Edge getEdgeAt(
			Point2D point)
	{
		return m_Graph.getEdgeAt(point);
	}


	//*************************************************************************

	/**
	 * Returns the graph mode
	 * 
	 * @return <code>true</code> if the graph is directed, or <code>false</code>
	 *         otherwise.
	 * 
	 * @see Graph#isDirected()
	 */
	public boolean isDirected()
	{
		return m_Graph.isDirected();
	}


	//*************************************************************************

	/**
	 * Provided for convenience, this method allows direct access to the
	 * underlying instance of VisualGraph.
	 * 
	 * @return Instance of Graph
	 */
	public Graph getGraph()
	{
		return m_Graph;
	}


	//*************************************************************************

	/**
	 * Provides an enumeration of the graph's vertices.
	 * 
	 * @return An instance of Enumeration.
	 * 
	 * @see Graph#getAllVertices()
	 */
	public Iterator<Vertex> getVertices()
	{
		return m_Graph.getAllVertices();
	}


	//*************************************************************************

	/**
	 * Provides and enumeration of the graph's edges.
	 * 
	 * @return An instance of Enumeration
	 * @see Graph#getAllEdges()
	 */
	public Iterator<Edge> getEdges()
	{
		return m_Graph.getAllEdges();
	}


	//*************************************************************************

	/**
	 * Returns the number of vertices in the graph
	 * 
	 * @return The number of vertices
	 * @see Graph#getNumVertices()
	 */
	public int getNumVertices()
	{
		return m_Graph.getNumVertices();
	}


	//*************************************************************************

	/**
	 * Returns the number of edges in the graph
	 * 
	 * @return The number of edges
	 * @see Graph#getNumEdges()
	 */
	public int getNumEdges()
	{
		return m_Graph.getNumEdges();
	}


	//*************************************************************************

	/**
	 * Provides a list of edges that are adjacent to the specified vertex.
	 * 
	 * @param vx
	 *            A vertex to be examined
	 * @return An instance of LinkedList
	 */
	public LinkedList getAdjacentEdges(
			Vertex vx)
	{
		LinkedList l = new LinkedList();
		Iterator<Edge> ae = vx.getEdgeIterator(anja.graph.Constants.CLOCKWISE);
		while (ae.hasNext())
			l.add(ae.next());
		return l;
	}


	//*************************************************************************

	/**
	 * Provides a list of edges that are adjacent to the specified edge. (NOT
	 * IMPLEMENTED)
	 * 
	 * @param edge
	 *            A vertex to be examined
	 * @return An instance of LinkedList
	 */
	public LinkedList getAdjacentEdges(
			Edge edge)
	{
		/*
		LinkedList list = new LinkedList();
		
		LinkedList start_list = 
		 getAdjacentEdges((VisualVertex)edge.getStartVertex());
		
		LinkedList target_list = 
		 getAdjacentEdges((VisualVertex)edge.getTargetVertex());
		
		list.addAll(start_list);
		
		if(edge.isSling() == false)
		 list.addAll(target_list);
		
		return list;*/
		return null; // stub
	}

	//*************************************************************************
	// 			     Protected instance methods
	//*************************************************************************

	//*************************************************************************
	// 			      Private instance methods
	//*************************************************************************
}
