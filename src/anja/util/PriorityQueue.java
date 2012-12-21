package anja.util;

import anja.geom.*;
import java.util.Vector;

/**
 * A Point2 priority queue. DeleteMin returns Point2 object 
 * with minimal value. 
 *  
 * DELETEMIN NOT OPTIMIZED YET !!!!! 
 *
 * @version	0.1  04.09.01
 * @author      Wolfgang Meiswinkel, Thomas Kamphans
 */

//****************************************************************
public class PriorityQueue
	implements 	Cloneable
//****************************************************************
{

    //************************************************************
    // private variables
    //************************************************************

    protected Vector _PQueue;

    //************************************************************
    // constructors
    //************************************************************


    /**
     * create new and empty priority queue
     */

    //============================================================
    public PriorityQueue()
    //============================================================
    {
	_PQueue = new Vector ();
    } // PriorityQueue

    /**
     * create new priority queue with copies of the given queue
     */

    //============================================================
    public PriorityQueue( PriorityQueue s )
    //============================================================
    {
	_PQueue = (Vector)  s._PQueue.clone();
    } // PriorityQueue


    //************************************************************
    // public methods
    //************************************************************

    //============================================================
    public Object clone()
    //============================================================
    {
	return ( new PriorityQueue ( this ) );
    } // clone

    /**
    * return true if queue is empty
    */

    //============================================================
    public boolean isEmpty( )
    //============================================================
    {
     return _PQueue.size()==0;
    } // isEmpty()

    /**
    * return number of queue elements
    */
     
    //============================================================
    public int noOfElements( )
    //============================================================
    {
     return _PQueue.size();
    } // noOfElements()



    /**
     * insert Point2 into queue
     */

    //============================================================
    public void insert( Point2 p )
    //============================================================
     {
      _PQueue.addElement( p );
    } // insert


    /**
     * get minimum from queue and remove it
     */

    //============================================================
    public Point2 deleteMin ( )
    //============================================================
    {
	if ( _PQueue.size()!=0 )
        { 
	  double aktMin = Double.POSITIVE_INFINITY;
	  int index =0;
	  for (int i=0;i<_PQueue.size();i++)
	   {
	    Point2 testPoint = ((Point2) _PQueue.elementAt(i));
	    float aktWhight = testPoint.getValue();
            if(aktWhight<aktMin)
	     {
	      aktMin=aktWhight;
	      index = i;
	     }
	   }
         Point2 _return = (Point2)_PQueue.elementAt(index);
	 _PQueue.removeElementAt(index);
	 return _return;
	}
	return null;
    } // deleteMin


} // PriorityQueue
