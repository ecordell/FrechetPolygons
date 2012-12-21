package anja.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;


import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

//additional imports
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;


/**
 * Ein JDisplayPanel hat die Aufgabe eine beliebige zweidimensionale
 * <i>Szene</i> in einem Koordinatensystem zu praesentieren und dabei Zoom- und
 * Scrollmoeglichkeiten bereitzustellen.<br>
 * Die anzuzeigende Szene ist ein Objekt der Klasse {@link Scene Scene}.
 *
 * @version 0.9 27.08.2004
 * @author Sascha Ternes
 */

public class JDisplayPanel extends JPanel implements ComponentListener,
        MouseListener,
        MouseMotionListener,
        MouseWheelListener {

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
    private static final Cursor _cs_hand;
    private static final Cursor _cs_move;
    private static final Cursor _cs_zoom;

    // user-definable default cursor
    private static Cursor _cs_user;

    // cursor override flag
    private boolean _bUserCursor;

    // *************************************************************************
    // Private variables
    // *************************************************************************

    // das Register der Komponenten:
    protected Register _reg; // geandert von private

    // gepufferte Zeichenflaeche:
    private JBufferedCanvas _canvas;
    // Flag fuer ihre Initialisierung:
    private boolean _first_canvas_resize;
    // alte Zeichenflaechengroesse fuer Ursprungsberechnung:
    private int _old_width;
    private int _old_height;

    // aktuelle Transformationsmatrix:
    private AffineTransform _transform;

    // Bezugspunkt fuer Koordinatensytem-Verschiebung:
    private Point _pressed;
    // angeklicktes Szenenobjekt:
    protected SceneObject _object; //geandert von private
    // gedrueckte Maustaste:
    private int _button;
    // Zentrum beim Zoomen:
    private Point _zoom_pixel;
    private Point2D.Double _zoom_point;

    // *************************************************************************
    // Class constructor
    // *************************************************************************

    static {

        _cs_hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        _cs_move = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        _cs_zoom = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);

    } // static


    // *************************************************************************
    // Constructors
    // *************************************************************************

    /*
     * Verbotener Konstruktor.
     */
    private JDisplayPanel() {}


    /**
     * Erzeugt ein neues Anzeigepanel. Dieses registriert sich im uebergebenen
     * Register-Objekt.
     *
     * @param register das Register-Objekt
     */
    public JDisplayPanel(
            Register register
            ) {

        super();
        register.display = this;
        _reg = register;
        _first_canvas_resize = true;

        _old_width = 2;
        _old_height = 2;

        _transform = new AffineTransform();

        _canvas = new JBufferedCanvas();
        _canvas.setCursor(_cs_hand);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, _canvas);

        _canvas.addMouseListener(this);
        _canvas.addComponentListener(this);
        _canvas.addMouseMotionListener(this);

        // experimental mouse wheel zoom support
        _canvas.addMouseWheelListener(this);

        //added section
        _bUserCursor = false;
        _cs_user = null;

    } // JDisplayPanel


    // *************************************************************************
    // Public methods
    // *************************************************************************

    /**
     * Liefert die Groesse der Zeichenflaeche.
     *
     * @return die Groesse der Zeichenflaeche
     */
    public Dimension getCanvasSize() {

        return _canvas.getSize();

    } // getCanvasSize


    /**Liefert die Zeichenflaeche.
     *
     */
    public JBufferedCanvas getCanvas(){
        return _canvas;
    }

    /**
     * Zeichnet das gesamte Anzeigepanel neu.
     */
    public void repaint() {

        _draw();

       super.repaint();

    } // repaint

    public void setDefaultUserCursor(Cursor cur) {
        _cs_user = cur;
        _canvas.setCursor(cur);
        _bUserCursor = true;
    }

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
        if (w == 0) {
            w++;
        }
        int h = canvas.getHeight();
        if (h == 0) {
            h++;
        }
        _old_width = w;
        _old_height = h;
        if (_first_canvas_resize) {
            _first_canvas_resize = false;
        } else {
            _reg.cosystem.origin.x = (w * _reg.cosystem.origin.x) / _old_width;
            _reg.cosystem.origin.y = (h * _reg.cosystem.origin.y) / _old_height;
        } // else

        _reg.cosystem.center(this.getSize());
        /*
               // pass new canvas dimensions to scene
               _reg.scene.setDisplayDimensions(canvas.getWidth(),
                  canvas.getHeight());*/

        //repaint(); // is this necessary ?

    } // componentResized


    // folgende Methoden werden nicht verwendet:
    public void componentHidden(ComponentEvent e) {}

    public void componentMoved(ComponentEvent e) {}


    public void componentShown(ComponentEvent e) {

        //Component canvas = (Component)e.getSource();
    }


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

        _pressed = e.getPoint();
        _button = e.getButton();

        System.out.println(_pressed);
        System.out.println(_reg.cosystem.transform(_pressed));
        if (_reg.scene == null) {
            _object = null;
        } else { if(_reg.editor!=null)
            _object = _reg.scene.selectObject(
                    _reg.cosystem.transform(_pressed), _reg.editor);
        }
        if (_object == null) {
            if (_button == MouseEvent.BUTTON1) {
                _canvas.setCursor(_cs_move);
            } else {
                _canvas.setCursor(_cs_zoom);
                _zoom_pixel = _pressed;
                _zoom_point = _reg.cosystem.transform(_zoom_pixel);
            } // else
        }

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
        if (_bUserCursor) {
            // use user-defined default cursor
            _canvas.setCursor(_cs_user);
        } else {
            _canvas.setCursor(_cs_hand);
        }

    } // mouseReleased


    /**
     * Reagiert auf einen Mausklick auf die freie Zeichenfensterflaeche oder ein
     * Szenenobjekt.
     *
     * @param e das <code>MouseEvent</code>-Objekt
     */
    public void mouseClicked(
            MouseEvent e
            ) {

        if (_reg.editor == null) {
            return;
        }
        if (_object == null) {
            _reg.editor.canvasClicked(_pressed, _button);
        } else // if
        if (_button == MouseEvent.BUTTON1) {
            _reg.editor.objectClicked(_object, _pressed);
        } else { // if
            _reg.editor.objectContext(_object, _pressed);
        }

    } // mouseClicked


    // folgende Methoden werden nicht benutzt:
    public void mouseEntered(MouseEvent e) {
        // this might be temporary...
        //System.out.println("mouse in JDisplayPanel");
        this.grabFocus();

    } // mouseEntered

    public void mouseExited(MouseEvent e) {
        // this might be temporary...
        //System.out.println("mouse out of JDisplayPanel");
        this.transferFocus();

    } // mouseExited


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

        Point p = e.getPoint();
        if (_object == null) {
            // Koordinatensystem verschieben:
            if (_button == MouseEvent.BUTTON1) {
                _reg.cosystem.origin.x += p.x - _pressed.x;
                _reg.cosystem.origin.y += p.y - _pressed.y;
                _pressed = p;
              if(_reg.editor!=null)  _reg.editor.canvasScrolled(p);
            } // if

            // Koordinatensystem zoomen:
            else {
                double factor = Math.pow(1.01, Math.abs(p.y - _pressed.y));
                if (p.y - _pressed.y < 0) {
                    factor = 1.0 / factor;
                }
                _reg.cosystem.ppx *= factor;
                _reg.cosystem.ppy *= factor;
                if (_reg.cosystem.ppx < 1.0e-4) {
                    _reg.cosystem.ppx = 1.0e-4;
                    _reg.cosystem.ppy = 1.0e-4;
                } else // if
                if (_reg.cosystem.ppx > 1.0e+8) {
                    _reg.cosystem.ppx = 1.0e+8;
                    _reg.cosystem.ppy = 1.0e+8;
                } // if
                // Koordinatenursprung anpassen:
                _reg.cosystem.origin.x -= _reg.cosystem.transformX(
                        _zoom_point.x) - _zoom_pixel.x;
                _reg.cosystem.origin.y -= _reg.cosystem.transformY(
                        _zoom_point.y) - _zoom_pixel.y;
                // Beschriftung anpassen:
                _reg.cosystem.adjustMarkings();
                _pressed = p;

              if(_reg.editor!=null)  _reg.editor.canvasZoomed(p);
            } // else
        }

        else
        if (_reg.editor != null) {
            _reg.editor.objectDragged(_object, p, _button);
        }

        repaint(); // neuzeichnen

    } // mouseDragged


    /**
     * Reagiert auf eine Bewegung des Mauscursors ueber die Zeichenflaeche.
     *
     * @param e das <code>MouseEvent</code>-Objekt
     */
    public void mouseMoved(MouseEvent e) {

        //_cursor_position = e.getPoint();

        if (_reg.editor != null) {
            _reg.editor.cursorMoved(e.getPoint());
        }

    } // mouseMoved


    /**
     *
     * Reacts to mouse wheel movement. Used to change the zoom factor
     * of the coordinate system
     *
     */

    public void mouseWheelMoved(MouseWheelEvent event) {
        // see mousePressed() and mouseDragged()
        //_canvas.setCursor( _cs_zoom );

        _zoom_pixel = event.getPoint(); //_cursor_position;
        _zoom_point = _reg.cosystem.transform(_zoom_pixel);

        int wheel_clicks = event.getWheelRotation();

        double factor = 1.1; //Math.pow( 1.2, Math.abs( wheel_clicks ) );
        if (wheel_clicks > 0) {
            factor = 1.0 / factor;
        }

        _reg.cosystem.ppx *= factor;
        _reg.cosystem.ppy *= factor;

        if (_reg.cosystem.ppx < 1.0e-4) {
            _reg.cosystem.ppx = 1.0e-4;
            _reg.cosystem.ppy = 1.0e-4;
        } else // if
        if (_reg.cosystem.ppx > 1.0e+8) {
            _reg.cosystem.ppx = 1.0e+8;
            _reg.cosystem.ppy = 1.0e+8;
        } // if

        // Koordinatenursprung anpassen:
        _reg.cosystem.origin.x -= _reg.cosystem.transformX(
                _zoom_point.x) - _zoom_pixel.x;
        _reg.cosystem.origin.y -= _reg.cosystem.transformY(
                _zoom_point.y) - _zoom_pixel.y;
        // Beschriftung anpassen:
        _reg.cosystem.adjustMarkings();
if(_reg.editor!=null)  _reg.editor.canvasZoomed(event.getPoint());

        repaint();

        /*
              // restore default cursor
           if(_bUserCursor)
              {
         // use user-defined default cursor
         _canvas.setCursor(_cs_user);
              }
              else _canvas.setCursor( _cs_hand );*/

    }

    // *************************************************************************
    // Private methods
    // *************************************************************************

    /*
     * Neuzeichnen der Zeichenflaeche. Danach zeigt ein Aufruf der
     * repaint()-Methode die Aenderungen im Panel an.
     */
    private void _draw() {

        if (_canvas == null) {
            return;
        }
        Graphics g = _canvas.getImageGraphics();
        if (g == null) {
            return;
        }

        // alles loeschen:
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, _canvas.getWidth(), _canvas.getHeight());

        // Szene zeichnen:
        _transform.setTransform(_reg.cosystem.ppx, 0.0, 0.0,
                                -_reg.cosystem.ppy, _reg.cosystem.origin.x,
                                _reg.cosystem.origin.y);
        if (_reg.extension != null) {
            _reg.extension.paintFirst(g, _transform);
        }
        if (_reg.scene != null) {
            _reg.scene.paint(g, _transform);
        }
        if (_reg.extension != null) {
            _reg.extension.paintLast(g, _transform);
        }

        // ggf. Koordinatensystem zeichnen:
        if (_reg.cosystem.isVisible()) {
            _reg.cosystem.draw(g, _canvas.getSize());
        }

        g.dispose();

    } // _drawScene

    /*
        public Dimension getMinimumSize()
        {
      return new Dimension(200,200);
        }

        public Dimension getPreferredSize()
        {
     return new Dimension(500,500);

        }

        public Dimension getMaximumSize()
        {
      return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }*/

} // JDisplayPanel
