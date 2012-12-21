package anja.geom;


import anja.util.CompareException;
import anja.util.KeyValueHolder;
import anja.util.StdComparitor;


/**
 * Vergleicher fuer Punkte des Typs Point2. PointComparitor kann Punkte bzgl.
 * ihrer X-Koordinaten, ihrer Y-Koordinaten oder einer angularen Ordnung
 * vergleichen. Mit <b>setOrder</b> wird eine Ordnung festgelegt, mittels
 * <b>order()</b> bekommt man die aktuelle Ordnung (der Standard ist
 * <i>X_ORDER</i>). <b>setAngularPoints</b> legt die Punkte fuer eine angulare
 * Sortierung fest. Ohne solche Punkte kann mit <b>setOrder(ANGULAR)</b> keine
 * angulare Ordnung gesetzt werden. Ein Beispiel:<br> Die Liste L enthalte
 * Punkte, die nach Winkeln um (0,0) mit der X-Achse sortiert werden sollen.
 * <pre>
 * PointComparitor PC=new PointComparitor(); 
 * PC.setAnglePoint(new Point2(0,0)); 
 * PC.setOrder(PointComparitor.ANGULAR);
 * L.sort(PC,Sorter.ASCENDING);
 * </pre>
 * 
 * @see "gishur.lang.Comparitor"
 */
