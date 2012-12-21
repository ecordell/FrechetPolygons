package anja.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;


/**
* Ein Grafikobjekt, das als Anzeige fuer eine (ausgewaehlte) Farbe dient und
* dessen Farbe per Klick geaendert werden kann. Dieser Farbknopf laesst auch
* <code>null</code> als Farbe zu, was als "ungefaerbt" interpretiert wird.<p>
*
* Um eine Farbaenderung sinnvoll per Ereignisbehandlung abfragen zu koennen, wird
* empfohlen, dazu einen <code>javax.swing.event.ChangeListener</code> zu
* implementieren, der zunaechst die Methode
* {@link #hasColorChanged() hasColorChanged()} abfragt und nur beim
* Rueckgabewert <code>true</code> die Farbe per {@link #getColor() getColor()}
* ermittelt.
*
* @version 0.7 26.08.2004
* @author Sascha Ternes
*/

public final class JColorButton
extends JButton
implements ActionListener
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // die Elternkomponente fuer den Farbauswahldialog:
   private Component _owner;

   // die tatsaechliche verwaltete Farbe:
   private Color _color;
   // Flag fuer Farbaenderung:
   private boolean _changed;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt einen ungefaerbten Farbknopf.
   */
   public JColorButton() {

      this( null, null );

   } // JColorButton


   /**
   * Erzeugt einen ungefaerbten Farbknopf und registriert eine Elternkomponente
   * fuer einen modalen Farbauswahldialog.
   *
   * @param owner die Elternkomponente fuer den modalen Farbauswahldialog
   */
   public JColorButton(
      Component owner
   ) {

      this( owner, null );

   } // JColorButton


   /**
   * Erzeugt einen Farbknopf mit der spezifizierten Farbe und registriert eine
   * Elternkomponente fuer einen modalen Farbauswahldialog.
   *
   * @param owner die Elternkomponente fuer den modalen Farbauswahldialog
   * @param color die Anfangsfarbe
   */
   public JColorButton(
      Component owner,
      Color color
   ) {

      super();
      _owner = owner;
      _color = color;
      _changed = false;
      _setBackground();
      setFocusPainted( false );
      setButtonSize( 32, 24 );
      addActionListener( this );

   } // JColorButton


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert die angezeigte Farbe.
   *
   * @return die Farbe
   */
   public synchronized Color getColor() {

      _changed = false;
      return _color;

   } // getColor


   /**
   * Setzt die angezeigte Farbe.
   *
   * @param color die Farbe
   */
   public synchronized void setColor(
      Color color
   ) {

      _color = color;
      _setBackground();
      _changed = false;

   } // setColor


   /**
   * Setzt die Groesse des Farbknopfs.
   *
   * @param width die gewueschte Breite in Pixeln
   * @param height die gewueschte Hoehe in Pixeln
   */
   public void setButtonSize(
      int width,
      int height
   ) {

      Dimension d = new Dimension( width, height );
      setMinimumSize( d );
      setMaximumSize( d );
      setPreferredSize( d );

   } // setButtonSize


   /**
   * Testet, ob sich seit dem letzten Auslesen per
   * {@link #getColor() getColor()} die Farbe des Farbknopfs geaendert hat.<p>
   *
   * Wenn ein Objekt sich bei einem <code>JColorButton</code> beispielsweise
   * als <code>ChangeListener</code> registriert hat, wird die
   * <code>stateChanged(...)</code>-Methode bei Klicken des Farbknopfs
   * insgesamt drei Mal aufgerufen. Nur nach dem letzten Mal hat sich die Farbe
   * tatsaechlich geaendert. Dieser Umstand kann mittels dieser Methode abgefragt
   * werden.
   *
   * @return <code>true</code>, wenn inzwischen eine neue Farbe ausgewaehlt
   *         wurde, sonst <code>false</code>
   */
   public synchronized boolean hasColorChanged() {

      return _changed;

   } // hasColorChanged


   // *************************************************************************
   // Interface ActionListener
   // *************************************************************************

   /**
   * Beim Anklicken des Farbknopfs erscheint ein Farbauswahldialog, mit dem
   * die Farbe des Knopfes geaendert werden kann.
   *
   * @param e das <code>ActionEvent</code>-Objekt
   */
   public synchronized void actionPerformed(
      ActionEvent e
   ) {

      _color = JColorChooser.showDialog( _owner, "Color", _color );
      _changed = true;
      _setBackground();

   } // actionPerformed


   // *************************************************************************
   // Private variables
   // *************************************************************************

   /*
   * Setzt die Farbe des Buttons.
   */
   private void _setBackground() {

      setBackground( _color );
      if ( _color == null )
         setBorderPainted( true );
      else
         setBorderPainted( false );

   } // _setBackground


} // JColorButton
