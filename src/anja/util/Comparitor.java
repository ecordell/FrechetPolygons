package anja.util;

/**<p>
* Das Comparitor-Interface sollte von Objekten implementiert werden,
* die als Vergleicher beim Sortieren mittels des Sorter-Objektes u.ae. 
* dienen sollen. Kann kein Vergleich der Objekte o1 und o2 erfolgen, 
* so sollte ein CompareException ausgeloest werden.
* @see CompareException
*
* @version	1.1 5 Apr 1997
* @author		Thomas Wolf
*/
public interface Comparitor {
 /** Rueckgabewert von compare: o1 soll in der Ordnung vor o2 liegen. */
 public final static short SMALLER=-1;
 /** Rueckgabewert von compare: o1 ist in der Ordnung identisch mit o2. */
 public final static short EQUAL=0;
 /** Rueckgabewert von compare: o1 soll in der Ordnung nach o2 liegen. */
 public final static short BIGGER=1;
 /**
 * Vergleicht die beiden Objekte o1 und o2 bezueglich einer Ordnung.
 * @param o1 und
 * @param o2 Objekte, die verglichen werden sollen.
 * @return eine der Vergleichskonstanten dieses Interfaces.
 */
 public short compare(Object o1,Object o2);
}
