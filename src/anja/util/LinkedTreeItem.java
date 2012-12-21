package anja.util;

/**
 * Das Interface LinkedTreeItem muss von allen TreeItem's
 * implementiert werden, die einem ExtendedRedBlackTree zugefuegt
 * werden sollen
 * ( @see ExtendedRedBlackTree ).
 *
 * Die TreeItem's eines ExtendedRedBlackTree's koennen nur richtig
 * miteinander verkettet werden, wenn das LinkedTreeItem-Interface
 * korrekt ( !!! ) implementiert wurde.
 * ( @see LinkedSimpleTreeItem und LinkedStdTreeItem )
 *
 * @version 0.1 15.11.02
 * @author      Ulrich Handel
 */

//****************************************************************
public interface LinkedTreeItem
//****************************************************************
{
    /**
     * @return  Das durch setNext(..) gesetzte TreeItem
     *          bzw. null, falls nicht gesetzt
     */

    //============================================================
    public abstract TreeItem getNext();
    //============================================================


    /**
     * @return  Das durch setPrev(..) gesetzte TreeItem
     *          bzw. null, falls nicht gesetzt
     */

    //============================================================
    public abstract TreeItem getPrev();
    //============================================================


    /**
     * Setzten des Rueckgabe-TreeItem's fuer die Methode getNext().
     *
     * @param item  Rueckgabe-TreeItem fuer getNext().
     *              ( null ist erlaubt )
     */

    //============================================================
    public abstract void setNext( TreeItem item );
    //============================================================


    /**
     * Setzten des Rueckgabe-TreeItem's fuer die Methode getPrev().
     *
     * @param item  Rueckgabe-TreeItem fuer getPrev().
     *              ( null ist erlaubt )
     */

    //============================================================
    public abstract void setPrev( TreeItem item );
    //============================================================

} // LinkedTreeItem
