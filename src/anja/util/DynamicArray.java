package anja.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
* Ein dynamisch erweiterbares Array fuer beliebige Objekte. Es koennen beliebig
* viele Objekte hinzugefuegt werden, und das Array bietet wahlfreien Lese- und
* Schreibzugriff auf bereits bestehende Indizes. Wenn der Platz im Array zur
* Neige geht, wird dessen Kapazitaet verdoppelt.<br>
*
* @version 0.9 18.03.2004
* @author Sascha Ternes
*/

public final class DynamicArray
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // die Menge der Objekte:
   private Object[] _list;
   // der aktuelle Index fuer die Erweiterung:
   private int _free;


   // *************************************************************************
   // Inner classes
   // *************************************************************************

   private class DynamicArrayEnumeration implements Enumeration {

      private DynamicArray _a;
      private int _next;

      DynamicArrayEnumeration( DynamicArray a ) {
         _a = a;
         _next = 0;
      } // DynamicArrayEnumeration

      public boolean hasMoreElements() {
         if ( _next < _a.size() ) return true;
         else return false;
      } // hasMoreElements

      public Object nextElement() {
         try {
            return _a.get( _next++ );
         } catch ( IndexOutOfBoundsException ioobe ) { // try
            throw new NoSuchElementException();
         } // catch
      } // nextElement

   } // DynamicArrayEnumeration


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /*
   * Dummy-Konstruktor zur Verwendung in convert(...)
   */
   private DynamicArray( boolean dummy ) {}


   /**
   * Erzeugt ein leeres Array mit einer Anfangskapazitaet von 1.
   */
   public DynamicArray() {

      this( 1 );

   } // DynamicArray


   /**
   * Erzeugt ein leeres Array mit der spezifizierten Anfangskapazitaet.
   *
   * @param capacity die Anfangskapazitaet des Arrays
   */
   public DynamicArray(
      int capacity
   ) {

      _list = new Object[capacity];
      _free = 0;

   } // DynamicArray


   /**
   * Erzeugt ein Array mit einer Kapazitaet von 1, dem das spezifizierte Objekt
   * hinzugefuegt wird.
   *
   * @param o das erste Objekt im Array
   */
   public DynamicArray(
      Object o
   ) {

      _list = new Object[1];
      _list[0] = o;
      _free = 1;

   } // DynamicArray


   /**
   * Integriert das spezifizierte Array in einem neuen dynamischen Array. Die
   * Anfangskapazitaet ist gleich der Laenge des Arrays. Der Arrayinhalt wird
   * <b>nicht</b> kopiert, sondern referenziert, daher darf das spezifizierte
   * Array selbst <b>nicht</b> mehr benutzt werden!
   *
   * @param o ein beliebiges Array; ist es <code>null</code> oder seine Laenge
   *        gleich Null, wird ein leeres Array mit Anfangskapazitaet 1 erzeugt
   */
   public DynamicArray(
      Object[] o
   ) {
      
      if ( ( o == null ) || ( o.length == 0 ) ) {
         _list = new Object[1];
         _free = 0;
      } else { // if
         _list = o;
         _free = o.length;
      } // else

   } // DynamicArray


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Haengt das spezifizierte Objekt an das Ende des Arrays an.
   *
   * @param o das hinzuzufuegende Objekt
   */
   public void add(
      Object o
   ) {

      // Liste ggf. verlaengern:
      if ( _free >= _list.length ) {
         Object[] new_list = new Object[_list.length * 2];
         for ( int i = 0; i < _list.length; i++ )
            new_list[i] = _list[i];
         _list = new_list;
      } // if

      _list[_free++] = o;

   } // add


   /**
   * Entfernt das Objekt am gegebenen Index aus dem Array. Die nachfolgenden
   * Objekte werden um eine Position nach vorne verschoben, somit wird die
   * Laenge des Arrays um eins geringer, bei gleichbleibender Kapazitaet. Daraus
   * resultiert eine lineare Laufzeit, die umso groesser ist, je kleiner der
   * Index ist!<br>
   * Falls der Index ausserhalb der aktuellen Arraygrenzen liegt, wird eine
   * <code>IndexOutOfBoundsException</code> ausgeworfen.
   *
   * @param i der Index des zu entfernenden Objekts
   * @return das entfernte Objekt
   * @exception IndexOutOfBoundsException falls der Index <code>i</code>
   *            kleiner Null oder groesser gleich der aktuellen Laenge des Arrays
   *            ist
   */
   public Object remove(
      int i
   ) {

      if ( ( i < 0 ) || ( i >= _free ) ) throw new IndexOutOfBoundsException();
      Object o = _list[i];
      for ( int k = i + 1; k < _free; k++ )
         _list[k - 1] = _list[k];
      _free--;
      return o;

   } // remove


   /**
   * Liefert die aktuelle Kapazitaet dieses Arrays.
   *
   * @return die aktuelle Kapazitaet
   */
   public int capacity() {

      return _list.length;

   } // capacity


   /**
   * Liefert eine geordnete Aufzaehlung aller Elemente, die in diesem Array
   * enthalten sind. Wenn waehrend der Aufzaehlung neue Elemente hinzugefuegt
   * werden, gelangen auch diese in die Aufzaehlung.
   *
   * @return eine Aufzaehlung aller enthaltenen Elemente
   */
   public Enumeration elements() {

      return new DynamicArrayEnumeration( this );

   } // elements


   /**
   * Liefert das erste Element des Arrays. Falls das Array leer ist, wird eine
   * <code>NoSuchElementException</code> ausgeworfen.
   *
   * @return das Element an Index 0
   * @exception NoSuchElementException falls das Array leer ist
   */
   public Object first() {

      if ( _free == 0 ) throw new NoSuchElementException();
      return _list[0];

   } // first


   /**
   * Liefert das letzte Element des Arrays. Falls das Array leer ist, wird eine
   * <code>NoSuchElementException</code> ausgeworfen.
   *
   * @return das Element an Index <code>length() - 1</code>
   * @exception NoSuchElementException falls das Array leer ist
   */
   public Object last() {

      if ( _free == 0 ) throw new NoSuchElementException();
      return _list[_free - 1];

   } // last


   /**
   * Liefert die aktuelle Laenge des Arrays.
   *
   * @return die aktuelle Laenge
   */
   public int length() {

      return size();

   } // length


   /**
   * Liefert das Objekt mit dem gegebenen Index. Falls der Index ausserhalb der
   * aktuellen Arraygrenzen liegt, wird eine
   * <code>IndexOutOfBoundsException</code> ausgeworfen.
   *
   * @param i der Index des zu lesenden Objekts
   * @return das Objekt am Index <code>i</code> des Arrays
   * @exception IndexOutOfBoundsException falls der Index <code>i</code>
   *            kleiner Null oder groesser gleich der aktuellen Laenge des Arrays
   *            ist
   */
   public Object get(
      int i
   ) {

      if ( ( i < 0 ) || ( i >= _free ) ) throw new IndexOutOfBoundsException();
      return _list[i];

   } // get


   /**
   * Testet, ob das Array leer ist.
   *
   * @return <code>true</code>, wenn das Array keine Elemente enthaelt, sonst
   *         <code>false</code>
   */
   public boolean isEmpty() {

      return _free == 0;

   } // isEmpty


   /**
   * Schreibt das gegebene Objekt in das Arrayelement mit dem spezifizierten
   * Index. Falls der Index ausserhalb der aktuellen Arraygrenzen liegt, wird
   * eine <code>IndexOutOfBoundsException</code> ausgeworfen.
   *
   * @param i der Index des zu schreibenden Arrayelements
   * @param o das neue Objekt des zu schreibenden Arrayelements
   * @exception IndexOutOfBoundsException falls der Index <code>i</code>
   *            kleiner Null oder groesser gleich der aktuellen Laenge des Arrays
   *            ist
   */
   public void set(
      int i,
      Object o
   ) {

      if ( ( i < 0 ) || ( i >= _free ) ) throw new IndexOutOfBoundsException();
      _list[i] = o;

   } // set


   /**
   * Liefert die aktuelle Laenge des Arrays.
   *
   * @return die aktuelle Laenge
   */
   public int size() {

      return _free;

   } // size


   /**
   * Liefert eine textuelle Repraesentation
   *
   * @return einen String, der dieses Array repraesentiert
   */
   public String toString() {

      String s = "{";
      for ( int i = 0; i < _free; i++ ) {
         s += _list[i];
         if ( i < _free - 1 ) s += ", ";
      } // for
      s += "}";
      return s;

   } // toString


} // DynamicArray
