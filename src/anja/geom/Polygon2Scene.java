package anja.geom;


import java.io.*;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import anja.geom.Point2Graph;
import anja.geom.visgraph.*;

import anja.util.Drawable;
import anja.util.GraphicsContext;
import anja.util.Matrix33;
import anja.util.SimpleList;

import java.util.Vector;


/**
 * Polygon2Scene implements a scene of polygons. This scene has various
 * parameters, e.g. it can be open or closed, i.e. bounded by an outer polygon.
 * It can contain open or closed polygons, see Polygon2.
 * 
 * @version 0.1 11.07.01
 * @author Wolfgang Meiswinkel, Thomas Kamphans
 */

// ****************************************************************
public class Polygon2Scene
		extends Polygon2SceneData
		implements Cloneable, Drawable
// ****************************************************************
{

	//************************************************************
	// public constants
	//************************************************************

	/** Constant */
	public final static int	OK						= 0;

	/** Constant */
	public final static int	POLYGON					= 1;

	/** Constant */
	public final static int	BOUNDING				= 2;

	//************************************************************
	// private variables
	//************************************************************

	protected boolean		notVisible				= false;

	protected int			numOfVisibleEdges		= -1;

	protected Intersection	lastIntersection		= new Intersection();

	protected boolean		useBoundigPolygonInVisG	= false;

	/**
	 * Controls the behavior of <tt>isInFreeSpace</tt>: if this is set to true,
	 * points on the obstacle boundaries are part of the free space, otherwise
	 * points on the obstacle boundaries are part of the obstacles.
	 */
	protected boolean		_obstaclesOpen			= true;

	/** True, if the stored visibility graph is valid. */
	protected boolean		_visGraphDirtyBit		= true;

	/**
	 * Stored vis graph.
	 * 
	 * @see #_visGraphDirtyBit
	 */
	protected Point2Graph	_visG					= null;

	/**
	 * Stored vis graph.
	 * 
	 * @see #_visGraphDirtyBit
	 */
	protected VisGraph		_visGraph				= null;


	//************************************************************
	// constructors
	//************************************************************

	/**
	 * create empty scene
	 */
	public Polygon2Scene()
	{} // Polygon2Scene


	/**
	 * Erzeugen einer Polygon2Scene aus einem DataInputStream
	 * 
	 * @param dis
	 *            Der Inputstream mit dem Objekt
	 */
	public Polygon2Scene(
			DataInputStream dis)
	{
		try
		{
			String type = dis.readUTF();
			if (!type.equals("Polygon2Scene"))
				return;

			int no_of_polys = dis.readInt();
			for (int i = 0; i < no_of_polys; i++)
				add(new Polygon2(dis));

			boolean bounding = dis.readBoolean();
			if (bounding)
				setBoundingPolygon(new Polygon2(dis));
		}
		catch (IOException ex)
		{
			System.out.println(ex);
		} // catch
	} // Polygon2Scene


	//************************************************************
	// class methods
	//************************************************************

	//************************************************************
	// public methods
	//************************************************************

	/**
	 * Schreiben der Polygon2Scene in den DataOutputStream dos
	 * 
	 * @param dos
	 *            Der Outputstream auf das Ziel
	 */
	public void save(
			DataOutputStream dos)
	{
		try
		{
			dos.writeUTF("Polygon2Scene");

			Polygon2[] polygons = getInteriorPolygons();

			dos.writeInt(polygons.length);
			for (int i = 0; i < polygons.length; i++)
				polygons[i].save(dos);

			Polygon2 bounding_poly = getBoundingPolygon();

			boolean bounding = (bounding_poly != null);
			dos.writeBoolean(bounding);
			if (bounding)
				bounding_poly.save(dos);
		}
		catch (IOException ex)
		{
			System.out.println(ex);
		} // catch
	} // save


	//------------------------------------------------------------
	// Control settings of the scene
	//------------------------------------------------------------

	/**
	 * Set the scene to be closed, i.e. bounded by an outer polygon, if a
	 * bounding polygon is set. (default)
	 */
	public void setClosed()
	{
	// ???
	} // setClosed


	/**
	 * Set the scene to be open, i.e. not bounded by an outer polygon If a
	 * bounding polygon is set, it will be ignored.
	 */
	public void setOpen()
	{} // setOpen


	/**
	 * Allow linesegments as obstacles (default)
	 */
	public void allowLineSegments()
	{} // allowLineSegments


	/**
	 * Prohibit linesegments as obstacles, allow only 'fleshy' polygons If open
	 * polygons are added, they will be ignored.
	 * 
	 * Not yet implemented.
	 */
	public void prohibitLineSegments()
	{} // prohibitLineSegments


	//------------------------------------------------------------
	// Access to polygons:
	//------------------------------------------------------------

	/**
	 * Add polygon.
	 * 
	 * @param poly
	 *            The polygon to add
	 */
	public void add(
			Polygon2 poly)
	{
		addPolygon(poly);
		_visGraphDirtyBit = false;
		numOfVisibleEdges = -1;
	} // add


	/**
	 * Remove Polygon from scene.
	 * 
	 * @param poly
	 *            The polygon to remove
	 */
	public void remove(
			Polygon2 poly)
	{
		removePolygon(poly);
		_visGraphDirtyBit = false;
		numOfVisibleEdges = -1;
	} // remove


	/**
	 * Remove ith Polygon from scene.
	 * 
	 * @param i
	 *            The numberr of the polygon to remove
	 */
	public void remove(
			int i)
	{
		Polygon2 poly = getPolygon(i);
		if (poly != null)
			remove(poly);
	} // remove


	/**
	 * Return an array of all interior polygons of the scene.
	 * 
	 * @return Array of the polygons of this scene
	 */
	public Polygon2[] getPolygons()
	{
		return getInteriorPolygons();
	} // getInteriorPolygons


	/**
	 * Returns the i_th polygon from the array of all interior polygons.
	 * 
	 * <br>Author: Sascha Ternes<br> Version: added 28.02.02
	 * 
	 * @param i
	 *            the index of the polygon in the array
	 * 
	 * @return the ith polygon or null, if no ith Polygon available
	 */
	public Polygon2 getPolygon(
			int i)
	{
		Polygon2[] polygons = getInteriorPolygons();

		if (i < 0 || i >= polygons.length)
			return null;

		return polygons[i];
	} // getPolygon


	/**
	 * Returns the ith polygon from the array of all polygons.
	 * 
	 * 
	 * <br>0 is the first closed polygon (boundingpolygon if exists)
	 * 
	 * <br>Author: Pit Prüßner<br> Version: added 25.04.07
	 * 
	 * @param i
	 *            the index of the polygon in the array
	 * 
	 * @return the ith polygon or null, if no ith Polygon available
	 */
	public Polygon2 getPolygonAll(
			int i)
	{
		Polygon2 polygon = getBoundingPolygon();
		Polygon2[] polygons;
		if (polygon != null)
		{
			Polygon2[] temppolygons = getInteriorPolygons();
			polygons = new Polygon2[temppolygons.length + 1];
			polygons[0] = polygon;
			System.arraycopy(temppolygons, 0, polygons, 1, temppolygons.length);
		}
		else
		{
			polygons = getInteriorPolygons();
		}

		if (i < 0 || i >= polygons.length)
			return null;

		return polygons[i];
	} // getPolygonAll


	/**
	 * Checks, if the position of the polygon is valid.
	 * 
	 * <br>Return values are:<br>{@link #OK} -> None polygon/bounding polygon
	 * intersection AND none polygon intersection<br>{@link #BOUNDING} ->
	 * Polygon / bounding polygon intersection<br>{@link #POLYGON} -> Polygon
	 * intersection with other scenepolygons
	 * 
	 * @return {@link #OK}, {@link #BOUNDING} or {@link #POLYGON}
	 * 
	 * @see #OK
	 * @see #BOUNDING
	 * @see #POLYGON
	 */
	public int isValid()
	{
		Polygon2 boundingPoly = getBoundingPolygon();
		Polygon2[] polygons = getInteriorPolygons();
		if (boundingPoly != null)
		{
			BasicLine2[] boundingEdges = boundingPoly.edges();
			int edgeCnt = boundingPoly.edgeNumber();
			for (int cnt3 = 0; cnt3 < edgeCnt; cnt3++)
			{
				BasicLine2 aktEdge = boundingEdges[cnt3];
				for (int cnt4 = 0; cnt4 < polygons.length; cnt4++)
				{
					Polygon2 testPoly = polygons[cnt4];
					testPoly.intersection(aktEdge, lastIntersection);
					if (lastIntersection.result != Intersection.EMPTY)
					{
						return BOUNDING;
					}
				} // for
			} // for
		}
		else
		{
			for (int cnt = 0; cnt < polygons.length; cnt++)
			{
				Polygon2 aktPoly = polygons[cnt];
				BasicLine2[] theEdges = aktPoly.edges();
				int edgeCounter = aktPoly.edgeNumber();
				for (int cnt1 = 0; cnt1 < edgeCounter; cnt1++)
				{
					BasicLine2 aktEdge = theEdges[cnt1];
					for (int cnt2 = cnt + 1; cnt2 < polygons.length; cnt2++)
					{
						Polygon2 testPoly = polygons[cnt2];
						testPoly.intersection(aktEdge, lastIntersection);
						if (lastIntersection.result != Intersection.EMPTY)
						{
							return POLYGON;
						}
					} // for
				} // for
			} // for
		} // else

		return OK;
	} // isValid


	//------------------------------------------------------------
	// Access to edges and vertices:
	//------------------------------------------------------------

	/**
	 * Return number of polygon vertices (interior polygons).
	 * 
	 * @return Number of all vertices
	 */
	public int numOfAllVertices()
	{
		int numOfPoints = 0;
		Polygon2[] polygons = getInteriorPolygons();
		for (int xy = 0; xy < polygons.length; xy++)
		{
			Polygon2 aktPoly = polygons[xy];
			numOfPoints = numOfPoints + (aktPoly.points()).length();
		}
		return numOfPoints;
	} // numOfAllVertices


	/**
	 * Return all polygon vertices (interior polygons).
	 * 
	 * @return Array of all points of the scene
	 */
	public Point2[] vertices()
	{
		Point2[] return_array = new Point2[numOfAllVertices()];
		Polygon2[] polygons = getInteriorPolygons();
		int arrayCount = 0;
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = (Polygon2) polygons[cnt];
			SimpleList thePoints = aktpoly.points();
			// SimpleList baseindex == 0 !
			for (int cnt1 = 0; cnt1 < thePoints.length(); cnt1++)
			{
				Point2 nextPoint = (Point2) thePoints.getValueAt(cnt1);
				return_array[arrayCount] = nextPoint;
				arrayCount++;
			}
		}
		return return_array;
	} // vertices


	/**
	 * Return number off all polygon edges (interior polygons).
	 * 
	 * @return Number of all edges
	 */
	public int numOfAllEdges()
	{
		int numOfEdges = 0;
		Polygon2[] polygons = getInteriorPolygons();
		for (int xy = 0; xy < polygons.length; xy++)
		{
			Polygon2 aktPoly = polygons[xy];
			numOfEdges = numOfEdges + aktPoly.edgeNumber();
		}
		return numOfEdges;
	} // numOfAllEdges


	/**
	 * Return all polygon edges (interior polygons).
	 * 
	 * @return All edges of the scene
	 */
	public Segment2[] edges()
	{
		Segment2[] return_array = new Segment2[numOfAllEdges()];
		int arrayCount = 0;
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			Segment2[] theEdges = aktpoly.edges();
			int edgecount = aktpoly.edgeNumber();
			for (int cnt1 = 0; cnt1 < edgecount; cnt1++)
			{
				return_array[arrayCount] = theEdges[cnt1];
				arrayCount++;
			}
		}
		return return_array;
	} // edges


	//------------------------------------------------------------
	// Intersections
	//------------------------------------------------------------

	/**
	 * Calculate the set of intersections of the scene with the BasicLine and
	 * return them using the Intersection parameter. The intersection can be
	 * empty or contain a set of points or line segments.
	 * 
	 * @param input_basic
	 *            The input line to check for an intersection with
	 * @param inout_intersection
	 *            The intersection object (reference)
	 */
	public void intersection(
			BasicLine2 input_basic,
			Intersection inout_intersection)
	{
		Point2 singlePoint = null;
		Segment2 singleSegment = null;
		SimpleList totalResult = new SimpleList();
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			aktpoly.intersectionSingle(input_basic, inout_intersection);
			if (inout_intersection.result != Intersection.EMPTY)
			{
				if (inout_intersection.result == Intersection.LIST)
				{
					totalResult.concat(inout_intersection.list);
				}
				if (inout_intersection.result == Intersection.POINT2)
				{
					totalResult.add(inout_intersection.point2);
					singlePoint = inout_intersection.point2;
				}
				if (inout_intersection.result == Intersection.SEGMENT2)
				{
					totalResult.add(inout_intersection.segment2);
					singleSegment = inout_intersection.segment2;
				}
			}
		}
		if (totalResult.length() == 0)
		{
			lastIntersection.set();
			return;
		}
		if (totalResult.length() == 1)
		{
			if (singlePoint != null)
			{
				lastIntersection.set(singlePoint);
				return;
			}
			if (singleSegment != null)
			{
				lastIntersection.set(singleSegment);
				return;
			}
		}
		inout_intersection.set(totalResult);
		lastIntersection = (Intersection) inout_intersection.clone();
	} // intersection


	/**
	 * Calculate the set of intersections of the scene with the BasicCircle and
	 * return them using the Intersection parameter. The intersection can be
	 * empty or contain a set of points.
	 * 
	 * @param input_basic
	 *            The input circle to check for an intersection with
	 * @param inout_intersection
	 *            The intersection object (reference)
	 */
	public void intersection(
			BasicCircle2 input_basic,
			Intersection inout_intersection)
	{
		Point2 singlePoint = null;
		SimpleList totalResult = new SimpleList();
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			aktpoly.intersection(input_basic, inout_intersection);
			if (inout_intersection.result != Intersection.EMPTY)
			{
				if (inout_intersection.result == Intersection.LIST)
				{
					totalResult.concat(inout_intersection.list);
				}
				if (inout_intersection.result == Intersection.POINT2)
				{
					totalResult.add(inout_intersection.point2);
					singlePoint = inout_intersection.point2;
				}
			}
		}
		if (totalResult.length() == 0)
		{
			lastIntersection.set();
			return;
		}
		if (totalResult.length() == 1)
		{
			lastIntersection.set(singlePoint);
			return;
		}
		inout_intersection.set(totalResult);
		lastIntersection = (Intersection) inout_intersection.clone();
	} // intersection


	/**
	 * Calculate the set of intersections of the scene with the Polygon2 and
	 * return them using the Intersection parameter. The intersection can be
	 * empty or contain a set of points or line segments.
	 * 
	 * @param input_basic
	 *            The input polygon to check for an intersection with
	 * @param inout_intersection
	 *            The intersection object (reference)
	 */
	public void intersection(
			Polygon2 input_basic,
			Intersection inout_intersection)
	{
		Rectangle2D br2d = input_basic.getBoundingRect();

		/*
		 * Warning: the following casts may be dangerous under certain circumstances,
		 * but they're necessary for this to compile correctly.
		 * (Or I'll need to add a double-type argument constructor 
		 * to Rectangle2
		 * 
		 */

		Rectangle2 br = new Rectangle2((float) br2d.getX(),
				(float) br2d.getY(), (float) br2d.getWidth(), (float) br2d
						.getHeight());
		if (!intersects(br))
		{
			inout_intersection.set();
			return;
		}
		Point2 singlePoint = null;
		Segment2 singleSegment = null;
		SimpleList totalResult = new SimpleList();
		int polyEdges = input_basic.edgeNumber();
		Segment2[] polySegs = input_basic.edges();
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			for (int j = 0; j < polyEdges; j++)
			{
				aktpoly.intersectionSingle(polySegs[j], inout_intersection);
				if (inout_intersection.result != Intersection.EMPTY)
				{
					if (inout_intersection.result == Intersection.LIST)
					{
						totalResult.concat(inout_intersection.list);
					}
					if (inout_intersection.result == Intersection.POINT2)
					{
						totalResult.add(inout_intersection.point2);
						singlePoint = inout_intersection.point2;
					}
					if (inout_intersection.result == Intersection.SEGMENT2)
					{
						totalResult.add(inout_intersection.segment2);
						singleSegment = inout_intersection.segment2;
					}
				}
			} // for
		}
		if (totalResult.length() == 0)
		{
			lastIntersection.set();
			return;
		}
		if (totalResult.length() == 1)
		{
			if (singlePoint != null)
			{
				lastIntersection.set(singlePoint);
				return;
			}
			if (singleSegment != null)
			{
				lastIntersection.set(singleSegment);
				return;
			}
		}
		inout_intersection.set(totalResult);
		lastIntersection = (Intersection) inout_intersection.clone();
	} // intersection


	/**
	 * Calculate the set of intersections of the scene with the Rectangle2 and
	 * return them using the Intersection parameter. The intersection can be
	 * empty or contain a set of points or line segments.
	 * 
	 * @param input_basic
	 *            The input rectangle to check for an intersection with
	 * @param inout_intersection
	 *            The intersection object (reference)
	 */
	public void intersection(
			Rectangle2 input_basic,
			Intersection inout_intersection)
	{
		Point2 singlePoint = null;
		Segment2 singleSegment = null;
		SimpleList totalResult = new SimpleList();
		Segment2[] rectSegs = new Segment2[4];
		rectSegs[0] = input_basic.top();
		rectSegs[1] = input_basic.right();
		rectSegs[2] = input_basic.bottom();
		rectSegs[3] = input_basic.left();
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			for (int j = 0; j < 4; j++)
			{
				aktpoly.intersectionSingle(rectSegs[j], inout_intersection);
				if (inout_intersection.result != Intersection.EMPTY)
				{
					if (inout_intersection.result == Intersection.LIST)
					{
						totalResult.concat(inout_intersection.list);
					}
					if (inout_intersection.result == Intersection.POINT2)
					{
						totalResult.add(inout_intersection.point2);
						singlePoint = inout_intersection.point2;
					}
					if (inout_intersection.result == Intersection.SEGMENT2)
					{
						totalResult.add(inout_intersection.segment2);
						singleSegment = inout_intersection.segment2;
					}
				}
			} // for
		}
		if (totalResult.length() == 0)
		{
			lastIntersection.set();
			return;
		}
		if (totalResult.length() == 1)
		{
			if (singlePoint != null)
			{
				lastIntersection.set(singlePoint);
				return;
			}
			if (singleSegment != null)
			{
				lastIntersection.set(singleSegment);
				return;
			}
		}
		inout_intersection.set(totalResult);
		lastIntersection = (Intersection) inout_intersection.clone();
	} // intersection


	/**
	 * Test, if the scene intersects with the given BasicLine
	 * 
	 * @param input_basic
	 *            The input line
	 * 
	 * @return true, if there is an intersection, false else
	 */
	public boolean intersects(
			BasicLine2 input_basic)
	{
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			aktpoly.intersectionSingle(input_basic, lastIntersection);
			if (lastIntersection.result != Intersection.EMPTY)
			{
				return true;
			}
		}
		return false;
	} // intersects


	/**
	 * Test, if the scene intersects with the given BasicCircle2
	 * 
	 * @param input_basic
	 *            The input circle
	 * 
	 * @return true, if there is an intersection, false else
	 */
	public boolean intersects(
			BasicCircle2 input_basic)
	{
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			aktpoly.intersection(input_basic, lastIntersection);
			if (lastIntersection.result != Intersection.EMPTY)
			{
				return true;
			}
		}
		return false;
	} // intersects


	/**
	 * Test, if the scene intersects with the given Polygon2
	 * 
	 * @param input_basic
	 *            The input polygon
	 * 
	 * @return true, if there is an intersection, false else
	 */
	public boolean intersects(
			Polygon2 input_basic)
	{
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			Segment2[] polyEdges = aktpoly.edges();
			int edgecounter = aktpoly.edgeNumber();
			for (int cnt1 = 0; cnt1 < edgecounter; cnt1++)
			{
				BasicLine2 nextEdge = polyEdges[cnt1];
				aktpoly.intersection(nextEdge, lastIntersection);
				if (lastIntersection.result != Intersection.EMPTY)
				{
					return true;
				}
			}
		}
		return false;
	} // intersects


	/**
	 * Test, if the scene intersects with the given Rectangle2
	 * 
	 * @param input_basic
	 *            The input rectangle
	 * 
	 * @return true, if there is an intersection, false else
	 */
	public boolean intersects(
			Rectangle2 input_basic)
	{
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			BasicLine2 bottom = input_basic.bottom();
			aktpoly.intersection(bottom, lastIntersection);
			if (lastIntersection.result != Intersection.EMPTY)
			{
				return true;
			}
			BasicLine2 left = input_basic.left();
			aktpoly.intersection(left, lastIntersection);
			if (lastIntersection.result != Intersection.EMPTY)
			{
				return true;
			}
			BasicLine2 top = input_basic.top();
			aktpoly.intersection(top, lastIntersection);
			if (lastIntersection.result != Intersection.EMPTY)
			{
				return true;
			}
			BasicLine2 right = input_basic.right();
			aktpoly.intersection(top, lastIntersection);
			if (lastIntersection.result != Intersection.EMPTY)
			{
				return true;
			}
		}
		return false;
	} // intersects


	//------------------------------------------------------------
	// Shortest path
	//------------------------------------------------------------

	/**
	 * Return shortest path between source and target. Return null if source or
	 * target is inside an obstacle.
	 * 
	 * VERY IMPORTANT!!! USE ORIGINAL SOURCE AND TARGET OBJECTS IF THEY ARE
	 * ALREADY INCLUDED IN THE GRAPH!!!!! DO NOT USE NEW OBJECTS WITH SAME
	 * COORDINATES!!!!
	 * 
	 * @param source
	 *            First point (source)
	 * @param target
	 *            Second point (target)
	 * 
	 * @return Polygon, that represents the way between source and target
	 */
	public Polygon2 shortestPath(
			Point2 source,
			Point2 target)
	{
		if (!isHalfFreeSpace(source))
		{
			return null; // startpoint inside an obstacle
		}
		if (!isHalfFreeSpace(target))
		{
			return null; // target inside an obstacle
		}
		Point2Graph visigraph = extendedVisG(source, target);

		if (!isFreeSpace(source))
		{
			// source liegt also genau am Rand eines Polygons, denn es gilt
			// zuaetzlich: isHalfFreeSpace( source ) == true  ( siehe oben ).

			BasicLine2 closest = closestEdge(source);
			Point2 src = closest.source();
			Segment2 srcSource = new Segment2(src, source);
			Point2 trg = closest.target();
			Segment2 sourceTrg = new Segment2(source, trg);
			visigraph.add(srcSource);
			visigraph.add(sourceTrg);
		}

		if (!isFreeSpace(target))
		{
			// target liegt also genau am Rand eines Polygons, denn es gilt
			// zuaetzlich: isHalfFreeSpace( target ) == true  ( siehe oben ).

			BasicLine2 closest = closestEdge(target);
			Point2 src = closest.source();
			Segment2 srcTarget = new Segment2(src, target);
			Point2 trg = closest.target();
			Segment2 targetTrg = new Segment2(target, trg);
			visigraph.add(srcTarget);
			visigraph.add(targetTrg);
		}
		return visigraph.dijkstra(source, target);

	} // shortestPath


	/**
	 * Shortest path from source to bounding-box. Null if source is not inside
	 * the box. Return null if source is inside an obstacle.
	 * 
	 * VERY IMPORTANT !!! USE ORIGINAL SOURCE OBJECT IF IT IS ALREADY INCLUDED
	 * IN THE GRAPH !!!!!
	 * 
	 * @param source
	 *            The input point
	 * 
	 * @return The polygon representing the shortest way out of the bounding-box
	 */
	public Polygon2 shortestEscapePath(
			Point2 source)
	{
		Rectangle2 bounding_box = getBoundingBox();
		if (bounding_box == null)
		{
			return null;
		}
		float min_x = bounding_box.x;
		float min_y = bounding_box.y;
		float max_x = min_x + bounding_box.width;
		float max_y = min_y + bounding_box.height;

		if (bounding_box.contains(source.x, source.y))
		{
			return null;
		}
		if (source.x == min_x || source.x == max_y || source.y == min_y
				|| source.y == max_y)
		{
			Polygon2 result = new Polygon2();
			result.setOpen();
			result.addPoint(source);
			return result;
		}
		if (!isFreeSpace(source) && !isHalfFreeSpace(source))
		{
			return null; // startpoint inside an obstacle
		}

		Point2Graph visigraph = extendedVisG(source);

		if (isHalfFreeSpace(source))
		{
			BasicLine2 closest = closestEdge(source);
			Point2 src = closest.source();
			Segment2 srcSource = new Segment2(src, source);
			Point2 trg = closest.target();
			Segment2 sourceTrg = new Segment2(source, trg);
			visigraph.add(srcSource);
			visigraph.add(sourceTrg);
		}

		Vector targetPoints = new Vector();
		double minPathLength = Double.POSITIVE_INFINITY;
		Polygon2 result = null;
		Polygon2[] polygons = getInteriorPolygons();
		for (int i = 0; i < polygons.length; i++)
		{
			Polygon2 aktPoly = polygons[i];
			int numOfPoints = (aktPoly.points()).length();
			PolygonAccess pa = new PolygonAccess(aktPoly);
			for (int j = 0; j < numOfPoints; j++)
			{
				Point2 testPoint = pa.nextPoint();
				Point2 rayTarget = new Point2(testPoint.x, (testPoint.y - 1)); // SOUTH
				Ray2 testRay = new Ray2(testPoint, rayTarget);
				Intersection dummy = new Intersection();
				intersection(testRay, dummy);
				if (dummy.result == Intersection.EMPTY)
				{
					Point2 newTarget = new Point2(testPoint.x, min_y);
					targetPoints.addElement(newTarget);
				}
				rayTarget = new Point2(testPoint.x, (testPoint.y + 1)); // NORTH
				testRay = new Ray2(testPoint, rayTarget);
				dummy = new Intersection();
				intersection(testRay, dummy);
				if (dummy.result == Intersection.EMPTY)
				{
					Point2 newTarget = new Point2(testPoint.x, max_y);
					targetPoints.addElement(newTarget);
				}
				rayTarget = new Point2(testPoint.x + 1, testPoint.y); // EAST
				testRay = new Ray2(testPoint, rayTarget);
				dummy = new Intersection();
				intersection(testRay, dummy);
				if (dummy.result == Intersection.EMPTY)
				{
					Point2 newTarget = new Point2(max_x, testPoint.y);
					targetPoints.addElement(newTarget);
				}
				rayTarget = new Point2(testPoint.x - 1, testPoint.y); // WEST
				testRay = new Ray2(testPoint, rayTarget);
				dummy = new Intersection();
				intersection(testRay, dummy);
				if (dummy.result == Intersection.EMPTY)
				{
					Point2 newTarget = new Point2(min_x, testPoint.y);
					targetPoints.addElement(newTarget);
				}
			}

		}

		int cnt = 0;
		while (cnt < targetPoints.size() - 1)
		{
			Point2 toCheck = (Point2) targetPoints.elementAt(cnt);
			int cnt2 = cnt + 1;
			while (cnt2 < targetPoints.size())
			{
				Point2 testMe = (Point2) targetPoints.elementAt(cnt2);
				if (testMe.equals(toCheck))
				{
					targetPoints.removeElementAt(cnt2);
				}
				cnt2++;
			}
			cnt++;
		}

		for (int i = 0; i < targetPoints.size(); i++)
		{
			Point2 toCheck = (Point2) targetPoints.elementAt(i);
			Point2[] sceneVer = vertices();
			for (int j = 0; j < sceneVer.length; j++)
			{
				if (toCheck.equals(sceneVer[j]))
				{
					targetPoints.insertElementAt(sceneVer[j], i);
					targetPoints.removeElementAt(i + 1);
				}
			}
		}

		for (int cou = 0; cou < targetPoints.size(); cou++)
		{
			Point2 nextTarget = (Point2) targetPoints.elementAt(cou);
			if (!source.equals(nextTarget))
			{
				visigraph = extendedVisG(source, nextTarget);
				Polygon2 minSoFar = visigraph.dijkstra(source, nextTarget);
				if (minSoFar.len() < minPathLength)
				{
					result = new Polygon2(minSoFar);
					minPathLength = minSoFar.len();
				}
			}
		}

		return result;
	}


	//------------------------------------------------------------
	// Visibility stuff
	//------------------------------------------------------------

	/**
	 * Diese Methode berechnet den Sichtbarkeitsgraphen und gibt die
	 * Sichtbarkeitskanten <b>ohne</b> die Polygonkanten als Point2Graph zurück.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @return Visiblity graph
	 */
	public Point2Graph visG()
	{
		if (_visGraphDirtyBit == true)
			return _visGraph.getVisG(false);

		_visGraph = new VisGraph(this, useBoundigPolygonInVisG);
		_visGraph.calculate();
		_visGraphDirtyBit = true;

		return _visGraph.getVisG(false);
	}


	//*************************************************************************

	/**
	 * Diese Methode berechnet den Sichtbarkeitsgraphen und gibt die
	 * Sichtbarkeitskanten <b>ohne</b> der Polygonkanten als Point2Graph zurück.
	 * Die Sichtbarkeitskanten zum Startpunkt werden mit ausgegeben.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @param sourcePoint
	 *            The source point
	 * 
	 * @return Visibility graph + visibility edges to the sourcePoint
	 */
	public Point2Graph visG(
			Point2 sourcePoint)
	{
		Point2 s = sourcePoint;
		if (_visGraphDirtyBit == true)
			return _visGraph.getVisG(s, false);

		_visGraph = new VisGraph(this, useBoundigPolygonInVisG);
		_visGraph.calculate();
		_visGraphDirtyBit = true;

		return _visGraph.getVisG(s, false);
	}


	//*************************************************************************

	/**
	 * Diese Methode berechnet den Sichtbarkeitsgraphen und gibt die
	 * Sichtbarkeitskanten <b>ohne</b> der Polygonkanten als Point2Graph
	 * zurueck. Die Sichtbarkeitskanten zum Start- und Endpunkt werden mit
	 * ausgegeben.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @param sourcePoint
	 *            The first point (source)
	 * @param targetPoint
	 *            The second point (target)
	 * 
	 * @return Visibility graph + visibility edges to source and target
	 */
	public Point2Graph visG(
			Point2 sourcePoint,
			Point2 targetPoint)
	{
		Point2 s = sourcePoint;
		Point2 t = targetPoint;
		if (_visGraphDirtyBit == true)
			return _visGraph.getVisG(s, t, false);

		_visGraph = new VisGraph(this, useBoundigPolygonInVisG);
		_visGraph.calculate();
		_visGraphDirtyBit = true;

		return _visGraph.getVisG(s, t, false);
	}


	//*************************************************************************

	/**
	 * Diese Methode berechnet den Sichtbarkeitsgraphen und gibt die
	 * Sichtbarkeitskanten <b>mit</b> den Polygonkanten als Point2Graph zurück.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @return Extended visibility graph
	 */
	public Point2Graph extendedVisG()
	{
		if (_visGraphDirtyBit == true)
			return _visGraph.getVisG(true);

		_visGraph = new VisGraph(this, useBoundigPolygonInVisG);
		_visGraph.calculate();
		_visGraphDirtyBit = true;

		return _visGraph.getVisG(true);
	}


	//*************************************************************************

	/**
	 * Diese Methode berechnet den Sichtbarkeitsgraphen und gibt die
	 * Sichtbarkeitskanten <b>mit</b> den Polygonkanten als Point2Graph zurück.
	 * Die Sichtbarkeitskanten zum Startpunkt werden mit ausgegeben.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @param sourcePoint
	 *            The source point
	 * 
	 * @return Extended visibility graph + visibility edges to source
	 */
	public Point2Graph extendedVisG(
			Point2 sourcePoint)
	{
		Point2 s = sourcePoint;
		if (_visGraphDirtyBit == true)
			return _visGraph.getVisG(s, true);

		_visGraph = new VisGraph(this, useBoundigPolygonInVisG);
		_visGraph.calculate();
		_visGraphDirtyBit = true;

		return _visGraph.getVisG(s, true);
	}


	//*************************************************************************

	/**
	 * Diese Methode berechnet den Sichtbarkeitsgraphen und gibt die
	 * Sichtbarkeitskanten <b>mit</b> den Polygonkanten als Point2Graph zurueck.
	 * Die Sichtbarkeitskanten zum Start- und Endpunkt werden mit ausgegeben.
	 * 
	 * <br>Author: Alexander Tiderko
	 * 
	 * @param sourcePoint
	 *            The first point (source)
	 * @param targetPoint
	 *            The second point (target)
	 * 
	 * @return Extended visibility graph + visibility edges to source and target
	 */
	public Point2Graph extendedVisG(
			Point2 sourcePoint,
			Point2 targetPoint)
	{
		Point2 s = sourcePoint;
		Point2 t = targetPoint;
		if (_visGraphDirtyBit == true)
			return _visGraph.getVisG(s, t, true);

		_visGraph = new VisGraph(this, useBoundigPolygonInVisG);
		_visGraph.calculate();
		_visGraphDirtyBit = true;

		return _visGraph.getVisG(s, t, true);
	}


	//*************************************************************************

	/**
	 * Return the scene's visibility graph.
	 * 
	 * @return Visibility graph of the scene
	 */
	public Point2Graph visGnaiv()
	{
		if (_visGraphDirtyBit == true)
		{
			return _visG;
		}
		else
		{
			Point2[] thePoints = vertices();
			int pointsOfScene = thePoints.length - 1;

			_visG = new Point2Graph();
			_visG.setWeighted();

			int counter = 0;

			while (counter <= pointsOfScene - 1)
			{
				Point2 source = thePoints[counter];
				for (int cnt = counter + 1; cnt <= pointsOfScene; cnt++)
				{
					Point2 target = thePoints[cnt];
					if (isVisible(target, source))
					{
						float x1 = target.x;
						float y1 = target.y;
						float x2 = source.x;
						float y2 = source.y;
						float dx = Math.abs(x1 - x2) / 2;
						float dy = Math.abs(y1 - y2) / 2;
						float testx = 0;
						float testy = 0;
						if (x1 < x2)
						{
							testx = x1 + dx;
						}
						else
						{
							testx = x2 + dx;
						}
						if (y1 < y2)
						{
							testy = y1 + dy;
						}
						else
						{
							testy = y2 + dy;
						}
						Point2 testPoint = new Point2(testx, testy);
						if (isFreeSpace(testPoint))
						{
							_visG.add(target, source);
						}
					}
				}
				counter++;
			}
		}
		_visGraphDirtyBit = true;
		return _visG;
	} // visG


	//*************************************************************************

	/**
	 * Return the scene's visibility graph . Include a source point.
	 * 
	 * @param sourcePoint
	 *            The source point
	 * 
	 * @return Visibility graph of the scene + visibility edges to source
	 */
	public Point2Graph visGnaiv(
			Point2 sourcePoint)
	{
		Point2Graph returnGraph = new Point2Graph();
		returnGraph.setWeighted();
		BasicLine2[] edges = visGnaiv().getAllEdges();

		for (int i = 0; i < edges.length; i++)
			returnGraph.add(edges[i]);

		Point2[] sceneVertices = vertices();
		int no_of_all_vertices = sceneVertices.length;

		boolean knownSource = false;

		for (int i = 0; i < no_of_all_vertices; i++)
		{
			if (sourcePoint == sceneVertices[i])
			{
				knownSource = true;
			}
		}

		if (!knownSource)
		{
			for (int i = 0; i < no_of_all_vertices; i++)
			{
				if (isVisible(sceneVertices[i], sourcePoint))
				{
					returnGraph.add(sceneVertices[i], sourcePoint);
				}
			}
		}
		return returnGraph;
	}


	//*************************************************************************

	/**
	 * Return the scene's visibility graph . Include source and target point.
	 * 
	 * @param sourcePoint
	 *            The source point
	 * @param targetPoint
	 *            The target point
	 * 
	 * @return Visibility graph of the scene + visibility edges to source and
	 *         target
	 */
	public Point2Graph visGnaiv(
			Point2 sourcePoint,
			Point2 targetPoint)
	{
		Point2Graph returnGraph = new Point2Graph();
		returnGraph.setWeighted();

		BasicLine2[] edges = visGnaiv().getAllEdges();

		for (int i = 0; i < edges.length; i++)
			returnGraph.add(edges[i]);

		Point2[] sceneVertices = vertices();
		int no_of_all_vertices = sceneVertices.length;

		boolean knownSource = false;
		boolean knownTarget = false;

		for (int i = 0; i < no_of_all_vertices; i++)
		{
			if (sourcePoint == sceneVertices[i])
			{
				knownSource = true;
			}
			if (targetPoint == sceneVertices[i])
			{
				knownTarget = true;
			}
		}

		if (!knownSource)
		{
			for (int i = 0; i < no_of_all_vertices; i++)
			{
				if (isVisible(sceneVertices[i], sourcePoint))
				{
					returnGraph.add(sceneVertices[i], sourcePoint);
				}
			}
		}

		Segment2 directView = new Segment2(sourcePoint, targetPoint);

		if (!intersects(directView) && (!knownSource || !knownTarget))
		{
			returnGraph.add(directView);
		}

		if (!knownTarget)
		{
			for (int i = 0; i < no_of_all_vertices; i++)
			{
				if (isVisible(sceneVertices[i], targetPoint))
				{
					returnGraph.add(sceneVertices[i], targetPoint);
				}
			}
		}
		return returnGraph;
	}


	//*************************************************************************

	/**
	 * Return the scene's visibility graph . Include all sceneedges.
	 * 
	 * @return Visibility graph of the scene + all edges of the scene
	 */
	public Point2Graph extendedVisGnaiv()
	{
		Point2Graph returnGraph = new Point2Graph();

		returnGraph.setWeighted();

		BasicLine2[] edges = visGnaiv().getAllEdges();

		for (int i = 0; i < edges.length; i++)

			returnGraph.add(edges[i]);

		BasicLine2[] allEdges = edges();
		int numofalledges = numOfAllEdges();
		for (int i = 0; i < numofalledges; i++)
		{
			returnGraph.add(allEdges[i]);
		}
		return returnGraph;
	}


	/**
	 * Return the scene's visibility graph . Include all sceneedges. Include a
	 * sourcepoint.
	 * 
	 * @param source
	 *            The source point
	 * 
	 * @return Visibility graph of the scene + all edges of the scene +
	 *         visibility edges to source
	 */
	public Point2Graph extendedVisGnaiv(
			Point2 source)
	{
		Point2Graph result = visGnaiv(source);
		BasicLine2[] allEdges = edges();
		int numofalledges = numOfAllEdges();
		for (int i = 0; i < numofalledges; i++)
		{
			result.add(allEdges[i]);
		}
		return result;
	}


	/**
	 * Return the scene's visibility graph . Include all sceneedges. Include
	 * source and targetpoint.
	 * 
	 * @param source
	 *            The source point
	 * @param target
	 *            The target point
	 * 
	 * @return Visibility graph of the scene + all edges of the scene +
	 *         visibility edges to source
	 */
	public Point2Graph extendedVisGnaiv(
			Point2 source,
			Point2 target)
	{
		Point2Graph result = visGnaiv(source, target);
		BasicLine2[] allEdges = edges();
		int numofalledges = numOfAllEdges();
		for (int i = 0; i < numofalledges; i++)
		{
			result.add(allEdges[i]);
		}
		return result;
	}


	/**
	 * Calculate all polygon vertices, that are visible from given input point.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return List of all points, that are visible from p
	 */
	public Point2List visiblePoints(
			Point2 p)
	{
		Point2List result = new Point2List();
		Point2[] check = vertices();
		for (int i = 0; i < check.length; i++)
		{
			Point2 testPoint = check[i];
			if (isVisible(testPoint, p))
			{
				result.addPoint(testPoint);
			}
		}
		return result;
	} // visiblePoints


	/**
	 * Calculate all edges (line segments) , that are visible from given input
	 * point.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return Array of segments, that are visible from p
	 */
	public BasicLine2[] visibleEdges(
			Point2 p)
	{
		Vector backup = new Vector();
		numOfVisibleEdges = 0;
		Polygon2[] polygons = getInteriorPolygons();
		for (int i = 0; i < polygons.length; i++)
		{
			Polygon2 nextPolygon = polygons[i];
			int numofedges = nextPolygon.edgeNumber();
			Segment2[] theEdges = nextPolygon.edges();
			for (int j = 0; j < numofedges; j++)
			{
				Segment2 nextEdge = theEdges[j];
				if (isVisible(nextEdge, p))
				{
					numOfVisibleEdges++;
					backup.addElement(nextEdge);
				}
			}
		}
		BasicLine2[] result = new BasicLine2[numOfVisibleEdges];
		for (int k = 0; k < numOfVisibleEdges; k++)
		{
			result[k] = (Segment2) backup.elementAt(k);
		}
		return result;
	} // visibleEdges


	/**
	 * Return last calculated number of all edges (line segments) Return -1 if
	 * nothing has been calculated yet
	 * 
	 * @return Last calculated number of all edges or -1
	 */
	public int lastNumOfVisibleEdges()
	{
		return numOfVisibleEdges;
	}


	/**
	 * Return true, if the given polygon vertex can be seen from the point
	 * viewpoint. The polygon vertex must be contained in the scene!
	 * 
	 * @param polyvertex
	 *            The first point
	 * @param viewpoint
	 *            The point of view
	 * 
	 * @return true, points are visible from each other, false else
	 */
	public boolean isVisible(
			Point2 polyvertex,
			Point2 viewpoint)
	{
		Segment2 testLine = new Segment2(polyvertex, viewpoint);
		intersection(testLine, lastIntersection);
		if (lastIntersection.result == Intersection.EMPTY)
		{
			return true;
		}
		if (lastIntersection.result == Intersection.LIST)
		{
			SimpleList intersectionResult = lastIntersection.list;
			if (intersectionResult.length() != 2)
			{
				return false;
			}
			if (!(((intersectionResult.first()).value()) instanceof Point2))
			{
				return false;
			}
			if (!(((intersectionResult.last()).value()) instanceof Point2))
			{
				return false;
			}
			Point2 p1 = (Point2) (intersectionResult.first()).value();
			Point2 p2 = (Point2) (intersectionResult.last()).value();
			float x1 = p1.x;
			float y1 = p1.y;
			float x2 = p2.x;
			float y2 = p2.y;
			Point2 testPoint = new Point2((x1 + x2) / 2, (y1 + y2) / 2);
			if (!isFreeSpace(testPoint))
			{
				return false;
			}
			else
			{
				return true;
			}

		}
		Point2 test = lastIntersection.point2;
		if (test != null)
		{
			if (test.x == polyvertex.x && test.y == polyvertex.y)
			{
				return true;
			}
		}
		return false;
	} // isVisible


	/**
	 * Return true, if the given polygon edge can be seen from the point
	 * viewpoint. The polygon edge must be contained in the scene!
	 * 
	 * @param polyedge
	 *            One edge of the scene
	 * @param viewpoint
	 *            The point of view
	 * 
	 * @return true, the edge can be seen from the point of view, false else
	 */
	public boolean isVisible(
			Segment2 polyedge,
			Point2 viewpoint)
	{
		Point2 start = polyedge.source();
		Point2 end = polyedge.target();
		Polygon2 clip = new Polygon2();
		clip.addPoint(start);
		clip.addPoint(end);
		clip.addPoint(viewpoint);
		notVisible = false;
		Vector edgesOfInterest = clipEdges(polyedge, viewpoint, start, end,
				clip);
		if (notVisible)
		{
			return false;
		}
		Vector pointsOfInterest = new Vector();
		Point2[] allPoints = vertices();
		int numOfAllPoints = allPoints.length;
		for (int i = 0; i < numOfAllPoints; i++)
		{
			Point2 testPoint = allPoints[i];
			if (clip.inside(testPoint))
			{
				pointsOfInterest.addElement(testPoint);
			}
		}
		if (pointsOfInterest.size() == 0)
		{
			return true;
		}
		for (int j = 0; j < pointsOfInterest.size(); j++)
		{
			Point2 nextPoint = (Point2) pointsOfInterest.elementAt(j);
			Ray2 testRay = new Ray2(viewpoint, nextPoint);
			Vector hitpoints = new Vector();
			boolean rayhit = false;
			for (int k = 0; k < edgesOfInterest.size(); k++)
			{
				Segment2 testEdge = (Segment2) edgesOfInterest.elementAt(k);
				testRay.intersection(testEdge, lastIntersection);
				if (lastIntersection.result != Intersection.EMPTY)
				{
					Point2 s = testEdge.source();
					Point2 t = testEdge.target();
					Point2 i = lastIntersection.point2;
					if (i.equals(s) || i.equals(t))
					{
						hitpoints.addElement(lastIntersection.point2);
					} // if
					else
					{
						rayhit = true;
						break;
					}
				} // if
			} // for
			if (!rayhit)
			{
				Point2 first = (Point2) hitpoints.elementAt(0);
				Ray2 help = new Ray2(viewpoint, first);
				Point2 halfpoint = help
						.intersection(polyedge, lastIntersection);
				Segment2 sourceline = new Segment2(viewpoint, start);
				Segment2 targetline = new Segment2(viewpoint, end);
				Segment2 halfline = new Segment2(viewpoint, halfpoint);
				if (!blocked(polyedge, sourceline, halfline, targetline,
						edgesOfInterest, pointsOfInterest))
				{
					return true;
				}
			}
		} // for

		return false;
	} // isVisible


	//------------------------------------------------------------
	// Point queries
	//------------------------------------------------------------

	/**
	 * Return true, if the given point lies on the boundary of one of the
	 * polygons.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return true, p lies on the boundary of one polygon, false else
	 */
	public boolean isOnBoundary(
			Point2 p)
	{
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			if (aktpoly.distance(p) == 0)
			{
				return true;
			}
		}
		return false;
	} // isOnBoundary


	/**
	 * Return true, iff the given point lies in free space, i.e. outside the
	 * interior polygons, inside the outer polygon (if the scene is closed) but
	 * NOT on the boundary of some polygon.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return true, p lies in free space and not in or on a polygon, false else
	 */
	public boolean isFreeSpace(
			Point2 p)
	{
		Polygon2[] polygons = getInteriorPolygons();
		for (int i = 0; i < polygons.length; i++)
		{
			Polygon2 nextPolygon = polygons[i];
			byte result = nextPolygon.locatePoint(p);
			if (result == Polygon2.POINT_INSIDE
					|| result == Polygon2.POINT_ON_EDGE)
			{
				return false;
			}
		}
		return true;
	} // isFreeSpace


	/**
	 * Return true, if the given point lies in free space, i.e. outside the
	 * interior polygons, inside the outer polygon (if the scene is closed) or
	 * on the boundary of some polygon.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return true, p lies outside or on the boundary of all/one polygon(s),
	 *         false else
	 */
	public boolean isHalfFreeSpace(
			Point2 p)
	{
		Polygon2[] polygons = getInteriorPolygons();
		for (int i = 0; i < polygons.length; i++)
		{
			Polygon2 nextPolygon = polygons[i];
			byte result = nextPolygon.locatePoint(p);
			if (result == Polygon2.POINT_INSIDE)
			{
				return false;
			}
		}
		return true;
	} // isHalfFreeSpace


	/**
	 * Return the point (on polygon boundary), that is closest to the given
	 * point.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return The point on a polygon, that is closest to p
	 */
	public Point2 closestPoint(
			Point2 p)
	{
		Point2 result = null;
		double minDist = Double.POSITIVE_INFINITY;
		Polygon2[] polygons = getInteriorPolygons();
		for (int i = 0; i < polygons.length; i++)
		{
			Polygon2 nextPolygon = polygons[i];
			Point2 testPoint = nextPolygon.closestPoint(p);
			double aktDist = testPoint.distance(p);
			if (aktDist < minDist)
			{
				result = testPoint;
				minDist = aktDist;
			}
		}
		return result;
	} // closestPoint


	/**
	 * Return the polygon vertex, that is closest to the given point.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return The vertex, that is closest to p
	 */
	public Point2 closestVertex(
			Point2 p)
	{
		Point2 result = null;
		double minDist = Double.POSITIVE_INFINITY;
		Polygon2[] polygons = getInteriorPolygons();
		for (int i = 0; i < polygons.length; i++)
		{
			Polygon2 nextPolygon = polygons[i];
			Point2 testPoint = nextPolygon.closestPoint(p,
					Double.POSITIVE_INFINITY);
			double aktDist = testPoint.distance(p);
			if (aktDist < minDist)
			{
				result = testPoint;
				minDist = aktDist;
			}
		}
		return result;
	} // closestVertex


	/**
	 * Return the polygon edge, that is closest to the given point.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return The segment, that is closest to p
	 */
	public Segment2 closestEdge(
			Point2 p)
	{
		Segment2 result = null;
		double minDist = Double.POSITIVE_INFINITY;
		Polygon2[] polygons = getInteriorPolygons();
		for (int i = 0; i < polygons.length; i++)
		{
			Polygon2 nextPolygon = polygons[i];
			int noOfEdges = nextPolygon.edgeNumber();
			Segment2[] edges = nextPolygon.edges();
			for (int j = 0; j < noOfEdges; j++)
			{
				Segment2 nextEdge = edges[j];
				double aktDist = nextEdge.distance(p);
				if (aktDist < minDist)
				{
					result = nextEdge;
					minDist = aktDist;
				}
			}
		}
		return result;
	} // closestEdge


	/**
	 * Return the polygon, that is closest to the given point.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return The polygon, that is closest to p
	 */
	public Polygon2 closestPolygon(
			Point2 p)
	{
		Polygon2 result = null;
		double minDist = Double.POSITIVE_INFINITY;
		Polygon2[] polygons = getInteriorPolygons();
		for (int i = 0; i < polygons.length; i++)
		{
			Polygon2 nextPolygon = polygons[i];
			double aktDist = nextPolygon.distance(p);
			if (aktDist < minDist)
			{
				result = nextPolygon;
				minDist = aktDist;
			}
		}
		return result;
	} // closestPolygon


	/**
	 * Return the square of the minimal distance between the given point and the
	 * polygon boundaries.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return The square of the distance to the closest object of the scene
	 */
	public double squareDistance(
			Point2 p)
	{
		Polygon2 closest = closestPolygon(p);
		return closest.squareDistance(p);
	} // squareDistance


	/**
	 * Return the minimal distance between the given point and the polygon
	 * boundaries.
	 * 
	 * @param p
	 *            The input point
	 * 
	 * @return The distance to the closest object of the scene
	 */
	public double distance(
			Point2 p)
	{
		Polygon2 firstGetClosest = closestPolygon(p);
		Point2 nowClosestPoint = firstGetClosest.closestPoint(p);
		return nowClosestPoint.distance(p);
	} // distance


	//------------------------------------------------------------
	// Transformations
	//------------------------------------------------------------

	/**
	 * Translate all polygons by the given vector.
	 * 
	 * @param vector
	 *            The translation vector
	 */
	public void translate(
			Point2 vector)
	{
		float xdist = vector.x;
		float ydist = vector.y;
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 aktpoly = polygons[cnt];
			SimpleList polyPoints = aktpoly.points();
			for (int cnt1 = 0; cnt1 < polyPoints.length(); cnt1++)
			{
				Point2 nextPoint = (Point2) polyPoints.getValueAt(cnt1);
				nextPoint.x = nextPoint.x + xdist;
				nextPoint.y = nextPoint.y + ydist;
			}
		}

	} // translate


	/**
	 * Transform the scene using the given transformation matrix.
	 * 
	 * @param a
	 *            The tranformation matrix
	 */
	public void transform(
			Matrix33 a)
	{
		Point2[] allVertices = vertices();
		float _00 = a.get(0, 0);
		float _01 = a.get(0, 1);
		float _02 = a.get(0, 2);
		float _10 = a.get(1, 0);
		float _11 = a.get(1, 1);
		float _12 = a.get(1, 2);
		for (int i = 0; i < allVertices.length; i++)
		{
			Point2 nextPoint = allVertices[i];
			float x = nextPoint.x;
			float y = nextPoint.y;
			nextPoint.x = x * _00 + y * _01 + _02;
			nextPoint.y = x * _10 + y * _11 + _12;
		}
	} // transform


	//------------------------------------------------------------
	// Other:
	//------------------------------------------------------------

	/**
	 * Clones the object
	 * 
	 * @return A copy of the object
	 */
	public Object clone()
	{
		Polygon2Scene theClone = new Polygon2Scene();
		Polygon2[] polygons = getInteriorPolygons();
		for (int i = 0; i < polygons.length; i++)
		{
			Polygon2 newClonePoly = new Polygon2(polygons[i]);
			theClone.add(newClonePoly);
		}
		Polygon2 bounding = getBoundingPolygon();
		if (bounding != null)
			theClone.setBoundingPolygon(new Polygon2(bounding));
		return theClone;
	} // clone


	/**
	 * Convert to string
	 * 
	 * @return The scene as a string
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("Polygon2Scene : ");
		Polygon2[] polygons = getInteriorPolygons();
		for (int i = 0; i < polygons.length; i++)
			sb.append(polygons[i].toString());
		return sb.toString();
	} // toString


	//------------------------------------------------------------
	// Interface drawable:
	//------------------------------------------------------------

	/**
	 * Inherited from {@link anja.util.Drawable}
	 * 
	 * @param graphics
	 *            The graphics object to draw in
	 * @param graphicsContext
	 *            The graphics context
	 * 
	 * @see anja.util.Drawable
	 */
	public void draw(
			Graphics2D graphics,
			GraphicsContext graphicsContext)
	{
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 nextPoly = polygons[cnt];
			nextPoly.draw(graphics, graphicsContext);
		}

		Polygon2 bounding_poly = getBoundingPolygon();
		if (bounding_poly != null)
		{
			bounding_poly.draw(graphics, graphicsContext);
		}
	} // draw


	/**
	 * Inherited from {@link anja.util.Drawable}
	 * 
	 * @param box
	 *            The rectangle to check for intersection with
	 * 
	 * @return true, if intersects, false else
	 * 
	 * @see anja.util.Drawable
	 */
	public boolean intersects(
			Rectangle2D box)
	{
		Polygon2[] polygons = getInteriorPolygons();
		for (int cnt = 0; cnt < polygons.length; cnt++)
		{
			Polygon2 nextPoly = polygons[cnt];
			if (nextPoly.intersects(box))
			{
				return true;
			}
		}

		Polygon2 bounding_poly = getBoundingPolygon();
		if (bounding_poly != null)
		{
			if (bounding_poly.intersects(box))
			{
				return true;
			}
		}
		return false;
	} //  intersects


	//************************************************************
	// Private methods
	//************************************************************

	/**
	 * ---
	 * 
	 * @param polyedge
	 *            Segment2
	 * @param viewpoint
	 *            The point of view
	 * @param start
	 *            The starting point
	 * @param end
	 *            The ending point
	 * @param clip
	 * 
	 * @return A Vector object
	 * 
	 */
	private Vector clipEdges(
			Segment2 polyedge,
			Point2 viewpoint,
			Point2 start,
			Point2 end,
			Polygon2 clip)
	{
		Segment2[] allEdges = edges();
		int numOfEdges = numOfAllEdges();
		Vector result = new Vector();
		Segment2 sourceLine = new Segment2(viewpoint, start);
		Segment2 targetLine = new Segment2(viewpoint, end);
		for (int i = 0; i < numOfEdges; i++)
		{
			Segment2 test = allEdges[i];
			Point2 source = test.source();
			Point2 target = test.target();
			if (clip.inside(source) || clip.inside(target))
			{
				result.addElement(test);
			}
			else
			{
				Point2 sourcetest = test.intersection(sourceLine,
						lastIntersection);
				Point2 targettest = test.intersection(targetLine,
						lastIntersection);
				if (sourcetest != null && targettest != null
						&& !test.equals(polyedge))
				{
					notVisible = true;
					return null;
				}
			} // else
		}
		return result;
	}


	/**
	 * ---
	 * 
	 * @param polyedge
	 *            Segment2
	 * @param sourceline
	 *            Segment2
	 * @param halfline
	 *            Segment2
	 * @param targetline
	 *            Segment2
	 * @param edgesOfInterest
	 *            Vector
	 * @param pointsOfInterest
	 *            Vector
	 * 
	 * @return true, false
	 */
	private boolean blocked(
			Segment2 polyedge,
			Segment2 sourceline,
			Segment2 halfline,
			Segment2 targetline,
			Vector edgesOfInterest,
			Vector pointsOfInterest)
	{
		boolean firstblock = false;
		boolean firstpoints = false;
		boolean secondblock = false;
		boolean secondpoints = false;
		for (int x = 0; x < edgesOfInterest.size(); x++)
		{
			Segment2 test = (Segment2) edgesOfInterest.elementAt(x);
			Point2 sourcetest = test.intersection(sourceline, lastIntersection);
			Point2 targettest = test.intersection(halfline, lastIntersection);
			if (sourcetest != null && targettest != null
					&& !test.equals(polyedge))
			{
				secondblock = true;
				//	 firstblock=true; // DAS WAR DIE FALSCHE SEITE  !! :)
				break;
			}
		}
		if (!secondblock)
		{
			Point2 vp = sourceline.source();
			Point2 ht = halfline.target();
			Point2 tg = targetline.target();
			Polygon2 clipcheck = new Polygon2();
			clipcheck.addPoint(vp);
			clipcheck.addPoint(ht);
			clipcheck.addPoint(tg);
			for (int u = 0; u < pointsOfInterest.size(); u++)
			{
				Point2 nextPoint = (Point2) pointsOfInterest.elementAt(u);
				if (clipcheck.inside(nextPoint))
				{
					firstpoints = true;
					break;
				}
			}
		}
		for (int y = 0; y < edgesOfInterest.size(); y++)
		{
			Segment2 test = (Segment2) edgesOfInterest.elementAt(y);
			Point2 sourcetest = test.intersection(halfline, lastIntersection);
			Point2 targettest = test.intersection(targetline, lastIntersection);
			if (sourcetest != null && targettest != null
					&& !test.equals(polyedge))
			{
				//	 secondblock=true;  // Genau wie oben .. :)
				firstblock = true;
				break;
			}
		}
		if (!firstblock)
		{
			Point2 vp = sourceline.source();
			Point2 ht = halfline.target();
			Point2 tg = sourceline.target();
			Polygon2 clipcheck = new Polygon2();
			clipcheck.addPoint(vp);
			clipcheck.addPoint(ht);
			clipcheck.addPoint(tg);
			for (int u = 0; u < pointsOfInterest.size(); u++)
			{
				Point2 nextPoint = (Point2) pointsOfInterest.elementAt(u);
				if (clipcheck.inside(nextPoint))
				{
					secondpoints = true;
					break;
				}
			}
		}
		if ((!firstpoints && !firstblock) || (!secondpoints && !secondblock))
		{
			return false;
		}
		return true;
	}

} // Polygon2Scene
