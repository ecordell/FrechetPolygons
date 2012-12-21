package anja.util.example;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * Some utilities which port objects to byte arrays or java code and the other
 * way round.
 * 
 * <br>
 * 
 * With this class it is possible to save an <code>Example</code> while running
 * the application and afterwards use the byte array to rebuild the
 * <code>Example</code> object.
 * 
 * <br>
 * 
 * Usually to build an example, all variables like polygons, points and so on
 * have to be generated seperately from each other and then be put together to
 * an <code>Example</code> object which can be read during runtime. This
 * adapters offer the possibility to save each of the variables during runtime
 * as a reference in an <code>Example</code> object and save the whole object at
 * a piece without caring about the single variables in it.
 * 
 * <br>
 * 
 * Every variable needed has to implement the <code>Serializable</code> object.
 * All other variables aren't written to the byte array.
 * 
 * @author Andreas Lenerz, 6.10.2011
 * @version 1.0
 * 
 * @see java.io.Serializable
 * @see anja.util.example.Example
 */
public class Adapters
{

	private Adapters()
	{}


	/**
	 * Reads an object and converts it into an byte array. The object has to
	 * implement <code>Serializable</code>.
	 * 
	 * @param <E>
	 *            The type of the object class which has to implement
	 *            <code>Serializable</code>
	 * @param o
	 *            The object
	 * 
	 * @return The byte array
	 * 
	 * @see java.io.Serializable
	 */
	public static <E extends Serializable> byte[] toByteArray(
			E o)
	{
		/*
		 * Use an ByteArrayOS, pipe it to the ObjectOS
		 * and return the result after the streams are closed
		 * 
		 */
		if (o == null)
			throw new NullPointerException();

		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try
		{
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);

			oos.writeObject(o);
		}
		catch (IOException e)
		{}
		finally
		{
			/*
			 * Try to close all streams
			 */
			if (oos != null)
			{
				try
				{
					oos.close();
				}
				catch (IOException e)
				{}
			}
			if (bos != null)
			{
				try
				{
					bos.close();
				}
				catch (IOException e)
				{}
			}
		}

		return bos.toByteArray();
	}


	/**
	 * Uses <code>toByteArray()</code> and converts the result to Java code
	 * which is returned as a <code>String</code>
	 * 
	 * @param <E>
	 *            The type of the object class which has to implement
	 *            <code>Serializable</code>
	 * @param o
	 *            The object
	 * 
	 * @return The byte array
	 * 
	 * @see anja.util.example.Adapters#toByteArray(Serializable)
	 * @see java.lang.String
	 * @see java.io.Serializable
	 */
	public static <E extends Serializable> String toJavaCode(
			E o)
	{
		byte[] b = Adapters.toByteArray(o);

		StringBuffer buf = new StringBuffer();
		buf.append("byte[] b = { ");
		for (int i = 0; i < b.length; ++i)
		{
			buf.append(b[i]);
			if (i < b.length - 1)
				buf.append(", ");
		}
		buf.append("};");

		return buf.toString();
	}


	/**
	 * This class interprets the given byte array as an object and returns an
	 * instance of this object.
	 * 
	 * @param b
	 *            The byte array
	 * 
	 * @return An instance of the object or null, if an <code>Exception</code>
	 *         occurs
	 */
	public static Object toJavaCode(
			byte[] b)
	{
		/*
		 * Use the ByteArrayIS, pipe it to the ObjectIS
		 * and try to read the object from the data.
		 * 
		 */
		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		Object o = null;

		try
		{
			bis = new ByteArrayInputStream(b);
			ois = new ObjectInputStream(bis);

			o = ois.readObject();
		}
		catch (IOException e)
		{}
		catch (ClassNotFoundException e)
		{}
		finally
		{
			/*
			 * Try to close all streams
			 */
			if (ois != null)
			{
				try
				{
					ois.close();
				}
				catch (IOException e)
				{}
			}
			if (bis != null)
			{
				try
				{
					bis.close();
				}
				catch (IOException e)
				{}
			}
		}

		return o;
	}

}
