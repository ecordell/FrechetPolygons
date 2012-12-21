package anja.util;

import java.io.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;

// Diese Klasse sollte eigentlich weiter hinten als Innerclass stehen...
 /**
 * Enumeration-Objekt zum Aufzaehlen von Listen-Elementen mit enumerate.
 */
 class ListEnumerator implements Enumeration {
  /** Cursor-ListItem. */
  private ListItem _i=null,_e=null;
  /** Maximale Anzahl zu durchlaufender ListItems. */
  private int _count=-1;
  /** Falls true, werden Values zurueckgegeben, ansonsten Keys. */
  private byte _objecttype;
  /** privater Konstruktor. */
  ListEnumerator(ListItem start,ListItem end,int count,byte objecttype) {
   _i=start; _e=end; if (_e!=null) _e=_e.next();
   _count=count; _objecttype=objecttype;
   if (_count==0) _i=null;
  }
  /** Liefert true, falls noch ein Element vorhanden. */
  public boolean hasMoreElements() {
   return (_i!=null);
  }
  /** Liefert das naechste Element in der Struktur. */
  public Object nextElement() {
  	if (_i==null) throw new NoSuchElementException();
   Object O=null;
   switch (_objecttype) {
   	case BasicList.LISTITEM:
   		O=_i;
   		break;
   	case BasicList.KEY:
   		O=_i.key();
   		break;
   	case BasicList.VALUE:
   		O=_i.value();
   		break;
   }
   _i=_i.next(); _count--;
   if ((_i==_e) || (_count==0)) _i=null;
   return O;
  }
 }

/**
* Rudimentaere und effiziente doppelt verkette Liste. Ist nur dazu gedacht,
* fuer weitere Listen als Basis zu dienen.
* Alle Methoden von BasicList sind protected, also zwar von abgeleiteten Klassen
* (und von Klassen desselben Packages) zugreifbar, aber nicht von 'aussen'!
* Der Zugriff der ListItems wird sehr restriktiv gehandhabt: Alle Aenderungen
* der ListItems bei Verknuepfungen und Besitzer werden unterbunden. Damit in einer
* abgeleiteten Klasse ListItems modifiziert werden koennen, sollte zuerst 
* allowAccess(ANY_ACCESS) aufgerufen werden und nach der Aenderung wieder
* allowAccess(NO_ACCESS).
* Diese Klasse ist nur dazu gedacht, als Basis fuer weitere Listenklassen zu dienen,
* deshalb sind alle Methoden auch protected und benoetigen meist viele Parameter.
* Oftmals wird ein Listenintervall uebergeben, das man durch ein Anfangselement und
* entweder einer Laenge oder ein Endelement spezifizieren kann. Soll es durch eine
* Laengenangabe geschehen, dann sollte man den Endwert auf null setzen, umgekehrt 
* setzt man die Laenge auf einen negativen Wert um das Intervall durch ein Endelement
* zu begrenzen. Beispiel: add(base,start,end,-1) fuegt das Intevall von start bis end 
* an das Element base der Liste an, dagegen fuegt add(base,start,null,3) 3 Elemente,
* die an start haengen nach base ein.
*
*
* @author Thomas Wolf
* @version 1.0
*/
public class BasicList implements Owner, Serializable {
 
 // Zugriffskonstanten fuer allowAccess
 /** Erlaubt keinerlei Zugriff (Standard). 
 * Keinen Zugriff bezieht sich hier ersteinmal auf Zugriffe der Verkettung. */
 public final static byte NO_ACCESS=0;
 /** Erlaube beliebigen Zugriff. */
 public final static byte ANY_ACCESS=1;
 
 // Konstanten fuer Objektzugriff
 /** Zugriff auf das ListItem-Objekt. */
 protected final static byte LISTITEM=0;
 /** Zugriff auf den Schluessel. */
 protected final static byte KEY=1;
 /** Zugriff auf den Wert. */
 protected final static byte VALUE=2;
 
 /** Erstes Element der Liste. */
 private ListItem _first=null;
 /** Letztes Element der Liste. */
 private ListItem _last=null;
 /** Anzahl der gespeicherten Elemente. */
 private int _length=0;
 /** Variable um den Zugriff von ListItems temporaer zu gestatten. */
 private byte _access=NO_ACCESS;
 
 // Konstruktion
 // ============
 
 /**
 * Geschuetzter Konstruktor.
 */
 protected BasicList() {
  _first=_last=null; _length=0;
  _access=NO_ACCESS;
 }
 
 // Allgemeine Methoden
 // ===================

