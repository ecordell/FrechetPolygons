/**
 * Copyright: CCPL BY-NC-SA 3.0 (cc) 2008 Jan Tulke <br> Creative Commons Public
 * Licence. Some Rights Reserved.<br>
 * http://creativecommons.org/licenses/by-nc-sa/3.0/de/ <br> If you use this
 * code, please send a message to one of the<br> author:
 * jan.tulke@hochtief.de<br>
 */

package anja.geom.triangulation.pslgtriang;


import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import anja.geom.Point2;
import anja.geom.Segment2;
import anja.geom.Polygon2;
import anja.util.GraphicsContext;


public class Triangulator
{

	TreeSet<Vertex>						vertices			= new TreeSet<Vertex>();
	Vector<Edge>						edges				= new Vector<Edge>();
	Vector<Stack<Edge>>					chains				= new Vector<Stack<Edge>>();
	HashSet<Triangle>					triangles			= new HashSet<Triangle>();

	long								nrOfErrors			= 0;

	/* set of edges where vertex is the end point of the edge,
	 * the set is order from upper to lower edges (counter clockwise) */
	Hashtable<Vertex, TreeSet<Edge>>	edgesIN				= new Hashtable<Vertex, TreeSet<Edge>>();
	/* set of edges where vertex is the start point of the edge,
	 * the set is order from upper to lower edges (clockwise) */
	Hashtable<Vertex, TreeSet<Edge>>	edgesOUT			= new Hashtable<Vertex, TreeSet<Edge>>();

	// the sweeplineStatus contains a ordered list of edges intersected by the
	// sweepline
	// the edges are ordered from top to down
	SweepLineComparator					comp				= new SweepLineComparator();
	TreeSet<SweepableObj>				sweepStatus			= new TreeSet<SweepableObj>(
																	comp);
	HashMap<Edge, Vertex>				hangingVertices		= new HashMap<Edge, Vertex>();
	Vertex								topEndHangingVertex	= null;

	Stack<Edge>							pocketEdges			= new Stack<Edge>();
	Stack<Triangle>						pocketTriangles		= new Stack<Triangle>();


	/**
	 * This method first add the convex hull to the edge set. Second it
	 * triangulates the PSLG. Third it removes outer pockets. An outer pocket is
	 * a cluster of connected triangles where at least one triangle has an edge
	 * of the convex hull which connects two vertices of the same polygon.
	 */
	public void computeIntermediateArea()
	{
		// System.out.println("triangulation started ...");
		// extractEdgesAsExample();
		// nrOfErrors = 0;
		computeConvexHull();
		constraindTriangulation();
		removePocketTriangles();
		// System.out.println("triangulation finished, " + nrOfErrors +
		// " errors");
	}


	/**
	 * This methods triangulates a PSLG
	 */
	public void constraindTriangulation()
	{
		// debugEdgeSets();
		regularizePSLG();
		// debugEdges();
		constructChainSet();
		// debugChainSet();
		triangulateChainSet();
	}


	/**
	 * Resets the status of this object.
	 * 
	 * Clears the set of vertices, edges and already calculated triangles...
	 */
	public void reset()
	{
		vertices.clear();
		edges.clear();
		chains.clear();
		triangles.clear();
		edgesIN.clear();
		edgesOUT.clear();
		sweepStatus.clear();
		Edge.resetNumbering();
		Vertex.resetNumbering();
		Triangle.resetNumbering();
	}


	/**
	 * Adds a vertex to the set of vertices
	 * 
	 * @param v
	 *            The vertex
	 */
	public void addVertex(
			Vertex v)
	{
		vertices.add(v);
	}


	/**
	 * Adds a polygon to the sets of vertices and edges.
	 * 
	 * @param poly
	 *            A closed polygon
	 */
	public void addPolygon2(
			Polygon2 poly)
	{
		if (poly.isClosed())
		{

			Segment2[] segs = poly.edges();

			for (int i = 0; i < segs.length; ++i)
			{
				Vertex v = new Vertex(segs[i].source().x, segs[i].source().y);
				addVertex(v);
			}

			for (int i = 0; i < segs.length; ++i)
			{
				Vertex v1 = new Vertex(segs[i].source().x, segs[i].source().y);
				Vertex v2 = new Vertex(segs[i].target().x, segs[i].target().y);
				addEdge(v1, v2, (new GraphicsContext()).getFillColor(),
						"Polygon1");
			}
		}

	}


