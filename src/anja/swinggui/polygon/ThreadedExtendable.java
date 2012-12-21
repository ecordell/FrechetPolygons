/**
 * 
 */
package anja.swinggui.polygon;


import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import anja.geom.Point2;
import anja.geom.Polygon2Scene;


/**
 * Dieses Interface fügt dem Extendable-Interface zwei Methoden hinzu, um länger
 * andauernde Berechnungen in einen Thread auszulagern, so dass die Bedienung
 * eines Java-Applets während der Berechnungen nicht unterbrochen wird.
 * 
 * Dazu werden die Methoden calculateData(), paintData und stop() eingeführt.
 * Dadurch wird das Einzeichnen der berechneten Daten von der eigentlichen
 * Berechnung getrennt, da ausserhalb der paint-Methode in einem Applet nichts
 * gezeichnet werden kann:
 * 
 * paint() übernimmt das Berechnen und Einzeichnen von einfacheren Sachen, die
 * etwa innerhalb einer Zehntelsekunde erledigt werden können.
 * 
 * paintData() zeichnet die in calculateData berechneten Daten ein. paintData()
 * wird nur aufgerufen, wenn calculateData() erfolgreich beendet wurde.
 * 
 * calculateData() übernimmt die aufwändigeren Berechnungen und ist mit der
 * run()-Methode eines Threads zu vergleichen. Während der Berechnungen muss
 * regelmässig überprüft werden, ob die Berechnung vorzeitig abgebrochen werden
 * soll.
 * 
 * stop() soll die Berechnungen vorzeitig abbrechen. Dazu sollte eine Variable
 * gesetzt werden, die regelmässig von der calculateData()-Methode überprüft
 * wird.
 * 
 * Der Editor zeigt eine Leiste mit dem Fortschritt der Berechnungen an. Dazu
 * ruft er die Methode getCalculationPercent() auf, die eine Zahl zwischen 1 und
 * 100 zurückgeben sollte, den Berechnungsfortschritt in Prozent.
 * 
 * Eine minimale Implementation dieses Interfaces würde also so aussehen:
 * 
 * private boolean _abort = false; //zeigt an, ob die run()-Methode abgebrochen
 * werden soll
 * 
 * public void paint(Polygon2Scene scene, Graphics2d g2d) { //Mache kleinere
 * Berechnungen und zeichne sie ein ... }
 * 
 * public void paintData(Polygon2Scene scene, Graphics2D g2d) { //Zeichne die in
 * calculateData Daten }
 * 
 * public void calculateData(Polygon2Scene scene) { _abort = false; while
 * (!_abort) { //führe Berechnungen aus } }
 * 
 * public int getCalculationPercent() { return -1; }
 * 
 * public void stop() { _abort = true; }
 * 
 * public void resumeCalculation() { _abort = false; }
 * 
 */

public interface ThreadedExtendable
		extends Extendable
{

	/**
	 * Diese Methode wird vom PolygonEditor als Thread aufgerufen und sollte
	 * alle rechenintensiven Aufgaben übernehmen. Wird die Berechnung nicht
	 * vorzeitig abgebrochen, wird automatisch ein Neuzeichnen der Szene
	 * ausgelöst, so dass die Ergebnisse innerhalb der Paint-Methode
	 * eingezeichnet werden können.
	 * 
	 * Diese Methode sollte regelmässig eine selbst definierte Variable
	 * überprüfen, die signalisiert, wenn der Thread (z.B. durch Änderung der
	 * Polygonszene) vorzeitig abgebrochen werden soll und die Berechnung dann
	 * beenden.
	 * 
	 * run() wird nur ausgeführt, wenn sich die Polygonszene geändert hat.
	 * 
	 */
	public void calculateData(
			Polygon2Scene scene);


	/**
	 * Den Rückgabewert dieser Methode benutzt der Editor, um anzuzeigen, wie
	 * weit die Berechnungen sind. Ein negativer Wert gibt an, dass die
	 * Berechnungen entweder beendet sind oder keine Prozentanzeige unterstützt
	 * wird.
	 * @return Stand der Berechnungen in %, Zahl zwischen 1 und 100
	 */
	public int getCalculationPercent();


	/**
	 * Diese Methode wird aufgerufen, wenn der Thread vorzeitig abgebrochen
	 * werden soll. Zur Implementierung sollte diese Methode eine Variable
	 * ändern, die regelmässig von der run-Methode überprüft wird.
	 */
	public void halt();


	/**
	 * Diese Methode wird vor dem Neustart des Threads aufgerufen (nachdem
	 * dieser abgebrochen wurde). Dadurch wird die Variable wieder umgesetzt,
	 * sodass die Berechnung fortgesetzt werden kann.
	 */
	public void prepareCalculation();


	/**
	 * Diese Methode übernimmt das Einzeichnen der von calculateData()
	 * berechneten Daten. Wird nur aufgerufen, wenn der letzte Durchlauf von
	 * calculateData erfolgreich beendet wurde.
	 */
	public void paintData(
			Polygon2Scene scene,
			Graphics2D g2d);

}
