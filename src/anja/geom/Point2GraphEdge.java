package anja.geom;


import java.io.Serializable;


/**
 * An edge contained in a Point2Graph. This edge contains one BasicLine2 and all
 * information neccessary for implementing a Quad Edge Data Structure [Guibas,
 * Stolfi, 1985]. The usage of BasicLine2 allows graphs with unbounded edges,
 * e.g. Voronoi-Diagrams.
 * 
 * @version 0.1 11.07.01
 * @author Wolfgang Meiswinkel, Thomas Kamphans
 */
public class Point2GraphEdge
		implements Serializable
{

	//************************************************************
	// public constants
	//************************************************************

	public final static int	SOURCE_POINT	= 0;
	public final static int	TARGET_POINT	= 1;
	public final static int	NONE			= 2;

	//************************************************************
	// private variables
	//************************************************************

	/** The edge itself */
	protected BasicLine2	_edge;

	/** The left and right face of the edge */
	protected Polygon2		_leftface, _rightface;

	/**
	 * The next (clockwise) and previous (counterclockwise) edges start from
	 * source and targetpoint.
	 */
	protected Point2GraphEdge	_spnext, _spprev, _tpnext, _tpprev;


	//************************************************************
	// constructors
	//************************************************************

	/**
	 * New edge (p,q) with next edges clockwise and counterclockwise from p and
	 * q, and left and right faces. Point2GraphEdge stores <b>References</b> to
	 * these objects!
	 * 
	 * @param theEdge
	 *            ---
	 * @param pcw
	 *            ---
	 * @param pccw
	 *            ---
	 * @param qcw
	 *            ---
	 * @param qccw
	 *            ---
	 * @param leftface
	 *            ---
	 * @param rightface
	 *            ---
	 */
	public Point2GraphEdge(
			BasicLine2 theEdge,
			Point2GraphEdge pcw,
			Point2GraphEdge pccw,
			Point2GraphEdge qcw,
			Point2GraphEdge qccw,
			Polygon2 leftface,
			Polygon2 rightface)
	{
		_edge = theEdge;
		_spnext = pcw;
		_spprev = pccw;
		_tpnext = qcw;
		_tpprev = qccw;
		_leftface = leftface;
		_rightface = rightface;
	} // Point2GraphEdge


	/**
	 * New edge (p, q) with all other data set to <tt>null</tt>
	 * 
	 * @param theEdge
	 *            The new edge
	 */
	public Point2GraphEdge(
			BasicLine2 theEdge)
	{
		_edge = theEdge;
		_spnext = null;
		_spprev = null;
		_tpnext = null;
		_tpprev = null;
		_leftface = null;
		_rightface = null;
	} // Point2GraphEdge


	//************************************************************
	// class methods
	//************************************************************

	//************************************************************
	// public methods
	//************************************************************

	/**
	 * Returns the reference to source point
	 * 
	 * @return The source point
	 */
	public Point2 source()
	{
		return _edge.source();
	} // source


	/**
	 * Returns the reference to target point
	 * 
	 * @return The target point
	 */
	public Point2 target()
	{
		return _edge.target();
	} // target


	/**
	 * Returns the reference to the BasicLine2 defining this edge
	 * 
	 * @return The line-object representing the edge
	 */
	public BasicLine2 line()
	{
		return _edge;
	} // line


	/**
	 * Return reference to the BasicLine2 defining this edge, casted to
	 * Segment2.
	 * 
	 * WARNING: This is only to be used in graphs, that consists only of edges
	 * of type Segment2!
	 * 
	 * @return The segment
	 */
	public Segment2 segment()
	{
		return (Segment2) _edge;
	} // segment


	/**
	 * Returns the next (clockwise) edge containing the source point
	 * 
	 * @return The next edge containing the source point
	 */
	public Point2GraphEdge sourcePointNextEdge()
	{
		return _spnext;
	} // SourcePointNextEdge


	/**
	 * Returns the previous (counterclockwise) edge containing the source point
	 * 
	 * @return The last edge containing the source point
	 */
	public Point2GraphEdge sourcePointPrevEdge()
	{
		return _spprev;
	} // SourcePointPrevEdge


	/**
	 * Returns the next (clockwise) edge containing the target point
	 * 
	 * @return The next edge containing the target point
	 */
	public Point2GraphEdge targetPointNextEdge()
	{
		return _tpnext;
	} // targetPointNextEdge 


	/**
	 * Returns the previous (counterclockwise) edge containing the target point
	 * 
	 * @return The last edge containing the target point
	 */
	public Point2GraphEdge targetPointPrevEdge()
	{
		return _tpprev;
	} // targetPointPrevEdge


	/**
	 * return the next (clockwise) edge containing point p or null
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return The next edge containing p or null
	 */
	public Point2GraphEdge nextEdge(
			Point2 p)
	{
		if (p == this.line().source())
		{
			return _spnext;
		}
		if (p == this.line().target())
		{
			return _tpnext;
		}
		return null;
	} // NextEdge


	/**
	 * Returns the previous (counterclockwise) edge containing point p or null
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return The last edge containing p or null
	 */
	public Point2GraphEdge prevEdge(
			Point2 p)
	{
		if (p == this.line().source())
		{
			return _spprev;
		}
		if (p == this.line().target())
		{
			return _tpprev;
		}
		return null;
	} // PrevEdge


	/**
	 * Returns SOURCE_POINT if p equals source point Returns TARGET_POINT if p
	 * equals target point else return NONE
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return SOURCE_POINT, TARGET_POINT or NONE
	 */
	public int pointEdgeRelation(
			Point2 p)
	{
		if (p == this.line().source())
		{
			return SOURCE_POINT;
		}
		if (p == this.line().target())
		{
			return TARGET_POINT;
		}
		return NONE;
	} // pointEdgeRelation


	/**
	 * Returns the next (clockwise) edge containing the source point
	 * 
	 * @return The next edge containing the source point
	 */
	public BasicLine2 sourcePointNextLine()
	{
		if (_spnext != null)
		{
			return _spnext.line();
		}
		else
		{
			return null;
		}
	} // SourcePointNextLine


	/**
	 * Returns the previous (counterclockwise) edge containing the source point
	 * 
	 * @return The last edge containing the source point
	 */
	public BasicLine2 sourcePointPrevLine()
	{
		if (_spprev != null)
		{
			return _spprev.line();
		}
		else
		{
			return null;
		}
	} // SourcePointPrevLine


	/**
	 * Returns the next (clockwise) edge containing the target point
	 * 
	 * @return The next edge containing the target point
	 */
	public BasicLine2 targetPointNextLine()
	{
		if (_tpnext != null)
		{
			return _tpnext.line();
		}
		else
		{
			return null;
		}
	} // targetPointNextLine 


	/**
	 * Returns the previous (counterclockwise) edge containing the target point
	 * 
	 * @return The last edge containing the target point
	 */
	public BasicLine2 targetPointPrevLine()
	{
		if (_tpprev != null)
		{
			return _tpprev.line();
		}
		else
		{
			return null;
		}
	} // targetPointPrevLine


	/**
	 * Returns the next (clockwise) edge containing point p or null
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return The next edge containing the source point or null
	 */
	public BasicLine2 nextLine(
			Point2 p)
	{
		if (p == this.line().source() && _spnext != null)
		{
			return _spnext.line();
		}
		if (p == this.line().target() && _tpnext != null)
		{
			return _tpnext.line();
		}
		return null;
	} // nextLine


	/**
	 * Returns the previous (counterclockwise) edge containing point p or null
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return The last edge containing the source point or null
	 */
	public BasicLine2 prevLine(
			Point2 p)
	{
		if (p == this.line().source() && _spprev != null)
		{
			return _spprev.line();
		}
		if (p == this.line().target() && _tpprev != null)
		{
			return _tpprev.line();
		}
		return null;
	} // prevLine


	/**
	 * Returns the left face
	 * 
	 * @return The left face
	 */
	public Polygon2 getLeftFace()
	{
		return _leftface;
	} // getLeftFace


	/**
	 * Returns the right face
	 * 
	 * @return The right face
	 */
	public Polygon2 getRrightFace()
	{
		return _rightface;
	} // getRrightFace


	/**
	 * Sets the next (clockwise) edge containing the source point
	 * 
	 * @param e
	 *            The next edge (source)
	 */
	public void sourcePointNextEdge(
			Point2GraphEdge e)
	{
		_spnext = e;
	} // SourcePointNextEdge


	/**
	 * Sets the previous (counterclockwise) edge containing the source point
	 * 
	 * @param e
	 *            The last edge (source)
	 */
	public void sourcePointPrevEdge(
			Point2GraphEdge e)
	{
		_spprev = e;
	} // SourcePointPrevEdge


	/**
	 * Sets the next (clockwise) edge containing the target point
	 * 
	 * @param e
	 *            The next edge (target)
	 */
	public void targetPointNextEdge(
			Point2GraphEdge e)
	{
		_tpnext = e;
	} // targetPointNextEdge 


	/**
	 * Sets the previous (counterclockwise) edge containing the target point
	 * 
	 * @param e
	 *            The last edge (target)
	 */
	public void targetPointPrevEdge(
			Point2GraphEdge e)
	{
		_tpprev = e;
	} // targetPointPrevEdge


	/**
	 * Sets the left face
	 * 
	 * @param p
	 *            The new left face
	 */
	public void setLeftFace(
			Polygon2 p)
	{
		_leftface = p;
	} // setLeftFace


	/**
	 * Sets the right face
	 * 
	 * @param p
	 *            The new right face
	 */
	public void setRightFace(
			Polygon2 p)
	{
		_rightface = p;
	} // setRightFace


	/**
	 * Convert to a string
	 * 
	 * @return A textual representation of this object
	 */
	public String toString()
	{
		String output_string = "\n";
		output_string = output_string + "Point2GraphEdge    \n";
		output_string = output_string + "Line          "
				+ this.line().toString() + " whight " + this.line().getValue()
				+ "\n";
		output_string = output_string + "Next (source) ";
		if (_spnext != null)
		{
			output_string = output_string + _spnext.line().toString() + "\n";
		}
		else
		{
			output_string = output_string + " null \n";
		}
		output_string = output_string + "Prev (source) ";
		if (_spprev != null)
		{
			output_string = output_string + _spprev.line().toString() + "\n";
		}
		else
		{
			output_string = output_string + " null \n";
		}
		output_string = output_string + "Next (target) ";
		if (_tpnext != null)
		{
			output_string = output_string + _tpnext.line().toString() + "\n";
		}
		else
		{
			output_string = output_string + " null \n";
		}
		output_string = output_string + "Prev (target) ";
		if (_tpprev != null)
		{
			output_string = output_string + _tpprev.line().toString();
		}
		else
		{
			output_string = output_string + " null ";
		}
		return output_string;
	} // toString

	//************************************************************
	// Private methods
	//************************************************************

} // Point2GraphEdge