	/**
	 * Adds an edge to this object.
	 * 
	 * The vertices are not automatically added to the set of vertices. Instead
	 * the closest points to v1 and v2 from the set of vertices are taken for
	 * further calculations.
	 * 
	 * @param v1
	 *            The first vertex (source)
	 * @param v2
	 *            The second vertex (target)
	 * @param c
	 *            The color
	 * @param polygonName
	 *            The name of the polygon, the edge belongs to
	 */
	public void addEdge(
			Vertex v1,
			Vertex v2,
			Color c,
			String polygonName)
	{
		if (v1.equals(v2))
		{
			addVertex(v1);
			return;
		}
		Edge edge = new Edge(v1, v2, c, polygonName);
		v1.assignMemberOf(polygonName);
		v2.assignMemberOf(polygonName);
		if (!checkLineCross(edge))
		{
			if (!vertices.add(edge.v1))
			{
				edge.v1 = vertices.floor(edge.v1);
				edge.v1.assignMemberOf(polygonName);
			}
			if (!vertices.add(edge.v2))
			{
				edge.v2 = vertices.floor(edge.v2);
				edge.v2.assignMemberOf(polygonName);
			}
			edges.add(edge);
			insertInOutEdges(edge);
		}
	}


	/**
	 * Add method for Point2 objects instead of vertices.
	 * 
	 * Inside the method uses a call of
	 * {@link anja.geom.triangulation.pslgtriang.Triangulator#addEdge(anja.geom.triangulation.pslgtriang.Vertex, anja.geom.triangulation.pslgtriang.Vertex, java.awt.Color, java.lang.String)}
	 * 
	 * @param p1
	 *            A Point2 object, the source of the segment
	 * @param p2
	 *            A Point2 object, the target of the segment
	 * @param c
	 *            The color
	 * @param polygonName
	 *            The name of the polygon, the edge belongs to
	 */
	public void addEdge(
			Point2 p1,
			Point2 p2,
			Color c,
			String polygonName)
	{
		Vertex v1 = new Vertex(p1.x, p1.y);
		Vertex v2 = new Vertex(p2.x, p2.y);

		addEdge(v1, v2, c, polygonName);
	}


	/**
	 * Add method for Segment2 objects instead of vertices.
	 * 
	 * Inside the method uses a call of
	 * {@link anja.geom.triangulation.pslgtriang.Triangulator#addEdge(anja.geom.triangulation.pslgtriang.Vertex, anja.geom.triangulation.pslgtriang.Vertex, java.awt.Color, java.lang.String)}
	 * 
	 * @param seg
	 *            A Segment2 object, that mirrors the edge
	 * @param c
	 *            The color
	 * @param polygonName
	 *            The name of the polygon, the edge belongs to
	 */
	public void addEdge(
			Segment2 seg,
			Color c,
			String polygonName)
	{
		Vertex v1 = new Vertex(seg.source().x, seg.source().y);
		Vertex v2 = new Vertex(seg.target().x, seg.target().y);

		addEdge(v1, v2, c, polygonName);
	}


	/**
	 * Inserts an edge in the set
	 * 
	 * @param edge
	 *            An edge object
	 */
	void insertInOutEdges(
			Edge edge)
	{
		Vertex v1 = edge.v1;
		Vertex v2 = edge.v2;
		if (!edgesOUT.containsKey(v1))
		{
			TreeSet<Edge> tree = new TreeSet<Edge>();
			tree.add(edge);
			edgesOUT.put(v1, tree);
		}
		else
		{
			TreeSet<Edge> tree = edgesOUT.get(v1);
			tree.add(edge);
		}
		if (!edgesIN.containsKey(v2))
		{
			TreeSet<Edge> tree = new TreeSet<Edge>();
			tree.add(edge);
			edgesIN.put(v2, tree);
		}
		else
		{
			TreeSet<Edge> tree = edgesIN.get(v2);
			tree.add(edge);
		}
	}


	/**
	 * Check, if the given edge crosses another edge of the set
	 * 
	 * @param edge
	 *            An edge object
	 * 
	 * @return A boolean, depending on whether the edge crosses another edge or
	 *         not
	 */
	private boolean checkLineCross(
			Edge edge)
	{
		for (Edge e : edges)
		{
			// not connected edges have to be tested if they are equals or do
			// cross
			if (!e.abut(edge) && (e.equals(edge) || e.cross(edge)))
			{
				nrOfErrors++;
				System.out.println("crossing or identical edges encountered");
				System.out.println(e.toString());
				System.out.println(edge.toString());
				System.out.println("edge E" + edge.nr + " discarded");
				System.out.println();
				// System.out.println("abut " + e.abut(edge));
				// System.out.println("equals " + e.equals(edge));
				// System.out.println("cross " + e.cross(edge));
				// System.out.println();
				return true;
			}
		}
		return false;
	}


	/**
	 * Getter for the set of vertices
	 * 
	 * @return The set of vertices
	 */
	public TreeSet<Vertex> getVertices()
	{
		return vertices;
	}


	/**
	 * Getter for the set of edges
	 * 
	 * @return The set of edges
	 */
	public Vector<Edge> getEdges()
	{
		return edges;
	}


	/**
	 * Getter for the calculated triangles
	 * 
	 * @return A set of triangles
	 */
	public HashSet<Triangle> getTriangles()
	{
		return triangles;
	}


