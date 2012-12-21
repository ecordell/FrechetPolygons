package anja.util;


import java.util.Enumeration;
import java.util.NoSuchElementException;


// Diese Klasse sollte eigentlich weiter hinten als Innerclass stehen...
/**
 * Enumeration-Objekt zum Aufzählen aller Baum-Elemente.
 */
class TreeEnumerator
		implements Enumeration
{

	/** Wurzel des Teilbaumes, der durchlaufen wird. */
	private TreeItem	_t_root;
	/** Cursor-TreeItem. */
	private TreeItem	_i, _end;
	/** Durchlaufordnung der Elemente. */
	private byte		_order;
	/** Falls true, wird Reihenfolge vorwärts durchlaufen, sonst rückwärts. */
	private boolean		_forward;
	/** Objekte die zurückgegeben werden sollen. */
	private byte		_objecttype;
	/** Zeiger auf Baum. */
	private BasicTree	_tree;


	/**
	 * Leerer Konstruktor. Enthält keine Elemente.
	 */
	TreeEnumerator()
	{
		_i = null;
	}


	/**
	 * Konstruktor zum Aufzählen kompletter Teilbäume in Durchlaufordnung order
	 * in Richtung forward mit Rückgabe von Objekten des Types objecttype.
	 */
	TreeEnumerator(
			BasicTree tree,
			TreeItem root,
			byte order,
			boolean forward,
			byte objecttype)
	{
		_tree = tree;
		_order = order;
		_objecttype = objecttype;
		_forward = forward;
		_t_root = root;
		if (_forward)
			_i = _tree.first(root, order);
		else
			_i = _tree.last(root, order);
		_end = null;
	}


	/**
	 * Konstruktor zum Aufzählen von Intervallen von start bis end in
	 * Durchlaufordnung order mit Rückgabe von Objekten des Types objecttype.
	 */
	TreeEnumerator(
			BasicTree tree,
			TreeItem start,
			TreeItem end,
			byte order,
			byte objecttype)
	{
		_tree = tree;
		_order = order;
		_objecttype = objecttype;
		_forward = true;
		_t_root = null;
		if (start != null)
			_i = start;
		else
			_i = _tree.first(null, order);
		_end = end;
	}


	/** Liefert true, falls noch ein Element vorhanden. */
	public boolean hasMoreElements()
	{
		return (_i != null);
	}


	/** Liefert das nächste Element in der Struktur. */
	public Object nextElement()
	{
		if (_i == null)
			throw new NoSuchElementException();
		Object O = null;
		switch (_objecttype)
		{
			case BasicTree.KEY:
				O = _i.key();
				break;
			case BasicTree.VALUE:
				O = _i.value();
				break;
			case BasicTree.TREEITEM:
				O = _i;
				break;
		}
		if (_i == _end)
			_i = null;
		else
		{
			if (_forward)
				_i = _tree.next(_t_root, _i, _order);
			else
				_i = _tree.prev(_t_root, _i, _order);
		}
		return O;
	}
}

/**
 * <p> Grundlegende Baum-Methoden. Dieses Objekt sollte Grundlage für weitere
 * Baumklassen sein. Gespeichert wird in BasicTree ledeglich eine Refernz auf
 * die Wurzel und die Anzahl der gespeicherten Elemente.
 * 
 * @author Thomas Wolf
 * @version 1.0
 */
