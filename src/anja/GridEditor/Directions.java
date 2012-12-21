package anja.GridEditor;

/*
 *  Defines the constants for the 4 directions.
 *  Meant to be used as static import.
 *  It is not an enum so that an array can be used in
 *  the Cell class instead of an EnumMap. 
 */

public class Directions {
	
	public final static int NORTH = 0;
	public final static int EAST = 1;
	public final static int SOUTH = 2;
	public final static int WEST = 3;
	public final static int NONE = -1;
}
