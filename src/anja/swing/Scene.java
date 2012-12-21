package anja.swing;

import java.awt.Color;
import java.awt.Graphics;
//import java.awt.Point;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import org.jdom.Element;

import anja.swing.event.SceneEvent;
import anja.swing.event.SceneListener;

import anja.util.AlphabeticalEnumeration;


/**
* Ein Objekt der Klasse Scene kann in einem
* {@link JDisplayPanel JDisplayPanel} angezeigt werden.<br>
* Allgemein ist eine Szene eine beliebige Sammlung zu zeichnender Objekte in
* einem zweidimensionalen Koordinatensystem. Die Objekte sind dabei sortiert
* nach ihrer Zeichenprioritaet gespeichert.
*
* @version 0.8 03.09.2004
* @author Sascha Ternes
*/

public abstract class Scene {

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * Szenenereignis: ein Objekt wurde zur Szene hinzugefuegt
   */
   public static final int OBJECT_ADDED = 1;
   /**
   * Szenenereignis: ein Objekt wurde aus der Szene entfernt
   */
   public static final int OBJECT_REMOVED = 2;
   /**
   * Szenenereignis: ein Objekt der Szene wurde selektiert
   */
   public static final int OBJECT_SELECTED = 3;
   /**
   * Szenenereignis: ein Objekt der Szene wurde veraendert
   */
   public static final int OBJECT_UPDATED = 4;
   /**
   * Szenenereignis: ein Objekt der Szene hat seine Zeichenprioritaet veraendert
   */
   public static final int PRIORITIES_CHANGED = 5;


   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * das Register der Komponenten
   */
   protected Register _reg;

   /**
   * die Objekte der Szene:
   */
   protected Vector _objects;

   /**
   * die registrierten <code>SceneListener</code>
   */
   protected Vector _listeners;
  
   //protected int 	_iDisplayWidth;
   //protected int 	_iDisplayHeight;

   // *************************************************************************
   // Constructors
   // *************************************************************************

   /*
   * Verbotener Konstruktor.
   */
   private Scene() {}


   /**
   * Erzeugt eine neue noch leere Szene. Diese registriert sich im uebergebenen
   * Register-Objekt.
   *
   * @param register das Register-Objekt
   */
   public Scene(
      Register register
   ) {

      register.scene = this;
      _reg = register;
      _objects = new Vector();
      _listeners = new Vector( 1 );
      
      //_iDisplayHeight =_iDisplayWidth = 0;
            

   } // Scene


   // *************************************************************************
   // Public methods
   // *************************************************************************
   
   /**
    * Informs the scene of a change in display panel's dimensions.
    * This is called internally by the corresponding JDisplayPanel
    * instance. 
    * The internal parameters <b>_iDisplayWidth </b> and
    * <b>_iDisplayHeight </b> may be useful in subclasses of Scene for
    * drawing things (such as guidelines etc.) that depend on
    * display's dimensions.
    * 
    * @param width
    *            new display width
    * @param height
    *            new display height
    */
   /*
   public void setDisplayDimensions(int width, int height)
   {
   		_iDisplayWidth = width;
   		_iDisplayHeight = height;
   }*/
   
   /**
   * Zeichnet die gesamte Szene neu. Die implementierende Klasse kann entweder
   * mit Pixelkoordinaten in das spezifizierte <code>Graphics</code>-Objekt
   * zeichnen oder mit Weltkoordinaten in ein gecastetes
   * <code>Graphics2D</code>-Objekt, auf welches die spezifizierte
   * Transformationsmatrix anzuwenden ist. Folgendes Codefragment erlaeutert die
   * Vorgehensweise:<p>
   *<pre>   (...) // zeichnen mit g
   *
   *   // wenn mit Graphics2D gezeichnet werden soll:
   *   Graphics2D g2d = (Graphics2D) g;
   *   // die alte Transformationsmatrix sichern:
   *   AffineTransform old_transform = g2d.getTransform();
   *   g2d.setTransform( transform );
   *   (...) // zeichnen mit g2d
   *   // alte Matrix wiederherstellen:
   *   g2d.setTransform( old_transform );</pre><p>
   *
   * Das Retten der urspruenglichen Transformationsmatrix ist notwendig, weil
   * eine gesetzte Matrix auch das Zeichnen mit <code>Graphics</code>
   * beeinflusst.
   *
   * @param g das <code>Graphics</code>-Objekt, in dem mit Pixelkoordinaten
   *          gezeichnet werden kann
   * @param transform die Transformationsmatrix fuer das Zeichnen mit
   *        Weltkoordinaten
   */
   public abstract void paint( Graphics g, AffineTransform transform );


