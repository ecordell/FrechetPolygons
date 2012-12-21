package anja.ratgeom;


import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import anja.util.Angle;
import anja.util.BigRational;
import anja.util.Comparitor;
import anja.util.Drawable;
import anja.util.FloatUtil;
import anja.util.GraphicsContext;
import anja.util.Savable;


/**
 * Zweidimensionaler zeichenbarer Punkt mit rationalen Koordinaten beliebiger
 * Genauigkeit.
 * 
 * @version 1.4 24.11.1997
 * @author Norbert Selle
 */

public class Point2
		implements Drawable, Savable, Cloneable
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	/** der Punkt liegt auf der Geraden */
	public final static int		ORIENTATION_COLLINEAR	= 1;

	/** der Punkt liegt links der Geraden */
	public final static int		ORIENTATION_LEFT		= 2;

	/** der Punkt liegt rechts der Geraden */
	public final static int		ORIENTATION_RIGHT		= 3;

	/** der Punkt liegt nicht auf der zu einem Punkt entarteten Geraden */
	public final static int		ORIENTATION_UNDEFINED	= 4;

	/** der Punkt liegt vor dem Intervall auf der Geraden */
	public static final int		LIES_BEFORE				= 11;

	/** der Punkt liegt in dem Intervall auf der Geraden */
	public static final int		LIES_ON					= 12;

	/** der Punkt liegt hinter dem Intervall auf der Geraden */
	public static final int		LIES_BEHIND				= 13;

	/* das Intervall hat die Laenge 0					*/
	private static final int	_ZERO_INTERVAL			= 14;

	/* Genauigkeit beim Wurzelziehen					*/
	private static final int	_SCALE					= 40;

	// ************************************************************************
	// Variables
	// ************************************************************************

	/** x-Koordinate */
	public BigRational			x;

	/** y-Koordinate */
	public BigRational			y;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt einen Punkt mit den Koordinaten (0, 0).
	 */

	public Point2()
	{
		x = BigRational.valueOf(0);
		y = x;

	} // Point2


	// ********************************

	/**
	 * Punkt einlesen.
	 */

	public Point2(
			DataInputStream dis)
	{
		try
		{
			String type = dis.readUTF();

			if (type.compareTo("Point2") != 0)
				return;

			x = BigRational.valueOf(dis.readFloat());
			y = BigRational.valueOf(dis.readFloat());
		}
		catch (IOException ex)
		{} // try

	} // Point2


	// ********************************

	/**
	 * Erzeugt einen Punkt mit den Eingabekoordinaten.
	 */

	public Point2(
			BigRational input_x,
			BigRational input_y)
	{
		x = input_x;
		y = input_y;

	} // Point2


	// ********************************

	/**
	 * Erzeugt einen Punkt mit den Eingabekoordinaten.
	 */

	public Point2(
			double input_x,
			double input_y)
	{
		x = BigRational.valueOf(input_x);
		y = BigRational.valueOf(input_y);

	} // Point2


	// ********************************

	/**
	 * Erzeugt einen Punkt mit den Eingabekoordinaten.
	 */

	public Point2(
			float input_x,
			float input_y)
	{
		x = BigRational.valueOf(input_x);
		y = BigRational.valueOf(input_y);

	} // Point2


	// ********************************

	/**
	 * Erzeugt einen Punkt mit den Koordinaten des Eingabepunktes.
	 * 
	 * @param input_point
	 *            der Eingabepunkt
	 */

	public Point2(
			Point2 input_point)
	{
		x = input_point.x;
		y = input_point.y;

	} // Point2


	// ********************************

	/**
	 * Erzeugt einen Punkt durch Skalarmultiplikation aus dem Eingabepunkt.
	 * 
	 * @param input_scalar
	 *            der Faktor
	 * @param input_point
	 *            der Eingabepunkt
	 */

	public Point2(
			double input_scalar,
			Point2 input_point)
	{
		BigRational big_scalar = BigRational.valueOf(input_scalar);

		x = big_scalar.multiply(input_point.x);
		y = big_scalar.multiply(input_point.y);

	} // Point2


	// ********************************

	/**
	 * Erzeugt einen Punkt s1*p1+s2*p2 durch Linearkombination von zwei Punkten.
	 * 
	 * @param s1
	 *            der Faktor vor p1
	 * @param p1
	 *            der erste Eingabepunkt
	 * @param s2
	 *            der Faktor vor p2
	 * @param p2
	 *            der zweite Eingabepunkt
	 */

	public Point2(
			double s1,
			Point2 p1,
			double s2,
			Point2 p2)
	{
		this(BigRational.valueOf(s1), p1, BigRational.valueOf(s2), p2);

	} // Point2


	// ********************************

	/**
	 * Erzeugt einen Punkt s1*p1+s2*p2 durch Linearkombination von zwei Punkten.
	 * 
	 * @param s1
	 *            der Faktor vor p1
	 * @param p1
	 *            der erste Eingabepunkt
	 * @param s2
	 *            der Faktor vor p2
	 * @param p2
	 *            der zweite Eingabepunkt
	 */

	public Point2(
			BigRational s1,
			Point2 p1,
			BigRational s2,
			Point2 p2)
	{
		x = (s1.multiply(p1.x)).add(s2.multiply(p2.x));

		y = (s1.multiply(p1.y)).add(s2.multiply(p2.y));

	} // Point2


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Berechnet den Winkel entgegen des Uhrzeigersinns zwischen dem Strahl
	 * beginnend beim Punkt parallel zum positiven Abschnitt der x-Achse und dem
	 * Segment vom Punkt zum Eingabepunkt. <BR>Das Ergebnis ist groesser gleich
	 * null und kleiner zwei Pi.
	 * 
	 * @param input_point
	 *            der Eingabepunkt
	 * 
	 * @return den berechneten Winkel in Bogenmass
	 */

	public double angle(
			Point2 input_point)
	{
		// Seien a und b die Katheten eines rechtwinkligen Dreiecks
		// mit a parallel zur y-Achse und b auf der x-Achse.
		// Alpha sei der Seite a gegenueberliegende Winkel und liege
		// am Koordinatenursprung.
		// Dann gilt:
		//		tan( alpha ) = a / b
		// und damit:
		//		alpha = atan( a / b )
		//
		// Der Fall dass b gleich null ist muss gesondert behandelt werden,
		// um Division durch null zu verhindern.

		BigRational a = input_point.y.subtract(y);
		BigRational b = input_point.x.subtract(x);

		double output_alpha;

		if (b.signum() == 1)
		{
			if (a.signum() >= 0) // a >= 0, b > 0
			{
				output_alpha = _atan(a.divide(b));
			}
			else
			// a < 0, b > 0
			{
				output_alpha = Angle.PI_MUL_2 + _atan(a.divide(b));
			} // if
		}
		else if (b.signum() == 0)
		{
			if (a.signum() == 1) // a > 0, b = 0
			{
				output_alpha = Angle.PI_DIV_2;
			}
			else if (a.signum() == 0) // a = 0, b = 0
			{
				output_alpha = 0;
			}
			else
			// a < 0, b = 0
			{
				output_alpha = Angle.PI + Angle.PI_DIV_2; // drei halbe Pi
			} // if
		}
		else
		//        b < 0
		{
			output_alpha = Angle.PI + _atan(a.divide(b));
		} // if

		return (output_alpha);

	} // angle


	// *****************

	/**
	 * Berechnet den Winkel entgegen dem Uhrzeigersinn zwischen dem Segment vom
	 * Punkt zum ersten Eingabepunkt und dem Segment vom Punkt zum zweiten
	 * Eingabepunkt. <BR>Das Ergebnis ist groesser gleich null und kleiner zwei
	 * Pi.
	 * 
	 * @param input_point1
	 *            der erste Eingabepunkt
	 * @param input_point2
	 *            der zweite Eingabepunkt
	 * 
	 * @return den berechneten Winkel in Bogenmass
	 */

	public double angle(
			Point2 input_point1,
			Point2 input_point2)
	{
		double alpha1 = angle(input_point1);
		double alpha2 = angle(input_point2);

		double output_angle = alpha2 - alpha1;

		if (output_angle < 0)
		{
			// Wertebereich eines negativen Ergebnisses durch Addition
			// von zwei Pi korrigieren

			output_angle = Angle.PI_MUL_2 + output_angle;
		} // if

		return (output_angle);

	} // angle


	// ************************************************************************

	/**
	 * Berechnet den euklidischen Abstand zum Nullpunkt.
	 * 
	 * @return den euklidischen Abstand zum Nullpunkt
	 * 
	 * @see #distance
	 */

	public BigRational bigDistance()
	{
		return (bigSquareDistance().sqrt(_SCALE));

	} // bigDistance


	// ********************************

	/**
	 * Berechnet den euklidischen Abstand zum Eingabepunkt.
	 * 
	 * @param input_point
	 *            Eingabepunkt
	 * 
	 * @return den euklidischen Abstand zum Eingabepunkt
	 * 
	 * @see #distance
	 */

	public BigRational bigDistance(
			Point2 input_point)
	{
		return (bigSquareDistance(input_point).sqrt(_SCALE));

	} // bigDistance


	// ************************************************************************

	/**
	 * Berechnet das Quadrat des euklidischen Abstands zum Nullpunkt.
	 * 
	 * @return das Quadrat des euklidischen Abstands
	 * 
	 * @see #distance
	 */

	public BigRational bigSquareDistance()
	{
		return (x.square().add(y.square()));

	} // bigSquareDistance


	// ********************************

	/**
	 * Berechnet das Quadrat des euklidischen Abstands zum Eingabepunkt.
	 * 
	 * @return das Quadrat des euklidischen Abstands
	 * 
	 * @see #distance
	 */

	public BigRational bigSquareDistance(
			Point2 input_point)
	{
		BigRational delta_x = x.subtract(input_point.x);
		BigRational delta_y = y.subtract(input_point.y);

		return ((delta_x.square()).add(delta_y.square()));

	} // bigSquareDistance


	// ************************************************************************

	/**
	 * Erzeugt eine Kopie und gibt sie zurueck.
	 */

	public Object clone()
	{
		return (new Point2(this));

	} // clone


	// ************************************************************************

	/**
	 * Berechnet den euklidischen Abstand zum Nullpunkt.
	 * 
	 * @return den euklidischen Abstand zum Nullpunkt
	 * 
	 * @see #squareDistance
	 */

	public double distance()
	{
		return (Math.sqrt(squareDistance()));

	} // distance


	// ********************************

	/**
	 * Berechnet den euklidischen Abstand zum Eingabepunkt.
	 * 
	 * @param input_point
	 *            Eingabepunkt
	 * 
	 * @return den euklidischen Abstand zum Eingabepunkt
	 * 
	 * @see #squareDistance
	 */

	public double distance(
			Point2 input_point)
	{
		return (Math.sqrt(squareDistance(input_point)));

	} // distance


	// ************************************************************************

	/**
	 * Zeichnet den Punkt. Verwendete Stilarten: <p> <b>CAP_SQUARE</b> malt ein
	 * Quadrat mit Seitenlaenge lineWidth <br><b>CAP_ROUND</b> malt eine
	 * Kreisscheibe mit Durchmesser lineWidth <br><b>CAP_BUTT</b> malt gar
	 * nichts.
	 * 
	 * @param g
	 *            Das Graphics2D Objekt, in das gezeichnet wird
	 * @param gc
	 *            Der dazugehörige GraphicsContext
	 * 
	 * @see anja.util.Drawable#draw(Graphics2D, GraphicsContext)
	 */

	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		toGeom().draw(g, gc);

	} // draw


	// ************************************************************************

	/**
	 * Gibt true zurueck bei Gleichheit, sonst false.
	 * 
	 * @return true bei Gleichheit, sonst false.
	 */

	public boolean equals(
			Object input_object)
	{
		if (!(input_object instanceof Point2))
			return (false);

		Point2 point = (Point2) input_object;

		return (x.equals(point.x) && y.equals(point.y));

	} // equals


	// ************************************************************************

	/**
	 * True, falls p,q1,q1 kollinear sind UND p im Intervall [q1,q2] liegt.
	 */

	public boolean inSegment(
			Point2 q1,
			Point2 q2)
	{
		return ((isCollinear(q1, q2)) && (inspectCollinearPoint(q1, q2) == Point2.LIES_ON));

	} // inSegment


	// ************************************************************************

	/**
	 * Untersucht die Lage des Punktes in Bezug auf die mit ihm auf einer Gerade
	 * liegenden Eingabepunkte. Der erste Eingabepunkt markiert den Anfang und
	 * der zweite das Ende eines Abschnitts auf der Geraden.
	 * 
	 * @param input_source
	 *            der Intervallanfang
	 * @param input_target
	 *            das Intervallende
	 * 
	 * @return LIES_BEFORE, LIES_ON oder LIES_BEHIND
	 * 
	 * @see #LIES_BEFORE
	 * @see #LIES_ON
	 * @see #LIES_BEHIND
	 */

	public int inspectCollinearPoint(
			Point2 input_source,
			Point2 input_target)
	{
		int result = _inspectIntervall(x, input_source.x, input_target.x);

		if (result != _ZERO_INTERVAL)
			return (result);

		// die Gerade ist vertikal, deshalb die y-Koordinaten untersuchen

		result = _inspectIntervall(y, input_source.y, input_target.y);

		if (result != _ZERO_INTERVAL)
			return (result);
		else
			return (LIES_ON);

	} // inspectCollinearPoint


	// ************************************************************************

	/**
	 * Testet ob der Punkt im Rechteck enthalten ist.
	 * 
	 * @param box
	 *            das zu testende Rechteck
	 * 
	 * @return true wenn ja, sonst false
	 */

	public boolean intersects(
			Rectangle2D box)
	{
		return (box.contains(x.floatValue(), y.floatValue()));

	} // intersects


	// ************************************************************************

	/**
	 * Testet ob der Punkt auf der Geraden durch die Eingabepunkte liegt.
	 * 
	 * @return true wenn ja, sonst false
	 * 
	 * @see #orientation
	 */
	public boolean isCollinear(
			Point2 input_source,
			Point2 input_target)
	{
		return (orientation(input_source, input_target) == ORIENTATION_COLLINEAR);

	} // isCollinear


	// ************************************************************************

	/**
	 * Teste, ob der Punkt im Winkelbereich PQR liegt, das heisst zwischen den
	 * Strahlen QP und QR oder genauer gesagt: Zurueckgegeben wird true genau
	 * dann, wenn der Punkt rechts von der Geraden QP UND links von der Geraden
	 * QR liegt. Vorausgesetzt, dass der Winkel PQR kleiner oder gleich 180 Grad
	 * ist.
	 */

	public boolean isInAngle(
			Point2 P,
			Point2 Q,
			Point2 R)
	{
		int orQP = orientation(Q, P);
		int orQR = orientation(Q, R);

		return (orQP == ORIENTATION_RIGHT && orQR == ORIENTATION_LEFT);

	} // isInAngle


	// ************************************************************************

	/**
	 * Teste, ob der Punkt (this) kleiner als der gegebenen Punkt ist.
	 */

	public boolean isSmaller(
			Point2 p)
	{
		return (PointComparitor.compareX(this, p) == Comparitor.SMALLER);

	} //isSmaller


	// ************************************************************************

	/**
	 * Setzt die Koordinaten auf die Eingabekoordinaten.
	 * 
	 * @param input_x
	 *            die x-Koordinate
	 * @param input_y
	 *            die y-Koordinate
	 */

	public void moveTo(
			double input_x,
			double input_y)
	{
		x = BigRational.valueOf(input_x);
		y = BigRational.valueOf(input_y);

	} // moveTo


	// ********************************

	/**
	 * Setzt die Koordinaten auf die Koordinaten des Eingabepunktes.
	 * 
	 * @param input_point
	 *            der Eingabepunkt
	 */

	public void moveTo(
			Point2 input_point)
	{
		x = input_point.x;
		y = input_point.y;

	} // moveTo


	// ************************************************************************

	/**
	 * Testet ob der Punkt in der linken oder der rechten Halbebene oder auf der
	 * Geraden durch den ersten und zweiten Eingabepunkt liegt, die Gerade ist
	 * in Richtung vom ersten zum zweiten Eingabepunkt orientiert.<br> Sind die
	 * Eingabepunkte gleich so wird <b> ORIENTATION_COLLINEAR </b>
	 * zurueckgegeben, wenn der Eingabepunkt auf ihnen liegt, sonst wird <b>
	 * ORIENTATION_UNDEFINED </b> zurueckgegeben.
	 * 
	 * @param input_source
	 *            der erste Eingabepunkt
	 * @param input_target
	 *            der zweite Eingabepunkt
	 * 
	 * @return ORIENTATION_COLLINEAR, ORIENTATION_LEFT etc.
	 * 
	 * @see #ORIENTATION_COLLINEAR
	 * @see #ORIENTATION_LEFT
	 * @see #ORIENTATION_RIGHT
	 * @see #ORIENTATION_UNDEFINED
	 * @see #isCollinear
	 */

	public int orientation(
			Point2 input_source,
			Point2 input_target)
	{
		int output_orientation;

		if (input_source.equals(input_target))
		{
			if (equals(input_source))
			{
				output_orientation = ORIENTATION_COLLINEAR;
			}
			else
			{
				output_orientation = ORIENTATION_UNDEFINED;
			} // if
		}
		else
		{
			BigRational dx_source = input_source.x.subtract(x);
			BigRational dx_target = input_target.x.subtract(x);

			BigRational dy_source = input_source.y.subtract(y);
			BigRational dy_target = input_target.y.subtract(y);

			BigRational area = (dx_source.multiply(dy_target))
					.subtract(dy_source.multiply(dx_target));

			if (area.signum() == 0)
			{
				output_orientation = ORIENTATION_COLLINEAR;
			}
			else
			{
				if (area.signum() == -1)
				{
					output_orientation = ORIENTATION_RIGHT;
				}
				else
				{
					output_orientation = ORIENTATION_LEFT;
				} // if
			} // if
		} // if

		return (output_orientation);

	} // orientation


	// ************************************************************************

	/**
	 * Berechnet die Dreiecksflaeche mit Vorzeichen. Sei source der erste und
	 * target der zweite Eingabepunkt. Die Gerade g durch source und target ist
	 * in Richtung von source nach target orientiert. Das Ergebnis ist positiv,
	 * wenn das Dreieck (source, target, this) die positive Folge der
	 * Dreiecksecken ist (d.h. die entgegen dem Uhrzeigersinn), sonst ist es
	 * negativ. Das Ergebnis ist gleich 0, wenn this auf der Geraden g liegt.
	 * 
	 * @return die Dreiecksflaeche mit Vorzeichen
	 */

	public float signedArea(
			Point2 input_source,
			Point2 input_target)
	{
		return ((_det_ab1(input_source.x, input_source.y, input_target.x,
				input_target.y, x, y).divide(BigRational.valueOf(2)))
				.floatValue());

	} // signedArea


	// ************************************************************************

	/**
	 * Berechnet das Quadrat des euklidischen Abstands zum Nullpunkt.
	 * 
	 * @return das Quadrat des euklidischen Abstands
	 * 
	 * @see #distance
	 */

	public double squareDistance()
	{
		return (x.square().add(y.square()).doubleValue());

	} // squareDistance


	// ********************************

	/**
	 * Berechnet das Quadrat des euklidischen Abstands zum Eingabepunkt.
	 * 
	 * @return das Quadrat des euklidischen Abstands
	 * 
	 * @see #distance
	 */

	public double squareDistance(
			Point2 input_point)
	{
		BigRational delta_x = x.subtract(input_point.x);
		BigRational delta_y = y.subtract(input_point.y);

		return (delta_x.square().add(delta_y.square()).doubleValue());

	} // squareDistance


	// ********************************

	/**
	 * Gibt die quadratische Distanz zu der Geraden zurueck, die durch die zwei
	 * gegebenen Punkte geht.
	 */

	public double squareDistance(
			Point2 p,
			Point2 q)
	{
		return (new Line2(p, q)).squareDistance(this);

	} // squareDistance


	// ************************************************************************

	/**
	 * Erzeugt einen anja.geom.Point2 aus dem Punkt und gibt ihn zurueck.
	 */

	public anja.geom.Point2 toGeom()
	{
		return (new anja.geom.Point2(x.doubleValue(), y.doubleValue()));

	} // toGeom


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation des Punkts.
	 * 
	 * @return die textuelle Repraesentation
	 */

	public String toString()
	{
		return "(" + FloatUtil.floatToString(x.floatValue()) + ", "
				+ FloatUtil.floatToString(y.floatValue()) + ")";

	} // toString


	// ************************************************************************

	/**
	 * Sichert den Punkt in eine Datei.
	 */

	public void save(
			DataOutputStream dos)
	{
		try
		{
			dos.writeUTF("Point2");
			dos.writeFloat(x.floatValue());
			dos.writeFloat(y.floatValue());
		}
		catch (IOException ioex)
		{}

	} // save


	// ************************************************************************

	/**
	 * Verschiebt den Punkt um die Eingabewerte.
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
		x = x.add(BigRational.valueOf(input_horizontal));
		y = y.add(BigRational.valueOf(input_vertical));

	} // translate


	// ********************************

	/**
	 * Verschiebt den Punkt um den Vektor vom Nullpunkt zum Eingabepunkt.
	 * 
	 * @param input_point
	 *            der Eingabepunkt
	 */

	public void translate(
			Point2 input_point)
	{
		x = x.add(input_point.x);
		y = y.add(input_point.y);

	} // translate


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/*
	* Berechnet den Arcustangens.
	*/

	private double _atan(
			BigRational input_value)
	{
		return (Math.atan(input_value.doubleValue()));

	} // _atan


	// ************************************************************************

	/**
	 * Berechnet die Determinante einer 3x3 Matrix, bei der die 3. Spalte gleich
	 * (1, 1, 1) ist.
	 */

	public static BigRational _det_ab1(
			BigRational input_0_0,
			BigRational input_0_1,
			BigRational input_1_0,
			BigRational input_1_1,
			BigRational input_2_0,
			BigRational input_2_1)
	{
		return ((input_0_0.multiply(input_1_1))
				.add(input_0_1.multiply(input_2_0))
				.add(input_1_0.multiply(input_2_1))
				.subtract(input_1_1.multiply(input_2_0))
				.subtract(input_0_0.multiply(input_2_1)).subtract(input_0_1
				.multiply(input_1_0)));

	} // _det_ab1


	// ************************************************************************

	/*
	* Untersucht die Lage von input_number zum geschlossenen Intervall von
	* input_bound1 bis input_bound2. Das Ergebnis ist
	* LIES_BEFORE	- wenn die Zahl vor dem Intervall liegt
	* LIES_ON		- wenn die Zahl im Intervall liegt
	* LIES_BEHIND	- wenn die Zahl hinter dem Intervall liegt
	* _ZERO_INTERVAL	- wenn die Intervallgrenzen gleich sind
	*/

	private int _inspectIntervall(
			BigRational number,
			BigRational bound1,
			BigRational bound2)
	{
		int b1_compare_b2 = bound1.compareTo(bound2);

		if (b1_compare_b2 == 0) // bound1 = bound2
			return (_ZERO_INTERVAL);

		int n_compare_b1 = number.compareTo(bound1);

		if (b1_compare_b2 == -1)
		{
			// bound1 < bound2

			if (n_compare_b1 == -1) // number < bound1
				return (LIES_BEFORE);
			else if (n_compare_b1 == 0) // number = bound1
				return (LIES_ON);
			else if (number.compareTo(bound2) <= 0)
				return (LIES_ON); // number <= bound2
			else
				return (LIES_BEHIND); // number > bound2
		}
		else
		{
			// bound1 > bound2

			if (n_compare_b1 == 1) // number > bound1
				return (LIES_BEFORE);
			else if (n_compare_b1 == 0) // number = bound1
				return (LIES_ON);
			else if (number.compareTo(bound2) >= 0)
				return (LIES_ON); // number >= bound2
			else
				return (LIES_BEHIND); // number < bound2
		} // if

	} // _inspectIntervall

	// ************************************************************************

} // class Point2

