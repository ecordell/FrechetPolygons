package anja.swing;

import java.awt.Color;

import org.jdom.Attribute;
import org.jdom.Element;


/**
* Diese abstrakte Klasse definiert die Objekte, die in einer
* {@link Scene Szene} dargestellt werden.
*
* @version 0.9 26.08.2004
* @author Sascha Ternes
*/

public abstract class SceneObject
implements Comparable
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * die Zeichenfarbe fuer ungefaerbte Objekte
   */
   public static final Color DEFAULT_COLOR = Color.BLACK;


   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
    *  selection flag
    */
   protected boolean _selected;
   
   /**
   * beliebige Bezeichnung des Szenenobjekts
   */
   protected String _name;

   /**
   * Zeichenfarbe im Anzeigepanel
   */
   protected Color _color;

   /**
   * Zeichenprioritaet in der Szene
   */
   protected int _priority;

   /**
   * das zugehoerige Plugin
   */
   protected JPluginDialog _plugin;


   // *************************************************************************
   // Class variables
   // *************************************************************************

   /**
   * das naechste Suffix fuer eine Standardbezeichnung
   */
   protected static int _next_suffix = 1;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues ungefaerbtes Szenenobjekt mit einer
   * Standard-Bezeichnung.
   */
   public SceneObject() {

      this( "object" + _next_suffix++, null );

   } // SceneObject


   /**
   * Erzeugt ein neues ungefaerbtes Szenenobjekt mit der angegebenen
   * Bezeichnung.
   *
   * @param name die Bezeichnung
   */
   public SceneObject(
      String name
   ) {

      this( name, null );

   } // SceneObject


   /**
   * Erzeugt ein neues Szenenobjekt mit angegebener Bezeichnung und
   * Zeichenfarbe.
   *
   * @param name die Bezeichnung
   * @param color die Zeichenfarbe
   */
   public SceneObject(
      String name,
      Color color
   ) {

      _name = name;
      _color = color;
      
      _selected = false;

   } // SceneObject


   /**
   * Erzeugt ein neues Szenenobjekt aus einer XML-Repraesentation.<p>
   *
   * Falls beim Einlesen der XML-Daten ein Fehler auftritt, wird eine
   * {@link XMLParseException XMLParseException} ausgeworfen.
   *
   * @param xml das XML-Element, das das zu erzeugende Szenenobjekt
   *        repraesentiert
   * @exception XMLParseException falls das Szenenobjekt nicht fehlerfrei aus
   *            der XML-Repraesentation gelesen werden kann.
   */
   public SceneObject(
      Element xml
   ) throws XMLParseException {

      Attribute att = xml.getAttribute( XMLFile.NAME );
      if ( att == null )
         throw new XMLParseException( xml, XMLFile.NAME, false );
      _name = att.getValue();
      att = xml.getAttribute( XMLFile.COLOR );
      if ( att == null )
         throw new XMLParseException( xml, XMLFile.COLOR, false );
      String s = att.getValue();
      if ( s != "" )
         try {
            _color = new Color( Integer.parseInt( s, 16 ) );
         } catch ( NumberFormatException nfe ) { // try
            throw new XMLParseException( xml, att );
         } // catch

   } // SceneObject


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Testet dieses Szenenobjekt mit dem spezifizierten auf Gleichheit.
   *
   * @param scene_object das zu vergleichende Szenenobjekt
   */
   public abstract boolean equals( Object scene_object );


   /**
   * Erzeugt ein XML-Element, das dieses Szenenobjekt repraesentiert. Dieses
   * Element wird von der Szenenklasse verwendet, um aus der Szene eine
   * XML-Repraesentation zu erzeugen, die von der Editorklasse in eine Datei
   * gespeichert wird.
   *
   * @return das XML-Element dieses Szenenobjekts
   */
   public abstract Element createXML();


   /**
   * Liefert die Bezeichnung dieses Objekts.
   *
   * @return die Bezeichnung
   */
   public String getName() {

      return _name;

   } // getName


   /**
   * Liefert die Zeichenfarbe dieses Objekts oder <code>null</code>, wenn
   * das Objekt ungefaerbt ist.
   *
   * @return die Zeichenfarbe
   */
   public Color getColor() {

      return _color;

   } // getColor


   /**
   * Liefert die Zeichenprioritaet dieses Objekts.
   *
   * @return die Zeichenprioritaet
   */
   public int getPriority() {

      return _priority;

   } // getPriority


   /**
   * Liefert das Plugin dieses Objekts, das fuer die Objektdialoge zustaendig
   * ist.
   *
   * @return das Plugin
   */
   public JPluginDialog getPlugin() {

      return _plugin;

   } // getPlugin

   
   /**
    * Returns the selection state of this object
    *     
    * @return <code>true</code>, if selected, otherwise <code>false</code>
    */
   
   public boolean isSelected()
   { return _selected; }

   /**
   * Aendert die Bezeichnung dieses Objekts.
   *
   * @param name die Bezeichnung
   */
   public void setName(
      String name
   ) {

      if ( name != null ) _name = name;

   } // setName


   /**
   * Aendert die Zeichenfarbe dieses Objekts.
   *
   * @param color die Zeichenfarbe; bei <code>null</code> ist das Objekt ab
   *        sofort ungefaerbt
   */
   public void setColor(
      Color color
   ) {

         _color = color;

   } // setColor


   /**
   * Aendert die Zeichenprioritaet dieses Objekts.
   *
   * @param priority die Zeichenprioritaet
   */
   public void setPriority(
      int priority
   ) {

      if ( priority > 0 ) _priority = priority;

   } // setPriority


   /**
   * Setzt das Plugin, das fuer die Objektdialoge dieses Objekts zustaendig ist.
   *
   * @param plugin das Plugin
   */
   public void setPlugin(
      JPluginDialog plugin
   ) {

      _plugin = plugin;

   } // setPlugin

   /**
    * Sets the selection state for this object
    *      
    * @param state New selection state, <code>true</code> for selected,
    * <code>false</code> for not selected
    */

   public void setSelected(boolean state)
   { _selected = state; }
   
   /**
   * Liefert eine textuelle Repraesentation.
   *
   * @return die Bezeichnung dieses Objekts
   */
   public String toString() {

      return _name;

   } // toString


   // *************************************************************************
   // Interface Comparable
   // *************************************************************************

   /**
   * Vergleicht dieses Szenenobjekt mit dem spezifizierten anhand der
   * Zeichenprioritaet und liefert das Funktionsergebnis (-1, 0 oder 1).
   *
   * @param o das Vergleichsobjekt vom Typ <code>SceneObject</code>
   * @return <ul><li><code>-1</code>, wenn <code>this.getPriority()</code>
   *         < <code>o.getPriority()</code> ist</li>
   *         <li><code>0</code>, wenn <code>this.getPriority()</code>
   *         = <code>o.getPriority()</code> ist</li>
   *         <li><code>1</code>, wenn <code>this.getPriority()</code>
   *         > <code>o.getPriority()</code> ist</li></ul>
   */
   public int compareTo(
      Object o
   ) {

      SceneObject object = (SceneObject) o;
      if ( this._priority < object._priority ) return -1;
      if ( this._priority > object._priority ) return 1;
      return 0;

   } // compareTo


} // SceneObject
