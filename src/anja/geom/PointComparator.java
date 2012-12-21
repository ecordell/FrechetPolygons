package anja.geom;


import java.util.Comparator;


/**
 * <b>ACHTUNG: Diese Klasse wurde aus dem Projekt JavaAnimA portiert und sollte
 * nur verwendet werden, wenn JavaAnimA-Klassen, die ebenfalls portiert werden,
 * den <code>PointComparator</code> benoetigen. Fuer neuprogrammierte Klassen
 * sollte diese Klasse <i>NICHT</i> verwendet werden!</b>
 * 
 * PointComparator stellt Vergleichsfunktionen fuer Point2-Objekte bereit. Er
 * bietet die folgenden Vergleichskriterien an:
 * 
 * <ul compact><li>X_ORDER, Vergleich der X-Koordinaten</li><li>Y_ORDER,
 * Vergleich der Y-Koordinaten</li><li>DISTANCE_ORDER, der Abstand zu einem
 * Bezugspunkt</li></ul>
 * 
 * Das Defaultkriterium ist <tt> X_ORDER</tt>. Mit <tt>setDirection()</tt> kann
 * man die Reihenfolge fuer die Vergleichsfunktion <tt>compare()</tt> auf
 * <tt>ASCENDING</tt> fuer aufsteigend und <tt>DESCENDING</tt> fuer absteigend
 * setzen, der Defaultwert ist <tt>ASCENDING</tt>.<br>Ein PointComparator mit
 * der <tt>X_ORDER</tt> liefert als Ergebnis von <tt> compare( p1, p2 )</tt> mit
 * p1=(1, 1) und p2=(2, 2) bei der Reihenfolge <tt> ASCENDING</tt> den Wert -1
 * fuer 'liegt davor' und bei <tt>DESCENDING</tt> den Wert 1 fuer 'liegt
 * dahinter'.
 * 
 * Im folgenden Beispiel wird die Liste <tt> list </tt> von Punkten aufsteigend
 * nach ihrem Abstand zum Punkt (1, 1) sortiert.
 * 
 * <pre>
 * PointComparator comparator = new PointComparator();
 * comparator.setOrder(PointComparator.DISTANCE_ORDER ); 
 * comparator.setReferencePoint( new Point2(1, 1 ) ); 
 * list.sort( comparator );
 * </pre>
 * 
 * Um die Liste aus dem Beispiel in absteigender Reihenfolge zu sortieren,
 * genuegt es einfach die Reihenfolge auf <tt>DESCENDING</tt> zu setzen.
 * 
 * @version 1.0 19.06.1998
 */
