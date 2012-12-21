/**
 * 
 */
package anja.graph.algorithms;


import static anja.graph.Constants.UNORDERED;

import java.util.Iterator;

import anja.graph.BDFSVertexIterator;
import anja.graph.Edge;
import anja.graph.Graph;
import anja.graph.Vertex;


/**
 * This class extends the BDFSVertexIterator. The additional functionality
 * allows to receive an associated edge for each vertex of the graph.
 * 
 * <br>If a vertex is visited by the BDFS the first time over an edge e, the
 * associated edge e is saved as a reference. For the source vertex of the BDFS
 * e is null, for every vertex in the same connected component it is != null.
 * 
 * <br>The class overwrites the protected method
 * {@link anja.graph.BDFSVertexIterator#addChildVertices(Vertex)} to receive a
 * reference to the neccessary edges.
 * 
 * <br>Each method from Iterator that was implemented by
 * {@link anja.graph.BDFSVertexIterator} has not been overwritten.
 * 
 * @author Andreas Lenerz
 * 
 */
public class ExtendedBDFSVertexIterator
		extends BDFSVertexIterator
{

	// *********************************
	// VARIABLES
	// *********************************

	/**
	 * Each vertex has an associated edge. For the source the edge is null for
	 * every other vertex the edge is the edge of the graph this vertex has been
	 * visited from the first time.
	 */
	protected Edge[]	_associatedEdge;


	// *********************************
	// CONSTRUCTORS
	// *********************************

	/**
	 * Default constructor (not implemented)
	 */
	public ExtendedBDFSVertexIterator()
	{}


	/**
	 * Creates an DFS-Iterator with source as its source.
	 * 
	 * @param source
	 *            The source vertex
	 */
	public ExtendedBDFSVertexIterator(
			Vertex source)
	{
		this(source, UNORDERED, true);
	}


	/**
	 * Creates an DFS-iterator with source as its source. The order in which the
	 * edges around a vertex are searched can be set via mode.
	 * 
	 * @param source
	 *            The source vertex
	 * @param mode
	 *            The mode of the BDFS
	 */
	public ExtendedBDFSVertexIterator(
			Vertex source,
			int mode)
	{
		this(source, mode, true);
	}


	/**
	 * Creates an DFS-iterator without a specific source vertex. The iterator
	 * gets its source vertex via graph.getAnyVertex()
	 * 
	 * @param graph
	 *            The graph
	 */
	public ExtendedBDFSVertexIterator(
			Graph graph)
	{
		this(graph.getAnyVertex(), UNORDERED, true);
	}


	/**
	 * Creates an Iterator with source as its source.
	 * 
	 * @param source
	 *            The source vertex
	 * @param dfs
	 *            true -> dfs, false -> bfs
	 */
	public ExtendedBDFSVertexIterator(
			Vertex source,
			boolean dfs)
	{
		this(source, UNORDERED, dfs);
	}


	/**
	 * Creates an iterator with source as its source. The order in which the
	 * edges around a vertex are searched can be set via mode.
	 * 
	 * @param source
	 *            The source vertex
	 * @param dfs
	 *            true -> dfs, false -> bfs
	 * @param mode
	 *            The mode of the BDFS
	 */
	public ExtendedBDFSVertexIterator(
			Vertex source,
			int mode,
			boolean dfs)
	{
		super(source, mode, dfs);

		Graph g = source.getGraph();
		if (g != null)
			_seenVertices = new boolean[g.getNextVertexID()];
	}


	/**
	 * Creates an iterator without a specific source vertex. The iterator gets
	 * its source vertex via graph.getAnyVertex()
	 * 
	 * @param graph
	 *            The graph
	 * @param dfs
	 *            true -> dfs, false -> bfs
	 */
	public ExtendedBDFSVertexIterator(
			Graph graph,
			boolean dfs)
	{
		this(graph.getAnyVertex(), UNORDERED, dfs);
		_currentVertex = graph.getAnyVertex();
	}


	// *********************************
	// PUBLIC METHODS
	// *********************************

	/**
	 * The associated edge == the edge the vertex has been visited from for the
	 * first time
	 * 
	 * @return The associated edge
	 */
	public Edge getAssociatedEdge()
	{
		return _associatedEdge[_currentVertex.getID()];
	}


	// *********************************
	// OVERWRITTEN/INHERITED METHODS
	// *********************************

	/**
	 * Overwritten method (
	 * {@link anja.graph.BDFSVertexIterator#addChildVertices(Vertex)})
	 */
	@Override
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
					{
						_vertexList.addFirst(newV);
						_associatedEdge[newV.getID()] = e;
					}
					else
					{
						_vertexList.addLast(newV);
						if (_associatedEdge[newV.getID()] == null)
							_associatedEdge[newV.getID()] = e;
					}
				}
			} //end while
		}
	} //end addChildVertices()

}
