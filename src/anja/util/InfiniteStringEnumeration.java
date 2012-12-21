package anja.util;


/**
* Implementation einer <code>InfiniteEnumeration</code> fuer Zeichenketten.
*
* @version 0.7 18.11.03
* @author Sascha Ternes
*/

public final class InfiniteStringEnumeration
implements InfiniteEnumeration
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // das Praefix der Aufzaehlungsstrings:
   private String _prefix;

   // aktueller Suffixzahl der Aufzaehlungsstrings:
   private int _suffix;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt eine neue Aufzaehlung fuer Zeichenketten mit dem Format
   * <i>suffix</i>, wobei <i>suffix</i> eine fortlaufende Zahl mit dem
   * Anfangswert <code>1</code> ist. Die resultierende Aufzaehlung ist also
   * {<code>1</code>, <code>2</code>, <code>3</code>, ...}.
   */
   public InfiniteStringEnumeration() {

      this( "", 1 );

   } // InfiniteStringEnumeration


   /**
   * Erzeugt eine neue Aufzaehlung fuer Zeichenketten mit dem Format
   * <i>prefix_suffix</i>, wobei <i>_</i> ein leerer String und <i>suffix</i>
   * eine fortlaufende Zahl mit dem Anfangswert <code>1</code> ist. Die
   * resultierende Aufzaehlung ist also {<code>prefix1</code>,
   * <code>prefix2</code>, <code>prefix3</code>, ...}.
   *
   * @param prefix das Praefix der Aufzaehlungszeichenketten
   */
   public InfiniteStringEnumeration(
      String prefix
   ) {

      this( prefix, 1 );

   } // InfiniteStringEnumeration


   /**
   * Erzeugt eine neue Aufzaehlung fuer Zeichenketten mit dem Format
   * <i>prefix_suffix</i>, wobei <i>_</i> ein leerer String und <i>suffix</i>
   * eine fortlaufende Zahl mit dem spezifizierten Anfangswert ist. Die
   * resultierende Aufzaehlung ist also {<code>prefix665</code>,
   * <code>prefix666</code>, <code>prefix667</code>, ...}, wenn <i>suffix</i>
   * beispielsweise den Wert <code>665</code> hat.
   *
   * @param prefix das Praefix der Aufzaehlungszeichenketten
   * @param suffix der Anfangswert des Suffixes
   */
   public InfiniteStringEnumeration(
      String prefix,
      int suffix
   ) {

      _prefix = prefix;
      _suffix = suffix;

   } // InfiniteStringEnumeration


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

      String element = _prefix + _suffix;
      _suffix++;
      return element;

   } // nextElement


} // InfiniteStringEnumeration
