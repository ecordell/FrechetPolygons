package anja.geom;


import anja.util.ListItem;
import anja.util.SimpleList;


/**
 * PointsAccess dient zum einfachen Zugriff auf Listen, deren Elemente
 * Point2-Objekte sind. Insbesondere fuehrt PointsAccess Castings nach Point2
 * durch. <br>Nach dem Erzeugen eines PointsAccess greift man mit <tt>
 * nextPoint()</tt> bzw. <tt>prevPoint()</tt> auf den ersten bzw. letzten Punkt
 * zu und kann dann mit diesen beiden Methoden durch die Liste navigieren,
 * <tt>cyclicNextPoint()</tt> und <tt>cyclicPrevPoint()</tt> arbeiten analog
 * dazu. <br> Mit <tt> currentPoint() </tt> kann man mehrfach auf den aktuellen
 * Punkt zugreifen. <br><b>Achtung:</b> Wird der aktuelle Punkt eines
 * PointsAccess von anderer Seite aus und nicht mit der remove-Methode dieses
 * PointsAccess geloescht oder der aktuelle Punkt zwar korrekt mit der
 * remove-Methode geloescht, aber seine direkten Nachbarn von anderer Seite aus
 * manipuliert, dann liefern weitere Methoden-Aufrufe dieser Klasse nur noch
 * rein zufaellig korrekte Ergebnisse.
 * 
 * @version 1.1 01.08.1997
 * @author Norbert Selle
 */
