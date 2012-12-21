package anja.util;

/**
 * Ein LinkedStdTreeItem ist ein StdTreeItem, welches das Interface
 * LinkedTreeItem implementiert.
 *
 * LinkedStdTreeItem's koennen linear miteinander verkettet werden.
 * Dazu hat jedes LinkedStdTreeItem jeweils einen Verweis auf
 * seinen Vorgaenger und seinen Nachfolger.
 * Vorgaenger und Nachfolger koennen mit den Interface-Methoden
 * setPrev(..), setNext(..), getPrev() und getNext() gesetzt und
 * abgefragt werden.
 *
 * LinkedStdTreeItem's werden z.B. von einem ExtendedRedBlackTree
 * erzeugt und/oder benutzt ( @see ExtendedRedBlackTree ), bei dem
 * die TreeItem's in LEFT_ROOT_RIGHT_ORDER ( @see BasicTree )
 * miteinander verkettet sind.
 *
 * @version 0.1 15.11.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class LinkedStdTreeItem extends    StdTreeItem
                               implements LinkedTreeItem
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************


    private TreeItem _item_prev = null; // Vorgaenger
    private TreeItem _item_next = null; // Nachfolger



    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen LinkedStdTreeItem's ohne Wertzuweisung.
     * ( @see StdTreeItem )
     */

    //============================================================
    public LinkedStdTreeItem( int maxrank )
    //============================================================
    {
        super( maxrank );
    } // LinkedStdTreeItem



    /**
     * Erzeugen eines neuen LinkedStdTreeItem's mit Wertzuweisung.
     * ( @see StdTreeItem )
     */

    //============================================================
    public LinkedStdTreeItem( int    maxrank,
                              Object key,
                              Object value )
    //============================================================
    {
        super( maxrank, key, value );
    } // LinkedStdTreeItem




    //************************************************************
    // Interface-Methoden fuer LinkedTreeItem
    //************************************************************


    /**
     * @return  Nachfolger dieses TreeItem's bzw. null, falls
     *          kein Nachfolger vorhanden ist
     */

    //============================================================
    public TreeItem getNext()
    //============================================================
    {
        return _item_next;
    } // getNext


    /**
     * @return  Vorgaenger dieses TreeItem's bzw. null, falls
     *          kein Vorgaenger vorhanden ist
     */

    //============================================================
    public TreeItem getPrev()
    //============================================================
    {
        return _item_prev;
    } // getPrev


    /**
     * Setzen des Nachfolgers.
     *
     * ( Der Aufruf setNext( null ) loescht den Verweis auf den
     *   ggf. gesetzten Nachfolger )
     */

    //============================================================
    public void setNext( TreeItem item )
    //============================================================
    {
        _item_next = item;
    } // setNext


    /**
     * Setzen des Vorgaengers.
     *
     * ( Der Aufruf setPrev( null ) loescht den Verweis auf den
     *   ggf. gesetzten Vorgaenger )
     */

    //============================================================
    public void setPrev( TreeItem item )
    //============================================================
    {
        _item_prev = item;
    } // setPrev


} // LinkedStdTreeItem
