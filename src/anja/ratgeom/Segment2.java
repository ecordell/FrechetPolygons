package anja.ratgeom;


import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import anja.util.GraphicsContext;


/**
 * Zweidimensionales gerichtetes zeichenbares Segment mit rationalen Koordinaten
 * beliebiger Genauigkeit.
 * 
 * @version 1.1 19.12.1997
 * @author Norbert Selle
 */

public class Segment2
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
	 * Erzeugt das Segment von (0, 0) nach (0, 0).
	 */

	public Segment2()
	{
		super();

	} // Segment2


	// ********************************

	/**
	 * Erzeugt ein zum Eingabesegment identisches Segement.
	 * 
	 * @param input_segment
	 *            das Eingabesegment
	 */

	public Segment2(
			Segment2 input_segment)
	{
		super(input_segment);

	} // Segment2


	// ********************************

	/**
	 * Erzeugt ein vom ersten Eingabepunkt - dem Startpunkt - zum zweiten
	 * Eingabepunkt - dem Endpunkt - gerichtetes Segment, das sich <B>
	 * Referenzen </B> auf die Punkte merkt.
	 * 
	 * @param input_source
	 *            der Startpunkt
	 * @param input_target
	 *            der Zielpunkt
	 */

	public Segment2(
			Point2 input_source,
			Point2 input_target)
	{
		super(input_source, input_target);

	} // Segment2


	// ********************************

	/**
	 * Erzeugt ein Segment von ersten beiden Koordinaten zu den zweiten beiden
	 * Koordinaten.
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

	public Segment2(
			double input_source_x,
			double input_source_y,
			double input_target_x,
			double input_target_y)
	{
		super(input_source_x, input_source_y, input_target_x, input_target_y);

	} // Segment2


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Erzeugt eine Kopie und gibt sie zurueck.
	 */

	public Object clone()
	{
		Segment2 output_segment = new Segment2(this);

		return (output_segment);

	} // clone


	// ************************************************************************

	/**
	 * Zeichnet das Segment.
	 */

	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		toGeom().draw(g, gc);

	} // draw


	// ********************************

	/**
	 * Zeichnet ein Segment.
	 */

	public static void draw(
			int device_source_x,
			int device_source_y,
			int device_target_x,
			int device_target_y,
			Graphics2D g,
			GraphicsContext gc)
	{
		g.setColor(gc.getForegroundColor());
		g.drawLine(device_source_x, device_source_y, device_target_x,
				device_target_y);

	} // draw


	// ************************************************************************

	/**
	 * Gibt true zurueck bei Gleichheit, sonst false.
	 */

	public boolean equals(
			Object input_object)
	{
		if (input_object == null)
			return (false);

		if (!(input_object instanceof Segment2))
			return (false);

		Segment2 in_segment = (Segment2) input_object;

		return ((_source.equals(in_segment._source)) && (_target
				.equals(in_segment._target)));

	} // equals


	// ************************************************************************

	/**
	 * Gibt das umschliessende Rechteck zurueck.
	 */

	public Rectangle2D getBoundingRect()
	{
		return (new Rectangle2D.Float(_source.x.floatValue(), // x
				_source.y.floatValue(), // y
				deltaX().floatValue(), // width
				deltaY().floatValue()) // height
		);

	} // getBoundingRect


	// ************************************************************************

	/**
	 * Untersucht die Lage des Eingabepunktes in Bezug auf das mit ihm auf einer
	 * Geraden liegende Segment.
	 * 
	 * @return Point2.LIES_BEFORE, Point2.LIES_ON oder Point2.LIES_BEHIND
	 * 
	 * @see Point2#LIES_BEFORE
	 * @see Point2#LIES_ON
	 * @see Point2#LIES_BEHIND
	 */

	public int inspectCollinearPoint(
			Point2 input_point)
	{
		return (input_point.inspectCollinearPoint(_source, _target));

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
			return _intersectionSegment2((Segment2) input_basic, inout_set);

		inout_set.set();
		return (null);

	} // intersection


	// ************************************************************************

	/**
	 * Testet ob das Segment das Rechteck schneidet.
	 * 
	 * @param input_box
	 *            das zu testende Rechteck
	 * 
	 * @return true wenn ja, sonst false
	 */

	public boolean intersects(
			Rectangle2D input_box)
	{
		anja.geom.Segment2 gs = toGeom();

		if ((input_box.contains(gs.source().x, gs.source().y))
				|| (input_box.contains(gs.target().x, gs.target().y)))
		{
			return (true); // Mindestens ein Endpunkt liegt im Rechteck
		} // if

		if (!gs.getBoundingRect().intersects(input_box))
		{
			return (false); // Das umschliessende Rechteck
			// schneidet input_box nicht
		} // if

		// Test auf Schnitt mit den Rechteckseiten

		anja.geom.Intersection set = new anja.geom.Intersection();
		anja.geom.Rectangle2 rect2 = new anja.geom.Rectangle2(input_box);

		gs.intersection(rect2.top(), set);
		if (set.result != Intersection.EMPTY)
		{
			return (true);
		} // if

		gs.intersection(rect2.bottom(), set);
		if (set.result != Intersection.EMPTY)
		{
			return (true);
		} // if

		gs.intersection(rect2.left(), set);
		if (set.result != Intersection.EMPTY)
		{
			return (true);
		} // if

		gs.intersection(rect2.right(), set);
		if (set.result != Intersection.EMPTY)
		{
			return (true);
		} // if

		return (false);

	} // intersects


	// ************************************************************************

	/**
	 * Berechnet die Laenge.
	 * 
	 * @return Die Länge der Strecke
	 */

	public double len()
	{
		return (_target.distance(_source));

	} // len


	// ************************************************************************

	/**
	 * Faellt das Lot vom Eingabepunkt auf das Segment und gibt den Schnittpunkt
	 * zurueck, falls er auf dem Segment liegt. Ist das Segment nur ein Punkt,
	 * so wird dieser zurueckgegeben.
	 * 
	 * @return den Schnittpunkt falls er existiert, sonst null
	 */

	public Point2 plumb(
			Point2 input_point)
	{
		if (_source.equals(_target))
			return (new Point2(_source));
		else
			return (intersection(orthogonal(input_point), new Intersection()));

	} // plumb


	// ************************************************************************

	/**
	 * Gibt den Punkt des Segments zurueck, der dem Eingabepunkt am naechsten
	 * liegt.
	 * 
	 * @see #plumb
	 * 
	 * @return den naechsten Punkt
	 */

	public Point2 closestPoint(
			Point2 input_point)
	{
		Point2 plumbPoint = plumb(input_point);

		if (plumbPoint != null)
			return plumbPoint;

		if ((input_point.bigSquareDistance(_source)).compareTo(input_point
				.bigSquareDistance(_target)) <= 0)
			return new Point2(_source);
		else
			return new Point2(_target);

	} // closestPoint


	// ************************************************************************

	/**
	 * Berechnet die Distanz zwischen dem Segment und dem Eingabepunkt und gibt
	 * sie zurueck.
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
	 * Berechnet das Quadrat der Distanz zwischen dem Segment und dem
	 * Eingabepunkt und gibt es zurueck.
	 * 
	 * @see #distance
	 */

	public double squareDistance(
			Point2 input_point)
	{
		Point2 plumbPoint = plumb(input_point);

		if (plumbPoint != null)
			return plumbPoint.squareDistance(input_point);
		else
			return Math.min(input_point.squareDistance(_source),
					input_point.squareDistance(_target));

	} // squareDistance


	// ************************************************************************

	/**
	 * Erzeugt ein anja.geom.Segment2 aus dem Segment und gibt es zurueck.
	 */

	public anja.geom.Segment2 toGeom()
	{
		return (new anja.geom.Segment2(_source.toGeom(), _target.toGeom()));

	} // toGeom


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/*
	* Berechnet die Schnittmenge des Segments mit dem mit ihm auf einer
	* Geraden liegenden Eingabestrahls und gibt sie im Intersection-
	* Parameter zurueck.
	*/

	private void _collinearRayIntersection(
			Ray2 input_ray,
			Intersection inout_intersection)
	{
		boolean is_source_on_ray = (input_ray.inspectCollinearPoint(_source) == Point2.LIES_ON);
		boolean is_target_on_ray = (input_ray.inspectCollinearPoint(_target) == Point2.LIES_ON);

		if (is_source_on_ray || is_target_on_ray)
		{
			Point2 new_source;
			Point2 new_target;

			if (is_source_on_ray && is_target_on_ray)
			{
				new_source = new Point2(_source);
				new_target = new Point2(_target);
			}
			else if (is_source_on_ray)
			{
				new_source = new Point2(_source);
				new_target = new Point2(input_ray.source());
			}
			else
			{
				new_source = new Point2(input_ray.source());
				new_target = new Point2(_target);
			} // if

			if (new_source.equals(new_target))
			{
				inout_intersection.set(new Point2(new_source));
			}
			else
			{
				inout_intersection.set(new Segment2(new_source, new_target));
			} // if
		}
		else
		{
			inout_intersection.set();
		} // if

	} // _collinearRayIntersection


	// ************************************************************************

	/*
	* Berechnet die Schnittmenge des Segments mit dem mit ihm auf einer
	* Geraden liegenden Eingabesegments und gibt sie im Intersection-
	* Parameter zurueck.
	*/

	private void _collinearSegmentIntersection(
			Segment2 input_segment,
			Intersection inout_intersection)
	{
		Point2 min1;
		Point2 max1;
		Point2 min2;
		Point2 max2;
		Point2 first;
		Point2 second;

		// kleineren und groesseren Punkt von this bestimmen

		if (source().isSmaller(target()))
		{
			min1 = source();
			max1 = target();
		}
		else
		{
			min1 = target();
			max1 = source();
		} // if

		// kleineren und groesseren Punkt von input_segment bestimmen

		if (input_segment.source().isSmaller(input_segment.target()))
		{
			min2 = input_segment.source();
			max2 = input_segment.target();
		}
		else
		{
			min2 = input_segment.target();
			max2 = input_segment.source();
		} // if

		if (max1.isSmaller(min2) || max2.isSmaller(min1))
		{
			// leere Schnittmenge
			inout_intersection.set();
		}
		else
		{
			// Schnittpunkt oder Schnittsegment

			if (min1.isSmaller(min2))
				first = min2;
			else
				first = min1;

			if (max1.isSmaller(max2))
				second = max1;
			else
				second = max2;

			if (first.equals(second))
				inout_intersection.set(new Point2(first));
			else
				inout_intersection.set(new Segment2(first, second));

		} // if

	} // _collinearSegmentIntersection


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge mit der Eingabegeraden und gibt sie im
	 * Intersection-Parameter zurueck, ist es ein Schnittpunkt wird er ausserdem
	 * als Ergebnis zurueckgegeben.
	 * 
	 * @param input_line
	 *            die Gerade zur Schnittpunktberechnung
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
			if (_source.equals(_target))
			{
				// Segment ist nur ein Punkt

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
				if (isCollinear(input_line._source))
				{
					inout_intersection.set(new Segment2(this));
				}
				else
				{
					inout_intersection.set();
				} // if
			} // if
		}
		else
		{
			if (prepare.orderOnThis == Point2.LIES_ON)
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

	/**
	 * Berechnet die Schnittmenge mit dem Eingabestrahl und gibt sie im
	 * Intersection-Parameter zurueck, ist es ein Schnittpunkt wird er ausserdem
	 * als Ergebnis zurueckgegeben.
	 * 
	 * @param input_ray
	 *            der Strahl zur Schnittpunktberechnung
	 * @param inout_intersection
	 *            die Schnittmenge
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
				// Segment ist nur ein Punkt

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
				if (isCollinear(input_ray._source))
				{
					_collinearRayIntersection(input_ray, inout_intersection);
				}
				else
				{
					inout_intersection.set();
				} // if
			}
		}
		else
		{
			if ((prepare.orderOnThis == Point2.LIES_ON)
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

	/**
	 * Berechnet die Schnittmenge mit dem Eingabesegment und gibt sie im
	 * Intersection-Parameter zurueck, ist es ein Schnittpunkt wird er ausserdem
	 * als Ergebnis zurueckgegeben.
	 * 
	 * @param input_segment
	 *            das Segment zur Schnittpunktberechnung
	 * @param inout_intersection
	 *            die Schnittmenge
	 * 
	 * @return den Schnittpunkt wenn er existiert, sonst null
	 */

	private Point2 _intersectionSegment2(
			Segment2 input_segment,
			Intersection inout_intersection)
	{
		InspectResult prepare = inspectBasicLine(input_segment);

		if (prepare.parallel)
		{
			if (_source.equals(_target))
			{
				// Segment ist nur ein Punkt

				if (input_segment.liesOn(_source))
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
				if (isCollinear(input_segment._source))
				{
					_collinearSegmentIntersection(input_segment,
							inout_intersection);
				}
				else
				{
					inout_intersection.set();
				} // if
			} // if
		}
		else
		{
			if ((prepare.orderOnThis == Point2.LIES_ON)
					&& (prepare.orderOnParam == Point2.LIES_ON))
			{
				inout_intersection.set(prepare.intersectionPoint);
			}
			else
			{
				inout_intersection.set();
			} // if
		} // if

		return (inout_intersection.point2);

	} // _intersectionSegment2

	// ************************************************************************

} // class Segment2

