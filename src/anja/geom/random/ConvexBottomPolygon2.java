package anja.geom.random;


import java.util.Collections;
import java.util.Vector;

import anja.geom.ConvexPolygon2;
import anja.geom.Point2;
import anja.geom.Point2List;
import anja.geom.PointComparator;
import anja.geom.Polygon2;
import anja.geom.Segment2;


/**
 * Erzeugung eines Zufallspolygons anhand des <i>Convex Bottom</i>-Algorithmus,
 * beschrieben in: <b>Polygonization of point sets</b>
 * 
 * (<a href="http://www.CS.McGill.CA/~ktulu/507/">
 * http://www.CS.McGill.CA/~ktulu/507/</a>)
 * 
 * @version 0.9 30.12.02
 * @author Thomas Kamphans, Sascha Ternes
 */

public class ConvexBottomPolygon2
		extends RandomPolygon2
{

	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt ein neues Polygon anhand des <i>Convex Bottom</i>-Algorithmus mit
	 * zufaelligen Punkten. Als Polygonparameter dienen Defaultwerte.
	 */
	public ConvexBottomPolygon2()
	{

		super(DefaultPolyMinPoints, DefaultPolyMaxPoints, DefaultPolyWidth,
				DefaultPolyHeight);

	} // ConvexBottomPolygon2


	/**
	 * Erzeugt ein neues Polygon anhand des <i>Convex Bottom</i>-Algorithmus mit
	 * zufaelligen Punkten, wobei die Polygonparameter uebergeben werden. Die
	 * Anzahl der Punkte wird innerhalb der uebergebenen Grenzen zufaellig
	 * gewaehlt, die Ausdehnung des Polygons erreicht maximal die uebergebenen
	 * Werte fuer Hoehe und Breite.
	 * 
	 * @param poly_min_points
	 *            Untergrenze der Punktanzahl
	 * @param poly_max_points
	 *            Obergrenze der Punktanzahl
	 * @param width
	 *            maximale horizontale Ausdehnung des Polygons
	 * @param height
	 *            maximale vertikale Ausdehnung des Polygons
	 */
	public ConvexBottomPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height)
	{

		super(poly_min_points, poly_max_points, width, height);

	} // ConvexBottomPolygon2


	/**
	 * Erzeugt ein neues Polygon anhand des <i>Convex Bottom</i>-Algorithmus aus
	 * den uebergebenen Punkten.
	 * 
	 * @param poly_min_points
	 *            Untergrenze der Punktanzahl
	 * @param poly_max_points
	 *            Obergrenze der Punktanzahl
	 * @param width
	 *            maximale horizontale Ausdehnung des Polygons
	 * @param height
	 *            maximale vertikale Ausdehnung des Polygons
	 * @param points
	 *            die Punkte des Polygons
	 */
	public ConvexBottomPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height,
			Point2List points)
	{

		super(poly_min_points, poly_max_points, width, height, points);

	} // ConvexBottomPolygon2


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/*
	* [javadoc-Beschreibung wird aus RandomPolygon2.java kopiert]
	*/
	public String toString()
	{

		return "ConvexBottomPolygon2[n=" + _n + "]";

	} // toString


	//**************************************************************************
	// Private methods
	//**************************************************************************

	/*
	* [javadoc-Beschreibung wird aus RandomPolygon2.java kopiert]
	*/
	protected void _init()
	{

		Point2 min, max, p;
		Segment2 cut;
		PointComparator comparator;
		Vector points_left, points_right;
		int i, n, x_min, x_max, i_min, i_max;
		Point2List pl;
		ConvexPolygon2 hull, hulltemp;

		// find min and max points:
		x_min = Integer.MAX_VALUE;
		i_min = 0;
		x_max = Integer.MIN_VALUE;
		i_max = 0;

		for (i = 0; i < _random_points.size(); i++)
		{
			p = (Point2) _random_points.elementAt(i);
			if (p.x < x_min)
			{
				x_min = (int) p.x;
				i_min = i;
			}
			if (p.x > x_max)
			{
				x_max = (int) p.x;
				i_max = i;
			}
		} // for

		min = (Point2) _random_points.elementAt(i_min);
		max = (Point2) _random_points.elementAt(i_max);

		if (i_min < i_max)
		{
			_random_points.remove(i_max);
			_random_points.remove(i_min);
		}
		else
		{
			_random_points.remove(i_min);
			_random_points.remove(i_max);
		}

		// divide list of points:
		cut = new Segment2(min, max);
		points_left = new Vector();
		points_right = new Vector();
		points_left.add(min);
		_divideSpace(_random_points, cut, points_left, points_right);
		points_left.add(max);

		// construct convex hull for 'lower' hemisphere:
		pl = new Point2List();
		for (i = 0; i < points_left.size(); i++)
		{
			pl.addPoint((Point2) points_left.elementAt(i));
		}
		hull = new ConvexPolygon2(pl);

		//*** construct polygon: ***

		hulltemp = new ConvexPolygon2(hull);

		// take care of the convex hull's orientation:
		if (min.y < max.y)
		{
			hulltemp.removeFirstPoint(); // this is min
			addPoint(min);
		}

		while (!hulltemp.empty())
		{
			p = hulltemp.lastPoint();
			hulltemp.removeLastPoint();
			addPoint(p);
			_random_points.removeElement(p);
			points_left.removeElement(p);
		}

		// remove points on the convex polygon from
		// _random_points. Could be more efficient with
		// a new implementation of ConvexPolygon2
		for (i = 0; i < points_left.size(); i++)
		{
			p = (Point2) points_left.elementAt(i);
			int test = hull.locatePoint(p);
			if (test == Polygon2.POINT_ON_EDGE)
			{
				_random_points.removeElement(p);
			}
		} // for

		// sort remaining points:
		comparator = new PointComparator(PointComparator.X_ORDER,
				PointComparator.DESCENDING);

		synchronized (_random_points)
		{
			Collections.sort(_random_points, comparator);
		}

		n = _random_points.size();
		for (i = 0; i < n; i++)
		{
			addPoint((Point2) _random_points.elementAt(i));
		}

	} // _init

} // ConvexBottomPolygon2