	/**
	 * Computation of convex hull according to Computational Geometry Algorithms
	 * and Applications 3rd Edition, Springer, 2008, p.6
	 */
	public void computeConvexHull()
	{
		int vertexCount = vertices.size();
		if (vertexCount < 2)
			return;

		Iterator<Vertex> iter = vertices.iterator();
		Vector<Vertex> upperHull = new Vector<Vertex>();
		upperHull.add(iter.next());
		upperHull.add(iter.next());
		while (iter.hasNext())
		{
			upperHull.add(iter.next());
			while (upperHull.size() > 2
					&& 0 < isRightTurn(upperHull.get(upperHull.size() - 3),
							upperHull.get(upperHull.size() - 2), upperHull
									.get(upperHull.size() - 1)))
			{
				upperHull.remove(upperHull.size() - 2);
			}
		}

		iter = vertices.descendingIterator();
		Vector<Vertex> lowerHull = new Vector<Vertex>();
		lowerHull.add(iter.next());
		lowerHull.add(iter.next());
		while (iter.hasNext())
		{
			lowerHull.add(iter.next());
			while (lowerHull.size() > 2
					&& 0 < isRightTurn(lowerHull.get(lowerHull.size() - 3),
							lowerHull.get(lowerHull.size() - 2), lowerHull
									.get(lowerHull.size() - 1)))
			{
				lowerHull.remove(lowerHull.size() - 2);
			}
		}
		Edge edge;
		for (int i = 0; i < upperHull.size() - 1; i++)
		{
			edge = new Edge(upperHull.get(i), upperHull.get(i + 1), Color.green);
			if (!edges.contains(edge))
			{
				edges.add(edge);
				if (edge.verticesOnSamePolygon())
					pocketEdges.push(edge);
				insertInOutEdges(edge);
			}
		}
		for (int i = lowerHull.size() - 1; i > 0; i--)
		{
			edge = new Edge(lowerHull.get(i), lowerHull.get(i - 1), Color.green);
			if (!edges.contains(edge))
			{
				edges.add(edge);
				if (edge.verticesOnSamePolygon())
					pocketEdges.push(edge);
				insertInOutEdges(edge);
			}
		}
	}


	/**
	 * Test if three consecutive points make a right turn
	 * 
	 * @param last
	 *            First point
	 * @param middle
	 *            Second point
	 * @param next
	 *            Third point
	 * 
	 * @return +1 if the three points make a right turn, 0 if the enclosed angle
	 *         is 180°, -1 if they make a left turn
	 */
	private int isRightTurn(
			Vertex last,
			Vertex middle,
			Vertex next)
	{

		double eps = 1E-14;
		// compute directional vectors
		double ax = next.x - middle.x;
		double ay = next.y - middle.y;
		double bx = middle.x - last.x;
		double by = middle.y - last.y;

		// compute the cross product
		double det = ax * by - ay * bx;
		// compute scalar product
		double s = ax * bx + ay * by;

		if (Math.abs(det) < eps && s > 0)
		{
			// vertices are collinear (not 360° but 180°
			return 0;
		}
		else if (det > 0)
			return +1;
		else
			return -1;
		// compute scalar product
	}


	/**
	 * Sweeps from left to right and the other way round
	 */
	public void regularizePSLG()
	{
		// sweep from left to right
		sweepLR();
		// sweep from right to left
		sweepRL();
	}


	/**
	 * Sweeps from the left to the right
	 */
	private void sweepLR()
	{
		sweepStatus.clear();
		hangingVertices.clear();
		topEndHangingVertex = null;
		Iterator<Vertex> iter = vertices.iterator();
		Vertex v;
		while (iter.hasNext())
		{
			v = iter.next();
			// Updates the comparator with the sweepline position.
			// The comparator compares vertices and edges intersected by the
			// sweepline
			comp.setSweepX(v.x);

			// remove all edges from the sweep line status which end at v and
			// connect related
			// hanging vertices with v
			if (edgesIN.containsKey(v))
			{
				HashSet<Vertex> verticesToBeConnected = new HashSet<Vertex>();
				for (Edge e : edgesIN.get(v))
				{
					// get the hanging vertex assigned to the interval starting
					// with e
					Vertex hangingVertex = hangingVertices.get(e);
					// if there is a hanging vertex connect it with v
					if (hangingVertex != null)
					{
						verticesToBeConnected.add(hangingVertex);
					}
					sweepStatus.remove(e);
				}
				for (Vertex e : verticesToBeConnected)
				{
					addEdge(e, v, Color.yellow, null);
					hangingVertices.remove(e);
				}
			}

			// getting the neighboring edge above v on the sweepline
			// y coordinate increases from top of screen to the bottom -> edge
			// above v = sweepStatus.lower(v)
			Edge edgeAbove = (Edge) sweepStatus.lower(v);
			if (edgeAbove != null)
			{
				// get the hanging vertex assigned to the interval starting with
				// edgeAbove
				Vertex hangingVertex = hangingVertices.get(edgeAbove);
				// if there is a hanging vertex connect it with v
				if (hangingVertex != null)
				{
					addEdge(hangingVertex, v, Color.yellow, null);
					hangingVertices.remove(edgeAbove);
				}
			}
			else if (topEndHangingVertex != null)
			{
				addEdge(topEndHangingVertex, v, Color.yellow, null);
				topEndHangingVertex = null;
			}

			if (edgesOUT.containsKey(v))
			{
				// add all edges to the sweep line status which start at v
				for (Edge e : edgesOUT.get(v))
				{
					sweepStatus.add(e);
				}
			}
			else
			{
				// hang v
				if (edgeAbove != null)
					hangingVertices.put(edgeAbove, v);
				else
					topEndHangingVertex = v;
			}

			// debugSweepStatus();
		}
	}


