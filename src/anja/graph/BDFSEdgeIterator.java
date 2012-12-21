package anja.graph;


import java.util.Iterator;

import java.util.LinkedList;
import java.util.Arrays;
import static anja.graph.Constants.*;


/**
 * A BDFSVertexIterator returns all edges reachable from the source vertex via
 * either BFS or DFS. A BDFSEdgeIterator should not be created directly but via
 * the Graph.getBDFSEdgeIterator methods. This way graph implementations can
 * replace an iterator with its own optimized version.
 */

public class BDFSEdgeIterator
		implements Iterator<Edge>, java.io.Serializable
{

	// *********************************
	// VARIABLES
	// *********************************

	protected Edge				_currentEdge;
	protected int				_mode;

	protected LinkedList<Edge>	_edgeList	= new LinkedList<Edge>();
	protected boolean			_dfs		= true;
	//stores true/false at _seenEdges[i] for having seen am edge with ID i.
	protected boolean[]			_seenEdges;
	protected boolean[]			_seenVertices;


	// *********************************
	// CONSTRUCTORS
	// *********************************

	/**
	 * Does not set a source vertex. An iterator created without a source will
	 * not be usable.
	 */
	public BDFSEdgeIterator()
	{}


	/**
	 * Creates an DFS-Iterator with source as its source.
	 */
	public BDFSEdgeIterator(
			Vertex source)
	{
		this(source, UNORDERED, true);
	}


	/**
	 * Creates an DFS-iterator with source as its source. The order in which the
	 * edges around a vertex are searched can be set via mode.
	 */
	public BDFSEdgeIterator(
			Vertex source,
			int mode)
	{
		this(source, mode, true);
	}


	/**
	 * Creates an DFS-iterator without a specific source vertex. The iterator
	 * gets its source vertex via graph.getAnyVertex()
	 */
	public BDFSEdgeIterator(
			Graph graph)
	{
		this(graph.getAnyVertex(), UNORDERED, true);
	}


	/**
	 * Creates an Iterator with source as its source.
	 * @param dfs
	 *            true->DFS, false->BFS
	 */
	public BDFSEdgeIterator(
			Vertex source,
			boolean dfs)
	{
		this(source, UNORDERED, dfs);

	}


	/**
	 * Creates an iterator with source as its source. The order in which the
	 * edges around a vertex are searched can be set via mode.
	 * @param dfs
	 *            true->DFS, false->BFS
	 */
	public BDFSEdgeIterator(
			Vertex source,
			int mode,
			boolean dfs)
	{
		_mode = mode;
		_dfs = dfs;

		//for DFS-Iterators:
		//to visit vertices in a given order the vertices have to be put
		//onto the stack in the reverse order. To achieve this in the easiest way,
		//_mode is inverted here.
		if (_dfs)
			switch (_mode)
			{
				case CLOCKWISE:
					_mode = COUNTERCLOCKWISE;
					break;
				case COUNTERCLOCKWISE:
					_mode = CLOCKWISE;
					break;
				case ASCENDING_WEIGHT:
					_mode = DESCENDING_WEIGHT;
					break;
				case DESCENDING_WEIGHT:
					_mode = ASCENDING_WEIGHT;
					break;
			}
		if (source == null)
			return;
		Graph g = source.getGraph();
		if (g == null)
			return;
		_seenEdges = new boolean[g.getNextEdgeID()];
		Arrays.fill(_seenEdges, false);
		_seenVertices = new boolean[g.getNextVertexID()];
		Arrays.fill(_seenVertices, false);
		_seenVertices[source.getID()] = true;
		addChildEdgesAndVertices(source);

	}


	/**
	 * Creates an iterator without a specific source vertex. The iterator gets
	 * its source vertex via graph.getAnyVertex()
	 * @param dfs
	 *            true->DFS, false->BFS
	 */
	public BDFSEdgeIterator(
			Graph graph,
			boolean dfs)
	{
		this(graph.getAnyVertex(), UNORDERED, dfs);
	}


	// *********************************
	// PUBLIC METHODS
	// *********************************

	@Override
	public boolean hasNext()
	{
		if (_edgeList == null)
			return false;
		return !_edgeList.isEmpty();
	}


	@Override
	public Edge next()
	{
		if (_edgeList.isEmpty())
			return null;

		//get next edge from list
		_currentEdge = _edgeList.removeFirst();
		//edge has been seen
		_seenEdges[_currentEdge.getID()] = true;

		int nextID = _currentEdge.getSource().getGraph().getNextVertexID();
		if (nextID > _seenVertices.length)
		{
			//Increase size of array containing seen vertices if graph has changed
			boolean[] newSeenVertices = new boolean[nextID];
			Arrays.fill(newSeenVertices, false);
			System.arraycopy(_seenVertices, 0, newSeenVertices, 0,
					_seenVertices.length);
			_seenVertices = newSeenVertices;
		}

		nextID = _currentEdge.getSource().getGraph().getNextEdgeID();
		if (nextID > _seenEdges.length)
		{
			//Increase size of array containing seen edges if graph has changed
			boolean[] newSeenEdges = new boolean[nextID];
			Arrays.fill(newSeenEdges, false);
			System.arraycopy(_seenEdges, 0, newSeenEdges, 0, _seenEdges.length);
			_seenEdges = newSeenEdges;
		}

		//get source vertex if it hasn't been seen yet else get target vertex
		Vertex nextVertex = _seenVertices[_currentEdge.getSource().getID()] ? _currentEdge
				.getTarget() : _currentEdge.getSource();
		//if vertex hasn't been seen (in case it's a target vertex) add adjacent edges to edge list
		if (!_seenVertices[nextVertex.getID()])
		{
			addChildEdgesAndVertices(nextVertex);
		}
		//vertex has been seen
		_seenVertices[nextVertex.getID()] = true;

		//remove any seen edges from the front of the list
		while (!_edgeList.isEmpty()
				&& _seenEdges[_edgeList.peekFirst().getID()])
			_edgeList.removeFirst();

		return _currentEdge;
	}


	@Override
	public void remove()
	{}


	// *********************************
	// PROTECTED/PRIVATE METHODS
	// *********************************

	protected void addChildEdgesAndVertices(
			Vertex v)
	{
		Iterator<Edge> ei = v.getEdgeIterator(_mode);
		if (ei != null && ei.hasNext())
		{
			while (ei.hasNext())
			{
				Edge e = ei.next();
				if (!_seenEdges[e.getID()])
				{
					if (_dfs)
						_edgeList.addFirst(e);
					else
						_edgeList.addLast(e);
				}
			}
		}
	}

}
