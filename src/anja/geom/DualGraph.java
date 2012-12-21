package anja.geom;


import java.util.Vector;
import java.util.Stack;


/**
 * This class creates the dual graph for a triangulated polygon by using the
 * center of mass of the ConvexPolygons represented by the triangles as the dual
 * vertexes. It calculates the DepthFirstSearch on that dual graph, too.
 * 
 * <br>Speeded the class up by now using linear time DFS and less unneccessary
 * calculations during the path calculation. Still the calculation of the
 * DualGraph takes O(n^2) time to build the graph itself. With a new
 * triangulation, this method can be changed to a faster one.
 * 
 * @author Anja Haupts, Andreas Lenerz
 * 
 * @version 1.1 23.11.07, ab November 2010
 */

public class DualGraph
		implements java.io.Serializable
{

	// *********************************************************************
	// Private instance variables
	// *********************************************************************

	/** the array containing the triangles of the Triangulation */
	private Polygon2[]			_triangPolys;

	/** amount of triangles = number of vertexes */
	private int					_triangCount;

	/** the dual vertexes */
	private Point2[]			_dualVertexes;

	/**
	 * A kind of adjacency list where the Point2-Objects (the dual points) are
	 * only referenced by the indexes. The first index corresponds to the
	 * indexes of the _dualVertexes-array. So the first entry refers to the
	 * first point of the _dualVertexes-Array. The second refers to the one to
	 * three neighbours of that point by index.<br>
	 */
	private int[][]				_dualG;

	/** the dual edges are saved in a vector */
	private DualEdge[][]		_dualEdges;

	/**
	 * the diagonals that are crossed by the DFS are saved separately, because
	 * they will be needed for the funnels
	 */
	private Vector<Segment2>	_diagonalsOfDFS;

	/** this stack is used to calculate the DFS */
	private Stack<Integer>		_stack;


	// *********************************************************************
	// Constructor
	// *********************************************************************

	/**
	 * Calculates the Dual Graph for a triangulated polygon.
	 * 
	 * @param triangPolys
	 *            the triangulation as a Polygon2[] of the polygon
	 */
	public DualGraph(
			Polygon2[] triangPolys)
	{
		_triangPolys = triangPolys;

		_triangCount = _triangPolys.length;

		_dualVertexes = new Point2[_triangCount];
		_dualG = new int[_triangCount][3];

		// the capacity of the vectors is constrained by the maximum
		// amount of dual edges which is equal to the amount of
		// diagonals
		_dualEdges = new DualEdge[_triangCount][3];
		_stack = new Stack<Integer>();
		_diagonalsOfDFS = new Vector<Segment2>(_triangCount);

		_calculate();

	}// public DualGraph(Polygon2[] triangPolys)


	// *********************************************************************
	// Public instance methods
	// *********************************************************************

	/**
	 * This method returns the diagonals that cross the path from start to
	 * target in the according DFS order. It saves them in _diagonalsOfDFS.
	 * 
	 * @param start_index
	 *            the start of the recursive DFS
	 * @param target_index
	 *            the exit point of the recursive DFS
	 * 
	 * @return The diagonals
	 * 
	 * @see #_DFSrecursiveStack(int position, int target)
	 * @see #_diagonalsOfDFS
	 * @see anja.geom.DualEdge
	 */
	public Segment2[] get_diagonals_by_DFS(
			int start_index,
			int target_index)
	{
		_diagonalsOfDFS = new Vector<Segment2>(_triangCount);
		_stack = new Stack<Integer>();

		_DFSrecursiveStack(start_index, target_index);

		if (_stack.size() == 1 && _stack.peek() == start_index)
			return null;

		for (int i = 0; i < _stack.size() - 1; i++)
		{
			int first = _stack.get(i);
			int second = _stack.get(i + 1);

			//DualEdge temp = _dualEdges[_stack.get(i)]

			if (_dualG[first][0] == second)
				_diagonalsOfDFS.addElement(_dualEdges[first][0]
						.get_original_edge());
			if (_dualG[first][1] == second)
				_diagonalsOfDFS.addElement(_dualEdges[first][1]
						.get_original_edge());
			if (_dualG[first][2] == second)
				_diagonalsOfDFS.addElement(_dualEdges[first][2]
						.get_original_edge());
		}// for (int i = 0; i < _visited.size()-1; i++)

		return _diagonalsOfDFS.toArray(new Segment2[0]);

	}// _get_diagonals_by_DFS(int start_index)


	// *********************************************************************
	// Private instance methods
	// *********************************************************************

	/**
	 * This method calculates the dual graph.
	 * 
	 * @see #_dualG
	 * @see #_dualVertexes
	 * @see #_dualEdges
	 * @see anja.geom.DualEdge
	 */
	private void _calculate()
	{
		// set default = -1 for no neighbouring edge
		for (int i = 0; i < _triangCount; i++)
		{
			_dualG[i][0] = -1;
			_dualG[i][1] = -1;
			_dualG[i][2] = -1;

		}// for (int i = 0; i < _triangCount; i++)

		// check every triangle as base triangle
		for (int i = 0; i < _triangCount; i++)
		{
			_dualVertexes[i] = _triangPolys[i].getCenterOfMass();

			Segment2[] base_triang = _triangPolys[i].edges();

			// look for neighbouring triangles == vertexes
			for (int j = 0; (j < _triangCount) && (j != i); j++)
			{
				// if the two triangles have no common edge,
				// it cannot be a neighbour
				Segment2[] possNeighTria = _triangPolys[j].edges();

				boolean common_edge = false;

				// check the edges of the possNeighTria
				// triangle
				for (int k = 0; k < 3; k++)
				{
					Segment2 common_test = possNeighTria[k];

					// edges of the base triangle
					for (int l = 0; l < 3; l++)
					{
						// if they have the same
						// vertexes, they are a common
						// edge
						if ((common_test.getLeftPoint()).equals(base_triang[l]
								.getLeftPoint())
								&& (common_test.getRightPoint())
										.equals(base_triang[l].getRightPoint()))
						{

							// insert the found dual
							// edge into the
							// adjascence list of
							// the base triangle by
							// index
							for (int m = 0; m < 3; m++)
							{
								if (_dualG[i][m] == j)
								{
									break;

								}
								else if (_dualG[i][m] == -1)
								{
									_dualG[i][m] = j;

									DualEdge de = new DualEdge(
											_dualVertexes[i], _dualVertexes[j],
											common_test);
									_dualEdges[i][m] = de;
									break;
								}// else
									// if

							}// for m

							// insert the found dual
							// edge into the second
							// triangle's list by
							// index
							for (int n = 0; n < 3; n++)
							{
								if (_dualG[j][n] == i)
								{
									break;

								}
								else if (_dualG[j][n] == -1)
								{
									_dualG[j][n] = i;
									DualEdge de = new DualEdge(
											_dualVertexes[j], _dualVertexes[i],
											common_test);
									_dualEdges[j][n] = de;
									break;
								}// else
									// if

							}// for n

							common_edge = true;

							// a pair of triangles
							// can only have one
							// common edge
							// so leave this loop
							break;

						}// if(....)

					}// for (int l = 0; l < 3; l++)

					// a pair of triangles can only have one
					// common edge
					// so leave this loop too
					if (common_edge == true)
						break;

				}// for (int k = 0; k < 3; k++)

			}// for (int j = i+1; j < _triangCount; j++)

		}// for (int i = 0; i < _triangCount; i++)

	}// private void _calculate()


	// *********************************************************************

	/**
	 * This method represents the recursive way to run the DFS. It finally adds
	 * only those dual points that really have to be crossed by any path from a
	 * source to a destination to the DFS, which is saved in a stack. As the
	 * method is recursive, after target, some more diagonals are added. Those
	 * have to be popped from the stack later.
	 * 
	 * <br>The stack results are now put together after the calculation
	 * 
	 * @param position
	 *            the start of the recursive DFS
	 * @param target
	 *            the exit point of the recursive DFS
	 * 
	 * @see #_stack
	 */
	private void _DFSrecursiveStack(
			int position,
			int target)
	{
		_DFSrec2(position, target, -1);

		Stack<Integer> tmp_stack = new Stack<Integer>();
		while (!_stack.isEmpty())
			tmp_stack.push(_stack.pop());

		_stack = tmp_stack;
	}// _DFSrecursive(int start, int target)


	/**
	 * This is the rewritten DFS. It searches for the target triangle in the
	 * graph in O(n) time
	 * 
	 * @param start
	 *            The starting triangle's number
	 * @param target
	 *            The target's number
	 * @param last
	 *            Initialize with -1. This value is used to know, which triangle
	 *            the algorithm traversed the current triangle from
	 * @return true, if target was found during the call of start, false else
	 */
	private boolean _DFSrec2(
			int start,
			int target,
			int last)
	{
		if (start == target)
		{
			_stack.push(start);
			return true;
		}
		else
		{
			for (int i = 0; i < 3; ++i)
			{
				int pos = _dualG[start][i];

				if (pos != last && pos != -1 && _DFSrec2(pos, target, start))
				{
					_stack.push(start);
					return true;
				}
			}
		}

		return false;
	}

}// public class DualGraph