 /**
 * Liefert die String-Repraesentation aller in der Liste enthaltenen Elemente 
 * getrennt durch einen separator-String und Zeilenumbruch nach etwa width 
 * Zeichen falls width>0 ist. Ist width<0, wird nach abs(width) ListItems ein
 * Zeilenumbruch eingefuegt, ist width==0 wird nie ein Zeilenumbruch eingefuegt.
 * @param separator Trenn-String zwischen den ListItems
 * @param width Breite einer Spalte oder -Anzahl der ListItems pro Zeile oder kein
 * @param item2string falls true, wird ListItem.toString benutzt, ansonsten werden
 * @param reverse falls true, wird Liste umgekehrt durchlaufen
 * direkt die Schluessel und Objekte in Strings umgewandelt
 * Zeilenumbruch
 */
 protected String getListString(String separator,int width,boolean item2string,boolean reverse) {
  StringBuffer s=new StringBuffer();
  ListItem i=_first;
  if (reverse) i=_last;
  int j=0,c=0;
  while (i!=null) {
   if (item2string) s.append(i.toString());
   else {
    if (i.value()!=i.key()) s.append("("+i.key()+","+i.value()+")");
    else s.append(i.key());
   }
   if ((!reverse) && (i.next()!=null)) s.append(separator);
   if ((reverse) && (i.prev()!=null)) s.append(separator);
   if (((width>0) && (s.length()-j>=width)) || ((width<0) && (c>=width))) {
    s.append("\n"); j=s.length(); c=0;
   }
   if (reverse) i=i.prev(); else i=i.next();
   c++;
  }
  return s.toString();
 }
 /**
 * Ueberschreibt Object.toString().
 * @see java.lang.Object#toString
 */
 public String toString() {
  return getClass().getName()+"[length="+_length+",{"+getListString(",",0,false,false)+"}]";
 }
 /**
 * Vergleicht zwei Listen miteinander. Zwei Listen sind genau dann gleich, wenn
 * sie die gleiche Anzahl von Elementen haben und das i-te Element der einen
 * Liste gleich dem i-ten Element der anderen Liste ist (bzgl. equals).
 * @param O Objekt, mit dem verglichen werden soll
 * @return true, falls Objekte gleich
 * @see java.lang.Object#equals
 */
 public boolean equals(Object O) {
  if (O==this) return true;
  if (!(O instanceof BasicList)) return false;
  BasicList L=(BasicList)O;
  if (_length!=L._length) return false;
  boolean eq=true;
  ListItem i=_first,j=L._first;
  while ((i!=null) && (eq)) {
   eq=i.equals(j);
   i=i.next(); j=j.next();
  }
  return eq;
 }
 
 // Zugriff
 // =======

