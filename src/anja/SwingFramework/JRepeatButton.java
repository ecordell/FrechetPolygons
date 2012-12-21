
/*
 * File: JRepeatButton.java
 * Created on 25.05.2006 by ibr
 *
 * TODO: Write documentation
 */

package anja.SwingFramework;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;

/**
 * Die Klasse JRepeatButton ist von javax.swing.JButton abgeleitet.
 *
 * Ein JRepeatButton benachrichtigt die registrierten ActionListener
 * bereits beim Druecken des Buttons und dann periodisch wiederholt solange,
 * bis der Button wieder losgelassen wird.( Bei einem javax.swing.JButton
 * werden die angeschlossenen ActionListener erst nach dem Loslassen der
 * Maustaste benachrichtigt.)
 * Die Benachrichtigung eines ActionListeners erfolgt dabei wie bei einem
 * JButton durch Aufruf seiner Methode
 * actionPerformed( ActionEvent e ).
 *
 * Die Klasse JRepeatButton stellt zwei Methoden zum Setzen der benoetigten
 * Zeitspannen bereit:
 *
 *  Die Methode   setDelayTime( int msecs )
 *  setzt die Zeit zwischen der ersten und der zweiten Benachrichtigung
 *  der registrierten ActionListener.
 *
 *  Die Methode   setPeriodTime( int msecs )
 *  setzt die Zeit zwischen den darauf folgenden Benachrichtigungen.
 *
 * Beide Zeiten koennen auch im Konstruktor angegeben werden.
 * Werden die Zeiten weder durch den Konstruktor noch durch die oben
 * erwaehnten Methoden gesetzt, werden folgende Voreinstellungen benutzt:
 *      DelayTime  = 0.4 sec
 *      PeriodTime = 0.1 sec
 *
 * Es ergibt sich insgeamt der folgende Ablauf beim Betaetigen eines
 * JRepeatButtons:
 *
 *      Wird die linke Maustaste ueber dem Button gedrueckt, wird die
 *      Methode actionPerformed() aller ActionListener aufgerufen, die
 *      beim JRepeatButton registriert sind ( siehe addActionListener() ).
 *
 *      Dies wird wiederholt, wenn nach Ablauf der Zeitspanne DelayTime
 *      die Maustaste immer noch gedrueckt ist, und ab diesem Zeitpunkt
 *      jeweils nach Ablauf der Zeitspanne PeriodTime bis die Maustaste
 *      losgelassen wird.
 *
 *
 * @version 0.1 23.07.01 ( grundsaetzliche Ueberarbeitung 08.02.02 )
 * @author      Ulrich Handel
 * 
 * TODO:  translate docs to English!!!
 */

public class JRepeatButton extends JButton implements Runnable
{
    
    //*************************************************************************
    //                             Public constants
    //*************************************************************************
    
    //*************************************************************************
    //                             Private constants
    //*************************************************************************
    private static final long serialVersionUID = -6778905956592318020L;
    
    //*************************************************************************
    //                             Class variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Public instance variables
    //*************************************************************************
    
    //*************************************************************************
    //                       Protected instance variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance variables
    //*************************************************************************
  
    private int         _delay_time  = 400; // 0.4 sec
    private int         _period_time = 100; // 0.1 sec

    private Thread      _thread; // This fires the button clicks repeatedly
    
