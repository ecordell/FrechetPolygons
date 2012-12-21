package anja.util;

/**<p>
* Ein Sweep-Ereignis. Ausser dem Schluessel-Wert-Paar kann ein Sweep-Event noch
* eine ID speichern.
*
* @author Thomas Wolf
* @version 1.0
*/
public interface SweepEvent extends KeyValueHolder {

	/**
	* Liefert die Event-ID zurueck.
	* @return ID
	*/
	public int getID();

}
