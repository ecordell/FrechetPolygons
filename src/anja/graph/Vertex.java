
/* File: Vertex.java
 * Created on 01.09.2005 by Birgit
 * 
 */

package anja.graph;

import java.util.Iterator;
import java.awt.*;
import java.awt.geom.*;
import java.util.Vector;

import org.jdom.*;

import anja.geom.*;
import anja.util.GraphicsContext;
import static anja.util.MathConstants.*; // for DBL_EPSILON
import static anja.graph.Constants.*;

/**
 * This Class implements a drawable vertex on a plane or in space.
 * 
 * @author Birgit Engels [engels@cs.uni-bonn.de]
 * @author Ibragim Kouliev [kouliev@cs.uni-bonn.de]
 * @author Jörg Wegener
 * 
 * 
 * <p>
 * TODO: XML I/O routines <br>
 * TODO: Complete basic documentation <br>
 * TODO: Write additional documentation <br>
 * TODO: Maybe replace the pairs mark() / unmark() etc. with
 * setMarked() and so on.
 * 
 * <p>
 * Please note that for three-dimensional vertices, only basic
 * geometric functionality (i.e. coordinate queries, L2-norm distance
 * etc.) is implemented, and there are <u>no</u> rendering routines
 * or point-in-vertex tests!
 * 
 * @see Graph
 * @see Edge
 */

//TODO: check and maybe rephrase some comments 
/* TODO: Will we ever need 4-dimensional vertices (i.e. for
 * projective space operations etc.)?
 */

