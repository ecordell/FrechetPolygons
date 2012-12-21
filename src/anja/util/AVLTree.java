package anja.util;


import java.lang.reflect.Array;
import java.util.Comparator;


/**
 * Diese Klasse implementiert eine AVL-Baum-Datenstruktur. In diesem Baum
 * koennen Objekte eingefuegt werden, die das Interface <i>Comparable</i>
 * implementieren. Hierbei koennen auch verschiedene Objektetypen eingefuegt
 * werden, sofern dies in deren <i>compareTo()</i>-Methode beruecksichtigt wird.
 * 
 * <br>Die Übergabe eines <i>Comparators</i> an die Klasse, ermöglicht ab diesem
 * Zeitpunkt die Nutzung dieses Objekts anstatt der <i>compareTo()</i>-Methode
 * der Einzelobjekte. Dies ermöglicht die Sortierung von Objekten, die zwar
 * nicht ihre Ordnung, aber ihre zu sortierenden Werte während des Programms
 * ändern können (beispielweise schnittfreie Segmente in der Ebene, deren
 * Abstand zu einem konkreten Punkt betrachtet wird. Der Abstand der Segmente
 * zum Punkt bezüglich bestimmter Blickwinkel kann sich ändern).
 * 
 * <br>Die Klasse unterstützt Generics E, die Comparable<E> implementieren. Es
 * ist hierbei möglich, mehrere Objekte mit gleichem Wert im Baum zu speichern.
 * Die <code>contains()</code>-Methode überprüft, ob ein solcher Wert im Baum
 * vorhanden ist, die <code>remove()</code>-Methode entfernt eine Instanz des
 * Wertes, sofern diese vorhanden ist.
 * 
 * @author Marina Bachran, Andreas Lenerz
 * 
 * @see java.lang.Comparable
 */
public class AVLTree<E extends Comparable<E>>
{

	// ***********************
	// INNER CLASSES
	// ***********************

	/**
	 * Eine Klasse, die einen Knoten des Baums und seine Verbindungen zu anderen
	 * Knoten enthaelt.
	 */
	protected class AVLTreeEntry
	{

		public E				value	= null;
		public AVLTreeEntry[]	sons	= null;
		public AVLTreeEntry		father	= null;
		public int				height	= 0;


		@SuppressWarnings("unchecked")
		public AVLTreeEntry(
				E val)
		{
			value = val;
			sons = (AVLTreeEntry[]) Array.newInstance(AVLTreeEntry.class, 2);
			sons[0] = null;
			sons[1] = null;
		}
	}


	// ***********************
	// VARIABLES
	// ***********************

	protected int				count		= 0;	// Anzahl der Knoten im Baum

	protected AVLTreeEntry	wurzel		= null; // Referenz auf die Wurzel

	private Class<?>		_cla		= null;

	private Comparator<E>	_comparator	= null;

	/**
	 * The smallest value
	 */
	protected AVLTreeEntry	_min_value	= null;

	/**
	 * The biggest value
	 */
	protected AVLTreeEntry	_max_value	= null;


	// ***********************
	// CONSTRUCTORS
	// ***********************

	/**
	 * Default constructor
	 */
	public AVLTree()
	{}


	/**
	 * Initializes the tree by adding all elements of the list to an empty tree.
	 * 
	 * @param list
	 *            The list of elements
	 */
	public AVLTree(
			java.util.List<E> list)
	{
		if (list != null && !list.isEmpty())
		{
			for (E a : list)
			{
				add(a);
			}
			_initialize(list.get(0).getClass());
		}
	}


	/**
	 * Uses the Comparator object instead of the compareTo() function of the
	 * Comparable interface to sort the values.
	 * 
	 * <br>This can be of importance, of the values of the objects change during
	 * the calculation, but the order stays the same.
	 * 
	 * <br>An example would be the distance of not intersecting segments in the
	 * plane in comparison to a fixed point.
	 * 
	 * @param comp
	 *            The Comparator object, which compares the two entries. If the
	 *            object is null, the Comparable interface of the objects is
	 *            used.
	 */
	public AVLTree(
			Comparator<E> comp)
	{
		_comparator = comp;
	}


