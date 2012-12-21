package anja.ratgeom;


import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import anja.util.GraphicsContext;


/**
 * Zweidimensionale gerichtete zeichenbare Gerade mit rationalen Koordinaten
 * beliebiger Genauigkeit.
 * 
 * @version 0.2 10.11.1997
 * @author Norbert Selle
 */

public class Line2
		extends BasicLine2
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	// ************************************************************************
	// Variables
	// ************************************************************************

	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt die Gerade von (0, 0) nach (1, 0).
	 */

	public Line2()
	{
		this(new Point2(0, 0), new Point2(1, 0));

	} // Line2


	// ********************************

	/**
	 * Erzeugt eine zur Eingabegeraden identische Gerade.
	 * 
	 * @param input_line
	 *            Eingabegerade
	 */

	public Line2(
			Line2 input_line)
	{
		this(new Point2(input_line._source), new Point2(input_line._target));

	} // Line2


	// ********************************

	/**
	 * Erzeugt eine Gerade durch den ersten Eingabepunkt in Richtung des zweiten
	 * Eingabepunktes, die Gerade merkt sich <B>Referenzen</B> auf diese Punkte.
	 * <BR><B>Vorbedingungen:</B> <BR>die Eingabepunkte sind ungleich null
	 * 
	 * @param input_source
	 *            der Startpunkt
	 * @param input_target
	 *            der Zielpunkt
	 */

	public Line2(
			Point2 input_source,
			Point2 input_target)
	{
		super(input_source, input_target);

		if (_source.equals(_target))
		{
			System.out
					.println("Warning: Line2 source equals target " + _source);
		} // if

	} // Line2


	// ********************************

	/**
	 * Erzeugt ein Gerade durch die Punkte mit den Eingabekoordinaten.
	 * 
	 * @param input_source_x
	 *            die x-Koordinate des Startpunkts
	 * @param input_source_y
	 *            die y-Koordinate des Startpunkts
	 * @param input_target_x
	 *            die x-Koordinate des Zielpunkts
	 * @param input_target_y
	 *            die y-Koordinate des Zielpunkts
	 */

	public Line2(
			double input_source_x,
			double input_source_y,
			double input_target_x,
			double input_target_y)
	{
		this(new Point2(input_source_x, input_source_y), new Point2(
				input_target_x, input_target_y));

	} // Line2


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Erzeugt eine Kopie und gibt sie zurueck.
	 */

	public Object clone()
	{
		return (new Line2(this));

	} // clone


	// ************************************************************************

	/**
	 * Gibt den Punkt der Geraden zurueck, der dem Eingabepunkt am naechsten
	 * liegt. In diesem Fall einer Geraden ist das natuerlich dasselbe wie
	 * plumb(Point2).
	 * 
	 * @see #plumb
	 */

	public Point2 closestPoint(
			Point2 input_point)
	{
		return plumb(input_point);

	} // closestPoint


	// ************************************************************************

	/**
	 * Berechnet die Distanz zwischen der Geraden und dem Eingabepunkt.
	 * 
	 * @return die Distanz
	 * 
	 * @see #squareDistance
	 */

	public double distance(
			Point2 input_point)
	{
		return Math.sqrt(squareDistance(input_point));

	} // distance


	// ************************************************************************

	/**
	 * Zeichnet die Gerade.
	 */

	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		toGeom().draw(g, gc);

	} // draw;


	// ************************************************************************

	/**
	 * Gibt Point2.LIES_ON zurueck.
	 * 
	 * @see anja.ratgeom.Point2#LIES_ON
	 */

	public int inspectCollinearPoint(
			Point2 input_point)
	{
		return (Point2.LIES_ON);

	} // inspectCollinearPoint


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge mit dem Eingabelinie und gibt sie im
	 * Intersection-Parameter zurueck, ist es ein Schnittpunkt wird er ausserdem
	 * als Ergebnis zurueckgegeben.
	 * 
	 * @param input_basic
	 *            die Eingabelinie
	 * @param inout_set
	 *            die Schnittmenge
	 * 
	 * @return den Schnittpunkt wenn er existiert, sonst null
	 */

	public Point2 intersection(
			BasicLine2 input_basic,
			Intersection inout_set)
	{
		if (input_basic instanceof Line2)
			return _intersectionLine2((Line2) input_basic, inout_set);

		if (input_basic instanceof Ray2)
			return ((Ray2) input_basic).intersection(this, inout_set);

		if (input_basic instanceof Segment2)
			return ((Segment2) input_basic).intersection(this, inout_set);

		inout_set.set();
		return (null);

	} // intersection


	// ************************************************************************

	/**
	 * Testet ob die Gerade das Rechteck schneidet.
	 * 
	 * @param input_box
	 *            Das zu testende Rechteck
	 * 
	 * @return true wenn ja, sonst false
	 */

	public boolean intersects(
			Rectangle2D input_box)
	{
		return (toGeom().intersects(input_box));

	} // intersects


	// ************************************************************************

	/**
	 * Faellt das Lot vom Eingabepunkt auf die Gerade und gibt den Schnittpunkt
	 * zurueck.
	 * 
	 * @return den Lotpunkt
	 */

	public Point2 plumb(
			Point2 input_point)
	{
		return intersection(orthogonal(input_point), new Intersection());

	} // plumb


	// ************************************************************************

	/**
	 * Berechnet das Quadrat der Distanz zwischen der Gerade und dem
	 * Eingabepunkt und gibt ihn zurueck.
	 * 
	 * @see #distance
	 */

	public double squareDistance(
			Point2 input_point)
	{
		return input_point.squareDistance(plumb(input_point));

	} // squareDistance


	// ************************************************************************

	/**
	 * Erzeugt eine anja.geom.Line2 aus der Gerade und gibt sie zurueck.
	 */

	public anja.geom.Line2 toGeom()
	{
		return (new anja.geom.Line2(_source.toGeom(), _target.toGeom()));

	} // toGeom


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/*
	* Berechnet die Schnittmenge mit der Eingabegeraden und gibt sie im 
	* Intersection-Parameter zurueck, ist es ein Schnittpunkt wird er
	* ausserdem als Ergebnis zurueckgegeben.
	* 
	* @param  input_line  die Eingabegerade
	* @param  inout_set   die Schnittmenge
	*
	* @return den Schnittpunkt wen er existiert, sonst null
	*/

	private Point2 _intersectionLine2(
			Line2 input_line,
			Intersection inout_set)
	{
		InspectResult prepare = inspectBasicLine(input_line);

		if (prepare.parallel)
		{
			if (_source.equals(_target))
			{
				// Gerade ist nur ein Punkt

				if (input_line.liesOn(_source))
				{
					inout_set.set(new Point2(_source));
				}
				else
				{
					inout_set.set();
				} // if
			}
			else
			{
				if (liesOn(input_line._source))
				{
					inout_set.set(new Line2(this));
				}
				else
				{
					inout_set.set();
				} // if
			} // if
		}
		else
		{
			inout_set.set(prepare.intersectionPoint);
		} // if

		return (inout_set.point2);

	} // _intersectionLine2

	// ************************************************************************

} // class Line2

