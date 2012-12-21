/*
 * File: JPointScene.java
 * Created on Aug 31, 2006 by ibr
 *
 * TODO Write documentation
 */
package anja.SwingFramework.point;


import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.*;

import anja.geom.Point2;
import anja.util.GraphicsContext;

import anja.SwingFramework.*;

import org.jdom.*;


/**
 * JPointScene provides basic facilities for maintaining an editable set of
 * two-dimensional points. These can be added/removed, located, modified, and
 * stored in XML form. This class uses a JPointSet object as internal dat
 * structure, and is used in conjunction with JPointEditor to provide a basic
 * point editor within the SwingFramework system.
 * 
 * @author Ibragim Kouliev
 */
public class JPointScene
		extends JAbstractScene
{

	//*************************************************************************
	//                             Public constants
	//*************************************************************************

	//*************************************************************************
	//                             Private constants
	//*************************************************************************

	//*************************************************************************
	//                             Class variables
	//*************************************************************************

	//*************************************************************************
	//                        Public instance variables
	//*************************************************************************

	//*************************************************************************
	//                       Protected instance variables
	//*************************************************************************

	protected JPointSet			_pointSet;
	protected GraphicsContext	_pointsGC;


	//*************************************************************************
	//                        Private instance variables
	//*************************************************************************

	//*************************************************************************
	//                              Constructors
	//*************************************************************************

	/**
	 * Creates an empty point scene with default parameters.
	 * 
	 * @param hub
	 *            system hub reference
	 */
	public JPointScene(
			JSystemHub hub)
	{
		super(hub);

		// initialization
		_pointSet = new JPointSet();
		_pointsGC = new GraphicsContext();

		// set initial rendering attributes        
		_pointsGC.setOutlineColor(Color.blue);

		_pointsGC.setEndCap(BasicStroke.CAP_SQUARE);
		_pointsGC.setLineWidth(0.0f);
	}


	//*************************************************************************
	//                         Public instance methods
	//*************************************************************************

	//-------------------------- Getters / setters ----------------------------

	/**
	 * Returns the GraphicsContext object that represents the current rendering
	 * attributes.
	 * 
	 * @return The GraphicsContext of the object
	 */
	public GraphicsContext getPointsGC()
	{
		return _pointsGC;
	}


	//*************************************************************************

	/**
	 * Sets the rendering attributes for the points.
	 * 
	 * @param gc
	 *            new rendering attributes
	 */
	public void setPointsGC(
			GraphicsContext gc)
	{
		_pointsGC = gc;
	}


	//*************************************************************************

	/**
	 * Sets the new color for points, leaves other rendering attributes
	 * unchanged. The default point color is <b><font
	 * color=blue>blue</font></b>.
	 * 
	 * @param color
	 *            The new color
	 */
	public void setPointColor(
			Color color)
	{
		_pointsGC.setForegroundColor(color);
	}


	//*************************************************************************

	/**
	 * Returns the current value of point rendering radius, in pixels.
	 * 
	 * @return The radius of the point in pixels
	 */
	public int getPointRadius()
	{
		return _pointSet.getPointRadius();
	}


	//*************************************************************************

	/**
	 * Sets a new point rendering radius.
	 * 
	 * @param radius
	 *            The new radius value, in pixels.
	 */
	public void setPointRadius(
			int radius)
	{
		_pointSet.setPointRadius(radius);
	}


	//*************************************************************************

	/**
	 * Returns the number of points in the container.
	 * 
	 * @return The number of points
	 * 
	 */
	public int getNumPoints()
	{
		return _pointSet.getNumPoints();
	}


	//-------------------- Element insertion / removal ------------------------

	/**
	 * Adds a new point to the scene.
	 * 
	 * @see JPointSet#addPoint(Point2)
	 * 
	 * @param point
	 *            A new point
	 * 
	 */
	public void addPoint(
			Point2 point)
	{
		_pointSet.addPoint(point);
		_sceneWasChanged = true;
	}


	//*************************************************************************

	/**
	 * Removes a point from the scene.
	 * 
	 * @see JPointSet#removePoint(Point2)
	 * 
	 * @param point
	 *            a point to be removed.
	 * 
	 */
	public void removePoint(
			Point2 point)
	{
		_pointSet.removePoint(point);
		_sceneWasChanged = true;
	}


	//*************************************************************************

	//----------------------------- Queries -----------------------------------

	/**
	 * NOT IMPLEMENTED YET!
	 * 
	 * @return The bounding rectangle
	 */
	public Rectangle2D getBoundingRectangle()
	{
		// TODO getBoundingRectangle()
		return null;
	}


	//*************************************************************************

	/**
	 * Clears the entire scene
	 * 
	 */
	public void clear()
	{
		_pointSet.clear();
		//TODO: repaint?

		_sceneWasChanged = true;
	}


	//*************************************************************************

	/**
	 * Returns <code><b>true</b></code> if the scene is empty, otherwise
	 * <code><b>false</b></code>.
	 * 
	 * @return true, if the scene is empty, false else
	 */
	public boolean isEmpty()
	{
		return _pointSet.isEmpty();
	}


	//*************************************************************************

	/**
	 * Tests whether the scene contains the specified point.
	 * 
	 * @param point
	 *            The point to be checked
	 *            
	 * @return <code><b>true</b></code> if the scene contains the point.
	 */
	public boolean contains(
			Point2 point)
	{
		return _pointSet.contains(point);
	}


	//*************************************************************************

	/**
	 * Returns the point at the specified coordinates. If no such point can be
	 * found, returns a <code><b>null</b></code> reference.
	 * 
	 * @param x
	 *            x-coordinate of the desired location
	 * @param y
	 *            y-coordinate of the desired location
	 * 
	 * @return The Point2 object associated or null
	 * 
	 * @see JPointSet#getPointAt(double, double)
	 */
	public Point2 getPointAt(
			double x,
			double y)
	{
		return _pointSet.getPointAt(x, y);
	}


	//*************************************************************************

	/**
	 * Wrapper for getPointAt(double x, double y). Takes a Point2D argument
	 * instead of individual coordinate values.
	 * 
	 * @param position
	 *            The position
	 * 
	 * @return The Point2 object associated with the position or null
	 */
	public Point2 getPointAt(
			Point2D position)
	{
		return _pointSet.getPointAt(position.getX(), position.getY());
	}


	//*************************************************************************

	/**
	 * Returns an iterator that sequentially accesses all of the scene's points.
	 * See {@link JPointSet#getAllPoints()} for further explanation.
	 * 
	 * @return An iterator to a Vector
	 */
	public Iterator getAllPoints()
	{
		return _pointSet.getAllPoints();
	}


	//---------------------------- Display ------------------------------------

	/**
	 * Draws the scene using the supplied Graphics2D object.
	 * 
	 * @param g The Graphics object
	 */
	public void draw(
			Graphics2D g)
	{
		_pointSet.draw(g, _pointsGC, 1.0 / _pixelSize);

		// any additional drawing code goes here
	}


	//---------------------------- XML I/O ------------------------------------

	/**
	 * NOT IMPLEMENTED YET!
	 * 
	 * @param root The root element of the XML
	 */
	public void readFromXML(
			Element root)
	{
		//TODO: readFromXML()
	}


	//*************************************************************************

	/**
	 * NOT IMPLEMENTED YET!
	 * 
	 * @return A XML representation of this object
	 */
	public Element convertToXML()
	{
		return super.convertToXML();
		//TODO: convertToXML()
	}

	//*************************************************************************
	//                       Protected instance methods
	//*************************************************************************

	//*************************************************************************
	//                        Private instance methods
	//*************************************************************************

}
