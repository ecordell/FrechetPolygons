/**
 * Embedder for trees based on the algorithm described in the paper 
 *                  
 *                  "Tidier Drawings of Trees"
 *                             by 
 *            Edward M. Reingold and John S. Tilford
 *   IEEE Transactions on Software Engineering Vol 7, No. 2, 1981
 *   
 * extended to handle any trees (m-ary trees).
 * 
 * Nothing but the coordinates of the vertices are changed!
 * 
 * In a preperation step the graph structure is transferred into a more 
 * convenient representation with special fields. (tree-datastructure with nodes)
 * In this preperation cycles are ignored, i.e. edges to already visited 
 * vertices.
 * After that in a setup step the relative positionings of all nodes are 
 * calculated in a postorder traversal and finally the absolute coordinates are 
 * calculated in a preorder traversal.
 *  
 * @author:  Florian Kunze
 * @version: 0.1
 */

package anja.graph.embedder;

import anja.graph.*;
import java.util.HashSet;
//import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;

import static anja.graph.Constants.*;

public class TreeEmbedder extends Embedder
{
  //*************************************************************************
  //                        Private instance variables
  //*************************************************************************
  
  private Vertex _root = null;
  private double _minSep = 10.0;      // minimal horizontal spacing
  private double _verticalSep = 10.0; // minimal vertical spacing
  private HashSet isVisited = new HashSet();
  private Node _tree;                 // temporary storage for the vertices
  private double _x;
  private double _y;
  
  private class Extreme
  {
    Node node;    // address
    int offset;   // offset from root of subtree
    int level;    // level in tree
    
    public String toString()
    {
      if(node != null)
        return "vertex " + node.vertex.getLabel();
      else return "nothing";
    }
  }
  
  private class Node
  {
    Vertex vertex;              // associated vertex 
    Vector link;                // child-nodes
    boolean thread;             // has this node threads?
    int level;
    double offset;              // relative position compared to the parent 
                                //   of the node
    double leftThreadOffset;    // offset of a thread thats left of the 
                                //   threaded node
    double rightThreadOffset;   // dito
    Node leftThread;            // reference to the left thread
    Node rightThread;           // reference to the right thread
    
    Node(Vertex v)
    {
      vertex = v;
      link = new Vector();
      thread = false;
    }
    
    public String toString()
    {
      return "vertex: " + vertex.getLabel();
    }
  }
  
  //*************************************************************************
  //                         Public instance methods
  //*************************************************************************
  
  public void embedGraph(Graph graphInstance)
  {
    // TODO: throw a Exception?!
    if(_root == null) return;
      
    _init(graphInstance.isDirected());
    _setup(_tree, 0, new Extreme(), new Extreme());
    _petrify(_tree, 0.0);
  }
  
  /**
   * set the root of the tree
   * 
   * @param root    vertex which is the root of the tree
   */
  public void setRoot(Vertex root)
  {
    _root = root;
  }
  
  /**
   * returns the root of the tree
   * @return    the root of the tree
   */
  public Vertex getRoot()
  {
    return _root;
  }
  
  /**
   * sets the minimal horizontal spacing between nodes on every level; 
   * defaultvalue is 10.0
   * @param sep  
   */
  public void setHorizontalSpacing(double sep)
  {
    _minSep = sep;
  }
  
  /**
   * get the minimal horizontal spacing
   * @return The horizontal spacing
   */
  public double getHorizontalSpacing()
  {
    return _minSep;
  }
  
  /**
   * sets the vertical spacing between levels; 
   * defaultvalue is 10.0
   * @param sep  
   */
  public void setVerticalSpacing(double sep)
  {
    _verticalSep = sep;
  }
  
  /**
   * Get the vertical spacing
   * @return The vertical spacing
   */
  public double getVerticalSpacing()
  {
    return _verticalSep;
  }
  
  /**
   * set the position where the root is placed
   * @param x x coordinate of the root
   * @param y y coordinate of the root
   */
  public void setRootPosition(double x, double y)
  {
    _x = x;
    _y = y;
  }
  
  /**
   * get the x coordinate of the root placement
   * @return x coord of the root
   */
  public double getX()
  {
    return _x;
  }
  
  /**
   * get the y coordinate of the root placement
   * @return y coord of the root
   */
  public double getY()
  {
    return _x;
  }
  
  //*************************************************************************
  //                        Private instance methods
  //*************************************************************************
  
  /**
   * initalize a more convenient representation of the tree in a preorder 
   * traversal
   * Assumption: Edges are added in counterclockwise order!
   * @param isDirected  is the graphInstance directed?
   */
  private void _init(boolean isDirected)
  {
    isVisited.clear();
    _tree = _initNode(_root, null, isDirected);
  }
  