public class PointComparitor
		extends StdComparitor
{

	/**
	 * Ordnung nach X-Koordinaten. Ein Punkt p1 liegt <b>vor</b> einem Punkt p2,
	 * falls:<ul><li><tt>p1.x&ltp2.x</tt>, also die X-Koordinate von p1
	 * kleiner ist als die von p2</li> <li><tt>p1.x==p2.x</tt> <i>und</i>
	 * <tt>p1.y&ltp2.y</tt>, also zwar die X-Koordinaten gleich sind, jedoch p1
	 * eine kleiner Y-Koordinate als p2 hat</li></ul> Ansonsten liegt p1 <b>nach</b>
	 * p2, ausser es gilt <tt>p1.x==p2.x</tt> <i>und</i> <tt>p1.y==p2.y</tt> -
	 * dann ist natuerlich p1 gleich p2.
	 * 
	 * @see #setOrder(byte)
	 */
	public static final byte	X_ORDER			= 1;

	/**
	 * Ordnung nach Y-Koordinaten. Ein Punkt p1 liegt <b>vor</b> einem Punkt p2,
	 * falls:<ul><li><tt>p1.y&ltp2.y</tt>, also die Y-Koordinate von p1
	 * kleiner ist als die von p2</li><li> <tt>p1.y==p2.y</tt> <i>und</i>
	 * <tt>p1.x&ltp2.x</tt>, also zwar die Y-Koordinaten gleich sind, jedoch p1
	 * eine kleiner X-Koordinate als p2 hat</li></ul> Ansonsten liegt p1 <b>nach</b>
	 * p2, ausser es gilt <tt>p1.x==p2.x</tt> <i>und</i> <tt>p1.y==p2.y</tt> -
	 * dann ist natuerlich p1 gleich p2.
	 * 
	 * @see #setOrder(byte)
	 */
	public static final byte	Y_ORDER			= 2;

	/**
	 * Angulare Ordnung bzueglich eines oder zweier weiterer Punkte, die man
	 * mittels <b>setAnglePoints</b> setzen kann. Wird nur ein Punkt angegeben,
	 * so wird der Winkel eines Punktes bzgl. des Strahls parallel zur X-Achse
	 * und beginnend in diesem Punkt angegeben, ansonsten bzgl. des Strahls
	 * beginnend im ersten Punkt in Richtung des zweiten Punktes. Ein Punkt p1
	 * liegt <b>vor</b> einem Punkt p2, falls:<ul><li> p1 einen kleineren
	 * Winkel als p2 hat</li><li>p1 und p2 zwar die gleichen Winkel haben, jedoch
	 * p1 naeher am Bezugspunkt als p2 liegt.</li></ul> Ansonsten liegt p1
	 * <b>nach</b> p2, ausser es gilt <tt>p1.x==p2.x</tt> <i>und</i>
	 * <tt>p1.y==p2.y</tt> - dann ist natuerlich p1 gleich p2.
	 * 
	 * @see #setOrder(byte)
	 * @see #setAnglePoints(Point2,Point2)
	 * @see #setAnglePoint(Point2)
	 */
	public static final byte	ANGULAR			= 3;

	/**
	 * Die Punkte werden bezueglich des Abstandes zu einem Bezugspunkt geordnet.
	 * Der Bezugspunkt wird mit setReferencePoint gesetzt. Haben die zu
	 * vergleichenden Punkte denselben Abstand zum Bezugspunkt, wird nach
	 * X-Order verglichen.
	 * @see #setOrder(byte)
	 * @see #setReferencePoint(Point2)
	 */
	public static final byte	DISTANCE_ORDER	= 4;

	/** Die gesetzte Ordnung; die Standardeinstellung ist X_ORDER. */
	private byte				_order			= X_ORDER;

	/** Die Bezugspunkte bei angularer Ordnung. */
	private Point2				_anglePoint		= null, _anglePoint2 = null;

	/** Bezugspunkt bei Distanz-Ordnung. */
	private Point2				_distancePoint	= null;


	// Konstruktion
	// ============

	/**
	 * Leerer Konstruktor.
	 */
	public PointComparitor()
	{
		super();
	}


	/**
	 * Konstruktor, der gleich die Ordnung einstellt.
	 * 
	 * @param order
	 *            Die Ordnung
	 */
	public PointComparitor(
			byte order)
	{
		super();
		setOrder(order);
	}


	/**
	 * Konstruktor, der gleich die Ordnung order einstellt und falls
	 * order==ANGULAR bzw ==DISTANCE_ORDER die entsprechenden Bezugspunkte
	 * einstellt.
	 * 
	 * @param order
	 *            Die Ordnung
	 * @param p
	 *            Der Bezugspunkt
	 */
	public PointComparitor(
			byte order,
			Point2 p)
	{
		super();
		if (order == ANGULAR)
			setAnglePoint(p);
		if (order == DISTANCE_ORDER)
			setReferencePoint(p);
		setOrder(order);
	}


	/**
	 * Überschreibt {@link java.lang.Object#toString()}.
	 * 
	 * @return Textuelle Repräsentation des Objekts
	 */
	public String toString()
	{
		String s = "unknown order";
		switch (_order)
		{
			case X_ORDER:
				s = "x-order";
				break;
			case Y_ORDER:
				s = "y-order";
				break;
			case ANGULAR:
				s = "angular order,pivot=" + _anglePoint.toString();
				if (_anglePoint2 != null)
					s += ",sidepoint=" + _anglePoint2.toString();
				break;
			case DISTANCE_ORDER:
				s = "distance-order,distance-point="
						+ _distancePoint.toString();
				break;
		}
		return getClass().getName() + "[" + s + "]";
	}


	/**
	 * Liefert die gesetzte Ordnung zurück.
	 *  
	 * @return X_ORDER,Y_ORDER oder ANGULAR
	 * 
	 * @see #X_ORDER
	 * @see #Y_ORDER
	 * @see #ANGULAR
	 * @see #DISTANCE_ORDER
	 *
	 */
	public byte order()
	{
		return _order;
	}


	/**
	 * Setzt die Ordnung. Wird ANGULAR gesetzt, so muss <b>vorher</b> mittels
	 * setAnglePoint mindestens ein Bezugspunkt gesetzt worden sein!
	 * 
	 * @param order
	 *            entweder X_ORDER,Y_ORDER oder ANGULAR (sonst passiert nichts)
	 *            
	 * @see #X_ORDER
	 * @see #Y_ORDER
	 * @see #ANGULAR
	 * @see #DISTANCE_ORDER
	 * 
	 */
	public void setOrder(
			byte order)
	{
		if (order == X_ORDER)
			_order = X_ORDER;
		if (order == Y_ORDER)
			_order = Y_ORDER;
		if ((order == ANGULAR) && (_anglePoint != null))
			_order = ANGULAR;
		if (order == DISTANCE_ORDER)
		{
			_order = DISTANCE_ORDER;
			if (_distancePoint == null)
				_distancePoint = new Point2(0, 0);
		}
	}


	/**
	 * Setzt zwei Bezugspunkte fuer Angulare Ordnung. Die angluare Ordnung
	 * selbst wird aber noch nicht gesetzt.
	 * 
	 * @param p1
	 *            Punkt, um den Winkel gemessen werden
	 * @param p2
	 *            Winkel werden vom Strahl beginnend in p1 mit Richtung p2 aus
	 *            gemessen ist p2==null, so verläuft der Strahl parallel zur
	 *            X-Achse.
	 *            
	 * @return wahr, falls p1!=null
	 * 
	 * @see #ANGULAR
	 */
	public boolean setAnglePoints(
			Point2 p1,
			Point2 p2)
	{
		_anglePoint = p1;
		_anglePoint2 = p2;
		if ((p1 == null) && (_order == ANGULAR))
			_order = X_ORDER;
		if (p1 == null)
			return false;
		else
			return true;
	}


	/**
	 * Setzt einen Bezugspunkt für angulare Ordnung.
	 * 
	 * @param p
	 *            Winkel werden vom Strahl beginnend in p und parallel zur
	 *            X-Achse aus gemessen.
	 *            
	 * @return wahr, falls p1!=null
	 * 
	 * @see #ANGULAR
	 */
	public boolean setAnglePoint(
			Point2 p)
	{
		return setAnglePoints(p, null);
	}


	/**
	 * Setzt den Referenzpunkt fuer Distanz-Ordnung, falls p!=null.
	 * 
	 * @param p
	 *            neuer Referenzpunkt
	 */
	public void setReferencePoint(
			Point2 p)
	{
		if (p != null)
			_distancePoint = p;
	}


	/**
	 * Vergleicht zwei Punkte bezüglich X_ORDER.
	 * 
	 * @param p1
	 *            erster Punkt
	 * @param p2
	 *            zweiter Punkt
	 *            
	 * @return SMALLER, falls p1 in der Ordnung <b>vor</b> p2 liegt; BIGGER,
	 *         falls p1 in der Ordnung <b>nach</b> p2 liegt; EQUAL, falls p1 und
	 *         p2 gleich sind
	 *         
	 * @see #X_ORDER
	 */
	public static short compareX(
			Point2 p1,
			Point2 p2)
	{
		if (p1.x < p2.x)
			return SMALLER;
		if (p1.x > p2.x)
			return BIGGER;
		// also implizit p1.x==p2.x
		if (p1.y < p2.y)
			return SMALLER;
		if (p1.y > p2.y)
			return BIGGER;
		return EQUAL;
	}


	/**
	 * Vergleicht zwei Punkte bezüglich Y_ORDER.
	 * 
	 * @param p1
	 *            erster Punkt
	 * @param p2
	 *            zweiter Punkt
	 *            
	 * @return SMALLER, falls p1 in der Ordnung <b>vor</b> p2 liegt; BIGGER,
	 *         falls p1 in der Ordnung <b>nach</b> p2 liegt; EQUAL, falls p1 und
	 *         p2 gleich sind
	 *         
	 * @see #Y_ORDER
	 */
	public static short compareY(
			Point2 p1,
			Point2 p2)
	{
		if (p1.y < p2.y)
			return SMALLER;
		if (p1.y > p2.y)
			return BIGGER;
		// also implizit p1.y==p2.y
		if (p1.x < p2.x)
			return SMALLER;
		if (p1.x > p2.x)
			return BIGGER;
		return EQUAL;
	}


	/**
	 * Vergleicht zwei Punkte bezüglich ANGULAR.
	 * 
	 * @param p1
	 *            erster Punkt
	 * @param p2
	 *            zweiter Punkt
	 *            
	 * @return SMALLER, falls p1 in der Ordnung <b>vor</b> p2 liegt; BIGGER,
	 *         falls p1 in der Ordnung <b>nach</b> p2 liegt; EQUAL, falls p1 und
	 *         p2 gleich sind
	 *         
	 * @see #ANGULAR
	 */
	public short compareAngle(
			Point2 p1,
			Point2 p2)
	{
		double a1, a2;
		if (_anglePoint2 == null)
		{
			a1 = _anglePoint.angle(p1);
			a2 = _anglePoint.angle(p2);
		}
		else
		{
			a1 = _anglePoint.angle(_anglePoint2, p1);
			a2 = _anglePoint.angle(_anglePoint2, p2);
		}
		if (a1 < a2)
			return SMALLER;
		if (a1 > a2)
			return BIGGER;
		// Also ist der Winkel gleich. Der Punkt, der naeher an _anglePoint liegt, ist 'kleiner'...
		a1 = _anglePoint.squareDistance(p1);
		a2 = _anglePoint.squareDistance(p2);
		if (a1 < a2)
			return SMALLER;
		if (a1 > a2)
			return BIGGER;
		return EQUAL;
	}


	/**
	 * Vergleicht zwei Punkte bezüglich DISTANCE_ORDER.
	 * 
	 * @param p1
	 *            erster Punkt
	 * @param p2
	 *            zweiter Punkt
	 *            
	 * @return SMALLER, falls p1 in der Ordnung <b>vor</b> p2 liegt; BIGGER,
	 *         falls p1 in der Ordnung <b>nach</b> p2 liegt; EQUAL, falls p1 und
	 *         p2 gleich sind
	 *         
	 * @see #DISTANCE_ORDER
	 * 
	 */
	public short compareDistance(
			Point2 p1,
			Point2 p2)
	{
		if (_distancePoint == null)
			return compareX(p1, p2);
		double d1 = _distancePoint.squareDistance(p1), d2 = _distancePoint
				.squareDistance(p2);
		if (d1 == d2)
			return compareX(p1, p2);
		if (d1 < d2)
			return SMALLER;
		else
			return BIGGER;
	}


	/**
	 * Vergleicht zwei Punkte (Point2-Objekte) miteinander. Je nach gesetzter
	 * Ordung wird compareX, compareY oder compareAngle aufgerufen.
	 * 
	 * @param p1
	 *            erster Punkt
	 * @param p2
	 *            zweiter Punkt
	 *            
	 * @return SMALLER, falls p1 in der Ordnung <b>vor</b> p2 liegt; BIGGER,
	 *         falls p1 in der Ordnung <b>nach</b> p2 liegt; EQUAL, falls p1 und
	 *         p2 gleich sind
	 *         
	 * @see #X_ORDER
	 * @see #Y_ORDER
	 * @see #ANGULAR
	 * @see #setOrder(byte)
	 * @see #compareX(Point2,Point2)
	 * @see #compareY(Point2,Point2)
	 * @see #compareAngle(Point2,Point2)
	 */
	public short compare(
			Point2 p1,
			Point2 p2)
	{
		switch (_order)
		{
			case X_ORDER:
				return compareX(p1, p2);
			case Y_ORDER:
				return compareY(p1, p2);
			case ANGULAR:
				return compareAngle(p1, p2);
			case DISTANCE_ORDER:
				return compareDistance(p1, p2);
		}
		throw new CompareException(this, p1, p2);
	}


	/**
	 * Überschreibt Comparitor.compare(). Akzeptiert werden Objekte des Types
	 * Point2 und KeyValueHolder, wobei bei KeyValueHolder-Objekten ihre
	 * key()-Werte ausgewertet werden (die also vom Typ Point2 sein sollten).
	 * 
	 * @param o1
	 *            erstes Objekt
	 * @param o2
	 *            zweites Objekt
	 *            
	 * @return SMALLER, falls o1 in der Ordnung <b>vor</b> o2 liegt; BIGGER,
	 *         falls o1 in der Ordnung <b>nach</b> o2 liegt; EQUAL, falls o1 und
	 *         o2 gleich sind
	 * 
	 * @see "gishur.lang.Comparitor#compare(...)"
	 */
	public short compare(
			Object o1,
			Object o2)
	{
		// KeyValueHolder-Objekte entpacken (nur eine Schicht!!!)...
		if (o1 instanceof KeyValueHolder)
			o1 = ((KeyValueHolder) o1).key();
		if (o2 instanceof KeyValueHolder)
			o2 = ((KeyValueHolder) o2).key();
		// Point-Objekte behandeln
		if ((o1 instanceof Point2) && (o2 instanceof Point2))
			return compare((Point2) o1, (Point2) o2);
		return super.compare(o1, o2);
	}
}
