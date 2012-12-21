package anja.util;


/**
 * Ein ExtendedRedBlackTree ist ein RedBlackTree ( @see RedBlackTree ),
 * bei dem die TreeItem's zusaetzlich in LEFT_ROOT_RIGHT_ORDER
 * ( @see BasicTree ) zu einer Liste verkettet sind.
 *
 * Das erste und das letzte TreeItem der Liste, sowie Vorgaenger
 * und Nachfolger eines gegebenen TreeItems, koennen in konstanter
 * Zeit ermittelt werden.
 * Dazu stehen die elementaren Methoden
 *      getFirst(), getLast(), getPrev(..) und getNext(..)
 * zur Verfuegung.
 *
 * Die Methoden getSmallest(), getBiggest(), getSmaller(..) und
 * getBigger(..) entsprechen den obigen Methoden, wobei die
 * Ordnung ( @see BinarySearchTree.leftIsSmaller() ) beruecksichtigt
 * wird.
 *
 * Zum zyklischen Durchlaufen der TreeItem-Liste stehen die Methoden
 * getPrevCyclic(..), getNextCyclic(..), getSmallerCyclic(..)
 * und getBiggerCyclic(..) zur Verfuegung.
 * Diese sind z.B. dann hilfreich, wenn eine Ordnung nach Winkeln
 * besteht.
 *
 * Um die Verkettung der TreeItems zu ermoeglichen, koennen in einen
 * ExtendedRedBlackTree nur TreeItem's eingefuegt werden
 * ( @see add(..) ), die das Interface LinkedTreeItem implementieren.
 * Fuer alle TreeItems, die "automatisch" neu erzeugt werden ist dies
 * gegeben ( @see LinkedSimpleTreeItem und LinkedStdTreeItem ).
 *
 * @version 0.1 15.11.02
 * @author      Ulrich Handel
 */


