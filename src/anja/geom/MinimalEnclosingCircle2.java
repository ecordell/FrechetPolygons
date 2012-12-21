package anja.geom;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import java.util.Enumeration;
import java.util.Iterator;

import anja.util.GraphicsContext;


/**
 * Ein Kleinster Umschliessender Kreis (<i>Minimal Enclosing Circle</i>,
 * <i>MEC</i>) einer Punktmenge. Neben dem Mittelpunkt des Kreises wird eine
 * Menge von Punkten verwaltet (im Folgenden <i>Kreispunkte</i> genannt), die
 * auf dem Kreis liegen und seinen Radius definieren.
 * 
 * @version 0.5 28.04.2003
 * @author Sascha Ternes
 */
public class MinimalEnclosingCircle2
		extends Circle2
{

	//**************************************************************************
	// Public constants
	//**************************************************************************

	/**
	 * Zeichenfarbe der Kreislinie
	 */
	public static final Color	COLOR_CIRCLE	= Color.RED;

	/**
	 * Zeichenfarbe des Kreismittelpunkts
	 */
	public static final Color	COLOR_CENTER	= Color.BLUE;

	/**
	 * Zeichenfarbe der Kreispunkte
	 */
	public static final Color	COLOR_POINTS	= Color.BLACK;

	/**
	 * Zeichenbreite der Kreislinie in Pixel
	 */
	public static final int		WIDTH_CIRCLE	= 1;

	/**
	 * Zeichenbreite des Kreismittelpunkts in Pixel
	 */
	public static final int		WIDTH_CENTER	= 8;

	/**
	 * Zeichengroesse der Kreispunkte in Pixel
	 */
	public static final int		WIDTH_POINTS	= 8;

	/**
	 * Pinselform zum Zeichnen der Kreislinie
	 */
	public static final int		CAP_CIRCLE		= BasicStroke.CAP_ROUND;

	/**
	 * Pinselform zum Zeichnen des Kreismittelpunkts
	 */
	public static final int		CAP_CENTER		= BasicStroke.CAP_SQUARE;

	/**
	 * Pinselform zum Zeichnen der Kreispunkte
	 */
	public static final int		CAP_POINTS		= BasicStroke.CAP_SQUARE;

	//**************************************************************************
	// Private variables
	//**************************************************************************

	// die Punktmenge:
	private Point2List			_point_set;

	// die Menge der Kreispunkte:
	private Point2				_points[];

	// das Aussehen des Kreises:
	private GraphicsContext		_gc_circle;
	// das Aussehen des Kreismittelpunkts:
	private GraphicsContext		_gc_center;
	// das Aussehen der Kreispunkte:
	private GraphicsContext		_gc_points;


	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt einen Kreis mit dem Mittelpunkt (0, 0) und dem Radius 0. Es wird
	 * eine einelementige Menge von Kreispunkten angelegt, die den Mittelpunkt
	 * enthaelt.
	 */
	public MinimalEnclosingCircle2()
	{

		super();
		_point_set = null;
		_points = new Point2[1];
		_points[0] = centre;
		_initGraphicsContexts();

	} // MinimalEnclosingCircle2


	/**
	 * Erzeugt den MEC des referenzierten Farthest Point Voronoi-Diagramms.
	 * 
	 * @param fpvd
	 *            das Farthest Point Voronoi-Diagramm, zu dem der MEC berechnet
	 *            werden soll
	 */
	public MinimalEnclosingCircle2(
			FPVoronoiDiagram2 fpvd)
	{

		_point_set = fpvd;
		_points = null;
		_computeFromFPVD(fpvd);
		_initGraphicsContexts();

	} // MinimalEnclosingCircle2


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/**
	 * Zeichnet den MEC mit den momentan eingestellten Zeichenparametern.
	 * 
	 * @param g2d
	 *            das <code>Graphics2D</code>-Objekt der Zeichenflaeche
	 */
	public void draw(
			Graphics2D g2d)
	{

		// Kreismittelpunkt zeichnen:
		if (centre != null)
		{
			centre.draw(g2d, _gc_center);
		} // if
		// Kreislinie zeichnen:
		super.draw(g2d, _gc_circle);
		// Kreispunkte zeichnen:
		for (int i = 0; i < _points.length; i++)
		{
			_points[i].draw(g2d, _gc_points);
		} // for

	} // draw


	/**
	 * Zeichnet den MEC. Der Kreis selbst wird durch Verwenden des uebergebenen
	 * <code>GraphicsContext</code>-Objekts gezeichnet, die Kreispunkte durch
	 * Verwenden der momentan eingestellten Parameter.
	 * 
	 * @param g
	 *            das <code>Graphics2D</code>-Objekt der Zeichenflaeche
	 * @param gc
	 *            das Aussehen des Kreises
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{

		GraphicsContext old_gc_circle = _gc_circle;
		_gc_circle = gc;
		draw(g);
		_gc_circle = old_gc_circle;

	} // draw


	/**
	 * Liefert die Punktmenge, die diesem MEC zugrunde liegt.
	 * 
	 * @return die Punktmenge zu diesem MEC
	 */
	public Point2List getPointList()
	{

		return _point_set;

	} // getPointList


	/**
	 * Liefert die Menge der Kreispunkte.
	 * 
	 * @return ein Array der Kreispunkte dieses MEC
	 */
	public Point2[] getCirclePoints()
	{

		return _points;

	} // getCirclePoints


	/**
	 * Liefert das aktuelle Aussehen der Kreislinie.
	 * 
	 * @return das aktuelle <code>GraphicsContext</code>-Objekt, das zum
	 *         Zeichnen der Kreislinie verwendet wird
	 */
	public GraphicsContext getCircleGraphicsContext()
	{

		return _gc_circle;

	} // getCircleGraphicsContext


	/**
	 * Liefert das aktuelle Aussehen der Kreismittelpunkts.
	 * 
	 * @return das aktuelle <code>GraphicsContext</code>-Objekt, das zum
	 *         Zeichnen des Kreismittelpunkts verwendet wird
	 */
	public GraphicsContext getCenterGraphicsContext()
	{

		return _gc_center;

	} // getCenterGraphicsContext


	/**
	 * Liefert das aktuelle Aussehen der Kreispunkte.
	 * 
	 * @return das aktuelle <code>GraphicsContext</code>-Objekt, das zum
	 *         Zeichnen der Kreispunkte verwendet wird
	 */
	public GraphicsContext getCirclePointsGraphicsContext()
	{

		return _gc_points;

	} // getCirclePointsGraphicsContext


	/**
	 * Setzt das Aussehen der Kreislinie.
	 * 
	 * @param gc
	 *            das <code>GraphicsContext</code>-Objekt, das zum Zeichnen der
	 *            Kreislinie verwendet wird
	 */
	public void setCircleGraphicsContext(
			GraphicsContext gc)
	{

		_gc_circle = gc;

	} // setCircleGraphicsContext


	/**
	 * Setzt das Aussehen des Kreismittelpunkts.
	 * 
	 * @param gc
	 *            das <code>GraphicsContext</code>-Objekt, das zum Zeichnen des
	 *            Kreismittelpunkts verwendet wird
	 */
	public void setCenterGraphicsContext(
			GraphicsContext gc)
	{

		_gc_center = gc;

	} // setCenterGraphicsContext


	/**
	 * Setzt das Aussehen der Kreispunkte.
	 * 
	 * @param gc
	 *            das <code>GraphicsContext</code>-Objekt, das zum Zeichnen der
	 *            Kreispunkte verwendet wird
	 */
	public void setCirclePointsGraphicsContext(
			GraphicsContext gc)
	{

		_gc_points = gc;

	} // setCirclePointsGraphicsContext


	//**************************************************************************
	// Private methods
	//**************************************************************************

	/**
	 * Berechnung des MEC eines FPVD.
	 * 
	 * @param fpvd
	 *            Voronoi-Diagramm
	 */
	private void _computeFromFPVD(
			FPVoronoiDiagram2 fpvd)
	{

		// Suche nach den optimalen Kreisparametern:
		centre = null;
		radius = Float.MAX_VALUE;
		Enumeration e = fpvd.vertices();
		while (e.hasMoreElements())
		{
			VoronoiVertex2 v = (VoronoiVertex2) e.nextElement();
			Iterator i = v.bisectors();
			VoronoiBisector2 bi = (VoronoiBisector2) i.next();
			Point2 point = bi.getPoints()[0];
			if (v.distance(point) < radius)
			{
				centre = v;
				radius = (float) v.distance(point);
			} // if
		} // while

		// Kreispunkte ermitteln:
		if (centre != null)
		{
			VoronoiVertex2 v = (VoronoiVertex2) centre;
			_points = new Point2[v.bisectorCount()];
			int curr = 0;
			// Lauf ueber alle Bisektoren des gefundenen Knotens:
			Iterator i = v.bisectors();
			while (i.hasNext())
			{
				Point2 p[] = ((VoronoiBisector2) i.next()).getPoints();
				// pruefen, ob naechster Punkt neu ist:
				boolean found = false;
				for (int j = 0; j < curr; j++)
				{
					if (_points[j].equals(p[0]))
					{
						found = true;
					} // if
				} // for
				// ggf. naechsten Punkt uebernehmen:
				if (!found)
				{
					_points[curr++] = p[0];
				} // if
				// pruefen, ob naechster Punkt neu ist:
				found = false;
				for (int j = 0; j < curr; j++)
				{
					if (_points[j].equals(p[1]))
					{
						found = true;
					} // if
				} // for
				// ggf. naechsten Punkt uebernehmen:
				if (!found)
				{
					_points[curr++] = p[1];
				} // if
			} // while
		} // if

		// Test, ob der Kreis noch verkleinert werden kann:
		Polygon2 poly = new Polygon2();
		poly.addPoint(_points[0]);
		poly.addPoint(_points[1]);
		poly.addPoint(_points[2]);
		if (poly.inside(centre))
			return;

		// Kreis hat nur zwei Kreispunkte:
		Point2 a = null;
		Point2 b = null;
		// Suche nach der maximalen Distanz zwischen zwei Punkten:
		double max = 0.0;
		for (int i = 0; i <= 2; i++)
		{
			for (int j = 0; j <= 2; j++)
			{
				if (i == j)
					continue;
				double d = _points[i].distance(_points[j]);
				if (d > max)
				{
					a = _points[i];
					b = _points[j];
					max = d;
				} // if
			} // for
		} // for
		centre = new Point2(a.x + (b.x - a.x) / 2, a.y + (b.y - a.y) / 2);
		radius = (float) (max / 2.0);
		_points = new Point2[2];
		_points[0] = a;
		_points[1] = b;

	} // _computeFromFPVD


	/**
	 * Initialisiert die Zeichenparameter mit den Standardwerten.
	 * 
	 */
	private void _initGraphicsContexts()
	{

		_gc_circle = new GraphicsContext();
		_gc_circle.setLineWidth(WIDTH_CIRCLE);
		_gc_circle.setForegroundColor(COLOR_CIRCLE);
		_gc_circle.setEndCap(CAP_CIRCLE);

		_gc_center = new GraphicsContext();
		_gc_center.setLineWidth(WIDTH_CENTER);
		_gc_center.setForegroundColor(COLOR_CENTER);
		_gc_center.setEndCap(CAP_CENTER);

		_gc_points = new GraphicsContext();
		_gc_points.setLineWidth(WIDTH_POINTS);
		_gc_points.setForegroundColor(COLOR_POINTS);
		_gc_points.setEndCap(CAP_POINTS);

	} // _initGraphicsContexts

} // MinimalEnclosingCircle2
