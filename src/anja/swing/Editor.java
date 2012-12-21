package anja.swing;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.awt.geom.Point2D;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import org.jdom.input.SAXBuilder;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import anja.swing.event.SceneEvent;
import anja.swing.event.SceneListener;

import anja.util.ColorName;
import anja.util.InfiniteStringEnumeration;


/**
* Abstrakte Grundklasse fuer einen Editor, der auf einem Anzeigepanel arbeitet.
*
* @version 0.7 22.10.2004
* @author Sascha Ternes
*/

public abstract class Editor
implements ActionListener,
           ItemListener,
           PluginProvider,
           SceneListener,
           WindowListener
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * Text des Menues <i>File</i>
   */
   public static final String ITEM_FILE = "File";
   /**
   * Text des Menuepunkts <i>File -> New</i>
   */
   public static final String ITEM_FILE_NEW = "New";
   /**
   * Text des Menuepunkts <i>File -> Open...</i>
   */
   public static final String ITEM_FILE_OPEN = "Open...";
   /**
   * Text des Menuepunkts <i>File -> Save</i>
   */
   public static final String ITEM_FILE_SAVE = "Save";
   /**
   * Text des Menuepunkts <i>File -> Save as...</i>
   */
   public static final String ITEM_FILE_SAVEAS = "Save as...";
   /**
   * Text des Menuepunkts <i>File -> Exit</i>
   */
   public static final String ITEM_FILE_EXIT = "Exit";

   /**
   * Text des Menues <i>Scene</i>
   */
   public static final String ITEM_SCENE = "Scene";
   /**
   * Text des Menuepunkts <i>Scene -> Paint priority...</i>
   */
   public static final String ITEM_SCENE_PRIO_SHOW = "Paint priority...";
   /**
   * Text des Menuepunkts <i>Scene -> Hide priority panel</i>
   */
   public static final String ITEM_SCENE_PRIO_HIDE = "Hide priority panel";

   /**
   * Text des obersten unveraenderlichen Eintrags in der Szenenobjektliste des
   * Szenenobjekt-Listenpanels
   */
   public static final String ITEM_OBJECT_LIST_NONE = "(none selected)";
   /**
   * Text des <i>Delete</i>-Buttons im Szenenobjekt-Listenpanel
   */
   public static final String ITEM_SCENE_OBJECT_DELETE = "Delete";

   /**
   * ActionCommand des Menuepunkts <i>File -> New</i>
   */
   public static final String MENU_FILE_NEW = "file_new";
   /**
   * ActionCommand des Menuepunkts <i>File -> Open...</i>
   */
   public static final String MENU_FILE_OPEN = "file_open";
   /**
   * ActionCommand des Menuepunkts <i>File -> Save</i>
   */
   public static final String MENU_FILE_SAVE = "file_save";
   /**
   * ActionCommand des Menuepunkts <i>File -> Save as...</i>
   */
   public static final String MENU_FILE_SAVEAS = "file_saveas";
   /**
   * ActionCommand des Menuepunkts <i>File -> Exit</i>
   */
   public static final String MENU_FILE_EXIT = "file_exit";

   /**
   * ActionCommand des Menuepunkts <i>Scene -> Paint priority...</i>
   */
   public static final String MENU_SCENE_PRIO = "scene_prio";

   /**
   * ActionCommand des <i>Delete</i>-Buttons im Szenenobjekt-Listenpanel
   */
   public static final String MENU_SCENE_OBJECT_DELETE = "sopanel_delete";


   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Meldung beim Ueberschreiben einer vorhandenen Datei:
   private static final String _MSG_EXISTS =
                       "A file with the chosen name does exist. Overwrite it?";

   // Meldung beim Einlesen einer falschen Datei:
   private static final String _MSG_INVALID =
                      "The chosen file does not have a valid format. Aborted.";

   // Meldung beim Einlesen einer Datei mit falschem Editor:
   private static final String _MSG_EDITOR =
             "The chosen file was created by an incompatible editor. Aborted.";

   // Meldung beim Einlesen einer Datei mit falschem Editor:
   private static final String _MSG_VERSION =
                "The chosen file's editor version is not compatible. Aborted.";

   // Meldung beim Einlesen einer Datei ohne Szenen:
   private static final String _MSG_NO_SCENE =
                       "The chosen file does not contain any scenes. Aborted.";

   // Meldung beim Parsen einer Szenendatei:
   private static final String _MSG_PARSE =
                "An error occurred while parsing the chosen XML scene file:\n";


   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * das Register der Komponenten
   */
   protected Register _reg;

   /**
   * das uebergeordnete Fensterobjekt bei einer Anwendung
   */
   protected JFrame _owner;

   /**
   * das <i>File</i>-Menue
   */
   protected JMenu _file_menu;

   /**
   * das <i>Coordinate system</i>-Menue
   */
   protected JMenu _cosystem_menu;

   /**
   * das <i>Scene</i>-Menue
   */
   protected JMenu _scene_menu;
   /**
   * der Menuepunkt <i>Scene -> Paint priority...</i>
   */
   protected JMenuItem _item_scene_prio;

   /**
   * das Szenenobjekt-Listenpanel
   */
   protected JPanel _scene_object_list_panel;
   /**
   * die Szenenobjektliste des Szenenobjekt-Listenpanels
   */
   protected JComboBox _scene_object_list;
   /**
   * der <i>Delete</i>-Button des Szenenobjekt-Listenpanels
   */
   protected JButton _scene_object_delete_button;
   /**
   * interne Hilfsvariable fuer die Unterstuetzung der externen Objektauswahl
   */
   protected boolean _enable_scene_object_list_state_changed;

   /**
   * die registrierten Listener fuer das <i>File</i>-Menue
   */
   protected Vector _file_listeners;
   /**
   * die registrierten Listener fuer das <i>Coordinate system</i>-Menue
   */
   protected Vector _cosystem_listeners;
   /**
   * die registrierten Listener fuer das <i>Scene</i>-Menue
   */
   protected Vector _scene_listeners;

   /**
   * fortlaufende Aufzaehlung fuer Standard-Objektbezeichner
   */
   protected Enumeration _default_names;

   /**
   * fortlaufende Aufzaehlung fuer Default-Objektfarben
   */
   protected Enumeration _default_colors;

   /**
   * Dateiauswahldialog zum Laden und Speichern
   */
   protected JFileChooser _file_chooser;
   /**
   * der Dateifilter fuer den Dateiauswahldialog
   */
   protected SimpleFileFilter _file_filter;
   /**
   * die aktuelle Datei, in die die editierte Szene gespeichert wird
   */
   protected File _current_file;

   /**
   * die Instanz, die fuer ein Szenenobjekt ein Plugin bereitstellt (per
   * Default <code>this</code>
   */
   protected PluginProvider _plugin_provider = this;


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // Flag fuer die Menuepunkte File -> Open.../Save/Save as...:
   private boolean _add_opensave_menu_items;
   // Flag fuer den Menuepunkt File -> Exit:
   private boolean _add_exit_menu_item;

   // Flag fuer den Menuepunkt Scene -> Paint priority...:
   private boolean _add_prio_menu_item;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /*
   * Verbotener Konstruktor.
   */
   private Editor() {}


   /**
   * Erzeugt einen neuen Editor. Dieser registriert sich im uebergebenen
   * Register-Objekt.
   *
   * @param register das Register-Objekt
   */
   public Editor(
      Register register
   ) {

      register.editor = this;
      _reg = register;
      _file_listeners = new Vector( 1 );
      _cosystem_listeners = new Vector( 1 );
      _scene_listeners = new Vector( 1 );
      _default_names = new InfiniteStringEnumeration( "obj" );
      _default_colors = ColorName.universal10Colors();
      _reg.cosystem.saveSettings();
      _reg.scene.addSceneListener( this );

   } // Editor


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert das uebergeordnete <code>JFrame</code>-Objekt.
   *
   * @return das uebergeordnete Fenster
   */
   public JFrame getOwner() {

      return _owner;

   } // getOwner


   /**
   * Setzt das uebergeordnete <code>JFrame</code>-Objekt fuer die vom Editor
   * erzeugten Plugins.
   *
   * @param frame das uebergeordnete Fenster
   */
   public void setOwner(
      JFrame frame
   ) {

      _owner = frame;

   } // setOwner


   /**
   * Registriert einen neuen {@link PluginProvider PluginProvider}.
   *
   * @param provider die neue <code>PluginProvider</code>-Instanz
   */
   public void setPluginProvider(
      PluginProvider provider
   ) {

      if ( provider != null )
         _plugin_provider = provider;

   } // setPluginProvider


   /**
   * Liefert ein neu erzeugtes <code>JMenuBar</code>-Menue mit den Menues des
   * Editors, das im uebergeordneten Applet oder der uebergeordneten Anwendung
   * verwendet werden kann.
   *
   * @param listener der Listener fuer die Menuepunkte der Menues, der bei Auswahl
   *        eines Menuepunkts benachrichtigt wird
   * @return ein <code>JMenuBar</code>-Objekt, das alle Editormenues enthaelt
   */
   public JMenuBar createJMenuBar(
      ActionListener listener
   ) {

      JMenuBar bar = new JMenuBar();
      bar.add( createFileMenu( listener ) );
      bar.add( createCoordinateSystemMenu( listener ) );
      bar.add( createSceneMenu( listener ) );
      if ( _reg.extension != null ) {
         JMenu ext = _reg.extension.createExtensionMenu();
         if ( ext != null )
            bar.add( ext );
      } // if
      return bar;

   } // createJMenuBar


   /**
   * Liefert das <i>File</i>-Menue, das im Menue des uebergeordneten Applets oder
   * der uebergeordneten Anwendung verwendet werden kann.<br>
   * Wenn das Applet oder die Anwendung keine eigenen Aktionen definiert, kann
   * als Listener <code>null</code> uebergeben werden.
   *
   * @param listener der Listener fuer die Menuepunkte des Menues, der bei Auswahl
   *        eines Menuepunkts benachrichtigt wird, oder <code>null</code>
   * @return das Menue, das die Menuepunkte des <i>File</i>-Menues enthaelt
   */
   public JMenu createFileMenu(
      ActionListener listener
   ) {

      
      if ( _file_menu == null ) {
         _file_menu = new JMenu( ITEM_FILE );
         JMenuItem item = new JMenuItem( ITEM_FILE_NEW );
         item.setActionCommand( MENU_FILE_NEW );
         item.addActionListener( this );
         _file_menu.add( item );
         if ( _add_opensave_menu_items ) {
            item = new JMenuItem( ITEM_FILE_OPEN );
            item.setActionCommand( MENU_FILE_OPEN );
            item.addActionListener( this );
            _file_menu.add( item );
            item = new JMenuItem( ITEM_FILE_SAVE );
            item.setActionCommand( MENU_FILE_SAVE );
            item.addActionListener( this );
            _file_menu.add( item );
            item = new JMenuItem( ITEM_FILE_SAVEAS );
            item.setActionCommand( MENU_FILE_SAVEAS );
            item.addActionListener( this );
            _file_menu.add( item );
         } // if
         if ( _add_exit_menu_item ) {
            _file_menu.addSeparator();
            item = new JMenuItem( ITEM_FILE_EXIT );
            item.setActionCommand( MENU_FILE_EXIT );
            item.addActionListener( this );
            _file_menu.add( item );
         } // if
      
         if ( listener != null ) _file_listeners.add( listener );
      } // if
      return _file_menu;

   } // createFileMenu


   /**
   * Liefert das <i>Coordinate system</i>-Menue, das im Menue des uebergeordneten
   * Applets oder der uebergeordneten Anwendung verwendet werden kann.<br>
   * Diese Methode ruft lediglich die entsprechende Methode des aktuellen
   * Koordinatensystems auf, allerdings wird die Ereignisbehandlung selbst
   * ausgefuehrt. Konkret bedeutet das, dass nach einer Aenderung des
   * Koordinatensystems das Anzeigepanel neugezeichnet wird und dann der
   * spezifizierte Listener benachrichtigt wird. In aller Regel kann also der
   * Listener <code>null</code> sein.
   *
   * @param listener der Listener fuer die Menuepunkte des Menues, der bei Auswahl
   *        eines Menuepunkts benachrichtigt wird, oder <code>null</code>
   * @return das Menue, das die Menuepunkte des <i>File</i>-Menues enthaelt
   */
   public JMenu createCoordinateSystemMenu(
      ActionListener listener
   ) {

      if ( _cosystem_menu == null ) {
         if ( listener != null ) _cosystem_listeners.add( listener );
         _cosystem_menu = _reg.cosystem.createMenu( this );
      } // if
      return _cosystem_menu;

   } // createCoordinateSystemMenu


   /**
   * Liefert das <i>Scene</i>-Menue, das im Menue des uebergeordneten Applets oder
   * der uebergeordneten Anwendung verwendet werden kann.<br>
   * Wenn das Applet oder die Anwendung keine eigenen Aktionen definiert, kann
   * als Listener <code>null</code> uebergeben werden.
   *
   * @param listener der Listener fuer die Menuepunkte des Menues, der bei Auswahl
   *        eines Menuepunkts benachrichtigt wird, oder <code>null</code>
   * @return das Menue, das die Menuepunkte des <i>Scene</i>-Menues enthaelt
   */
   public JMenu createSceneMenu(
      ActionListener listener
   ) {

      if ( _scene_menu == null ) {
         _scene_menu = new JMenu( ITEM_SCENE );
         if ( _add_prio_menu_item ) {
            _item_scene_prio = new JMenuItem( ITEM_SCENE_PRIO_SHOW );
            _item_scene_prio.setActionCommand( MENU_SCENE_PRIO );
            _item_scene_prio.addActionListener( this );
            _scene_menu.add( _item_scene_prio );
         } // if

         if ( listener != null ) _scene_listeners.add( listener );
      } // if
      return _scene_menu;

   } // createSceneMenu


   /**
   * Erzeugt ein Panel, in dem eine <i>ComboBox</i>-Liste der Szenenobjekte
   * dargestellt wird. Enthalten ist auch ein Button zum Loeschen des aktuell
   * selektierten Objekts. Es werden entsprechende Szenenereignisse ausgeloest
   * und die in der Szene registrierten lIstener werden benachrichtigt.<p>
   *
   * Da dieses Panel in etwa die Groesse eines Menuepunkts besitzt, kann es in ein
   * <code>JMenuBar</code>-Objekt eingebaut werden.
   *
   * @return das beschriebene Panel
   */
   public JPanel createSceneObjectListPanel() {

      if ( _scene_object_list_panel == null ) {
         FlowLayout layout = new FlowLayout( FlowLayout.RIGHT );
         layout.setHgap( 0 );
         layout.setVgap( 0 );
         _scene_object_list_panel = new JPanel( layout );
         _scene_object_list = new JComboBox();
         _scene_object_list.addItem( ITEM_OBJECT_LIST_NONE );
         _scene_object_list.setSelectedIndex( 0 );
         _scene_object_list.addItemListener( this );
         _scene_object_list_panel.add( _scene_object_list );
         _scene_object_delete_button = new JButton( ITEM_SCENE_OBJECT_DELETE );
         _scene_object_delete_button.setEnabled( false );
         _scene_object_delete_button.setActionCommand(
                                                    MENU_SCENE_OBJECT_DELETE );
         _scene_object_delete_button.addActionListener( this );
         _scene_object_list_panel.add( _scene_object_delete_button );
         _enable_scene_object_list_state_changed = true;
      } // if
      return _scene_object_list_panel;

   } // createSceneObjectListPanel


   /**
   * Liefert zurueck, ob die Menuepunkte <i>File -> Open...</i>,
   * <i>File -> Save</i> und <i>File -> Save as...</i> im <i>File</i>-Menue
   * enthalten sind.
   *
   * @return <code>true</code>, wenn die Menuepunkte enthalten sind, sonst
   *         <code>false</code>
   */
   public boolean isOpenSaveMenuItemIncluded() {

      return _add_opensave_menu_items;

   } // isOpenSaveMenuIncluded


   /**
   * Liefert zurueck, ob der Menuepunkt <i>File -> Exit</i> im <i>File</i>-Menue
   * enthalten ist.
   *
   * @return <code>true</code>, wenn der Menuepunkt enthalten ist, sonst
   *         <code>false</code>
   */
   public boolean isExitMenuItemIncluded() {

      return _add_exit_menu_item;

   } // isExitMenuItemIncluded


   /**
   * Liefert zurueck, ob der Menuepunkt <i>Scene -> Paint priority...</i> im
   * <i>Scene</i>-Menue enthalten ist.
   *
   * @return <code>true</code>, wenn der Menuepunkt enthalten ist, sonst
   *         <code>false</code>
   */
   public boolean isPaintPriorityMenuItemIncluded() {

      return _add_prio_menu_item;

   } // isPaintPriorityMenuItemIncluded


   /**
   * Sorgt dafuer, dass die Menuepunkte <i>File -> Open...</i>,
   * <i>File -> Save</i> und <i>File -> Save as...</i> in das <i>File</i>-Menue
   * eingefuegt werden.
   */
   public void includeOpenSaveMenuItems() {

      _add_opensave_menu_items = true;
      _initFileChooser();

   } // includeOpenSaveMenuItems


   /**
   * Sorgt dafuer, dass der Menuepunkt <i>File -> Exit</i> in das
   * <i>File</i>-Menue eingefuegt wird.
   */
   public void includeExitMenuItem() {

      _add_exit_menu_item = true;

   } // includeExitMenuItem


   /**
   * Sorgt dafuer, dass der Menuepunkt <i>Scene -> Paint priority...</i> in das
   * <i>Scene</i>-Menue eingefuegt wird. Als Parameter kann die Komponente
   * des Prioritaetspanels uebergeben werden, bei dem sich der Editor als
   * <code>WindowListener</code> registrieren soll, um den Text des Menuepunkts
   * beim Schliessen automatisch anzupassen.
   *
   * @param paint_priority der Dialog, der mit dem Menuepunkt verknuepft ist,
   *        oder <code>null</code>
   */
   public void includePaintPriorityMenuItem(
      JDialog paint_priority
   ) {

      _add_prio_menu_item = true;
      if ( paint_priority != null )
         paint_priority.addWindowListener( this );

   } // includePaintPriorityMenuItem


   /**
   * Liefert die naechste defaultmaessig zu verwendende Bezeichnung fuer ein
   * Szenenobjekt. Welcher String geliefert wird, kann vom spezifizierten
   * Parameter abhaengig gemacht werden, daher sollte diese Methode immer mit
   * dem Parameter <code>this</code> aufgerufen werden.
   *
   * @param who die aufrufende Instanz
   * @return die naechste Objektbezeichnung
   */
   public String nextName(
      Object who
   ) {

      return (String) _default_names.nextElement();

   } // nextName


   /**
   * Liefert die naechste defaultmaessig zu verwendende Zeichenfarbe fuer ein
   * Szenenobjekt. Welche Farbe geliefert wird, kann vom spezifizierten
   * Parameter abhaengig gemacht werden, daher sollte diese Methode immer mit
   * dem Parameter <code>this</code> aufgerufen werden.
   *
   * @param who die aufrufende Instanz
   * @return die naechste Objektfarbe
   */
   public Color nextColor(
      Object who
   ) {

      return (Color) _default_colors.nextElement();

   } // nextColor


   /**
   * Reagiert auf einen Mausklick im freien Bereich des Zeichenfensters.<p>
   *
   * Falls eine Editorerweiterung existiert, wird die dortige Methode
   * {@link EditorExtension#canvasClicked(Point,int) canvasClicked(...)}
   * aufgerufen und ihr Rueckgabewert zurueckgeliefert.<p>
   *
   * Wenn das Ereignis behandelt wurde, soll diese Methode den Wert
   * <code>true</code> liefern, sonst <code>false</code>. Wenn die Erweiterung
   * das Ereignis mit dem Ergebnis <code>true</code> behandelt hat, soll eine
   * abgeleitete Klasse dies beruecksichtigen. Dazu soll folgendes Codebeispiel
   * als Vorbild dienen:<p>
   *<pre>public boolean canvasClicked(Point point, int button) {
   *   // Methode der Superklasse aufrufen:
   *   if (super.canvasClicked(point, button))
   *      // durch Erweiterung behandeltes Ereignis beruecksichtigen:
   *      return true;
   *   // unbehandeltes Ereignis verarbeiten:
   *   (...)
   *}</pre><p>
   *
   * @param point der Punkt des Mausklicks in Weltkoordinaten
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   */
   public boolean canvasClicked(
      Point point,
      int button
   ) {

      if ( _reg.extension != null )
         return _reg.extension.canvasClicked( point, button );
      return false;

   } // canvasClicked


   /**
   * Bietet dem Editor die Moeglichkeit, nach einem Scrollen der Zeichenflaeche
   * Aktionen auszufuehren.
   *
   * Falls eine Editorerweiterung existiert, wird die dortige Methode
   * {@link EditorExtension#canvasScrolled(Point) canvasScrolled(...)}
   * aufgerufen und ihr Rueckgabewert zurueckgeliefert.<p>
   *
   * Fuer weitere Informationen siehe
   * {@link #canvasClicked(Point,int) canvasClicked(...)}.
   *
   * @param point die aktuelle Position des Mauscursors
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   * @see #canvasClicked(Point,int)
   */
   public boolean canvasScrolled(
      Point point
   ) {

      if ( _reg.extension != null )
         return _reg.extension.canvasScrolled( point );
      return false;

   } // canvasScrolled


   /**
   * Bietet dem Editor die Moeglichkeit, nach einem Zoomen der Zeichenflaeche
   * Aktionen auszufuehren.
   *
   * Falls eine Editorerweiterung existiert, wird die dortige Methode
   * {@link EditorExtension#canvasZoomed(Point) canvasZoomed(...)}
   * aufgerufen und ihr Rueckgabewert zurueckgeliefert.<p>
   *
   * Fuer weitere Informationen siehe
   * {@link #canvasClicked(Point,int) canvasClicked(...)}.
   *
   * @param point die aktuelle Position des Mauscursors
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   * @see #canvasClicked(Point,int)
   */
   public boolean canvasZoomed(
      Point point
   ) {

      if ( _reg.extension != null )
         return _reg.extension.canvasZoomed( point );
      return false;

   } // canvasZoomed


   /**
   * Reagiert auf einen Mausklick auf ein Szenenobjekt des Zeichenfensters.
   *
   * Falls eine Editorerweiterung existiert, wird die dortige Methode
   * {@link EditorExtension#objectClicked(SceneObject,Point) objectClicked(...)}
   * aufgerufen und ihr Rueckgabewert zurueckgeliefert.<p>
   *
   * Fuer weitere Informationen siehe
   * {@link #canvasClicked(Point,int) canvasClicked(...)}.
   *
   * @param object das angeklickte Szenenobjekt
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   * @see #canvasClicked(Point,int)
   */
   public boolean objectClicked(
      SceneObject object,
      Point point
   ) {

      if ( _reg.extension != null )
         return _reg.extension.objectClicked( object, point );
      return false;

   } // objectClicked


   /**
   * Reagiert auf einen Kontextmenue-Mausklick auf ein Szenenobjekt des
   * Zeichenfensters.
   *
   * Falls eine Editorerweiterung existiert, wird die dortige Methode
   * {@link EditorExtension#objectContext(SceneObject,Point) objectContext(...)}
   * aufgerufen und ihr Rueckgabewert zurueckgeliefert.<p>
   *
   * Fuer weitere Informationen siehe
   * {@link #canvasClicked(Point,int) canvasClicked(...)}.
   *
   * @param object das angeklickt Szenenobjekt
   * @param point die Pixelposition des Klickpunkts
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   * @see #canvasClicked(Point,int)
   */
   public boolean objectContext(
      SceneObject object,
      Point point
   ) {

      if ( _reg.extension != null )
         return _reg.extension.objectContext( object, point );
      return false;

   } // objectContext


   /**
   * Reagiert auf das Bewegen des Mauscursors ueber die Zeichenflaeche.
   *
   * Falls eine Editorerweiterung existiert, wird die dortige Methode
   * {@link EditorExtension#cursorMoved(Point) cursorMoved(...)}
   * aufgerufen und ihr Rueckgabewert zurueckgeliefert.<p>
   *
   * Fuer weitere Informationen siehe
   * {@link #canvasClicked(Point,int) canvasClicked(...)}.
   *
   * @param point die Position des Mauscursors in Weltkoordinaten
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   * @see #canvasClicked(Point,int)
   */
   public boolean cursorMoved(
      Point point
   ) {

      if ( _reg.extension != null )
         return _reg.extension.cursorMoved( point );
      return false;

   } // cursorMoved


   /**
   * Reagiert auf das Verschieben eines Szenenobjekts im Zeichenfenster.
   *
   * Falls eine Editorerweiterung existiert, wird die dortige Methode
   * {@link EditorExtension#objectDragged(SceneObject,Point,int) objectDragged(...)}
   * aufgerufen und ihr Rueckgabewert zurueckgeliefert.<p>
   *
   * Fuer weitere Informationen siehe
   * {@link #canvasClicked(Point,int) canvasClicked(...)}.
   *
   * @param object das verschobene Szenenobjekt
   * @param point die Position des Mauscursors in Weltkoordinaten
   * @param button die Maustaste, die waehrend des Verschiebens gedrueckt ist
   * @return <code>true</code>, falls das Ereignis behandelt wurde, sonst
   *         <code>false</code>
   * @see #canvasClicked(Point,int)
   */
   public boolean objectDragged(
      SceneObject object,
      Point point,
      int button
   ) {

      if ( _reg.extension != null )
         return _reg.extension.objectDragged( object, point, button );
      return false;

   } // objectDragged


   /**
   * Fuegt ein neues Szenenobjekt hinzu.
   *
   * @param object das einzufuegende <code>SceneObject</code>
   * @param source die aufrufende Instanz (normalerweise <code>this</code>)
   */
   public void addObject(
      SceneObject object,
      Object source
   ) {

      // ggf. Objekt sortiert in Liste einfuegen:
      _addListObject( object );
      _addObject( object, source );
      _reg.display.repaint();

   } // addObject


   /**
   * Ersetzt bzw. aktualisiert das bestehende Szenenobjekt und loest ein
   * entsprechendes Szenenereignis aus.
   *
   * @param object das aktualisierte <code>SceneObject</code>
   * @param source die aufrufende Instanz (normalerweise <code>this</code>)
   */
   public void replaceObject(
      SceneObject object,
      Object source
   ) {

      _scene_object_list_panel.repaint();
      _reg.display.repaint();
      _reg.scene.fireObjectUpdated( new SceneEvent( source, _reg.scene, object,
                                                      SceneEvent.FIELD_ANY ) );

   } // replaceFunction


   // *************************************************************************
   // Interface ActionListener
   // *************************************************************************

   /**
   * Reagiert auf die Auswahl eines Menuepunkts.
   *
   * @param e das <code>ActionEvent</code>-Objekt
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      String cmd = e.getActionCommand();

      if ( cmd.equals( MENU_FILE_NEW ) ) {
         _deleteAllObjects();
         _reg.display.repaint();
      } // if
      else if ( cmd.equals( MENU_FILE_OPEN ) )
         _openFile();
      else if ( cmd.equals( MENU_FILE_SAVE ) )
         _saveFile();
      else if ( cmd.equals( MENU_FILE_SAVEAS ) )
         _saveFileAs();
      else if ( cmd.equals( MENU_FILE_EXIT ) )
         _fireActionEvent( e, _file_listeners );

      else if ( cmd.equals( MENU_SCENE_PRIO ) ) {
         _togglePaintPriorityMenuItem();
         _fireActionEvent( e, _scene_listeners );

      } // if
      else if ( cmd.equals( MENU_SCENE_OBJECT_DELETE ) )
         _deleteSelectedObject();

      else if ( cmd.equals( CoordinateSystem.MENU_SHOW ) ||
                  cmd.equals( CoordinateSystem.MENU_HIDE ) ||
                  cmd.equals( CoordinateSystem.MENU_NONE ) ||
                  cmd.equals( CoordinateSystem.MENU_BROAD ) ||
                  cmd.equals( CoordinateSystem.MENU_FINE ) || 
                  cmd.equals( CoordinateSystem.MENU_RESET ) ) {
         _reg.display.repaint();
         _fireActionEvent( e, _cosystem_listeners );
      } // if

   } // actionPerformed


   // *************************************************************************
   // Interface ItemListener
   // *************************************************************************

   /**
   * Reagiert auf das Selektieren eines Szenenobjekts im
   * Szenenobjekt-Listenpanel und leitet das Selektionsereignis an die Szene
   * weiter.
   *
   * @param e das <code>ItemEvent</code>-Objekt
   */
   public void itemStateChanged(
      ItemEvent e
   ) {

      int i = _scene_object_list.getSelectedIndex();
      _scene_object_delete_button.setEnabled( i > 0 );
      if ( _enable_scene_object_list_state_changed ) {
         if ( i > 0 )
            _reg.scene.fireObjectSelected( new SceneEvent( this, _reg.scene,
                        (SceneObject) _scene_object_list.getSelectedItem() ) );
      } // if

   } // itemStateChanged


   // *************************************************************************
   // Interface PluginProvider
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus PluginProvider kopiert]
   */
   public abstract JPluginDialog createPlugin( Register register,
                                               String scene_object_class );

   /*
   * [javadoc-Beschreibung wird aus PluginProvider kopiert]
   */
   public abstract JPluginDialog createPlugin( Register register,
                                               SceneObject object );


   // *************************************************************************
   // Interface SceneListener
   // *************************************************************************

   /**
   * Fuegt ein Objekt zur Objektliste hinzu, wenn es extern hinzugefuegt wurde.
   *
   * @param e das <code>SceneEvent</code>-Objekt
   */
   public void objectAdded( SceneEvent e ) {

      if ( ( e.getSource() != this ) &&
                             ( ! ( e.getSource() instanceof JPluginDialog ) ) )
         _addListObject( e.getObject() );

   } // objectAdded


   /**
   * Entfernt ein Objekt aus der Objektliste, wenn es extern geloescht wurde.
   *
   * @param e das <code>SceneEvent</code>-Objekt
   */
   public void objectRemoved(
      SceneEvent e
   ) {

      if ( e.getSource() != this )
         _deleteListObject( e.getObject() );

   } // objectRemoved


   /**
   * Zeichnet die Objektliste neu, wenn ein Objekt selektiert wurde.
   *
   * @param e das <code>SceneEvent</code>-Objekt
   */
   public void objectSelected(
      SceneEvent e
   ) {

      _enable_scene_object_list_state_changed = false;
      _scene_object_list.setSelectedItem( e.getObject() );
      _enable_scene_object_list_state_changed = true;

   } // objectSelected


   /**
   * Veranlasst ein Neuzeichnen der Szene nach einer Zeichenprioritaetsaenderung.
   *
   * @param e das <code>SceneEvent</code>-Objekt
   */
   public void prioritiesChanged(
      SceneEvent e
   ) {

      if ( e.getSource() != this )
         _reg.display.repaint();

   } // prioritiesChanged


   // folgende Methode wird nicht verwendet:
   public void objectUpdated( SceneEvent e ) {}


   // *************************************************************************
   // Interface WindowListener
   // *************************************************************************

   /**
   * Reagiert auf das Schliessen eines Zeichenprioritaetsdialogs.
   *
   * @param e das <code>WindowEvent</code>-Objekt
   */
   public void windowClosing(
      WindowEvent e
   ) {

      _item_scene_prio.setText( ITEM_SCENE_PRIO_SHOW );

   } // windowClosing


   // folgende Methoden werden nicht verwendet:
   public void windowActivated(WindowEvent e) {}
   public void windowClosed(WindowEvent e) {}
   public void windowDeactivated(WindowEvent e) {}
   public void windowDeiconified(WindowEvent e) {}
   public void windowIconified(WindowEvent e) {}
   public void windowOpened(WindowEvent e) {}


   // ************************************************************************
   // Protected methods
   // ************************************************************************

   /**
   * Initialisiert den verwendeten Dateiauswahl-Dialog.
   */
   protected abstract void _initFileChooser();


   /**
   * Fuegt das spezifizierte Objekt unmittelbar in die Szene ein.
   *
   * @param object das einzufuegende Szenenobjekt
   * @param source die aufrufende Instanz (normalerweise <code>this</code>)
   */
   protected void _addObject(
      SceneObject object,
      Object source
   ) {

      _reg.scene.add( object, source );

   } // _addObject


   /**
   * Benachrichtigt die Listener eines Menues vom Auswaehlen eines Menuepunkts.
   *
   * @param e das <code>ActionEvent</code>-Objekt
   * @param listeners die zu benachrichtigenden Listener
   */
   protected void _fireActionEvent(
      ActionEvent e,
      Vector listeners
   ) {

      for ( int i = 0; i < listeners.size(); i++ )
         ( (ActionListener) listeners.get( i ) ).actionPerformed( e );

   } // fireActionEvent


   /**
   * Wechselt den Text des Menuepunkts <i>Scene -> Paint priority...</i>.
   */
   protected void _togglePaintPriorityMenuItem() {

      if ( _item_scene_prio.getText().equals( ITEM_SCENE_PRIO_SHOW ) )
         _item_scene_prio.setText( ITEM_SCENE_PRIO_HIDE );
      else
         _item_scene_prio.setText( ITEM_SCENE_PRIO_SHOW );

   } // _togglePaintPriorityMenuItem


   /**
   * Fuegt ein Objekt dem Szenenobjekt-Listenpanel hinzu.
   *
   * @param object das hinzuzufuegende Objekt
   */
   protected void _addListObject(
      SceneObject object
   ) {

      if ( _scene_object_list_panel != null ) {
         int i = 1;
         boolean cont = true;
         while ( cont && ( i < _scene_object_list.getItemCount() ) ) {
            String s = ( (SceneObject) _scene_object_list.getItemAt( i )
                                                                   ).getName();
            if ( object.getName().compareTo( s ) < 0 ) {
               _scene_object_list.insertItemAt( object, i ); // zwischendrin
               cont = false;
            } // if
            i++;
         } // while
         if ( cont )
            _scene_object_list.addItem( object ); // am Ende einfuegen
      } // if

   } // _addListObject


   /**
   * Loescht das im Szenenobjekt-Listenpanel selektierte Objekt aus der Szene.
   */
   protected void _deleteSelectedObject() {

      int i = _scene_object_list.getSelectedIndex();
      SceneObject object = (SceneObject) _scene_object_list.getItemAt( i );
      _scene_object_list.removeItemAt( i );
      if ( _scene_object_list.getItemCount() > i )
         _scene_object_list.setSelectedIndex( i );
      else
         _scene_object_list.setSelectedIndex( i - 1 );
      _reg.scene.remove( object, this );
      _reg.display.repaint();

   } // _deleteSelectedObject


   /**
   * Loescht ein Szenenobjekt aus der Szenenobjektliste.
   *
   * @param object das zu loeschende Objekt
   */
   protected void _deleteListObject(
      SceneObject object
   ) {

      int i = ( (DefaultComboBoxModel) _scene_object_list.getModel() ).
                                                          getIndexOf( object );
      _scene_object_list.removeItemAt( i );
      if ( _scene_object_list.getItemCount() > i )
         _scene_object_list.setSelectedIndex( i );
      else
         _scene_object_list.setSelectedIndex( i - 1 );

   } // _deleteSelectedObject


   /**
   * Loescht alle Szenenobjekte aus der Szene.
   */
   protected void _deleteAllObjects() {

      while ( _scene_object_list.getItemCount() > 1 )
         _scene_object_list.removeItemAt( 1 );
      _reg.scene.clear( this );

   } // _deleteAllObjects


   /**
   * Stellt einen Dateiauswahl-Dialog zum Oeffnen einer Szenendatei dar.
   */
   protected void _openFile() {

      int option = _file_chooser.showOpenDialog( _owner );
      // wenn OK gewaehlt wurde, Dateinamen auslesen:
      if ( option == JFileChooser.APPROVE_OPTION ) {
         // aktelle Datei sichern:
         _current_file = _file_chooser.getSelectedFile();
         _readScene();
         _current_file = null;
      } // if

   } // _openFile


   /**
   * Speichert die aktuelle Szene in die aktuelle Szenendatei. Falls es keine
   * solche gibt, wird die Methode {@link #_saveFileAs() _saveFileAs()}
   * aufgerufen.
   */
   protected void _saveFile() {

      if ( _current_file != null )
         _writeScene( null );
      else
         _saveFileAs();

   } // _saveFile


   /**
   * Stellt einen Dateiauswahl-Dialog zum Speichern einer Szenendatei dar.
   */
   protected void _saveFileAs() {

      int option = _file_chooser.showSaveDialog( _owner );
      // wenn OK gewaehlt wurde, Dateinamen auslesen:
      if ( option == JFileChooser.APPROVE_OPTION ) {
         // aktelle Datei sichern:
         String filename = _file_chooser.getSelectedFile().getPath();
         if ( ( _file_chooser.getFileFilter() == _file_filter ) &&
              ( ! filename.endsWith( _file_filter.getExtension() ) ) )
            filename += "." + _file_filter.getExtension();
         File f = new File( filename );
         if ( f.exists() ) {
            option = JOptionPane.showConfirmDialog( _owner, _MSG_EXISTS,
                                        "Warning", JOptionPane.YES_NO_OPTION );
            if ( option == JOptionPane.YES_OPTION ) {
               _current_file = f;
               _writeScene( null );
            } // if
         } else { // if
            _current_file = f;
            _writeScene( null );
         } // else
      } // if

   } // _saveFileAs


   /**
   * Liest eine Szene aus einer XML-Datei. Zurueckgeliefert wird das
   * Wurzelelement des XML-Dokuments, das bei gueltigen Dateien immer das
   * <code>&lt;scenes&gt;</code>-Element mit den Attributen fuer den Editor
   * ist.<p>
   *
   * In dieser Klasse ist diese Methode nicht vollstaendig implementiert.
   * Konkrete Editor-Implementierungen muessen <b>nach</b> dem
   * <code>super._readScene()</code>-Aufruf die Editor-Attribute auswerten
   * und die Szenenelemente an die Szenenklasse uebergeben.
   *
   * @return das Wurzelelement oder <code>null</code>, falls das Wurzelelement
   *         nicht fehlerfrei bestimmt werden konnte
   */
   protected Element _readScene() {

      SAXBuilder builder = new SAXBuilder();
      Document doc = null;
      Element root = null;
      try {
         doc = builder.build( _current_file );
         root = doc.getRootElement();
      } catch ( Exception e ) { // try
         JOptionPane.showMessageDialog( _owner, e.getMessage(), "Read error",
                                                   JOptionPane.ERROR_MESSAGE );
      } // catch
      if ( ( root == null ) || ( ! root.getName().equals( XMLFile.ROOT ) ) ) {
         JOptionPane.showMessageDialog( _owner, _MSG_INVALID, "Read error",
                                                   JOptionPane.ERROR_MESSAGE );
         return null;
      } // if
      return root;

   } // _readScene


   /**
   * Speichert die Szene im XML-Format in die gewaehlte Datei.<p>
   *
   * In dieser Klasse ist diese Methode nicht vollstaendig implementiert.
   * Konkrete Editor-Implementierungen muessen <b>vor</b> dem
   * <code>super._writeScene(...)</code>-Aufruf das <code>root</code>-Element
   * erzeugen und dessen Attribute korrekt belegen.
   *
   * @param root das Wurzelattribut fuer die XML-Datei
   */
   protected void _writeScene(
      Element root
   ) {

      root.addContent( _reg.scene.createXML() );
      Document doc = new Document( root );
      XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
      FileOutputStream file;
      try {
         file = new FileOutputStream( _current_file );
         out.output( doc, file );
         file.close();
      } catch ( IOException ioe ) {
         JOptionPane.showMessageDialog( _owner, ioe.getMessage(),
                                    "Write error", JOptionPane.ERROR_MESSAGE );
      } // try

   } // _writeScene


   /**
   * Zeigt einen Dialog mit einer Fehlermeldung an, wenn beim Einlesen einer
   * XML-Szenendatei das <code>editor</code>-Attribut nicht mit diesem Editor
   * uebereinstimmt und die Szene daher nicht von diesem Editor gelesen und
   * interpretiert werden kann.
   */
   protected void _showXMLEditorClassErrorMessage() {

      JOptionPane.showMessageDialog( _owner, _MSG_EDITOR,
                                     "Read error", JOptionPane.ERROR_MESSAGE );

   } // _showXMLEditorClassErrorMessage


   /**
   * Zeigt einen Dialog mit einer Fehlermeldung an, wenn beim Einlesen einer
   * XML-Szenendatei das <code>version</code>-Attribut nicht mit diesem Editor
   * uebereinstimmt und die Szene daher nicht von diesem Editor gelesen und
   * interpretiert werden kann.
   */
   protected void _showXMLEditorVersionErrorMessage() {

      JOptionPane.showMessageDialog( _owner, _MSG_VERSION,
                                     "Read error", JOptionPane.ERROR_MESSAGE );

   } // _showXMLEditorVersionErrorMessage


   /**
   * Zeigt einen Dialog mit einer Fehlermeldung an, wenn beim Einlesen einer
   * XML-Szenendatei keine Szenen gefunden werden koennen.
   */
   protected void _showXMLNoScenesErrorMessage() {

      JOptionPane.showMessageDialog( _owner, _MSG_NO_SCENE,
                                     "Read error", JOptionPane.ERROR_MESSAGE );

   } // _showXMLNoScenesErrorMessage


   /**
   * Zeigt einen Dialog mit einer Fehlermeldung an, wenn waehrend des Einlesens
   * einer XML-Szenendatei ein Fehler auftritt.
   *
   * @param message die Fehlermeldung einer
   *        {@link XMLParseException XMLParseException}
   */
   protected void _showXMLParseException(
      String message
   ) {

      JOptionPane.showMessageDialog( _owner, _MSG_PARSE + message,
                                    "Parse error", JOptionPane.ERROR_MESSAGE );

   } // _showXMLParseException


} // Editor
