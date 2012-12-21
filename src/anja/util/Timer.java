/**
 * 
 */
package anja.util;

/**
 * This class provides a timer function, that allows to make applications (animations)
 * run at the same speed on different systems, which not neccessary have the same CPU power.
 * 
 * The usage is easy:
 * Just make an object of this timer class. The time to set is the time, that the timer should wait
 * during your program. Every time the start() method is called, the timer resets to the current time.
 * After calling the await() method, the class uses Thread.sleep() to interrupt the execution as long
 * as it was set before minus the time, the calls of start() and await() differ.
 * 
 * By default the time to wait is set to 20fps.
 * 
 * Usage:
 * int fps = ...;
 * Timer t = new Timer(fps);
 * t.start();
 * do {
 * 	// Calculations here
 * 	// Draw to buffer
 * 	t.await();
 * 	// Draw buffer to screen
 * } while (Condition);
 * 
 * 
 * 
 * @author Andreas Lenerz, Juni 2009
 *
 */
public class Timer {

	// *************************************************************************
	// PRIVATE VARIABLES
	// *************************************************************************
	
	/**
	 * The time, the start() method has been called
	 */
	private long _starting_time = 0;
	
	
	/**
	 * The time, the timer should wait after calling the await() method
	 */
	private long _waiting_time = 0;
	
	
	// *************************************************************************
	// CONSTRUCTORS
	// *************************************************************************
	
	/**
	 * Default constructor, setting the waiting time to 50ms, which is 20fps
	 */
	public Timer()
	{
		_waiting_time = 50l;
	}
	
	/**
	 * The waiting time in milleseconds
	 * 
	 * @param milliseconds Waiting time
	 */
	public Timer(long milliseconds)
	{
		_waiting_time = milliseconds;
	}
	
	
	/**
	 * Instead of the waiting period, you can use this to set the fps. As a second only has
	 * 1000ms, a value of fps > 100 is not sensible.
	 * 
	 * @param fps The frames per second.
	 */
	public Timer(int fps)
	{
		this();
		
		if (fps <= 1000)
		{
			_waiting_time = (long)(1000/fps);
			
			if (_waiting_time == 0l)
			{
				_waiting_time = 1l;
			}
		}
		
	}
	
	
	// *************************************************************************
	// CONSTRUCTORS
	// *************************************************************************
	
	/**
	 * Returns the total time, the timer will wait at the most after the call of wait
	 * 
	 * @return The time to wait at the most
	 */
	public long getWaitingTime()
	{
		return _waiting_time;
	}
	
	
	/**
	 * Setter for the time to wait at the most
	 * 
	 * @param waiting_time The time in milliseconds
	 */
	public void setWaitingTime(long waiting_time)
	{
		_waiting_time = waiting_time;
	}
	
	
	/**
	 * Setter for the time to wait at the most in fps
	 * 
	 * @param fps The time in fps
	 */
	public void setWaitingTime(int fps)
	{
		_waiting_time = 20l;
		
		if (fps <= 1000)
		{
			_waiting_time = (long)(1000/fps);
			
			if (_waiting_time == 0l)
			{
				_waiting_time = 1l;
			}
		}
	}
	
	
	/**
	 * Starting the timer using the client system's time.
	 * Every time this method is called, the timer is set to the current system time. So this method
	 * is essential before you use the timer the first time.
	 */
	public void start()
	{
		_starting_time = System.currentTimeMillis();
	}
	
	
	/**
	 * This method takes the current time, subtracts the time, when start() was executed or
	 * await() was executed the last time, and compares this value to the time to wait, 
	 * that was given by the constructor. If there is time to wait left
	 * the algorithm will try to put the execution asleep for the remaining time.
	 * await can be called multiple times. Each time, the "frame" time is increased by the time to wait,
	 * so that each frame has the same length. 
	 * 
	 * The usage is:
	 * int fps = ...;
	 * Timer t = new Timer(fps);
	 * t.start();
	 * do {
	 * 	// Calculations here
	 * 	// Draw to buffer
	 * 	t.await();
	 * 	// Draw buffer to screen
	 * } while (Condition);
	 * 
	 * 
	 * This usage works with Timer(long), too.
	 */
	public void await()
	{
		long current = System.currentTimeMillis();
		
		long difference = current - _starting_time;
		
		if (difference < _waiting_time)
		{
			try
			{
				Thread.sleep(_waiting_time - difference);
			}
			catch (InterruptedException e)
			{
				System.err.println("Error in Timer: "+e.toString());
			}
		}
		else
		{
			//The time to wait is already over.
		}
		
		_starting_time = System.currentTimeMillis();
	}
}
