package anja.swing;

import java.awt.Graphics;
import java.awt.Point;

import java.awt.geom.AffineTransform;

import javax.swing.JMenu;


/**
* Eine Editorerweiterung dient zum einfachen Hinzufuegen von Funktionalitaeten
* zu einem Editor.
*
* @version 0.7 25.08.2004
* @author Sascha Ternes
*/

public abstract class EditorExtension {

   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * das Register der Komponenten
   */
   protected Register _reg;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /*
   * Verbotener Konstruktor.
   */
   private EditorExtension() {}


   /**
   * Erzeugt eine neue Editorerweiterung. Diese registriert sich im uebergebenen
   * Register-Objekt.
   *
   * @param register das Register-Objekt
   */
   public EditorExtension(
      Register register
   ) {

      register.extension = this;
      _reg = register;

   } // EditorExtension


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Wird aufgerufen, bevor die Szene gezeichnet wird, um der Erweiterung
   * Gelegenheit zum Zeichnen in die Zeichenflaeche zu geben.<p>
   *
   * Mehr Informationen zur Vorgehensweise gibt es bei der Beschreibung der
   * Methode {@link Scene#paint(Graphics,AffineTransform) Scene.paint}.<p>
   *
   * Die Methode ist in dieser Klasse leer implementiert.
   * @param g das <code>Graphics</code>-Objekt, in dem mit Pixelkoordinaten
   *          gezeichnet werden kann
   * @param transform die Transformationsmatrix fuer das Zeichnen mit
   *        Weltkoordinaten
   * @see Scene#paint(Graphics,AffineTransform)
   */
   public void paintFirst(
      Graphics g,
      AffineTransform transform
   ) {

      return;

   } // paintFirst


   /**
   * Wird aufgerufen, nachdem die Szene gezeichnet wurde, um der Erweiterung
   * Gelegenheit zum Zeichnen in die Zeichenflaeche zu geben.<p>
   *
   * Mehr Informationen zur Vorgehensweise gibt es bei der Beschreibung der
   * Methode {@link Scene#paint(Graphics,AffineTransform) Scene.paint}.<p>
   *
   * Die Methode ist in dieser Klasse leer implementiert.
   * @param g das <code>Graphics</code>-Objekt, in dem mit Pixelkoordinaten
   *          gezeichnet werden kann
   * @param transform die Transformationsmatrix fuer das Zeichnen mit
   *        Weltkoordinaten
   * @see Scene#paint(Graphics,AffineTransform)
   */
   public void paintLast(
      Graphics g,
      AffineTransform transform
   ) {

      return;

   } // paintLast


   /**
   * In dieser Methode kann die Erweiterung ein eigenes Menue definieren, das
   * in das Menue des Editors eingebaut wird.<p>
   *
   * Falls kein eigenes Menue verwendet werden soll, muss diese Methode
   * <code>null</code> zurueckliefern (so ist diese Methode auch in dieser
   * Klasse implementiert).
   *
   * @return das Menue oder <code>null</code>, wenn kein Menue definiert wird
   */
   public JMenu createExtensionMenu() {

      return null;

   } // createExtensionMenu


   /**
   * Reagiert auf einen Mausklick im freien Bereich des Zeichenfensters.<p>
   *
   * Wenn das Ereignis von dieser Erweiterung behandelt wurde und nicht vom
   * Editor behandelt werden soll, muss diese Methode den Wert
   * <code>true</code> liefern, sonst <code>false</code>.<p>
   *
   * Diese Methode ist leer implementiert und liefert den Wert
   * <code>false</code>.
   *
   * @param point der Punkt des Mausklicks in Weltkoordinaten
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   */
   public boolean canvasClicked(
      Point point,
      int button
   ) {

      return false;

   } // canvasClicked


   /**
   * Bietet dem Editor die Moeglichkeit, nach einem Scrollen der Zeichenflaeche
   * Aktionen auszufuehren.
   *
   * Wenn das Ereignis von dieser Erweiterung behandelt wurde und nicht vom
   * Editor behandelt werden soll, muss diese Methode den Wert
   * <code>true</code> liefern, sonst <code>false</code>. Allerdings sollte
   * dieses Ereignis nur in Ausnahmefaellen dem Editor vorenthalten werden.<p>
   *
   * Diese Methode ist leer implementiert und liefert den Wert
   * <code>false</code>.
   *
   * @param point die aktuelle Position des Mauscursors
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   */
   public boolean canvasScrolled(
      Point point
   ) {

      return false;

   } // canvasScrolled


   /**
   * Bietet dem Editor die Moeglichkeit, nach einem Zoomen der Zeichenflaeche
   * Aktionen auszufuehren.
   *
   * Wenn das Ereignis von dieser Erweiterung behandelt wurde und nicht vom
   * Editor behandelt werden soll, muss diese Methode den Wert
   * <code>true</code> liefern, sonst <code>false</code>. Allerdings sollte
   * dieses Ereignis nur in Ausnahmefaellen dem Editor vorenthalten werden.<p>
   *
   * Diese Methode ist leer implementiert und liefert den Wert
   * <code>false</code>.
   *
   * @param point die aktuelle Position des Mauscursors
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   */
   public boolean canvasZoomed(
      Point point
   ) {

      return false;

   } // canvasZoomed


   /**
   * Reagiert auf einen Mausklick auf ein Szenenobjekt des Zeichenfensters.
   *
   * Wenn das Ereignis von dieser Erweiterung behandelt wurde und nicht vom
   * Editor behandelt werden soll, muss diese Methode den Wert
   * <code>true</code> liefern, sonst <code>false</code>.<p>
   *
   * Diese Methode ist leer implementiert und liefert den Wert
   * <code>false</code>.
   *
   * @param object das angeklickte Szenenobjekt
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   */
   public boolean objectClicked(
      SceneObject object,
      Point point
   ) {

      return false;

   } // objectClicked


   /**
   * Reagiert auf einen Kontextmenue-Mausklick auf ein Szenenobjekt des
   * Zeichenfensters.
   *
   * Wenn das Ereignis von dieser Erweiterung behandelt wurde und nicht vom
   * Editor behandelt werden soll, muss diese Methode den Wert
   * <code>true</code> liefern, sonst <code>false</code>.<p>
   *
   * Diese Methode ist leer implementiert und liefert den Wert
   * <code>false</code>.
   *
   * @param object das angeklickt Szenenobjekt
   * @param point die Pixelposition des Klickpunkts
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   */
   public boolean objectContext(
      SceneObject object,
      Point point
   ) {

      return false;

   } // objectContext


   /**
   * Reagiert auf das Bewegen des Mauscursors ueber die Zeichenflaeche.
   *
   * Wenn das Ereignis von dieser Erweiterung behandelt wurde und nicht vom
   * Editor behandelt werden soll, muss diese Methode den Wert
   * <code>true</code> liefern, sonst <code>false</code>. Allerdings sollte
   * dieses Ereignis nur in Ausnahmefaellen dem Editor vorenthalten werden.<p>
   *
   * Diese Methode ist leer implementiert und liefert den Wert
   * <code>false</code>.
   *
   * @param point die Position des Mauscursors in Weltkoordinaten
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   */
   public boolean cursorMoved(
      Point point
   ) {

      return false;

   } // cursorMoved


   /**
   * Reagiert auf das Verschieben eines Szenenobjekts im Zeichenfenster.
   *
   * Wenn das Ereignis von dieser Erweiterung behandelt wurde und nicht vom
   * Editor behandelt werden soll, muss diese Methode den Wert
   * <code>true</code> liefern, sonst <code>false</code>.<p>
   *
   * Diese Methode ist leer implementiert und liefert den Wert
   * <code>false</code>.
   *
   * @param object das verschobene Szenenobjekt
   * @param point die Position des Mauscursors in Weltkoordinaten
   * @param button die Maustaste, die waehrend des Verschiebens gedrueckt ist
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   */
   public boolean objectDragged(
      SceneObject object,
      Point point,
      int button
   ) {

      return false;

   } // objectDragged


} // EditorExtension
