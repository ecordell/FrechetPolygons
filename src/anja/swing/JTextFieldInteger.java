package anja.swing;

import java.awt.AWTEvent;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;


/**
* Eingabefeld fuer eine ganze Zahl.
*
* @version 0.2 19.06.2004
* @author Ulrich Handel, Sascha Ternes
*/

public class JTextFieldInteger
extends JTextField
{

   // ************************************************************************
   // Private variables
   // ************************************************************************

   // die ganze Zahl:
   private int _value;

   // Grenzen:
   private int _min_value = Integer.MIN_VALUE;
   private int _max_value = Integer.MAX_VALUE;


   // ************************************************************************
   // Constructors
   // ************************************************************************

   /**
   * Erzeugt ein neues Eingabefeld mit Startwert.
   *
   * @param value der Startwert
   */
   public JTextFieldInteger(
      int value
   ) {

      setValue( value );
      setColumns( 3 );
      setHorizontalAlignment( JTextField.RIGHT );
      enableEvents( AWTEvent.FOCUS_EVENT_MASK | AWTEvent.KEY_EVENT_MASK );

   } // JTextFieldInteger


   /**
   * Erzeugt ein neues Eingabefeld mit Startwert und Beschraenkungen.
   *
   * @param value der Startwert
   * @param min der minimale Wert
   * @param max der maximale Wert
   */
   public JTextFieldInteger(
      int value,
      int min,
      int max
   ) {

      this( value );
      setValueLimits( min, max );

   } // JTextFieldInteger


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert den aktuellen Wert.
   *
   * @return den aktuellen Wert
   */

   public int getValue() {

      return _value;

   } // getValue


   /*
   * [javadoc-Beschreibung wird aus JTextField kopiert]
   */
   public void processKeyEvent(
      KeyEvent e
   ) {

      if ( e.getID() == KeyEvent.KEY_PRESSED &&
                                          e.getKeyCode() == KeyEvent.VK_ENTER )
         _refreshValue();
      super.processKeyEvent( e );

   } // processKeyEvent


   /*
   * [javadoc-Beschreibung wird aus JTextField kopiert]
   */
   public void processFocusEvent(
      FocusEvent e
   ) {

      if ( e.getID() == FocusEvent.FOCUS_LOST )
         _refreshValue();
      super.processFocusEvent( e );

   } // processFocusEvent


   /**
   * Setzt einen neuen Wert. Falls die Zahl ausserhalb der erlaubten Grenzen
   * liegt, wird die Zahl auf den naechstliegenden erlaubten Wert angepasst.
   *
   * @param value die neue Zahl
   */
   public void setValue(
      int value
   ) {

      if ( value < _min_value )
         _value = _min_value;
      else if ( value > _max_value )
         _value = _max_value;
      else
         _value = value;

      String string = String.valueOf( _value );
      if ( ! getText().equals( string ) )
         setText( string );

   } // setValue


   /**
   * Liefert den minimal erlaubten Wert.
   *
   * @return den Minimalwert
   */
   public int getMinValueLimit() {

      return _min_value;

   } // getMinValueLimit


   /**
   * Liefert den maximal erlaubten Wert.
   *
   * @return den Maximalwert
   */
   public int getMaxValueLimit() {

      return _max_value;

   } // getMaxValueLimit


   /**
   * Setzt die Minimal- und Maximalgrenze fuer dieses Eingabefeld.<br>
   * Falls <code>min</code> gruesser als <code>max</code> ist, werden die Grenzen
   * nicht veraendert.<br>
   * Falls der aktuelle Wert ausserhalb der neuen Grenzen liegt, wird er
   * entsprechend angepasst.
   *
   * @param min der neue Minimalwert
   * @param max der neue Maximalwert
   */
   public void setValueLimits(
      int min,
      int max
   ) {

      if( min > max ) return;
      _min_value = min;
      _max_value = max;
      setValue( _value );

   } // setValueLimits


   // *************************************************************************
   // Private methods
   // *************************************************************************

   /*
   * Liest den aktuellen Wert des Eingabefelds aus.
   */
   private void _refreshValue() {

      try {
         setValue( Integer.valueOf( getText() ).intValue() );
      } catch( NumberFormatException exception ) { // try
         setValue( _value );
      } // catch

   } // _refreshValue


} // JTextFieldInteger
