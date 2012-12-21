package anja.swing;

import java.awt.AWTEvent;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;


/**
* Eingabefeld fuer eine reelle Zahl.
*
* @version 0.2 19.06.2004
* @author Ulrich Handel, Sascha Ternes
*/

public class JTextFieldDouble
extends JTextField
{

   // ************************************************************************
   // Private variables
   // ************************************************************************

   // die ganze Zahl:
   private double _value;

   // Grenzen:
   private double _min_value = Double.NEGATIVE_INFINITY;
   private double _max_value = Double.POSITIVE_INFINITY;


   // ************************************************************************
   // Constructors
   // ************************************************************************

   /**
   * Erzeugt ein neues Eingabefeld mit Startwert.
   *
   * @param value der Startwert
   */
   public JTextFieldDouble(
      double value
   ) {

      setValue( value );
      setColumns( 4 );
      setHorizontalAlignment( JTextField.RIGHT );
      enableEvents( AWTEvent.FOCUS_EVENT_MASK | AWTEvent.KEY_EVENT_MASK );

   } // JTextFieldDouble


   /**
   * Erzeugt ein neues Eingabefeld mit Startwert und Beschraenkungen.
   *
   * @param value der Startwert
   * @param min der minimale Wert
   * @param max der maximale Wert
   */
   public JTextFieldDouble(
      double value,
      double min,
      double max
   ) {

      this( value );
      setValueLimits( min, max );

   } // JTextFieldDouble


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert den aktuellen Wert.
   *
   * @return den aktuellen Wert
   */

   public double getValue() {

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
      double value
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
   public double getMinValueLimit() {

      return _min_value;

   } // getMinValueLimit


   /**
   * Liefert den maximal erlaubten Wert.
   *
   * @return den Maximalwert
   */
   public double getMaxValueLimit() {

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
      double min,
      double max
   ) {

      if ( min > max ) return;
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

      setText( getText().replace( ',', '.' ) );
      try {
         setValue( Double.valueOf( getText() ).doubleValue() );
      } catch( NumberFormatException exception ) { // try
         setValue( _value );
      } // catch

   } // _refreshValue


} // JTextFieldDouble