   /**
   * Markiert die angegebenen Objekte in derselben spezifizierten Farbe. Die
   * Objekte werden in der Reihenfolge neu gezeichnet, wie sie im uebergebenen
   * <code>Vector</code> gespeichert sind. Bei naechsten Zeichnen der Szene
   * werden die Objekte wieder in ihrer eigenen individuellen Farbe
   * gezeichnet.<br>
   * Fuer weitere Informationen Vorgehensweise siehe
   * {@link #paint(Graphics,AffineTransform) paint(...)}.
   *
   * @param g das <code>Graphics</code>-Objekt, in dem mit Pixelkoordinaten
   *          gezeichnet werden kann
   * @param transform die Transformationsmatrix fuer das Zeichnen mit
   *        Weltkoordinaten
   * @param objects die zu markierenden Objekte
   * @param color die Farbe, in der markiert wird
   */
   public abstract void mark( Graphics g, AffineTransform transform,
                                                 Vector objects, Color color );


   /**
   * Macht einen vorher durchgefuehrten
   * {@link #mark(Graphics,AffineTransform,Vector,Color) mark(...)}-Aufruf
   * rueckgaengig, indem die spezifizierten Objekte in ihrer eigenen
   * individuellen Farbe neu gezeichnet werden.<br>
   * Fuer weitere Informationen Vorgehensweise siehe
   * {@link #paint(Graphics,AffineTransform) paint(...)}.
   *
   * @param g das <code>Graphics</code>-Objekt, in dem mit Pixelkoordinaten
   *          gezeichnet werden kann
   * @param transform die Transformationsmatrix fuer das Zeichnen mit
   *        Weltkoordinaten
   * @param objects die Objekte, deren Markierung zurueckgenommen werden soll
   */
   public abstract void unmark( Graphics g, AffineTransform transform,
                                                              Vector objects );


   /**
   * Erzeugt ein XML-Element, das diese Szene repraesentiert. Dieses Element
   * wird von der Editorklasse verwendet, um die Szene in eine Datei zu
   * speichern.
   *
   * @return das XML-Element dieser Szene
   */
   public abstract Element createXML();


   /**
   * Liest eine komplette Szene aus einer XML-Datei und uebernimmt die gelesenen
   * Szenenobjekte. Dabei sollen zunaechst alle bisher verwalteten Szenenobjekte
   * durch Aufruf der Methode {@link #clear(Object) clear(...)} geloescht und
   * dann die gelesenen Objekte einzeln zur Szene hinzugefuegt werden, wobei
   * jeweils ein <i>objectAdded</i>-Ereignis ausgeloest wird.<p>
   *
   * Wenn beim Einlesen ein Fehler auftritt, soll eine
   * {@link XMLParseException XMLParseException} ausgeworfen werden.
   *
   * @param scene das XML-Element, das eine Graphszene repraesentiert
   * @exception XMLParseException falls die Szene nicht fehlerfrei gelesen
   *            werden kann
   */
   public abstract void loadXML( Element scene ) throws XMLParseException;


   /**
   * Fuegt der Szene das angegebene Objekt hinzu und benachrichtigt alle
   * registrierten Listener. Das Objekt erhaelt die unterste Zeichenprioritaet.
   *
   * @param object das hinzuzufuegende Objekt
   * @param source die aufrufende Instanz (normalerweise <code>this</code>)
   */
   public void add(
      SceneObject object,
      Object source
   ) {

      _objects.add( object );
      object.setPriority( _objects.size() );
      SceneEvent e = new SceneEvent( source, this, object );
      fireObjectAdded( e );
      firePrioritiesChanged( e );

   } // add


   /**
   * Fuegt das angegebene Objekt mit hoechster Zeichenprioritaet der Szene hinzu
   * und benachrichtigt alle registrierten Listener.
   *
   * @param object das hinzuzufuegende Objekt
   * @param source die aufrufende Instanz (normalerweise <code>this</code>)
   */
   public void insert(
      SceneObject object,
      Object source
   ) {

      _objects.add( 0, object );
      _updatePriorities( 0 );
      SceneEvent e = new SceneEvent( source, this, object );
      fireObjectAdded( e );
      firePrioritiesChanged( e );

   } // insert


