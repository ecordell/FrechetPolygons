package anja.ratgeom;


import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import anja.util.Angle;
import anja.util.BigRational;
import anja.util.GraphicsContext;
import anja.util.LimitedAngle;
import anja.util.List;


/**
 * <p align="justify"> Arc2 ist ein zweidimensionaler zeichenbarer Kreisbogen,
 * der von seinem Startwinkel in die Richtung <tt> Angle.ORIENTATION_LEFT </tt>
 * (entgegen dem Uhrzeigersinn) oder <tt> Angle.ORIENTATION_RIGHT </tt> (im
 * Uhrzeigersinn) zu seinem Endwinkel gerichtet ist. <br> Der Wertebereich der
 * Winkel reicht von 0 bis einschliesslich zwei Pi in Bogenmass, ein Kreisbogen
 * ueber den vollen Wertebereich ist ein Vollkreis. <br> <tt> sourceAngle()
 * </tt> und <tt> targetAngle() </tt> liefern den Start- und Endwinkel in
 * Bogenmass, mit <tt> sourceTrig() </tt> und <tt> targetTrig() </tt> koennen
 * ihre trigonometrische Daten wie Sinus etc. <b> effizient </b> abgefragt
 * werden, und <tt> source() </tt> und <tt> target() </tt> liefern den Start-
 * und Endpunkt des Kreisbogens. <br> Man beachte, dass die zeichnerische
 * Darstellung eines Kreisbogens im Vergleich zum ueblichen mathematischen
 * Koordinatensystem auf dem Kopf steht, da im anja.gui die y-Koordinate vom
 * oberen zum unteren Fensterrand hin groesser wird. Um eine dem ueblichen
 * Koordinatensystem entsprechende Darstellung zu erhalten, muss die
 * Transformation von Welt- zu Geraetekoordinaten eine Spiegelung um die x-Achse
 * enthalten. </p>
 * 
 * @version 0.4 24.11.1997
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

	/*
	* Besonderheiten bei der Implementierung
	* --------------------------------------
	*
	* Die Variablen _source und _target werden nur bei Bedarf belegt, um sie
	* nicht unnoetig zu berechnen. Sind ihre exakten Werte in einem Konstruktor
	* bereits bekannt, werden sie selbstverstaendlich ebenfalls gesetzt, wodurch
	* die bei ihrer Berechnung nahezu unvermeidlichen Ungenauigkeiten verhindert
	* werden.
	* Bei Aenderungen der zugehoerigen Winkel oder des Radius muessen sie auf null
	* zurueckgesetzt werden, damit sie nicht falsche Werte enthalten.
	* translate() wirkt auch auf _source und _target, sofern sie belegt sind.
	*/

	// ************************************************************************
	// Constants
	// ************************************************************************

	// die Genauigkeit beim Wurzelziehen
	private static final int	_SCALE		= 40;

	// ************************************************************************
	// Variables
	// ************************************************************************

	/* Orientierung				*/
	private int					_orientation;

	/* Start- und Endpunkt werden nur bei Bedarf belegt, der Lesezugriff auf
	   sie muss deshalb ueber source() und target() erfolgen
	*/
	/* Startpunkt				*/
	private Point2				_source;
	/* Endpunkt					*/
	private Point2				_target;

	/* Startwinkel				*/
	private LimitedAngle		_source_angle;

	/* Endwinkel		 			*/
	private LimitedAngle		_target_angle;

	/* Minimale und maximale Faktoren fuer getBoundingRect	*/
	private double				_limit[]	= { 1, // limit max y
			-1, // limit min x
			-1, // limit min y
			1								};		// limit max x


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
				BigRational.valueOf(0), // radius
				0, // source angle
				0, // target angle
				Angle.ORIENTATION_LEFT);

	} // Arc2


	// ********************************              

	/**
	 * Erzeugt eine Kopie des eingegebenen Kreisbogens.
	 */

	public Arc2(
			Arc2 input_arc)
	{
		super(input_arc);

		_orientation = input_arc._orientation;
		_source = input_arc._source;
		_target = input_arc._target;
		_source_angle = new LimitedAngle(input_arc._source_angle);
		_target_angle = new LimitedAngle(input_arc._target_angle);

	} // Arc2


	// ********************************              

	/**
	 * Erzeugt einen Kreisbogen mit den Eingabeparametern, der Kreisbogen merkt
	 * sich eine <b> Referenz </b> auf den Mittelpunkt.
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
		this(input_centre, BigRational.valueOf(input_radius),
				input_source_angle, input_target_angle, input_orientation);

	} // Arc2


	// ********************************              

	/**
	 * Erzeugt einen Kreisbogen mit den Eingabeparametern, der Kreisbogen merkt
	 * sich eine <b> Referenz </b> auf den Mittelpunkt.
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
			BigRational input_radius,
			double input_source_angle,
			double input_target_angle,
			int input_orientation)
	{
		super(input_centre, input_radius);

		_orientation = input_orientation;
		_source = null;
		_target = null;
		_source_angle = new LimitedAngle(input_source_angle,
				LimitedAngle.CIRCLE_ABS);
		_target_angle = new LimitedAngle(input_target_angle,
				LimitedAngle.CIRCLE_ABS);

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
			double input_centre_x,
			double input_centre_y,
			double input_radius,
			double input_source_angle,
			double input_target_angle,
			int input_orientation)
	{
		this(new Point2(input_centre_x, input_centre_y), BigRational
				.valueOf(input_radius), input_source_angle, input_target_angle,
				input_orientation);

	} // Arc2


	// ********************************              

	/**
	 * Erzeugt einen vom Startpunkt ueber den Zwischenpunkt zum Endpunkt
	 * gerichteten Kreisbogen, es werden <b> keine Referenzen </b> auf die
	 * Punkte gespeichert.
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
		BigRational x1 = input_source.x;
		BigRational y1 = input_source.y;
		BigRational x2 = input_inner.x;
		BigRational y2 = input_inner.y;
		BigRational x3 = input_target.x;
		BigRational y3 = input_target.y;

		BigRational dx12 = x2.subtract(x1);
		BigRational dx13 = x3.subtract(x1);
		BigRational dy12 = y2.subtract(y1);
		BigRational dy13 = y3.subtract(y1);

		BigRational det = (dx12.multiply(dy13)).subtract(dx13.multiply(dy12));
		BigRational det_mul_2 = det.multiply(BigRational.valueOf(2));

		BigRational sum1s = x1.square().add(y1.square());
		BigRational f1 = x2.square().add(y2.square()).subtract(sum1s);
		BigRational f2 = x3.square().add(y3.square()).subtract(sum1s);

		BigRational det1 = f1.multiply(dy13).subtract(f2.multiply(dy12));
		BigRational det2 = f2.multiply(dx12).subtract(f1.multiply(dx13));

		centre = new Point2(det1.divide(det_mul_2), det2.divide(det_mul_2));

		_source_angle = new LimitedAngle(centre.angle(input_source),
				LimitedAngle.CIRCLE_ABS);
		_target_angle = new LimitedAngle(centre.angle(input_target),
				LimitedAngle.CIRCLE_ABS);

		_orientation = Angle.orientation(_source_angle.rad(),
				centre.angle(input_inner), _target_angle.rad());

		// Start- und Endpunkt werden auf die entsprechenden Eingabepunkte
		// gesetzt und das Quadrat des Radius aus dem Mittelpunkt und dem
		// inneren Punkt berechnet, so dass liesOn() fuer alle drei Punkte
		// korrekt true liefert.
		// _source und _target werden von setRadius() auf null gesetzt und
		// duerfen daher erst danach gesetzt werden!

		BigRational square_radius = centre.bigSquareDistance(input_inner);
		BigRational radius = square_radius.sqrt(_SCALE);

		setRadius(radius, square_radius);

		_source = input_source;
		_target = input_target;

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
	 * Zeichnet den Kreisbogen.
	 */

	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		toGeom().draw(g, gc);

	} // draw


	// ************************************************************************

	/**
	 * Gibt das umschliessende Rechteck zurueck.
	 */

	public Rectangle2D getBoundingRect()
	{
		// Anfangswinkel des Kreisbogen entgegen des Uhrzeigersinns
		LimitedAngle alpha = _ccwSourceAngle();

		// Endwinkel des Kreisbogen entgegen des Uhrzeigersinns
		LimitedAngle beta = _ccwTargetAngle();

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

		float f_radius = radius().floatValue();

		return (new Rectangle2D.Float((float) (centre.x.floatValue() + f_radius
				* min_max[1]), // x
				(float) (centre.y.floatValue() + f_radius * min_max[2]), // y
				(float) (f_radius * (min_max[3] - min_max[1])), // width
				(float) (f_radius * (min_max[0] - min_max[2])) // height
		));

	} // getBoundingRect


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge mit dem Kreisbogen und gibt sie im
	 * Intersection-Parameter zurueck. Sie kann die folgenden Resultate
	 * annehmen: <ul> <li> eine leere Menge <li> ein Punkt <li> ein Kreisbogen
	 * <li> eine Liste mit zwei Punkten <li> eine Liste mit einem Punkt und
	 * einem Kreisbogen <li> eine Liste mit zwei Kreisboegen </ul> Ein Punkt
	 * wird ausserdem als Funktionswert zurueckgegeben.
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
					inside[i] = _liesOnAngle(inspect.points[i])
							&& input_arc._liesOnAngle(inspect.points[i]);
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
	 * <ul> <li> eine leere Menge <li> ein Punkt <li> eine Liste mit zwei
	 * Punkten </ul> Ein Punkt wird ausserdem als Funktionswert zurueckgegeben.
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
		anja.geom.Point2 source = source().toGeom();
		anja.geom.Point2 target = target().toGeom();

		if (input_box.contains(source.x, source.y)
				|| input_box.contains(target.x, target.y))
		{
			return (true); // Start- oder Endpunkt im Rechteck
		} // if

		if (!getBoundingRect().intersects(input_box))
		{
			return (false); // das kreisumschliessende Rechteck
			// schneidet das Rechteck nicht
		} // if

		// Test auf Schnitt der Rechteckkanten mit dem Kreisbogen

		anja.geom.Arc2 arc = toGeom();
		anja.geom.Rectangle2 box = new anja.geom.Rectangle2(input_box);

		return (arc.intersects(box.top()) || arc.intersects(box.bottom())
				|| arc.intersects(box.left()) || arc.intersects(box.right()));

	} // intersects


	// ************************************************************************

	/**
	 * Testet ob der Winkel im geschlossenen Intervall vom Start- zum Endwinkel
	 * liegt, es wird <b> ohne Epsilon-Umgebung </b> gearbeitet.
	 * 
	 * @return true wenn ja, sonst false
	 */

	public boolean isExactInside(
			double input_angle)
	{
		double alpha = _ccwSourceAngle().rad();
		double beta = _ccwTargetAngle().rad();

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
		return (Math.abs(radius().doubleValue() * delta()));

	} // len


	// ************************************************************************

	/**
	 * Testet ob der Punkt auf dem Kreisbogen liegt.
	 * 
	 * @return true wenn ja, sonst false
	 */

	public boolean liesOn(
			Point2 input_point)
	{
		if ((centre.bigSquareDistance(input_point)).compareTo(squareRadius()) != 0)
		{
			// die Distanz zum Mittelpunkt ist ungleich dem Radius
			return (false);
		} // if

		if (radius().signum() == 0)
		{
			// der Radius ist gleich 0 und der Punkt liegt auf dem Mittelpunkt
			return (true);
		} // if

		return (_liesOnAngle(input_point));

	} // liesOn


	// ************************************************************************

	/**
	 * Berechnet den Punkt in der Mitte zwischen Start- und Endpunkt auf dem
	 * Kreisbogen.
	 * 
	 * @return den berechneten Punkt
	 */

	public Point2 midPoint()
	{
		double inner_angle = _source_angle.rad() + (delta() / 2);

		Point2 output_inner = new Point2(radius().multiply(
				BigRational.valueOf(Math.cos(inner_angle))), radius().multiply(
				BigRational.valueOf(Math.sin(inner_angle))));

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
	 * Angle.ORIENTATION_LEFT </tt> oder <tt> Angle.ORIENTATION_RIGHT</tt>, so
	 * bleibt die Orientierung unveraendert.
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
	 * Setzt den Radius auf den Betrag des Eingabewertes.
	 */

	public void setRadius(
			BigRational input_radius)
	{
		super.setRadius(input_radius);

		// Start- und Endpunkt zuruecksetzen, damit sie keine falschen Werte
		// enthalten

		_source = null;
		_target = null;

	} // setRadius


	// ************************************************************************

	/**
	 * Setzt den Radius auf den Betrag des Eingaberadius und den squareRadius
	 * auf den Betrag des Eingabe-SquareRadius, falls dieser ungleich null ist,
	 * sonst auf null. <br> Die korrektheit der beiden Werte wird nicht
	 * geprueft, das liegt in der Verantwortung der aufrufenden Methode.
	 */

	public void setRadius(
			BigRational input_radius,
			BigRational input_square)
	{
		super.setRadius(input_radius, input_square);

		// Start- und Endpunkt zuruecksetzen, damit sie keine falschen Werte
		// enthalten

		_source = null;
		_target = null;

	} // setRadius


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

		// den Startpunkt zuruecksetzen, damit er keinen falschen Wert enthaelt
		_source = null;

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

		// den Endpunkt zuruecksetzen, damit er keinen falschen Wert enthaelt
		_target = null;

	} // setTargetAngle


	// ************************************************************************

	/**
	 * Gibt eine Kopie des Startpunkts zurueck.
	 */

	public Point2 source()
	{
		if (_source == null)
			_source = _pointAtAngle(_source_angle);

		return (new Point2(_source));

	} // source


	// ************************************************************************

	/**
	 * Gibt den Startwinkel in Bogenmass zurueck.
	 */

	public double sourceAngle()
	{
		return (_source_angle.rad());

	} // sourceAngle


	// ************************************************************************

	/**
	 * Gibt diverse trigonometrische Daten des Startwinkels zurueck.
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
	 * Gibt eine Kopie des Endpunkts zurueck.
	 */

	public Point2 target()
	{
		if (_target == null)
			_target = _pointAtAngle(_target_angle);

		return (new Point2(_target));

	} // target


	// ************************************************************************

	/**
	 * Gibt den Endwinkel in Bogenmass zurueck.
	 */

	public double targetAngle()
	{
		return (_target_angle.rad());

	} // targetAngle


	// ************************************************************************

	/**
	 * Gibt diverse trigonometrische Daten des Endwinkels zurueck.
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
	 * Erzeugt einen anja.geom.Arc2 aus dem Kreisbogen und gibt ihn zurueck.
	 */

	public anja.geom.Arc2 toGeom()
	{
		return (new anja.geom.Arc2(centre.toGeom(), radius().floatValue(),
				_source_angle.rad(), _target_angle.rad(), _orientation));

	} // toGeom


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation.
	 * 
	 * @return die textuelle Repraesentation
	 */

	public String toString()
	{
		return (new String("(" + centre + ", r " + radius().doubleValue()
				+ ", source " + _source_angle.rad() + ", target "
				+ _target_angle.rad() + ", "
				+ Angle.orientationToString(_orientation) + ")"));

	} // toString


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
			double input_horizontal,
			double input_vertical)
	{
		translate(new Point2(input_horizontal, input_vertical));

	} // translate


	// ********************************

	/**
	 * Verschiebung um den Vektor vom Nullpunkt zum Eingabepunkt.
	 * 
	 * @param input_vector
	 *            der Eingabepunkt
	 */

	public void translate(
			Point2 input_vector)
	{
		centre.translate(input_vector);

		if (_source != null)
		{
			_source.translate(input_vector);
		} // if

		if (_target != null)
		{
			_target.translate(input_vector);
		} // if

	} // translate


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/*
	* Berechnet die Schnittmenge zweier Kreisboegen mit gleichem Mittelpunkt
	* und Radius und gibt sie im Intersection-Parameter zurueck. Die moeglichen
	* Ergebnisse sind:
	* · ein Punkt
	* · ein Kreisbogen
	* · eine Liste mit zwei Punkten
	* · eine Liste mit einem Punkt und einem Kreisbogen
	* · eine Liste mit zwei Kreisboegen
	*/

	private void _intersectionArc(
			Arc2 input_arc,
			Intersection inout_set)
	{
		if (radius().signum() == 0)
		{
			inout_set.set((Point2) centre.clone());
		}
		else
		{
			// a0 steht fuer this und a1 fuer input_arc,
			// a0 ist der Kreisbogen von s0 gegen den Uhrzeigersinn nach t0 und
			// a1 ist der Kreisbogen von s1 gegen den Uhrzeigersinn nach t1
			LimitedAngle s0 = _ccwSourceAngle();
			LimitedAngle t0 = _ccwTargetAngle();
			LimitedAngle s1 = input_arc._ccwSourceAngle();
			LimitedAngle t1 = input_arc._ccwTargetAngle();

			// die Lage der Winkel in Bezug auf den anderen Kreisbogen:
			//    Bit 0 = 1: s0 liegt auf a1
			//    Bit 1 = 1: t0 liegt auf a1
			//    Bit 2 = 1: s1 liegt auf a0
			//    Bit 3 = 1: t1 liegt auf a0
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
				case 1: // nur s0 auf a1              : unmoeglich
				case 2: // nur t0 auf a1              : unmoeglich
				case 4: // nur s1 auf a0              : unmoeglich
				case 5: // nur s0 auf a1 und s1 auf a0: unmoeglich
				case 8: // nur t1 auf a0              : unmoeglich
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
				case 12: //            s1 und t1 auf a0
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

	/*
	* Gibt den Startpunkt bei der Orientierung entgegen des Uhrzeigersinns
	* zurueck.
	*/

	private Point2 _ccwSource()
	{
		if (_orientation == Angle.ORIENTATION_LEFT)
			return (source());
		else
			return (target());

	} // _ccwSource


	// ************************************************************************

	/*
	* Gibt den Endpunkt bei der Orientierung entgegen des Uhrzeigersinns
	* zurueck.
	*/

	private Point2 _ccwTarget()
	{
		if (_orientation == Angle.ORIENTATION_LEFT)
			return (target());
		else
			return (source());

	} // _ccwTarget


	// ************************************************************************

	/*
	* Gibt den Startwinkel bei der Orientierung entgegen des Uhrzeigersinns
	* zurueck.
	*/

	private LimitedAngle _ccwSourceAngle()
	{
		if (_orientation == Angle.ORIENTATION_LEFT)
			return (_source_angle);
		else
			return (_target_angle);

	} // _ccwSourceAngle


	// ************************************************************************

	/*
	* Gibt den Endwinkel bei der Orientierung entgegen des Uhrzeigersinns
	* zurueck.
	*/

	private LimitedAngle _ccwTargetAngle()
	{
		if (_orientation == Angle.ORIENTATION_LEFT)
			return (_target_angle);
		else
			return (_source_angle);

	} // _ccwTargetAngle


	// ************************************************************************

	/*
	* Gibt true zurueck wenn der Eingabepunkt auf oder zwischen den Strahlen
	* vom Mittelpunkt durch den Start- und/oder Endpunkt liegt.
	*/

	private boolean _liesOnAngle(
			Point2 input_point)
	{
		Point2 ccw_source = _ccwSource();
		Point2 ccw_target = _ccwTarget();

		switch (ccw_target.orientation(centre, ccw_source))
		{
			case Point2.ORIENTATION_COLLINEAR:
				{
					if (ccw_target.inspectCollinearPoint(centre, ccw_source) == Point2.LIES_BEFORE)
					{
						// der Kreisbogenwinkel ist gleich 180 Grad
						int point_o_source = input_point.orientation(centre,
								ccw_source);
						return ((point_o_source == Point2.ORIENTATION_LEFT) || (point_o_source == Point2.ORIENTATION_COLLINEAR));
					}
					else
					{
						// der Kreisbogenwinkel ist gleich 0 Grad
						return (new Ray2(centre, ccw_source))
								.liesOn(input_point);
					} // if
				}
			case Point2.ORIENTATION_LEFT:
				{
					// der Kreisbogenwinkel ist kleiner als 180 Grad
					int point_o_source = input_point.orientation(centre,
							ccw_source);
					int point_o_target = input_point.orientation(centre,
							ccw_target);

					return (((point_o_source == Point2.ORIENTATION_LEFT) || (point_o_source == Point2.ORIENTATION_COLLINEAR)) && ((point_o_target == Point2.ORIENTATION_RIGHT) || (point_o_target == Point2.ORIENTATION_COLLINEAR)));
				}
			case Point2.ORIENTATION_RIGHT:
				{
					// der Kreisbogenwinkel ist groesser als 180 Grad
					int point_o_source = input_point.orientation(centre,
							ccw_source);
					int point_o_target = input_point.orientation(centre,
							ccw_target);
					return (((point_o_source == Point2.ORIENTATION_LEFT) || (point_o_source == Point2.ORIENTATION_COLLINEAR)) || ((point_o_target == Point2.ORIENTATION_RIGHT) || (point_o_target == Point2.ORIENTATION_COLLINEAR)));
				}
			case Point2.ORIENTATION_UNDEFINED:
			default:
				System.err.println("ratgeom.Arc2._liesOnAngle: failed");
				return (false);
		} // switch

	} // _liesOnAngle


	// ************************************************************************

	/*
	* Gibt den durch den Winkel spezifizierten Punkt auf dem Kreisbogen
	* zurueck.
	*/

	private Point2 _pointAtAngle(
			LimitedAngle input_angle)
	{
		Point2 point_at_angle = new Point2(radius().multiply(
				BigRational.valueOf(input_angle.cos())), radius().multiply(
				BigRational.valueOf(input_angle.sin())));

		point_at_angle.translate(centre);

		return (point_at_angle);

	} // _pointAtAngle


	// ************************************************************************

	/*
	* Gibt den durch die Eingabewinkel spezifizierten Punkt oder Kreisbogen
	* zurueck.
	*/

	private Object _pointOrArc(
			LimitedAngle input_source,
			LimitedAngle input_target)
	{
		if (input_source.rad() == input_target.rad())
		{
			return (_pointAtAngle(input_source));
		}
		else
		{
			return (new Arc2((Point2) centre.clone(), radius(),
					input_source.rad(), input_target.rad(),
					Angle.ORIENTATION_LEFT));
		} // if

	} // _pointOrArc


	// ************************************************************************

	/* 
	* Setzen der Schnittmenge Punkt oder Kreisbogen
	*/

	private void _set(
			LimitedAngle input_source,
			LimitedAngle input_target,
			Intersection inout_set)
	{
		Object x = _pointOrArc(input_source, input_target);

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

	/* 
	* Setzen der Schnittmenge mit zwei Elementen vom Typ Punkt und/oder
	* Kreisbogen.
	*/

	private void _set(
			LimitedAngle input_source1,
			LimitedAngle input_target1,
			LimitedAngle input_source2,
			LimitedAngle input_target2,
			Intersection inout_set)
	{
		inout_set.set(new List());
		inout_set.list.add(_pointOrArc(input_source1, input_target1));
		inout_set.list.add(_pointOrArc(input_source2, input_target2));

	} // _set

	// ************************************************************************

} // class Arc2

