package anja.geom;


/**
 * Point2GraphDFS is an iterator for a Point2Graph. The nodes of the graph are
 * visited in depth-first order.
 * 
 * @version 0.1 11.07.01
 * @author Wolfgang Meiswinkel, Thomas Kamphans
 */
public class Point2GraphDFS
		implements java.io.Serializable
{

	//************************************************************
	// public constants
	//************************************************************

	//************************************************************
	// private variables
	//************************************************************

	//************************************************************
	// constructors
	//************************************************************

	/**
	 * Creates new DFS iterator from Point2Graph
	 * 
	 * @param p
	 *            The graph
	 */
	public Point2GraphDFS(
			Point2Graph p)
	{
		//TODO
	} // Point2GraphDFS


	/**
	 * Creates new DFS iterator from given DFS iterator
	 * 
	 * @param p
	 *            The DFS iterator
	 */
	public Point2GraphDFS(
			Point2GraphDFS p)
	{
		//TODO
	} // Point2GraphDFS


	//************************************************************
	// class methods
	//************************************************************

	//************************************************************
	// public methods
	//************************************************************

	/**
	 * Return the next point from the graph in dfs order
	 * 
	 * @return The next point in dfs
	 */
	public Point2 next()
	{
		//TODO
		return null;
	} // next


	/**
	 * Convert to string
	 * 
	 * @return A textual representation of this object
	 */
	public String toString()
	{
		String s;

		s = "<Point2GraphDFS :";
		//TODO:
		// Hier wirklich einen String basteln und zurueckgeben, nicht
		// einfach nur mit System.out.println ausgeben !!!!
		s += ">";

		return s;

	} // toString

	//************************************************************
	// Private methods
	//************************************************************

} // Point2GraphDFS
