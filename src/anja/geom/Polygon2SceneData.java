package anja.geom;


import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import java.awt.geom.Rectangle2D;


/**
 * Polygon2SceneData ist die Basisklasse fuer Polygon2Scene zur Verwaltung der
 * Interior-Polygone und ggf. eines Bounding-Polygons der Polygon-Szene.
 * 
 * Polygon2SceneData stellt Methoden zum Zufuegen, Entfernen und Manipulieren
 * von Interior-Polygonen sowie eines Bounding-Polygons bereit. Dabei werden
 * Datenstrukturen aufgebaut, die das moeglichst effiziente "Beantworten" von
 * diversen Anfragen ermoeglichen.
 * 
 * @version 0.1 26.02.01
 * @author Ulrich Handel
 */

//****************************************************************
public class Polygon2SceneData
		implements java.io.Serializable
//****************************************************************
{

	//************************************************************
	// public constants
	//************************************************************

	/**
	 * Polygonumlauf mit linker Hand am Polygon
	 * 
	 * Bei einem Interior-Polygon ist LEFT_HAND gleichbedeutend mit linksrum
	 * bzw. gegen den Uhrzeigersinn. Beim Bounding-Polygon bedeutet LEFT_HAND
	 * die Umrundung des Polygons im Uhrzeigersinn, da beim Bounding-Polygon der
	 * freie Raum im Polygon-Inneren liegt ( und nicht wie bei einem
	 * Interior-Polygon ausserhalb des Polygons ).
	 * 
	 * Richtungen fuer Polygonumlaeufe
	 * 
	 * Die Definition als boolsche Variable erleichtert die Umkehrung der
	 * Richtung, da bei einer als Variable gegebenen Richtung dir die
	 * Gegenrichtung einfach durch !dir ermittelt werden kann.
	 */
	public final static boolean	LEFT_HAND				= true;

	/**
	 * Siehe {@link #LEFT_HAND}
	 * 
	 * @see #LEFT_HAND
	 */
	public final static boolean	RIGHT_HAND				= false;

	/**
	 * Rückgabeoption für verschiedene Methoden, die aus der Menge aller
	 * Polygone nur bestimmte zurückgeben. Dies ist der Schalter für offene
	 * Polygone.
	 */
	public final static int		OPEN_POLYGONS			= 7000;
	/**
	 * Rückgabeoption für verschiedene Methoden, die aus der Menge aller
	 * Polygone nur bestimmte zurückgeben. Dies ist der Schalter für
	 * geschlossene Polygone.
	 */
	public final static int		CLOSED_POLYGONS			= 7001;
	/**
	 * Rückgabeoption für verschiedene Methoden, die aus der Menge aller
	 * Polygone nur bestimmte zurückgeben. Dies ist der Schalter für offene und
	 * geschloseene Polygone.
	 */
	public final static int		ALL_POLYGONS			= 7002;

	//************************************************************
	// private variables
	//************************************************************

	private Vector				_polygons				= new Vector();
	// Alle Interior-Polygone der Szene

	private Polygon2			_bounding_polygon		= null;
	// Ein die Szene umschliessendes Polygon

	Rectangle2D					_polygons_box			= null;
	// Bounding-Box aller Interior-Polygone

	Rectangle2D					_bounding_polygon_box	= null;
	// Bounding-Box des umschliessenden Polygons

	private Hashtable			_table_polygons			= new Hashtable();
	// speichert zu jedem Polygon der Szene
	// ( Interior oder Bounding ) zusaetzliche
	// Informationen.
	// @see innere Klasse PolygonInfo

	private Hashtable			_table_vertices			= new Hashtable();


	// speichert zu jedem Polygon-Eckpunkt der
	// Szene ( Interior oder Bounding )
	// zusaetzliche Informationen.
	// @see innere Klasse VertexInfo

	//************************************************************
	// constructors
	//************************************************************

	//************************************************************
	// public methods
	//************************************************************

	//------------------------------------------------------------
	// Zufuegen und Entfernen von Polygonen
	//------------------------------------------------------------

	/**
	 * Hinzufügen eines Interior-Polygons.
	 * 
	 * @param poly
	 *            Das hinzuzufügende Polygon
	 */
	public void addPolygon(
			Polygon2 poly)
	{
		if (poly == null)
			return;

		if (_polygons.contains(poly))
			return; // Das Polygon poly ist bereits vorhanden

		// Polygon zufuegen und alle Datenstrukturen aktualisieren

		_polygons.addElement(poly);

		_putPolygonToHashTables(poly);

		_recalcBoundingBox();
	} // addPolygon


	/**
	 * Entfernen eines Interior-Polygons.
	 * 
	 * @param poly
	 *            Das zu entfernende Polygon
	 */
	public void removePolygon(
			Polygon2 poly)
	{
		if (poly == null)
			return;

		if (!_polygons.contains(poly))
			return; // Das Polygon poly ist nicht vorhanden

		// Polygon entfernen und alle Datenstrukturen aktualisieren

		_polygons.removeElement(poly);

		_removePolygonFromHashTables(poly);

		_recalcBoundingBox();
	} // removePolygon


	/**
	 * Setzen bzw Löschen (poly == null) des Bounding-Polygons
	 * 
	 * @param poly
	 *            Das begrenzende Bounding-Polygon
	 */
	public void setBoundingPolygon(
			Polygon2 poly)
	{
		if (_bounding_polygon == poly)
			return; // Nichts zu tun

		// Falls bereits ein Bounding-Polygon gesetzt ist, muss dieses
		// zuerst aus allen DatenStrukturen "entfernt" werden
		//
		if (_bounding_polygon != null)
		{
			_removePolygonFromHashTables(_bounding_polygon);
			_bounding_polygon_box = null;
		} // if

		_bounding_polygon = poly; // Neues Polygon setzen

		// Falls das neue Bounding-Polygon nicht null ist, muss dieses
		// in alle DatenStrukturen "eingefuegt" werden
		//
		if (_bounding_polygon != null)
		{
			_putPolygonToHashTables(_bounding_polygon);
			_bounding_polygon_box = poly.getBoundingRect();
		} // if
	} // setBoundingPolygon


	//------------------------------------------------------------
	// Zugriff auf Polygone
	//------------------------------------------------------------

	/**
	 * Liefert ein Array aller Interior-Polygone.
	 * 
	 * @return Ein Array aller Polygone
	 */
	public Polygon2[] getInteriorPolygons()
	{
		int size = _polygons.size();

		Polygon2[] array = new Polygon2[size];
		_polygons.copyInto(array);

		return array;
	} // getInteriorPolygons


	/**
	 * Liefert die Anzahl der Interior-Polygone in der Szene
	 * 
	 * @return Anzahl der Polygone
	 */
	public int getNumOfPolygons()
	{
		return _polygons.size();
	} // getNumOfPolygons


	/**
	 * Liefert das aktuelle Bounding-Polygon oder null, falls kein
	 * Bounding-Polygon gesetzt ist.
	 * 
	 * @return Das Bounding-Polygon oder null
	 */
	public Polygon2 getBoundingPolygon()
	{
		return _bounding_polygon;
	} // getBoundingPolygon


	//------------------------------------------------------------
	// Diverse Anfragen
	//------------------------------------------------------------

	/**
	 * Ermitteln des die Szene umschliessenden Rechtecks. (konstante Laufzeit)
	 * 
	 * @return Das umschließende Rechteck
	 */
	public Rectangle2 getBoundingBox()
	{
		Rectangle2D box = null;

		if (getBoundingPolygon() != null)
			box = _bounding_polygon_box;
		else
			box = _polygons_box;

		if (box == null)
			return null;

		return new Rectangle2(box);
	} // getBoundingBox


	/**
	 * Ermitteln des Rechtecks welches das Polygon poly umschliesst. Falls das
	 * Polygon poly eines der Interior-Polygone oder das Bounding-Polygon der
	 * Szene ist, so kann dessen Bounding-Box mit quasi konstanter Laufzeit
	 * ermittelt werden (Hashing)
	 * 
	 * @param poly
	 *            Das Polygon, dessen umschließendes Rechteck bestimmt wird.
	 * 
	 * @return Das umschließende Rechteck
	 */
	public Rectangle2 getBoundingBox(
			Polygon2 poly)
	{
		Rectangle2D box = null;

		PolygonInfo info = (PolygonInfo) _table_polygons.get(poly);

		if (info != null)
			box = info._box;
		else
			box = poly.getBoundingRect();

		if (box == null)
			return null;

		return new Rectangle2(box);
	} // getBoundingBox


	/**
	 * Ermitteln des Polygons (Interior oder Bounding) in dessen Punkteliste
	 * sich der Punkt vertex befindet. Wenn der Punkt kein Polygon-Eckpunkt ist
	 * wird null zurueckgegeben. (quasi-konstante Laufzeit durch Hashing)
	 * 
	 * @param vertex
	 *            Zu ermittelnder Punkt
	 * 
	 * @return Polygon, das den Punkt enthält
	 * 
	 */
	public Polygon2 getPolygonWithVertex(
			Point2 vertex)
	{
		if (vertex == null)
			return null;

		VertexInfo info = (VertexInfo) _table_vertices.get(vertex);
		if (info == null)
			return null;

		return info._polygon;
	} // getPolygonWithVertex


	/**
	 * Ermitteln des Eckpunktes, der vom Eckpunkt vertex aus als naechstes
	 * erreicht wird, wenn das zugehoerige Polygon in der angegebenen Richtung
	 * umrundet wird.
	 * 
	 * Falls der Eckpunkt zu einem offenen Polygon gehört, wird bei dir ==
	 * LEFT_HAND der Vorgaenger in der Punktliste des Polygons zurückgegeben und
	 * bei dir == LEFT_HAND der Nachfolger.
	 * 
	 * 
	 * (quasi-konstante Laufzeit durch Hashing) (siehe auch innere Klasse
	 * VertexInfo)
	 * 
	 * @param vertex
	 *            Ein Eckpunkt des Polygons
	 * @param dir
	 *            LEFT_HAND oder RIGHT_HAND
	 * 
	 * @return Den nächsten Punkt
	 */
	public Point2 getNeighbourVertex(
			Point2 vertex,
			boolean dir)
	{
		if (vertex == null)
			return null;

		VertexInfo info = (VertexInfo) _table_vertices.get(vertex);
		if (info == null)
			return null;

		if (dir == LEFT_HAND)
			return info._vertex_left_hand;
		else
			return info._vertex_right_hand;
	} // getNeighbourVertex


	/**
	 * Ermitteln des Polygons (Interior oder Bounding) zu dem die Kante edge
	 * gehoert. Wenn die Kante zu keinem Polygon gehoert wird null
	 * zurückgegeben. (quasi-konstante Laufzeit durch Hashing)
	 * 
	 * @param edge
	 *            Die zu ermittelnde Kante
	 * 
	 * @return Das Polygon, dass die Kante enthält oder null
	 */
	public Polygon2 getPolygonWithEdge(
			Segment2 edge)
	{
		return getPolygonWithVertex(edge.source());
	} // getPolygonWithEdge


	/**
	 * Ermitteln des Polygons auf dessen Fläche der Punkt point liegt.
	 * 
	 * Es wird zunächst nach einem geschlossenen Interior-Polygon gesucht auf
	 * dessen Fläche der Punkt point liegt. Wird kein Interior-Polygon gefunden,
	 * wird geprüft, ob der Punkt auf der Fläche des Bounding-Polygons liegt.
	 * 
	 * Falls der Punkt weder innnerhalb eines Interior-Polygons noch innerhalb
	 * des Bounding-Polygons liegt, wird null zurueckgegeben, ansonsten das
	 * gefundene Polygon (Interior oder Bounding).
	 * 
	 * @param point
	 *            Der Eingabepunkt
	 * 
	 * @return Das Polygon, das den Punkt enthält
	 * 
	 */
	public Polygon2 getPolygonWithPointInside(
			Point2 point)
	{
		for (int i = 0; i < _polygons.size(); i++)
		{
			Polygon2 poly = (Polygon2) _polygons.elementAt(i);
			if (poly.isOpen())
				continue;

			PolygonInfo info = (PolygonInfo) _table_polygons.get(poly);

			if (info == null)
				continue;

			Rectangle2D box = info._box;

			if (box == null || !box.contains(point.x, point.y))
				continue;

			if (poly.inside(point))
				return poly;
		} // for

		if (_bounding_polygon != null && _bounding_polygon.inside(point))
			return _bounding_polygon; // Bounding-Polygon gefunden

		return null;
	} // getPolygonWithPointInside


	/**
	 * Ermitteln des Eckpunktes der Szene der sich innerhalb des Abstandes
	 * distance am nächsten am Punkt point befindet.
	 * 
	 * Falls fuer einen Eckpunkt vertex gilt vertex == point, so wird dieser bei
	 * der Suche ausgeschlossen.
	 * 
	 * @param point
	 *            Der Eingabepunkt
	 * @param distance
	 *            Der Maximalabstand der Ecke
	 * 
	 * @return Der gefundene Eckpunkt oder null, falls kein Eckpunkt gefunden
	 *         wurde
	 */
	public Point2 getNearestVertex(
			Point2 point,
			double distance)
	{
		double dist_min = distance;

		Point2 vertex = null;

		Polygon2[] polygons = getInteriorPolygons();
		Polygon2 bounding_poly = getBoundingPolygon();

		for (int i = -1; i < polygons.length; i++)
		{
			Polygon2 polygon;

			if (i == -1)
				polygon = bounding_poly;
			else
				polygon = polygons[i];

			if (polygon == null)
				continue;

			Point2 p = polygon.closestPointExcept(point, point, distance);
			if (p == null)
				continue;

			double dist = p.distance(point);
			if (dist < dist_min)
			{
				dist_min = dist;
				vertex = p;
			} // if
		} // for

		return vertex;
	} // getNearestVertex


	/**
	 * Ermitteln des Eckpunktes des Polygons polygon, der sich innerhalb des
	 * Abstandes distance am nächsten am Punkt point befindet.
	 * 
	 * Falls es sich bei dem Polygon nicht um ein Interior-Polygon oder das
	 * Bounding-Polygon handelt, wird null zurückgegeben
	 * 
	 * Falls fuer einen Eckpunkt vertex gilt vertex == point, so wird dieser bei
	 * der Suche ausgeschlossen.
	 * 
	 * @param polygon
	 *            Das Eingabepolygon (Interior)
	 * @param point
	 *            Der Eingabepunkt
	 * @param distance
	 *            Die Maximaldistanz
	 * 
	 * @return Der gefundene Eckpunkt oder null, falls kein Eckpunkt gefunden
	 *         wurde
	 */
	public Point2 getNearestVertex(
			Polygon2 polygon,
			Point2 point,
			double distance)
	{
		if (point == null || polygon == null)
			return null;

		if (!(polygon == _bounding_polygon || _polygons.contains(polygon)))
			return null; // Das Polygon gehoert gar nicht zur Szene

		return polygon.closestPointExcept(point, point, distance);
	} // getNearestVertex


	/**
	 * Ermitteln der Kante der Szene, die sich innerhalb des Abstandes distance
	 * am nächsten am Punkt point befindet.
	 * 
	 * @param point
	 *            Der Eingabepunkt
	 * @param distance
	 *            Die Maximaldistanz
	 * 
	 * @return Die gefundene Kante oder null, falls keine Kante gefunden wurde
	 */
	public Segment2 getNearestEdge(
			Point2 point,
			double distance)
	{
		double dist_min = distance;

		Segment2 sel_edge = null;

		Polygon2[] polygons = getInteriorPolygons();
		Polygon2 bounding_poly = getBoundingPolygon();

		for (int i = -1; i < polygons.length; i++)
		{
			Polygon2 polygon;

			if (i == -1)
				polygon = bounding_poly;
			else
				polygon = polygons[i];

			if (polygon == null)
				continue;

			Segment2[] edges = polygon.edges();

			for (int j = 0; j < edges.length; j++)
			{
				Segment2 edge = edges[j];

				double dist = edge.distance(point);
				if (dist < dist_min)
				{
					dist_min = dist;
					sel_edge = edge;
				} // if
			} // for
		} // for
		return sel_edge;
	} // getNearestEdge


	/**
	 * Ermitteln der Kante des Polygons polygon, die sich innerhalb des
	 * Abstandes distance am naechsten am Punkt point befindet.
	 * 
	 * Falls es sich bei dem Polygon nicht um ein Interior-Polygon oder das
	 * Bounding-Polygon handelt, wird null zurueckgegeben
	 * 
	 * @param polygon
	 *            Das Eingabepolygon (Interior)
	 * @param point
	 *            Der Eingabepunkt
	 * @param distance
	 *            Die Maximaldistanz
	 * 
	 * @return Die gefundene Kante oder null, falls keine Kante gefunden wurde
	 */
	public Segment2 getNearestEdge(
			Polygon2 polygon,
			Point2 point,
			double distance)
	{
		if (point == null || polygon == null)
			return null;

		if (!(polygon == _bounding_polygon || _polygons.contains(polygon)))
			return null; // Das Polygon gehoert gar nicht zur Szene

		double dist_min = distance;

		Segment2 nearest_edge = null;

		Segment2[] edges = polygon.edges();

		for (int j = 0; j < edges.length; j++)
		{
			Segment2 edge = edges[j];

			double dist = edge.distance(point);
			if (dist < dist_min)
			{
				dist_min = dist;
				nearest_edge = edge;
			} // if
		} // for

		return nearest_edge;
	} // getNearestEdge


	/**
	 * Liefert alle Polygone zurück, die genau einen Punkt enhalten. <br>Als
	 * Ergebnis werden die Punkte geliefert.
	 * 
	 * @return Array aller Punkt-Polygone oder null
	 */
	public Point2[] getPoints()
	{
		if (_polygons == null || _polygons.size() == 0)
			return null;

		LinkedList<Point2> result = new LinkedList<Point2>();
		Object[] poly = _polygons.toArray();

		try
		{
			for (int i = 0; i < poly.length; ++i)
			{
				Polygon2 p = (Polygon2) poly[i];
				if (p.length() == 1)
					result.add(p.firstPoint());
			}
		}
		catch (Exception e) //Casten
		{
			return null;
		}

		if (result.isEmpty())
			return null;

		return result.toArray(new Point2[1]);
	} //end getPoints()


	/**
	 * Liefert alle Polygone zurück, die genau n Punkte enhalten. <br>Als
	 * Ergebnis wird ein Array der Polygone zurückgegeben.
	 * 
	 * @param n
	 *            Anzahl der Ecken eines Polygons
	 * 
	 * @return Array aller Dreiecke oder null
	 */
	public Polygon2[] getPolygonsWithNVertices(
			int n)
	{
		return getPolygonsWithNVertices(n, ALL_POLYGONS);
	} //end getTriangles()


	/**
	 * Liefert alle Polygone zurück, die genau n Punkte enhalten. <br>Als
	 * Ergebnis wird ein Array der Polygone zurückgegeben.
	 * 
	 * @param n
	 *            Anzahl der Ecken eines Polygons
	 * @param polytype
	 *            Der Typ der zurückgegebenen Polygon, entsprechend
	 *            {@link #ALL_POLYGONS}, {@link #OPEN_POLYGONS},
	 *            {@link #CLOSED_POLYGONS}
	 * 
	 * @return Array aller spezifizierten Polygone oder null
	 * 
	 * @see #ALL_POLYGONS
	 * @see #OPEN_POLYGONS
	 * @see #CLOSED_POLYGONS
	 */
	public Polygon2[] getPolygonsWithNVertices(
			int n,
			int polytype)
	{
		if (_polygons == null || _polygons.size() == 0 || n <= 0)
			return null;

		LinkedList<Polygon2> result = new LinkedList<Polygon2>();
		Object[] poly = _polygons.toArray();

		try
		{
			for (int i = 0; i < poly.length; ++i)
			{
				Polygon2 p = (Polygon2) poly[i];
				if (p.length() == n && (polytype == ALL_POLYGONS)
						|| (polytype == CLOSED_POLYGONS && p.isClosed())
						|| (polytype == OPEN_POLYGONS && p.isOpen()))
				{
					result.add(p);
				}
			}
		}
		catch (Exception e) //Casten
		{
			return null;
		}

		if (result.isEmpty())
			return null;

		return result.toArray(new Polygon2[1]);
	} //end getTriangles()


	/**
	 * Liefert alle Polygone zurück, die genau zwei Punkte enhalten. <br>Als
	 * Ergebnis werden die Segmente geliefert.
	 * 
	 * @return Array aller Segment-Polygone oder null
	 */
	public Segment2[] getSegments()
	{
		if (_polygons == null || _polygons.size() == 0)
			return null;

		LinkedList<Segment2> result = new LinkedList<Segment2>();
		Object[] poly = _polygons.toArray();

		try
		{
			for (int i = 0; i < poly.length; ++i)
			{
				Polygon2 p = (Polygon2) poly[i];
				if (p.length() == 2)
					result.add(new Segment2(p.firstPoint(), p.lastPoint()));
			}
		}
		catch (Exception e) //Casten
		{
			return null;
		}

		if (result.isEmpty())
			return null;

		return result.toArray(new Segment2[1]);
	} //end getSegments()


	/**
	 * Liefert alle Polygone zurück, die genau drei Punkte enhalten. <br>Als
	 * Ergebnis werden die Dreiecke geliefert.
	 * 
	 * @return Array aller Dreiecke oder null
	 */
	public ConvexPolygon2[] getTriangles()
	{
		if (_polygons == null || _polygons.size() == 0)
			return null;

		LinkedList<ConvexPolygon2> result = new LinkedList<ConvexPolygon2>();
		Object[] poly = _polygons.toArray();

		try
		{
			for (int i = 0; i < poly.length; ++i)
			{
				Polygon2 p = (Polygon2) poly[i];
				if (p.length() == 3 && p.isClosed())
				{
					ConvexPolygon2 cp = new ConvexPolygon2();
					Point2[] points = p.toArray();
					cp.addPoint(points[0]);
					cp.addPoint(points[1]);
					cp.addPoint(points[2]);
					result.add(cp);
				}
			}
		}
		catch (Exception e) //Casten
		{
			return null;
		}

		if (result.isEmpty())
			return null;

		return result.toArray(new ConvexPolygon2[1]);
	} //end getTriangles()


	/**
	 * Testen, ob sich der Punkt point auf der Fläche des Polygons polygon
	 * befindet.
	 * 
	 * Falls es sich bei dem angegebenen Polygon weder um ein geschlossenes
	 * Polygon der Szene noch um des Bounding-Polygon handelt, wird false
	 * zureckgegeben.
	 * 
	 * @param point
	 *            Der Eingabepunkt
	 * @param polygon
	 *            Das Eingabepolygon
	 * 
	 * @return true, wenn das Polygon den Punkt enthält, false sonst
	 */
	public boolean isPointInsidePolygon(
			Point2 point,
			Polygon2 polygon)
	{
		if (point == null || polygon == null || polygon.isOpen())
			return false;

		if (!(polygon == _bounding_polygon || _polygons.contains(polygon)))
			return false; // Das Polygon gehoert gar nicht zur Szene

		return polygon.inside(point);
	} // isPointInsidePolygon


	//------------------------------------------------------------
	// Diverse Validitaetspruefungen
	//------------------------------------------------------------

	/**
	 * Überprüfen ob die Szene nach dem Einfügen des Interior-Polygons oder
	 * Bounding-Polygons poly noch gültig ist.
	 * 
	 * Es wird davon ausgegangen, dass das Polygon selbst ein gültiges Polygon
	 * ist.
	 * 
	 * @param test_poly
	 *            Das Eingabepolygon
	 * 
	 * @return true, wenn die Scene gültig ist, false sonst
	 */
	public boolean polygonIsValid(
			Polygon2 test_poly)
	{
		// Ermitteln des Bounding-Rechtecks des zu testenden Polygons

		Rectangle2D test_poly_rect = test_poly.getBoundingRect();
		if (test_poly_rect == null)
			return true; // Kein BoundingRect, also auch keine Schnitte

		// Das zu testende Polygon in Bezug auf alle anderen Interior-
		// Polygone und ggf. das Bounding-Polygon pruefen

		Segment2[] test_poly_segments = _getPolygonSegments(test_poly);

		Polygon2[] polygons = getInteriorPolygons();
		Polygon2 bounding_poly = getBoundingPolygon();

		for (int i = -1; i < polygons.length; i++)
		{
			Polygon2 poly;

			if (i == -1)
				// Erstmal in Bezug auf Bounding-Poly testen
				poly = bounding_poly;
			else
				poly = polygons[i];

			if (poly == null)
				continue;

			if (poly == test_poly)
				continue; // Das zu testende Polygon darf nicht
			// auf Schnitt mit sich selbst getestet zu
			// werden.

			Rectangle2D poly_rect = getBoundingBox(poly);
			if (poly_rect == null)
				continue; // Kein BoundingRect, also auch kein Schnitt

			// Das Ueberschneidungsrechteck der BoundingRects ermitteln
			Rectangle2D intersect_rect = poly_rect
					.createIntersection(test_poly_rect);

			boolean rectangle_intersection = (intersect_rect.getWidth() >= 0.0 && intersect_rect
					.getHeight() >= 0.0);

			if (rectangle_intersection)
			{
				// Alle Segmente des zu testenden Polygons, die das
				// Ueberschneidungsrechteck schneiden mit allen Segmenten
				// des aktuellen Polygons, die das Ueberschneidungsrechteck
				// schneiden, auf Schnitt testen

				if (Segment2.intersects(_getPolygonSegments(poly),
						test_poly_segments, intersect_rect))
					return false;
			} // if

			if (poly == bounding_poly)
			{
				// Testen, ob das zu testende Polygon ganz ausserhalb
				// des Bounding-Polygons ist
				if (!poly.inside(test_poly.firstPoint()))
					return false;
			}
			else
			{
				// Testen, ob das zu testende Polygon ganz im aktuellen
				// Polygon enthalten ist, falls dieses geschlossen ist
				if (rectangle_intersection && poly.isClosed()
						&& poly.inside(test_poly.firstPoint()))
					return false;
			} // else

			if (test_poly == bounding_poly)
			{
				// Testen, ob das zu aktuelle Polygon ganz ausserhalb
				// des Bounding-Polygons ist
				if (!test_poly.inside(poly.firstPoint()))
					return false;
			}
			else
			{
				// Testen, ob das zu aktuelle Polygon ganz im zu
				// testenden Polygon enthalten ist, falls dieses
				// geschlossen ist
				if (rectangle_intersection && test_poly.isClosed()
						&& test_poly.inside(poly.firstPoint()))
					return false;
			} // else
		} // for

		return true;
	} // polygonIsValid


	/**
	 * Überprüfen, ob die Szene nach dem Ersatz von old_poly durch new_poly
	 * weiterhin gültig ist.
	 * 
	 * Das Polygon new_poly ist eine Kopie des Polygons old_poly, wobei einige
	 * oder alle Punkte um die gleichen x- und y-Offsets gegenüber den Punkten
	 * in old_poly verschoben sind.
	 * 
	 * @param new_poly
	 *            Das neue Polygon
	 * @param old_poly
	 *            Das alte, zu ersetzende Polygon
	 * 
	 * @return true, wenn die Scene nach Einfügen weiterhin gültig ist, false
	 *         sonst
	 */
	public boolean polygonIsValid(
			Polygon2 new_poly,
			Polygon2 old_poly)
	{
		if (new_poly.length() < 2)
			return polygonIsValid(new_poly);

		Segment2[] new_segs = new_poly.edges();
		Segment2[] old_segs = old_poly.edges();

		// Erzeuge Segmentliste seg_move, die alle Segmente aus
		// new_segs enthaelt, bei denen sich mindestens ein Punkt
		// bewegt hat.

		// Erzeuge Segmentliste seg_fix, die alle Segmente aus
		// new_segs enthaelt, bei denen sich mindestens ein Punkt
		// nicht bewegt hat.

		Vector v_move = new Vector();
		Vector v_fix = new Vector();
		for (int i = 0; i < new_segs.length; i++)
		{
			Segment2 new_seg = new_segs[i];
			Segment2 old_seg = old_segs[i];

			Point2 new_pt1 = new_seg.source();
			Point2 new_pt2 = new_seg.target();

			if (new_pt1.equals(new_pt2))
				return false; // Uebereinanderliegende Eckpunkte

			Point2 old_pt1 = old_seg.source();
			Point2 old_pt2 = old_seg.target();

			if (new_pt1.equals(old_pt1) || new_pt2.equals(old_pt2))
				v_fix.addElement(new_seg);

			if (!new_pt1.equals(old_pt1) || !new_pt2.equals(old_pt2))
				v_move.addElement(new_seg);

		} // for

		if (v_move.size() == 0)
			return true; // Es hat sich kein Punkt bewegt

		if (v_fix.size() == 0)
			return polygonIsValid(new_poly);
		// Es haben sich alle Punkte bewegt

		Segment2[] seg_move = new Segment2[v_move.size()];
		v_move.copyInto(seg_move);

		Segment2[] seg_fix = new Segment2[v_fix.size()];
		v_fix.copyInto(seg_fix);

		// Die BoundingRects der Segmente aus seg_move und
		// der Segmente aus seg_fix berechnen

		Rectangle2D seg_move_rect = Segment2.getBoundingRect(seg_move);
		Rectangle2D seg_fix_rect = Segment2.getBoundingRect(seg_fix);

		// Das Ueberschneidungsrechteck der BoundingRects ermitteln
		Rectangle2D intersect_rect = seg_move_rect
				.createIntersection(seg_fix_rect);

		if (intersect_rect.getWidth() >= 0.0
				&& intersect_rect.getHeight() >= 0.0)
		{
			// Alle Segmente aus seg_move, die das
			// Ueberschneidungsrechteck schneiden mit allen Segmenten
			// aus seg_fix, die das Ueberschneidungsrechteck
			// schneiden, auf Schnitt testen

			if (Segment2.intersects(seg_fix, seg_move, intersect_rect))
				return false;
		} // if

		// Die Segmente aus seg_move in Bezug auf alle anderen Interior-
		// Polygone und ggf. das Bounding-Polygon pruefen

		Polygon2[] polygons = getInteriorPolygons();
		Polygon2 bounding_poly = getBoundingPolygon();
		for (int i = -1; i < polygons.length; i++)
		{
			Polygon2 poly;

			if (i == -1)
				// Erstmal in Bezug auf Bounding-Poly testen
				poly = bounding_poly;
			else
				poly = polygons[i];

			if (poly == null)
				continue;

			if (poly == new_poly)
				continue; // Das zu testende Polygon braucht nicht
			// auf Schnitt mit sich selbst getestet zu
			// werden.

			Rectangle2D poly_rect = getBoundingBox(poly);
			if (poly_rect == null)
				continue; // Kein BoundingRect, also auch kein Schnitt

			// Das Ueberschneidungsrechteck der BoundingRects ermitteln
			intersect_rect = poly_rect.createIntersection(seg_move_rect);

			boolean rectangle_intersection = (intersect_rect.getWidth() >= 0.0 && intersect_rect
					.getHeight() >= 0.0);

			if (rectangle_intersection)
			{
				// Alle Segmente aus seg_move, die das
				// Ueberschneidungsrechteck schneiden mit allen Segmenten
				// des aktuellen Polygons, die das Ueberschneidungsrechteck
				// schneiden, auf Schnitt testen

				if (Segment2.intersects(_getPolygonSegments(poly), seg_move,
						intersect_rect))
					return false;
			} // if

			if (new_poly == bounding_poly)
			{
				// Testen, ob das zu aktuelle Polygon ganz ausserhalb
				// des Bounding-Polygons ist
				if (!new_poly.inside(poly.firstPoint()))
					return false;
			}
			else
			{
				// Testen, ob das aktuelle Polygon ganz im zu
				// testenden Polygon enthalten ist, falls dieses
				// geschlossen ist
				if (rectangle_intersection && new_poly.isClosed()
						&& new_poly.inside(poly.firstPoint()))
					return false;
			} // else
		} // for

		return true;
	} // polygonIsValid


	/**
	 * Überprüfen der Gültigkeit der Polygonszene in Bezug auf das gegebene
	 * Segment seg, das zu einem offenen Interior-Polygon gehört.
	 * 
	 * Es wird davon ausgegangen, dass die Szene ohne das Segment gültig war.
	 * 
	 * @param seg
	 *            Das Eingabesegment
	 * 
	 * @return true, das Segment gehört zu einem offnen Interior-Polygon, false
	 *         sonst
	 */
	public boolean segmentIsValid(
			Segment2 seg)
	{
		if (seg.source().equals(seg.target()))
			return false; // uebereinanderliegende Eckpunkte

		Segment2[] segments = new Segment2[1];
		segments[0] = seg;

		Rectangle2D seg_rect = Segment2.getBoundingRect(segments);

		Polygon2[] polygons = getInteriorPolygons();
		Polygon2 bounding_poly = getBoundingPolygon();
		for (int i = -1; i < polygons.length; i++)
		{
			Polygon2 poly;

			if (i == -1)
				// Erstmal in Bezug auf Bounding-Poly testen
				poly = bounding_poly;
			else
				poly = polygons[i];

			if (poly == null)
				continue;

			Rectangle2D poly_rect = getBoundingBox(poly);
			if (poly_rect == null)
				continue; // Kein BoundingRect, also auch kein Schnitt

			// Das Ueberschneidungsrechteck der BoundingRects ermitteln
			Rectangle2D intersect_rect = poly_rect.createIntersection(seg_rect);

			if (intersect_rect.getWidth() >= 0.0
					&& intersect_rect.getHeight() >= 0.0)
			{
				// Alle Segmente aus segments, die das
				// Ueberschneidungsrechteck schneiden mit allen Segmenten
				// des aktuellen Polygons, die das Ueberschneidungsrechteck
				// schneiden, auf Schnitt testen

				if (Segment2.intersects(_getPolygonSegments(poly), segments,
						intersect_rect))
					return false;
				//return true;
			} // if
		} // for

		return true;
	} // segmentIsValid


	/**
	 * Überprüfen der Gültigkeit der Polygonszene in Bezug auf das geschlossene
	 * Interior-Polygon poly.
	 * 
	 * Es wird davon ausgegangen, dass die Szene vor dem Schliessen des Polygons
	 * gültig war und auch das schliessende Segment gültig ist.
	 * 
	 * Es wird also nur getestet, ob alle Interior-Polygone ausserhalb des
	 * Polygons poly liegen
	 * 
	 * @param poly
	 *            Das Eingabepolygon
	 * 
	 * @return true, die Scene ist gültig, false sonst
	 */
	public boolean closingIsValid(
			Polygon2 poly)
	{
		Polygon2[] polygons = getInteriorPolygons();

		for (int i = 0; i < polygons.length; i++)
		{
			if (poly.inside(polygons[i].firstPoint()))
				return false;
		} // for

		return true;
	} // closingIsValid


	/**
	 * Überprüfen der Gültigkeit der Polygonszene in Bezug auf das
	 * Bounding-Polygon poly.
	 * 
	 * Es wird davon ausgegangen, dass die Szene vor dem Schliessen des Polygons
	 * gueltig war und auch das schliessende Segment gueltig ist.
	 * 
	 * Es wird also nur getestet, ob alle Interior-Polygone innerhalb des
	 * Polygons poly liegen
	 * 
	 * @param poly
	 *            Das Eingabepolygon
	 * 
	 * @return true, Scene/Bounding-Polygon gültig bezgl. des Eingabepolygons,
	 *         false sonst
	 * 
	 */
	public boolean boundingIsValid(
			Polygon2 poly)
	{
		Polygon2[] polygons = getInteriorPolygons();

		for (int i = 0; i < polygons.length; i++)
		{
			if (!poly.inside(polygons[i].firstPoint()))
				return false;
		} // for

		return true;
	} // boundingIsValid


	/**
	 * Überprüfen ob die als Polygon gegebene Fläche vollständig ausserhalb
	 * aller Interior-Polygone und ggf. vollständig innerhalb des
	 * Bounding-Polygons liegt. Dabei sind Berührungen der Fläche mit den
	 * Polygonen der Szene erlaubt.
	 * 
	 * Achtung !!! Diese Methode ist noch nicht richtig implementiert. Momentan
	 * liefert die Methode auch dann false wenn es zwar nicht zu Durchdringungen
	 * aber zu Berührungen mit den Polygonen der Szene kommt
	 * 
	 * Es wird davon ausgegangen, dass es sich bei dem gegebenen Polygon um ein
	 * geschlossenes Polygon handelt
	 * 
	 * @param test_poly
	 *            Das Eingabepolygon
	 * 
	 * @return true, Polygon außerhalb aller Interior-Polygone und innerhalb des
	 *         Bounding-Polygons, false sonst<br>true == Das Polygon test_poly
	 *         bedeckt ausschliesslich freie Fläche (also keinen Punkt innerhalb
	 *         eines Interior-Polygons oder ausserhalb des Bounding-Polygons)
	 */
	public boolean isFreeSpace(
			Polygon2 test_poly)
	{
		//-- Hier muss der Tatsache Rechnung getragen werden, dass Beruehrungen
		//   mit Polygonen der Szene erlaubt sind.
		return polygonIsValid(test_poly);
	} // isFreeSpace


	/**
	 * Vergleicht zwei Szenen, indem alle Polygone verglichen werden.
	 * 
	 * @author Jörg Wegener
	 * 
	 * @param otherPolygonSceneData
	 *            SceneData, mit dem verglichen werden soll
	 * 
	 * @return true, wenn die PolygonSzenen gleich sind, sonst false
	 */
	public boolean equals(
			Polygon2SceneData otherPolygonSceneData)
	{
		Polygon2 otherBounding = otherPolygonSceneData._bounding_polygon;
		if (!(_bounding_polygon == null ? otherBounding == null
				: _bounding_polygon.equals(otherBounding)))
			return false;
		Vector otherPolygons = otherPolygonSceneData._polygons;
		if (_polygons.size() != otherPolygons.size())
			return false;
		for (int i = 0; i < _polygons.size(); i++)
		{
			if (!((Polygon2) _polygons.get(i)).equals((Polygon2) otherPolygons
					.get(i)))
				return false;
		}
		return true;
	}


	//************************************************************
	// private methods
	//************************************************************

	/**
	 * Einfügen des Polygons poly in HashTables _table_vertices und _table_poly.
	 * 
	 * @param poly
	 *            Das einzufügende Eingabepolygon
	 */
	private void _putPolygonToHashTables(
			Polygon2 poly)
	{
		// Informationen ueber das Polygon in _table_polygons speichern

		PolygonInfo poly_info = new PolygonInfo();
		poly_info._box = poly.getBoundingRect();

		_table_polygons.put(poly, poly_info);

		// Informationen ueber alle Eckpunkte des Polygons in
		// _table_vertices speichern

		int no_of_vertices = poly.length();

		if (no_of_vertices == 0) // Keine Eckpunkte
			return;

		if (no_of_vertices == 1) // 1 Eckpunkt
		{
			VertexInfo vertex_info = new VertexInfo();
			vertex_info._polygon = poly;
			vertex_info._vertex_left_hand = null;
			vertex_info._vertex_right_hand = null;

			_table_vertices.put(poly.firstPoint(), vertex_info);
			return;
		} // if

		boolean ori = _getOrientation(poly);

		PointsAccess access = new PointsAccess(poly);

		Point2 pt_prev = null;
		Point2 pt_curr = access.nextPoint();

		while (pt_curr != null)
		{
			Point2 pt_next = access.nextPoint();

			VertexInfo vertex_info = new VertexInfo();

			vertex_info._polygon = poly;

			Point2 pt1 = pt_prev;
			Point2 pt2 = pt_next;

			if (poly.isClosed())
			{
				if (pt1 == null) // erster Punkt der Liste
					pt1 = poly.lastPoint();

				if (pt2 == null) // letzter Punkt der Liste
					pt2 = poly.firstPoint();
			} // if

			if (ori == LEFT_HAND)
			{
				vertex_info._vertex_right_hand = pt1;
				vertex_info._vertex_left_hand = pt2;
			}
			else
			{
				vertex_info._vertex_left_hand = pt1;
				vertex_info._vertex_right_hand = pt2;
			} // else

			_table_vertices.put(pt_curr, vertex_info);

			pt_prev = pt_curr;
			pt_curr = pt_next;
		} // while

	} // _putPolygonToHashTables


	/**
	 * Entfernen des Polygons poly aus HashTables _table_vertices und
	 * _table_poly.
	 * 
	 * @param poly
	 *            Das zu löschende Eingabepolygon
	 */
	private void _removePolygonFromHashTables(
			Polygon2 poly)
	{
		PointsAccess access = new PointsAccess(poly);
		for (Point2 pt = access.nextPoint(); pt != null; pt = access
				.nextPoint())
		{
			_table_vertices.remove(pt);
		} // for

		_table_polygons.remove(poly);
	} // _removePolygonFromHashTables


	/**
	 * Neuberechnen des umschliessenden Rechtecks aller Interior-Polygone
	 */
	private void _recalcBoundingBox()
	{
		Rectangle2D box = null;

		for (int i = 0; i < _polygons.size(); i++)
		{
			Polygon2 poly = (Polygon2) _polygons.elementAt(i);

			PolygonInfo info = (PolygonInfo) _table_polygons.get(poly);

			if (info == null)
				continue;

			Rectangle2D poly_box = info._box;

			if (poly_box == null)
				continue;

			if (box == null)
				box = poly_box;
			else
				box = box.createUnion(poly_box);
		} // for

		_polygons_box = box;
	} // _recalcBoundingBox


	/**
	 * Alle Segmente des Polygons poly ermitteln.
	 * 
	 * Falls das Polygon nur aus einem Punkt point besteht, wird dieser Punkt
	 * als Segment von point nach point zurueckgegeben
	 * 
	 * @param poly
	 *            Das Eingabepolygon
	 * 
	 * @return Alle Segmente des Polygons, bzw. Punkt als Segment, falls das
	 *         Polygon nur aus einen Punkt besteht
	 */
	private static Segment2[] _getPolygonSegments(
			Polygon2 poly)
	{
		Segment2[] poly_segments;
		if (poly.length() == 1)
		{
			poly_segments = new Segment2[1];
			Point2 point = poly.firstPoint();
			poly_segments[0] = new Segment2(point, point);
		}
		else
			poly_segments = poly.edges();

		return poly_segments;
	} // _getPolygonSegments


	/**
	 * Ermittelt die durch die Punkteliste definierte Umlaufrichtung der
	 * Eckpunkte des gegebenen Polygons. Es wird die Richtung (
	 * {@link #LEFT_HAND} oder {@link #RIGHT_HAND}) zurueckgegeben, in der man
	 * sich bewegt, wenn man die Punkte der Punkteliste des Polygons der Reihe
	 * nach besucht.
	 * 
	 * Falls das Polygon offen ist, wird immer {@link #LEFT_HAND}
	 * zurueckgegeben.
	 * 
	 * Falls es sich bei dem Polygon um ein Interior-Polygon handelt, so
	 * bedeutet {@link #LEFT_HAND} das gleiche wie gegen den Uhrzeigersinn. Bei
	 * einem Bounding-Polygon ist die Umrundung mit der linken Hand am Polygon
	 * gleichbedeutend mit einer Umrundung im Uhrzeigersinn.
	 * 
	 * @param poly
	 *            Das Eingabepolygon
	 * 
	 * @return {@link #LEFT_HAND} oder {@link #RIGHT_HAND}
	 * 
	 * @see #LEFT_HAND
	 * @see #RIGHT_HAND
	 */
	private boolean _getOrientation(
			Polygon2 poly)
	{
		if (poly.isOpen() || poly.length() < 3)
			return LEFT_HAND;

		boolean ori;

		if (poly.getOrientation() == Point2.ORIENTATION_LEFT)
			ori = LEFT_HAND;
		else
			ori = RIGHT_HAND;

		if (poly == _bounding_polygon)
			ori = !ori;

		return ori;
	} // _getOrientation


	//************************************************************
	// innere Klassen
	//************************************************************

	/**
	 * Innere Klasse PolygonInfo fasst Informationen zu einem Polygon zusammen.
	 */
	private class PolygonInfo
			implements java.io.Serializable
	{

		protected Rectangle2D	_box	= null; // Bounding-Box
	} // PolygonInfo


	/**
	 * Innere Klasse VertexInfo fasst Informationen zu einem Polygoneckpunkt
	 * zusammen.
	 */
	private class VertexInfo
			implements java.io.Serializable
	{

		protected Polygon2	_polygon			= null; // Polygon, zu dem der Eckpunkt
		// gehoert

		// Nachbar-Eckpunkte auf dem Weg entlang der Polygon-Kanten
		// ( linke/rechte Hand am Polygon )
		//
		protected Point2	_vertex_left_hand	= null;
		protected Point2	_vertex_right_hand	= null;
		//
		// Zu beachten !!!
		//
		// Wenn der Eckpunkt zu einem offenen Polygon gehoert, so ist
		// _vertex_left_hand der Vorgaenger in der Punktliste des
		// Polygons und _vertex_right_hand der Nachfolger.
		//
		// Wenn der Eckpunkt der einzige Punkt seines Polygons ist, so
		// gilt
		//    _vertex_left_hand  == _vertex_right_hand == null.

	} // VertexInfo

} // Polygon2SceneData
