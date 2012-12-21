package anja.geom;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Hashtable;
import java.util.Vector;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import anja.util.Drawable;
import anja.util.GraphicsContext;
import anja.util.PriorityQueue;


/**
 * Point2Graph is a drawable graph of Point2 objects. The graph can be directed
 * or undirected, weighted or unweighted. The operations on weighted graphs make
 * use of the <tt>value</tt> method from BasicLine2, so it is possible to use
 * other weights than the euclidian distance. If the graph is set to be a
 * distance graph, the value of an edge is set to its length at the time when
 * the edge is added to the graph.
 * 
 * The graph is stored in a Quad Edge Data Structure (Guibas, Stolfi, 1985) with
 * some extensions from the Winged-Edge structure (Baumgart, 1972; see also
 * Abramowski/Mller, 1991), that allow direct access to points and faces.
 * 
 * <b>IMPORTANT:</b> all query-methods that have a Point2 oder BasicLine2 as
 * input parameter (e.g. contains, degree, ...) expect the original object, that
 * was stored in the graph via add. In the case of points, it is not sufficient
 * to query a point with the same coordinates. If you only have the coordinates,
 * you can get the Point2-object using <tt>getPoint(x, y)</tt>. The actual
 * reason for this is, that hashtables are used to access the objects, that
 * allows access in constant expected running time. The access using
 * <tt>getPoint(x, y)</tt> incurs a running time linear to the number of points
 * stored in the graph.
 * 
 * <b>ACCESS TO THE FACES LEFT AND RIGHT TO EDGES IN NOT IMPLEMENTED YET!! </b>
 * 
 * @version 0.1 11.07.01
 * @author Wolfgang Meiswinkel, Thomas Kamphans
 */

//****************************************************************
public class Point2Graph
		implements Cloneable, Drawable, Serializable
