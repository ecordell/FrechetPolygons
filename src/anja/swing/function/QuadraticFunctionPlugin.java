package anja.swing.function;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import anja.analysis.QuadraticFunction;

import anja.swing.JTextFieldDouble;
import anja.swing.Register;


/**
* Plugin fuer quadratische Funktionen des Klassentyps
* <code>QuadraticFunction</code>.
*
* @version 0.9 18.07.2004
* @author Sascha Ternes
*/

public final class QuadraticFunctionPlugin
extends FunctionPlugin
{

   // *************************************************************************
   // Private Quadratics
   // *************************************************************************

   // Default-Titel eines Plugins:
   private static final String _DEFAULT_TITLE = "Quadratic Function";

   // ActionCommand fuer Koeffizient a:
   private static final String _A = "a";
   // ActionCommand fuer Koeffizient b:
   private static final String _B = "b";
   // ActionCommand fuer Konstante c:
   private static final String _C = "c";
   // ActionCommand fuer Nullstelle 1:
   private static final String _ZERO1 = "zero1";
   // ActionCommand fuer Nullstelle 2:
   private static final String _ZERO2 = "zero2";
   // ActionCommand fuer RadioButtons:
   private static final String _CHOOSE = "choose";


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // Panel mit den Eingabefeldern:
   private JPanel _form;

   // Auswahlknopf fuer die Normalform:
   private JRadioButton _normal;
   // Auswahlknopf fuer die Nullstellenform:
   private JRadioButton _zerospots;

   // Label:
   private JLabel _normal1;
   private JLabel _normal2;
   private JLabel _normal3;
   private JLabel _zerospots1;
   private JLabel _zerospots2;
   private JLabel _zerospots3;

   // Koeffizient a:
   private JTextFieldDouble _a;
   // Koeffizient b:
   private JTextFieldDouble _b;
   // Konstante c:
   private JTextFieldDouble _c;
   // Nullstelle 1:
   private JTextFieldDouble _zero1;
   // Nullstelle 2:
   private JTextFieldDouble _zero2;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Plugin fuer quadratische Funktionen.
   *
   * @param register das Register-Objekt
   */
   public QuadraticFunctionPlugin(
      Register register
   ) {

      super( register );
      setTitle( _DEFAULT_TITLE );

   } // QuadraticFunctionPlugin


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      String cmd = e.getActionCommand();
      if ( cmd.equals( _A ) ) {
         _checkFirstCoefficient();
         _actionApply();
      } // if
      else if ( cmd.equals( _B ) || cmd.equals( _C ) )
         _actionApply();
      else if ( cmd.equals( _ZERO1 ) || cmd.equals( _ZERO2 ) )
         _actionApply();
      else if ( cmd.equals( _CHOOSE ) )
         _actionChoose();
      else {
         _checkFirstCoefficient();
         super.actionPerformed( e );
      } // else

   } // actionPerformed


   // *************************************************************************
   // Private methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   protected void _buildFunctionPanel() {

      JLabel label = new JLabel(
                               "Choose how to specify a Quadratic function:" );
      _function_panel.add( label );
      ButtonGroup group = new ButtonGroup();
      _normal = new JRadioButton( "f(x) = ax² + bx + c (normal form)", true );
      _normal.setActionCommand( _CHOOSE );
      _normal.addActionListener( this );
      group.add( _normal );
      _function_panel.add( _normal );
      _zerospots = new JRadioButton( "f(x) = (x-a) · (x-b) (zero spots form)" );
      _zerospots.setActionCommand( _CHOOSE );
      _zerospots.addActionListener( this );
      group.add( _zerospots );
      _function_panel.add( _zerospots );
      _form = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      _function_panel.add( _form );

      // Initialisierungen:
      _normal1 = new JLabel( "f(x) =" );
      _normal2 = new JLabel( "x² +" );
      _normal3 = new JLabel( "x +" );
      _zerospots1 = new JLabel( "f(x) = (x-" );
      _zerospots2 = new JLabel( ") · (x-" );
      _zerospots3 = new JLabel( ")" );
      _a = new JTextFieldDouble( 1.0 );
      _a.setActionCommand( _A );
      _a.addActionListener( this );
      _b = new JTextFieldDouble( 0.0 );
      _b.setActionCommand( _B );
      _b.addActionListener( this );
      _c = new JTextFieldDouble( 0.0 );
      _c.setActionCommand( _C );
      _c.addActionListener( this );
      _zero1 = new JTextFieldDouble( 0.0 );
      _zero1.setActionCommand( _ZERO1 );
      _zero1.addActionListener( this );
      _zero2 = new JTextFieldDouble( 0.0 );
      _zero2.setActionCommand( _ZERO2 );
      _zero2.addActionListener( this );

      _actionChoose(); // Restaufbau

   } // _buildFunctionPanel


   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   protected void _updateObject() {

      // falls eine neue Funktion erzeugt wurde, initialisieren:
      if ( _creating ) _object = new FunctionObject( this );

      // Funktionsdaten auslesen:
      FunctionObject object = (FunctionObject) _object;
      QuadraticFunction f = (QuadraticFunction) object.getFunction();
      if ( f == null )
         if ( _normal.isSelected() )
            f = new QuadraticFunction( _a.getValue(), _b.getValue(),
                                                               _c.getValue() );
         else
            f = new QuadraticFunction( _zero1.getValue(), _zero2.getValue() );
      else
         if ( _normal.isSelected() ) {
            f.setCoefficient( 2, _a.getValue() );
            f.setCoefficient( 1, _b.getValue() );
            f.setCoefficient( 0, _c.getValue() );
         } else // if
            f = new QuadraticFunction( _zero1.getValue(), _zero2.getValue() );
      f.setDomain( _interval.getInterval() );
      object.setFunction( f );

   } // _updateObject


   /*
   * Konfiguration des Dialogs je nach Auswahl der Form:
   */
   private void _actionChoose() {

      _form.removeAll(); // Panel leeren
      // Normalform:
      if ( _normal.isSelected() ) {
         _form.add( _normal1 );
         _form.add( _a );
         _form.add( _normal2 );
         _form.add( _b );
         _form.add( _normal3 );
         _form.add( _c );
      // Nullstellenform:
      } else { // if
         _form.add( _zerospots1 );
         _form.add( _zero1 );
         _form.add( _zerospots2 );
         _form.add( _zero2 );
         _form.add( _zerospots3 );
      } // else
      repaint();

   } // _actionChoose


   /*
   * Prueft den ersten Koeffizienten und setzt ihn auf 1.0, falls er 0.0 ist.
   */
   private void _checkFirstCoefficient() {

      if ( _a.getValue() == 0.0 ) {
         _a.setValue( 1.0 );
         _a.repaint();
      } // if

   } // _checkFirstCoefficient


} // QuadraticFunctionPlugin
