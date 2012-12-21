package anja.geom;


import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;

import anja.util.Angle;
import anja.util.FloatUtil;
import anja.util.GraphicsContext;
import anja.util.List;
import anja.util.SimpleList;


/**
 * Zweidimensionaler zeichenbarer Kreis.
 * 
 * <b>Modifications </b> <br>17.01.2005 All drawing code now uses an
 * {@link java.awt.geom.Ellipse2D} primitive instead of calls to
 * {@link java.awt.Graphics#drawOval(int,int,int,int)}. This allows drawing in
 * floating point coordinates (finally!) and improves precision as well.
 * 
 * @version 1.1 08.09.1997
 * @author Norbert Selle
 * 
 * 
 */

public class Circle2
		extends BasicCircle2
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	/** Epsilon-Umgebung */
	private static final double	_EPSILON	= 5e-8d;

	// ************************************************************************
	// Variables
	// ************************************************************************

	private GraphicsContext		_last_gc;
	private Ellipse2D.Float		_geometry;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	// Man beachte, dass sich nur ein Teil der Konstruktoren auf
	// Circle2( Point2, float )
	// abstuetzt.

	// ********************************              

	/**
	 * Erzeugt einen Kreis mit dem Mittelpunkt (0, 0) und dem Radius 0.
	 */
	public Circle2()
	{
		this(new Point2(0, 0), 0);

		_last_gc = new GraphicsContext();
		_geometry = new Ellipse2D.Float(0, 0, 0, 0);

	} // Circle2


	// ********************************              

	/**
	 * Erzeugt einen Kreis mit den Daten des Eingabekreises.
	 * 
	 * @param input_circle
	 *            The circle to copy
	 */
	public Circle2(
			Circle2 input_circle)
	{
		this(new Point2(input_circle.centre), input_circle.radius);
		_last_gc = new GraphicsContext();

	} // Circle2


	// ********************************              

	/**
	 * Erzeugt einen Kreis mit den Mittelpunkt und Radius Parametern, der Kreis
	 * merkt sich eine <b> Referenz </b> auf den Mittelpunkt.
	 * 
	 * @param input_centre
	 *            Mittelpunkt
	 * @param input_radius
	 *            Radius
	 */
	public Circle2(
			Point2 input_centre,
			float input_radius)
	{
		super(input_centre, input_radius);
		_last_gc = new GraphicsContext();

		_geometry = new Ellipse2D.Float(0, 0, 0, 0);

	} // Circle2


	// ********************************              

	/**
	 * Erzeugt einen Kreis mit den Mittelpunkt und Radius Parametern.
	 * 
	 * @param input_centre_x
	 *            Mittelpunkt x-Koordinate
	 * @param input_centre_y
	 *            Mittelpunkt y-Koordinate
	 * @param input_radius
	 *            Radius
	 */
	public Circle2(
			float input_centre_x,
			float input_centre_y,
			float input_radius)
	{
		this(new Point2(input_centre_x, input_centre_y), input_radius);
		_last_gc = new GraphicsContext();

	} // Circle2


	// ************************************************************************

	/**
	 * Erzeugt einen Kreis aus den Endpunkten eines Durchmessers, der Kreis
	 * merkt sich <b>keine Referenzen</b> auf die Endpunkte.
	 * 
	 * @param input_diameter1
	 *            ein Durchmesserendpunkt
	 * @param input_diameter2
	 *            der andere Durchmesserendpunkt
	 */
	public Circle2(
			Point2 input_diameter1,
			Point2 input_diameter2)
	{
		double x1 = input_diameter1.x;
		double y1 = input_diameter1.y;
		double x2 = input_diameter2.x;
		double y2 = input_diameter2.y;
		double dx = x2 - x1;
		double dy = y2 - y1;

		centre = new Point2((x1 + x2) / 2, (y1 + y2) / 2);
		radius = (float) Math.sqrt((dx * dx) + (dy * dy)) / 2;

		_last_gc = new GraphicsContext();
		_geometry = new Ellipse2D.Float(0, 0, 0, 0);

	} // Circle2


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
	public Object clone()
	{
		return (new Circle2(this));

	} // clone


	// ************************************************************************

	/**
	 * Gibt den Durchmesser zurueck.
	 * 
	 * @return The diameter
	 */
	public float diameter()
	{
		return (radius + radius);

	} // diameter


	// ************************************************************************

	/**
	 * Zeichnet den Kreis. Inherited from Drawable.
	 * 
	 * @param g
	 *            The graphics object
	 * @param gc
	 *            The graphics context
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{

		float x = centre.x - radius;
		float y = centre.y - radius;
		float width = 2 * radius;
		float height = width;

		_geometry.setFrame(x, y, width, height);

		// save last rendering attributes

		_last_gc.setBackgroundColor(g.getBackground());
		_last_gc.setForegroundColor(g.getColor()); // is this correct ?
		_last_gc.setFillColor(g.getColor());

		BasicStroke last_stroke = (BasicStroke) g.getStroke();

		g.setStroke(gc.getStroke());

		if (gc.getFillStyle() != 0)
		{
			g.setColor(gc.getFillColor());
			g.fill(_geometry);

			if (gc.getFillColor() != gc.getForegroundColor())
			{
				g.setColor(gc.getForegroundColor());
				g.draw(_geometry);
			} // if
		}
		else
		{
			g.setColor(gc.getForegroundColor());
			g.draw(_geometry);
		}

		// restore graphics attributes

		g.setBackground(_last_gc.getBackgroundColor());
		g.setColor(_last_gc.getFillColor());
		g.setStroke(last_stroke);

		/* !!NS
		// !!NS ist noch nicht optimiert
		BezierPath        path    = new BezierPath( BezierPath.NON_ZERO );
		int               n       = _polygonEdgeNumber( g );
		float             x[]     = new float[ n ];
		float             y[]     = new float[ n ];

		double            delta_angle = 2 * Math.PI / n;

		for ( int i = 0; i < n; i++ )
		{
		x[ i ] = ( float ) ( radius * Math.cos( i * delta_angle ) );
		y[ i ] = ( float ) ( radius * Math.sin( i * delta_angle ) );
		} // for

		path.moveTo( centre.x + x[ 0 ], centre.y + y[ 0 ] );
		for ( int i = 1; i < n; i++ )
		{
		path.lineTo( centre.x + x[ i ], centre.y + y[ i ] );
		} // for
		path.closePath();
		  
		g.setColor(  gc.getForegroundColor()      );
		g.setStroke( gc.getStroke()               );

		g.drawPath( path );
		!!NS */

		g.setStroke(last_stroke);

	} // draw


	// ************************************************************************

	/**
	 * Gibt das umschliessende Rechteck zurueck.
	 * 
	 * @return The Rectangle, that surrounds the circle
	 */
	public Rectangle2D getBoundingRect()
	{
		return (new Rectangle2D.Float(centre.x - radius, // x
				centre.y - radius, // y
				diameter(), // width
				diameter()) // height
		);

	} // getBoundingRect


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge der Kreisumfaenge. Der Rueckgabeparameter
	 * Intersection kann leer sein oder einen Punkt, eine Kopie von this (wenn
	 * die Kreise aufeinanderliegen) oder eine Liste von zwei Punkten enthalten.
	 * Ist es ein Punkt, so ist er ausserdem das Funktionsergebnis, sonst ist
	 * das Funktionsergebnis null.
	 * 
	 * @param input_circle
	 *            The circle
	 * @param inout_intersection
	 *            The intersection object
	 * 
	 * @return A point, if there is a point intersection, null else
	 */
	public Point2 intersection(
			Circle2 input_circle,
			Intersection inout_intersection)
	{
		InspectBCResult inspect = inspectBasicCircle(input_circle);

		if (inspect == null)
		{
			inout_intersection.set();
		}
		else if (inspect.lies_on)
		{
			inout_intersection.set((Circle2) this.clone());
		}
		else if (inspect.points.length == 1)
		{
			inout_intersection.set(inspect.points[0]);
		}
		else if (inspect.points.length == 2)
		{
			List list = new List();
			list.add(inspect.points[0]);
			list.add(inspect.points[1]);
			inout_intersection.set(list);
		} // if

		return (inout_intersection.point2);

	} // intersection


	/**
	 * Berechnet den Schnitt des Objektes und einem Liniensegment
	 * 
	 * Als Kreis wird das aufgerufene Objekt verwendet, die Linie wird als
	 * Parameter übergeben
	 * 
	 * @param bl
	 *            Das BasicLine2-Objekt, das auf Schnitt mit dem Kreis getestet
	 *            werden soll.
	 * 
	 * @return Ein Intersection-Objekt. Da es zu 0, 1 oder 2 Schnitten kommen
	 *         kann, werden diese dort im @see{anja.util.SimpleList}-Objekt
	 *         gespeichert.
	 */
	public Intersection intersection(
			BasicLine2 bl)
	{
		return intersection(this, bl);

	} // intersection


	/**
	 * Berechnet den Schnitt eines Kreises und eines Liniensegments
	 * 
	 * @param input_circle
	 *            Der Kreis
	 * @param bl
	 *            Das Liniensegment (alle von BasicLine2 vererbten Objekte:
	 *            Line2, Ray2, Segment2)
	 * 
	 * @return Ein Intersection-Objekt. Da es zu 0, 1 oder 2 Schnitten kommen
	 *         kann, werden diese dort im @see{anja.util.SimpleList}-Objekt
	 *         gespeichert.
	 */
	public static Intersection intersection(
			Circle2 input_circle,
			BasicLine2 bl)
	{
		//Die Genauigkeit der Rechnung haengt von eps ab
		float eps = anja.util.MathConstants.FLT_EPSILON;

		//Idee: Berechne zuerst den Schnittpunkt mit Kreis und Gerade
		//Ueberpruefe dann, ob ein Strahl oder ein Segment das Ergebnis einschraenken.

		Intersection inter = new Intersection();

		//Die potenziellen Schnittpunkte
		Point2 schnitt1 = null;
		Point2 schnitt2 = null;

		//Zuerst muessen wir testen, ob das Segment senkrecht ist
		if (Math.abs(bl._target.x - bl._source.x) < eps)
		{

			//Nun koennen wir annehmen, dass wir eine Senkrechte vorliegen haben.
			//Alle Schnittpunkte haben somit x-Wert bl._source.x
			float distance_m_p = input_circle.centre.x - bl._source.x;

			if (Math.abs(distance_m_p) < eps)
			{
				//Eine Tangente
				schnitt1 = new Point2(bl._source.x, input_circle.centre.y);
			}
			else if (Math.abs(distance_m_p) > input_circle.radius)
			{
				//Kein Schnitt
			}

			else

			{
				//2 Schnitte mit Pythagoras
				float delta_y = (float) Math.sqrt(Math.pow(input_circle.radius,
						2)
						- Math.pow(distance_m_p, 2));

				schnitt1 = new Point2(bl._source.x, input_circle.centre.y
						+ delta_y);
				schnitt2 = new Point2(bl._source.x, input_circle.centre.y
						- delta_y);
			}
		} //end:if end:segment senkrecht

		else
		{
			//Unser Segment ist nicht senkrecht. Die Steigung ist != 0
			float gradient = (bl._target.y - bl._source.y)
					/ (bl._target.x - bl._source.x);

			//Zur Verkuerzung der Formeln, fuege ich hier einige Variablen ein
			//Der Kreismittelpunkt
			float m_x = input_circle.centre.x;
			float m_y = input_circle.centre.y;

			//Der Aufpunkt der Gerade
			float p_x = bl._source.x;
			float p_y = bl._source.y;

			//Die Rechnung ist in mehrere Teile geteilt. Sie basiert darauf, dass die Gerade in die Kreisgleichung eingesetzt wurde
			float helper_a = gradient * gradient + 1;
			float helper_b = gradient * (p_y - m_y) - gradient * gradient * p_x
					- m_x;
			float helper_c = m_x * m_x + gradient * gradient * p_x * p_x - 2
					* gradient * p_x * (p_y - m_y) + (p_y - m_y) * (p_y - m_y);
			float helper_d = input_circle.radius * input_circle.radius
					- helper_c + helper_b * helper_b / helper_a;
			helper_d /= helper_a;

			float helper_shift = -helper_b / helper_a;

			if (helper_d < -eps)
			{
				//keine Lösung

			}
			else if (helper_d < eps)
			{

				//eine Loesung
				float s_x = helper_shift;
				float s_y = gradient * (s_x - p_x) + p_y;

				schnitt1 = new Point2(s_x, s_y);
			}
			else
			{

				//2 Schnitte
				float s_x_1 = (float) (Math.sqrt(helper_d) + helper_shift);
				float s_x_2 = (float) (-1 * Math.sqrt(helper_d) + helper_shift);

				float s_y_1 = gradient * (s_x_1 - p_x) + p_y;
				float s_y_2 = gradient * (s_x_2 - p_x) + p_y;

				schnitt1 = new Point2(s_x_1, s_y_1);
				schnitt2 = new Point2(s_x_2, s_y_2);
			}
		} // end:if end:beliebige Steigung

		SimpleList temp_list = new SimpleList();

		if (bl instanceof Segment2)
		{
			if (schnitt1 != null && ((Segment2) bl).liesOn(schnitt1))
			{
				temp_list.add(schnitt1);
			}
			if (schnitt2 != null && ((Segment2) bl).liesOn(schnitt2))
			{
				temp_list.add(schnitt2);
			}
		}

		if (bl instanceof Ray2)
		{
			if (schnitt1 != null && ((Ray2) bl).liesOn(schnitt1))
			{
				temp_list.add(schnitt1);
			}
			if (schnitt2 != null && ((Ray2) bl).liesOn(schnitt2))
			{
				temp_list.add(schnitt2);
			}
		}

		if (bl instanceof Line2)
		{
			//Bei Line2 ist es durchgehend. Somit reicht es aus, dass der Schnittpunkt auf der Linie entdeckt wurde.
			if (schnitt1 != null)
			{
				temp_list.add(schnitt1);
			}
			if (schnitt2 != null)
			{
				temp_list.add(schnitt2);
			}
		}

		inter.setList(temp_list);

		return inter;

	} // intersection


	// ************************************************************************

	/**
	 * Testet ob der Kreis das Rechteck schneidet.
	 * 
	 * @param input_box
	 *            Das zu testende Rechteck
	 * 
	 * @return true wenn ja, sonst false
	 */
	public boolean intersects(
			Rectangle2D input_box)
	{
		if (input_box.contains(centre.x, centre.y))
		{
			return (true); // Mittelpunkt in input_box
		} // if

		// Changed if-statement, please consider the note
		// at anja.geom.Rectangle2.intersectsOrTouches() !
		Rectangle2 rect = new Rectangle2(getBoundingRect());
		if (!rect.intersectsOrTouches(input_box))
		{
			return (false); // Das kreisumschliessende Rechteck
			// schneidet input_box nicht
		} // if

		// das kreisumschliessende Rechteck schneidet input_box, deshalb
		// wird getestet ob auch der Kreis input_box schneidet,
		// Bedingung: die Distanz mind. einer input_box-Kante zu centre
		//	    ist kleiner gleich dem Radius

		float radius_square = radius * radius;
		Rectangle2 box = new Rectangle2(input_box);

		return ((box.top().squareDistance(centre) <= radius_square)
				|| (box.bottom().squareDistance(centre) <= radius_square)
				|| (box.left().squareDistance(centre) <= radius_square) || (box
				.right().squareDistance(centre) <= radius_square));

	} // intersects


	// ************************************************************************

	/**
	 * Berechnet den Umfang.
	 * 
	 * @return Der Umfang des Kreises
	 */
	public double len()
	{
		return (Math.abs(radius * Angle.PI_MUL_2));

	} // len


	// ************************************************************************

	/**
	 * Testet ob der Punkt auf dem Kreisumfang liegt. Es wird mit einer
	 * Epsilon-Umgebung gearbeitet.
	 * 
	 * @param input_point
	 *            The point to check
	 * 
	 * @return true wenn ja, sonst false
	 */
	public boolean liesOn(
			Point2 input_point)
	{
		double dx = input_point.x - centre.x;
		double dy = input_point.y - centre.y;
		double square_distance = (dx * dx) + (dy * dy);
		double square_radius = radius * radius;

		double bound_dx = Math.abs(input_point.x) + Math.abs(centre.x);
		double bound_dy = Math.abs(input_point.y) + Math.abs(centre.y);
		double bound_distance = (bound_dx * bound_dx) + (bound_dy * bound_dy);
		double bound_radius = square_radius;

		return (Math.abs(square_distance - square_radius) <= _EPSILON
				* (bound_distance + bound_radius));

	} // liesOn


	/**
	 * Tests whether a given point lies inside the circle.
	 * 
	 * This test is strict, that is, it does not include the circle itself but
	 * only its inner area.
	 * 
	 * @param point
	 *            point to be tested
	 * 
	 * @return <code>true</code> if the point is inside, otherwise
	 *         <code>false</code>.
	 */
	public boolean pointInCircle(
			Point2 point)
	{
		float x_quad = (point.x - centre.x) * (point.x - centre.x);
		float y_quad = (point.y - centre.y) * (point.y - centre.y);

		float square_distance = x_quad + y_quad;
		float square_radius = radius * radius;

		/* I think a strict inequality test with floats is safe */
		if (square_distance < square_radius)
		{
			return true;
		}

		return false;
	}


	/**
	 * Convenience method, does the same as pointInCircle(Point2 point), but
	 * takes the individual double-precision coordinates as arguments. This is
	 * one of the provisional interface points between anja.geom and
	 * anja.newgraph packages. One additional 'feature' here is that the
	 * arguments are double, and are internally cast to float to facilitate
	 * interfacing the newgraph code(which is based completely on
	 * double-precision coordinates). Thus, this method should be used with some
	 * caution because of inherent loss of precision!
	 * 
	 * @param x
	 *            x-coordinate of the point
	 * @param y
	 *            y-coordinate of the point
	 * 
	 * @return true, if the point is in the circle, false else
	 */
	public boolean pointInCircle(
			double x,
			double y)
	{
		float x_quad = (float) ((x - centre.x) * (x - centre.x));
		float y_quad = (float) ((y - centre.y) * (y - centre.y));

		float square_distance = x_quad + y_quad;
		float square_radius = radius * radius;

		/* I think a strict inequality test with floats is safe */
		if (square_distance < square_radius)
		{
			return true;
		}

		return false;
	}


	/**
	 *FIXME: This one doesn't work for some reason!
	 * 
	 * @param point
	 *            The point to check
	 * 
	 * @return true, if the point is on the circle, false else
	 */
	public boolean pointOnCircle(
			Point2 point)
	{
		/*
		double distance = point.distance(centre);
		
		if( Math.abs(distance - radius) < 0.001 * radius )
		{
			return true;
		}
		return false;*/

		float x_quad = (point.x - centre.x) * (point.x - centre.x);
		float y_quad = (point.y - centre.y) * (point.y - centre.y);

		float square_distance = x_quad + y_quad;
		float square_radius = radius * radius;

		//if(square_distance == square_radius)
		if (Math.abs(square_distance - square_radius) < 0.033 * radius)
		{
			return true;
		}

		return false;
	}


	public boolean pointOnOrInCircle(
			Point2 point)
	{
		return pointInCircle(point) | pointOnCircle(point);
	}


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation.
	 * 
	 * @return die textuelle Repraesentation
	 */
	public String toString()
	{
		return (new String("(" + centre + ", "
				+ FloatUtil.floatToString(radius) + ")"));

	} // toString

	// ************************************************************************
	// Private methods
	// ************************************************************************

} // class Circle2