 /**
 * Liefert die Anzahl der in der Liste gespeicherten Elemente.
 * @return Anzahl der Elemente
 */
 protected int length() {
  return _length;
 }
 /**
 * Liefert true, falls die Liste leer ist.
 * @return true, falls Liste leer ist
 */
 protected boolean empty() {
  return _length<=0;
 }
 /**
 * Liefert das erste ListItem der Liste.
 * @return erstes ListItem in der Liste
 */
 protected ListItem first() {
  return _first;
 }
 /**
 * Liefert das letzte ListItem der Liste.
 * @return letztes ListItem in der Liste
 */
 protected ListItem last() {
  return _last;
 }
 /**
 * Liefert true, falls das ListItem item in der Liste enthalten ist.
 * Der Test erfolgt in konstanter Zeit.
 * @param item zu testendes ListItem
 * @return true, falls item in der Liste enthalten ist.
 */
 protected boolean contains(ListItem item) {
  return ((item!=null) && (item.getOwningList()==this));
 }
 /**
 * Liefert das ListItem in der Liste, das von base count Schritte entfernt ist.
 * Ist cyclic true, dann wird die Liste zyklisch betrachtet. Negative Werte
 * count bestimmen Schritte zurueck, positive vorwaerts.
 * Wird beim Zugriff auf die Liste ueber den Rand hinaus gegangen (bei cyclic=false)
 * oder ist base in der Liste nicht enthalten, wird null zurueckgegeben.
 * Ist base==null, so wird vom Anfang der Liste aus gegangen, falls count>=0, falls
 * count<0, wird vom Ende aus nach vorne gegangen.
 * @param base ListItem, von dem ausgegangen wird.
 */
 protected ListItem getListItem(ListItem base,int count,boolean cyclic) {
  if (base==null) {
   if (count>=0) base=_first; else base=_last;
  } else if (!contains(base)) return null;
  ListItem i=base;
  while ((i!=null) && (count!=0)) {
   if (count<0) {
    count++;
    if ((cyclic) && (i.prev()==null)) i=_last; else i=i.prev();
   } else {
    count--;
    if ((cyclic) && (i.next()==null)) i=_first; else i=i.next();
   }
  }
  return i;
 }
 /**
 * Berechnet den Index innerhalb der Liste vom Anfang oder vom Ende an.
 * @param i ListItem, dessen Index bestimmt werden soll
 * @param fromStart falls true hat das erste Element hat Index=0 
 * ansonsten das letzte
 */ 
 protected int getIndex(ListItem i,boolean fromStart) {
  if (!contains(i)) return -1;
  return i.getIndex(fromStart);
 }
 /**
 * Liefert den Abstand der beiden ListItems i und j in der Liste.
 * Ein positiver Rueckgabewert bedeutet, dass j von i aus durch 
 * vorwaertslaufen zu erreichen ist, negative Werte bedeuten, dass j von
 * i aus durch rueckwaertslaufen erreichbar ist.
 * Ist i oder j nicht in der Liste oder sonst nicht erreichbar (?!)
 * so wir Integer.MAX_VALUE zurueckgegeben.
 * @param i ListItem1 und
 * @param j ListItem2, deren Abstand bestimmt werden soll.
 */
 protected int getDistance(ListItem i,ListItem j) {
  if ((!contains(i)) || (!contains(j))) return Integer.MAX_VALUE;
  int d=0;
  ListItem k=i;
  boolean neg=false;
  while ((k!=j) || (d>_length)) {
   d++; k=k.next();
   if (k==null) { k=_first; neg=true; }
  }
  if (d>_length) return Integer.MAX_VALUE;
  if (neg) d-=_length;
  return d;
 }
 /**
 * Liefert das ListItem aus dem Intervall start-end mit der maximalen
 * Laenge count (falls count>0), bei dem equals(O) true liefert. Ist
 * end==null so wird end auf das Listenende gesetzt. Ist cyclic==true,
 * wird die Liste als zyklische Liste betrachtet.
 * @param O gesuchtes Objekt
 * @param start erstes ListItem des Intervalles
 * @param end letztes ListItem des Intervalles (falls !=null)
 * @param count maximale Anzahl der ListItems im Intervall, falls >0
 * @param cyclic falls true, wird die Liste als zyklische Liste betrachtet
 * @return das gefundene ListItem oder null
 */
 protected ListItem find(Object O,ListItem start,ListItem end,int count,boolean cyclic) {
  if (start==null) start=_first;
  if (!contains(start)) return null;
  if (end!=null) end=end.next();
  if ((cyclic) && (count<0) && (end==null)) cyclic=false;
  ListItem i=start;
  boolean found=false;
  while ((i!=null) && (i!=end) && (count!=0) && (!(found=i.equals(O)))) {
   i=i.next(); count--;
   if ((i==null) && (cyclic)) i=_first;
  }
  if (found) return i; else return null;
 }
 /**
 * Sucht mit Hilfe des Comparitors comparitor in einem Bereich der Liste das
 * ListItem, welches 'am dichtesten' an key liegt. Ist compare==Comparitor.EQUAL,
 * wird nur ein ListItem zurueckgeliefert, bei dessen Vergleich Comparitor.EQUAL
 * zurueckgegeben wurde. Bei Comparitor.BIGGER wird das kleinste ListItem unter
 * denen zurueckgegeben, die groesser waren, bei Comparitor SMALLER wird das groesste
 * unter den kleineren zurueckgegeben. Wird als Comparitor null uebergeben, wird
 * ein Standard-Comparitor verwendet. start, end, count und cyclic beschreiben 
 * das ListenIntervall. Ist key ein ListItem, so werden ListItems miteinander
 * verglichen, ansonsten Schluesselobjekte.
 * Ist key==null, so wird bei compare==Comparitor.BIGGER das Maximum aller 
 * Werte gesucht, bei compare==Comparitor.SMALLER das Minimum (Comparitor.EQUAL 
 * liefert hier keine sinnvollen Ergebnisse).
 * @param key Vergleichsobjekt
 * @param comparitor Comparitor-Objekt
 * @param compare Art des Vergleiches: Falls Comparitor.EQUAL, werden gleiche Objekte
 * gesucht, bei Comparitor.BIGGER und Comparitor.SMALLER wird das kleinste groessere
 * bzw. groesste kleinere ListItem gesucht
 * @param start erstes ListItem des Intervalles
 * @param end letztes ListItem des Intervalles (falls !=null)
 * @param count maximale Anzahl der ListItems im Intervall, falls >0
 * @param cyclic falls true, wird die Liste als zyklische Liste betrachtet
 * @return das gefundene ListItem oder null
 */
 protected ListItem findClosest(Object key,Comparitor comparitor,short compare,
                                ListItem start,ListItem end,int count,boolean cyclic) {
  if (start==null) start=_first;
  if (!contains(start)) return null;
  if (end!=null) end=end.next();
  if ((cyclic) && (count<0) && (end==null)) cyclic=false;
  if (comparitor==null) comparitor=new StdComparitor();
  ListItem i=start,closest=null;
  boolean cont=true;
  short cerg;
  while ((i!=null) && (i!=end) && (count!=0) && (cont)) {
  	if (key==null) {
  		if (closest==null) closest=i;
  		else if (comparitor.compare(i,closest)==compare) closest=i;
  	} else {
				cerg=comparitor.compare(i,key);
  	 if (cerg==Comparitor.EQUAL) {
  	 	closest=i; cont=false;
  	 } else	if (cerg==compare) {
  	 	if (closest==null)	closest=i;
  	 	else if (comparitor.compare(closest,i)==compare) closest=i;
  	 }
  	}
   i=i.next(); count--;
   if ((i==null) && (cyclic)) i=_first;
  }
 	return closest;
 }

 
 // Zugriff auf Listenintervalle
 // ============================
 
