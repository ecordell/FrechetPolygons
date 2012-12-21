package anja.geom;


import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.util.Vector;

//import java_ersatz.java2d.BezierPath;
import java.awt.geom.GeneralPath;

import anja.util.GraphicsContext;
import anja.util.List;
import anja.util.ListItem;
import anja.util.Sorter;


/**
 * Zweidimensionales zeichenbares Segment.
 * 
 * @version 1.4 19.12.1997
 * @author Norbert Selle
 */

public class Segment2
		extends BasicLine2
{

	// ************************************************************************
	// Klassen-Methoden zu Segment2-Arrays.
	//
	// eingefuegt von Ulrich Handel am 28.02.02
	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation.
	 * 
	 * @return die textuelle Repraesentation
	 */
	public String toString()
	{
		return "Segment2 < " + _source.toString() + " | " + _target.toString()
				+ " >";

	} // toString


	// ************************************************************************

	/**
	 * Berechnen des umschliessenden Rechtecks der im array segments gegebenen
	 * Segmente.
	 * 
	 * @return Das Bounding-Rectangle oder null, falls das array leer ist
	 */
	static public Rectangle2D getBoundingRect(
			Segment2[] segments)
	{
		if (segments.length == 0)
			return null;

		float xmin = Float.MAX_VALUE;
		float ymin = Float.MAX_VALUE;
		float xmax = -Float.MAX_VALUE;
		float ymax = -Float.MAX_VALUE;

		for (int i = 0; i < segments.length; i++)
		{
			Segment2 seg = segments[i];

			Point2 pt1 = seg.source();
			Point2 pt2 = seg.target();

			if (pt1.x < xmin)
				xmin = pt1.x;
			if (pt2.x < xmin)
				xmin = pt2.x;
			if (pt1.x > xmax)
				xmax = pt1.x;
			if (pt2.x > xmax)
				xmax = pt2.x;
			if (pt1.y < ymin)
				ymin = pt1.y;
			if (pt2.y < ymin)
				ymin = pt2.y;
			if (pt1.y > ymax)
				ymax = pt1.y;
			if (pt2.y > ymax)
				ymax = pt2.y;
		} // for

		return new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin);
	} // getBoundingRect


	/**
	 * Alle Segmente aus segments ermitteln, die das gegebene Rechteck schneiden
	 * ( bzw innerhalb des Rechtecks liegen ).
	 * 
	 * @param segments
	 *            Die Segmente
	 * @param rect
	 *            Das Rechteck
	 * 
	 * @return Ein array aller Segmente, die das Rechteck schneiden.
	 */
	static public Segment2[] getIntersectionSegments(
			Segment2[] segments,
			Rectangle2D rect)
	{
		Vector result = new Vector();

		for (int i = 0; i < segments.length; i++)
		{
			Segment2 seg = segments[i];

			if (seg.intersects(rect))
				result.addElement(seg);
		} // for

		Segment2[] array = new Segment2[result.size()];
		result.copyInto(array);
		return array;
	} // getIntersectionSegments


	/**
	 * Alle Segmente aus segments1 mit allen Segmenten aus segments2 auf Schnitt
	 * testen.
	 * 
	 * Wichtig !!! 2 Segmente die eine Verbindung zwischen den identischen
	 * Punkten sind duerfen ( und muessen ) sich schneiden, ohne dass dies
	 * Beachtung findet. 2 Segmente die einen gemeinsamen identischen Punkt
	 * haben duerfen sich genau in diesem Punkt schneiden, ohne dass dies
	 * Beachtung findet.
	 * 
	 * @param segments1
	 *            Menge von Segmenten
	 * @param segments2
	 *            Andere Menge von Segmenten
	 * 
	 * @return true, false ein Schnitt gefunden wurde
	 */
	static public boolean intersects(
			Segment2[] segments1,
			Segment2[] segments2)
	{
		Intersection set = new Intersection();

		for (int i = 0; i < segments1.length; i++)
		{
			Segment2 seg1 = segments1[i];

			for (int j = 0; j < segments2.length; j++)
			{
				Segment2 seg2 = segments2[j];

				if ((seg1.source() == seg2.source() && seg1.target() == seg2
						.target())
						|| (seg1.source() == seg2.target() && seg1.target() == seg2
								.source()))
					continue; // Es handelt sich bei den beiden Segmenten
				// um die Verbindung zwischen den gleichen
				// Punkten, diese muessen und duerfen sich
				// schneiden

				seg1.intersection(seg2, set);

				if (seg1.source() == seg2.target()
						|| seg1.target() == seg2.source()
						|| seg1.source() == seg2.source()
						|| seg1.target() == seg2.target())
				{
					// Die beiden Segmente haben genau einen gemeinsamen
					// Punkt und sind deshalb Nachbarn.
					// Sie muessen und duerfen sich nur genau in diesem
					// Punkt schneiden

					if (set.result != Intersection.POINT2)
						return true;

					continue;
				} // if

				// Die beiden Segmente haben keine gemeinsamen Punkte
				// und duerfen sich ueberhaupt nicht schneiden

				if (set.result != Intersection.EMPTY)
					return true;

			} // for
		} // for

		return false; // Kein Schnitt gefunden
	} // intersects


	/**
	 * Alle Segmente aus segments1, die das Rechteck rect schneiden mit allen
	 * Segmenten aus segments2, die das Rechteck rect schneiden, auf Schnitt
	 * testen.
	 * 
	 * @param segments1
	 *            Menge von Segmenten
	 * @param segments2
	 *            Andere Menge von Segmenten
	 * @param rect
	 *            Das Rechteck
	 * 
	 * @return true, false ein Schnitt gefunden wurde
	 * 
	 * @see #intersects(Segment2[],Segment2[])
	 */
	static public boolean intersects(
			Segment2[] segments1,
			Segment2[] segments2,
			Rectangle2D rect)
	//============================================================
	{
		// Ermitteln der Segmente aus segmests1 und der Segmente aus
		// segments2, die das Rechteck schneiden
		//
		segments1 = getIntersectionSegments(segments1, rect);
		segments2 = getIntersectionSegments(segments2, rect);

		return intersects(segments1, segments2);
	} // intersects


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
		super((BasicLine2) input_segment);

	} // Segment2


	// ********************************

	/**
	 * Erzeugt ein Segment vom ersten Eingabepunkt zum zweiten Eingabepunkt, das
	 * Segment merkt sich <B>Referenzen</B> auf diese Punkte.
	 * 
	 * <BR><B>Vorbedingungen:</B> <BR>die Eingabepunkte sind ungleich null
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
			float input_source_x,
			float input_source_y,
			float input_target_x,
			float input_target_y)
	{
		super(input_source_x, input_source_y, input_target_x, input_target_y);

	} // Segment2


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Liefert den "Mittelpunkt" des Segments, also den Punkt, der das Segment
	 * in zwei gleichlange Teilsegmente teilt.
	 * 
	 * @return den Mittelpunkt des Segments
	 */
	public Point2 center()
	{

		float x = _source.x + (_target.x - _source.x) / 2;
		float y = _source.y + (_target.y - _source.y) / 2;
		Point2 center = new Point2(x, y);
		return center;

	} // center


	/**
	 * Clippt das Segment am Rechteck und gibt das resultierende Segment
	 * zurueck, null wenn das Segment nicht im Rechteck liegt. Das Resultat hat
	 * die gleiche Richtung wie das Eingabesegment.
	 * 
	 * @return das am Rechteck geclippte Segment oder null
	 */
	public Segment2 clip(
			Rectangle2D rect)
	{
		return (BasicLine2.clipper(this, rect));

	} // clip


	// ************************************************************************
	/**
	 * Kopiert das Segment.
	 * 
	 * @return die Kopie
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
		Rectangle2D clip_rectangle = BasicLine2.worldClipRectangle(g);

		if (clip_rectangle == null)
		{
			System.out.println("Warning: Segment2.draw(): clip rectangle null");
		}
		else
		{
			Segment2 clipped = this.clip(clip_rectangle);

			if (clipped != null)
			{
				GeneralPath bp = new GeneralPath(GeneralPath.WIND_NON_ZERO);

				bp.moveTo(clipped.source().x, clipped.source().y);
				bp.lineTo(clipped.target().x, clipped.target().y);
				g.setColor(gc.getForegroundColor());
				g.setStroke(gc.getStroke());
				g.draw(bp);

				// Anfangs- und Endpunkt zeichnen

				if (clipped.source().equals(source()))
					source().draw(g, gc);
				if (clipped.target().equals(target()))
					target().draw(g, gc);
			} // if
		} // if

	} // draw


	// ********************************

	/**
	 * Zeichnet ein Segment.
	 * 
	 * @param device_source_x
	 *            X-Wert des Startpunkts
	 * @param device_source_y
	 *            Y-Wert des Startpunkts
	 * @param device_target_x
	 *            X-Wert des Endpunkts
	 * @param device_target_y
	 *            Y-Wert des Endpunkts
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gc
	 *            Die dazugehörigen Formatierungsregeln
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
	 * Gibt das umschließende Rechteck zurueck.
	 * 
	 * @return Das umschließende Rechteck
	 */
	public Rectangle2D getBoundingRect()
	{
		return (new Rectangle2D.Double(_source.x, // x
				_source.y, // y
				_target.x - _source.x, // width
				_target.y - _source.y) // height
		);

	} // getBoundingRect


	// ************************************************************************

	/**
	 * Untersucht die Lage des Eingabepunktes in Bezug auf das mit ihm auf einer
	 * Geraden liegende Segment.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
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
		return (input_point.inspectCollinearPoint(this._source, this._target));

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
		if ((input_box.contains(_source.x, _source.y))
				|| (input_box.contains(_target.x, _target.y)))
		{
			return (true); // Mindestens ein Endpunkt liegt im Rechteck
		} // if

		// Changed if-statement, please consider the note
		// at anja.geom.Rectangle2.intersectsOrTouches() !
		Rectangle2 rect = new Rectangle2(getBoundingRect());
		if (!rect.intersectsOrTouches(input_box))
		{
			return (false); // Das umschliessende Rechteck
			// schneidet input_box nicht
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
	 * Berechnet die Laenge Des Segments in der euklidischen Metrik
	 * 
	 * @return Die euklidische Länge
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
	 * @param input_point
	 *            Der Eingabepunkt
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
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return den naechsten Punkt
	 * 
	 * @see #plumb
	 */
	public Point2 closestPoint(
			Point2 input_point)
	{
		Point2 plumbPoint = plumb(input_point);

		if (plumbPoint != null)
			return plumbPoint;

		if (input_point.squareDistance(_source) <= input_point
				.squareDistance(_target))
			return new Point2(_source);
		else
			return new Point2(_target);

	} // closestPoint


	// ************************************************************************

	/**
	 * Berechnet die Distanz zwischen dem Segment und dem Eingabepunkt.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return Die Distanz
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
		Point2 plumbPoint = plumb(input_point);

		if (plumbPoint != null)
		{
			return plumbPoint.squareDistance(input_point);
		}// if

		return Math.min(input_point.squareDistance(_source), input_point
				.squareDistance(_target));

	} // squareDistance


	/**
	 * Liefert den oberen Endpunkt des Segmentes zurueck. Ist das Segment
	 * waagerecht, so wird der rechte Endpunkt zurueckgegeben.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @return Point2 der obere Endpunkt
	 */
	public Point2 getUpperPoint()
	{
		return (source().comparePointsLexY(target()) == 1) ? source()
				: target();
	}


	/**
	 * Liefert den unteren Endpunkt des Segmentes zurueck. Ist das Segment
	 * waagerecht, so wird der linke Endpunkt zurueckgegeben.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @return Point2 der untere Endpunkt
	 */
	public Point2 getLowerPoint()
	{
		return (source().comparePointsLexY(target()) == -1) ? source()
				: target();
	}


	/**
	 * Liefert den linken Endpunkt des Segmentes zurueck. Ist das Segment
	 * senkrecht, so wird der untere Endpunkt zurueckgegeben.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @return Point2 der linken Endpunkt
	 */
	public Point2 getLeftPoint()
	{
		return (source().comparePointsLexX(target()) == -1) ? source()
				: target();
	}


	/**
	 * Liefert den rechten Endpunkt des Segmentes zurueck. Ist das Segment
	 * senkrecht, so wird der obere Endpunkt zurueckgegeben.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @return Point2 der rechten Endpunkt
	 */
	public Point2 getRightPoint()
	{
		return (source().comparePointsLexX(target()) == 1) ? source()
				: target();
	}


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge des Segments mit dem mit ihm auf einer Geraden
	 * liegenden Eingabestrahls und gibt sie im Intersection- Parameter zurueck.
	 * 
	 * @param input_ray
	 *            Der Eingabestrahl
	 * @param inout_intersection
	 *            Das Schnittobjekt
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

	/**
	 * Berechnet die Schnittmenge des Segments mit dem mit ihm auf einer Geraden
	 * liegenden Eingabesegments und gibt sie im Intersection- Parameter
	 * zurueck.
	 * 
	 * @param input_segment
	 *            Das Eingabesegment
	 * @param inout_intersection
	 *            Das Schnittobjekt
	 */
	private void _collinearSegmentIntersection(
			Segment2 input_segment,
			Intersection inout_intersection)
	{
		// Changed if-statement, please consider the note
		// at anja.geom.Rectangle2.intersectsOrTouches() !
		Rectangle2 rect = new Rectangle2(getBoundingRect());
		if (rect.intersectsOrTouches(input_segment.getBoundingRect()))
		{
			List points = new List();
			PointComparitor compare = new PointComparitor();

			// Punkte der Groesse nach sortieren

			points.add(_source);
			points.add(_target);
			points.add(input_segment._source);
			points.add(input_segment._target);

			// compare.setOrder( PointComparitor.X_ORDER );

			// Ersetzung von Ulrich Handel  24.11.01
			//
			// !!! Fehler-Beseitigung !!!
			//
			// Es reicht nicht einfach nur nach X zu sortieren,
			// da z.B. die Segmente
			//  (40, 20)-->(39.999999, 100)
			//  (40, 20)-->(40, 0)
			// zwar als collinear erkannt werden, aber durch Sortieren
			// nach X in in die Reihenfolge
			//  (39.999999, 100)
			//  (40, 0)
			//  (40, 20)
			//  (40, 20)
			// gebracht werden.
			// Als Schnittmenge ergibt sich dann das Segment
			//  (40, 0)-->(40, 20)
			// statt dem Punkt (40 ,20)

			compare.setOrder(PointComparitor.X_ORDER);
			double xmin = ((Point2) points.min(compare).value()).x;
			double xmax = ((Point2) points.max(compare).value()).x;

			compare.setOrder(PointComparitor.Y_ORDER);
			double ymin = ((Point2) points.min(compare).value()).y;
			double ymax = ((Point2) points.max(compare).value()).y;

			if ((xmax - xmin) > (ymax - ymin))
				compare.setOrder(PointComparitor.X_ORDER);
			else
				compare.setOrder(PointComparitor.Y_ORDER);

			// Ende der Ersetzung

			points.sort(compare, Sorter.ASCENDING);

			// die Schnittmenge reicht vom zweiten bis zum dritten Punkt

			ListItem second_item = points.first().next();
			ListItem third_item = second_item.next();

			Point2 second_point = (Point2) second_item.value();
			Point2 third_point = (Point2) third_item.value();

			if (second_point.equals(third_point))
			{
				inout_intersection.set(new Point2(second_point));
			}
			else
			{
				inout_intersection.set(new Segment2(second_point, third_point));
			} // if
		}
		else
		{
			inout_intersection.set();
		} // if

	} // _collinearSegmentIntersection


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge mit der Eingabegeraden und gibt sie im
	 * Intersection-Parameter zurueck, ist es ein Schnittpunkt wird er ausserdem
	 * als Ergebnis zurueckgegeben.
	 * 
	 * @param input_line
	 *            Die Gerade zur Schnittpunktberechnung
	 * @param inout_intersection
	 *            Die Schnittmenge
	 * 
	 * @return Der Schnittpunkt, wenn er existiert, sonst null
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
	 *            Der Strahl zur Schnittpunktberechnung
	 * @param inout_intersection
	 *            Die Schnittmenge
	 * 
	 * @return Der Schnittpunkt, wenn er existiert, sonst null
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
	 *            Das Segment zur Schnittpunktberechnung
	 * @param inout_intersection
	 *            Die Schnittmenge
	 * 
	 * @return Der Schnittpunkt, wenn er existiert, sonst null
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
		}//if
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