	/**
	 * Sweeps from the right to the left
	 */
	private void sweepRL()
	{
		sweepStatus.clear();
		hangingVertices.clear();
		topEndHangingVertex = null;
		Iterator<Vertex> iter = vertices.descendingIterator();
		Vertex v;
		while (iter.hasNext())
		{
			v = iter.next();
			// Updates the comparator with the sweepline position.
			// The comparator compares vertices and edges intersected by the
			// sweepline
			comp.setSweepX(v.x);

			// remove all edges from the sweep line status which start at v and
			// connect related
			// hanging vertices with v
			if (edgesOUT.containsKey(v))
			{
				HashSet<Vertex> verticesToBeConnected = new HashSet<Vertex>();
				for (Edge e : edgesOUT.get(v))
				{
					// get the hanging vertex assigned to the interval starting
					// with e
					Vertex hangingVertex = hangingVertices.get(e);
					// if there is a hanging vertex connect it with v
					if (hangingVertex != null)
					{
						verticesToBeConnected.add(hangingVertex);
					}
					sweepStatus.remove(e);
				}
				for (Vertex e : verticesToBeConnected)
				{
					addEdge(v, e, Color.yellow, null);
					hangingVertices.remove(e);
				}
			}

			// getting the neighboring edge above v on the sweepline
			// y coordinate increases from top of screen to the bottom -> edge
			// above v = sweepStatus.lower(v)
			Edge edgeAbove = (Edge) sweepStatus.lower(v);
			if (edgeAbove != null)
			{
				// get the hanging vertex assigned to the interval starting with
				// edgeAbove
				Vertex hangingVertex = hangingVertices.get(edgeAbove);
				// if there is a hanging vertex connect it with v
				if (hangingVertex != null)
				{
					addEdge(v, hangingVertex, Color.yellow, null);
					hangingVertices.remove(edgeAbove);
				}
			}
			else if (topEndHangingVertex != null)
			{
				addEdge(v, topEndHangingVertex, Color.yellow, null);
				topEndHangingVertex = null;
			}

			if (edgesIN.containsKey(v))
			{
				// add all edges to the sweep line status which end at v
				for (Edge e : edgesIN.get(v))
				{
					sweepStatus.add(e);
				}
			}
			else
			{
				// hang v
				if (edgeAbove != null)
					hangingVertices.put(edgeAbove, v);
				else
					topEndHangingVertex = v;
			}
			// debugSweepStatus();
		}
	}


	/**
	 * Weight balancing accroding to the chain method in F.P. Preparata & M.I.
	 * Shamos, Computational Geometry an Introduction, 1985, p.51 (scan
	 * direction inverted)
	 */
	public void constructChainSet()
	{
		long i;
		// initialize weights
		for (Edge e : edges)
		{
			e.weight = 1;
		}

		Vertex v;
		Edge topEdge;

		// first scan over all vertices (right to left)
		Iterator<Vertex> iter = vertices.descendingIterator();
		iter.next();
		int inSum, outSum;
		for (i = vertices.size() - 1; i >= 2; i--)
		{
			inSum = 0;
			outSum = 0;
			v = iter.next();
			// compute sum of weights of outgoing edges for current vertex
			for (Edge e : edgesOUT.get(v))
				outSum += e.weight;
			// get amount of incoming edges
			inSum = edgesIN.get(v).size();
			// get top incoming edge
			topEdge = edgesIN.get(v).first();
			// adjust weight of top edge if necessary
			if (outSum > inSum)
				topEdge.weight = outSum - inSum + 1;
		}

		// second scan over all vertices (left to right) incl. creation of
		// chains
		iter = vertices.iterator();
		v = iter.next();
		for (Edge e : edgesOUT.get(v))
		{
			for (i = 0; i < e.weight; i++)
			{
				Stack<Edge> chain = new Stack<Edge>();
				chain.push(e);
				chains.add(chain);
			}
		}
		int startIndex;
		int j;
		for (i = 2; i <= vertices.size() - 1; i++)
		{
			inSum = 0;
			outSum = 0;
			v = iter.next();
			startIndex = 0;
			while (!chains.get(startIndex).peek().v2.equals(v))
				startIndex++;
			// compute sum of weights of outgoing edges for current vertex
			for (Edge e : edgesOUT.get(v))
				outSum += e.weight;
			// compute sum of weights of incoming edges for current vertex
			for (Edge e : edgesIN.get(v))
				inSum += e.weight;
			// get top outgoing edge
			topEdge = edgesOUT.get(v).first();
			// adjust weight of top edge if necessary
			if (inSum > outSum)
				topEdge.weight = inSum - outSum + topEdge.weight;
			// extending the chains
			for (Edge e : edgesOUT.get(v))
			{
				outSum += e.weight;
				for (j = 0; j < e.weight; j++)
				{
					chains.get(startIndex++).push(e);
				}
			}
		}
	}


