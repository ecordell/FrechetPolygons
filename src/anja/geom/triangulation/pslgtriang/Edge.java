/**
 * Copyright: CCPL BY-NC-SA 3.0 (cc) 2008 Jan Tulke <br> Creative Commons Public
 * Licence. Some Rights Reserved.<br>
 * http://creativecommons.org/licenses/by-nc-sa/3.0/de/ <br> If you use this
 * code, please send a message to one of the<br> author:
 * jan.tulke@hochtief.de<br>
 */

package anja.geom.triangulation.pslgtriang;


import java.awt.Color;

import anja.geom.Point2;
import anja.geom.Segment2;


public class Edge
		implements Comparable<Edge>, SweepableObj
{

	static long			maxNr		= 0;
	public long			nr;
	public Vertex		v1;
	public Vertex		v2;
	public Color		color;
	// name of the polygon the edge belongs to, null if the edge belongs to no
	// polygon
	public String		polygonName;
	public int			weight;
	private Triangle[]	neighbours	= new Triangle[2];


	/**
	 * Creates a new edge from v1 to v2, which belongs to a certain polygon
	 * 
	 * @param v1
	 *            The first vertex (source)
	 * @param v2
	 *            The second vertex (target)
	 * @param color
	 *            The color
	 * @param polygonName
	 *            The name of the polygon, the edge belongs to
	 */
	public Edge(
			Vertex v1,
			Vertex v2,
			Color color,
			String polygonName)
	{
		nr = ++maxNr;
		this.v1 = v1;
		this.v2 = v2;
		this.color = color;
		this.polygonName = polygonName;
		checkOrder();
	}


	/**
	 * Creates a new edge from v1 to v2, which belongs to a certain polygon.
	 * 
	 * The polygonName variable is set to NULL in this constructor.
	 * 
	 * @param v1
	 *            The first vertex (source)
	 * @param v2
	 *            The second vertex (target)
	 * @param color
	 *            The color
	 */
	public Edge(
			Vertex v1,
			Vertex v2,
			Color color)
	{
		nr = ++maxNr;
		this.v1 = v1;
		this.v2 = v2;
		this.color = color;
		this.polygonName = null;
		checkOrder();
	}


	/**
	 * Creates a new edge from p1 to p2, which belongs to a certain polygon
	 * 
	 * @param p1
	 *            The first vertex (source)
	 * @param p2
	 *            The second vertex (target)
	 * @param color
	 *            The color
	 * @param polygonName
	 *            The name of the polygon, the edge belongs to
	 */
	public Edge(
			Point2 p1,
			Point2 p2,
			Color color,
			String polygonName)
	{
		nr = ++maxNr;
		this.v1 = new Vertex(p1.x, p1.y);
		this.v2 = new Vertex(p2.x, p2.y);
		this.color = color;
		this.polygonName = polygonName;
		checkOrder();
	}


	/**
	 * Creates a new edge from a segment, which belongs to a certain polygon
	 * 
	 * @param seg
	 *            The segment mirroring the edge
	 * @param color
	 *            The color
	 * @param polygonName
	 *            The name of the polygon, the edge belongs to
	 */
	public Edge(
			Segment2 seg,
			Color color,
			String polygonName)
	{
		nr = ++maxNr;
		this.v1 = new Vertex(seg.source().x, seg.source().y);
		this.v2 = new Vertex(seg.target().x, seg.target().y);
		this.color = color;
		this.polygonName = polygonName;
		checkOrder();
	}


	/**
	 * Checks the order of each the edge and changes it, if the order isn't
	 * correct.
	 */
	public void checkOrder()
	{
		if (!isCorrectlyOrdered())
			changeOrder();
	}


	/**
	 * Checks the order of the 2 vertices of this edge.
	 * 
	 * @return true, if they are correctly ordered after the comparison of the
	 *         {@link anja.geom.triangulation.pslgtriang.Vertex#compareTo(anja.geom.triangulation.pslgtriang.Vertex)}
	 *         method, false else
	 */
	public boolean isCorrectlyOrdered()
	{
		if (v1.compareTo(v2) > 0)
			return false;
		else
			return true;
	}


	/**
	 * Switches the order of the vertices
	 */
	public void changeOrder()
	{
		Vertex v = v1;
		v1 = v2;
		v2 = v;
	}


	/**
	 * Adds a neighbour to the list of neighbours of this edge.
	 * 
	 * @param t
	 *            The new neighbour
	 * 
	 * @return true, if the new neighbour has succesfully been added, false, if
	 *         both neighbours are occupied
	 */
	public boolean addNeighbour(
			Triangle t)
	{
		if (neighbours[0] == null)
		{
			neighbours[0] = t;
			return true;
		}
		else if (neighbours[1] == null)
		{
			neighbours[1] = t;
			return true;
		}
		return false;
	}


	/**
	 * Removes a neighbour for this edge.
	 * 
	 * @param t
	 *            The triangle t to remove from the neighbour list
	 */
	public void removeNeighbour(
			Triangle t)
	{
		if (neighbours[0] == t)
			neighbours[0] = null;
		else if (neighbours[1] == t)
			neighbours[1] = null;
	}


	/**
	 * Getter for the neighbour triangles of the edge.
	 * 
	 * @return An array of triangles (length 1 or 2)
	 */
	public Triangle[] getNeihbours()
	{
		return neighbours;
	}


	/**
	 * Checks for neighbours of this edge
	 * 
	 * @return true, if there is at least one neighbour, false else
	 */
	public boolean hasNeighbours()
	{
		return neighbours[0] != null || neighbours[1] != null;
	}


	/**
	 * Setter for the weight
	 * 
	 * @param i
	 *            The new weight
	 */
	public void setW(
			int i)
	{
		weight = i;
	}


	/**
	 * Checks if a vertex is located on this edge
	 * 
	 * @param v
	 *            Vertex to be tested
	 * 
	 * @return ture if the vertex is on the edge (in its interior or equals with
	 *         one of the endpoints)
	 */
	public boolean containsVertex(
			Vertex v)
	{
		// if edge is vertical
		if (v1.x == v2.x)
			return v.x == v1.x && v.y >= v1.y && v.y <= v2.y;
		// all other cases
		if (v.x >= v1.x && v.x <= v2.x)
			return (v.y - v1.y) * (v2.x - v1.x) - (v2.y - v1.y) * (v.x - v1.x) == 0;
		else
			return false;
	}


	/**
	 * Checks if the vertices are part of the same polygon
	 * 
	 * @return true, if they belong to the same polygon, false else
	 */
	public boolean verticesOnSamePolygon()
	{
		for (String polygonName : v1.belongsTo)
		{
			if (v2.belongsTo.contains(polygonName))
				return true;
		}
		return false;
	}


	@Override
	public boolean equals(
			Object obj)
	{
		if (obj instanceof Edge)
		{
			Edge e = (Edge) obj;
			if (v1.equals(e.v1) && v2.equals(e.v2))
				return true;
			return v1.equals(e.v2) && v2.equals(e.v1);
		}
		else
			return false;
	}


	@Override
	public int hashCode()
	{
		return (int) v1.x;
	}


	/**
	 * Prints the content of this object to System.out
	 */
	public void print()
	{
		System.out.print((new StringBuilder()).append(v1.x).append(" ").append(
				v1.y).append(" ").append(v2.x).append(" ").append(v2.y)
				.toString());
	}


	/**
	 * Checks if an edges cross this edge (this object)
	 * 
	 * @param edge
	 *            The second edge
	 * 
	 * @return true if the two edges intersect in the interior or in one of the
	 *         vertices or if they are equals
	 */
	public boolean cross(
			Edge edge)
	{
		// Concider that all edges were correctly ordered during creation
		// (increasing x and than y)
		if (v2.x < edge.v1.x || edge.v2.x < v1.x)
			return false;
		// both edges are vertical and in line, both are directed in pos y
		// direction
		if (edge.v1.x == edge.v2.x && edge.v1.x == v1.x && v1.x == v2.x)
			return edge.containsVertex(v1) || edge.containsVertex(v2)
					|| this.containsVertex(edge.v1)
					|| this.containsVertex(edge.v2);
		// both edges are vertical but not in line
		if (edge.v1.x == edge.v2.x && v1.x == v2.x)
			return false;
		double i = (edge.v1.y - v1.y) * (v2.x - v1.x) - (v2.y - v1.y)
				* (edge.v1.x - v1.x);
		double k = (edge.v2.y - v1.y) * (v2.x - v1.x) - (v2.y - v1.y)
				* (edge.v2.x - v1.x);
		if (i >= 0 && k <= 0 || i <= 0 && k >= 0)
		{
			double j = (v1.y - edge.v1.y) * (edge.v2.x - edge.v1.x)
					- (edge.v2.y - edge.v1.y) * (v1.x - edge.v1.x);
			double l = (v2.y - edge.v1.y) * (edge.v2.x - edge.v1.x)
					- (edge.v2.y - edge.v1.y) * (v2.x - edge.v1.x);
			return j >= 0 && l <= 0 || j <= 0 && l >= 0;
		}
		else
		{
			return false;
		}
	}


	/**
	 * Tests if two edges connect/abut each other
	 * 
	 * @param edge
	 *            Edge to be testet
	 * 
	 * @return true if edge to be tested correctly connects with this edge in
	 *         case of equal edges false is returned
	 */
	public boolean abut(
			Edge edge)
	{
		// Concider that all edges were correctly ordered during creation
		// (increasing x and than y)
		// both start points are connected -> second vertex shouldn't be
		// collinear
		if (v1.equals(edge.v1))
			return !(edge.containsVertex(v2) || this.containsVertex(edge.v2));
		// both end points are connected -> first vertex shouldn't be collinear
		if (v2.equals(edge.v2))
			return !(edge.containsVertex(v1) || this.containsVertex(edge.v1));
		// new edge connects at the end point of this edge -> o.k.
		if (v2.equals(edge.v1))
			return true;
		// new edge connects at the start point of this edge -> o.k.
		// if non of the cases -> edges are not correctly connected
		return v1.equals(edge.v2);
	}


	/**
	 * Compares two edges with each other (both should share a commen point) for
	 * outgoing edges the range (0° to 180°) is concidered for incoming edges
	 * the range (180° to 360°) is concidered.
	 * 
	 * @param edge
	 *            Compared to this edge
	 * 
	 * @return -1 if this edge is above edge, +1 if this edge is beneath edge, 0
	 *         if both edges have the same gradient
	 */
	public int compareTo(
			Edge edge)
	{
		// if both edges are equals
		if (v1.equals(edge.v1) && v2.equals(edge.v2))
			return 0;
		// if both edges share the same start point -> out going edges
		if (v1.equals(edge.v1))
		{
			// if this edge is vertical
			if (v1.x == v2.x)
				return +1;
			// if concidered edge is vertical
			else if (edge.v1.x == edge.v2.x)
				return -1;
			// all other cases
			else
			{
				// gradient of this edge
				double d1 = v2.y - v1.y;
				double d2 = v2.x - v1.x;
				double m1 = d1 / d2;
				// gradient of concidered edge
				d1 = edge.v2.y - edge.v1.y;
				d2 = edge.v2.x - edge.v1.x;
				double m2 = d1 / d2;
				if (m1 > m2)
					return +1;
				if (m1 < m2)
					return -1;
				else
					return 0;
			}
		}
		// if both edges share the same end point -> in coming edges
		else
		{
			// if this edge is vertical
			if (v1.x == v2.x)
				return -1;
			// if concidered edge is vertical
			else if (edge.v1.x == edge.v2.x)
				return +1;
			// all other cases
			else
			{
				// gradient of this edge
				double d1 = v2.y - v1.y;
				double d2 = v2.x - v1.x;
				double m1 = d1 / d2;
				// gradient of concidered edge
				d1 = edge.v2.y - edge.v1.y;
				d2 = edge.v2.x - edge.v1.x;
				double m2 = d1 / d2;
				if (m1 > m2)
					return -1;
				else if (m1 < m2)
					return +1;
				else
					return 0;
			}
		}

		// both edges vertical is not possible since vertical edges always have
		// y1<y2 (order() in Edge)
		// so they can't be out going and in coming edges at the same time for a
		// concidered vertex
	}


	/**
	 * Creates a string, that contains the content of this object
	 * 
	 * @return A string
	 */
	public String toString()
	{
		return "E" + nr + ": " + v1.toString() + " " + v2.toString();
	}


	/**
	 * Resets the maxNr variable.
	 */
	public static void resetNumbering()
	{
		maxNr = 0;
	}

}
