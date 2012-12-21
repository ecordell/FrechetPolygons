package anja.util;


/**
* Implementation einer <code>InfiniteEnumeration</code> durch Verwendung einer
* linearen Queue.
*
* @version 0.7 18.11.03
* @author Sascha Ternes
*/

public final class InfiniteQueueEnumeration
implements InfiniteEnumeration
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // die Queue:
   private SimpleList _queue;

   // Flag fuer eine leere Queue:
   private boolean _is_empty;

   // aktuelle Position der Aufzaehlung:
   private ListItem _next;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt eine neue Aufzaehlung, die nur das Element <code>null</code>
   * enthaelt; falls im weiteren Verlauf weitere Elemente durch die Methode
   * <code>add</code> hinzugefuegt werden, wird dieses erste Element entfernt.
   */
   public InfiniteQueueEnumeration() {

      _queue = new SimpleList();
      _queue.add( (Object) null );
      _is_empty = true;
      _next = _queue.first();

   } // InfiniteQueueEnumeration


   /**
   * Erzeugt eine neue Aufzaehlung aus den uebergebenen Elementen unter
   * Beibehaltung der Reihenfolge.
   *
   * @param elements die Elemente dieser Aufzaehlung
   */
   public InfiniteQueueEnumeration(
      Object[] elements
   ) {

      _queue = new SimpleList( elements, 0, elements.length );
      _is_empty = false;
      _next = _queue.first();

   } // InfiniteQueueEnumeration


   /**
   * Erzeugt eine neue Aufzaehlung aus den uebergebenen Elementen unter
   * Beibehaltung der Reihenfolge.
   *
   * @param elements die Elemente dieser Aufzaehlung
   */
   public InfiniteQueueEnumeration(
      SimpleList elements
   ) {

      _queue = new SimpleList( elements );
      _is_empty = false;
      _next = _queue.first();

   } // InfiniteQueueEnumeration


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Fuegt das spezifizierte Element der Queue hinzu.
   *
   * @param element das hinzuzufuegende Element
   */
   public void add(
      Object element
   ) {

      if ( _is_empty ) {
         _is_empty = false;
         _queue.clear();
         _queue.add( element );
         _next = _queue.first();
      } else { // if
         _queue.add( element );
      } // else

   } // add


   // *************************************************************************
   // Interface InfiniteEnumeration
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus InfiniteEnumeration kopiert]
   */
   public boolean hasMoreElements() {

      return true;

   } // hasMoreElements


   /*
   * [javadoc-Beschreibung wird aus InfiniteEnumeration kopiert]
   */
   public Object nextElement() {

      Object element = _next.value();
      _next = _queue.cyclicRelative( _next, 1 );
      return element;

   } // nextElement


} // InfiniteQueueEnumeration
