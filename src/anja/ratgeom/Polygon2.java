package anja.ratgeom;


import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.io.*;


import anja.util.BigRational;
import anja.util.Drawable;
import anja.util.GraphicsContext;
import anja.util.ListItem;
import anja.util.Savable;
import anja.util.SimpleList;


/**
 * Zweidimensionales zeichenbares Polygon. Mit <tt> setOpen() </tt> und <tt>
 * setClosed() </tt> setzt man den Zustand des Polygons auf offen oder
 * geschlossen, das wirkt sich z.B. auf <tt> draw() </tt> und <tt>
 * intersection() </tt> aus. Der Defaultwert ist geschlossen. Abfragen kann man
 * den Zustand mit <tt> isOpen() </tt> und <tt> isClosed()</tt>.
 * 
 * @version 0.4 19.12.1997
 * @author Norbert Selle
 */

public class Polygon2
		extends Point2List
		implements Drawable, Savable
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	/** Links herum orientieren. */
	public final static byte	ORIENTATION_LEFT	= 1;

	/** Rechts herum orientieren. */
	public final static byte	ORIENTATION_RIGHT	= 2;

	/**
	 * Rueckgabekonstante fuer locatePoint.
	 * @see #locatePoint
	 */
	public static final byte	POINT_INSIDE		= 1;

	/**
	 * Rueckgabekonstante fuer locatePoint.
	 * @see #locatePoint
	 */
	public static final byte	POINT_ON_EDGE		= 2;

	/**
	 * Rueckgabekonstante fuer locatePoint.
	 * @see #locatePoint
	 */
	public static final byte	POINT_OUTSIDE		= 3;

	// ************************************************************************
	// Variables
	// ************************************************************************

	/** Flagge ob das Polygon offen ist */
	private boolean				_is_open;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/*
	* Initialisierung eines neuen Polygons.
	*/

	private void _init()
	{
		_is_open = false;

	} // _init


	// ********************************

	/**
	 * Erzeugt ein leeres Polygon.
	 */

	public Polygon2()
	{
		super();
		_init();

	} // Polygon2


	// ********************************

	/**
	 * Erzeugt ein Polygon aus einem DataInputStream.
	 */

	public Polygon2(
			DataInputStream dis)
	{
		super();
		_init();

		try
		{
			String type = dis.readUTF();

			if (type.compareTo("Polygon2") != 0)
				return;

			int n = dis.readInt();

			_is_open = dis.readBoolean();

			for (int i = 0; i < n; i++)
				addPoint(new Point2(dis));
		}
		catch (IOException ex)
		{}
	} // Polygon2


	// ********************************

	/**
	 * Erzeugt ein Polygon aus der eingegebenen Punktliste, die Punkte des
	 * Polygons sind <B>Kopien</B> - nicht etwa Referenzen - der Punkte der
	 * Eingabeliste.
	 * 
	 * @param input_points
	 *            die Eingabeliste
	 */

	public Polygon2(
			Point2List input_points)
	{
		super(input_points);
		_init();

	} // Polygon2


	// ********************************

	/**
	 * Erzeugt ein neues Polygon als Kopie des Eingabepolygons, die Punkte des
	 * neuen Polygons sind <B>Kopien</B> - nicht etwa Referenzen - der Punkte
	 * des Eingabepolygons.
	 * 
	 * @param input_polygon
	 *            das Eingabepolygon
	 */

	public Polygon2(
			Polygon2 input_polygon)
	{
		super(input_polygon);
		_is_open = input_polygon._is_open;

	} // Polygon2


	// ********************************

	/**
	 * Erzeugt ein neues Polygon als skalierte Kopie des Eingabepolygons. Die
	 * Skalierung mit dem Faktor s erfolgt bezueglich des eingegebenen Punktes
	 * p.
	 * 
	 * @param input_polygon
	 *            das Eingabepolygon
	 */

	public Polygon2(
			double s,
			Polygon2 input_polygon,
			Point2 p)
	{
		super(s, input_polygon, p);
		_is_open = input_polygon._is_open;

	} // Polygon2


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Kopiert das Polygon, die Punkte der Kopie sind <B>Kopien</B> - nicht etwa
	 * Referenzen - der Punkte des Originals.
	 * 
	 * @return die Kopie
	 */

	public Object clone()
	{
		return (new Polygon2(this));

	} // clone


	// ************************************************************************

	/**
	 * Gibt den Punkt des Polygonrands zurueck, der dem Eingabepunkt am
	 * naechsten liegt.
	 * @return den naechsten Punkt
	 */
	public Point2 closestPoint(
			Point2 input_point)
	{
		if (length() == 0)
			return (null);
		if (length() == 1)
			return (firstPoint());

		BigRational min_distance = null;
		Point2 min_point = null;
		Segment2 edges[] = edges();

		for (int i = 0; i < edges.length; i++)
		{
			Segment2 segment = edges[i];
			Point2 point = segment.closestPoint(input_point);
			BigRational distance = point.bigSquareDistance(input_point);

			if ((min_point == null) || (distance.compareTo(min_distance) < 0))
			{
				min_point = point;
				min_distance = distance;
			} // if
		} // for

		return (min_point);

	} // closestPoint


	// ************************************************************************

	/**
	 * Zeichnet das Polygon.
	 */

	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		((anja.geom.Polygon2) toGeom()).draw(g, gc);

	} // draw


	// ************************************************************************

	/**
	 * Gibt die Anzahl der Kanten zurueck, zwei bei einem geschlossenem Polygon
	 * mit zwei Punkten.
	 * 
	 * @return die Anzahl der Kanten
	 */

	public int edgeNumber()
	{
		if (_points.length() <= 1)
		{
			return (0);
		} // if

		if (_is_open)
		{
			return (_points.length() - 1);
		}
		else
		{
			return (_points.length());
		} // if

	} // edgeNumber


	// ************************************************************************

	/**
	 * Gibt ein Array der Kanten zurueck.
	 * 
	 * @return die Kanten
	 */

	public Segment2[] edges()
	{
		int edge_number = edgeNumber();
		Segment2 output_edges[] = new Segment2[edge_number];

		if (edge_number > 0)
		{
			PointsAccess access = new PointsAccess(this);
			Point2 current_point;
			Point2 next_point;

			current_point = access.nextPoint();
			for (int index = 0; index < edge_number; index++)
			{
				next_point = access.cyclicNextPoint();
				output_edges[index] = new Segment2(current_point, next_point);
				current_point = next_point;
			} // for
		} // if

		return (output_edges);

	} // edges


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge der Polygonkanten mit dem BasicCircle und gibt
	 * die Schnittmenge im Intersection-Parameter zurueck, sie kann leer sein
	 * oder eine eine nichtleere Liste von Punkten enthalten.
	 */

	public void intersection(
			BasicCircle2 input_basic,
			Intersection inout_intersection)
	{
		if (_points.empty())
		{
			inout_intersection.set();
		}
		else
		{
			SimpleList intersection_list = new SimpleList();

			if (_points.length() == 1)
			{
				if (input_basic.liesOn(firstPoint()))
				{
					intersection_list.add(new Point2(firstPoint()));
				} // if
			}
			else
			// mindestens 2 Punkte
			{
				Intersection current_set = new Intersection();
				Segment2[] edges = edges();

				for (int index = 0; index < edgeNumber(); index++)
				{
					input_basic.intersection(edges[index], current_set);
					_addPoints(intersection_list, current_set);
				} // for
			} // if

			if (intersection_list.empty())
			{
				inout_intersection.set();
			}
			else
			{
				_removeDuplicates(intersection_list);
				inout_intersection.set(intersection_list);
			} // if
		} // if

	} // intersection


	// ************************************************************************

	/**
	 * Berechnet die Schnittmenge der Polygonkanten mit der Eingabelinie und
	 * gibt die Schnittmenge im Intersection-Parameter zurueck, sie kann leer
	 * sein oder eine eine nichtleere Liste von Punkten und/oder Segmenten
	 * enthalten.<br> Ist das Polygon einfach, so enthaelt die Schnittmenge
	 * keine Punkte, die andere Elemente der Schnittmenge schneiden.
	 */

	public void intersection(
			BasicLine2 input_basic,
			Intersection inout_intersection)
	{
		if (_points.empty())
		{
			inout_intersection.set();
		}
		else
		{
			SimpleList intersection_list = new SimpleList();

			if (_points.length() == 1)
			{
				if (input_basic.liesOn(firstPoint()))
				{
					intersection_list.add(new Point2(firstPoint()));
				} // if
			}
			else
			{
				int number = _points.length();
				Intersection current_set = new Intersection();
				PointsAccess access = new PointsAccess(this);
				Point2 current_point;
				Point2 next_point;
				Segment2 current_segment;

				if (_is_open)
				{
					number--;
				} // if

				current_point = access.nextPoint();
				for (int index = 0; index < number; index++)
				{
					next_point = access.cyclicNextPoint();
					current_segment = new Segment2(current_point, next_point);
					input_basic.intersection(current_segment, current_set);
					_conjunction(intersection_list, current_set);
					current_point = next_point;
				} // for

				_checkFirstTouchLast(intersection_list);
			} // if

			if (intersection_list.empty())
			{
				inout_intersection.set();
			}
			else
			{
				inout_intersection.set(intersection_list);
			} // if
		} // if

	} // intersection


	// ************************************************************************

	/**
	 * Testet ob die Linie den Polygonrand schneidet.
	 * 
	 * @param input_basic
	 *            die Linie
	 * 
	 * @return true wenn ja, sonst false
	 */

	public boolean intersectsEdge(
			BasicLine2 input_basic)
	{
		if (_points.empty())
		{
			return (false);
		}

		if (_points.length() == 1)
		{
			return (input_basic.liesOn(firstPoint()));
		} // if

		int number = _points.length();
		Intersection current_set = new Intersection();
		PointsAccess access = new PointsAccess(this);
		Point2 current_point;
		Point2 next_point;
		Segment2 current_segment;

		if (_is_open)
		{
			number--;
		} // if

		current_point = access.nextPoint();
		for (int index = 0; index < number; index++)
		{
			next_point = access.cyclicNextPoint();
			current_segment = new Segment2(current_point, next_point);
			input_basic.intersection(current_segment, current_set);
			if (current_set.result != Intersection.EMPTY)
			{
				return (true);
			} // if
			current_point = next_point;
		} // for

		return (false);

	} // intersectsEdge


	// ************************************************************************

	/**
	 * Testet ob das Rechteck das Polygon schneidet, bei einem geschlossenem
	 * einfachem Polygon wird sowohl auf Schnitt mit der Flaeche als auch auf
	 * Schnitt mit dem Rand getestet, bei allen anderen Polygonen nur auf
	 * Schnitt mit dem Rand.
	 * 
	 * @param input_box
	 *            das Rechteck
	 * 
	 * @return true wenn ja, sonst false
	 */

	public boolean intersects(
			Rectangle2D input_box)
	{
		return (((anja.geom.Polygon2) toGeom()).intersects(input_box));

	} // intersects


	// ************************************************************************

	/**
	 * Gibt true zurueck wenn der Modus des Polygons geschlossen ist.
	 */

	public boolean isClosed()
	{
		return (!_is_open);

	} // isClosed


	// ************************************************************************

	/**
	 * Gibt true zurueck wenn der Modus des Polygons offen ist.
	 */

	public boolean isOpen()
	{
		return (_is_open);

	} // isOpen


	// ************************************************************************

	/**
	 * Setzt den Modus auf geschlossen.
	 */

	public void setClosed()
	{
		_is_open = false;

	} // setClosed


	// ************************************************************************

	/**
	 * Setzt den Modus auf offen.
	 */

	public void setOpen()
	{
		_is_open = true;

	} // setOpen


	// ************************************************************************

	/**
	 * Testet ob das Polygon einfach ist, das heisst die folgenden Bedingungen
	 * erfuellt: <ul> <li> es ist geschlossen <li> die Anzahl der Eckpunkte ist
	 * groesser als zwei <li> es gibt keine uebereinanderliegenden Eckpunkte
	 * <li> Kanten schneiden sich nur an ihren Endpunkten </ul>
	 */

	public boolean isSimple()
	{
		if (_is_open || (_points.length() <= 2))
		{
			return (false);
		} // if

		Intersection set = new Intersection();
		Segment2 primary;
		Segment2 secondary;
		Segment2 edges[] = edges();
		int max_index = edges.length - 1;

		for (int primary_index = 0; primary_index <= max_index; primary_index++)
		{
			primary = edges[primary_index];

			if (primary.source().equals(primary.target()))
				return (false); // uebereinanderliegende Eckpunkte

			if (primary_index == max_index)
				secondary = edges[0];
			else
				secondary = edges[primary_index + 1];

			primary.intersection(secondary, set);
			if (set.result != Intersection.POINT2)
				return (false); // Nachfolger wird nicht in genau
			// einem Punkt geschnitten

			for (int secondary_index = primary_index + 2; secondary_index <= max_index; secondary_index++)
			{
				secondary = edges[secondary_index];
				primary.intersection(secondary, set);

				if ((primary_index == 0) && (secondary_index == max_index))
				{
					if (set.result != Intersection.POINT2)
						return (false); // erste und letzte Kante schneiden
					// sich nicht in einem Punkt
				}
				else
				{
					if (set.result != Intersection.EMPTY)
						return (false); // Schnitt nicht aufeinanderfolgender
					// Kanten
				} // if
			} // for
		} // for

		return (true);

	} // isSimple


	// ************************************************************************

	/**
	 * Berechnet die Laenge.
	 */

	public double len()
	{
		int edge_number = edgeNumber();
		double result_len = 0;

		if (edge_number > 0)
		{
			PointsAccess access = new PointsAccess(this);
			Point2 current = access.nextPoint();
			Point2 next;

			for (int counter = 0; counter < edge_number; counter++)
			{
				next = access.cyclicNextPoint();
				result_len += current.distance(next);
				current = next;
			} // for
		} // if

		return (result_len);

	} // len


	// ************************************************************************

	/**
	 * Berechnet das Quadrat der minimalen Distanz zwischen Polygonrand und
	 * Eingabepunkt. Erzeugt eine NoSuchElementException bei einem Polygon mit
	 * null Punkten.
	 * 
	 * @return Quadrat der minimalen Distanz zwischen Polygonrand und
	 *         Eingabepunkt
	 */

	public double squareDistance(
			Point2 input_point)
	{
		Point2 min_point = closestPoint(input_point);

		if (min_point == null)
		{
			throw new java.util.NoSuchElementException();
		}
		else
		{
			return (min_point.squareDistance(input_point));
		} // if

	} // squareDistance


	// ************************************************************************

	/**
	 * Berechnet die minimale Distanz zwischen Polygonrand und Eingabepunkt.
	 * Erzeugt eine NoSuchElementException bei einem Polygon mit null Punkten.
	 * 
	 * @return minimale Distanz zwischen Polygonrand und Eingabepunkt
	 */

	public double distance(
			Point2 input_point)
	{
		double square_distance = 0;

		try
		{
			square_distance = squareDistance(input_point);
		}
		catch (java.util.NoSuchElementException ex)
		{
			throw ex;
		} // try

		return (Math.sqrt(square_distance));

	} // distance


	// ************************************************************************

	/**
	 * Erzeugt ein anja.geom.Polygon2 aus dem Polygon und gibt es nach Object
	 * gecastet zurueck.
	 */

	public Object toGeom()
	{
		anja.geom.Polygon2 geom_polygon = new anja.geom.Polygon2();
		PointsAccess access = new PointsAccess(this);

		while (access.hasNextPoint())
		{
			geom_polygon.addPoint(access.nextPoint().toGeom());
		} // while

		if (isOpen())
		{
			geom_polygon.setOpen();
		}
		else
		{
			geom_polygon.setClosed();
		} // if

		return (geom_polygon);

	} // toGeom


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation.
	 * 
	 * @return die textuelle Repraesentation
	 */

	public String toString()
	{
		return "Polygon2 <" + _points.length() + " | "
				+ (_is_open ? "open" : "closed") + "> [" + listPoints() + "]";

	} // toString


	// ************************************************************************

	public void save(
			DataOutputStream dio)
	{
		try
		{
			dio.writeUTF("Polygon2");
			dio.writeInt(_points.length());
			dio.writeBoolean(_is_open);
			PointsAccess ac = new PointsAccess(_points);

			while (ac.hasNextPoint())
				ac.nextPoint().save(dio);
		}
		catch (IOException ioex)
		{}
	}


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/*
	* Haengt die nur Punkte enthaltende Menge an die Liste. Enthaelt die
	* Menge eine Liste, so ist diese anschliessend leer.
	*
	* @param	inout_list	die Liste
	* @param	inout_set	die Menge
	*/

	private void _addPoints(
			SimpleList inout_list,
			Intersection inout_set)
	{
		switch (inout_set.result)
		{
			case Intersection.EMPTY:
				break;
			case Intersection.POINT2:
				inout_list.add(inout_set.point2);
				break;
			case Intersection.LIST:
				inout_list.concat(inout_set.list);
				break;
			default:
				System.err.println("\nPolygon2._addPoints: unexpected case: "
						+ "\n  Polygon     : " + this + "\n  Intersection: "
						+ inout_set);
				break;
		} // switch

	} // _addPoints


	// ************************************************************************

	/*
	* Entfernt das erste oder letzte Element der Eingabeliste, wenn es ein
	* Punkt ist und das letzte bzw. erste Element der Eingabeliste beruehrt.
	*/
	private void _checkFirstTouchLast(
			SimpleList inout_list)
	{
		if (inout_list.length() >= 2)
		{
			if (inout_list.lastValue() instanceof Point2)
			{
				if (_isTouching((Point2) inout_list.lastValue(),
						inout_list.firstValue()))
				{
					// letztes Element loeschen ( kleines p bei pop )
					inout_list.pop();
				} // if
			}
			else if (inout_list.firstValue() instanceof Point2)
			{
				if (_isTouching((Point2) inout_list.firstValue(),
						inout_list.lastValue()))
				{
					// erstes Element loeschen ( grosses P bei Pop )
					inout_list.Pop();
				} // if
			} // if
		} // if

	} // _checkFirstTouchLast


	// ************************************************************************

	/*
	* Vereinigt die Liste und das Eingabeelement wie folgt:
	* - Ist das Eingabeelement ein Punkt, so wird er an die Liste angehaengt,
	*   wenn er das letzte Listenelement nicht schneidet
	* - Ist das Eingabeelement ein Segment so wird es an die Liste angehaengt,
	*   wenn das vorher letzte Listenelement ein das Segment schneidender
	*   Punkt ist, so wird der Punkt aus der Liste entfernt
	*
	* @param	inout_elements	Liste von Segmenten und Punkten
	* @param	input_element	Leer oder ein Punkt oder ein Segment
	*/

	private void _conjunction(
			SimpleList inout_elements,
			Intersection input_element)
	{
		switch (input_element.result)
		{
			case Intersection.EMPTY:
				break;
			case Intersection.POINT2:
				if (inout_elements.empty())
				{
					inout_elements.add(input_element.point2);
				}
				else
				{
					if (!_isTouching(input_element.point2,
							inout_elements.lastValue()))
					{
						inout_elements.add(input_element.point2);
					} // if

				} // if
				break;
			case Intersection.SEGMENT2:
				if ((!inout_elements.empty())
						&& (inout_elements.lastValue() instanceof Point2)
						&& (input_element.segment2
								.liesOn((Point2) inout_elements.lastValue())))
				{
					inout_elements.pop();
				} // if
				inout_elements.add(input_element.segment2);
				break;
			default:
				System.err.println("Polygon2._conjunction: unexpected case: "
						+ "  Polygon " + this + "  Intersection "
						+ input_element);
				break;
		} // switch

	} // _conjunction


	// ************************************************************************

	/*
	* Testet ob der Eingabepunkt das Eingabeobjekt der Klasse Point2 oder
	* Segment2 beruehrt.
	*/

	private boolean _isTouching(
			Point2 input_point,
			Object input_object)
	{
		if (input_object instanceof Point2)
		{
			return ((Point2) input_object).equals(input_point);
		}
		else
		{
			return ((Segment2) input_object).liesOn(input_point);
		} // if

	} // _isTouching


	// ************************************************************************

	/*
	* Loescht die Duplikate mehrfach in der Liste enthaltender Punkte aus
	* ebendieser, die Reihenfolge der Punkte kann dabei geaendert werden.
	*/

	private void _removeDuplicates(
			SimpleList inout_points)
	{
		if (inout_points.length() < 2)
		{
			return;
		} // if

		// die Punkte der Groesse nach sortieren

		PointComparitor compare = new PointComparitor();

		compare.setOrder(PointComparitor.X_ORDER);
		inout_points.sort(compare);

		// Duplikate aus der Liste loeschen

		ListItem current_item = inout_points.first();
		ListItem next_item = current_item.next();

		while (next_item != null)
		{
			if (((Point2) current_item.value()).equals(next_item.value()))
			{
				inout_points.remove(next_item);
			}
			else
			{
				current_item = next_item;
			} // if

			next_item = current_item.next();

		} // while

	} // _removeDuplicates


	// ************************************************************************

	// lup, 7.5. '97
	/**
	 * Orientiert das Polygon entsprechend ori.
	 * @param ori
	 *            entweder ORIENTATION_LEFT oder ORIENTATION_RIGHT
	 * @see #ORIENTATION_LEFT
	 * @see #ORIENTATION_RIGHT
	 */
	public void setOrientation(
			byte ori)
	{
		SimpleList L = points();
		if (((ori != ORIENTATION_LEFT) && (ori != ORIENTATION_RIGHT))
				|| (L.length() <= 2))
			return;
		double innen = 0, aussen = 0, phi;
		Point2 p1, p2, p3;
		ListItem i = L.first();
		while (i != null)
		{
			p1 = (Point2) i.value();
			p2 = (Point2) L.cyclicRelative(i, 1).value();
			p3 = (Point2) L.cyclicRelative(i, 2).value();
			phi = p2.angle(p3, p1);
			innen += phi;
			aussen += 2 * Math.PI - phi;
			i = i.next();
		}
		byte orientation = 0;
		if (innen < aussen)
			orientation = ORIENTATION_LEFT;
		else
			orientation = ORIENTATION_RIGHT;
		if (orientation != ori)
		{
			L.reverse(); // Punktreihenfole umkehren
			L.cycle(-1); // wieder den urspruenglichen Anfangspunkt herstellen
		}
	} // setOrientation


	// lup, 24.5. '97
	/**
	 * Ein Point-in-Polygon Test. Gibt true zurueck, falls der Punkt p innerhalb
	 * des Polygons liegt. Auf einer Polygonkante zaehlt hierbei als ausserhalb.
	 * Benutzt locatePoint.
	 * @param p
	 *            zu testender Punkt
	 * @return true, falls p innerhalb des Polygons liegt, ansonsten false
	 * @see #locatePoint
	 */
	public boolean inside(
			Point2 p)
	{
		return locatePoint(p) == POINT_INSIDE;
	} // inside


	// lup, 24.5. '97
	/**
	 * Point-Location. Je nach Position des Punktes p wird eine der drei
	 * Konstanten zurueckgegeben: <ul> <li><strong>POINT_INSIDE</strong>, falls
	 * p innerhalb des Polygons liegt</li> <li><strong>POINT_ON_EDGE</strong>,
	 * falls der Punkt p auf dem Rand des Polygons liegt</li>
	 * <li><strong>POINT_OUTSIDE</strong>, falls p ausserhalb des Polygons
	 * liegt</li> </ul>
	 * 
	 * @param q
	 *            zu lokalisierender Punkt
	 * 
	 * @return POINT_INSIDE, POINT_OUTSIDE oder POINT_ON_EDGE, je nach Lage des
	 *         Punktes.
	 */
	public byte locatePoint(
			Point2 q)
	{
		SimpleList L = points();
		if (L.length() < 1)
			return POINT_OUTSIDE;
		if (L.length() == 1)
		{
			if (((Point2) L.firstValue()).equals(q))
				return POINT_ON_EDGE;
			else
				return POINT_OUTSIDE;
		}
		if (L.length() == 2)
		{
			if ((new Segment2((Point2) L.firstValue(), (Point2) L.lastValue()))
					.liesOn(q))
				return POINT_ON_EDGE;
			else
				return POINT_OUTSIDE;
		}
		Ray2 test = new Ray2(q, 0, Ray2.LEFT);
		Segment2 s;
		Point2 pl, p1, p2 = (Point2) L.lastKey(), pi;
		Intersection inter = new Intersection();
		boolean on = false;
		int l = 0;
		for (ListItem i = L.first(); (i != null) && (!on); i = i.next())
		{
			p1 = p2;
			p2 = (Point2) i.key();
			s = new Segment2(p1, p2);
			if (!s.isHorizontal())
			{
				if ((pi = s.intersection(test, inter)) != null)
				{
					if (s.source().y.compareTo(s.target().y) < 0)
						pl = s.source();
					else
						pl = s.target();
					if (!pl.equals(pi))
						l++;
					on = q.equals(pi);
				}
			}
			else
				on = s.liesOn(q);
		}
		if (on)
			return POINT_ON_EDGE;
		if (l % 2 == 1)
			return POINT_INSIDE;
		else
			return POINT_OUTSIDE;
	} // locatePoint

	/**
	 * Berechnet mit Hilfe der Klasse visPolygon das Sichtbarkeitspolygon
	 * bezueglich des Punktes p in O(n) Zeit.
	 * @param p
	 *            Sichtpunkt
	 * @return Sichtbarkeitspolygon bezueglich des Punktes p
	 */
	/* !!NS not yet implemented
	 public Polygon2 vis(Point2 p) {
	  return new visPolygon(this).vis(p);
	 } // vis
	*/

	// ************************************************************************

} // class Polygon2

