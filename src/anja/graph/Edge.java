/* File: Edge.java
 * Created on: 01.09.2005 by birgit
 * 
 */

package anja.graph;


import java.awt.*;
import java.awt.geom.*;

import anja.geom.*;
import anja.util.GraphicsContext;
import static anja.util.MathConstants.*;

import org.jdom.*;


//TODO: Check comments...

/**
 * This class implements a drawable edge in the plane or in space.
 * 
 * @author Birgit Engels [engels@cs.uni-bonn.de]
 * @author Jörg Wegener
 * 
 * 
 *      <br> TODO: Complete basic documentation<br> TODO: Write additional
 *      documentation<br> TODO: XML I/O routines<br> TODO: check outcode method
 *      logic, it's probably buggy!
 *      
 * @see Graph
 * @see Edge
 */

abstract public class Edge<V extends Vertex>
		implements java.io.Serializable
		//implements Cloneable
{

	//*************************************************************************
	//                           Public Constants
	//*************************************************************************

	// outcodes for compareVertices()
	public static final int				NO_COMMON_VERTEX		= 0x00000000;
	public static final int				SAME_SOURCE				= 0x01;
	public static final int				SOURCE_TARGET			= SAME_SOURCE << 1;
	public static final int				TARGET_SOURCE			= SOURCE_TARGET << 1;
	public static final int				SAME_TARGET				= TARGET_SOURCE << 1;
	public static final int				SAME_EDGE				= SAME_TARGET << 1;
	public static final int				SAME_SRC_AND_TARGET		= SAME_EDGE << 1;
	public static final int				LOOP					= SAME_SRC_AND_TARGET << 1;

	//*************************************************************************
	//                           Private Constants
	//*************************************************************************

	// Used for generating unique ids for every instance of Edge
	//private static int _ID_GEN;

	// Default colors
	private static final Color			_DEFAULT_EDGE_COLOR		= Color.BLACK;
	//private static final Color _DEFAULT_EMPHASIS_COLOR  = Color.RED;
	private static final Color			_DEFAULT_TEXT_COLOR		= Color.BLACK;

	// Default values for visual parameters
	private static final float			_DEFAULT_LINE_WIDTH		= 1.0f;
	private static final float			_DEFAULT_EMPHASIS_WIDTH	= 2.0f;

	// line width scaling factor for selected edges
	private static final float			_SELECTION_SCALE_FACTOR	= 2.0f;

	// default width of the 'epsilon-band' around the edge, in pixels 
	private static final int			_CLICK_TOLERANCE		= 3;

	// directed edge arrow parameters (in pixels)
	private static final int			_ARROW_LENGTH			= 22;
	private static final int			_ARROW_HALF_WIDTH		= 6;
	private static final int			_ARROW_TAIL_INDENT		= 7;

	// arrow line drawing attributes
	private static final BasicStroke	_ARROW_STROKE;

	//------------------------ XML I/O constants ------------------------------

	// this one is package-visible because Graph.java needs to access it
	static final String					_XML_EDGE_NAME			= "Edge";

	// ------------------------ Attribute names -------------------------------

	static final String					_XML_SOURCE_VERTEX		= "source";
	static final String					_XML_TARGET_VERTEX		= "target";

	private static final String			_XML_LABEL				= "label";

	private static final String			_XML_EDGE_COLOR			= "edge_color";
	private static final String			_XML_LABEL_COLOR		= "label_color";

	private static final String			_XML_WEIGHT				= "weight";
	private static final String			_XML_WEIGHTED			= "weighted";

	private static final String			_XML_DIRECTED			= "directed";

	/* TODO: edge width, emphasis context elements etc., multi-edge flag...
	 * 
	 */

	//*************************************************************************
	//                          Protected variables
	//*************************************************************************


	//*************************************************************************
	//                           Private Variables
	//*************************************************************************

	/*
	 * origin and target vertex, indicating the direction of this
	 * edge, if this edge is directed ( see '_directed' )
	 */
	protected V							_source;
	protected V							_target;

	protected int						_id;												// unique ID
	protected String					_label					= null;					// label of the edge

	protected double					_length					= 0;						// length of the edge  
	protected double					_weight					= 0;						// weight of the edge

	/* flags with specified meaning */

	// multi-edge flag
	protected boolean					_isMultiEdge			= false;

	// 'directed-flag' : TRUE, if this edge is directed.
	protected boolean					_directed				= false;

	// 'weighted-flag' : TRUE, if this edge is weighted.
	protected boolean					_weighted				= false;

	/* flags with user defined meaning */
	protected boolean					_marked					= false;
	protected boolean					_known					= false;
	protected boolean					_visited				= false;

	// selection flag for visual editing purposes
	protected boolean					_isSelected				= false;

	// reference to any attached object
	protected Object					_reference				= null;

	//flag if this edge is a sling, so it doesn't have to calculated that often
	protected boolean					_isSling				= false;

	//---------- visual attributes / helper objects for rendering -------------

	protected double					_pixelSize;										// current pixel scaling     
	protected Color						_textColor;

	protected boolean					_drawEmphasized;									// used to emphasize an edge

	/*
	 * This section contains visual attributes etc. used by the
	 * compatibility rendering routine and point containment /
	 * proximity queries . They'll be removed when we have time to
	 * write new versions of those.
	 */

	//-------------------------------------------------------------------------

	// geometry primitives used for rendering etc.

	protected Arc2D						_slingArc;

	protected Rectangle2D				_boundingBox;
	protected Point2					_tempPoint;										// used for various purposes

	protected GeneralPath				_arrowShape;

	// associated graphic attributes
	protected GraphicsContext			_edgeGC;
	protected GraphicsContext			_emphasisGC;

	//-------------------------------------------------------------------------

	//--------------------------- static init ---------------------------------

	static
	{
		//_ID_GEN = 0;

		// initialize constant arrow drawing parameters
		_ARROW_STROKE = new BasicStroke(0.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f);
	}


	//*************************************************************************
	//                            Constructors
	//*************************************************************************

	/**
	 * Constructor for Edge
	 * 
	 * @param source
	 *            origin vertex of the edge.
	 * @param target
	 *            target vertex of the edge.
	 **/

	protected Edge(
			V source,
			V target)
	{
		// base constructor

		_source = source;
		_target = target;

		_length = _source.distance(_target);
		//_id         = _ID_GEN++;

		_label = null;

		// default visual parameters

		//------------------ Compatibility code -----------------------------
		_initContexts();
		_initGeometry();
		_checkSling();

		//----------------------- New code ----------------------------------

		_textColor = _DEFAULT_TEXT_COLOR;
	}


	//*************************************************************************
	//                             Class methods
	//*************************************************************************

	//*************************************************************************
	//                        Package-visible methods
	//*************************************************************************


	//*************************************************************************
	//                           Public Methods
	//*************************************************************************

	//------------------------- Getters / Setters -----------------------------

    /**
     * Assigns an ID to the edge.
     * Called by the graph, the graph itself is responsible for assigning unique ids.
     * Should not be called manually outside of an addEdge-Operation.
     * @param id The ID to assign.
     */
    public void setID(int id) {
    	_id = id;
    }
    
    /**
     * Returns the ID of the edge.
     */
    public int getID() {
    	return _id;
    }


	//*************************************************************************

	/**
	 * Returns the label of an edge.
	 * 
	 * @return The label
	 **/

	public String getLabel()
	{
		return _label;
	}


	//*************************************************************************

	/**
	 * Sets the label of an edge.
	 * 
	 * @param s
	 *            The new label
	 **/

	public void setLabel(
			String s)
	{
		_label = s;
	}


	//*************************************************************************

	/**
	 * Resets the label to NULL
	 **/

	public void resetLabel()
	{
		_label = null;
	}


	//*************************************************************************

	/**
	 * Returns the origin vertex
	 * 
	 * @return The origin of the edge
	 **/

	public V getSource()
	{
		return _source;
	}


	//*************************************************************************

	/**
	 * Sets the origin vertex.
	 * 
	 * @param source
	 *            Origin vertex
	 **/

	protected void setSource(
			V source)
	{
		_source = (V)source;
		_length = _source.distance(_target);
		_checkSling();
	}


	//*************************************************************************

	/**
	 * Returns the target vertex
	 * 
	 * @return The target of the edge
	 **/

	public V getTarget()
	{
		return _target;
	}


	//*************************************************************************

	/**
	 * Sets the target vertex
	 * 
	 * @param target
	 *            Target vertex
	 **/

	protected void setTarget(
			V target)
	{
		_target = (V)target;
		_length = _source.distance(_target);
		_checkSling();
	}


	//*************************************************************************

	/**
	 * Returns the stored object reference.
	 * 
	 * @return The reference object
	 **/

	public Object getReferenceObject()
	{
		return _reference;
	}


	//*************************************************************************

	/**
	 * Sets the internal object reference to objRef.
	 * 
	 * @param objRef
	 *            Object to which _reference is set
	 **/

	public void setReferenceObject(
			Object objRef)
	{
		_reference = objRef;
	}


	//*************************************************************************

	/**
	 * Sets the 'directed-flag' of this edge to dir
	 * 
	 * @param dir
	 *            Indicates if this edge is directed from _origin to _target (
	 *            dir = TRUE ) - or not ( dir = FALSE ).
	 **/

	public void setDirected(
			boolean dir)
	{
		_directed = dir;
	}


	//*************************************************************************

	/**
	 * Returns the value of this edge
	 * 
	 * @return The weight of this edge
	 **/

	public double getWeight()
	{
		return _weight;
	}


	//*************************************************************************

	/**
	 * Sets the weight of the edge and adjusts the 'weighted-flag' to TRUE (
	 * edge IS weighted )
	 * 
	 * @param weight
	 *            Desired value of this edges weight
	 **/

	public void setWeight(
			double weight)
	{
		_weight = weight;
		_weighted = true;
	}


	//*************************************************************************

	/**
	 * Resets the '_weight' value to zero and adjusts the '_weighted'-flag to
	 * FALSE ( edge is NOT weighted )
	 **/

	public void resetWeight()
	{
		_weight = 0;
		_weighted = false;
	}


	//*************************************************************************

	/**
	 * Sets the flag '_marked' to TRUE
	 **/

	public void mark()
	{
		_marked = true;
	}


	//*************************************************************************

	/**
	 * Sets the flag '_marked' to FALSE
	 **/

	public void unmark()
	{
		_marked = false;
	}


	//*************************************************************************

	/**
	 * Sets the flag '_known' to TRUE
	 **/

	public void known()
	{
		_known = true;
	}


	//*************************************************************************

	/**
	 * Sets the flag '_known' to FALSE
	 **/

	public void unknown()
	{
		_known = false;
	}// unmark


	//*************************************************************************

	/**
	 * Sets the flag '_visited' to TRUE
	 **/

	public void visited()
	{
		_visited = true;
	}


	//*************************************************************************

	/**
	 * Sets the flag '_visited' to FALSE
	 **/

	public void unvisited()
	{
		_visited = false;
	}


	//*************************************************************************

	/**
	 * #@deprecated
	 */
	public void select()
	{
		_isSelected = true;
	}


	//*************************************************************************

	/**
	 * #@deprecated
	 */
	public void deselect()
	{
		_isSelected = false;
	}


	//*************************************************************************

	/**
	 * Sets / resets the selection status of an edge.
	 * 
	 * @param on
	 *            use <b>true</b> for 'selected', <b>false</b> for 'not
	 *            selected'.
	 */
	public void setSelected(
			boolean on)
	{
		_isSelected = on;
	}


	//*************************************************************************

	/**
	 * Sets the _drawEmphasized variable to true.
	 */
	public void emphasize()
	{
		_drawEmphasized = true;
	}


	//*************************************************************************

	/**
	 * Sets the _drawEmphasized variable to false.
	 */
	public void deEmphasize()
	{
		_drawEmphasized = false;
	}


	//*************************************************************************

	/**
	 * Sets the value of the _drawEmphasized variable.
	 * 
	 * @param emphasized
	 *            The new value
	 */
	public void setEmphasized(
			boolean emphasized)
	{
		_drawEmphasized = emphasized;
	}


	// ------------- Getters / setters for visual attributes ------------------

	/**
	 * Returns the color of an edge.
	 * 
	 * @return The color
	 */
	public Color getColor()
	{
		return _edgeGC.getOutlineColor();
	}


	//*************************************************************************

	/**
	 * Sets the new color
	 * 
	 * @param color
	 *            The new color
	 */
	public void setColor(
			Color color)
	{
		if (color != null)
		{
			_edgeGC.setOutlineColor(color);
			_emphasisGC.setOutlineColor(color);
		}
	}


	//************************************************************************* 

	/**
	 * Sets label color.
	 * 
	 * @param color
	 *            The new label color
	 */
	public void setTextColor(
			Color color)
	{
		if (color != null)
			_textColor = color;
	}


	//*************************************************************************

	/**
	 * Set emphasis parameters. Emphasized edges are drawn using a possibly
	 * different color and different java.awt.BasicStroke parameters. <br>Both
	 * can be specified as arguments for this method. If either is set to
	 * <code><b>null</b></code>, the previous values are not altered and are
	 * used further. <br> By default an emphasized edge is drawn using it's own
	 * color and stroke parameters, and a width of 1.5 pixels.
	 * 
	 * @param color
	 *            new emphasis color
	 * @param stroke
	 *            new emphasis BasicStroke object
	 */
	public void setEmphasis(
			Color color,
			BasicStroke stroke)
	{
		if (color != null)
		{
			_emphasisGC.setOutlineColor(color);
		}

		if (stroke != null)
		{
			_emphasisGC.setStroke(stroke);
		}
	}


	//*************************************************************************

	/**
	 * Does one really need this method ???? TODO: the copy() method in
	 * GraphicsContext might not clone the objects correctly!
	 * 
	 * @return The emphasis gc
	 */
	public GraphicsContext getEmphasis()
	{
		return _emphasisGC.copy();
	}


	// ------------- Connectivity information access methods ------------------

	/**
	 * TODO: Write docs for all these methods...
	 * 
	 * @return The left face
	 */
	abstract public Face getLeftFace();


	//*************************************************************************

	/**
	 * TODO: Write docs for all these methods...
	 *  
	 * @return The right face
	 */
	abstract public Face getRightFace();


	//*************************************************************************

	/**
	 * TODO: Write docs for all these methods...
	 *  
	 * @return The next cw edge
	 */
	abstract public Edge getNextEdgeCW();



	//*************************************************************************

	/**
	 * TODO: Write docs for all these methods...
	 *  
	 * @return The previous cw edge
	 */
	abstract public Edge getPrevEdgeCW();


	//*************************************************************************

	/**
	 * TODO: Write docs for all these methods...
	 *  
	 * @return The next ccw edge
	 */
	abstract public Edge getNextEdgeCCW();


	//*************************************************************************

	/**
	 * TODO: Write docs for all these methods...
	 *  
	 * @return The previous ccw edge
	 */
	abstract public Edge getPrevEdgeCCW();


	//*************************************************************************

	/**
	 * Returns this edge's bounding box in world coordinates
	 * 
	 * @return The bounding box
	 */
	public Rectangle2D getBoundingBox()
	{
		// ------------------------ Legacy code ------------------

		return _getBBox();

		// -------------------------------------------------------
	}


	//*************************************************************************

	//-------------------------- Query methods --------------------------------

	/**
	 * Generates and returns an 'out code' by compare an edge's vertices with a
	 * second one.
	 * 
	 * <br> For a list of possible outcode, look at the list of static public
	 * constants at the beginning of the class doc. </p>
	 * 
	 * @param second
	 *            the second edge for testing.
	 * 
	 * @return The outcode
	 * 
	 *         TODO: complete logic [additional outcodes...]
	 */
	public int compareVertices(
			Edge second)
	{
		int outcode = NO_COMMON_VERTEX;

		if (this == second)
		{
			outcode = SAME_EDGE;
		}

		else if (_source == second._source)
		{
			// both edges have the same source vertex
			outcode = SAME_SOURCE;
		}

		else if (_source == second._target)
		{
			// this edge's source is target for the second
			outcode = SOURCE_TARGET;
		}

		else if (_target == second._source)
		{
			// this edge's target is source for the second
			outcode = TARGET_SOURCE;
		}

		else if (_target == second._target)
		{
			// both edges have the same target vertex
			outcode = SAME_TARGET;
		}

		return outcode;
	}


	//*************************************************************************

	/**
	 * Is the edge currently selected?
	 * 
	 * @return true, if the edge is selected, false else
	 */
	public boolean isSelected()
	{
		return _isSelected;
	}


	//*************************************************************************

	/**
	 * Returns the 'directed-flag' of this edge
	 * 
	 * @return true, if the edge is directed, false else
	 **/
	public boolean isDirected()
	{
		return _directed;
	}


	//*************************************************************************

	/**
	 * Returns TRUE, if this edge is weighted
	 * 
	 * @return true, if the edge is weighted, false else
	 **/

	public boolean isWeighted()
	{
		return _weighted;
	}


	//*************************************************************************

	/**
	 * Returns TRUE, iff the '_marked'-flag is set
	 * 
	 * @return true, if the edge is marked, false else
	 **/

	public boolean isMarked()
	{
		return _marked;
	}


	//*************************************************************************

	/**
	 * Returns TRUE, if the '_known'-flag is set
	 * 
	 * @return true, if the edge is known, false else
	 **/

	public boolean isKnown()
	{
		return _known;
	}


	//*************************************************************************

	/**
	 * Returns TRUE, if the '_visited'-flag is set
	 * 
	 * @return true, if the edge has been visited, false else
	 **/

	public boolean isVisited()
	{
		return _visited;
	}


	//*************************************************************************

	/**
	 * Returns <code><b>true</b></code>, if origin and target vertices of an
	 * edge are identical (i.e. the edge is a sling)
	 * 
	 * @return true, if the edge is a sling, false else
	 **/

	public boolean isSling()
	{
		return _isSling;
	}


	//*************************************************************************

	/**
	 * Returns <code><b>true</b></code>, if this edge has the same ID as the
	 * edge e.
	 * 
	 * @param e
	 *            The edge to compare
	 * 
	 * @return true, if this edge equals e, false else
	 */

	public boolean equals(
			Edge e)
	{
		if (e == null)
			return false;
		else
			return (this._id == e.getID());
	}


	//*************************************************************************

	/**
	 * Returns <code><b>true</b></code>, if this edge has the same start/end
	 * vertex positions as the edge e
	 * 
	 * @param e
	 *            The edge to compare
	 * 
	 * @return true, if this edge and e have the same position, false else
	 **/

	public boolean samePosition(
			Edge e)
	{
		if (e == null)
			return false;
		else
			return (((this._source.samePosition(e.getSource())) && (this._target
					.samePosition(e.getTarget()))) || ((this._source
					.samePosition(e.getTarget())) && (this._target
					.samePosition(e.getSource()))));
	}


	//*************************************************************************

	/**
	 * Returns <code><b>true</b></code>, if this edge has the same vertices as
	 * the edge e
	 * 
	 * @param e
	 *            The edge to compare
	 * 
	 * @return true, if this edge and e have the same vertices, false else
	 **/

	public boolean sameVertices(
			Edge e)
	{
		if (e == null)
			return false;
		else
			return (((this._source.equals(e.getSource())) && (this._target
					.equals(e.getTarget()))) || ((this._source.equals(e
					.getTarget())) && (this._target.equals(e.getSource()))));
	}


	//*************************************************************************

	/**
	 * Returns <code><b>true</b></code>, if this edge has the same direction as
	 * the edge <code>e</code><br> (which means that it has the same origin and
	 * target vertices as the edge <code>e</code>, in the same order)
	 * 
	 * FIXME: this semantic might not make sense....
	 * 
	 * @param e
	 *            The edge to compare
	 * 
	 * @return true, if this edge and e have the same direction, false else
	 **/

	public boolean sameDirection(
			Edge e)
	{
		if (e == null)
			return false;
		else
			return ((this._source.equals(e.getSource())) && (this._target
					.equals(e.getTarget())));
	}


	//*************************************************************************

	/**
	 * Returns <code><b>true</b></code>, if this edge has all the attributes of
	 * the edge e
	 * 
	 * @param e
	 *            The edge to compare
	 * 
	 * @return true, if this edge and e have the same attributes, false else
	 **/

	public boolean sameAttributes(
			Edge e)
	{
		if (e == null)
		{
			return false;
		}
		else
		{
			// Auskommentierte Zeilen vergleichen die edges weitergehend 
			// ( weight, direction, usw. ) - sinnvoll ?

			if (_weighted != e.isWeighted())
			{
				return false;
			}
			else if (_weight != e.getWeight())
			{
				return false;
			}
			else if (!_reference.equals(e.getReferenceObject()))
			{
				return false;
			}
			else
			{
				if (_directed && e.isDirected())
				{
					return (_source.equals(e.getSource()) && _target.equals(e
							.getTarget()));
				}
				else if (!_directed && !e.isDirected())
				{
					return ((_source.equals(e.getSource()) && _target.equals(e
							.getTarget()))

					||

					(_target.equals(e.getSource()) && _source.equals(e
							.getTarget())));
				}
				else
					return false;
			}
		}
	}


	//*************************************************************************

	/**
	 * Checks whether an edge 'contains' a point, i.e. whether the point in
	 * question lies within a predefined epsilon band around the line segment
	 * representing the edge (or, if the edge is a 'sling', the epsilon band
	 * around the arc segment representing a sling). (Atm if the edge is a
	 * sling, it checks wheter the point lies in the edge's bounding box)
	 * 
	 * @param x
	 *            x coordinate of the point
	 * @param y
	 *            y coordinate of the point
	 * 
	 * @return true, if the point lies in an band around this edge, false else
	 */
	public boolean containsPoint(
			double x,
			double y)
	{
		//------------------ Legacy code -------------------------

		return _containsPoint(x, y);

		// -------------------------------------------------------
	}


	//*************************************************************************

	/**
	 * (NOT IMPLEMENTED)
	 * 
	 * @param x
	 *            First point
	 * @param y
	 *            Second point
	 * @param z
	 *            Third point
	 * 
	 * @return Result of the 3-d point containment test
	 */
	public boolean containsPoint(
			double x,
			double y,
			double z)
	{
		//TODO: implement 3-d point containment test
		return false;
	}


	//*************************************************************************

	//wrappers

	/**
	 * Wrapper for containsPoint(x,y), takes a Point2D object instead of
	 * individual point coordinates.
	 * 
	 * @param point
	 *            The point
	 * 
	 * @return {@link #containsPoint(double, double)}
	 * 
	 */
	public boolean containsPoint(
			Point2D point)
	{
		return containsPoint(point.getX(), point.getY());
	}


	//*************************************************************************
	//                     Arithmetic and Geometric Methods
	//*************************************************************************

	/**
	 * Returns the length of an edge.
	 * 
	 * @return The length
	 **/
	public double getLength()
	{
		_length = _source.distance(_target);
		return _length;
	}


	//*************************************************************************

	/**
	 * Returns the midpoint of an edge.
	 * 
	 * @return The midpoint of the edge
	 */
	public Point2D getMidPoint()
	{
		Point2D midpoint = new Point2D.Double();

		midpoint.setLocation(_source.getX() + (_target.getX() - _source.getX())
				/ 2.0, _source.getY() + (_target.getY() - _source.getY()) / 2.0);

		return midpoint;
	}


	//*************************************************************************

	/**
	 * Linearly interpolates a point on the line segment between origin and
	 * target vertices, the relative position on the line being specified by the
	 * <b>distance</b> parameter.
	 * 
	 * 
	 * @param point
	 *            a Point2D object that will receive the resulting point
	 *            coordinates. This object <b>must</b> have been previously
	 *            allocated with <b>new</b>, otherwise a NullPointerException
	 *            will occur!
	 * 
	 * 
	 * @param distance
	 *            , a fraction of the length of the edge, must be between 0 and
	 *            1, and will be clamped to this range if it is found to lie
	 *            outside it!
	 * 
	 */
	public void interpolatePoint(
			Point2D point,
			double distance)
	{
		// clamp distance to allowed value range
		if (distance < 0.0)
		{
			distance = 0.0;
		}
		else if (distance > 1.0)
		{
			distance = 1.0;
		}

		point.setLocation(
				_source.getX() + distance * (_target.getX() - _source.getX()),
				_source.getY() + distance * (_target.getY() - _source.getY()));

	}


	//*************************************************************************

	/**
	 * Wrapper for interpolatePoint(point, distance). This one <b>does</b>
	 * allocate a new Point2D object and returns it!
	 * 
	 * @param distance
	 * 
	 * @return {@link #interpolatePoint(java.awt.geom.Point2D, double)}
	 */
	public Point2D interpolatePoint(
			double distance)
	{
		Point2D point = new Point2D.Double();
		interpolatePoint(point, distance);

		return point;
	}


	//*************************************************************************

	/**
	 * Returns the angle this edge makes with <b>target</b>, in CCW direction.
	 * The angle returned is in the range [ 0, &pi ] radians
	 * 
	 * @param target
	 *            The target edge
	 * 
	 * @return The angle
	 */
	public double angle(
			Edge target)
	{
		//TODO: check conditions & optimize math

		double length = getLength();
		double x1 = (_target.getX() - _source.getX()) / length;
		double y1 = (_target.getY() - _source.getY()) / length;

		length = target.getLength();

		double x2 = (target.getTarget().getX() - target.getSource().getX())
				/ length;
		double y2 = (target.getTarget().getY() - target.getSource().getY())
				/ length;

		double dot = x1 * x2 + y1 * y2;

		double angle;

		// epsilon guard for situations where the angle is nearly zero
		if (Math.abs(dot - 1.0) < DBL_EPSILON)
		{
			angle = 0.0;
		}
		else
		{
			angle = Math.acos(dot);
		}

		//if(this._source == target.getTarget() || 
		//   this._target == target.getSource())
		if ((compareVertices(target) == SOURCE_TARGET)
				|| (compareVertices(target) == TARGET_SOURCE))
		{
			angle = PI - angle;
		}

		return angle;

	}


	//*************************************************************************

	/**
	 * Returns the angle between this edge and <code>target</code>, in the range
	 * [0, 2PI]. It is a 'pseudo'-angle in the sence that it's not calculated in
	 * terms of inverse cosine of the dot product between two vectors, which
	 * would be in [0, PI]. Rather, <code><b>this edge</b></code> is used to
	 * split the 2d plane into halfplanes, and an orientation test is then
	 * issued, which checks whether the <code><b>target</b></code> edge lies in
	 * the positive or the negative halfplane of the first one. This information
	 * augments the angle calculation to produce an angle between 0 and 2PI.
	 * 
	 * 
	 * @param target
	 *            The second edge
	 * 
	 * @return The angle
	 */
	public double pseudoAngle(
			Edge target)
	{
		// original code from angle()

		//TODO: check conditions & optimize math

		double length = getLength();
		double x1 = (_target.getX() - _source.getX()) / length;
		double y1 = (_target.getY() - _source.getY()) / length;

		length = target.getLength();

		double x2 = (target.getTarget().getX() - target.getSource().getX())
				/ length;
		double y2 = (target.getTarget().getY() - target.getSource().getY())
				/ length;

		double dot = x1 * x2 + y1 * y2;

		double angle;

		// epsilon guard for situations where the angle is nearly zero
		if (Math.abs(dot - 1.0) < DBL_EPSILON)
		{
			angle = 0.0;
		}
		else
		{
			angle = Math.acos(dot);
		}

		if (this._source == target.getTarget()
				|| this._target == target.getSource())
		{
			angle = PI - angle;
		}

		/* Now perform an orientation test on the second
		 * vector to determine whether it lies in the 
		 * positive or negative half-plane of the
		 * first vector
		 *
		 * Some silly code here that will have to be
		 * optimized later!
		 */

		double ax, ay, bx, by, cx, cy;
		ax = _source.getX();
		ay = _source.getY();
		bx = _target.getX();
		by = _target.getY();

		//cx = target.getTarget().getX();
		//cy = target.getTarget().getY();

		if (this._source == target.getTarget()
				|| this._target == target.getTarget())
		{
			cx = target.getSource().getX();
			cy = target.getSource().getY();
		}
		else
		{
			cx = target.getTarget().getX();
			cy = target.getTarget().getY();
		}

		// orientation test

		double det = (ax - cx) * (by - cy) - (bx - cx) * (ay - cy);

		if (det > 0 || (Math.abs(det) < DBL_EPSILON))
		{
			// negative half-plane, angle > PI
			angle = 2 * PI - angle;
		}
		else
		{

			// positive half-plane, angle < PI
			//angle = 2*PI - angle;
		}

		return angle;
	}


	//*************************************************************************

	/**
	 * Returns the angle this edge makes with the 3 o'clock vector, i.e. (1.0,
	 * 0.0). The returned angle is in the range of [0, 2&pi] radians and is
	 * measured <b>clockwise</b>.
	 * 
	 * @return The angle between this edge and the 3 o'clock vector
	 */
	public double angle()
	{

		// Angle relative to 12 o'clock, clockwise

		//        //normalized x-coordinate
		//        double x = (_target.getX() - _source.getX()) / getLength();
		//        double y = (_target.getY() - _source.getY()) / getLength();
		//        
		//        //TODO: maybe introduce epsilon guard for angles close to zero
		//        
		//        double angle = Math.acos(y);
		//        if(( x < 0.0 ) /*|| (Math.abs(y) < DBL_EPSILON)*/)
		//        {
		//           angle = 2*PI - angle;   
		//        }

		// angle relative to 3 o'clock, clockwise

		// normalized x-coordinate
		double x = (_target.getX() - _source.getX()) / getLength();
		double y = _target.getY() - _source.getY();

		//TODO: maybe introduce epsilon guard for angles close to zero

		double angle = Math.acos(x);

		/* Very important: because the angle is measured
		 * clockwise from 3 o'clock, the angle
		 * range between [pi, 2pi] lies in quadrants
		 * I and IV, hence the conditional y > 0.0 
		 * 
		 */
		if ((y > 0.0) /*|| (Math.abs(y) < DBL_EPSILON)*/)
		{
			angle = 2 * PI - angle;
		}

		return angle;
	}


	//*************************************************************************

	/**
	 * Calculates the angle the other way round.
	 * 
	 * @return 2*PI - angle()
	 * 
	 * @see #angle()
	 */
	public double reverseAngle()
	{
		//normalized x-coordinate
		double x = -(_target.getX() - _source.getX()) / getLength();
		double y = -(_target.getY() - _source.getY());

		double angle = Math.acos(x);

		//FIXME: use epsilon tests here!
		return (y <= 0) ? (angle) : (2 * PI - angle);
	}


	//*************************************************************************

	//*************************************************************************

	/**
	 * Returns the distance between this edge and edge e <br><b>NOT IMPLEMENTED
	 * YET!</b>
	 * 
	 * @param e
	 *            The edge
	 * 
	 * @return The distance between this edge and e
	 **/

	public double distance(
			Edge e)
	{
		double dist = 0;
		return dist;
	}


	//*************************************************************************

	/**
	 * Returns the distance between the edge and the specified point. <br><b>Not
	 * checked yet!</b> <br><b>Distance measurement for slings is not
	 * implemented yet!</b>
	 * 
	 * <br> The math is the standard formula for line-point distance in 2D. See
	 * <a href=http://mathworld.wolfram.com/
	 * Point-LineDistance2-Dimensional.html>this link</a> for details.
	 * 
	 * @param x
	 *            X coordinate of the point
	 * @param y
	 *            Y coordinate of the point
	 * 
	 * @return The distance between this edge and (x,y)
	 */

	public double distance(
			double x,
			double y)
	{
		double numerator = Math.abs((_target.getX() - _source.getX())
				* (_source.getY() - y) - (_target.getY() - _source.getY())
				* (_source.getX() - x));

		// could of course just set denominator = _length
		double denominator = _target.distance(_source);

		if (isSling())
		{
			System.out.println("Distance measurement for slings"
					+ "is not implemented yet!");
			return 0;
		}

		if (denominator < DBL_EPSILON)
		{
			//TODO: debugging message            
			return _source.distance(x, y);
		}

		return numerator / denominator;
	}


	//*************************************************************************

	/**
	 * Wrapper for distance(x,y), takes a Point2D argument instead of individual
	 * coordinates.
	 * 
	 * @param point
	 *            The point
	 * 
	 * @return The distance between this edge and point
	 */
	public double distance(
			Point2D point)
	{
		return distance(point.getX(), point.getY());
	}


	//*************************************************************************

	/**
	 * Interchanges _origin and _target of this edge, if the direction of this
	 * edge flipped
	 **/

	public void flipDirection()
	{

		V tmp_vertex = _source;
		_source = _target;
		_target = tmp_vertex;

		/* Inconsistency ?!? 
		 * Huh ?
		Face tmp_face = _leftFace;
		_rightFace = _leftFace;
		_leftFace = _rightFace;*/

	}


	//*************************************************************************

	/**
	 * Translates the edge by translating both its vertices by the specified
	 * amount.
	 * 
	 * @param x
	 *            Translation in x direction
	 * @param y
	 *            Translation in y direction
	 */
	public void translate(
			double x,
			double y)
	{
		_source.translate(x, y);
		if (!_isSling)
			_target.translate(x, y);
	}


	//*************************************************************************

	/**
	 * Wrapper for translate(x,y), takes a Point2D argument instead of
	 * individual coordinates.
	 * 
	 * @param point
	 *            The point argument
	 */
	public void translate(
			Point2D point)
	{
		translate(point.getX(), point.getY());
	}


	//*************************************************************************

	/**
	 * Translates the edge as {@link #translate(Point2D)}, but instead of absolute
	 * value it uses the difference between target and source points as the
	 * translation value.
	 * 
	 * @param source
	 *            The source point
	 * @param target
	 *            The target point
	 */
	public void translateByDelta(
			Point2D source,
			Point2D target)
	{
		translate(target.getX() - source.getX(), target.getY() - source.getY());
	}


	//*************************************************************************

	//*************************************************************************
	//                         Iterator Access Methods
	//*************************************************************************

	/* All of these have been removed, as they're pointless for
	 * an edge (at least in a simple, planar graph...)
	 */

	//*************************************************************************
	//                            Visual Methods
	//*************************************************************************

	/**
	 * Draws this edge ...
	 * 
	 * TODO: maybe provide drawDashed() as a convenience method...
	 * 
	 * @param gc
	 *            The graphics object
	 * @param pixelSize
	 *            The size of a pixel
	 * @param labelFont
	 *            The font of the labels
	 **/

	public void draw(
			Graphics2D gc,
			double pixelSize,
			Font labelFont)

	{
		// ------------------- Legacy code ----------------------------

		_render(gc, pixelSize, labelFont);

		// ------------------------------------------------------------
	}


	//*************************************************************************

	/**
	 * <br><b>NOT IMPLEMENTED YET!</b>
	 **/

	public void drawAnim(/* ? */)

	{
		//TODO: implement animation code
	}


	//*************************************************************************

	/**
	 * Returns the XML representation of an edge in form of an JDOM element.
	 * 
	 * @return The XML representation of this object
	 */
	public Element convertToXML()
	{
		// "root" <edge> XML element
		Element xml_edge = new Element(_XML_EDGE_NAME);

		/*
		 * edge' vertex ids are essentially metadata, so attributes are 
		 * used here instead of a subelement.
		 * 
		 * WARNING: These values are only used internally by the Graph
		 * class for serialization and reconstruction of edge/edge
		 * relationships.
		 */
		xml_edge.setAttribute(_XML_SOURCE_VERTEX,
				String.valueOf(_source.hashCode()));

		xml_edge.setAttribute(_XML_TARGET_VERTEX,
				String.valueOf(_target.hashCode()));

		//------------------- serialize attributes --------------------------

		Element edge_name = new Element(_XML_LABEL);
		edge_name.addContent(_label);

		Element weight = new Element(_XML_WEIGHT);
		weight.addContent(String.valueOf(_weight));

		Element weighted = new Element(_XML_WEIGHTED);
		weighted.addContent(String.valueOf(_weighted));

		Element directed = new Element(_XML_DIRECTED);
		directed.addContent(String.valueOf(_directed));

		Element edge_color = new Element(_XML_EDGE_COLOR);
		edge_color.addContent(String
				.valueOf(_edgeGC.getOutlineColor().getRGB()));

		Element text_color = new Element(_XML_LABEL_COLOR);
		text_color.addContent(String.valueOf(_textColor.getRGB()));

		//---------------- construct xml edge subtree ---------------------

		xml_edge.addContent(edge_name);

		xml_edge.addContent(weight);
		xml_edge.addContent(weighted);

		xml_edge.addContent(edge_color);
		xml_edge.addContent(text_color);

		return xml_edge;
	}


	//*************************************************************************

	/**
	 * Reconstructs an edge from an XML descriptor.
	 * 
	 * @param edge
	 *            The edge
	 */
	public void setFromXML(
			Element edge)
	{
		/* Parse edge attributes from XML description,
		 * watching out for errors.
		 * 
		 */

		try
		{
			String temp_string;

			//----------------------- label  -------------------------------

			temp_string = edge.getChildText(_XML_LABEL);
			setLabel(temp_string);

			//--------------- weight and direction -------------------------

			temp_string = edge.getChildText(_XML_WEIGHT);
			setWeight(Double.parseDouble(temp_string));

			temp_string = edge.getChildText(_XML_WEIGHTED);
			_weighted = Boolean.parseBoolean(temp_string);

			temp_string = edge.getChildText(_XML_DIRECTED);
			_directed = Boolean.parseBoolean(temp_string);

			//----------------------- colors -------------------------------

			temp_string = edge.getChildText(_XML_EDGE_COLOR);
			setColor(new Color(Integer.parseInt(temp_string)));

			temp_string = edge.getChildText(_XML_LABEL_COLOR);
			setTextColor(new Color(Integer.parseInt(temp_string)));

		}
		catch (NullPointerException ex)
		{
			System.err.println(ex.getCause());
			System.err.println("One or more attibutes could not be parsed!\n");
		}
	}


	//*************************************************************************

	/**
	 * Returns a string representation of this edge
	 * 
	 * @return A textual representation
	 **/

	public String toString()

	{
		String s = new String();
		if (_directed)
			s = s.concat("Directed");
		else
			s = s.concat("Undirected");

		s = s.concat(" Edge " + _id);

		if (_label != null)
			s = s.concat(" (" + _label+")");
		
		s = s.concat(" from " + _source.toString() + " to "
				+ _target.toString());

		if (_weighted)
			s = s.concat(" of weight: " + _weight);

		if (_label != null)
			s = s.concat(" with Label: " + _label);

		return s;

		//TODO: check toString()
	}


	/**
	 * Clones the edge without its container, source and target vertices stay
	 * the same
	 */
	/*@Override
	public Object clone()
	{
		try
		{
			Edge newEdge = (Edge) super.clone();
			newEdge._edgeContainer = null;
			newEdge._initContexts();
			newEdge._initGeometry();
			newEdge.setColor(_edgeGC.getOutlineColor());
			newEdge.setEmphasis(_emphasisGC.getFillColor(),
					(BasicStroke) _emphasisGC.getStroke());
			return newEdge;
		}
		catch (CloneNotSupportedException e)
		{
			throw new InternalError();
		}
	}*/


	//*************************************************************************

	//*************************************************************************
	//                            Private methods
	//*************************************************************************

	// ---------------- Legacy rendering and query code -----------------------

	/* This section contains compatibility methods for rendering and point
	 * containment tests / proxy queries, as well as various associated
	 * helper routines. 
	 */

	private void _initContexts()
	{
		_edgeGC = new GraphicsContext();
		_emphasisGC = new GraphicsContext();

		_edgeGC.setOutlineColor(_DEFAULT_EDGE_COLOR);

		// this was erroneous!
		//_emphasisGC.setOutlineColor(_DEFAULT_EMPHASIS_COLOR);
		_emphasisGC.setOutlineColor(_DEFAULT_EDGE_COLOR);

		_edgeGC.setLineWidth(_DEFAULT_LINE_WIDTH);
		_emphasisGC.setLineWidth(_DEFAULT_EMPHASIS_WIDTH);

		//_edgeGC.setEndCap(BasicStroke.CAP_ROUND);
		//_edgeGC.setLineJoin(BasicStroke.JOIN_ROUND);

	}


	//*************************************************************************

	/*
	 * 
	 */
	private void _initGeometry()
	{
		_slingArc = new Arc2D.Double();
		_updateSlingArc(null);

		_boundingBox = new Rectangle2D.Double();
		_tempPoint = new Point2();

		_arrowShape = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	}


	//*************************************************************************

	/* Legacy rendering code, ported more or less 1:1 from
	 * VisualEdge.java [with some modifications]
	 *  
	 */

	private void _render(
			Graphics2D g2d,
			double pixelSize,
			Font labelFont)
	{
		//TODO: label, arrow and sling arc rendering   

		//----------------------- Edge rendering ------------------------------

		/* Setup rendering attributes based on edge's type and status.
		 * 
		 * This uses a temporary GraphicsContext object so that
		 * the rendering code doesn't have to mess with the 'actual'
		 * GC's that hold the visual parameters. This code is probably
		 * not very efficient, but it's the best I can do for now.
		 * 
		 */

		_pixelSize = pixelSize; // remember pixel size

		GraphicsContext tempGC;
		float line_width;

		if (_drawEmphasized)
		{
			tempGC = new GraphicsContext(_emphasisGC);
			line_width = _emphasisGC.getLineWidth();
		}
		else
		{
			tempGC = new GraphicsContext(_edgeGC);
			line_width = _edgeGC.getLineWidth();
		}

		/* line dash attributes have to be scaled
		 * with the current pixel size as well.
		 */
		float[] dash = tempGC.getDashArray();

		if (dash != null)
		{
			for (int i = 0; i < dash.length; i++)
			{
				dash[i] *= _pixelSize;
			}

			tempGC.setDash(dash);
		}

		// selection highlights edges by making them thicker
		if (_isSelected)
		{
			tempGC.setLineWidth((float) _pixelSize * line_width
					* _SELECTION_SCALE_FACTOR);
		}
		else
		{
			// otherwise, use 'normal' line width
			tempGC.setLineWidth((float) _pixelSize * line_width);
		}

		if (isSling())
		{
			_updateSlingArc(g2d);
			g2d.setColor(tempGC.getForegroundColor());
			g2d.setStroke(tempGC.getStroke());
			g2d.draw(_slingArc);
		}
		else
		{
			g2d.setStroke(tempGC.getStroke());
			g2d.setColor(tempGC.getForegroundColor());
			g2d.drawLine((int) _source.getX(), (int) _source.getY(),
					(int) _target.getX(), (int) _target.getY());
			//------------- Arrow rendering for directed edges --------------------

			/* Rework & optimize math code, and incorporate
			 * code that watches the line width, so that the
			 * arrows don't look stupid when using thick and/or dashed
			 * lines
			 *
			 * +
			 *
			 * Add protection code so that the arrows disappear
			 * when the zoom factor is small enough.
			 */
			if (_directed)
			{
				// edge start point and direction vector
				double start_x = _source.getX();
				double start_y = _source.getY();

				double dx = _target.getX() - _source.getX();
				double dy = _target.getY() - _source.getY();

				float point_x, point_y;
				/*
				 * Position of the arrow is is calcul
				 * ated in terms of the
				 * distance of the arrow's tip, wings and tail from the
				 * target vertex' circle. These parameters are then
				 * normalized and plugged into the parametric line
				 * equation of the edge's line segment, yielding the
				 * actual positions of the arrow's tip, wings and tail.
				 */
				double arrowScale = 1.0;
				if (line_width > 1)
					arrowScale = 1 + (line_width - 1) / 5;
				if (line_width < 1 && line_width > 0)
					arrowScale = line_width + (1 - line_width) / 5;
				double tip = 0.0, wings = 0.0, tail = 0.0;
				double wing_tips = 0.0;

				tip = 1.0 - (_target.getActualRadius() / getLength());
				wings = 1.0 - ((_target.getActualRadius() + _ARROW_LENGTH
						* _pixelSize * arrowScale) / getLength());

				tail = 1.0 - ((_target.getActualRadius() + (_ARROW_LENGTH - _ARROW_TAIL_INDENT)
						* _pixelSize * arrowScale) / getLength());

				wing_tips = (_ARROW_HALF_WIDTH * _pixelSize * arrowScale)
						/ getLength();

				_arrowShape.reset(); // reset arrow geometry

				// arrow tip
				point_x = (float) (start_x + tip * dx);
				point_y = (float) (start_y + tip * dy);

				_arrowShape.moveTo(point_x, point_y);

				// temporary midpoint for wing tips
				_tempPoint.x = (float) (start_x + wings * dx);
				_tempPoint.y = (float) (start_y + wings * dy);

				// lower wing tip
				point_x = _tempPoint.x + (float) (wing_tips * dy);
				point_y = _tempPoint.y - (float) (wing_tips * dx);

				_arrowShape.lineTo(point_x, point_y);

				// indented tail
				point_x = (float) (start_x + tail * dx);
				point_y = (float) (start_y + tail * dy);

				_arrowShape.lineTo(point_x, point_y);

				// upper wing tip
				point_x = _tempPoint.x - (float) (wing_tips * dy);
				point_y = _tempPoint.y + (float) (wing_tips * dx);

				_arrowShape.lineTo(point_x, point_y);
				_arrowShape.closePath(); // complete the shape

				g2d.setStroke(_ARROW_STROKE);
				g2d.draw(_arrowShape);
				g2d.fill(_arrowShape);

			}
		}

		//------------------------ TEST SECTION -------------------------------

		/*
		// testing arc rendering
		
		// edge start point and direction vector
		double start_x = _origin.getX();
		double start_y = _origin.getY();
		
		double dx = _target.getX() - _origin.getX();
		double dy = _target.getY() - _origin.getY();
		
		double mid_x = getMidPoint().getX(); 
		double mid_y = getMidPoint().getY();
		
		double t = 0.25;
		double ctrl_x, ctrl_y;
		QuadCurve2D.Double curve = new QuadCurve2D.Double();
		
		//g2d.setStroke(tempGC.getStroke());
		
		for(int i = 0; i < 1; i++)          
		{ 
		    ctrl_x = mid_x + t * dy;
		    ctrl_y = mid_y - t * dx;
		    
		    curve.setCurve(start_x, start_y, 
		                   ctrl_x, ctrl_y, 
		                   _target.getX(), _target.getY());
		    
		    g2d.draw(curve);
		    
		    ctrl_x = mid_x - t * dy;
		    ctrl_y = mid_y + t * dx;
		    
		    curve.setCurve(start_x, start_y, 
		                   ctrl_x, ctrl_y, 
		                   _target.getX(), _target.getY());
		    
		    g2d.draw(curve);
		    
		    t+= 0.15;
		}*/

	}


	//*************************************************************************

	/* Legacy bounding box routine
	 * 
	 * TODO: check for degenerate bounding boxes in case
	 * of horizontal or vertical lines.
	 */
	private Rectangle2D _getBBox()
	{
		if (_isSling)
		{
			_boundingBox.setFrame(_slingArc.getBounds2D());
		}
		else
		{
			double x, y;

			if (_target.getY() < _source.getY())
			{
				y = _target.getY();
			}
			else
			{
				y = _source.getY();
			}

			if (_target.getX() < _source.getX())
			{
				x = _target.getX();
			}
			else
			{
				x = _source.getX();
			}

			_boundingBox.setFrame(x, y,
					Math.abs(_target.getX() - _source.getX()),
					Math.abs(_target.getY() - _source.getY()));
		}
		return _boundingBox;
	}


	//*************************************************************************

	/* Legacy point-on-edge test routine
	 * WARNING: some code here is new - the call to the distance()
	 * method.
	 * 
	 */
	private boolean _containsPoint(
			double x,
			double y)
	{
		double distance = 0.0;
		if (_isSling)
		{
			_getBBox().contains(x, y);

		}
		else
		{
			// check whether the specified point lies on the edge
			distance = distance(x, y);
		}

		return (distance < _pixelSize * _CLICK_TOLERANCE);
	}


	/**
	 * Checks if this is a sling and sets internal variable accordingly
	 */
	private void _checkSling()
	{
		_isSling = _source.equals(_target);
		if (_isSling)
			_updateSlingArc(null);
	}


	/**
	 * Updates sling arc coordinates
	 */

	private void _updateSlingArc(
			Graphics2D g2d)
	{
		double radius = 0.0;
		if (g2d != null)
			radius = _source.getActualRadius(g2d, _pixelSize);
		else
			radius = _source.getActualRadius();
		double slingWidth = radius * 1.5;
		double slingHeight = radius * 2;
		double x = _source.getX() - (slingWidth / 2);
		double y = _source.getY() - radius - slingHeight * 0.9;
		_slingArc.setArc(x, y, slingWidth, slingHeight, -80, 340, Arc2D.OPEN);
	}

	//*************************************************************************
	//                            Class methods
	//*************************************************************************

	//disabled default constructor
	private Edge() {}
}
