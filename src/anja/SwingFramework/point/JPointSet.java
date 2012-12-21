/*
 * File: JPointSet.java
 * Created on Aug 31, 2006 by ibr
 *
 * TODO Write documentation
 */
package anja.SwingFramework.point;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.*;

import anja.geom.Point2;
import anja.util.GraphicsContext;


//import org.jdom.*;

/**
 * A general-purpose unstructured set of 2-dimensional points (more technically,
 * Point2 objects). The data structure supports insertion and removal of points,
 * translations, and can draw itself given a graphics2D context.
 * 
 * @author Ibragim Kouliev
 * 
 *         <br>TODO Write documentation TODO XML I/O here or in the point scene
 *         class ?
 */

public class JPointSet
{

	//*************************************************************************
	//                             Public constants
	//*************************************************************************

	//*************************************************************************
	//                             Private constants
	//*************************************************************************

	// search proximity radius, in world coordinate units
	private static final double	_PROXY_RADIUS			= 3;
	private static final int	_DEFAULT_POINT_RADIUS	= 5;

	//*************************************************************************
	//                             Class variables
	//*************************************************************************

	//*************************************************************************
	//                        Public instance variables
	//*************************************************************************

	//*************************************************************************
	//                       Protected instance variables
	//*************************************************************************

	/*
	 * storage container for points, this can later be replaced by
	 * something more efficient, like a BSP tree or a Quadtree, as
	 * long as it doesn't break the semantics and the public API.
	 */
	protected Vector			_points;

	// radius of a visual representation of a point (only used for rendering!)
	protected int				_pointRadius;

	// used for proximity testing
	protected double			_pixelSize;


	//*************************************************************************
	//                        Private instance variables
	//*************************************************************************

	//*************************************************************************
	//                              Constructors
	//*************************************************************************

	/**
	 * Creates an empty point set and sets the default radius for drawing points
	 * to 5 pixels.
	 * 
	 */
	public JPointSet()
	{
		// initialization
		_points = new Vector();
		_pointRadius = _DEFAULT_POINT_RADIUS;
	}


	//*************************************************************************
	//                         Public instance methods
	//*************************************************************************

	//----------------------------- Getters / setters -------------------------

	/**
	 * Returns the current value of the point radius. <br> <b>Note: This radius
	 * value is used ONLY for drawing and locating points! It has no 'real'
	 * geometric meaning, of course.</b>
	 * 
	 * @return current point radius
	 */
	public int getPointRadius()
	{
		return _pointRadius;
	}


	//*************************************************************************

	/**
	 * Sets the value of the point drawing radius, in pixels.
	 * 
	 * @param radius
	 *            new radius value
	 */
	public void setPointRadius(
			int radius)
	{
		_pointRadius = radius;
	}


	//*************************************************************************

	/**
	 * Returns an iterator that sequentially accesses all points in the set.
	 * This can be used like this: <p> <table width = 100% bgcolor="lavender"
	 * cellpading = 10> <tr><td>
	 * 
	 * <pre> Iterator it = set.getAllPoints(); Point 2 point;
	 * 
	 * while(it.hasNext()) { point = (Point2)it.nextElement(); //... do
	 * something with the point } </pre> </td></tr> </table>
	 * 
	 * @return iterator
	 */
	public Iterator getAllPoints()
	{
		return _points.iterator();
	}


	//*************************************************************************

	/**
	 * Returns the number of points in the container.
	 * 
	 * 
	 */
	public int getNumPoints()
	{
		return _points.size();
	}


	//*************************************************************************

	//---------------------- Element insertion and removal --------------------

	/**
	 * Adds a point to the set. This method will issue a warning (and do
	 * nothing) if the set already contains the specified point.
	 * 
	 * @param point
	 *            a new point to add
	 * 
	 */
	public void addPoint(
			Point2 point)
	{
		if (_points.contains(point) == false)
		{
			_points.add(point);
		}
		else
		{
			System.out.println("The point" + point.toString()
					+ "is already in the set!");
		}
	}


	//*************************************************************************