 /**
 * Liefert einen Ausschnitt der Liste als Array. Ist cyclic==false,
 * so wird das Intervall zwischen start und end zurueckgegeben.
 * Ist cyclic==true, so wird die Liste als zyklische Liste betrachtet
 * und das erste Element des Arrays ist immer start.
 * Das ganze funktioniert nur, falls start und end beide aus der Liste
 * und einander erreichbar sind, sonst wird null zurueckgegeben.
 * Ist start==null, wird start auf first() gesetzt, ist end==null, so
 * werden count Objekte kopiert oder (falls count<0) die Elemente bis
 * zum Ende der Liste.
 * @param start erstes ListItem des Intervalles
 * @param end letztes ListItem des Intervalles (falls !=null)
 * @param count maximale Anzahl der ListItems im Intervall, falls >0
 * @param cyclic falls true, wird die Liste als zyklische Liste betrachtet
 * @return Feld mit den entsprechenden ListItems
 */
 protected ListItem[] getListItemArray(ListItem start,ListItem end,int count,boolean cyclic) {
  if (start==null) start=_first;
  if ((count<0) && (end==null)) end=_last;
  if ((!contains(start)) || ((end!=null) && (!contains(end)))) return null;
  ListItem[] items;
  if (((start==_first) && (end==_last)) || ((end==_first) && (start==_last))) {
   items=new ListItem[_length];
   storeInArray(items,0,_length,_first,LISTITEM,true);
   return items;
  }
  if (count>=0) {
   items=new ListItem[count];
   storeInArray(items,0,count,start,LISTITEM,cyclic);
  } else {
   count=getDistance(start,end);
   if (count<0) {
    if (cyclic) count=_length+count;
    else { ListItem i=end; end=start; start=i; count=-count; }
   }
   count++;
   items=new ListItem[count];
   storeInArray(items,0,count,start,LISTITEM,cyclic);
  }

  return items;
 }
 /**
 * Speichert einen Teil der Liste in ein Array. storeInArray funktioniert auch mit
 * Ketten, die nicht zu Liste gehoeren oder nicht mit dem Rest verknuepft sind. Allerdings
 * sollte dann cyclic nicht gesetzt sein.
 * Ist start==null, so wird start=first() gesetzt.
 * @param array Feld, in dem Daten gespeichert werden sollen
 * @param startindex StartIndex des Array zum Speichern
 * @param length Anzahl der zu speichernden Elemente
 * @param objecttype gibt an was gespeichert werden soll: <ul>
 * <li>objecttype==LISTITEM die ListItems selbst sollen gespeichert werden
 * <li>objecttype==KEY die key()-Werte der ListItems werden gespeichert
 * <li>objecttype==VALUE die value()-Werte der ListItems werden gespeichert</ul>
 * @param cyclic die Liste wird als zyklische Liste betrachtet
 * @return tatsaechliche Anzahl der gespeicherten Elemente
 */
 protected int storeInArray(Object[] array,int startindex,int length,ListItem start,byte objecttype,boolean cyclic) {
  if (array==null) return 0;
  if (start==null) start=_first; 
  int i=startindex;
  while ((start!=null) && (length>0)) {
   switch (objecttype) {
    case LISTITEM:
     array[i]=start;
     break;
    case KEY:
     array[i]=start.key();
     break;
    case VALUE:
     array[i]=start.value();
     break;
   }
   i++; length--;
   start=start.next();
   if ((start==null) && (cyclic)) start=_first;
  }
  return i-startindex;
 }
 /**
 * Liefert eine Kopie der Kette beginnend mit start und endend mit end oder mit 
 * maximal count Elementen. Ist start==null, so wird start auf _first gesetzt.
 * Ist count<0, so endet die Kette bei end oder beim last(), falls end==null.
 * Die kopierte Kette hat keinen Besitzer. Die in den ListItems enthaltenen
 * Objekte werden nicht mit geklont.
 * @param start erstes Element der Kette
 * @param end letztes Element der Kette, falls end!=null
 * @param count maximale Anzahl der Elemente der Kette, falls count>=0
 * @param cyclic falls true wird die Liste als zyklische Liste betrachtet
 * @return erstes Element der kopierten Kette
 */
 protected ListItem getCopy(ListItem start,ListItem end,int count,boolean cyclic) {
  if (empty()) return null;
  if (start==null) start=_first;
  if ((start==null) || (!contains(start)) || ((end!=null) && (!contains(end)))) return null;
  if ((count<0) && (end==null)) end=_last;
  if (end!=null) end=end.next();
  ListItem first=null,i=null,j=null;
  while ((start!=null) && (start!=end) && (count!=0)) {
   i=(ListItem)start.clone();
   if (!i.connect(j,false)) throwException(new ListException(j,i,ListException.CONNECT));
   j=i; start=start.next(); count--;
   if ((cyclic) && (start==null)) start=_first;
   if (first==null) first=i;
  }
  return first;
 }
 /**
 * Klont die Kette von start bis end mit maximal count Elementen und gibt
 * eine Referenz auf das erste geklonte Element zurueck. Die geklonten
 * ListItems sollten auch geklonte Wert-Objekte enthalten (Aufruf von
 * cloneData()).
 * @param start Startelement des Intervalls
 * @param end Endelement des Intervalls
 * @param count maximal Laenge des Intervalls
 * @return Referenz auf das erste Element der geklonten Kette
 * @see ListItem#cloneData
 */
 protected static ListItem cloneChain(ListItem start,ListItem end,int count) {
 	if (start==null) return null;
 	if (end!=null) end=end.next();
 	ListItem first=null,i,j=null;
 	while ((start!=null) && (start!=end) && (count!=0)) {
 		i=(ListItem)start.clone();
 		i.cloneData();
   if (!i.connect(j,false)) throw new ListException(j,i,ListException.CONNECT);
 		if (first==null) first=i;
 		start=start.next(); count--; j=i;
 	}
 	return first;
 }
// Inner-Class. - Kann bei Umstieg auf Java 1.1 wieder aktiviert werden!
// /**
// * Enumeration-Objekt zum Aufzaehlen von Listen-Elementen mit enumerate.
// */
// private class ListEnumerator implements Enumeration {
//  /** Cursor-ListItem. */
//  private ListItem _i=null,_e=null;
//  /** Maximale Anzahl zu durchlaufender ListItems. */
//  private int _count=-1;
//  /** Falls true, werden Values zurueckgegeben, ansonsten Keys. */
//  private byte _objecttype;
//  /** privater Konstruktor. */
//  private ListEnumerator(ListItem start,ListItem end,int count,byte objecttype) {
//   _i=start; _e=end; if (_e!=null) _e=_e.next();
//   _count=count; _objecttype=objecttype;
//   if (_count==0) _i=null;
//  }
//  /** Liefert true, falls noch ein Element vorhanden. */
//  public boolean hasMoreElements() {
//   return (_i!=null);
//  }
//  /** Liefert das naechste Element in der Struktur. */
//  public Object nextElement() throws NoSuchElementException {
//  	if (_i==null) throw new NoSuchElementException();
//   Object O=null;
//   switch (_objecttype) {
//   	case LISTITEM:
//   		O=_i;
//   		break;
//   	case KEY:
//   		O=_i.key();
//   		break;
//   	case VALUE:
//   		O=_i.value();
//   		break;
//   }
//   _i=_i.next(); _count--;
//   if ((_i==_e) || (_count==0)) _i=null;
//   return O;
//  }
// }
 /**
 * Liefert ein Enumeration-Objekt, das das spezifizierte ListenIntervall
 * aufzaehlt.
 * @param start Beginn des Intervalls
 * @param end Ende des Intervalls (inklusive end)
 * @param count maximale Anzahl der Elemente im Intevall
 * @param objecttype Konstante um den Rueckgabewert von nextElement() zu
 * spezifizieren (also KEY, VALUE oder LISTITEM).
 */
 protected Enumeration enumerate(ListItem start,ListItem end,int count,byte objecttype) {
 	if (start==null) start=_first;
 	return new ListEnumerator(start,end,count,objecttype);
 }
 