	/**
	 * Triangulates a chain of edges
	 */
	private void triangulateChainSet()
	{
		/* This method is just a preprocessing before calling the actual triangulation
		   It steps through both chains to find enclosed regions for triangulation.   
		   The resulting vectorForTriangulation contains edges. Therby an edge from the 
		   upper chain represents its first vertex, an edge from the lower chain represents 
		   its second vertex. */
		Vector<Edge> vectorForTriangulation = new Vector<Edge>();
		Stack<Edge> upperChain;
		Stack<Edge> lowerChain;

		/* indicates to which chain each edge belongs to
		   1 for upper chain, -1 for lower chain */
		Vector<Integer> edgeType = new Vector<Integer>();
		Edge upperEdge;
		Edge lowerEdge;
		int upperSize;
		int lowerSize;
		int upperIndex;
		int lowerIndex;
		for (int i = 0; i < chains.size() - 1; i++)
		{
			upperIndex = 0;
			lowerIndex = 0;
			upperChain = chains.get(i);
			lowerChain = chains.get(i + 1);
			upperSize = upperChain.size();
			lowerSize = lowerChain.size();
			while (upperIndex < upperSize || lowerIndex < lowerSize)
			{
				if (upperIndex < upperSize)
					upperEdge = upperChain.get(upperIndex);
				else
					upperEdge = upperChain.get(upperIndex - 1);
				lowerEdge = lowerChain.get(lowerIndex);
				if (upperEdge.equals(lowerEdge))
				{
					upperIndex++;
					lowerIndex++;
				}
				else
				{
					// first unequal edges
					if (upperEdge.v1.equals(lowerEdge.v1)
							&& upperIndex < upperSize)
					{
						edgeType.add(1);
						vectorForTriangulation.addElement(upperEdge);
						upperIndex++;
					}
					else if (upperEdge.v1.compareTo(lowerEdge.v2) < 0
							&& upperIndex < upperSize)
					{
						edgeType.add(1);
						vectorForTriangulation.addElement(upperEdge);
						upperIndex++;
					}
					else
					{
						edgeType.add(-1);
						vectorForTriangulation.addElement(lowerEdge);
						lowerIndex++;
					}
					/* If edges are not equals but upper and lower chain have the same vertices, 
					   then vectorForTriangulation contains edges which form a separated region 
					   with space between both chains (no degenerated region). 
					   Make sure the lower chain was already processed. This is not garanteed if 
					   the region contains only one edge from the lower chain -> wait one more 
					   iteration */
					if (upperEdge.v1.equals(lowerEdge.v2)
							|| (upperEdge.v2.equals(lowerEdge.v2) && edgeType
									.lastElement() == -1))
					{
						triangulateMonotonPolygon(edgeType,
								vectorForTriangulation);
						vectorForTriangulation.clear();
						edgeType.clear();
					}
				}
			}
		}
	}