	/**
	 * Removes a point from the set. This method will issue a warning (and do
	 * nothing) if the set doesn't contain the specified point.
	 * 
	 * @param point
	 *            the point to be removed.
	 */
	public void removePoint(
			Point2 point)
	{
		if (_points.contains(point))
		{
			_points.remove(point);
		}
		else
		{
			System.out.println("The point" + point.toString()
					+ "is not in the set!");
		}
	}


	//*************************************************************************

	/**
	 * Clears the entire set.
	 * 
	 */
	public void clear()
	{
		_points.clear();
	}


	//-------------------------- Data queries ---------------------------------

	/**
	 * Returns <code><b>true</b></code> if the set is empty, otherwise
	 * <code><b>false</b></code>.
	 * 
	 * @return true, if the set is empty, false else
	 */
	public boolean isEmpty()
	{
		return _points.isEmpty();
	}


	//*************************************************************************

	/**
	 * Checks whether the set contains the specified point.
	 * 
	 * @param point
	 *            A point
	 * 
	 * @return <code><b>true</b></code> if the set contains the specified point,
	 *         otherwise <code><b>false</b></code>.
	 */
	public boolean contains(
			Point2 point)
	{
		return _points.contains(point);
	}


	//*************************************************************************

	/**
	 * Retrieves the point at the specified (x,y) coordinates. If no such point
	 * can be found, this method returns a <code><b>null</b></code> reference
	 * instead.
	 * 
	 * @param x
	 *            x-coordinate of the point location
	 * @param y
	 *            y-coordinate of the point location.
	 * 
	 * @return The Point2 object associated with the coordinates or null
	 */
	public Point2 getPointAt(
			double x,
			double y)
	{
		Iterator it = _points.iterator();
		Point2 point;

		/* perform simple O(n) linear search for a point with given 
		 * coordinates
		 */
		while (it.hasNext())
		{
			point = (Point2) it.next();

			if (point.distance(x, y) < _pointRadius * _pixelSize)
				return point;
		}
		return null; // no such point found!
	}


	//*************************************************************************

	/**
	 * (NOT IMPLEMENTED)
	 * 
	 * TODO: getNearestPoint()
	 * 
	 * @param x
	 *            x-coordinate of the point location
	 * @param y
	 *            y-coordinate of the point location.
	 * 
	 * @return The nearest Point2 object to the coordinates
	 * 
	 */
	public Point2 getNearestPoint(
			double x,
			double y)
	{
		return null; //TODO: getNearestPoint()
	}


	//*************************************************************************

	//---------------------------- Display ------------------------------------

	/**
	 * Draws the points in the set using graphic attributes specified by the
	 * <code>gc</code> GraphicsContext object.
	 * 
	 * @param g2d
	 *            a java.awt.Graphics2D rendering object
	 * 
	 * @param gc
	 *            a GraphicsContext object
	 * 
	 * @param pixelSize
	 *            This can be used to transform the actual radii of the points
	 *            in order to keep their pixel radii constant, which will make
	 *            their perceived size independent of the zoom factor. Otherwise
	 *            just set to 1.0
	 * 
	 * 
	 */
	public void draw(
			Graphics2D g2d,
			GraphicsContext gc,
			double pixelSize)
	{

		_pixelSize = pixelSize;

		// save previous rendering attributes
		Stroke s = g2d.getStroke();
		Color c = g2d.getColor();

		//----------------------------------------------------------

		/*
		 * For now, the rendering code uses the legacy draw() method
		 * in Point2. In will probably be replaced later. It may also
		 * change structurally, depending on the data structure used
		 * to hold the points.
		 */

		Iterator it = _points.iterator();
		Point2 point;

		while (it.hasNext())
		{
			point = (Point2) it.next();

			//point.draw(g2d, gc);

			// new rendering routine 
			point.draw(g2d, gc, _pointRadius, _pixelSize, false);
		}

		//----------------------------------------------------------

		// restore rendering attributes
		g2d.setStroke(s);
		g2d.setColor(c);
	}


	//---------------------------- XML I/O ------------------------------------

	//*************************************************************************
	//                       Protected instance methods
	//*************************************************************************

	protected void finalize()
	{
		_points.clear();
		_points = null; // just helping out the GC
	}

	//*************************************************************************
	//                        Private instance methods
	//*************************************************************************

	//*************************************************************************  

}