//****************************************************************
{

	//************************************************************
	// public constants
	//************************************************************

	public final static int		SOURCE_POINT	= Point2GraphEdge.SOURCE_POINT;
	public final static int		TARGET_POINT	= Point2GraphEdge.TARGET_POINT;
	public final static int		NONE			= Point2GraphEdge.NONE;
	public final static double	DIJKSTRA_DELTA	= 0.00001;

	//************************************************************
	// private variables
	//************************************************************

	/**
	 * Graph is directed oder not.
	 */
	protected boolean			_isDirected		= false;

	/**
	 * Graph is weighted oder not.
	 */
	protected boolean			_isWeighted		= true;

	/**
	 * Edge labels are set to the length of the egde.
	 */
	protected boolean			_distanceGraph	= true;

	/**
	 * Map points to an arbitrary edge adjacent to the point, hash contains
	 * pairs of type (Point2, Point2GraphEdge).
	 */
	protected Hashtable			_pointToEdge;

	/**
	 * Map segments to QEDS-edges, hash contains pairs of type (BasicLine2,
	 * Point2GraphEdge)
	 */
	protected Hashtable			_segToEdge;

	/**
	 * Map faces to an arbitrary edge adjacent to the face, hash contains pairs
	 * of type (Polygon2, Point2GraphEdge).
	 */
	protected Hashtable			_polyToEdge;
	protected Vector			_vertices;
	protected Vector			_edges;


	//************************************************************
	// constructors
	//************************************************************

	/**
	 * Create an empty Point2Graph object.
	 */
	public Point2Graph()
	{
		_pointToEdge = new Hashtable();
		_segToEdge = new Hashtable();
		_polyToEdge = new Hashtable();
		_vertices = new Vector();
		_edges = new Vector();
	} // Point2Graph


	/**
	 * Create Point2Graph object from the given Point2Graph. All objects
	 * (Points, Segements) are cloned!
	 * 
	 * @param g
	 *            The object to clone
	 */
	public Point2Graph(
			Point2Graph g)
	{
		Point2Graph dummy = (Point2Graph) g.clone();
		_pointToEdge = (Hashtable) dummy._pointToEdge;
		_segToEdge = (Hashtable) dummy._segToEdge;
		_polyToEdge = (Hashtable) dummy._polyToEdge;
		_vertices = (Vector) dummy._vertices;
		_edges = (Vector) dummy._edges;
	} // Point2Graph


	/**
	 * Create Point2Graph from given Point2List with points from Point2List, but
	 * no edges.
	 * 
	 * @param p
	 *            The basic point list, the graph is created from
	 */
	public Point2Graph(
			Point2List p)
	{
		_pointToEdge = new Hashtable();
		_segToEdge = new Hashtable();
		_polyToEdge = new Hashtable();
		_vertices = new Vector();
		_edges = new Vector();
		PointsAccess pa = new PointsAccess(p);
		Point2 new_point = pa.nextPoint();
		while (new_point != null)
		{
			_vertices.addElement(new_point);
			new_point = pa.nextPoint();
		}
	} // Point2Graph


	//************************************************************
	// class methods
	//************************************************************

	//************************************************************
	// public methods
	//************************************************************

	//------------------------------------------------------------
	// Simple methods: add and remove edges and vertices, access
	// labels etc.
	//------------------------------------------------------------

	/**
	 * Add vertex to graph.
	 * 
	 * @param p
	 *            The new vertex
	 */
	public void add(
			Point2 p)
	{
		_vertices.addElement(p);
	} // add


	/**
	 * Add vertice from Point2List to graph.
	 * 
	 * @param p
	 *            The list of points to be added
	 */
	public void add(
			Point2List p)
	{
		PointsAccess pa = new PointsAccess(p);
		Point2 new_point = pa.nextPoint();
		while (new_point != null)
		{
			_vertices.addElement(new_point);
			new_point = pa.nextPoint();
		}
	} // add


	/**
	 * Add edge from point1 to point2 to graph. Points are added to the graph,
	 * if they are not already contained in it.
	 * 
	 * @param point1
	 *            Source of the new edge
	 * @param point2
	 *            Target of the new edge
	 */
	public void add(
			Point2 point1,
			Point2 point2)
	{
		if (point1 == null || point2 == null)
		{
			return; // nothing to add
		}
		Segment2 newEdge = new Segment2(point1, point2);
		this.add(newEdge);
	} // add


	/**
	 * Add edge to graph. Points are added to the graph, if they are not already
	 * contained in it.
	 * 
	 * @param s
	 *            The edge to be added
	 */
	public void add(
			BasicLine2 s)
	{

		boolean source_connect = false;
		boolean target_connect = false;
		boolean source_checked = false;
		boolean target_checked = false;
		Point2GraphEdge toAdd = new Point2GraphEdge(s);
		Point2 source = s.source();
		Point2 target = s.target();
		if (this.isDistanceGraph())
		{
			s.setValue((float) source.distance(target));
		}
		if (!_vertices.contains(source))
		{
			_vertices.addElement(source);
			_pointToEdge.put(source, toAdd);
		}
		else
		{
			if (!_pointToEdge.containsKey(source))
			{
				_pointToEdge.put(source, toAdd);
			}
			else
			{
				source_connect = true;
			}

		}
		if (!_vertices.contains(target))
		{
			_vertices.addElement(target);
			_pointToEdge.put(target, toAdd);
		}
		else
		{
			if (!_pointToEdge.containsKey(target))
			{
				_pointToEdge.put(target, toAdd);
			}
			else
			{
				target_connect = true;
			}
		}
		_segToEdge.put(s, toAdd);
		_edges.addElement(toAdd);

		if (!source_connect && !target_connect)
		{
			return; // no connection ... :)
		}
		if (source_connect)
		{
			double toAdd_angle = source.angle(target);
			Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(source);

			if (first.nextEdge(source) == null)
			{
				if (first.pointEdgeRelation(source) == SOURCE_POINT)
				{
					first.sourcePointNextEdge(toAdd);
					first.sourcePointPrevEdge(toAdd);
					toAdd.sourcePointNextEdge(first);
					toAdd.sourcePointPrevEdge(first);
				}
				if (first.pointEdgeRelation(source) == TARGET_POINT)
				{
					first.targetPointNextEdge(toAdd);
					first.targetPointPrevEdge(toAdd);
					toAdd.sourcePointNextEdge(first);
					toAdd.sourcePointPrevEdge(first);
				}

				source_checked = true;
			}
			Point2GraphEdge aktEdge = first;
			Point2 aktSource = first.line().source();
			Point2 aktTarget = first.line().target();
			double aktLine_angle = aktSource.angle(aktTarget);
			if (aktTarget == source)
			{
				aktLine_angle = aktTarget.angle(aktSource);
			}
			double last_angle = aktLine_angle;

			if (toAdd_angle > aktLine_angle && !source_checked) // search ccw
			{
				Point2GraphEdge last_edge = first;
				Point2GraphEdge nextEdge = first.prevEdge(source);
				aktSource = nextEdge.line().source();
				aktTarget = nextEdge.line().target();
				aktLine_angle = aktSource.angle(aktTarget);
				if (aktTarget == source)
				{
					aktLine_angle = aktTarget.angle(aktSource);
				}
				while (aktLine_angle < toAdd_angle
						&& aktLine_angle > last_angle)
				{
					last_angle = aktLine_angle;
					last_edge = nextEdge;
					nextEdge = nextEdge.prevEdge(source);
					aktSource = nextEdge.line().source();
					aktTarget = nextEdge.line().target();
					aktLine_angle = aktSource.angle(aktTarget);
					if (aktTarget == source)
					{
						aktLine_angle = aktTarget.angle(aktSource);
					}
				}

				if (last_edge.pointEdgeRelation(source) == SOURCE_POINT)
				{
					last_edge.sourcePointPrevEdge(toAdd);
				}
				if (last_edge.pointEdgeRelation(source) == TARGET_POINT)
				{
					last_edge.targetPointPrevEdge(toAdd);
				}

				toAdd.sourcePointPrevEdge(nextEdge);
				toAdd.sourcePointNextEdge(last_edge);

				if (nextEdge.pointEdgeRelation(source) == SOURCE_POINT)
				{
					nextEdge.sourcePointNextEdge(toAdd);
				}
				if (nextEdge.pointEdgeRelation(source) == TARGET_POINT)
				{
					nextEdge.targetPointNextEdge(toAdd);
				}

				source_checked = true;
			}

			if (toAdd_angle <= aktLine_angle && !source_checked) // search cw
			{
				Point2GraphEdge last_edge = first;
				Point2GraphEdge nextEdge = first.nextEdge(source);
				aktSource = nextEdge.line().source();
				aktTarget = nextEdge.line().target();
				aktLine_angle = aktSource.angle(aktTarget);
				if (aktTarget == source)
				{
					aktLine_angle = aktTarget.angle(aktSource);
				}

				while (aktLine_angle > toAdd_angle
						&& aktLine_angle < last_angle)
				{
					last_angle = aktLine_angle;
					last_edge = nextEdge;
					nextEdge = nextEdge.nextEdge(source);
					aktSource = nextEdge.line().source();
					aktTarget = nextEdge.line().target();
					aktLine_angle = aktSource.angle(aktTarget);
					if (aktTarget == source)
					{
						aktLine_angle = aktTarget.angle(aktSource);
					}

				}

				if (last_edge.pointEdgeRelation(source) == SOURCE_POINT)
				{
					last_edge.sourcePointNextEdge(toAdd);
				}
				if (last_edge.pointEdgeRelation(source) == TARGET_POINT)
				{
					last_edge.targetPointNextEdge(toAdd);
				}

				toAdd.sourcePointPrevEdge(last_edge);
				toAdd.sourcePointNextEdge(nextEdge);
				if (nextEdge.pointEdgeRelation(source) == SOURCE_POINT)
				{
					nextEdge.sourcePointPrevEdge(toAdd);
				}
				if (nextEdge.pointEdgeRelation(source) == TARGET_POINT)
				{
					nextEdge.targetPointPrevEdge(toAdd);
				}

				source_checked = true;

			}
		}

		if (target_connect)
		{
			double toAdd_angle = target.angle(source);
			Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(target);
			if (first.nextEdge(target) == null)
			{
				if (first.pointEdgeRelation(target) == SOURCE_POINT)
				{
					first.sourcePointNextEdge(toAdd);
					first.sourcePointPrevEdge(toAdd);
					toAdd.targetPointNextEdge(first);
					toAdd.targetPointPrevEdge(first);
				}
				if (first.pointEdgeRelation(target) == TARGET_POINT)
				{
					first.targetPointNextEdge(toAdd);
					first.targetPointPrevEdge(toAdd);
					toAdd.targetPointNextEdge(first);
					toAdd.targetPointPrevEdge(first);
				}

				target_checked = true;
			}
			Point2GraphEdge aktEdge = first;
			Point2 aktSource = first.line().source();
			Point2 aktTarget = first.line().target();
			double aktLine_angle = aktSource.angle(aktTarget);
			if (aktTarget == target)
			{
				aktLine_angle = aktTarget.angle(aktSource);
			}
			double last_angle = aktLine_angle;
			if (toAdd_angle > aktLine_angle && !target_checked) // search ccw
			{
				Point2GraphEdge last_edge = first;
				Point2GraphEdge nextEdge = first.prevEdge(target);
				aktSource = nextEdge.line().source();
				aktTarget = nextEdge.line().target();
				aktLine_angle = aktSource.angle(aktTarget);
				if (aktTarget == target)
				{
					aktLine_angle = aktTarget.angle(aktSource);
				}
				while (aktLine_angle < toAdd_angle
						&& aktLine_angle > last_angle)
				{
					last_angle = aktLine_angle;
					last_edge = nextEdge;
					nextEdge = nextEdge.prevEdge(target);
					aktSource = nextEdge.line().source();
					aktTarget = nextEdge.line().target();
					aktLine_angle = aktSource.angle(aktTarget);
					if (aktTarget == target)
					{
						aktLine_angle = aktTarget.angle(aktSource);
					}
				}

				if (last_edge.pointEdgeRelation(target) == SOURCE_POINT)
				{
					last_edge.sourcePointPrevEdge(toAdd);
				}
				if (last_edge.pointEdgeRelation(target) == TARGET_POINT)
				{
					last_edge.targetPointPrevEdge(toAdd);
				}

				toAdd.targetPointPrevEdge(nextEdge);
				toAdd.targetPointNextEdge(last_edge);

				if (nextEdge.pointEdgeRelation(target) == SOURCE_POINT)
				{
					nextEdge.sourcePointNextEdge(toAdd);
				}
				if (nextEdge.pointEdgeRelation(target) == TARGET_POINT)
				{
					nextEdge.targetPointNextEdge(toAdd);
				}

				target_checked = true;
			}

			if (toAdd_angle <= aktLine_angle && !target_checked) // search cw
			{
				Point2GraphEdge last_edge = first;
				Point2GraphEdge nextEdge = first.nextEdge(target);
				aktSource = nextEdge.line().source();
				aktTarget = nextEdge.line().target();
				aktLine_angle = aktSource.angle(aktTarget);
				if (aktTarget == target)
				{
					aktLine_angle = aktTarget.angle(aktSource);
				}
				while (aktLine_angle > toAdd_angle
						&& aktLine_angle < last_angle)
				{
					last_angle = aktLine_angle;
					last_edge = nextEdge;
					nextEdge = nextEdge.nextEdge(target);
					aktSource = nextEdge.line().source();
					aktTarget = nextEdge.line().target();
					aktLine_angle = aktSource.angle(aktTarget);
					if (aktTarget == target)
					{
						aktLine_angle = aktTarget.angle(aktSource);
					}
				}

				if (last_edge.pointEdgeRelation(target) == SOURCE_POINT)
				{
					last_edge.sourcePointNextEdge(toAdd);
				}
				if (last_edge.pointEdgeRelation(target) == TARGET_POINT)
				{
					last_edge.targetPointNextEdge(toAdd);
				}

				toAdd.targetPointPrevEdge(last_edge);
				toAdd.targetPointNextEdge(nextEdge);
				if (nextEdge.pointEdgeRelation(target) == SOURCE_POINT)
				{
					nextEdge.sourcePointPrevEdge(toAdd);
				}
				if (nextEdge.pointEdgeRelation(target) == TARGET_POINT)
				{
					nextEdge.targetPointPrevEdge(toAdd);
				}

				target_checked = true;

			}

		}

	} // add


	/**
	 * Add given graph. Points and edges are added to the graph, if they are not
	 * already contained in it.
	 * 
	 * @param g
	 *            The graph to be added
	 */
	public void add(
			Point2Graph g)
	{
		int max = g._edges.size() - 1;
		int counter = 0;
		max = g._edges.size() - 1;
		counter = 0;
		while (counter <= max)
		{
			BasicLine2 newEdge = (BasicLine2) g._edges.elementAt(counter);
			this.add(newEdge);
			counter++;
		}
		max = g._vertices.size() - 1;
		counter = 0;
		while (counter <= max)
		{
			Point2 newPoint = (Point2) g._vertices.elementAt(counter);
			this.add(newPoint);
			counter++;
		}
	} // add


	/**
	 * Remove vertex and all edges, that are adjacent to it, from graph.
	 * 
	 * @param p
	 *            The vertex to be removed
	 */
	public void remove(
			Point2 p)
	{
		if (!_pointToEdge.containsKey(p))
		{
			int index = _vertices.indexOf(p);
			_vertices.removeElementAt(index);
		}
		else
		{
			Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
			Point2GraphEdge next = first.nextEdge(p);

			while (next != first && next != null)
			{
				this.remove(next.line());
				next = next.nextEdge(p);
			}
			this.remove(first.line());
			int index = _vertices.indexOf(p);
			_vertices.removeElementAt(index);
		}
	}


	/**
	 * Remove vertices in Point2List and all edges, that are adjacent to them.
	 * 
	 * @param pl
	 *            The list of vertices to be removed
	 */
	public void remove(
			Point2List pl)
	{
		PointsAccess pa = new PointsAccess(pl);
		Point2 _point = pa.nextPoint();
		while (_point != null)
		{
			this.remove(_point);
			_point = pa.nextPoint();
		}
	} // remove


	/**
	 * Remove edge from point1 to point2 from graph. The vertices are not
	 * removed.
	 * 
	 * @param point1
	 *            The source of the edge
	 * @param point2
	 *            The target of the edge
	 */
	public void remove(
			Point2 point1,
			Point2 point2)
	{
		if (point1 == null || point2 == null)
		{
			return;
		}
		if (this.contains(point1) == false || this.contains(point2) == false)
		{
			return;
		}
		Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(point1);
		Point2GraphEdge test = (Point2GraphEdge) _pointToEdge.get(point1);
		if (test.target() == point2)
		{
			BasicLine2 _edge = test.line();
			this.remove(_edge);
			return;
		}
		test = test.nextEdge(point1);
		while (test.target() != point2 && test != first)
		{
			test = test.nextEdge(point1);
		}
		if (test != first && test.target() == point2)
		{
			BasicLine2 _edge = test.line();
			this.remove(_edge);
			return;
		}
		return;
	} // remove


	/**
	 * Remove edge from graph. The vertices are not removed.
	 * 
	 * @param s
	 *            The edge to be removed
	 */
	public void remove(
			BasicLine2 s)
	{
		if (!_segToEdge.containsKey(s))
		{
			return;
		}
		Point2GraphEdge toRemove = (Point2GraphEdge) _segToEdge.get(s);
		Point2 source = toRemove.source();
		Point2 target = toRemove.target();
		Point2GraphEdge sourceNext = toRemove._spnext;
		Point2GraphEdge sourcePrev = toRemove._spprev;
		Point2GraphEdge targetNext = toRemove._tpnext;
		Point2GraphEdge targetPrev = toRemove._tpprev;
		// update pointer
		if (sourceNext != null && sourcePrev != null
				&& sourcePrev != sourceNext)
		{
			if (sourceNext.pointEdgeRelation(source) == SOURCE_POINT)
			{
				sourceNext.sourcePointPrevEdge(sourcePrev);
			}
			if (sourceNext.pointEdgeRelation(source) == TARGET_POINT)
			{
				sourceNext.targetPointPrevEdge(sourcePrev);
			}
			if (sourcePrev.pointEdgeRelation(source) == SOURCE_POINT)
			{
				sourcePrev.sourcePointNextEdge(sourceNext);
			}
			if (sourcePrev.pointEdgeRelation(source) == TARGET_POINT)
			{
				sourcePrev.targetPointNextEdge(sourceNext);
			}
		}
		if (sourceNext != null && sourceNext == sourcePrev)
		{
			if (sourceNext.pointEdgeRelation(source) == SOURCE_POINT)
			{
				sourceNext.sourcePointPrevEdge(null);
			}
			if (sourceNext.pointEdgeRelation(source) == TARGET_POINT)
			{
				sourceNext.targetPointPrevEdge(null);
			}
			if (sourcePrev.pointEdgeRelation(source) == SOURCE_POINT)
			{
				sourcePrev.sourcePointNextEdge(null);
			}
			if (sourcePrev.pointEdgeRelation(source) == TARGET_POINT)
			{
				sourcePrev.targetPointNextEdge(null);
			}
		}

		if (targetNext != null && targetPrev != null
				&& targetPrev != targetNext)
		{
			if (targetNext.pointEdgeRelation(target) == TARGET_POINT)
			{
				targetNext.targetPointPrevEdge(targetPrev);
			}
			if (targetNext.pointEdgeRelation(target) == SOURCE_POINT)
			{
				targetNext.sourcePointPrevEdge(targetPrev);
			}
			if (targetPrev.pointEdgeRelation(target) == TARGET_POINT)
			{
				targetPrev.targetPointNextEdge(targetNext);
			}
			if (targetPrev.pointEdgeRelation(target) == SOURCE_POINT)
			{
				targetPrev.sourcePointNextEdge(targetNext);
			}
		}
		if (targetNext != null && targetNext == targetPrev)
		{
			if (targetNext.pointEdgeRelation(target) == TARGET_POINT)
			{
				targetNext.targetPointPrevEdge(null);
			}
			if (targetNext.pointEdgeRelation(target) == SOURCE_POINT)
			{
				targetNext.sourcePointPrevEdge(null);
			}
			if (targetPrev.pointEdgeRelation(target) == TARGET_POINT)
			{
				targetPrev.targetPointNextEdge(null);
			}
			if (targetPrev.pointEdgeRelation(target) == SOURCE_POINT)
			{
				targetPrev.sourcePointNextEdge(null);
			}
		}

		// check points
		Point2GraphEdge sourceedge = (Point2GraphEdge) _pointToEdge.get(source);
		Point2GraphEdge targetedge = (Point2GraphEdge) _pointToEdge.get(target);
		if (sourceedge == toRemove)
		{
			_pointToEdge.remove(source);
			if (toRemove.sourcePointNextEdge() != null)
			{
				_pointToEdge.put(source, toRemove.sourcePointNextEdge());
			}
			else
			{
				if (toRemove.sourcePointPrevEdge() != null)
				{
					_pointToEdge.put(source, toRemove.sourcePointPrevEdge());
				}
			}
		}
		if (targetedge == toRemove)
		{
			_pointToEdge.remove(target);
			if (toRemove.targetPointNextEdge() != null)
			{
				_pointToEdge.put(target, toRemove.targetPointNextEdge());
			}
			else
			{
				if (toRemove.targetPointPrevEdge() != null)
				{
					_pointToEdge.put(target, toRemove.targetPointPrevEdge());
				}
			}
		}
		Point2GraphEdge removeEdge = (Point2GraphEdge) _segToEdge.get(s);
		_segToEdge.remove(s);
		int removeindex = _edges.indexOf(removeEdge);
		_edges.removeElementAt(removeindex);
	} // remove


	/**
	 * Remove subgraph <tt>g</tt> from graph, i.e. remove all vertices from
	 * <tt>g</tt> and all edges adjacent to that vertices.
	 * 
	 * @param g
	 *            The subgraph to be removed
	 */
	public void remove(
			Point2Graph g)
	{
		// no points => no edges !
		int max = g._vertices.size() - 1;
		int counter = 0;
		while (counter <= max)
		{
			Point2 toRemove = (Point2) g._vertices.elementAt(counter);
			this.remove(toRemove);
			counter++;
		}
	} // remove


	/**
	 * Set the graph to be directed.
	 */
	public void setDirected()
	{
		_isDirected = true;
	} // setDirected


	/**
	 * Set the graph to be undirected (default).
	 */
	public void setUndirected()
	{
		_isDirected = false;
	} // setUndirected


	/**
	 * Return true, iff graph is set to ''directed''. This returns only the
	 * directed-flag of the graph.
	 */
	public boolean isDirected()
	{
		return _isDirected;
	} // isDirected


	/**
	 * Set the graph to be weighted (default).
	 */
	public void setWeighted()
	{
		_isWeighted = true;
	} // setWeighted


	/**
	 * Set the graph to be unweighted. Also calls <tt>setNoDistanceGraph()</tt>.
	 * 
	 * @see #setNoDistanceGraph()
	 */
	public void setUnweighted()
	{
		_isWeighted = false;
		setNoDistanceGraph();
	} // setUnweighted


	/**
	 * Return true, iff graph is set to weighted.
	 */
	public boolean isWeighted()
	{
		return _isWeighted;
	} // isWeighted


	/**
	 * Set the graph to be a distance graph, i.e. use the length of edges their
	 * weights for operations on weighted graphs. User defined values of edges
	 * are overwritten at the time, the edge is added to the graph! This setting
	 * is the default. If <tt>setUnweighted</tt> is called, this will be set to
	 * false, too.
	 * 
	 * @see #setNoDistanceGraph()
	 */
	public void setDistanceGraph()
	{
		_distanceGraph = true;
	} // setDistanceGraph


	/**
	 * Use user defined labels for edges.
	 * 
	 * @see #setDistanceGraph()
	 */
	public void setNoDistanceGraph()
	{
		_distanceGraph = false;
	} // setNoDistanceGraph


	/**
	 * Return true, if edge labels are set to length of the edge.
	 * 
	 * @return true if labels are set, false else
	 */
	public boolean isDistanceGraph()
	{
		return _distanceGraph;
	} // isDistanceGraph


	//------------------------------------------------------------
	// Access to a graph:
	//------------------------------------------------------------

	/**
	 * Return true, if graph is empty.
	 * 
	 * @return true if empty, false else
	 */
	public boolean isEmpty()
	{
		return _vertices.size() == 0;
	} // isEmpty


	/**
	 * Return number of vertices.
	 * 
	 * @return The number of vertices
	 */
	public int numOfPoints()
	{
		return _vertices.size();
	} // numOfPoints


	/**
	 * Return all vertices.
	 * 
	 * @return A list of all vertices
	 */
	public Point2List getPoints()
	{
		int max = _vertices.size() - 1;
		int counter = 0;
		Point2List returnList = new Point2List();
		while (counter <= max)
		{
			Point2 nextPoint = (Point2) _vertices.elementAt(counter);
			counter++;
			returnList.addPoint(nextPoint);
		}
		return returnList;
	} // getPoints


	//------------------------------------------------------------
	// Access to an edge:
	//------------------------------------------------------------

	/**
	 * Return number of edges.
	 * 
	 * @return The number of edges
	 */
	public int numOfEdges()
	{
		return _edges.size();
	} // numOfEdges


	/**
	 * Return true, if edge is contained in the graph.
	 * 
	 * @param s
	 *            The line
	 * 
	 * @return true if the edge is contained, false else
	 */
	public boolean contains(
			BasicLine2 s)
	{
		return _segToEdge.containsKey(s);
	} // contains


	/**
	 * Return true, if edge is contained in the graph.
	 * 
	 * @param s
	 *            The edge
	 * 
	 * @return true if the graph contains the edge, false else
	 */
	public boolean contains(
			Point2GraphEdge s)
	{
		return _edges.contains(s);
	} // contains


	/**
	 * Calls setWhight() with the same parameters
	 * 
	 * @param s
	 *            The line
	 * @param weight
	 *            The weight
	 */
	public void setWeight(
			BasicLine2 s,
			double weight)
	{
		setWhight(s, weight);
	} // setWeight


	/**
	 * Set user defined weight. Only possible if isDistanceGraph()==false
	 * 
	 * @param s
	 *            The line
	 * @param whight
	 *            The weight
	 */
	public void setWhight(
			BasicLine2 s,
			double whight)
	{
		if (!isDistanceGraph())
		{
			s.setValue((float) whight);
		}
	} // setWhight


	/**
	 * Calls getWhight(s) and returns the result
	 * 
	 * @param s
	 *            The edge (if contained)
	 * 
	 * @return The weight of s if contained or NEGATIVE_INFINITY
	 */
	public double getWeight(
			BasicLine2 s)
	{
		return getWhight(s);
	} // getWeight


	/**
	 * Return NEGATIVE_INFINITY if s is not known.
	 * 
	 * @param s
	 *            The edge (if contained)
	 * 
	 * @return The weight of s if contained or NEGATIVE_INFINITY
	 */
	public double getWhight(
			BasicLine2 s)
	{
		if (_segToEdge.containsKey(s))
		{
			return s.getValue();
		}
		return Double.NEGATIVE_INFINITY;
	} // getWhight


	/**
	 * Return all edges
	 * 
	 * @return Array of all edges
	 */
	public BasicLine2[] getAllEdges()
	{
		BasicLine2[] return_array = new BasicLine2[_edges.size()];
		for (int cnt = 0; cnt < _edges.size(); cnt++)
		{
			Point2GraphEdge nextEdge = (Point2GraphEdge) _edges.elementAt(cnt);
			return_array[cnt] = nextEdge.line();
		}
		return return_array;
	}


	/**
	 * Return all edges
	 * 
	 * @return Array of all graph edges
	 */
	public Point2GraphEdge[] getAllGraphEdges()
	{
		Point2GraphEdge[] return_array = new Point2GraphEdge[_edges.size()];
		for (int cnt = 0; cnt < _edges.size(); cnt++)
		{
			Point2GraphEdge nextEdge = (Point2GraphEdge) _edges.elementAt(cnt);
			return_array[cnt] = nextEdge;
		}
		return return_array;
	}


	//------------------------------------------------------------
	// Access to a vertex:
	//------------------------------------------------------------

	/**
	 * Return Point2 object, if a point with coordinates (<i>x, y</i>) is
	 * contained in the graph. Otherwise return null.
	 * 
	 * @param x
	 *            x value of the vertex/point
	 * @param y
	 *            y value of the vertex/point
	 * 
	 * @return The point object, if the object exists, null else
	 */
	public Point2 getPoint(
			float x,
			float y)
	{
		int counter = 0;
		while (counter < _vertices.size() - 1)
		{
			Point2 testPoint = (Point2) _vertices.elementAt(counter);
			if (testPoint != null)
			{
				float tpx = testPoint.x;
				float tpy = testPoint.y;
				if (x == tpx && y == tpy)
				{
					return (Point2) _vertices.elementAt(counter);
				}
			}
			counter++;
		}
		return null;
	} // getPoint


	/**
	 * Return Point2 object(s), if a point (or points) with coordinates
	 * (<i>x,y</i>) is contained in the graph. Otherwise return empty vector.
	 * 
	 * @param x
	 *            x value of the vertex/point
	 * @param y
	 *            y value of the vertex/point
	 * 
	 * @return Vector with the result or empty
	 */
	public Vector getMultiPoint(
			float x,
			float y)
	//============================================================
	{
		Vector result = new Vector();
		int counter = 0;
		while (counter < _vertices.size() - 1)
		{
			Point2 testPoint = (Point2) _vertices.elementAt(counter);
			if (testPoint != null)
			{
				float tpx = testPoint.x;
				float tpy = testPoint.y;
				if (x == tpx && y == tpy)
				{
					result.addElement((Point2) _vertices.elementAt(counter));
				}
			}
			counter++;
		}
		return result;
	}


	/**
	 * Return true, if Point2 <tt>p</tt> is contained in the graph. See comment
	 * to the class concerning query-methods.
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return true if contained, false else
	 */
	public boolean contains(
			Point2 p)
	{
		return _vertices.contains(p);
	}


	/**
	 * Return number of edges adjacent to vertex <tt>p</tt>.
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return the degree of p
	 */
	public int degree(
			Point2 p)
	{
		if (!_pointToEdge.containsKey(p))
		{
			return 0;
		}
		int counter = 1;
		Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
		Point2GraphEdge nextedge = first.nextEdge(p);
		while (nextedge != null && nextedge != first)
		{
			counter++;
			nextedge = nextedge.nextEdge(p);
		}
		return counter;
	} // degree


	/**
	 * Return number of edges starting in vertex <tt>p</tt>. If the graph is
	 * undirected, this is the same as <tt>degree(p)</tt>.
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return The out-degree of p
	 */
	public int outDegree(
			Point2 p)
	{
		if (!_pointToEdge.containsKey(p))
		{
			return 0;
		}
		if (!this.isDirected())
		{
			return degree(p);
		}
		int counter = 0;
		Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
		if (first.source() == p)
		{
			counter++;
		}
		Point2GraphEdge nextedge = first.nextEdge(p);
		while (nextedge != null && nextedge != first)
		{
			if (nextedge.source() == p)
			{
				counter++;
			}
			nextedge = nextedge.nextEdge(p);
		}

		return counter;

	} // outDegree


	/**
	 * Return number of edges ending in vertex <tt>p</tt>. If the graph is
	 * undirected, this is the same as <tt>degree(p)</tt>.
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return The in-degree of p
	 */
	public int inDegree(
			Point2 p)
	{
		if (!_pointToEdge.containsKey(p))
		{
			return 0;
		}
		if (!this.isDirected())
		{
			return degree(p);
		}
		int counter = 0;
		Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
		if (first.target() == p)
		{
			counter++;
		}
		Point2GraphEdge nextedge = first.nextEdge(p);
		while (nextedge != null && nextedge != first)
		{
			if (nextedge.target() == p)
			{
				counter++;
			}
			nextedge = nextedge.nextEdge(p);
		}

		return counter;
	} // inDegree


	/**
	 * Return all edges adjacent to <tt>p</tt>.
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return Array of all edges adjacent to p
	 */
	public BasicLine2[] getEdges(
			Point2 p)
	{
		Vector theEdges = new Vector();
		if (_pointToEdge.containsKey(p) == false)
		{
			BasicLine2[] return_array = new BasicLine2[0];
			return return_array;
		}
		else
		{
			Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
			theEdges.addElement(first.line());
			if (first.nextEdge(p) == null)
			{
				BasicLine2[] return_array = new BasicLine2[1];
				return_array[0] = first.line();
				return return_array;
			}
			else
			{
				Point2GraphEdge next_edge = first.nextEdge(p);
				while (next_edge.nextEdge(p) != first)
				{
					theEdges.addElement(next_edge.line());
					next_edge = next_edge.nextEdge(p);
				}
				theEdges.addElement(next_edge.line());
				BasicLine2[] return_array = new BasicLine2[theEdges.size()];
				for (int i = 0; i <= theEdges.size() - 1; i++)
				{
					return_array[i] = (BasicLine2) theEdges.elementAt(i);
				}
				return return_array;
			}
		}

	} // getEdges


	/**
	 * Return all edges starting in <tt>p</tt>. If the graph is undirected, this
	 * is the same as <tt>getEdges(p)</tt>.
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return Array of all outgoing edges adjacent to p
	 */
	public BasicLine2[] getOutEdges(
			Point2 p)
	{
		if (!this.isDirected())
		{
			return this.getEdges(p);
		}
		Vector theEdges = new Vector();
		if (_pointToEdge.containsKey(p) == false)
		{
			BasicLine2[] return_array = new BasicLine2[0];
			return return_array;
		}
		else
		{
			Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
			if (first.source() == p)
			{
				theEdges.addElement(first.line());
			}
			if (first.nextEdge(p) == null)
			{
				if (first.source() == p)
				{
					BasicLine2[] return_array = new BasicLine2[1];
					return_array[0] = first.line();
					return return_array;
				}
				else
				{
					BasicLine2[] return_array = new BasicLine2[0];
					return return_array;
				}
			}
			else
			{
				Point2GraphEdge next_edge = first.nextEdge(p);
				while (next_edge.nextEdge(p) != first)
				{
					if (next_edge.source() == p)
					{
						theEdges.addElement(next_edge.line());
					}
					next_edge = next_edge.nextEdge(p);
				}
				if (next_edge.source() == p)
				{
					theEdges.addElement(next_edge.line());
				}
				BasicLine2[] return_array = new BasicLine2[theEdges.size()];
				for (int i = 0; i <= theEdges.size() - 1; i++)
				{
					return_array[i] = (BasicLine2) theEdges.elementAt(i);
				}
				return return_array;
			}
		}
	} // getOutEdges


	/**
	 * Return all edges ending in <tt>p</tt>. If the graph is undirected, this
	 * is the same as <tt>getEdges(p)</tt>.
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return Array of all edges ending in p
	 */
	public BasicLine2[] getInEdges(
			Point2 p)
	{
		if (!this.isDirected())
		{
			return this.getEdges(p);
		}
		Vector theEdges = new Vector();
		if (_pointToEdge.containsKey(p) == false)
		{
			BasicLine2[] return_array = new BasicLine2[0];
			return return_array;
		}
		else
		{
			Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
			if (first.target() == p)
			{
				theEdges.addElement(first.line());
			}
			if (first.nextEdge(p) == null)
			{
				if (first.target() == p)
				{
					BasicLine2[] return_array = new BasicLine2[1];
					return_array[0] = first.line();
					return return_array;
				}
				else
				{
					BasicLine2[] return_array = new BasicLine2[0];
					return return_array;
				}
			}
			else
			{
				Point2GraphEdge next_edge = first.nextEdge(p);
				while (next_edge.nextEdge(p) != first)
				{
					if (next_edge.target() == p)
					{
						theEdges.addElement(next_edge.line());
					}
					next_edge = next_edge.nextEdge(p);
				}
				if (next_edge.target() == p)
				{
					theEdges.addElement(next_edge.line());
				}
				BasicLine2[] return_array = new BasicLine2[theEdges.size()];
				for (int i = 0; i <= theEdges.size() - 1; i++)
				{
					return_array[i] = (BasicLine2) theEdges.elementAt(i);
				}
				return return_array;
			}
		}
	} // getInEdges


	/**
	 * Return true if graph is undirected and a line source <br>-> target OR
	 * target <br>-> source exsists. Return true if graph is directed and the
	 * line source <br>-> target exsists.
	 * 
	 * @param source
	 *            First vertex
	 * @param target
	 *            Second vertex
	 * 
	 * @return true if the graph is directed and source and target are
	 *         connected, false else
	 */
	public boolean isConnected(
			Point2 source,
			Point2 target)
	{
		if (!_pointToEdge.containsKey(source)
				|| !_pointToEdge.containsKey(target))
		{
			return false;
		}
		Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(source);
		if (first.target() == target)
		{
			return true;
		}
		if (!this.isDirected())
		{
			if (first.target() == source && first.source() == target)
			{
				return true;
			}
		}
		Point2GraphEdge next = first.nextEdge(source);
		while (next != null && next != first)
		{
			if (next.target() == target)
			{
				return true;
			}
			if (!this.isDirected())
			{
				if (next.target() == source && next.source() == target)
				{
					return true;
				}
			}
			next = next.nextEdge(source);
		}

		return false;
	}


	/**
	 * Return all edges adjacent to <tt>p</tt>. For convenience, these edges are
	 * casted to Segment2. WARNING: This is only applicable, if the graph
	 * consists only of edges of type Segment2!!!
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return Array of segments adjacent to p
	 */
	public Segment2[] getSegments(
			Point2 p)
	{
		Vector theEdges = new Vector();
		if (_pointToEdge.containsKey(p) == false)
		{
			Segment2[] return_array = new Segment2[0];
			return return_array;
		}
		else
		{
			Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
			theEdges.addElement(first.line());
			if (first.nextEdge(p) == null)
			{
				Segment2[] return_array = new Segment2[1];
				return_array[0] = (Segment2) first.line();
				return return_array;
			}
			else
			{
				Point2GraphEdge next_edge = first.nextEdge(p);
				while (next_edge.nextEdge(p) != first)
				{
					theEdges.addElement(next_edge.line());
					next_edge = next_edge.nextEdge(p);
				}
				theEdges.addElement(next_edge.line());
				Segment2[] return_array = new Segment2[theEdges.size()];
				for (int i = 0; i <= theEdges.size() - 1; i++)
				{
					return_array[i] = (Segment2) theEdges.elementAt(i);
				}
				return return_array;
			}
		}
	} // getSegments


	/**
	 * Return all edges starting in <tt>p</tt>. If the graph is undirected, this
	 * is the same as <tt>getSegments(p)</tt>. For convenience, these edges are
	 * casted to Segment2. WARNING: This is only applicable, if the graph
	 * consists only of edges of type Segment2!!!
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return Array of outgoing segments in p
	 */
	public Segment2[] getOutSegments(
			Point2 p)
	{
		if (!this.isDirected())
		{
			return this.getSegments(p);
		}
		Vector theEdges = new Vector();
		if (_pointToEdge.containsKey(p) == false)
		{
			Segment2[] return_array = new Segment2[0];
			return return_array;
		}
		else
		{
			Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
			if (first.source() == p)
			{
				theEdges.addElement(first.line());
			}
			if (first.nextEdge(p) == null)
			{
				if (first.source() == p)
				{
					Segment2[] return_array = new Segment2[1];
					return_array[0] = (Segment2) first.line();
					return return_array;
				}
				else
				{
					Segment2[] return_array = new Segment2[0];
					return return_array;
				}
			}
			else
			{
				Point2GraphEdge next_edge = first.nextEdge(p);
				while (next_edge.nextEdge(p) != first)
				{
					if (next_edge.source() == p)
					{
						theEdges.addElement(next_edge.line());
					}
					next_edge = next_edge.nextEdge(p);
				}
				if (next_edge.source() == p)
				{
					theEdges.addElement(next_edge.line());
				}
				Segment2[] return_array = new Segment2[theEdges.size()];
				for (int i = 0; i <= theEdges.size() - 1; i++)
				{
					return_array[i] = (Segment2) theEdges.elementAt(i);
				}
				return return_array;
			}
		}
	} // getOutSegments


	/**
	 * Return all edges ending in <tt>p</tt>. For convenience, these edges are
	 * casted to Segment2. WARNING: This is only applicable, if the graph
	 * consists only of edges of type Segment2!!! If the graph is undirected,
	 * this is the same as <tt>getSegments(p)</tt>.
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return Array of segments ending in p
	 */
	public Segment2[] getInSegments(
			Point2 p)
	{
		if (!this.isDirected())
		{
			return this.getSegments(p);
		}
		Vector theEdges = new Vector();
		if (_pointToEdge.containsKey(p) == false)
		{
			Segment2[] return_array = new Segment2[0];
			return return_array;
		}
		else
		{
			Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
			if (first.target() == p)
			{
				theEdges.addElement(first.line());
			}
			if (first.nextEdge(p) == null)
			{
				if (first.target() == p)
				{
					Segment2[] return_array = new Segment2[1];
					return_array[0] = (Segment2) first.line();
					return return_array;
				}
				else
				{
					Segment2[] return_array = new Segment2[0];
					return return_array;
				}
			}
			else
			{
				Point2GraphEdge next_edge = first.nextEdge(p);
				while (next_edge.nextEdge(p) != first)
				{
					if (next_edge.target() == p)
					{
						theEdges.addElement(next_edge.line());
					}
					next_edge = next_edge.nextEdge(p);
				}
				if (next_edge.target() == p)
				{
					theEdges.addElement(next_edge.line());
				}
				Segment2[] return_array = new Segment2[theEdges.size()];
				for (int i = 0; i <= theEdges.size() - 1; i++)
				{
					return_array[i] = (Segment2) theEdges.elementAt(i);
				}
				return return_array;
			}
		}
	} // getInSegments


	/**
	 * Return the nearest (reachable) neighbour to <tt>p</tt>.
	 * 
	 * @param p
	 *            The vertex
	 * 
	 * @return The nearest vertex
	 */
	public Point2 nearestNeighbour(
			Point2 p)
	{
		if (!_pointToEdge.containsKey(p))
		{
			return null;
		}
		Point2GraphEdge first = (Point2GraphEdge) _pointToEdge.get(p);
		Point2GraphEdge next = first.nextEdge(p);
		Point2GraphEdge nearest = first;
		double ndist = getWhight(nearest.line());
		if (isDirected())
		{
			if (nearest.pointEdgeRelation(p) == TARGET_POINT)
			{
				ndist = Double.POSITIVE_INFINITY;
			}
		}
		while (next != null && next != first)
		{
			double dist = getWhight(next.line());
			if (dist < ndist && !isDirected())
			{
				nearest = next;
				ndist = getWhight(nearest.line());
			}
			if (dist < ndist && isDirected())
			{
				if (nearest.pointEdgeRelation(p) == SOURCE_POINT)
				{
					nearest = next;
					ndist = getWhight(nearest.line());
				}
			}
			next = next.nextEdge(p);
		}
		if (nearest != null && nearest.pointEdgeRelation(p) == SOURCE_POINT
				&& ndist != Double.POSITIVE_INFINITY)
		{
			return nearest.target();
		}
		if (nearest != null && nearest.pointEdgeRelation(p) == TARGET_POINT
				&& ndist != Double.POSITIVE_INFINITY)
		{
			return nearest.source();
		}
		return null;
	} // nearestNeighbour


	//------------------------------------------------------------
	// More complex methods:
	//------------------------------------------------------------

	/**
	 * Return true, if the graph is complete.
	 * 
	 * @return true if complete, false else
	 */
	public boolean isComplete()
	{
		if (_vertices.size() == 1)
		{
			return true;
		}
		if (!isDirected())
		{
			for (int cnt = 0; cnt < _vertices.size(); cnt++)
			{
				Point2 source = (Point2) _vertices.elementAt(cnt);
				for (int cnt1 = cnt + 1; cnt1 < _vertices.size(); cnt1++)
				{
					Point2 target = (Point2) _vertices.elementAt(cnt1);
					if (!isConnected(source, target))
					{
						return false;
					}
				} // for
			} //for
		}
		else
		{
			for (int cnt = 0; cnt < _vertices.size(); cnt++)
			{
				Point2 source = (Point2) _vertices.elementAt(cnt);
				for (int cnt1 = cnt + 1; cnt1 < _vertices.size(); cnt1++)
				{
					Point2 target = (Point2) _vertices.elementAt(cnt1);
					if (!isConnected(source, target))
					{
						return false;
					}
					if (!isConnected(target, source))
					{
						return false;
					}
				} // for
			} //for
		}
		if (_vertices.size() == 0)
		{
			return false;
		}
		return true;
	} // isComplete


	/**
	 * Return true, if the graph is connected.
	 * 
	 * @return true if connected, false else
	 */
	public boolean isConnected()
	{
		if (_edges.size() == 0)
		{
			return false;
		}
		int[] compindex = new int[_vertices.size()];
		for (int cnt = 0; cnt < _vertices.size(); cnt++)
		{
			compindex[cnt] = cnt;
		}
		int finalIndex = _vertices.size();
		int nextIndex = finalIndex + 1;
		BasicLine2 first = ((Point2GraphEdge) _edges.elementAt(0)).line();
		int sourceindex = _vertices.indexOf(first.source());
		int targetindex = _vertices.indexOf(first.target());
		compindex[sourceindex] = finalIndex;
		compindex[targetindex] = finalIndex;
		for (int cnt = 1; cnt < _edges.size(); cnt++)
		{
			boolean done = false;
			BasicLine2 nextline = ((Point2GraphEdge) _edges.elementAt(cnt))
					.line();
			sourceindex = _vertices.indexOf(nextline.source());
			targetindex = _vertices.indexOf(nextline.target());
			if (compindex[sourceindex] < finalIndex
					&& compindex[targetindex] < finalIndex)
			{
				compindex[sourceindex] = nextIndex;
				compindex[targetindex] = nextIndex;
				nextIndex++;
				done = true;
			}
			if (compindex[sourceindex] == finalIndex
					|| compindex[targetindex] == finalIndex && !done)
			{
				if (compindex[sourceindex] == finalIndex)
				{
					int checkindex = compindex[targetindex];
					for (int cnt1 = 0; cnt1 < _vertices.size(); cnt1++)
					{
						if (compindex[cnt1] == checkindex)
						{
							compindex[cnt1] = finalIndex;
						}
					}
				}
				else
				{
					int checkindex = compindex[sourceindex];
					for (int cnt2 = 0; cnt2 < _vertices.size(); cnt2++)
					{
						if (compindex[cnt2] == checkindex)
						{
							compindex[cnt2] = finalIndex;
						}
					}
				}
				done = true;
			}
			if ((compindex[sourceindex] < finalIndex && compindex[targetindex] > finalIndex)
					|| (compindex[sourceindex] > finalIndex && compindex[targetindex] < finalIndex)
					&& !done)
			{
				if (compindex[sourceindex] < finalIndex)
				{
					int searchIndex = compindex[sourceindex];
					for (int cnt3 = 0; cnt3 < _vertices.size(); cnt3++)
					{
						if (compindex[cnt3] == searchIndex)
						{
							compindex[cnt3] = compindex[targetindex];
						}
						done = true;
					}
				}
				else
				{
					if (compindex[targetindex] < finalIndex)
					{
						int searchIndex = compindex[targetindex];
						for (int cnt4 = 0; cnt4 < _vertices.size(); cnt4++)
						{
							if (compindex[cnt4] == searchIndex)
							{
								compindex[cnt4] = compindex[sourceindex];
							}
						}
						done = true;
					}
				}
			}
			if (compindex[sourceindex] > finalIndex
					&& compindex[targetindex] > finalIndex
					&& compindex[sourceindex] != compindex[targetindex]
					&& !done)
			{
				if (compindex[sourceindex] > compindex[targetindex])
				{
					int searchIndex = compindex[targetindex];
					for (int cnt5 = 0; cnt5 < _vertices.size(); cnt5++)
					{
						if (compindex[cnt5] == searchIndex)
						{
							compindex[cnt5] = compindex[sourceindex];
						}
					}
					done = true;
				}
				else if (compindex[targetindex] > compindex[sourceindex])
				{
					int searchIndex = compindex[sourceindex];
					for (int cnt6 = 0; cnt6 < _vertices.size(); cnt6++)
					{
						if (compindex[cnt6] == searchIndex)
						{
							compindex[cnt6] = compindex[targetindex];
						}
					}
					done = true;
				}

			}

		}

		for (int cnt7 = 0; cnt7 < _vertices.size(); cnt7++)
		{
			if (compindex[cnt7] != finalIndex)
			{
				return false;
			}
		}

		return true;
	} // isConnected


	// auch noch denkbar:
	// weak-connected, strong-connected, acyclic

	/**
	 * Return true, if the graph is directed and for every edge (v,w) the edge
	 * (w,v) is contained in the graph, too. If the graph is undirected, this
	 * return always true.
	 * 
	 * @return true if symmetric, false else
	 */
	public boolean isSymmetric()
	{
		if (!this.isDirected())
		{
			return true;
		}
		for (int xy = 0; xy < _edges.size(); xy++)
		{
			Point2GraphEdge testEdge = (Point2GraphEdge) _edges.elementAt(xy);
			Point2 source = testEdge.source();
			Point2 target = testEdge.target();
			if (!isConnected(target, source))
			{
				return false;
			}
		}
		return true;
	} // isSymmetric


	/**
	 * Move all points in the graph.
	 * 
	 * @param xdist
	 *            Translation in x direction
	 * @param ydist
	 *            Translation in y direction
	 */
	public void translate(
			float xdist,
			float ydist)
	{
		int counter = _vertices.size() - 1;
		while (counter >= 0)
		{
			Point2 toMove = (Point2) _vertices.elementAt(counter);
			toMove.x = toMove.x + xdist;
			toMove.y = toMove.y + ydist;
			counter--;
		}
	} // translate


	/**
	 * Move all points in the graph.
	 * 
	 * @param vector
	 *            The translation in x and y direction
	 */
	public void translate(
			Point2 vector)
	{
		float x = vector.x;
		float y = vector.y;
		translate(x, y);
	} // translate


	/**
	 * Return the minimum spanning tree of the graph (Kruskal's algo).<br>The
	 * graph must be weighted and all edges must have value. If the graph is
	 * unweighted, this return <tt>null</tt>.
	 * 
	 * @return Minimum spanning tree
	 */
	public Point2Graph getMST()
	{
		if (!isWeighted())
			return null;
		double min = Double.POSITIVE_INFINITY;
		Point2GraphEdge minedge = null;
		Vector minsorted = new Vector();
		int[] marker = new int[_edges.size()];
		for (int zx = 0; zx < _edges.size(); zx++)
		{
			marker[zx] = 0;
		}
		// Optimize ! Bad n^2 algo ... :(
		while (minsorted.size() < _edges.size())
		{
			for (int i = 0; i < _edges.size(); i++)
			{
				Point2GraphEdge test = (Point2GraphEdge) _edges.elementAt(i);
				if (getWhight(test.line()) <= min && marker[i] == 0)
				{
					min = getWhight(test.line());
					minedge = (Point2GraphEdge) _edges.elementAt(i);
				}
			}
			min = Double.POSITIVE_INFINITY;
			minsorted.addElement(minedge);
			marker[_edges.indexOf(minedge)] = 1;
		}
		Point2GraphEdge toAdd = (Point2GraphEdge) minsorted.elementAt(0);
		Point2List theVertices = this.getPoints();
		Point2Graph returnMST = new Point2Graph(theVertices);
		returnMST.add(toAdd.line());
		int compindex[] = new int[_vertices.size()];
		for (int i = 0; i < _vertices.size() - 1; i++)
		{
			compindex[i] = i;
		}
		int finalIndex = _vertices.size() + 1;
		int nextComp = finalIndex + 1;
		int index = _vertices.indexOf(toAdd.source());
		compindex[index] = finalIndex;
		index = _vertices.indexOf(toAdd.target());
		compindex[index] = finalIndex;
		for (int i = 1; i < minsorted.size(); i++)
		{
			boolean out = false;
			Point2GraphEdge test = (Point2GraphEdge) minsorted.elementAt(i);
			BasicLine2 testLine = test.line();
			int indexPoint1 = _vertices.indexOf(testLine.source());
			int indexPoint2 = _vertices.indexOf(testLine.target());

			if (compindex[indexPoint1] < finalIndex
					&& compindex[indexPoint2] < finalIndex)
			{
				compindex[indexPoint1] = nextComp;
				compindex[indexPoint2] = nextComp;
				nextComp++;
				returnMST.add(test.line());
				out = true;
			}
			if (compindex[indexPoint1] == finalIndex
					|| compindex[indexPoint2] == finalIndex && !out)
			{
				if (compindex[indexPoint1] < finalIndex
						|| compindex[indexPoint2] < finalIndex)
				{
					if (compindex[indexPoint1] < finalIndex)
					{
						compindex[indexPoint1] = finalIndex;
						returnMST.add(test.line());
						out = true;
					}
					else
					{
						compindex[indexPoint2] = finalIndex;
						returnMST.add(test.line());
						out = true;
					}
				}
				if (compindex[indexPoint1] > finalIndex
						|| compindex[indexPoint2] > finalIndex)
				{
					if (compindex[indexPoint1] > finalIndex)
					{
						int check = compindex[indexPoint1];
						for (int j = 0; j < _vertices.size(); j++)
						{
							if (compindex[j] == check)
							{
								compindex[j] = finalIndex;
							}
						}
						compindex[indexPoint1] = finalIndex;
						returnMST.add(test.line());
						out = true;
					}
					else
					{
						int check = compindex[indexPoint2];
						for (int k = 0; k < _vertices.size(); k++)
						{
							if (compindex[k] == check)
							{
								compindex[k] = finalIndex;
							}
						}
						compindex[indexPoint2] = finalIndex;
						returnMST.add(test.line());
						out = true;
					}
				}
			}
			if (!out
					&& ((compindex[indexPoint1] > finalIndex && compindex[indexPoint2] < finalIndex) || (compindex[indexPoint1] < finalIndex && compindex[indexPoint2] > finalIndex)))
			{
				if (compindex[indexPoint1] > finalIndex)
				{
					compindex[indexPoint2] = compindex[indexPoint1];
					returnMST.add(test.line());
					out = true;
				}
				else
				{
					if (compindex[indexPoint2] > finalIndex)
					{
						compindex[indexPoint1] = compindex[indexPoint2];
						returnMST.add(test.line());
						out = true;
					}
				}
			}

			if (!out && compindex[indexPoint1] > finalIndex
					&& compindex[indexPoint2] > finalIndex
					&& compindex[indexPoint1] != compindex[indexPoint2])

			{
				if (compindex[indexPoint1] > compindex[indexPoint2])
				{
					int higherIndex = compindex[indexPoint1];
					int lowerIndex = compindex[indexPoint2];
					for (int x = 0; x < _vertices.size(); x++)
					{
						if (compindex[x] == lowerIndex)
						{
							compindex[x] = higherIndex;
						}
					}
					compindex[indexPoint2] = higherIndex;
					returnMST.add(test.line());
					out = true;
				}
				else if (compindex[indexPoint2] > compindex[indexPoint1])
				{
					int higherIndex = compindex[indexPoint2];
					int lowerIndex = compindex[indexPoint1];
					for (int y = 0; y < _vertices.size(); y++)
					{
						if (compindex[y] == lowerIndex)
						{
							compindex[y] = higherIndex;
						}
					}
					compindex[indexPoint1] = higherIndex;
					returnMST.add(test.line());
					out = true;
				}

			}
		} // for
		return returnMST;

	} // getMST


	/**
	 * Run Dijkstra's algorithm with given startpoint.<br>The graph must be
	 * weighted and all edges must have value. All vertex labels of the original
	 * graph are overwritten with the distance from the startpoint! If you want
	 * to preserve this labels, create copy WITH DEEPCOPY() (!!!) before running
	 * Dijkstra's algo. If the graph is unweighted, nothing is done.
	 * 
	 * @param start
	 *            The vertex to start with
	 */
	public void dijkstra(
			Point2 start)
	{

		if (!isWeighted())
			return;
		if (outDegree(start) == 0)
		{
			return;
		}
		for (int i = 0; i < _vertices.size(); i++)
		{
			Point2 nextPoint = (Point2) _vertices.elementAt(i);
			nextPoint.setValue(Float.POSITIVE_INFINITY);
		}
		start.setValue(0);
		BasicLine2[] outEdges = getOutEdges(start);
		int noOfOutEdges = outDegree(start);
		float aktWave = 0;
		PriorityQueue pq = new PriorityQueue();
		Point2 aktCheckPoint = start;
		for (int i = 0; i < noOfOutEdges; i++)
		{
			BasicLine2 nextLine = outEdges[i];
			float nextValue = nextLine.getValue(); // NEW !
			Point2 target = nextLine.target();
			if (nextLine.target() == aktCheckPoint)
			{
				target = nextLine.source();
			}
			if (!target.equals(start))
			{
				target.setValue(nextValue);
			}

			//     aktCheckPoint=target; // aktCheckPoint muss innerhalb dieser
			// for-Schleife doch weiterhin der Punkt
			// start sein, oder ?
			pq.insert(target);
		}

		while (!pq.isEmpty())
		{
			Point2 nextPoint = pq.deleteMin();
			aktWave = nextPoint.getValue();
			BasicLine2[] outEdg = getOutEdges(nextPoint);
			int noOfOutEdg = outDegree(nextPoint);
			for (int i = 0; i < noOfOutEdg; i++)
			{
				BasicLine2 nextLine = outEdg[i];
				float nextValue = nextLine.getValue(); // NEW !
				Point2 target = nextLine.target();
				if (nextLine.target() == nextPoint)
				{
					target = nextLine.source();
				}
				if (target.getValue() != Float.POSITIVE_INFINITY
						&& target != start)
				{
					if (aktWave + nextValue < target.getValue())
					{
						target.setValue(aktWave + nextValue);

					}
				}
				else
				{
					if (target != start)
					{
						target.setValue(aktWave + nextValue);

						pq.insert(target);
					}
				}
			} // for
		} // while

	} // dijkstra


	/**
	 * Run Dijkstra's algorithm and calculate shortest path between source and
	 * target. ( Open polygon ) If indegree target == 0 or outdegree source == 0
	 * null will be returned !
	 * 
	 * @param source
	 *            The vertex to start from
	 * @param target
	 *            The vertex to end at
	 * 
	 * @return The shortest path between source and target
	 */
	public Polygon2 dijkstra(
			Point2 source,
			Point2 target)
	{
		dijkstra(source);
		if (inDegree(target) == 0 || outDegree(source) == 0)
		{
			return null;
		}
		Vector pathPoints = new Vector();

		Point2 aktVertex = target;
		while (aktVertex != source)
		{
			BasicLine2[] checkEdges = getEdges(aktVertex);
			float mindist = Float.MAX_VALUE;
			Point2 nextVertex = null;
			for (int cnt = 0; cnt < checkEdges.length; cnt++)
			{
				BasicLine2 test = checkEdges[cnt];
				Point2 sp = test.source();
				Point2 tp = test.target();
				if (sp == aktVertex)
				{
					if (tp == source)
					{
						nextVertex = tp;
						break;
					}
					else
					{
						float value = test.getValue();
						float label = tp.getValue();
						float dist = value + label;
						if (dist < mindist)
						{
							mindist = dist;
							nextVertex = tp;
						}
					}
				}
				else if (tp == aktVertex)
				{
					if (sp == source)
					{
						nextVertex = sp;
						break;
					}
					else
					{
						float value = test.getValue();
						float label = sp.getValue();
						float dist = value + label;
						if (dist < mindist)
						{
							mindist = dist;
							nextVertex = sp;
						}
					}
				}
			} // for

			pathPoints.addElement(aktVertex);

			aktVertex = nextVertex;
		} // while

		pathPoints.addElement(source);
		Point2List returnPoints = new Point2List();

		for (int i = 0; i < pathPoints.size(); i++)
		{
			returnPoints.addPoint((Point2) pathPoints.elementAt((pathPoints
					.size() - i) - 1));
		}

		Polygon2 returnPoly = new Polygon2(returnPoints);
		returnPoly.setOpen();
		return returnPoly;
	} // dijkstra


	/*
	    //============================================================
	    public Polygon2 dijkstra( Point2 source , Point2 target)
	    //============================================================
	    {
	     dijkstra(source);
		if(inDegree(target)==0 || outDegree(source)==0)
		 {
		  return null;
		 }
	     Vector pathPoints = new Vector();
	     boolean finished = false;
	     Point2 aktVertex = target;
	     BasicLine2[] checkEdges ;
	     while (!finished)
	      {
	       checkEdges = getEdges(aktVertex);
	       int numOfCheckEdges = degree(aktVertex);
	       boolean found=false;
	       int cnt=0;
	       while(!found)
	        {
		 BasicLine2 test = checkEdges[cnt];
		 Point2 sp = test.source();
		 Point2 tp = test.target();
	         if(!sp.equals(tp))  // Endless loop if sp.equals(tp)==true !!!!
		  {

		 if (sp == aktVertex )
		  {
		   float value = test.getValue();
		   float label = tp.getValue();
		   if (((sp.getValue()-value)-label)<DIJKSTRA_DELTA
		       && ((sp.getValue()-value)-label)>-DIJKSTRA_DELTA)
		    {
		     pathPoints.addElement(aktVertex);
		     aktVertex=tp;
		     found=true;
		    }
		  }
		 else
		  {
		 if (tp == aktVertex )
		  {
		   float value = test.getValue();
		   float label = sp.getValue();
		   if (((tp.getValue()-value)-label)<DIJKSTRA_DELTA
		       && ((tp.getValue()-value)-label)>-DIJKSTRA_DELTA)
		    {
		     pathPoints.addElement(aktVertex);
		     aktVertex=sp;
		     found=true;
		    }
		  }
		 else
		  {
		 if(sp==source || tp==source)
		  {
		   finished=true;
		   found=true;
		   if(sp==source)
		    {
		     pathPoints.addElement(aktVertex);
		     //pathPoints.addElement(tp);
		    }
		   else
		    {
		     pathPoints.addElement(aktVertex);
		     //pathPoints.addElement(sp);
		    }
	          }
		  }
		  }
		  }

		 cnt++;
	        } // while

	      if(aktVertex==source)
	       {
	        finished=true;
	       }
	     } // while
	     pathPoints.addElement(source);
	     Point2List returnPoints = new Point2List();
	     for ( int i=0;i<pathPoints.size();i++)
	      {
	       returnPoints.addPoint((Point2)pathPoints.elementAt((pathPoints.size()-i)-1));
	      }
	     Polygon2 returnPoly = new Polygon2(returnPoints);
	     returnPoly.setOpen();
	     return returnPoly;
	    } // dijkstra
	*/

	//------------------------------------------------------------
	// Interface drawable:
	//------------------------------------------------------------

	/**
	 * Inherited from Drawable.
	 * 
	 * @param graphics
	 *            The graphics object to draw in
	 * @param graphicsContext
	 *            The given gc
	 * 
	 * @see anja.util.Drawable
	 */
	public void draw(
			Graphics2D graphics,
			GraphicsContext graphicsContext)
	{

		// vertices
		for (int i = 0; i < _vertices.size(); i++)
		{
			((Point2) _vertices.elementAt(i)).draw(graphics, graphicsContext);
		}
		// lines
		for (int i = 0; i < _edges.size(); i++)
		{
			if (_segToEdge.containsKey(((Point2GraphEdge) _edges.elementAt(i))
					.line()))
			{
				BasicLine2 theEdge = ((Point2GraphEdge) _edges.elementAt(i))
						.line();
				theEdge.draw(graphics, graphicsContext);
			}
		}
	} // draw


	/**
	 * Inherited from Drawable
	 * 
	 * @param box
	 *            The rectangle
	 * 
	 * @return true, if the graph intersects the box, false else
	 * 
	 * @see anja.util.Drawable
	 */
	public boolean intersects(
			Rectangle2D box)
	{

		for (int count = 0; count < _vertices.size(); count++)
		{
			Point2 checkPoint = (Point2) _vertices.elementAt(count);
			if (box.contains(checkPoint.x, checkPoint.y))
			{
				// at least one vertex inside testbox
				return true;
			}
		}
		for (int count = 0; count < _edges.size(); count++)
		{
			BasicLine2 checkLine = ((Point2GraphEdge) _edges.elementAt(count))
					.line();
			if (checkLine.intersects(box))
			{
				// al least one edge/testbox intersection
				return true;
			}
		}
		return false;
	}


	//------------------------------------------------------------
	// Other:
	//------------------------------------------------------------

	/**
	 * Duplicate Point2Graph WITH ALL OBJECTS. This will not only copy the
	 * references like clone().
	 * 
	 * @return Complete copy of this object
	 */
	public Point2Graph deepCopy()
	{
		Point2Graph returnGraph = new Point2Graph();
		ByteArrayOutputStream bufOutStream = new ByteArrayOutputStream();
		ObjectOutputStream outStream = null;
		try
		{
			outStream = new ObjectOutputStream(bufOutStream);
			outStream.writeObject(this);
			outStream.close();
		}
		catch (Exception ex)
		{
			System.out.println("Exception in Point2Graph.deepCopy()");
			System.err.println(ex);
		}
		byte[] buffer = bufOutStream.toByteArray();
		ByteArrayInputStream bufInStream = new ByteArrayInputStream(buffer);
		ObjectInputStream inStream = null;
		try
		{
			inStream = new ObjectInputStream(bufInStream);
			returnGraph = (Point2Graph) inStream.readObject();
			inStream.close();
		}
		catch (Exception e)
		{
			System.out.println("Exception in Point2Graph.deepCopy()");
			System.err.println(e);
		}
		return returnGraph;
	}


	/**
	 * Clones the object (reference)
	 * 
	 * @return Copy of this object
	 */
	public Object clone()
	{
		Point2Graph returnGraph = new Point2Graph();
		returnGraph._pointToEdge = (Hashtable) _pointToEdge.clone();
		returnGraph._segToEdge = (Hashtable) _segToEdge;
		returnGraph._polyToEdge = (Hashtable) _polyToEdge;
		returnGraph._vertices = (Vector) _vertices;
		returnGraph._edges = (Vector) _edges;
		return returnGraph;
	}


	/**
	 * Convert to string
	 * 
	 * @return Textual representation of this object
	 */
	public String toString()
	{
		String out_string = "Point2Graph [< ";
		int vertexcount = 0;
		for (int i = 0; i < _vertices.size(); i++)
		{
			if (!_pointToEdge.containsKey(((Point2) _vertices.elementAt(i))))
			{
				out_string = out_string + "vertex "
						+ ((Point2) _vertices.elementAt(i)).toString() + " ";
				vertexcount++;
				if (vertexcount % 3 == 0)
				{
					out_string = out_string + "\n";
				}

			}
		}
		out_string = out_string + "\n";
		int linecount = 0;
		for (int i = 0; i < _edges.size(); i++)
		{
			if (_segToEdge.containsKey(((Point2GraphEdge) _edges.elementAt(i))
					.line()))
			{
				BasicLine2 theEdge = ((Point2GraphEdge) _edges.elementAt(i))
						.line();
				out_string = out_string + "line " + theEdge.toString() + " ";
				linecount++;
			}
			if (linecount % 3 == 0)
			{
				out_string = out_string + "\n";
			}
		}
		out_string = out_string + " >]";

		return out_string;
	} // toString

	//************************************************************
	// Private methods
	//************************************************************

} // Point2Graph
