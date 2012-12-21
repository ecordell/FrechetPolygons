package anja.obstaclescene;


/**
 * @version 0.1 20.04.03
 * @author      Ulrich Handel
 */

//****************************************************************
public class QueryRectangle extends QueryArea
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************

    private int _x_min;
    private int _y_min;
    private int _x_max;
    private int _y_max;


    //************************************************************
    // constructors
    //************************************************************


    /**
     */

    //============================================================
    public QueryRectangle( int xmin, int ymin, int xmax, int ymax )
    //============================================================
    {
        _x_min = xmin;
        _y_min = ymin;
        _x_max = xmax;
        _y_max = ymax;

    } // QueryRectangle


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
        return (    xmin >= _x_min
                 && ymin >= _y_min
                 && xmax <= _x_max
                 && ymax <= _y_max );
    } // enclosesRectangle


    /**
     */

    //============================================================
    public boolean intersectsPoint( int x, int y )
    //============================================================
    {
        return (    x >= _x_min
                 && y >= _y_min
                 && x <= _x_max
                 && y <= _y_max );
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
        return Calculator.
                lineIntersectsRect( x1, y1, x2, y2,
                                    _x_min, _y_min,
                                    _x_max, _y_max );
    } // intersectsSegment


    /**
     */

    //============================================================
    public int getQuadranten( int     x_east,
                              int     y_north,
                              short[] quadranten )
    //============================================================
    {
        int number = 0;

        if( _x_max >= x_east )
        {
            if( _y_max >= y_north )
                quadranten[ number ++ ] = NORTH_EAST;

            if( _y_min < y_north )
                quadranten[ number ++ ] = SOUTH_EAST;
        } // if

        if( _x_min < x_east )
        {
            if( _y_max >= y_north )
                quadranten[ number ++ ] = NORTH_WEST;

            if( _y_min < y_north )
                quadranten[ number ++ ] = SOUTH_WEST;
        } // if

        return number;
    } // getQuadranten

} // QueryRectangle
