package anja.geom.random;


import java.util.Enumeration;
import java.util.Vector;

import anja.geom.Intersection;
import anja.geom.Point2;
import anja.geom.Point2List;
import anja.geom.PointsAccess;
import anja.geom.Polygon2;
import anja.geom.Segment2;


/**
 * <p>Oberklasse fuer zufallsgenerierte Polygone. Alle Klassen, die spezielle
 * Zufallspolygone nach bestimmten Algorithmen berechnen, sollten von dieser
 * Klasse abgeleitet werden.</p>
 * 
 * <p>Eine Instanz dieser Klasse ist ein einfaches Zufallspolygon, dessen
 * Eckpunkte wahllos verteilt sind, daher gibt es Kantenueberschneidungen.</p>
 * 
 * @version 0.5 26.12.02
 * @author Thomas Kamphans, Sascha Ternes
 */

public class RandomPolygon2
		extends Polygon2
{

	//**************************************************************************
	// Public constants
	//**************************************************************************

	public final static int	SLEEPTIME				= 500;

	public final static int	STEADY_GROWTH_POLYGON	= 0;
	public final static int	SPACE_PART_POLYGON		= 1;
	public final static int	TWO_OPT_MOVES_POLYGON	= 2;
	public final static int	TWO_PEASANTS_POLYGON	= 3;
	public final static int	CONVEX_BOTTOM_POLYGON	= 4;
	// public final static int    RADAR_SWEEP_POLYGON  = 5;
	// public final static int    RANDOM_POLYGON    = 6;
	public final static int	RADAR_SWEEP_POLYGON		= 33;	// funkt. noch nicht
	public final static int	RANDOM_POLYGON			= 5;
	/* RANDOM_POLYGON muss die letzte Konstante sein */

	//**************************************************************************
	// Public variables
	//**************************************************************************

	public static int		DefaultPolyMinPoints	= 20;
	public static int		DefaultPolyMaxPoints	= 100;
	public static int		DefaultPolyWidth		= 300;
	public static int		DefaultPolyHeight		= 300;

	//**************************************************************************
	// Private variables
	//**************************************************************************

	protected int			_poly_min_points;
	protected int			_poly_max_points;
	protected int			_width;
	protected int			_height;
	protected Vector		_random_points;				/* shuffled points */
	protected int			_n;							/* number of points */


	//**************************************************************************
	// Class methods
	//**************************************************************************

	/**
	 * Generate random number between <i>low</i> and <i>high</i>
	 * 
	 * @param low
	 *            Lower bound
	 * @param high
	 *            Upper bound
	 */
	public static int dice(
			int low,
			int high)
	{

		double r = Math.random();
		int range = high - low;
		return (Math.round((float) (r * range))) + low;

	} // dice


	/**
	 * Generate a random pointlist with <i>num</i> points
	 * 
	 * @param num
	 *            The number of points
	 * @param max_x
	 *            The maximum for the x value
	 * @param max_y
	 *            The maximum for the y value
	 */
	public static Point2List shufflePointList(
			int num,
			int max_x,
			int max_y)
	{

		Point2List pl;
		Point2 point;
		int i, x, y;

		pl = new Point2List();

		for (i = 0; i < num; i++)
		{
			x = dice(0, max_x);
			y = dice(0, max_y);
			point = new Point2((float) x, (float) y);
			pl.addPoint(point);
		}

		return pl;

	} // shufflePointList


	/**
	 * Test intersection between two segments. Return true only, if the segments
	 * intersect in one points that is not one of the segment's endpoints.
	 * 
	 * @param s1
	 *            The first segment
	 * @param s2
	 *            The second segment
	 * 
	 * @return true, if the intersection is one point, false else
	 */
	public static boolean openSegmentIntersect(
			Segment2 s1,
			Segment2 s2)
	{
		Intersection result;
		Point2 dummy;
		boolean intersects = false;

		result = new Intersection();

		dummy = s1.intersection(s2, result);
		if (result.result == Intersection.POINT2)
		{
			Point2 p = result.point2;
			// return false, if intersection is one of the
			// segment's endpoints:
			intersects = !(p.equals(s1.source()) || p.equals(s1.target())
					|| p.equals(s2.source()) || p.equals(s2.target()));
		}

		return intersects;

	} // openSegmentIntersect


	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt ein neues Zufallspolygon mit zufaelligen Punkten. Als
	 * Polygonparameter dienen Defaultwerte.
	 */
	public RandomPolygon2()
	{

		this(DefaultPolyMinPoints, DefaultPolyMaxPoints, DefaultPolyWidth,
				DefaultPolyHeight);

	} // RandomPolygon2


	/**
	 * Erzeugt ein neues Zufallspolygon mit zufaelligen Punkten, wobei die
	 * Polygonparameter uebergeben werden. Die Anzahl der Punkte wird innerhalb
	 * der uebergebenen Grenzen zufaellig gewaehlt, die Ausdehnung des Polygons
	 * erreicht maximal die uebergebenen Werte fuer Hoehe und Breite.
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
	public RandomPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height)
	{

		_poly_min_points = poly_min_points;
		_poly_max_points = poly_max_points;
		_width = width;
		_height = height;
		shuffle();

	} // RandomPolygon2


	/**
	 * Erzeugt ein neues Testpolygon aus den uebergebenen Punkten.
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
	public RandomPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height,
			Point2List points)
	{

		super();
		_poly_min_points = poly_min_points;
		_poly_max_points = poly_max_points;
		_width = width;
		_height = height;
		_initVector(points);
		_init();

	} // RandomPolygon2


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/**
	 * Zentriert das Polygon ueber dem Ursprung eines gedachten
	 * Koordinatensystems.
	 */
	public void centerOnOrigin()
	{

		float x_diff = (float) _width / 2;
		float y_diff = (float) _height / 2;
		Enumeration e = _points.values();
		while (e.hasMoreElements())
		{
			Point2 point = (Point2) e.nextElement();
			point.moveTo(point.x - x_diff, point.y - y_diff);
		} // while

	} // centerOnOrigin


	/**
	 * Liefert eine textuelle Repraesentation dieses Zufallspolygons.
	 * 
	 * @return das Zufallspolygon als String
	 */
	public String toString()
	{

		return "RandomPolygon2[n=" + _n + "]";

	} // toString


	/**
	 * Shuffle _new_ points and generate new polygon
	 */
	public void shuffle()
	{
		int i, x, y;
		Point2 point;
		boolean ok;

		point = new Point2(42, 42); // to avoid warning
		_random_points = new Vector();
		_n = dice(_poly_min_points, _poly_max_points);

		for (i = 0; i < _n; i++)
		{
			ok = false;
			while (!ok)
			{
				x = dice(0, _width);
				y = dice(0, _height);
				point = new Point2((float) x, (float) y);

				ok = true;
				for (int j = 0; j < i; j++)
				{
					if (point.equals((Point2) _random_points.elementAt(j)))
					{
						System.out.println("double point shuffled");
						ok = false;
					} // if
				} // for
			} // while

			_random_points.addElement(point);

		} // for

		_init();

	} // shuffle


	/**
	 * Test intersection between the segment defined by start and end and the
	 * polygon. Return false also, if the startpoint is the only intersecting
	 * point.
	 * 
	 * @param start
	 *            The source of the segment
	 * @param end
	 *            The target of the segment
	 * 
	 * @return true, if there is an intersection with the polygon, false else
	 */
	public boolean openIntersect(
			Point2 start,
			Point2 end)
	{
		Segment2 test;
		Intersection testresult;
		boolean intersects = true;

		test = new Segment2(start, end);
		testresult = new Intersection();

		this.intersection(test, testresult);
		// System.out.println(">>>>>>" + testresult.toString());

		if (testresult.result == Intersection.LIST)
		{
			if (testresult.list.length() == 1)
			{
				Object obj = testresult.list.firstValue();
				if (obj instanceof Point2)
				{
					intersects = !start.equals((Point2) testresult.list
							.firstValue());
				}
			}
		}
		else
		{
			if (testresult.result == Intersection.EMPTY)
			{
				intersects = false;
			}
		}
		return intersects;

	} // openIntersect


	//**************************************************************************
	// Private methods
	//**************************************************************************

	/**
	 * Erzeugt das Polygon aus der Punktliste.
	 */
	protected void _init()
	{

		int i;
		Point2 p;

		for (i = 0; i < _n; i++)
		{
			p = (Point2) _random_points.elementAt(i);
			addPoint(p);
		} // for
		setClosed();

	} // _init


	/**
	 * Copy input pointlist to vector _random_points
	 * 
	 * @param points
	 *            The list of points
	 */
	protected void _initVector(
			Point2List points)
	{
		PointsAccess pa;
		Point2 p;

		_random_points = new Vector();
		_n = points.length();
		pa = new PointsAccess(points);
		while (pa.hasNextPoint())
		{
			p = (Point2) pa.nextPoint().clone();
			_random_points.addElement(p);
		}
	} // _initVector


	/**
	 * Calculate partition of vector in along segment line. Used by
	 * SpacePartPolygon2 and TwoPeasantsPolygon2
	 * 
	 * @param points
	 *            The vector
	 * @param line
	 *            The segment line
	 * @param points_left
	 *            The left points
	 * @param points_right
	 *            The right points
	 */
	protected void _divideSpace(
			Vector points,
			Segment2 line,
			Vector points_left,
			Vector points_right)
	{
		int i;
		int n = points.size();
		Point2 p;

		for (i = 0; i < n; i++)
		{
			p = (Point2) points.elementAt(i);
			switch (line.orientation(p))
			{
				case Point2.ORIENTATION_LEFT:
					points_left.addElement(p);
					break;

				case Point2.ORIENTATION_RIGHT:
					points_right.addElement(p);
					break;

				case Point2.ORIENTATION_COLLINEAR:
					points_left.addElement(p);
					break;
				default:
					System.out.println("Something's wrong: " + line.toString()
							+ p.toString());
			}
		} // for

	} // _divideSpace

} // RandomPolygon2
