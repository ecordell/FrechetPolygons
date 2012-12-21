package anja.swing.function;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;

import anja.analysis.ConstantFunction;

import anja.swing.JTextFieldDouble;
import anja.swing.Register;


/**
* Plugin fuer konstante Funktionen des Klassentyps
* <code>ConstantFunction</code>.
*
* @version 0.9 12.08.2004
* @author Sascha Ternes
*/

public final class ConstantFunctionPlugin
extends FunctionPlugin
{

   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Default-Titel eines Plugins:
   private static final String _DEFAULT_TITLE = "Constant Function";

   // ActionCommand fuer Konstante c:
   private static final String _C = "c";


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // Konstante c:
   private JTextFieldDouble _c;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Plugin fuer konstante Funktionen.
   *
   * @param register das Register-Objekt
   */
   public ConstantFunctionPlugin(
      Register register
   ) {

      super( register );
      setTitle( _DEFAULT_TITLE );

   } // ConstantFunctionPlugin


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
      if ( cmd.equals( _C ) )
         _actionApply();
      else
         super.actionPerformed( e );

   } // actionPerformed


   // *************************************************************************
   // Protected methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   protected void _buildFunctionPanel() {

      
      JLabel label = new JLabel( "Specify a Constant function as f(x) = c." );
      _function_panel.add( label );
      JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      panel.add( new JLabel( "f(x) =" ) );
      _c = new JTextFieldDouble( 1.0 );
      _c.setActionCommand( _C );
      _c.addActionListener( this );
      panel.add( _c );
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
      ConstantFunction f = (ConstantFunction) object.getFunction();
      if ( f == null )
         f = new ConstantFunction( _c.getValue() );
      else
         f.setCoefficient( 0, _c.getValue() );
      f.setDomain( _interval.getInterval() );
      object.setFunction( f );

   } // _updateObject


} // ConstantFunctionPlugin
