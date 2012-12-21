/*
 * File: DCEL_Edge.java
 * Created on Nov 2, 2005 by ibr
 *  
 */
package anja.graph.dcel;

import java.awt.geom.*;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics2D;

import anja.graph.*;
import anja.util.MathConstants;

/*
 * Package visibility here, since these classes are not meant
 * to be instantiated from outside the DCEL package
 */

/**
 * TODO: !important!
 * if source or target is changed via setSource or setTarget, 
 * the edges around the old and new sources or targets have to be updated
 * generally setsource or settarget should probably be avoided altogether
 * and just a new edge created instead
 * 
 * 
 * @author ibr
 *
 * TODO Write documentation
 */

class DCEL_Edge extends Edge<DCEL_Vertex> implements MathConstants
{

    //*************************************************************************
    //                             Public constants
    //*************************************************************************
    
    //*************************************************************************
    //                             Private constants
    //*************************************************************************
    
    private static final boolean _DRAW_PIES = false;
    
    //*************************************************************************
    //                             Class variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Public instance variables
    //*************************************************************************
          
    //*************************************************************************
    //                       Protected instance variables
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance variables
    //*************************************************************************
    
    /**
     * These are actually not required by the data structure per se,
     * but have been included as helper references for various purposes
     */
    
    /** 
     * Next edge in clockwise order as seen from target vertex 
     */
    private DCEL_Edge _next_CW_Edge;
    
    /**
     *  Previous edge in clockwise order as seen from source vertex 
     */
    private DCEL_Edge _prev_CW_Edge;
    
    /** 
     * Next edge in counter-clockwise order as seen from target vertex 
     */
    private DCEL_Edge _next_CCW_Edge;
    
    /**
     * Previous edge in counter-clockwise order as seen from source
     * vertex
     */
    private DCEL_Edge _prev_CCW_Edge;
    
    /**
     * Left face, relative to the edge's orientation
     */
    private DCEL_Face _leftFace;    
    
    /**
     * Right face, relative to the edge's orientation
     */
    private DCEL_Face _rightFace;
    
    
    /* FIXME: correct these comments...
     * Stores the pseudo-cosine value of the angle between this edge
     * and another edge which is incident on the same vertex and is taken
     * to be the 'first one' in CCW order. 
     * 
     * 
     * These values are subsequently used in a kind of 'insertion sort'
     * that automatically orders edges into a CCW ordering around a vertex.
     */
    private double _sourceAngle;
    private double _targetAngle;
      
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    /**
     * Creates a new DCEL_Edge and initializes it with the
     * supplied edge reference.
     * 
     */
    protected DCEL_Edge(DCEL_Vertex source, DCEL_Vertex target)
    {
        super(source, target);
                
        _sourceAngle  = 0.0;
        _targetAngle  = 0.0;
        
        _leftFace     = _rightFace     = null;
        _next_CW_Edge = _next_CCW_Edge = null;
        _prev_CW_Edge = _prev_CCW_Edge = null;
        
        verticesMoved(); // calculate initial orientation angles
        
    }
        
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
    
    //-------------------------- Getters / setters ----------------------------
    
    /**
     * For method descriptions, see documentation for
     */
    
    public Face getLeftFace()
    {
        return _leftFace;
    }

    //*************************************************************************
    
    public Face getRightFace()
    {
        return _rightFace;
    }
    
    //*************************************************************************

    public Edge getNextEdgeCW()
    {
        if (this.isSling()) return _source.getDcelEdge();
        return _next_CW_Edge;
    }
    
    //*************************************************************************

    public Edge getPrevEdgeCW()
    {
        if (this.isSling()) return _source.getDcelEdge();
        return _prev_CW_Edge;
    }
    
    //*************************************************************************

    public Edge getNextEdgeCCW()
    {
        if (this.isSling()) return _source.getDcelEdge();
        return _next_CCW_Edge;
    }
    
    //*************************************************************************

    public Edge getPrevEdgeCCW()
    {
        if (this.isSling()) return _source.getDcelEdge();
        return _prev_CCW_Edge;
    }
    
    //********************** Package-internal methods *************************
    
    //-------------------------- Getters / setters ----------------------------
    
    
    //*************************************************************************
    
    void setLeftDcelFace(DCEL_Face leftFace)
    {
        _leftFace = leftFace;
    }

    //*************************************************************************
    
    void setRightDcelFace(DCEL_Face rightFace)
    {
        _rightFace = rightFace;
    }
    
    //*************************************************************************

    DCEL_Face getRightDcelFace()
    {
        return _rightFace;
    }
    
