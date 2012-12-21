package anja.swing.point;

import java.awt.Cursor;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.awt.geom.Point2D;

import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.*;

import org.jdom.Attribute;
import org.jdom.Element;

import anja.geom.Point2;

import anja.swing.Editor;
import anja.swing.JPluginDialog;
import anja.swing.Register;
import anja.swing.SceneObject;
import anja.swing.SimpleFileFilter;
import anja.swing.XMLFile;
import anja.swing.XMLParseException;

import anja.swing.event.SceneEvent;
import anja.swing.event.SceneListener;

import anja.util.InfiniteStringEnumeration;


/**
* Editor zur Erzeugung und Darstellung von Punktmengen.
*
* @version 0.1 26.08.2004
* @author Sascha Ternes
*/

public class PointEditor extends Editor
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * 
   * Constants for menu names, menu items and associated action
   * commands.
   * Covention : MENU_XXX = menu name
   * 			 ITEM_XXX_YYY = item name
   * 			 MENU_XXX_YYY = associated action command 
   */
   public static final String MENU_POINT = "Point";
   
   public static final String ITEM_POINT_CLEAR = "Clear all points";
   public static final String MENU_POINT_CLEAR = "clear_points";
   
   public static final String ITEM_VIEW_XHAIR = "Enable guidelines";

   // *************************************************************************
   // Private constants
   // *************************************************************************

   // die XML-relevante Versionsnummer dieser Klasse:
   private static final String _VERSION = "0.1";

   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * fortlaufende Aufzaehlung fuer Bezeichner von Kantenpunkten
   */
   protected Enumeration _site_names;

   // *************************************************************************
   // Private variables
   // *************************************************************************
   
   protected PointScene _rPointScene; // reference to the point scene
   
   private JMenu	_uiPointMenu; // "point" menu
   private JMenu	_uiViewMenu;  // "view" menu

   // *************************************************************************
   // Class methods
   // *************************************************************************

   /**
   * Liefert die Versionsnummer dieses Editors.
   *
   * @return die Versionsnummer
   */
   public static String getEditorVersion() {

      return _VERSION;

   } // getEditorVersion

   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt einen neuen Punktmengeneditor. Dieser registriert sich im
   * uebergebenen Register-Objekt.
   *
   * @param register das Register-Objekt
   */
   public PointEditor(Register register)
   {
      super( register );
      _site_names = new InfiniteStringEnumeration( "site" );
      
      //setupKeyBindings(); // temporarily disabled
      
      _rPointScene = (PointScene)_reg.scene;
      
      // override the default cursor in JDisplayScene;
      
      /*
      _reg.display.setDefaultUserCursor(Cursor.
      		getPredefinedCursor(Cursor.DEFAULT_CURSOR)); 
      
      _rPointScene.enableCrosshairs(false); */
   } // PointEditor

   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert das <i>Point</i>-Menue, das im Menue des uebergeordneten Applets
   * oder der uebergeordneten Anwendung verwendet werden kann.<br>
   * Wenn das Applet oder die Anwendung keine eigenen Aktionen definiert, kann
   * als Listener <code>null</code> uebergeben werden.<p>
   *
   * <i>Hinweis: das </i>Point</i>-Menue ist lediglich ein umbenanntes
   * </i>Scene</i>-Menue; diese Methode ruft lediglich die Methode
   * <code>createSceneMenu(...)</code> auf.</i>
   *
   * @param listener der Listener fuer die Menuepunkte des Menues, der bei Auswahl
   *         eines Menuepunkts benachrichtigt wird, oder <code>null</code>
   * @return das Menue, das die Menuepunkte des <i>Point</i>-Menues enthaelt
   */
   public JMenu createPointMenu(ActionListener listener)
   {
   		if(_uiPointMenu == null)
   		{
   			_uiPointMenu = new JMenu(MENU_POINT);
   			
   			// clear all points item
   			JMenuItem clear_item = new JMenuItem(ITEM_POINT_CLEAR);
            clear_item.setActionCommand(MENU_POINT_CLEAR);
            clear_item.addActionListener(this);
             
            _uiPointMenu.add(clear_item);
   		
   		}
   		return _uiPointMenu;
   
   } // createPointMenu

   public JMenu createViewMenu(ActionListener listener)
   {
   		if(_uiViewMenu == null)
   		{
   			_uiViewMenu = new JMenu("View");
   	   		
   			// coordinate system menu
   	   		_uiViewMenu.add(super.createCoordinateSystemMenu(null));
   	   		_uiViewMenu.add(new JSeparator());
   	   		
   	   		// guidelines toggle box
   	   		JCheckBox cross_box = new JCheckBox(ITEM_VIEW_XHAIR);
   	   		
   	   		cross_box.addChangeListener(new ChangeListener()
   	   				{
						public void stateChanged(ChangeEvent e)
						{
							JCheckBox box = (JCheckBox)e.getSource();
							_rPointScene.enableCrosshairs(box.isSelected());
							
							//_uiViewMenu.setVisible(false);
						}
  	   				});
   	   		  	   		
   	   		_uiViewMenu.add(cross_box);
   		
   		}
   		return _uiViewMenu;
   }
 
   public JMenu createSceneMenu(ActionListener listener)
   {
      if ( _scene_menu == null )
      {
         super.createSceneMenu( listener );
              
      } // if

      return _scene_menu;

   } // createSceneMenu

   
   public JMenuBar createMenus(ActionListener listener)
   {
   		JMenuBar menuBar = new JMenuBar();
   		
   		menuBar.add(super.createFileMenu(this));
   		menuBar.add(createViewMenu(this));
   		menuBar.add(createPointMenu(this));
   		
   		return menuBar;
   }

   /**
   * Liefert das Punktlistenpanel, das im uebergeordneten Applet oder in
   * der uebergeordneten Anwendung verwendet werden kann.<br>
   *
   * <i>Hinweis: dieses Panel ist lediglich ein umbenanntes
   * Szenenobjekt-Listenpanel; diese Methode ruft lediglich die Methode
   * <code>createSceneObjectListPanel(...)</code> auf.</i>
   *
   * @return das Panel
   */
   public JPanel createPointListPanel() 
   {
      return createSceneObjectListPanel();
   } // createPointListPanel

  
   public void actionPerformed(ActionEvent e) 
   {
   	  String cmd = e.getActionCommand();
   	
   	  // clear all points in the scene
      if(cmd == MENU_POINT_CLEAR)
      {
        _reg.scene.clear(_reg.scene); // the paremeter _reg.scene is IMPORTANT!
        _reg.display.repaint();
      }

      // call superclass implementation
      super.actionPerformed( e );
    
   } // actionPerformed

   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public boolean canvasScrolled(Point point)
   {
   		_rPointScene.setCursorPosition(point);
   		
      if ( super.canvasScrolled( point ) ) return true;
      return false;

   } // canvasScrolled


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public boolean canvasZoomed(Point point)
   {

      if ( super.canvasZoomed( point ) ) return true;
      return false;

   } // canvasZoomed


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public boolean canvasClicked(Point point, int button) 
   {

      if ( super.canvasClicked( point, button ) ) return true;
      // neuen Punkt einfuegen:
      if ( button == MouseEvent.BUTTON1 ) {
         PointObject vo = _createNewPoint( point );
         addObject( vo, this );
         _reg.display.repaint();
         return true;
      } // if

      return false;

   } // canvasClicked


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public boolean objectClicked(SceneObject object, Point point) 
   {

      if ( super.objectClicked( object, point ) ) return true;
      return false;

   } // objectClicked


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public boolean objectContext(SceneObject object, Point point)
   {

      if ( super.objectContext( object, point ) ) return true;
      return true;

   } // objectContext


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public boolean cursorMoved(Point point)
   { 	   
   		//  pass current cursor position to scene and redraw it 
	   _rPointScene.setCursorPosition(point);
	   _reg.display.repaint(); 
      
	  if ( super.cursorMoved( point ) ) return true;
      return false;

   } // cursorMoved


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   public boolean objectDragged(SceneObject object,
   								Point point,
								int button)
   {
   	
   		_rPointScene.setCursorPosition(point);

      if ( super.objectDragged( object, point, button ) ) return true;
      
      // einen Punkt verschieben:
      //Point2D.Double p = _reg.cosystem.transform( point );
      Point2D.Double p = new Point2D.Double();
      _reg.cosystem.transformAndSnapToGrid(point, p);
          
      
      ( (PointObject) object ).setPosition( p );
      replaceObject( object, this );
      return true;

   } // objectDragged


   // *************************************************************************
   // Protected methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   protected void _initFileChooser() 
   {

      _file_chooser = new JFileChooser();
      _file_filter = new SimpleFileFilter( "XML point scenes (*.pts)", "pts" );
      _file_chooser.setFileFilter( _file_filter );

   } // _initFileChooser


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   protected Element _readScene() {

      Element root = super._readScene();
      if ( root == null ) return null;
      // Editor pruefen:
      String value = root.getAttributeValue( XMLFile.ROOT_EDITOR );
      if ( ( value == null ) || ( ! value.equals( this.getClass().getName() )))
         _showXMLEditorClassErrorMessage();
      else {
         // Version pruefen:
         value = root.getAttributeValue( XMLFile.ROOT_VERSION );
         if ( ( value == null ) || ( ! value.equals( _VERSION ) ) )
            _showXMLEditorVersionErrorMessage();
         // Graphszene aus XML extrahieren und uebernehmen:
         else {
            // kein <graph>-Element:
            Element element = root.getChild( XMLFile.GRAPH );
            if ( element == null )
               _showXMLNoScenesErrorMessage();
            // Erfolg:
            else
               try {
                  _reg.scene.loadXML( element );
               } catch ( XMLParseException xmlpe ) { // try
                  _showXMLParseException( xmlpe.getMessage() );
               } // catch
         } // else
      } // else

      return root;

   } // _readScene


   /*
   * [javadoc-Beschreibung wird aus Editor kopiert]
   */
   protected void _writeScene(
      Element root_dummy
   ) {

      Element root = new Element( XMLFile.ROOT );
      Attribute att = new Attribute( XMLFile.ROOT_EDITOR, this.getClass().getName() );
      root.setAttribute( att );
      att = new Attribute ( XMLFile.ROOT_VERSION, _VERSION );
      root.setAttribute( att );
      super._writeScene( root );

   } // writeScene


   // *************************************************************************
   // Private methods
   // *************************************************************************

   /*
   * Erzeugt einen neuen Knoten.
   */
   private PointObject _createNewPoint(
      Point point
   ) {

   	  Point2 p = new Point2();
   	  _reg.cosystem.transformAndSnapToGrid(point, p);
   	  
   	  /*
      Point2 p = new Point2( _reg.cosystem.transformX( point.x ),
                             _reg.cosystem.transformY( point.y ) );*/
   	  
      PointObject object = new PointObject( p );
      return object;

   } // _createNewPoint

   /** 
    * 	Sets up key bindings for the scene:
    * 
    * 	DELETE key -> delete selected point
    * 
    */

   private void setupKeyBindings()
   {
   	   	
   	 _reg.display.getInputMap().put
	 (KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0),
   	 						 "del_key_pressed");
   	 
   	 Action delKeyAction = new AbstractAction()
	 {
   	 	public void actionPerformed(ActionEvent e)
   	 	{
   	 		PointScene scene = (PointScene)_reg.scene;
   	 		if(scene.getSelectedPoint() != null)
   	 		{
   	 			PointObject point = scene.getSelectedPoint();
   	 			
   	 			// temporary debugging code
   	 			System.out.println("Point at "+
   	 								point.getPoint().toString()+" deleted");
   	 			
   	 			scene.remove(point, this);
   	 			
   	 		}
    	 }
   	    };
   	 
   	 _reg.display.getActionMap().put("del_key_pressed",
   	 								 delKeyAction);
   	 
   	_reg.display.getInputMap().put
	 (KeyStroke.getKeyStroke(KeyEvent.VK_F10,0),
  	 						 "f10_key_pressed");
  	 
  	 Action f10KeyAction = new AbstractAction()
	 {
  	 	public void actionPerformed(ActionEvent e)
  	 	{
  	 		PointScene scene = (PointScene)_reg.scene;
  	 		_reg.scene.clear(_reg.scene);
  	 		System.out.println("Scene reset!");
  	 	}
  	 };
  	 
  	 _reg.display.getActionMap().put("f10_key_pressed",
  	 								 f10KeyAction);
   	 
   	 
   }

/* (non-Javadoc)
 * @see anja.swing.Editor#createPlugin(anja.swing.Register, java.lang.String)
 */
public JPluginDialog createPlugin(Register register, String scene_object_class)
{
	// TODO Auto-generated method stub
	return null;
}

/* (non-Javadoc)
 * @see anja.swing.Editor#createPlugin(anja.swing.Register, anja.swing.SceneObject)
 */
public JPluginDialog createPlugin(Register register, SceneObject object)
{
	// TODO Auto-generated method stub
	return null;
}
   
} // PointEditor
