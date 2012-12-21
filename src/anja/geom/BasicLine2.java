package anja.geom;


import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.io.Serializable;

import anja.util.Drawable;
import anja.util.Matrix33;


/**
 * BasicLine2 ist eine abstrakte Basisklasse fuer Line2, Ray2 und Segment2, das
 * sind gerichtete Geraden, Strahlen und Segmente.<br>Alle diese Objekte werden
 * ueber die beiden Punkte _source und _target definiert, und sie sind wie
 * erwartet von _source nach _target gerichtet.<br>Um bei der
 * Schnittpunktberechnung im Fall von Parallelitaet trotz Rechenungenauigkeiten
 * korrekte Ergebnisse zu erzielen, verwendet die Methode inspectBasicLine()
 * eine <b>Epsilon-Umgebung</b>.
 * 
 * @version 1.4 10.11.1997
 * @author Norbert Selle
 * @see Line2
 * @see Ray2
 * @see Segment2
 */

abstract public class BasicLine2
		implements Drawable, Cloneable, Serializable
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	// Epsilon-Umgebung fuer inspectBasicLine()
	protected static final double	_EPSILON			= 5e-8d;

	// Konstanten fuer clipper():
	// Codierung der Punktposition in Bezug auf ein Window in vier Bits.
	// Ein Bit wird auf 1 gesetzt wenn der Punkt sich in der entsprechenden
	// Position zum Window befindet
	private static final int		_POS_INSIDE			= 0;					// alle Bits gleich 0 - im Window
	private static final int		_POS_ABOVE			= 1;					// Bit 1 - ueber dem Window
	private static final int		_POS_BELOW			= 2;					// Bit 2 - unter dem Window
	private static final int		_POS_RIGHT			= 4;					// Bit 3 - rechts vom Window
	private static final int		_POS_LEFT			= 8;					// Bit 4 - links vom Window

	// ************************************************************************
	// Variables
	// ************************************************************************

	/** der Startpunkt */
	protected Point2				_source;

	/** der Endpunkt */
	protected Point2				_target;

	/** An arbitrary string to label the point */
	protected String				_label				= null;

	/** A user defined value */
	protected float					_value				= java.lang.Float.NaN;

	/** Preserve the hash-code of the line, even if the line changes */
	protected int					_hashCode;

	/* Wird von worldClipRectangle() im Fehlerfall auf true gesetzt, um
	*  diesbezueglich nur eine Fehlermeldung zu geben
	*/
	static private boolean			_clip_error_occured	= false;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt die Linie von (0, 0) nach (0, 0).
	 */
	public BasicLine2()
	{
		this(new Point2(0, 0), new Point2(0, 0));

	} // BasicLine2


	// ********************************

	/**
	 * Erzeugt eine zur Eingabelinie identische Linie.
	 * 
	 * @param input_basicline
	 *            Eingabelinie
	 */
	public BasicLine2(
			BasicLine2 input_basicline)
	{
		this((Point2) input_basicline._source.clone(),
				(Point2) input_basicline._target.clone());

	} // BasicLine2


	// ********************************

	/**
	 * Erzeugt eine Linie durch den ersten Eingabepunkt in Richtung des zweiten
	 * Eingabepunktes, die Linie merkt sich <B>Referenzen</B> auf diese Punkte.
	 * 
	 * <B>Vorbedingungen:</B> Die Eingabepunkte sind ungleich null
	 * 
	 * @param input_source
	 *            der Startpunkt
	 * @param input_target
	 *            der Zielpunkt
	 */
	public BasicLine2(
			Point2 input_source,
			Point2 input_target)
	{
		_source = input_source;
		_target = input_target;
		_hashCode = super.hashCode();

	} // BasicLine2


	// ********************************

	/**
	 * Erzeugt eine Linie durch die Punkte mit den Eingabekoordinaten.
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
	public BasicLine2(
			float input_source_x,
			float input_source_y,
			float input_target_x,
			float input_target_y)
	{
		this(new Point2(input_source_x, input_source_y), new Point2(
				input_target_x, input_target_y));

	} // BasicLine2


	// ************************************************************************
	// Class methods
	// ************************************************************************

	/**
	 * Berechnet das sichtbare Reckteck in Weltkoordinaten, zur Vermeidung von
	 * Rundungsfehlern wird es etwas groesser gemacht. Wenn die Abfrage des
	 * sichtbaren Rechtecks des Graphics2D fehlschlaegt, dann wird das Rechteck
	 * mit den Eckpunkten (-32000, -32000) und (32000, 32000) in
	 * Geraetekoordinaten verwendet.
	 * 
	 * @return das sichtbare Rechteck, null wenn es nicht berechenbar ist
	 */
	public static Rectangle2D worldClipRectangle(
			Graphics2D input_g)
	{
		// Die Berechnung von clip_min und clip_max beruecksichtigt die
		// Moeglichkeit negativer Werte von width und height.
		// Zur Vermeidung von Rundungsfehlern wird 1 subtrahiert bzw.
		// addiert.

		Rectangle clip = input_g.getClipBounds();
		Point2D.Float clip_min = null;
		Point2D.Float clip_max = null;

		if (clip == null)
		{
			if (!BasicLine2._clip_error_occured)
			{
				System.out
						.println("Warning: BasicLine2.worldClipRectangle(): failed\n"
								+ "         (no further warnings about this context)");
				BasicLine2._clip_error_occured = true;
			} // if
			clip_min = new Point2D.Float(-32000, -32000);
			clip_max = new Point2D.Float(32000, 32000);
		}
		else
		{
			clip_min = new Point2D.Float(
					Math.min(clip.x, clip.x + clip.width) - 1, Math.min(clip.y,
							clip.y + clip.height) - 1);
			clip_max = new Point2D.Float(
					Math.max(clip.x, clip.x + clip.width) + 1, Math.max(clip.y,
							clip.y + clip.height) + 1);
		} // if

		// Transformation ins Weltkoordinatensystem

		/*
		 * 
		 *  This has been modified to test some suspicions related
		 *  to AffineTransform.
		 * 
		 * 
		 * 
		try
		{
			
		 AffineTransform scr_to_world = input_g.getTransform().createInverse();
		
		 scr_to_world.transform( clip_min, clip_min );
		 scr_to_world.transform( clip_max, clip_max );
		}
		catch (NoninvertibleTransformException ex)
		{
		return ( null );
		} //try*/

		return (new Rectangle2D.Float(Math.min(clip_min.x, clip_max.x), // x
				Math.min(clip_min.y, clip_max.y), // y
				Math.abs(clip_min.x - clip_max.x), // width
				Math.abs(clip_min.y - clip_max.y) // height
		));

	} // worldClipRectangle


	// ************************************************************************

	/**
	 * Clippen des Segments an den Seitenlinien des Rechtecks mittels des Cohen-
	 * Sutherland Clipping Algorithmus (aus 'Fundamentals of Interactive
	 * Computer Graphics' von J.D. Foley und A. Van Dam). Die Richtung des
	 * resultierenden Segments entspricht der Richtung der Linie. Liegt kein
	 * Teil der Linie im Rechteck, so wird null zurueckgegeben.
	 * 
	 * @param input_segment
	 *            Das Eingabesegment
	 * @param input_rectangle
	 *            Das Eingaberechteck
	 * 
	 * @return die geclippte Linie, null wenn sie nicht sichtbar ist
	 */
	public static Segment2 clipper(
			Segment2 input_segment,
			Rectangle2D input_rectangle)
	{
		boolean accept = false;
		boolean done = false;
		Point2 min = new Point2();
		Point2 max = new Point2();
		Point2 source = new Point2(input_segment.source());
		Point2 target = new Point2(input_segment.target());

		_setMinMax(min, max, input_rectangle);

		do
		{
			// check trivial reject

			int source_outcode = _outcodes(source, min, max);
			int target_outcode = _outcodes(target, min, max);

			if (_rejectCheck(source_outcode, target_outcode))
			{
				done = true;
			}
			else
			{
				// check trivial accept

				accept = _acceptCheck(source_outcode, target_outcode);
				if (accept)
				{
					done = true;
				}
				else
				{
					// subdivide line since at most one endpoint is inside

					// First, if source is inside window, exchange source and target and their
					// outcodes to guarantee that source is outside window

					if (source_outcode == _POS_INSIDE)
					{
						_swap(source, target);
						int swap_outcode = source_outcode;
						source_outcode = target_outcode;
						target_outcode = swap_outcode;
					} // if

					// Now perform a subdivision, move source to the intersection point; use
					// the formulas
					//    y = source.y + slope * (x - source.x)
					//    x = source.x + 1 / slope * (y - source.y)

					if ((source_outcode & _POS_ABOVE) != 0)
					{
						// divide line at top of window

						source.x = source.x + (target.x - source.x)
								* (max.y - source.y) / (target.y - source.y);
						source.y = max.y;
					}
					else if ((source_outcode & _POS_BELOW) != 0)
					{
						// divide line at bottom of window

						source.x = source.x + (target.x - source.x)
								* (min.y - source.y) / (target.y - source.y);
						source.y = min.y;
					}
					else if ((source_outcode & _POS_RIGHT) != 0)
					{
						// divide line at right edge of window

						source.y = source.y + (target.y - source.y)
								* (max.x - source.x) / (target.x - source.x);
						source.x = max.x;
					}
					else if ((source_outcode & _POS_LEFT) != 0)
					{
						// divide line at left edge of window

						source.y = source.y + (target.y - source.y)
								* (min.x - source.x) / (target.x - source.x);
						source.x = min.x;
					} // if
				} // if accept
			} // if reject
		}
		while (!done);

		if (accept)
		{
			if (input_segment.source().squareDistance(source) < input_segment
					.source().squareDistance(target))
				return (new Segment2(source, target));
			else
				return (new Segment2(target, source));
		}
		else
		{
			return (null);
		} // if

	} // clipper


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Berechnet den Punkt mit X-Koordinate x auf der Gerade neu. Nur wenn er
	 * auch auf dem Objekt liegt (abhaengig von abgeleiteter Klasse), wird er
	 * zurueckgegeben, ansonsten null.
	 * 
	 * @param x
	 *            X-Koordinate
	 * 
	 * @return Punkt auf dem Objekt
	 */
	public Point2 calculatePoint(
			float x)
	{
		if (isVertical())
		{
			if (x == _source.x)
				return new Point2(x, Double.POSITIVE_INFINITY);
			else
				return null;
		}
		else
		{
			double slope = (double) (_source.y - _target.y)
					/ (double) (_source.x - _target.x);
			double y_abs = _source.y - slope * _source.x;
			Point2 p = new Point2(x, (float) (slope * x + y_abs));
			if (liesOn(p))
				return p;
			else
				return null;
		}
	}


	// ************************************************************************

	/**
	 * Clippt die Linie am Rechteck und gibt das resultierende Segment zurueck,
	 * null wenn die Linie nicht im Rechteck liegt. Das Segment hat die gleiche
	 * Richtung wie die Linie.
	 * 
	 * @param input_rectangle
	 *            Das Eingaberechteck
	 * 
	 * @return die am Rechteck geclippte Linie oder null
	 */
	abstract public Segment2 clip(
			Rectangle2D input_rectangle);


	// ************************************************************************

	/**
	 * Gibt true zurueck bei Gleichheit, sonst false.
	 * 
	 * @return true bei Gleichheit, sonst false
	 */
	public boolean equals(
			Object input_object)
	{
		if (input_object == null)
			return (false);

		if (getClass() != input_object.getClass())
			return (false);

		BasicLine2 basicline = (BasicLine2) input_object;

		return (_source.equals(basicline._source) && _target
				.equals(basicline._target));

	} // equals


	// ************************************************************************

	/**
	 * This method retrieves a float value frac, that represents the point on
	 * the line, that is represented by the line, using the source as 0 and the
	 * target as 1.
	 * 
	 * @param frac
	 *            the factor of the point on the segment as explained above.
	 *            Negative values are legal, but perhaps do not make sense.
	 * 
	 * @return The point represented by the factor, null, if source and target
	 *         are equal
	 */
	public Point2 fraction2Point(
			double frac)
	{
		//The results of the calculation
		double x, y;

		//If both (source and target) are equal, this calculation doesn't make sense
		if (this.source().equals(this.target()))
		{
			return null;
		}
		
		if (frac == 0)
			return new Point2(this.source());
		if (frac == 1)
			return new Point2(this.target());

		x = (this.target().x - this.source().x) * frac + this.source().x;
		y = (this.target().y - this.source().y) * frac + this.source().y;

		return new Point2(x, y);
	}


	// ************************************************************************

	/**
	 * Untersucht die Lage des Eingabepunktes in Bezug auf die mit ihr auf einer
	 * Geraden liegenden Linie.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return Lage des Punktes bzgl der Linie
	 */
	public abstract int inspectCollinearPoint(
			Point2 input_point);


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge mit dem Kreisumfang und gibt sie im
	 * Intersection-Parameter zurueck. Die Schnittmenge kann leer sein oder
	 * einen Punkt oder eine Liste mit zwei Punkten enthalten, im Falle eines
	 * Punktes wird er ausserdem als Ergebnis der Methode zurueckgegeben.
	 * 
	 * @param input_circle
	 *            Der Eingabekreis
	 * @param inout_set
	 *            Die Referenz auf das zu berechnende Schnittobjekt
	 * 
	 * @return Den Schnittpunkt, falls es ein Punkt ist.
	 * 
	 * @see Circle2#intersection
	 */
	public Point2 intersection(
			Circle2 input_circle,
			Intersection inout_set)
	{
		return (input_circle.intersection(this, inout_set));

	} // intersection


	// ********************************

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

	abstract public Point2 intersection(
			BasicLine2 input_basic,
			Intersection inout_set);


	// ************************************************************************

	/**
	 * Untersucht die Beziehung zur Eingabelinie, bei Parallelitaet ist die
	 * Flagge <em>parallel</em> des Ergebnisses gleich true, sonst ist sie
	 * gleich false und die Komponenten <em>intersectionPoint</em>, <em>
	 * orderOnThis</em> und <em>orderOnParam</em> des Ergebnisses sind gesetzt.
	 * 
	 * @param input_basic
	 *            Die Eingabelinie
	 * 
	 * @return Ergebnis der Untersuchung
	 */
	public InspectResult inspectBasicLine(
			BasicLine2 input_basic)
	{
		double x1 = _source.x;
		double x2 = _target.x;
		double x3 = input_basic._source.x;
		double x4 = input_basic._target.x;

		double y1 = _source.y;
		double y2 = _target.y;
		double y3 = input_basic._source.y;
		double y4 = input_basic._target.y;

		double dx_1_2 = x2 - x1;
		double dy_1_2 = y2 - y1;
		double dx_3_4 = x4 - x3;
		double dy_3_4 = y4 - y3;

		InspectResult result = new InspectResult();

		double det = (dx_1_2 * dy_3_4) - (dy_1_2 * dx_3_4);

		double bound_det = (Math.abs(x2) + Math.abs(x1))
				* (Math.abs(y4) + Math.abs(y3)) + (Math.abs(y2) + Math.abs(y1))
				* (Math.abs(x4) + Math.abs(x3));

		if (Math.abs(det) <= bound_det * _EPSILON)
		{
			result.parallel = true;
		}
		else
		{
			result.parallel = false;

			// Einfuegung von Ulrich Handel  03.02.02
			//
			// Vor der Berechnung des Schnittpunktes der nicht parallelen
			// Linien ist noch zu pruefen, ob eines der Segmente
			//
			//   ( _source -> _target ),
			//   ( input_basic._source -> input_basic._target )
			//
			// mit einem seiner Endpunkte auf dem jeweils anderen Segment
			// liegt.
			//
			// Gilt z.B.
			//
			//   _source.inSegment( input_basic._source,
			//                      input_basic._target ) == true
			//
			// so ist mit  _source  bereits ein Linien-Schnittpunkt gefunden,
			// der auf beiden Segmenten liegt ( Point2.LIES_ON ).
			//
			// Ohne diese Sonderfallbehandlung kann es im o.g Fall vorkommen,
			// dass aufgrund von Rechenungenauigkeiten ein Schnittpunkt s
			// ermittelt wird, fuer den gilt:
			//
			//   s.inSegment( _source, _target ) == false
			//
			// Dies ist aber ein Widerspruch, der in einigen Anwendungen zu
			// Problemen durch nicht erkannte Segmentschnitte gefuehrt hat.
			//

			Point2 pt = null; // Zu ermittelnder Schittpunkt
			if (_source.inSegment(input_basic._source, input_basic._target))
				pt = _source;
			else if (_target
					.inSegment(input_basic._source, input_basic._target))
				pt = _target;
			else if (input_basic._source.inSegment(_source, _target))
				pt = input_basic._source;
			else if (input_basic._target.inSegment(_source, _target))
				pt = input_basic._target;

			if (pt != null)
			{
				// pt ist ein Endpunkt eines der beiden Segmente, der auf dem
				// jeweils anderen Segment liegt.
				// Er kann also als Schittpunkt gelten, welcher auf beiden
				// Segmenten liegt.

				result.intersectionPoint = new Point2(pt);

				result.orderOnThis = Point2.LIES_ON;
				result.orderOnParam = Point2.LIES_ON;

				return result; // fertig
			} // if

			// Ende der Einfuegung

			double dx_1_3 = x3 - x1;
			double dy_1_3 = y3 - y1;

			double lambda = ((dx_1_3 * dy_3_4) - (dy_1_3 * dx_3_4)) / det;
			double mue = ((dx_1_3 * dy_1_2) - (dy_1_3 * dx_1_2)) / det;

			double new_x;
			double new_y;

			// Die beiden folgenden if-Anweisungen dienen dazu, bei
			// horizontalen- oder vertikalen Linien die exakten Werte
			// und nicht die berechneten Werte zu verwenden, die mit
			// Rundungsfehlern behaftet sein koennten

			if ((dx_3_4 == 0) || (mue == 0))
				new_x = x3;
			else
				new_x = x1 + (lambda * dx_1_2);

			if ((dy_3_4 == 0) || (mue == 0))
				new_y = y3;
			else
				new_y = y1 + (lambda * dy_1_2);

			result.intersectionPoint = new Point2(new_x, new_y);

			result.lambda = lambda;
			result.mue = mue;

			result.orderOnThis = result.intersectionPoint
					.inspectCollinearPoint(_source, _target);
			result.orderOnParam = result.intersectionPoint
					.inspectCollinearPoint(input_basic._source,
							input_basic._target);

		} // if

		return (result);

	} // inspectBasicLine


	// ************************************************************************

	/**
	 * Testet ob der Punkt auf der Geraden durch source und target liegt.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return true wenn ja, sonst false
	 * 
	 * @see #liesOn
	 */
	public boolean isCollinear(
			Point2 input_point)
	{
		return (input_point.isCollinear(_source, _target));

	} // isCollinear


	// ************************************************************************

	/**
	 * Ist die Linie horizontal?
	 * 
	 * @return true wenn horizontal, false sonst
	 */
	public boolean isHorizontal()
	{
		return (_source.y == _target.y);

	} // isHorizontal()


	// ************************************************************************

	/**
	 * Ist die Linie vertikal?
	 * 
	 * @return true wenn vertikal, false sonst
	 */
	public boolean isVertical()
	{
		return (_source.x == _target.x);

	} // isVertical()


	// ************************************************************************

	/**
	 * Teste, ob die Linie parallel zu der Linie durch die zwei Eingabepunkte
	 * ist.
	 * 
	 * @param pointP
	 *            Punkt 1 auf der zu testenden Linie
	 * @param pointQ
	 *            entsprechend Punkt 2
	 * 
	 * @return true wenn parallel, false sonst
	 */
	public boolean isParallel(
			Point2 pointP,
			Point2 pointQ)
	{
		return (inspectBasicLine(new Line2(pointP, pointQ)).parallel);

	} // isParallel


	// ********************************

	/**
	 * Teste, ob die Linie parallel zu der Eingabegeraden ist.
	 * 
	 * @param input_basic
	 *            Die Eingabelinie
	 * 
	 * @return true wenn parallel, false sonst
	 */
	public boolean isParallel(
			BasicLine2 input_basic)
	{
		return (inspectBasicLine(input_basic).parallel);

	} // isParallel


	// ************************************************************************

	/**
	 * Testet ob der Eingabepunkt auf dem Objekt liegt.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return true wenn ja, sonst false
	 * 
	 * @see #isCollinear
	 */
	public boolean liesOn(
			Point2 input_point)
	{
		return ((isCollinear(input_point)) && (inspectCollinearPoint(input_point) == Point2.LIES_ON));

	} // liesOn


	// ************************************************************************

	/**
	 * Bestimmt ob der Eingabepunkt in der linken oder der rechten Halbebene
	 * oder auf der Linie liegt.<br> Ist die Linie nur ein Punkt so wird <b>
	 * ORIENTATION_COLLINEAR </b> zurueckgegeben, wenn der Eingabepunkt auf
	 * diesem Punkt liegt, sonst wird <b> ORIENTATION_UNDEFINED </b>
	 * zurueckgegeben. <br> <b> Vorbedingungen: </b> <br> der Eingabepunkt ist
	 * nicht null
	 * 
	 * @param input_point
	 *            Eingabepunkt
	 * 
	 * @return ORIENTATION_COLLINEAR, ORIENTATION_LEFT etc.
	 * 
	 * @see anja.geom.Point2#ORIENTATION_COLLINEAR
	 * @see anja.geom.Point2#ORIENTATION_LEFT
	 * @see anja.geom.Point2#ORIENTATION_RIGHT
	 * @see anja.geom.Point2#ORIENTATION_UNDEFINED
	 */
	public int orientation(
			Point2 input_point)
	{
		return (input_point.orientation(_source, _target));

	} // orientation


	// ************************************************************************

	/**
	 * Berechnet die orthogonale Gerade.
	 * 
	 * @return die orthogonale Gerade
	 */
	public Line2 orthogonal()
	{
		Point2 new_source;
		Point2 new_target;

		float delta_x = _target.x - _source.x;
		float delta_y = _target.y - _source.y;

		new_source = (Point2) _source.clone();
		new_target = new Point2(new_source.x + delta_y, new_source.y - delta_x);

		return (new Line2(new_source, new_target));

	} // orthogonal


	// ********************************

	/**
	 * Berechnet die orthogonale Gerade durch den Eingabepunkt.
	 * 
	 * @param input_point
	 *            Der Punkte, zu dem die Orthigonale berechnet werden soll
	 * 
	 * @return die orthogonale Gerade
	 */
	public Line2 orthogonal(
			Point2 input_point)
	{
		Point2 new_source;
		Point2 new_target;

		float delta_x = _target.x - _source.x;
		float delta_y = _target.y - _source.y;

		new_source = (Point2) input_point.clone();
		new_target = new Point2(new_source.x + delta_y, new_source.y - delta_x);

		return (new Line2(new_source, new_target));

	} // orthogonal


	// ************************************************************************

	/**
	 * This method retrieves a point p, that lies on the line. Using the source
	 * as 0 and the target as 1, we can now calculate the fraction of the point
	 * on this line.
	 * 
	 * @param p
	 *            the point on the segment as explained above.
	 * 
	 * @return The fraction represented by the point, NaN, if source and target
	 *         are equal or p is not lying on the line
	 */
	public double point2Fraction(
			Point2 p)
	{
		//The results of the calculation
		double frac;

		//If both (source and target) are equal, this calculation doesn't make sense
		if (this.source().equals(this.target()))
		{
			return Double.NaN;
		}

		//If the point diesn't lie on the segment, there is still no sense in this calculation
		if (!this.liesOn(p))
		{
			return Double.NaN;
		}
		else
		{
			//If the x values are equal, we know at this point, that the y values must differ.
			if (this.source().x == this.target().x)
			{
				frac = (p.y - this.source().y)
						/ (this.target().y - this.source().y);
			}
			//and the other way round
			else
			{
				frac = (p.x - this.source().x)
						/ (this.target().x - this.source().x);
			}
		}

		return frac;
	}


	// ************************************************************************

	/**
	 * Liefert die Steigung der Geraden.
	 * 
	 * @return Die Steigung
	 */
	public double slope()
	{
		if (isVertical())
			return Double.POSITIVE_INFINITY;
		return (double) (_source.y - _target.y)
				/ (double) (_source.x - _target.x);
	}


	// ************************************************************************

	/**
	 * Liefert eine <B>Referenz</B> auf den Startpunkt.
	 * 
	 * @return Der Startpunkt
	 */
	public Point2 source()
	{
		return (_source);

	} // source


	// ************************************************************************

	/**
	 * Setzt die <B>Referenz</B> auf den Startpunkt neu.
	 * 
	 * @param source
	 *            Der neue Startpunkt
	 */
	public void setSource(
			Point2 source)
	{

		_source = source;

	} // setSource


	// ************************************************************************

	/**
	 * Sets the (x,y)-coordinates of the source point <b>explicitly</b>.
	 * 
	 * <b>WARNING:</b><br> this method does not set the reference to the start
	 * point, but merely modifies it's coordinate values, as opposed to
	 * {@link BasicLine2#setSource(Point2)}. Therefore, in can cause a
	 * NullPointerException if for some reason the internal source point
	 * variable is <code><b>null</b></code>!
	 * 
	 * <b>WARNING:</b> There's an inherent loss of precision here due to
	 * internal casting of arguments to <code><b>float</b></code>. Be careful!
	 * 
	 * 
	 * @param x
	 *            new x coordinate
	 * @param y
	 *            new y coordinate
	 * 
	 */
	public void setSource(
			double x,
			double y)
	{
		_source.x = (float) x;
		_source.y = (float) y;
	}


	// ************************************************************************
	/**
	 * Liefert eine <B>Referenz</B> auf den Endpunkt.
	 * 
	 * @return den Endpunkt
	 */
	public Point2 target()
	{
		return (_target);

	} // target


	// ************************************************************************

	/**
	 * Setzt die <B>Referenz</B> auf den Endpunkt neu.
	 * 
	 * @param target
	 *            der neue Endpunkt
	 */
	public void setTarget(
			Point2 target)
	{

		_target = target;

	} // setTarget


	// ************************************************************************

	/**
	 * Sets the (x,y)-coordinates of the target point <b>explicitly</b>.
	 * 
	 * <b>WARNING:</b><br> this method does not set the reference to the target
	 * point, but merely modifies it's coordinate values, as opposed to
	 * {@link BasicLine2#setSource(Point2)}. Therefore, in can cause a
	 * NullPointerException if for some reason the internal target point
	 * variable is <code><b>null</b></code>!
	 * 
	 * <b>WARNING:</b><br> There's an inherent loss of precision here due to
	 * internal casting of arguments to <code><b>float</b></code>. Be careful!
	 * 
	 * 
	 * @param x
	 *            new x coordinate
	 * @param y
	 *            new y coordinate
	 * 
	 */
	public void setTarget(
			double x,
			double y)
	{
		_target.x = (float) x;
		_target.y = (float) y;
	}


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation.
	 * 
	 * @return die textuelle Repraesentation
	 */
	public String toString()
	{
		String output_string;

		output_string = _source.toString() + "-->" + _target.toString();

		return (output_string);

	} // toString


	// ************************************************************************

	/**
	 * Verschiebt die Linie um den Vektor vom Nullpunkt zum Eingabepunkt.
	 * <BR><B>Vorbedingungen:</B> Der Eingabepunkt ist nicht null
	 * 
	 * @param input_point
	 *            der Eingabepunkt
	 */
	public void translate(
			Point2 input_point)
	{
		_source.translate(input_point);
		_target.translate(input_point);

	} // translate


	// ********************************

	/**
	 * Verschiebt die Linie um die Eingabewerte.
	 * 
	 * @param input_horizontal
	 *            die horizontale Verschiebung
	 * @param input_vertical
	 *            die vertikale Verschiebung
	 */
	public void translate(
			float input_horizontal,
			float input_vertical)
	{
		_source.translate(input_horizontal, input_vertical);
		_target.translate(input_horizontal, input_vertical);

	} // translate


	// ********************************

	/**
	 * Affine Abbildung der Punkte gem&auml;&szlig; der Transformationsmatrix
	 * <i>a</i>
	 * 
	 * <br>Version: 08.09.98<br> Author: Thomas Kamphans
	 * 
	 * @param a
	 *            Die Transformationsmatrix
	 * 
	 */
	//============================================================
	public void transform(
			Matrix33 a)
	//============================================================
	{
		_source.transform(a);
		_target.transform(a);

	} // transform


	/**
	 * Set the label of the point.
	 * 
	 * <br>Version: 16.07.01<br> Author: Thomas Kamphans
	 * 
	 * @param s
	 *            The new label
	 */
	//============================================================
	public void setLabel(
			String s)
	//============================================================
	{
		_label = s;
	} // setLabel


	/**
	 * Return the label of the point.
	 * 
	 * <br>Version: 16.07.01<br> Author: Thomas Kamphans
	 * 
	 * @return The label
	 */
	//============================================================
	public String getLabel()
	//============================================================
	{
		return _label;
	} // getLabel


	/**
	 * Set the value of the point.
	 * 
	 * <br>Version: 16.07.01<br> Author: Thomas Kamphans
	 * 
	 * @param f
	 *            The new value
	 */
	//============================================================
	public void setValue(
			float f)
	//============================================================
	{
		_value = f;
	} // setValue


	/**
	 * Return the value of the point.
	 * 
	 * <br>Version: 16.07.01<br> Author: Thomas Kamphans
	 * 
	 * @return The value
	 */
	//============================================================
	public float getValue()
	//============================================================
	{
		return _value;
	} // getValue


	/**
	 * Liefert einen fuer das Objekt eindeutigen HashCode.
	 * 
	 * <br>Version: 26.09.99<br> Author: Lars Kunert
	 * 
	 * @return Hash-Code
	 */
	/*
	  Das Ueberschreiben der Methode Object.hashCode wurde
	  notwendig,  da ab Java v 1.1 der HashCode abhaengig von den
	  aktuellen Attribut-Werten des Objektes bestimt wird.
	  Drawable-Objekte werden beim Einfuegen in eine Layer in
	  einer Hashtable abgelegt; wird nun ein Punkt gedraggt, so
	  aendert sich der zugehoerige Object und eine Lokalisierung
	  schlaegt fehl.
	*/
	//============================================================
	public int hashCode()
	//============================================================
	{
		return _hashCode;
	}


	// ************************************************************************
	// Private methods
	// ************************************************************************

	// ************************************************************************
	// Methoden zur Unterstuetzung von clipper()
	// ************************************************************************

	// true wenn beide Outcodes im Window liegen

	/**
	 * Checks, if both are inside the window
	 * 
	 * @param outcode1
	 *            First outcode
	 * @param outcode2
	 *            Second outcode
	 * 
	 * @return true, if inside the windows, false else
	 */
	private static boolean _acceptCheck(
			int outcode1,
			int outcode2)
	{
		return ((outcode1 == _POS_INSIDE) && (outcode2 == _POS_INSIDE));

	} // _acceptCheck


	// ********************************

	// true wenn beide Outcodes ueber, unter, links von oder rechts von dem Fenster sind

	/**
	 * Checks, if both are outside the window
	 * 
	 * @param outcode1
	 *            First outcode
	 * @param outcode2
	 *            Second outcode
	 * 
	 * @return true, if both outcodes are outside the window, false else
	 */
	private static boolean _rejectCheck(
			int outcode1,
			int outcode2)
	{
		return ((outcode1 & outcode2) != 0);

	} // _rejectCheck


	// ********************************

	// Outcode zu einem Punkt und einem Fenster ermitteln

	/**
	 * Computes the outcode of a point and a window
	 * 
	 * @param input_point
	 *            Input
	 * @param input_min
	 *            Input Minimum
	 * @param input_max
	 *            Input maximum
	 * 
	 * @return The outcode of a point and a window
	 */
	private static int _outcodes(
			Point2 input_point,
			Point2 input_min,
			Point2 input_max)
	{
		int outcode = _POS_INSIDE;

		if (input_point.y > input_max.y)
			outcode |= _POS_ABOVE;
		else if (input_point.y < input_min.y)
			outcode |= _POS_BELOW;

		if (input_point.x > input_max.x)
			outcode |= _POS_RIGHT;
		else if (input_point.x < input_min.x)
			outcode |= _POS_LEFT;

		return (outcode);

	} // _outcodes


	// ********************************

	// Vertauschen die Koordinaten zweier Punkte

	/**
	 * Swap the coordinates of two points
	 * 
	 * @param p1
	 *            First point
	 * @param p2
	 *            Second point
	 */
	private static void _swap(
			Point2 p1,
			Point2 p2)
	{
		Point2 swap = new Point2(p1);

		p1.moveTo(p2);
		p2.moveTo(swap);

	} // _swap


	// ********************************

	/**
	 * Calculate the min/max of the two points
	 * 
	 * @param inout_min
	 *            The first point
	 * @param inout_max
	 *            The second point
	 * @param input_rect
	 *            The rectangle
	 */
	private static void _setMinMax(
			Point2 inout_min,
			Point2 inout_max,
			Rectangle2D input_rect)
	{
		float x = ((Rectangle2D.Float) input_rect).x;
		float y = ((Rectangle2D.Float) input_rect).y;
		float width = ((Rectangle2D.Float) input_rect).width;
		float height = ((Rectangle2D.Float) input_rect).height;

		/*
		 * 03.06.2004 Patched this to work internally with 
		 * Rectangle2D.Float, since I don't know yet if this method 
		 * ever actually gets a generalized Rectangle2D argument
		 * 
		 */

		/*
		inout_min.x = Math.min( input_rect.x, input_rect.x + input_rect.width );
		inout_max.x = Math.max( input_rect.x, input_rect.x + input_rect.width );

		inout_min.y = Math.min( input_rect.y, input_rect.y + input_rect.height );
		inout_max.y = Math.max( input_rect.y, input_rect.y + input_rect.height );*/

		inout_min.x = Math.min(x, x + width);
		inout_max.x = Math.max(x, x + width);

		inout_min.y = Math.min(y, y + height);
		inout_max.y = Math.max(y, y + height);

	} // _setMinMax

	// ************************************************************************

} // class BasicLine2

