package anja.graph;


import java.util.Iterator;

import java.util.LinkedList;
import java.util.Arrays;

import static anja.graph.Constants.*;


/**
 * A BDFSVertexIterator returns all vertices reachable from the source via
 * either BFS or DFS. The source itself is the first vertex of currentVertex()
 * but never returned by nextVertex(). A BDFSVertexIterator should not be
 * created directly but via the Graph.getBDFSVertexIterator methods. This way
 * graph implementations can replace an iterator with its own optimized version.
 */

public class BDFSVertexIterator
		implements Iterator<Vertex>, java.io.Serializable
{

	// *********************************
	// VARIABLES
	// *********************************

	protected Vertex				_currentVertex;
	protected int					_mode;

	protected LinkedList<Vertex>	_vertexList	= new LinkedList<Vertex>();
	protected boolean				_dfs		= true;
	//stores true/false at _seenVertices[i] for having seen a vertex with ID i.
	protected boolean[]				_seenVertices;


	// *********************************
	// CONSTRUCTORS
	// *********************************

	/**
	 * Does not set a source vertex. An iterator created without a source will
	 * not be usable.
	 */
	public BDFSVertexIterator()
	{}


	/**
	 * Creates an DFS-Iterator with source as its source.
	 */
	public BDFSVertexIterator(
			Vertex source)
	{
		this(source, UNORDERED, true);
	}


	/**
	 * Creates an DFS-iterator with source as its source. The order in which the
	 * edges around a vertex are searched can be set via mode.
	 */
	public BDFSVertexIterator(
			Vertex source,
			int mode)
	{
		this(source, mode, true);
	}


	/**
	 * Creates an DFS-iterator without a specific source vertex. The iterator
	 * gets its source vertex via graph.getAnyVertex()
	 */
	public BDFSVertexIterator(
			Graph graph)
	{
		this(graph.getAnyVertex(), UNORDERED, true);
	}


	/**
	 * Creates an Iterator with source as its source.
	 * @param dfs
	 *            true->DFS, false->BFS
	 */
	public BDFSVertexIterator(
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
	public BDFSVertexIterator(
			Vertex source,
			int mode,
			boolean dfs)
	{
		_currentVertex = source;
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
		_seenVertices = new boolean[g.getNextVertexID()];
		Arrays.fill(_seenVertices, false);
		_seenVertices[source.getID()] = true;
		addChildVertices(source);

	}


	/**
	 * Creates an iterator without a specific source vertex. The iterator gets
	 * its source vertex via graph.getAnyVertex()
	 * @param dfs
	 *            true->DFS, false->BFS
	 */
	public BDFSVertexIterator(
			Graph graph,
			boolean dfs)
	{
		this(graph.getAnyVertex(), UNORDERED, dfs);
		_currentVertex = graph.getAnyVertex();
	}


	// *********************************
	// PUBLIC METHODS
	// *********************************

	@Override
	public boolean hasNext()
	{
		if (_vertexList == null)
			return false;
		return !_vertexList.isEmpty();
	}


	@Override
	public Vertex next()
	{
		if (_vertexList.isEmpty())
			return null;

		int nextID = _currentVertex.getGraph().getNextVertexID();
		if (nextID > _seenVertices.length)
		{
			//Increase size of array containing seen vertices if graph has changed
			boolean[] newSeenVertices = new boolean[nextID];
			Arrays.fill(newSeenVertices, false);
			System.arraycopy(_seenVertices, 0, newSeenVertices, 0,
					_seenVertices.length);
			_seenVertices = newSeenVertices;
		}

		_currentVertex = _vertexList.removeFirst();
		_seenVertices[_currentVertex.getID()] = true;
		addChildVertices(_currentVertex);

		//remove any seen vertices from the front of the list
		while (_vertexList.peekFirst() != null
				&& _seenVertices[_vertexList.peekFirst().getID()])
			_vertexList.removeFirst();

		return _currentVertex;
	}


	@Override
	public void remove()
	{}


	// *********************************
	// PROTECTED/PRIVATE METHODS
	// *********************************

	protected void addChildVertices(
			Vertex v)
	{
		Iterator<Edge> ei = v.getEdgeIterator(_mode);
		if (ei != null && ei.hasNext())
		{
			while (ei.hasNext())
			{
				Edge e = ei.next();
				Vertex newV = null;
				if (v != e.getSource())
					newV = e.getSource();
				else if (v != e.getTarget())
					newV = e.getTarget();
				if (newV != null && !_seenVertices[newV.getID()])
				{
					if (_dfs)
						_vertexList.addFirst(newV);
					else
						_vertexList.addLast(newV);
				}
			}
		}
	}

}
