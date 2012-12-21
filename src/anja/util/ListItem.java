package anja.util;

/**<p>
* Das Interface ListItem wird hauptsaechlich von doppelt verketteten Listen, Stacks
* und Queues benutzt.
*
* @author Thomas Wolf
* @version 1.0
*/
public interface ListItem extends Cloneable,KeyValueHolder {
 
 // Konstanten fuer requestAccess beim Owner
 // =======================================
 
 /** Der Besitzer wird neu gesetzt. */
 public final static int SET_OWNER=1;
 /** Object aus der Kette entfernen. */
 public final static int REMOVE=2;
 /** Verbindungen loeschen. */
 public final static int CLEAR_CONNECTIONS=3;
 /** Objekt halb nach vorne verknuepfen. */
 public final static int CONNECT_FORWARD=4;
 /** Objekt halb zurueck verknuepfen. */
 public final static int CONNECT_BACKWARD=5;
 
 // Cloneable
 // =========
 
 /**
 * Klont das Objekt. Dabei sollte folgendes gelten: Das neue ListItem ist 
 * <strong>nicht</strong> verknuepft und hat <strong>keinen</strong> Besitzer.
 * Die angehaengten Schluessel und Objekte werden ebenfalls geklont.
 * @return Kopie des ListItems
 */
 public Object clone();
 /**
 * Legt eine Kopie der im Objekt enthaltenen referenzierten Datenobjekte an.
 * Schluesselobjekte werden nicht geklont und Verbindungen sowie Besitzer 
 * bleiben unveraendert.
 */
 public void cloneData();
 
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
 public boolean setOwningList(Owner owner);
 /**
 * Liefert den Besitzer des ListItems.
 * @return der Besitzer
 */
 public Owner getOwningList();
 
 // Verbindungen
 // ============
 
 /**
 * Liefert das naechsten verknuepfte Objekt.
 * @return naechstes Objekt
 */
 public ListItem next();
 /**
 * Liefert das vorige verknuepfte Objekt.
 * @return voriges Objekt
 */
 public ListItem prev();
 /**
 * Sucht in der Kette das erste Objekt und liefert es zurueck.
 * @return erstes Objekt der Kette
 */
 public ListItem findFirst();
 /**
 * Sucht in der Kette das letzte Objekt und liefert es zurueck.
 * @return letztes Objekt der Kette
 */
 public ListItem findLast();
 /*
 * Test, ob das Objekt erstes einer Kette ist.
 * return true, falls das Objekt das erste der Kette ist
 */
 public boolean isFirst();
 /*
 * Test, ob das Objekt letztes einer Kette ist.
 * return true, falls das Objekt das letzte der Kette ist
 */
 public boolean isLast();
 /**
 * Sucht das Objekt O in der Kette.
 * @param O zu suchendes Objekt
 * @return gefundes ListItem-Objekt der Kette oder null, falls nicht gefunden.
 */
 public ListItem find(Object O);
 /**
 * Berechnet den Index innerhalb der Kette vom Anfang oder vom Ende an.
 * @param fromStart falls true hat das erste Element hat Index=0 
 * ansonsten das letzte
 */
 public int getIndex(boolean fromStart);
 
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
 public boolean answerConnect(ListItem link,boolean forward);
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
 public boolean connect(ListItem link,boolean forward);
 /**
 * Entfernt die Verknuepfungen mit anderen Objekten. perv() wird
 * direkt mit next() verbunden.
 * @return true, falls Entfernen gestattet und erfolgreich
 */
 public boolean remove();
 /**
 * Loescht alle Verbindungen vom ListItem aus. Die Verbindungen werden also nicht
 * korrekt entfernt. Es wird im Gegensatz zu remove nur eine Anfrage an den Besitzer
 * geben! Der Besitzer bleibt erhalten.
 * @return true, falls erfolgreich
 */
 public boolean clearConnections();
}
