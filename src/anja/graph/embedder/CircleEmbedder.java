package anja.graph.embedder;

//import java.util.Vector;
import anja.graph.*;
import java.util.Iterator;

/**
* Implementation of the most simple Embedder which positions all vertices 
* on a circle.
* 
* @author  Florian Kunze
* @version 0.1
*/


public class CircleEmbedder extends Embedder
{
  private double _x;
  private double _y;
  private double _spaceing = 40.0;
  
  /**
   * set the midpoint of the circle on which the vertices are placed
   * @param x
   * @param y
   */
  public void setMidpoint(double x, double y)
  {
    _x = x;
    _y = y;
  }
  
  public double getX()
  {
    return _x;
  }
  
  public double getY()
  {
    return _y;
  }
  
  /**
   * set the spacing between two adjacent vertices on the circle
   * default: 40.0
   * @param space
   */
  public void setSpacing(double space)
  {
    _spaceing = space;
  }
  
  public void embedGraph(Graph graphInstance)
  {
    graphInstance.setEmbedded(true);
    Iterator<Vertex> it = graphInstance.getAllVertices();
    
    
    int numOfVertices = graphInstance.getNumVertices();
    // calculating the needed arc length and the radius of the circle...
    double radius = 0;
    double angle = 0;
    
    if(numOfVertices > 1)
    {
      radius = numOfVertices*_spaceing/(2*Math.PI);
      angle = 2*Math.PI/numOfVertices;
    }
      
    // getting all vertices
    for(int i = 0; it.hasNext(); i++)
    {
      Vertex v = it.next();
      v.moveTo(_x + radius*Math.cos(angle*i), _y + radius*Math.sin(angle*i));
      v.setRadius(10);
    } 
  }
}
