package anja.swinggui.polygon;

import anja.geom.Polygon2;

import anja.geom.random.*;

import anja.swinggui.*;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;


/**
* Hierbei handelt es sich um ein grafisches Tool, das zur Erzeugung von
* verschiedenen Zufallspolygonen des Typs
* <a href=../../../anja/geom/random/RandomPolygon2.html
* ><code>RandomPolygon2</code></a> bzw. dessen abgeleiteten Typen dient. Zudem
* besteht die Moeglichkeit, das aktuell dargestellte Polygon in registrierte
* <a href=Polygon2ImportListener.html><code>Polygon2ImportListener</code></a>
* zu exportieren und dort weiterzuverwenden.
*
* @version 0.9 08.02.05
* @author Sascha Ternes
*/

public class RandomPolygon2Tool
   extends Polygon2SceneWorker
   implements ActionListener, WindowListener
{

   // *************************************************************************
   // Private constants
   // *************************************************************************
   
   // Titel des Fensters:
   private static final String _TITLE = "RandomPolygon2Tool";

   // Position und Abmessungen des Fensters:
   private static final int _POS_X  = 100;
   private static final int _POS_Y  = 100;
   private static final int _WIDTH  = 464;
   private static final int _HEIGHT = 402;


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // frame und content pane des Tools:
   private JFrame _frame;
   private Container _panel;

   // DisplayPanel fuer das Polygon:
   private DisplayPanel _display;

   // Liste der Import-Listener:
   private Vector _listeners;

   // Parameter-Felder:
   private JTextField _min_points;
   private JTextField _max_points;
   private JTextField _width;
   private JTextField _height;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Tool und startet es als unabhaengiges Fenster.
   */
   public RandomPolygon2Tool() {

      // Initialisiere Fenster:
      _frame = new JFrame( _TITLE );
      _panel = new JPanel();
      _panel.setLayout( new BorderLayout() );
      _frame.setContentPane( _panel );
      // Groesse und Position:
      _frame.setLocation( _POS_X, _POS_Y );
      _frame.setSize( _WIDTH, _HEIGHT );
      // Inhalt:
      _display = new DisplayPanel();
      _display.setScene( this );
      _panel.add( _display, BorderLayout.CENTER ); // DisplayPanel
      _panel.add( _createButtonsPanel(), BorderLayout.EAST ); // Buttons

      // fertig:
      _listeners = new Vector( 1, 1 ); // ein Listener sollte genuegen
      _frame.addWindowListener( this );
      _frame.setVisible( true );
      _setPolygon( new RandomPolygon2() ); // starte mit einem Zufallspolygon

   } // RandomPolygon2Tool


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Fuegt den angegebenen Listener zur Liste der Import-Listener hinzu und ruft
   * die Methode <code>instanceOpened(...)</code> des Listeners als
   * Bestaetigung auf.
   *
   * @param listener der Import-Listener; <code>null</code> wird ignoriert.
   */
   public void addImportListener(
      Polygon2ImportListener listener
   ) {

      // null abfangen:
      if ( listener != null ) {
         _listeners.add( listener ); // registrieren
         listener.instanceOpened( this ); // benachrichtigen
      } // if

   } // addImportListener


   /**
   * Entfernt den angegebenen Listener von der Liste der Import-Listener und
   * ruft die Methode <code>instanceClosed(...)</code> des Listeners als
   * Bestaetigung auf. Wenn der Listener nicht als Import-Listener registriert
   * ist, passiert nichts.
   *
   * @param listener der Import-Listener; <code>null</code> wird ignoriert.
   */
   public void removeImportListener(
      Polygon2ImportListener listener
   ) {

      // null abfangen:
      if ( listener != null ) {
         _listeners.remove( listener ); // entfernen
         listener.instanceClosed( this );
      } // if

   } // removeImportListener


   /**
   * Reaktiviert die Instanz dieses Tools.<p>
   *
   * <i>Achtung: Wenn das Tool zuvor geschlossen wurde, sind keine
   * <code>Polygon2ImportListener</code> mehr registriert, und die
   * Importfunktion ist ausser Betrieb, bis wieder mindestens ein Listener
   * per @see #addImportListener(Polygon2ImportListener) registriert wird.
   */
   public void reactivate() {

      _frame.setEnabled( true );
      _frame.setVisible( true );

   } // // reactivate


   /**
   * Reaktiviert die Instanz dieses Tools und registriert gleichzeitig einen
   * <code>Polygon2ImportListener</code>.<p>
   *
   * @param listener der zu registrierende Import-Listener; <code>null</code>
   *        wird ignoriert
   */
   public void reactivate(
      Polygon2ImportListener listener
   ) {

      reactivate();
      addImportListener( listener );

   } // // reactivate


   // *************************************************************************
   // Interface ActionListener
   // *************************************************************************

   /**
   * Wird aufgerufen, wenn der Import-Button gedrueckt wurde. Alle registrierten
   * Import-Listener werden benachrichtigt, dass ein Zufallspolygon zu
   * importieren ist.
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      // Parametereingaben ueberpruefen und uebernehmen:
      _updateParameters();

      // feststellen, was passieren soll:
      String what = e.getActionCommand();

      if ( what.equals( "poly_random" ) ) {
         _setPolygon( new RandomPolygon2() );
      } // if

      else if ( what.equals( "poly_convexbottom" ) ) {
        _setPolygon( new ConvexBottomPolygon2() );
      } // if

      else if ( what.equals( "poly_spacepart" ) ) {
         _setPolygon( new SpacePartPolygon2() );
      } // if

      else if ( what.equals( "poly_steadygrowth" ) ) {
         _setPolygon( new SteadyGrowthPolygon2() );
      } // if

      else if ( what.equals( "poly_2optmoves" ) ) {
         _setPolygon( new TwoOptMovesPolygon2() );
      } // if

      else if ( what.equals( "poly_2peasants" ) ) {
         _setPolygon( new TwoPeasantsPolygon2() );
      } // if

      else if ( what.equals( "import" ) ) {
         Enumeration enm = _listeners.elements();
         while ( enm.hasMoreElements() ) {
            // clone() wichtig, um das gleiche Polygon mehrmals zu importieren!
            ( (Polygon2ImportListener) enm.nextElement() ).importPolygon(
                 (Polygon2) getPolygon2Scene().getPolygon( 0 ).clone(), this );
         } // while
      } // if

      else if ( what.equals( "close" ) ) {
         windowClosing( null );
      } // if

   } // actionPerformed


   // *************************************************************************
   // Interface WindowListener
   // *************************************************************************

   /**
   * Beendet dieses Tool. Das Toolfenster wird unsichtbar gemacht, aber nicht
   * zerstoert, so dass die aktuelle Instanz des Tools spaeter durch Aufruf der
   * Methode @see #reactivate() weiterverwendet werden kann. Alle vormals
   * registrierten <code>Polygon2ImportListener</code> werden entfernt.
   *
   * @param e das <code>WindowEvent</code>-Objekt
   */
   public void windowClosing(
      WindowEvent e
   ) {
   
      // alle registrierten Listener benachrichtigen:
      Enumeration enm = _listeners.elements();
         while ( enm.hasMoreElements() ) {
            ( (Polygon2ImportListener) enm.nextElement() ).
                                        instanceClosed( this );
         } // while

      /* beendet die JVM:
      System.exit( 0 ); */

      // stattdessen:
      _listeners.clear();
      _frame.dispose();

   } // windowClosing;


   /**
   * Nicht verwendet.
   */
   public void windowActivated(WindowEvent e) {}
   /**
   * Nicht verwendet.
   */
   public void windowClosed(WindowEvent e) {}
   /**
   * Nicht verwendet.
   */
   public void windowDeactivated(WindowEvent e) {}
   /**
   * Nicht verwendet.
   */
   public void windowDeiconified(WindowEvent e) {}
   /**
   * Nicht verwendet.
   */
   public void windowIconified(WindowEvent e) {}
   /**
   * Nicht verwendet.
   */
   public void windowOpened(WindowEvent e) {}


   // ************************************************************************
   // Private methods
   // ************************************************************************

   /*
   * Aufbau des rechten Panels mit den Buttons.
   */
   private JPanel _createButtonsPanel() {

      // Panel:
      JPanel panel = new JPanel();
      GridBagLayout layout = new GridBagLayout();
      panel.setLayout( layout );
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbc.fill = GridBagConstraints.HORIZONTAL;

      // Button fuer Import:
      JButton button = new JButton( "Import" );
      button.setActionCommand( "import" );
      button.addActionListener( this );
      Insets old_insets = gbc.insets;
      gbc.insets = new Insets( 5, 0, 6, 0 );
      gbc.anchor = GridBagConstraints.NORTH;
      layout.setConstraints( button, gbc );
      panel.add( button );
      gbc.insets = old_insets;
      gbc.anchor = GridBagConstraints.CENTER;

      // Button fuer RandomPolygon2:
      button = new JButton( "Random" );
      button.setActionCommand( "poly_random" );
      button.addActionListener( this );
      layout.setConstraints( button, gbc );
      panel.add( button );
      // Button fuer ConvexBottomPolygon2:
      button = new JButton( "ConvexBottom" );
      button.setActionCommand( "poly_convexbottom" );
      button.addActionListener( this );
      layout.setConstraints( button, gbc );
      panel.add( button );
      // Button fuer SpacePartPolygon2:
      button = new JButton( "SpacePart" );
      button.setActionCommand( "poly_spacepart" );
      button.addActionListener( this );
      layout.setConstraints( button, gbc );
      panel.add( button );
      // Button fuer SteadyGrowthPolygon2:
      button = new JButton( "SteadyGrowth" );
      button.setActionCommand( "poly_steadygrowth" );
      button.addActionListener( this );
      layout.setConstraints( button, gbc );
      panel.add( button );
      // Button fuer TwoOptMovesPolygon2:
      button = new JButton( "2OptMoves" );
      button.setActionCommand( "poly_2optmoves" );
      button.addActionListener( this );
      layout.setConstraints( button, gbc );
      panel.add( button );
      // Button fuer TwoPeasantsPolygon2:
      button = new JButton( "2Peasants" );
      button.setActionCommand( "poly_2peasants" );
      button.addActionListener( this );
      layout.setConstraints( button, gbc );
      panel.add( button );

      // Abtrennung:
      JLabel dummy = new JLabel();
      gbc.insets = new Insets( 8, 0, 0, 0 );
      layout.setConstraints( dummy, gbc );
      panel.add( dummy );
      gbc.insets = old_insets;

      // Parameter:
      JLabel title = new JLabel( " Point # interval:" );
      layout.setConstraints( title, gbc );
      panel.add( title );
      _min_points = new JTextField( Integer.toString(
                                       RandomPolygon2.DefaultPolyMinPoints ) );
      _min_points.setHorizontalAlignment( JTextField.RIGHT );
      gbc.weightx = 1.0f;
      gbc.gridwidth = 1;
      layout.setConstraints( _min_points, gbc );
      panel.add( _min_points );
      _max_points = new JTextField( Integer.toString(
                                       RandomPolygon2.DefaultPolyMaxPoints ) );
      _max_points.setHorizontalAlignment( JTextField.RIGHT );
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      layout.setConstraints( _max_points, gbc );
      panel.add( _max_points );
      title = new JLabel( " max. Width / Height:" );
      gbc.weightx = 0.0f;
      layout.setConstraints( title, gbc );
      panel.add( title );
      _width = new JTextField( Integer.toString(
                                           RandomPolygon2.DefaultPolyWidth ) );
      _width.setHorizontalAlignment( JTextField.RIGHT );
      gbc.weightx = 1.0f;
      gbc.gridwidth = 1;
      layout.setConstraints( _width, gbc );
      panel.add( _width );
      _height = new JTextField( Integer.toString(
                                          RandomPolygon2.DefaultPolyHeight ) );
      _height.setHorizontalAlignment( JTextField.RIGHT );
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      layout.setConstraints( _height, gbc );
      panel.add( _height );

      // Abtrennung:
      dummy = new JLabel();
      gbc.gridheight = 3;
      gbc.weighty = 1.0f;
      layout.setConstraints( dummy, gbc );
      panel.add( dummy );
      gbc.gridheight = 1;
      gbc.weighty = 0.0f;

      // Button fuer Close:
      button = new JButton( "Close" );
      button.setActionCommand( "close" );
      button.addActionListener( this );
      gbc.anchor = GridBagConstraints.SOUTH;
      layout.setConstraints( button, gbc );
      panel.add( button );

      return panel; // fertig

   } // _createButtonsPanel


   /*
   * Ueberprueft die Parameter in den Eingabefeldern auf Validitaet, korrigiert
   * sie ggf. und speichert sie fuer das naechste Polygon.
   */
   private void _updateParameters() {

      // Test der Intervallgrenzen:
      int min = 0;
      int max = 0;
      try {
         min = Integer.parseInt( _min_points.getText() );
      } catch ( NumberFormatException nfe ) {} // try
      if ( min < 3 ) {
         min = RandomPolygon2.DefaultPolyMinPoints;
      } // if
      try {
         max = Integer.parseInt( _max_points.getText() );
      } catch ( NumberFormatException nfe ) {} // try
      if ( max < 3 ) {
         max = RandomPolygon2.DefaultPolyMaxPoints;
      } // if
      if ( min > max ) {
         int dummy = min;
         min = max;
         max = dummy;
      } // if
      // Korrektur der Intervallgrenzen:
      _min_points.setText( Integer.toString( min ) );
      _max_points.setText( Integer.toString( max ) );
      RandomPolygon2.DefaultPolyMinPoints = min;
      RandomPolygon2.DefaultPolyMaxPoints = max;

      // Test der Ausdehnung:
      int width = 0;
      int height = 0;
      try {
         width = Integer.parseInt( _width.getText() );
      } catch ( NumberFormatException nfe ) {} // try
      if ( width == 0 ) {
         width = RandomPolygon2.DefaultPolyWidth;
      } // if
      try {
         height = Integer.parseInt( _height.getText() );
      } catch ( NumberFormatException nfe ) {} // try
      if ( height == 0 ) {
         height = RandomPolygon2.DefaultPolyHeight;
      } // if
      // Korrektur der Ausdehnung:
      _width.setText( Integer.toString( width ) );
      _height.setText( Integer.toString( height ) );
      RandomPolygon2.DefaultPolyWidth = width;
      RandomPolygon2.DefaultPolyHeight = height;

   } // _updateParameters


   /*
   * Setzen des anzuzeigenden Polygons.
   */
   private void _setPolygon(
      RandomPolygon2 polygon
   ) {

      erasePolygons( ALL_POLYGONS );
      polygon.centerOnOrigin();
      addInteriorPolygon( polygon, false, true );

   } // _setPolygon


} // RandomPolygon2Tool
