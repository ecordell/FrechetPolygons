package anja.util;

import java.io.*;

/**<p>
* Einfachstes ListItem, das nur Verknuepfungen und einen Besitzer, jedoch
* kein Objekt oder Schluessel speichert.
*
* @author Thomas Wolf
* @version 1.0
*/
public class BasicListItem implements ListItem, Serializable {
 
 /** Die Liste, die das Objekt besitzt. */
 private Owner _owningList=null;
 /** Zeiger auf voriges und naechstes ListItem. */
 private ListItem _prev=null,_next=null;
 
 // Allgemeine Routinen
 // ===================
 
 /**
 * Vergleicht zwei ListItems. Zwei ListItems sind genau dann gleich,
 * wenn ihre Schluessel gleich (im Sinne von equals) sind.
 * equals liefert genau dann true, falls object auf dieses Objekt verweist,
 * object.equals(key()) true liefert oder object ein ListItem ist und 
 * gleich mit diesem ListItem ist.
 * Ueberschreibt Object.equals().
 * @param object zu vergleichendes Objekt
 * @return true, falls gleich
 */
 public boolean equals(Object object) {
  if (object==null) return false;
  if (this==object) return true;
  if (key()==object) return true;
  if (object.equals(key())) return true;
  if (object instanceof ListItem) {
   ListItem v=(ListItem)object;
   if (key()==v.key()) return true;
   if ((key()==null) || (v.key()==null)) return false;
   return (key().equals(v.key()));
  }
  return false;
 }
 /**
 * Ueberschreibt Object.toString().
 */
 public String toString() {
  return getClass().getName()+"[]";
 }

 /**
 * Klont das Objekt. Dabei sollte folgendes gelten: Das neue ListItem ist 
 * <strong>nicht</strong> verknuepft und hat <strong>keinen</strong> Besitzer.
 * Die angehaengten Schluessel und Objekte werden ebenfalls geklont.
 * @return Kopie des ListItems
 */
 public Object clone() {
  return new BasicListItem();
 }
 /**
 * Legt eine Kopie der im Objekt enthaltenen referenzierten Datenobjekte an.
 * Schluesselobjekte werden nicht geklont und Verbindungen sowie Besitzer 
 * bleiben unveraendert.
 */
 public void cloneData() {
 }
 
 // Angehoerigkeit
 // =============
 
 /**
 * Legt den Besitzer fest, dem das ListItem angehoert. Ist bereits ein
 * Besitzer vorhanden, so wird er um Zustimmung gebeten. Ausserdem wird
 * der neue Besitzer um Zustimmung gebeten. Erst wenn <strong>beide</strong>
 * zugestimmt haben wird die Aenderung vollzogen.
 * Ist der alte und der neue Besitzer ein und dasselbe Objekt, so wird
 * niemand um Zustimmung gebeten und true zurueckgeliefert.
 * Die Verknuepfungen bleiben von der Aenderung unberuehrt.
 * @param owner neuer Besitzer
 * @return true, falls Aenderung erfolgreich
 */
 public synchronized boolean setOwningList(Owner owner) {
  if (owner==_owningList) return true;
  if (!requestListAccess(SET_OWNER,owner)) return false;
  Owner oldown=_owningList; _owningList=owner;
  if (!requestListAccess(SET_OWNER,owner)) {
   _owningList=oldown; return false;
  }
  return true;
 }
 /**
 * Liefert den Besitzer des ListItems.
 * @return der Besitzer
 */
 public Owner getOwningList() {
  return _owningList;
 }
 /**
 * Bittet den zugehoerigen Owner um Genehmigung einer 
 * Aktion vom Typ accesstype mit dem Argument argument.
 * @param accesstype Typ der Aktion (des Zugriffs)
 * @param argument Argument der Aktion
 * @return true, falls der Owner die Aktion erlaubt, sonst false
 */
 protected boolean requestListAccess(int accesstype,Object argument) {
  if (_owningList==null) return true;
  return _owningList.requestAccess(accesstype,this,argument);
 }
 
 // Verbindungen
 // ============
 
