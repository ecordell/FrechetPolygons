package SeidelTriag;

import java.awt.BorderLayout;
import java.awt.Container;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;

// import anja.geom.Polygon2;

import anja.swinggui.BufferedCanvas;
import anja.swinggui.DisplayPanel;
import anja.swinggui.polygon.Extendable;
import anja.swinggui.polygon.ExtendablePolygonEditor;



/**
* Testprogramm für Polygon-Triangulation.
*
* @version 0.1 20.06.2003
* @author Sascha Ternes
*/

public class TestTriangulation
 extends     JApplet        //Swing-Applet!
 implements  WindowListener
{

   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Position und Abmessungen des Hauptfensters beim Start als Applikation:
   private static final int _DEFAULT_POS_X  = 200;
   private static final int _DEFAULT_POS_Y  = 100;
   private static final int _DEFAULT_WIDTH  = 480;
   private static final int _DEFAULT_HEIGHT = 360;


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // content pane des Applets:
   private Container _panel;

   // Editor mit Extension und DisplayPanel für das Polygon:
   private ExtendablePolygonEditor _editor;
   private Extendable _extension;
   private DisplayPanel _display;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Der Konstruktor erzeugt einen PolygonEditor mit Extension; dieser wird dem
   * DisplayPanel unterstellt.
   */
   public TestTriangulation() {

      // registriere content pane des Applets zur Vereinfachung:
      _panel = this.getContentPane();

      // Initialisierungen:
      _display = new DisplayPanel();
      _extension = new TestTriangulationExtension();
      _editor = new ExtendablePolygonEditor( _extension );
      _display.setScene( _editor );

   } // TestTriangulation


   // *************************************************************************
   // Class methods
   // *************************************************************************

   /**
   * Hauptprogramm für die Ausführung als Applikation statt als Applet.
   */
   public static void main(
      String args[]
   ) {

      TestTriangulation this_applet = new TestTriangulation();

      // baue Applet in ein JFrame ein:
      JFrame applet_window = new JFrame( "Polygon triangulation test" );
      applet_window.setLocation( _DEFAULT_POS_X, _DEFAULT_POS_Y );
      applet_window.setSize( _DEFAULT_WIDTH, _DEFAULT_HEIGHT );
      applet_window.addWindowListener( this_applet );
      applet_window.getContentPane().add( this_applet );

      // starte Applet und zeige es an:
      this_applet.init();
      applet_window.setVisible( true );
      this_applet.start();

   } // main


   // #########################################################################

   /**
   * Initialisiert die Zeichenfläche und registriert den Mausevent-Handler.
   */
   public void init() {

      // registriere Maus-Listener des Editors beim DisplayPanel:
      BufferedCanvas b_canvas = _display.getCanvas();
      b_canvas.addMouseListener( _editor );
      b_canvas.addMouseMotionListener( _editor );

      // initialisiere Zeichenfläche:
      _panel.add( _display, BorderLayout.CENTER );

   } // init


   // *************************************************************************

   public void start() {

      /*
      Polygon2 poly = new Polygon2();
      poly.addPoint( -120, 60 );
      poly.addPoint( 40, -100 );
      poly.addPoint( 80, 100 );
      poly.addPoint( -20, 20 );
      _editor.getPolygon2Scene().add( poly );
      _editor.refresh();
      */

   } // start


   // *************************************************************************

   public void stop() {} // stop


   // *************************************************************************

   public void destroy() {} // destroy


   // *************************************************************************
   // Interface WindowListener
   // *************************************************************************

   public void windowClosing(
      WindowEvent e
   ) {

      System.exit( 0 );

   } // windowClosing;

   // folgende Methoden werden nicht verwendet:
   public void windowActivated(WindowEvent e) {}
   public void windowClosed(WindowEvent e) {}
   public void windowDeactivated(WindowEvent e) {}
   public void windowDeiconified(WindowEvent e) {}
   public void windowIconified(WindowEvent e) {}
   public void windowOpened(WindowEvent e) {}


   // #########################################################################

} // TestTriangulation