public class PointsAccess
		implements java.io.Serializable
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	/** Status: Noch kein Zugriff erfolgt */
	private static final byte	_NEW				= 0;

	/** Status: Vor dem Listenanfang */
	private static final byte	_BEFORE				= 1;

	/** Status: Hinter dem Listenende */
	private static final byte	_BEHIND				= 2;

	/** Status: _current wurde geloescht */
	private static final byte	_DELETED			= 3;

	/** Status: _current ist ok */
	private static final byte	_VALID				= 4;

	// ************************************************************************
	// Variables
	// ************************************************************************

	/** Liste der Punkte */
	private SimpleList			_points;

	/** Status _NEW, _BEFORE, _BEHIND etc. */
	private byte				_status;

	/** Zuletzt zugegriffener Punkt */
	private ListItem			_current;

	/** Vorgaenger wenn der Status _DELETED ist */
	private ListItem			_prev;

	/** Nachfolger wenn der Status _DELETED ist */
	private ListItem			_next;

	/** Orientierung des Polygons */
	public final static byte	ORIENTATION_LEFT	= 1;
	public final static byte	ORIENTATION_RIGHT	= 2;
	private byte				_orientation		= ORIENTATION_LEFT;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt einen PointsAccess der an der gleichen Listenposition steht wie
	 * der Eingabe-PointsAccess.
	 * 
	 * @param input_access
	 *            Zu kopierendes Objekt
	 */
	public PointsAccess(
			PointsAccess input_access)
	{
		_points = input_access._points;
		_status = input_access._status;

		_current = input_access._current;
		_prev = input_access._prev;
		_next = input_access._next;

		_orientation = input_access._orientation;

	} // PointsAccess


	// ********************************

	/**
	 * Erzeugt einen PointsAccess für eine Liste von Point2-Objekten.
	 * 
	 * @param input_list
	 *            Die Eingabeliste
	 */
	public PointsAccess(
			SimpleList input_list)
	{
		_points = input_list;
		_status = _NEW;

	} // PointsAccess


	// ********************************

	/**
	 * Erzeugt einen PointsAccess für eine Point2List.
	 * 
	 * @param input_points
	 *            Die Eingabeliste
	 */
	public PointsAccess(
			Point2List input_points)
	{

		this(input_points.points());

	} // PointsAccess


	// ********************************

	/**
	 * Erzeugt einen PointsAccess für ein Polygon2.
	 * 
	 * @param input_polygon
	 *            Das Eingabepolygon
	 */
	public PointsAccess(
			Polygon2 input_polygon)
	{

		this(((Point2List) input_polygon).points());

	} // PointsAccess


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Gibt den aktuellen Punkt zurück, vorher muss mindestens ein Aufruf der
	 * Navigationsmethoden erfolgt sein, damit der aktuelle Punkt gesetzt worden
	 * ist, sonst wird null zurückgegeben, dito wenn der aktuelle Punkt gelöscht
	 * wurde.
	 * 
	 * @return den aktuellen Punkt, null wenn es keinen gibt
	 */
	public Point2 currentPoint()
	{
		if (_status == _VALID)
		{
			return ((Point2) _current.value());
		}
		else
		{
			return (null);
		} // if

	} // currentPoint


	// ************************************************************************

	/**
	 * Gibt den zyklischen Nachfolger des aktuellen Punkts zurück, den ersten
	 * Punkt wenn noch nicht auf einen Punkt zugegriffen wurde und null bei
	 * leerer Liste.
	 * 
	 * @return Der zyklische Nachfolger
	 */
	public Point2 cyclicNextPoint()
	{
		Point2 output_next = nextPoint();

		if ((output_next == null) && (!_points.empty()))
		{
			_status = _VALID;
			_current = _points.first();
			output_next = (Point2) _current.value();
		} // if

		return (output_next);

	} // cyclicNextPoint


	// ************************************************************************

	/**
	 * Gibt den zyklischen Vorgänger des aktuellen Punkts zurück, den letzten
	 * Punkt wenn noch nicht auf einen Punkt zugegriffen wurde und null bei
	 * leerer Liste.
	 * 
	 * @return Der zyklische Vorgänger
	 */
	public Point2 cyclicPrevPoint()
	{
		Point2 output_prev = prevPoint();

		if ((output_prev == null) && (!_points.empty()))
		{
			_status = _VALID;
			_current = _points.last();
			output_prev = (Point2) _current.value();
		} // if

		return (output_prev);

	} // cyclicPrevPoint


	// ************************************************************************

	/**
	 * Gibt den zyklischen Nachfolger des aktuellen Punkts zurück, den ersten
	 * Punkt wenn noch nicht auf einen Punkt zugegriffen wurde und null bei
	 * leerer Liste, aber im Gegensatz zu (cyclic)NextPoint ohne den internen
	 * Zeiger weiter zu setzen.
	 * 
	 * @return Der zyklische Nachfolger
	 */
	public Point2 getSucc()
	{
		ListItem output_next = null;

		switch (_status)
		{
			case _VALID:
				output_next = _current.next();
				break;
			case _DELETED:
				output_next = _next;
				break;
			case _NEW:
			case _BEFORE:
				if (!_points.empty())
				{
					output_next = _points.first();
				} // if
				break;
			case _BEHIND:
				return (null);
		} // switch

		if ((output_next == null) && (!_points.empty()))
		{
			output_next = _points.first();
		} // if

		return ((Point2) output_next.value());

	} // getSucc


	// ************************************************************************

	/**
	 * Gibt den zyklischen Nachfolger des Nachfolgers des aktuellen Punkts
	 * zurück, den ersten Punkt wenn noch nicht auf einen Punkt zugegriffen
	 * wurde und null bei leerer Liste, aber ohne den internen Zeiger weiter zu
	 * setzen.
	 * 
	 * @return Der zyklische Nach-Nachfolger
	 */
	public Point2 getSuccSucc()
	{
		ListItem output_next = null;

		switch (_status)
		{
			case _VALID:
				output_next = _current.next();
				if ((output_next == null) && (!_points.empty()))
				{
					output_next = _points.first();
				}//if
				output_next = output_next.next();
				break;
			case _DELETED:
				output_next = _next;
				break;
			case _NEW:
			case _BEFORE:
				if (!_points.empty())
				{
					output_next = _points.first();
				} // if
				break;
			case _BEHIND:
				return (null);
		} // switch

		if ((output_next == null) && (!_points.empty()))
		{
			output_next = _points.first();
		} // if

		return ((Point2) output_next.value());

	} // getSuccSucc


	// ************************************************************************

	/**
	 * Gibt den zyklischen Vorgänger des aktuellen Punkts zurück, den letzten
	 * Punkt wenn noch nicht auf einen Punkt zugegriffen wurde und null bei
	 * leerer Liste, aber im Gegensatz zu (cyclic)PrevPoint ohne den internen
	 * Zeiger weiter zu setzen.
	 * 
	 * @return Der zyklischen Vorgänger
	 */
	public Point2 getPrec()
	{
		ListItem output_prev = null;

		switch (_status)
		{
			case _VALID:
				output_prev = _current.prev();
				break;
			case _DELETED:
				output_prev = _prev;
				break;
			case _NEW:
			case _BEHIND:
				if (!_points.empty())
				{
					output_prev = _points.last();
				} // if
				break;
			case _BEFORE:
				return (null);
		} // switch

		if ((output_prev == null) && (!_points.empty()))
		{
			output_prev = _points.last();
		} // if

		return ((Point2) output_prev.value());

	} // getPrec


	// ************************************************************************

	/**
	 * Gibt den zyklischen Vorgänger des Vorgängers des aktuellen Punkts zurück,
	 * den letzten Punkt wenn noch nicht auf einen Punkt zugegriffen wurde und
	 * null bei leerer Liste, aber ohne den internen Zeiger weiter zu setzen.
	 * 
	 * @return Der zyklische Vor-Vorgänger
	 */
	public Point2 getPrecPrec()
	{
		ListItem output_prev = null;

		switch (_status)
		{
			case _VALID:
				output_prev = _current.prev();
				if ((output_prev == null) && (!_points.empty()))
				{
					output_prev = _points.last();
				}//if
				output_prev = output_prev.prev();
				break;
			case _DELETED:
				output_prev = _prev;
				break;
			case _NEW:
			case _BEHIND:
				if (!_points.empty())
				{
					output_prev = _points.last();
				} // if
				break;
			case _BEFORE:
				return (null);
		} // switch

		if ((output_prev == null) && (!_points.empty()))
		{
			output_prev = _points.last();
		} // if

		return ((Point2) output_prev.value());

	} // getPrecPrec


	// ************************************************************************

	/**
	 * Testet ob nextPoint() einen Punkt zurückgeben kann.
	 * 
	 * @return true wenn ja, sonst false
	 */
	public boolean hasNextPoint()
	{
		switch (_status)
		{
			case _VALID:
				return (_current.next() != null);
			case _DELETED:
				return (_next != null);
			case _NEW:
			case _BEFORE:
				return (!_points.empty());
			case _BEHIND:
				return (false);
		} // switch

		return (false);

	} // hasNextPoint


	// ************************************************************************

	/**
	 * Testet ob prevPoint() einen Punkt zurückgeben kann.
	 * 
	 * @return true wenn ja, sonst false
	 */
	public boolean hasPrevPoint()
	{
		switch (_status)
		{
			case _VALID:
				return (_current.prev() != null);
			case _DELETED:
				return (_prev != null);
			case _NEW:
			case _BEHIND:
				return (!_points.empty());
			case _BEFORE:
				return (false);
		} // switch

		return (false);

	} // hasPrevPoint


	// ************************************************************************

	/**
	 * Fügt den Punkt vor dem aktuellen Punkt ein. Exisitert kein aktueller
	 * Punkt, so wird die Methode wirkungslos verlassen.
	 */
	public void insertBefore(
			Point2 input_point)

	{
		if (currentPoint() != null)
		{
			_points.insert(_current, input_point);
		} // if

	} // insertBefore


	// ************************************************************************

	/**
	 * Fügt den Punkt hinter dem aktuellen Punkt ein. Exisitert kein aktueller
	 * Punkt, so wird die Methode wirkungslos verlassen.
	 * 
	 * @param input_point
	 *            Der einzufügende Punkt
	 */
	public void insertBehind(
			Point2 input_point)

	{
		if (currentPoint() != null)
		{
			ListItem succ = _current.next();

			if (succ == null)
			{
				_points.add(input_point);
			}
			else
			{
				_points.insert(succ, input_point);
			} // if

		} // if

	} // insertBehind


	// ************************************************************************

	/**
	 * Setzt den Zeiger auf Anfang der Liste zurück und gibt ersten Punkt der
	 * Liste aus.
	 * 
	 * @return Erstes Listenelement
	 */
	public Point2 reset()
	{

		if (!_points.empty())
		{
			_current = _points.first();
			_status = _NEW;
		}

		return ((Point2) _current.value());

	}


	//*************************************************************************

	/**
	 * Gibt den Nachfolger des zuletzt zugegriffenen Punkts zurück, den ersten
	 * Punkt wenn noch nicht auf einen Punkt zugegriffen wurde und null wenn
	 * kein erster beziehungsweise kein weiterer Punkt existiert.
	 * 
	 * @return Der Nachfolger
	 */
	public Point2 nextPoint()
	{
		switch (_status)
		{
			case _VALID:
				_current = _current.next();
				break;
			case _DELETED:
				_current = _next;
				break;
			case _NEW:
			case _BEFORE:
				if (!_points.empty())
				{
					_current = _points.first();
				} // if
				break;
			case _BEHIND:
				return (null);
		} // switch

		if (_current == null)
		{
			_status = _BEHIND;
			return (null);
		}
		else
		{
			_status = _VALID;
			return ((Point2) _current.value());
		} // if

	} // nextPoint


	// ************************************************************************

	/**
	 * Gibt den Vorgänger des zuletzt zugegriffenen Punkts zurück, den letzten
	 * Punkt wenn noch nicht auf einen Punkt zugegriffen wurde und null wenn
	 * kein letzter beziehungsweise kein voriger Punkt existiert.
	 * 
	 * @return Der Vorgänger
	 */
	public Point2 prevPoint()
	{
		switch (_status)
		{
			case _VALID:
				_current = _current.prev();
				break;
			case _DELETED:
				_current = _prev;
				break;
			case _NEW:
			case _BEHIND:
				if (!_points.empty())
				{
					_current = _points.last();
				} // if
				break;
			case _BEFORE:
				return (null);
		} // switch

		if (_current == null)
		{
			_status = _BEFORE;
			return (null);
		}
		else
		{
			_status = _VALID;
			return ((Point2) _current.value());
		} // if

	} // prevPoint


	// ************************************************************************

	/**
	 * Löscht den zuletzt zugegriffenen Punkt. Wenn noch nicht auf einen Punkt
	 * zugegriffen wurde oder er bereits gelöscht ist, dann wird eine Exception
	 * ausgelöst.
	 */
	public void removePoint()
	{
		if (_status == _VALID)
		{
			_prev = _current.prev();
			_next = _current.next();

			_points.remove(_current);
			_current = null;
			_status = _DELETED;
		}
		else
		{
			throw new java.util.NoSuchElementException("PointsAccess");
		} // if

	} // removePoint


	// ************************************************************************

	/**
	 * Vertauscht das aktuelle Element mit seinem Vorgänger, der aktuelles
	 * Element wird. Exisitert kein aktuelles oder Vorgängerelement, so wird die
	 * Methode wirkungslos verlassen.
	 * 
	 * @see #swapCyclicPred
	 */
	public void swapPred()
	{
		Point2 swap = currentPoint();

		if ((swap != null) && (hasPrevPoint()))
		{
			removePoint();
			prevPoint();
			_points.insert(_current, swap);

		} // if

	} // swapPred


	// ************************************************************************

	/**
	 * Move the current point to new Coordinates.
	 * 
	 * <br>Author: Joerg Matz, Hans-Peter Birneder
	 * 
	 * @param x
	 *            X Coordinate to move the point to
	 * @param y
	 *            Y Coordinate to move the point to
	 */
	public void moveCurrentPointTo(
			float x,
			float y)
	{
		Point2 _currentPoint = this.currentPoint();
		_currentPoint.moveTo(x, y);
	} // moveCurrentPointTo(x,y)


	// ************************************************************************

	/**
	 * Vertauscht das aktuelle Element mit seinem zyklischem Vorgaenger, der
	 * aktuelles Element wird. Exisitert kein aktuelles oder Vorgaengerelement,
	 * so wird die Methode wirkungslos verlassen.
	 * 
	 * @see #swapPred
	 */
	public void swapCyclicPred()
	{
		Point2 swap = currentPoint();

		if ((swap != null) && (_points.length() > 1))
		{
			removePoint();
			cyclicPrevPoint();
			_points.insert(_current, swap);

		} // if

	} // swapCyclicPred


	// ************************************************************************

	/**
	 * Setzt die Orientierung des Polygons, das durch den PointsAccess
	 * repräsentiert wird.
	 * 
	 * @param ori
	 *            Die neue Orientierung
	 */
	public void setOrientation(
			byte ori)
	{

		_orientation = ori;

	} // setOrientation


	// *************************************************************************

	/**
	 * Gibt die Orientierung des Polygons zurück, das durch den PointsAccess
	 * repräsentiert wird.
	 * 
	 * @return Die Orientierung
	 */
	public byte getOrientation()
	{

		return _orientation;

	} // getOrientation


	// *************************************************************************

	/**
	 * Testet, ob aktuelle Ecke _current eine reflexe Ecke ist.
	 * 
	 * <br>Author: Andrea Eubeler
	 * 
	 * @return true bei reflexer Ecke, false sonst
	 */
	public boolean isReflex()
	{
		boolean value = false;

		if (_status != _VALID)
			return false;

		float area = currentPoint().signedArea(getPrec(), getSucc());

		if (_orientation == ORIENTATION_LEFT)
		{
			value = (area < 0.0);
		}
		else
		{
			value = (area > 0.0);
		}

		return value;

	} // isReflex


	//*************************************************************************

	/**
	 * Erzeugt eine textuelle Repräsentation.
	 * 
	 * @return Die textuelle Repräsentation
	 */
	public String toString()
	{
		return ("PointsAccess <" + _points.length() + " | " + _statusToString()
				+ " | " + _itemToString(_prev) + " | "
				+ _itemToString(_current) + " | " + _itemToString(_next)
				+ "> [" + Point2List.listPoints(_points) + "]");

	} // toString


	// ************************************************************************

	/**
	 * Erzeugt zu einem ListItem der null ist oder einen Point2-beinhaltet eine
	 * textuelle Repräsentation.
	 * 
	 * @param input_item
	 *            Das Eingabeobjekt, von dem die Ausgabe erzeugt werden soll.
	 */
	private String _itemToString(
			ListItem input_item)
	{
		String result = null;

		if (input_item == null)
		{
			result = "null";
		}
		else
		{
			result = ((Point2) input_item.value()).toString();
		} // if

		return (result);

	} // _itemToString


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repräsentation von _status.
	 * 
	 * @return The status of this object
	 */
	private String _statusToString()
	{
		switch (_status)
		{
			case _NEW:
				return ("NEW");
			case _BEFORE:
				return ("BEFORE");
			case _BEHIND:
				return ("BEHIND");
			case _DELETED:
				return ("DELETED");
			case _VALID:
				return ("VALID");
			default:
				return ("error: unknown status");
		} // switch

	} // _statusToString

} // PointsAccess
