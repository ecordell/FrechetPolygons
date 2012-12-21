package anja.geom;


import java.util.Vector;


/**
 * This class implements the funnels that are used to calculate the shortest
 * path from the source (start) to the destination (target) inside a simple,
 * closed Polygon2. These funnels are used by the Funnel Algorithm by Lee and
 * Preparata (<i>D. T. Lee and F. P. Preparata: Euclidean shortest paths in the
 * presence of rectilinear barriers. Networks, 14:393-410, 1984.</i>). The core
 * method is {@link #_funnelCheck(boolean, Point2)}.
 * 
 * 
 * @author Anja Haupts
 * @version 1.2 02.12.07
 */
public class Funnel
		implements java.io.Serializable
{

	// *********************************************************************
	// Private instance variables
	// *********************************************************************

	/** the convex chain that forms the left wall of the funnel */
	private Point2List		_leftFunnel;

	/** the convex chain that forms the right wall of the funnel */
	private Point2List		_rightFunnel;

	/** the starting point for the exploration */
	private Point2			_start;

	/**
	 * the temporary shortest path, i.e. the part of the funnel, that will not
	 * change any more
	 */
	private Vector<Point2>	_tempSP;

	/** the final shortest path */
	private Segment2[]		_shortestPath;


	// *********************************************************************
	// Constructors
	// *********************************************************************

	/**
	 * Creates a Funnel that at first only consists of start.
	 * 
	 * @param start
	 *            The very beginning of a funnel exploration.
	 * @param max_sp_size
	 *            the maximum length of the shortest path
	 */
	public Funnel(
			Point2 start,
			int max_sp_size)
	{
		_start = start;
		_leftFunnel = new Point2List();
		_rightFunnel = new Point2List();
		_leftFunnel.addPoint(_start);
		_rightFunnel.addPoint(_start);
		_shortestPath = null;
		_tempSP = new Vector<Point2>(max_sp_size);

	}// public Funnel(Point2 start, int max_sp_size)


	// *********************************************************************
	// Public instance methods
	// *********************************************************************

	/**
	 * Returns the shortest path, when the exploration is completed and target
	 * has been added.
	 * 
	 * @param diagonals
	 *            The diagonals of a triangulation that need to be crossed by
	 *            any shortest path from start to target.
	 * @param target
	 *            the Point2 that represents the target
	 * 
	 * @return The shortest path as Segment2[]
	 * 
	 * @see #_add(Segment2)
	 */
	public Segment2[] getShortestPath(
			Segment2[] diagonals,
			Point2 target)
	{
		for (int i = 0; i < diagonals.length; i++)
			_add(diagonals[i]);

		_addTarget(target);

		_shortestPath = _getPath(_tempSP.toArray(new Point2[0]));
		return _shortestPath;

	}// public Point2[] getShortestPath(Segment2[] diagonals, Point2 target)


	// *********************************************************************
	// Private instance methods
	// *********************************************************************

	/**
	 * Calculates the shortest path from _start to _target iteratively. The new
	 * funnel will be intitialized with the first diagonal. Then for every new
	 * diagonal there will be a check, if the old funnel is only a diagonal. In
	 * that case it will be checked, if the new point of the current diagonal
	 * extends the diagonal to a triangle-shaped funnel or if the new funnel is
	 * also just a diagonal. Otherwise the funnel exploration will continue in
	 * {@link #_funnelCheck(boolean, Point2)}.
	 * 
	 * @param diag
	 *            the diagonal that is crossed
	 * 
	 * @see #_funnelCheck(boolean, Point2)
	 */
	private void _add(
			Segment2 diag)
	{
		Point2 diag_right = diag.getRightPoint();
		Point2 diag_left = diag.getLeftPoint();

		// intitialize the funnel
		if (_leftFunnel.length() == 1 && _rightFunnel.length() == 1)
		{
			// catch the special case when start lies on the
			// diagonal -> ignore that diagonal as it has not
			// really to be crossed
			if (_start.inSegment(diag_left, diag_right))
				return;

			Segment2 tester = new Segment2(_start, diag.center());
			if (tester.orientation(diag_left) == Point2.ORIENTATION_LEFT)
			{
				_leftFunnel.addPoint(diag_left);
				_rightFunnel.addPoint(diag_right);
			}
			else
			{
				_rightFunnel.addPoint(diag_left);
				_leftFunnel.addPoint(diag_right);
			}// else

			return;
		}// if

		// catch the cases, where the old funnel is only a diagonal
		if (_leftFunnel.length() + _rightFunnel.length() == 3)
		{
			if (_leftFunnel.length() == 2)
			{
				if (_leftFunnel.lastPoint().equals(diag_left))
				{
					_funnelCheck(false, diag_right);
					return;
				}// if

				if (_leftFunnel.lastPoint().equals(diag_right))
				{
					_funnelCheck(false, diag_left);
					return;
				}// if

				if (_leftFunnel.firstPoint().equals(diag_left))
				{
					_leftFunnel.removeLastPoint();
					_leftFunnel.addPoint(diag_right);
					return;
				}// if
				if (_leftFunnel.firstPoint().equals(diag_right))
				{
					_leftFunnel.removeLastPoint();
					_leftFunnel.addPoint(diag_left);
					return;
				}// if

			}
			else
			{
				if (_leftFunnel.firstPoint().equals(diag_left))
				{
					_rightFunnel.clear();
					_rightFunnel.addPoint(_leftFunnel.firstPoint());
					_rightFunnel.addPoint(diag_right);
					return;
				}// if
				if (_leftFunnel.firstPoint().equals(diag_right))
				{
					_rightFunnel.clear();
					_rightFunnel.addPoint(_leftFunnel.firstPoint());
					_rightFunnel.addPoint(diag_left);
					return;
				}// if
			}// else

			if (_rightFunnel.length() == 2)
			{
				if (_rightFunnel.lastPoint().equals(diag_left))
				{
					_funnelCheck(true, diag_right);
					return;
				}
				else
				{
					if (_rightFunnel.lastPoint().equals(diag_right))
					{
						_funnelCheck(true, diag_left);
						return;
					}// if
				}// else

				if (_rightFunnel.firstPoint().equals(diag_left))
				{
					_rightFunnel.removeLastPoint();
					_rightFunnel.addPoint(diag_right);
					return;
				}// if
				if (_rightFunnel.firstPoint().equals(diag_right))
				{
					_rightFunnel.removeLastPoint();
					_rightFunnel.addPoint(diag_left);
					return;
				}// if
			}
			else
			{
				if (_rightFunnel.firstPoint().equals(diag_left))
				{
					_leftFunnel.clear();
					_leftFunnel.addPoint(_rightFunnel.firstPoint());
					_leftFunnel.addPoint(diag_right);
					return;
				}// if
				if (_rightFunnel.firstPoint().equals(diag_right))
				{
					_leftFunnel.clear();
					_leftFunnel.addPoint(_rightFunnel.firstPoint());
					_leftFunnel.addPoint(diag_left);
					return;
				}// if
			}// else
		}// if(_leftFunnel.size()+_rightFunnel.size() == 3)

		// for all other cases: sweep the funnel walls
		if (_leftFunnel.lastPoint().equals(diag_left))
		{
			_funnelCheck(false, diag_right);
			return;
		}// if

		if (_leftFunnel.lastPoint().equals(diag_right))
		{
			_funnelCheck(false, diag_left);
			return;
		}// if

		if (_rightFunnel.lastPoint().equals(diag_left))
		{
			_funnelCheck(true, diag_right);
			return;
		}// if

		_funnelCheck(true, diag_left);
		return;

	}// private void _add(Segment2 diag)


	// *********************************************************************

	/**
	 * Calculates the shortest path from _start to _target - the last step of
	 * the funnel exploration.
	 * 
	 * @param target
	 *            the Point2 object, that will be inserted at the end
	 */
	private void _addTarget(
			Point2 target)
	{
		// test if target is one of the endpoints of the last diagonal
		// ->if not append it to the left funnel wall
		if (!target.equals(_leftFunnel.lastPoint())
				&& !target.equals(_rightFunnel.lastPoint()))
		{
			_funnelCheck(true, target);
		}
		else
		{
			// ->else find the according wall and append it to
			// _tempSP
			// here: if it is the right funnel wall
			if (target.equals(_rightFunnel.lastPoint()))
			{
				while (0 < _rightFunnel.length())
				{
					if (_tempSP.contains(_rightFunnel.firstPoint()))
					{
						_rightFunnel.removeFirstPoint();
						continue;
					}// if

					_tempSP.addElement(_rightFunnel.firstPoint());
					_rightFunnel.removeFirstPoint();

				}// while

				return;
			}// if
		}// else

		while (0 < _leftFunnel.length())
		{
			if (_tempSP.contains(_leftFunnel.firstPoint()))
			{
				_leftFunnel.removeFirstPoint();
				continue;
			}// if

			_tempSP.addElement(_leftFunnel.firstPoint());
			_leftFunnel.removeFirstPoint();

		}// while

		return;

	}// private void _addTarget(Segment2 diag)


	// *********************************************************************

	/**
	 * Calculates the array of Segment2 objects, that form the path.
	 * 
	 * @param points
	 *            the Point2 array that cointains the points of the final funnel
	 *            - that is the shortest path
	 * 
	 * @return the path as Segment2[] that the concatenation of the
	 *         <i>points</i> forms
	 */
	private Segment2[] _getPath(
			Point2[] points)
	{
		Segment2 current_seg = new Segment2(points[0], points[1]);
		Segment2[] path = new Segment2[points.length - 1];
		path[0] = current_seg;

		if (points.length == 2)
			return path;

		for (int i = 1; i < points.length - 1; i++)
		{
			current_seg = new Segment2(points[i], points[i + 1]);
			path[i] = current_seg;
		}// for (int i = 1; i < points.length-1; i++)

		return path;

	}// private Segment2[] _getPath(Point2[] points)


	// *********************************************************************

	/**
	 * Tests, if the examined funnel can be cut off and cuts that part off. This
	 * represents the basic idea of the funnel algorithm by Lee and
	 * Preparata.<br> This method checks which old funnel points must be
	 * deleted. So it visits the funnel points and looks for a tangent to the
	 * two convex chains that form the funnel walls. First there will be
	 * checked, if the new point can be appended directly to one of the convex
	 * chains. If not this search continues at the end of that funnel wall,
	 * where the added point should be inserted.<br> A tangent is found, when
	 * the two neighbouring edges of the examined point lie in the same
	 * halfplane of the sweep line or if one of them is collinear with the sweep
	 * line.<br> When the first point of the wall (i.e. the overlapping point of
	 * the funnel walls) is reached and the left and the right segment are in
	 * different halfplanes or one of them collinear to the sweep line, there is
	 * a direct connection from the added point to that <i>root</i>, so delete
	 * the whole examined convex chain except that <i>root</i>.<br> If there is
	 * also no "tangent" in the <i>root</i>, delete that point from the first
	 * funnel wall, but copy it to <b>_tempSP</b>. The method will continue to
	 * copy the first points of the second funnel wall to <b>_tempSP</b> and
	 * erase them until it will finnaly find a tangent. And it will find one as
	 * the funnel walls are convex chains.<br> No funnel point will be check
	 * twice as they will be deleted or added to <i>_tempSP</i>.
	 * 
	 * @param left_funnel_first
	 *            true, if the sweep should start in the left funnel
	 * @param addedPoint
	 *            the Point2 to be added
	 * 
	 * @see #_tempSP
	 */
	private void _funnelCheck(
			boolean left_funnel_first,
			Point2 addedPoint)
	{
		Point2List first_funnel = _rightFunnel;
		Point2List second_funnel = _leftFunnel;

		if (left_funnel_first)
		{
			first_funnel = _leftFunnel;
			second_funnel = _rightFunnel;
		}// if

		// catch the special case, when the funnel only consists of a
		// diagonal
		if (first_funnel.length() + second_funnel.length() <= 3)
		{
			if (first_funnel.length() == 1)
			{
				first_funnel.addPoint(addedPoint);
			}
			else
			{
				first_funnel.removeLastPoint();
				first_funnel.addPoint(addedPoint);
			}// else
			return;
		}// if

		Point2 before = null;
		Point2 after = null;

		Segment2 sweep_line = new Segment2(addedPoint, first_funnel.lastPoint());

		// to access the point before the last point of the first funnel
		// wall, save the last point and delete it from the funnel wall
		Point2 temp_point = first_funnel.lastPoint();
		first_funnel.removeLastPoint();

		after = first_funnel.lastPoint();

		// if the added point keeps the convex chain convex,
		// just add it to that chain after re-inserting temp_point
		if (left_funnel_first)
		{
			if (sweep_line.orientation(after) == Point2.ORIENTATION_RIGHT
					|| sweep_line.orientation(after) == Point2.ORIENTATION_COLLINEAR)
			{
				_leftFunnel.addPoint(temp_point);
				_leftFunnel.addPoint(addedPoint);
				return;
			}// if
		}
		else
		{
			if (sweep_line.orientation(after) == Point2.ORIENTATION_LEFT
					|| sweep_line.orientation(after) == Point2.ORIENTATION_COLLINEAR)
			{
				_rightFunnel.addPoint(temp_point);
				_rightFunnel.addPoint(addedPoint);
				return;
			}// if
		}// else

		// re-insert the temporarily deleted point
		first_funnel.addPoint(temp_point);

		while (first_funnel.length() > 1)
		{
			// Can a direct connection to the root of the
			// funnel be found?
			if (first_funnel.length() - 2 == 0)
			{
				sweep_line = new Segment2(addedPoint, first_funnel.firstPoint());

				temp_point = first_funnel.firstPoint();
				first_funnel.removeFirstPoint();
				before = first_funnel.firstPoint();

				// to access the second point of the second
				// funnel, save the first point and delete it
				// from the funnel wall
				Point2 second_temp_point = second_funnel.firstPoint();
				second_funnel.removeFirstPoint();

				// true, if a direct connection is found
				if (sweep_line.orientation(before) != sweep_line
						.orientation(second_funnel.firstPoint()))
				{
					first_funnel.removeLastPoint();
					first_funnel.addPoint(addedPoint);

					// put the temporarily deleted points
					// back into the funnel walls
					first_funnel.insertFront(temp_point);
					second_funnel.insertFront(second_temp_point);

					return;
				}// if

				// put the temporarily deleted points back into
				// the funnel walls
				first_funnel.insertFront(temp_point);
				second_funnel.insertFront(second_temp_point);

				first_funnel.clear();
				break;
			}// if

			before = first_funnel.lastPoint();
			first_funnel.removeLastPoint();
			sweep_line = new Segment2(addedPoint, first_funnel.lastPoint());

			// to access the point before the last point of the
			// first funnel wall, save the last point and delete it
			// from the funnel wall
			temp_point = first_funnel.lastPoint();
			first_funnel.removeLastPoint();
			after = first_funnel.lastPoint();

			// found a tangent
			if (sweep_line.orientation(before) == sweep_line.orientation(after))
			{
				first_funnel.addPoint(temp_point);
				first_funnel.addPoint(addedPoint);
				return;
			}// if

			// found a tangent collinear to one of the funnel walls
			if (sweep_line.orientation(after) == Point2.ORIENTATION_COLLINEAR)
			{
				first_funnel.addPoint(temp_point);
				first_funnel.addPoint(addedPoint);
				return;
			}// if

			// if no tangent has been found yet,
			// keep the first point of the old funnel wall deleted
			// and re-insert the temporarily deleted point
			first_funnel.addPoint(temp_point);

		}// while

		// check the other funnel wall
		while (second_funnel.length() > 1)
		{
			before = second_funnel.firstPoint();
			second_funnel.removeFirstPoint();
			sweep_line = new Segment2(addedPoint, second_funnel.firstPoint());

			if (second_funnel.length() == 1)
			{
				_tempSP.addElement(before);
				first_funnel.addPoint(second_funnel.firstPoint());
				first_funnel.addPoint(addedPoint);
				return;

			}// if (second_funnel.size() == 2)

			// to access the third point of the second funnel,
			// save the second point and also delete it from the
			// funnel wall
			temp_point = second_funnel.firstPoint();
			second_funnel.removeFirstPoint();
			after = second_funnel.firstPoint();

			// found a tangent
			if (sweep_line.orientation(before) == sweep_line.orientation(after))
			{
				_tempSP.addElement(before);
				second_funnel.insertFront(temp_point);

				first_funnel.addPoint(temp_point);
				first_funnel.addPoint(addedPoint);

				return;
			}// if

			// if no tangent has been found yet,
			// keep the first point of the old funnel wall deleted;
			// this point already belongs to the shortest path
			_tempSP.addElement(before);

			// re-insert the temporarily deleted point
			second_funnel.insertFront(temp_point);

		}// while

		return;

	}// private void _funnelCheck

}// public class Funnel
