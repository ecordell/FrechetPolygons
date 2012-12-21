package anja.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import javax.swing.border.EmptyBorder;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anja.swing.event.SceneEvent;
import anja.swing.event.SceneListener;


/**
* Abstrakte Klasse eines Editor-Plugins zur dialogunterstuetzten Bearbeitung von
* Szenenobjekten.
*
* @version 0.9 26.08.2004
* @author Sascha Ternes
*/

public abstract class JPluginDialog
extends JDialog
implements ActionListener,
           ChangeListener,
           SceneListener,
           WindowListener
{

   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Default-Titel eines Plugins:
   protected static final String _DEFAULT_TITLE = "Plugin";
   // Default-Groesse eines Dialogs:
   private static final int _DEFAULT_WIDTH = 312;
   private static final int _DEFAULT_HEIGHT = 208;


   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * Aufschrift und ActionCommand des OK-Buttons
   */
   public static final String ACTION_OK = "OK";
   /**
   * Aufschrift und ActionCommand des Apply-Buttons
   */
   public static final String ACTION_APPLY = "Apply";
   /**
   * Aufschrift und ActionCommand des Cancel-Buttons
   */
   public static final String ACTION_CANCEL = "Cancel";

   /**
   * ActionCommand des Eingabefelds fuer die Funktionsbezeichnung
   */
   public static final String ACTION_NAME = "Name";


   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * das Register der Komponenten
   */
   protected Register _reg;

   /**
   * Hauptpanel des Dialogs
   */
   protected Container _panel;
   /**
   * Sammlung der Panel zum Bearbeiten des Szenenobjekts
   */
   protected JTabbedPane _content;
   /**
   * Panel mit den Buttons am unteren Ende des Dialogs
   */
   protected JPanel _buttons;

   /**
   * Panel mit Optionen fuer die Darstellung des Szenenobjekts
   */
   protected JPanel _options_panel;
   /**
   * optionales Eingabefeld fuer die Objektbezeichnung:
   */
   protected JTextField _name;
   /**
   * optionales Anzeigefeld fuer die Objektfarbe:
   */
   protected JColorButton _color;

   /**
   * Panel mit Informationen ueber das Szenenobjekt
   */
   protected JPanel _info_panel;
   /**
   * Anzeigefeld fuer die Anzeigeprioritaet:
   */
   protected JTextFieldInteger _priority;

   /**
   * Flag, das waehrend der Erzeugung eines neuen Szenenobjekts gesetzt ist
   */
   protected boolean _creating;

   /**
   * das Szenenobjekt, das durch diese Plugin-Instanz verwaltet wird
   */
   protected SceneObject _object;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /*
   * Verbotener Konstruktor.
   */
   private JPluginDialog() {};


   /**
   * Erzeugt das Grundgeruest eines Plugins zur Erzeugung eines neuen
   * Szenenobjekts.
   *
   * @param register das Register-Objekt
   */
   public JPluginDialog(
      Register register
   ) {

      // Initialisierungen:
      super( register.editor.getOwner(), _DEFAULT_TITLE );
      _reg = register;

      // Hauptfenster-Layout:
      JFrame owner = _reg.editor.getOwner();
      int x = 100;
      int y = 100;
      if ( owner != null ) {
         x = owner.getX() + ( owner.getWidth() - _DEFAULT_WIDTH ) / 2;
         y = owner.getY() + ( owner.getHeight() - _DEFAULT_HEIGHT ) / 2;
      } // if
      setLocation( x, y );
      setSize( _DEFAULT_WIDTH, _DEFAULT_HEIGHT );
      setResizable( false );

      // Layout des Inhaltspanels:
      _content = new JTabbedPane();
      _options_panel = new JPanel( new GridLayout( 4, 1 ) );
      _options_panel.setBorder( new EmptyBorder( 0, 6, 0, 6 ) );
      _buildOptionsPanel();
      _content.add( "Options", _options_panel );
      _info_panel = new JPanel( new GridLayout( 4, 1 ) );
      _info_panel.setBorder( new EmptyBorder( 0, 6, 0, 6 ) );
      _buildInfoPanel();
      _content.add( "Information", _info_panel );

      // Layout des Button-Panels:
      _buttons = new JPanel();
      JButton button;
      button = new JButton( ACTION_OK );
      button.setActionCommand( ACTION_OK );
      button.addActionListener( this );
      _buttons.add( button );
      button = new JButton( ACTION_APPLY );
      button.setActionCommand( ACTION_APPLY );
      button.addActionListener( this );
      _buttons.add( button );
      button = new JButton( ACTION_CANCEL );
      button.setActionCommand( ACTION_CANCEL );
      button.addActionListener( this );
      _buttons.add( button );

      // Layout des Hauptpanels:
      _panel = getContentPane();
      _panel.setLayout( new BorderLayout() );
      _panel.add( _content, BorderLayout.CENTER );
      _panel.add( _buttons, BorderLayout.SOUTH );

      // als Listener bei der Szene registrieren:
      _reg.scene.addSceneListener( this );

   } // JPluginDialog


   /**
   * Erzeugt ein Plugin zur Bearbeitung des spezifizierten Szenenobjekts.
   *
   * @param register das Register-Objekt
   * @param object das Szenenobjekt
   */
   public JPluginDialog(
      Register register,
      SceneObject object
   ) {

      this( register );
      _object = object;
      _useObject( _object );

   } // JPluginDialog


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Oeffnet einen Dialog zum Erzeugen eines neuen Szenenobjekts. Wenn der
   * Dialog mit dem OK-Button beendet wird oder wenn der Apply-Button gedrueckt
   * wird, wird ein neues <code>SzeneObject</code> an den registrierten
   * <code>Editor</code> uebergeben.<p>
   *
   * Diese Methode ist optional, da ein Szenenobjekt im Allgemeinen vom Editor
   * selbst erzeugt werden kann und ein Plugin lediglich zur Bearbeitung eines
   * vorhandenen Objekts dient.
   */
   public void createNewObject() {

      _creating = true;
      setVisible( true );

   } // createNewObject


   /**
   * Oeffnet einen Dialog zum Editieren des Objekts. Wenn der Dialog mit dem
   * OK-Button beendet wird oder wenn der Apply-Button gedrueckt wird, wird
   * das <code>SzeneObject</code> aktualisiert und an den registrierten
   * <code>Editor</code> uebergeben.
   */
   public void editObject() {

      setVisible( true );

   } // editObject


   // *************************************************************************
   // Interface ActionListener
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus ActionListener kopiert]
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      String cmd = e.getActionCommand();
           if ( cmd.equals( ACTION_OK ) )
         _actionOK();
      else if ( cmd.equals( ACTION_APPLY ) )
         _actionApply();
      else if ( cmd.equals( ACTION_CANCEL ) )
         _actionCancel();

   } // actionPerformed


   // *************************************************************************
   // Interface ChangeListener
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus ChangeListener kopiert]
   */
   public void stateChanged(
      ChangeEvent e
   ) {

      _actionApply();

   } // stateChanged


   // *************************************************************************
   // Interface SceneListener
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus SzeneListener kopiert]
   */
   public void objectRemoved(
      SceneEvent e
   ) {

      SceneObject object = e.getObject();
      if ( object.getPlugin() == this ) {
         e.getScene().removeSceneListener( this );
         _actionCancel();
      } // if

   } // objectRemoved


   /*
   * [javadoc-Beschreibung wird aus SzeneListener kopiert]
   */
   public void objectUpdated(
      SceneEvent e
   ) {

      if ( e.getSource() == this ) return;
      SceneObject object = e.getObject();
      if ( object.getPlugin() == this )
         _useObject( _object );

   } // objectUpdated


   /*
   * [javadoc-Beschreibung wird aus SzeneListener kopiert]
   */
   public void prioritiesChanged(
      SceneEvent e
   ) {

      if ( _object != null )
         _priority.setValue( _object.getPriority() );

   } // prioritiesChanged


   // folgende Methode wird nicht verwendet:
   public void objectAdded( SceneEvent e ) {}
   public void objectSelected( SceneEvent e ) {}


   // *************************************************************************
   // Interface WindowListener
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus WindowListener kopiert]
   */
   public void windowClosing(
      WindowEvent e
   ) {

      _actionCancel();

   } // windowClosing


   // folgende Methoden werden nicht verwendet:
   public void windowActivated(WindowEvent e) {}
   public void windowClosed(WindowEvent e) {}
   public void windowDeactivated(WindowEvent e) {}
   public void windowDeiconified(WindowEvent e) {}
   public void windowIconified(WindowEvent e) {}
   public void windowOpened(WindowEvent e) {}


   // *************************************************************************
   // Protected methods
   // *************************************************************************

   /**
   * Wird am Ende der Initialisierung eines Plugins fuer ein vorhandenes
   * Szenenobjekt aufgerufen, um das Plugin mit den Daten des Objekts zu
   * initialisieren.
   *
   * @param object das Szenenobjekt
   */
   protected void _useObject(
      SceneObject object
   ) {

      _name.setText( object.getName() );
      _color.setColor( object.getColor() );
      _priority.setValue( object.getPriority() );

   } // useObject


   /**
   * Wird bei der Initialisierung des Plugins aufgerufen und baut das Panel
   * <code>_options_panel</code> auf, das zur Optionseinstellung fuer das
   * jeweilige Objekt dient.
   */
   protected void _buildOptionsPanel() {

      // Objektbezeichnung:
      JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      panel.add( new JLabel( "Name" ) );
      _name = new JTextField( _reg.editor.nextName( this ), 4 );
      _name.setActionCommand( ACTION_NAME );
      _name.addActionListener( this );
      panel.add( _name );
      _options_panel.add( panel );
      // Farbeinstellung:
      panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      _color = new JColorButton( this ); // noch ungefaerbt
      panel.add( _color );
      panel.add( new JLabel( " Display color (click to edit)" ) );
      _options_panel.add( panel );

   } // _buildOptionsPanel


   /**
   * Wird bei der Initialisierung des Plugins aufgerufen und baut das Panel
   * <code>_info_panel</code> auf, das zur Anzeige von Informationen ueber das
   * jeweilige Objekt dient.
   */
   protected void _buildInfoPanel() {

      JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      panel.add( new JLabel( "Display priority" ) );
      _priority = new JTextFieldInteger( 0 );
      _priority.setEditable( false );
      panel.add( _priority );
      _info_panel.add( panel );

   } // _buildInfoPanel


   /**
   * Wird zu Anfang der Methode <code>_actionApply()</code> aufgerufen und muss
   * dafuer sorgen, dass die Variable <code>_object</code> mit den aktuellen
   * Einstellungen im Dialog aktualisiert wird.<br>
   * Wenn ein neues Objekt erzeugt wird, ist <code>_object</code> zunaechst
   * <code>null</code>; sonst muss lediglich aktualisiert werden. Eine moegliche
   * Implementierung dieser Methode koennte folgendermassen aussehen:<p>
   *
   * <pre>protected void _updateObject() {
   *    // falls ein neues Objekt erzeugt wird, initialisieren:
   *    if ( _creating ) {
   *       _object = new [SceneObject]( ... );
   *    } // if
   *    [Objektparameter aktualisieren]
   * } // _updateFunction</pre><p>
   */
   abstract protected void _updateObject();


   /**
   * Wird im Zuge der Methode <code>_actionApply()</code> aufgerufen und sorgt
   * dafuer, dass die Optionsfelder der Variable <code>_object</code> mit den
   * aktuellen Optionseinstellungen im Dialog aktualisiert wird.
   */
   protected void _updateOptions() {

      _object.setName( _name.getText() );
      _object.setColor( _color.getColor() );

   } // _updateOptions


   /**
   * Aktion, die ausgefuehrt wird, wenn der OK-Button des Plugins gedrueckt wird.
   */
   protected void _actionOK() {

      _actionApply();
      _actionCancel();

   } // _action_OK


   /**
   * Aktion, die ausgefuehrt wird, wenn der Apply-Button des Plugins gedrueckt
   * wird.
   */
   protected void _actionApply() {

      _updateObject();
      _updateOptions();
      // ein neues Objekt an den Editor liefern:
      if ( _creating ) {
         _creating = false;
         _reg.editor.addObject( _object, this );
      // sonst ein bestehendes Objekt im Editor ersetzen:
      } else
         _reg.editor.replaceObject( _object, this );

   } // _actionApply


   /**
   * Aktion, die ausgefuehrt wird, wenn der Cancel-Button des Plugins gedrueckt
   * wird.
   */
   protected void _actionCancel() {

      dispose();

   } // _action_OK


} // JPluginDialog
