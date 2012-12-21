package anja.geom;


/*
import java_ersatz.java2d.BezierPath;
import java_ersatz.java2d.Graphics2D;
import java_ersatz.java2d.Rectangle2D;*/

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

//import java_ersatz.java2d.BezierPath;
import java.awt.geom.GeneralPath;

import anja.util.Comparitor;
import anja.util.GraphicsContext;


/**
 * Zweidimensionaler zeichenbarer Strahl.
 * 
 * @version 1.2 29.10.1997
 * @author Norbert Selle
 */

public class Ray2
		extends BasicLine2
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	/** Konstanten */
	public final static byte	RIGHT	= 1, LEFT = 2, UP = 3, DOWN = 4;


	// ************************************************************************
	// Variables
	// ************************************************************************

	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt den Strahl von (0, 0) nach (1, 0).
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
	 * <BR><B>Vorbedingungen:</B><BR>Die Eingabepunkte sind ungleich null
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
			float input_source_x,
			float input_source_y,
			float input_target_x,
			float input_target_y)
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
			float slope,
			byte dir)
	{
		float x2 = 0, y2 = 0;
		float delta = Math.max(Math.abs(input_source.x), Math
				.abs(input_source.y)) + 1;

		switch (dir)
		{
			case UP:
				x2 = input_source.x;
				y2 = input_source.y + delta;
				break;
			case DOWN:
				x2 = input_source.x;
				y2 = input_source.y - delta;
				break;
			case LEFT:
				if (Math.abs(slope) <= 1.0f)
				{
					x2 = input_source.x - delta;
					y2 = input_source.y - delta * slope;
				}
				else if (slope > 0.0f)
				{
					x2 = input_source.x - delta / slope;
					y2 = input_source.y - delta;
				}
				else
				{
					x2 = input_source.x + delta / slope;
					y2 = input_source.y + delta;
				}
				break;
			case RIGHT:
				if (Math.abs(slope) <= 1.0f)
				{
					x2 = input_source.x + delta;
					y2 = input_source.y + delta * slope;
				}
				else if (slope > 0.0f)
				{
					x2 = input_source.x + delta / slope;
					y2 = input_source.y + delta;
				}
				else
				{
					x2 = input_source.x - delta / slope;
					y2 = input_source.y - delta;
				}
				break;
			default:
				break;
		} // switch

		_source = input_source;
		_target = new Point2(x2, y2);

		if (_source.equals(_target))
		{
			System.out.println("Warning: Ray2 source equals target " + _source);
		} // if

	} // Ray2


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Clippt den Strahl am Rechteck und gibt das resultierende Segment zurueck,
	 * null wenn der Strahl nicht im Rechteck liegt. Das Segment hat die gleiche
	 * Richtung wie der Strahl.
	 * 
	 * @param rectangle
	 *            Das Rechteck
	 * 
	 * @return den am Rechteck geclippten Strahl oder null
	 */
	public Segment2 clip(
			Rectangle2D rectangle)
	{
		Line2 line1;
		Line2 line2;

		Rectangle2D.Float rect = (Rectangle2D.Float) rectangle;

		if (Math.abs(_target.y - _source.y) > Math.abs(_target.x - _source.x))
		{
			// steiler Strahl: Schneiden mit der Ober- und Unterkante
			line1 = new Line2(rect.x, rect.y, rect.x + rect.width, rect.y);
			line2 = new Line2(rect.x, rect.y + rect.height,
					rect.x + rect.width, rect.y + rect.height);
		}
		else
		{
			// flacher Strahl: Schneiden mit der linken und rechten
			// Seitenlinie
			line1 = new Line2(rect.x, rect.y, rect.x, rect.y + rect.height);
			line2 = new Line2(rect.x + rect.width, rect.y, rect.x + rect.width,
					rect.y + rect.height);
		} // if
		Intersection set = new Intersection();
		Point2 point1 = intersection(line1, set);
		Point2 point2 = intersection(line2, set);
		if ((point1 == null) && (point2 == null))
			return (null);
		if (point1 == null)
			point1 = _source;
		else if (point2 == null)
			point2 = _source;
		// Segment mit derselben Richtung wie this erzeugen und
		// clippen
		Segment2 clipped_at_lines;
		if (PointComparitor.compareX(source(), target()) == PointComparitor
				.compareX(point1, point2))
			clipped_at_lines = new Segment2(point1, point2);
		else
			clipped_at_lines = new Segment2(point2, point1);
		return (BasicLine2.clipper(clipped_at_lines, rect));
	} // clip


	// ************************************************************************

	/**
	 * Erzeugt eine Kopie des Strahls.
	 * 
	 * @return die Kopie
	 */
	public Object clone()
	{
		return (new Ray2(this));

	} // clone


	// ************************************************************************

	/**
	 * Gibt den Punkt des Strahls zurueck, der dem Eingabepunkt am naechsten
	 * liegt.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return Den naechsten Punkt
	 * 
	 * @see #plumb
	 */
	public Point2 closestPoint(
			Point2 input_point)
	{
		Point2 plumbPoint = plumb(input_point);

		if (plumbPoint != null)
			return plumbPoint;

		return (Point2) _source.clone();

	} // closestPoint


	// ************************************************************************

	/**
	 * Berechnet die Distanz zwischen dem Strahl und dem Eingabepunkt.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
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
	 * 
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gc
	 *            Die dazugehörigen Formatierungsregeln
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		Rectangle2D clip_rectangle = BasicLine2.worldClipRectangle(g);

		if (clip_rectangle == null)
		{
			System.out.println("Warning: Ray2.draw(): clip rectangle null");
		}
		else
		{
			Segment2 clipped = this.clip(clip_rectangle);

			if (clipped != null)
			{

				//TODO: Find the rendering bug in the draw() method
				/*
				 * 
				 * 
				 *   LATESTT POSSIBLE LOCATION OF THE RENDERING FAULT IS HERE
				 *   (????????) May have something to do with either clipping 
				 *  or rendering behaviour of GeneralPath objects.....
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 */

				GeneralPath bp = new GeneralPath(GeneralPath.WIND_NON_ZERO);

				bp.moveTo(clipped.source().x, clipped.source().y);
				bp.lineTo(clipped.target().x, clipped.target().y);

				//bp.moveTo(_source.x, _source.y);
				//bp.lineTo(_target.x, _target.y);

				g.setColor(gc.getForegroundColor());
				g.setStroke(gc.getStroke());
				g.draw(bp);

				// Anfangspunkt zeichnen

				if (clipped.source() == source())
					source().draw(g, gc);
			} // if
		} // if

	} // draw


	// ************************************************************************

	/**
	 * Untersucht die Lage des Eingabepunktes in Bezug auf den mit ihm auf einer
	 * Geraden liegenden Strahl.
	 * 
	 * @param input_point
	 *            der Eingabepunkt
	 * 
	 * @return Point2.LIES_BEFORE oder Point2.LIES_ON
	 * 
	 * @see Point2#LIES_BEFORE
	 * @see Point2#LIES_ON
	 */
	public int inspectCollinearPoint(
			Point2 input_point)
	{
		int output_position = Point2.LIES_ON;

		if (PointComparitor.compareX(_source, _target) == Comparitor.SMALLER)
		{
			// 1. Fall: _source liegt vor _target

			if (PointComparitor.compareX(input_point, _source) == Comparitor.SMALLER)
			{
				output_position = Point2.LIES_BEFORE;
			} // if
		}
		else
		{
			// 2. Fall: _source liegt hinter _target

			if (PointComparitor.compareX(input_point, _source) == Comparitor.BIGGER)
			{
				output_position = Point2.LIES_BEFORE;
			} // if
		} // if

		return (output_position);

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
		if ((input_box.contains(_source.x, _source.y))
				|| (input_box.contains(_target.x, _target.y)))
		{
			return (true); // Mindestens einer der den Strahl 
			// aufspannenden Punkte liegt im Rechteck
		} // if

		// Test auf Schnitt mit den Rechteckseiten

		Intersection set = new Intersection();
		Rectangle2 rect2 = new Rectangle2(input_box);

		intersection(rect2.top(), set);
		if (set.result != Intersection.EMPTY)
		{
			return (true);
		} // if

		intersection(rect2.bottom(), set);
		if (set.result != Intersection.EMPTY)
		{
			return (true);
		} // if

		intersection(rect2.left(), set);
		if (set.result != Intersection.EMPTY)
		{
			return (true);
		} // if

		intersection(rect2.right(), set);
		if (set.result != Intersection.EMPTY)
		{
			return (true);
		} // if

		return (false);

	} // intersects


	// ************************************************************************

	/**
	 * Faellt das Lot vom Eingabepunkt auf den Strahl und gibt den Schnittpunkt
	 * zurueck, falls er auf dem Strahl liegt.
	 * 
	 * @param input_point
	 *            der Eingabepunkt
	 * 
	 * @return den Schnittpunkt falls er existiert, sonst null
	 */
	public Point2 plumb(
			Point2 input_point)
	{
		return (intersection(orthogonal(input_point), new Intersection()));

	} // plumb


	// ************************************************************************

	/**
	 * Berechnet das Quadrat der Distanz zwischen dem Strahl und dem
	 * Eingabepunkt.
	 * 
	 * @param input_point
	 *            der Eingabepunkt
	 * 
	 * @return das Quadrat der Distanz
	 * 
	 * @see #distance
	 */
	public double squareDistance(
			Point2 input_point)
	{
		Point2 plumbPoint = plumb(input_point);

		if (plumbPoint != null)
		{
			return plumbPoint.squareDistance(input_point);
		}// if 

		return input_point.squareDistance(_source);

	} // squareDistance


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

	/**
	 * Berechnet die Schnittmenge mit dem Eingabestrahl und gibt sie im
	 * Intersection-Parameter zurueck, ist es ein Schnittpunkt wird er ausserdem
	 * als Ergebnis zurueckgegeben.
	 * 
	 * @param input_ray
	 *            der Eingabestrahl
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

	/**
	 * Berechnet die Schnittmenge des Strahls mit dem mit ihm auf einer Geraden
	 * liegenden Eingabestrahls und gibt sie ihm Intersection- Parameter
	 * zurueck.
	 * 
	 * @param input_ray
	 *            der Eingabestrahl
	 * @param inout_intersection
	 *            die Schnittmenge
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

