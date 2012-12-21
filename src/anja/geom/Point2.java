package anja.geom;


import java.awt.BasicStroke;
import java.awt.Graphics2D;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

// ***************************************************************
import anja.util.Drawable;
import anja.util.FloatUtil;
import anja.util.GraphicsContext;
import anja.util.Matrix;
import anja.util.Matrix33;
import anja.util.Savable;
import anja.util.Vector3;


/**
 * Zweidimensionaler zeichenbarer Punkt. Die Methoden inspectCollinearPoint()
 * und orientation() verwenden eine <b>Epsilon-Umgebung</b>, um falsche
 * Ergebnisse aufgrund von Rechenungenauigkeiten zu verhindern.
 * 
 * @version 0.4 15.04.2003
 * @author Norbert Selle, Sascha Ternes
 */

public class Point2
		extends Point2D.Float
		implements Drawable, Savable, Cloneable, Serializable
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	/**
	 * der Punkt liegt auf der Geraden
	 */
	public final static int		ORIENTATION_COLLINEAR	= 1;

	/**
	 * Der Punkt liegt links der Geraden
	 */
	public final static int		ORIENTATION_LEFT		= 2;

	/**
	 * Der Punkt liegt rechts der Geraden
	 */
	public final static int		ORIENTATION_RIGHT		= 3;

	/**
	 * Der Punkt liegt nicht auf der zu einem Punkt entarteten Geraden
	 */
	public final static int		ORIENTATION_UNDEFINED	= 4;

	/**
	 * Der Punkt liegt vor dem Intervall auf der Geraden
	 */
	public static final int		LIES_BEFORE				= 11;

	/**
	 * Der Punkt liegt in dem Intervall auf der Geraden
	 */
	public static final int		LIES_ON					= 12;

	/**
	 * Der Punkt liegt hinter dem Intervall auf der Geraden
	 */
	public static final int		LIES_BEHIND				= 13;

	// Maschinenspezifische Epsilon-Umgebung
	private final static float	_EPSILON				= 2e-7f;

	// Konstanten fuer die Winkelberechnung
	private final static float	_PI_HALBE				= (float) Math.PI * 0.5f;
	private final static float	_PI_DREI_HALBE			= (float) Math.PI * 1.5f;
	private final static float	_PI_ZWEI				= (float) Math.PI * 2.0f;

	//************************************************************
	// private variables
	//************************************************************

	/**
	 * An arbitrary string to label the point
	 */
	protected String			_label					= null;

	/**
	 * A user defined value
	 */
	protected float				_value					= java.lang.Float.NaN;

	/**
	 * Preserve the hash-code of the point, even if the point changes
	 */
	protected int				_hashCode;

	// path primitive used in drawing
	private GeneralPath			_pointPath;

	// new drawing
	private Rectangle2D			_pointSquare;
	private Ellipse2D			_pointCircle;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt einen Punkt an der Position (0, 0).
	 */
	public Point2()
	{
		super(0, 0);
		_hashCode = super.hashCode();

	} // Point2


	// ********************************

	/**
	 * Liest den Punkt aus einer Datei ein.
	 * 
	 * @param dis
	 *            Der Inputstream, aus dem der Punkt gelesen wird
	 */
	public Point2(
			DataInputStream dis)
	{
		try
		{
			String type = dis.readUTF();

			if (type.compareTo("Point2") != 0)
				return;

			x = dis.readFloat();
			y = dis.readFloat();
		}
		catch (IOException ex)
		{}

		_hashCode = super.hashCode();

	} // Point2


	// ********************************

	/**
	 * Erzeugt einen Punkt mit der eingegebenen x- und y-Koordinate.
	 * 
	 * @param input_x
	 *            die x-Koordinate
	 * @param input_y
	 *            die y-Koordinate
	 */
	public Point2(
			float input_x,
			float input_y)
	{
		super(input_x, input_y);
		_hashCode = super.hashCode();

	} // Point2


	// ********************************

	/**
	 * Erzeugt einen Punkt aus double-Koordinaten.
	 * 
	 * @param inx
	 *            Double-X-Wert
	 * @param iny
	 *            Double-Y-Wert
	 */
	public Point2(
			double inx,
			double iny)
	{
		x = (float) inx;
		y = (float) iny;
		_hashCode = super.hashCode();

	}


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
		if (input_point == null)
		{
			x = 0;
			y = 0;
		}
		else
		{
			x = input_point.x;
			y = input_point.y;
		} // if
		_hashCode = super.hashCode();

	} // Point2


	// ********************************

	/**
	 * Erzeugt einen Punkt s1*p1 durch Skalarmultiplikation von einem Punkt.
	 * 
	 * @param s1
	 *            der Faktor vor p1
	 * @param p1
	 *            der Eingabepunkt
	 */
	public Point2(
			double s1,
			Point2 p1)
	{
		x = (float) (s1 * p1.x);
		y = (float) (s1 * p1.y);
		_hashCode = super.hashCode();

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
		x = (float) (s1 * p1.x + s2 * p2.x);
		y = (float) (s1 * p1.y + s2 * p2.y);
		_hashCode = super.hashCode();

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
		// Achtung: der Fall dass b gleich null ist muss gesondert
		//          behandelt werden, um Division durch null zu
		//          verhindern.

		double a = input_point.y - y;
		double b = input_point.x - x;
		double output_alpha;

		if (b > 0)
		{
			if (a >= 0)
			{
				// a >= 0, b > 0
				output_alpha = Math.atan(a / b);
			}
			else
			{
				// a < 0, b > 0
				output_alpha = _PI_ZWEI + Math.atan(a / b);
			} // if
		}
		else if (b == 0)
		{
			if (a > 0)
			{
				// a > 0, b = 0
				output_alpha = _PI_HALBE;
			}
			else if (a == 0)
			{
				// a = 0, b = 0;
				output_alpha = 0;
			}
			else
			{
				// a < 0, b = 0
				output_alpha = _PI_DREI_HALBE;
			} // if
		}
		else
		{
			// b < 0
			output_alpha = Math.PI + Math.atan(a / b);
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
	 * @return Der berechnete Winkel in Bogenmass
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

			output_angle = _PI_ZWEI + output_angle;
		} // if

		return (output_angle);

	} // angle


	// ************************************************************************

	/**
	 * Erzeugt eine Kopie und gibt sie zurueck.
	 * 
	 * @return Die Kopie des Punktes
	 */
	public Object clone()
	{
		return (new Point2(this));

	} // clone


	// ************************************************************************

	/**
	 * Berechnet den euklidischen Abstand zum Nullpunkt.
	 * 
	 * @return Der euklidische Abstand zum Nullpunkt
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
	 * Zeichnet den Punkt. Verwendete Stilarten: <b>CAP_SQUARE</b> malt ein
	 * Quadrat mit Seitenlge lineWidth <br><b>CAP_ROUND</b> malt eine
	 * Kreisscheibe mit Durchmesser lineWidth <br><b>CAP_BUTT</b> malt gar
	 * nichts.
	 * 
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gc
	 *            Der vorgegebene Graphicskontext
	 * 
	 * @see anja.util.Drawable#draw
	 * @see BasicStroke#CAP_SQUARE
	 * @see BasicStroke#CAP_ROUND
	 * @see BasicStroke#CAP_BUTT
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		g.setColor(gc.getForegroundColor());
		g.setStroke(gc.getStroke());

		/**
		 * Deferred initialization of geometric primitives here, which reduces
		 * memory overhead for those points that are never drawn - their
		 * geometry is never instantiated!
		 * 
		 */
		if (_pointPath == null)
		{
			_pointPath = new GeneralPath(GeneralPath.WIND_NON_ZERO);
		}

		// remove old points from the path primitive
		_pointPath.reset();

		if (gc.getEndCap() == BasicStroke.CAP_SQUARE)
		{
			float lh = gc.getLineWidth() / 2.0f;

			/*
			Point2D.Float dist = new Point2D.Float(lh,lh);
			Point2D.Float zero = new Point2D.Float(0,0);
			     
			try 
			{
			   g.getTransform().inverseTransform(dist, dist);
			   g.getTransform().inverseTransform(zero, zero);
			} catch(NoninvertibleTransformException ex) {}; 
			
			float lhx = dist.x - zero.x;
			float lhy = dist.y - zero.y;*/

			_pointPath.moveTo(x - lh, y - lh);
			_pointPath.lineTo(x + lh, y - lh);
			_pointPath.lineTo(x + lh, y + lh);
			_pointPath.lineTo(x - lh, y + lh);
			_pointPath.closePath();

			g.fill(_pointPath);

		}
		else if (gc.getEndCap() == BasicStroke.CAP_ROUND)
		{
			//GeneralPath _pointPath = new GeneralPath(GeneralPath.WIND_NON_ZERO);
			float lh = gc.getLineWidth() / 2.0f;

			/*
			Point2D.Float dist = new Point2D.Float(lh,lh);
			Point2D.Float zero = new Point2D.Float();
			try 
			{
			   g.getTransform().inverseTransform(dist, dist);
			   g.getTransform().inverseTransform(zero, zero);
			} catch(NoninvertibleTransformException ex) {}; 
			
			float lhx = dist.x - zero.x;
			float lhy = dist.y - zero.y;*/

			float lhxa = lh * .8f;
			float lhya = lh * .8f;

			_pointPath.moveTo(x, y - lh);
			_pointPath.curveTo(x - lhxa, y - lhya, x - lhxa, y - lhya, x - lh,
					y);

			_pointPath.curveTo(x - lhxa, y + lhya, x - lhxa, y + lhya, x, y
					+ lh);

			_pointPath.curveTo(x + lhxa, y + lhya, x + lhxa, y + lhya, x + lh,
					y);

			_pointPath.curveTo(x + lhxa, y - lhya, x + lhxa, y - lhya, x, y
					- lh);

			_pointPath.closePath();
			g.fill(_pointPath);
		}
	} // draw


	//**************************************************************************

	/**
	 * Renders a point as a visible circle/square of specified radius and with
	 * specified graphics attributes.
	 * 
	 * @param g2d
	 *            as usual
	 * @param gc
	 *            rendering attributes
	 * @param radius
	 *            circle radius, in pixels
	 * @param square
	 *            set to <code><b>true</b></code> for a square point
	 * @param pixelSize
	 *            The size of one pixel in world-coordinate units. Must be
	 *            supplied by a rendering routine. Otherwise just use 1.0
	 * 
	 */
	public void draw(
			Graphics2D g2d,
			GraphicsContext gc,
			int radius,
			double pixelSize,
			boolean square)
	{
		g2d.setColor(gc.getForegroundColor());
		g2d.setStroke(gc.getStroke());

		if (square)
		{
			/**
			 * Deferred initialization of geometric primitives here, which
			 * reduces memory overhead for those points that are never drawn -
			 * their geometry is never instantiated!
			 * 
			 */
			if (_pointSquare == null)
			{
				_pointSquare = new Rectangle2D.Double();
			}

			_pointSquare.setRect(x - radius * pixelSize,
					y - radius * pixelSize, 2 * radius * pixelSize, 2 * radius
							* pixelSize);

			g2d.fill(_pointSquare);
		}
		else
		{
			if (_pointCircle == null)
			{
				_pointCircle = new Ellipse2D.Double();
			}

			_pointCircle
					.setFrame(x - radius * pixelSize, y - radius * pixelSize, 2
							* radius * pixelSize, 2 * radius * pixelSize);

			g2d.fill(_pointCircle);
		}
	}


	// ************************************************************************

	/**
	 * Testet ob der Inhalt gleich dem Inhalt des Eingabepunktes ist.
	 * 
	 * Ist der Eingabepunkt ein null value, so wird false zurueckgegeben.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return true bei Gleichheit, sonst false
	 */
	public boolean equals(
			Point2 input_point)
	{
		boolean output_is_equal = false;

		if (input_point != null)
		{
			output_is_equal = (x == input_point.x) && (y == input_point.y);
		} // if

		return (output_is_equal);

	} // equals


	// ************************************************************************

	/**
	 * Points Equal-Methode, die mit Epsilon-Umgebung arbeitet.
	 * 
	 * @param p1
	 *            Der zu vergleichende Eingabepunkt
	 * @param epsilon
	 *            Die fuer den Vergleich gueltige Epsilon-Umgebung
	 * 
	 * @return true falls gleich, false sonst
	 */
	public boolean equals(
			Point2 p1,
			double epsilon)
	{
		return (Math.abs(this.x - p1.x) <= epsilon)
				&& (Math.abs(this.y - p1.y) <= epsilon);
	} // pointsAreEpsilonEqual


	/**
	 * True, falls p,q1,q1 kollinear sind UND p im Intervall [q1,q2] liegt.
	 * 
	 * @param q1
	 *            Linke Intervallgrenze
	 * @param q2
	 *            Rechte Intervallgrenze
	 * 
	 * @return true falls im Intervall, false sonst
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
	 * der zweite das Ende eines Abschnitts auf der Geraden. <br>Liegt der Punkt
	 * innerhalb der <b>Epsilon-Umgebung</b> eines Eingabepunktes, so wird er
	 * als auf dem Abschnitt befindlich betrachtet. Sind die beiden
	 * Eingabepunkte gleich, so wird LIES_ON ausgegeben, falls der Punkt gleich
	 * den Eingabepunkten ist, ansonsten immer LIES_BEFORE.<br> <b>
	 * Vorbedingung:</b><br> - die drei Punkte sind kollinear
	 * 
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
		float delta_x = input_target.x - input_source.x;
		float delta_y = input_target.y - input_source.y;
		int output_position = LIES_BEHIND;

		// Eingabepunkte gleich ?
		if (input_source.equals(input_target))
		{
			if (this.equals(input_source))
				return LIES_ON;
			else
				return LIES_BEFORE;
		}

		// Eingabepunkte unterschiedlich, normale Analyse

		// Unterscheidung zwischen flacher und steiler Gerade, wichtig fuer
		// numerische Problemfaelle.

		if (Math.abs(delta_x) >= Math.abs(delta_y)) // 1. flache Gerade
		{
			float x_abs = Math.abs(x);

			if (delta_x > 0)
			{ // 1.1 input_source liegt vor input_target
				if (x < input_source.x - (x_abs + Math.abs(input_source.x))
						* _EPSILON)
					output_position = LIES_BEFORE;

				else if (x <= input_target.x
						+ (x_abs + Math.abs(input_target.x)) * _EPSILON)
					output_position = LIES_ON;
			}
			else
			{ // 1.2 input_source liegt hinter input_target
				if (x > input_source.x + (x_abs + Math.abs(input_source.x))
						* _EPSILON)
					output_position = LIES_BEFORE;

				else if (x >= input_target.x
						- (x_abs + Math.abs(input_target.x)) * _EPSILON)
					output_position = LIES_ON;
			} // if
		}
		else
		// 2. steile Gerade
		{
			float y_abs = Math.abs(y);

			if (delta_y > 0)
			{ // 2.1 input_source liegt vor input_target
				if (y < input_source.y - (y_abs + Math.abs(input_source.y))
						* _EPSILON)
					output_position = LIES_BEFORE;

				else if (y <= input_target.y
						+ (y_abs + Math.abs(input_target.y)) * _EPSILON)
					output_position = LIES_ON;
			}
			else
			{ // 2.2 input_source liegt hinter input_target
				if (y > input_source.y + (y_abs + Math.abs(input_source.y))
						* _EPSILON)
					output_position = LIES_BEFORE;

				else if (y >= input_target.y
						- (y_abs + Math.abs(input_target.y)) * _EPSILON)
					output_position = LIES_ON;
			} // if
		} // if flach/steil

		return (output_position);

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
		return (box.contains(x, y));

	} // intersects


	// ************************************************************************

	/**
	 * Testet ob der Punkt auf der Geraden durch die Eingabepunkte liegt.
	 * 
	 * @param input_source
	 *            Ein Punkt auf der Geraden
	 * @param input_target
	 *            Ein weiterer Punkt auf der Geraden
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
	 * Teste, ob der Punkt im Winkelbereich PQR liegt, das heir zwischen den
	 * Strahlen QP und QR oder genauer gesagt: Zurueckgegeben wird true genau
	 * dann, wenn der Punkt rechts von der Geraden QP UND links von der Geraden
	 * QR liegt. Vorausgesetzt, da?der Winkel PQR kleiner oder gleich 180 Grad
	 * ist.
	 * 
	 * @param P
	 *            Erster Punkt des Winkelbereichs
	 * @param Q
	 *            Zweiter Punkt des Winkelbereichs (liegt am Winkel an)
	 * @param R
	 *            Dritter Punkt des Winkelbereichs
	 * 
	 * @return true wenn ja, sonst false
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
	 * 
	 * @param p
	 *            Der Vergleichspunkt
	 * 
	 * @return true wenn ja, sonst false
	 * 
	 */
	public boolean isSmaller(
			Point2 p)
	{
		return (x < p.x || (x == p.x && y < p.y));

	} //isSmaller


	// ************************************************************************

	/**
	 * Spiegelt den Punkt an der uebergebenen Geraden und liefert den
	 * gespiegelten Punkt.
	 * 
	 * @param line
	 *            die Spiegelungsgerade
	 * 
	 * @return Der gespiegelte Punkt
	 */
	public Point2 mirror(
			Line2 line)
	{

		Point2 m = line.plumb(this);
		m.x = m.x + m.x - x;
		m.y = m.y + m.y - y;
		return m;

	} // mirror


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
			float input_x,
			float input_y)
	{
		x = input_x;
		y = input_y;

	} // moveTo


	// ********************************

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
		x = (float) input_x;
		y = (float) input_y;

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
		if (input_point == null)
		{
			x = 0;
			y = 0;
		}
		else
		{
			x = input_point.x;
			y = input_point.y;
		} // if

	} // moveTo


	// ************************************************************************

	/**
	 * Testet ob der Punkt in der linken oder der rechten Halbebene oder auf der
	 * Geraden durch den ersten und zweiten Eingabepunkt liegt, die Gerade ist
	 * in Richtung vom ersten zum zweiten Eingabepunkt orientiert.<br>Sind die
	 * Eingabepunkte gleich so wird <b>ORIENTATION_COLLINEAR</b> zuruekgegeben,
	 * wenn der Eingabepunkt auf ihnen liegt, sonst wird <b>
	 * ORIENTATION_UNDEFINED</b> zuruekgegeben.
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
			double area = (((input_source.x - x) * (input_target.y - y)) - ((input_source.y - y) * (input_target.x - x)));
			double bound = (((Math.abs(input_source.x) + Math.abs(x)) * (Math
					.abs(input_target.y) + Math.abs(y))) + ((Math
					.abs(input_source.y) + Math.abs(y)) * (Math
					.abs(input_target.x) + Math.abs(x))));

			if (Math.abs(area) <= bound * _EPSILON)
			{
				output_orientation = ORIENTATION_COLLINEAR;
			}
			else
			{
				if (area < 0)
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


	/**
	 * Erweiterung zu orientation(Point2, Point2). Die source()- bzw.
	 * target()-Werte werden hierbei als Input für die orientation(Point2,
	 * Point2) eingesetzt.
	 * 
	 * @param bl
	 *            Ein BasicLine2-Objekt (not null)
	 * 
	 * @return Die Orientierung von diesem Punktobjekt zu der gegebenen
	 *         BasicLine2
	 */
	public int orientation(
			BasicLine2 bl)
	{
		return orientation(bl.source(), bl.target());
	}


	// ************************************************************************

	/**
	 * Berechnet die Dreiecksflaeche mit Vorzeichen, das Ergebnis ist vom Typ
	 * float. Sei source der erste und target der zweite Eingabepunkt. Die
	 * Gerade g durch source und target ist in Richtung von source nach target
	 * orientiert. Das Ergebnis ist positiv, wenn das Dreieck (source, target,
	 * this) die positive Folge der Dreiecksecken ist (d.h. die entgegen dem
	 * Uhrzeigersinn), sonst ist es negativ. Das Ergebnis ist gleich 0, wenn
	 * this auf der Geraden g liegt.
	 * 
	 * @param input_source
	 *            Erster Punkt auf der Geraden
	 * @param input_target
	 *            Zweiter Punkt auf der Geraden
	 * 
	 * @return die Dreiecksflaeche mit Vorzeichen
	 */
	public float signedArea(
			Point2 input_source,
			Point2 input_target)
	{
		return (Matrix.det(input_source.x, input_source.y, 1, input_target.x,
				input_target.y, 1, x, y, 1) / 2.0f);

	} // signedArea


	// ************************************************************************

	/**
	 * Berechnet das Quadrat des euklidischen Abstands zum Nullpunkt.
	 * 
	 * @return Das Quadrat des euklidischen Abstands
	 * 
	 * @see #distance
	 */
	public double squareDistance()
	{
		return ((x * x) + (y * y));

	} // squareDistance


	// ********************************

	/**
	 * Berechnet das Quadrat des euklidischen Abstands zum Eingabepunkt.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 * 
	 * @return Das Quadrat des euklidischen Abstands
	 * 
	 * @see #distance
	 */
	public double squareDistance(
			Point2 input_point)
	{
		double delta_x = x - input_point.x;
		double delta_y = y - input_point.y;

		return ((delta_x * delta_x) + (delta_y * delta_y));

	} // squareDistance


	// ********************************

	/**
	 * Gibt die quadratische Distanz zu der Geraden zurueck, die durch die zwei
	 * gegebenen Punkte geht.
	 * 
	 * @param p
	 *            Erster Punkt auf der Geraden
	 * @param q
	 *            Zweiter Punkt auf der Geraden
	 * 
	 * @return Das Quadrat des euklidischen Abstands zur Geraden
	 */
	public double squareDistance(
			Point2 p,
			Point2 q)
	{
		return (new Line2(p, q)).squareDistance(this);
	}


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation des Punkts.
	 * 
	 * @return Die textuelle Repraesentation
	 */
	public String toString()
	{
		return "(" + FloatUtil.floatToString(x) + ", "
				+ FloatUtil.floatToString(y) + ")";

	} // toString


	// ************************************************************************

	/**
	 * Sichert den Punkt in eine Datei
	 * 
	 * @param dio
	 *            Outputstream
	 */
	public void save(
			DataOutputStream dio)
	{
		try
		{
			dio.writeUTF("Point2");
			dio.writeFloat(x);
			dio.writeFloat(y);
		}
		catch (IOException ioex)
		{}
	}


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
			float input_horizontal,
			float input_vertical)
	{
		x = x + input_horizontal;
		y = y + input_vertical;

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
		if (input_point != null)
		{
			translate(input_point.x, input_point.y);
		} // if

	} // translate


	// ********************************

	/**
	 * Affine Abbildung des Punktes gemaumlszlig; der Transformationsmatrix
	 * <i>a</i>
	 * 
	 * <br>Version: 08.09.98<br> Author: Thomas Kamphans
	 * 
	 * @param a
	 *            Die Transformationsmatrix
	 */
	public void transform(
			Matrix33 a)
	{
		Vector3 p = new Vector3(x, y, 1);

		p.transform(a);

		x = p.x;
		y = p.y;

	} // transform


	/**
	 * Set the label of the point.
	 * 
	 * <br>Version: 16.07.01<br> Author: Thomas Kamphans
	 * 
	 * @param s
	 *            The new label
	 */
	public void setLabel(
			String s)
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
	public String getLabel()
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
	public void setValue(
			float f)
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
	public float getValue()
	{
		return _value;
	} // getValue


	/**
	 * Liefert einen fuer das Objekt eindeutigen HashCode.
	 * 
	 * Das Ueberschreiben der Methode Object.hashCode in Point2 wurde notwendig,
	 * da ab Java v 1.1 der HashCode abhaengig von den aktuellen Attribut-Werten
	 * des Objektes bestimt wird. Drawable-Objekte werden beim Einfuegen in eine
	 * Layer in einer Hashtable abgelegt; wird ein Punkt nun gedraggt, so
	 * aendert sich der zugehoerige Object und eine Lokalisierung schlaegt fehl.
	 * 
	 * <br>Version: 26.09.99<br> Author: Lars Kunert
	 * 
	 * @return Hashcode
	 */
	public int hashCode()
	{
		return _hashCode;
	}


	/**
	 * Der Punkt wird mit dem uebergebenen Punkt p in Bezug auf seine
	 * Y-Koordinate verglichen. Hierbei wird eine spezielle lexikographische
	 * Lesart verwendet, die verhindert, dass zwei Punkte mit gleicher
	 * Y-Koordinate gleich sind, wenn nicht wirklich der gleiche Punkt vorliegt.
	 * Die wird durch einen zusaezlichen Vergleich der X-Koordinaten erreicht.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @param p
	 *            der Punkt mit dem verglichen wird
	 * 
	 * @return Ein negativer Wert, Null oder ein positiver Wert wenn der Punkt
	 *         kleiner, gleich oder groesser als p ist.
	 */
	public int comparePointsLexY(
			Point2 p)
	{
		if (y < p.y)
			return -1; // erstes Kriterium: Y-Wert
		if (y > p.y)
			return 1;
		if (x < p.x)
			return -1; // zweites Kriterium: X-Wert
		if (x > p.x)
			return 1;
		return 0; //gleicher Punkt!!!
	}


	/**
	 * Der Punkt wird mit dem uebergebenen Punkt p in Bezug auf seine
	 * X-Koordinate verglichen. Hierbei wird eine spezielle lexikographische
	 * Lesart verwendet, die verhindert, dass zwei Punkte mit gleicher
	 * X-Koordinate gleich sind, wenn nicht wirklich der gleiche Punkt vorliegt.
	 * Die wird durch einen zusaezlichen Vergleich der Y-Koordinaten erreicht.
	 * 
	 * <br>Author: Marina Bachran
	 * 
	 * @param p
	 *            der Punkt mit dem verglichen wird
	 * 
	 * @return Ein negativer Wert, Null oder ein positiver Wert wenn der Punkt
	 *         kleiner, gleich oder groesser als p ist.
	 */
	public int comparePointsLexX(
			Point2 p)
	{
		if (x < p.x)
			return -1; // erstes Kriterium: X-Wert
		if (x > p.x)
			return 1;
		if (y < p.y)
			return -1; // zweites Kriterium: Y-Wert
		if (y > p.y)
			return 1;
		return 0; //gleicher Punkt!!!
	}


	/**
	 * Sortiert aufsteigend das Array der Punkte nach der x-Koordinate, dabei
	 * wird die lexikographische Lesart verwendet. Fuer die Sortierung wird der
	 * Quicksort-Algorithmus benutzt.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @param points
	 *            Ein Array von Punkten (Point2)
	 * 
	 */
	static public void sortPointsX(
			Point2[] points)
	{
		sortPoints(points, 0, points.length - 1, true, true);
	}


	/**
	 * Sortiert das Array der Punkte nach der x-Koordinate, dabei wird die
	 * lexikographische Lesart verwendet. Fuer die Sortierung wird der
	 * Quicksort-Algorithmus benutzt.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @param points
	 *            Ein Array von Punkten (Point2)
	 * @param asc
	 *            <b>true</b> gibt an, dass das Array aufsteigend sortiert wird.
	 *            Sonst wird das Array absteigend sortiert.
	 */
	static public void sortPointsX(
			Point2[] points,
			boolean asc)
	{
		sortPoints(points, 0, points.length - 1, true, asc);
	}


	/**
	 * Sortiert aufsteigend das Array der Punkte nach der Y-Koordinate, dabei
	 * wird die lexikographische Lesart verwendet. Fuer die Sortierung wird der
	 * Quicksort-Algorithmus benutzt.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @param points
	 *            Ein Array von Punkten (Point2)
	 * 
	 */
	static public void sortPointsY(
			Point2[] points)
	{
		sortPoints(points, 0, points.length - 1, false, true);
	}


	/**
	 * Sortiert das Array der Punkte nach der Y-Koordinate, dabei wird die
	 * lexikographische Lesart verwendet. Fuer die Sortierung wird der
	 * Quicksort-Algorithmus benutzt.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @param points
	 *            Ein Array von Punkten (Point2)
	 * @param asc
	 *            <b>true</b> gibt an, dass das Array aufsteigend sortiert wird.
	 *            Sonst wird das Array absteigend sortiert.
	 */
	static public void sortPointsY(
			Point2[] points,
			boolean asc)
	{
		sortPoints(points, 0, points.length - 1, false, asc);
	}


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Sortiert das Array der Punkte, dabei wird die lexikographische Lesart
	 * verwendet. Fuer die Sortierung wird der Quicksort-Algorithmus benutzt.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @param points
	 *            Ein Array von Punkten (Point2)
	 * @param left
	 *            das erste Element im Array
	 * @param right
	 *            das letzte Element im Array
	 * @param sortByX
	 *            bei <b>true</b>, falls die Punkte nach der X-Koordinate
	 *            sortiert werden sollen, sonst wird nach der Y-Koordinate
	 *            sortiert.
	 * @param asc
	 *            <b>true</b> gibt an, dass das Array aufsteigend sortiert wird.
	 *            Sonst wird das Array absteigend sortiert.
	 * 
	 */
	static private void sortPoints(
			Point2[] points,
			int left,
			int right,
			boolean sortByX,
			boolean asc)
	{
		int isAscending = (asc) ? 1 : -1;
		// ueerpruefng: gueltiger Bereich ???
		if ((left < 0) || (left >= points.length) || (right < 0)
				|| (right >= points.length))
		{
			System.err.println("Es ist ein Bereichsfehler aufgetreten!");
			return;
		}

		/* Bestimmung des Pivot-Elementes. Sollte es nicht das linke sein, so tausche es
		 * mit dem linken und man hat den Fall des linken Pivot-Elements */
		int pivotIndex = ((right - left) / 2) + left;
		if (pivotIndex != left)
		{
			Point2 exch = points[left];
			points[left] = points[pivotIndex];
			points[pivotIndex] = exch;
		}
		Point2 pivot = points[left];

		// Zerteilungsschritt der Vorlesung (leicht veraedert!)
		int l = left + 1;
		int r = right;
		while (l < r)
		{
			while ((l <= right)
					&& (sortByX && ((isAscending * points[l]
							.comparePointsLexX(pivot)) < 0))
					|| (!sortByX && ((isAscending * points[l]
							.comparePointsLexY(pivot)) < 0)))
			{
				l++;
			}
			while ((left < r)
					&& (sortByX && ((isAscending * points[r]
							.comparePointsLexX(pivot)) >= 0))
					|| (!sortByX && ((isAscending * points[r]
							.comparePointsLexY(pivot)) >= 0)))
			{
				r--;
			}
			if (l < r)
			{
				Point2 exch = points[l];
				points[l] = points[r];
				points[r] = exch;
			}
		}
		if (r > left)
		{
			Point2 exch = points[left];
			points[left] = points[r];
			points[r] = exch;
		}

		// Sind die Teilfelder nur 2 Elemente gro? so tausche bei Bedarf, sonst fuehre einen rekursiven Aufruf durch
		if ((r - 1) > left + 1)
		{
			sortPoints(points, left, r - 1, sortByX, asc);
		}
		else if (((r - 1) == left + 1)
				&& (sortByX && ((isAscending * points[left]
						.comparePointsLexX(points[r - 1])) > 0))
				|| (!sortByX && ((isAscending * points[left]
						.comparePointsLexY(points[r - 1])) > 0)))
		{
			Point2 exch = points[left];
			points[left] = points[r - 1];
			points[r - 1] = exch;
		}
		if (right > r + 2)
		{
			sortPoints(points, r + 1, right, sortByX, asc);
		}
		else if ((right == r + 2)
				&& (sortByX && ((isAscending * points[r + 1]
						.comparePointsLexX(points[right])) > 0))
				|| (!sortByX && ((isAscending * points[r + 1]
						.comparePointsLexY(points[right])) > 0)))
		{
			Point2 exch = points[r + 1];
			points[r + 1] = points[right];
			points[right] = exch;
		}
	}

} // class Point2

