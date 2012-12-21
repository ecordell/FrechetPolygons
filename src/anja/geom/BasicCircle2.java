package anja.geom;


import anja.util.Drawable;
import anja.util.GraphicsContext;
import anja.util.List;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import anja.util.MathConstants;


/**
 * BasicCircle2 ist die abstrakte Basisklasse für Arc2 und Circle2. Sie werden
 * über den Mittelpunkt <tt>centre</tt> und den Radius <tt> radius </tt>
 * definiert. <tt>inspectBasicCircle()</tt> liegt den Schnittmengenberechnungen
 * der abgeleiteten Klassen zugrunde und verwendet eine <b>
 * Epsilon-Umgebung</b>.
 * 
 * @version 1.2 10.09.1997
 * @author Norbert Selle
 * 
 * @see Arc2
 * @see Circle2
 */

abstract public class BasicCircle2
		implements Drawable, Cloneable, MathConstants, java.io.Serializable
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	/** Epsilon-Umgebung */
	private static final double	_EPSILON	= 5e-8d;

	// ************************************************************************
	// Variables
	// ************************************************************************

	/** der Mittelpunkt */
	public Point2				centre;

	/** der Radius */
	public float				radius;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt ein Objekt mit dem Mittelpunkt (0, 0) und dem Radius 0.
	 */
	public BasicCircle2()
	{
		this(new Point2(0, 0), 0);

	} // BasicCircle2


	// ********************************

	/**
	 * Erzeugt ein Objekt mit den Eingabeparametern, es merkt sich eine <b>
	 * Referenz</b> auf den Mittelpunkt.
	 * 
	 * @param input_centre
	 *            der Mittelpunkt
	 * @param input_radius
	 *            der Radius
	 */
	public BasicCircle2(
			Point2 input_centre,
			float input_radius)
	{
		centre = input_centre;
		radius = input_radius;

	} // BasicCircle2


	// ************************************************************************
	// Class methods
	// ************************************************************************

	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Erzeugt eine Kopie.
	 * 
	 * @return die Kopie
	 */
	abstract public Object clone();


	// ************************************************************************

	/**
	 * Zeichnet das Objekt.
	 * 
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gc
	 *            Die dazugehörigen Formatierungsangaben
	 */
	abstract public void draw(
			Graphics2D g,
			GraphicsContext gc);


	// ************************************************************************

	/**
	 * Gibt das umschließende Rechteck zurück.
	 * 
	 * @return Das umschließende Rechteck
	 */
	abstract public Rectangle2D getBoundingRect();


	// ************************************************************************

	/**
	 * Untersucht die Schnittmenge mit dem eingegebenen BasicCircle. Bei leerer
	 * Schnittmenge wird null zurückgegeben. Sind die Mittelpunkte und Radien
	 * gleich, so ist lies_on gleich true und points gleich null. Ansonsten ist
	 * lies_on gleich false und points[] hat die Länge eins oder zwei und
	 * enthält den einen oder die zwei Schnittpunkte der Kreisumfänge.
	 * 
	 * @param input_basic
	 *            Der Eingabekreis
	 * 
	 * @return null bei leerer Schnittmenge, sonst ein InspectBCResult
	 */
	protected InspectBCResult inspectBasicCircle(
			BasicCircle2 input_basic)
	{
		double amount = radius + input_basic.radius;
		double distance = centre.distance(input_basic.centre);

		if ((amount == 0) // Fall 0: Punkt auf Punkt
				&& (distance == 0))
		{
			InspectBCResult result = new InspectBCResult();
			result.lies_on = true;
			return (result);
		} // if

		// Zuerst auf Epsilon-Gleichheit prüfen

		if (_outerEpsilonKiss(input_basic)) // Fall I: d = r1 + r2
			return (_outerKissPoint(input_basic, distance));

		if (distance > amount) // Fall II: d > r1 + r2
			return (null);

		// Fall III: d < r1 + r2

		double delta = Math.abs(radius - input_basic.radius);

		if (distance == 0) // Fall III a: d = 0
		{
			if (delta == 0) // Fall III a 1.: r1 = r2
			{
				InspectBCResult result = new InspectBCResult();
				result.lies_on = true;
				return (result);
			} // if
			else
				// Fall III a 2.: r1 <> r2
				return (null);
		} // if

		// Fall III b: d <> 0

		if (_innerEpsilonKiss(input_basic)) // Fall III b 1.:
			// d = abs( r2 - r1 )
			return (_innerKissPoint(input_basic, distance));

		if (distance < delta) // Fall III b 2.: d < abs( r2 - r1 )
			return (null);

		// Fall III b 3.: d > abs( r2 - r1 )

		return (_intersectionPoints(input_basic));

	} // inspectBasicCircle


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge mit den Polygonkanten und gibt sie im
	 * Intersection-Parameter zurück, sie kann leer sein oder eine eine
	 * nichtleere Liste von Punkten enthalten.
	 * 
	 * @param input_polygon
	 *            Das Eingabepolygon
	 * @param inout_intersection
	 *            Referenz auf das Schnittobjekt
	 */
	public void intersection(
			Polygon2 input_polygon,
			Intersection inout_intersection)
	{
		input_polygon.intersection(this, inout_intersection);

	} // intersection


	// ********************************

	/**
	 * Berechnet die Schnittmenge des Umfangs mit der Eingabelinie und gibt sie
	 * im Intersection-Parameter zurück. Die Schnittmenge kann leer sein oder
	 * einen Punkt oder eine Liste mit zwei Punkten enthalten, im Falle eines
	 * Punktes wird er außerdem als Ergebnis der Methode zurückgegeben. <br> Ein
	 * Segment oder ein Strahl kann den Kreis auch dann in genau einem Punkt
	 * schneiden, wenn es keine Tangente ist, sondern im Kreisinneren beginnt
	 * oder endet.
	 * 
	 * <br><dl><b>Author:</b> <dd>Urs Bachert<dd>, Norbert Selle (Anpassung an
	 * anja/geom)</dl>
	 * 
	 * @param input_line
	 *            Die Eingabelinie
	 * @param inout_intersection
	 *            Referenz auf das Schnittobjekt
	 * 
	 * @return Den eindeutigen Schnittpunkt, falls es einen solchen gibt
	 */
	public Point2 intersection(
			BasicLine2 input_line,
			Intersection inout_intersection)
	{
		// equations:
		// I. source.y = m * source.x + b (line)
		// II. target.y = m * target.x + b (line)
		// III. (x - centre.x)² + (y - centre.y)² = centre.r² (circle)

		Point2 source = input_line.source();
		Point2 target = input_line.target();
		List points = new List();

		// quadratic equation coeffs

		double a = (target.x - source.x) * (target.x - source.x)
				+ (target.y - source.y) * (target.y - source.y);

		double b = 2 * ((target.x - source.x) * (source.x - centre.x) + (target.y - source.y)
				* (source.y - centre.y));

		double c = (centre.x * centre.x + centre.y * centre.y + source.x
				* source.x + source.y * source.y)
				- 2
				* (centre.x * source.x + centre.y * source.y)
				- radius
				* radius;

		// now solve for t via discriminant

		double discriminant = b * b - 4 * a * c;

		double t1 = 0, t2 = 0;

		if (discriminant > 0) // 2 points
		{
			t1 = (-b - Math.sqrt(discriminant)) / (2 * a);
			t2 = (-b + Math.sqrt(discriminant)) / (2 * a);
		}
		else if (Math.abs(discriminant) < DBL_EPSILON) // 1 point
		{
			t1 = t2 = -b / (2 * a);
		}
		else
		// no solutions
		{
			inout_intersection.set();
			return (inout_intersection.point2);
		}

		// check if the resulting points actually lie within the line segment

		Point2 p1, p2;

		if ((t1 >= 0.0) && (t1 <= 1.0))
		{
			p1 = new Point2(source.x + t1 * (target.x - source.x), source.y
					+ t1 * (target.y - source.y));

			points.add(p1);
		}

		if ((t2 >= 0.0) && (t2 <= 1.0))
		{
			p2 = new Point2(source.x + t2 * (target.x - source.x), source.y
					+ t2 * (target.y - source.y));

			points.add(p2);
		}

		/*
		double		radicant;

		if ( input_line.isVertical() )
		{   
		radicant =   ( radius * radius ) 
		    - (source.x - centre.x) * (source.x - centre.x); 
		if (radicant == 0) 
		{
		   _addInside( input_line, new Point2( source.x, centre.y ), points );
		} 
		else if ( radicant > 0 )
		{
		  float  radicant_sqrt   = ( float ) Math.sqrt( radicant );
		  Point2  p1	= new Point2( source.x, centre.y + radicant_sqrt );
		  Point2  p2	= new Point2( source.x, centre.y - radicant_sqrt );
		  _addInside( input_line, p1, points);
		  _addInside( input_line, p2, points);
		} // if
		}
		else
		{
		double	m =  (target.y - source.y) / (target.x - source.x); 
		double	b =    (source.y * target.x - source.x * target.y) 
		     / (target.x - source.x);

		radicant =   ( radius * radius * ( 1 + ( m * m ) ) ) - ( b * b )
		    - ( centre.y * centre.y ) - ( centre.x * centre.x * m * m )
		    + 2 * (  b * centre.y - b * centre.x * m 
			   + centre.x * centre.y * m);

		if ( radicant >= 0 )
		{
		  double	real_part =   (centre.x - b * m + centre.y * m) 
		  			    / (1 + m * m);

		  if (radicant == 0) 
		  {   
		     Point2   p   = new Point2( real_part, m * real_part + b );
		     _addInside( input_line, p, points );
		  }
		  else
		  {
		     double	delta	= Math.sqrt( radicant ) / ( 1 + ( m * m ) );
		     double	x1	= real_part + delta;
		     double	x2	= real_part - delta;

		     _addInside( input_line, new Point2( x1, m * x1 + b ), points );
		     _addInside( input_line, new Point2( x2, m * x2 + b ), points );
		  }  // if
		} // if
		}  // if
		*/

		if (points.empty())
			inout_intersection.set();
		else if (points.length() == 1)
			inout_intersection.set((Point2) points.firstValue());
		else
			inout_intersection.set(points);

		return (inout_intersection.point2);

	} // intersection


	// ************************************************************************

	/**
	 * Testet ob der <b>Umfang</b> beziehungsweise der Kreisbogen die Linie
	 * schneidet.
	 * 
	 * @param input_line
	 *            Die zu testende Linie
	 * 
	 * @return true wenn ja, sonst false
	 */
	public boolean intersects(
			BasicLine2 input_line)

	{
		Intersection set = new Intersection();

		intersection(input_line, set);

		return (set.result != Intersection.EMPTY);

	} // intersects


	// ************************************************************************

	/**
	 * Testet ob die <b>Kreisfläche</b> beziehungsweise der Kreisbogen das
	 * Rechteck schneidet.
	 * 
	 * @param input_box
	 *            das zu testende Rechteck
	 * 
	 * @return true wenn ja, sonst false
	 */
	abstract public boolean intersects(
			Rectangle2D input_box);


	// ************************************************************************

	/**
	 * Berechnet den Umfang beziehungsweise die Länge des Kreisbogens.
	 */
	abstract public double len();


	// ************************************************************************

	/**
	 * Testet ob der Punkt auf dem Umfang beziehungsweise auf dem Kreisbogen
	 * liegt.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return true wenn ja, sonst false
	 */
	abstract public boolean liesOn(
			Point2 input_point);


	// ************************************************************************

	/**
	 * Berechnet die Anzahl Ecken, mit der ein regelmäßiges Polygon den Kreis
	 * hinreichend genau für eine runde zeichnerische Darstellung approximiert.
	 * 
	 * @param g
	 *            Das Grafikobjekt
	 * 
	 * @return Die Anzahl der Ecken
	 */
	protected int polygonEdgeNumber(
			Graphics2D g)
	{
		int output_number = 20;

		/* REMARK: Point2D.Float is more appropriate 
		 * here than Point2
		 */

		Point2D.Float zero = new Point2D.Float(0, 0);
		Point2D.Float one = new Point2D.Float(1, 1);

		try
		{
			g.getTransform().inverseTransform(zero, zero);
			g.getTransform().inverseTransform(one, one);

			float delta_pixel = Math.min(Math.abs(one.x - zero.x),
					Math.abs(one.y - zero.y));

			// Formal nach "Computer Graphics Software Construction" von
			// John R. Rankin, S. 73

			output_number = Math.round((float) (2 * Math.PI / (Math
					.asin(delta_pixel / radius))));
		}
		catch (NoninvertibleTransformException ex)
		{} // try

		return (output_number);

	} // polygonEdgeNumber


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repräsentation.
	 * 
	 * @return Die textuelle Repräsentation
	 */
	abstract public String toString();


	// ************************************************************************

	/**
	 * Verschiebung um die Eingabewerte.
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
		centre.translate(input_horizontal, input_vertical);

	} // translate


	// ********************************

	/**
	 * Verschiebung um den Vektor vom Nullpunkt zum Eingabepunkt.
	 * 
	 * @param input_point
	 *            der Eingabepunkt
	 */
	public void translate(
			Point2 input_point)
	{
		centre.translate(input_point);

	} // translate


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Hängt den Eingabepunkt an die Liste wenn er auf der Eingabelinie liegt.
	 * 
	 * @param input_line
	 *            Die Eingabelinie
	 * @param input_point
	 *            Der Eingabepunkt
	 * @param inout_points
	 *            Die Liste
	 */
	private void _addInside(
			BasicLine2 input_line,
			Point2 input_point,
			List inout_points)
	{
		if (input_line.inspectCollinearPoint(input_point) == Point2.LIES_ON)
		{
			inout_points.add(input_point);
		} // if

	} // _addInside


	// ************************************************************************

	/**
	 * Berechnet den Schnittpunkt mit dem Eingabekreis, einer der Kreise liegt
	 * im inneren des anderen.
	 * 
	 * @param input_basic
	 *            Der Eingabekreis
	 * @param input_distance
	 *            Die Distanz
	 * 
	 * @return Der entsprechende Schnittpunkt
	 */
	private InspectBCResult _innerKissPoint(
			BasicCircle2 input_basic,
			double input_distance)
	{
		double x;
		double y;
		double r_div_d;

		if (radius > input_basic.radius)
		{
			r_div_d = radius / input_distance;
			x = centre.x + r_div_d * (input_basic.centre.x - centre.x);
			y = centre.y + r_div_d * (input_basic.centre.y - centre.y);
		}
		else
		{
			r_div_d = input_basic.radius / input_distance;
			x = input_basic.centre.x + r_div_d
					* (centre.x - input_basic.centre.x);
			y = input_basic.centre.y + r_div_d
					* (centre.y - input_basic.centre.y);
		} // if

		InspectBCResult result = new InspectBCResult();

		result.points = new Point2[1];
		result.points[0] = new Point2(x, y);

		return (result);

	} // _innerKissPoint


	// ************************************************************************

	/**
	 * Testet ob der Abstand der Mittelpunkte ungefähr gleich der Summe der
	 * Radien ist (verwendet eine Epsilon-Umgebung).
	 * 
	 * @param input_basic
	 *            Der Eingabekreis
	 * 
	 * @return true wenn ja, sonst false
	 */
	boolean _outerEpsilonKiss(
			BasicCircle2 input_basic)
	{
		double dx = input_basic.centre.x - centre.x;
		double dy = input_basic.centre.y - centre.y;
		double amount = radius + input_basic.radius;
		double square_distance = (dx * dx) + (dy * dy);
		double square_amount = amount * amount;
		double bound_dx = Math.abs(input_basic.centre.x) + Math.abs(centre.x);
		double bound_dy = Math.abs(input_basic.centre.y) + Math.abs(centre.y);
		double bound_distance = (bound_dx * bound_dx) + (bound_dy * bound_dy);
		double bound_amount = square_amount;

		return (Math.abs(square_distance - square_amount) <= (bound_distance + bound_amount)
				* _EPSILON);

	} // _outerEpsilonKiss


	// ************************************************************************

	/**
	 * Testet ob der Abstand der Mittelpunkte ungefähr gleich dem Betrag der
	 * Differenz der Radien ist (verwendet eine Epsilon-Umgebung).
	 * 
	 * @param input_basic
	 *            Der Eingabekreis
	 * 
	 * @return true wenn ja, sonst false
	 */
	boolean _innerEpsilonKiss(
			BasicCircle2 input_basic)
	{
		double dx = input_basic.centre.x - centre.x;
		double dy = input_basic.centre.y - centre.y;
		double square_distance = (dx * dx) + (dy * dy);
		double delta = radius - input_basic.radius;
		double square_delta = delta * delta;
		double bound_dx = Math.abs(input_basic.centre.x) + Math.abs(centre.x);
		double bound_dy = Math.abs(input_basic.centre.y) + Math.abs(centre.y);
		double bound_distance_s = (bound_dx * bound_dx) + (bound_dy * bound_dy);
		double bound_delta = Math.abs(radius) + Math.abs(input_basic.radius);
		double bound_delta_s = bound_delta * bound_delta;

		return (Math.abs(square_distance - square_delta) <= (bound_distance_s + bound_delta_s)
				* _EPSILON);

	} // _innerEpsilonKiss


	// ************************************************************************

	/**
	 * Berechnet den Schnittpunkt mit dem Eingabekreis, keiner der beiden Kreise
	 * liegt im anderen.
	 * 
	 * @param input_basic
	 *            Der Eingabekreis
	 * @param input_distance
	 *            Die Distanz
	 * 
	 * @return Der entsprechende Schnittpunkt
	 * 
	 */
	private InspectBCResult _outerKissPoint(
			BasicCircle2 input_basic,
			double input_distance)
	{
		// Seien (x1, y1) der Mittelpunkt und r1 der Radius des einen Kreises,
		// (x2, y2) der Mittelpunkt und r2 der Radius des anderen Kreises und
		// d = r1 + r2. Dann gilt für den Schnittpunkt (x, y):
		//
		//          r1
		// x = x1 + -- * ( x2 - x1 )
		//          d
		//
		//          r1
		// y = y1 + -- * ( y2 - y1 )
		//          d

		InspectBCResult result = new InspectBCResult();

		result.points = new Point2[1];

		if (radius == 0)
		{
			result.points[0] = new Point2(centre);
		}
		else if (input_basic.radius == 0)
		{
			result.points[0] = new Point2(input_basic.centre);
		}
		else
		{
			double r1_div_d = radius / input_distance;

			double x = centre.x + r1_div_d * (input_basic.centre.x - centre.x);
			double y = centre.y + r1_div_d * (input_basic.centre.y - centre.y);

			result.points[0] = new Point2(x, y);
		} // if

		return (result);

	} // _outerKissPoint


	// ************************************************************************

	/**
	 * Berechnet die Schnittpunkte mit dem Eingabekreis. Sei der erste Kreis
	 * definiert durch x1, y1, r1 und der zweite durch x2, y2, r2. Dann kann man
	 * aus den Kreisgleichungen ableiten:
	 * 
	 * y = m * x + b
	 * 
	 * x2 - x1 mit m = - ------- y2 - y1
	 * 
	 * ( r1² - r2² ) + ( x2² - x1² ) + ( y2² - y1² ) und b =
	 * --------------------------------------------- 2 * ( y2 - y1 )
	 * 
	 * x = f ± sqrt( g + f² )
	 * 
	 * ( m * ( y1 - b ) + x1 ) mit f = ----------------------- 1 + m²
	 * 
	 * r1² - x1² - y1² + b * ( 2 * y1 - b ) und g =
	 * ------------------------------------ 1 + m²
	 * 
	 * @param input_basic
	 *            Der Eingabekreis
	 * 
	 * @return Die Schnittpunkte
	 */
	private InspectBCResult _intersectionPoints(
			BasicCircle2 input_basic)
	{
		if (centre.y == input_basic.centre.y)
			return (_intersectionHorizontal(input_basic));

		double dx = input_basic.centre.x - centre.x;
		double dy = input_basic.centre.y - centre.y;
		double x1_square = centre.x * centre.x;
		double y1_square = centre.y * centre.y;
		double x2_square = input_basic.centre.x * input_basic.centre.x;
		double y2_square = input_basic.centre.y * input_basic.centre.y;
		double r1_square = radius * radius;
		double r2_square = input_basic.radius * input_basic.radius;

		double m = -dx / dy;
		double b = (r1_square - r2_square + x2_square - x1_square + y2_square - y1_square)
				/ 2 / dy;

		double denum = 1 + (m * m);
		double f = (m * (centre.y - b) + centre.x) / denum;
		double g = (r1_square - x1_square - y1_square + b * (2 * centre.y - b))
				/ denum;
		double root = Math.sqrt(g + (f * f));

		InspectBCResult result = new InspectBCResult();

		if (root == 0)
		{
			result.points = new Point2[1];
			result.points[0] = new Point2(f, (m * f) + b);
		}
		else
		{
			result.points = new Point2[2];
			double p1x = f + root;
			double p2x = f - root;
			result.points[0] = new Point2(p1x, (m * p1x) + b);
			result.points[1] = new Point2(p2x, (m * p2x) + b);
		} // if

		return (result);

	} // _intersectionPoints


	// ************************************************************************

	/**
	 * Berechnet die Schnittpunkte mit dem Eingabekreis, dessen Mittelpunkt die
	 * gleiche y-Koordinate hat. Sei der erste Kreis definiert durch x1, y1, r1
	 * und der zweite durch x2, y2, r2. Dann folgt aus den Kreisgleichungen:
	 * 
	 * ( r1² - r2² ) + ( x2² - x1² ) x = ----------------------------- 2 * ( x2
	 * * * - x1 )
	 * 
	 * y = y1 ± sqrt( r1² - ( x - x1 )² )
	 * 
	 * @param input_basic
	 *            Der Eingabekreis
	 * 
	 * @return Die Schnittpunkte
	 */
	private InspectBCResult _intersectionHorizontal(
			BasicCircle2 input_basic)
	{
		double dx = input_basic.centre.x - centre.x;
		double x1_square = centre.x * centre.x;
		double x2_square = input_basic.centre.x * input_basic.centre.x;
		double r1_square = radius * radius;
		double r2_square = input_basic.radius * input_basic.radius;

		double x = (r1_square - r2_square + x2_square - x1_square) / 2 / dx;
		double l = x - centre.x;
		double root = Math.sqrt(r1_square - (l * l));

		InspectBCResult result = new InspectBCResult();

		if (root == 0)
		{
			result.points = new Point2[1];
			result.points[0] = new Point2(x, centre.y);
		}
		else
		{
			result.points = new Point2[2];
			result.points[0] = new Point2(x, centre.y + root);
			result.points[1] = new Point2(x, centre.y - root);
		} // if

		return (result);

	} // _intersectionHorizontal

	// ************************************************************************

} // class BasicCircle2