 // Einfuegen
 // ========
 
 /**
 * Fuegt eine bestehende Kette der maximalen Laenge count, beginnend mit item nach
 * dem Element base der Liste ein. Ist base==null, wird am Anfang der Liste eingefuegt.
 * ListItems werden direkt eingefuegt, wenn setOwningList() erfolgreich den Eigentuemer
 * konnte. War dagegen setOwningList() nicht erfolgreich, wird das betreffende Objekt
 * mittels clone() geklont und diese Kopie eingefuegt, sofern setOwningList() das aendern
 * des Besitzers zulaesst (sollte eigentlich so sein, sonst geben wir es auf).
 * Die Laenge der Liste wird um die Anzahl der eingefuegten Elemente erhoeht.
 * Zurueckgegeben wird die tatsaechliche Anzahl der eingefuegten Elemente.
 * @param base ListItem, <strong>nach</strong> dem eingefuegt werden soll oder null,
 * falls am Anfang der Liste eingefuegt wird.
 * @param item ListItem bzw. Kette von ListItems, die eingefuegt werden sollen
 * @param end Ende des Intervalls von ListItems, die eingefuegt werden sollen,
 * falls end!=null
 * @param count maximale Anzahl einzufuegender ListItems. Ist count<0, so werden so
 * wird die gesamte Kette, die an start in Vorwaertsrichtung(!) haengt und bis
 * end geht, falls end!=null bzw. bis zum Ende der Kette, eingehangen.
 */
 protected synchronized int add(ListItem base,ListItem item,ListItem end,int count) {
  allowAccess(ANY_ACCESS);
  ListItem start=null,i=null,j=null;
  int length=0;
  if (end!=null) end=end.next();
  while ((item!=null) && (item!=end) && (count!=0)) {
  	j=i; i=item; item=item.next();
  	if (!i.setOwningList(this)) {
  		i=(ListItem)i.clone();
  		if (!i.setOwningList(this)) throwException(new ListException(i,ListException.SET_OWNER));
  	}
   if ((j!=null) && (!j.connect(i,true))) throwException(new ListException(j,i,ListException.CONNECT));
  	if (start==null) start=i;
  	count--; length++;
  }
  allowAccess(NO_ACCESS);
  _length+=length=reconnect(base,start,null,length);
  return length;
 }
 /**
 * Fuegt length im ListItem-Array ab startindex enthaltene ListItems nach dem ListItem 
 * Die Laenge der Liste wird um die Anzahl der eingefuegten Elemente erhoeht.
 * base in die Liste ein. Ist base==null wird an den Anfang der Liste eingefuegt.
 * @param base ListItem, ab dem eingefuegt werden soll
 * @param array Feld mit den ListItems
 * @param startindex Startindex im Feld
 * @param length maximale Anzahl der Elemente aus dem Array
 * @return tatsaechliche Anzahl eingefuegter Elemente
 */
 protected synchronized int add(ListItem base,ListItem[] array,int startindex,int length) {
  allowAccess(ANY_ACCESS);
  for(int i=startindex;i<startindex+length;i++) {
   if ((array[i]!=null) && (!contains(array[i]))) {
    if (!array[i].setOwningList(this)) {
     array[i]=(ListItem)array[i].clone();
     if (!array[i].setOwningList(this)) array[i]=null;
    }
   }
  }
  int c=reconnect(base,array,startindex,length);
  _length+=c;
  allowAccess(NO_ACCESS);
  return c;
 }
 /**
 * Fuegt die Kette, die an start (nach vorne) haengt und maximal count Elemente
 * lang ist, nach dem Element base in die Liste ein. Alle Elemente der Kette
 * sollten bereits der Liste gehoeren! Ist dies nicht der Fall, gibt es einen
 * Fehler beim Verknuepfen und es wird ein ListException ausgeloest.
 * Base sollte ebenfalls zur Liste gehoeren. Ist base==null, so wird die Kette
 * am Anfang der Liste eingehangen.
 * Bei dieser Methode wird die Anzahl der in der Liste enthaltenen Objekte nicht
 * veraendert!
 * @param base ListItem, ab dem eingehangen werden soll
 * @param start ListItem bzw. Kette von ListItems, die eingefuegt werden sollen
 * @param end Ende des Intervalls von ListItems, die eingefuegt werden sollen,
 * falls end!=null
 * @param count maximale Anzahl einzufuegender ListItems. Ist count<0, so werden so
 * wird die gesamte Kette, die an start in Vorwaertsrichtung(!) haengt und bis
 * end geht, falls end!=null bzw. bis zum Ende der Kette, eingehangen.
 * @return tatsaechliche Anzahl eingehangener Elemente
 */
 protected synchronized int reconnect(ListItem base,ListItem start,ListItem end,int count) {
  if (((base!=null) && (!contains(base))) || (start==null) || (count==0)) return 0;
  allowAccess(ANY_ACCESS);
  ListItem next,i,j=null;
  if (base!=null) next=base.next(); else next=_first;
  if (!start.connect(base,false)) throwException(new ListException(start,base,ListException.CONNECT));
  i=start;
  if (end!=null) end=end.next();
  int c=0;
  while ((i!=null) && (i!=end) && (count!=0)) {
   j=i; i=i.next(); count--; c++;
  }
  if ((j!=null) && (!j.connect(next,true)))
   throwException(new ListException(j,next,ListException.CONNECT));
  if (next==null) _last=j;
  if (base==null) _first=start;
  allowAccess(NO_ACCESS);
  return c;
 }
 /**
 * Haengt ein Intervall eines Arrays von ListItems in die Liste ein. Alle 
 * Elemente des Array sollten bereits der Liste gehoeren! Ist dies nicht 
 * der Fall, werden die entsprechenden ListItems nicht eingehangen.
 * Base sollte ebenfalls zur Liste gehoeren. Ist base==null, so wird das Array
 * am Anfang der Liste eingehangen.
 * Bei dieser Methode wird die Anzahl der in der Liste enthaltenen Objekte nicht
 * veraendert!
 * @param base ListItem, ab dem eingehangen werden soll
 * @param array Array mit ListItems, die eingehaengt werden sollen
 * @param startindex Startindex im Array
 * @param length maximale Anzahl der Elemente aus dem Feld
 * @return tatsaechliche Anzahl eingehangener Elemente
 */
 protected synchronized int reconnect(ListItem base,ListItem[] array,int startindex,int length) {
  if (((base!=null) && (!contains(base))) || (array==null) || (length==0)) return 0;
  allowAccess(ANY_ACCESS);
  ListItem next,i=base,start=null;
  if (base!=null) next=base.next(); else next=_first;
  int j=startindex,c=0;
  while (length>0) {
   if ((array[j]!=null) && (array[j].connect(i,false))) {
    i=array[j]; c++;
    if (start==null) start=i;
   }
   j++; length--;
  }
  if ((start!=null) && (!i.connect(next,true)))
     throwException(new ListException(i,next,ListException.CONNECT));
  if (next==null) _last=i;
  if (base==null) _first=start;
  allowAccess(NO_ACCESS);
  return c;
 }

