package anja.geom;


import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import anja.geom.Arc2Ext;
import anja.geom.Point2;
import anja.geom.Segment2;
import anja.util.Drawable;
import anja.util.GraphicsContext;
import anja.util.ListItem;
import anja.util.SimpleList;


/**
 * Two dimensional Polygon consisting of arcs (Arc2Ext) and segments
 * (Segment2).<br><br>
 * 
 * The Polygon will be set automaticly as closed if _firstPoint and _lastPoint
 * are equal.
 * 
 * @author Darius Geiss
 * 
 */
public class Polygon2Ext
		implements Drawable, java.io.Serializable
{

	private SimpleList	_objectList	= null;

	private Point2		_firstPoint	= null;
	private Point2		_lastPoint	= null;

	private boolean		_closed		= false;


	/**
	 * Generates an empty Polygon
	 */
	public Polygon2Ext()
	{
		_objectList = new SimpleList();
	}


	/**
	 * Adds an Arc2Ext to the objectlist, only if arc.source or arc.target is
	 * equal to get_lastPoint().
	 * 
	 * If the Polygon is empty this function will accept every Arc2Ext.
	 * Arc2Ext.source will become lastPoint and Arc2Ext.target will become
	 * firstPoint.
	 * 
	 * @param arc
	 *            The Arc2Ext object
	 * 
	 * @return true, if successfully added, false else
	 */
	public boolean addArc2Ext(
			Arc2Ext arc)
	{
		if (_objectList.empty())
		{
			_firstPoint = arc.target();
			_lastPoint = arc.source();
			_objectList.add(arc);
			return true;
		}
		else
		{
			if (arc.source().equals(_lastPoint))
			{
				_objectList.add(arc);
				_lastPoint = arc.target();
				if (_firstPoint.equals(_lastPoint))
				{
					_closed = true;
				}
				return true;
			}
			else if (arc.target().equals(_lastPoint))
			{
				_objectList.add(arc);
				_lastPoint = arc.source();
				if (_firstPoint.equals(_lastPoint))
				{
					_closed = true;
				}
				return true;
			}
			else
			{
				return false;
			}
		}
	}


	/**
	 * Adds an Segment2 to the objectlist, only if seg.source or seg.target is
	 * equal to get_lastPoint().
	 * 
	 * If the Polygon is empty this function will accept every Segment2.
	 * Segment2.source will become lastPoint and Segment2.target will become
	 * firstPoint.
	 * 
	 * @param seg
	 *            The segment
	 * 
	 * @return true, if successfully added, false else
	 */
	public boolean addSegment2(
			Segment2 seg)
	{
		if (_objectList.empty())
		{
			_firstPoint = seg.target();
			_lastPoint = seg.source();
			_objectList.add(seg);
			return true;
		}
		else
		{
			if (seg.source().equals(_lastPoint))
			{
				_objectList.add(seg);
				_lastPoint = seg.target();
				if (_firstPoint.equals(_lastPoint))
				{
					_closed = true;
				}
				return true;
			}
			else if (seg.target().equals(_lastPoint))
			{
				_objectList.add(seg);
				_lastPoint = seg.source();
				if (_firstPoint.equals(_lastPoint))
				{
					_closed = true;
				}
				return true;
			}
			else
			{
				return false;
			}
		}
	}


	/**
	 * Adds all Arc2Ext and Segment2 Objects of the list to the Polygon and
	 * returns all rejected Objects.
	 * 
	 * This function uses "addArc2Ext(Arc2Ext arc)" and
	 * "addSegment2(Segment2 seg)"
	 * 
	 * @param list
	 *            The (Simple)List object
	 * 
	 * @return rejected Objects as a list
	 * 
	 * @see #addArc2Ext(Arc2Ext)
	 * @see #addSegment2(Segment2)
	 */
	public SimpleList addList(
			SimpleList list)
	{
		SimpleList rejected = new SimpleList();
		boolean answer = false;
		if (list != null)
		{
			for (ListItem i = list.first(); i != null; i = i.next())
			{
				if (i.value() instanceof Arc2Ext)
				{
					answer = addArc2Ext((Arc2Ext) i.value());
				}
				else if (i.value() instanceof Segment2)
				{
					answer = addSegment2((Segment2) i.value());
				}
				if (answer == false)
				{
					if (list.first().equals(i))
					{
						return list;
					}
					else
					{
						list.cut(list.first(), i.prev());
					}
				}
			}
		}
		return rejected;
	}


	/**
	 * Returns a copy of the Polygon-Objects.
	 * 
	 * @return SimpleList of all objects
	 */
	public SimpleList getObjects()
	{
		return (SimpleList) _objectList.clone();
	}


	/**
	 * Erases the whole Polygon
	 */
	public void clearPolygon()
	{
		_objectList.clear();
	}


	/**
	 * Draws all objects of the polygon, inherited from
	 * {@link anja.util.Drawable}
	 * 
	 * @param graphics
	 *            The graphics object to draw in
	 * @param graphicsContext
	 *            The graphics context object
	 * 
	 * @see anja.util.Drawable
	 */
	public void draw(
			Graphics2D graphics,
			GraphicsContext graphicsContext)
	{
		if (_objectList != null)
		{
			for (ListItem i = _objectList.first(); i != null; i = i.next())
			{
				if (i.value() instanceof Drawable)
				{
					((Drawable) i.value()).draw(graphics, graphicsContext);
				}
			}
		}
	}


	/**
	 * Is the polygon closed
	 * 
	 * @return true if closed, false else
	 */
	public boolean isClosed()
	{
		return _closed;
	}


	/**
	 * Get the first point
	 * 
	 * @return The first point
	 */
	public Point2 get_firstPoint()
	{
		return _firstPoint;
	}


	/**
	 * Get the last point
	 * 
	 * @return The last point
	 */
	public Point2 get_lastpoint()
	{
		return _lastPoint;
	}


	/**
	 * NOT IMPLEMENTED
	 * 
	 * This method returns false
	 * 
	 * @param box
	 *            The rectangle to check for intersection with
	 * 
	 * @return true, if there is an intersection, false else
	 */
	public boolean intersects(
			Rectangle2D box)
	{
		// TODO Auto-generated method stub
		return false;
	}


	/**
	 * Returns a copy of all objects of this polygon
	 * 
	 * @return A list of all objects of the polygon
	 */
	public SimpleList get_objectList()
	{
		return _objectList;
	}

}
