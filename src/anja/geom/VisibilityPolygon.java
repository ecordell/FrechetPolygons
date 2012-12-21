package anja.geom;


import java.awt.Graphics2D;

import anja.util.GraphicsContext;


/**
 * Diese Klasse benutzt visPolygon, um aehnlich der Klasse Kern auf einfache
 * Weise einen Algorithmus zur Berechnung des Sichtbarkeitspolygons zur
 * Verfuegung zu stellen.
 * 
 * @version 1.0 07.03.02
 * @author Sascha Ternes
 */

public class VisibilityPolygon
		extends visPolygon
{

	// *************************************************************************
	// Private variables
	// *************************************************************************

	// das Eingabepolygon:
	private Polygon2	_poly;
	// das Sichtbarkeitspolygon:
	private Polygon2	_vis_poly;
	// der Bezugspunkt des Sichtbarkeitspolygons:
	private Point2		_point;


	// *************************************************************************
	// Constructors
	// *************************************************************************

	/**
	 * Erzeugt eine neue Instanz eines Sichtbarkeitspolygons zum Eingabepolygon.
	 * Da kein Punkt festgelegt wird, ist das Sichtbarkeitspolygon vorerst noch
	 * <b><i>null</i></b>.
	 * 
	 * @param poly
	 *            das Eingabepolygon
	 */
	public VisibilityPolygon(
			Polygon2 poly)
	{

		// erzeuge neues visPolygon:
		super(poly);
		// initialisiere Sichtbarkeitspolygon:
		_point = null;
		_vis_poly = null;

		// Test, ob Eingabepolygon einfach:
		if (!poly.isSimple())
		{
			_poly = null;
		}
		else
		{ // if
			_poly = poly;
		} // else

	} // VisibilityPolygon


	/**
	 * Erzeugt eine neue Instanz eines Sichtbarkeitspolygons zum Eingabepolygon.
	 * Das Sichtbarkeitspolygon wird auf Basis des Eingabepunktes errechnet und
	 * kann anschliessend z.B. direkt gezeichnet werden.
	 * 
	 * @param poly
	 *            das Eingabepolygon
	 * @param point
	 *            der Bezugspunkt des Sichtbarkeitspolygons
	 */
	public VisibilityPolygon(
			Polygon2 poly,
			Point2 point)
	{

		// erzeuge Instanz:
		this(poly);
		// berechne konkretes Sichtbarkeitspolygon zu point:
		_point = new Point2(point);
		_vis_poly = vis(point);

	} // VisibilityPolygon


	// *************************************************************************
	// Public methods
	// *************************************************************************

	/**
	 * Gibt das Eingabepolygon zurueck. Wenn das Eingabepolygon nicht gueltig
	 * war, wird <b><i>null</i></b> zurueckgegeben.
	 * 
	 * @return das Eingabepolygon zum Sichtbarkeitspolygon
	 */
	public Polygon2 getPolygon()
	{

		return _poly;

	} // getPolygon


	/**
	 * Gibt das Sichtbarkeitspolygon auf Basis des gesetzten Bezugspunktes
	 * zurueck. Wenn bisher kein Bezugspunkt oder ein ungueltiger Bezugspunkt
	 * (d.h. ein Punkt ausserhalb des Eingabepolygons) festgelegt wurde, wird
	 * <b><i>null</i></b> zurueckgegeben.
	 * 
	 * @return das berechnete Sichtbarkeitspolygon
	 */
	public Polygon2 getVisPolygon()
	{

		return _vis_poly;

	} // getVisPolygon


	/**
	 * Setzt den Bezugspunkt neu und veranlasst eine Neuberechnung des
	 * Sichtbarkeitspolygons.
	 * 
	 * @param point
	 *            der Bezugspunkt des Sichtbarkeitspolygons
	 */
	public void setPoint(
			Point2 point)
	{

		_vis_poly = vis(point);

	} // setPoint


	/**
	 * Liefert den Bezugspunkt des Sichtbarkeitspolygons zurueck.
	 * 
	 * @return den Bezugspunkt des Sichtbarkeitspolygons
	 */
	public Point2 getPoint()
	{

		return _point;

	} // getPoint


	/**
	 * Zeichnet das Sichtbarkeitspolygon.
	 * 
	 * @param g2d
	 *            das Graphics2D-Objekt der Zeichenflaeche
	 * @param gc
	 *            das GraphicsContext-Objekt des Sichtbarkeitspolygons
	 */
	public void draw(
			Graphics2D g2d,
			GraphicsContext gc)
	{

		if (_vis_poly != null)
		{
			_vis_poly.draw(g2d, gc);
		} // if

	} // draw

	// #########################################################################

} // VisibilityPolygon
