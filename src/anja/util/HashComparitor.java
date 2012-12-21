package anja.util;

/**<p>
* Der einfachste und schnellste Comparitor. 
* Vergleicht einfach die Hashcodes der Objekte miteinander.
*
* @version 1.1
* @author Thomas Wolf
*/
public class HashComparitor implements Comparitor {
 /**
 * Ueberschreibt Comparitor.compare.
 * @param o1 und
 * @param o2 zu vergleichende Objekte
 * @return Vergleichskonstanten
 * @see Comparitor#compare
 */
 public short compare(Object o1,Object o2) {
  // Bei KeyValueHolder-Objekten werden die Schluesselobjekte 
  // stattdessen verglichen
  if (o1 instanceof KeyValueHolder) o1=((KeyValueHolder)o1).key();
  if (o2 instanceof KeyValueHolder) o2=((KeyValueHolder)o2).key();
  // Hash-Code-Abfragen...
  int h1=0,h2=0;
  if (o1!=null) h1=o1.hashCode();
  if (o2!=null) h2=o2.hashCode();
  if (h1<h2) return SMALLER;
  if (h1>h2) return BIGGER;
  return EQUAL;
 }
}
