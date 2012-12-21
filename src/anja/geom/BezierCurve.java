package anja.geom;


import java.awt.geom.GeneralPath;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import anja.util.Drawable;
import anja.util.GraphicsContext;


/**
 * Klasse zum erzeugen von Bezier-Kurve. Die Klasse verfügt außerdem über eine
 * Methode zum Zeichnen solcher.
 */

public class BezierCurve
		implements Drawable, java.io.Serializable
{

	/**
	 * Public variables
	 */
	public Point2	startPt, aux1Pt, aux2Pt, endPt;


	/**
	 * Constructor with all necessary data
	 * 
	 * @param st
	 *            Point2 object
	 * @param a1
	 *            Point2 object
	 * @param a2
	 *            Point2 Object
	 * @param e
	 *            Point2 object
	 */
	public BezierCurve(
			Point2 st,
			Point2 a1,
			Point2 a2,
			Point2 e)
	{
		startPt = st;
		aux1Pt = a1;
		aux2Pt = a2;
		endPt = e;
	}


	/**
	 * Inherited from Drawable, draws the Bezier curve
	 * 
	 * @param g
	 *            The graphics object
	 * @param gc
	 *            The graphics context
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

		g.setColor(gc.getForegroundColor());

		path.moveTo(startPt.x, startPt.y);
		path.curveTo(aux1Pt.x, aux1Pt.y, aux2Pt.x, aux2Pt.y, endPt.x, endPt.y);
		g.draw(path);
	}


	/**
	 * Inherited from Drawable, checks for intersection with a rectangle
	 * 
	 * @param rect
	 *            The rectangle, the bezier curve could intersect
	 * 
	 * @return true, if there is an intersection, false else
	 */
	public boolean intersects(
			Rectangle2D rect)
	{
		Rectangle2D bounds = new Rectangle2D.Float(startPt.x, startPt.y, 0, 0);
		bounds.add(aux1Pt.x, aux1Pt.y);
		bounds.add(aux2Pt.x, aux2Pt.y);
		bounds.add(endPt.x, endPt.y);
		return bounds.intersects(rect);
	}
}
