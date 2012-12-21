package anja.swing;

import java.awt.GridLayout;

import java.util.Vector;

import javax.swing.JPanel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anja.analysis.Bound;
import anja.analysis.Interval;


/**
* Ein komfortables Eingabefeld fuer ein mathematisches Intervall.
*
* @version 0.7 19.06.2004
* @author Sascha Ternes
*/

public class JIntervalFieldInteger
extends JPanel
implements ChangeListener
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // die beiden Bounds:
   private JBoundFieldInteger _bound1;
   private JBoundFieldInteger _bound2;

   // die registrierten Listener:
   private Vector _listeners;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Intervall-Eingabefeld. Das Intervall ist unbeschraenkt
   * und wird als geschlossenes Intervall ueber der Menge der ganzen Zahlen
   * initialisiert.
   */
   public JIntervalFieldInteger() {

      this( Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
                                               Integer.MAX_VALUE, true, true );

   } // JIntervalField


   /**
   * Erzeugt ein neues Intervall-Eingabefeld. Das Intervall ist unbeschraenkt
   * und wird als geschlossenes Intervall initialisiert. Beide Grenzen werden
   * auf den spezifizierten Wert gesetzt.
   *
   * @param bound der Anfangswert beider Intervallgrenzen
   */
   public JIntervalFieldInteger(
      int bound
   ) {

      this( bound, bound, Integer.MIN_VALUE, Integer.MAX_VALUE, true, true );

   } // JIntervalFieldInteger


   /**
   * Erzeugt ein neues Intervall-Eingabefeld. Das Intervall ist unbeschraenkt
   * und wird als geschlossenes Intervall von -1 bis 1 initialisiert.
   *
   * @param left_bound der Anfangswert der linken Grenze
   * @param right_bound der Anfangswert der rechten Grenze
   */
   public JIntervalFieldInteger(
      int left_bound,
      int right_bound
   ) {

      this( left_bound, right_bound, Integer.MIN_VALUE, Integer.MAX_VALUE,
                                                                  true, true );

   } // JIntervalField


   /**
   * Erzeugt ein neues, anfangs geschlossenes Intervall mit den angegebenen
   * Anfangswerten und Extrema.
   *
   * @param left_bound der Anfangswert der linken Grenze
   * @param right_bound der Anfangswert der rechten Grenze
   * @param minimum der kleinstmoegliche Wert der linken Grenze
   * @param maximum der groesstmoegliche Wert der rechten Grenze
   */
   public JIntervalFieldInteger(
      int left_bound,
      int right_bound,
      int minimum,
      int maximum
   ) {

      this( left_bound, right_bound, minimum, maximum, true, true );

   } // JIntervalFieldInteger


   /**
   * Erzeugt das spezifizierte Intervall mit den angegebenen Extrema.
   *
   * @param left_bound der Anfangswert der linken Grenze
   * @param right_bound der Anfangswert der rechten Grenze
   * @param minimum der kleinstmoegliche Wert der linken Grenze
   * @param maximum der groesstmoegliche Wert der rechten Grenze
   * @param left_inclusive gibt an, ob das Intervall links offen oder
            geschlossen ist
   * @param right_inclusive gibt an, ob das Intervall rechts offen oder
   *        geschlossen ist
   */
   public JIntervalFieldInteger(
      int left_bound,
      int right_bound,
      int minimum,
      int maximum,
      boolean left_inclusive,
      boolean right_inclusive
   ) {

      super( new GridLayout( 1, 2 ) );
      int min = minimum;
      int max = maximum;
      if ( min > max ) {
         min = maximum;
         max = minimum;
      } // if
      int left = left_bound;
      int right = right_bound;
      if ( left > right ) {
         left = right_bound;
         right = left_bound;
      } // if
      if ( left < min ) left = min;
      if ( right > max ) right = max;
      _bound1 = new JBoundFieldInteger( left, left_inclusive, min, right, 1,
                        JBoundFieldInteger.LEFT, JBoundFieldInteger.NEGATIVE );
      _bound1.addChangeListener( this );
      add( _bound1 );
      _bound2 = new JBoundFieldInteger( right, right_inclusive, left, max, 1,
                       JBoundFieldInteger.RIGHT, JBoundFieldInteger.POSITIVE );
      _bound2.addChangeListener( this );
      add( _bound2 );
      _listeners = new Vector();

   } // JIntervalFieldInteger


   /**
   * Erzeugt ein neues Intervall-Eingabefeld, das mit den Werten des
   * spezifizierten Intervalls initialisiert wird und unbeschraenkt ist.
   *
   * @param interval das Anfangsintervall
   */
   public JIntervalFieldInteger(
      Interval interval
   ) {

      super( new GridLayout( 1, 2 ) );
      Bound[] bounds = interval.getBounds();
      int left = ( (Integer) bounds[0].n[0] ).intValue();
      int right = ( (Integer) bounds[1].n[0] ).intValue();
      _bound1 = new JBoundFieldInteger( left, bounds[0].inclusion[0],
                  Integer.MIN_VALUE, right, 1, JBoundFieldInteger.LEFT,
                                                 JBoundFieldInteger.NEGATIVE );
      _bound1.addChangeListener( this );
      add( _bound1 );
      _bound2 = new JBoundFieldInteger( right, bounds[1].inclusion[0],
                  left, Integer.MAX_VALUE, 1, JBoundFieldInteger.RIGHT,
                                                 JBoundFieldInteger.POSITIVE );
      _bound2.addChangeListener( this );
      add( _bound2 );
      _listeners = new Vector();

   } // JIntervalFieldInteger


   /**
   * Erzeugt ein neues Intervall-Eingabefeld, das mit den Werten des
   * spezifizierten Intervalls initialisiert wird und nach den uebergebenen
   * Extrema beschraenkt ist.
   *
   * @param interval das Anfangsintervall
   * @param minimum der kleinstmoegliche Wert der linken Grenze
   * @param maximum der groesstmoegliche Wert der rechten Grenze
   */
   public JIntervalFieldInteger(
      Interval interval,
      int minimum,
      int maximum
   ) {

      super( new GridLayout( 1, 2 ) );
      Bound[] bounds = interval.getBounds();
      int left = ( (Integer) bounds[0].n[0] ).intValue();
      int right = ( (Integer) bounds[1].n[0] ).intValue();
      int min = minimum;
      int max = maximum;
      if ( min > max ) {
         min = maximum;
         max = minimum;
      } // if
      if ( left < min ) left = min;
      if ( right > max ) right = max;
      _bound1 = new JBoundFieldInteger( left, bounds[0].inclusion[0], min,
              right, 1, JBoundFieldInteger.LEFT, JBoundFieldInteger.NEGATIVE );
      _bound1.addChangeListener( this );
      add( _bound1 );
      _bound2 = new JBoundFieldInteger( right, bounds[1].inclusion[0], left,
               max, 1, JBoundFieldInteger.RIGHT, JBoundFieldInteger.POSITIVE );
      _bound2.addChangeListener( this );
      add( _bound2 );
      _listeners = new Vector();

   } // JIntervalFieldInteger


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert das aktuell eingestellte Intervall.
   *
   * @return das eingegebene Intervall
   */
   public Interval getInterval() {

      return new Interval( _bound1.getBound(), _bound2.getBound() );

   } // getInterval


   /**
   * Liefert das Swing-Objekt der linken Intervallgrenze.
   *
   * @return das Objekt der linken Intervallgrenze
   */
   public JBoundFieldInteger getLeftBoundObject() {

      return _bound1;

   } // getLeftBoundObject


   /**
   * Liefert das Swing-Objekt der rechten Intervallgrenze.
   *
   * @return das Objekt der rechten Intervallgrenze
   */
   public JBoundFieldInteger getRightBoundObject() {

      return _bound2;

   } // getRightBoundObject


   /**
   * Setzt die Intervallgrenzen neu.
   *
   * @param left_bound die linke Intervallgrenze
   * @param right_bound die rechte Intervallgrenze
   */
   public void setBounds(
      int left_bound,
      int right_bound
   ) {

      if ( left_bound <= right_bound ) {
         _bound1.setValue( left_bound );
         _bound2.setValue( right_bound );
      } else { // if
         _bound1.setValue( right_bound );
         _bound2.setValue( left_bound );
      } // else

   } // setBounds


   /**
   * Setzt die Extrema des Intervalls.
   *
   * @param minimum der minimale Wert der linken Intervallgrenze
   * @param maximum der maximale Wert der rechten Intervallgrenze
   */
   public void setLimits(
      int minimum,
      int maximum
   ) {

      _bound1.setMinimum( minimum );
      _bound2.setMaximum( maximum );

   } // setLimits


   /**
   * Registriert einen Listener, der Veraenderungen des Intervalls ueberwacht.
   *
   * @param listener der Listener
   */
   public void addChangeListener(
      ChangeListener listener
   ) {

      _listeners.add( listener );

   } // addChangeListener


   /**
   * Entfernt einen vorher registrierten Listener.
   *
   * @param listener der Listener
   */
   public void removeChangeListener(
      ChangeListener listener
   ) {

      _listeners.remove( listener );

   } // removeChangeListener


   // *************************************************************************
   // Interface ChangeListener
   // *************************************************************************

   /**
   * Reagiert auf Aenderungen des Intervalls und achtet darauf, dass die linke
   * Intervallgrenze kleiner oder gleich der rechten Intervallgrenze ist.
   * Weiterhin werden alle registrierten Listener von der Aenderung des
   * Intervalls informiert.
   *
   * @param e das <code>ChangeEvent</code>-Objekt
   */
   public void stateChanged(
      ChangeEvent e
   ) {

      // linke Grenze hat sich geaendert:
      if ( e.getSource() == _bound1 )
         _bound2.setMinimum( _bound1.getValue() );
      // rechte Grenze hat sich geaendert:
      else
         _bound1.setMaximum( _bound2.getValue() );

      // alle registrierten Listener informieren:
      for ( int i = 0; i < _listeners.size(); i++ )
         ( (ChangeListener) _listeners.get( i ) ).stateChanged(
                                                     new ChangeEvent( this ) );

   } // stateChanged


} // JIntervalFieldInteger