    private boolean     _is_running;   // true if the thread is running
    private boolean     _stop_request; // true when stopping the thread..
          
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    /**
     * Create a new JRepeatButton without label
     */
    public JRepeatButton()
    {
        super();
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | 
                     AWTEvent.FOCUS_EVENT_MASK );
    } 

    //*************************************************************************

    /**
     * Create a new JRepeatButton with label
     */
    public JRepeatButton(String label)    
    {
        this();
        setText(label);
    } 

    //*************************************************************************

    /**
     * Create a new JRepeatButton with label and supplied delay time
     * and period time ( msecs ).
     */
    public JRepeatButton(String label,
                         int    delay_time,
                         int    period_time)    
    {
        this(label);
        setDelayTime(delay_time);
        setPeriodTime(period_time);
    } 
    
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
    
    /**
     * Set delay time (msecs).
     */
    public void setDelayTime(int msecs)
    {
        _delay_time = msecs;
    }

    //*************************************************************************
    
    /**
     * Set period time (msecs).
     */
    public void setPeriodTime(int msecs)
    {
        _period_time = msecs;
    } 

    //*************************************************************************

    /**
     * Verarbeiten eines aufgetretenen MouseEvent.
     * 
     * Durch Druecken der linken Maustaste wird ein Thread gestartet,
     * der das wiederholte "Anklicken" des Buttons uebernimmt.
     * 
     * Durch Loslassen der Maustaste oder Verlassen des Buttons wird
     * der Thread wieder beendet.
     */
    public void processMouseEvent(MouseEvent e)
    {
        if(!e.isMetaDown()) // linke Maus-Taste
        {
            switch(e.getID())
            {
                case MouseEvent.MOUSE_PRESSED :
                    
                if(!_is_running)
                {
                    // Es laeuft kein Thread, also wird einer
                    // gestartet.
                    _stop_request = false;
                    _is_running = true;
                    _thread = new Thread(this);
                    _thread.setPriority(Thread.MIN_PRIORITY);
                    _thread.start();
                }
                break;
                
                case MouseEvent.MOUSE_RELEASED :
                case MouseEvent.MOUSE_EXITED :
                
                    _stop(); // Beendet ggf. den laufenden Thread
                    
                break;
            }
        }
       
        super.processMouseEvent(e);
    }

    //*************************************************************************

    /**
     * Verarbeiten eines aufgetretenen FocusEvent.
     * 
     * Der JRepeatButton produziert normalerweise ActionEvents vom
     * Druecken bis zum Loslassen der linken Maustaste. Wenn bei der
     * Verarbeitung eines dieser ActionEvents durch einen
     * registrierten Listener ein modaler Dialog geoeffnet wird, so
     * geht das Loslassen der Maustaste "verloren", so dass nach dem
     * Schliessen des Dialogs weiter wiederholt ActionEvents an die
     * Listener uebergeben werden, obwohl der Button nicht mehr
     * gedrueckt ist.
     * 
     * Um dies zu verhindern, muss der "Click"-Thread also beim
     * Oeffnen eines modalen Dialogs beendet werden. Da in diesem Fall
     * der Fokus verloren geht, ist diese Methode ein geeigneter Ort
     * um dies ggf. zu tun.
     */
    public void processFocusEvent(FocusEvent e)
    {
        if(e.getID() == FocusEvent.FOCUS_LOST)
        {
            _stop(); // Beendet ggf. den laufenden Thread
        } 
        
        super.processFocusEvent(e);
    } 

    //*************************************************************************

    /**
     * Run-Methode fuer interface Runnable
     *
     * Diese Methode wird durch den Thread _thread ausgefuehrt, aber
     * auch zum synchronisierten Absetzen von Button-Clicks aufgerufen
     * ( siehe java.awt.EventQueue.invokeAndWait() ).
     */
    public void run()
    {
        if(Thread.currentThread() == _thread)
        {
            // run() wurde also durch _thread.start() aufgerufen.
            
            long period = _delay_time; // Zeitspanne zischen
            
            // erstem und zweiten Click
            while(!_stop_request && isEnabled())
            {
                long millis = System.currentTimeMillis();
                
                try
                {
                    EventQueue.invokeAndWait(this); // Click
                    
                    // Die durch die Verarbeitung des Clicks bereits
                    // vergangene Zeitspanne ist von period abzuziehen
                    //
                    
                    period -= (System.currentTimeMillis() - millis);
                    
                    // period sollte aber nicht kleiner als eine
                    // 50stel
                    // Sekunde sein, damit andere Threads ( z.B der
                    // System-Thread zur Event-Verteilung ) genuegend
                    // Zeit haben.
                    //
                    
                    period = Math.max(period, 20);
                    Thread.sleep(period);
                    
                } 
                catch(InterruptedException e)
                {
                } 
                catch(InvocationTargetException e)
                {
                }
                
                period = _period_time; // Zeitspanne zwischen den
                // folgenden Clicks
            } 

            _is_running = false;
        }
        else
        {
            // run() wurde durch EventQueue.invokeAndWait() aufgerufen
            doClick(); // siehe javax.swing.AbstractButton()
        }
    }
  
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************
    
    /**
     * Stop the worker thread if it's running
     */
    private void _stop()
    {
        if( ! _is_running )
         return; // No thread's running....

        _stop_request = true;
        _thread.interrupt(); // The thread will wake up if  it's sleeping!
    } 
} 




