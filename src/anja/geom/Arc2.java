package anja.geom;


/*
 * import java_ersatz.java2d.Graphics2D; import java_ersatz.java2d.Rectangle2D;
 * import java_ersatz.java2d.Transform;
 */

// import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Arc2D;

import anja.util.Angle;
import anja.util.FloatUtil;
import anja.util.GraphicsContext;
import anja.util.LimitedAngle;
import anja.util.List;


/**
 * Arc2 ist ein zweidimensionaler zeichenbarer Kreisbogen, der von seinem
 * Startwinkel in die Richtung <tt>Angle.ORIENTATION_LEFT</tt> (entgegen dem
 * Uhrzeigersinn) oder <tt>Angle.ORIENTATION_RIGHT</tt> (im Uhrzeigersinn) zu
 * seinem Endwinkel gerichtet ist.<br>
 * 
 * Der Wertebereich der Winkel reicht von 0 bis einschliesslich zwei Pi in
 * Bogenmass, ein Kreisbogen ueber den vollen Wertebereich ist ein Vollkreis.
 * <br> <tt>sourceAngle()</tt> und <tt>targetAngle()</tt> liefern den Start- und
 * Endwinkel in Bogenmass, mit <tt>sourceTrig()</tt> und <tt> targetTrig()</tt>
 * koennen ihre trigonometrische Daten wie Sinus etc. <b>effizient</b> abgefragt
 * werden, und <tt>source()</tt> und <tt>target()</tt> liefern den Start- und
 * Endpunkt des Kreisbogens.<br>
 * 
 * Die Methode <tt>liesOn()</tt> arbeitet mit einer <b>Epsilon-Umgebung</b>.
 * <br>
 * 
 * Man beachte, dass die zeichnerische Darstellung eines Kreisbogens im
 * Vergleich zum ueblichen mathematischen Koordinatensystem auf dem Kopf steht,
 * da im anja.gui die y-Koordinate vom oberen zum unteren Fensterrand hin
 * groesser wird. Um eine dem ueblichen Koordinatensystem entsprechende
 * Darstellung zu erhalten, muss die Transformation von Welt- zu
 * Geraetekoordinaten eine Spiegelung um die x-Achse enthalten.
 * 
 * @version 1.3 08.09.1997
 * @author Norbert Selle
 * 
 * @see anja.util.Angle
 * @see anja.util.LimitedAngle
 * @see anja.util.Angle#ORIENTATION_LEFT
 * @see anja.util.Angle#ORIENTATION_RIGHT
 */

