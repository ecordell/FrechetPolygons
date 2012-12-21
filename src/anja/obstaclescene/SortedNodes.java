package anja.obstaclescene;

import anja.util.*;

/**
 *
 * @version 0.1 15.11.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class SortedNodes extends ExtendedRedBlackTree
//****************************************************************
{
    //************************************************************
    // public constants
    //************************************************************

    public final static int X_ORDER = 0;
    public final static int Y_ORDER = 1;


    //************************************************************
    // private variables
    //************************************************************

    private int _order; // X_ORDER oder Y_ORDER


    //************************************************************
    // constructors
    //************************************************************


    /**
     */

    //============================================================
    public SortedNodes( int order )
    //============================================================
    {
        super( ( order == X_ORDER ) ?
               Node.getXComparitor() :
               Node.getYComparitor() );

        _order = order;
    } // SortedNodes



    //************************************************************
    // public methods
    //************************************************************


    /**
     */

    //============================================================
    public int getMinCoor()
    //============================================================
    {
        return _getCoor( ( BasicTreeItem )getFirst() );
    } // getMinCoor


    /**
     */

    //============================================================
    public int getMaxCoor()
    //============================================================
    {
        return _getCoor( ( BasicTreeItem )getLast() );
    } // getMaxCoor



    //************************************************************
    // private methods
    //************************************************************


    /**
     */

    //============================================================
    private int _getCoor( BasicTreeItem item )
    //============================================================
    {
        if( item == null )
            return 0;

        Node node = ( Node )item.key();

        return ( _order == X_ORDER ) ?
               node.getX() :
               node.getY();
    } // _getCoor


} // SortedNodes
