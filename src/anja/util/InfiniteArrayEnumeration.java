package anja.util;


/**
* Implementation einer <code>InfiniteEnumeration</code> durch Verwendung eines
* Arrays.
*
* @version 0.7 18.11.03
* @author Sascha Ternes
*/

public final class InfiniteArrayEnumeration
implements InfiniteEnumeration
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // das Array:
   private Object[] _array;

   // aktuelle Position fuer add:
   private int _free;

   // aktuelle Position der Aufzaehlung:
   private int _next;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /*
   * Verbotener Konstruktor.
   */
   private InfiniteArrayEnumeration() {}


   /**
   * Erzeugt eine neue Aufzaehlung mit einem Array der spezifizierten Laenge.
   * Saemtliche Elemente erhalten den Wert <code>null</code>. Solange keine
   * Elemente durch die Methode <code>add</code> hinzugefuegt werden, liefert
   * diese Aufzaehlung nur <code>null</code>-Elemente. Falls jedoch mindestens
   * ein Element hinzugefuegt wurde, liefert die Aufzaehlung nur Elemente aus
   * der Menge der hinzugefuegten Elemente und keine dieser
   * <code>null<code>-Elemente mehr.
   *
   * @param n die Laenge des Arrays dieser Aufzaehlung
   */
   public InfiniteArrayEnumeration(
      int n
   ) {

      _array = new Object[n];
      _free = 0;
      _next = 0;

   } // InfiniteArrayEnumeration


   /**
   * Erzeugt eine neue Aufzaehlung aus den uebergebenen Elementen.
   *
   * @param elements die Elemente dieser Aufzaehlung
   */
   public InfiniteArrayEnumeration(
      Object[] elements
   ) {

      _array = elements;
      _free = _array.length;
      _next = 0;

   } // InfiniteArrayEnumeration


   /**
   * Erzeugt eine neue Aufzaehlung aus den uebergebenen Elementen.
   *
   * @param elements die Elemente dieser Aufzaehlung
   */
   public InfiniteArrayEnumeration(
      SimpleList elements
   ) {

      this( elements.convertValuesToArray() );

   } // InfiniteArrayEnumeration


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Fuegt das spezifizierte Element dem Array hinzu.<br>
   * Falls im Array kein Element mehr frei ist, wird eine
   * <code>ArrayIndexOutOfBoundsException</code> ausgeworfen.
   *
   * @param element das hinzuzufuegende Element
   * @exception ArrayIndexOutOfBoundsException falls das Array voll ist
   */
   public void add(
      Object element
   ) {

      _array[_free] = element;
      _free++;

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

      Object element = _array[_next++];
      if ( _next >= _free ) _next = 0;
      return element;

   } // nextElement


} // InfiniteArrayEnumeration
