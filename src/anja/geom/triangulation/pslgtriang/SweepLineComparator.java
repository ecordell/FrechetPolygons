/**
 * Copyright: CCPL BY-NC-SA 3.0 (cc) 2008 Jan Tulke <br> Creative Commons Public
 * Licence. Some Rights Reserved.<br>
 * http://creativecommons.org/licenses/by-nc-sa/3.0/de/ <br> If you use this
 * code, please send a message to one of the<br> author:
 * jan.tulke@hochtief.de<br>
 */

package anja.geom.triangulation.pslgtriang;


import java.util.Comparator;


public class SweepLineComparator
		implements Comparator<SweepableObj>
{

	// actual position of the sweepline
	private double	sweepX;


	/**
	 * The compare method, inherited from {@link java.util.Comparator}
	 */
	public int compare(
			SweepableObj obj1,
			SweepableObj obj2)
	{
		if (obj1 instanceof Vertex && obj2 instanceof Vertex)
		{
			Vertex v1 = (Vertex) obj1;
			Vertex v2 = (Vertex) obj2;
			return v1.compareInvertedTo(v2);
		}
		else if (obj1 instanceof Vertex && obj2 instanceof Edge)
		{
			Vertex v = (Vertex) obj1;
			double obj1_Y = v.y;
			double obj2_Y = getIntersectionY((Edge) obj2, sweepX);
			if (obj1_Y < obj2_Y)
				return -1;
			else if (obj1_Y > obj2_Y)
				return +1;
			else
				return 0;
		}
		else if (obj1 instanceof Edge && obj2 instanceof Vertex)
		{
			Vertex v = (Vertex) obj2;
			double obj1_Y = getIntersectionY((Edge) obj1, sweepX);
			double obj2_Y = v.y;
			if (obj1_Y < obj2_Y)
				return -1;
			else if (obj1_Y > obj2_Y)
				return +1;
			else
				return 0;
		}
		else
		{
			Edge e1 = (Edge) obj1;
			Edge e2 = (Edge) obj2;
			double obj1_Y = getIntersectionY(e1, sweepX);
			double obj2_Y = getIntersectionY(e2, sweepX);
			if (obj1_Y == obj2_Y)
				return e1.compareTo(e2);
			else if (obj1_Y < obj2_Y)
				return -1;
			else
				return +1;
		}
	}


	/**
	 * Sets the actual position of the sweepline
	 * 
	 * @param e
	 *            The line
	 * @param x
	 *            The position
	 * 
	 * @return The y value of the intersection
	 */
	private double getIntersectionY(
			Edge e,
			double x)
	{
		double y1 = e.v1.y;
		double x1 = e.v1.x;
		double y2 = e.v2.y;
		double x2 = e.v2.x;
		if (x1 == x2)
			return y1;
		else
			return y1 + (y2 - y1) * (x - x1) / (x2 - x1);

	}


	/**
	 * Setter for the position of the sweep line
	 * 
	 * @param x
	 *            The new position of the sweep line
	 */
	public void setSweepX(
			double x)
	{
		this.sweepX = x;
	}

}
