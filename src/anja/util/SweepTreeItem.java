package anja.util;

/**
* Ein TreeItem fuer einen Binaeren Baum. Es implementiert SweepEvent und ist somit
* geeignet fuer die Speicherung von Sweep-Ereignissen.
*
* @author Thomas Wolf
* @version 1.0
*/
public class SweepTreeItem extends StdTreeItem implements SweepEvent {

	/** Speichert die ID des Events. */
	private int _id;

 /**
 * Konstruktor mit Wertinitialisierung.
	* @param id ID-Wert des Sweep-Events
 * @param key neuer Schluessel
 * @param value neuer Wert
 */
 public SweepTreeItem(int id,Object key,Object value) {
		super(2,key,value);
		_id=id;
 }

 /**
 * Ueberschreibt Object.toString().
 */
 public String toString() {
  return getClass().getName()+"[id="+_id+",key="+key()+",value="+value()+"]";
 }

	/**
	* Liefert die Event-ID zurueck.
	* @return ID
	*/
	public int getID() {
		return _id;
	}


}
