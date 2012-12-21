package anja.util;

import java.util.Comparator;


/**
* Hilfsklasse fuer die alphabetische Sortierung von Objekten unter Benutzung
* ihrer <code>toString()</code>-Methode.
*
* @version 0.1 23.06.2004
* @author Sascha Ternes
*/

public final class AlphabeticalComparator
implements Comparator
{

   // *************************************************************************
   // Interface Comparator
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus Comparator kopiert]
   */
   public int compare(
      Object o1,
      Object o2
   ) {

      return o1.toString().compareTo( o2.toString() );

   } // compare


   /*
   * [javadoc-Beschreibung wird aus Comparator kopiert]
   */
   public boolean equals(
      Object obj
   ) {

      return obj instanceof AlphabeticalComparator;

   } // equals


} // AlphabeticalComparator