	/**
	 * Triangulation of a monotone polygon according to F.P. Preparata & M.I.
	 * Shamos, Computational Geometry an Introduction, 1985, p.240 and M.d.Berg,
	 * O. Cheong, M.v. Kreveld, M. Overmars, Computational Geometry Algorithms
	 * and Applications 3rd Edition 2008
	 * 
	 * @param chainIndex
	 *            indicates to which chain each edge belongs, 1 for edges in the
	 *            upper chain, -1 for edges in the lower chain
	 * @param edgeList
	 *            list of edges of the monotone polygon, x-sorted by the
	 *            vertices represented by the edge edges from the upper chain
	 *            represent their first vertex, edges from the lower chain
	 *            represent their last vertex
	 */
	public void triangulateMonotonPolygon(
			Vector<Integer> chainIndex,
			Vector<Edge> edgeList)
	{
		// debugVectorForTriangulation(chainIndex, edgeList);
		if (edgeList.size() < 3)
			return;
		Vertex[] triangleVertices;
		Edge[] triangelEdges;
		if (edgeList.size() == 3)
		{
			if (chainIndex.get(1) == -1)
			{
				triangleVertices = new Vertex[3];
				triangleVertices[0] = edgeList.get(0).v2;
				triangleVertices[1] = edgeList.get(1).v1;
				triangleVertices[2] = edgeList.get(2).v1;
				triangelEdges = new Edge[3];
				triangelEdges[0] = edgeList.get(0);
				triangelEdges[1] = edgeList.get(1);
				triangelEdges[2] = edgeList.get(2);
				triangles.add(new Triangle(triangleVertices, triangelEdges,
						Color.green));
			}
			else
			{
				triangleVertices = new Vertex[3];
				triangleVertices[0] = edgeList.get(0).v2;
				triangleVertices[1] = edgeList.get(2).v1;
				triangleVertices[2] = edgeList.get(1).v2;
				triangelEdges = new Edge[3];
				triangelEdges[0] = edgeList.get(0);
				triangelEdges[1] = edgeList.get(2);
				triangelEdges[2] = edgeList.get(1);
				triangles.add(new Triangle(triangleVertices, triangelEdges,
						Color.green));
			}
			return;
		}

		Stack<Integer> stack = new Stack<Integer>();
		stack.push(0);
		stack.push(1);
		// u = current index, v = index at peek of stack
		int u, v;
		Edge edge;
		int edgeIndex;
		Edge newEdge;
		int edgeCount = edgeList.size();
		for (u = 2; u < edgeCount - 1; u++)
		{
			v = stack.peek();
			if (chainIndex.get(u) != chainIndex.get(v))
			{
				for (int i = 1; i < stack.size(); i++)
				{
					// new edge von u nach v
					newEdge = new Edge(getReprVertex(chainIndex.get(u),
							edgeList.get(u)), getReprVertex(chainIndex
							.get(stack.get(i)), edgeList.get(stack.get(i))),
							Color.red);
					edges.add(newEdge);

					// assign edges & vertices to triangle in counter clockwise
					// order
					triangleVertices = new Vertex[3];
					triangleVertices[0] = getReprVertex(chainIndex.get(u),
							edgeList.get(u));
					triangelEdges = new Edge[3];
					triangelEdges[0] = newEdge;
					if (chainIndex.get(u) == -1)
					{
						triangleVertices[1] = getReprVertex(chainIndex
								.get(stack.get(i)), edgeList.get(stack.get(i)));
						triangleVertices[2] = getReprVertex(chainIndex
								.get(stack.get(i - 1)), edgeList.get(stack
								.get(i - 1)));

						triangelEdges[0] = newEdge;

						edge = new Edge(triangleVertices[2],
								triangleVertices[0], null);
						edgeIndex = edgeList.indexOf(edge);
						triangelEdges[2] = edgeList.get(edgeIndex);
					}
					else
					{
						triangleVertices[1] = getReprVertex(chainIndex
								.get(stack.get(i - 1)), edgeList.get(stack
								.get(i - 1)));
						triangleVertices[2] = getReprVertex(chainIndex
								.get(stack.get(i)), edgeList.get(stack.get(i)));

						edge = new Edge(triangleVertices[0],
								triangleVertices[1], null);
						edgeIndex = edgeList.indexOf(edge);
						triangelEdges[0] = edgeList.get(edgeIndex);

						triangelEdges[2] = newEdge;
					}
					edge = new Edge(triangleVertices[1], triangleVertices[2],
							null);
					edgeIndex = edgeList.indexOf(edge);
					triangelEdges[1] = edgeList.get(edgeIndex);

					// triangle to the list of triangles
					triangles.add(new Triangle(triangleVertices, triangelEdges,
							Color.green));

					/* the new edge is added to the and of the input edge list for easy search of already
					   existing edges */
					edgeList.add(newEdge);
				}
				stack.clear();
				stack.push(v);
				stack.push(u);
			}
			else
			{
				triangleVertices = new Vertex[3];
				triangelEdges = new Edge[3];
				triangleVertices[0] = getReprVertex(chainIndex.get(u), edgeList
						.get(u));
				if (chainIndex.get(u) == 1)
				{
					triangleVertices[1] = getReprVertex(chainIndex.get(v),
							edgeList.get(v));
					triangleVertices[2] = getReprVertex(chainIndex.get(stack
							.get(stack.size() - 2)), edgeList.get(stack
							.get(stack.size() - 2)));
				}
				else
				{
					triangleVertices[1] = getReprVertex(chainIndex.get(stack
							.get(stack.size() - 2)), edgeList.get(stack
							.get(stack.size() - 2)));
					triangleVertices[2] = getReprVertex(chainIndex.get(v),
							edgeList.get(v));
				}
				while (stack.size() > 1
						&& 0 < isRightTurn(triangleVertices[0],
								triangleVertices[1], triangleVertices[2]))
				{
					if (chainIndex.get(u) == 1)
					{
						newEdge = new Edge(triangleVertices[0],
								triangleVertices[2], Color.red);

						edge = new Edge(triangleVertices[0],
								triangleVertices[1], null);
						edgeIndex = edgeList.indexOf(edge);
						triangelEdges[0] = edgeList.get(edgeIndex);

						triangelEdges[2] = newEdge;
					}
					else
					{
						newEdge = new Edge(triangleVertices[0],
								triangleVertices[1], Color.red);

						triangelEdges[0] = newEdge;

						edge = new Edge(triangleVertices[2],
								triangleVertices[0], null);
						edgeIndex = edgeList.indexOf(edge);
						triangelEdges[2] = edgeList.get(edgeIndex);
					}
					// hier entsteht ein Fehler, wenn die Kante eine der vorher
					// hinzugefügten Kanten ist
					// d.h. weder eine Randkante noch die lastnewedge
					edge = new Edge(triangleVertices[1], triangleVertices[2],
							null);
					edgeIndex = edgeList.indexOf(edge);
					triangelEdges[1] = edgeList.get(edgeIndex);

					edges.add(newEdge);
					triangles.add(new Triangle(triangleVertices, triangelEdges,
							Color.green));

					edgeList.add(newEdge);
					stack.pop();
					v = stack.peek();
					// get verticies for the next triangle
					if (stack.size() > 1)
					{
						triangleVertices = new Vertex[3];
						triangelEdges = new Edge[3];
						triangleVertices[0] = getReprVertex(chainIndex.get(u),
								edgeList.get(u));
						if (chainIndex.get(u) == 1)
						{
							triangleVertices[1] = getReprVertex(chainIndex
									.get(v), edgeList.get(v));
							triangleVertices[2] = getReprVertex(chainIndex
									.get(stack.get(stack.size() - 2)), edgeList
									.get(stack.get(stack.size() - 2)));
						}
						else
						{
							triangleVertices[1] = getReprVertex(chainIndex
									.get(stack.get(stack.size() - 2)), edgeList
									.get(stack.get(stack.size() - 2)));
							triangleVertices[2] = getReprVertex(chainIndex
									.get(v), edgeList.get(v));
						}
					}
				}
				stack.push(u);
			}
		}
		for (int i = 1; i < stack.size(); i++)
		{
			newEdge = new Edge(
					getReprVertex(chainIndex.get(u), edgeList.get(u)),
					getReprVertex(chainIndex.get(stack.get(i)), edgeList
							.get(stack.get(i))), Color.red);
			if (i < stack.size() - 1)
				edges.add(newEdge);

			triangleVertices = new Vertex[3];
			triangelEdges = new Edge[3];

			triangleVertices[0] = getReprVertex(chainIndex.get(u), edgeList
					.get(u));
			if (chainIndex.get(stack.peek()) == 1)
			{
				triangleVertices[1] = getReprVertex(chainIndex
						.get(stack.get(i)), edgeList.get(stack.get(i)));
				triangleVertices[2] = getReprVertex(chainIndex.get(stack
						.get(i - 1)), edgeList.get(stack.get(i - 1)));

				if (i < stack.size() - 1)
					triangelEdges[0] = newEdge;
				else
					triangelEdges[0] = edgeList.get(stack.peek());

				edge = new Edge(triangleVertices[1], triangleVertices[2], null);
				edgeIndex = edgeList.indexOf(edge);
				triangelEdges[1] = edgeList.get(edgeIndex);

				edge = new Edge(triangleVertices[2], triangleVertices[0], null);
				edgeIndex = edgeList.indexOf(edge);
				triangelEdges[2] = edgeList.get(edgeIndex);
			}
			else
			{
				triangleVertices[2] = getReprVertex(chainIndex
						.get(stack.get(i)), edgeList.get(stack.get(i)));
				triangleVertices[1] = getReprVertex(chainIndex.get(stack
						.get(i - 1)), edgeList.get(stack.get(i - 1)));

				edge = new Edge(triangleVertices[0], triangleVertices[1], null);
				edgeIndex = edgeList.indexOf(edge);
				triangelEdges[0] = edgeList.get(edgeIndex);

				edge = new Edge(triangleVertices[1], triangleVertices[2], null);
				edgeIndex = edgeList.indexOf(edge);
				triangelEdges[1] = edgeList.get(edgeIndex);

				if (i < stack.size() - 1)
					triangelEdges[2] = newEdge;
				else
					triangelEdges[2] = edgeList.get(chainIndex.size() - 1); // last
				// element
				// of
				// input
				// edges
			}
			triangles.add(new Triangle(triangleVertices, triangelEdges,
					Color.green));
			edgeList.add(newEdge);
		}
	}


