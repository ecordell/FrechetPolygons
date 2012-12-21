/*
 * Created on Oct 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package anja.SwingFramework.tests;


import java.awt.*;
import java.awt.geom.*;
import java.util.*;

//import anja.geom.Point2;
import anja.graph.*;

import anja.SwingFramework.*;
import anja.SwingFramework.graph.*;

public class ScrapGraphScene extends JGraphScene
{
    //*************************************************************************
    //                             Public constants
    //*************************************************************************
    
    //*************************************************************************
    //                             Private constants
    //*************************************************************************
    
    private static final int            _NUM_VERTICES  = 10;
    private static final int            _NUM_EDGES     = 10;
    private static final double         _GRAPH_EXTENTS = 300;
    
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
    
    //private Font _baseFont;
    
    //private Font _flippedFont;          // derived font
    //private Font _flippedInvScaleFont;  // inverse-scaled font
    
    //private AffineTransform _flipTransform;     // y-axis mirroring
    //private AffineTransform _flipInvTransform; // flip and inv.scaling
    
    
    // test primitives
    private Vertex _vertex1, _vertex2, _vertex3;
    private Edge   _edge1, _edge2;
    
    private Graph  _graph;
    
    //*************************************************************************
    //                              Constructors
    //*************************************************************************
    
    public ScrapGraphScene(JSystemHub hub)
    {
        super(hub);
        
        /*
        _baseFont = new Font("Arial", Font.PLAIN, 12);
        
        _flipTransform = new AffineTransform(1.0, 0.0, 
                                             0.0, -1.0, 
                                             0.0, 0.0);
        
        _flipInvTransform = new AffineTransform();
        
        // create flipped font
        _flippedFont = _baseFont.deriveFont(_flipTransform);*/
        
        //init vertices
        Vertex.enableProxyZones(true);
        
        ///////////////////// position - label -- vertex color - text color
        
        //_vertex1 = new Vertex(100, 100, "one, #1", Color.red, Color.yellow);
        //_vertex1 = new Vertex(100, 100, "one");
        
        //_vertex1.setLabel("one, #1");
        //_vertex1.setColor(Color.red);
        //_vertex1.setTextColor(Color.yellow);
        
        //vertex1.setRadius(15);
        
        //_vertex2 = new Vertex(-50, 50, "two, #2", Color.green, Color.black);
        //_vertex2 = new Vertex(-50, 50, "two");
        
        //_vertex2.setLabel("two, #2");
        //_vertex2.setColor(Color.green);
        //_vertex2.setTextColor(Color.black);
        
        //vertex2.setRadius(15);
        
        //_vertex3 = new Vertex(-150, -100, "three", Color.yellow);
        //_vertex3 = new Vertex(-150, -100, "three");
        
        // init edges
        //_edge1 = new Edge(_vertex1, _vertex2);
        //_edge2 = new Edge(_vertex2, _vertex3);
        
        //edge1.setColor(Color.blue);
        // try edge emphasis
        
        //REMINDER: float width, int cap, int join, float 
        //miterlimit, float[] dash, float dash_phase
        
        /*
        float[] dash = new float[]{15.0f, 5.0f, 1.0f, 5.0f};
        
        BasicStroke emph_stroke 
        = new BasicStroke(5.0f, BasicStroke.CAP_BUTT,
                          BasicStroke.JOIN_BEVEL,
                          10.0f,
                          dash,
                          0.0f);
        
        //_edge1.setEmphasis(Color.BLACK, emph_stroke); */
        
        // graph tests
        
                          // structure dir    e.w.   v.w    embedded
                          //            |      |      |      |   
        //_graph = new Graph(Graph.DCEL, true, false, false, true);
        
        /*
        _graph.addVertex(_vertex1);
        _graph.addVertex(_vertex2);
        
        _graph.addEdge(_edge1);
        
        _graph.addVertex(_vertex3);
        _graph.addEdge(_edge2);*/
        
        //_createRandomGraph();
        
        //_createRegularStar(3);
        //_createRegularNGon(3,2);
        //_createHollowNGon(5);
            
        //_graph.listFubarStuff();
    }
    
    //*************************************************************************
    //                         Public instance methods
    //*************************************************************************
    
    public void updateAffineTransform(AffineTransform tx, 
                                      double pixelSize, 
                                      Font invertedFont)
    {
        super.updateAffineTransform(tx, pixelSize, invertedFont);
        
        /*
        // create flipped and inverse scaled font
        _flipInvTransform.setTransform(1.0 / tx.getScaleX(), 0.0,
                                       0.0,  1.0 / tx.getScaleY(), 
                                       0.0, 0.0);
        
        _flippedInvScaleFont = _baseFont.deriveFont(_flipInvTransform);*/
        
        /* REMINDER : the pixel size calculated is the actual 
         * value due to world-to-screen mapping, so for rendering,
         * the inverse of this must be used
         */
        /*
        Point2 point0 = new Point2();
        Point2 point1 = new Point2();

        tx.transform( new Point2( 0, 0 ), point0 );
        tx.transform( new Point2( 1, 0 ), point1 );
 
        m_pixelSize = point1.x - point0.x;*/
        
    }
    
    public void draw(Graphics2D g)
    {
        super.draw(g);
        
        
        
        m_Graph.drawDebug(g, 1.0 / _pixelSize, 
                          _invertedFont, 
                          _hub.getCoordinateSystem().getWorldBoundingBox()
                          /*viewport*/); 
        
        /*
        //_edge1.setDirected(true);
        //_edge2.setDirected(true);
        
        _graph.draw(g, 1.0 / m_dPixelSize, m_invertedFont);
        
        // ----- wrapper code ----------------------------
        Rectangle2D viewport = new Rectangle2D.Double();
        
        viewport.setRect(m_Hub.getDisplayPanel().getX(),
                         m_Hub.getDisplayPanel().getY(),
                         m_Hub.getDisplayPanel().getWidth(),
                         m_Hub.getDisplayPanel().getHeight());       
        // -----------------------------------------------
        
       
        
        // test rendering with cloned rendering contexts
        //Graphics2D gg = (Graphics2D)g.create();
        //gg.setFont(_flippedInvScaleFont);
        
        // test normal text rendering
        //g.drawString("TEST STRING", 50.0f, 25.0f);
        
        // test flipped and inv. scaled font rendering
        //gg.drawString("TEST STRING", 50.0f, 25.0f);        
        
        // test newgraph element rendering
        
        //_vertex1.highlightProxyZone();
        //_vertex2.highlightProxyZone();
        
        //_edge2.setColor(Color.red);
        
        //_edge1.select();
        //_edge1.emphasize(); */
        
        // use default colors for now

        /*
        _edge1.draw(g, 1.0 / m_pixelSize, _flippedInvScaleFont);
        _edge2.draw(g, 1.0 / m_pixelSize, _flippedInvScaleFont);
        
        _vertex1.draw(g, 1.0 / m_pixelSize, _flippedInvScaleFont);
        _vertex2.draw(g, 1.0 / m_pixelSize, _flippedInvScaleFont);
        _vertex3.draw(g, 1.0 / m_pixelSize, _flippedInvScaleFont); */
               
        //vx.highlightProxyZone();
        //vx.select();        
        //vx.setProxyRadius(30);
       
        // test flipped font rendering
        //Font ff = g.getFont();
        
        //g.setFont(_flippedFont);
        //g.drawString("TEST STRING", 50.0f, 50.0f);        
        
        // test flipped and inv. scaled font rendering
        //g.setFont(_flippedInvScaleFont);
        //g.drawString("TEST STRING", 50.0f, 100.0f);        
        
        //g.setFont(ff);
    }
    
    //*************************************************************************
    //                       Protected instance methods
    //*************************************************************************
    
    protected void _createRandomGraph()
    {
        // generate a number of randomly placed vertices 
        
        double x,y;
        Random rnd = new Random(System.currentTimeMillis());
     
        Vertex new_vertex, prev_vertex;
          
        x = (2 * rnd.nextDouble() - 1.0) * _GRAPH_EXTENTS;
        y = (2 * rnd.nextDouble() - 1.0) * _GRAPH_EXTENTS;
        
        new_vertex = _graph.addVertex(x,y,"--"+String.valueOf(0)+"--");
        prev_vertex = new_vertex;
        
        for(int i = 1; i < _NUM_VERTICES; i++)
        {
            do
            {
                x = (2 * rnd.nextDouble() - 1.0) * _GRAPH_EXTENTS;
                y = (2 * rnd.nextDouble() - 1.0) * _GRAPH_EXTENTS;
            }
            while(prev_vertex.distance(x,y) < 30);
            
            new_vertex = _graph.addVertex(x,y,"--"+String.valueOf(i)+"--");
            prev_vertex = new_vertex;
            
        }
        
        
    }
    
    
    protected void _createHollowNGon(int numSides)
    {
        double delta_phi = 2 * Math.PI / numSides;
        double x,y;
        double radius  = 300;
        
        Vertex center_vertex, first_vertex, prev_vertex, next_vertex;
        Edge next_edge;
        
        center_vertex = _graph.addVertex(0.0, 0.0, "");
        
        for(int i = 0; i < numSides; i++)
        {
            x = radius * Math.cos(i * delta_phi);
            y = radius * Math.sin(i * delta_phi);
            
            next_vertex = _graph.addVertex(x,y, String.valueOf(i+1));
                   
        }
        
        // link center to other vertices with directed edges
        
        // now create n-gon side edges
        Iterator<Vertex> vertex_it = _graph.getAllVertices();
        
        // skip center vertex
        vertex_it.next();
       
        first_vertex = vertex_it.next();
        prev_vertex = first_vertex;
        
        while(vertex_it.hasNext())
        {
            next_vertex = vertex_it.next();
            
            next_edge = _graph.addEdge(prev_vertex, next_vertex);
            next_edge.setDirected(true);
            prev_vertex = next_vertex;
        }
      
        // last link to complete the n-gon
        next_edge = _graph.addEdge(prev_vertex, first_vertex);
        next_edge.setDirected(true);
       
    }
    
    protected void _createRegularNGon(int numVertices, int numSides)
    {
        _createRegularStar(numVertices);
        
        Vertex center_vertex, first_vertex, prev_vertex, next_vertex;
        Edge next_edge;
        
       
        // now create n-gon side edges
        Iterator<Vertex> vertex_it = _graph.getAllVertices();
        
        // skip center vertex
        vertex_it.next();
       
        first_vertex = vertex_it.next();
        prev_vertex = first_vertex;
        
        int counter = 0;
        while((vertex_it.hasNext()) && (counter < numSides))
        {
            next_vertex = vertex_it.next();
            
            next_edge = _graph.addEdge(prev_vertex, next_vertex, true);
            prev_vertex = next_vertex;
            counter++;
        }
     
        if(numVertices == numSides)
        {
            // last link to complete the n-gon
            next_edge = _graph.addEdge(prev_vertex, first_vertex, true);
        }
        
        //Vertex outsider = new Vertex(400, 100, "");
        //Edge ee = new Edge(outsider, first_vertex, true);
        //_graph.addEdge(ee);
    }
    
    protected void _createRegularStar(int numSides)
    {
        double delta_phi = 2 * Math.PI / numSides;
        double x,y;
        double radius  = 300;
        
        Vertex center_vertex, first_vertex, prev_vertex, next_vertex;
        Edge next_edge;
        
        center_vertex = _graph.addVertex(0.0, 0.0, "");
        
        for(int i = 0; i < numSides; i++)
        {
            x = radius * Math.cos(i * delta_phi);
            y = radius * Math.sin(i * delta_phi);
            
            next_vertex = _graph.addVertex(x,y, String.valueOf(i+1));
            
            // link center to other vertices with directed edges
            
            if(i % 2 == 0)
            {
                next_edge = _graph.addEdge(center_vertex, next_vertex, true);
            }
            else
            {
                next_edge = _graph.addEdge(next_vertex, center_vertex, true);
            }
            
        }
        
    }
    
    //*************************************************************************
    //                        Private instance methods
    //*************************************************************************
}



