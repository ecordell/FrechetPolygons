package anja.swing.function;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.border.EmptyBorder;

import anja.swing.JIntervalFieldDouble;
import anja.swing.JPluginDialog;
import anja.swing.Register;


/**
* Abstrakte Klasse eines Plugins fuer eine bestimmte Funktion.
*
* @version 0.7 26.08.2004
* @author Sascha Ternes
*/

public abstract class FunctionPlugin
extends JPluginDialog
{

   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Default-Titel eines Plugins:
   protected static final String _DEFAULT_TITLE = "FPW-Plugin";


   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * Panel mit dem Editor fuer die Funktion
   */
   protected JPanel _function_panel;

   /**
   * Panel mit Einstellungen fuer die Definitionsmenge der Funktion
   */
   protected JPanel _domain_panel;
   /**
   * Eingabefeld fuer das Intervall
   */
   protected JIntervalFieldDouble _interval;

   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt das Grundgeruest eines Plugins zur Erzeugung eines neuen
   * Funktionsobjekts.
   *
   * @param register das Register-Objekt
   */
   public FunctionPlugin(
      Register register
   ) {

      // Initialisierungen:
      super( register );

      // Layout des Inhaltspanels:
      _function_panel = new JPanel( new GridLayout( 4, 1 ) );
      _function_panel.setBorder( new EmptyBorder( 0, 6, 0, 6 ) );
      _buildFunctionPanel();
      _content.insertTab( "Function", null, _function_panel, null, 0 );
      _domain_panel = new JPanel( new GridLayout( 4, 1 ) );
      _domain_panel.setBorder( new EmptyBorder( 0, 6, 0, 6 ) );
      _buildDomainPanel();
      _content.insertTab( "Domain", null, _domain_panel, null, 1 );

   } // FunctionPlugin


   // *************************************************************************
   // Protected methods
   // *************************************************************************

   /**
   * Wird bei der Initialisierung des Plugins aufgerufen und sollte das Panel
   * <code>_functions_panel</code> aufbauen, das zur Bearbeitung einer Funktion
   * dient.
   */
   abstract protected void _buildFunctionPanel();


   /**
   * Wird bei der Initialisierung des Plugins aufgerufen und baut das Panel
   * <code>_domain_panel</code> auf, das zur Einstellung der Definitionsmenge
   * der jeweiligen Funktion dient.
   */
   protected void _buildDomainPanel() {

      JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      panel.add( new JLabel( "Interval" ) );
      _interval = new JIntervalFieldDouble();
      _interval.addChangeListener( this );
      panel.add( _interval );
      _domain_panel.add( panel );

   } // _buildDomainPanel


   /*
   * [javadoc-Beschreibung wird aus JPluginDialog kopiert]
   */
   protected void _buildOptionsPanel() {

      super._buildOptionsPanel();
      // naechste Farbe aus Editor:
      _color.setColor( _reg.editor.nextColor( this ) );

   } // _buildOptionsPanel


} // FunctionPlugin
