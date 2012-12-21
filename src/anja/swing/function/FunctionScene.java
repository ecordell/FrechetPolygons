package anja.swing.function;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import java.util.Enumeration;
import java.util.Vector;

import org.jdom.Attribute;
import org.jdom.Element;

import anja.analysis.Bound;
import anja.analysis.RealFunction;

import anja.swing.Register;
import anja.swing.Scene;
import anja.swing.XMLParseException;


/**
* Eine Funktionenszene fasst mehrere mathematische Funktionen zu einer Szene
* zusammen, die in einem {@link anja.swing.JDisplayPanel JDisplayPanel}
* praesentiert werden kann.
*
* @version 0.9 03.09.2004
* @author Sascha Ternes
*/

public class FunctionScene
extends Scene
{

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // Flag fuer Zeichnen der Funktionen: Verbinden der Punkte
   private boolean _connect;


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt eine neue noch leere Funktionenszene. Diese registriert sich im
   * uebergebenen Register-Objekt.
   *
   * @param register das Register-Objekt
   */
   public FunctionScene(
      Register register
   ) {

      super( register );
      _connect = true;

   } // FunctionScene


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Legt fest, ob die gezeichneten Punkte der Funktionen verbunden werden
   * sollen, um visuell lueckenlose Funktionsgraphen zu zeichnen.
   *
   * @param connect <code>true</code> verbindet die gezeichneten Punkte, um
   *        lueckenlose Funktionsgraphen zu erhalten, bei <code>false</code>
   *        unterbleibt dies
   */
   public void setPointsConnected(
      boolean connect
   ) {

      _connect = connect;

   } // setPointsConnected


   // *************************************************************************
   // Interface Scene
   // *************************************************************************

   /**
   * Zeichnet alle Funktionen.
   *
   * @param g das <code>Graphics</code>-Objekt
   * @param transform die Transformationsmatrix fuer das Zeichnen mit
   *        Weltkoordinaten
   */
   public void paint(
      Graphics g,
      AffineTransform transform
   ) {

      if ( _connect)
         _drawFunctions( g );
      else
         _drawFunctionsSimple( g );

   } // paint


   /**
   * (Diese Methode ist noch nicht funktionsfaehig implementiert.)
   */
   public void mark(
      Graphics g,
      AffineTransform transform,
      Vector objects,
      Color color
   ) {

      return;

   } // createXML


   /**
   * (Diese Methode ist noch nicht funktionsfaehig implementiert.)
   */
   public void unmark(
      Graphics g,
      AffineTransform transform,
      Vector objects
   ) {

      return;

   } // createXML


   /*
   * [javadoc-Beschreibung wird aus Scene kopiert]
   */
   public Element createXML() {

      return null;

   } // createXML


   /*
   * [javadoc-Beschreibung wird aus Scene kopiert]
   */
   public void loadXML(
      Element scene
   ) throws XMLParseException {



   } // loadXML


   // *************************************************************************
   // Private methods
   // *************************************************************************

   /*
   * Zeichnen aller Funktionen ohne Verbinden der Punkte.
   */
   private void _drawFunctionsSimple(
      Graphics g
   ) {

      for ( int k = _objects.size() - 1; k >= 0; k-- ) {
         FunctionObject data = (FunctionObject) _objects.get( k ); 
         RealFunction f = data.getFunction();
         if ( f == null ) continue;
         Color color = data.getColor();
         if ( color == null ) color = FunctionObject.DEFAULT_COLOR;
         g.setColor( color );
         for ( int i = 0; i < _reg.display.getCanvasSize().width; i++ ) {
            double y = f.f( _reg.cosystem.transformX( i ) );
            if ( Double.isNaN( y ) ) continue;
            int j = _reg.cosystem.transformY( y );
            g.drawLine( i, j, i, j );
         } //
      } // for

   } // _drawFunctionsSimple


   /*
   * Zeichnen aller Funktionen mit Verbinden der Punkte.
   */
   private void _drawFunctions(
      Graphics g
   ) {

      int last_x = 0;
      int last_y = 0;
      for ( int k = _objects.size() - 1; k >= 0; k-- ) {
         FunctionObject data = (FunctionObject) _objects.get( k ); 
         RealFunction f = data.getFunction();
         if ( f == null ) continue;
         boolean no_last;
         Color color = data.getColor();
         if ( color == null ) color = FunctionObject.DEFAULT_COLOR;
         g.setColor( color );
         Enumeration e = f.getDiscontinuities().elements();
         int i = 0;
         int last = 0;
         boolean cont = true;
         // zwischen Sprungstellen den Funktionsgraphen zeichnen:
         do {
            if ( e.hasMoreElements() ) {
               // Sprungstellen-Pixel berechnen und anpassen:
               Bound bound = (Bound) e.nextElement();
               double dis = ( (Double) bound.n[0] ).doubleValue();
               last = _reg.cosystem.transformX( dis );
               if ( _reg.cosystem.transformX( last - 1 ) >= dis ) {
                  last--;
               } else // if
               if ( ( _reg.cosystem.transformX( last ) < dis ) ||
                    ( ( _reg.cosystem.transformX( last ) == dis ) &&
                                                       bound.inclusion[0] ) ) {
                  last++;
               } // if
            // letztes Segment hinter der letzten Sprungstelle:
            } else { // if
               last = _reg.display.getCanvasSize().width;
               cont = false;
            } // else
            no_last = true;
            // Zeichnen des Segments unter Beruecksichtigung von Def.luecken:
            for ( ; i < last; i++ ) {
               double y = f.f( _reg.cosystem.transformX( i ) );
               if ( Double.isNaN( y ) ) {
                  no_last = true;
                  continue;
               } // if
               int j = _reg.cosystem.transformY( y );
               if ( no_last ) {
                  g.drawLine( i, j, i, j );
                  no_last = false;
               } else { // if
                  g.drawLine( last_x, last_y, i, j );
               } // else
               last_x = i;
               last_y = j;
            } // for
         } while ( cont );
      } // for

   } // _drawFunctions


} // FunctionScene