	/**
	 * Initializes the tree by adding all elements of the array to an empty
	 * tree.
	 * 
	 * @param val
	 *            The array of elements
	 */
	public AVLTree(
			E[] val)
	{
		if (val != null && val.length > 0)
		{
			for (E a : val)
			{
				add(a);
			}
			_initialize(val[0].getClass());
		}
	}


	// ***********************
	// PUBLIC METHODS
	// ***********************

	/**
	 * Calls <code>add(E)</code> for all elements
	 * 
	 * @param keys
	 *            An array of elements
	 */
	public void add(
			E[] keys)
	{
		for (E a : keys)
		{
			add(a);
		}
	}


	/**
	 * Calls <code>add(E)</code> for all elements
	 * 
	 * @param list
	 *            An array of elements
	 */
	public void add(
			java.util.List<E> list)
	{
		for (E a : list)
		{
			add(a);
		}
	}


	/**
	 * Fuegt den Schluesselwert key in den Baum ein.
	 * 
	 * @param key
	 *            der Wert, der in den Baum eingefuegt wird.
	 */
	public void add(
			E key)
	{
		if (key != null)
			_initialize(key.getClass());

		if (wurzel == null)
		{
			_insertInEmptyTree(key);
		}
		else
		{
			AVLTreeEntry act = null;
			AVLTreeEntry next = wurzel;
			while (next != null)
			{
				act = next;
				next = act.sons[_getSonIndex(act, key)];
			}
			_insertBehind(act, key);
		}
	}


	/**
	 * Deletes all tree entries
	 */
	public void clear()
	{
		wurzel = null;
		count = 0;
		_min_value = null;
		_max_value = null;
	}


	/**
	 * Searches for the given key.
	 * 
	 * @param key
	 *            The key
	 * @return true iff the key already exists in the tree, false else
	 */
	public boolean contains(
			E key)
	{
		return _search(key) != null;
	}


	/**
	 * Returns the maximum of the saved values (ordered by Comparator)
	 * 
	 * <br>The method runs in O(1) as the min and max are already updated in the
	 * add()-method
	 * 
	 * @return The maximum value
	 */
	public E getMax()
	{
		return _max_value.value;
	}


	/**
	 * Returns the minimum of the saved values (ordered by Comparator)
	 * 
	 * <br>The method runs in O(1) as the min and max are already updated in the
	 * add()-method
	 * 
	 * @return The minimum value
	 */
	public E getMin()
	{
		return _min_value.value;
	}


	/**
	 * Liefert den Bauminhalt in In-Order zurueck.
	 * 
	 * @return Comparable[] der Inhalt des Baums
	 */
	@SuppressWarnings("unchecked")
	public E[] getTreeEntries()
	{
		if (_cla == null)
			return null;

		E[] items = (E[]) Array.newInstance(_cla, count);
		_inOrder(wurzel, items, 0);
		return items;
	}


	/**
	 * Invokes add(key)
	 * 
	 * @param key
	 *            The key that is added to the tree
	 */
	@Deprecated
	public void insert(
			E key)
	{
		add(key);
	}


	/**
	 * If the tree is empty, true is returned
	 * 
	 * @return true if the tree is empty, false else
	 */
	public boolean isEmpty()
	{
		return count == 0 || wurzel == null;
	}


	/**
	 * Loescht einen Schluesselwert key aus dem Baum (falls er existiert).
	 * 
	 * <br>If the key has been added multiple times, only one occurance is
	 * deleted while the others remain. If a key should be deleted, that has not
	 * been added before, then the method return false.
	 * 
	 * @param key
	 *            der Wert, der aus dem Baum geloescht werden soll.
	 * 
	 * @return true if the value was deleted or false else
	 */
	public boolean remove(
			E key)
	{
		AVLTreeEntry keyIndex = _search(key);

		if (keyIndex == null)
			return false;

		if ((keyIndex.sons[0] == null) && (keyIndex.sons[1] == null))
			_deleteLeaf(keyIndex);
		else
			_deleteVertice(keyIndex);
		count--;

		return true;
	}