  private Node _initNode(Vertex v, Vertex parent, boolean isDirected)
  {
    Node newNode = new Node(v);
    
    isVisited.add(v);   // add to already visited list
    
    Iterator<Edge> edgeItr = 
          v.getEdgeIterator(COUNTERCLOCKWISE);
    
    while(edgeItr.hasNext())
    {
      Edge nextEdge = edgeItr.next();
      
      // skip edges in directed graphs that are not starting at v
      if(isDirected && (nextEdge.getSource() != v)) continue;
      // skip edge to parent in undirected/directed graphs
      if(nextEdge.getTarget() == parent) continue;
      
      Vertex nextChildVertex = 
          (nextEdge.getSource() == v)?nextEdge.getTarget():nextEdge.getSource();
      
      // ATTENTION: make the tree acyclic by skiping already visited vertices!
      // -> "Schichtengraph"
      if(isVisited.contains(nextChildVertex)) continue;
      // create childnodes
      Node child = _initNode(nextChildVertex, v, isDirected);
      newNode.link.add(child);
    }
    
    return newNode;
  }
  
  /**
   * setup the subtree with relative positionings during a postorder traversal
   *  
   * @param t       root of the subtree
   * @param level   actual level
   * @param rmost   rightmost extreme decendant
   * @param lmost   leftmost extreme decendant
   */
  private void _setup(Node t, int level, Extreme rmost, Extreme lmost)
  {
    Node left, right;
    
    if(t == null)       // avoid selecting as extreme
    {
      lmost.level = -1;
      rmost.level = -1;
    }
    else
    {
      Iterator vertexItr = t.link.iterator();
      t.level = level;
      
      left = vertexItr.hasNext()?(Node)vertexItr.next():null;
      
      Extreme LR = new Extreme();
      Extreme LL = new Extreme();
      _setup(left, level + 1, LR, LL);
      
      if(t.thread) left = t.leftThread;
      
      /*
       * every adjacent pair of subtrees is placed minimum distance apart and
       * after placing two subtrees the extreme decendants are "merged" and the
       * "left clump" is placed with the next adjacent subtree
       */
      do
      {
        // next adjacent subtree to the left "clump"
        right = vertexItr.hasNext()?(Node)vertexItr.next():null;
                
        if(t.thread) right = t.rightThread;
        
        Extreme RR = new Extreme();
        Extreme RL = new Extreme();
        _setup(right, level + 1, RR, RL);
        if((right == null) && (left == null)) // leaf
        {
          /* 
           * a leaf is both the leftmost and rightmost node of the lowest level
           * of the subtree consisting of itself
           */ 
          rmost.node = t;
          lmost.node = t;
          rmost.level = level;
          lmost.level = level;
          rmost.offset = 0;
          lmost.offset = 0;
          
          t.offset = 0;
        }
        else
        {
          /* 
           * set up for subtree pushing. place roots of subtrees 
           * minimum distance apart.
           */
          double curSep = _minSep;     // seperation at current level
          double rootSep = _minSep;    // speration at the root
          int lOffSum = 0;          // sum of the left subtree/"clump" right 
                                    // contour offsets
          int rOffSum = 0;          // sum over offsets of right subtree contour
          
          
          // now consider each level in turn until one subtree exhausted, 
          // pushing the subtrees apart when necessary
          Node l = left;
          Node r = right;
                    
          while( (l != null) && (r != null) )
          {
            if(curSep < _minSep) // push?
            {
              rootSep = rootSep + (_minSep - curSep);
              curSep = _minSep;
            }
            
            // advance left and right subtrees
            Iterator leftsNodes = l.link.iterator();
            Node leftsLeftSon = leftsNodes.hasNext()?(Node)leftsNodes.next():null;
            Node leftsRightSon = null;
            while(leftsNodes.hasNext()) // the rightest son of lefts sons
              leftsRightSon = (Node)leftsNodes.next(); 
            
            if(l.thread)    // if l is a threaded node, l must be a leaf
            {
              leftsLeftSon = l.leftThread;
              leftsRightSon = l.rightThread;
            }
            

            Iterator rightsNodes = r.link.iterator();
            Node rightsLeftSon =
              rightsNodes.hasNext()?(Node)rightsNodes.next():null;
            Node rightsRightSon = null;
            while(leftsNodes.hasNext()) // the rightest son of rights sons
              rightsRightSon  = (Node)rightsNodes.next();
            
            if(r.thread)  // if r is a threaded node, r must be a leaf
            {
              rightsLeftSon = r.leftThread;
              rightsRightSon = r.rightThread;
            }
            
            if(leftsRightSon != null)
            {
              double offset = (l.thread)?leftsRightSon.rightThreadOffset:
                                      leftsRightSon.offset;
              offset = Math.abs(offset);
              lOffSum += offset;
              curSep -= offset;
              l = leftsRightSon;
            }
            else
            {
              if(leftsLeftSon != null)
              {
                double offset = (l.thread)?leftsLeftSon.leftThreadOffset:
                                        leftsLeftSon.offset;
                offset = Math.abs(offset);
                lOffSum -= offset;
                curSep +=  offset;
              }
              l = leftsLeftSon;
            }

            if(rightsLeftSon != null)
            {
              double offset = (r.thread)?rightsLeftSon.leftThreadOffset:
                                      rightsLeftSon.offset;
              offset = Math.abs(offset);
              rOffSum -= offset;
              curSep -=  offset;
              r = rightsLeftSon;
            }
            else
            {
              if(rightsRightSon != null)
              {
                double offset = (r.thread)?rightsRightSon.rightThreadOffset:
                                        rightsRightSon.offset;
                offset = Math.abs(offset);
                rOffSum += offset;
                curSep +=  offset;
              }
              r = rightsRightSon;
            }
          } // end of push part
          
          /* 
           * update all offsets of child nodes of t,
           * and include it in accumulated offsets for left and right
           */
          double pushOffset = (rootSep+1)/2;
          if(right != null)
            right.offset = left.offset + pushOffset;
          // pushing all nodes on the same level by pushOffset to the left
          for(int i = t.link.indexOf(left); i >= 0; i--)
          {
            Node nextNode = (Node)t.link.get(i);
            nextNode.offset -= pushOffset;
          }
          lOffSum -= pushOffset;
          rOffSum += pushOffset;
                
          /* 
           * update extreme descendants information
           */
          if((RL.level > LL.level) || (left == null))
          {
            lmost.node = RL.node;
            lmost.level = RL.level;
            lmost.offset += pushOffset;
          }
          else
          {
            lmost.node = LL.node;
            lmost.level = LL.level;
            lmost.offset -= pushOffset;
          }
          
          if((LR.level > RR.level) || (right == null))
          {
            rmost.node = LR.node;
            rmost.level = LR.level;
            rmost.offset -= pushOffset;
          }
          else
          {
            rmost.node = RR.node;
            rmost.level = RR.level;
            rmost.offset += pushOffset;
          } // end of extreme decendant update part
          
          /* 
           * if subtrees of t were of uneven heights, check to see if threading
           * is nescessary. at most one thread needs to be inserted.
           */
          if((l != null) && (l != left))
          {
            RR.node.thread = true;
            double offset = Math.abs((RR.offset + pushOffset) - lOffSum);
            if((lOffSum - pushOffset) <= Math.abs(RR.offset))
            {
              RR.node.leftThread = l;
              l.leftThreadOffset = offset;
            }
            else
            {
              RR.node.rightThread = l;
              l.rightThreadOffset = offset;
            }
          }
          else if((r != null) && (r != right))
          {
            LL.node.thread = true;
            double offset = Math.abs((LL.offset - pushOffset) - rOffSum);
            if((rOffSum + pushOffset) >= LL.offset)
            {
              LL.node.rightThread = r;
              r.rightThreadOffset = offset;
            }
            else
            {
              LL.node.leftThread = r;
              r.leftThreadOffset = offset;
            }
          } // end of thread creation part
        }
        
        // preparations for the next subtree scan and
        // merge of the extreme decendant information of the both subtrees
        LR.node = rmost.node;
        LR.level = rmost.level;
        LR.offset = rmost.offset;
        LL.node = lmost.node;
        LL.level = lmost.level;
        LL.offset = lmost.offset;
        left = right;
      } 
      while(vertexItr.hasNext());
      
    }
    
  }
  
  /**
   * performs a preorder traversal of the tree and converts the relative offsets
   * to absolute coordinates
   * @param t
   * @param xpos
   */
  private void _petrify(Node t, double xpos)
  {
    if(t != null)
    {
      t.vertex.moveTo(_x + xpos + t.offset, -_verticalSep*t.level + _y);
      Iterator vertexItr = t.link.iterator();
      while(vertexItr.hasNext())
      {
        Node nextNode = (Node)vertexItr.next();
        _petrify(nextNode, xpos + t.offset);
      }
    }
  }
  
}
