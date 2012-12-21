/*
 * Created on Jan 14, 2005
 *
 * JGenericGraphEditor.java
 */
package anja.SwingFramework.graph;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import anja.geom.*;
import anja.graph.*;
import anja.util.GraphicsContext;

import anja.SwingFramework.*;
import static anja.SwingFramework.graph.GraphEvent.*;
import static anja.graph.Constants.*;



/**
 * @author Ibragim Kouliev
 * 
 *         TODO: Write docs
 */
public class JGraphEditor
		extends JAbstractEditor
		implements ChangeListener//,
//GraphConstants
{

	//*************************************************************************
	// 				   Public constants
	//*************************************************************************

	//*************************************************************************
	// 				  Protected constants
	//*************************************************************************

	//----------------------- UI action command identifiers -------------------

	protected static final int		CMD_VERTEX_NAME			= 100;
	protected static final int		CMD_COLORIZE_EDGES		= 101;
	protected static final int		CMD_DIRECTED_GRAPH		= 102;
	protected static final int		CMD_UNDIRECTED_GRAPH	= 103;

	/* some flags that signify what occured in mousePressed()
	 * These determine which further action(s) will be taken
	 * in mouseDragged() and/or mousePressed()
	 */

	protected static final int		HIT_NOTHING				= 1;
	protected static final int		HIT_VERTEX				= 2;
	protected static final int		HIT_EDGE				= 3;
	protected static final int		HIT_PROXY_ZONE			= 4;
	protected static final int		HIT_SELECTION			= 5;

	protected static final int		MOUSE_SENSITIVITY		= 3;	// pixels

	//*************************************************************************
	// 				   Class variables
	//*************************************************************************

	//*************************************************************************
	// 			      Protected instance variables
	//*************************************************************************

	// warning this is shadowed variable!
	protected JGraphScene			m_Scene;

	protected Vertex				m_activeVertex;
	protected Edge					m_activeEdge;
	protected Vertex				m_proxyVertex;

	protected Line2					m_phantomEdge;
	protected Rectangle2			m_selectorBox;

	// vertex selection manipulator
	protected JSelectionSet			m_SelectionSet;

	// this one is for undos	
	protected Point2				m_prevObjectPosition;

	protected int					m_iDefaultVertexRadius;

	// additional mouse attributes:	

	// mouse position in world coordinates at the time of mousePressed()
	protected Point2				m_initialWorldMousePosition;

	// previous mouse position in mouseDragged()
	protected Point2				m_prevWorldMousePosition;

	// previous mouse position in screen coordinates
	protected Point					m_prevMousePosition;

	//--------------------------- Action flags --------------------------------

	protected int					m_iHitFlag;

	//previous drag flag
	protected boolean				m_bMouseWasDragged;

	protected boolean				m_bEdgeDeletionEnabled;

	// graphic atrributes
	protected GraphicsContext		m_normalGC;					// for normal drawing
	protected GraphicsContext		m_selectionGC;					// for selection set drawing
	protected GraphicsContext		m_specialGC;					// for special items drawing

	//--------------------------- UI elements ---------------------------------

	protected JCheckBoxMenuItem		m_uiEdgeColorItem;
	protected JRadioButtonMenuItem	m_uiDirGraphItem;
	protected JRadioButtonMenuItem	m_uiUndirGraphItem;

	//protected JMenu	                m_uiGraphModeMenu;    
	protected JTabbedPane			m_uiPropertyPane;

	//----------------------- Vertex coordinate UI ----------------------------

	//container for the spinners
	protected JPanel				m_uiVertexCoordinateBox;

	// point coordinate fields/spinners
	protected JSpinner				m_uiXCoordSpinner;
	protected JSpinner				m_uiYCoordSpinner;

	// coordinate spinners' precision selector
	protected JComboBox				m_uiSpinnerPrecisionBox;

	//----------------------- Vertex properties UI ----------------------------

	protected JPanel				m_uiVertexPropertiesBox;

	protected JTextField			m_uiVertexNameField;
	protected JSpinner				m_uiVertexRadiusSpinner;

	//------------------------- Vertex color UI -------------------------------

	protected JPanel				m_uiVertexColorBox;
	protected JTinyColorChooser		m_uiVertexColorChooser;

	//------------------------- Edge color UI ---------------------------------

	protected JPanel				m_uiEdgeColorBox;
	protected JTinyColorChooser		m_uiEdgeColorChooser;

	//----------------------- Edge properties UI ------------------------------

	protected JPanel				m_uiEdgePropertiesBox;


	//*************************************************************************
	// 			     Private instance variables
	//*************************************************************************

	//*************************************************************************
	// 				    Constructors
	//*************************************************************************

	/**
	 * @param hub
	 */
	public JGraphEditor(
			JSystemHub hub)
	{
		super(hub);
		createUI();

		m_Scene = (JGraphScene) super._scene;

		//m_selectionSet = new Vector();
		m_selectorBox = new Rectangle2();
		m_SelectionSet = new JSelectionSet(hub);

		m_phantomEdge = new Line2();

		m_phantomEdge.source().setLocation(0, 0);
		m_phantomEdge.target().setLocation(0, 0);

		m_initialWorldMousePosition = new Point2();
		m_prevWorldMousePosition = new Point2();
		m_prevMousePosition = new Point();
		m_prevObjectPosition = new Point2();

		//VisualVertex.enablePixelSizeTracking(true);
		Vertex.enableProxyZones(true);

		//VisualVertex.enableAlternativeHilight(true);
		//VisualEdge.enableAlternativeHilighting(true);

		m_bEdgeDeletionEnabled = true;

		//m_SelectionSet.setEdgeDeleteEnabled(false);

		m_iDefaultVertexRadius = 10;

		// various drawing attributes
		m_normalGC = new GraphicsContext();
		m_normalGC.setForegroundColor(Color.black);
		m_normalGC.setFillColor(Color.black);
		m_normalGC.setLineWidth(0.0f);

		float dash[] = { 5.0f };
		m_selectionGC = new GraphicsContext(Color.blue, Color.white,
				Color.white, 1.0f, dash);

		m_specialGC = new GraphicsContext(Color.red, Color.white, Color.white,
				1.0f, dash);
		// test section    
		setDefaultVertexRadius(7);
	}


	//*************************************************************************
	// 			      Public instance methods
	//*************************************************************************

	/**
	 * Draws the visual aids and additional information.
	 * 
	 * @param g
	 *            Graphics2D renderer, supplied internally by
	 *            JSimpleDisplayPanel
	 */
	public void draw(
			Graphics2D g)
	{
		// track current pixel size
		m_selectionGC.setLineWidth((float) (1.0f / _pixelSize));
		m_specialGC.setLineWidth((float) (1.0f / _pixelSize));

		// dash pattern has to track pixel size, too...
		float dash[] = { (float) (5.0f / _pixelSize) };
		m_selectionGC.setDash(dash);
		m_specialGC.setDash(dash);

		// draw selection visuals

		if (m_bMouseWasDragged)
		{
			//draw selection rectangle	
			m_selectionGC.setForegroundColor(Color.blue);
			m_selectorBox.draw(g, m_selectionGC);
		}
		else
		{
			// draw selected area
			//m_selectionGC.setForegroundColor(Color.gray);
			//m_selectionBoundingBox.draw(g, m_selectionGC);
		}

		// draw phantom edge if any
		if (m_phantomEdge.isNull() == false)
			m_phantomEdge.draw(g, m_specialGC);

		// return rendering attributes to normal
		g.setColor(m_normalGC.getForegroundColor());
		g.setStroke(m_normalGC.getStroke());

		// draw vertex selector box
		m_SelectionSet.draw(g);

	}


	//*************************************************************************

	/**
	 * Sets the new default vertex radius for the scene.
	 * 
	 * @param radius
	 *            new default vertex radius
	 */
	public void setDefaultVertexRadius(
			int radius)
	{
		m_iDefaultVertexRadius = radius;
	}


	//*************************************************************************
	//                             Event handlers
	//*************************************************************************

	/**
     * 
     */
	public void stateChanged(
			ChangeEvent event)
	{
		Object source = event.getSource();

		if (source == m_uiVertexColorChooser)
		{
			Color c = m_uiVertexColorChooser.getColor();

			if (m_SelectionSet.isEmpty())
			{
				if (m_activeVertex != null)
				{
					// change color of selected vertex
					if (!c.equals(m_activeVertex.getColor()))
					{
						putUndoAction(new GraphUndo(m_Scene, this,
								VERTEX_COLOR_CHANGED, m_activeVertex,
								m_activeVertex.getColor()));

						// change vertex color	
						m_activeVertex.deselect();
						m_activeVertex.setColor(c);

						fireGraphEditorEvent(this, m_activeVertex,
								VERTEX_COLOR_CHANGED);
					}
				}
				else if (m_activeEdge != null)
				{

					putUndoAction(new GraphUndo(m_Scene, this,
							EDGE_COLOR_CHANGED, m_activeEdge,
							m_activeEdge.getColor()));

					// change edge color
					m_activeEdge.setColor(m_uiVertexColorChooser.getColor());

					fireGraphEditorEvent(this, m_activeEdge, EDGE_COLOR_CHANGED);
				}
			}
			else
			{
				// store undo
				if (m_SelectionSet.isInVertexMode())
				{
					/*storeUndo(m_Scene, this, m_SelectionSet,
					          VERTEX_SELECTION_COLOR_CHANGED);
					*/
					/*
					if(m_SelectionSet.isEdgeColoringEnabled())
					{
						storeUndo(m_Scene, this, m_SelectionSet,
								  EDGE_SELECTION_COLOR_CHANGED);
					}*/
				}
				else
				{
					/*storeUndo(m_Scene, this, m_SelectionSet,
					          EDGE_SELECTION_COLOR_CHANGED);*/
				}

				m_SelectionSet.setSelectionColor(c);

				if (m_SelectionSet.isInVertexMode())
				{
					fireGraphEditorEvent(this,
							m_SelectionSet.getSelectedVertices(), null,
							VERTEX_SELECTION_COLOR_CHANGED, 0xFFFF);

					/*
					if(m_SelectionSet.isEdgeColoringEnabled())
					{
						fireNewGraphEditorEvent(this, 
						 m_SelectionSet.getSelectedVertices(),
						 null,EDGE_SELECTION_COLOR_CHANGED, 0xFFFF);
						
					}*/
				}
				else
				{
					fireGraphEditorEvent(this,
							m_SelectionSet.getSelectedEdges(), null,
							EDGE_SELECTION_COLOR_CHANGED, 0xFFFF);
				}
			} // selection set empty test						
		}
		else if (source == m_uiVertexRadiusSpinner)
		{
			//FIXME: Improve this code!

			// change vertex radius
			if (m_activeVertex != null)
			{
				Integer r = (Integer) m_uiVertexRadiusSpinner.getValue();

				putUndoAction(new GraphUndo(m_Scene, this,
						VERTEX_RADIUS_CHANGED, m_activeVertex, new Integer(
								m_activeVertex.getRadius())));

				m_activeVertex.setRadius(r.intValue());
				fireGraphEditorEvent(this, m_activeVertex,
						VERTEX_RADIUS_CHANGED);
			}
		}

		/*
		else if(source == m_uiEdgeColorChooser)
		{
		    if(m_activeEdge != null)
		    {
		    	storeUndo(m_Scene, this, m_activeEdge,
		    	          m_activeEdge.getMidPoint(),
		    	          EDGE_COLOR_CHANGED);
		    	
		    	// change edge color
		    	m_activeEdge.
		    	 setBoundingColor(m_uiEdgeColorChooser.getColor());
		    	
		    	fireNewGraphEditorEvent(this, m_activeEdge, EDGE_COLOR_CHANGED);
		    }
								
		}*/

		// for now,
		m_Scene.setChangeFlag();

		// update display
		_hub.getDisplayPanel().repaint();
	}


	//*************************************************************************

	/**
     * 
     */
	public void actionPerformed(
			ActionEvent event)
	{

		String cmd = event.getActionCommand();
		int code = Integer.parseInt(cmd);

		switch (code)
		{
			case CMD_VERTEX_NAME:

				if (m_activeVertex != null)
				{

					putUndoAction(new GraphUndo(m_Scene, this,
							VERTEX_LABEL_CHANGED, m_activeVertex,
							m_activeVertex.getLabel()));

					m_activeVertex.setLabel(m_uiVertexNameField.getText());

					fireGraphEditorEvent(this, m_activeVertex,
							VERTEX_LABEL_CHANGED);
				}

				break;

			case CMD_COLORIZE_EDGES:

				m_SelectionSet.enableEdgeColoring(m_uiEdgeColorItem
						.isSelected());

				break;

			case CMD_DIRECTED_GRAPH:
				m_Scene.setDirectedGraphMode(true);
				break;

			case CMD_UNDIRECTED_GRAPH:
				m_Scene.setDirectedGraphMode(false);
				break;

			default:
				//send event to the superclass implementation
				super.actionPerformed(event);
				break;

		}
		// TODO: edge name field changer

		_hub.getDisplayPanel().repaint();
	}


	//*************************************************************************

	public void mouseClicked(
			MouseEvent event)
	{
		// Auto-generated method stub		
	}


	//*************************************************************************

	public void mouseEntered(
			MouseEvent event)
	{
		// Auto-generated method stub		
	}


	//*************************************************************************

	public void mouseExited(
			MouseEvent event)
	{
		// Auto-generated method stub		
	}


	//*************************************************************************

	/**
     * 
     */
	public void mouseMoved(
			MouseEvent event)
	{
		/* For now, this just scans for proximity to a
		 * anchor zone of any vertex and draws if one 
		 * is found.
		 */

		Point position = event.getPoint();
		Point2 world_pos = _coordSystem.screenToWorld(position);

		Vertex vertex = m_Scene.getHotVertex(world_pos);

		if (m_SelectionSet.pointInBox(world_pos))
		{
			// send event to vertex selector
			//VisualVertex.enableProxyZones(false);

			m_SelectionSet.mouseMoved(event);

			//VisualVertex.enableProxyZones(true);
		}
		else if (vertex != null)
		{
			vertex.highlightProxyZone();
		}
		else
		{
			_hub.getDisplayPanel().setCursor(Cursor.getDefaultCursor());
		}
	}


	//*************************************************************************

	/**
     * 
     */
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
		m_initialWorldMousePosition.setLocation(world_position);
		m_prevWorldMousePosition.setLocation(world_position);

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
				resetVertexEdgeRefs();

				Vertex vertex = m_Scene.getVertexAt(world_position);

				Edge edge = m_Scene.getEdgeAt(world_position);

				if (vertex != null)
				{
					//m_iHitFlag = HIT_VERTEX; // this was wrong!	
					/*
					 * if(v.isSelected() == true) <-- this was also
					 * wrong! It also was responsible for one of the
					 * vertices in a selection set suddenly 'roaming
					 * around' on it own.
					 */

					if (m_SelectionSet.contains(vertex))
					{
						/* deselect and remove from
						 * current selection set
						 */

						////v.deselect();
						m_SelectionSet.removeVertex(vertex);
					}
					else
					{
						/* select and add to current 
						 * selection set
						 */

						//v.select();						
						m_SelectionSet.addVertex(vertex);
					}

					// HACK: temporarily disabled
					//fireNewGraphEditorEvent(this, m_selectionSet,
					//			   VERTEX_SELECTION_CHANGED);

				} // if v! = null

				if (edge != null)
				{
					if (m_SelectionSet.contains(edge))
						m_SelectionSet.removeEdge(edge);
					else
						m_SelectionSet.addEdge(edge);
				}

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

				//if(m_selectionBoundingBox.
				//	contains(world_position))
				//{
				m_iHitFlag = HIT_SELECTION;
				//m_uiPropertyPane.setSelectedIndex(0);
				//}

			} // multiple selection mode

			else
			{
				/* Normal mode
				 * This section tests the cursor position
				 * against the contents of the scene and 
				 * sets the action flag.
				 */
				Vertex v = m_Scene.getVertexAt(world_position);
				Edge e = m_Scene.getEdgeAt(world_position);
				Vertex hot = m_Scene.getHotVertex(world_position);

				if (m_SelectionSet.pointInBox(world_position)
						|| (m_SelectionSet
								.pointInRotationMarker(world_position)))
				{

					m_iHitFlag = HIT_SELECTION;

					// pass event to vertex selector box
					m_SelectionSet.mousePressed(event);
				}
				else if (hot != null)
				{
					m_iHitFlag = HIT_PROXY_ZONE;

					// deselect previously active vertex / edge
					resetVertexEdgeRefs();
					m_proxyVertex = hot;
				}
				else if (v != null)
				{
					m_iHitFlag = HIT_VERTEX;

					// deselect previous vertex (vertices)
					if (m_SelectionSet.isEmpty() == false)
						m_SelectionSet.deselectAll();

					// deselect active vertex/edge
					resetVertexEdgeRefs();

					// select new vertex
					v.select();
					m_activeVertex = v;
					showVertexData(v);

					// expose vertex property page
					// m_uiPropertyPane.setSelectedIndex(0);
					fireGraphEditorEvent(this, m_activeVertex, VERTEX_SELECTED);

					/* Store previous vertex position for
					 * undoing vertex displacement operations */
					m_prevObjectPosition.setLocation(m_activeVertex
							.getPosition());

				}
				else if (e != null)
				{
					m_iHitFlag = HIT_EDGE;

					// deselect active vertex/edge
					resetVertexEdgeRefs();

					e.select();
					m_activeEdge = e;

					// expose edge properties
					//m_uiPropertyPane.setSelectedIndex(1);
					showEdgeData(m_activeEdge);

					fireGraphEditorEvent(this, m_activeEdge, EDGE_SELECTED);

					//store previous position of the edge
					m_prevObjectPosition
							.setLocation(m_activeEdge.getMidPoint());
				}
				else
				{
					m_iHitFlag = HIT_NOTHING;
				}

			} // normal mode

		} // left mouse button handler

		/*
		else if(m_iMouseButton == MouseEvent.BUTTON3)
		{
		    //HACK: No meaningful code here yet!
		    
		    if(m_SelectionSet.pointInBox(world_position))
		    {
		    	m_SelectionSet.mousePressed(event);
		    }
		} // right mouse button handler*/
	}


	//*************************************************************************

	/**
     * 
     */
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
			//TODO: Clean up this code
			if (false)
			{
				//return; // bail
			}
			else
			{
				switch (m_iHitFlag)
				{
					case HIT_NOTHING:

						// pull visual selection rectangle	
						m_selectorBox.setFromPoints(
								m_initialWorldMousePosition, world_position);

						break;

					case HIT_VERTEX:

						// move the selected vertex 			
						m_activeVertex.moveTo(world_position);

						//showVertexData(m_activeVertex);				
						fireGraphEditorEvent(this, m_activeVertex, VERTEX_MOVED);

						m_Scene.setChangeFlag();

						break;

					case HIT_EDGE:

						// move the active edge
						m_activeEdge.translateByDelta(m_prevWorldMousePosition,
								world_position);

						fireGraphEditorEvent(this, m_activeEdge, EDGE_MOVED);

						m_Scene.setChangeFlag();

						break;

					case HIT_PROXY_ZONE:

						// pull phantom edge				
						Point2D start = m_proxyVertex.getPosition();

						/* the source point is cloned because it
						 otherwise the reference would be shared by 
						 the actual start vertex and the phantom edge
						 - the vertex position data would be destroyed
						 during edge reset in mouseReleased() */

						//m_phantomEdge.setSource((Point2)start.clone());
						m_phantomEdge.setSource(start.getX(), start.getY());

						m_phantomEdge.setTarget(world_position);

						break;

					case HIT_SELECTION:

						// send event to vertex selection box
						m_SelectionSet.mouseDragged(event);

						if (m_SelectionSet.hasBeenModified())
						{
							if (m_SelectionSet.isInVertexMode())
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
						}

						break;
				} // switch
			} // else

		}

		/* remember that the mouse was really dragged 
		 * prior to releasing the button 
		 */
		m_bMouseWasDragged = true;

		// save previous mouse position
		m_prevWorldMousePosition.setLocation(world_position);
	}


	//*************************************************************************

	/**
     * 
     * 
     */
	public void mouseReleased(
			MouseEvent event)
	{
		Point2 world_position = new Point2();

		_coordSystem.screenToWorld(event.getPoint(), world_position, true);

		if (_mouseButton == MouseEvent.BUTTON1)
		{
			if (m_bMouseWasDragged)
			{
				switch (m_iHitFlag)
				{
					case HIT_NOTHING:

						// test
						resetVertexEdgeRefs();

						// modify vertex selection box bounds
						m_SelectionSet.setFromPoints(
								m_initialWorldMousePosition, world_position);

						// optionally expose the vertex props page
						if (!m_SelectionSet.isEmpty())
							m_uiPropertyPane.setSelectedIndex(0);

						break;

					case HIT_PROXY_ZONE:

						/* Check if we've actually landed on another
						 * vertex, if yes, create a new edge between
						 * them, otherwise do nothing.
						 */
						Vertex v2 = m_Scene.getVertexAt(world_position);

						if (v2 != null)
						{
							Edge edge = m_Scene.addEdge(m_proxyVertex, v2);

							//edge.setColor(Color.gray);

							// fire event / store undo
							fireGraphEditorEvent(this, edge, EDGE_ADDED);
							putUndoAction(new GraphUndo(m_Scene, this,
									EDGE_ADDED, edge));

							m_Scene.setChangeFlag();
						}

						/* reset phantom edge so that 
						 * it doesn't remain on screen
						 */
						m_phantomEdge.reset();
						break;

					case HIT_VERTEX:

						// store an undo for VERTEX_MOVED op
						putUndoAction(new GraphUndo(m_Scene, this,
								VERTEX_MOVED, m_activeVertex,
								m_prevObjectPosition));
						fireGraphEditorEvent(this, m_activeVertex,
								VERTEX_MOVE_STOP);
						break;

					case HIT_EDGE:

						/*
						// store an undo for EDGE_MOVED op
						storeUndo(m_Scene, this, m_activeEdge,
						          m_prevObjectPosition, EDGE_MOVED);*/
						break;

					case HIT_SELECTION:

						m_SelectionSet.mouseReleased(event);

						// only if the selection has been really modified
						if (m_SelectionSet.hasBeenModified())
						{
							if (m_SelectionSet.isInVertexMode())
							{
								// fire appropriate event
								fireGraphEditorEvent(this,
										m_SelectionSet.getSelectedVertices(),
										m_SelectionSet.getTransform(),
										VERTEX_SELECTION_MODIFIED,
										m_SelectionSet.getTransformType());
							}
							else
							{
								//fire appropriate event
								fireGraphEditorEvent(this,
										m_SelectionSet.getSelectedEdges(),
										m_SelectionSet.getTransform(),
										EDGE_SELECTION_MODIFIED,
										m_SelectionSet.getTransformType());
							}

							/*
							// store undo
							storeUndo(m_Scene, this, 
							          m_SelectionSet,
							          VERTEX_SELECTION_MODIFIED);*/
						}

						break;

					default:
						break;

				} // switch
			}
			else
			// mouse was NOT dragged
			{
				switch (m_iHitFlag)
				{
					case HIT_NOTHING:

						/* If selection set not empty, clear it.
						 * Otherwise, create new vertex,
						 * select it and 
						 * add it to the graph
						 */
						if (m_SelectionSet.isEmpty() == false)
						{
							m_SelectionSet.deselectAll();
							/*deselectVertices();			
							fireNewGraphEditorEvent(this, m_selectionSet,
							                     VERTEX_SELECTION_CHANGED);*/
						}
						else
						{

							// deselect previously active vertex/edge
							resetVertexEdgeRefs();

							Vertex new_vertex;

							//new_vertex.setLabel(
							// String.valueOf(new_vertex.getID()));

							if (m_activeEdge != null)
							{
								//TODO: Edge splitting
								return;
							}
							else
							{
								new_vertex = m_Scene.addVertex(world_position);
							}

							new_vertex.select();
							m_activeVertex = new_vertex;

							//dispatch 'vertex added' event		
							fireGraphEditorEvent(this, new_vertex, VERTEX_ADDED);

							// store undo						
							putUndoAction(new GraphUndo(m_Scene, this,
									VERTEX_ADDED, new_vertex));

							// temporary											
							showVertexData(new_vertex);
						}

						break;

					case HIT_SELECTION:
						m_SelectionSet.mouseReleased(event);
						break;

				} // switch

			} // else

		} // left mouse button handler

		// reset the drag flag	
		m_bMouseWasDragged = false;

		// reset selector box
		m_selectorBox.setRect(0, 0, 0, 0);
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

				if (m_SelectionSet.isEmpty())
				{
					// remove active vertex/edge
					if (m_activeVertex != null)
					{
						deleteActiveVertex();
					}
					else if ((m_activeEdge != null)
							&& (m_bEdgeDeletionEnabled == true))
					{
						deleteActiveEdge();
					}
				}
				else
				// selection set is not empty
				{
					deleteSelection();
				}

				break;

		} // switch					
	}


	//*************************************************************************

	/**
	 * 
	 * @param tx
	 */
	public void updateAffineTransform(
			AffineTransform tx,
			double pixelSize,
			Font invertedFont)
	{
		super.updateAffineTransform(tx, pixelSize, invertedFont);
		m_SelectionSet.setPixelSize(_pixelSize);
	}


	//----------------------- Event firing methods ----------------------------

	/**
	 * Fires vertex-type events.
	 * 
	 * @param editor
	 *            from which this event originated, normally this
	 * @param vertex
	 *            the associated vertex
	 * @param event
	 *            The event
	 */
	public void fireGraphEditorEvent(
			JGraphEditor editor,
			Vertex vertex,
			GraphEvent event)
	{
		GraphEditorEvent e = new GraphEditorEvent(editor, vertex, event);
		//fire the new event
		fireEditorActionEvent(e);
	}


	//*************************************************************************

	/**
	 * Fires edge-type events.
	 * 
	 * @param editor
	 *            The graph editor
	 * @param edge
	 *            The edge
	 * @param event
	 *            The event
	 */
	public void fireGraphEditorEvent(
			JGraphEditor editor,
			Edge edge,
			GraphEvent event)
	{
		GraphEditorEvent e = new GraphEditorEvent(editor, edge, event);
		//fire the new event
		fireEditorActionEvent(e);
	}


	//*************************************************************************

	/**
	 * Fires selection-type events
	 * @param editor
	 *            The graph editor
	 * @param selection
	 *            can contain vertices OR edges
	 * @param transform
	 *            associated transform or <code>null</code>
	 * @param event
	 *            can be VERTEX_SELECTION_* or EDGE_SELECTION_*
	 * @param xform_type
	 *            can be any of SELECTION_*
	 */
	public void fireGraphEditorEvent(
			JGraphEditor editor,
			Vector selection,
			AffineTransform transform,
			GraphEvent event,
			int xform_type)
	{
		GraphEditorEvent e = new GraphEditorEvent(editor, selection, transform,
				event, xform_type);
		//fire the new event
		fireEditorActionEvent(e);
	}


	//*************************************************************************

	/**
	 * Used to fire special graph editor events, i.e. ones that carry no
	 * information other than the event code.
	 * 
	 * @param editor
	 *            The graph editor
	 * @param event
	 *            The event
	 */
	public void fireGraphEditorEvent(
			JGraphEditor editor,
			GraphEvent event)
	{
		GraphEditorEvent e = new GraphEditorEvent(editor, event);
		fireEditorActionEvent(e);
	}


	//*************************************************************************

	public void fireContextEvent(
			JGraphEditor editor,
			Vertex vertex,
			String command)
	{
		/*
		GraphContextEvent event = null;
		
		event = new GraphContextEvent(editor, vertex, command);
		fireContextActionEvent(event);*/
	}


	//*************************************************************************

	public void fireContextEvent(
			JGraphEditor editor,
			Edge edge,
			String command)
	{
		/*
		GraphContextEvent event = null;
		
		event = new GraphContextEvent(editor, edge, command);
		fireContextActionEvent(event);*/
	}


	//*************************************************************************

	public void fireContextEvent(
			JGraphEditor editor,
			Vector selection,
			String command)
	{
		/*
		GraphContextEvent event = null;
		
		event = new GraphContextEvent(editor, selection, command);
		fireContextActionEvent(event);*/
	}


	//*************************************************************************
	// 			      Protected instance methods
	//*************************************************************************

	protected FileNameExtensionFilter getFileFilter()
	{
		return new FileNameExtensionFilter("Newgraph Scene Files", "ngraph");
	}


	//*************************************************************************

	protected void clear()
	{
		super.clear(); // base class stuff...
		fireGraphEditorEvent(this, GraphEvent.GRAPH_CLEARED);
	}


	//*************************************************************************

	protected void loadDocument()
	{
		super.loadDocument();

		// sync GUI to scene data
		if (m_Scene.isDirected() == true)
		{
			m_uiDirGraphItem.setSelected(true);
		}
		else
		{
			m_uiUndirGraphItem.setSelected(true);
		}

		// fire necessary events
		Vertex vertex;
		Edge edge;

		Iterator<Vertex> vertices = m_Scene.getVertices();
		while (vertices.hasNext())
		{
			vertex = vertices.next();
			fireGraphEditorEvent(this, vertex, VERTEX_ADDED);
		}

		Iterator<Edge> edges = m_Scene.getEdges();
		while (edges.hasNext())
		{
			edge = edges.next();
			fireGraphEditorEvent(this, edge, EDGE_ADDED);
		}
	}


	//*************************************************************************

	// stores vertex undos
	protected void storeUndo(
			JGraphScene scene,
			JGraphEditor editor,
			Vertex vertex,
			Point2 previousPosition,
			int undoType)
	{
		/*
		JVertexUndo undo = new JVertexUndo(scene,editor, vertex,
		                                   previousPosition,
		                                   undoType);    	
		putUndoAction(undo);	*/
	}


	//*************************************************************************

	// stores edge undos
	protected void storeUndo(
			JGraphScene scene,
			JGraphEditor editor,
			Edge edge,
			Point2 previousPosition,
			int undoType)
	{
		/*
		JEdgeUndo undo = new JEdgeUndo(scene,editor,edge,
		                               previousPosition,
		                               undoType);
		putUndoAction(undo);		*/
	}


	//*************************************************************************

	// store vertex selection undos
	protected void storeUndo(
			JGraphScene scene,
			JGraphEditor editor,
			JSelectionSet selection,
			int undoType)
	{
		/*
		JSelectionUndo undo 
		= new JSelectionUndo(scene, editor, selection, undoType);
		
		putUndoAction(undo);*/
	}


	//*************************************************************************

	/**
	 * Creates the editor-specific UI panel & other elements
	 * 
	 */
	protected void createUI()
	{
		// ---------------- Menu items --------------------
		m_uiEdgeColorItem = new JCheckBoxMenuItem(
				"Use vertex color for adj. edges");

		m_uiEdgeColorItem.setActionCommand(String.valueOf(CMD_COLORIZE_EDGES));
		m_uiEdgeColorItem.addActionListener(this);
		_uiEditorMenu.add(m_uiEdgeColorItem);

		_uiEditorMenu.addSeparator();
		_uiEditorMenu.add(new JLabel("   Graph mode"));

		ButtonGroup mode_group = new ButtonGroup();

		m_uiDirGraphItem = new JRadioButtonMenuItem("Directed");
		m_uiDirGraphItem.setActionCommand(String.valueOf(CMD_DIRECTED_GRAPH));
		m_uiDirGraphItem.addActionListener(this);

		m_uiUndirGraphItem = new JRadioButtonMenuItem("Undirected");
		m_uiUndirGraphItem.setActionCommand(String
				.valueOf(CMD_UNDIRECTED_GRAPH));
		m_uiUndirGraphItem.addActionListener(this);

		mode_group.add(m_uiDirGraphItem);
		mode_group.add(m_uiUndirGraphItem);

		_uiEditorMenu.add(m_uiDirGraphItem);
		_uiEditorMenu.add(m_uiUndirGraphItem);

		m_uiUndirGraphItem.setSelected(true);

		// ---------- Vertex/edge UI elements ----------------

		_uiEditorPanel = new JPanel();
		m_uiPropertyPane = new JTabbedPane(SwingConstants.TOP);

		// ---------------------- Vertex panel ----------------

		makeVertexInputBox();
		makeVertexPropertiesBox();
		makeVertexColorBox();

		JPanel vertex_panel = new JPanel();

		//vertex_panel.setLayout(new GridLayout(1,3));
		//vertex_panel.setLayout(new BorderLayout());
		vertex_panel.setLayout(new FlowLayout());

		// vertex coordinate box	
		vertex_panel.add(m_uiVertexCoordinateBox);

		// vertex color box
		vertex_panel.add(m_uiVertexColorBox);

		// vertex props box
		vertex_panel.add(m_uiVertexPropertiesBox);

		m_uiPropertyPane.addTab("Vertex/Edge props", vertex_panel);

		// ----------------- Edge panel -----------------

		/*
		makeEdgeColorBox();
		
		JPanel edge_panel = new JPanel();
		edge_panel.setLayout(new FlowLayout());
		
		edge_panel.add(m_uiEdgeColorBox);
		
		m_uiPropertyPane.addTab("Edge", edge_panel);*/

		// everything's done!
		_uiEditorPanel.add(m_uiPropertyPane, BorderLayout.CENTER);
	}


	//*************************************************************************
	// 			     Data modification methods
	//*************************************************************************

	protected void deleteActiveVertex()
	{
		// dispatch events for adjacent edges
		LinkedList edges = m_Scene.getAdjacentEdges(m_activeVertex);

		Iterator it = edges.iterator();
		Edge edge;

		while (it.hasNext())
		{
			edge = (Edge) it.next();
			fireGraphEditorEvent(this, edge, EDGE_REMOVED);
			putUndoAction(new GraphUndo(m_Scene, this, EDGE_REMOVED, edge));
		}

		//dispatch 'vertex removed' event					
		// store undo action					
		putUndoAction(new GraphUndo(m_Scene, this, VERTEX_REMOVED,
				m_activeVertex));

		m_Scene.removeVertex(m_activeVertex);
		fireGraphEditorEvent(this, m_activeVertex, VERTEX_REMOVED);
		m_activeVertex = null;
	}


	//*************************************************************************

	protected void deleteActiveEdge()
	{

		// dispatch 'edge removed' event					
		fireGraphEditorEvent(this, m_activeEdge, EDGE_REMOVED);

		putUndoAction(new GraphUndo(m_Scene, this, EDGE_REMOVED, m_activeEdge));

		m_Scene.removeEdge(m_activeEdge);
		m_activeEdge = null;
	}


	//*************************************************************************

	protected void deleteSelection()
	{
		// dispatch 'vertex selection removed' event
		if (m_SelectionSet.isInVertexMode())
		{
			fireGraphEditorEvent(this, m_SelectionSet.getSelectedVertices(),
					null, VERTEX_SELECTION_REMOVED, 0);
			Vector vertices = m_SelectionSet.getSelectedVertices();
			Vector edges = new Vector();
			for (int i = 0; i < vertices.size(); i++)
			{
				Vertex v = (Vertex) vertices.get(i);
				Iterator<Edge> ei = v.getEdgeIterator(CLOCKWISE);
				while (ei.hasNext())
				{
					Edge e = ei.next();
					if (!edges.contains(e))
						edges.add(e);
				}
			}
			putUndoAction(new GraphUndo(m_Scene, this,
					VERTEX_SELECTION_REMOVED, m_SelectionSet
							.getSelectedVertices().toArray(), edges.toArray()));
		}
		else
		{
			fireGraphEditorEvent(this, m_SelectionSet.getSelectedEdges(), null,
					EDGE_SELECTION_REMOVED, 0);
			putUndoAction(new GraphUndo(m_Scene, this, EDGE_SELECTION_REMOVED,
					m_SelectionSet.getSelectedEdges().toArray()));
		}
		//remove selected vertices / edges
		m_SelectionSet.deleteSelection();
	}


	//*************************************************************************

	/*
	 * Deselects and resets previously active vertex/edge/proxy
	 * vertex. Used to simplify the code in event handlers (i.e. in
	 * almost any editor action the very first thing that happens is
	 * that all previously active elements (except selection sets) are
	 * reset).
	 */
	protected void resetVertexEdgeRefs()
	{
		// deselect & reset previous vertices / edges
		if (m_activeVertex != null)
		{
			m_activeVertex.deselect();
			m_activeVertex = null;
		}

		if (m_activeEdge != null)
		{
			m_activeEdge.deselect();
			m_activeEdge = null;
		}

		if (m_proxyVertex != null)
		{
			m_proxyVertex.deselect();
			m_proxyVertex = null;
		}
	}


	//*************************************************************************

	protected void showVertexData(
			Vertex v)
	{
		Point2D position = v.getPosition();
		String name = v.getLabel();
		Color color = v.getColor();
		int radius = v.getRadius();

		m_uiXCoordSpinner.setValue(new Double(position.getX()));
		m_uiYCoordSpinner.setValue(new Double(position.getY()));

		m_uiVertexRadiusSpinner.setValue(new Integer(radius));
		m_uiVertexNameField.setText(name);

		m_uiVertexColorChooser.setColor(color);

	}


	//*************************************************************************

	protected void showEdgeData(
			Edge edge)
	{
		m_uiVertexColorChooser.setColor(edge.getColor());
	}


	//*************************************************************************
	// 			      Private instance methods
	//*************************************************************************

	/**
	 * This code has been factored out of createUI because it's the single most
	 * crazy layout manager job I've ever done. I didn't want to mix it with the
	 * rest of the UI glue.
	 * 
	 */
	private void makeVertexInputBox()
	{
		// initialize the point coordinate spinners
		SpinnerNumberModel coordModelX = new SpinnerNumberModel(0.0,
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.1);

		SpinnerNumberModel coordModelY = new SpinnerNumberModel(0.0,
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.1);

		m_uiXCoordSpinner = new JSpinner(coordModelX);
		m_uiYCoordSpinner = new JSpinner(coordModelY);

		m_uiXCoordSpinner
				.setToolTipText("Changes X coordinate of selected point");

		m_uiYCoordSpinner
				.setToolTipText("Changes Y coordinate of selected point");

		// the coordinate spinner precision selector
		m_uiSpinnerPrecisionBox = new JComboBox();

		/*
		 * A short, but important note on setting the alignment of
		 * individual components: if the components are inserted into
		 * a container that uses BoxLayout, it is important that all
		 * components have consistent alignment values, otherwise the
		 * results might look very weird, with no apparent reason. In
		 * this particular case, if the spinners were centered, the
		 * layout manager would try to always center the labels as
		 * well, irrespective of the labels' actual alignment values.
		 * 
		 */

		m_uiXCoordSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
		m_uiYCoordSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
		m_uiSpinnerPrecisionBox.setAlignmentX(Component.CENTER_ALIGNMENT);

		// (for now, go with these precision values)
		m_uiSpinnerPrecisionBox.addItem(new String("1.0"));
		m_uiSpinnerPrecisionBox.addItem(new String("0.1"));
		m_uiSpinnerPrecisionBox.addItem(new String("0.01"));
		m_uiSpinnerPrecisionBox.addItem(new String("0.001"));
		m_uiSpinnerPrecisionBox.setSelectedIndex(1);
		m_uiSpinnerPrecisionBox
				.setToolTipText("Selects the spin boxes' step value");

		m_uiSpinnerPrecisionBox.setEnabled(false);

		// set up a container for the spinners/combo box
		m_uiVertexCoordinateBox = new JPanel();
		m_uiVertexCoordinateBox.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Vertex coordinates"));

		// using gridbag layout
		m_uiVertexCoordinateBox.setLayout(new GridBagLayout());

		// x coord label and spinner
		GridBagConstraints cc = new GridBagConstraints();
		cc.gridx = 0;
		cc.gridy = 0;
		cc.gridwidth = 1;
		cc.gridheight = 1;
		cc.fill = GridBagConstraints.NONE;
		cc.anchor = GridBagConstraints.WEST;
		cc.weightx = 0.0;
		cc.weighty = 1.0;
		cc.insets = new Insets(2, 2, 5, 5); // padding of 5 pixels
		m_uiVertexCoordinateBox.add(new JLabel("X", SwingConstants.LEFT), cc);

		cc.weightx = 1.0;
		cc.gridx = 1;
		cc.gridy = 0;
		// cc.gridwidth = 2;
		cc.anchor = GridBagConstraints.CENTER;
		cc.fill = GridBagConstraints.HORIZONTAL;
		m_uiVertexCoordinateBox.add(m_uiXCoordSpinner, cc);

		// y coord label and spinner
		cc.gridx = 0;
		cc.gridy = 1;
		cc.gridwidth = 1;
		cc.weightx = 0.0;
		cc.fill = GridBagConstraints.NONE;
		cc.anchor = GridBagConstraints.WEST;
		m_uiVertexCoordinateBox.add(new JLabel("Y", SwingConstants.RIGHT), cc);

		cc.weightx = 1.0;
		cc.gridx = 1;
		cc.gridy = 1;
		cc.anchor = GridBagConstraints.CENTER;
		cc.fill = GridBagConstraints.HORIZONTAL;
		// cc.gridwidth = 2;
		m_uiVertexCoordinateBox.add(m_uiYCoordSpinner, cc);

		// "create new point" button
		cc.gridx = 0;
		cc.gridy = 2;
		cc.gridwidth = 2;
		cc.anchor = GridBagConstraints.CENTER;
		cc.fill = GridBagConstraints.NONE;
		JButton newButton = new JButton("Create new point");
		newButton.setEnabled(false);
		m_uiVertexCoordinateBox.add(newButton, cc);

		// separator for the spinner precision box
		cc.gridy = 3;
		// cc.gridwidth = 5;
		cc.fill = GridBagConstraints.HORIZONTAL;
		m_uiVertexCoordinateBox.add(new JSeparator(), cc);

		// spinner precision label and combo box

		// precision selector box
		cc.gridx = 0;
		cc.gridy = 4;
		cc.gridwidth = 1;
		cc.anchor = GridBagConstraints.CENTER;
		cc.fill = GridBagConstraints.HORIZONTAL;
		m_uiVertexCoordinateBox.add(m_uiSpinnerPrecisionBox, cc);

		// label
		cc.gridx = 1;
		cc.gridy = 4;
		cc.anchor = GridBagConstraints.CENTER;
		cc.fill = GridBagConstraints.NONE;
		cc.gridwidth = 1;

		m_uiVertexCoordinateBox.add(new JLabel("Spinbox precision",
				SwingConstants.LEFT), cc);
		m_uiXCoordSpinner.addChangeListener(this);
		m_uiYCoordSpinner.addChangeListener(this);

		/*
		 * get the selected precision String prec =
		 * (String)_uiSpinnerPrecisionBox.getSelectedItem(); double
		 * precision = Double.parseDouble(prec);
		 *  // modify the spinner step values SpinnerNumberModel mod =
		 * (SpinnerNumberModel)_uiXCoordSpinner.getModel();
		 * mod.setStepSize(new Double(precision));
		 * 
		 * mod = (SpinnerNumberModel)_uiYCoordSpinner.getModel();
		 * mod.setStepSize(new Double(precision));
		 */
		// done
	}


	//*************************************************************************

	private void makeVertexPropertiesBox()
	{
		m_uiVertexPropertiesBox = new JPanel();

		m_uiVertexPropertiesBox.setLayout(new GridLayout(4, 1));
		/*
		m_uiVertexPropertiesBox.
		 setLayout(new BoxLayout(m_uiVertexPropertiesBox,
		 					     BoxLayout.Y_AXIS));*/

		m_uiVertexPropertiesBox.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Atrributes"));

		m_uiVertexNameField = new JTextField("");
		m_uiVertexNameField.setPreferredSize(new Dimension(70, 20));
		m_uiVertexNameField.setActionCommand(String.valueOf(CMD_VERTEX_NAME));
		m_uiVertexNameField.addActionListener(this);

		SpinnerNumberModel model = new SpinnerNumberModel(5, 5,
				Integer.MAX_VALUE, 1);

		m_uiVertexRadiusSpinner = new JSpinner(model);
		m_uiVertexRadiusSpinner.setPreferredSize(new Dimension(70, 20));

		JLabel name = new JLabel("Name");
		m_uiVertexPropertiesBox.add(name);
		m_uiVertexPropertiesBox.add(m_uiVertexNameField);

		JLabel radius = new JLabel("Radius");

		m_uiVertexPropertiesBox.add(radius);
		m_uiVertexPropertiesBox.add(m_uiVertexRadiusSpinner);

		m_uiVertexRadiusSpinner.addChangeListener(this);

	}


	//*************************************************************************

	private void makeVertexColorBox()
	{
		//vertex color chooser and stuff
		m_uiVertexColorBox = new JPanel();

		m_uiVertexColorBox
				.setBorder(BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
						"Color"));

		m_uiVertexColorChooser = new JTinyColorChooser();

		m_uiVertexColorChooser.addChangeListener(this);

		m_uiVertexColorBox.add(m_uiVertexColorChooser, BorderLayout.CENTER);

	}


	//*************************************************************************

	private void makeEdgeColorBox()
	{
		//edge color chooser and stuff
		m_uiEdgeColorBox = new JPanel();

		m_uiEdgeColorBox
				.setBorder(BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
						"Color"));

		m_uiEdgeColorChooser = new JTinyColorChooser();

		m_uiEdgeColorChooser.addChangeListener(this);

		m_uiEdgeColorBox.add(m_uiEdgeColorChooser, BorderLayout.CENTER);

	}

	//*************************************************************************
}
