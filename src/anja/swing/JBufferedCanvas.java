package anja.swing;

import javax.swing.JComponent;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import java.awt.event.ComponentEvent;


/**
* <code>JBufferedCanvas</code> ist eine von JComponent abgeleitete
* Zeichenflaeche. <code>JBufferedCanvas</code> verwaltet ein Offscreen-Image, in
* das mit dem durch {@link #getImageGraphics() getImageGraphics()} gelieferten
* Grafik-Objekt gezeichnet werden kann. Durch die Methode
* {@link #paint(Graphics) paint(...)} wird das Offscreen-Image in die
* Zeichenflaeche kopiert.<p>
*
* Ein Programmstueck, das gepuffert auf der Zeichenflaeche zeichnet, koennte also
* etwa folgendermassen aussehen:<pre>
*
*   // Zeichnen auf Offscreen-Image:
*   Graphics g = canvas.getImageGraphics
*   g.fillRect(...);
*   ...
*   g.dispose();
*
*   // Kopieren des Offscreen-Image auf Canvas:
*   g = canvas.getGraphics();
*   canvas.paint( g );
*   g.dispose();</pre>
*
* @version 0.9 19.06.2004
* @author Ulrich Handel, Sascha Ternes
*/

public class JBufferedCanvas
extends JComponent
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // das Offscreen-Image:
   private Image _img = null;
   // seine aktuelle Groesse:
   private int _img_width = 0;
   private int _img_height = 0;

   private Dimension _preferred_size = new Dimension(); // preferred canvas size


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt eine neue Zeichenflaeche.
   */
   public JBufferedCanvas() {

      setOpaque( true );
      setBackground( Color.white );
      // Reaktion auf resize erlauben:
      enableEvents( AWTEvent.COMPONENT_EVENT_MASK );
            
   } // JBufferedCanvas


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert das Grafik-Objekt des Offscreen-Image.
   *
   * @return das <code>Graphics</code>-Objekt oder <code>null</code>, wenn kein
   *         Grafik-Objekt existiert
   */
   public Graphics getImageGraphics() {

      if ( _img == null ) return null;
      return _img.getGraphics();

   } // getImageGraphics


   /*
   * [javadoc-Beschreibung wird aus JComponent kopiert]
   */
   public Dimension getMinimumSize() {

      return _preferred_size;

   } // getMinimumSize


   /*
   * [javadoc-Beschreibung wird aus JComponent kopiert]
   */
   public Dimension getPreferredSize() {

      return _preferred_size;

   } // getPreferredSize


   /**
   * Kopiert das Offscreen-Image in die Zeichenflaeche und zeigt es damit an.
   *
   * @param g das <code>Graphics</code>-Objekt
   */
   public void paint(
      Graphics g
   ) {

      if ( ( g != null ) && ( _img != null ) )
         g.drawImage( _img, 0, 0, this );

   } // paint


   /**
   * Reagiert auf Aenderung der Groesse der Zeichenflaeche.
   *
   * @param e das <code>ComponentEvent</code>-Objekt
   */
   public void processComponentEvent(
      ComponentEvent e
   ) {

      // Groesse der Zeichenflaeche wurde veraendert:
      if ( e.getID() == ComponentEvent.COMPONENT_RESIZED ) {
         /* Hier wird, falls erforderlich, ein neues Offscreen-Image erzeugt,
            das mindestens die neuen Abmessungen des Canvas hat.
            Es ist wichtig, dass dies geschieht, bevor registrierte
            ComponentListener ueber den Canvas-Resize informiert werden (Aufruf
            super.processComponentEvent() am Ende dieser Methode), da diese
            dann eventuell schon auf das aktuelle Offscreen-Image zeichnen
            wollen. */

         Dimension dim = getSize();
         int can_width = dim.width;
         int can_height = dim.height;
         // Die bevorzugten Abmessungen sind immer die aktuellen
         _preferred_size = dim;

         // die Canvas-Groesse uebersteigt die des aktuellen Images:
         if ( ( can_width > _img_width ) || ( can_height > _img_height ) ) {
            Image old_image = _img;
            // neue Groesse berechnen:
            _img_width = Math.max( can_width, _img_width );
            _img_height = Math.max( can_height, _img_height );
            // neues Image erzeugen:
            _img = createImage( _img_width, _img_height );

            // altes auf neues Image kopieren:
            if( old_image != null ) {
               Graphics g = _img.getGraphics();
               g.drawImage( old_image, 0, 0, this );
               g.dispose();
               old_image.flush(); // freigeben
            } // if
         } // if
      } // if

      super.processComponentEvent( e );

   } // processComponentEvent


   /*
   * [javadoc-Beschreibung wird aus JComponent kopiert]
   */
   public void setPreferredSize(
      Dimension size
   ) {

      _preferred_size = size;

   } // setPreferredSize


} // JBufferedCanvas
