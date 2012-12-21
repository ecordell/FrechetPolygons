package anja.util;

import java.util.Enumeration;
//import java.util.NoSuchElementException;

/**<p>
* Ein einfacher Stack basierend auf BasicList.
* @see BasicList
*
* @author Thomas Wolf
* @version 1.0
*/
public class Stack extends BasicList {

 // Konstruktion
 // ============
 
 /**
 * Leerer Konstruktor.
 */
 public Stack() {
  super();
 }

 // Allgemeine Methoden
 // ===================

 /**
 * Ueberschreibt Object.toString().
 * @see java.lang.Object#toString
 */
 public String toString() {
  if (super.length()>0)
   return getClass().getName()+"[length="+super.length()+",{top="+getListString(",",0,false,true)+"=bottom}]";
  else
   return  getClass().getName()+"[length="+super.length()+"{}]";
 }
 
 // Zugriff
 // =======

 /**
 * Liefert die Anzahl der im Stack gespeicherten Elemente.
 * @return Antzahl der Elemente
 */
 public int length() {
  return super.length();
 }
 /**
 * Liefert true, falls der Stack leer ist.
 * @return true, falls der Stack leer ist
 */
 public boolean empty() {
  return super.empty();
 }
 /**
 * Liefert true, falls das Objekt object im Stack enthalten ist.
 * @param object zu suchendes Objekt
 * @return true, falls item in der Liste enthalten ist.
 */
 public boolean contains(Object object) {
  return (super.find(object,super.first(),null,-1,false)!=null);
 }
 /**
 * Liefert das unterste (erste) Element des Stacks. Falls der
 * Stack leer ist, wird ein ListException ausgeloest.
 * @return unterstes Element
 */
 public Object bottom() {
  if (super.length()>0) return first().key();
  else throw new ListException(ListException.EMPTY_LIST);
 }
 /**
 * Liefert das oberste (letzte) Element des Stacks. Falls der
 * Stack leer ist, wird ein ListException ausgeloest.
 * @return oberstes Element
 */
 public Object top() {
  if (super.length()>0) return last().key();
  else throw new ListException(ListException.EMPTY_LIST);
 }
 /**
 * Wie top().
 * @return oberstes Element
 * @see #top
 */
 public Object peek() { return top(); }
 
 // Lesen und Schreiben von Elementen
 // =================================
 
 /**
 * Erzeugt ein neues ListItem und speichert O in ihm.
 * @param O zu speicherndes Objekt
 * @return neues ListItem
 */
 private ListItem createNew(Object O) {
  return new SimpleListItem(O);
 }
 /**
 * Legt ein Objekt auf dem Stack ab.
 * @param object zu speicherndes Objekt
 */
 public void push(Object object) {
  if (super.add(last(),createNew(object),null,1)!=1)
   throw new ListException(ListException.INSERTION_ERROR);
 }
 /**
 * Entnimmt das oberste (letzte) Element aus dem Stack und liefert es
 * zurueck.
 * @return oberstes Element
 */
 public Object pop() {
  if (super.length()<=0) throw new ListException(ListException.EMPTY_LIST);
  ListItem i=last();
  super.remove(i,null,1);
  return i.key();
 }
 
 // Zugriff auf den gesamten Stack
 // ==============================
 
 /**
 * Loescht den gesamten Stack.
 */
 public void clear() {
  super.clear();
 }
 /**
 * Konvertiert den Stack in ein Array. Das unterste Element ist das 
 * erste des Arrays und das oberste das letzte.
 * @return Objekt-Array
 */
 public Object[] convertToArray() {
  if (super.length()<=0) return new Object[0];
  int len=super.length();
  Object[] items=new Object[len];
  storeInArray(items,0,len,first(),KEY,false);
  return items;
 }
 /**
 * Liefert ein Enumerator-Objekt, das alle Elemente des Stacks aufzaehlt.
 * Das erste Element ist das unterste und das letzte das oberste.
 * @return Enumeration mit Stack-Elements
 */
 public Enumeration elements() {
  return enumerate(null,null,-1,KEY);
 }
 
 // Abkuerzungen fuer Basistypen
 // ==========================
 
 /**
 * Push fuer Java-Basistypen.
 * @param i zu pushender Int-Wert
 * @see #push
 */
 public void push(int i) {
  push(new Integer(i));
 }
 /**
 * Push fuer Java-Basistypen.
 * @param l zu pushender Long-Wert
 * @see #push
 */
 public void push(long l) {
  push(new Long(l));
 }
 /**
 * Push fuer Java-Basistypen.
 * @param f zu pushender Float-Wert
 * @see #push
 */
 public void push(float f) {
  push(new Float(f));
 }
 /**
 * Push fuer Java-Basistypen.
 * @param d zu pushender Double-Wert
 * @see #push
 */
 public void push(double d) {
  push(new Double(d));
 }
 /**
 * Push fuer Java-Basistypen.
 * @param b zu pushender Boolean-Wert
 * @see #push
 */
 public void push(boolean b) {
  push(new Boolean(b));
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das letzte Objekt als Integer-Wert
 * @see #pop
 */
 public int popInt() {
  if (top() instanceof Number) return ((Number)pop()).intValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das letzte Objekt als Long-Wert
 * @see #pop
 */
 public long popLong() {
  if (top() instanceof Number) return ((Number)pop()).longValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das letzte Objekt als Float-Wert
 * @see #pop
 */
 public float popFloat() {
  if (top() instanceof Number) return ((Number)pop()).floatValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das letzte Objekt als Double-Wert
 * @see #pop
 */
 public double popDouble() {
  if (top() instanceof Number) return ((Number)pop()).doubleValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das letzte Objekt als Boolean-Wert
 * @see #pop
 */
 public boolean popBoolean() {
  if (top() instanceof Boolean) return ((Boolean)pop()).booleanValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 
}
