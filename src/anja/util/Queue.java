package anja.util;

import java.util.Enumeration;
//import java.util.NoSuchElementException;

/**
* Eine einfache Schlange basierend auf BasicList.
* @see BasicList
* @see Stack
*
* @author Thomas Wolf
* @version 1.0
*/
public class Queue extends BasicList {

 // Konstruktion
 // ============
 
 /**
 * Leerer Konstruktor.
 */
 public Queue() {
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
   return getClass().getName()+"[length="+super.length()+",{first="+getListString(",",0,false,false)+"=last}]";
  else
   return  getClass().getName()+"[length="+super.length()+"{}]";
 }
 
 // Zugriff
 // =======

 /**
 * Liefert die Anzahl der in der Schlange gespeicherten Elemente.
 * @return Antzahl der Elemente
 */
 public int length() {
  return super.length();
 }
 /**
 * Liefert true, falls die Schlange leer ist.
 * @return true, falls die Schlange leer ist
 */
 public boolean empty() {
  return super.empty();
 }
 /**
 * Liefert true, falls das Objekt object in der Schlange enthalten ist.
 * @param object zu suchendes Objekt
 * @return true, falls item in der Liste enthalten ist.
 */
 public boolean contains(Object object) {
  return (super.find(object,super.first(),null,-1,false)!=null);
 }
 /**
 * Liefert das erste Element der Schlange. Falls die
 * Schlange leer ist, wird ein ListException ausgeloest.
 * @return erstes Element
 */
 public Object firstValue() {
  if (super.length()>0) return first().key();
  else throw new ListException(ListException.EMPTY_LIST);
 }
 /**
 * Liefert das letzte Element der Schlange. Falls die
 * Schlange leer ist, wird ein ListException ausgeloest.
 * @return letztes Element
 */
 public Object lastValue() {
  if (super.length()>0) return last().key();
  else throw new ListException(ListException.EMPTY_LIST);
 }
 /**
 * Wie firstValue().
 * @return erstes Element
 * @see #peek
 */
 public Object peek() { return firstValue(); }
 
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
 * Fuegt ein Objekt an die Schlange an
 * @param object zu speicherndes Objekt
 */
 public void push(Object object) {
  if (super.add(last(),createNew(object),null,1)!=1)
   throw new ListException(ListException.INSERTION_ERROR);
 }
 /**
 * Entnimmt das erste Element aus der Schlange und liefert es
 * zurueck.
 * @return erstes Element
 */
 public Object pop() {
  if (super.length()<=0) throw new ListException(ListException.EMPTY_LIST);
  ListItem i=first();
  super.remove(i,null,1);
  return i.key();
 }
 
 // Zugriff auf den gesamten Stack
 // ==============================
 
 /**
 * Loescht die gesamte Schlange.
 */
 public void clear() {
  super.clear();
 }
 /**
 * Konvertiert die Schlange in ein Array.
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
 * Liefert ein Enumerator-Objekt, das alle Elemente der Schlange aufzaehlt.
 * @return Enumeration mit Schlangen-Elements
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
 * @return das erste Objekt als Integer-Wert
 * @see #pop
 */
 public int popInt() {
  if (peek() instanceof Number) return ((Number)pop()).intValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das erste Objekt als Long-Wert
 * @see #pop
 */
 public long popLong() {
  if (peek() instanceof Number) return ((Number)pop()).longValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das erste Objekt als Float-Wert
 * @see #pop
 */
 public float popFloat() {
  if (peek() instanceof Number) return ((Number)pop()).floatValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das erste Objekt als Double-Wert
 * @see #pop
 */
 public double popDouble() {
  if (peek() instanceof Number) return ((Number)pop()).doubleValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das erste Objekt als Boolean-Wert
 * @see #pop
 */
 public boolean popBoolean() {
  if (peek() instanceof Boolean) return ((Boolean)pop()).booleanValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 
}