	/**
	 * Returns the source or target of the edge, depnding on its type
	 * 
	 * @param type
	 *            The type of the edge (1 for source, 2 for target)
	 * @param e
	 *            The edge
	 * 
	 * @return A vertex
	 */
	private Vertex getReprVertex(
			int type,
			Edge e)
	{
		if (type == 1)
			return e.v1;
		else
			return e.v2;
	}


	/**
	 * Removes the pocket triangles
	 */
	private void removePocketTriangles()
	{
		HashSet<String> foundPolygonNames = new HashSet<String>();
		for (Edge e : pocketEdges)
		{
			e.color = Color.orange;
			for (Triangle t : e.getNeihbours())
			{
				if (t != null)
				{
					findPocketTriangles(t, foundPolygonNames);
					if (foundPolygonNames.size() == 1)
						removeTriangles(pocketTriangles);
					foundPolygonNames.clear();
					pocketTriangles.clear();
				}
			}
		}
	}


	/**
	 * Deletes all triangles in the set of the parameter
	 * 
	 * @param setOfTriangles
	 *            The triangles to delete
	 */
	private void removeTriangles(
			Stack<Triangle> setOfTriangles)
	{
		// delete the triangle from the list of triangles
		for (Triangle triangle : setOfTriangles)
		{
			triangles.remove(triangle);
			for (Edge e : triangle.edges)
			{
				if (e.polygonName == null)
				{
					// remove the deleted triangle from the list of neighbours
					// of its edges
					e.removeNeighbour(triangle);
					if (!e.hasNeighbours())
					{
						// remove the edge from the list
						edges.remove(e);
					}
				}
			}
		}
	}


