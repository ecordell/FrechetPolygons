package anja.swinggui.polygon;

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
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;

import anja.geom.Point2;
import anja.geom.Polygon2;

import anja.swinggui.*;


/**
* Hierbei handelt es sich um ein grafisches Tool, mit dem Punkte und Polygone
* per Handeingabe ueber Koordinaten in den Polygoneditor eingefuegt werden
* koennen.
*
* @version 0.1 08.02.05
* @author Sascha Ternes
*/

public class PointInsertionTool
extends JFrame
implements ActionListener, WindowListener
{

   // *************************************************************************
   // Private constants
   // *************************************************************************
   
   // Titel des Fensters:
   private static final String _TITLE = "PointInsertionTool";

   // Position und Abmessungen des Fensters:
   private static final int _POS_X  = 120;
   private static final int _POS_Y  = 120;
   private static final int _WIDTH  = 350;
   private static final int _HEIGHT = 150;


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // content pane des Tools:
   private Container _panel;

   // DisplayPanel fuer das Polygon:
   private DisplayPanel _display;

   // Liste der Import-Listener:
   private Vector _listeners;

   // Parameter-Felder und Optionen:
   private JTextField _pointlist;
   private JCheckBox _opt_close;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Tool und startet es als unabhaengiges Fenster.
   */
   public PointInsertionTool() {

      // Initialisiere Fenster:
      super( _TITLE );
      _panel = getContentPane();
      _panel.setLayout( new BorderLayout() );
      // Groesse und Position:
      setLocation( _POS_X, _POS_Y );
      setSize( _WIDTH, _HEIGHT );
      // Inhalt:
      _panel.add( _createInputPanel(), BorderLayout.NORTH ); // Punkteingabe
      _panel.add( _createOptionsPanel(), BorderLayout.CENTER ); // Optionen
      _panel.add( _createButtonsPanel(), BorderLayout.SOUTH ); // Buttons

      // fertig:
      _listeners = new Vector( 1, 1 ); // ein Listener sollte genuegen
      addWindowListener( this );
      setVisible( true );

   } // PointInsertionTool


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

      setEnabled( true );
      setVisible( true );

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
   * Import-Listener werden benachrichtigt, dass etwas zu importieren ist.
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      // feststellen, was passieren soll:
      String what = e.getActionCommand();

      if ( what.equals( "import" ) ) {
         Enumeration enm = _listeners.elements();
         while ( enm.hasMoreElements() ) {
            Polygon2ImportListener listener =
                                   (Polygon2ImportListener) enm.nextElement();
            Polygon2 poly = _createPolygon();
            if ( poly != null )
               listener.importPolygon( poly, this );
         } // while
      } else if ( what.equals( "quit" ) )
         windowClosing( null );

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

      _listeners.clear();
      dispose();

   } // windowClosing;


   // Folgende Methoden werden nicht verwendet:
   public void windowActivated(WindowEvent e) {}
   public void windowClosed(WindowEvent e) {}
   public void windowDeactivated(WindowEvent e) {}
   public void windowDeiconified(WindowEvent e) {}
   public void windowIconified(WindowEvent e) {}
   public void windowOpened(WindowEvent e) {}


   // ************************************************************************
   // Private methods
   // ************************************************************************

   /*
   * Aufbau des oberen Panels mit der Eingabe.
   */
   private JPanel _createInputPanel() {

      // Panel:
      JPanel panel = new JPanel( new BorderLayout() );

      // Eingabefeld:
      panel.add( new JLabel(
                 "Enter space-separated points, e.g. 1.5/2 0.1,-1.2 -4;6.3 :" ),
                                                           BorderLayout.NORTH );
      _pointlist = new JTextField();
      _pointlist.setActionCommand( "import" );
      _pointlist.addActionListener( this );
      panel.add( _pointlist, BorderLayout.CENTER );
      return panel; // fertig
      
   } // _createInputPanel


   /*
   * Aufbau des mittleren Panels mit den Optionen.
   */
   private JPanel _createOptionsPanel() {

      // Panel:
      JPanel panel = new JPanel();

      // Option schliessen:
      _opt_close = new JCheckBox( "close polygon", true );
      _opt_close.setActionCommand( "close" );
      _opt_close.addActionListener( this );
      panel.add( _opt_close );

      return panel; // fertig

   } // _createOptionsPanel


   /*
   * Aufbau des unteren Panels mit den Buttons.
   */
   private JPanel _createButtonsPanel() {

      // Panel:
      JPanel panel = new JPanel();

      // Button fuer Import:
      JButton button = new JButton( "Import" );
      button.setActionCommand( "import" );
      button.addActionListener( this );
      panel.add( button );

      // Button fuer Close:
      button = new JButton( "Close" );
      button.setActionCommand( "quit" );
      button.addActionListener( this );
      panel.add( button );

      return panel; // fertig

   } // _createButtonsPanel


   /*
   * Erzeugt ein Polygon aus der Punktliste.
   */
   private Polygon2 _createPolygon() {

      Polygon2 poly = new Polygon2();
      // Eingabe ueberpruefen und ggf. verbessern:
      String input =
                  _pointlist.getText().replace( ',','/' ).replace( ';','/' );
      String token, x, y;
      float xf, yf;
      int p;
      StringTokenizer st = new StringTokenizer( input, " " );
      while ( st.hasMoreTokens() ) {
         token = st.nextToken();
         p = token.indexOf( '/' );
         if ( p == -1 ) continue;
         try {
            x = token.substring( 0, p );
            y = token.substring( p + 1 );
            xf = Float.parseFloat( x );
            yf = Float.parseFloat( y );
            poly.addPoint( xf, yf );
         } catch ( Exception e ) {
            continue;
         } // catch
      } // while

      if ( poly.length() == 0 ) {
         _pointlist.setText( "" );
         return null;
      } // if

      input = "";
      Point2 point;
      Enumeration points = poly.points().values();
      while ( points.hasMoreElements() ) {
         point = (Point2) points.nextElement();
         input += point.x + "/" + point.y;
         if ( points.hasMoreElements() ) input += " ";
      } // while
      _pointlist.setText( input );

      if ( _opt_close.isSelected() )
         poly.setClosed();
      else
         poly.setOpen();
      return poly; // fertig

   } // _createPolygon


} // PointInsertionTool
