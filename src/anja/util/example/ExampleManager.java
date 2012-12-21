package anja.util.example;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * A manager class to handle multiple examples. It is based on an
 * <code>ArrayList</code> but overrides several methods to work with the
 * <code>Example</code> in particular.
 * 
 * @author Andreas Lenerz, 27.09.2011
 * @version 1.0
 * 
 * @see java.util.ArrayList
 * @see anja.util.example.Example
 * @see anja.util.example.ExamplePanel
 */
public class ExampleManager<E extends Example>
		extends ArrayList<E>
{

	/**
	 * Basic constructor
	 */
	public ExampleManager()
	{
		super();
	}


	/**
	 * A contructor that adds all elements of the <code>Collection</code> to the
	 * list/this object.
	 * 
	 * <br>
	 * 
	 * As <code>ExampleManager</code> itself is a <code>Collection</code> this
	 * constructor can be used to copy the contents of one manager
	 * 
	 * @param em
	 *            The collection
	 * 
	 * @see java.util.Collection
	 */
	public ExampleManager(
			Collection<E> em)
	{
		this();
		this.addAll(em);
	}


	@Override
	public boolean contains(
			Object o)
	{
		return indexOf(o) >= 0;
	}


	@Override
	public int indexOf(
			Object o)
	{
		Example e = null;
		try
		{
			e = (Example) o;
		}
		catch (Exception exe)
		{
			return -1;
		}

		if (e != null)
		{
			int counter = 0;
			for (Example ex : this)
			{
				/*
				 * Compare the names of the objects instead of the
				 * objects itself
				 */
				if (e.toString().equals(ex.toString()))
					return counter;
				counter++;
			}
		}

		return -1;
	}


	@Override
	public boolean remove(
			Object o)
	{
		Example e = null;
		try
		{
			e = (Example) o;
		}
		catch (Exception exe)
		{
			return false;
		}

		boolean isDeleted = false;

		if (e != null)
		{
			Iterator<E> it = this.iterator();
			Example ex = null;
			while ((ex = it.next()) != null)
			{
				/*
				 * Compare the names of the objects instead of the
				 * objects itself
				 */
				if (e.toString().equals(ex.toString()))
				{
					it.remove();
					isDeleted = true;
				}
			}
		}

		return isDeleted;
	}

}
