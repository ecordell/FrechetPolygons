package anja.swinggui.polygon;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import anja.geom.Point2;
import anja.geom.Polygon2Scene;


/**
* Dieses Interface definiert die für Editor-Extensions notwendigen Methoden.
*
* @version      1.0 09.03.02
* @author       Sascha Ternes
*/

public interface Extendable
{

   // *************************************************************************
   // Interface-Methoden
   // *************************************************************************

   /**
   * Veranlasst das Zeichnen automatisch berechneter Grafikobjekte.
   * Diese Methode wird von einem PolygonEditor aufgerufen, um das Einzeichnen
   * automatisch berechneter Grafikobjekte in die Polygonszene zu veranlassen.
   */
   public void paint( Polygon2Scene scene, Graphics2D g2d );

   /**
   * Bietet die Möglichkeit, am Anfang des Popup-Kontextmenüs im Polygoneditor
   * zusätzliche Menüpunkte einzufügen.
   * Diese Methode muss nach folgendem Schema implementiert werden:<br><code>
   * ...<br>
   * // folgende Variable wird durch die Methode
   * {@link anja.swinggui.polygon.Extendable#registerPolygonEditor(anja.swinggui.polygon.ExtendablePolygonEditor)}<br>
   * // beim Initialisieren des aufrufenden PolygonEditors gesetzt:<br>
   * PolygonEditor der_aufrufende_Polygoneditor;<br>
   * ...<br>
   * JMenuItem item1 = new JMenuItem( "Menuepunkt 1" );<br>
   * item1.addActionListener( der_aufrufende_PolygonEditor );<br>
   * menu.add( item1 ); // fügt den Menüpunkt 1 hinzu<br>
   * menu.addSeparator(); // fügt eine Trennlinie hinzu</code>
   *
   * @param menu das Kontextmenü des Editors
   */
   public void popupMenu( JPopupMenu menu );
   
   /**
   * Diese Methode bietet die Möglichkeit, die Benutzerauswahl im Kontextmenü
   * abzufangen, bevor der Editor seine eigene Methode dazu ausführt.
   * Wenn diese Methode <code>false</code> zurückliefert, wird die Ausführung
   * der editoreigenen process-Methode unterbunden, bei Rückgabe von
   * <code>true</code> reagiert der Editor nach Abarbeitung dieser Methode
   * normal auf die Auswahl im Kontextmenü (jedoch nicht auf die Auswahl
   * zusätzlicher Menüpunkte, die durch die Methode
   * {@link anja.swinggui.polygon.Extendable#popupMenu(javax.swing.JPopupMenu)}
   * definiert wurden).
   *
   * @param e das ActionEvent-Objekt, welches das Kontextmenü-Ereignis enthält
   * @param point die Weltkoordinaten-Position des Mauszeigers vor dem Öffnen
   *              des Kontextmenüs
   * @return <code>true</code> wenn die Ereignisverarbeitung des Editors
   *         erfolgen soll <br>
   *         <code>false</code> wenn diese Methode die Kontextmenü-Auswahl
   *         komplett abfangen soll
   */
   public boolean processPopup( ActionEvent e, Point2 point );
   
   /**
   * Diese Methode wird vom Editor aufgerufen, wenn ein mouseDragged-Ereignis
   * aufgetreten ist, um der Editorerweiterung eine eigene Reaktion auf dieses
   * Ereignis zu ermöglichen. Beispielsweise könnte die Erweiterung hiermit
   * eigene zusätzliche Grafikobjekte in der Editor-Zeichenfläche verschieben.
   * <br>Die aufrufende Editor-Klasse
   * {@link anja.swinggui.polygon.ExtendablePolygonEditor}
   * kümmert sich dabei automatisch um das Neuzeichnen der Szene, indem sie die
   * Methode 
   * {@link anja.swinggui.polygon.Extendable#paint(anja.geom.Polygon2Scene, java.awt.Graphics2D)}
   * in diesem Interface aufruft.<br>
   * Wenn die Erweiterung nicht das mouseDragged-Ereignis behandeln möchte,
   * sollte sie <code>true</code> zurückliefern, um dem Editor selbst die
   * Ereignisbehandlung zu ermöglichen. Bei Rückgabe von <code>false</code>
   * wird die Ereignisbehandlung von der Erweitung beendet.
   *
   * @param e das MouseEvent-Objekt des mouseDragged-Ereignisses
   * @param point die Weltkoordinaten-Position des Mauszeigers beim Auslösen
   *              des mouseDragged-Ereignisses
   * @return <code>true</code> falls der Editor das mouseDragged-Ereignis
   *         behandeln soll<br>
   *         <code>false</code> falls die Ereignisbehandlung abgebrochen
   *         werden soll
   */
   public boolean processMouseDragged( MouseEvent e, Point2 point );
   
