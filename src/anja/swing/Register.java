package anja.swing;


/**
* Ein Objekt dieser Klasse dient als zentrale Registrierstelle fuer die
* Komponenten eines Editorsystems.
*
* @version 0.9 25.08.2004
* @author Sascha Ternes
*/

public class Register
{

   // *************************************************************************
   // Public variables
   // *************************************************************************

   /**
   * das Koordinatensystem
   */
   public CoordinateSystem cosystem;

   /**
   * das Anzeigepanel
   */
   public JDisplayPanel display;

   /**
   * der Editor
   */
   public Editor editor;

   /**
   * die Editorerweiterung
   */
   public EditorExtension extension;

   /**
   * die Szene
   */
   public Scene scene;


} // Register
