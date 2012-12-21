package anja.swinggui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.accessibility.*;

/**
 * Ein Objekt der Klasse BreakableProcess ermoeglicht das Ausfuehren
 * eines vom Benutzer abbbrechbaren Prozesses.
 *
 * Dazu wird dem Konstruktor unter anderem ein Runnable-Objekt uebergeben,
 * dessen run-Methode den auszufuehrenden abbbrechbaren Code enthaelt.
 * Diese run-Methode wird durch Aufruf der Methode
 *
 *     perform()
 *
 * als Thread gestartet.
 * Falls der Thread nach Ablauf einer Verzoegerungszeit ( voreingestellt
 * 0.5 sec ) nicht beendet ist, wird ein Dialog angezeigt, der dem
 * Benutzer den Abbruch ermoeglicht.
 * Die Methode perform() kehrt erst zurueck, wenn der Prozess entweder
 * vollstaendig ausgefuehrt ( return true ) oder durch den Benutzer
 * abgebrochen wurde ( return false ).
 *
 *
 * !!! Wichtig !!!
 *     Der Abbruch des Prozesses geschieht durch die Methode Thread.stop().
 *     Diese Methode gilt als "deprecated" ( siehe Java-Dokumentation ),
 *     da ihre Benutzung bestimmte Gefahren birgt.
 *     Es ist vom Benutzer dieser Klasse darauf zu achten, dass das
 *     Stoppen der run-Methode des uebergebenen Runnable-Objekts keine
 *     "Objekt-Truemmer" hinterlaesst, auf die danach noch durch andere
 *     Threads zugegriffen werden koennte.
 *
 *
 * @version 0.1 06.02.02
 * @author      Ulrich Handel
 */

//****************************************************************
public class BreakableProcess implements Runnable,
                                         ActionListener
