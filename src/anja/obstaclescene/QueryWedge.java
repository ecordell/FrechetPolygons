package anja.obstaclescene;


/**
 * @version 0.1 20.04.03
 * @author      Ulrich Handel
 */

//****************************************************************
public class QueryWedge extends QueryArea
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************

    private int _x_vertex;
    private int _y_vertex;

    private int _x_left;
    private int _y_left;

    private int _x_right;
    private int _y_right;


    //************************************************************
    // constructors
    //************************************************************


    /**
     */

    //============================================================
    public QueryWedge( int x_vertex, int y_vertex,
                       int x_left,   int y_left,
                       int x_right,  int y_right )
    //============================================================
    {

        _x_vertex = x_vertex;
        _y_vertex = y_vertex;

        _x_left = x_left;
        _y_left = y_left;

        _x_right = x_right;
        _y_right = y_right;
    } // QueryWedge


    //************************************************************
    // public methods
    //************************************************************


    /**
     */

    //============================================================
    public boolean enclosesRectangle( int xmin,
                                      int ymin,
                                      int xmax,
                                      int ymax )
    //============================================================
    {
//+
        return false;
    } // enclosesRectangle


    /**
     */

    //============================================================
    public boolean intersectsPoint( int x, int y )
    //============================================================
    {
//+
        return false;
    } // intersectsPoint


    /**
     */

    //============================================================
    public boolean intersectsSegment( int x1,
                                      int y1,
                                      int x2,
                                      int y2 )
    //============================================================
    {
//+
        return false;
    } // intersectsSegment


    /**
     */

    //============================================================
    public int getQuadranten( int     x_east,
                              int     y_north,
                              short[] quadranten )
    //============================================================
    {
//+
        return 0;
    } // getQuadranten

} // QueryWedge
