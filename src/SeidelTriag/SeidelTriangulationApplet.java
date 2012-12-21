/* Anmerkung: Zu diesem Projekt gehoeren die Dateien:
 * SeidelTriangulationEditorExtension.java, SeidelTriangulationProg.java
 * und SeidelTriangulationApplet.java
 */

package SeidelTriag;

import anja.swinggui.StartButtonApplet;


/** Dieses Applet hat als einzige Aufgabe, einen Startbutton fuer die Klasse
  * MinkowSumConcave zu erzeugen. Es ist also nur ein Einsprungspunkt fuer
  * das eigentliche Programm.
  *
  * @author       Marina Bachran
  *
  * @see SeidelTriangulationEditorExtension SeidelTriangulationProg
  * SeidelTriangulationApplet
  *
  */
public class SeidelTriangulationApplet extends StartButtonApplet {

  /**
    * Startet das eigentliche Applet.
    */
  public SeidelTriangulationApplet() {
    super();
    addApplication( "appsSwingGui.SeidelTriag.SeidelTriangulationProg" );
  }

}
