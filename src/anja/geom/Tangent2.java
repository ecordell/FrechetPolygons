package anja.geom;


import java.util.ArrayList;


/**
 * Berechnet die vier Tangentpunkte von zwei Punkten bezueglich einer konvexen
 * Distanzfunktion.
 * 
 * The class calculates the tangents for a polygon and a certain point of view.
 * It allows to use a static method with a O(n)-algorithm for the calculation,
 * or provides methods to do a precalculation in O(n) or O(n log n) (which
 * depends on the given data structure) and afterwards do a O(log n) search for
 * arbitrary points of view. For a certain point of view the results in both
 * cases are the vertices of the polygon, that define the tangents, if they
 * exist. (empty polygon, etc.)
 * 
 * @version 0.8 27.06.1997, 24.05.2008
 * @author Lihong Ma, Andreas Lenerz
 */

public class Tangent2
		implements java.io.Serializable
{

	// ************************************************************************
	// Variables
	// ************************************************************************

	/**
	 * Der rechte Tangentpunkt von p,q auf der linken Seite der Gerade pq.
	 */
	public Point2			TLR;

	// ************************************************************************

	/**
	 * Der linke Tangentpunkt von p,q auf der linken Seite der Gerade pq.
	 */
	public Point2			TLL;

	// ************************************************************************

	/**
	 * Der rechte Tangentpunkt von p,q auf der rechten Seite der Gerade pq.
	 */
	public Point2			TRR;

	// ************************************************************************

	/**
	 * Der linke Tangentpunkt von p,q auf der rechten Seite der Gerade pq.
	 */
	public Point2			TRL;

	// ************************************************************************

	/**
	 * Der Zeiger, der am Anfang TLL zeigt.
	 */
	public PolygonAccess	hq;

	// ************************************************************************
	/**
	 * Der Vorgaenger von TLR.
	 */
	public Point2			prevTLR;
	// ************************************************************************

	/**
	 * Der Nachfolger von TLL.
	 */
	public Point2			nextTLL;
	// ************************************************************************

	/**
	 * Der Vorgaenger von TRL.
	 */
	public Point2			prevTRL;
	// ************************************************************************

	/**
	 * Der Nachfolger von TRR.
	 */
	public Point2			nextTRR;

	// ************************************************************************
	// private variables
	// ************************************************************************

	//The viewpoint, from which the tangents are calculated
	private Point2			_viewpoint	= null;

	//The convex polygon for the O(logn) methods
	private ConvexPolygon2	_cp			= null;

	//The center of the polygon
	private Point2			_center		= null;

	//The angles between the center, the points and the x-axis.
	private double[]		_angles		= null;

	//The segments, the points are on
	private Segment2[]		_segments	= null;


	// ************************************************************************
	// Constructor
	// ************************************************************************

	// ************************************************************************
	/**
	 * Default Costructor
	 */
	public Tangent2()
	{}


	/**
	 * Berechnet die vier Tangentpunkte und ihre Vorgaenger und Nachfolger.
	 * 
	 * @param p
	 *            Punkt 1
	 * @param q
	 *            Punkt 2
	 * @param C
	 *            Das konvexe Polygon
	 */
	public Tangent2(
			Point2 p,
			Point2 q,
			ConvexPolygon2 C)
	{
		if (C.empty())
			return;
		Point2 origin = new Point2(0, 0);
		Point2 cen = C.center();
		ConvexPolygon2 copyC = (ConvexPolygon2) C.clone();

		if (!cen.equals(origin))
		{
			copyC.translate(-cen.x, -cen.y);
		}
		copyC.setCenter(origin); // copyC um origin.

		PolygonAccess sp;
		hq = copyC.leftMostRight(p, q); // hq zeigt TLR
		prevTLR = new Point2(hq.prevPoint());
		TLR = new Point2(hq.nextPoint());
		TLL = new Point2(hq.nextPoint());
		nextTLL = new Point2(hq.nextPoint());
		Line2 pq = new Line2(p, q);

		if (!pq.isParallel(TLL, TLR))
		{
			nextTLL = new Point2(hq.prevPoint());
			TLL = new Point2(hq.prevPoint());// hq zeigt TLL = TLR
		}
		else
		{
			TLL = new Point2(hq.prevPoint());//hq zeigt TLL      
		}
		sp = copyC.rightMostRight(p, q);
		nextTRR = new Point2(sp.nextPoint());
		TRR = new Point2(sp.prevPoint());
		TRL = new Point2(sp.prevPoint());
		prevTRL = new Point2(sp.prevPoint());
		if (!pq.isParallel(TRL, TRR))
		{
			prevTRL = new Point2(sp.nextPoint());
			TRL = new Point2(sp.nextPoint());
		}

	} // Konstruktor


	// ************************************************************************
	// class methods
	// ************************************************************************

	/**
	 * Calculate the 2 tangents of a coherent polygon from a certain viewpoint
	 * 
	 * The method is static, so no extra object is needed. It works in O(n).
	 * 
	 * @param viewpoint
	 *            the point of view
	 * @param poly
	 *            the polygon
	 * 
	 * @return the two points of the polygon, that represent the tangent-points
	 *         of the polygon or null, if the viewpoint is inside the polygon or
	 *         the polygon is null.
	 */

	public static Point2[] getTangentPoints(
			Point2 viewpoint,
			Polygon2 poly)
	{
		//Checks, if the polygon and the viewpoint exist. 
		//The viewpoint mustn't be inside the polygon. There are no tangents available.
		if ((poly == null) || (viewpoint == null) || poly.inside(viewpoint))
		{
			return null;
		}

		//Now we need some additional variables, that save the maximum and minimum for the tangent calc
		//result includes the min in the max in this order.
		Point2[] result = new Point2[2];

		//Take all edges and compare them to the first point of the polygon...
		Segment2[] segs = poly.edges();

		//segs[0].source() is our starting point
		result[0] = new Point2(segs[0].source());
		result[1] = new Point2(segs[0].source());

		//max_angle_diff is the difference between the max and the min angle
		double max_angle_diff = 0.0;

		//Now we compare every point to our reference point segs[0].source()
		for (int i = 0; i < segs.length; ++i)
		{
			//The new result must result in an angle for min and max, that is bigger than the one before

			if (viewpoint.angle(result[0], segs[i].target()) > max_angle_diff)
			{

				//Two possibilities:
				//angle is < 180°: The segment is positive orientated
				//else: negative

				double angle = viewpoint.angle(segs[i].source(),
						segs[i].target());

				if (angle < java.lang.Math.PI)
				{
					//angle_2 is more positive than angle_1, so the edge goes in positive direction
					if ((viewpoint.angle(result[1], segs[i].target()) < java.lang.Math.PI))
					{
						result[1] = new Point2(segs[i].target());
					}
				}
				else
				{
					//angle_2 is more negative, so the edge goes in negative direction
					if (viewpoint.angle(segs[i].target(), result[0]) < java.lang.Math.PI)
					{
						result[0] = new Point2(segs[i].target());
					}
				}
			} //end:if 

			//This saves the max_angle from the min to the max.
			if (viewpoint.angle(result[0], result[1]) >= max_angle_diff)
			{
				max_angle_diff = viewpoint.angle(result[0], result[1]);
			}

		} //end:for

		//Return the two values min and max or null
		return result;
	}


	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Receives a polygon, converts it to a convex polygon and prepares
	 * datastructures to use a O(log n) search for tangents on the polygon
	 * 
	 * The algorithm works in O(n log n) It uses setConvexPolygon(cp) and the
	 * Point2List class to create a convex polygon.
	 * 
	 * @param cp
	 *            A Point2List object
	 */
	public void setConvexPolygon(
			Point2List cp)
	{
		this.setConvexPolygon(new ConvexPolygon2(cp));
	}


	/**
	 * Receives a polygon, converts it to a convex polygon and prepares
	 * datastructures to use a O(log n) search for tangents on the polygon
	 * 
	 * The algorithm works in O(n log n) It uses setConvexPolygon(cp) and the
	 * Point2List class to create a convex polygon.
	 * 
	 * @param cp
	 *            A Polygon2 object
	 */
	public void setConvexPolygon(
			Polygon2 cp)
	{
		this.setConvexPolygon(new ConvexPolygon2((Point2List) cp));
	}


	/**
	 * Receives a convex polygon and prepares datastructures to use a O(log n)
	 * search for tangents on the polygon
	 * 
	 * The algorithm works in O(n)
	 * 
	 * @param cp
	 *            The convex polygon
	 */
	public void setConvexPolygon(
			ConvexPolygon2 cp)
	{

		//Before we start, the insert polygon has to be convex
		if (cp != null && cp.isConvex())
		{

			//Save the polygon
			_cp = cp;

			//Calculate the center of mass. This point is inside the polygon, because of the convexity
			_center = cp.getCenterOfMass();

			//Get all points
			_segments = _cp.edges();
			int numberOfPoints = _cp.edgeNumber();

			//Calculate the angle of the points and save the points of the polygon
			_angles = new double[numberOfPoints];

			//reference for all other points
			_angles[0] = 0.0;

			//Now we make a check, to see, if the polygon is saved clockwise or counterclockwise
			//clockwise would make the later O(logn) calculation more complex.
			//This only makes sense, if there are more than 2 points
			if (numberOfPoints >= 3)
			{

				//Check the angles to see, how the segments are ordered
				if (_center.angle(_segments[0].source(), _segments[2].source())
						- _center.angle(_segments[0].source(),
								_segments[1].source()) >= 0)
				{
					//This is the case, that all points are counterclockwise
					for (int i = 1; i < numberOfPoints; ++i)
					{
						//Calculate the angle with the center of mass
						//Here we use the convexity of the polygon -> the angles are sorted
						_angles[i] = _center.angle(_segments[0].source(),
								_segments[i].source());

					}
				}
				else
				{
					//This is the case, that all points are clockwise
					//In this case, we have to turn the order around
					Segment2[] temp_seg = new Segment2[numberOfPoints];

					for (int i = 1; i < numberOfPoints; ++i)
					{
						//Calculate the angle with the center of mass
						//Here we use the convexity of the polygon -> the angles are sorted
						_angles[numberOfPoints - i] = _center.angle(
								_segments[0].source(), _segments[i].source());
						temp_seg[numberOfPoints - i] = new Segment2(
								_segments[i].target(), _segments[i].source());
					}

					_segments = temp_seg;
				}

			}
			else
			{
				//This is the case, that we have only two points
				_angles[1] = java.lang.Math.PI;
			}

			//Now some additional information:
			//The points in the array are already sorted, because it's a convex polygon.

		} //end if
	}


	/**
	 * Sets the new viewpoint of the scene
	 * 
	 * The method does not check, whether the point is inside the polygon or
	 * not. This would cost O(n) time instead of O(1).
	 * 
	 * @param viewpoint
	 *            The point of view
	 */
	public void setViewpoint(
			Point2 viewpoint)
	{
		_viewpoint = viewpoint;
	}


	/**
	 * Make a O(log n) search from the point of view on the prepared convex
	 * polygon to find the tangent points.
	 * 
	 * @return The tangent points of the polygon or null, if the polygon is null
	 *         or the point of view isn't set.
	 */
	public Point2[] getTangentPoints()
	{

		//The result
		Point2[] result = null;

		if (_cp != null && _viewpoint != null)
		{
			//If the polygon has only one point (i don't know, if this can happen, but ...)
			if (_segments.length == 1)
			{

				result = new Point2[1];
				result[0] = _segments[0].source();

			}
			else
			{
				//The polygon has more than one point
				//We have 2 tangents. In the case of two points, there is one tangent possible. But in this case both are equal
				result = new Point2[2];

				//This is the angle of our viewpoint
				double angle = _center.angle(_segments[0].source(), _viewpoint);

				//If the point is far away from the polygon, the following calc will result in the two tangents.
				int lower = binarySearch((angle + java.lang.Math.PI * 3 / 2.0)
						% (2 * java.lang.Math.PI));
				//There is only one possibility, where the following statement is important.
				lower = (lower - 1 + _segments.length) % _segments.length;

				int upper = binarySearch((angle + java.lang.Math.PI * 5 / 2.0)
						% (2 * java.lang.Math.PI));
				//same here
				upper = (upper + 1 + _segments.length) % _segments.length;

				//Lower and upper content the points, which angles are nearest to -Pi/2 / +Pi/2 to our viewpoint angle
				//In most cases this is enough, but if the viewpoint is closer to the polygon, other tangents are possible.
				//We use the complexity, because lower and upper are the most extreme points, that could be chosen.
				//All other points, that are on the other side of the polygon are hidden by at least those two.

				//This is the "worst" point, the calculation can result in
				int middle = binarySearch(angle);

				//Now we make a second binary search
				//Because we have to do it two times, i make a seperate method
				result[0] = _segments[binarySearch(lower, middle, true)]
						.source();
				result[1] = _segments[binarySearch(middle, upper, false)]
						.source();

			}

		}

		return result;
	}


	/**
	 * Get the viewpoint of the scene
	 * 
	 * @return The viewpoint of the scene
	 */
	public Point2 getViewpoint()
	{
		return _viewpoint;
	}


	/**
	 * Get the convex polygon of the scene.
	 * 
	 * @return The convex polygon
	 */
	public ConvexPolygon2 getConvexPolygon()
	{
		return _cp;
	}


	/**
	 * Calculates the tangents of the two polygons by rotating two parallel
	 * tangents around each polygon.
	 * 
	 * <br>If one polygon is inside the convex hull of the other then the result
	 * is null. Otherwise there may be n+m tangents, while n is the number of
	 * vertices of polygon 1 and m of polygon 2.
	 * 
	 * @param poly1
	 *            The first polygon
	 * @param poly2
	 *            The second polygon
	 * 
	 * @return The tangents or null, if one contains the other
	 * 
	 * @see anja.geom.ConvexPolygon2#contains(ConvexPolygon2)
	 */
	public static Segment2[] getTangents(
			ConvexPolygon2 poly1,
			ConvexPolygon2 poly2)
	{
		//if the polygons are equal, then there is no tangent.
		if (poly1 == null || poly2 == null)
			return null;

		ConvexPolygon2 pol1 = new ConvexPolygon2(poly1);
		ConvexPolygon2 pol2 = new ConvexPolygon2(poly2);

		//Remove all collinear points from the convex hull
		pol1.removeRedundantPoints();
		pol2.removeRedundantPoints();

		if (pol1.equals(pol2))
			return null;

		ArrayList<Segment2> result = new ArrayList<Segment2>();

		pol1.setOrientation(Polygon2.ORIENTATION_LEFT);
		pol2.setOrientation(Polygon2.ORIENTATION_LEFT);

		Point2[] p1 = pol1.toArray();
		Point2[] p2 = pol2.toArray();

		//The current index of the polygon's points
		int current1 = 0;
		int current2 = 0;

		double max = p1[0].y;
		for (int i = 1; i < p1.length; ++i)
		{
			if (max < p1[i].y)
			{
				max = p1[i].y;
				current1 = i;
			}
			else
			{
				if (max == p1[i].y)
				{
					if (p1[current1].x > p1[i].x)
					{
						max = p1[i].y;
						current1 = i;
					}
				}
			}
		}

		max = p2[0].y;
		for (int i = 1; i < p2.length; ++i)
		{
			if (max < p2[i].y)
			{
				max = p2[i].y;
				current2 = i;
			}
			else
			{
				if (max == p2[i].y)
				{
					if (p2[current2].x > p2[i].x)
					{
						max = p2[i].y;
						current2 = i;
					}
				}
			}
		}

		//Now we rotate around the polygons.

		//The booleans and arrays have an important role.
		//The algorithm starts for the first time, if the two rotating lines
		//are not collinear for the first time. After that, a coolinear
		//situation is handled as a block - as one situation over several steps
		//Each collinear point is sved in the arrays and the tangent is calculated
		//when the result becomes != collinear for the first time afterwards.
		boolean hasStarted = false;
		boolean isCollinear = false;
		Point2[] colList1 = new Point2[2];
		Point2[] colList2 = new Point2[2];

		Line2 l1 = new Line2(p1[current1], new Point2(p1[current1].x - 1,
				p1[current1].y));
		Line2 l2 = new Line2(p2[current2], new Point2(p2[current2].x - 1,
				p2[current2].y));

		int ori = l1.orientation(l2.source());
		int counter = p1.length + p2.length;

		// >=0 because the last segments must be able to flip one time
		// otherwise the final tangent may not been found, if it is a
		//tangent between the starting points of p1 and p2
		while (counter >= 0)
		{
			if (ori != Point2.ORIENTATION_COLLINEAR && !hasStarted)
				hasStarted = true;

			if (hasStarted)
				counter--;

			if (ori == Point2.ORIENTATION_COLLINEAR && hasStarted)
			{
				isCollinear = true;

				if (l1.liesOn(p2[current2]))
				{
					for (int i = 0; i < colList2.length; ++i)
					{
						if (colList2[i] == null
								|| colList2[i].equals(p2[current2]))
						{
							colList2[i] = new Point2(p2[current2]);
							break;
						}
					}

					if (l1.liesOn(p2[(current2 - 1 + p2.length) % p2.length]))
					{
						for (int i = 0; i < colList2.length; ++i)
						{
							if (colList2[i] == null
									|| colList2[i]
											.equals(p2[(current2 - 1 + p2.length)
													% p2.length]))
							{
								colList2[i] = new Point2(
										p2[(current2 - 1 + p2.length)
												% p2.length]);
								break;
							}
						}
					}
				}

				if (l2.liesOn(p1[current1]))
				{
					for (int i = 0; i < colList1.length; ++i)
					{
						if (colList1[i] == null
								|| colList1[i].equals(p1[current1]))
						{
							colList1[i] = new Point2(p1[current1]);
							break;
						}
					}

					if (l2.liesOn(p1[(current1 - 1 + p1.length) % p1.length]))
					{
						for (int i = 0; i < colList1.length; ++i)
						{
							if (colList1[i] == null
									|| colList1[i]
											.equals(p1[(current1 - 1 + p1.length)
													% p1.length]))
							{
								colList1[i] = new Point2(
										p1[(current1 - 1 + p1.length)
												% p1.length]);
								break;
							}
						}
					}
				}

			}

			double angle1 = p1[current1].angle(l1.target(), p1[(current1 + 1)
					% p1.length]);
			double angle2 = p2[current2].angle(l2.target(), p2[(current2 + 1)
					% p2.length]);

			if (angle1 < angle2)
			{
				//The angles of the tangents have to be equal all the time. Therefor
				//the same ratio has to be used to rotate them.
				Point2 p1_temp = (Point2) p1[(current1 + 1) % p1.length]
						.clone();
				p1_temp.x += p1[(current1 + 1) % p1.length].x - p1[current1].x;
				p1_temp.y += p1[(current1 + 1) % p1.length].y - p1[current1].y;
				l1 = new Line2(p1[(current1 + 1) % p1.length], p1_temp);

				Point2 p2_temp = (Point2) p2[current2].clone();
				p2_temp.x += p1[(current1 + 1) % p1.length].x - p1[current1].x;
				p2_temp.y += p1[(current1 + 1) % p1.length].y - p1[current1].y;
				l2 = new Line2(p2[current2], p2_temp);

				if (l1.orientation(l2.source()) != ori
						&& l1.orientation(l2.source()) != Point2.ORIENTATION_COLLINEAR
						&& ori != Point2.ORIENTATION_COLLINEAR && hasStarted)
				{
					//Change in orientation
					result.add(new Segment2(p1[current1], p2[current2]));
				}
				ori = l1.orientation(l2.source());
				current1 = (current1 + 1) % p1.length;
			}
			else
			{
				//The angles of the tangents have to be equal all the time. Therefor
				//the same ratio has to be used to rotate them.
				Point2 p2_temp = (Point2) p2[(current2 + 1) % p2.length]
						.clone();
				p2_temp.x += p2[(current2 + 1) % p2.length].x - p2[current2].x;
				p2_temp.y += p2[(current2 + 1) % p2.length].y - p2[current2].y;
				l2 = new Line2(p2[(current2 + 1) % p2.length], p2_temp);

				Point2 p1_temp = (Point2) p1[current1].clone();
				p1_temp.x += p2[(current2 + 1) % p2.length].x - p2[current2].x;
				p1_temp.y += p2[(current2 + 1) % p2.length].y - p2[current2].y;
				l1 = new Line2(p1[current1], p1_temp);

				if (l1.orientation(l2.source()) != ori
						&& l1.orientation(l2.source()) != Point2.ORIENTATION_COLLINEAR
						&& ori != Point2.ORIENTATION_COLLINEAR && hasStarted)
				{
					//Change in orientation
					result.add(new Segment2(p1[current1], p2[current2]));
				}
				ori = l1.orientation(l2.source());
				current2 = (current2 + 1) % p2.length;
			}

			if (isCollinear && ori != Point2.ORIENTATION_COLLINEAR)
			{
				Segment2 tangseg = null;

				for (int i = 0; i < colList1.length; ++i)
					for (int j = 0; j < colList2.length; ++j)
					{
						if (tangseg == null
								|| (colList1[i] != null && colList2[j] != null && tangseg
										.len() > colList1[i]
										.distance(colList2[j])))
						{
							tangseg = new Segment2(colList1[i], colList2[j]);
						}
					}

				boolean isSolution = true;

				if (colList1[0] != null && colList1[1] != null)
				{
					Segment2 seg1 = new Segment2(colList1[0], colList1[1]);
					isSolution = isSolution
							&& ((colList2[0] != null) ? !seg1
									.liesOn(colList2[0]) : true);
					isSolution = isSolution
							&& ((colList2[1] != null) ? !seg1
									.liesOn(colList2[1]) : true);
				}

				if (colList2[0] != null && colList2[1] != null)
				{
					Segment2 seg2 = new Segment2(colList2[0], colList2[1]);
					isSolution = isSolution
							&& ((colList1[0] != null) ? !seg2
									.liesOn(colList1[0]) : true);
					isSolution = isSolution
							&& ((colList1[1] != null) ? !seg2
									.liesOn(colList1[1]) : true);
				}

				colList1 = new Point2[2];
				colList2 = new Point2[2];

				if (tangseg != null && isSolution)
					result.add(tangseg);

				isCollinear = false;
			}
		}

		//If during the one additional step of the while() loop, an aditional
		//tangent is added, it is equal to the first one.
		if (result.size() > 1
				&& result.get(0).equals(result.get(result.size() - 1)))
			result.remove(0);

		return (result.size() == 0) ? null : result.toArray(new Segment2[1]);
	}


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Calculates the number of the point in the array, that is closest to the
	 * given angle.
	 * 
	 * The method uses java.util.Arrays#binarySearch(double[], double)
	 * 
	 * @return the number in the points array of the polygon
	 * 
	 * @see java.util.Arrays#binarySearch(double[], double)
	 */
	private int binarySearch(
			double angle)
	{
		//Use the binary search in java.util.Arrays to find the closest object
		//The result is the element in the array, angle would be inserted into
		int closest = java.util.Arrays.binarySearch(_angles, angle);
		if (closest < 0)
			closest *= -1;
		closest--;

		//left could be -1. To save the 'if' modulo is used.
		int left = (closest - 1 + _angles.length) % _angles.length;
		int right = (closest + _angles.length) % _angles.length;

		//Now we have to check, which point/angle is nearer to our segment/the angle of our segment.
		if (java.lang.Math.abs(_angles[left] - angle)
				- java.lang.Math.abs(_angles[right] - angle) <= 0)
		{
			//_angles[left] is nearer
			return left;
		}

		return right;
	}


	/**
	 * Calculates the index between left and right, that represents the tangent
	 * point
	 * 
	 * @param left
	 *            the left border
	 * @param right
	 *            the right border
	 * @param lower
	 *            a boolean to specify, if we are looking for the upper or lower
	 *            tangent point (relatively to the viewpoint)
	 * 
	 * @return the number in the points array of the polygon
	 */
	private int binarySearch(
			int left,
			int right,
			boolean lower)
	{
		//the helper for our binary search
		int middle = 0;

		//The result
		int result = left;

		if (lower)
			result = right;

		//This is our condition. If we find our first tangent, it's our only tangent
		boolean tangentFound = false;

		while (!tangentFound)
		{

			//The modulo operation catches the case, that segments[0] is between left and right
			middle = (left <= right) ? ((left + right) / 2)
					: ((left + right + _segments.length) / 2)
							% _segments.length;

			//We check for 2 seperate possibilities
			//First the left tangent
			if (lower)
			{
				//In this case, we are looking for the left tangent (from the viewpoint)
				//This tangent will always be "lower" in the order of the tangent points.
				if (_segments[middle].source().angle(
						_viewpoint,
						_segments[(middle - 1 + _segments.length)
								% _segments.length].source()) < java.lang.Math.PI)
				{
					//In this case, the point is a tangent for our viewpoint
					if (_segments[middle].source().angle(_viewpoint,
							_segments[middle].target()) < java.lang.Math.PI)
					{
						result = middle;
						tangentFound = true;
					}

					//Now we reset the left boundary
					left = (middle + 1) % _segments.length;
				}
				else
				{
					//1. The boundary of our search has to be moved left, more outside the polygon than now.
					//2. We have no tangent point
					//3. We have to search "more outside" the polygon = change the right boundary
					right = (middle - 1 + _segments.length) % _segments.length;
				}

			} //end big_if

			//Now the right tangent (always from the viewpoint)
			else
			{

				//In this case, we are looking for the right tangent (from the viewpoint)
				//This tangent will always be "upper" in the order of the tangent points.
				//The descriptions are the same than above, just the conditions are the other way round
				if (_segments[middle].source().angle(_viewpoint,
						_segments[middle].target()) > java.lang.Math.PI)
				{
					if (_segments[middle].source().angle(
							_segments[(middle - 1 + _segments.length)
									% _segments.length].source(), _viewpoint) < java.lang.Math.PI)
					{
						//By the way: usually this part is called just once. There is only one tangent, that
						//fullfills all this conditions. With >= and <= in the conditions it maybe two of them.
						result = middle;
						tangentFound = true;
					}
					else
					{
						right = (middle - 1 + _segments.length)
								% _segments.length;
					}
				}
				else
				{
					left = (middle + 1) % _segments.length;
				}

			} //end big_else

		} //end while

		return result;
	}

}
