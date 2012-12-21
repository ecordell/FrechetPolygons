/**
 * Copyright: CCPL BY-NC-SA 3.0 (cc) 2008 Jan Tulke <br> Creative Commons Public
 * Licence. Some Rights Reserved.<br>
 * http://creativecommons.org/licenses/by-nc-sa/3.0/de/ <br> If you use this
 * code, please send a message to one of the<br> author:
 * jan.tulke@hochtief.de<br>
 */

package anja.geom.triangulation.pslgtriang;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import anja.geom.Segment2;
import anja.geom.Point2;
import anja.util.Drawable;
import anja.util.GraphicsContext;


public class Triangle
		implements Drawable
{

	static long		maxNr	= 0;
	public long		nr;

	// vertices in counter clockwise order
	public Vertex[]	vertices;
	/* edges in counter clockwise order 
	   (be aware, that the order of the vertices of an edge can be the other way round) */
	public Edge[]	edges;
	public Color	color;


	/**
	 * Constructor
	 * 
	 * @param vertices
	 *            An array of vertices
	 * @param edges
	 *            An array of edges
	 * @param c
	 *            The color
	 */
	public Triangle(
			Vertex[] vertices,
			Edge[] edges,
			Color c)
	{
		nr = ++maxNr;
		this.vertices = vertices;
		this.edges = edges;
		this.color = c;
		for (Edge e : edges)
		{
			e.addNeighbour(this);
		}
	}


	/**
	 * Creates a representation of this object
	 * 
	 * @return A String representing this object
	 */
	public String toString()
	{
		String str = new String();
		str += vertices[0].toString() + " ";
		str += vertices[1].toString() + " ";
		str += vertices[2].toString() + " ";
		return str;
	}


	/**
	 * Prints the content of this object to the screen
	 */
	public void print()
	{
		System.out.println("T" + nr + ":");
		System.out.println("\tVertices: " + toString());
		String str = new String();
		str += "\tEdges: ";
		str += edges[0].toString() + " ";
		str += edges[1].toString() + " ";
		str += edges[2].toString() + " ";
		System.out.println(str);
	}


	/**
	 * Resets the (maximum) number to zero
	 */
	public static void resetNumbering()
	{
		maxNr = 0;
	}


	/**
	 * Getter for the vertices of the triangle
	 * 
	 * @return Three vertices, marking the triangle
	 */
	public Vertex[] getTriangle()
	{
		return vertices;
	}


	/**
	 * The draw method allows the triangle itself into the given Graphics2D
	 * object, using the given GraphicsContext. Only the edges of the triangle
	 * are drawn.
	 * 
	 * @param g
	 *            The {@link java.awt.Graphics2D} object, the triangle should be
	 *            drawn to
	 * @param gc
	 *            The {@link anja.util.GraphicsContext} of the drawing
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{

		if (vertices != null && vertices.length == 3)
		{

			Segment2 seg = new Segment2(
					new Point2(vertices[0].x, vertices[0].y), new Point2(
							vertices[1].x, vertices[1].y));
			seg.draw(g, gc);

			seg = new Segment2(new Point2(vertices[1].x, vertices[1].y),
					new Point2(vertices[2].x, vertices[2].y));
			seg.draw(g, gc);

			seg = new Segment2(new Point2(vertices[2].x, vertices[2].y),
					new Point2(vertices[0].x, vertices[0].y));
			seg.draw(g, gc);

			// Anfangspunkt zeichnen

		} // if

	} // draw


	/**
	 * Inherited from {@link anja.util.Drawable}, but not implemented yet
	 * 
	 * @param box
	 *            The rectangle
	 * 
	 * @return false
	 */
	public boolean intersects(
			Rectangle2D box)
	{
		// Not implemented
		return false;
	}

}
