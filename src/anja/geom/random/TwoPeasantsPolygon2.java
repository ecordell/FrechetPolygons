package anja.geom.random;


import java.util.Collections;
import java.util.Vector;

import anja.geom.Point2;
import anja.geom.Point2List;
import anja.geom.PointComparator;
import anja.geom.Segment2;


/**
 * Erzeugung eines Zufallspolygons anhand des <i>2 Peasants</i>-Algorithmus,
 * beschrieben in: <b>Polygonization of point sets</b>
 * 
 * (<a href="http://www.CS.McGill.CA/~ktulu/507/">
 * http://www.CS.McGill.CA/~ktulu/ 507/</a>)
 * 
 * @version 0.9 26.12.02
 * @author Thomas Kamphans, Sascha Ternes
 */

public class TwoPeasantsPolygon2
		extends RandomPolygon2
{

	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt ein neues Polygon anhand des <i>2 Peasants</i>-Algorithmus mit
	 * zufaelligen Punkten. Als Polygonparameter dienen Defaultwerte.
	 */
	public TwoPeasantsPolygon2()
	{

		super(DefaultPolyMinPoints, DefaultPolyMaxPoints, DefaultPolyWidth,
				DefaultPolyHeight);

	} // TwoPeasantsPolygon2


	/**
	 * Erzeugt ein neues Polygon anhand des <i>2 Peasants</i>-Algorithmus mit
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
	public TwoPeasantsPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height)
	{

		super(poly_min_points, poly_max_points, width, height);

	} // TwoPeasantsPolygon2


	/**
	 * Erzeugt ein neues Polygon anhand des <i>2 Peasants</i>-Algorithmus aus
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
	public TwoPeasantsPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height,
			Point2List points)
	{

		super(poly_min_points, poly_max_points, width, height, points);

	} // TwoPeasantsPolygon2


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/*
	* [javadoc-Beschreibung wird aus RandomPolygon2.java kopiert]
	*/
	public String toString()
	{

		return "TwoPeasantsPolygon2[n=" + _n + "]";

	} // toString


	//**************************************************************************
	// Private methods
	//**************************************************************************

	/*
	* [javadoc-Beschreibung wird aus anja.geom.random.RandomPolygon2.java kopiert]
	*/
	protected void _init()
	{

		Point2 min, max;
		Segment2 cut;
		PointComparator comparator;
		Vector points_left, points_right;
		int i, n;

		// sort points:
		comparator = new PointComparator(PointComparator.X_ORDER,
				PointComparator.ASCENDING);

		synchronized (_random_points)
		{
			Collections.sort(_random_points, comparator);
		}
		// divide list of points:
		min = (Point2) _random_points.remove(0);
		max = (Point2) _random_points.remove(_random_points.size() - 1);

		cut = new Segment2(min, max);
		points_left = new Vector();
		points_right = new Vector();
		_divideSpace(_random_points, cut, points_left, points_right);

		// construct polygon:
		addPoint(min);
		n = points_left.size();
		for (i = 0; i < n; i++)
		{
			addPoint((Point2) points_left.elementAt(i));
		}
		addPoint(max);
		n = points_right.size();
		for (i = n - 1; i > -1; i--)
		{
			addPoint((Point2) points_right.elementAt(i));
		}

	} // _init

} // TwoPeasantsPolygon2
