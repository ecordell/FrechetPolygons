package anja.swinggui.point;


import anja.swinggui.*;
import anja.util.GraphicsContext;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.AffineTransform;

/*
import java_ersatz.java2d.BasicStroke;
import java_ersatz.java2d.Graphics2D;
import java_ersatz.java2d.NoninvertibleTransformException;
import java_ersatz.java2d.Rectangle2D;
import java_ersatz.java2d.Transform; */

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Enumeration;

import anja.geom.Point2;
import anja.geom.Point2List;
import anja.geom.Rectangle2;


//import anja.util.SimpleList;

/**
 * Point2ListWorker ist von WorldCoorScene abgeleitet und ist die Basisklasse
 * fuer Klassen, die eine Punktmenge der Klasse anja.geom.Point2List in einem
 * DisplayPanel darstellen.
 * 
 * @version 0.3 03.04.2003
 * @author Sascha Ternes, Andreas Lenerz
 */

public class Point2ListWorker
		extends WorldCoorScene
{

	//**************************************************************************
	// Private constants
	//**************************************************************************

	// Initialeigenschaften der gezeichneten Punkte:
	private static final int		_DEFAULT_POINT_SIZE		= 5;
	private static final Color		_DEFAULT_POINT_COLOR	= Color.BLACK;
	private static final int		_DEFAULT_POINT_CAP		= BasicStroke.CAP_SQUARE;	//ROUND;

	//**************************************************************************
	// Private variables
	//**************************************************************************

	// die Punktliste:
	private Point2List				_points;

	// Transformationsmatrix fuer Bild- in Weltkoordinaten zum Zeichnen:
	private AffineTransform			_matrix;

	// Zeicheneigenschaften der Punkte:
	private GraphicsContext			_gc_points;

	// Algorithmus auf der Punktliste:
	protected Point2ListAlgorithm	_algorithm;
	protected boolean				_algorithm_enabled;


	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt ein neues <code>Point2ListWorker</code>-Objekt und initialisiert
	 * einige interne Variablen.
	 */
	public Point2ListWorker()
	{

		_points = new Point2List();
		_matrix = null;
		_gc_points = new GraphicsContext();
		_gc_points.setLineWidth(_DEFAULT_POINT_SIZE);
		_gc_points.setForegroundColor(_DEFAULT_POINT_COLOR);
		_gc_points.setEndCap(_DEFAULT_POINT_CAP);
		_algorithm = null;
		_algorithm_enabled = true;

	} // Point2ListWorker


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/**
	 * Entfernt alle Punkte aus der Punktliste und aktualisiert die
	 * Zeichenfläche.
	 */
	public void clearPoint2List()
	{

		_points.clear();
		_refresh();

	} // clearPoint2List


	/**
	 * Liefert die Punktmenge als <code>Point2List</code>-Objekt.
	 * 
	 * @return die Punktmenge als Referenz
	 */
	public Point2List getPoint2List()
	{

		return _points;

	} // getPoint2List


	/**
	 * Setzt die Punktmenge.
	 * 
	 * @param points
	 *            die neue Punktmenge
	 */
	public void setPoint2List(
			Point2List points)
	{

		_points = points;
		_refresh();

	} // setPoint2List


	/**
	 * Setzt den Algorithmus, der auf der Punktliste arbeitet und das
	 * Zeichenfenster benutzen kann und aktualisiert die Zeichenfläche.
	 * 
	 * @param algorithm
	 *            der zu verwendende Algorithmus
	 */
	public void setAlgorithm(
			Point2ListAlgorithm algorithm)
	{

		_algorithm = algorithm;
		_algorithm_enabled = true;
		_refresh();

	} // setAlgorithm


	/**
	 * Aktiviert oder deaktiviert den aktuell gesetzten Algorithmus auf der
	 * Punktliste.
	 * 
	 * @param enable
	 *            <code>true</code> aktiviert den Algorithmus,
	 *            <code>false</code> deaktiviert ihm
	 */
	public void enableAlgorithm(
			boolean enable)
	{

		_algorithm_enabled = enable;
		_refresh();

	} // enableAlgorithm


	/**
	 * Gibt den GraphicsContext zurück, mit dem die Punkte gezeichnet werden,
	 * die Größe und Form der Punkte kann so geändert werden.
	 * 
	 * @return GraphicsContext-Objekt für die Punkte
	 */
	public GraphicsContext getPointGraphicsContext()
	{
		return _gc_points;
	}


	//**************************************************************************
	// Protected methods
	//**************************************************************************

	/**
	 * Ermitteln des die Szene umschliessenden Rechtecks im
	 * Weltkoordinaten-System.
	 * 
	 * @return das Rechteck, das die Szene umschliesst
	 */
	protected Rectangle2 getBoundingRectWorld()
	{
		Rectangle2D rect = _points.getBoundingRect();
		if (rect == null)
		{
			return null;
		} // if
		return new Rectangle2(rect);

	} // _getBoundingRectWorld


	/**
	 * Zeichnen der Szene.
	 * 
	 * @param g2d
	 *            das <code>Graphics2D</code>-Objekt der Zeichenflaeche
	 * @param g
	 *            das <code>Graphics</code>-Objekt der Zeichenflaeche
	 */
	protected void paint(
			Graphics2D g2d,
			Graphics g)
	{

		// Hintergrund zeichnen:
		_paintBackground(g2d, g);

		// ggf. Algorithmus ausfuehren:
		if (_algorithm_enabled)
		{
			_screenToWorld(); // Transformationsmatrix aktualisieren
			_algorithm.updateTransformMatrix(_matrix); // Matrixaenderung!
			_algorithm.compute(_points, g2d);
		} // if

		// Punkte zeichnen:
		_paintPointList(g2d, g);

	} // _paint


	/**
	 * Zeichnen des Hintergrundes.
	 * 
	 * @param g2d
	 *            das <code>Graphics2D</code>-Objekt der Zeichenflaeche
	 * @param g
	 *            das <code>Graphics</code>-Objekt der Zeichenflaeche
	 */
	protected void _paintBackground(
			Graphics2D g2d,
			Graphics g)
	{

		// Clipping-Rechteck in Bildkoordinaten ermitteln:
		Rectangle clip = g.getClipBounds();
		// Hintergrund fuellen:
		g.setColor(Color.white);
		g.fillRect(clip.x, clip.y, clip.width, clip.height);

	} // _paintBackground


	/**
	 * Zeichnen der Punktliste.
	 * 
	 * @param g2d
	 *            das <code>Graphics2D</code>-Objekt der Zeichenflaeche
	 * @param g
	 *            das <code>Graphics</code>-Objekt der Zeichenflaeche
	 */
	protected void _paintPointList(
			Graphics2D g2d,
			Graphics g)
	{

		// Lauf ueber alle Punkte:
		Enumeration e = _points.points().values();
		while (e.hasMoreElements())
		{
			Point2 p = (Point2) e.nextElement();
			//         System.out.println( p );
			p.draw(g2d, _gc_points);
		} // while

	} // _paintPointList


	/**
	 * Szene neu zeichnen und _action_listener informieren.
	 */
	protected void _refresh()
	{

		updateDisplayPanel();
		fireActionEvent();

	} // _refresh


	/**
	 * Szene neu zeichnen und _action_listener informieren.
	 * 
	 * @param x
	 *            Eck-
	 * @param y
	 *            Koordinaten,
	 * @param width
	 *            Breite und
	 * @param height
	 *            Hoehe des Bereiches, der neu zu zeichnen ist
	 */
	protected void _refresh(
			double x,
			double y,
			double width,
			double height)
	{

		updateDisplayPanel(x, y, width, height, 10);
		fireActionEvent();

	} // _refresh


	//**************************************************************************
	// Private methods
	//**************************************************************************

	/**
	 * Ermitteln der Transformationsmatrix zur Umwandlung von Bild- in
	 * Weltkoordinaten
	 */
	private void _screenToWorld()
	{

		try
		{
			_matrix = getWorldToScreen().createInverse();
		}
		catch (NoninvertibleTransformException e)
		{ // try
			e.printStackTrace();
		} // catch

	} // _screenToWorld

} // Point2ListWorker