public class BasicTree
		implements Owner, java.io.Serializable
{

	// Zugriffskonstanten für allowAccess
	/**
	 * Erlaubt keinerlei Zugriff (Standard). Keinen Zugriff bezieht sich hier
	 * ersteinmal auf Zugriffe der Verkettung.
	 */
	protected final static byte	NO_ACCESS				= 0;
	/** Erlaube beliebigen Zugriff. */
	protected final static byte	ANY_ACCESS				= 1;

	// Konstanten für Objektzugriff
	/** Zugriff auf das TreeItem-Objekt. */
	protected final static byte	TREEITEM				= 0;
	/** Zugriff auf den Schlüssel. */
	protected final static byte	KEY						= 1;
	/** Zugriff auf den Wert. */
	protected final static byte	VALUE					= 2;

	// Konstanten für Tree-Traversals
	/** Zuerst alle Kinder, dann die Wurzel. */
	public final static byte	CHILDS_ROOT_ORDER		= 1;
	/** Zuerst die Wurzel, dann die Kinder. */
	public final static byte	ROOT_CHILDS_ORDER		= 2;
	/** Links-Wurzel-Rechts (Funktioniert nur bei Binärbäumen richtig). */
	public final static byte	LEFT_ROOT_RIGHT_ORDER	= 3;
	/** Links-Rechts-Wurzel (Funktioniert nur bei Binärbäumen richtig). */
	public final static byte	LEFT_RIGHT_ROOT_ORDER	= 4;
	/** Wurzel-Links-Rechts (Funktioniert nur bei Binärbäumen richtig). */
	public final static byte	ROOT_LEFT_RIGHT_ORDER	= 5;

	/** Die Wurzel. */
	private TreeItem			_root					= null;
	/** Anzahl der gespeicherten Elemente. */
	private int					_length					= 0;
	/** Variable um den Zugriff von Knoten temporär zu gestatten. */
	protected byte				_access					= NO_ACCESS;


	// Konstruktion
	// ============

	/**
	 * Geschützter Konstruktor.
	 */
	protected BasicTree()
	{
		_root = null;
		_length = 0;
		_access = NO_ACCESS;
	}


	// Allgemeine Methoden
	// ===================

	/**
	 * @param item2string
	 *            falls true, Umwandeln der TreeItems in Strings; ansonsten
	 *            werden nur die keys bzw key-value-Paare in Strings umgewandelt
	 * @param preline
	 *            Beginn jeder Zeile
	 * @param sepl
	 *            String für senkrechte Linie (zum nächsten Sohn)
	 * @param sepsp
	 *            String für Zwischenraum (kein nächster Sohn)
	 * @param cmul
	 *            Abzweiger für Sohn (nicht der letzte)
	 * @param csing
	 *            Abzweiger für den letzten Sohn
	 */
	protected String getTreeString(
			boolean item2string,
			String preline,
			String sepl,
			String sepsp,
			String cmul,
			String csing)
	{
		if (_length <= 0)
			return "";
		String[] lines = new String[_length];
		boolean[] ch = new boolean[_length];
		int l = 0, lev = 0, pos;
		TreeItem i = _root, j = null;
		while (i != null)
		{
			lines[l] = "";
			for (int ll = 0; ll < lev - 1; ll++)
				if (ch[ll])
					lines[l] += sepl;
				else
					lines[l] += sepsp;
			if (lev > 0)
				if (ch[lev - 1])
					lines[l] += cmul;
				else
					lines[l] += csing;
			if (item2string)
				lines[l] += i;
			else
			{
				if (i.value() != i.key())
					lines[l] += "(" + i.key() + "," + i.value() + ")";
				else
					lines[l] += i.key();
				//if (i.balance()==0) lines[l]+=" [red]"; else lines[l]+=" [black]";
			}
			do
			{
				pos = i.nextChildPos(i.pos(j) + 1); // Liefert UNKNOWN, wenn durch... (nextChild liefert für PARENT 0!)
				j = i;
				if (pos == TreeItem.UNKNOWN)
				{
					i = i.parent();
					lev--;
				}
				else
				{
					ch[lev] = (i.nextChildPos(pos + 1) != TreeItem.UNKNOWN);
					i = i.child(pos);
					lev++;
				}
			}
			while ((i != null) && (pos == TreeItem.UNKNOWN));
			l++;
		}
		StringBuffer st = new StringBuffer();
		st.append("\n");
		for (l = 0; l < _length; l++)
		{
			st.append(preline);
			st.append(lines[l]);
			st.append("\n");
		}
		return st.toString();
	}


	/**
	 * Überschreibt Object.toString().
	 * @see java.lang.Object#toString
	 */
	public String toString()
	{
		return getClass().getName() + "[length=" + _length + ",{"
				+ getTreeString(false, "   ", "|  ", "   ", "|- ", "^- ")
				+ "}]";
	}


	// Zugriff
	// =======

	/**
	 * Liefert die Anzahl der im Baum gespeicherten Elemente.
	 * @return Anzahl der Elemente
	 */
	protected int length()
	{
		return _length;
	}


	/**
	 * Liefert true, falls der Baum leer ist.
	 * @return true, falls Baum leer ist
	 */
	protected boolean empty()
	{
		return _length <= 0;
	}


	/**
	 * Liefert true, falls der TreeItem node im Baum enthalten ist. Der Test
	 * erfolgt in konstanter Zeit.
	 * @param node
	 *            zu testender Knoten
	 * @return true, falls node im Baum enthalten ist.
	 */
	protected boolean contains(
			TreeItem node)
	{
		return ((node != null) && (node.getOwningTree() == this));
	}


	/**
	 * Liefert eine Referenz auf die Wurzel des Baumes zurück.
	 * @return Wurzel
	 */
	protected TreeItem root()
	{
		return _root;
	}


	/**
	 * Berechnet die Höhe des Teilbaumes mit der Wurzel root. Die Berechnung
	 * benötigt lineare Zeit!
	 * @param root
	 *            Wurzel des zu vermessenden Teilbaumes
	 * @return Höhe des Teilbaumes
	 */
	protected int height(
			TreeItem root)
	{
		if (root == null)
			root = _root;
		if (root == null)
			return 0;
		TreeItem i = root, j;
		int lev = 0, height = 0;
		while (i != null)
		{
			if (lev > height)
				height = lev;
			if (i.isLeaf())
			{
				do
				{
					j = i;
					lev--;
					if (i != root)
						i = i.parent();
					else
						i = null;
				}
				while ((i != null)
						&& (i.nextChildPos(i.pos(j) + 1) == TreeItem.UNKNOWN));
				if (i != null)
				{
					i = i.child(i.nextChildPos(i.pos(j) + 1));
					lev++;
				}
			}
			else
			{
				i = i.child(i.nextChildPos(0));
				lev++;
			}
		}
		return height + 1;
	}


	/**
	 * Liefert das letzte Element in der Kette
	 * root-root.child(pos)-root.child(pos).child(pos)... usw. Ist root==null,
	 * wird root auf die Wurzel des Baumes gesetzt. Diese Funktion liefert
	 * vermutlich auch korrekte Ergebnisse, wenn root Element eines anderen
	 * Baumes ist (dies sollte trotzdem vermieden werden).
	 * @param root
	 *            Wurzel des zu untersuchenden Teilbaumes
	 * @return am weitesten links liegendes TreeItem im Teilbaum mit Wurzel root
	 */
	protected TreeItem findLast(
			TreeItem root,
			int pos)
	{
		if (root == null)
			root = _root;
		TreeItem j = null;
		while (root != null)
		{
			j = root;
			root = root.child(pos);
		}
		return j;
	}


	/**
	 * Zählt die TreeItems im Teilbaum mit Wurzel root.
	 * @param root
	 *            Wurzel des zu durchzuzählenden Teilbaumes
	 * @return Anzahl der Elemente im Teilbaum
	 */
	protected int countItems(
			TreeItem root)
	{
		if (root == null)
			return 0;
		TreeItem i = first(root, ROOT_CHILDS_ORDER);
		int count = 0;
		while (i != null)
		{
			i = next(root, i, ROOT_CHILDS_ORDER);
			count++;
		}
		return count;
	}


	// Suche
	// =====

	/**
	 * Sucht ein TreeItem, bei dem equals(key) true zurückliefert im Teilbaum
	 * von start. Die Operation kostet O(n) Zeit! Ist start==null, wird
	 * start=root() gesetzt; ist start nicht im Baum enthalten oder konnte kein
	 * TreeItem mit key gefunden werden wird null zurückgegeben.
	 * @param key
	 *            gesuchter Schlüssel
	 * @return TreeItem mit passendem key oder null, falls nicht gefunden
	 */
	protected TreeItem find(
			Object key,
			TreeItem start)
	{
		if (((start != null) && (!contains(start))) || (_root == null))
			return null;
		TreeItem i = first(start, ROOT_CHILDS_ORDER), f = null;
		while ((i != null) && (f == null))
		{
			if (i.equals(key))
				f = i;
			i = next(start, i, ROOT_CHILDS_ORDER);
		}
		return f;
	}


	/**
	 * Sucht im Teilbaum von start mit Hilfe des Comparitors comparitor ein
	 * TreeItem mit einem möglichst 'nah' bei key liegendem Schlüssel. Diese
	 * Funktion liefert nur sinnvolle Werte, falls der Baum ein binärer Suchbaum
	 * ist. Ist start==null, wird start=root() gesetzt; ist start nicht im Baum
	 * enthalten, wird null zurückgegeben. Ist left_is_smaller auf true gesetzt,
	 * so wird im linken Teilbaum gesucht, wenn key kleiner als der aktuelle
	 * Knoten ist.
	 * @param key
	 *            gesuchter Schlüssel
	 * @param start
	 *            Wurzelknoten für die Suche
	 * @param comparitor
	 *            Comparitor-Objekt, mit dessen Hilfe die Vergleiche
	 *            durchgeführt werden
	 * @param left_is_smaller
	 *            Verhalten beim Vergleichen
	 * @return TreeItem, für das beim Vergleich EQUAL zurückgeliefert wurde bzw.
	 *         das am nächsten am Zielwert key liegt
	 */
	protected TreeItem find(
			Object key,
			TreeItem start,
			Comparitor comparitor,
			boolean left_is_smaller)
	{
		if (((start != null) && (!contains(start))) || (_root == null))
			return null;
		if (start == null)
			start = _root;
		TreeItem j = null, i = start;
		short comp = Comparitor.SMALLER, lef = Comparitor.SMALLER;
		if (!left_is_smaller)
			lef = Comparitor.BIGGER;
		while ((i != null) && (comp != Comparitor.EQUAL))
		{
			comp = comparitor.compare(key, i);
			j = i;
			if (comp == lef)
				i = i.child(TreeItem.LEFT);
			else
				i = i.child(TreeItem.RIGHT);
		}
		return j;
	}


	// Tree-Traversals
	// ===============

	/**
	 * Liefert das erste TreeItem im Teilbaum mit Wurzel root bezüglich der
	 * Traversal-Ordnung order. Diese Methode funktioniert auch für Teilbäume,
	 * die nicht zum aktuellen Baum gehören.
	 * @param root
	 *            Wurzel des zu durchreisenden Teilbaumes
	 * @param order
	 *            eine Traversal-Konstante (ROOT_CHILDS_ORDER,
	 *            CHILDS_ROOT_ORDER, LEFT_ROOT_RIGHT_ORDER,
	 *            LEFT_RIGHT_ROOT_ORDER oder ROOT_LEFT_RIGHT_ORDER)
	 */
	protected TreeItem first(
			TreeItem root,
			byte order)
	{
		if (root == null)
			root = _root;
		TreeItem i;
		switch (order)
		{
			case LEFT_ROOT_RIGHT_ORDER:
				return findLast(root, TreeItem.LEFT);
			case LEFT_RIGHT_ROOT_ORDER:
				i = findLast(root, TreeItem.LEFT);
				if ((i != null) && (i.child(TreeItem.RIGHT) != null))
					do
					{
						i = findLast(i.child(TreeItem.RIGHT), TreeItem.LEFT);
					}
					while (i.child(TreeItem.RIGHT) != null);
				return i;
			case CHILDS_ROOT_ORDER:
				i = findLast(root, 0);
				if ((i != null) && (i.nextChildPos(1) != TreeItem.UNKNOWN))
					do
					{
						i = findLast(i.child(i.nextChildPos(1)), TreeItem.LEFT);
					}
					while (i.nextChildPos(1) != TreeItem.UNKNOWN);
				return i;
			case ROOT_CHILDS_ORDER:
			case ROOT_LEFT_RIGHT_ORDER:
				return root;
		}
		return null;
	}


	/**
	 * Liefert das letzte TreeItem im Teilbaum mit Wurzel root bezüglich der
	 * Traversal-Ordnung order. Diese Methode funktioniert auch für Teilbäume,
	 * die nicht zum aktuellen Baum gehören.
	 * @param root
	 *            Wurzel des zu durchreisenden Teilbaumes
	 * @param order
	 *            eine Traversal-Konstante (ROOT_CHILDS_ORDER,
	 *            CHILDS_ROOT_ORDER, LEFT_ROOT_RIGHT_ORDER,
	 *            LEFT_RIGHT_ROOT_ORDER oder ROOT_LEFT_RIGHT_ORDER)
	 */
	protected TreeItem last(
			TreeItem root,
			byte order)
	{
		if (root == null)
			root = _root;
		switch (order)
		{
			case LEFT_ROOT_RIGHT_ORDER:
			case ROOT_LEFT_RIGHT_ORDER:
				return findLast(root, TreeItem.RIGHT);
			case ROOT_CHILDS_ORDER:
				return findLast(root, TreeItem.LAST_CHILD);
			case CHILDS_ROOT_ORDER:
			case LEFT_RIGHT_ROOT_ORDER:
				return root;
		}
		return null;
	}


	/**
	 * Liefert das nachfolgende TreeItem im Teilbaum mit der Wurzel root
	 * bezüglich der Traversal-Ordnung order zurück. Diese Methode funktioniert
	 * auch für Teilbäume, die nicht zum aktuellen Baum gehören.
	 * @param root
	 *            Wurzel des zu durchreisenden Teilbaumes
	 * @param i
	 *            TreeItem, dessen Nachfolger gesucht wird
	 * @param order
	 *            eine Traversal-Konstante (ROOT_CHILDS_ORDER,
	 *            CHILDS_ROOT_ORDER, LEFT_ROOT_RIGHT_ORDER,
	 *            LEFT_RIGHT_ROOT_ORDER oder ROOT_LEFT_RIGHT_ORDER)
	 */
	protected TreeItem next(
			TreeItem root,
			TreeItem i,
			byte order)
	{
		if (i == null)
			return first(root, order);
		if (root == null)
			root = _root;
		TreeItem j = null;
		switch (order)
		{
			case LEFT_ROOT_RIGHT_ORDER:
				if (i.child(TreeItem.RIGHT) == null)
				{
					do
					{
						j = i;
						if (i != root)
							i = i.parent();
						else
							i = null;
					}
					while ((i != null) && (i.pos(j) != TreeItem.LEFT));
				}
				else
				{
					i = findLast(i.child(TreeItem.RIGHT), TreeItem.LEFT);
				}
				break;
			case ROOT_LEFT_RIGHT_ORDER:
				if (i.child(TreeItem.LEFT) == null)
				{
					if (i.child(TreeItem.RIGHT) == null)
					{
						do
						{
							j = i;
							if (i != root)
								i = i.parent();
							else
								i = null;
						}
						while ((i != null)
								&& ((i.child(TreeItem.RIGHT) == j) || (i
										.child(TreeItem.RIGHT) == null)));
						if (i != null)
							i = i.child(TreeItem.RIGHT);
					}
					else
						i = i.child(TreeItem.RIGHT);
				}
				else
					i = i.child(TreeItem.LEFT);
				break;
			case LEFT_RIGHT_ROOT_ORDER:
				j = i;
				if (i != root)
					i = i.parent();
				else
					i = null;
				if ((i != null) && (i.child(TreeItem.RIGHT) != null)
						&& (i.child(TreeItem.RIGHT) != j))
				{
					do
					{
						i = findLast(i.child(TreeItem.RIGHT), TreeItem.LEFT);
					}
					while (i.child(TreeItem.RIGHT) != null);
				}
				break;
			case ROOT_CHILDS_ORDER:
				if (i.isLeaf())
				{
					do
					{
						j = i;
						if (i != root)
							i = i.parent();
						else
							i = null;
					}
					while ((i != null)
							&& (i.nextChildPos(i.pos(j) + 1) == TreeItem.UNKNOWN));
					if (i != null)
						i = i.child(i.nextChildPos(i.pos(j) + 1));
				}
				else
				{
					i = i.child(i.nextChildPos(0));
				}
				break;
			case CHILDS_ROOT_ORDER:
				j = i;
				if (i != root)
					i = i.parent();
				else
					i = null;
				if (i != null)
				{
					int pos = i.nextChildPos(i.pos(j) + 1);
					while (pos != TreeItem.UNKNOWN)
					{
						i = i.child(pos);
						pos = i.nextChildPos(0);
					}
				}
				break;
		}
		return i;
	}


	/**
	 * Liefert das vorhergehende TreeItem im Teilbaum mit der Wurzel root
	 * bezüglich der Traversal-Ordnung order zurück. Diese Methode funktioniert
	 * auch für Teilbäume, die nicht zum aktuellen Baum gehören.
	 * @param root
	 *            Wurzel des zu durchreisenden Teilbaumes
	 * @param i
	 *            TreeItem, dessen Vorgänger gesucht wird
	 * @param order
	 *            eine Traversal-Konstante (ROOT_CHILDS_ORDER,
	 *            CHILDS_ROOT_ORDER, LEFT_ROOT_RIGHT_ORDER,
	 *            LEFT_RIGHT_ROOT_ORDER oder ROOT_LEFT_RIGHT_ORDER)
	 */
	protected TreeItem prev(
			TreeItem root,
			TreeItem i,
			byte order)
	{
		if (i == null)
			return last(root, order);
		if (root == null)
			root = _root;
		TreeItem j = null;
		switch (order)
		{
			case LEFT_ROOT_RIGHT_ORDER:
				if (i.child(TreeItem.LEFT) == null)
				{
					do
					{
						j = i;
						if (i != root)
							i = i.parent();
						else
							i = null;
					}
					while ((i != null) && (i.pos(j) != TreeItem.RIGHT));
				}
				else
				{
					i = findLast(i.child(TreeItem.LEFT), TreeItem.RIGHT);
				}
				break;
			case ROOT_LEFT_RIGHT_ORDER:
				j = i;
				if (i != root)
					i = i.parent();
				else
					i = null;
				if ((i != null) && (i.child(TreeItem.LEFT) != null)
						&& (i.child(TreeItem.LEFT) != j))
				{
					do
					{
						i = findLast(i.child(TreeItem.LEFT), TreeItem.RIGHT);
					}
					while (i.child(TreeItem.LEFT) != null);
				}
				break;
			case LEFT_RIGHT_ROOT_ORDER:
				if (i.child(TreeItem.RIGHT) == null)
				{
					if (i.child(TreeItem.LEFT) == null)
					{
						do
						{
							j = i;
							if (i != root)
								i = i.parent();
							else
								i = null;
						}
						while ((i != null)
								&& ((i.child(TreeItem.LEFT) == j) || (i
										.child(TreeItem.LEFT) == null)));
						if (i != null)
							i = i.child(TreeItem.LEFT);
					}
					else
						i = i.child(TreeItem.LEFT);
				}
				else
					i = i.child(TreeItem.RIGHT);
				break;
			case ROOT_CHILDS_ORDER:
				j = i;
				if (i != root)
					i = i.parent();
				else
					i = null;
				if (i != null)
				{
					int pos = i.pos(j);
					if (pos > 0)
						pos = i.prevChildPos(pos - 1);
					else
						pos = TreeItem.UNKNOWN;
					while (pos != TreeItem.UNKNOWN)
					{
						i = i.child(pos);
						pos = i.prevChildPos(TreeItem.LAST_CHILD);
					}
				}
				break;
			case CHILDS_ROOT_ORDER:
				if (i.isLeaf())
				{
					do
					{
						j = i;
						if (i != root)
							i = i.parent();
						else
							i = null;
					}
					while ((i != null)
							&& ((i.pos(j) == 0) || (i
									.prevChildPos(i.pos(j) - 1) == TreeItem.UNKNOWN)));
					if (i != null)
						i = i.child(i.prevChildPos(i.pos(j) - 1));
				}
				else
				{
					i = i.child(i.prevChildPos(TreeItem.LAST_CHILD));
				}
				break;
		}
		return i;
	}


	// Kopieren von Daten in andere Datenstrukturen
	// ============================================

	/**
	 * Speichert den Teilbaum mit Wurzel root in Durchlaufordnung order in das
	 * Array array ab Index startindex mit maximaler Länge length. Zurückgegeben
	 * wird die tatsächliche Anzahl geschriebener Werte. objecttype bestimmt, ob
	 * die TreeItems, key() oder value()-Werte geschrieben werden sollen. Ist
	 * root==null, wird root auf root() gesetzt.
	 * @param array
	 *            Array zum Speichern
	 * @param startindex
	 *            Index ab dem der Teilbaum im Array gespeichert werden soll
	 * @param length
	 *            maximale Länge des zu speichernden Intervalles
	 * @param root
	 *            Wurzelknoten des zu speichernden Teilbaumes
	 * @param order
	 *            Durchlaufordnung in der der Teilbaum gespeichert werden soll
	 * @param objecttype
	 *            Objekttyp, der gespeichert werden soll
	 * @return tatsächliche Anzahl gespeicherter Objekte
	 */
	protected int storeInArray(
			Object[] array,
			int startindex,
			int length,
			TreeItem root,
			byte order,
			byte objecttype)
	{
		if (array == null)
			return 0;
		if (root == null)
			root = _root;
		int index = startindex, count = 0;
		TreeItem i = first(root, order);
		while ((i != null) && (length > 0))
		{
			switch (objecttype)
			{
				case TREEITEM:
					array[index] = i;
					break;
				case KEY:
					array[index] = i.key();
					break;
				case VALUE:
					array[index] = i.value();
					break;
			}
			i = next(root, i, order);
			index++;
			length--;
			count++;
		}
		return count;
	}


	/**
	 * Verwandelt den Teilbaum mit Wurzel root in eine Kette von ListItems. Der
	 * Teilbaum wird in Durchlaufordnung order durchlaufen. Sind im Baum
	 * gespeicherte TreeItems gleichzeitig ListItems (implementieren sie also
	 * das Interface ListItem), so werden in
	 */
	protected ListItem getListItemChain(
			TreeItem root,
			byte order)
	{
		if (root == null)
			root = _root;
		TreeItem i = first(root, order);
		ListItem f = null, l = null, n;
		while (i != null)
		{
			if (i instanceof ListItem)
			{
				n = (ListItem) i;
				if (!n.setOwningList(null))
				{
					n = (ListItem) i.clone();
					if (!n.setOwningList(null))
						n = new StdListItem(i.key(), i.value()); // dann halt nicht...
				}
			}
			else
			{
				if (i.key() == i.value())
					n = new SimpleListItem(i.key());
				else
					n = new StdListItem(i.key(), i.value());
			}
			n.connect(l, false);
			if (f == null)
				f = n;
			l = n;
			i = next(root, i, order);
		}
		return f;
	}


	// Inner-Class. - Kann bei Umstieg auf Java 1.1 wieder aktiviert werden!
	// /**
	// * Enumeration-Objekt zum Aufzählen aller Baum-Elemente.
	// */
	// private class TreeEnumerator implements Enumeration {
	// 	/** Wurzel des Teilbaumes, der durchlaufen wird. */
	//  private TreeItem _t_root;
	//  /** Cursor-TreeItem. */
	//  private TreeItem _i,_end;
	//  /** Durchlaufordnung der Elemente. */
	//  private byte _order;
	//  /** Falls true, wird Reihenfolge vorwärts durchlaufen, sonst rückwärts. */
	//  private boolean _forward;
	//  /** Objekte die zurückgegeben werden sollen. */
	//  private byte _objecttype;
	//		/**
	//		* Leerer Konstruktor. Enthält keine Elemente.
	//		*/
	//		TreeEnumerator() {
	//			_i=null;
	//		}
	//  /**
	//		* Konstruktor zum Aufzählen kompletter Teilbäume in Durchlaufordnung order
	//		* in Richtung forward mit Rückgabe von Objekten des Types objecttype.
	//		*/
	//  TreeEnumerator(TreeItem root,byte order,boolean forward,byte objecttype) {
	//   _order=order; _objecttype=objecttype; _forward=forward; _t_root=root;
	//  	if (_forward) _i=first(root,order); else _i=last(root,order);
	//			_end=null;
	//  }
	//		/**
	//		* Konstruktor zum Aufzählen von Intervallen von start bis end in 
	//		* Durchlaufordnung order mit Rückgabe von Objekten des Types objecttype.
	//		*/
	//		TreeEnumerator(TreeItem start,TreeItem end,byte order,byte objecttype) {
	//   _order=order; _objecttype=objecttype; _forward=true; _t_root=null;
	//			if (start!=null) _i=start; else _i=first(null,order);
	//			_end=end;
	//		}
	//  /** Liefert true, falls noch ein Element vorhanden. */
	//  public boolean hasMoreElements() {
	//   return (_i!=null);
	//  }
	//  /** Liefert das nächste Element in der Struktur. */
	//  public Object nextElement() throws NoSuchElementException {
	//  	if (_i==null) throw new NoSuchElementException();
	//   Object O=null;
	//   switch (_objecttype) {
	//   	case KEY:
	//   		O=_i.key();
	//   		break;
	//   	case VALUE:
	//   		O=_i.value();
	//   		break;
	//   	case TREEITEM:
	//   		O=_i;
	//   		break;
	//   }
	//			if (_i==_end) _i=null;
	//			else {
	//    if (_forward) _i=next(_t_root,_i,_order);
	//    else _i=prev(_t_root,_i,_order);
	//			}
	//   return O;
	//  }
	// }
	/**
	 * Zählt alle Objekte im Teilbaum mittels eines Enumeration-Objektes auf.
	 * @param root
	 *            Wurzel des aufzuzählenden Teilbaumes oder null, falls der
	 *            gesamte Baum aufgezählt werden soll
	 * @param order
	 *            Durchlaufreihenfolge, in der der Teilbaum durchlaufen wird
	 * @param forward
	 *            falls true, wird der Teilbaum in Vorwärtsrichtung durchlaufen,
	 *            ansonsten rückwärts
	 * @param objecttype
	 *            Objekttyp, der zurückgegeben wird (eine Konstante: TREEITEM,
	 *            KEY oder VALUE)
	 * @return Enumeration-Objekt, mit dem der Durchlauf realisiert werden kann
	 */
	protected Enumeration enumerate(
			TreeItem root,
			byte order,
			boolean forward,
			byte objecttype)
	{
		return new TreeEnumerator(this, root, order, forward, objecttype);
	}


	/**
	 * Zähl alle Objekte im Intervall von start bis end mittels eines
	 * Enumeration- Objektes auf.
	 * @param start
	 *            Start-TreeItem des Intervalles
	 * @param end
	 *            End-TreeItem des Intervalles
	 * @param order
	 *            Durchlaufreihenfolge, in der der Teilbaum durchlaufen wird
	 * @param objecttype
	 *            Objekttyp, der zurückgegeben wird (eine Konstante: TREEITEM,
	 *            KEY oder VALUE)
	 * @return Enumeration-Objekt, mit dem der Durchlauf realisiert werden kann
	 */
	protected Enumeration enumerate(
			TreeItem start,
			TreeItem end,
			byte order,
			byte objecttype)
	{
		return new TreeEnumerator(this, start, end, order, objecttype);
	}


	/**
	 * Liefert eine Aufzählung, die nichts aufzählt.
	 * @return Enumeration-Objekt
	 */
	protected Enumeration emptyEnumeration()
	{
		return new TreeEnumerator();
	}


	// Einfügen,Ausschneiden und Löschen
	// =================================

	/**
	 * Der Besitzer des Teilbaum mit Wurzel root wird auf owner geändert. Ist
	 * dies nicht möglich, werden die TreeItems geklont und der Besitzer der
	 * Kopien auf owner gesetzt. Es kann owner==this gelten!
	 * @param root
	 *            Wurzel des Teilbaumes
	 * @return Referenz auf die eventuell geklonte Wurzel des in Besitz genommen
	 *         Teilbaumes
	 */
	protected synchronized TreeItem ownTree(
			TreeItem root,
			Owner owner)
	{
		if (owner == this)
			allowAccess(ANY_ACCESS);
		TreeItem i = root, j = null, l = null, k, f = null;
		int pos = 0;
		while (i != null)
		{
			k = i;
			if (!k.setOwningTree(owner))
			{
				k = (TreeItem) k.clone();
				if (!k.setOwningTree(owner))
					throwException(new TreeException(k, TreeException.SET_OWNER));
				if (l == null)
				{
					if (!k.connect(null, TreeItem.PARENT))
						throwException(new TreeException(null, k,
								TreeException.CONNECT));
				}
				else if (!l.connect(k, pos))
					throwException(new TreeException(l, k,
							TreeException.CONNECT));
			}
			if (f == null)
				f = k;
			if (i.isLeaf())
			{
				l = k;
				do
				{
					j = i;
					if (i != root)
					{
						i = i.parent();
						l = l.parent();
					}
					else
						i = null;
				}
				while ((i != null)
						&& (i.nextChildPos(i.pos(j) + 1) == TreeItem.UNKNOWN));
				if (i != null)
				{
					pos = i.nextChildPos(i.pos(j) + 1);
					j = i;
					i = i.child(pos);
				}
			}
			else
			{
				pos = i.nextChildPos(0);
				l = k;
				j = i;
				i = i.child(pos);
			}
		}
		if (owner == this)
			allowAccess(NO_ACCESS);
		return f;
	}


	/**
	 * Fügt das TreeItem n als Kind Nummer pos an base an. Gehört n nicht dem
	 * Baum, wird der Besitzer geändert oder das TreeItem geklont. base sollte
	 * ein TreeItem des Baumes sein oder ==null, dann wird n als Wurzel
	 * eingefügt und der Baum als Kind Nummer pos an die neue Wurzel.
	 * @param n
	 *            neues einzufügendes TreeItem
	 * @param base
	 *            Position im Baum, an der eingefügt wird
	 * @param pos
	 *            Nummer des Kindes, als das n eingefügt werden soll
	 */
	protected synchronized TreeItem add(
			TreeItem n,
			TreeItem base,
			int pos)
	{
		if (n == null)
			return null;
		n = ownTree(n, this);
		_length += connect(n, base, pos);
		return n;
	}


	/**
	 * Knüpft einen Teilbaum mit Wurzel n an den Knoten base als Kind Nummer pos
	 * und gibt die Anzahl der verknüpften Elemente zurück. Die TreeItems, die
	 * an n hängen und base, sollten bereits zum Baum gehören, sonst werden beim
	 * Verknüpfen TreeExceptions ausgelöst. Ist base==null, so wird vor root
	 * eingefügt. Ist der Sohn Nummer pos von Base schon belegt oder wird vor
	 * der Baumwurzel eingefügt, wird der entsprechende Teilbaum an den ersten
	 * Knoten im anzufügenden Teilbaum angefügt, dessen Sohn Nummer pos nicht
	 * belegt ist.
	 * @param n
	 *            Wurzel des anzuknüpfenden Teilbaumes
	 * @param base
	 *            Position im Baum, an der eingefügt wird
	 * @param pos
	 *            Nummer des Kindes, als das n eingefügt werden soll
	 * @return Anzahl der angeknüpften TreeItems
	 */
	protected synchronized int connect(
			TreeItem n,
			TreeItem base,
			int pos)
	{
		if (((base != null) && (!contains(base))) || (!contains(n)))
			return 0;
		allowAccess(ANY_ACCESS);
		int count = countItems(n);
		TreeItem l = findLast(n, pos);
		if ((base != null) && (base.child(pos) != null))
			if (!l.connect(base.child(pos), pos))
			{
				throwException(new TreeException(l, base.child(pos),
						TreeException.CONNECT));
			}
		if (base == null)
		{
			if (!l.connect(_root, pos))
				throwException(new TreeException(l, _root,
						TreeException.CONNECT));
			_root = n;
		}
		else
		{
			if (!base.connect(n, pos))
				throwException(new TreeException(base, n, TreeException.CONNECT));
		}
		allowAccess(NO_ACCESS);
		return count;
	}


	/**
	 * Add für binäre Suchbäume. Das TreeItem n wird an die richtige Stelle im
	 * Baum mit Hilfe des Comparitors comparitor hinzugefügt. Ist
	 * left_is_smaller true, werden in den linken Teilbäume kleinere Schlüssel
	 * gespeichert. Ist allow_duplicates true, dann können doppelte Schlüssel
	 * eingefügt werden - sie werden in den Teilbaum mit den kleineren Werten
	 * gespeichert. Ist allow_duplicates false, so wird im Falle eines doppelten
	 * Schlüssels das alte TreeItem überschrieben. (In dem Fall sollte der neue
	 * und der alte Knoten denselben maximalen Rang haben.)
	 * @param n
	 *            neues TreeItem
	 * @param comparitor
	 *            Comparitor-Objekt für Vergleiche
	 * @param left_is_smaller
	 *            bestimmt, ob kleinere Werte (<=) in den linken oder rechten
	 *            Teilbaum kommen
	 * @param allow_duplicates
	 *            falls true werden Duplikate der Schlüssel erlaubt
	 * @return Refernz auf das eingefügte TreeItem
	 */
	protected synchronized TreeItem add(
			TreeItem n,
			Comparitor comparitor,
			boolean left_is_smaller,
			boolean allow_duplicates)
	{
		TreeItem base = find(n, null, comparitor, left_is_smaller);
		if (base == null)
		{
			return add(n, null, 0);
		}
		else
		{
			short comp = comparitor.compare(n, base);
			if ((!allow_duplicates) && (comp == Comparitor.EQUAL))
			{
				allowAccess(ANY_ACCESS);
				if (!n.setOwningTree(this))
				{
					n = (TreeItem) n.clone();
					if (!n.setOwningTree(this))
						throwException(new TreeException(n,
								TreeException.SET_OWNER));
				}
				TreeItem v = base.parent();
				if (v != null)
				{
					if (!v.connect(n, v.pos(base)))
						throwException(new TreeException(v, n,
								TreeException.CONNECT));
				}
				else
				{
					if (!base.connect(null, TreeItem.PARENT))
						throwException(new TreeException(null, base,
								TreeException.CONNECT));
					_root = base;
				}
				for (int i = 0; i < n.maxRank(); i++)
					if (!n.connect(base.child(i), i))
						throwException(new TreeException(n, base.child(i),
								TreeException.CONNECT));
				allowAccess(NO_ACCESS);
				return base;
			}
			else
			{
				int pos = TreeItem.LEFT;
				if (comp == Comparitor.BIGGER)
					pos = TreeItem.RIGHT;
				return add(n, base, pos);
			}
		}
	}


	/**
	 * Schneidet den Teilbaum beginnend mit root aus dem Baum aus und gibt die
	 * Anzahl der ausgeschnittenen TreeItems zurück. Ist root==null, so wird der
	 * gesamte Baum ausgeschnitten.
	 * @param root
	 *            Wurzel des auszuschneidenden Teilbaumes
	 * @return Anzahl der ausgeschnittenen TreeItems
	 */
	protected int cutTree(
			TreeItem root)
	{
		if (root == null)
			root = _root;
		else if (!contains(root))
			return 0;
		allowAccess(ANY_ACCESS);
		if (!root.cut())
			throwException(new TreeException(null, root, TreeException.CONNECT));
		// Jetzt noch Teilbaum durchlaufen
		TreeItem i = first(root, CHILDS_ROOT_ORDER);
		int count = 0;
		while (i != null)
		{
			if (!i.setOwningTree(null))
				throwException(new TreeException(i, TreeException.SET_OWNER));
			i = next(root, i, CHILDS_ROOT_ORDER);
			count++;
		}
		_length -= count;
		allowAccess(NO_ACCESS);
		return count;
	}


	/**
	 * Entfernt den Teilbaum mit Wurzel root aus dem Baum. Die betroffenen
	 * TreeItems hängen danach nicht mehr zusammen.
	 * @param root
	 *            Wurzel des Teilbaumes
	 * @return Anzahl der entfernten TreeItems
	 */
	public int removeTree(
			TreeItem root)
	{
		int c = cutTree(root);
		// allowAccess ist nicht notwendig, da nach cut der Teilbaum 'eh niemanden
		// mehr gehört...
		TreeItem i = first(root, CHILDS_ROOT_ORDER), j;
		while (i != null)
		{
			j = next(root, i, CHILDS_ROOT_ORDER);
			if (!i.clearConnections())
				throwException(new TreeException(i, TreeException.REMOVE));
			i = j;
		}
		return c;
	}


	/**
	 * Liefert ein entfernbares TreeItem im binären Suchbaum zum TreeItem i. Ein
	 * TreeItem kann nur korrekt entfernt werden, wenn es maximal einen Sohn
	 * hat. Ist dies der Fall, so liefert diese Methode i zurück, ansonsten
	 * einen symmetrischen Nachbarn, der entfernt werden kann (besser: i's
	 * Stelle einnehmen kann).
	 * @param i
	 *            zu löschendes TreeItem
	 * @return entfernbares TreeItem
	 */
	protected TreeItem getRemovableTreeItem(
			TreeItem i)
	{
		if (!contains(i))
			return null;
		if (i.rank() < 2)
			return i;
		TreeItem v = i.parent();
		int pos;
		if (v != null)
			pos = v.pos(i);
		else
			pos = TreeItem.RIGHT;
		return findLast(i.child(pos), (pos == TreeItem.RIGHT) ? TreeItem.LEFT
				: TreeItem.RIGHT);
	}


	/**
	 * Das TreeItem v übernimmt den Platz von u (und dessen Balancierungsdaten).
	 * Bedingungen: v und u müssen dem aktuellen Baum angehören und v muß
	 * entfernbar sein (also v.rank()<2). Hat v noch einen Sohn, so wird dieser
	 * an seinen Vater gehangen (v wird also korrekt entfernt).
	 * @param v
	 *            Knoten, der den Platz übernehmen soll
	 * @param u
	 *            Knoten, der entfernt wird
	 * @return Referenz auf u
	 */
	protected synchronized TreeItem fitInPlace(
			TreeItem v,
			TreeItem u)
	{
		TreeItem w = v.parent(), x;
		allowAccess(ANY_ACCESS);
		if (v.isLeaf())
			x = null;
		else
			x = v.child(v.nextChildPos(0));
		if ((w != v) && (w != null))
		{
			if (!w.connect(x, w.pos(v)))
				throwException(new TreeException(w, x, TreeException.CONNECT));
		}
		w = u.parent();
		if (w == null)
		{
			if (!v.connect(null, TreeItem.PARENT))
				throwException(new TreeException(null, v, TreeException.CONNECT));
			_root = v;
		}
		else if (!w.connect(v, w.pos(u)))
			throwException(new TreeException(w, v, TreeException.CONNECT));
		// Verbindungen und Balancierung übernehmen...
		for (int j = 0; j < u.maxRank(); j++)
			if (u.child(j) != v)
			{
				if (!v.connect(u.child(j), j))
					throwException(new TreeException(v, u.child(j),
							TreeException.CONNECT));
			}
			else
			{
				if (!v.connect(x, j))
					throwException(new TreeException(v, x,
							TreeException.CONNECT));
			}
		v.setBalance(u.balance());
		allowAccess(NO_ACCESS);
		return u;
	}


	/**
	 * Entfernt das TreeItem i symmetrisch aus dem Baum. Darf nur bei binären
	 * Bäumen angewandt werden! Hat i zwei Söhne, so wird i im Prinzip durch
	 * seinen symmetrischen Nachfolger (LEFT-ROOT-RIGHT-ORDER) ersetzt. Die
	 * Ordnungseigenschaft eines binären Suchbaumes bleibt so erhalten.
	 * @param i
	 *            zu entfernendes TreeItem
	 * @return Referenz auf entferntes TreeItem
	 */
	public synchronized TreeItem removeSym(
			TreeItem i)
	{
		TreeItem v = getRemovableTreeItem(i);
		if (v == null)
			return null;
		if (v == i)
		{
			TreeItem x = i.child(i.nextChildPos(0));
			if (i.parent() == null)
				_root = x;
			allowAccess(ANY_ACCESS);
			i.remove();
			allowAccess(NO_ACCESS);
		}
		else
		{
			fitInPlace(v, i);
		}
		allowAccess(ANY_ACCESS);
		if (!i.setOwningTree(null))
			throwException(new TreeException(i, TreeException.SET_OWNER));
		if (!i.clearConnections())
			throwException(new TreeException(i, TreeException.REMOVE));
		_length--;
		allowAccess(NO_ACCESS);
		return i;
	}


	// Modifizieren der Verknüpfungen
	// ==============================

	/**
	 * Eine einfache Rotation. Das Kind Nummer pos1 von u (=:v) übernimmt die
	 * Position von u im Baum, indem u als Kind von v an Position pos2 gehangen
	 * wird und dessen Teilbaum an pos2 an pos1 übernimmt. Üblicherweise wird
	 * die Rotation beim Balancieren von Binärbäumen verwendet:<pre> u v v T3 =>
	 * T1 u T1 T2 T2 T3 (hier ist pos1=TreeItem.LEFT und
	 * pos2=TreeItem.RIGHT)</pre>
	 * @param u
	 *            Knoten, an dem rotiert werden soll
	 * @param pos1
	 *            Position des Knotens, der auf u's Position gedreht wird
	 * @param pos2
	 *            Position an der u am gedrehten Knoten hängt
	 * @return Referenz auf das TreeItem, das die Rolle von u übernommen hat
	 */
	protected synchronized TreeItem rotation(
			TreeItem u,
			int pos1,
			int pos2)
	{
		if (!contains(u))
			return null;
		TreeItem v = u.child(pos1), w = u.parent();
		if (v == null)
			return u;
		allowAccess(ANY_ACCESS);
		if (!u.connect(v.child(pos2), pos1))
			throwException(new TreeException(u, v.child(pos2),
					TreeException.CONNECT));
		if (!v.connect(u, pos2))
			throwException(new TreeException(v, u, TreeException.CONNECT));
		if (w == null)
		{
			_root = v;
			if (!v.connect(null, TreeItem.PARENT))
				throwException(new TreeException(null, v, TreeException.CONNECT));
		}
		else if (!w.connect(v, w.pos(u)))
			throwException(new TreeException(w, v, TreeException.CONNECT));
		allowAccess(NO_ACCESS);
		return v;
	}


	/**
	 * Eine doppelte Rotation. Die drei Knoten u, v:=u.child(pos1) und
	 * x:=v.child(pos2) werden so gedreht, daß x die Position von u einnimmt, u
	 * an pos2 und v an pos1 an x hängen. Üblicherweise wird die doppelte
	 * Rotation beim Balancieren von Binärbäumen verwendet:<pre> u x v T3 => v u
	 * T1 x T1 T2 T3 T4 T2 T2 (hier ist pos1=TreeItem.LEFT und
	 * pos2=TreeItem.RIGHT)</pre>
	 * @param u
	 *            Knoten, an dem rotiert werden soll
	 * @param pos1
	 *            Position an der v an u hängt und später auch an x
	 * @param pos2
	 *            Position an der x an v hängt und später u an x
	 * @return Referenz auf das TreeItem, das die Rolle von u übernommen hat
	 *         (also x)
	 */
	public synchronized TreeItem double_rotation(
			TreeItem u,
			int pos1,
			int pos2)
	{
		if (!contains(u))
			return null;
		TreeItem v = u.child(pos1), w = u.parent(), x = v.child(pos2);
		if ((v == null) || (x == null))
			return u;
		allowAccess(ANY_ACCESS);
		if (!u.connect(x.child(pos2), pos1))
			throwException(new TreeException(u, x.child(pos2),
					TreeException.CONNECT));
		if (!v.connect(x.child(pos1), pos2))
			throwException(new TreeException(u, x.child(pos1),
					TreeException.CONNECT));
		if (!x.connect(u, pos2))
			throwException(new TreeException(x, u, TreeException.CONNECT));
		if (!x.connect(v, pos1))
			throwException(new TreeException(x, v, TreeException.CONNECT));
		if (w == null)
		{
			_root = x;
			if (!x.connect(null, TreeItem.PARENT))
				throwException(new TreeException(null, v, TreeException.CONNECT));
		}
		else if (!w.connect(x, w.pos(u)))
			throwException(new TreeException(w, x, TreeException.CONNECT));
		allowAccess(NO_ACCESS);
		return x;
	}


	// Methoden für das Interface Owner
	// ================================

	/**
	 * Erlaubt den Zugriff (temporär) von ListItems
	 */
	protected void allowAccess(
			byte accesskonst)
	{
		_access = accesskonst;
	}


	/**
	 * Sperrt den Baumzugriff und löst die übergebene RuntimeException aus.
	 * @param e
	 *            Fehlerklasse
	 */
	private void throwException(
			RuntimeException e)
	{
		allowAccess(NO_ACCESS);
		throw e;
	}


	/**
	 * Das Owned-Objekt who bittet den Owner um Genehmigung einer Aktion vom Typ
	 * accesstype mit dem Argument argument.
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
		if (_access == ANY_ACCESS)
			return true;
		switch (accesstype)
		{
			case TreeItem.SET_OWNER:
			case TreeItem.REMOVE:
			case TreeItem.CLEAR_CONNECTIONS:
			case TreeItem.CONNECT_PARENT:
			case TreeItem.CONNECT:
				return false;
			case TreeItem.SET_MAX_RANK:
				int mr = ((Number) argument).intValue();
				TreeItem i = (TreeItem) who;
				if (mr >= i.maxRank())
					return true;
				else
					return false;
		}
		return true;
	}

}
