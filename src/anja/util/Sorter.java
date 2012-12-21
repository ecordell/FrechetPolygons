package anja.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**<p>
 * Diese Klasse implementiert verschiedene Sortieralgorithmen. Um die
 * Objekte vergleichen zu koennen, wird ein Comparitor-Objekt benutzt.
 * Wird kein Comparitor gesetzt, so wird ein Standard-Comparitor (StdComparitor)
 * benutzt.
 * Um eine Menge sortieren zu koennen geht man folgendermassen vor:<p>
 * <ul><li> Erstellen eines neuen Sorter-Objektes und Zuweisen eines
 * Comparitor-Objektes zum vergleichen.<br>
 * <tt>Sorter S=new Sorter(new MyComparitor);</tt><br>
 * Nachtraeglich kann fuer ein bestehendes Sorter-Objekt auch noch ein
 * anderes Comparitor-Objekt zugewiesen werden mittels
 * <tt>setComparitor(Compariotor c)</tt>
 * <li> Eventuell festlegen der Sortierrichtung (aufsteigend mit
 * <tt>setAscendingOrder()</tt> / absteigend mit
 * <tt>setDescendingOrder()</tt>).
 * <li> Hinzufuegen von Objektreferenzen zum Sorter-Objekt mittels
 * <tt>add</tt>. <tt>add</tt> akzeptiert SimpleListen, Objekt-Arrays,
 * Intervalle in Objektarrays und SimpleListen sowie einzelne Objekte.
 * Zusaetzlich akzeptiert <tt>add</tt> noch jeden Java-Basistyp,
 * sowie alle Array-Typen.
 * <li> Durchfuehren der Sortierung mit der Standardsortierungsmethode
 * <tt>sort()</tt> oder mit den speziellen implementierten
 * Sortieralgorithmen <tt>BubbleSort()</tt>,<tt>QuickSort()</tt>
 * und <tt>HeapSort()</tt>. <tt>sort()</tt> verwendet fuer sehr kleine
 * Datenmengen das einfache Bubblesort und ansonsten das worst-case-effiziente
 * Heapsort.
 * <li> Abholen der sortierten Daten in einer Datenstruktur mittels einer
 * get-Funktion. <tt>getSimpleList()</tt> liefert eine SimpleListe zurueck,
 * <tt>getArray()</tt> ein Objektfeld, <tt>getIntArray()</tt> ein
 * Integer-Feld usw. </ul>
 * Anstatt die letzten drei Schritte einzeln auszufuehren, kann man auch eine
 * Sortierroutine mit einer Datenstruktur als Argument aufrufen, sowie
 * eventuell Begrenzungen eines Intervalls, falls noetig. Die Funktion gibt dann
 * eine Struktur desselben Typs mit sortierten Elementen zurueck. Bsp.:<br><pre>
 * Sorter S=new Sorter();
 * int[] array=new int[5000];
 * ... // Code, der array mit Zahlen fuellt...
 * int[] sortedArray=S.HeapSort(array,15,1500); // Sortiert den Bereich von array[15]-array[1500] mit Heapsort
 *   																			 // und speichert ihn in sortedArray
 * </pre>
 * <b>ACHTUNG!</b> Ein Sorter-Objekt ist keine Datenstruktur zum Speichern
 * von Objekten. Es sollten so wenig wie moeglich add-Methoden aufgerufen
 * werden, am besten nur einmal pro Sortiertvorgang.
 *
 * @see Comparitor
 * @see StdComparitor
 *
 * @version 1.4
 * @author		Thomas Wolf
 */
public class Sorter extends Object {
    /** Der gesetzte Vergleicher. */
    protected Comparitor _comparitor;
    /** Aufsteigend sortieren. */
    public static final byte ASCENDING = 1;
    /** Absteigend sortieren. */
    public static final byte DESCENDING = 2;
    /** Sortierreihenfolge. */
    private byte _order = ASCENDING;
    /** Feld mit den zu sortierenden Objekten. */
    private Object[] _data;
    /** Gibt an, ob Feld sortiert ist. */
    private boolean _sorted;

    // Konstruktoren
    // =============

    /**
     * Konstruktor, der einen Comparitor setzt bzw. einen Standard-Comparitor
     * erzeugt, falls c==null ist.
     * @param c Comparitor fuer die Vergleiche
     */
    public Sorter(Comparitor c) {
        clear();
        if (c == null) {
            c = new StdComparitor();
        }
        _comparitor = c;
        _order = ASCENDING;
    }

