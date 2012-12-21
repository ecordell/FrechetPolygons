package ShortestPathLP;

import java.util.Vector;
import java.util.Stack;

import anja.geom.Point2;
import anja.geom.Polygon2;
import anja.geom.Segment2;

/**
 * This class creates the dual graph for a triangulated polygon by using the
 * center of mass of the ConvexPolygons represented by the triangles as the dual
 * vertexes. It calculates the DepthFirstSearch on that dual graph, too.
 * 
 * @author Anja Haupts
 * 
 * @version 1.2   23.11.07
 */

public class DualGraph
{

        // *********************************************************************
        // Private instance variables
        // *********************************************************************

        /** the array containing the triangles of the Triangulation */
        private Polygon2[] _triangPolys;

        /** amount of triangles = number of vertexes */
        private int _triangCount;

        /** the dual vertexes */
        private Point2[] _dualVertexes;

        /**
         * A kind of adjacency list where the Point2-Objects (the dual points)
         * are only referenced by the indexes. The first index corresponds to
         * the indexes of the _dualVertexes-array. So the first entry refers to
         * the first point of the _dualVertexes-Array. The second refers to the
         * one to three neighbours of that point by index.<br>
         * ->will later be used for the dual edges and the depth first search
         */
        private int[][] _dualG;

        /** the dual edges are saved in a vector */
        private Vector<DualEdge> _dualEdges;

        /**
         * the diagonals that are crossed by the DFS are saved separately,
         * because they will be needed for the funnels
         */
        private Vector<Segment2> _diagonalsOfDFS;

        /**
         * indexes of the Seidel triangulation`s triangles in the order, they
         * are visited by the DFS
         */
        private Vector<Integer> _visited;

        /** this stack is used to calculate the DFS */
        private Stack<Integer> _stack;

        /**
         * checks for every dual vertex, if it has already been fully explored
         * by the DFS
         */
        private boolean[] _check_visited;


        // *********************************************************************
        // Constructor
        // *********************************************************************

        /**
         * Calculates the Dual Graph for a triangulated polygon.
         * 
         * @param triangPolys
         *                the triangulation as a Polygon2[] of the polygon
         */
        public DualGraph(Polygon2[] triangPolys)
        {
                _triangPolys = triangPolys;

                _triangCount = _triangPolys.length;

                _dualVertexes = new Point2[_triangCount];
                _dualG = new int[_triangCount][3];

                // the capacity of the vectors is constrained by the maximum
                // amount of dual edges which is equal to the amount of
                // diagonals
                _dualEdges = new Vector<DualEdge>(_triangCount);
                _stack = new Stack<Integer>();
                _diagonalsOfDFS = new Vector<Segment2>(_triangCount);

                _calculate();

        }// public DualGraph(Polygon2[] triangPolys)


        // *********************************************************************
        // Public instance methods
        // *********************************************************************

        /**
         * returns the vertex of the position-th triangle
         * 
         * @return Point2
         * @param position
         */
        public Point2 getDualVertex(int position)
        {
                return _dualVertexes[position];

        }// public Point2 getDualVertex(int position)


        // *********************************************************************

        /**
         * returns the dual Edge of the position-th position in the Vector
         * 
         * @return Segment2
         * @param position
         */
        public Segment2 getDualEdge(int position)
        {
                return _dualEdges.get(position).get_dualEdge1();

        }// public Segment2 getDualEdge(int position)


        // *********************************************************************

        /**
         * returns the Diagonal of the position-th positon of the DFS
         * 
         * @return Segment2
         * @param position
         */
        public Segment2 getDiagonalInDFS(int position)
        {
                return _diagonalsOfDFS.get(position);

        }// public Segment2 getDiagonalInDFS(int position)


        // *********************************************************************

        /**
         * returns the size of _diagonalsOfDFS
         * 
         * @return amount of diagonals in the DFS to be crossed to get from the
         *         start to the target
         * @see #_diagonalsOfDFS
         */
        public int getDiagonalsInDFS_size()
        {
                return _diagonalsOfDFS.size();

        }// public int getDiagonalInDFS_size()


        // *********************************************************************

        /**
         * This method returns an int that represents how many dual Edges there
         * are in inte dual graph.
         * 
         * @return amount of dual edges
         * @see #_dualEdges
         */
        public int getDualEdges_size()
        {
                return _dualEdges.size();

        }// public int getDualEdges_size()


        // *********************************************************************

