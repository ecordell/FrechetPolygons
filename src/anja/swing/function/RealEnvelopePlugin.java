package anja.swing.function;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import javax.swing.border.TitledBorder;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import anja.analysis.RealEnvelope;
import anja.analysis.RealFunction;

import anja.swing.Register;

import anja.swing.event.SceneEvent;


/**
* Plugin fuer Konturen des Klassentyps <code>RealEnvelope</code>.
*
* @version 0.9 22.07.2004
* @author Sascha Ternes
*/

public final class RealEnvelopePlugin
extends FunctionPlugin
implements ListSelectionListener
{

   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Default-Titel eines Plugins:
   private static final String _DEFAULT_TITLE = "Envelope";

   
   // *************************************************************************
   // Private variables
   // *************************************************************************

   // Auswahlknopf fuer untere Kontur:
   private JRadioButton _lower;
   // Auswahlknopf fuer obere Kontur:
   private JRadioButton _upper;

   // Liste der benutzten Funktionen:
   private JList _in_list;
   private DefaultListModel _in_model;

   // Liste der noch verfuegbaren Funktionen:
   private JList _out_list;
   private DefaultListModel _out_model;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Plugin fuer Konturen.
   *
   * @param register das Register-Objekt
   */
   public RealEnvelopePlugin(
      Register register
   ) {

      super( register );
      setTitle( _DEFAULT_TITLE );

   } // RealEnvelopePlugin


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   public void objectAdded(
      SceneEvent e
   ) {

      super.objectAdded( e );
      FunctionObject function = (FunctionObject) e.getObject();
      if ( function.getPlugin() != this )
         _registerFunction( function );

   } // objectAdded


   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   public void objectRemoved(
      SceneEvent e
   ) {

      super.objectRemoved( e );
      FunctionObject function = (FunctionObject) e.getObject();
      if ( function.getPlugin() != this )
         _out_model.removeElement( function );

   } // objectRemoved


   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   public void objectUpdated(
      SceneEvent e
   ) {

      super.objectUpdated( e );
      FunctionObject function = (FunctionObject) e.getObject();
      if ( function.getPlugin() != this )
         if ( _in_model.contains( function ) )
            _updateObject();

   } // objectUpdated


   // *************************************************************************
   // Interface ListSelectionListener
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus ListSelectionListener kopiert]
   */
   public void valueChanged(
      ListSelectionEvent e
   ) {

      if ( e.getSource() == _in_list )
         _removeFunction( _in_list.getSelectedIndex() );
      else
         _insertFunction( _out_list.getSelectedIndex() );

   } // valueChanged


   // *************************************************************************
   // Protected methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   protected void _buildFunctionPanel() {

      GridBagLayout layout = new GridBagLayout();
      GridBagConstraints con = new GridBagConstraints();
      _function_panel.setLayout( layout );

      JLabel label = new JLabel( "Choose kind of envelope:" );
      con.weightx = 1.0;
      con.fill = GridBagConstraints.HORIZONTAL;
      layout.setConstraints( label, con );
      _function_panel.add( label );
      ButtonGroup group = new ButtonGroup();
      _lower = new JRadioButton( "Lower", true );
      group.add( _lower );
      con.weightx = 0.0;
      layout.setConstraints( _lower, con );
      _function_panel.add( _lower );
      _upper = new JRadioButton( "Upper" );
      group.add( _upper );
      layout.setConstraints( _upper, con );
      _function_panel.add( _upper );

      JPanel panel = new JPanel( new GridLayout( 1, 2 ) );
      JPanel panel0 = new JPanel( new BorderLayout() );
      panel0.setBorder( new TitledBorder( "envelope" ) );
      _in_model = new DefaultListModel();
      Enumeration e = _reg.scene.elementsByName();
      while ( e.hasMoreElements() ) {
         FunctionObject data = (FunctionObject) e.nextElement();
         _insertFunction( data );
      } // while
      _in_list = new JList( _in_model );
      _in_list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      _in_list.addListSelectionListener( this );
      JScrollPane scroll = new JScrollPane( _in_list );
      scroll.setVerticalScrollBarPolicy(
                                       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
      panel0.add( scroll, BorderLayout.CENTER );
      panel.add( panel0 );
      panel0 = new JPanel( new BorderLayout() );
      panel0.setBorder( new TitledBorder( "available" ) );
      _out_model = new DefaultListModel();
      _out_list = new JList( _out_model );
      _out_list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      _out_list.addListSelectionListener( this );
      scroll = new JScrollPane( _out_list );
      scroll.setVerticalScrollBarPolicy(
                                       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
      panel0.add( scroll, BorderLayout.CENTER );
      panel.add( panel0 );
      con.gridx = 0;
      con.gridy = 1;
      con.weightx = 1.0;
      con.weighty = 1.0;
      con.fill = GridBagConstraints.BOTH;
      con.gridwidth = GridBagConstraints.REMAINDER;
      layout.setConstraints( panel, con );
      _function_panel.add( panel );

   } // _buildFunctionPanel


   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   protected void _updateObject() {

      // falls eine neue Funktion erzeugt wurde, initialisieren:
      if ( _creating ) _object = new FunctionObject( this );

      // Typ ermitteln:
      int type = RealEnvelope.LOWER_ENVELOPE;
      if ( _upper.isSelected() )
         type = RealEnvelope.UPPER_ENVELOPE;
      // Funktionenzahl und Konturzahl ermitteln:
      int functs = 0;
      int envels = 0;
      for ( int i = 0; i < _in_model.size(); i++ ) {
         RealFunction f = (RealFunction) ( (FunctionObject)
                                            _in_model.get( i ) ).getFunction();
         if ( f instanceof RealEnvelope ) envels++;
         else functs++;
      } // for
      // Funktionen und Konturen ermitteln:
      RealFunction[] functions = new RealFunction[functs];
      RealEnvelope[] env = new RealEnvelope[envels];
      functs = 0;
      envels = 0;
      for ( int i = 0; i < _in_model.size(); i++ ) {
         RealFunction f = (RealFunction) ( (FunctionObject)
                                            _in_model.get( i ) ).getFunction();
         if ( f instanceof RealEnvelope ) env[envels++] = (RealEnvelope) f;
         else functions[functs++] = f;
      } // for
      // Kontur berechnen und hinzufuegen:
      RealEnvelope fen = null;
      if ( functs > 0 )
         fen = new RealEnvelope( type, functions );
      else
         fen = new RealEnvelope( type );
      for ( int i = 0; i < envels; i++ )
         fen.add( (RealEnvelope) env[i] );
      fen.name = _object.getName();
      ( (FunctionObject) _object ).setFunction( fen );

   } // _updateObject


   // *************************************************************************
   // Protected methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus FunctionPlugin kopiert]
   */
   protected void _actionOK() {

      super._actionOK();
      for ( int i = _out_model.size() - 1; i >= 0; i-- ) {
         FunctionObject function = (FunctionObject) _out_model.get( i );
         if ( function.getPlugin() == null )
            _out_model.removeElementAt( i );
      } // for

   } // _action_OK


   /*
   * Fuegt die Funktion sortiert in die Konturliste ein.
   */
   private void _insertFunction(
      FunctionObject function
   ) {

      for ( int i = 0; i < _in_model.size(); i++ ) {
         String s = ( (FunctionObject) _in_model.get( i ) ).getName();
         if ( function.getName().compareTo( s ) < 0 ) {
            _in_model.insertElementAt( function, i ); // zwischendrin
            return;
         } // if
      } // for
      _in_model.addElement( function ); // am Schluss anfuegen

   } // _insertFunction


   /*
   * Fuegt die Funktion sortiert in die Vorratsliste ein.
   */
   private void _registerFunction(
      FunctionObject function
   ) {

      for ( int i = 0; i < _out_model.size(); i++ ) {
         String s = ( (FunctionObject) _out_model.get( i ) ).getName();
         if ( function.getName().compareTo( s ) < 0 ) {
            _out_model.insertElementAt( function, i ); // zwischendrin
            return;
         } // if
      } // for
      _out_model.addElement( function ); // am Schluss anfuegen

   } // _registerFunction


   /*
   * Fuegt die Funktion aus der Vorratsliste sortiert in die Konturliste ein.
   */
   private void _insertFunction(
      int index
   ) {

      if ( index < 0 ) return;
      FunctionObject p = (FunctionObject) _out_model.remove( index );
      _insertFunction( p );

   } // _insertFunction


   /*
   * Entfernt die Funktion aus der Konturliste.
   */
   private void _removeFunction(
      int index
   ) {

      if ( index < 0 ) return;
      FunctionObject p = (FunctionObject) _in_model.remove( index );
      _registerFunction( p );

   } // _removeFunction


} // RealEnvelopePlugin
