package anja.geom.triangulation;


import anja.geom.Point2;
import anja.geom.Polygon2;

import anja.util.ListItem;
import anja.util.SimpleList;
import anja.util.StdListItem;


/**
 * Hilfsklasse fuer die Triangulation. Aus einer unverzweigten Kette aus
 * verbundenen Trapezoiden, die durch degenerierte Trapezoide begrenzt ist, wird
 * ein Polygon der Originalpunkte der Trapezoide erzeugt. Dieses hat besondere
 * Eigenschaften, die aus der Tatsache resultieren, dass die Trapezoidkette nur
 * aus Trapezoiden aufgebaut ist, die ihre Originalpunkte saemtlich auf der
 * selben Seite haben:<br>
 * 
 * <ul> <li>Gespeichert wird das Polygon in der Form einer Kette, die in
 * Richtung der positiven y-Achse beginnend bei dem Fusspunkt des unteren
 * degenerierten Trapezoids "nach oben" verlaeuft und im Dachpunkt des oberen
 * degenerierten Trapezoids endet.</li>
 * 
 * <li>Diese Kette ist bezueglich der y-Koordinate ihrer Punkte streng monoton
 * steigend.</li>
 * 
 * <li>Zu jeder Kante kann ein <code>Connector</code>-Objekt gespeichert werden,
 * das von den Trapezoiden uebernommen werden kann.</li></ul>
 * 
 * <br><b>Wichtig:</b> Saemtliche Trapezoide, aus denen das Subpolygon erzeugt
 * wird, muessen exakt zwei Originalpunkte besitzen und korrekt untereinander
 * verknuepft sein!
 * 
 * @version 0.9 22.08.2003
 * @author Sascha Ternes
 */

final class SubPolygon2
{

	// *************************************************************************
	// Private constants
	// *************************************************************************

	// Label fuer Original-Punkte des Polygons:
	private static final String	_ORIGINAL	= Polygon2Triangulation.ORIGINAL;

	// *************************************************************************
	// Private variables
	// *************************************************************************

	// die Kette:
	private SimpleList			_chain;
	// Flag fuer Kettenseite:
	private boolean				_left;


	// *************************************************************************
	// Constructors
	// *************************************************************************

	/*
	* Verbotener Konstruktor.
	*/
	private SubPolygon2()
	{} // SubPolygon


	/**
	 * Erzeugt zu einer Trapezoid-Kette das Subpolygon.
	 * 
	 * @param bottom
	 *            ein <i>bottom degenerated</i>-Trapezoid
	 */
	SubPolygon2(
			Trapezoid bottom)
	{

		// Initialisierung der Kette:
		_chain = new SimpleList();
		Trapezoid trapez = bottom;
		Point2 point = trapez.getTopLeft();
		ListItem item = null;
		// Bestimmung der Seite:
		String s = point.getLabel();
		if ((s != null) && (s.equals(_ORIGINAL)))
		{
			_left = true;
		}
		else
		{
			_left = false;
		} // if

		// Aufbau der Kette:
		while (true)
		{
			// unteren Punkt des aktuellen Trapezoid aufnehmen:
			if (_left)
			{
				point = trapez.getBottomLeft();
			}
			else
			{ // if
				point = trapez.getBottomRight();
			} // else
			item = new StdListItem(point, trapez.connector);
			_chain.add(item);
			// ggf. naechstes Trapezoid ermitteln:
			if (trapez.topDegenerated())
				break;
			trapez = trapez.getTopNeighbor();
		} // while
		// obersten Kettenpunkt hinzufuegen:
		point = trapez.getTopLeft();
		item = new StdListItem(point, null);
		_chain.add(item);

	} // SubPolygon2


	// *************************************************************************
	// Public methods
	// *************************************************************************

	/**
	 * Liefert ein im Gegenuhrzeigersinn orientiertes
	 * <code>Polygon2</code>-Objekt, das dieses Subpolygon repraesentiert. Wenn
	 * das Subpolygon eine linke Kette hat, ist der erste Punkt des Polygons der
	 * oberste Punkt der Kette, bei einer rechten Kette ist der erste Punkt der
	 * untere Kettenpunkt.
	 * 
	 * @return ein <code>Polygon2</code>-Objekt
	 * 
	 * @see anja.geom.Polygon2
	 */
	public Polygon2 toPolygon2()
	{

		Polygon2 poly = new Polygon2();
		StdListItem item = null;
		if (_left)
		{
			item = (StdListItem) _chain.last();
			while (item != null)
			{
				poly.addPoint((Point2) item.key());
				item = (StdListItem) item.prev();
			} // while
		}
		else
		{ // if
			item = (StdListItem) _chain.first();
			while (item != null)
			{
				poly.addPoint((Point2) item.key());
				item = (StdListItem) item.next();
			} // while
		} // else
		return poly;

	} // toPolygon2


	/**
	 * Liefert die Kette des Subpolygons als Liste aus
	 * <code>StdListItem</code>-Objekten, die als <i>Key</i> den Punkt als
	 * <code>Point2</code>-Objekt und einen eventuellen <code>Connector</code>
	 * im <i>Value</i> enthalten.
	 * 
	 * @return die Kette
	 * 
	 * @see anja.util.SimpleList
	 */
	public SimpleList getChain()
	{

		return _chain;

	} // getChain


	/**
	 * Gibt zurueck, ob das Subpolygon aus einer linken Kette besteht.
	 * 
	 * @return <code>true</code>, wenn es sich um eine linke Kette handelt
	 */
	public boolean isLeftChain()
	{

		return _left;

	} // isLeftChain


	/**
	 * Gibt zurueck, ob das Subpolygon aus einer rechten Kette besteht.
	 * 
	 * @return <code>true</code>, wenn es sich um eine rechte Kette handelt
	 */
	public boolean isRightChain()
	{

		return !_left;

	} // isRightChain

} // SubPolygon2
