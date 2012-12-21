package anja.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;


/**
* Diese Klasse hat den einzigen Zweck, einen "Start"-Button als Applet zur
* Verfuegung zu stellen, um ihn in einer Webseite zum Starten von Anwendungen
* und Applets zu benutzen. Um sie zu verwenden, sollte diese Klasse abgeleitet
* werden nach folgendem Muster:<p>
*
<pre>public class MyStart extends StartButtonApplet {

   public MyStart() {
      super();
      addApplication( "apps.test.MyApplication" );
      addApplet( new MyApplet() );
   }

}</pre><p>
*
* Es ist so moeglich, mehrere Applikationen und Applets gleichzeitig zu starten.
*
* @version 0.4 02.05.2005
* @author Sascha Ternes
*/

public abstract class JStartButtonApplet
   extends JApplet             //Swing-Applet!
   implements ActionListener
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * Standard-Aufschrift des Buttons
   */
   public static final String DEFAULT_TEXT = "Start";


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // alle zu startenden Anwendungen:
   private Vector _applications;
   // deren Kommandozeilenparameter:
   private Vector _params;

   // alle zu startenden Applets:
   private Vector _applets;
   // Startposition und -dimension:
   private Vector _appdimensions;

   // Hilfsvariablen:
   private Class[] _prms;
   private Object[] _cmdlprms;
   private int[] _appdim;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt einen Start-Button mit der Standard-Aufschrift
   * {@link #DEFAULT_TEXT DEFAULT_TEXT}. Bevor das Anklicken des Buttons eine
   * Wirkung zeigt, muss mittels der Methoden
   * {@link #addApplication(String) addApplication(...)} oder
   * {@link #addApplet(JApplet) addApplet(...)} eine Anwendung oder ein Applet
   * zum Starten registriert werden.
   */
   public JStartButtonApplet() {

      this( DEFAULT_TEXT );

   } // JStartButtonApplet


   /**
   * Erzeugt einen Start-Button mit einer waehlbaren Aufschrift. Bevor das
   * Anklicken des Buttons eine Wirkung zeigt, muss mittels der Methode
   * {@link #addApplication(String) addApplication(...)} eine Anwendung zum
   * Starten registriert werden.
   *
   * @param text der Text, der auf dem Button erscheinen soll
   */
   public JStartButtonApplet(
      String text
   ) {

      _applications = new Vector();
      _params = new Vector();
      _applets = new Vector();
      _appdimensions = new Vector();

      // erzeuge Hilfsvariablen:
      _prms = new Class[1];
      _prms[0] = ( new String[0] ).getClass();
      _cmdlprms = new Object[1];
      _appdim = new int[4];

      // erzeuge Start-Button:
      JButton button = new JButton( text );
      button.addActionListener( this );
      getContentPane().add( button );

   } // JStartButtonApplet


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Fuegt die spezifizierte Anwendung der Liste zu startender Applikationen
   * hinzu. Sobald der Start-Button angeklickt wird, werden alle registrierten
   * Anwendungen gestartet.
   *
   * @param application_class die zu registrierende Anwendung
   */
   public void addApplication(
      String application_class
   ) {

      addApplication( application_class, new String[0] );

   } // addApplication


   /**
   * Fuegt die spezifizierte Anwendung samt Kommandozeilenparametern der Liste
   * zu startender Applikationen hinzu. Sobald der Start-Button angeklickt
   * wird, werden alle registrierten Anwendungen gestartet.
   *
   * @param application_class die zu registrierende Anwendung
   * @param application_params die dazugehoerigen Kommandozeilenparameter
   */
   public void addApplication(
      String application_class,
      String[] application_params
   ) {

      try {
         Class cl = Class.forName( application_class );
         _applications.add( cl );
         _params.add( application_params );
      } catch ( Exception e ) {} // try

   } // addApplication


   /**
   * Fuegt die spezifizierte Appletinstanz der Liste zu startender Applets
   * hinzu. Sobald der Start-Button angeklickt wird, werden alle registrierten
   * Applets gestartet.<br>
   * Diese Methode ist besonders geeignet fuer Applets, die das Interface
   * {@link ExternalApplet ExternalApplet} implementieren.
   *
   * @param applet das zu registrierende Applet
   */
   public void addApplet(
      JApplet applet
   ) {

      // Standardwerte initialisieren:
      int x = 100;
      int y = 100;
      int width = applet.getPreferredSize().width;
      int height = applet.getPreferredSize().height;

      // Startposition uebernehmen, falls das Applet eine solche definiert:
//      if ( applet instanceof ExternalApplet ) {
//         ExternalApplet ex = (ExternalApplet) applet;
//         x = ex.getPreferredPosition().x;
//         y = ex.getPreferredPosition().y;
//      } // if

      addApplet( applet, x, y, width, height );

   } // addApplet


   /**
   * Fuegt die spezifizierte Appletinstanz der Liste zu startender Applets
   * hinzu. Sobald der Start-Button angeklickt wird, werden alle registrierten
   * Applets gestartet.
   *
   * @param applet das zu registrierende Applet
   * @param x die x-Koordinate des Appletfensters beim Start
   * @param y die y-Koordinate des Appletfensters beim Start
   * @param width die Breite des Appletfensters beim Start
   * @param height die Hoehe des Appletfensters beim Start
   */
   public void addApplet(
      JApplet applet,
      int x,
      int y,
      int width,
      int height
   ) {

      _applets.add( applet );
      _appdim[0] = x;
      _appdim[1] = y;
      _appdim[2] = width;
      _appdim[3] = height;
      _appdimensions.add( _appdim );

   } // addApplet


   // *************************************************************************
   // Interface ActionListener
   // *************************************************************************

   /**
   * Reagiert auf das Anklicken des Start-Buttons, indem alle registrierten
   * Applikationen und Applets gestartet werden.
   *
   * @param e das <code>ActionEvent</code>-Objekt
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      // Applikationen starten:
      for ( int i = 0; i < _applications.size(); i++ ) {
         Class application = (Class) _applications.get( i );
         _cmdlprms[0] = _params.get( i );
         try {
            Method main_method = application.getMethod( "main", _prms );
            main_method.invoke( null, _cmdlprms );
         } catch ( Exception ex ) {
            System.out.println( ex );
            if ( ex instanceof InvocationTargetException ) {
               InvocationTargetException ite = (InvocationTargetException) ex;
               System.out.println( ite.getCause() );
               System.out.println( ite.getTargetException() );
            } // if
         } // try
      } // for

      // Applets starten:
      for ( int i = 0; i < _applets.size(); i++ ) {
         JApplet applet = (JApplet) _applets.get( i );
         _appdim = (int[]) _appdimensions.get( i );
         JFrame frame = new JFrame( applet.getClass().getName() );
         frame.setLocation( _appdim[0], _appdim[1] );
         frame.setSize( _appdim[2], _appdim[3] );
         frame.getContentPane().add( applet );
         frame.setVisible( true );
         applet.init();
         applet.start();
      } // for

   } // actionPerformed


} // JStartButtonApplet