    //*************************************************************************

    DCEL_Face getLeftDcelFace()
    {
        return _leftFace;
    }
    
    //*************************************************************************
    
    DCEL_Edge getNextDcelEdgeCW()
    {
        if (this.isSling()) return _source.getDcelEdge();
        return _next_CW_Edge;
    }
    
    //*************************************************************************
    
    void setNextDcelEdgeCW(DCEL_Edge edge)
    {
        _next_CW_Edge = edge;
    }
    
    //*************************************************************************
    
    DCEL_Edge getNextDcelEdgeCCW()
    {
        if (this.isSling()) return _source.getDcelEdge();
        return _next_CCW_Edge;
    }
    
    //*************************************************************************
    
    void setNextDcelEdgeCCW(DCEL_Edge edge)
    {
        _next_CCW_Edge = edge;
    }
    
    //*************************************************************************
    
    DCEL_Edge getPrevDcelEdgeCW()
    {
        if (this.isSling()) return _source.getDcelEdge();
        return _prev_CW_Edge;
    }
    
    //*************************************************************************
    
    void setPrevDcelEdgeCW(DCEL_Edge edge)
    {
        _prev_CW_Edge = edge;
    }
    
    //*************************************************************************
    
    DCEL_Edge getPrevDcelEdgeCCW()
    {
        if (this.isSling()) return _source.getDcelEdge();
        return _prev_CCW_Edge;
    }
    
    //*************************************************************************
    
    void setPrevDcelEdgeCCW(DCEL_Edge edge)
    {
        _prev_CCW_Edge = edge;
    }
    
    //*************************************************************************
    
    double getSourceAngle()
    {
        return _sourceAngle;
    }
    
    //*************************************************************************
    
    double getTargetAngle()
    {
        if (this.isSling()) return -3;
        return _targetAngle;
    }
    
    
    //-------------------------------- Queries --------------------------------
    
    /**
     * 
     */
    boolean sourceIsSelfLinked()
    {
        return ( (this == _prev_CW_Edge) && (this == _prev_CCW_Edge) );
    }
    
    //*************************************************************************
    
    /**
     * 
     */
    boolean targetIsSelfLinked()
    {
        return ( (this == _next_CW_Edge) && (this == _next_CCW_Edge) );
    }
    
    //*************************************************************************
    
    boolean hasSameSource(DCEL_Edge edge)
    {
        return (_source == edge.getSource());
    }
    
    //*************************************************************************
    
    boolean hasSameTarget(DCEL_Edge edge)
    {
        return (_target == edge.getTarget());
    }
    
    //*************************************************************************
    
    
    //*************************************************************************
    
    /**
     * This method calculates orientation 'angles' an edge makes
     * relative to 12 o'clock at its source and target vertices. These
     * values are only used internally by the DCEL for ordering edges
     * around vertices, so for efficiency the real angles are <u>not</u>
     * computed (as this would require inverse trigonometric mapping
     * via acos(), which is slow).<br>
     * [the math inside this method is, due to certain presumptions, 
     * optimized to some extent]
     * 
     */
    void verticesMoved()
    {
		/* Note: since the 12 o'clock vector is always (0,1),
		 * the dot products between it and edge directions are
		 * not explicitly calculated - the dot essentially
		 * discards the x-coordinate, so we can just use
		 * the edge vector's normalized y-coordinate.
		 *
		 */
		
		double angle;
		double dir_x, dir_y; // edge direction vectors
		
		// calculate 'angle' for source vertex
		// direction is from v1->v2
		
		dir_x = _target.getX() - _source.getX();
		dir_y = _target.getY() - _source.getY();
		
		angle = dir_y / getLength();
		
		// bias value so that angle is in range [1, -3]
		if( dir_x < 0.0 ) {
			_sourceAngle = (-1.0)*angle - 2.0;
		} else {
			_sourceAngle = angle;
		}
		
		/* now do the same for target vertex
		 * direction is now from v2->v1 => negate the direction vector
		 * which obviously negates the 'angle' value as well [hmmmm....]
		 */
		
		dir_x *= -1.0;
		angle *= -1.0;
		
		if( dir_x < 0.0 ) {
			_targetAngle = (-1.0)*angle - 2.0;
		} else {
			_targetAngle = angle;
		}
		
        
        // debug
//        System.out.println("source:" + Math.toDegrees(_sourceAngle) 
//                         + " target:" + Math.toDegrees(_targetAngle));
    }
    
    //*************************************************************************

