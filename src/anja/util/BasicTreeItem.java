package anja.util;


/**
 * <p> Einfachstes TreeItem. Alle Methoden von TreeItem sind korrekt
 * implementiert, allein kann es keinen Wert oder Schlüssel speichern.
 * Basisklasse für SimpleTreeItem und StdTreeItem.
 * 
 * @author Thomas Wolf
 * @version 1.0
 */
public class BasicTreeItem
		implements TreeItem, java.io.Serializable
{

	/** Referenz auf die besitzende Baum-Struktur. */
	private Owner		_owningTree	= null;
	/** Referenz auf den Vaterknoten. */
	private TreeItem	_parent		= null;
	/** Referenzen auf Kinder. */
	private TreeItem[]	_child		= null;
	/** Daten für Balancierung. */
	private byte		_balance	= 0;


	/**
	 * Erzeugt ein TreeItem, das maximal maxrank Söhne haben kann.
	 */
	public BasicTreeItem(
			int maxrank)
	{
		_child = new TreeItem[maxrank];
	}


	// Allgemeine Routinen
	// ===================

	/**
	 * Vergleicht zwei TreeItems. Zwei TreeItems sind genau dann gleich, wenn
	 * ihre Schlüssel gleich (im Sinne von equals) sind. equals liefert genau
	 * dann true, falls object auf dieses Objekt verweist, object.equals(key())
	 * true liefert oder object ein TreeItem ist und gleich mit diesem TreeItem
	 * ist. Überschreibt Object.equals().
	 * 
	 * @param object
	 *            zu vergleichendes Objekt
	 *            
	 * @return true, falls gleich
	 */
	public boolean equals(
			Object object)
	{
		if (object == null)
			return false;
		if (this == object)
			return true;
		if (key() == object)
			return true;
		if (object.equals(key()))
			return true;
		if (object instanceof TreeItem)
		{
			TreeItem v = (TreeItem) object;
			if (key() == v.key())
				return true;
			if ((key() == null) || (v.key() == null))
				return false;
			return (key().equals(v.key()));
		}
		return false;
	}


	/**
	 * Überschreibt Object.toString().
	 */
	@Override
	public String toString()
	{
		return getClass().getName() + "[balance=" + _balance + "]";
	}


	/**
	 * Klont das Objekt. Dabei sollte folgendes gelten: Der neue TreeItem ist
	 * <strong>nicht</strong> verknüpft und hat <strong>keinen</strong>
	 * Besitzer. Die angehängten Schlüssel und Objekte werden ebenfalls geklont.
	 * 
	 * @return Kopie des TreeItems
	 */
	public Object clone()
	{
		return new BasicTreeItem(_child.length);
	}


	/**
	 * Legt eine Kopie der im Objekt enthaltenen referenzierten Datenobjekte an.
	 * Schlüsselobjekte werden nicht geklont und Verbindungen sowie Besitzer
	 * bleiben unverändert.
	 */
	public void cloneData()
	{}


	// Angehörigkeit
	// =============

	/**
	 * Legt den Besitzer fest, dem das ListItem angehört. Ist bereits ein
	 * Besitzer vorhanden, so wird er um Zustimmung gebeten. Außerdem wird der
	 * neue Besitzer um Zustimmung gebeten. Erst wenn <strong>beide</strong>
	 * zugestimmt haben wird die Änderung vollzogen. Ist der alte und der neue
	 * Besitzer ein und dasselbe Objekt, so wird niemand um Zustimmung gebeten
	 * und true zurückgeliefert.
	 * 
	 * @param owner
	 *            neuer Besitzer
	 *            
	 * @return true, falls Änderung erfolgreich
	 */
	public synchronized boolean setOwningTree(
			Owner owner)
	{
		if (owner == _owningTree)
			return true;
		if (!requestTreeAccess(SET_OWNER, owner))
			return false;
		Owner oldown = _owningTree;
		_owningTree = owner;
		if (!requestTreeAccess(SET_OWNER, owner))
		{
			_owningTree = oldown;
			return false;
		}
		return true;
	}


	/**
	 * Liefert den Besitzer des ListItems.
	 * 
	 * @return der Besitzer
	 */
	public Owner getOwningTree()
	{
		return _owningTree;
	}


	/**
	 * Bittet den zugehörigen Owner um Genehmigung einer Aktion vom Typ
	 * accesstype mit dem Argument argument.
	 * 
	 * @param accesstype
	 *            Typ der Aktion (des Zugriffs)
	 * @param argument
	 *            Argument der Aktion
	 *            
	 * @return true, falls der Owner die Aktion erlaubt, sonst false
	 */
	protected boolean requestTreeAccess(
			int accesstype,
			Object argument)
	{
		if (_owningTree == null)
			return true;
		return _owningTree.requestAccess(accesstype, this, argument);
	}


	// Balancierungsdaten
	// ==================

	/**
	 * Liefert die Balancierungsdaten des Knotens.
	 * 
	 * @return Balancierungswert
	 */
	public byte balance()
	{
		return _balance;
	}


	/**
	 * Setzt die Balance-Daten für Rebalancierung. Es erfolgt eine Nachfrage
	 * beim Besitzer
	 * 
	 * @param b
	 *            neue Balance-Daten
	 *            
	 * @return true, falls Änderung erfolgreich, sonst false
	 */
	public boolean setBalance(
			byte b)
	{
		if (!requestTreeAccess(SET_BALANCE, new Integer(b)))
			return false;
		_balance = b;
		return true;
	}


	// Zugriff auf bestehende Verbindungen
	// ===================================

	/**
	 * Liefert den Vaterknoten in der Baum-Struktur.
	 * 
	 * @return Vater
	 */
	public TreeItem parent()
	{
		return _parent;
	}


	/**
	 * Liefert den Sohnknoten mit Position pos in der Baum-Struktur. Ist
	 * pos==PARENT, wird eine Referenz auf den Vater zurückgegeben.
	 * 
	 * @param pos
	 *            Position des gesuchten Knotens
	 *            
	 * @return Sohn
	 */
	public TreeItem child(
			int pos)
	{
		if (pos > _child.length)
			return null;
		if (pos < 0)
		{
			if (pos == PARENT)
				return _parent;
			if (pos == LAST_CHILD)
				return _child[_child.length - 1];
			return null;
		}
		return _child[pos];
	}


	/**
	 * Liefert den Bruder des aktuellen Knotens. Das macht vor allem Sinn bei
	 * binären Bäumen. Ansonsten ist es der Sohnknotens des Vaters, der
	 * symmetrisch zu dem aktuellen Knoten angeordnet ist.
	 * 
	 * @return Bruder
	 */
	public TreeItem sibling()
	{
		if (_parent == null)
			return null;
		int pos = _parent.pos(this);
		pos = _parent.maxRank() - 1 - pos;
		return _parent.child(pos);
	}


	/**
	 * Liefert die Position des Knotens child im aktuellen Knoten. Ist der
	 * zurückgegebene Wert==PARENT, ist child der Vater, ansonsten wird entweder
	 * UNKNOWN zurückgegeben, falls child nicht als Kind im Knoten gefunden
	 * wurde, oder die Position des Knotens child.
	 * 
	 * @param child
	 *            Knoten, dessen Position gesucht wird
	 *            
	 * @return Position von child bzw. PARENT oder UNKNOWN
	 */
	public int pos(
			TreeItem child)
	{
		if (_parent == child)
			return PARENT;
		int i;
		for (i = 0; (i < _child.length) && (_child[i] != child); i++)
			;
		if (i >= _child.length)
			return UNKNOWN;
		else
			return i;
	}


	/**
	 * Liefert die Position des nächsten Sohnknotens ab Position pos, der nicht
	 * null ist. Existiert solch einer nicht oder ist pos==LAST_CHILD, wird
	 * UNKNOWN zurückgegeben. Ist pos==UNKNOWN oder pos==PARENT wird das erste
	 * Kind !=null zurückgegeben.
	 * 
	 * @param pos
	 *            Position, ab der gesucht wird
	 *            
	 * @return Position des nächsten Sohnes !=null
	 */
	public int nextChildPos(
			int pos)
	{
		if (pos == LAST_CHILD)
			return UNKNOWN;
		if (pos < 0)
			pos = 0;
		for (; (pos < _child.length) && (_child[pos] == null); pos++)
			;
		if (pos >= _child.length)
			return UNKNOWN;
		return pos;
	}


	/**
	 * Liefert die Position des letzten Sohnknotens vor oder an Position pos,
	 * der nicht null ist. Existiert solch einer nicht, wird UNKNOWN
	 * zurückgegeben. Ist pos==LAST_CHILD, pos==UNKNOWN oder pos==PARENT, wird
	 * das letzte Kind !=null zurückgegeben.
	 * 
	 * @param pos
	 *            Position, vor und an der gesucht wird
	 *            
	 * @return Position des letzten Sohnes !=null
	 */
	public int prevChildPos(
			int pos)
	{
		if (pos < 0)
			pos = _child.length - 1;
		for (; (pos >= 0) && (_child[pos] == null); pos--)
			;
		if (pos < 0)
			return UNKNOWN;
		return pos;
	}


	/**
	 * Liefert true zurück, falls der Knoten ein Blatt ist (also keine Söhne
	 * hat).
	 * 
	 * @return true, falls Knoten ein Blatt ist
	 */
	public boolean isLeaf()
	{
		int i;
		for (i = 0; (i < _child.length) && (_child[i] == null); i++)
			;
		return (i >= _child.length);
	}


	/**
	 * Liefert true, falls der Knoten ein innerer Knoten ist (also mindestens
	 * einen Sohn hat).
	 * 
	 * @return true, falls Knoten ein innerer Knoten ist
	 */
	public boolean isInner()
	{
		return (!isLeaf());
	}


	/**
	 * Liefert true, falls der Knoten eine Wurzel ist, also keinen Vater hat.
	 * 
	 * @return true, falls Knoten eine Wurzel ist
	 */
	public boolean isRoot()
	{
		return (_parent == null);
	}


	/**
	 * Liefert den Grad des Knotens. Gemeint ist hier die Anzahl der ausgehenden
	 * Kanten; es gibt ja für jeden Knoten bis auf die Wurzel eine einmündende
	 * Kante.
	 * 
	 * @return Anzahl der ausgehenden Kanten
	 */
	public int rank()
	{
		int r = 0;
		for (int i = 0; i < _child.length; i++)
			if (_child[i] != null)
				r++;
		return r;
	}


	/**
	 * Liefert den maximalen Rang des TreeItems zurück.
	 * 
	 * @return maximaler Rang
	 */
	public int maxRank()
	{
		return _child.length;
	}


	/**
	 * Liefert die Ebene, in der sich das Element im Baum befindet. Die Wurzel
	 * hat Ebene 0, ihre Söhne Ebene 1 usw.
	 * 
	 * @return Ebene im Baum
	 */
	public int level()
	{
		int lev = 0;
		TreeItem n = _parent;
		while (n != null)
		{
			n = n.parent();
			lev++;
		}
		return lev;
	}


	// Verknüpfungen herstellen
	// ========================

	/**
	 * Setzt den maximalen Rang des Knotens auf mr. Es erfolgt eine Rückfrage an
	 * den Besitzer. Wurde die Anfrage genehmigt und erfolgreich durchgeführt,
	 * wird true zurückgegeben.
	 * 
	 * @param mr
	 *            neuer maximaler Rang
	 *            
	 * @return true, falls Operation gestattet und erfolgreich
	 */
	public synchronized boolean setMaxRank(
			int mr)
	{
		if (mr < 0)
			return false;
		if (!requestTreeAccess(SET_MAX_RANK, new Integer(mr)))
			return false;
		TreeItem[] ch = _child;
		_child = new TreeItem[mr];
		for (int i = 0; i < mr; i++)
			if (i < ch.length)
				_child[i] = ch[i];
			else
				_child[i] = null;
		return true;
	}


	/**
	 * Komplettiert die Verknüpfung und sollte daher nur von connect aus
	 * aufgerufen werden. Es darf hier der Besitzer nicht nocheinmal um
	 * Erlaubnis gefragt werden (alle notwendigen Tests führt connect bereits
	 * durch), jedoch muß answerConnect feststellen, daß der Aufruf nur von
	 * connect stammen kann. Dies geht beispielsweise, indem getestet wird, ob
	 * die 'halbe Verknüpfung' schon vollzogen wurde.
	 * 
	 * @param node
	 *            Knoten mit dem verknüpft werden soll
	 * @param pos
	 *            Ziel der Verknüpfung
	 *            
	 * @return true, falls Verknüpfung erfolgreich und gestattet
	 */
	public synchronized boolean answerConnect(
			TreeItem node,
			int pos)
	{
		if ((node == null) || (node.getOwningTree() != getOwningTree()))
			return false;
		if (node.child(pos) != this)
			return false;
		_parent = node;
		return true;
	}


	/**
	 * Verknüpft den Baumknoten mit einem anderen entsprechend der
	 * Verknüpfungskonstante target. Es können nur Knoten verknüpft werden, die
	 * derselben Struktur angehören. Um eine korrekte Verknüpfung mit node zu
	 * vollziehen, wird answerConnect von node aufgerufen. Wird die Verknüpfung
	 * vom Besitzer nicht gestattet oder tritt ein Fehler beim Verknüpfen auf,
	 * wird false zurückgegeben. Wird connect pos==PARENT aufgerufen, so wird
	 * node ignoriert und der Knoten mit null als Parent verknüpft.
	 * 
	 * @param node
	 *            Knoten, mit dem verknüpft werden soll
	 * @param pos
	 *            Ziel für die Verknüpfung (also eine Kind-Nummer)
	 *            
	 * @return true, falls Verknüpfung gestattet und erfolgreich
	 */
	public synchronized boolean connect(
			TreeItem node,
			int pos)
	{
		if ((node != null) && (node.getOwningTree() != getOwningTree()))
			return false;
		if (pos > _child.length)
			return false;
		if (pos < 0)
		{
			if (!requestTreeAccess(CONNECT_PARENT, node))
				return false;
			_parent = null;
		}
		else
		{
			if (!requestTreeAccess(CONNECT, node))
				return false;
			TreeItem n = _child[pos];
			_child[pos] = node;
			if ((node != null) && (!node.answerConnect(this, pos)))
			{
				_child[pos] = n;
				return false;
			}
		}
		return true;
	}


	/**
	 * Schneidet den Teilbaum beginnend mit diesem Knoten aus dem Baum heraus.
	 * Der besitzende Baum wird nur einmal von dem Vaterknoten gefragt, ob
	 * dieser seinen Sohn auf null setzen darf, der Rest der Verknüpfungen läuft
	 * dann ohne weitere Fragen.
	 * 
	 * @return true, falls ausschneiden erfolgreich und gestattet
	 */
	public synchronized boolean cut()
	{
		if (_parent == null)
			return true;
		if (!_parent.connect(null, _parent.pos(this)))
			return false;
		_parent = null;
		return true;
	}


	/**
	 * Entfernt den Knoten aus dem Baum. Das kann nur gehen, wenn rank()<2 ist,
	 * wenn also der Knoten höchstens einen Sohn hat. Dieser wird dann anstelle
	 * des Knotens an den Vater gehangen. Die Balancierungsdaten werden
	 * gelöscht.
	 * 
	 * @return true, falls Entfernen gestattet und erfolgreich
	 */
	public synchronized boolean remove()
	{
		if ((rank() >= 2) || (!requestTreeAccess(REMOVE, null)))
			return false;
		int pos = nextChildPos(0);
		if (_parent == null)
		{
			if (pos >= 0)
			{
				if (!_child[pos].connect(null, PARENT))
					return false;
				_child[pos] = null;
			}
		}
		else
		{
			int ppos = _parent.pos(this);
			if (pos >= 0)
			{
				if (!_parent.connect(_child[pos], ppos))
					return false;
				_child[pos] = null;
			}
			else
			{
				if (!_parent.connect(null, ppos))
					return false;
			}
		}
		_balance = 0;
		return true;
	}


	/**
	 * Löscht alle Verbindungen vom TreeItem aus. Die Verbindungen werden also
	 * nicht korrekt entfernt. Es wird im Gegensatz zu remove nur eine Anfrage
	 * an den Besitzer geben! Der Besitzer bleibt bestehen. Die
	 * Balancierungsdaten werden ebenfalls gelöscht.
	 * 
	 * @return true, falls erfolgreich
	 */
	public synchronized boolean clearConnections()
	{
		if (!requestTreeAccess(CLEAR_CONNECTIONS, null))
			return false;
		for (int i = 0; i < _child.length; i++)
			_child[i] = null;
		_parent = null;
		_balance = 0;
		return true;
	}


	// Eigentum
	// ========

	/**
	 * Liefert den gespeicherten Schlüssel des Objektes.
	 * 
	 * @return gespeicherter Schlüssel
	 */
	public Object key()
	{
		return null;
	}


	/**
	 * Setzt den zugeordneten Schlüssel.
	 * 
	 * @param key
	 *            neuer Schlüssel
	 */
	public boolean setKey(
			Object key)
	{
		return false;
	}


	/**
	 * Liefert den gespeicherten Wert zurück.
	 * 
	 * @return gespeichertes Objekt
	 */
	public Object value()
	{
		return null;
	}


	/**
	 * Speichert das Objekt object. (NICHT IMPLEMENTIERT)
	 * 
	 * @param object
	 *            zu speicherndes Objekt
	 */
	public boolean setValue(
			Object object)
	{
		return false;
	}

}
