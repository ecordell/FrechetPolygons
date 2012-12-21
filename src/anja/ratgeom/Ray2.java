package anja.ratgeom;


import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import anja.util.BigRational;
import anja.util.GraphicsContext;


/**
 * Zweidimensionaler gerichteter zeichenbarer Strahl mit rationalen Koordinaten
 * beliebiger Genauigkeit.
 * 
 * @version 0.2 10.11.1997
 * @author Norbert Selle
 */

public class Ray2
		extends BasicLine2
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	public final static byte	RIGHT	= 1;
	public final static byte	LEFT	= 2;
	public final static byte	UP		= 3;
	public final static byte	DOWN	= 4;


	// ************************************************************************
	// Variables
	// ************************************************************************

	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt den Strahl von (0, 0) nach (0, 0).
	 */

	public Ray2()
	{
		this(new Point2(0, 0), new Point2(1, 0));

	} // Ray2


	// ********************************

	/**
	 * Erzeugt einen zum Eingabestrahl identischen Strahl.
	 * 
	 * @param input_ray
	 *            der Eingabestrahl
	 */

	public Ray2(
			Ray2 input_ray)
	{
		this(new Point2(input_ray._source), new Point2(input_ray._target));

	} // Ray2


	// ********************************

	/**
	 * Erzeugt einen Strahl vom ersten Eingabepunkt in Richtung des zweiten
	 * Eingabepunktes, der Strahl merkt sich <B>Referenzen</B> auf diese Punkte.
	 * 
	 * @param input_source
	 *            der Startpunkt
	 * @param input_target
	 *            der Zielpunkt
	 */

	public Ray2(
			Point2 input_source,
			Point2 input_target)
	{
		super(input_source, input_target);

		if (_source.equals(_target))
		{
			System.out.println("Warning: Ray2 source equals target " + _source);
		} // if

	} // Ray2


	// ********************************

	/**
	 * Erzeugt einen Strahl beginnend bei den ersten beiden Koordinaten in
	 * Richtung der zweiten beiden Koordinaten.
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

	public Ray2(
			double input_source_x,
			double input_source_y,
			double input_target_x,
			double input_target_y)
	{
		this(new Point2(input_source_x, input_source_y), new Point2(
				input_target_x, input_target_y));

	} // Ray2


	// ********************************

	/**
	 * Erzeugt einen Strahl durch den Eingabepunkt s mit der Richtung dir, wobei
	 * slope die Steigung angibt im Fall dir=LEFT/RIGHT.
	 * 
	 * @param input_source
	 *            der Startpunkt
	 * @param slope
	 *            die Steigung
	 * @param dir
	 *            die Richtung
	 */

	public Ray2(
			Point2 input_source,
			double slope,
			byte dir)
	{
		BigRational x2 = BigRational.valueOf(0);
		BigRational y2 = BigRational.valueOf(0);

		switch (dir)
		{
			case UP:
				x2 = input_source.x;
				y2 = input_source.y.add(BigRational.valueOf(1));
				break;
			case DOWN:
				x2 = input_source.x;
				y2 = input_source.y.subtract(BigRational.valueOf(1));
				break;
			case LEFT:
				if (Math.abs(slope) <= 1)
				{
					x2 = input_source.x.subtract(BigRational.valueOf(1));
					y2 = input_source.y.subtract(BigRational.valueOf(slope));
				}
				else if (slope > 0)
				{
					x2 = input_source.x
							.subtract(BigRational.valueOf(1 / slope));
					y2 = input_source.y.subtract(BigRational.valueOf(1));
				}
				else
				{
					x2 = input_source.x.add(BigRational.valueOf(1 / slope));
					y2 = input_source.y.add(BigRational.valueOf(1));
				}
				break;
			case RIGHT:
				if (Math.abs(slope) <= 1)
				{
					x2 = input_source.x.add(BigRational.valueOf(1));
					y2 = input_source.y.add(BigRational.valueOf(slope));
				}
				else if (slope > 0)
				{
					x2 = input_source.x.add(BigRational.valueOf(1 / slope));
					y2 = input_source.y.add(BigRational.valueOf(1));
				}
				else
				{
					x2 = input_source.x
							.subtract(BigRational.valueOf(1 / slope));
					y2 = input_source.y.subtract(BigRational.valueOf(1));
				}
				break;
			default:
				break;
		} // switch

		_source = input_source;
		_target = new Point2(x2, y2);
	}


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Erzeugt eine Kopie des Strahls und gibt sie zurueck.
	 */

	public Object clone()
	{
		return (new Ray2(this));

	} // clone


	// ************************************************************************

	/**
	 * Gibt den Punkt des Strahls zurueck, der dem Eingabepunkt am naechsten
	 * liegt.
	 */

	public Point2 closestPoint(
			Point2 input_point)
	{
		Point2 plumb_point = plumb(input_point);

		if (plumb_point == null)
			return (new Point2(_source));
		else
			return (plumb_point);

	} // closestPoint


	// ************************************************************************

	/**
	 * Berechnet die Distanz zwischen dem Strahl und dem Eingabepunkt.
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
	 * Zeichnet den Strahl.
	 */

	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		toGeom().draw(g, gc);

	} // draw


	// ************************************************************************

	/**
	 * Untersucht die Lage des Eingabepunktes in Bezug auf den mit ihm auf einer
	 * Geraden liegenden Strahl.
	 * 
	 * @return Point2.LIES_BEFORE oder Point2.LIES_ON
	 * 
	 * @see Point2#LIES_BEFORE
	 * @see Point2#LIES_ON
	 */

	public int inspectCollinearPoint(
			Point2 input_point)
	{
		int position = input_point.inspectCollinearPoint(_source, _target);

		if (position == Point2.LIES_BEHIND)
			return (Point2.LIES_ON);
		else
			return (position);

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
	 * @return Der Schnittpunkt wenn er existiert, sonst null
	 */

	public Point2 intersection(
			BasicLine2 input_basic,
			Intersection inout_set)
	{
		if (input_basic instanceof Line2)
			return _intersectionLine2((Line2) input_basic, inout_set);

		if (input_basic instanceof Ray2)
			return _intersectionRay2((Ray2) input_basic, inout_set);

		if (input_basic instanceof Segment2)
			return ((Segment2) input_basic).intersection(this, inout_set);

		inout_set.set();
		return (null);

	} // intersection


	// ************************************************************************

	/**
	 * Testet ob der Strahl das Rechteck schneidet.
	 * 
	 * @param input_box
	 *            das zu testende Rechteck
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
	 * Faellt das Lot vom Eingabepunkt auf den Strahl und gibt den Schnittpunkt
	 * zurueck, falls er auf dem Strahl liegt.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return den Schnittpunkt falls er existiert, sonst null
	 */

	public Point2 plumb(
			Point2 input_point)
	{
		return (_intersectionLine2(orthogonal(input_point), new Intersection()));

	} // plumb


	// ************************************************************************

	/**
	 * Berechnet das Quadrat der Distanz zwischen dem Strahl und dem
	 * Eingabepunkt.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 *            
	 * @return das Quadrat der Distanz
	 * 
	 * @see #distance
	 */

	public double squareDistance(
			Point2 input_point)
	{
		Point2 plumb_point = plumb(input_point);

		if (plumb_point == null)
			return (input_point.squareDistance(_source));
		else
			return (input_point.squareDistance(plumb_point));

	} // squareDistance


	// ************************************************************************

	/**
	 * Erzeugt einen anja.geom.Ray2 aus dem Strahl und gibt ihn zurueck.
	 * 
	 * @return Ein anja.geom.Ray2 Objekt
	 */

	public anja.geom.Ray2 toGeom()
	{
		return (new anja.geom.Ray2(_source.toGeom(), _target.toGeom()));

	} // toGeom


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge mit der Eingabegeraden und gibt sie im
	 * Intersection-Parameter zurueck, ist es ein Schnittpunkt wird er ausserdem
	 * als Ergebnis zurueckgegeben.
	 * 
	 * @param input_line
	 *            die Eingabegerade
	 * @param inout_intersection
	 *            die Schnittmenge
	 * 
	 * @return den Schnittpunkt wenn er existiert, sonst null
	 */

	private Point2 _intersectionLine2(
			Line2 input_line,
			Intersection inout_intersection)
	{
		InspectResult prepare = inspectBasicLine(input_line);

		if (prepare.parallel)
		{
			// parallel oder mindestens eins von beiden ist nur ein Punkt

			if (_source.equals(_target))
			{
				// Strahl ist nur ein Punkt

				if (input_line.liesOn(_source))
				{
					inout_intersection.set(new Point2(_source));
				}
				else
				{
					inout_intersection.set();
				} // if
			}
			else
			{
				if (orientation(input_line._source) == Point2.ORIENTATION_COLLINEAR)
				{
					inout_intersection.set(new Ray2(this));
				}
				else
				{
					inout_intersection.set();
				} // if
			} // if
		}
		else
		{
			// nicht parallel

			if (prepare.orderOnThis != Point2.LIES_BEFORE)
			{
				inout_intersection.set(prepare.intersectionPoint);
			}
			else
			{
				inout_intersection.set();
			} // if
		} // if

		return (inout_intersection.point2);

	} // _intersectionLine2


	// ************************************************************************

	/*
	* Berechnet die Schnittmenge mit dem Eingabestrahl und gibt sie im 
	* Intersection-Parameter zurueck, ist es ein Schnittpunkt wird er
	* ausserdem als Ergebnis zurueckgegeben.
	* 
	* @param  input_ray		der Eingabestrahl
	* @param  inout_intersection	die Schnittmenge
	*
	* @return den Schnittpunkt wenn er existiert, sonst null
	*/

	private Point2 _intersectionRay2(
			Ray2 input_ray,
			Intersection inout_intersection)
	{
		InspectResult prepare = inspectBasicLine(input_ray);

		if (prepare.parallel)
		{
			if (_source.equals(_target))
			{
				// Strahl ist nur ein Punkt

				if (input_ray.liesOn(_source))
				{
					inout_intersection.set(new Point2(_source));
				}
				else
				{
					inout_intersection.set();
				} // if
			}
			else
			{
				if (orientation(input_ray._source) == Point2.ORIENTATION_COLLINEAR)
				{
					_intersectionCollinearRay(input_ray, inout_intersection);
				}
				else
				{
					inout_intersection.set();
				} // if
			} // if
		}
		else
		{
			if ((prepare.orderOnThis != Point2.LIES_BEFORE)
					&& (prepare.orderOnParam != Point2.LIES_BEFORE))
			{
				inout_intersection.set(prepare.intersectionPoint);
			}
			else
			{
				inout_intersection.set();
			} // if
		} // if

		return (inout_intersection.point2);

	} // _intersectionRay2


	// ************************************************************************

	/*
	* Berechnet die Schnittmenge des Strahls mit dem mit ihm auf einer
	* Geraden liegenden Eingabestrahls und gibt sie ihm Intersection-
	* Parameter zurueck.
	*/

	private void _intersectionCollinearRay(
			Ray2 input_ray,
			Intersection inout_intersection)
	{
		if (PointComparitor.compareX(_source, _target) == PointComparitor
				.compareX(input_ray.source(), input_ray.target()))
		{
			// 1. Fall: die Strahlen sind gleichgerichtet

			if (input_ray.inspectCollinearPoint(_source) == Point2.LIES_ON)
			{
				inout_intersection.set(new Ray2(this));
			}
			else
			{
				inout_intersection.set(new Ray2(input_ray));
			} // if
		}
		else
		{
			// 2. Fall: die Strahlen sind entgegengerichtet

			if (input_ray.inspectCollinearPoint(_source) == Point2.LIES_ON)
			{
				if (_source.equals(input_ray.source()))
				{
					inout_intersection.set(new Point2(_source));
				}
				else
				{
					inout_intersection.set(new Segment2(_source, input_ray
							.source()));
				} // if
			}
			else
			{
				inout_intersection.set();
			} // if
		} // if

	} // _intersectionCollinearRay

	// ************************************************************************

} // class Ray2