public class Arc2
		extends BasicCircle2
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	/** Epsilon-Umgebung */
	private static double	_EPSILON	= 5e-8d;

	/*
	 * final entfernt, Getter und Setter eingefügt um _EPSILON konfigurierbar zu
	 * machen, fuehrt manchmal zu Problemen und muss angepasst werden.
	 */

	// ************************************************************************
	// Variables
	// ************************************************************************
	/** Orientierung */
	private int				_orientation;

	/** Startwinkel */
	private LimitedAngle	_source_angle;

	/** Endwinkel */
	private LimitedAngle	_target_angle;

	/** Minimale und maximale Faktoren fuer getBoundingRect */
	private double			_limit[]	= { 1, // limit max y
			-1, // limit min x
			-1, // limit min y
			1							};			// limit max x

	/** An arbitrary string to label the arc */
	protected String		_label		= null;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt einen Kreisbogen mit dem Mittelpunkt (0, 0), dem Radius 0, dem
	 * Start- und Endwinkel 0 und der Orientierung <tt>
	 * Angle.ORIENTATION_LEFT</tt>.
	 * 
	 * @see anja.util.Angle#ORIENTATION_LEFT
	 */
	public Arc2()
	{
		this(new Point2(0, 0), // centre
				0, // radius
				0, // source angle
				0, // target angle
				Angle.ORIENTATION_LEFT);

	} // Arc2


	// ********************************

	/**
	 * Erzeugt eine Kopie des eingegebenen Kreisbogens.
	 * 
	 * @param input_arc
	 *            Das zu korierende Objekt
	 */
	public Arc2(
			Arc2 input_arc)
	{
		this(new Point2(input_arc.centre), input_arc.radius,
				input_arc._source_angle.rad(), input_arc._target_angle.rad(),
				input_arc._orientation);

	} // Arc2


	// ********************************

	/**
	 * Erzeugt einen Kreisbogen mit den Eingabeparametern, der Kreisbogen merkt
	 * sich eine <b>Referenz</b> auf den Mittelpunkt.
	 * 
	 * @param input_centre
	 *            der Mittelpunkt
	 * @param input_radius
	 *            der Radius
	 * @param input_source_angle
	 *            der Startwinkel in Bogenmass
	 * @param input_target_angle
	 *            der Endwinkel in Bogenmass
	 * @param input_orientation
	 *            die Orientierung Angle.ORIENTATION_LEFT etc.
	 * 
	 * @see anja.util.Angle#ORIENTATION_LEFT
	 * @see anja.util.Angle#ORIENTATION_RIGHT
	 */

	public Arc2(
			Point2 input_centre,
			double input_radius,
			double input_source_angle,
			double input_target_angle,
			int input_orientation)
	{
		super(input_centre, (float) input_radius);

		_source_angle = new LimitedAngle(input_source_angle,
				LimitedAngle.CIRCLE_ABS);
		_target_angle = new LimitedAngle(input_target_angle,
				LimitedAngle.CIRCLE_ABS);
		_orientation = input_orientation;

	} // Arc2


	// ********************************

	/**
	 * Erzeugt einen Kreisbogen mit den Eingabeparametern.
	 * 
	 * @param input_centre_x
	 *            die x-Koordinate des Mittelpunkts
	 * @param input_centre_y
	 *            die y-Koordinate des Mittelpunkts
	 * @param input_radius
	 *            der Radius
	 * @param input_source_angle
	 *            der Startwinkel in Bogenmass
	 * @param input_target_angle
	 *            der Endwinkel in Bogenmass
	 * @param input_orientation
	 *            die Orientierung Angle.ORIENTATION_LEFT etc.
	 * 
	 * @see anja.util.Angle#ORIENTATION_LEFT
	 * @see anja.util.Angle#ORIENTATION_RIGHT
	 */
	public Arc2(
			float input_centre_x,
			float input_centre_y,
			double input_radius,
			double input_source_angle,
			double input_target_angle,
			int input_orientation)
	{
		this(new Point2(input_centre_x, input_centre_y), input_radius,
				input_source_angle, input_target_angle, input_orientation);

	} // Arc2


	// ********************************

	/**
	 * Erzeugt einen vom Startpunkt ueber den Zwischenpunkt zum Endpunkt
	 * gerichteten Kreisbogen, es werden <b>keine Referenzen</b> auf die Punkte
	 * gespeichert.
	 * 
	 * @param input_source
	 *            der Startpunkt des Kreisbogens
	 * @param input_inner
	 *            ein Punkt zwischen Start- und Endpunkt
	 * @param input_target
	 *            der Endpunkt des Kreisbogens
	 */
	public Arc2(
			Point2 input_source,
			Point2 input_inner,
			Point2 input_target)
	{
		float x1 = input_source.x;
		float y1 = input_source.y;
		float x2 = input_inner.x;
		float y2 = input_inner.y;
		float x3 = input_target.x;
		float y3 = input_target.y;
		double x1s = x1 * x1;
		double y1s = y1 * y1;
		double x2s = x2 * x2;
		double y2s = y2 * y2;
		double x3s = x3 * x3;
		double y3s = y3 * y3;

		double det = (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1);

		double det1 = (x2s - x1s + y2s - y1s) * (y3 - y1)
				- (x3s - x1s + y3s - y1s) * (y2 - y1);

		double det2 = (x3s - x1s + y3s - y1s) * (x2 - x1)
				- (x2s - x1s + y2s - y1s) * (x3 - x1);

		double xc = det1 / det / 2;
		double yc = det2 / det / 2;
		double r = Math.sqrt((x1 - xc) * (x1 - xc) + (y1 - yc) * (y1 - yc));
		centre = new Point2(xc, yc);
		radius = (float) r;
		_source_angle = new LimitedAngle(centre.angle(input_source),
				LimitedAngle.CIRCLE_ABS);
		_target_angle = new LimitedAngle(centre.angle(input_target),
				LimitedAngle.CIRCLE_ABS);

		_orientation = Angle.orientation(_source_angle.rad(), centre
				.angle(input_inner), _target_angle.rad());

	} // Arc2


	/**
	 * Erzeugt einen vom Startpunkt zum Endpunkt gerichteten Kreisbogen, es
	 * werden <b>keine Referenzen</b> auf die Punkte gespeichert.
	 * 
	 * @param input_source
	 *            der Startpunkt des Kreisbogens
	 * @param input_target
	 *            der Endpunkt des Kreisbogens
	 * @param input_center
	 *            der Mittelpunkt des Kreises
	 * @param input_orientation
	 *            der Endpunkt des Kreisbogens
	 * 
	 */

	public Arc2(
			Point2 input_source,
			Point2 input_target,
			Point2 input_center,
			int input_orientation)
	{
		float x1 = input_source.x;
		float y1 = input_source.y;
		float x2 = input_center.x;
		float y2 = input_center.y;

		double r = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		centre = new Point2(input_center);
		radius = (float) r;
		_source_angle = new LimitedAngle(centre.angle(input_source),
				LimitedAngle.CIRCLE_ABS);
		_target_angle = new LimitedAngle(centre.angle(input_target),
				LimitedAngle.CIRCLE_ABS);

		_orientation = input_orientation;

	} // Arc2


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
		return (new Arc2(this));
	} // clone


	// ************************************************************************

	/**
	 * Berechnet unter Beruecksichtigung der Orientierung den Winkel vom
	 * Startwinkel zum Endwinkel, das Ergebnis liegt zwischen minus zwei Pi und
	 * zwei Pi.
	 * 
	 * @return den berechneten Winkel in Bogenmass
	 */

	public double delta()
	{
		return (_source_angle.delta(_target_angle, orientation()));

	} // delta


	// ************************************************************************

	/**
	 * Test-Zeichenfunktion
	 * 
	 * Nicht benutzen!
	 * 
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gc
	 *            Die dazugehörigen Formatierungsregeln
	 * 
	 */
	public void drawAlt(
			Graphics2D g,
			GraphicsContext gc)
	{
		// convert the parameters to suitable arguments for drawArc()
		int x, y, height, width, startAngle, arcAngle;

		x = (int) (centre.x - radius);
		y = (int) (centre.y - radius);
		height = width = (int) (2.0f * radius);

		startAngle = (int) _source_angle.deg();
		arcAngle = (int) _target_angle.deg() - startAngle;

		g.setColor(gc.getForegroundColor());
		g.setStroke(gc.getStroke());

		g.drawArc(x, y, width, height, startAngle, arcAngle);

	}


	/**
	 * Zeichnet den Kreisbogen.
	 * 
	 * FIXME: This method has been hacked to use Arc2D instead of
	 * Graphics.drawArc(). The hack is not optimized and may be slow!
	 * 
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gc
	 *            Die dazugehörigen Formatierungsregeln
	 * 
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		// centre, source, inner und target in Geraetekoordinaten berechnen

		Point2 center = new Point2();
		Point2 source = new Point2();
		Point2 inner = new Point2();
		Point2 target = new Point2();

		// AffineTransform trans = g.getTransform();

		center = centre;
		source = source();
		inner = midPoint();
		target = target();

		// Startwinkel, Bogenwinkel (der evt. seine Richtung geaendert hat)
		// und Radius in Geraetekoordinaten berechnen

		double source_angle = center.angle(source);
		double inner_angle = center.angle(inner);
		double target_angle = center.angle(target);

		int orientation = Angle.orientation(source_angle, inner_angle,
				target_angle);

		double delta = Angle.delta(source_angle, target_angle, orientation);

		double radius = center.distance(source);

		// Kreisbogen zeichnen. Die Winkel muessen zu Grad und ins mathematische
		// Koordinatensystem umgerechnet werden.

		/*
		 * int x = ( int ) ( centre_dv.x - radius_dv ); int y = ( int ) (
		 * centre_dv.y - radius_dv ); int width = ( int ) ( 2 * radius_dv ); int
		 * height= width; int start = ( int ) ( 360 - Angle.radToDeg(
		 * source_angle_dv ) ); int arc = ( int ) ( 0 - Angle.radToDeg( delta_dv
		 * ) );
		 */

		// test
		double start = 360 - Angle.radToDeg(source_angle);
		double arc = 0 - Angle.radToDeg(delta);

		Arc2D arc_geom = new Arc2D.Double();

		arc_geom.setArcByCenter(center.x, center.y, radius, start, arc,
				Arc2D.OPEN);

		g.setColor(gc.getForegroundColor());
		g.setStroke(gc.getStroke());

		g.draw(arc_geom);

		/*
		 * 02.06.2004 the following code was spliced in from
		 * java_ersatz.java2d.Graphics2D, since the drawArcDV() method is not
		 * part of the standard Java2D API
		 */

		// g.drawArc( x, y, width, height, start, arc );
	} // draw


	// ************************************************************************

	/**
	 * Gibt das umschliessende Rechteck zurueck.
	 * 
	 * @return Das umschließende Rechteck
	 */
	public Rectangle2D getBoundingRect()
	{
		// Anfangswinkel des Kreisbogen entgegen des Uhrzeigersinns
		LimitedAngle alpha = _leftOrientedSource();

		// Endwinkel des Kreisbogen entgegen des Uhrzeigersinns
		LimitedAngle beta = _leftOrientedTarget();

		// Winkel von alpha nach beta, der Wert liegt zwischen 0 und 2 Pi
		double delta = alpha.delta(beta, Angle.ORIENTATION_LEFT);

		// Berechnung der Anzahl wie oft der Kreisbogen die Achsen kreuzt

		int axes_no = (int) (delta / Angle.PI_DIV_2);

		if ((alpha.rad() % Angle.PI_DIV_2) + (delta % Angle.PI_DIV_2) >= Angle.PI_DIV_2)
			axes_no++;

		// Berechnung der minimalen und maximalen x- und y-Werte

		double min_max[] = { Math.max(alpha.sin(), beta.sin()), // max y
				Math.min(alpha.cos(), beta.cos()), // min x
				Math.min(alpha.sin(), beta.sin()), // min y
				Math.max(alpha.cos(), beta.cos()) // max x
		};

		int start = (int) (alpha.rad() / Angle.PI_DIV_2);
		int current;

		for (int i = start; i < start + axes_no; i++)
		{
			// Bei einem Achsenschnittpunkt den min_max-Wert auf
			// den entsprechenden Inhalt von _limit setzen

			current = i % 4;
			min_max[current] = _limit[current];
		} // for

		return (new Rectangle2D.Float((float) (centre.x + radius * min_max[1]), // x
				(float) (centre.y + radius * min_max[2]), // y
				(float) (radius * (min_max[3] - min_max[1])), // width
				(float) (radius * (min_max[0] - min_max[2])) // height
		));

	} // getBoundingRect


	/**
	 * Set the label of the arc.
	 * 
	 * @param s
	 *            The new label
	 */

	// ============================================================
	public void setLabel(
			String s)
	// ============================================================
	{
		_label = s;
	} // setLabel


	/**
	 * Returns the label of the arc.
	 * 
	 * @return The label
	 */

	// ============================================================
	public String getLabel()
	// ============================================================
	{
		return _label;
	} // getLabel


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge mit dem Kreisbogen und gibt sie im
	 * Intersection-Parameter zurueck. Sie kann die folgenden Resultate
	 * annehmen: <ul> <li>eine leere Menge <li>ein Punkt <li>ein Kreisbogen
	 * <li>eine Liste mit zwei Punkten <li>eine Liste mit einem Punkt und einem
	 * Kreisbogen <li>eine Liste mit zwei Kreisboegen </ul> Ein Punkt wird
	 * ausserdem als Funktionswert zurueckgegeben.
	 * 
	 * @param input_arc
	 *            Der Eingabekreisbogen
	 * @param inout_set
	 *            Die Referenz auf das zu berechnende Schnittobjekt
	 * 
	 * @return Der eindeutige Schnittpunkt, falls vorhanden
	 */

	public Point2 intersection(
			Arc2 input_arc,
			Intersection inout_set)
	{
		InspectBCResult inspect = inspectBasicCircle(input_arc);

		if (inspect == null)
		{
			// die Kreise auf denen die Kreisboegen liegen schneiden sich nicht

			inout_set.set();
		}
		else if (inspect.lies_on)
		{
			// die Kreisboegen liegen auf dem gleichen Kreis

			_intersectionArc(input_arc, inout_set);
		}
		else
		{
			// die Kreise auf denen die Kreisboegen liegen schneiden sich in
			// ein oder zwei Punkten

			inout_set.set();

			if (inspect.points != null)
			{
				boolean inside[] = { false, false };

				for (int i = 0; i < inspect.points.length; i++)
				{
					inside[i] = liesOn(inspect.points[i])
							&& input_arc.liesOn(inspect.points[i]);
				} // for

				if (inside[0] && inside[1])
				{
					List list = new List();
					list.add(inspect.points[0]);
					list.add(inspect.points[1]);
					inout_set.set(list);
				}
				else if (inside[0])
				{
					inout_set.set(inspect.points[0]);
				}
				else if (inside[1])
				{
					inout_set.set(inspect.points[1]);
				} // if
			} // if
		} // if

		return (inout_set.point2);

	} // intersection


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge mit der Linie und gibt sie im
	 * Intersection-Parameter zurueck. Sie kann die folgenden Werte annehmen:
	 * <ul> <li>eine leere Menge</li> <li>ein Punkt</li> <li>eine Liste mit zwei
	 * Punkten</li> </ul><br> Ein Punkt wird ausserdem als Funktionswert
	 * zurueckgegeben.
	 * 
	 * @param input_line
	 *            Die Eingabelinie
	 * @param inout_set
	 *            Die Referenz auf das zu berechnende Schnittobjekt
	 * 
	 * @return Der eindeutige Schnittpunkt, falls vorhanden
	 */

	public Point2 intersection(
			BasicLine2 input_line,
			Intersection inout_set)
	{
		// Berechnung der Schnittpunkte mit dem durch den Kreisbogen definierten
		// Kreis

		super.intersection(input_line, inout_set);

		// Ausfiltern der Schnittpunkte, die nicht auf dem Kreisbogen liegen

		if (inout_set.result == Intersection.POINT2)
		{
			if (!liesOn(inout_set.point2))
			{
				inout_set.set();
			} // if
		}
		else if (inout_set.result == Intersection.LIST)
		{
			if (!liesOn((Point2) inout_set.list.firstValue()))
			{
				inout_set.list.Pop();
			} // if

			if (!liesOn((Point2) inout_set.list.lastValue()))
			{
				inout_set.list.pop();
			} // if

			if (inout_set.list.empty())
			{
				inout_set.set();
			}
			else if (inout_set.list.length() == 1)
			{
				inout_set.set((Point2) inout_set.list.Pop());
			} // if
		} // if

		return (inout_set.point2);

	} // intersection


	// ************************************************************************

	/**
	 * Testet ob der Kreisbogen den Rand oder die Flaeche des Rechtecks
	 * schneidet.
	 * 
	 * @param input_box
	 *            das zu testende Rechteck
	 * 
	 * @return true wenn ja, sonst false
	 */

	public boolean intersects(
			Rectangle2D input_box)
	{
		Point2 source = source();
		Point2 target = target();

		if (input_box.contains(source.x, source.y)
				|| input_box.contains(target.x, target.y))
		{
			return (true); // Start- oder Endpunkt im Rechteck
		} // if

		Rectangle2 rect = new Rectangle2(getBoundingRect());
		if (!rect.intersectsOrTouches(input_box))
		{
			return (false); // das kreisumschliessende Rechteck
			// schneidet das Rechteck nicht
		} // if

		// Test auf Schnitt der Rechteckkanten mit dem Kreisbogen

		Rectangle2 box = new Rectangle2(input_box);

		return (intersects(box.top()) || intersects(box.bottom())
				|| intersects(box.left()) || intersects(box.right()));

	} // intersects


	// ************************************************************************

	/**
	 * Testet ob der Winkel im geschlossenen Intervall vom Start- zum Endwinkel
	 * liegt, es wird <b>ohne Epsilon-Umgebung</b> gearbeitet.
	 * 
	 * @param input_angle
	 *            Der Eingabewinkel
	 * 
	 * @return true wenn ja, sonst false
	 */

	public boolean isExactInside(
			double input_angle)
	{
		double alpha = _leftOrientedSource().rad();
		double beta = _leftOrientedTarget().rad();

		if (alpha <= beta)
		{
			return ((input_angle >= alpha) && (input_angle <= beta));
		}
		else
		{
			return ((input_angle >= alpha) || (input_angle <= beta));
		} // if

	} // isExactInside


	// ************************************************************************

	/**
	 * Berechnet die Laenge des Kreisbogens.
	 * 
	 * @return die Laenge
	 */

	public double len()
	{
		return (Math.abs(radius * delta()));

	} // len


	// ************************************************************************

	/**
	 * Testet ob der Punkt auf dem Kreisbogen liegt. Es wird mit einer
	 * Epsilon-Umgebung um den Kreisbogen herum gearbeitet.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
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

		if (Math.abs(square_distance - square_radius) > (bound_distance + bound_radius)
				* _EPSILON)
		{
			return (false);
		} // if

		double alpha = _leftOrientedSource().rad();
		double beta = _leftOrientedTarget().rad();
		double angle = centre.angle(input_point);

		double bound_alpha = Math.abs(angle) + Math.abs(alpha);
		double bound_beta = Math.abs(angle) + Math.abs(beta);

		if (alpha <= beta)
		{
			return ((angle >= alpha - (bound_alpha * _EPSILON)) && (angle <= beta
					+ (bound_beta * _EPSILON)));
		}
		else
		{
			return ((angle >= alpha - (bound_alpha * _EPSILON)) || (angle <= beta
					+ (bound_beta * _EPSILON)));
		} // if

	} // liesOn


	// ************************************************************************

	/**
	 * Berechnet den Punkt in der Mitte zwischen Start- und Endpunkt auf dem
	 * Kreisbogen.
	 * 
	 * @return Der berechneten Punkt
	 */

	public Point2 midPoint()
	{
		double inner_angle = _source_angle.rad() + (delta() / 2);

		Point2 output_inner = new Point2(radius * Math.cos(inner_angle), radius
				* Math.sin(inner_angle));

		output_inner.translate(centre);

		return (output_inner);

	} // midPoint


	// ************************************************************************

	/**
	 * Gibt die Orientierung zurueck.
	 * 
	 * @return Angle.ORIENTATION_LEFT oder Angle.ORIENTATION_RIGHT
	 * 
	 * @see anja.util.Angle#ORIENTATION_LEFT
	 * @see anja.util.Angle#ORIENTATION_RIGHT
	 */

	public int orientation()
	{
		return (_orientation);

	} // orientation


	// ************************************************************************

	/**
	 * Setzt die Orientierung, ist der Eingabewert nicht <tt>
	 * Angle.ORIENTATION_LEFT</tt> oder <tt>Angle.ORIENTATION_RIGHT</tt>, so
	 * bleibt die Orientierung unveraendert.
	 * 
	 * @param input_orientation
	 *            Die Orientierung
	 * 
	 * @see anja.util.Angle#ORIENTATION_LEFT
	 * @see anja.util.Angle#ORIENTATION_RIGHT
	 */

	public void setOrientation(
			int input_orientation)
	{
		if ((input_orientation == Angle.ORIENTATION_LEFT)
				|| (input_orientation == Angle.ORIENTATION_RIGHT))
		{
			_orientation = input_orientation;
		} // if

	} // setOrientation


	// ************************************************************************

	/**
	 * Setzt den Startwinkel.
	 * 
	 * @param input_source_angle
	 *            der Startwinkel in Bogenmass
	 */

	public void setSourceAngle(
			double input_source_angle)
	{
		_source_angle.set(input_source_angle);

	} // setSourceAngle


	// ************************************************************************

	/**
	 * Setzt den Endwinkel.
	 * 
	 * @param input_target_angle
	 *            der Endwinkel in Bogenmass
	 */

	public void setTargetAngle(
			double input_target_angle)
	{
		_target_angle.set(input_target_angle);

	} // setTargetAngle


	// ************************************************************************

	/**
	 * Gibt den Startpunkt zurueck.
	 * 
	 * @return Der Startpunkt
	 */

	public Point2 source()
	{
		return (new Point2(centre.x + _source_angle.cos() * radius, centre.y
				+ _source_angle.sin() * radius));

	} // source


	// ************************************************************************

	/**
	 * Gibt den Startwinkel in Bogenmaß zurueck.
	 * 
	 * @return Der Starwinkel im Bogenmaß
	 */

	public double sourceAngle()
	{
		return (_source_angle.rad());

	} // sourceAngle


	// ************************************************************************

	/**
	 * Gibt diverse trigonometrische Daten des Startwinkels zurueck.
	 * 
	 * @return Daten des Startwinkels
	 * 
	 * @see anja.util.Angle
	 * @see anja.util.LimitedAngle
	 */

	public LimitedAngle sourceTrig()
	{
		return (_source_angle);

	} // sourceTrig


	// ************************************************************************

	/**
	 * Gibt den Endpunkt zurueck.
	 * 
	 * @return Der Endpunkt
	 */

	public Point2 target()
	{
		return (new Point2(centre.x + _target_angle.cos() * radius, centre.y
				+ _target_angle.sin() * radius));

	} // target


	// ************************************************************************

	/**
	 * Gibt den Endwinkel in Bogenmaß zurueck.
	 * 
	 * @return Der Endwinkel im Bogenmaß
	 */

	public double targetAngle()
	{
		return (_target_angle.rad());

	} // targetAngle


	// ************************************************************************

	/**
	 * Gibt diverse trigonometrische Daten des Endwinkels zurueck.
	 * 
	 * @return Die Daten des Endwinkels
	 * 
	 * @see anja.util.Angle
	 * @see anja.util.LimitedAngle
	 */

	public LimitedAngle targetTrig()
	{
		return (_target_angle);

	} // targetTrig


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation.
	 * 
	 * @return die textuelle Repraesentation
	 */

	public String toString()
	{
		return (new String("(" + centre + ", r "
				+ FloatUtil.floatToString((float) radius) + ", source "
				+ _source_angle.rad() + ", target " + _target_angle.rad()
				+ ", " + Angle.orientationToString(_orientation) + ")"));

	} // toString


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge zweier Kreisboegen mit gleichem Mittelpunkt
	 * und Radius und gibt sie im Intersection-Parameter zurueck. Die moeglichen
	 * Ergebnisse sind: ein Punkt ein Kreisbogen eine Liste mit zwei Punkten
	 * eine Liste mit einem Punkt und einem Kreisbogen eine Liste mit zwei
	 * Kreisboegen
	 * 
	 * @param input_arc
	 *            Der Eingabebogen
	 * @param inout_set
	 *            Die Referenz auf das zu berechnende Schnittobjekt
	 */

	private void _intersectionArc(
			Arc2 input_arc,
			Intersection inout_set)
	{
		if (radius == 0)
		{
			inout_set.set((Point2) centre.clone());
		}
		else
		{
			// a0 steht fuer this und a1 fuer input_arc,
			// a0 ist der Kreisbogen von s0 gegen den Uhrzeigersinn nach t0 und
			// a1 ist der Kreisbogen von s1 gegen den Uhrzeigersinn nach t1
			LimitedAngle s0 = _leftOrientedSource();
			LimitedAngle t0 = _leftOrientedTarget();
			LimitedAngle s1 = input_arc._leftOrientedSource();
			LimitedAngle t1 = input_arc._leftOrientedTarget();

			// die Lage der Winkel in Bezug auf den anderen Kreisbogen:
			// Bit 0 = 1: s0 liegt auf a1
			// Bit 1 = 1: t0 liegt auf a1
			// Bit 2 = 1: s1 liegt auf a0
			// Bit 3 = 1: t1 liegt auf a0
			int relation = 0;

			if (input_arc.isExactInside(s0.rad()))
				relation |= 1;
			if (input_arc.isExactInside(t0.rad()))
				relation |= 2;
			if (isExactInside(s1.rad()))
				relation |= 4;
			if (isExactInside(t1.rad()))
				relation |= 8;

			switch (relation)
			{
				case 0: // keine Ueberschneidung
					inout_set.set();
					return;
				case 1: // nur s0 auf a1 : unmoeglich
				case 2: // nur t0 auf a1 : unmoeglich
				case 4: // nur s1 auf a0 : unmoeglich
				case 5: // nur s0 auf a1 und s1 auf a0: unmoeglich
				case 8: // nur t1 auf a0 : unmoeglich
				case 10: // nur t0 auf a1 und t1 auf a0: unmoeglich
					inout_set.set();
					System.err
							.println("Arc2._intersectionArc: unexpected case");
					return;
				case 3: // s0 und t0 auf a1
				case 7: // s0 und t0 auf a1 und s1 auf a0
				case 11: // s0 und t0 auf a1 und t1 auf a0
					_set(s0, t0, inout_set);
					return;
				case 6: // t0 auf a1 und s1 auf a0
					_set(s1, t0, inout_set);
					return;
				case 9: // s0 auf a1 und t1 auf a0
					_set(s0, t1, inout_set);
					return;
				case 12: // s1 und t1 auf a0
				case 13: // s0 auf a1, s1 und t1 auf a0
				case 14: // t0 auf a1, s1 und t1 auf a0
					_set(s1, t1, inout_set);
					return;
				case 15: // s0 und t0 auf a1, s1 und t1 auf a0
					if ((s0 == s1) && (t0 == t1)) // identische Kreisboegen
						_set(s0, t0, inout_set);
					else
						// Schnittmenge enthaelt zwei Elemente
						_set(s0, t1, s1, t0, inout_set);
					return;
				default:
					inout_set.set();
					System.err.println("Arc2._intersectionArc: unknown case");
					return;
			} // switch
		} // if

	} // _intersectionArc


	// ************************************************************************

	/**
	 * Setzen der Schnittmenge Punkt oder Kreisbogen
	 * 
	 * @param input_source
	 *            Wert 1
	 * @param input_target
	 *            Wert 2
	 * @param inout_set
	 *            Die Referenz auf das zu berechnende Schnittobjekt
	 */

	private void _set(
			LimitedAngle input_source,
			LimitedAngle input_target,
			Intersection inout_set)
	{
		Object x = _point_or_arc(input_source, input_target);

		if (x instanceof Point2)
		{
			inout_set.set((Point2) x);
		}
		else
		{
			inout_set.set((Arc2) x);
		} // if

	} // _set


	// ************************************************************************

	/**
	 * Setzen der Schnittmenge mit zwei Elementen vom Typ Punkt und/oder
	 * Kreisbogen.
	 * 
	 * @param input_source1
	 *            Element 1
	 * @param input_target1
	 *            Element 1
	 * @param input_source2
	 *            Element 2
	 * @param input_target2
	 *            Element 2
	 * @param inout_set
	 *            Die Referenz auf das zu berechnende Schnittobjekt
	 */

	private void _set(
			LimitedAngle input_source1,
			LimitedAngle input_target1,
			LimitedAngle input_source2,
			LimitedAngle input_target2,
			Intersection inout_set)
	{
		inout_set.set(new List());
		inout_set.list.add(_point_or_arc(input_source1, input_target1));
		inout_set.list.add(_point_or_arc(input_source2, input_target2));

	} // _set


	// ************************************************************************

	/**
	 * Gibt den durch die Eingabewinkel spezifizierten Punkt oder Kreisbogen
	 * zurueck.
	 * 
	 * @param input_source
	 *            Eingabewinkel, Wert 1
	 * @param input_target
	 *            Eingabewinkel, Wert 2
	 * 
	 * @return Der berechnete Punkt oder Kreisbogen
	 */

	private Object _point_or_arc(
			LimitedAngle input_source,
			LimitedAngle input_target)
	{
		if (input_source.rad() == input_target.rad())
		{
			return (new Point2(centre.x + radius * input_source.cos(), centre.y
					+ radius * input_source.sin()));
		}
		else
		{
			return (new Arc2((Point2) centre.clone(), radius, input_source
					.rad(), input_target.rad(), Angle.ORIENTATION_LEFT));
		} // if

	} // _point_or_arc


	// ************************************************************************

	/**
	 * Gibt den Startwinkel bei der Orientierung entgegen des Uhrzeigersinns
	 * zurueck.
	 * 
	 * @return Startwinkel entgegen des Uhrzeigersinns
	 */

	private LimitedAngle _leftOrientedSource()
	{
		if (_orientation == Angle.ORIENTATION_LEFT)
			return (_source_angle);
		else
			return (_target_angle);

	} // _leftOrientiedSource


	// ************************************************************************

	/**
	 * Gibt den Endwinkel bei der Orientierung entgegen des Uhrzeigersinns
	 * zurueck.
	 * 
	 * @return Der Endwinkel entgegen des Uhrzeigersinns
	 */

	private LimitedAngle _leftOrientedTarget()
	{
		if (_orientation == Angle.ORIENTATION_LEFT)
			return (_target_angle);
		else
			return (_source_angle);

	} // _leftOrientiedTarget


	public static double get_EPSILON()
	{
		return _EPSILON;
	}


	public static void set_EPSILON(
			double _epsilon)
	{
		_EPSILON = _epsilon;
	}

	// ************************************************************************

	// added methods

	/*
	 * private void drawArcDV( int x, int y, int width, int height, int
	 * startAngle, int arcAngle ) { //float w = _stroke.getLineWidth(); int d =
	 * (int) Math.round(w/2); int n = (int) Math.max( 1, Math.round(w+0.499) );
	 * for (int i=1; i<=n; i++ ) { _g.drawArc(x+d-i, y+d-i, width-d+2*i,
	 * height-d+2*i, startAngle, arcAngle ); } }
	 */

} // class Arc2

