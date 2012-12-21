package anja.geom.triangulation;


import anja.geom.Point2;
import anja.geom.Polygon2;


/**
 * Diese Klasse repraesentiert die Dreiecke der Triangulation. Jedes Dreieck
 * kann bis zu drei Nachbardreiecke haben.
 * 
 * @version 0.1 22.08.2003
 * @author Sascha Ternes
 */

public final class Triangle
		extends Polygon2
{

	// *************************************************************************
	// Private variables
	// *************************************************************************

	/** die Connector-Objekte zu den Nachbar-Dreiecken */
	private Connector[]	_connectors;


	// *************************************************************************
	// Constructors
	// *************************************************************************

	/**
	 * Erzeugt ein leeres Polygon.
	 */
	private Triangle()
	{

		super();

	} // Triangle


	/**
	 * Erzeugt ein neues Dreieck mit den spezifizierten Punkten.
	 * 
	 * @param a
	 *            Erster Punkt
	 * @param b
	 *            Zweiter Punkt
	 * @param c
	 *            Dritter Punkt
	 */
	Triangle(
			Point2 a,
			Point2 b,
			Point2 c)
	{

		super();
		addPoint(a);
		addPoint(b);
		addPoint(c);
		_connectors = new Connector[3];

	} // Triangle


	// *************************************************************************
	// Public methods
	// *************************************************************************

	/**
	 * Liefert die Anzahl der Nachbarn. Ein Dreieck kann bis zu drei Nachbarn
	 * haben.
	 * 
	 * @return die Zahl der Nachbarn
	 */
	public int neighborCount()
	{

		int n = 0;
		for (int i = 0; i < 3; i++)
		{
			if (_connectors[i] != null)
				n++;
		} // for
		return n;

	} // neighborCount


	/**
	 * Liefert die Nachbarn des Dreiecks.
	 * 
	 * @return ein dreielementiges Array, das maximal drei Nachbarn enthaelt.
	 */
	public Triangle[] getNeighbors()
	{

		Triangle[] neighbors = new Triangle[3];

		return neighbors;

	} // getNeighbors


	/**
	 * Fuegt einen Nachbarn hinzu.
	 * 
	 * @param neighbor
	 *            ein <code>Connector</code>-Objekt, das die
	 *            Nachbarschaftsbeziehung beschreibt
	 */
	void addNeighbor(
			Connector neighbor)
	{

		if (_connectors[0] == null)
			_connectors[0] = neighbor;
		else if (_connectors[1] == null)
			_connectors[1] = neighbor;
		else if (_connectors[2] == null)
			_connectors[2] = neighbor;

	} // addNeighbor

} // Triangle
