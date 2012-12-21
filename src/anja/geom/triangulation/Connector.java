package anja.geom.triangulation;


import anja.geom.Point2;


/**
 * Hilfsklasse zur Registrierung von diagonal geteilten Trapezoiden.
 * 
 * @version 0.8 22.07.2003
 * @author Sascha Ternes
 */

final class Connector
		implements java.io.Serializable
{

	// *************************************************************************
	// Package variables
	// *************************************************************************

	/**
	 * das linke Dreieck
	 */
	Triangle			left_triangle;

	/**
	 * das rechte Dreieck
	 */
	Triangle			right_triangle;

	// *************************************************************************
	// Private variables
	// *************************************************************************

	// das linke Trapezoid:
	private Trapezoid	_trapezoid1;
	// das rechte Trapezoid:
	private Trapezoid	_trapezoid2;

	// die verknuepfe Kante:
	private Point2[]	_edge;


	// *************************************************************************
	// Constructors
	// *************************************************************************

	/*
	* Verbotener Konstruktor.
	*/
	private Connector()
	{} // Connector


	/**
	 * Erzeugt einen Connector fuer die spezifizierten Trapezoide.
	 * 
	 * <br><b>Es ist auf die Reihenfolge der Trapezoide zu achten sowie darauf,
	 * dass die gemeinsame Kante fuer das linke Trapezoid im Gegenuhrzeigersinn
	 * orientiert ist (und damit fuer das rechte Trapezoid im
	 * Uhrzeigersinn)!</b>
	 * 
	 * @param trapez1
	 *            das linke Trapezoid
	 * @param trapez2
	 *            das rechte Trapezoid
	 * @param point1
	 *            der Startpunkt der gemeinsamen Kante
	 * @param point2
	 *            der Endpunkt der gemeinsamen Kante
	 */
	Connector(
			Trapezoid trapez1,
			Trapezoid trapez2,
			Point2 point1,
			Point2 point2)
	{

		_trapezoid1 = trapez1;
		_trapezoid2 = trapez2;
		left_triangle = null;
		right_triangle = null;
		_edge = new Point2[2];
		_edge[0] = point1;
		_edge[1] = point2;

	} // Connector


	/**
	 * Erzeugt einen Connector fuer die spezifizierten Dreiecke.
	 * 
	 * @param left
	 *            das linke Dreieck
	 * @param right
	 *            das rechte Dreieck
	 */
	Connector(
			Triangle left,
			Triangle right)
	{

		_trapezoid1 = null;
		_trapezoid2 = null;
		left_triangle = left;
		right_triangle = right;
		_edge = new Point2[2];
		_edge[0] = null;
		_edge[1] = null;

	} // Connector


	// *************************************************************************
	// Package methods
	// *************************************************************************

	/**
	 * Liefert die Verknuepfungskante als Punktarray.
	 * 
	 * @return die Verknuepfungskante; hierbei ist der Punkt mit Index 0 der
	 *         Startpunkt der Kante, die im Gegenuhrzeigersinn orientiert ist
	 */
	Point2[] getEdge()
	{

		return _edge;

	} // getEdge

} // Connector