//****************************************************************
{

    //************************************************************
    // private variables
    //************************************************************


    private Runnable _runnable; // Die run-Methode dieses Objekts enthaelt
                                // den auszufuehrenden abbrechbaren Code.
                                // ( siehe perform() )

    private JDialog _dialog; // Dialog, der dem Benutzer den Abbruch des
                             // Prozesses ermoeglicht.

    private Frame _frame;    // Eigentuemer des Dialogs.
                             // null:  Dialog wird ggf. als Kind des
                             //        aktiven Frames geoeffnet.

    private String _message; // Ausgabe-String fuer den Dialog.

    private int _delay = 500; // Zeit in msecs, die nach dem Starten des
                              // Prozesses gewartet wird, bis der
                              // Abbruch-Dialog angezeigt wird.


    private Thread _thread; // Thread zur Ausfuehrung des abbrechbaren
                            // Prozesses.

    private boolean _broken; // true markiert den Abbruch des Prozesses
                             // durch den Benutzer
                             // ( siehe actionPerformed() ).



    //************************************************************
    // constructors
    //************************************************************



    /**
     * Erzeugen eines neuen BreakableProcess-Objekts
     *
     * @param runnable  Objekt, dessen run-Methode als abbrechbarere
     *                  Prozess auszufuehren ist.
     *
     * @param message   String zur Anzeige im Abbruch-Dialog.
     *
     * @param frame     Frame, als dessen Kind der Abbruch-Dialog
     *                  ggf. geoeffnet wird.
     */

    //============================================================
    public BreakableProcess( Runnable runnable,
                             String   message,
                             Frame    frame )
    //============================================================
    {
        _runnable = runnable;
        _message = message;
        _frame = frame;
    } // BreakableProcess



    /**
     * Erzeugen eines neuen BreakableProcess-Objekts
     *
     * @param runnable  Objekt, dessen run-Methode als abbrechbarere
     *                  Prozess auszufuehren ist.
     *
     * @param message   String zur Anzeige im Abbruch-Dialog.
     *
     * Der Abbruch-Dialog wird ggf. als Kind des aktiven Frames geoeffnet.
     */

    //============================================================
    public BreakableProcess( Runnable runnable,
                             String   message )
    //============================================================
    {
        _runnable = runnable;
        _message = message;
    } // BreakableProcess




    //************************************************************
    // public methods
    //************************************************************



    /**
     * Ausfuehren des abbrechbaren Prozesses.
     *
     * Es wird die Methode run() des im Konstruktor uebergebenen
     * Runnable-Objekts als Thread ausgefuehrt.
     *
     * Falls nach Ablauf einer Verzoegerungszeit ( zu Setzen durch
     * setDelay() oder voreingestellt 0.5 sec ) der Thread nicht
     * beendet ist, wird ein Dialog geoeffnet, der dem Benutzer
     * den Abbruch des Prozesses ermoeglicht.
     *
     * @return true <==> Prozess wurde vollstaendig ausgefuehrt
     *                   ( kein Abbruch )
     */

    //============================================================
    public boolean perform()
    //============================================================
    {
        // Thread zur Ausfuehrung des abbrechbaren Prozesses erzeugen
        // und starten.
        //
        _thread = new Thread( _runnable );
        _thread.setPriority( Thread.MIN_PRIORITY );
        _thread.start();


        // Warten bis entweder die Verzoegerungszeit abgelaufen, oder der
        // Thread beendet ist.
        //
        _delay();


        if( ! _thread.isAlive() )
            return true; // Thread wurde also ohne Abbruch beendet


        _buildDialog(); // modalen Abbruch-Dialog aufbauen
                        // ( noch nicht anzeigen !!! )


        // Thread zum Zerstoeren des Dialogs ( nach Beendigung oder
        // Abbruch des Prozesses ) erzeugen und starten
        //
        Thread dispose = new Thread( this );
        dispose.start();


        _broken = false; // Bisher kein Abbruch.
                         // ( wird waehrend der Anzeige des Dialogs
                         // gesetzt, wenn der Prozess durch den Benutzer
                         // abgebrochen wurde )

        _dialog.show(); // Blockierendes Anzeigen des modalen Dialogs.
                        // Aus Methode show() wird erst zurueckgekehrt,
                        // wenn der Dialog durch den Thread dispose
                        // zerstoert wurde ( siehe run() )

        return ( ! _broken );
    } // perform




    /**
     * run-Methode fuer Interface Runnable.
     *
     * Diese Methode wird vor dem Anzeigen des Dialogs als Thread
     * gestartet ( siehe perform() und hat die Aufgabe, den Dialog zu
     * zerstoeren, sobald der abbrechbare Prozess beendet oder
     * abgebrochen wurde.
     *
     */

    //============================================================
    public void run()
    //============================================================
    {
        // Warten bis der Thread, der den abbrechbaren Prozess ausfuehrt,
        // beendet ist oder gestoppt wurde.
        //
        while( _thread.isAlive() )
        {
            try
            {
                _thread.join();
            }
            catch( InterruptedException e ){}
        } // while


        // ggf. muss jetzt noch gewartet werden, bis der Dialog
        // wirklich angezeigt ist.
        // ( vorher darf er nicht zerstoert werden !!! )
        //
        while( ! _dialog.isShowing() )
        {
            try
            {
                //Thread.currentThread().sleep( 10 );
            	Thread.sleep(10);
            }
            catch( InterruptedException e ){}
        } // while


        _dialog.dispose(); // Dialog zerstoeren
    } // run



    /**
     * Methode fuer Interface ActionListener.
     * Wird ausgefuehrt, wenn der Benutzer den Abbruch des Prozesses
     * angefordert hat ( Druecken des Break-Buttons ).
     *
     * Der Thread, der den abbrechbaren Prozess ausfuehrt, wird durch
     * Aufruf seiner Methode stop() beendet.
     */

    //============================================================
    public void actionPerformed( ActionEvent e )
    //============================================================
    {
        _thread.stop();

        _broken = true; // Markiert, dass der Prozess abgebrochen wurde.
                        // ( siehe perform() )
    } // actionPerformed



    /**
     * Setzen der Verzoegerungszeit mit der der Abbruch-Dialog nach
     * dem Starten des abbrechbaren Prozesses angezeigt wird.
     */

    //============================================================
    public void setDelay( int delay )
    //============================================================
    {
        _delay = delay;
    } // setDelay





    //************************************************************
    // private methods
    //************************************************************



    /**
     * Erzeugen und Aufbauen des Abbruch-Dialogs
     */

    //============================================================
    private void _buildDialog()
    //============================================================
    {
        // Ermitteln des Frame-Objekts, als dessen Kind der modale Dialog
        // zu erzeugen ist.
        //
        Frame frame = _frame;
        if( frame == null )
            frame = _getActiveFrame();


        // Dialog erzeugen
        //
        _dialog = new JDialog( frame, true );
        _dialog.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );


        // Break-Button erzeugen und dieses Objekt bei ihm als
        // ActionListener registrieren.
        //
        JButton button = new JButton( "Break" );
        button.addActionListener( this );


        // Dialog durch Zufuegen des Message-Strings und Break-Buttons
        // aufbauen.

        Container pane = _dialog.getContentPane();
        pane.setLayout( new GridBagLayout() );

        GridBagConstraints con = new GridBagConstraints();
        con.gridwidth = GridBagConstraints.REMAINDER;
        pane.add( new Label( _message ), con );
        pane.add( button, con );


        // Abmessungen und Position des Dialogs setzen

        _dialog.pack();

        Dimension dim  = _dialog.getSize();
        if( frame != null )
        {
            // Position des Dialogs so setzen, dass dieser im Zentrum des
            // Eigentuemer-Frame angezeigt wird
            //
            Rectangle rect = frame.getBounds();
            _dialog.setLocation( rect.x + ( rect.width - dim.width ) / 2,
                                 rect.y + ( rect.height - dim.height ) / 2 );
        }
        else
        {
            // Dialog hat kein Eigentuemer-Frame

            // Position des Dialogs so setzen, dass dieser im Zentrum des
            // Screens angezeigt wird
            //
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            _dialog.setLocation( ( size.width - dim.width ) / 2,
                                 ( size.height - dim.height ) / 2 );
        } // if


        _dialog.setResizable( false );
    } // _buildDialog



    /**
     * Warten bis entweder die Verzoegerungszeit _delay abgelaufen ist,
     * oder der Thread, der den abbrechbaren Prozess ausfuehrt, beendet
     * ist.
     */

    //============================================================
    private void _delay()
    //============================================================
    {
        long millis = System.currentTimeMillis() + _delay;

        while( _thread.isAlive() )
        {
            long sleep = millis - System.currentTimeMillis();
            if( sleep <= 0 )
                return;

            try
            {
                _thread.join( sleep );
            }
            catch( InterruptedException e ){}
        } // while
    } // _delay



    /**
     * Ermitteln des Frames, das augenblicklich aktiv ist.
     */

    //============================================================
    static private Frame _getActiveFrame()
    //============================================================
    {
        Frame[] frames = Frame.getFrames(); // Alle Frames des Applets
                                            // bzw. der Application

        for( int i = 0; i < frames.length; i++ )
        {
            if( frames[ i ].getAccessibleContext().
                            getAccessibleStateSet().
                            contains( AccessibleState.ACTIVE ) )
            {
                // frames[ i ] ist das aktive Frame
                return frames[ i ];
            } // if
        } // for

        return null; // Kein aktives Frame gefunden
    } // _getActiveFrame


} // BreakableProcess
