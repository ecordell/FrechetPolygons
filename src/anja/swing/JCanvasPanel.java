package anja.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;


/**
* Ein <code>JCanvasPanel</code> ist eine einfache und komfortable Moeglichkeit
* fuer eine Zeichenflaeche. Neben dem normalen Pixel-Koordinatensystem kann
* zusaetzlich ein Weltkoordinatensystem wie
* {@link CoordinateSystem CoordinateSystem} parallel verwendet werden. Optional
* laesst sich dann die Zeichenflaeche auch zoomen und scrollen.<p>
*
* Um ein <code>JCanvasPanel</code> zu verwenden, muss diese Klasse abgeleitet
* werden. Dabei genuegt als Minimalaufwand die Implementierung der abstrakten
* Methode {@link #draw(Graphics,AffineTransform) draw(...)}, in der die
* Zeichenbefehle eingebaut werden.<p>
*
* Beim Zeichnen mit Weltkoordinaten wird automatisch ein eigenes
* Koordinatensystem verwendet, dessen Ursprung im Zentrum der Zeichenflaeche
* liegt, mit einer Aufloesung von einem Pixel pro Einheit. Ueber die Methode
* {@link #setCoordinateSystem(CoordinateSystem) setCoordinateSystem(...)} kann
* dieses Koordinatensystem ersetzt werden. Eine interessante Anwendung besteht
* darin, fuer zwei verschiedene Zeichenflaechen das selbe Koordinatensystem zu
* verwenden: Zum Beispiel koennte eine Anwendung aus einem Punkteditor und einer
* zusaetzlichen Zeichenflaeche bestehen, in der aus der Punktmenge des Editors
* komplexere Grafikobjekte aufgebaut werden. Beim Zoomen im Editor wird dann
* automatisch das Grafikobjekt mitgezoomt.<p>
*
* Ein <code>JCanvasPanel</code> verfuegt schliesslich noch ueber eine eigene
* Scroll- und Zoomfunktion, die bei Bedarf aktiviert werden kann.
*
* @version 0.9 27.08.2004
* @author Sascha Ternes
*/