	/**
	 * Finds pocket triangles
	 * 
	 * @param pocketTriangle
	 *            A pocket triangle
	 * @param foundPolygonNames
	 *            A set
	 */
	private void findPocketTriangles(
			Triangle pocketTriangle,
			HashSet<String> foundPolygonNames)
	{
		// add the triangle to the list of pocket triangles
		pocketTriangles.add(pocketTriangle);
		pocketTriangle.color = Color.yellow.brighter().brighter();
		Triangle[] neighbours;
		for (Edge e : pocketTriangle.edges)
		{
			if (e.polygonName == null)
			{ // only non polygon edges are interesting
				neighbours = e.getNeihbours();
				for (Triangle neighbour : neighbours)
				{
					if (neighbour != null
							&& !pocketTriangles.contains(neighbour))
					{
						findPocketTriangles(neighbour, foundPolygonNames);
					}
				}
			}
			else
			{
				foundPolygonNames.add(e.polygonName);
			}
		}
	}


	// ############### for debugging only
	// ############################################################

	/**
	 * For debugging only
	 */
	private void debugVertexSet()
	{
		System.out.println("ordered vertices:");
		for (Iterator<Vertex> iter = vertices.iterator(); iter.hasNext();)
		{
			System.out.println("\t" + iter.next().toString());
		}
	}


	/**
	 * For debugging only
	 */
	private void debugEdges()
	{
		System.out.println("edges:");
		for (Edge e : edges)
		{
			System.out.println(e.toString());
		}

	}


	/**
	 * For debugging only
	 */
	public void extractEdgesAsExample()
	{
		System.out.println("\tprivate void buildExample1(){");
		System.out.println("\t\tprocessor.reset();");
		for (Edge e : edges)
		{
			System.out.println("\t\tprocessor.addEdge(new Vertex(" + e.v1.x
					+ "," + e.v1.y + "), new Vertex(" + e.v2.x + "," + e.v2.y
					+ "),Color.black, \"polygon1\");");
		}
		System.out.println("\t}");
	}


	/**
	 * For debugging only
	 */
	private void debugEdgeSets()
	{
		System.out.println("outgoing edges:");
		for (Vertex v : edgesOUT.keySet())
		{
			System.out.println("\t" + v.toString() + ":");
			TreeSet<Edge> edgeset = edgesOUT.get(v);
			for (Iterator<Edge> iter = edgeset.iterator(); iter.hasNext();)
			{
				System.out.println("\t\t" + iter.next().toString());
			}
		}
		System.out.println("incoming edges:");
		for (Vertex v : edgesIN.keySet())
		{
			System.out.println("\t" + v.toString() + ":");
			TreeSet<Edge> edgeset = edgesIN.get(v);
			for (Iterator<Edge> iter = edgeset.iterator(); iter.hasNext();)
			{
				System.out.println("\t\t" + iter.next().toString());
			}
		}

	}


	/**
	 * For debugging only
	 */
	private void debugSweepStatus()
	{
		Iterator<SweepableObj> iter = sweepStatus.iterator();
		String str = "sweep line status =";
		while (iter.hasNext())
		{
			SweepableObj obj = iter.next();
			if (obj instanceof Vertex)
			{
				Vertex v = (Vertex) obj;
				str += " V" + v.nr;
			}
			else
			{
				Edge e = (Edge) obj;
				str += " E" + e.nr;
			}
		}
		System.out.println(str);
	}


	/**
	 * For debugging only
	 */
	private void debugChainSet()
	{
		String str;
		for (int i = 0; i < chains.size(); i++)
		{
			str = "chain " + (i + 1) + " :";
			for (Edge e : chains.get(i))
			{
				str += " " + e.v1.toString();
			}
			str += chains.get(i).peek().v2.toString();
			System.out.println(str);
		}
	}


	/**
	 * For debugging only
	 * 
	 * @param chainIndex
	 *            Vector_Integer
	 * @param edgeList
	 *            List of edges
	 */
	public void debugVectorForTriangulation(
			Vector<Integer> chainIndex,
			Vector<Edge> edgeList)
	{
		String str = "triangulate: ";
		for (int j = 0; j < edgeList.size(); j++)
		{
			if (chainIndex.get(j) == 1)
				str += edgeList.get(j).v1.toString() + " ";
			else
				str += edgeList.get(j).v2.toString() + " ";
		}
		System.out.println(str);
	}
}