   /**
   * Entfernt das angegebene Objekt aus der Szene und benachrichtigt alle
   * registrierten Listener. Die Zeichenprioritaeten werden dabei angepasst.
   *
   * @param object das zu entfernende Objekt
   * @param source die aufrufende Instanz (normalerweise <code>this</code>)
   */
   public void remove(
      SceneObject object,
      Object source
   ) {

      if ( _objects.remove( object ) ) {
         _updatePriorities( 0 );
         SceneEvent e = new SceneEvent( source, this, object );
         fireObjectRemoved( e );
         firePrioritiesChanged( e );
      } // if

   } // remove


   /**
   * Entfernt saemtliche Objekte aus der Szene. Fuer jedes entfernte Objekt wird
   * ein entsprechendes Szenenereignis erzeugt; es wird jedoch kein
   * <i>prioritiesChanged</i>-Ereignis erzeugt.
   *
   * @param source die aufrufende Instanz (normalerweise <code>this</code>)
   */
   public void clear(
      Object source
   ) {

      while ( ! _objects.isEmpty() ) {
         SceneEvent e = new SceneEvent( source, this,
                                          (SceneObject) _objects.remove( 0 ) );
         fireObjectRemoved( e );
      } // while

   } // clear


   /**
   * Liefert eine Aufzaehlung aller Szenenobjekte gemaess ihrer Zeichenprioritaet.
   *
   * @return eine geordnete Aufzaehlung der Objekte
   */
   public Enumeration elements() {

      return elementsByPriority();

   } // elements


   /**
   * Liefert eine alphabetisch sortierte Aufzaehlung aller Szenenobjekte gemaess
   * ihrer Bezeichnung.
   *
   * @return eine geordnete Aufzaehlung der Objekte
   */
   public Enumeration elementsByName() {

      return new AlphabeticalEnumeration( _objects.toArray() );

   } // elementsByName


   /**
   * Liefert eine Aufzaehlung aller Szenenobjekte gemaess ihrer Zeichenprioritaet.
   *
   * @return eine geordnete Aufzaehlung der Objekte
   */
   public Enumeration elementsByPriority() {

      return _objects.elements();

   } // elementsByPriority

   
   public Vector objects()
   {
   	 return _objects;
   }

   /**
   * Liefert das zum spezifizierten Punkt naechstgelegene Objekt. Wenn diese
   * Szene leer ist, wird <code>null</code> zurueckgeliefert.<p>
   *
   * Diese Methode ist standardmaessig so implementiert, dass <b>immer</b>
   * <code>null</code> zurueckgeliefert wird. Daher muss diese Methode in
   * Subklassen normalerweise ueberschrieben werden.
   *
   * @param point der Punkt in Weltkoordinaten
   * @return das naechstgelegene Objekt oder <code>null</code>, falls diese
   *         Szene keine Objekte enthaelt
   */
   public SceneObject closestObject(
      Point2D.Double point
   ) {

      return null;

   } // closestObject


   /**
   * Versucht das zum spezifizierten Punkt naechstgelegene Objekt zu
   * selektieren. Wenn der spezifizierte Punkt ausserhalb aller Objekte liegt,
   * wird <code>null</code> zurueckgeliefert.<p>
   *
   * Diese Methode ist standardmaessig so implementiert, dass <b>immer</b>
   * <code>null</code> zurueckgeliefert wird. Daher muss diese Methode in
   * Subklassen normalerweise ueberschrieben werden.
   *
   * @param point der Selektionspunkt in Weltkoordinaten
   * @param source die aufrufende Instanz (normalerweise <code>this</code>)
   * @return das selektierte Objekt oder <code>null</code>
   */
   public SceneObject selectObject(
      Point2D.Double point,
      Object source
   ) {

      return null;

   } // selectObject


   /**
   * Sortiert die Szenenobjekte neu. Diese Methode sollte immer nach Aenderungen
   * der Zeichenprioritaet der Szenenobjekte aufgerufen werden.
   */
   public void sort() {

      Collections.sort( _objects );

   } // sort