	/**
	 * Switch the Comparator.
	 * 
	 * <br>This doesn't call a resorting of the objects that are already saved
	 * in the tree.
	 * 
	 * @param comp
	 *            The new Comparator object or null to use the Comparable of the
	 *            objects.
	 */
	public void setComparator(
			Comparator<E> comp)
	{
		_comparator = comp;
	}


	// ***********************
	// PRIVATE METHODS
	// ***********************

	/**
	 * Berechnet die Hoehe eines Knoten mit Hilfe der Hoehe der Soehne
	 */
	private int _calcHeight(
			AVLTreeEntry v)
	{
		v.height = Math.max(_getSonHeight(v, 0), _getSonHeight(v, 1)) + 1;
		return v.height;
	}


	/**
	 * Berechnet die Hoehenbalance mit Hilfe der Hoehe der beiden Soehne
	 */
	private int _calcHeightBalance(
			AVLTreeEntry v)
	{
		return _getSonHeight(v, 0) - _getSonHeight(v, 1);
	}


	/**
	 * Ueberprueft die Balance im Knoten v. Dabei finden ggf. rekursive Aufrufe
	 * statt.
	 */
	private void _checkBalance(
			AVLTreeEntry v)
	{
		if (v != null)
		{
			int oldHeight = v.height;
			int balv = _calcHeightBalance(v);
			boolean checkWBalance = false;
			int compare = 1; // balv == -2
			int d1 = 0;
			int d2 = 1;
			if (balv == 2)
			{
				d2 = 0;
				d1 = 1;
				compare = -1;
			}
			if ((balv == 2) || (balv == -2))
			{
				AVLTreeEntry w = v.sons[d2];
				int oldWHeight = w.height;
				int balw = _calcHeightBalance(w);
				if (compare * balw <= 0)
				{
					_rotation(d1, d2, v); // Links-Rotation mit v
				}
				else
				{
					_rotation(d2, d1, w); // Rechts-Rotation mit w
					_rotation(d1, d2, v); // Links-Rotation mit v
				}
				int newWHeight = _calcHeight(w);
				if (oldWHeight != newWHeight)
					checkWBalance = true;
			}
			int newHeight = _calcHeight(v);
			if ((oldHeight != newHeight) || (checkWBalance))
				_checkBalance(v.father);
		}
	}


	/**
	 * Loescht ein Blatt im AVL Baum.
	 */
	private void _deleteLeaf(
			AVLTreeEntry v)
	{

		// Wurzel löschen
		if (v.father == null)
		{
			wurzel = null;
			_min_value = null;
			_max_value = null;
			count = 0;
			return;
		}

		AVLTreeEntry father = v.father;
		int del_son = (father.sons[0] != null && father.sons[0] == v) ? 0 : 1;

		father.sons[del_son] = null;

		if (_min_value == v)
			_min_value = father;
		if (_max_value == v)
			_max_value = father;

		_checkBalance(father);
	}


	/**
	 * Loescht ein Knoten im AVL Baum.
	 */
	private void _deleteVertice(
			AVLTreeEntry entry)
	{
		AVLTreeEntry v = entry;
		int d1, d2;
		if (_getSonHeight(v, 0) >= _getSonHeight(v, 1))
		{
			d1 = 0;
			d2 = 1;
		}
		else
		{
			d1 = 1;
			d2 = 0;
		}
		AVLTreeEntry next = entry.sons[d1];
		while (next != null)
		{
			v = next;
			next = v.sons[d2];
		}
		entry.value = v.value;
		if (v.sons[d1] != null)
		{
			AVLTreeEntry father = v.father;
			int del_son = (father.sons[d2] != null && father.sons[d2] == v) ? d2
					: d1;
			father.sons[del_son] = v.sons[d1];
			v.sons[d1].father = father;
			_checkBalance(father);
		}
		else
			_deleteLeaf(v);
	}


	/**
	 * Gibt die Hoehe des Teilbaums des linken oder rechten Sohnes zurueck. Ist
	 * son = 0, so wird der linke zurueckgegeben, ist son = 1 so wird der rechte
	 * zurueckgegeben.
	 */
	private int _getSonHeight(
			AVLTreeEntry v,
			int son)
	{
		if (v.sons[son] != null)
			return v.sons[son].height;
		return -1;
	}