//****************************************************************
public class ExtendedRedBlackTree extends RedBlackTree
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************


    private TreeItem _item_first; // erstes  TreeItem
    private TreeItem _item_last;  // letztes TreeItem
                                  //   ( LEFT_ROOT_RIGHT_ORDER )


    //************************************************************
    // constructors
    //************************************************************


    /**
     * Konstruktor ohne Comparitor.
     * ( zum Vergleich werden die Hash-Werte der Objekte benutzt )
     *
     * @see RedBlackTree
     */

    //============================================================
    public ExtendedRedBlackTree()
    //============================================================
    {
        super();
    } // ExtendedRedBlackTree


    /**
     * Konstruktor mit Comparitor.
     * ( kleinere Schluessel werden in linken Teilbaeumen gespeichert )
     *
     * @see RedBlackTree
     */

    //============================================================
    public ExtendedRedBlackTree( Comparitor comparitor )
    //============================================================
    {
        super( comparitor );
    } // ExtendedRedBlackTree


    /**
     * Konstruktor mit Comparitor und Angabe, ob kleinere Schluessel
     * in linken oder rechten Teilbaeumen gespeichert werden.
     *
     * @see RedBlackTree
     */

    //============================================================
    public ExtendedRedBlackTree( Comparitor comparitor,
                                 boolean    left_is_smaller )
    //============================================================
    {
        super( comparitor, left_is_smaller );
    } // ExtendedRedBlackTree





    //************************************************************
    // public methods
    //
    // Navigation durch die verkettete Liste der TreeItem's
    //************************************************************


    /**
     * @return  erstes TreeItem ( LEFT_ROOT_RIGHT_ORDER ) des Baumes
     *          bzw. null, falls der Baum leer ist.
     */

    //============================================================
    public TreeItem getFirst()
    //============================================================
    {
        return _item_first;
    } // getFirst


    /**
     * @return  letztes TreeItem ( LEFT_ROOT_RIGHT_ORDER ) des Baumes
     *          bzw. null, falls der Baum leer ist.
     */

    //============================================================
    public TreeItem getLast()
    //============================================================
    {
        return _item_last;
    } // getLast


    /**
     * @return  Nachfolger des TreeItem's item ( LEFT_ROOT_RIGHT_ORDER )
     *          bzw. null, falls kein Nachfolger existiert
     *
     * Die Methode liefert null, falls das gegebenen TreeItem
     * gar nicht zu diesem Baum gehoert.
     */

    //============================================================
    public TreeItem getNext( TreeItem item )
    //============================================================
    {
        if(    item == null
            || item.getOwningTree() != this )
            return null;

        return ( ( LinkedTreeItem ) item ).getNext();
    } // getNext


    /**
     * @return  Vorgaenger des TreeItem's item ( LEFT_ROOT_RIGHT_ORDER )
     *          bzw. null, falls kein Vorgaenger existiert
     *
     * Die Methode liefert null, falls das gegebenen TreeItem
     * gar nicht zu diesem Baum gehoert.
     */

    //============================================================
    public TreeItem getPrev( TreeItem item )
    //============================================================
    {
        if(    item == null
            || item.getOwningTree() != this )
            return null;

        return ( ( LinkedTreeItem ) item ).getPrev();
    } // getPrev


    /**
     * @return  Zyklischer Nachfolger des TreeItem's item
     *          ( LEFT_ROOT_RIGHT_ORDER )
     *
     * Die Methode liefert das erste TreeItem, falls es sich beim
     * gegebenen TreeItem um das letzte TreeItem handelt
     *
     * Die Methode liefert null, falls das gegebenen TreeItem
     * gar nicht zu diesem Baum gehoert.
     */

    //============================================================
    public TreeItem getNextCyclic( TreeItem item )
    //============================================================
    {
        if(    item == null
            || item.getOwningTree() != this )
            return null;

        return ( item == _item_last ) ?
               _item_first :
               ( ( LinkedTreeItem ) item ).getNext();
    } // getNextCyclic


    /**
     * @return  Zyklischer Vorgaenger des TreeItem's item
     *          ( LEFT_ROOT_RIGHT_ORDER )
     *
     * Die Methode liefert das letzte TreeItem, falls es sich beim
     * gegebenen TreeItem um das erste TreeItem handelt
     *
     * Die Methode liefert null, falls das gegebenen TreeItem
     * gar nicht zu diesem Baum gehoert.
     */

    //============================================================
    public TreeItem getPrevCyclic( TreeItem item )
    //============================================================
    {
        if(    item == null
            || item.getOwningTree() != this )
            return null;

        return ( item == _item_first ) ?
               _item_last :
               ( ( LinkedTreeItem ) item ).getPrev();
    } // getPrevCyclic


    /**
     * @return  TreeItem mit kleinstem Schluessel
     *          bzw. null, falls der Baum leer ist.
     */

    //============================================================
    public TreeItem getSmallest()
    //============================================================
    {
        return leftIsSmaller() ? getFirst() : getLast();
    } // getSmallest


    /**
     * @return  TreeItem mit groesstem Schluessel
     *          bzw. null, falls der Baum leer ist.
     */

    //============================================================
    public TreeItem getBiggest()
    //============================================================
    {
        return leftIsSmaller() ? getLast() : getFirst();
    } // getBiggest


    /**
     * @return  TreeItem mit dem naechst groesserem Schluessel als
     *          der des TreeItems item
     *          bzw. null, falls ein solches nicht existiert
     *
     * Die Methode liefert auch null, falls das gegebenen TreeItem
     * gar nicht zu diesem Baum gehoert.
     */

    //============================================================
    public TreeItem getBigger( TreeItem item )
    //============================================================
    {
        return leftIsSmaller() ? getNext( item ) : getPrev( item );
    } // getBigger


    /**
     * @return  TreeItem mit dem naechst kleineren Schluessel als
     *          der des TreeItems item
     *          bzw. null, falls ein solches nicht existiert
     *
     * Die Methode liefert auch null, falls das gegebenen TreeItem
     * gar nicht zu diesem Baum gehoert.
     */

    //============================================================
    public TreeItem getSmaller( TreeItem item )
    //============================================================
    {
        return leftIsSmaller() ? getPrev( item ) : getNext( item );
    } // getBigger


    /**
     * @return  TreeItem mit dem naechst groesserem Schluessel als
     *          der des TreeItems item
     *          bzw. TreeItem mit dem kleinsten Schluessel, falls eines
     *          mit groesserem Schluessel nicht existiert
     *
     * Die Methode liefert null, falls das gegebenen TreeItem
     * gar nicht zu diesem Baum gehoert.
     */

    //============================================================
    public TreeItem getBiggerCyclic( TreeItem item )
    //============================================================
    {
        return leftIsSmaller() ?
               getNextCyclic( item ) :
               getPrevCyclic( item );
    } // getBiggerCyclic


    /**
     * @return  TreeItem mit dem naechst kleineren Schluessel als
     *          der des TreeItems item
     *          bzw. TreeItem mit dem groessten Schluessel, falls eines
     *          mit kleinerem Schluessel nicht existiert
     *
     * Die Methode liefert null, falls das gegebenen TreeItem
     * gar nicht zu diesem Baum gehoert.
     */

    //============================================================
    public TreeItem getSmallerCyclic( TreeItem item )
    //============================================================
    {
        return leftIsSmaller() ?
               getPrevCyclic( item ) :
               getNextCyclic( item );
    } // getSmallerCyclic





    //************************************************************
    // public methods
    //
    // Ueberschreiben der ererbten add-Methoden
    // ( @see BinarySearchTree )
    //************************************************************


    /**
     * Einfuegen des Objekts key an die passende Stelle im Baum.
     * Dabei wird ein LinkedSimpleTreeItem erzeugt.
     *
     * @return  erzeugtes und eingefuegtes TreeItem
     */

    //============================================================
    public TreeItem add( Object key )
    //============================================================
    {
        return add_item( new LinkedSimpleTreeItem( 2, key ) );
    } // add


    /**
     * Einfuegen eines ( key, value )-Paares an die passende Stelle
     * im Baum.
     * Dabei wird ein LinkedStdTreeItem erzeugt.
     *
     * @return  erzeugtes und eingefuegtes TreeItem
     */

    //============================================================
    public TreeItem add( Object key,
                         Object value )
    //============================================================
    {
        return add_item( new LinkedStdTreeItem( 2, key, value ) );
    } // add


    /**
     * Einfuegen eines TreeItem's an die passende Stelle im Baum.
     *
     * Das einzufuegende TreeItem muss das Interface LinkedTreeItem
     * implementieren.
     *
     * @return  eingefuegtes TreeItem
     */

    //============================================================
    public TreeItem add( TreeItem item )
    //============================================================
    {
        if( ! ( item instanceof LinkedTreeItem ) )
            return null;

        return super.add( item );
    } // add





    //************************************************************
    // public methods
    //
    // Ueberschreiben der ererbten Methoden afterAdd() und
    // beforeRemove()
    // ( @see BinarySearchTree )
    //************************************************************


    /**
     * Wird aufgerufen nachdem ein TreeItem dem Baum zugefuegt
     * wurde.
     * Diese Methode verkettet das neu zugefuegte TreeItem mit
     * seinen Nachbarn und aktualisiert ggf. die Verweise auf
     * erstes und letztes TreeItem der Kette.
     */

    //============================================================
    public void afterAdd( TreeItem item )
    //============================================================
    {
        if( item == null )
            return;

        LinkedTreeItem item_linked = ( LinkedTreeItem ) item;

        if(    item_linked.getPrev() != null
            || item_linked.getNext() != null )
            return;  // Eingefuegtes item ist schon mit Nachbarn
                     // verkettet.


        // Vorgaenger und Nachfolger im Baum ermitteln
        // ( LEFT_ROOT_RIGHT_ORDER )
        //
        TreeItem item_prev = prev( item );
        TreeItem item_next = next( item );


        // Verweise im zugefuegten TreeItem entsprechend setzen
        //
        item_linked.setPrev( item_prev );
        item_linked.setNext( item_next );



        if( item_prev != null )     // Es gibt einen Vorgaenger
        {
            // Nachfolger-Verweis des Vorgaengers setzen
            ( ( LinkedTreeItem ) item_prev ).setNext( item );
        }
        else                        // Es gibt keinen Vorgaenger
        {
            _item_first = item;
        } // else


        if( item_next != null )     // Es gibt einen Nachfolger
        {
            // Vorgaenger-Verweis des Nachfolgers setzen
            ( ( LinkedTreeItem ) item_next ).setPrev( item );
        }
        else                        // Es gibt keinen Nachfolger
        {
            _item_last = item;
        } // else

        super.afterAdd( item ); // Vielleicht hat die Superklassse
                                // noch was zu tun
    } // afterAdd



    /**
     * Wird aufgerufen bevor ein TreeItem aus dem Baum entfernt wird.
     *
     * Diese Methode loest die Verkettung des zu entfernenden TreeItem's
     * mit seinen Nachbarn und aktualisiert ggf. die Verweise auf
     * erstes und letztes TreeItem der Kette.
     */

    //============================================================
    public void beforeRemove( TreeItem item )
    //============================================================
    {
        super.beforeRemove( item ); // Vielleicht hat die Superklasse
                                    // erst noch was zu tun

        if( item == null )
            return;

        LinkedTreeItem item_linked = ( LinkedTreeItem ) item;


        // Nachbarn des zu entfernenden TreeItems ermitteln
        //
        TreeItem item_prev = item_linked.getPrev();
        TreeItem item_next = item_linked.getNext();


        // Verweise im zu entfernenden TreeItem loeschen
        //
        item_linked.setPrev( null );
        item_linked.setNext( null );


        if( item_prev != null )     // Es gibt einen Vorgaenger
        {
            // Nachfolger-Verweis des Vorgaengers aktualisieren
            ( ( LinkedTreeItem ) item_prev ).setNext( item_next );
        }
        else                        // Es gibt keinen Vorgaenger
        {
            _item_first = item_next;
        } // else


        if( item_next != null )     // Es gibt einen Nachfolger
        {
            // Vorgaenger-Verweis des Nachfolgers aktualisieren
            ( ( LinkedTreeItem ) item_next ).setPrev( item_prev );
        }
        else                        // Es gibt keinen Nachfolger
        {
            _item_last = item_prev;
        } // else

    } // beforeRemove


} // ExtendedRedBlackTree