   /**
   * Registriert einen Listener fuer Aenderungen in der Szene.
   *
   * @param listener der Listener, der benachrichtigt werden soll, wenn sich
   *        die Szene aendert
   */
   public void addSceneListener(
      SceneListener listener
   ) {

      if ( listener != null ) _listeners.add( listener );

   } // addSceneListener


   /**
   * Entfernt einen Listener fuer Aenderungen in der Szene.
   *
   * @param listener der zu entfernende Listener
   */
   public void removeSceneListener(
      SceneListener listener
   ) {

      if ( listener != null ) _listeners.remove( listener );

   } // removeSceneListener


   /**
   * Benachrichtigt die registrierten Listener ueber ein eingetretenes
   * Szenenereignis.
   *
   * @param action gibt die Art des Ereignisses an als eine der Konstanten
   *        <code>OBJECT_ADDED</code>, <code>OBJECT_REMOVED</code>,
   *        <code>OBJECT_SELECTED</code>, <code>OBJECT_UPDATED</code>,
   *        <code>PRIORITIES_CHANGED</code>
   * @param e das Szenenereignis-Objekt
   */
   public void fireSceneEvent(
      int action,
      SceneEvent e
   ) {

      switch ( action ) {
      case OBJECT_ADDED:
         fireObjectAdded( e );
         break;
      case OBJECT_REMOVED:
         fireObjectRemoved( e );
         break;
      case OBJECT_SELECTED:
         fireObjectSelected( e );
         break;
      case OBJECT_UPDATED:
         fireObjectUpdated( e );
         break;
      case PRIORITIES_CHANGED:
         firePrioritiesChanged( e );
      } // switch

   } // fireSceneEvent


   /**
   * Benachrichtigt die registrierten Listener ueber ein hinzugefuegtes Objekt.
   *
   * @param e das Szenenereignis-Objekt
   */
   public void fireObjectAdded(
      SceneEvent e
   ) {

      for ( int i = 0; i < _listeners.size(); i++ )
         ( (SceneListener) _listeners.get( i ) ).objectAdded( e );

   } // fireObjectAdded


   /**
   * Benachrichtigt die registrierten Listener ueber ein entferntes Objekt.
   *
   * @param e das Szenenereignis-Objekt
   */
   public void fireObjectRemoved(
      SceneEvent e
   ) {

      for ( int i = 0; i < _listeners.size(); i++ )
         ( (SceneListener) _listeners.get( i ) ).objectRemoved( e );

   } // fireObjectRemoved


   /**
   * Benachrichtigt die registrierten Listener ueber ein selektiertes Objekt.
   *
   * @param e das Szenenereignis-Objekt
   */
   public void fireObjectSelected(
      SceneEvent e
   ) {

      for ( int i = 0; i < _listeners.size(); i++ )
         ( (SceneListener) _listeners.get( i ) ).objectSelected( e );

   } // fireObjectSelected


   /**
   * Benachrichtigt die registrierten Listener ueber ein geaendertes Objekt.
   *
   * @param e das Szenenereignis-Objekt
   */
   public void fireObjectUpdated(
      SceneEvent e
   ) {

      for ( int i = 0; i < _listeners.size(); i++ )
         ( (SceneListener) _listeners.get( i ) ).objectUpdated( e );

   } // fireObjectUpdated


   /**
   * Benachrichtigt die registrierten Listener ueber geaenderterte
   * Zeichenprioritaeten.
   *
   * @param e das Szenenereignis-Objekt
   */
   public void firePrioritiesChanged(
      SceneEvent e
   ) {

      for ( int i = 0; i < _listeners.size(); i++ )
         ( (SceneListener) _listeners.get( i ) ).prioritiesChanged( e );

   } // firePrioritiesChanged


   // *************************************************************************
   // Protected methods
   // *************************************************************************

   /**
   * Ordnet den Szenenobjekten entsprechend ihrer Reihenfolge im
   * <code>Vector _objects</code> die richtigen Zeichenprioritaeten zu.
   *
   * @param first Index des ersten zu behandelnden Szeneobjekts
   */
   protected void _updatePriorities(
      int first
   ) {

      int i = first;
      while ( i < _objects.size() )
         ( (SceneObject) _objects.get( i++ ) ).setPriority( i );

   } // _updatePriorities


} // Scene
