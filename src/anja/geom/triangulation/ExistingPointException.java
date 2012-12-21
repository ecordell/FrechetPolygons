package anja.geom.triangulation;


import anja.geom.Point2;


/**
 * Hilfsklasse innerhalb der <code>QueryStructure</code> fuer Trapezoide.
 * 
 * @version 0.1 08.07.2003
 * @author Sascha Ternes
 */

final class ExistingPointException
		extends Exception
{

	// *************************************************************************
	// Constructors
	// *************************************************************************

	/**
	 * Erzeugt eine <code>ExistingPointException</code> ohne Angabe des
	 * verursachenden Punkts.
	 */
	ExistingPointException()
	{

		super("Queried point exists in search structure");

	} // ExistingPointException


	/**
	 * Erzeugt eine <code>ExistingPointException</code> mit Referenz auf den
	 * verursachenden Punkt.
	 * 
	 * @param point
	 *            der verursachende Punkt
	 */
	ExistingPointException(
			Point2 point)
	{

		super("Queried point exists in search structure: " + point);

	} // ExistingPointException

} // ExistingPointException