public abstract class JCanvasPanel
extends JPanel
implements ComponentListener,
           MouseListener,
           MouseMotionListener
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * die Hintergrundfarbe der Zeichenflaeche
   */
   public static final Color BACKGROUND_COLOR = Color.WHITE;


   // *************************************************************************
   // Class variables
   // *************************************************************************

   // Cursor fuer verschiedene Aktionen:
   private static final Cursor _cs_default;
   private static final Cursor _cs_hand;
   private static final Cursor _cs_move;
   private static final Cursor _cs_zoom;


   // *************************************************************************
   // Private variables
   // *************************************************************************

   /**
   * das Register der Komponenten, enthaelt das verwendete Koordinatensystem
   */
   protected Register _reg;

   // Flag fuer Zentrierung des Koordinatensystems:
   private boolean _init_cosystem;

   // gepufferte Zeichenflaeche:
   private JBufferedCanvas _canvas;
   // Flag fuer ihre Initialisierung:
   private boolean _first_canvas_resize;
   // alte Zeichenflaechengroesse fuer Ursprungsberechnung:
   private int _old_width;
   private int _old_height;

   // aktuelle Transformationsmatrix:
   private AffineTransform _transform;

   // Flag fuer Zulassen von Mausaktionen:
   private boolean _mouse_enable;

   // Bezugspunkt fuer Koordinatensytem-Verschiebung:
   private Point _pressed;
   // angeklicktes Szenenobjekt:
   private int _button;
   // Zentrum beim Zoomen:
   private Point _zoom_pixel;
   private Point2D.Double _zoom_point;


   // *************************************************************************
   // Class constructor
   // *************************************************************************

   static {

      _cs_default = Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR );
      _cs_hand = Cursor.getPredefinedCursor( Cursor.HAND_CURSOR );
      _cs_move = Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR );
      _cs_zoom = Cursor.getPredefinedCursor( Cursor.N_RESIZE_CURSOR );

   } // static


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt eine neue Zeichenflaeche, die nicht scroll- und zoombar ist.
   */
   public JCanvasPanel() {

      this( new Register() );
      new CoordinateSystem( _reg );
      _init_cosystem = false;

   } // JCanvasPanel


   /**
   * Erzeugt eine neue Zeichenflaeche mit dem spezifizierten Koordinatensystem,
   * die nicht scroll- und zoombar ist.
   *
   * @param cosystem das zu verwendende Koordinatensystem
   */
   public JCanvasPanel(
      CoordinateSystem cosystem
   ) {

      this( new Register() );
      _reg.cosystem = cosystem;

   } // JCanvasPanel


   /**
   * Erzeugt eine neue Zeichenflaeche, die nicht scroll- und zoombar ist. Aus
   * dem uebergebenen Register-Objekt wird das Koordinatensystem bezogen.
   *
   * @param register das Register-Objekt
   */
   public JCanvasPanel(
      Register register
   ) {

      super();
      _reg = register;
      _init_cosystem = true;
      _first_canvas_resize = true;
      _old_width = 2;
      _old_height = 2;
      _transform = new AffineTransform();
      _canvas = new JBufferedCanvas();
      setLayout( new BorderLayout() );
      add( BorderLayout.CENTER, _canvas );
      _canvas.addMouseListener( this );
      _canvas.addComponentListener( this );
      _canvas.addMouseMotionListener( this );

   } // JCanvasPanel


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * In dieser Methode werden die darzustellenden Grafikobjekte gezeichnet.<p>
   *
   * Die implementierende Klasse kann entweder mit Pixelkoordinaten in das
   * spezifizierte <code>Graphics</code>-Objekt zeichnen oder mit
   * Weltkoordinaten in ein gecastetes <code>Graphics2D</code>-Objekt, auf
   * welches die spezifizierte Transformationsmatrix anzuwenden ist.
   * Folgendes Codefragment erlaeutert die Vorgehensweise:<p>
   *<pre>   (...) // zeichnen mit g</pre><p>
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
   public abstract void draw( Graphics g, AffineTransform transform );


   /**
   * Liefert die Groesse der Zeichenflaeche.
   *
   * @return die Groesse der Zeichenflaeche
   */
   public Dimension getCanvasSize() {

      return _canvas.getSize();

   } // getCanvasSize


   /**
   * Liefert das verwendete Koordinatensystem.
   *
   * @return das Koordinatensystem
   */
   public CoordinateSystem getCoordinateSystem() {

      return _reg.cosystem;

   } // getCoordinateSystem


   /**
   * Setzt das verwendete Koordinatensystem.<p>
   *
   * <b>Achtung: Falls diese Instanz ueber den Konstruktor
   * {@link #JCanvasPanel(Register) JCanvasPanel(Register register)} erzeugt
   * wurde, wird das Koordinatensystem dieses <code>Register</code>-Objekts
   * ueberschrieben! Dies hat Auswirkungen auf ein Editorsystem, das dieses
   * <code>Register</code>-Objekt verwendet.</b>
   *
   * @param cosystem das Koordinatensystem
   */
   public void setCoordinateSystem(
      CoordinateSystem cosystem
   ) {

      if ( cosystem != null )
         _reg.cosystem = cosystem;

   } // setCoordinateSystem


   /**
   * Aktiviert oder Deaktiviert die Scroll- und Zoomfunktion.
   *
   * @param enable <code>true</code> aktiviert die Funktionen,
   *        <code>false</code> deaktiviert sie
   */
   public void setScrollZoomEnabled(
      boolean enable
   ) {

      _mouse_enable = enable;
      if ( enable )
         _canvas.setCursor( _cs_hand );
      else
         _canvas.setCursor( _cs_default );

   } // setScrollZoomEnabled


   /**
   * Zeichnet das gesamte Anzeigepanel neu.
   */
   public void repaint() {

      _draw();
      super.repaint();

   } // repaint


   // *************************************************************************
   // Interface ComponentListener
   // *************************************************************************

   /**
   * Bei einer Groessenaenderung des Zeichenfensters muss die Position des
   * Koordinatenursprungs neu berechnet werden.
   *
   * @param e das <code>ComponentEvent</code>-Objekt
   */
   public void componentResized(
      ComponentEvent e
   ) {

      Component canvas = (Component) e.getSource();
      int w = canvas.getWidth();
      if ( w == 0 ) w++;
      int h = canvas.getHeight();
      if ( h == 0 ) h++;
      _old_width = w;
      _old_height = h;
      if ( _first_canvas_resize )
         _first_canvas_resize = false;
      else {
         _reg.cosystem.origin.x = ( w * _reg.cosystem.origin.x ) / _old_width;
         _reg.cosystem.origin.y = ( h * _reg.cosystem.origin.y ) / _old_height;
      } // else
      repaint();

   } // componentResized


   // folgende Methoden werden nicht verwendet:
   public void componentHidden( ComponentEvent e ) {}
   public void componentMoved( ComponentEvent e ) {}
   public void componentShown( ComponentEvent e ) {}


   // *************************************************************************
   // Interface MouseListener
   // *************************************************************************

   /**
   * Speichert die Klickposition fuer spaeteres Verschieben des
   * Zeichenfensterinhalts.
   *
   * @param e das <code>MouseEvent</code>-Objekt
   */
   public void mousePressed(
      MouseEvent e
   ) {

      if ( ! _mouse_enable ) return;

      _pressed = e.getPoint();
      _button = e.getButton();
      if ( _button == MouseEvent.BUTTON1 )
         _canvas.setCursor( _cs_move );
      else {
         _canvas.setCursor( _cs_zoom );
         _zoom_pixel = _pressed;
         _zoom_point = _reg.cosystem.transform( _zoom_pixel );
      } // else

   } // mousePressed


   /**
   * Restauriert den Cursor nach Zoomen und Verschieben des
   * Zeichenfensterinhalts.
   *
   * @param e das <code>MouseEvent</code>-Objekt
   */
   public void mouseReleased(
      MouseEvent e
   ) {

      if ( _mouse_enable )
         _canvas.setCursor( _cs_hand );
      else
         _canvas.setCursor( _cs_default );

   } // mouseReleased


   // folgende Methoden werden nicht benutzt:
   public void mouseClicked( MouseEvent e ) {}
   public void mouseEntered( MouseEvent e ) {}
   public void mouseExited( MouseEvent e ) {}


   // *************************************************************************
   // Interface MouseMotionListener
   // *************************************************************************

   /**
   * Reagiert auf Verschieben oder Zoomen des Zeichenfensterinhalts.
   *
   * @param e das <code>MouseEvent</code>-Objekt
   */
   public void mouseDragged(
      MouseEvent e
   ) {

      if ( ! _mouse_enable ) return;

      Point p = e.getPoint();
      // Koordinatensystem verschieben:
      if ( _button == MouseEvent.BUTTON1 ) {
         _reg.cosystem.origin.x += p.x - _pressed.x;
         _reg.cosystem.origin.y += p.y - _pressed.y;
         _pressed = p;
      } // if

      // Koordinatensystem zoomen:
      else {
         double factor = Math.pow( 1.01, Math.abs( p.y - _pressed.y ) );
         if ( p.y - _pressed.y < 0 ) factor = 1.0 / factor;
         _reg.cosystem.ppx *= factor;
         _reg.cosystem.ppy *= factor;
         if ( _reg.cosystem.ppx < 1.0e-4 ) {
            _reg.cosystem.ppx = 1.0e-4;
            _reg.cosystem.ppy = 1.0e-4;
         } else // if
         if ( _reg.cosystem.ppx > 1.0e+8 ) {
            _reg.cosystem.ppx = 1.0e+8;
            _reg.cosystem.ppy = 1.0e+8;
         } // if
         // Koordinatenursprung anpassen:
         _reg.cosystem.origin.x -= _reg.cosystem.transformX(
                                               _zoom_point.x ) - _zoom_pixel.x;
         _reg.cosystem.origin.y -= _reg.cosystem.transformY(
                                               _zoom_point.y ) - _zoom_pixel.y;
         // Beschriftung anpassen:
         _reg.cosystem.adjustMarkings();
         _pressed = p;
       } // else

     repaint(); // neuzeichnen

   } // mouseDragged


   // folgende Methode wird nicht benutzt:
   public void mouseMoved( MouseEvent e ) {}


   // *************************************************************************
   // Private methods
   // *************************************************************************

   /*
   * Neuzeichnen der Zeichenflaeche. Danach zeigt ein Aufruf der
   * repaint()-Methode die Aenderungen im Panel an.
   */
   private void _draw() {

      if ( _canvas == null ) return;
      Graphics g = _canvas.getImageGraphics();
      if ( g == null ) return;

      if ( ! _init_cosystem ) {
         _reg.cosystem.origin.x = _canvas.getWidth() / 2;
         _reg.cosystem.origin.y = _canvas.getHeight() / 2;
         _init_cosystem = true;
      } // if

      // alles loeschen:
      g.setColor( BACKGROUND_COLOR );
      g.fillRect( 0, 0, _canvas.getWidth(), _canvas.getHeight() );

      // Transformation setzen:
      _transform.setTransform( _reg.cosystem.ppx, 0.0, 0.0,
         - _reg.cosystem.ppy, _reg.cosystem.origin.x, _reg.cosystem.origin.y );

      // zeichnen:
      draw( g, _transform );

      // ggf. Koordinatensystem zeichnen:
      if ( _reg.cosystem.isVisible() )
         _reg.cosystem.draw( g, _canvas.getSize() );

      g.dispose();

   } // _drawScene


} // JCanvasPanel
