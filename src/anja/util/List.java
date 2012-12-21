package anja.util;

//import java.util.Enumeration;
//import java.util.NoSuchElementException;

/**<p>
* Eine Liste basierend auf SimpleList.
*
* @see SimpleList
* @see ListItem
* @see StdListItem
*
* @author Thomas Wolf
* @version 1.0
*/
public class List extends SimpleList {
	
 // Konstruktion
 // ============
 
 /**
 * Leerer Konstruktor.
 */
 public List() { super(); }
 /**
 * Konvertier-Konstruktor. Uebernimmt alle Elemente der Struktur L in diese Liste.
 * @param L zu konvertierende Liste
 */
 public List(BasicList L) { super(L); }
 /**
 * Kopier-Konstruktor. Kopiert eine Teilliste in die Liste. Ist die Kette Teil einer
 * Struktur, so sollte i.a. eine Kopie eingefuegt werden und die Quellstruktur
 * nicht veraendert werden.
 * @param start Startelement
 * @param end Endelement
 */
 public List(ListItem start,ListItem end) { super(start,end); }
 /**
 * Kopier-Konstruktor. Kopiert eine Teilliste in die Liste. Ist die Kette Teil einer
 * Struktur, so sollte i.a. eine Kopie eingefuegt werden und die Quellstruktur
 * nicht veraendert werden.
 * @param start Startelement
 * @param count Laenge der Kette
 */
 public List(ListItem start,int count) { super(start,count); }
 /**
 * Teilarray-Konstruktor. Konstruiert eine Liste aus den uebergebenen Teil-Array.
 */
 public List(Object[] array,int startindex,int length) { super(array,startindex,length); }

 // Allgemeine Methoden
 // ===================

 /**
 * Ueberschreibt Object.toString().
 * @see java.lang.Object#toString
 */
 public String toString() {
  return super.toString();
 }
 /**
 * Klont die gesamte Liste. Das heisst, alle ListItems werden geklont, sowie die
 * in ihnen enthaltenen Objektwerte (falls sie Cloneable implementieren), aber
 * nicht die gespeicherten Schluessel.
 * @return geklonte Liste
 */
 public Object clone() {
 	List L=new List();
 	ListItem i=super.getCopy(null,null,-1,false),j=i;
 	while (j!=null) {
 		j.cloneData();
 		j=j.next();
 	}
 	L.add(null,i,null,-1);
 	return L;
 }

 // Einfuegen von Elementen
 // ======================
 
 /**
 * Erzeugt ein neues ListItem und speichert das key-value-Paar in ihm.
 * @param key zu speichernder Schluessel
 * @param value zu speichernder Wert
 * @return neues ListItem
 */
 private ListItem createNew(Object key,Object value) {
  return new StdListItem(key,value);
 }
 /**
 * Fuegt das Objekt object an das Ende der Liste an. Zurueckgegeben wird das ListItem,
 * das object enthaelt.
 * @param key einzufuegender Schluessel
 * @param value einzufuegender Wert
 * @return ListItem, das key und value enthaelt
 * @see SimpleList#add(ListItem)
 */
 public ListItem add(Object key,Object value) {
  return add(createNew(key,value));
 }
 /**
 * Erzeugt ein neues ListItem, das das key-value-Paar enthaelt und fuegt 
 * es vor base in die Liste ein. Ist base==null, so wird an das Ende 
 * der Liste eingefuegt. Gehoert base nicht zur Liste, so wird ein 
 * ListException ausgeloest.
 * @param base Position in der Liste, vor der eingefuegt wird
 * @param key einzufuegender Schluessel
 * @param value einzufuegender Wert
 * @return eingefuegtes ListItem
 * @see SimpleList#insert(ListItem, ListItem)
 */
 public ListItem insert(ListItem base,Object key,Object value) {
  return insert(base,createNew(key,value));
 }

}
