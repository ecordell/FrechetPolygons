/*
 * Created on Mar 6, 2005
 *
 * GraphConstants.java
 * 
 */

package anja.SwingFramework.graph;

/**
 * Defines various constants which are common to graph events, undos
 * etc. Can be 'implemented' by any class that wishes to refer to
 * these constants by unqualified names (i.e. no package/class name
 * prefix). Most of these constants identify event types.
 * 
 * @author ibr
 * 
 */
public interface GraphConstants
{
    //*************************************************************************
    //				  Public constants
    //*************************************************************************
    
    // ---------------------- Event type identifiers ---------------------
    
    public static final int NO_ACTION	                        = 0xFFFFFFFF;
    
    // --------------------------- Vertex events -------------------------
    
    public static final int VERTEX_ADDED                        = 1;
    public static final int VERTEX_REMOVED                      = 2;	
    public static final int VERTEX_SELECTED                     = 3;
    
    public static final int VERTEX_MOVED                        = 4;
    public static final int VERTEX_COLOR_CHANGED                = 5;
    public static final int VERTEX_NAME_CHANGED   		= 6;
    public static final int VERTEX_RADIUS_CHANGED 		= 7;
    
    // ----------------------------- Edge events -------------------------
    
    public static final int EDGE_ADDED                          = 8;
    public static final int EDGE_REMOVED 			= 9;	
    public static final int EDGE_MODIFIED 			= 10;	
    public static final int EDGE_SELECTED 			= 11;
    
    public static final int EDGE_MOVED 				= 12;
    public static final int EDGE_COLOR_CHANGED    		= 13;
    public static final int EDGE_WEIGHT_CHANGED   		= 14;
    
    // ----------------------- Vertex selection events -------------------
    
    public static final int VERTEX_SELECTION_REMOVED 		= 15;
    public static final int VERTEX_SELECTION_MODIFIED 		= 16;
    public static final int VERTEX_SELECTION_CHANGED    	= 17;
    public static final int VERTEX_SELECTION_COLOR_CHANGED 	= 18;
    
    // ----------------------- Edge selection events ---------------------
    
    public static final int EDGE_SELECTION_MOVED                = 19;
    public static final int EDGE_SELECTION_REMOVED              = 20;
    public static final int EDGE_SELECTION_MODIFIED 	 	= 21;
    public static final int EDGE_SELECTION_CHANGED		= 22;
    public static final int EDGE_SELECTION_COLOR_CHANGED        = 23;
    
    
    // --------------------- Transformation events -----------------------
    
    public static final int SELECTION_SCALED  			= 24;
    public static final int SELECTION_ROTATED 			= 25;
    public static final int SELECTION_TRANSLATED 		= 26;
    public static final int SELECTION_SHEARED 			= 27;
    
    // ----------------------- Special events ----------------------------
    
    public static final int GRAPH_CLEARED			= 28;
		
}






