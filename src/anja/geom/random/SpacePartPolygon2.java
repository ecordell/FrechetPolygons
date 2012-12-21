package anja.geom.random;


import java.util.Vector;

import anja.geom.Point2;
import anja.geom.Point2List;
import anja.geom.Segment2;


/**
 * Erzeugung eines Zufallspolygons durch <i>space partioning</i>, beschrieben
 * in:<br> <i>Auer/Held: RPG - Heuristics for the generation of random
 * polygons</i>
 * 
 * @version 0.9 30.12.02
 * @author Thomas Kamphans, Sascha Ternes
 */

public class SpacePartPolygon2
		extends RandomPolygon2
{

	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt ein neues Polygon durch <i>space partitioning</i> mit zufaelligen
	 * Punkten. Als Polygonparameter dienen Defaultwerte.
	 */
	public SpacePartPolygon2()
	{

		super(DefaultPolyMinPoints, DefaultPolyMaxPoints, DefaultPolyWidth,
				DefaultPolyHeight);

	} // SpacePartPolygon2


	/**
	 * Erzeugt ein neues Polygon durch <i>space partitioning</i> mit zufaelligen
	 * Punkten, wobei die Polygonparameter uebergeben werden. Die Anzahl der
	 * Punkte wird innerhalb der uebergebenen Grenzen zufaellig gewaehlt, die
	 * Ausdehnung des Polygons erreicht maximal die uebergebenen Werte fuer
	 * Hoehe und Breite.
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
	public SpacePartPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height)
	{

		super(poly_min_points, poly_max_points, width, height);

	} // SpacePartPolygon2


	/**
	 * Erzeugt ein neues Polygon durch <i>space partitioning</i> aus den
	 * uebergebenen Punkten.
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
	public SpacePartPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height,
			Point2List points)
	{

		super(poly_min_points, poly_max_points, width, height, points);

	} // SpacePartPolygon2


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/*
	* [javadoc-Beschreibung wird aus RandomPolygon2.java kopiert]
	*/
	public String toString()
	{

		return "SpacePartPolygon2[n=" + _n + "]";

	} // toString


	//**************************************************************************
	// Private methods
	//**************************************************************************

	/*
	* [javadoc-Beschreibung wird aus RandomPolygon2.java kopiert]
	*/
	protected void _init()
	{

		int k;
		Point2 sf, sl, p;
		Segment2 sf_sl;
		Vector points_left, points_right;
		Point2List pl_left, pl_right;

		k = dice(0, _random_points.size() - 1);
		sf = (Point2) _random_points.elementAt(k);
		_random_points.removeElementAt(k);

		k = dice(0, _random_points.size() - 1);
		sl = (Point2) _random_points.elementAt(k);
		_random_points.removeElementAt(k);

		sf_sl = new Segment2(sf, sl);

		points_left = new Vector();
		points_right = new Vector();
		_divideSpace(_random_points, sf_sl, points_left, points_right);

		// recursive calls:
		pl_left = _partSpace(sf, sl, points_left);
		pl_right = _partSpace(sl, sf, points_right);

		pl_left.removeLastPoint();
		pl_right.removeLastPoint();
		this.concat(pl_left);
		this.concat(pl_right);

	} // _init


	/**
	 * Rekursives space partitioning.
	 * 
	 * Liefert eine Liste der space partioning-Punkte
	 * 
	 * @param sf
	 *            Punkt 1
	 * @param sl
	 *            Punkt 2
	 * @param points
	 *            Punktliste
	 * 
	 * @return Liste der sp Punkte
	 */
	protected Point2List _partSpace(
			Point2 sf,
			Point2 sl,
			Vector points)
	{

		int i, k, n;
		float random;
		Point2 s, p, r;
		Segment2 sf_sl, s_r;
		Vector points_left, points_right;
		Point2List pl_left, pl_right, pl;

		pl = new Point2List();
		n = points.size();
		s_r = null;

		// Terminate recursion, if points is empty:
		if (n == 0)
		{
			pl.addPoint(sf);
			pl.addPoint(sl);
			return pl;
		}

		// else: 

		// just to avoid 'Variable s may not have been initialized':
		s = (Point2) points.elementAt(0);

		// shuffle point from points:
		k = dice(0, n - 1);
		s = (Point2) points.remove(k);

		// shuffle point on segment between sf and sl:
		sf_sl = new Segment2(sf, sl);
		random = (float) (Math.random());
		random = random * Math.abs(sl.x - sf.x);
		random = random + Math.min(sl.x, sf.x);
		r = sf_sl.calculatePoint(random);

		// construct segment between s and r

		// es ist ziemlich wichtig, die Orientierung dieses   !!
		// Segmentes zu beachten (von links nach rechts).  !!
		// Ist es falsch herum, so liefert _divideSpace    !!
		// points_left und points_right genau vertauscht,  !!
		// wodurch die resultierende Punkteliste falsch    !!
		// konstruiert wird              !!

		switch (sf_sl.orientation(s))
		{
			case Point2.ORIENTATION_LEFT:
				s_r = new Segment2(r, s);
				break;

			case Point2.ORIENTATION_RIGHT:
				s_r = new Segment2(s, r);
				break;

			case Point2.ORIENTATION_COLLINEAR:
				// should not occur

			default:
				System.out.println("Something's wrong sf_sl: "
						+ sf_sl.toString() + s.toString() + " " + n);
		}

		// divide space:
		points_left = new Vector();
		points_right = new Vector();
		_divideSpace(points, s_r, points_left, points_right);

		// recursive calls:
		pl_left = _partSpace(sf, s, points_left);
		pl_right = _partSpace(s, sl, points_right);

		// construct resulting pointlist:
		pl_left.removeLastPoint();
		pl.concat(pl_left);
		pl.concat(pl_right);

		return pl;

	} // _partSpace

} // SpacePartPolygon2
