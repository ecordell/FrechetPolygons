package anja.geom.triangulation;


import anja.geom.Point2;
import anja.geom.Polygon2;


/**
 * Objekte dieser Klasse werden in
 * {@link anja.geom.triangulation.Polygon2Triangulation} verwendet.
 * 
 * <br>Trapezoide sind einfache Polygone aus vier Punkten. Ihre Orientierung ist
 * {@link anja.geom.Polygon2#ORIENTATION_LEFT}, der Startpunkt ist der obere
 * linke Punkt (und somit ist der Endpunkt oben rechts).
 * 
 * <br>Trapezoide werden zu einem Netz verknuepft. Jedes Trapezoid kann jeweils
 * zwei * obere und untere Nachbarn haben.
 * 
 * @version 0.9 25.08.2003
 * @author Sascha Ternes
 */

public class Trapezoid
		extends Polygon2
{

	// *************************************************************************
	// Package variables
	// *************************************************************************

	/** zugehoeriger Node in der Query Structure */
	Object				node;

	/** Connector-Objekt bei diagonaler Teilung */
	Connector			connector;

	/** Status-Hilfsvariable, wird mit 0 initialisiert */
	int					status;

	// *************************************************************************
	// Private variables
	// *************************************************************************

	/** Nachbar */
	private Trapezoid[]	_top;

	/** Nachbar */
	private Trapezoid[]	_bottom;


	// *************************************************************************
	// Constructors
	// *************************************************************************

	/*
	* Verbotener Konstruktor.
	*/
	private Trapezoid()
	{}


	/**
	 * Erzeugt ein neues Trapezoid mit den spezifizierten Punkten.
	 * 
	 * @param top_left
	 *            Punkt oben links
	 * @param top_right
	 *            Punkt oben rechts
	 * @param bottom_left
	 *            Punkte unten links
	 * @param bottom_right
	 *            Punkt unten rechts
	 */
	public Trapezoid(
			Point2 top_left,
			Point2 top_right,
			Point2 bottom_left,
			Point2 bottom_right)
	{

		super();
		addPoint(top_left);
		addPoint(bottom_left);
		addPoint(bottom_right);
		addPoint(top_right);
		_top = new Trapezoid[2];
		_bottom = new Trapezoid[2];
		node = null;
		connector = null;
		status = 0;

	} // Trapezoid


	// *************************************************************************
	// Public methods
	// *************************************************************************

	/**
	 * Liefert ein Array der Eckpunkte dieses Trapezoids. Die Reihenfolge der
	 * Punkte im Array ist <i>oben links - oben rechts - unten links - unten
	 * rechts</i>.
	 * 
	 * @return Ein vierelementiges Array mit den Eckpunkten
	 */
	public Point2[] getPoints()
	{

		Point2[] p = new Point2[4];
		p[0] = getTopLeft();
		p[1] = getTopRight();
		p[2] = getBottomLeft();
		p[3] = getBottomRight();
		return p;

	} // getPoints


	/**
	 * Liefert den oberen linken Punkt des Trapezoids.
	 * 
	 * @return den oberen linken Punkt
	 */
	public Point2 getTopLeft()
	{

		return (Point2) _points.getValueAt(0);

	} // getTopLeft


	/**
	 * Liefert den oberen rechten Punkt des Trapezoids.
	 * 
	 * @return den oberen rechten Punkt
	 */
	public Point2 getTopRight()
	{

		return (Point2) _points.getValueAt(3);

	} // getTopRight


	/**
	 * Liefert den unteren linken Punkt des Trapezoids.
	 * 
	 * @return den unteren linken Punkt
	 */
	public Point2 getBottomLeft()
	{

		return (Point2) _points.getValueAt(1);

	} // getBottomLeft


	/**
	 * Liefert den unteren rechten Punkt des Trapezoids.
	 * 
	 * @return den unteren rechten Punkt
	 */
	public Point2 getBottomRight()
	{

		return (Point2) _points.getValueAt(2);

	} // getBottomRight


	/**
	 * Testet, ob das Trapezoid oben degeneriert ist, d.h. ob der obere linke
	 * und rechte Punkt identisch sind und das Trapezoid ein Dreieck ist.
	 * 
	 * @return <code>true</code>, wenn das Trapezoid degeneriert ist
	 */
	public boolean topDegenerated()
	{

		Point2 p1 = (Point2) _points.getValueAt(0);
		Point2 p2 = (Point2) _points.getValueAt(3);
		return p1.equals(p2);

	} // topDegenerated


	/**
	 * Testet, ob das Trapezoid unten degeneriert ist, d.h. ob der obere linke
	 * und rechte Punkt identisch sind und das Trapezoid ein Dreieck ist.
	 * 
	 * @return <code>true</code>, wenn das Trapezoid degeneriert ist
	 */
	public boolean bottomDegenerated()
	{

		Point2 p1 = (Point2) _points.getValueAt(1);
		Point2 p2 = (Point2) _points.getValueAt(2);
		return p1.equals(p2);

	} // bottomDegenerated


	/**
	 * Liefert die Anzahl der oberen Nachbarn. Ein Trapezoid kann keine, einen
	 * oder zwei obere Nachbarn haben.
	 * 
	 * @return Die Zahl der oberen Nachbarn
	 */
	public int topNeighborCount()
	{

		if (_top[0] == null)
		{
			return 0;
		}
		else
		{ // if
			if (_top[1] == null)
			{
				return 1;
			}
			else
			{ // if
				return 2;
			} // else
		} // else

	} // topNeighborCount


	/**
	 * Liefert die Anzahl der unteren Nachbarn. Ein Trapezoid kann keine, einen
	 * oder zwei untere Nachbarn haben.
	 * 
	 * @return Die Zahl der unteren Nachbarn
	 */
	public int bottomNeighborCount()
	{

		if (_bottom[0] == null)
		{
			return 0;
		}
		else
		{ // if
			if (_bottom[1] == null)
			{
				return 1;
			}
			else
			{ // if
				return 2;
			} // else
		} // else

	} // bottomNeighborCount


	/**
	 * Liefert den oberen Nachbarn dieses Trapezoids. Falls zwei Nachbarn
	 * existieren, wird der linke obere Nachbar geliefert.
	 * 
	 * @return Der obere Nachbar oder <code>null</code>, wenn keine oberen
	 *         Nachbarn existieren
	 */
	public Trapezoid getTopNeighbor()
	{

		return _top[0];

	} // getTopNeighbor


	/**
	 * Liefert den linken oberen Nachbarn dieses Trapezoids. Falls nur ein
	 * Nachbar existiert, wird der einzige obere Nachbar geliefert.
	 * 
	 * @return Der linke obere Nachbar oder <code>null</code>, wenn keine oberen
	 *         Nachbarn existieren
	 */
	public Trapezoid getTopLeftNeighbor()
	{

		return _top[0];

	} // getTopLeftNeighbor


	/**
	 * Liefert den rechten oberen Nachbarn dieses Trapezoids. Falls nur ein
	 * Nachbar existiert, wird der einzige obere Nachbar geliefert.
	 * 
	 * @return Der rechte obere Nachbar oder <code>null</code>, wenn keine
	 *         oberen Nachbarn existieren
	 */
	public Trapezoid getTopRightNeighbor()
	{

		if (_top[1] == null)
		{
			return _top[0];
		} // if
		return _top[1];

	} // getTopRightNeighbor


	/**
	 * Liefert die beiden oberen Nachbarn dieses Trapezoids.
	 * 
	 * @return Die beiden oberen Nachbarn
	 */
	public Trapezoid[] getTopNeighbors()
	{

		return _top;

	} // getTopNeighbors


	/**
	 * Liefert den unteren Nachbarn dieses Trapezoids. Falls zwei Nachbarn
	 * existieren, wird der linke untere Nachbar geliefert.
	 * 
	 * @return Der untere Nachbar oder <code>null</code>, wenn keine unteren
	 *         Nachbarn existieren
	 */
	public Trapezoid getBottomNeighbor()
	{

		return _bottom[0];

	} // getBottomNeighbor


	/**
	 * Liefert den linken unteren Nachbarn dieses Trapezoids. Falls nur ein
	 * Nachbar existiert, wird der einzige untere Nachbar geliefert.
	 * 
	 * @return Der linke untere Nachbar oder <code>null</code>, wenn keine
	 *         unteren Nachbarn existieren
	 */
	public Trapezoid getBottomLeftNeighbor()
	{

		return _bottom[0];

	} // getBottomLeftNeighbor


	/**
	 * Liefert den rechten unteren Nachbarn dieses Trapezoids. Falls nur ein
	 * Nachbar existiert, wird der einzige untere Nachbar geliefert.
	 * 
	 * @return Der rechte untere Nachbar oder <code>null</code>, wenn keine
	 *         unteren Nachbarn existieren
	 */
	public Trapezoid getBottomRightNeighbor()
	{

		if (_bottom[1] == null)
		{
			return _bottom[0];
		} // if
		return _bottom[1];

	} // getBottomRightNeighbor


	/**
	 * Liefert die beiden unteren Nachbarn dieses Trapezoids.
	 * 
	 * @return Die beiden unteren Nachbarn
	 */
	public Trapezoid[] getBottomNeighbors()
	{

		return _bottom;

	} // getBottomNeighbors


	/**
	 * Setzt den oberen Nachbarn dieses Trapezoids.
	 * 
	 * @param neighbor
	 *            der obere Nachbar; wird hier <code>null</code> uebergeben,
	 *            werden die oberen Nachbarn auf <code>null</code> gesetzt.
	 */
	public void linkTopTo(
			Trapezoid neighbor)
	{

		_top[0] = neighbor;
		_top[1] = null;

	} // linkTopTo


	/**
	 * Setzt die oberen Nachbarn dieses Trapezoids. Dabei gilt, dass im
	 * uebergebenen Array der linke obere Nachbar an Index 0 steht.
	 * 
	 * @param neighbors
	 *            die beiden oberen Nachbarn; wird hier <code>null</code>
	 *            uebergeben, werden beide Nachbarn auf <code>null</code>
	 *            gesetzt
	 */
	public void linkTopTo(
			Trapezoid[] neighbors)
	{

		if (neighbors != null)
		{
			_top = neighbors;
			if (_top[0] == null)
			{
				_top[0] = _top[1];
				_top[1] = null;
			} // if
		}
		else
		{ // if
			_top[0] = null;
			_top[1] = null;
		} // else

	} // linkTopTo


	/**
	 * Setzt den unteren Nachbarn dieses Trapezoids.
	 * 
	 * @param neighbor
	 *            der untere Nachbar; wird hier <code>null</code> uebergeben,
	 *            werden die unteren Nachbarn auf <code>null</code> gesetzt.
	 */
	public void linkBottomTo(
			Trapezoid neighbor)
	{

		_bottom[0] = neighbor;
		_bottom[1] = null;

	} // linkBottomTo


	/**
	 * Setzt die unteren Nachbarn dieses Trapezoids. Dabei gilt, dass im
	 * uebergebenen Array der linke untere Nachbar an Index 0 steht.
	 * 
	 * @param neighbors
	 *            die beiden unteren Nachbarn; wird hier <code>null</code>
	 *            uebergeben, werden beide Nachbarn auf <code>null</code>
	 *            gesetzt
	 */
	public void linkBottomTo(
			Trapezoid[] neighbors)
	{

		if (neighbors != null)
		{
			_bottom = neighbors;
			if (_bottom[0] == null)
			{
				_bottom[0] = _bottom[1];
				_bottom[1] = null;
			} // if
		}
		else
		{ // if
			_bottom[0] = null;
			_bottom[1] = null;
		} // else

	} // linkBottomTo


	/**
	 * Setzt die oberen Nachbarn auf <code>null</code>.
	 */
	public void removeTopLinks()
	{

		_top[0] = null;
		_top[1] = null;

	} // removeTopLinks


	/**
	 * Setzt die unteren Nachbarn auf <code>null</code>.
	 */
	public void removeBottomLinks()
	{

		_bottom[0] = null;
		_bottom[1] = null;

	} // removeTopLinks


	/**
	 * Liefert eine textuelle Repraesentation.
	 * 
	 * @return Einen <code>String</code>, der das Trapezoid beschreibt
	 */
	public String toString()
	{

		String s = "Trapezoid {";
		s += getTopLeft() + ",";
		s += getTopRight() + ",";
		s += getBottomLeft() + ",";
		s += getBottomRight() + "}";
		return s;

	} // toString

} // Trapezoid
