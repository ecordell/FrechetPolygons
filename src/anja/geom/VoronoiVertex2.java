package anja.geom;


import java.util.HashSet;
import java.util.Iterator;


/**
 * Hilfsklasse fuer zweidimensionale Voronoi-Diagramme, die einen Voronoi-Knoten
 * beschreibt.<br>Ein Voronoi-Knoten ist ein zweidimensionaler Punkt mit einer
 * zusaetzlichen Liste seiner anliegenden Voronoi-Bisektoren (und damit der
 * Voronoi-Regionen, die an ihn grenzen).
 * 
 * @version 0.5 27.03.2003
 * @author Sascha Ternes
 */

public class VoronoiVertex2
		extends Point2
{

	//**************************************************************************
	// Private variables
	//**************************************************************************

	// Menge der anliegenden Bisektoren:
	private HashSet	_bisectors;


	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt einen neuen Voronoi-Knoten an den uebergebenen Koordinaten. Seine
	 * Menge der anliegenden Bisektoren ist zunaechst leer; anliegende
	 * Bisektoren werden mit der Methode @see #addBisector(VoronoiBisector2)
	 * registriert.
	 * 
	 * @param x
	 *            x- und
	 * @param y
	 *            y-Koordinate des Voronoi-Knotens
	 */
	public VoronoiVertex2(
			float x,
			float y)
	{

		super(x, y);
		_bisectors = new HashSet(3, 1.0f);

	} // VoronoiVertex2


	/**
	 * Erzeugt einen neuen Voronoi-Knoten aus dem uebergebenen Punkt. Seine
	 * Menge der anliegenden Bisektoren ist zunaechst leer; anliegende
	 * Bisektoren werden mit der Methode @see #addBisector(VoronoiBisector2)
	 * registriert.
	 * 
	 * @param point
	 *            der Punkt des neuen Voronoi-Knotens
	 */
	public VoronoiVertex2(
			Point2 point)
	{

		super(point);
		_bisectors = new HashSet(3, 1.0f);

	} // VoronoiVertex2


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/**
	 * Fuegt den spezifizierten Bisektor der Menge der anliegenden Bisektoren
	 * dieses Voronoi-Knotens hinzu, falls er noch nicht registriert ist.
	 * 
	 * @param bisector
	 *            der hinzuzufuegende Bisektor
	 *            
	 * @throws java.lang.NullPointerException
	 *             falls <code>null</code> uebergeben wurde
	 */
	public void addBisector(
			VoronoiBisector2 bisector)
	{

		// null abfangen:
		if (bisector == null)
		{
			throw new NullPointerException();
		} // if
		_bisectors.add(bisector);

	} // addBisector


	/**
	 * Liefert eine ungeordnete Aufzaehlung der registrierten Bisektoren dieses
	 * Voronoi-Knotens.
	 * 
	 * @return eine ungeordnete Aufzaehlung der registrierten Bisektoren
	 */
	public Iterator bisectors()
	{

		return _bisectors.iterator();

	} // bisectors


	/**
	 * Liefert die Zahl der registrierten Bisektoren.
	 * 
	 * @return die Zahl der registrierten Bisektoren
	 */
	public int bisectorCount()
	{

		return _bisectors.size();

	} // bisectorCount


	/**
	 * Entfernt den spezifizierten Bisektor aus der Liste der registrierten
	 * Bisektoren dieses Voronoi-Knotens. Falls der Bisektor nicht registriert
	 * ist, passiert nichts.
	 * 
	 * @param bisector
	 *            der zu entfernende Bisektor
	 */
	public void removeBisector(
			VoronoiBisector2 bisector)
	{

		_bisectors.remove(bisector);

	} // removeBisector

} // VoronoiVertex2
