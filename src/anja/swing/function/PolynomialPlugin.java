package anja.swing.function;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import anja.analysis.Polynomial;

import anja.swing.Register;


/**
* Plugin fuer allgemeine Polynome des Klassentyps <code>Polynomial</code>.
*
* @version 0.9 18.07.2004
* @author Sascha Ternes
*/

public final class PolynomialPlugin
extends FunctionPlugin
{

   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Default-Titel eines Plugins:
   private static final String _DEFAULT_TITLE = "Polynomial";

   // Alternativtexte fuer die Eingabeaufforderung:
   private static final String _ENTER_COEFFICIENTS = "Enter c_n ... c_0: ";
   private static final String _ENTER_ZEROSPOTS = "Enter z_1 ... z_n: ";

   // ActionCommand fuer Konstante c:
   private static final String _STRING = "string";
   // ActionCommand fuer RadioButtons:
   private static final String _CHOOSE = "choose";

   
   // *************************************************************************
   // Private variables
   // *************************************************************************

   // Auswahlknopf fuer die Normalform:
   private JRadioButton _normal;
   // Auswahlknopf fuer die Nullstellenform:
   private JRadioButton _zerospots;

   // Eingabeaufforderung fuer Koeffizienten oder Nullstellen:
   private JLabel _enter;
   // Eingabefeld fuer Koeffizienten oder Nullstellen:
   private JTextField _string;

   // aktuelle Koeffizienten oder Nullstellen:
   private double[] _coeff;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Plugin fuer Polynome.
   *
   * @param register das Register-Objekt
   */
   public PolynomialPlugin(
      Register register
   ) {

      super( register );
      setTitle( _DEFAULT_TITLE );

   } // PolynomialPlugin


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
      if ( cmd.equals( _STRING ) )
         _actionApply();
      else if ( cmd.equals( _CHOOSE ) )
         _actionChoose();
      else
         super.actionPerformed( e );

   } // actionPerformed


   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   protected void _actionApply() {

      if ( _normal.isSelected() )
         _checkCoefficients();
      else
         _checkZeroSpots();
      _string.repaint();
      super._actionApply();

   } // _actionApply


   // *************************************************************************
   // Protected methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   protected void _buildFunctionPanel() {

      JLabel label = new JLabel( "Choose how to specify a polynomial:" );
      _function_panel.add( label );
      ButtonGroup group = new ButtonGroup();
      _normal = new JRadioButton( "p(x) = sum( c_i·x^i ) (normal form)",
                                                                        true );
      _normal.setActionCommand( _CHOOSE );
      _normal.addActionListener( this );
      group.add( _normal );
      _function_panel.add( _normal );
      _zerospots = new JRadioButton(
                                  "p(x) = product( x-z_i) (zero spots form)" );
      _zerospots.setActionCommand( _CHOOSE );
      _zerospots.addActionListener( this );
      group.add( _zerospots );
      _function_panel.add( _zerospots );
      
      GridBagLayout layout = new GridBagLayout();
      GridBagConstraints con = new GridBagConstraints();
      con.fill = GridBagConstraints.BOTH;
      con.weighty = 1.0;
      JPanel form = new JPanel( layout );
      _enter = new JLabel();
      layout.setConstraints( _enter, con );
      form.add( _enter );
      _string = new JTextField( "1.0 0.0 -1.0 0.0", 17 );
      _string.setActionCommand( _STRING );
      _string.addActionListener( this );
      con.weightx = 1.0;
      con.gridwidth = GridBagConstraints.REMAINDER;
      layout.setConstraints( _string, con );
      form.add( _string );
      _function_panel.add( form );
      _actionChoose(); // Restaufbau

   } // _buildFunctionPanel


   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   protected void _updateObject() {

      // falls eine neue Funktion erzeugt wurde, initialisieren:
      if ( _creating ) _object = new FunctionObject( this );

      Polynomial p;
      if ( _normal.isSelected() )
         p = new Polynomial( _coeff );
      else
         p = Polynomial.createFromZeroSpots( _coeff );
      p.setDomain( _interval.getInterval() );
      ( (FunctionObject) _object ).setFunction( p );

   } // _updateObject


   /*
   * Konfiguration des Dialogs je nach Auswahl der Form:
   */
   private void _actionChoose() {

      // Normalform:
      if ( _normal.isSelected() ) {
         _enter.setText( _ENTER_COEFFICIENTS );
      // Nullstellenform:
      } else { // if
         _enter.setText( _ENTER_ZEROSPOTS );
      } // else
      repaint();

   } // _actionChoose


   /*
   * Ueberprueft die Eingabe der Koeffizienten.
   */
   private void _checkCoefficients() {

      // Kommata austauschen:
      _string.setText( _string.getText().replace( ',', '.' ) );
      // Koeffizienten ueberpruefen:
      StringTokenizer s = new StringTokenizer( _string.getText(), " " );
      int i = s.countTokens();
      _coeff = null;
      // Token in Koeffizienten umwandeln:
      for ( i--; i >= 0; i-- )
         try {
            double c = Double.parseDouble( s.nextToken() );
            if ( c != 0.0 ) {
               if ( _coeff == null )
                  _coeff = new double[ i + 1 ];
               _coeff[i] = c;
            } else // if
               if ( _coeff != null )
                  _coeff[i] = c;
         } catch ( NumberFormatException nfe ) { // try
            _coeff[i] = 0.0;
         } // catch
      // leeren String abfangen:
      if ( _coeff == null ) {
         _coeff = new double[1];
         _coeff[0] = 0.0;
      } // if
      // String auf alle Faelle ersetzen:
      String coeff = new String( "" );
      for ( i = _coeff.length - 1; i >= 0; i-- ) {
         coeff += _coeff[i];
         if ( i > 0 ) coeff += " ";
      } // for
      _string.setText( coeff );

    } // _checkCoefficients


   /*
   * Ueberprueft die Eingabe der Nullstellen.
   */
   private void _checkZeroSpots() {

      // Kommata austauschen:
      _string.setText( _string.getText().replace( ',', '.' ) );
      // Koeffizienten ueberpruefen:
      StringTokenizer s = new StringTokenizer( _string.getText(), " " );
      int n = s.countTokens();
      TreeSet z = new TreeSet();
      // Token in Koeffizienten umwandeln:
      for ( int i = 0; i < n; i++ )
         try {
            Double d = new Double( Double.parseDouble( s.nextToken() ) );
            z.add( d );
         } catch ( NumberFormatException nfe ) {} // try
      // leeren String abfangen:
      if ( z.size() == 0 ) z.add( new Double( 0.0 ) );
      // Nullstellen speichern:
      n = z.size();
      _coeff = new double[n];
      Iterator it = z.iterator();
      for ( int i = 0; i < n; i++ )
         _coeff[i] = ( (Double) it.next() ).doubleValue();
      // String auf alle Faelle ersetzen:
      String coeff = new String( "" );
      for ( int i = 0; i < n; i++ ) {
         if ( i > 0 ) coeff += " ";
         coeff += _coeff[i];
      } // for
      _string.setText( coeff );

   } // _checkZeroSpots


} // PolynomialPlugin
