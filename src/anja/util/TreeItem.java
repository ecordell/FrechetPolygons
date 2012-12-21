package anja.util;


/**
 * <p>
 * 
 * 
 * @author Thomas Wolf
 * @version 1.0
 */
public interface TreeItem
		extends Cloneable, KeyValueHolder, java.io.Serializable
{

	// Konstanten für Positionen
	// =========================

	/** Vater */
	public final static int	PARENT				= -1;
	/** Unbekanntes Element. */
	public final static int	UNKNOWN				= -3;
	/** Letztes Kind. */
	public final static int	LAST_CHILD			= -2;
	/** Linken Sohn */
	public final static int	LEFT				= 0;
	/** Rechten Sohn */
	public final static int	RIGHT				= 1;

	// Konstanten für requestAccess beim Owner
	// =======================================

	/** Der Besitzer wird neu gesetzt. */
	public final static int	SET_OWNER			= ListItem.SET_OWNER;
	/** Object aus der Kette entfernen. */
	public final static int	REMOVE				= ListItem.REMOVE;
	/** Verbindungen löschen. */
	public final static int	CLEAR_CONNECTIONS	= ListItem.CLEAR_CONNECTIONS;
	/** Verknüfung zum Vater. */
	public final static int	CONNECT_PARENT		= 101;
	/** Verknüpfung zu einem Sohn. */
	public final static int	CONNECT				= 102;
	/** Den maximalen Rang setzen. */
	public final static int	SET_MAX_RANK		= 103;
	/** Setzen der Balancierungsdaten. */
	public final static int	SET_BALANCE			= 104;


	// Cloneable
	// =========

	/**
	 * Klont das Objekt. Dabei sollte folgendes gelten: Der neue TreeItem ist
	 * <strong>nicht</strong> verknüpft und hat <strong>keinen</strong>
	 * Besitzer. Die angehängten Schlüssel und Objekte werden ebenfalls geklont.
	 * @return Kopie des TreeItems
	 */
	public Object clone();


	/**
	 * Legt eine Kopie der im Objekt enthaltenen referenzierten Datenobjekte an.
	 * Schlüsselobjekte werden nicht geklont und Verbindungen sowie Besitzer
	 * bleiben unverändert.
	 */
	public void cloneData();


	// Angehörigkeit
	// =============

	/**
	 * Legt den Besitzer fest, dem das ListItem angehört. Ist bereits ein
	 * Besitzer vorhanden, so wird er um Zustimmung gebeten. Außerdem wird der
	 * neue Besitzer um Zustimmung gebeten. Erst wenn <strong>beide</strong>
	 * zugestimmt haben wird die Änderung vollzogen. Ist der alte und der neue
	 * Besitzer ein und dasselbe Objekt, so wird niemand um Zustimmung gebeten
	 * und true zurückgeliefert.
	 * @param owner
	 *            neuer Besitzer
	 * @return true, falls Änderung erfolgreich
	 */
	public boolean setOwningTree(
			Owner owner);


	/**
	 * Liefert den Besitzer des ListItems.
	 * @return der Besitzer
	 */
	public Owner getOwningTree();


	// Balancierungsdaten
	// ==================

	/**
	 * Liefert die Balancierungsdaten des Knotens.
	 * @return Balancierungswert
	 */
	public byte balance();


	/**
	 * Setzt die Balance-Daten für Rebalancierung. Es erfolgt eine Nachfrage
	 * beim Besitzer
	 * @param b
	 *            neue Balance-Daten
	 * @return true, falls Änderung erfolgreich, sonst false
	 */
	public boolean setBalance(
			byte b);


	// Zugriff auf bestehende Verbindungen
	// ===================================

	/**
	 * Liefert den Vaterknoten in der Baum-Struktur.
	 * @return Vater
	 */
	public TreeItem parent();


	/**
	 * Liefert den Sohnknoten mit Position pos in der Baum-Struktur. Ist
	 * pos==PARENT, wird eine Referenz auf den Vater zurückgegeben.
	 * @param pos
	 *            Position des gesuchten Knotens
	 * @return Sohn
	 */
	public TreeItem child(
			int pos);


	/**
	 * Liefert den Bruder des aktuellen Knotens. Das macht vor allem Sinn bei
	 * binären Bäumen. Ansonsten ist es der Sohnknotens des Vaters, der
	 * symmetrisch zu dem aktuellen Knoten angeordnet ist.
	 * @return Bruder
	 */
	public TreeItem sibling();


	/**
	 * Liefert die Position des Knotens child im aktuellen Knoten. Ist der
	 * zurückgegebene Wert==PARENT, ist child der Vater, ansonsten wird entweder
	 * UNKNOWN zurückgegeben, falls child nicht als Kind im Knoten gefunden
	 * wurde oder die Position des Knotens child.
	 * @param child
	 *            Knoten, dessen Position gesucht wird
	 * @return Position von child bzw. PARENT oder UNKNOWN
	 */
	public int pos(
			TreeItem child);


	/**
	 * Liefert die Position des nächsten Sohnknotens ab Position pos, der nicht
	 * null ist. Existiert solch einer nicht oder ist pos==LAST_CHILD, wird
	 * UNKNOWN zurückgegeben. Ist pos==UNKNOWN oder pos==PARENT wird das erste
	 * Kind !=null zurückgegeben.
	 * @param pos
	 *            Position, ab der gesucht wird
	 * @return Position des nächsten Sohnes !=null
	 */
	public int nextChildPos(
			int pos);


	/**
	 * Liefert die Position des letzten Sohnknotens vor oder an Position pos,
	 * der nicht null ist. Existiert solch einer nicht, wird UNKNOWN
	 * zurückgegeben. Ist pos==LAST_CHILD, pos==UNKNOWN oder pos==PARENT, wird
	 * das letzte Kind !=null zurückgegeben.
	 * @param pos
	 *            Position, vor und an der gesucht wird
	 * @return Position des letzten Sohnes !=null
	 */
	public int prevChildPos(
			int pos);


	/**
	 * Liefert true zurück, falls der Knoten ein Blatt ist (also keine Söhne
	 * hat).
	 * @return true, falls Knoten ein Blatt ist
	 */
	public boolean isLeaf();


	/**
	 * Liefert true, falls der Knoten ein innerer Knoten ist (also mindestens
	 * einen Sohn hat).
	 * @return true, falls Knoten ein innerer Knoten ist
	 */
	public boolean isInner();


	/**
	 * Liefert true, falls der Knoten eine Wurzel ist, also keinen Vater hat.
	 * @return true, falls Knoten eine Wurzel ist
	 */
	public boolean isRoot();


	/**
	 * Liefert den Grad des Knotens. Gemeint ist hier die Anzahl der ausgehenden
	 * Kanten; es gibt ja für jeden Knoten bis auf die Wurzel eine einmündende
	 * Kante.
	 * @return Anzahl der ausgehenden Kanten
	 */
	public int rank();


	/**
	 * Liefert den maximalen Rang des TreeItems zurück.
	 * @return maximaler Rang
	 */
	public int maxRank();


	/**
	 * Liefert die Ebene, in der sich das Element im Baum befindet. Die Wurzel
	 * hat Ebene 0, ihre Söhne Ebene 1 usw.
	 * @return Ebene im Baum
	 */
	public int level();


	// Verknüpfungen herstellen
	// ========================

	/**
	 * Setzt den maximalen Rang des Knotens auf mr. Es erfolgt eine Rückfrage an
	 * den Besitzer. Wurde die Anfrage genehmigt und erfolgreich durchgeführt,
	 * wird true zurückgegeben.
	 * @param mr
	 *            neuer maximaler Rang
	 * @return true, falls Operation gestattet und erfolgreich
	 */
	public boolean setMaxRank(
			int mr);


	/**
	 * Komplettiert die Verknüpfung und sollte daher nur von connect aus
	 * aufgerufen werden. Es darf hier der Besitzer nicht nocheinmal um
	 * Erlaubnis gefragt werden (alle notwendigen Tests führt connect bereits
	 * durch), jedoch muß answerConnect feststellen, daß der Aufruf nur von
	 * connect stammen kann. Dies geht beispielsweise, indem getestet wird, ob
	 * die 'halbe Verknüpfung' schon vollzogen wurde.
	 * @param node
	 *            Knoten mit dem verknüpft werden soll
	 * @param pos
	 *            Ziel der Verknüpfung
	 * @return true, falls Verknüpfung erfolgreich und gestattet
	 */
	public boolean answerConnect(
			TreeItem node,
			int pos);


	/**
	 * Verknüpft den Baumknoten mit einem anderen entsprechend der
	 * Verknüpfungskonstante target. Es können nur Knoten verknüpft werden, die
	 * derselben Struktur angehören. Um eine korrekte Verknüpfung mit node zu
	 * vollziehen, wird answerConnect von node aufgerufen. Wird die Verknüpfung
	 * vom Besitzer nicht gestattet oder tritt ein Fehler beim Verknüpfen auf,
	 * wird false zurückgegeben. Wird connect pos==PARENT aufgerufen, so wird
	 * node ignoriert und der Knoten mit null als Parent verknüpft.
	 * @param node
	 *            Knoten, mit dem verknüpft werden soll
	 * @param pos
	 *            Ziel für die Verknüpfung (also eine Kind-Nummer)
	 * @return true, falls Verknüpfung gestattet und erfolgreich
	 */
	public boolean connect(
			TreeItem node,
			int pos);


	/**
	 * Schneidet den Teilbaum beginnend mit diesem Knoten aus dem Baum heraus.
	 * Der besitzende Baum wird nur einmal von dem Vaterknoten gefragt, ob
	 * dieser seinen Sohn auf null setzen darf, der Rest der Verknüpfungen läuft
	 * dann ohne weitere Fragen.
	 * @return true, falls ausschneiden erfolgreich und gestattet
	 */
	public boolean cut();


	/**
	 * Entfernt den Knoten aus dem Baum. Das kann nur gehen, wenn rank()<2 ist,
	 * wenn also der Knoten höchstens einen Sohn hat. Dieser wird dann anstelle
	 * des Knotens an den Vater gehangen. Die Balancierungsdaten werden
	 * gelöscht.
	 * @return true, falls Entfernen gestattet und erfolgreich
	 */
	public boolean remove();


	/**
	 * Löscht alle Verbindungen vom TreeItem aus. Die Verbindungen werden also
	 * nicht korrekt entfernt. Es wird im Gegensatz zu remove nur eine Anfrage
	 * an den Besitzer geben! Der Besitzer bleibt bestehen. Die
	 * Balancierungsdaten werden ebenfalls gelöscht.
	 * @return true, falls erfolgreich
	 */
	public boolean clearConnections();
}
