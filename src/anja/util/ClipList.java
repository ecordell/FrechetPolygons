package anja.util;

//import java.util.Enumeration;
//import java.util.NoSuchElementException;

/**<p>
* Eine Liste, die Clipping-Funktionen unterstuetzt. Damit kann man die Liste zeitweise
* auf bestimmte Intervalle beschraenken.
*
* @see List
* @see ListItem
* @see SimpleListItem
*
* @author Thomas Wolf
* @version 1.0
*/
public class ClipList extends List {
	/** linker Clipping-Bereich. */
	private ListItem _leftclip=null;
	/** rechter Clipping-Bereich. */
	private ListItem _rightclip=null;

 // Konstruktion
 // ============
 
 /**
 * Leerer Konstruktor.
 */
 public ClipList() { super(); }
 /**
 * Konvertier-Konstruktor. Uebernimmt alle Elemente der Struktur L in diese Liste.
 * @param L zu konvertierende Liste
 */
 public ClipList(BasicList L) { super(L); }
 /**
 * Kopier-Konstruktor. Kopiert eine Teilliste in die Liste. Ist die Kette Teil einer
 * Struktur, so sollte i.a. eine Kopie eingefuegt werden und die Quellstruktur
 * nicht veraendert werden.
 * @param start Startelement
 * @param end Endelement
 */
 public ClipList(ListItem start,ListItem end) { super(start,end); }
 /**
 * Kopier-Konstruktor. Kopiert eine Teilliste in die Liste. Ist die Kette Teil einer
 * Struktur, so sollte i.a. eine Kopie eingefuegt werden und die Quellstruktur
 * nicht veraendert werden.
 * @param start Startelement
 * @param count Laenge der Kette
 */
 public ClipList(ListItem start,int count) { super(start,count); }
 /**
 * Teilarray-Konstruktor. Konstruiert eine Liste aus den uebergebenen Teil-Array.
 */
 public ClipList(Object[] array,int startindex,int length) { super(array,startindex,length); }

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
 	ClipList L=new ClipList();
 	ListItem i=super.getCopy(null,null,-1,false),j=i;
 	while (j!=null) {
 		j.cloneData();
 		j=j.next();
 	}
 	L.add(null,i,null,-1);
 	L._leftclip=cloneChain(_leftclip,null,-1);
 	L._rightclip=cloneChain(_rightclip,null,-1);
 	return L;
 }
 
 // Methoden fuer Clipping
 // =====================
 
 /**
 * Beschraenkt die Liste auf den Bereich von start bis end. Ist start i oder
 * end ==null oder nicht in der Liste enthalten, wird ein ListException 
 * ausgeloest.
 * @param start Beginn des Clipping-Bereiches
 * @param end Ende des Clipping-Bereiches
 */
 public void clip(ListItem start,ListItem end) {
 	if ((!contains(start)) || (!contains(end)))
 		throw new ListException(ListException.ILLEGAL_ACCESS);
 	ListItem i=null,j=null;
 	if (start.prev()!=null) {
 		i=first(); j=start.prev();
 		cut(i,j,-1);
 		if (_leftclip!=null) {
 			if (!_leftclip.findLast().connect(i,true))
 				throw new ListException(ListException.CONNECT);
 		} else _leftclip=i;
 	}
 	if (end.next()!=null) {
 		i=end.next(); j=last();
 		cut(i,last(),-1);
 		if (_rightclip!=null) {
 			if (!j.connect(_rightclip,true))
 				throw new ListException(ListException.CONNECT);
 		}
			_rightclip=i;
 	}
 }
 /**
 * Entfernt die mit clip gesetzten Beschraenkungen der Liste und stellt den
 * urspruenglichen Zustand wieder her. Bei mehreren clip-Aufrufen wird der
 * Zustand vor dem ersten wiederhergestellt.
 */
 public void removeClip() {
 	if (_leftclip!=null) add(null,_leftclip,null,-1);
 	if (_rightclip!=null) add(last(),_rightclip,null,-1);
 	_leftclip=_rightclip=null;
 }
	
}
