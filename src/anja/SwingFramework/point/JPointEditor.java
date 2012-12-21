/*
 * File: JPointEditor.java
 * Created on Aug 31, 2006 by ibr
 *
 * TODO Write documentation
 */

package anja.SwingFramework.point;


import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Point;
import java.awt.Font;

import java.awt.geom.*;
import java.awt.event.*;

import anja.SwingFramework.*;

import anja.geom.Point2;
import anja.geom.Rectangle2;
import anja.util.GraphicsContext;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.*;


/**
 * JPointEditor provides basic editing facilities for a set of 2-D points. It is
 * used in conjunction with JPointScene to provide a basic point set editor
 * within the SwingFramework system.
 * 
 * @author Ibragim Kouliev
 * 
 *         <br>TODO: Multiple point selection/transforms <br>TODO:XML load/save
 *         functions
 * 
 */
public class JPointEditor
		extends JAbstractEditor
{

	//*************************************************************************
	//                             Public constants
	//*************************************************************************

	//*************************************************************************
	//                             Private constants
	//*************************************************************************

	/* some flags that signify what occured in mousePressed()
	 * These determine which further action(s) will be taken
	 * in mouseDragged() and/or mousePressed()
	 */

	private static final int	HIT_NOTHING		= 1;
	private static final int	HIT_POINT		= 2;
	private static final int	HIT_SELECTION	= 3;

	//*************************************************************************
	//                             Class variables
	//*************************************************************************

	//*************************************************************************
	//                        Public instance variables
	//*************************************************************************

	//*************************************************************************
	//                       Protected instance variables
	//*************************************************************************

	//reference to scene is stored here for quick access

	// warning! this is a shadowed variable
	JPointScene					_scene;

	protected Point2			_activePoint;
	protected Rectangle2		_selectorBox;

	protected Point2			_hotPoint;

	// highlight rectangle for individual points
	protected Rectangle2		_highlightRect;
	protected String			_pointLabel;

	// point selection manipulator
	//protected JNewSelectionSet        _selectionSet;

	// this one is for undos    
	protected Point2			_prevObjectPosition;
	//protected int                     _defaultRadius;

	// additional mouse attributes:     

	// mouse position in world coordinates at the time of mousePressed()
	protected Point2			_initialWorldMousePosition;

	// previous mouse position in mouseDragged()
	protected Point2			_prevWorldMousePosition;

	// previous mouse position in screen coordinates
	protected Point				_prevMousePosition;

	//--------------------------- Action flags --------------------------------

	protected int				_hitFlag;

	//previous drag flag
	protected boolean			_mouseWasDragged;

	//------------------------- graphic atrributes ----------------------------

	protected GraphicsContext	_normalGC;					// for normal drawing
	protected GraphicsContext	_selectionGC;				// for selection set drawing
	protected GraphicsContext	_specialGC;				// for special items drawing


	//*************************************************************************
	//                        Private instance variables
	//*************************************************************************

	//*************************************************************************
	//                              Constructors
	//*************************************************************************

	/**
	 * Creates a new JPointEdtitor.
	 * 
	 * @param hub
	 *            system hub reference.
	 */
	public JPointEditor(
			JSystemHub hub)
	{
		super(hub);

		_scene = (JPointScene) _hub.getScene();
		_coordSystem = _hub.getCoordinateSystem();

		//------------- event variables and graphics stuff --------------------

		_selectorBox = new Rectangle2();
		//_SelectionSet                     = new JNewSelectionSet(hub);

		_initialWorldMousePosition = new Point2();
		_prevWorldMousePosition = new Point2();
		_prevMousePosition = new Point();
		_prevObjectPosition = new Point2();

		//m_iDefaultVertexRadius = 10;

		// various drawing attributes
		_normalGC = new GraphicsContext();
		_normalGC.setForegroundColor(Color.black);
		_normalGC.setFillColor(Color.black);
		_normalGC.setLineWidth(0.0f);

		float dash[] = { 5.0f };
		_selectionGC = new GraphicsContext(Color.blue, Color.white,
				Color.white, 1.0f, dash);

		_specialGC = new GraphicsContext(Color.red, Color.white, Color.white,
				0.0f, null);

		//---------------------------------------------------------------------

		_highlightRect = new Rectangle2();
		_highlightRect.setRect(0, 0, 0, 0);

		_pointLabel = ""; // no label

	}


	//*************************************************************************
	//                         Public instance methods
	//*************************************************************************

	public void mouseClicked(
			MouseEvent e)
	{}


	//*************************************************************************

	public void mouseEntered(
			MouseEvent e)
	{}


	//*************************************************************************

	public void mouseExited(
			MouseEvent e)
	{}


	//*************************************************************************

	public void mousePressed(
			MouseEvent event)
	{
		// store mouse parameters
		_mouseButton = event.getButton();
		_mouseModifiers = event.getModifiersEx();
		_mousePosition = event.getPoint();

		// convert mouse position to world coordinates 
		Point2 world_position = new Point2();
		_coordSystem.screenToWorld(_mousePosition, world_position, false);

		// store initial mouse coordinates
		_initialWorldMousePosition.setLocation(world_position);
		_prevWorldMousePosition.setLocation(world_position);

		// handle events for left mouse button
		if (_mouseButton == MouseEvent.BUTTON1)
		{
			if ((_mouseModifiers & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)
			{
				/* select / deselect multiple objects
				 * For now, this mode doesn't select multiple
				 * edges because of various issues related to
				 * edge removal / movement in a graph
				 */

				// deselect previous vertex/edge
				resetPointRefs();
				Point2 point = _scene.getPointAt(world_position);

				if (point != null)
				{
					//_HitFlag = HIT_VERTEX; // this was wrong!       
					/*
					 * if(v.isSelected() == true) <-- this was also
					 * wrong! It also was responsible for one of the
					 * vertices in a selection set suddenly 'roaming
					 * around' on it own.
					 */

					//TODO: selection set functionality

					//                    if(_SelectionSet.contains(vertex))
					//                    {
					//                        /* deselect and remove from
					//                         * current selection set
					//                         */
					//                        
					//                        ////v.deselect();
					//                        _SelectionSet.removeVertex(vertex);
					//                    }
					//                    else
					//                    {
					//                        /* select and add to current 
					//                         * selection set
					//                         */
					//                        
					//                        //v.select();                                           
					//                        _SelectionSet.addVertex(vertex);
					//                    }
					//                        
					//                    // HACK: temporarily disabled
					//                    //fireGraphEditorEvent(this, _selectionSet,
					//                    //                     VERTEX_SELECTION_CHANGED);

				} // if v! = null

				/* This was wrong too. <sigh>
				 * With the below if() the selection
				 * set is gonna be cleared completely 
				 * after the mouse is released under
				 * certain circumstances. Explanation -
				 * the selection bbox is adjusted after 
				 * a vertex is added/removed from the
				 * set. When a vertex is removed, the bbox
				 * becomes smaller and it may happen that
				 * the below test no longer passes, so 
				 * HIT_SELECTION flag isn't going to be
				 * set properly => incorrect jump in
				 * the mouseReleased() event later.
				 */

				//if(_selectionBoundingBox.
				//      contains(world_position))
				//{
				_hitFlag = HIT_SELECTION;
				//_uiPropertyPane.setSelectedIndex(0);
				//}

			} // multiple selection mode

			else
			{
				/* Normal mode
				 * This section tests the cursor position
				 * against the contents of the scene and 
				 * sets the action flag.
				 */

				Point2 p = _scene.getPointAt(world_position);

				// TODO: selection set functionality

				/*
				if(_SelectionSet.pointInBox(world_position) ||
				  (_SelectionSet.pointInRotationMarker(world_position)))
				{
				                                                
				    _HitFlag = HIT_SELECTION;
				    
				    // pass event to vertex selector box
				    _SelectionSet.mousePressed(event); 
				}*/

				if (p != null)
				{
					_hitFlag = HIT_POINT;

					// TODO: selection set functionality
					/*
					// deselect previous vertex (vertices)
					if(_SelectionSet.isEmpty() == false)
					 _SelectionSet.deselectAll();*/

					// deselect active vertex/edge
					resetPointRefs();

					// select new vertex
					_activePoint = p;

					// expose vertex property page
					// _uiPropertyPane.setSelectedIndex(0);
					//fireGraphEditorEvent(this, _activeVertex,
					//                    VERTEX_SELECTED);

					/* Store previous vertex position for
					 * undoing vertex displacement operations */
					_prevObjectPosition.setLocation(_activePoint);

				}
				else
				{
					_hitFlag = HIT_NOTHING;
				}

			} // normal mode

		} // left mouse button handler

		/*
		else if(m_iMouseButton == MouseEvent.BUTTON3)
		{
		    
		} // right mouse button handler*/

	}


	//*************************************************************************

	public void mouseReleased(
			MouseEvent event)
	{
		Point2 world_position = new Point2();

		_coordSystem.screenToWorld(event.getPoint(), world_position, true);

		if (_mouseButton == MouseEvent.BUTTON1)
		{
			if (_mouseWasDragged)
			{
				switch (_hitFlag)
				{
					case HIT_NOTHING:

						// test
						resetPointRefs();

						// TODO: selection set functionality
						/*
						// modify vertex selection box bounds
						m_SelectionSet.
						 setFromPoints(m_initialWorldMousePosition,
						               world_position);
						
						// optionally expose the vertex props page
						if(!m_SelectionSet.isEmpty())
						 m_uiPropertyPane.setSelectedIndex(0);*/

						break;

					case HIT_POINT:

						/*
						// store an undo for VERTEX_MOVED op
						storeUndo(m_Scene, this, m_activeVertex,
						          m_prevObjectPosition, VERTEX_MOVED);*/
						break;

					case HIT_SELECTION:

						//TODO: selection set functionality
						/*
						m_SelectionSet.mouseReleased(event);
						
						// only if the selection has been really modified
						if(m_SelectionSet.hasBeenModified())
						{
						    if(m_SelectionSet.isInVertexMode())
						    {   
						        // fire appropriate event
						        fireGraphEditorEvent(this, 
						         m_SelectionSet.getSelectedVertices(),
						         m_SelectionSet.getTransform(),
						         VERTEX_SELECTION_MODIFIED,
						         m_SelectionSet.getTransformType() 
						         );
						    }
						    else
						    {
						        //fire appropriate event
						        fireGraphEditorEvent(this, 
						         m_SelectionSet.getSelectedEdges(),
						         m_SelectionSet.getTransform(),
						         EDGE_SELECTION_MODIFIED,
						         m_SelectionSet.getTransformType());
						    }*/

						/*
						// store undo
						storeUndo(m_Scene, this, 
						          m_SelectionSet,
						          VERTEX_SELECTION_MODIFIED);
						}*/

						break;

					default:
						break;

				} // switch
			}
			else
			// mouse was NOT dragged
			{
				switch (_hitFlag)
				{
					case HIT_NOTHING:

						/* If selection set not empty, clear it.
						 * Otherwise, create new vertex,
						 * select it and 
						 * add it to the graph
						 */

						//TODO: selection set functionality
						if (/*m_SelectionSet.isEmpty() == false*/false)
						{
							//m_SelectionSet.deselectAll();

							/*deselectVertices();                   
							fireGraphEditorEvent(this, m_selectionSet,
							                     VERTEX_SELECTION_CHANGED);*/
						}
						else
						{

							// deselect previously active vertex/edge
							resetPointRefs();

							Point2 new_point = new Point2(world_position);

							_scene.addPoint(new_point);
							_activePoint = new_point;

							//--------- TEST SECTION ----------------------------

							firePointEditorEvent(new_point,
									PointEventConstants.POINT_ADDED);

							//---------------------------------------------------

							//dispatch 'vertex added' event         
							//fireGraphEditorEvent(this, new_vertex, VERTEX_ADDED);

							/*
							// store undo                                           
							storeUndo(m_Scene, this, new_vertex,
							          new_vertex.getPosition(),
							          VERTEX_ADDED);*/
						}

						break;

					case HIT_SELECTION:
						//TODO: selection set functionality
						//m_SelectionSet.mouseReleased(event);
						break;

				} // switch

			} // else

		} // left mouse button handler

		// reset the drag flag  
		_mouseWasDragged = false;

		// reset selector box
		_selectorBox.setRect(0, 0, 0, 0);

	}


	//*************************************************************************

	public void mouseDragged(
			MouseEvent event)
	{
		/* Now check the action flag and act 
		 * accordingly
		 */
		Point2 world_position = new Point2();

		_coordSystem.screenToWorld(event.getPoint(), world_position, true);

		if (_mouseButton == MouseEvent.BUTTON1)
		{

			switch (_hitFlag)
			{
				case HIT_NOTHING:

					// pull visual selection rectangle  
					_selectorBox.setFromPoints(_initialWorldMousePosition,
							world_position);

					break;

				case HIT_POINT:

					// move the selected vertex                         
					_activePoint.moveTo(world_position);

					//showVertexData(m_activeVertex);                           
					//fireGraphEditorEvent(this, m_activeVertex, VERTEX_MOVED);

					_scene.setChangeFlag();

					//--------- TEST SECTION ----------------------------

					firePointEditorEvent(_activePoint,
							PointEventConstants.POINT_MOVED);

					//---------------------------------------------------

					break;

				case HIT_SELECTION:

					//TODO: selection set functionality
					/*    
					// send event to vertex selection box
					m_SelectionSet.mouseDragged(event);
					
					if(m_SelectionSet.hasBeenModified())
					{
					    if(m_SelectionSet.isInVertexMode())
					    {
					        fireGraphEditorEvent(this,
					         m_SelectionSet.getSelectedVertices(),
					         m_SelectionSet.getTransform(),                                                      
					         VERTEX_SELECTION_MODIFIED,
					         m_SelectionSet.getTransformType());
					    }
					    else
					    {
					        fireGraphEditorEvent(this,
					         m_SelectionSet.getSelectedEdges(),
					         m_SelectionSet.getTransform(),
					         EDGE_SELECTION_MODIFIED,
					         m_SelectionSet.getTransformType());                                                        
					    }
					}*/

					break;
			} // switch
		} // if

		/* remember that the mouse was really dragged 
		 * prior to releasing the button 
		 */
		_mouseWasDragged = true;

		// save previous mouse position
		_prevWorldMousePosition.setLocation(world_position);

	}


	//*************************************************************************

	public void mouseMoved(
			MouseEvent event)
	{
		// stub
	}


	//*************************************************************************

	/**
     * 
     * 
     * 
     */
	public void draw(
			Graphics2D g2d)
	{

		// track current pixel size
		_selectionGC.setLineWidth((float) (1.0f / _pixelSize));
		//_specialGC.setLineWidth((float)(1.0f / _pixelSize));

		// dash pattern has to track pixel size, too...
		float dash[] = { (float) (5.0f / _pixelSize) };
		_selectionGC.setDash(dash);

		// draw selection visuals

		if (_mouseWasDragged)
		{
			//draw selection rectangle  
			_selectionGC.setForegroundColor(Color.blue);
			_selectorBox.draw(g2d, _selectionGC);
		}
		else
		{
			// draw selected area
		}

		// return rendering attributes to normal
		g2d.setColor(_normalGC.getForegroundColor());
		g2d.setStroke(_normalGC.getStroke());

		// draw vertex selector box
		//m_SelectionSet.draw(g);

		// highlight selected point
		if (_activePoint != null)
		{
			double radius = (_scene.getPointRadius() + 4) / _pixelSize;

			_highlightRect.setRect(_activePoint.x - radius, _activePoint.y
					- radius, 2 * radius, 2 * radius);

			_highlightRect.draw(g2d, _specialGC);

		}

	}


	//*************************************************************************

	/**
     * 
     */
	public void keyPressed(
			KeyEvent event)
	{
		// pass to superclass implementation first(for undos, etc.)
		super.keyPressed(event);

		// key mappings
		/*
		 * DELETE key removes the currently selected
		 * vertices / edges
		 */

		int code = event.getKeyCode();
		switch (code)
		{
			case KeyEvent.VK_DELETE:

				if (_activePoint != null)
				{
					deleteActivePoint();
				}

				//TODO: Selection set functionality    
				/*    
				if(m_SelectionSet.isEmpty())
				{
				    // remove active vertex/edge
				    if(m_activeVertex != null)
				    {
				        deleteActiveVertex();
				    }
				    else if((m_activeEdge != null) && 
				            (m_bEdgeDeletionEnabled == true))
				    {
				        deleteActiveEdge();
				    }
				}
				else // selection set is not empty
				{                           
				    deleteSelection();
				}*/

				break;

		} // switch                                     
	}


	//*************************************************************************
	//                       Protected instance methods
	//*************************************************************************

	protected void createUI()
	{
		// TODO createUI()

	}


	//*************************************************************************

	protected FileNameExtensionFilter getFileFilter()
	{
		return new FileNameExtensionFilter("Point Scene Files", ".point");
	}


	//*************************************************************************

	/*
	 * Deselects and resets previously active vertex/edge/proxy
	 * vertex. Used to simplify the code in event handlers (i.e. in
	 * almost any editor action the very first thing that happens is
	 * that all previously active elements (except selection sets) are
	 * reset).
	 */
	protected void resetPointRefs()
	{
		// deselect & reset previous vertices / edges
		if (_activePoint != null)
		{
			_activePoint = null;
		}
	}


	//*************************************************************************

	protected void deleteActivePoint()
	{
		//--------- TEST SECTION ----------------------------

		firePointEditorEvent(_activePoint, PointEventConstants.POINT_REMOVED);

		//---------------------------------------------------

		_scene.removePoint(_activePoint);
		_activePoint = null;

		//TODO: events, undos etc...
	}


	//*************************************************************************

	protected void firePointEditorEvent(
			Point2 point,
			PointEventConstants type)
	{
		fireEditorActionEvent(new JPointEditorEvent(this, point, type));
	}


	//*************************************************************************

	protected void clear()
	{
		super.clear();
		_activePoint = null;
		_highlightRect.setRect(0.0, 0.0, 0.0, 0.0);
	}

	//*************************************************************************
	//                        Private instance methods
	//*************************************************************************

}
