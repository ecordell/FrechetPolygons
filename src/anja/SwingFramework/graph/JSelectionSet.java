/*
 * Created on Mar 9, 2005
 *
 * JSelectionSet.java
 * 
 */
package anja.SwingFramework.graph;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import java.awt.image.*;

import java.util.*;
import java.net.URL;

import anja.geom.*;
import anja.graph.*;
import anja.util.GraphicsContext;
import anja.util.MathConstants;

import anja.SwingFramework.*;


/**
 * @author ibr
 * 
 *         TODO write docs...
 * 
 *         FIXME Sometimes m_mousePosition isn't initialized properly! FIXME
 *         Incomplete icon repaint after mode change in mouseReleased()
 * 
 */
public class JSelectionSet
		implements GraphConstants, MathConstants
{

	//*************************************************************************
	// 				Public constants
	//*************************************************************************

	//*************************************************************************
	// 				Private constants
	//*************************************************************************

	private static final int		MARKER_SIZE				= 8;					// pixels
	private static final int		MARKER_OFFSET			= 2;					// pixels

	//------------------------------- modes -----------------------------------

	private static final int		TRANSLATE_SCALE_MODE	= 1;
	private static final int		ROTATE_SHEAR_MODE		= 2;

	//------------------------ transformation types ---------------------------

	private static final int		NOP						= 0xFFFFFFFF;
	private static final int		MOVE_ROTATION_CENTER	= 0xFF;

	private static final int		TRANSLATE				= 0xFA;
	private static final int		ROTATE					= 0xFB;

	/* The *_SCALE constants are numbered sequentially from 
	 * 0 to 7 because it allows the transform type flag to
	 * be set directly to value of the index of the loop
	 * that searches through the eight markers in mousePressed()
	 * 
	 */
	private static final int		NW_SCALE				= 0;
	private static final int		N_SCALE					= 1;
	private static final int		NE_SCALE				= 2;
	private static final int		E_SCALE					= 3;
	private static final int		SE_SCALE				= 4;
	private static final int		S_SCALE					= 5;
	private static final int		SW_SCALE				= 6;
	private static final int		W_SCALE					= 7;

	/* Similar explanation as above, except:
	 * due to the arrangement of rotation/shear icons aroundt
	 * the selection set, all shear markers are at odd positions,
	 * thus the transform type flag can also be set to the 
	 * loop index if the loop index is odd.
	 * (Otherwise it's set to ROTATE)
	 */
	private static final int		N_SHEAR					= 1;
	private static final int		E_SHEAR					= 3;
	private static final int		S_SHEAR					= 5;
	private static final int		W_SHEAR					= 7;

	//cursor / icon file names
	static private final String		ICON_NAMES[]			= {
			"../icons/rotation_icon_NW.gif", "../icons/shear_icon_h.gif",
			"../icons/rotation_icon_NE.gif", "../icons/shear_icon_v.gif",
			"../icons/rotation_icon_SE.gif", "../icons/shear_icon_h.gif",
			"../icons/rotation_icon_SW.gif", "../icons/shear_icon_v.gif" };

	static private final String		CURSOR_NAMES[]			= {
			"../icons/rotate_cursor_32.gif", "../icons/shear_cursor_h_32.gif",
			"../icons/rotate_cursor_32.gif", "../icons/shear_cursor_v_32.gif",
			"../icons/rotate_cursor_32.gif", "../icons/shear_cursor_h_32.gif",
			"../icons/rotate_cursor_32.gif", "../icons/shear_cursor_v_32.gif" };

	//scale cursor map, used to alter cursors in event handlers
	static private final Cursor[]	SCALE_CURSORS			= {
			Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR),
			Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR),
			Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR),
			Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR),
			Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR),
			Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR),
			Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR),
			Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR) };

	//*************************************************************************
	// 			       Static initialization
	//*************************************************************************

	//*************************************************************************
	// 			      Private instance variables
	//*************************************************************************

	private JSystemHub				m_Hub;
	private JGraphEditor			m_Editor;
	private JGraphScene				m_Scene;
	private CoordinateSystem		m_coordSystem;
	private JSimpleDisplayPanel		m_Display;

	// selection / bounding boxes
	private Rectangle2				m_BoundingBox;
	private Rectangle2				m_selectorBox;

	private Vector					m_Vertices;									// selected vertices
	private Vector					m_Edges;										// selected edges

	// this one is different from m_transformCenter in that it can be moved
	private Point2					m_rotationCenter;

	private Point2					m_transformCenter;

	private AffineTransform			m_T_Matrix;									// global to local mapping
	private AffineTransform			m_inv_T_Matrix;								// local to global mapping
	private AffineTransform			m_TransformMatrix;								// transformation matrix

	private AffineTransform			m_accumulatedTransform;

	//--------------------- Transformation mode markers -----------------------

	// mode markers
	private Rectangle2[]			m_Markers;

	// icon positions
	private Point2[]				m_IconPositions;

	// rotation center marker
	private Rectangle2				m_rotCenterMarker;
	private String					m_mirrorMarkerText;

	// mouse parameters
	private int						m_iMouseButton;
	private int						m_iMouseModifiers;
	private Point					m_mousePosition;

	//mouse position in world coordinates at the time of mousePressed()
	private Point2					m_initialWorldMousePosition;

	// previous mouse position in mouseDragged()
	private Point2					m_prevWorldMousePosition;

	// previous mouse position in screen coordinates
	private Point					m_prevMousePosition;

	//--------------------------- Action flags etc ----------------------------

	private int						m_iTransformFlag;
	private int						m_iModeFlag;
	private boolean					m_bUniformMode;

	private boolean					m_bVertexMode;

	private boolean					m_bColorizeEdges;
	private boolean					m_bEdgeDeletionEnabled;

	//previous drag flag
	private boolean					m_bMouseWasDragged;

	private boolean					m_bContentsChanged;

	// mouse in/out detection flags for scaling operations
	private boolean					m_bMouseEntered;
	private boolean					m_bMouseExited;

	//-------------------------------------------------------------------------

	// drawing stuff
	private float					m_fPixelSize;

	private GraphicsContext			m_normalGC;
	private GraphicsContext			m_widgetGC;									// for drawing visual aids

	// custom cursors
	private Cursor[]				m_customCursors;

	//rotation / shear mode icons
	private Image[]					m_RotationShearIcons;

	//testing section
	private Shape					_testShape;
	private Circle2					_testPoint;
	private Rectangle2				_testRect;


	//*************************************************************************
	// 				   Class variables
	//*************************************************************************

	//*************************************************************************
	// 				    Constructors
	//*************************************************************************

	/**
	 * Constructor
	 * 
	 * @param hub
	 *            The SystemHub to initialize the object with
	 */
	public JSelectionSet(
			JSystemHub hub)
	{
		m_Hub = hub;
		m_Editor = (JGraphEditor) hub.getEditor();
		m_Scene = (JGraphScene) hub.getScene();
		m_coordSystem = hub.getCoordinateSystem();
		//m_Display	  	= hub.getDisplayPanel();

		initCustomCursors();
		initIcons();

		m_BoundingBox = new Rectangle2(0, 0, 0, 0);
		m_selectorBox = new Rectangle2(0, 0, 0, 0);

		m_Vertices = new Vector();
		m_Edges = new Vector();

		m_rotationCenter = new Point2(0, 0);
		m_rotCenterMarker = new Rectangle2(0, 0, 0, 0);

		m_T_Matrix = new AffineTransform();
		m_inv_T_Matrix = new AffineTransform();
		m_TransformMatrix = new AffineTransform();
		m_accumulatedTransform = new AffineTransform();

		m_transformCenter = new Point2();

		// mouse variables
		m_initialWorldMousePosition = new Point2();
		m_prevWorldMousePosition = new Point2();
		m_prevMousePosition = new Point();

		m_bContentsChanged = false;
		m_bMouseWasDragged = false;
		m_iTransformFlag = NOP;
		m_iModeFlag = TRANSLATE_SCALE_MODE;
		m_bUniformMode = false;
		m_bVertexMode = true;

		m_bColorizeEdges = false;
		m_bEdgeDeletionEnabled = true;

		m_bMouseEntered = m_bMouseExited = false;

		// init graphics etc.
		m_mirrorMarkerText = "";

		m_Markers = new Rectangle2[8];
		for (int i = 0; i < 8; i++)
		{
			m_Markers[i] = new Rectangle2();
		}

		m_IconPositions = new Point2[8];
		for (int i = 0; i < 8; i++)
		{
			m_IconPositions[i] = new Point2();
		}

		m_normalGC = new GraphicsContext();
		m_normalGC.setFillColor(Color.white);
		m_normalGC.setLineWidth(0.0f);

		m_widgetGC = new GraphicsContext();
		m_widgetGC.setFillColor(Color.black);
		m_widgetGC.setFillStyle(1);

		m_widgetGC.setLineWidth(0.0f);

		// test stuff
		_testShape = new Rectangle2D.Double();
		_testPoint = new Circle2(0, 0, 15);
		_testRect = new Rectangle2();
	}


	//*************************************************************************
	// 			      Public instance methods
	//*************************************************************************

	/**
	 * Add a vertex
	 * 
	 * @param vertex
	 *            The vertex
	 */
	public void addVertex(
			Vertex vertex)
	{
		/*
		 * If the selection is empty, and the first added object is a
		 * vertex, lock the selection set into vertex mode. All
		 * subsequent edge additions will be ignored!
		 *  
		 */
		if (m_Vertices.isEmpty() && m_Edges.isEmpty())
		{
			m_bVertexMode = true;
		}

		if (m_bVertexMode)
		{
			m_Vertices.add(vertex);

			/*
			 * This fixes the 'jump bug'.
			 * 
			 * Jump bug explanation: a malfunction caused by inadvertent
			 * mouse 'twitching'. When adding / removing vertices to the
			 * box, in some cases the mousePressed() event may be followed
			 * by a mouseDragged() event (even if the mouse has only moved
			 * by one pixel), which will jerk the box into a different
			 * position, because the mouseDragged() event calls
			 * translateSelectionByDelta() if the m_iTransformFlag is set
			 * to TRANSLATE. [The jerk occurs because of invalid previous
			 * mouse position values...]. Hence the transform flag is set
			 * explicitly to NOP here, which essentially prevents the
			 * mouseDragged() events from moving the box.
			 *  
			 */
			m_iTransformFlag = NOP;
			m_bContentsChanged = true;

			update();
			updateRotationCenter();
		}
	}


	//*************************************************************************

	/**
	 * Remove a vertex
	 * 
	 * @param vertex
	 *            The vertex
	 * 
	 */
	public void removeVertex(
			Vertex vertex)
	{
		if (m_bVertexMode)
		{
			m_Vertices.remove(vertex);
			m_iTransformFlag = NOP;
			m_bContentsChanged = true;

			update();
			updateRotationCenter();
		}
	}


	//*************************************************************************

	/**
	 * Add an edge
	 * 
	 * @param edge
	 *            The edge
	 * 
	 */
	public void addEdge(
			Edge edge)
	{
		/*
		 * If the selection is empty, and the first added object is an
		 * edge, lock the selection set into edge mode. All
		 * subsequent vertex additions will be ignored!
		 *  
		 */
		if (m_Vertices.isEmpty() && m_Edges.isEmpty())
		{
			m_bVertexMode = false;
		}

		if (!m_bVertexMode)
		{
			m_Edges.add(edge);
			edge.select(); // hilight this edge

			/* Add edge vertices to the vertex vector. This allows 
			 * the edges to be modified in the same ways as vertices
			 * (i.e. translation, scaling etc.) without any special
			 * code in the transformation methods. Thus, when in 
			 * edge selection mode, the 'usual' vertex vector 
			 * is used internally to transform the selection.
			 */

			Vertex v1 = edge.getSource();
			Vertex v2 = edge.getTarget();

			if (!m_Vertices.contains(v1))
				m_Vertices.add(v1);

			if (!m_Vertices.contains(v2))
				m_Vertices.add(v2);

			m_iTransformFlag = NOP;
			m_bContentsChanged = true;

			update();
			updateRotationCenter();
		}
	}


	//*************************************************************************

	/**
	 * Remove an edge
	 * 
	 * @param edge
	 *            The edge
	 * 
	 */
	public void removeEdge(
			Edge edge)
	{
		if (!m_bVertexMode)
		{
			m_Edges.remove(edge);
			edge.deselect();

			// remove corresponding vertices from selection as well
			Vertex v1 = edge.getSource();
			Vertex v2 = edge.getTarget();

			if (m_Vertices.contains(v1))
				m_Vertices.remove(v1);

			if (m_Vertices.contains(v2))
				m_Vertices.remove(v2);

			m_iTransformFlag = NOP;
			m_bContentsChanged = true;

			update();
			updateRotationCenter();
		}
	}


	//*************************************************************************

	/**
	 * Enable the option to delete edges
	 * 
	 * @param on
	 *            true to enable, false to disable
	 * 
	 */
	public void setEdgeDeleteEnabled(
			boolean on)
	{
		m_bEdgeDeletionEnabled = on;
	}


	//*************************************************************************

	/**
	 * Set the selection color
	 * 
	 * @param color
	 *            The new color
	 */
	public void setSelectionColor(
			Color color)
	{
		if (m_bVertexMode)
		{
			// set vertex color
			Vertex v;
			Enumeration vts = m_Vertices.elements();
			while (vts.hasMoreElements())
			{
				v = (Vertex) vts.nextElement();
				v.setColor(color);
			}
		}
		else
		{
			// set edge color
			Edge e;
			Enumeration edges = m_Edges.elements();
			while (edges.hasMoreElements())
			{
				e = (Edge) edges.nextElement();
				e.setColor(color);
			}
		}

		if ((m_bColorizeEdges == true) && (m_bVertexMode == true))
		{
			// use vertex colors for bounded edges

			Vertex v1, v2;
			Edge edge;

			Iterator<Edge> edges = m_Scene.getEdges();
			while (edges.hasNext())
			{
				edge = edges.next();

				v1 = edge.getSource();
				v2 = edge.getTarget();

				// only colorize edges whose both vertices are 
				// inside selection
				if (m_Vertices.contains(v1) && m_Vertices.contains(v2))
				{
					edge.setColor(color);
				}
			}
		}// endif
	}


	//*************************************************************************

	/**
	 * Returns the selection color
	 * 
	 * @return The color
	 */
	public Color getSelectionColor()
	{
		//FIXME: Need better definition here...
		return ((Vertex) m_Vertices.get(0)).getColor();
	}


	//*************************************************************************

	/**
	 * Enable the coloring of edges
	 * 
	 * @param on
	 *            true to enable, false to disable
	 * 
	 */
	public void enableEdgeColoring(
			boolean on)
	{
		m_bColorizeEdges = on;
	}


	//*************************************************************************

	/**
	 * Is edge coloring enabled?
	 * 
	 * @return true, if it is enabled, false else
	 * 
	 */
	public boolean isEdgeColoringEnabled()
	{
		return m_bColorizeEdges;
	}


	//*************************************************************************

	/**
	 * Return the affine transformation object
	 * 
	 * @return The transformation object
	 */
	public AffineTransform getTransform()
	{
		return m_accumulatedTransform;
	}


	//*************************************************************************

	/**
	 * Returns the type of the transformation
	 * 
	 * @return The tranformation type
	 */
	public int getTransformType()
	{
		int type = 0x00;
		if (m_iModeFlag == TRANSLATE_SCALE_MODE)
		{
			switch (m_iTransformFlag)
			{
				case TRANSLATE:
					type = SELECTION_TRANSLATED;
					break;

				case N_SCALE:
				case S_SCALE:
				case W_SCALE:
				case E_SCALE:
				case NW_SCALE:
				case NE_SCALE:
				case SW_SCALE:
				case SE_SCALE:
					type = SELECTION_SCALED;
					break;

			}
		}
		else
		{
			switch (m_iTransformFlag)
			{
				case ROTATE:
					type = SELECTION_ROTATED;
					break;

				case S_SHEAR:
				case N_SHEAR:
				case W_SHEAR:
				case E_SHEAR:
					type = SELECTION_SHEARED;
					break;
			}
		}
		return type;
	}


	//*************************************************************************

	/**
	 * Returns the list of selected vertices
	 * 
	 * @return The list of vertices
	 */
	public Vector getSelectedVertices()
	{
		return m_Vertices;
	}


	//*************************************************************************

	/**
	 * Returns the list of selected edges
	 * 
	 * @return The list of edges
	 */
	public Vector getSelectedEdges()
	{
		return m_Edges;
	}


	//*************************************************************************

	/**
	 * Has the data been modified lately?
	 * 
	 * @return true, if changes to the scene have been made, false else
	 */
	public boolean hasBeenModified()
	{
		return m_Scene.sceneHasChanged();
	}


	//*************************************************************************

	/**
	 * Does the scene contain a certain vertex?
	 * 
	 * @param vertex
	 *            The vertex
	 * 
	 * @return true, if the scene contains the vertex, false else
	 */
	public boolean contains(
			Vertex vertex)
	{
		return m_Vertices.contains(vertex);
	}


	//*************************************************************************

	/**
	 * Does the scene contain a certain edge?
	 * 
	 * @param edge
	 *            The edge
	 * 
	 * @return true, if the scene contains the edge, false else
	 */
	public boolean contains(
			Edge edge)
	{
		return m_Edges.contains(edge);
	}


	//*************************************************************************

	/**
	 * Returns if the point lies in the bounding box of the scene
	 * 
	 * @param point
	 *            The point
	 * 
	 * @return true, if inside the bounding box, false else
	 */
	public boolean pointInBox(
			Point2 point)
	{
		/* If bound box is zero-point (i.e. the selection
		 * is empty), always return false, to avoid 
		 * certain conflicts with JGenericGraphEditor's 
		 * event handlers.
		 * 
		 */
		if ((m_BoundingBox.width < DBL_EPSILON)
				&& (m_BoundingBox.height < DBL_EPSILON))
		{
			return false;
		}
		else
			return m_BoundingBox.contains(point);
	}


	//*************************************************************************

	/**
	 * Returns, if the point is contained in the marker
	 * 
	 * @param point
	 *            The point
	 * 
	 * @return true, if point contained, false else
	 */
	public boolean pointInRotationMarker(
			Point2 point)
	{
		return m_rotCenterMarker.contains(point);
	}


	//*************************************************************************

	/**
	 * Returns if the scene is empty
	 * 
	 * @return true, if no edge nor vertex is saved in the scene, false else
	 */
	public boolean isEmpty()
	{
		return (m_Vertices.isEmpty() & m_Edges.isEmpty());
	}


	//*************************************************************************

	/**
	 * Returns the vertex mode
	 * 
	 * @return true, if enabled, false else
	 * 
	 */
	public boolean isInVertexMode()
	{
		return m_bVertexMode;
	}


	//*************************************************************************

	/**
	 * Sets the selector box between the two points source and target
	 * 
	 * @param source
	 *            The first point
	 * @param target
	 *            The second point
	 */
	public void setFromPoints(
			Point2 source,
			Point2 target)
	{
		// reset boxes
		//m_boundingBox.setRect(0,0,0,0);
		//m_selectorBox.setRect(0,0,0,0);

		m_selectorBox.setFromPoints(source, target);

		/*
		 * If the selection is empty, and the first added object is a
		 * vertex, lock the selection set into vertex mode. All
		 * subsequent edge additions will be ignored!
		 *  
		 */
		if (m_Vertices.isEmpty() & m_Edges.isEmpty())
		{
			m_bVertexMode = true;
		}

		selectVertices();

		m_iModeFlag = TRANSLATE_SCALE_MODE;
		m_iTransformFlag = NOP;

		/* Only recalculate boxes / markers etc. if 
		 * something was really selected. (and more than
		 * one vertex, since it would be silly to try to
		 * manipulate it)
		 * 
		 * Reason:
		 * 
		 * 1) the bounding box is used to determine when
		 * the selector box should receive mouse events
		 * (namely when the mouse is inside it). If the
		 * selection is empty but the rectangle is nonetheless
		 * non-degenerate, the events will be send to the box
		 * even though there's nothing to modify.
		 * 
		 * 2) It saves some CPU cycles :]
		 * 
		 */
		if (m_Vertices.size() > 1)
		{
			update();

			// set default rotation center and its marker
			updateRotationCenter();

			// test section
			_testShape = new Rectangle2(m_BoundingBox);
		}
	}


	//*************************************************************************

	/**
	 * Sets the selector box
	 * 
	 * @param source
	 *            The new rectangle = selector box
	 */
	public void setBox(
			Rectangle2 source)
	{
		m_selectorBox.setRect(source);

		//TODO: update code
	}


	//*************************************************************************

	/**
	 * Set the pixel size
	 * 
	 * @param size The new size
	 */
	public void setPixelSize(
			double size)
	{
		m_fPixelSize = (float) size;
		update();
	}


	//*************************************************************************

	/**
     * Updates the scene
     *
     */
	public void update()
	{
		//reset boxes
		m_BoundingBox.setRect(0, 0, 0, 0);
		m_selectorBox.setRect(0, 0, 0, 0);

		if (m_Vertices.isEmpty() && m_Edges.isEmpty())
		{
			return; // bail..
		}

		updateSelectionBBox();
		m_BoundingBox.setRect(m_selectorBox);

		// re-position markers and icons
		setupScaleMarkers();
		setupRotationShearMarkers();

		// testing section
		//_testShape = m_boundingBox;
	}


	//*************************************************************************

	/**
	 * Draw the scene
	 * 
	 * @param g The graphics object
	 */
	public void draw(
			Graphics2D g)
	{
		if (m_Vertices.isEmpty() && m_Edges.isEmpty())
			return; // nothing to draw..

		// frame
		m_selectorBox.draw(g, m_normalGC);

		// half-size for center markers, in pixels
		double size;
		double x, y; // temporary position vars

		// mode logic
		if (m_iModeFlag == TRANSLATE_SCALE_MODE)
		{
			size = 0.9 * MARKER_SIZE / m_fPixelSize;

			//draw scale mode markers
			for (int i = 0; i < m_Markers.length; i++)
			{
				m_Markers[i].draw(g, m_widgetGC);
			}

			//draw center mark	
			x = m_selectorBox.getCenterX();
			y = m_selectorBox.getCenterY();

			Line2D h = new Line2D.Double(x - size, y, x + size, y);
			Line2D v = new Line2D.Double(x, y - size, x, y + size);

			g.draw(h);
			g.draw(v);

			AffineTransform tx = g.getTransform();
			g.setTransform(new AffineTransform());
			{
				int xx, yy;
				xx = m_coordSystem.worldToScreenX(m_BoundingBox.getMinX());
				yy = m_coordSystem.worldToScreenY(m_BoundingBox.getMaxY() + 10
						/ m_fPixelSize);

				g.drawString(m_mirrorMarkerText, xx, yy);
			}
			g.setTransform(tx);

		}
		else if (m_iModeFlag == ROTATE_SHEAR_MODE)
		{

			//m_rotCenterMarker.draw(g, m_normalGC);

			/* Draw rotation / shear marker icons. Because
			 * images are also affected by affine transforms,
			 * the graphics context's transform is temporarily
			 * disabled, and the upper-left corner positions
			 * for all images are transformed 'manually' instead.
			 * 
			 */

			AffineTransform tx = g.getTransform();
			g.setTransform(new AffineTransform());

			// draw center mark & cross
			int ssize = Math.round(0.9f * MARKER_SIZE);

			int xx = m_coordSystem.worldToScreenX(m_rotationCenter.x);
			int yy = m_coordSystem.worldToScreenY(m_rotationCenter.y);

			g.drawLine(xx - ssize, yy, xx + ssize, yy);
			g.drawLine(xx, yy - ssize, xx, yy + ssize);

			ssize = Math.round(0.6f * MARKER_SIZE);
			g.drawOval(xx - ssize, yy - ssize, 2 * ssize, 2 * ssize);

			// draw icons
			for (int i = 0; i < m_IconPositions.length; i++)
			{
				Image img = m_RotationShearIcons[i];

				x = m_IconPositions[i].x;
				y = m_IconPositions[i].y;

				g.drawImage(img, m_coordSystem.worldToScreenX(x),
						m_coordSystem.worldToScreenY(y), null);
			}
			g.setTransform(tx);
		}

		// draw status line		
		//int w = m_Hub.getDisplayPanel().getWidth();
		int h = m_Hub.getDisplayPanel().getHeight();

		int num_objects = m_bVertexMode ? m_Vertices.size() : m_Edges.size();
		String mode = m_bVertexMode ? "Vertex mode" : "Edge mode";

		AffineTransform tx = g.getTransform();
		g.setTransform(new AffineTransform());
		{
			g.drawString(mode + " , " + String.valueOf(num_objects)
					+ " objects selected", 10, h - 10);
		}
		g.setTransform(tx);

		// test section		
		//g.setColor(Color.blue);
		//g.draw(_testRect);
		//g.draw(_testShape);

	}


	//*************************************************************************

	/**
	 * Select all vertices inside the selector box
	 * 
	 */
	public void selectVertices()
	{
		Iterator<Vertex> vts = m_Scene.getVertices();

		Vertex v;

		// deselect previous set
		while (vts.hasNext())
		{
			v = vts.next();
			v.deselect();
		}
		m_Vertices.clear();

		// select new set
		vts = m_Scene.getVertices();

		while (vts.hasNext())
		{
			v = vts.next();

			if (m_selectorBox.contains(v.getBoundingBox()))
			{
				//v.select();
				m_Vertices.add(v);
			}
		}

		/* If only one vertex has been selected, just leave
		 * it alone since it doesn't make sense to manipulate
		 * a one-vertex selection :\
		 * 
		 */
		if (m_Vertices.size() == 1)
		{
			m_Vertices.remove(m_Vertices.get(0));
			return;
		}

		update();
		//updateSelectionBBox();
	}


	//*************************************************************************

	/**
	 * Deselect all vertices
	 * 
	 */
	public void deselectAll()
	{
		if (m_bVertexMode)
		{
			Vertex v;
			Enumeration vts = m_Vertices.elements();

			//deselect previous set		
			while (vts.hasMoreElements())
			{
				v = (Vertex) vts.nextElement();
				v.deselect();
			}

			m_Vertices.clear();
		}
		else
		{
			Edge e;
			Enumeration edges = m_Edges.elements();

			// deselect previous set
			while (edges.hasMoreElements())
			{
				e = (Edge) edges.nextElement();
				e.deselect();
			}

			m_Edges.clear();
			m_Vertices.clear();
		}

		update();
		//updateSelectionBBox();
	}


	//*************************************************************************

	/**
	 * Deletes all selected vertices or edges depending on 'vertex mode'
	 * 
	 */
	public void deleteSelection()
	{
		if (m_bVertexMode)
		{
			// remove selected vertices
			Enumeration e = m_Vertices.elements();

			Vertex v;
			Edge edge;

			/* A little explanation is in order here:
			 * When a vertex is removed, its adjacent edges are implicitly
			 * removed as well inside VisualGraph. Thus, it becomes 
			 * necessary to synchronize edge deletions with all registered
			 * GraphListener instances - hence the firing of EDGE_REMOVED
			 * events in the inner loop! 
			 * The net result is that the VERTEX_SELECTION_REMOVED event
			 * that carries the information about removed vertices is 
			 * preceded by a series of EDGE_REMOVED events with these
			 * vertices' adjacent edges.
			 */

			while (e.hasMoreElements())
			{

				v = (Vertex) e.nextElement();

				/*LinkedList edges = m_Scene.getAdjacentEdges(v);
				Iterator it = edges.iterator();
				while(it.hasNext())
				{
				    edge = (Edge)it.next();
				    
				    m_Editor.fireGraphEditorEvent(m_Editor, edge,
				                                  EDGE_REMOVED);
				}*/
				m_Scene.removeVertex(v);
			}
		}
		else
		{

			// if edge deletion is disabled, abort operation...
			if (m_bEdgeDeletionEnabled == false)
				return;

			// remove selected edges
			Enumeration edges = m_Edges.elements();
			Edge edge;

			while (edges.hasMoreElements())
			{
				edge = (Edge) edges.nextElement();

				//m_Editor.fireGraphEditorEvent(m_Editor, edge, EDGE_REMOVED);

				m_Scene.removeEdge(edge);
			}
		}

		/* The references to selected elements that have just
		 * been removed from the graph are also contained in the 
		 * selection set itself, so they have to be removed from
		 * it as well. Hence the call to deselectAll() !
		 */

		deselectAll();
		m_Scene.setChangeFlag();

		update();
	}


	//*************************************************************************
	// 			           Event handlers
	//*************************************************************************

	
	/**
	 * Handle mouse events
	 * 
	 * @param event The mouse event
	 */
	public void mousePressed(
			MouseEvent event)
	{
		//store mouse parameters
		m_iMouseButton = event.getButton();
		m_iMouseModifiers = event.getModifiersEx();
		m_mousePosition = event.getPoint();

		m_bContentsChanged = false;

		// reset transformation matrices
		m_TransformMatrix.setToIdentity();
		m_T_Matrix.setToIdentity();
		m_inv_T_Matrix.setToIdentity();
		m_accumulatedTransform.setToIdentity();

		// convert mouse position to world coordinates 
		Point2 world_position = new Point2();
		m_coordSystem.screenToWorld(m_mousePosition, world_position, false);

		// store initial mouse coordinates
		m_initialWorldMousePosition.setLocation(world_position);
		m_prevWorldMousePosition.setLocation(world_position);

		m_prevMousePosition.setLocation(m_mousePosition);

		// transform type switch

		m_iTransformFlag = NOP; // initially assume no-op 

		//test for marker positions		
		int marker = 0;
		boolean found = false;
		for (; marker < m_Markers.length; marker++)
		{
			if (m_Markers[marker].contains(world_position))
			{
				found = true;
				break;
			}
		}

		//if SHIFT is down, set to uniform transformation mode
		if ((m_iMouseModifiers & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK)
		{
			m_bUniformMode = true;
			//m_Hub.getTextDump().println("Uniform scale");
		}

		if (m_iModeFlag == TRANSLATE_SCALE_MODE)
		{
			if (found)
			{
				/* see explanation in declaration
				 * of *_SCALE constants.
				 */
				m_iTransformFlag = marker;
			}
			else if (m_selectorBox.contains(world_position))
			{
				// set translate mode
				m_iTransformFlag = TRANSLATE;
			}
		}
		else if (m_iModeFlag == ROTATE_SHEAR_MODE)
		{
			if (found)
			{
				/* marker is even => cursor is in one of the 
				* four corners => switch to ROTATE mode
				* See explanation in declaration of 
				* *_SHEAR constants.
				*/
				if (marker % 2 == 0)
				{
					m_iTransformFlag = ROTATE;
				}
				else
				{
					/* marker is odd => cursor is on one
					 * of the shear icons => set to 
					 * specific shear mode.  
					 */

					m_iTransformFlag = marker;
				}
			}

			// test for rotation center marker
			if (m_rotCenterMarker.contains(world_position))
			{
				m_iTransformFlag = MOVE_ROTATION_CENTER;
			}

		}// end elseif

		m_mirrorMarkerText = "";
		// testing section
	}


	//*************************************************************************

	/**
	 * Handle mouse events
	 * 
	 * @param event The mouse event
	 */
	public void mouseDragged(
			MouseEvent event)
	{
		m_mousePosition = event.getPoint();

		// convert mouse position to world coordinates 
		Point2 world_position = new Point2();
		m_coordSystem.screenToWorld(m_mousePosition, world_position, true); // for now...

		// mode logic

		if (m_iModeFlag == TRANSLATE_SCALE_MODE)
		{
			switch (m_iTransformFlag)
			{
				case TRANSLATE:

					translateSelection(m_prevWorldMousePosition, world_position);
					break;

				case N_SCALE:
				case S_SCALE:
				case W_SCALE:
				case E_SCALE:
				case NW_SCALE:
				case NE_SCALE:
				case SW_SCALE:
				case SE_SCALE:

					if (m_bUniformMode)
					{
						uniformScaleSelection(m_prevWorldMousePosition,
								world_position);
					}
					else
					{
						scaleSelection(m_prevWorldMousePosition, world_position);
					}
					break;

			}
		}
		else if (m_iModeFlag == ROTATE_SHEAR_MODE)
		{
			switch (m_iTransformFlag)
			{

				case MOVE_ROTATION_CENTER:

					Point2 delta = new Point2(world_position.x
							- m_prevWorldMousePosition.x, world_position.y
							- m_prevWorldMousePosition.y);

					m_rotationCenter.translate(delta);
					m_rotCenterMarker.translate(delta);

					break;

				case ROTATE:

					rotateSelection(m_prevWorldMousePosition, world_position);
					break;

				case N_SHEAR:
				case S_SHEAR:
				case W_SHEAR:
				case E_SHEAR:

					if (m_bUniformMode)
					{
						uniformShearSelection(m_prevWorldMousePosition,
								world_position);
					}
					else
					{
						shearSelection(m_prevWorldMousePosition, world_position);
					}
					break;
			} // end switch

		}

		//save previous mouse position
		m_prevMousePosition.setLocation(m_mousePosition);
		m_prevWorldMousePosition.setLocation(world_position);

		m_bMouseWasDragged = true;

		update();
	}


	//*************************************************************************

	/**
	 * Handle mouse events
	 * 
	 * @param event The mouse event
	 */
	public void mouseReleased(
			MouseEvent event)
	{
		// put together the accumulated transform matrix for
		// undos

		m_T_Matrix.setToTranslation(-m_transformCenter.x, -m_transformCenter.y);

		m_inv_T_Matrix.setToTranslation(m_transformCenter.x,
				m_transformCenter.y);

		// make the (T^-1) * Rx * (T) chain

		m_accumulatedTransform.concatenate(m_T_Matrix);
		m_inv_T_Matrix.concatenate(m_accumulatedTransform);

		m_accumulatedTransform.setTransform(m_inv_T_Matrix);

		if (m_bMouseWasDragged)
		{

		}
		else
		{
			if ((m_iMouseButton == MouseEvent.BUTTON1)
					&& (m_bContentsChanged == false))
			{
				//flip mode: translate/scale <-> rotate/shear
				if (m_iModeFlag == TRANSLATE_SCALE_MODE)
					m_iModeFlag = ROTATE_SHEAR_MODE;
				else
					m_iModeFlag = TRANSLATE_SCALE_MODE;
			}
		}

		m_bMouseWasDragged = false;
		m_bUniformMode = false;
		m_mirrorMarkerText = "";

		//test
		//m_Hub.getTextDump().println(m_accumulatedTransform.toString());

		update();
	}


	//*************************************************************************

	/**
	 * Handle mouse events
	 * 
	 * @param event The mouse event
	 */
	public void mouseMoved(
			MouseEvent event)
	{
		if (m_Vertices.isEmpty())
			return;

		/* Change cursors depending on mouse location within
		 * the selector to signify various transformation
		 * options. 
		 */
		JSimpleDisplayPanel display = m_Hub.getDisplayPanel();
		Point p = event.getPoint();
		Point2 world_pos = m_coordSystem.screenToWorld(p);

		Cursor cursor = Cursor.getDefaultCursor();

		//test for marker positions		
		int marker = 0;
		boolean found = false;
		for (; marker < 8; marker++)
		{
			if (m_Markers[marker].contains(world_pos))
			{
				found = true;
				break;
			}
		}

		// mode logic
		if (m_iModeFlag == TRANSLATE_SCALE_MODE)
		{
			/* Mouse hit one of the markers - 
			 * change to corresponding cursor and
			 * set the corresponding mode flag 
			 */
			if (found)
			{
				cursor = SCALE_CURSORS[marker];
			}
			else if (m_selectorBox.contains(world_pos))
			{
				// translate mode
				cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
			}
			else
			{
				cursor = Cursor.getDefaultCursor();
			}
		}
		else if (m_iModeFlag == ROTATE_SHEAR_MODE)
		{
			if (found)
			{
				cursor = m_customCursors[marker];
			}
		}

		display.setCursor(cursor);
	}


	//*************************************************************************
	// 			     Protected instance methods
	//*************************************************************************

	//------------------------ Transformation methods -------------------------

	protected void scaleSelection(
			Point2 source,
			Point2 target)
	{
		Point2 center = new Point2();
		Point2 corner = new Point2();
		double xscale = 1.0, yscale = 1.0;

		/* scale factor signs
		 * These are used to 'flip' the scaling
		 * direction depending on the position of the mouse
		 * relative to the transformation center.
		 */
		//double xsign = 1.0, ysign = 1.0;

		double source_distance; // between center and a box corner
		double target_distance; // between center and mouse position

		/* Setup transformation center and corner. This influences
		 * some code below, as well.
		 */

		switch (m_iTransformFlag)
		{
			case NW_SCALE: // upper - left corner

				center.setLocation(m_selectorBox.getMaxX(),
						m_selectorBox.getMinY());
				corner.setLocation(m_selectorBox.getMinX(),
						m_selectorBox.getMaxY());

				break;

			case N_SCALE: // top edge

				center.setLocation(m_selectorBox.getMidX(),
						m_selectorBox.getMinY());
				corner.setLocation(m_selectorBox.getMidX(),
						m_selectorBox.getMaxY());

				break;

			case NE_SCALE: // upper-right corner

				center.setLocation(m_selectorBox.getMinX(),
						m_selectorBox.getMinY());
				corner.setLocation(m_selectorBox.getMaxX(),
						m_selectorBox.getMaxY());

				break;

			case E_SCALE: // right edge

				center.setLocation(m_selectorBox.getMinX(),
						m_selectorBox.getMidY());
				corner.setLocation(m_selectorBox.getMaxX(),
						m_selectorBox.getMidY());
				break;

			case SE_SCALE: // lower-right corner

				center.setLocation(m_selectorBox.getMinX(),
						m_selectorBox.getMaxY());
				corner.setLocation(m_selectorBox.getMaxX(),
						m_selectorBox.getMinY());
				break;

			case S_SCALE: // bottom edge

				center.setLocation(m_selectorBox.getMidX(),
						m_selectorBox.getMaxY());
				corner.setLocation(m_selectorBox.getMidX(),
						m_selectorBox.getMinY());
				break;

			case SW_SCALE: // lower-left corner

				center.setLocation(m_selectorBox.getMaxX(),
						m_selectorBox.getMaxY());
				corner.setLocation(m_selectorBox.getMinX(),
						m_selectorBox.getMinY());
				break;

			case W_SCALE: // left edge

				center.setLocation(m_selectorBox.getMaxX(),
						m_selectorBox.getMidY());
				corner.setLocation(m_selectorBox.getMinX(),
						m_selectorBox.getMidY());
				break;
		}

		switch (m_iTransformFlag)
		{
			// corner-centered scales
			case NW_SCALE: // upper-left
			case NE_SCALE: // upper-right 
			case SE_SCALE: // lower-right
			case SW_SCALE: // lower-left
				{
					// calculate x-axis scale
					source_distance = Math.abs(corner.x - center.x);
					target_distance = Math.abs(target.x - center.x);

					xscale = (target_distance / source_distance);

					// calculate y-axis scale
					source_distance = Math.abs(corner.y - center.y);
					target_distance = Math.abs(target.y - center.y);

					yscale = (target_distance / source_distance);
				}
				break;

			// vertical scales
			case N_SCALE:
			case S_SCALE:
				{

					source_distance = Math.abs(corner.y - center.y);
					target_distance = Math.abs(target.y - center.y);

					xscale = 1.0; // no scale along X axis
					yscale = (target_distance / source_distance);
				}
				break;

			// horizontal scales
			case E_SCALE:
			case W_SCALE:
				{
					source_distance = Math.abs(corner.x - center.x);
					target_distance = Math.abs(target.x - center.x);

					xscale = (target_distance / source_distance);
					yscale = 1.0; // no scale on Y axis		
				}
				break;
		}

		// test section
		// END

		// initialize transform components
		m_T_Matrix.setToTranslation(-center.x, -center.y);
		m_inv_T_Matrix.setToTranslation(center.x, center.y);
		m_TransformMatrix.setToScale(xscale, yscale);

		//create the combined  (T^-1) * Rx * (T) matrix
		m_TransformMatrix.concatenate(m_T_Matrix);
		m_inv_T_Matrix.concatenate(m_TransformMatrix);

		//clamp scaling values

		// figure out the size of transformed bounding box diagonal

		Point2 corner1 = new Point2(m_selectorBox.getMinX(),
				m_selectorBox.getMinY());

		Point2 corner2 = new Point2(m_selectorBox.getMaxX(),
				m_selectorBox.getMaxY());

		m_inv_T_Matrix.transform(corner1, corner1);
		m_inv_T_Matrix.transform(corner2, corner2);

		double h_length = Math.abs(corner2.x - corner1.x);
		double v_length = Math.abs(corner2.y - corner1.y);

		double vertex_radius = ((Vertex) m_Vertices.get(0)).getActualRadius();

		if ((h_length < 4 * vertex_radius) || (v_length < 4 * vertex_radius))
		{
			xscale = yscale = 1.0; // supress scaling

			//initialize transform components
			m_T_Matrix.setToTranslation(-center.x, -center.y);
			m_inv_T_Matrix.setToTranslation(center.x, center.y);
			m_TransformMatrix.setToScale(xscale, yscale);

			//create the combined  (T^-1) * Rx * (T) matrix
			m_TransformMatrix.concatenate(m_T_Matrix);
			m_inv_T_Matrix.concatenate(m_TransformMatrix);

			//update the accumulated transform & center

			/* In order to maintain the correct order of 
			 * transformations, the accumulated affine transform
			 * must be in this case concatenated with an
			 * identity matrix. 
			 * Simply concatenating it with the current scaling
			 * matrix [before the scaling values get clamped] produces
			 * an incorrect inverse which, when later applied during
			 * and undo operation, blows up all
			 * vertex coordinates beyond good and evil.
			 */

			// this has the effect TX * E = TX
			m_accumulatedTransform.concatenate(new AffineTransform());
			m_transformCenter.setLocation(center);
		}
		else
		{
			m_TransformMatrix.setToScale(xscale, yscale);
			m_accumulatedTransform.concatenate(m_TransformMatrix);
			m_transformCenter.setLocation(center);
		}

		// scale selection
		Enumeration e = m_Vertices.elements();
		while (e.hasMoreElements())
		{
			Vertex v = (Vertex) e.nextElement();
			Point2D position = v.getPosition();

			m_inv_T_Matrix.transform(position, position);

			v.moveTo(position);
		}

		/* don't forget the rotation center, as it's also 
		 * a point inside the set!
		 */

		m_inv_T_Matrix.transform(m_rotationCenter, m_rotationCenter);
		m_Scene.setChangeFlag();

	}


	//*************************************************************************

	protected void uniformScaleSelection(
			Point2 source,
			Point2 target)
	{
		// transformation center
		Point2 center = new Point2();

		// box corner
		Point2 corner = new Point2();

		// x / y scale factors
		double xscale = 1.0, yscale = 1.0;

		/* scale factor signs
		 * These are used to 'flip' the scaling
		 * direction depending on the position of the mouse
		 * relative to the transformation center.
		 */
		double xsign = 1.0, ysign = 1.0;

		double source_distance; // between center and a box corner
		double target_distance; // between center and mouse position

		/* Setup transformation center. This influences
		 * some code below, as well.
		 */

		/* scale relative to the box' center
		 * In this case any corner can be used in the 
		 * calculations of scaling factors, since the
		 * box is symmetrical. I used the upper-right
		 * corner, but it could as well have been
		 * any other.
		 */
		center.setLocation(m_selectorBox.getCenter());

		double threshold = 0.0; //FLT_EPSILON;

		//detect zero crossings							
		if ((((target.x - center.x) > threshold) && ((source.x - center.x) < -threshold))
				|| (((target.x - center.x) < -threshold) && ((source.x - center.x) > threshold)))
		{
			xsign = -1.0;
		}

		if ((((target.y - center.y) > threshold) && ((source.y - center.y) < -threshold))
				|| (((target.y - center.y) < -threshold) && ((source.y - center.y) > threshold)))
		{
			ysign = -1.0;
		}

		switch (m_iTransformFlag)
		{
			// uniform x/y scale
			case NW_SCALE:
			case NE_SCALE:
			case SE_SCALE:
			case SW_SCALE:
				{
					//FIXME: Don't use the diagonal metric here!!!
					// It doesn't work properly!

					//double scale;

					corner.setLocation(m_selectorBox.getMaxX(),
							m_selectorBox.getMaxY());

					source_distance = center.distance(corner);
					target_distance = center.distance(target);

					xscale = (target_distance / source_distance);
					yscale = (target_distance / source_distance);
				}
				break;

			// uniform vertical scale
			case N_SCALE:
			case S_SCALE:
				{
					corner.setLocation(0.0, m_selectorBox.getMaxY());

					source_distance = Math.abs(corner.y - center.y);
					target_distance = Math.abs(target.y - center.y);

					xscale = 1.0; // no scale along X axis
					yscale = (target_distance / source_distance);
				}
				break;

			// uniform horizontal scale
			case E_SCALE:
			case W_SCALE:
				{
					corner.setLocation(m_selectorBox.getMaxX(), 0.0);

					source_distance = Math.abs(corner.x - center.x);
					target_distance = Math.abs(target.x - center.x);

					xscale = (target_distance / source_distance);
					yscale = 1.0; // no scale on Y axis		
				}
				break;
		}

		// initialize transform components
		m_T_Matrix.setToTranslation(-center.x, -center.y);
		m_inv_T_Matrix.setToTranslation(center.x, center.y);
		m_TransformMatrix.setToScale(xscale, yscale);

		//create the combined  (T^-1) * Rx * (T) matrix
		m_TransformMatrix.concatenate(m_T_Matrix);
		m_inv_T_Matrix.concatenate(m_TransformMatrix);

		//clamp scaling values
		// figure out the size of transformed bounding box diagonal

		Point2 corner1 = new Point2(m_selectorBox.getMinX(),
				m_selectorBox.getMinY());

		Point2 corner2 = new Point2(m_selectorBox.getMaxX(),
				m_selectorBox.getMaxY());

		m_inv_T_Matrix.transform(corner1, corner1);
		m_inv_T_Matrix.transform(corner2, corner2);

		double h_length = Math.abs(corner2.x - corner1.x);
		double v_length = Math.abs(corner2.y - corner1.y);

		double vertex_radius = ((Vertex) m_Vertices.get(0)).getActualRadius();

		/* The metric is the current radius of the vertices,
		 * the transformed bounding box must still be large
		 * enough to contain at least a single vertex (the point
		 * at which the coordinates of individual vertices are
		 * still far enough apart for them to be distinguished.)
		 */

		if ((h_length < 6 * vertex_radius) || (v_length < 6 * vertex_radius))
		{
			/* supress scaling, but preserve 
			 * potential mirroring. 
			 */
			xscale = xsign;
			yscale = ysign;

			if ((xsign < 0.0) && (ysign < 0.0))
				m_mirrorMarkerText = "Mirror X/Y";
			else if (xsign < 0.0)
				m_mirrorMarkerText = "Mirror X";
			else if (ysign < 0.0)
				m_mirrorMarkerText = "Mirror Y";

			//initialize transform components
			m_T_Matrix.setToTranslation(-center.x, -center.y);
			m_inv_T_Matrix.setToTranslation(center.x, center.y);
			m_TransformMatrix.setToScale(xscale, yscale);

			//update the accumulated transform
			m_accumulatedTransform.concatenate(m_TransformMatrix);

			//create the combined  (T^-1) * Rx * (T) matrix
			m_TransformMatrix.concatenate(m_T_Matrix);
			m_inv_T_Matrix.concatenate(m_TransformMatrix);
		}
		else
		{
			/* See explanation in scaleSelection() */
			m_TransformMatrix.setToScale(xscale, yscale);

			//update the accumulated transform
			m_accumulatedTransform.concatenate(m_TransformMatrix);
			m_transformCenter.setLocation(center);
		}

		// scale selection
		Enumeration e = m_Vertices.elements();
		while (e.hasMoreElements())
		{
			Vertex v = (Vertex) e.nextElement();
			Point2D position = v.getPosition();

			m_inv_T_Matrix.transform(position, position);
			v.moveTo(position);
		}

		/* don't forget the rotation center, as it's also 
		 * a point inside the set!
		 */

		m_inv_T_Matrix.transform(m_rotationCenter, m_rotationCenter);
		m_Scene.setChangeFlag();
	}


	//*************************************************************************

	protected void shearSelection(
			Point2 source,
			Point2 target)
	{
		Point2 center = new Point2();
		Point2 corner = new Point2();

		double xshear = 0.0, yshear = 0.0;

		/* Setup transformation center and corner. This influences
		 * some code below, as well.
		 */

		switch (m_iTransformFlag)
		{

			case N_SHEAR: // top edge

				center.setLocation(m_selectorBox.getMidX(),
						m_selectorBox.getMinY());
				corner.setLocation(m_selectorBox.getMidX(),
						m_selectorBox.getMaxY());
				break;

			case S_SHEAR: // bottom edge

				center.setLocation(m_selectorBox.getMidX(),
						m_selectorBox.getMaxY());
				corner.setLocation(m_selectorBox.getMidX(),
						m_selectorBox.getMinY());
				break;

			case W_SHEAR: // left edge

				center.setLocation(m_selectorBox.getMaxX(),
						m_selectorBox.getMidY());
				corner.setLocation(m_selectorBox.getMinX(),
						m_selectorBox.getMidY());
				break;

			case E_SHEAR: // right edge

				center.setLocation(m_selectorBox.getMinX(),
						m_selectorBox.getMidY());
				corner.setLocation(m_selectorBox.getMaxX(),
						m_selectorBox.getMidY());
				break;

		}

		// calculate shearing coefficients
		switch (m_iTransformFlag)
		{

			// horizontal shear
			case N_SHEAR:

				yshear = 0.0;
				xshear = (target.x - source.x) / m_selectorBox.getWidth();
				break;
			case S_SHEAR:

				yshear = 0.0;
				xshear = -(target.x - source.x) / m_selectorBox.getWidth();
				break;

			// vertical shear
			case W_SHEAR:

				xshear = 0.0;
				yshear = -(target.y - source.y) / m_selectorBox.getHeight();
				break;

			case E_SHEAR:
				xshear = 0.0;
				yshear = (target.y - source.y) / m_selectorBox.getHeight();
				break;
		}

		//initialize transform components
		m_T_Matrix.setToTranslation(-center.x, -center.y);
		m_inv_T_Matrix.setToTranslation(center.x, center.y);
		m_TransformMatrix.setToShear(xshear, yshear);

		//update the accumulated transform
		m_accumulatedTransform.concatenate(m_TransformMatrix);
		m_transformCenter.setLocation(center);

		//create the combined  (T^-1) * Rx * (T) matrix
		m_TransformMatrix.concatenate(m_T_Matrix);
		m_inv_T_Matrix.concatenate(m_TransformMatrix);

		// shear selection
		Enumeration e = m_Vertices.elements();
		while (e.hasMoreElements())
		{
			Vertex v = (Vertex) e.nextElement();
			Point2D position = v.getPosition();

			m_inv_T_Matrix.transform(position, position);
			v.moveTo(position);
		}

		/* don't forget the rotation center, as it's also 
		 * a point inside the set!
		 */

		m_inv_T_Matrix.transform(m_rotationCenter, m_rotationCenter);
		m_Scene.setChangeFlag();
	}


	//*************************************************************************

	protected void uniformShearSelection(
			Point2 source,
			Point2 target)
	{
		Point2 center = new Point2();
		double xshear = 0.0, yshear = 0.0;

		center.setLocation(m_selectorBox.getCenter());

		// calculate shearing coefficients
		switch (m_iTransformFlag)
		{

			// horizontal shear
			case N_SHEAR:

				yshear = 0.0;
				xshear = (target.x - source.x)
						/ (0.5 * m_selectorBox.getWidth());
				break;
			case S_SHEAR:

				yshear = 0.0;
				xshear = -(target.x - source.x)
						/ (0.5 * m_selectorBox.getWidth());
				break;

			// vertical shear
			case W_SHEAR:

				xshear = 0.0;
				yshear = -(target.y - source.y)
						/ (0.5 * m_selectorBox.getHeight());
				break;

			case E_SHEAR:
				xshear = 0.0;
				yshear = (target.y - source.y)
						/ (0.5 * m_selectorBox.getHeight());
				break;
		}

		//initialize transform components
		m_T_Matrix.setToTranslation(-center.x, -center.y);
		m_inv_T_Matrix.setToTranslation(center.x, center.y);
		m_TransformMatrix.setToShear(xshear, yshear);

		//update the accumulated transform
		m_accumulatedTransform.concatenate(m_TransformMatrix);
		m_transformCenter.setLocation(center);

		//create the combined  (T^-1) * Rx * (T) matrix
		m_TransformMatrix.concatenate(m_T_Matrix);
		m_inv_T_Matrix.concatenate(m_TransformMatrix);

		// shear selection
		Enumeration e = m_Vertices.elements();
		while (e.hasMoreElements())
		{
			Vertex v = (Vertex) e.nextElement();
			Point2D position = v.getPosition();

			m_inv_T_Matrix.transform(position, position);

			v.moveTo(position);
		}

		/* don't forget the rotation center, as it's also 
		 * a point inside the set!
		 */

		m_inv_T_Matrix.transform(m_rotationCenter, m_rotationCenter);
		m_Scene.setChangeFlag();
	}


	//*************************************************************************

	/*
	 * source - previous mouse position in world coordinates
	 * target - new mouse position in world coordinates
	 * 
	 */
	protected void rotateSelection(
			Point2 source,
			Point2 target)
	{
		//convert mouse positions to trig values

		// direction vectors from center of rotation to mouse positions
		Point2 dir1 = new Point2(source.x - m_rotationCenter.x, source.y
				- m_rotationCenter.y);

		Point2 dir2 = new Point2(target.x - m_rotationCenter.x, target.y
				- m_rotationCenter.y);

		// normalize vectors
		double length = Math.sqrt(dir1.x * dir1.x + dir1.y * dir1.y);
		dir1.x /= length;
		dir1.y /= length;

		length = Math.sqrt(dir2.x * dir2.x + dir2.y * dir2.y);
		dir2.x /= length;
		dir2.y /= length;

		/* dir1's and dir2's coordinates now contain the 
		 * corresponding cos and sin values for previous and 
		 * current angle and can be used directly to construct
		 * the transformation matrix. 
		 * For rotations the following holds:
		 * 
		 * R(phi) = [ cos(phi) -sin(phi)
		 * 	      sin(phi)  cos(phi) ]
		 * 
		 * phi = beta-alpha in this case, so the below
		 * values are used to construct the terms with
		 * help of basic trigonometric equations. 
		 */

		// some helper variables for mathematical clarity
		double cos_alpha = dir1.x;
		double sin_alpha = dir1.y;

		double cos_beta = dir2.x;
		double sin_beta = dir2.y;

		// Memo: AffineTransform constructors etc. are column-major !!!!!

		m_TransformMatrix.setTransform(

		cos_beta * cos_alpha + sin_beta * sin_alpha, // cos(beta-alpha)	
				sin_beta * cos_alpha - cos_beta * sin_alpha, // sin(beta-alpha)
				-(sin_beta * cos_alpha - cos_beta * sin_alpha), //-sin(beta-alpha) 
				cos_beta * cos_alpha + sin_beta * sin_alpha, // cos(beta-alpha)
				0, 0);

		m_T_Matrix.setToTranslation(-m_rotationCenter.x, -m_rotationCenter.y);

		m_inv_T_Matrix.setToTranslation(m_rotationCenter.x, m_rotationCenter.y);

		// update the accumulated transform
		m_accumulatedTransform.concatenate(m_TransformMatrix);
		m_transformCenter.setLocation(m_rotationCenter);

		// make the (T^-1) * Rx * (T) chain

		m_TransformMatrix.concatenate(m_T_Matrix);
		m_inv_T_Matrix.concatenate(m_TransformMatrix);

		//_testShape = Tminus1.createTransformedShape(_testShape);

		Enumeration e = m_Vertices.elements();
		while (e.hasMoreElements())
		{
			Vertex v = (Vertex) e.nextElement();

			Point2D position = v.getPosition();

			m_inv_T_Matrix.transform(position, position);

			v.moveTo(position);
		}

		m_Scene.setChangeFlag();
	}


	//*************************************************************************

	protected void translateSelection(
			Point2 source,
			Point2 target)
	{
		Point2 delta = new Point2(target.x - source.x, target.y - source.y);

		// translate all selected objects    	
		Vertex v;

		for (int i = 0; i < m_Vertices.size(); i++)
		{
			v = (Vertex) m_Vertices.get(i);
			v.translate(delta);
		}

		// update rotation center and marker
		m_rotationCenter.translate(delta);
		m_rotCenterMarker.translate(delta);

		/* Modify the transformation matrices. They're
		 * actually not used in this method, but the 
		 * combined transformation matrix for undo 
		 * operations will be built from them later,
		 * so appropriate values need to be set. 
		 */
		//m_T_Matrix.setToIdentity(); 
		//m_inv_T_Matrix.setToIdentity();
		m_TransformMatrix.setToTranslation(delta.x, delta.y);

		// update accumulated transform
		m_accumulatedTransform.concatenate(m_TransformMatrix);
		m_transformCenter.setLocation(0.0, 0.0);

		m_Scene.setChangeFlag();

	}


	//*************************************************************************

	/* used primarily by methods that add/remove vertices
	 * to selection
	 */
	protected void updateRotationCenter()
	{
		// update rotation center 
		m_rotationCenter.setLocation(m_selectorBox.getCenter());

		//update rotation center marker rectangle
		double size = MARKER_SIZE / m_fPixelSize;

		m_rotCenterMarker.setRect(m_rotationCenter.x - size, m_rotationCenter.y
				- size, 2 * size, 2 * size);

	}


	//*************************************************************************

	/* Update the bounding rectangle of the selection
	 * set after changes to its contents
	 */
	protected void updateSelectionBBox()
	{
		//empty previous rectangle
		m_selectorBox.setRect(0, 0, 0, 0);

		if (m_bVertexMode)
		{
			/* start with the first object in the list.
			 * This is necesaary for the bbox to be
			 * set up correctly (otherwise one corner/side
			 * will be stuck to the origin).
			 */

			Vertex v = (Vertex) m_Vertices.get(0);

			m_selectorBox.setRect(v.getBoundingBox());

			// build new selecting bounding box
			for (int i = 1; i < m_Vertices.size(); i++)
			{
				v = (Vertex) m_Vertices.get(i);

				m_selectorBox.add(v.getBoundingBox());
			}
		}
		else
		{
			Edge e = (Edge) m_Edges.get(0);
			m_selectorBox.setRect(e.getBoundingBox());

			for (int i = 1; i < m_Edges.size(); i++)
			{
				e = (Edge) m_Edges.get(i);
				m_selectorBox.add(e.getBoundingBox());
			}
		}
	}


	//*************************************************************************

	protected void setupScaleMarkers()
	{
		/* Position scale markers around the selection box
		 * The markers are positioned clockwise, starting
		 * with the marker in the upper-left corner of the
		 * selection box.
		 * 
		 */

		double size = MARKER_SIZE / m_fPixelSize;
		double offset = MARKER_OFFSET / m_fPixelSize;
		double nudge = 0; //= 1 / m_fPixelSize;

		double xmin, ymin, xmax, ymax, xmid, ymid;

		xmin = m_selectorBox.getMinX();
		ymin = m_selectorBox.getMinY();
		xmax = m_selectorBox.getMaxX();
		ymax = m_selectorBox.getMaxY();

		xmid = (xmin + xmax) / 2.0;
		ymid = (ymin + ymax) / 2.0;

		//upper-left marker
		m_Markers[0].setRect(xmin - size - offset, ymax + offset, size, size);

		// top mid marker
		m_Markers[1]
				.setRect(xmid - size / 2, ymax + offset + nudge, size, size);

		// upper-right marker
		m_Markers[2].setRect(xmax + offset, ymax + offset, size, size);

		// right mid marker
		m_Markers[3]
				.setRect(xmax + offset + nudge, ymid - size / 2, size, size);

		// lower-right marker
		m_Markers[4].setRect(xmax + offset, ymin - size - offset, size, size);

		// bottom mid marker
		m_Markers[5].setRect(xmid - size / 2, ymin - size - offset - nudge,
				size, size);

		// lower-left marker
		m_Markers[6].setRect(xmin - size - offset, ymin - size - offset, size,
				size);

		// left mid marker
		m_Markers[7].setRect(xmin - size - offset - nudge, ymid - size / 2,
				size, size);

		// update the bounding box
		m_BoundingBox.setRect(m_selectorBox);

		for (int i = 0; i < 8; i++)
			m_BoundingBox.add(m_Markers[i]);
	}


	//*************************************************************************

	protected void setupRotationShearMarkers()
	{
		//FIXME: Magic numbers could be a problem later...

		/* I.e. the entire code works well with 24x24 pixel
		 * icons, but I haven't tested it with any other
		 * icon sizes...
		 */

		double offset = MARKER_OFFSET / m_fPixelSize;
		double xmin, ymin, xmax, ymax, xmid, ymid;

		// all icon widths / heights are equal
		double icon_size = m_RotationShearIcons[0].getWidth(null)
				/ m_fPixelSize;

		xmin = m_selectorBox.getMinX();
		ymin = m_selectorBox.getMinY();
		xmax = m_selectorBox.getMaxX();
		ymax = m_selectorBox.getMaxY();

		xmid = (xmin + xmax) / 2.0;
		ymid = (ymin + ymax) / 2.0;

		//upper-left marker
		m_IconPositions[0].setLocation(xmin - icon_size / 2 - offset, ymax
				+ icon_size / 2 + offset);

		// top mid marker
		m_IconPositions[1].setLocation(xmid - icon_size / 2, ymax + icon_size
				* 0.75);

		// upper-right marker
		m_IconPositions[2].setLocation(xmax - icon_size / 2 + offset, ymax
				+ icon_size / 2 + offset);

		// right mid marker
		m_IconPositions[3].setLocation(xmax - icon_size * 0.25, ymid
				+ icon_size / 2);

		// lower-right marker
		m_IconPositions[4].setLocation(xmax - icon_size / 2 + offset, ymin
				+ icon_size / 2 - offset);

		// bottom mid marker
		m_IconPositions[5].setLocation(xmid - icon_size / 2, ymin + icon_size
				* 0.25);

		// lower-left marker
		m_IconPositions[6].setLocation(xmin - icon_size / 2 - offset, ymin
				+ icon_size / 2 - offset);

		// left mid marker
		m_IconPositions[7].setLocation(xmin - icon_size * 0.75, ymid
				+ icon_size / 2);

		// update rotation center marker rectangle
		double size = MARKER_SIZE / m_fPixelSize;
		m_rotCenterMarker.setRect(m_rotationCenter.x - size, m_rotationCenter.y
				- size, 2 * size, 2 * size);
	}


	//*************************************************************************
	// 			      Private instance methods
	//*************************************************************************

	private void initCustomCursors()
	{
		// load custom cursors
		m_customCursors = new Cursor[CURSOR_NAMES.length];

		URL url;
		Image icon;
		Toolkit kit = m_Hub.getParent().getToolkit();

		for (int i = 0; i < CURSOR_NAMES.length; i++)
		{
			url = getClass().getResource(CURSOR_NAMES[i]);

			if (url == null)
			{
				// we may be in a JAR file, so try to load
				// the resourse differently
				StringBuffer buffer = new StringBuffer("/anja/SwingFramework/");

				buffer.append(CURSOR_NAMES[i].substring(3));
				//System.out.println(buffer.toString());

				url = getClass().getResource(buffer.toString());
			}

			if (url != null)
			{
				icon = kit.getImage(url);

				m_customCursors[i] = kit.createCustomCursor(icon, new Point(16,
						16), CURSOR_NAMES[i]);
			}
			else
			{
				// dummy
				System.err.println("Could not load " + CURSOR_NAMES[i]);
				m_customCursors[i] = Cursor.getDefaultCursor();
			}
		}
	}


	//*************************************************************************

	private void initIcons()
	{
		// load rotation / shear mode icons	
		m_RotationShearIcons = new Image[ICON_NAMES.length];

		URL url;
		Toolkit kit = m_Hub.getParent().getToolkit();

		for (int i = 0; i < ICON_NAMES.length; i++)
		{
			url = getClass().getResource(ICON_NAMES[i]);

			if (url == null)
			{
				// we may be in a JAR file, so try to load
				// the resourse differently
				StringBuffer buffer = new StringBuffer("/anja/SwingFramework/");

				buffer.append(ICON_NAMES[i].substring(3));
				//System.out.println(buffer.toString());

				url = getClass().getResource(buffer.toString());
			}

			if (url != null)
			{
				m_RotationShearIcons[i] = kit.getImage(url);
			}
			else
			{
				// dummy
				System.err.println("Could not load " + ICON_NAMES[i]);

				m_RotationShearIcons[i] = new BufferedImage(1, 1,
						BufferedImage.TYPE_INT_ARGB);
			}
		}// for()			
	}

	//*************************************************************************
}
