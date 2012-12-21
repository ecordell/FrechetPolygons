package anja.ratgeom;


/**
 * PolygonAccess ist aehnlich wie PointsAccess. Es dient speziell zum Zugreifen
 * auf die Punkte eines Polygons in <BR>zyklischer Reihenfolge, d. h. es gibt
 * keinen letzten Punkt. <BR>Mit currentPoint() kann man mehrfach auf den
 * aktuellen Punkt zugreifen. Mit nextPoint() und prevPoint() kann man den
 * Nachfolger bzw. Vorgaenger zugreifen.
 * @version 0.1 26.11.1997
 * @author Lihong Ma
 */

public class PolygonAccess
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
	 * Gibt den Nachfolger des zuletzt zugegriffenen Punkts zurueck.
	 * 
	 * @return Der Nachfolger
	 */
	public Point2 nextPoint()
	{
		return _pa.cyclicNextPoint();
	}


	/**
	 * Gibt den Vorgaenger des zuletzt zugegriffenen Punkts zurueck.
	 * 
	 * @return Der Vorgaenger
	 */
	public Point2 prevPoint()
	{
		return _pa.cyclicPrevPoint();
	}


	/**
	 * Gibt den aktuellen Punkt zurueck, vorher muss mindestens ein Aufruf von
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

}
