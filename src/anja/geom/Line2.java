package anja.geom;


/*
import java_ersatz.java2d.BezierPath;
import java_ersatz.java2d.Graphics2D;
import java_ersatz.java2d.Rectangle2D; */

import java.awt.Graphics2D;
import java.awt.Stroke;

import java.awt.geom.Rectangle2D;

//import java_ersatz.java2d.BezierPath;
import java.awt.geom.GeneralPath; //import org.omg.PortableServer._ServantActivatorStub;

import anja.util.GraphicsContext;


/**
 * Zweidimensionale gerichtete zeichenbare Gerade.
 * 
 * @version 1.2 29.10.1997
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
	 *            Die Eingabegerade
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
	 * <BR><B>Vorbedingungen:</B><BR>die Eingabepunkte sind ungleich null
	 * 
	 * @param input_source
	 *            Der Startpunkt
	 * @param input_target
	 *            Der Zielpunkt
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
			float input_source_x,
			float input_source_y,
			float input_target_x,
			float input_target_y)
	{
		this(new Point2(input_source_x, input_source_y), new Point2(
				input_target_x, input_target_y));

	} // Line2


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * This essentially collapses the line into the point (0,0)
	 * 
	 */
	public void reset()
	{
		_source.setLocation(0, 0);
		_target.setLocation(0, 0);
	}


	/**
	 * This method is not implemented and just returns false
	 * 
	 * @return false
	 */
	public boolean isDegenerate()
	{
		return false; // stub
	}


	/**
	 * Is the object equal to the origin
	 * 
	 * @return true, if =(0,0), false else
	 */
	public boolean isNull()
	{
		double x, y;
		x = Math.abs(_source.getX());
		y = Math.abs(_source.getY());

		boolean s_is_null = ((x < _EPSILON) & (y < _EPSILON));

		x = Math.abs(_target.getX());
		y = Math.abs(_target.getY());

		boolean t_is_null = ((x < _EPSILON) & (y < _EPSILON));

		return (s_is_null & t_is_null);
	}


	/**
	 * Clippt die Gerade am Rechteck und gibt das resultierende Segment zurueck,
	 * null wenn die Gerade nicht im Rechteck liegt. Das Segment hat die gleiche
	 * Richtung wie die Gerade.
	 * 
	 * @param rectangle
	 *            Vorgegebenes Rechteck
	 * 
	 * @return die am Rechteck geclippte Gerade oder null
	 */
	public Segment2 clip(
			Rectangle2D rectangle)
	{
		Line2 line1;
		Line2 line2;

		/* 03.06.2004 Similar patch as in other methods that 
		 * expect a Rectangle2D argument, but also access its
		 * fields (which Rectangle2D doesn't have, of course) -
		 * hence the need to cast to the Rectangle2D.Float class.
		 * 
		 */

		Rectangle2D.Float rect = (Rectangle2D.Float) rectangle;

		if (Math.abs(_target.y - _source.y) > Math.abs(_target.x - _source.x))
		{
			// steile Gerade: Schneiden mit der Ober- und Unterkante
			line1 = new Line2(rect.x, rect.y, rect.x + rect.width, rect.y);
			line2 = new Line2(rect.x, rect.y + rect.height,
					rect.x + rect.width, rect.y + rect.height);
		}
		else
		{
			// flache Gerade: Schneiden mit der linken und rechten
			// Seitenlinie
			line1 = new Line2(rect.x, rect.y, rect.x, rect.y + rect.height);
			line2 = new Line2(rect.x + rect.width, rect.y, rect.x + rect.width,
					rect.y + rect.height);
		} // if

		Intersection set = new Intersection();
		Point2 point1 = intersection(line1, set);
		Point2 point2 = intersection(line2, set);

		if ((point1 == null) || (point2 == null))
			return (null);
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
	 * Erzeugt eine Kopie der Geraden.
	 * 
	 * @return die Kopie
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
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return Der naechste Punkt
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
	 * @param input_point
	 *            der Eingabepunkt
	 * 
	 * @return die Distanz zum Objekt
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
	 * Zeichnet die Gerade. Bitte nicht versuchen, das Clipping zu entfernen, da
	 * damit das zu zeichnende Segment bestimmt wird
	 * 
	 * <br>Author: Peter Koellner, Norbert Selle
	 * 
	 * @param g
	 *            Das Graphics-Objekt, in das gezeichnet wird
	 * @param gc
	 *            Der GraphicsContext
	 * 
	 * @see anja.util.Drawable
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{

		Rectangle2D clip_rectangle = BasicLine2.worldClipRectangle(g);

		if (clip_rectangle == null)
		{
			System.out.println("\nWarning: Line2.draw(): clip rectangle null");
		}
		else
		{
			Segment2 clipped = this.clip(clip_rectangle);

			if (clipped != null)
			{
				GeneralPath bp = new GeneralPath(GeneralPath.WIND_NON_ZERO);

				bp.moveTo(clipped.source().x, clipped.source().y);
				bp.lineTo(clipped.target().x, clipped.target().y);

				// save old rendering attributes
				Stroke s = g.getStroke();

				g.setColor(gc.getForegroundColor());
				g.setStroke(gc.getStroke());
				g.draw(bp);

				g.setStroke(s);
			} // if
		} // if

	} // draw


	// ************************************************************************

	/**
	 * Diese Methode ist nicht implementiert und liefert daher nur
	 * Point2.LIES_ON.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return LIES_ON
	 * 
	 * @see anja.geom.Point2#LIES_ON
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
	 * als Ergebnis zuruekgegeben.
	 * 
	 * @param input_basic
	 *            Die Eingabelinie
	 * @param inout_set
	 *            Die Schnittmenge (reference)
	 * 
	 * @return Der Schnittpunkt, wenn er existiert, sonst null
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
	 * Testet ob die Gerade das Rechteck schneidet. Vererbt von
	 * {@link anja.util.Drawable}.
	 * 
	 * @param input_box
	 *            Das zu testende Rechteck
	 * 
	 * @return true wenn ja, sonst false
	 */
	public boolean intersects(
			Rectangle2D input_box)
	{
		if ((input_box.contains(_source.x, _source.y))
				|| (input_box.contains(_target.x, _target.y)))
		{
			return (true); // Mindestens einer der die Geraden 
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
	 * Faellt das Lot vom Eingabepunkt auf die Gerade und gibt den Schnittpunkt
	 * zurueck.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return den Lotpunkt
	 */
	public Point2 plumb(
			Point2 input_point)
	{
		return (intersection(orthogonal(input_point), new Intersection()));

	} // plumb


	// ************************************************************************

	/**
	 * Berechnet das Quadrat der Distanz zwischen der Geraden und dem
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
		return input_point.squareDistance(plumb(input_point));

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
	 * @return den Schnittpunkt wen er existiert, sonst null
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
				// Gerade ist nur ein Punkt

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
				if (liesOn(input_line._source))
				{
					inout_intersection.set(new Line2(this));
				}
				else
				{
					inout_intersection.set();
				} // if
			} // if
		}
		else
		{
			inout_intersection.set(prepare.intersectionPoint);
		} // if

		return (inout_intersection.point2);

	} // _intersectionLine2

	// ************************************************************************

} // class Line2

