package anja.geom;


/**
 * PolygonAccess ist aehnlich wie PointsAccess. Es dient speziell zum Zugreifen
 * auf die Punkte eines Polygons in <BR>zyklischer Reihenfolge, d. h. es gibt
 * keinen letzten Punkt. <BR>Mit currentPoint() kann man mehrfach auf den
 * aktuellen Punkt zugreifen. Mit nextPoint() und prevPoint() kann man den
 * Nachfolger bzw. Vorgaenger zugreifen.
 * 
 * @version 0.2 05.05.1997
 * @author Lihong Ma
 */

public class PolygonAccess
		implements java.io.Serializable
{

	// ************************************************************************
	// Variables
	// ************************************************************************

	PointsAccess	_pa;
	Polygon2		_poly;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt einen PolygonAccess, der am Anfang des Eingabepolygons steht.
	 * 
	 * @param poly
	 *            Das Eingabepolygon
	 */
	public PolygonAccess(
			Polygon2 poly)
	{
		_poly = poly;
		_pa = new PointsAccess(_poly);
	} // PolygonAccess


	/**
	 * Erzeugt einen PolygonAccess der an der gleichen position steht wie der
	 * Eingabe-PolygonAccess.
	 * 
	 * @param input_polygon_access
	 *            Identisches Polygonaccess-Objekt
	 */
	public PolygonAccess(
			PolygonAccess input_polygon_access)
	{
		_pa = new PointsAccess(input_polygon_access._pa);
		_poly = input_polygon_access._poly;
	}


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Gibt den Nachfolger des zuletzt zugegriffenen Punkts zurück
	 * 
	 * @return Der Nachfolger
	 */
	public Point2 nextPoint()
	{
		return _pa.cyclicNextPoint();
	}


	/**
	 * Gibt den Vorgänger des zuletzt zugegriffenen Punkts zurück
	 * 
	 * @return Der Vorgänger
	 */
	public Point2 prevPoint()
	{
		return _pa.cyclicPrevPoint();
	}


	/**
	 * Gibt den aktuellen Punkt zurück, vorher muss mindestens ein Aufruf von
	 * nextPoint() oder prevPoint() erfolgt sein, damit der aktuelle Punkt
	 * gesetzt worden ist.
	 * 
	 * @return Der aktuelle Punkt.
	 */
	public Point2 currentPoint()
	{
		return _pa.currentPoint();
	}


	// ************************************************************************
	//
	// Erweiterung von Pit Prüßner
	// 23.01.2008
	// Um direkt auf die Kanten eines Polygons zuzugreifen

	/**
	 * Gibt den ersten Punkt des Polygons zurück
	 * 
	 * @return Der erste Punkt
	 */
	public Point2 firstVertex()
	{
		PointsAccess pa_temp = new PointsAccess(_poly);
		pa_temp.nextPoint();
		return pa_temp.currentPoint();
	}


	/**
	 * Gibt den letzten Punkt des Polygons zurück
	 * 
	 * @return Der letzte Punkt
	 */
	public Point2 lastVertex()
	{
		PointsAccess pa_temp = new PointsAccess(_poly);
		while (pa_temp.hasNextPoint())
		{
			pa_temp.nextPoint();
		}
		return pa_temp.currentPoint();
	}


	/**
	 * Gibt die erste Kante des Polygons zurück
	 * 
	 * @return Erste Kante
	 */
	public Segment2 firstEdge()
	{
		PointsAccess pa_temp = new PointsAccess(_poly);
		Point2 source = pa_temp.nextPoint();
		Point2 target = pa_temp.nextPoint();
		return new Segment2(source, target);
	}


	/**
	 * Gibt die letzte Kante des Polygons zurück
	 * 
	 * @return Letzte Kante
	 */
	public Segment2 lastEdge()
	{
		PointsAccess pa_temp = new PointsAccess(_poly);
		Segment2 edge = null;
		while (pa_temp.hasNextPoint())
		{
			edge = new Segment2(pa_temp.currentPoint(), pa_temp.nextPoint());
		}
		return edge;
	}


	/**
	 * Testet ob eine nächste Ecke vorhanden ist. Dieser Test ist natürlich
	 * nicht zyklisch!
	 * 
	 * @return true, wenn der Punkt einen Nachfolger hat, false sonst
	 */
	public boolean hasNextPoint()
	{
		return _pa.hasNextPoint();
	}


	/**
	 * Testet ob eine nächste Kante vorhanden ist. Dieser Test ist natürlich
	 * nicht zyklisch!
	 * 
	 * @return true, wenn die Kante einen Nachfolger hat, false sonst
	 */
	public boolean hasNextEdge()
	{
		return _pa.hasNextPoint() && !_pa.getSucc().equals(this.lastVertex());
	}


	/**
	 * Testet ob eine vorherige Ecke vorhanden ist. Dieser Test ist natürlich
	 * nicht zyklisch!
	 * 
	 * @return true, wenn der Punkt einen Vorgänger hat, false sonst
	 */
	public boolean hasPrevPoint()
	{
		return _pa.hasPrevPoint();
	}


	/**
	 * Testet ob eine vorherige Kante vorhanden ist. Dieser Test ist natürlich
	 * nicht zyklisch!
	 * 
	 * @return true, wenn die Kante einen Vorgänger hat, false sonst
	 */
	public boolean hasPrevEdge()
	{
		return _pa.hasPrevPoint();
	}


	/**
	 * Liefert die aktuelle Kante des Polygons (Kante mit der aktuellen Ecke als
	 * Startpunkt)
	 * 
	 * @return Die aktuelle Kante
	 */
	public Segment2 currEdge()
	{
		Point2 start = _pa.currentPoint();
		Point2 end = _pa.getSucc();
		Segment2 edge = new Segment2(start, end);

		return edge;
	}


	/**
	 * Liefert die nächste Kante des Polygons indem der Zeiger auf die nächste
	 * Polygonecke gesetzt wird.
	 * 
	 * @return Nächste Kante
	 */
	public Segment2 nextEdge()
	{
		_pa.nextPoint();
		Point2 start = _pa.currentPoint();
		Point2 end = _pa.getSucc();
		Segment2 edge = new Segment2(start, end);

		return edge;
	}


	/**
	 * Liefert die vorherige Kante des Polygons indem der Zeiger auf die
	 * vorherige Polygonecke gesetzt wird
	 * 
	 * @return Vorherige Kante
	 */
	public Segment2 prevEdge()
	{
		_pa.prevPoint();
		Point2 start = _pa.currentPoint();
		Point2 end = _pa.getSucc();
		Segment2 edge = new Segment2(start, end);

		return edge;
	}


	/**
	 * Liefert die Nummer der aktuellen Kante zurück
	 * 
	 * @return Die Nummer der Kante
	 */
	public int numberOfCurrentEdge()
	{
		int i = 0;
		Segment2 edge = this.currEdge();
		PolygonAccess temp_pa = new PolygonAccess(_poly);
		while (temp_pa.hasNextEdge())
		{
			i = i + 1;
			if (edge.equals(temp_pa.nextEdge()))
				return i;
		}
		return -1;
	}


	/**
	 * Löscht alle Daten dieses Objekts
	 */
	public void reset()
	{
		_pa.reset();
	}

}
