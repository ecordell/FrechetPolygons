package anja.swing.point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.BasicStroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import java.util.Enumeration;
import java.util.Iterator;
//import java.util.LinkedList;
import java.util.Vector;
import java.util.Collections;

import org.jdom.Attribute;
import org.jdom.Element;

import anja.util.Angle;
import anja.util.GraphicsContext;
import anja.geom.Arc2;
import anja.geom.Point2;
import anja.geom.Circle2;
import anja.geom.Point2List;


import anja.swing.Register;
import anja.swing.Scene;
import anja.swing.SceneObject;
import anja.swing.XMLFile;
import anja.swing.XMLParseException;

import anja.swing.event.SceneEvent;


/**
* PointScene represents an (unordered) two-dimensional point
* set. Points can be added, removed and modified at any time.
* 
*
* @version 0.3 26.08.2004
* @author Sascha Ternes, Ibragim Kuliyev
* 
* TODO: Ok, here's the deal. For now this thing uses a vector
* for internal data storage. In such implementation  
* modification/search/range search/removal operations have 
* O(n) complexity and WILL become inefficient if the number of 
* points becomes high. In the future, a 
* much better internal data structure would be a quadtree.
* 
**/

public class PointScene extends Scene
{

   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Standard-Durchmesser eines Punktkreises:
   protected static final int _SIZE = 5; //geandert von private

   // *************************************************************************
   // Private variables
   // *************************************************************************

   
   //private Point2List _list;

   // point container
   //private Vector _rPoints;
   
   // GraphicsContext objects for drawing points
   protected GraphicsContext _rNormalGC;  //geandert von private
   protected GraphicsContext _rSelectedGC; //geandert von private

   protected PointObject _rSelectedPoint; //geaendert von private
   
   protected Point  _rCursorPosition;
   
   protected BasicStroke _rDashStroke; //geaendert von private
   protected boolean	   _bDrawCrosshair; // crosshair enabled ? geandert von private
   
    
   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Erzeugt eine neue noch leere Punktszene. Diese registriert sich im
   * uebergebenen Register-Objekt.
   *
   * @param register das Register-Objekt
   */
   public PointScene(Register register)
   {

      super( register );
      
      //_list = new Point2List();
      //_rPoints = new Vector();
      
      _rNormalGC = new GraphicsContext();
      _rNormalGC.setForegroundColor( Color.black );
      _rNormalGC.setFillColor( new Color(10,150,255) ); // medium blue
      _rNormalGC.setFillStyle( 1 );
      _rNormalGC.setLineWidth( 1.0f );
            
      _rSelectedGC = new GraphicsContext();
      _rSelectedGC.setForegroundColor(Color.black);
      _rSelectedGC.setFillColor(Color.yellow);
      _rSelectedGC.setFillStyle( 1 );
      _rSelectedGC.setLineWidth( 1.0f );
      
      _rCursorPosition = new Point(0,0);
            
      _bDrawCrosshair = false;
      
      // create dashed stroke for visual aids
      float dash[] = {5.0f};
      _rDashStroke = new BasicStroke(1.0f,
      								 BasicStroke.CAP_BUTT,
									 BasicStroke.JOIN_MITER,
									 1.0f, dash, 0.0f);
      
   } // PointScene


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
    * Sets the current cursor position for later use by visual guidelines
    * @param position new cursor position
    * 
    */
   
   public void setCursorPosition(Point position)
   {
   		_rCursorPosition = position;
   		//System.out.println("cursor at "+position.toString());
   }
      
   /**
    *  Enables / disables drawing of the dashed crosshair guidelines
    * 
    */
   public void enableCrosshairs(boolean enable)
   { _bDrawCrosshair = enable; }
   
 
   public void paint(Graphics g, AffineTransform transform) 
   {
      Graphics2D g2d = (Graphics2D) g;
      // set some rendering attributes
      
      /*
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
      					   RenderingHints.VALUE_ANTIALIAS_ON); 
      
      g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
      					   RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
     					   RenderingHints.VALUE_RENDER_SPEED); */ 
     
      // save previous transform 
      //AffineTransform at = g2d.getTransform();
       PointObject po; 
     
      // visual symbol for the points
      Circle2 dot = new Circle2();
      dot.radius = 5.0f;
      
       //dot.radius = (float)(1.0f / tx.getScaleX());
      //_normal_gc.setLineWidth((float)(1.0 / tx.getScaleX()));
           
      for ( int i = 0; i < _objects.size(); i++ ) 
      {
         po = (PointObject) _objects.get( i );
         Point2 p = po.getPoint();
                      
         dot.centre = new Point2( _reg.cosystem.transformX( p.x ),
         						  _reg.cosystem.transformY( p.y ));
         
         if( po == _rSelectedPoint)
         {         	
         	// draw the selection rectangle and the selected point
         	
         	/*
         	 * WARNING: this code is not optimized and thus potentially
         	 * slow!
         	 */
         	
            g.setColor(Color.red);
         	g.drawRect(Math.round(dot.centre.x - dot.radius - 5),
         			   Math.round(dot.centre.y - dot.radius - 5),
         			   Math.round(2*(dot.radius + 5)),
					   Math.round(2*(dot.radius + 5))); 
         	
         	dot.draw(g2d, _rSelectedGC);
         }
         else
         {
         	// draw other points
         	dot.draw(g2d, _rNormalGC);         
         }
      }
      
      //draw the visual aids
     
      if(_bDrawCrosshair)
      {
      		int _iDisplayWidth = _reg.display.getWidth();
      		int _iDisplayHeight = _reg.display.getHeight();
      		
	      	//guidelines
	        g2d.setColor(Color.gray);
	        
	        BasicStroke lastStroke = (BasicStroke)g2d.getStroke();
	        g2d.setStroke(_rDashStroke);
	        
	        g2d.drawLine(0, _rCursorPosition.y, 
	        			 _iDisplayWidth, _rCursorPosition.y);
	        
	        g2d.drawLine(_rCursorPosition.x, 0, 
	        			 _rCursorPosition.x, _iDisplayHeight);
	        
	        g2d.setStroke(lastStroke);
      }
      
      //g2d.setTransform(at);

   } // paint

 
   public Element createXML() 
   {

      return null; // fertig

   } // createXML


