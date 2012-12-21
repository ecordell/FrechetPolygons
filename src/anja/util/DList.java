package anja.util;


/**
 * Eine einfache doppelt verkettete Liste
 * @author Peter Koellner
 * @version 1.0 24 Apr 1997
 */

public class DList
{

	DListElement	_first, _last;
	public int		_count;


	/**
	 * Leerer Konstruktor, legt eine leere Liste an.
	 */
	public DList()
	{}


	/**
	 * Kopierkonstruktor kopiert die Liste der Objekte. Objekte werden nicht
	 * kopiert. Dafuer ist clone() da.
	 * 
	 * @param cp
	 *            Zu kopierende Liste
	 */
	public DList(
			DList cp)
	{
		DListEnumeration enumeration = cp.elements();

		while (enumeration.hasNextElement())
		{
			Object data = enumeration.getNext();
			append(data);
			_count++;
		}
	}


	/**
	 * Liefert anzahl der Elemente
	 * 
	 * @return Länge der Liste
	 */
	public int getLength()
	{
		return _count;
	}


	/**
	 * Fuegt Objekt obj am Anfang der Liste ein.
	 * 
	 * @param obj
	 *            Einzufügendes Objekt
	 */
	public synchronized void insert(
			Object obj)
	{
		DListElement el = new DListElement(obj);

		_count++;

		if (_last == null)
		{
			_first = el;
			_last = el;
			el._nextEl = null;
		}
		else
		{
			_first._prevEl = el;
			el._nextEl = _first;
			_first = el;
		}

		el._prevEl = null;
	}


	/**
	 * Haengt Objekt obj ans Ende der Liste an
	 * 
	 * @param obj
	 *            Anzuhängendes Objekt
	 */
	public synchronized void append(
			Object obj)
	{
		DListElement el = new DListElement(obj);

		_count++;

		if (_first == null)
		{
			_first = el;
			_last = el;
			el._prevEl = null;
		}
		else
		{
			_last._nextEl = el;
			el._prevEl = _last;
			_last = el;
		}

		el._nextEl = null;
	}


	/**
	 * Haengt Objekt ans Ende der List an
	 * 
	 * @return DListElement, das neu eingefuegte Listenelement
	 */
	public synchronized DListElement append2(
			Object obj)
	{
		DListElement el = new DListElement(obj);

		_count++;

		if (_first == null)
		{
			_first = el;
			_last = el;
			el._prevEl = null;
		}
		else
		{
			//    System.out.println("In DLIST Append2 "+_last);
			_last._nextEl = el;
			el._prevEl = _last;
			_last = el;
		}

		el._nextEl = null;
		return el;
	}


	/**
	 * liefert Zugriffsobjekt fuer die Elemente
	 * 
	 * @return Enumeration der Liste
	 */
	public DListEnumeration elements()
	{
		return new DListEnumeration(this);
	}


	/**
	 * liefert zurueck, ob die Liste leer ist.
	 * 
	 * @return true, wenn Liste leer, false sonst
	 */
	public boolean isEmpty()
	{
		return _first == null;
	}


	/**
	 * Loescht die komplette Liste
	 */
	public synchronized void clear()
	{
		_first = null;
		_last = null;
		_count = 0;
	}


	/**
	 * Loescht die Liste mit Inhalt, also die DListElemente werden ebenfalls
	 * geloescht.
	 * 
	 * @return Anzahl der gelöschten Objekte in der Liste
	 */
	public synchronized int reset()
	{

		DListElement y = _first;
		DListElement x;
		int i = 0;
		while (y != null)
		{
			x = y._nextEl;
			y.Reset();
			y = x;
			i++;
		}

		_first = null;
		_last = null;
		_count = 0;
		return i;
		/*DListEnumeration en=new DListEnumeration(this);
		DListElement e;
		while(en.getCurrent()!=null){
		    e=(DListElement)en.getCurrent();
		    e.Reset();

		}
		*/
	}


