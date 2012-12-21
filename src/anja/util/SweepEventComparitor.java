package anja.util;

/**<p>
* Ein Sweep-Comparitor vergleicht SweepEvents mit Hilfe eines anderen Comparitors (der zumindest
* KeyValueHolder-Objekte vergleichen kann). Ergibt der Vergleich EQUAL, dann wird zusaetzlich
* die Sweep-Event-ID zum Vergleich herangezogen: Ereignisse mit hoeheren ID-Zahlen werden 
* als groesser betrachtet. So kann man eine Ordnung verschiedener Sweep-Ereignisse bei ansonsten
* gleichen Schluesseln erreichen.
*
* @author Thomas Wolf
* @version 1.0
*/
public class SweepEventComparitor implements Comparitor {

	/** Der Original-Comparitor. */
	private Comparitor _comparitor;

	/**
	* Konstruiert einen neuen SweepEventComparitor basierend auf dem Comparitor
	* compartitor.
	* @param comparitor Comparitor, dessen Vergleichsroutine benutzt werden soll
	*/
	public SweepEventComparitor(Comparitor comparitor) {
		_comparitor=comparitor;
	}
 /**
 * Ueberschreibt Object.toString().
 * @see java.lang.Object#toString
 */
 public String toString() {
		return getClass().getName()+"[comparitor="+_comparitor+"]";
	}
	/**
	* Liefert den Comparitor, der die hauptsaechliche Vergleichsarbeit leistet.
	* @return verknuepftes Comparitor-Objekt
	*/
	public Comparitor comparitor() {
		return _comparitor;
	}
 /**
 * Vergleicht die beiden Objekte o1 und o2 bezueglich einer Ordnung.
 * @param o1 und
 * @param o2 Objekte, die verglichen werden sollen.
 * @return eine der Vergleichskonstanten dieses Interfaces.
 */
 public short compare(Object o1,Object o2) {
		if (_comparitor==null) throw new CompareException(this,o1,o2);
		short comp=_comparitor.compare(o1,o2);
		if (comp!=EQUAL) return comp;
		if ((o1 instanceof SweepEvent) && (o2 instanceof SweepEvent)) {
			SweepEvent s1=(SweepEvent)o1,s2=(SweepEvent)o2;
			if (s1.getID()==s2.getID()) return EQUAL;
			if (s1.getID()<s2.getID()) return SMALLER; else return BIGGER;
		} else return comp;
	}


}
