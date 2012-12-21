package anja.swing.function;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;

import anja.analysis.LinearFunction;

import anja.swing.JTextFieldDouble;
import anja.swing.Register;


/**
* Plugin fuer lineare Funktionen des Klassentyps <code>LinearFunction</code>.
*
* @version 0.9 12.08.04
* @author Sascha Ternes
*/

public final class LinearFunctionPlugin
extends FunctionPlugin
{

   // *************************************************************************
   // Private Linears
   // *************************************************************************

   // Default-Titel eines Plugins:
   private static final String _DEFAULT_TITLE = "Linear Function";

   // ActionCommand fuer Koeffizient a:
   private static final String _A = "a";
   // ActionCommand fuer Konstante b:
   private static final String _B = "b";


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // Koeffizient a:
   private JTextFieldDouble _a;
   // Konstante b:
   private JTextFieldDouble _b;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Plugin fuer lineare Funktionen.
   *
   * @param register das Register-Objekt
   */
   public LinearFunctionPlugin(
      Register register
   ) {

      super( register );
      setTitle( _DEFAULT_TITLE );

   } // LinearFunctionPlugin


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
      else if ( cmd.equals( _B ) )
         _actionApply();
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
                               "Specify a Linear function as f(x) = ax + b." );
      _function_panel.add( label );
      JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      panel.add( new JLabel( "f(x) =" ) );
      _a = new JTextFieldDouble( 1.0 );
      _a.setActionCommand( _A );
      _a.addActionListener( this );
      panel.add( _a );
      panel.add( new JLabel( "x +" ) );
      _b = new JTextFieldDouble( 0.0 );
      _b.setActionCommand( _B );
      _b.addActionListener( this );
      panel.add( _b );
      _function_panel.add( panel );

   } // _buildFunctionPanel


   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   protected void _updateObject() {

      // falls eine neue Funktion erzeugt wurde, initialisieren:
      if ( _creating ) _object = new FunctionObject( this );

      // Funktionsdaten auslesen:
      FunctionObject object = (FunctionObject) _object;
      LinearFunction f = (LinearFunction) object.getFunction();
      if ( f == null )
         f = new LinearFunction( _a.getValue(), _b.getValue() );
      else {
         f.setCoefficient( 1, _a.getValue() );
         f.setCoefficient( 0, _b.getValue() );
      } // else
      f.setDomain( _interval.getInterval() );
      object.setFunction( f );

   } // _updateObject


   /*
   * Prueft den ersten Koeffizienten und setzt ihn auf 1.0, falls er 0.0 ist.
   */
   private void _checkFirstCoefficient() {

      if ( _a.getValue() == 0.0 ) {
         _a.setValue( 1.0 );
         _a.repaint();
      } // if

   } // _checkFirstCoefficient


} // LinearFunctionPlugin