	/**
	 * Gibt den Index des Sohnes (0=Links oder 1=Rechts) aus, bei dem
	 * weitergesucht werden soll.
	 */
	private int _getSonIndex(
			AVLTreeEntry v,
			E key)
	{
		int val;
		if (_comparator != null)
			val = _comparator.compare(key, v.value);
		else
			val = key.compareTo(v.value);

		if (val < 0)
			return 0;
		else
			return 1;
	}


	/**
	 * Used for generic purposes.
	 * 
	 * @param c
	 *            The class type of E
	 */
	private void _initialize(
			Class<?> c)
	{
		_cla = c;
	}


	/**
	 * Fuegt einen Knoten mit dem Schluesselwert key hinter dem Knoten father
	 * ein. Dabei sollte father keinen Sohn an der entsprechenden Seite haben!
	 */
	private void _insertBehind(
			AVLTreeEntry father,
			E key)
	{
		// Schreibe in leeren Bereich...
		AVLTreeEntry newEntry = new AVLTreeEntry(key);
		newEntry.father = father;
		father.sons[_getSonIndex(father, key)] = newEntry;

		if (father == _min_value && _getSonIndex(father, key) == 0)
			_min_value = newEntry;
		if (father == _max_value && _getSonIndex(father, key) == 1)
			_max_value = newEntry;

		_checkBalance(father);
		count++;
	}


	/**
	 * Fuegt in den leeren Baum ein.
	 */
	private void _insertInEmptyTree(
			E key)
	{
		AVLTreeEntry newEntry = new AVLTreeEntry(key);
		count = 1;
		wurzel = newEntry;
		_min_value = newEntry;
		_max_value = newEntry;
	}


	/**
	 * Liest den Baum in inOrder-Reihenfolge aus (wird fuer die Methode
	 * getTreeEntries() gebraucht),
	 */
	private int _inOrder(
			AVLTreeEntry v,
			E[] items,
			int index)
	{
		if (v.sons[0] != null)
			index = _inOrder(v.sons[0], items, index);
		items[index++] = v.value;
		if (v.sons[1] != null)
			index = _inOrder(v.sons[1], items, index);
		return index;
	}


	/**
	 * Rotation (allgemein)
	 */
	private void _rotation(
			int d1,
			int d2,
			AVLTreeEntry v)
	{
		AVLTreeEntry fatherV = v.father;
		//ist v der linke oder der rechte Sohn?
		AVLTreeEntry w = v.sons[d2];
		//Den Vater von v auf w setzen
		if (fatherV != null)
		{
			if (fatherV.sons[0] == v)
				fatherV.sons[0] = w;
			else
				fatherV.sons[1] = w;
		}
		else
			wurzel = w;
		w.father = fatherV;

		//Den Sohn von w als Sohn von v setzen
		if (w.sons[d1] != null)
		{
			v.sons[d2] = w.sons[d1];
			v.sons[d2].father = v;
		}
		else
			v.sons[d2] = null;

		//v als Sohn von w setzen
		w.sons[d1] = v;
		v.father = w;
	}


	/**
	 * Sucht ein Element im Baum. Es wird der Knoten (Index im Array) des
	 * gefundenen Elements zurueckgegeben. Ist der Schluesselwert nicht im Baum
	 * enthalten wird null zurueckgegeben.
	 */
	private AVLTreeEntry _search(
			E key)
	{
		AVLTreeEntry v = wurzel;
		int next = _searchVertice(v, key);
		while (next != -1)
		{
			if (v.sons[next] == null)
				return null;
			v = v.sons[next];
			next = _searchVertice(v, key);
		}

		return v;
	}


	/**
	 * Gibt an, in welchem Teilbaum vom Knoten v nach dem Schluesselwert (key)
	 * weitergesucht werden soll: -1 => dies ist der Knoten 0 => linker Teilbaum
	 * 1 => rechter Teilbaum
	 */
	private int _searchVertice(
			AVLTreeEntry v,
			E key)
	{
		int val;
		if (_comparator != null)
			val = _comparator.compare(key, v.value);
		else
			val = key.compareTo(v.value);

		if (val == 0)
			return -1;
		else if (val < 0)
			return 0;
		else
			return 1;
	}

}
