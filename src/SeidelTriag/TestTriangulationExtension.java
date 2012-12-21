package SeidelTriag;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import anja.geom.Point2;
import anja.geom.Polygon2;
import anja.geom.Polygon2Scene;

import anja.geom.triangulation.Polygon2Triangulation;
import anja.geom.triangulation.Triangulation;

import anja.swinggui.polygon.Extendable;
import anja.swinggui.polygon.ExtendablePolygonEditor;



/**
* Diese Klasse testet Polygontriangulation im Polygoneditor.
*
* @version 0.1 23.06.2003
* @author Sascha Ternes
*/

public class TestTriangulationExtension
   implements Extendable
{

   // *************************************************************************
   // Interface Extendable
   // *************************************************************************

   /**
   * Zeichnet die Triangulation.
   */
   public void paint(
      Polygon2Scene scene,
      Graphics2D g2d
   ) {

      Polygon2 polygon = scene.getPolygon( 0 );
      Triangulation _triangulation = null;
      try {
         _triangulation = new Polygon2Triangulation( polygon );
         _triangulation.draw( g2d );
      } catch ( IllegalArgumentException iae ) {} // try

   } // paint


   // folgende Methoden werden nicht verwendet, müssen aber zurückliefern:
   public boolean processPopup( ActionEvent e, Point2 point ) {
      return true; // Verarbeitung im Editor erlauben
   }
   public boolean processMouseDragged( MouseEvent e, Point2 point ) {
      return true; // Verarbeitung im Editor erlauben
   }
   public boolean processMousePressed( MouseEvent e, Point2 point ) {
      return true; // Verarbeitung im Editor erlauben
   }
   public boolean processMouseReleased( MouseEvent e, Point2 point ) {
      return true; // Verarbeitung im Editor erlauben
   }

   // folgende Methoden werden nicht verwendet:
   public void popupMenu( JPopupMenu menu ) {} // popupMenu
   public void registerPolygonEditor( ExtendablePolygonEditor editor ) {}


} // TestTriangulationExtension