    public void drawLinks(Graphics2D g2d, double pixelSize, Font font)
    {
      
        Edge prev_CW  = null; 
        Edge prev_CCW = null;
        Edge next_CW  = null; 
        Edge next_CCW = null; 
        
        boolean draw_PCW  = true;
        boolean draw_PCCW = true;
        boolean draw_NCW  = true;
        boolean draw_NCCW = true;
   
        // arc parameters
        double center_x = _source.getX();
        double center_y = _source.getY();
        double radius   = _source.getRadius() + 20;
        
        Arc2D arc            = new Arc2D.Double();
        //Ellipse2D tip        = new Ellipse2D.Double();
        //Point2D tip_position;
        
        // edge angle
        double this_angle = Math.toDegrees(this.angle());
        
        // ------------ debug -----------------------------------
        
        
        
        // ------------------------------------------------------
       
        // attempt to get edge links
        
        if(_prev_CW_Edge == null)
        {
            draw_PCW = false;
            System.out.println("No PCW for "+this.toString());
        }
        else
        {
            prev_CW  = _prev_CW_Edge;
        }
        
        if(_prev_CCW_Edge == null)
        {
            draw_PCCW = false;
            System.out.println("No PCCW for "+this.toString());
        }
        else
        {
            prev_CCW = _prev_CCW_Edge;  
        }
        
        if(_next_CW_Edge == null)
        {
            draw_NCW = false;
            System.out.println("No NCW for "+this.toString());
        }
        else
        {
            next_CW  = _next_CW_Edge;
        }
        
        if(_next_CCW_Edge == null)
        {
            draw_NCCW = false;
            System.out.println("No NCCW for "+this.toString());
        }
        else
        {
            next_CCW = _next_CCW_Edge; 
        }
        
        // remember previously used color
        Color prev_color = g2d.getColor();
        
        double arc_extent = 0;
        
        if(draw_PCW)
        {
            //------------- test PCW link rendering --------------------------
            
            if(prev_CW == this)
            {
                /* edge is linked to itself, so
                 * draw a sling
                 */
                arc_extent = 360.0;
            }
            else
            {
                //arc_extent = Math.toDegrees(this_edge.angle(prev_CW));
                arc_extent = Math.toDegrees(this.pseudoAngle(prev_CW));
            }
             
            arc.setArcByCenter(center_x, center_y, 
                               radius,
                               this_angle,
                               arc_extent,
                               Arc2D.OPEN);
  
            g2d.setColor(Color.red);
            
            if(_DRAW_PIES)
            {
                g2d.fill(arc);
            }
            else
            {
                g2d.draw(arc);
            }
            
            // test arrow rendering
            _drawArrowOnArc(g2d, arc); 
        }
        
        if(draw_PCCW)
        {
            // ------------- test PCCW link rendering -----------------------
            
            radius += 10.0;
            
            if(prev_CCW == this)
            {
                /* edge is linked to itself, so
                 * draw a sling
                 */
                arc_extent = 360.0;
                //radius += 10.0;
            }
            else
            {
                // normal
                //arc_extent = Math.toDegrees(this_edge.angle(prev_CCW));
                arc_extent = Math.toDegrees(this.pseudoAngle(prev_CCW));                
            }
            
            arc.setArcByCenter(center_x, center_y, 
                               radius,
                               this_angle,
                               arc_extent, // -arc_extent
                               Arc2D.OPEN);
 
            g2d.setColor(Color.blue);
            
            if(_DRAW_PIES)
            {
                g2d.fill(arc);
            }
            else
            {
                g2d.draw(arc);
            }
             
            _drawArrowOnArc(g2d, arc);
        }
        
        center_x = _target.getX();
        center_y = _target.getY();
        
        this_angle = Math.toDegrees(PI + angle());
        
        if(draw_NCW)
        {
            // ------------- test NCW link rendering ------------------------
            
            radius += 10.0;
            
            if(next_CW == this)
            {
                /* edge is linked to itself, so
                 * draw a sling
                 */
                arc_extent = 360.0;
            }
            else
            {
                // normal
                //arc_extent = Math.toDegrees(this_edge.angle(next_CW));
                arc_extent = Math.toDegrees(this.pseudoAngle(next_CW));   
            }
            
            arc.setArcByCenter(center_x, center_y, 
                                radius,
                                this_angle,
                                -arc_extent,
                                Arc2D.OPEN);
 
            g2d.setColor(Color.magenta);
            
            if(_DRAW_PIES)
            {
                g2d.fill(arc);
            }
            else
            {
                g2d.draw(arc);
            }
             
            _drawArrowOnArc(g2d, arc);
        }
        
        if(draw_NCCW)
        {
            // ------------ test NCCW link rendering ------------------------
            
            radius += 10.0;
            
            if(next_CCW == this)
            {
                /* edge is linked to itself, so
                 * draw a sling
                 */
                arc_extent = 360.0;
                //radius += 10.0;
            }
            else
            {
                // normal
                //arc_extent = Math.toDegrees(this_edge.angle(next_CCW));
                arc_extent = Math.toDegrees(this.pseudoAngle(next_CCW));   
            }
            
            arc.setArcByCenter(center_x, center_y, 
                                radius,
                                this_angle,
                                -arc_extent, //-arc_extent,
                                Arc2D.OPEN);
 
           
            g2d.setColor(Color.black);
            
            if(_DRAW_PIES)
            {
                g2d.fill(arc);
            }
            else
            {
                g2d.draw(arc);
            }
             
            _drawArrowOnArc(g2d, arc);
        }
        
        g2d.setColor(prev_color);

    }
    
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************
    
  
    //*************************************************************************
    
