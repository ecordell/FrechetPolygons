package anja.util;

import java.util.Enumeration;
//import java.util.NoSuchElementException;


/**<p>
* <p>SimpleList ist eine doppelt verkettete Liste basierend auf
* BasicList. SimpleList beinhaltet zahlreiche Funktionen zur
* Navigation, zum Suchen, zum Konvertieren und fuer
* Teillistenoperationen.</p>
* 
* <h4>0. Allgemeines und Funktionsweise</h4>
* 
* <p>SimpleList (und auch alle abgeleiteten Klassen) ist eine
* doppelt verkettete Liste, die <strong>ListItems</strong>
* speichern kann. ListItem ist ein Interface und wird
* beispielsweise von <strong>SimpleListItem</strong> oder <strong>StdListItem</strong>
* implementiert. SimpleList verwednet hauptsaechlich
* SimpleListItems. Da aber die Moeglichkeit besteht, direkt
* ListItems anzufuegen, kann jedes beliebige Objekt, das ListItem
* implementiert, angefuegt werden. Zum korrekten Funktionieren ist
* es aber erforderlich, dass das ListItem die Schnittsttellen
* korrekt implementiert; dies bedeutet auch, dass es den
* Spezifikationen entsprechend bei der uebergeordneten Struktur
* (der Liste der es angehoert) nachfragt, bevor es eine Aktion
* durchfuehrt.<br>
* Da SimpleList Cloneable implementiert, existiert auch <strong>clone()</strong>
* und funktioniert folgendermassen: Es werden Kopien aller
* gespeicherten ListItems angelegt, sowie aller gespeicherten
* Werte!<br>
* Desweiteren gibt es <strong>copyList(SimpleList)</strong>, das nur die Liste
* selbst klont, die ListItems werden nicht geklont.</p>
*
* <h4>1. Konstruktion neuer Listen</h4>
* 
* <p>Eine leere Liste wird mit <strong>SimpleList()</strong>
* erstellt. <strong>SimpleList(BasicList)</strong> konvertiert eine
* auf BasicList basierende Struktur in eine SimpleList. Dabei wird
* die uebergebene Liste geloescht. Im Gegensatz dazu <em>kopiert</em>
* <strong>SimpleList(ListItem,ListItem)</strong> eine Teil-Kette
* aus einer anderen Struktur. Die Teilkette wird nur direkt
* angefuegt, wenn sie keinen Besitzer hat oder dieser es zulaesst.
* Mit <strong>SimpleList(Object[],int,int)</strong> schliesslich
* kann man eine Liste aus einem Objekt-Array konstruieren.<br>
* <strong>SimpleList(java.util.LinkedList)</strong> erzeugt eine neue
* SimpleList, die die Elemente der LinkedList in gleicher Reihenfolge wie dort
* als Referenzen (nicht als Kopien!) enthaelt.</p>
* 
* <h4>2. Zugriff und Navigation</h4>
* 
* <p><strong>length()</strong> liefert die Anzahl der gespeicherten
* Elemente, <strong>first()</strong> liefert eine Referenz auf das
* erste ListItem der Liste, entsprechend <strong>last()</strong>
* einen auf das letzte. <strong>firstKey()</strong> liefert den
* ersten Schluessel, <strong>firstValue()</strong> den ersten
* Objektwert, entsprechend <strong>lastKey()</strong> und <strong>lastValue()</strong>
* fuer den letzten Schluessel / Wert. Wurden Objekte mit den
* Standard-Methoden angefuegt (also ohne vorher selbst ListItems zu
* erzeugen), liefern key() und value() jeweils dasselbe Objekt
* zurueck. <strong>empty()</strong> testet, ob die Liste leer ist, <strong>contains(ListItem)</strong>
* testet in konstanter Zeit, ob das ListItem dieser Liste
* angehoert. <strong>contains(Object)</strong> macht dasselbe, nur
* benoetigt diese Methode wegen der notwendigen Suche nach dem
* ListItem lineare Zeit. <strong>getIndex(ListItem)</strong>
* liefert den ListenIndex zurueck, andersherum liefert <strong>at(int)</strong>
* das ListItem mit einem bestimmten Index zurueck, entsprechend <strong>getValueAt(int)</strong>
* den Objektwert oder <strong>getKeyAt(int)</strong> den
* Schluesselwert. <strong>getDistance(ListItem,ListItem)</strong>
* berechnet den Abstand zweier ListItems in der Liste und <strong>value(ListItem)</strong>
* bzw. <strong>key(ListItem)</strong> liefern den im uebergebenen
* ListItem gespeicherten Wert bzw. Schluessel.<br>
* <strong>next(ListItem)</strong> liefert das in der Liste
* nachfolgende bzw. <strong>next(ListItem,int count)</strong> das
* count Schritte entfernte ListeItem. Entsprechend <strong>prev(ListItem)</strong>
* bzw. <strong>prev(ListItem,int)</strong> fuer vorhergehende. Mit <strong>relative(ListItem,int)</strong>
* bzw. <strong>cyclicRelative(ListItem,int)</strong> kann man auch
* auf von einem ListItem in einer bestimmten Entfernung liegende
* ListItems zugreifen (und die Liste dabei als Ring betrachten).</p>
* 
* <h4>3. Suchen von Objekten</h4>
* 
* <p><strong>find(Object)</strong> bzw. <strong>find(Object,ListItem)</strong>
* durchsucht die Liste nach einem passenden Objekt (ab einer
* bestimmten Position). Der Vergleich erfolgt ueber die
* equals-Methode der ListItems und sollte key()-Werte vergleichen.
* Zusaetzlich kann man noch mit <strong>findBigger(Object
* key,Comparitor)</strong> und <strong>findSmaller(Object,Comparitor)</strong>
* mit Hilfe eines Comparitor-Objektes nach ListItems mit Werten 'in
* der Naehe von key' Suchen. <strong>min(Comparitor)</strong> und <strong>max(Comparitor)</strong>
* liefern das Minimum bzw. Maximum in der Liste (der Comparitor
* sollte ListItems (bzw. KeyValueHolder) vergleichen koennen).</p>
* 
* <h4>4. Einfuegen und Entfernen von Elementen</h4>
* 
* <p>Objekte lassen sich mit <strong>add(Object)</strong> ans Ende
* der Liste oder mit <strong>insert(ListItem,Object)</strong>/ vor
* eine bestimmte Position einfuegen. Dabei wird ein SimpleListItem
* erzeugt, das das Objekt speichert. Zusaetzlich existieren
* Methoden, um Java-Basistypen direkt einzufuegen (es wird dann ein
* Objekt-Wrapper erzeugt und eingefuegt) und Methoden zum Verwenden
* von Listen als Stack oder Queue. Ausserdem koennen andere
* ListItems mit <strong>add(ListItem)</strong> bzw. <strong>insert(ListItem)</strong>
* angefuegt werden.<br>
* Mit <strong>addAll(BasicList)</strong> werden alle ListItems einer Liste
* an das Ende der Liste eingefuegt.</p>
* 
* <h4>5. Teillistenoperationen</h4>
* 
* <p>Mit <strong>copy</strong> koennen ListenIntervalle kopiert
* werden, mit <strong>cut</strong> werden sie ausgeschnitten und
* mit <strong>paste</strong> wieder vor einer Position eingefuegt.
* Bei paste werden die Intervall direkt angefuegt - wenn der
* Besitzer des Intervalls dies zulaesst, ansonsten wird das
* Intervall kopiert. Es ist also nicht unbedingt notwendig ein
* Intervall aus einer Liste mit copy zu kopieren um es in einer
* anderen einzufuegen; man kann das Intervall direkt in der anderen
* Liste anfuegen - es wird dann eine Kopie eingefuegt.</p>
* 
* <h4>6. Zugriff und Konvertierung der gesamten Liste</h4>
* 
* <p>Es stehen verschieden Methoden zur Verfuegen um die Liste in
* andere Datenstrukturen umzuwandeln. <strong>convertKeysToArray()</strong>
* speichert die Schluesselwerte, <strong>convertValuesToArray()</strong>
* die Objektwerte in ein Array. <strong>keys()</strong> bzw. <strong>values()</strong>
* liefern jeweils ein Enumeration-Objekt zum durchlaufen der
* Listenschluessel bzw. Listenwerte.</p>
* 
* <h4>7. Modifikation der gesamten Liste</h4>
* 
* <p><strong>reverse()</strong> kehrt die Reihenfolge der ListItems
* um, <strong>cycle</strong> verschiebt den Listenanfang zyklisch.
* Mit <strong>sort</strong> kann man die Listenschluessel mit Hilfe
* eines Comparitors sortieren (der Comparitor sollte ListItems
* (bzw. KeyValueHolder) vergleichen koennen).</p>
*
* @see BasicList
* @see Stack
* @see Queue
* @see ListItem
* @see SimpleListItem
*
* @author Thomas Wolf
* @version 1.1
*/
public class SimpleList extends BasicList implements Cloneable {

