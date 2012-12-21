package anja.geom;


import java.awt.Graphics2D;

import anja.util.GraphicsContext;


/**
 * Hilfsklasse fuer zweidimensionale Voronoi-Diagramme, die einen Bisektor
 * zwischen zwei Punkten im Voronoi-Diagramm beschreibt.<br> Ein
 * Voronoi-Bisektor ist ein zweidimensionales Linien-Objekt (entweder eine
 * Gerade, ein Strahl oder ein Segement), gegebenenfalls mit einem oder zwei
 * Endpunkten (bei Voronoi-Diagrammen sind dies stets Voronoi-Knoten), und
 * Referenzen auf die beiden Punkte, die den Bisektor bestimmen.
 * 
 * @version 0.5 17.04.2003
 * @author Sascha Ternes
 */

public class VoronoiBisector2
		implements java.io.Serializable
{

	//**************************************************************************
	// Public constants
	//**************************************************************************

	/**
	 * der Bisektor ist eine Gerade ohne Endpunkte
	 */
	public static final int	LINE_BISECTOR		= 1;

	/**
	 * der Bisektor ist ein Strahl mit einem Voronoi-Knoten als Startpunkt
	 */
	public static final int	RAY_BISECTOR		= 2;

	/**
	 * der Bisektor ist ein Segment und verbindet zwei Voronoi-Knoten
	 */
	public static final int	SEGMENT_BISECTOR	= 3;

	//**************************************************************************
	// Private variables
	//**************************************************************************

	// das repraesentierende Linienobjekt:
	private BasicLine2		_representation;

	// dessen Art:
	private int				_type;

	// die beiden erzeugenden Punkte:
	private Point2			_points[];


	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Konstruktor fuer einen Geraden-Bisektor, der durch zwei Punkte der
	 * Punktmenge des Voronoi-Diagramms definiert wird.
	 * 
	 * @param point1
	 *            die beiden Punkte,
	 * @param point2
	 *            die den Bisektor definieren
	 */
	public VoronoiBisector2(
			Point2 point1,
			Point2 point2)
	{

		// Berechnung des Bisektors:
		float dx = point2.x - point1.x; // x- und
		float dy = point2.y - point1.y; // y-Distanz zwischen den Punkten
		float c_x = point1.x + dx / 2; // Koordinaten
		float c_y = point1.y + dy / 2; // des Mittelpunkts
		// Erzeugung der repraesentierenden Gerade:
		Point2 p1 = new Point2(c_x - dy / 2, c_y + dx / 2);
		Point2 p2 = new Point2(c_x + dy / 2, c_y - dx / 2);
		_representation = new Line2(p1, p2);
		_type = LINE_BISECTOR;
		// Registrierung der erzeugenden Punkte:
		_registerPoints(point1, point2);

	} // VoronoiBisector2


	/**
	 * Konstruktor fuer einen Strahl-Bisektor, der durch zwei Punkte der
	 * Punktmenge des Voronoi-Diagramms definiert wird und einen Voronoi-Knoten
	 * als Startpunkt besitzt. Die Richtung des Strahls wird durch den Parameter
	 * <code>direction</code> gesteuert; der Strahl ist senkrecht zu dem Segment
	 * von <code>point1</code> nach <code>point2</code>, und basierend auf
	 * dieser Richtung wird der Strahl <i>links</i> oder <i>rechts</i> des
	 * Segments ausgerichtet.
	 * 
	 * @param start
	 *            der Startpunkt des Strahls
	 * @param orientation
	 *            die Orientierung des Richtungspunktes des Strahls bezueglich
	 *            des Segments von <code>point1</code> nach <code>point2</code>;
	 *            zulaessige Werte sind <code>Point2.ORIENTATION_LEFT</code> und
	 *            <code>Point2.ORIENTATION_RIGHT</code>
	 * @param point1
	 *            die beiden Punkte,
	 * @param point2
	 *            die den Bisektor definieren
	 * 
	 * @see Point2#ORIENTATION_LEFT
	 * @see Point2#ORIENTATION_RIGHT
	 */
	public VoronoiBisector2(
			VoronoiVertex2 start,
			int orientation,
			Point2 point1,
			Point2 point2)
	{

		// Berechnung des Bisektors:
		float dx = point1.x - point2.x; // x- und
		float dy = point1.y - point2.y; // y-Distanz zwischen den Punkten
		float c_x = point1.x + dx / 2; // Koordinaten
		float c_y = point1.y + dy / 2; // des Mittelpunkts
		// Ermittlung des korrekten Richtungspunktes:
		Point2 p = new Point2(c_x - dy, c_y + dx);
		if ((new Line2(point1, point2)).orientation(p) != orientation)
		{
			p = new Point2(c_x + dy, c_y - dx);
		} // if
		// Verschiebung des Richtungpunktes passend zum Startpunkt des Strahls:
		p.translate(start.x - c_x, start.y - c_y);
		// Erzeugung des repraesentierenden Strahls:
		_representation = new Ray2(start, p);
		_type = RAY_BISECTOR;
		// Registrierung der erzeugenden Punkte:
		_registerPoints(point1, point2);

	} // VoronoiBisector2


	/**
	 * Konstruktor fuer einen Segment-Bisektor, der durch zwei Punkte der
	 * Punktmenge des Voronoi-Diagramms definiert wird und zwei Voronoi-Knoten
	 * verbindet.
	 * 
	 * @param start
	 *            die beiden Endpunkte
	 * @param target
	 *            des Segments
	 * @param point1
	 *            die beiden Punkte,
	 * @param point2
	 *            die den Bisektor definieren
	 */
	public VoronoiBisector2(
			VoronoiVertex2 start,
			VoronoiVertex2 target,
			Point2 point1,
			Point2 point2)
	{

		// Erzeugung des repraesentierenden Segments:
		_representation = new Segment2(start, target);
		_type = SEGMENT_BISECTOR;
		// Registrierung der erzeugenden Punkte:
		_registerPoints(point1, point2);

	} // VoronoiBisector2


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/**
	 * Testet, ob der spezifizierte Punkt einer der definierenden Punkte ist und
	 * liefert das Ergebnis.
	 * 
	 * @param point
	 *            der zu testende Punkt
	 * 
	 * @return <code>true</code>, falls der Punkt einer der Bisektorpunkte ist
	 */
	public boolean contains(
			Point2 point)
	{

		if (point.equals(_points[0]) || point.equals(_points[1]))
		{
			return true;
		} // if
		return false;

	} // contains


	/**
	 * Zeichnet den Bisektor.
	 * 
	 * @param g2d
	 *            das <code>Graphics2D</code>-Objekt
	 * @param gc
	 *            das <code>GraphicsContext</code>-Objekt
	 */
	public void draw(
			Graphics2D g2d,
			GraphicsContext gc)
	{

		_representation.draw(g2d, gc);

	} // draw


	/**
	 * Liefert die beiden Punkte, die den Bisektor definieren.
	 * 
	 * @return die beiden Punkte in einem zweielementigen Array
	 */
	public Point2[] getPoints()
	{

		return _points;

	} // getPoints


	/**
	 * Liefert den oder die Voronoi-Knoten, wenn der Bisektor ein Strahl oder
	 * ein Segment ist.<br>Ist der Bisektor ein Segment, werden die beiden
	 * Voronoi-Knoten zurueckgeliefert, die das Segment begrenzen.<br>Ist der
	 * Bisektor ein Strahl, wird der Voronoi-Knoten, an dem der Bisektor
	 * beginnt, zurueckgeliefert. Ist der Bisektor eine Gerade und hat somit
	 * keine Voronoi-Knoten, an denen er endet, wird ein leeres Array
	 * zurueckgeliefert.
	 * 
	 * @return ein Array mit dem oder den Voronoi-Knoten, falls existent
	 */
	public VoronoiVertex2[] getVertices()
	{

		VoronoiVertex2 vertices[] = new VoronoiVertex2[2];
		if (!isLine())
		{
			vertices[0] = (VoronoiVertex2) _representation.source();
			if (isSegment())
			{
				vertices[1] = (VoronoiVertex2) _representation.target();
			} // if
		} // if
		return vertices;

	} // getVertices


	/**
	 * Liefert das Repraesentationsobjekt dieses Bisektors (je nach Art des
	 * Bisektors eine Gerade, einen Strahl oder ein Segment). Zur Bestimmung der
	 * Art dieses Bisektors koennen die dafuer vorgesehenen Methoden verwendet
	 * werden.
	 * 
	 * @return ein Objekt der Klasse
	 * 
	 * @see BasicLine2
	 * @see #getType()
	 * @see #isLine()
	 * @see #isRay()
	 * @see #isSegment()
	 */
	public BasicLine2 getRepresentation()
	{

		return _representation;

	} // getRepresentation


	/**
	 * Liefert die Klasse des Repraesentationsobjekts dieses Bisektors zur
	 * Laufzeit, das von der Methode #getRepresentation() geliefert wird.
	 * 
	 * @return die Klasse des Representationsobjekts
	 * 
	 * @see #getRepresentation
	 * @see Line2
	 * @see Ray2
	 * @see Segment2
	 */
	public Class getRepresentationClass()
	{

		return _representation.getClass();

	} // getRepresentationClass


	/**
	 * Liefert die Art dieses Bisektors. Diese bestimmt u.a. auch die
	 * Laufzeit-Klasse des Objektes, das von der Methode #getRepresentation()
	 * zurueckgeliefert wird. Die Art des Bisektors ist eine der Konstanten
	 * <code>LINE_BISECTOR</code>, <code>RAY_BISECTOR</code> oder
	 * <code>SEGMENT_BISECTOR</code>.
	 * 
	 * @return die Art des Bisektors
	 * 
	 * @see #getRepresentation
	 * @see #LINE_BISECTOR
	 * @see #RAY_BISECTOR
	 * @see #SEGMENT_BISECTOR
	 */
	public int getType()
	{

		return _type;

	} // getType


	/**
	 * Testet, ob der Bisektor eine Gerade ist.
	 * 
	 * @return <code>true</code>, wenn der Bisektor eine Gerade ist
	 */
	public boolean isLine()
	{

		return _type == LINE_BISECTOR;

	} // isLine


	/**
	 * Testet, ob der Bisektor ein Strahl ist.
	 * 
	 * @return <code>true</code>, wenn der Bisektor ein Strahl ist
	 */
	public boolean isRay()
	{

		return _type == RAY_BISECTOR;

	} // isRay


	/**
	 * Testet, ob der Bisektor ein Segment ist.
	 * 
	 * @return <code>true</code>, wenn der Bisektor ein Segment ist
	 */
	public boolean isSegment()
	{

		return _type == SEGMENT_BISECTOR;

	} // isSegment


	//**************************************************************************
	// Private methods
	//**************************************************************************

	/*
	* Hilfsmethode fuer die Konstruktoren. Speichert die Bisektor-Bezugspunkte.
	*/
	private void _registerPoints(
			Point2 point1,
			Point2 point2)
	{

		_points = new Point2[2];
		_points[0] = point1;
		_points[1] = point2;

	} // _registerPoints


	// added this thing for easier debugging

	/**
	 * Erzeugt eine texteulle Repräsentation
	 * 
	 * @return Die textuelle Repräsentation
	 */
	public String toString()
	{

		String s = "point 1: (" + _points[0].x + "," + _points[0].y + ")     "
				+ "point 2: (" + _points[1].x + "," + _points[1].y + ")\n";

		return s;
	}

} // VoronoiBisector2
