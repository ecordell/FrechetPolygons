package anja.util;

import java.lang.reflect.Array;

import anja.util.ListItem;
import anja.util.SimpleList;

//import java.util.Iterator;

/**
 * A very basic double linked list, that combines some useful functions of other lists in an easy way.
 * The list uses generics and makes it not only possible to handle the current element, but also the
 * last and the first element of the list. A cyclic handling is possible and the list can be converted to
 * an array, too.
 * Constructors for e.g. SimpleList_s are available.
 * 
 * <br>The functionality of the Iterable or Iterator interface is disabled.
 * 
 * @author Andreas Lenerz
 *
 */

public class DoubleLinkedList<E> 
		//implements Iterator<E>, Iterable<E>
{

	/**
	 * Inner class, that provides all methods to manage one single list entry.
	 * 
	 * @author Andreas Lenerz
	 */
	class DoubleLinkedListEntry 
	{
		
		// *************************************************************************
		// VARIABLES
		// *************************************************************************
		
		/**
		 * The value of this item
		 */
		private E _value = null;
		

		/**
		 * Reference to the next item
		 */
		private DoubleLinkedListEntry _next = null;
		
		
		/**
		 * Reference to the next item
		 */
		private DoubleLinkedListEntry _prev = null;
		
		
		// *************************************************************************
		// CONSTRUCTORS
		// *************************************************************************

		/**
		 * Default Constructor
		 */
		public DoubleLinkedListEntry()
		{
		}
		
		
		/**
		 * Constructor
		 * 
		 * @param value The new value of this class
		 */
		public DoubleLinkedListEntry(
				E value
		)
		{
			_value = value;
		}
		
		
		// *************************************************************************
		// PUBLIC METHODS
		// *************************************************************************

		
		/**
		 * Comparison of two objects
		 * 
		 * @see java.lang.Object#equals(Object)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj)
		{
			DoubleLinkedListEntry dlle = (DoubleLinkedListEntry)obj;
			
			if ( dlle.getValue().equals(_value) )
			{
				return true;
			}
			
			return false;
		}
		
		
		/**
		 * Getter for the value of the listentry
		 * 
		 * @return The value of the list
		 */
		public E getValue()
		{
			return _value;
		}
		
		
		/**
		 * Getter for the next listentry
		 * 
		 * @return The next entry
		 */
		public DoubleLinkedListEntry getNext()
		{
			return _next;
		}
		
		
		/**
		 * Getter for the previous listentry
		 * 
		 * @return The previous entry
		 */
		public DoubleLinkedListEntry getPrev()
		{
			return _prev;
		}
		

		/**
		 * Setter for the value of the listentry
		 * 
		 * @param value The new value
		 */
		public void setValue(
			E value	
		)
		{
			_value = value;
		}
		
		
		/**
		 * Setter for the reference to the next listentry
		 * 
		 * @param next The next entry or null
		 */
		public void setNext(
				DoubleLinkedListEntry next	
		)
		{
			_next = next;
		}
		
		
		/**
		 * Setter for the reference to the previous listentry
		 * 
		 * @param prev The previous entry or null
		 */
		public void setPrev(
				DoubleLinkedListEntry prev	
		)
		{
			_prev = prev;
		}
		
		
		/**
		 * Reset this listentry, setting all content to null
		 */
		public void reset()
		{
			_next = null;
			_prev = null;
			_value = null;
		}	
	}

	
	
	// *************************************************************************
	// VARIABLES
	// *************************************************************************
		
	/**
	 * Reference to the first listitem
	 */
	private DoubleLinkedListEntry _first = null;
	
	
	/**
	 * Reference to the last listitem
	 */
	private DoubleLinkedListEntry _last = null;
	
	
	/**
	 * Reference to the current listitem
	 */
	private DoubleLinkedListEntry _current = null;
	
	
	/**
	 * The length of the list
	 */
	private int _counter = 0;
	
		
	// *************************************************************************
	// CONSTRUCTORS
	// *************************************************************************

	/**
	 * Default Constructor
	 */
	public DoubleLinkedList()
	{
		reset();
	}
	
	
	/**
	 * Converts the content of a BasicList to a DoubleLinkedList.
	 * 
	 * <br>All the elements of the BasicList have to be of the type E of
	 * the DoubleLinkedList<E>
	 * 
	 * @see anja.util.BasicList
	 * 
	 * @param list A BasicList object.
	 */
	@SuppressWarnings("unchecked")
	public DoubleLinkedList(
			SimpleList list
	)
	{
		reset();
		
		ListItem current = list.first();
			
		while (current != null)
		{
			add((E)current.value());
			current = current.next();
		}
	}
	
	
	// *************************************************************************
	// PUBLIC METHODS
	// *************************************************************************

	
	/**
	 * Adding a new value at the end of the list
	 * 
	 * @see DoubleLinkedList#addLast(Object)
	 * 
	 * @param value The new value
	 */
	public void add(
			E value
	)
	{
		addLast(value);
	}
	
	
	/**
	 * Adding a new value after the current element of the list
	 * 
	 * @param value The new value
	 */
	public void addAfterCurrent(
			E value
	)
	{
		if (isEmpty())
		{
			addLast(value);
		}
		else if (_current == _last)
		{
			addLast(value);
		}
		else
		{
			DoubleLinkedListEntry dlle = new DoubleLinkedListEntry(value);
			
			dlle.setNext( _current.getNext() );
			dlle.setPrev( _current );
			_current.setNext( dlle );
			dlle.getNext().setPrev( dlle );
			
			inc();
		}
	}
	
	
	/**
	 * Adding a new value before the current element of the list
	 * 
	 * @param value The new value
	 */
	public void addBeforeCurrent(
			E value
	)
	{
		if (isEmpty())
		{
			addLast(value);
		}
		else if (_current == _first)
		{
			addFirst(value);
		}
		else
		{
			DoubleLinkedListEntry dlle = new DoubleLinkedListEntry(value);
			
			dlle.setNext( _current );
			dlle.setPrev( _current.getPrev() );
			_current.setPrev( dlle );
			dlle.getPrev().setNext( dlle );
			
			inc();
		}
	}
	
	
	/**
	 * Adding a new value at the beginning of the list
	 * 
	 * @param value The new value
	 */
	public void addFirst(
			E value
	)
	{
		if (isEmpty())
		{
			addLast(value);
		}
		else
		{
			DoubleLinkedListEntry dlle = new DoubleLinkedListEntry(value);
			
			dlle.setNext( _first );
			_first.setPrev( dlle );
			_first = dlle;
			
			inc();
		}
	}
	
	
	/**
	 * Adding a new value at the end of the list
	 * 
	 * @param value The new value
	 */
	public void addLast(
			E value
	)
	{
		DoubleLinkedListEntry dlle = new DoubleLinkedListEntry(value);
		
		if (isEmpty())
		{
			_last = dlle;
			_first = dlle;
			_current = dlle;
			
			inc();
		}
		else
		{
			dlle.setPrev( _last );
			_last.setNext( dlle );
			_last = dlle;
			
			inc();
		}
	}
	
	
	/**
	 * Delete all list entries
	 */
	public void clear()
	{
		reset();
	}
	
	
	/**
	 * Connects the list's first and last element and makes the current
	 * element the first and the current.prev the last one
	 * 
	 * Example:
	 * List (1, 2, 3, 4, 5, 6(current), 7, 8, 9)
	 * --> (6, 7, 8, 9, 1, 2, 3, 4, 5)
	 */
	public void cutAtCurrentPosition()
	{
		if (_current != _first)
		{
			_first.setPrev(_last);
			_last.setNext(_first);
			
			_first = _current;
			_last = _current.getPrev();
			
			_last.setNext(null);
			_first.setPrev(null);
		}
	}
	
	
	/**
	 * Does the list contain the given value
	 * 
	 * @param value The value to compare
	 * @return true for yes, false for no
	 */
	public boolean contains(
			E value
	)
	{
		if (isEmpty())
		{
			return false;
		}
		
		DoubleLinkedListEntry dlle = _first;
		for (int i = 0; i < size(); ++i)
		{
			if (dlle.getValue().equals(value))
			{
				return true;
			}
			dlle = dlle.getNext();
		}
		
		return false;
	}
	
	
	/**
	 * Getting the next element to the current, treating the list as a circle
	 * 
	 * @return next or first (current==last) or null (list empty)
	 */
	public E cyclicNext()
	{
		
		if (isEmpty())
		{
			return null;
		}
		
		if (_current == _last)
		{
			_current = _first;
			return _first.getValue();
		}
		else
		{
			_current = _current.getNext();
			return _current.getValue();
		}		
	}
	
	
	/**
	 * Getting the previous element to the current, treating the list as a circle
	 * 
	 * @return prev or last (current==first) or null (list empty)
	 */
	public E cyclicPrev()
	{
		if (isEmpty())
		{
			return null;
		}
		
		if (_current == _first)
		{
			_current = _last;
			return _last.getValue();
		}
		else
		{
			_current = _current.getPrev();
			return _current.getValue();
		}	
	}
	
	
	/**
	 * Getter for the current listentry
	 * 
	 * @return The previous entry
	 */
	public E getCurrent()
	{
		if (isEmpty())
		{
			return null;
		}
		return _current.getValue();
	}
	
	
	/**
	 * Getter for the first listentry
	 * 
	 * @return The next entry
	 */
	public E getFirst()
	{
		if (isEmpty())
		{
			return null;
		}
		return _first.getValue();
	}
	
	
	/**
	 * Getter for the last listentry
	 * 
	 * @return The previous entry
	 */
	public E getLast()
	{
		if (isEmpty())
		{
			return null;
		}
		return _last.getValue();
	}
	
	
	/**
	 * Do more elements follow the current one.
	 * 
	 * @return true if current is not the end of the list, false else
	 */
	public boolean hasMoreElements()
	{
		if (isEmpty())
		{
			return false;
		}
		else
		{
			return (_current.getNext() != null);
		}
	}
	
	
	/**
	 * Do more elements follow the current one.
	 * 
	 * <br>Calls {@link #hasMoreElements()}.
	 * 
	 * @return true if current is not the end of the list, false else
	 * 
	 */
	public boolean hasNext()
	{
		return hasMoreElements();
	}
	
	
	/**
	 * Is the list empty?
	 * 
	 * @return true, if the list is empty, false else
	 */
	public boolean isEmpty()
	{
		return (size() == 0);
	}
	
	
	/**
	 * Iterator for the current class. This allows the usage of the
	 * enhanced for loop.
	 * 
	 * <br>The current element is the start for the iterator.
	 * 
	 * @return This object
	 */
	/*
	@Override
	public Iterator<E> iterator()
	{
		return this;
	}
	*/

	/**
	 * Moving the current element to the next in the list
	 * 
	 * @return The next entry or null, if the current is the last
	 */
	public E next()
	{
		if (_current == _last)
		{
			return null;
		}
		else
		{
			_current = _current.getNext();
			return _current.getValue();
		}
	}
	
	
	/**
	 * Moving the current element to the previous in the list
	 * 
	 * @return The previous entry or null, if the current is the first
	 */
	public E prev()
	{
		if (_current == _first)
		{
			return null;
		}
		else
		{
			_current = _current.getPrev();
			return _current.getValue();
		}
	}
	
	
	/**
	 * Removes the current element of the list, setting current to the previous one
	 * 
	 * @see DoubleLinkedList#removeCurrent(boolean)
	 */
	public void remove()
	{
		removeCurrent(false);		
	}
	
	
	/**
	 * Removes the current element of the list and sets the current element to the value defined by the boolean
	 * 
	 * @param nextValue Choose the next (true) or the previous (false) element after deleting the current
	 */
	public void removeCurrent(
			boolean nextValue
	)
	{
		if (_last == _first)
		{
			reset();
		}
		else if (_current == _first)
		{
			removeFirst();
		}
		else if (_current == _last)
		{
			removeLast();
		}
		else
		{
			_current.getNext().setPrev( _current.getPrev() );
			_current.getPrev().setNext( _current.getNext() );
			
			if (nextValue)
			{
				_current = _current.getNext();
			}
			else
			{
				_current = _current.getPrev();
			}
			
			dec();
		}		
	} //end removeCurrent
	
	
	/**
	 * Removes the first element of the list
	 */
	public void removeFirst()
	{
		if (!isEmpty())
		{
			if (_first != _last)
			{
				_first.getNext().setPrev(null);
				
				if (_first == _current)
				{
					_current = _first.getNext();
				}
				
				_first = _first.getNext();
				
				dec();
			}
			else
			{
				reset();
			}
		}
	}
	
	
	/**
	 * Removes the last element of the list
	 */
	public void removeLast()
	{
		if (!isEmpty())
		{
			if (_first != _last)
			{
				_last.getPrev().setNext(null);
				
				if (_last == _current)
				{
					_current = _last.getPrev();
				}
				
				_last = _last.getPrev();
				
				dec();
			}
			else
			{
				reset();
			}
		}		
	}
		
	
	
	/**
	 * Set the value of the current element of the list
	 * 
	 * @param value The new value
	 */
	public void setCurrent(
			E value
	)
	{
		if (isEmpty())
		{
			_current.setValue(value);
		}
	}
	
	
	/**
	 * The first element of the list is the new current element
	 */
	public void setCurrentToFirst()
	{
		_current = _first;
	}
	
	
	/**
	 * The last element of the list is the new current element
	 */
	public void setCurrentToLast()
	{
		_current = _last;
	}
	
	
	/**
	 * Set the value of the first element of the list
	 * 
	 * @param value The new value
	 */
	public void setFirst(
			E value
	)
	{
		if (isEmpty())
		{
			_first.setValue(value);
		}
	}
	
	
	/**
	 * Set the value of the last element of the list
	 * 
	 * @param value The new value
	 */
	public void setLast(
			E value
	)
	{
		if (isEmpty())
		{
			_last.setValue(value);
		}
	}
	
	
	/**
	 * Returns the size of the list
	 * 
	 * @return The size as an int
	 */
	public int size()
	{
		return _counter;
	}
	
	
	/**
	 * Converts the list to an array
	 * 
	 * @return The array with all list elements
	 */
	@SuppressWarnings("unchecked")
	public E[] toArray(E[] arr)
	{
		if (isEmpty())
		{
			return null;
		}
		
		arr = (E[]) Array.newInstance(arr.getClass().getComponentType(), _counter);
		
		DoubleLinkedListEntry dlle = _first;
		
		for (int i = 0; i < _counter; ++i)
		{
			arr[i] = dlle.getValue();
			dlle = dlle.getNext();
		}
		
		return arr;
	}
	
	
	/**
	 * Converts the list an its content to a string
	 * 
	 * @return A string
	 */
	public String toString()
	{
		String result = "DoubleLinkedList <" + size() + "> [";
		
		DoubleLinkedListEntry dlle = _first;
		for (int i = 0; i < size(); ++i)
		{
			result += "("+ dlle.getValue().toString() + ") | ";
			dlle = dlle.getNext();
		}
		
		if (result.endsWith(" | "))
		{
			result = result.substring(0, result.length()-3);
		}
		
		result += "]";
		
		return result;		   
	}
	
	
	// *************************************************************************
	// PRIVATE METHODS
	// *************************************************************************
	
	
	private void dec()
	{
		_counter --;
	}
	
	private void inc()
	{
		_counter ++;
	}
	
	/**
	 * Reset this list, setting all content to null
	 */
	public void reset()
	{
		_last = null;
		_first = null;
		_current = null;
		_counter = 0;
	}

}
