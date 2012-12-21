package anja.geom;


/**
 * Zweidimensionaler Sichtpunkt
 * 
 * @version 0.1 12.05.1997
 * @author Elmar Langetepe
 */

public class ViewPoint2
		extends Point2
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	// ************************************************************************
	// Variables
	// ************************************************************************

	/** Standpunkt */
	public Point2	robot;

	/** Laufrichtung */
	public float	direction;

	/** Blickrichtung */
	public float	viewDirection;

	/** Punkt in Blickrichtung */
	public Point2	viewDirectionPoint;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Ruft Konstruktor von Point2 auf, legt Richtung fest.
	 * 
	 * @param input_x
	 *            Position in X-Richtung
	 * @param input_y
	 *            Position in Y-Richtung
	 * @param dir
	 *            Die Richtung
	 * @param viewdir
	 *            Die Blickrichtung(?)
	 */
	public ViewPoint2(
			float input_x,
			float input_y,
			float dir,
			float viewdir)
	{
		super(input_x, input_y);
		direction = dir;
		viewDirection = viewdir;
		viewDirectionPoint = new Point2();
	}


	// ************************************************************************
	// Class methods
	// ************************************************************************

	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Setzt die Richtung nach dem Blickpunkt, noetig, wenn dieser geaendert
	 * wurde
	 */
	public void setDirection()
	{
		viewDirection = (float) (robot.angle(viewDirectionPoint));
	}//  setDirection


	// ************************************************************************

	/**
	 * Berechnet die Kompassnadel als Polygon mit Winkel und Hoehe des unteren
	 * Dreiecks, das obere Dreieck hat Hoehe = 3/2*height
	 * 
	 * @param angle
	 *            Der Winkel
	 * @param height
	 *            Die Höhe
	 * 
	 * @return Die Kompassnadel
	 */
	public Polygon2 pointerPolygon(
			float angle,
			float height)
	{
		Polygon2 poly = new Polygon2();
		float startdirection = direction + angle;
		float enddirection = direction - angle;

		Point2 p1 = new Point2(robot.x + height * Math.cos(startdirection),
				robot.y + height * Math.sin(startdirection));
		Point2 p2 = new Point2(robot.x + 5 / 2 * height * Math.cos(direction),
				robot.y + 5 / 2 * height * Math.sin(direction));
		Point2 p3 = new Point2(robot.x + height * Math.cos(enddirection),
				robot.y + height * Math.sin(enddirection));

		poly.addPoint(robot);
		poly.addPoint(p1);
		poly.addPoint(p2);
		poly.addPoint(p3);

		return poly;
	}//pointerPolygon
} // class ViewPoint2

