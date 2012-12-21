package anja.util;

/**
* SweepListener dient als Alternative zum Ueberschreiben der processEvent-Methode der Sweep-Klasse.
* Eine Klasse, die SweepListener implementiert, kann direkt ein Sweep-Objekt erzeugen, sich selbst
* bei diesem mittels setSweepListener registrieren lassen und bei ihm execute aufrufen. Somit wird
* dann die processEvent-Methode dieses Objektes aufgerufen.
*
* @see Sweep
* @see SweepEvent
*
* @author Thomas Wolf
* @version 1.0
*/
public interface SweepListener {

	/**
	* Verarbeitet das SweepEvent e.
	* @param e SweepEvent
	*/
	public void processEvent(SweepEvent e);

}
