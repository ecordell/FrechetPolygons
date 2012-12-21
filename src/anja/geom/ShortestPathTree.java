package anja.geom;

import java.awt.Graphics2D;
import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;

import anja.util.*;

/**
 * Simple brute-force implementation of a shortest path tree.
 * 
 * <br>Currently the algorithm is as fast as the implementation of DualGraph,
 * which is important for the usage of this class. If DualGraph is implemented
 * in another way one day, the method of calculating the SPT can be changed to
 * the usage of DualGraph. Before that, there is improvement.
 *
 * <br>Instead of using the SPTSimpleTree, which only provides child->parent
 * usage, the modified class implements the getShortestPathTree() method,
 * which rturns the root of the SPT and offers an easy way to navigate through
 * the child nodes of each node and back to the parent. Each SPTNode is a Point2.
 * 
 * <br>The method drawSubtree() draws a subtree beginning with the node where it was called.
 * 
 * <br>When chaging the class to the usage with DualGraph, the new data structure should
 * fill the SPTNodes objects during the creation of the tree, which reduces the calculation
 * time.
 *
 * @version 1.0 12.10.08, Oktober 2010
 * @author Elitsa Dimitrova
 * @author Borislav Manolov
 * @author Andreas Lenerz
 */
public class ShortestPathTree
	implements anja.util.Drawable, java.io.Serializable
{
	
	// *************************************************************************
	// Inner classes
	// *************************************************************************

	/**
	 * Very simple drawable tree
	 *
	 * @version 1.0 12.10.08
	 * @author Elitsa Dimitrova
	 * @author Borislav Manolov
	 */
	class SPTSimpleTree<P extends Point2>
		implements anja.util.Drawable
	{
	
		private static final int DEFAULT_SIZE = 10;
		private HashMap<P,P> _table;
		private ArrayList<P> _innerNodes;
		private P _root;
	
	
		/**
		 * Sets up a tre with default size.
		 */
		public SPTSimpleTree()
		{
			this(DEFAULT_SIZE);
		}
	
	
		/**
		 * Sets up a tree with given size
		 * @param size A size for the tree
		 */
		public SPTSimpleTree(int size)
		{
			_table = new HashMap<P,P>(size);
			_innerNodes = new ArrayList<P>();
		}
	
	
		/**
		 * Adds a root node.
		 * @param root
		 */
		public void addRoot(P root)
		{
			_root = root;
			add(root, null);
		}
	
	
		/**
		 * Adds a child node for given parent node.
		 * @param child
		 * @param parent
		 */
		public void add(P child, P parent)
		{
			_table.put(child, parent);
			if ( parent != null && ! _innerNodes.contains(parent) ) {
				_innerNodes.add(parent);
			}
		}
	
	
		/**
		 * Returns the root node
		 * @return The root node
		 */
		public P getRoot()
		{
			return _root;
		}
		
		
		/**
		 * Returns the hashmap, that includes the child->parent relations
		 * 
		 * @return The hashmap
		 */
		public HashMap<P,P> getPointTable()
		{
			return _table;
		}
	
	
		/**
		 * Returns the parent for a given node
		 * @param node A node from the tree
		 * @return The parent of the given node
		 */
		public P getParent(P node)
		{
			return _table.get(node);
		}
	
	
		/**
		 * Returns the path to the root node from a given node
		 * @param node A node from the tree
		 * @return The path to the root node
		 */
		public SimpleList getPathToRoot(P node)
		{
			SimpleList path = new SimpleList();
			path.add(node);
			while ( ( node = getParent(node) ) != null ) {
				path.add(node);
			}
			return path;
		}
	
	
		/**
		 * Returns the inner nodes of the tree
		 * @return The inner nodes ot the tree
		 */
		public ArrayList<P> getInnerNodes()
		{
			return _innerNodes;
		}
	
	
		/**
		 * Tells whether the tree is empty
		 * @return True if the tree is empty, false otherwise
		 */
		public boolean isEmpty()
		{
			return _table.isEmpty();
		}
	
	
		/**
		 * Checks if the tree contains a given node
		 * @param node A node
		 * @return True if the tree contains the given node
		 */
		public boolean hasNode(Point2 node)
		{
			return _table.containsKey(node);
		}
	
	
		/**
		 * Returns the size of the tree measured in tree nodes
		 * @return Size of the tree
		 */
		public int size()
		{
			return _table.size();
		}
	
	
		/**
		 * Draws the tree
		 * @param g A graphics element
		 * @param gc A graphics context
		 */
		public void draw(Graphics2D g, GraphicsContext gc)
		{
			if ( _table.isEmpty() ) {
				return;
			}
			//System.out.println("Start tree\n==============");
			for ( Map.Entry<P,P> entry : _table.entrySet() ) {
				P child = entry.getKey();
				if ( child.equals( _root ) ) {
					_root.draw(g, gc);
					continue;
				}
				//System.out.println(child + " : " + entry.getValue());
				new Segment2(child, entry.getValue()).draw(g, gc);
			}
		}
	
	
		/**
		 * Prints the tree
		 */
		public void print()
		{
			if ( _table.isEmpty() ) {
				return;
			}
			System.out.println("Start tree\n==============");
			for ( Map.Entry<P,P> entry : _table.entrySet() ) {
				P child = entry.getKey();
				if ( child.equals( _root ) ) {
					System.out.println(_root);
					continue;
				}
				System.out.println(child + " : " + entry.getValue());
			}
		}
	
	
		/**
		 * Not implemented
		 */
		public boolean intersects(java.awt.geom.Rectangle2D box)
		{
			return false;
		}
	}
	
	
	/**
	 * Offers the SPT in another way. Instead of going from
	 * child to parent, this structure offers the possibility
	 * to go forward and backward beginning with the root.
	 * For the easier use, we offer the two variables as public
	 * 
	 * @author Andreas Lenerz
	 */
	public class SPTNode 
		extends Point2
		implements Comparable<SPTNode>
	{
		public SPTNode parent = null;
		public SPTNode[] child = null;
		public int id = -1;
		
		
		// *************************************************************************
		// Constructor
		// *************************************************************************
		
		
		/**
		 * Default constructor.
		 */
		public SPTNode()
		{
			super();
		}
		
		
		/**
		 * Constructor for Point2 objects. Calls super(p)
		 * 
		 * @param p The base for this object
		 */
		public SPTNode(Point2 p)
		{
			super(p);
			this.setValue(p.getValue());
		}
		
		
		/**
		 * Constructor which makes a cop of the point, but uses references
		 * ono the parents and childs
		 * 
		 * @param p The node to copy
		 */
		public SPTNode(SPTNode p)
		{
			super(p);
			this.setValue(p.getValue());
			parent = p.parent;
			child = p.child;
			id = p.id;
		}
		
		
		// *************************************************************************
		// Public fields
		// *************************************************************************

		
		/**
		 * Setter for the child SPTNode object
		 * <br>The Setter does not copy the object, but save a reference
		 * 
		 * @param child The child SPTNode or null if leaf
		 * @param n The number of the node
		 */
		public void setChild(int n, SPTNode child)
		{
			this.child[n] = child;
		}
		
		
		/**
		 * Set all childs at one time
		 * 
		 * @param childs Converts all point objects to new childs
		 */
		public void setChilds(Point2[] childs)
		{
			child = new SPTNode[childs.length];
			
			for (int i=0; i < childs.length; ++i)
			{
				child[i] = new SPTNode(childs[i]);
			}
		}
		
		
		/**
		 * Set all childs at one time (reference)
		 * 
		 * @param childs The children of the node
		 */
		public void setChilds(SPTNode[] childs)
		{
			child = childs;
		}
		
		
		/**
		 * Setter for the prev SPTNode object
		 * <br>The Setter does not copy the object, but save a reference
		 * 
		 * @param parent The parent of this object or null, if root
		 */
		public void setParent(SPTNode parent)
		{
			this.parent = parent;
		}
		
		
		/**
		 * Setter for the id  of the object
		 * 
		 * @param id The ID
		 */
		public void setID(int id)
		{
			this.id = id;
		}
		
		
		
		/**
		 * Initializes the array of childs with size n
		 * 
		 * @param n The new size of the array
		 */
		public void setNumberOfChilds(int n)
		{
			this.child = new SPTNode[n];
		}
		
		
		/**
		 * Getter for the parent of this node
		 * 
		 * @return The object of the parent or null if root
		 */
		public SPTNode getParent()
		{
			return parent;
		}
		
		
		/**
		 * Getter for the child n of this node
		 * 
		 * @return The object of the child with index n or null if leaf
		 */
		public SPTNode getChild(int n)
		{
			return child[n];
		}
		
		
		/**
		 * Getter for the whole array of childs
		 * 
		 * @return The array with all childs
		 */
		public SPTNode[] getChilds()
		{
			return child;
		}
		
		
		/**
		 * Getter for the ID of the object
		 * 
		 * @return The ID
		 */
		public int getID()
		{
			return id;
		}
		
		
		/**
		 * The number of childs of this node. Not all of the childs
		 * have to be initialized. The method returns the size of the array
		 * 
		 * @return The number of children of this node
		 */
		public int getNumberOfChilds()
		{
			if (child == null)
				return 0;
			return child.length;
		}
		
		
		/**
		 * Is the node a leaf or not
		 * 
		 * @return true if there are no childs, false else
		 */
		public boolean isLeaf()
		{
			return child == null;
		}
		
		
		/**
		 * Is this node the root or not
		 * 
		 * @return true if there is no parent, false else
		 */
		public boolean isRoot()
		{
			return parent == null;
		}
		
		
		/**
		 * DFS on a subtree with root 'root' to find the point p
		 * 
		 * @param root The root of the subtree
		 * @param p The point to find
		 * 
		 * @return The SPTNode of p or null
		 */
		public SPTNode findNode(SPTNode root, Point2 p)
		{
			if (root.equals(p))
				return root;
			
			SPTNode result = null;
			for (int i=0; i < root.getChilds().length && result == null; ++i)
			{
				result = findNode(root.getChild(i), p);
			}
			
			return result;
		}
	
		
		/**
		 * Draws the subtree of this node into the graphics object.
		 * 
		 * @param g2d The graphics object to draw in
		 * @param gc The graphics context. If null a new object is created
		 */
		public void drawSubtree(Graphics2D g2d, GraphicsContext gc)
		{
			if (child != null)
			{				
				for (int i=0; i < child.length; ++i)
				{
					if (child[i] != null)
					{
						(new Segment2(this, child[i])).draw(g2d, gc);
						child[i].drawSubtree(g2d, gc);
					}
				}
			}
		}
		
		
		/**
		 * Prints the subtree of this node into the given stream object.
		 * 
		 * <br>For usage on the screen, use System.out as PrintStream.
		 * 
		 * @param ps The PrintStream the output should be written to
		 */
		public void printSubtree(PrintStream ps)
		{
			ps.println("<"+id+", ("+this.x+", "+this.y+") >");
			if (child != null)
			{				
				for (int i=0; i < child.length; ++i)
				{
					if (child[i] != null)
					{
						child[i].printSubtree(ps);
					}
				}
			}
		}


		@Override
		public int compareTo(
				SPTNode o)
		{
			if (this.getValue() < o.getValue()) return -1;
			if (this.getValue() > o.getValue()) return 1;			
			return 0;
		}


		
		
	} //end SPTNode
		
	
	// *************************************************************************
	// Private fields
	// *************************************************************************

	private Polygon2 _poly;
	private SPTSimpleTree<Point2> _tree;
	private ConvexPolygon2[] _tri;
	private Vector<ConvexPolygon2> _triDone;
	private Vector<Point2> _vertDone;
	private HashMap<Segment2, Vector<ConvexPolygon2>> _diagTriMap;

	// *************************************************************************
	// Public methods
	// *************************************************************************

	/**
	 * Sets up a tree for given polygon and point
	 * @param start A source point
	 * @param poly A simple polygon
	 */
	public ShortestPathTree(Point2 start, Polygon2 poly)
	{
		_computeTreeBad(start, poly);
	}


	/**
	* Draw the shortest path tree.
	 * @param g A graphics element
	 * @param gc A graphics context
	*/
	public void draw(Graphics2D g, GraphicsContext gc)
	{
		_tree.draw(g, gc);
	}

	/**
	 *
	 * @param box A box
	 * @return True if the tree intersects the given box
	 */
	public boolean intersects(java.awt.geom.Rectangle2D box)
	{
		return _tree.intersects(box);
	}


	/**
	 * Prints the tree
	 */
	public void print()
	{
		_tree.print();
	}
	
	
	/**
	 * Returns the root (SPTNode) of the ShortestPathTree.
	 * 
	 * <br> The method sorts all childs, beginning depth==2 depending
	 * on the angle to the parent and the other childs. Therefor
	 * the childs can be used in a certain order.
	 * The childs of the root node are sorted in another way, but all the points
	 * follow the same order than the childs later do.
	 * 
	 * @return The root node.
	 */
	public SPTNode getShortestPathTree()
	{
		SPTNode root = new SPTNode(_tree.getRoot());
		
		HashMap<Point2,Point2> table = _tree.getPointTable();
		Point2[][] p = new Point2[2][table.entrySet().size()];
		Iterator<Entry<Point2,Point2>> it = table.entrySet().iterator();		
		
		int i=0;
		while (it.hasNext())
		{
			Entry<Point2,Point2> p_tmp = it.next();
			p[0][i] = p_tmp.getKey();
			p[1][i] = p_tmp.getValue();
			i++;
		}
		
		_initialize(root, p, true);
		
		return root;
	}


	// *************************************************************************
	// Private methods
	// *************************************************************************


	/**
	 * At this point, the triangulation can't be sorted in any way in linear time.
	 * Therefor all classes using the triangulation have to order the triangles
	 * before using them. This class should take linear time, but isn't be able to implemented
	 * in such time with the current triangulation.
	 * 
	 * <br>The building of a shortest path tree should be improved, when a new
	 * class for triangulation is available.
	 *
	 * @param start A source point
	 * @param poly A simple polygon
	 */
	//TODO Improve, when triangulation supports linear time building of the tree
	private void _computeTreeBad(Point2 start, Polygon2 poly)
	{
		_poly = new Polygon2(poly);
		ConvexPolygon2[] triangs = _poly.getTriangulation();
		DualGraph g = new DualGraph(triangs);
		SimpleList vertices = poly.points();
		_tree = new SPTSimpleTree<Point2>( vertices.length()*2 );
		_tree.addRoot(start);
		ListItem it = null;
		while ( (it = vertices.next(it)) != null ) {
			Point2 p = (Point2) it.value();
			if ( ! start.equals(p) ) {
				Segment2[] path = _poly.getShortestPath(start, p, g, triangs);
				//System.out.println("	Path to " + p);
				Point2 curParent = start;
				for ( int i = 0; i < path.length; i++ ) {
					//System.out.println("Segment: " + path[i]);
					Point2 left = path[i].getLeftPoint();
					Point2 right = path[i].getRightPoint();
					if ( left.equals(curParent) ) {
						if ( ! _tree.hasNode(right) ) {
							//System.out.println("A. Adding " + right +" <- "+ curParent);
							_tree.add( right, curParent );
						}
						//System.out.println("A. CurParent = " + right);
						curParent = right;
					} else {
						if ( ! _tree.hasNode(left) ) {
							//System.out.println("B. Adding " + left +" <- "+ curParent);
							_tree.add( left, curParent );
						}
						//System.out.println("B. CurParent = " + left);
						curParent = left;
					}
				}
			}
		}
	}


	/**
	 *
	 * @param start A source point
	 * @param poly A simple polygon
	 */
	private void _computeTree(Point2 start, Polygon2 poly)
	{
		_initTree(poly);
		// TODO make it work
		//_initDiagTriangleMap();
		_addPathsInStartTriangle(start);
	}


	/**
	 *
	 * @param poly A simple polygon
	 *
	 */
	private void _initTree(Polygon2 poly)
	{
		int pointCount = poly.points().length();
		_tri = poly.getTriangulation();
		_tree = new SPTSimpleTree<Point2>(pointCount);
		_triDone = new Vector<ConvexPolygon2>( _tri.length );
		_vertDone = new Vector<Point2>(pointCount);
	}


	/**
	 *
	 *
	 */
	private void _initDiagTriangleMap()
	{
		_diagTriMap = new HashMap<Segment2, Vector<ConvexPolygon2>>();
		for ( int i = 0; i < _tri.length; i++ ) {
			Segment2[] edges = _tri[i].edges();
			for ( int j = 0; j < edges.length; j++ ) {
				Segment2 edge = edges[j];
				if ( _diagTriMap.containsKey(edge) ) {
					_diagTriMap.get(edge).add( _tri[i] );
				} else {
					_diagTriMap.put(edge, new Vector<ConvexPolygon2>(1));
				}
			}
		}
	}


	/**
	 *
	 * @param start A source point
	 *
	 */
	private void _addPathsInStartTriangle(Point2 start)
	{
		ConvexPolygon2 startTri = _locateStartTriangle(start);
		SimpleList vertices = startTri.points();
		ListItem it = null;
		while ( (it = vertices.next(it)) != null ) {
			Point2 p = (Point2) it.value();
			if ( ! start.equals(p) ) {
				_addToTree(p, start);
			}
		}
		_traverseTriangle(start, startTri);
	}


	/**
	 *
	 * @param start A source point
	 * @return A triangle containing a source point if the source point is inner
	 * for the polygon else a suitable message
	 */
	private ConvexPolygon2 _locateStartTriangle(Point2 start)
	{
		for ( int i = 0; i < _tri.length; i++ ) {
			if ( _tri[i].contains(start) ) {
				return _tri[i];
			}
		}
		System.out.println(start + " is out of the polygon");
		return null;
	}


	/**
	 *
	 * @param start A source point
	 * @param tri A triangle
	 *
	 */
	private void _traverseTriangle(Point2 start, ConvexPolygon2 tri)
	{
		_triDone.add(tri);
		Segment2[] edges = tri.edges();
		for ( int i = 0; i < edges.length; i++ ) {
			//Vector edgeTrias = _diagTriMap.get( edges[i] );
			Vector<ConvexPolygon2> edgeTrias = _getTrianglesByEdge( edges[i] );
			Iterator<ConvexPolygon2> it = edgeTrias.iterator();
			while ( it.hasNext() ) {
				ConvexPolygon2 edgeTria = it.next();
				if ( _isProcessedTriangle(edgeTria) ) {
					continue;
				}
				_addPathsInTriangle(start, edgeTria, edges[i]);
			}
		}
	}


	/**
	 *
	 * @param edge A segment that is edge of a polygon
	 * @return The triangles that contains an given edge
	 */
	private Vector<ConvexPolygon2> _getTrianglesByEdge(Segment2 edge)
	{
		Point2 l = edge.getLeftPoint();
		Point2 r = edge.getRightPoint();
		Vector<ConvexPolygon2> v = new Vector<ConvexPolygon2>(1);
		for ( int i = 0; i < _tri.length; i++ ) {
			if ( _tri[i].inTriangle(l, true) && _tri[i].inTriangle(r, true) ) {
				v.add( _tri[i] );
			}
		}
		return v;
	}


	/**
	 *
	 * @param start A source point
	 * @param tri A triangle
	 * @param oldEdge An edge of triangle?
	 *
	 */
	private void _addPathsInTriangle(
		Point2 start,
		ConvexPolygon2 tri,
		Segment2 oldEdge)
	{
		System.out.println("Traversing " + tri);
		Point2 va = oldEdge.getLeftPoint();
		Point2 vb = oldEdge.getRightPoint();
		Point2 vc = _getThirdTriangleVertex(tri, va, vb);
		if ( start.equals(va) ) {
			System.out.println("I. " + start + " == " + va);
			_addToTree(vb, start);
			_addToTree(vc, start);
		} else if ( start.equals(vb) ) {
			System.out.println("II. " + start + " == " + vb);
			_addToTree(va, start);
			_addToTree(vc, start);
		} else if ( computeInnerAngle(start, va, vb, vc) >= Math.PI ) {
			System.out.println("III. " + start + " := " + va);
			_addToTree(va, start);
			_addToTree(vc, start);
			start = va;
		} else if ( computeInnerAngle(start, vb, va, vc) >= Math.PI ) {
			System.out.println("IV. " + start + " := " + vb);
			_addToTree(vb, start);
			_addToTree(vc, start);
			start = vb;
		} else {
			System.out.println("V. " + start + " with " + vc);
			_addToTree(vc, start);
		}
		_traverseTriangle(start, tri);
	}


	/**
	 *
	 * @param child A point to be added to a tree if it is not yet precessed
	 * @param parent A point that is a parent for the child point
	 *
	 */
	private void _addToTree(Point2 child, Point2 parent)
	{
		if ( ! _isProcessedVertex(child) ) {
			_tree.add(child, parent);
			System.out.println("PATH: " + parent + " --- " + child);
			_vertDone.add(child);
		}
	}


	/**
	 *
	 * @param v A vertex to be checked if it is processed
	 * @return True if a point is not yet processed else false
	 *
	 */
	private boolean _isProcessedVertex(Point2 v)
	{
		Iterator<Point2> it = _vertDone.iterator();
		while ( it.hasNext() ) {
			if ( v.equals( it.next() ) ) {
				return true;
			}
		}
		return false;
	}


	/**
	 *
	 * @param t A triangle to be checked if it is processed
	 * @return True if a triangle is not yet processed else false
	 *
	 */
	private boolean _isProcessedTriangle(ConvexPolygon2 t)
	{
		Iterator<ConvexPolygon2> it = _triDone.iterator();
		while ( it.hasNext() ) {
			if ( t.equals( it.next() ) ) {
				return true;
			}
		}
		return false;
	}


	/**
	 *
	 * @param tri A triangle
	 * @param v1 A one vertex of a triangle
	 * @param v2 A second vertex of a triangle
	 * @return The third vertex of a triangle
	 *
	 */
	private Point2 _getThirdTriangleVertex(
		ConvexPolygon2 tri,
		Point2 v1,
		Point2 v2)
	{
		SimpleList vertices = tri.points();
		ListItem it = null;
		while ( (it = vertices.next(it)) != null ) {
			Point2 v = (Point2) it.value();
			if ( ! v1.equals(v) && ! v2.equals(v) ) {
				return v;
			}
		}
		return null;
	}


	/**
	 *
	 * @param s An apex of a funnel
	 * @param a One end of a window
	 * @param b Other end of a window
	 * @param c A foot of the perpendicular from the apex of the funnel and the
	 * window that is the funnel base
	 * @return The angle between
	 *
	 */
	private double computeInnerAngle(Point2 s, Point2 a, Point2 b, Point2 c)
	{
		double angle = a.angle(s, c);
		System.out.println("Angle " + s+" - "+a+" - "+c + " = " + angle);
		if ( s.angle(b, a) > Math.PI ) {
			System.out.println("Get complement of it");
			angle = 2 * Math.PI - angle;
		}
		return angle;
	}
	
	
	/**
	 * Recursive method to calculate the childs of each node and build
	 * up the tree.
	 * 
	 * @param n The active node, the childs should be initialized for
	 * @param p The array with the child->parent relations.
	 */
	private void _initialize(SPTNode n, Point2[][] p, boolean isRoot)
	{
		ArrayList<Point2> list = new ArrayList<Point2>();
		
		for (int i=0; i < p[0].length; ++i)
		{
			if (((Point2)n).equals(p[1][i]))
				list.add(p[0][i]);
		}
		
		n.setID(_poly.indexOf(n));
		
		if (list.size() > 0)
		{
			Point2[] p_a = list.toArray(new Point2[0]);
			
			if (!isRoot)
			{
				Point2 firstP = p_a[0];
				for (int j=1; j < p_a.length; j++)
				{
					if (n.angle(firstP, p_a[j]) > Math.PI)
						firstP = p_a[j];
				}
				for (int j=0; j < p_a.length; j++)
				{
					p_a[j].setValue((float)(n.angle(firstP, p_a[j])));
				}
			}
			else
			{
				ListItem li = _poly.points().first();
				
				while (!((Point2)(li.value())).equals(n))
					li = li.next();
				
				Point2 firstP = (li.prev() == null) ? _poly.lastPoint() : (Point2)(li.prev().value());
				for (int j=0; j < p_a.length; j++)
				{
					p_a[j].setValue((float)(n.angle(firstP, p_a[j])));
				}
			}
						
			n.setChilds(p_a);
			Arrays.sort(n.getChilds());
			
			for (int i=0; i < list.size(); ++i)
			{
				_initialize(n.getChild(i), p, false);
				n.getChild(i).setParent(n);
			}
		}
	}
}