    private void _trigVerticesMoved()
    {
        //normalized x-coordinate
        double x = (_target.getX() - 
                    _source.getX()) / this.getLength();
        
        double y = (_target.getY() - 
                    _source.getY()) / this.getLength();
        
        //TODO: maybe introduce epsilon guard for angles close to zero
        
        // for now, measure angles from 12 o'clock
        
        // source angle
        double angle = Math.acos(y);
        
        if( x < 0.0 )
        {
            angle = 2*PI - angle;
        }
        
        _sourceAngle = angle;
        //_targetAngle = _sourceAngle + PI;
        
        
        x*= -1.0; y *= -1.0; 
        angle = Math.acos(y);
      
        if(( x < 0.0 ))
        {
            angle = 2*PI - angle;   
        }
  
        _targetAngle = angle;
        
        
        /* Very important: because the angle is measured
         * clockwise from 3 o'clock, the angle
         * range between [pi, 2pi] lies in quadrants
         * I and IV, hence the conditional y > 0.0 
         * 
         */
        
//        if(( y > 0.0 ) /*|| (Math.abs(y) < DBL_EPSILON)*/)
//        {
//           angle = 2*PI - angle;   
//        }
//        
//        _sourceAngle = angle;
//        
//        // target angle
//        
//        x*= -1.0; y *= -1.0; 
//        angle = Math.acos(x);
//        
//        if(( y > 0.0 ) /*|| (Math.abs(y) < DBL_EPSILON)*/)
//        {
//           angle = 2*PI - angle;   
//        }
//        
//        _targetAngle = angle;
        
    }
    
    //*************************************************************************
    
    private void _drawArrowOnArc(Graphics2D g2d, Arc2D arc)
    {
        // ---- patched code -----
        double this_angle = arc.getAngleStart();
        double angle_extent = arc.getAngleExtent();
        
        double center_x = arc.getCenterX();
        double center_y = arc.getCenterY();
        
        Point2D tip_position = arc.getEndPoint();
        
        //if(angle_extent < 0)
        //  tip_position = arc.getStartPoint();
        
        double radius = 
        Math.sqrt(
        (tip_position.getX() - center_x) * (tip_position.getX() - center_x) + 
        (tip_position.getY() - center_y) * (tip_position.getY() - center_y));
        
        // ----------------------
        
        //calculate arrow tail coordinates
        // angle in radians!
        double arrow_angle = 
            Math.toRadians(this_angle + angle_extent - 
                           ((angle_extent < 0)?-10.0:10.0));
        
         // end coordinates of direction vector
         double end_x = center_x + radius * Math.cos(-arrow_angle);
         double end_y = center_y + radius * Math.sin(-arrow_angle);
         
         // direction vector .... this is silly....
         double dir_x = end_x - center_x;
         double dir_y = end_y - center_y;
         
         // lower tailpoint
         float lower_x = (float)(center_x + 0.95*dir_x);
         float lower_y = (float)(center_y + 0.95*dir_y);

         // upper tailpoint
         float upper_x = (float)(center_x + 1.05*dir_x);
         float upper_y = (float)(center_y + 1.05*dir_y);
         
         // now create the arrow
         GeneralPath arrow = new GeneralPath();
         
         arrow.moveTo((float)tip_position.getX(),
                      (float)tip_position.getY());
         
         arrow.lineTo(lower_x, lower_y);
         arrow.lineTo(upper_x, upper_y);
         arrow.closePath();
         
         g2d.fill(arrow);        
    }
   
}
 
   