 // Ausschneiden und Entfernen
 // ==========================
 
 /**
 * Schneidet die Kette beginnend mit start und endend bei end bzw. nach count Schritten, falls
 * count>0, aus der Liste. Zurueckgegeben wird die Anzahl der tatsaechlich ausgeschnittenen
 * ListItems.
 * @param start erstes auszuschneidendes ListItem
 * @param end letztes auszuschneidendes ListItem
 * @param count maximale Anzahl auszuschneidener ListItems, falls >0
 * @return tatsaechliche Anzahl ausgeschnittener Elemente
 */
 protected synchronized int cut(ListItem start,ListItem end,int count) {
  int c=unconnect(start,end,count);
  if (c>0) {
   allowAccess(ANY_ACCESS);
   ListItem i,j=null;
   if (!start.connect(null,false)) throwException(new ListException(null,start,ListException.CONNECT));
   while (start!=null) {
    i=start.next();
    if (!start.setOwningList(null)) throwException(new ListException(start,ListException.SET_OWNER));
    j=start; start=i;
   }
   if (j!=null) if (!j.connect(null,true)) throwException(new ListException(j,null,ListException.CONNECT));
   _length-=c;
   allowAccess(NO_ACCESS);
  }
  return c;
 }
 /**
 * Loescht die Kette beginnend mit start und endend bei end bzw. nach count Schritten, falls
 * count>0, aus der Liste. Falls noch Referenzen auf die geloeschten ListItems existieren,
 * enthalten die ListItems zwar noch ihre Daten, sind aber nicht mehr verkettet.
 * @param start erstes zu loeschendes ListItem
 * @param end letztes zu loeschendes ListItem
 * @param count maximale Anzahl zu loeschender ListItems, falls >0
 * @return tatsaechliche Anzahl geloeschter Elemente
 */
 protected synchronized int remove(ListItem start,ListItem end,int count) {
  int c=unconnect(start,end,count);
  if (c>0) {
   allowAccess(ANY_ACCESS);
   ListItem i;
   while (start!=null) {
    i=start.next();
    if ((!start.clearConnections()) || (!start.setOwningList(null)))
     throwException(new ListException(start,ListException.REMOVE));
    start=i;
   }
   _length-=c;
   allowAccess(NO_ACCESS);
  }
  return c;
 }
 /**
 * Loest die Kette beginnend mit start und endend bei end bzw. nach
 * count Schritten, falls count>0, aus der Liste. Die Anzahl der
 * Listenelemente wird <strong>nicht</strong> geaendert.
 * @param start erstes auszuschneidendes ListItem
 * @param end letztes auszuschneidendes ListItem
 * @param count maximale Anzahl auszuschneidener ListItems, falls >0
 * @return tatsaechliche Anzahl ausgeschnittener Elemente
 */
 protected synchronized int unconnect(ListItem start,ListItem end,int count) {
  if (!contains(start)) return 0;
  ListItem base=start.prev(),i=start,j=null;
  if (end!=null) end=end.next();
  int c=0;
  while ((i!=null) && (i!=end) && (count!=0)) {
   j=i; i=i.next(); c++; count--;
  }
  allowAccess(ANY_ACCESS);
  if (!start.connect(null,false)) throwException(new ListException(null,start,ListException.CONNECT));
  if (j!=null) if (!j.connect(null,true)) throwException(new ListException(j,null,ListException.CONNECT));
  if (i!=null) {
   if (!i.connect(base,false)) throwException(new ListException(base,i,ListException.CONNECT));
  } else {
  	_last=base;
  	if (base!=null) if (!base.connect(null,true)) throwException(new ListException(base,i,ListException.CONNECT));
  }
  if (base==null) _first=i;
  allowAccess(NO_ACCESS);
  return c;
 }
 /**
 * Loescht die gesamte Liste. Falls beim Loeschen Fehler beim Freigeben der 
 * ListItems auftreten (kann eigentlich nur bei fehlerhafter ListItem-Implementation
 * passieren), werden diese abgefangen und die Liste trotzdem geloescht. (Es kann
 * dann sein, das ListItems uebrigbleiben, die angeblich dieser Liste gehoeren...)
 */
 protected void clear() {
  try {
   remove(_first,null,-1);
  } catch (ListException e) {}
  _first=null; _last=null; _length=0;
 }
 