   public void loadXML(Element scene) throws XMLParseException 
   {



   } // loadXML

   public void add(SceneObject object, Object source)
   {

      
      super.add( object, source );
      
      //sort();
      
      /*
      _rPoints.add(object);
      Collections.sort(_rPoints);
      
      SceneEvent e = new SceneEvent(source, this, object);
      fireSceneEvent(Scene.OBJECT_ADDED, e); */

   } // addPoint

   public void insert(SceneObject object, Object source) 
   {

      //_list.insertFront( ( (PointObject) object ).getPoint() );
      super.insert( object, source );
      
      //sort();

   } // insert

   public void remove(SceneObject object, Object source) 
   {
      //_list.points().remove( ( (PointObject) object ).getPoint() );
      super.remove( object, source );
      
      _rSelectedPoint = null; // reset 
      
      //sort();

   } // remove

   public void clear(Object source)
   {

      //_list.clear();
      super.clear( source );

   } // clear
 
   public SceneObject closestObject(Point2D.Double point)
   {
      // naechstgelegenen Knoten suchen:
      PointObject closest = null;
      PointObject vo;
      Point2 p;
      double distance = Double.POSITIVE_INFINITY;
      double d;
      for ( int i = 0; i < _objects.size(); i++ ) {
         vo = (PointObject) _objects.get( i );
         p = vo.getPoint();
         d = point.distance( p.x, p.y );
         if ( d < distance ) {
            distance = d;
            closest = vo;
         } // if
      } // while

      return closest;

   } // closestObject


   /*
   * [javadoc-Beschreibung wird aus Scene kopiert]
   */
   public SceneObject selectObject(Point2D.Double point,
   								   Object source) 
   {

      // Ermittlung des naechstgelegenen Punktobjekts:
      PointObject object = (PointObject) closestObject( point );
      
      if ( ( object != null ) && ( _isClicked( object, point ) ) ) 
      {
      	 _rSelectedPoint = object; // remember current object
      	      	
         SceneEvent e = new SceneEvent( source, this, object );
         fireObjectSelected( e );
         
         _reg.display.repaint();
         
         return object;
      } // if

      _rSelectedPoint = null;
      return null; // kein Punktobjekt selektiert

   } // selectObject

   /**
    * Use this to retrieve the currently selected point object
    * @return a reference to the selected point
    */
   public PointObject getSelectedPoint()
   { return _rSelectedPoint; }
   
  
   // *************************************************************************
   // Private methods
   // *************************************************************************


   /*
   * Prueft, ob ein Punkt angeklickt wurde.
   */
   private boolean _isClicked(PointObject object,
   							  Point2D.Double point)
   {

      Point2 p = object.getPoint();
      double d = point.distance( p.x, p.y ) * _reg.cosystem.ppx;
      double size = _SIZE;
      if ( d <= ( size / 2.0 + 2.0 ) ) return true;
      return false;

   } // _isClicked

   /** 
	 * Generates a set of random points, with total of <b>numPoints</b> 
	 * points, confined to the rectangular region specified by 
	 * <code>bounds</code>. 
	 * 
	 * @param numPoints number of points to generate
	 * @param bounds the bounding rectangle in world coordinates
	 * 
	 */
   
   public void createRandomPoints(int numPoints, Rectangle2D.Double bounds)
   {
   		//stub
   	
 
   }


	public void mark(Graphics g, AffineTransform transform, 
					 Vector objects, Color color)
	{
		
		
	}
	
	public void unmark(Graphics g, AffineTransform transform, Vector objects)
	{
		
	
	}
   
} // PointScene


/* JUNK
 * 
 * Arc2 tag1 = new Arc2(dot.centre.x, dot.centre.y,2.0f * dot.radius, 
							     -Math.PI / 3.0,-(2.0/3.0)*Math.PI,
							     Angle.ORIENTATION_RIGHT);
         	
         	Arc2 tag2 = new Arc2(dot.centre.x, dot.centre.y,2.0f * dot.radius, 
         						 -Math.PI / 3.0,-(2.0/3.0)*Math.PI,
								 Angle.ORIENTATION_RIGHT);
         	
         	Arc2 tag3 = new Arc2(dot.centre.x, dot.centre.y,2.0f * dot.radius, 
				     			 -Math.PI / 3.0,-(2.0/3.0)*Math.PI,
								 Angle.ORIENTATION_RIGHT);
         	
         	tag1.draw(g2d, _rNormalGC);
         	tag2.draw(g2d, _rNormalGC);
         	tag3.draw(g2d, _rNormalGC);
 */
 
