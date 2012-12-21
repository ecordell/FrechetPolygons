package anja.geom;


import java.awt.Color;
import java.awt.geom.*;

import java.util.Vector;
import java.util.Enumeration;

import java.awt.Graphics2D;
import anja.util.GraphicsContext;

import anja.graph.Graph;
import anja.graph.Vertex;
import anja.graph.Edge;


/**
 * This class computes the shortest watchman route in a rectangular Polygon. The
 * algorithm is described in "Bewegungsplanung fuer Roboter" by Rolf Klein,
 * Elmar Langetepe and Tom Kamphans.
 * 
 * @version 0.2 14.02.07
 * @author Christian Koehn
 */

public class SWRinRecPoly
		implements java.io.Serializable
{

	// *************************************************************************
	// Private constants
	// *************************************************************************

	// Color and size of the door (if shown)
	private static final Color	_DOOR_EDGE_COLOR	= Color.black;
	private static final Color	_DOOR_FILL_COLOR	= Color.green;
	private static final int	_DOOR_FILL_STYLE	= 1;
	private static final float	_DOOR_RADIUS		= 5.0f;

	// Color and size of the triangulation (if shown)
	private static final Color	_TRIAG_EDGE_COLOR	= Color.red;	// Kanten
	private static final Color	_TRIAG_FILL_COLOR	= Color.blue;	// Flaeche
	private static final int	_TRIAG_FILL_STYLE	= 0;			// fuellen
	private static final float	_TRIAG_RADIUS		= 2.0f;		// Radius

	private static final byte	POINT_OUTSIDE		= 3;			// Value of point location

	// ************************************************************************
	// Variables
	// ************************************************************************

	private GraphicsContext		_door_gc;							// Door
	private GraphicsContext		_triag_gc;							// Door

	/* given rectangular polygon */
	private Polygon2			_inputPoly;
	private Polygon2			_workingPoly;
	private Polygon2			_pathPoly;

	/* given startpoint */
	private Point2				_startPoint;

	/* This door is located on the edge of the polygon */
	private Point2				_door;

	/** If an error occours, this string contains the message */
	public String				errorMessage;

	/** contains the computed essential cuts */
	public Segment2[]			cuts;

	private Vector				_triangles;


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * The Constructor needs a rectangular polygon and a startpoint for the swr.
	 * 
	 * @param poly
	 *            Rectangular polygon
	 * @param startpoint
	 *            Startpoint
	 */
	public SWRinRecPoly(
			Polygon2 poly,
			Point2 startpoint)
	{
		if (_isRectangular(poly))
		{
			_inputPoly = new Polygon2(poly); // The original polygon
			_workingPoly = new Polygon2(poly); // A copy of the polygon
		} // if

		else
			_inputPoly = null;

		_startPoint = new Point2(startpoint);
		_door = null;
		_pathPoly = new Polygon2();

		_triangles = new Vector();

		// Look of the door:
		_door_gc = new GraphicsContext();
		_door_gc.setForegroundColor(_DOOR_EDGE_COLOR);
		_door_gc.setFillColor(_DOOR_FILL_COLOR);
		_door_gc.setFillStyle(_DOOR_FILL_STYLE);

		// Look of the door:
		_triag_gc = new GraphicsContext();
		_triag_gc.setForegroundColor(_TRIAG_EDGE_COLOR);
		_triag_gc.setFillColor(_TRIAG_FILL_COLOR);
		_triag_gc.setFillStyle(_TRIAG_FILL_STYLE);

	} // SWRinRecPoly


	// ************************************************************************************************************************************************
	// ************************************************************************************************************************************************

	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * This method computes the essential cuts of the given polygon and
	 * startpoint
	 * 
	 * @param polygon
	 *            Rectangular polygon
	 * @param point
	 *            Startpoint
	 * 
	 * @return Array of the computed cuts
	 */
	public Segment2[] computeCuts(
			Polygon2 polygon,
			Point2 point)
	{

		// Because the VisibilityCuts algorithm is not working well
		// i have to compute all possible cuts from all locations and
		// add the endpoints to our polygon.
		// Fortunately i can make use of the 
		// added points.

		_workingPoly = (Polygon2) _inputPoly.clone(); // A copy of the original Polygon is needed

		_computeAllVertices(); // The extra vertices are added

		VisibilityCuts _viscuts = new VisibilityCuts();
		_viscuts.setInputPolygon(_workingPoly);

		Polygon2 start = new Polygon2();
		start.addPoint(new Point2(point));
		_viscuts.resetStartPoint();
		_viscuts.setStartingPoint(start);

		_viscuts._computeCuts();

		cuts = new Segment2[_viscuts.getLength()];
		cuts = _viscuts.getVisiCuts();

		_door = start.firstPoint();

		// Now computing the essential cuts

		while (!_door.equals(_workingPoly.firstPoint())) // rotate until _door is the first Point
		{
			_workingPoly.addPoint(_workingPoly.firstPoint());
			_workingPoly.removeFirstPoint();
		}

		int anzahlPolyPunkte = _workingPoly.length();
		int anzahlcuts = cuts.length;

		Vector opencuts = new Vector(); // The opened cuts waiting to be closed
		Vector cutsVector = new Vector(); // All Cuts
		Vector essentialcuts = new Vector();// The essential cuts found

		for (int i = 0; i < anzahlcuts; i++)
			cutsVector.add(cuts[i]);

		Segment2 actualcut, cutToRemove;
		Point2 cutpoint1, cutpoint2, polyPoint;

		for (int i = 0; i < anzahlPolyPunkte; i++) // Walk through all points of the polygon
		{
			polyPoint = _workingPoly.firstPoint();
			_workingPoly.addPoint(_workingPoly.firstPoint());
			_workingPoly.removeFirstPoint();

			for (int j = 0; j < cutsVector.size(); j++)
			{
				actualcut = (Segment2) cutsVector.elementAt(j);
				cutpoint1 = actualcut.getLeftPoint();
				cutpoint2 = actualcut.getRightPoint();

				if (cutpoint1.equals(polyPoint) || cutpoint2.equals(polyPoint)) // Cut reached
				{
					if (opencuts.contains(actualcut)) // Cut has to be essential
					{
						essentialcuts.add(actualcut); // Essential cut added

						int toDelete = opencuts.indexOf(actualcut);
						for (int k = 0; k <= toDelete; k++)
						{
							cutToRemove = (Segment2) opencuts.firstElement();
							cutsVector.remove(cutToRemove);
							opencuts.remove(cutToRemove);
						} // for

					} // if 
					else
					// Cut is not essential, so we have another open-cut
					{
						opencuts.add(actualcut);
					} // else 

				} // if

			} // for cuts

		} // for anzahlpolypunkte

		cuts = new Segment2[essentialcuts.size()];

		for (int i = 0; i < essentialcuts.size(); i++)
		{
			cuts[i] = (Segment2) essentialcuts.elementAt(i);
		} // for
		this.cuts = cuts;
		return cuts;
	} // computeVisCuts


	// ************************************************************************************************************************************************

	/**
	 * This method is responsible for computing and drawing the selected step
	 * 
	 * @param g
	 *            The graphics to draw in
	 * @param gc
	 *            The given graphics context
	 * @param step
	 *            The step
	 * 
	 * @return true if computed correctly, false else
	 */
	public boolean drawTool(
			Graphics2D g,
			GraphicsContext gc,
			int step)

	{
		if (step == 0)
			return true; // Nothing to do...

		Polygon2 poly = new Polygon2(_inputPoly);
		Point2 start = new Point2(_startPoint);
		ConvexPolygon2 triang[], triangArr[];

		Segment2[] cuts = computeCuts(poly, start); // compute cuts

		if (step == 1) // Paint the cuts and return
		{
			for (int i = 0; i < cuts.length; i++)
			{
				((Segment2) cuts[i]).draw(g, gc);
			}
			return true;
		}

		Polygon2 cuttedPoly = _cutPoly(new Polygon2(_workingPoly), cuts); // cut off
		cuttedPoly.removeRedundantPoints(); // remove redundant points

		if (step == 2) // Paint cutted poly and return
		{
			cuttedPoly.draw(g, gc);
			return true;
		}

		triang = cuttedPoly.getTriangulation(); // get the triangulation

		if (step == 3) // Paint triangulation and return
		{
			for (int i = 0; i < triang.length; i++)
			{
				((ConvexPolygon2) triang[i]).draw(g, gc);
			}
			return true;
		}

		int startindex = 0; // index of the starttriangle
		Vector triangvec = new Vector(); // vector containing the triangles
		Vector rolledTriangs = new Vector(); // vector containing all triangles of the rolled out polygon

		for (int i = 0; i < triang.length; i++)
		{
			triangvec.add(triang[i]); // Create a vector containing the triangles
			if (triang[i].locatePoint(start) != POINT_OUTSIDE)
			{
				startindex = i; // Get the index of the start-triangle
			}
		}

		ConvexPolygon2 startTriangle = triang[startindex];
		ConvexPolygon2 targetTriangle = triang[startindex];
		ConvexPolygon2 shortStart = triang[startindex]; // startTriangle for shortest Path
		ConvexPolygon2 shortTarget = triang[startindex]; // targetTriangle for shortest Path

		Polygon2 zusammen = new Polygon2(startTriangle);
		Polygon2 polyToAdd = new Polygon2();
		Vector triangsToAdd = new Vector(); // The mirrored triangles to add

		for (int i = 0; i < cuts.length; i++) // construct the rolled out polygon (triangles)
		{
			Segment2[] mirrors = new Segment2[i + 1];
			for (int j = 0; j < mirrors.length; j++)
				mirrors[j] = cuts[j];

			if (i == 0) // path from start to the first cut
			{
				startTriangle = triang[startindex];
				targetTriangle = triang[_inPoly(triang, cuts[0])];
				polyToAdd = _computeRelTriag((Vector) triangvec.clone(),
						startTriangle, targetTriangle, startTriangle); // returns the relevant polygon
				zusammen = polyToAdd;
				triangsToAdd = (Vector) _selTriang(triang, polyToAdd).clone(); // get only the triangles inside the relevant polygon
				triangArr = new ConvexPolygon2[triangsToAdd.size()];
				for (int t = 0; t < triangsToAdd.size(); t++)
					triangArr[t] = (ConvexPolygon2) triangsToAdd.elementAt(t);
				rolledTriangs = _modTriang(triangArr, start); // Modify the start-triangle(s)
				if (rolledTriangs.size() == 0) // only the start-triangle exist
				{
					ConvexPolygon2 add = new ConvexPolygon2();
					add.addPoint(start);
					add.addPoint(cuts[0].getLeftPoint());
					add.addPoint(cuts[0].getRightPoint());
					rolledTriangs.add(add);
				}
				else
				{
					shortStart = (ConvexPolygon2) rolledTriangs.firstElement(); // _modtriang returns the vector with the start as the first element
					ConvexPolygon2[] helpArr = new ConvexPolygon2[rolledTriangs
							.size()];
					for (int t = 0; t < rolledTriangs.size(); t++)
						helpArr[t] = (ConvexPolygon2) rolledTriangs
								.elementAt(t);
					polyToAdd = _computeRelTriag(
							(Vector) rolledTriangs.clone(), shortStart,
							targetTriangle, shortStart);
					rolledTriangs = _selTriang(helpArr, polyToAdd);
				}
				shortStart = (ConvexPolygon2) rolledTriangs.firstElement(); // now we have all triangles and the correct start
			} // if i=0

			else
			// path from one cut to another
			{
				startTriangle = triang[_inPoly(triang, cuts[mirrors.length - 2])];
				targetTriangle = triang[_inPoly(triang,
						cuts[mirrors.length - 1])];
				polyToAdd = _computeRelTriag((Vector) triangvec.clone(),
						startTriangle, targetTriangle, startTriangle);
				triangsToAdd = _selTriang((ConvexPolygon2[]) triang.clone(),
						polyToAdd);
				_reflect(triangsToAdd, mirrors);
				for (int t = 0; t < triangsToAdd.size(); t++)
					rolledTriangs.add(triangsToAdd.elementAt(t));
			} // else         
		} // for all cuts

		// Nearly done..
		// we just need a path from the last cut to the startpoint to close the route

		Segment2[] mirrors = new Segment2[cuts.length + 1]; // the mirrors
		for (int j = 0; j < mirrors.length - 1; j++)
			mirrors[j] = cuts[j];
		mirrors[cuts.length] = cuts[cuts.length - 1];

		startTriangle = triang[_inPoly(triang, cuts[cuts.length - 1])];
		targetTriangle = triang[startindex];

		polyToAdd = _computeRelTriag((Vector) triangvec.clone(), startTriangle,
				targetTriangle, startTriangle);
		triangsToAdd = _selTriang(triang, polyToAdd);

		triangArr = new ConvexPolygon2[triangsToAdd.size()];
		for (int t = 0; t < triangsToAdd.size(); t++)
			triangArr[t] = (ConvexPolygon2) triangsToAdd.elementAt(t);

		triangsToAdd = _modTriang(triangArr, start); // Modify the triangles
		if (triangsToAdd.size() == 0)
		{
			ConvexPolygon2 add = new ConvexPolygon2();
			add.addPoint(start);
			add.addPoint(cuts[cuts.length - 1].getLeftPoint());
			add.addPoint(cuts[cuts.length - 1].getRightPoint());
			triangsToAdd.add(add);
		}

		// now we have got the path.... but we have to reflect it to its right position

		_reflect(triangsToAdd, mirrors);

		triang = new ConvexPolygon2[triangsToAdd.size()];
		for (int p = 0; p < triangsToAdd.size(); p++)
		{
			triang[p] = (ConvexPolygon2) triangsToAdd.elementAt(p);
		}

		// Compute the target Point...

		Line2 mirror, m2;
		Point2 target = start;
		for (int i = 0; i < cuts.length; i++)
		{
			mirror = new Line2(cuts[i].getLeftPoint(), cuts[i].getRightPoint());
			for (int j = i; j < cuts.length; j++)
			{
				m2 = new Line2(cuts[j].getLeftPoint(), cuts[j].getRightPoint());
				mirror.setSource(mirror.source().mirror(m2));
				mirror.setTarget(mirror.target().mirror(m2));
			}
			target = target.mirror(mirror);
		}

		shortTarget = triang[_inPoly(triang, target)];

		for (int t = 0; t < triangsToAdd.size(); t++)
			rolledTriangs.add(triangsToAdd.elementAt(t)); // add the last triangles to the vector

		Circle2 door = new Circle2(_door, _DOOR_RADIUS);

		triangsToAdd = (Vector) rolledTriangs.clone();
		_triangles = triangsToAdd;

		if (step == 4) // Paint rolled out poly and return
		{

			for (int i = 0; i < rolledTriangs.size(); i++)
			{
				((ConvexPolygon2) rolledTriangs.elementAt(i)).draw(g, gc);
			}

			//         shortStart.draw(g, _door_gc);
			//         shortTarget.draw(g, _door_gc);

			door.draw(g, _door_gc);

			Circle2 door4 = new Circle2(target, _DOOR_RADIUS);
			door4.draw(g, _door_gc);

			return true;
		}

		door.draw(g, _door_gc);

		// Now we compute the shortest Path and show it

		triangsToAdd = (Vector) rolledTriangs.clone();

		Point2List shortest = _shortestInTriangles(rolledTriangs, shortStart,
				shortTarget, start, target);
		Point2 pt1;
		Segment2 toDraw3;

		if (step == 5) // Paint shortest path and return
		{
			for (int i = 0; i < triangsToAdd.size(); i++)
			{
				((ConvexPolygon2) triangsToAdd.elementAt(i)).draw(g, gc);
			}

			Circle2 target2 = new Circle2(target, _DOOR_RADIUS);
			target2.draw(g, _door_gc);

			PointsAccess pa = new PointsAccess(shortest);
			pt1 = pa.nextPoint();

			while (pa.hasNextPoint())
			{
				toDraw3 = new Segment2(pa.currentPoint(), pa.nextPoint());
				toDraw3.draw(g, _triag_gc);
			}
			return true;
		}

		shortest = _foldBack(shortest, cuts); // fold back the path
		PointsAccess pa = new PointsAccess(shortest);
		pt1 = pa.nextPoint();

		while (pa.hasNextPoint()) // and draw its components
		{
			toDraw3 = new Segment2(pa.currentPoint(), pa.nextPoint());
			toDraw3.draw(g, _triag_gc);
		}
		return true;
	}// drawTool


	// ************************************************************************************************************************************************

	/**
	 * Returns the triangles
	 * 
	 * @return A Vector object containing all triangles
	 */
	public Vector getTriangles()
	{
		return _triangles;
	} // getTriangles


	// ************************************************************************************************************************************************
	// ************************************************************************************************************************************************
	// ************************************************************************************************************************************************

	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Reflect the triangles by the mirrors
	 * 
	 * @param triangs
	 *            The triangles
	 * @param mirrors
	 *            The mirrors
	 */
	private void _reflect(
			Vector triangs,
			Segment2[] mirrors)
	{
		Line2 actmirror;

		for (int i = 0; i < mirrors.length - 1; i++)
		{
			actmirror = new Line2(mirrors[i].getLeftPoint(),
					mirrors[i].getRightPoint());
			for (int k = i + 1; k < mirrors.length; k++)
			{
				Segment2 cut = mirrors[k];
				Point2 cutp1 = cut.getLeftPoint();
				Point2 cutp2 = cut.getRightPoint();
				cut = new Segment2(cutp1.mirror(actmirror),
						cutp2.mirror(actmirror));
				mirrors[k] = cut;
			}
			for (int j = 0; j < triangs.size(); j++)
			{
				ConvexPolygon2 ref = new ConvexPolygon2();
				PointsAccess pa = new PointsAccess(
						(ConvexPolygon2) triangs.elementAt(j));
				while (pa.hasNextPoint())
				{
					Point2 pt = pa.nextPoint();
					ref.addPoint(pt.mirror(actmirror));
				}
				triangs.remove(j);
				triangs.add(0, ref);
			}
		}
	} // _reflect


	// ************************************************************************************************************************************************

	/**
	 * Returns only the triangles which are inside the given poly as a vector
	 * 
	 * @param triangs
	 *            The triangles
	 * @param poly
	 *            The polygon
	 * 
	 * @return The triangles inside the polygon
	 */
	private Vector _selTriang(
			ConvexPolygon2[] triangs,
			Polygon2 poly)
	{
		Vector insideTriangs = new Vector();
		for (int i = 0; i < triangs.length; i++)
		{
			if (poly.inside(triangs[i].getCenterOfMass()))
			{
				insideTriangs.add((ConvexPolygon2) triangs[i].clone());
			}
		}
		return insideTriangs;
	} // _selTriang


	// ************************************************************************************************************************************************

	/**
	 * Fold back the shortest Path
	 * 
	 * @param path
	 *            The path
	 * @param cuts
	 *            The array of cuts
	 * 
	 * @return The shortest path
	 */
	private Point2List _foldBack(
			Point2List path,
			Segment2[] cuts)
	{
		Point2List remainingPath = new Point2List(path);
		Point2List returnPath = new Point2List();
		Point2List mirrorPath;
		Segment2 cut[] = new Segment2[1]; // actual cut
		Segment2 seg[] = new Segment2[1]; // actual pathpartsegment
		Point2 pt1, pt2, endPoint, toCopy;
		PointsAccess pa;
		Line2 actPath, helpLine;
		Line2 mirrorLine;
		Segment2 helpSeg;
		boolean cutreached = false;
		Intersection schnitt = new Intersection();

		if (cuts.length == 0) // if there are no cuts
			return path;

		returnPath.addPoint(new Point2(remainingPath.firstPoint()));

		for (int i = 0; i < cuts.length; i++) // for all cuts
		{
			mirrorLine = new Line2(cuts[i].getLeftPoint(),
					cuts[i].getRightPoint());
			pt1 = remainingPath.firstPoint();

			remainingPath.removeFirstPoint();
			cutreached = false;

			while (!cutreached && remainingPath.length() > 0)
			{
				pt2 = remainingPath.firstPoint();

				helpSeg = new Segment2(pt1, pt2);
				endPoint = _intersec(cuts[i], helpSeg);

				if (endPoint != null)
				{

					// add intersection to path and reflect the rest
					returnPath.addPoint(new Point2(endPoint));

					PointsAccess pa2 = new PointsAccess(remainingPath);
					mirrorPath = new Point2List();

					while (pa2.hasNextPoint()) // reflect all remaining points
					{

						toCopy = new Point2(pa2.nextPoint());

						mirrorPath.addPoint(toCopy.mirror(mirrorLine));

					}

					remainingPath = new Point2List(mirrorPath);
					remainingPath.insertFront(endPoint);

					returnPath.addPoint(new Point2(remainingPath.firstPoint()));
					cutreached = true;
				} // path over cut

				else
				{
					returnPath.addPoint(new Point2(pt2));
					remainingPath.removeFirstPoint();
				} //else
				pt1 = pt2;

			} // while not cutreached...

		} // for all cuts

		// add the remaining points to the Path...

		PointsAccess pa3 = new PointsAccess(remainingPath);
		while (pa3.hasNextPoint())
			returnPath.addPoint(pa3.nextPoint());

		return returnPath;
	} // _foldBack


	// ************************************************************************************************************************************************

	/**
	 * _shortestInTriangles
	 * 
	 * @param triangvec
	 *            The triangles
	 * @param starttriangle
	 *            The starting triangle
	 * @param targettriangle
	 *            The last triangle
	 * @param start
	 *            The starting point
	 * @param target
	 *            The last point
	 * 
	 * @return The resulting list of points
	 */
	private Point2List _shortestInTriangles(
			Vector triangvec,
			ConvexPolygon2 starttriangle,
			ConvexPolygon2 targettriangle,
			Point2 start,
			Point2 target)
	{

		// Now we have all we need :)
		// We have only path-triangles from start to target without any other triangles...

		ConvexPolygon2 acttriang; // actual triangle
		Segment2 actedge; // actual edge

		Point2List shortest = new Point2List(); // This will be our shortest path

		Point2List upperfunnel = new Point2List();
		Point2List lowerfunnel = new Point2List();

		ConvexPolygon2 acttriangArray[] = new ConvexPolygon2[1];
		Segment2 actdiag = new Segment2();
		Segment2 newdiag = actdiag;
		Segment2 mirror;
		Point2 diagPoint1, diagPoint2, upper, lower;

		PointsAccess pa;

		// IMPORTANT !!!!!!!!!!!!!!!!
		if (triangvec.contains(starttriangle)) // Remove start-triangle from list
			triangvec.remove(starttriangle);
		// IMPORTANT !!!!!!!!!!!!!!!!    

		// Calculate the first diag

		for (int i = 0; i < 3; i++)
		{
			actedge = starttriangle.edges()[i];
			if (!actedge.getLeftPoint().equals(start)
					&& !actedge.getRightPoint().equals(start))
			{
				actdiag = actedge;
				i = 3;
			}
		}

		byte ori = 2, oriup = 2, orilow = 1;

		starttriangle.setOrientation(ori);
		targettriangle.setOrientation(orilow);
		pa = new PointsAccess(starttriangle);
		pa.cyclicNextPoint();

		while (!pa.currentPoint().equals(start))
		{
			pa.cyclicNextPoint();
		}

		shortest.addPoint(start); // first add the start point to the shortest

		upperfunnel.addPoint(start); // initialize the upper funnel
		upperfunnel.addPoint(pa.getSucc());

		lowerfunnel.addPoint(start); // initialize the lower funnel
		lowerfunnel.addPoint(pa.getPrec());

		int max = 2 * triangvec.size();
		int counter = 0;

		while (!triangvec.isEmpty() && counter < max) // walking through all triangles
		{
			counter++;

			for (int i = 0; i < triangvec.size(); i++) // we cannot guarantee the order of the triangles...
			{
				acttriang = (ConvexPolygon2) triangvec.elementAt(i);
				acttriangArray[0] = acttriang;
				if (_inPoly(acttriangArray, actdiag) == 0) // next triangle found
				{ // acttriang is the next triangle with the same diag as the current
					newdiag = null;
					boolean found = false;
					for (int j = 0; j < 3; j++)
					{
						actedge = acttriang.edges()[j];
						if (actedge.getLeftPoint().equals(target))
						{
							found = true;
						}

						mirror = new Segment2(actedge.target(),
								actedge.source());
						Point2 center = actedge.center();
						Vector toTest = new Vector(triangvec);
						toTest.remove(acttriang);

						if (_inPoly(toTest, actedge) != -1
								&& !actedge.equals(actdiag)
								&& !mirror.equals(actdiag)) // diag is NOT on poly-edge
							newdiag = actedge;
					} //for

					if (acttriang.equals(targettriangle) || found) // Target reached
					{
						lowerfunnel = _computeFunnel(lowerfunnel, target,
								orilow);
						if (lowerfunnel == null)
							lowerfunnel = _computeFunnel(upperfunnel, target,
									oriup);
						if (lowerfunnel == null)
						{
							lowerfunnel = new Point2List();
							lowerfunnel.addPoint(shortest.lastPoint());
							lowerfunnel.addPoint(target);
						}

						upperfunnel = _computeFunnel(upperfunnel, target, oriup);

						if (upperfunnel == null)
							upperfunnel = _computeFunnel(lowerfunnel, target,
									orilow);
						if (upperfunnel == null)
						{
							upperfunnel = new Point2List();
							upperfunnel.addPoint(shortest.lastPoint());
							upperfunnel.addPoint(target);
						}

						// only a few computations are required...

						PointsAccess paUpper, paLower;
						paUpper = new PointsAccess(upperfunnel);
						paLower = new PointsAccess(lowerfunnel);
						Point2 pt1, pt2;
						pt1 = paUpper.cyclicNextPoint(); // start
						pt1 = paUpper.cyclicNextPoint(); // next upper
						pt2 = paLower.cyclicNextPoint(); // start
						pt2 = paLower.cyclicNextPoint(); // next lower

						while (pt1 != null && pt2 != null && pt1.equals(pt2)) // common funnel-point
						{
							shortest.addPoint(pt1);
							paUpper.cyclicPrevPoint();
							paUpper.removePoint();
							paLower.cyclicPrevPoint();
							paLower.removePoint();

							pt1 = paUpper.cyclicNextPoint(); // next upper
							pt2 = paLower.cyclicNextPoint(); // next lower                  
							pt1 = paUpper.cyclicNextPoint(); // next upper
							pt2 = paLower.cyclicNextPoint(); // next lower
						} // computing the new funnels and shortest

						shortest.removeLastPoint(); // the target Point is twice at the end

						return shortest;

					}
					// Target not reached yet!

					if (newdiag == null)
					{ // ERROR!
						errorMessage = "Shortest Path not found!";
						return shortest;
					}

					diagPoint1 = newdiag.getLeftPoint();
					diagPoint2 = newdiag.getRightPoint();
					upper = upperfunnel.lastPoint();
					lower = lowerfunnel.lastPoint();

					if (diagPoint1.equals(upper) || diagPoint2.equals(upper))
					{
						if (diagPoint1.equals(upper)) // diagPoint2 is next point in lower funnel
						{
							lowerfunnel = _computeFunnel(lowerfunnel,
									diagPoint2, orilow);
							if (lowerfunnel == null)
								lowerfunnel = _computeFunnel(upperfunnel,
										diagPoint2, oriup);
							if (lowerfunnel == null)
							{
								lowerfunnel = new Point2List();
								lowerfunnel.addPoint(shortest.lastPoint());
								lowerfunnel.addPoint(diagPoint2);
							}
						} // if diag1 is upper
						else
						{
							lowerfunnel = _computeFunnel(lowerfunnel,
									diagPoint1, orilow);
							if (lowerfunnel == null)
								lowerfunnel = _computeFunnel(upperfunnel,
										diagPoint1, oriup);
							if (lowerfunnel == null)
							{
								lowerfunnel = new Point2List();
								lowerfunnel.addPoint(shortest.lastPoint());
								lowerfunnel.addPoint(diagPoint1);
							}
						}
					}

					else
					// Point to add is on the lower funnel
					{
						if (diagPoint1.equals(lower)) // diagPoint2 is next point in upper funnel
						{
							upperfunnel = _computeFunnel(upperfunnel,
									diagPoint2, oriup);
							if (upperfunnel == null)
								upperfunnel = _computeFunnel(lowerfunnel,
										diagPoint2, orilow);
							if (upperfunnel == null)
							{
								upperfunnel = new Point2List();
								upperfunnel.addPoint(shortest.lastPoint());
								upperfunnel.addPoint(diagPoint2);
							}
						} // if diag1 is lower
						else
						{
							upperfunnel = _computeFunnel(upperfunnel,
									diagPoint1, oriup);
							if (upperfunnel == null)
								upperfunnel = _computeFunnel(lowerfunnel,
										diagPoint1, orilow);
							if (upperfunnel == null)
							{
								upperfunnel = new Point2List();
								upperfunnel.addPoint(shortest.lastPoint());
								upperfunnel.addPoint(diagPoint1);
							}
						}
					}

					// arrange the new funnels
					PointsAccess paUpper, paLower;
					paUpper = new PointsAccess(upperfunnel);
					paLower = new PointsAccess(lowerfunnel);
					Point2 pt1, pt2;
					pt1 = paUpper.cyclicNextPoint(); // start
					pt1 = paUpper.cyclicNextPoint(); // next upper
					pt2 = paLower.cyclicNextPoint(); // start
					pt2 = paLower.cyclicNextPoint(); // next lower

					while (pt1.equals(pt2)) // common funnel point
					{
						shortest.addPoint(pt1);
						paUpper.cyclicPrevPoint();
						paUpper.removePoint();
						paLower.cyclicPrevPoint();
						paLower.removePoint();

						pt1 = paUpper.cyclicNextPoint(); // next upper
						pt2 = paLower.cyclicNextPoint(); // next lower                  
						pt1 = paUpper.cyclicNextPoint(); // next upper
						pt2 = paLower.cyclicNextPoint(); // next lower
					} // computing the new funnels and shortest

					triangvec.remove(acttriang);
					i--;
				} // if next triangle found           
			}
			actdiag = newdiag;
		}
		return shortest;
	} // _shortestInTriangles


	/**
	 * Compute the funnel
	 * 
	 * @param funnel
	 *            The momentary list of points = the momentary funnel
	 * @param newPoint
	 *            The new point
	 * @param ori
	 *            The orientation
	 * 
	 * @return The resulting list of points
	 */
	private Point2List _computeFunnel(
			Point2List funnel,
			Point2 newPoint,
			byte ori)
	{
		Point2List returnFunnel = new Point2List((Point2List) funnel.clone());
		returnFunnel.addPoint(newPoint);
		PointsAccess pa = new PointsAccess(returnFunnel);
		pa.setOrientation(ori);
		pa.cyclicPrevPoint(); // last Point
		pa.cyclicPrevPoint(); // Point before last Point
		while (returnFunnel.length() > 2)
			if (pa.isReflex())
			{
				pa.removePoint();
				pa.cyclicPrevPoint();
			}
			else
			// Edge is reflex, so we can return the funnel
			{
				return returnFunnel;
			}
		return null; // The funnel is no longer existent, so null is returned
	} // _computeFunnel


	// ************************************************************************************************************************************************

	/**
	 * Gets an array of triangles and two points Modifys the triangles
	 * containing the points and returns the resulting triangles as a vector
	 * 
	 * @param triangulation
	 *            The triangulation
	 * @param start
	 *            One point
	 * 
	 * @return The resulting triangles
	 */
	private Vector _modTriang(
			ConvexPolygon2[] triangulation,
			Point2 start)
	{

		ConvexPolygon2 starttriangle = new ConvexPolygon2();
		ConvexPolygon2 acttriang = new ConvexPolygon2();
		ConvexPolygon2 triangcopy = new ConvexPolygon2();

		boolean startfound = false;
		Vector triangvec = new Vector(); // and put it into a vector
		int k = 0;

		for (int i = 0; i < triangulation.length; i++) // search for the start and target triangles and modify them
		{
			acttriang = triangulation[i];
			triangcopy = (ConvexPolygon2) acttriang.clone();
			if (acttriang.locatePoint(start) != POINT_OUTSIDE) // Modify the triangle so that the startpoint is a vertex
			{
				PointsAccess pa = new PointsAccess(acttriang); // New Points Access
				while (pa.hasNextPoint()) // check if point is on a corner
				{
					if (pa.nextPoint().equals(start)) // point is on a corner
					{
						Segment2 opposite = new Segment2();
						for (int t = 0; t < 3; t++)
						{
							if (!start.inSegment(
									acttriang.edges()[t].getLeftPoint(),
									acttriang.edges()[t].getRightPoint()))
								opposite = acttriang.edges()[t];
						}
						ConvexPolygon2 help = new ConvexPolygon2(); // just for temporary removing the actual triangle
						help.addPoint(0, 0);
						help.addPoint(0, 0);
						help.addPoint(0, 0);
						triangulation[i] = help;
						if (_inPoly(triangulation, opposite) != -1) // found opposite
						{
							starttriangle = (ConvexPolygon2) acttriang.clone();
							triangvec.add(0, starttriangle);
						}
						triangulation[i] = acttriang;
						startfound = true;
					} // if
				} // while

				Point2 first, second, third;
				Point2List toAdd;
				Segment2 check;

				first = acttriang.firstPoint();
				acttriang.removeFirstPoint();
				second = acttriang.firstPoint();
				acttriang.removeFirstPoint();
				third = acttriang.firstPoint();

				check = new Segment2(first, second);
				if (_inPoly(triangulation, check) != -1 && !startfound) // The acttriang is no triangle anymore... so we can search the rest
				{
					starttriangle = new ConvexPolygon2();
					starttriangle.addPoint(first);
					starttriangle.addPoint(second);
					starttriangle.addPoint(start);
					triangvec.add(0, starttriangle);
				}

				check = new Segment2(second, third);
				if (_inPoly(triangulation, check) != -1 && !startfound)
				{
					starttriangle = new ConvexPolygon2();
					starttriangle.addPoint(second);
					starttriangle.addPoint(third);
					starttriangle.addPoint(start);
					triangvec.add(0, starttriangle);
				}

				check = new Segment2(third, first);
				if (_inPoly(triangulation, check) != -1 && !startfound)
				{
					starttriangle = new ConvexPolygon2();
					starttriangle.addPoint(third);
					starttriangle.addPoint(first);
					starttriangle.addPoint(start);
					triangvec.add(0, starttriangle);
				}
				triangulation[i] = triangcopy; // recreate the degenerated triangle
			} // if
			else
				triangvec.add(triangulation[i]);
		}
		return triangvec; // returns the vector
	} // _modTriang


	// ************************************************************************************************************************************************

	/**
	 * Completes a triangulation in case of collinear points the triangulation
	 * is not complete.
	 * 
	 * @param poly
	 *            The polygon
	 * @param incompleteTriang
	 *            The momentary triangulation
	 */
	private void _completeTriang(
			Polygon2 poly,
			Vector incompleteTriang)
	{

		poly.setOrientation((byte) 2);
		PointsAccess pa = new PointsAccess(poly.getReflexVertices());
		ConvexPolygon2 acttriang;
		ConvexPolygon2 newtriang1 = new ConvexPolygon2();
		ConvexPolygon2 newtriang2 = new ConvexPolygon2();
		Segment2 actedge;
		Point2 third, first, second;
		while (pa.hasNextPoint())
		{
			Point2 pt = pa.nextPoint();
			if (true)
			{

				for (int i = 0; i < incompleteTriang.size(); i++)
				{
					acttriang = (ConvexPolygon2) incompleteTriang.elementAt(i);
					for (int j = 0; j < 3; j++)
					{
						newtriang1 = new ConvexPolygon2();
						newtriang2 = new ConvexPolygon2();
						actedge = (Segment2) acttriang.edges()[j].clone();
						third = acttriang.firstPoint();
						PointsAccess pa2 = new PointsAccess(acttriang);
						first = pa2.nextPoint();
						second = pa2.nextPoint();
						third = pa2.nextPoint();
						if (third.isCollinear(second, first))
						{
							incompleteTriang.remove(acttriang);
							i--;
							j = 3;
						}
						else if (pt.inSegment(actedge.getRightPoint(),
								actedge.getLeftPoint())
								&& !actedge.getLeftPoint().equals(pt)
								&& !actedge.getRightPoint().equals(pt)) // We have to add a diagonal
						{
							if (j == 0)
							{
								newtriang1.addPoint(pt);
								newtriang1.addPoint(acttriang.edges()[1]
										.getLeftPoint());
								newtriang1.addPoint(acttriang.edges()[1]
										.getRightPoint());

								newtriang2.addPoint(pt);
								newtriang2.addPoint(acttriang.edges()[2]
										.getLeftPoint());
								newtriang2.addPoint(acttriang.edges()[2]
										.getRightPoint());
							} // if
							else if (j == 1)
							{
								newtriang1.addPoint(pt);
								newtriang1.addPoint(acttriang.edges()[0]
										.getLeftPoint());
								newtriang1.addPoint(acttriang.edges()[0]
										.getRightPoint());

								newtriang2.addPoint(pt);
								newtriang2.addPoint(acttriang.edges()[2]
										.getLeftPoint());
								newtriang2.addPoint(acttriang.edges()[2]
										.getRightPoint());
							} // if
							else
							{
								newtriang1.addPoint(pt);
								newtriang1.addPoint(acttriang.edges()[0]
										.getLeftPoint());
								newtriang1.addPoint(acttriang.edges()[0]
										.getRightPoint());

								newtriang2.addPoint(pt);
								newtriang2.addPoint(acttriang.edges()[1]
										.getLeftPoint());
								newtriang2.addPoint(acttriang.edges()[1]
										.getRightPoint());
							} // else

							incompleteTriang.remove(acttriang);
							i--;
							if (!incompleteTriang.contains(newtriang1))
								incompleteTriang
										.addElement((ConvexPolygon2) newtriang1
												.clone());
							if (!incompleteTriang.contains(newtriang2))
								incompleteTriang
										.addElement((ConvexPolygon2) newtriang2
												.clone());

						} // if adding a diagonal
					} // for all edges of the triang
				} // for all triangles
			} // if pt is reflex
		} // while
		poly.setOrientation((byte) 1); // Sets the orientation back
	} // _completeTriang


	// ************************************************************************************************************************************************

	/**
	 * AddmirrorPoly - NOT used anymore!
	 * 
	 * @param startPoly
	 *            Polygon2
	 * @param mirrors
	 *            Segment2[]
	 * @param mirrorToAdd
	 *            Polygon2
	 * 
	 * @return Polygon2
	 */
	private Polygon2 _addMirrorPoly(
			Polygon2 startPoly,
			Segment2[] mirrors,
			Polygon2 mirrorToAdd)
	{
		Point2 point, startVertex, m1, m2;

		byte ori = 1;
		byte ori2 = 2;
		startPoly.setOrientation(ori);

		m1 = new Point2(mirrors[0].getLeftPoint());
		m2 = new Point2(mirrors[0].getRightPoint());

		for (int i = 0; i < mirrors.length - 1; i++) // Computing the mirror points for all the cuts
		{

			m1 = mirrors[i].getLeftPoint();
			m2 = mirrors[i].getRightPoint();

			Line2 mirrorLine = new Line2(m1, m2);
			Polygon2 polyToAdd = new Polygon2();

			for (int j = i + 1; j < mirrors.length; j++)
			{
				Segment2 cut = mirrors[j];
				Point2 cutp1 = cut.getLeftPoint();
				Point2 cutp2 = cut.getRightPoint();
				cut = new Segment2(cutp1.mirror(mirrorLine),
						cutp2.mirror(mirrorLine));
				mirrors[j] = cut;
			}

			PointsAccess pa = new PointsAccess(mirrorToAdd);
			while (pa.hasNextPoint())
			{
				point = pa.cyclicNextPoint();
				point = point.mirror(mirrorLine);
				polyToAdd.insertFront((Point2) point.clone());
			}

			mirrorToAdd = (Polygon2) polyToAdd.clone();

		} // for all mirrors

		startVertex = startPoly.firstPoint();

		while (startVertex.equals(m1) || startVertex.equals(m2)) // if start or endpoint is at the cut
		{ // we are ending in a mess.... so
			startPoly.removeFirstPoint(); // we fly over these points
			startPoly.addPoint(startVertex);
			startVertex = startPoly.firstPoint();
		}

		while (!startVertex.equals(m1) && !startVertex.equals(m2))
		{
			startPoly.removeFirstPoint();
			startPoly.addPoint(startVertex);
			startVertex = startPoly.firstPoint();
		}

		startPoly.removeFirstPoint();
		startPoly.addPoint(startVertex);

		startVertex = mirrorToAdd.firstPoint(); // now the other polygon can be added to the end

		while (!startVertex.equals(startPoly.lastPoint()))
		{
			mirrorToAdd.removeFirstPoint();
			mirrorToAdd.addPoint(startVertex);
			startVertex = mirrorToAdd.firstPoint();
		}

		if (!startPoly.firstPoint().equals(mirrorToAdd.lastPoint())) // wrong orientation
		{
			if (mirrorToAdd.getOrientation() == ori)
				mirrorToAdd.setOrientation(ori2);
			else
				mirrorToAdd.setOrientation(ori);
		}

		mirrorToAdd.removeFirstPoint();
		mirrorToAdd.removeLastPoint();

		Polygon2 returnPoly = (Polygon2) startPoly.clone();

		returnPoly.concat(mirrorToAdd);

		return returnPoly;

	} // addMirrorPoly


	// ************************************************************************************************************************************************

	/**
	 * Returns the index of the containing triangle
	 * 
	 * @param trianglesToTest
	 *            The triangles
	 * @param segmentToCheck
	 *            The segment
	 * 
	 * @return The index of the triangle
	 */
	private int _inPoly(
			ConvexPolygon2[] trianglesToTest,
			Segment2 segmentToCheck)
	{
		Segment2 mirror = new Segment2(segmentToCheck.target(),
				segmentToCheck.source());
		for (int i = 0; i < trianglesToTest.length; i++) // for all Triangles
		{
			Segment2[] edges = trianglesToTest[i].edges();

			for (int j = 0; j < edges.length; j++)
			{
				if (edges[j].equals(segmentToCheck) || edges[j].equals(mirror))
				{
					return i;
				}
			} // for
		} // for
		return -1;
	} // _inPoly


	// ************************************************************************************************************************************************

	/**
	 * Returns the index of the containing triangle
	 * 
	 * @param trianglesToTest
	 *            The triangles
	 * @param segmentToCheck
	 *            The segment
	 * 
	 * @return The index of the triangle
	 */
	private int _inPoly(
			Vector trianglesToTest,
			Segment2 segmentToCheck)
	{
		ConvexPolygon2[] triangles = new ConvexPolygon2[trianglesToTest.size()];

		for (int i = 0; i < trianglesToTest.size(); i++) // for all Triangles
		{
			triangles[i] = (ConvexPolygon2) trianglesToTest.elementAt(i);
		} // for
		return _inPoly(triangles, segmentToCheck);
	} // _inPoly


	// ************************************************************************************************************************************************

	/**
	 * Returns the index of the containing triangle
	 * 
	 * @param trianglesToTest
	 *            The triangles
	 * @param pointToCheck
	 *            The point
	 * 
	 * @return The index of the triangle
	 */
	private int _inPoly(
			ConvexPolygon2[] trianglesToTest,
			Point2 pointToCheck)
	{
		for (int i = 0; i < trianglesToTest.length; i++) // for all Triangles
		{
			Segment2[] edges = trianglesToTest[i].edges();

			for (int j = 0; j < edges.length; j++)
			{
				if (pointToCheck.inSegment(edges[j].getLeftPoint(),
						edges[j].getRightPoint()))
				{
					return i;
				}
			} // for
		} // for
		return -1;
	} // _inPoly


	// ************************************************************************************************************************************************

	/**
	 * Returns the intersection of two segments if exists, otherwise null
	 * 
	 * @param seg1
	 *            Segment 1
	 * @param seg2
	 *            Segment 2
	 * 
	 * @return The intersection (Point2), or null
	 */
	private Point2 _intersec(
			Segment2 seg1,
			Segment2 seg2)
	{

		Point2 interPoint1, interPoint2;
		Line2 line1, line2;
		Intersection schnitt = new Intersection();

		line1 = new Line2(seg1.getLeftPoint(), seg1.getRightPoint());
		line2 = new Line2(seg2.getLeftPoint(), seg2.getRightPoint());

		interPoint1 = new Point2();

		schnitt.set(interPoint1);
		interPoint1 = seg1.intersection(line2, schnitt);

		interPoint2 = new Point2();

		schnitt.set(interPoint2);
		interPoint2 = seg2.intersection(line1, schnitt);

		if (interPoint1 != null && interPoint2 != null)
			return interPoint1;

		return null;

	} // _intersec


	// ************************************************************************************************************************************************

	/**
	 * Cuts the Polygon by the given cuts
	 * 
	 * @param polyToCut
	 *            The polygon
	 * @param toCutAt
	 *            The array of cuts
	 * 
	 * @return The resulting polygon
	 */
	private Polygon2 _cutPoly(
			Polygon2 polyToCut,
			Segment2[] toCutAt)
	{
		Point2 actualPoint, cut1, cut2, toRemove;
		Point2 pt = new Point2();
		Point2 help = new Point2();
		Segment2 actualcut;
		Segment2 helpcut = new Segment2();
		Segment2 intercut = new Segment2();

		while (!polyToCut.firstPoint().equals(_door)) // Startpoint is firstPoint
		{
			polyToCut.addPoint(polyToCut.firstPoint());
			polyToCut.removeFirstPoint();
		}

		Vector cutsvec = new Vector();
		for (int i = 0; i < toCutAt.length; i++)
		{
			cutsvec.add(toCutAt[i]);
		}

		while (!cutsvec.isEmpty())
		{
			//rotate polypoints until cut is found
			actualcut = (Segment2) cutsvec.firstElement();
			cut1 = actualcut.getRightPoint();
			cut2 = actualcut.getLeftPoint();

			int round = polyToCut.length();
			actualPoint = polyToCut.firstPoint();

			boolean secondround = false;
			boolean thirdround = false;

			for (int i = 0; i < round; i++)
			{
				polyToCut.addPoint(actualPoint);
				polyToCut.removeFirstPoint();
				actualPoint = polyToCut.firstPoint();
				secondround = false;
				thirdround = false;

				if (actualPoint.equals(cut1) || actualPoint.equals(cut2)) // cutting begins
				{
					polyToCut.addPoint(actualPoint);
					// cut until intersection is reached
					for (int j = 1; j < cutsvec.size(); j++)
					{
						if ((pt = _intersec((Segment2) cutsvec.elementAt(j),
								actualcut)) != null) // intersection of two cuts
						{
							if (actualPoint.equals(cut1))
							{
								// cut has to be cut
								Segment2 newcut = new Segment2(cut1, pt);
								_changeSeg(toCutAt, actualcut, newcut);
								actualcut = newcut;
							}
							else
							{
								// cut has to be cut
								Segment2 newcut = new Segment2(cut2, pt);
								_changeSeg(toCutAt, actualcut, newcut);
								actualcut = newcut;
							}
							intercut = ((Segment2) cutsvec.elementAt(j)); // the other cut
							cut1 = intercut.getRightPoint();
							cut2 = intercut.getLeftPoint();

							help = (Point2) pt.clone();
							helpcut = (Segment2) intercut.clone();

							polyToCut.addPoint((Point2) pt.clone());
							if (cutsvec.size() > (j + 1)
									&& (pt = _intersec(
											(Segment2) cutsvec.elementAt(j),
											(Segment2) cutsvec.elementAt(j + 1))) != null) // again...
							{
								polyToCut.addPoint((Point2) pt.clone());
								intercut = ((Segment2) cutsvec.elementAt(j + 1));
								cut1 = intercut.getRightPoint();
								cut2 = intercut.getLeftPoint();
								cutsvec.remove(j + 1);
								secondround = true;
								thirdround = true;
								j = cutsvec.size(); // not more than 3 cuts possible...
							}
							else
							{
								secondround = true;
								cutsvec.remove(j);
							} // else
						} // if intersection
					} // for j=1 in cutsvec

					polyToCut.removeFirstPoint();
					toRemove = polyToCut.firstPoint();
					while (!toRemove.equals(cut1) && !toRemove.equals(cut2)) //cutting until end
					{
						polyToCut.removeFirstPoint();
						toRemove = polyToCut.firstPoint();
					} //while

					if (secondround)
					{
						Segment2 newcut = new Segment2();
						if (toRemove.equals(cut1))
						{
							// Der Cut muss abgeschnitten werden
							if (thirdround)
								newcut = new Segment2(cut2, pt);
							else
								newcut = new Segment2(cut2, help);
							_changeSeg(toCutAt, intercut, newcut);
						}
						else
						{
							// Der Cut muss abgeschnitten werden
							if (thirdround)
								newcut = new Segment2(cut1, pt);
							else
								newcut = new Segment2(cut1, help);
							_changeSeg(toCutAt, intercut, newcut);
						}

						polyToCut.removeFirstPoint();
						toRemove = polyToCut.firstPoint();
					}

					if (thirdround)
					{
						Segment2 newcut = new Segment2(help, pt);
						_changeSeg(toCutAt, helpcut, newcut);
					}
					while (secondround && !toRemove.equals(cut1)
							&& !toRemove.equals(cut2)) //cutting until end
					{
						polyToCut.removeFirstPoint();
						toRemove = polyToCut.firstPoint();
					} //while

					i = round;
				} // if
			} // for
			cutsvec.remove(0);
		} // while
		return polyToCut;
	} // _cutPoly


	// ************************************************************************************************************************************************

	/**
	 * Compute relevant triangles in the path from start to target
	 * 
	 * works like "breadth-first search"
	 * 
	 * @param triangulation
	 *            The triangles
	 * @param start
	 *            The first triangle
	 * @param target
	 *            the last triangle
	 * @param current
	 *            The polygon
	 * 
	 * @return The result, the path
	 * 
	 */
	private Polygon2 _computeRelTriag(
			Vector triangulation,
			ConvexPolygon2 start,
			ConvexPolygon2 target,
			Polygon2 current)
	{

		if (triangulation.contains(start)) // Remove start-triangle from list
			triangulation.remove(start);

		Vector triangulationClone = new Vector((Vector) triangulation.clone());
		ConvexPolygon2 toCheck = new ConvexPolygon2();
		Polygon2 currentClone = (Polygon2) current.clone();
		ConvexPolygon2 targetClone = (ConvexPolygon2) target.clone();

		if (start.equals(target)) // Starttriangle is Targettriangle
			return start;

		for (int i = 0; i < triangulationClone.size(); i++)
		{
			toCheck = (ConvexPolygon2) triangulationClone.elementAt(i);
			ConvexPolygon2 toCheckClone = new ConvexPolygon2(
					(ConvexPolygon2) toCheck.clone());

			Segment2 commonEdge = _checkNeighbour(start, toCheck);
			if (commonEdge != null) // append the triag and call function again
			{
				Polygon2 otherOrient = new Polygon2(toCheck);
				if (toCheck.getOrientation() == 1)
				{
					byte ori = 2;
					otherOrient.setOrientation(ori);
				}
				else
				{
					byte ori = 1;
					otherOrient.setOrientation(ori);
				}
				if (target.equals(toCheck) || target.equals(otherOrient)) // We found a way from start to target
				{
					int counter = 0;
					Point2 vertexToAdd = new Point2(toCheck.firstPoint());
					while ((vertexToAdd.equals(commonEdge.getRightPoint()) || vertexToAdd
							.equals(commonEdge.getLeftPoint())) && counter < 5)
					{
						counter++;
						toCheck.removeFirstPoint();
						toCheck.addPoint(vertexToAdd);
						vertexToAdd = toCheck.firstPoint();
					}
					currentClone.insert(commonEdge.getRightPoint(),
							commonEdge.getLeftPoint(), vertexToAdd);
					return currentClone;
				}
				// insert Vertex
				int counter = 0;
				Point2 vertexToAdd = new Point2(toCheck.firstPoint());
				while ((vertexToAdd.equals(commonEdge.getRightPoint()) || vertexToAdd
						.equals(commonEdge.getLeftPoint())) && counter < 5)
				{
					counter++;
					toCheck.removeFirstPoint();
					toCheck.addPoint(vertexToAdd);
					vertexToAdd = toCheck.firstPoint();
				}

				Polygon2 duplpoly = new Polygon2(
						(Polygon2) currentClone.clone());
				duplpoly.insert(commonEdge.getRightPoint(),
						commonEdge.getLeftPoint(), vertexToAdd);

				triangulationClone.remove(toCheck); // remove added triangle and 
				i--; // therefore decrease iterator

				Polygon2 test = _computeRelTriag(triangulationClone,
						toCheckClone, target, duplpoly);
				if (test != null)
					return test;
			}
		} // for all remaining triangles
		return null; // This try was not successful

	} // _computeRelTriag


	/**
	 * Changes an segment in an array
	 * 
	 * @param array
	 *            The segments
	 * @param oldseg
	 *            The old segment
	 * @param newseg
	 *            The new segment
	 */
	private void _changeSeg(
			Segment2[] array,
			Segment2 oldseg,
			Segment2 newseg)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i].equals(oldseg))
			{
				array[i] = newseg;
				i = array.length;
			}
		}
	} // _changeSeg


	// ************************************************************************************************************************************************

	/**
	 * Checks if two triangles are neighbours and returns the connecting edge.
	 * 
	 * Otherwise returning null.
	 * 
	 * @param triang1
	 *            The first triangle
	 * @param triang2
	 *            The second triangle
	 * 
	 * @return The connecting edge or null
	 */
	private Segment2 _checkNeighbour(
			ConvexPolygon2 triang1,
			ConvexPolygon2 triang2)
	{
		Segment2[] edges1 = triang1.edges();
		Segment2[] edges2 = triang2.edges();
		Segment2 mirror1;

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				mirror1 = new Segment2(edges1[i].target(), edges1[i].source());
				if (edges1[i].equals(edges2[j])) // common edge found
					return edges1[i];
				if (mirror1.equals(edges2[j])) // mirror common edge found
					return mirror1;
			}
		}
		return null;
	} // _checkNeighbour


	// ************************************************************************************************************************************************

	/**
	 * This method adds endpoints of cuts to a poly
	 */
	private void _computeAllVertices()
	{
		int numberOfPolyPoints = _inputPoly.length();
		Point2 pt1, pt2, startpoint;

		for (int i = 0; i < numberOfPolyPoints; i++) // For all points in the polygon
		{
			pt1 = _inputPoly.firstPoint();
			_inputPoly.removeFirstPoint();
			pt2 = _inputPoly.firstPoint();
			_inputPoly.addPoint(pt1);

			Segment2 help = new Segment2(pt1, pt2);
			startpoint = help.center();

			VisibilityCuts _viscuts = new VisibilityCuts(); // Create new VisibilityCuts
			_viscuts.setInputPolygon(new Polygon2(_workingPoly)); // Set polygon

			Polygon2 start = new Polygon2(); // Set startpoint
			start.addPoint(new Point2(startpoint));
			_viscuts.resetStartPoint();
			_viscuts.setStartingPoint(start);

			_viscuts._computeCuts();

			Segment2 cuts[] = new Segment2[_viscuts.getLength()];

			// add the points to the poly...

			cuts = _viscuts.getVisiCuts();
			_workingPoly = _addPoints(_workingPoly, cuts);
		}

	} // _computeAllVertices


	// ************************************************************************************************************************************************

	/**
	 * Check wether the polygon is rectangular or not
	 * 
	 * @param testPoly
	 *            The polygon
	 * 
	 * @return true if rectangular, false else
	 */
	private boolean _isRectangular(
			Polygon2 testPoly)
	{
		//TODO: Add code here...
		return true;
	} // _isRectangular


	// ************************************************************************************************************************************************

	/**
	 * This method adds endpoints of segments to a given polygon
	 * 
	 * @param poly
	 *            The original polygon
	 * @param cuts
	 *            The cuts
	 * 
	 * @return The resulting polygon
	 */
	private Polygon2 _addPoints(
			Polygon2 poly,
			Segment2[] cuts)
	{

		int anzahlPolyPunkte = poly.length();
		int anzahlcuts = cuts.length;
		Point2 cut1, cut2;
		Point2 erster = poly.firstPoint();
		Point2 zweiter = poly.lastPoint();
		Segment2 actual;

		for (int j = 0; j < anzahlcuts; j++)
		{
			cut1 = cuts[j].getLeftPoint();
			cut2 = cuts[j].getRightPoint();

			for (int i = 0; i < anzahlPolyPunkte; i++)
			{
				erster = poly.firstPoint();
				poly.addPoint(poly.firstPoint());
				poly.removeFirstPoint();
				zweiter = poly.firstPoint(); // Jetzt haben wir beide ersten Punkte
				actual = new Segment2(erster, zweiter);

				if (cut1.inSegment(erster, zweiter) && !cut1.equals(erster)
						&& !cut1.equals(zweiter))
				{
					poly.insert(erster, zweiter, new Point2(cut1));
				}
				if (cut2.inSegment(erster, zweiter) && !cut2.equals(erster)
						&& !cut2.equals(zweiter))
				{
					poly.insert(erster, zweiter, new Point2(cut2));
				}

			} // for anzahlpolypunkte

		} // for anzahlcuts 

		return poly;

	} // _addPoints

} // class SWRinRecPoly

