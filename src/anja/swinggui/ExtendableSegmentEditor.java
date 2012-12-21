package anja.swinggui;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import anja.geom.Point2;
import anja.geom.Polygon2;
import anja.geom.Segment2;
import anja.swinggui.polygon.Extendable;
import anja.swinggui.polygon.ExtendablePolygonEditor;


public class ExtendableSegmentEditor
		extends ExtendablePolygonEditor
{

	private final static int	_EDGE_SELECT_DISTANCE	= 5;
	private final static int	_POINT_SELECT_DISTANCE	= 6;

	private Point2				_selCoord;
	private Segment2			_selEdge;
	private Point2				_selVertex;
	private Polygon2			_selPoly;
	private boolean				_dragCycle;
	private int					count					= 0;
	private Point2				_begin;
	private Polygon2			recent;
	private boolean				_points					= false;
	private boolean				_popup					= false;
	private boolean				_crossing				= true;
	private Extendable			_extension;

	private JMenuItem			_itemEraseVertex		= new JMenuItem(
																"Erase Point");
	private JMenuItem			_itemEraseSegment		= new JMenuItem(
																"Erase Segment");
	private Polygon2			_trans;


	public ExtendableSegmentEditor(
			Extendable extension)
	{
		super(extension);
		_extension = extension;
		super.setNumClosedPolygons(0);
		//_extension.registerPolygonEditor(this);
		//Add listener for PopupMenu entries
		_itemEraseVertex.addActionListener(this);
		_itemEraseSegment.addActionListener(this);

	}


	/**
	 * Does nothing. All Segments are open polygons!
	 * 
	 * @param i
	 *            The number of closed polygons (does nothing)
	 * 
	 */
	@Override
	public void setNumClosedPolygons(
			int i)
	{}


	/**
	 * Set if points are allowed for the input
	 * 
	 * @param allow
	 *            if points are allowed
	 */
	public void setAllowPoints(
			boolean allow)
	{
		_points = allow;
	}


	/**
	 * Set if crossings are allowed for the input segments.
	 * @param allow
	 */
	public void setAllowCrossing(
			boolean allow)
	{
		_crossing = allow;
	}


	public void mousePressed(
			MouseEvent e)
	{
		_selCoord = transformScreenToWorld(e.getX(), e.getY()); //save current mouse location
		_dragCycle = updateMouseLocation(_selCoord);
		_popup = false;
		boolean continue_proc = _extension.processMousePressed(e, _selCoord);
		if (continue_proc)
		{

			if (_points && !(0 == count)) //points are allowed and the drawing of a segment is active
			{
				try
				{
					_dragCycle = !_selPoly.equals(recent); //the selected Polygon is the Polygon, that is currently drawn
				}
				catch (NullPointerException ex)
				{

				}
			}
			if (e.isMetaDown())
			{
				_popup = true;
				//delete the polygon that is currently drawn
				processPopupMenu(e.getX(), e.getY(), _selCoord, " ");
				count = 0;
				getPolygon2Scene().removePolygon(recent);
				refresh();
			}
		}
	}


	public void mouseReleased(
			MouseEvent e)
	{
		if (!_popup)
		{
			boolean continue_proc = _extension.processMouseReleased(e,
					_selCoord);

			if (continue_proc)
			{
				if (!(_dragCycle))//no segment is currently dragged.
				{
					if (null == _selPoly)
					{
						if ((count == 0)) //no other segment drawing process is active.
						{
							recent = new Polygon2(); //create new polygon
							recent.addPoint(_selCoord); //add the current mouse location
							recent.setOpen(); //segments are open polygons
							_begin = (Point2) _selCoord.clone();

						}
						else
						//the drawing process of a segment is active
						{
							recent.addPoint(transformScreenToWorld(e.getX(),
									e.getY())); //add the endpoint of the segment
							if (!_crossing
									&& !getPolygon2Scene().polygonIsValid(
											recent))
							{
								count = 1 - count;
								recent.removeLastPoint();
							}
							else
							{
								getPolygon2Scene().addPolygon(recent);
								recent = null;
							}

						}
						_selPoly = null;
						_selVertex = null; //no polygon is selected for dragging
						_selEdge = null; //during a drawing process
						count = 1 - count;
					}
					else if (_points && null != recent
							&& _selPoly.equals(recent))
					{
						getPolygon2Scene().addPolygon(recent);
						recent = null;
						_selPoly = null;
						count = 1 - count;
					}
				}
				else
				{
					getPolygon2Scene().remove(_selPoly);

					if (!_crossing)
					{
						if (getPolygon2Scene().polygonIsValid(_selPoly)
								&& validPointLocation(_selPoly.firstPoint())
								&& validPointLocation(_selPoly.lastPoint()))
							getPolygon2Scene().add(_selPoly);
						else
							getPolygon2Scene().add(_trans);
					}
					else
					{
						if (validPointLocation(_selPoly.firstPoint())
								&& validPointLocation(_selPoly.lastPoint()))
							getPolygon2Scene().add(_selPoly);
						else
							getPolygon2Scene().add(_trans);
					}

				}
			}
			refresh();
		}
	}


	public void mouseDragged(
			MouseEvent e)
	{
		Point2 _translation = transformScreenToWorld(e.getX(), e.getY()); //get current mouse location
		boolean continue_proc = _extension.processMouseDragged(e, _translation);
		if (continue_proc)
		{
			if (_dragCycle && 0 == count)
			{
				if (null == _selVertex) //no vertex selected
				{
					_translation.x -= _selCoord.x; //calculate translation
					_translation.y -= _selCoord.y;
					_selPoly.translate(_translation); //Apply translation to Polygon
					_selCoord.translate(_translation); //set new reference point.
				}
				else
				//a vertex was selected
				{
					_selVertex.moveTo(_translation); //move vertex
				}
			}
			else
			{
				updateMouseLocation(_translation); //used when the mouse is dragged but there is no point or segment to move
			}
		}
		refresh(); //refresh the scene
	}


	public void mouseMoved(
			MouseEvent e)
	{
		if (1 == count)
		{
			refresh();
			Point2 end = new Point2(e.getX(), e.getY());
			Point2 begin = transformWorldToScreen(_begin.getX(), _begin.getY());
			//drawXORLine(_begin, end);
			Graphics2D g2d = (Graphics2D) getCanvas().getGraphics();
			g2d.setColor(Color.white);
			g2d.setXORMode(Color.red);

			g2d.drawLine((int) begin.getX(), (int) begin.getY(),
					(int) end.getX(), (int) end.getY());

		}

	}


	protected void buildPopupMenu(
			JPopupMenu menu)
	{
		_extension.popupMenu(menu);
		if (_points && !(null == _selVertex))
			menu.add(_itemEraseVertex);
		if (!(null == _selPoly))
			menu.add(_itemEraseSegment);
	}


	public void actionPerformed(
			ActionEvent e)

	{
		if (e.getSource() == _itemEraseVertex) // Erase Point
			removePoint(_selPoly, _selVertex);

		if (e.getSource() == _itemEraseSegment) // Erase Segment
			removePolygon(_selPoly, true);
	}


	/**
	 * Checks the location of a point within the scene
	 * 
	 * @param point
	 *            the point to check
	 * @return if point lies on free space
	 */
	private boolean validPointLocation(
			Point2 point)
	{
		if (null == getPolygon2Scene().getNearestEdge(point,
				transformScreenToWorld(_POINT_SELECT_DISTANCE))
				&& null == getPolygon2Scene().getNearestVertex(point,
						transformScreenToWorld(_POINT_SELECT_DISTANCE)))
			return true;
		else
			return false;
	}


	private boolean updateMouseLocation(
			Point2 location)
	{

		if (!(null == recent)
				&& (recent.lastPoint().distance(location) < _POINT_SELECT_DISTANCE))
		{
			_selPoly = recent;
			return false;

		}

		if (null == (_selVertex = getPolygon2Scene().getNearestVertex(location,
				transformScreenToWorld(_POINT_SELECT_DISTANCE)))) //check if there is a vertex close to the mouse location
		{
			try
			//if not
			{
				_selEdge = getPolygon2Scene().getNearestEdge(location,
						transformScreenToWorld(_EDGE_SELECT_DISTANCE));//check if there is a segment close to the mouse location
				_selPoly = getPolygon2Scene().getPolygonWithEdge(_selEdge); //get the polygon, that the edge belongs to
				_trans = (Polygon2) _selPoly.clone();
				return true;
			}
			catch (NullPointerException ex) //No edge close to the mouse location
			{

			}

		}
		else
		{
			//there was a vertex close to the mouse location
			try
			{
				_selPoly = getPolygon2Scene().getPolygonWithVertex(_selVertex); //get the polygon that the vertex belongs to
				_trans = (Polygon2) _selPoly.clone();
				return true;
			}
			catch (NullPointerException ex)
			{

			}

		}
		_selPoly = null;
		return false;
	}
}