 // Modifikation der gesamten Liste
 // ===============================
 
 /**
 * Dreht die Liste zyklisch so, dass item das erste Element der Liste ist.
 * Konstante Laufzeit.
 * @param item ListItem, das erstes Element werden soll
 */
 protected synchronized void cycle(ListItem item) {
  if ((_length<2) || (!contains(item))) return;
  allowAccess(ANY_ACCESS);
  if (!_last.connect(_first,true)) throwException(new ListException(_last,_first,ListException.CONNECT));
  _first=item; _last=item.prev();
  if (!_first.connect(null,false)) throwException(new ListException(null,_first,ListException.CONNECT));
  if (!_last.connect(null,true)) throwException(new ListException(_last,null,ListException.CONNECT));
  allowAccess(NO_ACCESS);
 }
 /**
 * Verdreht die Liste zyklisch um dist ListItems
 * @param dist Anzahl der zu drehenden ListItems
 */
 protected void cycle(int dist) {
  ListItem i=getListItem(_first,dist,true);
  cycle(i);
 }
 /**
 * Kehrt die Reihenfolge der ListItems im Intervall von start bis end um.
 * Ist start==null, wird start auf den Listenanfang gesetzt. Das Intervall
 * ist maximal count ListItems lang (falls count>0). Ist end==null und 
 * count<0, so wird die gesamte Liste ab start umgedreht.
 * @param start erstes ListItem
 * @param end letztes ListItem (oder ==null)
 * @param count Laenge des Intervalls (falls count>0)
 */
 protected synchronized void reverse(ListItem start,ListItem end,int count) {
  if (start==null) start=_first;
  if ((!contains(start)) || ((end!=null) && (!contains(end)))) return;
  if (end!=null) end=end.next();
  ListItem i=start,j=null,n,k=start.prev();
  allowAccess(ANY_ACCESS);
  while ((i!=null) && (i!=end) && (count!=0)) {
   n=i.next();
   if (!i.connect(j,true)) throwException(new ListException(i,j,ListException.CONNECT));
   j=i; i=n; count--;
  }
  if (!j.connect(k,false)) throwException(new ListException(k,j,ListException.CONNECT));
  if (!start.connect(end,true)) throwException(new ListException(start,end,ListException.CONNECT));
  allowAccess(NO_ACCESS);
  if (k==null) _first=j;
  if (i==null) _last=start;
 }

