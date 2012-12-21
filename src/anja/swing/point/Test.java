package anja.swing.point;

import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

import anja.swing.CoordinateSystem;
import anja.swing.Editor;
import anja.swing.JDisplayPanel;
import anja.swing.JPaintPriorityPanel;
import anja.swing.Register;


/**
* SwingTest
*
* @version 0.1 26.08.2004
* @author Sascha Ternes
*/

public class Test
extends JApplet
implements ActionListener,
           WindowListener
{

   // *************************************************************************
   // Private constants
   // *************************************************************************

   // das Register-Objekt:
   private Register _reg;

   // Koordinatensystem und Zeichenflaeche:
   private CoordinateSystem _cosystem;
   private JDisplayPanel _display;

   // die Szene:
   private PointScene _scene;
   // der Szeneneditor:
   private PointEditor _editor;

   // der Zeichenprioritaetsdialog:
   private JDialog _priority;


   // *************************************************************************
   // Class methods
   // *************************************************************************

   /**
   * Start als Applikation.
   *
   * @param args Kommandozeilenargumente - nicht verwendet
   */
   public static void main(
      String args[]
   ) {

      try {
         UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
      } catch ( Exception e ) {} // try
      JFrame applet_window = new JFrame( "Test of PointEditor" );
      applet_window.setLocation( 300, 200 );
      applet_window.setSize( 500, 350 );
      Test this_applet = new Test( applet_window );
      applet_window.addWindowListener( this_applet );
      applet_window.getContentPane().add( this_applet );
      applet_window.setVisible( true );

   } // main


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Konstruktor fuer die Ausfuehrung als Applet.
   */
   public Test() {

      try {
         UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
      } catch ( Exception e ) {} // try
      _reg = new Register();
      _cosystem = new CoordinateSystem( _reg, new Point( 0, 294 ), 10.0 );
      _display = new JDisplayPanel( _reg );
      _scene = new PointScene( _reg );
      _editor = new PointEditor( _reg );
      JMenuBar bar = _editor.createJMenuBar( this );
      bar.add( _editor.createSceneObjectListPanel() );
      setJMenuBar( bar );
      getContentPane().add( _display );

   } // Test


   /**
   * Konstruktor fuer die Ausfuehrung als Applikation.
   *
   * @param owner das uebergeordnete Fensterobjekt fuer Dialoge
   */
   public Test(
      JFrame owner
   ) {

      _reg = new Register();
      _cosystem = new CoordinateSystem( _reg, new Point( 0, 294 ), 10.0 );
      _display = new JDisplayPanel( _reg );
      _scene = new PointScene( _reg );
      _priority = JPaintPriorityPanel.createJDialog( owner, null, _scene );
      _priority.setLocation( owner.getX() + owner.getWidth(), owner.getY() );
      _editor = new PointEditor( _reg );
      _editor.setOwner( owner );
      _editor.includeOpenSaveMenuItems();
      _editor.includeExitMenuItem();
      _editor.includePaintPriorityMenuItem( _priority );
      JMenuBar bar = _editor.createJMenuBar( this );
      bar.add( _editor.createSceneObjectListPanel() );
      setJMenuBar( bar );
      getContentPane().add( _display );

   } // Test


   // *************************************************************************
   // Interface ActionListener
   // *************************************************************************

   /**
   * Listener fuer die eigenen Menuepunkte.
   *
   * @param e das <code>ActionEvent</code>-Objekt
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      String cmd = e.getActionCommand();
      if ( cmd.equals( Editor.MENU_FILE_EXIT ) )
         windowClosing( null );
      else if ( cmd.equals( Editor.MENU_SCENE_PRIO ) )
         _priority.setVisible( ! _priority.isVisible() );

   } // actionPerformed


   // *************************************************************************
   // Interface WindowListener
   // *************************************************************************

   /**
   * Beendet die Applikation.
   *
   * @param e das <code>WindowEvent</code>-Objekt
   */
   public void windowClosing(
      WindowEvent e
   ) {

      System.exit( 0 );

   } // windowClosing


   // die folgende Methoden werden nicht verwendet:
   public void windowActivated(WindowEvent e) {}
   public void windowClosed(WindowEvent e) {}
   public void windowDeactivated(WindowEvent e) {}
   public void windowDeiconified(WindowEvent e) {}
   public void windowIconified(WindowEvent e) {}
   public void windowOpened(WindowEvent e) {}


} // Test