    /**
     * Standard-Konstruktor.
     */
    public Sorter() {
        this(null);
    }

    /**
     * Ueberschreibt Object.toString().
     * @see java.lang.Object#toString
     */
    public String toString() {
        String s = "order=";
        if (_order == ASCENDING) {
            s += "ASCENDING";
        }
        if (_order == DESCENDING) {
            s += "DESCENDING";
        }
        if (_sorted) {
            s += "/SORTED,length=";
        } else {
            s += ",length=";
        }
        s += _data.length + ",data={";
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < _data.length; i++) {
            buff.append(_data[i]);
            if (i < _data.length - 1) {
                buff.append(",");
            }
        }
        return getClass().getName() + "[" + s + buff.toString() + "}]";
    }

    // Routinen zum Vorbereiten des Sortierens
    // =======================================

    /**
     * Loescht alle im Sorter gespeicherten Objektreferenzen.
     */
    public void clear() {
        _data = new Object[0];
        _sorted = true;
    }

    /**
     * Setzt ein Comparitor-Objekt.
     * @param c neues Comparitor-Objekt
     */
    public void setComparitor(Comparitor c) {
        if (c != null) {
            if (c != _comparitor) {
                _sorted = false;
            }
            _comparitor = c;
        }
    }

    /**
     * Liefert das aktuelle Comparitor-Objekt.
     * @return aktuelles Comparitor-Objekt
     */
    public Comparitor getComparitor() {
        return _comparitor;
    }

    /**
     * Setzt aufsteigende Sortierreihenfolge.
     */
    public synchronized void setAscendingOrder() {
        if (_order != ASCENDING) {
            _sorted = false;
        }
        _order = ASCENDING;
    }

    /**
     * Setzt absteigende Sortierreihenfolge.
     */
    public synchronized void setDescendingOrder() {
        if (_order != DESCENDING) {
            _sorted = false;
        }
        _order = DESCENDING;
    }

    // Add-Routinen zum Hinzufuegen von Objekten, die sortiert werden sollen
    // ====================================================================

    /**
     * Liefert die Anzahl der zum Sortieren gespeicherten Elemente.
     * @return Anzahl der Objekte
     */
    public int length() {
        return _data.length;
    }

    /**
     * Liefert true zurueck, falls schon sortiert wurde.
     * @return true, falls sortiert
     */
    public boolean isSorted() {
        return _sorted;
    }

    /**
     * Setzt das zu sortierende Objektfeld direkt auf array!
     * @param array Objektfeld
     */
    protected synchronized void setArray(Object[] array) {
        _data = array;
        _sorted = false;
    }

    /**
     * Vergroessert das Array _data um len Plaetze. Der Rueckgabewert ist die erste Position, an der _data belegt
     * werden kann.
     */
    private int makeItBigger(int count) {
        Object[] a = new Object[_data.length + count];
        int pos = _data.length;
        try {
            if (_data.length > 0) {
                System.arraycopy(_data, 0, a, 0, _data.length);
            }
            _data = a;
        } catch (ArrayIndexOutOfBoundsException e1) {} catch (
                ArrayStoreException e2) {}
        _sorted = false; // Ich setze das hier, auch wenn es eigentlich bei jeder add-Routine stehen muesste, aber
                         // schliesslich ruft ja jede add-Routine diese Funktion auf - es ist also dasselbe.
        return pos;
    }

    public synchronized void add(Object o) {
        int pos =makeItBigger(1);
        _data[pos] = o;
    }

    public synchronized void add(int i) {
        _data[makeItBigger(1)] = new Integer(i);
    }

    public synchronized void add(long l) {
        _data[makeItBigger(1)] = new Long(l);
    }

    public synchronized void add(float f) {
        _data[makeItBigger(1)] = new Float(f);
    }

    public synchronized void add(double d) {
       int pos=makeItBigger(1);
        _data[pos] = new Double(d);
    }

    public synchronized void add(char c) {
        _data[makeItBigger(1)] = new Character(c);
    }

    public synchronized void add(boolean b) {
        _data[makeItBigger(1)] = new Boolean(b);
    }

    /**
     * Fuegt das Intervall des uebergebenen Objekt-Arrays hinzu.
     * @param o Objektfeld
     * @param l untere Feldgrenze
     * @param r obere Feldgrenze
     */
    public synchronized void add(Object[] o, int l, int r) {
        if (o == null) {
            return;
        }
        int pos = makeItBigger(r - l + 1);
        try {
            if (r - l + 1 > 0) {
                System.arraycopy(o, l, _data, pos, r - l + 1);
            }
        } catch (ArrayIndexOutOfBoundsException e1) {} catch (
                ArrayStoreException e2) {}
    }

    /**
     * Setzt das zu sortierende Feld auf das uebergebene Objektfeld.
     * @param o Objektfeld
     */
    public synchronized void add(Object[] o) {
        if (o == null) {
            return;
        }
        add(o, 0, o.length - 1);
    }

    public synchronized void add(int[] o, int l, int r) {
        if (o == null) {
            return;
        }
        int pos = makeItBigger(r - l + 1);
        for (int i = 0; i < r - l + 1; i++) {
            _data[pos + i] = new Integer(o[l + i]);
        }
    }

    public synchronized void add(int[] o) {
        if (o == null) {
            return;
        }
        add(o, 0, o.length - 1);
    }

    public synchronized void add(long[] o, int l, int r) {
        if (o == null) {
            return;
        }
        int pos = makeItBigger(r - l + 1);
        for (int i = 0; i < r - l + 1; i++) {
            _data[pos + i] = new Long(o[l + i]);
        }
    }

    public synchronized void add(long[] o) {
        if (o == null) {
            return;
        }
        add(o, 0, o.length - 1);
    }

    public synchronized void add(float[] o, int l, int r) {
        if (o == null) {
            return;
        }
        int pos = makeItBigger(r - l + 1);
        for (int i = 0; i < r - l + 1; i++) {
            _data[pos + i] = new Float(o[l + i]);
        }
    }

    public synchronized void add(float[] o) {
        if (o == null) {
            return;
        }
        add(o, 0, o.length - 1);
    }

    public synchronized void add(double[] o, int l, int r) {
        if (o == null) {
            return;
        }
        int pos = makeItBigger(r - l + 1);
        for (int i = 0; i < r - l + 1; i++) {
            _data[pos + i] = new Double(o[l + i]);
        }
    }

    public synchronized void add(double[] o) {
        if (o == null) {
            return;
        }
        add(o, 0, o.length - 1);
    }

    public synchronized void add(char[] o, int l, int r) {
        if (o == null) {
            return;
        }
        int pos = makeItBigger(r - l + 1);
        for (int i = 0; i < r - l + 1; i++) {
            _data[pos + i] = new Character(o[l + i]);
        }
    }

    public synchronized void add(char[] o) {
        if (o == null) {
            return;
        }
        add(o, 0, o.length - 1);
    }

    public synchronized void add(boolean[] o, int l, int r) {
        if (o == null) {
            return;
        }
        int pos = makeItBigger(r - l + 1);
        for (int i = 0; i < r - l + 1; i++) {
            _data[pos + i] = new Boolean(o[l + i]);
        }
    }

    public synchronized void add(boolean[] o) {
        if (o == null) {
            return;
        }
        add(o, 0, o.length - 1);
    }

    /**
     * Fuegt den Inhalt einer Liste (List,SimpleList,Queue,Stack) an den Sorter an.
     * @param L Listenobjekt
     */
    public synchronized void add(BasicList L) {
        if ((L == null) || (L.length() <= 0)) {
            return;
        }
        int pos = makeItBigger(L.length());
        L.storeInArray(_data, pos, L.length(), null, BasicList.LISTITEM, false);
    }

    /**
     * Fuegt den Inhalt der Enumeration enumers an den Sorter an.
     * @param enumers Enumeration-Objekt
     */
    public synchronized void add(Enumeration enumers) {
        SimpleList L = new SimpleList();
        while (enumers.hasMoreElements()) {
            L.add(enumers.nextElement());
        }
        if (L.empty()) {
            return;
        }
        int pos = makeItBigger(L.length());
        L.storeInArray(_data, pos, L.length(), null, BasicList.KEY, false);
    }

    // Methoden zum Abholen der sortierten Daten
    // =========================================

    public Object[] getArray() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        Object o[] = new Object[_data.length];
        try {
            if (_data.length > 0) {
                System.arraycopy(_data, 0, o, 0, _data.length);
            }
        } catch (ArrayIndexOutOfBoundsException e1) {} catch (
                ArrayStoreException e2) {}
        return o;
    }

    /**
     * Liefert das unsortierte Objektfeld. Werden Modifikationen an dem Feld
     * vorgenommen, so hat das Auswirkungen auf die Sortierung!
     * @return Array
     */
    protected Object[] getUnsortedArray() {
        return _data;
    }

    public int[] getIntArray() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        int[] ia = new int[_data.length];
        for (int i = 0; i < _data.length; i++) {
            if ((_data[i] != null) && (_data[i] instanceof Number)) {
                ia[i] = ((Number) _data[i]).intValue();
            }
        }
        return ia;
    }

    public long[] getLongArray() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        long[] ia = new long[_data.length];
        for (int i = 0; i < _data.length; i++) {
            if ((_data[i] != null) && (_data[i] instanceof Number)) {
                ia[i] = ((Number) _data[i]).longValue();
            }
        }
        return ia;
    }

    public float[] getFloatArray() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        float[] ia = new float[_data.length];
        for (int i = 0; i < _data.length; i++) {
            if ((_data[i] != null) && (_data[i] instanceof Number)) {
                ia[i] = ((Number) _data[i]).floatValue();
            }
        }
        return ia;
    }

    public double[] getDoubleArray() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        double[] ia = new double[_data.length];
        for (int i = 0; i < _data.length; i++) {
            if ((_data[i] != null) && (_data[i] instanceof Number)) {
                ia[i] = ((Number) _data[i]).doubleValue();
            }
        }
        return ia;
    }

    public boolean[] getBooleanArray() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        boolean[] ia = new boolean[_data.length];
        for (int i = 0; i < _data.length; i++) {
            if ((_data[i] != null) && (_data[i] instanceof Boolean)) {
                ia[i] = ((Boolean) _data[i]).booleanValue();
            }
        }
        return ia;
    }

    public char[] getCharArray() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        char[] ia = new char[_data.length];
        for (int i = 0; i < _data.length; i++) {
            if ((_data[i] != null) && (_data[i] instanceof Character)) {
                ia[i] = ((Character) _data[i]).charValue();
            }
        }
        return ia;
    }

    /**
     * Speichert den Inhalt des Sorters (sortiert oder nicht!) in der Liste L ab.
     * @param L Liste, in der gespeichert werden soll
     */
    public void storeInList(BasicList L) {
        for (int i = 0; i < _data.length; i++) {
            if (_data[i] instanceof ListItem) {
                L.add(L.last(), (ListItem) _data[i], null, 1);
            } else {
                L.add(L.last(), new SimpleListItem(_data[i]), null, 1);
            }
        }
    }

    public void storeInDList(DList L) {
         for (int i = 0; i < _data.length; i++) {
             L.append(_data[i]);
         }
    }


    /**
     * Liefert den sortierten Inhalt in einer SimpleList zurueck.
     * @return SimpleList mit sortierten Objekten
     */
    public SimpleList getSimpleList() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        SimpleList L = new SimpleList();
        storeInList(L);
        return L;
    }

    /**
     * Liefert den sortierten Inhalt in einer List zurueck.
     * @return List mit sortierten Objekten
     */
    public List getList() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        List L = new List();
        storeInList(L);
        return L;
    }

    /**
     * Liefert den sortierten Inhalt in einer List zurueck.
     * @return List mit sortierten Objekten
     */
    public DList getDList() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        DList L = new DList();
        storeInDList(L);
        return L;
    }


    /**
     * Liefert den sortierten Inhalt in einer Queue zurueck.
     * @return Queue mit sortierten Objekten
     */
    public List getQueue() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        List L = new List();
        storeInList(L);
        return L;
    }

    /**
     * Liefert den sortierten Inhalt in einem Stack zurueck.
     * @return Stack mit sortierten Objekten
     */
    public List getStack() {
        if (!_sorted) {
            sort();
        }
        if (_data == null) {
            return null;
        }
        List L = new List();
        storeInList(L);
        return L;
    }

    /**
     * Liefert eine Enumeration mit den sortierten Sorter-Objekten.
     * @return Enumeration
     */
    public Enumeration getEnumeration() {
        return new SortEnumerator(getArray());
    }

    // Die verschiedenen Sortieralgorithmen
    // ====================================

    private void swap(int i, int j) {
        Object o = _data[i];
        _data[i] = _data[j];
        _data[j] = o;
    }

    /**
     * Standard-Sortierverfahren. Bei weniger als 10 Elementen wird BubbleSort gestartet, ansonsten Heapsort.
     */
    public synchronized void sort() {
        if (_sorted) {
            return;
        }
        if (_data.length < 10) {
            BubbleSort();
        } else {
            HeapSort();
        }
    }

    /**
     * Bubble-Sort. Laufzeit O(n^2). Abbruch falls keine Vertauschungen mehr notwendig.
     */
    public synchronized void BubbleSort() {
        if (_sorted) {
            return;
        }
        int i, j;
        boolean changed = true;
        short condition;
        if (_order == ASCENDING) {
            condition = Comparitor.BIGGER;
        } else {
            condition = Comparitor.SMALLER;
        }
        for (i = _data.length - 1; ((i > 0) && changed); i--) {
            changed = false;
            for (j = 0; j < i; j++) {
                if (_comparitor.compare(_data[j], _data[j + 1]) == condition) {
                    swap(j, j + 1);
                    changed = true;
                }
            }
        }
        _sorted = true;
    }

    // Ab hier Heapsort
    private void sift(int i, int m, short condition) {
        int j;
        i++;
        m++; // Korrektur, Feld muss mit 1 beginnen!
        while (2 * i <= m) {
            j = 2 * i;
            if ((j < m) &&
                (_comparitor.compare(_data[j - 1], _data[j]) == condition)) {
                j++;
            }
            if (_comparitor.compare(_data[i - 1], _data[j - 1]) == condition) {
                swap(i - 1, j - 1);
                i = j;
            } else {
                i = m;
            }
        }
    }

    /**
     * Heapsort. Laufzeit O(log(n)*n). */
    public synchronized void HeapSort() {
        if (_sorted) {
            return;
        }
        int i;
        short condition;
        if (_order == ASCENDING) {
            condition = Comparitor.SMALLER;
        } else {
            condition = Comparitor.BIGGER;
        }
        for (i = _data.length / 2; i > 0; i--) {
            sift(i - 1, _data.length - 1, condition);
        }
        for (i = _data.length - 1; i > 0; i--) {
            swap(0, i);
            sift(0, i - 1, condition);
        }
        _sorted = true;
    }

    // Ab hier Quicksort
    private void qsort(int l, int r, short condition) {
        int i, j;
        boolean done = false;
        if (r <= l) {
            return;
        }
        i = l - 1;
        j = r;
        Object v = _data[r];
        //short comp;
        while (!(done)) {
            i++;
            while ((i <= r) && (_comparitor.compare(_data[i], v) == condition)) {
                i++;
            }
            j--;
            while ((j >= l) && (_comparitor.compare(v, _data[j]) == condition)) {
                j--;
            }
            if (i >= j) {
                done = true;
            } else {
                swap(i, j);
            }
        }
        swap(i, r);
        qsort(l, i - 1, condition);
        qsort(i + 1, r, condition);
    }

    /**
     * Quicksort.
     */
    public synchronized void QuickSort() {
        if (_sorted) {
            return;
        }
        short condition;
        if (_order == ASCENDING) {
            condition = Comparitor.SMALLER;
        } else {
            condition = Comparitor.BIGGER;
        }
        if (_data.length > 1) {
            qsort(0, _data.length - 1, condition);
        }
        _sorted = true;
    }

    // Suchroutinen
    // ============

    /**
     * Binaere Suche auf einem sortierten(!) Array array im Intervall von l bis r nach dem Objekt o.
     * @param array sortiertes Feld, in dem gesucht werden soll
     * @param l linke Grenze des Intervalles
     * @param r rechte Grenze des Intervalles
     * @param o zu suchendes Objekt
     * @return Position des Objektes o im Feld array; -1, falls o nicht enthalten
     */
    public int BinSearch(Object[] array, int l, int r, Object o) {
        int m = (l + r) / 2;
        if (m == l) {
            return -1;
        }
        short comp = _comparitor.compare(o, array[m]);
        if (comp == Comparitor.SMALLER) {
            return BinSearch(array, l, m, o);
        }
        if (comp == Comparitor.BIGGER) {
            return BinSearch(array, m, r, o);
        }
        return m;
    }

    /**
     * Wie BinSearch(Object[],int,int,Object), nur erfolgt die Suche hier auf dem sortierten Feld
     * des Sorter-Objektes.
     */
    public int BinSearch(int l, int r, Object o) {
        if (_data.length <= 0) {
            return -1;
        }
        if (!_sorted) {
            sort();
        }
        return BinSearch(_data, l, r, o);
    }

    /**
     * Wie BinSearch(int,int,Object), nur ohne Intervallgrenzen; im gesamten Feld wird gesucht.
     */
    public int BinSearch(Object o) {
        if (_data.length <= 0) {
            return -1;
        }
        if (!_sorted) {
            sort();
        }
        return BinSearch(_data, 0, _data.length - 1, o);
    }

    // Abkuerzungen zum schnellen Sortieren
    // ===================================

    public Object[] BubbleSort(Object[] o) {
        clear();
        add(o);
        BubbleSort();
        return getArray();
    }

    public Object[] BubbleSort(Object[] o, int l, int r) {
        clear();
        add(o, l, r);
        BubbleSort();
        return getArray();
    }

    public int[] BubbleSort(int[] o) {
        clear();
        add(o);
        BubbleSort();
        return getIntArray();
    }

    public int[] BubbleSort(int[] o, int l, int r) {
        clear();
        add(o, l, r);
        BubbleSort();
        return getIntArray();
    }

    public long[] BubbleSort(long[] o) {
        clear();
        add(o);
        BubbleSort();
        return getLongArray();
    }

    public long[] BubbleSort(long[] o, int l, int r) {
        clear();
        add(o, l, r);
        BubbleSort();
        return getLongArray();
    }

    public float[] BubbleSort(float[] o) {
        clear();
        add(o);
        BubbleSort();
        return getFloatArray();
    }

    public float[] BubbleSort(float[] o, int l, int r) {
        clear();
        add(o, l, r);
        BubbleSort();
        return getFloatArray();
    }

    public double[] BubbleSort(double[] o) {
        clear();
        add(o);
        BubbleSort();
        return getDoubleArray();
    }

    public double[] BubbleSort(double[] o, int l, int r) {
        clear();
        add(o, l, r);
        BubbleSort();
        return getDoubleArray();
    }

    public char[] BubbleSort(char[] o) {
        clear();
        add(o);
        BubbleSort();
        return getCharArray();
    }

    public char[] BubbleSort(char[] o, int l, int r) {
        clear();
        add(o, l, r);
        BubbleSort();
        return getCharArray();
    }

    public List BubbleSort(BasicList L) {
        clear();
        add(L);
        BubbleSort();
        return getList();
    }

    public Enumeration BubbleSort(Enumeration enumers) {
        clear();
        add(enumers);
        BubbleSort();
        return getEnumeration();
    }

    public Object[] HeapSort(Object[] o) {
        clear();
        add(o);
        HeapSort();
        return getArray();
    }

    public Object[] HeapSort(Object[] o, int l, int r) {
        clear();
        add(o, l, r);
        HeapSort();
        return getArray();
    }

    public int[] HeapSort(int[] o) {
        clear();
        add(o);
        HeapSort();
        return getIntArray();
    }

    public int[] HeapSort(int[] o, int l, int r) {
        clear();
        add(o, l, r);
        HeapSort();
        return getIntArray();
    }

    public long[] HeapSort(long[] o) {
        clear();
        add(o);
        HeapSort();
        return getLongArray();
    }

    public long[] HeapSort(long[] o, int l, int r) {
        clear();
        add(o, l, r);
        HeapSort();
        return getLongArray();
    }

    public float[] HeapSort(float[] o) {
        clear();
        add(o);
        HeapSort();
        return getFloatArray();
    }

    public float[] HeapSort(float[] o, int l, int r) {
        clear();
        add(o, l, r);
        HeapSort();
        return getFloatArray();
    }

    public double[] HeapSort(double[] o) {
        clear();
        add(o);
        HeapSort();
        return getDoubleArray();
    }

    public double[] HeapSort(double[] o, int l, int r) {
        clear();
        add(o, l, r);
        HeapSort();
        return getDoubleArray();
    }

    public char[] HeapSort(char[] o) {
        clear();
        add(o);
        HeapSort();
        return getCharArray();
    }

    public char[] HeapSort(char[] o, int l, int r) {
        clear();
        add(o, l, r);
        HeapSort();
        return getCharArray();
    }

    public List HeapSort(BasicList L) {
        clear();
        add(L);
        HeapSort();
        return getList();
    }

    public Enumeration HeapSort(Enumeration enumers) {
        clear();
        add(enumers);
        HeapSort();
        return getEnumeration();
    }

    public Object[] QuickSort(Object[] o) {
        clear();
        add(o);
        QuickSort();
        return getArray();
    }

    public Object[] QuickSort(Object[] o, int l, int r) {
        clear();
        add(o, l, r);
        QuickSort();
        return getArray();
    }

    public int[] QuickSort(int[] o) {
        clear();
        add(o);
        QuickSort();
        return getIntArray();
    }

    public int[] QuickSort(int[] o, int l, int r) {
        clear();
        add(o, l, r);
        QuickSort();
        return getIntArray();
    }

    public long[] QuickSort(long[] o) {
        clear();
        add(o);
        QuickSort();
        return getLongArray();
    }

    public long[] QuickSort(long[] o, int l, int r) {
        clear();
        add(o, l, r);
        QuickSort();
        return getLongArray();
    }

    public float[] QuickSort(float[] o) {
        clear();
        add(o);
        QuickSort();
        return getFloatArray();
    }

    public float[] QuickSort(float[] o, int l, int r) {
        clear();
        add(o, l, r);
        QuickSort();
        return getFloatArray();
    }

    public double[] QuickSort(double[] o) {
        clear();
        add(o);
        QuickSort();
        return getDoubleArray();
    }

    public double[] QuickSort(double[] o, int l, int r) {
        clear();
        add(o, l, r);
        QuickSort();
        return getDoubleArray();
    }

    public char[] QuickSort(char[] o) {
        clear();
        add(o);
        QuickSort();
        return getCharArray();
    }

    public char[] QuickSort(char[] o, int l, int r) {
        clear();
        add(o, l, r);
        QuickSort();
        return getCharArray();
    }

    public List QuickSort(BasicList L) {
        clear();
        add(L);
        QuickSort();
        return getList();
    }

    public Enumeration QuickSort(Enumeration enumers) {
        clear();
        add(enumers);
        QuickSort();
        return getEnumeration();
    }

    public Object[] sort(Object[] o) {
        clear();
        add(o);
        sort();
        return getArray();
    }

    public Object[] sort(Object[] o, int l, int r) {
        clear();
        add(o, l, r);
        sort();
        return getArray();
    }

    public int[] sort(int[] o) {
        clear();
        add(o);
        sort();
        return getIntArray();
    }

    public int[] sort(int[] o, int l, int r) {
        clear();
        add(o, l, r);
        sort();
        return getIntArray();
    }

    public long[] sort(long[] o) {
        clear();
        add(o);
        sort();
        return getLongArray();
    }

    public long[] sort(long[] o, int l, int r) {
        clear();
        add(o, l, r);
        sort();
        return getLongArray();
    }

    public float[] sort(float[] o) {
        clear();
        add(o);
        sort();
        return getFloatArray();
    }

    public float[] sort(float[] o, int l, int r) {
        clear();
        add(o, l, r);
        sort();
        return getFloatArray();
    }

    public double[] sort(double[] o) {
        clear();
        add(o);
        sort();
        return getDoubleArray();
    }

    public double[] sort(double[] o, int l, int r) {
        clear();
        add(o, l, r);
        sort();
        return getDoubleArray();
    }

    public char[] sort(char[] o) {
        clear();
        add(o);
        sort();
        return getCharArray();
    }

    public char[] sort(char[] o, int l, int r) {
        clear();
        add(o, l, r);
        sort();
        return getCharArray();
    }

    public List sort(BasicList L) {
        clear();
        add(L);
        sort();
        return getList();
    }

    public Enumeration sort(Enumeration enumers) {
        clear();
        add(enumers);
        sort();
        return getEnumeration();
    }
}


class SortEnumerator implements Enumeration {
    /** Array mit Objekten */
    Object[] _objects;
    /** Index */
    int _index;
    /** Konstruktor */
    SortEnumerator(Object[] objects) {
        _objects = objects;
        _index = 0;
    }

    /** Liefert true, falls noch ein Element vorhanden. */
    public boolean hasMoreElements() {
        return (_index < _objects.length);
    }

    /** Liefert das naechste Element in der Struktur. */
    public Object nextElement() {
        if (_index >= _objects.length) {
            throw new NoSuchElementException();
        }
        return _objects[_index++];
    }
}
