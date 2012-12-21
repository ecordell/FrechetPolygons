/**
 * Copyright: CCPL BY-NC-SA 3.0 (cc) 2008 Jan Tulke <br> Creative Commons Public
 * Licence. Some Rights Reserved.<br>
 * http://creativecommons.org/licenses/by-nc-sa/3.0/de/ <br> If you use this
 * code, please send a message to one of the<br> author:
 * jan.tulke@hochtief.de<br>
 */

package anja.geom.triangulation.pslgtriang;


import java.util.HashSet;


public class Vertex
		implements Comparable<Vertex>, SweepableObj
{

	// static double eps = 1e-6; // used to test if two verices are equals
	public double			x;
	public double			y;
	public long				nr;
	static long				maxNr		= 0;
	public HashSet<String>	belongsTo	= new HashSet<String>();


	/**
	 * Creates a vertex object, receiving x- and y-coordinate as parameters
	 * 
	 * @param x
	 *            The x value of the vertex
	 * @param y
	 *            The y value of the vertex
	 * @param numberOfDigits
	 *            The number of digits as a pow of 10
	 */
	public Vertex(
			double x,
			double y,
			int numberOfDigits)
	{
		nr = ++maxNr;
		double digits = Math.pow(10, numberOfDigits);
		this.x = Math.round(x * digits) / digits;
		this.y = Math.round(y * digits) / digits;
	}


	/**
	 * Creates a vertex object, receiving x- and y-coordinate as parameters and
	 * the polygon, the vertex belongs to.
	 * 
	 * @param x
	 *            The x value of the vertex
	 * @param y
	 *            The y value of the vertex
	 * @param polygonName
	 *            The name of the polygon, the vertex belongs to
	 * @param numberOfDigits
	 *            The number of digits as a pow of 10
	 */
	public Vertex(
			double x,
			double y,
			String polygonName,
			int numberOfDigits)
	{
		nr = ++maxNr;
		double digits = Math.pow(10, numberOfDigits);
		this.x = Math.round(x * digits) / digits;
		this.y = Math.round(y * digits) / digits;
		belongsTo.add(polygonName);
	}


	/**
	 * Creates a vertex object, receiving x- and y-coordinate as parameters
	 * 
	 * @param x
	 *            The x value of the vertex
	 * @param y
	 *            The y value of the vertex
	 */
	public Vertex(
			double x,
			double y)
	{
		nr = ++maxNr;
		this.x = x;
		this.y = y;
	}


	/**
	 * Creates a vertex object, receiving x- and y-coordinate as parameters and
	 * the polygon, the vertex belongs to.
	 * 
	 * @param x
	 *            The x value of the vertex
	 * @param y
	 *            The y value of the vertex
	 * @param polygonName
	 *            The name of the polygon, the vertex belongs to
	 */
	public Vertex(
			double x,
			double y,
			String polygonName)
	{
		nr = ++maxNr;
		this.x = x;
		this.y = y;
		belongsTo.add(polygonName);
	}


	/**
	 * Setter for the name of the polygon, the vertex belongs to
	 * 
	 * @param polygonName
	 *            The new name of the polygon, the vertex belongs to
	 */
	public void assignMemberOf(
			String polygonName)
	{
		belongsTo.add(polygonName);
	}


	// ###### without eps environment #####
	//    
	// @Override
	// public boolean equals(Object obj){
	// if (obj instanceof Vertex) {
	// Vertex v = (Vertex) obj;
	// if(x==v.x && y==v.y) return true;
	// else return false;
	// }else{
	// return false;
	// }
	// }

	// public int compareTo(Vertex v){
	// if(x>v.x || x==v.x && y>v.y) return 1;
	// else if(x<v.x || x==v.x && y<v.y)return -1;
	// else return 0;
	// }

	// public int compareInvertedTo(Vertex v){
	// if(y>v.y || y==v.y && x>v.x) return 1;
	// else if(y<v.y || y==v.y && x<v.x)return -1;
	// else return 0;
	// }

	// ###### with eps environment #####
	// public static void setTolerance(double tol)
	// {
	// eps = tol;
	// }

	// @Override
	// public boolean equals(Object obj){
	// if (obj instanceof Vertex) {
	// Vertex v = (Vertex) obj;
	// double dx = x-v.x;
	// double dy = y-v.y;
	// if(dx*dx+dy*dy<eps) return true;
	// else return false;
	// }else{
	// return false;
	// }
	// }

	@Override
	public boolean equals(
			Object obj)
	{
		if (obj instanceof Vertex)
		{
			Vertex v = (Vertex) obj;
			if (x == v.x && y == v.y)
				return true;
			else
				return false;
		}
		else
		{
			return false;
		}
	}


	/**
	 * Inherited from {@link java.lang.Comparable}
	 */
	public int compareTo(
			Vertex v)
	{
		if (this.equals(v))
			return 0;
		if (x > v.x || x == v.x && y > v.y)
			return 1;
		return -1;
	}


	/**
	 * Comapres two vertices, switching the positions of y- and x- coordinate
	 * during the comparison
	 * 
	 * @param v
	 *            The vertex to compare
	 * 
	 * @return 0, 1 or -1
	 * 
	 * @see anja.geom.triangulation.pslgtriang.Vertex#compareTo(Vertex)
	 */
	public int compareInvertedTo(
			Vertex v)
	{
		if (this.equals(v))
			return 0;
		if (y > v.y || y == v.y && x > v.x)
			return 1;
		return -1;
	}


	/**
	 * Compares the two vertices by their y value returns +1 0 -1 if this vertex
	 * is greater, equals or less than vertex v.
	 * 
	 * @param v
	 *            The vertex to compare
	 * 
	 * @return 0, 1 or -1
	 */
	public int yCompareTo(
			Vertex v)
	{
		if (y > v.y)
			return 1;
		else if (y == v.y)
			return 0;
		else
			return -1;
	}


	/**
	 * Overwritten to be able to find the right bucket in an hashtable
	 */
	@Override
	public int hashCode()
	{
		return (int) x;
	}


	/**
	 * Print the content of this object to System.out
	 */
	public void print()
	{
		System.out.println((new StringBuilder()).append("x=").append(x).append(
				" y=").append(y).toString());
	}


	/**
	 * Return the content of this object as a string
	 * 
	 * @return A string containing the value of this object
	 */
	public String toString()
	{
		return "V" + nr + "<" + x + "," + y + ">";
	}


	/**
	 * Resets the maxNr variable
	 */
	public static void resetNumbering()
	{
		maxNr = 0;
	}
}