public abstract class Vertex
		implements java.io.Serializable
{
    
    //*************************************************************************
    //                         Public constants
    //*************************************************************************
	
    /* Notwendig ? */
    public static final int NODIM   =   0;
    public static final int LINE    =   1;  
    public static final int PLANE   =   2;
    public static final int SPACE   =   3;
	
    //*************************************************************************
    //                         Private constants
    //*************************************************************************
    
   
    // Default radii in pixels
    private static final int   _DEFAULT_RADIUS        = 10;
    private static final int   _DEFAULT_PROXY_RADIUS  = 20;
    private static       int   _MIN_RADIUS            = 3;
    
    //------------------------ XML I/O constants ------------------------------
    
    // this one is package-visible because Graph.java needs to access it
    static final String _XML_VERTEX_NAME      = "Vertex";
    static final String _XML_VERTEX_ID          = "id";   
    // ------------------------ Attribute names -------------------------------
    

    
    private static final String _XML_LABEL              = "label";
    private static final String _XML_RADIUS             = "radius";
    private static final String _XML_PROXY_RADIUS       = "proxy_radius";
    
    private static final String _XML_FILL_COLOR         = "fill_color";
    private static final String _XML_OUTLINE_COLOR      = "outline_color";
    private static final String _XML_LABEL_COLOR        = "label_color";
    
    private static final String _XML_X_COORD            = "x";
    private static final String _XML_Y_COORD            = "y";
    private static final String _XML_Z_COORD            = "z";
    
    private static final String _XML_WEIGHT             = "weight";
    private static final String _XML_WEIGHTED           = "weighted";
    
    //*************************************************************************
    //                          Class variables
    //*************************************************************************
	
    protected static boolean            _proxyZonesEnabled;
    
    // shared graphics contexts for drawing proxy zones and selection rects
    protected static GraphicsContext    _PROXY_GC;
    protected static GraphicsContext    _HIGHLIGHT_GC;
    
    //*************************************************************************
    //                      Public instance variables
    //*************************************************************************
	
    //*************************************************************************
    //                      Protected instance variables
    //*************************************************************************
    
    protected double      _x;  // Coordinates 
    protected double      _y;
    protected double      _z;
             
    /** 
     * ID of the vertex in the graph,
     * the graph is responsible of assigning unique IDs to its vertices
     * from 0 counting upwards. -1 indicates that the vertex has not yet
     * been assigned to a graph.
     */
    protected int         _id = -1;
       
    protected int         _dimension  = NODIM;    // Dimension           
    protected String      _label      = null;     // Vertex label
    
    protected Object      _reference  = null;     // attached object reference
    protected int         _counter    =   0;      // general-purpose counter
    protected double      _weight     =   0;      // vertex weight
    
    /* flags with specific meaning */  
    protected boolean     _weighted   = false; 
    
    /* flags with user-determined meaning */    
    protected boolean     _marked     = false;
    protected boolean     _known      = false;
    protected boolean     _visited    = false;
    
    // selection flag for visual editing purposes
    protected boolean     _isSelected = false;

	// this variable can contain a vector of vertices which
	// match a path to this vertex
	protected Vector<Vertex> _path = null;
	
	protected Graph _graph = null;
    
    //---------- visual attributes / helper objects for rendering -------------
    
    protected int       _vertexRadius;
    protected int       _proxyZoneRadius;
    
    /* Used for tracking the pixel size during rendering and 
     * object hit tests.
     * 
     */
    protected double    _pixelSize; // current pixel size
    
    //protected Ellipse2D _circle;   
    //protected Color     _outlineColor;
    //protected Color     _fillColor;
    //protected Color     _proxyColor;
    protected Color     _textColor;
    
    /* Flag used for temporarily hilighting the proxy zone */
    protected boolean   _bDrawProxyZone;
    
    /*
     * This section contains visual attributes etc. used by the
     * compatibility rendering routine and point containment /
     * proximity queries . They'll be removed when we have time to
     * write new versions of those.
     */
    
    //-------------------------------------------------------------------------
    
    // geometry primitives used for rendering
    protected Circle2          _vertexCircle;
    protected Rectangle2       _hilightBox;
    protected Rectangle2D      _boundingBox;
    
    // associated graphic attributes
    protected GraphicsContext  _vertexGC;
    
    //-------------------------------------------------------------------------
    
    
    // static init
    static
    {
        _proxyZonesEnabled = false;
        
        
        // initialize rendering attributes for proxy zones and selection rects.
        _PROXY_GC  = new GraphicsContext(Constants._DEFAULT_VERTEX_PROXY_COLOR,
                                         Color.white);

        _HIGHLIGHT_GC = new GraphicsContext(_DEFAULT_VERTEX_HILIGHT_COLOR,
                                            Color.white);
        
        // proxy zones and hilight boxes are drawn as outlines only!
        _PROXY_GC.setFillStyle(0);
        _HIGHLIGHT_GC.setFillStyle(0);
        
        /* Line width of 0.0 means the thinnest possible
         * outlines, which are also NOT affected by affine transforms and
         * therefore don't change their width when zooming in/out.
         */
        _PROXY_GC.setLineWidth(0.0f);
        _HIGHLIGHT_GC.setLineWidth(0.0f);
        
        setProxyColor(_DEFAULT_VERTEX_PROXY_COLOR);
    }
    
    //*************************************************************************
    // 			     Private instance variables
    //*************************************************************************
        	
    //*************************************************************************
    //                            Constructors
    //*************************************************************************
    
    /**
     * Constructs a new vertex, with all coordinates set to zero and
     * predefined visual appearance, i.e:
     * <p>
     * <ul>
     * <li>Radius of 10 pixels</li>
     * <li>Black outline, no fill and black labels</li>
     * <li>Proxy zone radius of 20 pixels</li>
     * </ul>
     * <p>
     * All vertices also share default graphics attributes for proxy
     * zones and highlight rectangles: blue for proxies and red for
     * highlight rects.
     * </p>
     * 
     * <p>
     * All vertex attributes can be queried and modified at any time
     * by the corresponding getter and setter methods.
     * </p>
     * 
     * <p>
     * All other constructors below are convenience wrappers for various 
     * 'vertex creation situations', they internally call
     * the base constructor.
     * </p>
     * 
     */
    protected Vertex()
    {   
        /* this constructor does most of the work,
         * everything else is linked to it.
         */
    	
        _x = 0;
        _y = 0;
        _z = 0;
        
        _isSelected = _marked = _visited = _known = false;
        
        // default visual parameters
        
        //------------------ Compatibility code -----------------------------
        
        _initContexts();
        _initGeometry();
        
        setOutlineColor(_DEFAULT_VERTEX_OUTLINE_COLOR);
        setColor(_DEFAULT_VERTEX_FILL_COLOR);
        
        //----------------------- New code ----------------------------------
        
        _vertexRadius      = _DEFAULT_RADIUS;
        _proxyZoneRadius   = _DEFAULT_PROXY_RADIUS;   
        _textColor         = _DEFAULT_VERTEX_TEXT_COLOR;        
        
        //_outlineColor      = _DEFAULT_OUTLINE_COLOR;
        //_fillColor         = _DEFAULT_FILL_COLOR;
        //_proxyColor        = _DEFAULT_PROXY_COLOR;
        
        _bDrawProxyZone    = false;
        _pixelSize         = 1.0;
	
        _label = "";
    }
   
    //*************************************************************************
    //                     Package-visible instance methods
    //*************************************************************************
    
	
    //*************************************************************************
    //                        Public instance methods
    //*************************************************************************
	
    static public void setMinRadius(int radius)
    {
    	_MIN_RADIUS = radius;
    }
    // ------------------------ Getters / Setters -----------------------------
    
    /**
     * @param x The x to set.
     */
    public void setX(double x)
    {
        this._x = x;
        _graph.vertexMoved(this);
    }
    
    //*************************************************************************

    /**
     * @return Returns the x-coordinate.
     */
    public double getX()
    {
        return _x;
    }
    
    //*************************************************************************

    /**
     * @param y The y to set.
     */
    public void setY(double y)
    {
        this._y = y;
        _graph.vertexMoved(this);
    }
    
    //*************************************************************************

    /**
     * @return Returns the y.
     */
    public double getY()
    {
        return _y;
    }
    
    //*************************************************************************

    /**
     * @param z The z to set.
     */
    public void setZ(double z)
    {
        this._z = z;
        _graph.vertexMoved(this);
    }
    
    //*************************************************************************

    /**
     * @return Returns the z.
     */
    public double getZ()
    {
        return _z;
    }
    
    //*************************************************************************
    
    /**
     * Returns the position of a two-dimensional vertex as a Point2D
     * object. 
     * 
     */
    public Point2D getPosition()
    {
        return new Point2D.Double(_x, _y);
    }
    
  
    //-------------- Getters / setters for graphic attributes -----------------
    
    /**
     * Returns the fill color of a vertex
     * 
     */
    public Color getColor()
    {
        return _vertexGC.getFillColor();
    }
    
    //*************************************************************************
    
    /**
     * Sets the fill color of a vertex
     * 
     */
    public void setColor(Color color)
    {
        if(color != null)
         _vertexGC.setFillColor(color);
    }
     
       
    //*************************************************************************
    
    /**
     * Sets the outline color of a vertex
     * 
     */
    public void setOutlineColor(Color color)
    {
        if(color != null)
         _vertexGC.setOutlineColor(color);
    }
    
    public Color getOutlineColor() {
    	return _vertexGC.getOutlineColor();
    }
    
    //*************************************************************************
    
    /**
     * Sets the text (label) color of a vertex
     * 
     */
    public void setTextColor(Color color)
    {
        if(color != null)
         _textColor = color;
    }
    
    public Color getTextColor() {
    	return _textColor;
    }
    
    //************************************************************************* 
    
    /**
     * Returns the image-space radius of a vertex, in pixels.
     * The corresponding setter is setRadius(). 
     * 
     */
    public int getRadius()
    {
        return _vertexRadius;
    }
    
    //*************************************************************************
    
    /**
     * Returns the current object-space radius of a vertex, which
     * is its [usually] constant image-space radius * current 
     * world-to-image space scaling factor.
     * 
     * <p>
     * This value changes continuously, acoording to e.g. current 'zoom' 
     * factor in an editor, as oppposed to the value returned by
     * getRadius() !
     * </p>
     * 
     */
    public double getActualRadius()
    {
        return _vertexRadius * _pixelSize;
    }
    
    /**
     * Returns the radius of a vertex as it would have if the label was scaled to the
     * given pixel size in the given Graphics2D object.
     * This is needed for slings because slings derive their
     * size from the vertices size. Since edges are always
     * drawn before vertices, the vertex' radius might not have been scaled
     * to fit the label inside. (Pixel size is always updated by a call to
     * the draw/_render methods).
     */
    public double getActualRadius(Graphics2D g2d, double pixelSize)
    {
        if (_label != null) {
            // determine the width and height of the label string
            Rectangle2D label_bounds = g2d.getFontMetrics().getStringBounds(_label, g2d);
            if(_vertexRadius * pixelSize < 0.5 * label_bounds.getWidth())
                return ((int)(1.2 * (label_bounds.getWidth() / (2.0 * pixelSize))))*pixelSize;
        }
        return _vertexRadius * pixelSize;
    }    
    
    //*************************************************************************
    
    /**
     * Sets the radius of this vertex. This method will
     * also automatically adjust the proxy zone radius to be 
     * at least 10 pixels greater to keep proxy zones working
     * reliably.
     * Note: the minimum allowed radius is 10 pixels. Lower
     * values will be automatically clamped to that.
     * 
     * @param radius New vertex radius, in pixels.
     */
    public void setRadius(int radius)
    {
        _vertexRadius = Math.max(radius, _MIN_RADIUS);
        
        // keep proxy zone at least 10 pixels wider than the vertex itself
        if(_proxyZoneRadius < _vertexRadius + 10)
         _proxyZoneRadius = _vertexRadius + 10;
    }
    
    //*************************************************************************
    
    /**
     * Returns current radius of the proximity zone.
     * 
     * @return The radius
     */
    public int getProxyRadius()
    {
        return _proxyZoneRadius;
    }
    
    //*************************************************************************
    
    /**
     * Changes the radius of the proximity zone. Does not
     * have any effect if the specified radius is less
     * than the current vertex radius + 10 pixels.
     * <br>[in order for proxy zones to work reliably]
     * 
     * @param radius new radius, in pixels
     */
    public void setProxyRadius(int radius)
    {
        if(radius > _vertexRadius + 10)
          _proxyZoneRadius = radius;
       
    }
    
    //*************************************************************************
    
    /**
     * Returns this vertex' bounding box in world coordinates.
     * 
     * @return The bounding box
     */
    public Rectangle2D getBoundingBox()
    {
        // ------------------ Legacy code ------------------
        
        return _getBBox();
        
        // -------------------------------------------------
    }
    
    //*************************************************************************
     
    /**
     * This method is used to temporarily highlight the proximity
     * zone of a vertex. The highlight is active for the duration
     * of one rendering and is automatically reset after that.
     * 
     */
    public void highlightProxyZone()
    {
        _bDrawProxyZone = true;
    }
    
    //*************************************************************************
    
    /**
     * Returns the graph the vertex belongs to, null if the vertex is not in a graph.
     * 
     * @return reference to <code><b>Graph</b></code>
     */
    public final Graph getGraph()
    {
        return _graph;
    }
    
    //*************************************************************************
    
    /**
     * Returns the reference to an incident edge, if the containing 
     * data structure supports it, otherwise <code>null</code>.
     * <p>The edge returned is generally an arbitrary edge incident to this 
     * vertex.
     * 
     * @return Reference to an <code>Edge</code> 
     */
    abstract public Edge<? extends Vertex> getIncidentEdge();
    
    //*************************************************************************
       
    /**
     * Returns the label of this vertex
     **/
    public String getLabel() 
    {
        return _label;
    }
    
    //*************************************************************************
            
    /**
     * Sets the label of this vertex to s
     **/
    public void setLabel(String s)
    {
        _label = s;
    }
    
    //*************************************************************************
   
    /**
     * Sets the label to NULL
     **/  
    public void resetLabel()  
    {
        _label = null;
    }  
    
    //*************************************************************************
    
    /**
     * Returns the stored object reference. 
     **/  
    public Object getReferenceObject() 
    {
        return _reference;
    }
    
    //*************************************************************************
    
    /**
     * Sets the object reference to objectRef.
     *
     * @param objectRef Object reference to set
     **/ 
    public void setReferenceObject(Object objectRef)  
    {
        _reference = objectRef;
    }
    
    //*************************************************************************
        
    /**
     * Returns the current value of the internal general-purpose counter.
     */  
    public int getCounter()  
    {
        return _counter;
    }
    
    //*************************************************************************
        
    /**
     * Increments the internal general-purpose counter.
     **/  
    public void incCounter() 
    {
        _counter++;
    }
    
    //*************************************************************************
        
    /**
     * Sets the value of the internal general-purpose counter.
     *
     * @param value Integer value to set.
     **/    
    public void setCounter( int value ) 
    {
        _counter = value;
    }
    
    //*************************************************************************
        
    /**
     * Resets the counter to zero.
     **/  
    public void resetCounter()
    {
        _counter = 0;
    }
    
    //*************************************************************************
    
    /**
     * Returns the weight value of this vertex
     **/   
    public double getWeight()
    {
        return _weight;
    }
    
    //*************************************************************************
        
    /**
     * Sets the weight value of vertex and automatically sets the
     * 'weighted' flag to <b>true</b>.
     *
     * @param w Integer value to set.
     **/   
    public void setWeight( double w ) 
    {
        _weighted = true;
        _weight = w;
    } 
    
    //*************************************************************************
        
    /**
     * Resets the weight to zero and resets the 'weighted' flag.
     * 
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
    }
    
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
    
    public void select()
    {
        _isSelected = true;
    }
    
    //*************************************************************************
    
    public void deselect()
    {
        _isSelected = false;
    }
    
    //*************************************************************************
    
    public void setSelected(boolean on)
    {
        _isSelected = on;
    }
    
    /**
     * Assigns an ID to the vertex.
     * Called by the graph, the graph itself is responsible for assigning unique ids.
     * Should not be called manually outside of an addVertex-Operation.
     * @param id The ID to assign.
     */
    public void setID(int id) {
    	_id = id;
    }
    
    /**
     * Returns the ID of the vertex.
     */
    public int getID() {
    	return _id;
    }
    
    
    
    //*************************************************************************
    
    /**
     * Moves the vertex to a new location specified by the (x,y)
     * coordinates.
     * 
     * <p>
     * These moveTo() methods, as well as translate() and all setX/Y/Z
     * methods automatically synchronize the surrounding data
     * structure to the changes in vertex position, if necessary (e.g.
     * in a DCEL the edges surrounding a vertex must be re-ordered
     * after it's been moved).
     * 
     * @param xcoord x-coordinate of the target position
     * @param ycoord y-coordinate of the target position
     * 
     */ 
    
    public void moveTo(double xcoord, double ycoord)
    {
        _x = xcoord;
        _y = ycoord;
        _graph.vertexMoved(this);
    }
    
    //************************************************************************* 
    
    /**
     * Wrapper for moveTo(x,y), takes a Point2D object instead of 
     * individual coordinates.
     * 
     * @param position
     */
    public void moveTo(Point2D position)
    {
        moveTo(position.getX(), position.getY());
    }
   
    //************************************************************************* 
    
    /**
     * Same as above, but in <b>3-space</b>.
     * 
     * @param xcoord x-coordinate of the target position
     * @param ycoord y-coordinate of the target position
     * @param zcoord z-coordinate of the target position
     */
    
    public void moveTo(double xcoord, double ycoord, double zcoord)
    {
        _x = xcoord;
        _y = ycoord;
        _z = zcoord;
        _graph.vertexMoved(this);       
    }
    
    //*************************************************************************
    
    /**
     * Translates a 2-d vertex by values specified by <b>xdelta</b> and 
     * <b>ydelta</b>.
     * <p>
     * Note that this is <b>NOT</b> the same as setting a vertex'
     * position to a new set of coordinates!
     * </p>
     */
    public void translate(double xdelta, double ydelta)
    {
        _x += xdelta;
        _y += ydelta;
        _graph.vertexMoved(this);
    }
    
    //*************************************************************************
    
    /**
     * Wrapper for translate(dx,dy), takes a Point2D object instead
     * of individual coordinates.
     * 
     */
    public void translate(Point2D delta)
    {
        translate(delta.getX(), delta.getY());
    }
        
    //TODO: implement additional convenience moveTo() methods
    
    // --------------------------- Query methods ------------------------------
    
    /**
     **/  
    public boolean isWeighted()
    {
        return _weighted;
    }
    
    //*************************************************************************
        
    /**
     * Returns TRUE, if the '_marked'-flag is set
     **/
    public boolean isMarked()
    {
        return _marked;
    }
    
    //*************************************************************************
    
    /**
     * Returns TRUE, if the '_known'-flag is set
     **/
    public boolean isKnown()
    {
        return _known;
    }
	
    //*************************************************************************
    
    /**
     * Returns TRUE, if the '_marked'-flag is set
     **/
    public boolean isVisited()
    {
        return _visited;
    }
    
    //*************************************************************************
    
    public boolean isSelected()
    {
        return _isSelected;
    }
    
    //*************************************************************************
    
    /**
     * Returns <code><b>true</b></code>, if this vertex is located
     * at the same coordinates as the vertex v.
     *
     * @param v vertex to compare
     **/
    public boolean samePosition(Vertex v)
    {
        if (v == null)
        {
            return false;
        }
        else
        {
            return ( (Math.abs(_x - v._x) < DBL_EPSILON) && 
                     (Math.abs(_y - v._y) < DBL_EPSILON) && 
                     (Math.abs(_z - v._z) < DBL_EPSILON) 
                   );
        }
    }
    
    //*************************************************************************
    
    /**
     * Returns <code><b>true</b></code>, if this vertex' attributes  
     * are all identical to those of vertex v.
     *
     * @param v vertex to compare
     **/
    public boolean sameAttributes(Vertex v)
    {
        if (v==null) return false;
        else return ( samePosition(v) && 
                      _reference.equals(v.getReferenceObject()) &&
                      _label.equals(v.getLabel())               &&
                      ( _weight     == v.getWeight())           &&
                      ( _counter    == v.getCounter())          &&
                      ( _marked     == v.isMarked())            &&
                      ( _visited    == v.isVisited())           &&
                      ( _known      == v.isKnown()));
                      
    }
    
    //*************************************************************************
        
    /**
     * Returns <code><b>true</b></code>, if this vertex is identical
     * to the vertex v, which is true if they both have the same
     * ID.
     *
     * @param v vertex to compare
     * 
     * <p>
     * TODO:check logic here! 
     **/
    public boolean equals(Vertex v)
    {
        if( (v != null) && (hashCode() == v.hashCode()) )
        {
          return true;
        }        
        return false;
    }    
     
    //*************************************************************************
    
    /**
     * Checks whether a vertex contains the specified point. This
     * works in the following sense: the point is <u>inside</u> the
     * vertex if it's inside the 'circle' that visually represents 
     * the vertex.<br>
     * (Obviously, this is pointless for non-embedded graphs...)
     * 
     * @param xcoord x-corrdinate of the point to test
     * @param ycoord y-corrdinate of the point to test
     * 
     */
    public boolean containsPoint(double xcoord, double ycoord)
    {
        // ----------- Legacy code --------------------------
        
        return _containsPoint(xcoord, ycoord);
        
        // --------------------------------------------------
    }
    
    //*************************************************************************
    
    /**
     * A 3-d containment test (NOT IMPLEMENTED)
     * 
     * @param xcoord First coordinate
     * @param ycoord Second coordinate
     * @param zcoord Third coordinate
     * @return Result of the test
     */
    public boolean containsPoint(double xcoord, double ycoord, 
                                 double zcoord)
    {
        // TODO: implement 3-d point containment test
        return false;
    }
    
    //*************************************************************************
    
    /**
     * Tests whether a point is contained within the proximity zone of
     * a vertex. This is used primarily inside SwingFramework
     * components for detecting the mouse pointer's proximity to
     * vertices, but could conceivably be employed in other tasks.
     * 
     * @param x x-corrdinate of the point to test
     * @param y y-corrdinate of the point to test
     * 
     */
    public boolean pointInProxyZone(double x, double y)
    {
        // ------------- Legacy code -----------------------------
        
        return _pointInProxyZone(x,y);
        
        // -------------------------------------------------------
    }
    
    //*************************************************************************
    
    // ----------------------------- Wrappers --------------------------------
    
    /**
     * Wrapper for containsPoint(), takes a Point2D argument
     * instead of individual coordinates.
     * 
     * @param point point to test
     */
    public boolean containsPoint(Point2D point)
    {
        return containsPoint(point.getX(), point.getY());
    }
    
    //*************************************************************************
    
    /**
     * Wrapper for pointInProxyZone(), takes a Point2D argument
     * instead of individual coordinates.
     * 
     * @param point point to test
     */
    public boolean pointInProxyZone(Point2D point)
    {
        return pointInProxyZone(point.getX(), point.getY());
    }
    
    //*************************************************************************
    //                        Iterator Access Methods
    //*************************************************************************
         
    /**
     * Returns an instance of Iterator<Vertex>, which can be
     * used to enumerate all vertices adjacent to this one, i.e. those
     * on the "1-ring" around a given vertex.
     * 
     * @param mode See Constants for a list of possible
     *            modes.
     */    
    abstract public Iterator<Vertex> getVertexIterator(int mode);

    
    //*************************************************************************
    
    /**
     * Returns an instance of Iterator<Edge> which can be used
     * to enumerate all edges adjacent to this vertex.
     * 
     * @param mode See Constants for a list of possible
     *            modes.
     */
    abstract public Iterator<Edge> getEdgeIterator(int mode);
   
    //*************************************************************************
    
    /**
     * Returns an instance of AbstractIterator<Face> which can be used
     * to enumerate all faces adjacent to this vertex.
     * 
     * @param mode See AbstractIterator<Face> for a list of possible
     *            modes.
     */
    abstract public Iterator<Face> getFaceIterator(int mode);
    
    //*************************************************************************
    //                    Arithmetic and Geometric Methods
    //*************************************************************************
    
    
    /**
     * Returns the Euclidian distance to another vertex
     *
     * @param v Vertex to which the distance to this 
     *          vertex is calculated/returned.
     **/
    public double distance(Vertex v)
    {
        
        double dist = (this._x - v._x) * (this._x - v._x) + 
                      (this._y - v._y) * (this._y - v._y) + 
                      (this._z - v._z) * (this._z - v._z);
             
        dist = Math.sqrt(dist);
        
        return dist;
        
    } 
    
   
    //*************************************************************************
    
    /**
     * Returns the distance to a specified point in &#x211D<sup>3</sup>.
     * 
     * @param xcoord x-coordinate of the point
     * @param ycoord y-coordinate of the point
     * @param zcoord z-coordinate of the point
     */
    
    public double distance(double xcoord, double ycoord, double zcoord)
    {
        double dist = (this._x - xcoord) * (this._x - xcoord) + 
                      (this._y - ycoord) * (this._y - ycoord) + 
                      (this._z - zcoord) * (this._z - zcoord);

        dist = Math.sqrt(dist);

        return dist;
    }
    
    //*************************************************************************
    
    /**
     * Returns the distance to a specified point in &#x211D<sup>2</sup>.
     * 
     * @param xcoord x-coordinate of the point
     * @param ycoord y-coordinate of the point
     */
    public double distance(double xcoord, double ycoord)
    {
        double dist = (this._x - xcoord) * (this._x - xcoord) + 
                      (this._y - ycoord) * (this._y - ycoord);

        dist = Math.sqrt(dist);
        return dist;
        
    }
        
    //*************************************************************************
    
    public double distance(Point2D point)
    {
        return distance(point.getX(), point.getY());
    }
    
    //*************************************************************************
    
    /**
     * Returns the euclidian distance to an edge
     *
     * @param e Edge to which the distance to this 
     *           Vertex is calculated/returned.
     * <br><b>Math here is not checked yet!</b>
     **/
    public double distance(Edge e)
    {
        // Both vertices of the edge e and this vertex form a triangle ABC.
        // If the height from this vertex to the edge lies inside the
        // triangle, its length gives the distance to the edge.
        // Otherwise the distance to one of the vertices of the edge is shorter
        // than the height and is returned as distance.
        
        double a, b, c, dist;
        a = distance(e.getSource());
        b = distance(e.getTarget());
        c = e.getLength();
               
        dist = a*a - Math.sqrt((a*a -b*b + c*c)/(2*c));
        
        dist = Math.sqrt(dist);
        
        if (a<dist) dist = a;
        if (b<dist) dist = b;
        
        return dist;
        //TODO: check floating point comparisons and math
    }    
    
        
    //*************************************************************************
    //                            Visual Methods
    //*************************************************************************
        
    /**
     * Wrapper for draw(), takes only the graphics context argument,
     * assumes the value of 1.0 for pixel size and the GC's current
     * font.
     **/
    public void draw(Graphics2D gc)
    {
        draw(gc, 1.0, gc.getFont());
    }
    
    //*************************************************************************
    
    /**
     * Renders the vertex using a Graphics2D context. This method
     * expects the following arguments:
     * 
     * @param gc a Graphics2D rendering context
     *            <p>
     * @param pixelSize describes the scaling that should be applied
     *            to the vertex prior to rendering. This is typically
     *            used to hold the vertex radii constant (in pixels),
     *            when zooming in/out of a graph.
     *            <p>
     * @param labelFont font to be used when rendering the vertex
     *            label. In case of a 'Y-inverted' world coordinate
     *            system (and consequently, a 'Y-inverting'
     *            world-to-screen affine transform), this should be a
     *            Font that's been flipped vertically. For a more
     *            concise description how this works, read the
     *            documentation for the Graph class.
     * 
     */
    public void draw(Graphics2D gc, double pixelSize, Font labelFont)
    {
        // ------------------ Legacy code --------------------------
        
        _render(gc, pixelSize, labelFont);
        
        // --------------------------------------------------------- 
        
        // < new rendering code will go here someday...>
    }
    
    //*************************************************************************
    
    /**
     * <br><b>NOT IMPLEMENTED YET!</b>
     * 
     **/
    public void drawAnimation(/* ? */)
    {
         //TODO: implement animation code
    }
    
    //-------------------------- XML Input/Output -----------------------------
    
    /**
     * Returns the XML representation of a vertex in form of an
     * JDOM element.
     */
    public Element convertToXML()
    {
        // "root" <vertex> XML element
        Element xml_vertex = new Element(_XML_VERTEX_NAME);
        
        /*
         * vertex' id is essentially metadata, so an attribute is used
         * here instead of a subelement.
         * 
         * WARNING: These values are only used internally by the Graph
         * class for serialization and reconstruction of vertex/edge
         * relationships.
         */
        xml_vertex.setAttribute(_XML_VERTEX_ID, String.valueOf(hashCode()));
        
        //------------------- serialize attributes --------------------------
        
        Element vertex_name = new Element(_XML_LABEL);
        vertex_name.addContent(_label);
        
        Element vertex_radius = new Element(_XML_RADIUS);
        vertex_radius.addContent(String.valueOf(_vertexRadius));
        
        Element vertex_x = new Element(_XML_X_COORD);
        vertex_x.addContent(String.valueOf(_x));
        
        Element vertex_y = new Element(_XML_Y_COORD);
        vertex_y.addContent(String.valueOf(_y));
        
        Element vertex_z = new Element(_XML_Z_COORD);
        vertex_z.addContent(String.valueOf(_z));
        
        Element weight = new Element(_XML_WEIGHT);
        weight.addContent(String.valueOf(_weight));
        
        Element weighted = new Element(_XML_WEIGHTED);
        weighted.addContent(String.valueOf(_weighted));
        
        Element fill_color = new Element(_XML_FILL_COLOR);
        fill_color.addContent(
         String.valueOf(_vertexGC.getFillColor().getRGB()));
        
        Element outline_color = new Element(_XML_OUTLINE_COLOR);
        outline_color.addContent(
        String.valueOf(_vertexGC.getOutlineColor().getRGB()));
        
        Element text_color = new Element(_XML_LABEL_COLOR);
        text_color.addContent(String.valueOf(_textColor.getRGB()));
        
        //---------------- construct xml vertex subtree ---------------------
        
        xml_vertex.addContent(vertex_name);
        xml_vertex.addContent(vertex_radius);
        
        xml_vertex.addContent(vertex_x);
        xml_vertex.addContent(vertex_y);
        xml_vertex.addContent(vertex_z);
        
        xml_vertex.addContent(weight);
        xml_vertex.addContent(weighted);
        
        xml_vertex.addContent(outline_color);
        xml_vertex.addContent(fill_color);
        xml_vertex.addContent(text_color);
        
        return xml_vertex; 
    }
    
    
    //*************************************************************************
    
    /**
     * Reconstructs a vertex from an XML descriptor.
     * 
     * 
     */
    public void setFromXML(Element vertex)
    {
        /* Parse vertex attributes from XML description,
         * watching out for errors.
         * 
         */
        
        try
        {           
            String temp_string;
            
            //------------------ label & radius ----------------------------
            
            temp_string = vertex.getChildText(_XML_LABEL);
            setLabel(temp_string);
            
            temp_string = vertex.getChildText(_XML_RADIUS);
            setRadius(Integer.parseInt(temp_string));
            
            //--------------- coordinates and weight -----------------------
            
            temp_string = vertex.getChildText(_XML_X_COORD);
            setX(Double.parseDouble(temp_string));
            
            temp_string = vertex.getChildText(_XML_Y_COORD);
            setY(Double.parseDouble(temp_string));
            
            temp_string = vertex.getChildText(_XML_Z_COORD);
            setZ(Double.parseDouble(temp_string));
            
            temp_string = vertex.getChildText(_XML_WEIGHT);
            setWeight(Double.parseDouble(temp_string));
            
            temp_string = vertex.getChildText(_XML_WEIGHTED);
            _weighted = Boolean.parseBoolean(temp_string);
            
            //----------------------- colors -------------------------------
            
            temp_string = vertex.getChildText(_XML_OUTLINE_COLOR);
            setOutlineColor(new Color(Integer.parseInt(temp_string)));
            
            temp_string = vertex.getChildText(_XML_FILL_COLOR);
            setColor(new Color(Integer.parseInt(temp_string)));
            
            temp_string = vertex.getChildText(_XML_LABEL_COLOR);
            setTextColor(new Color(Integer.parseInt(temp_string)));
            
        }      
        catch(NullPointerException ex)
        {
            System.err.println(ex.getCause());            
            System.err.println(
             "One or more attibutes could not be parsed!\n");
        }
        
    }
    
    //*************************************************************************
    
    /**
     * Returns a string representation of this vertex
     **/
    public String toString()
    {
        String s = "Vertex "+ _id;
        if (_label!=null) s = s.concat(" ("+_label+")");
        s = s.concat(" at ");
        
        switch (_dimension)
        {
            case SPACE: 
                s=s.concat("("+_x+","+_y+","+_z+")");
                break;
            case PLANE:
                s=s.concat("("+_x+","+_y+")");
                break;
            case LINE:
                s=s.concat("("+_x+")");
                break;
        }
        return s;
    }
    
    //*************************************************************************
    //                            Private Methods
    //*************************************************************************
    
    /* This section contains compatibility methods for rendering and point
     * containment tests / proxy queries, as well as various associated
     * helper routines. 
     * 
     * It exists for the following reason:
     * We were hesitant to discard existing code implemented in VisualVertex
     * and VisualEdge classes of the old anja.graph package. To port it
     * into the new graph package, we decided to provide a 'fork' in
     * the rendering and other methods, in the following sense:
     * 
     * The methods draw() etc. visible to 'outside world' are essentially
     * wrappers that call private methods containing the 'old' code,
     * which perform the actual work.
     * 
     * As we design improved versions of the rendering and query routines,
     * they will be packaged into other private methods, and the wrappers
     * will be adjusted accordingly to call the new code.
     *  
     */
    
    private void _initContexts()
    {
        _vertexGC = new GraphicsContext(_DEFAULT_VERTEX_OUTLINE_COLOR,
                                        _DEFAULT_VERTEX_FILL_COLOR);
        
        // set common parameters
        _vertexGC.setFillStyle(1);
        
        /* Line width of 0.0 means the thinnest possible
         * outlines, which are also NOT affected by affine transforms and
         * therefore don't change their width when zooming in/out.
         */
        _vertexGC.setLineWidth(0.0f);        
    }
    
    //*************************************************************************
    
    private void _initGeometry()
    {
        _vertexCircle        = new Circle2(0, 0, _DEFAULT_RADIUS);
        _boundingBox         = new Rectangle2D.Double();
        _hilightBox          = new Rectangle2();
    }
    
    //*************************************************************************
    
    /*
     * Legacy rendering code, ported more or less 1:1 from
     * VisualVertex.java in anja.graph
     * 
     * @param gc
     * @param pixelSize
     * @param labelFont
     * 
     * TODO: adjust positioning constants if necessary
     */
    private void _render(Graphics2D g2d, double pixelSize, Font labelFont)
    {
        // remember pixel size 
        _pixelSize = pixelSize;
            
        /* Although label_bounds is initialized to null here,
         * it will never cause an exception in label rendering code,
         * since in can only be accessed if the label itself is 
         * NOT null.
         */
        
        Rectangle2D label_bounds = null;
        if(_label != null)
        {
            /* We also need to adjust the vertex radius if the
             * label doesn't fit inside, or if the 
             * radius is too large. This is admittedly rather
             * primitive, but the best I can do for now.
             */
            
            // determine the width and height of the label string
            FontMetrics metrics = g2d.getFontMetrics();
            label_bounds = metrics.getStringBounds(_label, g2d);
            
            // adjust vertex radius  
            if(
               ((_vertexRadius * _pixelSize) < 0.5 * label_bounds.getWidth()))
               //change: only change size if too small
                /*||
               ((_vertexRadius * _pixelSize) > 0.3 * label_bounds.getWidth()))*/
            {
                setRadius((int)
                  (1.2 * (label_bounds.getWidth() / (2.0 * _pixelSize))));
            }
        }
        
        // set vertex geometry and draw it
        
        _vertexCircle.centre.x  = (float)_x;
        _vertexCircle.centre.y  = (float)_y;        
        _vertexCircle.radius    = (float)(pixelSize * _vertexRadius);
        
        _vertexCircle.draw(g2d, _vertexGC);
        
        float hilight_radius = (float)((_vertexRadius + 4) * pixelSize);
        
        // if the vertex is selected, draw selection outline
        
        if(_isSelected)
        {
                _hilightBox.setRect(_vertexCircle.centre.x - hilight_radius,
                                    _vertexCircle.centre.y - hilight_radius,
                                     2 * hilight_radius,
                                     2 * hilight_radius);
                
                _hilightBox.draw(g2d, _HIGHLIGHT_GC);
        }
        
        // render proxy zone if necessary
        
        if((_proxyZonesEnabled == true) && (_bDrawProxyZone == true))          
        {
            
            // draw a small ring as a visual indicator for the proxy zone 
            _vertexCircle.radius = (float)(_proxyZoneRadius * pixelSize);
            _vertexCircle.draw(g2d, _PROXY_GC);
            
            // un-highlight proxy zone
            _bDrawProxyZone = false;           
        }
                
        // render vertex label if it's not empty
        
        if(_label != null) 
        {
           g2d.setColor( _textColor );  
           g2d.drawString(_label,
                          (float)(_x - label_bounds.getWidth() / 2.0),
                          (float)(_y + label_bounds.getHeight()/ 3.0));       
        }
        
    }
    
    //*************************************************************************
    
    /*
     *  Legacy point-in-proximity-zone test, ported from
     *  VisualVertex.java
     * 
     */
    private boolean _pointInProxyZone(double x, double y)
    {
        /* Short-circuit so that this method always
         * returns false if proxy zones are disabled
         */
        if(_proxyZonesEnabled == false)
         return false;
        
        // test against normal circle
        _vertexCircle.radius = (float)(_vertexRadius * _pixelSize);
        boolean in_vertexCircle = _vertexCircle.pointInCircle(x,y);
        
        // test against the proxy zone
        _vertexCircle.radius  = (float)(_proxyZoneRadius * _pixelSize);
        boolean in_proxy = _vertexCircle.pointInCircle(x,y);
        
        /* if the point is in proxy zone and NOT in vertex itself
         * return true, otherwise false
         */
        
        return (!in_vertexCircle) & (in_proxy);
    }
    
    //*************************************************************************
    
    // Legacy point containment test
    private boolean _containsPoint(double x, double y)
    {
        _vertexCircle.radius = (float)(_vertexRadius * _pixelSize);
       return _vertexCircle.pointInCircle(x,y);
    }
    
    //*************************************************************************
    
    // Legacy bounding box routine
    private Rectangle2D _getBBox()
    {
                //calculate bounding rectangle
                _boundingBox.setFrame(_x - _vertexRadius * _pixelSize,
                                      _y - _vertexRadius * _pixelSize,
                                       2 * _vertexRadius * _pixelSize,
                                       2 * _vertexRadius * _pixelSize);       
                
                return _boundingBox;
    }
    
    //*************************************************************************
    //                             Class Methods
    //*************************************************************************
    
    /**
     * Enables / disables checking of vertex proximity zones.
     * 
     * @param enabled Use <code><b>true</b></code> to enable, or
     * <code><b>false</b></code> to disable proxy zones.
     */
    public static void enableProxyZones(boolean enabled)
    {
        _proxyZonesEnabled = enabled;
    }
    
    //*************************************************************************
    
    /**
     * Sets the proxy zone color for all vertices.
     * 
     * @param color new proxy zone color
     */
    public static void setProxyColor(Color color)
    {
        if(color != null)
         _PROXY_GC.setOutlineColor(color);
    }
    
     
    //*************************************************************************
    /**
     * Sets the path to this vertex
     */
	public void setPath(Vector<Vertex> newPath) {
		_path = newPath;
	}

    //*************************************************************************
    /**
     * Returns the path to this vertex
     */
	public Vector<Vertex> getPath() {
		return _path;
	}

}