 // Methoden fuer das Interface Owner
 // ================================
 
 /**
 * Erlaubt den Zugriff (temporaer) von ListItems
 */
 protected void allowAccess(byte accesskonst) {
  _access=accesskonst;
 }
 /**
 * Sperrt den Listenzugriff und loest die uebergebene RuntimeException aus.
 * @param e Fehlerklasse
 */
 private void throwException(RuntimeException e) {
  allowAccess(NO_ACCESS);
  throw e;
 }
 /**
 * Das Owned-Objekt who bittet den Owner um Genehmigung einer 
 * Aktion vom Typ accesstype mit dem Argument argument.
 * @param accesstype Typ der Aktion (des Zugriffs)
 * @param who Objekt, welches den Zugriff erbittet
 * @param argument Argument der Aktion
 * @return true, falls der Owner die Aktion erlaubt, sonst false
 */
 public boolean requestAccess(int accesstype,Object who,Object argument) {
  if (_access==ANY_ACCESS) return true;
  switch (accesstype) {
   case ListItem.SET_OWNER:
   case ListItem.REMOVE:
   case ListItem.CLEAR_CONNECTIONS:
   case ListItem.CONNECT_FORWARD:
   case ListItem.CONNECT_BACKWARD:
    return false;
  }
  return true;
 }
 
 
 	/**
 	 * Wandelt die Liste in einen Array um und gibt diesen zurück
 	 * @return Ein Array der Listenelemente
 	 */
	 public Object[] toArray()
	 {
		 Object[] obj = null;
		 
		 if (_length > 0)
		 {
			 try
			 {
				 obj = new Object[_length];
				 ListItem current = _first;
				 
				 obj[0] = current.value();
				 current = current.next();
				 
				 for (int i=1; i< _length; ++i)
				 {
					 obj[i] = current.value();
					 current = current.next();
				 }
			 }
			 catch (Exception e)
			 {
				 System.err.println(e.toString());
			 }		 
		 }
		 return obj;
	 }
 
}