public class PointComparator
		implements Comparator
{

	// ************************************************************************
	// Public Constants
	// ************************************************************************

	/**
	 * Bei der Reihenfolge <em>aufsteigend</em> liegt ein Punkt a in <tt>
	 * compare()</tt> <em>vor</em> einem Punkt b, wenn a gemaess der Ordnung
	 * <em>kleiner</em> ist als b.
	 * 
	 * @see #compare
	 * @see #setDirection
	 */
	public final static int		ASCENDING			= 342;

	/**
	 * Bei der Reihenfolge <em>absteigend</em> liegt ein Punkt a in <tt>
	 * compare()</tt> <em>vor</em> einem Punkt b, wenn a gemaess der Ordnung
	 * <em>groesser</em> ist als b.
	 * 
	 * @see #compare
	 * @see #setDirection
	 */
	public final static int		DESCENDING			= 343;

	/**
	 * Ordnung nach dem Abstand zu einem Bezugspunkt. Dieser kann mit <tt>
	 * setReferencePoint() </tt>gesetzt werden, sein Defaultwert ist der
	 * Koordinatenursprung. In dieser Ordnung ist ein Punkt kleiner, gleich oder
	 * groesser als ein anderer Punkt, wenn sein Abstand zum Bezugspunkt
	 * kleiner, gleich oder groesser ist als der des anderen Punkts
	 * 
	 * @see #setOrder
	 * @see #setReferencePoint
	 */
	public static final int		DISTANCE_ORDER		= 765;

	/**
	 * Ordnung nach x-Koordinaten, bei Gleichheit sind die y-Koordinaten
	 * massgeblich. Das Verhaeltnis von einem Punkt zu einem anderen ist <ul
	 * compact><li><b>kleiner</b> bei kleinerer x-Koordinate oder bei gleichen
	 * x-Koordinaten und kleinerer y-Koordinate</li><li><b>gleich</b> bei gleichen
	 * x- und y-Koordinaten</li><li><b>groesser</b> bei groesserer x-Koordinate
	 * oder bei gleichen x-Koordinaten und groesserer y-Koordinate</li></ul>
	 * 
	 * @see #setOrder
	 */
	public final static int		X_ORDER				= 766;

	/**
	 * Ordnung nach y-Koordinaten, bei Gleichheit sind die x-Koordinaten
	 * massgeblich. Das Verhaeltnis von einem Punkt zu einem anderen ist <ul
	 * compact><li><b>kleiner</b> bei kleinerer y-Koordinate oder bei gleichen
	 * y-Koordinaten und kleinerer x-Koordinate</li><li><b>gleich</b> bei gleichen
	 * y- und x-Koordinaten</li><li><b>groesser</b> bei groesserer y-Koordinate
	 * oder bei gleichen y-Koordinaten und groesserer x-Koordinate</ul>
	 * 
	 * @see #setOrder
	 */
	public static final int		Y_ORDER				= 767;

	// ************************************************************************
	// Private Constants
	// ************************************************************************

	/* die Rueckgabewerte der Vergleichsoperationen	*/
	private static final int	_LESS				= -1;
	private static final int	_EQUAL				= 0;
	private static final int	_GREATER			= 1;

	/** der Defaultwert der Reihenfolge			*/
	private static final int	_DEFAULT_DIRECTION	= ASCENDING;

	/** der Defaultwert der Ordnung			*/
	private static final int	_DEFAULT_ORDER		= X_ORDER;

	/** der Default-Bezugspunkt fuer die DISTANCE_ORDER	*/
	private static final Point2	_DEFAULT_REFERENCE	= new Point2(0, 0);

	// ************************************************************************
	// Public Variables
	// ************************************************************************

	// ************************************************************************
	// Private Variables
	// ************************************************************************

	/** die Reihenfolge, der Defaultwert ist ASCENDING		*/
	private int					_direction;

	/** die Ordnung, der Defaultwert ist X_ORDER			*/
	private int					_order;

	/** der Bezugspunkt bei Distanz-Ordnung, Default ist (0, 0)	*/
	private Point2				_reference;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt einen PointComparator mit der Ordnung X_ORDER und mit
	 * aufsteigender Reihenfolge ASCENDING.
	 * 
	 * @see #X_ORDER
	 * @see #ASCENDING
	 */
	public PointComparator()
	{

		this(_DEFAULT_ORDER, _DEFAULT_DIRECTION);

	} // PointComparator


	// ********************************              

	/**
	 * Erzeugt einen PointComparator mit der eingegebenen Ordnung und mit
	 * aufsteigender Reihenfolge ASCENDING.
	 * 
	 * @param input_order
	 *            Die vorgegebene Ordnung
	 *            
	 * @see #DISTANCE_ORDER
	 * @see #X_ORDER
	 * @see #Y_ORDER
	 * @see #ASCENDING
	 */
	public PointComparator(
			int input_order)
	{

		this(input_order, _DEFAULT_DIRECTION);

	} // PointComparator


	// ********************************              

	/**
	 * Erzeugt einen PointComparator mit der eingegebenen Ordnung und mit der
	 * eingegebenen Reihenfolge.
	 * 
	 * @param input_order
	 *            Die vorgegebene Ordnung
	 * @param input_direction
	 *            Die Reihenfolge
	 *            
	 * @see #DISTANCE_ORDER
	 * @see #X_ORDER
	 * @see #Y_ORDER
	 * @see #ASCENDING
	 * @see #DESCENDING
	 * 
	 */
	public PointComparator(
			int input_order,
			int input_direction)
	{

		setOrder(input_order);
		setDirection(input_direction);
		setReferencePoint(_DEFAULT_REFERENCE);

	} // PointComparator


	// ************************************************************************
	// Class methods
	// ************************************************************************

	/**
	 * Vergleicht zwei Punkte mit der DISTANCE_ORDER und dem Koordinatenursprung
	 * als Bezugspunkt.
	 * 
	 * @param input_first
	 *            Erster Punkt
	 * @param input_second
	 *            Zweiter Punkt
	 * 
	 * @return -1, 0 oder 1 wenn der Abstand des ersten Punktes zum Bezugspunkt
	 *         kleiner, gleich oder groesser ist als der des zweiten Punktes
	 *         
	 * @see #DISTANCE_ORDER
	 */
	static public int compareDistance(
			Point2 input_first,
			Point2 input_second)
	{

		return compareDistance(_DEFAULT_REFERENCE, input_first, input_second);

	} // compareDistance


	// ************************************************************************

	/**
	 * Vergleicht zwei Punkte mit der DISTANCE_ORDER bezueglich des eingegebenen
	 * Bezugspunkts.
	 * 
	 * @param input_reference
	 *            Der Bezugspunkt
	 * @param input_first
	 *            Erster Punkt
	 * @param input_second
	 *            Zweiter Punkt
	 * 
	 * @return -1, 0 oder 1 wenn der Abstand des ersten Punktes zum Bezugspunkt
	 *         kleiner, gleich oder groesser ist als der des zweiten Punktes
	 *         
	 * @see #DISTANCE_ORDER
	 */

	static public int compareDistance(
			Point2 input_reference,
			Point2 input_first,
			Point2 input_second)
	{

		double first_distance = input_reference.squareDistance(input_first);
		double second_distance = input_reference.squareDistance(input_second);

		if (first_distance < second_distance)
		{
			return _LESS;
		}
		else if (first_distance == second_distance)
		{
			return _EQUAL;
		}
		else
		{
			return _GREATER;
		} // if

	} // compareDistance


	// ************************************************************************

	/**
	 * Vergleicht zwei Punkte mit der X_ORDER.
	 * 
	 * @param input_first
	 *            Erster Punkt
	 * @param input_second
	 *            Zweiter Punkt
	 * 
	 * @return -1, 0 oder 1 wenn der erste Punkt kleiner, gleich oder größer in
	 *         der X_ORDER ist als der zweite Punkt
	 *  
	 * @see #X_ORDER       
	 */
	static public int compareX(
			Point2 input_first,
			Point2 input_second)
	{

		if (input_first.x < input_second.x)
		{
			return _LESS;
		}
		else if (input_first.x > input_second.x)
		{
			return _GREATER;
		}
		else
		{ // input_first.x == input_second.x
			if (input_first.y < input_second.y)
			{
				return _LESS;
			}
			else if (input_first.y > input_second.y)
			{
				return _GREATER;
			}
			else
			{ // input_first.y == input_second.y
				return _EQUAL;
			} // if
		} // if

	} // compareX


	// ************************************************************************

	/**
	 * Vergleicht zwei Punkte mit der Y_ORDER.
	 * 
	 * @param input_first
	 *            erster Punkt
	 * @param input_second
	 *            zweiter Punkt
	 * 
	 * @return -1, 0 oder 1 wenn der erste Punkt kleiner, gleich oder größer in
	 *         der Y_ORDER ist als der zweite Punkt
	 * 
	 * @see #Y_ORDER
	 */
	public static int compareY(
			Point2 input_first,
			Point2 input_second)
	{

		if (input_first.y < input_second.y)
		{
			return _LESS;
		}
		else if (input_first.y > input_second.y)
		{
			return _GREATER;
		}
		else
		{ // input_first.y == input_second.y
			if (input_first.x < input_second.x)
			{
				return _LESS;
			}
			else if (input_first.x > input_second.x)
			{
				return _GREATER;
			}
			else
			{ // input_first.x == input_second.x
				return _EQUAL;
			} // if
		} // if

	} // compareY


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Vergleicht zwei Punkte entsprechend der gesetzten Ordnung und Reihenfolge
	 * miteinander.
	 * 
	 * @param input_first
	 *            Erster Punkt
	 * @param input_second
	 *            Zweiter Punkt
	 * 
	 * @return -1, 0 oder 1 wenn der erste Punkt entsprechend der <em> Ordnung
	 *         und Reihenfolge </em> vor, auf gleicher Höhe oder hinter dem
	 *         zweitem Punkt liegt
	 * 
	 * @throws ClassCastException
	 *             wenn ein Eingabeobjekt die falsche Klasse hat
	 * 
	 * @see #setOrder
	 * @see #setDirection
	 */
	public int compare(
			Object input_first,
			Object input_second)
	{

		Point2 first = (Point2) input_first;
		Point2 second = (Point2) input_second;
		int result = _LESS;

		switch (_order)
		{
			case X_ORDER:
				result = compareX(first, second);
				break;
			case Y_ORDER:
				result = compareY(first, second);
				break;
			case DISTANCE_ORDER:
				result = compareDistance(_reference, first, second);
				break;
			default:
				System.err.println("PointComparator.compare(): unkown order");
				break;
		} // switch

		if (_direction == DESCENDING)
		{
			result = -result;
		} // if

		return result;

	} // compare


	// ************************************************************************

	/**
	 * Gibt die Reihenfolge zurück.
	 * 
	 * @return Die Reihenfolge
	 * 
	 * @see #compare
	 * @see #setDirection
	 * @see #ASCENDING
	 * @see #DESCENDING
	 */
	public int getDirection()
	{

		return _direction;

	} // getDirection


	// ************************************************************************

	/**
	 * Setzt die Reihenfolge.
	 * 
	 * @param input_direction
	 *            ASCENDING oder DESCENDING
	 * 
	 * @see #compare
	 * @see #getDirection
	 * @see #ASCENDING
	 * @see #DESCENDING
	 * 
	 */
	public void setDirection(
			int input_direction)
	{

		if ((input_direction != ASCENDING) && (input_direction != DESCENDING))
		{
			_direction = ASCENDING;
			throw new IllegalArgumentException("direction expected");
		}
		else
		{
			_direction = input_direction;
		} // if

	} // setDirection


	// ************************************************************************

	/**
	 * Gibt die Ordnung zurück.
	 * 
	 * @return Die Ordnung
	 * 
	 * @see #setOrder
	 * @see #DISTANCE_ORDER
	 * @see #X_ORDER
	 * @see #Y_ORDER
	 * 
	 */
	public int getOrder()
	{

		return _order;

	} // getOrder


	// ************************************************************************

	/**
	 * Setzt die Ordnung.
	 * 
	 * @param input_order
	 *            X_ORDER, Y_ORDER oder DISTANCE_ORDER
	 * 
	 * @see #getOrder
	 * @see #X_ORDER
	 * @see #Y_ORDER
	 * @see #DISTANCE_ORDER
	 * 
	 */
	public void setOrder(
			int input_order)
	{

		if ((input_order != DISTANCE_ORDER) && (input_order != X_ORDER)
				&& (input_order != Y_ORDER))
		{
			_order = X_ORDER;
			throw new IllegalArgumentException("order expected");
		}
		else
		{
			_order = input_order;
		} // if

	} // setOrder


	// ************************************************************************

	/**
	 * Gibt den Bezugspunkt der DISTANCE_ORDER zurück.
	 * 
	 * @return Der Bezugspunkt
	 * 
	 * @see #setReferencePoint
	 * @see #DISTANCE_ORDER
	 * 
	 */
	public Point2 getReferencePoint()
	{

		return _reference;

	} // getReferencePoint


	// ************************************************************************

	/**
	 * Setzt den Bezugspunkt der DISTANCE_ORDER.
	 * 
	 * @param input_point
	 *            Der Bezugspunkt
	 * 
	 * @see #getReferencePoint
	 * @see #DISTANCE_ORDER
	 *            
	 */
	public void setReferencePoint(
			Point2 input_point)
	{

		if (input_point == null)
		{
			_reference = _DEFAULT_REFERENCE;
			throw new NullPointerException("reference point expected");
		} // if

		_reference = input_point;

	} // setReferencePoint


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repräsentation.
	 * 
	 * @return Eine String-Ausgabe dieses Objekts
	 */
	public String toString()
	{

		String order_string = null;
		String direction_string = null;

		switch (_order)
		{
			case X_ORDER:
				order_string = "X-order";
				break;
			case Y_ORDER:
				order_string = "Y-order";
				break;
			case DISTANCE_ORDER:
				order_string = "Distance-order, reference-point=" + _reference;
				break;
			default:
				order_string = "illegal order";
				break;
		} // switch

		switch (_direction)
		{
			case ASCENDING:
				direction_string = "Ascending";
				break;
			case DESCENDING:
				direction_string = "Descending";
				break;
			default:
				direction_string = "illegal direction";
				break;
		} // switch

		return order_string + ", " + direction_string;

	} // toString

	// ************************************************************************
	// Private methods
	// ************************************************************************

	// ************************************************************************

} // PointComparator

