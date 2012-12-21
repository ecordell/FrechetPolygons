package anja.obstaclescene;


/**
 * @version 0.1 20.04.03
 * @author      Ulrich Handel
 */

//****************************************************************
public class QueryCircle extends QueryArea
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************

    private int _x_center;
    private int _y_center;
    private int _radius;


    //************************************************************
    // constructors
    //************************************************************


    /**
     */

    //============================================================
    public QueryCircle( int x_center, int y_center, int radius )
    //============================================================
    {
        _x_center = x_center;
        _y_center = y_center;
        _radius   = radius;

    } // QueryCircle


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
        long dx = Math.max( _x_center - xmin,  xmax - _x_center );
        long dy = Math.max( _y_center - ymin,  ymax - _y_center );

        return (    ( dx * dx + dy * dy )
                 <= (long)_radius * (long)_radius );
    } // enclosesRectangle


    /**
     */

    //============================================================
    public boolean intersectsPoint( int x, int y )
    //============================================================
    {
        long dx = x - _x_center;
        long dy = y - _y_center;

        return (    ( dx * dx + dy * dy )
                 <= (long)_radius * (long)_radius );
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
        short we = ( _x_center >= x_east  ) ? EAST : WEST;
        short sn = ( _y_center >= y_north ) ? NORTH : SOUTH;

        int number = 0;

        quadranten[ number++ ] = (short)( we | sn );

        if( we == EAST )
        {
            if( ( _x_center - _radius ) < x_east )
                quadranten[ number++ ] = (short)( WEST | sn );
        }
        else
        {
            if( ( _x_center + _radius ) >= x_east )
                quadranten[ number++ ] = (short)( EAST | sn );
        } // else

        if( sn == NORTH )
        {
            if( ( _y_center - _radius ) < y_north )
                quadranten[ number++ ] = (short)( we | SOUTH );
        }
        else
        {
            if( ( _y_center + _radius ) >= y_north )
                quadranten[ number++ ] = (short)( we | NORTH );
        } // else


        if( number == 3 )
        {
            long dx = 2 * ( _x_center - x_east ) + 1;
            long dy = 2 * ( _y_center - y_north ) + 1;

            if(    ( dx * dx ) + ( dy * dy )
                >= 4 * ( long )_radius * _radius )
            {
                we = ( we == EAST )  ? WEST  : EAST;
                sn = ( sn == NORTH ) ? SOUTH : NORTH;
                quadranten[ number++ ] = (short)( we | sn );
            } // if

        } // if

        return number;
    } // getQuadranten

} // QueryCircle
