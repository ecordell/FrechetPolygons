
/*
 * file: MathConstants.java
 * 
 * Created on Mar 10, 2005
 */

package anja.util;

/**
 *
 * This interface defines some commonly used mathematical constants
 * that are used by the system components. Any class can implement
 * this interface to directly reference the constants.
 * 
 * @author Ibragim Kouliev
 * 
 */
public interface MathConstants
{
    //*************************************************************************
    // 		               Public constants
    //*************************************************************************
    	
    public final static double PI = Math.PI;
    public final static double HALF_PI = PI / 2.0;
    
    /**
     * IEEE754 single-precision floating point epsilon value
     */
    public final static float  FLT_EPSILON = 1.19209e-007f;	
    
    /**
     * IEEE754 double-precision floating point epsilon value
     */
    public final static double DBL_EPSILON = 2.22045e-016;
	
}