        /**
         * This method returns the diagonals that cross the path from start to
         * target in the according DFS order. It saves them in _diagonalsOfDFS.
         * 
         * @return Segment2[]
         * @param start_index
         *                the start of the recursive DFS
         * @param target_index
         *                the exit point of the recursive DFS
         * 
         * @see #_DFSrecursiveStack(int position, int target)
         * @see #_diagonalsOfDFS
         * @see #_visited
         * @see #_check_visited
         */
        public Segment2[] get_diagonals_by_DFS(int start_index, int target_index)
        {
                _visited = new Vector<Integer>(_dualVertexes.length + 2);
                _check_visited = new boolean[_dualVertexes.length];

                // initialize the array, that shows whether a vertex has already
                // been completely explored
                for (int i = 0; i < _check_visited.length; i++)
                {
                        _check_visited[i] = false;
                }// for i

                _DFSrecursiveStack(start_index, target_index);

                if (_stack.size() == 1 && _stack.peek() == start_index)
                        return null;

                // pop those stack elements, that are visited after target
                while (_stack.peek().intValue() != target_index)
                {
                        _stack.pop();
                }// while

                // copy the _stack to _visited
                for (int i = 0; i < _stack.size(); i++)
                {
                        int temporary = _stack.get(i).intValue();
                        _visited.addElement(temporary);
                }// for (int i = 0; i < temp.size(); i++)

                for (int i = 0; i < _visited.size() - 1; i++)
                {
                	Point2 de_point1 = _dualVertexes[_visited.get(i).intValue()];
                	Point2 de_point2 = _dualVertexes[_visited.get(i + 1).intValue()];
                	
                        DualEdge temp = new DualEdge(de_point1, de_point2);

                        // add the dualEdge to the new dualEdges list in DFS
                        // order
                        for (int j = 0; j < _dualEdges.size(); j++)
                        {

                                if (temp.equals(_dualEdges.get(j))
                                                && (_dualEdges.get(j).get_original_edge() != null))
                                {
                                        _diagonalsOfDFS.addElement(_dualEdges.get(j).get_original_edge());

                                        break;
                                }// if
                        }// for j

                }// for (int i = 0; i < _visited.size()-1; i++)

                return _diagonalsOfDFS.toArray(new Segment2[0]);

        }// _get_diagonals_by_DFS(int start_index)


        // *********************************************************************

        /**
         * Returns the position in the DFS of a special triangle if that
         * triangle occurs in the DFS or else -1
         * 
         * @return int
         * @param triangle_index
         */
        public int getPositionInDFS(int triangle_index)
        {
                if (!_visited.contains(triangle_index))
                        return -1;

                int temp = 0;

                while (temp < _visited.size())
                {
                        if (_visited.get(temp).intValue() == triangle_index)
                        {
                                return temp;
                        }// if

                        temp++;

                }// while

                return -1;

        }// public int getPositionInDFS(int triangle_index)


        // *********************************************************************
        // Private instance methods
        // *********************************************************************

        /**
         * This method calculates the dual graph.
         * 
         * @see #_dualG
         * @see #_dualVertexes
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
                                                if ((common_test.getLeftPoint()).equals(base_triang[l].getLeftPoint())
                                                                && (common_test.getRightPoint()).equals(base_triang[l].getRightPoint()))
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

                                                                } else if (_dualG[i][m] == -1)
                                                                {
                                                                        _dualG[i][m] = j;

                                                                        DualEdge de = new DualEdge(_dualVertexes[i], _dualVertexes[j], common_test);
                                                                        _dualEdges.addElement(de);

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

                                                                } else if (_dualG[j][n] == -1)
                                                                {
                                                                        _dualG[j][n] = i;
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
         * This method represents the recursive way to run the DFS. It finally
         * adds only those dual points that really have to be crossed by any
         * path from start to target to the DFS, which is saved in the stack. As
         * the method is recursive, after target, some more diagonals are added.
         * Those have to be popped from the stack later.
         * 
         * @param position
         *                the start of the recursive DFS
         * @param target
         *                the exit point of the recursive DFS
         * 
         * @see #_stack
         */
        private void _DFSrecursiveStack(int position, int target)
        {
                _stack.push(position);

                if (position == target)
                        return;

                int pos = position;

                for (int i = 0; i < 3; i++)
                {

                        position = _dualG[pos][i];

                        // if there is no following vertex, break
                        if (position == -1)
                        {
                                _check_visited[pos] = true;
                                break;
                        }

                        // only add the following vertex if it has not been
                        // visited yet, break, if its the target
                        if (!_stack.contains(position))
                        {
                                if (position == target)
                                {
                                        _stack.push(position);
                                        break;
                                }// if(position == target)

                                _DFSrecursiveStack(position, target);

                        }// if(!_stack.contains(position))

                        // if the third following vertex has been examined
                        // the actual vertex has been fully examined
                        if (i == 2)
                                _check_visited[pos] = true;

                }// for i

                // if the stack does not contain the target, the whole branch
                // can be deleted; for a full DFS, delete this if-clause
                if (!_stack.contains(target))
                {

                        while (_stack.peek().intValue() != pos)
                        {
                                _stack.pop();
                        }// while

                        if (_stack.size() > 1 && _check_visited[pos] == true)
                                _stack.pop();

                        return;

                }// if (!_stack.contains(target))

                return;

        }// _DFSrecursive(int start, int target)

}// public class DualGraph
