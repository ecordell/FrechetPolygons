package anja.swing.point;

import java.awt.geom.Point2D;

import org.jdom.Attribute;
import org.jdom.Element;

import anja.geom.Point2;

import anja.swing.SceneObject;
import anja.swing.XMLFile;
import anja.swing.XMLParseException;

import anja.util.ColorName;


/**
* Szenenobjekt fuer einen Punkt in einer Punktszene.
*
* @version 0.1 26.08.2004
* @author Sascha Ternes
*/

public class PointObject
extends SceneObject
{

   // *************************************************************************
   // Protected variables
   // *************************************************************************

   /**
   * die Position dieses Punktobjekts
   */
   protected Point2 _point;

   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt ein neues Punktobjekt fuer den spezifizierten Punkt.
   *
   * @param point der Punkt, eine Instanz der Klasse {@link Point2 Point2};
   *        <code>null</code> ist verboten!
   */
   public PointObject(Point2 point) 
   {

      _point = point;
      point.setLabel( "point" + _next_suffix++ );

   } // PointObject


   /*
   * [javadoc-Beschreibung wird aus SceneObject kopiert]
   */
   public PointObject(Element xml) throws XMLParseException 
   {
   } // PointObject


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /*
   * [javadoc-Beschreibung wird aus GraphObject kopiert]
   */
   public String getName() 
   {
      return _point.getLabel();
   } // getName


   /*
   * [javadoc-Beschreibung wird aus GraphObject kopiert]
   */
   public void setName(String name) 
   {
      if ( name != null )
         _point.setLabel( name );

   } // setName


   /**
   * Liefert den zugehoerigen Punkt.
   *
   * @return den Punkt
   */
   public Point2 getPoint() 
   {
      return _point;

   } // getPoint


   /**
   * Setzt dieses Punktobjekt an eine neue Position.
   *
   * @param position die neue Position in Weltkoordinaten
   */
   public void setPosition(Point2D.Double position) 
   {
      _point.moveTo( position.x, position.y );

   } // setPosition


   /*
   * [javadoc-Beschreibung wird aus SceneObject kopiert]
   */
   public String toString() 
   {
      return _point.getLabel();
   } // toString


   /*
   * [javadoc-Beschreibung wird aus SceneObject kopiert]
   */
   public Element createXML() 
   {
      return null; // fertig

   } // createXML


   /**
   * Testet, ob das spezifizierte Punktobjekt mit diesem Punktobjekt identisch
   * ist. Das ist dann der Fall, wenn die korrespondierenden Punkte gleich
   * sind.
   *
   * @param point_object das Testobjekt
   * @return <code>true</code>, falls die Objekte identisch sind
   */
   public boolean equals(Object point_object)
   {

      try
	  {
         PointObject p = (PointObject) point_object;
         if ( this._point.equals( p._point ) ) return true;
      } catch ( ClassCastException cce ) 
	  
	  { // try
         try 
		 {
            Point2 p2 = (Point2) point_object;
            if ( _point.equals( p2 ) ) return true;
         } catch ( ClassCastException cce2 ) {} // try
      } // catch
      return false;

   } // equals

   /**
    *  Compares the points 'lexicografically', by comparing their
    *  x coordinates. 
    * 
    * 
    */
   
   public int compareTo(Object o) 
   {
      PointObject pobject = (PointObject)o;
      Point2 p = pobject.getPoint();
      
      if(p.x < _point.x) return -1;
      else 
      if(p.x > _point.x) return 1;
      else return 0;

   } 

} // PointObject
