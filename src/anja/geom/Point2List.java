package anja.geom;


import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.io.Serializable;
import java.util.NoSuchElementException;

import anja.util.Drawable;
import anja.util.GraphicsContext;
import anja.util.ListItem;
import anja.util.Matrix33;
import anja.util.SimpleList;


/**
 * Point2List ist eine Liste von zweidimensionalen sortierbaren Punkten. Mit
 * PointsAccess-Objekten kann ohne Castings auf die Punkte zugegriffen werden.
 * Die Point2List ist <b>nicht</b> zeichenbar, zum Zeichnen dient
 * gui.Point2Module. Das Drawable-Interface dient nur zum besseren Handling,
 * z.B. in <code>apps.scene</code>.
 * 
 * @version 1.1 01.12.1997
 * @author Norbert Selle
 * @see anja.geom.PointsAccess
 */
public class Point2List
		implements Cloneable, Drawable, Serializable
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	// ************************************************************************
	// Variables
	// ************************************************************************

	/** Standard- GraphicsContext fuer die Punkte */
	protected GraphicsContext	_point_context;

	/** die Punkte */
	protected SimpleList		_points;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt _points und initialisiert sie mit Kopien der Punkte von
	 * input_points, ist input_points leer oder gleich null, so bleibt _points
	 * leer.
	 * 
	 * @param input_points
	 *            Die Eingabeliste
	 */
	private void _init(
			SimpleList input_points)
	{
		_points = new SimpleList();

		if (input_points != null)
		{
			PointsAccess origs = new PointsAccess(input_points);

			while (origs.hasNextPoint())
			{
				Point2 orig_point = origs.nextPoint();
				Point2 new_point = (Point2) orig_point.clone();

				addPoint(new_point);
			} // while
		} // if

	} // _init


	// ********************************

	/**
	 * Erzeugt eine leere Punktliste.
	 */
	public Point2List()
	{
		_init(null);

	} // Point2List


	// ********************************

	/**
	 * Erzeugt eine neue Punktliste als Kopie der Eingabeliste, die Punkte der
	 * neuen Liste sind <B>Kopien</B> - nicht etwa Referenzen - der Punkte der
	 * Eingabeliste.
	 * 
	 * @param input_points
	 *            Die Eingabeliste
	 */
	public Point2List(
			Point2List input_points)
	{
		_init(input_points._points);

	} // Point2List


	// ********************************

	/**
	 * Erzeugt eine neue Punktliste als skalierte Kopie der Eingabeliste. Die
	 * Skalierung mit dem Faktor s erfolgt bezüglich des eingegebenen Punktes p.
	 * 
	 * @param s
	 *            Der Skalierungsfaktor
	 * @param input_points
	 *            Die Eingabeliste
	 * @param p
	 *            Der Referenzpunkt für die Skalierung
	 */
	public Point2List(
			double s,
			Point2List input_points,
			Point2 p)
	{
		_points = new SimpleList();

		PointsAccess origs = new PointsAccess(input_points);

		while (origs.hasNextPoint())
			addPoint(new Point2(1 - s, p, s, origs.nextPoint()));

	} // Point2List


	// ************************************************************************
	// Class methods
	// ************************************************************************

	/**
	 * Erzeugt String mit Punktliste.
	 * 
	 * @param input_list
	 *            Die Eingabeliste
	 * 
	 * @return Die textuelle Repräsentation
	 */
	static public String listPoints(
			SimpleList input_list)
	{
		StringBuffer ret = new StringBuffer();
		PointsAccess pt = new PointsAccess(input_list);
		while (pt.hasNextPoint())
		{
			Point2 actual_point = pt.nextPoint();

			ret.append(actual_point.toString());
			if (pt.hasNextPoint())
				ret.append(" | ");

		} // while
		return ret.toString();

	} // listPoints


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Setzt den Standard-Graphics-Context für Punkte
	 * 
	 * @param context
	 *            Der GraphicsContext
	 * 
	 * @see anja.util.GraphicsContext
	 */
	public synchronized void setPointContext(
			GraphicsContext context)
	{
		_point_context = context;
	}


	// ************************************************************************

	/**
	 * Liefert den Standard-GraphicsContext fuer Punkte
	 * 
	 * @return Der eingestellte GraphicsContext
	 */
	public GraphicsContext getPointContext()
	{
		return (_point_context);
	}


	// ************************************************************************

	/**
	 * Hängt eine <B>Referenz</B> auf den Eingabepunkt an das Listenende.
	 * <BR><B>Vorbedingungen:</B><BR>der Eingabepunkt ist nicht null
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 */
	public synchronized void addPoint(
			Point2 input_point)
	{
		_points.add(input_point);

	} // addPoint


	// ************************************************************************

	/**
	 * Hängt einen neuen Punkt mit den Eingabekoordinaten an das Listenende.
	 * 
	 * @param input_x
	 *            x-Koordinate
	 * @param input_y
	 *            y-Koordinate
	 */
	public synchronized void addPoint(
			float input_x,
			float input_y)
	{
		Point2 new_point = new Point2(input_x, input_y);

		_points.add(new_point);

	} // addPoint


	// ************************************************************************

	/**
	 * Hängt einen neuen Punkt mit den Eingabekoordinaten an das Listenende.
	 * 
	 * @param input_x
	 *            x-Koordinate
	 * @param input_y
	 *            y-Koordinate
	 */
	public synchronized void addPoint(
			double input_x,
			double input_y)
	{
		Point2 new_point = new Point2(input_x, input_y);

		_points.add(new_point);

	} // addPoint


	// ************************************************************************

	/**
	 * Hängt <B>Referenzen</B> auf die Punkte der Eingabeliste sukzessive an das
	 * Listenende.
	 * 
	 * @param input_list
	 *            Eingabeliste der anzuhängenden Point2-Objekte
	 */
	public synchronized void appendCopy(
			SimpleList input_list)
	{
		SimpleList new_points = (SimpleList) input_list.clone();

		concat(new_points);

	} // appendCopy


	// ********************************

	/**
	 * Hängt <B>Referenzen</B> auf die Punkte der Eingabepunktliste sukzessive
	 * an das Listenende.
	 * 
	 * @param input_points
	 *            Eingabepunktliste der anzuhängenden Punkte
	 */
	public synchronized void appendCopy(
			Point2List input_points)
	{
		appendCopy(input_points.points());

	} // appendCopy


	// ************************************************************************

	/**
	 * Löscht alle Punkte aus der Liste.
	 */
	public void clear()
	{

		synchronized (_points)
		{
			_points.clear();
		} // synchronized

	} // clear


	// ************************************************************************

	/**
	 * Kopiert die Punktliste, die Punkte der Kopie sind <B>Kopien</B> - nicht
	 * etwa Referenzen - der Punkte des Originals.
	 * 
	 * @return Kopie der Punktliste
	 */
	public synchronized Object clone()
	{
		Point2List output_points = new Point2List(this);

		return (output_points);

	} // clone


	// ************************************************************************

	/**
	 * Berechne den Punkt aus der Point2List, der dem Argument p am nächsten
	 * ist.
	 * 
	 * @param p
	 *            Der Eingabepunkt
	 * 
	 * @return Der am nächsten liegende Punkt
	 */
	public Point2 closestPoint(
			Point2 p)
	{
		return closestPointAccess(p).currentPoint();
	} // closestPoint


	// ************************************************************************

	/**
	 * Berechne den Punkt aus der Point2List, der dem Argument p am nächsten
	 * ist, aber nur, wenn es überhaupt einen gibt, der naeher als maxdist dran
	 * ist.
	 * 
	 * @param p
	 *            Der Eingabepunkt
	 * @param maxdist
	 *            Die maximale Distanz
	 * 
	 * @return Der am nächsten liegende Punkt
	 */
	public Point2 closestPoint(
			Point2 p,
			double maxdist)
	{
		return closestPointAccess(p, maxdist).currentPoint();
	} // closestPoint


	// ************************************************************************

	/**
	 * Berechne den Punkt aus der Point2List, der dem Argument p am nächsten
	 * ist, abgesehen von dem Punkt ex, aber nur, wenn es überhaupt einen gibt,
	 * der näher als maxdist dran ist.
	 * 
	 * @param p
	 *            Der Eingabepunkt
	 * @param ex
	 *            Der Punkt, der ausgenommen werden soll
	 * @param maxdist
	 *            Die maximale Distanz
	 * 
	 * @return Der am nächsten liegende Punkt
	 */
	public Point2 closestPointExcept(
			Point2 p,
			Point2 ex,
			double maxdist)
	{
		return closestPointExceptAccess(p, ex, maxdist).currentPoint();
	} // closestPointExcept


	// ************************************************************************

	/**
	 * Wie closestPoint(Point2 p), aber liefert einen PointsAccess zurück.
	 * 
	 * @param p
	 *            Der Eingabepunkt
	 * 
	 * @return Ein PointsAccess auf das Ergebnis
	 */
	public PointsAccess closestPointAccess(
			Point2 p)
	{
		PointsAccess Access = new PointsAccess(this);
		if (empty())
			return Access;

		Point2 q = Access.nextPoint();
		PointsAccess rA = new PointsAccess(Access);
		double dist2, distcomp = p.squareDistance(q);

		while (Access.hasNextPoint())
		{
			q = Access.nextPoint();
			dist2 = p.squareDistance(q);
			if (dist2 < distcomp)
			{
				rA = new PointsAccess(Access);
				distcomp = dist2;
			}
		}
		return rA;
	} // closestPointAccess


	// ************************************************************************

	/**
	 * Wie closestPoint(Point2 p, double maxdist), aber liefert einen
	 * PointsAccess zurück.
	 * 
	 * @param p
	 *            Der Eingabepunkt
	 * @param maxdist
	 *            Die maximale Distanz
	 * 
	 * @return Ein PointsAccess auf das Ergebnis
	 */
	public PointsAccess closestPointAccess(
			Point2 p,
			double maxdist)
	{
		PointsAccess Access = new PointsAccess(this);
		if (empty())
			return Access;

		Point2 q = null;
		PointsAccess rA = new PointsAccess(Access);
		double dist2, distcomp = maxdist * maxdist;

		while (Access.hasNextPoint())
		{
			q = Access.nextPoint();
			dist2 = p.squareDistance(q);
			if (dist2 < distcomp)
			{
				rA = new PointsAccess(Access);
				distcomp = dist2;
			}
		}
		return rA;
	} // closestPointAccess


	// ************************************************************************

	/**
	 * Wie closestPointExcept(Point2 p, Point2 ex, double maxdist), aber liefert
	 * einen PointsAccess zurück.
	 * 
	 * @param p
	 *            Der Eingabepunkt
	 * @param ex
	 *            Der Punkt, der ausgenommen werden soll
	 * @param maxdist
	 *            Die maximale Distanz
	 * 
	 * @return Ein PointsAccess auf das Ergebnis
	 */
	public PointsAccess closestPointExceptAccess(
			Point2 p,
			Point2 ex,
			double maxdist)
	{
		PointsAccess Access = new PointsAccess(this);
		if (empty())
			return Access;

		Point2 q = null;
		PointsAccess rA = new PointsAccess(Access);
		double dist2, distcomp = maxdist * maxdist;

		while (Access.hasNextPoint())
		{
			q = Access.nextPoint();
			dist2 = p.squareDistance(q);
			if ((dist2 < distcomp) && (q != ex))
			{
				rA = new PointsAccess(Access);
				distcomp = dist2;
			}
		}
		return rA;
	} // closestPointExceptAccess


	/**
	 * Berechne den entferntesten Punkt der Point2List zum Argument p
	 * 
	 * @param p
	 *            Der Eingabepunkt
	 * 
	 * @return Der entfernteste Punkt
	 */
	public Point2 calcFarthestPoint(
			Point2 p)
	{
		double dist = 0;
		Point2List points = (Point2List) this.clone();
		int len = points.length();
		Point2 farthest = points.firstPoint();
		points.removeFirstPoint();

		for (int i = 0; i < len - 1; i++)
		{
			if (dist < p.distance(points.firstPoint()))
			{
				dist = p.distance(points.firstPoint());
				farthest = points.firstPoint();
			}
			points.removeFirstPoint();
		}
		return farthest;
	}//calcFarthestPoint


	/**
	 * Berechne die Enfernung des Argumentes p zum entferntesten Punkt der
	 * Point2List
	 * 
	 * @param p
	 *            Der Eingabepunkt
	 * 
	 * @return Die Distanz zum entferntesten Punkt
	 */
	public double calcFarthestDist(
			Point2 p)
	{
		return calcFarthestPoint(p).distance(p);
	}//calcFarthestDist


	// ************************************************************************

	// ************************************************************************

	/**
	 * Hängt die Elemente der Eingabeliste - die von der Klasse Point2 sein
	 * müssen - sukzessive um an das Listenende, die Eingabeliste ist danach
	 * leer.
	 * 
	 * @param input_list
	 *            Eingabeliste der umzuhängenden Point2-Objekte
	 */
	public synchronized void concat(
			SimpleList input_list)
	{
		_points.concat(input_list);

	} // concat


	// ********************************

	/**
	 * Hängt die Punkte der Eingabepunktliste sukzessive um an das Listenende,
	 * die Eingabepunktliste ist danach leer.
	 * 
	 * @param input_points
	 *            Eingabepunktliste der umzuhängenden Punkte
	 */
	public synchronized void concat(
			Point2List input_points)
	{
		concat(input_points.points());

	} // concat


	// ************************************************************************

	/**
	 * Zeichnet die Punkte. Vererbt von {@link anja.util.Drawable}.
	 * 
	 * @param g
	 *            Das Graphics-Objekt, in das gezeichnet wird
	 * @param gc
	 *            Der GraphicsContext
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{ //Bloss keine Punkte malen !!! Das wird bereits in Point2Module erledigt.
	} // draw


	// ************************************************************************

	/**
	 * Testet ob die Punktliste leer ist.
	 * 
	 * @return true wenn die Punktliste leer ist, sonst false
	 */
	public boolean empty()
	{
		return (_points.empty());

	} // empty


	// ************************************************************************

	/**
	 * Gibt den ersten Punkt zurück, null wenn die Liste leer ist.
	 * 
	 * @return Der erste Punkt oder null
	 */
	public Point2 firstPoint()
	{
		if (length() > 0)
		{
			return ((Point2) _points.firstValue());
		}
		else
		{
			return (null);
		} // if

	} // firstPoint


	// ************************************************************************

	/**
	 * Returns the index_th element of the list.
	 * 
	 * <br>Takes O(n) time
	 * 
	 * @param index
	 *            The index of the element
	 * 
	 * @return The point or null, if the list hasn't that much elements
	 */
	public Point2 get(
			int index)
	{
		Point2 p = null;

		if (index >= 0 && index < _points.length())
		{
			ListItem li = _points.first();

			for (int i = 0; i < index; ++i)
				li = li.next();

			p = (Point2) li.value();
		}

		return p;
	} //end get()


	// ************************************************************************

	/**
	 * Gibt das umschließende Rechteck zurück, null wenn die Punktliste leer
	 * ist.
	 * 
	 * @return Das umschliessende Rechteck oder null
	 */
	public Rectangle2D getBoundingRect()
	{
		Rectangle2D output_rectangle = null;

		if (!_points.empty())
		{
			float min_x = minimumX();
			float min_y = minimumY();

			output_rectangle = new Rectangle2D.Float(min_x, // x
					min_y, // y
					maximumX() - min_x, // width
					maximumY() - min_y); // height
		} // if

		return (output_rectangle);

	} // getBoundingRect


	// ************************************************************************

	/**
	 * Returns the index of a point/vertice in the list
	 * 
	 * @param p
	 *            The point
	 * 
	 * @return The index or -1, if p is not included
	 */
	public int indexOf(
			Point2 p)
	{
		Point2[] pa = toArray();
		for (int i = 0; i < pa.length; ++i)
		{
			if (pa[i].equals(p))
				return i;
		}

		return -1;
	} //end indexOf


	// ************************************************************************

	/**
	 * Fügt eine <B>Referenz</B> auf den dritten Eingabepunkt zwischen die
	 * ersten beiden Eingabepunkte ein, letztere müssen <em>Kopien</em> oder
	 * <em>Referenzen</em> von aufeinanderfolgenden Punkten oder dem ersten und
	 * dem letzten Punkt der Punktliste in beliebiger Reihenfolge enthalten, und
	 * die Punktliste muss mindestens zwei Punkte enthalten, ansonsten ist das
	 * Resultat der Funktion undefiniert.
	 * 
	 * @param input_first
	 *            einer der Punkte zwischen die eingefügt wird
	 * @param input_second
	 *            der andere der Punkte zwischen die eingefügt wird
	 * @param input_insert
	 *            der einzufügende Punkt
	 */
	public synchronized void insert(
			Point2 input_first,
			Point2 input_second,
			Point2 input_insert)
	{
		if (_points.length() >= 2)
		{
			ListItem current_item = _points.first();
			ListItem next_item;
			Point2 current_point;
			Point2 next_point;

			for (int count = 0; count < _points.length(); count++)
			{
				next_item = _points.cyclicRelative(current_item, 1);

				current_point = (Point2) current_item.value();

				if (current_point.equals(input_first))
				{
					next_point = (Point2) next_item.value();
					if (next_point.equals(input_second))
					{
						_points.insert(next_item, input_insert);
					}
					else
					{
						_points.insert(current_item, input_insert);
					} // if

					return;
				} // if

				current_item = next_item;

			} // while
		} // if

	} // insert


	// ************************************************************************

	/**
	 * Fügt eine <B>Referenz</B> auf den Eingabepunkt am Listenanfang ein.
	 * <BR><B>Vorbedingungen:</B><BR>Der Eingabepunkt ist nicht null
	 * 
	 * @param input_point
	 *            Der einzufügende Punkt
	 */
	public void insertFront(
			Point2 input_point)
	{
		_points.Push(input_point);

	} // insertFront


	// ********************************

	/**
	 * Fügt einen neuen Punkt mit den Eingabekoordinaten am Listenanfang ein.
	 * 
	 * @param input_x
	 *            x-Koordinate
	 * @param input_y
	 *            y-Koordinate
	 */
	public void insertFront(
			float input_x,
			float input_y)
	{
		Point2 new_point = new Point2(input_x, input_y);

		_points.Push(new_point);

	} // insertFront


	// ************************************************************************

	/**
	 * Testet ob ein oder mehrere Punkte im Rechteck enthalten sind.
	 * 
	 * @param box
	 *            Das zu testende Rechteck
	 * 
	 * @return true wenn ja, sonst false
	 * 
	 * @see anja.util.Drawable
	 */
	public boolean intersects(
			Rectangle2D box)
	{
		PointsAccess access = new PointsAccess(_points);

		while (access.hasNextPoint())
		{
			if (access.nextPoint().intersects(box))
			{
				return (true);
			} // if
		} // while

		return (false);

	} // intersects


	// ************************************************************************

	/**
	 * Gibt den letzten Punkt zurück, null wenn die Liste leer ist.
	 * 
	 * @return Der letzte Punkt oder null
	 */
	public Point2 lastPoint()
	{
		if (length() > 0)
		{
			return ((Point2) _points.lastValue());
		}
		else
		{
			return (null);
		} // if

	} // lastPoint


	// ************************************************************************

	/**
	 * Gibt die Anzahl der Punkte zurück.
	 * 
	 * @return Anzahl der Punkte
	 */
	public int length()
	{
		return (_points.length());

	} // length


	// ************************************************************************

	/**
	 * Erzeugt String mit Punktliste.
	 * 
	 * @return Textuelle Ausgabe aller Punkte
	 */
	public String listPoints()
	{
		return (listPoints(_points));

	} // listPoints


	// ************************************************************************

	/**
	 * Gibt die größte X-Koordinate der Punkte zurück.
	 * 
	 * @return The maximum x-value
	 * 
	 * @throws java.util.NoSuchElementException
	 */
	public float maximumX()
			throws NoSuchElementException
	{
		if (_points.empty())
		{
			throw new NoSuchElementException();
		} // if

		PointComparitor compare = new PointComparitor();

		compare.setOrder(PointComparitor.X_ORDER);

		return (((Point2) _points.max(compare).value()).x);

	} // maximumX()


	// ************************************************************************

	/**
	 * Gibt die kleinste X-Koordinate der Punkte zurück.
	 * 
	 * @return The minimum x-value
	 * 
	 * @throws java.util.NoSuchElementException
	 */
	public float minimumX()
			throws NoSuchElementException
	{
		if (_points.empty())
		{
			throw new NoSuchElementException();
		} // if

		PointComparitor compare = new PointComparitor();

		compare.setOrder(PointComparitor.X_ORDER);

		return (((Point2) _points.min(compare).value()).x);

	} // minimumX()


	// ************************************************************************

	/**
	 * Gibt die größte Y-Koordinate der Punkte zurück.
	 * 
	 * @return The maximum y-value
	 * 
	 * @throws java.util.NoSuchElementException
	 */

	public float maximumY()
			throws NoSuchElementException
	{
		if (_points.empty())
		{
			throw new NoSuchElementException();
		} // if

		PointComparitor compare = new PointComparitor();

		compare.setOrder(PointComparitor.Y_ORDER);

		return (((Point2) _points.max(compare).value()).y);

	} // maximumY()


	// ************************************************************************

	/**
	 * Gibt die kleinste Y-Koordinate der Punkte zurück.
	 * 
	 * @return The minimum y-value
	 * 
	 * @throws java.util.NoSuchElementException
	 */

	public float minimumY()
			throws NoSuchElementException
	{
		if (_points.empty())
		{
			throw new NoSuchElementException();
		} // if

		PointComparitor compare = new PointComparitor();

		compare.setOrder(PointComparitor.Y_ORDER);

		return (((Point2) _points.min(compare).value()).y);

	} // minimumY()


	// ************************************************************************

	/**
	 * Gibt eine <B>Referenz</B> auf die Liste der Punkte zurück.
	 * 
	 * @return Liste der Punkte
	 */
	public SimpleList points()
	{
		return (_points);

	} // points


	// ************************************************************************

	/**
	 * Löscht den ersten Punkt.
	 * 
	 * <BR><B>Vorbedingungen:</B><BR>die Punktliste ist nicht leer
	 */
	public void removeFirstPoint()
	{
		if (_points.empty())
		{
			throw new NoSuchElementException();
		}
		else
		{
			_points.remove(_points.first());
		} // if

	} // removeFirstPoint


	// ************************************************************************

	/**
	 * Löscht den letzten Punkt.
	 * 
	 * <BR><B>Vorbedingungen:</B><BR>die Punktliste ist nicht leer
	 */
	public void removeLastPoint()
	{
		if (_points.empty())
		{
			throw new NoSuchElementException();
		}
		else
		{
			_points.remove(_points.last());
		} // if

	} // removeLastPoint


	// ************************************************************************

	/**
	 * Sortiert die Punkte der Liste entsprechend des eingegebenen
	 * Vergleichskriteriums. Die Reihenfolge ist entweder Sorter.ASCENDING fuer
	 * aufsteigend oder Sorter.DESCENDING für absteigend.
	 * 
	 * @param input_comparitor
	 *            Vergleichskriterium
	 * @param input_order
	 *            Reihenfolge
	 * 
	 * @see anja.geom.PointComparitor
	 * @see anja.util.Sorter#ASCENDING
	 * @see anja.util.Sorter#DESCENDING
	 */
	public void sort(
			PointComparitor input_comparitor,
			byte input_order)
	{
		_points.sort(input_comparitor, input_order);

	} // sort


	// ************************************************************************

	/**
	 * Wandelt die Liste in einen Array um.
	 * 
	 * @return Ein Array der Punkte der Liste
	 */
	public Point2[] toArray()
	{
		if (_points.length() > 0)
		{
			Point2[] result = new Point2[_points.length()];
			PointsAccess access = new PointsAccess(_points);

			for (int i = 0; i < result.length; ++i)
			{
				result[i] = access.cyclicNextPoint();
			}

			return result;
		}
		return null;
	} //end toArray


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repräsentation.
	 * 
	 * @return Die textuelle Repräsentation
	 */
	public String toString()
	{
		return ("Point2List <" + _points.length() + "> [" + listPoints() + "]");

	} // toString


	// ************************************************************************

	/**
	 * Verschiebung um die Eingabewerte.
	 * 
	 * @param input_horizontal
	 *            horizontale Verschiebung
	 * @param input_vertical
	 *            vertikale Verschiebung
	 */
	public synchronized void translate(
			float input_horizontal,
			float input_vertical)
	{
		PointsAccess the_points = new PointsAccess(_points);

		while (the_points.hasNextPoint())
		{
			Point2 actual_point = the_points.nextPoint();

			actual_point.translate(input_horizontal, input_vertical);
		} // while

	} // translate


	// ********************************

	/**
	 * Verschiebung um den Vektor vom Nullpunkt zum Eingabepunkt.
	 * 
	 * <BR><B>Vorbedingungen:</B><BR>der Eingabepunkt ist nicht null
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 */
	public synchronized void translate(
			Point2 input_point)
	{
		translate(input_point.x, input_point.y);

	} // translate


	// ************************************************************************

	/**
	 * Affine Abbildung der Punktliste gem&auml;&szlig; der
	 * Transformationsmatrix <i>a</i>
	 * 
	 * <br>Version: 08.09.98<br> Author: Thomas Kamphans
	 * 
	 * @param a
	 *            Die Transformationsmatrix
	 */
	public void transform(
			Matrix33 a)
	{
		PointsAccess pa;
		Point2 p;

		synchronized (_points)
		{
			pa = new PointsAccess(this);
			while (pa.hasNextPoint())
			{
				p = pa.nextPoint();
				p.transform(a);
			}
		}

	} // transform


	// ************************************************************************

	/**
	 * Spiegelt die Punktliste an der uebergebenen Geraden
	 * 
	 * <br>Author: Johannes Werpup
	 * 
	 * @param line
	 *            Die Spiegelgerade
	 */
	public void mirror(
			Line2 line)
	{
		PointsAccess pa;
		Point2 p;

		synchronized (_points)
		{
			pa = new PointsAccess(this);
			while (pa.hasNextPoint())
			{
				p = pa.nextPoint();
				Point2 m = p.mirror(line);
				p.x = m.x;
				p.y = m.y;
			}
		}

	} // mirror
	
	
	// ************************************************************************

	/**
	 * Liefert die Teilliste zwischen start und end zurueck. Dabei darf
	 * end < start sein, da die Liste zyklisch durchlaufen wird.
	 * Bei den Punkten der zurueckgegebenen Teilliste handelt es sich
	 * wahlweise um Referenzen oder Kopien.
	 * 
	 * <br>Author: Johannes Werpup
	 * 
	 * @param start
	 *            Index des Startpunkts 
	 * @param end
	 *            Index des Endpunkts
	 * @param returnReferences
	 *            Falls true werden Referenzen der Punkte erstellt,
	 *            ansonsten Kopien
	 *            
	 * @return Die Teilliste
	 * 
	 * @throws java.util.NoSuchElementException
	 */
	public Point2List subList(
			int start,
			int end,
			boolean returnReferences)
	{
		if (start < 0 || end < 0 || start >= length() || end >= length())
			throw new java.util.NoSuchElementException();
		
		Point2List list = new Point2List();
		SimpleList points = points();
		ListItem item = points.at(start);
		int steps = (end > start) ? end - start + 1 : end + length() - start + 1;
		
		for (int i = 0; i < steps; i++)
		{
			Point2 point = (Point2)points.value(item);
			list.addPoint( returnReferences ? point : (Point2)point.clone() );
			item = item.next();
			if (item == null)
				item = points.first();
		} // for
				
		return list;
		
	} // subList

	
	// ************************************************************************

	/**
	 * Liefert die Teilliste zwischen start und end zurueck. Dabei darf
	 * end vor start in der Liste liegen, da diese zyklisch durchlaufen wird.
	 * Bei den Punkten der zurueckgegebenen Teilliste handelt es sich
	 * wahlweise um Referenzen oder Kopien.
	 * 
	 * <br>Author: Johannes Werpup
	 * 
	 * @param start
	 *            Startpunkt 
	 * @param end
	 *            Endpunkt
	 * @param returnReferences
	 *            Falls true werden Referenzen der Punkte erstellt,
	 *            ansonsten Kopien
	 *            
	 * @return Die Teilliste, oder null falls Punkte nicht enthalten
	 */
	public Point2List subList(
			Point2 start,
			Point2 end,
			boolean returnReferences)
	{
		int start_index = indexOf(start);
		int end_index = indexOf(end);
		if (start_index == -1 || end_index == -1)
			return null;
		
		return subList(start_index, end_index, returnReferences);
	}

	// ************************************************************************

} // class Point2List

