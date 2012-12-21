package anja.geom;


import java.util.Stack;


/**
 * A list of points, that can be used either as stack or as queue
 * 
 * @version 0.1 11.07.01
 * @author Wolfgang Meiswinkel, Thomas Kamphans
 */

//****************************************************************
public class Point2StackQueue
		implements Cloneable, java.io.Serializable
//****************************************************************
{

	//************************************************************
	// private variables
	//************************************************************

	protected Stack	_stk;


	//************************************************************
	// constructors
	//************************************************************

	/**
	 * Create new and empty list
	 */
	public Point2StackQueue()
	{
		_stk = new Stack();
	} // Point2StackQueue


	/**
	 * Create new list with copies of the given list
	 * 
	 * @param s
	 *            The input list
	 */
	public Point2StackQueue(
			Point2StackQueue s)
	{
		_stk = (Stack) s._stk.clone();
	} // Point2StackQueue


	//************************************************************
	// public methods
	//************************************************************

	/**
	 * Clone this object
	 * 
	 * @return A copy of this object
	 */
	public Object clone()
	{
		return (new Point2StackQueue(this));
	} // clone


	/**
	 * Put point on stack
	 * 
	 * @param p
	 *            The point to push on the stack
	 */
	public void push(
			Point2 p)
	{
		_stk.push(p);
	} // push


	/**
	 * Get point from stack and remove it
	 * 
	 * @return Top element of the stack
	 */
	public Point2 pop()
	{
		if (!_stk.empty())
		{
			return (Point2) _stk.pop();
		}
		return null;
	} // pop


	/**
	 * Get point from stack, the stack remains unchanged
	 * 
	 * @return Top element of the stack
	 */
	public Point2 top()
	{
		if (!_stk.empty())
		{
			return (Point2) _stk.peek();
		}
		return null;
	} // top


	/**
	 * Insert point into queue
	 * 
	 * @param p
	 *            The point to insert
	 */
	public void insert(
			Point2 p)
	{
		_stk.push(p);
	} // insert


	/**
	 * Get point from queue and remove that item
	 * 
	 * @return The first element of the queue
	 */
	public Point2 delete()
	{
		Point2 p;
		if (!_stk.empty())
		{
			p = (Point2) _stk.elementAt(0);
			_stk.removeElementAt(0);
			return p;
		}
		return null;
	} // delete


	/**
	 * Get point from queue, the queue remains unchanged
	 * 
	 * @return The first element of the queue
	 */
	public Point2 first()
	{
		if (!_stk.empty())
		{
			return (Point2) _stk.elementAt(0);
		}
		return null;
	} // first


	/**
	 * True, if the list contains no element
	 * 
	 * @return true, if the list is empty, false else
	 */
	public boolean isEmpty()
	{
		return _stk.empty();
	} // isEmpty


	/**
	 * Convert to string
	 * 
	 * @return A string object representing the content of this object
	 */
	public String toString()
	{
		String s;
		s = "Point2StackQueue <";
		int i = _stk.size() - 1;
		for (int j = i; j >= 0; j--)
		{
			s += _stk.elementAt(j).toString();
		}
		s += " >";
		return s;
	} // toString

	//************************************************************
	// Private methods
	//************************************************************

} // Point2StackQueue
