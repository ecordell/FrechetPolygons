package anja.util;

/**<p>
* CompareException wird immer dann ausgeloest, falls es einen Fehler beim 
* Vergleichen von zwei Objekten gegeben hat. CompareException wird im 
* allgemeinen nicht abgefangen und muss nicht in einer throws-Klausel erwaehnt
* werden.
*/
public class CompareException extends RuntimeException {
 /** Die Objekte, bei deren Vergleich der Fehler auftrat. */
 public Object o1,o2;
 /** Das Comparitor-Objekt, das den Vergleich durchfuehren sollte. */
 public Comparitor comparitor;
 
 /**
 * Konstruktor.
 */
 public CompareException(Comparitor c,Object o1,Object o2) {
  super("Error comparing "+o1.toString()+" to "+o2.toString()+" by "+c.toString()+".");
  comparitor=c; this.o1=o1; this.o2=o2;
 }
 public CompareException(String s) { super(s); }
 public CompareException() { super(); }
}
