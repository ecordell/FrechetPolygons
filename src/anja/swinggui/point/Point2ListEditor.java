package anja.swinggui.point;


import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import javax.swing.event.PopupMenuEvent;

import anja.geom.Point2;


//import anja.swinggui.*;

/**
 * Ein Editor fuer ein Point2List-Punktlistenobjekt. Die Punktliste kann visuell
 * editiert werden, das heisst, es koennen Punkte hinzugefuegt und geloescht
 * werden.
 * 
 * @version 0.2 10.04.2003
 * @author Sascha Ternes, Andreas Lenerz
 */

public class Point2ListEditor
		extends Point2ListWorker
		implements Runnable
{

	//**************************************************************************
	// Private constants
	//**************************************************************************

	// Klickradius eines Punktes:
	private static int			_TOLERANCE	= 5;

	//**************************************************************************
	// Private variables
	//**************************************************************************

	// Mausposition in Weltkoordinaten:
	private Point2				_sel_mouse;

	// aktuell gezogener Punkt:
	private Point2				_selected_point;

	// Items fuer PopupMenu:
	private JMenuItem			_item_clear;
	private JCheckBoxMenuItem	_item_enable_algo;
	private int					_label		= 1;

	//true, wenn Editor im Animationsmodus
	boolean						_animating	= false;

	//Dauer eines Frames der Animation, Animationsgeschwindigkeit ist std. 30 FPS
	long						_speed		= 1000 / 30;


	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt und initialisiert einen neuen Editor fuer eine Punktliste.
	 */
	public Point2ListEditor()
	{

		super();

		// erzeuge Menu-Items:
		_item_clear = new JMenuItem("Clear points");
		_item_enable_algo = new JCheckBoxMenuItem("Run algorithm",
				_algorithm_enabled);
		_item_enable_algo.setEnabled(false);

		// Editor als ActionListener bei Menu-Items registrieren:
		_item_clear.addActionListener(this);
		_item_enable_algo.addActionListener(this);

	} // Point2ListEditor


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/*
	* [javadoc-Beschreibung wird aus WorldCoorScene kopiert]
	*/
	public void popupMenuWillBecomeVisible(
			PopupMenuEvent e)
	{

		_item_clear.setEnabled(!getPoint2List().empty());

	} // popupMenuWillBecomeVisible


	/*
	 * [javadoc-Beschreibung wird aus Point2ListWorker kopiert]
	 */
	public void setAlgorithm(
			Point2ListAlgorithm algorithm)
	{

		super.setAlgorithm(algorithm);
		_item_enable_algo.setEnabled(true);
		algorithm.registerEditor(this);

	} // setAlgorithm


	/*
	 * [javadoc-Beschreibung wird aus Point2ListWorker kopiert]
	 */
	public void enableAlgorithm(
			boolean enable)
	{

		super.enableAlgorithm(enable);
		_item_enable_algo.setState(enable);

	} // setAlgorithm


	/**
	 * Veranlasst ein Neuzeichnen aller Objekte in der Zeichenflaeche inklusive
	 * der des Algorithmus.
	 */
	public void repaint()
	{

		_refresh();

	} // repaint


	//**************************************************************************
	// Interface ActionListener
	//**************************************************************************

	/**
	 * Wird aufgerufen, wenn ein Menue-Item des Popup-Menues ausgewaehlt wurde.
	 * 
	 * @param e
	 *            das <code>ActionEvent</code>-Objekt
	 */
	public void actionPerformed(
			ActionEvent e)
	{

		if (e.getSource() == _item_clear)
		{
			clearPoint2List();
		}
		else if (e.getSource() == _item_enable_algo)
		{ // if
			enableAlgorithm(_item_enable_algo.getState());
		} // else

	} // actionPerformed


	//**************************************************************************
	// Interface MouseListener
	//**************************************************************************

	/**
	 * Wird aufgerufen, wenn ein Maus-Button in der Zeichenflaeche gedrueckt
	 * wurde.
	 * 
	 * @param e
	 *            das <code>MouseEvent</code>-Objekt
	 */
	public void mousePressed(
			MouseEvent e)
	{
		_animating = false;

		// berechne Mauscursor-Position in Weltkoordinaten:
		_sel_mouse = transformScreenToWorld(e.getX(), e.getY());

		// Algorithmus faengt Mausereignis ggf. ab:
		if (_algorithm_enabled)
		{
			if (_algorithm.processMouseEvent(e, _sel_mouse, getPoint2List()))
			{
				_refresh();
				return;
			} // if
		} // if

		// ermittle einen gewaehlten Punkt:
		_selected_point = _getClickedPoint();

		// Aktion bei linker Maustaste:
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			// setze neuen Punkt:
			if (_selected_point == null)
			{
				_selected_point = new Point2(_sel_mouse);
				_selected_point.setLabel("<" + _label++ + ">");
				getPoint2List().addPoint(_selected_point);
			} // if
		} // if

		// Aktion bei rechter Maustaste:
		else
		{
			// loesche gewaehlten Punkt:
			if (_selected_point != null)
			{
				getPoint2List().points().remove(_selected_point);
				_selected_point = null;
				// zeige Kontextmenue an:
			}
			else
			{ // if
				processPopupMenu(e.getX(), e.getY());
			} // else
		} // else

		_refresh();

	} // mousePressed


	/**
	 * Wird aufgerufen, wenn ein Maus-Button ueber der Zeichenflaeche
	 * losgelassen wurde.
	 * 
	 * @param e
	 *            das <code>MouseEvent</code>-Objekt
	 */
	public void mouseReleased(
			MouseEvent e)
	{

		// berechne Mauscursor-Position in Weltkoordinaten:
		_sel_mouse = transformScreenToWorld(e.getX(), e.getY());

		// Algorithmus faengt Mausereignis ggf. ab:
		if (_algorithm_enabled)
		{
			if (_algorithm.processMouseEvent(e, _sel_mouse, getPoint2List()))
			{
				_refresh();
				return;
			} // if
		} // if

		// gezogener Punkt wurde abgelegt:
		if (_selected_point != null)
		{
			_selected_point = null;
		} // if

	} // mouseReleased


	//**************************************************************************
	// Interface MouseMotionListener
	//**************************************************************************

	/**
	 * Wird aufgerufen, wenn ein Objekt mit der Maus ueber die Zeichenflaeche
	 * gezogen wird.
	 * 
	 * @param e
	 *            das <code>MouseEvent</code>-Objekt
	 */
	public void mouseDragged(
			MouseEvent e)
	{

		// berechne Mauscursor-Position in Weltkoordinaten:
		_sel_mouse = transformScreenToWorld(e.getX(), e.getY());

		// Algorithmus faengt Mausereignis ggf. ab:
		if (_algorithm_enabled)
		{
			if (_algorithm.processMouseEvent(e, _sel_mouse, getPoint2List()))
			{
				_refresh();
				return;
			} // if
		} // if

		// Punkt ueber die Zeichenflaeche bewegen:
		if (_selected_point != null)
		{
			Point2 new_point = new Point2(_sel_mouse);
			Point2 closest = getPoint2List().closestPoint(new_point);
			if (!new_point.equals(closest))
			{
				// alten Punkt entfernen:
				getPoint2List().points().remove(_selected_point);
				_selected_point = new_point;
				// neuen Punkt hinzufuegen:
				getPoint2List().addPoint(_selected_point);
				_refresh();
			} // if
		} // if

	} // mouseDragged


	//**************************************************************************
	// Animation
	//**************************************************************************

	/**
	 * Setzt die Frames per Second einer Animation
	 * @param fps
	 *            Frames per Second
	 */
	public void setFPS(
			long fps)
	{
		_speed = 1000 / fps;
	}


	/**
	 * Gibt an, ob eine Animation läuft
	 * 
	 * @return true, wenn eine Animation läuft
	 */
	public boolean isAnimating()
	{
		return _animating;
	}


	/**
	 * Versetzt den Editor in den Animationsmodus
	 */
	public void startAnimation()
	{
		Thread t = new Thread(this);
		t.start();
	}


	/**
	 * Bricht den Animationsmodus ab
	 */
	public void stopAnimation()
	{
		_animating = false;
	}


	/**
	 * Run-Methode zur Animation, zeichnet in regelmässigen Abständen einfach
	 * die Zeichenfläche neu
	 */
	public void run()
	{
		_animating = true;
		while (_animating)
		{
			long time = System.nanoTime();
			this.repaint();
			time = System.nanoTime() - time;
			try
			{
				Thread.sleep(_speed - time / 1000000);
			}
			catch (Exception e)
			{}
			;
		}
	}


	//**************************************************************************
	// Protected methods
	//**************************************************************************

	/**
	 * Baut das Kontextmenue auf.
	 * 
	 * @param menu
	 *            das Kontextmenue-Objekt
	 */
	protected void buildPopupMenu(
			JPopupMenu menu)
	{

		menu.add(_item_clear);
		menu.add(_item_enable_algo);
		if (_algorithm_enabled)
		{
			_algorithm.changeContextMenu(menu);
		} // if

	} // buildPopupMenu


	//**************************************************************************
	// Private methods
	//**************************************************************************

	/**
	* Sucht den Punkt der Liste, der angeklickt wurde. Wenn kein Punkt in der
	* Naehe des Klickpunktes liegt, wird null zurueckgegeben.
	*/
	private Point2 _getClickedPoint()
	{

		double tolerance = transformScreenToWorld((double) _TOLERANCE);
		return getPoint2List().closestPoint(_sel_mouse, tolerance);

	} // _getClickedPoint

} // Point2ListEditor
