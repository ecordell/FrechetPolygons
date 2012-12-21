package anja.util;

import java.util.Arrays;
import java.util.Enumeration;


/**
* Hilfsklasse fuer die alphabetisch sortierte Aufzaehlung von Objekten.
*
* @version 0.1 23.06.2004
* @author Sascha Ternes
*/

public final class AlphabeticalEnumeration
implements Enumeration
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // die Elemente der Aufzaehlung:
   private Object[] _elements;

   // Index des naechsten Elements:
   private int _next;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt eine leere Aufzaehlung.
   */
   public AlphabeticalEnumeration() {

      _elements = new Object[0];

   } // AlphabeticalEnumeration


   /**
   * Erzeugt eine alphabetisch sortierte Aufzaehlung der spezifizierten
   * Objekte durch Benutzung der Methode <code>toString()</code> jedes Objekts.
   *
   * @param elements ein Array mit den aufzuzaehlenden Objekten
   */
   public AlphabeticalEnumeration(
      Object[] elements
   ) {

      _elements = elements;
      Arrays.sort( _elements, new AlphabeticalComparator() );

   } // AlphabeticalEnumeration


   /**
   * Erzeugt eine alphabetisch sortierte Aufzaehlung der spezifizierten
   * Objekte durch Benutzung des spezifizierten Comparators.
   *
   * @param elements ein Array mit den aufzuzaehlenden Objekten
   * @param comparator der Comparator zum Vergleichen der Objekte
   */
   public AlphabeticalEnumeration(
      Object[] elements,
      AlphabeticalComparator comparator
   ) {

      _elements = elements;
      Arrays.sort( _elements, comparator );

   } // AlphabeticalEnumeration


   // *************************************************************************
   // Interface Enumeration
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus Enumeration kopiert]
   */
   public boolean hasMoreElements() {

      return _next < _elements.length;

   } // hasMoreElements


   /*
   * [javadoc-Beschreibung wird aus Enumeration kopiert]
   */
   public Object nextElement() {

      return _elements[_next++];

   } // nextElement


} // AlphabeticalEnumeration