	/**
	 * Haengt das Objekt vor dem DListElement in die Liste. Wird vom
	 * DListElement aus aufgerufen.
	 * 
	 * @param obj
	 *            Das einzufügende Objekt
	 * @param El
	 *            Das Vergleichsobjekt, vor dem eingefügt wird
	 * 
	 * @return Das neue DListElement
	 */
	DListElement insertAt(
			Object obj,
			DListElement El)
	{
		if (El == null)
		{ // El == null => Liste ist gaz leer!
			insert(obj);
			return _first;
		}

		DListElement pred = El._prevEl;

		if (pred == null)
		{ // Leerer Predecessor => Erstes El. neu!
			insert(obj);
			return _first;
		}

		// Wirklich mittendrin einfuegen

		DListElement nEl = new DListElement(obj);

		_count++;

		nEl._prevEl = pred;
		pred._nextEl = nEl;

		nEl._nextEl = El;
		El._prevEl = nEl;

		return nEl;
	}


	/**
	 * Haengt das Objekt nach dem DListElement in die Liste. Wird vom
	 * DListElement aus aufgerufen.
	 * 
	 * @param obj
	 *            Das einzufügende Objekt
	 * @param El
	 *            Das Vergleichsobjekt, nach dem eingefügt wird
	 * 
	 * @return Das neue DListElement
	 */
	DListElement appendAt(
			Object obj,
			DListElement El)
	{
		if (El == null)
		{ // El == null => Liste ist ganz leer!
			append(obj);
			return _last;
		}

		DListElement succ = El._nextEl;

		if (succ == null)
		{ // Leerer Successor => Letztes El. neu!
			append(obj);
			return _last;
		}

		// Wirklich mittendrin einfuegen

		DListElement nEl = new DListElement(obj);

		_count++;

		nEl._prevEl = El;
		El._nextEl = nEl;

		nEl._nextEl = succ;
		succ._prevEl = nEl;

		return nEl;
	}


	/**
	 * Entfernt das Objekt, auf das das DListElement zeigt, aus der Liste.
	 * 
	 * @param El
	 *            Das Objekt, welches entfernt werden soll
	 * 
	 * @return Das naechste Element, wenn ungleich null. Sonst das vorherige
	 *         Element, und null bei leerer Liste.
	 */
	public DListElement remove(
			DListElement El)
	{
		if (El == null)
			return null;

		DListElement pred = El._prevEl;
		DListElement succ = El._nextEl;

		--_count;

		if (pred == null)
		{
			_first = succ;
		}
		else
		{
			pred._nextEl = succ;
		}

		if (succ == null)
		{
			_last = pred;
		}
		else
		{
			succ._prevEl = pred;
		}

		if (succ == null)
			return pred;

		return succ;
	}


	public DListElement remove_(
			DListElement El)
	{
		if (El == null)
			return null;

		DListElement pred = El._prevEl;
		DListElement succ = El._nextEl;

		--_count;

		if (pred == null)
		{
			_first = succ;
		}
		else
		{
			pred._nextEl = succ;
		}

		if (succ == null)
		{
			_last = pred;
		}
		else
		{
			succ._prevEl = pred;
		}

		// if(succ == null)
		//         return pred;

		return succ;
	}


	/**
	 * Haengt die Elemente der anderen Liste ans Ende dieser Liste.
	 * 
	 * @param other
	 *            Anzuhängende Liste
	 */
	public synchronized void concat(
			DList other)
	{
		if (_first == null)
		{ // Diese Liste ist leer => andere 1:1 holen
			_first = other._first;
			_last = other._last;
			_count = other._count;
			other.clear();
			return;
		}

		if (other.isEmpty())
		{ // Andere ist leer, nichts anzufuegen
			return;
		}

		_last._nextEl = other._first;
		other._first._prevEl = _last;
		_last = other._last;
		_count += other._count;
		other.clear();
	}


	/**
	 * Liefert das erste Element
	 * 
	 * @return Das erste Listenelement
	 * 
	 */
	public DListElement getFirst()
	{
		return _first;
	}


	/**
	 * Liefert das letzte Element
	 * 
	 * @return Das letzte Listenelement
	 */
	public DListElement getLast()
	{
		return _last;
	}
}
