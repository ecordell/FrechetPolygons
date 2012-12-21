package anja.geom;


/**
 * Point2GraphBFS is an iterator for a Point2Graph. The nodes of the graph are
 * visited in breath-first order.
 * 
 * @version 0.1 11.07.01
 * @author Wolfgang Meiswinkel, Thomas Kamphans
 */
public class Point2GraphBFS
		implements java.io.Serializable
{

	//************************************************************
	// public constants
	//************************************************************

	//************************************************************
	// private variables
	//************************************************************

	Point2StackQueue	_queue;


	//************************************************************
	// constructors
	//************************************************************

	/**
	 * Creates new BFS iterator from Point2Graph
	 * 
	 * @param p
	 *            The graph
	 */
	public Point2GraphBFS(
			Point2Graph p)
	{
		//TODO
	} // Point2GraphBFS


	/**
	 * Creates new BFS iterator from given BFS iterator
	 * 
	 * @param p
	 *            The BFS iterator
	 */
	public Point2GraphBFS(
			Point2GraphBFS p)
	{
		//TODO
	} // Point2GraphBFS


	//************************************************************
	// class methods
	//************************************************************

	//************************************************************
	// public methods
	//************************************************************

	/**
	 * Return the next point from the graph in bfs order
	 * 
	 * This method is not yet implemented.
	 * 
	 * @return The next point in BFS, NOW: null!
	 */
	public Point2 next()
	{
		//TODO
		return null;
	} // next


	/**
	 * Convert to a string
	 * 
	 * @return A textual representation of this object
	 */
	public String toString()
	{
		String s;

		s = "<Point2GraphBFS :";
		//TODO:
		// Hier wirklich einen String basteln und zurueckgeben, nicht
		// einfach nur mit System.out.println ausgeben !!!!
		s += ">";

		return s;

	} // toString

	//************************************************************
	// Private methods
	//************************************************************

} // Point2GraphBFS