   /**
   * Diese Methode wird vom Editor aufgerufen, wenn ein mousePressed-Ereignis
   * aufgetreten ist, um der Editorerweiterung eine eigene Reaktion auf dieses
   * Ereignis zu ermöglichen. Beispielsweise könnte die Erweiterung hiermit
   * eine eigene Drag-Phase starten, um eigene Grafikobjekte in der Szene zu
   * verschieben.<br>
   * Die aufrufende Editor-Klasse
   * {@link anja.swinggui.polygon.ExtendablePolygonEditor}
   * kümmert sich dabei automatisch um das Neuzeichnen der Szene, indem sie die
   * Methode 
   * {@link anja.swinggui.polygon.Extendable#paint(anja.geom.Polygon2Scene, java.awt.Graphics2D)}
   * in diesem Interface aufruft.<br>
   * Wenn die Erweiterung nicht das mousePressed-Ereignis behandeln möchte,
   * sollte sie <code>true</code> zurückliefern, um dem Editor selbst die
   * Ereignisbehandlung zu ermöglichen. Bei Rückgabe von <code>false</code>
   * wird die Ereignisbehandlung von der Erweitung beendet.
   *
   * @param e das MouseEvent-Objekt des mousePressed-Ereignisses
   * @param point die Weltkoordinaten-Position des Mauszeigers beim Auslösen
   *              des mousePressed-Ereignisses
   * @return <code>true</code> falls der Editor das mousePressed-Ereignis
   *         behandeln soll<br>
   *         <code>false</code> falls die Ereignisbehandlung abgebrochen
   *         werden soll
   */
   public boolean processMousePressed( MouseEvent e, Point2 point );

   /**
   * Diese Methode wird vom Editor aufgerufen, wenn ein mouseReleased-Ereignis
   * aufgetreten ist, um der Editorerweiterung eine eigene Reaktion auf dieses
   * Ereignis zu ermöglichen. Beispielsweise könnte die Erweiterung hiermit
   * eine eigene, zuvor gestartete Drag-Phase korrekt beenden.<br>
   * Die aufrufende Editor-Klasse
   * {@link anja.swinggui.polygon.ExtendablePolygonEditor}
   * kümmert sich dabei automatisch um das Neuzeichnen der Szene, indem sie die Methode 
   * {@link anja.swinggui.polygon.Extendable#paint(anja.geom.Polygon2Scene, java.awt.Graphics2D)}
   * in diesem Interface aufruft.<br>
   * Wenn die Erweiterung nicht das mouseReleased-Ereignis behandeln möchte,
   * sollte sie <code>true</code> zurückliefern, um dem Editor selbst die
   * Ereignisbehandlung zu ermöglichen. Bei Rückgabe von <code>false</code>
   * wird die Ereignisbehandlung von der Erweitung beendet.
   *
   * @param e das MouseEvent-Objekt des mouseReleased-Ereignisses
   * @param point die Weltkoordinaten-Position des Mauszeigers beim Auslösen
   *              des mouseReleased-Ereignisses
   * @return <code>true</code> falls der Editor das mouseReleased-Ereignis
   *         behandeln soll<br>
   *         <code>false</code> falls die Ereignisbehandlung abgebrochen
   *         werden soll
   */
   public boolean processMouseReleased( MouseEvent e, Point2 point );

   /**
   * Diese Methode wird vom Konstruktor der Editor-Klasse
   * {@link anja.swinggui.polygon.ExtendablePolygonEditor}
   * aufgerufen, der eine Referenz auf den Editor an die Extension übergibt.
   * Diese Referenz muss z.B. als ActionListener in den zusätzlichen
   * Kontextmenüpunkten eingetragen werden, wozu diese Methode gedacht ist.
   *
   * @param editor eine Referenz auf den aufrufenden PolygonEditor
   */
   public void registerPolygonEditor( ExtendablePolygonEditor editor );


} // Extendable