 // Konstruktion
 // ============
 
 /**
 * Leerer Konstruktor.
 */
 public SimpleList() {
  super();
 }
 /**
 * Konvertier-Konstruktor. Uebernimmt alle Elemente der Struktur L in diese Liste.
 * @param L zu konvertierende Liste
 */
 public SimpleList(BasicList L) {
  super();
  concat(L);
 }
 /**
 * Kopier-Konstruktor. Kopiert eine Teilliste in die Liste. Ist die Kette Teil einer
 * Struktur, so sollte i.a. eine Kopie eingefuegt werden und die Quellstruktur
 * nicht veraendert werden.
 * @param start Startelement
 * @param end Endelement
 */
 public SimpleList(ListItem start,ListItem end) {
  super();
  paste(null,start,end);
 }
 /**
 * Kopier-Konstruktor. Kopiert eine Teilliste in die Liste. Ist die Kette Teil einer
 * Struktur, so sollte i.a. eine Kopie eingefuegt werden und die Quellstruktur
 * nicht veraendert werden.
 * @param start Startelement
 * @param count Laenge der Kette
 */
 public SimpleList(ListItem start,int count) {
  super();
  paste(null,start,count);
 }
 /**
 * Teilarray-Konstruktor. Konstruiert eine Liste aus den uebergebenen Teil-Array.
 */
 public SimpleList(Object[] array,int startindex,int length) {
  super();
  insert(null,array,startindex,length);
 }


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
 	SimpleList L=new SimpleList();
 	ListItem i=super.getCopy(null,null,-1,false),j=i;
 	while (j!=null) {
 		j.cloneData();
 		j=j.next();
 	}
 	L.add(null,i,null,-1);
 	return L;
 }


   /**
   * Liefert eine Kopie der Liste, d.h. die Liste wird geklont, ohne die
   * ListItems selbst zu klonen.
   *
   * @return eine Kopie der Liste
   */
   public SimpleList copyList() {

      SimpleList cp = new SimpleList();
      for ( int i = 0; i < length(); i++ ) {
         cp.add( at( i ) );
      } // for
      return cp;

   } // copyList


 // Zugriff
 // =======

 /**
 * Liefert die Anzahl der in der Liste gespeicherten Elemente.
 * @return Anzahl der Elemente
 */
 public int length() { return super.length(); }
 /**
 * Liefert true, falls die Liste leer ist.
 * @return true, falls die Liste leer ist
 */
 public boolean empty() { return super.empty(); }
 /**
 * Liefert das erste ListItem der Liste.
 * @return erstes ListItem in der Liste
 */
 public ListItem first() { return super.first(); }
 /**
 * Liefert das letzte ListItem der Liste.
 * @return letztes ListItem in der Liste
 */
 public ListItem last() { return super.last(); }
 /**
 * Liefert den ersten in der Liste gespeicherten Schluessel. 
 * Falls die Liste leer ist, wird ein ListException ausgeloest.
 * @return erster Schluessel
 */
 public Object firstKey() {
  if (super.length()>0) return first().key();
  else throw new ListException(ListException.EMPTY_LIST);
 }
 /**
 * Liefert den letzten in der Liste gespeicherten Schluessel. 
 * Falls die Liste leer ist, wird ein ListException ausgeloest.
 * @return letztes Schluessel
 */
 public Object lastKey() {
  if (super.length()>0) return last().key();
  else throw new ListException(ListException.EMPTY_LIST);
 }
 /**
 * Liefert das erste in der Liste gespeicherte Objekt. 
 * Falls die Liste leer ist, wird ein ListException ausgeloest.
 * @return erstes Element
 */
 public Object firstValue() {
  if (super.length()>0) return first().value();
  else throw new ListException(ListException.EMPTY_LIST);
 }
 /**
 * Liefert das letzte in der Liste gespeicherte Objekt.
 * Falls die Liste leer ist, wird ein ListException ausgeloest.
 * @return letztes Element
 */
 public Object lastValue() {
  if (super.length()>0) return last().value();
  else throw new ListException(ListException.EMPTY_LIST);
 }
 /**
 * Liefert true, falls das ListItem item in der Liste enthalten ist.
 * Der Test erfolgt in konstanter Zeit.
 * @param item zu testendes ListItem
 * @return true, falls item in der Liste enthalten ist.
 */
 public boolean contains(ListItem item) { return super.contains(item); }
 /**
 * Liefert true, falls das Objekt object in der Liste enthalten ist.
 * @param object zu suchendes Objekt
 * @return true, falls item in der Liste enthalten ist.
 */
 public boolean contains(Object object) {
  return (super.find(object,super.first(),null,-1,false)!=null);
 }
 /**
 * Berechnet den Index innerhalb der Liste vom Anfang an.
 * Ist i nicht in der Liste enthalten, wird -1 zurueckgegeben.
 * @param i ListItem, dessen Index bestimmt werden soll
 * ansonsten das letzte
 */ 
 public int getIndex(ListItem i) {
  return super.getIndex(i,true);
 }
 /**
 * Liefert den Abstand der beiden ListItems i und j in der Liste.
 * Ein positiver Rueckgabewert bedeutet, dass j von i aus durch 
 * vorwaertslaufen zu erreichen ist, negative Werte bedeuten, dass j von
 * i aus durch rueckwaertslaufen erreichbar ist.
 * Ist i oder j nicht in der Liste oder sonst nicht erreichbar (?!)
 * so wir Integer.MAX_VALUE zurueckgegeben.
 * @param i und
 * @param j ListItems, deren Abstand bestimmt werden soll.
 */
 public int getDistance(ListItem i,ListItem j) {
  return super.getDistance(i,j);
 }
 /**
 * Liefert den Schluessel des ListItems item zurueck. Ist item nicht
 * in der Liste enthalten, wird ein ListException ausgeloest.
 * @return in item gespeicherter Schluessel
 */
 public Object key(ListItem item) {
  if (!contains(item)) throw new ListException(ListException.ILLEGAL_ACCESS);
  return item.key();
 }
 /**
 * Liefert den Wert des ListItems item zurueck. Ist item nicht
 * in der Liste enthalten, wird ein ListException ausgeloest.
 * @return in item gespeicherter Wert
 */
 public Object value(ListItem item) {
  if (!contains(item)) throw new ListException(ListException.ILLEGAL_ACCESS);
  return item.value();
 }
 
 // Navigation auf der Liste
 // ========================
 
 /**
 * Liefert das naechste ListItem nach i in der Liste.
 * Ist i==null, wird das erste ListItem der Liste zurueckgeliefert.
 * Ist i nicht in der Liste enthalten, wird null zurueckgegeben.
 * @param i ListItem in der Liste
 * @return nachfolgendes ListItem
 */
 public ListItem next(ListItem i) {
  if (i==null) return super.first();
  if (!super.contains(i)) return null;
  return i.next();
 }
 /**
 * Liefert das ListItem in der Liste, das count Schritte in Vorwaertsrichtung
 * von base entfernt ist (oder null, falls base nicht zur Liste gehoert).
 * Ist base==null, wird vom Anfang der Liste ausgegangen.
 */
 public ListItem next(ListItem base,int count) {
  return super.getListItem(base,count,false);
 }
 /**
 * Liefert das ListItem in der Liste, das count Schritte 
 * von base entfernt ist (oder null, falls base nicht zur Liste gehoert).
 * Ist base==null, wird vom Anfang der Liste ausgegangen.
 */
 public ListItem relative(ListItem base,int count) {
  return super.getListItem(base,count,false);
 }
 /**
 * Liefert den Wert des ListItems in der Liste, das count Schritte 
 * von base entfernt ist (oder null, falls base nicht zur Liste gehoert).
 * Ist base==null, wird vom Anfang der Liste ausgegangen.
 */
 public Object relativeValue(ListItem base,int count) {
  ListItem i=super.getListItem(base,count,false);
  if (i==null) throw new ListException(ListException.NO_SUCH_ELEMENT);
  return i.value();
 }
 /**
 * Liefert das ListItem in der Liste, das count Schritte 
 * von base entfernt ist (oder null, falls base nicht zur Liste gehoert),
 * wenn man die Liste als zyklische Liste betrachtet.
 * Ist base==null, wird vom Anfang der Liste ausgegangen.
 */
 public ListItem cyclicRelative(ListItem base,int count) {
  return super.getListItem(base,count,true);
 }
 /**
 * Liefert den Wert des ListItems in der Liste, das count Schritte 
 * von base entfernt ist (oder null, falls base nicht zur Liste gehoert),
 * wenn man die Liste als zyklische Liste betrachtet.
 * Ist base==null, wird vom Anfang der Liste ausgegangen.
 */
 public Object cyclicRelativeValue(ListItem base,int count) {
  ListItem i=super.getListItem(base,count,false);
  if (i==null) throw new ListException(ListException.NO_SUCH_ELEMENT);
  return i.value();
 }
 /**
 * Liefert das ListItem in der Liste, das count Schritte in Rueckwaertsrichtung
 * von base entfernt ist (oder null, falls base nicht zur Liste gehoert).
 * Ist base==null, wird vom Ende der Liste ausgegangen.
 */
 public ListItem prev(ListItem base,int count) {
  if (base==null) base=super.last();
  return super.getListItem(base,-count,false);
 }
 /**
 * Liefert das vorige ListItem vor i in der Liste.
 * Ist i==null, wird das letzte ListItem der Liste zurueckgeliefert.
 * Ist i nicht in der Liste enthalten, wird null zurueckgegeben.
 * @param i ListItem in der Liste
 * @return vorhergehendes ListItem
 */
 public ListItem prev(ListItem i) {
  if (i==null) return super.last();
  if (!super.contains(i)) return null;
  return i.prev();
 }
 /**
 * Liefert das ListItem mit Index index zurueck.
 * @param index Index
 * @return ListItem mit Index index
 */
 public ListItem at(int index) {
  return super.getListItem(null,index,false);
 }
 /**
 * Liefert den im ListItem mit Index index gespeicherten Wert zurueck.
 * @param index Index
 * @return an der Stelle index gespeicherter Wert
 */
 public Object getValueAt(int index) {
  ListItem i=super.getListItem(null,index,false);
  if (i==null) throw new ListException(ListException.NO_SUCH_ELEMENT);
  return i.value();
 }
 /**
 * Liefert den im ListItem mit Index index gespeicherten Schluessel zurueck.
 * @param index Index
 * @return an der Stelle index gespeicherter Schluessel
 */
 public Object getKeyAt(int index) {
  ListItem i=super.getListItem(null,index,false);
  if (i==null) throw new ListException(ListException.NO_SUCH_ELEMENT);
  return i.key();
 }
 
 // Methoden zum Suchen von ListItems
 // =================================
 
 /**
 * Sucht das ListItem item in der Liste und liefert es zurueck, falls es
 * gefunden wurde, ansonsten wird null zurueckgegeben.
 * Die Suche erfolgt in konstanter Zeit.
 * @param item zu suchendes ListItem
 * @return item, falls gefunden, ansonsten null
 */
 public ListItem find(ListItem item) { if (contains(item)) return item; else return null; }
 /**
 * Liefert das ListItem in der Liste, das das Objekt O
 * enthaelt, bei dem also equals(O) true zurueckgegeben hat.
 * Die Suche benoetigt lineare Zeit.
 * @param O gesuchtes Objekt
 * @return das gefundene ListItem oder null
 */
 public ListItem find(Object O) {
  return super.find(O,null,null,-1,false);
 }
 /**
 * Liefert das ListItem in der Liste ab dem ListItem start, das das Objekt O
 * enthaelt, bei dem also equals(O) true zurueckgegeben hat.
 * Ist start==null wird vom Anfang der Liste an gesucht.
 * Die Suche benoetigt lineare Zeit.
 * @param O gesuchtes Objekt
 * @param start ListItem ab dem gesucht werden soll.
 * @return das gefundene ListItem oder null
 */
 public ListItem find(Object O,ListItem start) {
  return super.find(O,start,null,-1,false);
 }
 /**
 * Sucht mittels des Comparitors comparitor in der Liste das ListItem, das das
 * kleinste unter denen ist, die groesser als key sind. Ist key ein ListItem, so
 * werden die ListItems direkt verglichen, ansonsten werden die Schluessel
 * miteinander verglichen. Ist key==null, so ist der Aufruf aequivalent zu 
 * max(comparitor). Ist comparitor==null, so wird ein StdComparitor benutzt.
 * @param key Vergleichsobjekt
 * @param comparitor Comparitor-Objekt
 * @return kleinstes ListItem in der Liste, das noch groesser(gleich) key ist
 * @see Comparitor
 */
 public ListItem findBigger(Object key,Comparitor comparitor) {
  return super.findClosest(key,comparitor,Comparitor.BIGGER,null,null,-1,false);
 }
 /**
 * Sucht mittels des Comparitors comparitor in der Liste ab start das ListItem,
 * das das kleinste unter denen ist, die groesser als key sind.
 * @param key Vergleichsobjekt
 * @param comparitor Comparitor-Objekt
 * @return kleinstes ListItem in der Liste, das noch groesser(gleich) key ist
 * @see #findBigger(Object, Comparitor)
 */
 public ListItem findBigger(Object key,Comparitor comparitor,ListItem start) {
  return super.findClosest(key,comparitor,Comparitor.BIGGER,start,null,-1,false);
 }
 /**
 * Sucht mittels des Comparitors comparitor in der Liste das ListItem, das das
 * groesste unter denen ist, die kleiner als key sind. Ist key ein ListItem, so
 * werden die ListItems direkt verglichen, ansonsten werden die Schluessel
 * miteinander verglichen. Ist key==null, so ist der Aufruf aequivalent zu 
 * min(comparitor). Ist comparitor==null, so wird ein StdComparitor benutzt.
 * @param key Vergleichsobjekt
 * @param comparitor Comparitor-Objekt
 * @return groesstes ListItem in der Liste, das noch kleiner(gleich) key ist
 * @see Comparitor
 */
 public ListItem findSmaller(Object key,Comparitor comparitor) {
  return super.findClosest(key,comparitor,Comparitor.SMALLER,null,null,-1,false);
 }
 /**
 * Sucht mittels des Comparitors comparitor in der Liste ab start das ListItem,
 * das das groesste unter denen ist, die kleiner als key sind.
 * @param key Vergleichsobjekt
 * @param comparitor Comparitor-Objekt
 * @return groesstes ListItem in der Liste, das noch kleiner(gleich) key ist
 * @see #findSmaller(Object, Comparitor)
 */
 public ListItem findSmaller(Object key,Comparitor comparitor,ListItem start) {
  return super.findClosest(key,comparitor,Comparitor.SMALLER,start,null,-1,false);
 }
 /**
 * Sucht das Minimum in der Liste bezueglich der durch den Comparitor comparitor
 * definierten Ordnung.
 * @param comparitor Comparitor-Objekt zum Vergleichen
 * @return ListItem mit dem Minimum in der Liste
 */
 public ListItem min(Comparitor comparitor) {
  return super.findClosest(null,comparitor,Comparitor.SMALLER,null,null,-1,false);
 }
 /**
 * Sucht das Maximum in der Liste bezueglich der durch den Comparitor comparitor
 * definierten Ordnung.
 * @param comparitor Comparitor-Objekt zum Vergleichen
 * @return ListItem mit dem Maximum in der Liste
 */
 public ListItem max(Comparitor comparitor) {
  return super.findClosest(null,comparitor,Comparitor.BIGGER,null,null,-1,false);
 }
  
 // Einfuegen von Elementen
 // ======================
 
 /**
 * Erzeugt ein neues ListItem und speichert O in ihm.
 * @param O zu speicherndes Objekt
 * @return neues ListItem
 */
 private ListItem createNew(Object O) {
  return new SimpleListItem(O);
 }
 /**
 * Fuegt das ListItem item an das Ende der Liste an.
 * Gehoert item schon zur Liste oder kann der Eigentuemer nicht geaendert werden,
 * dann wird eine Kopie des ListItems eingefuegt.
 * @param item neues ListItem
 * @return eingefuegtes ListItem
 */
 public ListItem add(ListItem item) {
  if (super.contains(item)) item=(ListItem)item.clone();
  ListItem base=super.last();
  if (super.add(base,item,null,1)==1) {
   if (base!=null) return base.next(); else return super.first();
  } else throw new ListException(ListException.INSERTION_ERROR);
 }
 /**
 * Fuegt das Objekt object an das Ende der Liste an. Zurueckgegeben wird das ListItem,
 * das object enthaelt.
 * @param object einzufuegendes Objekt
 * @return ListItem, das object enthaelt
 * @see #add(ListItem)
 */
 public ListItem add(Object object) {
  return add(createNew(object));
 }
 
 
   /**
   * Fuegt alle Objekte der Liste list an das Ende der Liste an.
   *
   * @param list die Liste mit den einzufuegenden Objekten
   */
   public void addAll(
      SimpleList list
   ) {

      for ( int i = 0; i < list.length(); i++ ) {
         add( list.at( i ) );
      } // for

   } // addAll


 /**
 * Fuegt das ListItem item vor dem ListItem base in der Liste ein. Ist base==null, so
 * wird an das Ende der Liste eingefuegt. Gehoert base nicht zur Liste, so wird ein
 * ListException ausgeloest.
 * Gehoert item schon zur Liste oder kann der Eigentuemer nicht geaendert werden,
 * dann wird eine Kopie des ListItems eingefuegt.
 * @param base Position in der Liste, vor der eingefuegt wird
 * @param item neues ListItem
 * @return eingefuegtes ListItem
 */
 public ListItem insert(ListItem base,ListItem item) {
  if ((base!=null) && (!contains(base)))
   throw new ListException(ListException.ILLEGAL_ACCESS);
  if (super.contains(item)) item=(ListItem)item.clone();
  if (base!=null) base=base.prev(); else base=super.last();
  if (super.add(base,item,null,1)==1) {
   if (base!=null) return base.next(); else return super.first();
  } else throw new ListException(ListException.INSERTION_ERROR);
 }
 /**
 * Erzeugt ein neues ListItem, das object enthaelt und fuegt es vor base in die Liste
 * ein. Ist base==null, so wird an das Ende der Liste eingefuegt. Gehoert base nicht 
 * zur Liste, so wird ein ListException ausgeloest.
 * @param base Position in der Liste, vor der eingefuegt wird
 * @param object einzufuegendes Objekt
 * @return eingefuegtes ListItem
 * @see #insert(ListItem, ListItem)
 */
 public ListItem insert(ListItem base,Object object) {
  return insert(base,createNew(object));
 }
 /**
 * Fuegt das Teilarray von array beginnend mit startindex und Laenge length vor 
 * das ListItem base in die Liste ein. Ist base==null, so wird an das Ende
 * der Liste eingefuegt. Gehoert base nicht zur Liste, wird ein ListException
 * ausgeloest.
 * @param base Position in der Liste, vor der eingefuegt wird
 * @param array Array mit einzufuegenden Objektdaten
 * @param startindex Startindex im Array
 * @param length Laenge des Teilarrays
 * @return erstes eingefuegtes ListItem
 */
 public ListItem insert(ListItem base,Object[] array,int startindex,int length) {
  if ((base!=null) && (!contains(base)))
   throw new ListException(ListException.ILLEGAL_ACCESS);
  if (base!=null) base=base.prev(); else base=super.last();
  ListItem[] items=new ListItem[length];
  for (int i=0;i<length;i++) items[i]=createNew(array[startindex+i]);
  if (super.add(base,items,0,length)==length) {
   if (base!=null) return base.next(); else return super.first();
  } else throw new ListException(ListException.INSERTION_ERROR);
 }
 /**
 * Entfernt das ListItem item aus der Liste. Ist item null oder gehoert item
 * nicht zur Liste, wird ein ListException ausgeloest.
 * @param item zu entfernendes ListItem
 * @return entferntes ListItem
 */
 public ListItem remove(ListItem item) {
 	if (super.remove(item,null,1)!=1)
 		throw new ListException(ListException.ILLEGAL_ACCESS);
  return item;
 }
 /**
 * Entfernt das ListItem, das den Schluessel key enthaelt, aus der Liste.
 * Ist item null oder gehoert item nicht zur Liste, wird ein ListException 
 * ausgeloest.
 * @param key Schluesselobjekt
 * @return entferntes ListItem
 */
 public ListItem remove(Object key) {
 	return remove(find(key));
 }
 
 // Teillistenoperationen
 // =====================
 
 /**
 * Kopiert eine Kette aus der Liste. Der Bereich von start bis end wird kopiert.
 * Zurueckgegeben wird ein Zeiger auf das erste kopierte Element der Kette, also
 * auf die Kopie von start.
 * @param start erstes ListeItem der Kette
 * @param end letztes ListItem der Kette
 * @return erstes ListItem der kopierten Kette
 */
 public ListItem copy(ListItem start,ListItem end) {
  return super.getCopy(start,end,-1,false);
 }
 /**
 * Kopiert eine Kette aus der Liste. Der zu kopierende Bereich beginnt mit 
 * start und ist maximal count Schritte lang.
 * Zurueckgegeben wird ein Zeiger auf das erste kopierte Element der Kette, also
 * auf die Kopie von start.
 * @param start erstes ListeItem der Kette
 * @param count (max.) Anzahl der Elemente der Kette
 * @return erstes ListItem der kopierten Kette
 */
 public ListItem copy(ListItem start,int count) {
  return super.getCopy(start,null,count,false);
 }
 /**
 * Schneidet eine Kette aus der Liste. Der Bereich von start bis end wird
 * ausgeschnitten. Zurueckgegeben wird die Anzahl der ausgeschnittenen 
 * Elemente.
 * @param start erstes ListeItem der Kette
 * @param end letztes ListItem der Kette
 * @return Anzahl der ausgeschnittenen Elemente
 */
 public int cut(ListItem start,ListItem end) {
  return super.cut(start,end,-1);
 }
 /**
 * Schneidet eine Kette aus der Liste. Der Bereich beginnt mit start und
 * ist maximal count Schritte lang. Zurueckgegeben wird die Anzahl der 
 * ausgeschnittenen Elemente.
 * @param start erstes ListeItem der Kette
 * @param count (max.) Anzahl der Elemente der Kette
 * @return Anzahl der ausgeschnittenen Elemente
 */
 public int cut(ListItem start,int count) {
  return super.cut(start,null,count);
 }
 /**
 * Fuegt eine Kette vor dem ListItem base in die Liste ein. Der Bereich 
 * von start bis end wird eingefuegt. Kann die Kette nicht direkt eingefuegt
 * werden, dann wird eine Kopie angelegt und diese eingefuegt. Zurueckgegeben
 * wird die tatsaechliche Anzahl der eingefuegten ListItems.
 * Ist base==null, so wird an das Ende der Liste angefuegt.
 * @param base Stelle, vor der eingefuegt werden soll oder null, falls Anfuegen
 * @param start Beginn der einzufuegenden Kette
 * @param end Ende der einzufuegenden Kette
 * @return Anzahl eingefuegter Elemente
 */
 public int paste(ListItem base,ListItem start,ListItem end) {
  if (base!=null) base=base.prev(); else base=super.last();
  return super.add(base,start,end,-1);
 }
 /**
 * Fuegt eine Kette vor dem ListItem base in die Liste ein. Der Bereich 
 * beginnend mit start, der maximal count Elemente lang ist, wird eingefuegt. 
 * Kann die Kette nicht direkt eingefuegt werden, dann wird eine Kopie 
 * angelegt und diese eingefuegt. Zurueckgegeben wird die tatsaechliche Anzahl 
 * der eingefuegten ListItems.
 * Ist base==null, so wird an das Ende der Liste angefuegt.
 * @param base Stelle, vor der eingefuegt werden soll oder null, falls Anfuegen
 * @param start Beginn der einzufuegenden Kette
 * @param count maximale Laenge der Kette
 * @return Anzahl eingefuegter Elemente
 */
 public int paste(ListItem base,ListItem start,int count) {
  if (base!=null) base=base.prev(); else base=super.last();
  return super.add(base,start,null,count);
 }
 /**
 * Fuegt die Liste L an das Ende dieser Liste an. Die Liste L enthaelt nach
 * dieser Operation keine Elemente mehr.
 * @param L Quelliste
 * @return Anzahl angefuegter ListItems.
 */
 public int concat(BasicList L) {
  ListItem i=L.first();
  int c=0;
  if ((c=L.cut(i,null,-1))>0) {
   super.add(super.last(),i,null,-1);
  }
  return c;
 }
 
 // Zugriff auf die gesamte Liste
 // =============================

 /**
 * Konvertiert die Schluessel der Liste in ein Array.
 * Wurde SimpleList mit den Standard-Einfuegemethoden erstellt, dann liefern 
 * convertKeysToArray und convertValuesToArray dasselbe Array.
 * @see #convertValuesToArray
 * @return Objekt-Array
 */
 public Object[] convertKeysToArray() {
  if (super.length()<=0) return new Object[0];
  int len=super.length();
  Object[] items=new Object[len];
  storeInArray(items,0,len,first(),KEY,false);
  return items;
 }
 /**
 * Konvertiert die Werte der Liste in ein Array.
 * Wurde SimpleList mit den Standard-Einfuegemethoden erstellt, dann liefern 
 * convertKeysToArray und convertValuesToArray dasselbe Array.
 * @see #convertKeysToArray
 * @return Objekt-Array
 */
 public Object[] convertValuesToArray() {
  if (super.length()<=0) return new Object[0];
  int len=super.length();
  Object[] items=new Object[len];
  storeInArray(items,0,len,first(),VALUE,false);
  return items;
 }
 /**
 * Liefert ein Enumerator-Objekt, das alle Schluessel der Listenelemente aufzaehlt.
 * Wurde SimpleList mit den Standard-Einfuegemethoden erstellt, dann liefern 
 * keys() und values() dasselbe Enumeration-Objekt.
 * @return Enumeration mit Schluesseln
 * @see #values
 */
 public Enumeration keys() {
  return enumerate(null,null,-1,KEY);
 }
 /**
 * Liefert ein Enumerator-Objekt, das alle Werte der Listenelemente aufzaehlt.
 * Wurde SimpleList mit den Standard-Einfuegemethoden erstellt, dann liefern 
 * keys() und values() dasselbe Enumeration-Objekt.
 * @return Enumeration mit Werten
 * @see #keys
 */
 public Enumeration values() {
  return enumerate(null,null,-1,VALUE);
 }
 
 // Modifikationm der gesamten Liste
 // =========================================
 
 /**
 * Loescht die gesamte Liste.
 */
 public void clear() {
  super.clear();
 }
 /**
 * Dreht die Liste zyklisch so, dass item das erste Element der Liste ist.
 * Konstante Laufzeit.
 * @param item ListItem, das erstes Element werden soll
 */
 public void cycle(ListItem item) {
  super.cycle(item);
 }
 /**
 * Verdreht die Liste zyklisch um dist ListItems
 * @param dist Anzahl der zu drehenden ListItems
 */
 public void cycle(int dist) {
  super.cycle(dist);
 }
 /**
 * Kehrt die Reihenfolge der ListItems in der Liste um.
 */
 public void reverse() {
  super.reverse(null,null,-1);
 }
 /**
 * Sortiert die Liste entsprechend der durch das Comparitor-Objekt definierten
 * Ordnung aufsteigend oder absteigend (ja nach order). Ist comparitor==null,
 * so wird StdComparitor benutzt.
 * Das benutzte Comparitor-Objekt sollte in der Lage sein, KeyValueHolder
 * miteindander zu vergleichen.
 * @param comparitor Comparitor-Objekt zum Vergleichen
 * @param order Sortier-Ordnung, entweder Sorter.ASCENDING fuer aufsteigende oder
 * Sorter.DESCENDING fuer absteigende Sortierung
 */
 public void sort(Comparitor comparitor,short order) {
 	if (super.length()<2) return;
  if (comparitor==null) comparitor=new StdComparitor();
  Sorter sorter=new Sorter(comparitor);
  if (order!=Sorter.DESCENDING) sorter.setAscendingOrder(); else sorter.setDescendingOrder();
  ListItem i=super.first();
  int len=super.unconnect(i,null,-1);
 	ListItem[] items=new ListItem[len];
 	super.storeInArray(items,0,len,i,super.LISTITEM,false);
 	sorter.add(items);
 	sorter.sort();
 	Object[] obl=sorter.getArray();
 	for(int k=0;k<obl.length;k++) items[k]=(ListItem)obl[k];
 	super.reconnect(null,items,0,len);
 }
 /**
 * Sortiert die Liste entsprechend der durch das Comparitor-Objekt definierten
 * Ordnung aufsteigend. Ist comparitor==null, so wird StdComparitor benutzt.
 * @param comparitor Comparitor-Objekt zum Vergleichen
 */
 public void sort(Comparitor comparitor) {
 	sort(comparitor,Sorter.ASCENDING);
 }

 // Operationen fuer Zugriff der Liste als Stack bzw. Queue
 // ======================================================

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
 * Stack-peek - Wie top().
 * @return oberstes Element
 * @see #top
 */
 public Object peek() { return top(); }
 /**
 * Queue-peek - Wie firstValue().
 * @return erstes Element
 * @see #peek
 */
 public Object Peek() { return firstValue(); }
 
 /**
 * Legt ein Objekt auf dem Stack ab bzw. fuegt es an die Schlange an.
 * @param object zu speicherndes Objekt
 * @return ListItem, das object speichert
 */
 public ListItem push(Object object) {
  return add(object);
 }
 /**
 * Fuegt ein Objekt an den Anfang der Liste an.
 * @param object zu speicherndes Objekt
 * @return ListItem, das object speichert
 */
 public ListItem Push(Object object) {
  return insert(first(),object);
 }
 /**
 * Stack-Pop. Entnimmt das oberste (letzte) Element aus dem Stack und liefert es
 * zurueck.
 * @return oberstes Element
 */
 public Object pop() {
  if (super.length()<=0) throw new ListException(ListException.EMPTY_LIST);
  ListItem i=last();
  super.remove(i,null,1);
  return i.key();
 }
 /**
 * Queue-pop. Entnimmt das erste Element aus der Schlange und liefert es
 * zurueck.
 * @return erstes Element
 */
 public Object Pop() {
  if (super.length()<=0) throw new ListException(ListException.EMPTY_LIST);
  ListItem i=first();
  super.remove(i,null,1);
  return i.key();
 }
 
 // Abkuerzungen fuer Java-Basistypen
 // ===============================
 
 /**
 * Push fuer Java-Basistypen.
 * @param i zu pushender Int-Wert
 * @return ListItem, das den Wert speichert
 * @see #push
 */
 public ListItem push(int i) {
  return push(new Integer(i));
 }
 /**
 * Push fuer Java-Basistypen.
 * @param l zu pushender Long-Wert
 * @return ListItem, das den Wert speichert
 * @see #push
 */
 public ListItem push(long l) {
  return push(new Long(l));
 }
 /**
 * Push fuer Java-Basistypen.
 * @param f zu pushender Float-Wert
 * @return ListItem, das den Wert speichert
 * @see #push
 */
 public ListItem push(float f) {
  return push(new Float(f));
 }
 /**
 * Push fuer Java-Basistypen.
 * @param d zu pushender Double-Wert
 * @return ListItem, das den Wert speichert
 * @see #push
 */
 public ListItem push(double d) {
  return push(new Double(d));
 }
 /**
 * Push fuer Java-Basistypen.
 * @param b zu pushender Boolean-Wert
 * @return ListItem, das den Wert speichert
 * @see #push
 */
 public ListItem push(boolean b) {
  return push(new Boolean(b));
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
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das erste Objekt als Integer-Wert
 * @see #pop
 */
 public int PopInt() {
  if (peek() instanceof Number) return ((Number)pop()).intValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das erste Objekt als Long-Wert
 * @see #pop
 */
 public long PopLong() {
  if (peek() instanceof Number) return ((Number)pop()).longValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das erste Objekt als Float-Wert
 * @see #pop
 */
 public float PopFloat() {
  if (peek() instanceof Number) return ((Number)pop()).floatValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das erste Objekt als Double-Wert
 * @see #pop
 */
 public double PopDouble() {
  if (peek() instanceof Number) return ((Number)pop()).doubleValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * Pop fuer Java-Basistypen. Kann das Objekt auf dem Stack nicht in
 * den Basistyp umgewandelt werden, wird eine ListException ausgeloest.
 * @return das erste Objekt als Boolean-Wert
 * @see #pop
 */
 public boolean PopBoolean() {
  if (peek() instanceof Boolean) return ((Boolean)pop()).booleanValue();
  else throw new ListException(ListException.ILLEGAL_OBJECT_FORMAT);
 }
 /**
 * add fuer Java-Basistypen.
 * @param w anzufuegender Wert
 * @return das ListItem, das den Wert enthaelt
 * @see #add
 */
 public ListItem add(int w) {
  return add(new Integer(w));
 }
 /**
 * add fuer Java-Basistypen.
 * @param w anzufuegender Wert
 * @return das ListItem, das den Wert enthaelt
 * @see #add
 */
 public ListItem add(long w) {
  return add(new Long(w));
 }
 /**
 * add fuer Java-Basistypen.
 * @param w anzufuegender Wert
 * @return das ListItem, das den Wert enthaelt
 * @see #add
 */
 public ListItem add(float w) {
  return add(new Float(w));
 }
 /**
 * add fuer Java-Basistypen.
 * @param w anzufuegender Wert
 * @return das ListItem, das den Wert enthaelt
 * @see #add
 */
 public ListItem add(double w) {
  return add(new Double(w));
 }
 /**
 * add fuer Java-Basistypen.
 * @param w anzufuegender Wert
 * @return das ListItem, das den Wert enthaelt
 * @see #add
 */
 public ListItem add(boolean w) {
  return add(new Boolean(w));
 }
 /**
 * insert fuer Java-Basistypen.
 * @param base Position in der Liste, vor der eingefuegt wird
 * @param w einzufuegender Wert
 * @return das ListItem, das den Wert enthaelt
 * @see #insert(ListItem, Object)
 */
 public ListItem insert(ListItem base,int w) {
  return insert(base,new Integer(w));
 }
 /**
 * insert fuer Java-Basistypen.
 * @param base Position in der Liste, vor der eingefuegt wird
 * @param w einzufuegender Wert
 * @return das ListItem, das den Wert enthaelt
 * @see #insert(ListItem, Object)
 */
 public ListItem insert(ListItem base,long w) {
  return insert(base,new Long(w));
 }
 /**
 * insert fuer Java-Basistypen.
 * @param base Position in der Liste, vor der eingefuegt wird
 * @param w einzufuegender Wert
 * @return das ListItem, das den Wert enthaelt
 * @see #insert(ListItem, Object)
 */
 public ListItem insert(ListItem base,float w) {
  return insert(base,new Float(w));
 }
 /**
 * insert fuer Java-Basistypen.
 * @param base Position in der Liste, vor der eingefuegt wird
 * @param w einzufuegender Wert
 * @return das ListItem, das den Wert enthaelt
 * @see #insert(ListItem, Object)
 */
 public ListItem insert(ListItem base,double w) {
  return insert(base,new Double(w));
 }
 /**
 * insert fuer Java-Basistypen.
 * @param base Position in der Liste, vor der eingefuegt wird
 * @param w einzufuegender Wert
 * @return das ListItem, das den Wert enthaelt
 * @see #insert(ListItem, Object)
 */
 public ListItem insert(ListItem base,boolean w) {
  return insert(base,new Boolean(w));
 }


} // SimpleList
