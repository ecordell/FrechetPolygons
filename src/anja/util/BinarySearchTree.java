package anja.util;


import java.util.Enumeration;


//import java.util.NoSuchElementException;

/**
 * <p> Ein binärer Suchbaum basierend auf BasicTree. Zur Gewährleistung der
 * Ordnungseigenschaft benötigt ein Binärbaum ein Comparitor-Objekt bei der
 * Konstruktion. Im Baum sollten nur Objekte gespeichert werden, die der
 * Comparitor vergleichen kann, ansonsten tritt (irgendwann einmal) ein
 * CompareException auf. Wird der leere Konstruktor benutzt, so wird ein
 * StdComparitor-Objekt konstruiert und benutzt.
 * 
 * @author Thomas Wolf
 * @version 1.1
 */
public class BinarySearchTree
		extends BasicTree
		implements Cloneable
{

	/** Comparitor für die Ordnung der Baumknoten. */
	private Comparitor	_comparitor			= null;
	/** Falls true, werden Duplikate der Schlüsselwerte erlaubt. */
	private boolean		_allow_duplicates	= true;
	/** Falls true, werden im linken Teilbaum kleinere Schlüssel gespeichert. */
	private boolean		_left_is_smaller	= true;


	// Konstruktion
	// ============

	/**
	 * Leerer Konstruktor. Zum Vergleich der Baumelemente werden die Hash-Werte
	 * der Objekte benutzt.
	 */
	public BinarySearchTree()
	{
		super();
		_comparitor = new StdComparitor();
	}


	/**
	 * Standard-Konstruktor. Uebergeben wird ein Comparitor-Objekt, das den
	 * Vergleich der Baumknoten ausführt.
	 * @param comparitor
	 *            Comparitor-Objekt zum Vergleichen von Schlüsseln
	 */
	public BinarySearchTree(
			Comparitor comparitor)
	{
		this(comparitor, true);
	}


	/**
	 * Konstruiert einen Binärbaum mit dem Comparitor comparitor und der Angabe
	 * ob kleinere Schlüssel in linken oder rechten Teilbäumen gespeichert
	 * werden.
	 * @param comparitor
	 *            Comparitor-Objekt zum Vergleichen von Schlüsseln
	 * @param left_is_smaller
	 *            falls true, werden kleinere Schlüssel im linken Teilbaum
	 *            gespeichert, ansonsten im rechten
	 */
	public BinarySearchTree(
			Comparitor comparitor,
			boolean left_is_smaller)
	{
		super();
		_comparitor = comparitor;
		if (_comparitor == null)
			_comparitor = new HashComparitor();
		_left_is_smaller = left_is_smaller;
	}


	// Allgemeine Methoden
	// ===================

	/**
	 * Liefert das Comparitor-Objekt, das zum Vergleichen der Knoten im Baum
	 * benutzt wird.
	 * @return Comparitor zum Vergleichen der Knoten
	 */
	public Comparitor comparitor()
	{
		return _comparitor;
	}


	/**
	 * Liefert true, falls der Baum das Anfügen von doppelten Schlüsselwerten
	 * erlaubt.
	 * @return true, falls doppelte Schlüssel erlaubt sind
	 */
	public boolean allowDuplicates()
	{
		return _allow_duplicates;
	}


	/**
	 * Erlaubt oder verbietet das Anfügen doppelter Schlüsselwerte. Die
	 * Einstellung hat keine Wirkung auf den bestehenden Baum, sondern wirkt
	 * erst beim Anfügen neuer Objekte.
	 * @param allow
	 *            true, falls Duplikate erlaubt werden sollen, sonst false
	 */
	public void setAllowDuplicates(
			boolean allow)
	{
		_allow_duplicates = allow;
	}


	/**
	 * Liefert true, falls in linken Teilbäumen kleinere (oder gleiche)
	 * Schlüsselwerte gespeichert werden, ansonsten false.
	 * @return true, falls links kleinere Schlüssel gespeichert werden
	 */
	public boolean leftIsSmaller()
	{
		return _left_is_smaller;
	}


	// Zugriff
	// =======

	/**
	 * Liefert die Anzahl der im Baum gespeicherten Elemente.
	 * @return Anzahl der Elemente
	 */
	public int length()
	{
		return super.length();
	}


	/**
	 * Berechnet die Anzahl der im Teilbaum mit Wurzel item gespeicherten
	 * TreeItems.
	 * @param item
	 *            Wurzel des zu zählenden Teilbaumes
	 * @return Anzahl der TreeItems im Teilbaum
	 */
	public int length(
			TreeItem item)
	{
		if (item == null)
			return super.length();
		return super.countItems(item);
	}


	/**
	 * Liefert true, falls der Baum leer ist.
	 * @return true, falls Baum leer ist
	 */
	public boolean empty()
	{
		return super.empty();
	}


	/**
	 * Liefert eine Referenz auf die Wurzel des Baumes zurück.
	 * @return Wurzel
	 */
	public TreeItem root()
	{
		return super.root();
	}


	/**
	 * Berechnet die Höhe des Baumes.
	 * @return Höhe des Baumes
	 */
	public int height()
	{
		return super.height(null);
	}


	/**
	 * Berechnet die Höhe des Teilbaumes mit der Wurzel root.
	 * @param root
	 *            Wurzel des zu vermessenden Teilbaumes
	 * @return Höhe des Teilbaumes
	 */
	public int height(
			TreeItem root)
	{
		return super.height(root);
	}


	/**
	 * Liefert true, falls der TreeItem node im Baum enthalten ist. Der Test
	 * erfolgt in konstanter Zeit.
	 * @param node
	 *            zu testender Knoten
	 * @return true, falls node im Baum enthalten ist.
	 */
	public boolean contains(
			TreeItem node)
	{
		return super.contains(node);
	}


	// Suchen
	// ======

	/**
	 * Liefert das TreeItem, an das der Schlüssel key angefügt werden müßte -
	 * hätte man das vor.
	 * @param key
	 *            gesuchter Schlüssel
	 * @return TreeItem im Baum, an das key angefügt werden müßte
	 */
	public TreeItem findPath(
			Object key)
	{
		return super.find(key, null, _comparitor, _left_is_smaller);
	}


	/**
	 * Sucht ein TreeItem, für das equals(key) true zurückliefert. Die Suche
	 * erfolgt im worst-case in O(n), bei balancierten Bäumen in O(log(n))
	 * Schritten. start gibt die Wurzel des zu durchsuchenden Teilbaumes an. Ist
	 * start==null, wird der gesamte Baum durchsucht. Da zum Suchen der
	 * Comparitor benutzt wird, ist es an dieser Stelle ganz wichtig, daß der
	 * Comparitor und equals() kompatibel sind (also
	 * comparitor.compare(key,i)==EQUALS, wenn i.equals(key)!).
	 * @param key
	 *            gesuchter Schlüssel
	 * @param start
	 *            Knoten, in dessen Teilbaum gesucht wird
	 * @return gefundenes TreeItem oder null, falls nicht enthalten
	 */
	public TreeItem find(
			Object key,
			TreeItem start)
	{
		TreeItem i = super.find(key, start, _comparitor, _left_is_smaller);
		if ((i != null) && (i.equals(key)))
			return i;
		else
			return null;
	}


	/**
	 * Sucht ein TreeItem, für das equals(key) true zurückliefert. Die Suche
	 * erfolgt im worst-case in O(n), bei balancierten Bäumen in O(log(n))
	 * Schritten.
	 * @param key
	 *            gesuchter Schlüssel
	 * @return gefundenes TreeItem oder null, falls nicht enthalten
	 */
	public TreeItem find(
			Object key)
	{
		if ((key instanceof TreeItem) && (contains((TreeItem) key)))
			return (TreeItem) key;
		TreeItem i = super.find(key, null, _comparitor, _left_is_smaller);
		if ((i != null) && (_comparitor.compare(key, i) == Comparitor.EQUAL))
			return i;
		else
			return null;
	}


	/**
	 * Sucht im Baum das TreeItem, das das Kleinste unter denen ist, die größer
	 * als key sind. start gibt die Wurzel des zu durchsuchenden Teilbaumes an.
	 * Ist start==null, wird der gesamte Baum durchsucht.
	 * @param key
	 *            Vergleichsobjekt
	 * @param start
	 *            Wurzel des Teilbaumes, in dem gesucht wird
	 * @return kleinstes TreeItem im Baum unter denen, die noch größer(gleich)
	 *         key sind
	 */
	public TreeItem findBigger(
			Object key,
			TreeItem start)
	{
		TreeItem i = super.find(key, start, _comparitor, _left_is_smaller);
		short comp = _comparitor.compare(key, i);
		if (comp != Comparitor.BIGGER)
			return i;
		if (_left_is_smaller)
			return next(start, i, LEFT_ROOT_RIGHT_ORDER);
		else
			return prev(start, i, LEFT_ROOT_RIGHT_ORDER);
	}


	/**
	 * Sucht im Baum das TreeItem, das das Kleinste unter denen ist, die größer
	 * als key sind.
	 * @param key
	 *            Vergleichsobjekt
	 * @return kleinstes TreeItem im Baum unter denen, die noch größer(gleich)
	 *         key sind
	 */
	public TreeItem findBigger(
			Object key)
	{
		return findBigger(key, null);
	}


	/**
	 * Sucht im Baum das TreeItem, das das Größte unter denen ist, die kleiner
	 * als key sind. start gibt die Wurzel des zu durchsuchenden Teilbaumes an.
	 * Ist start==null, wird der gesamte Baum durchsucht.
	 * @param key
	 *            Vergleichsobjekt
	 * @param start
	 *            Wurzel des Teilbaumes, in dem gesucht wird
	 * @return größtes TreeItem im Baum unter denen, die noch kleiner(gleich)
	 *         key sind
	 */
	public TreeItem findSmaller(
			Object key,
			TreeItem start)
	{
		TreeItem i = super.find(key, start, _comparitor, _left_is_smaller);
		short comp = _comparitor.compare(key, i);
		if (comp != Comparitor.SMALLER)
			return i;
		if (_left_is_smaller)
			return prev(start, i, LEFT_ROOT_RIGHT_ORDER);
		else
			return next(start, i, LEFT_ROOT_RIGHT_ORDER);
	}


	/**
	 * Sucht im Baum das TreeItem, das das Größte unter denen ist, die kleiner
	 * als key sind.
	 * @param key
	 *            Vergleichsobjekt
	 * @return größtes TreeItem im Baum unter denen, die noch kleiner(gleich)
	 *         key sind
	 */
	public TreeItem findSmaller(
			Object key)
	{
		return findSmaller(key, null);
	}


	/**
	 * Sucht das TreeItem mit dem Minimum im Teilbaum mit Wurzel start. Ist
	 * start==null wird der gesamte Baum durchsucht.
	 * @param start
	 *            Wurzel des zu durchsuchenden Teilbaumes
	 * @return TreeItem mit dem Minimum im Teilbaum
	 */
	public TreeItem min(
			TreeItem start)
	{
		if (start == null)
			start = super.root();
		if (start == null)
			return null;
		if (_left_is_smaller)
			return super.findLast(start, TreeItem.LEFT);
		else
			return super.findLast(start, TreeItem.RIGHT);
	}


	/**
	 * Sucht das TreeItem mit dem Minimum im Baum.
	 * @return TreeItem mit dem Minimum im Baum
	 */
	public TreeItem min()
	{
		return min(null);
	}


	/**
	 * Sucht das TreeItem mit dem Maximum im Teilbaum mit Wurzel start. Ist
	 * start==null wird der gesamte Baum durchsucht.
	 * @param start
	 *            Wurzel des zu durchsuchenden Teilbaumes
	 * @return TreeItem mit dem Maximum im Teilbaum
	 */
	public TreeItem max(
			TreeItem start)
	{
		if (start == null)
			start = super.root();
		if (start == null)
			return null;
		if (_left_is_smaller)
			return super.findLast(start, TreeItem.RIGHT);
		else
			return super.findLast(start, TreeItem.LEFT);
	}


	/**
	 * Sucht das TreeItem mit dem Maximum im Baum.
	 * @return TreeItem mit dem Maximum im Baum
	 */
	public TreeItem max()
	{
		return max(null);
	}


	// Tree-Traversals
	// ===============

	/**
	 * Liefert das erste TreeItem im Baum bezüglich der Traversal-Ordnung order.
	 * @param order
	 *            eine Traversal-Konstante (ROOT_CHILDS_ORDER,
	 *            CHILDS_ROOT_ORDER, LEFT_ROOT_RIGHT_ORDER,
	 *            LEFT_RIGHT_ROOT_ORDER oder ROOT_LEFT_RIGHT_ORDER)
	 * @return erstes TreeItem bezüglich order
	 */
	public TreeItem first(
			byte order)
	{
		return super.first(null, order);
	}


	/**
	 * Liefert das erste TreeItem im Baum bezüglich der Traversal-Ordnung
	 * Links-Wurzel-Rechts (symmetrische Ordnung).
	 * @return erstes TreeItem bezüglich der symmetrischen Ordnung
	 */
	public TreeItem first()
	{
		return super.first(null, LEFT_ROOT_RIGHT_ORDER);
	}


	/**
	 * Liefert das letzte TreeItem im Baum bezüglich der Traversal-Ordnung
	 * order.
	 * @param order
	 *            eine Traversal-Konstante (ROOT_CHILDS_ORDER,
	 *            CHILDS_ROOT_ORDER, LEFT_ROOT_RIGHT_ORDER,
	 *            LEFT_RIGHT_ROOT_ORDER oder ROOT_LEFT_RIGHT_ORDER)
	 * @return letztes TreeItem bezüglich order
	 */
	public TreeItem last(
			byte order)
	{
		return super.last(null, order);
	}


	/**
	 * Liefert das letzte TreeItem im Baum bezüglich der Traversal-Ordnung
	 * Links-Wurzel-Rechts (symmetrische Ordnung).
	 * @return letztes TreeItem bezüglich der symmetrischen Ordnung
	 */
	public TreeItem last()
	{
		return super.last(null, LEFT_ROOT_RIGHT_ORDER);
	}


	/**
	 * Liefert das nachfolgende TreeItem im Baum bezüglich der Traversal-Ordnung
	 * order.
	 * @param i
	 *            TreeItem, dessen Nachfolger gesucht wird
	 * @param order
	 *            eine Traversal-Konstante (ROOT_CHILDS_ORDER,
	 *            CHILDS_ROOT_ORDER, LEFT_ROOT_RIGHT_ORDER,
	 *            LEFT_RIGHT_ROOT_ORDER oder ROOT_LEFT_RIGHT_ORDER)
	 * @return Nachfolger bezüglich order
	 */
	public TreeItem next(
			TreeItem i,
			byte order)
	{
		return super.next(null, i, order);
	}


	/**
	 * Liefert das nachfolgende TreeItem im Baum bezüglich der Traversal-Ordnung
	 * Links-Wurzel-Rechts (symmetrische Ordnung).
	 * @param i
	 *            TreeItem, dessen Nachfolger gesucht wird
	 * @return Nachfolger bezüglich der symmetrischen Ordnung
	 */
	public TreeItem next(
			TreeItem i)
	{
		return super.next(null, i, LEFT_ROOT_RIGHT_ORDER);
	}


	/**
	 * Liefert das vorhergehende TreeItem im Baum bezüglich der
	 * Traversal-Ordnung order.
	 * @param i
	 *            TreeItem, dessen Vorgänger gesucht wird
	 * @param order
	 *            eine Traversal-Konstante (ROOT_CHILDS_ORDER,
	 *            CHILDS_ROOT_ORDER, LEFT_ROOT_RIGHT_ORDER,
	 *            LEFT_RIGHT_ROOT_ORDER oder ROOT_LEFT_RIGHT_ORDER)
	 * @return Vorgänger bezüglich order
	 */
	public TreeItem prev(
			TreeItem i,
			byte order)
	{
		return super.prev(null, i, order);
	}


	/**
	 * Liefert das vorhergehende TreeItem im Baum bezüglich der
	 * Traversal-Ordnung Links-Wurzel-Rechts (symmetrische Ordnung).
	 * @param i
	 *            TreeItem, dessen Vorgänger gesucht wird
	 * @return Nachfolger bezüglich der symmetrischen Ordnung
	 */
	public TreeItem prev(
			TreeItem i)
	{
		return super.prev(null, i, LEFT_ROOT_RIGHT_ORDER);
	}


	// Zugriff auf den gesamten Baum
	// =============================

	/**
	 * Liefert ein Enumerator-Objekt, das alle Schlüssel im Baum aufzählt.
	 * @return Enumeration mit Schlüsseln
	 * @see #values
	 */
	public Enumeration keys()
	{
		return super.enumerate(null, LEFT_ROOT_RIGHT_ORDER, true, KEY);
	}


	/**
	 * Liefert ein Enumerator-Objekt, das alle Werte im Baum aufzählt.
	 * @return Enumeration mit Werten
	 * @see #keys
	 */
	public Enumeration values()
	{
		return super.enumerate(null, LEFT_ROOT_RIGHT_ORDER, true, VALUE);
	}


	// Zugriff auf Intervalle
	// ======================

	/**
	 * Liefert ein Enumeration-Objekt, das alle Schlüssel im Baum aufzählt, die
	 * größergleich key1 und kleinergleich key2 sind. Die Aufzählung folgt
	 * Links-Wurzel-Rechts-Ordnung, also bei leftIsSmaller() in aufsteigender
	 * ansonsten in absteigender Schlüsselreihenfolge
	 * @param key1
	 *            Schlüssel, der die untere Grenze des Intervalles bestimmt oder
	 *            null, falls die Aufzählung mit dem kleinsten Schlüssel
	 *            beginnen soll
	 * @param key2
	 *            Schlüssel, der die obere Grenze des Intervalles bestimmt oder
	 *            null, falls die Aufzählung mit dem größten Schlüssel enden
	 *            soll
	 * @return Enumeration-Objekt, das das Intervall aufzählt
	 */
	public Enumeration keys(
			Object key1,
			Object key2)
	{
		TreeItem i = findBigger(key1), j = findSmaller(key2);
		if (_left_is_smaller)
		{
			if (_comparitor.compare(i, j) == Comparitor.BIGGER)
				return super.emptyEnumeration();
			return super.enumerate(i, j, LEFT_ROOT_RIGHT_ORDER, KEY);
		}
		else
		{
			if (_comparitor.compare(i, j) == Comparitor.SMALLER)
				return super.emptyEnumeration();
			return super.enumerate(j, i, LEFT_ROOT_RIGHT_ORDER, KEY);
		}
	}


	/**
	 * Liefert ein Enumeration-Objekt, das alle Werte im Baum aufzählt, deren
	 * Schlüsel größergleich key1 und kleinergleich key2 sind. Die Aufzählung
	 * folgt Links-Wurzel-Rechts-Ordnung, also bei leftIsSmaller() in
	 * aufsteigender ansonsten in absteigender Schlüsselreihenfolge
	 * @param key1
	 *            Schlüssel, der die untere Grenze des Intervalles bestimmt oder
	 *            null, falls die Aufzählung mit dem Wert des kleinsten
	 *            Schlüssels beginnen soll
	 * @param key2
	 *            Schlüssel, der die obere Grenze des Intervalles bestimmt oder
	 *            null, falls die Aufzählung mit dem Wert des größten Schlüssels
	 *            enden soll
	 * @return Enumeration-Objekt, das das Intervall aufzählt
	 */
	public Enumeration values(
			Object key1,
			Object key2)
	{
		TreeItem i = findBigger(key1), j = findSmaller(key2);
		if (_left_is_smaller)
		{
			if (_comparitor.compare(i, j) == Comparitor.BIGGER)
				return super.emptyEnumeration();
			return super.enumerate(i, j, LEFT_ROOT_RIGHT_ORDER, VALUE);
		}
		else
		{
			if (_comparitor.compare(i, j) == Comparitor.SMALLER)
				return super.emptyEnumeration();
			return super.enumerate(j, i, LEFT_ROOT_RIGHT_ORDER, VALUE);
		}
	}


	/**
	 * Liefert ein Enumeration-Objekt, das alle TreeItems im Baum aufzählt,
	 * deren Schlüsel größergleich key1 und kleinergleich key2 sind. Die
	 * Aufzählung folgt Links-Wurzel-Rechts-Ordnung, also bei leftIsSmaller() in
	 * aufsteigender ansonsten in absteigender Schlüsselreihenfolge
	 * @param key1
	 *            Schlüssel, der die untere Grenze des Intervalles bestimmt oder
	 *            null, falls die Aufzählung mit dem TreeItem des kleinsten
	 *            Schlüssels beginnen soll
	 * @param key2
	 *            Schlüssel, der die obere Grenze des Intervalles bestimmt oder
	 *            null, falls die Aufzählung mit dem TreeItem des größten
	 *            Schlüssels enden soll
	 * @return Enumeration-Objekt, das das Intervall aufzählt
	 */
	public Enumeration treeItems(
			Object key1,
			Object key2)
	{
		TreeItem i = findBigger(key1), j = findSmaller(key2);
		if (_left_is_smaller)
		{
			if (_comparitor.compare(i, j) == Comparitor.BIGGER)
				return super.emptyEnumeration();
			return super.enumerate(i, j, LEFT_ROOT_RIGHT_ORDER, TREEITEM);
		}
		else
		{
			if (_comparitor.compare(i, j) == Comparitor.SMALLER)
				return super.emptyEnumeration();
			return super.enumerate(j, i, LEFT_ROOT_RIGHT_ORDER, TREEITEM);
		}
	}


	// Baum-Ereignisse
	// ===============

	/**
	 * Wird aufgerufen, <i>bevor</i> ein TreeItem an den Boum angefügt wird.
	 * @param item
	 *            anzufügendes TreeItem
	 */
	public void beforeAdd(
			TreeItem item)
	{}


	/**
	 * Wird aufgerufen, <i>nachdem</i> ein TreeItem an den Boum angefügt wurde.
	 * @param item
	 *            angefügtes TreeItem
	 */
	public void afterAdd(
			TreeItem item)
	{}


	/**
	 * Wird aufgerufen, <i>bevor</i> ein TreeItem aus dem Baum entfernt wird.
	 * @param item
	 *            zu löschendes TreeItem
	 */
	public void beforeRemove(
			TreeItem item)
	{}


	/**
	 * Wird aufgerufen, <i>nachdem</i> ein TreeItem aus dem Baum entfernt wird.
	 * @param item
	 *            entferntes TreeItem
	 */
	public void afterRemove(
			TreeItem item)
	{}


	/**
	 * Eine Linksrotation wurde durchgeführt und zwar wird der Knoten y auf die
	 * Position des Knotens x nach links gedreht:<pre> x y T1 y => x T3 T2 T3 T1
	 * T2</pre> Das Ereignis wird aufgerufen, <i>nachdem</i> die Rotation
	 * stattfand.
	 * @param x
	 *            und
	 * @param y
	 *            vertauschte Knoten
	 */
	public void onLeftRotate(
			TreeItem x,
			TreeItem y)
	{}


	/**
	 * Eine Rechtsrotation wurde durchgeführt und zwar wird der Knoten x auf die
	 * Position des Knotens y nach rechts gedreht:<pre> y x x T3 => T1 y T1 T2
	 * T2 T3</pre> Das Ereignis wird aufgerufen, <i>nachdem</i> die Rotation
	 * stattfand.
	 * @param x
	 *            und
	 * @param y
	 *            vertauschte Knoten
	 */
	public void onRightRotate(
			TreeItem x,
			TreeItem y)
	{}


	/**
	 * Eine Doppelrotation wurde durchgeführt. Die TreeItems u,v und w sind wie
	 * folgt (bzw. spiegelverkehrt) angeordnet:<pre> u w v T3 => v u T1 w T1 T2
	 * T3 T4 T2 T2</pre> Das Ereignis wird aufgerufen, <i>nachdem</i> die
	 * Doppelrotation stattfand.
	 * @param u
	 *            ,
	 * @param v
	 *            und
	 * @param w
	 *            vertauschte Knoten
	 */
	public void onDoubleRotation(
			TreeItem u,
			TreeItem v,
			TreeItem w)
	{}


	// Rotationen mit Ereignissen
	// ==========================

	/**
	 * Überschreibt rotation von BasicTree.rotation um Baumereignisse
	 * einzubinden.
	 * @see BasicTree#rotation
	 */
	protected synchronized TreeItem rotation(
			TreeItem u,
			int pos1,
			int pos2)
	{
		TreeItem v = u.child(pos1), w;
		if ((v == null) || (pos1 == pos2) || (pos1 > 1) || (pos2 > 1))
			return u;
		w = super.rotation(u, pos1, pos2);
		if (pos2 == TreeItem.LEFT)
			onLeftRotate(u, v);
		else
			onRightRotate(v, u);
		return w;
	}


	/**
	 * Überschreibt rotation von BasicTree.double_rotation um Baumereignisse
	 * einzubinden.
	 * @see BasicTree#double_rotation
	 */
	public synchronized TreeItem double_rotation(
			TreeItem u,
			int pos1,
			int pos2)
	{
		TreeItem w = super.double_rotation(u, pos1, pos2);
		if ((u == null) || (w == u) || (w == null))
			return w;
		onDoubleRotation(w.child(pos1), w.child(pos2), w);
		return w;
	}


	// Hinzufügen und Löschen einzelner Elemente
	// =========================================

	/**
	 * Erzeugt ein neues TreeItem, das den Wert O speichert.
	 * @param O
	 *            zu speichernder Wert
	 * @return neues TreeItem mit O
	 */
	private TreeItem createNew(
			Object O)
	{
		return new SimpleTreeItem(2, O);
	}


	/**
	 * Erzeugt ein neues TreeItem, das den Schlüsel key und den Wert value
	 * speichert.
	 * @param key
	 *            zu speichernder Schlüssel
	 * @param value
	 *            zu speichernder Wert
	 * @return neues TreeItem mit (key,value)
	 */
	private TreeItem createNew(
			Object key,
			Object value)
	{
		return new StdTreeItem(2, key, value);
	}


	/**
	 * Fügt das TreeItem item letztendlich an. Diese Methode wird von allen
	 * add-Methoden benutzt und sollte überschrieben werden, wenn man z.B.
	 * balancierte Bäume schreiben will.
	 * @param item
	 *            neues TreeItem
	 * @return eingefügtes TreeItem
	 */
	protected TreeItem add_item(
			TreeItem item)
	{
		beforeAdd(item);
		item = super
				.add(item, _comparitor, _left_is_smaller, _allow_duplicates);
		afterAdd(item);
		return item;
	}


	/**
	 * Fügt das Objekt key an die passende Stelle im binären Suchbaum ein. Dabei
	 * wird ein TreeItem generiert, das den Schlüssel key enthält. Der Schlüssel
	 * key sollte von dem Comparitor des Baumes mit den anderen Schlüsseln im
	 * Baum vergleichbar sein, sonst kommt es (früher oder später) zu einem
	 * CompareException.
	 * @param key
	 *            neuer Schlüssel
	 * @return TreeItem, das den Schlüssel enthält
	 */
	public TreeItem add(
			Object key)
	{
		return add_item(createNew(key));
	}


	/**
	 * Fügt das (key,value)-Paar an die passende Stelle im binären Suchbaum ein.
	 * Dabei wird ein TreeItem generiert, das den Schlüssel key und den
	 * Objektwert value enthält. Der Schlüssel key sollte von dem Comparitor des
	 * Baumes mit den anderen Schlüsseln im Baum vergleichbar sein, sonst kommt
	 * es (früher oder später) zu einem CompareException.
	 * @param key
	 *            neuer Schlüssel
	 * @param value
	 *            neuer Objektwert
	 * @return TreeItem, das den Schlüssel enthält
	 */
	public TreeItem add(
			Object key,
			Object value)
	{
		return add_item(createNew(key, value));
	}


	/**
	 * Fügt das TreeItem item an die richtige Stelle im binären Suchbaum ein. Es
	 * wird nur <strong>ein</strong> TreeItem eingefügt! sollten mit item noch
	 * mehrere TreeItems verknüpft sein, so werden alle Verknüpfungen zu ihnen
	 * einseitig gelöscht (bzw. eine Kopie von item angelegt). Zum Einfügen von
	 * Teilbäumen paste benutzen!
	 * @param item
	 *            neues TreeItem
	 * @return eingefügtes TreeItem
	 */
	public TreeItem add(
			TreeItem item)
	{
		if (item == null)
			return null;
		// Besitzer löschen
		if (!item.setOwningTree(null))
		{
			item = (TreeItem) item.clone();
			if (!item.setOwningTree(null))
				throw new TreeException(item, TreeException.SET_OWNER);
		}
		// Verknüpfungen löschen
		if (!item.clearConnections())
			throw new TreeException(item, TreeException.REMOVE);
		// Rang setzen
		if (!item.setMaxRank(2))
			throw new TreeException(item, TreeException.SET_RANK);
		// Anfügen
		return add_item(item);
	}


	/**
	 * Entfernt das TreeItem item aus dem Baum und gibt eine Referenz auf das
	 * entfernte TreeItem zurück (oder null, falls es nicht entfernt werden
	 * konnte).
	 * @param item
	 *            zu entfernendes TreeItem
	 * @return entferntes TreeItem
	 */
	public synchronized TreeItem remove(
			TreeItem item)
	{
		beforeRemove(item);
		item = super.removeSym(item);
		afterRemove(item);
		return item;
	}


	/**
	 * Entfernt das TreeItem mit dem Schlüssel key und gibt eine Referenz auf
	 * das entfernte TreeItem zurück oder null, falls keines mit dem angegebenen
	 * Schlüssel gefunden wurde.
	 * @param key
	 *            gesuchter Schlüssel
	 * @return entferntes TreeItem
	 */
	public TreeItem remove(
			Object key)
	{
		TreeItem item = super.find(key, null, _comparitor, _left_is_smaller);
		if ((item != null) && (item.equals(key)))
			return remove(item);
		else
			return null;
	}


	// Teilbaumoperationen
	// ===================

	/**
	 * Löscht den gesamten Baum.
	 */
	public void clear()
	{
		removeTree(root());
	}


	// Abkürzungen für Java-Basistypen
	// ===============================

	/**
	 * add für Java-Basistyp.
	 * @param n
	 *            Java-Basistyp zum Einfügen
	 * @return TreeItem, das den ObjektWrapper mit dem Basistyp enthält
	 */
	public TreeItem add(
			int n)
	{
		return add(new Integer(n));
	}


	/**
	 * add für Java-Basistyp.
	 * @param n
	 *            Java-Basistyp zum Einfügen
	 * @return TreeItem, das den ObjektWrapper mit dem Basistyp enthält
	 */
	public TreeItem add(
			long n)
	{
		return add(new Long(n));
	}


	/**
	 * add für Java-Basistyp.
	 * @param n
	 *            Java-Basistyp zum Einfügen
	 * @return TreeItem, das den ObjektWrapper mit dem Basistyp enthält
	 */
	public TreeItem add(
			float n)
	{
		return add(new Float(n));
	}


	/**
	 * add für Java-Basistyp.
	 * @param n
	 *            Java-Basistyp zum Einfügen
	 * @return TreeItem, das den ObjektWrapper mit dem Basistyp enthält
	 */
	public TreeItem add(
			double n)
	{
		return add(new Double(n));
	}


	/**
	 * add für Java-Basistyp.
	 * @param n
	 *            Java-Basistyp zum Einfügen
	 * @return TreeItem, das den ObjektWrapper mit dem Basistyp enthält
	 */
	public TreeItem add(
			boolean n)
	{
		return add(new Boolean(n));
	}


	// Methoden für das Interface Owner
	// ================================

	/**
	 * Das Owned-Objekt who bittet den Owner um Genehmigung einer Aktion vom Typ
	 * accesstype mit dem Argument argument. Diese Methode überschreibt
	 * requestAccess von BasicTree um noch einige Restriktionen einzubringen
	 * (key() darf nicht geändert werden und maxRank() ebenfalls nicht).
	 * @param accesstype
	 *            Typ der Aktion (des Zugriffs)
	 * @param who
	 *            Objekt, welches den Zugriff erbittet
	 * @param argument
	 *            Argument der Aktion
	 * @return true, falls der Owner die Aktion erlaubt, sonst false
	 */
	public boolean requestAccess(
			int accesstype,
			Object who,
			Object argument)
	{
		boolean answer = super.requestAccess(accesstype, who, argument);
		if (!answer)
			return false;
		if (_access == ANY_ACCESS)
			return true;
		switch (accesstype)
		{
			case TreeItem.SET_KEY:
			case TreeItem.SET_MAX_RANK:
				return false;
		}
		return true;
	}
}
