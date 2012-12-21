package anja.swing.function;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import anja.analysis.RealEnvelope;

import anja.swing.Editor;
import anja.swing.JPluginDialog;
import anja.swing.Register;
import anja.swing.SceneObject;
import anja.swing.SimpleFileFilter;

import anja.swing.event.SceneListener;

import anja.util.ColorName;
import anja.util.InfiniteStringEnumeration;


/**
* Editor zur Erzeugung und Darstellung von mathematischen Funktionen.
*
* @version 0.7 22.08.2004
* @author Sascha Ternes
*/

public class FunctionEditor
extends Editor
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * geaenderter Text des Menues <i>Scene</i>; nun <i>Functions</i>
   */
   public static final String ITEM_FUNCTIONS = "Functions";

   /**
   * Text des Menuepunkts <i>Functions -> Create new function</i>
   */
   public static final String ITEM_FUNCTIONS_CREATE = "Create new function";
   /**
   * Text des Menuepunkts <i>Functions -> Create new function -> Constant
   * function</i>
   */
   public static final String ITEM_NEW_CONSTANT = "Constant function";
   /**
   * Text des Menuepunkts <i>Functions -> Create new function -> Linear
   * function</i>
   */
   public static final String ITEM_NEW_LINEAR = "Linear function";
   /**
   * Text des Menuepunkts <i>Functions -> Create new function -> Quadratic
   * function</i>
   */
   public static final String ITEM_NEW_QUADRATIC = "Quadratic function";
   /**
   * Text des Menuepunkts <i>Functions -> Create new function -> Polynomial</i>
   */
   public static final String ITEM_NEW_POLYNOMIAL = "Polynomial";
   /**
   * Text des Menuepunkts <i>Functions -> Create new function -> Envelope</i>
   */
   public static final String ITEM_NEW_ENVELOPE = "Envelope";

   /**
   * Text des Menuepunkts <i>Functions -> Draw Mode</i>
   */
   public static final String ITEM_FUNCTIONS_DRAW_MODE = "Draw mode";
   /**
   * Text des Menuepunkts <i>Functions -> Draw mode -> Single points</i>
   */
   public static final String ITEM_SINGLE_POINTS = "Single points";
   /**
   * Text des Menuepunkts <i>Functions -> Draw mode -> Connected points</i>
   */
   public static final String ITEM_CONNECTED_POINTS = "Connected points";

   /**
   * Text des <i>Edit</i>-Buttons im Funktionslistenpanel
   */
   public static final String ITEM_FUNCTION_EDIT = "Edit";

   /**
   * ActionCommand des Menuepunkts <i>Functions -> Create new function ->
   * Constant function</i>
   */
   public static final String MENU_NEW_CONSTANT = "f_new_constant";
   /**
   * ActionCommand des Menuepunkts <i>Functions -> Create new function ->
   * Linear function</i>
   */
   public static final String MENU_NEW_LINEAR = "f_new_linear";
   /**
   * ActionCommand des Menuepunkts <i>Functions -> Create new function ->
   * Quadratic function</i>
   */
   public static final String MENU_NEW_QUADRATIC = "f_new_quadratic";
   /**
   * ActionCommand des Menuepunkts <i>Functions -> Create new function ->
   * Polynomial</i>
   */
   public static final String MENU_NEW_POLYNOMIAL = "f_new_polynomial";
   /**
   * ActionCommand des Menuepunkts <i>Functions -> Create new function ->
   * Envelope</i>
   */
   public static final String MENU_NEW_ENVELOPE = "f_new_envelope";

   /**
   * ActionCOmmand des Menuepunkts <i>Functions -> Draw mode -> Single
   * points</i>
   */
   public static final String MENU_SINGLE_POINTS = "draw_mode_single";
   /**
   * ActionCommand des Menuepunkts <i>Functions -> Draw mode -> Connected
   * points</i>
   */
   public static final String MENU_CONNECTED_POINTS = "draw_mode_connected";

   /**
   * ActionCommand des <i>Edit</i>-Buttons im Funktionslistenpanel
   */
   public static final String MENU_FUNCTION_EDIT = "fp_edit";


   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * fortlaufende Aufzaehlung fuer Bezeichner von konstanten Funktionen
   */
   protected Enumeration _const_names;
   /**
   * fortlaufende Aufzaehlung fuer Bezeichner von linearen Funktionen
   */
   protected Enumeration _line_names;
   /**
   * fortlaufende Aufzaehlung fuer Bezeichner von quadratischen Funktionen
   */
   protected Enumeration _para_names;
   /**
   * fortlaufende Aufzaehlung fuer Polynomfunktionsbezeichner
   */
   protected Enumeration _poly_names;
   /**
   * fortlaufende Aufzaehlung fuer Konturfunktionsbezeichner
   */
   protected Enumeration _env_names;

   /**
   * das Menue <i>Functions -> Create new function</i>
   */
   protected JMenu _create_menu;
   /**
   * das Menue <i>Functions -> Draw mode</i>
   */
   protected JMenu _draw_mode_menu;

   /**
   * der <i>Edit</i>-Button des Funktionslistenpanels
   */
   protected JButton _function_edit_button;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt einen neuen Funktioneneditor. Dieser registriert sich im
   * uebergebenen Register-Objekt.
   *
   * @param register das Register-Objekt
   */
   public FunctionEditor(
      Register register
   ) {

      super( register );
      // Default-Funktionsbezeichnungen:
      _default_names = new InfiniteStringEnumeration( "f" );
      _const_names = new InfiniteStringEnumeration( "const" );
      _line_names = new InfiniteStringEnumeration( "line" );
      _para_names = new InfiniteStringEnumeration( "para" );
      _poly_names = new InfiniteStringEnumeration( "poly" );
      _env_names = new InfiniteStringEnumeration( "env" );

   } // FunctionEditor


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert das <i>Functions</i>-Menue, das im Menue des uebergeordneten Applets
   * oder der uebergeordneten Anwendung verwendet werden kann.<br>
   * Wenn das Applet oder die Anwendung keine eigenen Aktionen definiert, kann
   * als Listener <code>null</code> uebergeben werden.<p>
   *
   * <i>Hinweis: das </i>Functions</i>-Menue ist lediglich ein umbenanntes
   * </i>Scene</i>-Menue; diese Methode ruft lediglich die Methode
   * <code>createSceneMenu(...)</code> auf.</i>
   *
   * @param listener der Listener fuer die Menuepunkte des Menues, der bei Auswahl
   *        eines Menuepunkts benachrichtigt wird, oder <code>null</code>
   * @return das Menue, das die Menuepunkte des <i>Functions</i>-Menues enthaelt
   */
   public JMenu createFunctionsMenu(
      ActionListener listener
   ) {

      return createSceneMenu( listener );

   } // createFunctionsMenu


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public JMenu createSceneMenu(
      ActionListener listener
   ) {

      if ( _scene_menu == null ) {
         super.createSceneMenu( listener );
         _scene_menu.setText( ITEM_FUNCTIONS );

         _create_menu = new JMenu( ITEM_FUNCTIONS_CREATE );

         JMenuItem item = new JMenuItem( ITEM_NEW_CONSTANT );
         item.setActionCommand( MENU_NEW_CONSTANT );
         item.addActionListener( this );
         _create_menu.add( item );
         item = new JMenuItem( ITEM_NEW_LINEAR );
         item.setActionCommand( MENU_NEW_LINEAR );
         item.addActionListener( this );
         _create_menu.add( item );
         item = new JMenuItem( ITEM_NEW_QUADRATIC );
         item.setActionCommand( MENU_NEW_QUADRATIC );
         item.addActionListener( this );
         _create_menu.add( item );
         _create_menu.addSeparator();

         item = new JMenuItem( ITEM_NEW_POLYNOMIAL );
         item.setActionCommand( MENU_NEW_POLYNOMIAL );
         item.addActionListener( this );
         _create_menu.add( item );
         _create_menu.addSeparator();

         item = new JMenuItem( ITEM_NEW_ENVELOPE );
         item.setActionCommand( MENU_NEW_ENVELOPE );
         item.addActionListener( this );
         _create_menu.add( item );
         _scene_menu.add( _create_menu );
 
         _scene_menu.add( _create_menu, 0 );
         _scene_menu.add( new JSeparator(), 1 );

         _draw_mode_menu = new JMenu( ITEM_FUNCTIONS_DRAW_MODE );
         ButtonGroup group = new ButtonGroup();
         item = new JRadioButtonMenuItem( ITEM_SINGLE_POINTS );
         item.setActionCommand( MENU_SINGLE_POINTS );
         item.addActionListener( this );
         group.add( item );
         _draw_mode_menu.add( item );
         item = new JRadioButtonMenuItem( ITEM_CONNECTED_POINTS, true );
         item.setActionCommand( MENU_CONNECTED_POINTS );
         item.addActionListener( this );
         group.add( item );
         _draw_mode_menu.add( item );
         _scene_menu.add( _draw_mode_menu, 2 );
      } // if

      return _scene_menu;

   } // createSceneMenu


   /**
   * Liefert das Funktionslistenpanel, das im uebergeordneten Applet oder in der
   * uebergeordneten Anwendung verwendet werden kann.<br>
   *
   * <i>Hinweis: dieses Panel ist lediglich ein umbenanntes
   * Szenenobjekt-Listenpanel; diese Methode ruft lediglich die Methode
   * <code>createSceneObjectListPanel(...)</code> auf.</i>
   *
   * @return das Panel
   */
   public JPanel createFunctionListPanel() {

      return createSceneObjectListPanel();

   } // createFunctionListPanel


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public JPanel createSceneObjectListPanel() {

      if ( _scene_object_list_panel == null ) {
         super.createSceneObjectListPanel();
         _function_edit_button = new JButton( ITEM_FUNCTION_EDIT );
         _function_edit_button.setEnabled( false );
         _function_edit_button.setActionCommand( MENU_FUNCTION_EDIT );
         _function_edit_button.addActionListener( this );
         _scene_object_list_panel.add( _function_edit_button, 1 );
      } // if
      return _scene_object_list_panel;

   } // createSceneObjectListPanel


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public String nextName(
      Object who
   ) {

           if ( who instanceof ConstantFunctionPlugin )
         return (String) _const_names.nextElement();
      else if ( who instanceof LinearFunctionPlugin )
         return (String) _line_names.nextElement();
      else if ( who instanceof QuadraticFunctionPlugin )
         return (String) _para_names.nextElement();
      else if ( who instanceof PolynomialPlugin )
         return (String) _poly_names.nextElement();
      else if ( who instanceof RealEnvelopePlugin )
         return (String) _env_names.nextElement();
      else
         return super.nextName( who );

   } // nextName


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      String cmd = e.getActionCommand();

      // Erzeugen neuer Funktionen:
      JPluginDialog plugin = null;
      if ( cmd.equals( MENU_NEW_CONSTANT ) )
         plugin = _plugin_provider.createPlugin( _reg,
                                      ConstantFunctionPlugin.class.getName() );
      else if ( cmd.equals( MENU_NEW_LINEAR ) )
         plugin = _plugin_provider.createPlugin( _reg,
                                        LinearFunctionPlugin.class.getName() );
      else if ( cmd.equals( MENU_NEW_QUADRATIC ) )
         plugin = _plugin_provider.createPlugin( _reg,
                                     QuadraticFunctionPlugin.class.getName() );
      else if ( cmd.equals( MENU_NEW_POLYNOMIAL ) )
         plugin = _plugin_provider.createPlugin( _reg,
                                            PolynomialPlugin.class.getName() );
      else if ( cmd.equals( MENU_NEW_ENVELOPE ) )
         plugin = _plugin_provider.createPlugin( _reg,
                                          RealEnvelopePlugin.class.getName() );
      if ( plugin != null )
         plugin.createNewObject();

      // Editieren der selektierten Funktion:
      else if ( cmd.equals( MENU_FUNCTION_EDIT ) )
         ( (FunctionObject)
               _scene_object_list.getSelectedItem() ).getPlugin().editObject();

      // Listener der Superklasse aufrufen:
      else super.actionPerformed( e );

   } // actionPerformed


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public void itemStateChanged(
      ItemEvent e
   ) {

      super.itemStateChanged( e );
      _function_edit_button.setEnabled(
                                     _scene_object_delete_button.isEnabled() );

   } // itemStateChanged


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public JPluginDialog createPlugin(
      Register register,
      String scene_object_class
   ) {

           if ( scene_object_class.endsWith( "ConstantFunctionPlugin" ) )
         return new ConstantFunctionPlugin( register );
      else if ( scene_object_class.endsWith( "LinearFunctionPlugin" ) )
         return new LinearFunctionPlugin( register );
      else if ( scene_object_class.endsWith( "QuadraticFunctionPlugin" ) )
         return new QuadraticFunctionPlugin( register );
      else if ( scene_object_class.endsWith( "PolynomialPlugin" ) )
         return new PolynomialPlugin( register );
      else if ( scene_object_class.endsWith( "RealEnvelopePlugin" ) )
         return new RealEnvelopePlugin( register );
      else
         return null;

   } // createPlugin


   /*
   * Diese Methode wird nicht verwendet.
   *
   * @param register das <code>Register</code>-Objekt fuer das Plugin
   * @param object das Szenenobjekt, mit dem das Plugin initialisiert wird
   * @return <code>null</code>
   */
   public JPluginDialog createPlugin(
      Register register,
      SceneObject object
   ) {

      return null;

   } // createPlugin


   // ************************************************************************
   // Protected methods
   // ************************************************************************

   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   protected void _initFileChooser() {

      _file_chooser = new JFileChooser();
      _file_filter = new SimpleFileFilter(
                                        "XML function scenes (*.fns)", "fns" );
      _file_chooser.setFileFilter( _file_filter );

   } // _initFileChooser


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   protected void _addObject(
      SceneObject object,
      Object source
   ) {

      if ( ( (FunctionObject) object ).getFunction() instanceof RealEnvelope )
         _reg.scene.insert( object, source );
      else
         _reg.scene.add( object, source );

   } // _addObject


} // FunctionEditor