 /**
 * Liefert das naechsten verknuepfte Objekt.
 * @return naechstes Objekt
 */
 public ListItem next() { return _next; }
 /**
 * Liefert das vorige verknuepfte Objekt.
 * @return voriges Objekt
 */
 public ListItem prev() { return _prev; }
 /**
 * Sucht in der Kette das erste Objekt und liefert es zurueck.
 * @return erstes Objekt der Kette
 */
 public ListItem findFirst() {
  if (_prev==null) return this;
  ListItem i=_prev;
  while ((i.prev()!=null) && (i.prev()!=this)) i=i.prev();
  return i;
 }
 /**
 * Sucht in der Kette das letzte Objekt und liefert es zurueck.
 * @return letztes Objekt der Kette
 */
 public ListItem findLast() {
  if (_next==null) return this;
  ListItem i=_next;
  while ((i.next()!=null) && (i.next()!=this)) i=i.next();
  return i;
 }
 /*
 * Test, ob das Objekt erstes einer Kette ist.
 * return true, falls das Objekt das erste der Kette ist
 */
 public boolean isFirst() { return (_prev==null); }
 /*
 * Test, ob das Objekt letztes einer Kette ist.
 * return true, falls das Objekt das letzte der Kette ist
 */
 public boolean isLast() { return (_next==null); }
 /**
 * Sucht das Objekt O in der Kette.
 * @param O zu suchendes Objekt
 * @return gefundes ListItem-Objekt der Kette oder null, falls nicht gefunden.
 */
 public ListItem find(Object O) {
  if (this.equals(O)) return this;
  ListItem i=_next;
  while ((i!=null) && (!i.equals(O))) i=i.next();
  if (i!=null) return i;
  i=_prev;
  while ((i!=null) && (!i.equals(O))) i=i.prev();
  return i;
 }
 /**
 * Berechnet den Index innerhalb der Kette vom Anfang oder vom Ende an.
 * @param fromStart falls true hat das erste Element hat Index=0 
 * ansonsten das letzte
 */
 public int getIndex(boolean fromStart) {
  int j=-1;
  ListItem i=this;
  while (i!=null) {
   j++;
   if (fromStart) i=i.prev(); else i=i.next();
  }
  return j;
 }
 
 /**
 * Komplettiert die Verknuepfung und sollte daher nur von connect aus aufgerufen
 * werden. Es darf hier der Besitzer nicht nocheinmal um Erlaubnis gefragt
 * werden (alle notwendigen Tests fuehrt connect bereits durch), jedoch muss
 * answerConnect feststellen, dass der Aufruf nur von connect stammen kann.
 * Dies geht beispielsweise, indem getestet wird, ob die 'halbe Verknuepfung'
 * schon vollzogen wurde.
 * @param link ListItem, mit dem verknuepft werden soll
 * @param forward Richtung der Verknuepfung (falls true, nach vorne)
 * @return true, falls Verknuepfung erfolgreich
 */
 public synchronized boolean answerConnect(ListItem link,boolean forward) {
  if ((link==null) || (link.getOwningList()!=getOwningList())) return false;
  if (forward) {
   if (link.next()!=this) return false;
   _prev=link;
  } else {
   if (link.prev()!=this) return false;
   _next=link;
  }
  return true;
 }
 /**
 * Verknuepft das Objekt mit dem Objekt link. Die Verknuepfung erfolgt 'nach vorne',
 * falls forward==true ist, ansonsten 'nach hinten'. Um die doppelte Verkettung
 * herzustellen, wird answerConnect des ListItems link aufgerufen (falls link!=null).
 * Vor dem Verknuepfen wird der Besitzer um Erlaubnis gefragt. Verknuepfen ist nur
 * moeglich, falls beide ListItems den gleichen Besitzer haben.
 * @param link ListItem, mit dem verknuepft werden soll
 * @param forward Richtung der Verknuepfung (falls true, nach vorne)
 * @return true, falls Verknuepfung gestattet und erfolgreich
 */
 public synchronized boolean connect(ListItem link,boolean forward) {
  if ((link!=null) && (link.getOwningList()!=getOwningList())) return false;
  ListItem i;
  if (forward) {
   if (!requestListAccess(CONNECT_FORWARD,link)) return false;
   i=_next; _next=link;
   if ((link!=null) && (!link.answerConnect(this,true))) {
    _next=i;
    return false;
   }
  } else {
   if (!requestListAccess(CONNECT_BACKWARD,link)) return false;
   i=_prev; _prev=link;
   if ((link!=null) && (!link.answerConnect(this,false))) {
    _prev=i;
    return false;
   }
  }
  return true;
 }
 /**
 * Entfernt die Verknuepfungen mit anderen Objekten. prev() wird
 * direkt mit next() verbunden.
 * @return true, falls Entfernen gestattet und erfolgreich
 */
 public synchronized boolean remove() {
  if (!requestListAccess(REMOVE,null)) return false;
  if (_prev!=null) _prev.connect(_next,true);
  if (_next!=null) _next.connect(_prev,false);
  _prev=_next=null;
  _owningList=null;
  return true;
 }
 /**
 * Loescht alle Verbindungen vom ListItem aus. Die Verbindungen werden also nicht
 * korrekt entfernt. Es wird im Gegensatz zu remove nur eine Anfrage an den Besitzer
 * geben! Der Besitzer bleibt erhalten.
 * @return true, falls erfolgreich
 */
 public synchronized boolean clearConnections() {
  if (!requestListAccess(CLEAR_CONNECTIONS,null)) return false;
  _prev=_next=null;
  return true;
 }
 
 // Eigentum
 // ========
 
 /**
 * Liefert den gespeicherten Schluessel des Objektes.
 * @return gespeicherter Schluessel
 */
 public Object key() {
  return null;
 }
 /**
 * Setzt den zugeordneten Schluessel.
 * @param key neuer Schluessel
 */
 public boolean setKey(Object key) {
  return false;
 }
 /**
 * Liefert den gespeicherten Wert zurueck.
 * @return gespeichertes Objekt
 */
 public Object value() {
  return null;
 }
 /**
 * Speichert das Objekt object.
 * @param object zu speicherndes Objekt
 */
 public boolean setValue(Object object) {
  return false;
 }

 
}
